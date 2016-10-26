package sne.workorganizer.util;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.annotation.RequiresPermission;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

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
        ctx.startActivity(callIntent);
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
                    Toast.LENGTH_SHORT).show();
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
     *
     * @param ctx the context to use.
     * @param imageView {@link ImageView} to load an image into.
     * @param imageUri image location as an encoded URI string.
     *                 URI should point to a publishing content available for {@link ContentResolver},
     *                 i.e., should start with {@code "content://"}.
     * @param bitmapWidth the new bitmap's desired width. The image will be scaled proportionally.
     * @return image display name, if any.
     * @throws FileNotFoundException
     */
    public static String setScaledBitmap(Context ctx, ImageView imageView, String imageUri, int bitmapWidth)
            throws FileNotFoundException
    {
        Uri uri = Uri.parse(imageUri);
        return setScaledBitmap(ctx, imageView, uri, bitmapWidth);
    }

    /**
     *
     * @param ctx the context to use.
     * @param imageView {@link ImageView} to load an image into.
     * @param imageUri {@code Uri} of image location.
     *                 URI should point to a publishing content available for {@link ContentResolver}.
     * @param bitmapWidth the new bitmap's desired width. The image will be scaled proportionally.
     * @return image display name, if any.
     * @throws FileNotFoundException
     */
    public static String setScaledBitmap(Context ctx, ImageView imageView, Uri imageUri, int bitmapWidth)
            throws FileNotFoundException
    {
        String displayName = null;

        try
        {
            ContentResolver resolver = ctx.getContentResolver();
            String[] projection = new String[]{OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE};
            Cursor c = resolver.query(imageUri, projection, null, null, null);
            if (c == null)
            {
                throw new FileNotFoundException(imageUri.toString());
            }
            while (c.moveToNext())
            {
                int name_idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                displayName = c.getString(name_idx);
                int size_idx = c.getColumnIndex(OpenableColumns.SIZE);
                Long size = c.getLong(size_idx);
                Log.i(TAG, "setScaledBitmap() name = " + displayName + ", size = " + size);
            }
            c.close();
            //Log.i(TAG, "setScaledBitmap() path = "+path);
            //imageView.setImageURI(designUri);

            InputStream is = resolver.openInputStream(imageUri);
            Bitmap bm = BitmapFactory.decodeStream(is);
            assert is != null;
            close(is);
            int nh = (int) (bm.getHeight() * ((float) bitmapWidth / bm.getWidth()));
            Bitmap scaled = Bitmap.createScaledBitmap(bm, bitmapWidth, nh, true);
            imageView.setImageBitmap(scaled);
        }
        catch (SecurityException e)
        {
            throw new FileNotFoundException(e.getMessage());
        }

        return displayName;
    }

    /**
     * Returns the bitmap of the given size
     *
     * @param path path to original image
     * @param thumbnailSize
     * @return thumbnail image
     */
    public static Bitmap getThumbnailBitmap(String path, int thumbnailSize)
    {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bounds);
        if ((bounds.outWidth == -1) || (bounds.outHeight == -1))
        {
            return null;
        }
        int originalSize = (bounds.outHeight > bounds.outWidth) ? bounds.outHeight
                : bounds.outWidth;
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = originalSize / thumbnailSize;
        return BitmapFactory.decodeFile(path, opts);
    }

    /**
     * Create a file for saving a jpeg-image.
     *
     * @param basename   base filename
     * @return {@code Uri} of created file
     */
    public static Uri getOutputMediaFileUri(String basename)
    {
        return Uri.fromFile(getOutputMediaFile(basename));
    }

    /**
     * Create a File for saving an jpeg-image.
     * <br/>
     * Pattern for filename: {@code basename_timestamp.jpg}
     *
     * @param basename   base filename
     * @return created {@code File}
     */
    private static File getOutputMediaFile(String basename)
    {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "WorkOrganizer");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists())
        {
            if (!mediaStorageDir.mkdirs())
            {
                return null;
            }
        }

        // Create a media file name
        SimpleDateFormat sdf = new SimpleDateFormat("_yyyyMMdd_HHmmss", Locale.US);
        String today = sdf.format(new Date());
        String file_name = basename + today + ".jpg";
        return new File(mediaStorageDir.getPath() + File.separator + file_name);
    }
}
