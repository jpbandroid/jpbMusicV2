/*
 * Copyright (C) 2010 The Android Open Source Project
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
package com.jpb.eleven.locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jpb.eleven.provider.LocalizedStore;

/**
 * Locale change intent receiver that invokes {@link LocalizedStore} to update
 * the database for the new locale.
 */
public class LocaleChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_LOCALE_CHANGED.equals(action)) {
            LocalizedStore.getInstance(context).onLocaleChanged();
        }
    }
}
