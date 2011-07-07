package org.yacoob.trampoline;

import java.net.MalformedURLException;
import java.net.URL;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class HopPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        // FIXME: set summaries on start.
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        // FIXME: generalize this
        if (key.equals("baseUrl")) {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            final String baseUrl = prefs.getString("baseUrl",   null);
            try {
                // FIXME: Replace this with a regexp match? :(
                new URL(baseUrl);
            } catch (final MalformedURLException e) {
                final String default_url = getString(R.string.DEFAULT_BASE_URL);
                prefs.edit().putString("baseUrl", default_url).apply();
                findPreference("baseUrl").setSummary(default_url);
            }
        }
    }
}
