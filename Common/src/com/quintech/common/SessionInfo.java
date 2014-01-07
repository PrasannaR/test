package com.quintech.common;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.quintech.common.ILog.Type;


public class SessionInfo 
{
	public static String TAG = "SessionInfo";
	public int sysID = 0;
	public String bssid = "";
	public String ssid = "";
	public String startTime = "";
	public String endTime = "";
	public long octetsDownloaded = 0;
	public long octetsUploaded = 0;
	public boolean isJailBroken = false;
	public String latitude = "";
	public String longitude = "";
	public String timeZone = "";
	public String wisprLoginURL = "";
	public String wisprLogoffURL = "";
	public String wisprbwUserGroup = "";
	public String wisprLocationName = "";
	public String wisprLocationID = "";
	public String deviceModelNumber = "";
	public String deviceType = "";
	public String osType = "";
	public String osVersion = "";
	public String clientVersion = "";
	

	
	
	public long getElapsedTimeInSeconds()
	{
		long elapsedTime = 0;
		
		try
		{
			// parse string values
			DateFormat dateFormat = new SimpleDateFormat(AbstractLibrary.DATE_TIME_FORMAT);
			
			Date dateStart = dateFormat.parse(startTime);
			Date dateEnd = dateFormat.parse(endTime);
			
			// calculate difference in seconds
			elapsedTime = (dateEnd.getTime() / 1000) - (dateStart.getTime() / 1000);
		}
		catch (Exception e)
	    {
			AbstractConstants.log.add(TAG, Type.Error, "getElapsedTime", e);
	    }
		
		return elapsedTime;
	}
	
	
	public double getAverageDownloadRate()
	{
		double rate = 0.0;
		
		try
		{
			rate = Double.valueOf(octetsDownloaded) / Double.valueOf(getElapsedTimeInSeconds());
		}
		catch (Exception e)
	    {
			AbstractConstants.log.add(TAG, Type.Error, "getAverageDownloadRate", e);
	    }
		
		return rate;
	}
	
	
	public double getAverageUploadRate()
	{
		double rate = 0.0;
		
		try
		{
			rate = Double.valueOf(octetsUploaded) / Double.valueOf(getElapsedTimeInSeconds());
		}
		catch (Exception e)
	    {
			AbstractConstants.log.add(TAG, Type.Error, "getAverageUploadRate", e);
	    }
		
		return rate;
	}
}
