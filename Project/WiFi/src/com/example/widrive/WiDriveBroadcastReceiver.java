package com.example.widrive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;

public class WiDriveBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager cManager;
    private Channel cChannel;
    private WiDriveListener cActivity;

	
    public WiDriveBroadcastReceiver(WifiP2pManager manager, Channel channel, WiDriveListener activity) {
        super();
        this.cManager = manager;
        this.cChannel = channel;
        this.cActivity = activity;
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi Direct is enabled
            	Log.d(WiDriveActivity.TAG, "WiFi Direct is enabled");
            	cActivity.setIsWifiP2pEnabled(true);
            } else {
                // Wi-Fi Direct is not enabled
            	Log.d(WiDriveActivity.TAG, "WiFi Direct is disabled");
            	cActivity.setIsWifiP2pEnabled(false);
            	cActivity.resetData();
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (cManager != null) {
                cManager.requestPeers(cChannel, (PeerListListener) cActivity);
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            if (cManager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {

                // we are connected with the other device, request connection
                // info to find group owner IP
                cManager.requestConnectionInfo(cChannel, cActivity);
                WiDriveListener.CONNECTED = true;
            } else {
                // It's a disconnect
                cActivity.resetData();
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
        }
    }
}
