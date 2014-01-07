

package com.cognizant.trumobi.exchange;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * MailboxAlarmReceiver is used to "wake up" the SyncManager at the appropriate time(s).  It may
 * also be used for individual sync adapters, but this isn't implemented at the present time.
 *
 */
public class EmMailboxAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        long mailboxId = intent.getLongExtra("mailbox", EmSyncManager.SYNC_MANAGER_ID);
        // SYNC_MANAGER_SERVICE_ID tells us that the service is asking to be started
        if (mailboxId == EmSyncManager.SYNC_MANAGER_SERVICE_ID) {
            context.startService(new Intent(context, EmSyncManager.class));
        } else {
            EmSyncManager.log("Alarm received for: " + EmSyncManager.alarmOwner(mailboxId));
            EmSyncManager.alert(context, mailboxId);
        }
    }
}

