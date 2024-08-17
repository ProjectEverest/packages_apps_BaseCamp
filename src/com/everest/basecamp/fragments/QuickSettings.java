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

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.MediaStore;
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

import com.android.internal.util.everest.ThemeUtils;

import com.everest.basecamp.fragments.quicksettings.QsHeaderImageSettings;
import com.everest.basecamp.preferences.SystemSettingListPreference;
import com.everest.basecamp.utils.ImageUtils;
import com.everest.basecamp.utils.ResourceUtils;

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

    private static final int CUSTOM_IMAGE_REQUEST_CODE = 1001;

    private static final String KEY_INTERFACE_CATEGORY = "quick_settings_interface_category";
    private static final String KEY_QUICK_PULLDOWN = "qs_quick_pulldown";
    private static final String KEY_SHOW_BRIGHTNESS_SLIDER = "qs_show_brightness_slider";
    private static final String KEY_BRIGHTNESS_SLIDER_POSITION = "qs_brightness_slider_position";
    private static final String KEY_SHOW_AUTO_BRIGHTNESS = "qs_show_auto_brightness";
    private static final String KEY_MISCELLANEOUS_CATEGORY = "quick_settings_miscellaneous_category";
    private static final String KEY_BLUETOOTH_AUTO_ON = "qs_bt_auto_on";
    private static final String KEY_QS_COMPACT_PLAYER  = "qs_compact_media_player_mode";
    private static final String KEY_QS_SPLIT_SHADE = "qs_split_shade";
    private static final String KEY_QS_WIDGETS_ENABLED  = "qs_widgets_enabled";
    private static final String KEY_QS_WIDGETS_PHOTO_IMG  = "qs_widgets_photo_showcase_image";

    private static final String QS_SPLIT_SHADE_LAYOUT_CTG = "android.theme.customization.qs_landscape_layout";
    private static final String QS_SPLIT_SHADE_LAYOUT_PKG = "com.android.systemui.qs.landscape.split_shade_layout";
    private static final String QS_SPLIT_SHADE_LAYOUT_TARGET = "com.android.systemui";
    private static final String KEY_QS_PANEL_STYLE  = "qs_panel_style";

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
    private SwitchPreferenceCompat mSplitShade;
    private Preference mQsWidgetsPref;
    private Preference mQsWidgetPhoto;
    private ListPreference mQsPanelStyle;

    private static ThemeUtils mThemeUtils;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.everest_quicksettings);
        PreferenceScreen prefSet = getPreferenceScreen();

        final Context mContext = getActivity().getApplicationContext();

        mThemeUtils = new ThemeUtils(mContext);

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
        mQsWidgetsPref = findPreference(KEY_QS_WIDGETS_ENABLED);
        mQsWidgetsPref.setOnPreferenceChangeListener(this);
        mQsWidgetPhoto = findPreference(KEY_QS_WIDGETS_PHOTO_IMG);
        mQsWidgetPhoto.setOnPreferenceChangeListener(this);
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

        mSplitShade = findPreference(KEY_QS_SPLIT_SHADE);
        boolean ssEnabled = isSplitShadeEnabled();
        mSplitShade.setChecked(ssEnabled);
        mSplitShade.setOnPreferenceChangeListener(this);

        mQsPanelStyle = (ListPreference) findPreference(KEY_QS_PANEL_STYLE);
        mQsPanelStyle.setOnPreferenceChangeListener(this);

        checkQSOverlays(mContext);
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
        } else if (preference == mQsWidgetsPref) {
            LineageSettings.Secure.putIntForUser(resolver,
                    LineageSettings.Secure.QS_SHOW_BRIGHTNESS_SLIDER, 0, UserHandle.USER_CURRENT);
            SystemRestartUtils.showSystemUIRestartDialog(getActivity());
            return true;
        } else if (preference == mSplitShade) {
            updateSplitShadeState(((Boolean) newValue).booleanValue());
            return true;
        } else if (preference == mQsPanelStyle) {
            int value = Integer.parseInt((String) newValue);
            Settings.System.putIntForUser(resolver,
                    Settings.System.QS_PANEL_STYLE, value, UserHandle.USER_CURRENT);
            updateQsPanelStyle(getActivity());
            checkQSOverlays(getActivity());
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

    private boolean isSplitShadeEnabled() {
        return mThemeUtils.isOverlayEnabled(QS_SPLIT_SHADE_LAYOUT_PKG);
    }

    private void updateSplitShadeState(boolean enable) {

        mThemeUtils.setOverlayEnabled(
                QS_SPLIT_SHADE_LAYOUT_CTG,
                enable ? QS_SPLIT_SHADE_LAYOUT_PKG : QS_SPLIT_SHADE_LAYOUT_TARGET,
                QS_SPLIT_SHADE_LAYOUT_TARGET);
    }

    public static void reset(Context mContext) {
        ContentResolver resolver = mContext.getContentResolver();
        ResourceUtils.updateOverlay(mContext, QS_SPLIT_SHADE_LAYOUT_CTG, QS_SPLIT_SHADE_LAYOUT_TARGET,
                QS_SPLIT_SHADE_LAYOUT_TARGET);
        Settings.System.putIntForUser(resolver,
                Settings.System.QS_PANEL_STYLE, 0, UserHandle.USER_CURRENT);
        updateQsPanelStyle(mContext);
    }

    private static void updateQsPanelStyle(Context context) {
        ContentResolver resolver = context.getContentResolver();

        int qsPanelStyle = Settings.System.getIntForUser(resolver,
                Settings.System.QS_PANEL_STYLE, 0, UserHandle.USER_CURRENT);

        String qsPanelStyleCategory = "android.theme.customization.qs_panel";
        String overlayThemeTarget  = "com.android.systemui";
        String overlayThemePackage  = "com.android.systemui";

        switch (qsPanelStyle) {
            case 1:
              overlayThemePackage = "com.android.system.qs.outline";
              break;
            case 2:
            case 3:
              overlayThemePackage = "com.android.system.qs.twotoneaccent";
              break;
            case 4:
              overlayThemePackage = "com.android.system.qs.shaded";
              break;
            case 5:
              overlayThemePackage = "com.android.system.qs.cyberpunk";
              break;
            case 6:
              overlayThemePackage = "com.android.system.qs.neumorph";
              break;
            case 7:
              overlayThemePackage = "com.android.system.qs.reflected";
              break;
            case 8:
              overlayThemePackage = "com.android.system.qs.surround";
              break;
            case 9:
              overlayThemePackage = "com.android.system.qs.thin";
              break;
            default:
              break;
        }

        if (mThemeUtils == null) {
            mThemeUtils = new ThemeUtils(context);
        }

        // reset all overlays before applying
        mThemeUtils.setOverlayEnabled(qsPanelStyleCategory, overlayThemeTarget, overlayThemeTarget);

        if (qsPanelStyle > 0) {
            mThemeUtils.setOverlayEnabled(qsPanelStyleCategory, overlayThemePackage, overlayThemeTarget);
        }
    }

    private void checkQSOverlays(Context context) {
        ContentResolver resolver = context.getContentResolver();
        int qsPanelStyle = Settings.System.getIntForUser(resolver,
                Settings.System.QS_PANEL_STYLE , 0, UserHandle.USER_CURRENT);

        int index = mQsPanelStyle.findIndexOfValue(Integer.toString(qsPanelStyle));
        mQsPanelStyle.setValue(Integer.toString(qsPanelStyle));
        mQsPanelStyle.setSummary(mQsPanelStyle.getEntries()[index]);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == mQsWidgetPhoto) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, CUSTOM_IMAGE_REQUEST_CODE);
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode, resultCode, result);
        if (requestCode == CUSTOM_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK && result != null) {
            Uri imgUri = result.getData();
            if (imgUri != null) {
                String savedImagePath = ImageUtils.saveImageToInternalStorage(getContext(), imgUri, "qs_widgets", "QS_WIDGETS_PHOTO_SHOWCASE");
                if (savedImagePath != null) {
                    ContentResolver resolver = getContext().getContentResolver();
                    Settings.System.putStringForUser(resolver, "qs_widgets_photo_showcase_path", savedImagePath, UserHandle.USER_CURRENT);
                    mQsWidgetPhoto.setSummary(savedImagePath);
                }
            }
        }
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
