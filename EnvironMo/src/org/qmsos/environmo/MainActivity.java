package org.qmsos.environmo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qmsos.environmo.CitySelectDialog.CitySelectListener;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.text.format.DateFormat;
import android.util.LongSparseArray;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Main activity of EnvironMo.
 * 
 * @author EnvironMo
 * 
 */
public class MainActivity extends Activity implements CitySelectListener {

	private CustomRefreshLayout swipeRefresh;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		swipeRefresh = (CustomRefreshLayout) findViewById(R.id.swipe_refresh);
		ScrollView scrollView = (ScrollView) findViewById(R.id.scroll_view);
		swipeRefresh.setScrollView(scrollView);
		swipeRefresh.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				new Handler().postDelayed(new Runnable() {

					@Override
					public void run() {
						swipeRefresh.setRefreshing(false);

						updateContent();
					}
				}, 500);
			}
		});

		updateUI();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		updateUI();
	}

	@Override
	public void onFinishCitySelectDialog(String cityName) {
		PendingIntent pendingResult = createPendingResult(0, new Intent(), 0);
	
		Intent intent = new Intent(getApplicationContext(), MainUpdateService.class);
		intent.putExtra(MainUpdateService.EXTRA_PENDING_RESULT, pendingResult);
		intent.putExtra(MainUpdateService.CITY_NAME, cityName);
		intent.putExtra(MainUpdateService.QUERY_CITY, true);
	
		startService(intent);
	}

	public void settingCity(View view) {
		CitySelectDialog citySelectDialog = new CitySelectDialog();
		citySelectDialog.show(getFragmentManager(), "select");
	}

	private void updateContent() {
		PendingIntent pendingResult = createPendingResult(0, new Intent(), 0);

		Intent intent = new Intent(getApplicationContext(), MainUpdateService.class);
		intent.putExtra(MainUpdateService.EXTRA_PENDING_RESULT, pendingResult);
		intent.putExtra(MainUpdateService.QUERY_WEATHER, true);

		startService(intent);
	}

	private CityInfo parseCityInfo() {
		CityInfo cityInfo = new CityInfo(0, null, null, 0, 0);

		try {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

			String currentWeatherResuls = prefs.getString(MainUpdateService.CURRENT_RESULT, "null");
			JSONObject reader = new JSONObject(currentWeatherResuls);

			int id = reader.getInt("id");
			String name = reader.getString("name");

			JSONObject sys = reader.getJSONObject("sys");
			String country = sys.getString("country");

			JSONObject coord = reader.getJSONObject("coord");
			double longitude = coord.getDouble("lon");
			double latitude = coord.getDouble("lat");

			cityInfo = new CityInfo(id, name, country, longitude, latitude);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return cityInfo;
	}

	private WeatherInfo parseCurrentSimple() {
		WeatherInfo currentWeather = new WeatherInfo(null, null);

		try {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

			String currentWeatherResults = prefs.getString(MainUpdateService.CURRENT_RESULT, "null");
			JSONObject reader = new JSONObject(currentWeatherResults);

			JSONArray weather = reader.getJSONArray("weather");
			String weatherMain = weather.getJSONObject(0).getString("main");
			String weatherDescription = weather.getJSONObject(0).getString("description");

			JSONObject main = reader.getJSONObject("main");
			int temperature = main.getInt("temp");

			currentWeather = new WeatherInfo(weatherMain, weatherDescription);
			currentWeather.setTemperature(temperature);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return currentWeather;
	}
	
	private WeatherInfo parseCurrentComplex() {
		WeatherInfo currentWeather = new WeatherInfo(null, null);
		
		try {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			
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

	private LongSparseArray<WeatherInfo> parseForecastSimple() {
		LongSparseArray<WeatherInfo> forecastWeather = new LongSparseArray<WeatherInfo>();
		try {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

			String forecastWeatherResults = prefs.getString(MainUpdateService.FORECAST_RESULT, "null");
			JSONObject reader = new JSONObject(forecastWeatherResults);

			JSONArray list = reader.getJSONArray("list");
			for (int i = 0; i < list.length(); i++) {
				JSONObject forecast = list.getJSONObject(i);

				long date = forecast.getLong("dt");

				JSONArray weather = forecast.getJSONArray("weather");
				String weatherMain = weather.getJSONObject(0).getString("main");
				String weatherDescription = weather.getJSONObject(0).getString("description");

				JSONObject temp = forecast.getJSONObject("temp");
				int temperatureMin = temp.getInt("min");
				int temperatureMax = temp.getInt("max");

				WeatherInfo weatherInfo = new WeatherInfo(weatherMain, weatherDescription);
				weatherInfo.setTemperatureMin(temperatureMin);
				weatherInfo.setTemperatureMax(temperatureMax);

				forecastWeather.put(date, weatherInfo);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return forecastWeather;
	}
	
	private LongSparseArray<WeatherInfo> parseForecastComplex() {
		LongSparseArray<WeatherInfo> forecastWeather = new LongSparseArray<WeatherInfo>();
		try {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			
			String forecastWeatherResults = prefs.getString(MainUpdateService.FORECAST_RESULT, "null");
			JSONObject reader = new JSONObject(forecastWeatherResults);
			
			JSONArray list = reader.getJSONArray("list");
			for (int i = 0; i < list.length(); i++) {
				JSONObject forecast = list.getJSONObject(i);
				
				long date = forecast.getLong("dt");
				
				JSONArray weather = forecast.getJSONArray("weather");
				String weatherMain = weather.getJSONObject(0).getString("main");
				String weatherDescription = weather.getJSONObject(0).getString("description");
				
				JSONObject temp = forecast.getJSONObject("temp");
				int temperatureMin = temp.getInt("min");
				int temperatureMax = temp.getInt("max");
				
				int pressure = forecast.getInt("pressure");
				int humidity = forecast.getInt("humidity");
				
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
		
		return forecastWeather;
	}

	private void updateUI() {
		updateCityUI(parseCityInfo());
		updateCurrentUI(parseCurrentSimple());
		updateForecastUI(parseForecastSimple());
	}

	private void updateCityUI(CityInfo cityInfo) {
		TextView textView = (TextView) findViewById(R.id.dialog_city_name);
		if (cityInfo.getName() != null) {
			textView.setText(cityInfo.getName());
		}

		textView = (TextView) findViewById(R.id.city_country);
		if (cityInfo.getCountry() != null) {
			textView.setText(cityInfo.getCountry());
		}

		textView = (TextView) findViewById(R.id.city_coordinates);
		double latitude = cityInfo.getLatitude();
		String latString = latitude > 0 ? String.valueOf(latitude) + "\u00B0N" : String.valueOf(latitude) + "\u00B0S";
		double longitude = cityInfo.getLongitude();
		String lonString = longitude > 0 ? String.valueOf(longitude) + "\u00B0E"
				: String.valueOf(longitude) + "\u00B0W";
		textView.setText(latString + "/" + lonString);

	}

	private void updateCurrentUI(WeatherInfo currentWeather) {
		TextView textView = (TextView) findViewById(R.id.current_temperature);
		String temperatureString = String.valueOf(currentWeather.getTemperature()) + "\u00b0";
		textView.setText(temperatureString);

		textView = (TextView) findViewById(R.id.current_description);
		String weatherDescription = currentWeather.getWeatherDescription();
		if (weatherDescription != null) {
			textView.setText(weatherDescription);
		}

	}

	private void updateCurrentUIComplex(WeatherInfo currentWeather) {
		updateCurrentUI(currentWeather);
		
		TextView textView = (TextView) findViewById(R.id.current_wind_speed);
		textView.setText(String.valueOf(currentWeather.getWindSpeed()) + "m/s");
		
		textView = (TextView) findViewById(R.id.current_wind_direction);
		textView.setText(String.valueOf(currentWeather.getWindDirection()) + "\u00b0");
		
		textView = (TextView) findViewById(R.id.current_pressure);
		textView.setText(String.valueOf(currentWeather.getPressure()) + "hPa");

		textView = (TextView) findViewById(R.id.current_humidity);
		textView.setText(String.valueOf(currentWeather.getHumidity()) + "%");

		textView = (TextView) findViewById(R.id.current_humidity);
		textView.setText(String.valueOf(currentWeather.getHumidity()) + "%");

		textView = (TextView) findViewById(R.id.current_visibility);
		textView.setText(String.valueOf(currentWeather.getVisibility() / 1000) + "km");
		
		textView = (TextView) findViewById(R.id.current_cloudiness);
		textView.setText(String.valueOf(currentWeather.getCloudiness()) + "%");

		textView = (TextView) findViewById(R.id.current_sunrise);
		String timeString = DateFormat.format("HH:mm", currentWeather.getSunrise() * 1000).toString();
		textView.setText(timeString);

		textView = (TextView) findViewById(R.id.current_sunset);
		timeString = DateFormat.format("HH:mm", currentWeather.getSunset() * 1000).toString();
		textView.setText(timeString);
	}
	
	private void updateForecastUI(LongSparseArray<WeatherInfo> forecastWeather) {
		for (int i = 0; i < forecastWeather.size(); i++) {
			long date = forecastWeather.keyAt(i);
			WeatherInfo forecast = forecastWeather.get(date);

			if (forecast != null) {
				TextView textView = (TextView) findViewById(
						getResources().getIdentifier("forecast_date_" + i, "id", getPackageName()));
				String dateString = DateFormat.format("MM-dd", date * 1000).toString();
				textView.setText(dateString);

				textView = (TextView) findViewById(
						getResources().getIdentifier("forecast_temperature_" + i, "id", getPackageName()));
				String temperatureString = String.valueOf(forecast.getTemperatureMin()) + "\u00B0" + "/"
						+ String.valueOf(forecast.getTemperatureMax()) + "\u00B0";
				textView.setText(temperatureString);

				textView = (TextView) findViewById(
						getResources().getIdentifier("forecast_main_" + i, "id", getPackageName()));
				textView.setText(forecast.getWeatherMain());
			}
		}
	}

}