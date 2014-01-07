

package com.cognizant.trumobi.exchange;

import com.cognizant.trumobi.em.mail.EmMessagingException;
import com.cognizant.trumobi.em.provider.EmEmailContent.Account;
import com.cognizant.trumobi.em.provider.EmEmailContent.Mailbox;
import com.cognizant.trumobi.exchange.utility.EmFileLogger;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.NetworkInfo.DetailedState;
import android.util.Log;

import java.util.ArrayList;

/**
 * Base class for all protocol services SyncManager (extends Service, implements
 * Runnable) instantiates subclasses to run a sync (either timed, or push, or
 * mail placed in outbox, etc.) EasSyncService is currently implemented; my goal
 * would be to move IMAP to this structure when it comes time to introduce push
 * functionality.
 */
public abstract class EmAbstractSyncService implements Runnable {

    public String TAG = "AbstractSyncService";

    public static final int SECONDS = 1000;
    public static final int MINUTES = 60*SECONDS;
    public static final int HOURS = 60*MINUTES;
    public static final int DAYS = 24*HOURS;

    public static final int CONNECT_TIMEOUT = 30*SECONDS;
    public static final int NETWORK_WAIT = 15*SECONDS;

    public static final String EAS_PROTOCOL = "eas";
    public static final int EXIT_DONE = 0;
    public static final int EXIT_IO_ERROR = 1;
    public static final int EXIT_LOGIN_FAILURE = 2;
    public static final int EXIT_EXCEPTION = 3;
    public static final int EXIT_SECURITY_FAILURE = 4;

    public Mailbox mMailbox;
    protected long mMailboxId;
    protected Thread mThread;
    protected int mExitStatus = EXIT_EXCEPTION;
    protected String mMailboxName;
    public Account mAccount;
    public Context mContext;
    public int mChangeCount = 0;
    public int mSyncReason = 0;
    protected volatile boolean mStop = false;
    protected Object mSynchronizer = new Object();

    protected volatile long mRequestTime = 0;
    protected ArrayList<EmRequest> mRequests = new ArrayList<EmRequest>();
    protected EmPartRequest mPendingRequest = null;

    /**
     * Sent by SyncManager to request that the service stop itself cleanly
     */
    public abstract void stop();

    /**
     * Sent by SyncManager to indicate that an alarm has fired for this service, and that its
     * pending (network) operation has timed out. The service is NOT automatically stopped,
     * although the behavior is service dependent.
     *
     * @return true if the operation was stopped normally; false if the thread needed to be
     * interrupted.
     */
    public abstract boolean alarm();

    /**
     * Sent by SyncManager to request that the service reset itself cleanly; the meaning of this
     * operation is service dependent.
     */
    public abstract void reset();

    /**
     * Called to validate an account; abstract to allow each protocol to do what
     * is necessary. For consistency with the Email app's original
     * functionality, success is indicated by a failure to throw an Exception
     * (ugh). Parameters are self-explanatory
     *
     * @param host
     * @param userName
     * @param password
     * @param port
     * @param ssl
     * @param context
     * @throws EmMessagingException
     */
    public abstract void validateAccount(String host, String userName, String password, int port,
            boolean ssl, boolean trustCertificates, Context context) throws EmMessagingException;
    
    public abstract boolean sendOofSettings(long aId, boolean enable, String rMessage, String sDate, String eDate, Context context);
    public abstract boolean getOofSettings(long aId, Context context);
    
    public EmAbstractSyncService(Context _context, Mailbox _mailbox) {
        mContext = _context;
        mMailbox = _mailbox;
        mMailboxId = _mailbox.mId;
        mMailboxName = _mailbox.mServerId;
        mAccount = Account.restoreAccountWithId(_context, _mailbox.mAccountKey);
    }

    // Will be required when subclasses are instantiated by name
    public EmAbstractSyncService(String prefix) {
    }

    /**
     * The UI can call this static method to perform account validation.  This method wraps each
     * protocol's validateAccount method.   Arguments are self-explanatory, except where noted.
     *
     * @param klass the protocol class (EasSyncService.class for example)
     * @param host
     * @param userName
     * @param password
     * @param port
     * @param ssl
     * @param context
     * @throws EmMessagingException
     */
    static public void validate(Class<? extends EmAbstractSyncService> klass, String host,
            String userName, String password, int port, boolean ssl, boolean trustCertificates,
            Context context)
            throws EmMessagingException {
        EmAbstractSyncService svc;
        try {
            svc = klass.newInstance();
            svc.validateAccount(host, userName, password, port, ssl, trustCertificates, context);
        } catch (IllegalAccessException e) {
            throw new EmMessagingException("internal error", e);
        } catch (InstantiationException e) {
            throw new EmMessagingException("internal error", e);
        }
    }
    
