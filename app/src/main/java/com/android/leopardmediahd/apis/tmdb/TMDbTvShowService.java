package com.android.leopardmediahd.apis.tmdb;

import android.content.Context;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.android.leopardmediahd.apis.trakt.*;
import com.android.leopardmediahd.abstractclasses.TvShowApiService;
import com.android.leopardmediahd.apis.thetvdb.Episode;
import com.android.leopardmediahd.apis.thetvdb.Season;
import com.android.leopardmediahd.apis.thetvdb.TvShow;
import com.android.leopardmediahd.apis.trakt.Trakt;
import com.android.leopardmediahd.functions.Actor;
import com.android.leopardmediahd.functions.MizLib;
import com.android.leopardmediahd.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.android.leopardmediahd.functions.PreferenceKeys.TVSHOWS_RATINGS_SOURCE;

public class TMDbTvShowService extends com.android.leopardmediahd.abstractclasses.TvShowApiService {

	private static TMDbTvShowService mService;
	
	private final String mTmdbApiKey;
	private final Context mContext;

	public static TMDbTvShowService getInstance(Context context) {
		if (mService == null)
			mService = new TMDbTvShowService(context);
		return mService;
	}
	
	private TMDbTvShowService(Context context) {
		mContext = context;
		mTmdbApiKey = com.android.leopardmediahd.functions.MizLib.getTmdbApiKey(mContext);
	}

    /**
     * Get the ratings provider. This isn't a static value, so it should be reloaded when needed.
     * @return
     */
    public String getRatingsProvider() {
        return PreferenceManager.getDefaultSharedPreferences(mContext).getString(TVSHOWS_RATINGS_SOURCE, mContext.getString(R.string.ratings_option_4));
    }

	@Override
	public List<com.android.leopardmediahd.apis.thetvdb.TvShow> search(String query, String language) {
		language = getLanguage(language);

		String serviceUrl = "";

		try {
			serviceUrl = "https://api.themoviedb.org/3/search/tv?query=" + URLEncoder.encode(query, "utf-8") + "&language=" + language + "&api_key=" + mTmdbApiKey;
		} catch (UnsupportedEncodingException e) {}

		return getListFromUrl(serviceUrl);
	}

	@Override
	public List<com.android.leopardmediahd.apis.thetvdb.TvShow> search(String query, String year, String language) {
		language = getLanguage(language);

		String serviceUrl = "";

		try {
			serviceUrl = "https://api.themoviedb.org/3/search/tv?query=" + URLEncoder.encode(query, "utf-8") + "&language=" + language + "&first_air_date_year=" + year + "&api_key=" + mTmdbApiKey;
		} catch (UnsupportedEncodingException e) {}

		return getListFromUrl(serviceUrl);
	}

	@Override
	public List<com.android.leopardmediahd.apis.thetvdb.TvShow> searchByImdbId(String imdbId, String language) {
		language = getLanguage(language);

		ArrayList<com.android.leopardmediahd.apis.thetvdb.TvShow> results = new ArrayList<com.android.leopardmediahd.apis.thetvdb.TvShow>();

		try {
			JSONObject jObject = com.android.leopardmediahd.functions.MizLib.getJSONObject(mContext, "https://api.themoviedb.org/3/find/" + imdbId + "?language=" + language + "&external_source=imdb_id&api_key=" + mTmdbApiKey);
			JSONArray array = jObject.getJSONArray("tv_results");

			String baseUrl = com.android.leopardmediahd.functions.MizLib.getTmdbImageBaseUrl(mContext);
			String imageSizeUrl = com.android.leopardmediahd.functions.MizLib.getImageUrlSize(mContext);

			for (int i = 0; i < array.length(); i++) {
				com.android.leopardmediahd.apis.thetvdb.TvShow show = new com.android.leopardmediahd.apis.thetvdb.TvShow();
				show.setTitle(array.getJSONObject(i).getString("name"));
				show.setOriginalTitle(array.getJSONObject(i).getString("original_name"));
				show.setFirstAired(array.getJSONObject(i).getString("first_air_date"));
				show.setDescription(""); // TMDb doesn't support descriptions in search results
				show.setRating(String.valueOf(array.getJSONObject(i).getDouble("vote_average")));
				show.setId(String.valueOf(array.getJSONObject(i).getInt("id")));
				show.setCoverUrl(baseUrl + imageSizeUrl + array.getJSONObject(i).getString("poster_path"));
				results.add(show);
			}
		} catch (JSONException e) {}

		return results;
	}

