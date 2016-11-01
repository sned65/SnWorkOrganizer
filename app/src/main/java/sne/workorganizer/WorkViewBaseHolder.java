package sne.workorganizer;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import sne.workorganizer.db.Client;
import sne.workorganizer.db.DatabaseHelper;
import sne.workorganizer.db.Project;

/**
 * Created by Nikolay_Smirnov on 01.11.2016.
 */

public class WorkViewBaseHolder extends RecyclerView.ViewHolder
{
    private final static String TAG = WorkViewBaseHolder.class.getName();

    protected final View _itemView;
    protected final TextView _timeView;
    protected final TextView _datetimeView;
    protected final TextView _clientNameView;
    protected final TextView _workTitleView;
    protected Project _project;
    protected String _clientName;

    public WorkViewBaseHolder(View view)
    {
        super(view);
        _itemView = view;
        _timeView = (TextView) view.findViewById(R.id.work_time);
        _datetimeView = (TextView) view.findViewById(R.id.work_date_time);
        _clientNameView = (TextView) view.findViewById(R.id.client_name);
        _workTitleView = (TextView) view.findViewById(R.id.work_title);
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
        if (_timeView != null)
        {
            _timeView.setText(_project.getTimeString());
        }
        if (_datetimeView != null)
        {
            _datetimeView.setText(_project.getDateTimeString2());
        }
        _workTitleView.setText(_project.getName());
        DatabaseHelper db = DatabaseHelper.getInstance(_itemView.getContext());
        Client client = db.findClientById(_project.getClientId());
        _clientName = client.getName();
        _clientNameView.setText(_clientName);
    }
}
