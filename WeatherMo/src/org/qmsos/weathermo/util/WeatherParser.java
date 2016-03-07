package org.qmsos.weathermo.util;

import java.util.ArrayList;
import java.util.Collections;

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
	
	// count of parsed weather, RELLY THINK BEFORE MODIFY!!!
	private static final int COUNT_ELEMENTS_CURRENT = 2;
	private static final int COUNT_ELEMENTS_FORECAST = 3;
	
	// count of forecast parameters, the website provider often change so in case here.
	public static final int COUNT_FORECAST_DAYS = 3;
	public static final int COUNT_FORECAST_HOURS = 8;
	
	// Invalid constant values of various parameters.
	public static final int INVALID_TEMPERATURE = -274;
	public static final int INVALID_WEATHER_ID = 0;
	public static final double INVALID_UV_INDEX = -1.0f;
	
	public static String parseRawToCurrent(String raw) {
		try {
			JSONObject reader = new JSONObject(raw);
			JSONArray weather = reader.getJSONArray("weather");
			int weatherId = weather.getJSONObject(0).getInt("id");
			
			JSONObject main = reader.getJSONObject("main");
			int temperature = main.getInt("temp");
			
			// data sequence, important!
			StringBuilder builder = new StringBuilder();
			builder.append(weatherId);
			builder.append('|');
			builder.append(temperature);
			
			return builder.toString();
		} catch (JSONException e) {
			Log.e(TAG, "Error parsing JSON string");
			
			return null;
		}
	}
	
	public static String[] parseRawToForecastsDaily(String raw) {
		JSONArray list = null;
		int length = 0;
		try {
			JSONObject reader = new JSONObject(raw);
			list = reader.getJSONArray("list");
			length = list.length();
		} catch (JSONException e) {
			Log.e(TAG, "Error parsing JSON string");
			
			return null;
		}
		
		if (list == null || length < COUNT_FORECAST_DAYS + 1) {
			Log.e(TAG, "raw data does not have enough information");
			
			return null;
		}
		
		int i;
		int count;
		long firstMillis = 0;
		try {
			JSONObject forecastFirst = list.getJSONObject(0);
			firstMillis = forecastFirst.getLong("dt");
		} catch (JSONException e) {
			Log.e(TAG, "Error parsing JSON string");
			
			return null;
		}
		
		long currentMillis = System.currentTimeMillis();
		if (currentMillis > firstMillis) {
			i = 1;
			count = COUNT_FORECAST_DAYS + 1;
		} else {
			i = 0;
			count = COUNT_FORECAST_DAYS;
		}
		
		String[] parsed = new String[COUNT_FORECAST_DAYS];
		int j = 0;
		try {
			while (i < count) {
				JSONObject forecast = list.getJSONObject(i);
				
				JSONArray weather = forecast.getJSONArray("weather");
				int weatherId = weather.getJSONObject(0).getInt("id");
				
				JSONObject temp = forecast.getJSONObject("temp");
				int temperatureMin = (int) Math.floor(temp.getDouble("min"));
				int temperatureMax = (int) Math.ceil(temp.getDouble("max"));
				
				// data sequence, important!
				StringBuilder builder = new StringBuilder();
				builder.append(weatherId);
				builder.append('|');
				builder.append(temperatureMin);
				builder.append('|');
				builder.append(temperatureMax);
				
				parsed[j] = builder.toString();
				i++;
				j++;
			}
			
			return parsed;
		} catch (JSONException e) {
			Log.e(TAG, "Error parsing JSON string");
			
			return null;
		}
	}
	
	public static String[] parseRawToForecastsHourly(String raw) {
		JSONArray list = null;
		int length = 0;
		try {
			JSONObject reader = new JSONObject(raw);
			list = reader.getJSONArray("list");
			length = list.length();
		} catch (JSONException e) {
			Log.e(TAG, "Error parsing JSON string");
			
			return null;
		}
		
		if (list == null || length < COUNT_FORECAST_DAYS * COUNT_FORECAST_HOURS) {
			Log.e(TAG, "raw data does not have enough information");
			
			return null;
		}
		
		String[] parsed = new String[COUNT_FORECAST_DAYS];
		for (int i = 0; i < COUNT_FORECAST_DAYS; i++) {
			SparseIntArray tempWeatherIds = new SparseIntArray(); 
			ArrayList<Double> tempTemperatures = new ArrayList<Double>();
			for (int j = 0; j < COUNT_FORECAST_HOURS; j++) {
				int weatherId;
				double temperature;
				try {
					JSONObject forecast = list.getJSONObject(j + COUNT_FORECAST_HOURS * i);
					JSONArray weather = forecast.getJSONArray("weather");
					weatherId = weather.getJSONObject(0).getInt("id");
					
					JSONObject main = forecast.getJSONObject("main");
					temperature = main.getInt("temp");
				} catch (JSONException e) {
					Log.e(TAG, "Error parsing JSON string");
					
					return null;
				}
				
				int tempWeatherIdCounts = tempWeatherIds.get(weatherId);
				if (tempWeatherIdCounts > 0) {
					tempWeatherIdCounts++;
					tempWeatherIds.put(weatherId, tempWeatherIdCounts);
				} else {
					tempWeatherIds.put(weatherId, 1);
				}
				tempTemperatures.add(temperature);
			}
			
			int flagWeatherId = 0;
			int flagCount = 0;
			for (int k = 0; k < tempWeatherIds.size(); k++) {
				int tempKey = tempWeatherIds.keyAt(k);
				int tempCounts = tempWeatherIds.get(tempKey);
				if (flagCount < tempCounts) {
					flagWeatherId = tempKey;
					flagCount = tempCounts;
				}
			}
			
			int weatherId = flagWeatherId;
			int temperatureMin = (int) Math.floor(Collections.min(tempTemperatures));
			int temperatureMax = (int) Math.ceil(Collections.max(tempTemperatures));
			
			StringBuilder builder = new StringBuilder();
			builder.append(weatherId);
			builder.append('|');
			builder.append(temperatureMin);
			builder.append('|');
			builder.append(temperatureMax);
			
			parsed[i] = builder.toString();
		}
		
		return parsed;
	}

	public static double parseRawToUvIndex(String raw) {
		try {
			JSONObject reader = new JSONObject(raw);
			double value = reader.getDouble("value");
			
			return value;
		} catch (JSONException e) {
			Log.e(TAG, "Error parsing JSON string");
			
			return INVALID_UV_INDEX;
		}
	}
	
	public static int getWeatherId(String weatherRaw) {
		if (weatherRaw == null) {
			return INVALID_WEATHER_ID;
		}
		
		String[] elements = weatherRaw.split("\\|");
		if (elements == null) {
			return INVALID_WEATHER_ID;
		}
		
		int length = elements.length;
		if ((length == COUNT_ELEMENTS_CURRENT) || (length == COUNT_ELEMENTS_FORECAST)) {
			return Integer.parseInt(elements[0]);
		} else {
			return INVALID_WEATHER_ID;
		}
	}
	
	public static int getTemperature(String weatherRaw) {
		if (weatherRaw == null) {
			return INVALID_TEMPERATURE;
		}
		
		String[] elements = weatherRaw.split("\\|");
		if (elements == null) {
			return INVALID_TEMPERATURE;
		}
		
		int length = elements.length;
		if (length == COUNT_ELEMENTS_CURRENT) {
			return Integer.parseInt(elements[1]);
		} else {
			return INVALID_TEMPERATURE;
		}
	}
	
	public static int getTemperatureMin(String weatherRaw) {
		if (weatherRaw == null) {
			return INVALID_TEMPERATURE;
		}
		
		String[] elements = weatherRaw.split("\\|");
		if (elements == null) {
			return INVALID_TEMPERATURE;
		}
		
		int length = elements.length;
		if (length == COUNT_ELEMENTS_FORECAST) {
			return Integer.parseInt(elements[1]);
		} else {
			return INVALID_TEMPERATURE;
		}
	}
	
	public static int getTemperatureMax(String weatherRaw) {
		if (weatherRaw == null) {
			return INVALID_TEMPERATURE;
		}
		
		String[] elements = weatherRaw.split("\\|");
		if (elements == null) {
			return INVALID_TEMPERATURE;
		}
		
		int length = elements.length;
		if (length == COUNT_ELEMENTS_FORECAST) {
			return Integer.parseInt(elements[2]);
		} else {
			return INVALID_TEMPERATURE;
		}
	}

}
