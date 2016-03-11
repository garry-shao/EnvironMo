package org.qmsos.weathermo.fragment;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.cordova.ConfigXmlParser;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewImpl;
import org.apache.cordova.engine.SystemWebView;
import org.apache.cordova.engine.SystemWebViewEngine;
import org.json.JSONException;
import org.qmsos.weathermo.R;
import org.qmsos.weathermo.contract.ProviderContract.CityEntity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Show weather map.
 *
 */
public class WeatherMap extends Fragment implements CordovaInterface, LoaderCallbacks<Cursor> {

	private static final String KEY_CITY_ID = "KEY_CITY_ID";
	private static final String START_URL = "file:///android_asset/www/index.html";
	private static final String TAG = WeatherMap.class.getSimpleName();

	// The base url that connects to remote server, can add parameter of layer to this.
	private String mBaseLoadUrl = null;

	private SystemWebView mSystemWebView;
	private CordovaWebView mCordovaWebView;

	private ExecutorService mThreadPool;
	private CordovaPlugin mPermissionResultCallback;

	private CordovaPlugin mActivityResultCallback;
	private int mActivityResultRequestCode;

	/**
	 * Create a new instance that shows weather map.
	 * 
	 * @param cityId
	 *            The city id of the map that currently showing.
	 * @return The created fragment instance.
	 */
	public static WeatherMap newInstance(long cityId) {
		Bundle args = new Bundle();
		args.putLong(KEY_CITY_ID, cityId);
		
		WeatherMap fragment = new WeatherMap();
		fragment.setArguments(args);
		
		return fragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_weather_map, container, false);
		
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		mSystemWebView = (SystemWebView) view.findViewById(R.id.weather_map);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mThreadPool = Executors.newCachedThreadPool();
		
		ConfigXmlParser parser = new ConfigXmlParser();
        parser.parse(getContext());
        
        mCordovaWebView = new CordovaWebViewImpl(new SystemWebViewEngine(mSystemWebView));
		mCordovaWebView.init(this, parser.getPluginEntries(), parser.getPreferences());
		
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
	public ExecutorService getThreadPool() {
		return mThreadPool;
	}

	@Override
	public Object onMessage(String id, Object data) {
		if ("exit".equals(id)) {
            getActivity().finish();
        }
        return null;
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		
		if (mPermissionResultCallback != null) {
            try {
				mPermissionResultCallback.onRequestPermissionResult(
						requestCode, permissions, grantResults);
			} catch (JSONException e) {
				Log.e(TAG, e.getMessage());
			}
            mPermissionResultCallback = null;
        }
	}

	@Override
	public boolean hasPermission(String permission) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			int result = getContext().checkSelfPermission(permission);
			return PackageManager.PERMISSION_GRANTED == result;
		} else {
			return true;
		}
	}

	@Override
	public void requestPermission(CordovaPlugin plugin, int requestCode, String permission) {
		mPermissionResultCallback = plugin;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        	String[] permissions = { permission };
        	requestPermissions(permissions, requestCode);		
        }		
	}

	@Override
	public void requestPermissions(CordovaPlugin plugin, int requestCode, String[] permissions) {
		mPermissionResultCallback = plugin;
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        	requestPermissions(permissions, requestCode);
        }
	}

	@Override
	public void setActivityResultCallback(CordovaPlugin plugin) {
	    // Cancel any previously pending activity.
        if (mActivityResultCallback != null) {
            mActivityResultCallback.onActivityResult(
            		mActivityResultRequestCode, Activity.RESULT_CANCELED, null);
        }
        mActivityResultCallback = plugin;
	}

	@Override
	public void startActivityForResult(CordovaPlugin command, Intent intent, int requestCode) {
		setActivityResultCallback(command);
        try {
            startActivityForResult(intent, requestCode);
        } catch (RuntimeException e) { // E.g.: ActivityNotFoundException
            mActivityResultCallback = null;
            
            Log.e(TAG, e.getMessage());
            
            throw e;
        }
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		long cityId = getArguments().getLong(KEY_CITY_ID, -1L);
		
		String[] projection = { CityEntity.LONGITUDE, CityEntity.LATITUDE };
		String where = CityEntity.CITY_ID + " = " + cityId;
		
		return new CursorLoader(getContext(), CityEntity.CONTENT_URI, projection, where, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		StringBuilder builder = new StringBuilder(START_URL + "?");
		
		if (data != null && data.moveToFirst()) {
			double longitude = data.getDouble(data.getColumnIndexOrThrow(CityEntity.LONGITUDE));
			double latitude = data.getDouble(data.getColumnIndexOrThrow(CityEntity.LATITUDE));
			
			int zoomlevel = 4;
			
			builder.append("&lon=");
			builder.append(longitude);
			builder.append("&lat=");
			builder.append(latitude);
			builder.append("&zoom=");
			builder.append(zoomlevel);
		}
		
		mBaseLoadUrl = builder.toString();
		loadMap(null);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

	/**
	 * Show weather map with to specified layer.
	 * 
	 * @param layer
	 *            The name of the layer that will be showed, NULL means default layer.
	 */
	public void loadMap(String layer) {
		StringBuilder builder = new StringBuilder(mBaseLoadUrl);
		if (layer != null) {
			builder.append("&l=");
			builder.append(layer);
		}
		
		if (mCordovaWebView != null) {
			mCordovaWebView.loadUrlIntoView(builder.toString(), false);
		}
	}

}
