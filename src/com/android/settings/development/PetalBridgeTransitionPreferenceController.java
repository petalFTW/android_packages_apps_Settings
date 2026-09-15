/*
 * Copyright (C) 2026 petalOS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.development;

import android.content.Context;
import android.provider.Settings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

// bridge crossfade step, lives here because rab1d said so
public class PetalBridgeTransitionPreferenceController extends
        DeveloperOptionsPreferenceController implements Preference.OnPreferenceChangeListener,
        PreferenceControllerMixin {

    private static final String BRIDGE_FADE_KEY = "petal_bridge_fade_ms";

    private final String[] mListValues;
    private final String[] mListEntries;

    public PetalBridgeTransitionPreferenceController(Context context) {
        super(context);

        mListValues = context.getResources().getStringArray(R.array.bridge_transition_values);
        mListEntries = context.getResources().getStringArray(R.array.bridge_transition_entries);
    }

    @Override
    public String getPreferenceKey() {
        return BRIDGE_FADE_KEY;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        try {
            Settings.System.putInt(mContext.getContentResolver(), BRIDGE_FADE_KEY,
                    Integer.parseInt(newValue.toString()));
        } catch (NumberFormatException e) {
            return false;
        }
        updateState(preference);
        return true;
    }

    @Override
    public void updateState(Preference preference) {
        final int current = Settings.System.getInt(mContext.getContentResolver(),
                BRIDGE_FADE_KEY, 10000);
        int index = mListValues.length - 1; // 10 is the default
        for (int i = 0; i < mListValues.length; i++) {
            if (Integer.parseInt(mListValues[i]) == current) {
                index = i;
                break;
            }
        }
        final ListPreference listPreference = (ListPreference) mPreference;
        listPreference.setValue(mListValues[index]);
        listPreference.setSummary(mListEntries[index]);
    }

    @Override
    protected void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        // dev options off doesn't get to silently change the music, just reset the ui
        updateState(mPreference);
    }
}
