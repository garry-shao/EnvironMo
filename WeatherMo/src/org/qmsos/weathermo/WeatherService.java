package org.qmsos.weathermo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.qmsos.weathermo.datamodel.City;
import org.qmsos.weathermo.provider.WeatherContract;
import org.qmsos.weathermo.provider.WeatherContract.CityEntity;
import org.qmsos.weathermo.provider.WeatherContract.WeatherEntity;
import org.qmsos.weathermo.util.IntentConstants;
import org.qmsos.weathermo.util.WeatherParser;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
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
		
		if (action.equals(IntentConstants.ACTION_REFRESH_WEATHER)) {
			if (checkConnection()) {
				int[] flags = { 
						Contract.FLAG_CURRENT_WEATHER, 
						Contract.FLAG_CURRENT_UV_INDEX, 
						Contract.FLAG_FORECAST_HOURLY };
				
				executeRefreshWeather(flags);
			}
		} else if (action.equals(IntentConstants.ACTION_QUERY_CITY)) {
			Intent localIntent = new Intent(IntentConstants.ACTION_QUERY_EXECUTED);
			if (checkConnection()) {
				String cityname = intent.getStringExtra(IntentConstants.EXTRA_CITY_NAME);
				
				String result = executeSearchCity(cityname);
				if (result != null) {
					localIntent.putExtra(IntentConstants.EXTRA_QUERY_EXECUTED, true);
					localIntent.putExtra(IntentConstants.EXTRA_QUERY_RESULT, result);
				} else {
					localIntent.putExtra(IntentConstants.EXTRA_QUERY_EXECUTED, false);
				}
			} else {
				localIntent.putExtra(IntentConstants.EXTRA_QUERY_EXECUTED, false);
			}
			
			LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
		} else if (action.equals(IntentConstants.ACTION_INSERT_CITY)) {
			City city = intent.getParcelableExtra(IntentConstants.EXTRA_INSERT_CITY);
			if (city != null) {
				boolean flag = executeInsertCity(city);
				
				Intent localIntent = new Intent(IntentConstants.ACTION_INSERT_EXECUTED);
				localIntent.putExtra(IntentConstants.EXTRA_INSERT_EXECUTED, flag);
				
				LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
			}
		} else if (action.equals(IntentConstants.ACTION_DELETE_CITY)) {
			long cityId = intent.getLongExtra(IntentConstants.EXTRA_CITY_ID, -1);
			if (cityId != -1) {
				executeDeleteCity(cityId);
			}
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
	 * Proceed the execution of refreshing weather from remote server.
	 * 
	 * @param flags
	 *            Parameters used to control the refresh behavior, the valid flags
	 *            are in class {@link WeatherService.Contract}.
	 */
	private void executeRefreshWeather(int[] flags) {
		if (flags == null) {
			return;
		}
		
		long[] cityIds = getAvailableCityIds();
		if (cityIds == null) {
			return;
		}
		
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		for (int i = 0; i < cityIds.length; i++) {
			long cityId = cityIds[i];
			
			String where = WeatherEntity.CITY_ID + " = " + cityId;
			
			ContentValues value = executeRefreshInstance(cityId, flags);
			
			operations.add(
					ContentProviderOperation
					.newUpdate(WeatherEntity.CONTENT_URI)
					.withSelection(where, null)
					.withValues(value)
					.build());
		}
		
		try {
			getContentResolver().applyBatch(WeatherContract.AUTHORITY, operations);
		} catch (RemoteException e) {
			Log.e(TAG, "Error when batch update weathers: " + e.getMessage());
		} catch (OperationApplicationException e) {
			Log.e(TAG, "Error when batch update weathers: " + e.getMessage());
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
	 *            are in class {@link WeatherService.Contract}. PS: these flags should
	 *            be passed from the calling method.
	 * @return The parsed value that containing the refreshed data for further 
	 *         execution, as the refreshing should be a transaction operation.
	 */
	private ContentValues executeRefreshInstance(long cityId, int[] flags) {
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
			}
		}
		
		return value;
	}

	/**
	 * Get the available city ids that needs monitoring.
	 * 
	 * @return The array that containing available city ids. 
	 */
	private long[] getAvailableCityIds() {
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
			Log.e(TAG, "The column does not exist");
			
			return null;
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
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
		
		boolean flag = false;

		Cursor cursor = null;
		try {
			ContentResolver resolver = getContentResolver();
			String where = CityEntity.CITY_ID + " = " + city.getCityId();
			cursor = resolver.query(CityEntity.CONTENT_URI, null, where, null, null);
			if (cursor != null && !cursor.moveToNext()) {
				ContentValues values = new ContentValues();
				values.put(CityEntity.CITY_ID, city.getCityId());
				
				resolver.insert(WeatherEntity.CONTENT_URI, values);
				
				values.put(CityEntity.CITY_NAME, city.getCityName());
				values.put(CityEntity.COUNTRY, city.getCountry());
				values.put(CityEntity.LONGITUDE, city.getLongitude());
				values.put(CityEntity.LATITUDE, city.getLatitude());
				resolver.insert(CityEntity.CONTENT_URI, values);
				
				flag = true;
			}
		} catch (Exception e) {
			Log.e(TAG, "Error found when adding city to provider");
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		
		return flag;
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
	 * Assemble the request string that would be sent to remote server. 
	 * 
	 * @param flag
	 *            The flag of the refresh behavior, the valid flags
	 *            are in class {@link WeatherService.Contract}.
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
			String geo = assembleRequestExtra(cityId);
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
			return "http://api.openweathermap.org/data/2.5/"
					+ "find?" + "q=" + cityName
					+ "&type=" + "like"
					+ "&units=" + "metric"
					+ "&appid=" + Contract.API_KEY;
		default:
			return null;
		}
	}
	
	/**
	 * Assemble the extra section of request string that would be sent to remote 
	 * server. Only used when refresh ultra violet radiation value.
	 * <br>
	 * <br>
	 * This method should not be used alone.
	 * 
	 * @param cityId
	 *            The id of city.
	 * @return The assembled extra section.
	 */
	private String assembleRequestExtra(long cityId) {
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
			Log.e(TAG, "The column does not exist");
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
	 * Query remote server for specific request.
	 * 
	 * @param request
	 *            The request URL as string.
	 * @return Result of this query.
	 */
	private String download(String request) {
		if (request == null) {
			return null;
		}
		
		StringBuilder builder = new StringBuilder();
		try {
			// workaround: whitespace makes the url invalid, replace with URL-encode.
			URL url = new URL(request.replace(" ", "%20"));
			HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
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
				Log.e(TAG, "Error reading from the http connection");
			} finally {
				httpConnection.disconnect();
			}
		} catch (MalformedURLException e) {
			Log.e(TAG, "Malformed URL");
		} catch (IOException e) {
			Log.e(TAG, "Error opening the http connection");
		}
	
		return builder.toString();
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
