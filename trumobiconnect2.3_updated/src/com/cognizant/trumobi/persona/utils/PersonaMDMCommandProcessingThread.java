package com.cognizant.trumobi.persona.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.Looper;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.log.PersonaLog;
import com.cognizant.trumobi.persona.PersonaInstallCertificateActivity;
import com.cognizant.trumobi.persona.constants.PersonaConstants;


public class PersonaMDMCommandProcessingThread extends Thread {

	boolean isLocked = false;
	StringBuilder ackSb = new StringBuilder();
	private int mCertificateNumber;
	Set<String> localStore = new HashSet<String>();
	String[][] mDM_Message;
	static Context mContext;
	String mDMAction;
	boolean mIsBlockRequired;
	PersonaMDMCommandProcessingThread PersonaMDMCommandProcessingThread;
	public static DevicePolicyManager devicePolicyManager;
	public static ComponentName demoDeviceAdmin;
	public static boolean showProgress = true;
	SharedPreferences sSharedPreferences, mSharedPreferences, fileNames;
	String password;
	ArrayList<Camera> arraylist;
	PersonaMDMHashTable mApplication;
	String commandUid;
	PersonaMDMCommandProcessingThread personaMDMCommandProcessingThread;
	String LOG_TAG = PersonaMDMCommandProcessingThread.class.getSimpleName();


	public void run() {
		 synchronized (this) {
			Looper.prepare();
			performMDMAction();
			try {
				PersonaLog.i(LOG_TAG, "Inside Thread wait");
				personaMDMCommandProcessingThread.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			PersonaLog.i(LOG_TAG, "Inside Thread after Wait");
		}

	}

	public PersonaMDMCommandProcessingThread() {
		personaMDMCommandProcessingThread = this;
	}

	public static void getInstance(Context context) {

		mContext = context;

	}

	public void performMDMAction() {

		if (PersonaMDMHashTable.mHashMessage != null) {
			if (PersonaMDMHashTable.mHashMessage
					.equals(PersonaConstants.EMAIL_PUSH)) {


				if (PersonaMDMHashTable.mHashCertificateno != 0)
					mCertificateNumber = PersonaMDMHashTable.mHashCertificateno;
				PersonaLog.d(LOG_TAG, "mCertificateNumber: " + mCertificateNumber);
				sSharedPreferences = mContext.getSharedPreferences(
						PersonaConstants.PERSONAPREFERENCESFILE,Context.MODE_APPEND);
				
				String CertificateDownloadValue= sSharedPreferences.getString("certificate_downloaded",
						PersonaConstants.emptyString);

				if ((!PersonaMDMHashTable.email_initiate
						.contains(PersonaMDMHashTable.mHashUid)))
					PersonaMDMHashTable.email_initiate.add(PersonaMDMHashTable.mHashUid);		
				if(CertificateDownloadValue.equals(PersonaConstants.emptyString))
				emailIntegration();
			}

		}

	}



	public void emailIntegration() {

		try {
			PersonaLog.d("MDMCommandProcessingTread", "show_notification : ");

			long when = System.currentTimeMillis();
			NotificationManager notificationManager = (NotificationManager) mContext

			.getSystemService(Context.NOTIFICATION_SERVICE);

			String certificate_download_message = mContext
					.getString(R.string.certificate_download_message);

			Notification notification = new Notification(R.drawable.pr_app_icon,
					certificate_download_message, when);

			String title = mContext.getString(R.string.pr_app_name);

			if (PersonaMDMHashTable.mHashCertificateno != 0) {

				mCertificateNumber = PersonaMDMHashTable.mHashCertificateno;

				Intent notificationIntent = new Intent(mContext,
						PersonaInstallCertificateActivity.class);
				notificationIntent
						.putExtra("certificateNo", mCertificateNumber);
				notificationIntent.putExtra("Email_Id",
						PersonaMDMHashTable.mHashEmailId);
				PendingIntent intent = PendingIntent.getActivity(mContext, 0,

				notificationIntent, 0);

				notification.setLatestEventInfo(mContext, title,
						certificate_download_message, intent);

				notification.flags |= Notification.FLAG_AUTO_CANCEL;

				notificationManager.notify(0, notification);
			}

		} catch (Exception e) {
			PersonaLog.e("PersonaMDMCommandProcessingThread",
					"Notification : " + e.toString());
		}

	}


}
