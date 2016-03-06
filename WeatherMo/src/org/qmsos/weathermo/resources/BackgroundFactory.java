package org.qmsos.weathermo.resources;

import org.qmsos.weathermo.R;

import android.view.View;

/**
 * Set background of view, usually the outer most ViewGroup. 
 */
public class BackgroundFactory {
	
	/**
	 * Set the background of the view to specified drawable based on the weather id
	 * that passed on.
	 * 
	 * @param v
	 *            The view that the background would be set upon.
	 * @param weatherId
	 *            The id of specified weather., background drawable would be selected 
	 *            based on this parameter.
	 */
	public static void setBackground(View v, int weatherId) {
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
			resId = R.drawable.bg_thunderstorm;
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
			resId = R.drawable.bg_drizzle;
			break;
		case 500:
		case 501:
		case 502:
		case 503:
		case 504:
			resId = R.drawable.bg_rain;
			break;
		case 511:
			resId = R.drawable.bg_rain_freezing;
			break;
		case 520:
		case 521:
		case 522:
		case 531:
			resId = R.drawable.bg_rain_shower;
			break;
		case 600:
		case 601:
		case 602:
		case 620:
		case 621:
		case 622:
			resId = R.drawable.bg_snow;
			break;
		case 611:
		case 612:
		case 615:
		case 616:
			resId = R.drawable.bg_snow_rain;
			break;
		case 701:
		case 741:
			resId = R.drawable.bg_mist;
			break;
		case 711:
		case 721:
			resId = R.drawable.bg_haze;
			break;
		case 731:
		case 751:
		case 761:
			resId = R.drawable.bg_sand;
			break;
		case 762:
			resId = R.drawable.bg_volcanic_ash;
			break;
		case 771:
			resId = R.drawable.bg_squalls;
			break;
		case 781:
			resId = R.drawable.bg_tornado;
			break;
		case 800:
			resId = R.drawable.bg_sky_clear;
			break;
		case 801:
			resId = R.drawable.bg_clouds_few;
			break;
		case 802:
			resId = R.drawable.bg_clouds_scattered;
			break;
		case 803:
			resId = R.drawable.bg_clouds_broken;
			break;
		case 804:
			resId = R.drawable.bg_clouds_overcast;
			break;
		default:
			resId = 0;
		}
		
		v.setBackgroundResource(resId);
	}

}