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

import static com.android.settings.development.WindowAnimationScalePreferenceController.DEFAULT_VALUE;
import static com.android.settings.development.WindowAnimationScalePreferenceController.WINDOW_ANIMATION_SCALE_SELECTOR;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.os.RemoteException;
import android.view.IWindowManager;

import androidx.preference.PreferenceScreen;

import com.android.settingslib.widget.SliderPreference;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;

@RunWith(RobolectricTestRunner.class)
public class WindowAnimationScalePreferenceControllerTest {

    @Mock
    private SliderPreference mPreference;
    @Mock
    private PreferenceScreen mScreen;
    @Mock
    private IWindowManager mWindowManager;

    private Context mContext;
    private WindowAnimationScalePreferenceController mController;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mContext = RuntimeEnvironment.application;
        mController = new WindowAnimationScalePreferenceController(mContext);
        ReflectionHelpers.setField(mController, "mWindowManager", mWindowManager);
        when(mScreen.findPreference(mController.getPreferenceKey())).thenReturn(mPreference);
        mController.displayPreference(mScreen);
    }

    @Test
    public void onPreferenceChange_noValueSet_shouldSetDefault() throws RemoteException {
        mController.onPreferenceChange(mPreference, null /* new value */);

        verify(mWindowManager).setAnimationScale(WINDOW_ANIMATION_SCALE_SELECTOR, DEFAULT_VALUE);
    }

    @Test
    public void onPreferenceChange_progress100_shouldSetScale5() throws RemoteException {
        mController.onPreferenceChange(mPreference, 100);

        verify(mWindowManager).setAnimationScale(WINDOW_ANIMATION_SCALE_SELECTOR, 5f);
    }

    @Test
    public void updateState_scale5Set_shouldUpdatePreferenceToProgress100() throws RemoteException {
        when(mWindowManager.getAnimationScale(WINDOW_ANIMATION_SCALE_SELECTOR)).thenReturn(5f);

        mController.updateState(mPreference);

        verify(mPreference).setValue(100);
    }

    @Test
    public void updateState_scale1_5Set_shouldUpdatePreferenceToProgress30() throws RemoteException {
        when(mWindowManager.getAnimationScale(WINDOW_ANIMATION_SCALE_SELECTOR)).thenReturn(1.5f);

        mController.updateState(mPreference);

        verify(mPreference).setValue(30);
    }

    @Test
    public void onDeveloperOptionsSwitchDisabled_shouldDisablePreference() throws RemoteException {
        mController.onDeveloperOptionsSwitchDisabled();

        verify(mWindowManager).setAnimationScale(WINDOW_ANIMATION_SCALE_SELECTOR, DEFAULT_VALUE);
        verify(mPreference).setEnabled(false);
    }
}
