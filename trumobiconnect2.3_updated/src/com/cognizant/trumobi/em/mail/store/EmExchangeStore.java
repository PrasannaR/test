

package com.cognizant.trumobi.em.mail.store;

import com.cognizant.trumobi.em.service.IEmailService;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.em.EmExchangeUtils;
import com.cognizant.trumobi.em.mail.EmAuthenticationFailedException;
import com.cognizant.trumobi.em.mail.EmFolder;
import com.cognizant.trumobi.em.mail.EmMessagingException;
import com.cognizant.trumobi.em.mail.EmStore;
import com.cognizant.trumobi.em.mail.EmStoreSynchronizer;
import com.cognizant.trumobi.em.provider.EmEmailContent.Account;
import com.cognizant.trumobi.em.service.EmEasAuthenticatorService;
import com.cognizant.trumobi.em.service.EmEmailServiceProxy;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

/**
 * Our Exchange service does not use the sender/store model.  This class exists for exactly two
 * purposes, (1) to provide a hook for checking account connections, and (2) to return
 * "AccountSetupExchange.class" for getSettingActivityClass().
 */
public class EmExchangeStore extends EmStore {
    public static final String LOG_TAG = "ExchangeStore";

    private final URI mUri;
    private final ExchangeTransport mTransport;

    /**
     * Factory method.
     */
    public static EmStore newInstance(String uri, Context context, PersistentDataCallbacks callbacks)
            throws EmMessagingException {
        return new EmExchangeStore(uri, context, callbacks);
    }

    /**
     * eas://user:password@server/domain
     *
     * @param _uri
     * @param application
     */
    private EmExchangeStore(String _uri, Context context, PersistentDataCallbacks callbacks)
            throws EmMessagingException {
        try {
            mUri = new URI(_uri);
        } catch (URISyntaxException e) {
            throw new EmMessagingException("Invalid uri for ExchangeStore");
        }

        mTransport = ExchangeTransport.getInstance(mUri, context);
    }

    @Override
    public void checkSettings() throws EmMessagingException {
        mTransport.checkSettings(mUri);
    }

    static public AccountManagerFuture<Bundle> addSystemAccount(Context context, Account acct,
            boolean syncContacts, boolean syncCalendar, AccountManagerCallback<Bundle> callback) {
        // Create a description of the new account
        Bundle options = new Bundle();
        options.putString(EmEasAuthenticatorService.OPTIONS_USERNAME, acct.mEmailAddress);
        options.putString(EmEasAuthenticatorService.OPTIONS_PASSWORD, acct.mHostAuthRecv.mPassword);
        options.putBoolean(EmEasAuthenticatorService.OPTIONS_CONTACTS_SYNC_ENABLED, syncContacts);
        options.putBoolean(EmEasAuthenticatorService.OPTIONS_CALENDAR_SYNC_ENABLED, syncCalendar);

        // Here's where we tell AccountManager about the new account.  The addAccount
        // method in AccountManager calls the addAccount method in our authenticator
        // service (EasAuthenticatorService)
        return AccountManager.get(context).addAccount(Email.EXCHANGE_ACCOUNT_MANAGER_TYPE,
                null, null, options, null, callback, null);
    }

    /**
     * Remove an account from the Account manager - see {@link AccountManager#removeAccount(
     * android.accounts.Account, AccountManagerCallback, android.os.Handler)}.
     *
     * @param context context to use
     * @param acct the account to remove
     * @param callback async results callback - pass null to use blocking mode
     */
    static public AccountManagerFuture<Boolean> removeSystemAccount(Context context, Account acct,
            AccountManagerCallback<Bundle> callback) {
        android.accounts.Account systemAccount =
            new android.accounts.Account(acct.mEmailAddress, Email.EXCHANGE_ACCOUNT_MANAGER_TYPE);
        return AccountManager.get(context).removeAccount(systemAccount, null, null);
    }

    @Override
    public EmFolder getFolder(String name) {
        return null;
    }

    @Override
    public EmFolder[] getPersonalNamespaces() {
        return null;
    }

    /**
     * Get class of SettingActivity for this Store class.
     * @return Activity class that has class method actionEditIncomingSettings()
     */
    @Override
    public Class<? extends android.app.Activity> getSettingActivityClass() {
        return com.cognizant.trumobi.em.activity.setup.EmAccountSetupExchange.class;
    }

