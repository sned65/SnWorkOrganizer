package sne.workorganizer.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Process;
import android.util.Log;

/**
 * Singleton class for database manipulations.
 */
public class DatabaseHelper extends SQLiteOpenHelper
{
    private static final String TAG = DatabaseHelper.class.getName();

    private static final String DATABASE_NAME = "SnWorkOrganizer.db";
    private static final int SCHEMA_VERSION = 1;

    private static final int CLIENTS_NCOLS = 4;
    private static final String CLIENTS_TYPED_COLUMNS =
            "client_id TEXT PRIMARY KEY, fullname TEXT, phone TEXT, email TEXT";
    private static final String CLIENTS_COLUMNS =
            "client_id, fullname, phone, email";
    private static final String CLIENTS_VALUE_PLACEHOLDERS =
            "?, ?, ?, ?";

    private static final int PROJECTS_NCOLS = 5;
    private static final String PROJECTS_TYPED_COLUMNS =
            "proj_id TEXT PRIMARY KEY, client_id TEXT, proj_name TEXT, work_date TEXT, status TEXT," +
            " FOREIGN KEY(client_id) REFERENCES clients(client_id) ON DELETE CASCADE";
    private static final String PROJECTS_COLUMNS =
            "proj_id, client_id, proj_name, work_date, status";
    private static final String PROJECTS_VALUE_PLACEHOLDERS =
            "?, ?, ?, ?, ?";

    private static DatabaseHelper _instance;

    public synchronized static DatabaseHelper getInstance(Context ctx)
    {
        if (_instance == null)
        {
            _instance = new DatabaseHelper(ctx.getApplicationContext());
        }
        return _instance;
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

    /**
     * SQL INSERT or UPDATE
     */
    private class UpdateThread extends Thread
    {
        private Table _table;
        private Object _row;

        UpdateThread(Client row)
        {
            super();
            _table = Table.CLIENTS;
            _row = row;
        }

        UpdateThread(Project row)
        {
            super();
            _table = Table.PROJECTS;
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
                Log.i(TAG, String.format("UpdateThread: %s, using %s, %s, %s, %s", sql,
                        args[0], args[1], args[2], args[3]));
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

            String sql = "DELETE FROM " + _table + " WHERE id = ?";
            Object[] args = { _id };
            getWritableDatabase().execSQL(sql, args);
        }
    }
}
