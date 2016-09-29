package sne.workorganizer.db;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Single row in the Clients table.
 */
public class Client
{
    private String _id;
    private String _name;
    private String _phone;
    private String _email;
    private List<Project> _projects;

    public Client()
    {
        _id = UUID.randomUUID().toString();
    }

    public Client(String id, String name, String phone, String email)
    {
        this(id, name, phone, email, null);
    }

    public Client(String id, String name, String phone, String email, List<Project> projects)
    {
        _id = id;
        _name = name;
        _phone = phone;
        _email = email;
        if (projects == null)
        {
            _projects = new ArrayList<>();
        }
        else
        {
            _projects = projects;
        }
    }

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
}
