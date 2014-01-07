package com.quintech.common;


import java.util.List;

import com.quintech.common.DeviceCommandIssuedInfo.DeviceCommands;
import com.quintech.common.ILog.Type;
import com.quintech.common.SmartConnectAllowedConnectionTypes.ConnectionType;


public abstract class AbstractData
{
	private static String TAG = "AbstractData";
	
	public AbstractData()
	{
		
	}
	
	
	
	public String convertToString(boolean value)
	{
		// convert boolean to string representation to use for insertion into SQLite db
		String returnString = "0";
		
		try
		{
			if (value)
				returnString = "1";
		}
		catch (Exception e)
		{
			AbstractConstants.log.add(TAG, Type.Error, "convertToString", e);
		}
		
		return returnString;
	}
	
	
	public abstract String escapeString(String value);
	
	
	public abstract long insertLog(String tag, String type, String message, String error);
	
	
	public abstract void deleteOldLogs();
	
	
	public abstract HotspotInfo isSSIDLoginRequired(String ssid);
	
	
	public abstract HotspotInfo isSSIDInDirectory(String ssid, ConnectionType connectionType);
	
	public abstract long insertExchange(ExchangeSettings Exchangeinfo);
	
	public abstract void updateExchangeInfo(ExchangeSettings Exchangeinfo);
	
	public abstract DirectoryInfo getDirectoryInfo(long portalSysID);
	
	
	public abstract long insertDirectory(DirectoryInfo directoryInfo, boolean enabled, boolean imported);
	
	
	public abstract void setDirectoryEnabled(DirectoryInfo directoryInfo);
	
	
	public abstract void setDirectorySmartConnectRanking(long directoriesPortalSysID, int rank);
	
	
	public abstract void setImportedDirectoriesDisabled();
	
	
	public abstract void updateDirectoryInfo(DirectoryInfo directoryInfo);
	
	
	public abstract void insertHotspot(String directoryGUID, HotspotInfo hotspot);
	
	
	public abstract void deleteHotspots(String directoryGUID);
	

	public abstract float getHotspotVersion(String directoryGUID);

	
	public abstract HotspotInfo getHotspotInfo(int hotspotsID);
	
	
	public abstract List<SessionInfo> getNewSessions();
	

	public abstract List<FailedAuthenticationInfo> getNewFailedAuthentications();
	
	
	public abstract void setFailedAuthenticationsSubmittedDate(List<FailedAuthenticationInfo> listFailedAuthentications);
	
	
	public abstract long insertSession(String bSSID, String ssid, boolean isJailbroken, String deviceModelNumber, 
								String deviceType, String osType, String osVersion, String clientVersion,
								String latitude, String longitude);
	
	
	public abstract void insertFailedAuthentication(java.util.Date dateAdded, String latitude, String longitude, String ssid, String macAddress, String wisprXmlLog, String resultCode);
	
	
	public abstract void updateSessionEndTime(long sessionSysID);

	
	public abstract void updateSessionWISPrValues(long sessionSysID, String wisprLoginURL, String wisprLogoffURL, String wisprbwUserGroup, String wisprLocationName, String wisprLocationID);
	
	
	public abstract void addSessionNetworkTraffic(long sessionSysID, long addOctetsReceived, long addOctetsUploaded);
	
	
	public abstract SessionInfo getSession(String ssid, boolean filterRecent);

	
	public abstract void setSessionsSubmittedDate(List<SessionInfo> listSessions);
	

	public abstract void insertIgnoredHotspot(String ssid);
	

	public abstract void deleteIgnoredHotspot(String ssid);
	

	public abstract boolean isIgnoredHotspot(String ssid, int ignoredHotspotDurationInHours);
	

	public abstract void setSetting(Settings setting, String value, Boolean IsValueSetFromServer);
	
	public abstract void setSetting(Settings setting, String value, String ContainerID, Boolean IsValueSetFromServer);
	

	public abstract void setFlag(Flags flag, Boolean value, Boolean IsValueSetFromServer);

	public abstract void setFlag(Flags flag, Boolean value, Boolean IsValueSetFromServer, String ContainerID);

	
	public abstract void updateClientSettings(List<ClientSettingInfo> listClientSettings);
	

	public abstract String getSetting(Settings setting);
	
