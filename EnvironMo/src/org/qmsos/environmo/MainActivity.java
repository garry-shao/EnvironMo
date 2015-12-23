package org.qmsos.environmo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qmsos.environmo.CitySelectDialog.CitySelectListener;
import org.qmsos.environmo.data.City;
import org.qmsos.environmo.data.Weather;
import org.qmsos.environmo.util.UtilRefreshLayout;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
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

	private UtilRefreshLayout swipeRefresh;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		swipeRefresh = (UtilRefreshLayout) findViewById(R.id.swipe_refresh);
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

			return new City(id, name, country, longitude, latitude);
		} catch (JSONException e) {
			return null;
		}
	}

	private Weather parseCurrentSimple() {
		try {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

			String currentWeatherResults = prefs.getString(MainUpdateService.CURRENT_RESULT, "null");
			JSONObject reader = new JSONObject(currentWeatherResults);
			
			long time = reader.getLong("dt");
			SharedPreferences.Editor editor = prefs.edit();
			editor.putLong("time", time);
			editor.apply();

			JSONArray weather = reader.getJSONArray("weather");
			String weatherMain = weather.getJSONObject(0).getString("main");
			String weatherDescription = weather.getJSONObject(0).getString("description");

			JSONObject main = reader.getJSONObject("main");
			int temperature = main.getInt("temp");

			Weather currentWeather = new Weather(weatherMain, weatherDescription);
			currentWeather.setTemperature(temperature);
			
			return currentWeather;
		} catch (JSONException e) {
			return null;
		}
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
			return null;
		}

		return forecastWeather;
	}
	
	private void updateUI() {
		updateCityUI(parseCity());
		updateCurrentUI(parseCurrentSimple());
		updateForecastUI(parseForecastSimple());
	}

	private void updateCityUI(City city) {
		TextView textView = (TextView) findViewById(R.id.city_name);
		if (city.getName() != null) {
			textView.setText(city.getName());
		}
	}

	private void updateCurrentUI(Weather currentWeather) {
		TextView textView = (TextView) findViewById(R.id.current_temperature);
		String temperatureString = String.valueOf(currentWeather.getTemperature());
		textView.setText(temperatureString);

		textView = (TextView) findViewById(R.id.current_main);
		String weatherMain = currentWeather.getWeatherMain();
		if (weatherMain != null) {
			textView.setText(weatherMain);
		}
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		long time = prefs.getLong("time", 0);
		if (time != 0) {
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(time * 1000);
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd", Locale.US);
			String date = dateFormat.format(c.getTime());
			
			String day = getDayOfWeek(time);
			if (day != null) {
				textView = (TextView) findViewById(R.id.current_date);
				textView.setText(date + " " + day);
			}
		}
	}

	private void updateForecastUI(LongSparseArray<Weather> forecastWeather) {
		for (int i = 0; i < forecastWeather.size(); i++) {
			long date = forecastWeather.keyAt(i);
			Weather forecast = forecastWeather.get(date);

			if (forecast != null) {
				TextView textView = (TextView) findViewById(
						getResources().getIdentifier("forecast_" + i, "id", getPackageName()));

				String mainString = forecast.getWeatherMain();
				String temperatureString = String.valueOf(forecast.getTemperatureMin()) + "~"
						+ String.valueOf(forecast.getTemperatureMax()) + "\u00B0" + "C";
				String dateString = getDayOfWeek(date);
				if (dateString != null) {
					textView.setText(mainString + "\n" + dateString + "\n" + temperatureString);
				}
				
				if (i == 0) {
					textView = (TextView) findViewById(R.id.current_day_temperature);
					textView.setText(temperatureString);
				}
			}
		}
	}

	private String getDayOfWeek(long time) {
		if (time == 0) {
			return null;
		}
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time * 1000);
		
		int i = c.get(Calendar.DAY_OF_WEEK);
		switch (i) {
		case 1:
			return "Sun";
		case 2:
			return "Mon";
		case 3:
			return "Tues";
		case 4:
			return "Wed";
		case 5:
			return "Thur";
		case 6:
			return "Fri";
		case 7:
			return "Sat";
		default:
			return null;
		}
	}

}