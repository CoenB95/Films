package com.cbapps.films;

import android.util.Log;

import com.cbapps.films.movie.Movie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Coen Boelhouwers
 */

public class MovieCombiner {

	private static final String TAG = "MovieCombiner";
	private static final Set<String> match = new HashSet<>(Arrays.asList(
			", The", "The", "De"));

	public static void combineMovies(List<Movie> movies) {
		Map<String, Movie> mapping = new HashMap<>();
		ListIterator<Movie> it = movies.listIterator();
		while (it.hasNext()) {
			Movie movie = it.next();
			String key = createMatchString(movie);
			if (mapping.containsKey(key)) {
				Movie origin = mapping.get(key);
				Log.d(TAG, "Found a match: '" + origin.getName() + "' and '" + movie.getName() +
						"'!");
				Log.d(TAG, "Combining " + origin.getTimes().size() + " + " + movie.getTimes().size() +
						" times.");
				origin.addTimes(movie.getTimes());
				Collections.sort(origin.getTimes());
				it.remove();
			} else {
				mapping.put(key, movie);
			}
		}
		movies.clear();
		movies.addAll(mapping.values());
		Collections.sort(movies);
	}

	private static String createMatchString(Movie movie) {
		StringBuilder builder = new StringBuilder();
		List<String> list = Arrays.asList(movie.getName().split("\\s"));
		ListIterator<String> it = list.listIterator();
		while (it.hasNext()) {
			String n = it.next();
			if (!match.contains(n)) builder.append(n);
		}
		return builder.toString();
	}

}
