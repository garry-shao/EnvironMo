package org.qmsos.environmo;

import org.qmsos.environmo.util.UtilCursorAdapter;

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
	
	private UtilCursorAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_city);
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		adapter = new UtilCursorAdapter(this, null);
		RecyclerView cityNames = (RecyclerView) findViewById(R.id.city_candidate);
		cityNames.setLayoutManager(new LinearLayoutManager(this));
		cityNames.setAdapter(adapter);
		
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
		getLoaderManager().destroyLoader(0);

		super.onDestroy();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String where = CityProvider.KEY_CITYID;
		String[] projection = { CityProvider.KEY_ID, CityProvider.KEY_CITYID, CityProvider.KEY_NAME };
		
		return new CursorLoader(this, CityProvider.CONTENT_URI, projection, where, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
	}

}
