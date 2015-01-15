package cz.cuni.lf1.lge.ThunderSTORM.util;

import cz.cuni.lf1.lge.ThunderSTORM.UI.MacroParser;
import ij.IJ;

import javax.swing.*;
import java.util.concurrent.ExecutionException;

/**
 * This class is used for post-processing modules, where some of the calls might be time consuming,
 * therefore they are performed in a separate thread to avoid blocking of GUI. However, if ran from
 * macro, the blocking call is desirable so that the next macro command waits until the current
 * operation is finished to avoid race conditions.
 */
public abstract class WorkerThread<T> {

    /**
     * Implement what the worker is supposed to do.
     *
     * @return result of the operation
     */
    protected abstract T doJob();

    /**
     * Implement what to do with the results of the operation.
     *
     * @param result result from `doJob()`
     */
    protected void finishJob(T result) {
        //
    }

    /**
     * Main method, which checks if the method was called from macro or directly from GUI.
     */
    public void execute() {
        if (MacroParser.isRanFromMacro()) { // blocking call
            finishJob(doJob());
            exFinally();
        } else {    // run in background
            new SwingWorker<T, Void>() {
                @Override
                public T doInBackground() {
                    return doJob();
                }

                @Override
                protected void done() {
                    try {
                        finishJob(get());
                    } catch (InterruptedException ex) {
                        exCatch(ex);
                    } catch (ExecutionException ex) {
                        exCatch(ex);
                    } finally {
                        exFinally();
                    }
                }
            }.execute();
        }
    }

    /**
     * Since exceptions can be catched only within the active thread, this method is
     * called when an exception is catched.
     *
     * @param ex an exception
     */
    public void exCatch(Throwable ex) {
        IJ.handleException(ex);
    }

    /**
     * This method is called automatically at the end of `finishJob()` as part of try-catch-finally block.
     * See `exCatch()`.
     */
    public void exFinally() {
        //
    }
}
