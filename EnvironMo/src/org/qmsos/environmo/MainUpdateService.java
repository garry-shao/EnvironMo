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
import android.util.Log;
import android.util.SparseArray;

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

	/**
	 * Key to stored city ID in preferences.
	 */
	private static final String CITY_ID = "org.qmsos.environmo.CITY_ID";

	/**
	 * Key to current related weather query.
	 */
	private static final int CURRENT_KEY = 6;
	/**
	 * Key to forecast related weather query.
	 */
	private static final int FORECAST_KEY = 7;

	/**
	 * Days to forecast.
	 */
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

			queryWeathers();

			PendingIntent reply = intent.getParcelableExtra(EXTRA_PENDING_RESULT);
			try {
				reply.send();
			} catch (CanceledException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Query for city ID with the city name.
	 * 
	 * @param request
	 *            The city name to query.
	 */
	private void queryCity(String request) {
		String result = queryForCityId(request);
		int cityId = checkForCityId(result);
		storeCityId(cityId);
	}

	/**
	 * Query remote server for result with the city name.
	 * 
	 * @param cityName
	 *            The city name to query.
	 * @return The result of city name query.
	 */
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

	/**
	 * Check to see if the result contains a valid city ID.
	 * 
	 * @param result
	 *            The result string of this city ID query.
	 * @return the city ID contained in the result or ERROR(-1);
	 */
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

	/**
	 * Store city ID in the preferences.
	 * 
	 * @param cityId
	 *            The city ID to store.
	 */
	private void storeCityId(int cityId) {
		if (cityId > 0) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			SharedPreferences.Editor editor = prefs.edit();

			editor.putInt(CITY_ID, cityId);
			editor.apply();
		}
	}

	/**
	 * Query for current & forecast weathers.
	 */
	private void queryWeathers() {
		SparseArray<String> requests = assembleRequests();
		SparseArray<String> results = queryForWeathers(requests);
		storeWeathers(results);
	}

	/**
	 * Query weather with requests for results.
	 * 
	 * @param weatherRequests
	 *            The weather requests.
	 * @return The weather results.
	 */
	private SparseArray<String> queryForWeathers(SparseArray<String> weatherRequests) {
		SparseArray<String> weatherResults = new SparseArray<String>();

		String currentWeatherRequest = weatherRequests.get(CURRENT_KEY);
		String forecastWeatherRequest = weatherRequests.get(FORECAST_KEY);

		String currentWeatherResult = null;
		String forecastWeatherResult = null;
		try {
			currentWeatherResult = query(currentWeatherRequest);
			forecastWeatherResult = query(forecastWeatherRequest);
		} catch (IOException e) {
			e.printStackTrace();
		}

		weatherResults.put(CURRENT_KEY, currentWeatherResult);
		weatherResults.put(FORECAST_KEY, forecastWeatherResult);

		return weatherResults;
	}

	/**
	 * Check to see if the weather results are valid.
	 * 
	 * @param weatherResults
	 *            The weather results to verify.
	 * @return True if all the weather results are valid.
	 */
	private boolean checkWeathers(SparseArray<String> weatherResults) {
		String currentWeatherResult = weatherResults.get(CURRENT_KEY);
		if (currentWeatherResult != null) {
			try {
				JSONObject reader = new JSONObject(currentWeatherResult);
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

	/**
	 * Store weather results in the preferences.
	 * 
	 * @param weatherResults
	 *            The queried weather results to store.
	 */
	private void storeWeathers(SparseArray<String> weatherResults) {
		if (checkWeathers(weatherResults)) {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			SharedPreferences.Editor editor = prefs.edit();

			String currentWeatherResult = weatherResults.get(CURRENT_KEY);
			String forecastWeatherResult = weatherResults.get(FORECAST_KEY);

			editor.putString(CURRENT_RESULT, currentWeatherResult);
			editor.putString(FORECAST_RESULT, forecastWeatherResult);
			editor.apply();
		}
	}

	/**
	 * Assemble weather request URLs as strings from preferences.
	 * 
	 * @return The assembled weather request URLs.
	 */
	private SparseArray<String> assembleRequests() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		int cityId = prefs.getInt(CITY_ID, 2037355);

		String currentWeatherRequest = "http://api.openweathermap.org/data/2.5/" + "weather?" + "id=" + String.valueOf(cityId)
				+ "&units=" + "metric" + "&appid=" + API_KEY;

		String forecastWeatherRequest = "http://api.openweathermap.org/data/2.5/" + "forecast/daily?" + "id="
				+ String.valueOf(cityId) + "&cnt=" + DAY_COUNT + "&units=" + "metric" + "&appid=" + API_KEY;

		SparseArray<String> weatherRequests = new SparseArray<String>();
		weatherRequests.put(CURRENT_KEY, currentWeatherRequest);
		weatherRequests.put(FORECAST_KEY, forecastWeatherRequest);

		return weatherRequests;
	}

	/**
	 * Query remote server for specific request.
	 * 
	 * @param request
	 *            The request URL as string.
	 * @return Result of this query.
	 * @throws IOException
	 *             Something wrong happened during query action.
	 */
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
			Log.d(TAG, "Http connnection error! " + "responseCode = " + response);
		}

		return builder.toString();
	}

}
