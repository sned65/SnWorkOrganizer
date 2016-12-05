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

import com.silencedut.expandablelayout.ExpandableLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import sne.workorganizer.db.Client;
import sne.workorganizer.db.DatabaseHelper;
import sne.workorganizer.db.Project;
import sne.workorganizer.eb.WorkCreateEvent;
import sne.workorganizer.eb.WorkDeleteEvent;
import sne.workorganizer.eb.WorkUpdateEvent;
import sne.workorganizer.util.Mix;
import sne.workorganizer.util.WoConstants;

/**
 * An activity representing a list of Clients.
 */
public class ClientListActivity extends AppCompatActivity implements EditClientDialog.EditClientDialogCallback
{
    private static final String TAG = ClientListActivity.class.getName();
    private static final int RC_CREATE_CLIENT = 101;

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
    public void onPause()
    {
        ClientListAdapter adapter = getClientListAdapter();
        if (adapter != null)
        {
            EventBus.getDefault().unregister(adapter);
        }
        super.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        ClientListAdapter adapter = getClientListAdapter();
        if (adapter != null)
        {
            EventBus.getDefault().register(adapter);
        }
    }

    private ClientListAdapter getClientListAdapter()
    {
        RecyclerView rv = (RecyclerView) findViewById(R.id.client_list);
        return (ClientListAdapter) rv.getAdapter();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data)
    {
        Log.d(TAG, "requestCode = "+(requestCode == RC_CREATE_CLIENT ? "CREATE_CLIENT" : requestCode)
                +", resultCode = "+(resultCode == RESULT_OK ? "OK" : (resultCode == RESULT_CANCELED ? "CANCELLED" : resultCode)));
        if (resultCode != RESULT_OK) return;


        if (requestCode == RC_CREATE_CLIENT)
        {
            Client newClient = data.getParcelableExtra(WoConstants.ARG_CLIENT);
            createClient(newClient, 0);

//                Snackbar.make(_viewPager, "New client created", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
        }
    }

