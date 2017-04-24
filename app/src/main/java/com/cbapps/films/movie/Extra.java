package com.cbapps.films.movie;

import java.util.EnumSet;

/**
 * @author Coen Boelhouwers
 */

public enum Extra {
	ATMOS(" Dolby Atmos");

	private String[] slogans;

	Extra(String... slogans) {
		this.slogans = slogans;
	}

	/**
	 * Parses the movie's title to search for strings indicating the language
	 * of this movie. Once found the specific string(s) will be removed from the title,
	 * cleaning it up on the go.
	 * @param movie the Movie which needs its title to be parsed.
	 */
	public static EnumSet<Extra> apply(Movie movie) {
		EnumSet<Extra> result = EnumSet.noneOf(Extra.class);
		for (Extra e : Extra.values()) {
			for (String s : e.slogans) {
				int index = movie.getName().indexOf(s);
				if (index >= 0) {
					movie.setName(movie.getName().replace(s, ""));
					result.add(e);
				}
			}
		}
		return result;
	}
}
