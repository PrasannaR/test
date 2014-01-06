package com.quintech.common;

public class DownloadQueueInfo 
{
	public int applicationsAssignedPortalSysID = 0;
	public long downloadID = -1;
	public boolean silentInstall = false;
	
	public DownloadQueueInfo()
	{
		
	}
	
	public DownloadQueueInfo(int applicationsAssignedPortalSysID, long downloadID, boolean silentInstall)
	{
		this.applicationsAssignedPortalSysID = applicationsAssignedPortalSysID;
		this.downloadID = downloadID;
		this.silentInstall = silentInstall;
	}
}
