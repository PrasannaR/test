

package com.cognizant.trumobi.em.activity;

import java.io.IOException;
import java.util.List;

import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseActivity;
import com.cognizant.trumobi.em.EmAccountBackupRestore;
import com.cognizant.trumobi.em.EmExchangeUtils;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.em.activity.setup.EmAccountSettingsUtils;
import com.cognizant.trumobi.em.activity.setup.EmAccountSetupExchange;
import com.cognizant.trumobi.em.customsetup.EmAccountSetupBasics;
import com.cognizant.trumobi.em.provider.EmEmailContent;
import com.cognizant.trumobi.em.provider.EmEmailContent.Mailbox;
import com.cognizant.trumobi.em.service.EmEasAuthenticatorService;
import com.cognizant.trumobi.exchange.provider.EmExchangeData;
import com.cognizant.trumobi.externaladapter.ExternalAdapterRegistrationClass;
import com.cognizant.trumobi.externaladapter.ExternalPIMSettingsInfo;
import com.cognizant.trumobi.log.EmailLog;
import com.cognizant.trumobi.persona.PersonaMainActivity;
import com.quintech.rovacommon.rovashared;

/**
 * 
 * KEYCODE			    AUTHOR		PURPOSE
 * PIN_SCREEN_ISSUE		367712		PIN Screen Issue on Email Notification
 * ROVA_PIM_SETTINGS	367712		Assign the settings from RovaSDK to account
 */

/**
 * The Welcome activity initializes the application and decides what Activity
 * the user should start with.
 * If no accounts are configured the user is taken to the AccountSetupBasics Activity where they
 * can configure an account.
 * If a single account is configured the user is taken directly to the MessageList for
 * the INBOX of that account.
 * If more than one account is configured the user is taken to the AccountFolderList Activity so
 * they can select an account.
 */
public class EmWelcome extends TruMobiBaseActivity {

    /**
     * Launch this activity.  Note:  It's assumed that this activity is only called as a means to
     * 'reset' the UI state; Because of this, it is always launched with FLAG_ACTIVITY_CLEAR_TOP,
     * which will drop any other activities on the stack (e.g. AccountFolderList or MessageList).
     */
	public static Context app;
	
	private final String PersonaPackage = "com.cognizant.trumobi.persona";
	private final int INSTALL_TRUHUB = 0;
	private final int SELECT_AUTO_SYNC = 1;
	
	private ExternalAdapterRegistrationClass extadpreg;
    private rovashared rovaS;
	
    private EmExchangeData mExchangeData;
    public static boolean useCertBasedSetup = false;
    
