package sne.workorganizer.eb;

import sne.workorganizer.db.Project;

/**
 * Created by Nikolay_Smirnov on 23.11.2016.
 */

public class WorkUpdateEvent
{
    private final Project _work;
    private final int _position;

    public WorkUpdateEvent(Project work, int position)
    {
        _work = work;
        _position = position;
    }

    public Project getWork()
    {
        return _work;
    }

    public int getPosition()
    {
         return _position;
    }
}
