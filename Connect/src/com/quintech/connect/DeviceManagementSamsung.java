package com.quintech.connect;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import android.app.enterprise.ApplicationPolicy;
import android.app.enterprise.license.EnterpriseLicenseManager;
import android.content.ComponentName;
import android.os.Bundle;

import com.quintech.common.ApplicationAssignedInfo;
import com.quintech.common.ApplicationInstalledInfo;
import com.quintech.common.AbstractData.Flags;
import com.quintech.common.AbstractData.Settings;
import com.quintech.common.ILog.Type;
import com.sec.enterprise.knox.EnterpriseContainerCallback;
import com.sec.enterprise.knox.EnterpriseContainerManager;
import com.sec.enterprise.knox.EnterpriseContainerObject;
import com.sec.enterprise.knox.EnterpriseKnoxManager;
import com.sec.enterprise.knox.license.*;

public class DeviceManagementSamsung extends DeviceManagementManufacturer
{
	private static String TAG = "DeviceManagementSamsung";
	private static android.app.enterprise.EnterpriseDeviceManager edm = null;
	private static ComponentName componentDeviceAdminReceiver;
	
	private static ComponentName getComponentDeviceAdminReceiver()
	{
		try
		{
			if (componentDeviceAdminReceiver == null)
			{
				componentDeviceAdminReceiver = new ComponentName(Constants.applicationContext, DeviceAdminReceiver.class);
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "getComponentDeviceAdminReceiver", e);
		}

		return componentDeviceAdminReceiver;
	}
	
	private static android.app.enterprise.EnterpriseDeviceManager getEnterpriseDeviceManager()
	{
		if (edm == null)
		{
			// for 3.0+
			try
			{
				if (edm == null)
					edm = (android.app.enterprise.EnterpriseDeviceManager) Constants.applicationContext.getSystemService(android.app.enterprise.EnterpriseDeviceManager.ENTERPRISE_POLICY_SERVICE);
			}
			catch (java.lang.NoClassDefFoundError e)
			{
				Constants.getLog().add(TAG, Type.Warn, "Unable to access Samsung Enterprise SDK 3.0+");
			}
			catch (java.lang.Error e)
			{
				Constants.getLog().add(TAG, Type.Warn, "Unable to access Samsung Enterprise SDK 3.0+");
			}
			catch (Exception e)
			{
				Constants.getLog().add(TAG, Type.Warn, "Unable to access Samsung Enterprise SDK 3.0+");
				Constants.getLog().add(TAG, Type.Error, "getEnterpriseDeviceManager", e);
			}
			
			// for 1.0 and 2.1
			try
			{
				if (edm == null)
					edm = new android.app.enterprise.EnterpriseDeviceManager(Constants.applicationContext);
			}
			catch (java.lang.NoClassDefFoundError e)
			{
				Constants.getLog().add(TAG, Type.Warn, "Unable to access any Samsung Enterprise SDK version");
			}
			catch (java.lang.Error e)
			{
				Constants.getLog().add(TAG, Type.Warn, "Unable to access any Samsung Enterprise SDK version");
			}
			catch (Exception e)
			{
				Constants.getLog().add(TAG, Type.Warn, "Unable to access any Samsung Enterprise SDK version");
				Constants.getLog().add(TAG, Type.Error, "getEnterpriseDeviceManager", e);
			}
		}
		
		return edm;
	}
	

