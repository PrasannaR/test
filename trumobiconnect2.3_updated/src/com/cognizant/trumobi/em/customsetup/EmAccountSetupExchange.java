/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cognizant.trumobi.em.customsetup;

import java.net.URI;
import java.net.URISyntaxException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobBaseEditText;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseActivity;
import com.cognizant.trumobi.em.EmAccountBackupRestore;
import com.cognizant.trumobi.em.EmExchangeUtils;
import com.cognizant.trumobi.em.EmUtility;
import com.cognizant.trumobi.em.activity.EmWelcome;
import com.cognizant.trumobi.em.activity.setup.EmAccountSetupOptions;
import com.cognizant.trumobi.em.provider.EmEmailContent;
import com.cognizant.trumobi.em.provider.EmEmailContent.Account;
import com.cognizant.trumobi.em.provider.EmEmailContent.HostAuth;
import com.cognizant.trumobi.exchange.provider.EmExchangeData;

/**
 * Provides generic setup for Exchange accounts.  The following fields are supported:
 *
 *  Email Address   (from previous setup screen)
 *  Server
 *  Domain
 *  Requires SSL?
 *  User (login)
 *  Password
 *
 * There are two primary paths through this activity:
 *   Edit existing:
 *     Load existing values from account into fields
 *     When user clicks 'next':
 *       Confirm not a duplicate account
 *       Try new values (check settings)
 *       If new values are OK:
 *         Write new values (save to provider)
 *         finish() (pop to previous)
 *
 *   Creating New:
 *     Try Auto-discover to get details from server
 *     If Auto-discover reports an authentication failure:
 *       finish() (pop to previous, to re-enter username & password)
 *     If Auto-discover succeeds:
 *       write server's account details into account
 *     Load values from account into fields
 *     Confirm not a duplicate account
 *     Try new values (check settings)
 *     If new values are OK:
 *       Write new values (save to provider)
 *       Proceed to options screen
 *       finish() (removes self from back stack)
 *
 * NOTE: The manifest for this activity has it ignore config changes, because
 * we don't want to restart on every orientation - this would launch autodiscover again.
 * Do not attempt to define orientation-specific resources, they won't be loaded.
 */
