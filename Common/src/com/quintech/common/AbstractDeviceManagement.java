package com.quintech.common;

import java.util.EnumSet;
import java.util.List;

import com.quintech.common.DeviceCommandIssuedInfo.DeviceCommands;
import com.quintech.common.ILog.Type;


public abstract class AbstractDeviceManagement 
{
	private static String TAG = "AbstractDeviceManagement";
	
	
	public enum PolicyEnforcementResults
	{
		Compliant,
		EnrollmentPolicyFailure,
		DeviceAdministratorInactive,
		PasswordNotSufficient,
		RequiredAppsNotInstalled,
		LocationAccessRequired,
		PortalCredentialsRequired,
		Failure
	}
	
	
	public enum PolicyFailureNotificationType
	{
		Silent,
		SilentExceptRequiredItems,
		NotifyAndPrompt,
		NotifyOnly
	}
	
	
	public class DeviceCommandResult
	{
		public boolean succeeded;
		public String errorMessage;
		
		public DeviceCommandResult()
		{
			succeeded = false;
			errorMessage = null;
		}
	}
	
	public abstract boolean isDeviceAdministratorActive();
	public abstract void removeDeviceAdministrator();
	public abstract boolean isActivePasswordSufficient();
	public abstract Boolean isInternalStorageEncrypted();
	public abstract Boolean isExternalStorageEncrypted();
	public abstract DeviceCommandResult lockDevice();
	public abstract DeviceCommandResult clearPasscode();
	public abstract DeviceCommandResult requestPasswordReset();
	public abstract DeviceCommandResult eraseDevice();
	public abstract DeviceCommandResult enterpriseWipe();
	public abstract void setPolicySettings();
	public abstract EnumSet<PolicyEnforcementResults> enforcePolicy(PolicyFailureNotificationType notificationType);
	
	public void initialize()
	{
		// perform any initialization tasks here
		
	}
	
	
	public boolean executeDeviceCommands()
	{
		boolean requestSubsequentCheckIn = false;
		
		try
		{
			// get un-executed commands
			List<DeviceCommandIssuedInfo> deviceCommands = AbstractConstants.data.getDeviceCommandsIssued(false, true);
			
			if (deviceCommands == null)
			{
				return false;
			}
			
			
			// process each command in the list
			for (DeviceCommandIssuedInfo deviceCommand : deviceCommands)
			{
				try
				{
					AbstractConstants.log.add(TAG, Type.Debug, "Executing command " + deviceCommand.command.toString());
					
					// execute command
					if (deviceCommand.command == DeviceCommands.LockDevice)
					{
						// execute and log result
						DeviceCommandResult result = lockDevice();
						AbstractConstants.data.setDeviceCommandIssuedExecuted(deviceCommand.commandGuid, result.succeeded, result.errorMessage);
						AbstractConstants.log.add(TAG, Type.Debug, "Executed command " + deviceCommand.command.toString() + ". Result: " + String.valueOf(result.succeeded));
					
						// set flag to send results to portal
						requestSubsequentCheckIn = true;
					}
					
					else if (deviceCommand.command == DeviceCommands.RefreshData)
					{
						// log result
						AbstractConstants.data.setDeviceCommandIssuedExecuted(deviceCommand.commandGuid, true, null);
						
						// set flag to send device data to portal
						requestSubsequentCheckIn = true;
					}
					
					else if (deviceCommand.command == DeviceCommands.ClearPasscode)
					{
						// execute and log result
						DeviceCommandResult result = clearPasscode();
						AbstractConstants.data.setDeviceCommandIssuedExecuted(deviceCommand.commandGuid, result.succeeded, result.errorMessage);
						AbstractConstants.log.add(TAG, Type.Debug, "Executed command " + deviceCommand.command.toString() + ". Result: " + String.valueOf(result.succeeded));
					
						// set flag to send results to portal
						requestSubsequentCheckIn = true;
					}
					
					else if (deviceCommand.command == DeviceCommands.RequestPasscodeReset)
					{
						// execute and log result
						DeviceCommandResult result = requestPasswordReset();
						AbstractConstants.data.setDeviceCommandIssuedExecuted(deviceCommand.commandGuid, result.succeeded, result.errorMessage);
						AbstractConstants.log.add(TAG, Type.Debug, "Executed command " + deviceCommand.command.toString() + ". Result: " + String.valueOf(result.succeeded));
					
						// set flag to send results to portal
						requestSubsequentCheckIn = true;
					}
					
					else if (deviceCommand.command == DeviceCommands.EraseDevice)
					{
						// execute and log result -- note a successful result might not make it back to the portal
						DeviceCommandResult result = eraseDevice();
						AbstractConstants.data.setDeviceCommandIssuedExecuted(deviceCommand.commandGuid, result.succeeded, result.errorMessage);
						AbstractConstants.log.add(TAG, Type.Debug, "Executed command " + deviceCommand.command.toString() + ". Result: " + String.valueOf(result.succeeded));
					
						// set flag to send results to portal
						requestSubsequentCheckIn = true;
					}
					
					else
					{
						// unknown command
						
						// log failed command result
						AbstractConstants.data.setDeviceCommandIssuedExecuted(deviceCommand.commandGuid, false, "Unknown command");
						AbstractConstants.log.add(TAG, Type.Debug, "Command " + deviceCommand.command.toString() + " is unknown.");
					}
				}
				catch (Exception e)
				{
					if (deviceCommand != null && deviceCommand.commandGuid != null)
						AbstractConstants.log.add(TAG, Type.Error, "Error processing command: " + deviceCommand.commandGuid, e);
					else
						AbstractConstants.log.add(TAG, Type.Error, "Error processing command", e);
				}
			}
		}
		catch (Exception e)
		{
			AbstractConstants.log.add(TAG, Type.Error, "processDeviceCommands", e);
		}
		
		
		// return flag 
		return requestSubsequentCheckIn;
	}
}