	public static void activateLicense(String key)
	{
		try
		{
			// development key
			key = "5C6241E722CF7D4FB89E3AB1D9F7108C46A99C327682418C373266DF40AB3FA2113397CC7414F8BDEBC7DB4B67DFD4AF47B58B4CD01186293DE5EA9D9F749B80";
			EnterpriseLicenseManager lm = EnterpriseLicenseManager.getInstance(Constants.applicationContext);
			lm.activateLicense(key);
		}
		catch (java.lang.Error e)
		{
			Constants.getLog().add(TAG, Type.Warn, "Unable to activate Samsung SDK");
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Warn, "Unable to activate Samsung SDK");
		}
	}
	
	public static void testActivateKnox()
	{
		try
		{
						
			EnterpriseLicenseManager lm = EnterpriseLicenseManager.getInstance(Constants.applicationContext);
			activateLicense("");
			
			EnterpriseKnoxManager ekm = EnterpriseKnoxManager.getInstance();
			
			int state = EnterpriseKnoxManager.getDeviceKnoxifiedState();
			
			Constants.getLog().add(TAG, Type.Info, "Knox state = " + state);
			
			if (EnterpriseContainerManager.getOwnContainers() == null || EnterpriseContainerManager.getOwnContainers().length < 1)
			{

				JSONArray array = lm.getApiCallDataByAdmin(Constants.getLibrary().getClientPackageName());
				EnterpriseContainerCallback callback = new EnterpriseContainerCallback() {
					
					@Override
					public void updateStatus(int arg0, Bundle arg1) 
					{
						Constants.getLog().add(TAG, Type.Info, "Knox state = " + arg0);
												
						// container created
						EnterpriseContainerObject[] containers = EnterpriseContainerManager.getOwnContainers();
						
						if (containers != null)
							for (int i = 0; i < containers.length; i++)
							{
								int id = containers[0].getContainerId();
								EnterpriseContainerManager containerMgr = EnterpriseKnoxManager.getInstance().getEnterpriseContainerManager(id);
	
								boolean activated = containerMgr.activateContainer();
							}
					}
				};
				
				boolean bSuccessful = EnterpriseContainerManager.createContainer(callback);
				if (bSuccessful)
				{
					Constants.getLog().add(TAG, Type.Info, "Knox Container Created.");
				}
			}
			
			
			EnterpriseContainerObject[] containers = EnterpriseContainerManager.getOwnContainers();
			
			if (containers != null)
				for (int i = 0; i < containers.length; i++)
				{
					//boolean activated = containerMgr.activateContainer();
				}
		}
		catch (java.lang.Error e)
		{
			Constants.getLog().add(TAG, Type.Warn, "Unable to activate Samsung SDK");
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Warn, "Unable to activate Samsung SDK");
		}
	}
	
	
	public static float getCurrentSdkVersion()
	{
		float version = 0.0f;
		
		try
		{
			if (getEnterpriseDeviceManager() != null)
			{
				// if Samsung API is available, default to version 1.0
				version = 1.0f;
				
				// check if getEnterpriseSdkVer method exists (not available in 1.0)
				boolean hasGetSdkMethod = false;
				
				try
				{
					Method method = android.app.enterprise.EnterpriseDeviceManager.class.getMethod("getEnterpriseSdkVer");
					
					if (method != null)
						hasGetSdkMethod = true;
				}
				catch (Exception e)
				{ 
					
				}
				
				
				
				// getEnterpriseSdkVer() requires 2.0+
				if (hasGetSdkMethod)
				{
					switch (getEnterpriseDeviceManager().getEnterpriseSdkVer())
					{
						case ENTERPRISE_SDK_VERSION_2:
							version = 2.0f;
							break;
							
						case ENTERPRISE_SDK_VERSION_2_1:
							version = 2.1f;
							break;
							
						case ENTERPRISE_SDK_VERSION_2_2:
							version = 2.2f;
							break;
							
						case ENTERPRISE_SDK_VERSION_3:
							version = 3.0f;
							break;
							
						case ENTERPRISE_SDK_VERSION_4:
							version = 4.0f;
							break;
						
						case ENTERPRISE_SDK_VERSION_4_0_1:
							version = 4.01f;
							break;
					}
				}
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Warn, "getCurrentSdkVersion");
		}
		
		if (version > 0.0)
			Constants.getLog().add(TAG, Type.Debug, "Current Samsung Enterprise SDK Version: " + String.valueOf(version));
		else
			Constants.getLog().add(TAG, Type.Debug, "Samsung Enterprise SDK Version not detected");
		
		
		return version;
	}
	
	
	public boolean setStorageEncryption()
	{
		boolean result = false;
		
		try
		{
			boolean encryptInternalStorage = Constants.getData().getFlag(Flags.FLG_EncryptInternalStorage);
			boolean encryptExternalStorage = Constants.getData().getFlag(Flags.FLG_EncryptExternalStorage);
			
			if (getCurrentSdkVersion() >= 2.0)
			{
				if (encryptInternalStorage && !getEnterpriseDeviceManager().getSecurityPolicy().isInternalStorageEncrypted())
				{
					// require and initiate internal encryption
					getEnterpriseDeviceManager().getSecurityPolicy().setRequireDeviceEncryption(getComponentDeviceAdminReceiver(), true);
					getEnterpriseDeviceManager().getSecurityPolicy().setInternalStorageEncryption(true);
					Constants.getLog().add(TAG, Type.Debug, "SAFE - prompting user to encrypt internal storage");
				}
				else if (!encryptInternalStorage)
				{
					// disable requirement
					getEnterpriseDeviceManager().getSecurityPolicy().setRequireDeviceEncryption(getComponentDeviceAdminReceiver(), false);
					Constants.getLog().add(TAG, Type.Debug, "SAFE - disabled internal storage encryption requirement");
				}
				
				
				if (encryptExternalStorage && !getEnterpriseDeviceManager().getSecurityPolicy().isExternalStorageEncrypted())
				{
					// require and initiate external encryption
					getEnterpriseDeviceManager().getSecurityPolicy().setRequireStorageCardEncryption(getComponentDeviceAdminReceiver(), true);
					getEnterpriseDeviceManager().getSecurityPolicy().setExternalStorageEncryption(true);
					Constants.getLog().add(TAG, Type.Debug, "SAFE - prompting user to encrypt external storage");
				}
				else if (!encryptExternalStorage)
				{
					// disable requirement
					getEnterpriseDeviceManager().getSecurityPolicy().setRequireStorageCardEncryption(getComponentDeviceAdminReceiver(), false);
					Constants.getLog().add(TAG, Type.Debug, "SAFE - disabled external storage encryption requirement");
				}
				
				
				// set success flag
				result = true;
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Warn, "SAFE - Unable to set clipboard state");
		}
		
		return result;
	}
	
	
	public void enableApp(ApplicationAssignedInfo app)
	{
		try
		{
			if (getEnterpriseDeviceManager() == null)
			{
				Constants.getLog().add(TAG, Type.Debug, "Samsung Enterprise SDK not available");
				return;
			}
			
			
			// add app to whitelist by default
			if (getCurrentSdkVersion() >= 2.1)
			{
				getEnterpriseDeviceManager().getApplicationPolicy().addAppPackageNameToWhiteList(app.packageName);
				Constants.getLog().add(TAG, Type.Debug, "Added assigned app " + app.packageDisplayName + " to whitelist ");
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Warn, "SAFE - Unable to enable app");
		}
	}
	
	
	public boolean installAppLocalApk(String apkLocalFilePath)
	{
		boolean result = false;
		
		try
		{
			if (getEnterpriseDeviceManager() == null)
			{
				Constants.getLog().add(TAG, Type.Debug, "Samsung Enterprise SDK not available");
				return false;
			}
			
			
			// TODO
			// not sure if this is needed
			// TEST APP INSTALL PROCESS
			
//			// set allow first?
//			if (getCurrentSdkVersion() >= 2.0)
//				getEnterpriseDeviceManager().getApplicationPolicy().setApplicationInstallationMode(ApplicationPolicy.APPLICATION_INSTALLATION_MODE_ALLOW);
			
			
			// install app from local file path
			
			// strip off file:// prefix, SDK does not handle it correctly
			apkLocalFilePath = apkLocalFilePath.replace("file://", "");
			
			result = getEnterpriseDeviceManager().getApplicationPolicy().installApplication(apkLocalFilePath, false);
			
			Constants.getLog().add(TAG, Type.Debug, "Samsung app installation result for " + apkLocalFilePath + ": " + String.valueOf(result));
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Warn, "SAFE - Unable to install APK");
		}
		
		return result;
	}
	
	
	public boolean installAppGooglePlay(String googlePlayAppUrl)
	{
		// return false until implemented
		
		return false;
	}
	
	
	public boolean uninstallApp(String packageIdentifier)
	{
		boolean result = false;
		
		try
		{
			if (getEnterpriseDeviceManager() == null)
			{
				Constants.getLog().add(TAG, Type.Debug, "Samsung Enterprise SDK not available");
				return false;
			}
			
			// allow uninstallation
			getEnterpriseDeviceManager().getApplicationPolicy().setApplicationUninstallationEnabled(packageIdentifier);

			// uninstall
			result = getEnterpriseDeviceManager().getApplicationPolicy().uninstallApplication(packageIdentifier, false);
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Warn, "SAFE - Unable to uninstall app");
		}
		
		return result;
	}
	
	
	public Boolean isInternalStorageEncrypted()
	{
		Boolean isEncrypted = null;
		
		try
		{
			if (getEnterpriseDeviceManager() == null || getCurrentSdkVersion() < 2.0)
			{
				Constants.getLog().add(TAG, Type.Debug, "Samsung Enterprise SDK not available");
				return null;
			}
			
			isEncrypted = getEnterpriseDeviceManager().getSecurityPolicy().isInternalStorageEncrypted();
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Warn, "SAFE - Unable to determine storage encryption status");
		}
		
		return isEncrypted;
	}
	
	
	public Boolean isExternalStorageEncrypted()
	{
		Boolean isEncrypted = null;
		
		try
		{
			if (getEnterpriseDeviceManager() == null || getCurrentSdkVersion() < 2.0)
			{
				Constants.getLog().add(TAG, Type.Debug, "Samsung Enterprise SDK not available");
				return null;
			}
			
			isEncrypted = getEnterpriseDeviceManager().getSecurityPolicy().isExternalStorageEncrypted();
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Warn, "SAFE - Unable to determine external storage status");
		}
		
		return isEncrypted;
	}
	
	
	public boolean enterpriseWipe()
	{
		boolean success = false;
		
		try
		{
			if (getEnterpriseDeviceManager() == null)
			{
				Constants.getLog().add(TAG, Type.Debug, "Samsung Enterprise SDK not available");
				return false;
			}
			
			// uninstall all assigned apps from the portal
			String[] manangedPackages = getEnterpriseDeviceManager().getApplicationPolicy().getInstalledManagedApplicationsList();
			
			for (String packageName : manangedPackages)
			{
				// do not uninstall client yet
				if (packageName.equalsIgnoreCase(Constants.getLibrary().getClientPackageName()))
					continue;
				
				getEnterpriseDeviceManager().getApplicationPolicy().uninstallApplication(packageName, false);
			}
			
			
			// TODO
			// remove white and black lists
			
			// TODO
			// remove all mail/vpn/wifi settings
			
			// TODO
			// remove restrictions
			
			
			// attempt to uninstall client
			getEnterpriseDeviceManager().getApplicationPolicy().uninstallApplication(Constants.getLibrary().getClientPackageName(), false);
			
			// set flag
			success = true;
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Warn, "SAFE - Unable to perform enterprise wipe");
		}
		
		return success;
	}
	
	
	public void setPolicySettings() 
	{
		try
		{
			if (getEnterpriseDeviceManager() == null)
			{
				Constants.getLog().add(TAG, Type.Debug, "Samsung Enterprise SDK not available");
				return;
			}
			
			Constants.getLog().add(TAG, Type.Debug, "Setting Samsung Enterprise SDK options");
					
			setPasswordExpires();
			setPasswordHistory();
			setMinPasswordComplexChars();
			setDisableAndroidMarket();
			setDisableAndroidBrowser();
			setAutoFillSetting();
			setForceFraudWarningSetting();
			setJavaScriptSetting();
			setPopupsSetting();
			setDisableYouTube();
			setDisableVoiceDialer();
			setAllowNonMarketApps();
			setRoamingVoiceCalls();
			setRoamingData();
			setRoamingSync();
			setRoamingPush();
			setDisableCamera();
			setDisableScreenCapture();
			setCellularData();
			setTethering();
			setAllowBluetooth();
			setSdCardState();
			allowFactoryReset();
			allowSettingsChanges();
			setClipboardEnabled();
			setAllowInstallApps();
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Warn, "SAFE - Unable to set policy settings");
		}
	}
	
	
	private static void setPasswordExpires()
	{
		try
		{
			String value = Constants.getData().getSetting(Settings.SET_PasswordExpirationTimeoutMinutes);
			
			if (value != null && !value.equals(""))
			{
				int newPasswordExpirationTimeoutMinutes = Integer.valueOf(value);
				int oldPasswordExpirationTimeoutMinutes = 0;
				
				// get old value, convert days to minutes for comparison
				if (getCurrentSdkVersion() >= 2.0)
					oldPasswordExpirationTimeoutMinutes = getEnterpriseDeviceManager().getPasswordPolicy().getPasswordExpires(getComponentDeviceAdminReceiver()) * 24 * 60;
				
				else
					oldPasswordExpirationTimeoutMinutes = getEnterpriseDeviceManager().getPasswordExpires(getComponentDeviceAdminReceiver()) * 24 * 60;

				
				// set setting if required
				if (newPasswordExpirationTimeoutMinutes != oldPasswordExpirationTimeoutMinutes)
				{
					if (getCurrentSdkVersion() >= 2.0)
						getEnterpriseDeviceManager().getPasswordPolicy().setPasswordExpires(getComponentDeviceAdminReceiver(), newPasswordExpirationTimeoutMinutes);
					
					else
						getEnterpriseDeviceManager().setPasswordExpires(getComponentDeviceAdminReceiver(), newPasswordExpirationTimeoutMinutes);
					
					
					Constants.getLog().add(TAG, Type.Debug, "SAFE - Set password expiration timeout");
				}
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Warn, "SAFE - Unable to set password expiration timeout");
		}
	}
	
	private static void setPasswordHistory()
	{
		try
		{
			String value = Constants.getData().getSetting(Settings.SET_PasswordHistoryRestriction);
			
			if (value != null && !value.equals(""))
			{
				
				int newPasswordHistoryRestriction = Integer.valueOf(value);
				int oldPasswordHistoryRestriction = 0;
				
				// get old value
				if (getCurrentSdkVersion() >= 2.0)
					oldPasswordHistoryRestriction = getEnterpriseDeviceManager().getPasswordPolicy().getPasswordHistory(getComponentDeviceAdminReceiver());
						
				else
					oldPasswordHistoryRestriction = getEnterpriseDeviceManager().getPasswordHistory(getComponentDeviceAdminReceiver());
			
				
				// set setting if required
				if (newPasswordHistoryRestriction != oldPasswordHistoryRestriction)
				{
					if (getCurrentSdkVersion() >= 2.0)
						getEnterpriseDeviceManager().getPasswordPolicy().setPasswordHistory(getComponentDeviceAdminReceiver(), newPasswordHistoryRestriction);
					
					else
						getEnterpriseDeviceManager().setPasswordHistory(getComponentDeviceAdminReceiver(), newPasswordHistoryRestriction);
					
					Constants.getLog().add(TAG, Type.Debug, "SAFE - Set password history length");
				}
				
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Warn, "SAFE - Unable to set password history restriction value");
		}
	}
	
	private static void setMinPasswordComplexChars()
	{
		try
		{
			String value = Constants.getData().getSetting(Settings.SET_MinimumSymbolsRequired);
			
			if (value != null && !value.equals(""))
			{
				int newMinimumSymbolsRequired = Integer.valueOf(value);
				int oldMinimumSymbolsRequired = 0;
			
				// get old value
				if (getCurrentSdkVersion() >= 2.0)
					oldMinimumSymbolsRequired = getEnterpriseDeviceManager().getPasswordPolicy().getMinPasswordComplexChars(getComponentDeviceAdminReceiver());
						
				else
					oldMinimumSymbolsRequired = getEnterpriseDeviceManager().getMinPasswordComplexChars(getComponentDeviceAdminReceiver());
				
				
				if (newMinimumSymbolsRequired != oldMinimumSymbolsRequired)
				{
					if (getCurrentSdkVersion() >= 2.0)
						getEnterpriseDeviceManager().getPasswordPolicy().setMinPasswordComplexChars(getComponentDeviceAdminReceiver(), newMinimumSymbolsRequired);
					
					else
						getEnterpriseDeviceManager().setMinPasswordComplexChars(getComponentDeviceAdminReceiver(), newMinimumSymbolsRequired);
					
					Constants.getLog().add(TAG, Type.Debug, "SAFE - Set password minimum symbols");
				}
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Warn, "SAFE - Unable to set minimum password symbols");
		}
	}
	
	private static void setDisableAndroidMarket()
	{
		try
		{
			if (Constants.getData().getFlag(Flags.FLG_DisableAppStore))
			{
				getEnterpriseDeviceManager().getApplicationPolicy().disableAndroidMarket();
				Constants.getLog().add(TAG, Type.Debug, "SAFE - Disabled App Store");
			}
			else
			{
				getEnterpriseDeviceManager().getApplicationPolicy().enableAndroidMarket();
				Constants.getLog().add(TAG, Type.Debug, "SAFE - Enabled App Store");
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Warn, "SAFE -Unable to set disable App Store");
		}
	}
	
	private static void setDisableAndroidBrowser()
	{
		try
		{
			if (getCurrentSdkVersion() >= 2.0)
			{
				if (Constants.getData().getFlag(Flags.FLG_DisableBrowser))
				{
					getEnterpriseDeviceManager().getApplicationPolicy().disableAndroidBrowser();
					Constants.getLog().add(TAG, Type.Debug, "SAFE - Disabled Default Browser");
				}
				else
				{
					getEnterpriseDeviceManager().getApplicationPolicy().enableAndroidBrowser();
					Constants.getLog().add(TAG, Type.Debug, "SAFE - Enabled Default Browser");
				}
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Warn, "SAFE - Unable to set disable Default Browser");
		}
	}
	
	private static void setAutoFillSetting()
	{
		try
		{
			if (getCurrentSdkVersion() >= 2.0)
			{
				boolean browserDisableAutoFill = Constants.getData().getFlag(Flags.FLG_BrowserDisableAutoFill);
				
				if (browserDisableAutoFill != !getEnterpriseDeviceManager().getBrowserPolicy().getAutoFillSetting())
				{
					getEnterpriseDeviceManager().getBrowserPolicy().setAutoFillSetting(!browserDisableAutoFill);
					Constants.getLog().add(TAG, Type.Debug, "SAFE - Set Browser AutoFill Disabled: " + String.valueOf(browserDisableAutoFill));
				}
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Warn, "SAFE - Unable to set Default Browser AutoFill");
		}
	}
	
	private static void setForceFraudWarningSetting()
	{
		try
		{
			if (getCurrentSdkVersion() >= 2.0)
			{
				boolean browserForceFraudWarning = Constants.getData().getFlag(Flags.FLG_BrowserForceFraudWarning);
				
				if (browserForceFraudWarning != getEnterpriseDeviceManager().getBrowserPolicy().getForceFraudWarningSetting())
				{
					getEnterpriseDeviceManager().getBrowserPolicy().setForceFraudWarningSetting(browserForceFraudWarning);
					Constants.getLog().add(TAG, Type.Debug, "SAFE - Set Browser Force Fraud Warning: " + String.valueOf(browserForceFraudWarning));
				}
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Warn, "SAFE - Unable to set Default Browser Force Fraud Warning");
		}
	}
	
	private static void setJavaScriptSetting()
	{
		try
		{
			if (getCurrentSdkVersion() >= 2.0)
			{
				boolean browserDisableJavascript = Constants.getData().getFlag(Flags.FLG_BrowserDisableJavascript);
				
				if (browserDisableJavascript != !getEnterpriseDeviceManager().getBrowserPolicy().getJavaScriptSetting())
				{
					getEnterpriseDeviceManager().getBrowserPolicy().setJavaScriptSetting(!browserDisableJavascript);
					Constants.getLog().add(TAG, Type.Debug, "SAFE - Set Default Browser Disable Javascript: " + String.valueOf(browserDisableJavascript));
				}
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Warn, "SAFE - Unable to set Default Browser Javascript");
		}
	}
	
	private static void setPopupsSetting()
	{
		try
		{
			if (getCurrentSdkVersion() >= 2.0)
			{
				boolean browserDisablePopups = Constants.getData().getFlag(Flags.FLG_BrowserDisablePopups);
				
				if (browserDisablePopups != !getEnterpriseDeviceManager().getBrowserPolicy().getPopupsSetting())
				{
					getEnterpriseDeviceManager().getBrowserPolicy().setPopupsSetting(!browserDisablePopups);
					Constants.getLog().add(TAG, Type.Debug, "SAFE - Set Default Browser Disable popups: " + String.valueOf(browserDisablePopups));
				}
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Warn, "SAFE - Unable to set Default Browser popups");
		}
	}
	
	private static void setDisableYouTube()
	{
		try
		{
			if (getCurrentSdkVersion() >= 2.0)
			{
				boolean disableYouTubeApp = Constants.getData().getFlag(Flags.FLG_DisableYouTubeApp);
				
				if (disableYouTubeApp)
				{
					getEnterpriseDeviceManager().getApplicationPolicy().disableYouTube();
					Constants.getLog().add(TAG, Type.Debug, "SAFE - disabled youtube");
				}
				else
				{
					getEnterpriseDeviceManager().getApplicationPolicy().enableYouTube();
					Constants.getLog().add(TAG, Type.Debug, "SAFE - enabled youtube");
				}
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Warn, "SAFE - Unable to set Youtube access");
		}
	}
	
	private static void setDisableVoiceDialer()
	{
		try
		{
			if (getCurrentSdkVersion() >= 2.0)
			{
				boolean disableVoiceDialing = Constants.getData().getFlag(Flags.FLG_DisableVoiceDialing);
				
				if (disableVoiceDialing)
				{
					getEnterpriseDeviceManager().getApplicationPolicy().disableVoiceDialer();
					Constants.getLog().add(TAG, Type.Debug, "SAFE - disabled voice dialer");
				}
				else
				{
					getEnterpriseDeviceManager().getApplicationPolicy().enableVoiceDialer();
					Constants.getLog().add(TAG, Type.Debug, "SAFE - enabled voice dialer");
				}
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Warn, "SAFE - Unable to set voice dialer access");
		}
	}
	
	private static void setAllowNonMarketApps()
	{
		try
		{
			if (getCurrentSdkVersion() >= 2.0)
			{
				boolean disableNonMarketApps = Constants.getData().getFlag(Flags.FLG_DisableNonMarketApps);
				
				if (disableNonMarketApps != !getEnterpriseDeviceManager().getRestrictionPolicy().isNonMarketAppAllowed())
				{
					getEnterpriseDeviceManager().getRestrictionPolicy().setAllowNonMarketApps(!disableNonMarketApps);
					Constants.getLog().add(TAG, Type.Debug, "SAFE - allow non-market app: " + String.valueOf(!disableNonMarketApps));
				}
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Warn, "SAFE - Unable to set voice dialer access");
		}
	}
	
	private static void setRoamingVoiceCalls()
	{
		try
		{
			String value = Constants.getData().getSetting(Settings.SET_VoiceRoaming);
			
			if (getCurrentSdkVersion() >= 3.0 && value != null && !value.equals(""))
			{
				int voiceRoaming = Integer.valueOf(value);
				
				if (voiceRoaming == 0)
				{
					getEnterpriseDeviceManager().getRoamingPolicy().setRoamingVoiceCalls(false);
					Constants.getLog().add(TAG, Type.Debug, "SAFE - Disabled voice roaming");
				}
				
				else
				{
					getEnterpriseDeviceManager().getRoamingPolicy().setRoamingVoiceCalls(true);
					Constants.getLog().add(TAG, Type.Debug, "SAFE - Allowed voice roaming");
				}
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Warn, "SAFE - Unable to set data roaming");
		}
	}
	
	private static void setRoamingData()
	{
		try
		{
			String value = Constants.getData().getSetting(Settings.SET_DataRoaming);
			
			if (value != null && !value.equals(""))
			{
				int dataRoaming = Integer.valueOf(value);
				
				if (dataRoaming == 0)
				{
					getEnterpriseDeviceManager().getRoamingPolicy().setRoamingData(false);
					Constants.getLog().add(TAG, Type.Debug, "SAFE - Disabled data roaming");
				}
				
				else
				{
					getEnterpriseDeviceManager().getRoamingPolicy().setRoamingData(true);
					Constants.getLog().add(TAG, Type.Debug, "SAFE - Allowed data roaming");
				}
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Warn, "SAFE - Unable to set data roaming");
		}
	}
	
	private static void setRoamingSync()
	{
		try
		{
			boolean disableAutoSyncWhileRoaming = Constants.getData().getFlag(Flags.FLG_DisableAutoSyncWhileRoaming);
			
			if (disableAutoSyncWhileRoaming)
			{
				getEnterpriseDeviceManager().getRoamingPolicy().setRoamingSync(false);
				Constants.getLog().add(TAG, Type.Debug, "SAFE - disabled roaming sync");
			}
			else
			{
				getEnterpriseDeviceManager().getRoamingPolicy().setRoamingSync(true);
				Constants.getLog().add(TAG, Type.Debug, "SAFE - allowed roaming sync");
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Warn, "SAFE - Unable to set roaming sync");
		}
	}
	
	private static void setRoamingPush()
	{
		try
		{
			String value = Constants.getData().getSetting(Settings.SET_MMSRoaming);
			
			if (value != null && !value.equals(""))
			{
				int mmsRoaming = Integer.valueOf(value);
				
				if (mmsRoaming == 0)
				{
					getEnterpriseDeviceManager().getRoamingPolicy().setRoamingPush(false);
					Constants.getLog().add(TAG, Type.Debug, "SAFE - Disabled MMS roaming");
				}
				
				else
				{
					getEnterpriseDeviceManager().getRoamingPolicy().setRoamingPush(true);
					Constants.getLog().add(TAG, Type.Debug, "SAFE - Allowed MMS roaming");
				}
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Warn, "SAFE - Unable to set MMS roaming");
		}
	}
	
	private static void setDisableCamera()
	{
		try
		{
			if (getCurrentSdkVersion() >= 2.0)
			{
				boolean disableCamera = Constants.getData().getFlag(Flags.FLG_DisableCamera);
				
				if (disableCamera)
				{
					getEnterpriseDeviceManager().getRestrictionPolicy().setCameraState(false);
					Constants.getLog().add(TAG, Type.Debug, "SAFE - disabled camera");
				}
				else
				{
					getEnterpriseDeviceManager().getRestrictionPolicy().setCameraState(true);
					Constants.getLog().add(TAG, Type.Debug, "SAFE - enabled camera");
				}
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Warn, "SAFE - Unable to set camera state");
		}
	}
	
	private static void setDisableScreenCapture()
	{
		try
		{
			if (getCurrentSdkVersion() >= 2.0)
			{
				boolean disableScreenCapture = Constants.getData().getFlag(Flags.FLG_DisableScreenCapture);
				
				if (disableScreenCapture)
				{
					getEnterpriseDeviceManager().getRestrictionPolicy().setScreenCapture(false);
					Constants.getLog().add(TAG, Type.Debug, "SAFE - disabled screen capture");
				}
				else
				{
					getEnterpriseDeviceManager().getRestrictionPolicy().setScreenCapture(true);
					Constants.getLog().add(TAG, Type.Debug, "SAFE - enabled screen capture");
				}
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Warn, "SAFE - Unable to set screen capture state");
		}
	}
	
	private static void setCellularData()
	{
		try
		{
			if (getCurrentSdkVersion() >= 2.0)
			{
				boolean disableCellularData = Constants.getData().getFlag(Flags.FLG_DisableCellularData);
				
				if (disableCellularData)
				{
					getEnterpriseDeviceManager().getRestrictionPolicy().setCellularData(false);
					Constants.getLog().add(TAG, Type.Debug, "SAFE - disabled cell data");
				}
				else
				{
					getEnterpriseDeviceManager().getRestrictionPolicy().setCellularData(true);
					Constants.getLog().add(TAG, Type.Debug, "SAFE - enabled cell data");
				}
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Warn, "SAFE - Unable to set cell data state");
		}
	}
	
	private static void setTethering()
	{
		try
		{
			if (getCurrentSdkVersion() >= 2.0)
			{
				boolean disableTethering = Constants.getData().getFlag(Flags.FLG_DisableTethering);
				
				if (disableTethering)
				{
					getEnterpriseDeviceManager().getRestrictionPolicy().setTethering(false);
					Constants.getLog().add(TAG, Type.Debug, "SAFE - disabled tethering");
				}
				else
				{
					getEnterpriseDeviceManager().getRestrictionPolicy().setTethering(true);
					Constants.getLog().add(TAG, Type.Debug, "SAFE - enabled tethering");
				}
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Warn, "SAFE - Unable to set tethering state");
		}
	}
	
	private static void setAllowBluetooth()
	{
		try
		{
			if (getCurrentSdkVersion() >= 2.0)
			{
				boolean disableBluetooth = Constants.getData().getFlag(Flags.FLG_DisableBluetooth);
				
				if (disableBluetooth)
				{
					getEnterpriseDeviceManager().getRestrictionPolicy().allowBluetooth(false);
					Constants.getLog().add(TAG, Type.Debug, "SAFE - disabled bluetooth");
				}
				else
				{
					getEnterpriseDeviceManager().getRestrictionPolicy().allowBluetooth(true);
					Constants.getLog().add(TAG, Type.Debug, "SAFE - enabled bluetooth");
				}
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Warn, "SAFE - Unable to set bluetooth state");
		}
	}
	
	private static void setSdCardState()
	{
		try
		{
			if (getCurrentSdkVersion() >= 2.0)
			{
				boolean disableSdCard = Constants.getData().getFlag(Flags.FLG_DisableSdCard);
				
				if (disableSdCard)
				{
					getEnterpriseDeviceManager().getRestrictionPolicy().setSdCardState(false);
					Constants.getLog().add(TAG, Type.Debug, "SAFE - disabled SD Card");
				}
				else
				{
					getEnterpriseDeviceManager().getRestrictionPolicy().setSdCardState(true);
					Constants.getLog().add(TAG, Type.Debug, "SAFE - enabled SD Card");
				}
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Warn, "SAFE - Unable to set SD Card state");
		}
	}
	
	private static void allowFactoryReset()
	{
		try
		{
			if (getCurrentSdkVersion() >= 2.0)
			{
				boolean disableFactoryReset = Constants.getData().getFlag(Flags.FLG_DisableFactoryReset);
				
				if (disableFactoryReset)
				{
					getEnterpriseDeviceManager().getRestrictionPolicy().allowFactoryReset(false);
					Constants.getLog().add(TAG, Type.Debug, "SAFE - disabled Factory Reset");
				}
				else
				{
					getEnterpriseDeviceManager().getRestrictionPolicy().allowFactoryReset(true);
					Constants.getLog().add(TAG, Type.Debug, "SAFE - enabled Factory Reset");
				}
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Warn, "SAFE - Unable to set Factory Reset state");
		}
	}
	
	private static void allowSettingsChanges()
	{
		try
		{
			if (getCurrentSdkVersion() >= 2.0)
			{
				boolean disableSettingChanges = Constants.getData().getFlag(Flags.FLG_DisableSettingChanges);
				
				if (disableSettingChanges)
				{
					getEnterpriseDeviceManager().getRestrictionPolicy().allowSettingsChanges(false);
					Constants.getLog().add(TAG, Type.Debug, "SAFE - disabled Setting Changes");
				}
				else
				{
					getEnterpriseDeviceManager().getRestrictionPolicy().allowSettingsChanges(true);
					Constants.getLog().add(TAG, Type.Debug, "SAFE - enabled Setting Changes");
				}
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Warn, "SAFE - Unable to set Allow Setting Changes state");
		}
	}
	
	private static void setClipboardEnabled()
	{
		try
		{
			if (getCurrentSdkVersion() >= 2.0)
			{
				boolean disableClipboard = Constants.getData().getFlag(Flags.FLG_DisableClipboard);
				
				if (disableClipboard)
				{
					getEnterpriseDeviceManager().getRestrictionPolicy().setClipboardEnabled(false);
					Constants.getLog().add(TAG, Type.Debug, "SAFE - disabled clipboard");
				}
				else
				{
					getEnterpriseDeviceManager().getRestrictionPolicy().setClipboardEnabled(true);
					Constants.getLog().add(TAG, Type.Debug, "SAFE - enabled clipboard");
				}
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Warn, "SAFE - Unable to set clipboard state");
		}
	}
	
	private static void setAllowInstallApps()
	{
		// run in new thread, enabling/disabling existing apps can take awhile
		Thread t = new Thread() 
    	{
            @Override
            public void run() 
            {
				try
				{
					if (getCurrentSdkVersion() < 2.0)
						return;
					
					boolean enable = !Constants.getData().getFlag(Flags.FLG_DisableInstallingApps);
					
					// check if state differs from input flag
					int currentInstallationMode = getEnterpriseDeviceManager().getApplicationPolicy().getApplicationInstallationMode();
					
					if (enable && currentInstallationMode == ApplicationPolicy.APPLICATION_INSTALLATION_MODE_ALLOW)
					{
						Constants.getLog().add(TAG, Type.Debug, "SAFE - Set allow apps is true and currently enabled");
						return;
					}
					
					else if (!enable && currentInstallationMode == ApplicationPolicy.APPLICATION_INSTALLATION_MODE_DISALLOW)
					{
						Constants.getLog().add(TAG, Type.Debug, "SAFE - Set allow apps is false and currently disabled");
						return;
					}
					
					
					// set new state
					
					// set app installation
					if (enable)
						getEnterpriseDeviceManager().getApplicationPolicy().setApplicationInstallationMode(ApplicationPolicy.APPLICATION_INSTALLATION_MODE_ALLOW);
					else
						getEnterpriseDeviceManager().getApplicationPolicy().setApplicationInstallationMode(ApplicationPolicy.APPLICATION_INSTALLATION_MODE_DISALLOW);
					
					Constants.getLog().add(TAG, Type.Debug, "SAFE - Set allow app install state to " + String.valueOf(enable));
					
					
					// set all existing installed 3rd party apps status
					String[] reportedPackageList = getEnterpriseDeviceManager().getApplicationPolicy().getApplicationStateList(!enable);
					
					// filter list - do not include our app, or any apps assigned by the portal
					List<String> filteredPackageList = new ArrayList<String>();
					List<ApplicationAssignedInfo> listAssignedApps = Constants.getData().getAssignedApplications();
					List<ApplicationInstalledInfo> listApplicationsInstalledNonSystem = Constants.getLibrary().getInstalledApplications(false);
					
					for (String reportedPackage : reportedPackageList)
					{
						// skip operating system packages
						boolean isNonSystemApp = false;
						
						for (ApplicationInstalledInfo installedApp : listApplicationsInstalledNonSystem)
							if (installedApp.packageName.equalsIgnoreCase(reportedPackage))
							{
								isNonSystemApp = true;
								break;
							}
						
						if (!isNonSystemApp)
							continue;
						
						
						// skip EC package
						if (reportedPackage.equalsIgnoreCase(Constants.getLibrary().getClientPackageName()))
							continue;
						
						boolean isAssigned = false;
						
						for (ApplicationAssignedInfo assignedApp : listAssignedApps)
						{
							if (assignedApp.packageName.equalsIgnoreCase(reportedPackage))
							{
								isAssigned = true;
								break;
							}
						}
						
						// add to list if not assigned
						if (!isAssigned)
							filteredPackageList.add(reportedPackage);
					}
			
					
					// process list
					Constants.getLog().add(TAG, Type.Debug, "SAFE - Setting installed app enabled states to " + String.valueOf(enable));
					getEnterpriseDeviceManager().getApplicationPolicy().setApplicationStateList(filteredPackageList.toArray(new String[filteredPackageList.size()]), enable);
				}
				catch (Exception e)
				{
					Constants.getLog().add(TAG, Type.Warn, "SAFE - Unable to set disable install apps");
				}
            }
    	};
    	
    	t.start();
	}

}
