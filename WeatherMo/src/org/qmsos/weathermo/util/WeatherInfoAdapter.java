package org.qmsos.weathermo.util;

import java.util.Calendar;
import java.util.TimeZone;

import org.qmsos.weathermo.R;

import android.view.View;
import android.widget.TextView;

public class WeatherInfoAdapter {

	public static String getDayOfWeek(int day) {
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
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

	public static String getCategoryFromWeatherId(int weatherId) {
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
			return null;
		}
	}

	public static String getDescriptionFromWeatherId(int weatherId) {
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

	public static void setIconOfForecastView(TextView v, int id) {
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

	public static void setBackgroundOfView(View v, int id) {
		if (200 <= id && id <= 299) {
			v.setBackgroundResource(R.drawable.bg_11);
		} else if (300 <= id && id <= 399) {
			v.setBackgroundResource(R.drawable.bg_09);
		} else if (500 <= id && id <= 504) {
			v.setBackgroundResource(R.drawable.bg_10);
		} else if (511 == id) {
			v.setBackgroundResource(R.drawable.bg_13);
		} else if (520 <= id && id <= 599) {
			v.setBackgroundResource(R.drawable.bg_09);
		} else if (600 <= id && id <= 699) {
			v.setBackgroundResource(R.drawable.bg_13);
		} else if (700 <= id && id <= 799) {
			v.setBackgroundResource(R.drawable.bg_50);
		} else if (800 == id) {
			v.setBackgroundResource(R.drawable.bg_01);
		} else if (801 == id) {
			v.setBackgroundResource(R.drawable.bg_02);
		} else if (802 == id || 803 == id) {
			v.setBackgroundResource(R.drawable.bg_03);
		} else if (804 == id) {
			v.setBackgroundResource(R.drawable.bg_04);
		}
	}

}
