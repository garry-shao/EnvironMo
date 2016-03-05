package org.qmsos.weathermo;

import org.qmsos.weathermo.datamodel.City;
import org.qmsos.weathermo.fragment.CityCandidates;
import org.qmsos.weathermo.fragment.CityCandidates.OnStartQueryListener;
import org.qmsos.weathermo.fragment.CityList;
import org.qmsos.weathermo.util.IntentConstants;
import org.qmsos.weathermo.widget.CityCandidatesRecyclerViewAdapter.OnInsertCityClickedListener;
import org.qmsos.weathermo.widget.CityListRecyclerViewAdapter.OnDeleteCityClickedListener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

public class CityActivity extends AppCompatActivity 
implements OnDeleteCityClickedListener, OnStartQueryListener, OnInsertCityClickedListener {

	private static final String FRAGMENT_TAG_CITY_LIST = "FRAGMENT_TAG_CITY_LIST";
	private static final String FRAGMENT_TAG_CITY_CANDIDATES = "FRAGMENT_TAG_CITY_CANDIDATES";

	private MessageReceiver mMessageReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_city);
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		mMessageReceiver = new MessageReceiver();
		
		FragmentManager manager = getSupportFragmentManager();
		
		CityList cityList = null;
		Fragment fragmentCityList = manager.findFragmentByTag(FRAGMENT_TAG_CITY_LIST);
		if (fragmentCityList != null) {
			cityList = (CityList) fragmentCityList;
		} else {
			cityList = new CityList();
			
			FragmentTransaction transaction = manager.beginTransaction();
			transaction.add(R.id.fragment_container, cityList, FRAGMENT_TAG_CITY_LIST);
			transaction.commit();
		}
		
		CityCandidates cityCandidates = null;
		Fragment fragmentCityCandidates = manager.findFragmentByTag(FRAGMENT_TAG_CITY_CANDIDATES);
		if (fragmentCityCandidates != null) {
			cityCandidates = (CityCandidates) fragmentCityCandidates;
		}
		
		if (cityList != null && cityCandidates != null) {
			FragmentTransaction transaction = manager.beginTransaction();
			transaction.hide(cityList);
			transaction.commit();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(IntentConstants.ACTION_QUERY_EXECUTED);
		filter.addAction(IntentConstants.ACTION_INSERT_EXECUTED);
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filter);
	}

	@Override
	protected void onPause() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
		
		super.onPause();
	}

	@Override
	public void onSubViewButtonClicked() {
		FragmentManager manager = getSupportFragmentManager();
		
		Fragment fragmentCityList = manager.findFragmentByTag(FRAGMENT_TAG_CITY_LIST);
		Fragment fragmentAddCity = manager.findFragmentByTag(FRAGMENT_TAG_CITY_CANDIDATES);
		if (fragmentCityList != null && fragmentAddCity == null) {
			CityList cityList = (CityList) fragmentCityList;
			CityCandidates cityCandidates = new CityCandidates();
			
			FragmentTransaction transaction = manager.beginTransaction();
			transaction.hide(cityList);
			transaction.add(R.id.fragment_container, cityCandidates, FRAGMENT_TAG_CITY_CANDIDATES);
			transaction.addToBackStack(null);
			transaction.commit();
		}
	}

	@Override
	public void onDeleteCityClicked(long cityId) {
		Intent i = new Intent(this, WeatherService.class);
		i.setAction(IntentConstants.ACTION_DELETE_CITY);
		i.putExtra(IntentConstants.EXTRA_CITY_ID, cityId);
		
		startService(i);
	}

	@Override
	public void onInsertCityClicked(City city) {
		Intent i = new Intent(this, WeatherService.class);
		i.setAction(IntentConstants.ACTION_INSERT_CITY);
		i.putExtra(IntentConstants.EXTRA_INSERT_CITY, city);
		
		startService(i);		
	}

	@Override
	public void onStartQuery(String cityName) {
		Intent i = new Intent(getBaseContext(), WeatherService.class);
		i.setAction(IntentConstants.ACTION_QUERY_CITY);
		i.putExtra(IntentConstants.EXTRA_CITY_NAME, cityName);
		
		startService(i);		
	}

	private void onQueryResultReceived(String result) {
		FragmentManager manager = getSupportFragmentManager();
		Fragment fragmentCityCandidates = manager.findFragmentByTag(FRAGMENT_TAG_CITY_CANDIDATES);
		if (fragmentCityCandidates != null) {
			((CityCandidates) fragmentCityCandidates).swapData(result);
		}
	}

	private void onInsertExecuted(boolean flag) {
		String text = flag ? "Succeeded" : "Failed";
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Private Receiver used when receiving local broadcast from Service thread.
	 */
	private class MessageReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action != null) {
				if (action.equals(IntentConstants.ACTION_QUERY_EXECUTED)) {
					boolean flag = intent.getBooleanExtra(IntentConstants.EXTRA_QUERY_EXECUTED, false);
					if (flag) {
						String result = intent.getStringExtra(IntentConstants.EXTRA_QUERY_RESULT);
						
						onQueryResultReceived(result);
					}
				} else if (action.equals(IntentConstants.ACTION_INSERT_EXECUTED)) {
					boolean flag = intent.getBooleanExtra(IntentConstants.EXTRA_INSERT_EXECUTED, false);
					
					onInsertExecuted(flag);
				}
			}
		}
	}

}
