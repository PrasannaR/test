package com.quintech.connect;


import java.util.EnumSet;
import java.util.List;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;


import com.quintech.common.AbstractLibrary;
import com.quintech.common.ApplicationAssignedInfo;
import com.quintech.common.ApplicationInstalledInfo;
import com.quintech.common.Credentials;
import com.quintech.common.AbstractData.Flags;
import com.quintech.common.AbstractData.Settings;
import com.quintech.common.ILog.Type;
import com.quintech.connect.activities.CredentialPromptActivity;


public class DeviceManagement extends com.quintech.common.AbstractDeviceManagement
{
	private static String TAG = "DeviceManagement";
	private DevicePolicyManager devicePolicyManager;
	private ComponentName componentDeviceAdminReceiver;
	
	
	public DevicePolicyManager getDevicePolicyManager()
	{
		try
		{
			if (devicePolicyManager == null)
			{
				devicePolicyManager = (DevicePolicyManager) Constants.applicationContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "getDevicePolicyManager", e);
		}
		
		return devicePolicyManager;
	}
	
	
	public ComponentName getComponentDeviceAdminReceiver()
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
	
	
	@Override
	public boolean isDeviceAdministratorActive() 
	{
		boolean isActive = false;
		
		try
		{
			if (getDevicePolicyManager().isAdminActive(getComponentDeviceAdminReceiver()))
            {
				isActive = true;
            }
			
			Constants.getLog().add(TAG, Type.Verbose, "Device Administrator active: " + String.valueOf(isActive));
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "isDeviceAdministratorActive", e);
		}
		
