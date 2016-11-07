package sne.workorganizer;

import sne.workorganizer.db.Project;

/**
 * Common interface for Work List.
 */
interface WorkListMaster
{
    boolean isTwoPane();
    void removeWork(int position);
    void updateWork(Project work, int position);
    void removeWorkEditFragment();
}
