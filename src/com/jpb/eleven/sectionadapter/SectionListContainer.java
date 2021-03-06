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
package com.jpb.eleven.sectionadapter;

import com.jpb.eleven.utils.SectionCreatorUtils;

import java.util.List;
import java.util.TreeMap;

/**
 * Simple Container that contains a list of T items as well as the map of section information
 *
 * @param <T> the type of item that the list contains
 */
public class SectionListContainer<T> {
    public TreeMap<Integer, SectionCreatorUtils.Section> mSections;
    public List<T> mListResults;

    public SectionListContainer(final TreeMap<Integer, SectionCreatorUtils.Section> sections,
                                final List<T> results) {
        mSections = sections;
        mListResults = results;
    }
}
