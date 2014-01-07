package com.quintech.common;

import java.util.Iterator;
import java.util.List;

import com.quintech.common.AbstractConstants.BuildType;
import com.quintech.common.AbstractData.Flags;
import com.quintech.common.AbstractData.Settings;
import com.quintech.common.AbstractDeviceManagement.PolicyFailureNotificationType;
import com.quintech.common.ClientSettingInfo;
import com.quintech.common.FailedAuthenticationInfo;
import com.quintech.common.HotspotInfo;
import com.quintech.common.ILog.Type;
import com.quintech.common.SessionInfo;
import com.quintech.common.VZWDirectoryServices;
import com.quintech.common.VZWHotspotParser;




public class Configuration 
{
	private static String TAG = "Configuration";
	private static boolean inProgress = false;
	
	
	public static boolean isInProgress()
	{
		return inProgress;
	}
	
	
	public static void update(boolean forceRefresh)
	{
		try
		{
			// check flag
			if (!inProgress)
			{
				// set flag
				inProgress = true;
				
				AbstractConstants.log.add(TAG, Type.Debug, "Checking if application configuration requires an update.");
								
				// update all data if application has not been initialized
				if (!AbstractConstants.data.getFlag(Flags.FLG_HasInitialized))
				{
					forceRefresh = true;
					AbstractConstants.log.add(TAG, Type.Info, "Application configuration has not been initialized yet.");
				}
				
				// update when forceRefresh is true, or time since last updated exceeds interval
				if (forceRefresh || getHoursSinceLastUpdate() > AbstractConstants.getUpdateConfigurationIntervalInHours())
				{
					AbstractConstants.log.add(TAG, Type.Info, "Updating application configuration.");
					boolean result = false;
					
					
					// perform check here for build type
					if (AbstractConstants.getApplicationBuildType() == BuildType.VERIZON_WIRELESS)
						result = updateVerizonWireless(forceRefresh);
					
					else if (AbstractConstants.getApplicationBuildType() == BuildType.VERIZON_BUSINESS)
						result = updateVerizonBusiness(forceRefresh);
					
					
					// check result
					if (result)
					{
						// update configuration "last updated" date
						AbstractConstants.data.setSetting(Settings.SET_ConfigurationLastUpdatedMillis, String.valueOf(System.currentTimeMillis()), false);
						
						// flag as initialized
						AbstractConstants.data.setFlag(Flags.FLG_HasInitialized, true, false);
					}
					
					AbstractConstants.log.add(TAG, Type.Info, "Application configuration update completed.");
				}
				else
				{
					AbstractConstants.log.add(TAG, Type.Debug, "Skipped updating application configuration.");
				}

				
				// delete old log files
				AbstractConstants.library.deleteLogFiles();
				
				
				// reset flag when complete
				inProgress = false;
			}
			else
			{
				AbstractConstants.log.add(TAG, Type.Debug, "Skipping update configuration because it is already in progress.");
			}
		}
		catch (Exception e)
	    {
			AbstractConstants.log.add(TAG, Type.Error, "Update", e);
	    	
	    	// reset flag
			inProgress = false;
	    }
		finally
		{
			// refresh UI after update
			AbstractConstants.library.refreshUI();
		}
	}

	
	private static boolean updateVerizonWireless(boolean forceRefresh)
	{
		boolean result = false;
		
		try
		{
			// if we're forcing a refresh, or we have a WiFi connection
			if (forceRefresh || AbstractConstants.library.isConnectedToWiFi())
			{
				// update global hotspots
				
				AbstractConstants.log.add(TAG, Type.Info, "Updating Verizon Wireless global hotspot list.");
				
				// get current version
				float currentVersion = AbstractConstants.data.getHotspotVersion(VZWDirectoryServices.DIRECTORY_GUID);
				VZWHotspotParser ghParser = VZWDirectoryServices.getGlobalSSIDs(currentVersion);
				
				if (ghParser != null)
				{
					if (ghParser.version > currentVersion)
					{
						// delete all previous data 
						AbstractConstants.data.deleteHotspots(VZWDirectoryServices.DIRECTORY_GUID);
						
						// insert new data
						for (HotspotInfo hotspot : ghParser.hotspotList)
							AbstractConstants.data.insertHotspot(VZWDirectoryServices.DIRECTORY_GUID, hotspot);
						
						AbstractConstants.log.add(TAG, Type.Info, "Updated Verizon Wireless global hotspot list.");
					}
					else
					{
						AbstractConstants.log.add(TAG, Type.Info, "Verizon Wireless global hotspot list is already up-to-date.");
					}
				}
				else
				{
					// unable to update global hotspot list
					AbstractConstants.log.add(TAG, Type.Info, "No results from Verizon Wireless global hotspot directory query, list has not been updated.");
				}
				
				
				
				// update configuration data
				
				AbstractConstants.log.add(TAG, Type.Info, "Updating Verizon Wireless client settings.");
				List<ClientSettingInfo> listClientSettings = VZWDirectoryServices.getDefaultClientSettings();
				
				// update database setting values
				AbstractConstants.data.updateDatabaseSettingValues(listClientSettings);
				AbstractConstants.log.add(TAG, Type.Info, "Updated Verizon Wireless client settings.");
				
				
				// upload error log data
				if (!AbstractConstants.data.getFlag(Flags.FLG_EnableSessionReporting))
				{
					AbstractConstants.log.add(TAG, Type.Info, "Reporting failed authentications is disabled, no data has been sent.");
				}
				else
				{
					AbstractConstants.log.add(TAG, Type.Info, "Reporting failed authentications.");
					
					// get list of failed authentications
					List<FailedAuthenticationInfo> listFailedAuthentications = AbstractConstants.data.getNewFailedAuthentications();
					
					// pass list to directory services to update
					boolean postResult = VZWDirectoryServices.postFailedAuthentications(listFailedAuthentications);
					
					// update database records as submitted if successful
					if (postResult)
						AbstractConstants.data.setFailedAuthenticationsSubmittedDate(listFailedAuthentications);
					
					AbstractConstants.log.add(TAG, Type.Info, "Completed failed authentications report.");
				}
			}
			else
			{
				AbstractConstants.log.add(TAG, Type.Warn, "Skipped updating global hotspots, venues and configuration data due to lack of Wi-Fi connection.");
			}
			

			// upload session data over any connection type
			if (!AbstractConstants.data.getFlag(Flags.FLG_EnableSessionReporting))
			{
				AbstractConstants.log.add(TAG, Type.Info, "Session reporting is disabled, no data has been sent.");
			}
			else
			{
				AbstractConstants.log.add(TAG, Type.Info, "Reporting session data.");
				
				// get list of sessions
				List<SessionInfo> listSessions = AbstractConstants.data.getNewSessions();
				
				boolean postResult = false;
				
				// pass list to directory services to update
				postResult = VZWDirectoryServices.postSessions(listSessions);
	    			
				// update database records as submitted if successful
				if (postResult)
					AbstractConstants.data.setSessionsSubmittedDate(listSessions);
				
				AbstractConstants.log.add(TAG, Type.Info, "Completed session data report.");
			}
			
			// set return result
			result = true;
		}
		catch (Exception e)
	    {
			AbstractConstants.log.add(TAG, Type.Error, "updateVerizonWireless", e);
	    }
		
		
		return result;
	}
	
	
	private static boolean updateVerizonBusiness(boolean forceRefresh)
	{
		boolean result = false;
		
		try
		{		
			// TODO
			// implement portal settings to determine whether or not 
			// to allow updates over cellular connection
			
			//// if we're forcing a refresh, or we have a WiFi connection
			//if (forceRefresh || AbstractConstants.library.isConnectedToWiFi())
			
			// for now, update regardless of the connection
			
			
			// if no User ID has been set
			if (AbstractConstants.data.getSetting(Settings.SET_RovaPortalUserID) == null ||
					AbstractConstants.data.getSetting(Settings.SET_RovaPortalUserID).equals(""))
			{
				AbstractConstants.log.add(TAG, Type.Warn, "No User ID has been set, configuration update has been skipped");
				return false;
			}
				
			
			// update configuration data
			VZBResultParser parser = VZBDirectoryServices.update();
			
			// check result code and password validation result
			if (parser != null && 
					parser.isResultCodeValid &&
					parser.isPasswordValidated)
			{
				// update enrollment policy results
				AbstractConstants.data.setFlag(Flags.FLG_EnrollmentPolicyResult, parser.enrollmentPolicyResult.succeeded, false);
				AbstractConstants.data.setSetting(Settings.SET_EnrollmentPolicyMessage, parser.enrollmentPolicyResult.message, false);
				
				// update client settings
				
				// set old value for comparison
				String oldGCMProjectID = AbstractConstants.data.getSetting(Settings.SET_GCMProjectID);
				
				// save all posted values, included policy settings
				AbstractConstants.data.updateClientSettings(parser.clientSettings);
				
				// set specific settings
				AbstractConstants.data.setFlag(Flags.FLG_HasVerizonNetworkAccount, parser.hasHasVerizonNetworkAccount, true);
				AbstractConstants.data.setSetting(Settings.SET_VerizonNetworkPassword, parser.verizonNetworkPassword, true);
				
				// check if GCMProjectID has been updated, request new token if needed
				String newGCMProjectID = AbstractConstants.data.getSetting(Settings.SET_GCMProjectID);
				if (newGCMProjectID != null && !newGCMProjectID.equals("") && !newGCMProjectID.equals(oldGCMProjectID))
				{
					AbstractConstants.log.add(TAG, Type.Info, "GCM ProjectID has been updated to: " + newGCMProjectID);
					AbstractConstants.library.requestNewGCMToken();
				}
				
				
				// update Smart Connect Allowed Connection Types
				AbstractConstants.data.deleteSmartConnectAllowedConnectionTypes();
				AbstractConstants.data.insertSmartConnectAllowedConnectionTypes(parser.smartConnectAllowedConnectionTypes);
				
				//Save Exchange settings
				for (Iterator<ExchangeSettings> i = parser.assignedExchangeServers.iterator(); i.hasNext();)
				{
					ExchangeSettings item = i.next();
					AbstractConstants.data.insertExchange(item);	
				}

				
				// apply policy settings from database values
				AbstractConstants.deviceManagement.setPolicySettings();
				
				
				// log processed issued commands
				AbstractConstants.data.setDeviceCommandIssuedReceivedByPortal(parser.processedIssuedCommandGuids);
				
				
				// insert newly issued device commands into database (will check if CommandGUID already exists)
				AbstractConstants.data.insertDeviceCommandsIssued(parser.issuedDeviceCommands);
				
				
				// execute all queued commands
				boolean requestSubsequentCheckIn = AbstractConstants.deviceManagement.executeDeviceCommands();
				
				// if flag is true, call update() again to send latest results to the portal
				if (requestSubsequentCheckIn)
				{
					VZBDirectoryServices.update();
				}
				
				
				// update directories
				DirectoryImporter.importDirectories(parser.assignedDirectories);
				
				// update directory rankings
				for (int i = 0; i < parser.smartConnectRankedDirectoriesSysIDs.size(); i++)
					AbstractConstants.data.setDirectorySmartConnectRanking(parser.smartConnectRankedDirectoriesSysIDs.get(i), i);
				
				
				// update shortcuts
				AbstractLibrary.importShortcuts(parser.shortcuts);
				
				
				// update assigned applications
				AbstractConstants.library.importAssignedApplications(parser.assignedApplications);
				
				
				// set successful result
				result = true;
			

				// enforce policy settings
				
				// only show notifications if our application is not visible
				if (!AbstractConstants.library.isAppInForeground())
					AbstractConstants.deviceManagement.enforcePolicy(PolicyFailureNotificationType.NotifyOnly);
				
				else
					AbstractConstants.deviceManagement.enforcePolicy(PolicyFailureNotificationType.NotifyAndPrompt);
			}
		}
		catch (Exception e)
	    {
			AbstractConstants.log.add(TAG, Type.Error, "updateVerizonBusiness", e);
	    }
		
		
		return result;
	}
	
	
	private static int getHoursSinceLastUpdate()
	{
		// set default value to 10 days
		int numberOfHours = 10 * 24;
		
		
		try
		{
			// get value from database
			long lastUpdatedMillis = Long.valueOf(AbstractConstants.data.getSetting(Settings.SET_ConfigurationLastUpdatedMillis));
			numberOfHours = Math.round((System.currentTimeMillis() - lastUpdatedMillis) / (1000 * 60 * 60));
			AbstractConstants.log.add(TAG, Type.Debug, "Number of hours since last configuration update: " + String.valueOf(numberOfHours));
		}
		catch (Exception e)
	    {
			AbstractConstants.log.add(TAG, Type.Error, "getHoursSinceLastUpdate", e);
			
			// clear value that was not able to be parsed
			AbstractConstants.data.setSetting(Settings.SET_ConfigurationLastUpdatedMillis, "", false);
	    }
		
		return numberOfHours;
	}
}
