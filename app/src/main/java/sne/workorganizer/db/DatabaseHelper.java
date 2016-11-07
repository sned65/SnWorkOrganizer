package sne.workorganizer.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import sne.workorganizer.util.Mix;

// TODO work with photos
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
    private static final int CLIENTS_NCOLS = 5;
    private static final String CLIENTS_TYPED_COLUMNS =
            "client_id TEXT PRIMARY KEY, fullname TEXT, phone TEXT, email TEXT, social TEXT";
    private static final String CLIENTS_COLUMNS =
            "client_id, fullname, phone, email, social";
    private static final String CLIENTS_VALUE_PLACEHOLDERS =
            "?, ?, ?, ?, ?";
    public static final String CLIENTS_COL_FULLNAME = "fullname";

    // END OF CLIENTS TABLE

    // WORKS TABLE

    private static final int PROJECTS_NCOLS = 7;
    private static final String PROJECTS_PK = "proj_id";
    private static final String PROJECTS_COL_DESIGN = "design";
    private static final String PROJECTS_TYPED_COLUMNS =
            "proj_id TEXT PRIMARY KEY, client_id TEXT, proj_name TEXT, work_date INTEGER, status TEXT, price NUMERIC, " +
                    PROJECTS_COL_DESIGN + " TEXT," +
            " FOREIGN KEY(client_id) REFERENCES clients(client_id) ON DELETE CASCADE";
    private static final String PROJECTS_COLUMNS =
            "proj_id, client_id, proj_name, work_date, status, price, "+PROJECTS_COL_DESIGN;
    private static final String PROJECTS_VALUE_PLACEHOLDERS =
            "?, ?, ?, ?, ?, ?, ?";

    // END OF WORKS TABLE


    // PICTURES TABLE

    private static final int PICTURES_NCOLS = 3;
    private static final String PICTURES_PK = "pict_id";
    private static final String PICTURES_COL_WORK_ID = "proj_id";
    private static final String PICTURES_COL_PHOTO = "photo";
    private static final String PICTURES_TYPED_COLUMNS =
            PICTURES_PK + " TEXT PRIMARY KEY, " +
                    PICTURES_COL_WORK_ID + " TEXT, " +
                    PICTURES_COL_PHOTO + " TEXT," +
                    " FOREIGN KEY(" + PICTURES_COL_WORK_ID + ") REFERENCES projects(proj_id) ON DELETE SET NULL";
    private static final String PICTURES_COLUMNS =
            PICTURES_PK + ", " + PICTURES_COL_WORK_ID + ", " + PICTURES_COL_PHOTO;
    private static final String PICTURES_VALUE_PLACEHOLDERS =
            "?, ?, ?";

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

    private static final int INSERT_POSITION = 0;

    //////////////////////////////////////////////////////////////////
    public synchronized static DatabaseHelper getInstance(Context ctx)
    {
        if (_instance == null)
        {
            _instance = new DatabaseHelper(ctx.getApplicationContext());
        }
        return _instance;
    }

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

    private ArrayList<Client> selectClients()
    {
        ArrayList<Client> clients = new ArrayList<>();
        String sql = "SELECT " + CLIENTS_COLUMNS + " FROM " + Table.CLIENTS + " ORDER BY fullname";

        SQLiteDatabase db = getReadableDatabase();
        Log.i(TAG, sql);
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
                    " ORDER BY work_date";
        }
        else
        {
            sql = "SELECT " + PROJECTS_COLUMNS + " FROM " + Table.PROJECTS +
                    " WHERE work_date > ? AND work_date < ?" +
                    " ORDER BY work_date";

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
        Log.i(TAG, sql+", using "+dateFrom);
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
        Log.i(TAG, sql);
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
        String sqlProj = "SELECT " + PROJECTS_COLUMNS + " FROM " + Table.PROJECTS
                + " WHERE client_id = ? ORDER BY work_date";

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
        Log.i(TAG, sqlProj+", using "+args[0]);
        Cursor c_proj = db.rawQuery(sqlProj, args);
        while (c_proj.moveToNext())
        {
            Project proj = createProjectFromCursor(c_proj);
            projects.add(proj);
        }
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
        proj.setStatus(c.getString(4));
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
        Log.i(TAG, sql+", using "+clientId);
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
        Log.i(TAG, sql+", using "+workId);
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
     */
    public void createClient(Client client)
    {
        new UpdateThread(client).start();
    }

    /**
     * Update client in a separate thread.
     *
     * @param client existing client with new data
     */
    public void updateClient(Client client)
    {
        new UpdateThread(client).start();
    }

    /**
     * Delete client in a separate thread.
     *
     * @param id id of {@code Client}
     */
    public void deleteClient(String id)
    {
        new DeleteThread(Table.CLIENTS, id).start();
    }

    /**
     * Insert new project in a separate thread.
     *
     * @param project project to be inserted
     */
    public void createProject(Project project)
    {
        new UpdateThread(project).start();
    }

    /**
     * Delete work in a separate thread.
     *
     * @param id id of {@code Client}
     */
    public void deleteWork(String id)
    {
        new DeleteThread(Table.PROJECTS, id).start();
    }

    /**
     * Update work in a separate thread.
     *
     * @param work existing work with new data
     * @param picture update a reference in {@code Picture} table.
     */
    public void updateWork(Project work, Picture picture)
    {
        new UpdateThread(work, picture).start();
    }

    /**
     * Insert new picture in a separate thread.
     *
     * @param picture picture to be inserted
     */
    public void createPicture(Picture picture)
    {
        new UpdateThread(picture).start();
    }

    /**
     * Delete picture in a separate thread.
     *
     * @param id id of {@link Picture}
     */
    public void deletePicture(String id)
    {
        new DeleteThread(Table.PICTURES, id).start();
    }

    /**
     * Update picture in a separate thread.
     *
     * @param picture existing picture with new data
     */
    public void updatePicture(Picture picture)
    {
        new UpdateThread(picture).start();
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
        Log.i(TAG, sql+", using "+pictureId);
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
     */
    public Picture findPictureByWorkId(String workId)
    {
        Picture picture = null;
        String sql = "SELECT " + PICTURES_COLUMNS + " FROM " + Table.PICTURES +
                " WHERE " + PICTURES_COL_WORK_ID + " = ?";

        SQLiteDatabase db = getReadableDatabase();
        Log.i(TAG, sql+", using "+workId);
        String[] args = new String[] { workId };
        Cursor c = db.rawQuery(sql, args);

        while (c.moveToNext())
        {
            if (picture != null)
            {
                throw new SQLiteDatabaseCorruptException("Too many pictures for work id "+workId);
            }
            picture = createPictureFromCursor(c);
            break; // FIXME workaround of "Too many pictures for work id "
        }

        c.close();
        return picture;
    }

    /**
     * SQL INSERT or UPDATE
     */
    private class UpdateThread extends Thread
    {
        private Client _client;
        private Project _work;
        private Picture _picture;

        UpdateThread(Client client)
        {
            super();
            _client = client;
        }

        UpdateThread(Project work)
        {
            super();
            _work = work;
        }

        UpdateThread(Picture picture)
        {
            super();
            _picture = picture;
        }

        UpdateThread(Project work, Picture picture)
        {
            super();
            _work = work;
            _picture = picture;
        }

        @Override
        public void run()
        {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            String sql;
            Object[] args;
            if (_client != null)
            {
                sql = "INSERT OR REPLACE INTO " + Table.CLIENTS + "(" + CLIENTS_COLUMNS +
                        ") VALUES (" + CLIENTS_VALUE_PLACEHOLDERS + ")";
                args = new Object[CLIENTS_NCOLS];
                args[0] = _client.getId();
                args[1] = _client.getName();
                args[2] = _client.getPhone();
                args[3] = _client.getEmail();
                args[4] = _client.getSocial();
                Log.i(TAG, String.format("UpdateThread: %s, using %s, %s, %s, %s, %s", sql,
                        args[0], args[1], args[2], args[3], args[4]));
                getWritableDatabase().execSQL(sql, args);
            }

            if (_work != null)
            {
                sql = "INSERT OR REPLACE INTO " + Table.PROJECTS + "(" + PROJECTS_COLUMNS +
                        ") VALUES (" + PROJECTS_VALUE_PLACEHOLDERS + ")";
                args = new Object[PROJECTS_NCOLS];
                args[0] = _work.getId();
                args[1] = _work.getClientId();
                args[2] = _work.getName();
                args[3] = _work.getDate();
                args[4] = _work.getStatus();
                args[5] = _work.getPrice();
                args[6] = _work.getDesign();
                Log.i(TAG, String.format("UpdateThread: %s, using %s, %s, %s, %s, %s, %s, %s", sql,
                        args[0], args[1], args[2], new Date(_work.getDate()).toString(),
                        args[4], args[5], args[6]));
                getWritableDatabase().execSQL(sql, args);
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
                    sql = "INSERT OR REPLACE INTO " + Table.PICTURES + "(" + PICTURES_COLUMNS +
                            ") VALUES (" + PICTURES_VALUE_PLACEHOLDERS + ")";
                    args = new Object[PICTURES_NCOLS];
                    args[0] = _picture.getId();
                    args[1] = _picture.getWorkId();
                    args[2] = photo;
                    Log.i(TAG, String.format("UpdateThread: %s, using %s, %s, %s", sql,
                            args[0], args[1], args[2]));
                    getWritableDatabase().execSQL(sql, args);
                }
            }
        }

        private void clearResultPictures(String workId)
        {
            String sql = "UPDATE " + Table.PICTURES +
                    " SET " + PICTURES_COL_WORK_ID + " = NULL" +
                    " WHERE " + PICTURES_COL_WORK_ID + " = ?";
            Object[] args = new Object[1];
            args[0] = workId;
            Log.i(TAG, String.format("clearResultPictures: %s, using %s", sql, args[0]));

            getWritableDatabase().execSQL(sql, args);
        }

        private void deleteResultPictures(String workId)
        {
            String sql = "DELETE FROM " + Table.PICTURES +
                    " WHERE " + PICTURES_COL_WORK_ID + " = ?";
            Object[] args = new Object[1];
            args[0] = workId;
            Log.i(TAG, String.format("deleteResultPictures: %s, using %s", sql, args[0]));

            getWritableDatabase().execSQL(sql, args);
        }
    }

/*
    private class ClearPictureWorkRefThread extends Thread
    {
        private String _workId;

        ClearPictureWorkRefThread(String workId)
        {
            super();
            _workId = workId;
        }

        @Override
        public void run()
        {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            String sql = "UPDATE " + Table.PICTURES +
                    " SET " + PICTURES_COL_WORK_ID + " = NULL" +
                    " WHERE " + PICTURES_COL_WORK_ID + " = ?";
            Object[] args = new Object[1];
            args[0] = _workId;
            Log.i(TAG, String.format("ClearWorkRefThread: %s, using %s", sql, args[0]));

            getWritableDatabase().execSQL(sql, args);
        }
    }
*/

    /**
     * SQL DELETE
     */
    private class DeleteThread extends Thread
    {
        private final Table _table;
        private final String _id;

        DeleteThread(Table table, String id)
        {
            super();
            _table = table;
            _id = id;
        }

        @Override
        public void run()
        {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

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

            Log.i(TAG, sql+", using "+_id);
            getWritableDatabase().execSQL(sql, args);
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

/*
        public ArrayList<Client> selectClients()
        {
            ArrayList<Client> result = new ArrayList<>();
            String sql = "SELECT " + CLIENTS_COLUMNS + " FROM " + Table.CLIENTS;
            String sqlProj = "SELECT " + PROJECTS_COLUMNS + " FROM " + Table.PROJECTS
                    + " WHERE client_id = ?";

            SQLiteDatabase db = getReadableDatabase();
            Log.i(TAG, sql);
            Cursor c = db.rawQuery(sql, null);
            String[] args = new String[1];

            while (c.moveToNext())
            {
                Client client = new Client();
                client.setId(c.getString(0));
                client.setName(c.getString(1));
                client.setPhone(c.getString(2));
                client.setEmail(c.getString(3));
                client.setSocial(c.getString(4));

                // Fill projects

                List<Project> projects = new ArrayList<>();
                args[0] = client.getId();
                Log.i(TAG, sqlProj+", using "+args[0]);
                Cursor c_proj = db.rawQuery(sqlProj, args);
                while (c_proj.moveToNext())
                {
                    Project proj = new Project();
                    proj.setId(c.getString(0));
                    proj.setClientId(c.getString(1));
                    proj.setName(c.getString(2));
                    proj.setDate(c.getString(3));
                    proj.setStatus(c.getString(4));
                    projects.add(proj);
                }
                client.setProjects(projects);

                result.add(client);
            }

            c.close();
            return result;
        }
*/
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
