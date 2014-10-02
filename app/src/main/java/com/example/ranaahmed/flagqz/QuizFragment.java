package com.example.ranaahmed.flagqz;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by Rana Ahmed on 9/30/2014.
 */
public class QuizFragment extends Fragment{
    private static final String TAG = "QuizFrag Activity";
    private static final int FLAGS_IN_QUIZ = 10;
    private List<String> fileNameList;
    private List<String> quizCountriesList;
    private Set<String> regionsSet;
    private String correctAnswer;
    private int totalGuesses;
    private int correctAnswers;
    private int guessRows;
    private SecureRandom random;
    private Handler handler;
    private Animation shakeAnimation;

    private TextView questionNumberTextView;
    private ImageView flagImageView;
    private LinearLayout[] guessLinearLayouts;
    private TextView answerTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_quiz,container, false);
        fileNameList = new ArrayList<String>();
        quizCountriesList = new ArrayList<String>();
        random = new SecureRandom();
        handler = new Handler();

        shakeAnimation = AnimationUtils.loadAnimation(getActivity(),R.anim.incorrect_shake);
        shakeAnimation.setRepeatCount(3);

        questionNumberTextView = (TextView)view.findViewById(R.id.questionNumberTextView);
        flagImageView = (ImageView)view.findViewById(R.id.flagImageView);
        guessLinearLayouts = new LinearLayout[3];

        guessLinearLayouts[0] =
                (LinearLayout) view.findViewById(R.id.row1LinearLayout);
        guessLinearLayouts[1] =
                (LinearLayout) view.findViewById(R.id.row2LinearLayout);
        guessLinearLayouts[2] =
                (LinearLayout) view.findViewById(R.id.row3LinearLayout);
        answerTextView = (TextView) view.findViewById(R.id.answerTextView);

        for(LinearLayout row:guessLinearLayouts){
            for(int column = 0; column <row.getChildCount();column++){
            Button button = (Button)row.getChildAt(column);
            button.setOnClickListener(guessButtonListener);
        }}

        questionNumberTextView.setText(getResources().getString(R.string.question,1,FLAGS_IN_QUIZ));
        return view;
    }

       public void updateGuessRows(SharedPreferences sharedPreferences){
           String choices = sharedPreferences.getString(MyActivity.CHOICES, null);
           guessRows = Integer.parseInt(choices)/3;
           for(LinearLayout layout: guessLinearLayouts)
               layout.setVisibility(View.INVISIBLE);

           for(int row = 0;row<guessRows;row++)
               guessLinearLayouts[row].setVisibility(View.VISIBLE);
       }

    public void updateRegions(SharedPreferences sharedPreferences){
        regionsSet = sharedPreferences.getStringSet(MyActivity.REGIONS, null);
    }

    public void resetQuiz(){
        AssetManager assets = getActivity().getAssets();
        fileNameList.clear();
        try{
            for(String region: regionsSet){
                String[] paths = assets.list(region);
                for(String path: paths)
                    fileNameList.add(path.replace(".png",""));
            }
        }
        catch (IOException e){
            Log.e(TAG, "Error loading image filename",e);
        }
        correctAnswers = 0;
        totalGuesses = 0;
        quizCountriesList.clear();

        int flagCoutner = 1;
        int numberOfFlags = fileNameList.size();

        while(flagCoutner <= FLAGS_IN_QUIZ){
            int randomIndex = random.nextInt(numberOfFlags);
            String fileName = fileNameList.get(randomIndex);
            if(!quizCountriesList.contains(fileName)){
                quizCountriesList.add(fileName);
                ++flagCoutner;
            }
        }
        loadNextFlag();
    }

    private void loadNextFlag(){
        String nextImage = quizCountriesList.remove(0);
        correctAnswer = nextImage; // update the correct answer
        answerTextView.setText("");

        questionNumberTextView.setText((getResources().getString(R.string.question,(correctAnswers +1), FLAGS_IN_QUIZ)));
        String region = nextImage.substring(0,nextImage.indexOf('-'));
        AssetManager assets = getActivity().getAssets();

        try{
            InputStream st = assets.open(region + "/" + nextImage + ".png");
            Drawable flag = Drawable.createFromStream(st, nextImage);
            flagImageView.setImageDrawable(flag);

        }
        catch (IOException e){
            Log.e(TAG,"Error Loading " + nextImage,e);
        }
        Collections.shuffle(fileNameList);
        int correct = fileNameList.indexOf(correctAnswer);
        fileNameList.add(fileNameList.remove(correct));
        for(int row = 0; row < guessRows; row++){
            for(int column =0;column   <guessLinearLayouts[row].getChildCount();column++){
                Button newGuessButton =
                        (Button) guessLinearLayouts[row].getChildAt(column);
                newGuessButton.setEnabled(true);

                // get country name and set it as newGuessButton's text
                String fileName = fileNameList.get((row * 3) + column);
                newGuessButton.setText(getCountryName(fileName));
            }
        }
        int row = random.nextInt(guessRows);
        int column = random.nextInt(3);
        LinearLayout randomRow = guessLinearLayouts[row];
        String countryName = getCountryName(correctAnswer);
        ((Button)randomRow.getChildAt(column)).setText(countryName);

    }
    private String getCountryName(String name)
    {
        return name.substring(name.indexOf('-') + 1).replace('_', ' ');
    }
    private void disableButtons(){
        for(int row=0; row<guessRows; row++){
            LinearLayout guessRow = guessLinearLayouts[row];
            for(int i=0;i<guessRow.getChildCount();i++){
                guessRow.getChildAt(i).setEnabled(false);
            }
        }
    }
    private View.OnClickListener guessButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Button guessButton = ((Button)view);
            String guess = guessButton.getText().toString();
            String answer = getCountryName(correctAnswer);
            ++totalGuesses;
            if(guess.equals(answer)){
                ++correctAnswers;
                answerTextView.setText(answer + "1");
                answerTextView.setTextColor(getResources().getColor(R.color.correct_answer));
                disableButtons();
                if(correctAnswers == FLAGS_IN_QUIZ){
                    DialogFragment quizResults = new DialogFragment(){
                        @Override
                        public Dialog onCreateDialog(Bundle bundle){
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                    builder.setCancelable(false);
                            builder.setMessage(getResources().getString(R.string.results,totalGuesses,(1000 /(double)totalGuesses)));
                            builder.setPositiveButton(R.string.reset_quiz,new DialogInterface.OnClickListener(){
                               public void onClick(DialogInterface dialog,int id){
                                   resetQuiz();
                               }
                            });
                            return builder.create();
                        }
                    };

                }
                else // answer is correct but quiz is not over
                {
                    // load the next flag after a 1-second delay
                    handler.postDelayed(
                            new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    loadNextFlag();
                                }
                            }, 2000); // 2000 milliseconds for 2-second delay
                }

            }
            else
            {
                flagImageView.startAnimation(shakeAnimation); // play shake

                // display "Incorrect!" in red
                answerTextView.setText(R.string.incorrect_answer);
                answerTextView.setTextColor(
                        getResources().getColor(R.color.incorrect_answer));
                guessButton.setEnabled(false);
            }
    };
    };


}
