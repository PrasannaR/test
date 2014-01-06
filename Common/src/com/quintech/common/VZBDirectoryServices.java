package com.quintech.common;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.text.DecimalFormat;
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
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;


import com.quintech.common.AbstractData.Flags;
import com.quintech.common.AbstractData.Settings;
import com.quintech.common.ILog.Type;



public class VZBDirectoryServices 
{
	private static String TAG = "VZBDirectoryServices";
	

	public static boolean isUserAuthenticated(String userID, String password)
	{
		boolean result = false;
		
		try
		{
			// if no User ID is set, do not post
//			if (AbstractConstants.data.getSetting(Settings.SET_RovaPortalUserID) == null || 
//					AbstractConstants.data.getSetting(Settings.SET_RovaPortalUserID).equals(""))
//			{
//				AbstractConstants.log.add(TAG, Type.Warn, "Skipped posting SOAP envelope, no User ID is set");
//				return false;
//			}
			
			if (userID.length() == 0)
				userID = AbstractConstants.data.getSetting(Settings.SET_RovaPortalUserID);
			
			if (password.length() == 0)
				password = AbstractConstants.data.getSetting(Settings.SET_RovaPortalPassword);
			
			// post to portal 
			// create SOAP envelope
			StringBuilder sb = new StringBuilder();
			
			sb.append("<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">");
			sb.append("<soap12:Body>");
			sb.append("	<GetDeviceXML xmlns=\"http://www.rova.com/portal\">");
			sb.append("		<DeviceUUID></DeviceUUID>");
			sb.append("		<Flags>");
			sb.append("			<Flag ID=\"UserXML\" Value=\"0\" />");
			sb.append(" 	</Flags>");
			sb.append("		<Settings>");
			sb.append("			<Setting ID=\"NetworkUser\" Value=\"" + userID + "\" />");
			sb.append("			<Setting ID=\"NetworkPassword\" Value=\"" + password + "\" />");
			sb.append(" 	</Settings>");
			sb.append("		<LoadCDataXML>");
			sb.append("			<![CDATA[ ");
			sb.append("			]]>");
			sb.append("		</LoadCDataXML>");
			sb.append("	</GetDeviceXML>");
			sb.append("</soap12:Body>");
			sb.append("</soap12:Envelope>");


			// post and get result
			String resultXML = postSOAPEnvelope(sb.toString());
			
			// parse result
			VZBResultParser parser = getResult(resultXML);
			
			// process authentication result
			checkAuthenticationResult(parser);
			
			if (parser.isResultCodeValid && parser.isPasswordValidated)
				result = true;
			
			AbstractConstants.log.add(TAG, Type.Debug, "isUserAuthenticated result: " + String.valueOf(result));
		}
		catch (Exception e)
	    {
	    	AbstractConstants.log.add(TAG, Type.Error, "isUserAuthenticated", e);
	    }
		
		
		return result;
	}
	
	
	public static VZBResultParser update()
	{
		VZBResultParser parser = null;
		
		try
		{
			// if no User ID is set, do not post
			if (AbstractConstants.data.getSetting(Settings.SET_RovaPortalUserID) == null || 
					AbstractConstants.data.getSetting(Settings.SET_RovaPortalUserID).equals(""))
			{
				AbstractConstants.log.add(TAG, Type.Warn, "Skipped posting SOAP envelope, no User ID is set");
				return new VZBResultParser();
			}
			
			// create SOAP envelope
			StringBuilder sb = new StringBuilder();
			

			sb.append("<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">");
			sb.append("<soap12:Body>");
			sb.append("	<GetDeviceXML xmlns=\"http://www.rova.com/portal\">");

			// set application install UUID as device UUID
			// (there is no hardware UUID available for Android devices)
			sb.append("		<DeviceUUID>" + AbstractConstants.data.getApplicationInstallUUID() + "</DeviceUUID>");
			
			sb.append("		<Flags>");
			sb.append("			<Flag ID=\"UserXML\" Value=\"1\" />");
			sb.append("			<Flag ID=\"PasswordChange\" Value=\"0\" />");
			sb.append(" 	</Flags>");
			sb.append("		<Settings>");
			sb.append("			<Setting ID=\"NetworkUser\" Value=\"" + AbstractConstants.data.getSetting(Settings.SET_RovaPortalUserID) + "\" />");
			sb.append("			<Setting ID=\"NetworkPassword\" Value=\"" + AbstractConstants.data.getSetting(Settings.SET_RovaPortalPassword) + "\" />");
			sb.append("			<Setting ID=\"NetworkPasswordNew\" Value=\"\" />");
			
			sb.append("			<Setting ID=\"PlatformID\" Value=\"" + String.valueOf(AbstractConstants.library.getPlatformId()) + "\" />");
			sb.append("			<Setting ID=\"OSVersion\" Value=\"" + AbstractLibrary.formatForSOAP(AbstractConstants.library.getOSVersion()) + "\" />");
			sb.append("			<Setting ID=\"OSVersionSDK\" Value=\"" + AbstractLibrary.formatForSOAP(AbstractConstants.library.getSdkVersion()) + "\" />");
			sb.append("			<Setting ID=\"PhoneNumber\" Value=\"" + AbstractLibrary.formatForSOAP(AbstractConstants.library.getMSISDN()) + "\" />");
			sb.append("			<Setting ID=\"ESN\" Value=\"" + AbstractLibrary.formatForSOAP(AbstractConstants.library.getESN()) + "\" />");
			sb.append("			<Setting ID=\"GoogleClientRegistrationID\" Value=\"" + AbstractLibrary.formatForSOAP(AbstractConstants.data.getSetting(Settings.SET_GCMRegistrationID)) + "\" />");
			sb.append("			<Setting ID=\"WiFiMACAddress\" Value=\"" + AbstractLibrary.formatForSOAP(AbstractConstants.library.getDeviceMacAddress()) + "\" />");
			sb.append("			<Setting ID=\"Manufacturer\" Value=\"" + AbstractLibrary.formatForSOAP(AbstractConstants.library.getDeviceManufacturer()) + "\" />");
			sb.append("			<Setting ID=\"ModelName\" Value=\"" + AbstractLibrary.formatForSOAP(AbstractConstants.library.getDeviceModelNumber()) + "\" />");
			sb.append("			<Setting ID=\"ClientVersion\" Value=\"" + AbstractLibrary.formatForSOAP(AbstractConstants.library.getClientVersionName()) + "\" />");
			sb.append("			<Setting ID=\"ICCID\" Value=\"" + AbstractLibrary.formatForSOAP(AbstractConstants.library.getDeviceICCID()) + "\" />");
			sb.append("			<Setting ID=\"CurrentMCC\" Value=\"" + AbstractLibrary.formatForSOAP(AbstractConstants.library.getCurrentMobileCountryCode()) + "\" />");
			sb.append("			<Setting ID=\"CarrierNetworkHome\" Value=\"" + AbstractLibrary.formatForSOAP(AbstractConstants.library.getHomeCarrierNetwork()) + "\" />");
			sb.append("			<Setting ID=\"CellularTechnology\" Value=\"" + AbstractLibrary.formatForSOAP(AbstractConstants.library.getCellularTechnology()) + "\" />");
			sb.append("			<Setting ID=\"BatteryLevelPercent\" Value=\"" + AbstractLibrary.formatForSOAP(String.valueOf(AbstractConstants.library.getBatteryLevelPercentage())) + "\" />");
			sb.append("			<Setting ID=\"SamsungEnterpriseSdkVersion\" Value=\"" + AbstractLibrary.formatForSOAP(AbstractConstants.library.getSamsungEnterpriseSdkVersion()) + "\" />");			
			
			if (AbstractConstants.deviceManagement.isInternalStorageEncrypted() != null)
				sb.append("			<Setting ID=\"InternalStorageEncrypted\" Value=\"" + AbstractLibrary.formatForSOAP(AbstractConstants.deviceManagement.isInternalStorageEncrypted()) + "\" />");
			
			if (AbstractConstants.deviceManagement.isExternalStorageEncrypted() != null)
				sb.append("			<Setting ID=\"ExternalStorageEncrypted\" Value=\"" + AbstractLibrary.formatForSOAP(AbstractConstants.deviceManagement.isExternalStorageEncrypted()) + "\" />");
			
			
			if (AbstractConstants.library.isJailBroken())
				sb.append("			<Setting ID=\"JailBroken\" Value=\"true\" />");
			else
				sb.append("			<Setting ID=\"JailBroken\" Value=\"false\" />");
			
			if (AbstractConstants.library.isRoaming())
				sb.append("			<Setting ID=\"IsRoaming\" Value=\"true\" />");
			else
				sb.append("			<Setting ID=\"IsRoaming\" Value=\"false\" />");
			
			// format decimal values
			DecimalFormat decimalFormat = new DecimalFormat("#.##");
			sb.append("			<Setting ID=\"DeviceCapacityGB\" Value=\"" + decimalFormat.format(AbstractConstants.library.getDeviceCapacityGB(0, 0)) + "\" />");
			sb.append("			<Setting ID=\"AvailableDeviceCapacityGB\" Value=\"" + decimalFormat.format(AbstractConstants.library.getDeviceCapacityGB(0, 1)) + "\" />");
			sb.append("			<Setting ID=\"DeviceCapacityExternalGB\" Value=\"" + decimalFormat.format(AbstractConstants.library.getDeviceCapacityGB(1, 0)) + "\" />");
			sb.append("			<Setting ID=\"AvailableDeviceCapacityExternalGB\" Value=\"" + decimalFormat.format(AbstractConstants.library.getDeviceCapacityGB(1, 1)) + "\" />");
			
			// set current location
			String latitude = "";
			String longitude = "";
			
			
			
			// never report location if disabled by the portal, regardless of user preference
			if (!AbstractConstants.data.getSetting(Settings.SET_ReportDeviceLocation).equals("0"))
			{
				// report location if required, or user has enabled
				if (AbstractConstants.data.getSetting(Settings.SET_ReportDeviceLocation).equals("1") ||
						!AbstractConstants.data.getFlag(Flags.FLG_UserDisabledLocationReporting))
				{	
					// get current location
					Location location = AbstractConstants.library.getCurrentLocation();
					
					if (location != null)
					{		
						latitude = String.valueOf(Double.valueOf(location.latitude));
						longitude = String.valueOf(Double.valueOf(location.longitude));
					}
				}
			}
			
			
			sb.append("			<Setting ID=\"Latitude\" Value=\"" + latitude + "\" />");
			sb.append("			<Setting ID=\"Longitude\" Value=\"" + longitude + "\" />");
			
			
			// set IsRegisteredForMDM flag
			if (AbstractConstants.deviceManagement.isDeviceAdministratorActive())
				sb.append("			<Setting ID=\"IsRegisteredForMDM\" Value=\"1\" />");
			else
				sb.append("			<Setting ID=\"IsRegisteredForMDM\" Value=\"0\" />");
			
			sb.append(" 	</Settings>");
			sb.append("		<LoadCDataXML>");
			sb.append("			<![CDATA[ ");
			sb.append("			<xml>");
			
			
			

			
			// send completed device commands
			List<DeviceCommandIssuedInfo> listDeviceCommandsIssued = AbstractConstants.data.getDeviceCommandsIssued(true, false);
			
			sb.append("				<IssuedDeviceCommands>");
			
			for (DeviceCommandIssuedInfo command : listDeviceCommandsIssued)
			{
				try
				{
					String succeeded = (command.succeeded) ? "1" : "0";
					String errorMessage = (command.errorMessage != null) ? AbstractLibrary.formatForSOAP(command.errorMessage) : "";
					sb.append("						<IssuedCommand GUID=\"" + command.commandGuid + "\" Succeeded=\"" + succeeded + "\" ErrorMessage=\"" + errorMessage + "\" />");
				}
				catch (Exception e)
				{
					AbstractConstants.log.add(TAG, Type.Warn, "Problem parsing issued device command for reporting.  Command skipped.", e);
				}
			}
			
			sb.append("				</IssuedDeviceCommands>");
			
			
			// send installed applications, do not report system packages
			if (AbstractConstants.data.getSetting(Settings.SET_ReportInstalledApplications).equals("1"))
			{
				List<ApplicationInstalledInfo> listApplicationsInstalled = AbstractConstants.library.getInstalledApplications(false);
				
				sb.append("				<InstalledApplications>");
				
				for (ApplicationInstalledInfo app : listApplicationsInstalled)
				{
					String displayName = (app.packageDisplayName != null) ? AbstractLibrary.formatForSOAP(app.packageDisplayName) : "";
					String packageName = (app.packageName != null) ? AbstractLibrary.formatForSOAP(app.packageName) : "";
					String version = (app.version != null) ? AbstractLibrary.formatForSOAP(app.version) : "";
					String packageSizeBytes = String.valueOf(app.packageSizeBytes);
					String dynamicSizeBytes = String.valueOf(app.dynamicSizeBytes);
					
					sb.append("						<InstalledApplication DisplayName=\"" + displayName + "\" PackageName=\"" + packageName + "\" Version=\"" + version + "\" PackageSizeBytes=\"" + packageSizeBytes + "\" DynamicSizeBytes=\"" + dynamicSizeBytes + "\" />");
				}
				
				sb.append("				</InstalledApplications>");
			}
			
			
			sb.append("			</xml>");
			sb.append("			]]>");
			sb.append("		</LoadCDataXML>");
			sb.append("	</GetDeviceXML>");
			sb.append("</soap12:Body>");
			sb.append("</soap12:Envelope>");


			// post and get result
			String resultXML = postSOAPEnvelope(sb.toString());
			
			// parse result
			parser = getResult(resultXML);
			
			// process authentication result
			checkAuthenticationResult(parser);
		}
		catch (Exception e)
	    {
	    	AbstractConstants.log.add(TAG, Type.Error, "update", e);
	    }
		
		
		return parser;
	}
	
	
	private static void checkAuthenticationResult(VZBResultParser parser)
	{
		try
		{
			// if post succeeded, but password failed to validate
			if (parser.isResultCodeValid && !parser.isPasswordValidated)
			{
				AbstractConstants.log.add(TAG, Type.Debug, "ROVA Portal credentials failed to authenticate");
				AbstractConstants.data.setSetting(Settings.SET_RovaPortalUserID, "", false);
				AbstractConstants.data.setSetting(Settings.SET_RovaPortalPassword, "", false);
			}
			
			// clear any previous notifications
			else if (parser.isResultCodeValid && parser.isPasswordValidated)
			{
				AbstractConstants.log.add(TAG, Type.Debug, "ROVA Portal credential authentication succeeded");
				AbstractConstants.library.clearFailedPortalCredentialNotification();
			}
			else
				AbstractConstants.log.add(TAG, Type.Debug, "ROVA Portal credentials failed to authenticate - failed result code");
		}
		catch (Exception e)
	    {
	    	AbstractConstants.log.add(TAG, Type.Error, "getResult", e);
	    }
	}
	
	
	private static VZBResultParser getResult(String xmlResult)
	{
		VZBResultParser parser = null;
		
		try
		{
			parser = new VZBResultParser();

			// parse result
	    	SAXParserFactory spf = SAXParserFactory.newInstance();
	    	SAXParser sp = spf.newSAXParser();
	    	XMLReader xr = sp.getXMLReader();
	    	
	    	xr.setContentHandler(parser);
	    	xr.parse(new InputSource(new StringReader(xmlResult)));	   
		}
		catch (Exception e)
	    {
	    	AbstractConstants.log.add(TAG, Type.Error, "getResult", e);
	    }
		
		return parser;
	}
	
	
	private static String postSOAPEnvelope(String xmlSOAP)
	{
		String resultXML = "";
		String soapAction = "http://www.rova.com/portal/GetDeviceXML";
		
		
		try
		{
			List<String> listServers = null;
			
			
			// get server list
			if (listServers == null || listServers.size() < 1)
			{
				listServers = AbstractConstants.getRovaPortalServerList();
			}
			
			// get webservice path
			String webservicePath = AbstractConstants.data.getSetting(Settings.SET_RovaPortalWebservicePath);
			
			for (String server : listServers)
			{
				AbstractConstants.log.add(TAG, Type.Debug, "Posting action " + soapAction + " to " + server + webservicePath);
				AbstractConstants.log.add(TAG, Type.Info, "Sending request to " + server);
				AbstractConstants.log.add(TAG, Type.Verbose, "SOAP Envelope:\n " + xmlSOAP + "\n");
				
				HttpPost httppost = new HttpPost(server + webservicePath);  
				StringEntity se = null;
				HttpResponse httpResponse = null;
				
		
				// set post values
				se = new StringEntity(xmlSOAP, HTTP.UTF_8);
				se.setContentType("text/xml"); 
	
			    httppost.setHeader("Content-Type","application/soap+xml;charset=UTF-8");
			    httppost.addHeader("SOAPAction", soapAction);
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
