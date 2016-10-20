package sne.workorganizer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.TextView;

import sne.workorganizer.db.DatabaseHelper;
import sne.workorganizer.db.Project;
import sne.workorganizer.util.WoConstants;

import static sne.workorganizer.util.WoConstants.ARG_CLIENT_NAME;
import static sne.workorganizer.util.WoConstants.ARG_POSITION;

/**
 * Created by Nikolay_Smirnov on 20.10.2016.
 */
public class EditWorkActivity extends AppCompatActivity
{
    private static final String TAG = EditClientFragment.class.getSimpleName();
    private EditText _workTitleView;
    private EditText _workPriceView;
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

        _workTitleView = (EditText) findViewById(R.id.work_title);
        _workTitleView.setText(_work.getName());

        _workPriceView = (EditText) findViewById(R.id.work_price);
        Integer price = _work.getPrice();
        if (price != null)
        {
            _workPriceView.setText(_work.getPrice().toString());
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

        String title = _workTitleView.getText().toString();
        if (TextUtils.isEmpty(title))
        {
            _workTitleView.setError(getString(R.string.error_field_required));
            if (!cancel) focus = _workTitleView;
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
        _work.setName(_workTitleView.getText().toString());
        String price_str = _workPriceView.getText().toString();
        if (!TextUtils.isEmpty(price_str))
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
