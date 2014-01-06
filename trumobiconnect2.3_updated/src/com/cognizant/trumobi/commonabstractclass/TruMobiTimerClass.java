package com.cognizant.trumobi.commonabstractclass;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.text.TextUtils.TruncateAt;
import android.util.Log;

import com.TruBoxSDK.SharedPreferences;
import com.TruBoxSDK.TruBoxDatabase;
import com.TruBoxSDK.TruBoxPasswordGenrator;
import com.cognizant.trumobi.commonbroadcastreceivers.ExternalPolicyUtil;
import com.cognizant.trumobi.externaladapter.ExternalAdapterRegistrationClass;

import com.cognizant.trumobi.log.PersonaLog;
import com.cognizant.trumobi.persona.PersonaLocalAuthentication;
import com.cognizant.trumobi.persona.PersonaMainActivity;


/**
 * 
 * @author rudhra-resolved auto lock timer issue
 *
 */
public class TruMobiTimerClass {

	public static ScheduledExecutorService scheduledExecutorService;
	public static long adjustedtime = 0;
//	public static final long LOG_OUT_PERIOD = 10;//60;//seconds Changed 60 --> 180
	public static long LOG_OUT_PERIOD;
	static Context _context;
	public static final int FROM_APP_LOCK_TIMER_CLASS = 0x01;
	public static boolean bActivityDisplaying = true;

	private static String TAG = TruMobiTimerClass.class.getName();
	
	public  static void  startTimerTrigger(Context context){
		_context = context;
		//PIM_TIMER_CHG
		ExternalAdapterRegistrationClass mExtAdpReg = ExternalAdapterRegistrationClass.getInstance(context);
		
		long logoutTime = new SharedPreferences(context).getLong("selected_autolocktime_l", -1);
		if(logoutTime == -1){ 	
			
			LOG_OUT_PERIOD = mExtAdpReg.getExternalPIMSettingsInfo().PasswordFailLockout.getValue();
			LOG_OUT_PERIOD = 60;
			startTimer();
		}
		else{
			
			if(logoutTime!=0)
			{
			
			LOG_OUT_PERIOD = logoutTime; 
			startTimer();
			}
		}
		/*Timer code disabling ends here 290661*/
	//	PersonaLog.d(TAG,"--- LOG_OUT_PERIOD ---"+LOG_OUT_PERIOD);
		
		/*Timer code disabling starts here 290661*/
	}

	public static void userInteractedTrigger(){

		adjustedtime = SystemClock.elapsedRealtime();
	}

	public static void userInteractedStopTimer(){
		adjustedtime = 0; //PIM_TIMER_CHG
		if(scheduledExecutorService != null && !scheduledExecutorService.isShutdown()){
		//	PersonaLog.d(TAG,"===  - Timer trigger stopped in  userInteractedStopTimer ===");	
		scheduledExecutorService.shutdown();
		scheduledExecutorService = null;
		/*adjustedtime = 0; //PIM_TIMER_CHG
*/		}
	}
	
	private static boolean isAppInForeground() {
		ActivityManager activityManager = (ActivityManager)_context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> appProcesses = activityManager
				.getRunningAppProcesses();
		for (RunningAppProcessInfo appProcess : appProcesses) {

			if (appProcess.processName.equalsIgnoreCase(_context.getApplicationInfo().packageName)) {
				if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
					return true;
				}
		
		}
		}
		return false;
	}
	
	public static void launchMainActivity(Context ctx) {
		//PersonaMainActivity in turn launches PIN screen when PIN already saved
		/*if(TruBoxPasswordGenrator.isGenerated)
		{
			TruBoxDatabase.writeFile();
		}*/
		Intent pop = new Intent(ctx,PersonaMainActivity.class);
		//pop.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		pop.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		pop.putExtra("TIMER", FROM_APP_LOCK_TIMER_CLASS); // PIM_TIMER_CHG
		ctx.startActivity(pop);
		
	}

	private static void startTimer() {

		// TODO Auto-generated method stub
		if (scheduledExecutorService == null
				|| scheduledExecutorService.isTerminated()
				|| scheduledExecutorService.isShutdown()) {
			adjustedtime = 0;
			scheduledExecutorService =  Executors.newSingleThreadScheduledExecutor();
			scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
				public void run() {
					try {
						
						//PIM_TIMER_CHG
						if(adjustedtime == 0){
						
							adjustedtime = SystemClock.elapsedRealtime();
						} //PIM_TIMER_CHG
						long logoutime =   SystemClock.elapsedRealtime() - adjustedtime ;
						logoutime =logoutime/1000;
				//		PersonaLog.d(TAG,"--- logoutime :"+logoutime);
						
						if(logoutime > LOG_OUT_PERIOD-1)
						{
							//scheduledExecutorService.shutdown();
							//scheduledExecutorService = null;
							
						//	PersonaLog.d(TAG,"-- logoutime > LOG_OUT_PERIOD");
							adjustedtime = 0;
						
							writeInPreferences();
							if(isAppInForeground()) {
								launchMainActivity(_context);
							
							}
							/*else {
								writeInPreferences();
							}*/
							
//							Intent brdCastIntent = new Intent("com.cognizant.trubox.event");
//							brdCastIntent.putExtra("event", ExternalPolicyUtil.EVENT_SEC_PROFILE_CHANGE);
//							_context.sendBroadcast(brdCastIntent);
							

						}					 	  
					//	PersonaLog.d("logoutime", ""+logoutime);

					} catch (Throwable t) {
						//Since this runs in the background thread, any exceptions in the run method of the SessionExtender
						// would be ignored
					}

				}
				
				private void writeInPreferences() {
					new SharedPreferences(_context).edit().putBoolean("showPinOnResume", true).commit();
				}

			}, 1, 1, TimeUnit.SECONDS);
		}
	}
}
