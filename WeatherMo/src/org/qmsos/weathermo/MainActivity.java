package org.qmsos.weathermo;

import org.qmsos.weathermo.fragment.CityName;
import org.qmsos.weathermo.fragment.CityName.OnCityNameViewClickedListener;
import org.qmsos.weathermo.fragment.WeatherCurrent;
import org.qmsos.weathermo.fragment.WeatherForecast;
import org.qmsos.weathermo.fragment.WeatherForecast.OnForecastViewClickedListener;
import org.qmsos.weathermo.fragment.WeatherPagerAdapter;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

/**
 * Main activity of EnvironMo.
 * 
 * 
 */
public class MainActivity extends AppCompatActivity 
implements LoaderCallbacks<Cursor>, OnPageChangeListener, OnRefreshListener, 
		OnClickListener, OnCityNameViewClickedListener, OnForecastViewClickedListener {
	
	private static final int LOAD_ID_MAIN_INTERFACE = 0;
	private static final int LOAD_ID_ASYNC_BACKGROUND = 1;
	
	private SwipeRefreshLayout mRefreshLayout;
	private WeatherPagerAdapter mPagerAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
		mRefreshLayout.setOnRefreshListener(this);

		TextView weatherMap = (TextView) findViewById(R.id.weather_map);
		weatherMap.setOnClickListener(this);
	
		mPagerAdapter = new WeatherPagerAdapter(getSupportFragmentManager(), this, null);
		ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
		viewPager.setAdapter(mPagerAdapter);
		
		DotViewPagerIndicator pagerIndicator = (DotViewPagerIndicator) findViewById(R.id.pager_indicator);
		pagerIndicator.setViewPager(viewPager);
		pagerIndicator.setOnPageChangeListener(this);
		
		getSupportLoaderManager().initLoader(LOAD_ID_MAIN_INTERFACE, null, this);
		getSupportLoaderManager().initLoader(LOAD_ID_ASYNC_BACKGROUND, null, this);
	}

	@Override
	protected void onDestroy() {
		getSupportLoaderManager().destroyLoader(LOAD_ID_MAIN_INTERFACE);
		getSupportLoaderManager().destroyLoader(LOAD_ID_ASYNC_BACKGROUND);

		super.onDestroy();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = null;
		
		switch (id) {
		case LOAD_ID_MAIN_INTERFACE:
			projection = new String[] { WeatherEntity.CITY_ID };
			
			return new CursorLoader(this, WeatherEntity.CONTENT_URI, projection, null, null, null);
		case LOAD_ID_ASYNC_BACKGROUND:
			long cityId;
			int day;
			if (args != null) {
				cityId = args.getLong(IntentConstants.KEY_CITY_ID);
				day = args.getInt(IntentConstants.KEY_DAY, -1);
			} else {
				cityId = 0L;
				day = -1;
			}
			
			// 0 means current, 1 means tomorrow, etc.
			switch (day) {
			case 0:
				projection = new String[] { WeatherEntity.CURRENT };
				break;
			case 1:
				projection = new String[] { WeatherEntity.FORECAST1 };
				break;
			case 2:
				projection = new String[] { WeatherEntity.FORECAST2 };
				break;
			case 3:
				projection = new String[] { WeatherEntity.FORECAST3 };
				break;
			}
			
			String where = WeatherEntity.CITY_ID + " = " + cityId;
		
			return new CursorLoader(this, WeatherEntity.CONTENT_URI, projection, where, null, null);
		default:
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		switch (loader.getId()) {
		case LOAD_ID_MAIN_INTERFACE:
			mPagerAdapter.swapCursor(data);
			
			DotViewPagerIndicator indicator = (DotViewPagerIndicator) findViewById(R.id.pager_indicator);
			if (indicator != null) {
				indicator.dataChanged();
			}
			
			Bundle args = createReloadParameters(0);
			reloadFragmentCityName(args);
			reloadFragmentForecast(args);
			reloadBackgroundImage(args);
			
			return;
		case LOAD_ID_ASYNC_BACKGROUND:
			int weatherId;
			
			if (data != null && data.moveToFirst()) {
				String[] columnNames = data.getColumnNames();
				if (columnNames.length == 1) {
					String weatherRaw = data.getString(data.getColumnIndexOrThrow(columnNames[0]));
					weatherId = WeatherParser.getWeatherId(weatherRaw);
				} else {
					weatherId = 0;
				}
			} else {
				weatherId = 0;
			}
			
			View v = findViewById(R.id.swipe_refresh);
			BackgroundFactory.setBackground(v, weatherId);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mPagerAdapter.swapCursor(null);
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
	}

	@Override
	public void onPageSelected(int position) {
		DotViewPagerIndicator indicator = (DotViewPagerIndicator) findViewById(R.id.pager_indicator);
		if (indicator != null) {
			indicator.dataChanged();
		}
		
		Bundle args = createReloadParameters(0);
		reloadFragmentCityName(args);
		reloadFragmentForecast(args);
		reloadBackgroundImage(args);
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
		if (v.getId() != R.id.weather_map) {
			return;
		}
		
		ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
		if (viewPager == null) {
			return;
		}
		
		long cityId = mPagerAdapter.getCityId(viewPager.getCurrentItem());
		if (cityId != 0L) {
			Intent i = new Intent(this, MapActivity.class);
			i.putExtra(IntentConstants.EXTRA_CITY_ID, cityId);
			startActivity(i);
		}
	}

	@Override
	public void onCityNameViewClicked() {
		Intent i = new Intent(this, CityActivity.class);
		startActivity(i);
	}

	@Override
	public void onForecastViewClicked(int day) {
		reloadFragmentCurrent(day);
		
		Bundle args = createReloadParameters(day);
		reloadBackgroundImage(args);
	}

	/**
	 * Reload the current Fragment that shows in ViewPager with specified data, so this
	 * fragment can show forecast info.<br><br> 
	 * 
	 * NOTICE: there is loophole here: FragmentStatePagerAdapter's instantiateItem() method 
	 * will return the reference of fragment instead of calling getItem() method to create a new 
	 * one if it exists already.
	 * 
	 * @param day
	 *            Which day will be shown, 0 means current, 1 means tomorrow, etc.
	 */
	private void reloadFragmentCurrent(int day) {
		WeatherCurrent weatherCurrent = null;
		
		ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
		if ((viewPager != null) && (mPagerAdapter.getCount() > 0)) {
			weatherCurrent = (WeatherCurrent) 
					mPagerAdapter.instantiateItem(viewPager, viewPager.getCurrentItem());
		}
		
		if (weatherCurrent != null && weatherCurrent.isAdded()) {
			weatherCurrent.showWeather(day);
		}
	}

	/**
	 * Reload the city name Fragment with specified data.
	 * 
	 * @param args
	 *            This bundle should at least contains the city id 
	 *            of which wants to be shown.
	 */
	private void reloadFragmentCityName(Bundle args) {
		CityName cityName = (CityName) 
				getSupportFragmentManager().findFragmentById(R.id.fragment_city_name);
		if (cityName != null && cityName.isAdded()) {
			cityName.getLoaderManager().restartLoader(0, args, cityName);
		}
	}
	
	/**
	 * Reload the weather forecast Fragment with specified data.
	 * 
	 * @param args
	 *            This bundle should at least contains the city id 
	 *            of which wants to be shown.
	 */
	private void reloadFragmentForecast(Bundle args) {
		WeatherForecast weatherForecast = (WeatherForecast) 
				getSupportFragmentManager().findFragmentById(R.id.fragment_weather_forecast);
		if (weatherForecast != null && weatherForecast.isAdded()) {
			weatherForecast.getLoaderManager().restartLoader(0, args, weatherForecast);
		}
	}
	
	/**
	 * Reload background with new image that describing current weather.
	 * 
	 * @param args
	 *            This bundle should at least contains the city id and which 
	 *            day(0 means current, 1 means tomorrow, etc) that wants 
	 *            to be shown.
	 */
	private void reloadBackgroundImage(Bundle args) {
		getSupportLoaderManager().restartLoader(LOAD_ID_ASYNC_BACKGROUND, args, this);
	}
	
	/**
	 * Create the bundle that containing data to be used to reload GUI.<br><br>
	 * 
	 * NOTICE: the current city id is contained automatically if valid.
	 * 
	 * @param day
	 *            Which day that wants to show(0 means current, 1 means tomorrow, etc).
	 * @return The created bundle.
	 */
	private Bundle createReloadParameters(int day) {
		long cityId = getCityIdOfCurrentView();
		if (cityId <= 0) {
			return null;
		} else {
			Bundle args = new Bundle();
			args.putLong(IntentConstants.KEY_CITY_ID, cityId);
			args.putInt(IntentConstants.KEY_DAY, day);
			
			return args;
		}
	}
	
	/**
	 * Get the city id of current fragment that shown in ViewPager.
	 * 
	 * @return The city id or 0L if invalid.
	 */
	private long getCityIdOfCurrentView() {
		ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
		if (viewPager != null) {
			return mPagerAdapter.getCityId(viewPager.getCurrentItem());
		} else {
			return 0L;
		}
	}

}