    private void createClient(Client client, int position)
    {
        Log.d(TAG, "createClient("+client+", "+position+") called");
        RecyclerView rv = (RecyclerView) findViewById(R.id.client_list);
        ClientListAdapter adapter = (ClientListAdapter) rv.getAdapter();
        Log.d(TAG, "adapter.getClientCount() = "+adapter.getClientCount());
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

    private void updateClient(Client client, int position)
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
            ExpandableLayout expandableLayout = (ExpandableLayout) rv.findViewById(R.id.client_content);
            expandableLayout.setExpand(false);
        }
        else
        {
            adapter.notifyItemRemoved(position);
        }
    }

    @Override
    public void onEditClientFinished(Client client, int position)
    {
        updateClient(client, position);
    }

    private class ClientListAdapter extends RecyclerView.Adapter<RowHolder>
    {
        private final Activity _activity;
        private List<Client> _clients;

        public ClientListAdapter(Activity activity)
        {
            _activity = activity;
            DatabaseHelper db = DatabaseHelper.getInstance(_activity);
            //db.findAllClients();
            db.findAllClients(new DatabaseHelper.DbSelectClientsCallback()
            {
                @Override
                public void onSelectFinished(ArrayList<Client> records)
                {
                    Log.d(TAG, "onSelectFinished() "+records+", size = "+((records == null) ? "" : records.size()));
                    _clients = records;
                    showClientList();
                }
            });
        }

//        @Subscribe(threadMode = ThreadMode.MAIN)
//        public void onSearchFinished(ArrayList<Client> records)
//        {
//            Log.d(TAG, "onSearchFinished("+records.size()+") called");
//            _clients = records;
//            showClientList();
//        }

        @Override
        public RowHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.client_row, parent, false);

            return new RowHolder(v, _activity);
        }

        @Override
        public void onBindViewHolder(RowHolder holder, int position)
        {
            if (_clients.isEmpty())
            {
                holder.bindModel(Client.DUMMY);
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

        @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
        public void onWorkCreateEvent(WorkCreateEvent e)
        {
            Project newWork = e.getWork();
            EventBus.getDefault().removeStickyEvent(e);
            String clientId = newWork.getClientId();
            for (int ic = 0; ic < _clients.size(); ++ic)
            {
                Client c = _clients.get(ic);
                if (c.getId().equals(clientId))
                {
                    List<Project> works = c.getProjects();
                    for (int i = 0; i < works.size(); ++i)
                    {
                        Project w = works.get(i);
                        if (newWork.getDate() > w.getDate())
                        {
                            works.add(i, newWork);
                            notifyItemChanged(ic);
                            return;
                        }
                    }
                    works.add(newWork);
                    notifyItemInserted(ic);
                    break;
                }
            }
        }

        @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
        public void onWorkUpdateEvent(WorkUpdateEvent e)
        {
            Project changedWork = e.getWork();
            EventBus.getDefault().removeStickyEvent(e);
            String clientId = changedWork.getClientId();
            for (int ic = 0; ic < _clients.size(); ++ic)
            {
                Client c = _clients.get(ic);
                if (c.getId().equals(clientId))
                {
                    List<Project> works = c.getProjects();
                    for (int i = 0; i < works.size(); ++i)
                    {
                        Project w = works.get(i);
                        if (changedWork.getId().equals(w.getId()))
                        {
                            works.set(i, changedWork);
                            notifyItemChanged(ic);
                            return;
                        }
                    }
                }
            }
        }

        @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
        public void onWorkDeleteEvent(WorkDeleteEvent e)
        {
            Project deletedWork = e.getWork();
            EventBus.getDefault().removeStickyEvent(e);
            String clientId = deletedWork.getClientId();
            for (int ic = 0; ic < _clients.size(); ++ic)
            {
                Client c = _clients.get(ic);
                if (c.getId().equals(clientId))
                {
                    List<Project> works = c.getProjects();
                    for (int i = 0; i < works.size(); ++i)
                    {
                        Project w = works.get(i);
                        if (deletedWork.getId().equals(w.getId()))
                        {
                            works.remove(i);
                            notifyItemChanged(ic);
                            return;
                        }
                    }
                }
            }
        }

        void addClient(Client client, int position)
        {
            _clients.add(position, client);
        }

        void removeClient(int position)
        {
            _clients.remove(position);
        }

        void updateClient(Client client, int position)
        {
            _clients.set(position, client);
        }

        int getClientCount()
        {
            return (_clients == null) ? 0 : _clients.size();
        }
    }

    static class RowHolder extends RecyclerView.ViewHolder
            implements View.OnLongClickListener
    {
        private final Activity _activity;
        private Client _client;
        private final TextView _clientName;
        private final TextView _clientPhone;
        private final TextView _clientSocial;
        private final TextView _clientEmail;
        private final TableLayout _projTable;

        RowHolder(View itemView, Activity activity)
        {
            super(itemView);

            _activity = activity;
            _clientName = (TextView) itemView.findViewById(R.id.client_name);
            _clientPhone = (TextView) itemView.findViewById(R.id.client_phone);
            _clientSocial = (TextView) itemView.findViewById(R.id.client_social);
            _clientEmail = (TextView) itemView.findViewById(R.id.client_email);
            _projTable = (TableLayout) itemView.findViewById(R.id.tbl_client_proj);

            Mix.setupClientCommunications(_activity, _clientPhone, _clientEmail, _clientSocial);

            // set onLongClickListener for ExpandableLayout
            View content = itemView.findViewById(R.id.client_content);
            content.setOnLongClickListener(this);
        }

        void bindModel(Client client)
        {
            if (Client.isDummy(client))
            {
                _client = null;
                _clientName.setText(_clientName.getResources().getString(R.string.no_clients));
                _clientPhone.setText(null);
                _clientSocial.setText(null);
                _clientEmail.setText(null);
                _projTable.setVisibility(View.GONE);
                return;
            }

            _client = client;
            _clientName.setText(_client.getName());
            _clientPhone.setText(_client.getPhone());
            _clientSocial.setText(_client.getSocial());
            _clientEmail.setText(_client.getEmail());
            _projTable.setVisibility(View.VISIBLE);

            List<Project> projects = _client.getProjects();
            for (int i = 0; i < projects.size(); ++i)
            {
                fillProjectRow(i, projects.get(i));
            }
        }

        private void fillProjectRow(int nr, Project project)
        {
            TextView nameView = fillCellText(project.getName());
            TextView dateView = fillCellText(project.getDateTimeString());
            TextView statusView = fillCellText(Mix.statusToString(project.getStatus(), _activity));

            TableRow row = new TableRow(_activity);
            row.addView(nameView);
            row.addView(dateView);
            row.addView(statusView);

            _projTable.addView(row);
        }

        private TextView fillCellText(final String text)
        {
            TextView view = new TextView(_activity);
            view.setText(text);
            view.setGravity(Gravity.CENTER_HORIZONTAL);
            view.setPadding(25, 0, 25, 0);
            return view;
        }

        @Override
        public boolean onLongClick(View v)
        {
            Log.d(TAG, "onLongClick() called "+getAdapterPosition()+"; "+_client);
            if (_client == null)
            {
                return true;
            }

            ClientActionsFragment.newInstance(_client, getAdapterPosition()).show(_activity.getFragmentManager(), "client_actions");
            return true;
        }
    }
}
