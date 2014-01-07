package com.cognizant.trumobi.externaladapter;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;

import com.cognizant.trumobi.externaladapter.ExternalPIMSettingsInfo.ExternalPIMAuthType;
import com.cognizant.trumobi.externaladapter.ExternalPIMSettingsInfo.ExternalPasswordExpiration;
import com.cognizant.trumobi.externaladapter.ExternalPIMSettingsInfo.ExternalPasswordLockTime;
import com.cognizant.trumobi.externaladapter.ExternalPIMSettingsInfo.ExternalPasswordType;
import com.cognizant.trumobi.externaladapter.ExternalPIMSettingsInfo.ExternalSecurityResolution;
import com.cognizant.trumobi.log.ExternalAdapterLog;
import com.quintech.rovacommon.DeviceInfo;
import com.quintech.rovacommon.EmailSettings;
import com.quintech.rovacommon.PIMSettingsInfo;
import com.quintech.rovacommon.PIMSettingsInfo.PIMAuthType;
import com.quintech.rovacommon.Registrationlistner;
import com.quintech.rovacommon.SecurityProfileInfo;
import com.quintech.rovacommon.VPNDetailInfo;
import com.quintech.rovacommon.VPNSettingsInfo;
import com.quintech.rovacommon.rovashared;

/**
 *  FileName : ExternalAdapterListener
 * 
 *  Desc : 
 * 
 * 
 *  KeyCode 				Author				Date						Desc
 * 	NEW_ROVA_SDK								22-Oct-2013					Udpated with new ROVA SDK.
 */

public class ExternalAdapterRegistrationClass {
	
	
	static Context mExtContext;
	static ExternalAdapterRegistrationClass mExtnalAdapRegn;
	static rovashared mRova;
	ExternalAdapterListener mExtnalCallBack;
	
	DeviceInfo rovaDeviceInfo;
	EmailSettings rovaEmailSettings;
	PIMSettingsInfo rovaPIMSettingsInfo;
	VPNSettingsInfo rovaVPNSettingsInfo;
	VPNDetailInfo rovaVPNDetailInfo;
	SecurityProfileInfo rovaSecurityProfInfo;
	PIMAuthType rovaPIMAuthType;
	Map<String,String> rovaProxyInfo; //NEW_ROVA_SDK
	
	
	ExternalEmailSettingsInfo extEmailSettInfo;
	ExternalSecurityProfInfo extSecProfInfo;
	ExternalVPNDetailsInfo extVPNDetailsInfo;
	ExternalVPNSettingsInfo extVPNSettInfo;
	ExternalPIMSettingsInfo extPIMSettInfo;
	ExternalDeviceInfo extDeviceInfo;
	
	private static boolean bAllRovaClassInstiated = false;
	
	private static String TAG=ExternalAdapterRegistrationClass.class.getName();
	
	private ExternalAdapterRegistrationClass(){

		initializeRova();
	}	
	
	
	/**
	 *  getInstance()
	 *  
	 *  This method ,returns the instance of ExternalAdapterClass
	 *  
	 *  Input Parameter - context
	 *  Return parameter - instance of externalAdapterClass
	 */
	// 11Dec2013 - Added synchronized keyword
	public static synchronized ExternalAdapterRegistrationClass getInstance(Context mContext){
		ExternalAdapterLog.d(TAG, "=== In getInstance ===");
		if(mExtnalAdapRegn == null){
			ExternalAdapterLog.d(TAG, "------=== mExtnalAdapRegn NULL ===");
			mExtContext = mContext;
			mExtnalAdapRegn =  new ExternalAdapterRegistrationClass();
		}
		ExternalAdapterLog.d(TAG, "-------=== mExtnalAdapRegn NOT NULL ===");
		return mExtnalAdapRegn;
	}
	
	
	public static void setInstance() {
		mExtnalAdapRegn = null;
	}
	
	/**
	 *  isExternalAdapterRegistered()
	 *  
	 *  This method ,checks the device is registered already or not.
	 *  internally calls 3rd party isRegistered method.
	 *  
	 *  Input Parameter - void
	 *  Return parameter - boolean , true - registered ; false - not registered
	 */
	public boolean isExternalAdapterRegistered(){
		boolean bDeviceRegistered = false;

		if(mRova != null)
			bDeviceRegistered = mRova.isRegistered();
		
		return bDeviceRegistered;
		
	}

