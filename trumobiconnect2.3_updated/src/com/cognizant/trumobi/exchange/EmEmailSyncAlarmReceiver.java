

package com.cognizant.trumobi.exchange;

import com.cognizant.trumobi.em.provider.EmEmailContent.Message;
import com.cognizant.trumobi.em.provider.EmEmailContent.MessageColumns;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import java.util.ArrayList;

/**
 * EmailSyncAlarmReceiver (USAR) is used by the SyncManager to start up-syncs of user-modified data
 * back to the Exchange server.
 *
 * Here's how this works for Email, for example:
 *
 * 1) User modifies or deletes an email from the UI.
 * 2) SyncManager, which has a ContentObserver watching the Message class, is alerted to a change
 * 3) SyncManager sets an alarm (to be received by USAR) for a few seconds in the
 * future (currently 15), the delay preventing excess syncing (think of it as a debounce mechanism).
 * 4) ESAR Receiver's onReceive method is called
 * 5) ESAR goes through all change and deletion records and compiles a list of mailboxes which have
 * changes to be uploaded.
 * 6) ESAR calls SyncManager to start syncs of those mailboxes
 *
 */
public class EmEmailSyncAlarmReceiver extends BroadcastReceiver {
    final String[] MAILBOX_DATA_PROJECTION = {MessageColumns.MAILBOX_KEY};
    private static String TAG = "EmailSyncAlarm";

    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.v(TAG, "onReceive");
        new Thread(new Runnable() {
            public void run() {
                handleReceive(context);
            }
        }).start();
    }

    private void handleReceive(Context context) {
        ArrayList<Long> mailboxesToNotify = new ArrayList<Long>();
        ContentResolver cr = context.getContentResolver();
        int messageCount = 0;
        
        // Get a selector for EAS accounts (we don't want to sync on changes to POP/IMAP messages)
        String selector = EmSyncManager.getEasAccountSelector();
        
        // Find all of the deletions
        Cursor c = cr.query(Message.DELETED_CONTENT_URI, MAILBOX_DATA_PROJECTION, selector,
               null, null);
        try {
            // Keep track of which mailboxes to notify; we'll only notify each one once
            while (c.moveToNext()) {
                messageCount++;
                long mailboxId = c.getLong(0);
                if (!mailboxesToNotify.contains(mailboxId)) {
                    mailboxesToNotify.add(mailboxId);
                }
            }
        } finally {
        	if(c != null)	//367712
        		c.close();
        }

        // Now, find changed messages
        c = cr.query(Message.UPDATED_CONTENT_URI, MAILBOX_DATA_PROJECTION, selector,
                null, null);
        try {
            // Keep track of which mailboxes to notify; we'll only notify each one once
            while (c.moveToNext()) {
                messageCount++;
                long mailboxId = c.getLong(0);
                if (!mailboxesToNotify.contains(mailboxId)) {
                    mailboxesToNotify.add(mailboxId);
                }
            }
        } finally {
        	if(c != null)	//367712
        		c.close();
        }

        // Request service from the mailbox
        for (Long mailboxId: mailboxesToNotify) {
            EmSyncManager.serviceRequest(mailboxId, EmSyncManager.SYNC_UPSYNC);
        }
        Log.v(TAG, "Changed/Deleted messages: " + messageCount + ", mailboxes: " +
                mailboxesToNotify.size());
    }
}
