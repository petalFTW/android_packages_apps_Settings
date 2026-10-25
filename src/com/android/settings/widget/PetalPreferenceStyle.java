/* Copyright (C) 2026 petalOS
 * SPDX-License-Identifier: Apache-2.0
 */
package com.android.settings.widget;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceGroupAdapter;
import com.android.settings.R;
import com.android.settingslib.widget.GroupSectionDividerMixin;
import com.android.settingslib.widget.LayoutPreference;

public final class PetalPreferenceStyle {
    private PetalPreferenceStyle() {}

    public static void applyCategoryLayouts(PreferenceGroup group) {
        for (int i = 0; i < group.getPreferenceCount(); i++) {
            Preference item = group.getPreference(i);
            // Don't stomp layouts with actual widgets in them.
            if (item instanceof PreferenceCategory
                    && item.getLayoutResource() == androidx.preference.R.layout.preference_category) {
                item.setLayoutResource(R.layout.petal_settings_category);
            }
            if (item instanceof PreferenceGroup) {
                applyCategoryLayouts((PreferenceGroup) item);
            }
        }
    }

    public static int background(PreferenceGroupAdapter adapter, int position, boolean selected) {
        Preference item = adapter.getItem(position);
        if (!isGroupedRow(item)) return 0;
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
        return isGroupedRow(other) && item.getParent() == other.getParent();
    }

    private static boolean isGroupedRow(Preference item) {
        return item != null && !(item instanceof PreferenceCategory)
                && !(item instanceof LayoutPreference)
                && !(item instanceof GroupSectionDividerMixin);
    }
}
