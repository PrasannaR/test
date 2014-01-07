package com.quintech.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.SocketException;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.*;

import javax.net.ssl.SSLSocket;
import javax.security.cert.X509Certificate;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;  
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;  
import org.apache.http.client.methods.HttpGet;  
import org.apache.http.client.methods.HttpPost;  
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory; 
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.client.DefaultHttpClient;  
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;  
import org.apache.http.params.BasicHttpParams;   
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;  
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.quintech.common.AbstractConstants.BuildType;
import com.quintech.common.AbstractData.Flags;
import com.quintech.common.AbstractData.Settings;
import com.quintech.common.HotspotInfo;
import com.quintech.common.ILog.Type;
import com.quintech.common.WISPrXmlParser;


 
public class WISPrConnect  
{
	public final int WISPR_MESSAGE_TYPE_INITIAL = 100;    
	public final int WISPR_MESSAGE_TYPE_PROXY_NOTIFICATION = 110;    
	public final int WISPR_MESSAGE_TYPE_AUTH_NOTIFICATION = 120;    
	public final int WISPR_MESSAGE_TYPE_LOGOFF_NOTIFICATION = 130;    
	public final int WISPR_MESSAGE_TYPE_RESPONSE_AUTH_POLL = 140;    
	public final int WISPR_MESSAGE_TYPE_RESPONSE_ABORT_LOGIN = 150;    
	
	public final int WISPR_RESPONSE_CODE_NO_ERROR = 0;   	
	public final int WISPR_RESPONSE_CODE_LOGIN_SUCCEEDED = 50;   
	public final int WISPR_RESPONSE_CODE_LOGIN_FAILED = 100;    
	public final int WISPR_RESPONSE_CODE_RADIUS_ERROR = 102;    
	public final int WISPR_RESPONSE_CODE_NETWORK_ADMIN_ERROR = 105;    
	public final int WISPR_RESPONSE_CODE_LOGOFF_SUCCEEDED = 150;    
	public final int WISPR_RESPONSE_CODE_LOGING_ABORTED = 151;   
	public final int WISPR_RESPONSE_CODE_PROXY_DETECTION = 200;  
	public final int WISPR_RESPONSE_CODE_AUTH_PENDING = 201;   
	public final int WISPR_RESPONSE_CODE_INTERNAL_ERROR = 255;  
	public final int WISPR_NOT_PRESENT = 1024; 
	public final int ALREADY_CONNECTED = 1025;  
	public final int WEBPAGE_NOTFOUND = 1026;
	public final int CREDENTIALS_MISSING = 1027;
	public final int WEBPAGE_NOTTRUSTED = 1028;
	
	// unused
	//private static final int ALT_DNS_NAME = 2;

	private static final String TAG = "WISPrConnect";
	private static HttpParams defaultHttpParams = new BasicHttpParams(); 
	private static int MAX_TRIES = 3;	
	private HotspotInfo hotspotInfo = null;
	
	public static final String WISPR_TAG = "WISPAccessGatewayParam";
	
	// properties that will be used to track session data once connection is complete
	public String wisprLoginURL;
	public String wisprLogoffURL;
	public String wisprbwUserGroup;
	public String wisprLocationName;
	public String wisprLocationID;
	public StringBuilder wisprXmlLog;
	
	
	
