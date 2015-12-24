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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class WeatherFragment extends Fragment {
	
	public static final String KEY_CITYID = "KEY_CITYID";
	
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
		
		long cityId = getArguments().getLong(KEY_CITYID);
		TextView cityIdView = (TextView) view.findViewById(R.id.city_id);
		cityIdView.setText(String.valueOf(cityId));
		
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
