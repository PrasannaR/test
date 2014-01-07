package com.cognizant.trumobi.commonabstractclass;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.TruBoxSDK.SharedPreferences;

import com.cognizant.trumobi.log.PersonaLog;
import com.cognizant.trumobi.persona.PersonaMainActivity;
import com.cognizant.trumobi.persona.constants.PersonaConstants;
/**
 * 
 * KEYCODE			    AUTHOR		PURPOSE
 * PIN_SCREEN_ISSUE		367712		PIN Screen Issue on Email Notification
 *
 */
public class TruMobiBaseActivity extends Activity{

	public LocalBroadcastManager mLocalBcMgr;
	public static boolean bkillActivity = false;
    public static Context context;
    
    private static String TAG = TruMobiBaseActivity.class.getName();
    
    boolean bHomeKeyPressed = false;
    
    //371990 Geetha - added to handle to email security notification 
    public static boolean isAppWentToBg = false;
   	public static boolean isWindowFocused = false;
   	public static boolean isBackPressed = false;
   	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		context = this;
		mLocalBcMgr  = LocalBroadcastManager.getInstance(this);
		TruMobiTimerClass.userInteractedTrigger();
		new SharedPreferences(this).edit().putBoolean("appinBG", false).commit();	//PIN_SCREEN_ISSUE
	}
	
	//PIN_SCREEN_ISSUE, Added -->
	@Override
	protected void onUserLeaveHint() {
		// TODO Auto-generated method stub
		super.onUserLeaveHint();
		new SharedPreferences(this).edit().putBoolean("appinBG", true).commit();	//PIN_SCREEN_ISSUE
	}
	//PIN_SCREEN_ISSUE, <--
	
	//371990 Geetha - added to handle to email security notification 
	@Override
	protected void onStart() {
		if (isAppWentToBg) {
			isAppWentToBg = false;
		}
		new SharedPreferences(this).edit().putBoolean("appinBG", false).commit();	//PIN_SCREEN_ISSUE
		super.onStart();
	}


	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	
		
	//	TruMobiTimerClass.userInteractedStopTimer();

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		new SharedPreferences(this).edit().putBoolean("appinBG", false).commit();	//PIN_SCREEN_ISSUE
		 //DATA_WIPE_CHG
		if(TruMobiBaseActivity.bkillActivity){
			
			PersonaLog.d(TAG,"=== PersonaLauncer - TruMobiBaseActivity finish ===");
			finish();
			
		}
		else if(requirePinScreenOnResume())
		{
			PersonaLog.d(TAG,"===  - TruMobiBaseActivity launch main activity ===");
			TruMobiTimerClass.launchMainActivity(context);
		}
		else {
			PersonaLog.d(TAG,"===  - TruMobiBaseActivity Register for broadcast ===");
			mLocalBcMgr.registerReceiver(mMessageReceiver, new IntentFilter("KILL_ACT"));
			if(TruMobiTimerClass.scheduledExecutorService == null ||TruMobiTimerClass.scheduledExecutorService.isShutdown() || TruMobiTimerClass.scheduledExecutorService.isTerminated())
			{	
				PersonaLog.d(TAG,"===  - Timer trigger started ===");
				TruMobiTimerClass.startTimerTrigger(context);
			}
			
		}
		vulnarability();
//		mLocalBcMgr.registerReceiver(mMessageReceiver, new IntentFilter("KILL_ACT"));
//		if(TruMobiTimerClass.scheduledExecutorService == null ||TruMobiTimerClass.scheduledExecutorService.isShutdown() || TruMobiTimerClass.scheduledExecutorService.isTerminated())
//		{			
//			TruMobiTimerClass.startTimerTrigger(context);
//		}
//		vulnarability();
//		if(bkillActivity)
//			finish();
//		else {
//			if(requirePinScreenOnResume()) {
//				TruMobiTimerClass.launchMainActivity();
//				
//			}
//		}
		
	}

	


	private boolean requirePinScreenOnResume() {
		boolean requirePinScreen =  new SharedPreferences(this).getBoolean(
				"showPinOnResume", false);
		return requirePinScreen;
	}

	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {	
			PersonaLog.d(TAG, "in onReceive "+ TAG);
			bkillActivity = true;
			finish();
		}
	};




	@Override
	public boolean dispatchTouchEvent(final MotionEvent ev) {
		final int action = ev.getAction();
		if (action == MotionEvent.ACTION_DOWN) {}
		TruMobiTimerClass.userInteractedTrigger();
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean dispatchKeyEvent(final KeyEvent event) {

		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {}
		if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {}
		TruMobiTimerClass.userInteractedTrigger();
		return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean dispatchTrackballEvent(final MotionEvent ev) {	
		TruMobiTimerClass.userInteractedTrigger();
		return super.dispatchTrackballEvent(ev);
	}
	
	//371990 Geetha - added to handle to email security notification 
	@Override
	protected void onStop() {
		super.onStop();
		if (!isWindowFocused) {
			isAppWentToBg = true;
		}

	}
	//371990 Geetha - added to handle to email security notification 
	@Override
	public void onBackPressed() {

		
			isBackPressed = true;
		
	}

	//371990 Geetha - added to handle to email security notification 
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {

		isWindowFocused = hasFocus;

		if (isBackPressed && !hasFocus) {
			isBackPressed = false;
			isWindowFocused = true;
		}

		super.onWindowFocusChanged(hasFocus);
	}


	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	
		PersonaLog.d(TAG,"-- onDestroy --");
	
		
		 //DATA_WIPE_CHG
		TruMobiBaseActivity.bkillActivity = false;
		mLocalBcMgr.unregisterReceiver(mMessageReceiver); //DATA_WIPE_CHG
	
		
	}
	
	public void vulnarability(){
		boolean bvul = false;
		//check condition based on value from truboxsdk
		if(bvul)
		{
			finish();
		}
	}

	// IME_KEYPAD_TIMER_FIX : Added this method to handle Timer value during Keypad show.
	@Override
	public void onUserInteraction() {
		// TODO Auto-generated method stub
		super.onUserInteraction();
		TruMobiTimerClass.userInteractedTrigger();
	}

	
	
}
