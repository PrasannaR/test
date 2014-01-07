package com.quintech.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;


import com.quintech.common.AbstractConstants.BuildType;
import com.quintech.common.ILog.Type;


public abstract class AbstractLibrary 
{
	private static String TAG = "AbstractLibrary";
	public static String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static String DATE_FORMAT = "yyyy-MM-dd";
	public static String TIME_FORMAT = "HH.mm.ss.SSS";
	
	public enum DeviceType
	{
		VERIZON_WIRELESS_NONE,
		VERIZON_WIRELESS_3G ,
		VERIZON_WIRELESS_3G_INTERNATIONAL,
		VERIZON_WIRELESS_3G_4G,
		VERIZON_WIRELESS_4G
	}
	
	public abstract BuildType getDefaultBuildType();
	public abstract String getWisprAgentString();
	public abstract String getDeviceManufacturer();
	public abstract String getOSVersion();
	public abstract String getSdkVersion();
	public abstract String getOSType();
	public abstract int getPlatformId();
	public abstract String getClientVersionName();
	public abstract String getDeviceMacAddress();
	public abstract String getESN();
	public abstract String getMSISDN();
	public abstract String getDeviceIMSI();
	public abstract String getDeviceICCID();
	public abstract Location getCurrentLocation();
	public abstract void promptForCredentials();
	public abstract DeviceType getDeviceType();
	public abstract String getDeviceModelNumber();
	public abstract float getDeviceCapacityGB(int storageType, int reportType);
	public abstract int getBatteryLevelPercentage();
	public abstract String getCellularTechnology();
	public abstract String getHomeCarrierNetwork();
	public abstract String getCurrentMobileCountryCode();
	public abstract boolean isConnectedToWiFi();
	public abstract String getConnectedSSID();
	public abstract String getConnectedBSSID();
	public abstract boolean isJailBroken();
	public abstract List<ApplicationInstalledInfo> getInstalledApplications(boolean includeSystemPackages);
	public abstract void importAssignedApplications(List<ApplicationAssignedInfo> listAssignedApplications);
	public abstract void updateApplicationBytesUsed(long sessionSysID, long intervalBytesReceived, long intervalBytesSent);
	public abstract void deleteLogFiles();
	public abstract void refreshUI();
	public abstract String getLogDirectoryName();
	public abstract String getClientPackageName();
	public abstract String getClientPackageDisplayName();
	public abstract boolean isDebugBuild();
	public abstract boolean isWiFiConnected();
	public abstract File getWriteableDirectory();
	public abstract String readFileTostring(File file);
	public abstract void installShortcut(ShortcutInfo shortcut);
	public abstract void removeShortcut(ShortcutInfo shortcut);
	public abstract void requestDeviceAdministration();
	public abstract void notifyApplicationsRequired();
	public abstract void viewAvailableApplications();
	public abstract String encodeBase64(byte[] bytes);
	public abstract boolean hasLocationAccess();
	public abstract void promptForLocationAccess();
	public abstract void clearFailedPortalCredentialNotification();
	public abstract boolean isAppInForeground();
	public abstract void requestNewGCMToken();
	public abstract String getSamsungEnterpriseSdkVersion();
	public abstract void testSamsungKnox();
	public abstract boolean isRoaming();
	
