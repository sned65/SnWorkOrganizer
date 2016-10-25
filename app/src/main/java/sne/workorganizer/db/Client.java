package sne.workorganizer.db;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Single row in the Clients table.
 */
public class Client implements Parcelable, DbRow
{
    private String _id;
    private String _name;
    private String _phone;
    private String _email;
    private String _social;
    private List<Project> _projects = new ArrayList<>();

    public Client()
    {
        _id = UUID.randomUUID().toString();
    }

    public Client(String id, String name, String phone, String email, String social, List<Project> projects)
    {
        _id = id;
        _name = name;
        _phone = phone;
        _email = email;
        _social = social;
        if (projects != null)
        {
            _projects = projects;
        }
    }

    @Override
    public String toString()
    {
        return "Client["+_id+", "+_name+"]";
    }

    protected Client(Parcel in)
    {
        _id = in.readString();
        _name = in.readString();
        _phone = in.readString();
        _email = in.readString();
        _social = in.readString();

        Parcelable[] projArray = in.readParcelableArray(Project.class.getClassLoader());
        for (Parcelable p : projArray)
        {
            Project pr = (Project) p;
            _projects.add(pr);
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
        dest.writeString(_name);
        dest.writeString(_phone);
        dest.writeString(_email);
        dest.writeString(_social);

        Project[] projArray = _projects.toArray(new Project[0]);
        dest.writeParcelableArray(projArray, 0);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Client> CREATOR = new Parcelable.Creator<Client>()
    {
        @Override
        public Client createFromParcel(Parcel in)
        {
            return new Client(in);
        }

        @Override
        public Client[] newArray(int size)
        {
            return new Client[size];
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

    public String getName()
    {
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }

    public String getPhone()
    {
        return _phone;
    }

    public void setPhone(String phone)
    {
        _phone = phone;
    }

    public String getEmail()
    {
        return _email;
    }

    public void setEmail(String email)
    {
        _email = email;
    }

    public List<Project> getProjects()
    {
        return _projects;
    }

    public void setProjects(List<Project> projects)
    {
        _projects = projects;
    }

    public String getSocial()
    {
        return _social;
    }

    public void setSocial(String social)
    {
        _social = social;
    }
}
