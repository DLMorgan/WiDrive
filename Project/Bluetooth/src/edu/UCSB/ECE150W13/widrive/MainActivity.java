package edu.UCSB.ECE150W13.widrive;

import android.os.Bundle;
import android.view.Menu;
//import ioio.examples.hello.MainActivity.Looper;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

public class MainActivity extends IOIOActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    class Looper extends BaseIOIOLooper {
    	//Define pin definitions
    
    	@Override
    	protected void setup() throws ConnectionLostException {
    		//Open pins for I/O
    	}
    	
    	@Override
    	public void loop() throws ConnectionLostException {
    	
    	}
    }
    
    
    
}
