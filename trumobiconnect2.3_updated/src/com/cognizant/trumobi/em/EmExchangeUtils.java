

package com.cognizant.trumobi.em;

import com.cognizant.trumobi.em.service.IEmailService;
import com.cognizant.trumobi.em.service.IEmailServiceCallback;
import com.cognizant.trumobi.em.mail.EmMessagingException;
import com.cognizant.trumobi.em.service.EmEmailServiceProxy;
import com.cognizant.trumobi.exchange.EmCalendarSyncEnabler;
import com.cognizant.trumobi.exchange.EmSyncManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * Utility functions for Exchange support.
 */
public class EmExchangeUtils {
    /**
     * Starts the service for Exchange, if supported.
     */
    public static void startExchangeService(Context context) {
        //EXCHANGE-REMOVE-SECTION-START
        context.startService(new Intent(context, EmSyncManager.class));
        //EXCHANGE-REMOVE-SECTION-END
    }
    public static void stopExchangeService(Context context) {
        //EXCHANGE-REMOVE-SECTION-START
    	Intent i = new Intent(context, EmSyncManager.class);
        context.stopService(i);
        //EXCHANGE-REMOVE-SECTION-END
    }

    /**
     * Returns an {@link IEmailService} for the Exchange service, if supported.  Otherwise it'll
     * return an empty {@link IEmailService} implementation.
     *
     * @param context
     * @param callback Object to get callback, or can be null
     */
    public static IEmailService getExchangeEmailService(Context context,
            IEmailServiceCallback callback) {
        IEmailService ret = null;
        //EXCHANGE-REMOVE-SECTION-START
        ret = new EmEmailServiceProxy(context, EmSyncManager.class, callback);
        //EXCHANGE-REMOVE-SECTION-END
        if (ret == null) {
            ret = NullEmailService.INSTANCE;
        }
        return ret;
    }

    /**
     * Enable calendar sync for all the existing exchange accounts, and post a notification if any.
     */
    public static void enableEasCalendarSync(Context context) {
        //EXCHANGE-REMOVE-SECTION-START
        new EmCalendarSyncEnabler(context).enableEasCalendarSync();
        //EXCHANGE-REMOVE-SECTION-END
    }

    /**
     * An empty {@link IEmailService} implementation which is used instead of
     * {@link com.cognizant.trumobi.exchange.EmSyncManager} on the build with no exchange support.
     *
     * <p>In theory, the service in question isn't used on the no-exchange-support build,
     * because we won't have any exchange accounts in that case, so we wouldn't have to have this
     * class.  However, there are a few places we do use the service even if there's no exchange
     * accounts (e.g. setLogging), so this class is added for safety and simplicity.
     */
    private static class NullEmailService implements IEmailService {
        public static final NullEmailService INSTANCE = new NullEmailService();

        public Bundle autoDiscover(String userName, String password) throws RemoteException {
            return Bundle.EMPTY;
        }

        public boolean createFolder(long accountId, String name) throws RemoteException {
            return false;
        }

        public boolean deleteFolder(long accountId, String name) throws RemoteException {
            return false;
        }

        public void hostChanged(long accountId) throws RemoteException {
        }

        public void loadAttachment(long attachmentId, String destinationFile,
                String contentUriString) throws RemoteException {
        }

        public void loadMore(long messageId) throws RemoteException {
        }

        public boolean renameFolder(long accountId, String oldName, String newName)
                throws RemoteException {
            return false;
        }

        public void sendMeetingResponse(long messageId, int response) throws RemoteException {
        }

        public void setCallback(IEmailServiceCallback cb) throws RemoteException {
        }

        public void setLogging(int on) throws RemoteException {
        }

        public void startSync(long mailboxId) throws RemoteException {
        }

        public void stopSync(long mailboxId) throws RemoteException {
        }

        public void updateFolderList(long accountId) throws RemoteException {
        }

        public int validate(String protocol, String host, String userName, String password,
                int port, boolean ssl, boolean trustCertificates) throws RemoteException {
            return EmMessagingException.UNSPECIFIED_EXCEPTION;
        }

        public IBinder asBinder() {
            return null;
        }

		@Override
		public boolean sendOofSettings(long accountId, boolean enable,
				String replyMessage, String startDate, String endDate)
				throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean getOofSettings(long accountId) throws RemoteException {
			// TODO Auto-generated method stub
			return false;
		}
    }
}
