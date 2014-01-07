package com.quintech.connect;

import com.quintech.common.Configuration;
import com.quintech.common.AbstractData.Settings;
import com.quintech.common.DeviceCommandIssuedInfo.DeviceCommands;
import com.quintech.common.ILog.Type;
import com.quintech.connect.ApplicationService.Action;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;

public class ApplicationServiceManager extends BroadcastReceiver 
{ 
	public static final String TAG = "ApplicationServiceManager"; 
	
	@Override public void onReceive(Context context, Intent intent) 
	{  
		try
		{	
			final Context contextFinal = context;
			final Intent intentFinal = intent;
			
			// run this in new thread
			Thread t = new Thread() 
	        {
	            public void run() 
	            {
	            	try
	            	{
		            	// send intent to service to ensure that it is running
		            	Intent intent =	new Intent(contextFinal.getApplicationContext(), ApplicationService.class);
						intent.setAction(ApplicationService.Action.NO_ACTION);	
						contextFinal.startService(intent);
						
						// wait an arbitrary time period to allow service to initialize if necessary
		            	Thread.sleep(1200);
		            	
		            	handleMessage(contextFinal, intentFinal);
	            	}
	            	catch (Exception e)		
	        		{
	            		Constants.getLog().add(TAG, Type.Debug, "onReceive - processing thread", e);
	        		}
	            }
	        };
	          
	        t.start();
		}
		catch (Exception e)		
		{
			Constants.getLog().add(TAG, Type.Debug, "onReceive", e);
		}
	}
	
