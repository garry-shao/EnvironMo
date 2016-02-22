package org.qmsos.weathermo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.qmsos.weathermo.datamodel.City;
import org.qmsos.weathermo.provider.WeatherContract.CityEntity;
import org.qmsos.weathermo.provider.WeatherContract.WeatherEntity;
import org.qmsos.weathermo.util.IntentConstants;
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
	private static final int FLAG_CURRENT_UV_INDEX = 0x02;
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
		
		if (action.equals(IntentConstants.ACTION_REFRESH_WEATHER)) {
			if (checkConnection()) {
				executeRefreshWeather(FLAG_CURRENT_WEATHER);
				executeRefreshWeather(FLAG_FORECAST_HOURLY);
				executeRefreshWeather(FLAG_CURRENT_UV_INDEX);
//				executeRefreshWeather(FLAG_FORECAST_DAILY);
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
				boolean flag = insertCity(city);
				
				Intent localIntent = new Intent(IntentConstants.ACTION_INSERT_EXECUTED);
				localIntent.putExtra(IntentConstants.EXTRA_INSERT_EXECUTED, flag);
				
				LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
			}
		} else if (action.equals(IntentConstants.ACTION_DELETE_CITY)) {
			long cityId = intent.getLongExtra(IntentConstants.EXTRA_CITY_ID, -1);
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
			String[] projection = { WeatherEntity.CITY_ID };
			String where = WeatherEntity.CITY_ID;
			cursor = getContentResolver().query(
					WeatherEntity.CONTENT_URI, projection, where, null, null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					long cityId = cursor.getLong(
							cursor.getColumnIndexOrThrow(WeatherEntity.CITY_ID));
					
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
		switch (flag) {
		case FLAG_CURRENT_WEATHER:
			String parsedCurrent = WeatherParser.parseRawToCurrent(result);
			if (parsedCurrent != null) {
				values.put(WeatherEntity.CURRENT, parsedCurrent);
			}
			break;
		case FLAG_CURRENT_UV_INDEX:
			double value = WeatherParser.parseRawToUvIndex(result);
			if (value > 0) {
				values.put(WeatherEntity.UV_INDEX, value);
			}
			break;
		case FLAG_FORECAST_DAILY:
			String[] parsedForecastDaily = WeatherParser.parseRawToForecastsDaily(result);
			if (parsedForecastDaily != null) {
				values.put(WeatherEntity.FORECAST1, parsedForecastDaily[0]);
				values.put(WeatherEntity.FORECAST2, parsedForecastDaily[1]);
				values.put(WeatherEntity.FORECAST3, parsedForecastDaily[2]);
			}
			break;
		case FLAG_FORECAST_HOURLY:
			String[] parsedForecastHourly = WeatherParser.parseRawToForecastsHourly(result);
			if (parsedForecastHourly != null) {
				values.put(WeatherEntity.FORECAST1, parsedForecastHourly[0]);
				values.put(WeatherEntity.FORECAST2, parsedForecastHourly[1]);
				values.put(WeatherEntity.FORECAST3, parsedForecastHourly[2]);
			}
			break;
		default:
			return;
		}
		
		String where = WeatherEntity.CITY_ID + " = " + cityId;
		getContentResolver().update(WeatherEntity.CONTENT_URI, values, where, null);
	}

	private boolean insertCity(City city) {
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

	private boolean deleteCity(long cityId) {
		String where = CityEntity.CITY_ID + " = " + cityId;
		int rows1 = getContentResolver().delete(CityEntity.CONTENT_URI, where, null);
		int rows2 = getContentResolver().delete(WeatherEntity.CONTENT_URI, where, null);
		
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
		case FLAG_CURRENT_UV_INDEX:
			String geo = assembleRequestGeo(cityId);
			if (geo != null) {
				return "http://api.owm.io/air/1.0/uvi/"
						+ "current?" + geo
						+ "&appid=" + API_KEY;
			} else {
				return null;
			}
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
	
	private String assembleRequestGeo(long cityId) {
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
