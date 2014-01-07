

package com.cognizant.trumobi.em.activity.setup;

import java.io.IOException;

import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.TruBoxSDK.SharedPreferences;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseActivity;
import com.cognizant.trumobi.em.EmExchangeUtils;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.em.mail.EmStore;
import com.cognizant.trumobi.em.mail.store.EmExchangeStore;
import com.cognizant.trumobi.em.provider.EmEmailContent;
import com.cognizant.trumobi.em.provider.EmEmailContent.Account;
import com.cognizant.trumobi.persona.constants.PersonaConstants;

public class EmAccountSetupOptions extends TruMobiBaseActivity implements OnClickListener {

	private static final String EXTRA_ACCOUNT = "account";
	private static final String EXTRA_MAKE_DEFAULT = "makeDefault";
	private static final String EXTRA_EAS_FLOW = "easFlow";

	private Spinner mCheckFrequencyView;
	private Spinner mSyncWindowView;
	private CheckBox mDefaultView;
	private CheckBox mNotifyView;
	private CheckBox mSyncContactsView;
	private CheckBox mSyncCalendarView;
	private EmEmailContent.Account mAccount;
	private boolean mEasFlowMode;
	private Handler mHandler = new Handler();
	private boolean mDonePressed = false;
	private ImageView connectIcon;	//NaGa

	/** Default sync window for new EAS accounts */
	private static final int SYNC_WINDOW_EAS_DEFAULT = com.cognizant.trumobi.em.EmAccount.SYNC_WINDOW_3_DAYS;

