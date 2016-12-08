package sne.workorganizer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Process;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import sne.workorganizer.BuildConfig;
import sne.workorganizer.util.Mix;

// Notes about work with photos:
// http://blog.cindypotvin.com/saving-an-image-in-a-sqlite-database-in-your-android-application/

/**
 * Singleton class for database manipulations.
 */
public class DatabaseHelper extends SQLiteOpenHelper
{
    /**
     * List of DB tables.
     */
    private enum Table
    {
        CLIENTS, PROJECTS, PICTURES;

        @Override
        public String toString()
        {
            return name().toLowerCase();
        }
    }

    private static final String TAG = DatabaseHelper.class.getName();

    private static final String DATABASE_NAME = "SnWorkOrganizer.db";
    private static final int SCHEMA_VERSION = 1;

    // CLIENTS TABLE

    private static final String CLIENTS_PK = "client_id";
    public static final String CLIENTS_COL_FULLNAME = "fullname";
    private static final String CLIENTS_COL_PHONE = "phone";
    private static final String CLIENTS_COL_EMAIL = "email";
    private static final String CLIENTS_COL_SOCIAL = "social";
    private static final String CLIENTS_TYPED_COLUMNS =
            CLIENTS_PK + " TEXT PRIMARY KEY, " +
                    CLIENTS_COL_FULLNAME + " TEXT, " +
                    CLIENTS_COL_PHONE + " TEXT, " +
                    CLIENTS_COL_EMAIL + " TEXT, " +
                    CLIENTS_COL_SOCIAL + " TEXT";
    private static final String CLIENTS_COLUMNS =
            CLIENTS_PK + ", " + CLIENTS_COL_FULLNAME + ", " +
                    CLIENTS_COL_PHONE + ", " + CLIENTS_COL_EMAIL + ", " + CLIENTS_COL_SOCIAL;

    // END OF CLIENTS TABLE

    // WORKS TABLE

    private static final String PROJECTS_PK = "proj_id";
    private static final String PROJECTS_COL_CLIENT_ID = "client_id";
    private static final String PROJECTS_COL_NAME = "proj_name";
    private static final String PROJECTS_COL_DATE = "work_date";
    private static final String PROJECTS_COL_STATUS = "status";
    private static final String PROJECTS_COL_PRICE = "price";
    private static final String PROJECTS_COL_DESIGN = "design";
    private static final String PROJECTS_TYPED_COLUMNS =
            PROJECTS_PK + " TEXT PRIMARY KEY, " +
                    PROJECTS_COL_CLIENT_ID + " TEXT, " +
                    PROJECTS_COL_NAME + " TEXT, " +
                    PROJECTS_COL_DATE + " INTEGER, " +
                    PROJECTS_COL_STATUS + " TEXT, " +
                    PROJECTS_COL_PRICE + " NUMERIC, " +
                    PROJECTS_COL_DESIGN + " TEXT," +
            " FOREIGN KEY(" + PROJECTS_COL_CLIENT_ID +
                    ") REFERENCES clients(" + CLIENTS_PK + ") ON DELETE CASCADE";
    private static final String PROJECTS_COLUMNS =
            PROJECTS_PK + ", " +
                    PROJECTS_COL_CLIENT_ID + ", " +
                    PROJECTS_COL_NAME + ", " +
                    PROJECTS_COL_DATE + ", " +
                    PROJECTS_COL_STATUS + ", " +
                    PROJECTS_COL_PRICE + ", " +
                    PROJECTS_COL_DESIGN;

    // END OF WORKS TABLE


    // PICTURES TABLE

    private static final String PICTURES_PK = "pict_id";
    private static final String PICTURES_COL_WORK_ID = "proj_id";
    private static final String PICTURES_COL_PHOTO = "photo";
    private static final String PICTURES_TYPED_COLUMNS =
            PICTURES_PK + " TEXT PRIMARY KEY, " +
                    PICTURES_COL_WORK_ID + " TEXT, " +
                    PICTURES_COL_PHOTO + " TEXT," +
                    " FOREIGN KEY(" + PICTURES_COL_WORK_ID + ") REFERENCES projects(" +
                    PROJECTS_PK + ") ON DELETE SET NULL";
    private static final String PICTURES_COLUMNS =
            PICTURES_PK + ", " + PICTURES_COL_WORK_ID + ", " + PICTURES_COL_PHOTO;

