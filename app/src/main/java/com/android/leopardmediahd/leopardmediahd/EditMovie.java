package com.android.leopardmediahd.leopardmediahd;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.android.leopardmediahd.base.MizActivity;
import com.android.leopardmediahd.functions.IntentKeys;
import com.android.leopardmediahd.leopardmediahd.fragments.EditMovieFragment;
import com.android.leopardmediahd.utils.ViewUtils;

public class EditMovie extends MizActivity {

    private int mToolbarColor;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final String movieId = getIntent().getStringExtra("movieId");
        mToolbarColor = getIntent().getExtras().getInt(IntentKeys.TOOLBAR_COLOR);

		final FragmentManager fm = getSupportFragmentManager();
		
		if (fm.findFragmentById(android.R.id.content) == null) {
			fm.beginTransaction().add(android.R.id.content, EditMovieFragment.newInstance(movieId)).commit();
		}
	}

    @Override
    public void onStart() {
        super.onStart();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ViewUtils.setToolbarAndStatusBarColor(getSupportActionBar(), getWindow(), mToolbarColor);
    }
	
	@Override
	protected int getLayoutResource() {
		return 0;
	}
}