	public static boolean isInstalledSoftwareVersionCurrent(String currentVersion, String assignedVersion)
	{
		boolean isValid = false;
		
		try
		{
			// determine if installed version is current
			
			
			// if the assigned version is not specified, return true
			if (assignedVersion == null || assignedVersion.equals(""))
				return true;

			// if current version is not specified, return false
			if (currentVersion == null || currentVersion.equals(""))
				return false;
			
			
			// if both versions are null or empty, return true
			if ((currentVersion == null || currentVersion.equals("")) &&
					(assignedVersion == null || assignedVersion.equals("")))
				return true;
	
			// check for an exact match
			if (currentVersion.equalsIgnoreCase(assignedVersion))
				return true;
			
			// if versions do not contain any periods, an exact match was required
			if (!currentVersion.contains(".") && !assignedVersion.contains("."))
				return false;
	
			// check if the current version is greater than the assigned version
			String[] currentProperties = currentVersion.split("\\.");
			String[] assignedProperties = assignedVersion.split("\\.");
	
	
			// iterate version properties in order of significant parts
			isValid = true;
	
			for (int i = 0; i < assignedProperties.length; i++)
			{
				try
				{
					// check length of current version, return false if assigned version is longer
					if (i > currentProperties.length)
					{
						isValid = false;
						break;
					}
					
					// if current significant version part is greater than the assigned version, it is valid
					if (Integer.valueOf(currentProperties[i]) > Integer.valueOf(assignedProperties[i]))
						break;
						
					// if the assigned significant version part is greater than the current version, it is not valid
					else if (Integer.valueOf(currentProperties[i]) < Integer.valueOf(assignedProperties[i]))
						isValid = false;
					
					// continue processing
				}
				catch (Exception e) { }
			}
		}
		catch (Exception e)
    	{
    		AbstractConstants.log.add(TAG, Type.Error, "isInstalledSoftwareVersionCurrent", e);
    	}

		return isValid;
	}
	
	
	public static void importShortcuts(List<ShortcutInfo> postedShortcuts)
    {
    	try
    	{
    		List<ShortcutInfo> installedShortcuts = AbstractConstants.data.getInstalledShortcuts();
    		
    		
    		// iterate installed shortcuts
    		for (ShortcutInfo installedShortcut : installedShortcuts)
    		{
    			// uninstall and delete item if not in the posted list
    			
    			// Note: we're are purposefully not comparing the icon to avoid extra processing on the device
    			// and traffic to the portal
    			boolean isPosted = false;
    			
    			for (ShortcutInfo postedShortcut : postedShortcuts)
    			{
    				if (installedShortcut.portalSysID != postedShortcut.portalSysID) 
    					continue;
    				
    				if (!installedShortcut.label.equals(postedShortcut.label))
    					continue;
    				
    				if (!installedShortcut.url.equalsIgnoreCase(postedShortcut.url))
    					continue;
    				
    				isPosted = true;
    				break;
    			}
    			
    			if (!isPosted)
    			{
    				AbstractConstants.data.deleteInstalledShortcut(installedShortcut);
    				AbstractConstants.library.removeShortcut(installedShortcut);
    			}
    		}
			
    		
			// iterate posted shortcuts
    		for (ShortcutInfo postedShortcut : postedShortcuts)
    		{
    			// if it is NOT already installed, install and insert item
    			boolean isInstalled = false;
    			
    			for (ShortcutInfo installedShortcut : installedShortcuts)
    			{
    				if (installedShortcut.portalSysID != postedShortcut.portalSysID) 
    					continue;
    				
    				if (!installedShortcut.label.equals(postedShortcut.label))
    					continue;
    				
    				if (!installedShortcut.url.equalsIgnoreCase(postedShortcut.url))
    					continue;
    				
    				
    				isInstalled = true;
    				break;
    			}
    			
    			if (!isInstalled)
    			{
    				// download icon and store as base64 string
    				if (postedShortcut.iconExternalUrl != null && postedShortcut.iconExternalUrl != "")
    				{
    					java.util.Date date = new java.util.Date();
    					File tempFileDirectory = null;
    					
    					try
    					{
	    					tempFileDirectory = new File(AbstractConstants.library.getWriteableDirectory(), "/" + String.valueOf(date.getTime() + "/"));
	    					
	    					File fileIcon = downloadFile(postedShortcut.iconExternalUrl, tempFileDirectory, String.valueOf(postedShortcut.portalSysID));
	    					
	    					if (fileIcon != null && fileIcon.exists())
	    					{
	    						FileInputStream inputStream = new FileInputStream(fileIcon);
	    						ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    						byte[] b = new byte[1024];
	    						int bytesRead = 0;
	    						
	    						while ((bytesRead = inputStream.read(b)) != -1) 
	    						   bos.write(b, 0, bytesRead);
	    						
	    						inputStream.close();
	    						postedShortcut.iconBase64 = AbstractConstants.library.encodeBase64(bos.toByteArray());
	    					}
    					}
    					catch (Exception e)
    					{
    						AbstractConstants.log.add(TAG, Type.Error, "Problem downloading shortcut icon file", e);
    					}
    					finally
    					{
    						// delete all files in temp directory
    						if (tempFileDirectory != null && tempFileDirectory.exists())
    						{
    							for (File file : tempFileDirectory.listFiles())
    								file.delete();
    							
    							// delete temp directory
    							tempFileDirectory.delete();
    						}
    					}
    				}
    				
    				AbstractConstants.data.insertInstalledShortcut(postedShortcut);
    				AbstractConstants.library.installShortcut(postedShortcut);
    			}
    		}
    	}
    	catch (Exception e)
    	{
    		AbstractConstants.log.add(TAG, Type.Error, "importShortcuts", e);
    	}
    }
	
	
	public static File downloadFile(String downloadUrl, File targetFileDirectory, String targetFileName)
	{
		File downloadedFile = null;
		
		try 
		{
			AbstractConstants.log.add(TAG, Type.Debug, "Downloading file: " + downloadUrl);
			
			// set file to be downloaded
			URL url = new URL(downloadUrl);

			if (url.getProtocol().compareToIgnoreCase("https") == 0)
			{
				return downloadFileHttps(downloadUrl, targetFileDirectory, targetFileName);
			}
			else
				return downloadFileHttp(downloadUrl, targetFileDirectory, targetFileName);
						
		}
		catch (Exception e) 
		{
			// set return variable to null
			downloadedFile = null;
			
			// log message
			AbstractConstants.log.add(TAG, Type.Error, "downloadFile", e);
		}

		return downloadedFile;
	}
	
	
	private static File downloadFileHttp(String downloadUrl, File targetFileDirectory, String targetFileName)
	{
		
		File downloadedFile = null;
		
		try 
		{
			AbstractConstants.log.add(TAG, Type.Debug, "HTTP Download file: " + downloadUrl);
			
			// set file to be downloaded
			URL url = new URL(downloadUrl);
	
			// open connection
			HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);
			urlConnection.connect();
	
			// create target directory if necessary
			if (!targetFileDirectory.exists())
				targetFileDirectory.mkdir();
			
			
			// set target file path
			downloadedFile = new File(targetFileDirectory, targetFileName);
	
			// set streams
			FileOutputStream fileOutput = new FileOutputStream(downloadedFile);
			InputStream inputStream = urlConnection.getInputStream();
	
			// write buffer contents to file
			byte[] buffer = new byte[1024];
			int bufferLength = 0; 
	
			while ( (bufferLength = inputStream.read(buffer)) > 0 ) 
			{
				fileOutput.write(buffer, 0, bufferLength);
			}
	
			// close streams
			fileOutput.close();
			inputStream.close();
			
			AbstractConstants.log.add(TAG, Type.Debug, "Completed file download: " + downloadUrl + "Response Code: " + urlConnection.getResponseCode());
		} 
		catch (java.io.FileNotFoundException e)
		{
			// set return variable to null
			downloadedFile = null;
			
			// log message
			AbstractConstants.log.add(TAG, Type.Warn, "File not found: " + downloadUrl);
		}
		catch (Exception e) 
		{
			// set return variable to null
			downloadedFile = null;
			
			// log message
			AbstractConstants.log.add(TAG, Type.Error, "downloadFileHttp", e);
		}
	
