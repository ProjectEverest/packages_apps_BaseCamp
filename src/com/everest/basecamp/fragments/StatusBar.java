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

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.Indexable;
import com.android.settingslib.search.SearchIndexable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.everest.basecamp.preferences.CustomSeekBarPreference;
import com.everest.basecamp.preferences.SystemSettingSwitchPreference;
import com.everest.basecamp.utils.DeviceUtils;

@SearchIndexable
public class StatusBar extends SettingsPreferenceFragment
            implements Preference.OnPreferenceChangeListener {

    private static final String KEY_ICONS_CATEGORY = "status_bar_icons_category";
    private static final String KEY_BLUETOOTH_BATTERY_STATUS = "bluetooth_show_battery";
    private static final String KEY_DATA_DISABLED_ICON = "data_disabled_icon";
    private static final String KEY_FOUR_G_ICON = "show_fourg_icon";
    private static final String KEY_STATUSBAR_TOP_PADDING = "statusbar_top_padding";
    private static final String KEY_STATUSBAR_LEFT_PADDING = "statusbar_left_padding";
    private static final String KEY_STATUSBAR_RIGHT_PADDING = "statusbar_right_padding";
    private static final String DEFAULT = "_default";

    private PreferenceCategory mIconsCategory;
    private SystemSettingSwitchPreference mBluetoothBatteryStatus;
    private SystemSettingSwitchPreference mDataDisabledIcon;
    private SystemSettingSwitchPreference mFourgIcon;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.everest_statusbar);
        PreferenceScreen prefSet = getPreferenceScreen();
        final Context context = getContext();
        final ContentResolver resolver = context.getContentResolver();
        final Resources res = getResources();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mIconsCategory = (PreferenceCategory) findPreference(KEY_ICONS_CATEGORY);
        mBluetoothBatteryStatus = (SystemSettingSwitchPreference) findPreference(KEY_BLUETOOTH_BATTERY_STATUS);
        mDataDisabledIcon = (SystemSettingSwitchPreference) findPreference(KEY_DATA_DISABLED_ICON);
        mFourgIcon = (SystemSettingSwitchPreference) findPreference(KEY_FOUR_G_ICON);

        if (!DeviceUtils.deviceSupportsBluetooth(context)) {
            mIconsCategory.removePreference(mBluetoothBatteryStatus);
        }

        if (!DeviceUtils.deviceSupportsMobileData(context)) {
            mIconsCategory.removePreference(mDataDisabledIcon);
            mIconsCategory.removePreference(mFourgIcon);
        }

        CustomSeekBarPreference leftSeekBar = findPreference(KEY_STATUSBAR_LEFT_PADDING);
        int defaultLeftPadding = getResources().getDimensionPixelSize(com.android.internal.R.dimen.status_bar_padding_start);
        leftSeekBar.setDefaultValue(defaultLeftPadding, true);

        CustomSeekBarPreference rightSeekBar = findPreference(KEY_STATUSBAR_RIGHT_PADDING);
        int defaultRightPadding = getResources().getDimensionPixelSize(com.android.internal.R.dimen.status_bar_padding_end);
        rightSeekBar.setDefaultValue(defaultRightPadding, true);

        CustomSeekBarPreference topSeekbar = findPreference(KEY_STATUSBAR_TOP_PADDING);
        int defaultTopPadding = getResources().getDimensionPixelSize(com.android.internal.R.dimen.status_bar_padding_top);
        topSeekbar.setDefaultValue(defaultTopPadding, true);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
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
                    sir.xmlResId = R.xml.everest_statusbar;
                    return Arrays.asList(sir);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    final List<String> keys = super.getNonIndexableKeys(context);
                    if (!DeviceUtils.deviceSupportsBluetooth(context)) {
                        keys.add(KEY_BLUETOOTH_BATTERY_STATUS);
                    }
                    if (!DeviceUtils.deviceSupportsMobileData(context)) {
                        keys.add(KEY_DATA_DISABLED_ICON);
                        keys.add(KEY_FOUR_G_ICON);
                    }
                    return keys;
                }
            };
}
