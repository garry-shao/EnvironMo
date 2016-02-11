package org.qmsos.weathermo;

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
import org.qmsos.weathermo.util.IpcConstants;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.OnMenuItemClickListener;
import android.util.Log;
import android.view.MenuItem;

public class MapActivity extends AppCompatActivity implements OnMenuItemClickListener, CordovaInterface {

	private static final String START_URL = "file:///android_asset/www/index.html";
	
	private static final String TAG = MapActivity.class.getSimpleName();
	
	private SystemWebView mSystemWebView;
	private CordovaWebView mCordovaWebView;
	private ExecutorService mThreadPool;
	private CordovaPlugin mActivityResultCallback;
	private CordovaPlugin mPermissionResultCallback;	
	private int mActivityResultRequestCode;

	private long mCityId = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.setTitle(R.string.activity_weather_map);
		toolbar.inflateMenu(R.menu.menu_map_layer);
		toolbar.setOnMenuItemClickListener(this);
		
		ConfigXmlParser parser = new ConfigXmlParser();
        parser.parse(this);
		
        mThreadPool = Executors.newCachedThreadPool();
        
		mSystemWebView = (SystemWebView) findViewById(R.id.weather_map_view);
		mCordovaWebView = new CordovaWebViewImpl(new SystemWebViewEngine(mSystemWebView));
		mCordovaWebView.init(this, parser.getPluginEntries(), parser.getPreferences());

		mCityId = getIntent().getLongExtra(IpcConstants.EXTRA_CITY_ID, -1);
		
		mCordovaWebView.loadUrl(assembleStartUrl(null, mCityId));
	}

	@Override
	protected void onDestroy() {
		if (mSystemWebView != null && mSystemWebView.getCordovaWebView() != null) {
			mSystemWebView.getCordovaWebView().handleDestroy();
		}
		
		super.onDestroy();
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_layer_precipitation:
			mCordovaWebView.loadUrl(assembleStartUrl("precipitation", mCityId));
			return true;
		case R.id.menu_layer_rain:
			mCordovaWebView.loadUrl(assembleStartUrl("rain", mCityId));
			return true;
		case R.id.menu_layer_snow:
			mCordovaWebView.loadUrl(assembleStartUrl("snow", mCityId));
			return true;
		case R.id.menu_layer_clouds:
			mCordovaWebView.loadUrl(assembleStartUrl("clouds", mCityId));
			return true;
		case R.id.menu_layer_pressure:
			mCordovaWebView.loadUrl(assembleStartUrl("pressure", mCityId));
			return true;
		case R.id.menu_layer_temperature:
			mCordovaWebView.loadUrl(assembleStartUrl("temp", mCityId));
			return true;
		case R.id.menu_layer_windspeed:
			mCordovaWebView.loadUrl(assembleStartUrl("wind", mCityId));
			return true;
		default:
			return false;
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		
        if (mPermissionResultCallback != null) {
            try {
				mPermissionResultCallback.onRequestPermissionResult(
						requestCode, permissions, grantResults);
			} catch (JSONException e) {
				e.printStackTrace();
			}
            mPermissionResultCallback = null;
        }
	}

	@Override
	public void startActivityForResult(CordovaPlugin command, Intent intent, int requestCode) {
        setActivityResultCallback(command);
        try {
            startActivityForResult(intent, requestCode);
        } catch (RuntimeException e) { // E.g.: ActivityNotFoundException
            mActivityResultCallback = null;
            throw e;
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
	public Activity getActivity() {
		return this;
	}

	@Override
	public Object onMessage(String id, Object data) {
        if ("exit".equals(id)) {
            finish();
        }
        return null;
	}

	@Override
	public ExecutorService getThreadPool() {
		return mThreadPool;
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
	public boolean hasPermission(String permission) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			int result = checkSelfPermission(permission);
			return PackageManager.PERMISSION_GRANTED == result;
		} else {
			return true;
		}
	}

	private String assembleStartUrl(String layer, long cityId) {
		StringBuilder builder;
		if (layer == null) {
			builder = new StringBuilder(START_URL + "?");
		} else {
			builder = new StringBuilder(START_URL + "?l=" + layer);
		}
		
		if (cityId > 0) {
			String extra = assembleStartUrlExtra(cityId);
			if (extra != null) {
				builder.append(extra);
			}
		}
		
		return builder.toString();
	}
	
	private String assembleStartUrlExtra(long cityId) {
		if (cityId <= 0) {
			return null;
		}
		
		StringBuilder builder = new StringBuilder();
		
		Cursor cursor = null;
		try {
			String[] projection = { WeatherProvider.KEY_LONGITUDE, WeatherProvider.KEY_LATITUDE };
			String where = WeatherProvider.KEY_CITY_ID + " = " + cityId;
			
			cursor = getContentResolver().query(
					WeatherProvider.CONTENT_URI_CITIES, projection, where, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				long longitude = cursor.getLong(
						cursor.getColumnIndexOrThrow(WeatherProvider.KEY_LONGITUDE));
				long latitude = cursor.getLong(
						cursor.getColumnIndexOrThrow(WeatherProvider.KEY_LATITUDE));
				
				int zoomlevel = 4;
				
				builder.append("&lon=");
				builder.append(longitude);
				builder.append("&lat=");
				builder.append(latitude);
				builder.append("&zoom=");
				builder.append(zoomlevel);
			}
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "The column does not exist");
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		
		return builder.toString();
	}

}
