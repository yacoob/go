package org.yacoob.trampoline;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Helper class; constants and helper functions. Very static.
 */
public final class Hop extends Application {

    /** URL of Trampoline server. */
    static final String BASEURL = "http://go/hop";

    /** Offline indicator. */
    static final Boolean ISOFFLINE = false;

    /** Tag for Android logging. */
    static final String LOGTAG = "Trampoline";

    /** Time (ms) to show complaint {@link Toast} for. */
    private static final int COMPLAINT_TIME = 3000;

    /** Time (ms) to show info {@link Toast} for. */
    private static final int INFO_TIME = 1500;

    /**
     * Log a debug message.
     * 
     * @param msg
     *            The message to log.
     */
    static void debug(final String msg) {
        Log.d(LOGTAG, msg);
    }

    /**
     * Log a warning.
     * 
     * @param msg
     *            The message to log.
     */
    static void warn(final String msg) {
        Log.w(LOGTAG, msg);
    }

    private void showToast(final String msg, final int time) {
        Toast.makeText(getApplicationContext(), msg, time).show();
    }

    void showComplaint(final String complaint) {
        showToast(complaint, COMPLAINT_TIME);
    }

    void showInfo(final String info) {
        showToast(info, INFO_TIME);
    }

    Boolean onHomeNetwork() {
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        final String homeWifi = prefs.getString("wifiName", null);
        if (homeWifi != null && !homeWifi.isEmpty()) {
            final String currentNetwork = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                    .getConnectionInfo().getSSID();
            return (homeWifi.equals(currentNetwork) ? true : false);
        } else {
            return true;
        }
    }
}
