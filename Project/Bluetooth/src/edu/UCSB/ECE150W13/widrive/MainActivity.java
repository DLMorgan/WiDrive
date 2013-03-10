package edu.UCSB.ECE150W13.widrive;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.ToggleButton;
//import ioio.examples.hello.MainActivity.Looper;
//import ioio.examples.hello.MainActivity.Looper;
import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

public class MainActivity extends IOIOActivity implements SensorEventListener {
	private ToggleButton OnOffButton;
	private SensorManager sensormanager;
	private	Sensor accelerometer;
	private Sensor magneticsensor;
	float[] accelovalues = new float[3];
	float[] magnetvalues = new float[3];
	float[] rotmatrix = new float[9];
	float[] tiltvalues = new float[3];
	float startX;
	float startY;
	float currentX;
	float currentY;
	float deltaX;
	float deltaY;
	float volts;
	float distance;
	TextView displaydistance;
	
	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        OnOffButton = (ToggleButton) findViewById(R.id.onoff);
        
        displaydistance = (TextView) findViewById(R.id.textView1);
        displaydistance.setText("Distance to object: " + distance + " in.");
        
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled())
        {
          // prompt the user to turn BlueTooth on
          Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
          startActivityForResult(enableBtIntent, 1);
        }
        
        
        
        //setup sensors
        sensormanager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensormanager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticsensor = sensormanager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	sensormanager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    	sensormanager.registerListener(this, magneticsensor, SensorManager.SENSOR_DELAY_GAME);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	sensormanager.unregisterListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			System.arraycopy(event.values, 0, accelovalues, 0, 3);

		}
		else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			System.arraycopy(event.values, 0, magnetvalues, 0, 3);
		}
		
	}
	
	@Override
	public boolean onTouchEvent (MotionEvent event)	{
		int action = event.getActionMasked();
		switch (action) {
		
			case MotionEvent.ACTION_DOWN:
				startX = event.getRawX();
				startY = event.getRawY();
				break;
				
			case MotionEvent.ACTION_MOVE:
				currentX = event.getX();
				currentY = event.getY();
				//Log.d("touchevent", "rawX= " + startX + " currentX= " + currentX);
				//Log.d("touchevent", "rayY= " + startY + " currentY= " + currentY);
				//compute how much your finger has moved and normalize it between 0 and 1
				deltaX = (currentX - startX);
				deltaY = (currentY - startY);
				//Log.d("touchevent", "deltaX= " + deltaX + " deltaY= " + deltaY);
				break;
				
			case MotionEvent.ACTION_UP:
				//make motors stop and center servos
				deltaX = 0.0f;
				//Log.d("touchevent", "stop");
				//Log.d("touchevent", "deltaX= " + deltaX + " deltaY= " + deltaY);
				break;
				
				
		}
		return true;
	}
    
    class Looper extends BaseIOIOLooper {
    	//Define inputs and outputs
    	private DigitalOutput led;
    	
    	public PwmOutput motor;
    	public PwmOutput servo;
    	public AnalogInput input;
    
    	@Override
    	protected void setup() throws ConnectionLostException {
    		//Assign pins
    		led = ioio_.openDigitalOutput(0);
    		motor = ioio_.openPwmOutput(6, 1000);
    		servo = ioio_.openPwmOutput(7, 100);
    		input = ioio_.openAnalogInput(34);
    		
    		Log.d("setup", "setup complete");
    	}
    	
    	@Override
    	public void loop() throws ConnectionLostException {
    		led.write(!OnOffButton.isChecked());
    		
    		try {
				volts = input.getVoltage();
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    		
    		//Log.d("input", "voltage= " + volts);
			
			distance = volts/(.0064f); //distance away in inches
			
			setText(Float.toString(distance));
			
    		if (OnOffButton.isChecked()) {
    			 
    			 //get sensor info
    			SensorManager.getRotationMatrix(rotmatrix, null, accelovalues, magnetvalues);
    			SensorManager.getOrientation(rotmatrix, tiltvalues);
    			//CW azimuth more positive ? 
    			//pointing down makes pitch more positive - point straight up to stop
    			//tilting right makes roll more positive
    			//Log.d("tilt calc", "azimuth " + tiltvalues[0] + " pitch " + tiltvalues[1] + " roll " + tiltvalues[2]);
    			
    			//motor speed
    			//by touch
    			float dutycycle = (-deltaY/200);
    			if (dutycycle > 0.1 && dutycycle < 0.8) motor.setDutyCycle(dutycycle);
    			else motor.setDutyCycle(0);
    			//Log.d("motor", "dutycycle= " + dutycycle);
    			
    			
    			//by accelerometer
    			/*
    			float dutycycle = 1-Math.abs(tiltvalues[1]);
    			if (dutycycle < .75 && dutycycle > .15) {
    			motor.setDutyCycle(dutycycle);
    			Log.d("motor", "duty cycle= " + dutycycle);
    			}
    			else motor.setDutyCycle(0);
    			*/
    			
    			//servo steering
    			//by touch
    			float pw = 1500 + 2f*deltaX;
    			servo.setPulseWidth(pw);
    			//Log.d("servo", "pw= " + pw);
    			
    			
    			//by accelerometer
    			/*
    			float pw = 1500 + 500*tiltvalues[2];
    			Log.d("servo", "pulse width= " + pw);
    			servo.setPulseWidth(pw);
    			*/
    			
    			try {
    				Thread.sleep(200);
    			} catch (InterruptedException e) {
    				e.printStackTrace();
    			}
    			

    		}
    		else {
    			motor.setDutyCycle(0);
    			servo.setPulseWidth(1500);
    		}
    		
    		
    	}
    	
    	}
    	
    	
    void setText(final String dist) {
    	runOnUiThread(new Runnable() {
			@Override
			public void run() {
				displaydistance.setText("Distance to object: " + dist + " in.");
			}
		});
    	
    }
    
    @Override
	protected IOIOLooper createIOIOLooper() {
    		return new Looper();
	}
    
}
