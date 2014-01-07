package com.quintech.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
//import org.apache.http.util.EntityUtils;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.quintech.common.AbstractData.Flags;
import com.quintech.common.AbstractData.Settings;
import com.quintech.common.ClientSettingInfo;
import com.quintech.common.FailedAuthenticationInfo;
import com.quintech.common.ILog.Type;
import com.quintech.common.SessionInfo;
import com.quintech.common.VZWClientSettingParser;
import com.quintech.common.VZWDirectoryVenueParser;
import com.quintech.common.VZWReturnCodeParser;
import com.quintech.common.VenueInfo;


public class VZWDirectoryServices 
{
	private static String TAG = "VZWDirectoryServices";
	public static String DIRECTORY_GUID = "300597C6-1C42-47F8-BBE3-EB5400AB46F5";
	
	public static class ReturnCode
	{
		public final static int SUCCESS = 0;
		public final static int FAILURE_UNKNOWN = 1;
		public final static int SCHEMA_VALIDATION_FAILED = 1000;
		public final static int INVALID_INPUT_PARAMETER = 1002;
		public final static int AUTHENTICATION_UNSUCCESSFUL = 1005;
		public final static int TEMPORARY_FAILURE = 1011;
		public final static int AP_SSID_DOES_NOT_EXIST = 1701;
		public final static int APID_DOES_NOT_EXIST = 1702;
		public final static int SSID_DOES_NOT_EXIST = 13001;
	}
	
	
	private static String getReturnCode(String xmlResult, String nodeName)
	{
		String returnCode = "";
		
		try
		{
			VZWReturnCodeParser parser = new VZWReturnCodeParser(nodeName);

			// parse result
	    	SAXParserFactory spf = SAXParserFactory.newInstance();
	    	SAXParser sp = spf.newSAXParser();
	    	XMLReader xr = sp.getXMLReader();
	    	
	    	xr.setContentHandler(parser);
	    	xr.parse(new InputSource(new StringReader(xmlResult)));	    
			
	    	// set return value
			returnCode = parser.returnCode;
		}
		catch (Exception e)
	    {
	    	AbstractConstants.log.add(TAG, Type.Error, "getReturnCode", e);
	    }
		
		return returnCode;
	}
	

