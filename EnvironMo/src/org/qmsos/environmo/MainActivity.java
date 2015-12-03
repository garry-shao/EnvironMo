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
		citySelectDialog.show(getFragmentManager(), "selectCity");
	}
	
	public void showWeatherPlus(View view) {
		WeatherPlusDialog weatherPlusDialog = new WeatherPlusDialog();
		
		weatherPlusDialog.show(getFragmentManager(), "weatherPlus");
	}

	private void updateContent() {
		PendingIntent pendingResult = createPendingResult(0, new Intent(), 0);

		Intent intent = new Intent(getApplicationContext(), MainUpdateService.class);
		intent.putExtra(MainUpdateService.EXTRA_PENDING_RESULT, pendingResult);
		intent.putExtra(MainUpdateService.QUERY_WEATHER, true);

		startService(intent);
	}

	private City parseCity() {
		City city = new City(0, null, null, 0, 0);

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

			city = new City(id, name, country, longitude, latitude);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return city;
	}

	private Weather parseCurrentSimple() {
		Weather currentWeather = new Weather(null, null);

		try {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

			String currentWeatherResults = prefs.getString(MainUpdateService.CURRENT_RESULT, "null");
			JSONObject reader = new JSONObject(currentWeatherResults);

			JSONArray weather = reader.getJSONArray("weather");
			String weatherMain = weather.getJSONObject(0).getString("main");
			String weatherDescription = weather.getJSONObject(0).getString("description");

			JSONObject main = reader.getJSONObject("main");
			int temperature = main.getInt("temp");

			currentWeather = new Weather(weatherMain, weatherDescription);
			currentWeather.setTemperature(temperature);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return currentWeather;
	}
	
	private LongSparseArray<Weather> parseForecastSimple() {
		LongSparseArray<Weather> forecastWeather = new LongSparseArray<Weather>();
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

				Weather weatherInfo = new Weather(weatherMain, weatherDescription);
				weatherInfo.setTemperatureMin(temperatureMin);
				weatherInfo.setTemperatureMax(temperatureMax);

				forecastWeather.put(date, weatherInfo);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return forecastWeather;
	}
	
	private void updateUI() {
		updateCityUI(parseCity());
		updateCurrentUI(parseCurrentSimple());
		updateForecastUI(parseForecastSimple());
	}

	private void updateCityUI(City city) {
		TextView textView = (TextView) findViewById(R.id.dialog_city_name);
		if (city.getName() != null) {
			textView.setText(city.getName());
		}

		textView = (TextView) findViewById(R.id.city_country);
		if (city.getCountry() != null) {
			textView.setText(city.getCountry());
		}

		textView = (TextView) findViewById(R.id.city_coordinates);
		double latitude = city.getLatitude();
		String latString = latitude > 0 ? String.valueOf(latitude) + "\u00B0N" : String.valueOf(latitude) + "\u00B0S";
		double longitude = city.getLongitude();
		String lonString = longitude > 0 ? String.valueOf(longitude) + "\u00B0E"
				: String.valueOf(longitude) + "\u00B0W";
		textView.setText(latString + "/" + lonString);
	}

	private void updateCurrentUI(Weather currentWeather) {
		TextView textView = (TextView) findViewById(R.id.current_temperature);
		String temperatureString = String.valueOf(currentWeather.getTemperature()) + "\u00b0";
		textView.setText(temperatureString);

		textView = (TextView) findViewById(R.id.current_description);
		String weatherDescription = currentWeather.getWeatherDescription();
		if (weatherDescription != null) {
			textView.setText(weatherDescription);
		}

	}

	private void updateForecastUI(LongSparseArray<Weather> forecastWeather) {
		for (int i = 0; i < forecastWeather.size(); i++) {
			long date = forecastWeather.keyAt(i);
			Weather forecast = forecastWeather.get(date);

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