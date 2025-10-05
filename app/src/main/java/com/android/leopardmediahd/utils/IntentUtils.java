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

package com.android.leopardmediahd.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.android.leopardmediahd.leopardmediahd.ActorBrowser;
import com.android.leopardmediahd.leopardmediahd.ActorBrowserTv;
import com.android.leopardmediahd.leopardmediahd.ActorDetails;
import com.android.leopardmediahd.leopardmediahd.ActorMovies;
import com.android.leopardmediahd.leopardmediahd.ActorPhotos;
import com.android.leopardmediahd.leopardmediahd.ActorTaggedPhotos;
import com.android.leopardmediahd.leopardmediahd.ActorTvShows;
import com.android.leopardmediahd.leopardmediahd.ImageViewer;
import com.android.leopardmediahd.leopardmediahd.MizuuApplication;
import com.android.leopardmediahd.leopardmediahd.MovieDetails;
import com.android.leopardmediahd.leopardmediahd.SimilarMovies;
import com.android.leopardmediahd.leopardmediahd.TMDbMovieDetails;
import com.android.leopardmediahd.leopardmediahd.TvShowDetails;
import com.android.leopardmediahd.leopardmediahd.TvShowEpisodes;
import com.android.leopardmediahd.leopardmediahd.TvShowSeasons;

import java.util.List;

public class IntentUtils {

	private IntentUtils() {} // No instantiation
	
	/**
	 * Intent for actor details.
	 * @param context
	 * @param name
	 * @param id
	 * @param thumbnail
	 * @return
	 */
	public static Intent getActorIntent(Context context, String name, String id, String thumbnail) {
		Intent actorIntent = new Intent(context, com.android.leopardmediahd.leopardmediahd.ActorDetails.class);
		actorIntent.putExtra("actorName", name);
		actorIntent.putExtra("actorID", id);
		actorIntent.putExtra("thumb", thumbnail);
		return actorIntent;
	}
	
	/**
	 * Intent for actor details. Uses the supplied {@link com.android.leopardmediahd.functions.Actor} object to get details.
	 * @param context
	 * @param actor
	 * @return
	 */
	public static Intent getActorIntent(Context context, com.android.leopardmediahd.functions.Actor actor) {
		return getActorIntent(context, actor.getName(), actor.getId(), actor.getUrl());
	}
	
	/**
	 * Intent for the movie actor browser.
	 * @param context
	 * @param title
	 * @param movieId
	 * @return
	 */
	public static Intent getActorBrowserMovies(Context context, String title, String movieId, int toolbarColor) {
		Intent actorIntent = new Intent(context, com.android.leopardmediahd.leopardmediahd.ActorBrowser.class);
		actorIntent.putExtra("title", title);
		actorIntent.putExtra("movieId", movieId);
        actorIntent.putExtra(com.android.leopardmediahd.functions.IntentKeys.TOOLBAR_COLOR, toolbarColor);
		return actorIntent;
	}
	
	/**
	 * Intent for the TV show actor browser.
	 * @param context
	 * @param title
	 * @param showId
	 * @return
	 */
	public static Intent getActorBrowserTvShows(Context context, String title, String showId, int toolbarColor) {
		Intent actorIntent = new Intent(context, com.android.leopardmediahd.leopardmediahd.ActorBrowserTv.class);
		actorIntent.putExtra("title", title);
		actorIntent.putExtra("showId", showId);
        actorIntent.putExtra(com.android.leopardmediahd.functions.IntentKeys.TOOLBAR_COLOR, toolbarColor);
		return actorIntent;
	}
	
	/**
	 * Intent for the similar movies browser.
	 * @param context
	 * @param title
	 * @param movieId
	 * @return
	 */
	public static Intent getSimilarMovies(Context context, String title, String movieId, int toolbarColor) {
		Intent similarMoviesIntent = new Intent(context, com.android.leopardmediahd.leopardmediahd.SimilarMovies.class);
		similarMoviesIntent.putExtra("title", title);
		similarMoviesIntent.putExtra("movieId", movieId);
        similarMoviesIntent.putExtra(com.android.leopardmediahd.functions.IntentKeys.TOOLBAR_COLOR, toolbarColor);
		return similarMoviesIntent;
	}
	
	public static Intent getTmdbMovieDetails(Context context, com.android.leopardmediahd.functions.WebMovie movie) {
		Intent movieDetailsIntent = new Intent(context, movie.isInLibrary() ? com.android.leopardmediahd.leopardmediahd.MovieDetails.class : com.android.leopardmediahd.leopardmediahd.TMDbMovieDetails.class);
		movieDetailsIntent.putExtra("tmdbId", movie.getId());
		movieDetailsIntent.putExtra("title", movie.getTitle());
		return movieDetailsIntent;
	}
	
