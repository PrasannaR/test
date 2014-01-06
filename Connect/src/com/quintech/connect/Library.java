package com.quintech.connect;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.quintech.common.AbstractConstants.BuildType;
import com.quintech.common.AbstractData.Flags;
import com.quintech.common.AbstractData.Settings;
import com.quintech.common.AbstractData;
import com.quintech.common.AbstractLibrary;
import com.quintech.common.ApplicationAssignedInfo;
import com.quintech.common.ApplicationInstalledInfo;
import com.quintech.common.ShortcutInfo;
import com.quintech.common.ILog.Type;
import com.quintech.connect.activities.CredentialPromptActivity;
import com.quintech.connect.activities.Connect_MainActivity;
import com.quintech.connect.activities.TabHostActivity;


import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.util.Base64;




public class Library extends com.quintech.common.AbstractLibrary
{
	private static String TAG = "Library";
	
	public static String INTERFACE_FILE = "/proc/self/net/dev";
	public static String ACTION_REFRESH_UI = "com.quintech.connect.ACTION_REFRESH_UI";
	private static int DEBUG_SIGNATURE_HASH = 267971925;

	
	public BuildType getDefaultBuildType()
	{
		// set default value for the case where the database cannot be opened
		return BuildType.VERIZON_BUSINESS;
	}

	
	public int getClientLogoDrawableID()
    {
    	int id = -1;
    	
    	try
    	{
    		if (Constants.getApplicationBuildType() == BuildType.VERIZON_BUSINESS)
    			id = R.drawable.enterprise_connect_logo;
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "getClientLogoDrawableID", e);
    	}
    	
    	
    	return id;
    }
	
	
	public String getLogDirectoryName()
	{
		// return default value
		return "logs";
	}
	
	
	public String getWisprAgentString()
	{
		// return agent string
		String wisprAgent = "";
		
		try
		{
			// TODO
			// add logic to return agent string per SSID or per build type
			
			// get Verizon Wireless agent string
			wisprAgent = Constants.getData().getSetting(Settings.SET_VerizonWirelessWisprAgent);
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "getWisprAgentString", e);
    	}
		
		// set default value if necessary
		if (wisprAgent == null || wisprAgent.equals(""))
			wisprAgent = "Verizon_wispr";
		
		
		return wisprAgent;
	}
	
	
	public boolean isConnectedToWiFi()
	{
		boolean isConnected = false;
		
		try
		{
			WifiManager wifiManager = (WifiManager) Constants.applicationContext.getSystemService(Context.WIFI_SERVICE);

			if (wifiManager.getConnectionInfo().getSupplicantState().equals(SupplicantState.COMPLETED))
				isConnected = true;
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "isConnectedToWiFi", e);
    	}
		
		return isConnected;
	}
	
	
	public String getConnectedSSID()
	{
		String ssid = "";
		
		try
		{
			WifiManager wifiManager = (WifiManager) Constants.applicationContext.getSystemService(Context.WIFI_SERVICE);
			ssid = wifiManager.getConnectionInfo().getSSID();
			
			Constants.getLog().add(TAG, Type.Verbose, "Connected to SSID " + ssid);
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "getConnectedSSID", e);
    	}
		
		return ssid;
	}
	
	
	public String getConnectedBSSID()
	{
		String bssid = "";
		
		try
		{
			WifiManager wifiManager = (WifiManager) Constants.applicationContext.getSystemService(Context.WIFI_SERVICE);
			bssid = wifiManager.getConnectionInfo().getBSSID();
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "getConnectedBSSID", e);
    	}
		
		return bssid;
	}
	
	
	public String getESN()
	{
		String esn = "";
		
		try
		{
			TelephonyManager telephonyManager = (TelephonyManager)Constants.applicationContext.getSystemService(Context.TELEPHONY_SERVICE);
			
			if (telephonyManager.getDeviceId() != null)
				esn = telephonyManager.getDeviceId();
			else
				Constants.getLog().add(TAG, Type.Warn, "ESN is not available");
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "getESN", e);
    	}
		
		return esn.toLowerCase();
	}
	
	
	public String getMSISDN()
	{
		String msisdn = "";
		
		try
		{
			TelephonyManager telephonyManager = (TelephonyManager)Constants.applicationContext.getSystemService(Context.TELEPHONY_SERVICE);
			
			if (telephonyManager.getLine1Number() != null)
				msisdn = telephonyManager.getLine1Number().toString();
			else
				Constants.getLog().add(TAG, Type.Warn, "MSISDN is not available");
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "getMSISDN", e);
    	}
		
		return msisdn;
	}
	
	
	public String getDeviceICCID()
	{
		String deviceICCID = "";
		
		try
		{
			TelephonyManager telephonyManager = (TelephonyManager)Constants.applicationContext.getSystemService(Context.TELEPHONY_SERVICE);
			
			if (telephonyManager.getSimSerialNumber() != null)
				deviceICCID = telephonyManager.getSimSerialNumber().toString();
			else
				Constants.getLog().add(TAG, Type.Warn, "ICCID is not available");
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "getDeviceICCID", e);
    	}
		
		return deviceICCID.toLowerCase();
	}
	
	
	public String getDeviceIMSI()
	{
		String deviceIMSI = "";
		
		try
		{
			TelephonyManager telephonyManager = (TelephonyManager)Constants.applicationContext.getSystemService(Context.TELEPHONY_SERVICE);
			
			if (telephonyManager.getSubscriberId() != null)
				deviceIMSI = telephonyManager.getSubscriberId().toString();
			else
				Constants.getLog().add(TAG, Type.Warn, "IMSI is not available");
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "getDeviceIMSI", e);
    	}
		
		return deviceIMSI.toLowerCase();
	}
	
	
	public String getDeviceSoftwareVersion()
	{
		String deviceSoftwareVersion = "";
		
		try
		{
			TelephonyManager telephonyManager = (TelephonyManager)Constants.applicationContext.getSystemService(Context.TELEPHONY_SERVICE);
			
			if (telephonyManager.getDeviceSoftwareVersion() != null)
				deviceSoftwareVersion = telephonyManager.getDeviceSoftwareVersion().toString();
			else
				Constants.getLog().add(TAG, Type.Warn, "Device Software Version is not available");
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "getDeviceSoftwareVersion", e);
    	}
		
		return deviceSoftwareVersion;
	}
	
	
	public String getDeviceModelNumber()
	{
		String deviceModelNumber = "";
		
		try
		{
			deviceModelNumber = Build.MODEL;
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "getDeviceModelNumber", e);
    	}
		
		return deviceModelNumber;
	}
	
	
	public float getDeviceCapacityGB(int storageType, int reportType)
	{
		// storageType
		// 0 = internal storage
		// 1 = external storage
		
		// reportType
		// 0 = total capacity
		// 1 = available capacity
		
		float result = (float) 0.0;

		try
		{
			StatFs statFs = null;
			
			// instantiate based on storage type
			if (storageType == 0)
				statFs = new StatFs(Environment.getDataDirectory().getPath());   
			else
				statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
	        
			
			// block size is in bytes
			if (reportType == 0)
			{
				long totalBytes = (long)statFs.getBlockCount() * (long)statFs.getBlockSize();
				result = Float.valueOf(totalBytes) / (1024 * 1024 * 1024);
			}
			
			else
			{
				long availableBytes =  (long)statFs.getAvailableBlocks() * (long)statFs.getBlockSize();
				result = Float.valueOf(availableBytes) / (1024 * 1024 * 1024);
			}
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "getDeviceCapacityGB", e);
    	}
		
		
		return result;
	}
	
	
	public int getBatteryLevelPercentage()
	{
		int result = 0;
		
		try
		{
            Intent bat = Constants.applicationContext.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            int level = bat.getIntExtra("level", 0);
            int scale = bat.getIntExtra("scale", 100);
            result = level * 100 / scale; 
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "getBatteryLevelPercentage", e);
    	}
		
		return result;
	}
	
	
	public String getCellularTechnology()
	{
		String result = "";
		
		try
		{
			TelephonyManager telephonyManager = (TelephonyManager)Constants.applicationContext.getSystemService(Context.TELEPHONY_SERVICE);
			int type = telephonyManager.getNetworkType();
			
			switch (type)
			{
				// LTE is 13 since API level 11 
				case 13:
					result = "LTE";
					break;
			
				case TelephonyManager.NETWORK_TYPE_CDMA:
				case TelephonyManager.NETWORK_TYPE_EVDO_0:
				case TelephonyManager.NETWORK_TYPE_EVDO_A:
				case TelephonyManager.NETWORK_TYPE_1xRTT:
				case TelephonyManager.NETWORK_TYPE_IDEN:
					result = "CDMA";
					break;
					
				case TelephonyManager.NETWORK_TYPE_EDGE:
				case TelephonyManager.NETWORK_TYPE_UMTS:
				case TelephonyManager.NETWORK_TYPE_GPRS:
				case TelephonyManager.NETWORK_TYPE_HSDPA:
				case TelephonyManager.NETWORK_TYPE_HSPA:
				case TelephonyManager.NETWORK_TYPE_HSUPA:
					result = "GSM";
					break;
					
				case TelephonyManager.PHONE_TYPE_NONE:
					result = "None";
					break;
			}
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "getCellularTechnology", e);
    	}
		
		return result;
	}
	
	
	public String getHomeCarrierNetwork()
	{
		String result = "";
		
		try
		{
			TelephonyManager telephonyManager = ((TelephonyManager) Constants.applicationContext.getSystemService(Context.TELEPHONY_SERVICE));
			
			if (telephonyManager.getNetworkOperatorName() != null)
				result = telephonyManager.getNetworkOperatorName();
			else
				Constants.getLog().add(TAG, Type.Warn, "Home Carrier Network is not available");
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "getHomeCarrierNetwork", e);
    	}
		
		return result;
	}
	
	
	public String getCurrentMobileCountryCode()
	{
		String result = "";
		
		// this will return a two-letter country code
		
		try
		{
			TelephonyManager telephonyManager = (TelephonyManager)Constants.applicationContext.getSystemService(Context.TELEPHONY_SERVICE);
			
			if (telephonyManager.getNetworkCountryIso() != null)
				result = telephonyManager.getNetworkCountryIso();
			else
				Constants.getLog().add(TAG, Type.Warn, "Current Mobile Country Code is not available");
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "getCurrentMobileCountryCode", e);
    	}
		
		return result.toUpperCase();
	}
	
	
	public boolean isWiFiConnected()
	{
		boolean connected = false;
		
		try
		{
			ConnectivityManager conMan = (ConnectivityManager)Constants.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (networkInfo != null && networkInfo.isConnected())
				connected = true;
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "isWiFiConnected", e);
		}
		
		
		return connected;
	}
	
	
	public File getWriteableDirectory() 
	{
		File writeableDirectory = null;
		
		try
		{
			// return a writeable directory
			// ensure there are write permissions on this directory in all cases
			
			try
			{
				writeableDirectory = Constants.applicationContext.getExternalFilesDir(null);
				writeableDirectory.mkdirs();
				
				if (writeableDirectory.canRead() && writeableDirectory.canWrite())
				{
					Constants.getLog().add(TAG, Type.Debug, "Selected ExternalFilesDir as writeable directory");
					return writeableDirectory;
				}
			}
			catch (Exception e) { }
			
			
			try
			{
				writeableDirectory = Environment.getExternalStorageDirectory();
				writeableDirectory.mkdirs();
				
				if (writeableDirectory.canRead() && writeableDirectory.canWrite())
				{
					Constants.getLog().add(TAG, Type.Debug, "Selected ExternalStorageDirectory as writeable directory");
					return writeableDirectory;
				}
			}
			catch (Exception e) { }
			
			
			try
			{
				writeableDirectory = Environment.getDataDirectory();
				writeableDirectory.mkdirs();
				
				if (writeableDirectory.canRead() && writeableDirectory.canWrite())
				{
					Constants.getLog().add(TAG, Type.Debug, "Selected DataDirectory as writeable directory");
					return writeableDirectory;
				}
			}
			catch (Exception e) { }
			
			
			
			try
			{
				writeableDirectory = Environment.getDownloadCacheDirectory();
				writeableDirectory.mkdirs();
				
				if (writeableDirectory.canRead() && writeableDirectory.canWrite())
				{
					Constants.getLog().add(TAG, Type.Debug, "Selected DownloadCacheDirectory as writeable directory");
					return writeableDirectory;
				}
			}
			catch (Exception e) { }
		
			
			
			try
			{
				writeableDirectory = new File("/data/data/com.quintech.connect/writeable/");
				writeableDirectory.mkdirs();
				
				if (writeableDirectory.canRead() && writeableDirectory.canWrite())
				{
					Constants.getLog().add(TAG, Type.Debug, "Selected internal app directory as writeable directory");
					return writeableDirectory;
				}
			}
			catch (Exception e) { }
			
			
			Constants.getLog().add(TAG, Type.Warn, "Unable to retrieve a writeable directory");
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "getWriteableDirectory", e);
		}
		
		
		return null;
	}
	
	
	public String readFileTostring(File file) 
	{
		String contents = "";
		
		
		try
		{
			contents = com.google.common.io.Files.toString(file, Charset.defaultCharset());
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "readFileTostring", e);
		}
		
		return contents;
	}
	
	
	public DeviceType getDeviceType()
	{
		DeviceType dType = DeviceType.VERIZON_WIRELESS_3G;

		try
		{		
			TelephonyManager telephonyManager = (TelephonyManager)Constants.applicationContext.getSystemService(Context.TELEPHONY_SERVICE);
			int iPhoneType = telephonyManager.getPhoneType();
			
			if (iPhoneType == TelephonyManager.PHONE_TYPE_GSM )
			{
				dType = DeviceType.VERIZON_WIRELESS_3G_INTERNATIONAL;
			}						
			else if (iPhoneType == TelephonyManager.PHONE_TYPE_CDMA)
			{
				if (telephonyManager.getSimState() == TelephonyManager.SIM_STATE_UNKNOWN)
				{
					dType = DeviceType.VERIZON_WIRELESS_3G;
				}
				else
				{
					dType = DeviceType.VERIZON_WIRELESS_3G_4G;
				}
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "getDeviceType", e);
		}
		
		return dType;
	}
	
	
	public String getDeviceManufacturer()
	{
		String deviceManufacturer = "";
		
		try
		{
			deviceManufacturer = Build.MANUFACTURER;
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "getDeviceManufacturer", e);
    	}
		
		return deviceManufacturer;
	}
	
	
	public String getOSType()
	{
		return "Android";
	}
	
	
	public int getPlatformId()
	{
		// return the ROVA Portal PlatformID for Android
		return 1;
	}
	
	
	public String extractKeyValue(List<AbstractData.Settings> listValues)
    {
    	StringBuilder result = new StringBuilder();
    	
    	try
    	{
    		result.append(listValues.get(0).toString().charAt(20));
    		result.append(listValues.get(3).toString().charAt(19));
    		result.append(listValues.get(3).toString().charAt(18));
    		result.append(listValues.get(1).toString().charAt(17));
    		result.append(listValues.get(0).toString().charAt(16));
    		result.append(listValues.get(4).toString().charAt(15));
    		result.append(listValues.get(3).toString().charAt(14));
    		result.append(listValues.get(2).toString().charAt(13));
    		result.append(listValues.get(2).toString().charAt(12));
    		result.append(listValues.get(1).toString().charAt(11));
    		result.append(listValues.get(3).toString().charAt(10));
    		result.append(listValues.get(4).toString().charAt(9));
    		result.append(listValues.get(1).toString().charAt(8));
    		result.append(listValues.get(4).toString().charAt(7));
    		result.append(listValues.get(2).toString().charAt(6));
    		result.append(listValues.get(0).toString().charAt(5));
    		result.append(listValues.get(0).toString().charAt(4));
    		result.append(listValues.get(2).toString().charAt(3));
    		result.append(listValues.get(1).toString().charAt(2));
    	}
    	catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "getMaxConnectionTimeout()", e);
    	}
    	
    	return result.toString();
    }
	
	
	public String getOSVersion()
	{
		String osVersion = "";
		
		try
		{
			osVersion = Build.VERSION.RELEASE;
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "getOSVersion", e);
    	}
		
		return osVersion;
	}
	
	
	public String getSdkVersion()
	{
		String sdkVersion = "";
		
		try
		{
			sdkVersion = String.valueOf(Build.VERSION.SDK_INT);
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "getSdkVersion", e);
    	}
		
		return sdkVersion;
	}
	
	
	public String getClientVersionName()
	{
		String clientVersion = "";
		
		try
		{
			clientVersion = Constants.applicationContext.getPackageManager().getPackageInfo(Constants.applicationContext.getApplicationInfo().packageName, 0).versionName;
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "getClientVersionName", e);
    	}
		
		return clientVersion;
	}
	
	
	public String getClientPackageName()
	{
		String packageName = "";
		
		try
		{
			packageName = Constants.applicationContext.getApplicationInfo().packageName;
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "getClientPackageName", e);
    	}
		
		return packageName;
	}
	
	
	public String getClientPackageDisplayName()
	{
		String displayName = "";
		
		try
		{
			displayName = Constants.applicationContext.getResources().getStringArray(R.array.app_name)[Constants.getApplicationBuildType().getValue()];
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "getClientPackageDisplayName", e);
    	}
		
		return displayName;
	}
	
	
	public static int getClientVersionNumber(Context context)
	{
		int version = 1;
		
		try
		{
			version = context.getPackageManager().getPackageInfo(context.getApplicationInfo().packageName, 0).versionCode;
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "getClientVersionNumber", e);
    	}
		
		return version;
	}
	
	
	public boolean searchForSuBinary(String path)
	{
		try
		{
			File f = new File(path);
			File[] files = f.listFiles();
			
			Constants.getLog().add(TAG, Type.Debug, "     Path = " + path);
			
			if (files == null)
				return false;
			
			for (File file : files)
			{
				Constants.getLog().add(TAG, Type.Debug, "          " + file.getName() );		
				
				if (file.isDirectory())
					return searchForSuBinary(file.getName());
				
				if (file.getName().toLowerCase().compareTo("su")==0)
					return true;
			}
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "searchForSuBinary", e);
    	}
		
		return false;
	}
	
	
	public boolean isJailBroken()
	{
		boolean isJailBroken = false;
		
		try
		{
			Map<String, String> variables = System.getenv();  

			String value = (String) variables.get("PATH");
			Constants.getLog().add(TAG, Type.Debug, "Path = " + value);

			String []pathelements = value.split(":");
			
			for (String item : pathelements)
			{
				if (searchForSuBinary(item))
				{
					isJailBroken = true;
					Constants.getLog().add(TAG, Type.Info, "Detected jailbroken device.  SU found inside path: " + item);
					break;
				}
			}

			FileInputStream fr = new FileInputStream("data/system/packages.xml");

			BufferedReader reader = new BufferedReader(new InputStreamReader(fr));
			String str="";
			StringBuffer buf = new StringBuffer();			
		
			if (fr!=null) {							
				while ((str = reader.readLine()) != null) {	
					buf.append(str + "\n" );
				}				
			}		
			fr.close();
			
			Pattern p = Pattern.compile("package name=\"com\\.noshufou\\.android\\.su\".*(codePath=\"\\S*\").*");
			Matcher m = p.matcher(buf);
			while (m.find()) { // Find each match in turn; String can't do this.
			     m.group(1); // Access a submatch group; String can't do this.				
			     Constants.getLog().add(TAG, Type.Info, "Detected jailbroken device.  SU APK found inside packages.xml: " +  m.group(1));
			     isJailBroken = true;
			     break;
			} 
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "isJailBroken", e);
    	}
		
		return isJailBroken;
	}
	
	
	public boolean isRoaming()
	{
		boolean isRoaming = false;
		
		try
		{
			TelephonyManager telephony = (TelephonyManager) Constants.applicationContext.getSystemService(Context.TELEPHONY_SERVICE);
			isRoaming = telephony.isNetworkRoaming();
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "isRoaming", e);
    	}
		
		return isRoaming;
	}
	
	
	public String getDeviceMacAddress()
	{
		String macAddress = "";		
		
	    try 
	    {
	    	// check if value is saved in the database
	    	macAddress = Constants.getData().getSetting(Settings.SET_DeviceMacAddress);
	    	
	    	if (macAddress != null && !macAddress.trim().equals(""))
	    	{
	    		Constants.getLog().add(TAG, Type.Debug, "Device MAC Address retrieved from the database");
	    	}
	    	
	    	else
	    	{
	    		// attempt to read value from device
	    		
	    		// Wi-Fi must be enabled
		    	WifiManager manager = (WifiManager) Constants.applicationContext.getSystemService(Context.WIFI_SERVICE); 
		    	Boolean isWiFiEnabled = manager.isWifiEnabled();
		    	
		    	if (!isWiFiEnabled)
		    	{
		    		Constants.getLog().add(TAG, Type.Debug, "Wi-Fi is not enabled");
		    		Constants.getLog().add(TAG, Type.Debug, "Enabling Wi-Fi to retrieve device MAC Address");
		    		manager.setWifiEnabled(true);
		    	}	
		    	
	    		// set value
	    		macAddress = manager.getConnectionInfo().getMacAddress();
		    		
	    		
	    		if (!isWiFiEnabled)
		    	{
		    		// return to disabled state
		    		Constants.getLog().add(TAG, Type.Debug, "Returned Wi-Fi to disabled state");
		    		manager.setWifiEnabled(false);
		    	}
	    		
		    		
	    		if (macAddress != null && !macAddress.equals(""))
	    		{
	    			// save value in database
	    			Constants.getLog().add(TAG, Type.Debug, "Device MAC Address retrieved");
	    			Constants.getData().setSetting(Settings.SET_DeviceMacAddress, macAddress, false);
	    		}
	    		else
	    		{
	    			Constants.getLog().add(TAG, Type.Debug, "Unable to determine Device MAC Address");
	    		}
		    	
	    	}
	    }
	    catch (Exception e)
	    {
	    	Constants.getLog().add(TAG, Type.Error, "getDeviceMacAddress", e);
	    }
	    
	    
		return macAddress;
	}
	
	
	public static NetworkInterface getWifiNetworkInterface(WifiManager manager) 
	{
	    Enumeration<NetworkInterface> interfaces = null;
	    
	    try 
	    {
	        // get a list of network interfaces
	        interfaces = NetworkInterface.getNetworkInterfaces();
	   
		    // get IP address of current wifi connection
		    int connectedIP = manager.getConnectionInfo().getIpAddress();
		     
		    // compare connected IP to list of interfaces
		    while (interfaces.hasMoreElements()) 
		    {
		    	NetworkInterface nInterface = interfaces.nextElement();
		 
		        // itereate through each interface's IP addresses
		        Enumeration<InetAddress> inetAddresses = nInterface.getInetAddresses();
		        
		        while (inetAddresses.hasMoreElements()) 
		        {
		            InetAddress nextElement = inetAddresses.nextElement();
		            int byteArrayToInt = byteArrayToInt(nextElement.getAddress(),0);
		 
		            // compare both endianness to ensure we get a match
		            if (byteArrayToInt == connectedIP || byteArrayToInt == Integer.reverseBytes(connectedIP)) 
		            {
		                return nInterface;
		            }
		        }
		    }
	    } 
	    catch (Exception e) 
	    {
	    	Constants.getLog().add(TAG, Type.Error, "getWifiNetworkInterface", e);
	    }
	 
	    return null;
	}
        
	
	public com.quintech.common.Location getCurrentLocation()
	{
		com.quintech.common.Location location = null;
		
		
        try
        {
        	// never report location if disabled by the portal, regardless of user preference
			if (Constants.getData().getSetting(Settings.SET_ReportDeviceLocation).equals("0"))
			{
				// do not access location
        		Constants.getLog().add(TAG, Type.Info, "Location reporting is disabled by the portal");
				return location;
			}
			
			// if location reporting is allowed, but user has disabled the preference
			if (Constants.getData().getSetting(Settings.SET_ReportDeviceLocation).equals("2") &&
					Constants.getData().getFlag(Flags.FLG_UserDisabledLocationReporting))
			{	
        		// do not access location
        		Constants.getLog().add(TAG, Type.Info, "Location access is not required and user has disabled location reporting");
        		return location;
        	}
        	
        
    		// get current location
        	LocationManager locationManager = (LocationManager)Constants.applicationContext.getSystemService(Context.LOCATION_SERVICE);
        	android.location.Location newSystemLocation = locationManager.getLastKnownLocation(LocationUtility.getRequiredProvider());

        	
        	// if last known location is not satisfactory
        	if (!LocationUtility.isLocationSatisfactory(newSystemLocation))
        	{
	        	Constants.getLog().add(TAG, Type.Debug, "Last known location was not found or did not meet requirements. Attempting to retrieve new location");
	        	
	        	// get new location in new thread
	        	Thread t = new Thread() 
		    	{
		            @Override
		            public void run() 
		            {
	            		LocationUtility.updateLocation();
		            }
		    	};
		    	
		    	t.start();
	        	
	        	
	        	// wait for new location to be set, or a maximum time
	        	long locationQueryTimeMillis = 1000 * LocationUtility.getDefaultListenerDurationSeconds();	
	        	long startTimeMillis = System.currentTimeMillis();
	        	
	        	Constants.getLog().add(TAG, Type.Debug, "Waiting " + String.valueOf(LocationUtility.getDefaultListenerDurationSeconds()) + " seconds for best new location");
	        	
	        	// wait a small amount of time for IsInProgress flag to be set
	        	Thread.sleep(200);
	        	
	        	while (LocationUtility.isInProgress() && (System.currentTimeMillis() - startTimeMillis) < locationQueryTimeMillis)
	        	{
	        		// wait
	        		Thread.sleep(500);
	        	}
	        	
	        	
	        	// set new location value
	        	newSystemLocation = LocationUtility.getResultLocation();
	        }
        	
	
	        
	        // check results
	        if (LocationUtility.isLocationSatisfactory(newSystemLocation))
        	{
	        	long diffMinutes = ((System.currentTimeMillis() - newSystemLocation.getTime()) / 1000) / 60;
	        	float accuracy = newSystemLocation.getAccuracy();
	        	String provider = newSystemLocation.getProvider();
	        	
	        	location = new com.quintech.common.Location(LocationUtility.getResultLocation().getLatitude(), LocationUtility.getResultLocation().getLongitude());

	        	Constants.getLog().add(TAG, Type.Debug, "Retrieved location from " + String.valueOf(diffMinutes) + " minutes ago");
	        	Constants.getLog().add(TAG, Type.Debug, "Last known location accuracy: " + String.valueOf(accuracy) + " meters");
	        	Constants.getLog().add(TAG, Type.Debug, "Last known location provider: " + provider);
        	}
	        
	        else
	        	Constants.getLog().add(TAG, Type.Debug, "Unable to retrieve updated location");
        }
        catch (Exception e) 
	    {
        	Constants.getLog().add(TAG, Type.Warn, "Unable to access current location");
	    }
        
        
        return location;
	}
	
	
	public void refreshUI()
	{
		try
		{
			// send intent to the service to refresh UI
			Intent intent =	new Intent(Constants.applicationContext, ApplicationService.class);
			intent.setAction(ApplicationService.Action.REFRESH_UI);		
			Constants.applicationContext.startService(intent);
			
			Constants.getLog().add(TAG, Type.Verbose, "Sent REFRESH_UI intent to Application Service from Library");
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "refreshUI", e);
		}
	}
	
    
    public static boolean hasWiFiConnection(Context context)
    {
    	boolean hasWiFi = false;
    	
    	try
    	{
    		ConnectivityManager conMan = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    		
			if (networkInfo != null && networkInfo.isConnected())
				hasWiFi = true;
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "hasWiFiConnection", e);
    	}
    	
    	
    	return hasWiFi;
    }
    
    
    public static void enableWiFi(Context context)
    {
    	try
    	{
    		WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			wm.setWifiEnabled(true); 
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "enableWiFi", e);
    	}
    }

    
    public boolean isDebugBuild()
    {
    	boolean isDebug = false;
    	
    	try
    	{
    		PackageManager pm = Constants.applicationContext.getPackageManager();
    		PackageInfo pi = pm.getPackageInfo(Constants.applicationContext.getPackageName(), PackageManager.GET_SIGNATURES);

    		// check for our debug certificate
    		for (Signature s : pi.signatures)
    		{
    			if (s.hashCode() == DEBUG_SIGNATURE_HASH)
    				isDebug = true;
    		}
	
    		// check if debugging is enabled
    		if ((pi.applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0)
    			isDebug = true;
    	}
    	catch (Exception e)
    	{
    		// since isDebugBuild() is referenced by our logging code,
    		// do not call a log function here as it may create an infinite loop
    	}


    	return isDebug;
    }
    
    
    public void deleteLogFiles()
    {
    	try
    	{
    		Constants.getLog().add(TAG, Type.Debug, "Deleting old log files");
    		
    		// delete database log records
    		Constants.getData().deleteOldLogs();
    		
    		
    		// delete log files
    		
    		// get sorted list
    		List<File> logFiles = Library.getLogFileListSortedAsc();
    		
    		if (logFiles == null || logFiles.size() < 1)
    			Constants.getLog().add(TAG, Type.Debug, "No readable log files found.");
    		
    		else
    		{	
    			Constants.getLog().add(TAG, Type.Debug, "Attempting to delete old log files.");
    		
    			
    			// delete non .log files
    			for (int i = 0; i < logFiles.size(); i++)
    				if (!logFiles.get(i).getName().endsWith(".log"))
    				{
    					logFiles.get(i).delete();
	         			Constants.getLog().add(TAG, Type.Debug, "Deleted log file: " + logFiles.get(i).getName());
    				}
    			
    			
    			// reload list
    			logFiles = Library.getLogFileListSortedAsc();
    			
    			
	     		// delete old log files
	     		for (int i = 0; i < logFiles.size(); i++)
	     		{
	     			// do not delete the N most recent files, number specified by appVars
	     			if (i < (logFiles.size() - Constants.getNumberOfLogFilesToPersist()))
					{
	     				if (logFiles.get(i).canWrite())
	     				{
	     					logFiles.get(i).delete();
		         			Constants.getLog().add(TAG, Type.Debug, "Deleted log file: " + logFiles.get(i).getName());
	     				}
	     				else
	     					Constants.getLog().add(TAG, Type.Debug, "Unable to delete log file, permission denied: " + logFiles.get(i).getName());
					}
	     			else
	     				Constants.getLog().add(TAG, Type.Verbose, "Skipped deleting log file: " + logFiles.get(i).getName());
	     		}
    		}	
    	}
	    catch (Exception e)
	 	{
	 		Constants.getLog().add(TAG, Type.Error, "deleteLogFiles", e);
	 	}
    }
    
    
    public static List<File> getLogFileListSortedAsc()
    {
    	List<File> logFilesListSorted = new ArrayList<File>();
    	
    	try
    	{
    		Constants.getLog().add(TAG, Type.Debug, "Getting log file list");
    		
			File logDirectory = Constants.getLog().logToFile.getLogDirectory();

			
			if (!logDirectory.canRead())
			{
				Constants.getLog().add(TAG, Type.Warn, "Cannot access log files. Permission denied attempting to read from " + logDirectory.getAbsolutePath());
			}
			
			else
			{
             	// check if directory exists
             	if (!logDirectory.exists())
             	{
             		Constants.getLog().add(TAG, Type.Warn, "Cannot access log files. Log directory does not exist: " + logDirectory.getAbsolutePath());
             	}
             	
             	else
             	{
             		Constants.getLog().add(TAG, Type.Debug, "Parsing log file list");
             		
             		// iterate directory and sort log files
             		File[] logFiles = logDirectory.listFiles();
             		
             		// sort by last date modified ascending
             		for (int i = 0; i < logFiles.length; i++)
             		{
             			// add first item to sorted list
             			if (logFilesListSorted.size() == 0)
             			{
             				logFilesListSorted.add(logFiles[i]);
             				continue;
             			}
             			
             			// if current file is newer than the last sorted item, append it to the sorted list
             			if (logFiles[i].lastModified() >= logFilesListSorted.get(i - 1).lastModified())
             			{
             				logFilesListSorted.add(logFiles[i]);
             			}
             			else
             			{
             				// loop through sorted list and insert current file before the next newest date modified
             				for (int j = 0; j < logFilesListSorted.size(); j++)
             				{
             					if (logFiles[i].lastModified() < logFilesListSorted.get(j).lastModified())
             					{
             						// this will add the item before the item currently in the "j" position
             						logFilesListSorted.add(j, logFiles[i]);
             						break;
             					}
             				}
             			}
             		}
             	}
			}
			
			if (logFilesListSorted != null)
				Constants.getLog().add(TAG, Type.Debug, "Number of log files sorted: " + String.valueOf(logFilesListSorted.size()));
    	}
	    catch (Exception e)
	 	{
	 		Constants.getLog().add(TAG, Type.Error, "getLogFileListSortedDesc", e);
	 	}
	    
	    
	    return logFilesListSorted;
    }
    
    
    public void copyDatabaseToLogDirectory(boolean decryptDatabase)
    {
    	// copy current database to log directory
    	try
    	{
    		Constants.getLog().addInitializationMessage(TAG, "Copying database to log directory", null);
    		
    		File logDirectory = Constants.getLog().logToFile.getLogDirectory();
    		
            if (logDirectory.canWrite()) 
            {
            	File currentDB = new File(Data.DB_PATH, Data.DB_NAME);
            	File backupDB = new File(logDirectory, Data.DB_NAME);
            	
            	// decrypt if flag is set
            	if (decryptDatabase)
            	{
	            	File decryptedDB = new File(logDirectory, "decrypted.db3");
	            	
	            	// delete file if it exists
	            	if (decryptedDB.exists())
	            		decryptedDB.delete();
	            	
	                Data.getDatabase().rawExecSQL("PRAGMA key = '" + Constants.getLibrary().extractKeyValue(Constants.getLibrary().listRequiredSettings()) + "'");
	                Data.getDatabase().rawExecSQL("ATTACH DATABASE '" + decryptedDB.getPath() + "' AS plaintext KEY ''");
	                Data.getDatabase().rawExecSQL("SELECT sqlcipher_export('plaintext')");
	                Data.getDatabase().rawExecSQL("DETACH DATABASE plaintext"); 
            	}
            	
            	// delete file if it exists
            	if (backupDB.exists())
            		backupDB.delete();
            	
            	// create new file
            	backupDB.createNewFile();

                if (currentDB.exists()) 
                {
                	Files.copy(currentDB, backupDB);

                    Constants.getLog().addInitializationMessage(TAG, "Copied database to log directory", null);
                }
                else
                	Constants.getLog().addInitializationMessage(TAG, "Current datbase not found", null);
            }
            else
            	Constants.getLog().addInitializationMessage(TAG, "Unable to write to log directory", null);
	    } 
    	catch (Exception e)
    	{
    		Constants.getLog().addInitializationMessage(TAG, "copyDatabaseToLogDirectory", e);
    	}
    }

    
    public void emailLogFiles()
	{
		try
		{
			// create output stream for zip file
			File logDirectory = Constants.getLog().logToFile.getLogDirectory();
			Constants.getLog().add(TAG, Type.Verbose, "Log directory: " + logDirectory.getPath());
			
			File fileZip = new File(logDirectory + "/Logs_" + Library.getDateTimeGMT("yyyy-MM-dd_HH.mm.ss") + ".zip");
			Constants.getLog().add(TAG, Type.Verbose, "ZIP path: " + fileZip.getPath());
			
			
			OutputStream outStream = new BufferedOutputStream(new FileOutputStream(fileZip));
			Constants.getLog().add(TAG, Type.Verbose, "Created output stream.");
			
			ZipOutputStream zipOutStream = new ZipOutputStream(new BufferedOutputStream(outStream));
			Constants.getLog().add(TAG, Type.Verbose, "Created ZIP output stream.");
			
			
			// copy encrypted database to log directory, decrypt if in debug mode
			copyDatabaseToLogDirectory(isDebugBuild());
			
			try
			{
				PackageManager pm = Constants.applicationContext.getPackageManager();
	    		PackageInfo pi = pm.getPackageInfo(Constants.applicationContext.getPackageName(), PackageManager.GET_SIGNATURES);
	
	    		Constants.getLog().add(TAG, Type.Debug, "pi.applicationInfo.flags: " + String.valueOf(pi.applicationInfo.flags));
			}
			catch (Exception e)
			{
				Constants.getLog().add(TAG, Type.Error, "getting app info flags", e);
			}
			
			// attach all available log files (which will include database)
			List<File> logFiles = Library.getLogFileListSortedAsc();
			
			if (logFiles != null)
			{
				Constants.getLog().add(TAG, Type.Verbose, "Number of all files in log directory: " + String.valueOf(logFiles.size()));
			
				for (File file : logFiles)
				{
					if (file != null && file.exists() && file.canRead())  
					{
						// do not include other zip files
						if (file.getName().toLowerCase().endsWith(".zip"))
							continue;
						
						Constants.getLog().add(TAG, Type.Verbose, "Adding file: " + file.getName());
					
						try
						{
							// add to zip file
							ZipEntry entry = new ZipEntry(file.getName());
							zipOutStream.putNextEntry(entry);
							zipOutStream.write(ByteStreams.toByteArray(new FileInputStream(file)));
							zipOutStream.closeEntry();
						}
						catch (Exception e)
				    	{
							Constants.getLog().add(TAG, Type.Verbose, "Error adding file: " + file.getName());
				    	}
						
					}
					else
						Constants.getLog().add(TAG, Type.Warn, "Unable to attach application log to support email.");
				}
			}
			else
				Constants.getLog().add(TAG, Type.Warn, "No application log files found.");
			
			
				
			// close streams
			zipOutStream.close();
			outStream.close();
			
			// attach zip file
			Constants.getLog().add(TAG, Type.Verbose, "Creating intent to send email with attachment.");
			Intent intent = new Intent(Intent.ACTION_SEND); 
			intent.putExtra(Intent.EXTRA_SUBJECT, Constants.applicationContext.getResources().getStringArray(R.array.app_name)[Constants.getApplicationBuildType().getValue()] + " Logs"); 		
			intent.putExtra(Intent.EXTRA_TEXT, Constants.applicationContext.getResources().getStringArray(R.array.application_logs_attached)[Constants.getApplicationBuildType().getValue()]);
			intent.setType("message/rfc822");   
			
			Uri uri = Uri.parse("file://" + fileZip.getAbsolutePath()); 
			intent.putExtra(Intent.EXTRA_STREAM, uri);
			
			Intent intentChooser = Intent.createChooser(intent, Constants.applicationContext.getResources().getStringArray(R.array.select_an_email_application)[Constants.getApplicationBuildType().getValue()]); 
			intentChooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			Constants.applicationContext.startActivity(intentChooser); 
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "emailLogFiles", e);
    	}
	}

    
    public void promptForCredentials()
	{
		try
		{
			// create new activity to show prompt
			
	    	Intent intent = new Intent(Constants.applicationContext,  CredentialPromptActivity.class);
	    	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			Constants.applicationContext.startActivity(intent);
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "promptForCredentials", e);
		}
	}


    public List<AbstractData.Settings> listRequiredSettings()
    {
    	List<AbstractData.Settings> listSettings = new ArrayList<AbstractData.Settings>();
    	
    	try
    	{
    		listSettings.add(AbstractData.Settings.SET_MaximumFailedPasswordAttempts);
    		listSettings.add(AbstractData.Settings.SET_MaximumInactivityTimeoutMinutes);
    		listSettings.add(AbstractData.Settings.SET_MinimumNumericalDigitsRequired);
    		listSettings.add(AbstractData.Settings.SET_MinimumNonLetterCharactersRequired);
    		listSettings.add(AbstractData.Settings.SET_MinimumUppercaseLettersRequired);
    	}
    	catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "Error creating required setting list", e);
    	}
    	
    	return listSettings;
    }
    
    
    public List<ApplicationInstalledInfo> getInstalledApplications(boolean includeSystemPackages)
    {
    	List<ApplicationInstalledInfo> listApplicationsInstalled = new ArrayList<ApplicationInstalledInfo>();
    	
    	try
    	{
	    	PackageManager pm = Constants.applicationContext.getPackageManager();
	    	
	    	// get a list of installed apps
	    	List<PackageInfo> packages = pm.getInstalledPackages(0);
	
	        for (PackageInfo packageInfo : packages) 
	        {
	        	// ignore system packages
	        	if (!includeSystemPackages && (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0)
	        		continue;

	        	ApplicationInstalledInfo appInstalled = new ApplicationInstalledInfo();
	        	appInstalled.packageDisplayName = packageInfo.applicationInfo.loadLabel(pm).toString();
	        	appInstalled.packageName = packageInfo.applicationInfo.packageName;
	        	appInstalled.version = packageInfo.versionName;
	        	appInstalled.packageSizeBytes = getDirectoryBytes(new File(packageInfo.applicationInfo.sourceDir));
	        	appInstalled.dynamicSizeBytes = getDirectoryBytes(new File(packageInfo.applicationInfo.dataDir));
	        	listApplicationsInstalled.add(appInstalled);
		    }
    	}
    	catch (Exception e) 
 	    {
         	Constants.getLog().add(TAG, Type.Error, "getInstalledApplications", e);
 	    }

        
        return listApplicationsInstalled;
    }
    
    
    public void importAssignedApplications(List<ApplicationAssignedInfo> listAssignedApplications)
    {
    	try
    	{
    		// calculate count of new app assignments
    		List<ApplicationAssignedInfo> listUninstalledMandatoryApps = new ArrayList<ApplicationAssignedInfo>();
    		int newAssignmentsCount = 0;
    		List<ApplicationInstalledInfo> listInstalledApps =	Constants.getLibrary().getInstalledApplications(true);
    		
    		// iterate new assignments
    		for (ApplicationAssignedInfo newAssignment : listAssignedApplications)
    		{
    			// check if assignment is new
    			boolean isNew = true;
    			
    			// iterate current assignments, if there is a match set flag to false
    			for (ApplicationAssignedInfo currentAssignment : Constants.getData().getAssignedApplications())
    			{
    				if (!newAssignment.packageName.equalsIgnoreCase(currentAssignment.packageName))
    					continue;
    				
    				// if the newAssignment has a newer version, continue
    				if (!AbstractLibrary.isInstalledSoftwareVersionCurrent(newAssignment.version, currentAssignment.version))
    					continue;
    				
    				// if the currentAssignment has a newer version, set flag to false
    				isNew = false;
    				break;
    			}
    			
    			
				// check if assignment is already installed
				boolean isInstalled = false;
				
				for (ApplicationInstalledInfo installedApp : listInstalledApps)
				{
					if (!newAssignment.packageName.equalsIgnoreCase(installedApp.packageName))
						continue;
					
					// check that installed version is greater or equal to the assigned version
					isInstalled = AbstractLibrary.isInstalledSoftwareVersionCurrent(installedApp.version, newAssignment.version);	
					
					break;
				}
    				
    				
    			// if assignment is new and not installed, increment counter
    			if (isNew && !isInstalled)
					newAssignmentsCount++;

    
    			// add mandatory apps to list if not installed
				if (!isInstalled && newAssignment.mandatory)
					listUninstalledMandatoryApps.add(newAssignment);
    		}
    		
    		
    		// update assignments
    		
    		// delete old entries and insert new list
			Constants.getData().deleteAssignedApplications();
			Constants.getData().insertAssignedApplications(listAssignedApplications);
			
			

			// attempt to download and silently install MANDATORY packages with manufacturer API
			for (ApplicationAssignedInfo mandatoryApp : listUninstalledMandatoryApps)
				ApplicationInstallation.install(mandatoryApp, true);
			
			
			// set notification of new assignments
			if (newAssignmentsCount > 0)
				Notifications.setNewApplicationsAvailable(newAssignmentsCount);
    	}
    	catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "importAssignedApplications", e);
		}
    }
    
    
    private static long getDirectoryBytes(File directory) 
    {
    	long bytes = 0;
    	
    	try
    	{
    		if (directory == null)
    			return bytes;
    		
    		// check if specified directory is a file
    		if (directory.isFile())
    			return directory.length();
    		
    		// iterate files in directory
    		File[] directoryList = directory.listFiles();
    		
    		if (directoryList != null)
    		{
		    	for (File file : directoryList) 
		    	{
		    		if (file == null)
		    			continue;
		    		
		    	    if (file.isFile()) 
		    	        bytes += file.length();
		
		    	    else
		    	    	bytes += getDirectoryBytes(file);
		    	}
    		}
    	}
    	catch (Exception e) 
 	    {
         	Constants.getLog().add(TAG, Type.Error, "getDirectoryBytes", e);
 	    }
    	
    	return bytes;
	}


    public void installShortcut(ShortcutInfo shortcut)
    {
    	try
    	{
			Intent shortcutIntent = new Intent();
			shortcutIntent.setAction(Intent.ACTION_VIEW);
	        shortcutIntent.setData(Uri.parse(shortcut.url));
	        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	
	        Intent intent = new Intent();
	        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
	        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcut.label);
	        intent.putExtra("duplicate", false);
	        
	        // add icon
	        byte[] icon = Base64.decode(shortcut.iconBase64, Base64.DEFAULT);
	        Bitmap bitmapIcon = BitmapFactory.decodeByteArray(icon, 0, icon.length);
	        
	        // scale bitmap to prevent off-centered or incorrectly sized icons
	        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmapIcon, 72, 72, true);
	        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, scaledBitmap);
	        
	        
	        // uninstall first to avoid duplicates
	        intent.setAction("com.android.launcher.action.UNINSTALL_SHORTCUT");
	        Constants.applicationContext.sendBroadcast(intent);
	        
	        // install
	        intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
	        Constants.applicationContext.sendBroadcast(intent);
    	}
    	catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "installShortcut", e);
    	}
    }
    
    
    public void removeShortcut(ShortcutInfo shortcut)
    {
    	try
    	{
    		Intent shortcutIntent = new Intent();
			shortcutIntent.setAction(Intent.ACTION_VIEW);
	        shortcutIntent.setData(Uri.parse(shortcut.url));
	        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	
	        Intent intent = new Intent();
	        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
	        
	        // uninstall first to avoid duplicates
	        intent.setAction("com.android.launcher.action.UNINSTALL_SHORTCUT");
	        Constants.applicationContext.sendBroadcast(intent);
    	}
    	catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "removeShortcut", e);
    	}
    }
    
    
    public void updateApplicationBytesUsed(long sessionSysID, long intervalBytesReceived, long intervalBytesSent)
    {
		ArrayList<String> lines = new ArrayList<String>();
        String wifiInterfaceName = "";
        
        try
        {
    		// get connected WiFi interface name
        	try
        	{
        		WifiManager wifiManager = (WifiManager) Constants.applicationContext.getSystemService(Context.WIFI_SERVICE);
        		wifiInterfaceName = Library.getWifiNetworkInterface(wifiManager).getName();
        	}
        	catch (Exception e)
        	{
        		Constants.getLog().add(TAG, Type.Warn, "Unable to retrieve the Wi-Fi network interface to update bytes used.");
        		return;
        	}
	   

        	// read data from interface file into an array list
        	FileReader fstream = new FileReader(Library.INTERFACE_FILE);

            if (fstream != null) 
            {
            	BufferedReader in = new BufferedReader(fstream, 500);
                String line;

                while ((line = in.readLine()) != null) 
                    lines.add(line);

                in.close();
                fstream.close();
            }
	     
	
	        
	        // read bytes sent/received totals from interface file
	        long bytesReceivedTotal = 0;
	        long bytesSentTotal = 0;
	        
	        for (String line : lines) 
	        {
	            Matcher matcher = LINE_PATTERN.matcher(line);
	
	            if (!matcher.matches())
	            	continue;
	            
	            String deviceName = matcher.group(1).trim();
	            
	            if (!deviceName.equals(wifiInterfaceName))
	            	continue;
	            
	            // add to totals
	        	bytesReceivedTotal += Long.parseLong(matcher.group(2));
	        	bytesSentTotal += Long.parseLong(matcher.group(10));
	        }
	        
	        
		    
		    // add values to current session once there is data
		    if (intervalBytesReceived > 0 && intervalBytesSent > 0)
		    {
		    	// calculate amounts to add
		        long diffbytesReceived = bytesReceivedTotal - intervalBytesReceived;
			    long diffbytesSent = bytesSentTotal - intervalBytesSent;
		    	
			    Constants.getData().addSessionNetworkTraffic(sessionSysID, diffbytesReceived, diffbytesSent);
		    }
			    
			    
		    // update internal interval values
		    Constants.getSession().setBytesReceived(bytesReceivedTotal);
		    Constants.getSession().setBytesSent(bytesSentTotal);
		    
		    Constants.getLog().add(TAG, Type.Verbose, "Updated bytes sent/received for Session ID " + String.valueOf(sessionSysID));
    	}
    	catch (Exception e) 
 	    {
         	Constants.getLog().add(TAG, Type.Error, "updateApplicationBytesUsed", e);
 	    }
    }
    
    
    public void requestDeviceAdministration()
    {
    	try
    	{
    		// launch tabhost activity which will prompt for access
			Intent intent = new Intent(Constants.applicationContext, TabHostActivity.class);
	        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        Constants.applicationContext.startActivity(intent);
    	}
    	catch (Exception e) 
 	    {
         	Constants.getLog().add(TAG, Type.Error, "requestDeviceAdministration", e);
 	    }
    }
    
    
    public void notifyApplicationsRequired()
    {
    	try
    	{
    		// clear notification
			Notifications.remove(Notifications.Type.NOTIFY_APPLICATIONS_REQUIRED);
			Notifications.remove(Notifications.Type.NOTIFY_NEW_APPLICATIONS_AVAILABLE);
			
			
    		Intent intent = new Intent(Constants.applicationContext, TabHostActivity.class);
	        intent.setAction(TabHostActivity.ACTION_NOTIFY_APPS_REQUIRED);
	        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        Constants.applicationContext.startActivity(intent);
    	}
    	catch (Exception e) 
 	    {
         	Constants.getLog().add(TAG, Type.Error, "notifyApplicationsRequired", e);
 	    }
    }
    
    
    public void viewAvailableApplications()
    {
    	try
    	{
    		// clear notifications
    		Notifications.remove(Notifications.Type.NOTIFY_APPLICATIONS_REQUIRED);
			Notifications.remove(Notifications.Type.NOTIFY_NEW_APPLICATIONS_AVAILABLE);
			
			
    		Intent intent = new Intent(Constants.applicationContext, TabHostActivity.class);
	        intent.setAction(TabHostActivity.ACTION_VIEW_AVAILABLE_APPS);
	        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        Constants.applicationContext.startActivity(intent);
    	}
    	catch (Exception e) 
 	    {
         	Constants.getLog().add(TAG, Type.Error, "notifyApplicationsRequired", e);
 	    }
    }
    
    
    public String encodeBase64(byte[] bytes)
    {
    	String result = "";
    	
    	try
    	{
    		result = Base64.encodeToString(bytes, Base64.DEFAULT);
    	}
    	catch (Exception e) 
 	    {
         	Constants.getLog().add(TAG, Type.Error, "encodeBase64", e);
 	    }
    	
    	
    	return result;
    }
    
    
    public boolean hasLocationAccess()
    {
    	boolean result = false;
    	
    	try
    	{
    		// check if application has access to location data
    		
    		LocationManager locationManager = (LocationManager)Constants.applicationContext.getSystemService(Context.LOCATION_SERVICE);

	        Criteria locationCritera = new Criteria();
	        locationCritera.setAccuracy(Criteria.ACCURACY_FINE);
	        locationCritera.setAltitudeRequired(false);
	        locationCritera.setBearingRequired(false);
	        locationCritera.setCostAllowed(true);
	        locationCritera.setPowerRequirement(Criteria.NO_REQUIREMENT);

	        String providerName = locationManager.getBestProvider(locationCritera, true);

	        if (providerName != null && locationManager.isProviderEnabled(providerName)) 
	            result = true;
    	}
    	catch (Exception e) 
 	    {
         	Constants.getLog().add(TAG, Type.Error, "hasLocationAccess", e);
 	    }
    	
    	
    	return result;
    }
    
    
    public void promptForLocationAccess()
    {
    	try
    	{
    		// remove notifications
			Notifications.remove(Notifications.Type.LOCATION_ACCESS_REQUIRED);
			
    		// create intent to prompt user to allow location access
	    	Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    	Constants.applicationContext.startActivity(intent);
    	}
    	catch (Exception e) 
 	    {
         	Constants.getLog().add(TAG, Type.Error, "promptForLocationAccess", e);
 	    }
    }
    	
    
    public void clearFailedPortalCredentialNotification()
    {
    	try
    	{
    		Notifications.remove(Notifications.Type.FAILED_ROVA_PORTAL_AUTHENTICATION);
    	}
    	catch (Exception e) 
 	    {
         	Constants.getLog().add(TAG, Type.Error, "clearFailedPortalCredentialNotification", e);
 	    }
    }
    
    
    public void requestNewGCMToken()
    {
    	try
    	{
    		Constants.getLog().add(TAG, Type.Debug, "Requesting new GCM registration token");
    		
        	Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
        	registrationIntent.putExtra("app", PendingIntent.getBroadcast(Constants.applicationContext, 0, new Intent(), 0)); 
        	registrationIntent.putExtra("sender", Constants.getData().getSetting(Settings.SET_GCMProjectID));
        	Constants.applicationContext.startService(registrationIntent);
    	}
    	catch (Exception e) 
 	    {
         	Constants.getLog().add(TAG, Type.Error, "requestNewGCMToken", e);
 	    }
    }
    
    
    public boolean isAppInForeground()
    {
    	boolean result = false;
    	
    	try
    	{
    		ActivityManager am = (ActivityManager) Constants.applicationContext.getSystemService(Context.ACTIVITY_SERVICE);
    		
    		// check if application is on top
    		for (RunningAppProcessInfo appProcess : am.getRunningAppProcesses()) 
		    { 
		    	if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND && 
		    			appProcess.processName.equalsIgnoreCase(Constants.applicationContext.getPackageName()))
		    	{
		    		result = true; 
		    		break;
		    	}
		    } 
    		
    		// return if app is not in foreground
    		if (!result)
    		{
    			Constants.getLog().add(TAG, Type.Verbose, "isAppInForeground - Process not in foreground");
    			return result;
    		}
    		
    		// reset flag for top activity check
    		else
    			result = false;
    		
			
			// check if the TabHostActivity is the top most activity
			for (RunningTaskInfo taskInfo : am.getRunningTasks(1))
			{
				if (taskInfo.topActivity == null)
					continue;
				
				// skip if top activity is the main activity
				if (taskInfo.topActivity.getClassName().equals(Connect_MainActivity.class.getName()))
					continue;
				

				// check if top activity is Tab Host
				if (taskInfo.topActivity.getClassName().equalsIgnoreCase(TabHostActivity.class.getName()))
				{
					result = true;
					break;
				}
			}
			
			Constants.getLog().add(TAG, Type.Verbose, "isAppInForeground - Top Activity check: " + String.valueOf(result));
    	}
    	catch (Exception e) 
 	    {
         	Constants.getLog().add(TAG, Type.Error, "isAppInForeground", e);
 	    }
    	
    	return result;
    }
    
    
    public List<String> getAppSignature()
    {
    	List<String> signatureList = new ArrayList<String>();
    	
    	try
    	{
	    	Signature[] sigs = Constants.applicationContext.getPackageManager().getPackageInfo(Constants.applicationContext.getPackageName(), PackageManager.GET_SIGNATURES).signatures;
	    	
	    	for (Signature sig : sigs)
	    		signatureList.add(String.valueOf(sig.hashCode()));
    	}
    	catch (Exception e) 
 	    {
         	Constants.getLog().add(TAG, Type.Error, "getAppSignature", e);
 	    }
    	
    	return signatureList;
    }
    
    
    public String getSamsungEnterpriseSdkVersion()
    {
    	String version = "";
    	
    	try
    	{
    		if (DeviceManagementSamsung.getCurrentSdkVersion() > 0.0)
    			version = Float.toString(DeviceManagementSamsung.getCurrentSdkVersion());
    	}
    	catch (Exception e) 
 	    {
         	Constants.getLog().add(TAG, Type.Error, "requestNewGCMToken", e);
 	    }
    	
    	return version;
    }
    
    public void testSamsungKnox()
    {
    	DeviceManagementSamsung.testActivateKnox();
    }
    
	// used to parse network interface file
    private static final Pattern LINE_PATTERN = Pattern.compile("^" + // start
            "(.*?):" + // the device name (group = 1)
            "\\s*" + // blanks
            "([0-9]+)" + // 1st number (group = 2) -> bytes received
            "\\s+" + // blanks
            "([0-9]+)" + // 2nd number (group = 3) -> packets received
            "\\s+" + // blanks
            "([0-9]+)" + // 3rd number (group = 4)
            "\\s+" + // blanks
            "([0-9]+)" + // 4th number (group = 5)
            "\\s+" + // blanks
            "([0-9]+)" + // 5th number (group = 6)
            "\\s+" + // blanks
            "([0-9]+)" + // 6th number (group = 7)
            "\\s+" + // blanks
            "([0-9]+)" + // 7th number (group = 8)
            "\\s+" + // blanks
            "([0-9]+)" + // 8th number (group = 9)
            "\\s+" + // blanks
            "([0-9]+)" + // 9th number (group = 10) -> bytes sent
            "\\s+" + // blanks
            "([0-9]+)" + // 10th number (group = 11) -> packets sent
            "\\s+" + // blanks
            "([0-9]+)" + // 11th number (group = 12)
            "\\s+" + // blanks
            "([0-9]+)" + // 12th number (group = 13)
            "\\s+" + // blanks
            "([0-9]+)" + // 13th number (group = 14)
            "\\s+" + // blanks
            "([0-9]+)" + // 14th number (group = 15)
            "\\s+" + // blanks
            "([0-9]+)" + // 15th number (group = 16)
            "\\s+" + // blanks
            "([0-9]+)" + // 16th number (group = 17)
            "$"); // end of the line  	

}
