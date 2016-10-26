package sne.workorganizer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import sne.workorganizer.db.DatabaseHelper;
import sne.workorganizer.db.Picture;
import sne.workorganizer.db.Project;
import sne.workorganizer.util.Mix;

/**
 * The activity provides an interface for managing a collection of images.
 */
public class GalleryActivity extends AppCompatActivity
{
    private static final String TAG = GalleryActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());
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

        GalleryAdapter adapter = new GalleryAdapter(this);
        photoGallery.setAdapter(adapter);
    }

    private void showGallery()
    {
        findViewById(R.id.progressBar).setVisibility(View.GONE);
        findViewById(R.id.photo_gallery).setVisibility(View.VISIBLE);
    }

    private class GalleryAdapter extends RecyclerView.Adapter<RowHolder>
    {
        private Activity _activity;
        private List<Picture> _pictures;

        public GalleryAdapter(Activity activity)
        {
            _activity = activity;
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
            Log.i(TAG, "onBindViewHolder("+position+")");
            holder.bindModel(_pictures.get(position));
        }

        @Override
        public int getItemCount()
        {
            return _pictures.size();
        }
    }

    static class RowHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener
    {
        private Activity _activity;
        private Picture _picture;

        private ImageView _imageView;
        private TextView _workTitle;

        public RowHolder(View itemView, Activity activity)
        {
            super(itemView);

            _activity = activity;
            _imageView = (ImageView) itemView.findViewById(R.id.photo);
            _workTitle = (TextView) itemView.findViewById(R.id.work_title);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        public void bindModel(Picture picture)
        {
            _picture = picture;

            new FillPicture().execute();
        }

        @Override
        public void onClick(View v)
        {
            Log.i(TAG, "onClick() called " + getAdapterPosition() + "; " + _picture);
            if (_picture == null)
            {
                return;
            }

            // TODO show big picture
        }

        @Override
        public boolean onLongClick(View v)
        {
            Log.i(TAG, "onLongClick() called " + getAdapterPosition() + "; " + _picture);
            if (_picture == null)
            {
                return true;
            }

            // TODO picture actions
            //PictureActionsFragment.newInstance(_picture, getAdapterPosition()).show(_activity.getFragmentManager(), "picture_actions");
            return true;
        }

        private class FillPicture extends AsyncTask<Void, Void, Void>
        {
            private Project _project;
            private Bitmap _bitmap;

            public FillPicture()
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

                _bitmap = Mix.getThumbnailBitmap(_picture.getResultPhoto(), 256);
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
}
