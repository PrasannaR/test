package com.quintech.rovacommon;

import java.util.HashMap;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;

import com.quintech.common.Configuration;
import com.quintech.common.VZBDirectoryServices;
import com.quintech.common.AbstractData.Flags;
import com.quintech.common.AbstractData.Settings;
import com.quintech.connect.Constants;
import com.quintech.connect.Data;
import com.quintech.rovacommon.PIMSettingsInfo.PIMAuthType;
import com.quintech.connect.ApplicationService;

public class rovashared {
	
	private Registrationlistner delegate;
	private FetchData db;
	private Context myContext;
	
	public rovashared(Context context)
	{		
		db = new FetchData();	
		Constants.getData().initialize(context);
		myContext = context;
	}
	
	public void startEmaasLogin()
	{
	  	new Thread(new Runnable() {
	  		  public void run() {
	  		    try {
	  				
	  		    	boolean bLoginResult = VZBDirectoryServices.isUserAuthenticated("", "");
					Intent return_intent = new Intent("com.cognizant.trubox.event");
					
					// Add Event Type
					return_intent.putExtra("Event","LOGIN_COMPLETE_EVENT");
					// Add login return flag
					return_intent.putExtra("Result", bLoginResult);
					
					LocalBroadcastManager.getInstance(myContext).sendBroadcast(return_intent);
					
	  				}
	  			catch(Exception e) {
	  				Intent return_intent = new Intent("com.cognizant.trubox.event");
	  			    // Add Event Type
					return_intent.putExtra("Event","LOGIN_COMPLETE_EVENT");
					// Add data
					return_intent.putExtra("Result", false);
					
					LocalBroadcastManager.getInstance(myContext).sendBroadcast(return_intent);
	  			}
	  		  }
	  	   	}).start();		
	}
	
	public void initiateToFetchAllSettingsInfo()
	{
	  	new Thread(new Runnable() {
	  		  public void run() {
	  		    try {
	  				int tries = 1;
	  		    	Configuration.update(false);
	  		    	
	  		    	while (Configuration.isInProgress())
	  		    	{
	  		    		Thread.sleep(5000);
	  		    		if (tries > 5)
	  		    			break;
	  		    		tries = tries +1;
	  		    	}
	  		    	
					Intent return_intent = new Intent("com.cognizant.trubox.event");
					// Add Event Type
					return_intent.putExtra("Event","SETTINGS_INFORMATION_FETCH_COMPLETE_EVENT");
					
					// Add Event Type
	  		    	if (Configuration.isInProgress())
	  		    	{
	  		    		// Add data
						return_intent.putExtra("Result", false);
	  		    	}
	  		    	else
	  		    	{
	  		    		// Add data
						return_intent.putExtra("Result", true);
	  		    	}

					LocalBroadcastManager.getInstance(myContext).sendBroadcast(return_intent);
					
	  				}
	  			catch(Exception e) {
	  				Intent return_intent = new Intent("com.cognizant.trubox.event");
	  				// Add Event Type
					return_intent.putExtra("Event","SETTINGS_INFORMATION_FETCH_COMPLETE_EVENT");
					// Add data
					return_intent.putExtra("Result", false);
					
					LocalBroadcastManager.getInstance(myContext).sendBroadcast(return_intent);
	  			}
	  		  }
	  	   	}).start();		
	}
	
    public boolean StartRegistration(final Map<String,String> Credentials, Registrationlistner callback)
    {
    	delegate = callback;
    	boolean breturn = false;
  		    try {
  				
 				

  				// send initialize intent to service
  		        Intent intentInitialize = new Intent(myContext, ApplicationService.class);
  		        intentInitialize.setAction(ApplicationService.Action.INITIALIZE_SERVICE);
  				myContext.startService(intentInitialize);
  				
  				
  				Intent intent =	 new Intent(myContext, ApplicationService.class);
  				intent.setAction("com.quintech.connect.REQUEST_REGISTRATION");
  				intent.putExtra("Username", Credentials.get("Username"));
  				intent.putExtra("Password", Credentials.get("Password"));
  				
  				LocalBroadcastManager.getInstance(myContext).registerReceiver(mMessageReceiver,
    				      new IntentFilter("com.quintech.connect.REG_COMPLETE"));
  				
  				myContext.startService(intent);
  				breturn = true;
  			}
  			catch(Exception e) {
  				delegate.OnFailed("error", 1);
  			}
    	
    	return breturn;
    }
    
