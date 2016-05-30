package org.qmsos.weathermo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.qmsos.weathermo.contract.IntentContract;
import org.qmsos.weathermo.contract.ProviderContract;
import org.qmsos.weathermo.contract.ProviderContract.CityEntity;
import org.qmsos.weathermo.contract.ProviderContract.WeatherEntity;
import org.qmsos.weathermo.datamodel.City;
import org.qmsos.weathermo.util.WeatherParser;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

/**
 * Update weather info and city info in background.
 * 
 *
 */
public class WeatherService extends IntentService {

	private static final String TAG = WeatherService.class.getSimpleName();
	
	/**
	 * Implementation of the superclass constructor.
	 * 
	 * @param name
	 *            Used to name the worker thread, important only for debugging.
	 */
	public WeatherService(String name) {
		super(name);
	}

	/**
	 * Default empty constructor.
	 */
	public WeatherService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String action = intent.getAction();
		if (action == null) {
			return;
		}
		
		if (action.equals(IntentContract.ACTION_REFRESH_WEATHER_AUTO)) {
			boolean flagAuto = intent.getBooleanExtra(IntentContract.EXTRA_REFRESH_WEATHER_AUTO, false);
			
			scheduleAutoRefresh(flagAuto);
			
			if (flagAuto && checkConnection()) {
				int[] flags = { 
						Contract.FLAG_CURRENT_WEATHER, 
						Contract.FLAG_CURRENT_UV_INDEX, 
						Contract.FLAG_FORECAST_HOURLY };
				
				executeRefreshWeather(flags);
			}
		} else if (action.equals(IntentContract.ACTION_REFRESH_WEATHER_MANUAL)) {
			if (checkConnection()) {
				int[] flags = { 
						Contract.FLAG_CURRENT_WEATHER, 
						Contract.FLAG_CURRENT_UV_INDEX, 
						Contract.FLAG_FORECAST_DAILY };
				
				executeRefreshWeather(flags);
			}
		} else if (action.equals(IntentContract.ACTION_SEARCH_CITY)) {
			Intent localIntent = new Intent(IntentContract.ACTION_SEARCH_EXECUTED);
			if (checkConnection()) {
				String cityName = intent.getStringExtra(IntentContract.EXTRA_CITY_NAME);
				
				String result = executeSearchCity(cityName);
				if (result != null) {
					localIntent.putExtra(IntentContract.EXTRA_SEARCH_EXECUTED, true);
					localIntent.putExtra(IntentContract.EXTRA_SEARCH_RESULT, result);
				} else {
					localIntent.putExtra(IntentContract.EXTRA_SEARCH_EXECUTED, false);
				}
			} else {
				localIntent.putExtra(IntentContract.EXTRA_SEARCH_EXECUTED, false);
			}
			
			LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
		} else if (action.equals(IntentContract.ACTION_INSERT_CITY)) {
			City city = intent.getParcelableExtra(IntentContract.EXTRA_INSERT_CITY);
			if (city != null) {
				boolean flag = executeInsertCity(city);
				
				Intent localIntent = new Intent(IntentContract.ACTION_INSERT_EXECUTED);
				localIntent.putExtra(IntentContract.EXTRA_INSERT_EXECUTED, flag);
				
				LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
			}
		} else if (action.equals(IntentContract.ACTION_DELETE_CITY)) {
			long cityId = intent.getLongExtra(IntentContract.EXTRA_CITY_ID, -1);
			if (cityId != -1) {
				executeDeleteCity(cityId);
			}
		}
	}

	/**
	 * Proceed the execution of refreshing weather from remote server.
	 * 
	 * @param flags
	 *            Parameters used to control the refresh behavior, the valid flags
	 *            are in class {@link Contract}.
	 */
	private void executeRefreshWeather(int[] flags) {
		if (flags == null) {
			return;
		}
		
		long[] cityIds = getMonitoringCities();
		if (cityIds == null) {
			return;
		}
		
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		for (int i = 0; i < cityIds.length; i++) {
			long cityId = cityIds[i];
			
			String where = WeatherEntity.CITY_ID + " = " + cityId;
			
			ContentValues value = refreshInstance(cityId, flags);
			
			operations.add(
					ContentProviderOperation
					.newUpdate(WeatherEntity.CONTENT_URI)
					.withSelection(where, null)
					.withValues(value)
					.build());
		}
		
		try {
			getContentResolver().applyBatch(ProviderContract.AUTHORITY, operations);
		} catch (RemoteException e) {
			Log.e(TAG, "Error when batch update weathers: " + e.getMessage());
		} catch (OperationApplicationException e) {
			Log.e(TAG, "Error when batch update weathers: " + e.getMessage());
		}
	}

	/**
	 * Proceed the execution of searching city id with city name.
	 * 
	 * @param cityName
	 *            The phrase of city name which would be used to search for.
	 * @return The raw, unparsed results from remote server.
	 */
	private String executeSearchCity(String cityName) {
		if (cityName == null) {
			return null;
		}
		
		String request = assembleRequest(Contract.FLAG_SEARCH_CITY, 0, cityName);
		String result = download(request);
		
		return result;
	}
	
	/**
	 * Proceed the execution of inserting new city to provider.
	 * 
	 * @param city
	 *            The instance of city that will be inserted.
	 * @return Whether the operation succeed, TRUE means succeed.
	 */
	private boolean executeInsertCity(City city) {
		if (city == null) {
			return false;
		}
		
		ContentResolver resolver = getContentResolver();
		
		boolean cityExisting = false;
		Cursor cursor = null;
		try {
			String where = CityEntity.CITY_ID + " = " + city.getCityId();
			
			cursor = resolver.query(CityEntity.CONTENT_URI, null, where, null, null);
			if (cursor != null && cursor.moveToNext()) {
				cityExisting = true;
			}
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		
		if (!cityExisting) {
			ContentValues cityValues = new ContentValues();
			cityValues.put(CityEntity.CITY_ID, city.getCityId());
			cityValues.put(CityEntity.CITY_NAME, city.getCityName());
			cityValues.put(CityEntity.COUNTRY, city.getCountry());
			cityValues.put(CityEntity.LONGITUDE, city.getLongitude());
			cityValues.put(CityEntity.LATITUDE, city.getLatitude());
			
			ContentValues weatherValues = new ContentValues();
			weatherValues.put(CityEntity.CITY_ID, city.getCityId());
			
			resolver.insert(CityEntity.CONTENT_URI, cityValues);
			resolver.insert(WeatherEntity.CONTENT_URI, weatherValues);
			
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Proceed the execution of deleting specified city from provider.
	 * 
	 * @param cityId
	 *            The id of city that would be deleted.
	 * @return Whether the operation succeed, TRUE means succeed.
	 */
	private boolean executeDeleteCity(long cityId) {
		String where = CityEntity.CITY_ID + " = " + cityId;
		
		int rows1 = getContentResolver().delete(CityEntity.CONTENT_URI, where, null);
		int rows2 = getContentResolver().delete(WeatherEntity.CONTENT_URI, where, null);
		
		return (rows1 > 0 && rows2 > 0) ? true : false;
	}

	/**
	 * Schedule the behavior of the alarm that invoked repeatedly to execute automatic refresh.
	 * 
	 * @param flag
	 *            TRUE when setting up the alarm, FALSE when canceling the alarm.
	 */
	private void scheduleAutoRefresh(boolean flag) {
		int requestCode = 1;
		
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

		PendingIntent alarmIntent = PendingIntent.getBroadcast(this, requestCode,
				new Intent(IntentContract.ACTION_REFRESH_ALARM), PendingIntent.FLAG_UPDATE_CURRENT);
		
		if (flag) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			int frequency = Integer.parseInt(prefs.getString(
					getString(R.string.PREF_REFRESH_AUTO_FREQUENCY), 
					getString(R.string.frequency_values_default)));
			
			long intervalMillis = frequency * AlarmManager.INTERVAL_HOUR;
			long timeToRefresh = SystemClock.elapsedRealtime() + intervalMillis;
			
			alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 
					timeToRefresh, intervalMillis, alarmIntent);
		} else {
			alarmManager.cancel(alarmIntent);
		}
	}

	/**
	 * This is the single instance of refreshing weather from remote server. should
	 * be considered is a component of operation, should not be used alone.
	 * 
	 * @param cityId
	 *            The city id of which would be executed upon.
	 * @param flags
	 *            Parameters used to control the refresh behavior, the valid flags
	 *            are in class {@link Contract}. PS: these flags should
	 *            be passed from the calling method.
	 * @return The parsed value that containing the refreshed data for further 
	 *         execution, as the refreshing should be a transaction operation.
	 */
	private ContentValues refreshInstance(long cityId, int[] flags) {
		if (cityId == 0L || flags == null) {
			return null;
		}
		
		ContentValues value = new ContentValues();
		for (int i = 0; i < flags.length; i++) {
			int flag = flags[i];
			
			String request = assembleRequest(flag, cityId, null);
			String result = download(request);
			
			switch (flag) {
			case Contract.FLAG_CURRENT_WEATHER:
				String parsedCurrent = WeatherParser.parseRawToCurrent(result);
				if (parsedCurrent != null) {
					value.put(WeatherEntity.CURRENT, parsedCurrent);
				}
				break;
			case Contract.FLAG_CURRENT_UV_INDEX:
				double parsedUvIndex = WeatherParser.parseRawToUvIndex(result);
				if (parsedUvIndex > 0) {
					value.put(WeatherEntity.UV_INDEX, parsedUvIndex);
				}
				break;
			case Contract.FLAG_FORECAST_HOURLY:
				String[] parsedForecastHourly = WeatherParser.parseRawToForecastsHourly(result);
				if (parsedForecastHourly != null) {
					value.put(WeatherEntity.FORECAST1, parsedForecastHourly[0]);
					value.put(WeatherEntity.FORECAST2, parsedForecastHourly[1]);
					value.put(WeatherEntity.FORECAST3, parsedForecastHourly[2]);
				}
				break;
			case Contract.FLAG_FORECAST_DAILY:
				String[] parsedForecastDaily = WeatherParser.parseRawToForecastsDaily(result);
				if (parsedForecastDaily != null) {
					value.put(WeatherEntity.FORECAST1, parsedForecastDaily[0]);
					value.put(WeatherEntity.FORECAST2, parsedForecastDaily[1]);
					value.put(WeatherEntity.FORECAST3, parsedForecastDaily[2]);
				}
				break;
			}
		}
		
		return value;
	}

	/**
	 * Download from remote server for results.
	 * 
	 * @param request
	 *            The URL string of request, <b>should comply with URL-Encoding</b>.
	 * @return Results of this request, NULL otherwise.
	 */
	private String download(String request) {
		if (request == null) {
			return null;
		}
		
		URL url = null;
		try {
			url = new URL(request);
		} catch (MalformedURLException e) {
			Log.e(TAG, "The provided URL is Malformed. " + e.getMessage());
			
			return null;
		}
		
		HttpURLConnection httpConnection = null;
		try {
			httpConnection = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			Log.e(TAG, "Error opening connection. " + e.getMessage());
			
			return null;
		}
		
		StringBuilder builder = new StringBuilder();
		try {
			int responseCode = httpConnection.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				InputStream inStream = httpConnection.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
				
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
			}
		} catch (IOException e) {
			Log.e(TAG, "Error reading from the connection. " + e.getMessage());
			
			return null;
		} finally {
			if (httpConnection != null) {
				httpConnection.disconnect();
			}
		}
		
		return builder.toString();
	}

	/**
	 * Assemble the request string that would be sent to remote server. 
	 * 
	 * @param flag
	 *            The flag of the refresh behavior, the valid flags
	 *            are in class {@link Contract}.
	 * @param cityId
	 *            The id of city.
	 * @param cityName
	 *            The name of city, only used when search city by name.
	 * @return The assembled string.
	 */
	private String assembleRequest(int flag, long cityId, String cityName) {
		switch (flag) {
		case Contract.FLAG_CURRENT_WEATHER:
			return "http://api.openweathermap.org/data/2.5/" 
					+ "weather?" + "id=" + String.valueOf(cityId)
					+ "&units=" + "metric"
					+ "&appid=" + Contract.API_KEY;
		case Contract.FLAG_CURRENT_UV_INDEX:
			String geo = assembleRequestExtraUv(cityId);
			if (geo != null) {
				return "http://api.owm.io/air/1.0/uvi/"
						+ "current?" + geo
						+ "&appid=" + Contract.API_KEY;
			} else {
				return null;
			}
		case Contract.FLAG_FORECAST_DAILY:
			int days = WeatherParser.FORECAST_IN_DAYS + 1;
			
			return "http://api.openweathermap.org/data/2.5/" 
					+ "forecast/daily?" + "id=" + String.valueOf(cityId) 
					+ "&cnt=" + days
					+ "&units=" + "metric"
					+ "&appid=" + Contract.API_KEY;
		case Contract.FLAG_FORECAST_HOURLY:
			int count = WeatherParser.FORECAST_IN_DAYS * Contract.DATAPOINTS_IN_ONE_DAY;
			
			return "http://api.openweathermap.org/data/2.5/" 
					+ "forecast?" + "id=" + String.valueOf(cityId) 
					+ "&cnt=" + count
					+ "&units=" + "metric"
					+ "&appid=" + Contract.API_KEY;
		case Contract.FLAG_SEARCH_CITY:
			String encodedCityName = assembleRequestEncode(cityName);
			if (encodedCityName != null) {
				return "http://api.openweathermap.org/data/2.5/"
						+ "find?" + "q=" + encodedCityName
						+ "&type=" + "like"
						+ "&units=" + "metric"
						+ "&appid=" + Contract.API_KEY;
			} else {
				return null;
			}
		default:
			return null;
		}
	}
	
	/**
	 * Assemble the extra section of request string that would be sent to remote 
	 * server. Only used when refresh ultra violet radiation value.
	 * <br>
	 * <br>
	 * <b>This method should only be used in assembling request.</b>
	 * 
	 * @param cityId
	 *            The id of city.
	 * @return The assembled extra section.
	 */
	private String assembleRequestExtraUv(long cityId) {
		double latitude = 200.0f;
		double longitude = 200.0f;
		
		Cursor cursor = null;
		try {
			String[] projection = { CityEntity.CITY_ID, CityEntity.LATITUDE,  CityEntity.LONGITUDE };
			String where =  CityEntity.CITY_ID + " = " + cityId;
			
			cursor = getContentResolver().query(
					 CityEntity.CONTENT_URI, projection, where, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(CityEntity.LATITUDE));
				longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(CityEntity.LONGITUDE));
			}
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "The column does not exist. " + e.getMessage());
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		
		if (latitude > 90.0f || latitude < -90.0f || longitude > 180.0f || longitude < -180.0f) {
			return null;
		} else {
			return "lat=" + latitude + "&lon=" + longitude;
		}
	}

	/**
	 * Encode the city name that would be used as search parameter with <b>URL-Encoding</b>.
	 * <br>
	 * <br>
	 * <b>This method should only be used in assembling request.</b>
	 * 
	 * @param cityName
	 *            The city name that to be encoded.
	 * @return The encoded URL-Encoding complied string.
	 */
	private String assembleRequestEncode(String cityName) {
		String encodedCityName = null;
		try {
			encodedCityName = URLEncoder.encode(cityName, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "The encoding is not supported. " + e.getMessage());
			
			return null;
		}
		return encodedCityName;
	}

	/**
	 * Get the available city ids that currently monitoring.
	 * 
	 * @return The array that containing ids of the cities monitoring. 
	 */
	private long[] getMonitoringCities() {
		Cursor cursor = null;
		try {
			String[] projection = { WeatherEntity.CITY_ID };
			String where = WeatherEntity.CITY_ID;
			
			cursor = getContentResolver().query(WeatherEntity.CONTENT_URI, projection, where, null, null);
			if (cursor == null) {
				return null;
			}
			
			int i = 0;
			long[] cityIds = new long[cursor.getCount()];
			while (cursor.moveToNext()) {
				long cityId = cursor.getLong(cursor.getColumnIndexOrThrow(WeatherEntity.CITY_ID));
				
				cityIds[i] = cityId;
				i++;
			}
			
			return cityIds;
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "The column does not exist. " + e.getMessage());
			
			return null;
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
	}

	/**
	 * Check if there is a valid connection to Internet.
	 * 
	 * @return TRUE if a valid connection exists, FALSE otherwise.
	 */
	private boolean checkConnection() {
		ConnectivityManager manager = 
				(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		if (info != null && info.isConnected()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Containing contract of service behavior that out class scope should not see.
	 */
	private static class Contract {
		
		/**
		 * API key used to contact remote server.
		 */
		static final String API_KEY = "054dcbb7bea48220bc5d30d5fc53932e";
		
		/**
		 * How many data points in a whole day(24H) when performing forecast by hours.
		 */
		static final int DATAPOINTS_IN_ONE_DAY = 8;
		
		/**
		 * Used to refresh current weather section from remote server.
		 */
		static final int FLAG_CURRENT_WEATHER = 0x01;
		
		/**
		 * Used to refresh current uv index section from remote server.
		 */
		static final int FLAG_CURRENT_UV_INDEX = 0x02;
		
		/**
		 * Used to refresh forecast weather section from remote server.
		 * <br>
		 * <br>
		 * This should never be used along by {@link #FLAG_FORECAST_HOURLY}, 
		 * these two are exclusive event defined by remote server.
		 * 
		 */
		static final int FLAG_FORECAST_DAILY = 0x10;
		
		/**
		 * Used to refresh forecast weather section from remote server.
		 * <br>
		 * <br>
		 * This should never be used along by {@link #FLAG_FORECAST_DAILY}, 
		 * these two are exclusive event defined by remote server.
		 */
		static final int FLAG_FORECAST_HOURLY = 0x11;
		
		/**
		 * Used to search city id by name from remote server.
		 */
		static final int FLAG_SEARCH_CITY = 0x20;
	}

}
