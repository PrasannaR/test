/*
 * Copyright (C) 2008 The Android Open Source Project
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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobBaseEditText;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseActivity;
import com.cognizant.trumobi.em.EmAccountBackupRestore;
import com.cognizant.trumobi.em.EmEmailAddressValidator;
import com.cognizant.trumobi.em.EmUtility;
import com.cognizant.trumobi.em.EmVendorPolicyLoader;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.em.activity.EmDebug;
import com.cognizant.trumobi.em.activity.EmMessageList;
import com.cognizant.trumobi.em.activity.EmWelcome;
import com.cognizant.trumobi.em.activity.setup.EmAccountSettingsUtils;
import com.cognizant.trumobi.em.activity.setup.EmAccountSettingsUtils.Provider;
import com.cognizant.trumobi.em.activity.setup.EmAccountSetupNames;
import com.cognizant.trumobi.em.provider.EmEmailContent;
import com.cognizant.trumobi.em.provider.EmEmailContent.Account;
import com.cognizant.trumobi.em.provider.EmEmailContent.Mailbox;
import com.cognizant.trumobi.exchange.provider.EmExchangeData;

/**
 * 
 * KEYCODE			    AUTHOR		PURPOSE
 * SECRET_CODE		    367712		Commenting Secret Code Implementation
 *
 */

/**
 * Prompts the user for the email address and password. Also prompts for
 * "Use this account as default" if this is the 2nd+ account being set up.
 * Attempts to lookup default settings for the domain the user specified. If the
 * domain is known the settings are handed off to the AccountSetupCheckSettings
 * activity. If no settings are found the settings are handed off to the
 * AccountSetupAccountType activity.
 */
