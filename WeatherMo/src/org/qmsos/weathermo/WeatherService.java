package org.qmsos.weathermo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;
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

	private static final int FLAG_CURRENT = 1;
	private static final int FLAG_FORECAST = 2;
	
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
				queryWeather(FLAG_CURRENT);
				queryWeather(FLAG_FORECAST);
			}
		} else if (action.equals(IpcConstants.ACTION_QUERY_CITY)) {
			if (checkConnection()) {
				String cityname = intent.getStringExtra(IpcConstants.EXTRA_CITY_NAME);
				
				queryCity(cityname);
			}
		} else if (action.equals(IpcConstants.ACTION_DELETE_CITY)) {
			long cityId = intent.getLongExtra(IpcConstants.EXTRA_CITY_ID, -1);
			if (cityId != -1) {
				deleteCityFromProvider(cityId);
			}
		}
	}

	private boolean queryCity(String cityName) {
		if (cityName == null) {
			return false;
		}
		
		String request = "http://api.openweathermap.org/data/2.5/"
				+ "weather?" + "q=" + cityName
				+ "&units=" + "metric"
				+ "&appid=" + WeatherService.API_KEY;
		String result = download(request);
		if (result == null) {
			return false;
		}
			
		boolean flag = false;
		try {
			JSONObject reader = new JSONObject(result);
			long id = reader.getLong("id");
			String name = reader.getString("name");
			
			JSONObject sys = reader.getJSONObject("sys");
			String country = sys.getString("country");
			
			JSONObject coord = reader.getJSONObject("coord");
			double longitude = coord.getDouble("lon");
			double latitude = coord.getDouble("lat");
			
			City city =  new City(id, name, country, longitude, latitude);
			
			flag = addCityToProvider(city);
		} catch (JSONException e) {
			Log.e(TAG, "Error parsing json when querying for city id");
		}
		
		return flag;
	}
	
	private void queryWeather(int flag) {
		Cursor cursor = null;
		try {
			String[] projection = { WeatherProvider.KEY_CITY_ID };
			String where = WeatherProvider.KEY_CITY_ID;
			cursor = getContentResolver().query(
					WeatherProvider.CONTENT_URI_WEATHER, projection, where, null, null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					long cityId = cursor.getLong(cursor.getColumnIndexOrThrow(WeatherProvider.KEY_CITY_ID));
					String query = assembleQuery(cityId, flag);
					if (query != null) {
						String result =  download(query);
						
						updateWeatherToProvider(result, cityId, flag);
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
	
	private String assembleQuery(long cityId, int flag) {
		if (cityId == 0) {
			return null;
		}
		
		switch (flag) {
		case FLAG_CURRENT:
			return "http://api.openweathermap.org/data/2.5/" 
					+ "weather?" + "id=" + String.valueOf(cityId)
					+ "&units=" + "metric"
					+ "&appid=" + API_KEY;
		case FLAG_FORECAST:
			return "http://api.openweathermap.org/data/2.5/" 
					+ "forecast?" + "id=" + String.valueOf(cityId) 
					+ "&units=" + "metric"
					+ "&appid=" + API_KEY;
		default:
			return null;
		}
	}

	private void updateWeatherToProvider(String result, long cityId, int flag) {
		if (result == null || cityId == 0 || !(flag == FLAG_CURRENT || flag == FLAG_FORECAST)) {
			return;
		}
		
		String where = WeatherProvider.KEY_CITY_ID + " = " + cityId;
		ContentValues values = new ContentValues();
		
		String parsed = null;
		switch (flag) {
		case FLAG_CURRENT:
			parsed = WeatherParser.parseRawToPattern(result, WeatherParser.FLAG_CURRENT);
			if (parsed != null) {
				values.put(WeatherProvider.KEY_CURRENT, parsed);
			}
			
			break;
		case FLAG_FORECAST:
			parsed = WeatherParser.parseRawToPattern(result, WeatherParser.FLAG_FORECAST);
			if (parsed != null) {
				values.put(WeatherProvider.KEY_FORECAST, parsed);
			}
			
			break;
		}
		getContentResolver().update(WeatherProvider.CONTENT_URI_WEATHER, values, where, null);
	}

	private boolean addCityToProvider(City city) {
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

	private boolean deleteCityFromProvider(long cityId) {
		String where = WeatherProvider.KEY_CITY_ID + " = " + cityId;
		int rows1 = getContentResolver().delete(WeatherProvider.CONTENT_URI_CITIES, where, null);
		int rows2 = getContentResolver().delete(WeatherProvider.CONTENT_URI_WEATHER, where, null);
		
		return (rows1 > 0 && rows2 > 0) ? true : false;
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
			URL url = new URL(request);
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

}