 // handler for received Intents for the "com.quintech.connect.REG_COMPLETE" event 
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		// Extract data included in the Intent
    		boolean result = intent.getBooleanExtra("Result", false);

    		Constants.getData().setFlag(Flags.FLG_IsRegistered, result, false, Data.SafeSpace);

    		Bundle bundle = new Bundle();
    		Message msg = new Message();
    		bundle.putBoolean("Result", result);
    		msg.setData(bundle);

    		if (result)
    			delegate.onRegistered(msg);
    		else
    		{
    			delegate.OnFailed("Invalid Username or Password.", 1);
    		}	
    	}
    };
       
    
    public boolean validateCheckSum(String checkSum)
    {
    	boolean bValid = true;
    	
    	//call webservice 
    	//to be built waiting on webservice
    	
    	return bValid;
    }
    
    public RegistrationInfo GetRegistrationInformation()
    {
    	RegistrationInfo registrationInfo = new RegistrationInfo();
    	
    	registrationInfo.pimSettingInfo = GetPIMSettingInfo();
    	registrationInfo.deviceInfo = GetDeviceInfo();
    	registrationInfo.emailset = GetEmailSettingsInformation();
    	registrationInfo.secProfInfo = GetSecurityProfileSettings();
    	registrationInfo.vpnInfo = GetVPNSettingsWithCertificate();
    	registrationInfo.authenticationType = GetPIMAuthenticationTypeInformation();
    	registrationInfo.proxyInfo = getProxySettingsInformation();
    	return registrationInfo;
    }
       
	//    Returns the type of authentication. 
    //    The enum values are Certificate, Basic and None.	
    public PIMAuthType GetPIMAuthenticationTypeInformation ()
    {
    	PIMAuthType pimauthtype = PIMAuthType.None;
    	
    	String authtype = Constants.getData().getSetting(Settings.SET_PIMAuthType, Data.SafeSpace);
    	if (authtype == null || authtype.trim().equals(""))
    	{
    		
    	}
    	else
    	{
    		pimauthtype = PIMAuthType.fromInt(Integer.parseInt(authtype));
    	}
    	return pimauthtype;
    }
    
     
    //    this method which will return us if the device is registered or not. 
    //    This will be invoked during the authentication service to determine the screen to be showed.   
    public boolean isRegistered()
    {    	
    	return Constants.getData().getFlag(Flags.FLG_IsRegistered, Data.SafeSpace);
    }
       
    
    //This will capture information about the following data.
	//a) Device Wipe
	//b) Device Block
	//c) Device Unregister
    
    public SecurityProfileInfo GetSecurityProfileSettings()
    {
    	SecurityProfileInfo secprofile;
    	
    	secprofile = db.getSecurityProfileInfo();
    	
		return secprofile;   	
    }

    
    //
    //Returns a class with the corresponding email settings
    // and methods to retrieve these settings.
    //
    EmailSettings GetEmailSettingsInformation()
    {
    	EmailSettings settings = new EmailSettings();
    	settings = db.getEmailSettings();
    	return settings;
    }
    
    
    PIMSettingsInfo GetPIMSettingInfo()
    {
    	
    	PIMSettingsInfo PIMSettings = db.getPIMSettings();
    	
    	return PIMSettings;
    }
    

    VPNSettingsInfo GetVPNSettingsWithCertificate()
    {
    	VPNSettingsInfo vpnInfo = db.getVPNSettingsInfo();
    	
    	return vpnInfo;
    }
    
    DeviceInfo GetDeviceInfo()
    {
    	DeviceInfo devinfo = db.getDeviceInfo();
    	
    	return devinfo;
    }
 
    Map<String,String>  getProxySettingsInformation()
    {
    	String ProxyServerName = Constants.getData().getSetting(Settings.SET_ProxyServerName, Data.SafeSpace);
    	String ProxyServerPortNumber =Constants.getData().getSetting(Settings.SET_ProxyServerPortNumber, Data.SafeSpace);
    	Map <String,String> returndata = new HashMap();
    	
    	returndata.put("ProxyServerName", ProxyServerName);
    	returndata.put("ProxyServerPortNumber", ProxyServerPortNumber);
    	
    	
		return returndata;
    	
    }
}
