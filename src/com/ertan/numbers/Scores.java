package com.ertan.numbers;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.view.Window;

import java.io.*;
import java.net.*;
import java.util.*;

public class Scores extends Activity
{
	HashMap<String,Integer> list ;
	private boolean local=true;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.scores);
	    printScores();
    }
    private void printScores(){
    	if(local)
	    	showLocal();
	    else
	    	showGlobal();
    }
    private void showGlobal(){
    	LinearLayout ll=(LinearLayout) findViewById(R.id.listLayout);
    	ll.removeAllViews();
    	try 
        {
            URL url = new URL( "http://numbershs.appspot.com/" );
        
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str;

            while ((str = in.readLine()) != null) 
            {
              TextView temp=new TextView(this);
              temp.setText(str.trim());
              ll.addView(temp);
            }

            in.close();
        } 
        catch (Exception e) {
        	Toast.makeText(this, "Couldn't connect the server. Please try again later.", Toast.LENGTH_LONG);
        }

    }
    private void showLocal(){
    	LinearLayout ll=(LinearLayout) findViewById(R.id.listLayout);
    	ll.removeAllViews();
        SharedPreferences score= getSharedPreferences("numbers_scores",0);
	    list=Game.decodeCSV(score.getString("scores", null));
	    
		 // put some tuples in yourMap ...
	
		 // to hold the result
		 HashMap<String,Integer> map = new LinkedHashMap<String,Integer>();
	
	
		 List<String> mapKeys = new ArrayList<String>(list.keySet());
		 List<Integer> mapValues = new ArrayList<Integer>(list.values());
		 TreeSet<Integer> sortedSet = new TreeSet<Integer>(mapValues);
		 Object[] sortedArray = sortedSet.toArray();
		 int size = sortedArray.length;
	
		 for (int i=0; i<size; i++) {
		    map.put
		       (mapKeys.get(mapValues.indexOf(sortedArray[i])),
		        (Integer)sortedArray[i]);
		 }
		 Set<String> ref = map.keySet();
		 Iterator<String> it = ref.iterator();
		 while (it.hasNext()) {
		   String name = (String)it.next();
		   TextView v=new TextView(this);
	   		v.setText(name+"-->"+list.get(name));
	   		v.setTextSize(15);
	   		ll.addView(v);
		 }
    }
    public void close(View v){
    	finish();
    }
    public void toggleScore(View v){
    	local=!local;
    	printScores();
    }
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
      savedInstanceState.putBoolean("local", local);
      super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
      super.onRestoreInstanceState(savedInstanceState);
      local=savedInstanceState.getBoolean("local");
      printScores();
    }
}
