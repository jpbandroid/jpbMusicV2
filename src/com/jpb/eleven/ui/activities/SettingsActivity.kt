/*
 * Copyright (C) 2012 Andrew Neal
 * Copyright (C) 2014 The CyanogenMod Project
 * Copyright (C) 2018-2021 The LineageOS Project
 * Copyright (C) 2019 SHIFT GmbH
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
package com.jpb.eleven.ui.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.DialogInterface
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.text.TextUtils
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.text.HtmlCompat
import androidx.preference.*
import com.jpb.eleven.*
import com.jpb.eleven.cache.ImageFetcher
import com.jpb.eleven.utils.MusicUtils
import com.jpb.eleven.utils.MusicUtils.ServiceToken
import com.jpb.eleven.utils.PreferenceUtils
import org.lineageos.eleven.R
import rikka.material.app.LocaleDelegate
import java.util.*


class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    class SettingsFragment : PreferenceFragmentCompat(), ServiceConnection,
        OnSharedPreferenceChangeListener {
        private var mToken: ServiceToken? = null
        private var mService: IElevenService? = null
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            // Since we use RenderEffect, we need to make sure we run >= Android S
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                preferenceScreen.removePreference(
                    Objects.requireNonNull(
                        findPreference(
                            PreferenceUtils.USE_BLUR
                        )
                    )
                )
            }
            val deleteCache = findPreference<Preference>("delete_cache")
            if (deleteCache != null) {
                deleteCache.onPreferenceClickListener =
                    Preference.OnPreferenceClickListener { preference: Preference? ->
                        AlertDialog.Builder(context)
                            .setMessage(R.string.delete_warning)
                            .setPositiveButton(android.R.string.ok) { dialog: DialogInterface?, which: Int ->
                                ImageFetcher.getInstance(
                                    context
                                ).clearCaches()
                            }
                            .setNegativeButton(R.string.cancel) { dialog: DialogInterface, which: Int -> dialog.dismiss() }
                            .show()
                        true
                    }
            }
            val md3pref = findPreference<SwitchPreferenceCompat>("md3")
            md3pref?.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _, newValue ->
                    GlobalValues.md3Theme = newValue as Boolean
                    activity?.recreate()
                    true
                }
            findPreference<ListPreference>(Constants.PREF_DARK_MODE)?.apply {
                this.setOnPreferenceChangeListener(
                    object : Preference.OnPreferenceChangeListener {
                        override fun onPreferenceChange(
                            preference: Preference,
                            newValue: Any?
                        ): Boolean {
                            val themeOption = newValue as String?
                            if (themeOption != null) {
                                ThemeHelper.applyTheme(themeOption)
                            }
                            return true
                        }


                    })





            val languagePreference =
                (findPreference<ListPreference>(Constants.PREF_LOCALE))?.apply {
                    setOnPreferenceChangeListener { _, newValue ->
                        if (newValue is String) {
                            val locale: Locale = if ("SYSTEM" == newValue) {
                                LocaleDelegate.systemLocale
                            } else {
                                Locale.forLanguageTag(newValue)
                            }
                            LocaleDelegate.defaultLocale = locale
                            activity?.recreate()
                        }
                        true
                    }
                }!!
            val tag = languagePreference.value
            val index = listOf(*languagePreference.entryValues).indexOf(tag)
            val localeName: MutableList<String> = ArrayList()
            val localeNameUser: MutableList<String> = ArrayList()
            val userLocale = GlobalValues.locale
            for (i in 1 until languagePreference.entries.size) {
                val locale = Locale.forLanguageTag(languagePreference.entries[i].toString())
                localeName.add(
                    if (!TextUtils.isEmpty(locale.script)) locale.getDisplayScript(locale) else locale.getDisplayName(
                        locale
                    )
                )
                localeNameUser.add(
                    if (!TextUtils.isEmpty(locale.script)) locale.getDisplayScript(
                        userLocale
                    ) else locale.getDisplayName(userLocale)
                )
            }

            for (i in 1 until languagePreference.entries.size) {
                if (index != i) {
                    languagePreference.entries[i] = HtmlCompat.fromHtml(
                        String.format(
                            "%s - %s",
                            localeName[i - 1],
                            localeNameUser[i - 1]
                        ),
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                } else {
                    languagePreference.entries[i] = localeNameUser[i - 1]
                }
            }

            if (TextUtils.isEmpty(tag) || "SYSTEM" == tag) {
                languagePreference.summary = getString(rikka.core.R.string.follow_system)
            } else if (index != -1) {
                val name = localeNameUser[index - 1]
                languagePreference.summary = name
            }}
            PreferenceUtils.getInstance(context).setOnSharedPreferenceChangeListener(this)
        }

        override fun onDestroy() {
            PreferenceUtils.getInstance(context).removeOnSharedPreferenceChangeListener(this)
            super.onDestroy()
        }

        override fun onCreatePreferences(bundle: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.settings, rootKey)
        }

        override fun onStart() {
            super.onStart()

            // Bind to Eleven's service
            mToken = MusicUtils.bindToService(activity, this)
        }

        override fun onStop() {
            super.onStop()

            // Unbind from the service
            MusicUtils.unbindFromService(mToken)
            mToken = null
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            mService = IElevenService.Stub.asInterface(service)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            mService = null
        }


        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            val activity: Activity? = activity
            when (key) {
                PreferenceUtils.SHOW_VISUALIZER -> {
                    val showVisualizer = sharedPreferences.getBoolean(key, false)
                    if (showVisualizer && activity != null &&
                        !PreferenceUtils.canRecordAudio(activity)
                    ) {
                        PreferenceUtils.requestRecordAudio(activity)
                    }
                }
                PreferenceUtils.USE_BLUR -> {
                    val useBlur = sharedPreferences.getBoolean(key, false)
                    if (activity != null) {
                        val fetcher = ImageFetcher.getInstance(activity)
                        fetcher.setUseBlur(useBlur)
                        fetcher.clearCaches()
                    }
                }
                PreferenceUtils.SHAKE_TO_PLAY -> {
                    val enableShakeToPlay = sharedPreferences.getBoolean(key, false)
                    try {
                        mService!!.setShakeToPlayEnabled(enableShakeToPlay)
                    } catch (exc: RemoteException) {
                        // do nothing
                    }
                }
            }
        }

    }
}