	@Override
	public com.android.leopardmediahd.apis.thetvdb.TvShow get(String id, String language) {
		language = getLanguage(language);

		com.android.leopardmediahd.apis.thetvdb.TvShow show = new com.android.leopardmediahd.apis.thetvdb.TvShow();
		show.setId("tmdb_" + id); // this is a hack to store the TMDb ID for the show in the database without a separate column for it

		String baseUrl = com.android.leopardmediahd.functions.MizLib.getTmdbImageBaseUrl(mContext);

		JSONObject jObject = com.android.leopardmediahd.functions.MizLib.getJSONObject(mContext, "https://api.themoviedb.org/3/tv/" + id + "?api_key=" + mTmdbApiKey + "&language=" + language + "&append_to_response=credits,images,external_ids");

		// Set title
		show.setTitle(com.android.leopardmediahd.functions.MizLib.getStringFromJSONObject(jObject, "name", ""));

		// Set description
		show.setDescription(com.android.leopardmediahd.functions.MizLib.getStringFromJSONObject(jObject, "overview", ""));

		if (!language.equals("en")) { // This is a localized search - let's fill in the blanks
			JSONObject englishResults = com.android.leopardmediahd.functions.MizLib.getJSONObject(mContext, "https://api.themoviedb.org/3/tv/" + id + "?api_key=" + mTmdbApiKey + "&language=en");

			if (TextUtils.isEmpty(show.getTitle()))
				show.setTitle(com.android.leopardmediahd.functions.MizLib.getStringFromJSONObject(englishResults, "name", ""));

			if (TextUtils.isEmpty(show.getDescription()))
				show.setDescription(com.android.leopardmediahd.functions.MizLib.getStringFromJSONObject(englishResults, "overview", ""));
		}

		// Set actors
		try {
			StringBuilder actors = new StringBuilder();

			JSONArray array = jObject.getJSONObject("credits").getJSONArray("cast");
			for (int i = 0; i < array.length(); i++) {
				actors.append(array.getJSONObject(i).getString("name"));
				actors.append("|");
			}

			show.setActors(actors.toString());
		} catch (Exception e) {}

		// Set genres
		try {
			String genres = "";
			for (int i = 0; i < jObject.getJSONArray("genres").length(); i++)
				genres = genres + jObject.getJSONArray("genres").getJSONObject(i).getString("name") + ", ";
			show.setGenres(genres.substring(0, genres.length() - 2));
		} catch (Exception e) {}

		// Set rating
		show.setRating(com.android.leopardmediahd.functions.MizLib.getStringFromJSONObject(jObject, "vote_average", "0.0"));

		// Set cover path
		show.setCoverUrl(baseUrl + com.android.leopardmediahd.functions.MizLib.getImageUrlSize(mContext) + com.android.leopardmediahd.functions.MizLib.getStringFromJSONObject(jObject, "poster_path", ""));

		// Set backdrop path
		show.setBackdropUrl(baseUrl + com.android.leopardmediahd.functions.MizLib.getBackdropUrlSize(mContext) + com.android.leopardmediahd.functions.MizLib.getStringFromJSONObject(jObject, "backdrop_path", ""));

		// Set certification - not available with TMDb
		show.setCertification("");

		try {
			// Set runtime
			show.setRuntime(String.valueOf(jObject.getJSONArray("episode_run_time").getInt(0)));
		} catch (JSONException e) {}

		// Set first aired date
		show.setFirstAired(com.android.leopardmediahd.functions.MizLib.getStringFromJSONObject(jObject, "first_air_date", ""));

		try {
			// Set IMDb ID
			show.setIMDbId(jObject.getJSONObject("external_ids").getString("imdb_id"));
		} catch (JSONException e) {}

		// Trakt.tv
		if (getRatingsProvider().equals(mContext.getString(R.string.ratings_option_2))) {
			try {
				com.android.leopardmediahd.apis.trakt.Movie movieSummary = com.android.leopardmediahd.apis.trakt.Trakt.getMovieSummary(mContext, id);
				double rating = (double) (movieSummary.getRating() / 10);

				if (rating > 0 || show.getRating().equals("0.0"))
					show.setRating(String.valueOf(rating));	
			} catch (Exception e) {}
		}

		// OMDb API / IMDb
		if (getRatingsProvider().equals(mContext.getString(R.string.ratings_option_3))) {
			try {
				jObject = com.android.leopardmediahd.functions.MizLib.getJSONObject(mContext, "http://www.omdbapi.com/?i=" + show.getImdbId());
				double rating = Double.valueOf(com.android.leopardmediahd.functions.MizLib.getStringFromJSONObject(jObject, "imdbRating", "0"));

				if (rating > 0 || show.getRating().equals("0.0"))
					show.setRating(String.valueOf(rating));	
			} catch (Exception e) {}
		}

		// Seasons
		try {
			JSONArray seasons = jObject.getJSONArray("seasons");

			for (int i = 0; i < seasons.length(); i++) {
				com.android.leopardmediahd.apis.thetvdb.Season s = new com.android.leopardmediahd.apis.thetvdb.Season();

				s.setSeason(seasons.getJSONObject(i).getInt("season_number"));
				s.setCoverPath(baseUrl + com.android.leopardmediahd.functions.MizLib.getImageUrlSize(mContext) + com.android.leopardmediahd.functions.MizLib.getStringFromJSONObject(seasons.getJSONObject(i), "poster_path", ""));

				show.addSeason(s);
			}
		} catch (JSONException e) {}

		// Episode details
		for (com.android.leopardmediahd.apis.thetvdb.Season s : show.getSeasons()) {
			jObject = com.android.leopardmediahd.functions.MizLib.getJSONObject(mContext, "https://api.themoviedb.org/3/tv/" + id + "/season/" + s.getSeason() + "?api_key=" + mTmdbApiKey);
			try {
				JSONArray episodes = jObject.getJSONArray("episodes");
				for (int i = 0; i < episodes.length(); i++) {
					com.android.leopardmediahd.apis.thetvdb.Episode ep = new com.android.leopardmediahd.apis.thetvdb.Episode();
					ep.setSeason(s.getSeason());
					ep.setEpisode(episodes.getJSONObject(i).getInt("episode_number"));
					ep.setTitle(episodes.getJSONObject(i).getString("name"));
					ep.setAirdate(episodes.getJSONObject(i).getString("air_date"));
					ep.setDescription(episodes.getJSONObject(i).getString("overview"));
					ep.setRating(com.android.leopardmediahd.functions.MizLib.getStringFromJSONObject(episodes.getJSONObject(i), "vote_average", "0.0"));

					try {
						// This is quite nasty... An HTTP call for each episode, yuck!
						// Sadly, this is needed in order to get proper screenshot URLS
						// and info about director, writer and guest stars
						JSONObject episodeCall = com.android.leopardmediahd.functions.MizLib.getJSONObject(mContext, "https://api.themoviedb.org/3/tv/" + id + "/season/" + s.getSeason() + "/episode/" + ep.getEpisode() + "?api_key=" + mTmdbApiKey + "&append_to_response=credits,images");

						// Screenshot URL in the correct size
						JSONArray images = episodeCall.getJSONObject("images").getJSONArray("stills");
						if (images.length() > 0) {
							JSONObject firstImage = images.getJSONObject(0);
							int width = firstImage.getInt("width");
							if (width < 500) {
								ep.setScreenshotUrl(baseUrl + "original" + com.android.leopardmediahd.functions.MizLib.getStringFromJSONObject(firstImage, "file_path", ""));
							} else {
								ep.setScreenshotUrl(baseUrl + com.android.leopardmediahd.functions.MizLib.getBackdropThumbUrlSize(mContext) + com.android.leopardmediahd.functions.MizLib.getStringFromJSONObject(firstImage, "file_path", ""));
							}
						}

						try {
							// Guest stars
							StringBuilder actors = new StringBuilder();
							JSONArray guest_stars = episodeCall.getJSONObject("credits").getJSONArray("guest_stars");

							for (int j = 0; j < guest_stars.length(); j++) {
								actors.append(guest_stars.getJSONObject(j).getString("name"));
								actors.append("|");
							}

							ep.setGueststars(actors.toString());
						} catch (Exception e) {}

						try {
							// Crew information
							StringBuilder director = new StringBuilder(), writer = new StringBuilder();
							JSONArray crew = episodeCall.getJSONObject("credits").getJSONArray("crew");

							for (int j = 0; j < crew.length(); j++) {
								if (crew.getJSONObject(j).getString("job").equals("Director")) {
									director.append(crew.getJSONObject(j).getString("name"));
									director.append("|");
								} else if (crew.getJSONObject(j).getString("job").equals("Writer")) {
									writer.append(crew.getJSONObject(j).getString("name"));
									writer.append("|");
								}
							}

							ep.setDirector(director.toString());
							ep.setWriter(writer.toString());
							
						} catch (Exception e) {}

					} catch (Exception e) {}

					show.addEpisode(ep);
				}
			} catch (JSONException e) {}
		}

		return show;
	}

