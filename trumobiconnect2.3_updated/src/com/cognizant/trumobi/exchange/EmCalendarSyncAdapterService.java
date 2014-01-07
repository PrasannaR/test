

package com.cognizant.trumobi.exchange;


import com.cognizant.trumobi.common.provider.Calendar.Events;
import com.cognizant.trumobi.em.provider.EmEmailContent;
import com.cognizant.trumobi.em.provider.EmEmailContent.AccountColumns;
import com.cognizant.trumobi.em.provider.EmEmailContent.Mailbox;
import com.cognizant.trumobi.em.provider.EmEmailContent.MailboxColumns;

import android.accounts.Account;
import android.accounts.OperationCanceledException;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class EmCalendarSyncAdapterService extends Service {
    private static final String TAG = "EAS CalendarSyncAdapterService";
    private static SyncAdapterImpl sSyncAdapter = null;
    private static final Object sSyncAdapterLock = new Object();

    private static final String ACCOUNT_AND_TYPE_CALENDAR =
        MailboxColumns.ACCOUNT_KEY + "=? AND " + MailboxColumns.TYPE + '=' + Mailbox.TYPE_CALENDAR;
    private static final String DIRTY_IN_ACCOUNT =
        Events._SYNC_DIRTY + "=1 AND " + Events._SYNC_ACCOUNT + "=?";
    private static final String[] ID_SYNC_KEY_PROJECTION =
        new String[] {MailboxColumns.ID, MailboxColumns.SYNC_KEY};
    private static final int ID_SYNC_KEY_MAILBOX_ID = 0;
    private static final int ID_SYNC_KEY_SYNC_KEY = 1;

    public EmCalendarSyncAdapterService() {
        super();
    }

    private static class SyncAdapterImpl extends AbstractThreadedSyncAdapter {
        private Context mContext;

        public SyncAdapterImpl(Context context) {
            super(context, true /* autoInitialize */);
            mContext = context;
        }

        @Override
        public void onPerformSync(Account account, Bundle extras,
                String authority, ContentProviderClient provider, SyncResult syncResult) {
            try {
                EmCalendarSyncAdapterService.performSync(mContext, account, extras,
                        authority, provider, syncResult);
            } catch (OperationCanceledException e) {
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new SyncAdapterImpl(getApplicationContext());
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }

    /**
     * Partial integration with system SyncManager; we tell our EAS SyncManager to start a calendar
     * sync when we get the signal from the system SyncManager.
     * The missing piece at this point is integration with the push/ping mechanism in EAS; this will
     * be put in place at a later time.
     */
    private static void performSync(Context context, Account account, Bundle extras,
            String authority, ContentProviderClient provider, SyncResult syncResult) 
            throws OperationCanceledException {
//        ContentResolver cr = context.getContentResolver();
//        boolean logging = EmEas.USER_LOG;
//        if (extras.getBoolean(ContentResolver.SYNC_EXTRAS_UPLOAD)) {
//            Cursor c = cr.query(Events.CONTENT_URI,
//                    new String[] {Events._ID}, DIRTY_IN_ACCOUNT, new String[] {account.name}, null);
//            try {
//                if (c != null && !c.moveToFirst()) {	//NaGa
//                    if (logging) {
//                        Log.d(TAG, "No changes for " + account.name);
//                    }
//                    return;
//                }
//            } finally {
//            	if(c != null)	//NaGa
//                c.close();
//            }
//        }
//
//        // Find the (EmailProvider) account associated with this email address
//        Cursor accountCursor =
//            cr.query(EmEmailContent.Account.CONTENT_URI,
//                    EmEmailContent.ID_PROJECTION, AccountColumns.EMAIL_ADDRESS + "=?",
//                    new String[] {account.name}, null);
//        try {
//            if (accountCursor.moveToFirst()) {
//                long accountId = accountCursor.getLong(0);
//                // Now, find the calendar mailbox associated with the account
//                Cursor mailboxCursor = cr.query(Mailbox.CONTENT_URI, ID_SYNC_KEY_PROJECTION,
//                        ACCOUNT_AND_TYPE_CALENDAR, new String[] {Long.toString(accountId)}, null);
//                try {
//                     if (mailboxCursor.moveToFirst()) {
//                        if (logging) {
//                            Log.d(TAG, "Upload sync requested for " + account.name);
//                        }
//                        String syncKey = mailboxCursor.getString(ID_SYNC_KEY_SYNC_KEY);
//                        if ((syncKey == null) || (syncKey.equals("0"))) {
//                            if (logging) {
//                                Log.d(TAG, "Can't sync; mailbox in initial state");
//                            }
//                            return;
//                        }
//                        // Ask for a sync from our sync manager
//                        EmSyncManager.serviceRequest(mailboxCursor.getLong(ID_SYNC_KEY_MAILBOX_ID),
//                                EmSyncManager.SYNC_UPSYNC);
//                    }
//                } finally {
//                    mailboxCursor.close();
//                }
//            }
//        } finally {
//            accountCursor.close();
//        }
   }
}