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
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.view.View;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.everest.SystemRestartUtils;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;

import com.everest.basecamp.fragments.quicksettings.QsHeaderImageSettings;
import com.everest.basecamp.preferences.SystemSettingListPreference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.everest.basecamp.utils.DeviceUtils;
import lineageos.preference.LineageSecureSettingListPreference;
import lineageos.preference.LineageSecureSettingSwitchPreference;
import lineageos.preference.LineageSystemSettingListPreference;
import lineageos.providers.LineageSettings;

@SearchIndexable
public class QuickSettings extends SettingsPreferenceFragment
            implements Preference.OnPreferenceChangeListener {

    private static final String KEY_INTERFACE_CATEGORY = "quick_settings_interface_category";
    private static final String KEY_QUICK_PULLDOWN = "qs_quick_pulldown";
    private static final String KEY_SHOW_BRIGHTNESS_SLIDER = "qs_show_brightness_slider";
    private static final String KEY_BRIGHTNESS_SLIDER_POSITION = "qs_brightness_slider_position";
    private static final String KEY_SHOW_AUTO_BRIGHTNESS = "qs_show_auto_brightness";
    private static final String KEY_MISCELLANEOUS_CATEGORY = "quick_settings_miscellaneous_category";
    private static final String KEY_BLUETOOTH_AUTO_ON = "qs_bt_auto_on";
    private static final String KEY_QS_COMPACT_PLAYER  = "qs_compact_media_player_mode";

    private static final int PULLDOWN_DIR_NONE = 0;
    private static final int PULLDOWN_DIR_RIGHT = 1;
    private static final int PULLDOWN_DIR_LEFT = 2;
    private static final int PULLDOWN_DIR_BOTH = 3;

    private PreferenceCategory mInterfaceCategory;
    private LineageSecureSettingListPreference mShowBrightnessSlider;
    private LineageSecureSettingListPreference mBrightnessSliderPosition;
    private LineageSecureSettingSwitchPreference mShowAutoBrightness;
    private PreferenceCategory mMiscellaneousCategory;
    private LineageSystemSettingListPreference mQuickPulldown;
    private Preference mQsCompactPlayer;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.everest_quicksettings);
        PreferenceScreen prefSet = getPreferenceScreen();

        final Context context = getContext();
        final ContentResolver resolver = context.getContentResolver();
        final Resources res = getResources();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mQuickPulldown =
                (LineageSystemSettingListPreference) findPreference(KEY_QUICK_PULLDOWN);
        mQuickPulldown.setOnPreferenceChangeListener(this);
        updateQuickPulldownSummary(mQuickPulldown.getIntValue(0));

        mShowBrightnessSlider = (LineageSecureSettingListPreference) findPreference(KEY_SHOW_BRIGHTNESS_SLIDER);
        mBrightnessSliderPosition = (LineageSecureSettingListPreference) findPreference(KEY_BRIGHTNESS_SLIDER_POSITION);
        mShowAutoBrightness = (LineageSecureSettingSwitchPreference) findPreference(KEY_SHOW_AUTO_BRIGHTNESS);

        mShowBrightnessSlider.setOnPreferenceChangeListener(this);
        boolean showSlider = LineageSettings.Secure.getIntForUser(resolver,
                LineageSettings.Secure.QS_SHOW_BRIGHTNESS_SLIDER, 1, UserHandle.USER_CURRENT) > 0;

        mBrightnessSliderPosition.setEnabled(showSlider);

        boolean autoBrightnessAvailable = res.getBoolean(
                com.android.internal.R.bool.config_automatic_brightness_available);
        if (autoBrightnessAvailable) {
            mShowAutoBrightness.setEnabled(showSlider);
        } else {
            mInterfaceCategory.removePreference(mShowAutoBrightness);
        }

        mMiscellaneousCategory = (PreferenceCategory) findPreference(KEY_MISCELLANEOUS_CATEGORY);

        if (getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            mQuickPulldown.setEntries(R.array.status_bar_quick_pull_down_entries_rtl);
            mQuickPulldown.setEntryValues(R.array.status_bar_quick_pull_down_values_rtl);
        }

        if (!DeviceUtils.deviceSupportsBluetooth(context)) {
            prefScreen.removePreference(mMiscellaneousCategory);
        }

        mQsCompactPlayer = (Preference) findPreference(KEY_QS_COMPACT_PLAYER);
        mQsCompactPlayer.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final Context context = getContext();
        final ContentResolver resolver = context.getContentResolver();
        if (preference == mQuickPulldown) {
            int value = Integer.parseInt((String) newValue);
            updateQuickPulldownSummary(value);
            return true;
        } else if (preference == mShowBrightnessSlider) {
            int value = Integer.parseInt((String) newValue);
            mBrightnessSliderPosition.setEnabled(value > 0);
            if (mShowAutoBrightness != null)
                mShowAutoBrightness.setEnabled(value > 0);
            return true;
        } else if (preference == mQsCompactPlayer) {
            SystemRestartUtils.showSystemUIRestartDialog(getActivity());
            return true;
        }
        return false;
    }

    private void updateQuickPulldownSummary(int value) {
        String summary = "";
        switch (value) {
            case PULLDOWN_DIR_NONE:
                summary = getResources().getString(
                    R.string.status_bar_quick_pull_down_off);
                break;
            case PULLDOWN_DIR_RIGHT:
            case PULLDOWN_DIR_LEFT:
            case PULLDOWN_DIR_BOTH:
                summary = getResources().getString(
                    R.string.status_bar_quick_pull_down_summary,
                    getResources().getString(
                        value == PULLDOWN_DIR_RIGHT
                            ? R.string.status_bar_quick_pull_down_right
                            : value == PULLDOWN_DIR_LEFT
                                ? R.string.status_bar_quick_pull_down_left
                                : R.string.status_bar_quick_pull_down_both
                    )
                );
                break;
        }
        mQuickPulldown.setSummary(summary);
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
                    sir.xmlResId = R.xml.everest_quicksettings;
                    return Arrays.asList(sir);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    final List<String> keys = super.getNonIndexableKeys(context);
                    final Resources res = context.getResources();
                    boolean autoBrightnessAvailable = res.getBoolean(
                            com.android.internal.R.bool.config_automatic_brightness_available);
                    if (!autoBrightnessAvailable) {
                        keys.add(KEY_SHOW_AUTO_BRIGHTNESS);
                    }
                    if (!DeviceUtils.deviceSupportsBluetooth(context)) {
                        keys.add(KEY_BLUETOOTH_AUTO_ON);
                    }
                    return keys;
                }
            };
}
