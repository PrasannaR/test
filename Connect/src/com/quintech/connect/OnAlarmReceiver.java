package com.quintech.connect;

import com.quintech.common.AbstractData.Flags;
import com.quintech.common.Configuration;
import com.quintech.common.ILog.Type;
import com.quintech.connect.ApplicationService.Action;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

public class OnAlarmReceiver extends BroadcastReceiver 
{
	private static String TAG = "OnAlarmReceiver";
	private static Context currentContext = null;
	
	@Override  
	public void onReceive(Context context, Intent intent) 
	{    
		try
		{
			String intentAction = intent.getAction();
			currentContext = context;
			
			if ((intentAction!= null) && (intentAction.equalsIgnoreCase(Action.UPDATE_ALARM)))
			{
				// update configuration data in a new thread
		        Thread t = new Thread() 
		        {
		            public void run() 
		            {
		            	try
		            	{
			            	Constants.getLog().add(TAG, Type.Verbose, "Update Configuration timer ticked.");
			            	Configuration.update(false);
			            	
							// Notify Container if there were any new client settings
							if (Constants.getData().getFlag(Flags.FLG_IsRegistered, Data.SafeSpace))
							{
								
								if (Constants.getData().getFlag(Flags.FLG_PIMSettings_Updated))
								{
									Intent return_intent = new Intent("com.cognizant.trubox.event");

									// Add Event Type
									return_intent.setAction("PIM_Configuration_Update_Event");
									
									return_intent.putExtra("Event","PIM_Configuration_Update_Event");
									LocalBroadcastManager.getInstance(currentContext).sendBroadcast(return_intent);
									Constants.getData().setFlag(Flags.FLG_PIMSettings_Updated, false, false, Data.SafeSpace);
								}
								
								if (Constants.getData().getFlag(Flags.FLG_SecuritySettings_Updated))
								{
									Intent return_intent = new Intent("com.cognizant.trubox.event");

									// Add Event Type
									return_intent.setAction("Security_Profile_Change_Event");
									
									return_intent.putExtra("Event","Security_Profile_Change_Event");
									LocalBroadcastManager.getInstance(currentContext).sendBroadcast(return_intent);
									Constants.getData().setFlag(Flags.FLG_SecuritySettings_Updated, false, false, Data.SafeSpace);

								}			
								
								if (Constants.getData().getFlag(Flags.FLG_EmailSettings_Updated))
								{
									Intent return_intent = new Intent("com.cognizant.trubox.event");

									// Add Event Type
									return_intent.setAction("Email_Settings_Update_Complete_Event");
									
									return_intent.putExtra("Event","Email_Settings_Update_Complete_Event");
									LocalBroadcastManager.getInstance(currentContext).sendBroadcast(return_intent);
									Constants.getData().setFlag(Flags.FLG_EmailSettings_Updated, false, false, Data.SafeSpace);
								}			
									
							}
		            	}
		            	catch (Exception e)
		            	{
		            		Constants.getLog().add(TAG, Type.Error, "onReceive - new thread", e);
		            	}
		            }
		        };
		          
		        t.start();
			}
		}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "onReceive", e);
    	}
	}
}
