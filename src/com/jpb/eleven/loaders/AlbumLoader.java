/*
 * Copyright (C) 2012 Andrew Neal
 * Copyright (C) 2014 The CyanogenMod Project
 * Copyright (C) 2020-2021 The LineageOS Project
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
package com.jpb.eleven.loaders;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AlbumColumns;

import com.jpb.eleven.model.Album;
import com.jpb.eleven.provider.LocalizedStore;
import com.jpb.eleven.provider.LocalizedStore.SortParameter;
import com.jpb.eleven.sectionadapter.SectionCreator;
import com.jpb.eleven.utils.EmptyCursor;
import com.jpb.eleven.utils.Lists;
import com.jpb.eleven.utils.MusicUtils;
import com.jpb.eleven.utils.PreferenceUtils;
import com.jpb.eleven.utils.SortOrder;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to query  and return
 * the albums on a user's device.
 *
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public class AlbumLoader extends SectionCreator.SimpleListLoader<Album> {

    /**
     * The result
     */
    private final ArrayList<Album> mAlbumsList = Lists.newArrayList();

    /**
     * Additional selection filter
     */
    protected Long mArtistId;

    /**
     * @param context The {@link Context} to use
     */
    public AlbumLoader(final Context context) {
        this(context, null);
    }

    /**
     * @param context  The {@link Context} to use
     * @param artistId The artistId to filter against or null if none
     */
    public AlbumLoader(final Context context, final Long artistId) {
        super(context);

        mArtistId = artistId;
    }

    @Override
    public List<Album> loadInBackground() {

        return mAlbumsList;
    }

    /**
     * For string-based sorts, return the localized store sort parameter, otherwise return null
     *
     * @param sortOrder the song ordering preference selected by the user
     */
    private static LocalizedStore.SortParameter getSortParameter(String sortOrder) {
        if (sortOrder.equals(SortOrder.AlbumSortOrder.ALBUM_A_Z) ||
                sortOrder.equals(SortOrder.AlbumSortOrder.ALBUM_Z_A)) {
            return LocalizedStore.SortParameter.Album;
        } else if (sortOrder.equals(SortOrder.AlbumSortOrder.ALBUM_ARTIST)) {
            return LocalizedStore.SortParameter.Artist;
        }

        return null;
    }

    /**
     * Creates the {@link Cursor} used to run the query.
     *
     * @param context  The {@link Context} to use.
     * @param artistId The artistId we want to find albums for or null if we want all albums
     * @return The {@link Cursor} used to run the album query.
     */
    public static Cursor makeAlbumCursor(final Context context, final Long artistId) {
        // requested album ordering
        final String albumSortOrder = PreferenceUtils.getInstance(context).getAlbumSortOrder();
        Uri uri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        if (artistId != null) {
            if (artistId == -1) {
                return new EmptyCursor();
            }
            uri = MediaStore.Audio.Artists.Albums.getContentUri("external", artistId);
        }

        Cursor cursor = context.getContentResolver().query(uri,
                new String[]{
                        /* 0 */
                        AlbumColumns.ALBUM_ID,
                        /* 1 */
                        AlbumColumns.ALBUM,
                        /* 2 */
                        AlbumColumns.ARTIST,
                        /* 3 */
                        AlbumColumns.NUMBER_OF_SONGS,
                        /* 4 */
                        AlbumColumns.FIRST_YEAR
                }, null, null, albumSortOrder);

        // if our sort is a localized-based sort, grab localized data from the store
        final SortParameter sortParameter = getSortParameter(albumSortOrder);
        if (sortParameter != null && cursor != null) {
            final boolean descending = MusicUtils.isSortOrderDesending(albumSortOrder);
            return LocalizedStore.getInstance(context).getLocalizedSort(cursor,
                    AlbumColumns.ALBUM_ID, SortParameter.Album, sortParameter,
                    descending, artistId == null);
        }

        return cursor;
    }
}
