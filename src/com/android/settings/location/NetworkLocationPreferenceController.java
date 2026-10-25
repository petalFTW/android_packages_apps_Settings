/*
 * Copyright (C) 2026 The petalOS Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.android.settings.location;

import android.content.Context;
import android.content.pm.PackageManager;
import android.ext.settings.NetworkLocationSettings;

import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;

/** User-controlled choice of the OS network location service. */
public class NetworkLocationPreferenceController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {
    private static final String PROVIDER_PACKAGE = "app.grapheneos.networklocation";

    public NetworkLocationPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        try {
            mContext.getPackageManager().getApplicationInfo(PROVIDER_PACKAGE, 0);
            return AVAILABLE;
        } catch (PackageManager.NameNotFoundException e) {
            return UNSUPPORTED_ON_DEVICE;
        }
    }

    @Override
    public void updateState(Preference preference) {
        ListPreference list = (ListPreference) preference;
        int setting = NetworkLocationSettings.NETWORK_LOCATION_SETTING.get(mContext);
        int index = list.findIndexOfValue(Integer.toString(setting));
        if (index < 0) {
            index = 0;
        }
        list.setValueIndex(index);
        list.setSummary(list.getEntries()[index]);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int setting;
        try {
            setting = Integer.parseInt((String) newValue);
        } catch (NumberFormatException e) {
            return false;
        }
        if (!NetworkLocationSettings.NETWORK_LOCATION_SETTING.put(mContext, setting)) {
            return false;
        }
        updateState(preference);
        return true;
    }
}
