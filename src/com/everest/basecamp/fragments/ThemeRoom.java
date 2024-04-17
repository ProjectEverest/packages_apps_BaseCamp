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

import com.everest.basecamp.utils.DeviceUtils;

@SearchIndexable
public class ThemeRoom extends SettingsPreferenceFragment
            implements Preference.OnPreferenceChangeListener {

    private static final String KEY_ICONS_CATEGORY = "themes_icons_category";
    private static final String KEY_SIGNAL_ICON = "android.theme.customization.signal_icon";

    private PreferenceCategory mIconsCategory;
    private Preference mSignalIcon;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.everest_theme);
        PreferenceScreen prefSet = getPreferenceScreen();
        final Context context = getContext();
        final ContentResolver resolver = context.getContentResolver();
        final Resources res = getResources();
        final PreferenceScreen prefScreen = getPreferenceScreen();

        mIconsCategory = (PreferenceCategory) findPreference(KEY_ICONS_CATEGORY);
        mSignalIcon = (Preference) findPreference(KEY_SIGNAL_ICON);

        if (!DeviceUtils.deviceSupportsMobileData(context)) {
            mIconsCategory.removePreference(mSignalIcon);
        }
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
                    sir.xmlResId = R.xml.everest_theme;
                    return Arrays.asList(sir);
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    final List<String> keys = super.getNonIndexableKeys(context);
                    if (!DeviceUtils.deviceSupportsMobileData(context)) {
                        keys.add(KEY_SIGNAL_ICON);
                    }
                    return keys;
                }
            };
}
