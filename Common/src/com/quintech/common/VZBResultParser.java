package com.quintech.common;


import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler; 

import com.quintech.common.DeviceCommandIssuedInfo.DeviceCommands;
import com.quintech.common.ILog.Type;

public class VZBResultParser extends DefaultHandler 
{ 
	private static String TAG = "VZBResultParser";
	private StringBuffer buffer = null;
    private boolean buffering = false; 
    private static String currentContainerId = "0";
    
    public boolean isResultCodeValid = false;
    public EnrollmentPolicyResult enrollmentPolicyResult = null;
    public boolean isPasswordValidated = false;
    public boolean isPasswordChanged = false;
    public boolean hasHasVerizonNetworkAccount = false;
    public String verizonNetworkPassword = "";
    public String deviceUUID = "";
    public List<String> processedIssuedCommandGuids = null;
    public List<DeviceCommandIssuedInfo> issuedDeviceCommands = null;
    public List<DirectoryInfo> assignedDirectories = null;
    public List<ClientSettingInfo> clientSettings = null;
    public List<ShortcutInfo> shortcuts = null;
    public List<ApplicationAssignedInfo> assignedApplications = null;
    public List<SmartConnectAllowedConnectionTypes> smartConnectAllowedConnectionTypes = null;
    public List<Long> smartConnectRankedDirectoriesSysIDs = null;
    public List<ExchangeSettings> assignedExchangeServers = null;

    
    public VZBResultParser()
    {
    	// initialize
    	enrollmentPolicyResult = new EnrollmentPolicyResult();
    	processedIssuedCommandGuids = new ArrayList<String>();
    	issuedDeviceCommands = new ArrayList<DeviceCommandIssuedInfo>();
    	assignedDirectories = new ArrayList<DirectoryInfo>();
    	clientSettings = new ArrayList<ClientSettingInfo>();
    	shortcuts = new ArrayList<ShortcutInfo>();
    	assignedApplications = new ArrayList<ApplicationAssignedInfo>();
    	smartConnectAllowedConnectionTypes = new ArrayList<SmartConnectAllowedConnectionTypes>();
    	smartConnectRankedDirectoriesSysIDs = new ArrayList<Long>();
    	assignedExchangeServers = new ArrayList<ExchangeSettings>();
    }
    
    
    @Override
    public void startDocument() throws SAXException 
    {

    } 
    
    
    @Override
    public void endDocument() throws SAXException 
    {


    } 
  
    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException 
    {
    	// parse ROVA Portal XML
    	if (localName.equalsIgnoreCase("WebServiceLog"))
        {
    		// read attribute
    		if (atts.getValue("", "ResultCode").equals("0"))
    			isResultCodeValid = true;
        }   
    	else if (localName.equalsIgnoreCase("ExchangeServer"))
    	{
    		ExchangeSettings assignedExchangeServer = new ExchangeSettings();
    		String value = "";
       		// read attributes
    		try
    		{
    			assignedExchangeServer.Host = String.valueOf(atts.getValue("", "Host"));
    			assignedExchangeServer.Domain = String.valueOf(atts.getValue("", "Domain"));
    			assignedExchangeServer.UserAccountName = String.valueOf(atts.getValue("", "UserAccountName"));
    			assignedExchangeServer.UserEmailAddress = String.valueOf(atts.getValue("", "UserEmailAddress"));
       			assignedExchangeServer.AllowMove = String.valueOf(atts.getValue("", "AllowMove"));
       			assignedExchangeServer.EmailCertificate = String.valueOf(atts.getValue("", "EmailCertificate"));
       			if (assignedExchangeServer.EmailCertificate.equals("null"))
       				assignedExchangeServer.EmailCertificate = "";
       			
       			assignedExchangeServer.Port = String.valueOf(atts.getValue("", "Port"));
       			if (assignedExchangeServer.Port.equals("null"))
       				assignedExchangeServer.Port = "";
       			
       			value = atts.getValue("", "SyncInterval");       			
       			assignedExchangeServer.SyncInterval = Integer.valueOf((value==null || value.trim().length()==0)? "0": value);

       			value = atts.getValue("", "SyncPastDays");
       			assignedExchangeServer.SyncPeriod = Integer.valueOf((value==null || value.trim().length()==0)? "0": value);

       			value = atts.getValue("", "useSSL");
       			assignedExchangeServer.useSSL = Boolean.valueOf((value==null || value.trim().length()==0)? "false": value);

       			assignedExchangeServer.AccountDisplayName = String.valueOf(atts.getValue("", "AccountDisplayName"));
       		    			
    			assignedExchangeServers.add(assignedExchangeServer);
    		}
    		catch (Exception e)
    		{
    			AbstractConstants.log.add(TAG, Type.Error, "Error parsing issued exchange information.", e);
    		}
    	}
    	else if (localName.equalsIgnoreCase("EnrollmentPolicyResult"))
    	{
    		// read attribute
    		if (atts.getValue("", "Succeeded").equals("1"))
    			enrollmentPolicyResult.succeeded = true;
    		else
    			enrollmentPolicyResult.succeeded = false;
    	}
    	
    	else if (localName.equalsIgnoreCase("EnrollmentPolicyFailure"))
    	{
    		// start buffering
            buffer = new StringBuffer("");
            buffering = true;
    	}
    	
    	else if (localName.equalsIgnoreCase("EnrollmentPolicyMessage"))
    	{
    		// start buffering
            buffer = new StringBuffer("");
            buffering = true;
    	}
    	
    	else if (localName.equalsIgnoreCase("RovaUserInfo"))
    	{
    		// read attribute
    		if (atts.getValue("", "HasVerizonNetworkAccount").equals("1"))
    			hasHasVerizonNetworkAccount = true;
    		
    		verizonNetworkPassword = atts.getValue("", "VerizonNetworkPassword");
    	}
    	
    	else if (localName.equalsIgnoreCase("ValidatePasswordResult"))
    	{
    		// read attribute
    		if (atts.getValue("", "PasswordValidated").equals("1"))
    			isPasswordValidated = true;
    	}
    	else if (localName.equalsIgnoreCase("ChangePasswordResult"))
    	{
    		// read attribute
    		if (atts.getValue("", "PasswordChanged").equals("1"))
    			isPasswordChanged = true;
    	}
    	else if (localName.equalsIgnoreCase("ProcessedIssuedCommand"))
    	{
    		// read attribute
    		String commandGuid = atts.getValue("", "CommandGUID");
    		
    		// add result to list
    		if (commandGuid != null && commandGuid.length() != 0)
    		{
    			processedIssuedCommandGuids.add(commandGuid.trim());
    		}
    	}
    	else if (localName.equalsIgnoreCase("IssuedCommand"))
    	{
    		DeviceCommandIssuedInfo deviceCommand = new DeviceCommandIssuedInfo();
    		
    		// read attributes
    		try
    		{
    			
    			deviceCommand.commandGuid = atts.getValue("", "GUID");
    			deviceCommand.command = DeviceCommands.fromInteger(Integer.valueOf(atts.getValue("", "ID")));
    			deviceCommand.parameter = atts.getValue("", "Parameter");
    		}
    		catch (Exception e)
    		{
    			AbstractConstants.log.add(TAG, Type.Error, "Error parsing issued device command.", e);
    		}
    		
    		// add result to list
    		if (deviceCommand.commandGuid != null && deviceCommand.commandGuid.length() != 0)
    		{
    			issuedDeviceCommands.add(deviceCommand);
    		}
    	}
    	else if (localName.equalsIgnoreCase("ISP"))
    	{
    		DirectoryInfo directoryInfo = new DirectoryInfo();
    		
    		// read attributes
    		try
    		{
    			directoryInfo.portalSysID = Long.valueOf(atts.getValue("", "PortalSysID"));
    			directoryInfo.displayName = atts.getValue("", "Name");
    			directoryInfo.guid = atts.getValue("", "GUID");
    			directoryInfo.distinctHotspotDirectoryPath = atts.getValue("", "DistinctHotspotDirectoryPath");
    			directoryInfo.credentialSet = atts.getValue("", "CredentialSet");
    			directoryInfo.sequenceID = Integer.parseInt(atts.getValue("", "SequenceID"));
    			directoryInfo.userId = atts.getValue("", "UserID");
    			directoryInfo.password = atts.getValue("", "Pwd");

    			// strip any brackets off GUID value
    			if (directoryInfo.guid != null)
    				directoryInfo.guid = directoryInfo.guid.replace("{", "").replace("}", "");
    		}
    		catch (Exception e)
    		{
    			AbstractConstants.log.add(TAG, Type.Error, "Error parsing assigned directory.", e);
    		}
    		
    		// add result to list
    		if (directoryInfo.guid != null && directoryInfo.guid.length() != 0)
    		{
    			assignedDirectories.add(directoryInfo);
    		}
    	}
    	else if (localName.equalsIgnoreCase("ClientSettingSet"))
    	{
    		String ID = atts.getValue("TypeID");
    		if ((ID == null) || (ID.equals("")))
    			currentContainerId = "0";
    		else
    			currentContainerId = ID;
    	}
    	else if (localName.equalsIgnoreCase("ClientSetting"))
    	{
    		ClientSettingInfo csi = new ClientSettingInfo();
    		
    		// read attributes
    		try
    		{
    			csi.settingName = atts.getValue("", "ID");
    			csi.value = atts.getValue("", "Value");
    			csi.Containerid = currentContainerId;
    		}
    		catch (Exception e)
    		{
    			AbstractConstants.log.add(TAG, Type.Error, "Error parsing client setting.", e);
    		}
    		
    		// add result to list
    		if (csi.settingName != null && csi.settingName.length() != 0)
    		{
    			clientSettings.add(csi);
    		}
    	}
    	else if (localName.equalsIgnoreCase("SmartConnectSetting"))
    	{
    		// read attributes
    		try
    		{
    			// parse connection type rankings
    			if (atts.getValue("", "ID").equalsIgnoreCase("SCSET_ConnectionTypeRanking"))
    			{
    				String[] values = atts.getValue("", "Value").split(",");
    				
    				for (int i = 0; i < values.length; i++)
    					smartConnectAllowedConnectionTypes.add(new SmartConnectAllowedConnectionTypes(Integer.valueOf(values[i]), i));
    			}
    			
    			// parse connection type rankings
    			else if (atts.getValue("", "ID").equalsIgnoreCase("SCSET_ISPsRanking"))
    			{
    				String[] values = atts.getValue("", "Value").split(",");
    				
    				for (int i = 0; i < values.length; i++)
    					smartConnectRankedDirectoriesSysIDs.add(Long.parseLong(values[i]));
    			}
    		}
    		catch (Exception e)
    		{
    			AbstractConstants.log.add(TAG, Type.Error, "Error parsing Smart Connect setting.", e);
    		}
    	}
    	else if (localName.equalsIgnoreCase("WebClip"))
    	{
    		ShortcutInfo shortcutInfo = new ShortcutInfo();
    		
    		// read attributes
    		try
    		{
    			shortcutInfo.portalSysID = Integer.valueOf(atts.getValue("", "PortalSysID"));
    			shortcutInfo.label = atts.getValue("", "Label");
    			shortcutInfo.url = atts.getValue("", "URL");
    			shortcutInfo.iconExternalUrl = atts.getValue("", "IconUrl");
    		}
    		catch (Exception e)
    		{
    			AbstractConstants.log.add(TAG, Type.Error, "Error parsing web clip.", e);
    		}
    		
    		// add result to list
    		if (shortcutInfo.url != null && shortcutInfo.url.length() != 0)
    		{
    			shortcuts.add(shortcutInfo);
    		}
    	}
    	else if (localName.equalsIgnoreCase("SoftwareUpdate"))
    	{
    		ApplicationAssignedInfo app = new ApplicationAssignedInfo();
    		
    		// read attributes
    		try
    		{
    			app.portalSysID = Integer.valueOf(atts.getValue("", "PortalSysID"));
    			app.packageName = atts.getValue("", "PackageIdentifier");
    			app.packageDisplayName = atts.getValue("", "AppName");
    			app.version = atts.getValue("", "Version");
    			app.filePath = atts.getValue("", "FilePath");
    			
    			if (atts.getValue("", "Mandatory").equals("1"))
					app.mandatory = true;
    			else
    				app.mandatory = false;
    		}
    		catch (Exception e)
    		{
    			AbstractConstants.log.add(TAG, Type.Error, "Error parsing assigned application.", e);
    		}
    		
    		// add result to list
    		if (app.portalSysID > 0)
    		{
    			assignedApplications.add(app);
    		}
    	}
    	else if (localName.equalsIgnoreCase("IconBase64"))
    	{
    		// start buffering
            buffer = new StringBuffer("");
            buffering = true;
    	}
    	else if (localName.equalsIgnoreCase("RASProfile"))
    	{
    		// start buffering
            buffer = new StringBuffer("");
            buffering = true;
    	}
    	else if (localName.equalsIgnoreCase("DeviceUUID"))
    	{
    		// start buffering
            buffer = new StringBuffer("");
            buffering = true;
    	}
    } 
    
    
    @Override
    public void characters(char ch[], int start, int length) 
    {
        if (buffering) 
        {
        	// read into buffer
            buffer.append(ch, start, length);
        }
    } 
    
    
    @Override
    public void endElement(String namespaceURI, String localName, String qName)  throws SAXException 
    {
    	// Reset ContainerID if just finished 
    	if (localName.equalsIgnoreCase("ClientSettingSet"))
    		currentContainerId = "0";
    	
    	// save values from buffer
    	if (localName.equalsIgnoreCase("DeviceUUID"))
    	{
    		try
    		{
    			deviceUUID = buffer.toString().trim();	
    		}
    		catch (Exception e)
    		{
    			
    		}
    		finally
    		{
    			buffering = false;
    		}
    	}
    	
    	else if (localName.equalsIgnoreCase("EnrollmentPolicyFailure"))
    	{
    		try
    		{
    			// add item to list
    			enrollmentPolicyResult.failedPolicyItemIds.add(buffer.toString().trim());	
    		}
    		catch (Exception e)
    		{
    			
    		}
    		finally
    		{
    			buffering = false;
    		}
    	}
    	
    	else if (localName.equalsIgnoreCase("EnrollmentPolicyMessage"))
    	{
    		try
    		{
    			// save message, remove CDATA tags
				enrollmentPolicyResult.message = buffer.toString().trim().replace("<![CDATA[", "").replace("<![CDATA[", "]]>").trim();
    		}
    		catch (Exception e)
    		{
    			
    		}
    		finally
    		{
    			buffering = false;
    		}
    	}
    	
    	else if (localName.equalsIgnoreCase("RASProfile"))
    	{
    		try
    		{
    			if (assignedDirectories != null && assignedDirectories.size() > 0)
    			{
    				String cDataElement = buffer.toString().trim();	
    			
    				// save RASProfile data (remove CDATA tags) 
    				String rasProfile = cDataElement.replace("<![CDATA[", "").replace("<![CDATA[", "]]>").trim();
    				
    				// set flag in the most recent directory if there is a match
    				if (rasProfile.replace(" ", "").toLowerCase().contains("verizonwifidirectory=1"))
    					assignedDirectories.get(assignedDirectories.size() - 1).isVerizonDirectory = true;
    				else
    					assignedDirectories.get(assignedDirectories.size() - 1).isVerizonDirectory = false;
    			}
    		}
    		catch (Exception e)
    		{
    			
    		}
    		finally
    		{
    			buffering = false;
    		}
    	}
    }
}

