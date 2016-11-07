package sne.workorganizer.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import sne.workorganizer.R;

/**
 * Collection of general-purpose utilities.
 */

public class Mix
{
    private static final String TAG = Mix.class.getName();

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
     * Adds one month and subtracts one day to/from the given date.
     *
     * @param date date to be incremented
     * @return new date
     */
    public static Date plusMonth(Date date)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.MONTH, 1);
        c.add(Calendar.DAY_OF_MONTH, -1);
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
     * @param view {@link CalendarView}
     * @param year year
     * @param month month. The month value is 0-based.
     * @param dayOfMonth day of month
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

    /**
     *
     * @param millis the number of milliseconds since epoch
     * @return {@code true} if the given date is Saturday or Sunday
     */
    public static boolean isHoliday(long millis)
    {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY;
    }

    /**
     * Wrapper around new and deprecated {@link TimePicker#getHour()} function.
     * @param timePicker {@code TimePicker} from which to get the currently selected hour
     * @return the currently selected hour, in the range (0-23)
     */
    public static int timePickerGetHour(TimePicker timePicker)
    {
        int hh;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            hh = timePicker.getHour();
        }
        else
        {
            //noinspection deprecation
            hh = timePicker.getCurrentHour();
        }
        return hh;
    }

    /**
     * Wrapper around new and deprecated {@link TimePicker#setHour(int)} function.
     * @param timePicker {@code TimePicker} to set the hour
     * @param hour the hour to set, in the range (0-23)
     */
    public static void timePickerSetHour(TimePicker timePicker, int hour)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            timePicker.setHour(hour);
        }
        else
        {
            //noinspection deprecation
            timePicker.setCurrentHour(hour);
        }
    }

    /**
     * Wrapper around new and deprecated {@link TimePicker#getMinute()} function.
     * @param timePicker {@code TimePicker} from which to get the currently selected minute
     * @return the currently selected minute, in the range (0-59)
     */
    public static int timePickerGetMinute(TimePicker timePicker)
    {
        int mm;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            mm = timePicker.getMinute();
        }
        else
        {
            //noinspection deprecation
            mm = timePicker.getCurrentMinute();
        }
        return mm;
    }

    /**
     * Wrapper around new and deprecated {@link TimePicker#setMinute(int)} function.
     * @param timePicker {@code TimePicker} to set the currently selected minute
     * @param minute the minute to set, in the range (0-59)
     */
    public static void timePickerSetMinute(TimePicker timePicker, int minute)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            timePicker.setMinute(minute);
        }
        else
        {
            //noinspection deprecation
            timePicker.setCurrentMinute(minute);
        }
    }

    /**
     * Display the dialog with error/warning/info message.
     *
     * @param activity parent activity
     * @param message the message to display
     * @param severity message severity as defined by {@code InfoDialogFragment.Severity}
     */
    public static void showError(Activity activity, String message,
                                 InfoDialogFragment.Severity severity)
    {
        InfoDialogFragment about = new InfoDialogFragment();
        Bundle args = new Bundle();
        args.putString(InfoDialogFragment.ARG_MESSAGE, message);
        args.putString(InfoDialogFragment.ARG_SEVERITY, severity.toString());
        about.setArguments(args);
        about.show(activity.getFragmentManager(), "error");
    }

    /**
     * Dial a number as specified by the {@code phone}.
     *
     * @param ctx The context to use.
     * @param phone phone to be dialed.
     */
    public static void callPhone(Context ctx, String phone)
    {
        if (TextUtils.isEmpty(phone)) return;
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:"+ Uri.encode(phone.trim())));
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try
        {
            ctx.startActivity(callIntent);
        }
        catch (android.content.ActivityNotFoundException e)
        {
            Toast.makeText(ctx,
                    "No phone installed.",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Invoke an activity to send e-mail.
     *
     * @param ctx The context to use.
     * @param email e-mail address
     */
    public static void sendEmail(Context ctx, String email)
    {
        Log.i(TAG, "sendEmail() called for "+email);
        if (TextUtils.isEmpty(email)) return;

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        //emailIntent.setType("text/plain");
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] { email });
        try
        {
            ctx.startActivity(Intent.createChooser(emailIntent, ctx.getString(R.string.send_email_title)));
        }
        catch (android.content.ActivityNotFoundException e)
        {
            Toast.makeText(ctx,
                    "No email clients installed.",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Invoke an activity to browse URL.
     *
     * @param ctx The context to use.
     * @param url URL to be opened.
     */
    public static void openUrl(Context ctx, String url)
    {
        if (TextUtils.isEmpty(url)) return;
        if (!url.startsWith("http://") && !url.startsWith("https://"))
        {
            url = "http://" + url;
        }
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        if (browserIntent.resolveActivity(ctx.getPackageManager()) != null)
        {
            ctx.startActivity(browserIntent);
        }
    }

    /**
     * Display a photo.
     *
     * @param ctx The context to use.
     * @param path The path of the file to be shown.
     */
    public static void showPhoto(Context ctx, String path)
    {
        if (TextUtils.isEmpty(path)) return;
        File file = new File(path);
        Uri uri = Uri.fromFile(file);
        Intent showPhotoIntent = new Intent(Intent.ACTION_VIEW);
        //showPhotoIntent.setData(uri);
        showPhotoIntent.setDataAndType(uri, "image/jpeg");
        if (showPhotoIntent.resolveActivity(ctx.getPackageManager()) != null)
        {
            ctx.startActivity(showPhotoIntent);
        }
    }

    /**
     * Setups {@code OnClickListener}s for client communication {@code TextView}s.
     *
     * @param ctx The context to use.
     * @param clientPhone {@code TextView} containing a phone number
     * @param clientEmail {@code TextView} containing an e-mail address
     * @param clientSocial {@code TextView} containing URL
     */
    public static void setupClientCommunications(final Context ctx,
                                                 TextView clientPhone, TextView clientEmail, TextView clientSocial)
    {
        clientPhone.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String phone = ((TextView) v).getText().toString();
                Mix.callPhone(ctx, phone);
            }
        });

        clientSocial.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String url = ((TextView) v).getText().toString().trim();
                Mix.openUrl(ctx, url);
            }
        });

        clientEmail.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String email = ((TextView) v).getText().toString();
                Mix.sendEmail(ctx, email);
            }
        });
    }

    /**
     * Closes the given stream suppressing an error than can occur while closing this stream.
     *
     * @param is {@link InputStream}
     */
    public static void close(InputStream is)
    {
        if (is != null)
        {
            try
            {
                is.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Checks that all permissions are granted.
     * If at least one permission was not granted the activity will be finished.
     *
     * @param activity activity which requested permissions.
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     */
    public static void checkGrantedPermissions(final Activity activity,
                                               @NonNull String[] permissions,
                                               @NonNull int[] grantResults)
    {
        for (int i = 0; i < grantResults.length; ++i)
        {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
            {
                String msg = activity.getString(R.string.error_permissions)+" " + permissions[i]
                        + "\n" + activity.getString(R.string.app_will_be_stopped);
                Log.e(TAG, msg);

                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        activity.finish();
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(R.string.app_name)
                        .setMessage(msg)
                        .setPositiveButton(R.string.ok, listener)
                        .show();
                return;
            }
            else
            {
                Log.i(TAG, "checkGrantedPermissions() Permission "+permissions[i]+" granted");
            }
        }
    }
}
