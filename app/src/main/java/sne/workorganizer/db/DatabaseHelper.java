package sne.workorganizer.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Process;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton class for database manipulations.
 */
public class DatabaseHelper extends SQLiteOpenHelper
{
    private static final String TAG = DatabaseHelper.class.getName();

    private static final String DATABASE_NAME = "SnWorkOrganizer.db";
    private static final int SCHEMA_VERSION = 1;

    private static final String CLIENTS_PK = "client_id";
    private static final int CLIENTS_NCOLS = 5;
    private static final String CLIENTS_TYPED_COLUMNS =
            "client_id TEXT PRIMARY KEY, fullname TEXT, phone TEXT, email TEXT, social TEXT";
    private static final String CLIENTS_COLUMNS =
            "client_id, fullname, phone, email, social";
    private static final String CLIENTS_VALUE_PLACEHOLDERS =
            "?, ?, ?, ?, ?";

    private static final int PROJECTS_NCOLS = 5;
    private static final String PROJECTS_PK = "proj_id";
    private static final String PROJECTS_TYPED_COLUMNS =
            "proj_id TEXT PRIMARY KEY, client_id TEXT, proj_name TEXT, work_date TEXT, status TEXT," +
            " FOREIGN KEY(client_id) REFERENCES clients(client_id) ON DELETE CASCADE";
    private static final String PROJECTS_COLUMNS =
            "proj_id, client_id, proj_name, work_date, status";
    private static final String PROJECTS_VALUE_PLACEHOLDERS =
            "?, ?, ?, ?, ?";

    private static DatabaseHelper _instance;

    private ArrayList<Client> _clients;
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

    public interface DbSelectCallback
    {
        void onSelectFinished(ArrayList<Client> records);
    }

    private DatabaseHelper(Context ctx)
    {
        super(ctx, DATABASE_NAME, null, SCHEMA_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE "+ Table.CLIENTS + "(" + CLIENTS_TYPED_COLUMNS + ");");
        db.execSQL("CREATE TABLE "+ Table.PROJECTS + "(" + PROJECTS_TYPED_COLUMNS + ");");
        db.execSQL("CREATE INDEX idx_projects_client ON projects(client_id)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        throw new RuntimeException("No upgrade available");
    }

    /**
     * List of DB tables.
     */
    public enum Table
    {
        CLIENTS, PROJECTS;

        @Override
        public String toString()
        {
            return name().toLowerCase();
        }
    }

    public void invalidateCache()
    {
        _clients = null;
    }

    private ArrayList<Client> select()
    {
        ArrayList<Client> result = new ArrayList<>();
        String sql = "SELECT " + CLIENTS_COLUMNS + " FROM " + Table.CLIENTS + " ORDER BY fullname";
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

    /**
     * Finds all clients synchronously.
     *
     * @return list of {@code Client}s
     */
    public List<Client> findAllClients()
    {
        //Log.i(TAG, "findAllClients() called");
        if (_clients == null)
        {
            //Log.i(TAG, "findAllClients() SELECT");
            _clients = select();
        }
        return _clients;
    }

    /**
     * Finds all clients asynchronously.
     * Calls {@code callback} when the query is finished.
     *
     * @param callback
     */
    public void findAllClients(DbSelectCallback callback)
    {
        new SelectTask(callback).execute();
    }

    public int getInsertPosition()
    {
        return INSERT_POSITION;
    }

    /**
     * Insert new client.
     *
     * @param client client to be inserted
     */
    public void createClient(Client client)
    {
        _clients.add(INSERT_POSITION, client);
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
        for (int i = 0; i < _clients.size(); ++i)
        {
            Client c = _clients.get(i);
            if (client.getId().equals(c.getId()))
            {
                _clients.set(i, client);
                new UpdateThread(client).start();
                return i;
            }
        }
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
        for (int i = 0; i < _clients.size(); ++i)
        {
            Client c = _clients.get(i);
            if (id.equals(c.getId()))
            {
                _clients.remove(i);
                new DeleteThread(Table.CLIENTS, id).start();
                return i;
            }
        }
        return -1;
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
                Log.i(TAG, String.format("UpdateThread: %s, using %s, %s, %s, %s, %s", sql,
                        args[0], args[1], args[2], args[3], args[4]));
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

            Log.i(TAG, sql+", using "+_id);
            getWritableDatabase().execSQL(sql, args);
        }
    }

    private class SelectTask extends AsyncTask<Void, Void, ArrayList<Client>>
    {
        private DbSelectCallback _callback = null;

        SelectTask(DbSelectCallback callback)
        {
            super();
            _callback = callback;
        }

        @Override
        protected ArrayList<Client> doInBackground(Void... params)
        {
            return select();
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
        public ArrayList<Client> select()
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
}
