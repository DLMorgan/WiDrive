package com.example.widrive;

import java.util.ArrayList;
import android.annotation.SuppressLint;
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
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class WiDriveListener extends FragmentActivity implements PeerListListener, ConnectionInfoListener {
	
	static final int CAR = 1;
	static final int REMOTE = 2;
	
	//public static final String IP_SERVER = "192.168.49.1";	
	private int WiDriveInterface;
	private WifiP2pInfo info = null;
	public static WifiP2pDeviceList peerList;
	
	WifiP2pManager cManager;
	Channel cChannel;
	BroadcastReceiver cReceiver;
	
	public static ArrayList<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
	private static WiDriveCustomAdapter adapter;
    
	public static boolean retryDiscover;
	public static boolean CONNECTED;
	public static boolean isSearching = false;
    public static boolean isWifiP2pEnabled = false;
    IntentFilter cIntentFilter;
    
	ProgressDialog progressDialog = null;
	
	DialogFragment EnableWifiFragment = new EnableWifiDirectDialogFragment();
	DialogFragment PeerSelectFragment = new PeerSelectDialogFragment();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		CONNECTED = false;
		Intent mIntent = getIntent();
		WiDriveInterface = mIntent.getIntExtra("a", 0);
		setContentView(R.layout.activity_view);
		if (WiDriveInterface == CAR || WiDriveInterface == REMOTE){
			setupView(WiDriveInterface);
		}
		else {
			setupView(1);
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
	
	/** register the broadcast receiver with the intent values to be matched **/
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
	
    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
    	WiDriveListener.peerList = peerList;
    	ActivityHelper.getCurrentPeers();
    	
        if (peers.size() == 0) {
            Log.d(WiDriveActivity.TAG, "onPeersAvalaible - No devices found");
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            //resetData();
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
    
	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo info) {
        Log.d(WiDriveActivity.TAG, "onConnectionInfoAvailable");
        
        //if (this.info == null){
        	this.info = info;
        //}
        
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        if (ActivityHelper.getCurrentPeers()){
        	Log.d(WiDriveActivity.TAG,"I HAVE PEERS!!");
	        	try {
	        		WifiP2pDevice device = peers.get(0);  //need to fix this if multiple devices available
	
					TextView tx1 = (TextView) findViewById(R.id.connection);
					tx1.setTextColor(Color.GREEN);
					tx1.setText(this.getResources().getString(R.string.connection_on) +" " + device.deviceName);
					View tx2 = findViewById(R.id.disconnect);
					tx2.setVisibility(View.VISIBLE);
					CONNECTED = true;
		        } catch (Exception e) {
		            Log.e(WiDriveActivity.TAG, e.getMessage());
		        }
	
	        // hide the connect button
	        //hide pair
        }
        else {	//wifi direct bug with last connection
        	//toggleWiFi();
        }
        
	}
	
	/** Called when the user clicks the pair button **/
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
            	ActivityHelper.cancelDisconnect(cManager, cChannel);
            }
        });
        //retryDiscover = true;
	        cManager.discoverPeers(cChannel, new WifiP2pManager.ActionListener() {
	
	            @Override
	            public void onSuccess() {
	                Toast.makeText(WiDriveListener.this, "Discovery Initiated",
	                        Toast.LENGTH_SHORT).show();
	                isSearching = true;
	                //retryDiscover = false;
	            }
	
	            @Override
	            public void onFailure(int reasonCode) {
	                if (progressDialog != null && progressDialog.isShowing()) {
	                    progressDialog.dismiss();
	                }
	                if (reasonCode == 0){
		                Toast.makeText(WiDriveListener.this, "Device not ready, please try again", Toast.LENGTH_SHORT).show();
	                	
	                }
	                else {
		                Toast.makeText(WiDriveListener.this, "Discovery Failed : " + reasonCode,
		                        Toast.LENGTH_SHORT).show();
	                }
	            }
	        });
	}
	
	/** Called when the user clicks the start button */
	public void start(View view) {
		if (CONNECTED){
			if (WiDriveInterface == CAR){
		        // Transfer a string to group owner
				/*
		        String data = "Hello World";
		        Log.d(WiDriveActivity.TAG, "Intent----------- " + data);
		        Intent serviceIntent = new Intent(this, MJPEGStreamService.class);
		        serviceIntent.setAction(MJPEGStreamService.ACTION_SEND_JPEG);
		        serviceIntent.putExtra(MJPEGStreamService.EXTRAS_STRING, data);
		        serviceIntent.putExtra(MJPEGStreamService.EXTRAS_GROUP_OWNER_ADDRESS,
		                info.groupOwnerAddress.getHostAddress());
		        serviceIntent.putExtra(MJPEGStreamService.EXTRAS_GROUP_OWNER_PORT, 8988);
		        this.startService(serviceIntent);
		        */
				Intent intent = new Intent(this, WiDriveCamActivity.class);
				intent.putExtra(WiDriveCamActivity.EXTRAS_GROUP_OWNER_ADDRESS,
		                info.groupOwnerAddress.getHostAddress());
				startActivity(intent);
			}
		
			if (WiDriveInterface == REMOTE) {
	    	    Intent intent = new Intent(this, WiDriveStreamActivity.class);
	    	    startActivity(intent);
	        }
		}
		else {
            Toast.makeText(WiDriveListener.this, "Connect to a client first",
                    Toast.LENGTH_SHORT).show();
		}
	}
	
	/** Called when the user clicks the disconnect button */
	public void disconnect(View view) {
	    Log.d(WiDriveActivity.TAG,"disconnected");
		cManager.removeGroup(cChannel, new ActionListener() {

			public void onFailure(int reasonCode) {
				Log.d(WiDriveActivity.TAG,"Disconnect failed. Reason :" + reasonCode);

			}

			public void onSuccess() {

			}

		});
	}

	@SuppressLint("ValidFragment")
	public class PeerSelectDialogFragment extends DialogFragment {
	    @Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	    	Log.d(WiDriveActivity.TAG, "Peer Select Dialog onCreate");
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
	
	public void setupView(int myInterface) {
		if (myInterface == CAR) {
			TextView tx1 = (TextView) findViewById(R.id.title);
			tx1.setText(this.getResources().getString(R.string.car_title));
			TextView tx2 = (TextView) findViewById(R.id.description);
			tx2.setText(this.getResources().getString(R.string.car_description));
		}
		
		if (myInterface == REMOTE) {
			TextView tx1 = (TextView) findViewById(R.id.title);
			tx1.setText(this.getResources().getString(R.string.remote_title));
			TextView tx2 = (TextView) findViewById(R.id.description);
			tx2.setText(this.getResources().getString(R.string.remote_description));
			Log.d(WiDriveActivity.TAG,"got here setupView");
		}
		
		if (!CONNECTED){
			View tx2 = findViewById(R.id.disconnect);
			tx2.setVisibility(View.INVISIBLE);
		}
		else {
			View tx2 = findViewById(R.id.disconnect);
			tx2.setVisibility(View.VISIBLE);
		}
	}
	
    /**
     * Remove all peers and clear all fields. This is called on
     * BroadcastReceiver receiving a state change event.
     */
    public void resetData() {
    	WiDriveListener.peers.clear();
    	//toggleWiFi();
    	
		TextView tx1 = (TextView) findViewById(R.id.connection);
		tx1.setTextColor(Color.RED);
		tx1.setText(this.getResources().getString(R.string.connection_off));
		View tx2 = findViewById(R.id.disconnect);
		tx2.setVisibility(View.INVISIBLE);
		
    }
}