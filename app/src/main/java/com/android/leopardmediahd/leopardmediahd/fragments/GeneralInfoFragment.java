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

import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.leopardmediahd.R;
import com.android.leopardmediahd.functions.MizLib;
import com.android.leopardmediahd.utils.FileUtils;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class GeneralInfoFragment extends Fragment {

	private ImageView logo;
	private ImageView family;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.general_info, container, false);
	}

	@Override
	public void onViewCreated(View v, Bundle savedInstanceState) {
		super.onViewCreated(v, savedInstanceState);

		if (!MizLib.isTablet(getActivity()))
			getActivity().setTitle(R.string.prefsUIGeneral);

		logo = (ImageView) v.findViewById(R.id.logo);
		logo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.leopardmediahd.com"));
				startActivity(browserIntent);
			}
		});

		family = (ImageView) v.findViewById(R.id.family);
		family.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.smartleopard.tv"));
				startActivity(browserIntent);
			}
		});
	}
}