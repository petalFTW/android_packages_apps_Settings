/*
 * Copyright (C) 2026 petalOS
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.accessibility.detail.extradim

import android.content.Context

/** petalOS: when extra dim fucks off in the morning. */
class ExtraDimEndTimePreferenceController(context: Context, prefKey: String) :
    ExtraDimTimePreferenceController(context, prefKey) {

    override val settingKey = "petal_reduce_bright_colors_custom_end_time"
    override val defaultTime = "07:00"
}
