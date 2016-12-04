package sne.workorganizer;


import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import sne.workorganizer.db.Project;
import sne.workorganizer.help.AboutAppDialogFragment;
import sne.workorganizer.util.FileUtils;
import sne.workorganizer.util.Mix;
import sne.workorganizer.util.WoConstants;

/**
 * An activity representing a list of Works with filter.
 * This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link WorkDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class MainActivity extends WorkListAbstractActivity
{
    private static final String TAG = MainActivity.class.getName();
    private static final int RC_PERMISSIONS = 2;
    private static final String ARG_DATE_FROM = "arg_date_from";
    private static final String ARG_DATE_TO = "arg_date_to";
    private static final int COLOR_SEARCH_WARNING = Color.rgb(0xff, 0xa5, 0x00);

    private Button _btnDateFrom;
    private Button _btnDateTo;
    private ImageButton _btnSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        if (BuildConfig.DEBUG)
        {
            Log.d(TAG, "onCreate(" + savedInstanceState + ") called");
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());
        toolbar.setSubtitle(R.string.journal);

        _btnDateFrom = (Button) findViewById(R.id.btn_date_from);
        _btnDateTo = (Button) findViewById(R.id.btn_date_to);

        Date today = Mix.truncateDate(new Date());
        long dateFrom = today.getTime();

        Date plusMonth = Mix.plusMonth(today);
        long dateTo = plusMonth.getTime();

        setNoWorkMessage(R.string.info_no_works_for_dates);
        setupWorkListView(dateFrom, dateTo);

        _btnSearch = (ImageButton) findViewById(R.id.btn_search);
        _btnSearch.setBackgroundColor(Color.LTGRAY);
        _btnSearch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                search();
                _btnSearch.setBackgroundColor(Color.LTGRAY);
            }
        });

        if (savedInstanceState == null)
        {
            setupFilterButtons();
            showProgressBar(true);
            search();
        }

        if (findViewById(R.id.work_detail_container) != null)
        {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            setTwoPane(true);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            String[] permissions = new String[] {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
            };
            ActivityCompat.requestPermissions(this, permissions, RC_PERMISSIONS);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state)
    {
        super.onSaveInstanceState(state);
        state.putLong(ARG_DATE_FROM, getDateFrom());
        state.putLong(ARG_DATE_TO, getDateTo());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        if (BuildConfig.DEBUG)
        {
            Log.d(TAG, "onRestoreInstanceState() called");
        }
        super.onRestoreInstanceState(savedInstanceState);
        setDateFrom(savedInstanceState.getLong(ARG_DATE_FROM, 0));
        setDateTo(savedInstanceState.getLong(ARG_DATE_TO, 0));
        setupFilterButtons();
        search();
    }

    private void setupFilterButtons()
    {
        final int year_from = Mix.getDateField(getDateFrom(), Calendar.YEAR);
        final int month_from = Mix.getDateField(getDateFrom(), Calendar.MONTH);
        final int day_from = Mix.getDateField(getDateFrom(), Calendar.DAY_OF_MONTH);
        _btnDateFrom.setText(SimpleDateFormat.getDateInstance().format(new Date(getDateFrom())));
        _btnDateFrom.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener()
                {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth)
                    {
                        Calendar c = Calendar.getInstance();
                        c.set(year, month, dayOfMonth);
                        Date d = c.getTime();
                        setDateFrom(d.getTime());
                        String old_date = _btnDateFrom.getText().toString();
                        String new_date = SimpleDateFormat.getDateInstance().format(d);
                        if (!old_date.equals(new_date))
                        {
                            _btnSearch.setBackgroundColor(COLOR_SEARCH_WARNING);
                        }
                        _btnDateFrom.setText(new_date);
                    }
                }, year_from, month_from, day_from).show();
            }
        });

        final int year_to = Mix.getDateField(getDateTo(), Calendar.YEAR);
        final int month_to = Mix.getDateField(getDateTo(), Calendar.MONTH);
        final int day_to = Mix.getDateField(getDateTo(), Calendar.DAY_OF_MONTH);
        _btnDateTo.setText(SimpleDateFormat.getDateInstance().format(new Date(getDateTo())));
        _btnDateTo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener()
                {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth)
                    {
                        Calendar c = Calendar.getInstance();
                        c.set(year, month, dayOfMonth);
                        Date d = c.getTime();
                        setDateTo(d.getTime());
                        String old_date = _btnDateTo.getText().toString();
                        String new_date = SimpleDateFormat.getDateInstance().format(d);
                        if (!old_date.equals(new_date))
                        {
                            _btnSearch.setBackgroundColor(COLOR_SEARCH_WARNING);
                        }
                        _btnDateTo.setText(new_date);
                    }
                }, year_to, month_to, day_to).show();
            }
        });
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        //Log.i(TAG, "onRequestPermissionsResult() called");
        if (requestCode != RC_PERMISSIONS)
        {
            Log.e(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        Mix.checkGrantedPermissions(this, permissions, grantResults);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId())
        {
//        case R.id.menu_settings:
//        {
////            Intent settings = new Intent(this, SettingsActivity.class);
////            startActivity(settings);
//            return true;
//        }

        case R.id.menu_about:
        {
            AboutAppDialogFragment about = new AboutAppDialogFragment();
            Bundle args = new Bundle();
            args.putString(AboutAppDialogFragment.ARG_TITLE, getResources().getString(R.string.app_name));
            about.setArguments(args);
            about.show(getFragmentManager(), "about");
            return true;
        }

        case R.id.menu_calendar:
        {
            Intent cal = new Intent(this, WorkListActivity.class);
            startActivity(cal);
            return true;
        }

        case R.id.menu_clients:
        {
            Intent clientList = new Intent(this, ClientListActivity.class);
            startActivity(clientList);
            return true;
        }

        case R.id.menu_gallery_work:
        {
            Intent gallery = new Intent(this, GalleryActivity.class);
            gallery.putExtra(GalleryActivity.ARG_PICT_TYPE, GalleryActivity.PICT_AFTER);
            startActivity(gallery);
            return true;
        }

        case R.id.menu_gallery_design:
        {
            Intent gallery = new Intent(this, GalleryActivity.class);
            gallery.putExtra(GalleryActivity.ARG_PICT_TYPE, GalleryActivity.PICT_BEFORE);
            startActivity(gallery);
            return true;
        }

        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data)
    {
        if (BuildConfig.DEBUG)
        {
            Log.i(TAG, "requestCode = " + (requestCode == WoConstants.RC_CREATE_WORK ? "CREATE_PROJECT" : requestCode)
                    + ", resultCode = " + (resultCode == RESULT_OK ? "OK" : (resultCode == RESULT_CANCELED ? "CANCELLED" : resultCode)));
        }

        if (resultCode == Activity.RESULT_OK && requestCode == WoConstants.RC_EDIT_WORK)
        {
            Project work = data.getParcelableExtra(WoConstants.ARG_WORK);
            int position = data.getIntExtra(WoConstants.ARG_POSITION, -1);
            updateWork(work, position);

            WorkDetailFragment wdf = (WorkDetailFragment) getSupportFragmentManager().findFragmentByTag(WoConstants.FRG_DETAILS);
            if (wdf != null)
            {
                wdf.setWork(work);
            }
        }

        else if (resultCode == Activity.RESULT_OK && requestCode == WoConstants.RC_OPEN_DESIGN_DOCUMENT)
        {
            Uri uri = data.getData();
            if (BuildConfig.DEBUG)
            {
                Log.d(TAG, "onActivityResult() uri = " + uri.toString());
            }
            String path = FileUtils.getPath(this, uri);

            EditWorkFragment frg = (EditWorkFragment) getSupportFragmentManager().findFragmentByTag(WoConstants.FRG_WORK_EDIT);
            frg.changeDesign(path);
        }

        else if (resultCode == Activity.RESULT_OK && requestCode == WoConstants.RC_OPEN_RESULT_DOCUMENT)
        {
            Uri uri = data.getData();
            if (BuildConfig.DEBUG)
            {
                Log.d(TAG, "onActivityResult() uri = " + uri.toString());
            }
            String path = FileUtils.getPath(this, uri);

            EditWorkFragment frg = (EditWorkFragment) getSupportFragmentManager().findFragmentByTag(WoConstants.FRG_WORK_EDIT);
            frg.setResultPath(path);
            frg.refreshResult();
        }

        else if (requestCode == WoConstants.RC_TAKE_PICTURE)
        {
            EditWorkFragment frg = (EditWorkFragment) getSupportFragmentManager().findFragmentByTag(WoConstants.FRG_WORK_EDIT);
            frg.acceptPhoto(resultCode == Activity.RESULT_OK);
        }
    }
}
