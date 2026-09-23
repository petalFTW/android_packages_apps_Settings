/*
 * Copyright (C) 2026 petalOS
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.settings.accessibility.detail.extradim

import android.content.Context

/** petalOS: when extra dim kicks in. */
class ExtraDimStartTimePreferenceController(context: Context, prefKey: String) :
    ExtraDimTimePreferenceController(context, prefKey) {

    override val settingKey = "petal_reduce_bright_colors_custom_start_time"
    override val defaultTime = "22:00"
}
