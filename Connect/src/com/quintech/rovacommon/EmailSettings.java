package com.quintech.rovacommon;

import com.quintech.rovacommon.PIMSettingsInfo.PIMAuthType;

public class EmailSettings 
{

	public String EmpId;
	public String EmailAddress;
	public String Password;

	public PIMAuthType Authtype;
	public int SyncInterval;
	public int SyncPeriod;
	public String Server;
	public String Domain;
	public Boolean useSSL;
	public Boolean AlwaysSSL;
	public String Port;
	public String EmailCertificate;
	
	public Boolean bSetupAutomatic;
	public Boolean bDisplayEmailSettings;
	public Boolean bAllowEditEmailSettings;
}
