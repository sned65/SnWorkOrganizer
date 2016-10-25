package sne.workorganizer.db;

import java.util.UUID;

/**
 * Single row in the Pictures table.
 */
public class Picture implements DbRow
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
