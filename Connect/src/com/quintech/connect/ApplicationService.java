package com.quintech.connect;


import com.quintech.common.AbstractConstants.BuildType;
import com.quintech.common.AbstractData.Settings;
import com.quintech.common.Configuration;
import com.quintech.common.SmartConnectAllowedConnectionTypes.ConnectionType;
import com.quintech.common.VZBDirectoryServices;
import com.quintech.common.WISPrConnect;
import com.quintech.common.ILog.Type;
import com.quintech.connect.activities.TabHostActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;



public class ApplicationService extends Service 
{
	private static String TAG = "ApplicationService";
	private static WiFiReceiver wifiReceiver = null;
	private static WifiManager wifi = null;
	private static Handler handlerRefreshUI;
	private static boolean bUpdateAlarmCreated = false;
	private static boolean hasInitialized = false;
	
	public static class Action
	{
		public static String NO_ACTION = "com.quintech.connect.NO_ACTION";
		public static String INITIALIZE_SERVICE = "com.quintech.connect.INITIALIZE_SERVICE";
		public static String CONNECT = "com.quintech.connect.CONNECT";
		public static String SMART_CONNECT = "com.quintech.connect.SMART_CONNECT";
		public static String CANCEL_CONNECTION = "com.quintech.connect.CANCEL_CONNECTION";
		public static String CANCEL_WISPR_AUTHENTICATION = "com.quintech.connect.CANCEL_WISPR_AUTHENTICATION";
		public static String DISCONNECT = "com.quintech.connect.DISCONNECT";
		public static String START_WISPR_TASK = "com.quintech.connect.START_WISPR_TASK";		
		public static String REFRESH_UI = "com.quintech.connect.REFRESH_UI";
		public static String UPDATE_ALARM = "com.quintech.connect.UPDATE_ALARM";
		public static String REQUEST_DEVICE_ADMINISTRATOR_ACCESSS = "com.quintech.connect.REQUEST_DEVICE_ADMINISTRATOR_ACCESSS";	
		public static String NOTIFY_APPLICATIONS_REQUIRED = "com.quintech.connect.NOTIFY_APPLICATIONS_REQUIRED";
		public static String VIEW_AVAILABLE_APPS = "com.quintech.connect.VIEW_AVAILABLE_APPS";
		public static String REQUEST_PASSWORD_RESET = "com.quintech.connect.REQUEST_PASSWORD_RESET";	
		public static String REQUEST_REGISTRATION = "com.quintech.connect.REQUEST_REGISTRATION";
	}
	  
	
	@Override
	public IBinder onBind(Intent intent) 
	{
		return null;
	}
	
	
    @Override    
    public void onCreate() 
    {        
    	try
    	{
    		// initialize
    		initializeService(null);     
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "onCreate", e);
    	}
    }
    
    
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
    	try
    	{
    		handleCommand(intent);
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "onStartCommand", e);
    	}
    	
        
    	return START_STICKY;
	}
	
	
	public static WiFiReceiver getWiFiReceiver()
	{
		return wifiReceiver;
	}
	
	
	private void handleCommand(Intent intent)
	{
		try
    	{
			// check for connect action
    		if (intent != null && intent.getAction() != null)
    		{
    			Constants.getLog().add(TAG, Type.Debug, "Service received intent " + intent.getAction());
    			
    			if (intent.getAction().equalsIgnoreCase(Action.NO_ACTION))
            	{
        			// do nothing
            	}
    			else if (intent.getAction().equalsIgnoreCase(Action.REFRESH_UI))
            	{
        			// refresh UI
    				refreshUI();
            	}
        		else if (intent.getAction().equalsIgnoreCase(Action.INITIALIZE_SERVICE))
            	{
        			initializeService(intent);
            	}
        		else if (intent.getAction().equalsIgnoreCase(Action.REQUEST_REGISTRATION))
        		{
        			RegisterUser(intent);
        		}
        		else if (intent.getAction().equalsIgnoreCase(Action.CONNECT))
            	{
        			connectAP(intent);
            	}
        		else if (intent.getAction().equalsIgnoreCase(Action.SMART_CONNECT))
            	{
        			smartConnect();
            	}
        		else if (intent.getAction().equalsIgnoreCase(Action.CANCEL_CONNECTION))
        		{
        			// TODO
        			// cancel connecting to hotspot
        		}
        		else if (intent.getAction().equalsIgnoreCase(Action.CANCEL_WISPR_AUTHENTICATION))
        		{
        			// TODO
        			// cancel WISPr authentication
        		}
        		else if (intent.getAction().equalsIgnoreCase(Action.DISCONNECT))
        		{
        			// logout from WISPr session and disconnect
			        Thread t = new Thread() 			        
					{
						@Override
						public void run() 
						{
							Notifications.removeAllConnectionNotifications(null);
							
							// disconnect
							wifiReceiver.disconnectAP(true);
						}
					};
					t.start();
        		}
        		else if (intent.getAction().equalsIgnoreCase(Action.START_WISPR_TASK))
        		{
        			// start WISPr authentication
        			startWISPrAuthentication();
        		}
        		else if (intent.getAction().equalsIgnoreCase(Action.REQUEST_DEVICE_ADMINISTRATOR_ACCESSS))
        		{
        			// send intent to main activity to request device administration rights
					Constants.getLibrary().requestDeviceAdministration();
        		}
        		else if (intent.getAction().equalsIgnoreCase(Action.NOTIFY_APPLICATIONS_REQUIRED))
        		{
        			// notify
        			Constants.getLibrary().notifyApplicationsRequired();
        		}
        		else if (intent.getAction().equalsIgnoreCase(Action.VIEW_AVAILABLE_APPS))
        		{
        			// notify
        			Constants.getLibrary().viewAvailableApplications();
        		}
        		else if (intent.getAction().equalsIgnoreCase(Action.REQUEST_PASSWORD_RESET))
        		{
        			// request
        			Constants.getDeviceManagement().requestPasswordReset();
        		}
    		}
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "handleCommand", e);
    	}
	}
	
	
	private void RegisterUser(Intent intent) {
		try
		{
			Bundle b = intent.getExtras();
			
			if (b != null)
			{
				String username = (String) b.get("Username");
				String password = (String) b.get("Password");
				Constants.getData().setSetting(Settings.SET_RovaPortalUserID, username, false);
				Constants.getData().setSetting(Settings.SET_RovaPortalPassword, password, false);
	
		        Thread t = new Thread() 			        
				{
					@Override
					public void run() 
					{

						Configuration.update(true);
						boolean bRegResult = VZBDirectoryServices.isUserAuthenticated("", "");

						Intent return_intent = new Intent("com.quintech.connect.REG_COMPLETE");
						// Add data
						return_intent.putExtra("Result", bRegResult);
						LocalBroadcastManager.getInstance(Constants.applicationContext).sendBroadcast(return_intent);
					}
				};
		        
		        t.start();
			}
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "RegisterUser", e);
    	}
		
	}


	@Override
	public void onDestroy() 
	{
		try
		{
	    	// close database
	    	Constants.getData().closeDatabase();
	    	
	    	Constants.getLog().add(TAG, Type.Debug, "Closed database");
			
			super.onDestroy();
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "onDestroy");
    	}
	}
	
	
	private void initializeService(Intent callingIntent)
	{
		try
		{
    		// check if service needs to be initialized
			if (hasInitialized)
			{
				Constants.getLog().add(TAG, Type.Debug, "Skipping service initialization, already initialized");
				return;
			}
			else
				Constants.getLog().add(TAG, Type.Debug, "Initializing service");
			
			
			// re-initialize Application Constants
    		Constants.initializeApplicationConstants(this.getApplicationContext());
			
	        
	        
	        // Verizon Business builds
	        if (Constants.getApplicationBuildType() == BuildType.VERIZON_BUSINESS)
	        {
	        	Constants.getLog().add(TAG, Type.Debug, "Initializing Verizon Business application service");
	        	
	        	// update location in a new thread
		        Thread t1 = new Thread() 
		        {
		            public void run() 
		            {
		               LocationUtility.updateLocation();
		            }
		        };
		          
		        t1.start();
		        	
            	// register WiFi receiver
	        	registerWiFiReceiver();
	        	
	        	// create alarm to update configuration on an interval
		        if (!bUpdateAlarmCreated)
		        	createUpdateConfigurationAlarm();
		        
		        
		        // update configuration data in a new thread
//		        Thread t2 = new Thread() 
//		        {
//		            public void run() 
//		            {
//		            	// update configuration
//		               Configuration.update(false);
//		               
//		               // check if Divide is installed but requires activation
//		               DivideHelper.checkDivideStatus();
//		            }
//		        };
		          
//		        t2.start();
	        }
	        
	        // set flag
	        hasInitialized = true;
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "initializeService", e);
    	}
	}
	
	
	private void registerWiFiReceiver()
	{
		try
		{
			// register WiFi receiver
	        if (wifi == null)
	        {
	        	Constants.getLog().add(TAG, Type.Debug, "Initializing Wi-Fi manager");
	        	
	        	wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	        }
	        
	        if (wifiReceiver == null)
	        {
	        	Constants.getLog().add(TAG, Type.Debug, "Initializing Wi-Fi receiver");
	        	
				wifiReceiver = new WiFiReceiver(wifi);
			
		        // register the broadcast receiver for wifi state events
		        IntentFilter filter = new IntentFilter();
		        filter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
		        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);        
		        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		        filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
		        
		        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		        filter.addAction(WifiManager.EXTRA_SUPPLICANT_ERROR);
		        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		        filter.addAction(WifiManager.ACTION_PICK_WIFI_NETWORK);
		        
				registerReceiver(wifiReceiver, filter);
	        }

	        // perform scan
	        Constants.getLog().add(TAG, Type.Debug, "Performing initial Wi-Fi scan");
			wifi.startScan();
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "registerWiFiReceiver", e);
		}
	}
	
	
	private void connectAP(Intent intent)
    {
		try
		{
			Bundle b = intent.getExtras();
			
			if (b != null)
			{
				ScanResult scanResult = (ScanResult) b.getParcelable("ScanResult");
				String password = (String) b.get("Password");
				int wepKeyIndex = b.getInt("WepKeyIndex");
				
				wifiReceiver.connectAP(scanResult, password, wepKeyIndex);
			}
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "connectAP", e);
    	}
    }
	
	
	private void smartConnect()
    {
		try
		{
			// start Smart Connect to make connection
			Constants.getSmartConnect().start(true);
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "smartConnect");
    	}
    }
	

	private void refreshUI()
	{
		try
		{
			// use a timer to send intents to refresh activity UIs
			// this will prevent multiple messages from being sent too quickly
			
			// Initialize handler if null
			if (handlerRefreshUI == null)
			{
				handlerRefreshUI = new Handler(); 
			}
			
			Constants.getLog().add(TAG, Type.Verbose, "Reset RefreshUI timer.");
			
			// remove existing callbacks
			handlerRefreshUI.removeCallbacks(runnableRefreshUI);
			
			// create new callback to send intents to refresh activity UIs
			handlerRefreshUI.postDelayed(runnableRefreshUI, 1200);
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "refreshUI", e);
		}
	}
	
	
	private void startWISPrAuthentication()
	{
		try
		{
			// run in new thread
	    	Thread t = new Thread() 
	    	{
	            @Override
	            public void run() 
	            {
			    	try
			    	{
			    		// check if session already exists
						if (Constants.getSession() != null && Constants.getSession().getSessionSysID() > 0)
						{	
							Constants.getLog().add(TAG, Type.Debug, "Skipped starting of new WISPr task, session " + String.valueOf(Constants.getSession().getSessionSysID() + " already exists"));
						}
						else
						{
							// start Wispr task - this will exit if not required
							Constants.getLog().add(TAG, Type.Debug, "Starting new WISPr task");
							wifiReceiver.startWISPrTask();
						}
			    	}
			    	catch (Exception e)
			    	{
			    		Constants.getLog().add(TAG, Type.Error, "startWISPrAuthentication", e);
			    	}
	            }
	    	};
			
	    	t.start();
		}
		catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "startWISPrAuthentication", e);
    	}
	}
	
	
	private void createUpdateConfigurationAlarm()
	{
		try
		{
			// notify service to attempt connection
			Intent intentAlarm = new Intent(Constants.applicationContext, OnAlarmReceiver.class);	
		
			intentAlarm.setAction(Action.UPDATE_ALARM);
			
			PendingIntent sender = PendingIntent.getBroadcast(Constants.applicationContext, 0, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
		
			// Get the AlarmManager service
			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
			am.setInexactRepeating(AlarmManager.RTC, System.currentTimeMillis() + 5000, AlarmManager.INTERVAL_HALF_HOUR, sender);
			bUpdateAlarmCreated = true;
		}
		catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "createUpdateConfigurationAlarm", e);
    	}
	}
	

	private Runnable runnableRefreshUI = new Runnable() 
	{
	    public void run() 
	    {
	    	// run in new thread
	    	Thread t = new Thread() 
	    	{
	            @Override
	            public void run() 
	            {
			    	try
			    	{
			    		Constants.getLog().add(TAG, Type.Verbose, "RefreshUI timer tick begin.");
				    	
				    	// send intent to tab activity to refresh it's UI
						
						// check if top activity is Tab Host
						if (Constants.getLibrary().isAppInForeground())
						{
							// check if we're authenticated/probe URL
							boolean isAuthenticated = false;
							
							try
							{
								// if connected to WiFi
								if (wifi != null &&
										wifi.getConnectionInfo().getBSSID() != null)
								{
									// if hotspot is not in a known directory or does not require a WISPr login
									if (Constants.getData().isSSIDInDirectory(wifi.getConnectionInfo().getSSID(), ConnectionType.Unknown) == null ||
											Constants.getData().isSSIDLoginRequired(wifi.getConnectionInfo().getSSID()) == null)
									{
										// default flag to true
										isAuthenticated = true;
									}
									
									// check probe to see if there is internet connectivity
									else
									{
										isAuthenticated = WISPrConnect.probeTest();
									}
								}
							}
							catch (Exception e)
							{
								Constants.getLog().add(TAG, Type.Error, "runnableRefreshUI", e);
							}
							
	
							// send refresh UI to Tab Activity if it is still on top
					    	if (Constants.getLibrary().isAppInForeground())
					    	{
								// send intent
						        Intent intent = new Intent(Constants.applicationContext, TabHostActivity.class);
						        intent.setAction(Library.ACTION_REFRESH_UI);
						        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						       
						        // add isAuthenticated flag
						        Bundle b = new Bundle();
						    	b.putString("isAuthenticated", String.valueOf(isAuthenticated)); 
						    	intent.putExtras(b);
					    	
					    		Constants.applicationContext.startActivity(intent);
					    		Constants.getLog().add(TAG, Type.Verbose, "Sent ACTION_REFRESH_UI to TabHostActivity.");
					    	}
						}
				    	
						Constants.getLog().add(TAG, Type.Verbose, "RefreshUI timer tick end.");
			    	}
			    	catch (Exception e)
			    	{
			    		Constants.getLog().add(TAG, Type.Error, "runnableRefreshUI", e);
			    	}
	            }
	    	};
	    	
	    	t.start();
	    }
    };
}