	/**
	 *  StartRegistrationExternalAdapter()
	 *  
	 *  This method registers with its callback
	 *  internally calls 3rd party StartRegistration method.
	 *  
	 *  Input Parameter -
	 *  i.HashMap<String,String> - UserDetail (Username / password) in keyvalue pair.
	 *  ii.ExternalAdapterListener callback - Listener with two methods.
	 *  
	 *  Return parameter - void
	 */
	public void StartRegistrationExternalAdapter(HashMap<String,String> userDetails,
												 ExternalAdapterListener adapCallBack)
	{
		ExternalAdapterLog.d(TAG, "StartRegistrationExternalAdapter============");
		mExtnalCallBack = adapCallBack;
		mRova.StartRegistration(userDetails, rovaCallBack);
		
	}
	
	
	/**
	 *  ROVA Registration call back listener
	 * 
	 */
	Registrationlistner rovaCallBack = new Registrationlistner() {
		
		public boolean onRegistered(boolean bRegistered) {
			// TODO Auto-generated method stub
			
			ExternalAdapterLog.d(TAG, "onRegistered ROVA ============");
			mExtnalCallBack.onExternalAdapterRegistered(bRegistered);
			return false;
		}
		
		@Override
		public void OnFailed(String error, int errorCode) {
			// TODO Auto-generated method stub
			mExtnalCallBack.onExternalAdapterFailed(error, errorCode);
		}

		@Override
		public boolean onRegistered(Message msg) {
			ExternalAdapterLog.d(TAG, "onRegistered ROVA ============");
			Bundle data = msg.getData();
			boolean bRegistered = data.getBoolean("Result");
			mExtnalCallBack.onExternalAdapterRegistered(bRegistered);
			return false;
		}
	};
	

	/**
	 *  isAllRegisteredInformationAvailable()
	 *  
	 *  This method checks the availability of all registered information 
	 *  internally calls 3rd party GetRegistration method.
	 *  
	 *  Input Parameter -
	 *  
	 *  Return parameter - boolean true / false
	 */
	public boolean isAllRegisteredInformationAvailable(){
		
		boolean bRet = false;
	
		
		if( isEmailSettingsInfoAvailble() || isPIMSettingsInfoAvailable() || isVPNSettingsInfoAvailable() || isPIMAuthTypeInfoAvailable() )
		{
			ExternalAdapterLog.d(TAG,"**** All INFO AVAILABLE *****");
			bRet = true;
			bAllRovaClassInstiated = true;
			return bRet;
		}
		else {
			bRet = false;
			bAllRovaClassInstiated = false;
			return bRet;
		}
		

	}
	

	public void initiateExternalToFetchAllSettingsInfo(){
		
		/*
		 *  This method has to be exposed by ROVA.
		 *  Pending from ROVA side.
		 *  
		 *  This method has to be called after TruMobi LOGIN authentication.
		 *  
		 *  Upon invoking this method , ROVA would send out notification
		 *  "SETTINGS_INFORMATION_FETCH_COMPLETE_EVENT" through BROADCAST INTENT.
		 *  On receiving this event in broadCastReceiver ,
		 *  isAllRegisteredInformationAvailable() method should be called 
		 *  to check the availability of all info.If not try it for 5 times 
		 *  and send message to Server.
		 *    
		 */
		ExternalAdapterLog.d(TAG, "*********** Inside Receiver ************");
		mRova.initiateToFetchAllSettingsInfo();
	}

	public void startLoginExternalAdapter(){
		
		/*
		 *  This method has to be exposed by ROVA.
		 *  Pending from ROVA side.
		 *  
		 *  This method has to be called after TruMobi LOGIN authentication.
		 *  ROVA would send LOGIN_COMPELTE_EVENT through BROADCAST INTENT.    
		 */
		
		mRova.startEmaasLogin();
		
	}
	
	public  ExternalDeviceInfo getExternalDeviceInformation(){
	
		
		
	//	 devinfo = (ExternalAdapterDeviceInfo) mRova.GetRegistrationInformation().deviceInfo;
		
		//devinfo.getDeviceInfo()
		
		getRovaDeviceInfo();
		
		return extDeviceInfo;

	}
	
	
	/**
	 *  getExternalSecurityProfInfo()
	 *  
	 *  This method returns SecurityProf information 
	 *  internally calls 3rd party SecurityProfileSettings method.
	 *  
	 *  Input Parameter -
	 *  
	 *  Return parameter - ExternalSecurityProfInfo
	 */
	public ExternalSecurityProfInfo getExternalSecurityProfInfo(){
		
		getRovaSecurityProfileSettings();
		
		return extSecProfInfo;
		
	}
	
	
	/**
	 *  getExternalEmailSettingsInfo()
	 *  
	 *  This method returns Emailsettings information 
	 *  internally calls 3rd party EmailSettings method.
	 *  
	 *  Input Parameter -
	 *  
	 *  Return parameter - ExternalEmailSettingsInfo
	 */
	public ExternalEmailSettingsInfo getExternalEmailSettingsInfo(){
		
		ExternalAdapterLog.d(TAG, "getExternalEmailSettingsInfo============");
				
		getRovaEmailSettings();

		return extEmailSettInfo;
	}
	

