package com.ertan.numbers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

public class main extends Activity {
    /** Called when the activity is first created. */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.landing);
    }
    public void startGame(View v){
    	Intent intent = new Intent(this,Game.class);
    	startActivity(intent);
    }
    public void settings(View v){
    	Intent intent = new Intent(this,Settings.class);
    	startActivity(intent);
    }
    public void showScores(View v){
    	Intent intent = new Intent(this,Scores.class);
    	startActivity(intent);
    }
    public void showHowto(View v){
    	Intent intent = new Intent(this,Howto.class);
    	startActivity(intent);
    }

}