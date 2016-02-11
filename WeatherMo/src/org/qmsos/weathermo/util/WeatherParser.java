package org.qmsos.weathermo.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * Utility class for several useful methods.
 * 
 *
 */
public class WeatherParser {
	
	private static final String TAG = WeatherParser.class.getSimpleName();
	
	public static final int FLAG_CURRENT = 1;
	public static final int FLAG_FORECAST = 2;
	
	// count of parsed weather, RELLY THINK BEFORE MODIFY!!!
	public static final int COUNT_ELEMENTS_CURRENT = 2;
	public static final int COUNT_ELEMENTS_FORECAST = 3;
	
	public static final int COUNT_FORECAST_DAY = 3;
	
	public static String parseRawToPattern(String raw, int flag) {
		if (raw == null) {
			return null;
		}

		StringBuilder builder = new StringBuilder();
		
		switch (flag) {
		case FLAG_CURRENT:
			try {
				JSONObject reader = new JSONObject(raw);
				JSONArray weather = reader.getJSONArray("weather");
				int weatherId = weather.getJSONObject(0).getInt("id");
				
				JSONObject main = reader.getJSONObject("main");
				int temperature = main.getInt("temp");
				
				builder.append(weatherId);
				builder.append('|');
				builder.append(temperature);
				
				return builder.toString();
			} catch (JSONException e) {
				Log.e(TAG, "Error parsing JSON string");
			}
		case FLAG_FORECAST:
			try {
				JSONObject reader = new JSONObject(raw);
				JSONArray list = reader.getJSONArray("list");
				int length = list.length();
				
				if (length < COUNT_FORECAST_DAY + 1) {
					return null;
				}
				
				int i;
				int count;
				
				long currentMillis = System.currentTimeMillis();
				JSONObject forecastFirst = list.getJSONObject(0);
				long firstTime = forecastFirst.getLong("dt");
				if (currentMillis > firstTime) {
					i = 1;
					count = COUNT_FORECAST_DAY + 1;
				} else {
					i = 0;
					count = COUNT_FORECAST_DAY;
				}
				
				while (i < count) {
					JSONObject forecast = list.getJSONObject(i);
					
					JSONArray weather = forecast.getJSONArray("weather");
					int weatherId = weather.getJSONObject(0).getInt("id");
					
					JSONObject temp = forecast.getJSONObject("temp");
					int temperatureMin = (int) Math.floor(temp.getDouble("min"));
					int temperatureMax = (int) Math.ceil(temp.getDouble("max"));
					
					builder.append(weatherId);
					builder.append('|');
					builder.append(temperatureMin);
					builder.append('|');
					builder.append(temperatureMax);
					if (i < length - 1) {
						builder.append(';');
					}
					
					i++;
				}
				
				return builder.toString();
			} catch (JSONException e) {
				Log.e(TAG, "Error parsing JSON string");
			}
		default:
			return null;
		}
	}
	
}
