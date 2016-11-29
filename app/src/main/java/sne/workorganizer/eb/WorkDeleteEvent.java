package sne.workorganizer.eb;

import sne.workorganizer.db.Project;

/**
 * Created by Nikolay_Smirnov on 23.11.2016.
 */

public class WorkDeleteEvent
{
    private final Project _work;

    public WorkDeleteEvent(Project work)
    {
        _work = work;
    }

    public Project getWork()
    {
        return _work;
    }
}
