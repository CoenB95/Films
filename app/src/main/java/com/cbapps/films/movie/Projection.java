package com.cbapps.films.movie;

/**
 * @author Coen Boelhouwers
 */

public enum Projection {
	P2D(" (2D)", " 2D"),
	P3D(" (3D)", " 3D");

	private String[] slogans;

	Projection(String... slogans) {
		this.slogans = slogans;
	}

	/**
	 * Parses the movie's title to search for strings indicating the projection
	 * of this movie. Once found the specific string(s) will be removed from the title,
	 * cleaning it up on the go.
	 * @param movie the Movie which needs its title to be parsed.
	 */
	public static Projection apply(Movie movie) {
		for (Projection p : Projection.values()) {
			for (String s : p.slogans) {
				int index = movie.getName().indexOf(s);
				if (index >= 0) {
					movie.setName(movie.getName().replace(s, ""));
					return p;
				}
			}
		}
		return getDefault();
	}

	public static Projection getDefault() {
		return P2D;
	}

	@Override
	public String toString() {
		return name().substring(1);
	}
}
