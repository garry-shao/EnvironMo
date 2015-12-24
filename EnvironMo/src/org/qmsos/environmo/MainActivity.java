package org.qmsos.environmo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qmsos.environmo.util.UtilPagerAdapter;
import org.qmsos.environmo.util.UtilRefreshLayout;
import org.qmsos.environmo.util.UtilResultReceiver;
import org.qmsos.environmo.util.UtilResultReceiver.Receiver;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Main activity of EnvironMo.
 * 
 * @author EnvironMo
 * 
 */
public class MainActivity extends AppCompatActivity 
implements OnPageChangeListener, OnRefreshListener, Receiver {

	private static final String KEY_RECEIVER = "KEY_RECEIVER";
	
	private static final String TAG = MainActivity.class.getSimpleName();
	
	private UtilResultReceiver receiver;
	private UtilRefreshLayout refreshLayout;
	
	private List<Fragment> fragmentList;
	private List<Long> cityList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState != null) {
			receiver = savedInstanceState.getParcelable(KEY_RECEIVER);
		} else {
			receiver = new UtilResultReceiver(new Handler());
		}

		ScrollView scrollView = (ScrollView) findViewById(R.id.scroll_view);
		refreshLayout = (UtilRefreshLayout) findViewById(R.id.swipe_refresh);
		refreshLayout.setScrollView(scrollView);
		refreshLayout.setOnRefreshListener(this);

		fragmentList = new ArrayList<Fragment>();
		cityList = new ArrayList<Long>();
		
		String[] projection = { CityProvider.KEY_ID, CityProvider.KEY_CITYID, CityProvider.KEY_NAME };
		String where = CityProvider.KEY_CITYID;
		Cursor query = getContentResolver().query(CityProvider.CONTENT_URI, projection, where, null, null);
		if (query != null && query.getCount() != 0) {
			while (query.moveToNext()) {
				Long cityId = query.getLong(query.getColumnIndex(CityProvider.KEY_CITYID));
				WeatherFragment fragment = WeatherFragment.newInstance(this, cityId);
				fragmentList.add(fragment);
				cityList.add(cityId);
			}
			
			ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
			UtilPagerAdapter adapter = new UtilPagerAdapter(getSupportFragmentManager(), fragmentList);
			viewPager.setAdapter(adapter);
			viewPager.addOnPageChangeListener(this);

			long cityId = cityList.get(viewPager.getCurrentItem());
			updateCityName(cityId);
			updateForecast(cityId);
		} else {
			Intent i = new Intent(this, CityActivity.class);
			
			startActivity(i);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		receiver.setReceiver(this);
	}

	@Override
	protected void onPause() {
		receiver.setReceiver(null);

		super.onPause();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(KEY_RECEIVER, receiver);
		
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onRefresh() {
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				refreshLayout.setRefreshing(false);

				Intent intent = new Intent(getBaseContext(), MainUpdateService.class);
				intent.setAction(MainUpdateService.ACTION_REFRESH);
				intent.putExtra(UtilResultReceiver.RECEIVER, receiver);
				
				startService(intent);
			}
		}, 500);		
	}

	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {
		switch (resultCode) {
		case MainUpdateService.RESULT_CODE_REFRESHED:
//			TODO: update GUI
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int arg0) {
		ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
		long cityId = cityList.get(viewPager.getCurrentItem());
		updateCityName(cityId);
		updateForecast(cityId);
	}

	public void settingCity(View view) {
		Intent i = new Intent(this, CityActivity.class);
		
		startActivity(i);
	}

	private void updateCityName(long cityId) {
		if (cityId > 0) {
			String[] projection = { CityProvider.KEY_CITYID, CityProvider.KEY_NAME };
			String where = CityProvider.KEY_CITYID + " = " + cityId;
			Cursor cursor = getContentResolver()
					.query(CityProvider.CONTENT_URI, projection, where, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				String name = cursor.getString(cursor.getColumnIndex(CityProvider.KEY_NAME));
				TextView textView = (TextView) findViewById(R.id.city_name);
				textView.setText(name);
			}
			cursor.close();
		}
	}
	
	private void updateForecast(long cityId) {
		if (cityId > 0) {
			String[] projection = { CityProvider.KEY_CITYID, CityProvider.KEY_FORECAST };
			String where = CityProvider.KEY_CITYID + " = " + cityId;
			Cursor cursor = getContentResolver()
					.query(CityProvider.CONTENT_URI, projection, where, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				String forecast = cursor.getString(cursor.getColumnIndex(CityProvider.KEY_FORECAST));
				updateForecast(forecast);
			}
			cursor.close();
		}
	}
	
	private void updateForecast(String json) {
		try {
			JSONObject reader = new JSONObject(json);
	
			JSONArray list = reader.getJSONArray("list");
			for (int i = 0; i < list.length(); i++) {
				JSONObject forecast = list.getJSONObject(i);
				
				long date = forecast.getLong("dt");
				
				JSONArray weather = forecast.getJSONArray("weather");
				String mainString = weather.getJSONObject(0).getString("main");
				
				JSONObject temp = forecast.getJSONObject("temp");
				int temperatureMin = temp.getInt("min");
				int temperatureMax = temp.getInt("max");
	
				
				String temperatureString = String.valueOf(temperatureMin) + "~"
						+ String.valueOf(temperatureMax) + "\u00B0" + "C";
				String dateString = getDayOfWeek(date);
				if (dateString != null) {
					TextView textView = (TextView) findViewById(
							getResources().getIdentifier("forecast_" + i, "id", getPackageName()));
					textView.setText(mainString + "\n" + dateString + "\n" + temperatureString);
				}
			}
		} catch (JSONException e) {
			Log.e(TAG, "JSON parsing error!");
			
			return;
		}
	}

	private String getDayOfWeek(long time) {
		if (time == 0) {
			return null;
		}
		Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("UTC"));
		c.setTimeInMillis(time * 1000);
		
		int i = c.get(Calendar.DAY_OF_WEEK);
		switch (i) {
		case 1:
			return "Sun";
		case 2:
			return "Mon";
		case 3:
			return "Tues";
		case 4:
			return "Wed";
		case 5:
			return "Thur";
		case 6:
			return "Fri";
		case 7:
			return "Sat";
		default:
			return null;
		}
	}

}