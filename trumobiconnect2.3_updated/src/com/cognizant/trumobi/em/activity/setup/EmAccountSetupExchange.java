

/**
*  FileName : EmAccountSetupExchange
* 
*  Desc : 
* 
* 
*     KeyCode				Author                     Date                                         Desc
*     						371990                  07-Oct-2013                         setting Pim configuration as false;
*     PWD_CHANGE_IMPL		367712				  27-12-2013						Warn user when Account password changed on Server 
*/

package com.cognizant.trumobi.em.activity.setup;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.PreparedStatement;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.TruBoxSDK.SharedPreferences;
import com.TruBoxSDK.TruBoxDatabase;
import com.cognizant.trumobi.PersonaLauncher;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobBaseEditText;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseActivity;
import com.cognizant.trumobi.em.EmAccountBackupRestore;
import com.cognizant.trumobi.em.EmExchangeUtils;
import com.cognizant.trumobi.em.EmUtility;
import com.cognizant.trumobi.em.activity.EmWelcome;
import com.cognizant.trumobi.em.customsetup.EmAccountSetupBasics;
import com.cognizant.trumobi.em.provider.EmEmailContent;
import com.cognizant.trumobi.em.provider.EmEmailContent.Account;
import com.cognizant.trumobi.em.provider.EmEmailContent.HostAuth;
import com.cognizant.trumobi.exchange.EmSyncManager;
import com.cognizant.trumobi.exchange.provider.EmExchangeData;

/**
 * Provides generic setup for Exchange accounts. The following fields are
 * supported:
 * 
 * Email Address (from previous setup screen) Server Domain Requires SSL? User
 * (login) Password
 * 
 * There are two primary paths through this activity: Edit existing: Load
 * existing values from account into fields When user clicks 'next': Confirm not
 * a duplicate account Try new values (check settings) If new values are OK:
 * Write new values (save to provider) finish() (pop to previous)
 * 
 * Creating New: Try Auto-discover to get details from server If Auto-discover
 * reports an authentication failure: finish() (pop to previous, to re-enter
 * username & password) If Auto-discover succeeds: write server's account
 * details into account Load values from account into fields Confirm not a
 * duplicate account Try new values (check settings) If new values are OK: Write
 * new values (save to provider) Proceed to options screen finish() (removes
 * self from back stack)
 * 
 * NOTE: The manifest for this activity has it ignore config changes, because we
 * don't want to restart on every orientation - this would launch autodiscover
 * again. Do not attempt to define orientation-specific resources, they won't be
 * loaded.
 */
/**
 * Keycode					Author			Purpose
 * EMAILFORMAT_CHG			290661			email formt made generic for asyncmail and securecontain
 * MANUAL_SETUP_CHG			290661			Set text fields enable/disable based on values from SDK
 */
