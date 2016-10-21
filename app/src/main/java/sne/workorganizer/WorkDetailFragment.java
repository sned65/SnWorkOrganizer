package sne.workorganizer;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

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

    private View _rootView;

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

        if (getArguments().containsKey(WoConstants.ARG_WORK))
        {
            // Load the content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            _project = getArguments().getParcelable(WoConstants.ARG_WORK);

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
        _rootView = inflater.inflate(R.layout.work_detail, container, false);
        if (_project == null) return _rootView;

        fillViews();

        return _rootView;
    }

    private void fillViews()
    {
        ((TextView) _rootView.findViewById(R.id.work_date_time)).setText(_project.getDateTimeString());
        ((TextView) _rootView.findViewById(R.id.work_title)).setText(_project.getName());
        String price = _project.getPrice() == null ? "" : _project.getPrice().toString();
        ((TextView) _rootView.findViewById(R.id.work_price)).setText(price);

        DatabaseHelper db = DatabaseHelper.getInstance(getActivity());
        Client client = db.findClientById(_project.getClientId());
        ((TextView) _rootView.findViewById(R.id.work_client_name)).setText(client.getName());
        ((TextView) _rootView.findViewById(R.id.client_phone)).setText(client.getPhone());
        ((TextView) _rootView.findViewById(R.id.client_email)).setText(client.getEmail());
        ((TextView) _rootView.findViewById(R.id.client_social)).setText(client.getSocial());

        ImageView designView = (ImageView) _rootView.findViewById(R.id.work_design);
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
            c.close();
            //Log.i(TAG, "onCreateView() path = "+path);
            //designView.setImageURI(designUri);

            try
            {
                final int BITMAP_WIDTH = 256;
                InputStream is = resolver.openInputStream(designUri);
                Bitmap bm = BitmapFactory.decodeStream(is);
                assert is != null;
                is.close();
                int nh = (int) (bm.getHeight() * ((float) BITMAP_WIDTH / bm.getWidth()));
                Bitmap scaled = Bitmap.createScaledBitmap(bm, BITMAP_WIDTH, nh, true);
                designView.setImageBitmap(scaled);
            }
            catch (FileNotFoundException e)
            {
                Toast.makeText(getActivity(), "File not found: "+designStr, Toast.LENGTH_LONG).show();
            }
            catch (IOException e)
            {
                Log.e(TAG, "onCreateView() Close stream error for "+designStr);
                e.printStackTrace();
            }
        }
    }

    public void setWork(Project work)
    {
        _project = work;
        if (_project == null)
        {
            // TODO
            return;
        }

        fillViews();
    }
}
