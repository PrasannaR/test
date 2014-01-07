package com.quintech.connect;


import com.quintech.common.AbstractData.Flags;
import com.quintech.connect.activities.CredentialPromptActivity;
import com.quintech.connect.activities.Connect_MainActivity;
import com.quintech.connect.activities.TabHostActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;


@SuppressWarnings("deprecation")
public class Notifications 
{
	private static String TAG = "Notifications";
	
	public enum Type
	{
		AVAILABLE_HOTSPOT(0),
		CONNECTING(1),
		AUTHENTICATING_WISPR(2),
		AUTHENTICATED_WISPR(3),
		FAILED_WISPR_AUTHENTICATION(4),
		UNTRUSTED_NETWORK(5),
		AUTHENTICATING_ROVA_PORTAL_CREDENTIALS(6),
		FAILED_ROVA_PORTAL_AUTHENTICATION(7),
		REQUEST_DEVICE_ADMINISTRATION(8),
		NOTIFY_APPLICATIONS_REQUIRED(9),
		REQUEST_PASSWORD_RESET(10),
		FAILED_TO_OPEN_DATABASE(11),
		ENROLLMENT_POLICY_FAILURE(12),
		LOCATION_ACCESS_REQUIRED(13),
		NOTIFY_NEW_APPLICATIONS_AVAILABLE(14),
		NOTIFY_DEBUG_BUILD(15);
		
		private int value;    
		private Type(int value) 
		{
			this.value = value;
		}

