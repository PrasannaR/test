package com.quintech.common;


import java.util.Timer;
import java.util.TimerTask;

import com.quintech.common.AbstractConstants.BuildType;
import com.quintech.common.AbstractData.Flags;
import com.quintech.common.ILog.Type;
import com.quintech.common.SessionInfo;




public class Session 
{
	private static String TAG = "Session";
	private static int updateIntervalSeconds = 40;
	
	private String bSSID = "";
	private String SSID = "";
	private long sessionSysID = 0;
	private Timer timerUpdate = null;
	private long intervalBytesReceived = 0;
    private long intervalBytesSent = 0;
	
    
	public Session()
	{

	}
	

	public long getSessionSysID()
	{
		return this.sessionSysID;
	}
	
	
	public void setBytesReceived(long bytesReceived)
	{
		this.intervalBytesReceived = bytesReceived;
	}
	
	
	public void setBytesSent(long bytesSent)
	{
		this.intervalBytesSent = bytesSent;
	}
	
	
	public void start()
	{
		try
		{
			// check if connected
			if (!AbstractConstants.library.isConnectedToWiFi())
			{
				AbstractConstants.log.add(TAG, Type.Debug, "Skipping starting of session because Wi-Fi supplication state is not connected.");
				return;
			}
			
			
			// set values
			this.bSSID = AbstractConstants.library.getConnectedBSSID();
			this.SSID = AbstractConstants.library.getConnectedSSID();
			
			// set current location
			Location location = AbstractConstants.library.getCurrentLocation();
			String latitude = null;
			String longitude = null;
			
			if ((location != null) && (AbstractConstants.getApplicationBuildType() == BuildType.VERIZON_BUSINESS))
			{
				latitude = String.valueOf(Double.valueOf(location.latitude));
				longitude = String.valueOf(Double.valueOf(location.longitude));
			}			
			
			// insert new session, save new session id
			this.sessionSysID = 
				AbstractConstants.data.insertSession(	bSSID, 
														SSID, 
														AbstractConstants.library.isJailBroken(), 
														AbstractConstants.library.getDeviceModelNumber(), 
														AbstractConstants.library.getDeviceManufacturer(), 
														AbstractConstants.library.getOSType(), 
														AbstractConstants.library.getOSVersion(), 
														AbstractConstants.library.getClientVersionName(), 
														latitude, 
														longitude);

			// set initial bytes used data
			updateBytesUsed();
			
			// reset timer to continuously update the session end time
			restartTimer();
			
			AbstractConstants.log.add(TAG, Type.Info, "Started Session ID " + String.valueOf(this.sessionSysID));
			
			// update configuration data in a new thread
	        Thread t = new Thread() 
	        {
	            public void run() 
	            {
	               Configuration.update(false);
	            }
	        };
	          
	        t.start();
		}
		catch (Exception e)
    	{
			AbstractConstants.log.add(TAG, Type.Error, "start", e);
			AbstractConstants.log.add(TAG, Type.Warn, "Failed to start session " + String.valueOf(this.sessionSysID));
    	}
	}
	
	
	public void continueSession()
	{
		try
		{
			// check if a matching session record exists
			String connectedSSID = "";
			
			try
			{
				// get info for currently connected hotspot
				connectedSSID = AbstractConstants.library.getConnectedSSID();
			}
			catch (Exception ex) 
			{ 
				AbstractConstants.log.add(TAG, Type.Warn, "Skipping update of recently used applications because there is no Session ID.");
			}
			
			
			// set SessionSysID
			SessionInfo previousSession = AbstractConstants.data.getSession(connectedSSID, true);
			this.sessionSysID = 0;
			
			if (previousSession != null)
				this.sessionSysID = previousSession.sysID;

			
			// if session was found
			if (this.sessionSysID > 0)
			{
				// set values
				this.bSSID = AbstractConstants.library.getConnectedBSSID();
				this.SSID = connectedSSID;
				
				// set inital bytes used data
				updateBytesUsed();
				
				// reset timer to continuously update the session end time
				restartTimer();
				
				AbstractConstants.log.add(TAG, Type.Info, "Continued Session ID " + String.valueOf(this.sessionSysID));
			}
			
			else
			{
				// if not found, start new session
				AbstractConstants.log.add(TAG, Type.Info, "Unable to continue session. Starting new session.");
				start();
			}
		}
		catch (Exception e)
    	{
			AbstractConstants.log.add(TAG, Type.Error, "continueSession", e);
    	}
	}
	
	
	public String getWISPrLoginUrl()
	{
		String url = "";
		
		try
		{
			// get data from the database - data might not be available from in memory objects if user
			// continued an existing WISPr session
			if (SSID.length()==0)
				SSID = AbstractConstants.library.getConnectedSSID();
			
			SessionInfo sessionInfo = AbstractConstants.data.getSession(SSID, true);
			
			if (sessionInfo != null)
				url = sessionInfo.wisprLoginURL;
			
			AbstractConstants.log.add(TAG, Type.Verbose, "Retrieved WISPr Login URL from stored session: " + url);
		}
		catch (Exception e)
    	{
			AbstractConstants.log.add(TAG, Type.Error, "getWISPrLoginUrl", e);
    	}
		
		
		return url;
	}
	
	
	public String getWISPrLogoffUrl()
	{
		String url = "";
		
		try
		{
			// get data from the database - data might not be available from in memory objects if user
			// continued an existing WISPr session
			SessionInfo sessionInfo = AbstractConstants.data.getSession(SSID, true);
			
			if (sessionInfo != null)
				url = sessionInfo.wisprLogoffURL;
			
			AbstractConstants.log.add(TAG, Type.Verbose, "Retrieved WISPr Logoff URL from stored session: " + url);
		}
		catch (Exception e)
    	{
			AbstractConstants.log.add(TAG, Type.Error, "getWISPrLogoffUrl", e);
    	}
		
		
		return url;
	}
	
	
	public void setWISPrProperties(String wisprLoginURL, String wisprLogoffURL, String wisprbwUserGroup, String wisprLocationName, String wisprLocationID)
	{
		// set WISPr values
		
		try
		{
			if (this.sessionSysID < 0)
			{
				AbstractConstants.log.add(TAG, Type.Debug, "Skipping update of WISPr properties because there is no Session ID");
				return;
			}
			
			AbstractConstants.data.updateSessionWISPrValues(this.sessionSysID, wisprLoginURL, wisprLogoffURL, wisprbwUserGroup, wisprLocationName, wisprLocationID);
			
			AbstractConstants.log.add(TAG, Type.Debug, "Set WISPr properties for Session ID " + String.valueOf(this.sessionSysID));
			AbstractConstants.log.add(TAG, Type.Debug, "WISPr Login URL: " + wisprLoginURL);
			AbstractConstants.log.add(TAG, Type.Debug, "WISPr Logoff URL: " + wisprLogoffURL);
			AbstractConstants.log.add(TAG, Type.Debug, "WISPr BW User Group: " + wisprbwUserGroup);
			AbstractConstants.log.add(TAG, Type.Debug, "WISPr Location Name: " + wisprLocationName);
			AbstractConstants.log.add(TAG, Type.Debug, "WISPr Location ID: " + wisprLocationID);
		}
		catch (Exception e)
    	{
			AbstractConstants.log.add(TAG, Type.Error, "setWISPrProperties", e);
    	}
	}
	
	
	public void stop()
	{
		try
		{
			AbstractConstants.log.add(TAG, Type.Debug, "Stopping session " + String.valueOf(this.sessionSysID));
			
			// clear current session variables
			this.bSSID = "";
			this.SSID = "";
			this.sessionSysID = 0;
			
			
			// stop timer
			if (timerUpdate != null)
				timerUpdate.cancel();
			
			AbstractConstants.log.add(TAG, Type.Info, "Stopped session");
		}
		catch (Exception e)
    	{
			AbstractConstants.log.add(TAG, Type.Error, "stop", e);
    	}
	}
	
	
	private void update()
	{
		try
		{
			// check if connected, and properties match this session record
			if (this.sessionSysID > 0 &&
					AbstractConstants.library.isConnectedToWiFi() &&
					AbstractConstants.library.getConnectedBSSID().equals(this.bSSID) &&
					AbstractConstants.library.getConnectedSSID().equals(this.SSID))
			{
				// update session end time
				AbstractConstants.data.updateSessionEndTime(this.sessionSysID);
				
				// update bytes downloaded/uploaded
				updateBytesUsed();
			}
			
			else
			{
				AbstractConstants.log.add(TAG, Type.Info, "Stopping Session ID " + String.valueOf(this.sessionSysID) + " due to loss of connection.");
				
				// stop session
				stop();
			}
		}
		catch (Exception e)
    	{
			AbstractConstants.log.add(TAG, Type.Error, "update", e);
    	}
	}
	
	
	private void updateBytesUsed()
	{
        try
        {
        	if (AbstractConstants.data.getFlag(Flags.FLG_EnableSessionBytesReporting))
        			AbstractConstants.library.updateApplicationBytesUsed(this.sessionSysID, this.intervalBytesReceived, this.intervalBytesSent);
        	else
        		AbstractConstants.log.add(TAG, Type.Info, "Bytes Used reporting is disabled.");
        }
        catch (Exception e)
    	{
        	AbstractConstants.log.add(TAG, Type.Error, "updateBytesUsed", e);
    	}
	}
        
	
	private void restartTimer()
	{
		// reset timer to continuously update the session end time
		try
		{
			// cancel previous timer
			if (timerUpdate != null)
				timerUpdate.cancel();
			
				
			// create a new timer
			timerUpdate = new Timer();
			
			// create schedule
			timerUpdate.scheduleAtFixedRate(new TimerTask() 
											{
												@Override
												public void run() 
												{
													update();
												}
											}, updateIntervalSeconds * 1000, updateIntervalSeconds * 1000);
			
			AbstractConstants.log.add(TAG, Type.Debug, "Restarted timer for Session ID " + String.valueOf(this.sessionSysID));
		}
		catch (Exception e)
    	{
			AbstractConstants.log.add(TAG, Type.Error, "restartTimer", e);
    	}
	}
}
