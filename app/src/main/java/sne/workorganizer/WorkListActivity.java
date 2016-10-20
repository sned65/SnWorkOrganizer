package sne.workorganizer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import sne.workorganizer.util.WoConstants;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * An activity representing a list of Works. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link WorkDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class WorkListActivity extends AppCompatActivity
{
    private static final String TAG = WorkListActivity.class.getName();
    private static final int RC_CREATE_WORK = 100;
    public static final int RC_EDIT_WORK = 101;

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
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth)
            {
                Log.i(TAG, "onSelectedDayChange(" + year + "-" + month + "-" + dayOfMonth + ") called");

                // It seems there is a bug in CalendarView implementation for some devices.
                // http://stackoverflow.com/questions/31602849/calendarview-returns-always-current-day-ignoring-what-user-selects
                // In Nexus 10 tablet whatever date is selected, CalendarView returns the current day.
                // And if I keep changing the date, nothing changes - getDate() always returns the current day.
                // The code below is workaround.
                Calendar c = GregorianCalendar.getInstance();
                c.set(year, month, dayOfMonth);
                long ms = c.getTimeInMillis();
                view.setDate(ms);

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

        View workListView = findViewById(R.id.work_list);
        assert workListView != null;
        setupRecyclerView((RecyclerView) workListView);

        if (findViewById(R.id.work_detail_container) != null)
        {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            _twoPane = true;
        }
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
        long date = savedInstanceState.getLong(WoConstants.ARG_CURRENT_DATE, -1);
        if (date > 0)
        {
            _calendarView.setDate(date);
        }
    }

    private void resetWorkList()
    {
        RecyclerView workListView = (RecyclerView) findViewById(R.id.work_list);
        setupRecyclerView(workListView);
    }

    public void removeWork(int position)
    {
        RecyclerView workListView = (RecyclerView) findViewById(R.id.work_list);
        WorkListViewAdapter adapter = (WorkListViewAdapter) workListView.getAdapter();
        adapter.removeWork(position);
        adapter.notifyItemRemoved(position);
    }

    public void updateWork(Project work, int position)
    {
        RecyclerView workListView = (RecyclerView) findViewById(R.id.work_list);
        WorkListViewAdapter adapter = (WorkListViewAdapter) workListView.getAdapter();
        adapter.updateWork(work, position);
        adapter.notifyItemChanged(position);
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

        case R.id.menu_clients:
        {
            Intent clientList = new Intent(this, ClientListActivity.class);
            startActivity(clientList);
            return true;
        }

        case R.id.menu_examples:
        {
            // TODO
            return true;
        }

        case R.id.menu_create_project:
        {
            Intent createProject = new Intent(this, CreateWorkActivity.class);
            long msec = _calendarView.getDate();
            createProject.putExtra(CreateWorkActivity.EXTRA_DATE, msec);
            startActivityForResult(createProject, RC_CREATE_WORK);
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
        Log.i(TAG, "requestCode = "+(requestCode == RC_CREATE_WORK ? "CREATE_PROJECT" : requestCode)
                +", resultCode = "+(resultCode == RESULT_OK ? "OK" : (resultCode == RESULT_CANCELED ? "CANCELLED" : resultCode)));
        if (resultCode != RESULT_OK) return;

        if (requestCode == RC_CREATE_WORK)
        {
            resetWorkList();
        }

        else if (requestCode == RC_EDIT_WORK)
        {
            Project work = data.getParcelableExtra(WoConstants.ARG_WORK);
            int position = data.getIntExtra(WoConstants.ARG_POSITION, -1);
            updateWork(work, position);
        }
    }

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

    public class WorkListViewAdapter
            extends RecyclerView.Adapter<WorkListViewAdapter.ViewHolder>
    {
        private Activity _activity;
        private Date _date;
        private List<Project> _projects;

        //private final List<DummyContent.DummyItem> mValues;

        public WorkListViewAdapter(Activity activity, Date date)
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

//        @Override
//        public void onBindViewHolder(final ViewHolder holder, int position)
//        {
//            holder.bindModel(_projects.get(position));
//        }



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
                                .replace(R.id.work_detail_container, fragment)
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
            _projects.remove(position);
        }

        public void updateWork(Project work, int position)
        {
            _projects.set(position, work);
        }

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
    }
}
