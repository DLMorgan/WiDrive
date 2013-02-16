package com.example.widrive;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

public class ActivityHelper {
    public static void initialize(Activity activity) {
        //Do all sorts of common task for your activities here including:

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
        //Do all sorts of common task for your activities here including:

    	activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }
}