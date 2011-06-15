package com.ertan.numbers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Shows a form to gather information to send to the server.
 * Currently sends the score to the server which is hosted on google appEngine.
 * There is no checks or encoding. Using just a simple http post currently.
 * Getting the username from previous activity if it is entered this time.
 * @author ertan
 *
 */
public class GlobalScoreHandler extends Activity{
	TextView scoreText;
	private int score;
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        score=getIntent().getExtras().getInt("score");
        setContentView(R.layout.global);
        scoreText=(TextView)findViewById(R.id.globalScoreText);
        scoreText.setText("Your Score: "+score);
        ((EditText)findViewById(R.id.globalNameBox)).setText(getIntent().getExtras().getCharSequence("name"));
	}
	/**
	 * Finishes the activity and goes back to the main menu.
	 * @param v
	 */
	public void cancel(View v){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Do you really want to ignore this score?")
		       .setCancelable(false)
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	finish();
		           }
		       })
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                return;
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	/***
	 * Activates the send process.
	 * @param v
	 */
	public void send(View v){
		try {
		    // Construct data
		    String data = URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(((EditText)findViewById(R.id.globalNameBox)).getText().toString(), "UTF-8");
		    data += "&" + URLEncoder.encode("comment", "UTF-8") + "=" + URLEncoder.encode(((EditText)findViewById(R.id.globalCommentBox)).getText().toString(), "UTF-8");
		    //Getting email to inform on a new highscore is not enabled right now. so it is commented out.
		    //data += "&" + URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(((EditText)findViewById(R.id.globalMailBox)).getText().toString(), "UTF-8");
		    data += "&" + URLEncoder.encode("score", "UTF-8") + "=" + URLEncoder.encode(""+score, "UTF-8");
		    // Send data
		    URL url = new URL("http://numbershs.appspot.com/save");
		    URLConnection conn = url.openConnection();
		    conn.setDoOutput(true);
		    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		    wr.write(data);
		    wr.flush();

		    // Get the response
		    //if the response is "true"(String) then score save successfully. 
		    //if it is "false" then entered username has a better highscore.
		    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		    String line;
		    while ((line = rd.readLine()) != null) {
		    	if(line.equals("true")){
					Toast.makeText(this, "Score Sent!", Toast.LENGTH_SHORT).show();
					finish();
		    	}
		    	else{
		    		Toast.makeText(this, "\""+((EditText)findViewById(R.id.globalNameBox)).getText().toString()+"\" already has a higher score.", Toast.LENGTH_SHORT).show();
		    	}
		    }
		    wr.close();
		    rd.close();

		} catch (Exception e) {
			Toast.makeText(this, "An error occured!,Please try again.", Toast.LENGTH_SHORT).show();
		}

	}
}
