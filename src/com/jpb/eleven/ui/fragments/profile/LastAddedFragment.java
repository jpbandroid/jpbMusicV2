/*
 * Copyright (C) 2012 Andrew Neal
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
package com.jpb.eleven.ui.fragments.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.loader.content.Loader;

import com.jpb.eleven.Config;
import com.jpb.eleven.Config.SmartPlaylistType;
import org.lineageos.eleven.R;
import com.jpb.eleven.loaders.LastAddedLoader;
import com.jpb.eleven.model.Song;
import com.jpb.eleven.sectionadapter.SectionCreator;
import com.jpb.eleven.sectionadapter.SectionListContainer;
import com.jpb.eleven.ui.activities.BaseActivity;
import com.jpb.eleven.ui.fragments.ISetupActionBar;
import com.jpb.eleven.utils.MusicUtils;
import com.jpb.eleven.widgets.NoResultsContainer;

/**
 * This class is used to display all of the songs the user put on their device
 * within the last four weeks.
 *
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public class LastAddedFragment extends SmartPlaylistFragment implements ISetupActionBar {

    @NonNull
    @Override
    public Loader<SectionListContainer<Song>> onCreateLoader(final int id, final Bundle args) {
        // show the loading progress bar
        mLoadingEmptyContainer.showLoading();

        LastAddedLoader loader = new LastAddedLoader(getActivity());
        return new SectionCreator<>(getActivity(), loader, null);
    }

    @Override
    public void setupNoResultsContainer(NoResultsContainer empty) {
        super.setupNoResultsContainer(empty);

        empty.setMainText(R.string.empty_last_added_main);
        empty.setSecondaryText(R.string.empty_last_added);
    }

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container,
                                   Bundle savedInstanceState) {
        setupActionBar();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void setupActionBar() {
        final FragmentActivity activity = getActivity();
        if (activity instanceof BaseActivity) {
            final BaseActivity baseActivity = (BaseActivity) activity;
            baseActivity.setupActionBar(R.string.playlist_last_added);
            baseActivity.setActionBarElevation(true);
        }
    }

    @Override
    protected long getFragmentSourceId() {
        return Config.SmartPlaylistType.LastAdded.mId;
    }

    protected SmartPlaylistType getSmartPlaylistType() {
        return Config.SmartPlaylistType.LastAdded;
    }

    @Override
    protected int getShuffleTitleId() {
        return R.string.menu_shuffle_last_added;
    }

    @Override
    protected int getClearTitleId() {
        return R.string.clear_last_added;
    }

    @Override
    protected void clearList() {
        MusicUtils.clearLastAdded(getActivity());
    }
}
