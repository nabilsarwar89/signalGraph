package com.example.radarapp;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	protected static final int REQUEST_SAVE = 0;
	private static final int REQUEST_LOAD = 0;
	private InputStream is;
	private Context context;
	private int resId;
	private String chosenFile;
	private Boolean chosen;
	
	
	 @Override
	    public void onCreate(Bundle b)
	    {
		 super.onCreate(b);
	     setContentView(R.layout.activity_main);
	     context = this.getApplicationContext();
	     
	     TextView tv = (TextView) findViewById(R.id.textView1);
	     tv.setText("\"This App can be used to process \nWave Files and display various\ngraphs based on it's samples.\n" +
	     		"It can be useful for Processing\nRadar output data (wav).\"");
	    
	        
	     Button chooseFile = (Button) findViewById(R.id.chooseFile);
	     chooseFile.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View view) {
	            	Intent intent = new Intent(getBaseContext(), FileDialog.class);
	                intent.putExtra(FileDialog.START_PATH, Environment.getExternalStorageDirectory().getPath());
	                
	                //can user select directories or not
	                intent.putExtra(FileDialog.CAN_SELECT_DIR, true);
	                
	                //alternatively you can set file filter
	                intent.putExtra(FileDialog.FORMAT_FILTER, new String[] { "wav" });
	                
	                startActivityForResult(intent, REQUEST_SAVE);
	            }
	        });
	     
	     Button doppler = (Button) findViewById(R.id.startDoppler);
	     doppler.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View view) {
	            	try {
	            	resId = context.getResources().getIdentifier("doppler", "raw", context.getPackageName());
	            	is = context.getResources().openRawResource(resId);}
	            	catch (Exception e){e.printStackTrace();}
	            	if (is != null){
	            		Intent intent = new Intent(MainActivity.this, SimpleGraph.class);
	            		intent.putExtra("CHOSEN", chosen);
	            		intent.putExtra("FILE_PATH", chosenFile);
	            		startActivity(intent);
	            		}
	            	else { Toast.makeText(getApplicationContext(), "inputstream is null!, resID:" + resId, Toast.LENGTH_LONG).show();}
	            }
	        });
	     Button dGraph = (Button) findViewById(R.id.startDynamicGraph);
	     dGraph.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View view) {
	            	Intent intent = new Intent(MainActivity.this, DynamicGraph.class);
	            	intent.putExtra("CHOSEN", chosen);
            		intent.putExtra("FILE_PATH", chosenFile);
	            	startActivity(intent);
	            }
	        });
	     Button fftGraph = (Button) findViewById(R.id.FftGraph);
	     fftGraph.setOnClickListener(new View.OnClickListener() {
	            @Override
	            public void onClick(View view) {
	            	Intent intent = new Intent(MainActivity.this, FftGraphActivity.class);
            		intent.putExtra("FILE_PATH", chosenFile);
	            	startActivity(intent);
	            }
	        });
	     

	    }
	 @Override
	 public synchronized void onActivityResult(final int requestCode,
             int resultCode, final Intent data) {

             if (resultCode == Activity.RESULT_OK) {

                     chosenFile = data.getStringExtra(FileDialog.RESULT_PATH);
                     chosen = true;

             } else if (resultCode == Activity.RESULT_CANCELED) {
                     Logger.getLogger("Hello").log(
                                     Level.WARNING, "file not selected");
                     chosen = false;
             }
             
             else { chosen = false;}
             
             Toast.makeText(getApplicationContext(), "File has been selected: "+chosenFile+".", Toast.LENGTH_LONG).show();

     }

	
}
