package org.qmsos.weathermo.fragment;

import java.util.regex.PatternSyntaxException;

import org.qmsos.weathermo.MainActivity;
import org.qmsos.weathermo.R;
import org.qmsos.weathermo.WeatherProvider;
import org.qmsos.weathermo.util.WeatherParser;

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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_forecast, container, false);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		TextView textView = (TextView) getView().findViewById(R.id.current);
		textView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					((OnWeatherClickListener) getContext()).onCurrentClick();
				} catch (ClassCastException e) {
					throw new ClassCastException("context must implement OnWeatherClickListener");
				}
			}
		});

		for (int i = 0; i < MainActivity.DAY_COUNT; i++) {
			final int j = i;
			textView = (TextView) getView().findViewById(
					getResources().getIdentifier("forecast_" + j, "id", getContext().getPackageName()));
			textView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					try {
						((OnWeatherClickListener) getContext()).onForecastClick(j);
					} catch (ClassCastException e) {
						throw new ClassCastException("context must implement OnWeatherClickListener");
					}
				}
			});
		}
	}

	public void refresh(long cityId) {
		Cursor cursor = null;
		try {
			String[] projection = { 
					WeatherProvider.KEY_CITY_ID, WeatherProvider.KEY_CURRENT, WeatherProvider.KEY_FORECAST };
			String where = WeatherProvider.KEY_CITY_ID + " = " + cityId;

			cursor = getContext().getContentResolver().query(
					WeatherProvider.CONTENT_URI_WEATHER, projection, where, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				String current = cursor.getString(cursor.getColumnIndexOrThrow(WeatherProvider.KEY_CURRENT));
				String forecast = cursor.getString(cursor.getColumnIndexOrThrow(WeatherProvider.KEY_FORECAST));

				if (current != null) {
					try {
						String[] elements = current.split("\\|");
						if (elements.length == WeatherParser.COUNT_ELEMENTS_CURRENT) {
							int weatherId = Integer.parseInt(elements[0]);

							String s = "Now" + "\n" + elements[1] + "\u00B0" + "C";

							TextView textView = (TextView) getView().findViewById(R.id.current);
							textView.setText(s);

							WeatherParser.setIconOfForecastView(textView, weatherId);
						}
					} catch (PatternSyntaxException e) {
						Log.e(TAG, "the syntax of the supplied regular expression is not valid");
					} catch (NumberFormatException e) {
						Log.e(TAG, "string cannot be parsed as an integer value");
					}
				}
				if (forecast != null) {
					try {
						String[] elements = forecast.split(";");
						if (elements.length == MainActivity.DAY_COUNT) {
							for (int i = 0; i < elements.length; i++) {
								String element = elements[i];
								String[] values = element.split("\\|");
								if (values.length == WeatherParser.COUNT_ELEMENTS_FORECAST) {
									int weatherId = Integer.parseInt(values[0]);

									String s = (i + 1) * 12 + "h\n" 
											+ values[1] + "~" + values[2] + "\u00B0" + "C";

									TextView v = (TextView) getView().findViewById(
											getResources().getIdentifier(
													"forecast_" + i, "id", getContext().getPackageName()));
									
									v.setText(s);

									WeatherParser.setIconOfForecastView(v, weatherId);
								}
							}
						}
					} catch (PatternSyntaxException e) {
						Log.e(TAG, "the syntax of the supplied regular expression is not valid");
					} catch (NumberFormatException e) {
						Log.e(TAG, "string cannot be parsed as an integer value");
					}
				}
			}
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "The column does not exist");
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
	}

	public interface OnWeatherClickListener {
		
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
