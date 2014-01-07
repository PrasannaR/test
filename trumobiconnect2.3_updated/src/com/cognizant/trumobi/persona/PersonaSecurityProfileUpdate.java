package com.cognizant.trumobi.persona;

import java.io.File;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.AccountManager;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;

import com.TruBoxSDK.SharedPreferences;
import com.TruBoxSDK.TruBoxDatabase;
import com.cognizant.trumobi.PersonaLauncher;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.em.EmAccountBackupRestore;
import com.cognizant.trumobi.em.EmExchangeUtils;
import com.cognizant.trumobi.em.EmSecurityPolicy;
import com.cognizant.trumobi.em.EmSecurityPolicy.PolicyAdmin;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.em.provider.EmEmailContent;
import com.cognizant.trumobi.em.provider.EmEmailContent.Account;
import com.cognizant.trumobi.em.service.EmMailService;
import com.cognizant.trumobi.externaladapter.ExternalAdapterRegistrationClass;
import com.cognizant.trumobi.log.PersonaLog;
import com.cognizant.trumobi.persona.PersonaSecurityUpdateDialogFragment.DialogCancelListener;
import com.cognizant.trumobi.persona.constants.PersonaCertfifcateAuthConstants;
import com.cognizant.trumobi.persona.constants.PersonaConstants;
import com.cognizant.trumobi.persona.net.PersonaGetJsonInterface;
import com.cognizant.trumobi.persona.net.PersonaJSONClient;

