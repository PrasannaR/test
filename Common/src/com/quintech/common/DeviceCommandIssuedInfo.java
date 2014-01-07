package com.quintech.common;

public class DeviceCommandIssuedInfo 
{
	public enum DeviceCommands
	{
		Unknown (-1),
		ClearPasscode (0),
		EraseDevice (1),
		LockDevice (15),
		RefreshData (16),
		RequestPasscodeReset (19);
		
		private int value;    
		private DeviceCommands(int value) 
		{
			this.value = value;
		}

		public int getValue() 
		{
			return value;
		}
		
		public static DeviceCommands fromInteger(int i) 
		{
	        switch (i) 
	        {
		        case 0:
		        	return ClearPasscode;
		        	
		        case 1: 
		        	return EraseDevice;
		        	
		        case 15:
		        	return LockDevice;
		        	
		        case 16: 
		        	return RefreshData;
		        	
		        case 19:
		        	return RequestPasscodeReset;
	        }
	        
	        return Unknown;
	    }
	}
	
	public static String TAG = "DeviceCommandIssuedInfo";
	
	public String commandGuid = "";
	public DeviceCommands command = DeviceCommands.Unknown;
	public String parameter = "";
	public String dateReceived = "";
	public String dateExecuted = "";
	public boolean succeeded = false;
	public String errorMessage = "";
	public String dateReceivedByPortal = "";
}
