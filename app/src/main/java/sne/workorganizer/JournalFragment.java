package sne.workorganizer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * A fragment containing journal of works.
 */
public class JournalFragment extends Fragment
{
    private static final String TAG = JournalFragment.class.getSimpleName();

    private CalendarView _calendarView;

    /**
     * Returns a new instance of JournalFragment.
     */
    public static JournalFragment newInstance()
    {
        JournalFragment fragment = new JournalFragment();
//        Bundle args = new Bundle();
//        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.journal, container, false);

        _calendarView = (CalendarView) rootView.findViewById(R.id.calendarView);

        _calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener()
        {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth)
            {
                changeDate(year, month, dayOfMonth);
            }
        });

        TextView textView = (TextView) rootView.findViewById(R.id.section_label);
        textView.setText("*** List of works! ***");
        return rootView;
    }

    private void changeDate(int year, int month, int dayOfMonth)
    {
        // TODO
        Log.i(TAG, "changeDate("+year+", "+month+", "+dayOfMonth+") called");
        Calendar then = new GregorianCalendar(year, month, dayOfMonth);
        Toast.makeText(getActivity(), then.getTime().toString(), Toast.LENGTH_LONG)
                .show();
    }
}
