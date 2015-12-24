package org.qmsos.environmo;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class CityActivity extends AppCompatActivity implements OnEditorActionListener {

	public static final String KEY_CITY_NAME = "KEY_CITY_NAME";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_city);
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		
		InputFilter noWhitespaceFilter = new InputFilter() {

			@Override
			public CharSequence filter(
					CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
				for (int i = start; i < end; i++) {
					if (Character.isSpaceChar(source.charAt(i))) {
						return "";
					}
				}
				return null;
			}
		};
		
		EditText cityNameEditText = (EditText) findViewById(R.id.city_name);
		cityNameEditText.setFilters(new InputFilter[] { noWhitespaceFilter });
		cityNameEditText.setOnEditorActionListener(this);
		
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
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		String action = intent.getAction();
		if (action != null && action.equals(MainUpdateService.ACTION_CITY_ADDED)) {
//			TODO: update GUI
		}
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		String cityName = v.getText().toString();
		
		Intent intent = new Intent(this, MainUpdateService.class);
		intent.setAction(MainUpdateService.ACTION_QUERY_CITY);
		intent.putExtra(MainUpdateService.EXTRA_KEY_CITY_NAME, cityName);
	
		startService(intent);
		
		finish();
		
		return true;
	}

	public void onClick(View v) {
		Intent i = new Intent(this, MainUpdateService.class);
		i.setAction(MainUpdateService.ACTION_IMPORT_CITY);
		
		startService(i);
		
		finish();
	}

}
