package sne.workorganizer;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import sne.workorganizer.db.Client;
import sne.workorganizer.db.DatabaseHelper;
import sne.workorganizer.help.AboutAppDialogFragment;

public class MainActivity extends AppCompatActivity
{
    private static final String TAG = MainActivity.class.getName();
    private static final int PAGE_JOURNAL = 0;
    private static final int PAGE_CLIENTS = 1;
    private static final int MENU_ITEM_CREATE_CLIENT = 0;
    private static final int MENU_ITEM_CREATE_PROJECT = 1;
    private static final int RC_CREATE_CLIENT = 100;

    public static final String TAG_NEW_CLIENT = "new_client";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter _sectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager _viewPager;
    private Menu _menu;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //Log.i(TAG, "onCreate("+savedInstanceState+") called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        _menu = toolbar.getMenu();

        _viewPager = (ViewPager) findViewById(R.id.container);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        _sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        _viewPager.setAdapter(_sectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(_viewPager);

        ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.SimpleOnPageChangeListener()
        {
            @Override
            public void onPageSelected(int position)
            {
                super.onPageSelected(position);
                changeMenuItemVisibility(position);
            }
        };
        _viewPager.addOnPageChangeListener(pageChangeListener);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View view)
//            {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }
//
//    @Override
//    public void onRestoreInstanceState(Bundle state)
//    {
//        super.onRestoreInstanceState(state);
//    }

    /**
     *
     * @param position current page viewer page
     */
    private void changeMenuItemVisibility(int position)
    {
        if (_menu.size() == 0)
        {
            return;
        }

        MenuItem createClientItem = _menu.getItem(MENU_ITEM_CREATE_CLIENT);
        MenuItem createProjectItem = _menu.getItem(MENU_ITEM_CREATE_PROJECT);
        if (position == PAGE_JOURNAL)
        {
            createClientItem.setVisible(false);
            createProjectItem.setVisible(true);
        }
        else if (position == PAGE_CLIENTS)
        {
            createClientItem.setVisible(true);
            createProjectItem.setVisible(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        changeMenuItemVisibility(_viewPager.getCurrentItem());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId())
        {

        // COMMON ITEMS

        case R.id.menu_settings:
//            Intent settings = new Intent(this, SettingsActivity.class);
//            startActivity(settings);
            return true;
        case R.id.menu_about:
        {
            AboutAppDialogFragment about = new AboutAppDialogFragment();
            Bundle args = new Bundle();
            args.putString(AboutAppDialogFragment.ARG_TITLE, getResources().getString(R.string.app_name));
            about.setArguments(args);
            about.show(getFragmentManager(), "about");
            return true;
        }

        // CLIENT ITEMS
        case R.id.menu_create_client:
            Intent createClient = new Intent(this, CreateClientActivity.class);
            startActivityForResult(createClient, RC_CREATE_CLIENT);
            return true;

        // JOURNAL ITEMS
        case R.id.menu_create_project:
            // TODO
            Snackbar.make(_viewPager, "Create new project", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data)
    {
//        Log.i(TAG, "requestCode = "+(requestCode == RC_CREATE_CLIENT ? "CREATE_CLIENT" : requestCode)
//                +", resultCode = "+(resultCode == RESULT_OK ? "OK" : (resultCode == RESULT_CANCELED ? "CANCELLED" : resultCode)));
        if (resultCode == RESULT_OK)
        {
            if (requestCode == RC_CREATE_CLIENT)
            {
                DatabaseHelper db = DatabaseHelper.getInstance(this);
                RecyclerView rv = (RecyclerView) findViewById(R.id.client_list);
                rv.getAdapter().notifyItemInserted(db.getInsertPosition());//.notifyDataSetChanged();

//                Snackbar.make(_viewPager, "New client created", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        }
    }

    public void callPhone(View view)
    {
        String phone = ((TextView) view).getText().toString();
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:"+ Uri.encode(phone.trim())));
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(callIntent);
    }

    public void sendEmail(View view)
    {
        // TODO
    }

    public void openUrl(View view)
    {
        String url = ((TextView) view).getText().toString().trim();
        if (!url.startsWith("http://") && !url.startsWith("https://"))
        {
            url = "http://" + url;
        }
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        if (browserIntent.resolveActivity(getPackageManager()) != null)
        {
            startActivity(browserIntent);
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter
    {

        public SectionsPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int position)
        {
            //Log.i(TAG, "SectionsPagerAdapter.getItem("+position+") called");
            // getItem is called to instantiate the fragment for the given page.
            switch (position)
            {
            case 0: return JournalFragment.newInstance();
            case 1:
            {
                return ClientsFragment.newInstance();
            }
            default: return null;
            }
        }

        @Override
        public int getCount()
        {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            switch (position)
            {
            case 0:
                return getString(R.string.calendar);
            case 1:
                return getString(R.string.clients);
            }
            return null;
        }
    }
}