    public static void actionStart(Activity fromActivity) {
        Intent i = new Intent(fromActivity, EmWelcome.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        fromActivity.startActivity(i);
    }
      
    @Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		app = getApplicationContext();
		
		if(!ContentResolver.getMasterSyncAutomatically())
		{
			showDialogView(SELECT_AUTO_SYNC);
			return;
		}
		
		boolean fromAdmin = false;
		
		Intent iValues = getIntent();
		if(iValues != null)
			fromAdmin = iValues.getBooleanExtra("fromadminSecurity", false);
		
		extadpreg = ExternalAdapterRegistrationClass.getInstance(this);
        rovaS = new rovashared(this);
        
        if(useCertBasedSetup)
        	mExchangeData = EmExchangeData.getInstance(this);
		
		// Because the app could be reloaded (for debugging, etc.), we need
		// to make sure that
		// SyncManager gets a chance to start. There is no harm to starting
		// it if it has already
		// been started
		// TODO More completely separate SyncManager from Email app
		EmExchangeUtils.startExchangeService(this);
		
		
		if(!ContentResolver.getMasterSyncAutomatically())
		{
			showDialogView(SELECT_AUTO_SYNC);
			return;
		}
		
		
			// Reset the "accounts changed" notification, now that we're here
			Email.setNotifyUiAccountsChanged(false);

			// Quickly check for bulk upgrades (from older app versions) and
			// switch to the
			// upgrade activity if necessary
			if (EmUpgradeAccounts.doBulkUpgradeIfNecessary(this)) {
				finish();
				return;
			}

			// Restore accounts, if it has not happened already
			// NOTE: This is blocking, which it should not be (in the UI thread)
			// We're going to live with this for the short term and replace with
			// something
			// smarter. Long-term fix: Move this, and most of the code below, to
			// an AsyncTask
			// and do the DB work in a thread. Then post handler to finish() as
			// appropriate.
			EmAccountBackupRestore.restoreAccountsIfNeeded(this);

			

			// Find out how many accounts we have, and if there's just one, go
			// directly to it
			Cursor c = null;
			try {
				c = getContentResolver().query(
						EmEmailContent.Account.CONTENT_URI,
						EmEmailContent.Account.ID_PROJECTION, null, null, null);
				if(c!=null) {
				switch (c.getCount()) {
				case 0:
					if (useCertBasedSetup)
					{
						mExchangeData.setCertificateData(this);
						EmAccountSetupBasics.actionNewAccount(this);
					}
					else
						EmAccountSetupExchange.actionIncomingSettings(this, null,
								false, true, true); 
					break;
				case 1:
					
					c.moveToFirst();
					long accountId = c
							.getLong(EmEmailContent.Account.CONTENT_ID_COLUMN);
						EmMessageList.actionHandleAccount(this, accountId,
								Mailbox.TYPE_INBOX, fromAdmin);
						setEmailPIMSettings(accountId);	//ROVA_PIM_SETTINGS
					break;
				default:
						//EmAccountFolderList.actionShowAccounts(this);
					Intent i = new Intent(this, EmWelcome.class); // NaGa
            		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            		this.startActivity(i);
					break;
			
				}
			}
			} finally {
				if (c != null) {
					c.close();
				}
			}
			// Intent intent1 = new Intent();
			// intent1.setAction("com.cognizant.appstore.nativeandroid.screens");
			// intent1.putExtra("Email_id", "geetha@truboxmdmdev.com");
			// sendBroadcast(intent1);
			//accountDetails();
			finish();
		
	}
    
    //ROVA_POLICY_CHECK - 31Dec2013, -->
    private void setEmailPIMSettings(long accountId) {
    	
    	ExternalAdapterRegistrationClass mExtAdapReg;
		ExternalPIMSettingsInfo extnPIMSettInfo;
    	
		
		if (PersonaMainActivity.isRovaPoliciesOn) {
			mExtAdapReg = ExternalAdapterRegistrationClass.getInstance(this);
			extnPIMSettInfo = mExtAdapReg.getExternalPIMSettingsInfo();
			
			int attSize = extnPIMSettInfo.nMaxAttachmentSize;// extadpreg.getExternalPIMSettingsInfo().nMaxAttachmentSize;
			if (attSize > 0) {
				Log.e("attSizeattSizeattSize", "attSize : " + attSize);
				Email.MAX_ATTACHMENT_UPLOAD_SIZE = (attSize * 1024 * 1024);//attachment in terms of MB;
			}
			
			boolean downlodAtt = extnPIMSettInfo.bEmailDownloadAttachments;// extadpreg.getExternalPIMSettingsInfo().bEmailDownloadAttachments;
			String rovaSignature = extnPIMSettInfo.sEmailSignature;
			
			EmEmailContent.Account account = EmEmailContent.Account.restoreAccountWithId(this,
					accountId);
			if(rovaSignature != null) 
				account.setSignature(rovaSignature);
			Log.d("EmailWelcome","******** rovaSignature :"+rovaSignature);
			
			EmAccountSettingsUtils.commitSettings(this, account);
			
//			int newFlags = account.getFlags()
//					& ~(EmEmailContent.Account.FLAGS_BACKGROUND_ATTACHMENTS);
//			boolean downlodAtt = pimS.pimSettingInfo.bEmailDownloadAttachments;// extadpreg.getExternalPIMSettingsInfo().bEmailDownloadAttachments;
//			EmailLog.e("downlodAttdownlodAtt", "downlodAtt : " + downlodAtt);
//			newFlags |= downlodAtt ? EmailAccount.FLAGS_BACKGROUND_ATTACHMENTS
//					: 0;
//			EmailLog.e("attSizeattSize : " + attSize, " downlodAttdownlodAtt : "
//					+ downlodAtt);
//			account.setFlags(newFlags);
		}
	}
    //ROVA_POLICY_CHECK, <--
    
