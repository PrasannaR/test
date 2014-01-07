package com.quintech.connect;


import com.quintech.common.AbstractDeviceManagement.PolicyFailureNotificationType;
import com.quintech.common.Configuration;
import com.quintech.common.ILog.Type;
import com.quintech.connect.activities.TabHostActivity;

import android.content.Context;
import android.content.Intent;

public class DeviceAdminReceiver extends android.app.admin.DeviceAdminReceiver
{
	public static String TAG = "DeviceAdminReceiver";
	
	@Override
    public void onEnabled(Context context, Intent intent) 
    {
		try
		{
			Constants.getLog().add(TAG, Type.Info, "Device administration enabled.");
			
			// update configuration data in a new thread to notify portal
	        Thread t = new Thread() 
	        {
	            public void run() 
	            {
	            	try
	            	{
		            	Configuration.update(true);
	            	}
	            	catch (Exception e)
	        		{
	        			Constants.getLog().add(TAG, Type.Error, "onEnabled - update", e);
	        		}
	            }
	        };
	          
	        t.start();
			
			// launch tabhost activity after access has been granted
			Intent intentActivity = new Intent(Constants.applicationContext, TabHostActivity.class);
			intentActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        Constants.applicationContext.startActivity(intentActivity);
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "onEnabled", e);
		}
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) 
    {
    	try
    	{
    		Constants.getLog().add(TAG, Type.Info, "Device administration disable requested.");
    	}
    	catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "onDisableRequested", e);
		}
    	
        return "Device Administrator access is required to continue using Enterprise Connect.";
    }
    
    @Override
    public void onDisabled(Context context, Intent intent) 
    {
    	try
    	{
    		Constants.getLog().add(TAG, Type.Info, "Device administration disabled.");
    		
    		// set notification
    		Notifications.setRequestDeviceAdministration();
    		

    		// update configuration data in a new thread to notify portal
	        Thread t = new Thread() 
	        {
	            public void run() 
	            {
	            	try
	            	{
		            	Configuration.update(true);
	            	}
	            	catch (Exception e)
	        		{
	        			Constants.getLog().add(TAG, Type.Error, "onDisabled - update", e);
	        		}
	            }
	        };
	          
	        t.start();
    	}
    	catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "onDisabled", e);
		}
    }

    @Override
    public void onPasswordChanged(Context context, Intent intent) 
    {
    	try
    	{
    		Constants.getLog().add(TAG, Type.Info, "Device password changed.");
    		
    		// check policy
    		Constants.getDeviceManagement().enforcePolicy(PolicyFailureNotificationType.NotifyOnly);
    	}
    	catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "onPasswordChanged", e);
		}
    }

    @Override
    public void onPasswordFailed(Context context, Intent intent) 
    {
    	try
    	{
    		Constants.getLog().add(TAG, Type.Info, "Device password authentication failed.");
    		
    		// check policy
    		Constants.getDeviceManagement().enforcePolicy(PolicyFailureNotificationType.NotifyOnly);
    	}
    	catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "onPasswordFailed", e);
		}
    }

    @Override
    public void onPasswordSucceeded(Context context, Intent intent) 
    {
    	try
    	{
    		Constants.getLog().add(TAG, Type.Info, "Device password authentication succeeded.");
    	}
    	catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "onPasswordSucceeded", e);
		}
    }
}