package org.qmsos.weathermo.fragment;

import java.util.regex.PatternSyntaxException;

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
				mListener.onCurrentClick();
			}
		});

		for (int i = 0; i < 3; i++) {
			final int j = i;
			textView = (TextView) getView().findViewById(
					getResources().getIdentifier("forecast_" + j, "id", getContext().getPackageName()));
			textView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mListener.onForecastClick(j);
				}
			});
		}
	}

	public void refresh(long cityId) {
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
		
		if (current != null) {
			try {
				String[] elements = current.split("\\|");
				if (elements.length != WeatherParser.COUNT_ELEMENTS_CURRENT) {
					return;
				}
				
				int weatherId = Integer.parseInt(elements[0]);
				int temperature = Integer.parseInt(elements[1]);
				
				TextView textView = (TextView) getView().findViewById(R.id.current);
				
				WeatherInfoAdapter.setIconOfForecastView(textView, weatherId);
				
				textView.setText("Now" + "\n" + temperature + "\u00B0" + "C");
				
			} catch (PatternSyntaxException e) {
				Log.e(TAG, "the syntax of the supplied regular expression is not valid");
			} catch (NumberFormatException e) {
				Log.e(TAG, "string cannot be parsed as an integer value");
			}
		}
		
		if (forecast != null) {
			try {
				String[] elements = forecast.split(";");
				if (elements.length != WeatherParser.COUNT_FORECAST_DAY) {
					return;
				}
				
				for (int i = 0; i < elements.length; i++) {
					String element = elements[i];
					
					String[] values = element.split("\\|");
					if (values.length != WeatherParser.COUNT_ELEMENTS_FORECAST) {
						return;
					}
					
					int weatherId = Integer.parseInt(values[0]);
					int temperatureMin = Integer.parseInt(values[1]);
					int temperatureMax = Integer.parseInt(values[2]);
					
					TextView v = (TextView) getView().findViewById(
							getResources().getIdentifier(
									"forecast_" + i, "id", getContext().getPackageName()));
					
					WeatherInfoAdapter.setIconOfForecastView(v, weatherId);
					
					v.setText(WeatherInfoAdapter.getDayOfWeek(i + 1) + "\n" 
							+ temperatureMin + "~" + temperatureMax + "\u00B0" + "C");
				}
			} catch (PatternSyntaxException e) {
				Log.e(TAG, "the syntax of the supplied regular expression is not valid");
			} catch (NumberFormatException e) {
				Log.e(TAG, "string cannot be parsed as an integer value");
			}
		}
	}

	public interface OnWeatherClickedListener {
		
		/**
		 * When view of current weather is clicked.
		 */
		void onCurrentClick();

		/**
		 * When view of forecast weather is clicked.
		 * 
		 * @param day
		 *            which day is clicked(0 means next 24h, 1 means 48h,
		 *            etc...).
		 */
		void onForecastClick(int day);
	}

}
