package sne.workorganizer;

import sne.workorganizer.db.Project;

/**
 * Created by Nikolay_Smirnov on 02.11.2016.
 */

public interface WorkListMaster
{
    boolean isTwoPane();
    void removeWork(int position);
    void updateWork(Project work, int position);
    void removeWorkEditFragment();
}
