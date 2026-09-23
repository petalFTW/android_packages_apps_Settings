/* Copyright (C) 2026 petalOS
 * SPDX-License-Identifier: Apache-2.0
 */
package com.android.settings.homepage;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.UserManager;
import com.android.settings.core.BasePreferenceController;

/** Mirrors Hub's primary-profile-only entry and hides it when the package is absent. */
public class PetalHubPreferenceController extends BasePreferenceController {
    public PetalHubPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        if (!mContext.getSystemService(UserManager.class).isSystemUser()) {
            return DISABLED_FOR_USER;
        }
        Intent intent = new Intent(Intent.ACTION_MAIN).setComponent(new ComponentName(
                "org.petalos.hub", "org.petalos.hub.HubActivity"));
        return intent.resolveActivity(mContext.getPackageManager()) != null
                ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }
}
