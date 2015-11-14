package org.qmsos.environmo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.LongSparseArray;
import android.view.View;
import android.widget.TextView;

/**
 * Main activity of EnvironMo.
 * 
 * @author EnvironMo
 * 
 */
public class MainActivity extends Activity {
	
	private static CityInfo cityInfo = new CityInfo(0, null, null, 0, 0);
	private static WeatherInfo currentWeather = new WeatherInfo(null, null);
	private static LongSparseArray<WeatherInfo> forecastWeather = new LongSparseArray<WeatherInfo>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		
		updateUI();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		updateUI();
	}
	
	public void settingCity(View view) {
		startActivity(new Intent(getApplicationContext(), SettingCityActivity.class));
	}
	
	public void updateContent(View view) {
		PendingIntent pendingResult = createPendingResult(0, new Intent(), 0);
		Intent intent = new Intent(getApplicationContext(), MainUpdateService.class);
		intent.putExtra(MainUpdateService.EXTRA_PENDING_RESULT, pendingResult);
		startService(intent);
	}
	
	private void currentWeather() {
		try {
			SharedPreferences prefs = 
					PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

			String currentWeatherJSON = prefs.getString(MainUpdateService.CURRENT_WEATHER, "null");
			JSONObject reader = new JSONObject(currentWeatherJSON);
			
			int id = reader.getInt("id");
			String name = reader.getString("name");
			
			JSONObject sys = reader.getJSONObject("sys");
			String country = sys.getString("country");
			long sunrise = sys.getLong("sunrise");
			long sunset = sys.getLong("sunset");

			JSONObject coord = reader.getJSONObject("coord");
			double longitude = coord.getDouble("lon");
			double latitude = coord.getDouble("lat");
			
			JSONArray weather = reader.getJSONArray("weather");
			String weatherMain = weather.getJSONObject(0).getString("main");
			String weatherDescription = weather.getJSONObject(0).getString("description");
			
			JSONObject main = reader.getJSONObject("main");
			int temperature = main.getInt("temp");
			int pressure = main.getInt("pressure");
			int humidity = main.getInt("humidity");
			
			JSONObject wind = reader.getJSONObject("wind");
			int windSpeed = wind.getInt("speed");
			int windDirection = wind.getInt("deg");
			
			int visibility = reader.getInt("visibility");
			int cloudiness = reader.getJSONObject("clouds").getInt("all");
			
			currentWeather = new WeatherInfo(weatherMain, weatherDescription);
			currentWeather.setTemperature(temperature);
			currentWeather.setPressure(pressure);
			currentWeather.setHumidity(humidity);
			currentWeather.setWindSpeed(windSpeed);
			currentWeather.setWindDirection(windDirection);
			currentWeather.setVisibility(visibility);
			currentWeather.setCloudiness(cloudiness);
			
			cityInfo = new CityInfo(id, name, country, longitude, latitude);
			cityInfo.setSunrise(sunrise);
			cityInfo.setSunset(sunset);
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	private void forecastWeather() {
		try {
			SharedPreferences prefs = 
					PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

			String forecastWeatherJSON = prefs.getString(MainUpdateService.FORECAST_WEATHER, "null");
			JSONObject reader = new JSONObject(forecastWeatherJSON);
	
			JSONArray list = reader.getJSONArray("list");
			for (int i = 0; i < list.length(); i++) { 
				JSONObject forecast = list.getJSONObject(i);
				
				long date = forecast.getLong("dt");
				
				JSONObject temp = forecast.getJSONObject("temp");
				int temperatureMin = temp.getInt("min");
				int temperatureMax = temp.getInt("max");
				
				int pressure = forecast.getInt("pressure");
				int humidity = forecast.getInt("humidity");
			
				JSONArray weather = forecast.getJSONArray("weather");
				String weatherMain = weather.getJSONObject(0).getString("main");
				String weatherDescription = weather.getJSONObject(0).getString("description");
				
				int windSpeed = forecast.getInt("speed");
				int windDirection = forecast.getInt("deg");
				
				int cloudiness = forecast.getInt("clouds");
				
				WeatherInfo weatherInfo = new WeatherInfo(weatherMain, weatherDescription);
				weatherInfo.setTemperatureMin(temperatureMin);
				weatherInfo.setTemperatureMax(temperatureMax);
				weatherInfo.setPressure(pressure);
				weatherInfo.setHumidity(humidity);
				weatherInfo.setWindSpeed(windSpeed);
				weatherInfo.setWindDirection(windDirection);
				weatherInfo.setCloudiness(cloudiness);
				
				forecastWeather.put(date, weatherInfo);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void updateUI() {
		currentWeather();
		forecastWeather();
		
		updateCityUI();
		updateCurrentUI();
		updateForecastUI();
	}
	
	private void updateCityUI() {
		TextView textView = (TextView) findViewById(R.id.city_name);
		if (cityInfo.getName() != null) {
			textView.setText(cityInfo.getName());
		}

		textView = (TextView) findViewById(R.id.city_country);
		if (cityInfo.getCountry() != null) {
			textView.setText(cityInfo.getCountry());
		}

		textView = (TextView) findViewById(R.id.city_coordinates);
		double latitude = cityInfo.getLatitude();
		String latString = latitude > 0 ? 
				String.valueOf(latitude) + "\u00B0N" : String.valueOf(latitude) + "\u00B0S";
		double longitude = cityInfo.getLongitude();
		String lonString = longitude > 0 ? 
				String.valueOf(longitude) + "\u00B0E" : String.valueOf(longitude) + "\u00B0W";
		textView.setText(latString + "/" + lonString);
		
		textView = (TextView) findViewById(R.id.city_sunrise);
		textView.setText(
				DateFormat.format("HH:mm", cityInfo.getSunrise() * 1000).toString());
		
		textView = (TextView) findViewById(R.id.city_sunset);
		textView.setText(
				DateFormat.format("HH:mm", cityInfo.getSunset() * 1000).toString());
	}
	
	private void updateCurrentUI() {
		TextView textView = (TextView) findViewById(R.id.current_temperature);
		textView.setText(
				String.valueOf(currentWeather.getTemperature()) + "\u00B0C");

		textView = (TextView) findViewById(R.id.current_main);
		if (currentWeather.getWeatherMain() != null) {
			textView.setText(currentWeather.getWeatherMain());
		}
		
		textView = (TextView) findViewById(R.id.current_pressure);
		textView.setText(String.valueOf(currentWeather.getPressure()) + "hPa");
		
		textView = (TextView) findViewById(R.id.current_humidity);
		textView.setText(String.valueOf(currentWeather.getHumidity()) + "%");
		
		textView = (TextView) findViewById(R.id.current_wind_speed);
		textView.setText(String.valueOf(currentWeather.getWindSpeed()) + "m/s");
		
		textView = (TextView) findViewById(R.id.current_wind_direction);
		textView.setText(String.valueOf(currentWeather.getWindDirection()) + "\u00B0");
	
	}
	
	private void updateForecastUI() {
		for (int i = 0; i < 3; i++) {
			long date = forecastWeather.keyAt(i);
			WeatherInfo forecast = forecastWeather.get(date);

			if (forecast != null) {
				TextView textView = (TextView) findViewById(
						getResources().getIdentifier("forecast_date_" + i, "id", getPackageName()));
				textView.setText(DateFormat.format("MM-dd", date * 1000).toString());
				
				textView = (TextView) findViewById(
						getResources().getIdentifier("forecast_temperature_" + i, "id", getPackageName()));
				textView.setText(
						String.valueOf(forecast.getTemperatureMax()) + "\u00B0C/" + 
								String.valueOf(forecast.getTemperatureMin()) + "\u00B0C");
				
				textView = (TextView) findViewById(
						getResources().getIdentifier("forecast_main_" + i, "id", getPackageName()));
				textView.setText(forecast.getWeatherMain());
			}
		}
	}
	
}