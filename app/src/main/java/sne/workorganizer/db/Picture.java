package sne.workorganizer.db;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

/**
 * Single row in the Pictures table.
 */
public class Picture implements DbRow, Parcelable
{
    private String _id;
    private String _workId;
    private String _resultPhoto;

    public Picture()
    {
        _id = UUID.randomUUID().toString();
    }

    public Picture(String id, String workId, String resultPhoto)
    {
        _id = id;
        _workId = workId;
        _resultPhoto = resultPhoto;
    }

    protected Picture(Parcel in)
    {
        _id = in.readString();
        _workId = in.readString();
        _resultPhoto = in.readString();
    }

    public String toString()
    {
        return "Picture["+_id+", "+_workId+", "+_resultPhoto+"]";
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(_id);
        dest.writeString(_workId);
        dest.writeString(_resultPhoto);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Picture> CREATOR = new Parcelable.Creator<Picture>()
    {
        @Override
        public Picture createFromParcel(Parcel in)
        {
            return new Picture(in);
        }

        @Override
        public Picture[] newArray(int size)
        {
            return new Picture[size];
        }
    };

    public String getId()
    {
        return _id;
    }

    public void setId(String id)
    {
        _id = id;
    }

    public String getWorkId()
    {
        return _workId;
    }

    public void setWorkId(String workId)
    {
        _workId = workId;
    }

    public String getResultPhoto()
    {
        return _resultPhoto;
    }

    public void setResultPhoto(String resultPhoto)
    {
        _resultPhoto = resultPhoto;
    }
}