	/**
	 *  getExternalPIMSettingsInfo()
	 *  
	 *  This method returns PIMSettings information 
	 *  internally calls 3rd party PIMSettings method.
	 *  
	 *  Input Parameter -
	 *  
	 *  Return parameter - ExternalPIMSettingsInfo
	 */
	public ExternalPIMSettingsInfo getExternalPIMSettingsInfo(){
	
		getPIMSettingsInfo();
		
		return extPIMSettInfo;
	}
	
	
	/**
	 *  getExternalVPNSettingsInfo()
	 *  
	 *  This method returns VPNSettings information 
	 *  internally calls 3rd party VPNSettings method.
	 *  
	 *  Input Parameter -
	 *  
	 *  Return parameter - ExternalVPNSettingsInfo
	 */
	
	public ExternalVPNSettingsInfo getExternalVPNSettingsInfo(){
		
		
		//getRovaVPNSettingsInfo(rovaVPNSettingsInfo);
		getRovaVPNSettingsInfo();
		
		return extVPNSettInfo;
	}
	
	
	/**
	 *  getExternalVPNDetailsInfo()
	 *  
	 *  This method returns VPNDetails information 
	 *  internally calls 3rd party VPNDetails method.
	 *  
	 *  Input Parameter -
	 *  
	 *  Return parameter - ExternalVPNDetailsInfo
	 */
	
	public ExternalVPNDetailsInfo getExternalVPNDetailsInfo(){
		
		//getRovaVPNDetailsInfo(rovaVPNDetailInfo);
		
		getRovaVPNDetailsInfo();
		return extVPNDetailsInfo;
	}
	
	//NEW_ROVA_SDK
	/**
	 *  getExternalValidCheckSum()
	 *  
	 *  This method checks the validity of checksum for the entered string value. 
	 * 	
	 *  Input parameter - String.
	 *  
	 *  Return parameter - boolean.
	 * 
	 */
	public boolean getExternalValidCheckSum(String sCheckSum){
		
		boolean bExtValidCheck = true;
		
		bExtValidCheck = mRova.validateCheckSum(sCheckSum);
		
		return bExtValidCheck;
	}
	//NEW_ROVA_SDK
	
	
	//NEW_ROVA_SDK
    /**
    *  getExternalProxySettingsInfo()
    * 
     *  This method returns proxy settings info from external 3rd party
    *  
     *  Return parameter - HashMap values from 3rd party.
    * 
     */
    public Map<String,String> getExternalProxySettingsInfo(){
           
           Map<String,String> extProxySettInfo = new HashMap<String, String>();
           
           extProxySettInfo = rovaProxyInfo;
           extProxySettInfo.put("ProxyServerName", rovaProxyInfo.get("ProxyServerName"));
           extProxySettInfo.put("ProxyServerPortNumber", rovaProxyInfo.get("ProxyServerPortNumber"));
           return extProxySettInfo;
    }
    //NEW_ROVA_SDK

	
	
