package com.cbapps.films;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
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
import org.joda.time.LocalTime;
import org.joda.time.Minutes;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Coen Boelhouwers
 */
public class MovieAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	public static final int JUST_STARTED_MINUTES = 10;

	private Context context;
	private List<ViewType> viewTypes;
	private @ColorInt int colorOrange;
	private @ColorInt int colorGreen;
	private @ColorInt int colorRed;
	private @ColorInt int colorGrey;
	private @ColorInt int colorTextSecondary;

	public MovieAdapter(Context c) {
		context = c;
		viewTypes = new ArrayList<>();
		colorTextSecondary = ContextCompat.getColor(context, R.color.textDarkSecondary);
		colorRed = ContextCompat.getColor(context, R.color.red500);
		colorOrange = ContextCompat.getColor(context, R.color.orange500);
		colorGreen = ContextCompat.getColor(context, R.color.green500);
		colorGrey = ContextCompat.getColor(context, R.color.grey500);
	}

	public void setMovies(List<Movie> movies) {
		viewTypes.clear();
		for (Movie m : movies) {
			viewTypes.add(new ViewType(m, ViewType.TYPE_NAME));
			for (int i = 0; i < m.getTimes().size(); i++) {
				ScheduledTime time = m.getTimes().get(i);
				if (i == m.getTimes().size() - 1) {
					viewTypes.add(new ViewType(time, ViewType.TYPE_END));
				} else {
					viewTypes.add(new ViewType(time, ViewType.TYPE_TIME));
				}
			}
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

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		switch (viewType) {
			case ViewType.TYPE_NAME:
				return new MovieHolder(LayoutInflater.from(parent.getContext())
						.inflate(R.layout.movie_list_item, parent, false));
			case ViewType.TYPE_TIME:
				return new ScheduledTimeHolder(LayoutInflater.from(parent.getContext())
						.inflate(R.layout.scheduled_time_list_item, parent, false));
			case ViewType.TYPE_END:
				return new ScheduledTimeHolder(LayoutInflater.from(parent.getContext())
						.inflate(R.layout.movie_end_list_item, parent, false));
			default:
				return null;
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		ViewType viewType = viewTypes.get(position);
		switch (viewType.type) {
			case ViewType.TYPE_NAME:
				((MovieHolder) holder).setMovie((Movie) viewType.movie);
				break;
			case ViewType.TYPE_TIME:
			case ViewType.TYPE_END:
				((ScheduledTimeHolder) holder).setTime((ScheduledTime) viewType.movie);
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

		private Object movie;
		private int type;

		private ViewType(Object movie, int type) {
			this.movie = movie;
			this.type = type;
		}
	}

	public class ScheduledTimeHolder extends RecyclerView.ViewHolder {

		private TextView nextTime;
		private TextView nextTimeAttr;

		public ScheduledTimeHolder(View itemView) {
			super(itemView);
			nextTime = (TextView) itemView.findViewById(R.id.next_time);
			nextTimeAttr = (TextView) itemView.findViewById(R.id.all_types);
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

		private TextView title;

		public MovieHolder(View itemView) {
			super(itemView);
			title = (TextView) itemView.findViewById(R.id.title);

		}

		public void setMovie(Movie movie) {
			title.setText(movie.getName());
		}
	}
}
