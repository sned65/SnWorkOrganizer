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
    public enum Table
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

    public static final String CLIENTS_PK = "client_id";
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
    private static final String PROJECTS_TYPED_COLUMNS =
            "proj_id TEXT PRIMARY KEY, client_id TEXT, proj_name TEXT, work_date INTEGER, status TEXT, price NUMERIC, design TEXT," +
            " FOREIGN KEY(client_id) REFERENCES clients(client_id) ON DELETE CASCADE";
    private static final String PROJECTS_COLUMNS =
            "proj_id, client_id, proj_name, work_date, status, price, design";
    private static final String PROJECTS_VALUE_PLACEHOLDERS =
            "?, ?, ?, ?, ?, ?, ?";

    // END OF WORKS TABLE


    // PICTURES TABLE

    private static final int PICTURES_NCOLS = 3;
    private static final String PICTURES_PK = "pict_id";
    private static final String PICTURES_TYPED_COLUMNS =
            "pict_id TEXT PRIMARY KEY, proj_id TEXT, photo TEXT," +
            " FOREIGN KEY(proj_id) REFERENCES projects(proj_id) ON DELETE CASCADE";
    private static final String PICTURES_COLUMNS =
            "pict_id, proj_id, photo";
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

    //private ArrayList<Client> _clients;
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

//    public void invalidateCache()
//    {
//        _clients = null;
//    }

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

    /*
     * Finds all clients synchronously.
     *
     * @return list of {@code Client}s
     * @deprecated Use asynchronous findAllClients(DbSelectCallback callback)
     */
//    @Deprecated
//    public List<Client> findAllClients()
//    {
//        //Log.i(TAG, "findAllClients() called");
//        if (_clients == null)
//        {
//            //Log.i(TAG, "findAllClients() SELECT");
//            _clients = selectClients();
//        }
//        return _clients;
//    }

    private ArrayList<Project> selectProjects(Date date)
    {
        ArrayList<Project> projects = new ArrayList<>();
        String sql = "SELECT " + PROJECTS_COLUMNS + " FROM " + Table.PROJECTS +
                " WHERE work_date > ? AND work_date < ?" +
                " ORDER BY work_date";

        long from = date.getTime();
        long to = from + 24 * 60 * 60 * 1000;
        String[] args = new String[] { String.valueOf(from), String.valueOf(to) };

        SQLiteDatabase db = getReadableDatabase();
        Log.i(TAG, sql+", using "+date);
        Cursor c = db.rawQuery(sql, args);

        while (c.moveToNext())
        {
            Project proj = createProjectFromCursor(c);
            projects.add(proj);
        }

        c.close();
        return projects;
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

    /**
     * Finds all clients asynchronously.
     * Calls {@code callback} when the query is finished.
     *
     * @param callback
     */
    public void findAllClients(DbSelectClientsCallback callback)
    {
        new SelectClientsTask(callback).execute();
    }

    /**
     * Synchronously selects {@code Client} by its id.
     *
     * @param clientId
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
     * Finds all projects for the given date asynchronously.
     * Calls {@code callback} when the query is finished.
     *
     * @param callback
     * @param date
     */
    public void findAllProjects(DbSelectProjectsCallback callback, Date date)
    {
        new SelectProjectsTask(callback, date).execute();
    }

    public int getInsertPosition()
    {
        return INSERT_POSITION;
    }

    /**
     * Insert new client in a separate thread.
     *
     * @param client client to be inserted
     */
    public void createClient(Client client)
    {
        //_clients.add(INSERT_POSITION, client);
        new UpdateThread(client).start();
    }

    /**
     * Update client in a separate thread.
     *
     * @param client
     * @return position in internal client list,
     * or -1 if not found.
     */
    public int updateClient(Client client)
    {
//        for (int i = 0; i < _clients.size(); ++i)
//        {
//            Client c = _clients.get(i);
//            if (client.getId().equals(c.getId()))
//            {
//                _clients.set(i, client);
//                new UpdateThread(client).start();
//                return i;
//            }
//        }
        new UpdateThread(client).start();
        return -1;
    }

    /**
     * Delete client in a separate thread.
     *
     * @param id id of {@code Client}
     * @return position in the original internal client list,
     * or -1 if not found.
     */
    public int deleteClient(String id)
    {
//        for (int i = 0; i < _clients.size(); ++i)
//        {
//            Client c = _clients.get(i);
//            if (id.equals(c.getId()))
//            {
//                _clients.remove(i);
//                new DeleteThread(Table.CLIENTS, id).start();
//                return i;
//            }
//        }
        new DeleteThread(Table.CLIENTS, id).start();
        return -1;
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
     * @return position in the original internal client list,
     * or -1 if not found.
     */
    public int deleteWork(String id)
    {
        new DeleteThread(Table.PROJECTS, id).start();
        return -1;
    }

    /**
     * Update work in a separate thread.
     *
     * @param work
     */
    public void updateWork(Project work)
    {
        new UpdateThread(work).start();
    }

    /**
     * SQL INSERT or UPDATE
     */
    private class UpdateThread extends Thread
    {
        private Object _row;

        UpdateThread(Client row)
        {
            super();
            _row = row;
        }

        UpdateThread(Project row)
        {
            super();
            _row = row;
        }

        @Override
        public void run()
        {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            String sql = null;
            Object[] args = null;
            if (_row instanceof Client)
            {
                sql = "INSERT OR REPLACE INTO " + Table.CLIENTS + "(" + CLIENTS_COLUMNS +
                        ") VALUES (" + CLIENTS_VALUE_PLACEHOLDERS + ")";
                Client client = (Client) _row;
                args = new Object[CLIENTS_NCOLS];
                args[0] = client.getId();
                args[1] = client.getName();
                args[2] = client.getPhone();
                args[3] = client.getEmail();
                args[4] = client.getSocial();
                Log.i(TAG, String.format("UpdateThread: %s, using %s, %s, %s, %s, %s", sql,
                        args[0], args[1], args[2], args[3], args[4]));
            }
            else if (_row instanceof Project)
            {
                sql = "INSERT OR REPLACE INTO " + Table.PROJECTS + "(" + PROJECTS_COLUMNS +
                        ") VALUES (" + PROJECTS_VALUE_PLACEHOLDERS + ")";
                Project project = (Project) _row;
                args = new Object[PROJECTS_NCOLS];
                args[0] = project.getId();
                args[1] = project.getClientId();
                args[2] = project.getName();
                args[3] = project.getDate();
                args[4] = project.getStatus();
                args[5] = project.getPrice();
                args[6] = project.getDesign();
                Log.i(TAG, String.format("UpdateThread: %s, using %s, %s, %s, %s, %s, %s, %s", sql,
                        args[0], args[1], args[2], args[3], args[4], args[5], args[6]));
            }

            getWritableDatabase().execSQL(sql, args);
        }
    }

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

            String sql = null;
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
        private Date _date;

        SelectProjectsTask(DbSelectProjectsCallback callback, Date date)
        {
            super();
            _callback = callback;
            _date = Mix.truncateDate(date);
        }

        @Override
        protected ArrayList<Project> doInBackground(Void... params)
        {
            return selectProjects(_date);
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
}
