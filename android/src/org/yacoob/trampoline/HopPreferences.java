package org.yacoob.trampoline;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class HopPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    private static Pattern url = Pattern.compile("\\(?\\bhttp://[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]");
    private SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        setSummary("baseUrl");
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        if (key.equals("baseUrl")) {
            final String baseUrl = prefs.getString("baseUrl", null);
            final Matcher m = url.matcher(baseUrl);
            if (!m.matches()) {
                final String default_url = getString(R.string.DEFAULT_BASE_URL);
                ((EditTextPreference) findPreference("baseUrl")).setText(default_url);
            }
            setSummary("baseUrl");
        }
    }

    private void setSummary(String key) {
        final EditTextPreference p = (EditTextPreference) findPreference(key);
        p.setSummary(prefs.getString(key, null));
    }
}
