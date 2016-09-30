package sne.workorganizer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sne.workorganizer.db.Client;
import sne.workorganizer.db.Project;

/**
 * A fragment containing list of clients.
 */
public class ClientsFragment extends Fragment
{
    private RecyclerView _recyclerView;

    private static Client _EMPTY = new Client();
    static {
        _EMPTY.setId("EMPTY");
    }

    private ArrayList<Client> _clients = new ArrayList<>();
    private boolean _clientsLoaded = false;

    /**
     * Returns a new instance of ClientsFragment.
     */
    public static ClientsFragment newInstance()
    {
        ClientsFragment fragment = new ClientsFragment();
//        Bundle args = new Bundle();
//        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //Log.i(TAG, "onCreate() called "+(savedInstanceState == null));
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.client_list, container, false);
        _recyclerView = (RecyclerView) rootView.findViewById(R.id.client_list);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        //_recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(rootView.getContext());
        _recyclerView.setLayoutManager(layoutManager);

        RecyclerView.Adapter adapter = new ClientListAdapter(getActivity());
        _recyclerView.setAdapter(adapter);
        return rootView;
    }

    private class ClientListAdapter extends RecyclerView.Adapter<RowHolder>
    {
        private Activity _activity;

        public ClientListAdapter(Activity activity)
        {
            _activity = activity;
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
    }

    static class RowHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener
    {
        private Activity _activity;
        private Client _client;
        private TextView _clientName;
        private TableLayout _projTable;

        public RowHolder(View itemView, Activity activity)
        {
            super(itemView);

            _activity = activity;
            _clientName = (TextView) itemView.findViewById(R.id.client_name);
            _projTable = (TableLayout) itemView.findViewById(R.id.tbl_client_proj);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public void setName(String text)
        {
            _clientName.setText(text);
        }

        public void bindModel(Client client)
        {
            if (_EMPTY.getId().equals(client.getId()))
            {
                _client = null;
                setName(_clientName.getResources().getString(R.string.no_clients));
                _projTable.setVisibility(View.GONE);
                return;
            }

            _client = client;
            setName(_client.getName());
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
/*
            row.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    onClickRow(v);
                }
            });
*/
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

        @Override
        public void onClick(View v)
        {
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
            //Log.i(TAG, "onLongClick() called "+getAdapterPosition());
            if (_client == null)
            {
                return true;
            }

            //NoteActionsFragment.newInstance(_bookmark).show(_activity.getFragmentManager(), "note_actions");
            return true;
        }
    }
}