	public static List<VenueInfo> getVenuesByLocation(String countryCode, String zipCode, String state, String city, int radius)
	{
		List<VenueInfo> venueList = null;
		
		try
		{
			// hotspot searching parameters
			//postalCode = "10002-3133"; 
			
			int MaxListingsToReturn = 10000;
			String radiusNode = "";
			String cityNode = "";
			String stateNode = "";
			String countryNode = "";
			String zipcodeNode = "";
			String latLongNode = "";
			
	
				
			if (city != null && city.trim().length() > 0)
				cityNode = "<smar:city>" + city + "</smar:city>";
			else
				cityNode = "";
			
			if (state != null && state.trim().length() > 0)
				stateNode = "<smar:state>" + state + "</smar:state>";
			else
				stateNode = "";
			
			if (countryCode != null && countryCode.trim().length() > 0)
				countryNode = "<smar:country>" + countryCode + "</smar:country>";
			else
				countryNode = "";
			
			if (zipCode != null && zipCode.trim().length() > 0)
				zipcodeNode = "<smar:zipcode>" + zipCode + "</smar:zipcode>";
			else
				zipcodeNode = "";
			
			if (radius > 0)
				radiusNode = "<smar:radius>" + radius + "</smar:radius>";
			else
				radiusNode = "";
			
			
			// DISABLED
			latLongNode = "";
	//		"				<latLong>" +
	//		"					<com:latitude>40.7328<com:/latitude>" + 
	//		"					<com:longitude>-73.989<com:/longitude>" + 
	//		"				</latLong>" + 
			
		    
		    String VenueRequestXML = 
		    			"<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:hds=\"http://www.verizon.com/HDS/\" xmlns:smar=\"http://www.verizon.com/hds/xsd/smartphone\" xmlns:com=\"http://www.verizon.com/hds/xsd/common\">" +
		    			"	<s:Body>" +
		    			"		<hds:EGetVenueByLocationRequest>" + 
		    			
		    						getRequestorInfoNode() +
		    			
		    						getRequestorAuthInfoNode() + 
		    			
		    			"			<smar:venueSearchInfo>" + 
		    							cityNode +
		    							stateNode + 
		    							countryNode +
		    							zipcodeNode +
		    							latLongNode +
		    							radiusNode + 
		    			"			</smar:venueSearchInfo>" + 
		    			
		    			"			<smar:numberOfListingToReturn>" + MaxListingsToReturn + "</smar:numberOfListingToReturn>" + 
		    			"		</hds:EGetVenueByLocationRequest>" + 
		    			"	</s:Body>" + 
		    			"</s:Envelope>";
		    
		 
		    // get result
		    String xmlResult = postSOAPEnvelope(VenueRequestXML, "http://www.verizon.com/HDS/getVenueByLocation");
		    VZWDirectoryVenueParser directoryVenueParser = new VZWDirectoryVenueParser();
		    
		  
	    	// parse result
	    	SAXParserFactory spf = SAXParserFactory.newInstance();
	    	SAXParser sp = spf.newSAXParser();
	    	XMLReader xr = sp.getXMLReader();
	    	
	    	xr.setContentHandler(directoryVenueParser);
	    	xr.parse(new InputSource(new StringReader(xmlResult)));	   
	    	
	    	// set return value
	    	venueList = directoryVenueParser.directoryVenueList;
		}
		catch (Exception e)
		{
			AbstractConstants.log.add(TAG, Type.Error, "getVenuesByLocation", e);
		}
	   
	    // return result list
	    return venueList;
	}
	
	
	public static VZWHotspotParser getGlobalSSIDs(float currentVersion)
	{
		VZWHotspotParser parser = new VZWHotspotParser();
		
		try
		{
			String requestXML = 
		    			"<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:hds=\"http://www.verizon.com/HDS/\" xmlns:smar=\"http://www.verizon.com/hds/xsd/smartphone\" xmlns:com=\"http://www.verizon.com/hds/xsd/common\">" +
		    			"	<s:Body>" +
		    			"		<hds:EGetGlobalSSIDsRequest>" + 
		    						
		    						getRequestorInfoNode() +
		    			
		    						getRequestorAuthInfoNode() +
	
		    			"			<smar:version>" + String.valueOf(currentVersion) + "</smar:version>" + 
		    			"		</hds:EGetGlobalSSIDsRequest>" + 
		    			"	</s:Body>" + 
		    			"</s:Envelope>";
		    
		 
		    // get result
		    String xmlResult = postSOAPEnvelope(requestXML, "http://www.verizon.com/HDS/getGlobalSSIDs");
		    

	    	// parse result
	    	SAXParserFactory spf = SAXParserFactory.newInstance();
	    	SAXParser sp = spf.newSAXParser();
	    	XMLReader xr = sp.getXMLReader();
	    	
	    	xr.setContentHandler(parser);
	    	xr.parse(new InputSource(new StringReader(xmlResult)));	    
	    }
	    catch (Exception e)
	    {
	    	AbstractConstants.log.add(TAG, Type.Error, "getGlobalSSIDs", e);
	    	
	    	// set return value to null 
	    	parser = null;
	    }
	    
	   
	    // return result parser, which contains list
	    return parser;
	}
	
	
	/*	
	public static void getDirectory(float currentVersion)
	{
		
		// Note this function is not in use
		// This function provides functionality to download an entire directory of hotspot data in a compressed format
		 
		// This function has not been fully implemented or tested
		
		// NOTE THAT AS OF 2011-09-27 THE requestorInfo and requestorAuthInfo NODES HAVE DIFFERENT NAMESPACES THAN THE OTHER APIs
		
		try
	    {
			
			String requestXML = 
		    			"<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:hds=\"http://www.verizon.com/HDS/\" xmlns:smar=\"http://www.verizon.com/hds/xsd/smartphone\" xmlns:com=\"http://www.verizon.com/hds/xsd/common\">" +
		    			"	<s:Body>" +
		    			"		<hds:EGetDirectoryRequest>" + 
		    						
		    						getRequestorInfoNode() +
		    			
		    						getRequestorAuthInfoNode() +
		    						
						"			<smar:directoryVersion>" + String.valueOf(currentVersion) + "</smar:directoryVersion>" + 
						"			<smar:optimized>" + String.valueOf("true") + "</smar:optimized>" + 
		    			"		</hds:EGetDirectoryRequest>" + 
		    			"	</s:Body>" + 
		    			"</s:Envelope>";
		    
		 
		    // get result
		    String xmlResult = postSOAPEnvelope(requestXML, "http://www.verizon.com/HDS/getDirectory");
		    

	    	// parse result
		    VZWClientSettingParser parser = new VZWClientSettingParser();
//	    	SAXParserFactory spf = SAXParserFactory.newInstance();
//	    	SAXParser sp = spf.newSAXParser();
//	    	XMLReader xr = sp.getXMLReader();
//	    	
//	    	xr.setContentHandler(parser);
//	    	xr.parse(new InputSource(new StringReader(xmlResult)));	    
//	    	
//	    	// set return value
//	    	listClientSettingInfo = parser.clientSettingInfoList;
	    }
	    catch (Exception e)
	    {
	    	AbstractConstants.log.add(TAG, Type.Error, "getDirectory", e);
	    }
	}
*/
	
	
	public static List<ClientSettingInfo> getDefaultClientSettings()
	{
		List<ClientSettingInfo> listClientSettingInfo = null;
		
		
		try
	    {
			String requestXML = 
		    			"<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:hds=\"http://www.verizon.com/HDS/\" xmlns:smar=\"http://www.verizon.com/hds/xsd/smartphone\" xmlns:com=\"http://www.verizon.com/hds/xsd/common\">" +
		    			"	<s:Body>" +
		    			"		<hds:EGetCustomerSettingsRequest>" + 
		    						
		    						getRequestorInfoNode() +
		    			
		    						getRequestorAuthInfoNode() +
		    						
		    			"		</hds:EGetCustomerSettingsRequest>" + 
		    			"	</s:Body>" + 
		    			"</s:Envelope>";
		    

			// get result
		    String xmlResult = postSOAPEnvelope(requestXML, "http://www.verizon.com/HDS/getCustomerSettings");
		    

	    	// parse result
		    VZWClientSettingParser parser = new VZWClientSettingParser();
	    	SAXParserFactory spf = SAXParserFactory.newInstance();
	    	SAXParser sp = spf.newSAXParser();
	    	XMLReader xr = sp.getXMLReader();
	    	
	    	xr.setContentHandler(parser);
	    	xr.parse(new InputSource(new StringReader(xmlResult)));	    
	    	
	    	// set return value
	    	listClientSettingInfo = parser.clientSettingInfoList;
	    }
	    catch (Exception e)
	    {
	    	AbstractConstants.log.add(TAG, Type.Error, "getDefaultClientSettings", e);
	    }
	    
	   
	    // return list
	    return listClientSettingInfo;
	}


