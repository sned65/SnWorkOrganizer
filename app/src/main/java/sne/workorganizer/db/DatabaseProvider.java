package sne.workorganizer.db;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;

import sne.workorganizer.BuildConfig;

/**
 * Wraps access to {@link DatabaseHelper}.
 */
public class DatabaseProvider extends ContentProvider
{
    private static final String TAG = DatabaseProvider.class.getName();

    // helper constants for use with the UriMatcher
    private static final int CLIENT_LIST = 1;
    private static final int CLIENT_ID = 2;

    private static final UriMatcher URI_MATCHER;
    // prepare the UriMatcher
    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(DatabaseContract.AUTHORITY, DatabaseContract.Clients.PATH, CLIENT_LIST);
        URI_MATCHER.addURI(DatabaseContract.AUTHORITY, DatabaseContract.Clients.PATH+"/*", CLIENT_ID);
    }

    private DatabaseHelper _db;
    private final ThreadLocal<Boolean> _inBatchMode = new ThreadLocal<>();

    @Override
    public boolean onCreate()
    {
        _db = DatabaseHelper.getInstance(getContext());
        return (_db != null);
    }

    @Nullable
    @Override
    public String getType(Uri uri)
    {
        switch (URI_MATCHER.match(uri))
        {
        case CLIENT_LIST:
            return DatabaseContract.Clients.CONTENT_TYPE;
        case CLIENT_ID:
            return DatabaseContract.Clients.CONTENT_ITEM_TYPE;
        default:
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        //return ContentResolver.CURSOR_DIR_BASE_TYPE+"/vnd.sne.workorganizer.db.clients";//"vnd.android.cursor.dir/sne.workorganizer/client";
//        if (isCollectionUri(uri))
//        {
//            return "vnd.android.cursor.dir/client";
//        }
//
//        return "vnd.android.cursor.item/client";
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection,
                        String selection, String[] selectionArgs, String sortOrder)
    {
        if (BuildConfig.DEBUG)
        {
            Log.i(TAG, "query " + uri);
        }

        SQLiteDatabase db = _db.getReadableDatabase();
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        boolean useAuthorityUri = false;

        switch (URI_MATCHER.match(uri))
        {
        case CLIENT_LIST:
            builder.setTables(DbSchema.TBL_CLIENTS);
            break;
        case CLIENT_ID:
            builder.setTables(DbSchema.TBL_CLIENTS);
            // limit query to one row at most:
            builder.appendWhere(DbSchema.CLIENTS_PK + " = '"
                    + uri.getLastPathSegment() + "'");
            break;
        default:
            throw new IllegalArgumentException("Unsupported URI for query: " + uri);
        }

        if (BuildConfig.DEBUG)
        {
            Log.i(TAG, "query: " + builder.buildQuery(projection, selection, null, null, sortOrder, null));
        }

        Cursor cursor = builder.query(db, projection, selection, selectionArgs,
                null, null, sortOrder);

        // if we want to be notified of any changes:
        if (useAuthorityUri)
        {
            cursor.setNotificationUri(getContext().getContentResolver(), DatabaseContract.CONTENT_URI);
        }
        else
        {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return cursor;

//        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
//        qb.setTables(DatabaseHelper.Table.CLIENTS.toString());
//        Cursor c =
//                qb.query(_db.getReadableDatabase(), projection, selection,
//                        selectionArgs, null, null, sortOrder);
//
//        c.setNotificationUri(getContext().getContentResolver(), uri);
//
//        return c;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        if (BuildConfig.DEBUG)
        {
            Log.i(TAG, "insert " + uri);
        }
        if (URI_MATCHER.match(uri) != CLIENT_LIST)
        {
            throw new IllegalArgumentException(
                    "Unsupported URI for insertion: " + uri);
        }

        SQLiteDatabase db = _db.getWritableDatabase();
        if (URI_MATCHER.match(uri) == CLIENT_LIST)
        {
            long id = db.insert(DbSchema.TBL_CLIENTS, null, values);
            Log.i(TAG, "insert() *** id = "+id);
            Uri uriWithId = getUriForId(id, uri);
            Log.i(TAG, "uriWithId = "+uriWithId);
            return uriWithId;
        }
        else
        {
            return null;
        }
    }

    // TODO check the logic: id is long.
    private Uri getUriForId(long id, Uri uri)
    {
        if (id > 0)
        {
            Uri itemUri = ContentUris.withAppendedId(uri, id);
            if (!isInBatchMode())
            {
                // notify all listeners of changes and return itemUri:
                getContext().getContentResolver().notifyChange(itemUri, null);
            }
            return itemUri;
        }
        // s.th. went wrong:
        throw new SQLException("Problem while inserting into uri: " + uri);
    }

    private boolean isInBatchMode()
    {
        return _inBatchMode.get() != null && _inBatchMode.get();
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs)
    {
        if (BuildConfig.DEBUG)
        {
            Log.i(TAG, "delete " + uri);
        }

        SQLiteDatabase db = _db.getWritableDatabase();
        int delCount = 0;

        switch (URI_MATCHER.match(uri))
        {
        case CLIENT_LIST:
            delCount = db.delete(DbSchema.TBL_CLIENTS, selection, selectionArgs);
            break;
        case CLIENT_ID:
            String idStr = uri.getLastPathSegment();
            String where = DbSchema.CLIENTS_PK + " = '" + idStr + "'";
            if (!TextUtils.isEmpty(selection))
            {
                where += " AND " + selection;
            }
            delCount = db.delete(DbSchema.TBL_CLIENTS, where, selectionArgs);
            break;
        default:
            throw new IllegalArgumentException("Unsupported URI for deletion: " + uri);
        }

        // notify all listeners of changes:
        if (delCount > 0 && !isInBatchMode())
        {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return delCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        if (BuildConfig.DEBUG)
        {
            Log.i(TAG, "update " + uri);
        }

        SQLiteDatabase db = _db.getWritableDatabase();
        int updateCount = 0;

        switch (URI_MATCHER.match(uri))
        {
        case CLIENT_LIST:
            updateCount = db.update(DbSchema.TBL_CLIENTS, values, selection, selectionArgs);
            break;
        case CLIENT_ID:
            String idStr = uri.getLastPathSegment();
            String where = DbSchema.CLIENTS_PK + " = '" + idStr + "'";
            if (!TextUtils.isEmpty(selection))
            {
                where += " AND " + selection;
            }
            updateCount = db.update(DbSchema.TBL_CLIENTS, values, where, selectionArgs);
            break;
        default:
            // no support for updating photos!
            throw new IllegalArgumentException("Unsupported URI for update: " + uri);
        }

        // notify all listeners of changes:
        if (updateCount > 0 && !isInBatchMode())
        {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return updateCount;
    }

    @Override
    public @NonNull ContentProviderResult[] applyBatch(
            @NonNull ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException
    {
        SQLiteDatabase db = _db.getWritableDatabase();
        _inBatchMode.set(true);
        // the next line works because SQLiteDatabase
        // uses a thread local SQLiteSession object for
        // all manipulations
        db.beginTransaction();
        try
        {
            final ContentProviderResult[] retResult = super.applyBatch(operations);
            db.setTransactionSuccessful();
            getContext().getContentResolver().notifyChange(DatabaseContract.CONTENT_URI, null);
            return retResult;
        }
        finally
        {
            _inBatchMode.remove();
            db.endTransaction();
        }
    }
}
