package sne.workorganizer;

import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import sne.workorganizer.db.Client;
import sne.workorganizer.db.DatabaseHelper;
import sne.workorganizer.db.Project;

/**
 * Base class for work list {@code ViewHolder}s.
 */
public class WorkViewBaseHolder extends RecyclerView.ViewHolder
{
    @SuppressWarnings("unused")
    private final static String TAG = WorkViewBaseHolder.class.getName();

    @StringRes private final int _noWorkMessage;
    protected final View _itemView;
    private final TextView _timeView;
    private final TextView _datetimeView;
    private final TextView _clientNameView;
    private final TextView _workTitleView;
    protected Project _project;
    protected String _clientName;

    public WorkViewBaseHolder(View view, @StringRes int noWorkMessage)
    {
        super(view);
        _noWorkMessage = noWorkMessage;
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
        if (Project.isDummy(_project))
        {
            if (_timeView != null) _timeView.setVisibility(View.GONE);
            if (_datetimeView != null) _datetimeView.setVisibility(View.GONE);
            if (_clientNameView != null) _clientNameView.setVisibility(View.GONE);
            _workTitleView.setText(_noWorkMessage);
            return;
        }

        if (_timeView != null)
        {
            _timeView.setText(_project.getTimeString());
            _timeView.setVisibility(View.VISIBLE);
        }
        if (_datetimeView != null)
        {
            _datetimeView.setText(_project.getDateTimeString2());
            _datetimeView.setVisibility(View.VISIBLE);
        }
        _workTitleView.setText(_project.getName());
        DatabaseHelper db = DatabaseHelper.getInstance(_itemView.getContext());
        Client client = db.findClientById(_project.getClientId());
        _clientName = client.getName();
        _clientNameView.setText(_clientName);
        _clientNameView.setVisibility(View.VISIBLE);
    }
}
