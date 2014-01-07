package com.quintech.connect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import com.quintech.common.Configuration;
import com.quintech.common.HotspotInfo;
import com.quintech.common.WISPrConnect;
import com.quintech.common.AbstractData.Flags;
import com.quintech.common.ILog.Type;
import com.quintech.common.SmartConnectAllowedConnectionTypes;
import com.quintech.common.SmartConnectAllowedConnectionTypes.ConnectionType;



public class SmartConnect 
{
	private static String TAG = "SmartConnect";
	public static int RESTART_SMART_CONNECT_MINIMUM_INTERVAL_MINUTES = 60;
	private List<RankedScanResultItem> listAttemptedItems = new ArrayList<SmartConnect.RankedScanResultItem>();
	private boolean hasStarted = false;
	private boolean isAttemptInProgress = false;
	private boolean forceConnect = false;
	private WifiManager wifiManager = null;
	private ConnectivityManager connectivityManager = null;
	private NetworkInfo networkInfo = null;
	private RankedScanResultItem connectRankedScanResultItem = null;
	private long lastAttemptMillis = 0;
	
	
	public class RankedScanResultItem
	{
		public int connectionTypeRanking = -1;
		public ScanResult scanResult = null;
		public ConnectionType connectionType = ConnectionType.Unknown;
		public HotspotInfo hotspotInfo = null;
		
		public RankedScanResultItem(ScanResult scanResult)
		{
			this.scanResult = scanResult;
		}
	}
	
	public class RankedScanResultItemComparator implements Comparator<RankedScanResultItem>
    {    
    	@Override    
    	public int compare(RankedScanResultItem o1, RankedScanResultItem o2) 
    	{       
    		// sort in order of ConnectionTypeRanking, Directory Ranking, Signal Strength
    		int i = 0;
    		
    		// ConnectionTypeRanking
    		i = o1.connectionTypeRanking - o2.connectionTypeRanking;
    	    if (i != 0) return i;

    	    // Directory Ranking
    	    if (o1.hotspotInfo != null && o1.hotspotInfo.getDirectoryInfo() != null && o2.hotspotInfo != null && o2.hotspotInfo.getDirectoryInfo() != null)
    	    {
	    	    i = o1.hotspotInfo.getDirectoryInfo().smartConnectRanking - o2.hotspotInfo.getDirectoryInfo().smartConnectRanking;
	    	    if (i != 0) return i;
    	    }
	    	
    	    // Signal Strength
    		return WifiManager.compareSignalLevel(o2.scanResult.level, o1.scanResult.level);    
    	}
    }
	
	
	
	private WifiManager getWifiManager()
	{
		try
		{
			if (wifiManager == null)
				wifiManager = (WifiManager) Constants.applicationContext.getSystemService(Context.WIFI_SERVICE);
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "getWifiManager", e);
    	}
		
