package edu.UCSB.ECE150.widrive;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class WiDriveActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wi_drive);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_wi_drive, menu);
        return true;
    }
}
