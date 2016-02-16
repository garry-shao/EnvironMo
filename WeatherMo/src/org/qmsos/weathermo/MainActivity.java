package org.qmsos.weathermo;

import java.util.regex.PatternSyntaxException;

import org.qmsos.weathermo.fragment.CurrentWeather;
import org.qmsos.weathermo.fragment.ForecastWeather;
import org.qmsos.weathermo.fragment.ForecastWeather.OnWeatherClickedListener;
import org.qmsos.weathermo.fragment.WeatherPagerAdapter;
import org.qmsos.weathermo.util.IpcConstants;
import org.qmsos.weathermo.util.WeatherInfoAdapter;
import org.qmsos.weathermo.util.WeatherParser;
import org.qmsos.weathermo.widget.DotViewPagerIndicator;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

/**
 * Main activity of EnvironMo.
 * 
 * @author EnvironMo
 * 
 */
public class MainActivity extends AppCompatActivity 
implements LoaderCallbacks<Cursor>, OnPageChangeListener, OnClickListener, OnWeatherClickedListener {
	
	private static final String TAG = MainActivity.class.getSimpleName();

	private WeatherPagerAdapter mPagerAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
		refreshLayout.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						refreshLayout.setRefreshing(false);

						Intent intent = new Intent(getBaseContext(), WeatherService.class);
						intent.setAction(IpcConstants.ACTION_REFRESH_WEATHER);
						startService(intent);
					}
				}, 500);					
			}
		});

		TextView cityName = (TextView) findViewById(R.id.city_name);
		cityName.setOnClickListener(this);
		
		TextView weatherMap = (TextView) findViewById(R.id.weather_map);
		weatherMap.setOnClickListener(this);
	
		mPagerAdapter = new WeatherPagerAdapter(getSupportFragmentManager(), this, null);
		ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
		viewPager.setAdapter(mPagerAdapter);
		
		DotViewPagerIndicator pagerIndicator = (DotViewPagerIndicator) findViewById(R.id.pager_indicator);
		pagerIndicator.setViewPager(viewPager);
		pagerIndicator.setOnPageChangeListener(this);
		
		getSupportLoaderManager().initLoader(0, null, this);
	}

	@Override
	protected void onDestroy() {
		getSupportLoaderManager().destroyLoader(0);

		super.onDestroy();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = { WeatherProvider.KEY_CITY_ID };
		String where = WeatherProvider.KEY_CITY_ID;

		return new CursorLoader(this, WeatherProvider.CONTENT_URI_WEATHER, projection, where, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mPagerAdapter.swapCursor(data);
		
		refreshGUI();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mPagerAdapter.swapCursor(null);
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int arg0) {
		refreshGUI();
	}
	
	@Override
	public void onClick(View v) {
		if (v != null) {
			if (v.getId() == R.id.weather_map) {
				ViewPager pager = (ViewPager) findViewById(R.id.view_pager);
				if (pager != null) {
					int position = pager.getCurrentItem();
					long cityId = mPagerAdapter.getCityId(position);
					if (cityId != 0) {
						Intent i = new Intent(getBaseContext(), MapActivity.class);
						i.putExtra(IpcConstants.EXTRA_CITY_ID, cityId);
						startActivity(i);
					}
				}
			} else if (v.getId() == R.id.city_name) {
				Intent i = new Intent(getBaseContext(), CityActivity.class);
				startActivity(i);
			}
		}
	}

	@Override
	public void onCurrentClick() {
		ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
		
//		Workaround: FragmentStatePagerAdapter's instantiateItem() method will return 
//		the reference of fragment instead of calling getItem() method to create a new 
//		one if it exists already.
		CurrentWeather fragment = (CurrentWeather) 
				mPagerAdapter.instantiateItem(viewPager, viewPager.getCurrentItem());
		if (fragment != null && fragment.isAdded()) {
			fragment.showCurrent();
			
			long cityId = mPagerAdapter.getCityId(viewPager.getCurrentItem());

			updateBackground(cityId, 0, WeatherParser.FLAG_CURRENT);
		}		
	}

	@Override
	public void onForecastClick(int day) {
		ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
		
//		Workaround: FragmentStatePagerAdapter's instantiateItem() method will return 
//		the reference of fragment instead of calling getItem() method to create a new 
//		one if it exists already.
		CurrentWeather fragment = (CurrentWeather) 
				mPagerAdapter.instantiateItem(viewPager, viewPager.getCurrentItem());
		if (fragment != null && fragment.isAdded()) {
			fragment.showForecast(day);
			
			long cityId = mPagerAdapter.getCityId(viewPager.getCurrentItem());

			updateBackground(cityId, day, WeatherParser.FLAG_FORECAST);
		}
	}

	private void refreshGUI() {
		ForecastWeather fragment = (ForecastWeather) 
				getSupportFragmentManager().findFragmentById(R.id.forecast_fragment);
		if (fragment != null && fragment.isAdded()) {
			ViewPager pager = (ViewPager) findViewById(R.id.view_pager);
			if (pager != null) {
				long cityId = mPagerAdapter.getCityId(pager.getCurrentItem());
					
				fragment.refresh(cityId);
				
				updateBackground(cityId, 0, WeatherParser.FLAG_CURRENT);
				updateCityName(cityId);
			}
		}
		
		DotViewPagerIndicator indicator = (DotViewPagerIndicator) findViewById(R.id.pager_indicator);
		if (indicator != null) {
			indicator.dataChanged();
		}
	}

	private void updateBackground(long cityId, int day, int flag) {
		String current = null;
		String forecast = null;
		Cursor cursor = null;
		try {
			String[] projection = { WeatherProvider.KEY_CURRENT, WeatherProvider.KEY_FORECAST };
			String where = WeatherProvider.KEY_CITY_ID + " = " + cityId;
			
			cursor = getContentResolver().query(
					WeatherProvider.CONTENT_URI_WEATHER, projection, where, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				current = cursor.getString(cursor.getColumnIndexOrThrow(WeatherProvider.KEY_CURRENT));
				forecast = cursor.getString(cursor.getColumnIndexOrThrow(WeatherProvider.KEY_FORECAST));
			}
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "The column does not exist");
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		
		int weatherId = 0;
		if (current != null && forecast != null) {
			switch (flag) {
			case WeatherParser.FLAG_CURRENT:
				try {
					String[] elements = current.split("\\|");
					if (elements.length == WeatherParser.COUNT_ELEMENTS_CURRENT) {
						weatherId = Integer.parseInt(elements[0]);
					}
				} catch (PatternSyntaxException e) {
					Log.e(TAG, "the syntax of the supplied regular expression is not valid");
				} catch (NumberFormatException e) {
					Log.e(TAG, "string cannot be parsed as an integer value");
				}
				break;
			case WeatherParser.FLAG_FORECAST:
				try {
					String[] elements = forecast.split(";");
					if (elements.length == 3 && 0 <= day && day < 3) {
						String[] values = elements[day].split("\\|");
						if (values.length == WeatherParser.COUNT_ELEMENTS_FORECAST) {
							weatherId = Integer.parseInt(values[0]);
						}
					}
				} catch (PatternSyntaxException e) {
					Log.e(TAG, "the syntax of the supplied regular expression is not valid");
				} catch (NumberFormatException e) {
					Log.e(TAG, "string cannot be parsed as an integer value");
				}
				break;
			}
		}
		
		View v = findViewById(R.id.swipe_refresh);
		WeatherInfoAdapter.setBackgroundOfView(v, weatherId);
	}
	
	private void updateCityName(long cityId) {
		String name = null;
		String country = null;
		Cursor cursor = null;
		try {
			String[] projection = { 
					WeatherProvider.KEY_CITY_ID, WeatherProvider.KEY_NAME, WeatherProvider.KEY_COUNTRY };
			String where = WeatherProvider.KEY_CITY_ID + " = " + cityId;
			
			cursor = getContentResolver().query(
					WeatherProvider.CONTENT_URI_CITIES, projection, where, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				name = cursor.getString(cursor.getColumnIndexOrThrow(WeatherProvider.KEY_NAME));
				country = cursor.getString(cursor.getColumnIndexOrThrow(WeatherProvider.KEY_COUNTRY));
			}
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "The column does not exist");
		} finally {
			if (cursor != null & !cursor.isClosed()) {
				cursor.close();
			}
		}
		
		TextView textView = (TextView) findViewById(R.id.city_name);
		if (name != null && country != null) {
			String raw = name + " " +country;
			
			SpannableString spanned = new SpannableString(raw);
			spanned.setSpan(new RelativeSizeSpan(0.5f), 
					name.length() + 1, raw.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			
			textView.setText(spanned);
		} else {
			textView.setText(R.string.placeholder);
		}
	}

}