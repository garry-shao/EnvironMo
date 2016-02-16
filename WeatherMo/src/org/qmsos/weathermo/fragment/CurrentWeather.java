package org.qmsos.weathermo.fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.qmsos.weathermo.R;
import org.qmsos.weathermo.WeatherProvider;
import org.qmsos.weathermo.util.WeatherInfoAdapter;
import org.qmsos.weathermo.util.WeatherParser;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CurrentWeather extends Fragment {
	
	private static final String TAG = CurrentWeather.class.getSimpleName();
	
	private static final String KEY_CITY_ID = "KEY_CITY_ID";
	
	public static CurrentWeather newInstance(Context context, long cityId) {
		Bundle b = new Bundle();
		b.putLong(KEY_CITY_ID, cityId);

		CurrentWeather fragment = new CurrentWeather();
		fragment.setArguments(b);
		
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view =  inflater.inflate(R.layout.fragment_current_weather, container, false);
	
		return view;
	}


	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		showCurrentWeather();
	}

	public void showCurrentWeather() {
		long cityId = getArguments().getLong(KEY_CITY_ID);
		
		String current = null;
		Cursor cursor = null;
		try {
			String[] projection = { WeatherProvider.KEY_CURRENT };
			String where = WeatherProvider.KEY_CITY_ID + " = " + cityId;
			
			cursor = getContext().getContentResolver().query(
					WeatherProvider.CONTENT_URI_WEATHER, projection, where, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				current = cursor.getString(cursor.getColumnIndexOrThrow(WeatherProvider.KEY_CURRENT));
			}
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "the column does not exist");
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		
		int currentWeatherId = WeatherParser.getCurrentWeatherId(current);
		int currentTemperature = WeatherParser.getCurrentTemperature(current);
		
		TextView v = (TextView) getView().findViewById(R.id.current_temperature);
		if (currentTemperature != WeatherParser.TEMPERATURE_INVALID) {
			v.setText(String.valueOf(currentTemperature) + "\u00B0");
		} else {
			v.setText(R.string.placeholder);
		}
		
		v = (TextView) getView().findViewById(R.id.current_main);
		v.setText(WeatherInfoAdapter.getCategoryFromWeatherId(currentWeatherId));
		
		v = (TextView) getView().findViewById(R.id.current_uv_index);
		v.setText("TODO: UV Index");
		
		Calendar c = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd", Locale.US);
		String date = dateFormat.format(c.getTime());
		
		v = (TextView) getView().findViewById(R.id.current_date);
		v.setText(date + " " + WeatherInfoAdapter.getDayOfWeek(0));
	}
	
	public void showForecastWeather(int day) {
		long cityId = getArguments().getLong(KEY_CITY_ID);
		
		String forecast = null;	
		Cursor cursor = null;
		try {
			String[] projection = { WeatherProvider.KEY_FORECAST };
			String where = WeatherProvider.KEY_CITY_ID + " = " + cityId;
				
			cursor = getContext().getContentResolver().query(
					WeatherProvider.CONTENT_URI_WEATHER, projection, where, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				forecast = cursor.getString(cursor.getColumnIndexOrThrow(WeatherProvider.KEY_FORECAST));
			}
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "the column does not exist");
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		
		int forecastWeatherId = WeatherParser.getForecastWeatherId(day, forecast);
		int forecastTemperatureMin = WeatherParser.getForecastTemperatureMin(day, forecast);
		int forecastTemperatureMax = WeatherParser.getForecastTemperatureMax(day, forecast);
		
		TextView v = (TextView) getView().findViewById(R.id.current_temperature);
		if (forecastTemperatureMin != WeatherParser.TEMPERATURE_INVALID
				|| forecastTemperatureMax != WeatherParser.TEMPERATURE_INVALID) {
			
			String raw = forecastTemperatureMin + "~" + forecastTemperatureMax + "\u00B0";
			SpannableString spanned = new SpannableString(raw);
			spanned.setSpan(new RelativeSizeSpan(0.6f), 0, raw.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			
			v.setText(spanned);
		} else {
			v.setText(R.string.placeholder);
		}
		
		v = (TextView) getView().findViewById(R.id.current_main);
		v.setText(null);
		
		v = (TextView) getView().findViewById(R.id.current_uv_index);
		v.setText(WeatherInfoAdapter.getDescriptionFromWeatherId(forecastWeatherId));
		
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_YEAR, day + 1);
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd", Locale.US);
		String date = dateFormat.format(c.getTime());
		
		v = (TextView) getView().findViewById(R.id.current_date);
		v.setText(date + " " + WeatherInfoAdapter.getDayOfWeek(day + 1));
	}
	
}
