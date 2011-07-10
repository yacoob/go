package org.yacoob.trampoline;

import java.io.IOException;
import java.net.URLEncoder;

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
    private final int ackDelay = 3000;

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();
        // FIXME: only handle actual URLs.
        // FIXME: Handle offline situation.
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        final String sharedUrl = getIntent().getExtras().getString(
                Intent.EXTRA_TEXT);
        dialog.setMessage(getString(R.string.push_msg_active));
        dialog.show();

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        // FIXME: verify whether pushUrl gets encoded properly.
        final String pushUrl = prefs.getString("baseUrl", null) + "/push?url="
                + URLEncoder.encode(sharedUrl);

        // FIXME: Use AsyncTask for pushing.
        try {
            UrlFetch.urlToString(pushUrl);
        } catch (final IOException e) {
            dialog.setMessage(getString(R.string.push_msg_failed));
        }
        // FIXME: handle the progress bar / spinner
        dialog.setMessage(getString(R.string.push_msg_done));

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
