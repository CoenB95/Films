package com.cbapps.films;

import android.content.Context;
import android.support.annotation.NonNull;

import com.cbapps.films.movie.TimeParseException;

import java.util.Calendar;
import java.util.Locale;

/**
 * @author Coen Boelhouwers
 * @deprecated convert to JodaTime.
 */
@Deprecated
public class SimpleTime implements Comparable<SimpleTime> {
	private byte hour;
	private byte minute;

	private SimpleTime(int hour, int minute) {
		while (minute <= -60) {
			minute += 60;
			hour -= 1;
		}
		while (minute >= 60) {
			minute -= 60;
			hour += 1;
		}
		this.hour = (byte) (hour % 24);
		this.minute = (byte) minute;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof SimpleTime)) return false;
		SimpleTime other = (SimpleTime) obj;
		return compareTo(other) == 0;
	}

	public boolean isAfter(SimpleTime other) {
		return compareTo(other) > 0;
	}

	public boolean isBefore(SimpleTime other) {
		return compareTo(other) < 0;
	}

	public SimpleTime minusMinutes(int value) {
		return plusMinutes(-value);
	}

	public static SimpleTime now() {
		Calendar cal = Calendar.getInstance();
		return new SimpleTime(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
	}

	public static SimpleTime of(int hour, int minute) {
		return new SimpleTime(hour, minute);
	}

	public static SimpleTime ofMinutes(int minutes) {
		return new SimpleTime(0, minutes);
	}

	public static SimpleTime parse(String value) throws TimeParseException {
		int index = value.indexOf(':');
		if (index < 0) throw new TimeParseException(value, "':' missing.");
		try {
			int h = Integer.parseInt(value.substring(0, index));
			int m = Integer.parseInt(value.substring(index + 1, value.length()));
			return new SimpleTime(h, m);
		} catch (NumberFormatException e) {
			throw new TimeParseException(value, e.getMessage());
		}
	}

	public SimpleTime plusHours(int amount) {
		return plusMinutes(amount * 60);
	}

	public SimpleTime plusMinutes(int amount) {
		return new SimpleTime(hour, minute + amount);
	}

	public String getDurationTill(SimpleTime other) {
		int min = getMinutesTill(other);
		int h = min / 60;
		int m = min % 60;
		if (h == 0) return m + " min";
		return h + ":" + m + " min";
	}

	public int getHour() {
		return hour;
	}

	public int getMinute() {
		return minute;
	}

	public int getMinutesTill(SimpleTime other) {
		return other.toMinutes() - toMinutes();
	}

	public int toMinutes() {
		return hour * 60 + minute;
	}

	@Override
	public String toString() {
		return String.format(Locale.US, "%02d:%02d", getHour(), getMinute());
	}

	@Override
	public int compareTo(@NonNull SimpleTime o) {
		return toMinutes() - o.toMinutes();
	}
}
