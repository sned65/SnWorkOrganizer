package sne.workorganizer.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import sne.workorganizer.R;

/**
 * Dialog Fragment for information/warning/error messages.
 */
public class InfoDialogFragment extends DialogFragment
{
    public static final String ARG_MESSAGE = "message";
    public static final String ARG_SEVERITY = "severity";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder msgBox = new AlertDialog.Builder(getActivity());
        Bundle args = getArguments();
        String msg = args.getString(ARG_MESSAGE);
        String sev = args.getString(ARG_SEVERITY);
        if (sev != null)
        {
            Severity severity = Severity.valueOf(sev);
            switch (severity)
            {
            case ERROR:
                msgBox.setTitle("Error");
                msgBox.setIcon(R.drawable.ic_warning_red_24dp);
                break;
            case WARNING:
                msgBox.setTitle("Warning");
                msgBox.setIcon(R.drawable.ic_warning_yellow_24dp);
                break;
            case INFO:
                msgBox.setTitle("Information");
                break;
            }
        }
        msgBox.setMessage(msg);
        return msgBox.create();
    }

    public enum Severity
    {
        INFO, WARNING, ERROR
    }
}
