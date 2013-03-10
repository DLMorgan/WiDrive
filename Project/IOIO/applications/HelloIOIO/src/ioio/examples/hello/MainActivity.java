package ioio.examples.hello;

import java.util.ArrayList;

import ioio.examples.hello.R;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ToggleButton;
import android.widget.SeekBar;

/**
 * This is the main activity of the HelloIOIO example application.
 * 
 * It displays a toggle button on the screen, which enables control of the
 * on-board LED. This example shows a very simple usage of the IOIO, by using
 * the {@link IOIOActivity} class. For a more advanced use case, see the
 * HelloIOIOPower example.
 */
public class MainActivity extends IOIOActivity {
	private ToggleButton button_;
	private SeekBar seekBar_;
	ArrayList<String> stringinarray = new ArrayList<String>();
	public EditText input;
	ArrayAdapter<String> adapter;

	/**
	 * Called when the activity is first created. Here we normally initialize
	 * our GUI.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		button_ = (ToggleButton) findViewById(R.id.button);
		seekBar_ = (SeekBar)findViewById(R.id.seekBar1);
		input = (EditText)findViewById(R.id.textinput1);
		
		 Spinner spinner1 = (Spinner) findViewById(R.id.spinner1);
			// Create an ArrayAdapter using the string array and a default spinner layout
	        adapter = new ArrayAdapter<String>(this,
			        android.R.layout.simple_spinner_item,stringinarray);
			/*ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
			        R.array.returnedArray, android.R.layout.simple_spinner_item);
			        */
			// Specify the layout to use when the list of choices appears
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			// Apply the adapter to the spinner
			spinner1.setAdapter(adapter);
			stringinarray.add("Press the button");
			adapter.notifyDataSetChanged();
	}

	/**
	 * This is the thread on which all the IOIO activity happens. It will be run
	 * every time the application is resumed and aborted when it is paused. The
	 * method setup() will be called right after a connection with the IOIO has
	 * been established (which might happen several times!). Then, loop() will
	 * be called repetitively until the IOIO gets disconnected.
	 */
	class Looper extends BaseIOIOLooper {
		/** The on-board LED. */
		private DigitalOutput led_;
		private PwmOutput pwmout_;

		/**
		 * Called every time a connection with IOIO has been established.
		 * Typically used to open pins.
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * 
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#setup()
		 */
		@Override
		protected void setup() throws ConnectionLostException {
			led_ = ioio_.openDigitalOutput(0, true);
			pwmout_ = ioio_.openPwmOutput(10, 1000);
		}
		public void texttoarray() {
			stringinarray.add("hello");
			adapter.notifyDataSetChanged();
			return;
		}

		/**
		 * Called repetitively while the IOIO is connected.
		 * 
		 * @throws ConnectionLostException
		 *             When IOIO connection is lost.
		 * 
		 * @see ioio.lib.util.AbstractIOIOActivity.IOIOThread#loop()
		 */
		
		@Override
		public void loop() throws ConnectionLostException {
			led_.write(!button_.isChecked());
			texttoarray();
			if (button_.isChecked())
				{
				pwmout_.setPulseWidth(seekBar_.getProgress()*6);
				}
			else
				{
					pwmout_.setDutyCycle(0);
				}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}	
	
	/**
	 * A method to create our IOIO thread.
	 * 
	 * @see ioio.lib.util.AbstractIOIOActivity#createIOIOThread()
	 */
	@Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}
}