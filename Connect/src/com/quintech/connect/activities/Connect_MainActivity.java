package com.quintech.connect.activities;


import com.quintech.common.AbstractData.Flags;
import com.quintech.common.ILog.Type;
import com.quintech.connect.ApplicationService;
import com.quintech.connect.Constants;
import com.quintech.connect.Notifications;

import android.os.Bundle;
import android.content.Intent;
import android.app.Activity;


public class Connect_MainActivity extends Activity
{
	private static String TAG = "MainActivity";
	
	public static String ACTION_INITIALIZE = "com.quintech.connect.INITIALIZE_ACTIVITY";
	public static String CLOSE_ACTIVITY = "com.quintech.connect.CLOSE_ACTIVITY";

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	try
    	{
    		super.onCreate(savedInstanceState);
    	
    		Constants.getLog().add(TAG, Type.Verbose, "Activity created.");
    
    		
    		// initialize Application Constants
    		Constants.initializeApplicationConstants(this.getApplicationContext());

 
    		// check for debug builds or prompt flag
    		if (Constants.getLibrary().isDebugBuild() || Constants.getData().getFlag(Flags.FLG_AlwaysShowTestBuildPrompt))
			{
    			if (Constants.getLibrary().isDebugBuild())
    				Constants.getLog().add(TAG, Type.Info, "DEBUG APPLICATION BUILD DETECTED");
    			
    			else if (Constants.getData().getFlag(Flags.FLG_AlwaysShowTestBuildPrompt))
    				Constants.getLog().add(TAG, Type.Info, "TEST BUILD FLAG DETECTED");
    			
    			
    			// show debug notification
    			Notifications.setDebugNotice();
			}
    		
  
			// send initialize intent to service
	        Intent intentInitialize = new Intent(this, ApplicationService.class);
	        intentInitialize.setAction(ApplicationService.Action.INITIALIZE_SERVICE);
			startService(intentInitialize);
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "onCreate", e);
    	}
    }
    
    
    @Override
	protected void onStart() 
	{
		try
		{
			// user manually launched app
			Constants.getLog().add(TAG, Type.Verbose, "Activity started.");
			
			super.onStart();
			
			
			// if UI is being shown, ensure service is running
			if (Constants.getData().getFlag(Flags.FLG_EnableFullUserInterface))
			{				
				Intent intent =	new Intent(this.getApplicationContext(), ApplicationService.class);
				intent.setAction(ApplicationService.Action.NO_ACTION);	
				this.startService(intent);
			}

			// if no UI is being shown
			else
			{
				// send message to service to ensure the service is running
				Intent intent =	new Intent(this.getApplicationContext(), ApplicationService.class);
				intent.setAction(ApplicationService.Action.NO_ACTION);	
				this.startService(intent);
				this.finish();
			}
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "onStart", e);
    	}
	}
    
    
    @Override
    protected void onResume() 
    {
    	try
    	{
	    	super.onResume();
	    	
	    	Constants.getLog().add(TAG, Type.Verbose, "Activity resumed.");
	    	
	    	// if flag to show full UI is not set, go back to Android home screen
	        if (!Constants.getData().getFlag(Flags.FLG_EnableFullUserInterface))
			{
				Intent intentMain = new Intent(Intent.ACTION_MAIN);
				intentMain.addCategory(Intent.CATEGORY_HOME);
				intentMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intentMain);
			}
	        
    		// show tab interface
	        else
	        {
	        	Intent intentTabActivity = new Intent(this, TabHostActivity.class);
	        	startActivity(intentTabActivity);
	        }
	        
	    	
	    	// close activity
	    	this.finish();
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "onResume", e);
    	}
    }
    
    
    @Override
    protected void onDestroy() 
    {
    	try
    	{
	    	super.onDestroy();
	    	
	    	Constants.getLog().add(TAG, Type.Verbose, "Activity destroyed.");
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "onDestroy", e);
    	}
    }
    
    
    @Override
    protected void onRestart() 
    {
    	try
    	{
	    	super.onRestart();
	    	
	    	Constants.getLog().add(TAG, Type.Verbose, "Activity restarted.");
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "onRestart", e);
    	}
    }
    
    
    @Override
    protected void onNewIntent(Intent intent) 
    {
    	try
    	{
	    	super.onNewIntent(intent);
	    	
	    	Constants.getLog().add(TAG, Type.Verbose, "Intent received.");
	    	
	    	if ((intent.getAction() != null) && (intent.getAction().equalsIgnoreCase(ACTION_INITIALIZE)))
	    	{
	    		initialize();
	    	}
	    	else if ((intent.getAction() != null) && (intent.getAction().equalsIgnoreCase(CLOSE_ACTIVITY)))
	    	{
	    		Constants.getLog().add(TAG, Type.Debug, "Closing activity");
	    	}
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "onNewIntent", e);
    	}
    	finally
    	{
    		this.finish();
    	}
    }
    
    
    private void initialize()
    {
    	try
    	{
    		// if flag to show full UI is not set, go back to Android home screen
	        if (!Constants.getData().getFlag(Flags.FLG_EnableFullUserInterface))
			{
				Intent intentMain = new Intent(Intent.ACTION_MAIN);
				intentMain.addCategory(Intent.CATEGORY_HOME);
				intentMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intentMain);
				
				// close activity
	        	this.finish();

				return;
			}
	        
	        
    		// show tab interface
	        Intent intentTabActivity = new Intent(this, TabHostActivity.class);
			startActivity(intentTabActivity);
	       
	        
	        // show splash screen
	        if (Constants.getData().getFlag(Flags.FLG_EnableSplashScreen))
	        {
				Intent intentSplashActivity = new Intent(this, SplashScreenActivity.class);
				startActivity(intentSplashActivity);
    		}
	        
	        // close activity
	        this.finish();
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "initialize", e);
    	}
    }
}