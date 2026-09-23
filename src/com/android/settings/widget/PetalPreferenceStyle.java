/* Copyright (C) 2026 petalOS
 * SPDX-License-Identifier: Apache-2.0
 */
package com.android.settings.widget;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceGroupAdapter;
import com.android.settings.R;

/** Surfaces follow the visible adapter, including dynamically hidden preferences. */
public final class PetalPreferenceStyle {
    private PetalPreferenceStyle() {}

    public static int background(PreferenceGroupAdapter adapter, int position, boolean selected) {
        Preference item = adapter.getItem(position);
        if (item == null || item instanceof PreferenceCategory) return 0;
        if ("petal_settings_hub".equals(item.getKey()) && !selected) {
            return R.drawable.petal_settings_glass;
        }
        Preference previous = position > 0 ? adapter.getItem(position - 1) : null;
        Preference next = position + 1 < adapter.getItemCount()
                ? adapter.getItem(position + 1) : null;
        boolean first = !sameGroup(item, previous);
        boolean last = !sameGroup(item, next);
        if (first && last) return selected ? R.drawable.petal_settings_group_single_selected
                : R.drawable.petal_settings_group_single;
        if (first) return selected ? R.drawable.petal_settings_group_top_selected
                : R.drawable.petal_settings_group_top;
        if (last) return selected ? R.drawable.petal_settings_group_bottom_selected
                : R.drawable.petal_settings_group_bottom;
        return selected ? R.drawable.petal_settings_group_center_selected
                : R.drawable.petal_settings_group_center;
    }

    private static boolean sameGroup(Preference item, Preference other) {
        return other != null && !(other instanceof PreferenceCategory)
                && item.getParent() == other.getParent();
    }
}
