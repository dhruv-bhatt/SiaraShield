package com.cybersiara.app;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectivityDetector {
    public static boolean isConnectingToInternet(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();

            if (info != null)

                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
        }
        return false;
    }
}
