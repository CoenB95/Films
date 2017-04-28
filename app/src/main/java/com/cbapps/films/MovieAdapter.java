package com.cbapps.films;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
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
import org.joda.time.Minutes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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

	private View combineLayouts(ViewGroup parent, int cardRes, @LayoutRes int childRes) {
		View view2 = LayoutInflater.from(parent.getContext())
				.inflate(childRes, parent, false);
		if (cardRes >= 0) {
			View view = LayoutInflater.from(parent.getContext())
					.inflate(cardRes, parent, false);
			((CardView) view.findViewById(R.id.card_view)).addView(view2);
			return view;
		}
		return view2;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		boolean cardTop = ((viewType & ViewType.CARD_START) == ViewType.CARD_START);
		boolean cardBottom = ((viewType & ViewType.CARD_END) == ViewType.CARD_END);
		boolean cardMid = ((viewType & ViewType.CARD_MID) == ViewType.CARD_MID);
		boolean cardBoth = cardTop && cardBottom;
		int cardRes = cardBoth ? R.layout.card_both : (cardMid ? R.layout.card_mid :
				(cardTop ? R.layout.card_start : (cardBottom ? R.layout.card_end : -1)));

		if ((viewType & ViewType.TYPE_HEADER) == ViewType.TYPE_HEADER)
				return new HeaderHolder(combineLayouts(parent, cardRes, R.layout.header_row));
		if ((viewType & ViewType.TYPE_TIME) == ViewType.TYPE_TIME)
			return new ScheduledTimeHolder(combineLayouts(parent, cardRes, R.layout.scheduled_time_row));
		if ((viewType & ViewType.TYPE_TITLE) == ViewType.TYPE_TITLE)
			return new MovieHolder(combineLayouts(parent, cardRes, R.layout.movie_title_row));
		return null;
		/*switch (viewType) {
			case ViewType.TYPE_HEADER:
				return new HeaderHolder(LayoutInflater.from(parent.getContext())
						.inflate(R.layout.header_row, parent, false));
			case ViewType.TYPE_END:
			case ViewType.TYPE_END_EMPTY:
				return new ScheduledTimeHolder(combineLayouts(parent, R.layout.card_start,
						R.layout.scheduled_time_row));
			case ViewType.TYPE_NAME:
				return new MovieHolder(LayoutInflater.from(parent.getContext())
						.inflate(R.layout.movie_title_row, parent, false));
			case ViewType.TYPE_TIME:
				return new ScheduledTimeHolder(LayoutInflater.from(parent.getContext())
						.inflate(R.layout.scheduled_time_list_item, parent, false));
			default:
				return null;
		}*/
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		ViewType viewType = viewTypes.get(position);
		switch (viewType.type & ViewType.TYPE_ALL) {
			case ViewType.TYPE_HEADER:
				((HeaderHolder) holder).setHeader((String) viewType.movie);
				break;
			case ViewType.TYPE_TITLE:
				((MovieHolder) holder).setMovie((Movie) viewType.movie);
				break;
			case ViewType.TYPE_TIME:
			//case ViewType.TYPE_END:
				((ScheduledTimeHolder) holder).setTime((ScheduledTime) viewType.movie);
				break;
			//case ViewType.TYPE_END_EMPTY:
			//	((ScheduledTimeHolder) holder).setNoneToday();
			//	break;
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
		private static final int TYPE_HEADER =  0b0001_0000;
		private static final int TYPE_TITLE =   0b0010_0000;
		private static final int TYPE_TIME =    0b0100_0000;
		private static final int TYPE_ALL =     0b0111_0000;
		private static final int CARD_START =   0b0000_0001;
		private static final int CARD_MID =     0b0000_0010;
		private static final int CARD_END =     0b0000_0100;
		private static final int CARD_BOTH =    0b0000_0101;

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
			viewTypes.add(new ViewType(m, ViewType.TYPE_TITLE | ViewType.CARD_START));

			List<ScheduledTime> showsToday = m.getTimes().get(LocalDate.now());
			if (showsToday == null || showsToday.isEmpty()) {
				viewTypes.add(new ViewType(null, ViewType.TYPE_TIME | ViewType.CARD_END));
			} else {
				Collections.sort(showsToday);
				for (int i = 0; i < showsToday.size(); i++) {
					ScheduledTime time = showsToday.get(i);
					if (i == showsToday.size() - 1) {
						viewTypes.add(new ViewType(time, ViewType.TYPE_TIME | ViewType.CARD_END));
					} else {
						viewTypes.add(new ViewType(time, ViewType.TYPE_TIME | ViewType.CARD_MID));
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
				viewTypes.add(new ViewType(null, ViewType.TYPE_TIME | ViewType.CARD_BOTH));
			} else {
				Collections.sort(list);
				for (int i2 = 0; i2 < list.size(); i2++)
					viewTypes.add(new ViewType(list.get(i2),
							ViewType.TYPE_TIME | (i2 == 0 ? ViewType.CARD_START : 0) |
									(i2 == list.size() - 1 ? ViewType.CARD_END : 0) |
									(i2 > 0 && i2 < list.size() - 1 ? ViewType.CARD_MID : 0)));
			}
			today = today.plusDays(1);
		}
		notifyDataSetChanged();
	}

	public void updateTimes() {
		DateTime now = DateTime.now();
		for (int i = 0; i < viewTypes.size(); i++) {
			ViewType v = viewTypes.get(i);
			if (v.movie != null && (v.type & ViewType.TYPE_TIME) == ViewType.TYPE_TIME) {
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
			if (time == null) {
				setNoneToday();
				return;
			}
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

		public void setMovie(final Movie movie) {
			if (movie == null) {
				title.setText("");
				title.setOnClickListener(null);
			} else {
				title.setText(movie.getName());
				title.setOnClickListener(new View.OnClickListener() {
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
