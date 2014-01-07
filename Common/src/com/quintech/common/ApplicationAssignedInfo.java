package com.quintech.common;


public class ApplicationAssignedInfo 
{
	public int portalSysID = -1;
	public String packageName = "";
	public String packageDisplayName = "";
	public String version = "";
	public String filePath = "";
	public boolean mandatory = false;
	public boolean isInstalled = false;
	
	public String getTempLocalFilename()
	{
		// if package name or version are not available, generate unique name
		if (packageName == null || version == null)
			return java.util.UUID.randomUUID().toString() + ".apk";;
		
		// add current time to create unique filename
		return packageName + "_" + version + "_" + String.valueOf(System.currentTimeMillis()) + ".apk";
	}
}