	/** 
	 *  Internal getRovaEmailSettings() method.
	 *  This private method is used to retrieve
	 *  EmailSettingsInfo information.
	 * 
	 */
	private void getRovaEmailSettings()
	{
		ExternalAdapterLog.d(TAG, "===== In getRovaEmailSettings ====");

		
		extEmailSettInfo = new ExternalEmailSettingsInfo();
		if(mRova == null){
			ExternalAdapterLog.d(TAG,"===== mRova NULL --- getRovaEmailSettings");
			initializeRova();
		}

		
	
		try{  

			extEmailSettInfo.EmpId = rovaEmailSettings.EmpId;
			extEmailSettInfo.EmailAddress = rovaEmailSettings.EmailAddress;
			//ExternalAdapterLog.d(TAG, "===== extEmailSettInfo ===="+rovaEmailSettings.EmailAddress);
			//ExternalAdapterLog.d(TAG, "===== extEmailSettInfo ===="+extEmailSettInfo.EmailAddress);
			extEmailSettInfo.Password = rovaEmailSettings.Password;
	
	
			//ExternalAdapterLog.d(TAG, "===== getRovaEmailSettings auth value ===="+rovaEmailSettings.Authtype.getValue());
			int rovaAuthTypeValue = rovaEmailSettings.Authtype.getValue();
			ExternalAdapterLog.d(TAG, "===== rovaAuthTypeValue  ===="+rovaAuthTypeValue);
			extEmailSettInfo.Authtype = ExternalPIMAuthType.fromInt(rovaAuthTypeValue);
		//	ExternalAdapterLog.d(TAG, "===== getExternalEmailSettings auth value ===="+extEmailSettInfo.Authtype.getValue());
			
			
			extEmailSettInfo.SyncInterval = rovaEmailSettings.SyncInterval;
			extEmailSettInfo.SyncPeriod = rovaEmailSettings.SyncPeriod;
			extEmailSettInfo.Server = rovaEmailSettings.Server;
			
			extEmailSettInfo.Domain = rovaEmailSettings.Domain;
			extEmailSettInfo.useSSL = rovaEmailSettings.useSSL;
			extEmailSettInfo.AlwaysSSL =rovaEmailSettings.AlwaysSSL;
			extEmailSettInfo.Port = rovaEmailSettings.Port;
			extEmailSettInfo.EmailCertificate =rovaEmailSettings.EmailCertificate;
			ExternalAdapterLog.d(TAG, "===== getExternalEmailSettings auth value ===="+extEmailSettInfo.EmailCertificate);
			extEmailSettInfo.bAllowEditEmailSettings = rovaEmailSettings.bAllowEditEmailSettings;
			extEmailSettInfo.bDisplayEmailSettings = rovaEmailSettings.bDisplayEmailSettings;
			extEmailSettInfo.bSetupAutomatic = rovaEmailSettings.bSetupAutomatic;
		}
		catch(Exception e){ // 11Dec2013 - Added Try catch for setting default value
			
			exceptionSetDefaultEmailSettingsInfo();
		}
		
		ExternalAdapterLog.d(TAG, "===== out setRovaEmailSettings ====");
	}
	
	
	/** 
	 *  Internal getRovaSecurityProfileSettings() method.
	 *  This private method is used to retrieve
	 *  SecurityProfInfo information.
	 * 
	 */
	private void getRovaSecurityProfileSettings(){
		
		ExternalAdapterLog.d(TAG,"============ In getRovaSecurityProfileSettings =========");
		
		extSecProfInfo = new ExternalSecurityProfInfo();
		
		if(mRova == null){
			ExternalAdapterLog.d(TAG,"===== mRova NULL --- getRovaEmailSettings");
			initializeRova();
		}
	
					
			extSecProfInfo.bDeviceBlock = rovaSecurityProfInfo.bDeviceBlock;
			extSecProfInfo.bDeviceUnregister = rovaSecurityProfInfo.bDeviceUnregister;
			extSecProfInfo.bDeviceWipe = rovaSecurityProfInfo.bDeviceWipe;

		ExternalAdapterLog.d(TAG, "===== SecurityProfileSettings deviceBlock ===="+extSecProfInfo.bDeviceBlock);
		
		ExternalAdapterLog.d(TAG,"============ Out getRovaSecurityProfileSettings =========");
	}
	
	

	private void getRovaVPNDetailsInfo(){
		
		extVPNDetailsInfo = new ExternalVPNDetailsInfo();
		
		if(mRova == null){
			ExternalAdapterLog.d(TAG,"===== mRova NULL --- getRovaEmailSettings");
			initializeRova();
		}
		
	
		try{

			extVPNDetailsInfo.VPNName = rovaVPNDetailInfo.VPNName;
			extVPNDetailsInfo.VPNPassword = rovaVPNDetailInfo.VPNPassword;
			extVPNDetailsInfo.VPNPort = rovaVPNDetailInfo.VPNPort;
			extVPNDetailsInfo.VPNServer = rovaVPNDetailInfo.VPNServer;
			extVPNDetailsInfo.VPNUsername = rovaVPNDetailInfo.VPNUsername;
		}catch(Exception e){ 
			
			exceptionSetDefaultVPNDetailsInfo();
		}

	}
	
	
		
