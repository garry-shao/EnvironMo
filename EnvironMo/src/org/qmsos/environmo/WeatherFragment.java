package org.qmsos.environmo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class WeatherFragment extends Fragment {
	
	private static final String KEY_CITYID = "KEY_CITYID";
	
	private float CURRENT_SIZE;
	
	public static WeatherFragment newInstance(Context context, long cityId) {
		Bundle b = new Bundle();
		b.putLong(KEY_CITYID, cityId);

		WeatherFragment fragment = new WeatherFragment();
		fragment.setArguments(b);
		
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view =  inflater.inflate(R.layout.view_current, container, false);
		
		TextView v = (TextView) view.findViewById(R.id.current_temperature);
		CURRENT_SIZE = v.getTextSize();
		
		long cityId = getArguments().getLong(KEY_CITYID);
		
		String[] projection = { CityProvider.KEY_CURRENT };
		String where = CityProvider.KEY_CITYID + " = " + cityId;
		Cursor cursor = getContext().getContentResolver()
				.query(CityProvider.CONTENT_URI, projection, where, null, null);
		if (cursor != null && cursor.moveToFirst()) {
			String current = cursor.getString(cursor.getColumnIndex(CityProvider.KEY_CURRENT));
			
			updateCurrent(view, current);
		}
		cursor.close();
		
		String[] projection2 = { CityProvider.KEY_FORECAST };
		Cursor cursor2 = getContext().getContentResolver()
				.query(CityProvider.CONTENT_URI, projection2, where, null, null);
		if (cursor2 != null && cursor2.moveToFirst()) {
			String forecast = cursor2.getString(cursor2.getColumnIndex(CityProvider.KEY_FORECAST));
			
			updateForecast(view, forecast);
		}
		cursor2.close();
		
		return view;
	}

	public void showForecast(int day) {
		float forecastSize = 72f;
		
		
		long cityId = getArguments().getLong(KEY_CITYID);
		String where = CityProvider.KEY_CITYID + " = " + cityId;

		if (day == 0) {
			String[] projection = { CityProvider.KEY_CURRENT };
			Cursor cursor = getContext().getContentResolver()
					.query(CityProvider.CONTENT_URI, projection, where, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				String current = cursor.getString(cursor.getColumnIndex(CityProvider.KEY_CURRENT));
				
				updateCurrent(getView(), current);
				
				TextView v = (TextView) getView().findViewById(R.id.current_temperature);
				v.setTextSize(TypedValue.COMPLEX_UNIT_PX, CURRENT_SIZE);
				
				v = (TextView) getView().findViewById(R.id.current_temperature_format);
				v.setText(R.string.temperature_format);
			}
			cursor.close();
			
			String[] projection2 = { CityProvider.KEY_FORECAST };
			Cursor cursor2 = getContext().getContentResolver()
					.query(CityProvider.CONTENT_URI, projection2, where, null, null);
			if (cursor2 != null && cursor2.moveToFirst()) {
				String forecast = cursor2.getString(cursor2.getColumnIndex(CityProvider.KEY_FORECAST));
				
				updateForecast(getView(), forecast);
			}
			cursor2.close();
		} else if (day >= 1 && day <= 3) {
			String[] projection = { CityProvider.KEY_FORECAST };
			Cursor cursor = getContext().getContentResolver()
					.query(CityProvider.CONTENT_URI, projection, where, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				String results = cursor.getString(cursor.getColumnIndex(CityProvider.KEY_FORECAST));
				
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
