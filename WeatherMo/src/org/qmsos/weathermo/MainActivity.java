package org.qmsos.weathermo;

import org.qmsos.weathermo.fragment.CurrentWeather;
import org.qmsos.weathermo.fragment.ForecastWeather;
import org.qmsos.weathermo.fragment.ForecastWeather.OnForecastViewClickedListener;
import org.qmsos.weathermo.fragment.WeatherPagerAdapter;
import org.qmsos.weathermo.provider.WeatherContract.CityEntity;
import org.qmsos.weathermo.provider.WeatherContract.WeatherEntity;
import org.qmsos.weathermo.resources.BackgroundFactory;
import org.qmsos.weathermo.util.IntentConstants;
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
 * 
 */
public class MainActivity extends AppCompatActivity 
implements LoaderCallbacks<Cursor>, OnPageChangeListener, 
		OnRefreshListener, OnClickListener, OnForecastViewClickedListener {
	
	private static final String TAG = MainActivity.class.getSimpleName();

	private SwipeRefreshLayout mRefreshLayout;
	private WeatherPagerAdapter mPagerAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
		mRefreshLayout.setOnRefreshListener(this);

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
		String[] projection = { WeatherEntity.CITY_ID };

		return new CursorLoader(this, WeatherEntity.CONTENT_URI, projection, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mPagerAdapter.swapCursor(data);
		
		refreshGui();
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
		refreshGui();
	}
	
	@Override
	public void onRefresh() {
		final Intent intent = new Intent(this, WeatherService.class);
		intent.setAction(IntentConstants.ACTION_REFRESH_WEATHER);
		
		// make animation here 
		int animationTimeInMillis = 1000;
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				mRefreshLayout.setRefreshing(false);

				startService(intent);
			}
		}, animationTimeInMillis);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.weather_map) {
			ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
			if (viewPager != null) {
				long cityId = mPagerAdapter.getCityId(viewPager.getCurrentItem());
				if (cityId != 0) {
					Intent i = new Intent(this, MapActivity.class);
					i.putExtra(IntentConstants.EXTRA_CITY_ID, cityId);
					startActivity(i);
				}
			}
		} else if (v.getId() == R.id.city_name) {
			Intent i = new Intent(this, CityActivity.class);
			startActivity(i);
		}
	}

	@Override
	public void onForecastViewClicked(int day) {
		ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
		if ((viewPager != null) && (mPagerAdapter.getCount() > 0)) {
//			Workaround: FragmentStatePagerAdapter's instantiateItem() method will return 
//			the reference of fragment instead of calling getItem() method to create a new 
//			one if it exists already.
			CurrentWeather fragment = (CurrentWeather) 
					mPagerAdapter.instantiateItem(viewPager, viewPager.getCurrentItem());
			if (fragment != null && fragment.isAdded()) {
				fragment.showWeather(day);
				
				long cityId = mPagerAdapter.getCityId(viewPager.getCurrentItem());
				updateBackground(cityId, day);
			}
		}
	}

	private void refreshGui() {
		ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
		if (viewPager != null) {
			ForecastWeather fragment = (ForecastWeather) 
					getSupportFragmentManager().findFragmentById(R.id.forecast_fragment);
			if (fragment != null && fragment.isAdded()) {
				long cityId = mPagerAdapter.getCityId(viewPager.getCurrentItem());
				
				Bundle args = new Bundle();
				args.putLong(IntentConstants.KEY_CITY_ID, cityId);
				fragment.getLoaderManager().restartLoader(0, args, fragment);
				
				updateBackground(cityId, 0);
				updateCityName(cityId);
			}
		}
		
		DotViewPagerIndicator indicator = (DotViewPagerIndicator) findViewById(R.id.pager_indicator);
		if (indicator != null) {
			indicator.dataChanged();
		}
	}

	private void updateBackground(long cityId, int day) {
		String current = null;
		String[] forecasts = new String[WeatherParser.COUNT_FORECAST_DAYS];
		Cursor cursor = null;
		try {
			String[] projection = { WeatherEntity.CURRENT, WeatherEntity.FORECAST1, 
					WeatherEntity.FORECAST2, WeatherEntity.FORECAST3 };
			String where = WeatherEntity.CITY_ID + " = " + cityId;
			
			cursor = getContentResolver().query(
					WeatherEntity.CONTENT_URI, projection, where, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				current = cursor.getString(cursor.getColumnIndexOrThrow(WeatherEntity.CURRENT));
				
				forecasts[0] = cursor.getString(cursor.getColumnIndexOrThrow(WeatherEntity.FORECAST1));
				forecasts[1] = cursor.getString(cursor.getColumnIndexOrThrow(WeatherEntity.FORECAST2));
				forecasts[2] = cursor.getString(cursor.getColumnIndexOrThrow(WeatherEntity.FORECAST3));
			}
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "The column does not exist");
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		
		int weatherId;
		if (day == 0) {
			weatherId = WeatherParser.getCurrentWeatherId(current);
		} else {
			weatherId = WeatherParser.getForecastWeatherId(forecasts[day - 1]);
		}
		
		View v = findViewById(R.id.swipe_refresh);
		BackgroundFactory.setBackground(v, weatherId);
	}
	
	private void updateCityName(long cityId) {
		String name = null;
		String country = null;
		Cursor cursor = null;
		try {
			String[] projection = { CityEntity.CITY_ID, CityEntity.CITY_NAME, CityEntity.COUNTRY };
			String where = CityEntity.CITY_ID + " = " + cityId;
			
			cursor = getContentResolver().query(
					CityEntity.CONTENT_URI, projection, where, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				name = cursor.getString(cursor.getColumnIndexOrThrow(CityEntity.CITY_NAME));
				country = cursor.getString(cursor.getColumnIndexOrThrow(CityEntity.COUNTRY));
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