package sne.workorganizer.db;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

/**
 * This class contains file manipulation utilities.
 * The database tables keep only the paths to the images and
 * the images are saved in the internal storage of the application.
 */
public class PictureFiles
{
    private static final String TAG = PictureFiles.class.getName();
    private static final String APP_DATA_DIR = "WorkOrganizer";

    private static File getInternalFile(Context ctx, String id)
    {
        File internalStorage = ctx.getDir(APP_DATA_DIR, Context.MODE_PRIVATE);
        return new File(internalStorage, id + ".png");
    }

    /**
     * Create or update an internal png-file for the given picture.
     *
     * @param ctx {@code Context}
     * @param id the identifier of the picture (GUID)
     * @param picture the picture to save to the internal storage.
     */
    public static void savePicture(Context ctx, String id, Bitmap picture)
    {
        File file = getInternalFile(ctx, id);
        if (file.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }

        FileOutputStream fos;
        try
        {
            fos = new FileOutputStream(file);
            // Note: PNG which is lossless, will ignore the quality setting
            picture.compress(Bitmap.CompressFormat.PNG, 100 /*quality*/, fos);
            fos.close();
        }
        catch (Exception ex)
        {
            Log.i(TAG, "Problem saving picture for id = " + id, ex);
        }
    }

    /**
     *
     * @param ctx {@code Context}
     * @param id the identifier of the picture (GUID)
     */
    public static void deletePicture(Context ctx, String id)
    {
        File file = getInternalFile(ctx, id);
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }

    /**
     *
     * @param ctx {@code Context}
     * @param id the identifier of the picture (GUID)
     * @return the resulting bitmap, or {@code null} if it could not be decoded.
     */
    public static Bitmap retrievePicture(Context ctx, String id)
    {
        String path = getInternalFile(ctx, id).getPath();
        return BitmapFactory.decodeFile(path);
    }
}
