package com.quintech.connect;


import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.sqlcipher.DatabaseUtils;
import net.sqlcipher.database.SQLiteDatabase;

import android.content.ContentValues;
import android.content.Context;

import com.quintech.common.ApplicationAssignedInfo;
import com.quintech.common.ClientSettingInfo;
import com.quintech.common.DeviceCommandIssuedInfo;
import com.quintech.common.DeviceCommandIssuedInfo.DeviceCommands;
import com.quintech.common.DirectoryInfo;
import com.quintech.common.DownloadQueueInfo;
import com.quintech.common.ExchangeSettings;
import com.quintech.common.FailedAuthenticationInfo;
import com.quintech.common.HotspotInfo;
import com.quintech.common.ShortcutInfo;
import com.quintech.common.ILog.Type;
import com.quintech.common.SessionInfo;
import com.quintech.common.SmartConnectAllowedConnectionTypes;
import com.quintech.common.SmartConnectAllowedConnectionTypes.ConnectionType;


public class Data extends com.quintech.common.AbstractData
{
	private static String TAG = "Data";
	
	public static String DB_PATH = "/data/data/com.quintech.connect/databases/";
	public static String DB_NAME = "encrypted.db3";
	private static SQLiteDatabase database = null;
	private static int DATABASE_VERSION = 145;
	
	public static final String SafeSpace ="3";
	
