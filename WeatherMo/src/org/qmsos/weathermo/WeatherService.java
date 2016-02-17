package org.qmsos.weathermo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.qmsos.weathermo.util.City;
import org.qmsos.weathermo.util.IpcConstants;
import org.qmsos.weathermo.util.WeatherParser;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
	 * api_key from openweathermap.org
	 */
	private static final String API_KEY = "054dcbb7bea48220bc5d30d5fc53932e";

	private static final int FLAG_CURRENT_WEATHER = 0x01;
	private static final int FLAG_FORECAST_DAILY = 0x10;
	private static final int FLAG_FORECAST_HOURLY = 0x11;
	private static final int FLAG_SEARCH_CITY = 0x20;
	
	/**
	 * Default empty constructor.
	 */
	public WeatherService() {
		super(TAG);
	}

	/**
	 * Implementation of the superclass constructor.
	 * 
	 * @param name
	 *            Used to name the worker thread, important only for debugging.
	 */
	public WeatherService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String action = intent.getAction();
		if (action == null) {
			return;
		}
		
		if (action.equals(IpcConstants.ACTION_REFRESH_WEATHER)) {
			if (checkConnection()) {
				executeRefreshWeather(FLAG_CURRENT_WEATHER);
				executeRefreshWeather(FLAG_FORECAST_HOURLY);
//				executeRefreshWeather(FLAG_FORECAST_DAILY);
			}
		} else if (action.equals(IpcConstants.ACTION_QUERY_CITY)) {
			Intent localIntent = new Intent(IpcConstants.ACTION_QUERY_EXECUTED);
			if (checkConnection()) {
				String cityname = intent.getStringExtra(IpcConstants.EXTRA_CITY_NAME);
				
				String result = executeSearchCity(cityname);
				if (result != null) {
					localIntent.putExtra(IpcConstants.EXTRA_QUERY_EXECUTED, true);
					localIntent.putExtra(IpcConstants.EXTRA_QUERY_RESULT, result);
				} else {
					localIntent.putExtra(IpcConstants.EXTRA_QUERY_EXECUTED, false);
				}
			} else {
				localIntent.putExtra(IpcConstants.EXTRA_QUERY_EXECUTED, false);
			}
			
			LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
		} else if (action.equals(IpcConstants.ACTION_INSERT_CITY)) {
			City city = intent.getParcelableExtra(IpcConstants.EXTRA_INSERT_CITY);
			if (city != null) {
				boolean flag = insertCity(city);
				
				Intent localIntent = new Intent(IpcConstants.ACTION_INSERT_EXECUTED);
				localIntent.putExtra(IpcConstants.EXTRA_INSERT_EXECUTED, flag);
				
				LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
			}
		} else if (action.equals(IpcConstants.ACTION_DELETE_CITY)) {
			long cityId = intent.getLongExtra(IpcConstants.EXTRA_CITY_ID, -1);
			if (cityId != -1) {
				deleteCity(cityId);
			}
		}
	}

	private String executeSearchCity(String cityName) {
		if (cityName == null) {
			return null;
		}
		
		String request = assembleRequest(FLAG_SEARCH_CITY, 0, cityName);
		String result = download(request);
		
		return result;
	}
	
	private void executeRefreshWeather(int flag) {
		Cursor cursor = null;
		try {
			String[] projection = { WeatherProvider.KEY_CITY_ID };
			String where = WeatherProvider.KEY_CITY_ID;
			cursor = getContentResolver().query(
					WeatherProvider.CONTENT_URI_WEATHER, projection, where, null, null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					long cityId = cursor.getLong(
							cursor.getColumnIndexOrThrow(WeatherProvider.KEY_CITY_ID));
					
					String request = assembleRequest(flag, cityId, null);
					if (request != null) {
						String result =  download(request);
						
						updateWeather(flag, cityId, result);
					}
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
	
	private void updateWeather(int flag, long cityId, String result) {
		if (result == null || cityId == 0) {
			return;
		}
		
		ContentValues values = new ContentValues();
		String parsed = null;
		switch (flag) {
		case FLAG_CURRENT_WEATHER:
			parsed = WeatherParser.parseRawToCurrent(result);
			if (parsed != null) {
				values.put(WeatherProvider.KEY_CURRENT, parsed);
			}
			break;
		case FLAG_FORECAST_DAILY:
			parsed = WeatherParser.parseRawToForecastDaily(result);
			if (parsed != null) {
				values.put(WeatherProvider.KEY_FORECAST, parsed);
			}
			break;
		case FLAG_FORECAST_HOURLY:
			parsed = WeatherParser.parseRawToForecastHourly(result);
			if (parsed != null) {
				values.put(WeatherProvider.KEY_FORECAST, parsed);
			}
			break;
		default:
			return;
		}
		
		String where = WeatherProvider.KEY_CITY_ID + " = " + cityId;
		getContentResolver().update(WeatherProvider.CONTENT_URI_WEATHER, values, where, null);
	}

	private boolean insertCity(City city) {
		if (city == null) {
			return false;
		}
		
		boolean flag = false;

		Cursor cursor = null;
		try {
			ContentResolver resolver = getContentResolver();
			String where = WeatherProvider.KEY_CITY_ID + " = " + city.getCityId();
			cursor = resolver.query(WeatherProvider.CONTENT_URI_CITIES, null, where, null, null);
			if (cursor != null && !cursor.moveToNext()) {
				ContentValues values = new ContentValues();
				values.put(WeatherProvider.KEY_CITY_ID, city.getCityId());
				
				resolver.insert(WeatherProvider.CONTENT_URI_WEATHER, values);
				
				values.put(WeatherProvider.KEY_NAME, city.getCityName());
				values.put(WeatherProvider.KEY_COUNTRY, city.getCountry());
				values.put(WeatherProvider.KEY_LONGITUDE, city.getLongitude());
				values.put(WeatherProvider.KEY_LATITUDE, city.getLatitude());
				resolver.insert(WeatherProvider.CONTENT_URI_CITIES, values);
				
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

	private boolean deleteCity(long cityId) {
		String where = WeatherProvider.KEY_CITY_ID + " = " + cityId;
		int rows1 = getContentResolver().delete(WeatherProvider.CONTENT_URI_CITIES, where, null);
		int rows2 = getContentResolver().delete(WeatherProvider.CONTENT_URI_WEATHER, where, null);
		
		return (rows1 > 0 && rows2 > 0) ? true : false;
	}

	/**
	 * Query remote server for specific request.
	 * 
	 * @param request
	 *            The request URL as string.
	 * @return Result of this query.
	 */
	private String download(String request) {
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

	private String assembleRequest(int flag, long cityId, String cityName) {
		switch (flag) {
		case FLAG_CURRENT_WEATHER:
			return "http://api.openweathermap.org/data/2.5/" 
					+ "weather?" + "id=" + String.valueOf(cityId)
					+ "&units=" + "metric"
					+ "&appid=" + API_KEY;
		case FLAG_FORECAST_DAILY:
			int days = WeatherParser.COUNT_FORECAST_DAYS + 1;
			
			return "http://api.openweathermap.org/data/2.5/" 
					+ "forecast/daily?" + "id=" + String.valueOf(cityId) 
					+ "&cnt=" + days
					+ "&units=" + "metric"
					+ "&appid=" + API_KEY;
		case FLAG_FORECAST_HOURLY:
			int count = WeatherParser.COUNT_FORECAST_DAYS * WeatherParser.COUNT_FORECAST_HOURS;
			
			return "http://api.openweathermap.org/data/2.5/" 
					+ "forecast?" + "id=" + String.valueOf(cityId) 
					+ "&cnt=" + count
					+ "&units=" + "metric"
					+ "&appid=" + API_KEY;
		case FLAG_SEARCH_CITY:
			return "http://api.openweathermap.org/data/2.5/"
					+ "find?" + "q=" + cityName
					+ "&type=" + "like"
					+ "&units=" + "metric"
					+ "&appid=" + WeatherService.API_KEY;
		default:
			return null;
		}
	}

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

}
