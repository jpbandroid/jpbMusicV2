/*
 * Copyright (C) 2012 Andrew Neal
 * Copyright (C) 2014 The CyanogenMod Project
 * Copyright (C) 2018-2021 The LineageOS Project
 * Copyright (C) 2019 SHIFT GmbH
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
package com.jpb.eleven.ui.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.DialogInterface
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.jpb.appcompat.ExtendedAppCompatActivity
import com.jpb.appcompat.darkmode.ThemeHelper
import com.jpb.eleven.Constants
import com.jpb.eleven.IElevenService
import com.jpb.eleven.cache.ImageFetcher
import com.jpb.eleven.utils.MusicUtils
import com.jpb.eleven.utils.MusicUtils.ServiceToken
import com.jpb.eleven.utils.PreferenceUtils
import org.lineageos.eleven.R
import rikka.core.util.ContextUtils.requireActivity
import java.util.*


class SettingsActivity : ExtendedAppCompatActivity() {
    private val prefs: SharedPreferences? = null
    fun createLocaleConfiguration(language: String): Configuration {
        val config = Configuration()
        if (language.contains("_")) {
            val parts = language.split("_").toTypedArray()
            val locale = Locale(parts[0], parts[1])
            Locale.setDefault(locale)
            config.locale = locale
        }
        return config
    }

    fun getCountryName(name: String): String {
        for (locale in Locale.getAvailableLocales()) {
            if (name == locale.language + '_' + locale.country) {
                val language = locale.getDisplayName(locale)
                return language.substring(0, 1).uppercase(Locale.getDefault()) + language.substring(
                    1
                )
            }
        }
        return name
    }

    private fun onLanguageUpdated(newValue: String): Boolean {
        this.prefs!!.edit().putString("language", newValue).apply()
        val config: Configuration = if (newValue == "System Default") {
            this.createLocaleConfiguration(Resources.getSystem().configuration.locale.toString())
        } else {
            this.createLocaleConfiguration(newValue)
        }
        val resources = requireActivity<Activity>(this).baseContext.resources
        resources!!.updateConfiguration(config, resources.displayMetrics)
        requireActivity<Activity>(this).recreate()
        return true
    }

    private fun populateLanguages(languages: ListPreference) {
        val locales = resources.getStringArray(R.array.locales)
        val language = ArrayList<String>()
        for (locale in locales) {
            language.add(this.getCountryName(locale))
        }
        val languageValue = language.toTypedArray()
        languages.entries = languageValue
    }

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





            val languages =
                (findPreference<ListPreference>("locale"))?.apply {
                    setOnPreferenceChangeListener { _, newValue ->
                        (newValue as String?)?.let { SettingsActivity().onLanguageUpdated(it) }
                        true
        }}}}

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