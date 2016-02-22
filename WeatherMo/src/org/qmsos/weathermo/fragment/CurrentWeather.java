package org.qmsos.weathermo.fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.qmsos.weathermo.R;
import org.qmsos.weathermo.provider.WeatherContract.WeatherEntity;
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
	
	private static final String KEY_CITY_ID = "KEY_CITY_ID";
	
	public static CurrentWeather newInstance(Context context, long cityId) {
		Bundle b = new Bundle();
		b.putLong(KEY_CITY_ID, cityId);

		CurrentWeather fragment = new CurrentWeather();
		fragment.setArguments(b);
		
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view =  inflater.inflate(R.layout.fragment_current_weather, container, false);
	
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		showWeather(0);
	}

	// TODO: using loader will making the view flicker, guessing updating will be
	//      some gap between valid data flows back. so still use synchronous data
	//		query, making adjust later??
	public void showWeather(int day) {
		long cityId = getArguments().getLong(KEY_CITY_ID);
		
		double uvIndex = WeatherParser.UV_INDEX_INVALID;
		String current = null;
		String[] forecasts = null;
		Cursor cursor = null;
		try {
			String[] projection = { WeatherEntity.CURRENT, WeatherEntity.UV_INDEX, 
					WeatherEntity.FORECAST1, WeatherEntity.FORECAST2, WeatherEntity.FORECAST3 };
			String where = WeatherEntity.CITY_ID + " = " + cityId;
			
			cursor = getContext().getContentResolver().query(
					WeatherEntity.CONTENT_URI, projection, where, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				current = cursor.getString(cursor.getColumnIndexOrThrow(WeatherEntity.CURRENT));
				String forecast1 = cursor.getString(cursor.getColumnIndexOrThrow(WeatherEntity.FORECAST1));
				String forecast2 = cursor.getString(cursor.getColumnIndexOrThrow(WeatherEntity.FORECAST2));
				String forecast3 = cursor.getString(cursor.getColumnIndexOrThrow(WeatherEntity.FORECAST3));
				forecasts = new String[] { forecast1, forecast2, forecast3 };
				
				int columnIndexUv = cursor.getColumnIndexOrThrow(WeatherEntity.UV_INDEX);
				if (!cursor.isNull(columnIndexUv)) {
					uvIndex = cursor.getDouble(columnIndexUv);
				}
			}
		} catch (IllegalArgumentException e) {
			Log.e(TAG, "the column does not exist");
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
		
		if (day == 0) {
			int currentWeatherId = WeatherParser.getCurrentWeatherId(current);
			int currentTemperature = WeatherParser.getCurrentTemperature(current);
			
			TextView v = (TextView) getView().findViewById(R.id.current_temperature);
			if (currentTemperature != WeatherParser.TEMPERATURE_INVALID) {
				v.setText(String.valueOf(currentTemperature) + "\u00B0");
			} else {
				v.setText(R.string.placeholder);
			}
			
			v = (TextView) getView().findViewById(R.id.current_main);
			v.setText(InfoFactory.getCategoryFromWeatherId(currentWeatherId));
			
			v = (TextView) getView().findViewById(R.id.current_uv_index);
			if (Double.compare(uvIndex, WeatherParser.UV_INDEX_INVALID) != 0) {
				v.setText("UV: " + uvIndex + " - " + InfoFactory.getCategoryFromUvIndex(uvIndex));
			} else {
				v.setText(R.string.placeholder);
			}
		} else {
			int forecastWeatherId = WeatherParser.getForecastWeatherId(forecasts[day - 1]);
			int forecastTemperatureMin = WeatherParser.getForecastTemperatureMin(forecasts[day - 1]);
			int forecastTemperatureMax = WeatherParser.getForecastTemperatureMax(forecasts[day - 1]);
			
			TextView v = (TextView) getView().findViewById(R.id.current_temperature);
			if (forecastTemperatureMin != WeatherParser.TEMPERATURE_INVALID
					|| forecastTemperatureMax != WeatherParser.TEMPERATURE_INVALID) {
				
				String raw = forecastTemperatureMin + "~" + forecastTemperatureMax + "\u00B0";
				SpannableString spanned = new SpannableString(raw);
				spanned.setSpan(
						new RelativeSizeSpan(0.6f), 0, raw.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				
				v.setText(spanned);
			} else {
				v.setText(R.string.placeholder);
			}
			
			v = (TextView) getView().findViewById(R.id.current_main);
			v.setText(InfoFactory.getCategoryFromWeatherId(forecastWeatherId));
			
			v = (TextView) getView().findViewById(R.id.current_uv_index);
			v.setText(InfoFactory.getDescriptionFromWeatherId(forecastWeatherId));
		}
		
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_YEAR, day);
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd", Locale.US);
		String date = dateFormat.format(c.getTime());
		
		TextView v = (TextView) getView().findViewById(R.id.current_date);
		v.setText(date + " " + CalendarFactory.getDayOfWeek(day));
	}

	private static class CalendarFactory {
		
		static String getDayOfWeek(int day) {
			Calendar c = Calendar.getInstance();
			c.add(Calendar.DAY_OF_YEAR, day);
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
	
	private static class InfoFactory {
		
		static String getCategoryFromWeatherId(int weatherId) {
			if (200 <= weatherId && weatherId <= 299) {
				return "Thunderstorm";
			} else if (300 <= weatherId && weatherId <= 399) {
				return "Drizzle";
			} else if (500 <= weatherId && weatherId <= 599) {
				return "Rain";
			} else if (600 <= weatherId && weatherId <= 699) {
				return "Snow";
			} else if (701 == weatherId) {
				return "Mist";
			} else if (711 == weatherId) {
				return "Smoke";
			} else if (721 == weatherId) {
				return "Haze";
			} else if (731 == weatherId) {
				return "Sand";
			} else if (741 == weatherId) {
				return "Fog";
			} else if (751 == weatherId) {
				return "Sand";
			} else if (761 == weatherId) {
				return "Dust";
			} else if (762 == weatherId) {
				return "Volcanic Ash";
			} else if (771 == weatherId) {
				return "Squalls";
			} else if (781 == weatherId) {
				return "Tornado";
			} else if (800 == weatherId) {
				return "Clear";
			} else if (801 <= weatherId && weatherId <= 899) {
				return "Clouds";
			} else {
				return "Null";
			}
		}

		static String getDescriptionFromWeatherId(int weatherId) {
			switch (weatherId) {
			case 200:
				return "thunderstorm with light rain";
			case 201:
				return "thunderstorm with rain";
			case 202:
				return "thunderstorm with heavy rain";
			case 210:
				return "light thunderstorm";
			case 211:
				return "thunderstorm";
			case 212:
				return "heavy thunderstorm";
			case 221:
				return "ragged thunderstorm";
			case 230:
				return "thunderstorm with light drizzle";
			case 231:
				return "thunderstorm with drizzle";
			case 232:
				return "thunderstorm with heavy drizzle";
			case 300:
				return "light intensity drizzle";
			case 301:
				return "drizzle";
			case 302:
				return "heavy intensity drizzle";
			case 310:
				return "light intensity drizzle rain";
			case 311:
				return "drizzle rain";
			case 312:
				return "heavy intensity drizzle rain";
			case 313:
				return "shower rain and drizzle";
			case 314:
				return "heavy shower rain and drizzle";
			case 321:
				return "shower drizzle";
			case 500:
				return "light rain";
			case 501:
				return "moderate rain";
			case 502:
				return "heavy intensity rain";
			case 503:
				return "very heavy rain";
			case 504:
				return "extreme rain";
			case 511:
				return "freezing rain";
			case 520:
				return "light intensity shower rain";
			case 521:
				return "shower rain";
			case 522:
				return "heavy intensity shower rain";
			case 531:
				return "ragged shower rain";
			case 600:
				return "light snow";
			case 601:
				return "snow";
			case 602:
				return "heavy snow";
			case 611:
				return "sleet";
			case 612:
				return "shower sleet";
			case 615:
				return "light rain and snow";
			case 616:
				return "rain and snow";
			case 620:
				return "light shower snow";
			case 621:
				return "shower snow";
			case 622:
				return "heavy shower snow";
			case 701:
				return "mist";
			case 711:
				return "smoke";
			case 721:
				return "haze";
			case 731:
				return "sand, dust whirls";
			case 741:
				return "fog";
			case 751:
				return "sand";
			case 761: 	
				return "dust";
			case 762:
				return "volcanic ash";
			case 771:
				return "squalls";
			case 781:
				return "tornado";
			case 800:
				return "clear sky";
			case 801:
				return "few clouds";
			case 802:
				return "scattered clouds";
			case 803:
				return "broken clouds";
			case 804:
				return "overcast clouds";
			case 900:
				return "tornado";
			case 901:
				return "tropical storm";
			case 902:
				return "hurricane";
			case 903:
				return "cold";
			case 904:
				return "hot";
			case 905:
				return "windy";
			case 906:
				return "hail";
			case 951:
				return "calm";
			case 952:
				return "light breeze";
			case 953:
				return "gentle breeze";
			case 954:
				return "moderate breeze";
			case 955:
				return "fresh breeze";
			case 956: 	
				return "strong breeze";
			case 957: 	
				return "high wind, near gale";
			case 958: 	
				return "gale";
			case 959: 	
				return "severe gale";
			case 960: 	
				return "storm";
			case 961: 	
				return "violent storm";
			case 962: 	
				return "hurricane";
			default:
				return "null";
			}
		}
	
		static String getCategoryFromUvIndex(double uvIndex) {
			if (uvIndex < 0.0f) {
				return "Null";
			} else if (uvIndex < 3.0f) {
				return "Low";
			} else if (uvIndex < 6.0f) {
				return "Moderate";
			} else if (uvIndex < 8.0f) {
				return "High";
			} else if (uvIndex < 11.0f) {
				return "Very high";
			} else {
				return "Extreme";
			}
		}
	
	}

}
