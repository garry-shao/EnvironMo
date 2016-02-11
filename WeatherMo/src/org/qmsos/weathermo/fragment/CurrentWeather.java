package org.qmsos.weathermo.fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.PatternSyntaxException;

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
	
	private static final String KEY_CITYID = "KEY_CITYID";
	
	public static CurrentWeather newInstance(Context context, long cityId) {
		Bundle b = new Bundle();
		b.putLong(KEY_CITYID, cityId);

		CurrentWeather fragment = new CurrentWeather();
		fragment.setArguments(b);
		
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view =  inflater.inflate(R.layout.fragment_current, container, false);
	
		return view;
	}


	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		showCurrent();
	}

	public void showCurrent() {
		long cityId = getArguments().getLong(KEY_CITYID);
		
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
		
		if (current != null) {
			try {
				String[] elements = current.split("\\|");
				if (elements.length != WeatherParser.COUNT_ELEMENTS_CURRENT) {
					return;
				}
				
				int weatherId = Integer.parseInt(elements[0]);
				int temperature = Integer.parseInt(elements[1]);
				
				TextView v = (TextView) getView().findViewById(R.id.current_temperature);
				v.setText(String.valueOf(temperature) + "\u00B0");
				
				v = (TextView) getView().findViewById(R.id.current_main);
				v.setText(WeatherInfoAdapter.getCategoryFromWeatherId(weatherId));
				
				v = (TextView) getView().findViewById(R.id.current_uv_index);
				v.setText("TODO: UV Index");
				
				Calendar c = Calendar.getInstance();
				SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd", Locale.US);
				String date = dateFormat.format(c.getTime());
				
				v = (TextView) getView().findViewById(R.id.current_date);
				v.setText(date + " " + WeatherInfoAdapter.getDayOfWeek(0));
			} catch (PatternSyntaxException e) {
				Log.e(TAG, "the syntax of the supplied regular expression is not valid");
			} catch (NumberFormatException e) {
				Log.e(TAG, "string cannot be parsed as an integer value");
			}
		}
	}
	
	public void showForecast(int day) {
		if (day < 0 || day >= WeatherParser.COUNT_FORECAST_DAY) {
			return;
		}
			
		long cityId = getArguments().getLong(KEY_CITYID);
		
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
		
		if (forecast != null) {
			try {
				String[] elements = forecast.split(";");
				if (elements.length != WeatherParser.COUNT_FORECAST_DAY) {
					return;
				}
				
				String element = elements[day];
				String[] values = element.split("\\|");
				if (values.length != WeatherParser.COUNT_ELEMENTS_FORECAST) {
					return;
				}
				
				int weatherId = Integer.parseInt(values[0]);
				int temperatureMin = Integer.parseInt(values[1]);
				int temperatureMax = Integer.parseInt(values[2]);
				
				String raw = temperatureMin + "~" + temperatureMax + "\u00B0";
				
				SpannableString spanned = new SpannableString(raw);
				spanned.setSpan(
						new RelativeSizeSpan(0.6f), 0, raw.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				
				TextView v = (TextView) getView().findViewById(R.id.current_temperature);
				v.setText(spanned);
				
				v = (TextView) getView().findViewById(R.id.current_main);
				v.setText(null);
				
				v = (TextView) getView().findViewById(R.id.current_uv_index);
				v.setText(WeatherInfoAdapter.getDescriptionFromWeatherId(weatherId));
			
				Calendar c = Calendar.getInstance();
				c.add(Calendar.DAY_OF_YEAR, day + 1);
				SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd", Locale.US);
				String date = dateFormat.format(c.getTime());
				
				v = (TextView) getView().findViewById(R.id.current_date);
				v.setText(date + " " + WeatherInfoAdapter.getDayOfWeek(day + 1));
			} catch (PatternSyntaxException e) {
				Log.e(TAG, "the syntax of the supplied regular expression is not valid");
			} catch (NumberFormatException e) {
				Log.e(TAG, "string cannot be parsed as an integer value");
			}
		}
	}
	
}
