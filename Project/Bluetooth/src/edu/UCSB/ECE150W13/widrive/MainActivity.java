package edu.UCSB.ECE150W13.widrive;

import android.os.Bundle;
import android.view.Menu;
import android.widget.ToggleButton;
//import ioio.examples.hello.MainActivity.Looper;
//import ioio.examples.hello.MainActivity.Looper;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

public class MainActivity extends IOIOActivity {
	private ToggleButton OnOffButton;
	
	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        OnOffButton = (ToggleButton) findViewById(R.id.onoff);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    class Looper extends BaseIOIOLooper {
    	//Define inputs and outputs
    	private DigitalOutput led;
    
    	@Override
    	protected void setup() throws ConnectionLostException {
    		//Assign pins
    		led = ioio_.openDigitalOutput(0);
    	}
    	
    	@Override
    	public void loop() throws ConnectionLostException {
    		led.write(!OnOffButton.isChecked());
    	}
    	
    	
    }
    
    @Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}
    
}
