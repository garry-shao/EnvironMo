package org.qmsos.weathermo.resources;

import org.qmsos.weathermo.R;

import android.content.Context;
import android.content.res.Resources;

/**
 * Provide various descriptive information of weather. 
 */
public class WeatherInfoFactory {
	
	/**
	 * Get description of weather category.
	 * 
	 * @param context
	 *            The context that xml resources can be accessed.
	 * @param weatherId
	 *            The id of specified weather.
	 * @return The category description of specified weather.
	 */
	public static String getWeatherCategory(Context context, int weatherId) {
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
			return null;
		}
	}

	/**
	 * Get more detailed description of weather.
	 * 
	 * @param context
	 *            The context that xml resources can be accessed.
	 * @param weatherId
	 *            The id of specified weather.
	 * @return The detailed description of weather.
	 */
	public static String getWeatherDescription(Context context, int weatherId) {
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
			return null;
		}
	}

	/**
	 * Get description based on provided ultra-violet radiation value.
	 * 
	 * @param context
	 *            The context that xml resources can be accessed.
	 * @param uvIndex
	 *            The value of ultra-violet radiation.
	 * @return The description of specified value.
	 */
	public static String getUvIndexDescription(Context context, double uvIndex) {
		Resources resources = context.getResources();
		
		if (uvIndex < 0.0f) {
			return null;
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