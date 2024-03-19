/*
 * Copyright (C) 2023 The EverestOS Project
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

package com.everest.basecamp.fragments;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.everest.OmniJawsClient;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;

import com.everest.basecamp.preferences.SecureSettingSwitchPreference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SearchIndexable
public class LockScreen extends SettingsPreferenceFragment
            implements Preference.OnPreferenceChangeListener {

    private static final String KEY_FINGERPRINT_CATEGORY = "lock_screen_fingerprint_category";
    private static final String KEY_RIPPLE_EFFECT = "enable_ripple_effect";
    private static final String KEY_AUTHENTICATION_SUCCESS = "fp_success_vibrate";
    private static final String KEY_AUTHENTICATION_ERROR = "fp_error_vibrate";
    private static final String KEY_SCREEN_OFF_UDFPS = "screen_off_udfps_enabled";
    private static final String KEY_WEATHER = "lockscreen_weather_enabled";

    private PreferenceCategory mFingerprintCategory;
    private SecureSettingSwitchPreference mScreenOffUdfps;
    private Preference mWeather;
    private OmniJawsClient mWeatherClient;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.everest_lockscreen);
        PreferenceScreen prefSet = getPreferenceScreen();
        final Resources res = getResources();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mFingerprintCategory = (PreferenceCategory) findPreference(KEY_FINGERPRINT_CATEGORY);
        mScreenOffUdfps = (SecureSettingSwitchPreference) findPreference(KEY_SCREEN_OFF_UDFPS);

        FingerprintManager fingerprintManager = (FingerprintManager)
                getActivity().getSystemService(Context.FINGERPRINT_SERVICE);

        mWeather = (Preference) findPreference(KEY_WEATHER);
        mWeatherClient = new OmniJawsClient(getContext());
        updateWeatherSettings();

        if (fingerprintManager == null || !fingerprintManager.isHardwareDetected()) {
            prefScreen.removePreference(mFingerprintCategory);
        } else {
            boolean screenOffUdfpsAvailable = res.getBoolean(
                    com.android.internal.R.bool.config_supportScreenOffUdfps) ||
                    !TextUtils.isEmpty(res.getString(
                            com.android.internal.R.string.config_dozeUdfpsLongPressSensorType));

            if (!screenOffUdfpsAvailable) {
                mFingerprintCategory.removePreference(mScreenOffUdfps);
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final Context context = getContext();
        final ContentResolver resolver = context.getContentResolver();
        return false;
    }

    private void updateWeatherSettings() {
        if (mWeatherClient == null || mWeather == null) return;

        boolean weatherEnabled = mWeatherClient.isOmniJawsEnabled();
        mWeather.setEnabled(weatherEnabled);
        mWeather.setSummary(weatherEnabled ? R.string.lockscreen_weather_summary :
            R.string.lockscreen_weather_enabled_info);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateWeatherSettings();
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.EVEREST;
    }
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {
                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(
                        Context context, boolean enabled) {
                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.everest_lockscreen;
                    return Arrays.asList(sir);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    final List<String> keys = super.getNonIndexableKeys(context);
                    final Resources res = context.getResources();
                    FingerprintManager fingerprintManager = (FingerprintManager)
                        context.getSystemService(Context.FINGERPRINT_SERVICE);

                    if (fingerprintManager == null || !fingerprintManager.isHardwareDetected()) {
                        keys.add(KEY_RIPPLE_EFFECT);
                        keys.add(KEY_AUTHENTICATION_SUCCESS);
                        keys.add(KEY_AUTHENTICATION_ERROR);
                        keys.add(KEY_SCREEN_OFF_UDFPS);
                    } else {
                        boolean screenOffUdfpsAvailable = res.getBoolean(
                            com.android.internal.R.bool.config_supportScreenOffUdfps) ||
                            !TextUtils.isEmpty(res.getString(
                                com.android.internal.R.string.config_dozeUdfpsLongPressSensorType));
                        if (!screenOffUdfpsAvailable) {
                            keys.add(KEY_SCREEN_OFF_UDFPS);
                        }
                    }
                    return keys;
                }
            };
}
