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

package com.android.leopardmediahd.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.leopardmediahd.leopardmediahd.MizuuApplication;
import com.android.leopardmediahd.R;

import java.io.File;
import java.util.ArrayList;

public class IdentifyTvShowService extends IntentService implements com.android.leopardmediahd.functions.TvShowLibraryUpdateCallback {

	private boolean mDebugging = true;
	private String mNewShowId, mOldShowId, mLanguage;
	private ArrayList<com.android.leopardmediahd.identification.ShowStructure> mFiles = new ArrayList<com.android.leopardmediahd.identification.ShowStructure>();
	private final int NOTIFICATION_ID = 4100;
	private int mEpisodeCount, mTotalFiles;
	private NotificationManager mNotificationManager;
	private NotificationCompat.Builder mBuilder;

	public IdentifyTvShowService() {
		super("IdentifyTvShowService");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		log("onDestroy()");

		if (mNotificationManager == null)
			mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		mNotificationManager.cancel(NOTIFICATION_ID);
		
		com.android.leopardmediahd.utils.LocalBroadcastUtils.updateTvShowLibrary(this);

		com.android.leopardmediahd.utils.WidgetUtils.updateTvShowWidgets(this);
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		if (com.android.leopardmediahd.functions.MizLib.isTvShowLibraryBeingUpdated(this)) {
			Handler mHandler = new Handler(Looper.getMainLooper());
			mHandler.post(new Runnable() {            
		        @Override
		        public void run() {
		            Toast.makeText(IdentifyTvShowService.this, R.string.cant_identify_while_updating, Toast.LENGTH_LONG).show();
		        }
		    });
			
			return;
		}
		
		log("clear()");
		clear();

		log("setup()");
		setup();

		log("Intent extras");
		Bundle b = intent.getExtras();
		mNewShowId = b.getString("newShowId");
		mOldShowId = b.getString("oldShowId");
		mLanguage = b.getString("language", "all");

		log("setupList()");
		setupList();

		log("removeOldDatabaseEntries()");
		removeOldDatabaseEntries();

		log("start()");
		start();
	}

	private void setupList() {
		Cursor cursor = com.android.leopardmediahd.leopardmediahd.MizuuApplication.getTvShowEpisodeMappingsDbAdapter().getAllFilepaths(mOldShowId);
		com.android.leopardmediahd.functions.ColumnIndexCache cache = new com.android.leopardmediahd.functions.ColumnIndexCache();
		
		try {
			while (cursor.moveToNext())
				mFiles.add(new com.android.leopardmediahd.identification.ShowStructure(
						cursor.getString(cache.getColumnIndex(cursor, com.android.leopardmediahd.db.DbAdapterTvShowEpisodeMappings.KEY_FILEPATH))
						));
		} catch (Exception e) {} finally {
			cursor.close();
			cache.clear();
		}

		// Count all episodes
		for (com.android.leopardmediahd.identification.ShowStructure ss : mFiles) {
			mTotalFiles += ss.getEpisodes().size();
		}
	}

	private void removeOldDatabaseEntries() {
		// This will remove all episode mappings, episodes and the TV show from the database
		boolean result = com.android.leopardmediahd.leopardmediahd.MizuuApplication.getTvShowEpisodeMappingsDbAdapter().deleteAllFilepaths(mOldShowId);
		if (result) {
			// Delete the old TV show thumb and backdrop images
			com.android.leopardmediahd.utils.FileUtils.getTvShowThumb(this, mOldShowId).delete();
			com.android.leopardmediahd.utils.FileUtils.getTvShowBackdrop(this, mOldShowId).delete();

			// Delete season photos
			File[] seasonPhotos = com.android.leopardmediahd.leopardmediahd.MizuuApplication.getTvShowSeasonFolder(this).listFiles();
			for (int i = 0; i < seasonPhotos.length; i++) {
				if (seasonPhotos[i].getName().startsWith(mOldShowId + "_S"))
					seasonPhotos[i].delete();
			}

			// Delete episode photos
			File[] episodePhotos = com.android.leopardmediahd.leopardmediahd.MizuuApplication.getTvShowEpisodeFolder(this).listFiles();
			for (int i = 0; i < episodePhotos.length; i++) {
				if (episodePhotos[i].getName().startsWith(mOldShowId + "_S"))
					episodePhotos[i].delete();
			}
			
			com.android.leopardmediahd.utils.LocalBroadcastUtils.updateTvShowLibrary(this);
		}
	}

	private void start() {
		com.android.leopardmediahd.identification.TvShowIdentification identification = new com.android.leopardmediahd.identification.TvShowIdentification(this, this, mFiles);
		identification.setShowId(mNewShowId);
		identification.setLanguage(mLanguage);
		identification.start();
	}

	/**
	 * Clear all instance variables
	 * since this is an {@link IntentService}.
	 */
	private void clear() {
		mNewShowId = "";
		mOldShowId = "";
		mLanguage = "";
		mFiles = new ArrayList<com.android.leopardmediahd.identification.ShowStructure>();
	}

	private void setup() {
		// Setup up notification
		mBuilder = new NotificationCompat.Builder(getApplicationContext());
        mBuilder.setColor(getResources().getColor(R.color.color_primary));
		mBuilder.setSmallIcon(R.drawable.ic_sync_white_24dp);
		mBuilder.setTicker(getString(R.string.identifying_show));
		mBuilder.setContentTitle(getString(R.string.identifying_show));
		mBuilder.setContentText(getString(R.string.gettingReady));
		mBuilder.setOngoing(true);
		mBuilder.setOnlyAlertOnce(true);

		// Build notification
		Notification updateNotification = mBuilder.build();

		// Show the notification
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(NOTIFICATION_ID, updateNotification);

		// Tell the system that this is an ongoing notification, so it shouldn't be killed
		startForeground(NOTIFICATION_ID, updateNotification);
	}

	@Override
	public void onTvShowAdded(String showId, String title, Bitmap cover, Bitmap backdrop, int count) {
		// We're done!
	}

	@Override
	public void onEpisodeAdded(String showId, String title, Bitmap cover, Bitmap photo) {
		mEpisodeCount++;
		updateEpisodeAddedNotification(showId, title, cover, photo);
	}

	private void updateEpisodeAddedNotification(String showId, String title, Bitmap cover, Bitmap backdrop) {
		String contentText = getString(R.string.stringJustAdded) + ": " + title;

		mBuilder.setLargeIcon(cover);
		mBuilder.setContentTitle(getString(R.string.identifying_show) + " (" + (int) ((100.0 / (double) mTotalFiles) * (double) mEpisodeCount) + "%)");
		mBuilder.setContentText(contentText);
		mBuilder.setStyle(
				new NotificationCompat.BigPictureStyle()
				.setSummaryText(contentText)
				.bigPicture(backdrop)
				);

		// Show the updated notification
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}

	private void log(String msg) {
		if (mDebugging)
			Log.d("IdentifyTvShowService", msg);
	}
}