package org.qmsos.environmo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.SparseArray;

public class MainUpdateService extends IntentService {

	/**
	 * api_key from openweathermap.org
	 */
	protected static final String API_KEY = "2de143494c0b295cca9337e1e96b00e0";

	// api_key from openweathermap.com
	// private static final String API_KEY = "054dcbb7bea48220bc5d30d5fc53932e";
	// api_key from forecast.io
	// private static final String API_KEY = "b2794078a6804a588f58950bffd10151";

	public static final String EXTRA_PENDING_RESULT = "pending_result";
	
	public static final String CURRENT_RESULT = "org.qmsos.environmo.CURRENT_RESULT";
	public static final String FORECAST_RESULT = "org.qmsos.environmo.FORECAST_RESULT";

	public static final String QUERY_CITY = "org.qmsos.environmo.QUERY_CITY";
	public static final String QUERY_WEATHER = "org.qmsos.environmo.QUERY_WEATHER";

	public static final String CITY_NAME = "org.qmsos.environmo.CITY_NAME";
	
	private static final String CITY_ID = "org.qmsos.environmo.CITY_ID";
	
	private static final int CURRENT_KEY = 6;
	private static final int FORECAST_KEY = 7;

	private static String DAY_COUNT = "4";

	/**
	 * Default empty constructor.
	 */
	public MainUpdateService() {
		super(MainUpdateService.class.getSimpleName());
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

		if (intent.getBooleanExtra(QUERY_CITY, false)) {
			String cityname = intent.getStringExtra(CITY_NAME);
			queryCity(cityname);
			
			PendingIntent reply = intent.getParcelableExtra(EXTRA_PENDING_RESULT);
			try {
				reply.send();
			} catch (CanceledException e) {
				e.printStackTrace();
			}
		}

		if (intent.getBooleanExtra(QUERY_WEATHER, false)) {

			queryWeather();

			PendingIntent reply = intent.getParcelableExtra(EXTRA_PENDING_RESULT);
			try {
				reply.send();
			} catch (CanceledException e) {
				e.printStackTrace();
			}
		}
	}

	private void queryCity(String request) {
		String result = queryForCityId(request);
		int cityId = checkForCityId(result);
		storeCityId(cityId);
	}

	private String queryForCityId(String cityName) {
		String request = "http://api.openweathermap.org/data/2.5/" + "weather?" + "q=" + cityName + "&units=" + "metric"
				+ "&appid=" + MainUpdateService.API_KEY;

		String result = null;
		try {
			result = query(request);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	private int checkForCityId(String result) {
		final int ERROR = -1;

		int cityId = ERROR;
		if (result != null) {
			JSONObject reader;
			try {
				reader = new JSONObject(result);
				cityId = reader.getInt("id");
				if (cityId > 0) {
					return cityId;
				} else {
					return ERROR;
				}
			} catch (JSONException e) {
				e.printStackTrace();

				return ERROR;
			}
		} else {
			return ERROR;
		}
	}

	private void storeCityId(int cityId) {
		if (cityId > 0) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			SharedPreferences.Editor editor = prefs.edit();

			editor.putInt(CITY_ID, cityId);
			editor.apply();
		}
	}

	private void queryWeather() {
		SparseArray<String> requests = assembleRequests();
		SparseArray<String> results = queryForResults(requests);
		storeResults(results);
	}

	private SparseArray<String> assembleRequests() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		int cityId = prefs.getInt(CITY_ID, 2037355);

		String currentRequest = "http://api.openweathermap.org/data/2.5/" + "weather?" + "id=" + String.valueOf(cityId)
				+ "&units=" + "metric" + "&appid=" + API_KEY;

		String forecastRequest = "http://api.openweathermap.org/data/2.5/" + "forecast/daily?" + "id="
				+ String.valueOf(cityId) + "&cnt=" + DAY_COUNT + "&units=" + "metric" + "&appid=" + API_KEY;

		SparseArray<String> requests = new SparseArray<String>();
		requests.put(CURRENT_KEY, currentRequest);
		requests.put(FORECAST_KEY, forecastRequest);

		return requests;
	}

	private SparseArray<String> queryForResults(SparseArray<String> requests) {
		SparseArray<String> results = new SparseArray<String>();

		String currentRequest = requests.get(CURRENT_KEY);
		String forecastRequest = requests.get(FORECAST_KEY);

		String currentResult = null;
		String forecastResult = null;
		try {
			currentResult = query(currentRequest);
			forecastResult = query(forecastRequest);
		} catch (IOException e) {
			e.printStackTrace();
		}

		results.put(CURRENT_KEY, currentResult);
		results.put(FORECAST_KEY, forecastResult);

		return results;
	}

	private boolean checkResults(SparseArray<String> results) {
		String currentResult = results.get(CURRENT_KEY);
		if (currentResult != null) {
			try {
				JSONObject reader = new JSONObject(currentResult);
				if (reader.getInt("id") > 0) {
					return true;
				} else {
					return false;
				}
			} catch (JSONException e) {
				e.printStackTrace();

				return false;
			}
		} else {
			return false;
		}
	}

	private void storeResults(SparseArray<String> results) {
		if (checkResults(results)) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			SharedPreferences.Editor editor = prefs.edit();

			String currentResult = results.get(CURRENT_KEY);
			String forecastResult = results.get(FORECAST_KEY);

			editor.putString(CURRENT_RESULT, currentResult);
			editor.putString(FORECAST_RESULT, forecastResult);
			editor.apply();
		}
	}

	private String query(String request) throws IOException {
		StringBuilder builder = new StringBuilder();

		URL url = new URL(request);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		int response = connection.getResponseCode();
		if (response == HttpURLConnection.HTTP_OK) {
			InputStream in = connection.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));

			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
		} else {
			throw new RuntimeException("Query failed!!!");
		}

		return builder.toString();
	}

}
