package com.[YOUR-DOMAIN-NAME].android;

import com.[YOUR-DOMAIN-NAME].android.DownloadManager;
import java.io.InputStream;
import java.security.Signature;

import android.app.Activity;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
//foo: added
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.util.ByteArrayBuffer;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
//foo: added-end
import org.apache.http.util.ByteArrayBuffer;

import android.util.Log;

public class ApplicationUpdate extends Activity implements OnClickListener {
    ProgressDialog dialog;
    int increment;
    int val;
   
    private final static String PATH = "/data/data/com.[YOUR-DOMAIN-NAME].android/";
    @Override
    
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.application_update);
        Button startbtn = (Button) findViewById(R.id.startbtn);
        startbtn.setOnClickListener(this);
        
        Log.i("sys","system property:"+System.getProperties() );
        Log.i("","STORAGE DIRECTORY: "+ Environment.getExternalStorageDirectory() );
        Log.i("CREATE","initialize everything.....");
    }
    
    public void onClick(View view) { 
    	dialog = new ProgressDialog(this);
        dialog.setCancelable(true);
        dialog.setMessage("Downloading Updates...");
        Log.i("ONLCLICK","start downloading...");
        // set the progress to be horizontal
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        // reset the bar to the default value of 0
        dialog.setProgress(0);
 
        DownloadManager.DownloadFromUrl("http://www.webdunnit.com/", "home_page.jpg");
        val = DownloadManager.M_statusProgress;
        
        dialog.setMax(val);
        Log.i("ONLCLICK","update the progress bar..");
        //end test
        //dialog.setMax(maximum);
        // display the progressbar
        dialog.show();
 
        // create a thread for updating the progress bar
        Thread background = new Thread (new Runnable() {
           public void run() {
               try {
                   // enter the code to be run while displaying the progressbar.
                   //
                   //  increment the progress bar:
                   // So keep running until the progress value reaches maximum value
            	  // ImageManager.DownloadFromUrl("http://www.webdunnit.com/", "bhome.jpg");
            	   while(dialog.getProgress() <= DownloadManager.M_statusProgress) {
            	//   while (dialog.getProgress() <= dialog.getMax()) {
                       // wait 500ms between each update
                       Thread.sleep(500);
 
                       // active the update handler
                       progressHandler.sendMessage(progressHandler.obtainMessage());
                   }
               } catch (java.lang.InterruptedException e) {
                   // if something fails do something smart
            	   Log.i("THREAD:","Something wrong has happened....");
               }
           }
        });
 
        // start the background thread
        background.start();
 
    }
 
    // handler for the background updating
    Handler progressHandler = new Handler() {
        public void handleMessage(Message msg) {
          dialog.incrementProgressBy(val);
            
            if(dialog.getProgress() == dialog.getMax())
            {
            	dialog.dismiss();
            //	alertbox("Install Update","This will install updates on your device.");
            }
            /*
            if(dialog.getProgress() >= 100) {
              dialog.dismiss();
            }else {
              dialog.incrementProgressBy(increment);
            }*/
 
        }
    };
	protected void alertbox(String title, String mymessage)
	{
	   try {
		new AlertDialog.Builder(this)
	      .setMessage(mymessage)
	      .setTitle(title)
	      .setCancelable(true)
	      .setNeutralButton("OK",
	         new DialogInterface.OnClickListener() {
	         public void onClick(DialogInterface dialog, int whichButton){
	        	 String fileName = "/data/data/com.[YOUR-DOMAIN-NAME].android/xmas.jpg";
	        	 
	        	 Intent intent = new Intent(Intent.ACTION_VIEW);
	        	 intent.setDataAndType(Uri.fromFile(new File(fileName)), "application/vnd.android.package-archive");
	        	 startActivity(intent);
	         }
	         })
	      .show();
	   }catch(Exception e ){
		   e.printStackTrace();
	   }
	 }