		return isActive;
	}
	
	
	public boolean isDeviceAdministratorPromptRequired() 
	{
		boolean isPromptRequired = false;
		
		try
		{
			// return true if Device Administrator is required and not active
			if (Constants.getData().getFlag(Flags.FLG_RequireDeviceAdministratorAccess) && !isDeviceAdministratorActive())
            {
				isPromptRequired = true;
            }
			
			Constants.getLog().add(TAG, Type.Verbose, "Device Administrator prompt required: " + String.valueOf(isPromptRequired));
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "isDeviceAdministratorPromptRequired", e);
		}
		
		return isPromptRequired;
	}
	
	
	public void removeDeviceAdministrator()
	{
		try
		{
			if (getDevicePolicyManager().isAdminActive(getComponentDeviceAdminReceiver()))
            {
				getDevicePolicyManager().removeActiveAdmin(getComponentDeviceAdminReceiver());
            }
			
			Constants.getLog().add(TAG, Type.Debug, "Removed Device Administrator");
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "removeDeviceAdministrator", e);
		}
	}
	
	
	public boolean isActivePasswordSufficient()
	{
		boolean isSufficent = false;
		
		try
		{
			isSufficent = getDevicePolicyManager().isActivePasswordSufficient();
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "isActivePasswordSufficient", e);
		}
		
		return isSufficent;
	}
	
	
	public Boolean isInternalStorageEncrypted()
	{
		Boolean isEncrypted = null;
		
		try
		{
			// check manufacturer API
			if (Constants.getDeviceManagementManufacturer() != null)
				isEncrypted = Constants.getDeviceManagementManufacturer().isInternalStorageEncrypted();
			
			// check Android SDK
			if (isEncrypted == null && Build.VERSION.SDK_INT >= 11)
			{
				if (getDevicePolicyManager().getStorageEncryptionStatus() == DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE ||
						getDevicePolicyManager().getStorageEncryptionStatus() == DevicePolicyManager.ENCRYPTION_STATUS_ACTIVATING)
					isEncrypted = true;
				
				else if (getDevicePolicyManager().getStorageEncryptionStatus() == DevicePolicyManager.ENCRYPTION_STATUS_INACTIVE)
					isEncrypted = false;
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "isInternalStorageEncrypted", e);
		}
		
		return isEncrypted;
	}
	
	
	public Boolean isExternalStorageEncrypted()
	{
		Boolean isEncrypted = null;
		
		try
		{
			// not supported by Android SDK, check manufacturer API
			if (Constants.getDeviceManagementManufacturer() != null)
				isEncrypted = Constants.getDeviceManagementManufacturer().isExternalStorageEncrypted();
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "isExternalStorageEncrypted", e);
		}
		
		return isEncrypted;
	}
	
	
	@Override
	public DeviceCommandResult lockDevice() 
	{
		DeviceCommandResult result = new DeviceCommandResult();

		try
		{
			// lock device
			getDevicePolicyManager().lockNow();
			Constants.getLog().add(TAG, Type.Info, "Locked device screen");
			result.succeeded = true;
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "lockDevice", e);
			
			// return error message
			result.errorMessage = e.getMessage();
		}
		
		
		return result;
	}
	
	
	@Override
	public DeviceCommandResult clearPasscode() 
	{
		DeviceCommandResult result = new DeviceCommandResult();
		
		try
		{
			boolean hasCleared = false;
			
			// temporarily remove password requirements
			getDevicePolicyManager().setPasswordQuality(getComponentDeviceAdminReceiver(), DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED);
			getDevicePolicyManager().setPasswordMinimumLength(getComponentDeviceAdminReceiver(), 0);
			
			if (Build.VERSION.SDK_INT >= 11)
			{
				getDevicePolicyManager().setPasswordMinimumLetters(getComponentDeviceAdminReceiver(), 0);
				getDevicePolicyManager().setPasswordMinimumLowerCase(getComponentDeviceAdminReceiver(), 0);
				getDevicePolicyManager().setPasswordMinimumNonLetter(getComponentDeviceAdminReceiver(), 0);
				getDevicePolicyManager().setPasswordMinimumNumeric(getComponentDeviceAdminReceiver(), 0);
				getDevicePolicyManager().setPasswordMinimumSymbols(getComponentDeviceAdminReceiver(), 0);
				getDevicePolicyManager().setPasswordMinimumUpperCase(getComponentDeviceAdminReceiver(), 0);
				getDevicePolicyManager().setPasswordHistoryLength(getComponentDeviceAdminReceiver(), 0);
			}
			
			
			// clear device passcode
			hasCleared = getDevicePolicyManager().resetPassword("", 0);
			
			// check result
			if (hasCleared)
			{
				Constants.getLog().add(TAG, Type.Info, "Cleared device passcode");
				result.succeeded = true;
			}
			else
				throw new Exception("Unable to clear device passcode.");
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "clearPasscode", e);
			
			// return error message
			result.errorMessage = e.getMessage();
		}
		finally
		{
			// reset policy settings
			setPolicySettings();
			
			// check new password value
			Constants.getDeviceManagement().enforcePolicy(PolicyFailureNotificationType.NotifyAndPrompt);
		}
		
		
		return result;
	}

	
	@Override
	public DeviceCommandResult requestPasswordReset()
	{
		DeviceCommandResult result = new DeviceCommandResult();

		try
		{
			// clear notification
			Notifications.remove(Notifications.Type.REQUEST_PASSWORD_RESET);
			
			
			// send intent to request user to reset their password
			Intent intentAction = new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD);
			intentAction.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			Constants.applicationContext.startActivity(intentAction);
			
			Constants.getLog().add(TAG, Type.Info, "Requested passcode reset");
			result.succeeded = true;
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "requestPasswordReset", e);
			
			// return error message
			result.errorMessage = e.getMessage();
		}
		
		
		return result;
	}
	
	
	@Override
	public DeviceCommandResult eraseDevice() 
	{
		DeviceCommandResult result = new DeviceCommandResult();
		
		try
		{
			// erase device
			getDevicePolicyManager().wipeData(0);
			
			Constants.getLog().add(TAG, Type.Info, "Issued wipe data command");
			result.succeeded = true;
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "eraseDevice", e);
			
			// return error message
			result.errorMessage = e.getMessage();
		}
		
		
		return result;
	}
	
	
	@Override
	public DeviceCommandResult enterpriseWipe() 
	{
		DeviceCommandResult result = new DeviceCommandResult();
		
		try
		{
			// enterprise wipe
			
			
			Constants.getLog().add(TAG, Type.Info, "Issued wipe data command");
			result.succeeded = true;
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "eraseDevice", e);
			
			// return error message
			result.errorMessage = e.getMessage();
		}
		
		
		return result;
	}
	
	
	public void setPolicySettings()
	{
		try
		{
			// check if device admin is required
			if (!Constants.getData().getFlag(Flags.FLG_RequireDeviceAdministratorAccess))
			{
				Constants.getLog().add(TAG, Type.Debug, "Device Administrator Access is not required, skipping setting policy settings.");
				return;
			}
			
			// check if device admin is active
			else if (!isDeviceAdministratorActive())
			{
				Constants.getLog().add(TAG, Type.Debug, "Device Administrator is not active, skipping setting policy settings.");
				return;
			}
			
			
			// set policy settings
			Constants.getLog().add(TAG, Type.Debug, "Setting policy settings.");
			
			
			// set settings from database
			try
			{
				// if password is not required
				if (!Constants.getData().getFlag(Flags.FLG_PasswordEnabled))
				{
					// reset required quality level
					getDevicePolicyManager().setPasswordQuality(getComponentDeviceAdminReceiver(), DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED);
					Constants.getLog().add(TAG, Type.Debug, "Set password quality to PASSWORD_QUALITY_UNSPECIFIED");
				}
			}
			catch (Exception e)
			{
				Constants.getLog().add(TAG, Type.Error, "Unable to set password quality", e);
			}
				

			try
			{
				String value = Constants.getData().getSetting(Settings.SET_MinimumPasswordLength);
				
				if (value != null && !value.equals(""))
				{
					int newMinimumPasswordLength = Integer.valueOf(value);
					int oldMinimumPasswordLength = getDevicePolicyManager().getPasswordMinimumLength(getComponentDeviceAdminReceiver());
				
					if (newMinimumPasswordLength != oldMinimumPasswordLength)
					{
						getDevicePolicyManager().setPasswordMinimumLength(getComponentDeviceAdminReceiver(), newMinimumPasswordLength);
						Constants.getLog().add(TAG, Type.Debug, "Set minimum password length");
					}
				}
			}
			catch (Exception e)
			{
				Constants.getLog().add(TAG, Type.Error, "Unable to set minimum password length", e);
			}
				
				
				
			try
			{
				String value = Constants.getData().getSetting(Settings.SET_PasswordQualityType);
				
				if (value != null && !value.equals(""))
				{
					// set new value
					int newPasswordQualityType = 0; 
					
					switch (Integer.valueOf(value))
					{
						case 0:
							newPasswordQualityType = DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED;
							break;
						
						case 1:
							newPasswordQualityType = DevicePolicyManager.PASSWORD_QUALITY_SOMETHING;
							break;
							
						case 2: 
							newPasswordQualityType = DevicePolicyManager.PASSWORD_QUALITY_NUMERIC;
							break;
							
						case 3:
							newPasswordQualityType = DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC;
							break;
							
						case 4:
							newPasswordQualityType = DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC;
							break;
							
						case 5:
							newPasswordQualityType = DevicePolicyManager.PASSWORD_QUALITY_COMPLEX;
							break;
					}
					
					int oldPasswordQualityType = getDevicePolicyManager().getPasswordQuality(getComponentDeviceAdminReceiver());
				
					if (newPasswordQualityType != oldPasswordQualityType)
					{
						getDevicePolicyManager().setPasswordQuality(getComponentDeviceAdminReceiver(), newPasswordQualityType);
						Constants.getLog().add(TAG, Type.Debug, "Set password quality");
					}
				}
			}
			catch (Exception e)
			{
				Constants.getLog().add(TAG, Type.Error, "Unable to set password quality type", e);
			}
				
				
			try
			{
				String value = Constants.getData().getSetting(Settings.SET_MinimumLettersRequired);
				
				if (value != null && !value.equals(""))
				{
					if (Build.VERSION.SDK_INT < 11)
						Constants.getLog().add(TAG, Type.Info, "Minimum letters required password setting requires Android build " + String.valueOf(Build.VERSION.SDK_INT) + " or higher");
					
					else
					{
						int newMinimumLettersRequired = Integer.valueOf(value);
						int oldMinimumLettersRequired = getDevicePolicyManager().getPasswordMinimumLetters(getComponentDeviceAdminReceiver());
					
						if (newMinimumLettersRequired != oldMinimumLettersRequired)
						{
							getDevicePolicyManager().setPasswordMinimumLetters(getComponentDeviceAdminReceiver(), newMinimumLettersRequired);
							Constants.getLog().add(TAG, Type.Debug, "Set password minimum letters");
						}
					}
				}
			}
			catch (Exception e)
			{
				Constants.getLog().add(TAG, Type.Error, "Unable to set minimum password letters required setting", e);
			}
				
			
			try
			{
				String value = Constants.getData().getSetting(Settings.SET_MinimumLowercaseLettersRequired);
		
				if (value != null && !value.equals(""))
				{
					if (Build.VERSION.SDK_INT < 11)
						Constants.getLog().add(TAG, Type.Info, "Password minimum lowercase letters setting requires Android build " + String.valueOf(Build.VERSION.SDK_INT) + " or higher");
					
					else
					{
						int newMinimumLowercaseLettersRequired = Integer.valueOf(value);
						int oldMinimumLowercaseLettersRequired = getDevicePolicyManager().getPasswordMinimumLowerCase(getComponentDeviceAdminReceiver());
					
						if (newMinimumLowercaseLettersRequired != oldMinimumLowercaseLettersRequired)
						{
							getDevicePolicyManager().setPasswordMinimumLowerCase(getComponentDeviceAdminReceiver(), newMinimumLowercaseLettersRequired);
							Constants.getLog().add(TAG, Type.Debug, "Set password minimum lowercase letters");
						}
					}
				}
			}
			catch (Exception e)
			{
				Constants.getLog().add(TAG, Type.Error, "Unable to set minimum lowercase letters required", e);
			}
				

			try
			{
				String value = Constants.getData().getSetting(Settings.SET_MinimumNonLetterCharactersRequired);
				
				if (value != null && !value.equals(""))
				{
					if (Build.VERSION.SDK_INT < 11)
						Constants.getLog().add(TAG, Type.Info, "Minimum non-letter characters password setting requires Android build " + String.valueOf(Build.VERSION.SDK_INT) + " or higher");
					
					else
					{
						int newMinimumNonLetterCharactersRequired = Integer.valueOf(value);
						int oldMinimumNonLetterCharactersRequired = getDevicePolicyManager().getPasswordMinimumNonLetter(getComponentDeviceAdminReceiver());
					
						if (newMinimumNonLetterCharactersRequired != oldMinimumNonLetterCharactersRequired)
						{
							getDevicePolicyManager().setPasswordMinimumNonLetter(getComponentDeviceAdminReceiver(), newMinimumNonLetterCharactersRequired);
							Constants.getLog().add(TAG, Type.Debug, "Set password minimum non-letter characters");
						}
					}
				}
			}
			catch (Exception e)
			{
				Constants.getLog().add(TAG, Type.Error, "Unable to set minimum non-letter password characters", e);
			}
				
				
			
			try
			{
				String value = Constants.getData().getSetting(Settings.SET_MinimumNumericalDigitsRequired);
				
				if (value != null && !value.equals(""))
				{
					if (Build.VERSION.SDK_INT < 11)
						Constants.getLog().add(TAG, Type.Info, "Minimum numerical digits password setting requires Android build " + String.valueOf(Build.VERSION.SDK_INT) + " or higher");
					
					else
					{
						int newMinimumNumericalDigitsRequired = Integer.valueOf(value);
						int oldMinimumNumericalDigitsRequired = getDevicePolicyManager().getPasswordMinimumNumeric(getComponentDeviceAdminReceiver());
					
						if (newMinimumNumericalDigitsRequired != oldMinimumNumericalDigitsRequired)
						{
							getDevicePolicyManager().setPasswordMinimumNumeric(getComponentDeviceAdminReceiver(), newMinimumNumericalDigitsRequired);
							Constants.getLog().add(TAG, Type.Debug, "Set password minimum numeric characters");
						}
					}
				}
			}
			catch (Exception e)
			{
				Constants.getLog().add(TAG, Type.Error, "Unable to set minimum numerical digits password", e);
			}
				
				
			try
			{
				String value = Constants.getData().getSetting(Settings.SET_MinimumSymbolsRequired);
				
				if (value != null && !value.equals(""))
				{
					if (Build.VERSION.SDK_INT < 11)
						Constants.getLog().add(TAG, Type.Info, "Minimum symbols password setting requires Android build " + String.valueOf(Build.VERSION.SDK_INT) + " or higher");
					
					else
					{
						int newMinimumSymbolsRequired = Integer.valueOf(value);
						int oldMinimumSymbolsRequired = getDevicePolicyManager().getPasswordMinimumSymbols(getComponentDeviceAdminReceiver());
					
						if (newMinimumSymbolsRequired != oldMinimumSymbolsRequired)
						{
							getDevicePolicyManager().setPasswordMinimumSymbols(getComponentDeviceAdminReceiver(), newMinimumSymbolsRequired);
							Constants.getLog().add(TAG, Type.Debug, "Set password minimum symbols");
						}
					}
				}
			}
			catch (Exception e)
			{
				Constants.getLog().add(TAG, Type.Error, "Unable to set minimum password symbols", e);
			}
				
				
			try
			{
				String value = Constants.getData().getSetting(Settings.SET_MinimumUppercaseLettersRequired);
				
				if (value != null && !value.equals(""))
				{
					if (Build.VERSION.SDK_INT < 11)
						Constants.getLog().add(TAG, Type.Info, "Minimum uppercase letters password setting requires Android build " + String.valueOf(Build.VERSION.SDK_INT) + " or higher");
					
					else
					{
						int newMinimumUppercaseLettersRequired = Integer.valueOf(value);
						int oldMinimumUppercaseLettersRequired = getDevicePolicyManager().getPasswordMinimumUpperCase(getComponentDeviceAdminReceiver());
					
						if (newMinimumUppercaseLettersRequired != oldMinimumUppercaseLettersRequired)
						{
							getDevicePolicyManager().setPasswordMinimumUpperCase(getComponentDeviceAdminReceiver(), newMinimumUppercaseLettersRequired);
							Constants.getLog().add(TAG, Type.Debug, "Set password minimum uppercase letters");
						}
					}
				}
			}
			catch (Exception e)
			{
				Constants.getLog().add(TAG, Type.Error, "Unable to set minimum uppercase letters password", e);
			}
				
			
			try
			{
				String value = Constants.getData().getSetting(Settings.SET_PasswordExpirationTimeoutMinutes);
				
				if (value != null && !value.equals(""))
				{
					if (Build.VERSION.SDK_INT < 11)
						Constants.getLog().add(TAG, Type.Info, "Password expiration timeout setting requires Android build " + String.valueOf(Build.VERSION.SDK_INT) + " or higher");
					
					else
					{
						long newPasswordExpirationTimeoutMillis = Long.valueOf(value) * 60 * 1000;  // convert to ms
						long oldPasswordExpirationTimeoutMillis = getDevicePolicyManager().getPasswordExpirationTimeout(getComponentDeviceAdminReceiver());
					
						if (newPasswordExpirationTimeoutMillis != oldPasswordExpirationTimeoutMillis)
						{
							getDevicePolicyManager().setPasswordExpirationTimeout(getComponentDeviceAdminReceiver(), newPasswordExpirationTimeoutMillis);
							Constants.getLog().add(TAG, Type.Debug, "Set password expiration timeout");
						}
					}
				}
			}
			catch (Exception e)
			{
				Constants.getLog().add(TAG, Type.Error, "Unable to set password expiration timeout", e);
			}
				
				
			try
			{
				String value = Constants.getData().getSetting(Settings.SET_PasswordHistoryRestriction);
				
				if (value != null && !value.equals(""))
				{
					if (Build.VERSION.SDK_INT < 11)
						Constants.getLog().add(TAG, Type.Info, "Password history restriction setting requires Android build " + String.valueOf(Build.VERSION.SDK_INT) + " or higher");
					
					else
					{
						int newPasswordHistoryRestriction = Integer.valueOf(value);
						int oldPasswordHistoryRestriction = getDevicePolicyManager().getPasswordHistoryLength(getComponentDeviceAdminReceiver());
					
						if (newPasswordHistoryRestriction != oldPasswordHistoryRestriction)
						{
							getDevicePolicyManager().setPasswordHistoryLength(getComponentDeviceAdminReceiver(), newPasswordHistoryRestriction);
							Constants.getLog().add(TAG, Type.Debug, "Set password history length");
						}
					}
				}
			}
			catch (Exception e)
			{
				Constants.getLog().add(TAG, Type.Error, "Unable to set password history restriction value", e);
			}
				
				
				
			try
			{
				String value = Constants.getData().getSetting(Settings.SET_MaximumFailedPasswordAttempts);
				
				if (value != null && !value.equals(""))
				{
					int newMaximumFailedPasswordAttempts = Integer.valueOf(value);
					int oldMaximumFailedPasswordAttempts = getDevicePolicyManager().getMaximumFailedPasswordsForWipe(getComponentDeviceAdminReceiver());
				
					if (newMaximumFailedPasswordAttempts != oldMaximumFailedPasswordAttempts)
					{
						getDevicePolicyManager().setMaximumFailedPasswordsForWipe(getComponentDeviceAdminReceiver(), newMaximumFailedPasswordAttempts);
						Constants.getLog().add(TAG, Type.Debug, "Set maximum failed password attempts");
					}
				}
			}
			catch (Exception e)
			{
				Constants.getLog().add(TAG, Type.Error, "Unable to set maximum failed password attempts", e);
			}
				
				
			try
			{
				String value = Constants.getData().getSetting(Settings.SET_MaximumInactivityTimeoutMinutes);
				
				if (value != null && !value.equals(""))
				{
					long newMaximumInactivityTimeoutMillis = Long.valueOf(value) * 60 * 1000;  // convert to ms
					long oldMaximumInactivityTimeoutMillis = getDevicePolicyManager().getMaximumTimeToLock(getComponentDeviceAdminReceiver());
				
					if (newMaximumInactivityTimeoutMillis != oldMaximumInactivityTimeoutMillis)
					{
						getDevicePolicyManager().setMaximumTimeToLock(getComponentDeviceAdminReceiver(), newMaximumInactivityTimeoutMillis);
						Constants.getLog().add(TAG, Type.Debug, "Set maximum time to lock");
					}
				}
			}
			catch (Exception e)
			{
				Constants.getLog().add(TAG, Type.Error, "Unable to set maximum inactivity timeout", e);
			}
				
				
				
			try
			{
				// attempt to set storage encryption policies from manufacturer API
				boolean isCompliant = false;
				
				if (Constants.getDeviceManagementManufacturer() != null)
					Constants.getDeviceManagementManufacturer().setStorageEncryption();
				
				
				if (!isCompliant && Build.VERSION.SDK_INT < 11)
					Constants.getLog().add(TAG, Type.Info, "Require storage encryption setting requires Android build " + String.valueOf(Build.VERSION.SDK_INT) + " or higher");
				
				else if (!isCompliant)
				{
					// set storage encryption policy with Android SDK if available and not set by manufacturer API
					boolean newRequireStorageEncryption = Constants.getData().getFlag(Flags.FLG_EncryptInternalStorage);
					boolean oldRequireStorageEncryption = getDevicePolicyManager().getStorageEncryption(getComponentDeviceAdminReceiver());
					
					if (newRequireStorageEncryption != oldRequireStorageEncryption)
					{
						getDevicePolicyManager().setStorageEncryption(getComponentDeviceAdminReceiver(), newRequireStorageEncryption);
						Constants.getLog().add(TAG, Type.Debug, "Set storage encryption requirement");
					}
				}
			}
			catch (Exception e)
			{
				Constants.getLog().add(TAG, Type.Error, "Unable to set require storage encryption", e);
			}
				
				
				
			try
			{
				if (Build.VERSION.SDK_INT < 14)
					Constants.getLog().add(TAG, Type.Info, "Disable camera setting requires Android build " + String.valueOf(Build.VERSION.SDK_INT) + " or higher");
				
				else
				{
					boolean newDisableCamera = Constants.getData().getFlag(Flags.FLG_DisableCamera);
					boolean oldDisableCamera = getDevicePolicyManager().getCameraDisabled(getComponentDeviceAdminReceiver());
					
					if (newDisableCamera != oldDisableCamera)
					{
						getDevicePolicyManager().setCameraDisabled(getComponentDeviceAdminReceiver(), newDisableCamera);
						Constants.getLog().add(TAG, Type.Debug, "Set camera enabled status");
					}
				}
			}
			catch (Exception e)
			{
				Constants.getLog().add(TAG, Type.Error, "Unable to set disable camera setting", e);
			}
			
			
			// call manufacturer specific API to set policy settings
			if (Constants.getDeviceManagementManufacturer() != null)
				Constants.getDeviceManagementManufacturer().setPolicySettings();
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "setPolicySettings", e);
		}
	}

	
	public EnumSet<PolicyEnforcementResults> enforcePolicy(PolicyFailureNotificationType notificationType)
	{
		EnumSet<PolicyEnforcementResults> results = EnumSet.noneOf(PolicyEnforcementResults.class);
		

		try
		{
			// enforce policy settings
			Constants.getLog().add(TAG, Type.Verbose, "Enforcing policy settings.");
			
			
			
			// check device administrator access
			if (!isDeviceAdministratorPromptRequired())
			{
				// remove notifications
				Notifications.remove(Notifications.Type.REQUEST_DEVICE_ADMINISTRATION);
			}
			
			else
			{
				Constants.getLog().add(TAG, Type.Debug, "Device administration is required and not active, notifying user");
            	
				// notify failure
				switch (notificationType)
				{
					case SilentExceptRequiredItems:
					case NotifyAndPrompt:
					
						// set notification
						Notifications.setRequestDeviceAdministration();
						
						// request device administration rights
						Constants.getLibrary().requestDeviceAdministration();
						break;
						
					case NotifyOnly:
						
						// set notification
						Notifications.setRequestDeviceAdministration();
						
						break;
						
					case Silent:
					default:
						break;
				}
				
            	
				// set result and return
    	        results.add(PolicyEnforcementResults.Failure);
				results.add(PolicyEnforcementResults.DeviceAdministratorInactive);
				return results;
			}
			
			
			
			// check if ROVA portal credential has been saved
			if (Constants.getData().getSetting(Settings.SET_RovaPortalUserID) != null &&
					!Constants.getData().getSetting(Settings.SET_RovaPortalUserID).equals(""))
			{
				Constants.getLog().add(TAG, Type.Verbose, "ROVA Portal credential check succeeded");
			}
			
			else
			{
				Constants.getLog().add(TAG, Type.Debug, "ROVA Portal credential check failed, notifying user");
				
				// notify failure
				switch (notificationType)
				{
					// always notify user of credential failure regardless of notificationType flag
					case NotifyAndPrompt:
					case SilentExceptRequiredItems:
						
						// add failure notification
						Notifications.setFailedRovaPortalAuthentication();	
						
						// set type and open prompt
			    		Constants.setCredentials(new Credentials(Credentials.PromptType.RovaPortalCredentials, "Login Credentials"));
						Constants.getCredentials().promptForCredentials();
		            	
						break;
						
					case NotifyOnly:
						
						// add failure notification
						Notifications.setFailedRovaPortalAuthentication();	
						
						break;
						
					case Silent:
					default:
						break;
				}
				
				
				// set result and return
    	        results.add(PolicyEnforcementResults.Failure);
				results.add(PolicyEnforcementResults.PortalCredentialsRequired);
				return results;
			}
			
			
			
			// check if GCM ProjectID has been set by the portal
			if (Constants.getData().getSetting(Settings.SET_GCMProjectID) != null &&
					!Constants.getData().getSetting(Settings.SET_GCMProjectID).equals(""))
			{
				// register for GCM if not already registered
	        	if (Constants.getData().getSetting(Settings.SET_GCMRegistrationID) == null ||
	        			Constants.getData().getSetting(Settings.SET_GCMRegistrationID).equals(""))
	        	{
	        		Constants.getLog().add(TAG, Type.Debug, "Registering device for GCM");
		        	Constants.getLibrary().requestNewGCMToken();
	        	}	
			}
			
			
			// if app has initialized
			if (Constants.getData().getFlag(Flags.FLG_HasInitialized))
			{
				// check enrollment policy result (stored in database)
				if (Constants.getData().getFlag(Flags.FLG_EnrollmentPolicyResult))
				{
					Constants.getLog().add(TAG, Type.Verbose, "Enrollment policy check succeeded");
	
					// remove notifications
					Notifications.remove(Notifications.Type.ENROLLMENT_POLICY_FAILURE);
				}
				
				else
				{
					Constants.getLog().add(TAG, Type.Debug, "Enrollment policy failed, notifying user");
					
					
					
					// notify failure
					switch (notificationType)
					{
						// always notify user of enrollment failure regardless of notificationType flag
						case NotifyAndPrompt:
						case SilentExceptRequiredItems:
							
							// add enrollment failure notification
							Notifications.setEnrollmentPolicyFailure();
							
							// open portal credential activity
							Constants.setCredentials(new Credentials(Credentials.PromptType.RovaPortalCredentials, "Login Credentials"));
							Constants.getCredentials().promptForCredentials();

							// show alert dialog with failure details by sending intent to credential prompt
					    	Intent intent = new Intent(Constants.applicationContext, CredentialPromptActivity.class);
					    	intent.setAction(CredentialPromptActivity.ACTION_ALERT_ENROLLMENT_POLICY_FAILURE);	
					    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					    	Constants.applicationContext.startActivity(intent);
					    	
							break;
							
						case NotifyOnly:
							
							// add enrollment failure notification
							Notifications.setEnrollmentPolicyFailure();
							
							break;
							
						case Silent:
						default:
							break;
					}
					
					
					// set result and return
	    	        results.add(PolicyEnforcementResults.Failure);
					results.add(PolicyEnforcementResults.EnrollmentPolicyFailure);
					return results;
				}
			}
			
			
		
			
			// check if user needs to reset their password due to new policy settings
			if (isDeviceAdministratorActive() && isActivePasswordSufficient())
			{
				Constants.getLog().add(TAG, Type.Verbose, "Password complies with current policy");
				
				// remove notifications
				Notifications.remove(Notifications.Type.REQUEST_PASSWORD_RESET);
			}
			
			else if (isDeviceAdministratorActive())
			{
				Constants.getLog().add(TAG, Type.Info, "Password failed compliancy check");
				
				// notify failure
				switch (notificationType)
				{
					case SilentExceptRequiredItems:
					case NotifyAndPrompt:
						requestPasswordReset();
						Notifications.setPasswordChangeRequired();
						break;
						
					case NotifyOnly:
						Notifications.setPasswordChangeRequired();
						break;
						
					case Silent:
					default:
						break;
				}
				

				// set result and return
				results.add(PolicyEnforcementResults.Failure);
				results.add(PolicyEnforcementResults.PasswordNotSufficient);
				return results;
			}
			
			
			
			
			// check if location access is required, and if access is available
			if (!Constants.getData().getSetting(Settings.SET_ReportDeviceLocation).equals("1"))
			{
				Constants.getLog().add(TAG, Type.Verbose, "Location access is not required");
				
				// remove notifications
				Notifications.remove(Notifications.Type.LOCATION_ACCESS_REQUIRED);
			}
			
			else if (Constants.getLibrary().hasLocationAccess())
			{
				Constants.getLog().add(TAG, Type.Verbose, "Location access is required and available");
				
				// remove notifications
				Notifications.remove(Notifications.Type.LOCATION_ACCESS_REQUIRED);
			}
			
			else
			{
				Constants.getLog().add(TAG, Type.Info, "Location access is required but not available");
				
				// notify failure
				switch (notificationType)
				{
					case SilentExceptRequiredItems:
					case NotifyAndPrompt:

						// prompt
						Constants.getLibrary().promptForLocationAccess();
						
						// notify
						Notifications.setLocationAccessRequired();
						break;
						
					case NotifyOnly:
						
						// notify
						Notifications.setLocationAccessRequired();
						
						break;
						
					case Silent:
					default:
						break;
				}
				

				// set result and return
				results.add(PolicyEnforcementResults.Failure);
				results.add(PolicyEnforcementResults.LocationAccessRequired);
				return results;
			}
			
			
			
			
			// check if user has installed all required apps
			boolean hasRequiredApps = true;
			
			// get assigned application list
			List<ApplicationAssignedInfo> listAssignedApps = Constants.getData().getAssignedApplications();
			
			// get installed application list, include system packages
			List<ApplicationInstalledInfo> listInstalledApps =	Constants.getLibrary().getInstalledApplications(true);
			
			for (ApplicationAssignedInfo assignedApp : listAssignedApps)
			{
				boolean isInstalled = false;
				
				for (ApplicationInstalledInfo installedApp : listInstalledApps)
				{
					if (!assignedApp.packageName.equalsIgnoreCase(installedApp.packageName))
						continue;
					
					// check that installed version is greater or equal to the assigned version
					isInstalled = AbstractLibrary.isInstalledSoftwareVersionCurrent(installedApp.version, assignedApp.version);	
					
					break;
				}
				
				// set flag for mandatory apps that are not installed
				if (!isInstalled && assignedApp.mandatory)
					hasRequiredApps = false;
			}
			
			
			if (hasRequiredApps)
			{
				Constants.getLog().add(TAG, Type.Verbose, "User has all required applications installed");
			
				// remove notifications
				Notifications.remove(Notifications.Type.NOTIFY_APPLICATIONS_REQUIRED);
			}
			
			else
			{
				// send intent to tab host activity for remediation
				Constants.getLog().add(TAG, Type.Verbose, "Not all required applications are installed");
				
				// notify failure
				switch (notificationType)
				{
					case NotifyAndPrompt:
						Constants.getLibrary().notifyApplicationsRequired();
						Notifications.setApplicationsRequired();
						break;
						
					case NotifyOnly:
						Notifications.setApplicationsRequired();
						break;
						
					case SilentExceptRequiredItems:
					case Silent:
					default:
						break;
				}
				
					
				// set result and return
				results.add(PolicyEnforcementResults.Failure);
				results.add(PolicyEnforcementResults.RequiredAppsNotInstalled);
				return results;
			}
			
			
			// add compliant flag if no checks failed
			if (results.isEmpty())
			{
				results.add(PolicyEnforcementResults.Compliant);
				Constants.getLog().add(TAG, Type.Debug, "Passed policy check");
			}
			else
				Constants.getLog().add(TAG, Type.Debug, "Failed policy check");
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "enforcePolicy", e);
			
			// set failure result
			results.remove(PolicyEnforcementResults.Compliant);
			results.add(PolicyEnforcementResults.Failure);
		}
		
		
		return results;
	}
}
