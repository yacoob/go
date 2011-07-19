package org.yacoob.trampoline;

import java.util.regex.Matcher;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * Preferences activity.
 */
public final class HopPreferences extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {

    /**
     * Array enumerating all EditTextPreferences in this activity. We use this list for things like
     * summaries updates.
     */
    private static String[] editTextPrefs = {
            "baseUrl", "wifiName"
    };

    /** Preferences object. */
    private SharedPreferences prefs;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        setSummaries();
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences,
            final String key) {

        // Unregister as handler to avoid multiple calls on edits.
        prefs.unregisterOnSharedPreferenceChangeListener(this);

        // Trim spaces from strings.
        for (final String pref : editTextPrefs) {
            final String p = prefs.getString(pref, null);
            if (p != null) {
                final String f = p.trim();
                if (!p.equals(f)) {
                    ((EditTextPreference) findPreference(pref)).setText(f);
                }
            }
        }

        // Sanity check Trampoline URL.
        if (key.equals("baseUrl")) {
            final String baseUrl = prefs.getString("baseUrl", null);
            final Matcher m = Hop.URLPATTERN.matcher(baseUrl);
            if (!m.matches()) {
                final String defaultUrl = getString(R.string.DEFAULT_BASE_URL);
                ((EditTextPreference) findPreference("baseUrl")).setText(defaultUrl);
            }
        }

        // Set summaries on prefs screen, re-register as handler.
        setSummaries();
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Updates the summaries in activity. Too bad Android doesn't offer a convenience method to do
     * that.
     */
    private void setSummaries() {
        for (final String key : editTextPrefs) {
            final EditTextPreference p = (EditTextPreference) findPreference(key);
            p.setSummary(prefs.getString(key, null));
        }

        // TODO: add summary setting for listpreferences
    }
}