	private void accountDetails() {
		android.accounts.Account[] easAccountsList = AccountManager.get(this)
				.getAccountsByType(Email.EXCHANGE_ACCOUNT_MANAGER_TYPE);
		if (easAccountsList != null) {
			for (android.accounts.Account easAccount : easAccountsList) {
				AccountManagerFuture<Bundle> accountBunble = null;
				accountBunble = AccountManager.get(getApplicationContext()).getAuthToken(
						easAccount, null, true, null, null);
				try {
					Log.e("Unameeeeeeeeeeeeeeeeee",
							"Nameeeeeeeeeeeeeeeee : "
									+ accountBunble
											.getResult()
											.getString(
													EmEasAuthenticatorService.OPTIONS_USERNAME));

					Log.e("Upwddddddddddddddddddd",
							"Passworddddddddddddd : "
									+ accountBunble
											.getResult()
											.getString(
													EmEasAuthenticatorService.OPTIONS_USERNAME));
				} catch (OperationCanceledException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (AuthenticatorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
    
    private boolean isTruHubAvailable()
    {
    	final PackageManager pm = getPackageManager();
    	//get a list of installed apps.
    	List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

    	for (ApplicationInfo packageInfo : packages) {
    	    //Log.v("Packageeee", "Installed package :" + packageInfo.packageName);
    	    //Log.v("LaunchActivity", "Launch Activity :" + pm.getLaunchIntentForPackage(packageInfo.packageName));
    	    //if(packageInfo.packageName.equals(TruHubPackage) || packageInfo.packageName.equals(TruhubTabletPackage))
    	    if(packageInfo.packageName.equals(PersonaPackage))
    	    	return true;    	     
    	}
    	return false;
    	// the getLaunchIntentForPackage returns an intent that you can use with startActivity() 
    }
    private void showDialogView(int msg)
    {
    	int message_ID = (msg == INSTALL_TRUHUB)? R.string.account_setup_failed_persona_notavailable:R.string.account_setup_failed_certificate_inaccessible;
    	if(msg == SELECT_AUTO_SYNC)
    		message_ID = R.string.accounts_sync_not_active;
    	//message_ID = (msg == UNINSTALL_INSTALL)? R.string.uninstall_install_on_NoPermission:message_ID;
    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				this);
		alertDialogBuilder.setIcon(R.drawable.em_ic_dialog_alert);
		alertDialogBuilder.setTitle(this
				.getString(R.string.account_setup_failed_dlg_title));
		// set dialog message
		alertDialogBuilder
				.setMessage(
						this.getString(message_ID))
				.setCancelable(false)
				.setPositiveButton("Exit",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int id) {
								// if this button is clicked, close
								// current activity
								EmWelcome.this.finish();
							}
						});
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
    }
    
	//PIN_SCREEN_ISSUE, Added -->
    public static int numActsonTask = -1;
	public static boolean getCurrentTopActivity(final Context context, boolean isThis) {

		ActivityManager mActivityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> RunningTask = mActivityManager
				.getRunningTasks(1);
		ActivityManager.RunningTaskInfo ar = RunningTask.get(0);

		EmailLog.d("EmWelcome", "===== current Activity ======"
				+ ar.topActivity.getClassName().toString());
		
		if(isThis && ar.topActivity.getClassName().toString()
				.equals("com.cognizant.trumobi.email.activity.EmailWelcomeActivity"))
		{
			numActsonTask = ar.numActivities;
			return true;
		}
		if (ar.topActivity.getClassName().toString()
				.equals("com.cognizant.trumobi.persona.PersonaMainActivity"))
		{
			numActsonTask = ar.numActivities;
			return true;
		}
		else
			numActsonTask = ar.numActivities;

		return false;
	}
	

	public static boolean isPersonaLauncherOnTop(final Context context) {

		ActivityManager mActivityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> RunningTask = mActivityManager
				.getRunningTasks(1);
		ActivityManager.RunningTaskInfo ar = RunningTask.get(0);

		if (ar.topActivity
						.getClassName()
						.toString()
						.equals("com.cognizant.trumobi.PersonaLauncher"))
		{
			numActsonTask = ar.numActivities;
			return true;
		}

		return false;
	}
	//PIN_SCREEN_ISSUE, <--
    
}
