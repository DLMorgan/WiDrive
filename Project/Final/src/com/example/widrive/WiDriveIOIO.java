package com.example.widrive;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.http.client.ClientProtocolException;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.ToggleButton;

public class WiDriveIOIO extends IOIOActivity implements SensorEventListener {
	//private boolean inLOOP = false;
	private  AsyncTask<String, Void, WiDriveInputStream> Stream_;
	private Socket client;
	
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

	private WiDriveStreamView mjpegview = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);    
        win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        setContentView(R.layout.ioio_activity);
        
        //inLOOP = false;
        if (Stream_ == null){
        	Stream_ = new ReadStream();
        }
        mjpegview = (WiDriveStreamView) findViewById(R.id.mjpegview);
        
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
    	if (Stream_ != null) {
    		Stream_.cancel(true);
    	}
		if (client != null){
			try {
				client.shutdownInput();
				client.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
        if(mjpegview!=null){
        	mjpegview.stopPlayback();
        }
    	sensormanager.unregisterListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_wi_drive, menu);
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
    		if(Stream_.getStatus() != AsyncTask.Status.RUNNING && Stream_.getStatus() != AsyncTask.Status.FINISHED ){
    			Log.d(WiDriveActivity.TAG,"entered ReadStream loop");
    			Stream_.execute();
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
    
    /**
     * A simple server socket that accepts connection and writes some data on
     * the stream.
     */
    public class ReadStream extends AsyncTask<String, Void, WiDriveInputStream> {
    	
        protected WiDriveInputStream doInBackground(String... url) {
        	Log.d(WiDriveActivity.TAG,"ReadStream doInBackground");
        	//inLOOP = true;
            try {
            	ServerSocket serverSocket = new ServerSocket(8988);
                Log.d(WiDriveActivity.TAG, "Server: Socket opened");
                client = serverSocket.accept();                             //wait for connection from client
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
