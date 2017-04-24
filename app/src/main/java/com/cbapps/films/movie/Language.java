package com.cbapps.films.movie;

/**
 * @author Coen Boelhouwers
 */

public enum Language {
	NL(" (NL)", " NL"),
	OV(" (OV)", " OV");

	private String[] slogans;

	Language(String... slogans) {
		this.slogans = slogans;
	}

	/**
	 * Parses the movie's title to search for strings indicating the language
	 * of this movie. Once found the specific string(s) will be removed from the title,
	 * cleaning it up on the go.
	 * @param movie the Movie which needs its title to be parsed.
	 */
	public static Language apply(Movie movie) {
		for (Language l : Language.values()) {
			for (String s : l.slogans) {
				int index = movie.getName().indexOf(s);
				if (index >= 0) {
					movie.setName(movie.getName().replace(s, ""));
					return l;
				}
			}
		}
		return getDefault();
	}

	public static Language getDefault() {
		return OV;
	}

	@Override
	public String toString() {
		return name();
	}
}
