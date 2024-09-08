/*
 * Copyright (C) 2023-2024 the risingOS Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.everest.basecamp.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.util.Log;
import android.provider.Settings;
import android.os.UserHandle;
import android.content.res.Resources;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import java.util.List;

import com.everest.basecamp.preferences.SystemSettingListPreference;
import lineageos.providers.LineageSettings;

import static org.lineageos.internal.util.DeviceKeysConstants.*;

@SearchIndexable
public class Gestures extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String KEY_THREE_FINGERS_SWIPE_ACTION = "three_fingers_swipe";
    private static final String KEY_THREE_FINGERS_LONG_PRESS_ACTION = "three_finger_long_press_action";
    private static final String KEY_SHAKE_GESTURE_ACTION = "shake_gestures_action";

    private SystemSettingListPreference mThreeFingersSwipeAction;
    private SystemSettingListPreference mThreeFingersLongPressAction;
    private SystemSettingListPreference mShakeGestureAction;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.everest_gestures);

        final PreferenceScreen prefScreen = getPreferenceScreen();
        final Resources res = getResources();

        mThreeFingersSwipeAction = initList(KEY_THREE_FINGERS_SWIPE_ACTION,
                LineageSettings.System.getIntForUser(getContentResolver(),
                        LineageSettings.System.KEY_THREE_FINGERS_SWIPE_ACTION, 0, UserHandle.USER_CURRENT));

        mThreeFingersLongPressAction = initList(KEY_THREE_FINGERS_LONG_PRESS_ACTION,
                LineageSettings.System.getIntForUser(getContentResolver(),
                        LineageSettings.System.KEY_THREE_FINGERS_LONG_PRESS_ACTION, 0, UserHandle.USER_CURRENT));

        mShakeGestureAction = initList(KEY_SHAKE_GESTURE_ACTION,
                LineageSettings.System.getIntForUser(getContentResolver(),
                        LineageSettings.System.KEY_SHAKE_GESTURE_ACTION, 0, UserHandle.USER_CURRENT));
    }

    private SystemSettingListPreference initList(String key, Action value) {
        return initList(key, value.ordinal());
    }

    private SystemSettingListPreference initList(String key, int value) {
        SystemSettingListPreference list = (SystemSettingListPreference) getPreferenceScreen().findPreference(key);
        if (list == null) return null;
        list.setValue(Integer.toString(value));
        list.setSummary(list.getEntry());
        list.setOnPreferenceChangeListener(this);
        return list;
    }

    private void handleListChange(SystemSettingListPreference pref, Object newValue, String setting) {
        String value = (String) newValue;
        int index = pref.findIndexOfValue(value);
        pref.setSummary(pref.getEntries()[index]);
        LineageSettings.System.putIntForUser(getContentResolver(), setting, Integer.valueOf(value), UserHandle.USER_CURRENT);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mThreeFingersSwipeAction) {
            handleListChange((SystemSettingListPreference) preference, newValue,
                    LineageSettings.System.KEY_THREE_FINGERS_SWIPE_ACTION);
            return true;
        } else if (preference == mThreeFingersLongPressAction) {
            handleListChange((SystemSettingListPreference) preference, newValue,
                    LineageSettings.System.KEY_THREE_FINGERS_LONG_PRESS_ACTION);
            return true;
        } else if (preference == mShakeGestureAction) {
            handleListChange((SystemSettingListPreference) preference, newValue,
                    LineageSettings.System.KEY_SHAKE_GESTURE_ACTION);
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.VIEW_UNKNOWN;
    }

    /**
     * For search
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.everest_gestures) {

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
            };
}
