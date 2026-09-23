/*
 * Copyright (C) 2026 petalOS
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.accessibility.detail.extradim

import android.content.Context
import android.hardware.display.ColorDisplayManager
import android.provider.Settings
import com.android.settings.R
import com.android.settings.core.TogglePreferenceController

/** petalOS: master switch for the extra dim schedule. */
class ExtraDimSchedulePreferenceController(context: Context, prefKey: String) :
    TogglePreferenceController(context, prefKey) {

    override fun getAvailabilityStatus(): Int {
        return if (ColorDisplayManager.isReduceBrightColorsAvailable(mContext)) {
            AVAILABLE
        } else {
            UNSUPPORTED_ON_DEVICE
        }
    }

    override fun isChecked(): Boolean {
        return Settings.Secure.getInt(
            mContext.contentResolver,
            SETTING_KEY,
            0,
        ) == 1
    }

    override fun setChecked(isChecked: Boolean): Boolean {
        return Settings.Secure.putInt(
            mContext.contentResolver,
            SETTING_KEY,
            if (isChecked) 1 else 0,
        )
    }

    override fun getSliceHighlightMenuRes(): Int = R.string.menu_key_accessibility

    override fun isSliceable(): Boolean = false

    companion object {
        // matches ColorDisplayService.RBC_AUTO_MODE, don't rename either side
        private const val SETTING_KEY = "petal_reduce_bright_colors_auto_mode"
    }
}
