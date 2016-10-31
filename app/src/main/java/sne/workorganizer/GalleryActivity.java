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
    private static abstract class GalleryAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH>
    {
        protected Activity _activity;

        GalleryAdapter(Activity activity)
        {
            _activity = activity;
        }
    }

    /////////////////////////////////////////////////////////////////////////////////
    private class GalleryAdapterResult extends GalleryAdapter<RowHolder>
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
        public RowHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            // create a new view
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.picture_row, parent, false);

            //GridLayoutManager.LayoutParams params = new GridLayoutManager.LayoutParams(parent.getContext(), attrs);
            //v.setLayoutParams(params);
            // TODO set the view's size, margins, paddings and layout parameters
            return new RowHolder(v, _activity);
        }

        @Override
        public void onBindViewHolder(RowHolder holder, int position)
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
    static class RowHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener
    {
        private Activity _activity;
        private Picture _picture;

        private ImageView _imageView;
        private TextView _workTitle;

        RowHolder(View itemView, Activity activity)
        {
            super(itemView);

            _activity = activity;
            _imageView = (ImageView) itemView.findViewById(R.id.photo);
            _workTitle = (TextView) itemView.findViewById(R.id.work_title);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        void bindModel(Picture picture)
        {
            _picture = picture;

            new FillPicture().execute();
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

        private class FillPicture extends AsyncTask<Void, Void, Void>
        {
            private Project _project;
            private Bitmap _bitmap;

            FillPicture()
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
                    _project = db.findProjectById(workId);
                }

                if (_picture.getResultPhoto() != null)
                {
                    _bitmap = PhotoUtils.getThumbnailBitmap(_picture.getResultPhoto(), WoConstants.WIDTH_MEDIUM);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result)
            {
                if (_project != null)
                {
                    _workTitle.setText(_project.getName());
                }

                if (_bitmap != null)
                {
                    _imageView.setImageBitmap(_bitmap);
                }
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
    static class RowHolderDesign extends RecyclerView.ViewHolder
            implements View.OnClickListener
    {
        private Activity _activity;
        private Project _work;

        private ImageView _imageView;
        private TextView _workTitle;

        RowHolderDesign(View itemView, Activity activity)
        {
            super(itemView);

            _activity = activity;
            _imageView = (ImageView) itemView.findViewById(R.id.photo);
            _workTitle = (TextView) itemView.findViewById(R.id.work_title);

            itemView.setOnClickListener(this);
        }

        void bindModel(Project work)
        {
            _work = work;

            new FillPicture().execute();
        }

        @Override
        public void onClick(View v)
        {
            Log.i(TAG, "onClick() called " + getAdapterPosition() + "; " + _work);
            if (_work == null) return;

            Mix.showPhoto(_activity, _work.getDesign());
        }

        private class FillPicture extends AsyncTask<Void, Void, Void>
        {
            private Bitmap _bitmap;

            FillPicture()
            {
                super();
            }

            @Override
            protected Void doInBackground(Void... params)
            {
                if (_work.getDesign() != null)
                {
                    _bitmap = PhotoUtils.getThumbnailBitmap(_work.getDesign(), WoConstants.WIDTH_MEDIUM);
                }
                return null;
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
        }
    }
}
