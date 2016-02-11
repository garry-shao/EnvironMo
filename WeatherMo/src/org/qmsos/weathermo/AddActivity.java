package org.qmsos.weathermo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qmsos.weathermo.util.City;
import org.qmsos.weathermo.util.IpcConstants;
import org.qmsos.weathermo.widget.CityRecyclerViewAdapter;
import org.qmsos.weathermo.widget.CityRecyclerViewAdapter.InsertCityCallback;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class AddActivity extends AppCompatActivity implements InsertCityCallback {

	private MessageReceiver mMessageReceiver;
	private CityRecyclerViewAdapter mCandidateAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add);
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		
		mMessageReceiver = new MessageReceiver();
		
		mCandidateAdapter = new CityRecyclerViewAdapter();
		RecyclerView candidatesView = (RecyclerView) findViewById(R.id.city_candidates);
		candidatesView.setLayoutManager(new LinearLayoutManager(this));
		candidatesView.setAdapter(mCandidateAdapter);
		
		EditText cityNameEditText = (EditText) findViewById(R.id.city_name);
		cityNameEditText.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					String cityName = v.getText().toString();
					
					Intent intent = new Intent(getBaseContext(), WeatherService.class);
					intent.setAction(IpcConstants.ACTION_QUERY_CITY);
					intent.putExtra(IpcConstants.EXTRA_CITY_NAME, cityName);
					
					startService(intent);
					
					InputMethodManager manager = 
							(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
					
					return true;
				} else {
					return false;
				}
				
			}
		});
	}

	@Override
	protected void onPause() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);

		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(IpcConstants.ACTION_QUERY_EXECUTED);
		filter.addAction(IpcConstants.ACTION_INSERT_EXECUTED);
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filter);
	}

	@Override
	public void onInsertCity(City city) {
		Intent i = new Intent(this, WeatherService.class);
		i.setAction(IpcConstants.ACTION_INSERT_CITY);
		i.putExtra(IpcConstants.EXTRA_INSERT_CITY, city);
		
		startService(i);
	}

	private void parseSearchResult(String result) {
		if (result == null) {
			return;
		}
		
		try {
			JSONObject reader = new JSONObject(result);
			JSONArray list = reader.getJSONArray("list");
			
			int length = list.length();
			City[] candidates = new City[length];
			for (int i = 0; i < length; i++) { 
				JSONObject instance = list.getJSONObject(i);
				
				long cityId = instance.getLong("id");
				String name = instance.getString("name");
				
				JSONObject coord = instance.getJSONObject("coord");
				double longitude = coord.getDouble("lon");
				double latitude = coord.getDouble("lat");
				
				JSONObject sys = instance.getJSONObject("sys");
				String country = sys.getString("country");

				longitude = longitude * 100;
				longitude = Math.round(longitude);
				longitude = longitude / 100;
				
				latitude = latitude * 100;
				latitude = Math.round(latitude);
				latitude = latitude / 100;
				
				candidates[i] = new City(cityId, name, country, longitude, latitude);
			}
			
			mCandidateAdapter.swapData(candidates);
		} catch (JSONException e) {
		}
	}

	private class MessageReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action != null) {
				if (action.equals(IpcConstants.ACTION_QUERY_EXECUTED)) {
					boolean flag = intent.getBooleanExtra(IpcConstants.EXTRA_QUERY_EXECUTED, false);
					if (flag) {
						String result = intent.getStringExtra(IpcConstants.EXTRA_QUERY_RESULT);
						parseSearchResult(result);
					}
				} else if (action.equals(IpcConstants.ACTION_INSERT_EXECUTED)) {
					boolean flag = intent.getBooleanExtra(IpcConstants.EXTRA_INSERT_EXECUTED, false);
					String text = flag ? "Succeeded" : "Failed";
					Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

}