	private ArrayList<com.android.leopardmediahd.apis.thetvdb.TvShow> getListFromUrl(String serviceUrl) {
		ArrayList<com.android.leopardmediahd.apis.thetvdb.TvShow> results = new ArrayList<com.android.leopardmediahd.apis.thetvdb.TvShow>();

		try {
			JSONObject jObject = com.android.leopardmediahd.functions.MizLib.getJSONObject(mContext, serviceUrl);
			JSONArray array = jObject.getJSONArray("results");

			String baseUrl = com.android.leopardmediahd.functions.MizLib.getTmdbImageBaseUrl(mContext);
			String imageSizeUrl = com.android.leopardmediahd.functions.MizLib.getImageUrlSize(mContext);

			for (int i = 0; i < array.length(); i++) {
				com.android.leopardmediahd.apis.thetvdb.TvShow show = new com.android.leopardmediahd.apis.thetvdb.TvShow();
				show.setTitle(array.getJSONObject(i).getString("name"));
				show.setOriginalTitle(array.getJSONObject(i).getString("original_name"));
				show.setFirstAired(array.getJSONObject(i).getString("first_air_date"));
				show.setDescription(""); // TMDb doesn't support descriptions in search results
				show.setRating(String.valueOf(array.getJSONObject(i).getDouble("vote_average")));
				show.setId(String.valueOf(array.getJSONObject(i).getInt("id")));
				show.setCoverUrl(baseUrl + imageSizeUrl + array.getJSONObject(i).getString("poster_path"));
				results.add(show);
			}
		} catch (JSONException e) {}

		return results;
	}

