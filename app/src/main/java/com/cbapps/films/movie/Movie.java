package com.cbapps.films.movie;

import android.support.annotation.NonNull;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import com.cbapps.films.SimpleTime;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * @author Coen Boelhouwers
 */

public class Movie implements Comparable<Movie> {

	private static final String TAG = "Movie";

	private String name;
	private int id;
	private String suburl;
	private EnumSet<Classification> classifications;
	private List<ScheduledTime> times;

	public Movie(String name) {
		this.name = name;
		this.times = new ArrayList<>();
		this.classifications = EnumSet.noneOf(Classification.class);
	}

	public void addTime(ScheduledTime t) {
		times.add(t);
	}

	public void addTimes(Collection<ScheduledTime> times) {
		this.times.addAll(times);
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

	public EnumSet<Extra> getExtras() {
		EnumSet<Extra> extras = EnumSet.noneOf(Extra.class);
		for (ScheduledTime time : times) extras.addAll(time.getExtras());
		return extras;
	}

	public EnumSet<Language> getLanguages() {
		EnumSet<Language> langs = EnumSet.noneOf(Language.class);
		for (ScheduledTime time : times) langs.add(time.getLanguage());
		return langs;
	}

	public String getName() {
		return name;
	}

	public ScheduledTime getNextTime(SimpleTime now) {
		Collections.sort(times);
		for (ScheduledTime time : times) {
			if (time.getTime().isAfter(now)) return time;
		}
		return null;
	}

	public EnumSet<Projection> getProjections() {
		EnumSet<Projection> projs = EnumSet.noneOf(Projection.class);
		for (ScheduledTime time : times) projs.add(time.getProjection());
		return projs;
	}

	public List<ScheduledTime> getTimes() {
		return times;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSuburl(String suburl) {
		this.suburl = suburl;
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
		for (ScheduledTime time : times) timeArray.put(time.toJson());
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
