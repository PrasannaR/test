

package com.cognizant.trumobi.em.activity.setup;

import com.cognizant.trumobi.commonabstractclass.TruMobBaseEditText;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseActivity;
import com.cognizant.trumobi.em.EmAccountBackupRestore;
import com.cognizant.trumobi.PersonaLauncher;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.em.EmUtility;
import com.cognizant.trumobi.em.activity.EmWelcome;
import com.cognizant.trumobi.em.activity.setup.EmAccountSetupBasics;
import com.cognizant.trumobi.em.provider.EmEmailContent;
import com.cognizant.trumobi.em.provider.EmEmailContent.Account;
import com.cognizant.trumobi.em.provider.EmEmailContent.AccountColumns;
import com.cognizant.trumobi.em.provider.EmEmailContent.HostAuth;
import com.cognizant.trumobi.exchange.EmEasSyncService;
import com.cognizant.trumobi.persona.PersonaLocalAuthentication;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.TextKeyListener;
import android.text.method.TextKeyListener.Capitalize;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 
 * KEYCODE			    		AUTHOR		PURPOSE
 * USER_SETTINGS_INFO_CHG		367712		To get EmailAddress from Server
 */

public class EmAccountSetupNames extends TruMobiBaseActivity implements OnClickListener {
	private static final String EXTRA_ACCOUNT_ID = "accountId";
	private static final String EXTRA_EAS_FLOW = "easFlow";
	private static final int REQUEST_SECURITY = 0;

	private TruMobBaseEditText mDescription;
	private TruMobBaseEditText mName;
	private Account mAccount;
	private Button mDoneButton;
	private boolean mEasAccount = false;
	private ImageView connectIcon;	//NaGa

	private CheckAccountStateTask mCheckAccountStateTask;

	private static final int ACCOUNT_INFO_COLUMN_FLAGS = 0;
	private static final int ACCOUNT_INFO_COLUMN_SECURITY_FLAGS = 1;
	private static final String[] ACCOUNT_INFO_PROJECTION = new String[] {
			AccountColumns.FLAGS, AccountColumns.SECURITY_FLAGS };

	public static void actionSetNames(Activity fromActivity, long accountId,
			boolean easFlowMode) {
		Intent i = new Intent(fromActivity, EmAccountSetupNames.class);
		i.putExtra(EXTRA_ACCOUNT_ID, accountId);
		i.putExtra(EXTRA_EAS_FLOW, easFlowMode);
		fromActivity.startActivity(i);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // NaGa, adding title
															// Bar
		setContentView(R.layout.em_account_setup_names);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.em_customtitlebar);

		mDescription = (TruMobBaseEditText) findViewById(R.id.account_description);
		mName = (TruMobBaseEditText) findViewById(R.id.account_name);
		mDoneButton = (Button) findViewById(R.id.done);
		mDoneButton.setOnClickListener(this);
		((TextView) findViewById(R.id.title)).setText(R.string.em_account_setup_header);
		//NaGa, Connect button functionality -->
        connectIcon = (ImageView) findViewById(R.id.connectHome);
        connectIcon.setVisibility(View.GONE);
        ((ImageView) findViewById(R.id.outlooklogo)).setImageResource(R.drawable.pr_connect_header_icon);
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
		mName.addTextChangedListener(validationTextWatcher);

		mName.setKeyListener(TextKeyListener.getInstance(false,
				Capitalize.WORDS));

		long accountId = getIntent().getLongExtra(EXTRA_ACCOUNT_ID, -1);
		mAccount = EmEmailContent.Account.restoreAccountWithId(this, accountId);
		// Shouldn't happen, but it could
		if (mAccount == null) {
			onBackPressed();
			return;
		}
		// Get the hostAuth for receiving
		HostAuth hostAuth = HostAuth.restoreHostAuthWithId(this,
				mAccount.mHostAuthKeyRecv);
		if (hostAuth == null) {
			onBackPressed();
		}

		/*
		 * Since this field is considered optional, we don't set this here. If
		 * the user fills in a value we'll reset the current value, otherwise we
		 * just leave the saved value alone.
		 */
		// mDescription.setText(mAccount.getDescription());
		mEasAccount = hostAuth.mProtocol.equals("eas");
		// ==============================================================================//
		// NaGa, eas changes -->
		
		//USER_SETTINGS_INFO_CHG, -->
		String accountEmail = EmEasSyncService.tempValue;//mAccount.mEmailAddress;
		if(accountEmail != null)
			mAccount.setEmailAddress(accountEmail);
		EmEasSyncService.tempValue = null;
		//USER_SETTINGS_INFO_CHG, <--
		
		String[] emailParts = accountEmail.split("@");
		String uName = emailParts[0].trim();

		{
			mDescription.setText(accountEmail);
			mDescription.setEnabled(false);
			mDescription.setKeyListener(null);

			// mOptionallaDes.setText(R.string.account_setup_names_account_name_label_custom);
		}
		// ==============================================================================//
		// Remember whether we're an EAS account, since it doesn't require the
		// user name field