	public static void actionOptions(Activity fromActivity,
			EmEmailContent.Account account, boolean makeDefault,
			boolean easFlowMode) {
		Intent i = new Intent(fromActivity, EmAccountSetupOptions.class);
		i.putExtra(EXTRA_ACCOUNT, account);
		i.putExtra(EXTRA_MAKE_DEFAULT, makeDefault);
		i.putExtra(EXTRA_EAS_FLOW, easFlowMode);
		fromActivity.startActivity(i);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); // NaGa, adding title
															// Bar
		setContentView(R.layout.em_account_setup_options);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.em_customtitlebar);

		mCheckFrequencyView = (Spinner) findViewById(R.id.account_check_frequency);
		mSyncWindowView = (Spinner) findViewById(R.id.account_sync_window);
		mDefaultView = (CheckBox) findViewById(R.id.account_default);
		mNotifyView = (CheckBox) findViewById(R.id.account_notify);
		mSyncContactsView = (CheckBox) findViewById(R.id.account_sync_contacts);
		mSyncCalendarView = (CheckBox) findViewById(R.id.account_sync_calendar);

		findViewById(R.id.next).setOnClickListener(this);
		((ImageView) findViewById(R.id.outlooklogo)).setImageResource(R.drawable.pr_connect_header_icon);
		((TextView) findViewById(R.id.title)).setText(R.string.account_settings_action);
		//NaGa, Connect button functionality -->
        connectIcon = (ImageView) findViewById(R.id.connectHome);
        connectIcon.setVisibility(View.GONE);

		mAccount = (EmEmailContent.Account) getIntent().getParcelableExtra(
				EXTRA_ACCOUNT);
		boolean makeDefault = getIntent().getBooleanExtra(EXTRA_MAKE_DEFAULT,
				false);

		// Generate spinner entries using XML arrays used by the preferences
		int frequencyValuesId;
		int frequencyEntriesId;
		EmStore.StoreInfo info = EmStore.StoreInfo.getStoreInfo(
				mAccount.getStoreUri(this), this);
		if (info.mPushSupported) {
			frequencyValuesId = R.array.account_settings_check_frequency_values_push;
			frequencyEntriesId = R.array.account_settings_check_frequency_entries_push;
		} else {
			frequencyValuesId = R.array.account_settings_check_frequency_values;
			frequencyEntriesId = R.array.account_settings_check_frequency_entries;
		}
		CharSequence[] frequencyValues = getResources().getTextArray(
				frequencyValuesId);
		CharSequence[] frequencyEntries = getResources().getTextArray(
				frequencyEntriesId);

		// Now create the array used by the Spinner
		EmSpinnerOption[] checkFrequencies = new EmSpinnerOption[frequencyEntries.length];
		for (int i = 0; i < frequencyEntries.length; i++) {
			checkFrequencies[i] = new EmSpinnerOption(
					Integer.valueOf(frequencyValues[i].toString()),
					frequencyEntries[i].toString());
		}

		ArrayAdapter<EmSpinnerOption> checkFrequenciesAdapter = new ArrayAdapter<EmSpinnerOption>(
				this, android.R.layout.simple_spinner_item, checkFrequencies);
		checkFrequenciesAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mCheckFrequencyView.setAdapter(checkFrequenciesAdapter);

		if (info.mVisibleLimitDefault == -1) {
			enableEASSyncWindowSpinner();
		}

		// Note: It is OK to use mAccount.mIsDefault here *only* because the
		// account
		// has not been written to the DB yet. Ordinarily, call
		// Account.getDefaultAccountId().
		if (mAccount.mIsDefault || makeDefault) {
			mDefaultView.setChecked(true);
		}
		setonEAS(); // NaGa, eas uncheck default account
		mNotifyView
				.setChecked((mAccount.getFlags() & EmEmailContent.Account.FLAGS_NOTIFY_NEW_MAIL) != 0);
		EmSpinnerOption.setSpinnerOptionValue(mCheckFrequencyView,
				mAccount.getSyncInterval());

		// Setup any additional items to support EAS & EAS flow mode
		mEasFlowMode = getIntent().getBooleanExtra(EXTRA_EAS_FLOW, false);
		if ("eas".equals(info.mScheme)) {

			// "also sync contacts" == "true"
			mSyncContactsView.setVisibility(View.VISIBLE);
			mSyncContactsView.setChecked(true);
			mSyncCalendarView.setVisibility(View.VISIBLE);
			mSyncCalendarView.setChecked(true);
		}

	}

	private void setonEAS() {
		mDefaultView.setChecked(false);
	}

	AccountManagerCallback<Bundle> mAccountManagerCallback = new AccountManagerCallback<Bundle>() {
		public void run(AccountManagerFuture<Bundle> future) {
			try {
				Bundle bundle = future.getResult();
				bundle.keySet();
				mHandler.post(new Runnable() {
					public void run() {
						finishOnDone();
					}
				});
				return;
			} catch (OperationCanceledException e) {
				Log.d(Email.LOG_TAG, "addAccount was canceled");
			} catch (IOException e) {
				Log.d(Email.LOG_TAG, "addAccount failed: " + e);
			} catch (AuthenticatorException e) {
				Log.d(Email.LOG_TAG, "addAccount failed: " + e);
			}
			showErrorDialog(R.string.account_setup_failed_dlg_auth_message,
					R.string.system_account_create_failed);
		}
	};

	private void showErrorDialog(final int msgResId, final Object... args) {
		mHandler.post(new Runnable() {
			public void run() {
				new AlertDialog.Builder(EmAccountSetupOptions.this)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setTitle(
								getString(R.string.account_setup_failed_dlg_title))
						.setMessage(getString(msgResId, args))
						.setCancelable(true)
						.setPositiveButton(
								getString(R.string.account_setup_failed_dlg_edit_details_action),
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										finish();
									}
								}).show();
			}
		});
	}

	private void finishOnDone() {
		
		new SharedPreferences(this).edit().putBoolean("isEmailAccountConfigured",true).commit();
		
		// Clear the incomplete flag now
		mAccount.mFlags &= ~Account.FLAGS_INCOMPLETE;
		EmAccountSettingsUtils.commitSettings(this, mAccount);
		Email.setServicesEnabled(this);
		EmAccountSetupNames.actionSetNames(this, mAccount.mId, mEasFlowMode);
		// Start up SyncManager (if it isn't already running)
		EmExchangeUtils.startExchangeService(getApplicationContext());
		finish();
	}

	AccountManagerFuture<Bundle> managerAccount;

	private void onDone() {
		try
		{
		mAccount.setDisplayName(mAccount.getEmailAddress());
		int newFlags = mAccount.getFlags()
				& ~(EmEmailContent.Account.FLAGS_NOTIFY_NEW_MAIL);
		if (mNotifyView.isChecked()) {
			newFlags |= EmEmailContent.Account.FLAGS_NOTIFY_NEW_MAIL;
		}
		mAccount.setFlags(newFlags);
		mAccount.setSyncInterval((Integer) ((EmSpinnerOption) mCheckFrequencyView
				.getSelectedItem()).value);
		if (mSyncWindowView.getVisibility() == View.VISIBLE) {
			int window = (Integer) ((EmSpinnerOption) mSyncWindowView
					.getSelectedItem()).value;
			mAccount.setSyncLookback(window);
		}
		//mAccount.setDefaultAccount(mDefaultView.isChecked());
		mAccount.setDefaultAccount(true);

		// Call EAS to store account information for use by AccountManager
		if (!mAccount.isSaved() && mAccount.mHostAuthRecv != null
				&& mAccount.mHostAuthRecv.mProtocol.equals("eas")) {
			Log.e("onDoneonDoneonDone ", "1111111111111111111111111111");
			boolean alsoSyncContacts = mSyncContactsView.isChecked();
			boolean alsoSyncCalendar = mSyncCalendarView.isChecked();
			// Set the incomplete flag here to avoid reconciliation issues in
			// SyncManager (EAS)
			mAccount.mFlags |= Account.FLAGS_INCOMPLETE;
			EmAccountSettingsUtils.commitSettings(this, mAccount);
			managerAccount = EmExchangeStore.addSystemAccount(getApplication(),
					mAccount, alsoSyncContacts, alsoSyncCalendar,
					mAccountManagerCallback);
			/*
			 * try { String uName =
			 * managerAccount.getResult().getString(EasAuthenticatorService
			 * .OPTIONS_USERNAME); String uPwd =
			 * managerAccount.getResult().getString
			 * (EasAuthenticatorService.OPTIONS_USERNAME);
			 * Log.e("uNameuNameuName ", "uNameuName "+uName);
			 * Log.e("uNameuNameuName ", "uNameuName "+uName); } catch
			 * (OperationCanceledException e) { // TODO Auto-generated catch
			 * block e.printStackTrace(); } catch (AuthenticatorException e) {
			 * // TODO Auto-generated catch block e.printStackTrace(); } catch
			 * (IOException e) { // TODO Auto-generated catch block
			 * e.printStackTrace(); }
			 */
		} else {
			Log.e("onDoneonDoneonDone ", "2222222222222222222222222222222");
			finishOnDone();
		}
		}catch(Exception e){e.printStackTrace();}
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.next:
			// Don't allow this more than once (Exchange accounts call an async
			// method
			// before finish()'ing the Activity, which allows this code to
			// potentially be
			// executed multiple times
			if (!mDonePressed) {
				onDone();
				mDonePressed = true;
			}
			break;
		}
	}

	/**
	 * Enable an additional spinner using the arrays normally handled by
	 * preferences
	 */
	private void enableEASSyncWindowSpinner() {
		// Show everything
		findViewById(R.id.account_sync_window_label)
				.setVisibility(View.VISIBLE);
		mSyncWindowView.setVisibility(View.VISIBLE);

		// Generate spinner entries using XML arrays used by the preferences
		CharSequence[] windowValues = getResources().getTextArray(
				R.array.account_settings_mail_window_values);
		CharSequence[] windowEntries = getResources().getTextArray(
				R.array.account_settings_mail_window_entries);

		// Now create the array used by the Spinner
		EmSpinnerOption[] windowOptions = new EmSpinnerOption[windowEntries.length];
		int defaultIndex = -1;
		for (int i = 0; i < windowEntries.length; i++) {
			final int value = Integer.valueOf(windowValues[i].toString());
			windowOptions[i] = new EmSpinnerOption(value,
					windowEntries[i].toString());
			if (value == SYNC_WINDOW_EAS_DEFAULT) {
				defaultIndex = i;
			}
		}

		ArrayAdapter<EmSpinnerOption> windowOptionsAdapter = new ArrayAdapter<EmSpinnerOption>(
				this, android.R.layout.simple_spinner_item, windowOptions);
		windowOptionsAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSyncWindowView.setAdapter(windowOptionsAdapter);

		EmSpinnerOption.setSpinnerOptionValue(mSyncWindowView,
				mAccount.getSyncLookback());
		if (defaultIndex >= 0) {
			mSyncWindowView.setSelection(defaultIndex);
		}
	}
}
