package org.qmsos.weathermo.res;

import org.qmsos.weathermo.R;

/**
 * Provide various descriptive information of weather. 
 */
public class TextFactory {

    /**
     * Get description based on provided ultra-violet radiation value.
     *
     * @param uvIndex
     *            The value of ultra-violet radiation.
     * @return The resource id of the description of specified value.
     */
    public static int getUvIndexDescription(double uvIndex) {
        int resId;

        if (uvIndex < 0.0f) {
            resId = 0;
        } else if (uvIndex < 3.0f) {
            resId = R.string.uv_low;
        } else if (uvIndex < 6.0f) {
            resId = R.string.uv_moderate;
        } else if (uvIndex < 8.0f) {
            resId = R.string.uv_high;
        } else if (uvIndex < 11.0f) {
            resId = R.string.uv_very_high;
        } else {
            resId = R.string.uv_extreme;
        }

        return resId;
    }

    /**
     * Get description of weather category.
     *
     * @param weatherId
     *            The id of specified weather.
     * @return The resource id of the category description of specified weather.
     */
    public static int getWeatherCategory(int weatherId) {
        int resId;

        switch (weatherId) {
        case 200:
        case 201:
        case 202:
        case 210:
        case 211:
        case 212:
        case 221:
        case 230:
        case 231:
        case 232:
            resId = R.string.cat_thunderstorm;
            break;
        case 300:
        case 301:
        case 302:
        case 310:
        case 311:
        case 312:
        case 313:
        case 314:
        case 321:
            resId = R.string.cat_drizzle;
            break;
        case 500:
        case 501:
        case 502:
        case 503:
        case 504:
        case 511:
        case 520:
        case 521:
        case 522:
        case 531:
            resId = R.string.cat_rain;
            break;
        case 601:
        case 602:
        case 620:
        case 621:
        case 622:
        case 611:
        case 612:
        case 615:
        case 616:
            resId = R.string.cat_snow;
            break;
        case 701:
            resId = R.string.cat_mist;
            break;
        case 711:
            resId = R.string.cat_smoke;
            break;
        case 721:
            resId = R.string.cat_haze;
            break;
        case 731:
        case 751:
            resId = R.string.cat_sand;
            break;
        case 741:
            resId = R.string.cat_fog;
            break;
        case 761:
            resId = R.string.cat_dust;
            break;
        case 762:
            resId = R.string.cat_volcanic_ash;
            break;
        case 771:
            resId = R.string.cat_squalls;
            break;
        case 781:
            resId = R.string.cat_tornado;
            break;
        case 800:
            resId = R.string.cat_clear;
            break;
        case 801:
        case 802:
        case 803:
        case 804:
            resId = R.string.cat_clouds;
            break;
        default:
            resId = R.string.ui_null;
        }

        return resId;
    }

