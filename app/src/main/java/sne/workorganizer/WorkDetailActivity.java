package sne.workorganizer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import sne.workorganizer.db.Client;
import sne.workorganizer.db.Project;
import sne.workorganizer.util.WoConstants;

/**
 * An activity representing a single Work detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link WorkListActivity}.
 */
public class WorkDetailActivity extends AppCompatActivity implements EditClientDialog.EditClientDialogCallback
{
    private static final String TAG = WorkDetailActivity.class.getSimpleName();
    private static final String FRG_DETAILS = "FRG_DETAILS";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

/*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Snackbar.make(view, "Replace with your own detail action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
*/

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null)
        {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putParcelable(WoConstants.ARG_WORK,
                    getIntent().getParcelableExtra(WoConstants.ARG_WORK));
            WorkDetailFragment fragment = new WorkDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.work_detail_container, fragment, FRG_DETAILS)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch (id)
        {
        case android.R.id.home:
        {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            Intent i = new Intent(this, WorkListActivity.class);
            Project p = getIntent().getParcelableExtra(WoConstants.ARG_WORK);
            i.putExtra(WoConstants.ARG_CURRENT_DATE, p.getDate());
            navigateUpTo(i);
            return true;
        }

        case R.id.menu_edit_client:
        {
            WorkDetailFragment wdf = (WorkDetailFragment) getSupportFragmentManager().findFragmentByTag(FRG_DETAILS);
            EditClientDialog.newInstance(wdf.getClient(), -1).show(getFragmentManager(), "edit_client");
            return true;
        }

        case R.id.menu_edit_work:
        {
            WorkDetailFragment wdf = (WorkDetailFragment) getSupportFragmentManager().findFragmentByTag(FRG_DETAILS);
            Intent i = new Intent(this, EditWorkActivity.class);
            i.putExtra(WoConstants.ARG_WORK, wdf.getWork());
            i.putExtra(WoConstants.ARG_CLIENT_NAME, wdf.getClient().getName());
            startActivityForResult(i, WorkListActivity.RC_EDIT_WORK);
            return true;
        }

        case R.id.menu_delete:
        {
            WorkDetailFragment wdf = (WorkDetailFragment) getSupportFragmentManager().findFragmentByTag(FRG_DETAILS);
            ConfirmDeleteWorkFragment.newInstance(wdf.getWork(), wdf.getClient().getName())
                    .show(getFragmentManager(), "confirm_del_work");
            return true;
        }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data)
    {
        Log.i(TAG, "requestCode = " + requestCode
                + ", resultCode = " + (resultCode == RESULT_OK ? "OK" : (resultCode == RESULT_CANCELED ? "CANCELLED" : resultCode)));
        if (resultCode != Activity.RESULT_OK) return;

        if (requestCode == WorkListActivity.RC_EDIT_WORK)
        {
            Project work = data.getParcelableExtra(WoConstants.ARG_WORK);

            WorkDetailFragment wdf = (WorkDetailFragment) getSupportFragmentManager().findFragmentByTag(FRG_DETAILS);
            if (wdf != null)
            {
                wdf.setWork(work);
            }
        }
    }

    @Override
    public void onEditClientFinished(Client client, int position)
    {
        WorkDetailFragment wdf = (WorkDetailFragment) getSupportFragmentManager().findFragmentByTag(FRG_DETAILS);
        wdf.updateClient(client);
    }
}
