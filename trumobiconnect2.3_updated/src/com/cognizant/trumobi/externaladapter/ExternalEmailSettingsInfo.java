package com.cognizant.trumobi.externaladapter;

import java.util.HashMap;

import com.cognizant.trumobi.externaladapter.ExternalPIMSettingsInfo.ExternalPIMAuthType;
import com.cognizant.trumobi.log.ExternalAdapterLog;
import com.quintech.rovacommon.EmailSettings;


/**
 *  FileName : ExternalEmailSettingsInfo
 * 
 *  Desc : 
 * 
 * 
 *  KeyCode 				Author				Date						Desc
 * 
 */
public class ExternalEmailSettingsInfo {
	
	
	public String EmpId;
	public String EmailAddress;
	public String Password;

	public ExternalPIMAuthType Authtype;
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
	
	HashMap<String,ExternalEmailSettingsInfo> externalEmailInfoMap = new HashMap<String,ExternalEmailSettingsInfo>();
	ExternalEmailSettingsInfo extEmailSettInfo;
	
	private static String TAG=ExternalEmailSettingsInfo.class.getName(); 
	
	public ExternalEmailSettingsInfo(){
		
	}
	
//	private void setRovaEmailSettings(EmailSettings rovaEmailSet)
//	{
//		ExternalAdapterLog.d(TAG, "===== In setRovaEmailSettings ====");
//		
//		extEmailSettInfo = new ExternalEmailSettingsInfo();
//		extEmailSettInfo.EmpId = rovaEmailSet.EmpId;
//		extEmailSettInfo.EmailAddress = rovaEmailSet.EmailAddress;
//		extEmailSettInfo.Password = rovaEmailSet.Password;
//
//		//	extEmailSettInfo.Authtype. = rovaEmailSet.Authtype.; // getValue of AuthType to be assigned to setValue of extEmailSett
//		
//		
//		extEmailSettInfo.SyncInterval = rovaEmailSet.SyncInterval;
//		extEmailSettInfo.SyncPeriod = rovaEmailSet.SyncPeriod;
//		extEmailSettInfo.Server = rovaEmailSet.Server;
//		
//		extEmailSettInfo.Domain = rovaEmailSet.Domain;
//		extEmailSettInfo.useSSL = rovaEmailSet.useSSL;
//		extEmailSettInfo.AlwaysSSL =rovaEmailSet.AlwaysSSL;
//		extEmailSettInfo.Port = rovaEmailSet.Port;
//		extEmailSettInfo.EmailCertificate =rovaEmailSet.EmailCertificate;
//		
//		extEmailSettInfo.bAllowEditEmailSettings = rovaEmailSet.bAllowEditEmailSettings;
//		extEmailSettInfo.bDisplayEmailSettings = rovaEmailSet.bDisplayEmailSettings;
//		extEmailSettInfo.bSetupAutomatic = rovaEmailSet.bSetupAutomatic;
//		
//		
//		
//		externalEmailInfoMap.put("emailsettings", extEmailSettInfo);
//		
//		ExternalAdapterLog.d(TAG, "===== setRovaEmailSettings email address ===="+extEmailSettInfo.EmailAddress);
//		
//		ExternalAdapterLog.d(TAG, "===== out setRovaEmailSettings ====");
//	}
//	
//	public ExternalEmailSettingsInfo getExternalEmailSettings(EmailSettings rovaEmSettings){
//		
//		ExternalAdapterLog.d(TAG, "===== In getExternalEmailSettings ====");
//		
//		ExternalEmailSettingsInfo emailSettInfo = null;
//		
//		
//		setRovaEmailSettings(rovaEmSettings);
//		
//	//	ExternalAdapterLog.d(TAG, "===== emailsettins email ===="+externalEmailInfoMap.get("emailsettings").EmailAddress);
//		
//	//	if(!externalEmailInfoMap.isEmpty())
//	//	{
//	//		ExternalAdapterLog.d(TAG, "===== emailSettInfo ===="+emailSettInfo.EmailAddress);
//			ExternalAdapterLog.d(TAG, "===== HashMap is not empty===="+externalEmailInfoMap.isEmpty());
//			emailSettInfo = externalEmailInfoMap.get("emailsettings");
//			ExternalAdapterLog.d(TAG, "===== emailSettInfo ===="+emailSettInfo.EmailAddress);
//	//	}	
//		ExternalAdapterLog.d(TAG, "===== out getExternalEmailSettings ====");
//		return emailSettInfo;
//		
//	}

}
