package org.qmsos.weathermo.fragment;

import org.qmsos.weathermo.R;
import org.qmsos.weathermo.WeatherProvider;
import org.qmsos.weathermo.util.WeatherInfoAdapter;
import org.qmsos.weathermo.util.WeatherParser;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class ForecastWeather extends Fragment {

	private static final String TAG = ForecastWeather.class.getSimpleName();

	private static final int COUNT_FORECAST_VIEWS = 3;
	
	private OnWeatherClickedListener mListener;
	
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		
		try {
			mListener = (OnWeatherClickedListener) context;
		} catch (ClassCastException e) {
			String listenerName = OnWeatherClickedListener.class.getSimpleName();
			
			throw new ClassCastException(context.toString() + " must implements " + listenerName);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_forecast_weather, container, false);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		TextView textView = (TextView) getView().findViewById(R.id.current);
		textView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mListener.onCurrentWeatherClicked();
			}
		});

		for (int i = 0; i < COUNT_FORECAST_VIEWS; i++) {
			final int j = i;
			textView = (TextView) getView().findViewById(
					getResources().getIdentifier("forecast_" + j, "id", getContext().getPackageName()));
			textView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mListener.onForecastWeatherClick(j);
				}
			});
		}
	}

	public void showWeather(long cityId) {
		String current = null;
		String forecast = null;
		Cursor cursor = null;
		try {
			String[] projection = { WeatherProvider.KEY_CURRENT, WeatherProvider.KEY_FORECAST };
			String where = WeatherProvider.KEY_CITY_ID + " = " + cityId;

			cursor = getContext().getContentResolver().query(
					WeatherProvider.CONTENT_URI_WEATHER, projection, where, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				current = cursor.getString(cursor.getColumnIndexOrThrow(WeatherProvider.KEY_CURRENT));
				forecast = cursor.getString(cursor.getColumnIndexOrThrow(WeatherProvider.KEY_FORECAST));
			}
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "The column does not exist");
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		
		int currentWeatherId = WeatherParser.getCurrentWeatherId(current);
		int currentTemperature = WeatherParser.getCurrentTemperature(current);
		
		TextView textView = (TextView) getView().findViewById(R.id.current);
		if (currentTemperature != WeatherParser.TEMPERATURE_INVALID) {
			textView.setText("Now" + "\n" + currentTemperature + "\u00B0" + "C");
		} else {
			textView.setText(R.string.placeholder);
		}
		WeatherInfoAdapter.setIconOfForecastView(textView, currentWeatherId);
		
		for (int i = 0; i < COUNT_FORECAST_VIEWS; i++) {
			int forecastWeatherId = WeatherParser.getForecastWeatherId(i, forecast);
			int forecastTemperatureMin = WeatherParser.getForecastTemperatureMin(i, forecast);
			int forecastTemperatureMax = WeatherParser.getForecastTemperatureMax(i, forecast);
			
			TextView v = (TextView) getView().findViewById(
					getResources().getIdentifier("forecast_" + i, "id", getContext().getPackageName()));
			if (forecastTemperatureMin != WeatherParser.TEMPERATURE_INVALID
					|| forecastTemperatureMax != WeatherParser.TEMPERATURE_INVALID) {
				
				v.setText(WeatherInfoAdapter.getDayOfWeek(i + 1) + "\n" 
						+ forecastTemperatureMin + "~" + forecastTemperatureMax + "\u00B0" + "C");
			} else {
				v.setText(R.string.placeholder);
			}
			WeatherInfoAdapter.setIconOfForecastView(v, forecastWeatherId);
		}
	}

	public interface OnWeatherClickedListener {
		
		/**
		 * When view of current weather is clicked.
		 */
		void onCurrentWeatherClicked();

		/**
		 * When view of forecast weather is clicked.
		 * 
		 * @param day
		 *            which day is clicked(0 means next 24h, 1 means 48h,
		 *            etc...).
		 */
		void onForecastWeatherClick(int day);
	}

}
