package org.qmsos.weathermo;

import org.qmsos.weathermo.widget.CursorRecyclerViewAdapter;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

public class CityActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {
	
	private CursorRecyclerViewAdapter mCursorAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_city);
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		mCursorAdapter = new CursorRecyclerViewAdapter(this, null);
		RecyclerView citiesView = (RecyclerView) findViewById(R.id.cities);
		citiesView.setLayoutManager(new LinearLayoutManager(this));
		citiesView.setAdapter(mCursorAdapter);
		
		getSupportLoaderManager().initLoader(0, null, this);
	}

	@Override
	protected void onDestroy() {
		getSupportLoaderManager().destroyLoader(0);

		super.onDestroy();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String where = WeatherProvider.KEY_CITY_ID;
		
		return new CursorLoader(this, WeatherProvider.CONTENT_URI_CITIES, null, where, null, null);
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
