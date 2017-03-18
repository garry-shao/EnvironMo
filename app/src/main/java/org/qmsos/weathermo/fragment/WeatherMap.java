package org.qmsos.weathermo.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.apache.cordova.ConfigXmlParser;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewImpl;
import org.apache.cordova.engine.SystemWebView;
import org.apache.cordova.engine.SystemWebViewEngine;
import org.qmsos.weathermo.R;
import org.qmsos.weathermo.contract.ProviderContract.CityEntity;

/**
 * Show weather map.
 */
public class WeatherMap extends BaseCordovaFragment implements LoaderCallbacks<Cursor> {

    private static final String KEY_CITY_ID = "KEY_CITY_ID";
    private static final String KEY_LAYER = "KEY_LAYER";

    private SystemWebView mSystemWebView;
    private CordovaWebView mCordovaWebView;

    /**
     * Create a new instance that shows weather map.
     *
     * @param cityId
     *            The city id of the map that currently showing.
     * @param layer
     *            The layer of the map that currently showing.
     * @return The created fragment instance.
     */
    public static WeatherMap newInstance(long cityId, String layer) {
        Bundle args = new Bundle();
        args.putLong(KEY_CITY_ID, cityId);
        args.putString(KEY_LAYER, layer);

        WeatherMap fragment = new WeatherMap();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_weather_map, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSystemWebView = (SystemWebView) view.findViewById(R.id.weather_map);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ConfigXmlParser parser = new ConfigXmlParser();
        parser.parse(getContext());
        
        SystemWebViewEngine systemWebViewEngine = new SystemWebViewEngine(mSystemWebView);
        mCordovaWebView = new CordovaWebViewImpl(systemWebViewEngine);
        mCordovaWebView.init(this,
                parser.getPluginEntries(),
                parser.getPreferences());

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onDestroyView() {
        getLoaderManager().destroyLoader(0);

        if (mSystemWebView != null && mSystemWebView.getCordovaWebView() != null) {
            mSystemWebView.getCordovaWebView().handleDestroy();
        }

        super.onDestroyView();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        long cityId = getArguments().getLong(KEY_CITY_ID, -1L);

        String[] projection = { CityEntity.LONGITUDE, CityEntity.LATITUDE };
        String where = CityEntity.CITY_ID + " = " + cityId;

        return new CursorLoader(getContext(),
                CityEntity.CONTENT_URI,
                projection,
                where,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            double latitude = data.getDouble(
                    data.getColumnIndexOrThrow(CityEntity.LATITUDE));
            double longitude = data.getDouble(
                    data.getColumnIndexOrThrow(CityEntity.LONGITUDE));

            loadView(latitude, longitude);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    /**
     * Load specified map area from remote server to WebView.
     *
     * @param latitude
     *            The latitude of the center of map area.
     * @param longitude
     *            The longitude of the center of map area.
     */
    private void loadView(double latitude, double longitude) {
        StringBuilder urlBuilder = new StringBuilder("file:///android_asset/www/index.html");
        urlBuilder.append("?");
        urlBuilder.append("&lat=");
        urlBuilder.append(latitude);
        urlBuilder.append("&lon=");
        urlBuilder.append(longitude);
//        Internally, set the zoom level to fixed value, may change later.
        urlBuilder.append("&zoom=");
        urlBuilder.append(7);

        String layer = getArguments().getString(KEY_LAYER);
        if (layer != null) {
            urlBuilder.append("&l=");
            urlBuilder.append(layer);
        }

        String url = urlBuilder.toString();

        if (mCordovaWebView != null) {
            mCordovaWebView.loadUrlIntoView(url, false);
        }
    }
}