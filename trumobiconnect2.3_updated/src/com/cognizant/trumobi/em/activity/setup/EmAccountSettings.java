/**
*  FileName : EmAccountSettings
* 
*  Desc : 
* 
* 
*     KeyCode				Author                     Date                                         Desc
*     						371990                  04-Oct-2013                         Implementation of encryption for PreferenceActivity
*     PWD_CHANGE_IMPL		367712				  27-12-2013						Warn user when Account password changed on Server
*/



package com.cognizant.trumobi.em.activity.setup;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff.Mode;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.TruBoxSDK.SharedPreferences;
import com.TruBoxSDK.SharedPreferences.Editor;
import com.cognizant.trubox.contacts.db.ContactsConsts;
import com.cognizant.trumobi.PersonaLauncher;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.calendar.util.CalendarConstants;
import com.cognizant.trumobi.common.provider.Calendar;
import com.cognizant.trumobi.commonabstractclass.TruMobiBasePreferenceActivity;
import com.cognizant.trumobi.em.EmAccountBackupRestore;
import com.cognizant.trumobi.em.EmSecurityPolicy;
import com.cognizant.trumobi.em.EmUtility;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.em.activity.EmMessageList;
import com.cognizant.trumobi.em.activity.EmWelcome;
import com.cognizant.trumobi.em.mail.EmMessagingException;
import com.cognizant.trumobi.em.mail.EmSender;
import com.cognizant.trumobi.em.mail.EmStore;
import com.cognizant.trumobi.em.provider.EmEmailContent;
import com.cognizant.trumobi.em.provider.EmEmailContent.Account;
import com.cognizant.trumobi.em.provider.EmEmailContent.AccountColumns;
import com.cognizant.trumobi.em.provider.EmEmailContent.HostAuth;
import com.cognizant.trumobi.em.service.EmMailService;
import com.cognizant.trumobi.externaladapter.ExternalAdapterRegistrationClass;
import com.cognizant.trumobi.externaladapter.ExternalEmailSettingsInfo;
import com.cognizant.trumobi.persona.PersonaMainActivity;
import com.cognizant.trumobi.persona.constants.PersonaConstants;

