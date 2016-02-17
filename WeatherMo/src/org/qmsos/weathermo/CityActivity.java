package org.qmsos.weathermo;

import org.qmsos.weathermo.fragment.AddCity;
import org.qmsos.weathermo.fragment.AddCity.OnInputDoneListener;
import org.qmsos.weathermo.fragment.CityList;
import org.qmsos.weathermo.util.City;
import org.qmsos.weathermo.util.IntentConstants;
import org.qmsos.weathermo.widget.CityRecyclerViewAdapter.OnInsertCityClickedListener;
import org.qmsos.weathermo.widget.CursorRecyclerViewAdapter.OnViewHolderClickedListener;

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
implements OnViewHolderClickedListener, OnInputDoneListener, OnInsertCityClickedListener {

	private static final String FRAGMENT_TAG_CITY_LIST = "FRAGMENT_TAG_CITY_LIST";
	private static final String FRAGMENT_TAG_ADD_CITY = "FRAGMENT_TAG_ADD_CITY";

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
		
		AddCity addCity = null;
		Fragment fragmentAddCity = manager.findFragmentByTag(FRAGMENT_TAG_ADD_CITY);
		if (fragmentAddCity != null) {
			addCity = (AddCity) fragmentAddCity;
		}
		
		if (cityList != null && addCity != null) {
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
	public void onAddMoreCity() {
		FragmentManager manager = getSupportFragmentManager();
		
		Fragment fragmentCityList = manager.findFragmentByTag(FRAGMENT_TAG_CITY_LIST);
		Fragment fragmentAddCity = manager.findFragmentByTag(FRAGMENT_TAG_ADD_CITY);
		if (fragmentCityList != null && fragmentAddCity == null) {
			CityList cityList = (CityList) fragmentCityList;
			AddCity addCity = new AddCity();
			
			FragmentTransaction transaction = manager.beginTransaction();
			transaction.hide(cityList);
			transaction.add(R.id.fragment_container, addCity, FRAGMENT_TAG_ADD_CITY);
			transaction.addToBackStack(null);
			transaction.commit();
		}
	}

	@Override
	public void onDeleteCity(long cityId) {
		Intent i = new Intent(this, WeatherService.class);
		i.setAction(IntentConstants.ACTION_DELETE_CITY);
		i.putExtra(IntentConstants.EXTRA_CITY_ID, cityId);
		
		startService(i);
	}

	@Override
	public void onInputDone(String text) {
		Intent i = new Intent(getBaseContext(), WeatherService.class);
		i.setAction(IntentConstants.ACTION_QUERY_CITY);
		i.putExtra(IntentConstants.EXTRA_CITY_NAME, text);
		
		startService(i);		
	}

	@Override
	public void onInsertCity(City city) {
		Intent i = new Intent(this, WeatherService.class);
		i.setAction(IntentConstants.ACTION_INSERT_CITY);
		i.putExtra(IntentConstants.EXTRA_INSERT_CITY, city);
		
		startService(i);		
	}

	private void onQueryResultReceived(String result) {
		FragmentManager manager = getSupportFragmentManager();
		Fragment fragmentAddCity = manager.findFragmentByTag(FRAGMENT_TAG_ADD_CITY);
		if (fragmentAddCity != null) {
			((AddCity) fragmentAddCity).swapData(result);
		}
	}

	private void onInsertExecuted(boolean flag) {
		String text = flag ? "Succeeded" : "Failed";
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

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