	public static boolean postFailedAuthentications(List<FailedAuthenticationInfo> listFailedAuthentications)
	{
		boolean resultFlag = false;
		String requestXML = "";
		StringBuilder errorLogListNode = new StringBuilder();
		
		try
	    {
			// check input
			if (listFailedAuthentications == null || listFailedAuthentications.size() < 1)
			{
				AbstractConstants.log.add(TAG, Type.Debug, "No failed authentications present, skipping post to HDS.");
				return false;
			}
			
			
			// build error log node list
			errorLogListNode.append("<smar:errorLogList>");
			
			for (FailedAuthenticationInfo fa : listFailedAuthentications)
			{
				errorLogListNode.append("<smar:errorLog>");
				errorLogListNode.append("	<smar:ssid>" + AbstractLibrary.formatForSOAP(fa.ssid) + "</smar:ssid>");
				errorLogListNode.append("	<smar:macAddress>" + AbstractLibrary.formatForSOAP(fa.macAddress) + "</smar:macAddress>");
				errorLogListNode.append("	<smar:wispAuthLog><![CDATA[" + fa.wisprXmlLog + "]]></smar:wispAuthLog>");
				errorLogListNode.append("	<smar:latLong>");
				errorLogListNode.append("		<com:latitude>" + AbstractLibrary.formatForSOAP(fa.latitude) + "</com:latitude>");
				errorLogListNode.append("		<com:longitude>" + AbstractLibrary.formatForSOAP(fa.longitude) + "</com:longitude>");
				errorLogListNode.append("	</smar:latLong>");
				errorLogListNode.append("	<smar:errorCodeList>");
				errorLogListNode.append("		<smar:errorCode>" + AbstractLibrary.formatForSOAP(fa.resultCode) + "</smar:errorCode>");
				errorLogListNode.append("	</smar:errorCodeList>");
				errorLogListNode.append("</smar:errorLog>");
			}
			
			errorLogListNode.append("</smar:errorLogList>");
			
			
			requestXML = 
		    			"<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:hds=\"http://www.verizon.com/HDS/\" xmlns:smar=\"http://www.verizon.com/hds/xsd/smartphone\" xmlns:com=\"http://www.verizon.com/hds/xsd/common\">" +
		    			"	<s:Body>" +
		    			"		<hds:EReportErrorLogRequest>" + 
		    						
		    						getRequestorInfoNode() +
		    			
		    						getRequestorAuthInfoNode() +
	
		    						errorLogListNode.toString() + 
		    						
		    			"		</hds:EReportErrorLogRequest>" + 
		    			"	</s:Body>" + 
		    			"</s:Envelope>";
		    
		 
		    // get result
		    String xmlResult = postSOAPEnvelope(requestXML, "http://www.verizon.com/HDS/reportErrorLog");
		    
		    AbstractConstants.log.add(TAG, Type.Debug, "Posted failed authentications to HDS.");

	    	// parse result     
		    String resultCode = getReturnCode(xmlResult, "returnCode");
		    AbstractConstants.log.add(TAG, Type.Debug, "Failed authentications HDS post returned " + resultCode);

	    	int returnCode =  Integer.valueOf(resultCode);
	    		
    		switch (returnCode)
    		{
    			// return true for these cases, do not retry posting data
    			case ReturnCode.SUCCESS:
    			case ReturnCode.SSID_DOES_NOT_EXIST:
    			case ReturnCode.SCHEMA_VALIDATION_FAILED:
    			case ReturnCode.INVALID_INPUT_PARAMETER:
    			case ReturnCode.FAILURE_UNKNOWN:
    				resultFlag = true;
    				break;
    			
    			// return false for temporary failures, retry posting data
    			case ReturnCode.AUTHENTICATION_UNSUCCESSFUL:
    			case ReturnCode.TEMPORARY_FAILURE:
    			default:
    				resultFlag = false;
    				break;
    		}   
	    }
	    catch (Exception e)
	    {
	    	AbstractConstants.log.add(TAG, Type.Error, "postFailedAuthentications", e);
	    }
		
		
		return resultFlag;
	}

	
	private static String formatTimeZone(String value)
	{
		String returnValue = "";
		
		// timezone value must match the pattern for HDS:
		// pattern: '[+-][0-9]{4}' 
		
		try
		{
			if (value == null || value.equals(""))
			{
				returnValue = "-0000";
			}
			else
			{
				// cast value for comparison
				long timeZone = Long.valueOf(value);
				String prefix = "";
				
				if (timeZone <= 0)
				{
					prefix = "-";
				}
				else
				{
					prefix = "+";
				}
				
				
				// four digits are required, add leading zeroes as necessary
				
				// reset to absolute value
				timeZone = Math.abs(timeZone);
				
				if (timeZone < 10)
				{
					returnValue = "000" + String.valueOf(timeZone);
				}
				else if (timeZone < 100)
				{
					returnValue = "00" + String.valueOf(timeZone);
				}
				else if (timeZone < 1000)
				{
					returnValue = "0" + String.valueOf(timeZone);
				}
				else
				{
					returnValue = String.valueOf(timeZone);
				}
				
				
				// add prefix
				returnValue = prefix + returnValue;
			}
		}
		catch (Exception e)
	    {
			AbstractConstants.log.add(TAG, Type.Error, "formatTimeZone", e);
	    	
	    	// set default value
	    	returnValue = "-0000";
	    }
		
		return returnValue;
	}
	
	
	public static boolean postSessions(List<SessionInfo> listSessions)
	{
		boolean resultFlag = false;
		String requestXML = "";
		StringBuilder sessionsListNode = new StringBuilder();
		
		try
	    {
			// check input
			if (listSessions == null || listSessions.size() < 1)
			{
				AbstractConstants.log.add(TAG, Type.Debug, "No sessions present, skipping post to HDS.");
				return false;
			}
			
			
			// build error log node list
			sessionsListNode.append("<smar:sessionStatusList>");
			
			for (SessionInfo si : listSessions)
			{
				sessionsListNode.append("<smar:sessionStatus>");
				
				sessionsListNode.append("		<smar:macSSIDPair>");
				sessionsListNode.append("			<com:macAddress>" + AbstractLibrary.formatForSOAP(si.bssid) + "</com:macAddress>");
				sessionsListNode.append("			<com:ssid>" + AbstractLibrary.formatForSOAP(si.ssid) + "</com:ssid>");
				sessionsListNode.append("		</smar:macSSIDPair>");
				
				sessionsListNode.append("	<smar:startTime>" + AbstractLibrary.formatDateTimeStringForSOAP(si.startTime) + "</smar:startTime>");
				sessionsListNode.append("	<smar:endTime>" + AbstractLibrary.formatDateTimeStringForSOAP(si.endTime) + "</smar:endTime>");
				sessionsListNode.append("	<smar:loginElapsedTime>" + AbstractLibrary.formatForSOAP(si.getElapsedTimeInSeconds()) + "</smar:loginElapsedTime>");
				
				
				// bytes used
				if (!AbstractConstants.data.getFlag(Flags.FLG_EnableSessionBytesReporting))
				{
					AbstractConstants.log.add(TAG, Type.Info, "Bytes Used reporting is disabled, no data has been sent.");
					sessionsListNode.append("	<smar:octetsDownloaded>" + AbstractLibrary.formatForSOAP(0) + "</smar:octetsDownloaded>");
					sessionsListNode.append("	<smar:octetsUploaded>" + AbstractLibrary.formatForSOAP(0) + "</smar:octetsUploaded>");
				}
				else
				{
					sessionsListNode.append("	<smar:octetsDownloaded>" + AbstractLibrary.formatForSOAP(si.octetsDownloaded) + "</smar:octetsDownloaded>");
					sessionsListNode.append("	<smar:octetsUploaded>" + AbstractLibrary.formatForSOAP(si.octetsUploaded) + "</smar:octetsUploaded>");
				}
				
				sessionsListNode.append("	<smar:deviceType>" + AbstractLibrary.formatForSOAP(si.deviceType) + "</smar:deviceType>");
				sessionsListNode.append("	<smar:deviceModelNumber>" + AbstractLibrary.formatForSOAP(si.deviceModelNumber) + "</smar:deviceModelNumber>");
				sessionsListNode.append("	<smar:osType>" + AbstractLibrary.formatForSOAP(si.osType) + "</smar:osType>");
				sessionsListNode.append("	<smar:osVersion>" + AbstractLibrary.formatForSOAP(si.osVersion) + "</smar:osVersion>");
				sessionsListNode.append("	<smar:wifiConnectionClientVersion>" + AbstractLibrary.formatForSOAP(si.clientVersion) + "</smar:wifiConnectionClientVersion>");
				
				
				// do not include optional field if no value is present
				if (si.wisprLoginURL != null && !si.wisprLoginURL.equals(""))
					sessionsListNode.append("	<smar:loginURL>" + AbstractLibrary.formatForSOAP(si.wisprLoginURL) + "</smar:loginURL>");
				
				if (si.latitude != null && !si.latitude.equals("") && 
						si.longitude != null && !si.longitude.equals(""))
				{
					sessionsListNode.append("	<smar:latLong>");
					sessionsListNode.append("		<com:latitude>" + AbstractLibrary.formatForSOAP(si.latitude) + "</com:latitude>");
					sessionsListNode.append("		<com:longitude>" + AbstractLibrary.formatForSOAP(si.longitude) + "</com:longitude>");
					sessionsListNode.append("	</smar:latLong>");
				}
				
				sessionsListNode.append("	<smar:timeZone>" + AbstractLibrary.formatForSOAP(formatTimeZone(si.timeZone)) + "</smar:timeZone>");
				
				
				// do not include optional fields if no values are present
				
				if (si.wisprLocationName != null && !si.wisprLocationName.equals(""))
					sessionsListNode.append("	<smar:locationName>" + AbstractLibrary.formatForSOAP(si.wisprLocationName) + "</smar:locationName>");
				
				if (si.wisprLocationID != null && !si.wisprLocationID.equals(""))
					sessionsListNode.append("	<smar:locationId>" + AbstractLibrary.formatForSOAP(si.wisprLocationID) + "</smar:locationId>");
				
				if (si.wisprbwUserGroup != null && !si.wisprbwUserGroup.equals(""))
					sessionsListNode.append("	<smar:bwUserGroup>" + AbstractLibrary.formatForSOAP(si.wisprbwUserGroup) + "</smar:bwUserGroup>");
				
				
				sessionsListNode.append("	<smar:jailBroken>" + AbstractLibrary.formatForSOAP(si.isJailBroken) + "</smar:jailBroken>");
				sessionsListNode.append("	<smar:avgDataRxRate>" + AbstractLibrary.formatForSOAP(String.format("%.2f", si.getAverageDownloadRate())) + "</smar:avgDataRxRate>");
				sessionsListNode.append("	<smar:avgDataTxRate>" + AbstractLibrary.formatForSOAP(String.format("%.2f", si.getAverageUploadRate())) + "</smar:avgDataTxRate>");
				
				// these are fields that we are not reporting on and are not required by the API
				//sessionsListNode.append("	<smar:errorCodesList></smar:errorCodesList>");
				//sessionsListNode.append("	<smar:venueType></smar:venueType>");
				//sessionsListNode.append("	<smar:peakDataTxRate></smar:peakDataTxRate>");
				//sessionsListNode.append("	<smar:peakDataRxRate></smar:peakDataRxRate>");

				sessionsListNode.append("</smar:sessionStatus>");
			}
			
			sessionsListNode.append("</smar:sessionStatusList>");
			
			
			requestXML = 
		    			"<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:hds=\"http://www.verizon.com/HDS/\" xmlns:smar=\"http://www.verizon.com/hds/xsd/smartphone\" xmlns:com=\"http://www.verizon.com/hds/xsd/common\">" +
		    			"	<s:Body>" +
		    			"		<hds:EReportSessionStatusRequest>" + 
		    						
		    						getRequestorInfoNode() +
		    			
		    						getRequestorAuthInfoNode() +
	
		    						sessionsListNode.toString() + 
		    						
		    			"		</hds:EReportSessionStatusRequest>" + 
		    			"	</s:Body>" + 
		    			"</s:Envelope>";
		    
		 
		    // get result
		    String xmlResult = postSOAPEnvelope(requestXML, "http://www.verizon.com/HDS/reportSessionStatus");
		    
		    AbstractConstants.log.add(TAG, Type.Debug, "Posted sessions to HDS.");

	    	// parse result     
		    String resultCode = getReturnCode(xmlResult, "returnCode");
		    
		    AbstractConstants.log.add(TAG, Type.Debug, "Report Sessions HDS post returned " + resultCode);
		    
    		int returnCode =  Integer.valueOf(resultCode);

    		switch (returnCode)
    		{
    			// return true for these cases, do not retry posting data
    			case ReturnCode.SUCCESS:
    			case ReturnCode.SCHEMA_VALIDATION_FAILED:
    			case ReturnCode.INVALID_INPUT_PARAMETER:
    			case ReturnCode.AP_SSID_DOES_NOT_EXIST:
    			case ReturnCode.SSID_DOES_NOT_EXIST:
    			case ReturnCode.FAILURE_UNKNOWN:
    				resultFlag = true;
    				break;
    			
    			// return false for temporary failures, retry posting data
    			case ReturnCode.AUTHENTICATION_UNSUCCESSFUL:
    			case ReturnCode.TEMPORARY_FAILURE:
    			default:
    				resultFlag = false;
    				break;
    		}    
	    }
	    catch (Exception e)
	    {
	    	AbstractConstants.log.add(TAG, Type.Error, "postSessions", e);
	    }
		
		
		return resultFlag;
	}
	
	
	private static String getRequestorInfoNode()
	{
		String node = "";
		
		try
		{
			String requesterClass = "Smartphone";
		    String requesterSubClass = AbstractConstants.library.getDeviceManufacturer();
		    String requestorVersion = AbstractConstants.library.getOSVersion();
		    
		    node = 	"	<smar:requestorInfo>" + 
	    			"		<com:requesterClass>" + AbstractLibrary.formatForSOAP(requesterClass) + "</com:requesterClass>" + 
	    			"		<com:requesterSubClass>" + AbstractLibrary.formatForSOAP(requesterSubClass) + "</com:requesterSubClass>" +
	    			"		<com:requestorVersion>" + AbstractLibrary.formatForSOAP(requestorVersion) + "</com:requestorVersion>" +
	    			"	</smar:requestorInfo>";
		    
		}
		catch (Exception e)
		{
			AbstractConstants.log.add(TAG, Type.Error, "getRequestorInfoNode", e);
		}
	    
		return node;
	}
	
	
	private static String getRequestorAuthInfoNode()
	{
		String node = "";
		
		try
		{
			String macAddress = formatMacAddressForAuthentication(AbstractConstants.library.getDeviceMacAddress());
			String hashedPassword = getHashedPasswordForAuthentication(macAddress);
			
		    node = 	"	<smar:requestorAuthInfo>" +
					"		<com:macAddress>" + macAddress + "</com:macAddress>" + 
					"		<com:password>" + hashedPassword + "</com:password>" + 
					"	</smar:requestorAuthInfo>";
		}
		catch (Exception e)
		{
			AbstractConstants.log.add(TAG, Type.Error, "getRequestorAuthInfoNode", e);
		}
		
		return node;
	}
	
	
	private static String formatMacAddressForAuthentication(String macAddress)
	{
		String formattedValue = "";
		
		try
		{
			// convert to lowercase and take out colons
		    formattedValue = macAddress.toLowerCase().replace(":", "");
		}
		catch (Exception e)
		{
			AbstractConstants.log.add(TAG, Type.Error, "formatMacAddressForAuthentication", e);
		}
		
		return formattedValue;
	}
	
	
	private static String getHashedPasswordForAuthentication(String macAddress)
	{
		String password = "";
		StringBuilder reverseMacAddress = new StringBuilder();
		
		try
		{
			// reverse macAddress string
			for (int i = macAddress.toCharArray().length - 1; i >=0; i--)
				reverseMacAddress.append(macAddress.toCharArray()[i]);
			
			// create password
		    password = SimpleSHA1.getSHA1(macAddress + reverseMacAddress.toString() + "VZWHotspotDirectoryServices-5557");
		}
		catch (Exception e)
		{
			AbstractConstants.log.add(TAG, Type.Error, "getHashedPasswordForAuthentication", e);
		}
		
		return password;
	}
	
	
	private static String postSOAPEnvelope(String xmlSOAP, String SOAPAction)
	{
		String resultXML = "";
		
		try
		{
			List<String> listServers = null;
			
			// check for debug server URL
			if (AbstractConstants.data.getFlag(Flags.FLG_AlwaysUseDefaultVZWServer))
			{
				String debugServerURL = AbstractConstants.data.getSetting(Settings.SET_DefaultVZWServer);
				
				if (debugServerURL != null && !debugServerURL.equals(""))
				{
					listServers = new ArrayList<String>();
					listServers.add(debugServerURL);
				}
			}
			
			// use regular server list
			if (listServers == null || listServers.size() < 1)
			{
				listServers = AbstractConstants.getVZWServerList();
			}
			
			for (String server : listServers)
			{
				AbstractConstants.log.add(TAG, Type.Debug, "Posting action " + SOAPAction + " to " + server);
				AbstractConstants.log.add(TAG, Type.Info, "Sending request to " + server);
				AbstractConstants.log.add(TAG, Type.Verbose, "SOAP Envelope:\n " + xmlSOAP + "\n");
				
				HttpPost httppost = new HttpPost(server);  
				StringEntity se = null;
				HttpResponse httpResponse = null;
				
		
				// set post values
				se = new StringEntity(xmlSOAP, HTTP.UTF_8);
				se.setContentType("text/xml"); 
	
			    httppost.setHeader("Content-Type","application/soap+xml;charset=UTF-8");
			    httppost.addHeader("SOAPAction", SOAPAction);
			    httppost.setEntity(se);  
			      
			    HttpClient httpClient = new DefaultHttpClient();
			    
			    // set timeout
			    HttpParams params = httpClient.getParams();
			    HttpConnectionParams.setConnectionTimeout(params, 10000);
			    HttpConnectionParams.setSoTimeout(params, 10000);

			    
			    try
			    {
			    	// attempt post
			    	httpResponse = httpClient.execute(httppost);
			    	AbstractConstants.log.add(TAG, Type.Debug, "HTTP Response Code: " + String.valueOf(httpResponse.getStatusLine().getStatusCode()));
			    	
			    	// get result SOAP envelope
			    	resultXML = getResponseBody(httpResponse);
			    	
			    	// if successful, break from loop
			    	AbstractConstants.log.add(TAG, Type.Debug, "Post succeeded");
			    	break;
			    }
			    catch (Exception e) 
			    {
			    	// try posting to secondary server ?
			    	AbstractConstants.log.add(TAG, Type.Error, "Error posting SOAP envelope", e);
			    }
			}
		}
		catch (Exception e)
		{
			AbstractConstants.log.add(TAG, Type.Error, "postSOAPEnvelope", e);
		}
	   
		return resultXML;
	}
	
	
	private static String getResponseBody(HttpResponse response)
	{
		String response_text = null;
		HttpEntity entity = null;

		try 
		{
			entity = response.getEntity();
			response_text = _getResponseBody(entity);
		} 
		catch (IOException e) 
		{
			if (entity != null) 
			{
				try 
				{
					entity.consumeContent();
//					EntityUtils.consume(entity);
				} 
				catch (IOException e1)  { }
			}
		}
		catch (Exception e)
		{
			AbstractConstants.log.add(TAG, Type.Error, "getResponseBody", e);
		}

		return response_text;
	}

	
	private static String _getResponseBody(final HttpEntity entity) throws IOException
	{
		if (entity == null) 
			throw new IllegalArgumentException("HTTP entity may not be null");

		InputStream instream = entity.getContent();

		if (instream == null)
			return "";

		if (entity.getContentLength() > Integer.MAX_VALUE) 
			throw new IllegalArgumentException("HTTP entity too large to be buffered in memory"); 

		String charset = getContentCharSet(entity);

		if (charset == null) 
			charset = HTTP.DEFAULT_CONTENT_CHARSET;


		Reader reader = new InputStreamReader(instream, charset);

		StringBuilder buffer = new StringBuilder();

		try 
		{
			char[] tmp = new char[1024];

			int l;

			while ((l = reader.read(tmp)) != -1) 
				buffer.append(tmp, 0, l);

		} 
		finally
		{
			reader.close();
		}

		
		return buffer.toString();
	}

	
	private static String getContentCharSet(final HttpEntity entity)
	{
		String charset = null;
		
		try
		{
			if (entity == null)
				throw new IllegalArgumentException("HTTP entity may not be null");
	
			if (entity.getContentType() != null) 
			{
				HeaderElement values[] = entity.getContentType().getElements();
	
				if (values.length > 0) 
				{
					NameValuePair param = values[0].getParameterByName("charset");
	
					if (param != null) 
						charset = param.getValue();
				}
			}
		}
		catch (Exception e)
		{
			AbstractConstants.log.add(TAG, Type.Error, "getContentCharSet", e);
		}

		return charset;
	}
}
