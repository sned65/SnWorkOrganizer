package sne.workorganizer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import sne.workorganizer.db.Client;
import sne.workorganizer.db.DatabaseHelper;

/**
 * Delete a client with confirmation.
 */
public class ConfirmDeleteClientFragment extends DialogFragment
        implements DialogInterface.OnClickListener
{
    private static final String TAG = ConfirmDeleteClientFragment.class.getSimpleName();
    private static final String KEY_CLIENT = "key_client";
    private static final String KEY_POSITION = "key_position";
    private Client _client;
    private int _position;

    //    "With normal Java objects, you might pass this [data] in via the constructor, but it is not a
    //    good idea to implement a constructor on a Fragment. Instead, the recipe is to create
    //    a static factory method (typically named newInstance()) that will create the
    //    Fragment and provide the parameters to it by updating the fragment’s “arguments”
    //    (a Bundle)"
    public static ConfirmDeleteClientFragment newInstance(Client client, int position)
    {
        ConfirmDeleteClientFragment f = new ConfirmDeleteClientFragment();

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
                .inflate(R.layout.client_confirm_delete, null);

        _client = getArguments().getParcelable(KEY_CLIENT);
        _position = getArguments().getInt(KEY_POSITION);

        TextView clientNameView = (TextView) form.findViewById(R.id.client_name);
        clientNameView.setText(_client.getName());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder.setTitle(R.string.client_delete_dlg_title).setView(form)
                .setPositiveButton(android.R.string.ok, this)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which)
    {
        final ClientListActivity mainActivity = (ClientListActivity) getActivity();

        // Remove from DB
        DatabaseHelper.getInstance(getActivity()).deleteClient(_client.getId(), new DatabaseHelper.DbDeleteCallback()
        {
            @Override
            public void onDeleteFinished(String id)
            {
                // Remove from UI
                mainActivity.removeClient(_position);

                // Inform subscribers
//                ClientDeleteEvent event = new ClientDeleteEvent(_client);
//                EventBus.getDefault().postSticky(event);
            }
        });
    }
}