		return wifiManager;
	}
	
	
	private NetworkInfo getNetworkInfo()
	{
		try
		{
			// resuse ConnectivityManager when possible
			if (connectivityManager == null)
				connectivityManager = (ConnectivityManager) Constants.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
			
			// create new NetworkInfo every time to get the latest values
			networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "getNetworkInfo", e);
    	}
		
		return networkInfo;
	}
	
	
	public RankedScanResultItem getConnectRankedScanResultItem()
	{
		try
		{
			if (connectRankedScanResultItem == null)
				connectRankedScanResultItem = getNextConnectItem();
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "getNetworkInfo", e);
    	}
		
		return connectRankedScanResultItem;
	}
	
	
	public boolean getHasStarted()
	{
		return hasStarted;
	}
	
	
	public boolean getIsAttemptInProgress()
	{
		return isAttemptInProgress;
	}
	
	
	public int getAttemptedItemsCount()
	{
		int count = 0;
		
		try
		{
			count = listAttemptedItems.size();
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "getAttemptedItemsCount", e);
    	}
		
		return count;
	}
	
	
	public void start(boolean forceConnect)
	{
		try
		{
			Constants.getLog().add(TAG, Type.Debug, "Starting Smart Connect, forceConnect: " + String.valueOf(forceConnect));
			
			// clear attempt list if not already in progress
			if (!hasStarted || forceConnect)
			{
				Constants.getLog().add(TAG, Type.Debug, "Smart Connect, has not been started. Cleared attempted item list");
				listAttemptedItems.clear();
			}
			
			
			// set flags
			this.hasStarted = true;
			this.forceConnect = forceConnect;
			
			// set timestamp
			lastAttemptMillis = System.currentTimeMillis();
			
			// start smart connect
			attemptNextConnectionAsnyc();
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "start", e);
    	}
	}
	

	public void attemptNextConnectionAsnyc()
	{
		try
		{
			// run in new thread
			Thread t = new Thread() 
	        {
	            public void run() 
	            {
	            	attemptNextConnection();
	            }
	        };
	          
	        t.start();
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "attemptNextConnectionAsnyc", e);
    	}
	}
	

	private void attemptNextConnection()
	{
		try
		{
			if (isAttemptInProgress)
			{
				Constants.getLog().add(TAG, Type.Debug, "Skipping Smart Connect attmept, already in progress");
				return;
			}
			
			
			// check Wi-Fi status
			if (getWifiManager().getWifiState() !=  WifiManager.WIFI_STATE_ENABLED)
			{
				Constants.getLog().add(TAG, Type.Debug, "Stopping Smart Connect, Wi-Fi is not enabled");
					
				reset();
	        	return;
			}
			
			

			// wait, allow Wi-Fi to process if there has been any previous attempts
			if (!listAttemptedItems.isEmpty())
				Thread.sleep(5000);
			

	        // check if Wi-Fi is already connected or connecting
			if (getNetworkInfo() != null)
			{
				if (getNetworkInfo().isConnected() && !Constants.isWISPrInProgress)
				{
					// check for internet connection, this will start Wispr login if required
					boolean isConnected = checkInternetConnectivity();
					
					if (isConnected)
						Constants.getLog().add(TAG, Type.Debug, "Stopping Smart Connect, Wi-Fi is already connected");
					
		        	return;
				}
				
				else if (getNetworkInfo().isConnected() && Constants.isWISPrInProgress)
				{
					Constants.getLog().add(TAG, Type.Debug, "Skipping Smart Connect attmept, Wi-Fi is authenticating");
		        	return;
				}
				
				else if (getNetworkInfo().isConnectedOrConnecting())
				{
					Constants.getLog().add(TAG, Type.Debug, "Skipping Smart Connect attmept, Wi-Fi is connecting");
		        	return;
				}
			}
			else
				Constants.getLog().add(TAG, Type.Debug, "No current network info found, continuing Smart Connect");
			
			
			// set flag
			isAttemptInProgress = true;

			
			// check for available connection item
			connectRankedScanResultItem = getNextConnectItem();
			
			if (connectRankedScanResultItem == null)
			{
				Constants.getLog().add(TAG, Type.Debug, "No available hotspot for next Smart Connect attempt");
	        	return;
			}
			
			
        	// if forceConnect flag or Auto-Connect setting is enabled, attempt to make connection
        	if (forceConnect || Constants.getData().getFlag(Flags.FLG_AutoConnect))
        	{
        		Constants.getLog().add(TAG, Type.Info, "Smart Connect attempting connection to " + connectRankedScanResultItem.scanResult.SSID);
        		
        		// add item to attempt list
        		listAttemptedItems.add(connectRankedScanResultItem);
        		
        		// set timestamp
    			lastAttemptMillis = System.currentTimeMillis();
        		
        		
        		// notify service to attempt connection
        		Intent intentConnect =	new Intent(Constants.applicationContext, ApplicationService.class);
        		intentConnect.setAction(ApplicationService.Action.CONNECT);	
        		
        		
        		// set password and WepKeyIndex values from database if available
        		String password = "";
        		int wepKeyIndex = 0;
        		
        		if (connectRankedScanResultItem.hotspotInfo != null)
        		{
        			password = connectRankedScanResultItem.hotspotInfo.password;
        			wepKeyIndex = connectRankedScanResultItem.hotspotInfo.wepKeyIndex;
        		}
        		
        		Bundle b = new Bundle();
        		b.putParcelable("ScanResult", connectRankedScanResultItem.scanResult);
        		b.putString("Password", password);
        		b.putInt("WepKeyIndex", wepKeyIndex);
        		
    	    	intentConnect.putExtras(b);
    	    	
    	    	Constants.applicationContext.startService(intentConnect);	    				  
        	}
        	
        	else
        	{
        		// show notification to user that a hotspot is available
	            Notifications.setAvailableHotspot(connectRankedScanResultItem.scanResult.SSID);
	            
	            Constants.getLog().add(TAG, Type.Debug, "Auto-connect is not active and Smart Connect not in progress. Notified available connection for " + connectRankedScanResultItem.scanResult.SSID);
	            
	            // stop smart connect
	            reset();
        	}
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "attemptNextConnection", e);
    	}
		finally
		{
			// reset flag
			isAttemptInProgress = false;
		}
	}
	
	
	public boolean checkInternetConnectivity()
	{
		boolean isConnected = false;
		
		try
		{
			String ssid = "";
			
			try
			{
				ssid = getWifiManager().getConnectionInfo().getSSID(); 
			}
			catch (Exception e)
			{
	        	Constants.getLog().add(TAG, Type.Info, "Unable to determine connected SSID to attempt WISPr authentication");
			}
			

			// check connectivity
			isConnected = WISPrConnect.probeTest();
			
			if (isConnected)
			{
				Constants.getLog().add(TAG, Type.Info, "Successful internet ping test");
				
				// reset Smart Connect
				Constants.getSmartConnect().reset();
				
				// update configuration data in a new thread
		        Thread t = new Thread() 
		        {
		            public void run() 
		            {
		               Configuration.update(false);
		            }
		        };
		          
		        t.start();
			}
			
			
			// if currently connected SSID is in the local directory
			else if (!ssid.equals("") && Constants.getData().isSSIDLoginRequired(ssid) != null)
			{
				Constants.getLog().add(TAG, Type.Info, "Failed internet ping test");
				Constants.getLog().add(TAG, Type.Info, "Detected connection to hostpot within the network that requires authentication.");
				
				// notify service to attempt WSIPr login
        		Intent intentWISPr = new Intent(Constants.applicationContext, ApplicationService.class);
        		intentWISPr.setAction(ApplicationService.Action.START_WISPR_TASK);	
        		Constants.applicationContext.startService(intentWISPr);	  
			}
			
			// in this case we do not have an internet connection, or any credentials for authentication
			else
			{
				Constants.getLog().add(TAG, Type.Info, "Failed internet ping test");
				
				// disconnect if smart connect is active to continue to the next available item
				if (!listAttemptedItems.isEmpty())
				{
					// notify service to disconnect
	        		Intent intentWISPr = new Intent(Constants.applicationContext, ApplicationService.class);
	        		intentWISPr.setAction(ApplicationService.Action.DISCONNECT);	
	        		Constants.applicationContext.startService(intentWISPr);	  
				}
				
				// TODO
				// open a web page to check if user needs to login to hotspot outside of our directories?
				// for now we'll leave the network connected without internet connectivity and let the user figure it out
				Constants.getLog().add(TAG, Type.Warn, "User must browse to the web to check for manual authentication");
			}
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "checkInternetConnectivity", e);
    	}
		
		return isConnected;
	}
		
	
	public void reset()
	{
		try
		{
			// reset flags
			hasStarted = false;
			forceConnect = false;

			// clear attempts
			listAttemptedItems.clear();
			
			// clear
			lastAttemptMillis = 0;
			
			// clear item
			connectRankedScanResultItem = null;
			
			Constants.getLog().add(TAG, Type.Debug, "Reset Smart Connect");
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "reset", e);
    	}
	}
	
	
	private RankedScanResultItem getNextConnectItem()
	{
		RankedScanResultItem item = null;
		
		try
		{
			// check last attempt time and clear attempt list if required
			long minutesSinceLastAttempt = ((System.currentTimeMillis() - lastAttemptMillis) / 1000) / 60;
			Constants.getLog().add(TAG, Type.Verbose, "Minutes since last Smart Connect attempt: " + String.valueOf(minutesSinceLastAttempt));
			
			if (lastAttemptMillis > 0 && minutesSinceLastAttempt > RESTART_SMART_CONNECT_MINIMUM_INTERVAL_MINUTES)
			{
				Constants.getLog().add(TAG, Type.Debug, "Reset Smart Connect attempted item list after " + String.valueOf(RESTART_SMART_CONNECT_MINIMUM_INTERVAL_MINUTES) + " minutes");
				listAttemptedItems.clear();
			}
			
			
			// get sorted list of RankedScanResultItems
			List<RankedScanResultItem> listRankedScanResultItems = getRankedScanResultItems();
			
			// get next item from the list that hasn't been attempted
			
			Constants.getLog().add(TAG, Type.Verbose, "Retrieved ranked scan item list");
			
			// log list
			for (RankedScanResultItem rankedItem : listRankedScanResultItems)
				Constants.getLog().add(TAG, Type.Verbose, String.valueOf(rankedItem.connectionType) + " - (" + String.valueOf(rankedItem.scanResult.level) + ") - " + rankedItem.scanResult.SSID);
			
			
			// select item
			for (RankedScanResultItem rankedItem : listRankedScanResultItems)
			{
				// skip if item is in the attempted list
				boolean hasAttempted = false;
				
				for (RankedScanResultItem attemptedItem : listAttemptedItems)
				{
					if (rankedItem.scanResult.SSID.equalsIgnoreCase(attemptedItem.scanResult.SSID))
					{
						hasAttempted = true;
						break;
					}
				}
				
				if (hasAttempted)
				{
					Constants.getLog().add(TAG, Type.Verbose, "Smart Connect skipped attempted item: " + String.valueOf(rankedItem.connectionType) + " - (" + String.valueOf(rankedItem.scanResult.level) + ") - " + rankedItem.scanResult.SSID);
					continue;
				}
				
				
				// skip if SSID requires a password and there is not one in the directory
				if (isScanResultPrivate(rankedItem.scanResult) && 
						(rankedItem.hotspotInfo == null || rankedItem.hotspotInfo.password == null || rankedItem.hotspotInfo.password.equals(""))
				)
				{
					Constants.getLog().add(TAG, Type.Verbose, "Smart Connect skipped item due to lack of password: " + String.valueOf(rankedItem.connectionType) + " - (" + String.valueOf(rankedItem.scanResult.level) + ") - " + rankedItem.scanResult.SSID);
					continue;
				}
					
				
				// set item
				Constants.getLog().add(TAG, Type.Verbose, "Smart Connect selected for next attempt: " + String.valueOf(rankedItem.connectionType) + " - (" + String.valueOf(rankedItem.scanResult.level) + ") - " + rankedItem.scanResult.SSID);
				
				item = rankedItem;
				break;
			}	
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "getNextConnectItem", e);
    	}
		
		return item;
	}
	
	
	private boolean isScanResultPrivate(ScanResult scanResult)
	{
		// default to open
		boolean result = false;
		
		try
		{
			if (scanResult != null && scanResult.capabilities != null)
			{
				ArrayList<String> privateCapabilities = new ArrayList<String>();
				
				privateCapabilities.add("wpa");
				privateCapabilities.add("wpa2");
				privateCapabilities.add("psk");
				privateCapabilities.add("peap");
				privateCapabilities.add("tls");
				privateCapabilities.add("ttls");
				privateCapabilities.add("eap");
				privateCapabilities.add("wep");
				privateCapabilities.add("ccmp");
				
				
				for (String s : privateCapabilities)
				{
					if (scanResult.capabilities.toLowerCase().contains(s))
					{
						result = true;
						break;
					}
				}
			}
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "isScanResultPrivate", e);
    	}
		
		return result;
	}
	
	
	private List<RankedScanResultItem> getRankedScanResultItems()
	{
		List<RankedScanResultItem> listRankedScanResultItems = new ArrayList<RankedScanResultItem>();
		
		
		try
		{
			if (Constants.wifiScanList == null)
			{
				Constants.getLog().add(TAG, Type.Warn, "Wi-Fi scan list is not available, no ranked scan items available.");
				return listRankedScanResultItems;
			}
			
			
			// set allowed types from the database
			SmartConnectAllowedConnectionTypes allowedWiFiVerizon = Constants.getData().getSmartConnectAllowedConnectionType(ConnectionType.WiFi_Verizon);
			SmartConnectAllowedConnectionTypes allowedWiFiCorporate = Constants.getData().getSmartConnectAllowedConnectionType(ConnectionType.WiFi_Corporate);
			SmartConnectAllowedConnectionTypes allowedWiFiOpen = Constants.getData().getSmartConnectAllowedConnectionType(ConnectionType.WiFi_Open);
			SmartConnectAllowedConnectionTypes allowedWiFiPrivate = Constants.getData().getSmartConnectAllowedConnectionType(ConnectionType.WiFi_Private);
			
			// disallow auto-connecting to unknown open networks if flag is not set
			if (!Constants.getData().getFlag(Flags.FLG_SmartConnectIncludeOpenNetworks))
				allowedWiFiOpen = null;
			
			// for each item in the current scan list (already ranked by signal strength, high to low)
			for (ScanResult sr : Constants.wifiScanList)
			{
				// ignore SSIDs with low signal level
				if (WifiManager.calculateSignalLevel(sr.level, 5) < 2)
					continue;
				
				// determine type
				RankedScanResultItem rankedScanResultItem = new RankedScanResultItem(sr);
				
				// Verizon
				if (allowedWiFiVerizon != null)
				{
					rankedScanResultItem.hotspotInfo = Constants.getData().isSSIDInDirectory(sr.SSID, ConnectionType.WiFi_Verizon);
					
					if (rankedScanResultItem.hotspotInfo != null)
					{
						// set type and rank
						rankedScanResultItem.connectionType = ConnectionType.WiFi_Verizon;
						rankedScanResultItem.connectionTypeRanking = allowedWiFiVerizon.rank;
						
						// add to list and continue to process next item
						listRankedScanResultItems.add(rankedScanResultItem);
						continue;
					}
				}
				
				// Corporate
				if (allowedWiFiCorporate != null)
				{
					rankedScanResultItem.hotspotInfo = Constants.getData().isSSIDInDirectory(sr.SSID, ConnectionType.WiFi_Corporate);
					
					if (rankedScanResultItem.hotspotInfo != null)
					{
						// set type and rank
						rankedScanResultItem.connectionType = ConnectionType.WiFi_Corporate;
						rankedScanResultItem.connectionTypeRanking = allowedWiFiCorporate.rank;
						
						// add to list and continue to process next item
						listRankedScanResultItems.add(rankedScanResultItem);
						continue;
					}
				}
				
				// Private
				if (allowedWiFiPrivate != null && isScanResultPrivate(sr))
				{
					// set type and rank
					rankedScanResultItem.connectionType = ConnectionType.WiFi_Private;
					rankedScanResultItem.connectionTypeRanking = allowedWiFiPrivate.rank;
					
					// add to list and continue to process next item
					listRankedScanResultItems.add(rankedScanResultItem);
					continue;
				}
				
				// Open
				if (allowedWiFiOpen != null && !isScanResultPrivate(sr))
				{
					// set type and rank
					rankedScanResultItem.connectionType = ConnectionType.WiFi_Open;
					rankedScanResultItem.connectionTypeRanking = allowedWiFiOpen.rank;
					
					// add to list and continue to process next item
					listRankedScanResultItems.add(rankedScanResultItem);
					continue;
				}
			}
			
			
			// sort list based on Smart Connect connection type rankings and scan item signal strength
			Collections.sort(listRankedScanResultItems, new RankedScanResultItemComparator());	        
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "setConnectionTypes", e);
    	}
		
		return listRankedScanResultItems;
	}
}
