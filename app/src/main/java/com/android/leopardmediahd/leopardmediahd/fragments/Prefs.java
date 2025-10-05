/*
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

package com.android.leopardmediahd.leopardmediahd.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.android.leopardmediahd.functions.PreferenceKeys;
import com.android.leopardmediahd.leopardmediahd.MizuuApplication;
import com.android.leopardmediahd.R;
import com.android.leopardmediahd.utils.FileUtils;
import com.android.leopardmediahd.utils.LocalBroadcastUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import static com.android.leopardmediahd.functions.PreferenceKeys.IGNORED_FILES_ENABLED;
import static com.android.leopardmediahd.functions.PreferenceKeys.LANGUAGE_PREFERENCE;

public class Prefs extends PreferenceFragment implements OnSharedPreferenceChangeListener {

	private Preference mPref, mLanguagePref, mCopyDatabase, mIgnoreNfoFiles;
	private CheckBoxPreference mLanguageEspanol, mLanguageEnglish, mLanguageCurrent;
	private Locale[] mSystemLocales;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		int res=getActivity().getResources().getIdentifier(getArguments().getString("resource"), "xml", getActivity().getPackageName());
		addPreferencesFromResource(res);

		PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);

		mLanguageEspanol = (CheckBoxPreference) getPreferenceScreen().findPreference("prefsApplicationLanguageEspanol");
		mLanguageEnglish = (CheckBoxPreference) getPreferenceScreen().findPreference("prefsApplicationLanguageEnglish");
		mLanguageCurrent = (CheckBoxPreference) getPreferenceScreen().findPreference("prefsApplicationLanguageCurrent");

		try {
			mLanguageEspanol.setChecked(false);
			mLanguageEnglish.setChecked(false);
			mLanguageCurrent.setChecked(false);

			String l = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("USER_SELECTED_LANGUAGE", "en");
			switch (l)
			{
				case "en":
					mLanguageEnglish.setChecked(true);
					break;
				case "es":
					mLanguageEspanol.setChecked(true);
					break;
				default:
					mLanguageCurrent.setChecked(true);
					break;
			}
		}
		catch(Exception e) { }

		if (mLanguageEspanol != null)
			mLanguageEspanol.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					savePreference("USER_SELECTED_LANGUAGE", "es");
					savePreference(LANGUAGE_PREFERENCE, "es");
					mLanguageEnglish.setChecked(false);
					mLanguageCurrent.setChecked(false);
					return true;
				}
			});

		if (mLanguageEnglish != null)
			mLanguageEnglish.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					savePreference("USER_SELECTED_LANGUAGE", "en");
					savePreference(LANGUAGE_PREFERENCE, "en");
					mLanguageEspanol.setChecked(false);
					mLanguageCurrent.setChecked(false);
					return true;
				}
			});

		if (mLanguageCurrent != null)
			mLanguageCurrent.setOnPreferenceClickListener(new OnPreferenceClickListener() {
				@Override
				public boolean onPreferenceClick(Preference preference) {
					String locale = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("DEFAULT_LOCALE", "en");
					savePreference("USER_SELECTED_LANGUAGE", locale);
					savePreference(LANGUAGE_PREFERENCE, locale);
					mLanguageEnglish.setChecked(false);
					mLanguageEspanol.setChecked(false);
					return true;
				}
			});

		mPref = getPreferenceScreen().findPreference("prefsIgnoredFiles");
		if (mPref != null)
			mPref.setEnabled(PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(IGNORED_FILES_ENABLED, false));

        mCopyDatabase = getPreferenceScreen().findPreference("prefsCopyDatabase");
        if (mCopyDatabase != null)
            mCopyDatabase.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    String path = FileUtils.copyDatabase(getActivity());

                    if (!TextUtils.isEmpty(path)) {
                        Toast.makeText(getActivity(), getString(R.string.database_copied) + "\n(" + path + ")", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(), R.string.errorSomethingWentWrong, Toast.LENGTH_SHORT).show();
                    }

                    return true;
                }
            });

        mIgnoreNfoFiles = getPreferenceScreen().findPreference(PreferenceKeys.IGNORED_NFO_FILES);
        if (mIgnoreNfoFiles != null)
            mIgnoreNfoFiles.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    // Clear the cache
                    MizuuApplication.clearLruCache(getActivity());

                    // Refresh the movie library
                    LocalBroadcastUtils.updateMovieLibrary(getActivity());

                    return true;
                }
            });

		mLanguagePref = getPreferenceScreen().findPreference(LANGUAGE_PREFERENCE);
		if (mLanguagePref != null)
			mLanguagePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

				@Override
				public boolean onPreferenceClick(Preference preference) {

					mSystemLocales = Locale.getAvailableLocales();
					String[] languageCodes = Locale.getISOLanguages();

					final ArrayList<Locale> mTemp = new ArrayList<Locale>();
					for (String code : languageCodes) {
						if (code.length() == 2) { // We're only interested in two character codes
							Locale l = new Locale(code);
							if (hasLocale(l))
								mTemp.add(l);
						}
					}

					Collections.sort(mTemp, new Comparator<Locale>() {
						@Override
						public int compare(Locale lhs, Locale rhs) {
							return lhs.getDisplayLanguage(Locale.getDefault()).compareToIgnoreCase(rhs.getDisplayLanguage(Locale.getDefault()));
						}
					});

					String[] items = new String[mTemp.size()];
					for (int i = 0; i < mTemp.size(); i++)
						items[i] = mTemp.get(i).getDisplayLanguage(Locale.getDefault());
					
					final String[] codes = new String[mTemp.size()];
					for (int i = 0; i < mTemp.size(); i++)
						codes[i] = mTemp.get(i).getLanguage();
					
					mTemp.clear();
					
					int checkedItem = getIndexForLocale(codes, PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(LANGUAGE_PREFERENCE, "en"));
					if (checkedItem == -1)
						checkedItem = getIndexForLocale(codes, "en"); // "en" by default
					
					AlertDialog.Builder bldr = new AlertDialog.Builder(getActivity());
					bldr.setTitle(R.string.set_pref_language_title);
					bldr.setSingleChoiceItems(items, checkedItem, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							savePreference(LANGUAGE_PREFERENCE, codes[which]);
							dialog.dismiss();
						}
					});
					bldr.show();

					return true;
				}
			});
	}
	
	private void savePreference(String key, String value) {
		PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString(key, value).commit();
	}

	private void getPreference(String key) {
		PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(key, "");
	}
	
	private boolean hasLocale(Locale l) {
		for (Locale locale : mSystemLocales)
			if (locale.equals(l))
				return true;
		return false;
	}
	
	public int getIndexForLocale(String[] languages, String locale) {
		for (int i = 0; i < languages.length; i++)
			if (languages[i].equalsIgnoreCase(locale))
				return i;
		return -1;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(IGNORED_FILES_ENABLED)) {
			if (mPref != null)
				mPref.setEnabled(sharedPreferences.getBoolean(IGNORED_FILES_ENABLED, false));
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(this);
	}
}