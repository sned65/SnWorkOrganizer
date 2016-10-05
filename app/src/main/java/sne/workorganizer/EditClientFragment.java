package sne.workorganizer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import sne.workorganizer.db.Client;
import sne.workorganizer.db.DatabaseHelper;

/**
 * Edit client dialog
 */
public class EditClientFragment extends DialogFragment
        implements DialogInterface.OnClickListener
{
    private static final String TAG = EditClientFragment.class.getSimpleName();
    private static final String KEY_CLIENT = "client";
    private EditText _clientNameView;
    private EditText _clientPhoneView;
    private EditText _clientSocialView;
    private EditText _clientEmailView;
    private Client _client;

    //    "With normal Java objects, you might pass this [data] in via the constructor, but it is not a
    //    good idea to implement a constructor on a Fragment. Instead, the recipe is to create
    //    a static factory method (typically named newInstance()) that will create the
    //    Fragment and provide the parameters to it by updating the fragment’s “arguments”
    //    (a Bundle)"
    public static EditClientFragment newInstance(Client client)
    {
        EditClientFragment f = new EditClientFragment();

        Bundle args = new Bundle();
        args.putParcelable(KEY_CLIENT, client);
        f.setArguments(args);

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        View form = getActivity().getLayoutInflater()
                .inflate(R.layout.client_fields, null);

        _client = getArguments().getParcelable(KEY_CLIENT);

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
        _client.setName(_clientNameView.getText().toString());
        _client.setPhone(_clientPhoneView.getText().toString());
        _client.setSocial(_clientSocialView.getText().toString());
        _client.setEmail(_clientEmailView.getText().toString());

        int position = DatabaseHelper.getInstance(getActivity()).updateClient(_client);
        MainActivity mainActivity = (MainActivity) getActivity();
        RecyclerView rv = (RecyclerView) mainActivity.findViewById(R.id.client_list);
        rv.getAdapter().notifyItemChanged(position);
    }
}