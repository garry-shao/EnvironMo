package org.qmsos.weathermo.fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.PatternSyntaxException;

import org.qmsos.weathermo.MainActivity;
import org.qmsos.weathermo.R;
import org.qmsos.weathermo.WeatherProvider;
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
		
		Cursor cursor = null;
		try {
			String[] projection = { WeatherProvider.KEY_CURRENT, WeatherProvider.KEY_FORECAST };
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
							int temperature = Integer.parseInt(elements[1]);
							
							TextView v = (TextView) getView().findViewById(R.id.current_temperature);
							v.setText(String.valueOf(temperature) + "\u00B0");
							
							v = (TextView) getView().findViewById(R.id.current_main);
							v.setText(WeatherParser.getCategoryFromWeatherId(weatherId));
							
							Calendar c = Calendar.getInstance();
							SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd", Locale.US);
							String date = dateFormat.format(c.getTime());
							
							String day = WeatherParser.getCurrentDayOfWeek();
							if (day != null) {
								v = (TextView) getView().findViewById(R.id.current_date);
								v.setText(date + " " + day);
							}
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
							String element = elements[0];
							String[] values = element.split("\\|");
							if (values.length == WeatherParser.COUNT_ELEMENTS_FORECAST) {
								String s = values[1] + "~" + values[2] + "\u00B0" + "C";
								
								TextView textView = (TextView) getView().findViewById(
										R.id.current_day_temperature);
								
								textView.setText(s);
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
			Log.e(TAG, "the column does not exist");
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
	}
	
	public void showForecast(int day) {
		if (0 <= day && day < MainActivity.DAY_COUNT) {
			
			long cityId = getArguments().getLong(KEY_CITYID);
			
			Cursor cursor = null;
			try {
				String where = WeatherProvider.KEY_CITY_ID + " = " + cityId;
				String[] projection = { WeatherProvider.KEY_FORECAST };
				
				cursor = getContext().getContentResolver().query(
						WeatherProvider.CONTENT_URI_WEATHER, projection, where, null, null);
				if (cursor != null && cursor.moveToFirst()) {
					String forecast = cursor.getString(
							cursor.getColumnIndexOrThrow(WeatherProvider.KEY_FORECAST));
					if (forecast != null) {
						try {
							String[] elements = forecast.split(";");
							if (elements.length == MainActivity.DAY_COUNT) {
								String element = elements[day];
								String[] values = element.split("\\|");
								if (values.length == WeatherParser.COUNT_ELEMENTS_FORECAST) {
									int weatherId = Integer.parseInt(values[0]);
									
									String s = values[1] + "~" + values[2] + "\u00B0";
									
									SpannableString spanned = new SpannableString(s);
									spanned.setSpan(new RelativeSizeSpan(0.6f), 
											0,
											s.length(), 
											Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
									
									TextView v = (TextView) getView().findViewById(
											R.id.current_temperature);
									v.setText(spanned);
									
									v = (TextView) getView().findViewById(R.id.current_main);
									v.setText(null);
									
									v = (TextView) getView().findViewById(R.id.current_day_temperature);
									v.setText(WeatherParser.getDescriptionFromWeatherId(weatherId));
									
									v = (TextView) getView().findViewById(R.id.current_date);
									v.setText("+" + (day + 1) * 24 + ":00h");
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
				Log.e(TAG, "the column does not exist");
			} finally {
				if (cursor != null && !cursor.isClosed()) {
					cursor.close();
				}
			}
		}
	}
	
}
