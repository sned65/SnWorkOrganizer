package sne.workorganizer;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.CalendarView;
import android.widget.TextView;


import sne.workorganizer.db.Client;
import sne.workorganizer.db.DatabaseHelper;
import sne.workorganizer.db.Project;
import sne.workorganizer.help.AboutAppDialogFragment;
import sne.workorganizer.util.FileUtils;
import sne.workorganizer.util.Mix;
import sne.workorganizer.util.WoConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * An activity representing a list of Works with calendar.
 * This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link WorkDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class WorkListActivity extends AppCompatActivity implements WorkListMaster
{
    private static final String TAG = WorkListActivity.class.getName();
    private static final int RC_PERMISSIONS = 2;

    private Menu _menu;
    private CalendarView _calendarView;

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean _twoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreate("+savedInstanceState+") called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());
        _menu = toolbar.getMenu();

        _calendarView = (CalendarView) findViewById(R.id.calendarView);
        _calendarView.setShowWeekNumber(false);
        long current_date = getIntent().getLongExtra(WoConstants.ARG_CURRENT_DATE, -1);
        if (current_date > 0)
        {
            Log.i(TAG, "onCreate() current date = "+(new Date(current_date)));
            _calendarView.setDate(current_date);
        }
        _calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener()
        {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth)
            {
                //Log.i(TAG, "onSelectedDayChange(" + year + "-" + month + "-" + dayOfMonth + ") called");

                Mix.calendarSetSelection(view, year, month, dayOfMonth);
                resetWorkList();
            }
        });
