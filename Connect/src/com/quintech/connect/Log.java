package com.quintech.connect;

import com.quintech.common.AbstractData.Flags;


public class Log implements com.quintech.common.ILog
{
	private static String TAG = "Log";
	public LogToFile logToFile;
	
	

	public boolean initialize()
	{
		boolean result = false;
		
		try
		{
			// create new log file if no instance exists
			if (logToFile == null)
			{
				logToFile = new LogToFile();
			
				add("LogInfo", Type.Info, "Start Log");
				
				// set flag when logToFile has been created
				result = true;
			}
		}
		catch (Exception e)
		{
			android.util.Log.e(TAG, "initialize: " + e.toString());
			e.printStackTrace();
		}
		
		return result;
	}
	
	
	public void writeDeviceInfo()
	{
		try
		{
			// write to both database and file
			
			// if database is open
			if (Constants.getData().isDatabaseOpen())
			{
				writeToDatabase("DeviceInfo", Type.Info, "Client Version: " + Constants.getLibrary().getClientVersionName(), null);
				writeToDatabase("DeviceInfo", Type.Info, "Release Build: " + String.valueOf(!Constants.getLibrary().isDebugBuild()), null);
				writeToDatabase("DeviceInfo", Type.Info, "Operating System: " + Constants.getLibrary().getOSType() + " " + Constants.getLibrary().getOSVersion(), null);
				writeToDatabase("DeviceInfo", Type.Info, "Device Manufacturer: " + Constants.getLibrary().getDeviceManufacturer(), null);
				writeToDatabase("DeviceInfo", Type.Info, "Device Model: " + Constants.getLibrary().getDeviceModelNumber(), null);
				writeToDatabase("DeviceInfo", Type.Info, "Debug mode: " + String.valueOf(Constants.getData().getFlag(Flags.FLG_EnableDebugMode)), null);
				writeToDatabase("DeviceInfo", Type.Info, "Samsung Enterprise Version: " + Constants.getLibrary().getSamsungEnterpriseSdkVersion(), null);
				
				for (String s : Constants.getLibrary().getAppSignature())
					writeToDatabase("DeviceInfo", Type.Info, "Application signature: " + s, null);
			}
			
			// write to file
			logToFile.write("\n\nDEVICE INFORMATION\n");
			logToFile.write(Constants.getLibrary().getClientPackageDisplayName());
			logToFile.write("Client Version: " + Constants.getLibrary().getClientVersionName());
			logToFile.write("Release Build: " + String.valueOf(!Constants.getLibrary().isDebugBuild()));
			logToFile.write("Operating System: " + Constants.getLibrary().getOSType() + " " + Constants.getLibrary().getOSVersion());
			logToFile.write("Device Manufacturer: " + Constants.getLibrary().getDeviceManufacturer());
			logToFile.write("Device Model: " + Constants.getLibrary().getDeviceModelNumber());
			logToFile.write("Debug mode: " + String.valueOf(Constants.getData().getFlag(Flags.FLG_EnableDebugMode)));
			logToFile.write("Samsung Enterprise Version: " + Constants.getLibrary().getSamsungEnterpriseSdkVersion());
			
			for (String s : Constants.getLibrary().getAppSignature())
				logToFile.write("Application signature: " + s);
			
			logToFile.write("\n\n");
		}
		
		catch (Exception e)
		{
			android.util.Log.e(TAG, "add: " + e.toString());
			e.printStackTrace();
		}
	}
	
	
	public void addToFileOnly(String tag, Type type, String message)
	{
		addToFileOnly(tag, type, message, null);
	}
	
	
	public void addToFileOnly(String tag, Type type, String message, Exception error)
	{
		// do not write to database
		
		try
		{						
			// write to debugger
			writeToDebugger(tag, type, message, error);
			
			// write log to file
			writeToFile(tag, type, message, error);
		}
		catch (Exception e)
		{
			android.util.Log.e(TAG, "add: " + e.toString());
			e.printStackTrace();
		}
	}
	
	
	public void add(String tag, Type type, String message, Exception error)
	{
		try
		{			
			// write to debugger
			writeToDebugger(tag, type, message, error);
			
			
			// attempt to write log to database
			boolean dbResult = false;

			if (Constants.getData().isDatabaseOpen())
				dbResult = writeToDatabase(tag, type, message, error);
			
			
			// write log to file
			if (!dbResult)
				writeToFile(tag, type, message, error);
		}
		catch (Exception e)
		{
			android.util.Log.e(TAG, "add: " + e.toString());
			e.printStackTrace();
		}
	}

	
	public void add(String tag, Type type, String message)
	{
		add(tag, type, message, null);
	}	
	
	
	public void addInitializationMessage(String tag, String message, Exception error)
	{
		// bypass check for log level to avoid infinite loop during database initialization
		
		// write to debugger
		writeToDebugger(tag, Type.Info, message, error);
		
		// write to both database and file
		
		// if database is open
		if (Constants.getData().isDatabaseOpen())
			writeToDatabase(tag, Type.Info, message, error);
		
		// write to file
		android.util.Log.i(tag, message);
		writeToFile(tag, Type.Info, message, error);
	}	
	
	
	private boolean writeToDatabase(String tag, Type type, String message, Exception error)
	{
		boolean result = false;
		
		try
		{
			// if we are not in debug mode, do not log verbose or debug messages
			if (!Constants.getData().getFlag(Flags.FLG_EnableDebugMode) && !Constants.getLibrary().isDebugBuild())
			{
				if (type == Type.Verbose || type == Type.Debug)
					return true;
			}
			
			
			// write all other messages to encrypted database regardless of allowed log level
			
			StringBuilder sb = new StringBuilder();
			
			if (error != null)
			{
				sb.append(error.getMessage().toString() + "\n");
				
				for (int i = 0; i < error.getStackTrace().length; i++)
					sb.append(error.getStackTrace()[i].toString() + "\n");
			}
			
			long count = Constants.getData().insertLog(tag, type.toString(), message, sb.toString());
			
			if (count > 0)
				result = true;
		}
		catch (Exception e)
		{
			
		}
		
		return result;
	}
	
	
	private void writeToDebugger(String tag, Type type, String message, Exception error)
	{
		try
		{			
			// only output to debug builds
			if (!Constants.getLibrary().isDebugBuild())
				return;
			
			// send log output
			if (error == null)
			{
				switch (type)
				{
					case Fatal:
						
						// Only available in API 8+
						//Log.wtf(tag, message);
						android.util.Log.v(tag, message);
						
						break;
						
					case Verbose:
						android.util.Log.v(tag, message);
						break;
						
					case Error:
						android.util.Log.e(tag, message);
						break;
						
					case Debug:
						android.util.Log.d(tag, message);
						break;
						
					case Warn:
						android.util.Log.w(tag, message);
						break;
						
					case Info:
						android.util.Log.i(tag, message);
						break;
						
					default:
						break;
				}
			}		
			else
			{
				switch (type)
				{
					case Fatal:
						
						// Only available in API 8+
						//Log.wtf(APPLICATION_TAG, tag + "\t\t" + message, error);
						
						android.util.Log.v(tag, message, error);
						break;
						
					case Verbose:
						android.util.Log.v(tag, message, error);
						break;
						
					case Error:
						android.util.Log.e(tag, message, error);
						break;
						
					case Debug:
						android.util.Log.d(tag, message, error);
						break;
						
					case Warn:
						android.util.Log.w(tag, message, error);
						break;
						
					case Info:
						android.util.Log.i(tag, message, error);
						break;
						
					default:
						break;
				}
			}
		}
		catch (Exception e)
		{
			android.util.Log.e(TAG, "add: " + e.toString());
			e.printStackTrace();
		}
	}
	
	
	private boolean writeToFile(String tag, Type type, String message, Exception error)
	{
		boolean result = false;
		
		try
		{			
			// check log level - only write info and warning level messages to unencrypted file
			if (type.ordinal() >= (int)Type.Warn.ordinal())
			{
				// write to file
				logToFile.write(tag, message, error);	
			}
			
			result = true;
		}
		catch (Exception e)
		{
			android.util.Log.e(TAG, "add: " + e.toString());
			e.printStackTrace();
		}
		
		return result;
	}
}