public class EmAccountSettings extends TruMobiBasePreferenceActivity implements
		OnTouchListener {
	private static final String PREFERENCE_TOP_CATEGORY = "account_settings";
	private static final String PREFERENCE_DESCRIPTION = "account_description";
	private static final String PREFERENCE_NAME = "account_name";
	private static final String PREFERENCE_SIGNATURE = "account_signature";
	private static final String PREFERENCE_FREQUENCY = "account_check_frequency";
	private static final String PREFERENCE_DEFAULT = "account_default";
	private static final String PREFERENCE_NOTIFY = "account_notify";
	private static final String PREFERENCE_VIBRATE_WHEN = "account_settings_vibrate_when";
	private static final String PREFERENCE_RINGTONE = "account_ringtone";
	private static final String PREFERENCE_SERVER_CATERGORY = "account_servers";
	private static final String PREFERENCE_INCOMING = "incoming";
	private static final String PREFERENCE_OUTGOING = "outgoing";
	private static final String PREFERENCE_SYNC_EMAIL = "account_sync_email";
//	private static final String PREFERENCE_SYNC_CONTACTS = "account_sync_contacts";
//	private static final String PREFERENCE_SYNC_CALENDAR = "account_sync_calendar";
	private static final String PREFERENCE_OOF = "oof_settings";
	// private static final String PREFERENCE_REMOVE_ACCOUNT = "remove_account";
	// //NaGa, for Remove Account

	// These strings must match account_settings_vibrate_when_* strings in
	// strings.xml
	private static final String PREFERENCE_VALUE_VIBRATE_WHEN_ALWAYS = "always";
	private static final String PREFERENCE_VALUE_VIBRATE_WHEN_SILENT = "silent";
	private static final String PREFERENCE_VALUE_VIBRATE_WHEN_NEVER = "never";

	// NOTE: This string must match the one in res/xml/account_preferences.xml
	public static final String ACTION_ACCOUNT_MANAGER_ENTRY = "com.cognizant.trumobi.em.activity.setup.ACCOUNT_MANAGER_ENTRY";
	// NOTE: This constant should eventually be defined in
	// android.accounts.Constants, but for
	// now we define it here
	private static final String ACCOUNT_MANAGER_EXTRA_ACCOUNT = "account";
	private static final String EXTRA_ACCOUNT_ID = "account_id";
	private static final String EXTRA_LOGIN_WARN = "login_warn";	//PWD_CHANGE_IMPL
	private static final String TAG = EmAccountSettings.class.getSimpleName();

	private long mAccountId = -1;
	private Account mAccount;
	private boolean mAccountDirty;
	private TextView bartitle;

	private EditTextPreference mAccountDescription;
	private EditTextPreference mAccountName;
	private EditTextPreference mAccountSignature;
	private ListPreference mCheckFrequency;
	private ListPreference mSyncWindow;
	private CheckBoxPreference mAccountDefault;
	private CheckBoxPreference mAccountNotify;
	private ListPreference mAccountVibrateWhen;
	private RingtonePreference mAccountRingtone;
//	private CheckBoxPreference mSyncContacts;
//	private CheckBoxPreference mSyncCalendar;
	private CheckBoxPreference mSyncEmail;

	private DeleteAccountTask mDeleteAccountTask;
	
	private android.content.SharedPreferences mEmInsecurePrefs;
	private SharedPreferences mEmSecurePrefs;
	private android.content.SharedPreferences.Editor mEmInsecureEditor;
	private Editor mEmSecureEditor;

	/**
	 * Display (and edit) settings for a specific account
	 */
	public static void actionSettings(Activity fromActivity, long accountId) {
		Intent i = new Intent(fromActivity, EmAccountSettings.class);
		i.putExtra(EXTRA_ACCOUNT_ID, accountId);
		fromActivity.startActivity(i);
	}

	private ImageView backButton, titleIcon, connectIcon; // NaGa

	@SuppressLint("ResourceAsColor")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		super.onCreate(savedInstanceState);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.em_customtitlebar);

		bartitle = (TextView) findViewById(R.id.title);
		bartitle.setText(R.string.account_settings_action);

		// NaGa, Back button functionality -->
		titleIcon = (ImageView) findViewById(R.id.outlooklogo);
		backButton = (ImageView) findViewById(R.id.goback);
		backButton.setVisibility(View.VISIBLE);
		connectIcon = (ImageView) findViewById(R.id.connectHome);
		titleIcon.setOnTouchListener(this);
		backButton.setOnTouchListener(this);
		connectIcon.setOnTouchListener(this);
		// NaGa, <--
		
		//ROVA_POLICY_CHECK 31Dec2013
		boolean isDispEmail = false;
		if(PersonaMainActivity.isRovaPoliciesOn) {
			ExternalAdapterRegistrationClass mExtAdapReg = ExternalAdapterRegistrationClass.getInstance(this);
			ExternalEmailSettingsInfo extEmailSettInfo = mExtAdapReg.getExternalEmailSettingsInfo();
			isDispEmail = extEmailSettInfo.bDisplayEmailSettings;
			Log.d(TAG, "value of rova email display: "+extEmailSettInfo.bDisplayEmailSettings);
			
		}
		Log.d(TAG, "value of email display: "+isDispEmail);
		//ROVA_POLICY_CHECK 31Dec2013
		
		 //Secured shared preference
        if(mEmSecurePrefs == null) {
        	mEmSecurePrefs = new SharedPreferences(this);
        	mEmSecureEditor = mEmSecurePrefs.edit();
        	mEmInsecurePrefs = PreferenceManager.getDefaultSharedPreferences(this);
        	mEmInsecureEditor = mEmInsecurePrefs.edit();
        }

		Intent i = getIntent();
		if (ACTION_ACCOUNT_MANAGER_ENTRY.equals(i.getAction())) {
			// This case occurs if we're changing account settings from Settings
			// -> Accounts
			setAccountIdFromAccountManagerIntent();
		} else {
			// Otherwise, we're called from within the Email app and look for
			// our extra
			mAccountId = i.getLongExtra(EXTRA_ACCOUNT_ID, -1);
		}
		
		boolean loginwarn = i.getBooleanExtra(EXTRA_LOGIN_WARN, false);	//PWD_CHANGE_IMPL
		
		// If there's no accountId, we're done
		if (mAccountId == -1) {
			finish();
			return;
		}

		mAccount = Account.restoreAccountWithId(this, mAccountId);
		// Similarly, if the account has been deleted
		if (mAccount == null) {
			finish();
			return;
		}
		mAccount.mHostAuthRecv = HostAuth.restoreHostAuthWithId(this,
				mAccount.mHostAuthKeyRecv);
		mAccount.mHostAuthSend = HostAuth.restoreHostAuthWithId(this,
				mAccount.mHostAuthKeySend);
		// Or if HostAuth's have been deleted
		if (mAccount.mHostAuthRecv == null || mAccount.mHostAuthSend == null) {
			finish();
			return;
		}
		mAccountDirty = false;
		
		if(loginwarn)	showLoginWarn(mAccount);	//PWD_CHANGE_IMPL
		
		addPreferencesFromResource(R.xml.em_account_settings_preferences);

		PreferenceCategory topCategory = (PreferenceCategory) findPreference(PREFERENCE_TOP_CATEGORY);
		topCategory.setTitle(encryptString(PREFERENCE_TOP_CATEGORY,getString(R.string.account_settings_title_fmt)));

		mAccountDescription = (EditTextPreference) findPreference(PREFERENCE_DESCRIPTION);
		mAccountDescription.setEnabled(true);
		
		/*mAccountDescription.setSummary(mAccount.getDisplayName());
		mAccountDescription.setText(mAccount.getDisplayName());*/
		mAccountDescription.setSummary(encryptString(PREFERENCE_DESCRIPTION, mAccount.getDisplayName()));
		mAccountDescription.setText(encryptString(PREFERENCE_DESCRIPTION, mAccount.getDisplayName()));
		mAccountDescription
				.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						final String summary = newValue.toString();
						/*mAccountDescription.setSummary(summary);
						mAccountDescription.setText(summary);*/
						mAccountDescription.setSummary(encryptString(PREFERENCE_DESCRIPTION, summary));
						mAccountDescription.setText(encryptString(PREFERENCE_DESCRIPTION, summary));
						return false;
					}
				});

		mAccountName = (EditTextPreference) findPreference(PREFERENCE_NAME);
		String senderName = mAccount.getSenderName();
		// In rare cases, sendername will be null; Change this to empty string
		// to avoid NPE's
		if (senderName == null)
			senderName = "";
		/*mAccountName.setSummary(senderName);
		mAccountName.setText(senderName);*/
		mAccountName.setSummary(encryptString(PREFERENCE_NAME, senderName));
		mAccountName.setText(encryptString(PREFERENCE_NAME, senderName));
		mAccountName
				.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						final String summary = newValue.toString();
						/*mAccountName.setSummary(summary);
						mAccountName.setText(summary);*/
						mAccountName.setSummary(encryptString(PREFERENCE_NAME, summary));
						mAccountName.setText(encryptString(PREFERENCE_NAME, summary));
						return false;
					}
				});

		mAccountSignature = (EditTextPreference) findPreference(PREFERENCE_SIGNATURE);
		/*mAccountSignature.setSummary(mAccount.getSignature());
		mAccountSignature.setText(mAccount.getSignature());*/
		mAccountSignature.setSummary(encryptString(PREFERENCE_NAME, mAccount.getSignature()));
		mAccountSignature.setText(encryptString(PREFERENCE_NAME, mAccount.getSignature()));
		mAccountSignature
				.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						String summary = newValue.toString();
						if (summary == null || summary.length() == 0) {
							mAccountSignature
									.setSummary(R.string.account_settings_signature_hint);
						} else {
							//mAccountSignature.setSummary(summary);
							mAccountSignature.setSummary(encryptString(PREFERENCE_NAME,summary));
						}
						mAccountSignature.setText(encryptString(PREFERENCE_NAME,summary));
						return false;
					}
				});

		mCheckFrequency = (ListPreference) findPreference(PREFERENCE_FREQUENCY);

		// Before setting value, we may need to adjust the lists
		EmStore.StoreInfo info = EmStore.StoreInfo.getStoreInfo(
				mAccount.getStoreUri(this), this);
		if (info.mPushSupported) {
			mCheckFrequency
					.setEntries(R.array.account_settings_check_frequency_entries_push);
			mCheckFrequency
					.setEntryValues(R.array.account_settings_check_frequency_values_push);
		}

		/*mCheckFrequency.setValue(String.valueOf(mAccount.getSyncInterval()));
		mCheckFrequency.setSummary(mCheckFrequency.getEntry());*/
		mCheckFrequency.setValue(encryptString(PREFERENCE_FREQUENCY,String.valueOf(mAccount.getSyncInterval())));
		mCheckFrequency.setSummary(encryptString(PREFERENCE_FREQUENCY,(mCheckFrequency.getEntry()).toString()));
		mCheckFrequency
				.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						final String summary = newValue.toString();
						int index = mCheckFrequency.findIndexOfValue(summary);
						/*mCheckFrequency.setSummary(mCheckFrequency.getEntries()[index]);
						mCheckFrequency.setValue(summary);*/
						mCheckFrequency.setSummary(encryptString(PREFERENCE_FREQUENCY,
								(mCheckFrequency.getEntries()[index]).toString()));
						mCheckFrequency.setValue(encryptString(PREFERENCE_FREQUENCY,summary));
						return false;
					}
				});

		// Add check window preference
		mSyncWindow = null;
		if (info.mVisibleLimitDefault == -1) {
			mSyncWindow = new ListPreference(this);
			mSyncWindow
					.setTitle(R.string.account_setup_options_mail_window_label);
			mSyncWindow
					.setEntries(R.array.account_settings_mail_window_entries);
			mSyncWindow
					.setEntryValues(R.array.account_settings_mail_window_values);
			/*mSyncWindow.setValue(String.valueOf(mAccount.getSyncLookback()));*/
			mSyncWindow.setValue(encryptString("sync_amount", String.valueOf(mAccount.getSyncLookback())));
			mSyncWindow.setKey("sync_amount");
			mSyncWindow.setSummary(mSyncWindow.getEntry());
			//mSyncWindow.setSummary(encryptString("sync_amount", (mSyncWindow.getEntry()).toString()));
			mSyncWindow.setOrder(4);
			mSyncWindow
					.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
						public boolean onPreferenceChange(
								Preference preference, Object newValue) {
							final String summary = newValue.toString();
							int index = mSyncWindow.findIndexOfValue(summary);
							mSyncWindow.setSummary(mSyncWindow.getEntries()[index]);
							/*mSyncWindow.setValue(summary);*/
							//mSyncWindow.setSummary(encryptString("sync_amount", (mSyncWindow.getEntries()[index]).toString()));
							mSyncWindow.setValue(encryptString("sync_amount", summary));
							return false;
						}
					});
			topCategory.addPreference(mSyncWindow);
		}

		mAccountDefault = (CheckBoxPreference) findPreference(PREFERENCE_DEFAULT);
		/*mAccountDefault.setChecked(mAccount.mId == Account
				.getDefaultAccountId(this));*/		
		mAccountDefault.setChecked(encryptBoolean(PREFERENCE_DEFAULT, 
				mAccount.mId == Account.getDefaultAccountId(this)));

		mAccountNotify = (CheckBoxPreference) findPreference(PREFERENCE_NOTIFY);
		/*mAccountNotify
				.setChecked(0 != (mAccount.getFlags() & Account.FLAGS_NOTIFY_NEW_MAIL));*/
		mAccountNotify.setChecked(encryptBoolean(PREFERENCE_NOTIFY,
				0 != (mAccount.getFlags() & Account.FLAGS_NOTIFY_NEW_MAIL)));

		mAccountRingtone = (RingtonePreference) findPreference(PREFERENCE_RINGTONE);

		// XXX: The following two lines act as a workaround for the
		// RingtonePreference
		// which does not let us set/get the value programmatically
