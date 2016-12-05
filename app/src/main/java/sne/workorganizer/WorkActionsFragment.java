package sne.workorganizer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import sne.workorganizer.db.DatabaseHelper;
import sne.workorganizer.db.Project;
import sne.workorganizer.eb.WorkUpdateEvent;
import sne.workorganizer.util.WoConstants;

/**
 * Popup dialog to select actions on Work.
 */
public class WorkActionsFragment extends DialogFragment
{
    private static final String TAG = WorkActionsFragment.class.getName();
    private static final String KEY_WORK = "key_work";
    private static final String KEY_CLIENT_NAME = "key_client_name";
    private static final String KEY_POSITION = "key_position";
    private Project _work;
    private String _clientName;
    private int _position;

    //    "With normal Java objects, you might pass this [data] in via the constructor, but it is not a
    //    good idea to implement a constructor on a Fragment. Instead, the recipe is to create
    //    a static factory method (typically named newInstance()) that will create the
    //    Fragment and provide the parameters to it by updating the fragment’s “arguments”
    //    (a Bundle)"
    public static WorkActionsFragment newInstance(Project project, String clientName, int position)
    {
        WorkActionsFragment f = new WorkActionsFragment();

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
        _work = getArguments().getParcelable(KEY_WORK);
        _clientName = getArguments().getString(KEY_CLIENT_NAME);
        _position = getArguments().getInt(KEY_POSITION);

        View form = getActivity().getLayoutInflater()
                .inflate(R.layout.dialog_base_actions, null);

        ListView actionList = (ListView) form.findViewById(R.id.base_actions_list);
        List<String> items = new ArrayList<>();
        // Note: insert order is correlated with on click actions
        items.add(getString(R.string.delete));
        items.add(getString(R.string.edit));
        if (_work.getStatus() != Project.WorkStatus.DONE)
        {
            items.add(getString(R.string.mark_as_done));
        }
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
            ConfirmDeleteWorkFragment.newInstance(_work, _clientName, _position)
                    .show(getFragmentManager(), "confirm_del_work");
            break;

        case 1: // Edit
            WorkListMaster master = (WorkListMaster) getActivity();
            if (master.isTwoPane())
            {
                Bundle arguments = new Bundle();
                Project work_clone = _work.clone();
                arguments.putParcelable(WoConstants.ARG_WORK, work_clone);
                arguments.putString(WoConstants.ARG_CLIENT_NAME, _clientName);
                arguments.putInt(WoConstants.ARG_POSITION, _position);
                EditWorkFragment fragment = new EditWorkFragment();
                fragment.setArguments(arguments);
                ((AppCompatActivity) master).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.work_detail_container, fragment, WoConstants.FRG_WORK_EDIT)
                        .commit();

            }
            else
            {
                Intent i = new Intent(getActivity(), EditWorkActivity.class);
                i.putExtra(WoConstants.ARG_WORK, _work);
                i.putExtra(WoConstants.ARG_CLIENT_NAME, _clientName);
                i.putExtra(WoConstants.ARG_POSITION, _position);
                // Contrary to documentation
                // this.startActivityForResult(Intent, int);
                // does NOT call startActivityForResult() from the fragment's containing Activity.
                // Explicit call is needed to get control back to onActivityResult().
                getActivity().startActivityForResult(i, WoConstants.RC_EDIT_WORK);
            }
            break;

        case 2: // Mark as Done
        {
            _work.setStatus(Project.WorkStatus.DONE);

            // Update DB
            DatabaseHelper db = DatabaseHelper.getInstance(getActivity());
            db.updateWork(_work, null);

            // Inform subscribers
            WorkUpdateEvent event = new WorkUpdateEvent(_work, _position);
            EventBus.getDefault().postSticky(event);
            break;
        }
        }
    }
}