	@Override
	public List<String> getCovers(String id) {
		ArrayList<String> covers = new ArrayList<String>();
		String baseUrl = com.android.leopardmediahd.functions.MizLib.getTmdbImageBaseUrl(mContext);
		
		try {
			JSONObject jObject = com.android.leopardmediahd.functions.MizLib.getJSONObject(mContext, "https://api.themoviedb.org/3/tv/" + id + "/images" + "?api_key=" + mTmdbApiKey);
			JSONArray jArray = jObject.getJSONArray("posters");
			for (int i = 0; i < jArray.length(); i++) {
				covers.add(baseUrl + com.android.leopardmediahd.functions.MizLib.getImageUrlSize(mContext) + com.android.leopardmediahd.functions.MizLib.getStringFromJSONObject(jArray.getJSONObject(i), "file_path", ""));
			}
		} catch (JSONException e) {}

		return covers;
	}

	@Override
	public List<String> getBackdrops(String id) {
		ArrayList<String> covers = new ArrayList<String>();
		String baseUrl = com.android.leopardmediahd.functions.MizLib.getTmdbImageBaseUrl(mContext);
		
		try {
			JSONObject jObject = com.android.leopardmediahd.functions.MizLib.getJSONObject(mContext, "https://api.themoviedb.org/3/tv/" + id + "/images" + "?api_key=" + mTmdbApiKey);
			JSONArray jArray = jObject.getJSONArray("backdrops");
			for (int i = 0; i < jArray.length(); i++) {
				covers.add(baseUrl + com.android.leopardmediahd.functions.MizLib.getBackdropThumbUrlSize(mContext) + com.android.leopardmediahd.functions.MizLib.getStringFromJSONObject(jArray.getJSONObject(i), "file_path", ""));
			}
		} catch (JSONException e) {}

		return covers;
	}
	
