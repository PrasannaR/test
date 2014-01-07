package com.quintech.rovacommon;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import net.sqlcipher.DatabaseUtils;
import net.sqlcipher.database.SQLiteDatabase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.quintech.common.AbstractData.Flags;
import com.quintech.common.AbstractData.Settings;
import com.quintech.common.ExchangeSettings;
import com.quintech.connect.Constants;
import com.quintech.connect.Data;
import com.quintech.rovacommon.PIMSettingsInfo.*;

public class FetchData 
{
	
	public static String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static String DATE_FORMAT = "yyyy-MM-dd";
	public static String TIME_FORMAT = "HH.mm.ss.SSS";
	
	private static String TAG = "FetchData";
	
	public EmailSettings getEmailSettings()
	{
		List <ExchangeSettings> ExchangeServers;
		EmailSettings emailsettings = new EmailSettings();
		ExchangeServers = Constants.getData().getExchangeSettings();
		
		if (Constants.getData().getFlag(Flags.FLG_IsRegistered, Data.SafeSpace))
		{
			if (ExchangeServers.size()>0)
			{
			
				ExchangeSettings Exchange = ExchangeServers.get(0);
				
				emailsettings.AlwaysSSL = false;
				emailsettings.Authtype = PIMAuthType.Basic;
				emailsettings.useSSL = Exchange.useSSL;
				emailsettings.SyncInterval = Exchange.SyncInterval;
				emailsettings.SyncPeriod = Exchange.SyncPeriod;
				emailsettings.EmailCertificate = Exchange.EmailCertificate;
				emailsettings.bSetupAutomatic = false; 
				emailsettings.bDisplayEmailSettings = Constants.getData().getFlag(Flags.FLG_DisplayEmailSettings, Data.SafeSpace);
				emailsettings.bAllowEditEmailSettings = Constants.getData().getFlag(Flags.FLG_AllowEditEmailSettings, Data.SafeSpace);
	
				emailsettings.Domain = Exchange.Domain;
				emailsettings.EmailAddress = Exchange.UserEmailAddress;
				emailsettings.EmpId = Exchange.UserAccountName;
				emailsettings.Port = Exchange.Port;
				emailsettings.Server = Exchange.Host;
				emailsettings.Password = "";
			}
			else 
			{			
				emailsettings.AlwaysSSL =  Constants.getData().getFlag(Flags.FLG_EmailAlwaysSSL, Data.SafeSpace);
				emailsettings.Authtype = PIMAuthType.fromInt(Integer.parseInt(Constants.getData().getSetting(Settings.SET_EmailAuthMode, Data.SafeSpace)));
				emailsettings.useSSL = Constants.getData().getFlag(Flags.FLG_EmailUseSSL, Data.SafeSpace);
				emailsettings.SyncInterval = Integer.parseInt(Constants.getData().getSetting(Settings.SET_EmailSyncInterval, Data.SafeSpace));
				emailsettings.SyncPeriod = Integer.parseInt(Constants.getData().getSetting(Settings.SET_EmailSyncPeriod, Data.SafeSpace));
				emailsettings.EmailCertificate = Constants.getData().getSetting(Settings.SET_EmailCertificate, Data.SafeSpace);
				emailsettings.bSetupAutomatic = true; //Constants.getData().getFlag(Flags.FLG_EmailSetupAutomatic, Data.SafeSpace);
				emailsettings.bDisplayEmailSettings = Constants.getData().getFlag(Flags.FLG_DisplayEmailSettings, Data.SafeSpace);
				emailsettings.bAllowEditEmailSettings = Constants.getData().getFlag(Flags.FLG_AllowEditEmailSettings, Data.SafeSpace);
		
				emailsettings.Domain = Constants.getData().getSetting(Settings.SET_ExchangeDomain, Data.SafeSpace);
				emailsettings.EmailAddress = Constants.getData().getSetting(Settings.SET_UserEmailAddress, Data.SafeSpace);
				emailsettings.EmpId = Constants.getData().getSetting(Settings.SET_ExchangeUsername, Data.SafeSpace);
				emailsettings.Port = Constants.getData().getSetting(Settings.SET_ExchangePort, Data.SafeSpace);
				emailsettings.Server = Constants.getData().getSetting(Settings.SET_ExchangeHost, Data.SafeSpace);
				emailsettings.Password = Constants.getData().getSetting(Settings.SET_EmailPassword, Data.SafeSpace);
			}
		}
		return emailsettings;
	}
	
	
	
	
	public PIMSettingsInfo getPIMSettings()
	{
		PIMSettingsInfo PIMSettings = new PIMSettingsInfo();
		
		try
		{
		//General Settings:
		 
		PIMSettings.AuthMode = PIMAuthType.fromInt(Integer.parseInt(Constants.getData().getSetting(Settings.SET_PIMAuthType, Data.SafeSpace)));
		PIMSettings.bShortcuts = Constants.getData().getFlag(Flags.FLG_ShortcutsAllowed, Data.SafeSpace);
		PIMSettings.bWidgets = Constants.getData().getFlag(Flags.FLG_WidgetsAllowed, Data.SafeSpace);
		
		//Password Settings:
		PIMSettings.bPasswordPIN = Constants.getData().getFlag(Flags.FLG_PasswordPIN, Data.SafeSpace);
		PIMSettings.nMaxPasswordTries = Integer.parseInt(Constants.getData().getSetting(Settings.SET_MaxPasswordTries, Data.SafeSpace));
		PIMSettings.nPasswordLength = Integer.parseInt(Constants.getData().getSetting(Settings.SET_PasswordLength, Data.SafeSpace));
		PIMSettings.Passwordtype = PasswordType.fromInt(Integer.parseInt(Constants.getData().getSetting(Settings.SET_PasswordType, Data.SafeSpace)));
		PIMSettings.bPasswordSimpleAllowed = Constants.getData().getFlag(Flags.FLG_PasswordSimpleAllowed, Data.SafeSpace);
		PIMSettings.nPasswordComplexCharsRequired = Integer.parseInt(Constants.getData().getSetting(Settings.SET_PasswordComplexChars, Data.SafeSpace));
		PIMSettings.PasswordExpires = PasswordExpiration.fromInt(Integer.parseInt(Constants.getData().getSetting(Settings.SET_PasswordExpires, Data.SafeSpace)));
		PIMSettings.nPasswordHistory = Integer.parseInt(Constants.getData().getSetting(Settings.SET_PasswordHistory, Data.SafeSpace));
		PIMSettings.PasswordFailLockout = PasswordLockTime.fromInt(Integer.parseInt(Constants.getData().getSetting(Settings.SET_PasswordFailLockout, Data.SafeSpace)));
		
		//Security Settings:
		PIMSettings.bCopyPaste = Constants.getData().getFlag(Flags.FLG_CopyPasteAllowed, Data.SafeSpace);
		PIMSettings.SecuritySIMRemove = SecurityResolution.fromInt(Integer.parseInt(Constants.getData().getSetting(Settings.SET_SecuritySIMRemoved, Data.SafeSpace)));
		PIMSettings.SecurityDebugMode = SecurityResolution.fromInt(Integer.parseInt(Constants.getData().getSetting(Settings.SET_SecurityDebugMode, Data.SafeSpace)));
		PIMSettings.SecurityRoot = SecurityResolution.fromInt(Integer.parseInt(Constants.getData().getSetting(Settings.SET_SecurityRoot, Data.SafeSpace)));
		PIMSettings.SecurityIME = SecurityResolution.fromInt(Integer.parseInt(Constants.getData().getSetting(Settings.SET_SecurityIME, Data.SafeSpace)));
		
		//Email Settings:
		PIMSettings.sEmailSignature = Constants.getData().getSetting(Settings.SET_EmailSignature, Data.SafeSpace);
		PIMSettings.bEmailDownloadAttachments = Constants.getData().getFlag(Flags.FLG_EmailAttachmentDownloadEnabled, Data.SafeSpace);
		PIMSettings.nMaxAttachmentSize = Integer.parseInt(Constants.getData().getSetting(Settings.SET_MaxAttachmentSize, Data.SafeSpace));
		
		//ApplicationSettings:
		PIMSettings.bShowEmail = Constants.getData().getFlag(Flags.FLG_EmailEnabled, Data.SafeSpace);
		PIMSettings.bShowContact = Constants.getData().getFlag(Flags.FLG_ContactsEnabled, Data.SafeSpace);
		PIMSettings.bShowCalendar = Constants.getData().getFlag(Flags.FLG_CalendarEnabled, Data.SafeSpace);
		PIMSettings.bShowBrowser = Constants.getData().getFlag(Flags.FLG_BrowserEnabled, Data.SafeSpace);
		PIMSettings.bShowTXTMessage = Constants.getData().getFlag(Flags.FLG_TXTMsgEnabled, Data.SafeSpace);
		PIMSettings.bShowDialer = Constants.getData().getFlag(Flags.FLG_DialerEnabled, Data.SafeSpace);
		PIMSettings.bShowSupport = Constants.getData().getFlag(Flags.FLG_SupportEnabled, Data.SafeSpace);
		PIMSettings.bisProxyBasedRoutingEnabled = Constants.getData().getFlag(Flags.FLG_ProxyBasedRoutingEnabled, Data.SafeSpace);
		
		}
		catch (Exception e)
		{
			
		}
		
		return PIMSettings;
	}


