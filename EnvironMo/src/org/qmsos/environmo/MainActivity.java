package org.qmsos.environmo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qmsos.environmo.util.UtilPagerAdapter;
import org.qmsos.environmo.util.UtilPagerIndicator;
import org.qmsos.environmo.util.UtilRefreshLayout;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Main activity of EnvironMo.
 * 
 * @author EnvironMo
 * 
 */
public class MainActivity extends AppCompatActivity 
implements LoaderCallbacks<Cursor>, OnPageChangeListener {
	
	private List<Long> cityList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ScrollView scrollView = (ScrollView) findViewById(R.id.scroll_view);
		final UtilRefreshLayout refreshLayout = (UtilRefreshLayout) findViewById(R.id.swipe_refresh);
		refreshLayout.setScrollView(scrollView);
		refreshLayout.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						refreshLayout.setRefreshing(false);

						Intent intent = new Intent(getBaseContext(), MainUpdateService.class);
						intent.setAction(MainUpdateService.ACTION_REFRESH);
						startService(intent);
					}
				}, 500);					
			}
		});

		TextView cityName = (TextView) findViewById(R.id.city_name);
		cityName.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(getBaseContext(), CityActivity.class);
				startActivity(i);
			}
		});
		
		for (int i = 0; i <= 3; i++) {
			final int j = i;
			TextView textView = (TextView) findViewById(
					getResources().getIdentifier("forecast_" + j, "id", getPackageName()));
			textView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					click(j);
				}
			});
		}

		cityList = new ArrayList<Long>();
		
		String[] projection = { CityProvider.KEY_ID, CityProvider.KEY_CITYID, CityProvider.KEY_NAME };
		String where = CityProvider.KEY_CITYID;
		Cursor query = getContentResolver().query(CityProvider.CONTENT_URI, projection, where, null, null);
		if (query != null && query.getCount() != 0) {
			List<Fragment> fragmentList = new ArrayList<Fragment>();
			while (query.moveToNext()) {
				Long cityId = query.getLong(query.getColumnIndex(CityProvider.KEY_CITYID));
				WeatherFragment fragment = WeatherFragment.newInstance(this, cityId);
				fragmentList.add(fragment);
				cityList.add(cityId);
			}
			query.close();
			
			ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
			UtilPagerAdapter adapter = new UtilPagerAdapter(getSupportFragmentManager(), fragmentList);
			viewPager.setAdapter(adapter);
			UtilPagerIndicator pagerIndicator = (UtilPagerIndicator) findViewById(R.id.pager_indicator);
			pagerIndicator.setViewPager(viewPager);
			pagerIndicator.setOnPageChangeListener(this);

			long cityId = cityList.get(viewPager.getCurrentItem());
			updateBackground(cityId);
			updateCityName(cityId);
			updateForecast(cityId);
		} else {
			query.close();
			Intent i = new Intent(this, CityActivity.class);
			
			startActivity(i);
		}
	
		getSupportLoaderManager().initLoader(0, null, this);
	}

	@Override
	protected void onDestroy() {
		getSupportLoaderManager().destroyLoader(0);

		super.onDestroy();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = { CityProvider.KEY_ID, CityProvider.KEY_CITYID, CityProvider.KEY_NAME };
		String where = CityProvider.KEY_CITYID;

		return new CursorLoader(this, CityProvider.CONTENT_URI, projection, where, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// TODO Auto-generated method stub
		Log.e("onLoadFinished", "finished?");
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// TODO Auto-generated method stub
		Log.e("onLoadReset", "Reset?");
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
		
		updateBackground(cityId);
		updateCityName(cityId);
		updateForecast(cityId);
	}

	private void click(int day) {
		if (day < 0 || day > 3) {
			return;
		}
		
		FragmentManager manager = getSupportFragmentManager();

		ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
		UtilPagerAdapter adapter = (UtilPagerAdapter) viewPager.getAdapter();

		String tag = adapter.getTag(viewPager.getCurrentItem());
		if (tag != null && manager.findFragmentByTag(tag) != null) {
			WeatherFragment fragment = (WeatherFragment) manager.findFragmentByTag(tag);
			fragment.showForecast(day);
		}

		long cityId = cityList.get(viewPager.getCurrentItem());
		if (day == 0) {
			updateBackground(cityId);
		} else {
			String[] projection = { CityProvider.KEY_FORECAST };
			String where = CityProvider.KEY_CITYID + " = " + cityId;
			Cursor cursor = getContentResolver()
					.query(CityProvider.CONTENT_URI, projection, where, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				String forecast = cursor.getString(cursor.getColumnIndex(CityProvider.KEY_FORECAST));
				
				JSONObject reader;
				try {
					reader = new JSONObject(forecast);
					JSONArray list = reader.getJSONArray("list");
					JSONObject dayForecast = list.getJSONObject(day);
					JSONArray weather = dayForecast.getJSONArray("weather");
					int weatherId = weather.getJSONObject(0).getInt("id");
					
					View v = findViewById(R.id.swipe_refresh);
					setBackgroundImage(v, weatherId);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			cursor.close();
		}
	}

	private void updateBackground(long cityId) {
		if (cityId > 0) {
			String[] projection = { CityProvider.KEY_CITYID, CityProvider.KEY_CURRENT };
			String where = CityProvider.KEY_CITYID + " = " + cityId;
			Cursor cursor = getContentResolver()
					.query(CityProvider.CONTENT_URI, projection, where, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				String current = cursor.getString(cursor.getColumnIndex(CityProvider.KEY_CURRENT));
				try {
					JSONObject reader = new JSONObject(current);
					JSONArray weather = reader.getJSONArray("weather");
					int id = weather.getJSONObject(0).getInt("id");
					
					View v = findViewById(R.id.swipe_refresh);
					setBackgroundImage(v, id);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			cursor.close();
		}
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
				int weatherId = weather.getJSONObject(0).getInt("id");
				
				JSONObject temp = forecast.getJSONObject("temp");
				int temperatureMin = temp.getInt("min");
				int temperatureMax = temp.getInt("max");
				
				String temperatureString = String.valueOf(temperatureMin) + "~"
						+ String.valueOf(temperatureMax) + "\u00B0" + "C";
				String dateString = getDayOfWeek(date);
				if (dateString != null) {
					TextView textView = (TextView) findViewById(
							getResources().getIdentifier("forecast_" + i, "id", getPackageName()));
					textView.setText(dateString + "\n" + temperatureString);
					
					setForecastIcon(textView, weatherId);
				}
			}
		} catch (JSONException e) {
			Log.e(MainActivity.class.getSimpleName(), "JSON parsing error!");
			
			return;
		}
	}

	private void setBackgroundImage(View v, int id) {
		if (200 <= id && id <= 299) {
			v.setBackgroundResource(R.drawable.bg_11);
		} else if (300 <= id && id <= 399) {
			v.setBackgroundResource(R.drawable.bg_09);
		} else if (500 <= id && id <= 504) {
			v.setBackgroundResource(R.drawable.bg_10);
		} else if (511 == id) {
			v.setBackgroundResource(R.drawable.bg_13);
		} else if (520 <= id && id <= 599) {
			v.setBackgroundResource(R.drawable.bg_09);
		} else if (600 <= id && id <= 699) {
			v.setBackgroundResource(R.drawable.bg_13);
		} else if (700 <= id && id <= 799) {
			v.setBackgroundResource(R.drawable.bg_50);
		} else if (800 == id) {
			v.setBackgroundResource(R.drawable.bg_01);
		} else if (801 == id) {
			v.setBackgroundResource(R.drawable.bg_02);
		} else if (802 == id || 803 == id) {
			v.setBackgroundResource(R.drawable.bg_03);
		} else if (804 == id) {
			v.setBackgroundResource(R.drawable.bg_04);
		}
	}
	
	private void setForecastIcon(TextView v, int id) {
		if (200 <= id && id <= 299) {
			v.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_11, 0, 0);
		} else if (300 <= id && id <= 399) {
			v.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_09, 0, 0);
		} else if (500 <= id && id <= 504) {
			v.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_10, 0, 0);
		} else if (511 == id) {
			v.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_13, 0, 0);
		} else if (520 <= id && id <= 599) {
			v.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_09, 0, 0);
		} else if (600 <= id && id <= 699) {
			v.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_13, 0, 0);
		} else if (700 <= id && id <= 799) {
			v.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_50, 0, 0);
		} else if (800 == id) {
			v.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_01, 0, 0);
		} else if (801 == id) {
			v.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_02, 0, 0);
		} else if (802 == id || 803 == id) {
			v.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_03, 0, 0);
		} else if (804 == id) {
			v.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_04, 0, 0);
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