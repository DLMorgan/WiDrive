package com.example.widrive;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.util.Log;

public class ActivityHelper {
    public static void initialize(Activity activity) {

        int loadedOrientation = activity.getResources().getConfiguration().orientation;
        int requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        if (loadedOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else if (loadedOrientation == Configuration.ORIENTATION_PORTRAIT) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
        activity.setRequestedOrientation(requestedOrientation);
    }
    
    public static void uninitialize(Activity activity) {

    	activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }
    
	public static boolean getCurrentPeers() {
		if (WiDriveListener.peers == null || WiDriveListener.peerList == null){
			return false;
		}
		WiDriveListener.peers.clear();
		WiDriveListener.peers.addAll(WiDriveListener.peerList.getDeviceList());
        return true;
	}
	
	public static void cancelDisconnect(WifiP2pManager wdManager, Channel wdChannel) {

		/*
		 * A cancel abort request by user. Disconnect i.e. removeGroup if
		 * already connected. Else, request WifiP2pManager to abort the ongoing
		 * request
		 */
		if (wdManager != null) {

			wdManager.cancelConnect(wdChannel, new ActionListener() {

				public void onSuccess() {
					Log.d(WiDriveActivity.TAG,"Aborting connection");
				}

				public void onFailure(int reasonCode) {
					Log.d(WiDriveActivity.TAG,"Connect abort request failed. Reason Code: "
							+ reasonCode);
				}
			});
		}
	}
	
	 /**
     * @param isWifiP2pEnabled the isWifiP2pEnabled to set
     */
    public static void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        WiDriveListener.isWifiP2pEnabled = isWifiP2pEnabled;
    }
	
	/*
	public void toggleWiFi() {
		WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		if (wifi.isWifiEnabled()){
		  wifi.setWifiEnabled(false);
		  wifi.setWifiEnabled(true);
		}
	}
	*/
}