package com.cbapps.films.movie;

import android.support.annotation.NonNull;

import com.cbapps.films.SimpleTime;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.EnumSet;

/**
 * @author Coen Boelhouwers
 */

public class ScheduledTime implements Comparable<ScheduledTime> {
	private SimpleTime time;
	private boolean active;
	private boolean full;
	private Language language;
	private Projection projection;
	private EnumSet<Extra> extras;

	private ScheduledTime(SimpleTime time, boolean active, boolean full) {
		this.time = time;
		this.active = active;
		this.full = full;
		this.extras = EnumSet.noneOf(Extra.class);
	}

	public void addExtra(Extra extra) {
		extras.add(extra);
	}

	public void addExtras(Collection<? extends Extra> extras) {
		this.extras.addAll(extras);
	}

	public static ScheduledTime fromJson(JSONObject object) throws TimeParseException {
		ScheduledTime time = new ScheduledTime(
				SimpleTime.ofMinutes(object.optInt("time")),
				object.optBoolean("active"),
				object.optBoolean("full"));
		time.setLanguage(Language.valueOf(object.optString("language",
				Language.getDefault().name())));
		time.setProjection(Projection.valueOf(object.optString("projection",
				Projection.getDefault().name())));
		try {
			JSONArray extraArray = object.getJSONArray("extras");
			for (int i = 0; i < extraArray.length(); i++)
				time.addExtra(Extra.valueOf(extraArray.getString(i)));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return time;
	}

	public static ScheduledTime ofString(String value, boolean active, boolean full)
			throws TimeParseException {
		return new ScheduledTime(SimpleTime.parse(value), active, full);
	}

	public static ScheduledTime ofString(String value, boolean active, boolean full,
	                                     Language lang, Projection proj, Collection<Extra> extras)
			throws TimeParseException {
		ScheduledTime time = new ScheduledTime(SimpleTime.parse(value), active, full);
		time.setLanguage(lang);
		time.setProjection(proj);
		time.addExtras(extras);
		return time;
	}

	public String getAttrString() {
		StringBuilder builder = new StringBuilder();
		builder.append(language.toString()).append(' ');
		builder.append(projection.toString());
		for (Extra extra : extras) builder.append(' ').append(extra.toString());
		builder.append(" - ").append(getCinema());
		return builder.toString();
	}

	public String getCinema() {
		return "Euroscoop Tilburg";
	}

	public Language getLanguage() {
		return language;
	}

	public Projection getProjection() {
		return projection;
	}

	public EnumSet<Extra> getExtras() {
		return extras;
	}

	public SimpleTime getTime() {
		return time;
	}

	public boolean isActive() {
		return active;
	}

	public boolean isFull() {
		return full;
	}

	public void setLanguage(Language language) {
		this.language = language;
	}

	public void setProjection(Projection projection) {
		this.projection = projection;
	}

	public JSONObject toJson() throws JSONException {
		JSONObject object = new JSONObject();
		object.put("time", time.toMinutes());
		object.put("active", active);
		object.put("full", full);
		object.put("language", language.name());
		object.put("projection", projection.name());
		JSONArray extraArray = new JSONArray();
		for (Extra extra : extras) extraArray.put(extra.name());
		object.put("extras", extraArray);
		return object;
	}

	@Override
	public String toString() {
		return "ScheduledTime{time=" + time + ", active=" + active + ", full=" + full +
				", language=" + language + ", projection=" + projection +
				", extras=" + extras + "}";
	}

	@Override
	public int compareTo(@NonNull ScheduledTime o) {
		return time.compareTo(o.time);
	}
}