    // END OF PICTURES TABLE


    // Notes:
    // (1) I put photos in a separate table to be possible
    //     to keep the image when its project is removed
    //     (for publicity).
    // (2) The PICTURES table keeps only the path to the image and
    //     the image is saved in the internal storage of the application.
    private static final String[] DB_CREATE_SQL = new String[] {
            "CREATE TABLE "+ Table.CLIENTS + "(" + CLIENTS_TYPED_COLUMNS + ");",
            "CREATE TABLE "+ Table.PROJECTS + "(" + PROJECTS_TYPED_COLUMNS + ");",
            "CREATE TABLE "+ Table.PICTURES + "(" + PICTURES_TYPED_COLUMNS + ");",

            "CREATE INDEX idx_projects_client ON projects(client_id)",
            "CREATE INDEX idx_pictures_proj ON pictures(proj_id)"
    };

    private static DatabaseHelper _instance;

    //////////////////////////////////////////////////////////////////
    public synchronized static DatabaseHelper getInstance(Context ctx)
    {
        if (_instance == null)
        {
            _instance = new DatabaseHelper(ctx.getApplicationContext());
        }
        return _instance;
    }

    //////////////////////////////////////////////////////////////////
    //   C A L L B A C K   I N T E R F A C E S
    //////////////////////////////////////////////////////////////////

    public interface DbSelectClientsCallback
    {
        void onSelectFinished(ArrayList<Client> records);
    }

    public interface DbSelectProjectsCallback
    {
        void onSelectFinished(ArrayList<Project> records);
    }

    public interface DbSelectPicturesCallback
    {
        void onSelectFinished(ArrayList<Picture> records);
    }

    public interface DbCreateClientCallback
    {
        void onCreateFinished(Client client);
    }

    public interface DbCreateWorkCallback
    {
        void onCreateFinished(Project work);
    }

    public interface DbUpdateClient
    {
        void onUpdateFinished(Client client);
    }

    public interface DbDeleteCallback
    {
        void onDeleteFinished(String id);
    }

    //////////////////////////////////////////////////////////////////
    //   E N D   O F   C A L L B A C K   I N T E R F A C E S
    //////////////////////////////////////////////////////////////////

