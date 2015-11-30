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
	protected void onResume() {
		super.onResume();
		
		updateContent();
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

	private WeatherInfo parseCurrentWeather() {
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
			// int pressure = main.getInt("pressure");
			// int humidity = main.getInt("humidity");

			// JSONObject wind = reader.getJSONObject("wind");
			// int windSpeed = wind.getInt("speed");
			// int windDirection = wind.getInt("deg");

			currentWeather = new WeatherInfo(weatherMain, weatherDescription);
			currentWeather.setTemperature(temperature);
			// currentWeather.setPressure(pressure);
			// currentWeather.setHumidity(humidity);
			// currentWeather.setWindSpeed(windSpeed);
			// currentWeather.setWindDirection(windDirection);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return currentWeather;
	}

	private LongSparseArray<WeatherInfo> parseForecastWeather() {
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

				// int pressure = forecast.getInt("pressure");
				// int humidity = forecast.getInt("humidity");

				// int windSpeed = forecast.getInt("speed");
				// int windDirection = forecast.getInt("deg");

				WeatherInfo weatherInfo = new WeatherInfo(weatherMain, weatherDescription);
				weatherInfo.setTemperatureMin(temperatureMin);
				weatherInfo.setTemperatureMax(temperatureMax);
				// weatherInfo.setPressure(pressure);
				// weatherInfo.setHumidity(humidity);
				// weatherInfo.setWindSpeed(windSpeed);
				// weatherInfo.setWindDirection(windDirection);

				forecastWeather.put(date, weatherInfo);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return forecastWeather;
	}

	private void updateUI() {
		updateCityUI(parseCityInfo());
		updateCurrentUI(parseCurrentWeather());
		updateForecastUI(parseForecastWeather());
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
		String temperatureString = String.valueOf(currentWeather.getTemperature()) + "\u00b0" + "C";
		textView.setText(temperatureString);

		textView = (TextView) findViewById(R.id.current_main);
		if (currentWeather.getWeatherMain() != null) {
			textView.setText(currentWeather.getWeatherMain());
		}

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