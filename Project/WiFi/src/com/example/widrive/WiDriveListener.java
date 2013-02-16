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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class WiDriveListener extends FragmentActivity implements PeerListListener {
	
	WifiP2pManager cManager;
	Channel cChannel;
	BroadcastReceiver cReceiver;
	
    //private static ArrayList<WifiP2pDevice> peers;
	private ArrayList<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
	private static WiDriveCustomAdapter adapter;
    
    public static boolean isWifiP2pEnabled = false;
    IntentFilter cIntentFilter;
    
    //private Activity activity;
	ProgressDialog progressDialog = null;
	
	DialogFragment EnableWifiFragment = new EnableWifiDirectDialogFragment();
	DialogFragment PeerSelectFragment = new PeerSelectDialogFragment();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent mIntent = getIntent();
		int intValue = mIntent.getIntExtra("a", 0);
		 
		if (intValue == 1){
			setContentView(R.layout.activity_car);
		} else {
			setContentView(R.layout.activity_remote);
		}
		
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
    	
    	if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    	
    	peers.clear();
    	Log.d(WiDriveActivity.TAG, "got here 0");
        peers.addAll(peerList.getDeviceList());
        //((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
        Log.d(WiDriveActivity.TAG, "got here 1");
        if (peers.size() == 0) {
            Log.d(WiDriveActivity.TAG, "No devices found");
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            return;
        }
        Log.d(WiDriveActivity.TAG, "got here 2");
        PeerSelectFragment.show(getSupportFragmentManager(), "Wifi Direct");
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
	        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	        
	        ListView lv = new ListView(getActivity());
	        adapter = new WiDriveCustomAdapter(getActivity(), R.layout.row_devices, peers);
	        lv.setAdapter(adapter);

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
	 
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
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
	                bottom.setText(getDeviceStatus(device.status));
	            }
	        }
	        return v;
	    }
	 
	}
	
}