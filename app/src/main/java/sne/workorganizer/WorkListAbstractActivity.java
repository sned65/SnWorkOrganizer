package sne.workorganizer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import sne.workorganizer.db.DatabaseHelper;
import sne.workorganizer.db.Project;
import sne.workorganizer.util.WoConstants;

/**
 * Common base class for activities showing work list.
 * The layout must contain {@code ProgressBar} with id {@code R.id.progressBar}
 * and {@code RecyclerView} with id {@code R.id.work_list}.
 */
public abstract class WorkListAbstractActivity extends AppCompatActivity implements WorkListMaster
{
    private static final String TAG = WorkListAbstractActivity.class.getName();

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean _twoPane = false;

    private RecyclerView _workListView;

    private long _dateFrom;
    private long _dateTo;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    protected void setupWorkListView(RecyclerView workListView, Long dateFrom, Long dateTo)
    {
        _dateFrom = dateFrom;
        if (dateTo == null)
        {
            _dateTo = _dateFrom;
        }
        else
        {
            _dateTo = dateTo;
        }
        _workListView = workListView;
        WorkListViewAdapter adapter = new WorkListViewAdapter(this, dateFrom, dateTo);
        _workListView.setAdapter(adapter);
    }

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
        return _dateFrom;
    }

    protected void setDateFrom(long dateFrom)
    {
        _dateFrom = dateFrom;
    }

    protected long getDateTo()
    {
        return _dateTo;
    }

    protected void setDateTo(long dateTo)
    {
        _dateTo = dateTo;
    }

    /////////////////////////////////////////////////////////////////////////
    private class WorkListViewAdapter
            extends RecyclerView.Adapter<WorkListViewAdapter.ViewHolder>
    {
        private Activity _activity;
        private List<Project> _works = new ArrayList<>();
        private Long _dateFrom;
        private Long _dateTo;

        WorkListViewAdapter(Activity activity, Long dateFrom, Long dateTo)
        {
            _activity = activity;
            _dateFrom = dateFrom;
            if (_dateTo == null)
            {
                _dateTo = _dateFrom;
            }
            else
            {
                _dateTo = dateTo;
            }
        }

        public void setWorks(ArrayList<Project> works)
        {
            _works = works;
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
