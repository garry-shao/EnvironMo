package org.qmsos.environmo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;
import org.qmsos.environmo.data.City;
import org.qmsos.environmo.util.UtilResultReceiver;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.ResultReceiver;
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
	private static final String API_KEY = "2de143494c0b295cca9337e1e96b00e0";

	// api_key from openweathermap.com
	// private static final String API_KEY = "054dcbb7bea48220bc5d30d5fc53932e";
	// api_key from forecast.io
	// private static final String API_KEY = "b2794078a6804a588f58950bffd10151";

	/**
	 * Days to forecast.
	 */
	private static final String DAY_COUNT = "4";

	private static final int FLAG_CURRENT = 1;
	private static final int FLAG_FORECAST = 2;

	public static final String ACTION_REFRESH = "org.qmsos.environmo.ACTION_REFRESH";
	public static final String ACTION_QUERY_CITY = "org.qmsos.environmo.ACTION_QUERY_CITY";
	public static final String ACTION_IMPORT_CITY = "org.qmsos.environmo.ACTION_IMPORT_CITY";
	public static final String ACTION_CITY_ADDED = "org.qmsos.environmo.ACTION_CITY_ADDED";
	
	public static final String BUNDLE_KEY_CURRENT = "BUNDLE_KEY_CURRENT";
	public static final String BUNDLE_KEY_FORECAST = "BUNDLE_KEY_FORECAST";
	
	public static final String EXTRA_KEY_CITY_NAME = "EXTRA_KEY_CITY_NAME";

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
				ResultReceiver receiver = intent.getParcelableExtra(UtilResultReceiver.RECEIVER);
				if (receiver != null) {
					queryWeathers(FLAG_CURRENT);
					queryWeathers(FLAG_FORECAST);
					
					Bundle b = new Bundle();
					receiver.send(RESULT_CODE_REFRESHED, b);
				}
			} else if (action.equals(ACTION_QUERY_CITY)) {
				String cityname = intent.getStringExtra(EXTRA_KEY_CITY_NAME);
				boolean result = queryCity(cityname);
				if (result) {
					Intent i = new Intent(ACTION_CITY_ADDED);
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(i);
				}
			} else if (action.equals(ACTION_IMPORT_CITY)) {
				readAssets();
			}
		}
	}

	private boolean queryCity(String cityName) {
		if (cityName != null) {
			String request = "http://api.openweathermap.org/data/2.5/"
					+ "weather?" + "q=" + cityName
					+ "&units=" + "metric"
					+ "&appid=" + MainUpdateService.API_KEY;
			
			String result = download(request);
			if (result != null) {
				City city = readCityFromQuery(result);
				if (city != null) {
					boolean flag =  addCityToProvider(city);
					if (flag) {
						queryWeathers(FLAG_CURRENT);
						queryWeathers(FLAG_FORECAST);
					}
					
					return flag;
				}
			}
		}

		return false;
	}
	
	private void queryWeathers(int flag) {
		String[] projection = { CityProvider.KEY_ID, CityProvider.KEY_CITYID, CityProvider.KEY_NAME };
		
		ContentResolver resolver = getContentResolver();
		String where = CityProvider.KEY_CITYID;

		Cursor query = resolver.query(CityProvider.CONTENT_URI, projection, where, null, null);
		if (query != null && query.getCount() != 0) {
			while (query.moveToNext()) {
				Long cityId = query.getLong(query.getColumnIndex(CityProvider.KEY_CITYID));
				String result = queryWeather(cityId, flag);
				updateWeatherToProvider(result, cityId, flag);
			}
		}
	}

	private void updateWeatherToProvider(String result, long cityId, int flag) {
		ContentResolver resolver = getContentResolver();
		String where = CityProvider.KEY_CITYID + " = " + cityId;
		ContentValues values = new ContentValues();
		
		switch (flag) {
		case FLAG_CURRENT:
			values.put(CityProvider.KEY_CURRENT, result);
			break;
		case FLAG_FORECAST:
			values.put(CityProvider.KEY_FORECAST, result);
			break;
		}
		resolver.update(CityProvider.CONTENT_URI, values, where, null);
	}

	private String queryWeather(long cityId, int flag) {
		String query = assembleQuery(cityId, flag);
		if (query != null) {
			return download(query);
		}
		
		return null;
	}
	