	private void getRovaVPNSettingsInfo(){
		
		extVPNSettInfo = new ExternalVPNSettingsInfo();
		
		if(mRova == null){
			ExternalAdapterLog.d(TAG,"===== mRova NULL --- getRovaEmailSettings");
			initializeRova();
		}
		
		extVPNSettInfo.VPNCertficate = rovaVPNSettingsInfo.VPNCertficate;	//NEW_ROVA_SDK
		extVPNSettInfo.vpninfo = getExternalVPNDetailsInfo();			//NEW_ROVA_SDK

		ExternalAdapterLog.d(TAG, "===== VPN Settings VPNcertificate ===="+extVPNSettInfo.VPNCertficate);
		ExternalAdapterLog.d(TAG, "===== VPN Settings VPNName ===="+extVPNSettInfo.vpninfo.VPNName + "VPN Settings VPNPort" + extVPNSettInfo.vpninfo.VPNPort);
		ExternalAdapterLog.d(TAG, "===== VPN Settings VPNServer ===="+extVPNSettInfo.vpninfo.VPNServer + "===== VPN Settings VPNUsername ===="
				+ extVPNSettInfo.vpninfo.VPNUsername + "VPN Settings VPNPassword" + extVPNSettInfo.vpninfo.VPNPassword );
		ExternalAdapterLog.d(TAG,"============ out getRovaVPNSettingsInfo =========");
	}
	
	
	
