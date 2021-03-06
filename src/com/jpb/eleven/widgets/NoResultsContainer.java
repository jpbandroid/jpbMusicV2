/*
 * Copyright (C) 2014 The CyanogenMod Project
 * Copyright (C) 2021 The LineageOS Project
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
package com.jpb.eleven.widgets;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.lineageos.eleven.R;

/**
 * This class is the default empty state view for most listviews/fragments
 * It allows the ability to set a main text, a main highlight text and a secondary text
 * By default this container has some strings loaded, but other classes can call the apis to change
 * the text
 */
public class NoResultsContainer extends LinearLayout {
    public NoResultsContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * This changes the Main text (top-most text) of the empty container
     *
     * @param resId String resource id
     */
    public void setMainText(final int resId) {
        ((TextView) findViewById(R.id.no_results_main_text)).setText(resId);
    }

    public void setSecondaryText(final int resId) {
        ((TextView) findViewById(R.id.no_results_secondary_text)).setText(resId);
    }

    public void setTextColor(int color) {
        ((ImageView) findViewById(R.id.no_results_image))
                .setColorFilter(color, PorterDuff.Mode.SRC_IN);
        ((TextView) findViewById(R.id.no_results_main_text)).setTextColor(color);
        ((TextView) findViewById(R.id.no_results_main_highlight_text)).setTextColor(color);
        ((TextView) findViewById(R.id.no_results_secondary_text)).setTextColor(color);
    }
}
