package com.listmylife.avita.listmylife;

import android.app.Application;

public class MyFirebaseApp extends Application {

    private static MyFirebaseApp mInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
    }

    public static synchronized MyFirebaseApp getInstance() {
        return mInstance;
    }

    public void setConnectivityListener(ConnectivityReceiver.ConnectivityReceiverListener listener) {
        ConnectivityReceiver.connectivityReceiverListener = listener;
    }
}