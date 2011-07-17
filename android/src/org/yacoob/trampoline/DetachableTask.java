package org.yacoob.trampoline;

import android.app.Activity;
import android.os.AsyncTask;

public abstract class DetachableTask<Params, Progress, Result, ActivityType extends Activity>
        extends AsyncTask<Params, Progress, Result> {

    private ActivityType parentActivity = null;

    /**
     * Associate task to activity.
     * 
     * @param activity
     *            Activity to attach to.
     */
    void attach(final ActivityType activity) {
        parentActivity = activity;
    }

    /**
     * Disassociate task from activity.
     */
    void detach() {
        parentActivity = null;
    }

    /**
     * Get current parent activity this task is assigned to.
     * 
     * @return Parent activity for this task.
     */
    protected ActivityType getParentActivity() {
        return parentActivity;
    }
}
