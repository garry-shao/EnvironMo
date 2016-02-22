package org.qmsos.weathermo.fragment;

import java.util.Calendar;

import org.qmsos.weathermo.R;
import org.qmsos.weathermo.provider.WeatherContract.WeatherEntity;
import org.qmsos.weathermo.util.IntentConstants;
import org.qmsos.weathermo.util.WeatherParser;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class ForecastWeather extends Fragment implements LoaderCallbacks<Cursor> {

	private OnForecastClickedListener mListener;
	
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		
		try {
			mListener = (OnForecastClickedListener) context;
		} catch (ClassCastException e) {
			String listenerName = OnForecastClickedListener.class.getSimpleName();
			
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
				mListener.onForecastClicked(0);
			}
		});

		for (int i = 1; i <= WeatherParser.COUNT_FORECAST_DAYS; i++) {
			final int j = i;
			textView = (TextView) getView().findViewById(
					getResources().getIdentifier("forecast_" + j, "id", getContext().getPackageName()));
			textView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mListener.onForecastClicked(j);
				}
			});
		}
		
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onDestroyView() {
		getLoaderManager().destroyLoader(0);
		
		super.onDestroyView();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		long cityId;
		if (args != null) {
			cityId = args.getLong(IntentConstants.KEY_CITY_ID);
		} else {
			cityId = 0L;
		}
		
		String[] projection = { WeatherEntity.CURRENT, WeatherEntity.FORECAST1, 
				WeatherEntity.FORECAST2, WeatherEntity.FORECAST3 };
		String where = WeatherEntity.CITY_ID + " = " + cityId;
		
		return new CursorLoader(getContext(), WeatherEntity.CONTENT_URI, projection, where, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		String current = null;
		String[] forecasts = new String[WeatherParser.COUNT_FORECAST_DAYS];
		if (data != null && data.moveToFirst()) {
			current = data.getString(data.getColumnIndexOrThrow(WeatherEntity.CURRENT));
			
			forecasts[0] = data.getString(data.getColumnIndexOrThrow(WeatherEntity.FORECAST1));
			forecasts[1] = data.getString(data.getColumnIndexOrThrow(WeatherEntity.FORECAST2));
			forecasts[2] = data.getString(data.getColumnIndexOrThrow(WeatherEntity.FORECAST3));
		}
		
		int currentWeatherId = WeatherParser.getCurrentWeatherId(current);
		int currentTemperature = WeatherParser.getCurrentTemperature(current);
		
		TextView textView = (TextView) getView().findViewById(R.id.current);
		if (currentTemperature != WeatherParser.TEMPERATURE_INVALID) {
			textView.setText("Now" + "\n" + currentTemperature + "\u00B0" + "C");
		} else {
			textView.setText(R.string.placeholder);
		}
		IconFactory.setIconOfForecastView(textView, currentWeatherId);
		
		for (int i = 1; i <= WeatherParser.COUNT_FORECAST_DAYS; i++) {
			int forecastWeatherId = WeatherParser.getForecastWeatherId(forecasts[i - 1]);
			int forecastTemperatureMin = WeatherParser.getForecastTemperatureMin(forecasts[i - 1]);
			int forecastTemperatureMax = WeatherParser.getForecastTemperatureMax(forecasts[i - 1]);
			
			TextView v = (TextView) getView().findViewById(
					getResources().getIdentifier("forecast_" + i, "id", getContext().getPackageName()));
			if (forecastTemperatureMin != WeatherParser.TEMPERATURE_INVALID
					|| forecastTemperatureMax != WeatherParser.TEMPERATURE_INVALID) {
				
				v.setText(CalendarFactory.getDayOfWeek(i) + "\n" 
						+ forecastTemperatureMin + "~" + forecastTemperatureMax + "\u00B0" + "C");
			} else {
				v.setText(R.string.placeholder);
			}
			IconFactory.setIconOfForecastView(v, forecastWeatherId);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

	public interface OnForecastClickedListener {

		/**
		 * When views of forecast weather are clicked.
		 * 
		 * @param day
		 *            which day is clicked(0 means current, 1 means next 24h,
		 *            etc...).
		 */
		void onForecastClicked(int day);
	}

	private static class CalendarFactory {
		
		static String getDayOfWeek(int day) {
			Calendar c = Calendar.getInstance();
			c.add(Calendar.DAY_OF_YEAR, day);
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
	
	private static class IconFactory {
		
		static void setIconOfForecastView(TextView v, int id) {
			if (200 <= id && id <= 299) {
				v.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_11, 0, 0);
			} else if (300 <= id && id <= 399) {
				v.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_09, 0, 0);
			} else if (500 <= id && id <= 504) {
				v.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_10, 0, 0);
			} else if (511 == id) {
				v.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_13, 0, 0);
			} else if (520 <= id && id <= 599) {
				v.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_09, 0, 0);
			} else if (600 <= id && id <= 699) {
				v.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_13, 0, 0);
			} else if (700 <= id && id <= 799) {
				v.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_50, 0, 0);
			} else if (800 == id) {
				v.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_01, 0, 0);
			} else if (801 == id) {
				v.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_02, 0, 0);
			} else if (802 == id || 803 == id) {
				v.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_03, 0, 0);
			} else if (804 == id) {
				v.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_04, 0, 0);
			} else {
				v.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
			}
		}
		
	}

}
