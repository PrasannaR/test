

package com.cognizant.trumobi.em;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.util.UUID;

import com.TruBoxSDK.SharedPreferences;

public class EmPreferences {

    // Preferences file
    private static final String PREFERENCES_FILE = "AndroidMail.Main";

    // Preferences field names
    private static final String ACCOUNT_UUIDS = "accountUuids";
    private static final String DEFAULT_ACCOUNT_UUID = "defaultAccountUuid";
    private static final String ENABLE_DEBUG_LOGGING = "enableDebugLogging";
    private static final String ENABLE_SENSITIVE_LOGGING = "enableSensitiveLogging";
    private static final String ENABLE_EXCHANGE_LOGGING = "enableExchangeLogging";
    private static final String ENABLE_EXCHANGE_FILE_LOGGING = "enableExchangeFileLogging";
    private static final String DEVICE_UID = "deviceUID";
    private static final String ONE_TIME_INITIALIZATION_PROGRESS = "oneTimeInitializationProgress";

    private static EmPreferences sPreferences;

    final SharedPreferences mSharedPreferences;

    private EmPreferences(Context context) {
//        mSharedPreferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
    	mSharedPreferences = new SharedPreferences(context);
    }

    /**
     * TODO need to think about what happens if this gets GCed along with the
     * Activity that initialized it. Do we lose ability to read Preferences in
     * further Activities? Maybe this should be stored in the Application
     * context.
     */
    public static synchronized EmPreferences getPreferences(Context context) {
        if (sPreferences == null) {
            sPreferences = new EmPreferences(context);
        }
        return sPreferences;
    }

    /**
     * Returns an array of the accounts on the system. If no accounts are
     * registered the method returns an empty array.
     */
    public EmAccount[] getAccounts() {
        String accountUuids = mSharedPreferences.getString(ACCOUNT_UUIDS, null);
        if (accountUuids == null || accountUuids.length() == 0) {
            return new EmAccount[] {};
        }
        String[] uuids = accountUuids.split(",");
        EmAccount[] accounts = new EmAccount[uuids.length];
        for (int i = 0, length = uuids.length; i < length; i++) {
            accounts[i] = new EmAccount(this, uuids[i]);
        }
        return accounts;
    }

    /**
     * Get an account object by Uri, or return null if no account exists
     * TODO: Merge hardcoded strings with the same strings in Account.java
     */
    public EmAccount getAccountByContentUri(Uri uri) {
        if (!"content".equals(uri.getScheme()) || !"accounts".equals(uri.getAuthority())) {
            return null;
        }
        String uuid = uri.getPath().substring(1);
        if (uuid == null) {
            return null;
        }
        String accountUuids = mSharedPreferences.getString(ACCOUNT_UUIDS, null);
        if (accountUuids == null || accountUuids.length() == 0) {
            return null;
        }
        String[] uuids = accountUuids.split(",");
        for (int i = 0, length = uuids.length; i < length; i++) {
            if (uuid.equals(uuids[i])) {
                return new EmAccount(this, uuid);
            }
        }
        return null;
    }

    /**
     * Returns the Account marked as default. If no account is marked as default
     * the first account in the list is marked as default and then returned. If
     * there are no accounts on the system the method returns null.
     */
    public EmAccount getDefaultAccount() {
        String defaultAccountUuid = mSharedPreferences.getString(DEFAULT_ACCOUNT_UUID, null);
        EmAccount defaultAccount = null;
        EmAccount[] accounts = getAccounts();
        if (defaultAccountUuid != null) {
            for (EmAccount account : accounts) {
                if (account.getUuid().equals(defaultAccountUuid)) {
                    defaultAccount = account;
                    break;
                }
            }
        }

        if (defaultAccount == null) {
            if (accounts.length > 0) {
                defaultAccount = accounts[0];
                setDefaultAccount(defaultAccount);
            }
        }

        return defaultAccount;
    }

    public void setDefaultAccount(EmAccount account) {
        mSharedPreferences.edit().putString(DEFAULT_ACCOUNT_UUID, account.getUuid()).commit();
    }

    public void setEnableDebugLogging(boolean value) {
        mSharedPreferences.edit().putBoolean(ENABLE_DEBUG_LOGGING, value).commit();
    }

    public boolean getEnableDebugLogging() {
        return mSharedPreferences.getBoolean(ENABLE_DEBUG_LOGGING, false);
    }

    public void setEnableSensitiveLogging(boolean value) {
        mSharedPreferences.edit().putBoolean(ENABLE_SENSITIVE_LOGGING, value).commit();
    }

    public boolean getEnableSensitiveLogging() {
        return mSharedPreferences.getBoolean(ENABLE_SENSITIVE_LOGGING, false);
    }

    public void setEnableExchangeLogging(boolean value) {
        mSharedPreferences.edit().putBoolean(ENABLE_EXCHANGE_LOGGING, value).commit();
    }

    public boolean getEnableExchangeLogging() {
        return mSharedPreferences.getBoolean(ENABLE_EXCHANGE_LOGGING, false);
    }

    public void setEnableExchangeFileLogging(boolean value) {
        mSharedPreferences.edit().putBoolean(ENABLE_EXCHANGE_FILE_LOGGING, value).commit();
    }

    public boolean getEnableExchangeFileLogging() {
        return mSharedPreferences.getBoolean(ENABLE_EXCHANGE_FILE_LOGGING, false);
    }

    /**
     * Generate a new "device UID".  This is local to Email app only, to prevent possibility
     * of correlation with any other user activities in any other apps.
     * @return a persistent, unique ID
     */
    public synchronized String getDeviceUID() {
         String result = mSharedPreferences.getString(DEVICE_UID, null);
         if (result == null) {
             result = UUID.randomUUID().toString();
             mSharedPreferences.edit().putString(DEVICE_UID, result).commit();
         }
         return result;
    }

    public int getOneTimeInitializationProgress() {
        return mSharedPreferences.getInt(ONE_TIME_INITIALIZATION_PROGRESS, 0);
    }

    public void setOneTimeInitializationProgress(int progress) {
        mSharedPreferences.edit().putInt(ONE_TIME_INITIALIZATION_PROGRESS, progress).commit();
    }

    public void save() {
    }

    public void clear() {
        mSharedPreferences.edit().clear().commit();
    }

    public void dump() {
        if (Email.LOGD) {
            for (String key : mSharedPreferences.getAll().keySet()) {
                Log.v(Email.LOG_TAG, key + " = " + mSharedPreferences.getAll().get(key));
            }
        }
    }
}
