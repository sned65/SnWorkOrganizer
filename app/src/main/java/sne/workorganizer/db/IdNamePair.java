package sne.workorganizer.db;

/**
 * Class containing a pair of object id and title (name).
 * Used for {@code AutoCompleteTextView}.
 */
public class IdNamePair
{
    private String _id;
    private String _name;

    public IdNamePair() {}

    public IdNamePair(String id, String name)
    {
        _id = id;
        _name = name;
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
}
