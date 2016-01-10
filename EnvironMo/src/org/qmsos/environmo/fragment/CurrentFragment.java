package org.qmsos.environmo.fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qmsos.environmo.MainProvider;
import org.qmsos.environmo.R;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CurrentFragment extends Fragment {
	
	private static final String TAG = CurrentFragment.class.getSimpleName();
	private static final String KEY_CITYID = "KEY_CITYID";
	
	private float CURRENT_SIZE;
	
	public static CurrentFragment newInstance(Context context, long cityId) {
		Bundle b = new Bundle();
		b.putLong(KEY_CITYID, cityId);

		CurrentFragment fragment = new CurrentFragment();
		fragment.setArguments(b);
		
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view =  inflater.inflate(R.layout.view_current, container, false);
		
		TextView v = (TextView) view.findViewById(R.id.current_temperature);
		CURRENT_SIZE = v.getTextSize();
		
		long cityId = getArguments().getLong(KEY_CITYID);
		
		Cursor cursor = null;
		try {
			String[] projection = { MainProvider.KEY_CURRENT, MainProvider.KEY_FORECAST };
			String where = MainProvider.KEY_CITY_ID + " = " + cityId;
			cursor = getContext().getContentResolver()
					.query(MainProvider.CONTENT_URI_WEATHER, projection, where, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				String current = cursor.getString(cursor.getColumnIndexOrThrow(MainProvider.KEY_CURRENT));
				String forecast = cursor.getString(cursor.getColumnIndexOrThrow(MainProvider.KEY_FORECAST));
				
				updateCurrent(view, current);
				updateForecast(view, forecast);
			}
		} catch (Exception e) {
			Log.e(TAG, "the column does not exist");
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
	
		return view;
	}

	public void showForecast(int day) {
		if (day < 0 || day > 3) {
			return;
		}
		
		float forecastSize = 72f;
		
		long cityId = getArguments().getLong(KEY_CITYID);
		String where = MainProvider.KEY_CITY_ID + " = " + cityId;

		if (day == 0) {
			String[] projection = { MainProvider.KEY_CURRENT, MainProvider.KEY_FORECAST };
			Cursor cursor = getContext().getContentResolver()
					.query(MainProvider.CONTENT_URI_WEATHER, projection, where, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				String current = cursor.getString(cursor.getColumnIndexOrThrow(MainProvider.KEY_CURRENT));
				String forecast = cursor.getString(cursor.getColumnIndexOrThrow(MainProvider.KEY_FORECAST));
				
				updateCurrent(getView(), current);
				updateForecast(getView(), forecast);
				
				TextView v = (TextView) getView().findViewById(R.id.current_temperature);
				v.setTextSize(TypedValue.COMPLEX_UNIT_PX, CURRENT_SIZE);
				
				v = (TextView) getView().findViewById(R.id.current_temperature_format);
				v.setText(R.string.temperature_format);
			}
			cursor.close();
		} else if (day >= 1 && day <= 3) {
			String[] projection = { MainProvider.KEY_FORECAST };
			Cursor cursor = getContext().getContentResolver()
					.query(MainProvider.CONTENT_URI_WEATHER, projection, where, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				String results = cursor.getString(cursor.getColumnIndex(MainProvider.KEY_FORECAST));
				
				try {
					JSONObject reader = new JSONObject(results);
					JSONArray list = reader.getJSONArray("list");
					JSONObject forecast = list.getJSONObject(day);
					
					long date = forecast.getLong("dt");
					
					JSONArray weather = forecast.getJSONArray("weather");
					String mainString = weather.getJSONObject(0).getString("main");
					
					JSONObject temp = forecast.getJSONObject("temp");
					int temperatureMin = temp.getInt("min");
					int temperatureMax = temp.getInt("max");
					
					String temperatureString = String.valueOf(temperatureMin) + "~"
							+ String.valueOf(temperatureMax) + "\u00B0";
					
					Calendar c = Calendar.getInstance();
					c.setTimeZone(TimeZone.getTimeZone("UTC"));
					c.setTimeInMillis(date * 1000);
					SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd", Locale.US);
					String dateString = dateFormat.format(c.getTime());
					String dayString = getDayOfWeek(date);
					
					TextView v = (TextView) getView().findViewById(R.id.current_temperature);
					v.setText(temperatureString);
					v.setTextSize(forecastSize);
					
					v = (TextView) getView().findViewById(R.id.current_main);
					v.setText(null);
					
					v = (TextView) getView().findViewById(R.id.current_temperature_format);
					v.setText(null);
					
					v = (TextView) getView().findViewById(R.id.current_day_temperature);
					v.setText(mainString);
					
					v = (TextView) getView().findViewById(R.id.current_date);
					v.setText(dateString + " " + dayString);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			cursor.close();
		}
	}

	private void updateCurrent(View view, String json) {
		if (json == null) {
			return;
		}
		
		try {
			JSONObject reader = new JSONObject(json);
			
			// Current
			JSONObject main = reader.getJSONObject("main");
			int temperature = main.getInt("temp");
			TextView textView = (TextView) view.findViewById(R.id.current_temperature);
			textView.setText(String.valueOf(temperature));
			
			JSONArray weather = reader.getJSONArray("weather");
			String weatherMain = weather.getJSONObject(0).getString("main");
			textView = (TextView) view.findViewById(R.id.current_main);
			textView.setText(weatherMain);
			
			// Today
			Calendar c = Calendar.getInstance();
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd", Locale.US);
			String date = dateFormat.format(c.getTime());
			
			String day = getDayOfWeek();
			if (day != null) {
				textView = (TextView) view.findViewById(R.id.current_date);
				textView.setText(date + " " + day);
			}
		} catch (JSONException e) {
			return;
		}
	}

	private void updateForecast(View view, String json) {
		if (json == null) {
			return;
		}
		
		try {
			JSONObject reader = new JSONObject(json);
	
			JSONArray list = reader.getJSONArray("list");
			JSONObject todayForecast = list.getJSONObject(0);
			JSONObject temp = todayForecast.getJSONObject("temp");
			int temperatureMin = temp.getInt("min");
			int temperatureMax = temp.getInt("max");
			String temperatureString = String.valueOf(temperatureMin) + "~"
					+ String.valueOf(temperatureMax) + "\u00B0" + "C";
			TextView textView = (TextView) view.findViewById(R.id.current_day_temperature);
			textView.setText(temperatureString);
		} catch (JSONException e) {
			return;
		}
	}

	private String getDayOfWeek(long time) {
		if (time == 0) {
			return null;
		}
		Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("UTC"));
		c.setTimeInMillis(time * 1000);
		
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

	private String getDayOfWeek() {
		Calendar c = Calendar.getInstance();
		c.setTimeZone(TimeZone.getTimeZone("UTC"));
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
