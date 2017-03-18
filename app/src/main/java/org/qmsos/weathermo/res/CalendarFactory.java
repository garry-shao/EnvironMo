package org.qmsos.weathermo.res;

import android.content.Context;
import android.content.res.Resources;

import org.qmsos.weathermo.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

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

        return dateFormat.format(c.getTime());
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
        case Calendar.SUNDAY:
            return resources.getString(R.string.abbrev_sunday);
        case Calendar.MONDAY:
            return resources.getString(R.string.abbrev_monday);
        case Calendar.TUESDAY:
            return resources.getString(R.string.abbrev_tuesday);
        case Calendar.WEDNESDAY:
            return resources.getString(R.string.abbrev_wednesday);
        case Calendar.THURSDAY:
            return resources.getString(R.string.abbrev_thursday);
        case Calendar.FRIDAY:
            return resources.getString(R.string.abbrev_friday);
        case Calendar.SATURDAY:
            return resources.getString(R.string.abbrev_saturday);
        default:
            return null;
        }
    }
}