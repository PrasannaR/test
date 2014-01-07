package com.quintech.connect;


import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;

import com.quintech.common.Credentials;
import com.quintech.common.ILog.Type;
import com.quintech.common.Session;
import com.quintech.common.VenueInfo;
import com.quintech.connect.Log;


public class Constants extends com.quintech.common.AbstractConstants
{
	protected static String TAG = "Constants";
	public static Context applicationContext = null;
	public static List<VenueInfo> directoryVenueList = null;
	public static List<ScanResult> wifiScanList = null;
	public static int selectedDirectoryVenueIndex = -1;
	private static boolean hasInitialized = false;
	private static SmartConnect smartConnect = null;
	private static DeviceManagementManufacturer deviceManagementManufacturer = null;
	
	public static boolean getHasInitialized()
	{
		return hasInitialized;
	}
	
	
	public static void initializeApplicationConstants(Context context)
	{
		try
		{
			// initialize Application Constants
			Constants.applicationContext = context;
			Constants.setLibrary(new Library());
			
			
			// initialize log
			if (log != null)
				Constants.getLog().initialize();
			
			// initialize database
			Constants.getData().initialize(context);
						
			
			// write device info after database initialization
			Constants.getLog().writeDeviceInfo();
			
			
			// initialize device management
			Constants.getDeviceManagement().initialize();
			
			
			// set flag
			hasInitialized = true;
		}
		catch (Exception e)
    	{
    		Constants.getLog().addInitializationMessage(TAG, "Problem initializing application", e);
    	}
	}
	
	
	public static Data getData()
	{
		try
		{
			if (data == null)
				data = (Data) new Data();
			
			// TODO
			// check if database is INITIALIZED/open
			
			// if not open, close app or block user
			// maybe implement in device policy management
			
		}
		catch (Exception e)
    	{
    		Constants.getLog().addInitializationMessage(TAG, "Problem initializing Data", e);
    	}
		
		return (Data) data;
	}
	
	
	public static Log getLog()
	{
		try
		{
			if (log == null)
				log = (Log) new Log();
		}
		catch (Exception e)
    	{
    		// do not log an error if we can't access the log class!
    	}
		
		return (Log) log;
	}

	
	public static Library getLibrary()
	{
		try
		{
			if (library == null)
				library = (Library) new Library();
		}
		catch (Exception e)
    	{
    		Constants.getLog().addInitializationMessage(TAG, "Problem initializing Library", e);
    	}
		
		return (Library) library;
	}
	
	
	public static SmartConnect getSmartConnect()
	{
		try
		{
			if (smartConnect == null)
				smartConnect = new SmartConnect();
		}
		catch (Exception e)
    	{
    		Constants.getLog().addInitializationMessage(TAG, "Problem initializing Smart Connect", e);
    	}
		
		return smartConnect;
	}
	
	
	public static DeviceManagement getDeviceManagement()
	{
		try
		{
			if (deviceManagement == null)
				deviceManagement = (DeviceManagement) new DeviceManagement();
		}
		catch (Exception e)
    	{
    		Constants.getLog().addInitializationMessage(TAG, "Problem initializing Device Management", e);
    	}
		
		return (DeviceManagement) deviceManagement;
	}
	

	public static DeviceManagementManufacturer getDeviceManagementManufacturer()
	{
		try
		{
			if (deviceManagementManufacturer == null)
			{
				// select device management manufacturer for current device
				if (DeviceManagementSamsung.getCurrentSdkVersion() > 0.0)
					deviceManagementManufacturer = (DeviceManagementManufacturer) new DeviceManagementSamsung();
			}
		}
		catch (Exception e)
    	{
    		Constants.getLog().addInitializationMessage(TAG, "Problem initializing Device Management Manufacturer", e);
    	}
		
		return deviceManagementManufacturer;
	}
	
	
	public static Session getSession()
	{
		return session;
	}
	
	
	public static void setSession(Session newSession)
	{
		try
		{
			session = newSession;
		}
		catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "setSession", e);
    	}
	}
	
	
	public static Credentials getCredentials()
	{
		return credentials;
	}
	
	
	public static void setCredentials(Credentials newCredentials)
	{
		try
		{
			credentials = newCredentials;
		}
		catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "setCredentials", e);
    	}
	}
}
