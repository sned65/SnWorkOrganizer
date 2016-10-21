package sne.workorganizer.util;

import android.text.InputType;
import android.widget.CalendarView;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Collection of general-purpose utilities.
 */

public class Mix
{
    private Mix() {}

    /**
     * The xml attribute doesn't work for some models
     * as well as plain InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
     * for Samsung devices.
     *
     * @return input type for flag "no suggestion"
     * @see <a href="http://stackoverflow.com/questions/10495296/cant-disable-predictive-text">
     *     http://stackoverflow.com/questions/10495296/cant-disable-predictive-text</a>
     */
    public static int getInputTypeForNoSuggestsInput()
    {
        if (android.os.Build.MANUFACTURER.equalsIgnoreCase("Samsung"))
        {
            return InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                    | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
        }
        else
        {
            return InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
        }
    }

    /**
     *
     * @param date date to be truncated
     * @return date with the time portion of the day truncated
     */
    public static Date truncateDate(Date date)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.HOUR_OF_DAY, c.getActualMinimum(Calendar.HOUR_OF_DAY));
        c.set(Calendar.MINUTE, c.getActualMinimum(Calendar.MINUTE));
        c.set(Calendar.SECOND, c.getActualMinimum(Calendar.SECOND));
        c.set(Calendar.MILLISECOND, c.getActualMinimum(Calendar.MILLISECOND));
        return c.getTime();
    }

    /**
     *
     * @param milliseconds the number of milliseconds since epoch
     * @param field date field as specified in {@code Calendar} such as
     *              {@code Calendar.HOUR_OF_DAY, {@code Calendar.MINUTE}}
     * @return the value of the given field
     *
     * @see Calendar
     */
    public static int getDateField(long milliseconds, int field)
    {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(milliseconds);
        return c.get(field);
    }

    /**
     *
     * @param dt the number of milliseconds since epoch
     * @param hh hours
     * @param mm minutes
     * @return the number of milliseconds since epoch using date part from
     * {@code dt} and the given hours and minutes.
     */
    public static long updateTime(long dt, int hh, int mm)
    {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(dt);
        c.set(Calendar.HOUR_OF_DAY, hh);
        c.set(Calendar.MINUTE, mm);
        c.set(Calendar.SECOND, c.getActualMinimum(Calendar.SECOND));
        c.set(Calendar.MILLISECOND, c.getActualMinimum(Calendar.MILLISECOND));
        return c.getTimeInMillis();
    }

    /**
     * It seems there is a bug in a CalendarView implementation for some devices.
     * In Nexus 10 tablet whatever date is selected, CalendarView returns the current day.
     * And if I keep changing the date, nothing changes - getDate() always returns the current day.
     * This function is workaround which should be called in
     * {@code OnDateChangeListener} of {@link CalendarView}.
     *
     * @param view
     * @param year
     * @param month
     * @param dayOfMonth
     * @see <a href="http://stackoverflow.com/questions/31602849/calendarview-returns-always-current-day-ignoring-what-user-selects">
     *     http://stackoverflow.com/questions/31602849/calendarview-returns-always-current-day-ignoring-what-user-selects</a>
     */
    public static void calendarSetSelection(CalendarView view, int year, int month, int dayOfMonth)
    {
        Calendar c = GregorianCalendar.getInstance();
        c.set(year, month, dayOfMonth);
        long ms = c.getTimeInMillis();
        view.setDate(ms);
    }

    /**
     *
     * @param date1 the number of milliseconds since epoch
     * @param date2 the number of milliseconds since epoch
     * @return {@code true} if the given datetimes have the same day
     */
    public static boolean sameDate(long date1, long date2)
    {
        Calendar c1 = Calendar.getInstance();
        c1.setTimeInMillis(date1);
        Calendar c2 = Calendar.getInstance();
        c2.setTimeInMillis(date2);
        return c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH) &&
                c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH) &&
                c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR);
    }

    /**
     *
     * @param date1 the number of milliseconds since epoch
     * @param date2 the number of milliseconds since epoch
     * @return {@code true} if the given datetimes have the same time (hours and minutes).
     */
    public static boolean sameTime(Long date1, Long date2)
    {
        Calendar c1 = Calendar.getInstance();
        c1.setTimeInMillis(date1);
        Calendar c2 = Calendar.getInstance();
        c2.setTimeInMillis(date2);
        return c1.get(Calendar.HOUR_OF_DAY) == c2.get(Calendar.HOUR_OF_DAY) &&
                c1.get(Calendar.MINUTE) == c2.get(Calendar.MINUTE);
    }
}