	private void handleMessage(Context context, Intent intent) 
	{
		try
		{
			// handle intents
			if (intent.getAction().equalsIgnoreCase("android.intent.action.BOOT_COMPLETED")) 
			{   
				ComponentName comp = new ComponentName(context.getPackageName(), ApplicationService.class.getName());   
				ComponentName service = context.startService(new Intent().setComponent(comp)); 
				
				if (null == service)
				{    
					try
					{
						// something really wrong here    
						LogToFile serviceLog = new LogToFile();
						serviceLog.write(TAG, "Could not start service " + comp.toString(), null);  
					}
					catch (Exception e)
					{
						
					}
				}  
			} 
			
			else if (intent.getAction().equalsIgnoreCase("android.intent.action.PACKAGE_ADDED")) 
			{   
				// clear attempts when a new intent is received
				DivideHelper.resetAttempts();
				
				// check if Divide application needs activation whenever a new package has been installed on the device 
				DivideHelper.checkDivideStatus();
			} 
			
			else if (intent.getAction().equalsIgnoreCase("android.intent.action.PACKAGE_REMOVED")) 
			{
				// check status to reset flags when Divide app is not detected
				DivideHelper.isDivideClientInstalled();
			}
			
			else if (intent.getAction().equalsIgnoreCase("com.google.android.c2dm.intent.REGISTRATION")) 
			{
				// save client registration_id and send to portal
				
				String registrationID = intent.getStringExtra("registration_id"); 
				
			    if (intent.getStringExtra("error") != null) 
			    {
			        // Registration failed, should try again later.
			    } 
			    else if (intent.getStringExtra("unregistered") != null) 
			    {
			        // unregistration done, new messages from the authorized sender will be rejected
			    	
			    	// clear ID in database
			    	Constants.getData().setSetting(Settings.SET_GCMRegistrationID, "", false);
			    	
			    	// send to portal
			    	//Configuration.update(true);
					Intent intentAlarm = new Intent(Constants.applicationContext, OnAlarmReceiver.class);		
					intentAlarm.setAction(Action.UPDATE_ALARM);

					intentAlarm.putExtra("ForceUpdate", true);
					LocalBroadcastManager.getInstance(Constants.applicationContext).sendBroadcast(intentAlarm);
			    } 
			    else if (registrationID != null) 
			    {
			    	// Send the registration ID to the 3rd party site that is sending the messages.
			    	// This should be done in a separate thread.
			    	// When done, remember that all registration is done. 
			    	
			    	
			    	Constants.getLog().add(TAG, Type.Debug, "GCM ID: " + registrationID);
			    	
			    	// save ID in database
			    	Constants.getData().setSetting(Settings.SET_GCMRegistrationID, registrationID, false);

			    	
			    	// update configuration to send new registration ID to the portal
			    	// wait 2 minutes to allow any currently running updates to finish
			    	Thread t = new Thread() 
			        {
			            public void run() 
			            {
			            	try
			            	{
			            		// if an update is already in progress, wait
			            		if (Configuration.isInProgress())
			            		{
					            	// sleep for 2 minutes
					            	Thread.sleep(1000 * 60 * 2);
			            		}
			            		
			            		// update configuration
				            	//Configuration.update(true);
								Intent intentAlarm = new Intent(Constants.applicationContext, OnAlarmReceiver.class);		
								intentAlarm.setAction(Action.UPDATE_ALARM);

								intentAlarm.putExtra("ForceUpdate", true);
								LocalBroadcastManager.getInstance(Constants.applicationContext).sendBroadcast(intentAlarm);
			            	}
			            	catch (Exception e)
			        		{
			        			Constants.getLog().add(TAG, Type.Debug, "handleMessage", e);
			        		}
			            }
			        };
			          
			        t.start();
			    }
			}
			
			
			else if (intent.getAction().equalsIgnoreCase("com.google.android.c2dm.intent.RECEIVE")) 
			{
				// handle C2DM message
				
				// parse command info
				DeviceCommands deviceCommand = DeviceCommands.Unknown;
				String commandGuid = "";
				String commandParameter = "";
				
				try
				{
					deviceCommand = DeviceCommands.fromInteger(Integer.valueOf(intent.getExtras().getString("command")));
					
					
					if (intent.getExtras().getString("commandGuid") != null)
					{
						commandGuid = intent.getExtras().getString("commandGuid");
					}
					
					if (intent.getExtras().getString("commandParameter") != null)
					{
						commandParameter = intent.getExtras().getString("commandParameter");
					}
				}
				catch (Exception e)
				{
					Constants.getLog().add(TAG, Type.Error, "Unable to process MDM command");
					Constants.getLog().add(TAG, Type.Debug, "Unable to parse command: " + intent.getExtras().getString("COMMAND"));
				}
				
				Constants.getLog().add(TAG, Type.Debug, "Received MDM command: " + deviceCommand.toString());
				
				
				if ((commandGuid == null || commandGuid.equals("")) && 
						deviceCommand == DeviceCommands.RefreshData)
				{
					// update configuration, do not log command if CommandGUID was not specified
					//Configuration.update(true);	
					Intent intentAlarm = new Intent(Constants.applicationContext, OnAlarmReceiver.class);		
					intentAlarm.setAction(Action.UPDATE_ALARM);

					intentAlarm.putExtra("ForceUpdate", true);
					LocalBroadcastManager.getInstance(Constants.applicationContext).sendBroadcast(intentAlarm);
					
					//LocalBroadcastManager.getInstance(Constants.applicationContext).sendBroadcast(intentAlarm);
				}
				
				else if (deviceCommand != DeviceCommands.Unknown)
				{
					// insert newly issued device command into database (will check if CommandGUID already exists)
					Constants.getData().insertDeviceCommandIssued(commandGuid, deviceCommand, commandParameter);
					
					// execute all queued commands
					boolean requestCheckIn = Constants.getDeviceManagement().executeDeviceCommands();
					
					// if flag is true, call update() to send latest results to the portal
					if (requestCheckIn)
					{
					//	Configuration.update(true);	
						Intent intentAlarm = new Intent(Constants.applicationContext, OnAlarmReceiver.class);		
						intentAlarm.setAction(Action.UPDATE_ALARM);

						intentAlarm.putExtra("ForceUpdate", true);
						LocalBroadcastManager.getInstance(Constants.applicationContext).sendBroadcast(intentAlarm);
					}
				}
				else
				{
					// unknown command
					Constants.getLog().add(TAG, Type.Debug, "Received unknown MDM command: " + intent.getExtras().getString("COMMAND"));
				}
			}
			
			
			else 
			{   
				try
				{
					// unexpected intent
					LogToFile serviceLog = new LogToFile();
					serviceLog.write(TAG, "Received unexpected intent " + intent.toString(), null);     
				}
				catch (Exception e)
				{
					
				}
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Debug, "handleMessage", e);
		}
	}
}