package com.cognizant.trumobi.commonbroadcastreceivers;

import android.content.Context;
import android.content.Intent;

import com.cognizant.trumobi.externaladapter.ExternalAdapterRegistrationClass;
import com.cognizant.trumobi.log.PersonaLog;




public class ExternalPolicyUtil {

	//Events ROVA
	public static final String EVENT_VPN_UPDATE = "Vpn_Settings_Update_Complete_event";
	public static final String EVENT_EMAIL_SETTINGS_UPDATE = "Email_Settings_Update_Complete_Event";
	public static final String EVENT_SIM_CARD_REMOVAL_NOTI =  "SIM_Card_Removal_Notification";
	public static final String EVENT_SEC_PROFILE_CHANGE =  "Security_Profile_Change_Event";
	public static final String EVENT_PIM_CONFIG_UPDATE = "PIM_Configuration_Update_Event";
//	public static final String EVENT_LOGIN_REGISTER_COMP = "Login_Or_Registration_Completion_Event";
	public static final String EVENT_LOGIN_REGISTER_COMP = "LOGIN_COMPLETE_EVENT";
//	public static final String EVENT_SETTING_INFO_COMP =  "Setting_Information_Complete_Event";
	public static final String EVENT_SETTING_INFO_COMP =  "SETTINGS_INFORMATION_FETCH_COMPLETE_EVENT";
	
	//Ids ROVA
	public static final int EVENT_VPN_UPDATE_ID = 1;
	public static final int EVENT_EMAIL_SETTINGS_UPDATE_ID = 2;
	public static final int EVENT_SIM_CARD_REMOVAL_NOTI_ID = 3;
	public static final int EVENT_SEC_PROFILE_CHANGE_ID = 4;
	public static final int EVENT_PIM_CONFIG_UPDATE_ID = 5;
	public static final int EVENT_LOGIN_REGISTER_COMP_ID = 6;
	public static final int EVENT_SETTING_INFO_COMP_ID = 7 ;
	public static int NUM_OF_CALLS_MADE = 0;
	private static String TAG=ExternalPolicyUtil.class.getName();


	public static int getEvent_ID(String mstr){
		if(mstr.equalsIgnoreCase(EVENT_VPN_UPDATE)){
			return EVENT_VPN_UPDATE_ID;
		}else if(mstr.equalsIgnoreCase(EVENT_EMAIL_SETTINGS_UPDATE)){
			return EVENT_EMAIL_SETTINGS_UPDATE_ID;
		}else if(mstr.equalsIgnoreCase(EVENT_SIM_CARD_REMOVAL_NOTI)){
			return EVENT_SIM_CARD_REMOVAL_NOTI_ID;
		}else if(mstr.equalsIgnoreCase(EVENT_SEC_PROFILE_CHANGE)){
			return EVENT_SEC_PROFILE_CHANGE_ID;
		}else if(mstr.equalsIgnoreCase(EVENT_PIM_CONFIG_UPDATE)){
			return EVENT_PIM_CONFIG_UPDATE_ID;
		}else if(mstr.equalsIgnoreCase(EVENT_LOGIN_REGISTER_COMP)){
			return EVENT_LOGIN_REGISTER_COMP_ID;
		}else if(mstr.equalsIgnoreCase(EVENT_SETTING_INFO_COMP)){
			return EVENT_SETTING_INFO_COMP_ID;
		}else{
			return 0;
		}
	}

	/*After successful login Emass will download VPN settings update when there is on update in VPN 
	settings level. Upon the update it will trigger “Vpn_Settings_Update_Complete_event” 
	then Trumobi will invoke “GetVPNSettingsWithCertificate” to fetch all vpn settings 
	information. Based on vpn settings TruMobi will behave.*/
	public static void vpnSettingsUpdateComplete_EventHandler(Context mContext, Intent intent){

	}



	/*After successful login Emass will download Emass settings update when 
	there is on update in Email  settings level. Upon the update it 
	will trigger “Email_Settings_Update_Complete_Event” then Trumobi 
	will invoke “GetEmailSettings” to fetch all email settings information.
	Based on email settings TruMobi will behave.
	 */
	public static void emailSettingsUpdateComplete_EventHandler(Context mContext, Intent intent){

	}



	/*After successful login Emass will trigger “SIM_Card_Removal_Notification” when 
	there is SIM removal in device. Then TruMobi will invoke wipe local content data.*/
	public static void simCardRemovalNotification_EventHandler(Context mContext, Intent intent){

	}

