package com.example.ranaahmed.flagqz;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.Set;


public class MyActivity extends Activity {


    private static final String CHOICES = "pref_numberOfChoices";
    private static final String REGIONS = "pref_regionsToInclude";
    private boolean phoneDevice = true;
    private boolean preferenceChanged = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        PreferenceManager.setDefaultValues(this,R.xml.preferences,false); // Used to initialize preference as false
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(preferenceChangeListener);

        int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        if(screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE || screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE){
            phoneDevice = false;
        }
        if(phoneDevice)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected void onStart(){
        super.onStart();

        if(preferenceChanged){
            QuizFragment q = (QuizFragment)getFragmentManager().findFragmentById(R.id.quizFragment);
            q.updateGuessRows(PreferenceManager.getDefaultSharedPreferences(this));
            q.updateRegions(PreferenceManager.getDefaultSharedPreferences(this));
            q.resetQuiz();
            preferenceChanged = false;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        Display d = ((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        Point s = new Point();
        d.getRealSize(s);
        if(s.x < s.y){
        getMenuInflater().inflate(R.menu.my, menu);
        return true;}
        else
            return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
        Intent i = new Intent(this,SettingsActivity.class);
        startActivity(i);
        return super.onOptionsItemSelected(item);
    }

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener =
            new SharedPreferences.OnSharedPreferenceChangeListener(){
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key){
            preferenceChanged = true;
            QuizFragment quizFragment = (QuizFragment)getFragmentManager().findFragmentById(R.id.quizFragment);

            if(key.equals(CHOICES)){
                quizFragment.updateGuessRows(sharedPreferences);
                quizFragment.resetQuiz();
            }
            else if(key.equals(REGIONS)){
                Set<String> regions = sharedPreferences.getStringSet(REGIONS,null);
                if(regions != null && regions.size() >0){
                    quizFragment.updateReqgions(sharedPreferences);
                    quizFragment.resetQuiz();

                }
                else
                {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    regions.add(
                            getResources().getString(R.string.default_region));
                    editor.putStringSet(REGIONS, regions);
                    editor.commit();
                    Toast.makeText(MyActivity.this,
                            R.string.default_region_message,
                            Toast.LENGTH_SHORT).show();
                }
            }
            Toast.makeText(MyActivity.this,
                    R.string.restarting_quiz, Toast.LENGTH_SHORT).show();

        }


    };
}