    static public boolean sendOof(Class<? extends EmAbstractSyncService> klass, long aId,
            boolean enable, String rMessage, String sDate, String eDate,
            Context context)
            throws EmMessagingException {
        EmAbstractSyncService svc;
        try {
            svc = klass.newInstance();
            return svc.sendOofSettings(aId, enable, rMessage, sDate, eDate, context);
        } catch (IllegalAccessException e) {
            throw new EmMessagingException("internal error", e);
        } catch (InstantiationException e) {
            throw new EmMessagingException("internal error", e);
        }
    }
    static boolean getOof(Class<EmEasSyncService> class1, long accountId,
			Context context) throws EmMessagingException {
		// TODO Auto-generated method stub
    	EmAbstractSyncService svc;
        try {
            svc = class1.newInstance();
            return svc.getOofSettings(accountId, context);
        } catch (IllegalAccessException e) {
            throw new EmMessagingException("internal error", e);
        } catch (InstantiationException e) {
            throw new EmMessagingException("internal error", e);
        }
	}

    public static class ValidationResult {
        static final int NO_FAILURE = 0;
        static final int CONNECTION_FAILURE = 1;
        static final int VALIDATION_FAILURE = 2;
        static final int EXCEPTION = 3;

        static final ValidationResult succeeded = new ValidationResult(true, NO_FAILURE, null);
        boolean success;
        int failure = NO_FAILURE;
        String reason = null;
        Exception exception = null;

        ValidationResult(boolean _success, int _failure, String _reason) {
            success = _success;
            failure = _failure;
            reason = _reason;
        }

        ValidationResult(boolean _success) {
            success = _success;
        }

        ValidationResult(Exception e) {
            success = false;
            failure = EXCEPTION;
            exception = e;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getReason() {
            return reason;
        }
    }

    public boolean isStopped() {
        return mStop;
    }

    public Object getSynchronizer() {
        return mSynchronizer;
    }

    /**
     * Convenience methods to do user logging (i.e. connection activity).  Saves a bunch of
     * repetitive code.
     */
    public void userLog(String string, int code, String string2) {
        if (EmEas.USER_LOG) {
            userLog(string + code + string2);
        }
    }

    public void userLog(String string, int code) {
        if (EmEas.USER_LOG) {
            userLog(string + code);
        }
    }

    public void userLog(String str, Exception e) {
        if (EmEas.USER_LOG) {
            Log.e(TAG, str, e);
        } else {
            Log.e(TAG, str + e);
        }
        if (EmEas.FILE_LOG) {
            EmFileLogger.log(e);
        }
    }

    /**
     * Standard logging for EAS.
     * If user logging is active, we concatenate any arguments and log them using Log.d
     * We also check for file logging, and log appropriately
     * @param strings strings to concatenate and log
     */
    public void userLog(String ...strings) {
        if (EmEas.USER_LOG) {
            String logText;
            if (strings.length == 1) {
                logText = strings[0];
            } else {
                StringBuilder sb = new StringBuilder(64);
                for (String string: strings) {
                    sb.append(string);
                }
                logText = sb.toString();
            }
            Log.d(TAG, logText);
            if (EmEas.FILE_LOG) {
                EmFileLogger.log(TAG, logText);
            }
        }
    }

    /**
     * Error log is used for serious issues that should always be logged
     * @param str the string to log
     */
    public void errorLog(String str) {
        Log.e(TAG, str);
        if (EmEas.FILE_LOG) {
            EmFileLogger.log(TAG, str);
        }
    }

    /**
     * Waits for up to 10 seconds for network connectivity; returns whether or not there is
     * network connectivity.
     *
     * @return whether there is network connectivity
     */
    public boolean hasConnectivity() {
        ConnectivityManager cm =
            (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        int tries = 0;
        while (tries++ < 1) {
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                DetailedState state = info.getDetailedState();
                if (state == DetailedState.CONNECTED) {
                    return true;
                }
            }
            try {
                Thread.sleep(10*SECONDS);
            } catch (InterruptedException e) {
            }
        }
        return false;
    }

    /**
     * Request handling (common functionality)
     * Can be overridden if desired
     */

    public void addRequest(EmRequest req) {
        synchronized (mRequests) {
            mRequests.add(req);
            mRequestTime = System.currentTimeMillis();
        }
    }

    public void removeRequest(EmRequest req) {
        synchronized (mRequests) {
            mRequests.remove(req);
        }
    }

    /**
     * Convenience method wrapping calls to retrieve columns from a single row, via EmailProvider.
     * The arguments are exactly the same as to contentResolver.query().  Results are returned in
     * an array of Strings corresponding to the columns in the projection.
     */
    protected String[] getRowColumns(Uri contentUri, String[] projection, String selection,
            String[] selectionArgs) {
        String[] values = new String[projection.length];
        ContentResolver cr = mContext.getContentResolver();
        Cursor c = cr.query(contentUri, projection, selection, selectionArgs, null);
        try {
            if (c.moveToFirst()) {
                for (int i = 0; i < projection.length; i++) {
                    values[i] = c.getString(i);
                }
            } else {
                return null;
            }
        } finally {
            c.close();
        }
        return values;
    }

    /**
     * Convenience method for retrieving columns from a particular row in EmailProvider.
     * Passed in here are a base uri (e.g. Message.CONTENT_URI), the unique id of a row, and
     * a projection.  This method calls the previous one with the appropriate URI.
     */
    protected String[] getRowColumns(Uri baseUri, long id, String ... projection) {
        return getRowColumns(ContentUris.withAppendedId(baseUri, id), projection, null, null);
    }

	

	
}
