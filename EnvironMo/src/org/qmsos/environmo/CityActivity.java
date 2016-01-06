package org.qmsos.environmo;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class CityActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_city);
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		
		
		TextView v = (TextView) findViewById(R.id.city_candidate);
		
		StringBuilder b = new StringBuilder();
		
		String[] projection = { CityProvider.KEY_ID, CityProvider.KEY_CITYID, CityProvider.KEY_NAME };
		
		ContentResolver resolver = getContentResolver();
		String where = CityProvider.KEY_CITYID;

		Cursor query = resolver.query(CityProvider.CONTENT_URI, projection, where, null, null);
		if (query != null && query.getCount() != 0) {
			while (query.moveToNext()) {
				Long cityId = query.getLong(query.getColumnIndex(CityProvider.KEY_CITYID));
				String name = query.getString(query.getColumnIndex(CityProvider.KEY_NAME));
				b.append(cityId);
				b.append(name);
				b.append("\n");
			}
		}
		query.close();
		
		v.setText(b.toString());
		
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
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		String action = intent.getAction();
		if (action != null && action.equals(MainUpdateService.ACTION_CITY_ADDED)) {
//			TODO: update GUI
		}
	}

}
