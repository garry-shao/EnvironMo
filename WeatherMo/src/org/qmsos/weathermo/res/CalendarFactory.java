package org.qmsos.weathermo.res;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.qmsos.weathermo.R;

import android.content.Context;
import android.content.res.Resources;

/**
 * Provide day of the week.
 */
public class CalendarFactory {

	/**
	 * Get formatted date description based on current time.
	 * 
	 * @param day
	 *            How many days in difference of today, 1 means tomorrow, 
	 *            0 means today, -1 means yesterday, etc.
	 * @return The formatted description of date.
	 */
	public static String getOffsetDate(int day) {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_YEAR, day);
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd", Locale.US);
		String date = dateFormat.format(c.getTime());
		
		return date;
	}
	
	/**
	 * Get day of week based on current time.
	 * 
	 * @param context
	 *            The context that xml resources can be accessed.
	 * @param day
	 *            How many days in difference of today, 1 means tomorrow, 
	 *            0 means today, -1 means yesterday, etc.
	 * @return The abbreviation of specified day of weak.
	 */
	public static String getDayOfWeek(Context context, int day) {
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