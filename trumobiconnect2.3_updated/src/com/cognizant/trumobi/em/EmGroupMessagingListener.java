

package com.cognizant.trumobi.em;

import android.content.Context;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EmGroupMessagingListener extends EmMessagingListener {
    /* The synchronization of the methods in this class
       is not needed because we use ConcurrentHashMap.
       
       Nevertheless, let's keep the "synchronized" for a while in the case
       we may want to change the implementation to use something else
       than ConcurrentHashMap.
    */

    private ConcurrentHashMap<EmMessagingListener, Object> mListenersMap =
        new ConcurrentHashMap<EmMessagingListener, Object>();

    private Set<EmMessagingListener> mListeners = mListenersMap.keySet();

    synchronized public void addListener(EmMessagingListener listener) {
        // we use "this" as a dummy non-null value
        mListenersMap.put(listener, this);
    }

    synchronized public void removeListener(EmMessagingListener listener) {
        mListenersMap.remove(listener);
    }

    synchronized public boolean isActiveListener(EmMessagingListener listener) {
        return mListenersMap.containsKey(listener);
    }

    @Override
    synchronized public void listFoldersStarted(long accountId) {
        for (EmMessagingListener l : mListeners) {
            l.listFoldersStarted(accountId);
        }
    }

    @Override
    synchronized public void listFoldersFailed(long accountId, String message) {
        for (EmMessagingListener l : mListeners) {
            l.listFoldersFailed(accountId, message);
        }
    }

    @Override
    synchronized public void listFoldersFinished(long accountId) {
        for (EmMessagingListener l : mListeners) {
            l.listFoldersFinished(accountId);
        }
    }

    @Override
    synchronized public void synchronizeMailboxStarted(long accountId, long mailboxId) {
        for (EmMessagingListener l : mListeners) {
            l.synchronizeMailboxStarted(accountId, mailboxId);
        }
    }

    @Override
    synchronized public void synchronizeMailboxFinished(long accountId, long mailboxId,
            int totalMessagesInMailbox, int numNewMessages) {
        for (EmMessagingListener l : mListeners) {
            l.synchronizeMailboxFinished(accountId, mailboxId,
                    totalMessagesInMailbox, numNewMessages);
        }
    }

    @Override
    synchronized public void synchronizeMailboxFailed(long accountId, long mailboxId, Exception e) {
        for (EmMessagingListener l : mListeners) {
            l.synchronizeMailboxFailed(accountId, mailboxId, e);
        }
    }

    @Override
    synchronized public void loadMessageForViewStarted(long messageId) {
        for (EmMessagingListener l : mListeners) {
            l.loadMessageForViewStarted(messageId);
        }
    }

    @Override
    synchronized public void loadMessageForViewFinished(long messageId) {
        for (EmMessagingListener l : mListeners) {
            l.loadMessageForViewFinished(messageId);
        }
    }

    @Override
    synchronized public void loadMessageForViewFailed(long messageId, String message) {
        for (EmMessagingListener l : mListeners) {
            l.loadMessageForViewFailed(messageId, message);
        }
    }

    @Override
    synchronized public void checkMailStarted(Context context, long accountId, long tag) {
        for (EmMessagingListener l : mListeners) {
            l.checkMailStarted(context, accountId, tag);
        }
    }

    @Override
    synchronized public void checkMailFinished(Context context, long accountId, long folderId,
            long tag) {
        for (EmMessagingListener l : mListeners) {
            l.checkMailFinished(context, accountId, folderId, tag);
        }
    }

    @Override
    synchronized public void sendPendingMessagesStarted(long accountId, long messageId) {
        for (EmMessagingListener l : mListeners) {
            l.sendPendingMessagesStarted(accountId, messageId);
        }
    }

    @Override
    synchronized public void sendPendingMessagesCompleted(long accountId) {
        for (EmMessagingListener l : mListeners) {
            l.sendPendingMessagesCompleted(accountId);
        }
    }

    @Override
    synchronized public void sendPendingMessagesFailed(long accountId, long messageId,
            Exception reason) {
        for (EmMessagingListener l : mListeners) {
            l.sendPendingMessagesFailed(accountId, messageId, reason);
        }
    }

    @Override
    synchronized public void messageUidChanged(long accountId, long mailboxId,
            String oldUid, String newUid) {
        for (EmMessagingListener l : mListeners) {
            l.messageUidChanged(accountId, mailboxId, oldUid, newUid);
        }
    }

    @Override
    synchronized public void loadAttachmentStarted(
            long accountId,
            long messageId,
            long attachmentId,
            boolean requiresDownload) {
        for (EmMessagingListener l : mListeners) {
            l.loadAttachmentStarted(accountId, messageId, attachmentId, requiresDownload);
        }
    }

    @Override
    synchronized public void loadAttachmentFinished(
            long accountId,
            long messageId,
            long attachmentId) {
        for (EmMessagingListener l : mListeners) {
            l.loadAttachmentFinished(accountId, messageId, attachmentId);
        }
    }

    @Override
    synchronized public void loadAttachmentFailed(
            long accountId,
            long messageId,
            long attachmentId,
            String reason) {
        for (EmMessagingListener l : mListeners) {
            l.loadAttachmentFailed(accountId, messageId, attachmentId, reason);
        }
    }

    @Override
    synchronized public void controllerCommandCompleted(boolean moreCommandsToRun) {
        for (EmMessagingListener l : mListeners) {
            l.controllerCommandCompleted(moreCommandsToRun);
        }
    }
}
