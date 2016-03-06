package org.qmsos.weathermo.resources;

import org.qmsos.weathermo.R;

import android.widget.TextView;

/**
 * Set weather icon to TextView, usually those acted as forecast view.
 */
public class WeatherIconFactory {
	
	/**
	 * Set weather icon to specified TextView, based on the provided weather id.
	 * 
	 * @param tv
	 *            The TextView that the weather icon will be setting to.
	 * @param weatherId
	 *            The id of specified weather.
	 */
	public static void setWeatherIcon(TextView tv, int weatherId) {
		int resId;
		if (200 <= weatherId && weatherId <= 299) {
			resId = R.drawable.ic_11;
		} else if (300 <= weatherId && weatherId <= 399) {
			resId = R.drawable.ic_09;
		} else if (500 <= weatherId && weatherId <= 504) {
			resId = R.drawable.ic_10;
		} else if (511 == weatherId) {
			resId = R.drawable.ic_13;
		} else if (520 <= weatherId && weatherId <= 599) {
			resId = R.drawable.ic_09;
		} else if (600 <= weatherId && weatherId <= 699) {
			resId = R.drawable.ic_13;
		} else if (700 <= weatherId && weatherId <= 799) {
			resId = R.drawable.ic_50;
		} else if (800 == weatherId) {
			resId = R.drawable.ic_01;
		} else if (801 == weatherId) {
			resId = R.drawable.ic_02;
		} else if (802 == weatherId || 803 == weatherId) {
			resId = R.drawable.ic_03;
		} else if (804 == weatherId) {
			resId = R.drawable.ic_04;
		} else {
			resId = 0;
		}
		
		tv.setCompoundDrawablesRelativeWithIntrinsicBounds(0, resId, 0, 0);
	}
	
}