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
package com.jpb.eleven.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jpb.eleven.MusicStateListener;
import org.lineageos.eleven.R;
import com.jpb.eleven.adapters.ArtistAdapter;
import com.jpb.eleven.adapters.PagerAdapter;
import com.jpb.eleven.loaders.ArtistLoader;
import com.jpb.eleven.model.Artist;
import com.jpb.eleven.sectionadapter.SectionCreator;
import com.jpb.eleven.sectionadapter.SectionListContainer;
import com.jpb.eleven.ui.activities.BaseActivity;
import com.jpb.eleven.ui.fragments.phone.MusicBrowserFragment;
import com.jpb.eleven.utils.ArtistPopupMenuHelper;
import com.jpb.eleven.utils.NavUtils;
import com.jpb.eleven.utils.PopupMenuHelper;
import com.jpb.eleven.utils.SectionCreatorUtils;
import com.jpb.eleven.utils.SectionCreatorUtils.IItemCompare;
import com.jpb.eleven.widgets.LoadingEmptyContainer;
import com.jpb.eleven.widgets.SectionSeparatorItemDecoration;

import java.util.TreeMap;

/**
 * This class is used to display all of the artists on a user's device.
 *
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public class ArtistFragment extends MusicBrowserFragment implements
        LoaderManager.LoaderCallbacks<SectionListContainer<Artist>>, MusicStateListener {

    /**
     * Fragment UI
     */
    private ViewGroup mRootView;

    /**
     * The adapter for the grid
     */
    private ArtistAdapter mAdapter;

    /**
     * Pop up menu helper
     */
    private PopupMenuHelper mPopupMenuHelper;

    /**
     * Loading container and no results container
     */
    private LoadingEmptyContainer mLoadingEmptyContainer;
    /**
     * The list view.
     */
    private RecyclerView mListView;

    /**
     * Empty constructor as per the {@link Fragment} documentation
     */
    public ArtistFragment() {
    }

    @Override
    public int getLoaderId() {
        return PagerAdapter.MusicFragments.ARTIST.ordinal();
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPopupMenuHelper = new ArtistPopupMenuHelper(getActivity(), getChildFragmentManager()) {
            @Override
            public Artist getArtist(int position) {
                return mAdapter.getItem(position);
            }
        };

        // Create the adapter
        final int layout = R.layout.list_item_normal;
        mAdapter = new ArtistAdapter(requireActivity(), layout, this::onItemClick);
        mAdapter.setPopupMenuClickedListener((v, position) ->
                mPopupMenuHelper.showPopupMenu(v, position));
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        // The View for the fragment's UI
        mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_list, container, false);
        initListView();

        // Register the music status listener
        final Activity activity = getActivity();
        if (activity instanceof BaseActivity) {
            ((BaseActivity) activity).setMusicStateListenerListener(this);
        }

        return mRootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        final Activity activity = getActivity();
        if (activity instanceof BaseActivity) {
            ((BaseActivity) activity).removeMusicStateListenerListener(this);
        }
    }


    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Enable the options menu
        setHasOptionsMenu(true);
        // Start the loader
        initLoader(null, this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mAdapter.flush();
    }

    public void onItemClick(final int position) {
        Artist artist = mAdapter.getItem(position);
        NavUtils.openArtistProfile(getActivity(), artist.mArtistName);
    }

    @NonNull
    @Override
    public Loader<SectionListContainer<Artist>> onCreateLoader(final int id, final Bundle args) {
        mLoadingEmptyContainer.showLoading();
        final Context context = getActivity();
        IItemCompare<Artist> comparator = SectionCreatorUtils.createArtistComparison(context);
        return new SectionCreator<>(getActivity(), new ArtistLoader(context), comparator);
    }

    @Override
    public void onLoadFinished(@NonNull final Loader<SectionListContainer<Artist>> loader,
                               final SectionListContainer<Artist> data) {
        Handler handler = new Handler(requireActivity().getMainLooper());
        if (data.mListResults.isEmpty()) {
            handler.post(() -> mAdapter.unload());
            mLoadingEmptyContainer.showNoResults();
            return;
        }

        mLoadingEmptyContainer.setVisibility(View.GONE);

        handler.post(() -> mAdapter.setData(data.mListResults));
        setHeaders(data.mSections);
    }

    @Override
    public void onLoaderReset(@NonNull final Loader<SectionListContainer<Artist>> loader) {
        // Clear the data in the adapter
        mAdapter.unload();
    }

    /**
     * Restarts the loader.
     */
    public void refresh() {
        // Wait a moment for the preference to change.
        SystemClock.sleep(10);
        restartLoader();
    }

    @Override
    public void restartLoader() {
        // Update the list when the user deletes any items
        restartLoader(null, this);
    }

    @Override
    public void onMetaChanged() {
        // Nothing to do
    }

    @Override
    public void onPlaylistChanged() {
        // Nothing to do
    }

    private void setHeaders(TreeMap<Integer, SectionCreatorUtils.Section> sections) {
        for (int i = 0; i < mListView.getItemDecorationCount(); i++) {
            mListView.removeItemDecorationAt(i);
        }
        mListView.addItemDecoration(new SectionSeparatorItemDecoration(requireContext(), sections));
    }

    /**
     * Sets up the list view
     */
    private void initListView() {
        // Initialize the grid
        mListView = mRootView.findViewById(R.id.list_base);
        // Set the data behind the list
        mListView.setAdapter(mAdapter);
        mListView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        mListView.setItemAnimator(new DefaultItemAnimator());

        // set the loading and empty view container
        mLoadingEmptyContainer = mRootView.findViewById(R.id.loading_empty_container);
        mLoadingEmptyContainer.setVisibility(View.VISIBLE);
    }
}
