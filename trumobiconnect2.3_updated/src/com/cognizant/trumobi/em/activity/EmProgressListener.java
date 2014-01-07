

package com.cognizant.trumobi.em.activity;

import android.content.Context;

/**
 * A listener that the user can register for global, persistent progress events.
 */
public interface EmProgressListener {
    /**
     * @param context
     * @param title
     * @param message
     * @param currentProgress
     * @param maxProgress
     * @param indeterminate
     */
    void showProgress(Context context, String title, String message, long currentProgress,
            long maxProgress, boolean indeterminate);

    /**
     * @param context
     * @param title
     * @param message
     * @param currentProgress
     * @param maxProgress
     * @param indeterminate
     */
    void updateProgress(Context context, String title, String message, long currentProgress,
            long maxProgress, boolean indeterminate);

    /**
     * @param context
     */
    void hideProgress(Context context);
}