public class EmAccountSetupBasics extends TruMobiBaseActivity
        implements OnClickListener, TextWatcher {
    private final static boolean ENTER_DEBUG_SCREEN = true;

    private final static String EXTRA_ACCOUNT = "com.cognizant.trumobi.em.AccountSetupBasics.account";
    public final static String EXTRA_EAS_FLOW = "com.cognizant.trumobi.em.extra.eas_flow";

    // Action asking us to return to our original caller (i.e. finish)
    private static final String ACTION_RETURN_TO_CALLER =
        "com.cognizant.trumobi.em.AccountSetupBasics.return";
    // Action asking us to restart the task from the Welcome activity (which will figure out the
    // right place to go) and finish
    private static final String ACTION_START_AT_MESSAGE_LIST =
        "com.cognizant.trumobi.em.AccountSetupBasics.messageList";

    private final static String EXTRA_USERNAME = "com.cognizant.trumobi.em.AccountSetupBasics.username";
    private final static String EXTRA_PASSWORD = "com.cognizant.trumobi.em.AccountSetupBasics.password";

    private final static int DIALOG_NOTE = 1;
    private final static int DIALOG_DUPLICATE_ACCOUNT = 2;

    private final static String STATE_KEY_PROVIDER =
        "com.cognizant.trumobi.em.AccountSetupBasics.provider";

    // NOTE: If you change this value, confirm that the new interval exists in arrays.xml
    private final static int DEFAULT_ACCOUNT_CHECK_INTERVAL = 15;

    private TruMobBaseEditText mEmailView;
    private TruMobBaseEditText mPasswordView;
    private CheckBox mDefaultView;
    private Button mNextButton;
    private Button mManualSetupButton;
    private EmEmailContent.Account mAccount;
    private Provider mProvider;
    private boolean mEasFlowMode;
    private String mDuplicateAccountName;
    
    private EmExchangeData mExchangeData;

    private EmEmailAddressValidator mEmailValidator = new EmEmailAddressValidator();

    public static void actionNewAccount(Activity fromActivity) {
        Intent i = new Intent(fromActivity, EmAccountSetupBasics.class);
        fromActivity.startActivity(i);
    }

    public static void actionNewAccountWithCredentials(Activity fromActivity,
            String username, String password, boolean easFlow) {
        Intent i = new Intent(fromActivity, EmAccountSetupBasics.class);
        i.putExtra(EXTRA_USERNAME, username);
        i.putExtra(EXTRA_PASSWORD, password);
        i.putExtra(EXTRA_EAS_FLOW, easFlow);
        fromActivity.startActivity(i);
    }

    /**
     * This creates an intent that can be used to start a self-contained account creation flow
     * for exchange accounts.
     */
    public static Intent actionSetupExchangeIntent(Context context) {
        Intent i = new Intent(context, EmAccountSetupBasics.class);
        i.putExtra(EXTRA_EAS_FLOW, true);
        return i;
    }

    public static void actionAccountCreateFinishedEas(Activity fromActivity) {
        Intent i= new Intent(fromActivity, EmAccountSetupBasics.class);
        // If we're in the "eas flow" (from AccountManager), we want to return to the caller
        // (in the settings app)
        i.putExtra(EmAccountSetupBasics.ACTION_RETURN_TO_CALLER, true);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        fromActivity.startActivity(i);
    }

    public static void actionAccountCreateFinished(Activity fromActivity, long accountId) {
        Intent i= new Intent(fromActivity, EmAccountSetupBasics.class);
        // If we're not in the "eas flow" (from AccountManager), we want to show the message list
        // for the new inbox
        i.putExtra(EmAccountSetupBasics.ACTION_START_AT_MESSAGE_LIST, accountId);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        fromActivity.startActivity(i);
    }
    
    //private Button mCancelButton;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent.getBooleanExtra(ACTION_RETURN_TO_CALLER, false)) {
            // Return to the caller who initiated account creation
            finish();
            return;
        } else {
            long accountId = intent.getLongExtra(ACTION_START_AT_MESSAGE_LIST, -1);
            if (accountId >= 0) {
                // Show the message list for the new account
                EmMessageList.actionHandleAccount(this, accountId, Mailbox.TYPE_INBOX, false);
                finish();
                return;
            }
        }
        mExchangeData = EmExchangeData.getInstance(this);
       // mExchangeData.setCertificateData(this);
        mExchangeData.getEasDetails();
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);	//NaGa, adding title Bar
        setContentView(R.layout.em_account_setup_incoming);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.em_customtitlebar);
        ImageView img = (ImageView) findViewById(R.id.connectHome);
        img.setVisibility(View.GONE);
        
        //mCancelButton = (Button) findViewById(R.id.cancel);
        //mCancelButton.setVisibility(View.INVISIBLE);
        

        // Find out how many accounts we have, and if there one or more, then we have a choice
        // about being default or not.
        Cursor c = null;
        try {
            c = getContentResolver().query(
                    EmEmailContent.Account.CONTENT_URI,
                    EmEmailContent.Account.ID_PROJECTION,
                    null, null, null);
            if (c.getCount() > 0) {
                //mDefaultView.setVisibility(View.VISIBLE);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        mEasFlowMode = intent.getBooleanExtra(EXTRA_EAS_FLOW, false);
        if (mEasFlowMode) {
            // No need for manual button -> next is appropriate
            mManualSetupButton.setVisibility(View.GONE);
            // Swap welcome text for EAS-specific text
            TextView welcomeView = (TextView) findViewById(R.id.instructions);
            final boolean alternateStrings =
                    EmVendorPolicyLoader.getInstance(this).useAlternateExchangeStrings();
            setTitle(alternateStrings
                    ? R.string.account_setup_basics_exchange_title_alternate
                    : R.string.account_setup_basics_exchange_title);
            welcomeView.setText(alternateStrings
                    ? R.string.accounts_welcome_exchange_alternate
                    : R.string.accounts_welcome_exchange);
        }

        if (intent.hasExtra(EXTRA_USERNAME)) {
            mEmailView.setText(intent.getStringExtra(EXTRA_USERNAME));
        }
        if (intent.hasExtra(EXTRA_PASSWORD)) {
            mPasswordView.setText(intent.getStringExtra(EXTRA_PASSWORD));
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_ACCOUNT)) {
            mAccount = (EmEmailContent.Account)savedInstanceState.getParcelable(EXTRA_ACCOUNT);
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_KEY_PROVIDER)) {
            mProvider = (Provider)savedInstanceState.getSerializable(STATE_KEY_PROVIDER);
        }
        Log.v("You areeeeeee", "1111111111111111111111111");
        onNext();
    }

    @Override
    public void onResume() {
        super.onResume();
        validateFields();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_ACCOUNT, mAccount);
        if (mProvider != null) {
            outState.putSerializable(STATE_KEY_PROVIDER, mProvider);
        }
    }

    public void afterTextChanged(Editable s) {
        validateFields();
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    private void validateFields() {
       
    }

    private String getOwnerName() {
        String name = null;
/* TODO figure out another way to get the owner name
        String projection[] = {
            ContactMethods.NAME
        };
        Cursor c = getContentResolver().query(
                Uri.withAppendedPath(Contacts.People.CONTENT_URI, "owner"), projection, null, null,
                null);
        if (c != null) {
            if (c.moveToFirst()) {
                name = c.getString(0);
            }
            c.close();
        }
*/

        if (name == null || name.length() == 0) {
            long defaultId = Account.getDefaultAccountId(this);
            if (defaultId != -1) {
                Account account = Account.restoreAccountWithId(this, defaultId);
                if (account != null) {
                    name = account.getSenderName();
                }
            }
        }
        return name;
    }

    @Override
    public Dialog onCreateDialog(int id) {
        if (id == DIALOG_NOTE) {
            if (mProvider != null && mProvider.note != null) {
                return new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(android.R.string.dialog_alert_title)
                .setMessage(mProvider.note)
                .setPositiveButton(
                        getString(R.string.okay_action),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finishAutoSetup();
                            }
                        })
                        .setNegativeButton(
                                getString(R.string.cancel_action),
                                null)
                                .create();
            }
        } else if (id == DIALOG_DUPLICATE_ACCOUNT) {
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
            case DIALOG_NOTE:
                if (mProvider != null && mProvider.note != null) {
                    AlertDialog alert = (AlertDialog) dialog;
                    alert.setMessage(mProvider.note);
                }
                break;
            case DIALOG_DUPLICATE_ACCOUNT:
                if (mDuplicateAccountName != null) {
                    AlertDialog alert = (AlertDialog) dialog;
                    alert.setMessage(getString(R.string.account_duplicate_dlg_message_fmt,
                            mDuplicateAccountName));
                }
                break;
        }
    }

    private void finishAutoSetup() {
        String email = mExchangeData.getEmail_ID().trim();//mEmailView.getText().toString().trim();
        String password = "xxxxxx";//mPasswordView.getText().toString();
        String[] emailParts = email.split("@");
        String user = emailParts[0];
        String domain = emailParts[1];
        URI incomingUri = null;
        URI outgoingUri = null;
        try {
            String incomingUsername = mProvider.incomingUsernameTemplate;
            incomingUsername = incomingUsername.replaceAll("\\$email", email);
            incomingUsername = incomingUsername.replaceAll("\\$user", user);
            incomingUsername = incomingUsername.replaceAll("\\$domain", domain);

            URI incomingUriTemplate = mProvider.incomingUriTemplate;
            incomingUri = new URI(incomingUriTemplate.getScheme(), incomingUsername + ":"
                    + password, incomingUriTemplate.getHost(), incomingUriTemplate.getPort(),
                    incomingUriTemplate.getPath(), null, null);

            String outgoingUsername = mProvider.outgoingUsernameTemplate;
            outgoingUsername = outgoingUsername.replaceAll("\\$email", email);
            outgoingUsername = outgoingUsername.replaceAll("\\$user", user);
            outgoingUsername = outgoingUsername.replaceAll("\\$domain", domain);

            URI outgoingUriTemplate = mProvider.outgoingUriTemplate;
            outgoingUri = new URI(outgoingUriTemplate.getScheme(), outgoingUsername + ":"
                    + password, outgoingUriTemplate.getHost(), outgoingUriTemplate.getPort(),
                    outgoingUriTemplate.getPath(), null, null);

            // Stop here if the login credentials duplicate an existing account
            mDuplicateAccountName = EmUtility.findDuplicateAccount(this, -1,
                    incomingUri.getHost(), incomingUsername);
            if (mDuplicateAccountName != null) {
                this.showDialog(DIALOG_DUPLICATE_ACCOUNT);
                return;
            }

        } catch (URISyntaxException use) {
            /*
             * If there is some problem with the URI we give up and go on to
             * manual setup.  Technically speaking, AutoDiscover is OK here, since user clicked
             * "Next" to get here.  This would never happen in practice because we don't expect
             * to find any EAS accounts in the providers list.
             */
            onManualSetup(true);
            return;
        }

        mAccount = new EmEmailContent.Account();
        mAccount.setSenderName(getOwnerName());
        mAccount.setEmailAddress(email);
        mAccount.setStoreUri(this, incomingUri.toString());
        mAccount.setSenderUri(this, outgoingUri.toString());
/* TODO figure out the best way to implement this concept
        mAccount.setDraftsFolderName(getString(R.string.special_mailbox_name_drafts));
        mAccount.setTrashFolderName(getString(R.string.special_mailbox_name_trash));
        mAccount.setOutboxFolderName(getString(R.string.special_mailbox_name_outbox));
        mAccount.setSentFolderName(getString(R.string.special_mailbox_name_sent));
*/
        if (incomingUri.toString().startsWith("imap")) {
            // Delete policy must be set explicitly, because IMAP does not provide a UI selection
            // for it. This logic needs to be followed in the auto setup flow as well.
            mAccount.setDeletePolicy(EmEmailContent.Account.DELETE_POLICY_ON_DELETE);
        }
        mAccount.setSyncInterval(DEFAULT_ACCOUNT_CHECK_INTERVAL);
        EmAccountSetupCheckSettings.actionValidateSettings(this, mAccount, true, true);
    }

    private void onNext() {
        // If this is EAS flow, don't try to find a provider for the domain!
        if (!mEasFlowMode) {
            String email = mExchangeData.getEmail_ID().trim();
            String[] emailParts = email.split("@");
            String domain = emailParts[1].trim();
            mProvider = EmAccountSettingsUtils.findProviderForDomain(this, domain);
            if (mProvider != null) {
                if (mProvider.note != null) {
                    showDialog(DIALOG_NOTE);
                } else {
                    finishAutoSetup();
                }
                return;
            }
        }
        // Can't use auto setup (although EAS accounts may still be able to AutoDiscover)
        onManualSetup(false);
    }

    /**
     * This is used in automatic setup mode to jump directly down to the names screen.
     *
     * NOTE:  With this organization, it is *not* possible to auto-create an exchange account,
     * because certain necessary actions happen during AccountSetupOptions (which we are
     * skipping here).
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
        	Log.v("debugggggggggggg", "44444444444444444444444444ty");
            String email = mAccount.getEmailAddress();
            boolean isDefault = mDefaultView.isChecked();
            mAccount.setDisplayName(email);
            mAccount.setDefaultAccount(isDefault);
            // At this point we write the Account object to the DB for the first time.
            // From now on we'll only pass the accountId around.
            mAccount.save(this);
            // Update the backup (side copy) of the accounts
            EmAccountBackupRestore.backupAccounts(this);
            Email.setServicesEnabled(this);
            EmAccountSetupNames.actionSetNames(this, mAccount.mId, false);
            finish();
        }
    }

    /**
     * @param allowAutoDiscover - true if the user clicked 'next' and (if the account is EAS)
     * it's OK to use autodiscover.  false to prevent autodiscover and go straight to manual setup.
     * Ignored for IMAP & POP accounts.
     */
    private void onManualSetup(boolean allowAutoDiscover) {
        String email = mExchangeData.getEmail_ID().trim();//mEmailView.getText().toString().trim();
        String password = "xxxxxx";//mPasswordView.getText().toString();
        String[] emailParts = email.split("@");
        String user = emailParts[0].trim();
        String domain = emailParts[1].trim();

        // Alternate entry to the debug options screen (for devices without a physical keyboard:
        //  Username: d@d.d
        //  Password: debug
        //SECRET_CODE, -->
        /*if (ENTER_DEBUG_SCREEN && "d@d.d".equals(email) && "debug".equals(password)) {
            mEmailView.setText("");
            mPasswordView.setText("");
            startActivity(new Intent(this, EmDebug.class));
            return;
        }*/
        //SECRET_CODE, <--

        mAccount = new EmEmailContent.Account();
        mAccount.setSenderName(getOwnerName());
        mAccount.setEmailAddress(email);
        try {
            URI uri = new URI("placeholder", user + ":" + password, domain, -1, null, null, null);
            mAccount.setStoreUri(this, uri.toString());
            mAccount.setSenderUri(this, uri.toString());
        } catch (URISyntaxException use) {
            // If we can't set up the URL, don't continue - account setup pages will fail too
            Toast.makeText(this, R.string.account_setup_username_password_toast, Toast.LENGTH_LONG)
                    .show();
            mAccount = null;
            return;
        }
/* TODO figure out the best way to implement this concept
        mAccount.setDraftsFolderName(getString(R.string.special_mailbox_name_drafts));
        mAccount.setTrashFolderName(getString(R.string.special_mailbox_name_trash));
        mAccount.setOutboxFolderName(getString(R.string.special_mailbox_name_outbox));
        mAccount.setSentFolderName(getString(R.string.special_mailbox_name_sent));
*/
        mAccount.setSyncInterval(DEFAULT_ACCOUNT_CHECK_INTERVAL);

        EmAccountSetupAccountType.actionSelectAccountType(this, mAccount, true,
				true, allowAutoDiscover);
        finish();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.next:
                onNext();
                break;
            case R.id.manual_setup:
                // no AutoDiscover - user clicked "manual"
                onManualSetup(false);
                break;
        }
    }
}
