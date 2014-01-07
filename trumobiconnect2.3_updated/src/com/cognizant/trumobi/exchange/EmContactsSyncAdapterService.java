

package com.cognizant.trumobi.exchange;

import com.cognizant.trumobi.em.Email;
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
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;

public class EmContactsSyncAdapterService extends Service {
    private static final String TAG = "EAS ContactsSyncAdapterService";
    private static SyncAdapterImpl sSyncAdapter = null;
    private static final Object sSyncAdapterLock = new Object();

    private static final String[] ID_PROJECTION = new String[] {EmEmailContent.RECORD_ID};
    private static final String ACCOUNT_AND_TYPE_CONTACTS =
        MailboxColumns.ACCOUNT_KEY + "=? AND " + MailboxColumns.TYPE + '=' + Mailbox.TYPE_CONTACTS;

    public EmContactsSyncAdapterService() {
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
                EmContactsSyncAdapterService.performSync(mContext, account, extras,
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
     * Partial integration with system SyncManager; we tell our EAS SyncManager to start a contacts
     * sync when we get the signal from the system SyncManager.
     * The missing piece at this point is integration with the push/ping mechanism in EAS; this will
     * be put in place at a later time.
     */
    private static void performSync(Context context, Account account, Bundle extras,
            String authority, ContentProviderClient provider, SyncResult syncResult)
            throws OperationCanceledException {
        ContentResolver cr = context.getContentResolver();
        Log.i(TAG, "performSync");
        if (extras.getBoolean(ContentResolver.SYNC_EXTRAS_UPLOAD)) {
            Uri uri = RawContacts.CONTENT_URI.buildUpon()
                .appendQueryParameter(RawContacts.ACCOUNT_NAME, account.name)
                .appendQueryParameter(RawContacts.ACCOUNT_TYPE, Email.EXCHANGE_ACCOUNT_MANAGER_TYPE)
                .build();
            Cursor c = cr.query(uri,
                    new String[] {RawContacts._ID}, RawContacts.DIRTY + "=1", null, null);
            try {
                if (!c.moveToFirst()) {
                    Log.i(TAG, "Upload sync; no changes");
                    return;
                }
            } finally {
                c.close();
            }
        }

        // Find the (EmailProvider) account associated with this email address
        Cursor accountCursor =
            cr.query(com.cognizant.trumobi.em.provider.EmEmailContent.Account.CONTENT_URI, ID_PROJECTION,
                AccountColumns.EMAIL_ADDRESS + "=?", new String[] {account.name}, null);
        try {
            if (accountCursor.moveToFirst()) {
                long accountId = accountCursor.getLong(0);
                // Now, find the contacts mailbox associated with the account
                Cursor mailboxCursor = cr.query(Mailbox.CONTENT_URI, ID_PROJECTION,
                        ACCOUNT_AND_TYPE_CONTACTS, new String[] {Long.toString(accountId)}, null);
                try {
                     if (mailboxCursor.moveToFirst()) {
                        Log.i(TAG, "Contact sync requested for " + account.name);
                        // Ask for a sync from our sync manager
                        EmSyncManager.serviceRequest(mailboxCursor.getLong(0),
                                EmSyncManager.SYNC_UPSYNC);
                    }
                } finally {
                    mailboxCursor.close();
                }
            }
        } finally {
            accountCursor.close();
        }
    }
}