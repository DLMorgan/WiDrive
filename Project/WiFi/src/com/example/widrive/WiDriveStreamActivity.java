package com.example.widrive;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class WiDriveStreamActivity extends Activity {

	private WiDriveStreamView mjpegview = null;
	private Context context_ = null;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);    
        win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.wistreamview);
        mjpegview = (WiDriveStreamView) findViewById(R.id.mjpegview);      
        context_ = this;
        //new ReadStream().execute();
	    Intent intent = new Intent(this, WiDriveIOIO.class);
	    startActivity(intent);
    }

    public void onPause() {
        super.onPause();
        if(mjpegview!=null){
        	//mjpegview.stopPlayback();
        }
    }
    
    /**
     * A simple server socket that accepts connection and writes some data on
     * the stream.
     */
    public class ReadStream extends AsyncTask<String, Void, WiDriveInputStream> {

        protected WiDriveInputStream doInBackground(String... url) {
        	Log.d(WiDriveActivity.TAG,"ReadStream doInBackground");
            try {
                ServerSocket serverSocket = new ServerSocket(8988);
                Log.d(WiDriveActivity.TAG, "Server: Socket opened");
                Socket client = serverSocket.accept();                             //wait for connection from client
                serverSocket.close();
                Log.d(WiDriveActivity.TAG, "Server: connection done");
                InputStream inputstream = client.getInputStream();
                return new WiDriveInputStream(inputstream);  
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        protected void onPostExecute(WiDriveInputStream result) {
        	if (result == null){
        		Log.d(WiDriveActivity.TAG,"result is null");
        	}
        	if (mjpegview == null){
        		Log.d(WiDriveActivity.TAG,"mjpegview is null");
        	}
        	Log.d(WiDriveActivity.TAG,"starting playback!");
			mjpegview = (WiDriveStreamView) findViewById(R.id.mjpegview); 
        	mjpegview.startPlayback(result);
    	    //Intent intent = new Intent(context_, WiDriveIOIO.class);
    	    //startActivity(intent);
        }
    }
}