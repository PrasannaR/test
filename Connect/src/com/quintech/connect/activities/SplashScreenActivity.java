package com.quintech.connect.activities;
	 

import com.quintech.common.ILog.Type;
import com.quintech.connect.*;


import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
	 
public class SplashScreenActivity extends Activity 
{
	private static String TAG = "SplashScreenActivity";
	private int welcomeScreenDisplayTimeMS = 5000;
	
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) 
	{
		try
		{
			if (ev.getAction() == MotionEvent.ACTION_UP)
			{
				// reset display time to close splash after user touches it
				welcomeScreenDisplayTimeMS = 0;
			}
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "dispatchTouchEvent", e);
    	}
		
		return super.dispatchTouchEvent(ev);
	};
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		try
		{
			super.onCreate(savedInstanceState);
	
			setContentView(R.layout.splash);
			
			// set time to display splash screen
			welcomeScreenDisplayTimeMS = 5000;
			
			// create a thread to show splash screen for the specified time
			Thread welcomeThread = new Thread() 
			{
				int wait = 0;
		 
				@Override
				public void run() 
				{
					try 
					{
						super.run();
		 
						// use while to get the splash time. Use sleep() to increase the wait variable for every 100L.
						while (wait < welcomeScreenDisplayTimeMS) 
						{
							sleep(100);
							wait += 100;
						}
					} 
					catch (Exception e) 
					{
						System.out.println("EXc=" + e);
					} 
					finally 
					{
						// close splash activity
						finish();
					}
				}
			};
			
			welcomeThread.start();
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "onCreate", e);
		}
	}
}
	 