/*	private String assembleQuery(int flag) {
		StringBuilder b = new StringBuilder("id=");
		
		ContentResolver resolver = getContentResolver();
		
		String[] projection = { CityProvider.KEY_CITYID };
		String where = CityProvider.KEY_CITYID;

		Cursor query = resolver.query(CityProvider.CONTENT_URI, projection, where, null, null);
		if (query != null && query.getCount() != 0) {
			while (query.moveToNext()) {
				Long cityId = query.getLong(query.getColumnIndex(CityProvider.KEY_CITYID));
				b.append(cityId);
			}
		}
		
		switch (flag) {
		case FLAG_CURRENT:
			return "http://api.openweathermap.org/data/2.5/" 
			+ "group?" + b.toString()
			+ "&units=" + "metric"
			+ "&appid=" + API_KEY;
		default:
			return null;
		}
	}
*/	
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
					+ "forecast/daily?" + "id="	+ String.valueOf(cityId) 
					+ "&cnt=" + DAY_COUNT
					+ "&units=" + "metric"
					+ "&appid=" + API_KEY;
		default:
			return null;
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
			InputStream in = getAssets().open("city.list.json");
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			while (reader.readLine() != null) {
				City city = readCityFromAssets(reader.readLine());
				if (city != null) {
					boolean flag =  addCityToProvider(city);
					if (flag) {
						queryWeathers(FLAG_CURRENT);
						queryWeathers(FLAG_FORECAST);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private City readCityFromQuery(String query) {
		if (query == null) {
			return null;
		}
		
		JSONObject reader;
		try {
			reader = new JSONObject(query);
			long id = reader.getLong("id");
			String name = reader.getString("name");
			
			JSONObject sys = reader.getJSONObject("sys");
			String country = sys.getString("country");
			
			JSONObject coord = reader.getJSONObject("coord");
			double longitude = coord.getDouble("lon");
			double latitude = coord.getDouble("lat");
			
			return new City(id, name, country, longitude, latitude);
		} catch (JSONException e) {
			return null;
		}
	}
	
	private City readCityFromAssets(String line) {
		if (line == null) {
			return null;
		}
		
		JSONObject reader;
		try {
			reader = new JSONObject(line);
			long id = Long.parseLong(reader.getString("_id"));
			String name = reader.getString("name");
			String country = reader.getString("country");
			
			JSONObject coord = reader.getJSONObject("coord");
			double longitude = coord.getDouble("lon");
			double latitude = coord.getDouble("lat");
			
			return new City(id, name, country, longitude, latitude);
		} catch (JSONException e) {
			return null;
		}
	}

	private boolean addCityToProvider(City city) {
		boolean result = false;

		if (city != null) {
			ContentResolver resolver = getContentResolver();
			String where = CityProvider.KEY_CITYID + " = " + city.getCityId();
			Cursor query = resolver.query(CityProvider.CONTENT_URI, null, where, null, null);
			if (query != null) {
				if (query.getCount() == 0) {
					ContentValues values = new ContentValues();
					values.put(CityProvider.KEY_CITYID, city.getCityId());
					values.put(CityProvider.KEY_NAME, city.getName());
					values.put(CityProvider.KEY_COUNTRY, city.getCountry());
					values.put(CityProvider.KEY_LONGITUDE, city.getLongitude());
					values.put(CityProvider.KEY_LATITUDE, city.getLatitude());
					resolver.insert(CityProvider.CONTENT_URI, values);
					
					result = true;
				}
			
				query.close();
			}
		}
		
		return result;
	}

}