		public int getValue() 
		{
			return value;
		}
	}
	
	
	public static void remove(Type type)
	{
		try
		{
			// clear specified notification
		    String ns = Context.NOTIFICATION_SERVICE; 
			NotificationManager mNotificationManager = (NotificationManager) Constants.applicationContext.getSystemService(ns);
			mNotificationManager.cancel(type.value);
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Log.Type.Error, "remove", e);
    	}
	}
	
	
	public static void removeAllConnectionNotifications(Type exceptType)
	{
		try
		{
			// clear all known connection notifications
		    String ns = Context.NOTIFICATION_SERVICE;
			NotificationManager mNotificationManager = (NotificationManager) Constants.applicationContext.getSystemService(ns);
			
			if (exceptType == null || exceptType != Type.AVAILABLE_HOTSPOT)
				mNotificationManager.cancel(Type.AVAILABLE_HOTSPOT.getValue());
			
			if (exceptType == null || exceptType != Type.CONNECTING)
				mNotificationManager.cancel(Type.CONNECTING.getValue());
			
			if (exceptType == null || exceptType != Type.AUTHENTICATING_WISPR)
				mNotificationManager.cancel(Type.AUTHENTICATING_WISPR.getValue());
			
			if (exceptType == null || exceptType != Type.AUTHENTICATED_WISPR)
				mNotificationManager.cancel(Type.AUTHENTICATED_WISPR.getValue());
			
			if (exceptType == null || exceptType != Type.FAILED_WISPR_AUTHENTICATION)
				mNotificationManager.cancel(Type.FAILED_WISPR_AUTHENTICATION.getValue());
			
			if (exceptType == null || exceptType != Type.UNTRUSTED_NETWORK)
				mNotificationManager.cancel(Type.UNTRUSTED_NETWORK.getValue());
			
			if (exceptType == null || exceptType != Type.AUTHENTICATING_ROVA_PORTAL_CREDENTIALS)
				mNotificationManager.cancel(Type.AUTHENTICATING_ROVA_PORTAL_CREDENTIALS.getValue());
			
			if (exceptType == null || exceptType != Type.FAILED_ROVA_PORTAL_AUTHENTICATION)
				mNotificationManager.cancel(Type.FAILED_ROVA_PORTAL_AUTHENTICATION.getValue());
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Log.Type.Error, "", e);
    	}
	}
	
	
	public static void setAlertMode(Notification notification)
	{
		try
		{
			if (!Constants.getData().getFlag(Flags.FLG_DisableNotificationVibration))
			{
				notification.defaults |= Notification.DEFAULT_VIBRATE;
			}

			if (!Constants.getData().getFlag(Flags.FLG_DisableNotificationAudio))
			{
				notification.defaults |= Notification.DEFAULT_SOUND;
			}
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Log.Type.Error, "setAlertMode", e);
    	}
	}
	
	
	public static void setDebugNotice()
    {
    	try
    	{
    		String notificationText = "This is a TEST build for internal use only";
    		String notificationSubtext = "It may contain test features and interface items";
    		
    		// Get a reference to the NotificationManager
    		String ns = Context.NOTIFICATION_SERVICE;
    		NotificationManager mNotificationManager = (NotificationManager) Constants.applicationContext.getSystemService(ns);
	    	
    		
	    	// Instantiate the Notification
	    	CharSequence tickerText = notificationText;
	    	long when = System.currentTimeMillis();
	    	
	    	Notification notification = new Notification(Constants.getLibrary().getClientLogoDrawableID(), tickerText, when);
	    	notification.flags |= Notification.FLAG_NO_CLEAR;
	    	notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
	    	
	    	// set alert modes
	    	setAlertMode(notification);
	    	
	    	// set content view
	    	notification.contentView = getContentView(notificationText, notificationSubtext);
	    		    
	    
	    	// create intent to launch app
	    	Intent intent = new Intent(Constants.applicationContext, Connect_MainActivity.class);
	    	notification.contentIntent = PendingIntent.getActivity(((ContextWrapper) Constants.applicationContext).getBaseContext(), 0, intent, 0);
	    	
	    	
	    	// pass the Notification to the NotificationManager
	    	mNotificationManager.notify(Type.NOTIFY_DEBUG_BUILD.value, notification);
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Log.Type.Error, "setDebugNotice", e);
    	}
    }
	
	
	public static void setAvailableHotspot(String ssid)
    {
    	try
    	{
    		// Remove previous notifications
    		removeAllConnectionNotifications(Type.AVAILABLE_HOTSPOT);
    		
    		if (ssid == null)
    			ssid = "";
    		
    		
    		String notificationText = Constants.applicationContext.getResources().getStringArray(R.array.notification_network_found)[Constants.getApplicationBuildType().getValue()].replace("%SSID%", ssid);
    		String notificationSubtext = Constants.applicationContext.getResources().getStringArray(R.array.click_here_to_connect)[Constants.getApplicationBuildType().getValue()];
    		
    		// Get a reference to the NotificationManager
    		String ns = Context.NOTIFICATION_SERVICE;
    		NotificationManager mNotificationManager = (NotificationManager) Constants.applicationContext.getSystemService(ns);
	    	
    		
	    	// Instantiate the Notification
	    	CharSequence tickerText = notificationText;
	    	long when = System.currentTimeMillis();
	    	
	    	Notification notification = new Notification(Constants.getLibrary().getClientLogoDrawableID(), tickerText, when);
	    	notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
	    	
	    	// set alert modes
	    	setAlertMode(notification);
	    	
	    	// set content view
	    	notification.contentView = getContentView(notificationText, notificationSubtext);
	    	
	    	
	    	
	    	// create intent to connect
	    	Intent intentConnect = new Intent(Constants.applicationContext, ApplicationService.class);
	    	intentConnect.setAction(ApplicationService.Action.SMART_CONNECT);		    			        		       
	    	notification.contentIntent = PendingIntent.getService(((ContextWrapper) Constants.applicationContext).getBaseContext(), 0, intentConnect, 0);
	    	
	    	
	    	// pass the Notification to the NotificationManager
	    	mNotificationManager.notify(Type.AVAILABLE_HOTSPOT.value, notification);
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Log.Type.Error, "setAvailableHotspot", e);
    	}
    }
	
	
	public static void setConnectingToHotspot(String ssid)
    {
    	try
    	{
    		// Remove previous notifications
    		removeAllConnectionNotifications(Type.CONNECTING);
    		
    		if (ssid == null)
    			ssid = "";
    		
    		
    		String notificationText = Constants.applicationContext.getResources().getStringArray(R.array.notification_network_connecting)[Constants.getApplicationBuildType().getValue()].replace("%SSID%", ssid);
    		String notificationSubtext = "";
    		
    		
    		// Get a reference to the NotificationManager
    		String ns = Context.NOTIFICATION_SERVICE;
    		NotificationManager mNotificationManager = (NotificationManager) Constants.applicationContext.getSystemService(ns);
	    	
	    	// Instantiate the Notification
	    	CharSequence tickerText = notificationText;
	    	long when = System.currentTimeMillis();
	    	
	    	Notification notification = new Notification(Constants.getLibrary().getClientLogoDrawableID(), tickerText, when);
	    	notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;

	    	
	    	// set content view
	    	notification.contentView = getContentView(notificationText, notificationSubtext);
	    	
	    	
	    	// Pass intent to cancel connection
	    	Intent notificationIntent =	new Intent(Constants.applicationContext,  ApplicationService.class);
	    	notificationIntent.setAction(ApplicationService.Action.CANCEL_CONNECTION);		    			        		       
	    				    	
	    	notification.contentIntent = PendingIntent.getService(((ContextWrapper) Constants.applicationContext).getBaseContext(), 0, notificationIntent, 0);
	    	
	    	
	    	// Pass the Notification to the NotificationManager:
	    	mNotificationManager.notify(Type.CONNECTING.value, notification);
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Log.Type.Error, "setConnectingToHotspot", e);
    	}
    }
	
	
	public static void setUntrustedAP(String ssid)
    {
    	try
    	{
    		// Remove previous notifications
    		removeAllConnectionNotifications(Type.UNTRUSTED_NETWORK);
    		
    		if (ssid == null)
    			ssid = "";
    		
    		
    		String notificationText = Constants.applicationContext.getResources().getStringArray(R.array.notification_network_untrusted)[Constants.getApplicationBuildType().getValue()].replace("%SSID%", ssid);
    		String notificationSubtext = Constants.applicationContext.getResources().getStringArray(R.array.click_here_to_try_again)[Constants.getApplicationBuildType().getValue()];
    		
    		// Get a reference to the NotificationManager
    		String ns = Context.NOTIFICATION_SERVICE;
    		NotificationManager mNotificationManager = (NotificationManager) Constants.applicationContext.getSystemService(ns);
	    	
	    	// Instantiate the Notification
	    	CharSequence tickerText = notificationText;
	    	long when = System.currentTimeMillis();
	    	
	    	Notification notification = new Notification(Constants.getLibrary().getClientLogoDrawableID(), tickerText, when);
	    	notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;

	    	
	    	// set content view
	    	notification.contentView = getContentView(notificationText, notificationSubtext);
	    	
	    	// set alert modes
	    	setAlertMode(notification);
	    	

	    	// create intent to connect
	    	Intent intentReconnect = new Intent(Constants.applicationContext, ApplicationService.class);
	    	intentReconnect.setAction(ApplicationService.Action.SMART_CONNECT);		    			        		       
	    	notification.contentIntent = PendingIntent.getService(((ContextWrapper) Constants.applicationContext).getBaseContext(), 0, intentReconnect, 0);
	    		
	    	
	    	// Pass the Notification to the NotificationManager:
	    	mNotificationManager.notify(Type.UNTRUSTED_NETWORK.value, notification);
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Log.Type.Error, "setUntrustedAP", e);
    	}
    }
	
	
	public static void setFailedToAssociate(String ssid)
    {
    	try
    	{
    		// Remove previous notifications
    		removeAllConnectionNotifications(Type.FAILED_WISPR_AUTHENTICATION);
    		
    		if (ssid == null)
    			ssid = "";
    		
    		String notificationText = Constants.applicationContext.getResources().getStringArray(R.array.notification_network_associate_failed_from)[Constants.getApplicationBuildType().getValue()].replace("%SSID%", ssid);
    		String notificationSubtext = Constants.applicationContext.getResources().getStringArray(R.array.click_here_to_try_again)[Constants.getApplicationBuildType().getValue()];
    		
    		// reset notification text if no SSID was specified
    		if (ssid == null || ssid.equals(""))
    		{
    			notificationText = Constants.applicationContext.getResources().getStringArray(R.array.notification_network_associate_failed_general)[Constants.getApplicationBuildType().getValue()];
    		}
    		
    		
    		// Get a reference to the NotificationManager
    		String ns = Context.NOTIFICATION_SERVICE;
    		NotificationManager mNotificationManager = (NotificationManager) Constants.applicationContext.getSystemService(ns);
	    	
	    	// Instantiate the Notification
	    	CharSequence tickerText = notificationText;
	    	long when = System.currentTimeMillis();
	    	
	    	Notification notification = new Notification(Constants.getLibrary().getClientLogoDrawableID(), tickerText, when);
	    	notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;

	    	
	    	// set content view
	    	notification.contentView = getContentView(notificationText, notificationSubtext);
	    	
	    	// set alert modes
	    	setAlertMode(notification);
	    	

	    	// create intent to connect
	    	Intent intentReconnect = new Intent(Constants.applicationContext,  ApplicationService.class);
	    	intentReconnect.setAction(ApplicationService.Action.SMART_CONNECT);		    			        		       
	    	notification.contentIntent = PendingIntent.getService(((ContextWrapper) Constants.applicationContext).getBaseContext(), 0, intentReconnect, 0);
	    		
	    	
	    	// Pass the Notification to the NotificationManager:
	    	mNotificationManager.notify(Type.FAILED_WISPR_AUTHENTICATION.value, notification);
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Log.Type.Error, "setFailedToAssociate", e);
    	}
    }
	
	
	public static void setAuthenticatingWISPr(String ssid)
    {
    	try
    	{
    		// Remove previous notifications
    		removeAllConnectionNotifications(Type.AUTHENTICATING_WISPR);
    		
    		if (ssid == null)
    			ssid = "";
    		
    		String notificationText = Constants.applicationContext.getResources().getStringArray(R.array.notification_network_authenticating)[Constants.getApplicationBuildType().getValue()].replace("%SSID%", ssid);
    		String notificationSubtext = "";
    		
    		// Get a reference to the NotificationManager
    		String ns = Context.NOTIFICATION_SERVICE;
    		NotificationManager mNotificationManager = (NotificationManager) Constants.applicationContext.getSystemService(ns);
	    	
	    	// Instantiate the Notification
	    	CharSequence tickerText = notificationText;
	    	long when = System.currentTimeMillis();
	    	
	    	Notification notification = new Notification(Constants.getLibrary().getClientLogoDrawableID(), tickerText, when);
	    	notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;

	    	
	    	// set content view
	    	notification.contentView = getContentView(notificationText, notificationSubtext);
	    	
	    	
	    	
	    	// Pass intent to cancel WISPr authentication
	    	Intent notificationIntent =	new Intent(Constants.applicationContext,  ApplicationService.class);
	    	notificationIntent.setAction(ApplicationService.Action.CANCEL_CONNECTION);		    			        		       			    	
	    	notification.contentIntent = PendingIntent.getService(((ContextWrapper) Constants.applicationContext).getBaseContext(), 0, notificationIntent, 0);
	    	
	    	
	    	// Pass the Notification to the NotificationManager:
	    	mNotificationManager.notify(Type.AUTHENTICATING_WISPR.value, notification);
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Log.Type.Error, "setAuthenticatingWISPr", e);
    	}
    }
	
	
	public static void setAuthenticatedWISPr(String ssid)
    {
    	try
    	{
    		// Remove previous notifications
    		removeAllConnectionNotifications(Type.AUTHENTICATED_WISPR);
    		
    		
    		if (ssid == null)
    			ssid = "";

    		
    		String notificationText = Constants.applicationContext.getResources().getStringArray(R.array.notification_network_authenticated)[Constants.getApplicationBuildType().getValue()].replace("%SSID%", ssid);
    		String notificationSubtext = Constants.applicationContext.getResources().getStringArray(R.array.click_here_to_disconnect)[Constants.getApplicationBuildType().getValue()];
    		
    		// Get a reference to the NotificationManager
    		String ns = Context.NOTIFICATION_SERVICE;
    		NotificationManager mNotificationManager = (NotificationManager) Constants.applicationContext.getSystemService(ns);
	    	
	    	// Instantiate the Notification
	    	CharSequence tickerText = notificationText;
	    	long when = System.currentTimeMillis();
	    	
	    	Notification notification = new Notification(Constants.getLibrary().getClientLogoDrawableID(), tickerText, when);
	    	notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;

	    	
	    	// set content view
	    	notification.contentView = getContentView(notificationText, notificationSubtext);
	    	
	    	// set alert modes
	    	setAlertMode(notification);
	    	
	    	
	    	// Pass intent to logoff WISPr and disconnect from hotspot
	    	Intent notificationIntent =	new Intent(Constants.applicationContext,  ApplicationService.class);
	    	notificationIntent.setAction(ApplicationService.Action.DISCONNECT);		    			        		       			    	
	    	notification.contentIntent = PendingIntent.getService(((ContextWrapper) Constants.applicationContext).getBaseContext(), 0, notificationIntent, 0);
	    	
	    	// Pass the Notification to the NotificationManager:
	    	mNotificationManager.notify(Type.AUTHENTICATED_WISPR.value, notification);
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Log.Type.Error, "setAuthenticatedWISPr", e);
    	}
    }
	
	
	public static void setFailedWISPrAuthentication(String ssid)
    {
    	try
    	{
    		// Remove previous notifications
    		removeAllConnectionNotifications(Type.FAILED_WISPR_AUTHENTICATION);
    		
    		if (ssid == null)
    			ssid = "";
    		
    		String notificationText = Constants.applicationContext.getResources().getStringArray(R.array.notification_network_authentication_failed_from)[Constants.getApplicationBuildType().getValue()].replace("%SSID%", ssid);
    		String notificationSubtext = Constants.applicationContext.getResources().getStringArray(R.array.click_here_to_try_again)[Constants.getApplicationBuildType().getValue()];
    		
    		// reset notification text if no SSID was specified
    		if (ssid == null || ssid.equals(""))
    		{
    			notificationText = Constants.applicationContext.getResources().getStringArray(R.array.notification_network_authentication_failed_general)[Constants.getApplicationBuildType().getValue()];
    		}
    		
    		// Get a reference to the NotificationManager
    		String ns = Context.NOTIFICATION_SERVICE;
    		NotificationManager mNotificationManager = (NotificationManager) Constants.applicationContext.getSystemService(ns);
	    	
	    	// Instantiate the Notification
	    	CharSequence tickerText = notificationText;
	    	long when = System.currentTimeMillis();
	    	
	    	Notification notification = new Notification(Constants.getLibrary().getClientLogoDrawableID(), tickerText, when);
	    	notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;

	    	
	    	// set content view
	    	notification.contentView = getContentView(notificationText, notificationSubtext);
	    	
	    	// set alert modes
	    	setAlertMode(notification);
	    	

	    	// create intent to connect
	    	Intent intentReconnect = new Intent(Constants.applicationContext,  ApplicationService.class);
	    	intentReconnect.setAction(ApplicationService.Action.SMART_CONNECT);		    			        		       
	    	notification.contentIntent = PendingIntent.getService(((ContextWrapper) Constants.applicationContext).getBaseContext(), 0, intentReconnect, 0);
	    		
	    	
	    	// Pass the Notification to the NotificationManager:
	    	mNotificationManager.notify(Type.FAILED_WISPR_AUTHENTICATION.value, notification);
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Log.Type.Error, "setFailedWISPrAuthentication", e);
    	}
    }
	
	
	public static void setAuthenticatingRovaPortalCredentials()
    {
    	try
    	{
    		// Remove previous notifications
    		removeAllConnectionNotifications(Type.AUTHENTICATING_ROVA_PORTAL_CREDENTIALS);
    		
    		String notificationText = Constants.applicationContext.getResources().getStringArray(R.array.notification_authenticating_rova_portal_credentials)[Constants.getApplicationBuildType().getValue()];
    		
    		// Get a reference to the NotificationManager
    		String ns = Context.NOTIFICATION_SERVICE;
    		NotificationManager mNotificationManager = (NotificationManager) Constants.applicationContext.getSystemService(ns);
	    	
	    	// Instantiate the Notification
	    	CharSequence tickerText = notificationText;
	    	long when = System.currentTimeMillis();
	    	
	    	Notification notification = new Notification(Constants.getLibrary().getClientLogoDrawableID(), tickerText, when);
	    	notification.flags |= Notification.FLAG_AUTO_CANCEL;

	    	
	    	// set content view
	    	notification.contentView = getContentView(notificationText, "");
	    	
	    	// set alert modes
	    	setAlertMode(notification);
	    	
	    	// create intent to do nothing
	    	Intent intentReconnect = new Intent(Constants.applicationContext,  ApplicationService.class);
	    	intentReconnect.setAction(ApplicationService.Action.NO_ACTION);		    			        		       
	    	notification.contentIntent = PendingIntent.getService(((ContextWrapper) Constants.applicationContext).getBaseContext(), 0, intentReconnect, 0);
	    	
	    	
	    	// Pass the Notification to the NotificationManager:
	    	mNotificationManager.notify(Type.AUTHENTICATING_ROVA_PORTAL_CREDENTIALS.value, notification);
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Log.Type.Error, "setAuthenticatingPortalCredentials", e);
    	}
    }


	public static void setFailedRovaPortalAuthentication()
    {
    	try
    	{
    		// Remove previous notifications
    		removeAllConnectionNotifications(Type.FAILED_ROVA_PORTAL_AUTHENTICATION);
    		
    		String notificationText = Constants.applicationContext.getResources().getStringArray(R.array.notification_authentication_failed_rova_portal_credentials)[Constants.getApplicationBuildType().getValue()];
    		String notificationSubtext = Constants.applicationContext.getResources().getStringArray(R.array.click_here_to_try_again)[Constants.getApplicationBuildType().getValue()];
    		
    		// reset text for first time intialization
    		if (!Constants.getData().getFlag(Flags.FLG_HasInitialized))
    		{
    			notificationText = Constants.applicationContext.getResources().getStringArray(R.array.notification_enter_rova_portal_credentials)[Constants.getApplicationBuildType().getValue()];
        		notificationSubtext = Constants.applicationContext.getResources().getStringArray(R.array.click_here_to_authenticate)[Constants.getApplicationBuildType().getValue()];
    		}

    		// Get a reference to the NotificationManager
    		String ns = Context.NOTIFICATION_SERVICE;
    		NotificationManager mNotificationManager = (NotificationManager) Constants.applicationContext.getSystemService(ns);
	    	
	    	// Instantiate the Notification
	    	CharSequence tickerText = notificationText;
	    	long when = System.currentTimeMillis();
	    	
	    	Notification notification = new Notification(Constants.getLibrary().getClientLogoDrawableID(), tickerText, when);
	    	notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;

	    	
	    	// set content view
	    	notification.contentView = getContentView(notificationText, notificationSubtext);
	    	
	    	// set alert modes
	    	setAlertMode(notification);
	    	

	    	// create intent to update configuration to retry
	    	Intent intent = new Intent(Constants.applicationContext,  TabHostActivity.class);
	    	intent.setAction(TabHostActivity.ACTION_UPDATE_CONFIGURATION);		    			        		       
	    	notification.contentIntent = PendingIntent.getActivity(((ContextWrapper) Constants.applicationContext).getBaseContext(), 0, intent, 0);
	    		
	    	
	    	// Pass the Notification to the NotificationManager:
	    	mNotificationManager.notify(Type.FAILED_ROVA_PORTAL_AUTHENTICATION.value, notification);
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Log.Type.Error, "setFailedRovaPortalAuthentication", e);
    	}
    }

	
	public static void setRequestDeviceAdministration()
    {
    	try
    	{
    		String notificationText = Constants.applicationContext.getResources().getStringArray(R.array.notification_request_device_administrator)[Constants.getApplicationBuildType().getValue()];
    		String notificationSubtext = Constants.applicationContext.getResources().getStringArray(R.array.click_here_to_authorize)[Constants.getApplicationBuildType().getValue()];
    		
    		// Get a reference to the NotificationManager
    		String ns = Context.NOTIFICATION_SERVICE;
    		NotificationManager mNotificationManager = (NotificationManager) Constants.applicationContext.getSystemService(ns);
	    	
    		
	    	// Instantiate the Notification
	    	CharSequence tickerText = notificationText;
	    	long when = System.currentTimeMillis();
	    	
	    	Notification notification = new Notification(Constants.getLibrary().getClientLogoDrawableID(), tickerText, when);
	    	notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
	    	
	    	// set alert modes
	    	setAlertMode(notification);
	    	
	    	// set content view
	    	notification.contentView = getContentView(notificationText, notificationSubtext);
	    	

	    	// create intent to request device administration
	    	Intent intentConnect = new Intent(Constants.applicationContext, ApplicationService.class);
	    	intentConnect.setAction(ApplicationService.Action.REQUEST_DEVICE_ADMINISTRATOR_ACCESSS);		    			        		       
	    	notification.contentIntent = PendingIntent.getService(((ContextWrapper) Constants.applicationContext).getBaseContext(), 0, intentConnect, 0);
	    	
	    	
	    	// pass the Notification to the NotificationManager
	    	mNotificationManager.notify(Type.REQUEST_DEVICE_ADMINISTRATION.value, notification);
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Log.Type.Error, "setRequestDeviceAdministration", e);
    	}
    }
	
	
	public static void setApplicationsRequired()
    {
    	try
    	{
    		String notificationText = Constants.applicationContext.getResources().getStringArray(R.array.notification_applications_required)[Constants.getApplicationBuildType().getValue()];
    		String notificationSubtext = Constants.applicationContext.getResources().getStringArray(R.array.click_here_to_view)[Constants.getApplicationBuildType().getValue()];
    		
    		// Get a reference to the NotificationManager
    		String ns = Context.NOTIFICATION_SERVICE;
    		NotificationManager mNotificationManager = (NotificationManager) Constants.applicationContext.getSystemService(ns);
	    	
    		
	    	// Instantiate the Notification
	    	CharSequence tickerText = notificationText;
	    	long when = System.currentTimeMillis();
	    	
	    	Notification notification = new Notification(Constants.getLibrary().getClientLogoDrawableID(), tickerText, when);
	    	notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
	    	
	    	// set alert modes
	    	setAlertMode(notification);
	    	
	    	// set content view
	    	notification.contentView = getContentView(notificationText, notificationSubtext);

	    	// create intent
	    	Intent intent = new Intent(Constants.applicationContext, ApplicationService.class);
	    	intent.setAction(ApplicationService.Action.NOTIFY_APPLICATIONS_REQUIRED);		    			        		       
	    	notification.contentIntent = PendingIntent.getService(((ContextWrapper) Constants.applicationContext).getBaseContext(), 0, intent, 0);
   	
	    	
	    	// pass the Notification to the NotificationManager
	    	mNotificationManager.notify(Type.NOTIFY_APPLICATIONS_REQUIRED.value, notification);
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Log.Type.Error, "setApplicationsRequired", e);
    	}
    }
	
	
	public static void setNewApplicationsAvailable(int appCount)
    {
    	try
    	{
    		String notificationText = "";
    		String notificationSubtext = Constants.applicationContext.getResources().getStringArray(R.array.click_here_to_view)[Constants.getApplicationBuildType().getValue()];
    		
    		
    		// set text based on count
    		if (appCount == 1)
    			notificationText = Constants.applicationContext.getResources().getStringArray(R.array.notification_new_application_available)[Constants.getApplicationBuildType().getValue()];
    		else 
    		{
    			notificationText = Constants.applicationContext.getResources().getStringArray(R.array.notification_new_applications_available)[Constants.getApplicationBuildType().getValue()];
    		
    			// replace token with count
    			notificationText = notificationText.replace("%COUNT%", String.valueOf(appCount));
    		}
    		
    		// Get a reference to the NotificationManager
    		String ns = Context.NOTIFICATION_SERVICE;
    		NotificationManager mNotificationManager = (NotificationManager) Constants.applicationContext.getSystemService(ns);
	    	
    		
	    	// Instantiate the Notification
	    	CharSequence tickerText = notificationText;
	    	long when = System.currentTimeMillis();
	    	
	    	Notification notification = new Notification(Constants.getLibrary().getClientLogoDrawableID(), tickerText, when);
	    	notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
	    	
	    	// set alert modes
	    	setAlertMode(notification);
	    	
	    	// set content view
	    	notification.contentView = getContentView(notificationText, notificationSubtext);

	    	// create intent
	    	Intent intent = new Intent(Constants.applicationContext, ApplicationService.class);
	    	intent.setAction(ApplicationService.Action.VIEW_AVAILABLE_APPS);		    			        		       
	    	notification.contentIntent = PendingIntent.getService(((ContextWrapper) Constants.applicationContext).getBaseContext(), 0, intent, 0);
   	
	    	
	    	// pass the Notification to the NotificationManager
	    	mNotificationManager.notify(Type.NOTIFY_NEW_APPLICATIONS_AVAILABLE.value, notification);
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Log.Type.Error, "setApplicationsRequired", e);
    	}
    }
	
	
	public static void setEnrollmentPolicyFailure()
    {
    	try
    	{
    		String notificationText = Constants.applicationContext.getResources().getStringArray(R.array.enrollment_policy_failure)[Constants.getApplicationBuildType().getValue()];
    		String notificationSubtext = "";
    		
    		// Get a reference to the NotificationManager
    		String ns = Context.NOTIFICATION_SERVICE;
    		NotificationManager mNotificationManager = (NotificationManager) Constants.applicationContext.getSystemService(ns);
	    	
    		
	    	// Instantiate the Notification
	    	CharSequence tickerText = notificationText;
	    	long when = System.currentTimeMillis();
	    	
	    	Notification notification = new Notification(Constants.getLibrary().getClientLogoDrawableID(), tickerText, when);
	    	notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
	    	
	    	// set alert modes
	    	setAlertMode(notification);
	    	
	    	// set content view
	    	notification.contentView = getContentView(notificationText, notificationSubtext);
	    	

	    	// create intent
	    	Intent intent = new Intent(Constants.applicationContext, CredentialPromptActivity.class);
	    	intent.setAction(CredentialPromptActivity.ACTION_ALERT_ENROLLMENT_POLICY_FAILURE);		    			        		       
	    	notification.contentIntent = PendingIntent.getActivity(((ContextWrapper) Constants.applicationContext).getBaseContext(), 0, intent, 0);
	    	
	    	
	    	// pass the Notification to the NotificationManager
	    	mNotificationManager.notify(Type.ENROLLMENT_POLICY_FAILURE.value, notification);
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Log.Type.Error, "setEnrollmentPolicyFailure", e);
    	}
    }
	
	
	public static void setLocationAccessRequired()
    {
    	try
    	{
    		String notificationText = Constants.applicationContext.getResources().getStringArray(R.array.location_access_required)[Constants.getApplicationBuildType().getValue()];
    		String notificationSubtext = Constants.applicationContext.getResources().getStringArray(R.array.tap_to_view)[Constants.getApplicationBuildType().getValue()];
    		
    		// Get a reference to the NotificationManager
    		String ns = Context.NOTIFICATION_SERVICE;
    		NotificationManager mNotificationManager = (NotificationManager) Constants.applicationContext.getSystemService(ns);
	    	
    		
	    	// Instantiate the Notification
	    	CharSequence tickerText = notificationText;
	    	long when = System.currentTimeMillis();
	    	
	    	Notification notification = new Notification(Constants.getLibrary().getClientLogoDrawableID(), tickerText, when);
	    	notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
	    	
	    	// set alert modes
	    	setAlertMode(notification);
	    	
	    	// set content view
	    	notification.contentView = getContentView(notificationText, notificationSubtext);
	    	

	    	// create intent to launch selection screen
	    	Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);       
	    	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    	notification.contentIntent = PendingIntent.getActivity(((ContextWrapper) Constants.applicationContext).getBaseContext(), 0, intent, 0);
	    	
	    	
	    	// pass the Notification to the NotificationManager
	    	mNotificationManager.notify(Type.LOCATION_ACCESS_REQUIRED.value, notification);
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Log.Type.Error, "setLocationAccessRequired", e);
    	}
    }
	
	
	public static void setPasswordChangeRequired()
    {
    	try
    	{
    		String notificationText = Constants.applicationContext.getResources().getStringArray(R.array.notification_password_reset_required)[Constants.getApplicationBuildType().getValue()];
    		String notificationSubtext = Constants.applicationContext.getResources().getStringArray(R.array.click_here_to_reset)[Constants.getApplicationBuildType().getValue()];
    		
    		// Get a reference to the NotificationManager
    		String ns = Context.NOTIFICATION_SERVICE;
    		NotificationManager mNotificationManager = (NotificationManager) Constants.applicationContext.getSystemService(ns);
	    	
    		
	    	// Instantiate the Notification
	    	CharSequence tickerText = notificationText;
	    	long when = System.currentTimeMillis();
	    	
	    	Notification notification = new Notification(Constants.getLibrary().getClientLogoDrawableID(), tickerText, when);
	    	notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
	    	
	    	// set alert modes
	    	setAlertMode(notification);
	    	
	    	// set content view
	    	notification.contentView = getContentView(notificationText, notificationSubtext);
	    	

	    	// create intent to request device administration
	    	Intent intentConnect = new Intent(Constants.applicationContext, ApplicationService.class);
	    	intentConnect.setAction(ApplicationService.Action.REQUEST_PASSWORD_RESET);	
	    	notification.contentIntent = PendingIntent.getService(((ContextWrapper) Constants.applicationContext).getBaseContext(), 0, intentConnect, 0);
	    	
	    	
	    	// pass the Notification to the NotificationManager
	    	mNotificationManager.notify(Type.REQUEST_PASSWORD_RESET.value, notification);
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Log.Type.Error, "setPasswordChangeRequired", e);
    	}
    }
	
	
	public static void setFailedToOpenDatabase()
    {
    	try
    	{
    		String notificationText = Constants.applicationContext.getResources().getStringArray(R.array.notification_failed_to_open_database)[Constants.getApplicationBuildType().getValue()];
    		String notificationSubtext = Constants.applicationContext.getResources().getStringArray(R.array.click_here_to_try_again)[Constants.getApplicationBuildType().getValue()];
    		
    		// Get a reference to the NotificationManager
    		String ns = Context.NOTIFICATION_SERVICE;
    		NotificationManager mNotificationManager = (NotificationManager) Constants.applicationContext.getSystemService(ns);
	    	
    		
	    	// Instantiate the Notification
	    	CharSequence tickerText = notificationText;
	    	long when = System.currentTimeMillis();
	    	
	    	Notification notification = new Notification(Constants.getLibrary().getClientLogoDrawableID(), tickerText, when);
	    	notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
	    	
	    	// set alert modes
	    	setAlertMode(notification);
	    	
	    	// set content view
	    	notification.contentView = getContentView(notificationText, notificationSubtext);
	    	

	    	// create intent to re-initialize service
	    	Intent intentConnect = new Intent(Constants.applicationContext, ApplicationService.class);
	    	intentConnect.setAction(ApplicationService.Action.INITIALIZE_SERVICE);	
	    	notification.contentIntent = PendingIntent.getService(((ContextWrapper) Constants.applicationContext).getBaseContext(), 0, intentConnect, 0);
	    	
	    	
	    	// pass the Notification to the NotificationManager
	    	mNotificationManager.notify(Type.FAILED_TO_OPEN_DATABASE.value, notification);
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Log.Type.Error, "setPasswordChangeRequired", e);
    	}
    }
	
	
	private static RemoteViews getContentView(String notificationText, String notificationSubtext)
	{
		RemoteViews contentView = null;
		
		try
		{
			// use styles supported in 2.3+
			if (Build.VERSION.SDK_INT >= 9)
				contentView = new RemoteViews(Constants.applicationContext.getPackageName(), R.layout.notification_layout);
			
			// use older method
			else
				contentView = new RemoteViews(Constants.applicationContext.getPackageName(), R.layout.notification_layout_2_2);
			
	    	contentView.setImageViewResource(R.id.notify_image, Constants.getLibrary().getClientLogoDrawableID());
	    	contentView.setTextViewText(R.id.TextView_notification, notificationText);
	    	contentView.setTextViewText(R.id.TextView_subtext, notificationSubtext);
		}
		catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Log.Type.Error, "getContentView", e);
    	}

		return contentView;
	}
}
