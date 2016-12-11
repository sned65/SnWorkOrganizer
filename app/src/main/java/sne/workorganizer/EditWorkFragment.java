package sne.workorganizer;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;

import java.io.File;
import java.util.Calendar;

import sne.workorganizer.db.DatabaseHelper;
import sne.workorganizer.db.Picture;
import sne.workorganizer.db.Project;
import sne.workorganizer.util.FileUtils;
import sne.workorganizer.util.InfoDialogFragment;
import sne.workorganizer.util.Mix;
import sne.workorganizer.util.PhotoUtils;
import sne.workorganizer.util.WoConstants;

import static sne.workorganizer.util.WoConstants.ARG_CLIENT_NAME;
import static sne.workorganizer.util.WoConstants.ARG_HIDE_BUTTONS;
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
    private String _designPath;
    private ImageButton _designImageBtn;
    private ImageButton _designClearBtn;

    private ImageButton _resultClearBtn;
    private ImageButton _resultSelectBtn;
    private ImageButton _cameraBtn;
    private String _resultPath;
    private String _photoPath;
    private boolean _resultCanBeChanged = false;
    private Picture _resultPicture;

    private CheckBox _workStatusBtn;

    private Project _work;
    //private int _position;
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
        //setRetainInstance(true);

        if (getArguments().containsKey(ARG_WORK))
        {
            // Load the content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            _work = getArguments().getParcelable(ARG_WORK);
            //_position = getArguments().getInt(ARG_POSITION, -1);
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
                    WorkListMaster activity = (WorkListMaster) getActivity();
                    activity.removeWorkEditFragment();
                }
            });

            Button btnSave = (Button) rootView.findViewById(R.id.btn_save);
            btnSave.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // Note: buttons are present in two-pane mode only
                    if (!save(new DatabaseHelper.DbUpdateWorkCallback()
                    {
                        @Override
                        public void onUpdateFinished(Project work, Picture result)
                        {
                            WorkListMaster master = (WorkListMaster) getActivity();
                            master.updateWork(_work, _resultPicture);
                            master.removeWorkEditFragment();
                        }
                    }))
                    {
                        // do nothing
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
        //noinspection deprecation
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
        Mix.timePickerSetHour(_timeView, hh);
        int mm = Mix.getDateField(_work.getDate(), Calendar.MINUTE);
        Mix.timePickerSetMinute(_timeView, mm);

        _titleView = (EditText) rootView.findViewById(R.id.work_title);
        _titleView.setText(_work.getName());

        _priceView = (EditText) rootView.findViewById(R.id.work_price);
        Integer price = _work.getPrice();
        if (price != null)
        {
            _priceView.setText(_work.getPrice().toString());
        }

        // Design

        _designView = (TextView) rootView.findViewById(R.id.work_design);
        _designImageBtn = (ImageButton) rootView.findViewById(R.id.btn_select_design);
        _designImageBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onSelectDesign();
            }
        });

        _designClearBtn = (ImageButton) rootView.findViewById(R.id.btn_clear_design);
        _designClearBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                changeDesign(null);
            }
        });

        _designPath = _work.getDesign();
        if (_designPath == null)
        {
            _designClearBtn.setVisibility(View.GONE);
        }
        else
        {
            _designClearBtn.setVisibility(View.VISIBLE);
            String imageName = new File(_designPath).getName();
            Bitmap bm = PhotoUtils.getThumbnailBitmap(_designPath, WoConstants.WIDTH_THUMBNAIL);
            if (bm != null)
            {
                _designView.setText(imageName);
                _designImageBtn.setImageBitmap(bm);
            }
            else
            {
                String msg = getString(R.string.error_file_not_found) + ": " + _designPath;
                Mix.showError(getActivity(), msg, InfoDialogFragment.Severity.ERROR);
            }
        }

        // Result

        _resultClearBtn = (ImageButton) rootView.findViewById(R.id.btn_clear_result);
        _resultClearBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                _resultCanBeChanged = true;
                _resultPath = null;
                refreshResult();
            }
        });

        _resultSelectBtn = (ImageButton) rootView.findViewById(R.id.btn_select_result);
        _resultSelectBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                _resultCanBeChanged = true;
                onSelectResult();
            }
        });

        _cameraBtn = (ImageButton) rootView.findViewById(R.id.btn_camera);
        _cameraBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                _resultCanBeChanged = true;
                makePhoto();
            }
        });

        DatabaseHelper db = DatabaseHelper.getInstance(getActivity());
        Picture resultPhoto = null;
        try
        {
            resultPhoto = db.findPictureByWorkId(_work.getId());
        }
        catch (SQLiteDatabaseCorruptException e)
        {
            Mix.showError(getActivity(), e.getMessage(), InfoDialogFragment.Severity.ERROR);
        }
        if (resultPhoto == null || resultPhoto.getResultPhoto() == null)
        {
            _resultClearBtn.setVisibility(View.GONE);
        }
        else
        {
            _resultClearBtn.setVisibility(View.VISIBLE);
            _resultPath = resultPhoto.getResultPhoto();

            Bitmap bm = PhotoUtils.getThumbnailBitmap(resultPhoto.getResultPhoto(), WoConstants.WIDTH_THUMBNAIL);
            if (bm != null)
            {
                _resultSelectBtn.setImageBitmap(bm);
            }
            else
            {
                String msg = getString(R.string.error_file_not_found) + ":" + resultPhoto.getResultPhoto();
                Mix.showError(getActivity(), msg, InfoDialogFragment.Severity.WARNING);
            }
        }

        // Status

        _workStatusBtn = (CheckBox) rootView.findViewById(R.id.btn_work_status);
        _workStatusBtn.setChecked(_work.getStatus() == Project.WorkStatus.DONE);
    }

    private void onSelectDesign()
    {
        Intent i = new Intent().setType("image/*");
        i.setAction(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE);
        getActivity().startActivityForResult(i, WoConstants.RC_OPEN_DESIGN_DOCUMENT);
    }

    private void onSelectResult()
    {
        Intent i = new Intent().setType("image/*");
        i.setAction(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE);
        getActivity().startActivityForResult(i, WoConstants.RC_OPEN_RESULT_DOCUMENT);
    }

    private void makePhoto()
    {
        Log.d(TAG, "makePhoto() called");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri resultUri = PhotoUtils.getOutputMediaFileUri(_work.getName()); // create a file to save the image
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, resultUri); // set the image file name
        // The following option is ignored. Apparently, bug in SDK.
        //takePictureIntent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Notice that the startActivityForResult() method is protected
        // by a condition that calls resolveActivity() as recommended by
        // https://developer.android.com/training/camera/photobasics.html
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null)
        {
            _photoPath = FileUtils.getPath(getActivity(), resultUri);
            getActivity().startActivityForResult(takePictureIntent, WoConstants.RC_TAKE_PICTURE);
        }
    }

    public boolean save(DatabaseHelper.DbUpdateWorkCallback callback)
    {
        Log.d(TAG, "save() called");
        if (validateFields()) return false;

        fillWork();
        fillResultPicture();

        // Update DB
        DatabaseHelper db = DatabaseHelper.getInstance(getActivity());
        db.updateWork(_work, _resultPicture, callback);
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
        int hh = Mix.timePickerGetHour(_timeView);
        int mm = Mix.timePickerGetMinute(_timeView);
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

        _work.setDesign(_designPath);

        if (_workStatusBtn.isChecked())
        {
            _work.setStatus(Project.WorkStatus.DONE);
        }
        else
        {
            _work.setStatus(Project.WorkStatus.CREATED);
        }
    }

    private void fillResultPicture()
    {
        Log.i(TAG, "fillResultPicture() _resultPath = "+_resultPath+(_resultCanBeChanged?" Changed":" NOT Changed"));
        if (_resultCanBeChanged)
        {
            _resultPicture = new Picture();
            _resultPicture.setWorkId(_work.getId());
            _resultPicture.setResultPhoto(_resultPath);
        }
        else
        {
            _resultPicture = null;
        }
    }

    public Project getWork()
    {
        return _work;
    }

    public Picture getResultPicture()
    {
        return _resultPicture;
    }

    public void changeDesign(String path)
    {
        _designPath = path;
        if (path == null)
        {
            _designClearBtn.setVisibility(View.GONE);
            _designImageBtn.setImageResource(R.drawable.ic_local_florist_black_24dp);
            _designView.setText(getString(R.string.select_design));
        }
        else
        {
            _designClearBtn.setVisibility(View.VISIBLE);
            String imageName = new File(path).getName();
            Bitmap bm = PhotoUtils.getThumbnailBitmap(path, WoConstants.WIDTH_THUMBNAIL);
            if (bm != null)
            {
                _designView.setText(imageName);
                _designImageBtn.setImageBitmap(bm);
            }
            else
            {
                String msg = getString(R.string.error_file_not_found) + ": " + _designPath;
                Mix.showError(getActivity(), msg, InfoDialogFragment.Severity.ERROR);
            }
        }
    }

    public void setResultPath(String path)
    {
        _resultPath = path;
    }

    public void acceptPhoto(boolean flag)
    {
        Log.d(TAG, "acceptPhoto("+flag+") called for "+_photoPath);
        if (flag)
        {
            _resultPath = _photoPath;
            refreshResult();
        }
        _photoPath = null;
    }

    public void refreshResult()
    {
        if (_resultPath == null)
        {
            _resultClearBtn.setVisibility(View.GONE);
            _resultSelectBtn.setImageResource(R.drawable.ic_local_florist_black_24dp);
            _cameraBtn.setImageResource(R.drawable.ic_photo_camera_black_24dp);
        }
        else
        {
            _resultClearBtn.setVisibility(View.VISIBLE);

            Bitmap thumbnail = PhotoUtils.getThumbnailBitmap(_resultPath, WoConstants.WIDTH_THUMBNAIL);
            if (thumbnail != null)
            {
                _resultSelectBtn.setImageBitmap(thumbnail);
            }
            else
            {
                String msg = getString(R.string.error_file_not_found) + ": " + _resultPath;
                Mix.showError(getActivity(), msg, InfoDialogFragment.Severity.ERROR);
            }
        }
    }
}
