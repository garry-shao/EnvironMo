package org.qmsos.weathermo.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.util.SparseIntArray;

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
				int count = list.length();
				if (count < 12) {
					return null;
				}
				
				for (int i = 0; i < 3; i++) {
					SparseIntArray a = new SparseIntArray(); 
					List<Integer> temps = new ArrayList<Integer>();
					for (int j = 0; j < 4; j++) {
						JSONObject forecast = list.getJSONObject(j + 4 * i);
						
						JSONArray weather = forecast.getJSONArray("weather");
						int weatherId = weather.getJSONObject(0).getInt("id");
						
						JSONObject main = forecast.getJSONObject("main");
						int temperature = main.getInt("temp");
						
						
						int value = a.get(weatherId);
						if (value > 0) {
							value++;
							a.put(weatherId, value);
						} else {
							a.put(weatherId, 1);
						}
						temps.add(temperature);
					}
					
					int weatherId = 0;
					int counts = 0;
					for (int k = 0; k < a.size(); k++) {
						int tempKey = a.keyAt(k);
						int tempCounts = a.get(tempKey);
						if (counts < tempCounts) {
							weatherId = tempKey;
							counts = tempCounts;
						}
					}
					
					int temperatureMax = Collections.max(temps);
					int temperatureMin = Collections.min(temps);
					
					builder.append(weatherId);
					builder.append('|');
					builder.append(temperatureMin);
					builder.append('|');
					builder.append(temperatureMax);
					if (i < 2) {
						builder.append(';');
					}
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