    private DatabaseHelper(Context ctx)
    {
        super(ctx, DATABASE_NAME, null, SCHEMA_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        for (String sql : DB_CREATE_SQL)
        {
            db.execSQL(sql);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        throw new RuntimeException("No upgrade available");
    }

    @Override
    public void onOpen(SQLiteDatabase db)
    {
        super.onOpen(db);
        // SQLite disables foreign key constraint by default, so we need to enable it.
        db.execSQL("PRAGMA foreign_keys=ON");
    }

    private ArrayList<Client> selectClients()
    {
        ArrayList<Client> clients = new ArrayList<>();
        String sql = "SELECT " + CLIENTS_COLUMNS + " FROM " + Table.CLIENTS + " ORDER BY " + CLIENTS_COL_FULLNAME;

        SQLiteDatabase db = getReadableDatabase();
        if (BuildConfig.DEBUG)
        {
            Log.d(TAG, sql);
        }
        Cursor c = db.rawQuery(sql, null);

        while (c.moveToNext())
        {
            Client client = createClientFromCursor(c);
            clients.add(client);
        }

        c.close();
        return clients;
    }

    private ArrayList<Project> selectProjects(Date dateFrom, Date dateTo, String where)
    {
        ArrayList<Project> projects = new ArrayList<>();
        String sql;
        String[] args = null;
        if (dateFrom == null)
        {
            sql = "SELECT " + PROJECTS_COLUMNS + " FROM " + Table.PROJECTS +
                    (where == null ? "" : " WHERE "+where) +
                    " ORDER BY " + PROJECTS_COL_DATE;
        }
        else
        {
            sql = "SELECT " + PROJECTS_COLUMNS + " FROM " + Table.PROJECTS +
                    " WHERE " + PROJECTS_COL_DATE + " > ? AND " + PROJECTS_COL_DATE + " < ?" +
                    " ORDER BY " + PROJECTS_COL_DATE;

            long from = dateFrom.getTime();
            long to;
            if (dateTo == null)
            {
                to = from + 24 * 60 * 60 * 1000;
            }
            else
            {
                to = dateTo.getTime() + 24 * 60 * 60 * 1000;
            }
            args = new String[] { String.valueOf(from), String.valueOf(to) };
        }

        SQLiteDatabase db = getReadableDatabase();
        if (BuildConfig.DEBUG)
        {
            Log.d(TAG, sql + ", using " + dateFrom);
        }
        Cursor c = db.rawQuery(sql, args);

        while (c.moveToNext())
        {
            Project proj = createProjectFromCursor(c);
            projects.add(proj);
        }

        c.close();
        return projects;
    }

    private ArrayList<Picture> selectPictures()
    {
        ArrayList<Picture> pictures = new ArrayList<>();
        String sql = "SELECT " + PICTURES_COLUMNS + " FROM " + Table.PICTURES;

        SQLiteDatabase db = getReadableDatabase();
        if (BuildConfig.DEBUG)
        {
            Log.d(TAG, sql);
        }
        Cursor c = db.rawQuery(sql, null);

        while (c.moveToNext())
        {
            Picture p = createPictureFromCursor(c);
            pictures.add(p);
        }

        c.close();
        return pictures;
    }

    private Client createClientFromCursor(Cursor c)
    {
        final String sqlWorks = "SELECT " + PROJECTS_COLUMNS + " FROM " + Table.PROJECTS
                + " WHERE client_id = ? ORDER BY " + PROJECTS_COL_DATE;

        Client client = new Client();
        client.setId(c.getString(0));
        client.setName(c.getString(1));
        client.setPhone(c.getString(2));
        client.setEmail(c.getString(3));
        client.setSocial(c.getString(4));

        // Fill projects

        SQLiteDatabase db = getReadableDatabase();
        String[] args = new String[1];

        List<Project> projects = new ArrayList<>();
        args[0] = client.getId();
        if (BuildConfig.DEBUG)
        {
            Log.d(TAG, sqlWorks + ", using " + args[0]);
        }
        Cursor c_work = db.rawQuery(sqlWorks, args);
        while (c_work.moveToNext())
        {
            Project work = createProjectFromCursor(c_work);
            projects.add(work);
        }
        c_work.close();
        client.setProjects(projects);
        return client;
    }

    private Project createProjectFromCursor(Cursor c)
    {
        Project proj = new Project();
        proj.setId(c.getString(0));
        proj.setClientId(c.getString(1));
        proj.setName(c.getString(2));
        proj.setDate(c.getLong(3));
        proj.setStatus(Project.WorkStatus.fromString(c.getString(4)));
        proj.setPrice(c.getInt(5));
        proj.setDesign(c.getString(6));
        return proj;
    }

    private Picture createPictureFromCursor(Cursor c)
    {
        Picture p = new Picture();
        p.setId(c.getString(0));
        p.setWorkId(c.getString(1));
        p.setResultPhoto(c.getString(2));
        return p;
    }

    /**
     * Finds all clients asynchronously.
     * Calls {@code callback} when the query is finished.
     *
     * @param callback to be called when the query is finished. Can be {@code null}.
     */
    public void findAllClients(DbSelectClientsCallback callback)
    {
        new SelectClientsTask(callback).execute();
    }

    /**
     * Synchronously selects {@code Client} by its id.
     * FIXME remove "Synchronously".
     *
     * @param clientId id of client
     * @return {@code Client} corresponding the given id, or {@code null}
     */
    public Client findClientById(String clientId)
    {
        Client client = null;
        String sql = "SELECT " + CLIENTS_COLUMNS + " FROM " + Table.CLIENTS +
                " WHERE " + CLIENTS_PK + " = ?";

        SQLiteDatabase db = getReadableDatabase();
        if (BuildConfig.DEBUG)
        {
            Log.d(TAG, sql + ", using " + clientId);
        }
        String[] args = new String[] { clientId };
        Cursor c = db.rawQuery(sql, args);

        while (c.moveToNext())
        {
            if (client != null)
            {
                throw new SQLiteDatabaseCorruptException("Too many clients for id "+clientId);
            }
            client = createClientFromCursor(c);
        }

        c.close();
        return client;
    }

    // FIXME remove "Synchronously".
    public Cursor getClientCursorByNameStart(String startsWith)
    {
        String selection;
        String [] selectionArgs;
        if (TextUtils.isEmpty(startsWith))
        {
            selection = null;
            selectionArgs = null;
        }
        else
        {
            selection = DatabaseHelper.CLIENTS_COL_FULLNAME+" LIKE ?";
            selectionArgs = new String[] { startsWith+"%" };
        }

        String table = DatabaseHelper.Table.CLIENTS.toString();
        String[] columns = new String[] {
                DatabaseHelper.CLIENTS_PK+" AS _id",
                DatabaseHelper.CLIENTS_COL_FULLNAME
        };
        String orderBy = DatabaseHelper.CLIENTS_COL_FULLNAME;
        Cursor result =
                getReadableDatabase()
                        .query(table, columns,
                                selection, selectionArgs,
                                null, null, orderBy);

        // Call getCount() on the Cursor, to force it to actually perform
        // the query — query() returns the Cursor, but the query is not
        // actually executed until we do something that needs the result set.
        // We need to make sure to "touch" the Cursor while we are on the
        // background thread.
        result.getCount();

        return result;
    }

    /**
     * Finds all projects asynchronously for the given date range.
     * Calls {@code callback} when the query is finished.
     *
     * @param callback to be called when the query is finished. Can be {@code null}.
     * @param dateFrom start date
     * @param dateTo end date (including).
     *               If {@code null} then {@code dateFrom} is used as the end date.
     */
    public void findAllProjects(DbSelectProjectsCallback callback, Date dateFrom, Date dateTo)
    {
        new SelectProjectsTask(callback, dateFrom, dateTo).execute();
    }

    /**
     * Finds all projects with filled design column.
     * Calls {@code callback} when the query is finished.
     *
     * @param callback to be called when the query is finished. Can be {@code null}.
     */
    public void findAllProjectsWithDesign(DbSelectProjectsCallback callback)
    {
        new SelectProjectsTask(callback, PROJECTS_COL_DESIGN+" IS NOT NULL").execute();
    }

    /**
     * Synchronously selects {@link Project} by its id.
     *
     * @param workId id of work
     * @return {@link Project} corresponding the given id, or {@code null}
     */
    public Project findProjectById(String workId)
    {
        Project work = null;
        String sql = "SELECT " + PROJECTS_COLUMNS + " FROM " + Table.PROJECTS +
                " WHERE " + PROJECTS_PK + " = ?";

        SQLiteDatabase db = getReadableDatabase();
        if (BuildConfig.DEBUG)
        {
            Log.d(TAG, sql + ", using " + workId);
        }
        String[] args = new String[] { workId };
        Cursor c = db.rawQuery(sql, args);

        while (c.moveToNext())
        {
            if (work != null)
            {
                throw new SQLiteDatabaseCorruptException("Too many works for id "+workId);
            }
            work = createProjectFromCursor(c);
        }

        c.close();
        return work;
    }

    /**
     * Finds all pictures asynchronously.
     * Calls {@code callback} when the query is finished.
     *
     * @param callback to be called when the query is finished. Can be {@code null}.
     */
    public void findAllPictures(DbSelectPicturesCallback callback)
    {
        new SelectPicturesTask(callback).execute();
    }

    /**
     * Insert new client in a separate thread.
     *
     * @param client client to be inserted
     * @param callback to be called after insertion
     */
    public void createClient(Client client, DbCreateClientCallback callback)
    {
        new InsertTask(client, callback).execute();
    }

    /**
     * Update client in a separate thread.
     *
     * @param client existing client with new data
     * @param callback to be called after update
     */
    public void updateClient(Client client, DbUpdateClient callback)
    {
        new UpdateTask(client, callback).execute();
    }

    /**
     * Delete client in a separate thread.
     *
     * @param id id of {@code Client}
     * @param callback to be called when delete is finished. Can be {@code null}.
     */
    public void deleteClient(String id, DbDeleteCallback callback)
    {
        new DeleteTask(Table.CLIENTS, id, callback).execute();
    }

    /*
     * Insert new project in a separate thread.
     *
     * @param project project to be inserted
     */
//    public void createProject(Project project)
//    {
//        createProjectWithClient(project, null);
//    }

    /**
     * Insert new work and client in a separate thread.
     *
     * @param project work to be inserted
     * @param newClient new client.
     *                  Should be {@code null} if the work is for existing client.
     * @param callback to be called after insertion
     */
    public void createProjectWithClient(Project project, @Nullable Client newClient, DbCreateWorkCallback callback)
    {
        new InsertTask(project, newClient, callback).execute();
    }

    /**
     * Delete work in a separate thread.
     *
     * @param id id of {@code Client}
     * @param callback to be called when delete is finished. Can be {@code null}.
     */
    public void deleteWork(String id, DbDeleteCallback callback)
    {
        new DeleteTask(Table.PROJECTS, id, callback).execute();
    }

    /**
     * Update work in a separate thread.
     *
     * @param work existing work with new data
     * @param picture update a reference in {@code Picture} table.
     *                If {@code null} then the reference is not changed.
     * param callback to be called after update
     */
    public void updateWork(Project work, Picture picture)
    {
        new UpdateTask(work, picture).execute();
    }

    /*
     * Insert new picture in a separate thread.
     *
     * @param picture picture to be inserted
     */
//    public void createPicture(Picture picture)
//    {
//        new InsertTask(picture).execute();
//    }

    /*
     * Update picture in a separate thread.
     *
     * @param picture existing picture with new data
     */
//    public void updatePicture(Picture picture)
//    {
//        new UpdateTask(picture).start();
//    }

    /**
     * Delete picture in a separate thread.
     *
     * @param id id of {@link Picture}
     * @param callback to be called when delete is finished. Can be {@code null}.
     */
    public void deletePicture(String id, DbDeleteCallback callback)
    {
        new DeleteTask(Table.PICTURES, id, callback).execute();
    }

    /**
     * Synchronously selects {@code Picture} by its id.
     *
     * @param pictureId id of picture
     * @return {@link Picture} corresponding the given id, or {@code null}
     */
    public Picture findPictureById(String pictureId)
    {
        Picture picture = null;
        String sql = "SELECT " + PICTURES_COLUMNS + " FROM " + Table.PICTURES +
                " WHERE " + PICTURES_PK + " = ?";

        SQLiteDatabase db = getReadableDatabase();
        if (BuildConfig.DEBUG)
        {
            Log.d(TAG, sql + ", using " + pictureId);
        }
        String[] args = new String[] { pictureId };
        Cursor c = db.rawQuery(sql, args);

        while (c.moveToNext())
        {
            if (picture != null)
            {
                throw new SQLiteDatabaseCorruptException("Too many pictures for id "+pictureId);
            }
            picture = createPictureFromCursor(c);
        }

        c.close();
        return picture;
    }

    /**
     * Synchronously selects {@code Picture} by its id.
     *
     * @param workId {@link Project} id
     * @return {@link Picture} corresponding the given work id, or {@code null}
     * @throws SQLiteDatabaseCorruptException if too many pictures found for the given work id.
     */
    public Picture findPictureByWorkId(String workId)
    {
        Picture picture = null;
        String sql = "SELECT " + PICTURES_COLUMNS + " FROM " + Table.PICTURES +
                " WHERE " + PICTURES_COL_WORK_ID + " = ?";

        SQLiteDatabase db = getReadableDatabase();
        if (BuildConfig.DEBUG)
        {
            Log.d(TAG, "findPictureByWorkId(): "+sql + ", using " + workId);
        }
        String[] args = new String[] { workId };
        Cursor c = db.rawQuery(sql, args);

        while (c.moveToNext())
        {
            if (picture != null)
            {
                throw new SQLiteDatabaseCorruptException("Too many pictures for work id "+workId);
            }
            picture = createPictureFromCursor(c);
        }

        c.close();
        Log.d(TAG, "findPictureByWorkId() picture "+(picture == null ? "NOT found" : picture.getResultPhoto()));
        return picture;
    }

    private void deleteResultPictures(String workId)
    {
        if (BuildConfig.DEBUG)
        {
            Log.i(TAG, "InsertTask: Delete Picture for work "+workId);
        }
        getWritableDatabase().delete(Table.PICTURES.toString(), PICTURES_COL_WORK_ID + " = ?", new String[] { workId });
    }

    /**
     * SQL INSERT
     */
    private class InsertTask extends AsyncTask<Void, Void, Void>
    {
        private Client _client;
        private DbCreateClientCallback _clientCallback;
        private Project _work;
        private DbCreateWorkCallback _workCallback;
        private Picture _picture;
        //private DbCreatePictureCallback _pictureCallback;

        InsertTask(Client client, DbCreateClientCallback callback)
        {
            super();
            _client = client;
            _clientCallback = callback;
        }

        InsertTask(Project work, DbCreateWorkCallback callback)
        {
            super();
            _work = work;
            _workCallback = callback;
        }

        InsertTask(Project work, Client client, DbCreateWorkCallback callback)
        {
            super();
            _client = client;
            _work = work;
            _workCallback = callback;
        }

        InsertTask(Picture picture)
        {
            super();
            _picture = picture;
        }

        InsertTask(Project work, Picture picture)
        {
            super();
            _work = work;
            _picture = picture;
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            if (_client != null)
            {
                ContentValues values = new ContentValues();
                values.put(CLIENTS_PK, _client.getId());
                values.put(CLIENTS_COL_FULLNAME, _client.getName());
                values.put(CLIENTS_COL_PHONE, _client.getPhone());
                values.put(CLIENTS_COL_EMAIL, _client.getEmail());
                values.put(CLIENTS_COL_SOCIAL, _client.getSocial());
                if (BuildConfig.DEBUG)
                {
                    Log.i(TAG, "InsertTask: Client "+values);
                }
                getWritableDatabase().insert(Table.CLIENTS.toString(), null, values);
            }

            if (_work != null)
            {
                ContentValues values = new ContentValues();
                values.put(PROJECTS_PK, _work.getId());
                values.put(PROJECTS_COL_CLIENT_ID, _work.getClientId());
                values.put(PROJECTS_COL_NAME, _work.getName());
                values.put(PROJECTS_COL_DATE, _work.getDate());
                values.put(PROJECTS_COL_STATUS, _work.getStatus().toString());
                values.put(PROJECTS_COL_PRICE, _work.getPrice());
                values.put(PROJECTS_COL_DESIGN, _work.getDesign());
                if (BuildConfig.DEBUG)
                {
                    Log.i(TAG, "InsertTask: Work "+values);
                }
                getWritableDatabase().insert(Table.PROJECTS.toString(), null, values);
            }

            if (_picture != null)
            {
                String photo = _picture.getResultPhoto();
                if (TextUtils.isEmpty(photo))
                {
                    deleteResultPictures(_picture.getWorkId());
                }
                else
                {
                    ContentValues values = new ContentValues();
                    values.put(PICTURES_PK, _picture.getId());
                    values.put(PICTURES_COL_WORK_ID, _picture.getWorkId());
                    values.put(PICTURES_COL_PHOTO, photo);
                    if (BuildConfig.DEBUG)
                    {
                        Log.i(TAG, "InsertTask: Picture "+values);
                    }
                    getWritableDatabase().insert(Table.PICTURES.toString(), null, values);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            if (_clientCallback != null)
            {
                _clientCallback.onCreateFinished(_client);
            }
            if (_workCallback != null)
            {
                _workCallback.onCreateFinished(_work);
            }
            // TODO picture
        }
    }

    /**
     * SQL UPDATE
     */
    private class UpdateTask extends AsyncTask<Void, Void, Void>
    {
        private Client _client;
        private DbUpdateClient _clientCallback;
        private Project _work;
        private Picture _picture;

        UpdateTask(Client client, DbUpdateClient callback)
        {
            super();
            _client = client;
            _clientCallback = callback;
        }

        UpdateTask(Project work)
        {
            super();
            _work = work;
        }

        UpdateTask(Project work, Client client)
        {
            super();
            _client = client;
            _work = work;
        }

        UpdateTask(Picture picture)
        {
            super();
            _picture = picture;
        }

        UpdateTask(Project work, Picture picture)
        {
            super();
            _work = work;
            _picture = picture;
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            if (_client != null)
            {
                ContentValues values = new ContentValues();
                values.put(CLIENTS_COL_FULLNAME, _client.getName());
                values.put(CLIENTS_COL_PHONE, _client.getPhone());
                values.put(CLIENTS_COL_EMAIL, _client.getEmail());
                values.put(CLIENTS_COL_SOCIAL, _client.getSocial());
                if (BuildConfig.DEBUG)
                {
                    Log.i(TAG, "UpdateTask: Client "+_client.getId()+": "+values);
                }
                getWritableDatabase().update(Table.CLIENTS.toString(), values, CLIENTS_PK + " = ?", new String[] {_client.getId()});
            }

            if (_work != null)
            {
                ContentValues values = new ContentValues();
                values.put(PROJECTS_COL_CLIENT_ID, _work.getClientId());
                values.put(PROJECTS_COL_NAME, _work.getName());
                values.put(PROJECTS_COL_DATE, _work.getDate());
                values.put(PROJECTS_COL_STATUS, _work.getStatus().toString());
                values.put(PROJECTS_COL_PRICE, _work.getPrice());
                values.put(PROJECTS_COL_DESIGN, _work.getDesign());
                if (BuildConfig.DEBUG)
                {
                    Log.i(TAG, "UpdateTask: Work: "+_work.getId()+": "+values);
                }
                getWritableDatabase().update(Table.PROJECTS.toString(), values, PROJECTS_PK + " = ?", new String[] {_work.getId()});
            }

            if (_picture != null)
            {
                String photo = _picture.getResultPhoto();
                if (TextUtils.isEmpty(photo))
                {
                    deleteResultPictures(_picture.getWorkId());
                }
                else
                {
                    ContentValues values = new ContentValues();
                    values.put(PICTURES_COL_PHOTO, photo);
                    if (BuildConfig.DEBUG)
                    {
                        Log.i(TAG, "UpdateTask: Picture for work "+_picture.getWorkId()+": "+values);
                    }
                    int n = getWritableDatabase().update(Table.PICTURES.toString(), values, PICTURES_COL_WORK_ID + " = ?", new String[] {_picture.getWorkId()});
                    if (n == 0)
                    {
                        // no rows updated => insert new row
                        values.put(PICTURES_PK, _picture.getId());
                        values.put(PICTURES_COL_WORK_ID, _picture.getWorkId());
                        if (BuildConfig.DEBUG)
                        {
                            Log.i(TAG, "UpdateTask: Insert Picture: "+values);
                        }
                        getWritableDatabase().insert(Table.PICTURES.toString(), null, values);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            if (_clientCallback != null)
            {
                _clientCallback.onUpdateFinished(_client);
            }
            // TODO work
//            if (_workCallback != null)
//            {
//                _workCallback.onCreateFinished(_work);
//            }
            // TODO picture
        }

        private void clearResultPictures(String workId)
        {
            String sql = "UPDATE " + Table.PICTURES +
                    " SET " + PICTURES_COL_WORK_ID + " = NULL" +
                    " WHERE " + PICTURES_COL_WORK_ID + " = ?";
            Object[] args = new Object[1];
            args[0] = workId;
            if (BuildConfig.DEBUG)
            {
                Log.d(TAG, String.format("clearResultPictures: %s, using %s", sql, args[0]));
            }

            getWritableDatabase().execSQL(sql, args);
        }
    }

    /**
     * SQL DELETE
     */
    private class DeleteTask extends AsyncTask<Void, Void, Void>
    {
        private final Table _table;
        private final String _id;
        private final DbDeleteCallback _callback;

        DeleteTask(Table table, String id, DbDeleteCallback callback)
        {
            super();
            _table = table;
            _id = id;
            _callback = callback;
        }

        @Override
        protected Void doInBackground(Void... params)
        {
            SystemClock.sleep(5000);
            String sql;
            Object[] args = { _id };

            if (_table == Table.CLIENTS)
            {
                sql = "DELETE FROM " + _table + " WHERE " + CLIENTS_PK + " = ?";
            }
            else if (_table == Table.PROJECTS)
            {
                sql = "DELETE FROM " + _table + " WHERE " + PROJECTS_PK + " = ?";
            }
            else if (_table == Table.PICTURES)
            {
                sql = "DELETE FROM " + _table + " WHERE " + PICTURES_PK + " = ?";
            }
            else
            {
                throw new IllegalArgumentException("Cannot delete from unknown table "+_table);
            }

            if (BuildConfig.DEBUG)
            {
                Log.i(TAG, sql + ", using " + _id);
            }
            getWritableDatabase().execSQL(sql, args);
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            if (_callback != null)
            {
                _callback.onDeleteFinished(_id);
            }
        }
    }

    private class SelectClientsTask extends AsyncTask<Void, Void, ArrayList<Client>>
    {
        private DbSelectClientsCallback _callback = null;

        SelectClientsTask(DbSelectClientsCallback callback)
        {
            super();
            _callback = callback;
        }

        @Override
        protected ArrayList<Client> doInBackground(Void... params)
        {
            return selectClients();
        }

        @Override
        protected void onPostExecute(ArrayList<Client> result)
        {
            if (_callback != null)
            {
                _callback.onSelectFinished(result);
            }
        }
    }

    private class SelectProjectsTask extends AsyncTask<Void, Void, ArrayList<Project>>
    {
        private DbSelectProjectsCallback _callback = null;
        private Date _dateFrom;
        private Date _dateTo;
        private String _where;

        SelectProjectsTask(DbSelectProjectsCallback callback, Date dateFrom, Date dateTo)
        {
            super();
            _callback = callback;
            if (dateFrom != null)
            {
                _dateFrom = Mix.truncateDate(dateFrom);
            }
            if (dateTo != null)
            {
                _dateTo = Mix.truncateDate(dateTo);
            }
        }

        SelectProjectsTask(DbSelectProjectsCallback callback, String where)
        {
            super();
            _callback = callback;
            _where = where;
        }

        @Override
        protected ArrayList<Project> doInBackground(Void... params)
        {
            //SystemClock.sleep(5000);
            return selectProjects(_dateFrom, _dateTo, _where);
        }

        @Override
        protected void onPostExecute(ArrayList<Project> result)
        {
            if (_callback != null)
            {
                _callback.onSelectFinished(result);
            }
        }
    }

    private class SelectPicturesTask extends AsyncTask<Void, Void, ArrayList<Picture>>
    {
        private DbSelectPicturesCallback _callback = null;

        SelectPicturesTask(DbSelectPicturesCallback callback)
        {
            super();
            _callback = callback;
        }

        @Override
        protected ArrayList<Picture> doInBackground(Void... params)
        {
            return selectPictures();
        }

        @Override
        protected void onPostExecute(ArrayList<Picture> result)
        {
            if (_callback != null)
            {
                _callback.onSelectFinished(result);
            }
        }
    }
}
