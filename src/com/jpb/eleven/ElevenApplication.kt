/*
 * Copyright (C) 2012 Andrew Neal
 * Copyright (C) 2014 The CyanogenMod Project
 * Copyright (C) 2019-2021 The LineageOS Project
 * Copyright (C) 2022 jpb
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
package com.jpb.eleven

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import com.jpb.appcompat.darkmode.ThemeHelper.applyTheme
import com.jpb.eleven.cache.ImageCache
import rikka.material.app.DayNightDelegate
import rikka.material.app.LocaleDelegate


class ElevenApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (GlobalValues.md3Theme) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }
    }

    override fun onLowMemory() {
        ImageCache.getInstance(this)
        ImageCache.evictAll()
        super.onLowMemory()
    }

    companion object {
        var appContext: Context? = null
            private set
    }
}