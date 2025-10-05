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
import android.preference.PreferenceManager;
import android.view.View;

import com.android.leopardmediahd.*;
import com.android.leopardmediahd.leopardmediahd.SplashScreen;
import com.android.leopardmediahd.leopardmediahd.fragments.ScheduledUpdatesFragment;

import java.util.Locale;

public class Splash extends Activity {

	private final int SPLASH_DISPLAY_LENGTH = 2000;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);

		if (Build.VERSION.SDK_INT < 19) {
			View v = this.getWindow().getDecorView();
			v.setSystemUiVisibility(View.GONE);
		} else {
			View decorView = this.getWindow().getDecorView();
			int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
			decorView.setSystemUiVisibility(uiOptions);
		}

		PreferenceManager.getDefaultSharedPreferences(this).edit().putString("DEFAULT_LOCALE", Locale.getDefault().getLanguage()).commit();

		String language = PreferenceManager.getDefaultSharedPreferences(this).getString("USER_SELECTED_LANGUAGE", "");
		if(language == "")
			language = Locale.getDefault().getLanguage();

		Locale locale = new Locale(language);
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		getApplicationContext().getResources().updateConfiguration(config, null);

		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				boolean isFirstLaunch = PreferenceManager.getDefaultSharedPreferences(Splash.this).getBoolean("IS_FIRST_LAUNCH", true);
				if(isFirstLaunch)
				{
					Intent mainIntent = new Intent(com.android.leopardmediahd.leopardmediahd.Splash.this, LanguageFirstLaunch.class);
					startActivity(mainIntent);
					finish();
				}
				else
				{
					Intent mainIntent = new Intent(com.android.leopardmediahd.leopardmediahd.Splash.this, SplashScreen.class);
					startActivity(mainIntent);
					finish();
				}
			}
		}, SPLASH_DISPLAY_LENGTH);
	}
}