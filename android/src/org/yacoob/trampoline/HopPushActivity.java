package org.yacoob.trampoline;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.regex.Matcher;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

/**
 * Push activity. It consists of single progressdialog on top of translucent
 * background. It also kills itself once push is done (or has failed).
 */
public final class HopPushActivity extends Activity {

    /** How long should a finished push announcement remain on the screen? */
    private final int ackDelay = 2000;

    /** Reference to current Application object. */
    private Hop app;

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (Hop) getApplication();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Only handle actual URLs.
        final String sharedUrl = getIntent().getExtras().getString(
                Intent.EXTRA_TEXT);
        final Matcher m = Hop.URLPATTERN.matcher(sharedUrl);
        if (!m.matches()) {
            app.showComplaint(getString(R.string.push_msg_not_url));
            finish();
            return;
        }

        // Check if we're offline.
        if (!app.onHomeNetwork()) {
            app.showComplaint(getString(R.string.push_msg_not_on_home_network));
            finish();
            return;
        }

        // Set up dialog window.
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage(getString(R.string.push_msg_active));
        dialog.show();

        // Work out URL at which we can push.
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        final String pushUrl = prefs.getString("baseUrl", null) + "/push?url="
                + URLEncoder.encode(sharedUrl);

        // Actually push the URL.
        String result = getString(R.string.push_msg_done);
        try {
            UrlFetch.urlToString(pushUrl);
        } catch (final IOException e) {
            result = getString(R.string.push_msg_failed);
        }
        dialog.setMessage(result);

        // Hide the dialog and finish activity after some delay.
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dialog.cancel();
                finish();
            }
        }, ackDelay);
    }
}
