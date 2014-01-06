package com.cognizant.trumobi.em.activity;

import java.io.File;

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
import android.util.Log;
import android.widget.Toast;

public class EmUninstallBroadCastReceiver extends BroadcastReceiver {
	private Context mContext;
	@Override
	public void onReceive(Context context, Intent intent) {
		try
		{
			mContext = context;
		// TODO Auto-generated method stub
		Log.e("getdata",""+intent.getData());
		if(intent.getData()!=null){
		
		Log.e("UninstallBroadCastReceiver","App uninstalled "+intent.getData()+" package:com.cognizant.trumobi.persona");
		if((intent.getData().toString()).equals("package:com.cognizant.trumobi.persona")) {
			Log.e("Deletingggggggggg ", "Dataaaaaaaaaaaaaaa");
			Toast.makeText(context, "App  Uninstalled", Toast.LENGTH_LONG).show();
			//delete your app's local data
			//any other action item(if required) on deletion of persona
			removeAccounts(context);
			deleteLocalData();
			removeAdmin();
		}
		}
		}catch(Exception e){
			Log.e("Exception in UninstallBroadCastReceiver ", "onReceive : "+e);
		}
	}
	
	public void removeAdmin()
	{
		ComponentName devAdminReceiver = new ComponentName(mContext, PolicyAdmin.class);
	    DevicePolicyManager dpm = (DevicePolicyManager) (mContext).getSystemService(Context.DEVICE_POLICY_SERVICE);
	    dpm.removeActiveAdmin(devAdminReceiver);

	}
	
	private void removeAccounts(Context context) {
		// TODO Auto-generated method stub
		Cursor c = null;
		try {
			Log.e("Hiiiiiiiiiiiiiiiiii", "55555555555555555555555");
			c = context.getContentResolver().query(EmEmailContent.Account.CONTENT_URI,
					EmEmailContent.Account.CONTENT_PROJECTION, null, null, null);
			Log.e("Hiiiiiiiiiiiiiiiiii", "66666666666666666666666666666");
			while (c.moveToNext()) {
				long accountId = c.getLong(EmEmailContent.Account.CONTENT_ID_COLUMN);
				if(accountId != -1)
					removeACC(accountId);
				Log.e("Hiiiiiiiiiiiiiiiiii", "7777777777777777777777777777");
			}
			Log.e("Hiiiiiiiiiiiiiiiiii", "11111111111111111111111");
			// Update the backup (side copy) of the accounts
            EmAccountBackupRestore.backupAccounts(context);
            Log.e("Hiiiiiiiiiiiiiiiiii", "2222222222222222222222");
            // Release or relax device administration, if relevant
            EmSecurityPolicy.getInstance(context).reducePolicies();
            Log.e("Hiiiiiiiiiiiiiiiiii", "3333333333333333333333333333333");
            if(c != null)
            	c.close();
            Log.e("Hiiiiiiiiiiiiiiiiii", "444444444444444444444");
		} catch (Exception e) {
			if(c != null)
				c.close();
			Log.e("Exception in ", "UninstallBroadCastReceiver : " + e);
		}
	}

	private EmEmailContent.Account mSelectedContextAccount;
	private void removeACC(long mAccountId)
	{
		try {
			mSelectedContextAccount = EmEmailContent.Account.restoreAccountWithId(mContext, mAccountId);
			Uri uri = ContentUris.withAppendedId(
                    EmEmailContent.Account.CONTENT_URI, mAccountId);
			mContext.getContentResolver().delete(uri, null, null);
			Email.setServicesEnabled(mContext);
        } catch (Exception e) {
                // Ignore
        	Log.e("removeACCremoveACCremoveACC ", "removeACC "+e);
        }
		
	}

	public void deleteLocalData() {
		// TODO Auto-generated method stub
		File cache = mContext.getCacheDir();
		File appDir = new File(cache.getParent());

		if (appDir.exists()) {
			String[] children = appDir.list();
			for (String s : children) {
				if (!s.equals("lib")) {
					deleteDir(new File(appDir, s));

					Log.i("deleteLocalData", "/data/data/com.cognizant.trumobi.em/" + s + " DELETED");
				}
			}

		}

	}

	public boolean deleteDir(File dir) {
		if (dir != null && dir.isDirectory()) {
			String[] children = dir.list();
			int mChildrenLength = children.length;
			for (int i = 0; i < mChildrenLength; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}

		// if (!dir.getName().equals("GeneralInfo.xml"))

		return dir.delete();
		// return true;
	}
	
}
