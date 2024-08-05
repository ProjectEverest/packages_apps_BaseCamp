/*
 * Copyright (C) 2024 Project-Pixelstar
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

package com.everest.basecamp.categories;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.widget.LayoutPreference;
import com.google.android.material.card.MaterialCardView;
import com.android.internal.logging.nano.MetricsProto;

public class Themes extends SettingsPreferenceFragment implements View.OnClickListener, View.OnLongClickListener {

    private LayoutPreference mPreference;
    private MaterialCardView lsclock;
    private LinearLayout qs, statusbar;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.themes);

        mPreference = findPreference("basecamp_header");
        lsclock = mPreference.findViewById(R.id.wallpaper);
        statusbar = mPreference.findViewById(R.id.statusbar);
        qs = mPreference.findViewById(R.id.qs);

        lsclock.setOnClickListener(this);
        lsclock.setOnLongClickListener(this);
        qs.setOnClickListener(this);
        statusbar.setOnClickListener(this);

        View headerView = mPreference.findViewById(R.id.header_root);
        if (headerView != null) {
            headerView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true; // Intercept touch events
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        if (v == lsclock) {
            startActivity("LockscreenActivity");
        } else if (v == qs) {
            startActivity("QSActivity");
        } else if (v == statusbar) {
            startActivity("StatusBarActivity");
        }
    }

    private void startActivity(String activity) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setClassName("com.android.settings", "com.android.settings.Settings$" + activity);
        getContext().startActivity(intent);
    }

    public boolean onLongClick(View view) {
        if (view.getId() == R.id.wallpaper) {
            launchWallpaperPickerActivity();
            return true;
        }
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.EVEREST;
    }

    private void launchWallpaperPickerActivity() {
        Intent intent = new Intent();
        intent.setClassName("com.google.android.apps.wallpaper", "com.google.android.apps.wallpaper.picker.CategoryPickerActivity");
        startActivity(intent);
    }

    @Override
    public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
        RecyclerView rcv = super.onCreateRecyclerView(inflater, container, icicle);
        GridLayoutManager layoutG = new GridLayoutManager(getActivity(), 2);
        layoutG.setSpanSizeLookup(new SpanSizeLookupG());
        rcv.setLayoutManager(layoutG);
        return rcv;
    }

    class SpanSizeLookupG extends GridLayoutManager.SpanSizeLookup {
        @Override
        public int getSpanSize(int position) {
            if (position == 0) {
                return 2;
            } else {
                return 1;
            }
        }
    }
}
