package com.example.widrive;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class WiDriveListener extends FragmentActivity implements PeerListListener, ConnectionInfoListener {
	
	static final int CAR = 1;
	static final int REMOTE = 2;
	
	WifiP2pManager cManager;
	Channel cChannel;
	BroadcastReceiver cReceiver;
	
	private ArrayList<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
	private static WiDriveCustomAdapter adapter;
    
	public static boolean isSearching = false;
    public static boolean isWifiP2pEnabled = false;
    IntentFilter cIntentFilter;
    
	ProgressDialog progressDialog = null;
	
	DialogFragment EnableWifiFragment = new EnableWifiDirectDialogFragment();
	DialogFragment PeerSelectFragment = new PeerSelectDialogFragment();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent mIntent = getIntent();
		int intValue = mIntent.getIntExtra("a", 0);
		
		setContentView(R.layout.activity_view);
		
		setupView(intValue);
		
	    cIntentFilter = new IntentFilter();
	    cIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
	    cIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
	    cIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
	    cIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		
	    cManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
	    cChannel = cManager.initialize(this, getMainLooper(), null);
	    cReceiver = new WiDriveBroadcastReceiver(cManager, cChannel, this);
		
	    
	}
	
	/* register the broadcast receiver with the intent values to be matched */
	@Override
	protected void onResume() {
	    super.onResume();
	    registerReceiver(cReceiver, cIntentFilter);
	}
	/* unregister the broadcast receiver */
	@Override
	protected void onPause() {
	    super.onPause();
	    unregisterReceiver(cReceiver);
	}
	
	/** Called when the user clicks the pair button */
	public void pair(View view) {
        if (!WiDriveListener.isWifiP2pEnabled) {
        	EnableWifiFragment.show(getSupportFragmentManager(), "Wifi Direct");
        	return;
        }
        
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = ProgressDialog.show(this, "Press back to cancel", "finding peers", true, true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                
            }
        });
        
        cManager.discoverPeers(cChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Toast.makeText(WiDriveListener.this, "Discovery Initiated",
                        Toast.LENGTH_SHORT).show();
                isSearching = true;
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(WiDriveListener.this, "Discovery Failed : " + reasonCode,
                        Toast.LENGTH_SHORT).show();
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        });
	}
	
	 /**
     * @param isWifiP2pEnabled the isWifiP2pEnabled to set
     */
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        WiDriveListener.isWifiP2pEnabled = isWifiP2pEnabled;
    }
	
    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
    	
    	//if (progressDialog != null && progressDialog.isShowing()) {
        //    progressDialog.dismiss();
        //}
    	
    	peers.clear();
        peers.addAll(peerList.getDeviceList());
        //((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
        if (peers.size() == 0) {
            Log.d(WiDriveActivity.TAG, "No devices found");
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            return;
        }
        
        
        WifiP2pDevice device = peers.get(0);  //need to fix this if multiple devices available
        
        if (PeerSelectFragment.isAdded()) {
        	PeerSelectFragment.dismiss();
        }
        
        if (device.status == WifiP2pDevice.AVAILABLE && isSearching){
        	PeerSelectFragment.show(getSupportFragmentManager(), "Wifi Direct");
        	isSearching = false;
        }
    }
    
	/** Called when the user clicks the start button */
	public void start(View view) {

	}
    
	public static class EnableWifiDirectDialogFragment extends DialogFragment {
	    @Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	        // Use the Builder class for convenient dialog construction
	        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	        builder.setMessage(R.string.enable_wifi)
	               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                	   startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
	                   }
	               })
	               .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                       // User cancelled the dialog
	                   }
	               });
	        // Create the AlertDialog object and return it
	        return builder.create();
	    }
	}
    
	@SuppressLint("ValidFragment")
	public class PeerSelectDialogFragment extends DialogFragment {
	    @Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	    	Log.d(WiDriveActivity.TAG, "onCreate");
	        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	        
	        ListView lv = new ListView(getActivity());
	        adapter = new WiDriveCustomAdapter(getActivity(), R.layout.row_devices, peers);
	        lv.setAdapter(adapter);

	        lv.setClickable(true);
	        lv.setOnItemClickListener(myClickListener);
	        
	        builder.setView(lv);
	        // Add action buttons
		        builder.setMessage(R.string.select_peers)
	               .setPositiveButton("ok", new DialogInterface.OnClickListener() {
	                   @Override
	                   public void onClick(DialogInterface dialog, int id) {
	                       // sign in the user ...
	                   }
	               })
	               .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                	   PeerSelectDialogFragment.this.getDialog().cancel();
	                   }
	               });      
	        return builder.create();
	    }
	    
	    @Override
	    public void onDismiss(DialogInterface dialog) {
	    	ActivityHelper.uninitialize(getActivity());
	        super.onDismiss(dialog);
	    }

	    @Override
	    public void onStart() {
	    	ActivityHelper.initialize(getActivity());
	        super.onStart();
	    }
	}
	
	public OnItemClickListener myClickListener = new OnItemClickListener() {
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			WiDriveCustomAdapter adapter = (WiDriveCustomAdapter) parent.getAdapter();
			
	        WifiP2pDevice device = (WifiP2pDevice) adapter.getItem(position);
	        WifiP2pConfig config = new WifiP2pConfig();
	        config.deviceAddress = device.deviceAddress;
	        config.wps.setup = WpsInfo.PBC;
	        
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            progressDialog = ProgressDialog.show(view.getContext(), "Press back to cancel",
                    "Connecting to :" + device.deviceAddress, true, true
            );
	        
	        cManager.connect(cChannel, config, new WifiP2pManager.ActionListener() {

	            @Override
	            public void onSuccess() {
	            	Log.d(WiDriveActivity.TAG, "Connection Successful");
	            	
	            }

	            @Override
	            public void onFailure(int reason) {
	            	Log.d(WiDriveActivity.TAG, "Connection Unsuccessful");
	            }
	        });
			
		}
	};
	
	public class WiDriveCustomAdapter extends ArrayAdapter<WifiP2pDevice>{
	    private ArrayList<WifiP2pDevice> peers;
	    private Activity activity;
	 
	    public WiDriveCustomAdapter(Activity a, int textViewResourceId, ArrayList<WifiP2pDevice> entries) {
	        super(a, textViewResourceId, entries);
	        this.peers = entries;
	        this.activity = a;
	    }
	   
	    private String getDeviceStatus(int deviceStatus) {
	        Log.d(WiDriveActivity.TAG, "Peer status :" + deviceStatus);
	        switch (deviceStatus) {
	            case WifiP2pDevice.AVAILABLE:  	// 3
	                return "Available";
	            case WifiP2pDevice.INVITED:    	// 1
	                return "Invited";
	            case WifiP2pDevice.CONNECTED:  	// 0
	                return "Connected";
	            case WifiP2pDevice.FAILED:     	// 2
	                return "Failed";
	            case WifiP2pDevice.UNAVAILABLE:	// 4
	                return "Unavailable";
	            default:
	                return "Unknown";

	        }
	    }
	 
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	    	Log.d(WiDriveActivity.TAG, "getView");
	        View v = convertView;
	        if (v == null) {
	            LayoutInflater vi =
	                (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            v = vi.inflate(R.layout.row_devices, null);
	        }
	 
	        WifiP2pDevice device = peers.get(position);
	        if (device != null) {
	            TextView top = (TextView) v.findViewById(R.id.device_name);
	            TextView bottom = (TextView) v.findViewById(R.id.device_details);
	            if (top != null) {
	                top.setText(device.deviceName);
	            }
	            if (bottom != null) {
	            	Log.d(WiDriveActivity.TAG, "bottom set text");
	                bottom.setText(getDeviceStatus(device.status));
	            }
	        }
	        return v;
	    }
	 
	}

	
	public void setupView(int WiDriveInterface) {
		if (WiDriveInterface == CAR) {
			TextView tx1 = (TextView) findViewById(R.id.title);
			tx1.setText(this.getResources().getString(R.string.car_title));
			TextView tx2 = (TextView) findViewById(R.id.description);
			tx2.setText(this.getResources().getString(R.string.car_description));
		}
		
		if (WiDriveInterface == REMOTE) {
			TextView tx1 = (TextView) findViewById(R.id.title);
			tx1.setText(this.getResources().getString(R.string.remote_title));
			TextView tx2 = (TextView) findViewById(R.id.description);
			tx2.setText(this.getResources().getString(R.string.remote_description));
		}
	}

	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo info) {
        Log.d(WiDriveActivity.TAG, "next level");
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        
        WifiP2pDevice device = peers.get(0);  //need to fix this if multiple devices available
        
		TextView tx1 = (TextView) findViewById(R.id.connection);
		tx1.setTextColor(Color.GREEN);
		tx1.setText(this.getResources().getString(R.string.connection_on) +" " + device.deviceName);
		View tx2 = findViewById(R.id.disconnect);
		tx2.setVisibility(0);
	}
	
    /**
     * Remove all peers and clear all fields. This is called on
     * BroadcastReceiver receiving a state change event.
     */
    public void resetData() {
    	peers.clear();

		TextView tx1 = (TextView) findViewById(R.id.connection);
		tx1.setTextColor(Color.RED);
		tx1.setText(this.getResources().getString(R.string.connection_off));
		View tx2 = findViewById(R.id.disconnect);
		tx2.setVisibility(1);
    }
	
}