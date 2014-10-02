package com.example.ranaahmed.flagqz;

/**
 * Created by Rana Ahmed on 10/1/2014.
 */

    import android.app.Activity;
    import android.os.Bundle;

    public class SettingsActivity extends Activity
    {
        // use FragmentManager to display SettingsFragment
        @Override
        protected void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_settings);
        }
    } // end class SettingsActivity

