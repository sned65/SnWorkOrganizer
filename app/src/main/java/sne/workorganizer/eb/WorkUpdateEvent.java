package sne.workorganizer.eb;

import sne.workorganizer.db.Project;

/**
 * Created by Nikolay_Smirnov on 23.11.2016.
 */

public class WorkUpdateEvent
{
    private final Project _work;

    public WorkUpdateEvent(Project work)
    {
        _work = work;
    }

    public Project getWork()
    {
        return _work;
    }
}
