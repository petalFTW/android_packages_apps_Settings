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

package com.android.settings.deviceinfo.firmwareversion

import android.content.Context
import android.content.Intent
import android.os.UserHandle
import android.os.UserManager
import androidx.preference.Preference
import com.android.settings.R
import com.android.settings.Utils
import com.android.settings.contract.TAG_DEVICE_STATE_PREFERENCE
import com.android.settingslib.RestrictedLockUtils
import com.android.settingslib.RestrictedLockUtilsInternal
import com.android.settingslib.metadata.PreferenceMetadata
import com.android.settingslib.metadata.PreferenceSummaryProvider
import com.android.settingslib.preference.PreferenceBinding

// LINT.IfChange
class LineageVersionDetailPreference :
    PreferenceMetadata, PreferenceSummaryProvider, PreferenceBinding,
    Preference.OnPreferenceClickListener {

    override val key: String
        get() = "lineage_version"

    override val title: Int
        get() = R.string.petal_os_version

    override val indexable
        get() = false

    override fun tags(context: Context) = arrayOf(TAG_DEVICE_STATE_PREFERENCE)

    override fun intent(context: Context): Intent? =
        Intent(Intent.ACTION_MAIN)
            .setClassName(PLATLOGO_PACKAGE_NAME, PLATLOGO_ACTIVITY_CLASS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    override fun bind(preference: Preference, metadata: PreferenceMetadata) {
        super.bind(preference, metadata)
        preference.isCopyingEnabled = true
        preference.onPreferenceClickListener = this
    }

    override fun getSummary(context: Context): CharSequence =
        context.getString(R.string.petal_os_version_value)

    // true eats the tap, false lets the intent fly
    override fun onPreferenceClick(preference: Preference): Boolean {
        if (Utils.isMonkeyRunning()) return true

        val context = preference.context
        val userManager = context.getSystemService(Context.USER_SERVICE) as? UserManager
        if (userManager?.hasUserRestriction(UserManager.DISALLOW_FUN) != true) return false

        // Sorry, no fun for you!
        val myUserId = UserHandle.myUserId()
        val enforcedAdmin =
            RestrictedLockUtilsInternal.checkIfRestrictionEnforced(
                context,
                UserManager.DISALLOW_FUN,
                myUserId,
            ) ?: return true
        val disallowedBySystem =
            RestrictedLockUtilsInternal.hasBaseUserRestriction(
                context,
                UserManager.DISALLOW_FUN,
                myUserId,
            )
        if (!disallowedBySystem) {
            RestrictedLockUtils.sendShowAdminSupportDetailsIntent(context, enforcedAdmin)
        }
        return true
    }

    companion object {
        // single tap now, it's a real page not an egg anymore
        const val PLATLOGO_PACKAGE_NAME: String = "org.petalos.hub"
        const val PLATLOGO_ACTIVITY_CLASS: String = PLATLOGO_PACKAGE_NAME + ".PetalVersionActivity"
    }
}
// LINT.ThenChange(LineageVersionDetailPreferenceController.java)
