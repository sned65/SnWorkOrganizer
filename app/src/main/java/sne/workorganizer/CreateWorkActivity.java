package sne.workorganizer;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
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
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import sne.workorganizer.db.Client;
import sne.workorganizer.db.ClientLoaderCallbacks;
import sne.workorganizer.db.DatabaseHelper;
import sne.workorganizer.db.IdNamePair;
import sne.workorganizer.db.Project;
import sne.workorganizer.util.FileUtils;
import sne.workorganizer.util.Mix;
import sne.workorganizer.util.PhotoUtils;
import sne.workorganizer.util.WoConstants;

/**
 * Create new or Update existing project.
 */
public class CreateWorkActivity extends AppCompatActivity
{
    private static final String TAG = CreateWorkActivity.class.getSimpleName();
    public static final String EXTRA_DATE = "work_date";

    private TimePicker _timePicker;
    private ClientLoaderCallbacks _clientLoaderCallbacks;
    private TextInputEditText _titleView;
    private TextInputEditText _priceView;

    private TextView _designView;
    private String _designPath;
    private ImageButton _designSelectBtn;

    private Date _workDate;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_work);

        initActionMode();

        initWorkDate();

        _timePicker = (TimePicker) findViewById(R.id.work_time_picker);
        _timePicker.setIs24HourView(true);

        initClientSelector();

        _titleView = (TextInputEditText) findViewById(R.id.work_title);
        _titleView.setInputType(Mix.getInputTypeForNoSuggestsInput());

        _priceView = (TextInputEditText) findViewById(R.id.work_price);
        _designView = (TextView) findViewById(R.id.work_design);

        _designSelectBtn = (ImageButton) findViewById(R.id.btn_select_design);
        _designSelectBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onSelectDesign();
            }
        });
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
        Spinner selectClientView = (Spinner) findViewById(R.id.select_client);
        SimpleCursorAdapter clientsAdapter =
                new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1,
                        null, new String[] {
                        DatabaseHelper.CLIENTS_COL_FULLNAME },
                        new int[] { android.R.id.text1 },
                        0);
        clientsAdapter.setCursorToStringConverter(new SimpleCursorAdapter.CursorToStringConverter()
        {
            @Override
            public CharSequence convertToString(Cursor cur)
            {
                int index = cur.getColumnIndex(DatabaseHelper.CLIENTS_COL_FULLNAME);
                return cur.getString(index);
            }
        });
        selectClientView.setAdapter(clientsAdapter);

        _clientLoaderCallbacks = new ClientLoaderCallbacks(selectClientView);
        getLoaderManager().initLoader(0, null, _clientLoaderCallbacks);
    }

    private void onSelectDesign()
    {
        Intent i = new Intent().setType("image/*");
        i.setAction(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(i, WoConstants.RC_OPEN_DESIGN_DOCUMENT);
   }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData)
    {
        if (BuildConfig.DEBUG)
        {
            Log.i(TAG, "onActivityResult(" + requestCode + ", " + resultCode + ") called");
        }
        if (resultCode != Activity.RESULT_OK) return;
        if (resultData == null) return;

        if (requestCode == WoConstants.RC_OPEN_DESIGN_DOCUMENT)
        {
            Uri uri = resultData.getData();
            if (BuildConfig.DEBUG)
            {
                Log.i(TAG, uri.toString());
            }

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
                    if (BuildConfig.DEBUG)
                    {
                        Log.i(TAG, "Display name: " + displayName);
                    }
                }

                c.close();

                _designPath = FileUtils.getPath(this, uri);
                if (BuildConfig.DEBUG)
                {
                    Log.i(TAG, "onActivityResult() uri (" + uri + ") -> _designPath (" + _designPath + ")");
                }
                _designView.setText(displayName);

                //int thumbnailWidth = getResources().getDimensionPixelSize(android.R.dimen.thumbnail_width);
                //Log.i(TAG, "onActivityResult() thumbnailWidth = "+thumbnailWidth);
                if (!TextUtils.isEmpty(_designPath))
                {
                    Bitmap design = PhotoUtils.getThumbnailBitmap(_designPath, WoConstants.WIDTH_THUMBNAIL);
                    if (design != null)
                    {
                        _designSelectBtn.setImageBitmap(design);
                    }
                }
            }
            else
            {
                Log.w(TAG, "?No metadata available for "+uri);
            }
        }

        else if (requestCode == WoConstants.RC_CREATE_CLIENT)
        {
            Client client = resultData.getParcelableExtra(WoConstants.ARG_CLIENT);
            IdNamePair selectedClient = new IdNamePair(client.getId(), client.getName());
            _clientLoaderCallbacks.setSelectedClient(selectedClient);
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

        boolean cancel = false;
        View focus = null;

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

        DatabaseHelper db = DatabaseHelper.getInstance(this);

        // Create Project structure

        Project project = fillWork();

        db.createProjectWithClient(project, null, new DatabaseHelper.DbCreateWorkCallback()
        {
            @Override
            public void onCreateFinished(Project work)
            {
                Intent result = new Intent();
                result.putExtra(WoConstants.ARG_WORK, work);
                setResult(RESULT_OK, result);
                finish();
            }
        });
    }

    private Project fillWork()
    {
        Project project = new Project();
        project.setClientId(_clientLoaderCallbacks.getSelectedClient().getId());
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

        int hh = Mix.timePickerGetHour(_timePicker);
        int mm = Mix.timePickerGetMinute(_timePicker);

        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(_workDate);
        int yyyy = cal.get(Calendar.YEAR);
        int MM = cal.get(Calendar.MONTH);
        int dd = cal.get(Calendar.DAY_OF_MONTH);
        cal.set(yyyy, MM, dd, hh, mm);
        project.setDate(cal.getTimeInMillis());

        if (_designPath != null)
        {
            project.setDesign(_designPath);
        }

        return project;
    }

    public void onCreateClient(View view)
    {
        Intent cc = new Intent(this, CreateClientActivity.class);
        startActivityForResult(cc, WoConstants.RC_CREATE_CLIENT);
    }
}
