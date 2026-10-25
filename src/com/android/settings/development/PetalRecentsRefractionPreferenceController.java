/* Copyright (C) 2026 petalOS; SPDX-License-Identifier: Apache-2.0 */
package com.android.settings.development;

import android.content.Context;
import android.provider.Settings;
import androidx.preference.Preference;
import androidx.preference.TwoStatePreference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public final class PetalRecentsRefractionPreferenceController
        extends DeveloperOptionsPreferenceController
        implements Preference.OnPreferenceChangeListener, PreferenceControllerMixin {
    private static final String KEY = "petal_recents_refraction";

    public PetalRecentsRefractionPreferenceController(Context context) { super(context); }

    @Override public String getPreferenceKey() { return KEY; }

    @Override public boolean onPreferenceChange(Preference preference, Object value) {
        return Settings.Secure.putInt(mContext.getContentResolver(), KEY, (Boolean) value ? 1 : 0);
    }

    @Override public void updateState(Preference preference) {
        ((TwoStatePreference) preference).setChecked(
                Settings.Secure.getInt(mContext.getContentResolver(), KEY, 0) == 1);
    }

    @Override protected void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        Settings.Secure.putInt(mContext.getContentResolver(), KEY, 0);
        updateState(mPreference);
    }
}