public class PersonaSecurityProfileUpdate extends FragmentActivity implements
		DialogCancelListener,PersonaGetJsonInterface {

	ProgressDialog progressDialog;
	int mEvent, mAction;

	private static String TAG = PersonaSecurityProfileUpdate.class.getName();

	
	private static long accountId = -1;
	private static String name = null;
	private static String email = null;
	private String mail_id;
	

	private final int SUCCESS = 0x01;
	private final int NOT_SUCCESS = 0x02;
	private Context mContext;
	private boolean isWipedAlready;
	private String mCol_S, mCol_SA;
	PersonaSecurityProfileUpdate personaSecurityProfileUpdate;
	PersonaJSONClient mUpdateEmailRevoke;
	int REVOKE_EMAIL=1200;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_main);
		mContext = this;
		Intent callingIntent = getIntent();
		mEvent = callingIntent.getIntExtra("securityevent", -1);
		mAction = callingIntent.getIntExtra("securityaction", -1);
		PersonaLog.d(TAG, "#################### Event is : "+mEvent);
		PersonaLog.d(TAG, "#################### Action is : "+mAction);
		isWipedAlready = callingIntent.getBooleanExtra("wipedAlready", false);
		updateSecurityAction(mEvent);
		mail_id = callingIntent.getStringExtra("Email_id");
		
		PersonaLog.d(TAG, "########### Received action is "+mAction);
	
		switch (mAction) {

		case  PersonaConstants.WIPE_APP:
			mCol_SA = "SA3";
			if(!isWipedAlready) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("Wiping Data.. Please Wait");
			progressDialog.setCancelable(false);
			progressDialog.show();
			
			AppWipe appWipe = new AppWipe();
			appWipe.start();
		
			}
			else {
				showMessage();
			}
			break;
		case  PersonaConstants.BLOCK_DEVICE:	
			mCol_SA = "SA2";
			updateDB();
			killAllActivitiesInStack(mContext);
			showMessage();
			break;
		case  PersonaConstants.WARN_USER:
			mCol_SA = "SA1";
			updateDB();
			showMessage();
			break;
		case PersonaConstants.SHOW_PIN_EXPIRY:			
			PersonaLog.d(TAG, "############## Inside Show PIN Expiry switch case");
			showMessage();
			break;
		case  PersonaConstants.BUGSENSE_ON_ALERT:
			showMessage();
			break;
		default:
			showMessage();
			break;
		}
	}

	private Handler appHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);

			updateUI();

		}

	};

	@Override
	public void onDialogCancelled() {
		if(PersonaMainActivity.isCertificateBasedAuth)
			updateEmailRevoke();
		
		
		finish();

	}
	private void updateSecurityAction(int event) {
		String eventS = Integer.toString(event);
		mCol_S = "S"+eventS;
	}
	private void updateEmailRevoke(){
		
		JSONObject holder = null;

		try {
			holder = new JSONObject();
			String sDeviceId = Secure.getString(mContext.getContentResolver(),
					Secure.ANDROID_ID);
			holder.put("device_id", sDeviceId);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		PersonaLog.d(TAG, "PersonaAPIConstants.DOWNLOAD_CERTIFICATE : holder :"
				+ holder);

		mUpdateEmailRevoke = new PersonaJSONClient(this,
				personaSecurityProfileUpdate, holder, mContext.getResources()
						.getString(R.string.download_email_certificate),
						PersonaCertfifcateAuthConstants.EMAIL_REVOKE_SERVICE, false,
						REVOKE_EMAIL, false);
		mUpdateEmailRevoke.execute();
		
	
	}
	
	
	public static void killAllActivitiesInStack(Context ctx) {
		LocalBroadcastManager mLocalBcMgr  = LocalBroadcastManager.getInstance(ctx);
		 Intent mKillActivity = new Intent("KILL_ACT");
		 mLocalBcMgr.sendBroadcast(mKillActivity);
   		// sendBroadcast(mKillActivity);
	}

	private void updateUI() {
		//if (iResult == SUCCESS) {
		deleteLocalData(mContext);
		/*boolean b = new SharedPreferences(mContext).getBoolean("isEmailAccountConfigured", false);
		String s = 	new SharedPreferences(mContext).getString("trumobi_username", "defstring");
		PersonaLog.d(TAG,"After wiping data.. reading from preferences.." + b + s);*/
		updateDB();
		killAllActivitiesInStack(mContext);
		ExternalAdapterRegistrationClass.setInstance();
		progressDialog.cancel();
		NotificationManager nMgr = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nMgr.cancelAll();
		showMessage();
	}


	
	private void updateDB() {
		String where = PersonaExternalDBHelper.COL0 + " = ?";
		String [] whereArgs = {"1"};
		ContentValues values = new ContentValues();
		values.put(PersonaExternalDBHelper.COL1, mCol_S);
		values.put(PersonaExternalDBHelper.COL2, mCol_SA);
		if(mAction == PersonaConstants.WIPE_APP) {
			values.put(PersonaExternalDBHelper.COL5,"N0");
		}
		getContentResolver().update(PersonaDatabaseProvider.ROUTINE_CHECK_CONTENT_URI, values,where, whereArgs);
	}

	private void showMessage() {
		android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
		PersonaSecurityUpdateDialogFragment dialog = new PersonaSecurityUpdateDialogFragment(
				mEvent, mAction);
		dialog.setRetainInstance(true);
		dialog.show(fm, "fragment_name");

	}
	
	public static boolean deleteLocalData(Context mAppContext) {
		// TODO Auto-generated method stub
		boolean bRet = false;
		PersonaLog.d(TAG, "==== deleteLocalData ====");
		File cache = mAppContext.getCacheDir();
		File appDir = new File(cache.getParent());

		if (appDir.exists()) {
			String[] children = appDir.list();
			for (String s : children) {
				if (!s.equals("lib")) {
					bRet = deleteDir(new File(appDir, s),mAppContext);

					PersonaLog.d(TAG, appDir.toString() + s
							+ " DELETED");
				}
			}

		}
		updateDbPostAdminDisabled(mAppContext);
		PersonaLog.d(TAG, "==== deleteLocalData ====" + bRet);
		return bRet;

	}

	private static boolean deleteDir(File dir,Context ctx) {
		if (dir != null && dir.isDirectory()) {
			String[] children = dir.list();
			int mChildrenLength = children.length;
			for (int i = 0; i < mChildrenLength; i++) {
				boolean success = deleteDir(new File(dir, children[i]),ctx);
				if (!success) {
					return false;
				}
			}
		}
	
		PersonaLog.d(TAG, "==== deleteDir file name ===="+dir.getName());
	//	if(!dir.getName().equals(TruBoxDatabase.getHashValue(PersonaDatabaseProvider.DB_NAME, ctx)) && !dir.getName().equals("encrypted.db3")) {
		if(!dir.getName().equals(TruBoxDatabase.getHashValue(PersonaDatabaseProvider.PERSONA_EXTERNAL_DB_NAME,ctx))) {
		dir.delete();
		if(dir.exists())
		{
			PersonaLog.d(TAG, "==== "+dir.getName() + "exists..!!!");
		}
		else {
			PersonaLog.d(TAG, "==== "+dir.getName() + "deleted..!!!");
		}
		}
		return true;

	}
	
	private static void updateDbPostAdminDisabled(Context ctx) {
		
		String where = PersonaExternalDBHelper.COL0 + " = 1";
		ContentValues values = new ContentValues();
		
			values.put(PersonaExternalDBHelper.COL5,"N0");
		
		ctx.getContentResolver().update(PersonaDatabaseProvider.ROUTINE_CHECK_CONTENT_URI, values,where, null);
	}


	private class AppWipe extends Thread {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();

		//	boolean bEmailWipe = emailDataWipe();
		//	if (bEmailWipe) {
		
			new SharedPreferences(mContext).edit().putBoolean("AdminDisabledAfterWipe",true).commit();
			TruBoxDatabase.resetAllValues(mContext);
			emailDataWipe(mContext,mail_id,false);
			appHandler.sendEmptyMessage(SUCCESS);
				/*boolean bDelLocalData = deleteLocalData(getApplicationContext());
			
				PersonaLog.d(TAG, "==== return value of bDelLocalData==:"
						+ bDelLocalData);
				if (bDelLocalData) {
					PersonaLog.d(TAG, "==== return value of bDelLocalData==:"
							+ bDelLocalData);
					appHandler.sendEmptyMessage(SUCCESS);
				} else {
					appHandler.sendEmptyMessage(NOT_SUCCESS);
				}*/
		/*	} else {
				PersonaLog.d(TAG, "====NOT SUCCESS =====");
				appHandler.sendEmptyMessage(NOT_SUCCESS);
			}
*/
		}

		
	


		

		/*public void removeAdmin() {
			ComponentName devAdminReceiver = new ComponentName(EmWelcome.app,
					PolicyAdmin.class);
			DevicePolicyManager dpm = (DevicePolicyManager) (EmWelcome.app)
					.getSystemService(Context.DEVICE_POLICY_SERVICE);
			dpm.removeActiveAdmin(devAdminReceiver);

		}*/
		
		
		
		
	
	}

	@Override
	public void onShowNextScreen(int screen) {
	
		if(PersonaMainActivity.isCertificateBasedAuth)
			updateEmailRevoke();
		
		finish();
		switch(screen) {
		case PersonaConstants.WIPE_APP:
			Intent launcherScreen = new Intent(mContext, PersonaMainActivity.class);
			launcherScreen.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			launcherScreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(launcherScreen);
			System.exit(0); // handle to kill all objects used in app
			break;			
		case PersonaConstants.SHOW_LAUNCHER_SCREEN:
			Intent showLauncherScreen = new Intent(mContext, PersonaLauncher.class);
			startActivity(showLauncherScreen);
		}
		
	}
	
	public static boolean emailDataWipe(Context ctx,String mail_id,boolean adminRemoved) {

		boolean bRet = true;
		PersonaLog.d(TAG, "=========== onReceive ============");

		getIds(ctx, mail_id);

		if (accountId != -1) {

		/*	EmailController.getInstance(getApplicationContext())
					.deleteAccount(accountId); // NaGa, Removing Corporate
												// Account if
												// Certificate is not
												// available
			EmailAccountSetupBasics
					.actionSetupExchangeIntent(getApplicationContext());*/
			removeACC(accountId,ctx);
			if(!adminRemoved)
			removeAdmin();

				/*EmailServiceUtils
						.stopExchangeService(getApplicationContext());*/
			EmExchangeUtils.stopExchangeService(ctx);
			accountId = -1;

				bRet = true;
			
		} else {
			bRet = false;
		}

		deleteAccountsConfigured(ctx);
		return bRet;

	}
	

	public static void getIds(Context context, String mail) {
		Cursor c = context.getContentResolver().query(Account.CONTENT_URI,
				Account.CONTENT_PROJECTION, null, null, null);

		while (c!= null && c.moveToNext()) {
			accountId = c.getLong(Account.CONTENT_ID_COLUMN);

			email = c.getString(Account.CONTENT_EMAIL_ADDRESS_COLUMN);

			// Log.v("Detailsssss : ",
			// "accountId : "+accountId+" name : "+name+" email "+email);
			if (email != null && email.equals(mail)) {
				c.close();
				return;
			}
		}
		if(c != null)
		c.close();
	}
	

	public static void removeAdmin() {
		ComponentName devAdminReceiver = new ComponentName(Email.getAppContext(),
				PolicyAdmin.class);
		DevicePolicyManager dpm = (DevicePolicyManager) (Email.getAppContext())
				.getSystemService(Context.DEVICE_POLICY_SERVICE);
		dpm.removeActiveAdmin(devAdminReceiver);

	}
	public static void removeACC(long mAccountId,Context ctx) {
		Context mCtx = ctx;
		try {
			new SharedPreferences(ctx).edit().putBoolean("isEmailAccountConfigured",false).commit();
        	
        	 // Get the account URI.
            final Account account = Account.restoreAccountWithId(ctx, accountId);
            if (account == null) {
                return; // Already deleted?
            }
            EmEmailContent.deleteSyncedDataSync(mCtx,accountId);
			Uri uri = ContentUris.withAppendedId(
					EmEmailContent.Account.CONTENT_URI, mAccountId);
			ctx.getContentResolver().delete(uri,
					null, null);
			// Update the backup (side copy) of the accounts
			EmAccountBackupRestore.backup(ctx);
			// Release or relax device administration, if relevant
			EmSecurityPolicy.getInstance(ctx)
					.reducePolicies();
			
			 // Clean up      
            Email.setServicesEnabled(ctx);
            Email.setNotifyUiAccountsChanged(true);
            EmMailService.actionReschedule(ctx);
            
		} catch (Exception e) {
			// Ignore
		}
		
	}
	
	public static void deleteAccountsConfigured(Context ctx) {
		// When tried incorrect pin even before account is synced (email DB
		// is not created).
		// Accounts are not getting deleted. So explicitly deleting the
		// Accounts
		try {
			new SharedPreferences(ctx).edit().putBoolean(
					"isEmailAccountConfigured", false);
			android.accounts.Account[] accountMgrList = AccountManager.get(
					ctx)
					.getAccountsByType("com.cognizant.trumobi.exchange");

			for (android.accounts.Account accountManagerAccount : accountMgrList) {
				AccountManager.get(ctx).removeAccount(
						accountManagerAccount, null, null);
				PersonaLog.d(TAG, "Deleting Account : "
						+ accountManagerAccount.name);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}



	
}
	@Override
	public void onRemoteCallComplete(Message json, int code) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onRemoteCallComplete(String json, int code) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRequestCertificate() {
		// TODO Auto-generated method stub
		
	}
}
