package org.qmsos.weathermo.util;

import android.util.Log;
import android.util.SparseIntArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Utility class that used to parse weather string from the raw response of remote server.
 */
public class WeatherParser {
	
	private static final String TAG = WeatherParser.class.getSimpleName();
	
	/**
	 * Forecast range in days, this is <b>critical</b> because fragment of forecast weather
	 * may dependent on this value.
	 */
	public static final int FORECAST_IN_DAYS = 3;
	
	/**
	 * Invalid value of temperature, make sure below <b>Kelvin Zero</b>, or "<b>Absolute Zero</b>".
	 */
	public static final int INVALID_TEMPERATURE = -274;
	
	/**
	 * Invalid value of weather id.
	 */
	public static final int INVALID_WEATHER_ID = 0;
	
	/**
	 * Invalid value of ultra-violet radiation.
	 */
	public static final double INVALID_UV_INDEX = -1.0f;
	
	/**
	 * Parse the valid current weather values from the raw results.
	 * 
	 * @param raw
	 *            The raw results from remote server.
	 * @return The formatted string used in content provider(have multiple elements)
	 *         or Null if the raw results are invalid.
	 */
	public static String parseRawToCurrent(String raw) {
		try {
			JSONObject reader = new JSONObject(raw);
			JSONArray weather = reader.getJSONArray("weather");
			int weatherId = weather.getJSONObject(0).getInt("id");
			
			JSONObject main = reader.getJSONObject("main");
			int temperature = main.getInt("temp");
			
			// data sequence, important!
            return String.valueOf(weatherId) + '|' + temperature;
		} catch (JSONException e) {
			Log.e(TAG, "Error parsing JSON string. " + e.getMessage());
			
			return null;
		}
	}
	
	/**
	 * Parse the valid forecast weather values from the raw results.
	 * <br>
	 * <br>
	 * Notice: because the configuration of remote server always change, here providing two
	 * methods of the forecast feature, these two methods are exclusive, avoid using at the
	 * same time, see {@link #parseRawToForecastsHourly(String)}.
	 * <br>
	 * <br>
	 * Notice: normally {@link #parseRawToForecastsHourly(String)} is <b>preferred</b>.
	 * 
	 * @param raw
	 *            The raw results from remote server.
	 * @return The formatted array of strings used in content provider
	 *         (have multiple elements) or Null if the raw results are invalid.
	 */
	public static String[] parseRawToForecastsDaily(String raw) {
		JSONArray list;
		int length;
		try {
			JSONObject reader = new JSONObject(raw);
			list = reader.getJSONArray("list");
			length = list.length();
		} catch (JSONException e) {
			Log.e(TAG, "Error parsing JSON string. " + e.getMessage());
			
			return null;
		}
		
		if (length < FORECAST_IN_DAYS + 1) {
			Log.e(TAG, "raw data does not have enough information");
			
			return null;
		}
		
		int i;
		int count;
		long firstMillis;
		try {
			JSONObject forecastFirst = list.getJSONObject(0);
			firstMillis = forecastFirst.getLong("dt");
		} catch (JSONException e) {
			Log.e(TAG, "Error parsing JSON string. " + e.getMessage());
			
			return null;
		}
		
		long currentMillis = System.currentTimeMillis();
		if (currentMillis > firstMillis) {
			i = 1;
			count = FORECAST_IN_DAYS + 1;
		} else {
			i = 0;
			count = FORECAST_IN_DAYS;
		}
		
		String[] parsed = new String[FORECAST_IN_DAYS];
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
                String builder = String.valueOf(weatherId) + '|' +
                        temperatureMin + '|' + temperatureMax;

                parsed[j] = builder;
				i++;
				j++;
			}
			
