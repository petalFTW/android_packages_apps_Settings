/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.settings.development;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.IWindowManager;

import androidx.annotation.VisibleForTesting;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;
import com.android.settingslib.widget.SliderPreference;

import java.util.Locale;

public class WindowAnimationScalePreferenceController extends
        DeveloperOptionsPreferenceController implements Preference.OnPreferenceChangeListener,
        PreferenceControllerMixin {

    private static final String WINDOW_ANIMATION_SCALE_KEY = "window_animation_scale";

    @VisibleForTesting
    static final int WINDOW_ANIMATION_SCALE_SELECTOR = 0;
    @VisibleForTesting
    static final float DEFAULT_VALUE = 1;
    @VisibleForTesting
    static final int MIN_PROGRESS = 0;
    @VisibleForTesting
    static final int MAX_PROGRESS = 200;

    private static final int PROGRESS_PER_UNIT = 20;

    private final IWindowManager mWindowManager;

    public WindowAnimationScalePreferenceController(Context context) {
        super(context);

        mWindowManager = IWindowManager.Stub.asInterface(
                ServiceManager.getService(Context.WINDOW_SERVICE));
    }

    @Override
    public String getPreferenceKey() {
        return WINDOW_ANIMATION_SCALE_KEY;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);

        final SliderPreference preference = (SliderPreference) mPreference;
        preference.setMin(MIN_PROGRESS);
        preference.setMax(MAX_PROGRESS);
        preference.setSliderIncrement(1);
        preference.setUpdatesContinuously(true);
        preference.setShowSliderValue(true);
        preference.setLabelFormater(value ->
                String.format(Locale.US, "%.2fx", value / (float) PROGRESS_PER_UNIT));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        writeAnimationScaleOption((Integer) newValue);
        return true;
    }

    @Override
    public void updateState(Preference preference) {
        updateAnimationScaleValue();
    }

    @Override
    protected void onDeveloperOptionsSwitchDisabled() {
        super.onDeveloperOptionsSwitchDisabled();
        writeAnimationScaleOption(null);
    }

    private void writeAnimationScaleOption(Integer progress) {
        try {
            float scale = progress != null ? progress / (float) PROGRESS_PER_UNIT : DEFAULT_VALUE;
            mWindowManager.setAnimationScale(WINDOW_ANIMATION_SCALE_SELECTOR, scale);
            updateAnimationScaleValue();
        } catch (RemoteException e) {
            // intentional no-op
        }
    }

    private void updateAnimationScaleValue() {
        try {
            final float scale = mWindowManager.getAnimationScale(WINDOW_ANIMATION_SCALE_SELECTOR);
            final int progress = Math.round(scale * PROGRESS_PER_UNIT);
            ((SliderPreference) mPreference).setValue(progress);
        } catch (RemoteException e) {
            // intentional no-op
        }
    }
}
