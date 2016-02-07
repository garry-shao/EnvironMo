package org.qmsos.environmo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

import org.json.JSONException;
import org.json.JSONObject;
import org.qmsos.environmo.data.City;
import org.qmsos.environmo.util.UtilWeatherParser;

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
 * @author environmo(at)sina.com
 *
 */
public class MainUpdateService extends IntentService {

	private static final String TAG = MainUpdateService.class.getSimpleName();
	
	/**
	 * api_key from openweathermap.org
	 */
	private static final String API_KEY = "054dcbb7bea48220bc5d30d5fc53932e";

	private static final int FLAG_CURRENT = 1;
	private static final int FLAG_FORECAST = 2;

	public static final String ACTION_REFRESH = "org.qmsos.environmo.ACTION_REFRESH";
	public static final String ACTION_DELETE_CITY = "org.qmsos.environmo.ACTION_DELETE_CITY";
	public static final String ACTION_QUERY_CITY = "org.qmsos.environmo.ACTION_QUERY_CITY";
	public static final String ACTION_IMPORT_CITY = "org.qmsos.environmo.ACTION_IMPORT_CITY";
	public static final String ACTION_CITY_CHANGED = "org.qmsos.environmo.ACTION_CITY_CHANGED";
	
	public static final String BUNDLE_KEY_CURRENT = "BUNDLE_KEY_CURRENT";
	public static final String BUNDLE_KEY_FORECAST = "BUNDLE_KEY_FORECAST";
	
	public static final String EXTRA_KEY_CITY_NAME = "EXTRA_KEY_CITY_NAME";
	public static final String EXTRA_KEY_CITY_ID = "EXTRA_KEY_CITY_ID";

	public static final int RESULT_CODE_REFRESHED = 1;
	
	/**
	 * Default empty constructor.
	 */
	public MainUpdateService() {
		super(TAG);
	}

	/**
	 * Implementation of the superclass constructor.
	 * 
	 * @param name
	 *            Used to name the worker thread, important only for debugging.
	 */
	public MainUpdateService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String action = intent.getAction();
		if (action != null) {
			if (action.equals(ACTION_REFRESH)) {
				if (checkConnection()) {
					queryWeather(FLAG_CURRENT);
					queryWeather(FLAG_FORECAST);
				}
			} else if (action.equals(ACTION_QUERY_CITY)) {
				if (checkConnection()) {
					String cityname = intent.getStringExtra(EXTRA_KEY_CITY_NAME);
					
					queryCity(cityname);
				}
			} else if (action.equals(ACTION_DELETE_CITY)) {
				long cityId = intent.getLongExtra(EXTRA_KEY_CITY_ID, -1);
				if (cityId != -1) {
					deleteCityFromProvider(cityId);
				}
			} else if (action.equals(ACTION_IMPORT_CITY)) {
				readAssets();
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
				+ "&appid=" + MainUpdateService.API_KEY;
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
			String[] projection = { MainProvider.KEY_CITY_ID };
			String where = MainProvider.KEY_CITY_ID;
			cursor = getContentResolver().query(
					MainProvider.CONTENT_URI_WEATHER, projection, where, null, null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					long cityId = cursor.getLong(cursor.getColumnIndexOrThrow(MainProvider.KEY_CITY_ID));
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
		
		String where = MainProvider.KEY_CITY_ID + " = " + cityId;
		ContentValues values = new ContentValues();
		
		String parsed = null;
		switch (flag) {
		case FLAG_CURRENT:
			parsed = UtilWeatherParser.parseRawToPattern(result, UtilWeatherParser.FLAG_CURRENT);
			if (parsed != null) {
				values.put(MainProvider.KEY_CURRENT, parsed);
			}
			
			break;
		case FLAG_FORECAST:
			parsed = UtilWeatherParser.parseRawToPattern(result, UtilWeatherParser.FLAG_FORECAST);
			if (parsed != null) {
				values.put(MainProvider.KEY_FORECAST, parsed);
			}
			
			break;
		}
		getContentResolver().update(MainProvider.CONTENT_URI_WEATHER, values, where, null);
	}

	private boolean addCityToProvider(City city) {
		if (city == null) {
			return false;
		}
		
		boolean flag = false;

		Cursor cursor = null;
		try {
			ContentResolver resolver = getContentResolver();
			String where = MainProvider.KEY_CITY_ID + " = " + city.getCityId();
			cursor = resolver.query(MainProvider.CONTENT_URI_CITIES, null, where, null, null);
			if (cursor != null && !cursor.moveToNext()) {
				ContentValues values = new ContentValues();
				values.put(MainProvider.KEY_CITY_ID, city.getCityId());
				
				resolver.insert(MainProvider.CONTENT_URI_WEATHER, values);
				
				values.put(MainProvider.KEY_NAME, city.getName());
				values.put(MainProvider.KEY_COUNTRY, city.getCountry());
				values.put(MainProvider.KEY_LONGITUDE, city.getLongitude());
				values.put(MainProvider.KEY_LATITUDE, city.getLatitude());
				resolver.insert(MainProvider.CONTENT_URI_CITIES, values);
				
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
		String where = MainProvider.KEY_CITY_ID + " = " + cityId;
		int rows1 = getContentResolver().delete(MainProvider.CONTENT_URI_CITIES, where, null);
		int rows2 = getContentResolver().delete(MainProvider.CONTENT_URI_WEATHER, where, null);
		
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

	private void readAssets() {
		try {
			LinkedList<ContentValues> cityList = new LinkedList<ContentValues>();
			
			InputStream in = getAssets().open("city.list.json");
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
			while (bufferedReader.readLine() != null) {
				String s = bufferedReader.readLine();
				
				JSONObject reader;
				try {
					reader = new JSONObject(s);
					long id = Long.parseLong(reader.getString("_id"));
					String name = reader.getString("name");
					String country = reader.getString("country");
					
					JSONObject coord = reader.getJSONObject("coord");
					double longitude = coord.getDouble("lon");
					double latitude = coord.getDouble("lat");
					
					Cursor cursor = null;
					try {
						ContentResolver resolver = getContentResolver();
						String where = MainProvider.KEY_CITY_ID + " = " + id;
						cursor = resolver.query(MainProvider.CONTENT_URI_CITIES, null, where, null, null);
						if (cursor != null && !cursor.moveToNext()) {
							ContentValues value = new ContentValues();
							value.put(MainProvider.KEY_CITY_ID, id);
							value.put(MainProvider.KEY_NAME, name);
							value.put(MainProvider.KEY_COUNTRY, country);
							value.put(MainProvider.KEY_LONGITUDE, longitude);
							value.put(MainProvider.KEY_LATITUDE, latitude);
							
							cityList.add(value);
							
						}
					} finally {
						if (cursor != null && !cursor.isClosed()) {
							cursor.close();
						}
					}
				} catch (JSONException e) {
				}
			}
			
			int cityListSize = cityList.size();
			ContentValues[] values = new ContentValues[cityListSize];
			cityList.toArray(values);
			
			getContentResolver().bulkInsert(MainProvider.CONTENT_URI_CITIES, values);
		} catch (IOException e) {
			Log.e(TAG, "Something wrong with the result JSON");
		}
	}

}
