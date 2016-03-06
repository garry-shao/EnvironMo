package org.qmsos.weathermo.fragment;

import org.qmsos.weathermo.R;
import org.qmsos.weathermo.provider.WeatherContract.WeatherEntity;
import org.qmsos.weathermo.resources.CalendarFactory;
import org.qmsos.weathermo.resources.WeatherInfoFactory;
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
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		showWeather(0);
	}

	// TODO: using loader will making the view flicker, guessing updating will be
	//      some gap between valid data flows back. so still use synchronous data
	//		query, making adjust later??
	public void showWeather(int day) {
		long cityId = getArguments().getLong(KEY_CITY_ID);
		
		double uvIndex = WeatherParser.UV_INDEX_INVALID;
		String current = null;
		String[] forecasts = null;
		Cursor cursor = null;
		try {
			String[] projection = { WeatherEntity.CURRENT, WeatherEntity.UV_INDEX, 
					WeatherEntity.FORECAST1, WeatherEntity.FORECAST2, WeatherEntity.FORECAST3 };
			String where = WeatherEntity.CITY_ID + " = " + cityId;
			
			cursor = getContext().getContentResolver().query(
					WeatherEntity.CONTENT_URI, projection, where, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				current = cursor.getString(cursor.getColumnIndexOrThrow(WeatherEntity.CURRENT));
				String forecast1 = cursor.getString(cursor.getColumnIndexOrThrow(WeatherEntity.FORECAST1));
				String forecast2 = cursor.getString(cursor.getColumnIndexOrThrow(WeatherEntity.FORECAST2));
				String forecast3 = cursor.getString(cursor.getColumnIndexOrThrow(WeatherEntity.FORECAST3));
				forecasts = new String[] { forecast1, forecast2, forecast3 };
				
				int columnIndexUv = cursor.getColumnIndexOrThrow(WeatherEntity.UV_INDEX);
				if (!cursor.isNull(columnIndexUv)) {
					uvIndex = cursor.getDouble(columnIndexUv);
				}
			}
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "the column does not exist");
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		
		if (day == 0) {
			int currentWeatherId = WeatherParser.getCurrentWeatherId(current);
			int currentTemperature = WeatherParser.getCurrentTemperature(current);
			
			TextView v = (TextView) getView().findViewById(R.id.current_temperature);
			if (currentTemperature != WeatherParser.TEMPERATURE_INVALID) {
				v.setText(String.valueOf(currentTemperature) + "\u00B0");
			} else {
				v.setText(R.string.placeholder);
			}
			
			v = (TextView) getView().findViewById(R.id.current_main);
			v.setText(WeatherInfoFactory.getWeatherCategory(getContext(), currentWeatherId));
			
			v = (TextView) getView().findViewById(R.id.current_uv_index);
			if (Double.compare(uvIndex, WeatherParser.UV_INDEX_INVALID) != 0) {
				String uvDescription = WeatherInfoFactory.getUvIndexDescription(getContext(), uvIndex);
				v.setText("UV: " + uvIndex + " - " + uvDescription);
			} else {
				v.setText(R.string.placeholder);
			}
		} else {
			int forecastWeatherId = WeatherParser.getForecastWeatherId(forecasts[day - 1]);
			int forecastTemperatureMin = WeatherParser.getForecastTemperatureMin(forecasts[day - 1]);
			int forecastTemperatureMax = WeatherParser.getForecastTemperatureMax(forecasts[day - 1]);
			
			TextView v = (TextView) getView().findViewById(R.id.current_temperature);
			if (forecastTemperatureMin != WeatherParser.TEMPERATURE_INVALID
					|| forecastTemperatureMax != WeatherParser.TEMPERATURE_INVALID) {
				
				String raw = forecastTemperatureMin + "~" + forecastTemperatureMax + "\u00B0";
				SpannableString spanned = new SpannableString(raw);
				spanned.setSpan(
						new RelativeSizeSpan(0.6f), 0, raw.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				
				v.setText(spanned);
			} else {
				v.setText(R.string.placeholder);
			}
			
			v = (TextView) getView().findViewById(R.id.current_main);
			v.setText(WeatherInfoFactory.getWeatherCategory(getContext(), forecastWeatherId));
			
			v = (TextView) getView().findViewById(R.id.current_uv_index);
			v.setText(WeatherInfoFactory.getWeatherDescription(getContext(), forecastWeatherId));
		}
		
		TextView v = (TextView) getView().findViewById(R.id.current_date);
		v.setText(CalendarFactory.getDate(day) + " " + CalendarFactory.getDayOfWeek(getContext(), day));
	}

}
