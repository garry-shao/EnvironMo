package org.qmsos.weathermo.fragment;

import org.qmsos.weathermo.R;
import org.qmsos.weathermo.contract.LoaderContract;
import org.qmsos.weathermo.contract.ProviderContract.WeatherEntity;
import org.qmsos.weathermo.resources.WeatherIconFactory;
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

public class WeatherForecast extends Fragment implements LoaderCallbacks<Cursor> {

	private OnForecastViewClickedListener mListener;
	
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		
		try {
			mListener = (OnForecastViewClickedListener) context;
		} catch (ClassCastException e) {
			String listenerName = OnForecastViewClickedListener.class.getSimpleName();
			
			throw new ClassCastException(context.toString() + " must implements " + listenerName);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_weather_forecast, container, false);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		TextView textView = (TextView) getView().findViewById(R.id.current);
		textView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mListener.onForecastViewClicked(0);
			}
		});

		for (int i = 1; i <= WeatherParser.FORECAST_IN_DAYS; i++) {
			final int j = i;
			textView = (TextView) getView().findViewById(
					getResources().getIdentifier("forecast_" + j, "id", getContext().getPackageName()));
			textView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mListener.onForecastViewClicked(j);
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
		
		TextView textView = (TextView) getView().findViewById(R.id.current);
		if (temperature != WeatherParser.INVALID_TEMPERATURE) {
			String uiCurrent = getContext().getString(R.string.ui_current);
			textView.setText(uiCurrent + "\n" + temperature + "\u00B0" + "C");
		} else {
			textView.setText(null);
		}
		WeatherIconFactory.setWeatherIcon(textView, currentWeatherId);
		
		for (int i = 1; i <= WeatherParser.FORECAST_IN_DAYS; i++) {
			int forecastWeatherId = WeatherParser.getWeatherId(forecasts[i - 1]);
			int temperatureMin = WeatherParser.getTemperatureMin(forecasts[i - 1]);
			int temperatureMax = WeatherParser.getTemperatureMax(forecasts[i - 1]);
			
			TextView v = (TextView) getView().findViewById(
					getResources().getIdentifier("forecast_" + i, "id", getContext().getPackageName()));
			if (temperatureMin != WeatherParser.INVALID_TEMPERATURE
					|| temperatureMax != WeatherParser.INVALID_TEMPERATURE) {
				
				String forecastTime = "+" + 24 * i + ":00H";
				String forecastTemperature = temperatureMin + "~" + temperatureMax + "\u00B0" + "C";
				v.setText(forecastTime + "\n" + forecastTemperature);
			} else {
				v.setText(null);
			}
			WeatherIconFactory.setWeatherIcon(v, forecastWeatherId);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

	/**
	 * Callback that will be invoked when user clicked views of forecast.
	 */
	public interface OnForecastViewClickedListener {

		/**
		 * Called when views of forecast weather are clicked.
		 * 
		 * @param day
		 *            which day is clicked(0 means current, 1 means next 24h,
		 *            etc...).
		 */
		void onForecastViewClicked(int day);
	}

}
