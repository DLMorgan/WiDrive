package com.example.widrive;

import java.util.ArrayList;
import java.util.List;

import com.example.widrive.CarActivity.PeerSelectDialogFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class RemoteActivity extends FragmentActivity implements PeerListListener {

	WifiP2pManager rManager;
	Channel rChannel;
	BroadcastReceiver rReceiver;
	ProgressDialog progressDialog = null;
	
	private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
	private boolean isWifiP2pEnabled = false;
	
	IntentFilter rIntentFilter;
	
	DialogFragment EnableWifiFragment = new EnableWifiDirectDialogFragment();
	DialogFragment PeerSelectFragment = new PeerSelectDialogFragment();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_remote);
		
	    rIntentFilter = new IntentFilter();
	    rIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
	    rIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
	    rIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
	    rIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		
	    rManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
	    rChannel = rManager.initialize(this, getMainLooper(), null);
	    rReceiver = new WiDriveRemoteBroadcastReceiver(rManager, rChannel, this);
	    
	}
	
	/* register the broadcast receiver with the intent values to be matched */
	@Override
	protected void onResume() {
	    super.onResume();
	    registerReceiver(rReceiver, rIntentFilter);
	}
	/* unregister the broadcast receiver */
	@Override
	protected void onPause() {
	    super.onPause();
	    unregisterReceiver(rReceiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_remote, menu);
		return true;
	}
	
    private static String getDeviceStatus(int deviceStatus) {
        Log.d(WiDriveActivity.TAG, "Peer status :" + deviceStatus);
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";

        }
    }
	
	/** Called when the user clicks the pair button */
	public void pair(View view) {
        if (!isWifiP2pEnabled) {
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
        
        rManager.discoverPeers(rChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Toast.makeText(RemoteActivity.this, "Discovery Initiated",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(RemoteActivity.this, "Discovery Failed : " + reasonCode,
                        Toast.LENGTH_SHORT).show();
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        });
	}
	
    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
    	
    	Log.d(WiDriveActivity.TAG, "got here");
    	
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
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
        PeerSelectFragment.show(getSupportFragmentManager(), "Wifi Direct"); /* start here */
    }
	
	/** Called when the user clicks the start button */
	public void start(View view) {
		
	}
	
	 /**
     * @param isWifiP2pEnabled the isWifiP2pEnabled to set
     */
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
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
	public class PeerSelectDialogFragment extends DialogFragment {
	    @Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	    	int position;
	        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	        // Get the layout inflater
	        LayoutInflater inflater = getActivity().getLayoutInflater();

	        // Inflate and set the layout for the dialog
	        // Pass null as the parent view because its going in the dialog layout
	        
	        	for (position = 0; position < peers.size(); position++) {
	            WifiP2pDevice device = peers.get(position);
				    if (device != null) {
				        TextView top = (TextView) findViewById(R.id.device_name);
				        TextView bottom = (TextView) findViewById(R.id.device_details);
				        
				        Log.d(WiDriveActivity.TAG, "name is " + device.deviceName);
				        if (top != null) {
				            top.setText(device.deviceName);
				            Log.d(WiDriveActivity.TAG, "name is " + device.deviceName);
				        }
				        
				        Log.d(WiDriveActivity.TAG, "status is " + getDeviceStatus(device.status));
				        if (bottom != null) {
				            bottom.setText(getDeviceStatus(device.status));
				            Log.d(WiDriveActivity.TAG, "status is " + device.status);
				        }
				    }
	        	}
				
		        builder.setView(inflater.inflate(R.layout.row_devices, null));
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
	}
}