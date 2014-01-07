package com.quintech.connect.activities;



import com.quintech.common.Configuration;
import com.quintech.common.AbstractConstants.BuildType;
import com.quintech.common.AbstractDeviceManagement.PolicyFailureNotificationType;
import com.quintech.common.ILog.Type;
import com.quintech.connect.*;


import android.os.Bundle;
import android.app.TabActivity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.*;


@SuppressWarnings("deprecation")
public class TabHostActivity extends TabActivity
{
	public static String ACTION_NOTIFY_APPS_REQUIRED = "com.quintech.connect.ACTION_NOTIFY_APPS_REQUIRED";
	public static String ACTION_VIEW_AVAILABLE_APPS = "com.quintech.connect.ACTION_VIEW_AVAILABLE_APPS";
	public static String ACTION_UPDATE_CONFIGURATION = "com.quintech.connect.ACTION_UPDATE_CONFIGURATION";
	private static String TAG = "TabHostActivity";
	private static String TAB_CONNECT = "CONNECT";
	private static String TAB_APPS = "APPLICATIONS";
	private static String TAB_SETTINGS = "SETTINGS";

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	try
    	{
    		super.onCreate(savedInstanceState);
    		
    		// ensure log file is initialized
			Constants.getLog().initialize();

    		Constants.getLog().add(TAG, Type.Verbose, "Activity created.");
    		
    		// use custom title bar
	        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	        setContentView(R.layout.tab_host);
	        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title_bar);

			//Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/VerizonApex-Medium.otf");
			TextView tv = (TextView) findViewById(R.id.TextViewHeader);
			
			// set custom font
			//tv.setTypeface(tf);
			
			// set default text
			tv.setText(getResources().getStringArray(R.array.app_name)[Constants.getApplicationBuildType().getValue()].toUpperCase());
			
    		

	        // get tab host			
	        TabHost tabHost = getTabHost();

	        
	        // initialize a TabSpec for each tab and add it to the TabHost
	        TabHost.TabSpec spec;  						
	        Intent intent;  
	        View tabIndicator;
	        TextView textViewTitle;
	        ImageView imageViewIcon;
	

	        // connect tab
	        intent = new Intent().setClass(this, ConnectActivity.class);
	        spec = tabHost.newTabSpec(TAB_CONNECT);
	        
