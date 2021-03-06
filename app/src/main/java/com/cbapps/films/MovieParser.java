package com.cbapps.films;

import android.util.Log;

import com.cbapps.films.movie.Extra;
import com.cbapps.films.movie.Language;
import com.cbapps.films.movie.Movie;
import com.cbapps.films.movie.Projection;
import com.cbapps.films.movie.ScheduledTime;
import com.cbapps.films.movie.TimeParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * @author Coen Boelhouwers
 */

public class MovieParser {

	private static final String TAG = "MovieParser";
	private static final int VERSION = 3;

	public static List<Movie> loadFromFile(File cacheFile) {
		List<Movie> movies = new ArrayList<>();
		try {
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new FileReader(cacheFile));
			String line;
			while ((line = reader.readLine()) != null) builder.append(line);
			JSONObject movieObject = new JSONObject(builder.toString());
			int v = movieObject.optInt("version", 0);
			if (v != VERSION) {
				Log.w(TAG, "Version mismatch: expected " + VERSION + " but found " + v);
				Log.w(TAG, "Skip loading, return empty list.");
				return movies;
			}
			JSONArray movieArray = movieObject.getJSONArray("movies");
			for (int i = 0; i < movieArray.length(); i++) {
				movies.add(Movie.fromJson(movieArray.getJSONObject(i)));
			}
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}
		return movies;
	}

	private static List<Movie> loadEuroscoop(int dayCode) {
		List<Movie> result = new ArrayList<>();
		try {
			URLConnection conn = new URL("https:www.euroscoop.nl/tilburg/system/" +
					"cinema-performances/?id=1&offset=" + dayCode).openConnection();
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder builder = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			result = parseJson(builder.toString(), dayCode);//data.substring(20, data.length()));
			for (Movie movie : result) {
				Log.d(TAG, "Parsed movie: " + movie.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static List<Movie> loadFromNetwork() {
		List<Movie> res = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			res.addAll(loadEuroscoop(i));
		}
		return res;
	}

    private static List<Movie> parseJson(String data, int offsetDays) {
	    List<Movie> result = new ArrayList<>();
	    try {
		    JSONArray array = new JSONArray(data);
		    for (int i = 0; i < array.length(); i++) {
			    JSONObject obj = array.getJSONObject(i);
			    Movie movie = new Movie(obj.optString("name"));
			    Language lang = Language.apply(movie);
			    Projection proj = Projection.apply(movie);
			    EnumSet<Extra> extras = Extra.apply(movie);
			    movie.setId(obj.optInt("id"));
			    JSONArray timeslots = obj.getJSONArray("timeslots");
			    JSONArray active = obj.getJSONArray("timeslots");
			    JSONArray full = obj.getJSONArray("timeslots");
			    for (int i2 = 0; i2 < timeslots.length(); i2++) {
				    try {
					    movie.addTime(ScheduledTime.ofString(
					    		timeslots.optString(i2),
							    offsetDays,
							    active.optBoolean(i2),
							    full.optBoolean(i2),
							    lang, proj, extras));
				    } catch (TimeParseException e) {
					    e.printStackTrace();
				    }
			    }
			    result.add(movie);
		    }
	    } catch (JSONException e) {
		    e.printStackTrace();
	    }
	    return result;
    }

    public static boolean saveToFile(List<Movie> movies, File file) {
	    try {
		    JSONObject object = new JSONObject();
		    object.put("version", VERSION);
		    JSONArray movieArray = new JSONArray();
		    for (Movie movie : movies) movieArray.put(movie.toJson());
		    object.put("movies", movieArray);

		    FileWriter writer = new FileWriter(file);
		    String json = object.toString(2);
		    writer.write(json);
		    writer.close();
		    return true;
	    } catch (JSONException | IOException e) {
		    e.printStackTrace();
	    }
	    return false;
    }
}
