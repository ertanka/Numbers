package com.ertan.numbers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Game extends Activity implements OnClickListener{
    /** Called when the activity is first created. */
	private LinearLayout mainLayout;
	private LinearLayout[] lines;
	private int[] locations;
	private int dim;
	private int time;
	private ArrayList<Button> buttons;
	private int curr;
	private Handler handler;
	private TextView timeView;
	private Thread timerThread;
	private boolean active;
	private AlertDialog.Builder successDialog;
	private HashMap<String,Integer> scoreMap;
	private int score;
	private long lastPress;
	private boolean relax;
	private boolean scoreBypass;
	private String lastEnteredName="";
	private int undoPenalty=7;
	private int relaxScore=3;
	private int buttonTime=5;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.game);
        
        //Handler to increase the counter.
        handler = new Handler() {
        	@Override
        	public void handleMessage(Message msg) {
        		super.handleMessage(msg);
        		if(active)
        			time++;
        		timeView.setText("Time passed: "+time);
        	}
        };
        //initializing the game

        SharedPreferences pref=getSharedPreferences("numbers_settings", 0);
        dim=pref.getInt("dim", 5);
        relax=pref.getBoolean("relax", false);
        active=true;
        scoreBypass=false;
        score=0;     
        locations=new int[dim*dim];
        for(int i=0;i<dim*dim;i++){
        	locations[i]=-1;
        }
        //Setting up the UI
        mainLayout=(LinearLayout)findViewById(R.id.mainHolder);
        timeView=(TextView)findViewById(R.id.time);
        lines=new LinearLayout[dim];

        buttons=new ArrayList<Button>();
        for(int j=0;j<dim;j++){
        	lines[j]=new LinearLayout(this);
        	lines[j].setOrientation(LinearLayout.HORIZONTAL);
        	lines[j].setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
                    LayoutParams.WRAP_CONTENT,(float)1/dim));
        	for(int i=0;i<dim;i++){
        		Button b=new Button(this);
        		b.setTextSize(20-dim);
        		b.setId(j*dim+i);
        		b.setText("");
        		b.setOnClickListener(this);
        		b.setVisibility(View.VISIBLE);
        		b.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT,(float)1/dim));
        		lines[j].addView(b);
        		buttons.add(b);
        	}
        }
        //in order to put "1" to different place every time.
        if(savedInstanceState==null){
            Random r=new Random(Calendar.getInstance().getTimeInMillis());
            int rand=r.nextInt(dim*dim);
            buttons.get(rand).setText(""+1);
            locations[rand]=0;
        }
        curr=1;
        for(LinearLayout l:lines){
        	mainLayout.addView(l);
        }
        
        //Get ready to start
        lastPress=Calendar.getInstance().getTimeInMillis();
        time=0;

        if(relax){
        	timeView.setText("Relax Mode");
        }
        timerThread=initializeTimeCounter();
        //This null checks could be if(!relax) instead. But I preferred this. I think they may be useful later.
        //However, I am not sure about the performance difference between these two methods.
        
        if(timerThread!=null)
        	timerThread.start();
        	
    }
    /**
     * This method converts a Comma Seperated Value string into HashMap. There is no checks while doing this.
     * So I assume that the input string is always a valid one.
     * In case of an error it may be necessary to clear high score table.
     * @param value (like "ert,123,ertan,234")
     * @return corresponding hashmap.
     */
    protected static HashMap<String,Integer> decodeCSV(String value){
    	HashMap<String,Integer> map =new HashMap<String,Integer>();
    	if(value==null){
    		return map;
    	}
    	String[] keyAndVal=value.split(",");
    	for(int i=0;i<keyAndVal.length;i=i+2){
    		map.put(keyAndVal[i], Integer.parseInt(keyAndVal[i+1]));
    	}
    	return map;
    }
    
    /**
     * This method takes a high score hashMap and encodes it into a Comma Seperated Value String
     * By doing this we can save this in SharedPreferences of Android platform.
     * It may be unnecessary to keep highscores in local?
     * @param map High Score Map
     * @return Comma Seperated Value string of given map
     */
    protected static String encodeCVS(HashMap<String,Integer> map){
    	String encoded="";
    	for(String key:map.keySet()){
    		encoded+=key+","+map.get(key)+",";
    	}
    	return encoded.substring(0, encoded.length()-1);
    }
    
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
      savedInstanceState.putInt("curr", curr);
      savedInstanceState.putIntArray("locations", locations);
      savedInstanceState.putInt("time",time);
      savedInstanceState.putInt("score",score);
      savedInstanceState.putLong("lastPress",lastPress);
      savedInstanceState.putBoolean("active",active); //is game active or not
      savedInstanceState.putBoolean("relax", relax); //relax mode status.
      savedInstanceState.putBoolean("scoreBypass", scoreBypass); //To bypass the "you made a high score" screen if it is shown once and orientation changed after.
      super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
      super.onRestoreInstanceState(savedInstanceState);
      locations=savedInstanceState.getIntArray("locations");
      curr=savedInstanceState.getInt("curr");
      time=savedInstanceState.getInt("time");
      score=savedInstanceState.getInt("score");
      lastPress =savedInstanceState.getLong("lastPress");
      active=savedInstanceState.getBoolean("active");
      relax=savedInstanceState.getBoolean("relax");
      scoreBypass=savedInstanceState.getBoolean("scoreBypass");
      timeView.setText("Time passed: "+time);
      restoreButtonTexts();
      if(curr==dim*dim){
    	  endGame();
      }
      if(relax){
    	  timeView.setText("Relax Mode");
      }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.new_game:
        	active=false;
        	if(timerThread!=null)
        		timerThread.interrupt();
            clear();
            return true;

        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    private void restoreButtonTexts(){
    	for(int i=0;i<dim*dim;i++){
    		if(locations[i]!=-1){
    			buttons.get(i).setText(""+(locations[i]+1));
    			buttons.get(i).getBackground().clearColorFilter();
    		}
    	}
    }

    /**
     * Handles the button clicks during the game.
     * There is no check when game is active. So player can put the next number anywhere he likes.
     */
	public void onClick(View v) {
		Button pressed=(Button)v;
		//If the button is not empty and user clicked on the latest number then clear it.
		if(!pressed.getText().toString().equals("")){
			//Decreases the current number, reduces the score if necessary and resets the time counter
			if(locations[v.getId()]==curr-1 && locations[v.getId()]!=0){
					curr--;
					locations[v.getId()]=-1;
					pressed.setText("");
					pressed.getBackground().clearColorFilter();
					score-=undoPenalty;
					lastPress=Calendar.getInstance().getTimeInMillis();
					return;
			}
			return;
		}
		//Score calculations
		
		if(!relax){
			//Give points if player presses less than some specific time.
			int newScore=buttonTime-((int)(Calendar.getInstance().getTimeInMillis()-lastPress)/1000);
			if(newScore>0){
				score+=newScore;
			}
			lastPress=Calendar.getInstance().getTimeInMillis();
		}else{		
			//Every button has fixed score if relax mode is chosen regardless of the time.
			score+=relaxScore;
		}
		//Change the text and prepare for the next button
		pressed.setText(""+(curr+1));
		locations[v.getId()]=curr;
		curr++;
		
		//Check if the game ended or not
		if(curr==dim*dim){
			endGame();
			return;
		}
	}
	/**
	 * This function is called when the game ends and user has at least 1 mistake
	 * Function shows and alert and handles the response.
	 */
	private void failed(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("You made a mistake! Would you like to start a new game?")
		       .setCancelable(false)
		       .setPositiveButton("Clear Table", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	active=false;
		        	if(timerThread!=null)
		        		timerThread.interrupt();
		                clear();
		           }
		       })
		       .setNegativeButton("Let me fix.", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                return;
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
		}
	
	/**
	 * In the case of successful completion.
	 */
	private void success(){
		final int newScore=this.score;
		if(timerThread!=null)
			timerThread.interrupt();
		active=false;
		successDialog = new AlertDialog.Builder(this);
		successDialog.setMessage("Congratulations!. What would you like to do now? ")
		       .setCancelable(false)
		       .setPositiveButton("Start Again", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                clear();
		           }
		       })
		       .setNeutralButton("Global Scores", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
	                scoreBypass=false;
			    	showGlobalScore();					
				}
			})
		       .setNegativeButton("Back to Menu", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                finish();
		           }
		       });
        SharedPreferences score= getSharedPreferences("numbers_scores",0);
        scoreMap=decodeCSV(score.getString("scores", null));
        Log.i("Game","bypass:"+scoreBypass);
        if(!scoreBypass && (scoreMap.size()<=10 || isHighScore(scoreMap,newScore))){
        	
        	AlertDialog.Builder alert = new AlertDialog.Builder(this);
        	alert.setCancelable(false);
        	alert.setTitle("Hi-Score!");
			alert.setMessage("Your score :"+newScore+"\n");

			// Set an EditText view to get user input 
			final EditText input = new EditText(this);
			input.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
			input.setHint("Your name here!");
			alert.setView(input);

			alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			    String value = input.getText().toString();	
			    if(value=="")
			    	return;
			    if(scoreMap.containsKey(value)){
			    	int oldVal=scoreMap.get(value);
			    	if(oldVal>newScore){
			    		Toast.makeText(getApplicationContext(), "This user has a higher score..", 3000).show();
			    		success();
			    		return;
			    	}
			    }
			    String tempUsername=value.split(",")[0];
			  	scoreMap.put(tempUsername, newScore);
			  	lastEnteredName=tempUsername;
		        SharedPreferences score= getSharedPreferences("numbers_scores",0);
		        SharedPreferences.Editor editor=score.edit();
		        editor.putString("scores", encodeCVS(scoreMap));
		        editor.commit();
		        scoreBypass=true;
		        AlertDialog alert = successDialog.create();
				alert.show();
			  }
			});

			alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int whichButton) {
				  scoreBypass=true;
				AlertDialog alert = successDialog.create();
				alert.show();
			    return;
			  }
			});

			alert.show();
        }
        else{
        	AlertDialog alert = successDialog.create();
    		alert.show();
        }
		
		
	}
	/**
	 * This method show the Global High Score Activity.
	 * Currently only passes lastly entered name and score but some other data may be 
	 * included to improve usability.
	 */
	private void showGlobalScore(){
    	Intent intent = new Intent(this,GlobalScoreHandler.class);
    	intent.putExtra("score", score);
    	intent.putExtra("name", lastEnteredName);
    	startActivity(intent);
    	finish();
	}
	
	/**
	 * Takes the current score map and the newly made score and checks whether it is a high score or not
	 * @param map The hashMap that holds the current score list.
	 * @param value the score that will be checked.
	 */
	protected static boolean isHighScore(HashMap<String,Integer> map,int value){
		int biggerCount=0;
		for(int oldScore:map.values()){
			if(oldScore>value)
				biggerCount++;
		}
		if(biggerCount<10){
			return true;
		}
		return false;
	}
	/**
	 * Generates a new timer which sleeps for a second and sends a message to the handler.
	 * !!Handler should be initialized before starting the time..
	 * @return the new timer.
	 */
	private Thread initializeTimeCounter(){
		if(relax){
			return null;
		}
		return new Thread() {
        	public void run() {
        		while(true){
	        		try {
						sleep(1000);
					} catch (InterruptedException e) {
					}
					if(!active)
						break;
	        		handler.sendEmptyMessage(0);
        	}
        	}
        	};
	}
	
	/**
	 * Clears the board and makes the board ready for a new game.
	 * This can be called to restart the game..
	 */
	private void clear(){
		curr=1;
		score=0;
		Random r=new Random(Calendar.getInstance().getTimeInMillis());
		int rand=r.nextInt(dim*dim);
		for(int i=0;i<dim*dim;i++){
			locations[i]=-1;
			buttons.get(i).setText("");
			buttons.get(i).getBackground().clearColorFilter();
		}
		buttons.get(rand).setText(""+1);
		locations[rand]=0;
		time=0;
        scoreBypass=false;
		timerThread=initializeTimeCounter();
		if(timerThread!=null)
			timerThread.start();
        active=true;
		
	}
	
	/**
	 * Checks if player successfully finished the game or not.
	 * Current method is just a simple solution. However, in order to implement dynamic rules
	 * we have to get another way to check the board.
	 */
	private void endGame(){
		for(int i=0;i<dim*dim;i++){
			ArrayList<Integer> checkList=new ArrayList<Integer>();
			checkList.add(i+dim);
			checkList.add(i+2*dim);
			checkList.add(i-dim);
			checkList.add(i-2*dim);
			if(i%dim>0){
				checkList.add(i-1);
				if(i%dim>1){
					checkList.add(i-2);
					checkList.add(i-dim-1);
					checkList.add(i+dim-1);
				}
			}
			if(i%dim<dim-1){
				checkList.add(i+1);
				checkList.add(i-dim+1);
				checkList.add(i+dim+1);
				if(i%dim<dim-2){
					checkList.add(i+2);
				}
			}
			for(int j:checkList){
				if(j>0 && j<dim*dim){
					if(locations[j]==locations[i]-1 || locations[j]==locations[i]+1){
						buttons.get(j).getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, 0xFFAA0000));
						buttons.get(i).getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, 0xFFAA0000));
						failed();
						return;
					}
				}
			}
		}
		success();
	}
}