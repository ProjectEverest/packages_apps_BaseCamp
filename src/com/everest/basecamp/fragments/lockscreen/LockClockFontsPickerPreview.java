/*
 * Copyright (C) 2023-2024 The risingOS Android Project
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
package com.everest.basecamp.fragments.lockscreen;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextClock;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.everest.basecamp.fragments.themes.fonts.FontArrayAdapter;
import com.everest.basecamp.fragments.themes.fonts.FontManager;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.List;

public class LockClockFontsPickerPreview extends SettingsPreferenceFragment {

    private static final String TAG = "LockClockFontsPickerPreview";
    private static final String PREF_FIRST_TIME = "first_time_clock_face_access";

    private ViewPager viewPager;
    private ClockPagerAdapter pagerAdapter;
    private Spinner fontSpinner;
    private FontManager fontManager;
    private ExtendedFloatingActionButton applyFab;
    private View highlightGuide;

    private int mCurrentFontPosition = -1;
    private int mClockPosition = 0;

    private static final int[] CLOCK_LAYOUTS = {
            R.layout.keyguard_clock_default,
            R.layout.keyguard_clock_oos,
            R.layout.keyguard_clock_center,
            R.layout.keyguard_clock_cos,
            R.layout.keyguard_clock_custom,
            R.layout.keyguard_clock_miui,
            R.layout.keyguard_clock_ide,
            R.layout.keyguard_clock_hyper,
            R.layout.keyguard_clock_stylish,
            R.layout.keyguard_clock_sidebar,
            R.layout.keyguard_clock_minimal,
            R.layout.keyguard_clock_minimal2
    };

    private static final int[] NON_CHANGEABLE_CLOCK_LAYOUTS = {
            R.layout.keyguard_clock_hyper,
            R.layout.keyguard_clock_stylish,
            R.layout.keyguard_clock_sidebar,
            R.layout.keyguard_clock_minimal,
            R.layout.keyguard_clock_minimal2
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fontManager = new FontManager(getActivity(), true);
        getActivity().setTitle(getActivity().getString(R.string.theme_customization_lock_clock_title));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.lockscreen_font_picker_preview, container, false);

        viewPager = rootView.findViewById(R.id.view_pager);
        pagerAdapter = new ClockPagerAdapter();
        viewPager.setAdapter(pagerAdapter);
        mClockPosition = Settings.System.getIntForUser(getContext().getContentResolver(), "clock_style", 0, UserHandle.USER_CURRENT);
        if (mClockPosition < 0 || mClockPosition >= CLOCK_LAYOUTS.length) {
            mClockPosition = 0;
            Settings.System.putIntForUser(getContext().getContentResolver(), "clock_style", 0, UserHandle.USER_CURRENT);
        }
        viewPager.setCurrentItem(mClockPosition);

        TextView fontMessage = rootView.findViewById(R.id.font_message);
        fontSpinner = rootView.findViewById(R.id.font_spinner);
        List<String> fontPackageNames = fontManager.getAllFontPackages();
        FontArrayAdapter fontAdapter = new FontArrayAdapter(
                getActivity(),
                android.R.layout.simple_spinner_dropdown_item,
                fontPackageNames,
                fontManager
        );
        fontSpinner.setAdapter(fontAdapter);
        if (isStaticClockStyle(mClockPosition)) {
            fontSpinner.setVisibility(View.GONE);
            fontMessage.setVisibility(View.VISIBLE);
        } else {
            fontSpinner.setVisibility(View.VISIBLE);
            fontMessage.setVisibility(View.GONE);
        }

        String currentFontPackage = fontManager.getCurrentFontPackage();
        mCurrentFontPosition = fontPackageNames.indexOf(currentFontPackage);
        if (mCurrentFontPosition != -1) {
            fontSpinner.setSelection(mCurrentFontPosition);
            if (!isStaticClockStyle(mClockPosition)) {
                String fontPackage = fontPackageNames.get(mCurrentFontPosition);
                applyFontToAllPreviews(fontPackage);
            }
        }

        fontSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCurrentFontPosition = position;
                if (!isStaticClockStyle(mClockPosition)) {
                    String fontPackage = fontPackageNames.get(mCurrentFontPosition);
                    applyFontToAllPreviews(fontPackage);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        applyFab = rootView.findViewById(R.id.apply_extended_fab);
        applyFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fontPackage = fontPackageNames.get(mCurrentFontPosition);
                if (!isStaticClockStyle(mClockPosition)) {
                    applyFontToAllPreviews(fontPackage);
                    fontManager.enableFontPackage(mCurrentFontPosition);
                }
                Settings.System.putIntForUser(getContext().getContentResolver(), "clock_style", mClockPosition, UserHandle.USER_CURRENT);
            }
        });

        highlightGuide = rootView.findViewById(R.id.highlight_guide);
        if (isFirstTime()) {
            highlightGuide.setVisibility(View.VISIBLE);
            highlightGuide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    highlightGuide.setVisibility(View.GONE);
                    disableHighlight();
                }
            });
        } else {
            highlightGuide.setVisibility(View.GONE);
        }

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {}
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageSelected(int position) {
                mClockPosition = position;
                if (isStaticClockStyle(mClockPosition)) {
                    fontSpinner.setVisibility(View.GONE);
                    fontMessage.setVisibility(View.VISIBLE);
                } else {
                    fontSpinner.setVisibility(View.VISIBLE);
                    fontMessage.setVisibility(View.GONE);
                    String fontPackage = fontPackageNames.get(mCurrentFontPosition);
                    applyFontToAllPreviews(fontPackage);
                }
            }
        });

        return rootView;
    }

    private boolean isStaticClockStyle(int clockStyle) {
        if (clockStyle < 0 || clockStyle >= CLOCK_LAYOUTS.length) {
            return false;
        }
        for (int layout : NON_CHANGEABLE_CLOCK_LAYOUTS) {
            if (CLOCK_LAYOUTS[clockStyle] == layout) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isFirstTime() {
        return Settings.System.getIntForUser(
            getContext().getContentResolver(), PREF_FIRST_TIME, 1, UserHandle.USER_CURRENT) != 0;
    }

    private void disableHighlight() {
        Settings.System.putIntForUser(getContext().getContentResolver(), PREF_FIRST_TIME, 0, UserHandle.USER_CURRENT);
    }

    private class ClockPagerAdapter extends PagerAdapter {
        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View layout = inflater.inflate(CLOCK_LAYOUTS[position], container, false);
            container.addView(layout);
            if (!isStaticClockStyle(position)) {
                String fontPackage = fontManager.getAllFontPackages().get(mCurrentFontPosition);
                applyFontToPreview(fontPackage, layout, position);
            }
            return layout;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return CLOCK_LAYOUTS.length;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }
    }

    private void applyFontToAllPreviews(String font) {
        Typeface typeface = fontManager.getTypeface(getContext(), font);
        int childCount = viewPager.getChildCount();
        //Log.d(TAG, "Total number of children in viewPager: " + childCount);
        if (typeface != null) {
            for (int i = 0; i < childCount; i++) {
                View currentLayout = viewPager.getChildAt(i);
                int currentPosition = viewPager.getCurrentItem();
                if (currentLayout != null) {
                    if (!isStaticClockStyle(currentPosition)) {
                        updateAllTextViews(currentLayout, typeface);
                        //Log.d(TAG, "Applied font to layout at position: " + currentPosition);
                    } else {
                        //Log.d(TAG, "Skipped applying font to static layout at position: " + currentPosition);
                    }
                }
            }
        } else {
            Log.d(TAG, "Failed to apply font");
        }
    }

    private void applyFontToPreview(String font, View layout, int position) {
        if (isStaticClockStyle(position)) {
            return;
        }
        Typeface typeface = fontManager.getTypeface(getContext(), font);
        if (typeface != null) {
            updateAllTextViews(layout, typeface);
        } else {
            Toast.makeText(getContext(), "Failed to apply font", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateAllTextViews(View view, Typeface typeface) {
        if (view instanceof TextView || view instanceof TextClock) {
            ((TextView) view).setTypeface(typeface);
        } else if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                updateAllTextViews(child, typeface);
            }
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.VIEW_UNKNOWN;
    }
}

