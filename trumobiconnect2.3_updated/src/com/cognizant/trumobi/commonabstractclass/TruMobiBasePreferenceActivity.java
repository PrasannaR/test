package com.cognizant.trumobi.commonabstractclass;

import com.TruBoxSDK.SharedPreferences;
import com.cognizant.trumobi.log.PersonaLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * 
 * KEYCODE			    AUTHOR		PURPOSE
 * PIN_SCREEN_ISSUE		367712		PIN Screen Issue on Email Notification
 *
 */

public class TruMobiBasePreferenceActivity extends PreferenceActivity{

	public LocalBroadcastManager mLocalBcMgr;
	public static boolean bkillActivity = false;
    public static Context context;
    
    private static String TAG = TruMobiBasePreferenceActivity.class.getName();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		context = this;
		mLocalBcMgr  = LocalBroadcastManager.getInstance(this);
		TruMobiTimerClass.userInteractedTrigger();
		new SharedPreferences(this).edit()
				.putBoolean("appinBG", false).commit(); // PIN_SCREEN_ISSUE
	}

	// PIN_SCREEN_ISSUE, Added -->
	@Override
	protected void onUserLeaveHint() {
		// TODO Auto-generated method stub
		super.onUserLeaveHint();
		new SharedPreferences(this).edit()
				.putBoolean("appinBG", true).commit(); // PIN_SCREEN_ISSUE
	}

	// PIN_SCREEN_ISSUE, <--

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	//	mLocalBcMgr.unregisterReceiver(mMessageReceiver);
	//	TruMobiTimerClass.userInteractedStopTimer(); //PIM_TIMER_CHG
	}

	

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		new SharedPreferences(this).edit()
				.putBoolean("appinBG", false).commit(); // PIN_SCREEN_ISSUE
		
		 //DATA_WIPE_CHG
		if(bkillActivity){
			
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
			TruMobiBaseActivity.bkillActivity = true; // PIM_REQ_CHG
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


	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mLocalBcMgr.unregisterReceiver(mMessageReceiver);
		//TruMobiTimerClass.userInteractedTrigger();
	
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
