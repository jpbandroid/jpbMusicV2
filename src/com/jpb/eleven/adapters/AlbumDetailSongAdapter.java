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
package com.jpb.eleven.adapters;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.jpb.eleven.Config;
import org.lineageos.eleven.R;
import com.jpb.eleven.cache.ImageFetcher;
import com.jpb.eleven.model.Song;
import com.jpb.eleven.utils.MusicUtils;

public class AlbumDetailSongAdapter extends DetailSongAdapter {

    public AlbumDetailSongAdapter(FragmentActivity activity) {
        super(activity);
    }

    @Override
    protected int rowLayoutId() {
        return R.layout.album_detail_song;
    }

    @Override
    protected Config.IdType getSourceType() {
        return Config.IdType.Album;
    }

    protected Holder newHolder(View root, ImageFetcher fetcher) {
        return new AlbumHolder(root, fetcher, mContext);
    }

    private static class AlbumHolder extends Holder {
        TextView duration;
        Context context;

        protected AlbumHolder(View root, ImageFetcher fetcher, Context context) {
            super(root, fetcher);
            this.context = context;
            duration = root.findViewById(R.id.duration);
        }

        protected void update(Song song) {
            title.setText(song.mSongName);
            duration.setText(MusicUtils.makeShortTimeString(context, song.mDuration));
        }
    }
}
