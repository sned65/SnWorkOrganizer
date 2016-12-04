package sne.workorganizer.db;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * Single row in the Projects table.
 */
public class Project implements Parcelable, Cloneable, DbRow
{

    /**
     * List of work statuses.
     */
    public enum WorkStatus
    {
        CREATED, DONE;

        public static WorkStatus fromString(String text)
        {
            if (TextUtils.isEmpty(text))
            {
                return CREATED;
            }
            else
            {
                return WorkStatus.valueOf(text);
            }
        }
    }

    private static final String TAG = Project.class.getName();
    private static final String DATETIME_FORMAT = "dd MMM yyyy HH:mm";
    private static final String DATETIME_FORMAT_2 = "dd MMM yyyy HH:mm,   EEE";
    private static final String TIME_FORMAT = "HH:mm";

    private static final String DUMMY_ID = "DUMMY";
    public static final Project DUMMY = new Project(DUMMY_ID);

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

    public static boolean isDummy(Project project)
    {
        return DUMMY_ID.equals(project.getId());
    }

    public Project()
    {
        _id = UUID.randomUUID().toString();
    }

    @SuppressWarnings("SameParameterValue")
    private Project(String id)
    {
        _id = id;
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

    @Override
    public Project clone()
    {
        try
        {
            return (Project) super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString()
    {
        if (isDummy(this))
        {
            return "Work[DUMMY]";
        }
        String date = _date == null ? "" : (new Date(_date)).toString();
        return "Work["+_id + ", " + _name + ", " + date + " for " + _clientId + ", " + _status + ", design " + _design + "]";
    }

    /**
     *
     * @return work date and time string formatted as "dd MMM yyyy HH:mm",
     * or empty string.
     */
    public String getDateTimeString()
    {
        if (_date == null) return "";

        Date date = new Date(_date);
        SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT, Locale.getDefault());
        return sdf.format(date);
    }

    /**
     *
     * @return work date and time string formatted as "dd MMM yyyy HH:mm   EEE",
     * or empty string.
     */
    public String getDateTimeString2()
    {
        if (_date == null) return "";

        Date date = new Date(_date);
        SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT_2, Locale.getDefault());
        return sdf.format(date);
    }

    /**
     *
     * @return work time string formatted as "HH:mm",
     * or empty string.
     */
    public String getTimeString()
    {
        if (_date == null) return "";

        Date date = new Date(_date);
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
        return sdf.format(date);
    }

    protected Project(Parcel in)
    {
        Log.d(TAG, "*** Project(Parcel in) called ***");
        _id = in.readString();
        _clientId = in.readString();
        _name = in.readString();
        _date = in.readLong();
        _status = in.readString();
        _design = in.readString();
        int price = in.readInt();
        if (price < 0)
        {
            _price = null;
        }
        else
        {
            _price = price;
        }
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
        if (_price == null)
        {
            dest.writeInt(-1);
        }
        else
        {
            dest.writeInt(_price);
        }
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

    public WorkStatus getStatus()
    {
        if (_status == null)
        {
            return WorkStatus.CREATED;
        }
        return WorkStatus.valueOf(_status);
    }

    public void setStatus(WorkStatus status)
    {
        _status = status.toString();
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
