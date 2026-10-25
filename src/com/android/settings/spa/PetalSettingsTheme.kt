/* Copyright (C) 2026 petalOS; SPDX-License-Identifier: Apache-2.0 */
package com.android.settings.spa

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.res.colorResource
import com.android.settings.R
import com.android.settingslib.spa.framework.theme.SettingsTheme

@Composable
internal fun PetalSettingsTheme(content: @Composable () -> Unit) {
    SettingsTheme {
        val canvas = colorResource(R.color.petal_settings_canvas)
        val surface = colorResource(R.color.petal_settings_surface)
        val text = colorResource(R.color.petal_settings_text)
        val secondary = colorResource(R.color.petal_settings_secondary)
        val accent = colorResource(R.color.petal_settings_accent)
        val selected = colorResource(R.color.petal_settings_selected)
        val onAccent = colorResource(R.color.petal_settings_on_accent)
        val colors = MaterialTheme.colorScheme.copy(
            primary = accent,
            onPrimary = onAccent,
            primaryContainer = selected,
            onPrimaryContainer = text,
            inversePrimary = selected,
            secondary = accent,
            onSecondary = onAccent,
            secondaryContainer = selected,
            onSecondaryContainer = text,
            tertiary = accent,
            onTertiary = onAccent,
            tertiaryContainer = selected,
            onTertiaryContainer = text,
            background = canvas,
            onBackground = text,
            surface = surface,
            onSurface = text,
            surfaceVariant = selected,
            onSurfaceVariant = secondary,
            surfaceTint = accent,
            surfaceBright = surface,
            surfaceDim = canvas,
            // SPA paints its page with surfaceContainer, because of course it does
            surfaceContainer = canvas,
            surfaceContainerLowest = canvas,
            surfaceContainerLow = surface,
            surfaceContainerHigh = surface,
            surfaceContainerHighest = selected,
            outline = secondary,
            outlineVariant = colorResource(R.color.petal_settings_rim),
        )
        MaterialTheme(colorScheme = colors) {
            CompositionLocalProvider(LocalContentColor provides text, content = content)
        }
    }
}