	        tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_indicator, getTabWidget(), false);
	        textViewTitle = (TextView) tabIndicator.findViewById(R.id.title);
	        textViewTitle.setText(getResources().getStringArray(R.array.connect_title)[Constants.getApplicationBuildType().getValue()]);
	        imageViewIcon = (ImageView) tabIndicator.findViewById(R.id.icon);
	        imageViewIcon.setImageResource(R.layout.tab_connect);
			
			spec.setIndicator(tabIndicator);
			spec.setContent(intent);
			tabHost.addTab(spec);
	        
	        
			
	        // apps tab
			
			// only add for VZB
			if (Constants.getApplicationBuildType() == BuildType.VERIZON_BUSINESS)
			{
		        intent = new Intent().setClass(this, ApplicationsActivity.class);
		        spec = tabHost.newTabSpec(TAB_APPS);
		        
		        tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_indicator, getTabWidget(), false);
		        textViewTitle = (TextView) tabIndicator.findViewById(R.id.title);
		        textViewTitle.setText("Apps");
		        imageViewIcon = (ImageView) tabIndicator.findViewById(R.id.icon);
		        imageViewIcon.setImageResource(R.layout.tab_applications);
				
				spec.setIndicator(tabIndicator);
				spec.setContent(intent);
				tabHost.addTab(spec);
			}
			
	        
	        // settings tab
	        intent = new Intent().setClass(this, PreferencesActivity.class);
	        spec = tabHost.newTabSpec(TAB_SETTINGS);
	        
	        tabIndicator = LayoutInflater.from(this).inflate(R.layout.tab_indicator, getTabWidget(), false);
	        textViewTitle = (TextView) tabIndicator.findViewById(R.id.title);
	        textViewTitle.setText(getResources().getStringArray(R.array.settings_title)[Constants.getApplicationBuildType().getValue()]);
	        imageViewIcon = (ImageView) tabIndicator.findViewById(R.id.icon);
	        imageViewIcon.setImageResource(R.layout.tab_settings);
			
			spec.setIndicator(tabIndicator);
			spec.setContent(intent);
			tabHost.addTab(spec);
	        
	        
	        // default to first tab
	        tabHost.setCurrentTab(0);
	        
	        
	        // for VZB builds only
			if (Constants.getApplicationBuildType() == BuildType.VERIZON_BUSINESS)
			{
				// request device admin access if it is required and not active
				if (Constants.getDeviceManagement().isDeviceAdministratorPromptRequired())
					requestDeviceAdministratorAccess();
				
				// enforce policy, only prompt for items required for the app to run
				else
					Constants.getDeviceManagement().enforcePolicy(PolicyFailureNotificationType.SilentExceptRequiredItems);
			}
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "onCreate", e);
    	}
    }
    
    
    private void requestDeviceAdministratorAccess()
    {
    	try
    	{
    		// clear notification
			Notifications.remove(Notifications.Type.REQUEST_DEVICE_ADMINISTRATION);
			
    		// request device administration rights from the user
    		ComponentName componentDeviceAdminReceiver = new ComponentName(this, DeviceAdminReceiver.class);
    		
    		Intent intentAction = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
    		intentAction.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentDeviceAdminReceiver);
    		intentAction.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,  "This application requires Administrator privileges to continue.");
    		
            this.startActivity(intentAction);
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "requestDeviceAdministratorAccess", e);
    	}
    }
        
    
    @Override
    protected void onNewIntent(Intent intent) 
    {
    	try
    	{
	    	super.onNewIntent(intent);
	    	
	    	Constants.getLog().add(TAG, Type.Verbose, "Intent received.");
	    	
	    	
	    	// check for required app notification intent
	    	if (intent.getAction() != null && intent.getAction().equalsIgnoreCase(ACTION_NOTIFY_APPS_REQUIRED))
	    	{
	    		// show application tab
	    		getTabHost().setCurrentTabByTag(TAB_APPS);
	    		
	    		// show toast message
	    		Toast.makeText(	Constants.applicationContext, 
	    						getResources().getStringArray(R.array.required_apps_must_be_installed)[Constants.getApplicationBuildType().getValue()],
	    						Toast.LENGTH_LONG).show();
	    		return;
	    	}

	    	else if (intent.getAction() != null && intent.getAction().equalsIgnoreCase(ACTION_VIEW_AVAILABLE_APPS))
	    	{
	    		// show application tab
	    		getTabHost().setCurrentTabByTag(TAB_APPS);
	    		
	    		return;
	    	}
	    	
	    	else if (intent.getAction() != null && intent.getAction().equalsIgnoreCase(ACTION_UPDATE_CONFIGURATION))
	    	{
	    		// update configuration in a new thread
				Thread t = new Thread()
				{
		            public void run() 
		            {
		            	Configuration.update(true);
		            }
		        };
		        t.start();
		        
		        // show settings tab
	    		getTabHost().setCurrentTabByTag(TAB_SETTINGS);
	    		
	    		// refresh
	    		((PreferencesActivity)this.getCurrentActivity()).refreshUI();
	    		
	    		return;
	    	}
	    	

	    	
	    	// refresh connect activity when requested
	    	
	    	if (this.getCurrentActivity() == null)
	    		return;

	    	// if refresh intent was not issued
	    	if (intent.getAction() == null || !intent.getAction().equalsIgnoreCase(Library.ACTION_REFRESH_UI))
	    		return;
	    		
	    	// if current tab is connect activity
	    	if (this.getCurrentActivity().getClass().getName().equalsIgnoreCase(ConnectActivity.class.getName()))
	    	{
	    		boolean isAuthenticated = false;
	    		
	    		try
	    		{
		    		// set flag from intent bundle
		    		Bundle b = intent.getExtras();
					
					if (b != null)
						isAuthenticated = Boolean.valueOf(b.get("isAuthenticated").toString());
	    		}
	    		catch (Exception e)
	    		{
	    			Constants.getLog().add(TAG, Type.Error, "onNewIntent", e);
	    		}
				
				// refresh
	    		((ConnectActivity)this.getCurrentActivity()).refreshUI(isAuthenticated);
	    	}
	    	
	    	// if current tab is applications activity
	    	else if (this.getCurrentActivity().getClass().getName().equalsIgnoreCase(ApplicationsActivity.class.getName()))
	    	{
	    		// refresh
	    		((ApplicationsActivity)this.getCurrentActivity()).refreshUI();
	    	}
	    	
	    	// if current tab is settings activity
	    	else if (this.getCurrentActivity().getClass().getName().equalsIgnoreCase(PreferencesActivity.class.getName()))
	    	{
	    		// refresh
	    		((PreferencesActivity)this.getCurrentActivity()).refreshUI();
	    	}
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "onNewIntent", e);
    	}
    }
    
    
    @Override
	protected void onResume() 
	{
		try
		{
			Constants.getLog().add(TAG, Type.Verbose, "Activity resumed.");
			
			// for VZB builds only
			if (Constants.getApplicationBuildType() == BuildType.VERIZON_BUSINESS)
			{
				// request device admin access if it required and not active
				if (Constants.getDeviceManagement().isDeviceAdministratorPromptRequired())
					requestDeviceAdministratorAccess();
				
				// enforce policy, only prompt for items required for the app to run
				else
					Constants.getDeviceManagement().enforcePolicy(PolicyFailureNotificationType.SilentExceptRequiredItems);
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "onResume", e);
		}
		
		super.onResume();
	}
}