			return parsed;
		} catch (JSONException e) {
			Log.e(TAG, "Error parsing JSON string. " + e.getMessage());
			
			return null;
		}
	}
	
	/**
	 * Parse the valid forecast weather values from the raw results.
	 * <br>
	 * <br>
     * Notice: because the configuration of remote server always change, here providing two
     * methods of the forecast feature, these two methods are exclusive, avoid using at the
     * same time, see {@link #parseRawToForecastsDaily(String)}.
	 * <br>
	 * <br>
	 * Notice: this method is <b>preferred</b> due to accuracy and remote server policy.
	 * 
	 * @param raw
	 *            The raw results from remote server.
	 * @return The formatted array of strings used in content provider
	 *         (have multiple elements) or Null if the raw results are invalid.
	 */
	public static String[] parseRawToForecastsHourly(String raw) {
		JSONArray list;
		int length;
		try {
			JSONObject reader = new JSONObject(raw);
			list = reader.getJSONArray("list");
			length = list.length();
		} catch (JSONException e) {
			Log.e(TAG, "Error parsing JSON string. " + e.getMessage());
			
			return null;
		}
		
		if (length < FORECAST_IN_DAYS * Contract.DATA_IN_ONE_DAY) {
			Log.e(TAG, "raw data does not have enough information");
			
			return null;
		}
		
		String[] parsed = new String[FORECAST_IN_DAYS];
		for (int i = 0; i < FORECAST_IN_DAYS; i++) {
			SparseIntArray tempWeatherIds = new SparseIntArray(); 
			ArrayList<Double> tempTemperatures = new ArrayList<>();
			for (int j = 0; j < Contract.DATA_IN_ONE_DAY; j++) {
				int weatherId;
				double temperature;
				try {
					JSONObject forecast = list.getJSONObject(j + Contract.DATA_IN_ONE_DAY * i);
					JSONArray weather = forecast.getJSONArray("weather");
					weatherId = weather.getJSONObject(0).getInt("id");
					
					JSONObject main = forecast.getJSONObject("main");
					temperature = main.getInt("temp");
				} catch (JSONException e) {
					Log.e(TAG, "Error parsing JSON string. " + e.getMessage());
					
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

            String builder = String.valueOf(weatherId) + '|' +
                    temperatureMin + '|' + temperatureMax;

            parsed[i] = builder;
		}
		
		return parsed;
	}

	/**
	 * Parse the valid ultra-violet radiation value from the raw results.
	 * 
	 * @param raw
	 *            The raw results from remote server.
	 * @return The valid uv index of {@link #INVALID_UV_INDEX}.
	 */
	public static double parseRawToUvIndex(String raw) {
		try {
			JSONObject reader = new JSONObject(raw);

			return reader.getDouble("value");
		} catch (JSONException e) {
			Log.e(TAG, "Error parsing JSON string. " + e.getMessage());
			
			return INVALID_UV_INDEX;
		}
	}
	
	/**
	 * Get weather id from raw string from content provider.
	 * <br>
	 * <br>
	 * Notice: the raw string is already parsed when they are stored into
	 * the content provider, but that still contains multiple elements(weather 
	 * id, temperature etc.), so here are still described as "raw" string.
	 * 
	 * @param weatherRaw
	 *           The string retrieved from content provider.
	 * @return The valid weather id or {@link #INVALID_WEATHER_ID}.
	 */
	public static int getWeatherId(String weatherRaw) {
		if (weatherRaw == null) {
			return INVALID_WEATHER_ID;
		}
		
		String[] elements = weatherRaw.split("\\|");

        int length = elements.length;
		if ((length == Contract.SEGMENTS_CURRENT) || (length == Contract.SEGMENTS_FORECAST)) {
			return Integer.parseInt(elements[0]);
		} else {
			return INVALID_WEATHER_ID;
		}
	}
	
	/**
	 * Get temperature from raw string of current weather from content provider.
	 * <br>
	 * <br>
	 * Notice: only use this when retrieving from current weather, or else the result
	 * is invalid.
	 * 
	 * @param weatherRaw
	 *           The string of current weather retrieved from content provider.
	 * @return The valid temperature of current weather or {@link #INVALID_TEMPERATURE}.
	 */
	public static int getTemperature(String weatherRaw) {
		if (weatherRaw == null) {
			return INVALID_TEMPERATURE;
		}
		
		String[] elements = weatherRaw.split("\\|");

        int length = elements.length;
		if (length == Contract.SEGMENTS_CURRENT) {
			return Integer.parseInt(elements[1]);
		} else {
			return INVALID_TEMPERATURE;
		}
	}
	
	/**
	 * Get the minimum temperature from raw string of forecast weather from content provider.
	 * <br>
	 * <br>
	 * Notice: only use this when retrieving from forecast weather, or else the result
	 * is invalid.
	 * 
	 * @param weatherRaw
	 *           The string of forecast weather retrieved from content provider.
	 * @return The valid minimum temperature of forecast weather or {@link #INVALID_TEMPERATURE}.
	 */
	public static int getTemperatureMin(String weatherRaw) {
		if (weatherRaw == null) {
			return INVALID_TEMPERATURE;
		}
		
		String[] elements = weatherRaw.split("\\|");

        int length = elements.length;
		if (length == Contract.SEGMENTS_FORECAST) {
			return Integer.parseInt(elements[1]);
		} else {
			return INVALID_TEMPERATURE;
		}
	}
	
	/**
	 * Get the maximum temperature from raw string of forecast weather from content provider.
	 * <br>
	 * <br>
	 * Notice: only use this when retrieving from forecast weather, or else the result
	 * is invalid.
	 * 
	 * @param weatherRaw
	 *           The string of forecast weather retrieved from content provider.
	 * @return The valid maximum temperature of forecast weather or {@link #INVALID_TEMPERATURE}.
	 */
	public static int getTemperatureMax(String weatherRaw) {
		if (weatherRaw == null) {
			return INVALID_TEMPERATURE;
		}
		
		String[] elements = weatherRaw.split("\\|");

        int length = elements.length;
		if (length == Contract.SEGMENTS_FORECAST) {
			return Integer.parseInt(elements[2]);
		} else {
			return INVALID_TEMPERATURE;
		}
	}
	
	/**
	 * Containing contract of parsed string that describes weather. 
	 */
	private static class Contract {
		
		/**
		 * How many segments that contained in parsed current weather.
		 */
		static final int SEGMENTS_CURRENT = 2;
		
		/**
		 * How many segments that contained in parsed forecast weather.
		 */
		static final int SEGMENTS_FORECAST = 3;
		
		/**
		 * How many data points in a whole day(24H) when performing forecast by hours.
		 */
		static final int DATA_IN_ONE_DAY = 8;
	}

}
