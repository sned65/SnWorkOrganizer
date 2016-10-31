package sne.workorganizer.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Collection of utilities to work wih images and files.
 */
public class PhotoUtils
{
    private static final String TAG = PhotoUtils.class.getName();
    private static final String WO_DIR = "WorkOrganizer";

    private PhotoUtils()
    {
    }


    /**
     * @param ctx         the context to use.
     * @param imageView   {@link ImageView} to load an image into.
     * @param imageUri    image location as an encoded URI string.
     *                    URI should point to a publishing content available for {@link ContentResolver},
     *                    i.e., should start with {@code "content://"}.
     * @param bitmapWidth the new bitmap's desired width. The image will be scaled proportionally.
     * @return image display name, if any.
     * @throws FileNotFoundException
     * @deprecated Use {@link #getThumbnailBitmap(String, int)}
     */
    @Deprecated
    public static String setScaledBitmap(Context ctx, ImageView imageView, String imageUri, int bitmapWidth)
            throws FileNotFoundException
    {
        Uri uri = Uri.parse(imageUri);
        return setScaledBitmap(ctx, imageView, uri, bitmapWidth);
    }

    /**
     * @param ctx         the context to use.
     * @param imageView   {@link ImageView} to load an image into.
     * @param imageUri    {@code Uri} of image location.
     *                    URI should point to a publishing content available for {@link ContentResolver}.
     * @param bitmapWidth the new bitmap's desired width. The image will be scaled proportionally.
     * @return image display name, if any.
     * @throws FileNotFoundException
     * @deprecated Use {@link #getThumbnailBitmap(String, int)}
     */
    @Deprecated
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
            Mix.close(is);
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
     * Returns the bitmap of the given size.
     * If {@code thumbnailSize} less than the number of pixels
     * in either dimension of the original image, then
     * return a smaller image. Otherwise, the original image is returned.
     *
     * @param path          path to original image
     * @param thumbnailSize recommended size in the larger dimension.
     * @return thumbnail image
     */
    public static Bitmap getThumbnailBitmap(String path, int thumbnailSize)
    {
        Log.d(TAG, "getThumbnailBitmap("+path+", "+thumbnailSize+") called");
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bounds);
        if ((bounds.outWidth == -1) || (bounds.outHeight == -1))
        {
            Log.d(TAG, "getThumbnailBitmap() bounds.outWidth="+bounds.outWidth+", bounds.outHeight="+bounds.outHeight);
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
     * @param basename base filename
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
     * @param basename base filename
     * @return created {@code File}
     */
    private static File getOutputMediaFile(String basename)
    {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), WO_DIR);
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
