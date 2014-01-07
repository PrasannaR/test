package com.quintech.common;


import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler; 

import com.quintech.common.HotspotInfo;

public class VZWHotspotParser extends DefaultHandler 
{ 
	public List<HotspotInfo> hotspotList = null;
	public float version = 0.0F;
    private StringBuffer buffer = null;
    private boolean buffering = false; 
    
    public VZWHotspotParser()
    {
    	// initialize 
    	hotspotList = new ArrayList<HotspotInfo>();
    }
    
    
    @Override
    public void startDocument() throws SAXException 
    {
        // re-initialize
    	hotspotList = new ArrayList<HotspotInfo>();
    } 
    
    @Override
    public void endDocument() throws SAXException 
    {

    } 
    
    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException 
    {
    	if (localName.equalsIgnoreCase("globalssidInfo"))
    	{
    		// create new object
    		hotspotList.add(new HotspotInfo());
   			// set directory version on each hotspot
			hotspotList.get(hotspotList.size() - 1).version = Float.valueOf(buffer.toString());
    	}
    	
        	
    	if (localName.equalsIgnoreCase("version") ||
    		localName.equalsIgnoreCase("ssidId") ||
			localName.equalsIgnoreCase("ssid") || 
			localName.equalsIgnoreCase("displaySSID") ||
			localName.equalsIgnoreCase("loginRequired") ||
			localName.equalsIgnoreCase("autoLogin") ||
			localName.equalsIgnoreCase("forceConnect") ||
			localName.equalsIgnoreCase("aggregatorName") ||
			localName.equalsIgnoreCase("operatorName") ||
			localName.equalsIgnoreCase("decorationName") ||
			localName.equalsIgnoreCase("loginURL") ||
			localName.equalsIgnoreCase("probeURL"))
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
    	// save values from buffer
    	if (localName.equalsIgnoreCase("version"))
    	{
    		try
    		{
    			// set directory version
    			version = Float.valueOf(buffer.toString());
    			
    			// set directory version on each hotspot
    			hotspotList.get(hotspotList.size() - 1).version = Float.valueOf(buffer.toString());
    		}
    		catch (Exception ex) { }
    	}
    	
    	else if (localName.equalsIgnoreCase("ssidId"))
    	{
    		try
    		{
    			hotspotList.get(hotspotList.size() - 1).id = Integer.parseInt(buffer.toString());
    		}
    		catch (Exception ex) { }
    	}
    	else if (localName.equalsIgnoreCase("ssid"))
    		hotspotList.get(hotspotList.size() - 1).ssid = buffer.toString();
    	
    	else if (localName.equalsIgnoreCase("displaySSID"))
    		hotspotList.get(hotspotList.size() - 1).displaySSID = buffer.toString();
    	
    	else if (localName.equalsIgnoreCase("loginRequired"))
    	{
    		try
    		{
    			hotspotList.get(hotspotList.size() - 1).loginRequired = Boolean.parseBoolean(buffer.toString());
    		}
    		catch (Exception ex) { }
    	}
    	
    	else if (localName.equalsIgnoreCase("autoLogin"))
    	{
    		try
    		{
    			hotspotList.get(hotspotList.size() - 1).autoLogin = Boolean.parseBoolean(buffer.toString());
    		}
    		catch (Exception ex) { }
    	}
    	
    	else if (localName.equalsIgnoreCase("forceConnect"))
    	{
    		try
    		{
    			hotspotList.get(hotspotList.size() - 1).forceConnect = Boolean.parseBoolean(buffer.toString());
    		}
    		catch (Exception ex) { }
    	}
    	
    	else if (localName.equalsIgnoreCase("aggregatorName"))
    		hotspotList.get(hotspotList.size() - 1).aggregatorName = buffer.toString();
    	
    	else if (localName.equalsIgnoreCase("operatorName"))
    		hotspotList.get(hotspotList.size() - 1).operatorName = buffer.toString();
    	
    	else if (localName.equalsIgnoreCase("decorationName"))
    		hotspotList.get(hotspotList.size() - 1).decorationName = buffer.toString();
    	
    	else if (localName.equalsIgnoreCase("loginURL"))
    		hotspotList.get(hotspotList.size() - 1).loginURL = buffer.toString();
    	
    	else if (localName.equalsIgnoreCase("probeURL"))
    		hotspotList.get(hotspotList.size() - 1).probeURL = buffer.toString();
    }
}

