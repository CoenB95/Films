package com.cbapps.films;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.net.ConnectivityManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.cbapps.films.movie.Movie;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

	private static final String TAG = "MainActivity";

	private List<Movie> movies;
	private BroadcastReceiver networkReceiver;
	private IntentFilter networkIntentFilter;
	private ScheduledFuture future;
    private RecyclerView recyclerView;
	private MovieAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

	    adapter = new MovieAdapter(getBaseContext());
	    adapter.addOnMovieClickListener(new MovieAdapter.OnMovieClickListener() {
		    @Override
		    public void onMovieClicked(Movie movie) {
			    Snackbar.make(recyclerView, movie.getName() + " clicked", Snackbar.LENGTH_SHORT).show();
			    adapter.showTimes(movie);
		    }
	    });

	    recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
	    recyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
//	    recyclerView.addItemDecoration(new DividerItemDecoration(getBaseContext(),
//			    DividerItemDecoration.VERTICAL));
	    recyclerView.setAdapter(adapter);

	    networkIntentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
	    networkReceiver = new BroadcastReceiver() {
		    @Override
		    public void onReceive(Context context, Intent intent) {
			    ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			    NetworkInfo info = ConnectivityManagerCompat.getNetworkInfoFromBroadcast(cm, intent);
			    if (info != null && info.isConnected()) {
				    Log.d(TAG, "Network connected! Refresh movies");
				    refreshMovies(true);
			    } else {
				    Log.d(TAG, "Network disconnected");
			    }
		    }
	    };

        refreshMovies(false);
    }

	@Override
	protected void onStart() {
		super.onStart();
		registerReceiver(networkReceiver, networkIntentFilter);
		future = scheduleUpdater();
	}

	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(networkReceiver);
		future.cancel(true);
	}

	public void refreshMovies(final boolean fromNetwork) {
	    new AsyncTask<Boolean, Void, List<Movie>>() {

		    @Override
		    protected List<Movie> doInBackground(Boolean... params) {
			    List<Movie> movieList;
			    if (params[0]) {
				    Log.d(TAG, "Loading movies from network...");
				    movieList = MovieParser.loadFromNetwork();
			    } else {
				    Log.d(TAG, "Loading movies from storage...");
				    movieList = MovieParser.loadFromFile(
						    new File(getBaseContext().getFilesDir(), "movies.json"));
			    }
			    Log.d(TAG, "Loading movies done.");
			    Log.d(TAG, "Combining movies...");
			    MovieCombiner.combineMovies(movieList);
			    Log.d(TAG, "Combining movies done.");
			    return movieList;
		    }

		    @Override
		    protected void onPostExecute(List<Movie> newMovies) {
			    movies = newMovies;
			    adapter.showMovies(movies);
			    if (fromNetwork) {
				    saveMovies();
			    }
		    }
	    }.execute(fromNetwork);
    }

	public void saveMovies() {
		new AsyncTask<Movie, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Movie... params) {
				Log.d(TAG, "Saving movies...");
				return MovieParser.saveToFile(movies,
						new File(getBaseContext().getFilesDir(), "movies.json"));
			}

			@Override
			protected void onPostExecute(Boolean success) {
				Log.d(TAG, "Saving movies done.");
				if (success) Snackbar.make(recyclerView, R.string.saving_succeeded, Snackbar.LENGTH_SHORT).show();
				else Snackbar.make(recyclerView, R.string.saving_failed, Snackbar.LENGTH_SHORT).show();
			}
		}.execute();
	}

	public ScheduledFuture scheduleUpdater() {
		Calendar now = Calendar.getInstance();
		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		return service.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Log.d(TAG, "Update times...");
							adapter.updateTimes();
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 60 - now.get(Calendar.SECOND) + 1, 60, TimeUnit.SECONDS);
	}
}