public class EmAccountSetupExchange extends TruMobiBaseActivity implements OnClickListener,
        OnCheckedChangeListener {
    /*package*/ static final String EXTRA_ACCOUNT = "account";
    private static final String EXTRA_MAKE_DEFAULT = "makeDefault";
    private static final String EXTRA_EAS_FLOW = "easFlow";
    /*package*/ static final String EXTRA_DISABLE_AUTO_DISCOVER = "disableAutoDiscover";

    private final static int DIALOG_DUPLICATE_ACCOUNT = 1;

    private TruMobBaseEditText mUsernameView;
    private TruMobBaseEditText mPasswordView;
    private TruMobBaseEditText mServerView;
    private CheckBox mSslSecurityView;
    private CheckBox mTrustCertificatesView;

    private Button mNextButton;
    private Button mCancelButton;
    private Account mAccount;
    private boolean mMakeDefault;
    private String mCacheLoginCredential;
    private String mDuplicateAccountName;

    public static void actionIncomingSettings(Activity fromActivity, Account account,
            boolean makeDefault, boolean easFlowMode, boolean allowAutoDiscover) {
        Intent i = new Intent(fromActivity, EmAccountSetupExchange.class);
        i.putExtra(EXTRA_ACCOUNT, account);
        i.putExtra(EXTRA_MAKE_DEFAULT, makeDefault);
        i.putExtra(EXTRA_EAS_FLOW, easFlowMode);
        if (!allowAutoDiscover) {
            i.putExtra(EXTRA_DISABLE_AUTO_DISCOVER, true);
        }
        fromActivity.startActivity(i);
    }

    public static void actionEditIncomingSettings(Activity fromActivity, Account account)
            {
        Intent i = new Intent(fromActivity, EmAccountSetupExchange.class);
        i.setAction(Intent.ACTION_EDIT);
        i.putExtra(EXTRA_ACCOUNT, account);
        fromActivity.startActivity(i);
    }

    /**
     * For now, we'll simply replicate outgoing, for the purpose of satisfying the
     * account settings flow.
     */
    public static void actionEditOutgoingSettings(Activity fromActivity, Account account)
            {
        Intent i = new Intent(fromActivity, EmAccountSetupExchange.class);
        i.setAction(Intent.ACTION_EDIT);
        i.putExtra(EXTRA_ACCOUNT, account);
        fromActivity.startActivity(i);
    }
    
    @Override
    protected void onDestroy() {	//NaGa
    	// TODO Auto-generated method stub
    	super.onDestroy();
    }
    
    @Override
    protected void onResume() {
    	// TODO Auto-generated method stub
    	super.onResume();
    	Log.e("AccountSetupCheckSettings", "onResumeonResumeonResume");
    	if(EmAccountSetupCheckSettings.finishAll)
        {
        	EmAccountSetupCheckSettings.finishAll = false;
        	this.finish();
        }
    }
    
    @Override
    public void onBackPressed() {
    	// TODO Auto-generated method stub
    	super.onBackPressed();
    	Log.e("AccountSetupExchange","onBackPressed");
    	this.finish();
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.em_account_setup_check_settings);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.em_customtitlebar);
        Log.v("Checkingggggggg","22222222222222222222222222");
        ImageView img = (ImageView) findViewById(R.id.connectHome);
        img.setVisibility(View.GONE);
        
        mCancelButton = (Button) findViewById(R.id.cancel);
        mCancelButton.setVisibility(View.INVISIBLE);

        /*
         * Calls validateFields() which enables or disables the Next button
         * based on the fields' validity.
         */
        TextWatcher validationTextWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {
                validateFields();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        };
        

        Intent intent = getIntent();
        mAccount = (EmEmailContent.Account) intent.getParcelableExtra(EXTRA_ACCOUNT);
        mMakeDefault = intent.getBooleanExtra(EXTRA_MAKE_DEFAULT, false);

        /*
         * If we're being reloaded we override the original account with the one
         * we saved
         */
        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_ACCOUNT)) {
            mAccount = (EmEmailContent.Account) savedInstanceState.getParcelable(EXTRA_ACCOUNT);
        }

        loadFields(mAccount);
        //validateFields();

        // If we've got a username and password and we're NOT editing, try autodiscover
        String username = mAccount.mHostAuthRecv.mLogin;
        String password = "xxxxx";//mAccount.mHostAuthRecv.mPassword;
        if (username != null && password != null &&
                !Intent.ACTION_EDIT.equals(intent.getAction())) {
            // NOTE: Disabling AutoDiscover is only used in unit tests
            boolean disableAutoDiscover =
                intent.getBooleanExtra(EXTRA_DISABLE_AUTO_DISCOVER, false);
            if (!disableAutoDiscover) {
                EmAccountSetupCheckSettings
                    .actionAutoDiscover(this, mAccount, mAccount.mEmailAddress, password);
            }
        }
        onNext();
        //EXCHANGE-REMOVE-SECTION-START
        // Show device ID
        //try {
            //((TextView) findViewById(R.id.device_id)).setText(SyncManager.getDeviceId(this));
        //} catch (IOException ignore) {
            // There's nothing we can do here...
        //}
        //EXCHANGE-REMOVE-SECTION-END
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_ACCOUNT, mAccount);
    }

    private boolean usernameFieldValid(TruMobBaseEditText usernameView) {
        return EmUtility.requiredFieldValid(usernameView) &&
            !usernameView.getText().toString().equals("\\");
    }

    /**
     * Prepare a cached dialog with current values (e.g. account name)
     */
    @Override
    public Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DUPLICATE_ACCOUNT:
                return new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.account_duplicate_dlg_title)
                    .setMessage(getString(R.string.account_duplicate_dlg_message_fmt,
                            mDuplicateAccountName))
                    .setPositiveButton(R.string.okay_action,
                            new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dismissDialog(DIALOG_DUPLICATE_ACCOUNT);
                        }
                    })
                    .create();
        }
        return null;
    }

    /**
     * Update a cached dialog with current values (e.g. account name)
     */
    @Override
    public void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DIALOG_DUPLICATE_ACCOUNT:
                if (mDuplicateAccountName != null) {
                    AlertDialog alert = (AlertDialog) dialog;
                    alert.setMessage(getString(R.string.account_duplicate_dlg_message_fmt,
                            mDuplicateAccountName));
                }
                break;
        }
    }

    /**
     * Copy mAccount's values into UI fields
     */
    /* package */ void loadFields(Account account) {
        HostAuth hostAuth = account.mHostAuthRecv;

        String userName = hostAuth.mLogin;
        if (userName != null) {
            // Add a backslash to the start of the username, but only if the username has no
            // backslash in it.
            if (userName.indexOf('\\') < 0) {
                userName = "\\" + userName;
            }
           
        }

        

        String protocol = hostAuth.mProtocol;
        if (protocol == null || !protocol.startsWith("eas")) {
            throw new Error("Unknown account type: " + account.getStoreUri(this));
        }

       

        boolean ssl = 0 != (hostAuth.mFlags & HostAuth.FLAG_SSL);
        boolean trustCertificates = 0 != (hostAuth.mFlags & HostAuth.FLAG_TRUST_ALL_CERTIFICATES);
       
        
    }

    /**
     * Check the values in the fields and decide if it makes sense to enable the "next" button
     * NOTE:  Does it make sense to extract & combine with similar code in AccountSetupIncoming?
     * @return true if all fields are valid, false if fields are incomplete
     */
    private boolean validateFields() {
        boolean enabled = usernameFieldValid(mUsernameView)
                && EmUtility.requiredFieldValid(mPasswordView)
                && EmUtility.requiredFieldValid(mServerView);
        if (enabled) {
            try {
                URI uri = getUri();
            } catch (URISyntaxException use) {
                enabled = false;
            }
        }
        mNextButton.setEnabled(enabled);
        EmUtility.setCompoundDrawablesAlpha(mNextButton, enabled ? 255 : 128);
        return enabled;
    }

    private void doOptions() {
        boolean easFlowMode = getIntent().getBooleanExtra(EXTRA_EAS_FLOW, false);
        EmAccountSetupOptions.actionOptions(this, mAccount, mMakeDefault, easFlowMode);
        finish();
    }

    /**
     * There are three cases handled here, so we split out into separate sections.
     * 1.  Validate existing account (edit)
     * 2.  Validate new account
     * 3.  Autodiscover for new account
     *
     * For each case, there are two or more paths for success or failure.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EmAccountSetupCheckSettings.REQUEST_CODE_VALIDATE) {
            if (Intent.ACTION_EDIT.equals(getIntent().getAction())) {
                doActivityResultValidateExistingAccount(resultCode, data);
            } else {
                doActivityResultValidateNewAccount(resultCode, data);
            }
        } else if (requestCode == EmAccountSetupCheckSettings.REQUEST_CODE_AUTO_DISCOVER) {
            doActivityResultAutoDiscoverNewAccount(resultCode, data);
        }
    }

    /**
     * Process activity result when validating existing account
     */
    private void doActivityResultValidateExistingAccount(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (mAccount.isSaved()) {
                // Account.update will NOT save the HostAuth's
                mAccount.update(this, mAccount.toContentValues());
                mAccount.mHostAuthRecv.update(this,
                        mAccount.mHostAuthRecv.toContentValues());
                mAccount.mHostAuthSend.update(this,
                        mAccount.mHostAuthSend.toContentValues());
                if (mAccount.mHostAuthRecv.mProtocol.equals("eas")) {
                    // For EAS, notify SyncManager that the password has changed
                    try {
                        EmExchangeUtils.getExchangeEmailService(this, null)
                        .hostChanged(mAccount.mId);
                    } catch (RemoteException e) {
                        // Nothing to be done if this fails
                    }
                }
            } else {
                // Account.save will save the HostAuth's
                mAccount.save(this);
            }
            // Update the backup (side copy) of the accounts
            EmAccountBackupRestore.backupAccounts(this);
            finish();
        }
        // else (resultCode not OK) - just return into this activity for further editing
    }

    /**
     * Process activity result when validating new account
     */
    private void doActivityResultValidateNewAccount(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            // Go directly to next screen
            doOptions();
        } else if (resultCode == EmAccountSetupCheckSettings.RESULT_SECURITY_REQUIRED_USER_CANCEL) {
            finish();
        }
        // else (resultCode not OK) - just return into this activity for further editing
    }

    /**
     * Process activity result when validating new account
     */
    private void doActivityResultAutoDiscoverNewAccount(int resultCode, Intent data) {
        // If authentication failed, exit immediately (to re-enter credentials)
        if (resultCode == EmAccountSetupCheckSettings.RESULT_AUTO_DISCOVER_AUTH_FAILED) {
            finish();
            return;
        }

        // If data was returned, populate the account & populate the UI fields and validate it
        if (data != null) {
            Parcelable p = data.getParcelableExtra("HostAuth");
            if (p != null) {
                HostAuth hostAuth = (HostAuth)p;
                mAccount.mHostAuthSend = hostAuth;
                mAccount.mHostAuthRecv = hostAuth;
                loadFields(mAccount);
                if (validateFields()) {
                    // "click" next to launch server verification
                    onNext();
                }
            }
        }
        // Otherwise, proceed into this activity for manual setup
    }

    /**
     * Attempt to create a URI from the fields provided.  Throws URISyntaxException if there's
     * a problem with the user input.
     * @return a URI built from the account setup fields
     */
    /* package */ URI getUri() throws URISyntaxException {
        boolean sslRequired = true;//mSslSecurityView.isChecked();
        boolean trustCertificates = false;//mTrustCertificatesView.isChecked();
        String scheme = (sslRequired)
                        ? (trustCertificates ? "eas+ssl+trustallcerts" : "eas+ssl+")
                        : "eas";
        EmExchangeData mExchangeData = EmExchangeData.getInstance(this);
        String email =  mExchangeData.getEmail_ID().trim();
        String[] emailParts = email.split("@");
        //String user = emailParts[0];
        String userName = emailParts[0];//mUsernameView.getText().toString().trim();
        // Remove a leading backslash, if there is one, since we now automatically put one at
        // the start of the username field
        if (userName.startsWith("\\")) {
            userName = userName.substring(1);
        }
        mCacheLoginCredential = userName;
        String userInfo = userName + ":" + "";//mPasswordView.getText();
        String host = mExchangeData.getServer();//mServerView.getText().toString().trim();	//Naga
        String path = null;

        URI uri = new URI(
                scheme,
                userInfo,
                host,
                0,
                path,
                null,
                null);

        return uri;
    }

    /**
     * Note, in EAS, store & sender are the same, so we always populate them together
     */
    private void onNext() {
        try {
            URI uri = getUri();
            mAccount.setStoreUri(this, uri.toString());
            mAccount.setSenderUri(this, uri.toString());

            // Stop here if the login credentials duplicate an existing account
            // (unless they duplicate the existing account, as they of course will)
            mDuplicateAccountName = EmUtility.findDuplicateAccount(this, mAccount.mId,
                    uri.getHost(), mCacheLoginCredential);
            if (mDuplicateAccountName != null) {
                this.showDialog(DIALOG_DUPLICATE_ACCOUNT);
                return;
            }
        } catch (URISyntaxException use) {
            /*
             * It's unrecoverable if we cannot create a URI from components that
             * we validated to be safe.
             */
            throw new Error(use);
        }

        EmAccountSetupCheckSettings.actionValidateSettings(this, mAccount, true, false);
        //finish();	//NaGa
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.next:
                onNext();
                break;
        }
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.account_ssl) {
            mTrustCertificatesView.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        }
    }
}