	/** 
	 *  Internal getPIMSettingsInfo() method.
	 *  This private method is used to retrieve
	 *  PIMSettingsInfo information.
	 * 
	 */
	private void getPIMSettingsInfo(){
		
		extPIMSettInfo = new ExternalPIMSettingsInfo();
//		if(mRova == null){
			ExternalAdapterLog.d(TAG,"===== mRova NULL --- getPIMSettingsInfo");
			initializeRova();
			
//		}   Commented out this check to initializeROVA everytime ,till we get proper working realtime values.
		
	
			try {
			
			extPIMSettInfo.bCopyPaste = rovaPIMSettingsInfo.bCopyPaste;
			extPIMSettInfo.bEmailDownloadAttachments = rovaPIMSettingsInfo.bEmailDownloadAttachments;
			extPIMSettInfo.bPasswordPIN = rovaPIMSettingsInfo.bPasswordPIN;
			
			extPIMSettInfo.bPasswordSimpleAllowed = rovaPIMSettingsInfo.bPasswordSimpleAllowed;
			extPIMSettInfo.bShortcuts = rovaPIMSettingsInfo.bShortcuts;
			extPIMSettInfo.bShowBrowser = rovaPIMSettingsInfo.bShowBrowser;
			extPIMSettInfo.bShowCalendar = rovaPIMSettingsInfo.bShowCalendar;
			extPIMSettInfo.bShowDialer = rovaPIMSettingsInfo.bShowDialer;
			extPIMSettInfo.bShowContact = rovaPIMSettingsInfo.bShowContact;
			extPIMSettInfo.bShowEmail	= rovaPIMSettingsInfo.bShowEmail;
			extPIMSettInfo.bShowSupport = rovaPIMSettingsInfo.bShowSupport;
			extPIMSettInfo.bShowTXTMessage = rovaPIMSettingsInfo.bShowTXTMessage;
			extPIMSettInfo.bWidgets = rovaPIMSettingsInfo.bWidgets;
			
			extPIMSettInfo.nMaxAttachmentSize = rovaPIMSettingsInfo.nMaxAttachmentSize;
			extPIMSettInfo.nMaxPasswordTries = rovaPIMSettingsInfo.nMaxPasswordTries;
			extPIMSettInfo.nPasswordComplexCharsRequired = rovaPIMSettingsInfo.nPasswordComplexCharsRequired;
			extPIMSettInfo.nPasswordHistory = rovaPIMSettingsInfo.nPasswordHistory;
			extPIMSettInfo.nPasswordLength = rovaPIMSettingsInfo.nPasswordLength;
			ExternalAdapterLog.d(TAG,"----------- rovaPIMSettingsInfo.nPasswordLength --------" + rovaPIMSettingsInfo.nPasswordLength);
			ExternalAdapterLog.d(TAG,"----------- rovaPIMSettingsInfo.bPasswordPIN --------" + rovaPIMSettingsInfo.bPasswordPIN);
			ExternalAdapterLog.d(TAG,"----------- passwordtype"+
			ExternalPasswordType.fromInt(rovaPIMSettingsInfo.Passwordtype.getValue()));
			int mAuthModeValue = rovaPIMSettingsInfo.AuthMode.getValue();
			extPIMSettInfo.AuthMode = ExternalPIMAuthType.fromInt(mAuthModeValue);
			
			extPIMSettInfo.PasswordExpires = ExternalPasswordExpiration.fromInt(rovaPIMSettingsInfo.PasswordExpires.getValue());
			
			extPIMSettInfo.PasswordFailLockout = ExternalPasswordLockTime.fromInt(rovaPIMSettingsInfo.PasswordFailLockout.getValue());
			
			extPIMSettInfo.Passwordtype = ExternalPasswordType.fromInt(rovaPIMSettingsInfo.Passwordtype.getValue());
			
			extPIMSettInfo.SecurityDebugMode = ExternalSecurityResolution.fromInt(rovaPIMSettingsInfo.SecurityDebugMode.getValue());
			extPIMSettInfo.SecurityIME = ExternalSecurityResolution.fromInt(rovaPIMSettingsInfo.SecurityIME.getValue());
			extPIMSettInfo.SecurityRoot = ExternalSecurityResolution.fromInt(rovaPIMSettingsInfo.SecurityRoot.getValue());
			extPIMSettInfo.SecuritySIMRemove = ExternalSecurityResolution.fromInt(rovaPIMSettingsInfo.SecuritySIMRemove.getValue());
			
			// NEW_ADDITION_BROWSER - 290767,11/11/2013
			extPIMSettInfo.bisProxyBasedRoutingEnabled = rovaPIMSettingsInfo.bisProxyBasedRoutingEnabled;
			extPIMSettInfo.sEmailSignature = rovaPIMSettingsInfo.sEmailSignature; // ROVA_POLICY_CHECK - 26Dec2013 , added emailsignature for PIMsettings
			
			}catch(Exception e) { 
				
				exceptionSetDefaultPIMSettingsInfo();
			}
		
	}
	
	
	/**
	 *  Need to check ,whether this method will be used in TruMobi.
	 *  Incase there is a situation ,then DeviceInfo from ROVA should be made public
	 *  so that device values could be accessed from ROVA provided GetDeviceInfo API.
	 * 
	 * 
	 */
	private void getRovaDeviceInfo(){
		
		ExternalDeviceInfo extDeviceInfo =new ExternalDeviceInfo();
//		extDeviceInfo.SerialNumber = rovaDeviceInfo.SerialNumber;
//		extDeviceInfo.UDID= rovaDeviceInfo.UDID;
//		extDeviceInfo.WiFiMACAddress=rovaDeviceInfo.WiFiMACAddress;
//		extDeviceInfo.PhoneNumber=rovaDeviceInfo.PhoneNumber;
//		extDeviceInfo.IMEI=rovaDeviceInfo.IMEI;
//		extDeviceInfo.Make=rovaDeviceInfo.Make;
//		extDeviceInfo.Model=rovaDeviceInfo.Model;
//		extDeviceInfo.OSType=rovaDeviceInfo.OSType;
//		extDeviceInfo.OSVersion=rovaDeviceInfo.OSVersion;
//		extDeviceInfo.ESN=rovaDeviceInfo.ESN;
//		extDeviceInfo.bJailBroken=rovaDeviceInfo.bJailBroken;
		
	}
	
	
	/**
	 *  initializeRova()
	 *  
	 *  This method is to initialize ROVA instance
	 *  and its subclassess.
	 * 
	 */
	private void initializeRova(){
		
		mRova = new rovashared(mExtContext);
		
		//ROVA_SDK_CHG
		if(mRova != null){
			ExternalAdapterLog.d(TAG,"------- rova value is not null");
			com.quintech.rovacommon.RegistrationInfo reginfo = null;
			
			reginfo = mRova.GetRegistrationInformation();
			if (reginfo != null)
			{
				rovaDeviceInfo = reginfo.deviceInfo;
				rovaEmailSettings = reginfo.emailset;
				rovaPIMSettingsInfo = reginfo.pimSettingInfo;
			
				rovaSecurityProfInfo = reginfo.secProfInfo;
				rovaPIMAuthType = reginfo.authenticationType;
				
				rovaVPNSettingsInfo = reginfo.vpnInfo;  //NEW_ROVA_SDK
				rovaVPNDetailInfo = rovaVPNSettingsInfo.vpninfo;
				rovaProxyInfo = reginfo.proxyInfo;	//NEW_ROVA_SDK
			//rovaPIMAuthType = mRova.GetRegistrationInformation().authenticationType;
			}
			
		}
	}
	
	
	
	/**  
	 * isEmailSettingsInfoAvailble() 
	 *  
	 *  This method checks for the availability of all Email settings information
	 * 
	 * @return boolean : TRUE - Information available 
	 */
	
