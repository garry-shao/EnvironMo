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
	
	// count of parsed weather, RELLY THINK BEFORE MODIFY!!!
	private static final int COUNT_ELEMENTS_CURRENT = 2;
	private static final int COUNT_ELEMENTS_FORECAST = 3;
	
	public static final int COUNT_FORECAST_DAYS = 3;
	public static final int TEMPERATURE_INVALID = -274;
	public static final int WEATHER_ID_INVALID = 0;
	
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
	
	public static String parseRawToForecast(String raw) {
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
		
		try {
			StringBuilder builder = new StringBuilder();
			while (i < count) {
				JSONObject forecast = list.getJSONObject(i);
				
				JSONArray weather = forecast.getJSONArray("weather");
				int weatherId = weather.getJSONObject(0).getInt("id");
				
				JSONObject temp = forecast.getJSONObject("temp");
				int temperatureMin = (int) Math.floor(temp.getDouble("min"));
				int temperatureMax = (int) Math.ceil(temp.getDouble("max"));
				
				// data sequence, important!
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
			
			return null;
		}	
	}
	
	public static int getCurrentWeatherId(String current) {
		if (current == null) {
			return WEATHER_ID_INVALID;
		}
		
		String[] elements = current.split("\\|");
		if (elements != null && (elements.length == COUNT_ELEMENTS_CURRENT)) {
			return Integer.parseInt(elements[0]);
		} else {
			return WEATHER_ID_INVALID;
		}
	}
	
	public static int getCurrentTemperature(String current) {
		if (current == null) {
			return TEMPERATURE_INVALID;
		}
		
		String[] elements = current.split("\\|");
		if (elements.length == COUNT_ELEMENTS_CURRENT) {
			return Integer.parseInt(elements[1]);
		} else {
			return TEMPERATURE_INVALID;
		}
	}

	public static int getForecastWeatherId(int day, String forecast) {
		if ((forecast == null) || (day < 0) || (day >= COUNT_FORECAST_DAYS)) {
			return WEATHER_ID_INVALID;
		}
		
		String[] elements = forecast.split(";");
		if (elements != null && (elements.length == COUNT_FORECAST_DAYS)) {
			String[] values = elements[day].split("\\|");
			if (values.length == COUNT_ELEMENTS_FORECAST) {
				return Integer.parseInt(values[0]);
			} else {
				return WEATHER_ID_INVALID;
			}
		} else {
			return WEATHER_ID_INVALID;
		}
	}

	public static int getForecastTemperatureMin(int day, String forecast) {
		if ((forecast == null) || (day < 0) || (day >= COUNT_FORECAST_DAYS)) {
			return TEMPERATURE_INVALID;
		}
		
		String[] elements = forecast.split(";");
		if (elements.length == COUNT_FORECAST_DAYS) {
			String[] values = elements[day].split("\\|");
			if (values.length == COUNT_ELEMENTS_FORECAST) {
				return Integer.parseInt(values[1]);
			} else {
				return TEMPERATURE_INVALID;
			}
		} else {
			return TEMPERATURE_INVALID;
		}
	}
	
	public static int getForecastTemperatureMax(int day, String forecast) {
		if ((forecast == null) || (day < 0) || (day >= COUNT_FORECAST_DAYS)) {
			return TEMPERATURE_INVALID;
		}
		
		String[] elements = forecast.split(";");
		if (elements.length == COUNT_FORECAST_DAYS) {
			String[] values = elements[day].split("\\|");
			if (values.length == COUNT_ELEMENTS_FORECAST) {
				return Integer.parseInt(values[2]);
			} else {
				return TEMPERATURE_INVALID;
			}
		} else {
			return TEMPERATURE_INVALID;
		}
	}

}
