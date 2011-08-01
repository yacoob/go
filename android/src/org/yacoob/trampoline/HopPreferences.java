package org.yacoob.trampoline;

import java.util.regex.Matcher;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * Preferences activity.
 */
public final class HopPreferences extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {

    /**
     * Array enumerating all EditTextPreferences in this activity. It's used for summaries updates.
     */
    private static String[] editTextPrefs = {
            "baseUrl", "wifiName"
    };

    /**
     * Array enumerating all ListPreferences in this activity. It's used for summaries updates.
     */
    private static String[] listPrefs = {
        "refreshFrequency",
    };

    /** Preferences object. */
    private SharedPreferences prefs;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
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

        // Check whether service is running, change its state according to user preferences.
        if (key.equals("refreshLists")) {
            final Intent intent = new Intent(this, HopRefreshService.class);
            if (prefs.getBoolean("refreshLists", false)) {
                intent.setAction(HopRefreshService.RESTART_REFRESH_ACTION);
            } else {
                intent.setAction(HopRefreshService.CANCEL_REFRESH_ACTION);
            }
            startService(intent);
        }

        // Check whether frequency of data refreshes need to be adjusted.
        if (key.equals("refreshFrequency")) {
            final Intent intent = new Intent(this, HopRefreshService.class);
            intent.setAction(HopRefreshService.RESTART_REFRESH_ACTION);
            startService(intent);
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
            final Preference p = findPreference(key);
            p.setSummary(prefs.getString(key, null));
        }

        for (final String key : listPrefs) {
            final ListPreference p = (ListPreference) findPreference(key);
            p.setSummary(p.getEntry());
        }
    }
}