	private boolean isEmailSettingsInfoAvailble(){
		
		boolean bRet = false;
		
		
		
		ExternalAdapterLog.d(TAG,"*** rova domain **"+rovaEmailSettings.Domain);
		ExternalAdapterLog.d(TAG,"*** rova server **"+rovaEmailSettings.Server);
		ExternalAdapterLog.d(TAG,"*** rova address **"+rovaEmailSettings.EmailAddress);
		ExternalAdapterLog.d(TAG,"*** rova empid **"+rovaEmailSettings.EmpId);
		try{
			
			com.quintech.rovacommon.RegistrationInfo reginfo = null;
			
			reginfo = mRova.GetRegistrationInformation();
			
			rovaEmailSettings = reginfo.emailset;
			
			if ( (rovaEmailSettings.Domain.length() != 0) || 
				 (rovaEmailSettings.EmailAddress.length() != 0)  ||	
				 (rovaEmailSettings.EmpId.length() != 0) || 
				 (rovaEmailSettings.Password.length() != 0) || 
				 (rovaEmailSettings.Port.length() != 0) || 
				 (rovaEmailSettings.Server.length() != 0)	 )
			{
				ExternalAdapterLog.d(TAG,"***** Email settings TRUE ****");
				bRet = true;
				return bRet;
			}
			
		}catch(Exception e){
			
			bRet = false;
		}
		ExternalAdapterLog.d(TAG,"***** Email settings FALSE ****");
		return bRet;
	}
	
	
	/**  
	 * isPIMSettingsInfoAvailable() 
	 *  
	 *  This method checks for the availability of necessary PIM settings information
	 * 
	 * @return boolean : TRUE - Information available 
	 */
	
	private boolean isPIMSettingsInfoAvailable(){
		
		boolean bRet = false;
		
		try{
		
			com.quintech.rovacommon.RegistrationInfo reginfo = null;
			
			reginfo = mRova.GetRegistrationInformation();
			
			rovaPIMSettingsInfo = reginfo.pimSettingInfo;
			
			if( (rovaPIMSettingsInfo.nMaxAttachmentSize >= 0) || 
				(rovaPIMSettingsInfo.AuthMode.getValue() >= 0 ) ||
				(rovaPIMSettingsInfo.nMaxPasswordTries >= 0) || 
				(rovaPIMSettingsInfo.nPasswordComplexCharsRequired >= 0) || 
				(rovaPIMSettingsInfo.nPasswordHistory >= 0) ||
				(rovaPIMSettingsInfo.PasswordExpires.getValue() >= 0) ||
				(rovaPIMSettingsInfo.PasswordFailLockout.getValue() >= 0) || 
				(rovaPIMSettingsInfo.Passwordtype.getValue() >= 0) || 
				(rovaPIMSettingsInfo.SecurityDebugMode.getValue() >= 0) || 
				(rovaPIMSettingsInfo.SecurityIME.getValue() >= 0) || 
				(rovaPIMSettingsInfo.SecurityRoot.getValue() >= 0) || 
				(rovaPIMSettingsInfo.SecuritySIMRemove.getValue() >= 0) ){
				
				ExternalAdapterLog.d(TAG,"***** PIM settings TRUE ****");
				bRet = true;
				return bRet;
			}
			
		}catch(Exception e){
			
			bRet =  false;
		}
		ExternalAdapterLog.d(TAG,"***** PIM settings FALSE ****");
		return bRet;
	}
	
	
	/**  
	 * isPIMAuthTypeInfoAvailable() 
	 *  
	 *  This method checks for the availability of PIM Authtype information
	 * 
	 * @return boolean : TRUE - Information available 
	 */
	
	
	private boolean isPIMAuthTypeInfoAvailable(){
		
		boolean bRet = false;
		
		try {
			
			com.quintech.rovacommon.RegistrationInfo reginfo = null;
			
			reginfo = mRova.GetRegistrationInformation();
			
			rovaPIMAuthType = reginfo.authenticationType;
			
			if(rovaPIMAuthType.getValue() >= 0 ){
				
				ExternalAdapterLog.d(TAG,"***** PIM Auth type TRUE ****");
				bRet = true;
				return bRet;
			}
		
		}catch(Exception e){
			
			bRet = false;
		}
		ExternalAdapterLog.d(TAG,"***** PIM Auth type FALSE ****");
		return bRet;
	}
	
	
	/**  
	 * isVPNSettingsInfoAvailable() 
	 *  
	 *  This method checks for the availability of VPN Settings information
	 * 
	 * @return boolean : TRUE - Information available 
	 */
	
