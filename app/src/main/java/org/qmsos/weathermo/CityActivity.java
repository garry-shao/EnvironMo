package org.qmsos.weathermo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import org.qmsos.weathermo.contract.IntentContract;
import org.qmsos.weathermo.fragment.CityList;
import org.qmsos.weathermo.fragment.CitySearch;
import org.qmsos.weathermo.model.City;
import org.qmsos.weathermo.widget.CityListRecyclerViewAdapter;
import org.qmsos.weathermo.widget.CitySearchRecyclerViewAdapter;

/**
 * Manage cities currently monitoring.
 */
public class CityActivity extends AppCompatActivity
        implements CityListRecyclerViewAdapter.OnDeleteCityClickedListener,
        CitySearchRecyclerViewAdapter.OnInsertCityClickedListener,
        CitySearch.OnStartSearchListener {

    private static final String FRAGMENT_TAG_CITY_LIST = "FRAGMENT_TAG_CITY_LIST";
    private static final String FRAGMENT_TAG_CITY_SEARCH = "FRAGMENT_TAG_CITY_SEARCH";

    private MessageReceiver mMessageReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mMessageReceiver = new MessageReceiver();

        FragmentManager manager = getSupportFragmentManager();
        Fragment fragmentCityList = manager.findFragmentByTag(FRAGMENT_TAG_CITY_LIST);
        if (fragmentCityList == null) {
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.fragment_container,
                    new CityList(),
                    FRAGMENT_TAG_CITY_LIST);
            transaction.commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(IntentContract.ACTION_SEARCH_EXECUTED);
        filter.addAction(IntentContract.ACTION_INSERT_EXECUTED);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mMessageReceiver, filter);
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mMessageReceiver);

        super.onPause();
    }

    @Override
    public void onSubViewButtonClicked() {
        FragmentManager manager = getSupportFragmentManager();

        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.fragment_container,
                new CitySearch(),
                FRAGMENT_TAG_CITY_SEARCH);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onDeleteCityClicked(long cityId) {
        Intent i = new Intent(this, WeatherService.class);
        i.setAction(IntentContract.ACTION_DELETE_CITY);
        i.putExtra(IntentContract.EXTRA_CITY_ID, cityId);

        startService(i);
    }

    @Override
    public void onInsertCityClicked(City city) {
        Intent i = new Intent(this, WeatherService.class);
        i.setAction(IntentContract.ACTION_INSERT_CITY);
        i.putExtra(IntentContract.EXTRA_INSERT_CITY, city);

        startService(i);
    }

    @Override
    public void onStartSearch(String cityName) {
        Intent i = new Intent(getBaseContext(), WeatherService.class);
        i.setAction(IntentContract.ACTION_SEARCH_CITY);
        i.putExtra(IntentContract.EXTRA_CITY_NAME, cityName);

        startService(i);
    }

    private void onSearchResultReceived(String result) {
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragmentCitySearch = manager.findFragmentByTag(FRAGMENT_TAG_CITY_SEARCH);
        if (fragmentCitySearch != null) {
            ((CitySearch) fragmentCitySearch).swapData(result);
        }
    }

    private void onInsertExecuted(boolean flag) {
        View view = findViewById(R.id.layout_activity_city);
        if (view != null) {
            int snackbarActionTextColor = ContextCompat.getColor(this,
                    R.color.snackbar_action_text_color);
            int snackbarBackgroundColor = ContextCompat.getColor(this,
                    R.color.snackbar_background_color);

            int resId;
            if (flag) {
                resId = R.string.snackbar_succeed;
            } else {
                resId = R.string.snackbar_failed;
            }

            Snackbar snackbar = Snackbar.make(view, resId, Snackbar.LENGTH_LONG);
            snackbar.setActionTextColor(snackbarActionTextColor);
            snackbar.getView().setBackgroundColor(snackbarBackgroundColor);
            snackbar.show();
        }
    }

    /**
     * Private Receiver used when receiving local broadcast from Service thread.
     */
    private class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }

            if (action.equals(IntentContract.ACTION_SEARCH_EXECUTED)) {
                boolean isSearchExecuted = intent.getBooleanExtra(
                        IntentContract.EXTRA_SEARCH_EXECUTED, false);
                if (isSearchExecuted) {
                    String result = intent.getStringExtra(
                            IntentContract.EXTRA_SEARCH_RESULT);

                    onSearchResultReceived(result);
                }
            } else if (action.equals(IntentContract.ACTION_INSERT_EXECUTED)) {
                boolean isInsertExecuted = intent.getBooleanExtra(
                        IntentContract.EXTRA_INSERT_EXECUTED, false);

                onInsertExecuted(isInsertExecuted);
            }
        }
    }
}