/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
*/

        if (savedInstanceState == null)
        {
            View workListView = findViewById(R.id.work_list);
            assert workListView != null;
            setupRecyclerView((RecyclerView) workListView);
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
    protected void onSaveInstanceState(Bundle state)
    {
        super.onSaveInstanceState(state);
        state.putLong(WoConstants.ARG_CURRENT_DATE, _calendarView.getDate());
        //Log.i(TAG, "onSaveInstanceState() stored current date");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        Log.i(TAG, "onRestoreInstanceState() called");
        super.onRestoreInstanceState(savedInstanceState);
        long date = savedInstanceState.getLong(WoConstants.ARG_CURRENT_DATE, -1);
        if (date > 0)
        {
            Log.i(TAG, "onRestoreInstanceState() set date "+(new Date(date)));
            _calendarView.setDate(date);

            View workListView = findViewById(R.id.work_list);
            assert workListView != null;
            setupRecyclerView((RecyclerView) workListView);
        }
    }

/*
    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);

        Log.i(TAG, "onPostCreate() _twoPane = "+_twoPane+"; _updateDetailPane = "+_updateDetailPane);
        if (_twoPane && _updateDetailPane >= 0)
        {
            RecyclerView workListView = (RecyclerView) findViewById(R.id.work_list_daily);
            // TODO update detail part, if any
            boolean done = workListView.getChildAt(_updateDetailPane).performClick();
            if (done) Log.i(TAG, "onRestoreInstanceState() click successful");
            else Log.i(TAG, "onRestoreInstanceState() click NOT successful");
        }
    }
*/

    private void resetWorkList()
    {
        RecyclerView workListView = (RecyclerView) findViewById(R.id.work_list);
        setupRecyclerView(workListView);
    }

    @Override
    public void removeWork(int position)
    {
        RecyclerView workListView = (RecyclerView) findViewById(R.id.work_list);
        WorkListViewAdapter adapter = (WorkListViewAdapter) workListView.getAdapter();
        adapter.removeWork(position);
        adapter.notifyItemRemoved(position);
    }

    @Override
    public void updateWork(Project work, int position)
    {
        RecyclerView workListView = (RecyclerView) findViewById(R.id.work_list);
        WorkListViewAdapter adapter = (WorkListViewAdapter) workListView.getAdapter();
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
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_work_list_daily, menu);
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

        case R.id.menu_journal:
        {
            Intent m = new Intent(this, MainActivity.class);
            startActivity(m);
            return true;
        }

        case R.id.menu_create_project:
        {
            Intent createProject = new Intent(this, CreateWorkActivity.class);
            long msec = _calendarView.getDate();
            createProject.putExtra(CreateWorkActivity.EXTRA_DATE, msec);
            startActivityForResult(createProject, WoConstants.RC_CREATE_WORK);
//            Snackbar.make(findViewById(R.id.frameLayout), "Create new project", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show();
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
        //super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "requestCode = "+(requestCode == WoConstants.RC_CREATE_WORK ? "CREATE_PROJECT" : requestCode)
                +", resultCode = "+(resultCode == RESULT_OK ? "OK" : (resultCode == RESULT_CANCELED ? "CANCELLED" : resultCode)));

        if (resultCode == Activity.RESULT_OK && requestCode == WoConstants.RC_CREATE_WORK)
        {
            resetWorkList();
        }

        else if (resultCode == Activity.RESULT_OK && requestCode == WoConstants.RC_EDIT_WORK)
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

/*
    @Override
    public void onResume()
    {
        super.onResume();
        Fragment frg = getSupportFragmentManager().findFragmentByTag(FRG_DETAILS);
        Log.i(TAG, "onResume() FRAGMENT = "+frg);
    }
*/

    private void setupRecyclerView(@NonNull RecyclerView recyclerView)
    {
        Date date = new Date(_calendarView.getDate());
        Log.i(TAG, "setupRecyclerView() for "+date);
        recyclerView.setAdapter(new WorkListViewAdapter(this, date));
    }

    private void showProjectList()
    {
        RecyclerView workListView = (RecyclerView) findViewById(R.id.work_list);
        workListView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public boolean isTwoPane()
    {
        return _twoPane;
    }

    public void resetDetailPane(int position)
    {
        // TODO resetDetailPane
    }

    public class WorkListViewAdapter
            extends RecyclerView.Adapter<WorkListViewAdapter.ViewHolder>
    {
        private Activity _activity;
        private Date _date;
        private List<Project> _projects;

        WorkListViewAdapter(Activity activity, Date date)
        {
            _activity = activity;
            _date = date;
            DatabaseHelper db = DatabaseHelper.getInstance(_activity);
            db.findAllProjects(new DatabaseHelper.DbSelectProjectsCallback()
            {
                @Override
                public void onSelectFinished(ArrayList<Project> records)
                {
                    Log.i(TAG, "onSelectFinished() "+records+", size = "+((records == null) ? "" : records.size()));
                    _projects = records;
                    showProjectList();
                }
            }, _date);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.work_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position)
        {
            holder.setProject(_projects.get(position));

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
            if (_projects == null) return 0;
            return _projects.size();
        }

        public void removeWork(int position)
        {
            Log.i(TAG, "removeWork("+position+") called");
            _projects.remove(position);

            if (_twoPane)
            {
                // TODO clear details
            }
        }

        public int updateWork(Project work, int position)
        {
            Log.i(TAG, "updateWork("+work+", "+position+") called");
            int new_position = -1;
            Project old = _projects.get(position);
            Log.i(TAG, "updateWork() compare dates for "+old+" and "+work);
            Log.i(TAG, "updateWork() compare dates "+(new Date(old.getDate()))+" and "+(new Date(work.getDate())));
            boolean sameDate = Mix.sameDate(old.getDate(), work.getDate());
            if (sameDate)
            {
                Log.i(TAG, "updateWork() the same dates");
                boolean sameTime = Mix.sameTime(old.getDate(), work.getDate());
                _projects.set(position, work);
                if (!sameTime)
                {
                    sortWorksByTime();
                    for (int i = 0; i < _projects.size(); ++i)
                    {
                        Project p = _projects.get(i);
                        if (work.getId().equals(p.getId()))
                        {
                            new_position = i;
                            break;
                        }
                    }
                }
            }
            else
            {
                Log.i(TAG, "updateWork() the different dates");
                removeWork(position);
            }

            return new_position;
        }

        private void sortWorksByTime()
        {
            Collections.sort(_projects, new Comparator<Project>()
            {
                @Override
                public int compare(Project w1, Project w2)
                {
                    return w1.getDate().compareTo(w2.getDate());
                }
            });
        }

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
                if (_project == null)
                {
                    return true;
                }

                WorkActionsFragment.newInstance(_project, _clientName, getAdapterPosition())
                        .show(_activity.getFragmentManager(), "project_actions");
                return true;
            }
        }
/*
        public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnLongClickListener
        {
            public final View _itemView;
            public final TextView _timeView;
            public final TextView _clientNameView;
            public final TextView _workTitleView;
            private Project _project;
            private String _clientName;

            public ViewHolder(View view)
            {
                super(view);
                _itemView = view;
                _timeView = (TextView) view.findViewById(R.id.work_time);
                _clientNameView = (TextView) view.findViewById(R.id.client_name);
                _workTitleView = (TextView) view.findViewById(R.id.work_title);

                _itemView.setOnLongClickListener(this);
            }

            @Override
            public String toString()
            {
                return super.toString() + " '" + _clientNameView.getText() + "'";
            }

            public Project getProject()
            {
                return _project;
            }

            public void setProject(Project project)
            {
                _project = project;
                _timeView.setText(_project.getTimeString());
                _workTitleView.setText(_project.getName());
                DatabaseHelper db = DatabaseHelper.getInstance(_itemView.getContext());
                Client client = db.findClientById(_project.getClientId());
                _clientName = client.getName();
                _clientNameView.setText(_clientName);
            }

            @Override
            public boolean onLongClick(View v)
            {
                Log.i(TAG, "onLongClick() called "+getAdapterPosition()+"; "+_project);
                if (_project == null)
                {
                    return true;
                }

                WorkActionsFragment.newInstance(_project, _clientName, getAdapterPosition())
                        .show(_activity.getFragmentManager(), "project_actions");
                return true;
            }
        }
*/
    }
}
