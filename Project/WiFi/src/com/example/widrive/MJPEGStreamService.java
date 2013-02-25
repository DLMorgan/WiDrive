// Copyright 2011 Google Inc. All Rights Reserved.

package com.example.widrive;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * A service that process each JPEG of the MJPEG stream opening a
 * socket connection with the WiFi Direct Group Owner and sending the data
 */
public class MJPEGStreamService extends IntentService {

    private static final int SOCKET_TIMEOUT = 5000;
    public static final String ACTION_SEND_JPEG = "com.example.android.wifidirect.SEND_JPEG";
    public static final String EXTRAS_STRING = "JPEG";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";

    public MJPEGStreamService(String name) {
        super(name);
    }

    public MJPEGStreamService() {
        super("MJPEGStreamService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        //Context context = getApplicationContext();
        if (intent.getAction().equals(ACTION_SEND_JPEG)) {
            String message = intent.getExtras().getString(EXTRAS_STRING);
            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
            Socket socket = new Socket();
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);

            try {
                Log.d(WiDriveActivity.TAG, "Opening client socket - ");
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

                Log.d(WiDriveActivity.TAG, "Client socket - " + socket.isConnected());
                OutputStream stream = socket.getOutputStream();
           //send string to server
                stream.write(message.getBytes(Charset.forName("UTF-8")));
                Log.d(WiDriveActivity.TAG, "Client: Data written " + message);
            } catch (IOException e) {
                Log.e(WiDriveActivity.TAG, e.getMessage());
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
    }
}
