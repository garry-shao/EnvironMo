package org.qmsos.weathermo.fragment;

import org.qmsos.weathermo.R;
import org.qmsos.weathermo.contract.ProviderContract.WeatherEntity;
import org.qmsos.weathermo.resources.CalendarFactory;
import org.qmsos.weathermo.resources.WeatherInfoFactory;
import org.qmsos.weathermo.util.WeatherParser;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class WeatherCurrent extends Fragment implements LoaderCallbacks<Cursor> {
	
	private static final String KEY_CITY_ID = "KEY_CITY_ID";
	
	private int mDayOnDisplay = 0;
	
	public static WeatherCurrent newInstance(Context context, long cityId) {
		Bundle b = new Bundle();
		b.putLong(KEY_CITY_ID, cityId);

		WeatherCurrent fragment = new WeatherCurrent();
		fragment.setArguments(b);
		
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view =  inflater.inflate(R.layout.fragment_weather_current, container, false);
	
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onDestroyView() {
		getLoaderManager().destroyLoader(0);
		
		super.onDestroyView();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = null;
		switch (mDayOnDisplay) {
		case 0:
			projection = new String[] { WeatherEntity.CURRENT, WeatherEntity.UV_INDEX };
			break;
		case 1:
			projection = new String[] { WeatherEntity.FORECAST1 };
			break;
		case 2:
			projection = new String[] { WeatherEntity.FORECAST2 };
			break;
		case 3:
			projection = new String[] { WeatherEntity.FORECAST3 };
			break;
		}
		
		long cityId = getArguments().getLong(KEY_CITY_ID);
		String where = WeatherEntity.CITY_ID + " = " + cityId;
		
		return new CursorLoader(getContext(), WeatherEntity.CONTENT_URI, projection, where, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if (data != null && data.moveToFirst()) {
			String[] columnNames = data.getColumnNames();
			switch (columnNames.length) {
			case 1:
				String forecastRaw = data.getString(data.getColumnIndexOrThrow(columnNames[0]));
				
				int forecastWeatherId = WeatherParser.getWeatherId(forecastRaw);
				int temperatureMin = WeatherParser.getTemperatureMin(forecastRaw);
				int temperatureMax = WeatherParser.getTemperatureMax(forecastRaw);
				
				TextView fv = (TextView) getView().findViewById(R.id.current_temperature);
				if (temperatureMin != WeatherParser.INVALID_TEMPERATURE
						|| temperatureMax != WeatherParser.INVALID_TEMPERATURE) {
					
					String raw = temperatureMin + "~" + temperatureMax + "\u00B0";
					SpannableString spanned = new SpannableString(raw);
					spanned.setSpan(
							new RelativeSizeSpan(0.6f), 0, raw.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					
					fv.setText(spanned);
				} else {
					fv.setText(null);
				}
				
				fv = (TextView) getView().findViewById(R.id.current_main);
				fv.setText(WeatherInfoFactory.getWeatherCategory(getContext(), forecastWeatherId));
				
				fv = (TextView) getView().findViewById(R.id.current_uv_index);
				fv.setText(WeatherInfoFactory.getWeatherDescription(getContext(), forecastWeatherId));
				break;
			case 2:
				String currentRaw = data.getString(data.getColumnIndexOrThrow(WeatherEntity.CURRENT));
				
				double uvIndex;
				int columnIndexUv = data.getColumnIndexOrThrow(WeatherEntity.UV_INDEX);
				if (!data.isNull(columnIndexUv)) {
					uvIndex = data.getDouble(columnIndexUv);
				} else {
					uvIndex = WeatherParser.INVALID_UV_INDEX;
				}
				
				int currentWeatherId = WeatherParser.getWeatherId(currentRaw);
				int temperature = WeatherParser.getTemperature(currentRaw);
				
				TextView cv = (TextView) getView().findViewById(R.id.current_temperature);
				if (temperature != WeatherParser.INVALID_TEMPERATURE) {
					cv.setText(String.valueOf(temperature) + "\u00B0");
				} else {
					cv.setText(null);
				}
				
				cv = (TextView) getView().findViewById(R.id.current_main);
				cv.setText(WeatherInfoFactory.getWeatherCategory(getContext(), currentWeatherId));
				
				cv = (TextView) getView().findViewById(R.id.current_uv_index);
				if (Double.compare(uvIndex, WeatherParser.INVALID_UV_INDEX) != 0) {
					String uvDescription = WeatherInfoFactory.getUvIndexDescription(getContext(), uvIndex);
					cv.setText("UV: " + uvIndex + " - " + uvDescription);
				} else {
					cv.setText(null);
				}
			}
		}
		
		String dateText;
		switch (mDayOnDisplay) {
		case 0:
			String date = CalendarFactory.getDate(mDayOnDisplay);
			String dayOfWeek = CalendarFactory.getDayOfWeek(getContext(), mDayOnDisplay);
			dateText = date + " " + dayOfWeek;
			break;
		case 1:
		case 2:
		case 3:
			dateText = "+" + 24 * mDayOnDisplay + ":00H";
			break;
		default:
			dateText = null;
		}
		TextView v = (TextView) getView().findViewById(R.id.current_date);
		v.setText(dateText);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

	public int getDayOnDisplay() {
		return mDayOnDisplay;
	}

	public void reload(int newDayOnDisplay) {
		mDayOnDisplay = newDayOnDisplay;
		
		getLoaderManager().restartLoader(0, null, this);
	}

}
