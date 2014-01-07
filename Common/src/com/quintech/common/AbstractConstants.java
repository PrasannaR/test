package com.quintech.common;

import java.util.ArrayList;
import java.util.List;

import com.quintech.common.AbstractData.Settings;
import com.quintech.common.ILog.Type;


public abstract class AbstractConstants 
{
	protected static String TAG = "AbstractConstants";
	protected static ILog log = null;
	protected static AbstractData data = null;
	protected static AbstractLibrary library = null;
	protected static AbstractDeviceManagement deviceManagement = null;
	protected static Session session = null;
	protected static Credentials credentials = null;
	
	public static boolean isWISPrInProgress = false;
	public static boolean isWISPrLoggedIn = false;
	
	
	public enum BuildType
	{
		VERIZON_WIRELESS(0),
		VERIZON_BUSINESS(1);
		
		private int value;    
		private BuildType(int value) 
		{
			this.value = value;
		}

		public int getValue() 
		{
			return value;
		}
		
		public static BuildType getBuildType(int value)
		{
			for (BuildType type : BuildType.values())
			{
				if (type.getValue() ==  value)
					return type;
			}
			
			return BuildType.VERIZON_WIRELESS;
		}
	}
	
	
	public static void setLibrary(AbstractLibrary abstractLibrary)
	{
		library = abstractLibrary;
	}
	

	public static BuildType getApplicationBuildType()
	{
		// set default value
		BuildType type = library.getDefaultBuildType();
		
		// attempt to read value from database
		try
		{
			type = BuildType.getBuildType(Integer.valueOf(data.getSetting(Settings.SET_ApplicationBuildType)));
		}
		catch (Exception e)
    	{
			log.add(TAG, Type.Error, "getApplicationBuildType", e);
			
			// reset to default value
			type = library.getDefaultBuildType();
    	}
		
		return type;
	}
	
	
	public static int getIgnoredHotspotDurationInHours()
	{
		// default value
		int iReturn = 24;
		
		// attempt to read value from database
		try
		{
			iReturn = Integer.valueOf(data.getSetting(Settings.SET_IgnoredHotspotDurationInHours));
		}
		catch (Exception e)
    	{
			log.add(TAG, Type.Error, "getIgnoredHotspotDurationInHours", e);
			
			// reset to default value
			iReturn = 24;
    	}
		
		return iReturn;
	}
	
	
	public static int getSessionTimeoutInHours()
	{
		// default value
		int iReturn = 2;
		
		// attempt to read value from database
		try
		{
			iReturn = Integer.valueOf(data.getSetting(Settings.SET_SessionTimeoutInHours));
		}
		catch (Exception e)
    	{
			log.add(TAG, Type.Error, "getSessionTimeoutInHours", e);
			
			// reset to default value
			iReturn = 2;
    	}
		
		return iReturn;
	}
	
	
	public static int getUpdateConfigurationIntervalInHours()
	{
		// default value (7 days)
		int iReturn = 168;
		
		// attempt to read value from database
		try
		{
			iReturn = Integer.valueOf(data.getSetting(Settings.SET_UpdateConfigurationIntervalInHours));
		}
		catch (Exception e)
    	{
			log.add(TAG, Type.Error, "getUpdateConfigurationIntervalInHours", e);
			
			// reset to default value
			iReturn = 168;
    	}
		
		return iReturn;
	}
	
	
	public static int getNumberOfLogFilesToPersist()
	{
		// default value (keep the most recent 5 log files)
		int iReturn = 5;
		
		// attempt to read value from database
		try
		{
			iReturn = Integer.valueOf(data.getSetting(Settings.SET_NumberOfLogFilesToPersist));
		}
		catch (Exception e)
    	{
			log.add(TAG, Type.Error, "getNumberOfLogFilesToPersist", e);
			
			// reset to default value
			iReturn = 5;
    	}
		
		return iReturn;
	}
	
	
	public static List<String> getVZWServerList()
	{
		List<String> listServers = new ArrayList<String>();
		
		try
		{
			// parse semicolon separated list of servers
			String strValue = data.getSetting(Settings.SET_VZWServerList);
			
			String[] arrayServers = strValue.split(";");
			
			for (String s : arrayServers)
			{
				if (!s.trim().equals(""))
					listServers.add(s.trim());
			}
		}
		catch (Exception e)
    	{
			log.add(TAG, Type.Error, "getVZWServerList", e);
    	}
		finally
		{
			if (listServers == null)
				listServers = new ArrayList<String>();
			
			// add default servers
			if (listServers.size() < 1)
			{
				// primary - production
				listServers.add("http://hds.myvzw.com:7476/hds-soap-api/HDS");
				
				// secondary - testing/staging
				//listServers.add("http://66.198.74.88:7476/hds-soap-api/HDS");
			}
		}

		
		return listServers;
	}
	
	
	public static List<String> getRovaPortalServerList()
	{
		List<String> listServers = new ArrayList<String>();
		
		try
		{
			// parse semicolon separated list of servers
			String strValue = data.getSetting(Settings.SET_RovaPortalServerList);
			
			String[] arrayServers = strValue.split(";");
			
			for (String s : arrayServers)
			{
				if (!s.trim().equals(""))
					listServers.add(s.trim());
			}
		}
		catch (Exception e)
    	{
			log.add(TAG, Type.Error, "getRovaPortalServerList", e);
    	}
		finally
		{
			if (listServers == null)
				listServers = new ArrayList<String>();
			
			// add default servers
			if (listServers.size() < 1)
			{
				// primary - production
				listServers.add("https://vztest1.rova.com/");
			}
		}

		
		return listServers;
	}
}
