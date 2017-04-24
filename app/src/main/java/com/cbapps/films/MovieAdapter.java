package com.cbapps.films;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cbapps.films.movie.Extra;
import com.cbapps.films.movie.Movie;
import com.cbapps.films.movie.ScheduledTime;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Coen Boelhouwers
 */
public class MovieAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

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
		private int index;

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
			SimpleTime now = SimpleTime.now();
			if (time.getTime().isBefore(now)) {
				nextTime.setTextColor(colorGrey);
				nextTimeAttr.setTextColor(colorGrey);
				nextTime.setText(time.getTime().toString());
			} else if (time.getTime().isBefore(now.plusHours(1))) {
				nextTime.setTextColor(colorOrange);
				nextTime.setText(SimpleTime.now().getDurationTill(time.getTime()));
			} else {
				nextTime.setTextColor(colorGreen);
				nextTime.setText(time.getTime().toString());
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
//
//			List<ScheduledTime> times = movie.getTimes();
//
//
//			for (int i = 0; i < times.size(); i++) {
//				ScheduledTime time = times.get(i);
//				if (time.getTime().isBefore(now)) continue;
//
//
//
//
//
//				view.setTextColor(colorTextSecondary);
//				if (i >= times.size()) {
//					view.setText("");
//					continue;
//				}
//				StringBuilder textBuilder = new StringBuilder();
//
//				int minTill = SimpleTime.now().getMinutesTill(time.getTime());
//				if (firstTime && minTill < 60 && minTill > 0) {
//					firstTime = false;
//					textBuilder.append(SimpleTime.now().getDurationTill(time.getTime()));
//				} else textBuilder.append(time.getTime().toString());
//				textBuilder.append(' ').append(time.getLanguage().toString());
//				textBuilder.append(' ').append(time.getProjection().toString());
//				for (Extra e : time.getExtras()) textBuilder.append(' ').append(e.toString());
//
//				if (time.getTime().isBefore(now))
//					view.setTextColor(colorGrey);
//				else if (time.getTime().isBefore(now.plusHours(1)))
//					view.setTextColor(colorOrange);
//				else
//					view.setTextColor(colorGreen);
//				view.setText(textBuilder.toString());
//			}
		}
	}
}
