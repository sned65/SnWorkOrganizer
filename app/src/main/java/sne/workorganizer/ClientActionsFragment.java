package sne.workorganizer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import sne.workorganizer.db.Client;

/**
 * Popup menu with actions on a client.
 */
public class ClientActionsFragment extends DialogFragment
{
    private static final String KEY_CLIENT = "key_client";
    private static final String KEY_POSITION = "key_position";
    private Client _client;
    private int _position;

    //    "With normal Java objects, you might pass this [data] in via the constructor, but it is not a
    //    good idea to implement a constructor on a Fragment. Instead, the recipe is to create
    //    a static factory method (typically named newInstance()) that will create the
    //    Fragment and provide the parameters to it by updating the fragment’s “arguments”
    //    (a Bundle)"
    public static ClientActionsFragment newInstance(Client client, int position)
    {
        ClientActionsFragment f = new ClientActionsFragment();

        Bundle args = new Bundle();
        args.putParcelable(KEY_CLIENT, client);
        args.putInt(KEY_POSITION, position);
        f.setArguments(args);

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        View form = getActivity().getLayoutInflater()
                .inflate(R.layout.dialog_base_actions, null);

        ListView actionList = (ListView) form.findViewById(R.id.base_actions_list);
        List<String> items = new ArrayList<>();
        // Note: insert order is correlated with on click actions
        items.add(getString(R.string.delete));
        items.add(getString(R.string.edit));
        actionList.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, items));
        actionList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                //Log.i(TAG, "onItemClick("+position+")");
                doAction(position);
            }
        });

        _client = getArguments().getParcelable(KEY_CLIENT);
        _position = getArguments().getInt(KEY_POSITION);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder.setTitle(R.string.client_actions_dlg_title).setView(form).create();
    }

    private void doAction(int position)
    {
        dismiss();
        // Note: position is correlated with item insert order
        switch (position)
        {
        case 0: // Delete
            ConfirmDeleteClientFragment.newInstance(_client, _position).show(getFragmentManager(), "confirm_del_client");
            break;
        case 1: // Edit
            EditClientDialog.newInstance(_client, _position).show(getFragmentManager(), "edit_client");
            break;
        }
    }
}
