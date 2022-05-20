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
package com.jpb.eleven.ui.activities

import rikka.material.internal.ThemedAppCompatActivity
import rikka.material.app.MaterialActivity
import android.content.ServiceConnection
import com.jpb.eleven.MusicStateListener
import com.jpb.eleven.cache.ICacheListener
import java.util.ArrayList
import com.jpb.eleven.utils.Lists
import com.jpb.eleven.utils.MusicUtils.ServiceToken
import com.jpb.eleven.widgets.PlayPauseProgressButton
import com.jpb.eleven.widgets.PlayPauseButtonContainer
import android.widget.TextView
import android.widget.ImageView
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.media.AudioManager
import org.lineageos.eleven.R
import androidx.core.content.ContextCompat
import com.jpb.eleven.cache.ImageFetcher
import android.content.ComponentName
import android.os.IBinder
import android.view.Menu
import android.view.MenuItem
import android.content.Intent
import com.jpb.eleven.about.AboutActivity
import com.jpb.eleven.utils.MusicUtils
import android.content.IntentFilter
import com.jpb.eleven.MusicPlaybackService
import android.graphics.drawable.ColorDrawable
import android.view.View
import com.jpb.eleven.utils.ElevenUtils
import com.jpb.eleven.ui.activities.BaseActivity
import android.content.BroadcastReceiver
import java.lang.ref.WeakReference
import android.content.Context
import android.content.res.Resources
import android.util.TypedValue
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.jpb.eleven.GlobalValues
import com.jpb.eleven.utils.NavUtils
import java.lang.UnsupportedOperationException

