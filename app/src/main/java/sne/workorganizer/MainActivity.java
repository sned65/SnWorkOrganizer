package sne.workorganizer;


import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import sne.workorganizer.db.DatabaseHelper;
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
public class MainActivity extends AppCompatActivity implements WorkListMaster
{
    private static final String TAG = MainActivity.class.getName();
    private static final int RC_PERMISSIONS = 2;
    private static final String ARG_DATE_FROM = "arg_date_from";
    private static final String ARG_DATE_TO = "arg_date_to";

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean _twoPane;

    private RecyclerView _workListView;

    private Button _btnDateFrom;
    private Button _btnDateTo;

    private long _dateFrom;
    private long _dateTo;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreate(" + savedInstanceState + ") called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        _workListView = (RecyclerView) findViewById(R.id.work_list);
        WorkListViewAdapter adapter = new WorkListViewAdapter(this);
        _workListView.setAdapter(adapter);

        _btnDateFrom = (Button) findViewById(R.id.btn_date_from);
        _btnDateTo = (Button) findViewById(R.id.btn_date_to);

        Date today = Mix.truncateDate(new Date());
        _dateFrom = today.getTime();

        Date plusMonth = Mix.plusMonth(today);
        _dateTo = plusMonth.getTime();

        ImageButton btnSearch = (ImageButton) findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                search();
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
            _twoPane = true;
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
        state.putLong(ARG_DATE_FROM, _dateFrom);
        state.putLong(ARG_DATE_TO, _dateTo);
        //Log.i(TAG, "onSaveInstanceState() stored current date");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        Log.i(TAG, "onRestoreInstanceState() called");
        super.onRestoreInstanceState(savedInstanceState);
        _dateFrom = savedInstanceState.getLong(ARG_DATE_FROM, 0);
        _dateTo = savedInstanceState.getLong(ARG_DATE_TO, 0);
        setupFilterButtons();
        search();
    }

    private void setupFilterButtons()
    {
        final int year_from = Mix.getDateField(_dateFrom, Calendar.YEAR);
        final int month_from = Mix.getDateField(_dateFrom, Calendar.MONTH);
        final int day_from = Mix.getDateField(_dateFrom, Calendar.DAY_OF_MONTH);
        _btnDateFrom.setText(SimpleDateFormat.getDateInstance().format(new Date(_dateFrom)));
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
                        _dateFrom = d.getTime();
                        _btnDateFrom.setText(SimpleDateFormat.getDateInstance().format(d));
                    }
                }, year_from, month_from, day_from).show();
            }
        });

        final int year_to = Mix.getDateField(_dateTo, Calendar.YEAR);
        final int month_to = Mix.getDateField(_dateTo, Calendar.MONTH);
        final int day_to = Mix.getDateField(_dateTo, Calendar.DAY_OF_MONTH);
        _btnDateTo.setText(SimpleDateFormat.getDateInstance().format(new Date(_dateTo)));
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
                        _dateTo = d.getTime();
                        _btnDateTo.setText(SimpleDateFormat.getDateInstance().format(d));
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
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
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
        case R.id.menu_settings:
        {
//            Intent settings = new Intent(this, SettingsActivity.class);
//            startActivity(settings);
            return true;
        }

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

    private void showProgressBar(boolean flag)
    {
        View progressBar = findViewById(R.id.progressBar);
        View workListView = findViewById(R.id.work_list);
        if (flag)
        {
            progressBar.setVisibility(View.VISIBLE);
            workListView.setVisibility(View.GONE);
        }
        else
        {
            progressBar.setVisibility(View.GONE);
            workListView.setVisibility(View.VISIBLE);
        }
    }

    private void search()
    {
        Date dateFrom = new Date(_dateFrom);
        Date dateTo = new Date(_dateTo);
        Log.i(TAG, "search() from "+dateFrom+" to "+dateTo);

        DatabaseHelper db = DatabaseHelper.getInstance(this);
        db.findAllProjects(new DatabaseHelper.DbSelectProjectsCallback()
        {
            @Override
            public void onSelectFinished(ArrayList<Project> records)
            {
                onSearchFinished(records);
            }
        }, dateFrom, dateTo);
    }

    private void onSearchFinished(ArrayList<Project> records)
    {
        WorkListViewAdapter adapter = (WorkListViewAdapter) _workListView.getAdapter();
        adapter.setWorks(records);
        adapter.notifyDataSetChanged();
        showProgressBar(false);
    }

    @Override
    public boolean isTwoPane()
    {
        return _twoPane;
    }

    @Override
    public void removeWork(int position)
    {
        WorkListViewAdapter adapter = (WorkListViewAdapter) _workListView.getAdapter();
        adapter.removeWork(position);
        adapter.notifyItemRemoved(position);

    }

    @Override
    public void updateWork(Project work, int position)
    {
        WorkListViewAdapter adapter = (WorkListViewAdapter) _workListView.getAdapter();
        adapter.updateWork(work, position);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void removeWorkEditFragment()
    {
        if (_twoPane)
        {
            Fragment frg = getSupportFragmentManager().findFragmentByTag(WoConstants.FRG_WORK_EDIT);
            getSupportFragmentManager().beginTransaction()
                    .remove(frg)
                    .commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data)
    {
        //super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "requestCode = "+(requestCode == WoConstants.RC_CREATE_WORK ? "CREATE_PROJECT" : requestCode)
                +", resultCode = "+(resultCode == RESULT_OK ? "OK" : (resultCode == RESULT_CANCELED ? "CANCELLED" : resultCode)));

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
            Log.i(TAG, "onActivityResult() uri = "+uri.toString());
            String path = FileUtils.getPath(this, uri);

            EditWorkFragment frg = (EditWorkFragment) getSupportFragmentManager().findFragmentByTag(WoConstants.FRG_WORK_EDIT);
            frg.changeDesign(path);
        }

        else if (resultCode == Activity.RESULT_OK && requestCode == WoConstants.RC_OPEN_RESULT_DOCUMENT)
        {
            Uri uri = data.getData();
            Log.i(TAG, "onActivityResult() uri = "+uri.toString());
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

    /////////////////////////////////////////////////////////////////////////
    private class WorkListViewAdapter
            extends RecyclerView.Adapter<WorkListViewAdapter.ViewHolder>
    {
        private Activity _activity;
        private List<Project> _works = new ArrayList<>();

        WorkListViewAdapter(Activity activity)
        {
            _activity = activity;
        }

        public void setWorks(ArrayList<Project> works)
        {
            _works = works;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.journal_item_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position)
        {
            if (_works.isEmpty())
            {
                holder.setProject(Project.DUMMY);
                return;
            }

            holder.setProject(_works.get(position));

            holder._itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (_twoPane)
                    {
                        Bundle arguments = new Bundle();
                        arguments.putParcelable(WoConstants.ARG_WORK, holder.getProject());
                        WorkDetailFragment fragment = new WorkDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.work_detail_container, fragment, WoConstants.FRG_DETAILS)
                                .commit();
                    }
                    else
                    {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, WorkDetailActivity.class);
                        intent.putExtra(WoConstants.ARG_WORK, holder.getProject());

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount()
        {
            if (_works == null || _works.isEmpty()) return 1;
            return _works.size();
        }

        public void updateWork(Project work, int position)
        {
            Log.i(TAG, "updateWork("+work+", "+position+") called");
            Project old = _works.get(position);
            boolean inRange = work.getDate() >= _dateFrom && work.getDate() < _dateTo + 24*60*60*1000;
            if (inRange)
            {
                Log.i(TAG, "updateWork() new date is in the filter range");
                _works.set(position, work);
                if (old.getDate().longValue() != work.getDate().longValue())
                {
                    sortWorksByDateTime();
                }
            }
            else
            {
                Log.i(TAG, "updateWork() new date is not in the filter range");
                removeWork(position);
            }
        }

        private void sortWorksByDateTime()
        {
            Collections.sort(_works, new Comparator<Project>()
            {
                @Override
                public int compare(Project w1, Project w2)
                {
                    return w1.getDate().compareTo(w2.getDate());
                }
            });
        }

        public void removeWork(int position)
        {
            Log.i(TAG, "removeWork("+position+") called");
            _works.remove(position);
        }

        /////////////////////////////////////////////////////////////////////////
        public class ViewHolder extends WorkViewBaseHolder
                implements View.OnLongClickListener
        {
            public ViewHolder(View view)
            {
                super(view);

                _itemView.setOnLongClickListener(this);
            }

            @Override
            public boolean onLongClick(View v)
            {
                Log.i(TAG, "onLongClick() called "+getAdapterPosition()+"; "+_project);
                if (_project == null || Project.isDummy(_project))
                {
                    return true;
                }

                WorkActionsFragment.newInstance(_project, _clientName, getAdapterPosition())
                        .show(_activity.getFragmentManager(), "project_actions");
                return true;
            }
        }
    }
}
