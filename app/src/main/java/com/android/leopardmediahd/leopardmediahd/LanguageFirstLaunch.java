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

package com.android.leopardmediahd.leopardmediahd;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.LinearLayout;

import com.android.leopardmediahd.R;

import java.util.Locale;

import static com.android.leopardmediahd.functions.PreferenceKeys.LANGUAGE_PREFERENCE;

public class LanguageFirstLaunch extends Activity {

	private LinearLayout Espanol, English, Col;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.language_first_launch);

		Espanol = (LinearLayout) this.findViewById(R.id.espanol);
		Espanol.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				PreferenceManager.getDefaultSharedPreferences(LanguageFirstLaunch.this).edit().putString("USER_SELECTED_LANGUAGE", "es").commit();
				Locale locale = new Locale("es");
				Locale.setDefault(locale);
				Configuration config = new Configuration();
				config.locale = locale;
				getApplicationContext().getResources().updateConfiguration(config, null);
				PreferenceManager.getDefaultSharedPreferences(LanguageFirstLaunch.this).edit().putString(LANGUAGE_PREFERENCE, "es").commit();
				PreferenceManager.getDefaultSharedPreferences(LanguageFirstLaunch.this).edit().putBoolean("IS_FIRST_LAUNCH", false).commit();
				Intent mainIntent = new Intent(LanguageFirstLaunch.this, SplashScreen.class);
				startActivity(mainIntent);
				finish();
			}
		});

		English = (LinearLayout) this.findViewById(R.id.english);
		English.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				PreferenceManager.getDefaultSharedPreferences(LanguageFirstLaunch.this).edit().putString("USER_SELECTED_LANGUAGE", "en").commit();
				Locale locale = new Locale("en");
				Locale.setDefault(locale);
				Configuration config = new Configuration();
				config.locale = locale;
				getApplicationContext().getResources().updateConfiguration(config, null);
				PreferenceManager.getDefaultSharedPreferences(LanguageFirstLaunch.this).edit().putString(LANGUAGE_PREFERENCE, "en").commit();
				PreferenceManager.getDefaultSharedPreferences(LanguageFirstLaunch.this).edit().putBoolean("IS_FIRST_LAUNCH", false).commit();
				Intent mainIntent = new Intent(LanguageFirstLaunch.this, SplashScreen.class);
				startActivity(mainIntent);
				finish();
			}
		});

		Col = (LinearLayout) this.findViewById(R.id.col);
		Col.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String localeL = PreferenceManager.getDefaultSharedPreferences(LanguageFirstLaunch.this).getString("DEFAULT_LOCALE", "en");
				PreferenceManager.getDefaultSharedPreferences(LanguageFirstLaunch.this).edit().putString("USER_SELECTED_LANGUAGE", localeL).commit();
				Locale locale = new Locale(localeL);
				Locale.setDefault(locale);
				Configuration config = new Configuration();
				config.locale = locale;
				getApplicationContext().getResources().updateConfiguration(config, null);
				PreferenceManager.getDefaultSharedPreferences(LanguageFirstLaunch.this).edit().putString(LANGUAGE_PREFERENCE, localeL).commit();
				PreferenceManager.getDefaultSharedPreferences(LanguageFirstLaunch.this).edit().putBoolean("IS_FIRST_LAUNCH", false).commit();
				Intent mainIntent = new Intent(LanguageFirstLaunch.this, SplashScreen.class);
				startActivity(mainIntent);
				finish();
			}
		});
	}
}