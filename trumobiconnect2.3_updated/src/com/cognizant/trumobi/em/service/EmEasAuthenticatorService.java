

package com.cognizant.trumobi.em.service;


import com.cognizant.trubox.contacts.db.ContactsConsts;
import com.cognizant.trumobi.calendar.util.CalendarConstants;
import com.cognizant.trumobi.common.provider.Calendar;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.em.activity.setup.EmAccountSetupBasics;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;

/**
 * A very basic authenticator service for EAS.  At the moment, it has no UI hooks.  When called
 * with addAccount, it simply adds the account to AccountManager directly with a username and
 * password.  We will need to implement confirmPassword, confirmCredentials, and updateCredentials.
 */
public class EmEasAuthenticatorService extends Service {
    public static final String OPTIONS_USERNAME = "username";
    public static final String OPTIONS_PASSWORD = "password";
    public static final String OPTIONS_CONTACTS_SYNC_ENABLED = "contacts";
    public static final String OPTIONS_CALENDAR_SYNC_ENABLED = "calendar";

    class EasAuthenticator extends AbstractAccountAuthenticator {
        public EasAuthenticator(Context context) {
            super(context);
        }

        @Override
        public Bundle addAccount(AccountAuthenticatorResponse response, String accountType,
                String authTokenType, String[] requiredFeatures, Bundle options)
                throws NetworkErrorException {
            // There are two cases here:
            // 1) We are called with a username/password; this comes from the traditional email
            //    app UI; we simply create the account and return the proper bundle
            if (options != null && options.containsKey(OPTIONS_PASSWORD)
                    && options.containsKey(OPTIONS_USERNAME)) {
                final Account account = new Account(options.getString(OPTIONS_USERNAME),
                        Email.EXCHANGE_ACCOUNT_MANAGER_TYPE);
                AccountManager.get(EmEasAuthenticatorService.this).addAccountExplicitly(
                            account, options.getString(OPTIONS_PASSWORD), null);

                // Set up contacts syncing.  SyncManager will use information from ContentResolver
                // to determine syncability of Contacts for Exchange
                boolean syncContacts = false;
                if (options.containsKey(OPTIONS_CONTACTS_SYNC_ENABLED) &&
                        options.getBoolean(OPTIONS_CONTACTS_SYNC_ENABLED)) {
                    syncContacts = true;
                }
                ContentResolver.setIsSyncable(account, ContactsConsts.AUTHORITY, 1);
                ContentResolver.setSyncAutomatically(account, ContactsConsts.AUTHORITY,
                        syncContacts);

                // Set up calendar syncing, as above
                boolean syncCalendar = false;
                if (options.containsKey(OPTIONS_CALENDAR_SYNC_ENABLED) &&
                        options.getBoolean(OPTIONS_CALENDAR_SYNC_ENABLED)) {
                    syncCalendar = true;
                }
                ContentResolver.setIsSyncable(account, CalendarConstants.AUTHORITY, 1);
                ContentResolver.setSyncAutomatically(account, CalendarConstants.AUTHORITY, syncCalendar);

                Bundle b = new Bundle();
                b.putString(AccountManager.KEY_ACCOUNT_NAME, options.getString(OPTIONS_USERNAME));
                b.putString(AccountManager.KEY_ACCOUNT_TYPE, Email.EXCHANGE_ACCOUNT_MANAGER_TYPE);
                return b;
            // 2) The other case is that we're creating a new account from an Account manager
            //    activity.  In this case, we add an intent that will be used to gather the
            //    account information...
            } else {
                Bundle b = new Bundle();
                Intent intent =
                    EmAccountSetupBasics.actionSetupExchangeIntent(EmEasAuthenticatorService.this);
                // Add extras that indicate this is an Exchange account creation
                // So we'll skip the "account type" activity, and we'll use the response when
                // we're done
                intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
                b.putParcelable(AccountManager.KEY_INTENT, intent);
                return b;
            }
        }

        @Override
        public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account,
                Bundle options) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
            return null;
        }

        @Override
        public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account,
                String authTokenType, Bundle loginOptions) throws NetworkErrorException {
            return null;
        }

        @Override
        public String getAuthTokenLabel(String authTokenType) {
            // null means we don't have compartmentalized authtoken types
            return null;
        }

        @Override
        public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account,
                String[] features) throws NetworkErrorException {
            return null;
        }

        @Override
        public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account,
                String authTokenType, Bundle loginOptions) {
            // TODO Auto-generated method stub
            return null;
        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Replace this with an appropriate constant in AccountManager, when it's created
        String authenticatorIntent = "android.accounts.AccountAuthenticator";

        if (authenticatorIntent.equals(intent.getAction())) {
            return new EasAuthenticator(this).getIBinder();
        } else {
            return null;
        }
    }
}