    /**
     * Get more detailed description of weather.
     *
     * @param weatherId
     *            The id of specified weather.
     * @return The resource id of the detailed description of weather.
     */
    public static int getWeatherDescription(int weatherId) {
        int resId;

        switch (weatherId) {
        case 200:
            resId = R.string.desc_thunderstorm_rain_light;
            break;
        case 201:
            resId = R.string.desc_thunderstorm_rain;
            break;
        case 202:
            resId = R.string.desc_thunderstorm_rain_heavy;
            break;
        case 210:
            resId = R.string.desc_thunderstorm_light;
            break;
        case 211:
            resId = R.string.desc_thunderstorm;
            break;
        case 212:
            resId = R.string.desc_thunderstorm_heavy;
            break;
        case 221:
            resId = R.string.desc_thunderstorm_ragged;
            break;
        case 230:
            resId = R.string.desc_thunderstorm_drizzle_light;
            break;
        case 231:
            resId = R.string.desc_thunderstorm_drizzle;
            break;
        case 232:
            resId = R.string.desc_thunderstorm_drizzle_heavy;
            break;
        case 300:
            resId = R.string.desc_drizzle_light;
            break;
        case 301:
            resId = R.string.desc_drizzle;
            break;
        case 302:
            resId = R.string.desc_drizzle_heavy;
            break;
        case 310:
            resId = R.string.desc_drizzle_rain_light;
            break;
        case 311:
            resId = R.string.desc_drizzle_rain;
            break;
        case 312:
            resId = R.string.desc_drizzle_rain_heavy;
            break;
        case 313:
            resId = R.string.desc_drizzle_shower_rain;
            break;
        case 314:
            resId = R.string.desc_drizzle_shower_heavy;
            break;
        case 321:
            resId = R.string.desc_drizzle_shower;
            break;
        case 500:
            resId = R.string.desc_rain_light;
            break;
        case 501:
            resId = R.string.desc_rain_moderate;
            break;
        case 502:
            resId = R.string.desc_rain_heavy;
            break;
        case 503:
            resId = R.string.desc_rain_very_heavy;
            break;
        case 504:
            resId = R.string.desc_rain_extreme;
            break;
        case 511:
            resId = R.string.desc_rain_freezing;
            break;
        case 520:
            resId = R.string.desc_rain_shower_light;
            break;
        case 521:
            resId = R.string.desc_rain_shower;
            break;
        case 522:
            resId = R.string.desc_rain_shower_heavy;
            break;
        case 531:
            resId = R.string.desc_rain_shower_ragged;
            break;
        case 600:
            resId = R.string.desc_snow_light;
            break;
        case 601:
            resId = R.string.desc_snow;
            break;
        case 602:
            resId = R.string.desc_snow_heavy;
            break;
        case 611:
            resId = R.string.desc_sleet;
            break;
        case 612:
            resId = R.string.desc_sleet_shower;
            break;
        case 615:
            resId = R.string.desc_snow_rain_light;
            break;
        case 616:
            resId = R.string.desc_snow_rain;
            break;
        case 620:
            resId = R.string.desc_snow_shower_light;
            break;
        case 621:
            resId = R.string.desc_snow_shower;
            break;
        case 622:
            resId = R.string.desc_snow_shower_heavy;
            break;
        case 701:
            resId = R.string.desc_mist;
            break;
        case 711:
            resId = R.string.desc_smoke;
            break;
        case 721:
            resId = R.string.desc_haze;
            break;
        case 731:
            resId = R.string.desc_sand_dust;
            break;
        case 741:
            resId = R.string.desc_fog;
            break;
        case 751:
            resId = R.string.desc_sand;
            break;
        case 761:
            resId = R.string.desc_dust;
            break;
        case 762:
            resId = R.string.desc_volcanic_ash;
            break;
        case 771:
            resId = R.string.desc_squalls;
            break;
        case 781:
            resId = R.string.desc_tornado;
            break;
        case 800:
            resId = R.string.desc_sky_clear;
            break;
        case 801:
            resId = R.string.desc_clouds_few;
            break;
        case 802:
            resId = R.string.desc_clouds_scattered;
            break;
        case 803:
            resId = R.string.desc_clouds_broken;
            break;
        case 804:
            resId = R.string.desc_clouds_overcast;
            break;
        case 900:
            resId = R.string.desc_tornado;
            break;
        case 901:
            resId = R.string.desc_storm_tropical;
            break;
        case 902:
            resId = R.string.desc_hurricane;
            break;
        case 903:
            resId = R.string.desc_cold;
            break;
        case 904:
            resId = R.string.desc_hot;
            break;
        case 905:
            resId = R.string.desc_windy;
            break;
        case 906:
            resId = R.string.desc_hail;
            break;
        case 951:
            resId = R.string.desc_calm;
            break;
        case 952:
            resId = R.string.desc_breeze_light;
            break;
        case 953:
            resId = R.string.desc_breeze_gentle;
            break;
        case 954:
            resId = R.string.desc_breeze_moderate;
            break;
        case 955:
            resId = R.string.desc_breeze_fresh;
            break;
        case 956:
            resId = R.string.desc_breeze_strong;
            break;
        case 957:
            resId = R.string.desc_gale_near;
            break;
        case 958:
            resId = R.string.desc_gale;
            break;
        case 959:
            resId = R.string.desc_gale_severe;
            break;
        case 960:
            resId = R.string.desc_storm;
            break;
        case 961:
            resId = R.string.desc_storm_violent;
            break;
        case 962:
            resId = R.string.desc_hurricane;
            break;
        default:
            resId = R.string.ui_null;
        }

        return resId;
    }
}