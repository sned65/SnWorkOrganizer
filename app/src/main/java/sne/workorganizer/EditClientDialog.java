package sne.workorganizer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import org.greenrobot.eventbus.EventBus;

import sne.workorganizer.db.Client;
import sne.workorganizer.db.DatabaseHelper;
import sne.workorganizer.eb.ClientNameUpdateEvent;

/**
 * Edit client dialog
 */
public class EditClientDialog extends DialogFragment
        implements DialogInterface.OnClickListener
{
    interface EditClientDialogCallback
    {
        void onEditClientFinished(Client client, int position);
    }

    private static final String TAG = EditClientDialog.class.getSimpleName();
    private static final String KEY_CLIENT = "key_client";
    private static final String KEY_POSITION = "key_position";
    private EditText _clientNameView;
    private EditText _clientPhoneView;
    private EditText _clientSocialView;
    private EditText _clientEmailView;
    private Client _client;
    private int _position;

    //    "With normal Java objects, you might pass this [data] in via the constructor, but it is not a
    //    good idea to implement a constructor on a Fragment. Instead, the recipe is to create
    //    a static factory method (typically named newInstance()) that will create the
    //    Fragment and provide the parameters to it by updating the fragment’s “arguments”
    //    (a Bundle)"
    public static EditClientDialog newInstance(Client client, int position)
    {
        EditClientDialog f = new EditClientDialog();

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
                .inflate(R.layout.client_input_fields, null);

        _client = getArguments().getParcelable(KEY_CLIENT);
        _position = getArguments().getInt(KEY_POSITION);

        _clientNameView = (EditText) form.findViewById(R.id.client_name);
        _clientNameView.setText(_client.getName());

        _clientPhoneView = (EditText) form.findViewById(R.id.client_phone);
        _clientPhoneView.setText(_client.getPhone());

        _clientSocialView = (EditText) form.findViewById(R.id.client_social);
        _clientSocialView.setText(_client.getSocial());

        _clientEmailView = (EditText) form.findViewById(R.id.client_email);
        _clientEmailView.setText(_client.getEmail());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder.setTitle(R.string.client_edit_dlg_title).setView(form)
                .setPositiveButton(android.R.string.ok, this)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which)
    {
        // Fill client
        String old_name = _client.getName();
        String new_name = _clientNameView.getText().toString().trim();
        _client.setName(new_name);
        _client.setPhone(_clientPhoneView.getText().toString());
        _client.setSocial(_clientSocialView.getText().toString());
        _client.setEmail(_clientEmailView.getText().toString());

        final Activity a = getActivity();

        // Update DB
        DatabaseHelper.getInstance(getActivity()).updateClient(_client, new DatabaseHelper.DbUpdateClient()
        {
            @Override
            public void onUpdateFinished(Client client)
            {
                // Update UI
                if (a instanceof EditClientDialogCallback)
                {
                    EditClientDialogCallback mainActivity = (EditClientDialogCallback) a;
                    mainActivity.onEditClientFinished(_client, _position);
                }
            }
        });


        // Inform subscribers
//        if (!new_name.equals(old_name))
//        {
//            ClientNameUpdateEvent event = new ClientNameUpdateEvent(_client);
//            EventBus.getDefault().postSticky(event);
//        }
    }
}
