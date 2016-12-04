package sne.workorganizer;

import android.graphics.Color;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import sne.workorganizer.db.Client;
import sne.workorganizer.db.DatabaseHelper;
import sne.workorganizer.db.Project;
import sne.workorganizer.util.Mix;

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
    private final TextView _workStatusView;
    protected Project _project;
    protected String _clientName;

    protected WorkViewBaseHolder(View view, @StringRes int noWorkMessage)
    {
        super(view);
        _noWorkMessage = noWorkMessage;
        _itemView = view;
        _timeView = (TextView) view.findViewById(R.id.work_time);
        _datetimeView = (TextView) view.findViewById(R.id.work_date_time);
        _clientNameView = (TextView) view.findViewById(R.id.client_name);
        _workTitleView = (TextView) view.findViewById(R.id.work_title);
        _workStatusView = (TextView) view.findViewById(R.id.work_status);
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
            if (_workStatusView != null) _workStatusView.setVisibility(View.GONE);
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
            if (Mix.isHoliday(_project.getDate()))
            {
                _datetimeView.setTextColor(Color.RED);
            }
            else
            {
                _datetimeView.setTextColor(Color.BLACK);
            }
            _datetimeView.setVisibility(View.VISIBLE);
        }
        _workTitleView.setText(_project.getName());
        DatabaseHelper db = DatabaseHelper.getInstance(_itemView.getContext());
        Client client = db.findClientById(_project.getClientId());
        if (client == null)
        {
            // should never happen
            _clientName = "???";
        }
        else
        {
            _clientName = client.getName();
        }
        _clientNameView.setText(_clientName);
        _clientNameView.setVisibility(View.VISIBLE);

        if (_project.getStatus() == Project.WorkStatus.CREATED)
        {
            _workStatusView.setVisibility(View.GONE);
        }
        else
        {
            String projectStatus = Mix.statusToString(_project.getStatus(), _itemView.getContext());
            _workStatusView.setText(projectStatus);
            _workStatusView.setVisibility(View.VISIBLE);
        }
    }
}
