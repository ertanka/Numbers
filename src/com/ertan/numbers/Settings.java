package com.ertan.numbers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;
/**
 * Setting screen handler.
 * Nothing special here actually.
 * @author ertan
 *
 */
public class Settings extends Activity implements android.view.View.OnClickListener{
	Properties properties;
	private ArrayList<Button> buttons;
	private int dim;
    SharedPreferences settings ;
    private ToggleButton relaxToggle;

	 public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
	        setContentView(R.layout.settings);
	        settings= getSharedPreferences("numbers_settings",0);
	        relaxToggle=(ToggleButton)findViewById(R.id.relaxToggle);
	        relaxToggle.setChecked(settings.getBoolean("relax", false));
	        buttons= new ArrayList<Button>();
	        buttons.add((Button)findViewById(R.id.button1));
	        buttons.add((Button)findViewById(R.id.button2));
	        buttons.add((Button)findViewById(R.id.button3));
	        buttons.add((Button)findViewById(R.id.button4));
	        buttons.add((Button)findViewById(R.id.button5));
	        buttons.add((Button)findViewById(R.id.button6));
	        for(Button b:buttons){
	        	b.setOnClickListener(this);
	        }	
	            dim=settings.getInt("dim", 5);
	            ColorButtons();
       
	 }
	    public void save(View v){
	    	SharedPreferences.Editor edit=settings.edit();
	    	edit.putInt("dim", dim);
	    	edit.putBoolean("relax", relaxToggle.isChecked());
	    	edit.commit();
	    	finish();
	    }
	 private void ColorButtons(){
		 for(int i=0;i<buttons.size();i++){
			 buttons.get(i).invalidate();
			 if(i+5==dim){
				 buttons.get(i).getBackground().clearColorFilter();
				 buttons.get(i).getBackground().setColorFilter(0xFFFF0000, android.graphics.PorterDuff.Mode.MULTIPLY);

			 }
			 else{
				 buttons.get(i).getBackground().clearColorFilter();
				 //buttons.get(i).getBackground().setColorFilter(new LightingColorFilter(0xFFFFFFFF, 0xFFAA0000));
		 	}
		 }
		 
		 if(dim>9 || dim<5){
			 buttons.get(5).getBackground().clearColorFilter();
			 buttons.get(5).getBackground().setColorFilter(0xFFFF0000, android.graphics.PorterDuff.Mode.MULTIPLY);
			 buttons.get(5).setText("Custom Value ("+dim+")");

		 }
	 }

	public void onClick(View v) {
		if(!v.equals(buttons.get(5))){
			int pressed=buttons.indexOf((Button)v);
			int newDim=pressed+5;
			if(newDim==dim)
				return;
			else{
				dim=newDim;
				Log.i("Setting","New dim:"+dim);
				 buttons.get(5).setText("Custom Value");
				ColorButtons();
			}
		}
		else{
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Enter Custom Value");
			alert.setMessage("There is no limit about this value. However, it may cause problems in layout. So enter wisely :)");

			// Set an EditText view to get user input 
			final EditText input = new EditText(this);
			input.setInputType(InputType.TYPE_CLASS_NUMBER);
			alert.setView(input);

			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			  String value = input.getText().toString();		  
			  	int val=Integer.parseInt(value);
			  	if(val<2){
			  		Toast.makeText(getApplicationContext(), "Dimension cannot be less than 2", 3000).show();
			  		return;
			  	}
			  	dim=val;
			  	ColorButtons();
			  	if(val>20){
			  		Toast.makeText(getApplicationContext(), "This value may cause some problems. Please keep in mind", 3000).show();			  		
			  	}
			  }
			});

			alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int whichButton) {
			    return;
			  }
			});

			alert.show();
		}
	}
	public void clearScores(View v){
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Clear Hi-Scores");
		final int confirm=new Random(Calendar.getInstance().getTimeInMillis()).nextInt(1000);
		alert.setMessage("Please enter \""+confirm+"\" below to clear scores");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_NUMBER);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		  String value = input.getText().toString();	
		  if(value.equals(""))
			  return;
		  	int val=Integer.parseInt(value);
		  	if(val==confirm){
		  		SharedPreferences.Editor editor=getSharedPreferences("numbers_scores",0).edit();
		  		editor.remove("scores");
		  		editor.commit();
		  	}
		  }
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    return;
		  }
		});

		alert.show();
	}

}
