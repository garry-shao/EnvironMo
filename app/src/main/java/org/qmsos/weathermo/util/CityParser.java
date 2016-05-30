package org.qmsos.weathermo.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qmsos.weathermo.datamodel.City;

/**
 * Utility class that used to parse response from the search that by city name.
 */
public class CityParser {
	
	/**
	 * Parse response of the search that by city name to a formatted array of 
	 * city presenting as candidates. 
	 * 
	 * @param result
	 *            The raw response of the search action.
	 * @return The parsed array of candidates.
	 */
	public static City[] parseResult(String result) {
		if (result == null) {
			return null;
		}

		JSONArray list = null;
		int length = 0;
		try {
			JSONObject reader = new JSONObject(result);
			list = reader.getJSONArray("list");
			length = list.length();
		} catch (JSONException e) {
			return null;
		}
		
		if (length == 0) {
			return null;
		}

		City[] candidates = new City[length];
		for (int i = 0; i < length; i++) {
			try {
				JSONObject instance = list.getJSONObject(i);

				long cityId = instance.getLong("id");
				String name = instance.getString("name");

				JSONObject coord = instance.getJSONObject("coord");
				double longitude = coord.getDouble("lon");
				double latitude = coord.getDouble("lat");

				JSONObject sys = instance.getJSONObject("sys");
				String country = sys.getString("country");

				longitude = longitude * 100;
				longitude = Math.round(longitude);
				longitude = longitude / 100;

				latitude = latitude * 100;
				latitude = Math.round(latitude);
				latitude = latitude / 100;

				candidates[i] = new City(cityId, name, country, longitude, latitude);
			} catch (JSONException e) {
				return null;
			}
		}
		
		return candidates;
	}
	
}