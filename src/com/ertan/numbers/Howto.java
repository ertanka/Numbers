
package com.ertan.numbers;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

/**
 * Uses a TextSwitcher.
 */
public class Howto extends Activity implements ViewSwitcher.ViewFactory,
        View.OnClickListener {

    private TextSwitcher mSwitcher;
    private TextView status;

    private int mCounter = 1;
    private int pageCount;
    private Button nextButton;
    private String[] texts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.howto);

        mSwitcher = (TextSwitcher) findViewById(R.id.switcher);
        mSwitcher.setFactory(this);
        status=(TextView) findViewById(R.id.status);
        Animation in = AnimationUtils.loadAnimation(this,
                android.R.anim.fade_in);
        Animation out = AnimationUtils.loadAnimation(this,
                android.R.anim.fade_out);
        mSwitcher.setInAnimation(in);
        mSwitcher.setOutAnimation(out);

        nextButton = (Button) findViewById(R.id.next);
        nextButton.setOnClickListener(this);
        initTexts();
        pageCount=texts.length-1;
        updateText();
        updateCounter();
    }

    private void initTexts(){
    	texts=new String[]{"","The Game\n\nAim of this game is to fill every single box with numbers.\nIn the normal version (which is the only choise for now) it is forbidden to place the second number;\ncloser than 2 boxes vertically and horizontally\nand\ncloser than 1 box crosswise.",
    			"Controls\n\nYou can click on a box to place the next number.\nIf you click on the highest numbered box on the board, you can undo the last move\nThere is no limit on undo operation but it may cost you some points.",	
    			"Scoring\n\nOn the normal mode, You get points if you click on a button in less than 5 seconds.\nIf you redo a move you lose some points in this mode.\nEvery username has only 1 score and if a username has a higher score you cannot save the new score over the older one.",
    			"Glocal Score List\n\nJust like the local score list, every username has one score and one comment field\nIf there is no score associated with the username or you have a higher score, you can submit your score and comment.",
    			"Relax Mode\n\nActivating relax mode removes the time limits of the game.\nIn this mode, you don't have to place the next number in 5 seconds in order to get points.\nHowever, you still have to obey the rules depending on the game type.\nEvery correct number worths 3 points and you lose 7 points for every redo.",
    			"Contact\n\nThere may be some bugs on the game as you can imagine :)\nIf you are unlucky enough to encounter one please inform us by sending an email to\nnumbers.android@gmail.com\nYou may also find a way to reach us on our blog:\nblog.thesaykan.com",
    			"What is next?\n\nStrict mode\nFancy animations and graphics\nMore?\n\nIf you have any suggestions we would like to hear them. Please email us!"};
    }
    public void onClick(View v) {
    	if(mCounter==pageCount){
    		finish();
    		return;
    	}
        if(mCounter==pageCount-1){
        	nextButton.setText("Finish");
        }
        mCounter++;
        updateCounter();
        updateText();
    }

    private void updateCounter() {
        status.setText(mCounter+"/"+pageCount);
    }
    private void updateText(){
    	if(mCounter==texts.length)
    		return;
    	mSwitcher.setText(texts[mCounter]);
    }

    public View makeView() {
        TextView t = new TextView(this);
        t.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        t.setTextSize(16);
        return t;
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
      savedInstanceState.putInt("counter", mCounter);
      super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
      super.onRestoreInstanceState(savedInstanceState);
      mCounter=savedInstanceState.getInt("counter");
      updateText();
      updateCounter();
    }
}