public class EmAccountSetupExchange extends TruMobiBaseActivity implements
		OnClickListener, OnCheckedChangeListener, OnTouchListener {
	/* package */static final String EXTRA_ACCOUNT = "account";
	private static final String EXTRA_MAKE_DEFAULT = "makeDefault";
	private static final String EXTRA_EAS_FLOW = "easFlow";
	/* package */static final String EXTRA_DISABLE_AUTO_DISCOVER = "disableAutoDiscover";
	private static final String EXTRA_FROM_LOGIN_WARN = "loginwarn";	//PWD_CHANGE_IMPL

	private final static int DIALOG_DUPLICATE_ACCOUNT = 1;

	private TruMobBaseEditText mUsernameView;
	private TruMobBaseEditText mPasswordView;
	private TruMobBaseEditText mServerView;
	private TruMobBaseEditText mPortView;
	private TruMobBaseEditText mDomainView;
	private CheckBox mSslSecurityView;
	private CheckBox mTrustCertificatesView;

	private Button mNextButton;
	private Account mAccount;
	private boolean mMakeDefault;
	private String mCacheLoginCredential;
	private String mDuplicateAccountName;
	private ImageView connectIcon;	//NaGa
	private boolean useManualSetup = false;
	private boolean useEditableSetup = false;

	public static void actionIncomingSettings(Activity fromActivity,
			Account account, boolean makeDefault, boolean easFlowMode,
			boolean allowAutoDiscover) {
		Intent i = new Intent(fromActivity, EmAccountSetupExchange.class);
		i.putExtra(EXTRA_ACCOUNT, account);
		i.putExtra(EXTRA_MAKE_DEFAULT, makeDefault);
		i.putExtra(EXTRA_EAS_FLOW, easFlowMode);
		if (!allowAutoDiscover) {
			i.putExtra(EXTRA_DISABLE_AUTO_DISCOVER, true);
		}
		fromActivity.startActivity(i);
	}

	public static void actionEditIncomingSettings(Activity fromActivity,
			Account account) {
		Intent i = new Intent(fromActivity, EmAccountSetupExchange.class);
		i.setAction(Intent.ACTION_EDIT);
		i.putExtra(EXTRA_ACCOUNT, account);
		i.putExtra(EXTRA_FROM_LOGIN_WARN, true);	//PWD_CHANGE_IMPL
		fromActivity.startActivity(i);
	}

	/**
	 * For now, we'll simply replicate outgoing, for the purpose of satisfying
	 * the account settings flow.
	 */
	public static void actionEditOutgoingSettings(Activity fromActivity,
			Account account) {
		Intent i = new Intent(fromActivity, EmAccountSetupExchange.class);
		i.setAction(Intent.ACTION_EDIT);
		i.putExtra(EXTRA_ACCOUNT, account);
		fromActivity.startActivity(i);
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
//		if(uDomain != null)	finish();
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // NaGa, adding title
															// Bar
		setContentView(R.layout.em_account_setup_exchange);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
			R.layout.em_customtitlebar);
		ImageView img = (ImageView) findViewById(R.id.connectHome);
        img.setVisibility(View.GONE);
		
		TruBoxDatabase.setPimSettings(false);

		if(EmWelcome.useCertBasedSetup)
		{
			EmExchangeData mExchangeData = EmExchangeData.getInstance(this);
			mExchangeData.setCertificateData(this);
			EmAccountSetupBasics.actionNewAccount(this);
			finish();
		}
		
		((ImageView) findViewById(R.id.outlooklogo)).setImageResource(R.drawable.pr_connect_header_icon);
		
		((TextView) findViewById(R.id.title)).setText(R.string.em_account_setup_header);
		
		mUsernameView = (TruMobBaseEditText) findViewById(R.id.account_username);
		mPasswordView = (TruMobBaseEditText) findViewById(R.id.account_password);
		mServerView = (TruMobBaseEditText) findViewById(R.id.account_server);
		mPortView = (TruMobBaseEditText) findViewById(R.id.em_port);
		mDomainView = (TruMobBaseEditText) findViewById(R.id.account_domain_name);
		mSslSecurityView = (CheckBox) findViewById(R.id.account_ssl);
		mSslSecurityView.setOnCheckedChangeListener(this);
		//mTrustCertificatesView = (CheckBox) findViewById(R.id.account_trust_certificates);

		mNextButton = (Button) findViewById(R.id.next);
		mNextButton.setOnClickListener(this);
		
		//290661 added
		
	     String server = getIntent().getStringExtra("server") ;
	     String domain = getIntent().getStringExtra("domain") ;
	     
		
	     // MANUAL_SETUP_CHG
	     
	     boolean enabled = true;
	     useManualSetup = new SharedPreferences(context).getBoolean("useManualSetup", false);
	     useEditableSetup = new SharedPreferences(context).getBoolean("usereditable", false);
	     if(!useManualSetup) {
	    	 enabled= useEditableSetup;
	    	 mServerView.setEnabled(false);
		     mPasswordView.setEnabled(enabled);
		     mUsernameView.setEnabled(enabled);
		     mDomainView.setEnabled(false);
		     mSslSecurityView.setEnabled(false);
		     mPortView.setEnabled(false);
	     }
	     else {
	     mServerView.setEnabled(enabled);
	     mPasswordView.setEnabled(enabled);
	     mUsernameView.setEnabled(enabled);
	     mDomainView.setEnabled(enabled);
	     mSslSecurityView.setEnabled(enabled);
	     mPortView.setEnabled(enabled);
	     }

		if(getIntent().getStringExtra("username")!=null  && getIntent().getStringExtra("password")!=null && !useManualSetup){
			mUsernameView.setText(getIntent().getStringExtra("username"));
			mPasswordView.setText(getIntent().getStringExtra("password"));
			
			//290661 modified
			
			/*mServerView.setText("trubox.cognizant.com");
			mDomainView.setText("truboxmdmdev.com");*/
			
			mServerView.setText(server);
			mDomainView.setText(domain);
			mSslSecurityView.setChecked(true);	//NaGa
			mSslSecurityView.setEnabled(false);
			
		}
	      
		
		
		
		//NaGa, Connect button functionality -->
        connectIcon = (ImageView) findViewById(R.id.connectHome);
        connectIcon.setOnTouchListener(this);

		
		if(getIntent().getStringExtra("username")!=null  && getIntent().getStringExtra("password")!=null && !useManualSetup){
			mUsernameView.setText(getIntent().getStringExtra("username"));
			mPasswordView.setText(getIntent().getStringExtra("password"));
		}
		
		/*
		 * Calls validateFields() which enables or disables the Next button
		 * based on the fields' validity.
		 */
		TextWatcher validationTextWatcher = new TextWatcher() {
			public void afterTextChanged(Editable s) {
				validateFields();
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		};
		mUsernameView.addTextChangedListener(validationTextWatcher);
		mPasswordView.addTextChangedListener(validationTextWatcher);
		mDomainView.addTextChangedListener(validationTextWatcher);
		mPortView.addTextChangedListener(validationTextWatcher);
		mServerView.addTextChangedListener(validationTextWatcher);

		Intent intent = getIntent();
		mAccount = new EmEmailContent.Account();// intent.getParcelableExtra(EXTRA_ACCOUNT);
		mMakeDefault = intent.getBooleanExtra(EXTRA_MAKE_DEFAULT, false);

		/*
		 * If we're being reloaded we override the original account with the one
		 * we saved
		 */
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(EXTRA_ACCOUNT)) {
			mAccount = (EmEmailContent.Account) savedInstanceState
					.getParcelable(EXTRA_ACCOUNT);
		}
		
		//PWD_CHANGE_IMPL, -->
		if (getIntent() != null
				&& getIntent().getBooleanExtra(EXTRA_FROM_LOGIN_WARN, false))
		{
			mAccount = getIntent().getParcelableExtra(EXTRA_ACCOUNT);
			loadFields(mAccount);
		}
		//PWD_CHANGE_IMPL, <--
		
		validateFields();

		// If we've got a username and password and we're NOT editing, try
		// autodiscover
		// String username = mAccount.mHostAuthRecv.mLogin;
		// String password = mAccount.mHostAuthRecv.mPassword;
		// if (username != null && password != null &&
		// !Intent.ACTION_EDIT.equals(intent.getAction())) {
		// // NOTE: Disabling AutoDiscover is only used in unit tests
		// boolean disableAutoDiscover =
		// intent.getBooleanExtra(EXTRA_DISABLE_AUTO_DISCOVER, false);
		// if (!disableAutoDiscover) {
		// EmAccountSetupCheckSettings
		// .actionAutoDiscover(this, mAccount, mAccount.mEmailAddress,
		// password);
		// }
		// }

		// EXCHANGE-REMOVE-SECTION-START
		// Show device ID
		try {
			((TextView) findViewById(R.id.device_id)).setText(EmSyncManager
					.getDeviceId(this));
		} catch (IOException ignore) {
			// There's nothing we can do here...
		}
		// EXCHANGE-REMOVE-SECTION-END
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(EXTRA_ACCOUNT, mAccount);
	}

	private boolean usernameFieldValid(TruMobBaseEditText usernameView) {
		return EmUtility.requiredFieldValid(usernameView)
				&& !usernameView.getText().toString().equals("\\");
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
					.setMessage(
							getString(
									R.string.account_duplicate_dlg_message_fmt,
									mDuplicateAccountName))
					.setPositiveButton(R.string.okay_action,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dismissDialog(DIALOG_DUPLICATE_ACCOUNT);
								}
							}).create();
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
				alert.setMessage(getString(
						R.string.account_duplicate_dlg_message_fmt,
						mDuplicateAccountName));
			}
			break;
		}
	}

	/**
	 * Copy mAccount's values into UI fields
	 */
	/* package */void loadFields(Account account) {
		HostAuth hostAuth = account.mHostAuthRecv;

		String userName = hostAuth.mLogin;
		if (userName != null) {
			// Add a backslash to the start of the username, but only if the
			// username has no
			// backslash in it.
			if (userName.indexOf('\\') < 0) {
				userName = "\\" + userName;
			}
			//PWD_CHANGE_IMPL, -->
			if(userName.contains("\\"))
			{
				userName = userName.substring(userName.indexOf('\\')+1);
			}
			//PWD_CHANGE_IMPL, <--
			mUsernameView.setText(userName);
			mUsernameView.setEnabled(false);
		}

		if (hostAuth.mPassword != null) {
			mPasswordView.setText(hostAuth.mPassword);
		}
		
		//PWD_CHANGE_IMPL, -->
		String domain = hostAuth.mDomain;
		if(domain != null)
		{
			mDomainView.setText(domain);
			mDomainView.setEnabled(false);
		}
		
		int port = hostAuth.mPort;
		if(domain != null)
		{
			mPortView.setText(""+port);
			mPortView.setEnabled(false);
		}
		//PWD_CHANGE_IMPL, <--
		
		String protocol = hostAuth.mProtocol;
		if (protocol == null || !protocol.startsWith("eas")) {
			throw new Error("Unknown account type: "
					+ account.getStoreUri(this));
		}

		if (hostAuth.mAddress != null) {
			
			mServerView.setText(hostAuth.mAddress);
			mServerView.setEnabled(false);
			mServerView.setClickable(false);
			
		}
		

		boolean ssl = 0 != (hostAuth.mFlags & HostAuth.FLAG_SSL);
		boolean trustCertificates = 0 != (hostAuth.mFlags & HostAuth.FLAG_TRUST_ALL_CERTIFICATES);
		mSslSecurityView.setChecked(ssl);
		mSslSecurityView.setEnabled(false);
		//mTrustCertificatesView.setChecked(trustCertificates);
		//mTrustCertificatesView.setVisibility(ssl ? View.VISIBLE : View.GONE);
	}

	/**
	 * Check the values in the fields and decide if it makes sense to enable the
	 * "next" button NOTE: Does it make sense to extract & combine with similar
	 * code in AccountSetupIncoming?
	 * 
	 * @return true if all fields are valid, false if fields are incomplete
	 */
	private boolean validateFields() {
		boolean enabled = usernameFieldValid(mUsernameView)
				&& EmUtility.requiredFieldValid(mPasswordView)
				&& EmUtility.requiredFieldValid(mServerView)
				&& EmUtility.isPortFieldValid(mPortView);
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
		boolean easFlowMode = getIntent()
				.getBooleanExtra(EXTRA_EAS_FLOW, false);
		EmAccountSetupOptions.actionOptions(this, mAccount, mMakeDefault,
				easFlowMode);
		finish();
	}

	/**
	 * There are three cases handled here, so we split out into separate
	 * sections. 1. Validate existing account (edit) 2. Validate new account 3.
	 * Autodiscover for new account
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
	private void doActivityResultValidateExistingAccount(int resultCode,
			Intent data) {
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
		// else (resultCode not OK) - just return into this activity for further
		// editing
	}

	/**
	 * Process activity result when validating new account
	 */
	private void doActivityResultValidateNewAccount(int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			// Go directly to next screen
			doOptions();
		} else if (resultCode == EmAccountSetupCheckSettings.RESULT_SECURITY_REQUIRED_USER_CANCEL) {
			//finish(); //371990 Geetha comment it based on requirement to edit the details
		}
		// else (resultCode not OK) - just return into this activity for further
		// editing
	}

	/**
	 * Process activity result when validating new account
	 */
	private void doActivityResultAutoDiscoverNewAccount(int resultCode,
			Intent data) {
		// If authentication failed, exit immediately (to re-enter credentials)
		if (resultCode == EmAccountSetupCheckSettings.RESULT_AUTO_DISCOVER_AUTH_FAILED) {
			finish();
			return;
		}

		// If data was returned, populate the account & populate the UI fields
		// and validate it
		if (data != null) {
			Parcelable p = data.getParcelableExtra("HostAuth");
			if (p != null) {
				HostAuth hostAuth = (HostAuth) p;
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
	 * Attempt to create a URI from the fields provided. Throws
	 * URISyntaxException if there's a problem with the user input.
	 * 
	 * @return a URI built from the account setup fields
	 */
	/* package */URI getUri() throws URISyntaxException {
		boolean sslRequired = mSslSecurityView.isChecked();
		boolean trustCertificates = true;//mTrustCertificatesView.isChecked();
		String scheme = (sslRequired) ? (trustCertificates ? "eas+ssl+trustallcerts"
				: "eas+ssl+")
				: "eas";
		String userName = mUsernameView.getText().toString().trim();
		// Remove a leading backslash, if there is one, since we now
		// automatically put one at
		// the start of the username field
		if (userName.startsWith("\\")) {
			userName = userName.substring(1);
		}
		
		String uName = uDomain+ "\\" +userName;
		mCacheLoginCredential = uName;
		String userInfo = uName + ":" + mPasswordView.getText().toString();
		String host = mServerView.getText().toString().trim();
		String path = null;

		URI uri = new URI(scheme, userInfo, host, 0, path, null, null);

		return uri;
	}
	
	private void showDialogView()
    {
    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				this);
		alertDialogBuilder.setIcon(R.drawable.em_ic_dialog_alert);
		alertDialogBuilder.setTitle(this
				.getString(R.string.account_setup_failed_dlg_title));
		// set dialog message
		alertDialogBuilder
				.setMessage(
						this.getString(R.string.accounts_sync_not_active))
				.setCancelable(false)
				.setPositiveButton("Exit",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int id) {
								// if this button is clicked, close
								// current activity
								finish();
							}
						});
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
    }
	
	/**
	 * Note, in EAS, store & sender are the same, so we always populate them
	 * together
	 */
	String uDomain = null;
	private void onNext() {
		try {
			
			if(!ContentResolver.getMasterSyncAutomatically())
			{
				showDialogView();
				return;
			}
			
			//uDomain = "truboxmdmdev.com";
			//290778 modified for Lab server
		//	uDomain = "emcslab.com";
			
			
//			URI uri = getUri();
//			mAccount.setStoreUri(this, uri.toString());
//			mAccount.setSenderUri(this, uri.toString());
			String user = mUsernameView.getText().toString();
			String password = mPasswordView.getText().toString();
			uDomain = mDomainView.getText().toString();
			//uDomain = "truboxmdmdev.com";
			
			String email;
			if(user.contains("@")) {
				email = user;	
			}
			else {
				 email = user + "@" + uDomain +((uDomain.contains(".com")? "":".com"));
			}
			
			//fixed security exception issue
			String ex_server = mServerView.getText().toString();
			if (ex_server != null && !ex_server.equals("")) 
		    	 new SharedPreferences(context).edit().putString("server", ex_server).commit();
			
			
			
			URI uri = getUri();
			mAccount.setStoreUri(this, uri.toString());
			mAccount.setSenderUri(this, uri.toString());
			
			mAccount.setEmailAddress(email);

			try {
				URI turi = new URI(mAccount.getStoreUri(this));
				uri = new URI("eas+ssl+", turi.getUserInfo(), uri.getHost(),
						uri.getPort(), null, null, null);
				mAccount.setStoreUri(this, turi.toString());
				mAccount.setSenderUri(this, turi.toString());
			} catch (URISyntaxException use) {
				/*
				 * This should not happen.
				 */
				throw new Error(use);
			}
			
			//PWD_CHANGE_IMPL, -->
			mAccount.mHostAuthRecv.mDomain = uDomain;
			mAccount.mHostAuthRecv.mPort = Integer.parseInt(mPortView.getText().toString());
			//PWD_CHANGE_IMPL, <--
			
			// TODO: Confirm correct delete policy for exchange
			mAccount.setDeletePolicy(Account.DELETE_POLICY_ON_DELETE);
			mAccount.setSyncInterval(Account.CHECK_INTERVAL_PUSH);
			mAccount.setSyncLookback(1);
			mAccount.setDefaultAccount(false);

			// Stop here if the login credentials duplicate an existing account
			// (unless they duplicate the existing account, as they of course
			// will)
			mDuplicateAccountName = EmUtility.findDuplicateAccount(this,
					mAccount.mId, uri.getHost(), mCacheLoginCredential);

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

		EmAccountSetupCheckSettings.actionValidateSettings(this, mAccount,
				true, false);
		//finish();
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
			//mTrustCertificatesView.setVisibility(isChecked ? View.VISIBLE
					//: View.GONE);
		}
	}

	//NaGa, connect button functionality -->
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.connectHome) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				connectIcon.getDrawable().setColorFilter(
						R.color.menu_option_color, Mode.SRC_ATOP);
				connectIcon.invalidate();
				break;
			case MotionEvent.ACTION_UP:
				connectIcon.getDrawable().clearColorFilter();
				connectIcon.invalidate();
				finish();
				//Intent connectIntent = new Intent(this,EmWelcome.class);
				//connectIntent.putExtra("FINISH_EMAIL", true);
				//startActivity(connectIntent);
				Intent i = new Intent(this, PersonaLauncher.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            	this.startActivity(i);
				break;

			}
		}
		return true;
	}
  	//NaGa, <--
}
