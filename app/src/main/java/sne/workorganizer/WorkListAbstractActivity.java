package sne.workorganizer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import sne.workorganizer.db.Client;
import sne.workorganizer.db.DatabaseHelper;
import sne.workorganizer.db.Project;
import sne.workorganizer.eb.ClientDeleteEvent;
import sne.workorganizer.eb.WorkCreateEvent;
import sne.workorganizer.eb.WorkUpdateEvent;
import sne.workorganizer.util.WoConstants;

/**
 * Common base class for activities showing work list.
 * The layout must contain {@code ProgressBar} with id {@code R.id.progressBar}
 * and {@code RecyclerView} with id {@code R.id.work_list}.
 */
public abstract class WorkListAbstractActivity extends AppCompatActivity implements WorkListMaster
{
    private static final String TAG = WorkListAbstractActivity.class.getName();
    private static final String ARG_WORK_LIST = "arg_work_list";

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean _twoPane = false;

    private RecyclerView _workListView;

    @StringRes private int _noWorkMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreate() called: savedInstanceState == null is "+(savedInstanceState == null));
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null)
        {
            ArrayList<Project> works = savedInstanceState.getParcelableArrayList(ARG_WORK_LIST);
            Log.i(TAG, "onCreate() works.size = "+works.size());
            _workListView = (RecyclerView) findViewById(R.id.work_list);
            ((WorkListViewAdapter) _workListView.getAdapter()).setWorks(works);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state)
    {
        if (BuildConfig.DEBUG)
        {
            Log.i(TAG, "onSaveInstanceState() called");
        }
        super.onSaveInstanceState(state);
        ArrayList<Project> works = ((WorkListViewAdapter) _workListView.getAdapter()).getWorks();
        Log.i(TAG, "onSaveInstanceState() works.size = "+works.size());
        state.putParcelableArrayList(ARG_WORK_LIST, works);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        if (BuildConfig.DEBUG)
        {
            Log.i(TAG, "onRestoreInstanceState() called");
        }
        super.onRestoreInstanceState(savedInstanceState);
        ArrayList<Project> works = savedInstanceState.getParcelableArrayList(ARG_WORK_LIST);
        ((WorkListViewAdapter) _workListView.getAdapter()).setWorks(works);
    }

    @Override
    public void onResume()
    {
        Log.i(TAG, "onResume() called");
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause()
    {
        Log.i(TAG, "onPause() called");
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    protected void setupWorkListView(Long dateFrom, Long dateTo)
    {
        _workListView = (RecyclerView) findViewById(R.id.work_list);
        WorkListViewAdapter adapter = new WorkListViewAdapter(this, dateFrom, dateTo);
        adapter.setNoWorkMessage(_noWorkMessage);
        _workListView.setAdapter(adapter);
    }

    protected void setNoWorkMessage(@StringRes int noWorkMessage)
    {
        _noWorkMessage = noWorkMessage;
    }

    @SuppressWarnings("SameParameterValue")
    protected void setTwoPane(boolean flag)
    {
        _twoPane = flag;
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

    protected void search()
    {
        WorkListViewAdapter adapter = (WorkListViewAdapter) _workListView.getAdapter();
        Date dateFrom = new Date(adapter.getDateFrom());
        Date dateTo = new Date(adapter.getDateTo());
        if (BuildConfig.DEBUG)
        {
            Log.d(TAG, "search() from " + dateFrom + " to " + dateTo);
        }

        DatabaseHelper db = DatabaseHelper.getInstance(this);
//        db.findAllProjects(dateFrom, dateTo);
        db.findAllProjects(new DatabaseHelper.DbSelectProjectsCallback()
        {
            @Override
            public void onSelectFinished(ArrayList<Project> records)
            {
                onSearchFinished(records);
            }
        }, dateFrom, dateTo);
    }

    //@Subscribe(threadMode = ThreadMode.MAIN)
    public void onSearchFinished(ArrayList<Project> records)
    {
        Log.i(TAG, "onSearchFinished("+records.size()+") called");
        WorkListViewAdapter adapter = (WorkListViewAdapter) _workListView.getAdapter();
        adapter.setWorks(records);
        adapter.notifyDataSetChanged();
        showProgressBar(false);
    }

    @Subscribe(sticky=true, threadMode = ThreadMode.MAIN)
    public void onWorkCreated(WorkCreateEvent e)
    {
        EventBus.getDefault().removeStickyEvent(e);
        search();
    }

    @Subscribe(sticky=true, threadMode = ThreadMode.MAIN)
    public void onWorkUpdated(WorkUpdateEvent e)
    {
        Log.i(TAG, "onWorkUpdated() called");
        EventBus.getDefault().removeStickyEvent(e);
        Project modifiedWork = e.getWork();
        Log.i(TAG, "onWorkUpdated() "+modifiedWork);
        WorkListViewAdapter adapter = (WorkListViewAdapter) _workListView.getAdapter();
        List<Project> works = adapter.getWorks();
        Log.i(TAG, "onWorkUpdated() # works = "+works.size());
        for (int position = 0; position < works.size(); ++position)
        {
            if (modifiedWork.getId().equals(works.get(position).getId()))
            {
                Log.i(TAG, "change work at position "+position);
                works.set(position, modifiedWork);
                adapter.notifyItemChanged(position);
                break;
            }
        }
    }

    @Subscribe(sticky=true, threadMode = ThreadMode.MAIN)
    public void onClientDeleteEvent(ClientDeleteEvent e)
    {
        Client client = e.getClient();
        EventBus.getDefault().removeStickyEvent(e);
        WorkListViewAdapter adapter = (WorkListViewAdapter) _workListView.getAdapter();
        ArrayList<Project> records = new ArrayList<>();
        for (Project w : adapter.getWorks())
        {
            if (!w.getClientId().equals(client.getId()))
            {
                records.add(w);
            }
        }

        if (adapter.getWorks().size() != records.size())
        {
            adapter.setWorks(records);
            adapter.notifyDataSetChanged();
            showProgressBar(false);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onClientNameModified(Client client)
    {
        EventBus.getDefault().removeStickyEvent(client);
        WorkListViewAdapter adapter = (WorkListViewAdapter) _workListView.getAdapter();
        boolean modified = false;
        for (Project w : adapter.getWorks())
        {
            if (w.getClientId().equals(client.getId()))
            {
                modified = true;
            }
        }

        if (modified)
        {
            adapter.notifyDataSetChanged();
            showProgressBar(false);
        }
    }

    protected void showProgressBar(boolean flag)
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

    protected long getDateFrom()
    {
        WorkListViewAdapter adapter = (WorkListViewAdapter) _workListView.getAdapter();
        return adapter.getDateFrom();
    }

    protected void setDateFrom(long dateFrom)
    {
        WorkListViewAdapter adapter = (WorkListViewAdapter) _workListView.getAdapter();
        adapter.setDateFrom(dateFrom);
    }

    protected long getDateTo()
    {
        WorkListViewAdapter adapter = (WorkListViewAdapter) _workListView.getAdapter();
        return adapter.getDateTo();
    }

    protected void setDateTo(long dateTo)
    {
        WorkListViewAdapter adapter = (WorkListViewAdapter) _workListView.getAdapter();
        adapter.setDateTo(dateTo);
    }

    /////////////////////////////////////////////////////////////////////////
    private class WorkListViewAdapter
            extends RecyclerView.Adapter<WorkListViewAdapter.ViewHolder>
    {
        private Activity _activity;
        private ArrayList<Project> _works = new ArrayList<>();
        private Long _dateFrom;
        private Long _dateTo;
        @StringRes private int _noWorkMessage;

        WorkListViewAdapter(Activity activity, Long dateFrom, Long dateTo)
        {
            _activity = activity;
            _dateFrom = dateFrom;
            if (dateTo == null)
            {
                _dateTo = _dateFrom;
            }
            else
            {
                _dateTo = dateTo;
            }
        }

        protected void setNoWorkMessage(@StringRes int noWorkMessage)
        {
            _noWorkMessage = noWorkMessage;
        }

        public void setWorks(ArrayList<Project> works)
        {
            _works = works;
        }

        public ArrayList<Project> getWorks()
        {
            return _works;
        }

        @Override
        public WorkListViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.journal_item_content, parent, false);
            return new WorkListViewAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final WorkListViewAdapter.ViewHolder holder, int position)
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
            if (BuildConfig.DEBUG)
            {
                Log.d(TAG, "updateWork(" + work + ", " + position + ") called. Date range: " + new Date(_dateFrom) + " - " + new Date(_dateTo));
            }
            Project old = _works.get(position);
            boolean inRange = work.getDate() >= _dateFrom && work.getDate() < _dateTo + 24*60*60*1000;
            if (inRange)
            {
                if (BuildConfig.DEBUG)
                {
                    Log.d(TAG, "updateWork() new date is in the filter range");
                }
                _works.set(position, work);
                if (old.getDate().longValue() != work.getDate().longValue())
                {
                    sortWorksByDateTime();
                }
            }
            else
            {
                if (BuildConfig.DEBUG)
                {
                    Log.d(TAG, "updateWork() new date is NOT in the filter range");
                }
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
            if (BuildConfig.DEBUG)
            {
                Log.d(TAG, "removeWork(" + position + ") called");
            }
            _works.remove(position);
        }

        public Long getDateFrom()
        {
            return _dateFrom;
        }

        public void setDateFrom(Long dateFrom)
        {
            _dateFrom = dateFrom;
        }

        public Long getDateTo()
        {
            return _dateTo;
        }

        public void setDateTo(Long dateTo)
        {
            _dateTo = dateTo;
        }

        /////////////////////////////////////////////////////////////////////////
        public class ViewHolder extends WorkViewBaseHolder
                implements View.OnLongClickListener
        {
            public ViewHolder(View view)
            {
                super(view, _noWorkMessage);

                _itemView.setOnLongClickListener(this);
            }

            @Override
            public boolean onLongClick(View v)
            {
                if (BuildConfig.DEBUG)
                {
                    Log.d(TAG, "onLongClick() called " + getAdapterPosition() + "; " + _project);
                }
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
