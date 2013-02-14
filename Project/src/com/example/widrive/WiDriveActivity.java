package com.example.widrive;
	
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
	
public class WiDriveActivity extends Activity {
	
	public static final String TAG = "WiDrive";
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_wi_drive);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_wi_drive, menu);
		return true;
	}
	
	/** Called when the user clicks the car button */
	public void startCarActivity(View view) {
	    Intent intent = new Intent(this, CarActivity.class);
	    startActivity(intent);
	}
	
	/** Called when the user clicks the remote button */
	public void startRemoteActivity(View view) {
	    Intent intent = new Intent(this, RemoteActivity.class);
	    startActivity(intent);
	}
	
}