	public static Intent getTmdbTvShowLink(Context context, com.android.leopardmediahd.functions.WebMovie show) {
		Intent showIntent;

        if (show.isInLibrary() && !TextUtils.isEmpty(com.android.leopardmediahd.leopardmediahd.MizuuApplication.getTvDbAdapter().getShowId(show.getTitle()))) {
            showIntent = new Intent(context, com.android.leopardmediahd.leopardmediahd.TvShowDetails.class);
            showIntent.putExtra("showId", com.android.leopardmediahd.leopardmediahd.MizuuApplication.getTvDbAdapter().getShowId(show.getTitle()));
        } else {
            showIntent = new Intent(Intent.ACTION_VIEW);
            showIntent.setData(Uri.parse("http://www.themoviedb.org/tv/" + show.getId()));
        }

		return showIntent;
	}

	public static Intent getActorPhotoIntent(Context context, List<String> photos, int selectedIndex) {	
		String[] array = new String[photos.size()];
		for (int i = 0; i < photos.size(); i++)
			array[i] = photos.get(i).replace(com.android.leopardmediahd.functions.MizLib.getActorUrlSize(context), "original");
		
		Intent intent = new Intent(context, com.android.leopardmediahd.leopardmediahd.ImageViewer.class);
		intent.putExtra("photos", array);
		intent.putExtra("selectedIndex", selectedIndex);

		return intent;
	}
	
	public static Intent getActorTaggedPhotoIntent(Context context, List<String> photos, int selectedIndex) {	
		String[] array = new String[photos.size()];
		for (int i = 0; i < photos.size(); i++)
			array[i] = photos.get(i).replace(com.android.leopardmediahd.functions.MizLib.getBackdropThumbUrlSize(context), "original");
		
		Intent intent = new Intent(context, com.android.leopardmediahd.leopardmediahd.ImageViewer.class);
		intent.putExtra("photos", array);
		intent.putExtra("portraitPhotos", false);
		intent.putExtra("selectedIndex", selectedIndex);

		return intent;
	}
	
	public static Intent getActorMoviesIntent(Context context, String actorName, String actorId, int toolbarColor) {
		Intent actorMoviesIntent = new Intent(context, com.android.leopardmediahd.leopardmediahd.ActorMovies.class);
		actorMoviesIntent.putExtra("actorName", actorName);
		actorMoviesIntent.putExtra("actorId", actorId);
        actorMoviesIntent.putExtra(com.android.leopardmediahd.functions.IntentKeys.TOOLBAR_COLOR, toolbarColor);
		return actorMoviesIntent;
	}
	
	public static Intent getActorTvShowsIntent(Context context, String actorName, String actorId, int toolbarColor) {
		Intent actorTvShowIntent = new Intent(context, com.android.leopardmediahd.leopardmediahd.ActorTvShows.class);
		actorTvShowIntent.putExtra("actorName", actorName);
		actorTvShowIntent.putExtra("actorId", actorId);
        actorTvShowIntent.putExtra(com.android.leopardmediahd.functions.IntentKeys.TOOLBAR_COLOR, toolbarColor);
		return actorTvShowIntent;
	}
	
	public static Intent getActorPhotosIntent(Context context, String actorName, String actorId, int toolbarColor) {
		Intent actorPhotosIntent = new Intent(context, com.android.leopardmediahd.leopardmediahd.ActorPhotos.class);
		actorPhotosIntent.putExtra("actorName", actorName);
		actorPhotosIntent.putExtra("actorId", actorId);
        actorPhotosIntent.putExtra(com.android.leopardmediahd.functions.IntentKeys.TOOLBAR_COLOR, toolbarColor);
		return actorPhotosIntent;
	}
	
	public static Intent getActorTaggedPhotosIntent(Context context, String actorName, String actorId, int toolbarColor) {
		Intent actorTaggedPhotosIntent = new Intent(context, com.android.leopardmediahd.leopardmediahd.ActorTaggedPhotos.class);
		actorTaggedPhotosIntent.putExtra("actorName", actorName);
		actorTaggedPhotosIntent.putExtra("actorId", actorId);
        actorTaggedPhotosIntent.putExtra(com.android.leopardmediahd.functions.IntentKeys.TOOLBAR_COLOR, toolbarColor);
		return actorTaggedPhotosIntent;
	}

    public static Intent getTvShowSeasonsIntent(Context context, String title, String showId, int toolbarColor) {
        Intent seasonsIntent = new Intent(context, com.android.leopardmediahd.leopardmediahd.TvShowSeasons.class);
        seasonsIntent.putExtra("showTitle", title);
        seasonsIntent.putExtra("showId", showId);
        seasonsIntent.putExtra(com.android.leopardmediahd.functions.IntentKeys.TOOLBAR_COLOR, toolbarColor);
        return seasonsIntent;
    }

    public static Intent getTvShowSeasonIntent(Context context, String showId, int season, int episodeCount, int toolbarColor) {
        Intent seasonIntent = new Intent(context, com.android.leopardmediahd.leopardmediahd.TvShowEpisodes.class);
        seasonIntent.putExtra("showId", showId);
        seasonIntent.putExtra("season", season);
        seasonIntent.putExtra("episodeCount", episodeCount);
        seasonIntent.putExtra(com.android.leopardmediahd.functions.IntentKeys.TOOLBAR_COLOR, toolbarColor);
        return seasonIntent;
    }
}