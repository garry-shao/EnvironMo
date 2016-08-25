package org.qmsos.weathermo.fragment;

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

import org.qmsos.weathermo.R;
import org.qmsos.weathermo.contract.ProviderContract.WeatherEntity;
import org.qmsos.weathermo.res.CalendarFactory;
import org.qmsos.weathermo.res.TextFactory;
import org.qmsos.weathermo.util.WeatherParser;

/**
 * Show details of weather. 
 */
public class WeatherDetails extends Fragment implements LoaderCallbacks<Cursor> {
	
	private static final String KEY_CITY_ID = "KEY_CITY_ID";
	
	private int mDayOnDisplay = 0;
	
	/**
	 * Create an instance of fragment that shows details of weather.
	 * 
	 * @param context
	 *            The containing context.
	 * @param cityId
	 *            The id of the city currently showing.
	 * @return The created fragment instance.
	 */
	public static WeatherDetails newInstance(Context context, long cityId) {
		Bundle b = new Bundle();
		b.putLong(KEY_CITY_ID, cityId);

		WeatherDetails fragment = new WeatherDetails();
		fragment.setArguments(b);
		
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_weather_details, container, false);
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
			// Indicates current.
			projection = new String[] { WeatherEntity.CURRENT, WeatherEntity.UV_INDEX };
			break;
		case 1:
			// Indicates forecast 1.
			projection = new String[] { WeatherEntity.FORECAST1 };
			break;
		case 2:
			// Indicates forecast 2.
			projection = new String[] { WeatherEntity.FORECAST2 };
			break;
		case 3:
			// Indicates forecast 3.
			projection = new String[] { WeatherEntity.FORECAST3 };
			break;
		}
		
		long cityId = getArguments().getLong(KEY_CITY_ID);
		String where = WeatherEntity.CITY_ID + " = " + cityId;
		
		return new CursorLoader(getContext(),
                WeatherEntity.CONTENT_URI, projection, where, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if (data != null && data.moveToFirst()) {
			// Hack here!
			// Tell whether showing current or forecast by the columns containing.
			String[] columnNames = data.getColumnNames();
			switch (columnNames.length) {
			case 1:
				// Indicates forecast.
				String forecastRaw = data.getString(data.getColumnIndexOrThrow(columnNames[0]));
				
				int forecastWeatherId = WeatherParser.getWeatherId(forecastRaw);
				int temperatureMin = WeatherParser.getTemperatureMin(forecastRaw);
				int temperatureMax = WeatherParser.getTemperatureMax(forecastRaw);
				
				TextView fv = (TextView) getView().findViewById(R.id.details_temperature);
				if (temperatureMin != WeatherParser.INVALID_TEMPERATURE
						|| temperatureMax != WeatherParser.INVALID_TEMPERATURE) {
					
					String raw = temperatureMin + "~" + temperatureMax + "\u00B0";
					SpannableString spanned = new SpannableString(raw);
					spanned.setSpan(new RelativeSizeSpan(0.6f), 0, raw.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
					
					fv.setText(spanned);
				} else {
					fv.setText(null);
				}
				
				fv = (TextView) getView().findViewById(R.id.details_uv_index);
				fv.setText(null);
				
				fv = (TextView) getView().findViewById(R.id.details_description);
				fv.setText(TextFactory.getWeatherDescription(forecastWeatherId));
				break;
			case 2:
				// Indicates current.
				String currentRaw = data.getString(
                        data.getColumnIndexOrThrow(WeatherEntity.CURRENT));
				
				double uvIndex;
				int columnIndexUv = data.getColumnIndexOrThrow(WeatherEntity.UV_INDEX);
				if (!data.isNull(columnIndexUv)) {
					uvIndex = data.getDouble(columnIndexUv);
				} else {
					uvIndex = WeatherParser.INVALID_UV_INDEX;
				}
				
				int currentWeatherId = WeatherParser.getWeatherId(currentRaw);
				int temperature = WeatherParser.getTemperature(currentRaw);
				
				TextView cv = (TextView) getView().findViewById(R.id.details_temperature);
				if (temperature != WeatherParser.INVALID_TEMPERATURE) {
					String temperatureInfo = String.valueOf(temperature) + "\u00B0";
					cv.setText(temperatureInfo);
				} else {
					cv.setText(null);
				}
				
				cv = (TextView) getView().findViewById(R.id.details_uv_index);
				if (Double.compare(uvIndex, WeatherParser.INVALID_UV_INDEX) != 0) {
					String UvIndexInfo = "UV: " + uvIndex + " - " +
							getString(TextFactory.getUvIndexDescription(uvIndex));

                    cv.setText(UvIndexInfo);
				} else {
					cv.setText(null);
				}

				cv = (TextView) getView().findViewById(R.id.details_description);
				cv.setText(TextFactory.getWeatherDescription(currentWeatherId));
			}
		}
		
		String dateText;
		switch (mDayOnDisplay) {
		case 0:
			String date = CalendarFactory.getOffsetDate(mDayOnDisplay);
			String dayOfWeek = CalendarFactory.getDayOfWeek(getContext(), mDayOnDisplay);
			dateText = date + " " + dayOfWeek;
			break;
		case 1:
		case 2:
		case 3:
			dateText = "+" + 24 * mDayOnDisplay + ":00";
			break;
		default:
			dateText = null;
		}
		TextView v = (TextView) getView().findViewById(R.id.details_timestamp);
		v.setText(dateText);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

	/**
	 * Get the day currently showing.
	 * 
	 * @return The day currently showing, (0 means current, 1 means next 24 hours, etc).
	 */
	public int getCurrentShowingDay() {
		return mDayOnDisplay;
	}

	/**
	 * Reload view to show specified day of details.
	 * 
	 * @param newDay
	 *            The day to be shown.
	 */
	public void showDetails(int newDay) {
		mDayOnDisplay = newDay;
		
		getLoaderManager().restartLoader(0, null, this);
	}

}
