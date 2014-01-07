

package com.cognizant.trumobi.exchange.adapter;

import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.em.provider.EmEmailContent.Account;
import com.cognizant.trumobi.em.provider.EmEmailContent.Mailbox;
import com.cognizant.trumobi.exchange.EmEasSyncService;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

/**
 * Parent class of all sync adapters (EasMailbox, EasCalendar, and EasContacts)
 *
 */
public abstract class EmAbstractSyncAdapter {

    public static final int SECONDS = 1000;
    public static final int MINUTES = SECONDS*60;
    public static final int HOURS = MINUTES*60;
    public static final int DAYS = HOURS*24;
    public static final int WEEKS = DAYS*7;

    public Mailbox mMailbox;
    public EmEasSyncService mService;
    public Context mContext;
    public Account mAccount;
    public final android.accounts.Account mAccountManagerAccount;

    // Create the data for local changes that need to be sent up to the server
    public abstract boolean sendLocalChanges(EmSerializer s)
        throws IOException;
    // Parse incoming data from the EAS server, creating, modifying, and deleting objects as
    // required through the EmailProvider
    public abstract boolean parse(InputStream is)
        throws IOException;
    // The name used to specify the collection type of the target (Email, Calendar, or Contacts)
    public abstract String getCollectionName();
    public abstract void cleanup();
    public abstract boolean isSyncable();

    public boolean isLooping() {
        return false;
    }

    public EmAbstractSyncAdapter(Mailbox mailbox, EmEasSyncService service) {
        mMailbox = mailbox;
        mService = service;
        mContext = service.mContext;
        mAccount = service.mAccount;
        mAccountManagerAccount = new android.accounts.Account(mAccount.mEmailAddress,
                Email.EXCHANGE_ACCOUNT_MANAGER_TYPE);
    }

    public void userLog(String ...strings) {
        mService.userLog(strings);
    }

    public void incrementChangeCount() {
        mService.mChangeCount++;
    }

    /**
     * Returns the current SyncKey; override if the SyncKey is stored elsewhere (as for Contacts)
     * @return the current SyncKey for the Mailbox
     * @throws IOException
     */
    public String getSyncKey() throws IOException {
        if (mMailbox.mSyncKey == null) {
            userLog("Reset SyncKey to 0");
            mMailbox.mSyncKey = "0";
        }
        return mMailbox.mSyncKey;
    }

    public void setSyncKey(String syncKey, boolean inCommands) throws IOException {
        mMailbox.mSyncKey = syncKey;
    }
}

