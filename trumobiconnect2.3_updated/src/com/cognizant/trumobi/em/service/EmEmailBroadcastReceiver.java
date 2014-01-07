

package com.cognizant.trumobi.em.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * The broadcast receiver.  The actual job is done in EmailBroadcastProcessor on a worker thread.
 */
public class EmEmailBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        EmEmailBroadcastProcessorService.processBroadcastIntent(context, intent);
    }
}
