package org.yacoob.trampoline;

import java.util.regex.Matcher;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

/**
 * Push activity. It consists of single progressdialog on top of translucent background. It also
 * kills itself once push is done (or has failed).
 */
public final class HopPushActivity extends Activity {

    /** Reference to current Application object. */
    private Hop app;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (Hop) getApplication();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Only handle actual URLs.
        final String sharedUrl = getIntent().getExtras().getString(Intent.EXTRA_TEXT);
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

        // Work out base URL.
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final String pushUrl = prefs.getString("baseUrl", null) + "/push?url=";

        // Actually push the URL.
        new TaskPushUrl(this, pushUrl).execute(sharedUrl);
    }
}
