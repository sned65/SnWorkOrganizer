package sne.workorganizer;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CursorAdapter;
import android.widget.FilterQueryProvider;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import sne.workorganizer.db.Client;
import sne.workorganizer.db.DatabaseHelper;
import sne.workorganizer.db.IdNamePair;
import sne.workorganizer.db.Project;
import sne.workorganizer.util.Mix;
import sne.workorganizer.util.WoConstants;

/**
 * Create new or Update existing project.
 */
public class CreateWorkActivity extends AppCompatActivity
{
    private static final String TAG = CreateWorkActivity.class.getSimpleName();
    public static final String EXTRA_DATE = "work_date";
    private static final int RC_OPEN = 123;
    private static final int RC_CREATE_CLIENT = 124;

    private TimePicker _timePicker;
    private AutoCompleteTextView _selectClientView;
    private TextInputEditText _titleView;
    private TextInputEditText _priceView;
    private TextView _designView;
    private Uri _designUri;

    private Cursor _currentCursor;
    private AsyncTask _loadClientsTask;

    private Date _workDate;
    private IdNamePair _selectedClient;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_work);

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
        if (msec < 0)
        {
            _workDate = new Date();
        }
        else
        {
            _workDate = new Date(msec);
        }
        String workDate = SimpleDateFormat.getDateInstance().format(_workDate);
        //SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
        //String workDate = sdf.format(_workDate);

        TextView dateView = (TextView) findViewById(R.id.work_date);
        dateView.setText(workDate);
    }

    private void initClientSelector()
    {
        _selectClientView = (AutoCompleteTextView) findViewById(R.id.select_client);
        // TODO move to DatabaseHandler
        SimpleCursorAdapter adapter =
                new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1,
                        null, new String[] {
                        DatabaseHelper.CLIENTS_COL_FULLNAME },
                        new int[] { android.R.id.text1 },
                        0);
//        SimpleCursorAdapter adapter =
//                new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2,
//                        null, new String[] {
//                        DatabaseHelper.CLIENTS_COL_FULLNAME, "_id" },
//                        new int[] { android.R.id.text1, android.R.id.text2 },
//                        0);
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

        _selectClientView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // Cursor -> Client Id-Name pair
                Cursor c = (Cursor) (parent.getAdapter().getItem(position));
                _selectedClient = new IdNamePair();
                int idx_id = c.getColumnIndex("_id");
                _selectedClient.setId(c.getString(idx_id));
                int idx_name = c.getColumnIndex(DatabaseHelper.CLIENTS_COL_FULLNAME);
                _selectedClient.setName(c.getString(idx_name));
            }
        });

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
        Cursor c = ((CursorAdapter) _selectClientView.getAdapter()).getCursor();
        if (c != null) c.close();
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

        if (requestCode == RC_OPEN)
        {
            Uri uri = resultData.getData();
            Log.i(TAG, uri.toString());

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

        else if (requestCode == RC_CREATE_CLIENT)
        {
            Client client = resultData.getParcelableExtra(WoConstants.ARG_CLIENT);
            _selectedClient = new IdNamePair(client.getId(), client.getName());
            _selectClientView.setText(_selectedClient.getName());
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
        // Validate fields

        String client_name = _selectClientView.getText().toString();
        Log.i(TAG, "save() client_name = "+client_name);

        boolean cancel = false;
        View focus = null;

        if (TextUtils.isEmpty(client_name))
        {
            _selectClientView.setError(getString(R.string.error_field_required));
            focus = _selectClientView;
            cancel = true;
        }

        String title = _titleView.getText().toString();
        if (TextUtils.isEmpty(title))
        {
            _titleView.setError(getString(R.string.error_field_required));
            if (!cancel) focus = _titleView;
            cancel = true;
        }

        if (cancel)
        {
            focus.requestFocus();
            return;
        }

        if (_selectedClient == null || !client_name.equals(_selectedClient.getName()))
        {
            // new client
            Log.i(TAG, "save() new client");
            DatabaseHelper db = DatabaseHelper.getInstance(this);
            Client newClient = new Client();
            newClient.setName(client_name);
            db.createClient(newClient);

            _selectedClient = new IdNamePair();
            _selectedClient.setId(newClient.getId());
            _selectedClient.setName(client_name);
        }

        // Create Project structure

        Project project = fillProject();

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        db.createProject(project);

        Intent result = new Intent();
        result.putExtra(WoConstants.ARG_WORK, project);
        setResult(RESULT_OK, result);
        finish();
    }

    private Project fillProject()
    {
        Project project = new Project();
        project.setClientId(_selectedClient.getId());
        project.setName(_titleView.getText().toString().trim());

        String price_str = _priceView.getText().toString();
        if (!TextUtils.isEmpty(price_str))
        {
            try
            {
                Integer price = Integer.valueOf(price_str);
                project.setPrice(price);
            }
            catch (NumberFormatException e)
            {
                // ignore
            }
        }

        int hh;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            hh = _timePicker.getHour();
        }
        else
        {
            hh = _timePicker.getCurrentHour();
        }

        int mm;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            mm = _timePicker.getMinute();
        }
        else
        {
            mm = _timePicker.getCurrentMinute();
        }


        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(_workDate);
        int yyyy = cal.get(Calendar.YEAR);
        int MM = cal.get(Calendar.MONTH);
        int dd = cal.get(Calendar.DAY_OF_MONTH);
        cal.set(yyyy, MM, dd, hh, mm);
        project.setDate(cal.getTimeInMillis());

        if (_designUri != null)
        {
            project.setDesign(_designUri.toString());
        }

        return project;
    }

    public void onCreateClient(View view)
    {
        Intent cc = new Intent(this, CreateClientActivity.class);
        startActivityForResult(cc, RC_CREATE_CLIENT);
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
            DatabaseHelper db = DatabaseHelper.getInstance(CreateWorkActivity.this);
            return db.getClientCursorByNameStart(startsWith);
        }
    }
}
