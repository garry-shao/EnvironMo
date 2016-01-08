package org.qmsos.environmo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qmsos.environmo.fragment.CurrentFragment;
import org.qmsos.environmo.fragment.ForecastFragment;
import org.qmsos.environmo.fragment.ForecastFragment.OnWeatherClickListener;
import org.qmsos.environmo.util.UtilPagerAdapter;
import org.qmsos.environmo.util.UtilPagerIndicator;
import org.qmsos.environmo.util.UtilRefreshLayout;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.AppCompatActivity;
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
implements LoaderCallbacks<Cursor>, OnPageChangeListener, OnWeatherClickListener {

	private UtilPagerAdapter adapter;

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
		String[] projection = { CityProvider.KEY_ID, CityProvider.KEY_CITYID, CityProvider.KEY_NAME };
		String where = CityProvider.KEY_CITYID;

		return new CursorLoader(this, CityProvider.CONTENT_URI, projection, where, null, null);
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
	public void onWeatherClick(int day) {
		ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
		
//		Workaround: FragmentStatePagerAdapter's instantiateItem() method will return 
//		the reference of fragment instead of calling getItem() method to create a new 
//		one if it exists already.
		CurrentFragment fragment = (CurrentFragment) 
				adapter.instantiateItem(viewPager, viewPager.getCurrentItem());
		if (fragment != null && fragment.isAdded()) {
			fragment.showForecast(day);
			
			long cityId = adapter.getId(viewPager.getCurrentItem());
			setBackground(cityId, day);
		}
	}

	private void refreshGUI() {
		ForecastFragment fragment = (ForecastFragment) 
				getSupportFragmentManager().findFragmentById(R.id.forecast_fragment);
		if (fragment != null && fragment.isAdded()) {
			ViewPager pager = (ViewPager) findViewById(R.id.view_pager);
			if (pager != null) {
				int position = pager.getCurrentItem();
				long cityid = adapter.getId(position);
				if (cityid != 0) {
					fragment.refresh(cityid);
					
					updateBackground(cityid);
					updateCityName(cityid);
				}
				
			}
		}
		UtilPagerIndicator indicator = (UtilPagerIndicator) findViewById(R.id.pager_indicator);
		if (indicator != null) {
			indicator.invalidate();
		}
	}

	private void setBackground(long cityId, int day) {
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

}