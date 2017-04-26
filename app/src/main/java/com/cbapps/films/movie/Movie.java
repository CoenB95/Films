package com.cbapps.films.movie;

import android.support.annotation.NonNull;
import android.util.Log;

import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Coen Boelhouwers
 */

public class Movie implements Comparable<Movie> {

	private static final String TAG = "Movie";

	private String name;
	private int id;
	private EnumSet<Classification> classifications;
	private Map<LocalDate, List<ScheduledTime>> times;

	public Movie(String name) {
		this.name = name;
		this.times = new HashMap<>();
		this.classifications = EnumSet.noneOf(Classification.class);
	}

	public void addTime(ScheduledTime t) {
		LocalDate key = t.getTime().toLocalDate();
		if (times.containsKey(key)) {
			times.get(key).add(t);
		} else {
			List<ScheduledTime> list = new ArrayList<>();
			list.add(t);
			times.put(key, list);
		}
	}

	public void addTimes(Collection<ScheduledTime> times) {
		for (ScheduledTime t : times) addTime(t);
	}

	public static Movie fromJson(JSONObject object) {
		Movie movie = new Movie(object.optString("name"));
		movie.setId(object.optInt("id"));
		JSONArray timesArray = object.optJSONArray("times");
		if (timesArray != null) {
			for (int i = 0; i < timesArray.length(); i++) {
				try {
					movie.addTime(ScheduledTime.fromJson(timesArray.optJSONObject(i)));
				} catch (TimeParseException e) {
					Log.w(TAG, "Could not parse some time: " + e.getMessage());
				}
			}
		}
		return movie;
	}

	public List<ScheduledTime> getAllTimes() {
		List<ScheduledTime> result = new ArrayList<>();
		for (List<ScheduledTime> list : times.values()) result.addAll(list);
		return result;
	}

	public String getName() {
		return name;
	}

	public Map<LocalDate, List<ScheduledTime>> getTimes() {
		return times;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public JSONObject toJson() throws JSONException {
		JSONObject object = new JSONObject();
		object.put("name", name);
		object.put("id", id);

		// Classifications
		JSONArray classArray = new JSONArray();
		for (Classification classification : classifications) classArray.put(classification.name());
		object.put("classifications", classArray);

		// Times
		JSONArray timeArray = new JSONArray();
		for (List<ScheduledTime> tims : times.values())
			for (ScheduledTime time : tims) timeArray.put(time.toJson());
		object.put("times", timeArray);

		return object;
	}

	@Override
	public String toString() {
		return "Movie{name=" + name + ", id=" + id + ", classifications=" + classifications +
				", times=" + times + "}";
	}

	@Override
	public int compareTo(@NonNull Movie o) {
		return name.compareTo(o.name);
	}
}
