package sne.workorganizer.db;

import java.util.Date;
import java.util.UUID;

/**
 * Single row in the Projects table.
 */
public class Project
{
    private String _id;
    private String _clientId;
    private String _name;
    // SQLite does not have a storage class set aside for storing dates and/or times.
    // Instead, dates and times can be stored as ISO8601 strings ("YYYY-MM-DD HH:MM:SS.SSS").
    private String _date;
    private String _status;

    public Project()
    {
        _id = UUID.randomUUID().toString();
    }

    public Project(String id, String clientId, String name, String date, String status)
    {
        _id = id;
        _clientId = clientId;
        _name = name;
        _date = date;
        _status = status;
    }

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

    public String getDate()
    {
        return _date;
    }

    public void setDate(String date)
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
}