		if (mEasAccount) {
			mName.setText(uName);
			mName.setVisibility(View.GONE);
			findViewById(R.id.account_name_label).setVisibility(View.GONE);
		}
		// NaGa, <--
		if (mAccount != null && mAccount.getSenderName() != null) {
			// mName.setText(mAccount.getSenderName()); //NaGa
		}

		// Make sure the "done" button is in the proper state
		validateFields();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mCheckAccountStateTask != null
				&& mCheckAccountStateTask.getStatus() != CheckAccountStateTask.Status.FINISHED) {
			mCheckAccountStateTask.cancel(true);
			mCheckAccountStateTask = null;
		}
	}

	/**
	 * TODO: Validator should also trim the name string before checking it.
	 */
	private void validateFields() {
		if (!mEasAccount) {
			mDoneButton.setEnabled(EmUtility.requiredFieldValid(mName));
		}
		EmUtility.setCompoundDrawablesAlpha(mDoneButton,
				mDoneButton.isEnabled() ? 255 : 128);
	}

	@Override
	public void onBackPressed() {
		boolean easFlowMode = getIntent()
				.getBooleanExtra(EXTRA_EAS_FLOW, false);
		if (easFlowMode) {
			EmAccountSetupBasics.actionAccountCreateFinishedEas(this);
		} else {
			if (mAccount != null) {
				EmAccountSetupBasics.actionAccountCreateFinished(this,
						mAccount.mId);
			} else {
				// Safety check here; If mAccount is null (due to external
				// issues or bugs)
				// just rewind back to Welcome, which can handle any
				// configuration of accounts
				EmWelcome.actionStart(this);
			}
		}
		finish();
	}

	/**
	 * After having a chance to input the display names, we normally jump
	 * directly to the inbox for the new account. However if we're in EAS flow
	 * mode (externally-launched account creation) we simply "pop" here which
	 * should return us to the Accounts activities.
	 * 
	 * TODO: Validator should also trim the description string before checking
	 * it.
	 */
	private void onNext() {

		if (EmUtility.requiredFieldValid(mDescription)) {
			mAccount.setDisplayName(mDescription.getText().toString());
		}
		String name = mName.getText().toString();
		mAccount.setSenderName(name);
		ContentValues cv = new ContentValues();
		cv.put(AccountColumns.DISPLAY_NAME, mAccount.getDisplayName());
		cv.put(AccountColumns.SENDER_NAME, name);
		mAccount.update(this, cv);
		// Update the backup (side copy) of the accounts
		EmAccountBackupRestore.backupAccounts(this);

		// Before proceeding, launch an AsyncTask to test the account for any
		// syncing problems,
		// and if there's a problem, bring up the UI to update the security
		// level.
		mCheckAccountStateTask = new CheckAccountStateTask(mAccount.mId);
		mCheckAccountStateTask.execute();
		//EmAccountSecurity.policyRequired = true;
		//Intent i = new Intent(this, EmWelcome.class); // NaGa
		//i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		//this.startActivity(i);
		finish();
		Intent localAuthentication = new Intent(
				EmAccountSetupNames.this, PersonaLauncher.class);
		localAuthentication.putExtra("callDeviceProvisioning",true);
		startActivity(localAuthentication);
		

	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.done:
			onNext();
			break;
		}
	}

	/**
	 * This async task is launched just before exiting. It's a last chance test,
	 * before leaving this activity, for the account being in a "hold" state,
	 * and gives the user a chance to update security, enter a device PIN, etc.
	 * for a more seamless account setup experience.
	 * 
	 * TODO: If there was *any* indication that security might be required, we
	 * could at least force the DeviceAdmin activation step, without waiting for
	 * the initial sync/handshake to fail. TODO: If the user doesn't update the
	 * security, don't go to the MessageList.
	 */
	private class CheckAccountStateTask extends AsyncTask<Void, Void, Boolean> {

		private long mAccountId;

		public CheckAccountStateTask(long accountId) {
			mAccountId = accountId;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			Cursor c = EmAccountSetupNames.this.getContentResolver()
					.query(ContentUris.withAppendedId(Account.CONTENT_URI,
							mAccountId), ACCOUNT_INFO_PROJECTION, null, null,
							null);
			try {
				if (c.moveToFirst()) {
					int flags = c.getInt(ACCOUNT_INFO_COLUMN_FLAGS);
					int securityFlags = c
							.getInt(ACCOUNT_INFO_COLUMN_SECURITY_FLAGS);
					if ((flags & Account.FLAGS_SECURITY_HOLD) != 0) {
						return Boolean.TRUE;
					}
				}
			} finally {
				c.close();
			}

			return Boolean.FALSE;
		}

		@Override
		protected void onPostExecute(Boolean isSecurityHold) {
			if (!isCancelled()) {
				if (isSecurityHold) {
					//EmAccountSecurity.policyRequired = true;
					Intent i = EmAccountSecurity.actionUpdateSecurityIntent(
							EmAccountSetupNames.this, mAccountId);
					EmAccountSetupNames.this.startActivityForResult(i,
							REQUEST_SECURITY);
				} else {
					//onBackPressed();
				}
			}
		}
	}

	/**
	 * Handle the eventual result from the security update activity
	 * 
	 * TODO: If the user doesn't update the security, don't go to the
	 * MessageList.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_SECURITY:
			onBackPressed();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}
