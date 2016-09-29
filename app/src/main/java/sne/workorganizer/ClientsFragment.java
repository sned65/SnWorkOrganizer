package sne.workorganizer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A fragment containing list of clients.
 */
public class ClientsFragment extends Fragment
{
    private RecyclerView _recyclerView;

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
        _recyclerView.setHasFixedSize(true);

        LinearLayoutManager layoutManager = new LinearLayoutManager(rootView.getContext());
        _recyclerView.setLayoutManager(layoutManager);


        return rootView;
    }
}