	public static SQLiteDatabase getDatabase()
	{
		return database;
	}
	
	
	public void initialize(Context context)
	{
		try
		{
			if (isDatabaseOpen() && !database.needUpgrade(DATABASE_VERSION))
			{
				Constants.getLog().add(TAG, Type.Debug, "Database is already open and version is current, skipping initialization");
				return;
			}
			
			DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
			
			Constants.getLog().addToFileOnly(TAG, Type.Debug, "Initializing database");
			SQLiteDatabase.loadLibs(context);
			String dbFilePath = DB_PATH + DB_NAME;
			String dbPass = Constants.getLibrary().extractKeyValue(Constants.getLibrary().listRequiredSettings());

			try
			{
				database = SQLiteDatabase.openDatabase(dbFilePath, dbPass, null, SQLiteDatabase.OPEN_READWRITE);
			}
			catch (Exception e)
	    	{
				Constants.getLog().addToFileOnly(TAG, Type.Debug, "Unable to open or find existing database");
	    	}
			
			// create and initialize new database if required
			if (database == null || !database.isOpen() || database.isReadOnly())
			{
				// create new database
				createDatabase(dbFilePath, dbPass);
			}

			// check db version, overwrite with new database if required
			Constants.getLog().addToFileOnly(TAG, Type.Debug, "Current database version: " + String.valueOf(database.getVersion()));

	        if (database.needUpgrade(DATABASE_VERSION))
	        {
	        	Constants.getLog().addToFileOnly(TAG, Type.Debug, "Database requires new version: " + String.valueOf(DATABASE_VERSION));
	        	
	        	// save previous credentials and user settings
	        	Constants.getLog().addToFileOnly(TAG, Type.Debug, "Preserving saved credentials and user settings");
	        	String userId = Constants.getData().getSetting(Settings.SET_RovaPortalUserID);
	        	String userPass = Constants.getData().getSetting(Settings.SET_RovaPortalPassword);
	        	Boolean smartConnect = Constants.getData().getFlag(Flags.FLG_AutoConnect);
	        	Boolean notificationsAudio = Constants.getData().getFlag(Flags.FLG_DisableNotificationAudio);
	        	Boolean notificationsVibrate = Constants.getData().getFlag(Flags.FLG_DisableNotificationVibration);
	        	
	        	createDatabase(dbFilePath, dbPass);
	        	
	        	// set previous credentials and user settings
	        	Constants.getLog().addToFileOnly(TAG, Type.Debug, "Restoring saved credentials and user settings");
	        	Constants.getData().setSetting(Settings.SET_RovaPortalUserID, userId, false);
	        	Constants.getData().setSetting(Settings.SET_RovaPortalPassword, userPass, false);
	        	Constants.getData().setFlag(Flags.FLG_AutoConnect, smartConnect, false);
	        	Constants.getData().setFlag(Flags.FLG_DisableNotificationAudio, notificationsAudio, false);
	        	Constants.getData().setFlag(Flags.FLG_DisableNotificationVibration, notificationsVibrate, false);
	        }
		}
		catch (Exception e)
    	{
			Constants.getLog().addToFileOnly(TAG, Type.Error, "initialize()", e);
    	}
	}
	
	
	public boolean isDatabaseOpen()
	{
		try
		{
			if (database != null && database.isOpen() && !database.isReadOnly())
				return true;
		}
		catch (Exception e)
    	{
			// do not log errors in this class until initialization has completed to avoid startup issues
			if (Constants.getHasInitialized())
				Constants.getLog().addToFileOnly(TAG, Type.Error, "isDatabaseOpen", e);
    	}
		
		// default return value
		return false;
	}
	
	
	public void closeDatabase()
	{
		try
		{
			if (database != null && database.isOpen())
				database.close();
		}
		catch (Exception e)
    	{
			Constants.getLog().addToFileOnly(TAG, Type.Error, "closeDatabase", e);
    	}
	}
	
	
	private void createDatabase(String dbFilePath, String dbPass)
	{
		try
		{
			Constants.getLog().addToFileOnly(TAG, Type.Debug, "Creating new database");
			
			// create new database	        
	        
	        File databaseFile = new File(dbFilePath);
	        if (!databaseFile.getParentFile().exists()) 
	        {	            
	        	String dir = databaseFile.getParentFile().getAbsolutePath();
	        	Boolean bdirs = databaseFile.getParentFile().mkdirs();
	        	android.util.Log.e(TAG, "initialize db folder: " + bdirs.toString());	
	        }

	        databaseFile.delete();
	        
			database = SQLiteDatabase.openOrCreateDatabase(databaseFile, dbPass, null);
			
			// execute initialization scripts
			Constants.getLog().addToFileOnly(TAG, Type.Debug, "Creating database schema and default data");
			
			for (String sql : getInitializeDatabaseSql())
				database.execSQL(sql);
			
			// set version
			database.setVersion(DATABASE_VERSION);
			
			Constants.getLog().addToFileOnly(TAG, Type.Debug, "Set database version to " + String.valueOf(DATABASE_VERSION));
		}
		catch (Exception e)
    	{
			Constants.getLog().addToFileOnly(TAG, Type.Error, "createDatabase", e);
    	}
	}
	
	
	private List<String> getInitializeDatabaseSql()
	{
		List<String> initializeDatabaseSql = new ArrayList<String>();
		
		String defaultServerList = null;
		//defaultServerList = "https://emmc.verizon.com/";
		defaultServerList = "https://vzdemo.rova.com/";
		//defaultServerList = "https://vztest1.rova.com/";
		//defaultServerList = "http://208.39.107.27/";	// terremark test server
		//defaultServerList = "http://lebowski.quintech.local/;http://192.168.50.228/;http://10.0.1.6/";
		
		String rovaPortalRegistrationUrl = defaultServerList + "pages/Register.aspx";
		
		// tables
		initializeDatabaseSql.add("CREATE TABLE ApplicationsAssigned (PortalSysID LONG, PackageName TEXT COLLATE NOCASE, PackageDisplayName TEXT COLLATE NOCASE, Version TEXT COLLATE NOCASE, FilePath TEXT COLLATE NOCASE, Mandatory BOOL);");
		initializeDatabaseSql.add("CREATE TABLE ClientSettings (ID TEXT NOT NULL, Value TEXT NULL, DateModified DATE NULL, IsValueSetFromServer BOOL DEFAULT 0, ContainerID LONG DEFAULT 0);");
		initializeDatabaseSql.add("CREATE TABLE Directories (PortalSysID LONG NOT NULL DEFAULT 0, Name TEXT COLLATE NOCASE, Description TEXT COLLATE NOCASE, CredentialSet TEXT COLLATE NOCASE, UserID TEXT COLLATE NOCASE, Password TEXT COLLATE NOCASE, Enabled BOOL, Imported BOOL, IsVerizonDirectory BOOL, GUID TEXT COLLATE NOCASE, SmartConnectRanking INTEGER NOT NULL DEFAULT 0);");
		initializeDatabaseSql.add("CREATE TABLE InstalledShortcuts (PortalSysID LONG NOT NULL, Label TEXT NOT NULL, URL TEXT NOT NULL, IconBase64 TEXT NOT NULL, DateModified DATE NULL);");
		initializeDatabaseSql.add("CREATE TABLE Sessions (SysID INTEGER PRIMARY KEY AUTOINCREMENT  NOT NULL  DEFAULT 0, bSSID TEXT COLLATE NOCASE, SSID TEXT COLLATE NOCASE, StartTime DATE, EndTime DATE, OctetsDownloaded INT, OctetsUploaded INT, IsJailbroken BOOL DEFAULT 0 , Latitude TEXT COLLATE NOCASE, Longitude TEXT COLLATE NOCASE, TimeZone TEXT COLLATE NOCASE, WISPrLoginURL TEXT COLLATE NOCASE, WISPrLogoffURL TEXT COLLATE NOCASE, WISPrbwUserGroup TEXT COLLATE NOCASE, WISPrLocationName TEXT COLLATE NOCASE, WISPrLocationID TEXT COLLATE NOCASE, DeviceModelNumber TEXT COLLATE NOCASE, DeviceType TEXT COLLATE NOCASE, OSType TEXT COLLATE NOCASE, OSVersion TEXT COLLATE NOCASE, ClientVersion TEXT COLLATE NOCASE, SubmittedDate DATE);");
		initializeDatabaseSql.add("CREATE TABLE SmartConnectAllowedConnectionTypes (ID INTEGER NOT NULL, Rank INTEGER NOT NULL, DateModified DATE NULL);");
		initializeDatabaseSql.add("CREATE TABLE Hotspots (ID INTEGER PRIMARY KEY AUTOINCREMENT  NOT NULL  DEFAULT 0, DirectoriesPortalSysID LONG, SSID TEXT COLLATE NOCASE, DisplaySSID TEXT COLLATE NOCASE, Password TEXT COLLATE NOCASE, WEPKeyIndex TEXT COLLATE NOCASE, LoginRequired BOOL DEFAULT 0, AutoLogin BOOL DEFAULT 0, ForceConnect BOOL DEFAULT 0, AggregatorName  TEXT COLLATE NOCASE, OperatorName  TEXT COLLATE NOCASE, DecorationName  TEXT COLLATE NOCASE, LoginURL  TEXT COLLATE NOCASE, ProbeURL  TEXT COLLATE NOCASE, Version FLOAT DEFAULT 0.0);");
		initializeDatabaseSql.add("CREATE TABLE FailedAuthentications (SysID INTEGER PRIMARY KEY AUTOINCREMENT  NOT NULL  DEFAULT 0, DateAdded DATE, SSID TEXT COLLATE NOCASE, MacAddress TEXT COLLATE NOCASE, WISPrXmlLog TEXT COLLATE NOCASE, Latitude TEXT COLLATE NOCASE, Longitude TEXT COLLATE NOCASE, ResultCode TEXT COLLATE NOCASE, SubmittedDate DATE);");
		initializeDatabaseSql.add("CREATE TABLE DeviceCommandsIssued (CommandGUID TEXT COLLATE NOCASE, ID LONG, Parameter TEXT COLLATE NOCASE, DateReceived DATE NULL, DateExecuted DATE NULL, Succeeded BOOL DEFAULT 0, ErrorMessage TEXT COLLATE NOCASE, DateReceivedByPortal DATE NULL);");
		initializeDatabaseSql.add("CREATE TABLE DownloadQueue (ApplicationsAssignedPortalSysID INTEGER, DownloadID LONG, Silent BOOL, DateAdded DATE);");
		initializeDatabaseSql.add("CREATE TABLE Log (Tag TEXT, Type TEXT, Message TEXT, Error TEXT, DateAdded DATE, DateAddedTicks LONG);");
	    initializeDatabaseSql.add("CREATE TABLE BlackWhiteList (Scheme TEXT NULL, Host TEXT NOT NULL, Path TEXT  NULL, Port LONG DEFAULT 0, ContainerID LONG DEFAULT 0, BWValue LONG NOT NULL);");

	    initializeDatabaseSql.add("CREATE TABLE ReportedBlockedURLs (URL TEXT NOT NULL, ContainerID LONG DEFAULT 0, DateAdded DATE);");

		initializeDatabaseSql.add("CREATE TABLE ExchangeSettings (ID TEXT NOT NULL, Host TEXT NULL, Domain TEXT NULL, UserAccountName TEXT NULL, UserEmailAddress TEXT NULL, AllowMove TEXT NULL, SyncInterval Long DEFAULT 0, SyncPeriod LONG DEFAULT 0, useSSL BOOL DEFAULT 1, Port TEXT NULL, EmailCertificate TEXT NULL);");
				
		// indexes
		initializeDatabaseSql.add("CREATE INDEX index_Hotspots_SSID_DirectoriesPortalSysID on Hotspots (SSID COLLATE NOCASE asc, DirectoriesPortalSysID asc);");
	
		// default client settings
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('FLG_RequireDeviceAdministratorAccess', 'false', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('FLG_AlwaysShowTestBuildPrompt', 'false', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('FLG_AlwaysUseDefaultUserIdDecoration', 'false', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('FLG_AlwaysUseDefaultVZWServer', 'false', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('FLG_AlwaysUseDefaultWisprCredential', 'false', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('FLG_AutoConnect', 'true', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('FLG_DisableNotificationAudio', 'false', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('FLG_DisableNotificationVibration', 'false', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('FLG_EnableFullUserInterface', 'true', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('FLG_EnableSessionBytesReporting', 'true', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('FLG_EnableSessionReporting', 'true', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('FLG_EnableSplashScreen', 'false', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('FLG_EnrollmentPolicyResult', 'false', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('FLG_HasInitialized', 'false', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('FLG_HasNotifiedForAvailableHotspot', 'false', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('FLG_HasVerizonNetworkAccount', 'false', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('FLG_RequireDeviceLocationAccess', 'false', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('FLG_UseVerizonWirelessCredentials', 'false', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('FLG_VerifyWebsiteTrust', 'false', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('FLG_EnableDebugMode', 'false', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('SET_ApplicationBuildType', '1', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('SET_ApplicationInstallUUID', '', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('SET_ConfigurationLastUpdatedMillis', '', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('SET_DefaultUserIdDecoration', '', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('SET_DefaultVZWServer', '', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('SET_DefaultWisprPassword', '', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('SET_DefaultWisprUserID', '', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('SET_DeviceMacAddress', '', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('SET_EnrollmentPolicyMessage', '', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('SET_GCMProjectID', '', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('SET_GCMRegistrationID', '', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('SET_LogLevel', 'Info', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('SET_NumberOfLogFilesToPersist', '5', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('SET_RovaPortalPassword', '', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('SET_RovaPortalRegistrationUrl', '" + rovaPortalRegistrationUrl + "', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('SET_RovaPortalServerList', '" + defaultServerList + "', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('SET_RovaPortalUserID', '', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('SET_RovaPortalWebservicePath', 'portalwebservice.asmx', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('SET_SessionTimeoutInHours', '2', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('SET_UpdateConfigurationIntervalInHours', '24', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('SET_UserDebugCode', '5931247', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('SET_VerizonBusinessProbeUrl', 'https://emmc.verizon.com/ecprobe.html', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('SET_VerizonNetworkPassword', '', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('SET_VerizonWirelessProbeUrl', 'http://www.verizonwireless.com', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('SET_VerizonWirelessUserDecoration3G', 'VzW3652987!%USERNAME%@hds.vzw3g.com', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('SET_VerizonWirelessUserDecoration4G', 'VzW3652987!%USERNAME%@hds.vzw4g.com', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('SET_VerizonWirelessWisprAgent', 'Verizon_wispr', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('SET_VZWServerList', 'http://hds.myvzw.com:7476/hds-soap-api/HDS', NULL, NULL);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer) VALUES ('SET_MinimumLocationAccuracyMeters', '80', NULL, NULL);");

		// default PIM client settings
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('FLG_IsRegistered', 'false', NULL, NULL,3);");                    
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('FLG_ShortcutsAllowed', 'true', NULL, NULL,3);");             
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('FLG_WidgetsAllowed', 'true', NULL, NULL,3);");               
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('FLG_PasswordPIN', 'true', NULL, NULL,3);");                  
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('FLG_PasswordSimpleAllowed', 'true', NULL, NULL,3);");         
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('FLG_CopyPasteAllowed','true', NULL, NULL,3);");              
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('FLG_EmailEnabled','true', NULL, NULL,3);");                  
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('FLG_ContactsEnabled', 'true', NULL, NULL,3);");              
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('FLG_BrowserEnabled','true', NULL, NULL,3);");                
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('FLG_TXTMsgEnabled','false', NULL, NULL,3);");                 
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('FLG_CalendarEnabled', 'true', NULL, NULL,3);");              
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('FLG_DialerEnabled','false', NULL, NULL,3);");                 
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('FLG_SupportEnabled', 'false', NULL, NULL,3);");               
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('FLG_EmailAttachmentDownloadEnabled','true', NULL, NULL,3);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('FLG_DeviceWipe','false', NULL, NULL,3);");                    
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('FLG_DeviceBlock',  'false', NULL, NULL,3);");                 
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('FLG_DeviceUnregister', 'false', NULL, NULL,3);");             
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('FLG_EmailAlwaysSSL','true', NULL, NULL,3);");                
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('FLG_EmailUseSSL', 'true', NULL, NULL,3);");                  
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('FLG_EmailUseCertificate', 'false', NULL, NULL,3);");          
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('FLG_JailBroken', 'false', NULL, NULL,3);");                   
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('SET_PIMAuthType', '2', NULL, NULL,3);");            
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('SET_MaxPasswordTries', '0', NULL, NULL,3);");     
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('SET_PasswordLength', '4', NULL, NULL,3);");      
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('SET_PasswordType', '1', NULL, NULL,3);");        
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('SET_PasswordComplexChars','0', NULL, NULL,3);");  
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('SET_PasswordExpires', '0', NULL, NULL,3);");      
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('SET_PasswordHistory', '0', NULL, NULL,3);");     
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('SET_PasswordFailLockout', '30', NULL, NULL,3);"); 
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('SET_SecuritySIMRemoved', '0', NULL, NULL,3);");  
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('SET_SecurityDebugMode','0', NULL, NULL,3);");
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('SET_SecurityRoot', '0', NULL, NULL,3);");        
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('SET_SecurityIME', '0', NULL, NULL,3);");         
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('SET_EmailSignature', 'Secured by Verizon', NULL, NULL,3);");      
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('SET_MaxAttachmentSize',  '0', NULL, NULL,3);");  
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('SET_EmailSyncInterval', '0', NULL, NULL,3);");   
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('SET_EmailSyncPeriod',   '0', NULL, NULL,3);");   
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('SET_ExchangeHost', '', NULL, NULL,3);");        
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('SET_ExchangeDomain', '', NULL, NULL,3);");         		
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('SET_ExchangePort', '443', NULL, NULL,3);");           
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('SET_UserEmailAddress', '', NULL, NULL,3);");        
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('SET_EmailCertificate', '', NULL, NULL,3);");     
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('SET_ExchangeUsername',  '', NULL, NULL,3);");         
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('SET_EmailPassword', '', NULL, NULL,3);");       
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('SET_EmailAuthMode', '2', NULL, NULL,3);");       
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('SET_DeviceID', '1', NULL, NULL,3);");            
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('SET_DeviceSerialNumber',  '1', NULL, NULL,3);"); 
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('SET_DeviceUDID',  '1', NULL, NULL,3);");         
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('SET_DeviceMac', '', NULL, NULL,3);");           
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('SET_DeviceMDN', '', NULL, NULL,3);");           
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('SET_DeviceIMEI',  '1', NULL, NULL,3);");         
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('SET_DeviceMake',   '', NULL, NULL,3);");        
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('SET_DeviceModel',   '', NULL, NULL,3);");       
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('SET_DeviceOS',      'Android', NULL, NULL,3);");       
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('SET_DeviceOSVersion',  '', NULL, NULL,3);");    		                                                                                                               
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('SET_DeviceESN',  '', NULL, NULL,3);");           	
		initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('FLG_AllowEditEmailSettings', 'true', NULL, NULL,3);");         
		
		initializeDatabaseSql.add("INSERT INTO BlackWhiteList (Scheme, Host, Path, Port, ContainerID, BWValue) VALUES (NULL,'facebook.com', NULL, NULL, 3, 1 );");
	    
	    initializeDatabaseSql.add("INSERT INTO BlackWhiteList (Scheme, Host, Path, Port, ContainerID, BWValue) VALUES (NULL,'verizon.com', NULL, NULL, 3, 1 );");
	    initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('SET_BrowserBlackWhite', '1', NULL, NULL, 3);");
	    initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('FLG_CookiesBlocked', 'false', NULL, NULL, 3);");
	    initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('FLG_FileDownloadBlocked', 'false', NULL, NULL, 3);");
	    initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('FLG_PrintingBlocked', 'false', NULL, NULL, 3);");
	    initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('FLG_BrowserCopyPasteBlocked', 'false', NULL, NULL, 3);");
	    initializeDatabaseSql.add("INSERT INTO ClientSettings (ID, Value, DateModified, IsValueSetFromServer, ContainerID) VALUES ('SET_BlockedMessage', 'URL Blocked by your Administrator.', NULL, NULL, 3);");
		
		return initializeDatabaseSql;
	}


	private void closeCursor(android.database.Cursor cursor)
	{
		try
		{
			// close cursor
			cursor.close();
		}
		catch (Exception e)
    	{
			// do not log errors in this class until initialization has completed to avoid startup issues
			if (Constants.getHasInitialized())
				Constants.getLog().addToFileOnly(TAG, Type.Error, "getSetting", e);
    	}
	}
	
	
	@Override
	public String escapeString(String value) 
	{
		// escape invalid characters for SQL statements
		// note this also wraps the string value in single quotes
		
		String returnString = "";
		
		try
		{
			if (value == null)
				returnString = "";
			else
				returnString = DatabaseUtils.sqlEscapeString(value);
		}
		catch (Exception e)
		{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "escapeString", e);
		}
		
		return returnString;
	}

	
	@Override
	public long insertSession(String bSSID, String ssid, boolean isJailbroken, String deviceModelNumber, 
			String deviceType, String osType, String osVersion, String clientVersion,
			String latitude, String longitude)
	{
		long iResult = 0;
		
		try
		{
			// calculate GMT offset
			String timeZone = String.valueOf(Library.getGMTOffSet());
			
			// insert new record
			ContentValues contentValues = new ContentValues();
			contentValues.put("bSSID", bSSID);
			contentValues.put("SSID", ssid);
			contentValues.put("StartTime", Library.getDateTimeGMT(Library.DATE_TIME_FORMAT));
			contentValues.put("EndTime", Library.getDateTimeGMT(Library.DATE_TIME_FORMAT));
			contentValues.put("OctetsDownloaded", 0);
			contentValues.put("OctetsUploaded", 0);
			contentValues.put("IsJailbroken", isJailbroken);
			contentValues.put("Latitude", latitude);
			contentValues.put("Longitude", longitude);
			contentValues.put("TimeZone", timeZone);
			
			// these values must be set after a successful login
			contentValues.putNull("WISPrLoginURL");
			contentValues.putNull("WISPrbwUserGroup");
			contentValues.putNull("WISPrLocationName");
			contentValues.putNull("WISPrLocationID");
			
			contentValues.put("DeviceModelNumber", deviceModelNumber);
			contentValues.put("DeviceType", deviceType);
			contentValues.put("OSType", osType);
			contentValues.put("OSVersion", osVersion);
			contentValues.put("ClientVersion", clientVersion);
			contentValues.putNull("SubmittedDate");
			
			iResult = database.insert("Sessions", null, contentValues);
			
			Constants.getLog().add(TAG, Type.Debug, "Inserted session record for Session ID " + iResult);
		}
		catch (Exception e)
		{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "", e);
		}
		
		return iResult;
	}
	
	
	@Override
	public HotspotInfo isSSIDLoginRequired(String ssid)
	{
		HotspotInfo hotspotInfo = null;
		String query = "";
		android.database.Cursor cur = null;
		
		try
		{
			if (ssid == null || ssid == "")
				return null;
			
			
			// only return results in enabled directories
			query = "SELECT gh.ID " + 
					"FROM 	Hotspots gh " + 
					"			JOIN Directories d " + 
					"				on gh.DirectoriesPortalSysID = d.PortalSysID " + 
					"WHERE 	d.Enabled = 1 " +
					"	and gh.SSID = " + escapeString(ssid) + " " +
					"	and LoginRequired = 1 " +
					"LIMIT 1 ";
			
			cur = database.rawQuery(query, null);
	
			if (cur.getCount() > 0)
			{
				cur.moveToFirst();
				
				hotspotInfo = getHotspotInfo(cur.getInt(0));
			}
			
			if (hotspotInfo != null)
				Constants.getLog().add(TAG, Type.Verbose, "SSID " + ssid + " exists in the available directories");
		}
		catch (Exception e)
    	{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "isSSIDLoginRequired", e);
    	}
		finally
		{
			closeCursor(cur);
		}

		return hotspotInfo;	
	}
	
	
	@Override
	public HotspotInfo isSSIDInDirectory(String ssid, ConnectionType connectionType)
	{
		HotspotInfo hotspotInfo = null;
		String query = "";
		android.database.Cursor cur = null;
		
		try
		{
			if (ssid == null || ssid == "")
				return null;
			
			
			// only return results in enabled directories
			query = "SELECT gh.ID " + 
					"FROM 	Hotspots gh " + 
					"			JOIN Directories d " + 
					"				on gh.DirectoriesPortalSysID = d.PortalSysID " + 
					"WHERE 	d.Enabled = 1 " +
					"	and gh.SSID = " + escapeString(ssid) + " ";
			
			// filter for specified connection type
			if (connectionType == ConnectionType.WiFi_Verizon)
			{
				query += " 	and d.IsVerizonDirectory = 1 ";
			}
			
			else if (connectionType == ConnectionType.WiFi_Corporate)
			{
				query += " 	and d.IsVerizonDirectory != 1 ";
			}
			
			// return top result for specified Unknown type, otherwise return null
			else if (connectionType != ConnectionType.Unknown)
			{
				return null;
			}
			
			
			
			cur = database.rawQuery(query, null);
	
			if (cur.getCount() > 0)
			{
				cur.moveToFirst();
				
				hotspotInfo = getHotspotInfo(cur.getInt(0));
			}
			
			if (hotspotInfo != null)
				Constants.getLog().add(TAG, Type.Verbose, "SSID " + ssid + " exists in the available directories");
		}
		catch (Exception e)
    	{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "isSSIDInDirectory", e);
    	}
		finally
		{
			closeCursor(cur);
		}

		return hotspotInfo;		
	}
	
	
	@Override
	public DirectoryInfo getDirectoryInfo(long portalSysID)
	{
		DirectoryInfo directoryInfo = new DirectoryInfo();
		String query = "";
		android.database.Cursor cur = null;
		
		try
		{
			// select directory record
			query = "SELECT d.PortalSysID, d.Name, d.GUID, d.CredentialSet, d.UserID, d.Password, d.IsVerizonDirectory, d.SmartConnectRanking " + 
					"FROM 	Directories d " +  
					"WHERE 	d.Enabled = 1 " +
					"	and d.PortalSysID = " + String.valueOf(portalSysID) + " " + 
					"LIMIT 1 ";
	
			cur = database.rawQuery(query, null);
		
			if (cur.getCount() == 1)
			{
				cur.moveToFirst();
				
				// set properties
				directoryInfo.portalSysID = cur.getLong(0);
				directoryInfo.displayName = cur.getString(1);
				directoryInfo.guid = cur.getString(2);
				directoryInfo.credentialSet = cur.getString(3);
				directoryInfo.userId = cur.getString(4);
				directoryInfo.password = cur.getString(5);
				
				if (cur.getString(6).equals("1") || cur.getString(6).equalsIgnoreCase("true"))
					directoryInfo.isVerizonDirectory = true;
				else
					directoryInfo.isVerizonDirectory = false;
				
				directoryInfo.smartConnectRanking = cur.getInt(7);
			}
			
			// close
			closeCursor(cur);
			
			
			// if there is no credential found, check other directories for credentials with the same CredentialSet 
			if ((directoryInfo.credentialSet != null && !directoryInfo.credentialSet.equals("")) &&
					directoryInfo.userId == null || directoryInfo.userId.equals("") || 
					directoryInfo.password == null || directoryInfo.equals(""))
			{
				Constants.getLog().add(TAG, Type.Debug, "No credentials found in directory " + directoryInfo.displayName + ". Checking for matching credential set");
				
				query = "SELECT d.PortalSysID, d.Name, d.GUID, d.CredentialSet, d.UserID, d.Password, d.IsVerizonDirectory, d.SmartConnectRanking " + 
						"FROM 	Directories d " + 
						"WHERE 	d.Enabled = 1 " +
						"	and d.UserID IS NOT NULL " +
						"	and d.UserID != '' " +
						"	and d.Password IS NOT NULL " +
						"	and d.Password != '' " +
						"	and d.CredentialSet IS NOT NULL " +
						"	and d.CredentialSet != '' " +
						"	and d.CredentialSet = " + escapeString(directoryInfo.credentialSet) + " " +
						"LIMIT 1 ";
		
				cur = database.rawQuery(query, null);
				
				if (cur.getCount() == 1)
				{
					cur.moveToFirst();
					
					// reset credential properties
					directoryInfo.userId = cur.getString(4);
					directoryInfo.password = cur.getString(5);
					
					Constants.getLog().add(TAG, Type.Debug, "Credential set " + directoryInfo.credentialSet + " set from " + cur.getString(1));
				}
			}
		}
		catch (Exception e)
    	{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "getDirectoryInfo", e);
    	}
		finally
		{
			closeCursor(cur);
		}

		return directoryInfo;		
	}
	
	
	@Override
	public long insertDirectory(DirectoryInfo directoryInfo, boolean enabled, boolean imported)
	{
		long iResult = 0;
		
		try
		{
			// check if directory record already exists
			android.database.Cursor cursor = null;
			String query = 	"SELECT GUID " + 
							"FROM 	Directories " + 
							"WHERE 	GUID = " + escapeString(directoryInfo.guid) + " ";
			
			cursor = database.rawQuery(query, null);
			
			if (cursor.getCount() != 0)
			{
				Constants.getLog().add(TAG, Type.Debug, "Directory record exists for " + directoryInfo.displayName);
			}
			else
			{
				// insert new record
				ContentValues contentValues = new ContentValues();
				contentValues.put("PortalSysID", directoryInfo.portalSysID);
				contentValues.put("Name", directoryInfo.displayName);
				contentValues.put("Description", "");
				contentValues.put("Enabled", enabled);
				contentValues.put("Imported", imported);
				contentValues.put("GUID", directoryInfo.guid);
				contentValues.put("CredentialSet", directoryInfo.credentialSet);
				contentValues.put("UserID", directoryInfo.userId);
				contentValues.put("Password", directoryInfo.password);
				contentValues.put("IsVerizonDirectory", directoryInfo.isVerizonDirectory);
				contentValues.put("SmartConnectRanking", directoryInfo.smartConnectRanking);
				
				iResult = database.insert("Directories", null, contentValues);
				Constants.getLog().add(TAG, Type.Debug, "Inserted directory record for " + directoryInfo.displayName);
			}
			
			cursor.close();
		}
		catch (Exception e)
		{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "insertDirectory", e);
		}
		
		return iResult;
	}
	
	public List<ExchangeSettings> getExchangeSettings()
	{
		List<ExchangeSettings> listExchange = new ArrayList<ExchangeSettings>();
	
		android.database.Cursor cursor = null;
		
		try
		{
			
			String query = 	"SELECT ID, Domain, Host, UserAccountName, UserEmailAddress, AllowMove, SyncInterval, " +
							"SyncPeriod, useSSL, Port, EmailCertificate " +
							"FROM 	ExchangeSettings";
			
			cursor = database.rawQuery(query, null);
			
			Constants.getLog().add(TAG, Type.Debug, String.valueOf(cursor.getCount()) + " ExchangeSettings records retrieved from the database.");
			
			while (cursor.moveToNext())
	        {
	        	try
	        	{
	        		ExchangeSettings ExchangeInfo = new ExchangeSettings();

		        	// get info
	    			ExchangeInfo.AccountDisplayName = cursor.getString(cursor.getColumnIndex("ID"));
	    			ExchangeInfo.Domain = cursor.getString(cursor.getColumnIndex("Domain"));
	    			ExchangeInfo.Host = cursor.getString(cursor.getColumnIndex("Host"));
	    			ExchangeInfo.UserAccountName = cursor.getString(cursor.getColumnIndex("UserAccountName"));
	    			ExchangeInfo.UserEmailAddress = cursor.getString(cursor.getColumnIndex("UserEmailAddress"));
	    			ExchangeInfo.SyncInterval = cursor.getInt(cursor.getColumnIndex("SyncInterval"));
	    			ExchangeInfo.SyncPeriod = cursor.getInt(cursor.getColumnIndex("SyncPeriod"));
	    			ExchangeInfo.AllowMove = cursor.getString(cursor.getColumnIndex("AllowMove"));
	    			
	    			if (cursor.getString(cursor.getColumnIndex("useSSL")).equals("true") ||
	        				cursor.getString(cursor.getColumnIndex("useSSL")).equals("1"))
	        		{
	        			ExchangeInfo.useSSL = true;
	        		}
	        		else
	        		{
	        			ExchangeInfo.useSSL = false;
	        		}

	    			ExchangeInfo.Port = cursor.getString(cursor.getColumnIndex("Port"));
	    			ExchangeInfo.EmailCertificate = cursor.getString(cursor.getColumnIndex("EmailCertificate"));
	        		// add to list
	    			listExchange.add(ExchangeInfo);
	        	}
	    		catch (Exception e)
	        	{
	    			Constants.getLog().add(TAG, Type.Warn, "Problem parsing Exchange record from database.", e);
	        	}
	        }
		}
		catch (Exception e)
    	{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "getExchangeSettings", e);
    	}
		finally
		{
			closeCursor(cursor);
		}
		
		return listExchange;
	}
	
	@Override
	public void updateExchangeInfo(ExchangeSettings Exchange)
	{
		try
		{
			// update record
			ContentValues contentValues = new ContentValues();
			contentValues.put("ID", Exchange.AccountDisplayName);
			contentValues.put("Domain", Exchange.Domain);
			contentValues.put("Host", Exchange.Host);
			contentValues.put("UserAccountName", Exchange.UserAccountName);
			contentValues.put("UserEmailAddress", Exchange.UserEmailAddress);
			contentValues.put("AllowMove", Exchange.AllowMove);
			contentValues.put("SyncInterval", Exchange.SyncInterval);
			contentValues.put("SyncPeriod", Exchange.SyncPeriod);
			contentValues.put("useSSL", Exchange.useSSL);
			contentValues.put("Port", Exchange.Port);
			contentValues.put("EmailCertificate", Exchange.EmailCertificate);
			

			database.update("ExchangeSettings", contentValues, "ID = ?", new String[] { Exchange.AccountDisplayName });
			
			Constants.getLog().add(TAG, Type.Verbose, "Updated Exchange name for " + Exchange.AccountDisplayName);
		}
		catch (Exception e)
		{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "updateExchangeInfo", e);
		}
	}
	
	@Override
	public long insertExchange(ExchangeSettings Exchange)
	{
		long iResult = 0;
		
		try
		{
			// check if directory record already exists
			android.database.Cursor cursor = null;
			String query = 	"SELECT ID " + 
							"FROM 	ExchangeSettings " + 
							"WHERE 	ID = " + escapeString(Exchange.AccountDisplayName) + " ";
			
			cursor = database.rawQuery(query, null);
			
			if (cursor.getCount() != 0)
			{
				updateExchangeInfo(Exchange);
				Constants.getLog().add(TAG, Type.Debug, "Exchange record exists for " + Exchange.AccountDisplayName);
			}
			else
			{
				// insert new record

				ContentValues contentValues = new ContentValues();
				contentValues.put("ID", Exchange.AccountDisplayName);
				contentValues.put("Domain", Exchange.Domain);
				contentValues.put("Host", Exchange.Host);
				contentValues.put("UserAccountName", Exchange.UserAccountName);
				contentValues.put("UserEmailAddress", Exchange.UserEmailAddress);
				contentValues.put("AllowMove", Exchange.AllowMove);
				contentValues.put("SyncInterval", Exchange.SyncInterval);
				contentValues.put("SyncPeriod", Exchange.SyncPeriod);
				contentValues.put("useSSL", Exchange.useSSL);
				contentValues.put("Port", Exchange.Port);
				contentValues.put("EmailCertificate", Exchange.EmailCertificate);
				iResult = database.insert("ExchangeSettings", null, contentValues);
				Constants.getLog().add(TAG, Type.Debug, "Inserted Exchange record for " + Exchange.AccountDisplayName);
			}
			
			cursor.close();
		}
		catch (Exception e)
		{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "insertExchange", e);
		}
		
		return iResult;
	}
	
	@Override
	public void setDirectoryEnabled(DirectoryInfo directoryInfo)
	{
		try
		{
			// update record
			ContentValues contentValues = new ContentValues();
			contentValues.put("Enabled", true);
			
			database.update("Directories", contentValues, "GUID = ?", new String[] { directoryInfo.guid });
			
			Constants.getLog().add(TAG, Type.Verbose, "Enabled directory " + directoryInfo.displayName);
		}
		catch (Exception e)
		{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "setDirectoryEnabled", e);
		}
	}
	
	
	@Override
	public void setDirectorySmartConnectRanking(long directoriesPortalSysID, int rank)
	{
		try
		{
			// update record
			ContentValues contentValues = new ContentValues();
			contentValues.put("SmartConnectRanking", rank);
			
			database.update("Directories", contentValues, "PortalSysID = ?", new String[] { String.valueOf(directoriesPortalSysID) });
			
			Constants.getLog().add(TAG, Type.Verbose, "Set directory rank for SysID " + String.valueOf(directoriesPortalSysID) + " to " + String.valueOf(rank));
		}
		catch (Exception e)
		{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "setDirectorySmartConnectRanking", e);
		}
	}
	
	
	@Override
	public void setImportedDirectoriesDisabled()
	{
		try
		{
			// update all imported directories as disabled
			ContentValues contentValues = new ContentValues();
			contentValues.put("Enabled", false);
			
			database.update("Directories", contentValues, "Imported = ?", new String[] { "1" });
			
			Constants.getLog().add(TAG, Type.Verbose, "Disabled imported directories");
		}
		catch (Exception e)
		{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "setImportedDirectoriesDisabled", e);
		}
	}
	
	
	@Override
	public void updateDirectoryInfo(DirectoryInfo directoryInfo)
	{
		try
		{
			// update record
			ContentValues contentValues = new ContentValues();
			contentValues.put("Name", directoryInfo.displayName);
			contentValues.put("Description", "");
			contentValues.put("CredentialSet", directoryInfo.credentialSet);
			contentValues.put("UserID", directoryInfo.userId);
			contentValues.put("Password", directoryInfo.password);
			contentValues.put("IsVerizonDirectory", directoryInfo.isVerizonDirectory);
			contentValues.put("SmartConnectRanking", directoryInfo.smartConnectRanking);
			
			database.update("Directories", contentValues, "GUID = ?", new String[] { directoryInfo.guid });
			
			Constants.getLog().add(TAG, Type.Verbose, "Updated directory name for " + directoryInfo.displayName);
		}
		catch (Exception e)
		{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "updateDirectoryName", e);
		}
	}
	

	@Override
	public void insertHotspot(String directoryGUID, HotspotInfo hotspot)
	{
		String query;
		
		
		try
		{
			// only return results in enabled directories
			query = "INSERT INTO Hotspots " + 
					"(SSID, DirectoriesPortalSysID, DisplaySSID, Password, WEPKeyIndex, LoginRequired, AutoLogin, ForceConnect, AggregatorName, OperatorName, DecorationName, LoginURL, ProbeURL, Version) " + 
					
					"SELECT 	" + escapeString(hotspot.ssid) + " as SSID, " + 
					"			(SELECT PortalSysID FROM Directories WHERE GUID = " + escapeString(directoryGUID) + ") as DirectoriesPortalSysID, " +
					"			" + escapeString(hotspot.displaySSID) + " as DisplaySSID, " + 
					"			" + escapeString(hotspot.password) + " as Password, " + 
					"			" + escapeString(String.valueOf(hotspot.wepKeyIndex)) + " as WEPKeyIndex, " + 
					"			" + convertToString(hotspot.loginRequired) + " as LoginRequired, " + 
					"			" + convertToString(hotspot.autoLogin) + " as AutoLogin, " + 
					"			" + convertToString(hotspot.forceConnect) + " as ForceConnect, " +
					"			" + escapeString(hotspot.aggregatorName) + " as AggregatorName, " + 
					"			" + escapeString(hotspot.operatorName) + " as OperatorName, " + 
					"			" + escapeString(hotspot.decorationName) + " as DecorationName, " + 
					"			" + escapeString(hotspot.loginURL) + " as LoginURL, " + 
					"			" + escapeString(hotspot.probeURL) + " as ProbeURL, " + 
					"			" + String.valueOf(hotspot.version) + " as Version ";

			database.execSQL(query);
		}
		catch (Exception e)
		{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "insertHotspot", e);
		}
	}
	

	@Override
	public void deleteHotspots(String directoryGUID)
	{
		String query;
		
		
		try
		{
			// delete all data for the specified directory
			query = "DELETE  	" +
					"FROM 		Hotspots " +
					"WHERE		DirectoriesPortalSysID IN " +
					"				( SELECT PortalSysID FROM Directories WHERE GUID = " + escapeString(directoryGUID) + ") ";
	
			database.execSQL(query);
			
			Constants.getLog().add(TAG, Type.Debug, "Deleted all hotspots for directory: " + directoryGUID);
		}
		catch (Exception e)
		{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "deleteHotspots", e);
		}
	}
	

	@Override
	public float getHotspotVersion(String directoryGUID)
	{
		float fReturn = -1.0F;
		String query = "";
		android.database.Cursor cur = null;
		
		try
		{
			// return highest hotspot version for the specified directory
			query = "SELECT MAX(gh.Version), COUNT(gh.Version) " + 
					"FROM 	Hotspots gh " + 
					"			JOIN Directories d " + 
					"				on gh.DirectoriesPortalSysID = d.PortalSysID " + 
					"WHERE 	d.GUID = " + escapeString(directoryGUID) + " ";
			
			cur = database.rawQuery(query, null);
	
			if (cur.getCount() == 1)
			{
				cur.moveToFirst();
				float maxVersion = cur.getFloat(0);
				int hotspotCount = cur.getInt(1);
				
				// set the highest version in the directory if it contains hotspots
				if (hotspotCount > 0)
					fReturn = maxVersion;
			}
		}
		catch (Exception e)
    	{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "getHotspotVersion", e);
    	}
		finally
		{
			closeCursor(cur);
		}

		return fReturn;		
	}
	

	@Override
	public HotspotInfo getHotspotInfo(int hotspotsID)
	{
		HotspotInfo hotspotInfo = new HotspotInfo();
		android.database.Cursor cursor = null;
		
		try
		{
			// select top record for specified SSID
			String query = 	"SELECT h.ID, h.DirectoriesPortalSysID, h.SSID, h.DisplaySSID, h.Password, h.WEPKeyIndex, h.LoginRequired, h.AutoLogin, h.ForceConnect, h.AggregatorName, " +
							"		h.OperatorName, h.DecorationName, h.LoginURL, h.ProbeURL, h.Version " +
							"FROM 	Hotspots h " +
							"WHERE 	ID = " + String.valueOf(hotspotsID) + " " +
							"ORDER BY h.Version DESC " + 
							"LIMIT 1 ";
			
			cursor = database.rawQuery(query, null);
			
			while (cursor.moveToNext())
	        {
	        	try
	        	{
	        		// get info
	        		hotspotInfo.directoriesPortalSysID = cursor.getLong(cursor.getColumnIndex("DirectoriesPortalSysID"));
	        		hotspotInfo.id = cursor.getInt(cursor.getColumnIndex("ID"));
	        		hotspotInfo.ssid = cursor.getString(cursor.getColumnIndex("SSID"));
	        		hotspotInfo.displaySSID = cursor.getString(cursor.getColumnIndex("DisplaySSID"));
	        		hotspotInfo.password = cursor.getString(cursor.getColumnIndex("Password"));
	        		
	        		hotspotInfo.wepKeyIndex = 0;
	        		
	        		try
	        		{
	        			hotspotInfo.wepKeyIndex = Integer.parseInt(cursor.getString(cursor.getColumnIndex("WEPKeyIndex")));
	        		}
	        		catch (Exception e) { }
	        		
	        		
	        		if (cursor.getString(cursor.getColumnIndex("LoginRequired")).equals("true") ||
	        				cursor.getString(cursor.getColumnIndex("LoginRequired")).equals("1"))
	        		{
	        			hotspotInfo.loginRequired = true;
	        		}
	        		else
	        		{
	        			hotspotInfo.loginRequired = false;
	        		}
	        		
	        		
	        		if (cursor.getString(cursor.getColumnIndex("AutoLogin")).equals("true") ||
	        				cursor.getString(cursor.getColumnIndex("AutoLogin")).equals("1"))
	        		{
	        			hotspotInfo.autoLogin = true;
	        		}
	        		else
	        		{
	        			hotspotInfo.autoLogin = false;
	        		}
	        		
	        		
	        		if (cursor.getString(cursor.getColumnIndex("ForceConnect")).equals("true") ||
	        				cursor.getString(cursor.getColumnIndex("ForceConnect")).equals("1"))
	        		{
	        			hotspotInfo.forceConnect = true;
	        		}
	        		else
	        		{
	        			hotspotInfo.forceConnect = false;
	        		}
	        		
	        		
	        		hotspotInfo.aggregatorName = cursor.getString(cursor.getColumnIndex("AggregatorName"));
	        		hotspotInfo.operatorName = cursor.getString(cursor.getColumnIndex("OperatorName"));
	        		hotspotInfo.decorationName = cursor.getString(cursor.getColumnIndex("DecorationName"));
	        		hotspotInfo.loginURL = cursor.getString(cursor.getColumnIndex("LoginURL"));
	        		hotspotInfo.probeURL = cursor.getString(cursor.getColumnIndex("ProbeURL"));
	        		
	        		// break after parsing first result
	        		break;
	        	}
	    		catch (Exception e)
	        	{
	    			if (Constants.getHasInitialized())
	    				Constants.getLog().add(TAG, Type.Warn, "Problem parsing hotspot record from database.", e);
	        	}
	        }
		}
		catch (Exception e)
    	{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "getHotspotInfo", e);
    	}
		finally
		{
			closeCursor(cursor);
		}
		
		return hotspotInfo;
	}
	

	@Override
	public List<SessionInfo> getNewSessions()
	{
		List<SessionInfo> listSessions = new ArrayList<SessionInfo>();
		android.database.Cursor cursor = null;
		
		try
		{
			// select unsubmitted records
			
			// create filter to ignore any current sessions
			String sqlSessionFilter = "";
			
			if (Constants.getSession() != null)
				sqlSessionFilter = " 	and SysID != " + String.valueOf(Constants.getSession().getSessionSysID()) + " ";
			
			
			// limit to top 100 results, this is the maximum the directory server will process at one time
			String query = 	"SELECT SysID, bSSID, SSID, StartTime, EndTime, OctetsDownloaded, OctetsUploaded, IsJailbroken, Latitude, " +
							"		Longitude, TimeZone, WISPrLoginURL, WISPrLogoffURL, WISPrbwUserGroup, WISPrLocationName, WISPrLocationID, DeviceModelNumber, " + 
							"		DeviceType, OSType, OSVersion, ClientVersion, SubmittedDate " +
							"FROM 	Sessions " +
							"WHERE 	(SubmittedDate IS NULL or SubmittedDate = '') " +
									sqlSessionFilter +
							"ORDER BY EndTime " + 
							"LIMIT 100 ";
			
			cursor = database.rawQuery(query, null);
			
			Constants.getLog().add(TAG, Type.Debug, String.valueOf(cursor.getCount()) + " session records retrieved from the database.");
			
			while (cursor.moveToNext())
	        {
	        	try
	        	{
	        		SessionInfo sessionInfo = new SessionInfo();
	        		
		        	// get info
	        		sessionInfo.sysID = cursor.getInt(cursor.getColumnIndex("SysID"));
	        		sessionInfo.bssid = cursor.getString(cursor.getColumnIndex("bSSID"));
	        		sessionInfo.ssid = cursor.getString(cursor.getColumnIndex("SSID"));
	        		sessionInfo.startTime = cursor.getString(cursor.getColumnIndex("StartTime"));
	        		sessionInfo.endTime = cursor.getString(cursor.getColumnIndex("EndTime"));
	        		sessionInfo.octetsDownloaded = cursor.getLong(cursor.getColumnIndex("OctetsDownloaded"));
	        		sessionInfo.octetsUploaded = cursor.getLong(cursor.getColumnIndex("OctetsUploaded"));
	        		sessionInfo.latitude = cursor.getString(cursor.getColumnIndex("Latitude"));
	        		sessionInfo.longitude = cursor.getString(cursor.getColumnIndex("Longitude"));
	        		sessionInfo.timeZone = cursor.getString(cursor.getColumnIndex("TimeZone"));
	        		sessionInfo.wisprLoginURL = cursor.getString(cursor.getColumnIndex("WISPrLoginURL"));
	        		sessionInfo.wisprLogoffURL = cursor.getString(cursor.getColumnIndex("WISPrLogoffURL"));
	        		sessionInfo.wisprbwUserGroup = cursor.getString(cursor.getColumnIndex("WISPrbwUserGroup"));
	        		sessionInfo.wisprLocationName = cursor.getString(cursor.getColumnIndex("WISPrLocationName"));
	        		sessionInfo.wisprLocationID = cursor.getString(cursor.getColumnIndex("WISPrLocationID"));
	        		sessionInfo.deviceModelNumber = cursor.getString(cursor.getColumnIndex("DeviceModelNumber"));
	        		sessionInfo.deviceType = cursor.getString(cursor.getColumnIndex("DeviceType"));
	        		sessionInfo.osType = cursor.getString(cursor.getColumnIndex("OSType"));
	        		sessionInfo.osVersion = cursor.getString(cursor.getColumnIndex("OSVersion"));
	        		sessionInfo.clientVersion = cursor.getString(cursor.getColumnIndex("ClientVersion"));
	        		
	        		if (cursor.getString(cursor.getColumnIndex("IsJailbroken")).equals("true") ||
	        				cursor.getString(cursor.getColumnIndex("IsJailbroken")).equals("1"))
	        		{
	        			sessionInfo.isJailBroken = true;
	        		}
	        		else
	        		{
	        			sessionInfo.isJailBroken = false;
	        		}
	        		
	        		
	        		// add to list
	        		listSessions.add(sessionInfo);
	        	}
	    		catch (Exception e)
	        	{
	    			Constants.getLog().add(TAG, Type.Warn, "Problem parsing session record from database.", e);
	        	}
	        }
		}
		catch (Exception e)
    	{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "getNewSessions", e);
    	}
		finally
		{
			closeCursor(cursor);
		}
		
		
		return listSessions;
	}
	

	@Override
	public List<FailedAuthenticationInfo> getNewFailedAuthentications()
	{
		List<FailedAuthenticationInfo> listFailedAuthenticaions = new ArrayList<FailedAuthenticationInfo>();
		android.database.Cursor cursor = null;
		
		try
		{
			// select unsubmitted records
			
			// limit to top 500 results, this is the maximum the directory server will process at one time
			String query = 	"SELECT SysID, SSID, MacAddress, WISPrXmlLog, Latitude, Longitude, ResultCode " +
							"FROM 	FailedAuthentications " +
							"WHERE 	SubmittedDate IS NULL or SubmittedDate = '' " +
							"ORDER BY DateAdded " + 
							"LIMIT 500 ";
			
			cursor = database.rawQuery(query, null);
			
			Constants.getLog().add(TAG, Type.Debug, String.valueOf(cursor.getCount()) + " failed authentication records retrieved from the database.");
			
			while (cursor.moveToNext())
	        {
	        	try
	        	{
	        		FailedAuthenticationInfo failedAuthentication = new FailedAuthenticationInfo();
	        		
		        	// get info
	        		failedAuthentication.sysID = cursor.getInt(cursor.getColumnIndex("SysID"));
	        		failedAuthentication.ssid = cursor.getString(cursor.getColumnIndex("SSID"));
	        		failedAuthentication.macAddress = cursor.getString(cursor.getColumnIndex("MacAddress"));
	        		failedAuthentication.wisprXmlLog = cursor.getString(cursor.getColumnIndex("WISPrXmlLog"));
	        		failedAuthentication.latitude = cursor.getString(cursor.getColumnIndex("Latitude"));
	        		failedAuthentication.longitude = cursor.getString(cursor.getColumnIndex("Longitude"));
	        		failedAuthentication.resultCode = cursor.getString(cursor.getColumnIndex("ResultCode"));
	        		
	        		// add to list
	        		listFailedAuthenticaions.add(failedAuthentication);
	        	}
	    		catch (Exception e)
	        	{
	    			Constants.getLog().add(TAG, Type.Warn, "Problem parsing failed authentication record from database.", e);
	        	}
	        }
		}
		catch (Exception e)
    	{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "getNewFailedAuthentications", e);
    	}
		finally
		{
			closeCursor(cursor);
		}
		
		return listFailedAuthenticaions;
	}
	

	@Override
	public void setFailedAuthenticationsSubmittedDate(List<FailedAuthenticationInfo> listFailedAuthentications)
	{		
		try
		{
			// set value
			ContentValues contentValues = new ContentValues();
			contentValues.put("SubmittedDate", Library.getDateTimeGMT(Library.DATE_TIME_FORMAT));
			
			// update records
			for (FailedAuthenticationInfo fa : listFailedAuthentications)
			{
				database.update("FailedAuthentications", contentValues, "SysID = ?", new String[] { String.valueOf(fa.sysID) });
				Constants.getLog().add(TAG, Type.Verbose, "Updated submission date for Failed Authentication ID " + String.valueOf(fa.sysID));
			}
		}
		catch (Exception e)
    	{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "setFailedAuthenticationsSubmittedDate", e);
    	}
	}
	

	@Override
	public long insertLog(String tag, String type, String message, String error)
	{
		long iResult = 0;
		
		try
		{
			// insert new record
			ContentValues contentValues = new ContentValues();
			contentValues.put("Tag", tag);
			contentValues.put("Type", type);
			contentValues.put("Message", message);
			contentValues.put("Error", error);
			contentValues.put("DateAdded", Library.getDateTimeGMT(Library.DATE_TIME_FORMAT));
			contentValues.put("DateAddedTicks", System.currentTimeMillis());
			
			iResult = database.insert("Log", null, contentValues);
		}
		catch (Exception e)
		{
			// do not log failed logging attempts
		}
		
		return iResult;
	}
	
	
	@Override
	public void deleteOldLogs()
	{
		String query;
		
		
		try
		{
			// delete all logs older than the specified time
			
			// number of milliseconds in 3 days
    		long deleteIntervalTicks = 1000 * 60 * 60 * 24 * 3;
			
			query = "DELETE  	" +
					"FROM 		Log " +
					"WHERE		DateAddedTicks < " + String.valueOf(System.currentTimeMillis() - deleteIntervalTicks) + " ";
	
			database.execSQL(query);
			
			Constants.getLog().add(TAG, Type.Debug, "Deleted log records older than " + String.valueOf(System.currentTimeMillis() - deleteIntervalTicks));
		}
		catch (Exception e)
		{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "deleteHotspots", e);
		}
	}
	

	@Override
	public void insertFailedAuthentication(java.util.Date dateAdded, String latitude, String longitude, String ssid, String macAddress, String wisprXmlLog, String resultCode)
	{
		try
		{
			// insert new record
			ContentValues contentValues = new ContentValues();
			contentValues.put("DateAdded", Library.getDateTimeGMT(Library.DATE_TIME_FORMAT));
			contentValues.put("SSID", ssid);
			contentValues.put("MacAddress", macAddress);
			contentValues.put("WISPrXmlLog", wisprXmlLog);
			contentValues.put("Latitude", latitude);
			contentValues.put("Longitude", longitude);
			contentValues.put("ResultCode", resultCode);
			
			database.insert("FailedAuthentications", null, contentValues);
			
			Constants.getLog().add(TAG, Type.Debug, "Inserted failed authentication record for SSID " + ssid);
		}
		catch (Exception e)
		{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "insertFailedAuthentication", e);
		}
	}
	

	@Override
	public void updateSessionEndTime(long sessionSysID)
	{
		try
		{
			// update record
			ContentValues contentValues = new ContentValues();
			contentValues.put("EndTime", Library.getDateTimeGMT(Library.DATE_TIME_FORMAT));
			
			database.update("Sessions", contentValues, "SysID = ?", new String[] { String.valueOf(sessionSysID) });
			
			Constants.getLog().add(TAG, Type.Verbose, "Updated session end time for Session ID " + String.valueOf(sessionSysID));
		}
		catch (Exception e)
		{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "updateSessionEndTime", e);
		}
	}
	
	
	@Override
	public void updateSessionWISPrValues(long sessionSysID, String wisprLoginURL, String wisprLogoffURL, String wisprbwUserGroup, String wisprLocationName, String wisprLocationID)
	{
		try
		{
			// update record
			ContentValues contentValues = new ContentValues();
			contentValues.put("WISPrLoginURL", wisprLoginURL);
			contentValues.put("WISPrLogoffURL", wisprLogoffURL);
			contentValues.put("WISPrbwUserGroup", wisprbwUserGroup);
			contentValues.put("WISPrLocationName", wisprLocationName);
			contentValues.put("WISPrLocationID", wisprLocationID);
			
			database.update("Sessions", contentValues, "SysID = ?", new String[] { String.valueOf(sessionSysID) });
			
			Constants.getLog().add(TAG, Type.Debug, "Updated WISPr values for Session ID " + String.valueOf(sessionSysID));
		}
		catch (Exception e)
		{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "updateSessionWISPrValues", e);
		}
	}
	

	@Override
	public void addSessionNetworkTraffic(long sessionSysID, long addOctetsReceived, long addOctetsUploaded)
	{
		String query;
		
		try
		{
			query = "UPDATE 	Sessions " + 
					"SET 		OctetsDownloaded = OctetsDownloaded + " + String.valueOf(addOctetsReceived) + ", " + 
					"			OctetsUploaded = OctetsUploaded + " + String.valueOf(addOctetsUploaded) + " " + 
					"WHERE 		SysID  = " + String.valueOf(sessionSysID) + "  ";
					
			database.execSQL(query);
			
			Constants.getLog().add(TAG, Type.Verbose, "Updated network traffic usage for Session ID " + String.valueOf(sessionSysID));
		}
		catch (Exception e)
		{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "addSessionNetworkTraffic", e);
		}
	}
	
	
	@Override
	public SessionInfo getSession(String ssid, boolean filterRecent)
	{
		SessionInfo sessionInfo = new SessionInfo();
		String query;
		android.database.Cursor cursor = null;
		
		try
		{
			// if flag is true, only search for recent sessions
			String strFilterRecent = "";
			
			if (filterRecent)
				strFilterRecent = " and  ((strftime('%s','" + Library.getDateTimeGMT(Library.DATE_TIME_FORMAT) + "') - strftime('%s', EndTime)) / 3600) < " + String.valueOf(Constants.getSessionTimeoutInHours()) + " ";
			
			
			query = "SELECT  	SysID, bSSID, SSID, StartTime, EndTime, OctetsDownloaded, OctetsUploaded, IsJailbroken, " +
					" 			Latitude, Longitude, TimeZone, WISPrLoginURL, WISPrLogoffURL, WISPrbwUserGroup, WISPrLocationName, WISPrLocationID, " +
					" 			DeviceModelNumber, DeviceType, OSType, OSVersion, ClientVersion, SubmittedDate " +	
					"FROM 		Sessions " +
					"WHERE 		SSID = " + escapeString(ssid) + " " +
					"		and (SubmittedDate IS NULL or SubmittedDate = '') " +
								strFilterRecent +
					"ORDER BY EndTime DESC, StartTime DESC " +
					"LIMIT 1 ";
			
			cursor = database.rawQuery(query, null);

			if (cursor.getCount() == 1)
			{
				cursor.moveToFirst();
				
				// fill session info object
				sessionInfo.sysID = cursor.getInt(cursor.getColumnIndex("SysID"));
				sessionInfo.bssid = cursor.getString(cursor.getColumnIndex("bSSID"));
				sessionInfo.ssid = cursor.getString(cursor.getColumnIndex("SSID"));
				sessionInfo.startTime = cursor.getString(cursor.getColumnIndex("StartTime"));
				sessionInfo.endTime = cursor.getString(cursor.getColumnIndex("EndTime"));
				sessionInfo.octetsDownloaded = cursor.getLong(cursor.getColumnIndex("OctetsDownloaded"));
				sessionInfo.octetsUploaded = cursor.getLong(cursor.getColumnIndex("OctetsUploaded"));
				
				if (cursor.getString(cursor.getColumnIndex("IsJailbroken")).equals("true") ||
        				cursor.getString(cursor.getColumnIndex("IsJailbroken")).equals("1"))
        		{
        			sessionInfo.isJailBroken = true;
        		}
        		else
        		{
        			sessionInfo.isJailBroken = false;
        		}

				sessionInfo.latitude = cursor.getString(cursor.getColumnIndex("Latitude"));
				sessionInfo.longitude = cursor.getString(cursor.getColumnIndex("Longitude"));
				sessionInfo.timeZone = cursor.getString(cursor.getColumnIndex("TimeZone"));
				sessionInfo.wisprLoginURL = cursor.getString(cursor.getColumnIndex("WISPrLoginURL"));
				sessionInfo.wisprLogoffURL = cursor.getString(cursor.getColumnIndex("WISPrLogoffURL"));
				sessionInfo.wisprbwUserGroup = cursor.getString(cursor.getColumnIndex("WISPrbwUserGroup"));
				sessionInfo.wisprLocationName = cursor.getString(cursor.getColumnIndex("WISPrLocationName"));
				sessionInfo.wisprLocationID = cursor.getString(cursor.getColumnIndex("WISPrLocationID"));
				sessionInfo.deviceModelNumber = cursor.getString(cursor.getColumnIndex("DeviceModelNumber"));
				sessionInfo.deviceType = cursor.getString(cursor.getColumnIndex("DeviceType"));
				sessionInfo.osType = cursor.getString(cursor.getColumnIndex("OSType"));
				sessionInfo.osVersion = cursor.getString(cursor.getColumnIndex("OSVersion"));
				sessionInfo.clientVersion = cursor.getString(cursor.getColumnIndex("ClientVersion"));
			}
		}
		catch (Exception e)
    	{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "getSession", e);
			
			// return null value if exception was hit
			sessionInfo = null;
    	}
		finally
		{
			closeCursor(cursor);
		}
		
		return sessionInfo;
	}
	

	@Override
	public void setSessionsSubmittedDate(List<SessionInfo> listSessions)
	{		
		try
		{
			// set value
			ContentValues contentValues = new ContentValues();
			contentValues.put("SubmittedDate", Library.getDateTimeGMT(Library.DATE_TIME_FORMAT));
			
			// update records
			for (SessionInfo si : listSessions)
			{
				database.update("Sessions", contentValues, "SysID = ?", new String[] { String.valueOf(si.sysID) });
				Constants.getLog().add(TAG, Type.Verbose, "Updated submission date for Session ID " + String.valueOf(si.sysID));
			}
		}
		catch (Exception e)
    	{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "setSessionsSubmittedDate", e);
    	}
	}
	

	@Override
	public void insertIgnoredHotspot(String ssid)
	{
		// not implemented for VZB
	}
	

	@Override
	public void deleteIgnoredHotspot(String ssid)
	{
		// not implemented for VZB
	}
	

	@Override
	public boolean isIgnoredHotspot(String ssid, int ignoredHotspotDurationInHours)
	{
		// not implemented for VZB
		return false;
	}
	
	
	public boolean isExistingDeviceCommandIssued(String commandGuid)
	{
		boolean returnValue = false;
		android.database.Cursor cur = null;
		String query;
		
		try
		{
			query = "SELECT  	COUNT(CommandGUID) " +
					"FROM 		DeviceCommandsIssued " +
					"WHERE 		CommandGUID = " + escapeString(commandGuid) + " ";
	
			cur = database.rawQuery(query, null);
		
			if (cur.getCount() == 1)
			{
				cur.moveToFirst();
				
				if (cur.getInt(0) > 0)
					returnValue = true;
			}
			
			Constants.getLog().add(TAG, Type.Verbose, "Issued device command " + commandGuid + " exists in the database: " + String.valueOf(returnValue));
		}
		catch (Exception e)
		{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "isIgnoredHotspot", e);
		}
		finally
		{
			closeCursor(cur);
		}
		

		return returnValue;
	}
	
	
	public void insertDeviceCommandsIssued(List<DeviceCommandIssuedInfo> issuedDeviceCommands)
	{
		try
		{
			if (issuedDeviceCommands == null)
			{
				return;
			}
			
			for (DeviceCommandIssuedInfo commandInfo : issuedDeviceCommands)
			{
				insertDeviceCommandIssued(commandInfo.commandGuid, commandInfo.command, commandInfo.parameter);
			}
		}
		catch (Exception e)
		{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "insertDeviceCommandsIssued", e);
		}
	}
	
	
	public void insertDeviceCommandIssued(String commandGuid, DeviceCommands command, String parameter)
	{
		try
		{
			// check if CommandGUID already exists
			if (!isExistingDeviceCommandIssued(commandGuid) && commandGuid != null && !commandGuid.equals(""))
			{
				// insert new record if CommandGUID does not exist
				ContentValues contentValues = new ContentValues();
				contentValues.put("CommandGUID", commandGuid);
				contentValues.put("ID", command.getValue());
				contentValues.put("Parameter", parameter);
				contentValues.put("DateReceived", Library.getDateTimeGMT(Library.DATE_TIME_FORMAT));
				contentValues.putNull("DateExecuted");
				contentValues.put("Succeeded", false);
				contentValues.putNull("ErrorMessage");
				contentValues.putNull("DateReceivedByPortal");
				
				database.insert("DeviceCommandsIssued", null, contentValues);
				
				Constants.getLog().add(TAG, Type.Verbose, "Queued issued device command " + command.toString());
			}
			else
			{
				Constants.getLog().add(TAG, Type.Verbose, "Skipped queuing issued device command " + command.toString() + " because it already exists");
			}
		}
		catch (Exception e)
		{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "insertDeviceCommandIssued", e);
		}
	}
	
	
	public void setDeviceCommandIssuedExecuted(String commandGuid, Boolean succeeded, String errorMessage)
	{
		try
		{
			// update record
			ContentValues contentValues = new ContentValues();
			contentValues.put("Succeeded", succeeded);
			contentValues.put("ErrorMessage", errorMessage);
			contentValues.put("DateExecuted", Library.getDateTimeGMT(Library.DATE_TIME_FORMAT));
			
			database.update("DeviceCommandsIssued", contentValues, "CommandGUID = ?", new String[] { commandGuid });
			
			Constants.getLog().add(TAG, Type.Verbose, "Logged issued device command as executed: " + commandGuid);
		}
		catch (Exception e)
		{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "setDeviceCommandIssuedExecuted", e);
		}
	}
	
	
	public List<DeviceCommandIssuedInfo> getDeviceCommandsIssued(boolean getUnsentExecuted, boolean getNotExecuted)
	{
		List<DeviceCommandIssuedInfo> listDeviceCommandsIssued = new ArrayList<DeviceCommandIssuedInfo>();
		android.database.Cursor cursor = null;
		
		
		try
		{
			// select records that have been exectuted but not received by the portal
			Constants.getLog().add(TAG, Type.Debug, "Selecting issued device commands.");
			Constants.getLog().add(TAG, Type.Debug, "getUnsentExecuted: " + String.valueOf(getUnsentExecuted));
			Constants.getLog().add(TAG, Type.Debug, "getNotExecuted: " + String.valueOf(getNotExecuted));
			
			String query = "";
			
			if (getUnsentExecuted)
			{
				query = 	"SELECT CommandGUID, ID, Parameter, DateReceived, DateExecuted, Succeeded, ErrorMessage, DateReceivedByPortal " +
							"FROM 	DeviceCommandsIssued " +
							"WHERE 	DateReceivedByPortal IS NULL " +
							"	and DateExecuted IS NOT NULL " +
							"ORDER BY DateExecuted " + 
							"LIMIT 1000 ";
			}
			
			else if (getNotExecuted)
			{
				query = 	"SELECT CommandGUID, ID, Parameter, DateReceived, DateExecuted, Succeeded, ErrorMessage, DateReceivedByPortal " +
							"FROM 	DeviceCommandsIssued " +
							"WHERE 	DateExecuted IS NULL " +
							"ORDER BY DateReceived ";
			}
			
			
			cursor = database.rawQuery(query, null);
			
			Constants.getLog().add(TAG, Type.Debug, String.valueOf(cursor.getCount()) + " device commands selected.");
			
			while (cursor.moveToNext())
	        {
	        	try
	        	{
	        		DeviceCommandIssuedInfo deviceCommandIssuedInfo = new DeviceCommandIssuedInfo();
	        		
		        	// get info
	        		deviceCommandIssuedInfo.commandGuid = cursor.getString(cursor.getColumnIndex("CommandGUID"));
	        		deviceCommandIssuedInfo.command = DeviceCommands.fromInteger(cursor.getInt(cursor.getColumnIndex("ID")));
	        		deviceCommandIssuedInfo.parameter = cursor.getString(cursor.getColumnIndex("Parameter"));
	        		deviceCommandIssuedInfo.dateReceived = cursor.getString(cursor.getColumnIndex("DateReceived"));
	        		deviceCommandIssuedInfo.dateExecuted = cursor.getString(cursor.getColumnIndex("DateExecuted"));
	        		
	        		if (cursor.getString(cursor.getColumnIndex("Succeeded")).equals("true") ||
	        				cursor.getString(cursor.getColumnIndex("Succeeded")).equals("1"))
	        		{
	        			deviceCommandIssuedInfo.succeeded = true;
	        		}
	        		else
	        		{
	        			deviceCommandIssuedInfo.succeeded = false;
	        		}
	        		
	        		deviceCommandIssuedInfo.errorMessage = cursor.getString(cursor.getColumnIndex("ErrorMessage"));
	        		deviceCommandIssuedInfo.dateReceivedByPortal = cursor.getString(cursor.getColumnIndex("DateReceivedByPortal"));
	        		
	        		
	        		// add to list
	        		listDeviceCommandsIssued.add(deviceCommandIssuedInfo);
	        	}
	    		catch (Exception e)
	        	{
	    			Constants.getLog().add(TAG, Type.Warn, "Problem parsing issued device command record from database.", e);
	        	}
	        }
		}
		catch (Exception e)
    	{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "getDeviceCommandsIssued", e);
    	}
		finally
		{
			closeCursor(cursor);
		}
		
		return listDeviceCommandsIssued;
	}
	
	
	public void setDeviceCommandIssuedReceivedByPortal(List<String> listCommandGuids)
	{
		try
		{
			if (listCommandGuids == null)
				return;
			
			// mark all CommandGUIDs in the specified list as proccessed by the portal
			for (String commandGuid : listCommandGuids)
			{
				// update record
				ContentValues contentValues = new ContentValues();
				contentValues.put("DateReceivedByPortal", Library.getDateTimeGMT(Library.DATE_TIME_FORMAT));
				
				database.update("DeviceCommandsIssued", contentValues, "CommandGUID = ?", new String[] { commandGuid });
				
				Constants.getLog().add(TAG, Type.Verbose, "Logged issued device command as received by the Portal: " + commandGuid);
			}
		}
		catch (Exception e)
		{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "setDeviceCommandIssuedReceivedByPortal", e);
		}
	}
	

	public List<ShortcutInfo> getInstalledShortcuts()
	{
		List<ShortcutInfo> listInstalledShortcuts = new ArrayList<ShortcutInfo>();
		android.database.Cursor cursor = null;
		
		
		try
		{
			// select all records that have been exectuted but not received by the portal
			Constants.getLog().add(TAG, Type.Debug, "Selecting installed shortcuts.");
			
			String query = "";
			

			query = 	"SELECT PortalSysID, Label, URL, IconBase64 " +
						"FROM 	InstalledShortcuts ";
			
			cursor = database.rawQuery(query, null);
			
			Constants.getLog().add(TAG, Type.Debug, String.valueOf(cursor.getCount()) + " installed shortcuts selected.");
			
			while (cursor.moveToNext())
	        {
	        	try
	        	{
	        		ShortcutInfo shortcut = new ShortcutInfo();
	        		
		        	// get info
	        		shortcut.portalSysID = cursor.getInt(cursor.getColumnIndex("PortalSysID"));
	        		shortcut.label = cursor.getString(cursor.getColumnIndex("Label"));
	        		shortcut.url = cursor.getString(cursor.getColumnIndex("URL"));
	        		shortcut.iconBase64 = cursor.getString(cursor.getColumnIndex("IconBase64"));
	        		
	        		// add to list
	        		listInstalledShortcuts.add(shortcut);
	        	}
	    		catch (Exception e)
	        	{
	    			Constants.getLog().add(TAG, Type.Warn, "Problem parsing installed shortcut record from database.", e);
	        	}
	        }
		}
		catch (Exception e)
    	{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "getInstalledShortcuts", e);
    	}
		finally
		{
			closeCursor(cursor);
		}
		
		return listInstalledShortcuts;
	}
	
	
	public void insertInstalledShortcut(ShortcutInfo shortcut)
	{
		try
		{
			if (shortcut == null)
				return;
				
			// insert new record
			ContentValues contentValues = new ContentValues();
			contentValues.put("PortalSysID", shortcut.portalSysID);
			contentValues.put("Label", shortcut.label);
			contentValues.put("URL", shortcut.url);
			contentValues.put("IconBase64", shortcut.iconBase64);
			contentValues.put("DateModified", Library.getDateTimeGMT(Library.DATE_TIME_FORMAT));
			
			database.insert("InstalledShortcuts", null, contentValues);
			
			Constants.getLog().add(TAG, Type.Verbose, "Inserted installed shortcut " + shortcut.label + ".");
		}
		catch (Exception e)
		{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "insertInstalledShortcut", e);
		}
	}
	
	
	public void deleteInstalledShortcut(ShortcutInfo shortcut)
	{
		String query;
		
		try
		{
			query = "DELETE  	" +
					"FROM 		InstalledShortcuts " +
					"WHERE 		PortalSysID = " + String.valueOf(shortcut.portalSysID) + " ";
			
			database.execSQL(query);
			
			Constants.getLog().add(TAG, Type.Debug, "Deleted installed shortcut " + shortcut.label + ".");
		}
		catch (Exception e)
		{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "deleteInstalledShortcut", e);
		}
	}
	
	@Override
	public void setSetting(Settings setting, String value, Boolean IsValueSetFromServer)
	{
		setSetting(setting,value,"0",IsValueSetFromServer);
	}
		
	public void setSetting(Settings setting, String value, String ContainerID, Boolean IsValueSetFromServer)
	{
		String query = null;
		android.database.Cursor cur = null;
		ContentValues values = null;
		String CurValue = null;
		
		try
		{
			// check if setting exists
			Boolean Exists = false;
			
			query = "SELECT value " +
					"FROM 	ClientSettings " +
					"WHERE 	ID = " + escapeString(setting.name()) + " " +
					"and ContainerID = " + ContainerID;
			
			cur = database.rawQuery(query, null);

			if (cur.getCount() == 1)
			{
				cur.moveToFirst();
				CurValue = cur.getString(0);

				Exists = true;
			}
			
			if (!ContainerID.equals("0"))
			{
				if (setting.ordinal() > Settings.SET_CONTAINER_EMAILSETTINGS_START.ordinal() &&
						setting.ordinal() < Settings.SET_CONTAINER_EMAILSETTINGS_END.ordinal())
				{
					if ((Exists == false) || (!CurValue.equalsIgnoreCase(value)))
						Constants.getData().setFlag(Flags.FLG_EmailSettings_Updated, true, false, ContainerID);
					
				}
				else if (setting.ordinal() > Settings.SET_CONTAINER_PIMSETTINGS_START.ordinal() &&
						setting.ordinal() < Settings.SET_CONTAINER_PIMSETTINGS_END.ordinal())
				{
					if ((Exists == false) || (!CurValue.equalsIgnoreCase(value)))
						Constants.getData().setFlag(Flags.FLG_PIMSettings_Updated, true, false, ContainerID);
					
				}
				else if (setting.ordinal() > Settings.SET_CONTAINER_SECURITYSETTINGS_START.ordinal() &&
						setting.ordinal() < Settings.SET_CONTAINER_SECURITYSETTINGS_START.ordinal())
				{
					if ((Exists == false) || (!CurValue.equalsIgnoreCase(value)))
						Constants.getData().setFlag(Flags.FLG_SecuritySettings_Updated, true, false, ContainerID);
					
				}
			}
			
			// set value
			if (value == null)
				value = "";
			
			values = new ContentValues();
			values.put("Value", value.toString());
			values.put("IsValueSetFromServer", IsValueSetFromServer.toString());
	        values.put("DateModified", Library.getDateTimeGMT(Library.DATE_TIME_FORMAT));
			values.put("ContainerID", ContainerID);
			
			// update existing setting
			if (Exists)
				database.update("ClientSettings", values, "ID = ? AND  ContainerID = ?", new String[] { setting.name(), ContainerID });
			
			// insert new setting
			else
			{
				values.put("ID", setting.name());
				database.insert("ClientSettings", null, values);
			}
			
			
			// do not log password values
			if (setting == Settings.SET_RovaPortalPassword || 
				setting == Settings.SET_DefaultWisprPassword ||
				setting == Settings.SET_VerizonNetworkPassword)
			{
				Constants.getLog().add(TAG, Type.Verbose, "Set setting " + setting.toString());
			}
			else
				Constants.getLog().add(TAG, Type.Verbose, "Set setting " + setting.toString() + " to " + value);
		}
		catch (Exception e)
    	{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "setSetting", e);
    	}
		finally
		{
			closeCursor(cur);
		}
	}
	
	@Override
	public void setFlag(Flags flag, Boolean value, Boolean IsValueSetFromServer)
	{
		setFlag(flag,value,IsValueSetFromServer,"0");
	}
	
	@Override
	public void setFlag(Flags flag, Boolean value, Boolean IsValueSetFromServer, String ContainerID)
	{
		String query = null;
		android.database.Cursor cur = null;
		ContentValues values = null;
		Boolean CurValue = false;
		
		try
		{
			// check if setting exists
			Boolean exists = false;
			
			query = "SELECT COUNT(*) as [Count] " +
					"FROM 	ClientSettings " +
					"WHERE 	ID = " + escapeString(flag.name()) + " " +
					" and ContainerID = " + ContainerID;
			
			cur = database.rawQuery(query, null);

			if (cur.getCount() == 1)
			{
				cur.moveToFirst();
				
				CurValue =  Boolean.parseBoolean(cur.getString(0));
				exists = true;
			}
			
			
			if (!ContainerID.equals("0"))
			{
				if (flag.ordinal() > Flags.FLG_CONTAINER_EMAILSETTINGS_START.ordinal() &&
						flag.ordinal() < Flags.FLG_CONTAINER_EMAILSETTINGS_END.ordinal())
				{
					if ((exists == false) || (CurValue!=value))
						Constants.getData().setFlag(Flags.FLG_EmailSettings_Updated, true, false, ContainerID);
					
				}
				else if (flag.ordinal() > Flags.FLG_CONTAINER_PIMSETTINGS_START.ordinal() &&
						flag.ordinal() < Flags.FLG_CONTAINER_PIMSETTINGS_END.ordinal())
				{
					if ((exists == false) || (CurValue!=value))
						Constants.getData().setFlag(Flags.FLG_PIMSettings_Updated, true, false, ContainerID);
					
				}
				else if (flag.ordinal() > Flags.FLG_CONTAINER_SECURITYSETTINGS_START.ordinal() &&
						flag.ordinal() < Flags.FLG_CONTAINER_SECURITYSETTINGS_START.ordinal())
				{
					if ((exists == false) || (CurValue!=value))
						Constants.getData().setFlag(Flags.FLG_SecuritySettings_Updated, true, false, ContainerID);
					
				}
			}			
			
			// set values
			values = new ContentValues();
	        values.put("Value", value.toString());
	        values.put("IsValueSetFromServer", IsValueSetFromServer.toString());
	        values.put("DateModified", Library.getDateTimeGMT(Library.DATE_TIME_FORMAT));
			values.put("ContainerID", ContainerID);
			
			// update existing setting
			if (exists)
				database.update("ClientSettings", values, "ID = ? AND  ContainerID = ?", new String[] { flag.name(), ContainerID });

			// insert new setting
			else
			{
				values.put("ID", flag.name());
				database.insert("ClientSettings", null, values);
			}
			
			Constants.getLog().add(TAG, Type.Verbose, "Set flag " + flag.toString() + " to " + value);
		}
		catch (Exception e)
    	{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "setFlag", e);
    	}
		finally
		{
			closeCursor(cur);
		}
	}
	

	public void updateClientSettings(List<ClientSettingInfo> listClientSettings)
	{
		try
		{
			if (listClientSettings == null)
				return;
			
			for (ClientSettingInfo settingInfo : listClientSettings)
			{
				// save setting
				if (settingInfo.settingName.toLowerCase().startsWith("set_"))
				{
					for (Settings setting : Settings.values()) 
					{
						if (!setting.name().equalsIgnoreCase(settingInfo.settingName))
							continue;
						
						setSetting(setting, settingInfo.value, settingInfo.Containerid, true);
						break;
					}

				}
				
				// save flag
				else if (settingInfo.settingName.toLowerCase().startsWith("flg_"))
				{
					for (Flags flag : Flags.values())
					{
						if (!flag.name().equalsIgnoreCase(settingInfo.settingName))
							continue;
						
						if (settingInfo.value.equals("1") || settingInfo.value.equalsIgnoreCase("true"))
							setFlag(flag, true, true, settingInfo.Containerid);
						else
							setFlag(flag, false, true, settingInfo.Containerid);
							
						break;
					}
				}
			}
		}
		catch (Exception e)
    	{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "updateClientSettings", e);
    	}
	}
	
	
	public void deleteSmartConnectAllowedConnectionTypes()
	{
		String query;
		
		try
		{
			// delete all Smart Connect allowed connection types
			query = "DELETE  	" +
					"FROM 		SmartConnectAllowedConnectionTypes ";
			
			database.execSQL(query);
			
			Constants.getLog().add(TAG, Type.Debug, "Deleted all allowed Smart Connect connection types.");
		}
		catch (Exception e)
		{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "deleteSmartConnectAllowedConnectionTypes", e);
		}
	}
	
	
	public void insertSmartConnectAllowedConnectionTypes(List<SmartConnectAllowedConnectionTypes> listAllowedTypes)
	{
		try
		{
			if (listAllowedTypes == null)
				return;
				
			// insert new records
			for (SmartConnectAllowedConnectionTypes type : listAllowedTypes)
			{
				ContentValues contentValues = new ContentValues();
				contentValues.put("ID", type.id);
				contentValues.put("Rank", type.rank);
				contentValues.put("DateModified", Library.getDateTimeGMT(Library.DATE_TIME_FORMAT));
				
				database.insert("SmartConnectAllowedConnectionTypes", null, contentValues);
				
				Constants.getLog().add(TAG, Type.Verbose, "Inserted assigned Smart Connect allowed connection type " + String.valueOf(type.id));
			}
		}
		catch (Exception e)
		{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "insertSmartConnectAllowedConnectionTypes", e);
		}
	}
	
	
	public SmartConnectAllowedConnectionTypes getSmartConnectAllowedConnectionType(SmartConnectAllowedConnectionTypes.ConnectionType connectionType)
	{
		SmartConnectAllowedConnectionTypes allowedConnectionType = null;
		android.database.Cursor cursor = null;
		
		try
		{
			// select top record for specified SSID
			String query = 	"SELECT ID, Rank " +
							"FROM 	SmartConnectAllowedConnectionTypes " +
							"WHERE 	ID = " + String.valueOf(connectionType.getValue()) + " " +
							"LIMIT 1 ";
			
			cursor = database.rawQuery(query, null);
			
			if (cursor.getCount() == 1)
	        {
				cursor.moveToFirst();
				
				// set info
				allowedConnectionType = new SmartConnectAllowedConnectionTypes(cursor.getInt(cursor.getColumnIndex("ID")), cursor.getInt(cursor.getColumnIndex("Rank")));	
	        }
		}
		catch (Exception e)
    	{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "getSmartConnectAllowedConnectionType", e);
    	}
		finally
		{
			closeCursor(cursor);
		}
		
		return allowedConnectionType;
	}
	

	public void deleteAssignedApplications()
	{
		String query;
		
		try
		{
			// delete all assigned applications
			query = "DELETE  	" +
					"FROM 		ApplicationsAssigned ";
			
			database.execSQL(query);
			
			Constants.getLog().add(TAG, Type.Debug, "Deleted all assigned applications.");
		}
		catch (Exception e)
		{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "deleteAssignedApplications", e);
		}
	}
	
	
	public void insertAssignedApplications(List<ApplicationAssignedInfo> listApplications)
	{
		try
		{
			if (listApplications == null)
				return;
				
			// insert new records
			for (ApplicationAssignedInfo app : listApplications)
			{
				ContentValues contentValues = new ContentValues();
				contentValues.put("PortalSysID", app.portalSysID);
				contentValues.put("PackageName", app.packageName);
				contentValues.put("PackageDisplayName", app.packageDisplayName);
				contentValues.put("Version", app.version);
				contentValues.put("Mandatory", app.mandatory);
				contentValues.put("FilePath", app.filePath);
				
				database.insert("ApplicationsAssigned", null, contentValues);
				
				Constants.getLog().add(TAG, Type.Verbose, "Inserted assigned application " + app.packageName);
			}
		}
		catch (Exception e)
		{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "insertAssignedApplications", e);
		}
	}
	
	
	public List<ApplicationAssignedInfo> getAssignedApplications()
	{
		List<ApplicationAssignedInfo> listApplications = new ArrayList<ApplicationAssignedInfo>();
		android.database.Cursor cursor = null;
		
		
		try
		{
			// select all records
			
			String query = 	"SELECT PortalSysID, PackageName, PackageDisplayName, Version, FilePath, Mandatory " +
							"FROM 	ApplicationsAssigned " +
							"ORDER BY PackageDisplayName ";
			
			cursor = database.rawQuery(query, null);
			
			Constants.getLog().add(TAG, Type.Verbose, String.valueOf(cursor.getCount()) + " assigned application records retrieved from the database");
			
			while (cursor.moveToNext())
	        {
	        	try
	        	{
	        		ApplicationAssignedInfo app = new ApplicationAssignedInfo();
	        		
		        	// get info
	        		app.portalSysID = cursor.getInt(cursor.getColumnIndex("PortalSysID"));
	        		app.packageName = cursor.getString(cursor.getColumnIndex("PackageName"));
	        		app.packageDisplayName = cursor.getString(cursor.getColumnIndex("PackageDisplayName"));
	        		app.version = cursor.getString(cursor.getColumnIndex("Version"));
	        		app.filePath = cursor.getString(cursor.getColumnIndex("FilePath"));
	        		
	        		if (cursor.getString(cursor.getColumnIndex("Mandatory")).equals("1") ||
	        				cursor.getString(cursor.getColumnIndex("Mandatory")).equalsIgnoreCase("true"))
	        			app.mandatory = true;
	        		else
	        			app.mandatory = false;
	        		
	        		
	        		// add to list
	        		listApplications.add(app);
	        	}
	    		catch (Exception e)
	        	{
	    			if (Constants.getHasInitialized())
	    				Constants.getLog().add(TAG, Type.Warn, "Problem parsing assigned application record from database.", e);
	        	}
	        }
		}
		catch (Exception e)
    	{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "getAssignedApplications", e);
    	}
		finally
		{
			closeCursor(cursor);
		}
		
		return listApplications;
	}
	
	
	public void insertDownloadQueueInfo(DownloadQueueInfo downloadQueueInfo)
	{
		try
		{
			if (downloadQueueInfo == null)
				return;
			
			// delete any existing records
			deleteDownloadQueueInfo(downloadQueueInfo.downloadID);
				
			// insert new record
			ContentValues contentValues = new ContentValues();
			contentValues.put("ApplicationsAssignedPortalSysID", downloadQueueInfo.applicationsAssignedPortalSysID);
			contentValues.put("DownloadID", downloadQueueInfo.downloadID);
			contentValues.put("Silent", downloadQueueInfo.silentInstall);
			contentValues.put("DateAdded", Library.getDateTimeGMT(Library.DATE_TIME_FORMAT));
			
			database.insert("DownloadQueue", null, contentValues);
			
			Constants.getLog().add(TAG, Type.Verbose, "Inserted dowload id " + String.valueOf(downloadQueueInfo.downloadID) + " into DownloadQueue");
			
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "insertDownloadQueueInfo", e);
		}
	}
	
	
	public void deleteDownloadQueueInfo(long downloadID)
	{
		String query;
		
		try
		{
			// delete all assigned applications
			query = "DELETE  	" +
					"FROM 		DownloadQueue " +
					"WHERE 		DownloadID = " + String.valueOf(downloadID);
			
			database.execSQL(query);
			
			Constants.getLog().add(TAG, Type.Debug, "Deleted DownloadQueue for Download ID " + String.valueOf(downloadID));
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "deleteDownloadQueueInfo", e);
		}
	}
	
	
	public DownloadQueueInfo getDownloadQueueInfo(long downloadID)
	{
		DownloadQueueInfo downloadQueueInfo = new DownloadQueueInfo();
		android.database.Cursor cursor = null;
		
		try
		{
			// select the newest matching record
			String query = "SELECT  	ApplicationsAssignedPortalSysID, DownloadID, Silent " +
							"FROM 		DownloadQueue " +
							"WHERE 		DownloadID = " + String.valueOf(downloadID) + " " +
							"ORDER BY 	DateAdded DESC " +
							"LIMIT 1 ";
			
			cursor = database.rawQuery(query, null);

			if (cursor.getCount() == 1)
			{
				cursor.moveToFirst();
				
				// fill object
				downloadQueueInfo.applicationsAssignedPortalSysID = cursor.getInt(cursor.getColumnIndex("ApplicationsAssignedPortalSysID"));
				downloadQueueInfo.downloadID =  cursor.getLong(cursor.getColumnIndex("DownloadID"));

				if (cursor.getString(cursor.getColumnIndex("Silent")).equals("true") ||
        				cursor.getString(cursor.getColumnIndex("Silent")).equals("1"))
        		{
					downloadQueueInfo.silentInstall = true;
        		}
        		else
        		{
        			downloadQueueInfo.silentInstall = false;
        		}
			}
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "getDownloadQueueInfo", e);
    	}
		finally
		{
			closeCursor(cursor);
		}
		
		
		return downloadQueueInfo;
	}
	
	
	public DownloadQueueInfo getDownloadQueueInfo(int applicationsAssignedPortalSysID)
	{
		DownloadQueueInfo downloadQueueInfo = new DownloadQueueInfo();
		android.database.Cursor cursor = null;
		
		try
		{
			// select the newest matching record
			String query = "SELECT  	ApplicationsAssignedPortalSysID, DownloadID, Silent " +
							"FROM 		DownloadQueue " +
							"WHERE 		ApplicationsAssignedPortalSysID = " + String.valueOf(applicationsAssignedPortalSysID) + " " +
							"ORDER BY 	DateAdded DESC " +
							"LIMIT 1 ";
			
			cursor = database.rawQuery(query, null);

			if (cursor.getCount() == 1)
			{
				cursor.moveToFirst();
				
				// fill object
				downloadQueueInfo.applicationsAssignedPortalSysID = cursor.getInt(cursor.getColumnIndex("ApplicationsAssignedPortalSysID"));
				downloadQueueInfo.downloadID =  cursor.getLong(cursor.getColumnIndex("DownloadID"));

				if (cursor.getString(cursor.getColumnIndex("Silent")).equals("true") ||
        				cursor.getString(cursor.getColumnIndex("Silent")).equals("1"))
        		{
					downloadQueueInfo.silentInstall = true;
        		}
        		else
        		{
        			downloadQueueInfo.silentInstall = false;
        		}
			}
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "getDownloadQueueInfo", e);
    	}
		finally
		{
			closeCursor(cursor);
		}
		
		
		return downloadQueueInfo;
	}
	
	@Override
	public String getSetting(Settings setting)
	{
		return getSetting(setting, "0");		
	}
	
	@Override
	public String getSetting(Settings setting, String ContainerID)
	{
		String returnString = "";
		String query = null;
		android.database.Cursor cur = null;
		
		
		try
		{
			// get value
			query = "SELECT Value " +
					"FROM 	ClientSettings " +
					"WHERE 	ID = " + escapeString(setting.name()) + " " +
					" and ContainerID = " + ContainerID;
			
			cur = database.rawQuery(query, null);

			if (cur.getCount() == 1)
			{
				cur.moveToFirst();
				returnString = cur.getString(0);
			}
			
			
			// reset null values to an empty string
			if (returnString == null)
				returnString = "";
		}
		catch (Exception e)
    	{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "getSetting", e);
    	}
		finally
		{
			closeCursor(cur);
		}
		
		
		return returnString;
	}
	
	@Override
	public Boolean getFlag(Flags flag)
	{
		return getFlag(flag, "0");
	}
	
	@Override
	public Boolean getFlag(Flags flag, String ContainerID)
	{
		Boolean returnFlag = false;
		String query = null;
		android.database.Cursor cur = null;
		
		try
		{
			// get value
			query = "SELECT Value " +
					"FROM 	ClientSettings " +
					"WHERE 	ID = " + escapeString(flag.name()) + " " +
					" and ContainerID = " + ContainerID ;
			
			cur = database.rawQuery(query, null);

			if (cur.getCount() == 1)
			{
				cur.moveToFirst();
				returnFlag = Boolean.parseBoolean(cur.getString(0));
			}
		}
		catch (Exception e)
    	{
			if (Constants.getHasInitialized())
				Constants.getLog().add(TAG, Type.Error, "getFlag", e);
    	}
		finally
		{
			closeCursor(cur);
		}
		
		
		return returnFlag;
	}
	
	public Boolean FindHost( String URLString)
	{
	    Boolean bfound = false;
	    URI url;
	    String scheme, host, path;
	    Integer port;
		android.database.Cursor cur = null;
	    
		try
		{
			url = new URI(URLString);

			scheme = url.getScheme();
			host = url.getHost();
			port = url.getPort();
			path = url.getPath();


			String query = "SELECT count (*) FROM BlackWhiteList where host = '" + host + "'";

			cur = database.rawQuery(query, null);

			if (cur.getCount() == 1)
			{

				cur.moveToFirst();

				int Count = cur.getInt(0);

				if (Count > 0)
					bfound= true;;


			}

		}
	    catch (Exception err) {
	        
	    }
		finally
		{
			closeCursor(cur);
		}
	    
	    return bfound;
	}

}