/**
 * A base [FragmentActivity] used to update the bottom bar and
 * bind to Eleven's service.
 *
 *
 * [SlidingPanelActivity] extends from this skeleton.
 *
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
abstract class BaseActivity : AppCompatActivity(), ServiceConnection, MusicStateListener,
    ICacheListener {
    /**
     * Play-state and meta change listener
     */
    private val mMusicStateListener = Lists.newArrayList<MusicStateListener>()
    private var mToolBar: Toolbar? = null
    private var mActionBarHeight = 0

    /**
     * The service token
     */
    private var mToken: ServiceToken? = null

    /**
     * Play pause progress button
     */
    private var mPlayPauseProgressButton: PlayPauseProgressButton? = null
    private var mPlayPauseButtonContainer: PlayPauseButtonContainer? = null

    /**
     * Track name (BAB)
     */
    private var mTrackName: TextView? = null

    /**
     * Artist name (BAB)
     */
    private var mArtistName: TextView? = null

    /**
     * Album art (BAB)
     */
    private var mAlbumArt: ImageView? = null

    /**
     * Broadcast receiver
     */
    private var mPlaybackStatus: PlaybackStatus? = null
    private var mActionBarBackground: Drawable? = null

    /**
     * Called when all requirements (like permissions) are satisfied and we are ready
     * to initialize the app.
     */

    fun computeUserThemeKey(): String {
        return GlobalValues.darkMode + GlobalValues.rengeTheme + GlobalValues.md3Theme
    }

    private val localeDelegate by lazy {
        LocaleDelegate()
    }
    
    protected open fun init(savedInstanceState: Bundle?) {
        // Control the media volume
        volumeControlStream = AudioManager.STREAM_MUSIC

        // Initialize the broadcast receiver
        mPlaybackStatus = PlaybackStatus(this)

        // Calculate ActionBar height
        val value = TypedValue()
        if (theme.resolveAttribute(android.R.attr.actionBarSize, value, true)) {
            mActionBarHeight = TypedValue.complexToDimensionPixelSize(
                value.data,
                resources.displayMetrics
            )
        }

        if (GlobalValues.md3Theme) {
            theme.applyStyle(R.style.Theme_MD3, true)
        }
        if (GlobalValues.rengeTheme) {
            theme.applyStyle(R.style.Theme_Renge, true)
        }

        // Set the layout
        setContentView(setContentView())
        mToolBar = findViewById(R.id.toolbar)
        setSupportActionBar(mToolBar)
        setActionBarTitle(getString(R.string.app_name))

        // set the background on the root view
        window.decorView.rootView.setBackgroundColor(
            ContextCompat.getColor(this, R.color.background_color)
        )
        // Initialize the bottom action bar
        initBottomActionBar()

        // listen to changes to the cache status
        ImageFetcher.getInstance(this).addCacheListener(this)
    }

    val isInitialized: Boolean
        get() = mToken != null

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        // Set the playback drawables
        updatePlaybackControls()
        // Current info
        onMetaChanged()
        // if there were any pending intents while the service was started
        handlePendingPlaybackRequests()
    }

    override fun onServiceDisconnected(name: ComponentName) {}
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Settings
        menuInflater.inflate(R.menu.activity_base, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_settings) {
            // Settings
            NavUtils.openSettings(this)
            return true
        }
        if (item.itemId == R.id.menu_about) {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        if (isInitialized) {
            // Set the playback drawables
            updatePlaybackControls()
            // Current info
            onMetaChanged()
        }
    }

    override fun onStart() {
        super.onStart()

        // Bind Eleven's service
        mToken = MusicUtils.bindToService(this, this)
        val filter = IntentFilter()
        // Play and pause changes
        filter.addAction(MusicPlaybackService.PLAYSTATE_CHANGED)
        // Track changes
        filter.addAction(MusicPlaybackService.META_CHANGED)
        // Update a list, probably the playlist fragment's
        filter.addAction(MusicPlaybackService.REFRESH)
        // If a playlist has changed, notify us
        filter.addAction(MusicPlaybackService.PLAYLIST_CHANGED)
        // If there is an error playing a track
        filter.addAction(MusicPlaybackService.TRACK_ERROR)
        registerReceiver(mPlaybackStatus, filter)
    }

    override fun onStop() {
        super.onStop()

        // Unbind from the service
        MusicUtils.unbindFromService(mToken)
        mToken = null

        // Unregister the receiver
        try {
            unregisterReceiver(mPlaybackStatus)
        } catch (e: Throwable) {
            //$FALL-THROUGH$
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Remove any music status listeners
        mMusicStateListener.clear()

        // remove cache listeners
        ImageFetcher.getInstance(this).removeCacheListener(this)
    }

    fun setupActionBar(resId: Int) {
        setupActionBar(getString(resId))
    }

    fun setupActionBar(title: String?) {
        setActionBarTitle(title)
        if (mActionBarBackground == null) {
            val actionBarColor = ContextCompat.getColor(
                this,
                R.color.header_action_bar_color
            )
            mActionBarBackground = ColorDrawable(actionBarColor)
            mToolBar!!.background = mActionBarBackground
        }
    }

    fun setActionBarTitle(title: String?) {
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.title = title
        }
    }

    fun setActionBarAlpha(alpha: Int) {
        mActionBarBackground!!.alpha = alpha
    }

    fun setActionBarElevation(isElevated: Boolean) {
        val targetElevation: Float =
            if (isElevated) resources.getDimension(R.dimen.action_bar_elevation) else 0F
        mToolBar!!.elevation = targetElevation
    }

    fun setFragmentPadding(enablePadding: Boolean) {
        val height = if (enablePadding) mActionBarHeight else 0
        findViewById<View>(R.id.activity_base_content).setPadding(0, height, 0, 0)
    }

    /**
     * Initializes the items in the bottom action bar.
     */
    protected open fun initBottomActionBar() {
        // Play and pause button
        mPlayPauseProgressButton = findViewById(R.id.playPauseProgressButtonAlt)
        mPlayPauseProgressButton?.enableAndShow()
        if (mPlayPauseButtonContainer != null) {
            mPlayPauseButtonContainer = findViewById(R.id.playPauseProgressButton)
            mPlayPauseButtonContainer?.enableAndShow()
        }

        // Track name
        mTrackName = findViewById(R.id.bottom_action_bar_line_one)
        // Artist name
        mArtistName = findViewById(R.id.bottom_action_bar_line_two)
        // Album art
        mAlbumArt = findViewById(R.id.bottom_action_bar_album_art)
        // Open to the currently playing album profile
        mAlbumArt?.setOnClickListener(mOpenCurrentAlbumProfile)
    }

    protected open fun clearMetaInfo() {
        mAlbumArt!!.setImageResource(R.drawable.default_artwork)
    }

    /**
     * Sets the track name, album name, and album art.
     */
    private fun updateBottomActionBarInfo() {
        // Set the track name
        mTrackName!!.text = MusicUtils.getTrackName()
        // Set the artist name
        mArtistName!!.text = MusicUtils.getArtistName()
        // Set the album art
        ElevenUtils.getImageFetcher(this).loadCurrentArtwork(mAlbumArt)
    }

    /**
     * Sets the correct drawable states for the playback controls.
     */
    private fun updatePlaybackControls() {
        // Set the play and pause image
        if (mPlayPauseButtonContainer != null) {
            mPlayPauseButtonContainer!!.updateState()
        }
        mPlayPauseProgressButton!!.updateState()
    }

    /**
     * Opens the album profile of the currently playing album
     */
    private val mOpenCurrentAlbumProfile = View.OnClickListener { v: View? ->
        if (MusicUtils.getCurrentAudioId() != -1L) {
            NavUtils.openAlbumProfile(
                this@BaseActivity, MusicUtils.getAlbumName(),
                MusicUtils.getArtistName(), MusicUtils.getCurrentAlbumId()
            )
        } else {
            MusicUtils.shuffleAll(this@BaseActivity)
        }
    }

    /**
     * Used to monitor the state of playback
     */
    private class PlaybackStatus(activity: BaseActivity) : BroadcastReceiver() {
        private val mReference: WeakReference<BaseActivity> = WeakReference(activity)
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == null || action.isEmpty()) {
                return
            }
            val baseActivity = mReference.get() ?: return
            when (action) {
                MusicPlaybackService.META_CHANGED -> baseActivity.onMetaChanged()
                MusicPlaybackService.PLAYSTATE_CHANGED -> {
                    if (baseActivity.mPlayPauseButtonContainer != null) {
                        baseActivity.mPlayPauseButtonContainer!!.updateState()
                    }
                    if (baseActivity.mPlayPauseProgressButton != null) {
                        baseActivity.mPlayPauseProgressButton!!.updateState()
                    }
                }
                MusicPlaybackService.REFRESH -> baseActivity.restartLoader()
                MusicPlaybackService.PLAYLIST_CHANGED -> baseActivity.onPlaylistChanged()
                MusicPlaybackService.TRACK_ERROR -> {
                    val errorMsg = context.getString(
                        R.string.error_playing_track,
                        intent.getStringExtra(MusicPlaybackService.TrackErrorExtra.TRACK_NAME)
                    )
                    Toast.makeText(baseActivity, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    override fun onMetaChanged() {
        // update action bar info
        updateBottomActionBarInfo()

        // Let the listener know to the meta changed
        for (listener in mMusicStateListener) {
            listener?.onMetaChanged()
        }
    }

    override fun restartLoader() {
        // Let the listener know to update a list
        for (listener in mMusicStateListener) {
            listener?.restartLoader()
        }
    }

    override fun onPlaylistChanged() {
        // Let the listener know to update a list
        for (listener in mMusicStateListener) {
            listener?.onPlaylistChanged()
        }
    }

    /**
     * @param status The [MusicStateListener] to use
     */
    fun setMusicStateListenerListener(status: MusicStateListener?) {
        if (status === this) {
            throw UnsupportedOperationException("Override the method, don't add a listener")
        }
        if (status != null) {
            mMusicStateListener.add(status)
        }
    }

    /**
     * @param status The [MusicStateListener] to use
     */
    fun removeMusicStateListenerListener(status: MusicStateListener?) {
        if (status != null) {
            mMusicStateListener.remove(status)
        }
    }

    override fun onCacheResumed() {
        // Set the album art
        ElevenUtils.getImageFetcher(this).loadCurrentArtwork(mAlbumArt)
    }

    fun onApplyUserThemeResource(theme: Resources.Theme, isDecorView: Boolean) {
        theme.applyStyle(R.style.ThemeOverlay, true)

        if (GlobalValues.md3Theme) {
            theme.applyStyle(R.style.Theme_MD3, true)
        }
        if (GlobalValues.rengeTheme) {
            theme.applyStyle(R.style.Theme_Renge, true)
        }
    }

    /**
     * @return The resource ID to be inflated.
     */
    abstract fun setContentView(): Int

    /**
     * handle pending playback requests
     */
    abstract fun handlePendingPlaybackRequests()
}