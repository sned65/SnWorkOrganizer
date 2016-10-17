package sne.workorganizer.db;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
import java.util.UUID;

/**
 * Single row in the Projects table.
 */
public class Project implements Parcelable
{
    private String _id;
    private String _clientId;
    private String _name;
    // SQLite does not have a storage class set aside for storing dates and/or times.
    // Instead, dates and times can be stored as Unix Time, the number of seconds
    // since 1970-01-01 00:00:00 UTC.
    private Long _date;
    private String _status;
    private String _design;
    private Integer _price;

    public Project()
    {
        _id = UUID.randomUUID().toString();
    }

    public Project(String id, String clientId, String name, Long date, String status, String design, Integer price)
    {
        _id = id;
        _clientId = clientId;
        _name = name;
        _date = date;
        _status = status;
        _design = design;
        _price = price;
    }

    protected Project(Parcel in)
    {
        _id = in.readString();
        _clientId = in.readString();
        _name = in.readString();
        _date = in.readLong();
        _status = in.readString();
        _design = in.readString();
        _price = in.readInt();
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
        dest.writeString(_clientId);
        dest.writeString(_name);
        dest.writeLong(_date);
        dest.writeString(_status);
        dest.writeString(_design);
        dest.writeInt(_price);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Project> CREATOR = new Parcelable.Creator<Project>()
    {
        @Override
        public Project createFromParcel(Parcel in)
        {
            return new Project(in);
        }

        @Override
        public Project[] newArray(int size)
        {
            return new Project[size];
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

    public String getClientId()
    {
        return _clientId;
    }

    public void setClientId(String clientId)
    {
        _clientId = clientId;
    }

    public String getName()
    {
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }

    public Long getDate()
    {
        return _date;
    }

    public void setDate(Long date)
    {
        _date = date;
    }

    public String getStatus()
    {
        return _status;
    }

    public void setStatus(String status)
    {
        _status = status;
    }

    public String getDesign()
    {
        return _design;
    }

    public void setDesign(String design)
    {
        _design = design;
    }

    public Integer getPrice()
    {
        return _price;
    }

    public void setPrice(Integer price)
    {
        _price = price;
    }
}