	public abstract String getSetting(Settings setting, String ContainerID);


	public abstract Boolean getFlag(Flags flag);

	public abstract Boolean getFlag(Flags flag, String ContainerID);

	
	public abstract boolean isExistingDeviceCommandIssued(String commandGuid);
	
	
	public abstract void insertDeviceCommandsIssued(List<DeviceCommandIssuedInfo> issuedDeviceCommands);
	
	
	public abstract void insertDeviceCommandIssued(String commandGuid, DeviceCommands command, String parameter);
	
	
	public abstract void setDeviceCommandIssuedExecuted(String commandGuid, Boolean succeeded, String errorMessage);
	
	
	public abstract List<DeviceCommandIssuedInfo> getDeviceCommandsIssued(boolean getUnsentExecuted, boolean getNotExecuted);
	
	
	public abstract void setDeviceCommandIssuedReceivedByPortal(List<String> listCommandGuids);
	
	
	public abstract List<ShortcutInfo> getInstalledShortcuts();
	
	
	public abstract void insertInstalledShortcut(ShortcutInfo shortcut);
	
	
	public abstract void deleteInstalledShortcut(ShortcutInfo shortcut);
	
	
	public abstract void deleteSmartConnectAllowedConnectionTypes();
	
	
	public abstract void insertSmartConnectAllowedConnectionTypes(List<SmartConnectAllowedConnectionTypes> listAllowedTypes);
	
	
	public abstract SmartConnectAllowedConnectionTypes getSmartConnectAllowedConnectionType(ConnectionType connectionType);
	
	
	public abstract void deleteAssignedApplications();
	
	
	public abstract void insertAssignedApplications(List<ApplicationAssignedInfo> listApplications);
	
	
	public abstract List<ApplicationAssignedInfo> getAssignedApplications();
	
	
	public abstract void insertDownloadQueueInfo(DownloadQueueInfo downloadQueueInfo);
	
	
	public abstract void deleteDownloadQueueInfo(long downloadID);
	
	
	public abstract DownloadQueueInfo getDownloadQueueInfo(long downloadID);
	
	
	public abstract DownloadQueueInfo getDownloadQueueInfo(int applicationsAssignedPortalSysID);
	
	
	public void updateDatabaseSettingValues(List<ClientSettingInfo> listClientSettings)
	{
		try
		{
			// update database values with the specified setting values from the server
			
			if (listClientSettings == null)
				return;
			
			// for each new value
			for (ClientSettingInfo settingInfo : listClientSettings)
			{
				// skip null values
				if (settingInfo.settingName == null || settingInfo.value == null)
					continue;
				
				// find matching setting name
				for (Settings setting : Settings.values()) 
				{
					if (!settingInfo.settingName.equalsIgnoreCase(setting.name()))
						continue;
					
					// update value
					setSetting(setting, settingInfo.value, true);
					break;
				}
				
				// find matching flag name
				for (Flags flag : Flags.values()) 
				{
					if (!settingInfo.settingName.equalsIgnoreCase(flag.name()))
						continue;
					
					// check that value parses to a boolean value
					try
					{
						boolean bValue = Boolean.parseBoolean(settingInfo.value);
						
						// update value
						setFlag(flag, bValue, true);					
					}
					catch (Exception e)
					{
						AbstractConstants.log.add(TAG, Type.Error, "Unable to parse boolean client setting value for " + settingInfo.settingName, e);
					}
					
					break;
				}
			}
		}
		catch (Exception e)
    	{
			AbstractConstants.log.add(TAG, Type.Error, "updateDatabaseSettingValues", e);
    	}
	}
	
	
	public String getApplicationInstallUUID()
	{
		String UUID = "";
		
		try
		{
			// retrieve existing application installaion UUID
			UUID = getSetting(Settings.SET_ApplicationInstallUUID);
			
			// if UUID does not exist, create a new one and save it in the database
			if (UUID == null || UUID.length() == 0)
			{
				// remove dashes from new UUID
				UUID = java.util.UUID.randomUUID().toString().replace("-", "");
				
				setSetting(Settings.SET_ApplicationInstallUUID, UUID, false);
				AbstractConstants.log.add(TAG, Type.Debug, "Created Application Install UUID: " + UUID);
			}
			
			AbstractConstants.log.add(TAG, Type.Debug, "Retrieved Application Install UUID: " + UUID);
		}
		catch (Exception e)
	    {
	    	AbstractConstants.log.add(TAG, Type.Error, "getApplicationInstallUUID", e);
	    }
		
		
		return UUID;
	}
	
	
	public enum Flags
	{
		FLG_EnableDebugMode,
		FLG_AutoConnect,
		FLG_UseVerizonWirelessCredentials,
		FLG_EnableFullUserInterface,
		FLG_EnableSplashScreen,
		FLG_DisableNotificationAudio,
		FLG_DisableNotificationVibration,		
		FLG_HasInitialized,
		FLG_HasNotifiedForAvailableHotspot,
		FLG_VerifyWebsiteTrust,
		FLG_AlwaysUseDefaultWisprCredential,
		FLG_AlwaysUseDefaultUserIdDecoration,
		FLG_AlwaysUseDefaultVZWServer,
		FLG_EnableSessionBytesReporting,
		FLG_EnableSessionReporting, 
		FLG_HasVerizonNetworkAccount,
		FLG_AlwaysShowTestBuildPrompt,
		FLG_VerizonDevice,
		FLG_PasswordEnabled,
		FLG_DisableCamera,
		FLG_EnrollmentPolicyResult,
		FLG_UserDisabledLocationReporting,
		FLG_DisableAppStore,
		FLG_DisableBrowser,
		FLG_BrowserDisableAutoFill,
		FLG_BrowserForceFraudWarning,
		FLG_BrowserDisableJavascript,
		FLG_BrowserDisablePopups, 
		FLG_DisableYouTubeApp,
		FLG_DisableVoiceDialing,
		FLG_DisableInstallingApps,
		FLG_DisableNonMarketApps,
		FLG_DisableAutoSyncWhileRoaming,
		FLG_DisableScreenCapture,
		FLG_DisableCellularData,
		FLG_DisableTethering,
		FLG_DisableBluetooth,
		FLG_DisableSdCard,
		FLG_DisableFactoryReset,
		FLG_DisableSettingChanges,
		FLG_DisableClipboard,
		FLG_EncryptInternalStorage,
		FLG_EncryptExternalStorage,
		FLG_SmartConnectIncludeOpenNetworks,
		FLG_RequireDeviceAdministratorAccess,
		FLG_DivideEnabled,
		FLG_DivideMdmClientRegistered,
		FLG_IsRegistered,
		