//		SharedPreferences prefs = mAccountRingtone.getPreferenceManager()
//				.getSharedPreferences();
		SharedPreferences prefs = new SharedPreferences(getApplicationContext());
		prefs.edit().putString(PREFERENCE_RINGTONE, mAccount.getRingtone())
				.commit();

		mAccountVibrateWhen = (ListPreference) findPreference(PREFERENCE_VIBRATE_WHEN);
		boolean flagsVibrate = 0 != (mAccount.getFlags() & Account.FLAGS_VIBRATE_ALWAYS);
		boolean flagsVibrateSilent = 0 != (mAccount.getFlags() & Account.FLAGS_VIBRATE_WHEN_SILENT);
		mAccountVibrateWhen
				.setValue(encryptString(PREFERENCE_VIBRATE_WHEN, flagsVibrate ? PREFERENCE_VALUE_VIBRATE_WHEN_ALWAYS
						: flagsVibrateSilent ? PREFERENCE_VALUE_VIBRATE_WHEN_SILENT
								: PREFERENCE_VALUE_VIBRATE_WHEN_NEVER));

		findPreference(PREFERENCE_INCOMING).setOnPreferenceClickListener(
				new Preference.OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						onIncomingSettings();
						return true;
					}
				});

		/*
		 * //NaGa, for Remove Account, -->
		 * findPreference(PREFERENCE_REMOVE_ACCOUNT
		 * ).setOnPreferenceClickListener( new
		 * Preference.OnPreferenceClickListener() {
		 * 
		 * @Override public boolean onPreferenceClick(Preference preference) {
		 * // TODO Auto-generated method stub NotificationManager
		 * notificationManager = (NotificationManager)
		 * getSystemService(Context.NOTIFICATION_SERVICE);
		 * notificationManager.cancel
		 * (EmMailService.NOTIFICATION_ID_NEW_MESSAGES); mDeleteAccountTask =
		 * (DeleteAccountTask) new DeleteAccountTask( mAccountId,
		 * mAccount.getStoreUri(EmAccountSettings.this)).execute();
		 * SharedPreferences sharedPreferences =
		 * getSharedPreferences(PersonaConstants.PERSONAPREFERENCESFILE,
		 * MODE_APPEND); Editor editor = sharedPreferences.edit();
		 * 
		 * editor.putBoolean("isEmailAccountConfigured",false); editor.commit();
		 * finish(); return true; } } ); //NaGa, for Remove Account, <--
		 */

		// Hide the outgoing account setup link if it's not activated
		Preference prefOutgoing = findPreference(PREFERENCE_OUTGOING);
		boolean showOutgoing = true;
		try {
			EmSender sender = EmSender.getInstance(getApplication(),
					mAccount.getSenderUri(this));
			if (sender != null) {
				Class<? extends android.app.Activity> setting = sender
						.getSettingActivityClass();
				showOutgoing = (setting != null);
			}
		} catch (EmMessagingException me) {
			// just leave showOutgoing as true - bias towards showing it, so
			// user can fix it
		}
		if (showOutgoing) {
			prefOutgoing
					.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
						public boolean onPreferenceClick(Preference preference) {
							onOutgoingSettings();
							return true;
						}
					});
		} else {
			PreferenceCategory serverCategory = (PreferenceCategory) findPreference(PREFERENCE_SERVER_CATERGORY);
			serverCategory.removePreference(prefOutgoing);
		}

		/*mSyncContacts = (CheckBoxPreference) findPreference(PREFERENCE_SYNC_CONTACTS);
		mSyncCalendar = (CheckBoxPreference) findPreference(PREFERENCE_SYNC_CALENDAR);
		if (mAccount.mHostAuthRecv.mProtocol.equals("eas")) {
			android.accounts.Account acct = new android.accounts.Account(
					mAccount.mEmailAddress, Email.EXCHANGE_ACCOUNT_MANAGER_TYPE);
			mSyncContacts.setChecked(ContentResolver.getSyncAutomatically(acct,
					ContactsConsts.AUTHORITY));
			mSyncCalendar.setChecked(ContentResolver.getSyncAutomatically(acct,
					CalendarConstants.AUTHORITY));
		} else {
			PreferenceCategory serverCategory = (PreferenceCategory) findPreference(PREFERENCE_SERVER_CATERGORY);
			serverCategory.removePreference(mSyncContacts);
			serverCategory.removePreference(mSyncCalendar);
		}*/
		
		mSyncEmail = (CheckBoxPreference) findPreference(PREFERENCE_SYNC_EMAIL);
		if (mAccount.mHostAuthRecv.mProtocol.equals("eas")) {
			android.accounts.Account acct = new android.accounts.Account(
					mAccount.mEmailAddress, Email.EXCHANGE_ACCOUNT_MANAGER_TYPE);
			mSyncEmail.setChecked(encryptBoolean(PREFERENCE_SYNC_EMAIL,
					ContentResolver.getSyncAutomatically(acct, ContactsConsts.AUTHORITY)));
		} else {
			PreferenceCategory serverCategory = (PreferenceCategory) findPreference(PREFERENCE_SERVER_CATERGORY);
			serverCategory.removePreference(mSyncEmail);
		}

		String email = mAccount.mEmailAddress; // NaGa

		// { //NaGa, Disable the Setting screen from Editing...
		@SuppressWarnings("deprecation")
		PreferenceScreen myPref = (PreferenceScreen) findPreference(getResources()
				.getString(R.string.email_pref));
		myPref.setEnabled(isDispEmail);
		findPreference(PREFERENCE_DESCRIPTION).setEnabled(false);
		findPreference(PREFERENCE_NAME).setEnabled(false);
		/*Preference pref_cal = myPref.findPreference(PREFERENCE_SYNC_CALENDAR);
		pref_cal.setEnabled(true);
		Preference pref_caontacts = myPref
				.findPreference(PREFERENCE_SYNC_CONTACTS);
		pref_caontacts.setEnabled(true);*/
		
		Preference pref_email = myPref.findPreference(PREFERENCE_SYNC_EMAIL);
		pref_email.setEnabled(isDispEmail);
		
		Preference pref = myPref.findPreference(PREFERENCE_SIGNATURE);
		pref.setEnabled(isDispEmail);
		/*
		 * Preference pref_remove =
		 * myPref.findPreference(PREFERENCE_REMOVE_ACCOUNT); //NaGa, for Remove
		 * Account pref_remove.setEnabled(true);
		 */
		Preference pref_sync = myPref.findPreference("sync_amount");
		pref_sync.setEnabled(isDispEmail);
		Preference pref_freq = myPref.findPreference(PREFERENCE_FREQUENCY);
		pref_freq.setEnabled(isDispEmail);
		Preference pref_default = myPref.findPreference(PREFERENCE_DEFAULT);
		pref_default.setEnabled(false);
		Preference pref_oof = myPref.findPreference(PREFERENCE_OOF);
		pref_oof.setEnabled(isDispEmail);
		Preference pref_notify = myPref.findPreference(PREFERENCE_NOTIFY);
		pref_notify.setEnabled(isDispEmail);
		Preference pref_vibrate = myPref
				.findPreference(PREFERENCE_VIBRATE_WHEN);
		pref_vibrate.setEnabled(isDispEmail);
		Preference pref_ringtone = myPref.findPreference(PREFERENCE_RINGTONE);
		pref_ringtone.setEnabled(isDispEmail);
		
		//On Password change in server, enabling for manual and auto setup not Cert -->
        int authType = new SharedPreferences(this).getInt("AuthType", 0);
		 boolean enableServerSettings = (authType != PersonaConstants.mAuthTypeCertificate) ;
        findPreference(PREFERENCE_SERVER_CATERGORY).setEnabled(enableServerSettings);
        findPreference(PREFERENCE_INCOMING).setEnabled(enableServerSettings);
        findPreference(PREFERENCE_SYNC_EMAIL).setEnabled(isDispEmail);
        mAccountSignature.setEnabled(isDispEmail);
		// }
		

		pref_oof.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				// TODO Auto-generated method stub
				Intent oofIntent = new Intent(getApplicationContext(),
						EmOofSettingsActivity.class);
				oofIntent.putExtra("ACCOUNT_ID", mAccountId);
				startActivity(oofIntent);
				return false;
			}
		});
		
		pref_oof.setSummary(isOofEnabled() ? R.string.account_oof_settings_summary_on
				: R.string.account_oof_settings_summary_off);

		// getListView().setBackgroundColor(R.color.connection_error_banner);
		getListView()
				.setSelector(
						getResources()
								.getDrawable(
										R.drawable.em_message_list_item_background_unread_checked)); // NaGa,
																										// Change
																										// highlation
	}
	
	private boolean isOofEnabled() {
//		SharedPreferences oofPreferences = getSharedPreferences( // oof
//				EmOofSettingsActivity.EMAIL_OOF_PREFS_FILE, MODE_APPEND);
		SharedPreferences oofPreferences = new SharedPreferences(getApplicationContext());
		return oofPreferences.getBoolean("oofsettingssuccess", false);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mDeleteAccountTask != null) {
			EmUtility.cancelTask(mDeleteAccountTask, false); // NaGa
			mDeleteAccountTask = null;
		}
	}

	// NaGa, for Remove Account, -->
	private class DeleteAccountTask extends AsyncTask<Void, Void, Void> {
		private final long mAccountId;
		private final String mAccountUri;

		public DeleteAccountTask(long accountId, String accountUri) {
			mAccountId = accountId;
			mAccountUri = accountUri;
		}

		@Override
		protected Void doInBackground(Void... params) {
			try {
				// Delete Remote store at first.
				EmStore.getInstance(mAccountUri, getApplication(), null)
						.delete();
				// Remove the Store instance from cache.
				EmStore.removeInstance(mAccountUri);
				Uri uri = ContentUris.withAppendedId(
						EmEmailContent.Account.CONTENT_URI, mAccountId);
				EmAccountSettings.this.getContentResolver().delete(uri, null,
						null);
				// Update the backup (side copy) of the accounts
				EmAccountBackupRestore.backupAccounts(EmAccountSettings.this);
				// Release or relax device administration, if relevant
				EmSecurityPolicy.getInstance(EmAccountSettings.this)
						.reducePolicies();
			} catch (Exception e) {
				// Ignore
			}
			Email.setServicesEnabled(EmAccountSettings.this);
			return null;
		}
	}

	// NaGa, for Remove Account, <--

	private void setAccountIdFromAccountManagerIntent() {
		// First, get the AccountManager account that we've been ask to handle
		android.accounts.Account acct = (android.accounts.Account) getIntent()
				.getParcelableExtra(ACCOUNT_MANAGER_EXTRA_ACCOUNT);
		// Find a HostAuth using eas and whose login is klthe name of the
		// AccountManager account
		Cursor c = getContentResolver().query(Account.CONTENT_URI,
				new String[] { AccountColumns.ID },
				AccountColumns.EMAIL_ADDRESS + "=?",
				new String[] { acct.name }, null);
		try {
			if (c.moveToFirst()) {
				mAccountId = c.getLong(0);
			}
		} finally {
			c.close();
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}

	@Override
	public void onResume() {
		super.onResume();

		// Exit immediately if the accounts list has changed (e.g. externally
		// deleted)
		if (Email.getNotifyUiAccountsChanged()) {
			EmWelcome.actionStart(this);
			finish();
			return;
		}

		if (mAccountDirty) {
			// if we are coming back from editing incoming or outgoing settings,
			// we need to refresh them here so we don't accidentally overwrite
			// the
			// old values we're still holding here
			mAccount.mHostAuthRecv = HostAuth.restoreHostAuthWithId(this,
					mAccount.mHostAuthKeyRecv);
			mAccount.mHostAuthSend = HostAuth.restoreHostAuthWithId(this,
					mAccount.mHostAuthKeySend);
			// Because "delete policy" UI is on edit incoming settings, we have
			// to refresh that as well.
			Account refreshedAccount = Account.restoreAccountWithId(this,
					mAccount.mId);
			if (refreshedAccount == null || mAccount.mHostAuthRecv == null
					|| mAccount.mHostAuthSend == null) {
				finish();
				return;
			}
			mAccount.setDeletePolicy(refreshedAccount.getDeletePolicy());
			mAccountDirty = false;
		}
		
		PreferenceScreen myPref = (PreferenceScreen) findPreference(getResources()
				.getString(R.string.email_pref));
		Preference pref_oof = myPref.findPreference(PREFERENCE_OOF);
		pref_oof.setSummary(encryptString(PREFERENCE_OOF, getString(isOofEnabled() ? R.string.account_oof_settings_summary_on
				: R.string.account_oof_settings_summary_off)));
		
	}

	private void saveSettings() {
		int newFlags = mAccount.getFlags()
				& ~(Account.FLAGS_NOTIFY_NEW_MAIL
						| Account.FLAGS_VIBRATE_ALWAYS | Account.FLAGS_VIBRATE_WHEN_SILENT);

		mAccount.setDefaultAccount(mAccountDefault.isChecked());
		mAccount.setDisplayName(mAccountDescription.getText());
		mAccount.setSenderName(mAccountName.getText());
		mAccount.setSignature(mAccountSignature.getText());
		newFlags |= mAccountNotify.isChecked() ? Account.FLAGS_NOTIFY_NEW_MAIL
				: 0;
		mAccount.setSyncInterval(Integer.parseInt(mCheckFrequency.getValue()));
		if (mSyncWindow != null) {
			mAccount.setSyncLookback(Integer.parseInt(mSyncWindow.getValue()));
		}
		if (mAccountVibrateWhen.getValue().equals(
				PREFERENCE_VALUE_VIBRATE_WHEN_ALWAYS)) {
			newFlags |= Account.FLAGS_VIBRATE_ALWAYS;
		} else if (mAccountVibrateWhen.getValue().equals(
				PREFERENCE_VALUE_VIBRATE_WHEN_SILENT)) {
			newFlags |= Account.FLAGS_VIBRATE_WHEN_SILENT;
		}
//		SharedPreferences prefs = mAccountRingtone.getPreferenceManager()
//				.getSharedPreferences();
		SharedPreferences prefs = new SharedPreferences(getApplicationContext());
		mAccount.setRingtone(prefs.getString(PREFERENCE_RINGTONE, null));
		mAccount.setFlags(newFlags);

		
		if (mAccount.mHostAuthRecv.mProtocol.equals("eas")) {
			
			android.accounts.Account acct = new android.accounts.Account(
					mAccount.mEmailAddress, Email.EXCHANGE_ACCOUNT_MANAGER_TYPE);
			/*ContentResolver.setSyncAutomatically(acct,
					ContactsConsts.AUTHORITY, mSyncContacts.isChecked());
			ContentResolver.setSyncAutomatically(acct,
					CalendarConstants.AUTHORITY,

					mSyncCalendar.isChecked());*/
			ContentResolver.setSyncAutomatically(acct,
					ContactsConsts.AUTHORITY, mSyncEmail.isChecked());

		}
		EmAccountSettingsUtils.commitSettings(this, mAccount);
		Email.setServicesEnabled(this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			saveSettings();
		}
		return super.onKeyDown(keyCode, event);
	}

	private void onIncomingSettings() {
		try {
			EmStore store = EmStore.getInstance(mAccount.getStoreUri(this),
					getApplication(), null);
			if (store != null) {
				Class<? extends android.app.Activity> setting = store
						.getSettingActivityClass();
				if (setting != null) {
					java.lang.reflect.Method m = setting.getMethod(
							"actionEditIncomingSettings",
							android.app.Activity.class, Account.class);
					m.invoke(null, this, mAccount);
					mAccountDirty = true;
				}
			}
		} catch (Exception e) {
			Log.d(Email.LOG_TAG,
					"Error while trying to invoke store settings.", e);
		}
	}

	private void onOutgoingSettings() {
		try {
			EmSender sender = EmSender.getInstance(getApplication(),
					mAccount.getSenderUri(this));
			if (sender != null) {
				Class<? extends android.app.Activity> setting = sender
						.getSettingActivityClass();
				if (setting != null) {
					java.lang.reflect.Method m = setting.getMethod(
							"actionEditOutgoingSettings",
							android.app.Activity.class, Account.class);
					m.invoke(null, this, mAccount);
					mAccountDirty = true;
				}
			}
		} catch (Exception e) {
			Log.d(Email.LOG_TAG,
					"Error while trying to invoke sender settings.", e);
		}
	}

	// NaGa, Back button functionality -->
	@Override
	public boolean onTouch(View v, MotionEvent event) { // NaGa
		// TODO Auto-generated method stub
		if (v.getId() == R.id.outlooklogo || v.getId() == R.id.goback)
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:// Log.e("onTouchonTouch",
											// "ACTION_DOWNACTION_DOWN");
				backButton.getDrawable().setColorFilter(
						R.color.menu_option_color, Mode.SRC_ATOP);
				backButton.invalidate();
				titleIcon.getDrawable().setColorFilter(
						R.color.menu_option_color, Mode.SRC_ATOP);
				titleIcon.invalidate();
				break;
			case MotionEvent.ACTION_UP:// Log.e("onTouchonTouch",
										// "defaultdefaultdefaultdefault");
				backButton.getDrawable().clearColorFilter();
				backButton.invalidate();
				titleIcon.getDrawable().clearColorFilter();
				titleIcon.invalidate();
				saveSettings();
				onBackPressed();
				// finish();
				break;
			}
		else if (v.getId() == R.id.connectHome) {
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
				// Intent connectIntent = new Intent(this,EmWelcome.class);
				// connectIntent.putExtra("FINISH_EMAIL", true);
				// startActivity(connectIntent);
				saveSettings();
				Intent i = new Intent(this, PersonaLauncher.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				this.startActivity(i);
				break;
			}
		}

		return true;
	}
	// NaGa, <--
	
	/**
	 * Encrypt the preference string value
	 */
	private String encryptString(String key, String value) {
		mEmInsecureEditor.putString(key, value).commit();
		mEmSecureEditor.putString(key, mEmInsecurePrefs.getString(key, null)).commit();
		mEmInsecureEditor.remove(key);
        mEmSecurePrefs.getString(key, null);
		return mEmSecurePrefs.getString(key, null);
		
	}
	
	/**
	 * Encrypt the preference boolean value
	 */	
	private boolean encryptBoolean(String key, boolean value) {
		mEmInsecureEditor.putBoolean(key, value).commit();
		mEmSecureEditor.putBoolean(key, mEmInsecurePrefs.getBoolean(key, false)).commit();
		mEmInsecureEditor.remove(key);		
		return mEmSecurePrefs.getBoolean(key, true);
	}
	
	//PWD_CHANGE_IMPL, -->
	private void showLoginWarn(final Account nAccount) {
		// message_ID = (msg == UNINSTALL_INSTALL)?
		// R.string.uninstall_install_on_NoPermission:message_ID;
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setIcon(R.drawable.em_ic_dialog_alert);
		alertDialogBuilder
				.setTitle(getString(R.string.account_settings_login_dialog_title));
		// set dialog message
		alertDialogBuilder
				.setMessage(
						this.getString(
								R.string.account_settings_login_dialog_content_fmt,
								nAccount.mDisplayName))
				.setCancelable(false)
				.setPositiveButton(R.string.okay_action,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// if this button is clicked, close
								// current activity
								dialog.dismiss();
								NotificationManager notificationManager = (NotificationManager)
						                getSystemService(Context.NOTIFICATION_SERVICE);
						        notificationManager.cancel(EmMailService.NOTIFICATION_ID_LOGIN_FAILED);
								onIncomingSettings();
							}
						})
				.setNegativeButton(R.string.cancel_action,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								EmMailService.actionNotifyLoginFailed(
										getApplicationContext(), nAccount.mId);
								dialog.dismiss();
								finish();
							}
						});
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}
	//PWD_CHANGE_IMPL, <--
	
}
