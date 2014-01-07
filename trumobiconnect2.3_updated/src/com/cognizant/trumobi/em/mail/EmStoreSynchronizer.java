

package com.cognizant.trumobi.em.mail;

import com.cognizant.trumobi.em.EmGroupMessagingListener;
import com.cognizant.trumobi.em.EmMessagingListener;
import com.cognizant.trumobi.em.provider.EmEmailContent;

import android.content.Context;

/**
 * This interface allows a store to define a completely different synchronizer algorithm,
 * as necessary.
 */
public interface EmStoreSynchronizer {
    
    /**
     * An object of this class is returned by SynchronizeMessagesSynchronous to report
     * the results of the sync run.
     */
    public static class SyncResults {
        /**
         * The total # of messages in the folder
         */
        public int mTotalMessages;
        /**
         * The # of new messages in the folder
         */
        public int mNewMessages;
        
        public SyncResults(int totalMessages, int newMessages) {
            mTotalMessages = totalMessages;
            mNewMessages = newMessages;
        }
    }
    
    /**
     * The job of this method is to synchronize messages between a remote folder and the
     * corresponding local folder.
     * 
     * The following callbacks should be called during this operation:
     *  {@link EmMessagingListener#synchronizeMailboxNewMessage(EmAccount, String, Message)}
     *  {@link EmMessagingListener#synchronizeMailboxRemovedMessage(EmAccount, String, Message)}
     *  
     * Callbacks (through listeners) *must* be synchronized on the listeners object, e.g.
     *   synchronized (listeners) {
     *       for(MessagingListener listener : listeners) {
     *           listener.synchronizeMailboxNewMessage(account, folder, message);
     *       }
     *   }
     *
     * @param account The account to synchronize
     * @param folder The folder to synchronize
     * @param listeners callbacks to make during sync operation
     * @param context if needed for making system calls
     * @return an object describing the sync results
     */
    public SyncResults SynchronizeMessagesSynchronous(
            EmEmailContent.Account account, EmEmailContent.Mailbox folder,
            EmGroupMessagingListener listeners, Context context) throws EmMessagingException;
    
}