	/**
	 * Get the language code or a default one if
	 * the supplied one is empty or {@link null}.
	 * @param language
	 * @return Language code
	 */
	@Override
	public String getLanguage(String language) {
		if (TextUtils.isEmpty(language))
			language = "en";
		return language;
	}

	@Override
	public List<com.android.leopardmediahd.apis.thetvdb.TvShow> searchNgram(String query, String language) {
		language = getLanguage(language);

		String serviceUrl = "";

		try {
			serviceUrl = "https://api.themoviedb.org/3/search/tv?query=" + URLEncoder.encode(query, "utf-8") + "&language=" + language + "&search_type=ngram&api_key=" + mTmdbApiKey;
		} catch (UnsupportedEncodingException e) {}

		return getListFromUrl(serviceUrl);
	}

	@Override
	public List<com.android.leopardmediahd.functions.Actor> getActors(String id) {
		ArrayList<com.android.leopardmediahd.functions.Actor> results = new ArrayList<com.android.leopardmediahd.functions.Actor>();

		String baseUrl = com.android.leopardmediahd.functions.MizLib.getTmdbImageBaseUrl(mContext);

		try {
			JSONObject jObject;
			
			if (!id.startsWith("tmdb_")) {
				jObject = com.android.leopardmediahd.functions.MizLib.getJSONObject(mContext, "https://api.themoviedb.org/3/find/" + id + "?api_key=" + mTmdbApiKey + "&external_source=tvdb_id");
				id = com.android.leopardmediahd.functions.MizLib.getStringFromJSONObject(jObject.getJSONArray("tv_results").getJSONObject(0), "id", "");
			} else {
				id = id.replace("tmdb_", "");
			}
			
			jObject = com.android.leopardmediahd.functions.MizLib.getJSONObject(mContext, "https://api.themoviedb.org/3/tv/" + id + "/credits?api_key=" + mTmdbApiKey);
			JSONArray jArray = jObject.getJSONArray("cast");

			Set<String> actorIds = new HashSet<String>();

			for (int i = 0; i < jArray.length(); i++) {
				if (!actorIds.contains(jArray.getJSONObject(i).getString("id"))) {
					actorIds.add(jArray.getJSONObject(i).getString("id"));

					results.add(new com.android.leopardmediahd.functions.Actor(
							jArray.getJSONObject(i).getString("name"),
							jArray.getJSONObject(i).getString("character"),
							jArray.getJSONObject(i).getString("id"),
							baseUrl + com.android.leopardmediahd.functions.MizLib.getActorUrlSize(mContext) + jArray.getJSONObject(i).getString("profile_path")));
				}
			}
		} catch (Exception ignored) {}

		return results;
	}
}