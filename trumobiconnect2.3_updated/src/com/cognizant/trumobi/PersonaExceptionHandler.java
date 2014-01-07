package com.cognizant.trumobi;

import java.lang.Thread.UncaughtExceptionHandler;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import com.TruBoxSDK.SharedPreferences;
import com.cognizant.trumobi.log.PersonaLog;
import com.cognizant.trumobi.persona.PersonaMainActivity;

public class PersonaExceptionHandler implements UncaughtExceptionHandler {

	private UncaughtExceptionHandler defaultExceptionHandler;

	private static final String TAG = "UNHANDLED_EXCEPTION";
	Context mContext;

	// constructor
	public PersonaExceptionHandler(Context context,
			UncaughtExceptionHandler pDefaultExceptionHandler) {
		defaultExceptionHandler = pDefaultExceptionHandler;
		this.mContext = context;
	}

	@Override
	public void uncaughtException(Thread arg0, Throwable e) {
		// TODO Auto-generated method stub

		// Intent i = new Intent(mContext, TestActivity.class);
		// mContext.startActivity(i);
		boolean isEmailAccountConfigured = new SharedPreferences(mContext)
				.getBoolean("isEmailAccountConfigured", false);
		PersonaLog.d(TAG, "Error :: " + e.toString()
				+ " isEmailAccountConfigured " + isEmailAccountConfigured);
		if (isEmailAccountConfigured) {
			showNotification();
		}

		defaultExceptionHandler.uncaughtException(arg0, e);
	}

	public void showNotification() {

		// define sound URI, the sound to be played when there's a notification
		Uri soundUri = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

		// intent triggered, you can add other intent for other actions
		Intent intent = new Intent(mContext, PersonaMainActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		// this is it, we'll build the notification!
		// in the addAction method, if you don't want any icon, just set the
		// first param to 0
		Resources res=mContext.getResources();
		CharSequence title =(CharSequence) res.getString(R.string.pr_app_crash_title,res.getString(R.string.pr_app_name));
		CharSequence message = (CharSequence) res.getString(R.string.pr_app_crash_message,res.getString(R.string.pr_app_name));
		Notification mNotification = new Notification(R.drawable.pr_app_icon,
				title, System.currentTimeMillis());
		mNotification
				.setLatestEventInfo(
						mContext,
						title,
						message,
						pIntent);
		mNotification.icon = R.drawable.pr_connect_header_icon;

		mNotification.sound = soundUri;
		// Notification mNotification = new Notification.Builder(mContext)
		//
		// .setContentTitle("SafeSpace crashed!")
		// .setContentText(
		// "Unexpectedly safespace crashed. Please login again to continue!")
		// .setSmallIcon(R.drawable.pr_app_icon).setContentIntent(pIntent)
		// .setSound(soundUri).setContentIntent(action)
		// // .addAction(R.drawable.pr_app_icon, "View", pIntent)
		// // .addAction(0, "Remind", pIntent)
		//
		// .build();

		//
		// mNotification.flags |= Notification.FLAG_ONGOING_EVENT;
		// mNotification.flags |= Notification.FLAG_SHOW_LIGHTS;
		NotificationManager notificationManager = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);

		// If you want to hide the notification after it was selected, do the
		// code below
		// myNotification.flags |= Notification.FLAG_AUTO_CANCEL;

		notificationManager.notify(9999, mNotification);
	}
}
