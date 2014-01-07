package com.quintech.common;

import org.xml.sax.Attributes;  
import org.xml.sax.helpers.DefaultHandler;

import com.quintech.common.ILog.Type;


public class WISPrXmlParser extends DefaultHandler 
{
	private static String TAG = "WISPrXmlParser";
	
	enum Tag 	
	{                  
		wispaccessgatewayparam, 
		redirect, 
		accessprocedure, 
		loginurl, 
		logoffurl,
		abortloginurl, 
		messagetype, 
		responsecode, 
		accesslocation, 
		locationname,
		fonresponsecode, 
		replymessage, 
		authenticationpollreply, 
		authenticationreply,
		loginresultsurl,
		proxy,
		nexturl,
		logoffreply
	} 
	
	private Tag actualTag;            
	private String accessProcedure = "";            
	private String accessLocation = "";            
	private String loginURL = "";       
	private String logoffURL = "";
	private String abortLoginURL = "";            
	private String messageType = "";            
	private String responseCode = "";            
	private String locationName = "";  
	private String loginResultURL = "";
	private String nextURL = "";
	
	
	@Override          
	public void startElement(String uri, String name, String qName, Attributes atts) 
	{                  
		try
		{
			actualTag = Tag.valueOf(name.trim().toLowerCase());   
			AbstractConstants.log.add(TAG, Type.Debug, "XML Open Tag Found = " + actualTag);
		}
		catch (IllegalArgumentException e)
		{
			AbstractConstants.log.add(TAG, Type.Warn, "startElement", e);
		}
		catch (Exception e)
		{
			AbstractConstants.log.add(TAG, Type.Error, "startElement", e);
		}
	}     
	
	@Override
	public void endElement(String uri, String name, String qName) 
	{
		try
		{
			actualTag = Tag.valueOf(name.trim().toLowerCase());   
			AbstractConstants.log.add(TAG, Type.Debug, "XML Close Tag Found = " + actualTag);
		}
		catch (IllegalArgumentException e)
		{
			AbstractConstants.log.add(TAG, Type.Warn, "endElement", e);
		}
		catch (Exception e)
		{
			AbstractConstants.log.add(TAG, Type.Error, "endElement", e);
		}
	}

	
	@Override          
	public void characters(char ch[], int start, int length) 
	{          
		try
		{
			String chars = (new String(ch).substring(start, start + length));                  
			if (actualTag.equals(Tag.accessprocedure)) 
			{                          
				accessProcedure += chars;                  			
			} 
			else if (actualTag.equals(Tag.loginurl)) 
			{                          
				loginURL += chars;                  
			} 
			else if (actualTag.equals(Tag.abortloginurl)) 
			{                          
				abortLoginURL += chars;                  
			} 
			else if (actualTag.equals(Tag.messagetype)) 
			{                          
				messageType += chars;                  
			} 
			else if (actualTag.equals(Tag.responsecode)) 
			{                          
				responseCode += chars;                  
			} 
			else if (actualTag.equals(Tag.accesslocation)) 
			{                          
				accessLocation += chars;                  
			} 
			else if (actualTag.equals(Tag.locationname)) 
			{                          
				locationName += chars;                  
			}          
			else if (actualTag.equals(Tag.logoffurl)) 
			{
				logoffURL += chars;  
			} 
			else if (actualTag.equals(Tag.loginresultsurl))
			{
				loginResultURL += chars;
			}
			else if (actualTag.equals(Tag.nexturl))
			{
				nextURL += chars;
			}
			else if (actualTag.equals(Tag.logoffreply))
			{
			}			
		}
		catch (Exception e)
		{
			AbstractConstants.log.add(TAG, Type.Error, "characters", e);
		}
	}            

	
	public String getNextURL() 
	{                  
		return nextURL.trim();          
	}    
	
	
	public String getLoginResultURL() 
	{                  
		return loginResultURL.trim();          
	}            

	
	public String getAccessProcedure() 
	{                  
		return accessProcedure.trim();          
	}            
	
	
	public String getLoginURL() 
	{                  
		return loginURL.trim();          
	}            
	
	
	public String getAbortLoginURL() 
	{                  
		return abortLoginURL.trim();          
	}            
	
	
	public String getMessageType() 
	{                  
		return messageType.trim();          
	}            
	
	
	public String getResponseCode() 
	{                  
		return responseCode.trim();          
	}            
	
	
	public String getAccessLocation() 
	{                  
		return accessLocation.trim();          
	}            
	
	
	public String getLocationName() 
	{                  
		return locationName.trim();          
	}            
	
	
    public String getLogoffURL()
    {
    	return logoffURL.trim(); 
    }  
    
    
	@Override          
	public String toString() 
	{                  
		StringBuilder sb = new StringBuilder();                  
		sb.append("WISPrInfoHandler{");                  
		sb.append("accessProcedure: ").append(accessProcedure).append(", ");                 
		sb.append("accessLocation: ").append(accessLocation).append(", ");                  
		sb.append("locationName: ").append(locationName).append(", ");                  
		sb.append("loginURL: ").append(loginURL).append(", ");                  
		sb.append("abortLoginURL: ").append(abortLoginURL).append(", ");                  
		sb.append("messageType: ").append(messageType).append(", ");                  
		sb.append("responseCode: ").append(responseCode);                  
		sb.append("}");                    
		return sb.toString();          
	}  
}  

