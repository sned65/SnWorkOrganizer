package sne.workorganizer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import sne.workorganizer.db.Client;
import sne.workorganizer.db.DatabaseHelper;
import sne.workorganizer.db.Picture;
import sne.workorganizer.db.Project;
import sne.workorganizer.util.InfoDialogFragment;
import sne.workorganizer.util.Mix;
import sne.workorganizer.util.PhotoUtils;
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
    private Client _client;

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
        _client = db.findClientById(_project.getClientId());
        fillClient();

        ImageView designView = (ImageView) _rootView.findViewById(R.id.work_design);
        final String designStr = _project.getDesign();
        //Log.i(TAG, "onCreateView() designStr = "+designStr);
        if (designStr != null)
        {
            //PhotoUtils.setScaledBitmap(getActivity(), designView, designStr, BITMAP_WIDTH);
            Bitmap bm = PhotoUtils.getThumbnailBitmap(designStr, WoConstants.WIDTH_THUMBNAIL);
            if (bm != null)
            {
                designView.setImageBitmap(bm);
                designView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Mix.showPhoto(getActivity(), designStr);
                    }
                });
            }
            else
            {
                String msg = getString(R.string.design)+" "+getString(R.string.error_file_not_found) + ":" + designStr;
                Mix.showError(getActivity(), msg, InfoDialogFragment.Severity.WARNING);
            }
        }

        Picture resultPhoto = db.findPictureByWorkId(_project.getId());
        if (resultPhoto == null || resultPhoto.getResultPhoto() == null)
        {
            View result = _rootView.findViewById(R.id.detail_row_result);
            result.setVisibility(View.GONE);
        }
        else
        {
            View result = _rootView.findViewById(R.id.detail_row_result);
            result.setVisibility(View.VISIBLE);

            ImageView resultView = (ImageView) _rootView.findViewById(R.id.work_result);
            final String photo = resultPhoto.getResultPhoto();
            Bitmap bm = PhotoUtils.getThumbnailBitmap(photo, WoConstants.WIDTH_THUMBNAIL);
            if (bm != null)
            {
                resultView.setImageBitmap(bm);
                resultView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Mix.showPhoto(getActivity(), photo);
                    }
                });
            }
            else
            {
                String msg = getString(R.string.error_file_not_found) + ":" + resultPhoto.getResultPhoto();
                Mix.showError(getActivity(), msg, InfoDialogFragment.Severity.WARNING);
            }
        }
    }

    private void fillClient()
    {
        ((TextView) _rootView.findViewById(R.id.work_client_name)).setText(_client.getName());
        TextView phoneView = (TextView) _rootView.findViewById(R.id.client_phone);
        phoneView.setText(_client.getPhone());
        TextView emailView = (TextView) _rootView.findViewById(R.id.client_email);
        emailView.setText(_client.getEmail());
        TextView socialView = (TextView) _rootView.findViewById(R.id.client_social);
        socialView.setText(_client.getSocial());

        Mix.setupClientCommunications(getActivity(), phoneView, emailView, socialView);
    }

    public Project getWork()
    {
        return _project;
    }

    public void setWork(Project work)
    {
        _project = work;
        fillViews();
    }

    public Client getClient()
    {
        return _client;
    }

    public void updateClient(Client client)
    {
        _client = client;
        fillClient();
    }
}
