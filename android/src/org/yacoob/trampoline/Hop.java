package org.yacoob.trampoline;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

// TODO: Auto-generated Javadoc
/**
 * Helper class; constants and helper functions. Very static.
 */
public final class Hop extends Application {

    /** URL of Trampoline server. */
    static final String BASEURL = "http://go/hop";

    /** Tag for Android logging. */
    static final String LOGTAG = "Trampoline";

    /** Time (ms) to show complaint {@link Toast} for. */
    private static final int COMPLAINT_TIME = 3000;

    /** Time (ms) to show info {@link Toast} for. */
    private static final int INFO_TIME = 1500;

    /** Offline indicator. */
    private Boolean appOffline = false;

    /**
     * Logs a debug message.
     * 
     * @param msg
     *            The message to log.
     */
    static void debug(final String msg) {
        Log.d(LOGTAG, msg);
    }

    /**
     * Logs a warning.
     * 
     * @param msg
     *            The message to log.
     */
    static void warn(final String msg) {
        Log.w(LOGTAG, msg);
    }

    /**
     * Shows a toast notification.
     * 
     * @param msg
     *            The message to show.
     * @param time
     *            How long should notification be displayed? [ms]
     */
    private void showToast(final String msg, final int time) {
        Toast.makeText(getApplicationContext(), msg, time).show();
    }

    /**
     * Shows a toast notification with a complaint.
     * 
     * @param complaint
     *            The message to show.
     */
    void showComplaint(final String complaint) {
        showToast(complaint, COMPLAINT_TIME);
    }

    /**
     * Shows a toast notification with an information.
     * 
     * @param info
     *            The message to show.
     */
    void showInfo(final String info) {
        showToast(info, INFO_TIME);
    }

    /**
     * Gets application state (offline/online).
     * 
     * @return True if offline, false otherwise.
     */
    public Boolean isOffline() {
        return appOffline;
    }

    /**
     * Sets whether application is considered to be offline or not.
     * 
     * @param newState
     *            State to set.
     */
    public void setOffline(final Boolean newState) {
        appOffline = newState;
    }

    /**
     * Checks whether we're currently connected to the wifi network Trampoline
     * is running in. If user has set the relevant preference to empty string,
     * we always return true here.
     * 
     * @return True if we're on home network, false otherwise.
     */
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
