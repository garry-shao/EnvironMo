package org.qmsos.environmo;

import java.util.regex.PatternSyntaxException;

import org.qmsos.environmo.fragment.CurrentFragment;
import org.qmsos.environmo.fragment.ForecastFragment;
import org.qmsos.environmo.fragment.ForecastFragment.OnWeatherClickListener;
import org.qmsos.environmo.util.UtilPagerAdapter;
import org.qmsos.environmo.util.UtilPagerIndicator;
import org.qmsos.environmo.util.UtilWeatherParser;

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
implements LoaderCallbacks<Cursor>, OnPageChangeListener, OnWeatherClickListener {
	
	private static final String TAG = MainActivity.class.getSimpleName();

	public static final int DAY_COUNT = 3;
	
	private UtilPagerAdapter adapter;

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
	
		adapter = new UtilPagerAdapter(getSupportFragmentManager(), this, null);
		ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
		viewPager.setAdapter(adapter);
		UtilPagerIndicator pagerIndicator = (UtilPagerIndicator) findViewById(R.id.pager_indicator);
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
		String[] projection = { MainProvider.KEY_CITY_ID };
		String where = MainProvider.KEY_CITY_ID;

		return new CursorLoader(this, MainProvider.CONTENT_URI_WEATHER, projection, where, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);
		
		refreshGUI();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);
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
	public void onCurrentClick() {
		ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
		
//		Workaround: FragmentStatePagerAdapter's instantiateItem() method will return 
//		the reference of fragment instead of calling getItem() method to create a new 
//		one if it exists already.
		CurrentFragment fragment = (CurrentFragment) 
				adapter.instantiateItem(viewPager, viewPager.getCurrentItem());
		if (fragment != null && fragment.isAdded()) {
			fragment.showCurrent();
			
			long cityId = adapter.getId(viewPager.getCurrentItem());

			updateBackground(cityId, 0, UtilWeatherParser.FLAG_CURRENT);
		}		
	}

	@Override
	public void onForecastClick(int day) {
		ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
		
//		Workaround: FragmentStatePagerAdapter's instantiateItem() method will return 
//		the reference of fragment instead of calling getItem() method to create a new 
//		one if it exists already.
		CurrentFragment fragment = (CurrentFragment) 
				adapter.instantiateItem(viewPager, viewPager.getCurrentItem());
		if (fragment != null && fragment.isAdded()) {
			fragment.showForecast(day);
			
			long cityId = adapter.getId(viewPager.getCurrentItem());

			updateBackground(cityId, day, UtilWeatherParser.FLAG_FORECAST);
		}
	}

	private void refreshGUI() {
		ForecastFragment fragment = (ForecastFragment) 
				getSupportFragmentManager().findFragmentById(R.id.forecast_fragment);
		if (fragment != null && fragment.isAdded()) {
			ViewPager pager = (ViewPager) findViewById(R.id.view_pager);
			if (pager != null) {
				int position = pager.getCurrentItem();
				long cityId = adapter.getId(position);
				if (cityId != 0) {
					fragment.refresh(cityId);
					
					updateBackground(cityId, 0, UtilWeatherParser.FLAG_CURRENT);
					updateCityName(cityId);
				}
			}
		}
		
		UtilPagerIndicator indicator = (UtilPagerIndicator) findViewById(R.id.pager_indicator);
		if (indicator != null) {
			indicator.dataChanged();
		}
	}

	private void updateBackground(long cityId, int day, int flag) {
		Cursor cursor = null;
		try {
			String[] projection = { MainProvider.KEY_CURRENT, MainProvider.KEY_FORECAST };
			String where = MainProvider.KEY_CITY_ID + " = " + cityId;
			
			cursor = getContentResolver().query(
					MainProvider.CONTENT_URI_WEATHER, projection, where, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				switch (flag) {
				case UtilWeatherParser.FLAG_CURRENT:
					String current = cursor.getString(
							cursor.getColumnIndexOrThrow(MainProvider.KEY_CURRENT));
					if (current != null) {
						try {
							String[] elements = current.split("\\|");
							if (elements.length == UtilWeatherParser.COUNT_ELEMENTS_CURRENT) {
								int weatherId = Integer.parseInt(elements[0]);
								
								View v = findViewById(R.id.swipe_refresh);
								UtilWeatherParser.setBackgroundOfView(v, weatherId);
							}
						} catch (PatternSyntaxException e) {
							Log.e(TAG, "the syntax of the supplied regular expression is not valid");
						} catch (NumberFormatException e) {
							Log.e(TAG, "string cannot be parsed as an integer value");
						}
					}
					break;
				case UtilWeatherParser.FLAG_FORECAST:
					String forecast = cursor.getString(
							cursor.getColumnIndexOrThrow(MainProvider.KEY_FORECAST));
					if (forecast != null) {
						try {
							String[] elements = forecast.split(";");
							if (elements.length == DAY_COUNT) {
								if (0 <= day && day < DAY_COUNT) {
									String element = elements[day];
									String[] values = element.split("\\|");
									if (values.length == UtilWeatherParser.COUNT_ELEMENTS_FORECAST) {
										int weatherId = Integer.parseInt(values[0]);
										
										View v = findViewById(R.id.swipe_refresh);
										UtilWeatherParser.setBackgroundOfView(v, weatherId);
									}
								}
							}
						} catch (PatternSyntaxException e) {
							Log.e(TAG, "the syntax of the supplied regular expression is not valid");
						} catch (NumberFormatException e) {
							Log.e(TAG, "string cannot be parsed as an integer value");
						}
					}
					break;
				}
			}
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "The column does not exist");
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
	}
	
	private void updateCityName(long cityId) {
		Cursor cursor = null;
		try {
			String[] projection = { MainProvider.KEY_CITY_ID, MainProvider.KEY_NAME, MainProvider.KEY_COUNTRY };
			String where = MainProvider.KEY_CITY_ID + " = " + cityId;
			
			cursor = getContentResolver().query(
					MainProvider.CONTENT_URI_CITIES, projection, where, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				String name = cursor.getString(cursor.getColumnIndexOrThrow(MainProvider.KEY_NAME));
				String country = cursor.getString(cursor.getColumnIndexOrThrow(MainProvider.KEY_COUNTRY));
				
				String raw = name + " " +country;
				
				SpannableString spanned = new SpannableString(raw);
				spanned.setSpan(new RelativeSizeSpan(0.5f), 
						name.length() + 1,raw.length(), 
						Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				
				TextView textView = (TextView) findViewById(R.id.city_name);
				textView.setText(spanned);
			}
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "The column does not exist");
		} finally {
			if (cursor != null & !cursor.isClosed()) {
				cursor.close();
			}
		}
	}

}