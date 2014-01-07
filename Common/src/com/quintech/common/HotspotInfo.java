package com.quintech.common;

import com.quintech.common.ILog.Type;


public class HotspotInfo
{
	public int id = 0;
	public long directoriesPortalSysID = 0;
	public String ssid = "";
	public String displaySSID = "";
	public Boolean loginRequired = true;
	public Boolean autoLogin = false;
	public Boolean forceConnect = false;
	public String aggregatorName = "";
	public String operatorName = "";
	public String decorationName = "";
	public String loginURL = "";
	public String probeURL = "";
	public float version = 0.0F;
	public String password = "";
	public int wepKeyIndex = 0;
	
	private DirectoryInfo directoryInfo = null;
	
	public DirectoryInfo getDirectoryInfo()
	{
		try
		{
			// get directoryInfo object
			if (directoriesPortalSysID > 0 && directoryInfo == null)
				directoryInfo = AbstractConstants.data.getDirectoryInfo(directoriesPortalSysID);
		}
		catch (Exception e)
	    {
			AbstractConstants.log.add("HotspotInfo", Type.Error, "getDirectoryInfo", e);
	    }
		
		return directoryInfo;
	}
}
