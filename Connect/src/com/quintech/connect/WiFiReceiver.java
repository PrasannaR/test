package com.quintech.connect;

import java.util.EnumSet;
import java.util.List;

import com.quintech.common.AbstractConstants.BuildType;
import com.quintech.common.AbstractData.Flags;
import com.quintech.common.AbstractDeviceManagement.PolicyEnforcementResults;
import com.quintech.common.AbstractDeviceManagement.PolicyFailureNotificationType;
import com.quintech.common.Credentials;
import com.quintech.common.DirectoryInfo;
import com.quintech.common.ILog.Type;
import com.quintech.common.SmartConnectAllowedConnectionTypes.ConnectionType;
import com.quintech.common.HotspotInfo;
import com.quintech.common.Session;
import com.quintech.common.WISPrConnect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration.KeyMgmt;

public class WiFiReceiver extends BroadcastReceiver 
{
	private static String TAG = "WiFiReceiver";
	private WifiManager wifimgr;
	private WISPrConnect wisprAttempt;
	 


	public WiFiReceiver(WifiManager wifi) 
	{
		super();
		
		this.wifimgr = wifi;
	}


	public int startWISPrTask()
	{
		int wispr_result = 0;
		
		try
		{	
			// check if login is required for connected SSID
			HotspotInfo hotspotInfo = Constants.getData().isSSIDLoginRequired(wifimgr.getConnectionInfo().getSSID());
			

			if (hotspotInfo == null || !hotspotInfo.loginRequired) 
			{
				// return connected response if no login is required
				wispr_result = 50;
				
				// clear notifications
				Notifications.removeAllConnectionNotifications(null);
				
				return wispr_result;
			}
		
			
			Constants.getLog().add(TAG, Type.Debug, "startWISPrTask() InProgress flag: " + String.valueOf(Constants.isWISPrInProgress));
			
	    	if (Constants.isWISPrInProgress == false)
	    	{
	    		Constants.isWISPrInProgress = true;
	    		
	    		// create "authenticating" notification
				if (Constants.getApplicationBuildType() == BuildType.VERIZON_BUSINESS)
					Notifications.setAuthenticatingWISPr(hotspotInfo.ssid);
	    		

	    		// Reset WIPSr logged in flag
	    		Constants.isWISPrLoggedIn = false;
	    		    			    		
	    		wisprAttempt = new WISPrConnect(hotspotInfo);

	    		wispr_result =  wisprAttempt.connect(WISPrConnect.getProbeURL());
	    		
	    		Constants.getLog().add(TAG, Type.Info, "WISPr Result Code: " + String.valueOf(wispr_result));
	    		
	    		// send intent to top most activity to refresh the UI
	    		Constants.getLibrary().refreshUI();
	    		
				switch (wispr_result)
				{
					// NEED TO SWITCH to defined values listed in WISPrConnect
					case 50:
						
						// reset Smart Connect
						Constants.getSmartConnect().reset();

						// create new session object
		                if (Constants.getSession() == null)
		                	Constants.setSession(new Session());
		                 
		        		// start new session
		                Constants.getSession().start();
		                
		                // save WISPr session properties
		                Constants.getSession().setWISPrProperties(	wisprAttempt.wisprLoginURL, 
		                											wisprAttempt.wisprLogoffURL,
																	wisprAttempt.wisprbwUserGroup, 
																	wisprAttempt.wisprLocationName, 
																	wisprAttempt.wisprLocationID);								                    
		                Constants.isWISPrLoggedIn = true; 
	                    
	                    // Create "authenticated" notification
	                    Notifications.setAuthenticatedWISPr(wifimgr.getConnectionInfo().getSSID());
	                    
						break;
						
						
					
					case 1025:	// Existing WISPr session
						
						// reset Smart Connect
						Constants.getSmartConnect().reset();
						
						// Attempt to continue session record
						Constants.isWISPrLoggedIn = true;
						
						// create new session object
	                    if (Constants.getSession() == null)
	                    	Constants.setSession(new Session());
	                    	                    	                    
	                    Constants.getSession().continueSession();
	                    
	                    
	                    // Create "authenticated" notification
	                    Notifications.setAuthenticatedWISPr(wifimgr.getConnectionInfo().getSSID());
	
						break;
						
						
						
					// failed credentials
					case 100:

					// missing credentials
					case 1027: 
						
						DirectoryInfo directoryInfo = Constants.getData().getDirectoryInfo(hotspotInfo.directoriesPortalSysID);
						
						// if UI is being shown, device credentials are NOT being used, and directory credential is not being used
						if (Constants.getData().getFlag(Flags.FLG_EnableFullUserInterface) && 
								!Constants.getData().getFlag(Flags.FLG_UseVerizonWirelessCredentials) &&
								(directoryInfo.userId == null || directoryInfo.userId.equals("")) &&
								(directoryInfo.password == null || directoryInfo.password.equals(""))
								)
						{
							// re-prompt user
							Credentials credentials = new Credentials(Credentials.PromptType.NetworkCredentials, "Invalid credentials - Try again to connect");
							
							// set callback to process when the credentials have been entered
							credentials.runnableCompleted = new Runnable() 
							{
								@Override
								public void run() 
								{
									startWISPrTask();
								}
							};	
							
							// notify failure if the user cancels the prompt
							credentials.runnableCanceled = new Runnable() 
							{
								@Override
								public void run() 
								{
									Notifications.setFailedWISPrAuthentication(Constants.getLibrary().getConnectedSSID());
									
									// disconnect
									disconnectAP(true);
								}
							};	
							
							Constants.setCredentials(credentials);

							// show credential prompt, will run runnableCompleted or runnableCanceled			
							credentials.promptForCredentials();						
	
							break;
						}
						else
						{
							// Credentials failed, since there is no UI shown, fall through to default error case
						}

					case 1028:	
					case 1024:	// Not logged in using wispr but was able to browse the web.  This is an error according to spec.	
					default:

						// stop existing session
						if (Constants.getSession() != null)
							Constants.getSession().stop();
						
						
						// build failed authentication record
						String ssid = null;
						String bssid = null;
						String latitude = null;
						String longitude = null;
						
						if (wifimgr != null && wifimgr.getConnectionInfo() != null)
						{
							if (wifimgr.getConnectionInfo().getSSID() != null)
								ssid = wifimgr.getConnectionInfo().getSSID();
							
							if (wifimgr.getConnectionInfo().getBSSID() != null)
								bssid = wifimgr.getConnectionInfo().getBSSID(); 
						}
						
						
						// build WISPr xml log string
						if (wisprAttempt.wisprXmlLog != null)
						{
							wisprAttempt.wisprXmlLog.insert(0, "<xml>\n");
							wisprAttempt.wisprXmlLog.append("\n</xml>");
						}
						else
							wisprAttempt.wisprXmlLog = new StringBuilder();
						
						
						// log WISPr authentication error
						Constants.getData().insertFailedAuthentication(new java.util.Date(), latitude, longitude, 
																			ssid, bssid, wisprAttempt.wisprXmlLog.toString(), 
																			String.valueOf(wispr_result));
						
						// Add failed notification
						if (wispr_result == 1028 || wispr_result == 1024)
						{
							Notifications.setUntrustedAP(ssid);
						}
						else
						{
							Notifications.setFailedWISPrAuthentication(ssid);							
						}
						
						
						// disconnect
						this.disconnectAP(true);
						
						break;
				}
				
				Constants.isWISPrInProgress = false;
		    }
		}
		catch (Exception e)
		{        	
        	Constants.getLog().add(TAG, Type.Error, "startWISPrTask", e);
        	
    		e.printStackTrace();
    		
    		// clear flag
    		Constants.isWISPrInProgress = false;
		}
		
	    return wispr_result;
	}
	
    
	@Override
	public void onReceive(Context c, Intent intent) 
	{
		try
		{
			// process onReceive in a new thread
			final Context finalContext = c;
			final Intent finalIntent = intent;
			
			Thread t = new Thread() 
	        {
	            public void run() 
	            {
	            	processOnReceive(finalContext, finalIntent);
	            }
	        };
	          
	        t.start();
		}
		catch (Exception e)
		{        	
        	Constants.getLog().add(TAG, Type.Error, "onReceive", e);
		}
	}
	