		FLG_CONTAINER_PIMSETTINGS_START,
		FLG_ShareContact,
		FLG_ShareCalendar,
		FLG_ShortcutsAllowed,
		FLG_WidgetsAllowed,
		FLG_PasswordPIN,
		FLG_PasswordSimpleAllowed,
		FLG_CopyPasteAllowed,
		FLG_EmailEnabled,
		FLG_ContactsEnabled,
		FLG_BrowserEnabled,
		FLG_TXTMsgEnabled,
		FLG_CalendarEnabled,
		FLG_DialerEnabled,
		FLG_SupportEnabled,
		FLG_ProxyBasedRoutingEnabled,
		FLG_ProxyHTTPS,
		FLG_ProxyHTTP,
		FLG_ProxyUDP,
		FLG_ProxyTCP,
		FLG_CONTAINER_PIMSETTINGS_END,
		
		FLG_CONTAINER_EMAILSETTINGS_START,
		FLG_EmailAttachmentDownloadEnabled,
		FLG_EmailAlwaysSSL,
		FLG_EmailUseSSL,
		FLG_EmailUseCertificate, 
		FLG_EmailSetupAutomatic,
		FLG_DisplayEmailSettings,
		FLG_AllowEditEmailSettings,
		FLG_CONTAINER_EMAILSETTINGS_END,
		
		FLG_CONTAINER_SECURITYSETTINGS_START,
		FLG_DeviceWipe,
		FLG_DeviceBlock,
		FLG_DeviceUnregister,
		FLG_CONTAINER_SECURITYSETTINGS_END,
		
		FLG_JailBroken,
	
		FLG_PIMSettings_Updated,
		FLG_EmailSettings_Updated,
		FLG_SecuritySettings_Updated,
		FLG_DestinationManagement_Updated,
		
