package sne.workorganizer;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

import sne.workorganizer.db.DatabaseHelper;
import sne.workorganizer.db.Project;
import sne.workorganizer.util.Mix;

import static sne.workorganizer.util.WoConstants.ARG_CLIENT_NAME;
import static sne.workorganizer.util.WoConstants.ARG_HIDE_BUTTONS;
import static sne.workorganizer.util.WoConstants.ARG_POSITION;
import static sne.workorganizer.util.WoConstants.ARG_WORK;

/**
 * A fragment representing a single Work Edit screen.
 * This fragment is either contained in a {@link WorkListActivity}
 * in two-pane mode (on tablets) or a {@link EditWorkActivity}
 * on handsets.
 */
public class EditWorkFragment extends Fragment
{
    private static final String TAG = EditWorkFragment.class.getName();

    //private View _rootView;
    private CalendarView _dateView;
    private TimePicker _timeView;
    private EditText _titleView;
    private EditText _priceView;
    private TextView _designView;
    private Uri _designUri;

    private Project _work;
    private int _position;
    private String _clientName;
    private boolean _hideButtons = false;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EditWorkFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_WORK))
        {
            // Load the content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            _work = getArguments().getParcelable(ARG_WORK);
            _position = getArguments().getInt(ARG_POSITION, -1);
            _clientName = getArguments().getString(ARG_CLIENT_NAME);
            _hideButtons = getArguments().getBoolean(ARG_HIDE_BUTTONS, false);

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null)
            {
                appBarLayout.setTitle(_work.getName());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.activity_edit_work, container, false);
        if (_hideButtons)
        {
            View buttonPanel = rootView.findViewById(R.id.button_panel);
            buttonPanel.setVisibility(View.GONE);
        }
        else
        {
            Button btnCancel = (Button) rootView.findViewById(R.id.btn_cancel);
            btnCancel.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    WorkListActivity activity = (WorkListActivity) getActivity();
                    activity.removeWorkEditFragment();
                    // TODO
                }
            });

            Button btnSave = (Button) rootView.findViewById(R.id.btn_save);
            btnSave.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (save())
                    {
                        WorkListActivity activity = (WorkListActivity) getActivity();
                        ((WorkListActivity) getActivity()).updateWork(_work, _position);
                        activity.removeWorkEditFragment();
                        // TODO
                    }
                }
            });
        }
        if (_work == null) return rootView;

        fillViews(rootView);

        return rootView;
    }

    private void fillViews(View rootView)
    {
        TextView clientNameView = (TextView) rootView.findViewById(R.id.client_name);
        clientNameView.setText(_clientName);

        _dateView = (CalendarView) rootView.findViewById(R.id.work_date);
        _dateView.setShowWeekNumber(false);
        _dateView.setDate(_work.getDate());
        _dateView.setOnDateChangeListener(new CalendarView.OnDateChangeListener()
        {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth)
            {
                // CalendarView bug workaround. See javadoc comments.
                Mix.calendarSetSelection(view, year, month, dayOfMonth);
            }
        });

        _timeView = (TimePicker) rootView.findViewById(R.id.work_time_picker);
        _timeView.setIs24HourView(true);
        int hh = Mix.getDateField(_work.getDate(), Calendar.HOUR_OF_DAY);
        _timeView.setCurrentHour(hh);
        int mm = Mix.getDateField(_work.getDate(), Calendar.MINUTE);
        _timeView.setCurrentMinute(mm);

        _titleView = (EditText) rootView.findViewById(R.id.work_title);
        _titleView.setText(_work.getName());

        _priceView = (EditText) rootView.findViewById(R.id.work_price);
        Integer price = _work.getPrice();
        if (price != null)
        {
            _priceView.setText(_work.getPrice().toString());
        }

        _designView = (TextView) rootView.findViewById(R.id.work_design);
    }

    public boolean save()
    {
        Log.i(TAG, "save() called");
        if (validateFields()) return false;

        fillWork();

        // Update DB
        DatabaseHelper.getInstance(getActivity()).updateWork(_work);

        return true;
    }

    private boolean validateFields()
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
        }

        return cancel;
    }

    private void fillWork()
    {
        long dt = _dateView.getDate();
        int hh = _timeView.getCurrentHour();
        int mm = _timeView.getCurrentMinute();
        long date = Mix.updateTime(dt, hh, mm);
        _work.setDate(date);

        _work.setName(_titleView.getText().toString());
        String price_str = _priceView.getText().toString();
        if (TextUtils.isEmpty(price_str))
        {
            _work.setPrice(null);
        }
        else
        {
            try
            {
                Integer price = Integer.valueOf(price_str);
                _work.setPrice(price);
            }
            catch (NumberFormatException e)
            {
                // ignore
            }
        }

/*
        int hh;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            hh = _timePicker.getHour();
        }
        else
        {
            hh = _timePicker.getCurrentHour();
        }

        int mm;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            mm = _timePicker.getMinute();
        }
        else
        {
            mm = _timePicker.getCurrentMinute();
        }


        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(_workDate);
        int yyyy = cal.get(Calendar.YEAR);
        int MM = cal.get(Calendar.MONTH);
        int dd = cal.get(Calendar.DAY_OF_MONTH);
        cal.set(yyyy, MM, dd, hh, mm);
        project.setDate(cal.getTimeInMillis());
*/

        if (_designUri != null)
        {
            _work.setDesign(_designUri.toString());
        }
    }

    public Project getWork()
    {
        return _work;
    }
}
