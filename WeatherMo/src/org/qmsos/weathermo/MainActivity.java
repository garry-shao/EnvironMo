package org.qmsos.weathermo;

import org.qmsos.weathermo.contract.IntentContract;
import org.qmsos.weathermo.contract.LoaderContract;
import org.qmsos.weathermo.contract.ProviderContract.WeatherEntity;
import org.qmsos.weathermo.fragment.CityHeader;
import org.qmsos.weathermo.fragment.CityHeader.OnCityHeaderClickedListener;
import org.qmsos.weathermo.fragment.WeatherDetails;
import org.qmsos.weathermo.fragment.WeatherSummary;
import org.qmsos.weathermo.fragment.WeatherSummary.OnSummaryClickedListener;
import org.qmsos.weathermo.fragment.WeatherDetailsPagerAdapter;
import org.qmsos.weathermo.resources.BackgroundFactory;
import org.qmsos.weathermo.util.WeatherParser;
import org.qmsos.weathermo.widget.DotViewPagerIndicator;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
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
import android.support.v7.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * Main activity of EnvironMo.
 * 
 * 
 */
public class MainActivity extends AppCompatActivity 
implements LoaderCallbacks<Cursor>, OnPageChangeListener, OnSharedPreferenceChangeListener, 
		OnCityHeaderClickedListener, OnSummaryClickedListener {
	
	private static final int LOADER_MAIN_INTERFACE = 0x01;
	private static final int LOADER_ASYNC_BACKGROUND = 0x02;
	
	private WeatherDetailsPagerAdapter mPagerAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
		refreshLayout.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				animateRefreshing();
			}
		});

		View buttonWeatherMap = findViewById(R.id.button_map);
		buttonWeatherMap.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				loadWeatherMap();
			}
		});

		mPagerAdapter = new WeatherDetailsPagerAdapter(getSupportFragmentManager(), this, null);
		ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
		viewPager.setAdapter(mPagerAdapter);
		
		DotViewPagerIndicator pagerIndicator = (DotViewPagerIndicator) findViewById(R.id.pager_indicator);
		pagerIndicator.setViewPager(viewPager);
		pagerIndicator.setOnPageChangeListener(this);
		
		getSupportLoaderManager().initLoader(LOADER_MAIN_INTERFACE, null, this);
		getSupportLoaderManager().initLoader(LOADER_ASYNC_BACKGROUND, null, this);
		
		scheduleService();
	}

	@Override
	protected void onDestroy() {
		getSupportLoaderManager().destroyLoader(LOADER_MAIN_INTERFACE);
		getSupportLoaderManager().destroyLoader(LOADER_ASYNC_BACKGROUND);

		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_main, menu);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		
		switch (item.getItemId()) {
		case R.id.menu_main_preference:
			Intent i = new Intent(this, PreferenceActivity.class);
			startActivity(i);
			
			return true;
		default:
			return false;
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = null;
		
		switch (id) {
		case LOADER_MAIN_INTERFACE:
			projection = new String[] { WeatherEntity.CITY_ID };
			
			return new CursorLoader(this, WeatherEntity.CONTENT_URI, projection, null, null, null);
		case LOADER_ASYNC_BACKGROUND:
			long cityId;
			int day;
			if (args != null) {
				cityId = args.getLong(LoaderContract.KEY_CITY_ID);
				day = args.getInt(LoaderContract.KEY_DAY, -1);
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
		case LOADER_MAIN_INTERFACE:
			mPagerAdapter.swapCursor(data);
			
			DotViewPagerIndicator indicator = (DotViewPagerIndicator) findViewById(R.id.pager_indicator);
			if (indicator != null) {
				indicator.dataChanged();
			}
			
			Bundle args = createReloadParameters(0);
			reloadHeader(args);
			reloadSummary(args);
			reloadBackgroundImage(args);
			
			return;
		case LOADER_ASYNC_BACKGROUND:
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
		
		int day;
		WeatherDetails details = getCurrentDetailsFragment();
		if (details != null) {
			day = details.getCurrentShowingDay();
		} else {
			day = 0;
		}
		
		Bundle args = createReloadParameters(day);
		reloadHeader(args);
		reloadSummary(args);
		reloadBackgroundImage(args);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(getString(R.string.PREF_REFRESH_AUTO_TOGGLE)) || 
				key.equals(getString(R.string.PREF_REFRESH_AUTO_FREQUENCY))) {
			
			scheduleService();
		}		
	}

	@Override
	public void onCityHeaderClicked() {
		Intent i = new Intent(this, CityActivity.class);
		startActivity(i);
	}

	@Override
	public void onSummaryClicked(int day) {
		reloadDetails(day);
		
		Bundle args = createReloadParameters(day);
		reloadBackgroundImage(args);
	}

	/**
	 * Reload the weather details Fragment that shows in ViewPager with specified data, so this
	 * fragment can show forecast info.<br><br> 
	 * 
	 * NOTICE: there is loophole here: FragmentStatePagerAdapter's instantiateItem() method 
	 * will return the reference of fragment instead of calling getItem() method to create a new 
	 * one if it exists already.
	 * 
	 * @param day
	 *            Which day will be shown, 0 means current, 1 means tomorrow, etc.
	 */
	private void reloadDetails(int day) {
		WeatherDetails details = getCurrentDetailsFragment();
		if (details != null && details.isAdded()) {
			details.showDetails(day);
		}
	}

	/**
	 * Reload the header Fragment of current city with specified data.
	 * 
	 * @param args
	 *            This bundle should at least contains the city id 
	 *            of which wants to be shown.
	 */
	private void reloadHeader(Bundle args) {
		CityHeader header = (CityHeader) 
				getSupportFragmentManager().findFragmentById(R.id.fragment_current_city);
		if (header != null && header.isAdded()) {
			header.getLoaderManager().restartLoader(0, args, header);
		}
	}
	
	/**
	 * Reload the weather summary Fragment with specified data.
	 * 
	 * @param args
	 *            This bundle should at least contains the city id 
	 *            of which wants to be shown.
	 */
	private void reloadSummary(Bundle args) {
		WeatherSummary summary = (WeatherSummary) 
				getSupportFragmentManager().findFragmentById(R.id.fragment_weather_summary);
		if (summary != null && summary.isAdded()) {
			summary.getLoaderManager().restartLoader(0, args, summary);
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
		getSupportLoaderManager().restartLoader(LOADER_ASYNC_BACKGROUND, args, this);
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
		long cityId = getCurrentCityId();
		if (cityId <= 0) {
			return null;
		} else {
			Bundle args = new Bundle();
			args.putLong(LoaderContract.KEY_CITY_ID, cityId);
			args.putInt(LoaderContract.KEY_DAY, day);
			
			return args;
		}
	}
	
	/**
	 * Get the city id of the fragment that currently showing in ViewPager.
	 * 
	 * @return The city id or 0L if invalid.
	 */
	private long getCurrentCityId() {
		ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
		if (viewPager != null) {
			return mPagerAdapter.getCityId(viewPager.getCurrentItem());
		} else {
			return 0L;
		}
	}
	
	/**
	 * Get the fragment that currently showing in ViewPager.
	 * 
	 * @return The fragment that currently showing or else null.
	 */
	private WeatherDetails getCurrentDetailsFragment() {
		ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
		if ((viewPager != null) && (mPagerAdapter.getCount() > 0)) {
			return (WeatherDetails)	mPagerAdapter.instantiateItem(viewPager, viewPager.getCurrentItem());
		} else {
			return null;
		}
	}

	/**
	 * Schedule the state of background service based on user preferences.
	 */
	private void scheduleService() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean isAutoFresh = prefs.getBoolean(getString(R.string.PREF_REFRESH_AUTO_TOGGLE), false);
		
		Intent intent = new Intent(this, WeatherService.class);
		intent.setAction(IntentContract.ACTION_REFRESH_WEATHER_AUTO);
		intent.putExtra(IntentContract.EXTRA_REFRESH_WEATHER_AUTO, isAutoFresh);
		startService(intent);
	}

	/**
	 * Load weather map of the city currently showing.
	 */
	private void loadWeatherMap() {
		ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
		if (viewPager == null) {
			return;
		}
		
		long cityId = mPagerAdapter.getCityId(viewPager.getCurrentItem());
		if (cityId != 0L) {
			Intent i = new Intent(this, MapActivity.class);
			i.putExtra(IntentContract.EXTRA_CITY_ID, cityId);
			startActivity(i);
		}
	}

	/**
	 * Animate refreshing action.
	 */
	private void animateRefreshing() {
		final SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
		if (refreshLayout == null) {
			return;
		}
		
		final Intent intent = new Intent(this, WeatherService.class);
		intent.setAction(IntentContract.ACTION_REFRESH_WEATHER_MANUAL);
		
		// make animation here 
		int animationTimeInMillis = 1000;
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				refreshLayout.setRefreshing(false);

				startService(intent);
			}
		}, animationTimeInMillis);
	}

}