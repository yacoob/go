package org.yacoob.trampoline;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

/**
 * Helper class; constants and helper functions. Very static.
 */
public class Hop extends Application {

    /** URL of Trampoline serwer. */
    static final String BASEURL = "http://192.168.1.34:8080/hop";

    /** Filename to cache list data to. */
    static final String CACHE_FILE = "url_cache";

    /** URL of JSON endpoint on Trampoline server. */
    static final String RESTURL = BASEURL + "/r";

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

    private void showToast(String msg, int time) {
        Toast.makeText(getApplicationContext(), msg, time).show();
    }

    void showComplaint(String complaint) {
        showToast(complaint, COMPLAINT_TIME);
    }

    void showInfo(String info) {
        showToast(info, INFO_TIME);
    }
}
