/*
 * Copyright (C) 2019-2025 The LineageOS Project
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

package com.android.settings.deviceinfo.firmwareversion;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.VisibleForTesting;
import androidx.preference.Preference;

import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtilsInternal;

// LINT.IfChange
public class LineageVersionDetailPreferenceController extends BasePreferenceController {

    private static final String TAG = "lineageVersionDialogCtrl";

    // single tap now, it's a real page not an egg anymore
    private static final String PLATLOGO_PACKAGE_NAME = "org.petalos.hub";
    private static final String PLATLOGO_ACTIVITY_CLASS =
            PLATLOGO_PACKAGE_NAME + ".PetalVersionActivity";

    private final UserManager mUserManager;

    private RestrictedLockUtils.EnforcedAdmin mFunDisallowedAdmin;
    private boolean mFunDisallowedBySystem;

    public LineageVersionDetailPreferenceController(Context context, String key) {
        super(context, key);
        mUserManager = (UserManager) mContext.getSystemService(Context.USER_SERVICE);
        initializeAdminPermissions();
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean useDynamicSliceSummary() {
        return true;
    }

    @Override
    public boolean isSliceable() {
        return true;
    }

    @Override
    public CharSequence getSummary() {
        return mContext.getString(R.string.petal_os_version_value);
    }

    @Override
    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), getPreferenceKey())) {
            return false;
        }
        if (Utils.isMonkeyRunning()) {
            return false;
        }
        if (mUserManager.hasUserRestriction(UserManager.DISALLOW_FUN)) {
            if (mFunDisallowedAdmin != null && !mFunDisallowedBySystem) {
                RestrictedLockUtils.sendShowAdminSupportDetailsIntent(mContext,
                        mFunDisallowedAdmin);
            }
            Log.d(TAG, "Sorry, no fun for you!");
            return true;
        }

        final Intent intent = new Intent(Intent.ACTION_MAIN)
                 .setClassName(PLATLOGO_PACKAGE_NAME, PLATLOGO_ACTIVITY_CLASS);
        try {
            mContext.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Unable to start activity " + intent.toString());
        }
        return true;
    }

    @VisibleForTesting
    void initializeAdminPermissions() {
        mFunDisallowedAdmin = RestrictedLockUtilsInternal.checkIfRestrictionEnforced(
                mContext, UserManager.DISALLOW_FUN, UserHandle.myUserId());
        mFunDisallowedBySystem = RestrictedLockUtilsInternal.hasBaseUserRestriction(
                mContext, UserManager.DISALLOW_FUN, UserHandle.myUserId());
    }
}
// LINT.ThenChange(LineageVersionDetailPreference.kt)
