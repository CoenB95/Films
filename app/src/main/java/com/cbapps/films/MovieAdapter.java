package com.cbapps.films;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cbapps.films.movie.Movie;
import com.cbapps.films.movie.ScheduledTime;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Minutes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Coen Boelhouwers
 */
public class MovieAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	public static final int JUST_STARTED_MINUTES = 10;

	private Context context;
	private List<OnMovieClickListener> listeners;
	private List<ViewType> viewTypes;
	private @ColorInt int colorOrange;
	private @ColorInt int colorGreen;
	private @ColorInt int colorRed;
	private @ColorInt int colorGrey;
	private @ColorInt int colorTextSecondary;

	public MovieAdapter(Context c) {
		context = c;
		listeners = new ArrayList<>();
		viewTypes = new ArrayList<>();
		colorTextSecondary = ContextCompat.getColor(context, R.color.textDarkSecondary);
		colorRed = ContextCompat.getColor(context, R.color.red500);
		colorOrange = ContextCompat.getColor(context, R.color.orange500);
		colorGreen = ContextCompat.getColor(context, R.color.green500);
		colorGrey = ContextCompat.getColor(context, R.color.grey500);
	}

	public void addOnMovieClickListener(OnMovieClickListener l) {
		listeners.add(l);
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		switch (viewType) {
			case ViewType.TYPE_HEADER:
				return new HeaderHolder(LayoutInflater.from(parent.getContext())
						.inflate(R.layout.header_list_item, parent, false));
			case ViewType.TYPE_END:
			case ViewType.TYPE_END_EMPTY:
				return new ScheduledTimeHolder(LayoutInflater.from(parent.getContext())
						.inflate(R.layout.movie_end_list_item, parent, false));
			case ViewType.TYPE_NAME:
				return new MovieHolder(LayoutInflater.from(parent.getContext())
						.inflate(R.layout.movie_list_item, parent, false));
			case ViewType.TYPE_TIME:
				return new ScheduledTimeHolder(LayoutInflater.from(parent.getContext())
						.inflate(R.layout.scheduled_time_list_item, parent, false));
			default:
				return null;
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		ViewType viewType = viewTypes.get(position);
		switch (viewType.type) {
			case ViewType.TYPE_HEADER:
				((HeaderHolder) holder).setHeader((String) viewType.movie);
				break;
			case ViewType.TYPE_NAME:
				((MovieHolder) holder).setMovie((Movie) viewType.movie);
				break;
			case ViewType.TYPE_TIME:
			case ViewType.TYPE_END:
				((ScheduledTimeHolder) holder).setTime((ScheduledTime) viewType.movie);
				break;
			case ViewType.TYPE_END_EMPTY:
				((ScheduledTimeHolder) holder).setNoneToday();
				break;
		}
	}

	@Override
	public int getItemCount() {
		return viewTypes.size();
	}

	@Override
	public int getItemViewType(int position) {
		return viewTypes.get(position).type;
	}

	private static class ViewType {
		private static final int TYPE_HEADER = 0;
		private static final int TYPE_NAME = 1;
		private static final int TYPE_TIME = 2;
		private static final int TYPE_END = 3;
		private static final int TYPE_END_EMPTY = 4;

		private Object movie;
		private int type;

		private ViewType(Object movie, int type) {
			this.movie = movie;
			this.type = type;
		}
	}
	public void showMovies(List<Movie> movies) {
		viewTypes.clear();
		for (Movie m : movies) {
			viewTypes.add(new ViewType(m, ViewType.TYPE_NAME));

			List<ScheduledTime> showsToday = m.getTimes().get(LocalDate.now());
			if (showsToday == null || showsToday.isEmpty()) {
				viewTypes.add(new ViewType(null, ViewType.TYPE_END_EMPTY));
			} else {
				Collections.sort(showsToday);
				for (int i = 0; i < showsToday.size(); i++) {
					ScheduledTime time = showsToday.get(i);
					if (i == showsToday.size() - 1) {
						viewTypes.add(new ViewType(time, ViewType.TYPE_END));
					} else {
						viewTypes.add(new ViewType(time, ViewType.TYPE_TIME));
					}
				}
			}
		}
		notifyDataSetChanged();
	}

	public void showTimes(Movie movie) {
		viewTypes.clear();
		LocalDate today = LocalDate.now();
		for (int i = 0; i < 10; i++) {
			viewTypes.add(new ViewType(today.dayOfWeek().getAsText(Locale.getDefault()),
					ViewType.TYPE_HEADER));

			List<ScheduledTime> list = movie.getTimes().get(today);

			if (list == null || list.isEmpty()) {
				viewTypes.add(new ViewType(null, ViewType.TYPE_END_EMPTY));
			} else {
				Collections.sort(list);
				for (int i2 = 0; i2 < list.size(); i2++)
					viewTypes.add(new ViewType(list.get(i2),
							i2 == list.size() - 1 ? ViewType.TYPE_END : ViewType.TYPE_TIME));
			}
			today = today.plusDays(1);
		}
		notifyDataSetChanged();
	}

	public void updateTimes() {
		DateTime now = DateTime.now();
		for (int i = 0; i < viewTypes.size(); i++) {
			ViewType v = viewTypes.get(i);
			if (v.type == ViewType.TYPE_TIME || v.type == ViewType.TYPE_END) {
				int timeTill = Minutes.minutesBetween(now, ((ScheduledTime) v.movie).getTime())
						.getMinutes();
				if (timeTill >= -JUST_STARTED_MINUTES && timeTill < 60) {
					Log.d("MovieAdapter", "Update time at position " + i);
					notifyItemChanged(i);
				}
			}
		}
		notifyItemRangeChanged(0, getItemCount());
	}

	public class HeaderHolder extends RecyclerView.ViewHolder {

		private TextView header;

		public HeaderHolder(View itemView) {
			super(itemView);
			header = (TextView) itemView.findViewById(R.id.header_text);
		}

		public void setHeader(String value) {
			header.setText(value);
		}
	}

	public class ScheduledTimeHolder extends RecyclerView.ViewHolder {

		private CardView cardView;
		private TextView nextTime;
		private TextView nextTimeAttr;

		public ScheduledTimeHolder(View itemView) {
			super(itemView);
			cardView = (CardView) itemView.findViewById(R.id.card_view);
			nextTime = (TextView) itemView.findViewById(R.id.next_time);
			nextTimeAttr = (TextView) itemView.findViewById(R.id.all_types);
		}

		public void enableOnClick(final Movie movie) {
			if (movie == null) cardView.setOnClickListener(null);
			else cardView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					for (OnMovieClickListener l : listeners) l.onMovieClicked(movie);
				}
			});
		}

		public void setNoneToday() {
			nextTime.setTextColor(colorGrey);
			nextTime.setText(R.string.none_today);
			nextTimeAttr.setText("");
		}

		public void setTime(ScheduledTime time) {
			nextTime.setTextColor(colorTextSecondary);
			nextTimeAttr.setTextColor(colorTextSecondary);
			DateTime now = DateTime.now();
			if (now.isBefore(time.getTime().minusMinutes(60))) {
				nextTime.setTextColor(colorGreen);
				nextTime.setText(time.getTime().withZone(DateTimeZone.getDefault()).toString(ScheduledTime.LOCAL_FORMAT));
			} else if (now.isBefore(time.getTime())) {
				nextTime.setTextColor(colorOrange);
				nextTime.setText(context.getResources().getString(R.string.minutes_format,
						Minutes.minutesBetween(now, time.getTime()).getMinutes()));
			} else if (now.isBefore(time.getTime().plusMinutes(JUST_STARTED_MINUTES))) {
				nextTime.setTextColor(colorRed);
				nextTimeAttr.setTextColor(colorGrey);
				nextTime.setText(R.string.just_started);
			} else {
				nextTime.setTextColor(colorGrey);
				nextTimeAttr.setTextColor(colorGrey);
				nextTime.setText(time.getTime().withZone(DateTimeZone.getDefault()).toString(ScheduledTime.LOCAL_FORMAT));
			}
			nextTimeAttr.setText(time.getAttrString());
		}
	}

	public class MovieHolder extends RecyclerView.ViewHolder {

		private CardView cardView;
		private TextView title;

		public MovieHolder(View itemView) {
			super(itemView);
			cardView = (CardView) itemView.findViewById(R.id.card_view);
			title = (TextView) itemView.findViewById(R.id.title);
		}

		public void setMovie(final Movie movie) {
			if (movie == null) {
				title.setText("");
				cardView.setOnClickListener(null);
			} else {
				title.setText(movie.getName());
				cardView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						for (OnMovieClickListener l : listeners) l.onMovieClicked(movie);
					}
				});
			}
		}
	}

	public interface OnMovieClickListener {
		void onMovieClicked(Movie movie);
	}
}
