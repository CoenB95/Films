package com.cbapps.films.movie;

/**
 * @author Coen Boelhouwers
 */

public class TimeParseException extends Exception {

	public TimeParseException(String timeValue, String reason) {
		super("Could not parse time '" + timeValue + "': " + reason);
	}
}
