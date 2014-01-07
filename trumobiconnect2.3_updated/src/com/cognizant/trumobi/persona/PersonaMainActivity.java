package com.cognizant.trumobi.persona;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import com.TruBoxSDK.SharedPreferences;
import com.TruBoxSDK.TruBoxContentEncryption;

import com.cognizant.trumobi.PersonaLauncher;

import com.TruBoxSDK.TruBoxDatabase;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobiTimerClass;
import com.cognizant.trumobi.log.PersonaLog;
import com.cognizant.trumobi.persona.PersonaSecurityUpdateDialogFragment.DialogCancelListener;
import com.cognizant.trumobi.persona.constants.PersonaConstants;
import com.cognizant.trumobi.persona.net.PersonaGetJsonInterface;
import com.cognizant.trumobi.persona.utils.PersonaAuthenticationTask;
import com.cognizant.trumobi.persona.utils.PersonaCommonfunctions;

/**
 * 
 * @author rudhra
 *CertificateCheck		290778		28/11/13
 */
/**
 * 
 * KEYCODE			    AUTHOR		PURPOSE
 * PIN_SCREEN_ISSUE		367712		PIN Screen Issue on Email Notification
 * CERTIFICATE			290778		Certificate based check implemeted
 *
 */
public class PersonaMainActivity extends FragmentActivity implements
		PersonaGetJsonInterface, DialogCancelListener {

	static Context mContext, mAppContext;
	PersonaAuthenticationTask mAuthenticationTask;
	SharedPreferences sharedPreferences;

	PersonaCommonfunctions mCommonFunctions;
	PersonaMainActivity personaMainActivity;
	HashMap<String, String> myDeviceDetails;
	static final String LOG_TAG = "PersonaLauncher";
	static final boolean LOGD = true;
	String message, title;
	String username, password;
	ProgressDialog updatingPolicyProgress;
	boolean isShowAuthenticatePinUi, finishActivity;
	TruBoxContentEncryption nBoxContentEncryption;
	int mLocalAuthType, mNumOfTrialsAllowed;
	long mPwdSetDate;
	PersonaMainActivity mActivity;
	public static int INITIATE_FETCH_SETTINGS_CALLED_COUNT = 1;
	private String mSA, mS, mPS, mAT;
	int mAction, mEvent,mAuthType,mNumberOfTrialsAllowed,mExpiryIndays;
	boolean emailNotification;
	Bundle bndleToLocalAuthention;

	// PIM_TIMER_CHG
	boolean bFromTimer = false;
	int fromAutoLockTimer;
	// 371990 Geetha - added to handle to email security notification
	public static boolean securityNotification = false;

	public static  boolean isCertificateBasedAuth=false; 
	public static final boolean isRovaPoliciesOn = true;
	public static final boolean pullRovaSettingsOn = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {

			finish();
			return;
		}
		personaMainActivity = this;
		mContext = personaMainActivity;
		mAppContext = getApplicationContext();
		mCommonFunctions = new PersonaCommonfunctions(mContext);
		mActivity = this;
	}

	private void showMessage() {

		Intent intent = new Intent(mContext, PersonaSecurityProfileUpdate.class);
		intent.putExtra("securityevent", mEvent);
		intent.putExtra("securityaction", mAction);
		intent.putExtra("wipedAlready", true);
		startActivity(intent);

	}

	private boolean isBlockedOrWiped() {

		boolean blockedOrWiped = false;
		Cursor c = getContentResolver().query(
				PersonaDatabaseProvider.ROUTINE_CHECK_CONTENT_URI, null, null,
				null, null);
		try {
			if (c.getCount() > 0) {
				c.moveToFirst();

				mS = c.getString(c.getColumnIndex(PersonaExternalDBHelper.COL1));
				mSA = c.getString(c
						.getColumnIndex(PersonaExternalDBHelper.COL2));
				mPS = c.getString(c
						.getColumnIndex(PersonaExternalDBHelper.COL5));
				mAT = c.getString(c.getColumnIndex(PersonaExternalDBHelper.COL3));
				mNumberOfTrialsAllowed = c.getInt(c.getColumnIndex(PersonaExternalDBHelper.COL4));
				mAuthType = mCommonFunctions.getAuthenticationType(mAT);
				mExpiryIndays = c.getInt(c.getColumnIndex(PersonaExternalDBHelper.COL6));
				PersonaLog.d("Personamainactivity","-------- values retrived -----" + "mS " + mS + "mSA " + mSA );
				PersonaLog.d("PersonaMainActivity", "mPS " + mPS + "mAT " + mAT + "mNumberOfTrialsAllowed " + mNumberOfTrialsAllowed);
				PersonaLog.d("Personamainactivity","-------- values retrived -----" + "mExpiryIndays " + mExpiryIndays);
				mEvent = Integer.parseInt(Character.toString(mS.charAt(1)));
				mAction = Integer.parseInt(Character.toString(mSA.charAt(2)));

				if (mEvent == 2 || mEvent == 3) {
					blockedOrWiped = true;
				}
			}

		} catch (Exception e) {

		} finally {
			if (c != null)
				c.close();
		}
		return blockedOrWiped;
	}

	public void init() {

		setPersonaKey();

		isShowAuthenticatePinUi = isPinSaved();

		if (isShowAuthenticatePinUi) {

			username = new SharedPreferences(this).getString(
					"trumobi_username", "");
			password = new SharedPreferences(this).getString(
					"trumobi_password", "");

			PersonaLocalAuthentication mLocalAuth = new PersonaLocalAuthentication();

			mLocalAuth
					.bindMainActivityContext(mContext, mActivity,
							getBaseContext(), emailNotification,
							bndleToLocalAuthention);
			Intent i = getIntent();

			if (i.getExtras() != null) {// 371990 Geetha - added to handle to
										// email security notification
				emailNotification = i.getExtras().getBoolean(
						"newemail_notification");
				PersonaLog.d("Personamainactivity", "------------ emailNotification ----------" + emailNotification);
				securityNotification = i.getExtras().getBoolean(
						"email_securitynotify");
				long frmEmail = i.getLongExtra("accountId", -1);
				new SharedPreferences(mContext).edit()
						.putLong("notificatioId", frmEmail).commit();
				//PIN_SCREEN_ISSUE --> Commented
//				if (emailNotification) {
//					mLocalAuth.bindMainActivityContext(mContext, mActivity,
//							getBaseContext(), emailNotification,
//							bndleToLocalAuthention);
//				}
				//PIN_SCREEN_ISSUE <--

			}
			//PIN_SCREEN_ISSUE -->, Added
			if(!emailNotification)
				emailNotification = new SharedPreferences(mContext).getBoolean("enotificationonpin", false);
			
			if (emailNotification) {
				mLocalAuth.bindMainActivityContext(mContext, mActivity,
						getBaseContext(), true,
						bndleToLocalAuthention);
			}
			//PIN_SCREEN_ISSUE, <--

			mLocalAuth.createView(mAuthType,mNumberOfTrialsAllowed,mExpiryIndays);

		} else {

			if (mCommonFunctions.isConnectedToNetwork()) {

			
				Intent loginActivity = new Intent(mContext,
						PersonaLoginActivity.class);
				loginActivity.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(loginActivity);
				finish();

			} else {
				// Show an error msg that no network connectivity
				title = getString(R.string.pr_title_couldn_register);
				message = getString(R.string.error_in_connection);
				finishActivity = true;
				mCommonFunctions.showAlertDialog(message, title,
						finishActivity, mContext);
			}
		}

	}

	/*
	 * private void getSettingsFromDB() {
	 * 
	 * 
	 * File mDB =
	 * personaMainActivity.getDatabasePath(PersonaDbEncryptHelper.DB_NAME);
	 * TruBoxDatabase mPersonaDB = TruBoxDatabase.openOrCreateDatabase(mDB,
	 * null); String tabName = PersonaDbEncryptHelper.TABLE_PWDSETTINGS; String
	 * query = "SELECT * FROM "+tabName+ " WHERE "+
	 * PersonaDbEncryptHelper.USERNAME_PWDSETTINGS+"= '"+username+"'"; Cursor c
	 * = mPersonaDB.rawQuery(query, null); c =
	 * mPersonaDB.rawQuery("SELECT * FROM "+tabName, null); if(c.moveToFirst())
	 * { while(!c.isAfterLast()){ mLocalAuthType = c.getInt(2); mExpiryInDays =
	 * c.getInt(3); mNumOfTrialsAllowed = c.getInt(6); mPwdSetDate =
	 * c.getLong(11); PersonaLog.d("PersonaMainActivity",
	 * "Reading values from PWDSETTINGS table  " + " AuthType " + mLocalAuthType
	 * + " ExpiryDuration in Days "+ mExpiryInDays + " NumOfTrialsAllowed "+
	 * mNumOfTrialsAllowed); c.moveToNext(); } }
	 * 
	 * c.close(); mPersonaDB.close(); }
	 */

	private boolean isPinSaved() {

		/*
		 * boolean isPinSaved = new SharedPreferences(this).getBoolean(
		 * "isPersonaKeySaved", false);
		 * 
		 * if (isPinSaved) { return true; } return false;
		 */
		boolean isPinSaved = false;
		if (mPS != null && mPS.equals("Y1")) {
			isPinSaved = true;
		}
		return isPinSaved;

	}

	private void setPersonaKey() {
		long time = System.currentTimeMillis();

		// 290778 edited for content encryptionÂ84

		if (!isPersonaKeyExists()) {
			try {

				new SharedPreferences(this).edit()
						.putString("persona_Key", "persona" + time).commit();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	private boolean isPersonaKeyExists() {

		String personaKeyValue = "";
		boolean isPersonaKeyExists = false;

		try {

			personaKeyValue = new SharedPreferences(this).getString(
					"persona_Key", PersonaConstants.emptyString);

			if (personaKeyValue.equals(PersonaConstants.emptyString)) {
				isPersonaKeyExists = false;
			} else {
				isPersonaKeyExists = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (isPersonaKeyExists)
			return true;
		return false;

	}

	/*
	 * private void checkRegistrationInGcm() { GCMRegistrar.checkDevice(this);
	 * GCMRegistrar.checkManifest(this); final String regId =
	 * GCMRegistrar.getRegistrationId(this); if (regId.equals("")) {
	 * GCMRegistrar.register(this, PersonaConstants.SENDER_ID); } else {
	 * PersonaCommonfunctions.saveRegistrationId(this, regId); } }
	 */

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mAuthenticationTask != null) {
			mAuthenticationTask.cancel(true);
		}
		if (updatingPolicyProgress != null
				&& updatingPolicyProgress.isShowing()) {

			updatingPolicyProgress.cancel();
			updatingPolicyProgress = null;

		}
		PersonaLocalAuthentication localAuthclass = new PersonaLocalAuthentication();
		try {
			unregisterReceiver(localAuthclass.LoginCompleteReceiver);
		} catch (Exception e) {
			// e.printStackTrace();
		}

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onRemoteCallComplete(Message msg, int code) {

	}

	
	//CERTIFICATE
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		PersonaLog.d("PersonaMainActivity", "--------- onresume ----------");
		super.onResume();
		
		isCertificateBasedAuth=PersonaCommonfunctions.checkIsCertificatePoliciesOn(this, PersonaConstants.CERTIFICATE_ENABLED);
		// PIM_TIMER_CHG
		// int fromAutoLockTimer = getIntent().getIntExtra("TIMER", 0);
		boolean isDeviceRootedCheck=mCommonFunctions.isDeviceRooted();
		//boolean isDeviceRootedCheck=false;
		//290778 block app if the device is rooted
		if(isCertificateBasedAuth && isDeviceRootedCheck)
		{
		
				Intent intent = new Intent(PersonaMainActivity.this,
						PersonaSecurityProfileUpdate.class);
				intent.putExtra("securityevent",
						PersonaConstants.DEVICE_ROOTED_EVENT);
				intent.putExtra("securityaction", PersonaConstants.BLOCK_DEVICE);
				startActivity(intent);
				finish();
			
		}
		else{
			
	
		fromAutoLockTimer = getIntent().getIntExtra("TIMER", 0);
		if (fromAutoLockTimer == TruMobiTimerClass.FROM_APP_LOCK_TIMER_CLASS) {
			bFromTimer = true;
			bndleToLocalAuthention = new Bundle();
			bndleToLocalAuthention.putBoolean("fromTimer", bFromTimer);
		}

		if (isBlockedOrWiped()) {
			showMessage();
		} else {
			init();
		}
		}
		// PIM_TIMER_CHG
	}

	@Override
	public void onRemoteCallComplete(String json, int code) {
		// TODO Auto-generated method stub

	}

	// PIM_TIMER_CHG
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub

		// PIM_TIMER_CHG
		if (fromAutoLockTimer == TruMobiTimerClass.FROM_APP_LOCK_TIMER_CLASS || emailNotification) {
			// don't handle BACK KEY PRESS
		} else {
			super.onBackPressed();
		}
		//PIN_SCREEN_ISSUE, Added Line -->
		new SharedPreferences(getApplicationContext()).edit().putBoolean("enotificationonpin", false).commit();
		//PIN_SCREEN_ISSUE, <--
	}
	// PIM_TIMER_CHG

	@Override
	public void onDialogCancelled() {
		finish();
	}

	
	
	@Override
	public void onShowNextScreen(int showNextScreen) {
		// TODO Auto-generated method stub
		finish();
		switch(showNextScreen) {
		case PersonaConstants.WIPE_APP:
			Intent launcherScreen = new Intent(mContext, PersonaMainActivity.class);
			launcherScreen.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			launcherScreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
			launcherScreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(launcherScreen);
			System.exit(0); // handle to kill all objects used in app
			break;			
		case PersonaConstants.SHOW_LAUNCHER_SCREEN:
			Intent showLauncherScreen = new Intent(mContext, PersonaLauncher.class);
			startActivity(showLauncherScreen);
		}
	}

	@Override
	public void onRequestCertificate() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this,PersonaInstallCertificateActivity.class);
		startActivity(intent);
		finish();
		
	}
	
	
	
	

}
