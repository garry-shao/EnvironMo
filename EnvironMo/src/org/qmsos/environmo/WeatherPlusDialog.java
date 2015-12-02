package org.qmsos.environmo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class WeatherPlusDialog extends DialogFragment {

	public WeatherPlusDialog() {
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.dialog_weather_plus, container);
		
		updateWeatherPlus(view, parseWeatherPlus());
		
		getDialog().setTitle(R.string.dialog_weather_plus_title);
		
		return view;
	}

	private WeatherInfo parseWeatherPlus() {
		WeatherInfo currentWeather = new WeatherInfo(null, null);
		
		try {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
			
			String currentWeatherResults = prefs.getString(MainUpdateService.CURRENT_RESULT, "null");
			JSONObject reader = new JSONObject(currentWeatherResults);
			
			JSONArray weather = reader.getJSONArray("weather");
			String weatherMain = weather.getJSONObject(0).getString("main");
			String weatherDescription = weather.getJSONObject(0).getString("description");
			
			JSONObject main = reader.getJSONObject("main");
			int temperature = main.getInt("temp");
			int pressure = main.getInt("pressure");
			int humidity = main.getInt("humidity");
			
			int visibility = reader.getInt("visibility");
			
			JSONObject wind = reader.getJSONObject("wind");
			int windSpeed = wind.getInt("speed");
			int windDirection = wind.getInt("deg");
			
			JSONObject clouds = reader.getJSONObject("clouds");
			int cloudiness = clouds.getInt("all");
			
			JSONObject sys = reader.getJSONObject("sys");
			long sunrise = sys.getLong("sunrise");
			long sunset =sys.getLong("sunset");
			
			currentWeather = new WeatherInfo(weatherMain, weatherDescription);
			currentWeather.setTemperature(temperature);
			currentWeather.setPressure(pressure);
			currentWeather.setHumidity(humidity);
			currentWeather.setVisibility(visibility);
			currentWeather.setWindSpeed(windSpeed);
			currentWeather.setWindDirection(windDirection);
			currentWeather.setCloudiness(cloudiness);
			currentWeather.setSunrise(sunrise);
			currentWeather.setSunset(sunset);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return currentWeather;
	}
	
	private void updateWeatherPlus(View view, WeatherInfo currentWeather) {
		TextView textView = (TextView) view.findViewById(R.id.current_wind_speed);
		textView.setText(String.valueOf(currentWeather.getWindSpeed()) + "m/s");
		
		textView = (TextView) view.findViewById(R.id.current_wind_direction);
		textView.setText(String.valueOf(currentWeather.getWindDirection()) + "\u00b0");
		
		textView = (TextView) view.findViewById(R.id.current_pressure);
		textView.setText(String.valueOf(currentWeather.getPressure()) + "hPa");

		textView = (TextView) view.findViewById(R.id.current_humidity);
		textView.setText(String.valueOf(currentWeather.getHumidity()) + "%");

		textView = (TextView) view.findViewById(R.id.current_humidity);
		textView.setText(String.valueOf(currentWeather.getHumidity()) + "%");

		textView = (TextView) view.findViewById(R.id.current_visibility);
		textView.setText(String.valueOf(currentWeather.getVisibility() / 1000) + "km");
		
		textView = (TextView) view.findViewById(R.id.current_cloudiness);
		textView.setText(String.valueOf(currentWeather.getCloudiness()) + "%");

		textView = (TextView) view.findViewById(R.id.current_sunrise);
		String timeString = DateFormat.format("HH:mm", currentWeather.getSunrise() * 1000).toString();
		textView.setText(timeString);

		textView = (TextView) view.findViewById(R.id.current_sunset);
		timeString = DateFormat.format("HH:mm", currentWeather.getSunset() * 1000).toString();
		textView.setText(timeString);
	}
	
}
