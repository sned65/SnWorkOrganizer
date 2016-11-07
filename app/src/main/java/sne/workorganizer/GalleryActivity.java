package sne.workorganizer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import sne.workorganizer.db.DatabaseHelper;
import sne.workorganizer.db.Picture;
import sne.workorganizer.db.Project;
import sne.workorganizer.util.Mix;
import sne.workorganizer.util.PhotoUtils;
import sne.workorganizer.util.WoConstants;

/**
 * The activity provides an interface for managing a collection of images.
 */
public class GalleryActivity extends AppCompatActivity
{
    private static final String TAG = GalleryActivity.class.getName();
    public static final String ARG_PICT_TYPE = "arg_pict_type";
    // Define the list of accepted constants and declare the PictureType annotation
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({PICT_BEFORE, PICT_AFTER})
    public @interface PictureType {}
    // Declare the constants
    public static final int PICT_BEFORE = 1;
    public static final int PICT_AFTER = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //toolbar.setTitle(getTitle());
        // add back arrow to toolbar
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            //getSupportActionBar().setDisplayShowHomeEnabled(true);
            //getSupportActionBar().setHomeButtonEnabled(true);
        }
        //_menu = toolbar.getMenu();

        RecyclerView photoGallery = (RecyclerView) findViewById(R.id.photo_gallery);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        photoGallery.setHasFixedSize(true);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);//, GridLayoutManager.HORIZONTAL, false);
        photoGallery.setLayoutManager(layoutManager);

        int type = getIntent().getIntExtra(ARG_PICT_TYPE, -1);

        GalleryAdapter adapter;
        if (type == PICT_BEFORE)
        {
            //toolbar.setTitle(R.string.menu_gallery_design);
            setTitle(R.string.menu_gallery_design);
            adapter = new GalleryAdapterDesign(this);
        }
        else
        {
            //toolbar.setTitle(R.string.menu_gallery_work);
            setTitle(R.string.menu_gallery_work);
            adapter = new GalleryAdapterResult(this);
        }
        photoGallery.setAdapter(adapter);
    }

    private void showGallery()
    {
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        findViewById(R.id.photo_gallery).setVisibility(View.VISIBLE);
    }

    public void removeItem(int position)
    {
        RecyclerView rv = (RecyclerView) findViewById(R.id.photo_gallery);
        GalleryAdapterResult adapter = (GalleryAdapterResult) rv.getAdapter();
        adapter.removePicture(position);
        adapter.notifyItemRemoved(position);
    }

    /////////////////////////////////////////////////////////////////////////////////
    private static abstract class GalleryAdapter<VH extends RowHolder> extends RecyclerView.Adapter<VH>
    {
        final Activity _activity;

        GalleryAdapter(Activity activity)
        {
            _activity = activity;
        }
    }

    /////////////////////////////////////////////////////////////////////////////////
    static abstract class RowHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener
    {
        final Activity _activity;

        final ImageView _imageView;
        final TextView _workTitle;

        RowHolder(View itemView, Activity activity)
        {
            super(itemView);

            _activity = activity;
            _imageView = (ImageView) itemView.findViewById(R.id.photo);
            _workTitle = (TextView) itemView.findViewById(R.id.work_title);

            itemView.setOnClickListener(this);
        }

        abstract class FillPicture extends AsyncTask<Void, Void, Void>
        {
            private Project _work;
            private Bitmap _bitmap;

            FillPicture()
            {
                super();
            }

            FillPicture(Project work)
            {
                super();
                _work = work;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                if (_work != null)
                {
                    _workTitle.setText(_work.getName());
                }

                if (_bitmap != null)
                {
                    _imageView.setImageBitmap(_bitmap);
                }
            }

            protected Project getWork()
            {
                return _work;
            }

            protected void setWork(Project work)
            {
                _work = work;
            }

            protected Bitmap getBitmap()
            {
                return _bitmap;
            }

            protected void setBitmap(Bitmap bitmap)
            {
                _bitmap = bitmap;
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////
    private class GalleryAdapterResult extends GalleryAdapter<RowHolderResult>
    {
        private List<Picture> _pictures;

        GalleryAdapterResult(Activity activity)
        {
            super(activity);
            DatabaseHelper db = DatabaseHelper.getInstance(_activity);
            db.findAllPictures(new DatabaseHelper.DbSelectPicturesCallback()
            {
                @Override
                public void onSelectFinished(ArrayList<Picture> records)
                {
                    Log.i(TAG, "onSelectFinished() " + records + ", size = " + ((records == null) ? "" : records.size()));
                    _pictures = records;
                    showGallery();
                }
            });
        }

        @Override
        public RowHolderResult onCreateViewHolder(ViewGroup parent, int viewType)
        {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.picture_row, parent, false);

            //GridLayoutManager.LayoutParams params = new GridLayoutManager.LayoutParams(parent.getContext(), attrs);
            //v.setLayoutParams(params);
            // TODO set the view's size, margins, paddings and layout parameters
            return new RowHolderResult(v, _activity);
        }

        @Override
        public void onBindViewHolder(RowHolderResult holder, int position)
        {
            Log.i(TAG, "onBindViewHolder("+position+") "+_pictures.get(position));
            holder.bindModel(_pictures.get(position));
        }

        @Override
        public int getItemCount()
        {
            return _pictures.size();
        }

        void removePicture(int position)
        {
            _pictures.remove(position);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////
    static class RowHolderResult extends RowHolder
            implements View.OnLongClickListener
    {
        private Picture _picture;

        RowHolderResult(View itemView, Activity activity)
        {
            super(itemView, activity);

            itemView.setOnLongClickListener(this);
        }

        void bindModel(Picture picture)
        {
            _picture = picture;

            new FillPictureResult().execute();
        }

        @Override
        public void onClick(View v)
        {
            Log.i(TAG, "onClick() called " + getAdapterPosition() + "; " + _picture);
            if (_picture == null) return;

            Mix.showPhoto(_activity, _picture.getResultPhoto());
        }

        @Override
        public boolean onLongClick(View v)
        {
            Log.i(TAG, "onLongClick() called " + getAdapterPosition() + "; " + _picture);
            if (_picture == null)
            {
                return true;
            }

            PictureActionsFragment.newInstance(_picture, getAdapterPosition()).show(_activity.getFragmentManager(), "picture_actions");
            return true;
        }

        private class FillPictureResult extends FillPicture
        {
            FillPictureResult()
            {
                super();
            }

            @Override
            protected Void doInBackground(Void... params)
            {
                DatabaseHelper db = DatabaseHelper.getInstance(_activity);
                String workId = _picture.getWorkId();
                if (workId != null)
                {
                    setWork(db.findProjectById(workId));
                }

                if (_picture.getResultPhoto() != null)
                {
                    setBitmap(PhotoUtils.getThumbnailBitmap(_picture.getResultPhoto(), WoConstants.WIDTH_MEDIUM));
                }
                return null;
            }
        }
    }

    /////////////////////////////////////////////////////////////////////////////////
    private class GalleryAdapterDesign extends GalleryAdapter<RowHolderDesign>
    {
        private List<Project> _projects;

        GalleryAdapterDesign(Activity activity)
        {
            super(activity);
            DatabaseHelper db = DatabaseHelper.getInstance(_activity);
            db.findAllProjectsWithDesign(new DatabaseHelper.DbSelectProjectsCallback()
            {
                @Override
                public void onSelectFinished(ArrayList<Project> records)
                {
                    Log.i(TAG, "onSelectFinished() size = " + ((records == null) ? "" : records.size())+ ": " + records);
                    _projects = records;
                    showGallery();
                }
            });
        }

        @Override
        public RowHolderDesign onCreateViewHolder(ViewGroup parent, int viewType)
        {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.picture_row, parent, false);

            //GridLayoutManager.LayoutParams params = new GridLayoutManager.LayoutParams(parent.getContext(), attrs);
            //v.setLayoutParams(params);
            // TODO set the view's size, margins, paddings and layout parameters
            return new RowHolderDesign(v, _activity);
        }

        @Override
        public void onBindViewHolder(RowHolderDesign holder, int position)
        {
            Log.i(TAG, "onBindViewHolder("+position+")");
            holder.bindModel(_projects.get(position));
        }

        @Override
        public int getItemCount()
        {
            return _projects.size();
        }
    }

    //////////////////////////////////////////////////////////////////////
    static class RowHolderDesign extends RowHolder
    {
        private Project _work;

        RowHolderDesign(View itemView, Activity activity)
        {
            super(itemView, activity);
        }

        void bindModel(Project work)
        {
            _work = work;

            new FillPictureDesign(_work).execute();
        }

        @Override
        public void onClick(View v)
        {
            Log.i(TAG, "onClick() called " + getAdapterPosition() + "; " + _work);
            if (_work == null) return;

            Mix.showPhoto(_activity, _work.getDesign());
        }

        private class FillPictureDesign extends FillPicture
        {
            FillPictureDesign(Project work)
            {
                super(work);
            }

            @Override
            protected Void doInBackground(Void... params)
            {
                if (_work.getDesign() != null)
                {
                    setBitmap(PhotoUtils.getThumbnailBitmap(_work.getDesign(), WoConstants.WIDTH_MEDIUM));
                }
                return null;
            }
        }
    }
}
