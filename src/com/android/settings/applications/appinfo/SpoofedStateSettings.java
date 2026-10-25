/* Copyright (C) 2026 petalOS; SPDX-License-Identifier: Apache-2.0 */
package com.android.settings.applications.appinfo;

import android.app.AppOpsManager;
import android.app.PetalStateOverrides;
import android.app.settings.SettingsEnums;
import android.app.usage.UsageStatsManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.preference.ListPreference;
import androidx.preference.SwitchPreferenceCompat;

import com.android.internal.app.PetalSpoofConfig;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.fuelgauge.AdvancedPowerUsageDetail;

// just the app's story; the real switches live elsewhere
public final class SpoofedStateSettings extends SettingsPreferenceFragment {
    private static final String[] KEYS = {
            "", "bluetooth", "location", "battery", "background", "standby",
            "location_permission"};
    private static final int[] DESCRIPTIONS = {
            0, R.string.petal_spoof_bluetooth_description,
            R.string.petal_spoof_location_description, R.string.petal_spoof_battery_description,
            R.string.petal_spoof_background_description, R.string.petal_spoof_standby_description,
            R.string.petal_spoof_location_permission_description};
    private String mPackageName;
    private int mUid;
    private Context mTargetContext;
    private final ContentObserver mObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
        @Override
        public void onChange(boolean selfChange) { refresh(); }
    };

    @Override
    public int getMetricsCategory() { return SettingsEnums.APPLICATIONS_INSTALLED_APP_DETAILS; }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!android.app.Flags.petalReportedStateOverrides()) { finish(); return; }
        addPreferencesFromResource(R.xml.petal_spoofed_state);
        getActivity().setTitle(R.string.petal_spoof_title);
        final Bundle args = getArguments();
        if (args == null) { finish(); return; }
        mPackageName = args.getString(AppInfoDashboardFragment.ARG_PACKAGE_NAME);
        mUid = args.getInt(AppInfoDashboardFragment.ARG_PACKAGE_UID, -1);
        if (mPackageName == null || mUid < 0) { finish(); return; }
        mTargetContext = getContext().createContextAsUser(
                UserHandle.of(UserHandle.getUserId(mUid)), 0);
        if (!validateTarget()) { finish(); return; }
        findPreference("explanation").setTitle(mPackageName);

        for (int state = 1; state < KEYS.length; state++) {
            final int token = state;
            final ListPreference pref = findPreference(KEYS[state]);
            pref.setOnPreferenceChangeListener((preference, value) -> {
                try {
                    final int mode = Integer.parseInt((String) value);
                    if (token == PetalStateOverrides.LOCATION_PERMISSION
                            && mode != PetalSpoofConfig.OFF) {
                        final int reason = backgroundLocationUnavailableReason();
                        if (reason != 0) {
                            Toast.makeText(getContext(), reason, Toast.LENGTH_LONG).show();
                            return false;
                        }
                    }
                    if (!PetalSpoofConfig.setMode(mTargetContext, mPackageName, token, mode)) {
                        showWriteError();
                        return false;
                    }
                    // let the damn list catch up first
                    new Handler(Looper.getMainLooper()).post(this::refresh);
                    return true;
                } catch (PackageManager.NameNotFoundException | RuntimeException e) {
                    showWriteError();
                    return false;
                }
            });
        }
        final SwitchPreferenceCompat idle = findPreference("idle_exempt");
        idle.setOnPreferenceChangeListener((preference, value) -> {
            try {
                if (!PetalSpoofConfig.setIdleExempt(mTargetContext, mPackageName, (boolean) value)) {
                    showWriteError();
                    return false;
                }
                // server pokes PetalPower when this lands
                return true;
            } catch (PackageManager.NameNotFoundException | RuntimeException e) {
                showWriteError();
                return false;
            }
        });
        findPreference("real_battery").setOnPreferenceClickListener(preference -> {
            // let the battery page own the real stuff
            final Bundle batteryArgs = new Bundle();
            batteryArgs.putString(AdvancedPowerUsageDetail.EXTRA_PACKAGE_NAME, mPackageName);
            batteryArgs.putInt(AdvancedPowerUsageDetail.EXTRA_UID, mUid);
            new SubSettingLauncher(getContext())
                    .setDestination(AdvancedPowerUsageDetail.class.getName())
                    .setArguments(batteryArgs)
                    .setSourceMetricsCategory(getMetricsCategory())
                    .setUserHandle(UserHandle.of(UserHandle.getUserId(mUid)))
                    .launch();
            return true;
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mTargetContext == null) return;
        getContext().getContentResolver().registerContentObserver(
                Settings.Secure.getUriFor(Settings.Secure.PETAL_SPOOFED_PACKAGES), false,
                mObserver, mTargetContext.getUserId());
        getContext().getContentResolver().registerContentObserver(
                Settings.Secure.getUriFor(Settings.Secure.PETAL_SPOOF_POWER_ERRORS), false,
                mObserver, mTargetContext.getUserId());
        refresh();
    }

    @Override
    public void onPause() {
        getContext().getContentResolver().unregisterContentObserver(mObserver);
        super.onPause();
    }

    private boolean validateTarget() {
        try {
            final PackageInfo info = PetalSpoofConfig.eligiblePackage(mTargetContext, mPackageName);
            return info.applicationInfo.uid == mUid;
        } catch (PackageManager.NameNotFoundException | RuntimeException e) {
            return false;
        }
    }

    private void refresh() {
        if (mTargetContext == null || !isAdded()) return;
        if (!validateTarget()) { finish(); return; }
        final PetalSpoofConfig config = PetalSpoofConfig.read(mTargetContext);
        PetalSpoofConfig.Entry entry = config.get(mPackageName);
        if (!PetalSpoofConfig.matches(mTargetContext, mPackageName, entry)) entry = null;
        for (int state = 1; state < KEYS.length; state++) {
            final ListPreference pref = findPreference(KEYS[state]);
            final int mode = entry == null ? PetalSpoofConfig.OFF : entry.mode(state);
            String description = getString(DESCRIPTIONS[state]);
            if (state == PetalStateOverrides.LOCATION_PERMISSION) {
                final int reason = backgroundLocationUnavailableReason();
                // still let them clear an old choice after revoking location
                pref.setEnabled(reason == 0 || mode != PetalSpoofConfig.OFF);
                if (reason != 0) description += "\n" + getString(reason);
            }
            pref.setValue(Integer.toString(mode));
            pref.setSummary(getString(R.string.petal_spoof_actual_and_reported,
                    description, actualState(state), pref.getEntry()));
        }
        final SwitchPreferenceCompat idle = findPreference("idle_exempt");
        idle.setChecked(entry != null && entry.idleExempt);
        final String errors = Settings.Secure.getStringForUser(getContext().getContentResolver(),
                Settings.Secure.PETAL_SPOOF_POWER_ERRORS, mTargetContext.getUserId());
        final boolean failed = errors != null
                && java.util.Arrays.asList(errors.split(",")).contains(mPackageName);
        idle.setSummary(failed ? R.string.petal_spoof_idle_restore_error
                : R.string.petal_spoof_idle_exempt_summary);
    }

    private String actualState(int state) {
        try {
            switch (state) {
                case PetalStateOverrides.BLUETOOTH:
                    final BluetoothAdapter adapter = mTargetContext.getSystemService(
                            BluetoothManager.class).getAdapter();
                    return adapter == null ? getString(R.string.petal_spoof_unavailable)
                            : getString(adapter.isEnabled()
                                    ? R.string.petal_spoof_on : R.string.petal_spoof_off);
                case PetalStateOverrides.LOCATION:
                    return getString(mTargetContext.getSystemService(LocationManager.class)
                            .isLocationEnabledForUser(UserHandle.of(mTargetContext.getUserId()))
                            ? R.string.petal_spoof_on : R.string.petal_spoof_off);
                case PetalStateOverrides.LOCATION_PERMISSION:
                    return getString(mTargetContext.getPackageManager().checkPermission(
                            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION, mPackageName)
                            == PackageManager.PERMISSION_GRANTED
                            ? R.string.petal_spoof_granted : R.string.petal_spoof_denied);
                case PetalStateOverrides.BATTERY_OPTIMIZATION:
                    return getString(mTargetContext.getSystemService(PowerManager.class)
                            .isIgnoringBatteryOptimizations(mPackageName)
                            ? R.string.petal_spoof_exempt : R.string.petal_spoof_optimized);
                case PetalStateOverrides.BACKGROUND:
                    return getString(mTargetContext.getSystemService(AppOpsManager.class)
                            .unsafeCheckOpNoThrow(AppOpsManager.OPSTR_RUN_ANY_IN_BACKGROUND,
                             mUid, mPackageName) == AppOpsManager.MODE_ALLOWED
                            ? R.string.petal_spoof_unrestricted : R.string.petal_spoof_restricted);
                case PetalStateOverrides.STANDBY:
                    return standbyBucketName(mTargetContext.getSystemService(UsageStatsManager.class)
                            .getAppStandbyBucket(mPackageName));
                default:
                    return getString(R.string.petal_spoof_unavailable);
            }
        } catch (RuntimeException e) {
            return getString(R.string.petal_spoof_unavailable);
        }
    }

    private int backgroundLocationUnavailableReason() {
        if (PetalSpoofConfig.canReportBackgroundLocation(mTargetContext, mPackageName)) return 0;
        try {
            final String[] requested = mTargetContext.getPackageManager().getPackageInfo(
                    mPackageName, PackageManager.GET_PERMISSIONS).requestedPermissions;
            if (requested == null || !java.util.Arrays.asList(requested).contains(
                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                return R.string.petal_spoof_location_permission_not_requested;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return R.string.petal_spoof_unavailable;
        }
        return R.string.petal_spoof_location_permission_not_granted;
    }

    private String standbyBucketName(int bucket) {
        switch (bucket) {
            case UsageStatsManager.STANDBY_BUCKET_EXEMPTED:
                return getString(R.string.petal_spoof_standby_exempted);
            case UsageStatsManager.STANDBY_BUCKET_ACTIVE:
                return getString(R.string.petal_spoof_standby_active);
            case UsageStatsManager.STANDBY_BUCKET_WORKING_SET:
                return getString(R.string.petal_spoof_standby_working);
            case UsageStatsManager.STANDBY_BUCKET_FREQUENT:
                return getString(R.string.petal_spoof_standby_frequent);
            case UsageStatsManager.STANDBY_BUCKET_RARE:
                return getString(R.string.petal_spoof_standby_rare);
            case UsageStatsManager.STANDBY_BUCKET_RESTRICTED:
                return getString(R.string.petal_spoof_standby_restricted);
            case UsageStatsManager.STANDBY_BUCKET_NEVER:
                return getString(R.string.petal_spoof_standby_never);
            default:
                return getString(R.string.petal_spoof_unavailable);
        }
    }

    private void showWriteError() {
        Toast.makeText(getContext(), R.string.petal_spoof_write_error, Toast.LENGTH_LONG).show();
    }
}
