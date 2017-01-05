package sne.workorganizer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import sne.workorganizer.db.DatabaseHelper;
import sne.workorganizer.db.Picture;
import sne.workorganizer.db.Project;
import sne.workorganizer.util.FileUtils;
import sne.workorganizer.util.WoConstants;

/**
 * An activity representing a single Work Edit screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item edit form is presented side-by-side with a list of items
 * in a {@link WorkListActivity}.
 */
public class EditWorkActivity extends AppCompatActivity
{
    private static final String TAG = EditWorkActivity.class.getName();
    private int _position;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        if (BuildConfig.DEBUG)
        {
            Log.i(TAG, "onCreate() called. instance = " + savedInstanceState);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_detail);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
//        toolbar.setVisibility(View.INVISIBLE);
//        setSupportActionBar(toolbar);
//
//        // Show the Up button in the action bar.
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null)
//        {
//            actionBar.setDisplayHomeAsUpEnabled(true);
//        }

        //initActionMode();

        if (savedInstanceState == null)
        {
            // Create the edit fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putParcelable(WoConstants.ARG_WORK,
                    getIntent().getParcelableExtra(WoConstants.ARG_WORK));
            _position = getIntent().getIntExtra(WoConstants.ARG_POSITION, -1);
            arguments.putInt(WoConstants.ARG_POSITION, _position);
            arguments.putString(WoConstants.ARG_CLIENT_NAME,
                    getIntent().getStringExtra(WoConstants.ARG_CLIENT_NAME));
            arguments.putBoolean(WoConstants.ARG_HIDE_BUTTONS, true);

            EditWorkFragment fragment = new EditWorkFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.work_detail_container, fragment, WoConstants.FRG_WORK_EDIT)
                    .commitNow();

            initActionMode();

            // It seems that layouts of the NestedScrollView and the Toolbar
            // are calculated in different threads and not synchronized (bug ?).
            // As a result they overlaps sometimes.
            // The code below scrolls the NestedScrollView to its initial position
            // to protect against the layout overlapping.
            final NestedScrollView scroll = (NestedScrollView) findViewById(R.id.work_detail_scroll);
            scroll.post(new Runnable()
            {
                @Override
                public void run()
                {
                    scroll.scrollTo(0, 0);
                }
            });
        }
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

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData)
    {
        if (BuildConfig.DEBUG)
        {
            Log.i(TAG, "onActivityResult(" + requestCode + ", " + requestCode + ") called");
        }

        if (resultCode == Activity.RESULT_OK && requestCode == WoConstants.RC_OPEN_DESIGN_DOCUMENT)
        {
            if (resultData == null) return;
            Uri uri = resultData.getData();
            if (BuildConfig.DEBUG)
            {
                Log.i(TAG, "onActivityResult() uri = " + uri.toString());
            }
            String path = FileUtils.getPath(this, uri);

            EditWorkFragment frg = (EditWorkFragment) getSupportFragmentManager().findFragmentByTag(WoConstants.FRG_WORK_EDIT);
            frg.changeDesign(path);
        }

        else if (resultCode == Activity.RESULT_OK && requestCode == WoConstants.RC_OPEN_RESULT_DOCUMENT)
        {
            if (resultData == null) return;
            Uri uri = resultData.getData();
            if (BuildConfig.DEBUG)
            {
                Log.i(TAG, "onActivityResult() uri = " + uri.toString());
            }
            String path = FileUtils.getPath(this, uri);

            EditWorkFragment frg = (EditWorkFragment) getSupportFragmentManager().findFragmentByTag(WoConstants.FRG_WORK_EDIT);
            frg.setResultPath(path);
            frg.refreshResult();
        }

        else if (requestCode == WoConstants.RC_TAKE_PICTURE)
        {
            EditWorkFragment frg = (EditWorkFragment) getSupportFragmentManager().findFragmentByTag(WoConstants.FRG_WORK_EDIT);
            frg.acceptPhoto(resultCode == Activity.RESULT_OK);
        }
    }

    private void cancel()
    {
        Intent result = new Intent();
        setResult(RESULT_CANCELED, result);
        finish();
    }

    private void save()
    {
        final EditWorkFragment frg = (EditWorkFragment) getSupportFragmentManager().findFragmentByTag(WoConstants.FRG_WORK_EDIT);
        if (!frg.save(new DatabaseHelper.DbUpdateWorkCallback()
        {
            @Override
            public void onUpdateFinished(Project work, Picture result)
            {
                Intent answer = new Intent();
                answer.putExtra(WoConstants.ARG_WORK, frg.getWork());
                //answer.putExtra(WoConstants.ARG_POSITION, _position);
                answer.putExtra(WoConstants.ARG_PICTURE, frg.getResultPicture());
                setResult(RESULT_OK, answer);
                finish();
            }
        }))
        {
            // do nothing
        }
    }
}
