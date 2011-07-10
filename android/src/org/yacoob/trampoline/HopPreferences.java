package org.yacoob.trampoline;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public final class HopPreferences extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {
    private static Pattern url = Pattern
            .compile("\\(?\\bhttp://[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]");
    private static String[] editTextPrefs = {
            "baseUrl", "wifiName"
    };
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
    public void onSharedPreferenceChanged(
            final SharedPreferences sharedPreferences, final String key) {
        if (key.equals("baseUrl")) {
            final String baseUrl = prefs.getString("baseUrl", null);
            final Matcher m = url.matcher(baseUrl);
            if (!m.matches()) {
                final String defaultUrl = getString(R.string.DEFAULT_BASE_URL);
                ((EditTextPreference) findPreference("baseUrl"))
                        .setText(defaultUrl);
            }
        }
        setSummaries();
    }

    private void setSummaries() {
        for (final String key : editTextPrefs) {
            final EditTextPreference p = (EditTextPreference) findPreference(key);
            p.setSummary(prefs.getString(key, null));
        }
    }
}
