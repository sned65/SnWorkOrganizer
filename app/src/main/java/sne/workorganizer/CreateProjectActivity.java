package sne.workorganizer;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.CursorAdapter;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Date;

import sne.workorganizer.db.DatabaseHelper;
import sne.workorganizer.db.Project;
import sne.workorganizer.util.Mix;

/**
 * Create new or Update existing project.
 */
public class CreateProjectActivity extends AppCompatActivity
{
    private static final String TAG = CreateProjectActivity.class.getSimpleName();
    public static final String EXTRA_DATE = "work_date";
    private static final int RC_OPEN = 123;

    private TimePicker _timePicker;
    private AutoCompleteTextView _selectClientView;
    private TextInputEditText _titleView;
    private TextInputEditText _priceView;
    private TextView _designView;
    private Uri _designUri;

    private Cursor _currentCursor;
    private AsyncTask _loadClientsTask;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_project);

        initActionMode();

        initWorkDate();

        _timePicker = (TimePicker) findViewById(R.id.work_time);
        _timePicker.setIs24HourView(true);

        initClientSelector();

        _titleView = (TextInputEditText) findViewById(R.id.work_title);
        _titleView.setInputType(Mix.getInputTypeForNoSuggestsInput());

        _priceView = (TextInputEditText) findViewById(R.id.work_price);
        _designView = (TextView) findViewById(R.id.work_design);
    }

    private void initActionMode()
    {
        startActionMode(new ActionMode.Callback()
        {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu)
            {
                getMenuInflater().inflate(R.menu.menu_editor_actions, menu);
                //mode.setTitle("New Client");
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu)
            {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item)
            {
                switch (item.getItemId())
                {
                case R.id.btn_cancel:
                    cancel();
                    return true;
                case R.id.btn_save:
                    save();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode)
            {
                cancel();
            }
        });
    }

    private void initWorkDate()
    {
        long msec = getIntent().getLongExtra(EXTRA_DATE, -1);
        Date wd;
        if (msec < 0)
        {
            wd = new Date();
        }
        else
        {
            wd = new Date(msec);
        }
        String workDate = SimpleDateFormat.getDateInstance().format(wd);
        //SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
        //String workDate = sdf.format(wd);

        TextView dateView = (TextView) findViewById(R.id.work_date);
        dateView.setText(workDate);
    }

    private void initClientSelector()
    {
        _selectClientView = (AutoCompleteTextView) findViewById(R.id.select_client);
        // TODO
        SimpleCursorAdapter adapter =
                new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2,
                        null, new String[] {
                        DatabaseHelper.CLIENTS_COL_FULLNAME, "_id" },
                        new int[] { android.R.id.text1, android.R.id.text2 },
                        0);
        adapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter()
        {
            @Override
            public CharSequence convertToString(Cursor cur)
            {
                int index = cur.getColumnIndex(DatabaseHelper.CLIENTS_COL_FULLNAME);
                return cur.getString(index);
            }
        });

        adapter.setFilterQueryProvider(new FilterQueryProvider()
        {
            @Override
            public Cursor runQuery(CharSequence str)
            {
                if (str != null)
                {
                    _loadClientsTask = new LoadCursorTask(str.toString()).execute();
                }
                return _currentCursor;
            }
        });

        _selectClientView.setAdapter(adapter);

        // FIXME move to callback
//        if (_currentCursor == null)
//        {
//            _loadClientsTask = new LoadCursorTask(null).execute();
//        }
    }

    @Override
    public void onDestroy()
    {
        cancelClientTask();
        closeClientCursor();

        super.onDestroy();
    }

    private void cancelClientTask()
    {
        if (_loadClientsTask != null)
        {
            _loadClientsTask.cancel(false);
        }
    }

    private void closeClientCursor()
    {
        ((CursorAdapter) _selectClientView.getAdapter()).getCursor().close();
    }

    public void onSelectDesign(View view)
    {
        Intent i = new Intent().setType("image/*");
        i.setAction(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(i, RC_OPEN);
   }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData)
    {
        Log.i(TAG, "onActivityResult("+requestCode+", "+requestCode+") called");
        if (resultCode != Activity.RESULT_OK) return;
        if (resultData == null) return;

        Uri uri = resultData.getData();
        Log.i(TAG, uri.toString());

        if (requestCode == RC_OPEN)
        {
            Cursor c =
                    getContentResolver().query(uri, null, null,
                            null, null);
            String displayName = uri.toString();
            if (c != null && c.moveToFirst())
            {
                int displayNameColumn =
                        c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (displayNameColumn >= 0)
                {
                    displayName = c.getString(displayNameColumn);
                    Log.i(TAG, "Display name: " + displayName);
                }
//                int sizeColumn = c.getColumnIndex(OpenableColumns.SIZE);
//                if (sizeColumn < 0 || c.isNull(sizeColumn))
//                {
//                    Log.i(TAG, "Size not available");
//                }
//                else
//                {
//                    Log.i(TAG, String.format("Size: %d",
//                            c.getInt(sizeColumn)));
//                }

                _designUri = uri;
                _designView.setText(displayName);
            }
            else
            {
                Log.i(TAG, "?No metadata available for "+uri);
            }
        }
    }

    private void cancel()
    {
        Intent result = new Intent();
        setResult(RESULT_CANCELED, result);
        finish();
    }

    private void save()
    {
        // TODO save project
        String text = _selectClientView.getText().toString();
        int selection = _selectClientView.getListSelection();
        Log.i(TAG, "save() text = "+text+"; selection = "+selection);
        Project project = new Project();

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        //db.createProject(project);

        Intent result = new Intent();
        setResult(RESULT_OK, result);
        finish();
    }

    private class LoadCursorTask extends BaseTask<Void>
    {
        private String _pattern;

        public LoadCursorTask(String pattern)
        {
            _pattern = pattern;
        }

        @Override
        protected Cursor doInBackground(Void... params)
        {
            return doQuery(_pattern);
        }
    }

    abstract private class BaseTask<T> extends AsyncTask<T, Void, Cursor>
    {
        @Override
        public void onPostExecute(Cursor result)
        {
            ((CursorAdapter) _selectClientView.getAdapter()).changeCursor(result);
            _loadClientsTask = null;
        }

        protected Cursor doQuery(String startsWith)
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

            DatabaseHelper db = DatabaseHelper.getInstance(CreateProjectActivity.this);
            // FIXME move to DatabaseHelper
            String table = DatabaseHelper.Table.CLIENTS.toString();
            String[] columns = new String[] {
                    DatabaseHelper.CLIENTS_PK+" AS _id",
                    DatabaseHelper.CLIENTS_COL_FULLNAME
            };
            String orderBy = DatabaseHelper.CLIENTS_COL_FULLNAME;
            Cursor result =
                    db.getReadableDatabase()
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
    }
}