	/*After successful login Emass will download security profile settings update when there is on update in 
	security settings level. Upon the update it will trigger “Security_Profile_Change_Event” then Trumobi will invoke 
	“GetSecurityProfileSettings” to fetch all securitysettings information. Based on security settings TruMobi will behave.
	 */	
	public static void securityProfileChange_EventHandler(Context mContext, Intent intent){

		PersonaLog.d(TAG,"===== securityProfileChange_EventHandler === ");
		Intent intentSvc = new Intent(mContext, ExternalPolicyIntentService.class);
		intentSvc.putExtra("Id", getEvent_ID(intent.getStringExtra("event")));
		mContext.startService(intentSvc);
	}
	
	public static void pimConfigurationUpdate_EventHandler(Context mContext, Intent intent){

		//General Settings:
		/*public PIMAuthType AuthMode;
		public Boolean bShortcuts;
		public Boolean bWidgets;

		//Password Settings:
		public Boolean bPasswordPIN;
		public Integer nMaxPasswordTries;
		public Integer nPasswordLength;
		public PasswordType Passwordtype;
		public Boolean bPasswordSimpleAllowed;
		public Integer nPasswordComplexCharsRequired;
		public PasswordExpiration PasswordExpires;
		public Integer nPasswordHistory;
		public PasswordLockTime PasswordFailLockout;

		//Security Settings:
		public Boolean bCopyPaste;
		public SecurityResolution SecuritySIMRemove;
		public SecurityResolution SecurityDebugMode;
		public SecurityResolution SecurityRoot;
		public SecurityResolution SecurityIME;

		//Email Settings:
		String sEmailSignature;
		public Boolean bEmailDownloadAttachments;
		public Integer nMaxAttachmentSize;

		//ApplicationSettings:
		public Boolean bShowEmail;
		public Boolean bShowContact;
		public Boolean bShowCalendar;
		public Boolean bShowBrowser;
		public Boolean bShowTXTMessage;
		public Boolean bShowDialer;
		public Boolean bShowSupport;*/
		//PolicyIntentSrevice

		/*intent.setClass(mContext, PolicyIntentService.class);
		intent.putExtra("Id", PolicyUtil.EVENT_PIM_CONFIG_UPDATE_ID);
		mContext.startService(intent);*/
	}
	public static void loginOrRegistrationCompletion_EventHandler(Context mContext, Intent intent){

		/*check below these  fails call ->“InitiateToFetchAllSettingsinformation” */
		/*i.	Registration information, 
		ii.	Authentication Type,
		iii.	 EmailSettings,
		iv.	 EmailCertificate,
		v.	 VPNSetingsWithCertificate, 
		vi.	ProfileSettings,
		vii.	 PIMSettings*/
		/*Intent localBroadCast = new Intent("com.cognizant.trubox.event.trumobireceiver");
		localBroadCast.putExtra("event", event);
		mContext.sendBroadcast(localBroadCast);*/
		boolean loginResult = intent.getBooleanExtra("Result", true);
		Intent updateUI = new Intent("LoginActionComplete");
		updateUI.putExtra("Result", loginResult);
		mContext.sendBroadcast(updateUI);
	}

	
	public static void settingInformationCompleten_EventHandler(Context mContext, Intent intent){

		ExternalAdapterRegistrationClass mExtAdapReg;
		mExtAdapReg = ExternalAdapterRegistrationClass.getInstance(mContext);
		boolean isSettingsInfoAvailable = mExtAdapReg.isAllRegisteredInformationAvailable();
		
		if(isSettingsInfoAvailable) {
		sendThisUIUpdate(mContext,isSettingsInfoAvailable);
		}
		else {
			ExternalPolicyUtil.NUM_OF_CALLS_MADE++;
			 if(ExternalPolicyUtil.NUM_OF_CALLS_MADE <= 5) {
				 mExtAdapReg.initiateExternalToFetchAllSettingsInfo();
			PersonaLog.d("Calling initiate", ExternalPolicyUtil.NUM_OF_CALLS_MADE + "");
				 //Added to test. Remove these lines once event comes from SDK
				/* Intent trumobiReceiverIntent = new Intent("com.cognizant.trubox.event");
				 mContext.sendBroadcast(trumobiReceiverIntent);*/
			 }
			 
			 else 
				 sendThisUIUpdate(mContext,false);
		}

	 }

	private static void sendThisUIUpdate(Context mContext, boolean isSettingsInfoAvailable) {
		boolean safeMethodfailed = !isSettingsInfoAvailable;
		/*Intent localBroadCast = new Intent("com.cognizant.trubox.event.trumobireceiver");
		localBroadCast.putExtra("safe_method_failed", safeMethodfailed);
		localBroadCast.putExtra("event", event);
		
		mContext.sendBroadcast(localBroadCast);*/
		
		Intent updateUI = new Intent("UpdateLoginScreenUI");
		updateUI.putExtra("safe_method_failed",safeMethodfailed);
		mContext.sendBroadcast(updateUI);
		
	}
}