	public SecurityProfileInfo getSecurityProfileInfo() 
	{
		SecurityProfileInfo securityprofile = new SecurityProfileInfo();
		
		securityprofile.bDeviceBlock = Constants.getData().getFlag(Flags.FLG_DeviceBlock, Data.SafeSpace);
		securityprofile.bDeviceUnregister = Constants.getData().getFlag(Flags.FLG_DeviceUnregister, Data.SafeSpace);
		securityprofile.bDeviceWipe = Constants.getData().getFlag(Flags.FLG_DeviceWipe, Data.SafeSpace);
		return securityprofile;
		
	}

	public VPNSettingsInfo getVPNSettingsInfo()
	{
		VPNSettingsInfo vpninfo = new VPNSettingsInfo();
		
		return vpninfo;
	}

	public DeviceInfo getDeviceInfo() {
		DeviceInfo devinfo = new DeviceInfo();
		
		devinfo.DeviceID = Constants.getData().getSetting(Settings.SET_DeviceID);
		devinfo.SerialNumber= Constants.getData().getSetting(Settings.SET_DeviceSerialNumber);
		devinfo.UDID= Constants.getData().getSetting(Settings.SET_DeviceUDID);
		devinfo.WiFiMACAddress= Constants.getData().getSetting(Settings.SET_DeviceMac);
		devinfo.PhoneNumber= Constants.getData().getSetting(Settings.SET_DeviceMDN);
		devinfo.IMEI= Constants.getData().getSetting(Settings.SET_DeviceIMEI);
		devinfo.Make= Constants.getData().getSetting(Settings.SET_DeviceMake);
		devinfo.Model= Constants.getData().getSetting(Settings.SET_DeviceModel);
		devinfo.OSType= Constants.getData().getSetting(Settings.SET_DeviceOS);
		devinfo.OSVersion= Constants.getData().getSetting(Settings.SET_DeviceOSVersion);
		devinfo.ESN= Constants.getData().getSetting(Settings.SET_DeviceESN);
		devinfo.bJailBroken = Constants.getData().getFlag(Flags.FLG_JailBroken);
		
		return devinfo;
	}
}
