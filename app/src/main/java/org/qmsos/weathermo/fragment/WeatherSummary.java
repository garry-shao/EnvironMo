package org.qmsos.weathermo.fragment;

import org.qmsos.weathermo.R;
import org.qmsos.weathermo.contract.LoaderContract;
import org.qmsos.weathermo.contract.ProviderContract.WeatherEntity;
import org.qmsos.weathermo.res.IconFactory;
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

/**
 * Show summary of weather.
 *
 */
public class WeatherSummary extends Fragment implements LoaderCallbacks<Cursor> {

	private OnSummaryClickedListener mListener;
	
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		
		try {
			mListener = (OnSummaryClickedListener) context;
		} catch (ClassCastException e) {
			String listenerName = OnSummaryClickedListener.class.getSimpleName();
			
			throw new ClassCastException(context.toString() + " must implements " + listenerName);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_weather_summary, container, false);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		TextView tv = (TextView) getView().findViewById(R.id.current);
		tv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mListener.onSummaryClicked(0);
			}
		});

		for (int i = 1; i <= WeatherParser.FORECAST_IN_DAYS; i++) {
			final int j = i;
			tv = (TextView) getView().findViewById(
					getResources().getIdentifier("forecast_" + j, "id", getContext().getPackageName()));
			tv.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mListener.onSummaryClicked(j);
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
			cityId = args.getLong(LoaderContract.KEY_CITY_ID);
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
		String[] forecasts = new String[WeatherParser.FORECAST_IN_DAYS];
		if (data != null && data.moveToFirst()) {
			current = data.getString(data.getColumnIndexOrThrow(WeatherEntity.CURRENT));
			
			forecasts[0] = data.getString(data.getColumnIndexOrThrow(WeatherEntity.FORECAST1));
			forecasts[1] = data.getString(data.getColumnIndexOrThrow(WeatherEntity.FORECAST2));
			forecasts[2] = data.getString(data.getColumnIndexOrThrow(WeatherEntity.FORECAST3));
		}
		
		int currentWeatherId = WeatherParser.getWeatherId(current);
		int temperature = WeatherParser.getTemperature(current);
		
		TextView tv = (TextView) getView().findViewById(R.id.current);
		if (temperature != WeatherParser.INVALID_TEMPERATURE) {
			String uiCurrent = getContext().getString(R.string.ui_current);
			tv.setText(uiCurrent + "\n" + temperature + "\u00B0" + "C");
		} else {
			tv.setText(null);
		}
		int currentResId = IconFactory.getWeatherIcon(currentWeatherId);
		tv.setCompoundDrawablesRelativeWithIntrinsicBounds(0, currentResId, 0, 0);
		
		for (int i = 1; i <= WeatherParser.FORECAST_IN_DAYS; i++) {
			int forecastWeatherId = WeatherParser.getWeatherId(forecasts[i - 1]);
			int temperatureMin = WeatherParser.getTemperatureMin(forecasts[i - 1]);
			int temperatureMax = WeatherParser.getTemperatureMax(forecasts[i - 1]);
			
			tv = (TextView) getView().findViewById(
					getResources().getIdentifier("forecast_" + i, "id", getContext().getPackageName()));
			if (temperatureMin != WeatherParser.INVALID_TEMPERATURE
					|| temperatureMax != WeatherParser.INVALID_TEMPERATURE) {
				
				String forecastTime = "+" + 24 * i + ":00H";
				String forecastTemperature = temperatureMin + "~" + temperatureMax + "\u00B0" + "C";
				tv.setText(forecastTime + "\n" + forecastTemperature);
			} else {
				tv.setText(null);
			}
			int forecastResId = IconFactory.getWeatherIcon(forecastWeatherId);
			tv.setCompoundDrawablesRelativeWithIntrinsicBounds(0, forecastResId, 0, 0);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

	/**
	 * Callback that will be invoked when user clicked views of weather.
	 */
	public interface OnSummaryClickedListener {

		/**
		 * Called when views of weather are clicked.
		 * 
		 * @param day
		 *            which day is clicked(0 means current, 1 means next 24h,
		 *            etc...).
		 */
		void onSummaryClicked(int day);
	}

}
