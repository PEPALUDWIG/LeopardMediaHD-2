package com.android.leopardmediahd.leopardmediahd.fragments;/*
 * Copyright (C) 2014 Michell Bak
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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;
import com.android.leopardmediahd.R;
import com.android.leopardmediahd.leopardmediahd.fragments.TvShowLibraryFragment;

public class TvShowLibraryOverviewFragment extends Fragment {

    private ViewPager mViewPager;
    private PagerSlidingTabStrip mTabs;

    public TvShowLibraryOverviewFragment() {} // Empty constructor

    public static TvShowLibraryOverviewFragment newInstance() {
        return new TvShowLibraryOverviewFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.viewpager_with_tabs, container, false);

        if (com.android.leopardmediahd.functions.MizLib.hasLollipop())
            ((ActionBarActivity) getActivity()).getSupportActionBar().setElevation(0);

        mViewPager = (ViewPager) v.findViewById(R.id.awesomepager);
        mViewPager.setPageMargin(com.android.leopardmediahd.functions.MizLib.convertDpToPixels(getActivity(), 16));

        mTabs = (PagerSlidingTabStrip) v.findViewById(R.id.tabs);

        mViewPager.setAdapter(new PagerAdapter(getChildFragmentManager()));
        mTabs.setViewPager(mViewPager);
        mTabs.setVisibility(View.VISIBLE);

        // Work-around a bug that sometimes happens with the tabs
        mViewPager.setCurrentItem(0);

        if (com.android.leopardmediahd.functions.MizLib.hasLollipop())
            mTabs.setElevation(1f);

        return v;
    }

    private class PagerAdapter extends FragmentPagerAdapter {

        private final String[] TITLES = {getString(R.string.choiceAllShows), getString(R.string.choiceFavorites), getString(R.string.choiceAired),
                getString(R.string.watched_tv_shows), getString(R.string.unwatched_tv_shows)};

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public Fragment getItem(int index) {
            switch (index) {
                case 0:
                    return com.android.leopardmediahd.leopardmediahd.fragments.TvShowLibraryFragment.newInstance(com.android.leopardmediahd.loader.TvShowLoader.ALL_SHOWS);
                case 1:
                    return com.android.leopardmediahd.leopardmediahd.fragments.TvShowLibraryFragment.newInstance(com.android.leopardmediahd.loader.TvShowLoader.FAVORITES);
                case 2:
                    return com.android.leopardmediahd.leopardmediahd.fragments.TvShowLibraryFragment.newInstance(com.android.leopardmediahd.loader.TvShowLoader.RECENTLY_AIRED);
                case 3:
                    return com.android.leopardmediahd.leopardmediahd.fragments.TvShowLibraryFragment.newInstance(com.android.leopardmediahd.loader.TvShowLoader.WATCHED);
                case 4:
                    return com.android.leopardmediahd.leopardmediahd.fragments.TvShowLibraryFragment.newInstance(com.android.leopardmediahd.loader.TvShowLoader.UNWATCHED);
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }
    }
}
