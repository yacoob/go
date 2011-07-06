package org.yacoob.trampoline;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class HopPreferences extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.layout.preferences);
    }
}
