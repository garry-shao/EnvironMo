package org.qmsos.environmo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.IntentService;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class MainUpdateService extends IntentService {

	/**
	 * api_key from openweathermap.org
	 */
	protected static final String API_KEY = "2de143494c0b295cca9337e1e96b00e0";
	
//	api_key from openweathermap.com
//	private static final String API_KEY = "054dcbb7bea48220bc5d30d5fc53932e";
//	api_key from forecast.io
//	private static final String API_KEY = "b2794078a6804a588f58950bffd10151";
	
	public static final String EXTRA_PENDING_RESULT = "pending_result";
	public static final String CURRENT_WEATHER = "current_weather";
	public static final String FORECAST_WEATHER = "forecast_weather";
	public static final String CURRENT_QUERY = "current_query";
	public static final String FORECAST_QUERY = "forecast_query";
	public static final String CITY_ID = "city_id";
	
	private static String DAY_COUNT = "4";
	
	private static String cQuery = "http://api.openweathermap.org/data/2.5/" +
			"weather?" + 
			"id=" + "2037355" + 
			"&units=" + "metric" + 
			"&appid=" + API_KEY;
	
	private static String fQuery = "http://api.openweathermap.org/data/2.5/" + 
			"forecast/daily?" + 
			"id=" + "2037355" + 
			"&cnt=" + "4" +
			"&units=" + "metric" + 
			"&appid=" + API_KEY;

	/**
	 * Default empty constructor.
	 */
	public MainUpdateService() {
		super(MainUpdateService.class.getSimpleName());
	}

	/**
	 * Implementation of the superclass constructor.
	 * 
	 * @param name Used to name the worker thread, important only for debugging.
	 */
	public MainUpdateService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			storeQueryString();
			storeQueryResults();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		PendingIntent reply = intent.getParcelableExtra(EXTRA_PENDING_RESULT);
		try {
			reply.send();
		} catch (CanceledException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Query openweathermap.com getting results as JSON strings.
	 * 
	 * @param queryString The formed string that will submit to server. 
	 * @return the query results as JSON.
	 * @throws IOException something happened with I/O during query.
	 */
	protected static String queryAsJSON(String queryString) throws IOException {
		StringBuilder builder = new StringBuilder();
		
		URL url = new URL(queryString);
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
	
	private void storeQueryResults() throws IOException {
		SharedPreferences prefs = 
				PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String currentQuery = prefs.getString(CURRENT_QUERY, cQuery);
		String forecastQuery = prefs.getString(FORECAST_QUERY, fQuery);
		
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(CURRENT_WEATHER, queryAsJSON(currentQuery));
		editor.putString(FORECAST_WEATHER, queryAsJSON(forecastQuery));
		editor.apply();
	}
	
	private void storeQueryString() {
		SharedPreferences prefs = 
				PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		int cityId = prefs.getInt(CITY_ID, 2037355); 
		
		String currentQuery = "http://api.openweathermap.org/data/2.5/" +
				"weather?" + 
				"id=" + String.valueOf(cityId) + 
				"&units=" + "metric" + 
				"&appid=" + API_KEY;
		
		String forecastQuery =	"http://api.openweathermap.org/data/2.5/" + 
				"forecast/daily?" + 
				"id=" + String.valueOf(cityId) + 
				"&cnt=" + DAY_COUNT +
				"&units=" + "metric" + 
				"&appid=" + API_KEY;
	
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(CURRENT_QUERY, currentQuery);
		editor.putString(FORECAST_QUERY, forecastQuery);
		editor.apply();
	}
}
