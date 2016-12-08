package sne.workorganizer;

import sne.workorganizer.db.Project;

/**
 * Common interface for Work List.
 */
interface WorkListMaster
{
    /**
     *
     * @return Whether or not the activity is in two-pane mode,
     * i.e. running on a tablet device.
     */
    boolean isTwoPane();

    /**
     *
     * @param flag if {@code true} then show progress bar,
     *             otherwise hide.
     */
    void showProgressBar(boolean flag);

    /**
     * Remove work from the UI.
     *
     * @param position position of work in the list
     */
    void removeWork(int position);

    /**
     * Update work data in the UI.
     *
     * @param work updated work
     * @param position position of work in the list
     */
    void updateWork(Project work, int position);

    /**
     * Removes a fragment responsible for modification of work.
     */
    void removeWorkEditFragment();
}
