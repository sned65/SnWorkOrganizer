package sne.workorganizer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

import sne.workorganizer.db.DatabaseHelper;
import sne.workorganizer.db.Project;
import sne.workorganizer.util.Mix;
import sne.workorganizer.util.WoConstants;

import static sne.workorganizer.util.WoConstants.ARG_CLIENT_NAME;
import static sne.workorganizer.util.WoConstants.ARG_POSITION;

/**
 * Created by Nikolay_Smirnov on 20.10.2016.
 */
public class EditWorkActivity extends AppCompatActivity
{
    private static final String TAG = EditClientFragment.class.getSimpleName();
    private CalendarView _dateView;
    private TimePicker _timeView;
    private EditText _titleView;
    private EditText _priceView;
    private TextView _designView;
    private Uri _designUri;

    private Project _work;
    private int _position;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_work);

        initActionMode();

        _work = getIntent().getParcelableExtra(WoConstants.ARG_WORK);
        _position = getIntent().getIntExtra(ARG_POSITION, -1);
        String clientName = getIntent().getStringExtra(ARG_CLIENT_NAME);

        TextView clientNameView = (TextView) findViewById(R.id.client_name);
        clientNameView.setText(clientName);

        _dateView = (CalendarView) findViewById(R.id.work_date);
        _dateView.setDate(_work.getDate());
        _dateView.setOnDateChangeListener(new CalendarView.OnDateChangeListener()
        {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth)
            {
                // CalendarView bug workaround. See javadoc comments.
                Mix.calendarSetSelection(view, year, month, dayOfMonth);
            }
        });

        _timeView = (TimePicker) findViewById(R.id.work_time);
        _timeView.setIs24HourView(true);
        int hh = Mix.getDateField(_work.getDate(), Calendar.HOUR_OF_DAY);
        _timeView.setCurrentHour(hh);
        int mm = Mix.getDateField(_work.getDate(), Calendar.MINUTE);
        _timeView.setCurrentMinute(mm);

        _titleView = (EditText) findViewById(R.id.work_title);
        _titleView.setText(_work.getName());

        _priceView = (EditText) findViewById(R.id.work_price);
        Integer price = _work.getPrice();
        if (price != null)
        {
            _priceView.setText(_work.getPrice().toString());
        }

        _designView = (TextView) findViewById(R.id.work_design);
    }

    private void initActionMode()
    {
        startActionMode(new ActionMode.Callback()
        {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu)
            {
                getMenuInflater().inflate(R.menu.menu_editor_actions, menu);
                //mode.setTitle("New Client");
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu)
            {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item)
            {
                switch (item.getItemId())
                {
                case R.id.btn_cancel:
                    cancel();
                    return true;
                case R.id.btn_save:
                    save();
                    return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode)
            {
                cancel();
            }
        });
    }

    private void cancel()
    {
        Intent result = new Intent();
        setResult(RESULT_CANCELED, result);
        finish();
    }

    private void save()
    {
        Log.i(TAG, "save() called");
        if (validateFields()) return;

        fillWork();

        // Update DB
        DatabaseHelper.getInstance(this).updateWork(_work);

        // Update UI
        Intent result = new Intent();
        result.putExtra(WoConstants.ARG_WORK, _work);
        result.putExtra(WoConstants.ARG_POSITION, _position);
        setResult(RESULT_OK, result);
        finish();
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

/*
    @Override
    public void onClick(DialogInterface dialog, int which)
    {
        if (validateFields()) return;

        fillWork();

        // Update DB
        DatabaseHelper.getInstance(getActivity()).updateWork(_work);

        // Update UI
        WorkListActivity mainActivity = (WorkListActivity) getActivity();
        mainActivity.updateWork(_work, _position);
    }
*/
}
