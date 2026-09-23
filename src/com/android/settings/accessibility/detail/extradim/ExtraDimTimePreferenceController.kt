/*
 * Copyright (C) 2026 petalOS
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.accessibility.detail.extradim

import android.app.TimePickerDialog
import android.content.Context
import android.hardware.display.ColorDisplayManager
import android.provider.Settings
import android.text.format.DateFormat
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import com.android.settings.core.BasePreferenceController
import java.time.LocalTime
import java.util.Calendar
import java.util.Locale

/** petalOS: shared start/end time row for the extra dim schedule. */
abstract class ExtraDimTimePreferenceController(
    context: Context,
    prefKey: String,
) : BasePreferenceController(context, prefKey), Preference.OnPreferenceClickListener {

    protected abstract val settingKey: String
    protected abstract val defaultTime: String

    override fun getAvailabilityStatus(): Int {
        return if (ColorDisplayManager.isReduceBrightColorsAvailable(mContext)) {
            AVAILABLE
        } else {
            UNSUPPORTED_ON_DEVICE
        }
    }

    override fun displayPreference(screen: PreferenceScreen?) {
        super.displayPreference(screen)
        screen?.findPreference<Preference>(preferenceKey)?.setOnPreferenceClickListener(this)
    }

    override fun updateState(preference: Preference?) {
        super.updateState(preference)
        val time = readTime()
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, time.hour)
        calendar.set(Calendar.MINUTE, time.minute)
        preference?.summary = DateFormat.getTimeFormat(mContext).format(calendar.time)
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        val time = readTime()
        TimePickerDialog(
            mContext,
            { _, hour, minute ->
                Settings.Secure.putString(
                    mContext.contentResolver,
                    settingKey,
                    String.format(Locale.US, "%02d:%02d", hour, minute),
                )
            },
            time.hour,
            time.minute,
            DateFormat.is24HourFormat(mContext),
        ).show()
        return true
    }

    private fun readTime(): LocalTime {
        val raw = Settings.Secure.getString(mContext.contentResolver, settingKey)
        return try {
            LocalTime.parse(raw ?: defaultTime)
        } catch (e: Exception) {
            LocalTime.parse(defaultTime)
        }
    }
}
