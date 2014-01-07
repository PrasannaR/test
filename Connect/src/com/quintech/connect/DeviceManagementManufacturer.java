package com.quintech.connect;


import com.quintech.common.ApplicationAssignedInfo;

public abstract class DeviceManagementManufacturer 
{
	public abstract void setPolicySettings();
	public abstract boolean setStorageEncryption();
	public abstract boolean installAppLocalApk(String apkLocalFilePath);
	public abstract boolean installAppGooglePlay(String googlePlayAppUrl);
	public abstract void enableApp(ApplicationAssignedInfo app);
	public abstract boolean uninstallApp(String packageIdentifier);
	public abstract Boolean isInternalStorageEncrypted();
	public abstract Boolean isExternalStorageEncrypted();
	public abstract boolean enterpriseWipe();
}
