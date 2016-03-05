package org.qmsos.weathermo.fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.qmsos.weathermo.R;
import org.qmsos.weathermo.provider.WeatherContract.WeatherEntity;
import org.qmsos.weathermo.util.WeatherParser;

import android.content.Context;
import android.content.res.Resources;
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
			v.setText(InfoFactory.getCategoryFromWeatherId(getContext(), currentWeatherId));
			
			v = (TextView) getView().findViewById(R.id.current_uv_index);
			if (Double.compare(uvIndex, WeatherParser.UV_INDEX_INVALID) != 0) {
				String uvIndexDescription = InfoFactory.getCategoryFromUvIndex(getContext(), uvIndex);
				v.setText("UV: " + uvIndex + " - " + uvIndexDescription);
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
			v.setText(InfoFactory.getCategoryFromWeatherId(getContext(), forecastWeatherId));
			
			v = (TextView) getView().findViewById(R.id.current_uv_index);
			v.setText(InfoFactory.getDescriptionFromWeatherId(getContext(), forecastWeatherId));
		}
		
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_YEAR, day);
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd", Locale.US);
		String date = dateFormat.format(c.getTime());
		
		TextView v = (TextView) getView().findViewById(R.id.current_date);
		v.setText(date + " " + CalendarFactory.getDayOfWeek(getContext(), day));
	}

	private static class CalendarFactory {
		
		static String getDayOfWeek(Context context, int day) {
			Resources resources = context.getResources();
			
			Calendar c = Calendar.getInstance();
			c.add(Calendar.DAY_OF_YEAR, day);
			int i = c.get(Calendar.DAY_OF_WEEK);
			switch (i) {
			case 1:
				return resources.getString(R.string.abbrev_sunday);
			case 2:
				return resources.getString(R.string.abbrev_monday);
			case 3:
				return resources.getString(R.string.abbrev_tuesday);
			case 4:
				return resources.getString(R.string.abbrev_wednesday);
			case 5:
				return resources.getString(R.string.abbrev_thursday);
			case 6:
				return resources.getString(R.string.abbrev_friday);
			case 7:
				return resources.getString(R.string.abbrev_saturday);
			default:
				return null;
			}
		}
	
	}	
	
	private static class InfoFactory {
		
		static String getCategoryFromWeatherId(Context context, int weatherId) {
			Resources resources = context.getResources();
			
			if (200 <= weatherId && weatherId <= 299) {
				return resources.getString(R.string.cat_thunderstorm);
			} else if (300 <= weatherId && weatherId <= 399) {
				return resources.getString(R.string.cat_drizzle);
			} else if (500 <= weatherId && weatherId <= 599) {
				return resources.getString(R.string.cat_rain);
			} else if (600 <= weatherId && weatherId <= 699) {
				return resources.getString(R.string.cat_snow);
			} else if (701 == weatherId) {
				return resources.getString(R.string.cat_mist);
			} else if (711 == weatherId) {
				return resources.getString(R.string.cat_smoke);
			} else if (721 == weatherId) {
				return resources.getString(R.string.cat_haze);
			} else if (731 == weatherId) {
				return resources.getString(R.string.cat_sand);
			} else if (741 == weatherId) {
				return resources.getString(R.string.cat_fog);
			} else if (751 == weatherId) {
				return resources.getString(R.string.cat_sand);
			} else if (761 == weatherId) {
				return resources.getString(R.string.cat_dust);
			} else if (762 == weatherId) {
				return resources.getString(R.string.cat_volcanic_ash);
			} else if (771 == weatherId) {
				return resources.getString(R.string.cat_squalls);
			} else if (781 == weatherId) {
				return resources.getString(R.string.cat_tornado);
			} else if (800 == weatherId) {
				return resources.getString(R.string.cat_clear);
			} else if (801 <= weatherId && weatherId <= 899) {
				return resources.getString(R.string.cat_clouds);
			} else {
				return resources.getString(R.string.cat_invalid);
			}
		}

		static String getDescriptionFromWeatherId(Context context, int weatherId) {
			Resources resources = context.getResources();
			
			switch (weatherId) {
			case 200:
				return resources.getString(R.string.desc_thunderstorm_rain_light);
			case 201:
				return resources.getString(R.string.desc_thunderstorm_rain);
			case 202:
				return resources.getString(R.string.desc_thunderstorm_rain_heavy);
			case 210:
				return resources.getString(R.string.desc_thunderstorm_light);
			case 211:
				return resources.getString(R.string.desc_thunderstorm);
			case 212:
				return resources.getString(R.string.desc_thunderstorm_heavy);
			case 221:
				return resources.getString(R.string.desc_thunderstorm_ragged);
			case 230:
				return resources.getString(R.string.desc_thunderstorm_drizzle_light);
			case 231:
				return resources.getString(R.string.desc_thunderstorm_drizzle);
			case 232:
				return resources.getString(R.string.desc_thunderstorm_drizzle_heavy);
			case 300:
				return resources.getString(R.string.desc_drizzle_light);
			case 301:
				return resources.getString(R.string.desc_drizzle);
			case 302:
				return resources.getString(R.string.desc_drizzle_heavy);
			case 310:
				return resources.getString(R.string.desc_drizzle_rain_light);
			case 311:
				return resources.getString(R.string.desc_drizzle_rain);
			case 312:
				return resources.getString(R.string.desc_drizzle_rain_heavy);
			case 313:
				return resources.getString(R.string.desc_drizzle_shower_rain);
			case 314:
				return resources.getString(R.string.desc_drizzle_shower_heavy);
			case 321:
				return resources.getString(R.string.desc_drizzle_shower);
			case 500:
				return resources.getString(R.string.desc_rain_light);
			case 501:
				return resources.getString(R.string.desc_rain_moderate);
			case 502:
				return resources.getString(R.string.desc_rain_heavy);
			case 503:
				return resources.getString(R.string.desc_rain_very_heavy);
			case 504:
				return resources.getString(R.string.desc_rain_extreme);
			case 511:
				return resources.getString(R.string.desc_rain_freezing);
			case 520:
				return resources.getString(R.string.desc_rain_shower_light);
			case 521:
				return resources.getString(R.string.desc_rain_shower);
			case 522:
				return resources.getString(R.string.desc_rain_shower_heavy);
			case 531:
				return resources.getString(R.string.desc_rain_shower_ragged);
			case 600:
				return resources.getString(R.string.desc_snow_light);
			case 601:
				return resources.getString(R.string.desc_snow);
			case 602:
				return resources.getString(R.string.desc_snow_heavy);
			case 611:
				return resources.getString(R.string.desc_sleet);
			case 612:
				return resources.getString(R.string.desc_sleet_shower);
			case 615:
				return resources.getString(R.string.desc_snow_rain_light);
			case 616:
				return resources.getString(R.string.desc_snow_rain);
			case 620:
				return resources.getString(R.string.desc_snow_shower_light);
			case 621:
				return resources.getString(R.string.desc_snow_shower);
			case 622:
				return resources.getString(R.string.desc_snow_shower_heavy);
			case 701:
				return resources.getString(R.string.desc_mist);
			case 711:
				return resources.getString(R.string.desc_smoke);
			case 721:
				return resources.getString(R.string.desc_haze);
			case 731:
				return resources.getString(R.string.desc_sand_dust);
			case 741:
				return resources.getString(R.string.desc_fog);
			case 751:
				return resources.getString(R.string.desc_sand);
			case 761: 	
				return resources.getString(R.string.desc_dust);
			case 762:
				return resources.getString(R.string.desc_volcanic_ash);
			case 771:
				return resources.getString(R.string.desc_squalls);
			case 781:
				return resources.getString(R.string.desc_tornado);
			case 800:
				return resources.getString(R.string.desc_sky_clear);
			case 801:
				return resources.getString(R.string.desc_clouds_few);
			case 802:
				return resources.getString(R.string.desc_clouds_scattered);
			case 803:
				return resources.getString(R.string.desc_clouds_broken);
			case 804:
				return resources.getString(R.string.desc_clouds_overcast);
			case 900:
				return resources.getString(R.string.desc_tornado);
			case 901:
				return resources.getString(R.string.desc_storm_tropical);
			case 902:
				return resources.getString(R.string.desc_hurricane);
			case 903:
				return resources.getString(R.string.desc_cold);
			case 904:
				return resources.getString(R.string.desc_hot);
			case 905:
				return resources.getString(R.string.desc_windy);
			case 906:
				return resources.getString(R.string.desc_hail);
			case 951:
				return resources.getString(R.string.desc_calm);
			case 952:
				return resources.getString(R.string.desc_breeze_light);
			case 953:
				return resources.getString(R.string.desc_breeze_gentle);
			case 954:
				return resources.getString(R.string.desc_breeze_moderate);
			case 955:
				return resources.getString(R.string.desc_breeze_fresh);
			case 956: 	
				return resources.getString(R.string.desc_breeze_strong);
			case 957: 	
				return resources.getString(R.string.desc_gale_near);
			case 958: 	
				return resources.getString(R.string.desc_gale);
			case 959: 	
				return resources.getString(R.string.desc_gale_severe);
			case 960: 	
				return resources.getString(R.string.desc_storm);
			case 961: 	
				return resources.getString(R.string.desc_storm_violent);
			case 962: 	
				return resources.getString(R.string.desc_hurricane);
			default:
				return resources.getString(R.string.desc_invalid);
			}
		}
	
		static String getCategoryFromUvIndex(Context context, double uvIndex) {
			Resources resources = context.getResources();
			
			if (uvIndex < 0.0f) {
				return resources.getString(R.string.uv_invalid);
			} else if (uvIndex < 3.0f) {
				return resources.getString(R.string.uv_low);
			} else if (uvIndex < 6.0f) {
				return resources.getString(R.string.uv_moderate);
			} else if (uvIndex < 8.0f) {
				return resources.getString(R.string.uv_high);
			} else if (uvIndex < 11.0f) {
				return resources.getString(R.string.uv_very_high);
			} else {
				return resources.getString(R.string.uv_extreme);
			}
		}
	
	}

}