		return downloadedFile;
	}
	
	
	private static File downloadFileHttps(String downloadUrl, File targetFileDirectory, String targetFileName)
	{
		File downloadedFile = null;
		
		try 
		{
			AbstractConstants.log.add(TAG, Type.Debug, "HTTPS Download file: " + downloadUrl);
			
			// set file to be downloaded
			URL url = new URL(downloadUrl);
	
			// open connection
			HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
	
			// create target directory if necessary
			if (!targetFileDirectory.exists())
				targetFileDirectory.mkdir();
			
			
			// set target file path
			downloadedFile = new File(targetFileDirectory, targetFileName);
	
			// set streams
			FileOutputStream fileOutput = new FileOutputStream(downloadedFile);
			InputStream inputStream = urlConnection.getInputStream();
	
			// write buffer contents to file
			byte[] buffer = new byte[1024];
			int bufferLength = 0; 
	
			while ( (bufferLength = inputStream.read(buffer)) > 0 ) 
			{
				fileOutput.write(buffer, 0, bufferLength);
			}
	
			// close streams
			fileOutput.close();
			inputStream.close();
			
			AbstractConstants.log.add(TAG, Type.Debug, "Completed file download: " + downloadUrl + "Response Code: " + urlConnection.getResponseCode());
		} 
		catch (java.io.FileNotFoundException e)
		{
			// set return variable to null
			downloadedFile = null;
			
			// log message
			AbstractConstants.log.add(TAG, Type.Warn, "File not found: " + downloadUrl);
		}
		catch (Exception e) 
		{
			// set return variable to null
			downloadedFile = null;
			
			// log message
			AbstractConstants.log.add(TAG, Type.Error, "downloadFileHttps", e);
		}
	
		return downloadedFile;
	}

	
	public static String getDateTimeGMT(String formatString)
	{
		String strDate = "";
		
		try
		{
			SimpleDateFormat dateFormatGmt = new SimpleDateFormat(formatString);
    		dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
    		
    		Calendar cal = Calendar.getInstance();
			
    		strDate = dateFormatGmt.format(cal.getTime());
		}
		catch (Exception e)
    	{
			AbstractConstants.log.add(TAG, Type.Error, "getDateTimeGMT", e);
    	}
    	
		return strDate;
	}
	
	
	public static String formatDateTimeStringForSOAP(String strDateTime)
	{
		String strReturn = "";
		
		try
		{
			// replace space between date and time with a "T"
			strReturn = strDateTime.replace(" ", "T");
		}
		catch (Exception e)
    	{
			AbstractConstants.log.add(TAG, Type.Error, "formatDateTimeStringForSOAP", e);
    	}
    	
		return strReturn;
	}
	
	
	public static String formatForSOAP(Object value)
	{
		String strReturn = "";
		
		try
		{
			if (value == null)
				strReturn = "";
			
			else
			{
				// convert to string
				strReturn = String.valueOf(value);
				
				// escape special XML characters
				strReturn = strReturn.replace("\"", "&quot;");
				strReturn = strReturn.replace("&", "&amp;");
				strReturn = strReturn.replace("'", "&apos;");
				strReturn = strReturn.replace("<", "&lt;");
				strReturn = strReturn.replace(">", "&gt;");
			}
		}
		catch (Exception e)
    	{
			AbstractConstants.log.add(TAG, Type.Error, "formatStringForSOAP", e);
    	}
    	
		return strReturn;
	}
	

	public static int getGMTOffSet()
	{
		int gmtOffSet = 0;
		
		try
		{
			TimeZone z = Calendar.getInstance().getTimeZone();
		    gmtOffSet = z.getRawOffset() / 1000 / 60 / 60;
		}
		catch (Exception e) 
		{
			AbstractConstants.log.add(TAG, Type.Error, "getGMTOffSet", e);
		}
		
		return gmtOffSet;
	}
	
	
	public static int byteArrayToInt(byte[] arr, int offset) 
	{
		try
		{
			if (arr == null || arr.length - offset < 4)
				return -1;
	 
		    int r0 = (arr[offset] & 0xFF) << 24;
		    int r1 = (arr[offset + 1] & 0xFF) << 16;
		    int r2 = (arr[offset + 2] & 0xFF) << 8;
		    int r3 = arr[offset + 3] & 0xFF;
		    
		    return r0 + r1 + r2 + r3;
		} 
	    catch (Exception e) 
	    {
	    	AbstractConstants.log.add(TAG, Type.Error, "byteArrayToInt", e);
	    }
	    
	    return -1;
	}
	
	
    public static <T extends Enum<T>> String[] enumNameToStringArray(T[] values) 
    {   
        int i = 0;   
        String[] result = null;
        
        try
        {
	        result = new String[values.length];  
	        
	        for (T value: values) 
	        {   
	            result[i++] = value.name();   
	        }   
        }
        catch (Exception e)
        {
        	AbstractConstants.log.add(TAG, Type.Error, "enumNameToStringArray", e);
        }
        
        return result;   
    }       

    
    public static String convertToHex(byte[] data) 
    {         
    	StringBuffer buffer = new StringBuffer();     
    	
    	
    	try
    	{
	    	for (int i = 0; i < data.length; i++) 
	    	{             
	    		int halfbyte = (data[i] >>> 4) & 0x0F;            
	    		int two_halfs = 0;            
	    		do 
	    		{                 
	    			if ((0 <= halfbyte) && (halfbyte <= 9))                     
	    				buffer.append((char) ('0' + halfbyte));                
	    			else                     
	    				buffer.append((char) ('a' + (halfbyte - 10)));                
	    			halfbyte = data[i] & 0x0F;            
	    		} 
	    		while(two_halfs++ < 1);        
	    	}         
    	}
    	catch (Exception e) 
		{
			AbstractConstants.log.add(TAG, Type.Error, "convertToHex", e);
		}
    	
    	return buffer.toString();    
    }
}
