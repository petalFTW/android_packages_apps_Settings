/*
 * Copyright (C) 2026 petalOS
 * SPDX-License-Identifier: Apache-2.0
 */
package com.android.settings.applications.appinfo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.TwoStatePreference;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Per-app "Network access" switch. The state is stored in Settings.System and
 * PetalPower pushes it to netd's firewall (chain OEM_DENY_1).
 */
public final class AppNetworkAccessPreferenceController extends AppInfoPreferenceControllerBase
        implements Preference.OnPreferenceChangeListener {
    private static final String SETTING_DENIED = "petal_network_denied_packages";
    private static final String APPLY_ACTION = "org.petalos.power.action.APPLY_NETWORK_POLICY";
    private static final String APPLY_PACKAGE = "org.petalos.power";

    private String mPackageName;
    private boolean mBlockable;

    public AppNetworkAccessPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    void setPackage(@NonNull String packageName) {
        mPackageName = packageName;
        try {
            final ApplicationInfo info = mContext.getPackageManager()
                    .getApplicationInfo(packageName, 0);
            mBlockable = (info.flags & ApplicationInfo.FLAG_SYSTEM) == 0
                    && info.uid >= android.os.Process.FIRST_APPLICATION_UID;
        } catch (PackageManager.NameNotFoundException e) {
            mBlockable = false;
        }
    }

    @Override
    public int getAvailabilityStatus() {
        return mBlockable ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        if (preference instanceof TwoStatePreference) {
            ((TwoStatePreference) preference).setChecked(!isDenied());
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        setDenied(!((Boolean) newValue));
        return true;
    }

    private boolean isDenied() {
        return deniedPackages().contains(mPackageName);
    }

    private void setDenied(boolean denied) {
        final Set<String> deniedPackages = deniedPackages();
        final boolean changed = denied
                ? deniedPackages.add(mPackageName)
                : deniedPackages.remove(mPackageName);
        if (!changed) return;
        Settings.System.putString(mContext.getContentResolver(), SETTING_DENIED,
                TextUtils.join(",", deniedPackages));
        final Intent intent = new Intent(APPLY_ACTION);
        intent.setPackage(APPLY_PACKAGE);
        mContext.sendBroadcast(intent);
    }

    private Set<String> deniedPackages() {
        final Set<String> result = new LinkedHashSet<>();
        final String raw = Settings.System.getString(mContext.getContentResolver(), SETTING_DENIED);
        if (TextUtils.isEmpty(raw)) return result;
        for (String entry : raw.split(",")) {
            final String packageName = entry.trim();
            if (!packageName.isEmpty()) result.add(packageName);
        }
        return result;
    }
}
