package sne.workorganizer;

import sne.workorganizer.db.Picture;
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
     * @param result result picture. If {@code null}, then the argument will be ignored.
     *               To remove the result picture, pass a {@code Picture} with
     *               {@code null} reference to file.
     */
    void updateWork(Project work, Picture result);

    /**
     * Removes a fragment responsible for modification of work.
     */
    void removeWorkEditFragment();
}
