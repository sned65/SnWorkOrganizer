package sne.workorganizer;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;

import sne.workorganizer.db.Client;
import sne.workorganizer.db.DatabaseHelper;
import sne.workorganizer.db.Project;
import sne.workorganizer.util.WoConstants;

/**
 * A fragment representing a single Work detail screen.
 * This fragment is either contained in a {@link WorkListActivity}
 * in two-pane mode (on tablets) or a {@link WorkDetailActivity}
 * on handsets.
 */
public class WorkDetailFragment extends Fragment
{
    private static final String TAG = WorkDetailFragment.class.getName();
    /**
     * The content this fragment is presenting.
     */
    private Project _project;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public WorkDetailFragment()
    {
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(WoConstants.ARG_PROJECT))
        {
            // Load the content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            _project = getArguments().getParcelable(WoConstants.ARG_PROJECT);

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null)
            {
                appBarLayout.setTitle(_project.getName());
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.work_detail, container, false);
        if (_project == null) return rootView;

        ((TextView) rootView.findViewById(R.id.work_date_time)).setText(_project.getDateTimeString());
        ((TextView) rootView.findViewById(R.id.work_title)).setText(_project.getName());
        String price = _project.getPrice() == null ? "" : _project.getPrice().toString();
        ((TextView) rootView.findViewById(R.id.work_price)).setText(price);

        DatabaseHelper db = DatabaseHelper.getInstance(getActivity());
        Client client = db.findClientById(_project.getClientId());
        ((TextView) rootView.findViewById(R.id.work_client_name)).setText(client.getName());
        ((TextView) rootView.findViewById(R.id.client_phone)).setText(client.getPhone());
        ((TextView) rootView.findViewById(R.id.client_email)).setText(client.getEmail());
        ((TextView) rootView.findViewById(R.id.client_social)).setText(client.getSocial());

        ImageView designView = (ImageView) rootView.findViewById(R.id.work_design);
        String designStr = _project.getDesign();
        Log.i(TAG, "onCreateView() designStr = "+designStr);
        if (designStr != null)
        {
            Uri designUri = Uri.parse(designStr);
            ContentResolver resolver = getActivity().getContentResolver();
            String[] projection = new String[] { OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE };
            Cursor c = resolver.query(designUri, projection, null, null, null);
            while (c.moveToNext())
            {
                int name_idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                String name = c.getString(name_idx);
                int size_idx = c.getColumnIndex(OpenableColumns.SIZE);
                Long size = c.getLong(size_idx);
                Log.i(TAG, "onCreateView() name = "+name+", size = "+size);
            }
            //Log.i(TAG, "onCreateView() path = "+path);
            //designView.setImageURI(designUri);

            InputStream is = null;
            try
            {
                final int BITMAP_WIDTH = 256;
                is = resolver.openInputStream(designUri);
                Bitmap bm = BitmapFactory.decodeStream(is);
                int nh = (int) (bm.getHeight() * ((float) BITMAP_WIDTH / bm.getWidth()));
                Bitmap scaled = Bitmap.createScaledBitmap(bm, BITMAP_WIDTH, nh, true);
                designView.setImageBitmap(scaled);
            }
            catch (FileNotFoundException e)
            {
                Toast.makeText(getActivity(), "File not found: "+designStr, Toast.LENGTH_LONG).show();
            }
        }

        return rootView;
    }
}
