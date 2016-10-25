package sne.workorganizer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import sne.workorganizer.db.DatabaseHelper;
import sne.workorganizer.db.Project;

/**
 * Created by Nikolay_Smirnov on 19.10.2016.
 */
public class ConfirmDeleteWorkFragment extends DialogFragment
        implements DialogInterface.OnClickListener
{
    private static final String TAG = ConfirmDeleteClientFragment.class.getSimpleName();
    private static final String KEY_WORK = "key_work";
    private static final String KEY_CLIENT_NAME = "key_client_name";
    private static final String KEY_POSITION = "key_position";
    private Project _project;
    private int _position;

    //    "With normal Java objects, you might pass this [data] in via the constructor, but it is not a
    //    good idea to implement a constructor on a Fragment. Instead, the recipe is to create
    //    a static factory method (typically named newInstance()) that will create the
    //    Fragment and provide the parameters to it by updating the fragment’s “arguments”
    //    (a Bundle)"
    public static ConfirmDeleteWorkFragment newInstance(Project project, String clientName, int position)
    {
        ConfirmDeleteWorkFragment f = new ConfirmDeleteWorkFragment();

        Bundle args = new Bundle();
        args.putParcelable(KEY_WORK, project);
        args.putString(KEY_CLIENT_NAME, clientName);
        args.putInt(KEY_POSITION, position);
        f.setArguments(args);

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        View form = getActivity().getLayoutInflater()
                .inflate(R.layout.work_confirm_delete, null);

        _project = getArguments().getParcelable(KEY_WORK);
        _position = getArguments().getInt(KEY_POSITION);

        TextView workTimeView = (TextView) form.findViewById(R.id.work_time);
        workTimeView.setText(_project.getDateTimeString());

        String clientName = getArguments().getString(KEY_CLIENT_NAME);
        TextView clientNameView = (TextView) form.findViewById(R.id.client_name);
        clientNameView.setText(clientName);

        TextView workTitleView = (TextView) form.findViewById(R.id.work_title);
        workTitleView.setText(_project.getName());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        return builder.setTitle(R.string.project_delete_dlg_title).setView(form)
                .setPositiveButton(android.R.string.ok, this)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which)
    {
        // Remove from DB
        DatabaseHelper.getInstance(getActivity()).deleteWork(_project.getId());

        // Remove from UI
        WorkListActivity mainActivity = (WorkListActivity) getActivity();
        mainActivity.removeWork(_position);
    }
}