package org.qmsos.weathermo;

import org.qmsos.weathermo.widget.RecyclerViewCursorAdapter;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class CityActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {
	
	private RecyclerViewCursorAdapter mCursorAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_city);
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		mCursorAdapter = new RecyclerViewCursorAdapter(this, null);
		RecyclerView cityNames = (RecyclerView) findViewById(R.id.city_candidate);
		cityNames.setLayoutManager(new LinearLayoutManager(this));
		cityNames.setAdapter(mCursorAdapter);
		
		getSupportLoaderManager().initLoader(0, null, this);

		Button addButton = (Button) findViewById(R.id.button_add);
		addButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(getBaseContext(), AddActivity.class);
				startActivity(i);
			}
		});
	}

	@Override
	protected void onDestroy() {
		getSupportLoaderManager().destroyLoader(0);

		super.onDestroy();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String where = WeatherProvider.KEY_CITY_ID;
		String[] projection = { WeatherProvider.KEY_ID, WeatherProvider.KEY_CITY_ID, WeatherProvider.KEY_NAME };
		
		return new CursorLoader(this, WeatherProvider.CONTENT_URI_CITIES, projection, where, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mCursorAdapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mCursorAdapter.swapCursor(null);
	}

}