	private void processOnReceive(Context c, Intent intent) 
	{		
		WifiInfo connectionInfo = null;
		SupplicantState ssState;

    	
    	try
    	{
    		// send intent to top most activity to refresh the UI
    		Constants.getLibrary().refreshUI();
    		
    		
			ConnectivityManager conMan = (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
						
			Constants.getLog().add(TAG, Type.Debug, intent.getAction().toString());
			
			connectionInfo = wifimgr.getConnectionInfo();
			ssState = connectionInfo.getSupplicantState();
						
			
			if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) 
			{
				// reload application scan list
				Constants.wifiScanList = wifimgr.getScanResults();	  		 			

		        
		        // if not currently connected or connecting
		        if (networkInfo == null || 
	        			(networkInfo.getTypeName().equalsIgnoreCase("WIFI") && !networkInfo.isConnectedOrConnecting()))
		        {
		        	// initiate Smart Connect
		        	if (!Constants.getSmartConnect().getHasStarted())
		        		Constants.getSmartConnect().start(false);
		        	else
		        		Constants.getSmartConnect().attemptNextConnectionAsnyc();
		        }
			}
			else if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION))
			{
				if (wifimgr.getWifiState() == WifiManager.WIFI_STATE_DISABLED)
				{
					Constants.getLog().add(TAG, Type.Debug, "Detected disabled Wi-Fi state, removing notifications");
					Notifications.removeAllConnectionNotifications(null);	
					
					// stop existing session
					if (Constants.getSession() != null)
						Constants.getSession().stop();
					
					// reset Smart Connect
					Constants.getSmartConnect().reset();
				}
			}
			else if (intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, 0) ==  WifiManager.ERROR_AUTHENTICATING)
			{
				int error = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, 0);
								
				Constants.getLog().add(TAG, Type.Debug, "Error Authenticating: " + String.valueOf(error));
				
				
				// show error notification if SSID is in the directory
				
				String ssid = "";
				if (connectionInfo != null && connectionInfo.getSSID() != null)
					ssid = connectionInfo.getSSID();
				
				if (Constants.getData().isSSIDInDirectory(ssid, ConnectionType.Unknown) != null)
					Notifications.setFailedToAssociate(ssid);
				
				// stop existing session
				if (Constants.getSession() != null)
					Constants.getSession().stop();
				
				// attempt next Smart Connect connection
	        	Constants.getSmartConnect().attemptNextConnectionAsnyc();
			}
			else if (intent.getAction().equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION))
			{
	        	if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false))
	        	{	        		
	        		Constants.getLog().add(TAG, Type.Info, "Connected");
	        	}								
			}			
			else if (intent.getAction().equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) 
			{
				Constants.getLog().add(TAG, Type.Debug, ssState.toString());
				
				if (ssState == SupplicantState.SCANNING)
					return;
				
				else if (ssState == SupplicantState.ASSOCIATING)
					Constants.getLog().add(TAG, Type.Info, "Associating");

				else if (ssState == SupplicantState.ASSOCIATED)
					Constants.getLog().add(TAG, Type.Info, "Associated");

		        else if (ssState == SupplicantState.DISCONNECTED)
		        {		        	
	        		Constants.getLog().add(TAG, Type.Info, "Disconnected");
	        		
		        	// Disconnected
					Notifications.remove(Notifications.Type.CONNECTING);
					Notifications.remove(Notifications.Type.AUTHENTICATING_WISPR);
					Notifications.remove(Notifications.Type.AUTHENTICATED_WISPR);
					
					// stop existing session
					if (Constants.getSession() != null)
						Constants.getSession().stop();
		        }
		        else if (ssState == SupplicantState.COMPLETED)
		        {
		        	Constants.getLog().add(TAG, Type.Info, "Completed");	        	
		        }	
			}
		    else if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) 
			{
		    	Constants.getLog().add(TAG, Type.Debug, networkInfo.getDetailedState().toString());
		    	
		    	if (networkInfo.getExtraInfo() != null)
		    		Constants.getLog().add(TAG, Type.Debug, networkInfo.getExtraInfo());
		    	
				if (networkInfo != null && networkInfo.isConnected())
				{									
					Constants.getLog().add(TAG, Type.Info, "Network state is connected with IP " + connectionInfo.getIpAddress());
					
					// attempt next Smart Connect connection - this will initiate Wispr login if required
		        	Constants.getSmartConnect().attemptNextConnectionAsnyc();
				}
				
				else if (networkInfo != null && !networkInfo.isConnectedOrConnecting())
				{
					// Disconnected
					Notifications.remove(Notifications.Type.CONNECTING);
					Notifications.remove(Notifications.Type.AUTHENTICATING_WISPR);
					Notifications.remove(Notifications.Type.AUTHENTICATED_WISPR);
					
					// stop existing session
					if (Constants.getSession() != null)
						Constants.getSession().stop();
					
					// attempt next Smart Connect connection
		        	Constants.getSmartConnect().attemptNextConnectionAsnyc();
				}
								
				Constants.getLog().add(TAG, Type.Debug, networkInfo.getState().toString() + " : " + networkInfo.getTypeName());
		    }					
		}
    	catch (Exception e)
		{
    		Constants.getLog().add(TAG, Type.Error, "processOnReceive", e);
		}
	}
	
	
	public int getPreviousConfiguration(String ssid)
	{
		int ret_netid = -1;
		
		try
		{
			// List available networks
			List<WifiConfiguration> configs = wifimgr.getConfiguredNetworks();
			
			for (WifiConfiguration config : configs) 
			{
				if (config.SSID.equalsIgnoreCase("\"" + ssid + "\""))
				{											
					ret_netid = config.networkId;
					Constants.getLog().add(TAG, Type.Debug, "getPreviousConfiguration found SSID: " + ssid + " w/NetId = " + ret_netid);
					break;
				}
			}				
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "getPreviousConfiguration", e);
    	}
		
		return ret_netid;
	}
	
	
	public void connectAP(ScanResult scanResult, String password, int wepKeyIndex)
	{   
		try
		{
			// check if user complies with policy for VZB
			if (Constants.getApplicationBuildType() == BuildType.VERIZON_BUSINESS)
			{
				// enforce policy prior to connecting
	        	EnumSet<PolicyEnforcementResults> policyCheckResults = Constants.getDeviceManagement().enforcePolicy(PolicyFailureNotificationType.NotifyAndPrompt);
	        	
	        	// ensure that device complies with set policy
	            if (!policyCheckResults.contains(PolicyEnforcementResults.Compliant))
	            {
	    	        // break from function if not active
	            	Constants.getLog().add(TAG, Type.Debug, "Device does not comply with current policy. Exiting connectAP().");
	    	        return;
	            }
			}
			
			int netId = 0;

			Constants.getLog().add(TAG, Type.Info , "Connecting to network " + scanResult.SSID);
			
			// create "connecting" notification
			Notifications.setConnectingToHotspot(scanResult.SSID);
			
			// disconnect first		
			disconnectAP(false);		
			
			
			// attempt to connect to OPEN network
			if (password == null || password.equals(""))
			{
				netId = saveOpenConfig(scanResult.SSID, scanResult.BSSID);
			}
			
			// attempt to connect to WPA
			else if (scanResult.capabilities.toUpperCase().contains("WPA"))
			{
				netId = saveWPAConfig(scanResult.SSID, scanResult.BSSID, password);
			}
			
			// attempt to connect to WEP
			else if (scanResult.capabilities.toUpperCase().contains("WEP"))
			{
				netId = saveWepConfig(scanResult.SSID, scanResult.BSSID, password, wepKeyIndex);
			}	
		    
		            
		    boolean b = wifimgr.enableNetwork(netId, true); 		 
			Constants.getLog().add(TAG, Type.Info, "Network enabled: " + String.valueOf(b));
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "connectAP", e);
    	}
	}	
	
	
	public void disconnectAP(boolean removeConfiguration)
	{
		try
		{
		    int netid =  this.wifimgr.getConnectionInfo().getNetworkId();
		    String ssid = this.wifimgr.getConnectionInfo().getSSID();
		    
		    if (ssid == null)
		    	ssid = "";
		    
		    Constants.getLog().add(TAG, Type.Info, "Disconnecting from network " + ssid);
	
		    // logoff the WISPr session before disconnecting
		    if (Constants.isWISPrLoggedIn && Constants.getSession() != null)
		    { 
		    	this.wisprAttempt.logoutWISPr(Constants.getSession().getWISPrLogoffUrl());
		    	Constants.isWISPrLoggedIn = false;
		    }		   
		    
		    if (netid > -1 && removeConfiguration)
		    {
		    	boolean b = this.wifimgr.removeNetwork(netid);
		    	Constants.getLog().add(TAG, Type.Info, "Remove Network result: " + String.valueOf(b));
		    }
		    else
		    {
		    	this.wifimgr.disableNetwork(netid);
		    }
		    
		    // configuration must be saved to take effect
		    boolean b = this.wifimgr.saveConfiguration();
		    Constants.getLog().add(TAG, Type.Info, "Save configuration result: " + String.valueOf(b));
		}
		catch (Exception e)
    	{
			Constants.isWISPrLoggedIn = false;
			Constants.getLog().add(TAG, Type.Error, "disconnectAP", e);
    	}
	}
	

	private int saveOpenConfig(String ssid, String bssid)
	{
		int netId = -1;
		
		try
		{
			WifiConfiguration wc = new WifiConfiguration();
			
			wc.priority = 40;
			wc.SSID = "\"" + ssid + "\"";
			wc.BSSID = bssid;
			
			wc.allowedKeyManagement.set(KeyMgmt.NONE);		
			wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
			wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			wc.status = WifiConfiguration.Status.DISABLED;
			netId = getPreviousConfiguration(ssid);
			
		    if (netId >= 0)
		    {
		    	wc.networkId = netId;
		    	netId = wifimgr.updateNetwork(wc);
		    	
		    	Constants.getLog().add(TAG, Type.Debug, "saveOpenConfig(): " + "updateNetwork returned " + netId);
		    }
		    else
		    {
		    	netId = wifimgr.addNetwork(wc);
		    	Constants.getLog().add(TAG, Type.Debug, "add " + ssid + "Open Network returned " + netId);
 	
		    	boolean es = wifimgr.saveConfiguration();
		    	Constants.getLog().add(TAG, Type.Debug , "saveOpenConfig(): " + es);
		    }		   		   		    
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "saveOpenConfig", e);
    	}
		
		return netId;		
	}

	
	public String encodeHexString(String sourceText) 
	{
		String ReturnValue = "";
		
		try
		{
			byte[] rawData = sourceText.getBytes();
			StringBuffer hexText= new StringBuffer();
			String initialHex = null;
			int initHexLength=0;
	
			for (int i=0; i<rawData.length; i++) 
			{
				int positiveValue = rawData[i] & 0x000000FF;
				initialHex = Integer.toHexString(positiveValue);
				initHexLength=initialHex.length();
				
				while(initHexLength++ < 2) 
				{
					hexText.append("0");
				}
				
				hexText.append(initialHex);
			}
			
			ReturnValue =  hexText.toString();
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "encodeHexString", e);
    	}
		
		return ReturnValue;
	}

	
	private int saveWepConfig(String ssid, String bssid, String password, int wepKeyIndex) 
	{     		
		int netId  = -1;
		
		try
		{
			WifiConfiguration wc = new WifiConfiguration();
			wc.priority = 40;
			wc.SSID = "\"" + ssid + "\""; // needs to be in quotes
			wc.BSSID = bssid;
			wc.status = WifiConfiguration.Status.DISABLED;

			wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);   
			wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN); 
			wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
			wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED); 
			wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP); 
			wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP); 
			wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);    
			wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);   
			wc.wepKeys[wepKeyIndex] = encodeHexString(password);
			wc.wepTxKeyIndex = wepKeyIndex;   
	 
			boolean result = false;
			netId = getPreviousConfiguration(ssid);
			if (netId >= 0)
		    {
		    	wc.networkId = netId;
		    	int retnetID = wifimgr.updateNetwork(wc);
		    	result = (netId == retnetID);
		    	
		    	Constants.getLog().add(TAG, Type.Debug, "saveWepConfig(): " + "updateNetwork returned " + retnetID);
		    }
		    else
		    {
				netId = wifimgr.addNetwork(wc);  
				Constants.getLog().add(TAG, Type.Debug, "add " + ssid + " WEP Network returned " + String.valueOf(netId));
		    	result = wifimgr.saveConfiguration();
		    }
			
			Constants.getLog().add(TAG, Type.Debug, "saveConfiguration returned " + String.valueOf(result));
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "saveWepConfig", e);
    	}

		return netId;
	}
	
	
	private int saveWPAConfig(String sSSID, String sBSSID, String sPassword) 
	{
		int netId = -1;
		
		try
		{
		     WifiConfiguration wc = new WifiConfiguration();
		     wc.SSID = "\"" + sSSID + "\""; // needs to be in quotes
		     wc.BSSID = sBSSID;
		     wc.hiddenSSID = true;     
		     wc.status = WifiConfiguration.Status.DISABLED;          
		     wc.priority = 40;     
		     wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);     
		     wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);      
		     wc.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
		     wc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);          
		     wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);     
		     wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);     
		     wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);     
		     wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
		     wc.preSharedKey = "\"" + sPassword + "\"" ;  
		     
			boolean result = false;
			netId = getPreviousConfiguration(sSSID);
			
			if (netId >= 0)
		    {
		    	wc.networkId = netId;
		    	int retnetID = wifimgr.updateNetwork(wc);
		    	result = (netId == retnetID);
		    	
		    	Constants.getLog().add(TAG, Type.Debug, "saveWPAConfig(): " + "updateNetwork returned " + retnetID);
		    }
		    else
		    {
				netId = wifimgr.addNetwork(wc);  
			    Constants.getLog().add(TAG, Type.Debug, "add " + sSSID + "WPA Network returned " + String.valueOf(netId));
		    	result = wifimgr.saveConfiguration();
		    }
				
		     Constants.getLog().add(TAG, Type.Debug, "saveConfiguration returned " + String.valueOf(result)); 
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "saveWPAConfig", e);
    	}
 
	    return netId;
	}
}