package com.quintech.common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.quintech.common.AbstractData.Flags;
import com.quintech.common.AbstractData.Settings;
import com.quintech.common.AbstractLibrary.DeviceType;
import com.quintech.common.HotspotInfo;
import com.quintech.common.ILog.Type;


public class Credentials
{
	public enum PromptType
	{
		NetworkKey,
		NetworkCredentials,
		NetworkUsernameDecoration,
		VerizonDeviceCredentials,
		RovaPortalCredentials
	}
	
	private static String TAG = "Credentials";
	
	public PromptType promptType;
	public String promptTitle;
	public Runnable runnableCompleted;
	public Runnable runnableCanceled;
	public String userID = "";
	public String password = "";
	public String decoration = "";
	
	
	public Credentials(PromptType promptType, String promptTitle)
	{
		this.promptType = promptType;
		this.promptTitle = promptTitle;
	}	
	
    
	private void getVerizonDeviceCredentials(HotspotInfo hotspotInfo)
	{
		try
		{
			AbstractConstants.log.add(TAG, Type.Debug, "Generating Verizon Wireless credentials.");
			
			String mdn = AbstractConstants.library.getMSISDN();
			String currentDate = AbstractLibrary.getDateTimeGMT("MMddyyyy"); 
			String hashdata = "";
			
			AbstractConstants.log.add(TAG, Type.Debug, "MDN: " + mdn);
			AbstractConstants.log.add(TAG, Type.Debug, "Date string: " + currentDate);

			
			if (AbstractConstants.library.getDeviceType() == DeviceType.VERIZON_WIRELESS_4G)
			{
				// TODO: need to fetch the ICCID+IMSI to build the hashdata (4G Only)
				hashdata = AbstractConstants.library.getDeviceICCID();
				hashdata +=AbstractConstants.library.getDeviceIMSI().toLowerCase();
			}
			else
			{
				// note the data must be lowercase for the password hash
				// For 3G CDMA and 3G global devices: SHA1(mmddyyyy+MDN+ESN|MEID)
				// For 3G/4G combo smartphones:   SHA1(mmddyyyy+MDN+EUIMID)
				
				hashdata = AbstractConstants.library.getESN();
				AbstractConstants.log.add(TAG, Type.Debug, "MEID/ESN: " + hashdata);
			}
						
		    MessageDigest sha = null;
		    
			try 
			{
				sha = MessageDigest.getInstance("SHA-1");
			} 
			catch (NoSuchAlgorithmException e) 
			{
				AbstractConstants.log.add(TAG, Type.Warn, "Unable to get instance of SHA-1", e);
			}
			
		    byte[] plainpass = (currentDate + mdn + hashdata).getBytes();
	
		    sha.update(plainpass);
		    byte[] hashpass = sha.digest();
		    	 
			
			// set decoration
			
			// try value from hotspot
			if (hotspotInfo != null)
				decoration = hotspotInfo.decorationName;
			
			// try verizon wireless values
			if (decoration == null || decoration.equals(""))
			{
				if (AbstractConstants.library.getDeviceType() == DeviceType.VERIZON_WIRELESS_4G)
					decoration = AbstractConstants.data.getSetting(Settings.SET_VerizonWirelessUserDecoration4G);
				else
					decoration = AbstractConstants.data.getSetting(Settings.SET_VerizonWirelessUserDecoration3G);
			}
			
			// check flag for default value
			if (AbstractConstants.data.getFlag(Flags.FLG_AlwaysUseDefaultUserIdDecoration))
			{
				String defaultDecoration = AbstractConstants.data.getSetting(Settings.SET_DefaultUserIdDecoration);
				
				if (defaultDecoration != null && !defaultDecoration.equals(""))
					decoration = defaultDecoration;
			}
	
			// set value if none has been specified
			if (decoration == null || decoration.equals(""))
			{
				decoration = "%USERNAME%";
			}
			
			
			userID = decoration.replace("%USERNAME%", mdn);
		    password = AbstractLibrary.convertToHex(hashpass);
		    
		    AbstractConstants.log.add(TAG, Type.Debug, "User ID decoration: " + decoration);
		    AbstractConstants.log.add(TAG, Type.Debug, "User ID: " + userID);
		}
		catch (Exception e)
		{
			AbstractConstants.log.add(TAG, Type.Error, "getVerizonDeviceCredentials", e);
		}
	}
	
	
	public void getCredentials(HotspotInfo hotspotInfo)
	{
		try
		{
			// reset values
			userID = "";
			password = "";
			decoration = "";
			
			
			// check if credentials are stored in the database
			
			if (promptType == PromptType.NetworkCredentials)
			{	
				AbstractConstants.log.add(TAG, Type.Debug, "Checking for stored network credentials");
				
				
				// always use default credential if flag is set
				if (AbstractConstants.data.getFlag(Flags.FLG_AlwaysUseDefaultWisprCredential))
				{	
					userID = AbstractConstants.data.getSetting(Settings.SET_DefaultWisprUserID);
					password = AbstractConstants.data.getSetting(Settings.SET_DefaultWisprPassword);
					
					if (!userID.equals("") && !password.equals(""))
						AbstractConstants.log.add(TAG, Type.Debug, "Default WISPr credential was found");
					else
						AbstractConstants.log.add(TAG, Type.Debug, "No default WISPr credential was found");
				}	
				
				
				
				// check for directory credential (using CredentialSet)
				if (hotspotInfo != null && (userID.equals("") || password.equals("")))
				{
					DirectoryInfo directoryInfo = AbstractConstants.data.getDirectoryInfo(hotspotInfo.directoriesPortalSysID);
					
					if (directoryInfo != null)
					{
						userID = directoryInfo.userId;
						password = directoryInfo.password;
					}
					
					if (!userID.equals("") && !password.equals(""))
						AbstractConstants.log.add(TAG, Type.Debug, "Directory WISPr credential was found");
					else
						AbstractConstants.log.add(TAG, Type.Debug, "No directory WISPr credential was found");
				}
				
				
				// check for verizon network credential
				if ((userID.equals("") || password.equals("")) && 
						AbstractConstants.data.getFlag(Flags.FLG_HasVerizonNetworkAccount))
				{
					// ROVA Portal UserID will match the Verizon Network UserID for VZB
					userID = AbstractConstants.data.getSetting(Settings.SET_RovaPortalUserID);
					password = AbstractConstants.data.getSetting(Settings.SET_VerizonNetworkPassword);
					
					if (!userID.equals("") && !password.equals(""))
						AbstractConstants.log.add(TAG, Type.Debug, "Verizon Network credential was found");
					else
						AbstractConstants.log.add(TAG, Type.Debug, "No Verizon Network credential was found");
				}
				
				
				// check for default credential
				if (userID.equals("") || password.equals(""))
				{
					userID = AbstractConstants.data.getSetting(Settings.SET_DefaultWisprUserID);
					password = AbstractConstants.data.getSetting(Settings.SET_DefaultWisprPassword);
					
					if (!userID.equals("") && !password.equals(""))
						AbstractConstants.log.add(TAG, Type.Debug, "Default WISPr credential was found");
					else
						AbstractConstants.log.add(TAG, Type.Debug, "No default WISPr credential was found");
				}
			}	
			
			else if (promptType == PromptType.RovaPortalCredentials)
			{
				userID = AbstractConstants.data.getSetting(Settings.SET_RovaPortalUserID);
				password = AbstractConstants.data.getSetting(Settings.SET_RovaPortalPassword);
			}
			
			else if (promptType == PromptType.VerizonDeviceCredentials)
			{
				getVerizonDeviceCredentials(hotspotInfo);
			}
			
			
			
			// prompt user if not set
			if (userID.equals("") || password.equals(""))
			{
				// do not prompt if using generated device credentials
				if (!AbstractConstants.data.getFlag(Flags.FLG_UseVerizonWirelessCredentials))
				{
					// show prompt, this will fire completed callback when finished
					promptForCredentials();
				}
			}
			else
			{
				// fire callback
				if (runnableCompleted != null)
					runnableCompleted.run();
			}
		}
		catch (Exception e)
    	{
			AbstractConstants.log.add(TAG, Type.Error, "getCredentials", e);
    	}
	}
	

	public void promptForCredentials()
	{
		try
		{
			// show prompt
			AbstractConstants.library.promptForCredentials();
		}
		catch (Exception e)
		{
			AbstractConstants.log.add(TAG, Type.Error, "promptForCredentials", e);
		}
	}
}
