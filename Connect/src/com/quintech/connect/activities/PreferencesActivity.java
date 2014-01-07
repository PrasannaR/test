package com.quintech.connect.activities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.quintech.common.AbstractConstants.BuildType;
import com.quintech.common.AbstractData.Flags;
import com.quintech.common.AbstractData.Settings;
import com.quintech.common.Configuration;
import com.quintech.common.Credentials;
import com.quintech.common.ILog.Type;
import com.quintech.connect.*;
import com.quintech.connect.ApplicationService.Action;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputType;
import android.view.KeyEvent;
import android.widget.*;

@SuppressWarnings("deprecation")
public class PreferencesActivity extends PreferenceActivity  
{
	private static String TAG = "PreferencesActivity";
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) 
	{        
        super.onCreate(savedInstanceState);   
        
        try
        {
	        // set layout
	        addPreferencesFromResource(R.layout.preferences);     
	
	        // set transparent color hint to avoid a black background while scrolling
	        this.getListView().setCacheColorHint(0);
	        
	        // set the background here to avoid a black background on the 3.1 tablet devices
	        this.getListView().setBackgroundResource(R.drawable.app_background);
	        
	        
	        // non-debug settings
	        setNonDebugControlProperties();
	        
	        // DEBUG SETTINGS
	        setDebugControlProperties();
        }
        catch (Exception e)
       	{
   			Constants.getLog().add(TAG, Type.Error, "onCreate", e);
       	}
    }	
	
	
	@Override
    protected void onNewIntent(Intent intent) 
    {
    	try
    	{
	    	super.onNewIntent(intent);
	    	
	    	// refresh UI
	    	refreshUI();
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "onNewIntent", e);
    	}
    }
	
	
	@Override
	protected void onStart()
	{
		try
    	{
	    	super.onStart();
	    	
	    	refreshUI();
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "onStart", e);
    	}
	}
	
	
	@Override
	protected void onResume() 
	{
		try
		{
			super.onResume();
			
			refreshUI();
		}
		catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "onResume", e);
    	}
	}
	
	
	public void refreshUI()
	{
		try
		{
			// re-enable button and reset text
			Preference prefConfigurationUpdate = (Preference) findPreference("update_configuration");
			 
			if (Configuration.isInProgress())
			{
				prefConfigurationUpdate.setEnabled(false);
				prefConfigurationUpdate.setTitle(getResources().getStringArray(R.array.updating_configuration)[Constants.getApplicationBuildType().getValue()]);
			}
			
			else
			{
				prefConfigurationUpdate.setEnabled(true);
				prefConfigurationUpdate.setTitle(getResources().getStringArray(R.array.update_configuration)[Constants.getApplicationBuildType().getValue()]);
			}
			
			// reset last updated text
			setLastUpdatedText();
			
			
			// reset Smart Connect value (it may have changed from the Connect tab)
			CheckBoxPreference prefSmartConnect = (CheckBoxPreference) findPreference("smart_connect");
			prefSmartConnect.setChecked(Constants.getData().getFlag(Flags.FLG_AutoConnect));
			 
			
			// check location reporting policy
			PreferenceCategory categoryGeneral = (PreferenceCategory)getPreferenceScreen().getPreferenceManager().findPreference("general");
	        CheckBoxPreference prefNotificationsLocationReporting = (CheckBoxPreference) findPreference("enable_location_reporting");
	        
        	if (prefNotificationsLocationReporting == null)
        	{
        		// create new instance
        		prefNotificationsLocationReporting = new CheckBoxPreference(this);
        		prefNotificationsLocationReporting.setKey("enable_location_reporting");
        		
        		// reset properties
        		prefNotificationsLocationReporting.setPersistent(false);
    	        prefNotificationsLocationReporting.setTitle(getResources().getStringArray(R.array.enable_location_reporting)[Constants.getApplicationBuildType().getValue()]);
    	        prefNotificationsLocationReporting.setSummary(getResources().getStringArray(R.array.check_to_enable)[Constants.getApplicationBuildType().getValue()]);
    	        prefNotificationsLocationReporting.setChecked(!Constants.getData().getFlag(Flags.FLG_UserDisabledLocationReporting));
    	        prefNotificationsLocationReporting.setLayoutResource(R.layout.custom_preference_layout);
    	        
    	        prefNotificationsLocationReporting.setOnPreferenceChangeListener(new OnPreferenceChangeListener() 
    	        {
    				@Override
    				public boolean onPreferenceChange(Preference preference, Object newValue) 
    				{
    					try
    					{
    						// save value
    						Constants.getData().setFlag(Flags.FLG_UserDisabledLocationReporting, !Boolean.valueOf(newValue.toString()), false);
    					}
    					catch (Exception e)
    		           	{
    		       			Constants.getLog().add(TAG, Type.Error, "EventHandler", e);
    		           	}
    					
    					return true;
    				}
    			});
        	}
	        
	        // remove existing preference by default
        	try
        	{
        		categoryGeneral.removePreference(prefNotificationsLocationReporting);
        	}
        	catch (Exception e) { } 
        	
        	
	        
	        // only show location reporting option if user preference is allowed by policy
	        if (Constants.getData().getSetting(Settings.SET_ReportDeviceLocation).equals("2"))
	        {
	        	// add preference
	        	categoryGeneral.addPreference(prefNotificationsLocationReporting);
	        	
	        	// set values
	        	prefNotificationsLocationReporting.setChecked(!Constants.getData().getFlag(Flags.FLG_UserDisabledLocationReporting));
	        	prefNotificationsLocationReporting.setEnabled(true);
	        }
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "refreshUI", e);
    	}
	}
		
	
	private void setNonDebugControlProperties()
	{
		try
		{
			// set category reference
	        PreferenceCategory categoryGeneral = (PreferenceCategory) findPreference("general");
	      
	        
	        // smart connect
	        CheckBoxPreference prefSmartConnect = (CheckBoxPreference) findPreference("smart_connect");
	        prefSmartConnect.setPersistent(false);
	        prefSmartConnect.setTitle(getResources().getStringArray(R.array.smart_connect)[Constants.getApplicationBuildType().getValue()]);
	        prefSmartConnect.setSummary(getResources().getStringArray(R.array.connect_to_known_hotspots)[Constants.getApplicationBuildType().getValue()]);
	        prefSmartConnect.setChecked(Constants.getData().getFlag(Flags.FLG_AutoConnect));
	        
	        final Activity activity = this;
	        
	        prefSmartConnect.setOnPreferenceChangeListener(new OnPreferenceChangeListener() 
	        {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) 
				{
					try
					{
						// save value
						Constants.getData().setFlag(Flags.FLG_AutoConnect, Boolean.valueOf(newValue.toString()), false);
						
						// reset Smart Connect when changing the setting
						Constants.getSmartConnect().reset();
						
						// if turning Smart Connect on, attempt to make connection
						if (Boolean.valueOf(newValue.toString()))
						{
							// create intent to connect
					    	Intent intentConnect = new Intent(Constants.applicationContext, ApplicationService.class);
					    	intentConnect.setAction(ApplicationService.Action.SMART_CONNECT);		    			        		       
					    	activity.startService(intentConnect);
						}
					}
					catch (Exception e)
		           	{
		       			Constants.getLog().add(TAG, Type.Error, "EventHandler", e);
		           	}
					
					return true;
				}
			});
	        
	        
	        // audio notifications
	        CheckBoxPreference prefNotificationsAudio = (CheckBoxPreference) findPreference("notifications_audio");
	        prefNotificationsAudio.setPersistent(false);
	        prefNotificationsAudio.setTitle(getResources().getStringArray(R.array.audio_notifications)[Constants.getApplicationBuildType().getValue()]);
	        prefNotificationsAudio.setSummary(getResources().getStringArray(R.array.check_to_enable)[Constants.getApplicationBuildType().getValue()]);
	        prefNotificationsAudio.setChecked(!Constants.getData().getFlag(Flags.FLG_DisableNotificationAudio));
	        
	        prefNotificationsAudio.setOnPreferenceChangeListener(new OnPreferenceChangeListener() 
	        {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) 
				{
					try
					{
						// save value
						Constants.getData().setFlag(Flags.FLG_DisableNotificationAudio, !Boolean.valueOf(newValue.toString()), false);
					}
					catch (Exception e)
		           	{
		       			Constants.getLog().add(TAG, Type.Error, "EventHandler", e);
		           	}
					
					return true;
				}
			});
	        
	        
	        // vibration notifications
	        CheckBoxPreference prefNotificationsVibration = (CheckBoxPreference) findPreference("notifications_vibrate");
	        prefNotificationsVibration.setPersistent(false);
	        prefNotificationsVibration.setTitle(getResources().getStringArray(R.array.vibration_notifications)[Constants.getApplicationBuildType().getValue()]);
	        prefNotificationsVibration.setSummary(getResources().getStringArray(R.array.check_to_enable)[Constants.getApplicationBuildType().getValue()]);
	        prefNotificationsVibration.setChecked(!Constants.getData().getFlag(Flags.FLG_DisableNotificationVibration));
	        
	        prefNotificationsVibration.setOnPreferenceChangeListener(new OnPreferenceChangeListener() 
	        {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) 
				{
					try
					{
						// save value
						Constants.getData().setFlag(Flags.FLG_DisableNotificationVibration, !Boolean.valueOf(newValue.toString()), false);
					}
					catch (Exception e)
		           	{
		       			Constants.getLog().add(TAG, Type.Error, "EventHandler", e);
		           	}
					
					return true;
				}
			});
	        
	        
	        // update configuration
	        Preference prefConfigurationUpdate = (Preference) findPreference("update_configuration");
	        prefConfigurationUpdate.setPersistent(false);
	        prefConfigurationUpdate.setTitle(getResources().getStringArray(R.array.update_configuration)[Constants.getApplicationBuildType().getValue()]);
	        setLastUpdatedText();
	        
	        prefConfigurationUpdate.setOnPreferenceClickListener(new OnPreferenceClickListener() 
	        {
				@Override
				public boolean onPreferenceClick(Preference preference) 
				{
					try
					{
						// disable button
						preference.setEnabled(false);
						preference.setTitle(getResources().getStringArray(R.array.updating_configuration)[Constants.getApplicationBuildType().getValue()]);
					
						Intent intentAlarm = new Intent(Constants.applicationContext, OnAlarmReceiver.class);		
						intentAlarm.setAction(Action.UPDATE_ALARM);
						LocalBroadcastManager.getInstance(Constants.applicationContext).sendBroadcast(intentAlarm);

//						// run this in a new thread
//						Thread t = new Thread()
//						{
//				            public void run() 
//				            {
//				            	Configuration.update(true);
//								// Notify container
//								if (true)
//								{
//									
//									if (Constants.getData().getFlag(Flags.FLG_PIMSettings_Updated))
//									{
//										Intent return_intent = new Intent("com.cognizant.trubox.event");
//
//										// Add Event Type
//										return_intent.setAction("PIM_Configuration_Update_Event");
//										
//										return_intent.putExtra("Event","PIM_Configuration_Update_Event");
//										LocalBroadcastManager.getInstance(Constants.applicationContext).sendBroadcast(return_intent);
//									}
//									
//									if (Constants.getData().getFlag(Flags.FLG_SecuritySettings_Updated))
//									{
//										Intent return_intent = new Intent("com.cognizant.trubox.event");
//
//										// Add Event Type
//										return_intent.setAction("Security_Profile_Change_Event");
//										
//										return_intent.putExtra("Event","Security_Profile_Change_Event");
//										LocalBroadcastManager.getInstance(Constants.applicationContext).sendBroadcast(return_intent);
//									}			
//									
//									if (Constants.getData().getFlag(Flags.FLG_EmailSettings_Updated))
//									{
//										Intent return_intent = new Intent("com.cognizant.trubox.event");
//
//										// Add Event Type
//										return_intent.setAction("Email_Settings_Update_Complete_Event");
//										
//										return_intent.putExtra("Event","Email_Settings_Update_Complete_Event");
//										LocalBroadcastManager.getInstance(Constants.applicationContext).sendBroadcast(return_intent);
//									}			
//										
//								}				           
//							}
//				        };
//				        t.start();
					}
					catch (Exception e)
		           	{
		       			Constants.getLog().add(TAG, Type.Error, "EventHandler", e);
		           	}
			        
					return true;
				}
			});
	        
	        
	        // enable location reporting
	        CheckBoxPreference prefNotificationsLocationReporting = (CheckBoxPreference) findPreference("enable_location_reporting");
	        prefNotificationsLocationReporting.setPersistent(false);
	        prefNotificationsLocationReporting.setTitle(getResources().getStringArray(R.array.enable_location_reporting)[Constants.getApplicationBuildType().getValue()]);
	        prefNotificationsLocationReporting.setSummary(getResources().getStringArray(R.array.check_to_enable)[Constants.getApplicationBuildType().getValue()]);
	        prefNotificationsLocationReporting.setChecked(!Constants.getData().getFlag(Flags.FLG_UserDisabledLocationReporting));
	        
	        prefNotificationsLocationReporting.setOnPreferenceChangeListener(new OnPreferenceChangeListener() 
	        {
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) 
				{
					try
					{
						// save value
						Constants.getData().setFlag(Flags.FLG_UserDisabledLocationReporting, !Boolean.valueOf(newValue.toString()), false);
					}
					catch (Exception e)
		           	{
		       			Constants.getLog().add(TAG, Type.Error, "EventHandler", e);
		           	}
					
					return true;
				}
			});
	        
	        
	        // portal credentials
	        Preference prefPortalCredentials = (Preference) findPreference("set_portal_credentials");
	        
	        // show for VZB
	        if (Constants.getApplicationBuildType() == BuildType.VERIZON_BUSINESS)
	        {
		        prefPortalCredentials.setPersistent(false);
		        prefPortalCredentials.setTitle(getResources().getStringArray(R.array.set_rova_portal_credentials)[Constants.getApplicationBuildType().getValue()]);
		        prefPortalCredentials.setSummary(getResources().getStringArray(R.array.credentials_used_to_login_to_portal)[Constants.getApplicationBuildType().getValue()]);
		                
		        prefPortalCredentials.setOnPreferenceClickListener(new OnPreferenceClickListener() 
		        {
					@Override
					public boolean onPreferenceClick(Preference preference) 
					{
						try
						{
							// show credential prompt
							Constants.setCredentials(new Credentials(Credentials.PromptType.RovaPortalCredentials, "Login Credentials"));
							Constants.getCredentials().promptForCredentials();
						}
						catch (Exception e)
			           	{
			       			Constants.getLog().add(TAG, Type.Error, "EventHandler", e);
			           	}
						
						return true;
					}
				});
	        }
	        
	        // remove
	        else
	        	categoryGeneral.removePreference(prefPortalCredentials);
	        

	        
	        
	        // device administrator 
	        Preference prefUninstall = (Preference) findPreference("uninstall_application");
	        
	        // show for VZB
	        if (Constants.getApplicationBuildType() == BuildType.VERIZON_BUSINESS)
	        {
	        	prefUninstall.setPersistent(false);
        		prefUninstall.setTitle(getResources().getStringArray(R.array.uninstall_app)[Constants.getApplicationBuildType().getValue()]);
    			prefUninstall.setSummary(getResources().getStringArray(R.array.uninstall_app_info)[Constants.getApplicationBuildType().getValue()]);
		                
	        	prefUninstall.setOnPreferenceClickListener(new OnPreferenceClickListener() 
		        {
					@Override
					public boolean onPreferenceClick(Preference preference) 
					{
						try
						{
							// remove device administrator
							Constants.getDeviceManagement().removeDeviceAdministrator();
							
							// prompt to uninstall
							ApplicationInstallation.uninstall(Constants.applicationContext.getPackageName());
						}
						catch (Exception e)
						{
							Constants.getLog().add(TAG, Type.Error, "prefUninstall onPreferenceClick", e);
						}
						
						return true;
					}
				});
	        }
	        
	        // remove
	        else
	        	categoryGeneral.removePreference(prefUninstall);
	        
	        
	        
	        // show terms and conditions for VZW
	        Preference prefTerms = (Preference) findPreference("terms_and_conditions");
	        
	        if (Constants.getApplicationBuildType() == BuildType.VERIZON_WIRELESS)
	        {
	        	prefTerms.setPersistent(false);
	        	prefTerms.setTitle(getResources().getStringArray(R.array.terms_and_conditions)[Constants.getApplicationBuildType().getValue()]);
	        	prefTerms.setSummary(getResources().getStringArray(R.array.tap_to_view)[Constants.getApplicationBuildType().getValue()]);
	        	
	        	prefTerms.setOnPreferenceClickListener(new OnPreferenceClickListener() 
		        {
					@Override
					public boolean onPreferenceClick(Preference preference) 
					{
						try
						{
							// show credential terms
							Intent intent = new Intent(PreferencesActivity.this, TermsConditionsActivity.class);        
							startActivity(intent);
						}
						catch (Exception e)
			           	{
			       			Constants.getLog().add(TAG, Type.Error, "EventHandler", e);
			           	}
						
						return true;
					}
				});
	        }
	        
	        // remove
	        else
	        	categoryGeneral.removePreference(prefTerms);
	        
	        
	        
	        // set debug mode
	        Preference prefEnableDebugMode = (Preference) findPreference("enable_debug_mode");
	        prefEnableDebugMode.setPersistent(false);
	        prefEnableDebugMode.setTitle(getResources().getStringArray(R.array.enable_debug_mode)[Constants.getApplicationBuildType().getValue()]);
	        
	        prefEnableDebugMode.setOnPreferenceClickListener(new OnPreferenceClickListener() 
	        {
				@Override
				public boolean onPreferenceClick(Preference preference) 
				{
					try
					{
						// turn off by default
						Constants.getData().setFlag(Flags.FLG_EnableDebugMode, false, false);
						
						// show prompt
						showDebugPrompt();
					}
					catch (Exception e)
		           	{
		       			Constants.getLog().add(TAG, Type.Error, "EventHandler", e);
		           	}
			        
					return true;
				}
			});
		}
		catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "setNonDebugControlProperties", e);
    	}
	}
	
	
	private void setDebugControlProperties()
	{
		try
		{
			// remove debug options
			
			// create category
        	PreferenceCategory categoryDebug = new PreferenceCategory(this);
        	categoryDebug.setTitle("Debug Settings");
        	categoryDebug.setKey("debug");
        	
        	// remove existing category if it exists
        	try
        	{
        		getPreferenceScreen().removePreference(categoryDebug);
        	}
        	catch (Exception e) { } 
			
		
        	
			// add debug options
			if (Constants.getLibrary().isDebugBuild() || Constants.getData().getFlag(Flags.FLG_EnableDebugMode))
	        {
	        	// add new category
	        	getPreferenceScreen().addPreference(categoryDebug);
	        
	        	
	        	// add items
	        	
	            
	            // email logs
	            Preference prefEmailLogs = new Preference(this);
	            categoryDebug.addPreference(prefEmailLogs);
	            prefEmailLogs.setLayoutResource(R.layout.custom_preference_layout);
	            prefEmailLogs.setPersistent(false);
	            prefEmailLogs.setTitle(getResources().getStringArray(R.array.email_application_logs)[Constants.getApplicationBuildType().getValue()]);
	            prefEmailLogs.setSummary("");
	            
	            prefEmailLogs.setOnPreferenceClickListener(new OnPreferenceClickListener() 
	            {
	    			@Override
	    			public boolean onPreferenceClick(Preference preference) 
	    			{
	    				try
	    				{
	    					Constants.getLibrary().emailLogFiles();
	    				}
	    				catch (Exception e)
			           	{
			       			Constants.getLog().add(TAG, Type.Error, "EventHandler", e);
			           	}
	    				
	    				return true;
	    			}
	    		});
	            
	            
	            // minimum location accuracy
	            EditTextPreference prefMinLocationAccuracy = new EditTextPreference(this);
	            categoryDebug.addPreference(prefMinLocationAccuracy);
	            prefMinLocationAccuracy.setLayoutResource(R.layout.custom_preference_layout);
	            prefMinLocationAccuracy.setPersistent(false);
	            prefMinLocationAccuracy.setTitle(getResources().getStringArray(R.array.location_accuracy)[Constants.getApplicationBuildType().getValue()]);
	            prefMinLocationAccuracy.setSummary(getResources().getStringArray(R.array.location_accuracy_info)[Constants.getApplicationBuildType().getValue()]);
	            prefMinLocationAccuracy.setText(Constants.getData().getSetting(Settings.SET_MinimumLocationAccuracyMeters));
	            
	            prefMinLocationAccuracy.setOnPreferenceClickListener(new OnPreferenceClickListener() 
	            {
	    			@Override
	    			public boolean onPreferenceClick(Preference preference) 
	    			{
	    				try
	    				{
	    					// set default value
	    					int value = 80;
	    					
	    					// parse value
	    					value = Integer.valueOf(((EditTextPreference) preference).getText());
	    					
	    					// save value
	    					Constants.getData().setSetting(Settings.SET_MinimumLocationAccuracyMeters, String.valueOf(value), false);	
	    				}
	    				catch (Exception e)
			           	{
			       			Constants.getLog().add(TAG, Type.Error, "EventHandler", e);
			           	}
	    				
	    				return true;
	    			}
	    		});
	            
	            
	            
	            // use default WISPr credential
	            CheckBoxPreference prefUseDefaultCredential = new CheckBoxPreference(this);
	            categoryDebug.addPreference(prefUseDefaultCredential);
	            prefUseDefaultCredential.setKey("use_default_credential");
	            prefUseDefaultCredential.setLayoutResource(R.layout.custom_preference_layout);
	            prefUseDefaultCredential.setPersistent(false);
	            prefUseDefaultCredential.setTitle(getResources().getStringArray(R.array.always_use_default_wispr_credential)[Constants.getApplicationBuildType().getValue()]);
	            prefUseDefaultCredential.setSummary(getResources().getStringArray(R.array.for_all_wispr_authentications)[Constants.getApplicationBuildType().getValue()]);
	            prefUseDefaultCredential.setChecked(Constants.getData().getFlag(Flags.FLG_AlwaysUseDefaultWisprCredential));
	            
	            prefUseDefaultCredential.setOnPreferenceChangeListener(new OnPreferenceChangeListener() 
	            {
	    			@Override
	    			public boolean onPreferenceChange(Preference preference, Object newValue) 
	    			{
	    				try
	    				{
		    				// save value
		    				Constants.getData().setFlag(Flags.FLG_AlwaysUseDefaultWisprCredential, Boolean.valueOf(newValue.toString()), false);
	    				}
	    				catch (Exception e)
			           	{
			       			Constants.getLog().add(TAG, Type.Error, "EventHandler", e);
			           	}
	    				
	    				return true;
	    			}
	    		});
	            
	            
	            // set default credential
	            Preference prefSetDefaultCredential = new Preference(this);
	            categoryDebug.addPreference(prefSetDefaultCredential);
	            prefSetDefaultCredential.setLayoutResource(R.layout.custom_preference_layout);
	            prefSetDefaultCredential.setPersistent(false);
	            prefSetDefaultCredential.setTitle(getResources().getStringArray(R.array.set_default_credentials)[Constants.getApplicationBuildType().getValue()]);
	            prefSetDefaultCredential.setSummary(getResources().getStringArray(R.array.for_all_wispr_authentications)[Constants.getApplicationBuildType().getValue()]);
	            prefSetDefaultCredential.setDependency("use_default_credential");
	            
	            prefSetDefaultCredential.setOnPreferenceClickListener(new OnPreferenceClickListener() 
	            {
	    			@Override
	    			public boolean onPreferenceClick(Preference preference) 
	    			{
	    				try
	    				{
		    				// show credential prompt
							Constants.setCredentials(new Credentials(Credentials.PromptType.NetworkCredentials, "Network Credentials"));
							Constants.getCredentials().promptForCredentials();
	    				}
	    				catch (Exception e)
			           	{
			       			Constants.getLog().add(TAG, Type.Error, "EventHandler", e);
			           	}
	    				
	    				
	    				return true;
	    			}
	    		});
	            
	            
	            
	            // FOR VZW ONLY
	            if (Constants.getApplicationBuildType() == BuildType.VERIZON_WIRELESS)
	            {
		            // use default UserID decoration
		            CheckBoxPreference prefUseDefaultUserIdDecoration = new CheckBoxPreference(this);
		            categoryDebug.addPreference(prefUseDefaultUserIdDecoration);
		            prefUseDefaultUserIdDecoration.setKey("use_default_userid_decoration");
		            prefUseDefaultUserIdDecoration.setLayoutResource(R.layout.custom_preference_layout);
		            prefUseDefaultUserIdDecoration.setPersistent(false);
		            prefUseDefaultUserIdDecoration.setTitle(getResources().getStringArray(R.array.always_use_default_userid_decoration)[Constants.getApplicationBuildType().getValue()]);
		            prefUseDefaultUserIdDecoration.setSummary(getResources().getStringArray(R.array.for_all_wispr_authentications)[Constants.getApplicationBuildType().getValue()]);
		            prefUseDefaultUserIdDecoration.setChecked(Constants.getData().getFlag(Flags.FLG_AlwaysUseDefaultUserIdDecoration));
		            
		            prefUseDefaultUserIdDecoration.setOnPreferenceChangeListener(new OnPreferenceChangeListener() 
		            {
		    			@Override
		    			public boolean onPreferenceChange(Preference preference, Object newValue) 
		    			{
		    				try
		    				{
			    				// save value
			    				Constants.getData().setFlag(Flags.FLG_AlwaysUseDefaultUserIdDecoration, Boolean.valueOf(newValue.toString()), false);
		    				}
		    				catch (Exception e)
				           	{
				       			Constants.getLog().add(TAG, Type.Error, "EventHandler", e);
				           	}
		    				
		    				return true;
		    			}
		    		});
		            
		            
		         	// set default UserID decoration
		            Preference prefSetDefaultUserIdDecoration = new Preference(this);
		            categoryDebug.addPreference(prefSetDefaultUserIdDecoration);
		            prefSetDefaultUserIdDecoration.setLayoutResource(R.layout.custom_preference_layout);
		            prefSetDefaultUserIdDecoration.setPersistent(false);
		            prefSetDefaultUserIdDecoration.setTitle(getResources().getStringArray(R.array.set_default_userid_decoration)[Constants.getApplicationBuildType().getValue()]);
		            prefSetDefaultUserIdDecoration.setSummary(getResources().getStringArray(R.array.for_all_wispr_authentications)[Constants.getApplicationBuildType().getValue()]);
		            prefSetDefaultUserIdDecoration.setDependency("use_default_userid_decoration");
		            
		            prefSetDefaultUserIdDecoration.setOnPreferenceClickListener(new OnPreferenceClickListener() 
		            {
		    			@Override
		    			public boolean onPreferenceClick(Preference preference) 
		    			{
		    				try
		    				{
			    				// show credential prompt
			    				Constants.setCredentials(new Credentials(Credentials.PromptType.NetworkUsernameDecoration, "User ID Decoration"));
			    				Constants.getCredentials().promptForCredentials();
		    				}
		    				catch (Exception e)
				           	{
				       			Constants.getLog().add(TAG, Type.Error, "EventHandler", e);
				           	}
		    				
		    				
		    				return true;
		    			}
		    		});
		            
		            
		            // use default VZW server
		            CheckBoxPreference prefUseDefaultVzwServer = new CheckBoxPreference(this);
		            categoryDebug.addPreference(prefUseDefaultVzwServer);
		            prefUseDefaultVzwServer.setKey("use_default_vzw_server");
		            prefUseDefaultVzwServer.setLayoutResource(R.layout.custom_preference_layout);
		            prefUseDefaultVzwServer.setPersistent(false);
		            prefUseDefaultVzwServer.setTitle(getResources().getStringArray(R.array.always_use_default_vzw_server)[Constants.getApplicationBuildType().getValue()]);
		            prefUseDefaultVzwServer.setSummary("");
		            prefUseDefaultVzwServer.setChecked(Constants.getData().getFlag(Flags.FLG_AlwaysUseDefaultVZWServer));
		            
		            prefUseDefaultVzwServer.setOnPreferenceChangeListener(new OnPreferenceChangeListener() 
		            {
		    			@Override
		    			public boolean onPreferenceChange(Preference preference, Object newValue) 
		    			{
		    				try
		    				{
			    				// save value
			    				Constants.getData().setFlag(Flags.FLG_AlwaysUseDefaultVZWServer, Boolean.valueOf(newValue.toString()), false);
		    				}
		    				catch (Exception e)
				           	{
				       			Constants.getLog().add(TAG, Type.Error, "EventHandler", e);
				           	}
		    				
		    				
		    				return true;
		    			}
		    		});
		            
		            
		            // set default VZW server
		            Preference prefSetDefaultVzwServer = new Preference(this);
		            categoryDebug.addPreference(prefSetDefaultVzwServer);
		            prefSetDefaultVzwServer.setLayoutResource(R.layout.custom_preference_layout);
		            prefSetDefaultVzwServer.setPersistent(false);
		            prefSetDefaultVzwServer.setTitle(getResources().getStringArray(R.array.set_default_vzw_server)[Constants.getApplicationBuildType().getValue()]);
		            prefSetDefaultVzwServer.setSummary("");
		            prefSetDefaultVzwServer.setDependency("use_default_vzw_server");
		            
		            prefSetDefaultVzwServer.setOnPreferenceClickListener(new OnPreferenceClickListener() 
		            {
		    			@Override
		    			public boolean onPreferenceClick(Preference preference) 
		    			{
		    				try
		    				{
			    				// show prompt
			    				showDefaultVZWServerPrompt();
		    				}
		    				catch (Exception e)
				           	{
				       			Constants.getLog().add(TAG, Type.Error, "EventHandler", e);
				           	}
		    				
		    				
		    				return true;
		    			}
		    		});
	            }
	        }
		}
		catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "setDebugOptions", e);
    	}
	}
	
	
	private void showDebugPrompt()
	{
		try
		{
			Constants.getLog().add(TAG, Type.Info, "Prompting user for code to enter User Debug Mode.");
			
			// create and show dialog for setting default VZW server
			final EditText editTextDebugCode = new EditText(this);
			editTextDebugCode.setInputType(InputType.TYPE_CLASS_NUMBER);
			
			AlertDialog.Builder dialogDebugPrompt = new AlertDialog.Builder(PreferencesActivity.this);
			dialogDebugPrompt.setTitle("Enter User Debug Mode");
			dialogDebugPrompt.setView(editTextDebugCode);
			
	
			dialogDebugPrompt.setPositiveButton("Enter", new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int whichButton) 
				{
					try
					{
						// do nothing if nothing was entered
						if (editTextDebugCode.getText().toString().equals(""))
						{
							return;
						}
						
						// show debug options if correct code was entered
						if (editTextDebugCode.getText().toString().equals(Constants.getData().getSetting(Settings.SET_UserDebugCode)))
						{
							Constants.getLog().add(TAG, Type.Info, "User entered into User Debug Mode.");
							Constants.getData().setFlag(Flags.FLG_EnableDebugMode, true, false);
							
							// reset properties
							setDebugControlProperties();
						}
						
						else
						{
							Toast.makeText(Constants.applicationContext,  "Invalid code", Toast.LENGTH_SHORT).show();
						}
					}
					catch (Exception e)
		           	{
		       			Constants.getLog().add(TAG, Type.Error, "EventHandler", e);
		           	}
				}
			});
		    
			dialogDebugPrompt.setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int whichButton) 
				{
					 // do nothing
				}
			});
			
			// show dialog
			dialogDebugPrompt.show();
		}
		catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "showDebugPrompt", e);
    	}
	}
	
	
	private void setLastUpdatedText()
	{
		try
		{
			// set last updated text
			Preference prefConfigurationUpdate = (Preference) findPreference("update_configuration");
			
			long lastUpdatedMillis = 0;
			
			try
			{
				lastUpdatedMillis = Long.valueOf(Constants.getData().getSetting(Settings.SET_ConfigurationLastUpdatedMillis));
			}
			catch (Exception e) { }
			
			String message = "";
			
			// set message text if there is a last updated date
			if (lastUpdatedMillis > 0)
			{
				message = "Last updated on " + new Date(lastUpdatedMillis).toGMTString() + ".";
			}
			
			prefConfigurationUpdate.setSummary(message);
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "setLastUpdatedText", e);
    	}
	}
	
	
	private void showDefaultVZWServerPrompt()
	{
		try
		{
			// create and show dialog for setting default VZW server
			final EditText editTextDefaultVZWServer = new EditText(this);
			editTextDefaultVZWServer.setText(Constants.getData().getSetting(Settings.SET_DefaultVZWServer));
			
			AlertDialog.Builder dialogDefaultVZWServer = new AlertDialog.Builder(PreferencesActivity.this);
			dialogDefaultVZWServer.setTitle(getResources().getStringArray(R.array.set_default_vzw_server)[Constants.getApplicationBuildType().getValue()]);
			dialogDefaultVZWServer.setView(editTextDefaultVZWServer);
			
	
			dialogDefaultVZWServer.setPositiveButton("Save", new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int whichButton) 
				{
					try
					{
						Constants.getData().setSetting(Settings.SET_DefaultVZWServer, editTextDefaultVZWServer.getText().toString(), false);
					}
					catch (Exception e)
		           	{
		       			Constants.getLog().add(TAG, Type.Error, "EventHandler", e);
		           	}
				}
			});
		    
			dialogDefaultVZWServer.setNegativeButton("Cancel", new DialogInterface.OnClickListener() 
			{
				public void onClick(DialogInterface dialog, int whichButton) 
				{
					 // do nothing
				}
			});
			
			// show dialog
			dialogDefaultVZWServer.show();
		}
		catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "showDefaultVZWServerPrompt", e);
    	}
	}
}
