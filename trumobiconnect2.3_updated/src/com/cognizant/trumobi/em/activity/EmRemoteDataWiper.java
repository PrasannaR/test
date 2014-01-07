package com.cognizant.trumobi.em.activity;

import com.cognizant.trumobi.em.EmAccountBackupRestore;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.em.EmSecurityPolicy;
import com.cognizant.trumobi.em.EmSecurityPolicy.PolicyAdmin;

import com.cognizant.trumobi.em.provider.EmEmailContent;
import com.cognizant.trumobi.em.provider.EmEmailContent.Account;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

public class EmRemoteDataWiper extends BroadcastReceiver {

	private static long accountId = -1;

	private static String email = null;

	public void getIds(Context context, String mail) {
		Cursor c = context.getContentResolver().query(Account.CONTENT_URI,
				Account.CONTENT_PROJECTION, null, null, null);

		while (c.moveToNext()) {
			accountId = c.getLong(Account.CONTENT_ID_COLUMN);

			email = c.getString(Account.CONTENT_EMAIL_ADDRESS_COLUMN);

			// Log.v("Detailsssss : ",
			// "accountId : "+accountId+" name : "+name+" email "+email);
			if (email != null && email.equals(mail)) {
				c.close();
				return;
			}
		}
		c.close();
	}

	public void removeAdmin() {
		ComponentName devAdminReceiver = new ComponentName(EmWelcome.app,
				PolicyAdmin.class);
		DevicePolicyManager dpm = (DevicePolicyManager) (EmWelcome.app)
				.getSystemService(Context.DEVICE_POLICY_SERVICE);
		dpm.removeActiveAdmin(devAdminReceiver);

	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub

		String mail_id = intent.getStringExtra("Email_id");

		getIds(context, mail_id);
		if (intent.getAction().equals(
				"com.cognizant.appstore.nativeandroid.screens")) {
			if (accountId != -1) {
				try {

					removeACC(accountId);
					removeAdmin();

					accountId = -1;

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void removeACC(long mAccountId) {
		try {
			Account.restoreAccountWithId(EmAccountFolderList.thisContext,
					mAccountId);
			Uri uri = ContentUris.withAppendedId(
					EmEmailContent.Account.CONTENT_URI, mAccountId);
			EmAccountFolderList.thisContext.getContentResolver().delete(uri,
					null, null);
			// Update the backup (side copy) of the accounts
			EmAccountBackupRestore
					.backupAccounts(EmAccountFolderList.thisContext);
			// Release or relax device administration, if relevant
			EmSecurityPolicy.getInstance(EmAccountFolderList.thisContext)
					.reducePolicies();
		} catch (Exception e) {
			// Ignore
		}
		Email.setServicesEnabled(EmAccountFolderList.thisContext);
	}

}
