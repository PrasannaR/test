package com.cognizant.trumobi.em;

import com.TruBoxSDK.SharedPreferences;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseActivity;
import com.cognizant.trumobi.em.activity.EmWelcome;
import com.cognizant.trumobi.log.EmailLog;
import com.cognizant.trumobi.persona.PersonaMainActivity;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * 
 * KEYCODE			    AUTHOR		PURPOSE
 * PIN_SCREEN_ISSUE		367712		PIN Screen Issue on Email Notification
 *
 */

public class EmNotificationService extends Service {

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent i, int flags, int startId) {
		// TODO Auto-generated method stub
		EmWelcome.getCurrentTopActivity(getApplicationContext(), true);
		Intent intent;
		boolean isAppinBG = new SharedPreferences(getApplicationContext()).getBoolean("appinBG", false);
		boolean personaMainOnTop = (EmWelcome.numActsonTask == 1) && !isAppinBG && !EmWelcome.isPersonaLauncherOnTop(getApplicationContext())
				; // Opening after
		boolean appinBgbyHome = isAppinBG && requirePinScreenOnResume();
		boolean appinBgbyBack = (TruMobiBaseActivity.isAppWentToBg && requirePinScreenOnResume());
		EmailLog.e("EmailNotificationService",
				"onStartCommand *** personaMainOnTop " + personaMainOnTop
						+ " appinBGbyBack : " + appinBgbyBack
						+ " appinBgbyHome : " + appinBgbyHome);
//		if(PersonaMainActivity.isPMAResume) {
//			Log.e("EmailNotificationService", "onStartCommand $$$ ");
//			new SharedPreferences(getApplicationContext()).edit().putBoolean("emailnotification", true).commit();
//			/*intent = new Intent(this, PersonaMainActivity.class);
//			intent.putExtra("newemail_notification", true);
//			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);*/
//			
//			return 0;
//		}
//		else
		if ((personaMainOnTop || appinBgbyHome || appinBgbyBack)) {
			EmailLog.e("EmailNotificationService", "onStartCommand $$$ ");
			intent = new Intent(this, PersonaMainActivity.class);
			intent.putExtra("newemail_notification", true);
			if(personaMainOnTop || requirePinScreenOnResume())
				new SharedPreferences(getApplicationContext()).edit().putBoolean("enotificationonpin", true).commit();
		} 
		else {
			EmailLog.e("EmailNotificationService", "onStartCommand ### ");
			intent = new Intent(this, EmWelcome.class);
		}
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		startActivity(intent);

		return START_NOT_STICKY;
	}

	private boolean requirePinScreenOnResume() {
		boolean requirePinScreen = new SharedPreferences(
				getApplicationContext()).getBoolean("showPinOnResume", false);
		return requirePinScreen;
	}

}
