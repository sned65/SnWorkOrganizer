package sne.workorganizer.util;

import android.text.InputType;

import java.util.Calendar;
import java.util.Date;

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
}
