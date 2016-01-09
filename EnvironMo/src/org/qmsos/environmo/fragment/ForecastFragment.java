package org.qmsos.environmo.fragment;

import java.util.Calendar;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qmsos.environmo.CityProvider;
import org.qmsos.environmo.MainActivity;
import org.qmsos.environmo.R;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

public class ForecastFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.view_forecast, container, false);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		for (int i = 0; i <= 3; i++) {
			final int j = i;
			TextView textView = (TextView) getView().findViewById(
					getResources().getIdentifier("forecast_" + j, "id", getContext().getPackageName()));
			textView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					try {
						((OnWeatherClickListener) getContext()).onWeatherClick(j);
					} catch (ClassCastException e) {
						throw new ClassCastException("context must implement OnWeatherClickListener");
					}
				}
			});
		}
	}

	public void refresh(long cityId) {
		String[] projection = { CityProvider.KEY_CITYID, CityProvider.KEY_FORECAST };
		String where = CityProvider.KEY_CITYID + " = " + cityId;
		Cursor cursor = getContext().getContentResolver()
				.query(CityProvider.CONTENT_URI, projection, where, null, null);
		if (cursor != null && cursor.moveToFirst()) {
			String forecast = cursor.getString(cursor.getColumnIndex(CityProvider.KEY_FORECAST));
			updateForecast(getView(), forecast);
		}
		cursor.close();
	}
	
	private void updateForecast(View view, String json) {
		try {
			JSONObject reader = new JSONObject(json);
	
			JSONArray list = reader.getJSONArray("list");
			for (int i = 0; i < list.length(); i++) {
				JSONObject forecast = list.getJSONObject(i);
				
				long date = forecast.getLong("dt");
				
				JSONArray weather = forecast.getJSONArray("weather");
				int weatherId = weather.getJSONObject(0).getInt("id");
				
				JSONObject temp = forecast.getJSONObject("temp");
				int temperatureMin = temp.getInt("min");
				int temperatureMax = temp.getInt("max");
				
				String temperatureString = String.valueOf(temperatureMin) + "~"
						+ String.valueOf(temperatureMax) + "\u00B0" + "C";
				String dateString = getDayOfWeek(date);
				if (dateString != null) {
					TextView textView = (TextView) view.findViewById(getResources().getIdentifier(
							"forecast_" + i, "id", getContext().getPackageName()));
					textView.setText(dateString + "\n" + temperatureString);
					
					setForecastIcon(textView, weatherId);
				}
			}
		} catch (JSONException e) {
			Log.e(MainActivity.class.getSimpleName(), "JSON parsing error!");
			
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

	private void setForecastIcon(TextView v, int id) {
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
		}
	}

	public interface OnWeatherClickListener {
		void onWeatherClick(int day);
	}
}
