package sne.workorganizer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sne.workorganizer.db.Client;
import sne.workorganizer.db.DatabaseHelper;
import sne.workorganizer.db.Project;

/**
 * An activity representing a list of Clients.
 */
public class ClientListActivity extends AppCompatActivity
{
    private static final String TAG = ClientListActivity.class.getName();
    private static final int RC_CREATE_CLIENT = 101;
    private static final int RC_UPDATE_CLIENT = 102;
    private static final int RC_DELETE_CLIENT = 103;
    public static final String KEY_NEW_CLIENT = "key_new_client";
    public static final String KEY_POSITION = "key_position";

    //private RecyclerView _clientListView;

    private static Client _EMPTY = new Client();
    static {
        _EMPTY.setId("EMPTY");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());
        // add back arrow to toolbar
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            //getSupportActionBar().setDisplayShowHomeEnabled(true);
            //getSupportActionBar().setHomeButtonEnabled(true);
        }
        //_menu = toolbar.getMenu();

        RecyclerView clientListView = (RecyclerView) findViewById(R.id.client_list);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        //_clientListView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        clientListView.setLayoutManager(layoutManager);

        ClientListAdapter adapter = new ClientListAdapter(this);
        clientListView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_clients, menu);
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
        case R.id.menu_create_client:
        {
            Intent createClient = new Intent(this, CreateClientActivity.class);
            startActivityForResult(createClient, RC_CREATE_CLIENT);
            return true;
        }
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void showClientList()
    {
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        findViewById(R.id.client_list).setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data)
    {
        Log.i(TAG, "requestCode = "+(requestCode == RC_CREATE_CLIENT ? "CREATE_CLIENT" : requestCode)
                +", resultCode = "+(resultCode == RESULT_OK ? "OK" : (resultCode == RESULT_CANCELED ? "CANCELLED" : resultCode)));
        if (resultCode != RESULT_OK) return;


        if (requestCode == RC_CREATE_CLIENT)
        {
            //DatabaseHelper db = DatabaseHelper.getInstance(this);
            //rv.getAdapter().notifyItemInserted(db.getInsertPosition());//.notifyDataSetChanged();
            Client newClient = data.getParcelableExtra(KEY_NEW_CLIENT);
            createClient(newClient, 0);

//                Snackbar.make(_viewPager, "New client created", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
        }
    }

    public void createClient(Client client, int position)
    {
        Log.i(TAG, "createClient("+client+", "+position+") called");
        RecyclerView rv = (RecyclerView) findViewById(R.id.client_list);
        ClientListAdapter adapter = (ClientListAdapter) rv.getAdapter();
        Log.i(TAG, "adapter.getClientCount() = "+adapter.getClientCount());
        if (adapter.getClientCount() == 0)
        {
            adapter.addClient(client, position);
//            adapter.notifyItemInserted(position);
            adapter.notifyDataSetChanged();
//            adapter.notifyItemChanged(0);
        }
        else
        {
            adapter.addClient(client, position);
            adapter.notifyItemInserted(position);
        }
    }

    public void updateClient(Client client, int position)
    {
        RecyclerView rv = (RecyclerView) findViewById(R.id.client_list);
        ClientListAdapter adapter = (ClientListAdapter) rv.getAdapter();
        adapter.updateClient(client, position);
        adapter.notifyItemChanged(position);
    }

    public void removeClient(int position)
    {
        RecyclerView rv = (RecyclerView) findViewById(R.id.client_list);
        ClientListAdapter adapter = (ClientListAdapter) rv.getAdapter();
        adapter.removeClient(position);
        if (adapter.getClientCount() == 0)
        {
            adapter.notifyDataSetChanged();
        }
        else
        {
            adapter.notifyItemRemoved(position);
        }
    }

    private class ClientListAdapter extends RecyclerView.Adapter<RowHolder>
    {
        private Activity _activity;
        List<Client> _clients;

        public ClientListAdapter(Activity activity)
        {
            _activity = activity;
            DatabaseHelper db = DatabaseHelper.getInstance(_activity);
            db.findAllClients(new DatabaseHelper.DbSelectCallback()
            {
                @Override
                public void onSelectFinished(ArrayList<Client> records)
                {
                    Log.i(TAG, "onSelectFinished() "+records+", size = "+((records == null) ? "" : records.size()));
                    _clients = records;
                    showClientList();
                }
            });
        }

        @Override
        public RowHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.client_row, parent, false);

            // TODO set the view's size, margins, paddings and layout parameters
            return new RowHolder(v, _activity);
        }

        @Override
        public void onBindViewHolder(RowHolder holder, int position)
        {
            if (_clients.isEmpty())
            {
                holder.bindModel(_EMPTY);
            }
            else
            {
                holder.bindModel(_clients.get(position));
            }

        }

        @Override
        public int getItemCount()
        {
            if (_clients.isEmpty())
            {
                return 1;
            }
            else
            {
                return _clients.size();
            }
        }

        public void addClient(Client client, int position)
        {
            _clients.add(position, client);
        }

        public void removeClient(int position)
        {
            _clients.remove(position);
        }

        public void updateClient(Client client, int position)
        {
            _clients.set(position, client);
        }

        public int getClientCount()
        {
            return (_clients == null) ? 0 : _clients.size();
        }
    }

    static class RowHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener
    {
        private Activity _activity;
        private Client _client;
        private View _tblClientDetails;
        private TextView _clientName;
        private TextView _clientPhone;
        private TextView _clientSocial;
        private TextView _clientEmail;
        private TableLayout _projTable;

        public RowHolder(View itemView, Activity activity)
        {
            super(itemView);

            _activity = activity;
            _tblClientDetails = itemView.findViewById(R.id.tbl_client_details);
            _clientName = (TextView) itemView.findViewById(R.id.client_name);
            _clientPhone = (TextView) itemView.findViewById(R.id.client_phone);
            _clientSocial = (TextView) itemView.findViewById(R.id.client_social);
            _clientEmail = (TextView) itemView.findViewById(R.id.client_email);
            _projTable = (TableLayout) itemView.findViewById(R.id.tbl_client_proj);

            //itemView.setOnClickListener(this);
            //Log.i(TAG, "RowHolder() itemView "+itemView.getClass());
            //itemView.setOnLongClickListener(this);
            // set onLongClickListener for ExpandableLayout
            View content = itemView.findViewById(R.id.client_content);
            content.setOnLongClickListener(this);

            //content.setOnClickListener(this);  // FIXME test
        }

        public void bindModel(Client client)
        {
            if (_EMPTY.getId().equals(client.getId()))
            {
                _client = null;
                _clientName.setText(_clientName.getResources().getString(R.string.no_clients));
                _clientPhone.setText(null);
                _clientSocial.setText(null);
                _clientEmail.setText(null);
                //_tblClientDetails.setVisibility(View.GONE); // FIXME
                _projTable.setVisibility(View.GONE);
                return;
            }

            _client = client;
            _clientName.setText(_client.getName());
            _clientPhone.setText(_client.getPhone());
            _clientSocial.setText(_client.getSocial());
            _clientEmail.setText(_client.getEmail());
            //_tblClientDetails.setVisibility(View.VISIBLE); // FIXME
            _projTable.setVisibility(View.VISIBLE);

            List<Project> projects = _client.getProjects();
            for (int i = 0; i < projects.size(); ++i)
            {
                fillRow(i, projects.get(i));
            }
        }

        private void fillRow(int nr, Project project)
        {
            TextView nameView = fillCell(project.getName());
            TextView dateView = fillCell(project.getDate());
            TextView statusView = fillCell(project.getStatus());

            TableRow row = new TableRow(_activity);
            row.addView(nameView);
            row.addView(dateView);
            row.addView(statusView);
//            row.setOnClickListener(new View.OnClickListener()
//            {
//                @Override
//                public void onClick(View v)
//                {
//                    onClickRow(v);
//                }
//            });
//            _trMap.put(row, nr);

            _projTable.addView(row);
        }

        private TextView fillCell(final String text)
        {
            TextView view = new TextView(_activity);
            view.setText(text);
            view.setGravity(Gravity.CENTER_HORIZONTAL);
            view.setPadding(0, 25, 0, 25);
            return view;
        }

//        public void onClickRow(View v)
//        {
//            onClick(v);
//        }

        @Override
        public void onClick(View v)
        {
            Log.i(TAG, "onClick() called "+getAdapterPosition()+"; "+_client);
            if (_client == null)
            {
                return;
            }
            //Log.i(TAG, "onClick() called "+getAdapterPosition());
//            Intent main = new Intent(v.getContext(), MainActivity.class);
//            main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            main.putExtra(MainActivity.ARG_BOOK_DIR, _bookmark.getDir());
//            main.putExtra(MainActivity.ARG_BOOK_FILE, _bookmark.getFilename());
//            main.putExtra(MainActivity.ARG_BOOK_PAGE_NUMBER, _bookmark.getPosition());
//            v.getContext().startActivity(main);
        }

        @Override
        public boolean onLongClick(View v)
        {
            Log.i(TAG, "onLongClick() called "+getAdapterPosition()+"; "+_client);
            if (_client == null)
            {
                return true;
            }

            ClientActionsFragment.newInstance(_client, getAdapterPosition()).show(_activity.getFragmentManager(), "client_actions");
            return true;
        }
    }
}