	    FLG_BROWSER_SETTINGS_START,
	    FLG_CookiesBlocked,
	    FLG_FileDownloadBlocked,
	    FLG_PrintingBlocked,
	    FLG_BrowserCopyPasteBlocked,
	    FLG_BROWSER_SETTINGS_END
	}
	

	public enum Settings
	{
		SET_DefaultWisprUserID,
		SET_DefaultWisprPassword,
		SET_VerizonWirelessUserDecoration3G,
		SET_VerizonWirelessUserDecoration4G,
		SET_ConfigurationLastUpdatedMillis,
		SET_VZWServerList,
		SET_IgnoredHotspotDurationInHours,
		SET_SessionTimeoutInHours,
		SET_UpdateConfigurationIntervalInHours,
		SET_NumberOfLogFilesToPersist,
		SET_DeviceMacAddress,
		SET_ApplicationBuildType,
		SET_VerizonWirelessWisprAgent,
		SET_VerizonWirelessProbeUrl,
		SET_VerizonBusinessProbeUrl,
		SET_DefaultUserIdDecoration,
		SET_DefaultVZWServer,
		SET_UserDebugCode,
		SET_GCMRegistrationID,
		SET_RovaPortalServerList,
		SET_RovaPortalWebservicePath,
		SET_RovaPortalUserID,
		SET_RovaPortalPassword,
		SET_ApplicationInstallUUID,
		SET_GCMProjectID, 
		SET_VerizonNetworkPassword,
		SET_MinimumPasswordLength,
		SET_PasswordQualityType,
		SET_MinimumLettersRequired,
		SET_MinimumLowercaseLettersRequired,
		SET_MinimumNonLetterCharactersRequired,
		SET_MinimumNumericalDigitsRequired,
		SET_MinimumSymbolsRequired,
		SET_MinimumUppercaseLettersRequired,
		SET_PasswordExpirationTimeoutMinutes,
		SET_PasswordHistoryRestriction,
		SET_MaximumFailedPasswordAttempts,
		SET_MaximumInactivityTimeoutMinutes,
		SET_RovaPortalRegistrationUrl,
		SET_EnrollmentPolicyMessage,
		SET_ReportDeviceLocation,
		SET_ReportInstalledApplications,
		SET_DataRoaming,
		SET_VoiceRoaming,
		SET_MMSRoaming,
		SET_MinimumLocationAccuracyMeters,
		
		SET_ExchangeDomain,
		SET_ExchangeHost,
		SET_ExchangePort,
		SET_ExchangeUsername,
		SET_UserEmailAddress,
		
		SET_CONTAINER_EMAILSETTINGS_START,
		SET_EmailSignature,
		SET_MaxAttachmentSize,
		SET_EmailSyncInterval,
		SET_EmailSyncPeriod,
		SET_EmailCertificate,
		SET_EmailPassword, 
		SET_EmailAuthMode, 
		SET_CONTAINER_EMAILSETTINGS_END,
		
		SET_CONTAINER_PIMSETTINGS_START,
		SET_PIMAuthType,
		SET_MaxPasswordTries,
		SET_PasswordLength,
		SET_PasswordType,
		SET_PasswordComplexChars,
		SET_PasswordExpires,
		SET_PasswordHistory,
		SET_PasswordFailLockout,
		SET_ProxyServerPortNumber,
		SET_ProxyServerName,
		SET_CONTAINER_PIMSETTINGS_END,
		
		SET_CONTAINER_SECURITYSETTINGS_START,
		SET_SecuritySIMRemoved,
		SET_SecurityDebugMode,
		SET_SecurityRoot,
		SET_SecurityIME,
		SET_CONTAINER_SECURITYSETTINGS_END,

	    SET_BROWSER_SETTINGS_START,
	    SET_BrowserBlackWhite,
	    SET_BlockedMessage,
	    SET_HomePageURL,
	    SET_BROWSER_SETTINGS_END,
	    
		SET_DeviceID, 
		SET_DeviceSerialNumber, 
		SET_DeviceUDID, 
		SET_DeviceMac, 
		SET_DeviceMDN, 
		SET_DeviceIMEI, 
		SET_DeviceMake, 
		SET_DeviceModel, 
		SET_DeviceOS, 
		SET_DeviceOSVersion, 
		SET_DeviceESN

	}
	
}