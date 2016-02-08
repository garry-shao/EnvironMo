package org.qmsos.weathermo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class AddActivity extends AppCompatActivity implements OnEditorActionListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add);
		
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
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		String cityName = v.getText().toString();
		
		Intent intent = new Intent(this, WeatherService.class);
		intent.setAction(WeatherService.ACTION_QUERY_CITY);
		intent.putExtra(WeatherService.EXTRA_KEY_CITY_NAME, cityName);
	
		startService(intent);
		
		finish();
		
		return true;
	}

}
