package com.quintech.connect;

import android.os.Message;

import com.enterproid.mdm.AbstractMDMCallback;
import com.enterproid.mdm.MDMServiceDisconnectedException;
import com.enterproid.mdm.MDMServiceHelper;
import com.quintech.common.AbstractData.Flags;
import com.quintech.common.AbstractData.Settings;
import com.quintech.common.ApplicationInstalledInfo;
import com.quintech.common.ILog.Type;

public class DivideHelper 
{
	public static String TAG = "DivideHelper";
	
	public static int attempts = 0;
	public static int autoRetries = 6;
	public static int retryDelaySeconds = 1;
	
	public static void resetAttempts()
	{
		attempts = 0;
	}
	
	public static void checkDivideStatus()
	{
		try
		{
			Constants.getLog().add(TAG, Type.Debug, "Checking Divide application status");
			
			// check if Divide is enabled via the portal and we have the exchange host
			if (!Constants.getData().getFlag(Flags.FLG_DivideEnabled))
			{
				Constants.getLog().add(TAG, Type.Debug, "Exiting, Divide is not enabled in portal");
				return;
			}
			
			
			if (Constants.getData().getSetting(Settings.SET_ExchangeDomain) == null ||
					Constants.getData().getSetting(Settings.SET_ExchangeDomain).length() == 0)
			{
				Constants.getLog().add(TAG, Type.Debug, "Exiting, Exchange domain has not been provided by portal");
				return;
			}
			
			if (Constants.getData().getSetting(Settings.SET_UserEmailAddress) == null ||
					Constants.getData().getSetting(Settings.SET_UserEmailAddress).length() == 0)
			{
				Constants.getLog().add(TAG, Type.Debug, "Exiting, user email address has not been provided by portal");
				return;
			}
			
			if (Constants.getData().getSetting(Settings.SET_ExchangeHost) == null ||
					Constants.getData().getSetting(Settings.SET_ExchangeHost).length() == 0)
			{
				Constants.getLog().add(TAG, Type.Debug, "Exiting, Exchange host has not been provided by portal");
				return;
			}
			
			
			MDMServiceHelper helper = new MDMServiceHelper(Constants.applicationContext, new MDMConnectionCallback());
			

			// check if Divide is installed
			if (!isDivideClientInstalled())
			{
				Constants.getLog().add(TAG, Type.Debug, "Divide application is not installed");
				return;
			}
			
			
			// check our flag if Divide client has been registered
			if (!Constants.getData().getFlag(Flags.FLG_DivideMdmClientRegistered))
			{
				// attempt to register MDM client
				try 
				{
					Constants.getLog().add(TAG, Type.Debug, "Attempting to register MDM client with Divide");
					attempts++;
	                helper.registerMDMClient(new MDMRegisterCallback());
	            } 
				catch (MDMServiceDisconnectedException e) 
				{
					Constants.getLog().add(TAG, Type.Debug, "Unable to register MDM client with Divide");
					
					// retry
	        		autoRetry();
	            }
				
				return;
			}
			
			
			// attempt to activate Divide account
			activateDivideAccount();			
		}
		catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "CheckDivideStatus", e);
    	}
	}
	
	public static boolean isDivideClientInstalled()
	{
		// check if Divide is installed
		boolean isInstalled = false;
		
		try
		{
			// attempt to check via Divide API
			try
			{
				MDMServiceHelper helper = new MDMServiceHelper(Constants.applicationContext, new MDMConnectionCallback());
				isInstalled = helper.isDivideInstalled();
			}
			catch (Exception e) { }
				
			// attempt to check by installed package name
			if (!isInstalled)
			{
				for (ApplicationInstalledInfo app : Constants.getLibrary().getInstalledApplications(false))
					if (app.packageName.toLowerCase().startsWith(("com.enterproid.")))
					{
						isInstalled = true;
						break;
					}
			}
			
			
			// if Divide app is not installed
			if (!isInstalled)
			{
				// clear registered flag
				Constants.getData().setFlag(Flags.FLG_DivideMdmClientRegistered, false, false);
				
				// reset attempts
				resetAttempts();
			}
			
			Constants.getLog().add(TAG, Type.Debug, "Divide application installed status: " + String.valueOf(isInstalled));
		}
		catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "IsDivideClientInstalled", e);
    	}
		
		return isInstalled;
	}
	
	public static void activateDivideAccount()
	{
		try 
		{
			// increment
			attempts++;
			
			MDMServiceHelper helper = new MDMServiceHelper(Constants.applicationContext, new MDMConnectionCallback());
			
			if (helper.isActivated(null))
			{
				Constants.getLog().add(TAG, Type.Debug, "Divide application is already activated");
				return;
			}
			
			
			// get user exchange settings
			String exchangeDomain = Constants.getData().getSetting(Settings.SET_ExchangeDomain);
			String exchangeUsername = Constants.getData().getSetting(Settings.SET_ExchangeUsername);
			String emailAddress = Constants.getData().getSetting(Settings.SET_UserEmailAddress);
			String password = Constants.getData().getSetting(Settings.SET_RovaPortalPassword);
			String exchangeHost = Constants.getData().getSetting(Settings.SET_ExchangeHost);
			

			// set default port to 443
			int port = 443;
			
			// attempt to set from settings
			try
			{
				port = Integer.valueOf(Constants.getData().getSetting(Settings.SET_ExchangePort));
			}
			catch (Exception e) { }
			
			
			helper.activateDivideClientExchange(emailAddress, password, exchangeHost, exchangeDomain, port, exchangeUsername, true, true, new MDMActivationCallback());
		} 
		catch (MDMServiceDisconnectedException e) 
		{
			Constants.getLog().add(TAG, Type.Debug, "Unable to activate Divide application, MDM service disconnected");
			
			// retry activation
			autoRetry();
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "RegisterMDMClient", e);
		}
	}
	
	public static void autoRetry()
	{
		try
		{	
			// auto-retry
			if (attempts <= autoRetries)
			{
				Constants.getLog().add(TAG, Type.Debug, "attempts: " + String.valueOf(attempts));
				Constants.getLog().add(TAG, Type.Debug, "Retrying after delay");
				
				// delay
				Thread.sleep(attempts * retryDelaySeconds * 1000);
				
				// retry
				checkDivideStatus();
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "AutoRetry", e);
		}
	}
	
	public static class MDMConnectionCallback extends AbstractMDMCallback 
	{
        @Override
        public boolean handleMessage(Message msg) {
            return false;
        }

        @Override
        public void onStart() {
        }

        @Override
        public void onSuccess() {
        }

        @Override
        public void onFail() {
        }
    }
	
	public static class MDMActivationCallback extends AbstractMDMCallback 
	{
        @Override
        public boolean handleMessage(Message msg) {
            return false;
        }

        @Override
        public void onStart() {
        }

        @Override
        public void onSuccess() {
        	Constants.getLog().add(TAG, Type.Info, "Successfuly activated Divide application.");
        }

        @Override
        public void onFail() 
        {
        	Constants.getLog().add(TAG, Type.Warn, "Failed to activate Divide application.");
        	
        	// retry activation
			autoRetry();
        }
    }
	
	public static class MDMRegisterCallback extends AbstractMDMCallback 
	{

        @Override
        public boolean handleMessage(Message arg0) {
            return false;
        }

        @Override
        public void onStart() 
        {
        	
        }

        @Override
        public void onSuccess() 
        {
			try
			{
				// set flag
				Constants.getData().setFlag(Flags.FLG_DivideMdmClientRegistered, true, false);
				
				// clear counter
				resetAttempts();
				
				Constants.getLog().add(TAG, Type.Debug, "Successfully registered MDM client with Divide application");
				
				// attempt to activate Divide user account
				activateDivideAccount();
			}
			catch (Exception e)
			{
				Constants.getLog().add(TAG, Type.Error, "MDMRegisterCallback - onSuccess", e);
			}
        }

        @Override
        public void onFail() 
        {
        	try
        	{
        		Constants.getLog().add(TAG, Type.Debug, "Failed to register or already registered MDM client with Divide application.");
        		
        		// retry
        		autoRetry();
        	}
        	catch (Exception e)
			{
				Constants.getLog().add(TAG, Type.Error, "MDMRegisterCallback - onFail", e);
			}
        }
        
    }
	
//	 public static class MDMGetStatusCallback extends AbstractMDMCallback 
//	 {
//	        @Override
//	        public boolean handleMessage(Message msg) {
//	            return false;
//	        }
//
//	        @Override
//	        public void onStart() {
//	        }
//
//	        @Override
//	        public void onSuccess() {
//	        }
//
//	        @Override
//	        public void onFail() {
//	        }
//	        
//    }
}