	private boolean isVPNSettingsInfoAvailable() {
		
		boolean bRet = false;
		
		try {
			
			com.quintech.rovacommon.RegistrationInfo reginfo = null;
			
			reginfo = mRova.GetRegistrationInformation();
			
			rovaVPNSettingsInfo = reginfo.vpnInfo;
			
			if( (rovaVPNSettingsInfo.VPNCertficate.length() != 0) ||
				(rovaVPNSettingsInfo.vpninfo.VPNName.length() != 0) ||
				(rovaVPNSettingsInfo.vpninfo.VPNPassword.length() != 0) || 
				(rovaVPNSettingsInfo.vpninfo.VPNPort.length() != 0 ) || 
				(rovaVPNSettingsInfo.vpninfo.VPNServer.length() != 0) || 
				(rovaVPNSettingsInfo.vpninfo.VPNUsername.length() != 0 )  )
			{
			
				ExternalAdapterLog.d(TAG,"***** VPN settings TRUE ****");
				bRet = true;
				return bRet;
			}
			
		}catch(Exception e){
			
			bRet = false;
		}
		ExternalAdapterLog.d(TAG,"***** VPN settings FALSE ****");
		return bRet;
	}
	// 11Dec2013
	
	// 11Dec2013
	private boolean isSecuritySettingsInfoAvailable(){
		
		boolean bRet = false;
		
		try{
			
			com.quintech.rovacommon.RegistrationInfo reginfo = null;
			
			reginfo = mRova.GetRegistrationInformation();
			
			rovaProxyInfo = reginfo.proxyInfo;
			
			if( !(rovaProxyInfo.isEmpty()) )
					return true; 
			
		}catch(Exception e){
			
			bRet = false;
			
		}
			
		return bRet;
	}

	
	/**
	 * 	exceptionSetDefaultEmailSettingsInfo()
	 * 
	 *  This method is invoked ,if there is an exception on ExternalEmailSettingsInfo.
	 *  
	 */
	private void exceptionSetDefaultEmailSettingsInfo(){
		
		ExternalEmailSettingsInfo extEmailSettInfo = new ExternalEmailSettingsInfo();
		

		int rovaAuthTypeValue = ExternalPIMAuthType.Basic.getValue(); // 2 - Basic Auth type value. 
		ExternalAdapterLog.d(TAG, "===== rovaAuthTypeValue  ===="+rovaAuthTypeValue);
		extEmailSettInfo.Authtype = ExternalPIMAuthType.fromInt(rovaAuthTypeValue);
	
	}
	
	
	/**
	 * 	exceptionSetDefaultPIMSettingsInfo()
	 * 
	 *  This method is invoked ,if there is an exception on ExternalPIMSettingsInfo.
	 *  
	 */
	private void exceptionSetDefaultPIMSettingsInfo(){
		
		ExternalPIMSettingsInfo extPIMSettInfo = new ExternalPIMSettingsInfo();
		
		extPIMSettInfo.nMaxAttachmentSize = 0;
		extPIMSettInfo.nMaxPasswordTries = 1;
		extPIMSettInfo.nPasswordComplexCharsRequired = 0;
		extPIMSettInfo.nPasswordHistory = 1;
		extPIMSettInfo.nPasswordLength = 5;
		
		int mAuthModeValue = rovaPIMSettingsInfo.AuthMode.getValue();
		extPIMSettInfo.AuthMode = ExternalPIMAuthType.fromInt(mAuthModeValue);
		
//		extPIMSettInfo.PasswordExpires = ExternalPasswordExpiration.fromInt(ExternalPasswordExpiration.NotSet.getValue());
		
//		extPIMSettInfo.PasswordFailLockout = ExternalPasswordLockTime.fromInt(ExternalPasswordLockTime.NotSet.getValue());
		
//		extPIMSettInfo.Passwordtype = ExternalPasswordType.fromInt(ExternalPasswordType.NotSet.getValue());
		
		extPIMSettInfo.SecurityDebugMode = ExternalSecurityResolution.fromInt(ExternalSecurityResolution.Warn.getValue());
		extPIMSettInfo.SecurityIME = ExternalSecurityResolution.fromInt(ExternalSecurityResolution.Warn.getValue());
		extPIMSettInfo.SecurityRoot = ExternalSecurityResolution.fromInt(ExternalSecurityResolution.Warn.getValue());
		extPIMSettInfo.SecuritySIMRemove = ExternalSecurityResolution.fromInt(ExternalSecurityResolution.Warn.getValue());
	}
	
	
	/**
	 * 	exceptionSetDefaultVPNDetailsInfo()
	 * 
	 *  This method is invoked ,if there is an exception on ExternalVPNDetailsInfo.
	 *  
	 */
	private void exceptionSetDefaultVPNDetailsInfo(){
		
		extVPNDetailsInfo = new ExternalVPNDetailsInfo();
		
		extVPNDetailsInfo.VPNName = "";
		extVPNDetailsInfo.VPNPassword = "";
		extVPNDetailsInfo.VPNPort = "";
		extVPNDetailsInfo.VPNServer = "";
		extVPNDetailsInfo.VPNUsername = "";
	}
	
	//11Dec2013
}
