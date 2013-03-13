package org.yacoob.trampoline;

import android.app.Activity;
import android.os.AsyncTask;

/**
 * This is a subclass of AsyncTask that keeps track of the activity that created it. You should call
 * attach() from constructor and detach in onPostExecute. Parent activity will call attach/detach as
 * needed (ie. during screen rotate). @see <a href="http://bit.ly/lWbHjZ">More details</a>.
 * 
 * @param <Params>
 *            the type of the parameters sent to the task upon execution.
 * @param <Progress>
 *            the type of the progress units published during the background computation.
 * @param <Result>
 *            the type of the result of the background computation.
 * @param <ActivityType>
 *            the type of Activity this AsyncTask is attached to.
 */
public abstract class DetachableTask<Params, Progress, Result, ActivityType extends Activity>
        extends AsyncTask<Params, Progress, Result> {

    /** Parent to which this AsyncTask is attached. */
    private ActivityType parentActivity = null;

    /**
     * Associate task to activity.
     * 
     * @param activity
     *            Activity to attach to.
     */
    final void attach(final ActivityType activity) {
        parentActivity = activity;
    }

    /**
     * Disassociate task from activity.
     */
    // CSOFF: DesignForExtension
    void detach() {
        // CSON: DesignForExtension
        parentActivity = null;
    }

    /**
     * Get current parent activity this task is assigned to.
     * 
     * @return Parent activity for this task.
     */
    final ActivityType getParentActivity() {
        return parentActivity;
    }
}
