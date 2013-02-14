package com.example.widrive;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class WiDriveActivity extends Activity {

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

}
