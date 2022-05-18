/*
 * Copyright (C) 2014-2016 The Dirty Unicorns Project
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

package com.everest.basecamp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.preference.Preference;
import androidx.viewpager.widget.ViewPager;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.everest.basecamp.categories.Themes;
import com.everest.basecamp.categories.System;
import com.everest.basecamp.categories.About;

import com.everest.basecamp.navigation.BubbleNavigationConstraintView;
import com.everest.basecamp.navigation.BubbleNavigationChangeListener;

public class BaseCamp extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_basecamp, container, false);

        BubbleNavigationConstraintView bubbleNavigationConstraintView = view.findViewById(R.id.floating_top_bar_navigation);
        ViewPager viewPager = view.findViewById(R.id.viewpager);
        PagerAdapter mPagerAdapter = new PagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(mPagerAdapter);

        // Set the initial item to "Themes" (second tab)
        int initialItem = 0;
        viewPager.setCurrentItem(initialItem, false);
        bubbleNavigationConstraintView.setCurrentActiveItem(initialItem);

        bubbleNavigationConstraintView.setNavigationChangeListener(new BubbleNavigationChangeListener() {
            @Override
            public void onNavigationChanged(View view, int position) {
                viewPager.setCurrentItem(position, true);
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {}

            @Override
            public void onPageSelected(int i) {
                bubbleNavigationConstraintView.setCurrentActiveItem(i);

                // Ensuring the text is visible for the selected item
                for (int j = 0; j < bubbleNavigationConstraintView.getChildCount(); j++) {
                    View child = bubbleNavigationConstraintView.getChildAt(j);
                    if (child instanceof TextView) {
                        TextView textView = (TextView) child;
                        if (j == i) {
                            textView.setVisibility(View.VISIBLE);
                        } else {
                            textView.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {}
        });

        return view;
    }

    class PagerAdapter extends FragmentPagerAdapter {
        String titles[] = getTitles();
        private Fragment frags[] = new Fragment[titles.length];

        PagerAdapter(FragmentManager fm) {
            super(fm);
            frags[0] = new Themes();
            frags[1] = new System();
            frags[2] = new About();
        }

        @Override
        public Fragment getItem(int position) {
            return frags[position];
        }

        @Override
        public int getCount() {
            return frags.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }

    private String[] getTitles() {
        return new String[]{
            getString(R.string.themes_category),
            getString(R.string.system_category),
            getString(R.string.about_category)
        };
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.basecamp_title);
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.EVEREST;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        return true;
    }
}
