package org.qmsos.weathermo;

import org.qmsos.weathermo.util.IpcConstants;
import org.qmsos.weathermo.widget.CursorRecyclerViewAdapter;
import org.qmsos.weathermo.widget.CursorRecyclerViewAdapter.ManageCityCallback;

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

public class CityActivity extends AppCompatActivity 
implements LoaderCallbacks<Cursor>, ManageCityCallback {
	
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

	@Override
	public void onInsertCity() {
		Intent i = new Intent(this, AddActivity.class);
		startActivity(i);		
	}

	@Override
	public void onDeleteCity(long cityId) {
		Intent i = new Intent(this, WeatherService.class);
		i.setAction(IpcConstants.ACTION_DELETE_CITY);
		i.putExtra(IpcConstants.EXTRA_CITY_ID, cityId);
		startService(i);		
	}

}