    /**
     * Get class of sync'er for this Store class.  Because exchange Sync rules are so different
     * than IMAP or POP3, it's likely that an Exchange implementation will need its own sync
     * controller.  If so, this function must return a non-null value.
     *
     * @return Message Sync controller, or null to use default
     */
    @Override
    public EmStoreSynchronizer getMessageSynchronizer() {
        return null;
    }

    /**
     * Inform MessagingController that this store requires message structures to be prefetched
     * before it can fetch message bodies (this is due to EAS protocol restrictions.)
     * @return always true for EAS
     */
    @Override
    public boolean requireStructurePrefetch() {
        return true;
    }

    /**
     * Inform MessagingController that messages sent via EAS will be placed in the Sent folder
     * automatically (server-side) and don't need to be uploaded.
     * @return always false for EAS (assuming server-side copy is supported)
     */
    @Override
    public boolean requireCopyMessageToSentFolder() {
        return false;
    }

    public static class ExchangeTransport {
        private final Context mContext;

        private String mHost;
        private String mDomain;
        private String mUsername;
        private String mPassword;

        private static final HashMap<String, ExchangeTransport> sUriToInstanceMap =
            new HashMap<String, ExchangeTransport>();

        /**
         * Public factory.  The transport should be a singleton (per Uri)
         */
        public synchronized static ExchangeTransport getInstance(URI uri, Context context)
        throws EmMessagingException {
            if (!uri.getScheme().equals("eas") && !uri.getScheme().equals("eas+ssl+") &&
                    !uri.getScheme().equals("eas+ssl+trustallcerts")) {
                throw new EmMessagingException("Invalid scheme");
            }

            final String key = uri.toString();
            ExchangeTransport transport = sUriToInstanceMap.get(key);
            if (transport == null) {
                transport = new ExchangeTransport(uri, context);
                sUriToInstanceMap.put(key, transport);
            }
            return transport;
        }

        /**
         * Private constructor - use public factory.
         */
        private ExchangeTransport(URI uri, Context context) throws EmMessagingException {
            mContext = context;
            setUri(uri);
        }

        /**
         * Use the Uri to set up a newly-constructed transport
         * @param uri
         * @throws EmMessagingException
         */
        private void setUri(final URI uri) throws EmMessagingException {
            mHost = uri.getHost();
            if (mHost == null) {
                throw new EmMessagingException("host not specified");
            }

            mDomain = uri.getPath();
            if (!TextUtils.isEmpty(mDomain)) {
                mDomain = mDomain.substring(1);
            }

            final String userInfo = uri.getUserInfo();
            if (userInfo == null) {
                throw new EmMessagingException("user information not specifed");
            }
            final String[] uinfo = userInfo.split(":", 2);
            if (uinfo.length != 2) {
                throw new EmMessagingException("user name and password not specified");
            }
            mUsername = uinfo[0];
            mPassword = uinfo[1];
        }

        /**
         * Here's where we check the settings for EAS.
         * @param uri the URI of the account to create
         * @throws EmMessagingException if we can't authenticate the account
         */
        public void checkSettings(URI uri) throws EmMessagingException {
            setUri(uri);
            boolean ssl = uri.getScheme().contains("+ssl");
            boolean tssl = uri.getScheme().contains("+trustallcerts");
            try {
                int port = ssl ? 443 : 80;

                IEmailService svc = EmExchangeUtils.getExchangeEmailService(mContext, null);
                // Use a longer timeout for the validate command.  Note that the instanceof check
                // shouldn't be necessary; we'll do it anyway, just to be safe
                if (svc instanceof EmEmailServiceProxy) {
                    ((EmEmailServiceProxy)svc).setTimeout(90);
                }
                int result = svc.validate("eas", mHost, mUsername, mPassword, port, ssl, tssl);
                if (result != EmMessagingException.NO_ERROR) {
                    if (result == EmMessagingException.AUTHENTICATION_FAILED) {
                        throw new EmAuthenticationFailedException("Authentication failed.");
                    } else {
                        throw new EmMessagingException(result);
                    }
                }
            } catch (RemoteException e) {
                throw new EmMessagingException("Call to validate generated an exception", e);
            }
        }
    }

    /**
     * We handle AutoDiscover for Exchange 2007 (and later) here, wrapping the EmailService call.
     * The service call returns a HostAuth and we return null if there was a service issue
     */
    @Override
    public Bundle autoDiscover(Context context, String username, String password)
            throws EmMessagingException {
        try {
            return EmExchangeUtils.getExchangeEmailService(context, null)
                .autoDiscover(username, password);
        } catch (RemoteException e) {
            return null;
        }
    }
}