	public WISPrConnect(HotspotInfo hotspotInfo)
	{
		try
		{
			wisprXmlLog = new StringBuilder();
			
			this.hotspotInfo = hotspotInfo;
			
			if (AbstractConstants.data.getFlag(Flags.FLG_UseVerizonWirelessCredentials) == true)
			{
			    AbstractConstants.credentials = new Credentials(Credentials.PromptType.VerizonDeviceCredentials, "");
			}
			else
			{
				AbstractConstants.credentials = new Credentials(Credentials.PromptType.NetworkCredentials, "Enter Boingo Credentials");
			}
		}
		catch (Exception e)
    	{
			AbstractConstants.log.add(TAG, Type.Error, "WISPrConnect", e);
    	}
	}
	
	
	public static String getProbeURL()
	{
		String probeUrl = "";
		
		try
		{
			if (AbstractConstants.getApplicationBuildType() == BuildType.VERIZON_WIRELESS)
			{
				probeUrl = AbstractConstants.data.getSetting(Settings.SET_VerizonWirelessProbeUrl);
			}
			
			else if (AbstractConstants.getApplicationBuildType() == BuildType.VERIZON_BUSINESS)
			{
				probeUrl = AbstractConstants.data.getSetting(Settings.SET_VerizonBusinessProbeUrl);
			}
		}
		catch (Exception e)
    	{
			AbstractConstants.log.add(TAG, Type.Error, "getProbeURL", e);
    	}
		
		// set default return value if none was set
		if (probeUrl == null || probeUrl.equals(""))
			probeUrl = "http://www.verizon.com";
	
		
		return probeUrl;
	}
	
	
	private boolean verifyTrust(String secureURL)
	{
		boolean bPassed = false;
		
		try 
		{
			AbstractConstants.log.add(TAG, Type.Info, "verifyTrust : " + secureURL);
			
			if (AbstractConstants.data.getFlag(Flags.FLG_VerifyWebsiteTrust) == false)
			{
				return true;
			}
				
	
			URI data = new URI(secureURL);
			int port = data.getPort();
			
			//Check to see if a port was specified on the URL if not use the default 443
			if (port < 0)
				port = 443;
			
			String hostname = data.getHost(); 
		    String Scheme = data.getScheme();
		    
			if ((Scheme != null) && (Scheme.equalsIgnoreCase("https")))
			{		
				HostnameVerifier hostnameVerifier;
				
				hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER; 
				
				
				String loginDomain = "";
				
				// set loginURL
				if ((hotspotInfo != null) && (hotspotInfo.loginURL.length()!=0))
					loginDomain = hotspotInfo.loginURL;
				else
				{
					if (AbstractConstants.getApplicationBuildType() == BuildType.VERIZON_WIRELESS)
						loginDomain = ".*\\.vzwwifi\\.com";
				}
				
				Pattern p = Pattern.compile(loginDomain); 
			    Matcher m = p.matcher("");
			    m.reset(hostname); 
			    
				if (m.matches() == true)
				{
				    SchemeRegistry registry = new SchemeRegistry(); 
				    SSLSocketFactory socketFactory = SSLSocketFactory.getSocketFactory(); 
				    socketFactory.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier); 
				    registry.register(new Scheme("https", socketFactory, 443)); 
				 
				    // Set verifier      
				    HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier); 	 			
				    
					javax.net.ssl.SSLSocketFactory factory = HttpsURLConnection.getDefaultSSLSocketFactory();
					SSLSocket socket = (SSLSocket)factory.createSocket(hostname, port);
										
					// Connect to the server
					socket.startHandshake();
					
				    bPassed = hostnameVerifier.verify(hostname, socket.getSession());
				        			        
					// Retrieve the server's certificate chain
				    X509Certificate[] serverCerts = socket.getSession().getPeerCertificateChain();
		
				    for (X509Certificate Cert : serverCerts)
				    {
				    	String CAName = Cert.getIssuerDN().getName();				    
				    	AbstractConstants.log.add(TAG, Type.Debug, "verifyTrust SSL CA : " + CAName);
				    }
					// Close the socket
					socket.close();
				}
				else
				{
					AbstractConstants.log.add(TAG, Type.Info, "verifyTrust Pattern Match: Failed");
					AbstractConstants.log.add(TAG, Type.Debug, "verifyTrust Pattern: " + loginDomain);
					AbstractConstants.log.add(TAG, Type.Debug, "verifyTrust Hostname: " + hostname);
				}
			}	
			else
			{
				AbstractConstants.log.add(TAG, Type.Info, "verifyTrust Scheme: Missing https");
			}
		} 
		catch (SSLException e) 
		{

			AbstractConstants.log.add(TAG, Type.Info, "verifyTrust SSL: ", e);
		} 
		catch (ClientProtocolException e) 
		{
			AbstractConstants.log.add(TAG, Type.Info, "verifyTrust Client Protocol: ", e);
		} 
		catch (IOException e) 
		{
			AbstractConstants.log.add(TAG, Type.Info, "verifyTrust IO: ", e);
		} 
		catch (Exception e) 
		{	
			AbstractConstants.log.add(TAG, Type.Info, "verifyTrust Exception: ", e);
		} 
			
		return bPassed; 
	}
	
	
	public static HttpClient getAllCertHttpClient() 
	{ 
	    try 
	    { 
	        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType()); 
	        trustStore.load(null, null); 
	 
	        SSLSocketFactory sf = new MySSLSocketFactory(trustStore); 
	        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER); 
	 
	        HttpParams params = new BasicHttpParams(); 
	        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1); 
	        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8); 
	 
	        SchemeRegistry registry = new SchemeRegistry(); 
	        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80)); 
	        registry.register(new Scheme("https", sf, 443)); 
	 
	        ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry); 
	 
	        return new DefaultHttpClient(ccm, params); 
	    } 
	    catch (Exception e) 
	    { 
	        return new DefaultHttpClient(); 
	    } 
	} 

	
	private String postData(String url, List <NameValuePair> nameValuePairs) 
	{    
		HttpClient httpclient = null;
		HttpPost httppost = null;
		HttpResponse response = null; 
		String page = "";
        String line = "";
        String NL;
        StringBuffer sb;   

		try 
		{
			NL = System.getProperty("line.separator");
			sb = new StringBuffer("");  
			
			// Create a new HttpClient and Post Header 
			// If verify website trust is false use the one that overrides the cert checks
			// otherwise use the default
			if (AbstractConstants.data.getFlag(Flags.FLG_VerifyWebsiteTrust) == false)
			{
				httpclient = getAllCertHttpClient();   
			}
			else
			{
				httpclient = new DefaultHttpClient(defaultHttpParams);   
			}
			httpclient.getParams().setParameter(CoreProtocolPNames.USER_AGENT, AbstractConstants.library.getWisprAgentString());
			
			httppost = new HttpPost(url);    
			httppost.setHeader("User-Agent", AbstractConstants.library.getWisprAgentString());
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));        
	
			// Execute HTTP Post Request        
			response = httpclient.execute(httppost);
            int StatusCode = response.getStatusLine().getStatusCode();

            AbstractConstants.log.add(TAG, Type.Debug, "HTTP Post: " + url + " Response:" + StatusCode);
    		
            BufferedReader in = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));


            line = in.readLine();

            if (line == null)
            {
            	//data from socket is pending sleep a couple seconds
            	Thread.sleep(2000);
            	line = in.readLine();
            }
            
            while (line != null) 
            {
                sb.append(line + NL);
                line = in.readLine();
            }
            
            in.close();
            page = sb.toString();
		} 
		catch (ClientProtocolException e) 
		{        
			AbstractConstants.log.add(TAG, Type.Warn, "postData", e);
		}
		catch (IOException e) 
		{        
			AbstractConstants.log.add(TAG, Type.Warn, "postData", e);	
		}
		catch (Exception e)
		{
			AbstractConstants.log.add(TAG, Type.Warn, "postData", e);
		}
		
		return page;
	}
	
	
	public int connect(String url)
	{
		return connect(url, false, null);
	}
	
	
	private int connect(String url, boolean post, List <NameValuePair> nameValuePairs)
	{
		int iRetValue = -1;
		wisprLogoffURL = null;
		Session testsession = null;
		
		try
		{
			String probeWebPage = null;
			String wisprXml = null;
			String wisprMsgCode = null;
		    String nextUrl = null;
		    String wisprRespCode = null;
		    		    
		    if (post)
		    	probeWebPage = postData(url, nameValuePairs);
		    else
		    	probeWebPage = getWebPage(url, MAX_TRIES, false, true);
			
			if ((probeWebPage != null) && (!probeWebPage.equals("")))
			{			
				AbstractConstants.log.add(TAG, Type.Debug, "HTML = " + probeWebPage);
				
				wisprXml = getWISPrXML(probeWebPage);
				
				if (wisprXml != null)
				{				
					AbstractConstants.log.add(TAG, Type.Debug, "XML = " + wisprXml);
					
					wisprXmlLog.append(wisprXml);
					
					WISPrXmlParser wisprXmlParser = new WISPrXmlParser();  
					SAXParserFactory spf = SAXParserFactory.newInstance();
			    	SAXParser sp = spf.newSAXParser();
			    	XMLReader xr = sp.getXMLReader();
			    	
			    	xr.setContentHandler(wisprXmlParser);
			    	xr.parse(new InputSource(new StringReader(wisprXml)));	 
					
			
					wisprRespCode = wisprXmlParser.getResponseCode();
					wisprMsgCode = wisprXmlParser.getMessageType();
					nextUrl = wisprXmlParser.getLoginURL();
					
					if (nextUrl==null || nextUrl.equals(""))
						nextUrl = wisprXmlParser.getLoginResultURL();
	
					AbstractConstants.log.add(TAG, Type.Debug, "WISPr Response Code = " + wisprRespCode);
					AbstractConstants.log.add(TAG, Type.Debug, "WISPr Message Code = " + wisprMsgCode);
					AbstractConstants.log.add(TAG, Type.Debug, "WISPr Next Url = " + nextUrl);
					
					if (wisprMsgCode == null)
					{
						if (wisprLoginURL != null)
						{
							if (verifyTrust(wisprLoginURL) == false)
							{
								return WEBPAGE_NOTTRUSTED;
							}
							else
								return ALREADY_CONNECTED;
						}							
						else
							return WISPR_NOT_PRESENT;
					}
					
					switch (Integer.parseInt(wisprMsgCode))
					{
						case WISPR_MESSAGE_TYPE_PROXY_NOTIFICATION:
							nextUrl = wisprXmlParser.getNextURL();
							
							if (nextUrl == null)
								return WISPR_NOT_PRESENT;
							
							iRetValue = connect(nextUrl);
							break;
							
						case WISPR_MESSAGE_TYPE_INITIAL:
							if (nextUrl == null)
								return WISPR_NOT_PRESENT;
							
							// set values which will get saved into the session data
							this.wisprLoginURL = wisprXmlParser.getLoginURL();
							this.wisprLocationID = wisprXmlParser.getAccessLocation();
							this.wisprLocationName = wisprXmlParser.getLocationName();
							this.wisprbwUserGroup = null;
							
							AbstractConstants.credentials.getCredentials(hotspotInfo);
							
							if (verifyTrust(nextUrl) == false)
							{
								return WEBPAGE_NOTTRUSTED;
							}
							
							if (!AbstractConstants.credentials.userID.equals("") && !AbstractConstants.credentials.password.equals(""))
							{
								
								String sNextUrlWithData = nextUrl; 

								List<NameValuePair> PostValuePairs = new ArrayList<NameValuePair>(2);
								PostValuePairs.add(new BasicNameValuePair("UserName", AbstractConstants.credentials.userID));        
								PostValuePairs.add(new BasicNameValuePair("Password", AbstractConstants.credentials.password));   
								PostValuePairs.add(new BasicNameValuePair("device-model-number", AbstractConstants.library.getDeviceModelNumber()));
								PostValuePairs.add(new BasicNameValuePair("device-type", String.valueOf(AbstractConstants.library.getDeviceType())));
								PostValuePairs.add(new BasicNameValuePair("calling-station-id", AbstractConstants.library.getDeviceMacAddress()));
								iRetValue = connect(sNextUrlWithData, true, PostValuePairs);
							}
							else
								return CREDENTIALS_MISSING;
							break;
							
						case WISPR_MESSAGE_TYPE_AUTH_NOTIFICATION:							
						case WISPR_MESSAGE_TYPE_RESPONSE_AUTH_POLL:
							if (wisprRespCode == null)
								return WISPR_NOT_PRESENT;
														
							if (Integer.parseInt(wisprRespCode) == WISPR_RESPONSE_CODE_LOGIN_SUCCEEDED)
							{
								wisprLogoffURL = wisprXmlParser.getLogoffURL();
								
																
								return WISPR_RESPONSE_CODE_LOGIN_SUCCEEDED;
							}
							else if (Integer.parseInt(wisprRespCode) == WISPR_RESPONSE_CODE_LOGIN_FAILED)
							{								
								// clear failed default credentials if they were used
								if (AbstractConstants.credentials.userID.equals(AbstractConstants.data.getSetting(Settings.SET_DefaultWisprUserID)))
								{
									AbstractConstants.data.setSetting(Settings.SET_DefaultWisprUserID, "", false);
									AbstractConstants.data.setSetting(Settings.SET_DefaultWisprPassword, "", false);	
								}
								
								return WISPR_RESPONSE_CODE_LOGIN_FAILED;
							}
							else if (Integer.parseInt(wisprRespCode) == WISPR_RESPONSE_CODE_AUTH_PENDING)
							{
								Thread.sleep(1000);
								iRetValue = connect(nextUrl);
							}
							else
								return WISPR_RESPONSE_CODE_INTERNAL_ERROR;
							
							break;
							
						case WISPR_MESSAGE_TYPE_RESPONSE_ABORT_LOGIN:							
						default:							
							return Integer.parseInt(wisprMsgCode);			
					}
				}
				else 
				{
					if (AbstractConstants.session == null)
						testsession = new Session();
					else
						testsession = AbstractConstants.session;
					
					wisprLoginURL = testsession.getWISPrLoginUrl();
					
					if (wisprLoginURL != null)
					{
						AbstractConstants.log.add(TAG, Type.Debug, "wisprLogin URL = " + wisprLoginURL );
						
						if (verifyTrust(wisprLoginURL) == false)
						{
							AbstractConstants.log.add(TAG, Type.Debug, "verifyTrust returned: false");
							return WEBPAGE_NOTTRUSTED;
						}
						else
						{
							if (probeTest())
								return ALREADY_CONNECTED;
							else
								return WEBPAGE_NOTFOUND;
						}
					}	
					else
					{
						AbstractConstants.log.add(TAG, Type.Debug, "wisprLoginURL not found.");
						
						if (AbstractConstants.data.getFlag(Flags.FLG_VerifyWebsiteTrust) == false)
						{
							if (probeTest())
								return ALREADY_CONNECTED;
							else
								return WEBPAGE_NOTFOUND;
						}
						else
							return WEBPAGE_NOTTRUSTED;
					}	
				}
			}
			else
			{
				if (AbstractConstants.session == null)
					testsession = new Session();
				else
					testsession = AbstractConstants.session;
				
				wisprLoginURL = testsession.getWISPrLoginUrl();
				
				if (wisprLoginURL != null)
				{
					AbstractConstants.log.add(TAG, Type.Debug, "wisprLogin URL = " + wisprLoginURL ); 
					if (verifyTrust(wisprLoginURL) == false)
					{
						AbstractConstants.log.add(TAG, Type.Debug, "verifyTrust returned: false");
						return WEBPAGE_NOTTRUSTED;
					}
					else
					{
						if (probeTest())
							return ALREADY_CONNECTED;
						else
							return WEBPAGE_NOTFOUND;
					}
				}	
				else
				{
					AbstractConstants.log.add(TAG, Type.Debug, "wisprLoginURL not found.");
					if (AbstractConstants.data.getFlag(Flags.FLG_VerifyWebsiteTrust) == false)
					{
						if (probeTest())
							return ALREADY_CONNECTED;
						else
							return WEBPAGE_NOTFOUND;
					}
					else
						return WEBPAGE_NOTTRUSTED;
				}	
			}
		}
		catch (Exception e)
		{
			AbstractConstants.log.add(TAG, Type.Error, "connect", e);
		}
		
		return iRetValue;
	}

	
	public static boolean probeTest()
	{
		boolean bReturn = false;
		
		
		try
		{
			AbstractConstants.log.add(TAG, Type.Debug, "Attempting web page probe");
			String probeWebPage = null;
			String wisprXml = null;
		    
		    if (AbstractConstants.library.isWiFiConnected())
		    {
		    	probeWebPage = getWebPage(getProbeURL(), 1, true, false);
			
				if (probeWebPage != null)
				{
					wisprXml = getWISPrXML(probeWebPage);
					
					if (wisprXml == null)
					{					
						bReturn = true;
					}
				}
		    }
			AbstractConstants.log.add(TAG, Type.Info, "Web page probe result: " + String.valueOf(bReturn));
		}
		catch (Exception e)
		{
			AbstractConstants.log.add(TAG, Type.Error, "probeTest", e);
		}
		
		
		return bReturn;
	}
	
            
	private static String getWISPrXML(String webPage) 
	{                  
		String wisprXML = null;
		
		try
		{
			int startTAG = webPage.indexOf("<" + WISPR_TAG);                  
			int endTAG = webPage.indexOf("</" + WISPR_TAG + ">", startTAG) + WISPR_TAG.length() + 3; 
			
			if (startTAG > -1 && endTAG > -1) 
			{                          
				wisprXML = new String(webPage.substring(startTAG, endTAG));                          
				if (!wisprXML.contains("&amp;")) 
				{                                  
					wisprXML = wisprXML.replace("&", "&amp;");                          
				}
				if (wisprXML.contains("”"))
				{
					wisprXML = wisprXML.replace("”", "\""); 
				}
			}          
		}
		catch (Exception e)
		{
			AbstractConstants.log.add(TAG, Type.Error, "getWISPrXML", e);
		}
		
		return wisprXML;          
	}  
	
	    
    private static String getWebPage(String url, int maxRetries, boolean bHandleRedirects, boolean bSendUserAgent)  
    {                  
        BufferedReader in = null;
        String page = null;
        Header redirectheader = null;
        int statusCode = 0;
        String redirecturl = null;
        
        try 
        {
        	StringBuffer sb = new StringBuffer("");
        	
        	if (maxRetries <= 0)
        		return null;
        	
        	if (url == null || url.equals(""))
        	{
        		AbstractConstants.log.add(TAG, Type.Debug, "Skipped getting webpage, URL is not specified");  
        		return null;
        	}
        	
        	AbstractConstants.log.add(TAG, Type.Debug, "HTTP get: " + url);  
        	        	
        	HttpClient client = null;
        	
			if (AbstractConstants.data.getFlag(Flags.FLG_VerifyWebsiteTrust) == false)
			{
				client = getAllCertHttpClient();
			}
			else
			{
				client = new DefaultHttpClient(defaultHttpParams);				
			}        
        	
			client.getParams().setBooleanParameter(org.apache.http.client.params.AllClientPNames.HANDLE_REDIRECTS, bHandleRedirects);

        	
        	HttpGet request = new HttpGet();
        	
        	if (bSendUserAgent)
        	{        		            
        		client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, AbstractConstants.library.getWisprAgentString());
        		request.setHeader("User-Agent", AbstractConstants.library.getWisprAgentString());
        	}
                                
            String line = "";
            String NL = System.getProperty("line.separator");
            
            URI URIurl = null;
            
            URIurl = new URI(url);
            
            request.setURI(URIurl);
            
            HttpResponse response = client.execute(request);
            
            statusCode = response.getStatusLine().getStatusCode();
            AbstractConstants.log.add(TAG, Type.Debug, "Response: " + String.valueOf(statusCode));
            
            if (statusCode == 302)   
            {
            	redirectheader = response.getFirstHeader("Location");
            	
            	if (redirectheader!= null)
            		AbstractConstants.log.add(TAG, Type.Debug, "Redirect URL: " + redirectheader.getValue());            
            }

            
            in = new BufferedReader (new InputStreamReader(response.getEntity().getContent()));
                      
            line = in.readLine();
            
            if (line == null)
            {
            	//data from socket is pending sleep a couple seconds
            	Thread.sleep(2000);
            	line = in.readLine();
            }
            
            while (line != null) 
            {
                sb.append(line + NL);
                line = in.readLine();
            }
            
            in.close();
            
            page = sb.toString(); 
            
            if (statusCode == 200 && bHandleRedirects==false)
            {
            	String wisprXml = getWISPrXML(page);
            	if (wisprXml == null && page.toLowerCase().contains("window.location"))
            	{
            		int startloc = page.toLowerCase().indexOf("window.location");
            		int redirecturlstart = page.toLowerCase().indexOf("\"", startloc);
            		int redirecturlend = page.toLowerCase().indexOf("\"", redirecturlstart + 1);
            		redirecturl = page.substring(redirecturlstart+1, redirecturlend-1);
            		AbstractConstants.log.add(TAG, Type.Debug, "No WISPr XML Found but javascript redirect found --Redirect URL:" + redirecturl);
            		AbstractConstants.log.add(TAG, Type.Debug, "Setting up Redirect workaround");
            		page = null;
            		url = redirecturl;
            	}
            }
            
                       
        }         
        catch (UnknownHostException e)
        {
        	AbstractConstants.log.add(TAG, Type.Warn, "getWebPage: " + e.getMessage());
        	AbstractConstants.log.add(TAG, Type.Warn, "getWebPage: Unknown host " + url);             	
        }
        catch (SocketException e)
        {
        	AbstractConstants.log.add(TAG, Type.Warn, "getWebPage: " + e.getMessage());
        	AbstractConstants.log.add(TAG, Type.Warn, "getWebPage: Network unreachable " + url);        
        }
        catch (Exception e)
        {
        	AbstractConstants.log.add(TAG, Type.Error, "getWebPage", e);        
    	} 
        finally
        {
        	if (in != null) 
            {
                try 
                {
                    in.close();
                } 
                catch (IOException eIO) 
                {
                	AbstractConstants.log.add(TAG, Type.Error, "getWebPage", eIO);
                }
            }         
        }
        
        try
        {
			if ((page == null) || page.equals(""))
			{
				Thread.sleep(500);
				// check to see if we were redirected if so use redirect url
				// if not just try the same url again.
				if (statusCode == 302)				
					page = getWebPage(url, maxRetries -1, true, bSendUserAgent);
				else
					page = getWebPage(url, maxRetries -1, bHandleRedirects, bSendUserAgent);	
			}
        }
        catch (Exception e) 
        {
        	AbstractConstants.log.add(TAG, Type.Error, "getWebPage", e);
        }

    	return page;          
    }
    
    
    public void logoutWISPr(String wisprLogoffURL)  
    {
    	try
    	{
	    	if (wisprLogoffURL == null || wisprLogoffURL.equals(""))
	    	{
	    		AbstractConstants.log.add(TAG, Type.Warn, "WISPr Logoff URL not specified, logoff canceled");
	    		return;
	    	}
	 	    	
	    	String logoutPage = getWebPage(wisprLogoffURL, 1, false, true);
	    	
			if (logoutPage == null || logoutPage.equals(""))
			{
				AbstractConstants.log.add(TAG, Type.Warn, "WISPr Logoff page not found, logoff canceled");
	    		return;
	    	}
	
			AbstractConstants.log.add(TAG, Type.Info, "Requesting WISPr logoff URL: " + wisprLogoffURL);
			
			String wisprXml = getWISPrXML(logoutPage);
			
			if (wisprXml != null)
			{					
				try
				{
					WISPrXmlParser wisprXmlParser = new WISPrXmlParser();  
					SAXParserFactory spf = SAXParserFactory.newInstance();
			    	SAXParser sp = spf.newSAXParser();
			    	XMLReader xr = sp.getXMLReader();
			    	
			    	xr.setContentHandler(wisprXmlParser);
			    	xr.parse(new InputSource(new StringReader(wisprXml)));	
					
			    	
			
					String wisprRespCode = wisprXmlParser.getResponseCode();
					String wisprMsgCode = wisprXmlParser.getMessageType();
					String nextUrl = wisprXmlParser.getLoginURL();
					
					if (nextUrl == null || nextUrl.equals(""))
						nextUrl = wisprXmlParser.getLoginResultURL();
					
					AbstractConstants.log.add(TAG, Type.Debug, "WISPr Response Code = " + wisprRespCode);
					AbstractConstants.log.add(TAG, Type.Debug, "WISPr Message Code = " + wisprMsgCode);
					AbstractConstants.log.add(TAG, Type.Debug, "WISPr Next Url = " + nextUrl);
				}
				catch (Exception e)
				{
					AbstractConstants.log.add(TAG, Type.Error, "LogoutWISPr() Xml.parse error", e);
				}
			}
    	}
    	catch (Exception e)
    	{
    		AbstractConstants.log.add(TAG, Type.Error, "logoutWISPr", e);
    	}
    }
}
