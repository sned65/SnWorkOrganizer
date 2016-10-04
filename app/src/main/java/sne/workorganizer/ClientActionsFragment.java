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
 * Created by Nikolay_Smirnov on 04.10.2016.
 */
public class ClientActionsFragment extends DialogFragment
{
    private static final String KEY_CLIENT = "key_client";
    private Client _client;

    //    "With normal Java objects, you might pass this [data] in via the constructor, but it is not a
    //    good idea to implement a constructor on a Fragment. Instead, the recipe is to create
    //    a static factory method (typically named newInstance()) that will create the
    //    Fragment and provide the parameters to it by updating the fragment’s “arguments”
    //    (a Bundle)"
    public static ClientActionsFragment newInstance(Client client)
    {
        ClientActionsFragment f = new ClientActionsFragment();

        Bundle args = new Bundle();
        args.putParcelable(KEY_CLIENT, client);
        f.setArguments(args);

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        View form = getActivity().getLayoutInflater()
                .inflate(R.layout.dialog_client_actions, null);

        ListView actionList = (ListView) form.findViewById(R.id.client_actions_list);
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
            ConfirmDeleteClientFragment.newInstance(_client).show(getFragmentManager(), "confirm_del_client");
            break;
        case 1: // Edit
//            EditNoteFragment.newInstance(_note).show(getFragmentManager(), "edit_note");
            break;
        }
    }
}
