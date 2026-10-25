/* Copyright (C) 2026 petalOS; SPDX-License-Identifier: Apache-2.0 */
package com.android.settings.applications.appinfo;

import android.content.Context;
import android.content.pm.PackageManager;
import androidx.preference.Preference;
import com.android.internal.app.PetalSpoofConfig;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

/** Navigation only; actual reported-state controls live on their own page. */
public final class SpoofedStatePreferenceController extends AppInfoPreferenceControllerBase {
    private String mPackageName;

    public SpoofedStatePreferenceController(Context context, String key) {
        super(context, key);
    }

    void setPackage(String name) {
        mPackageName = name;
    }

    @Override
    public int getAvailabilityStatus() {
        if (!android.app.Flags.petalReportedStateOverrides() || mPackageName == null
                || mParent == null || mParent.getAppEntry() == null) {
            return UNSUPPORTED_ON_DEVICE;
        }
        try {
            final Context target = mContext.createContextAsUser(
                    android.os.UserHandle.of(android.os.UserHandle.getUserId(
                            mParent.getAppEntry().info.uid)), 0);
            PetalSpoofConfig.eligiblePackage(target, mPackageName);
            return AVAILABLE;
        } catch (PackageManager.NameNotFoundException | RuntimeException e) {
            return UNSUPPORTED_ON_DEVICE;
        }
    }

    @Override
    protected Class<? extends SettingsPreferenceFragment> getDetailFragmentClass() {
        return SpoofedStateSettings.class;
    }

    @Override
    public void updateState(Preference preference) {
        if (preference != null) preference.setSummary(R.string.petal_spoof_summary);
    }
}
