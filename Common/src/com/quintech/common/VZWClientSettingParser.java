package com.quintech.common;


import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler; 

import com.quintech.common.ClientSettingInfo;

public class VZWClientSettingParser extends DefaultHandler 
{ 
	public List<ClientSettingInfo> clientSettingInfoList = null;
    
    public VZWClientSettingParser()
    {
    	// initialize 
    	clientSettingInfoList = new ArrayList<ClientSettingInfo>();
    }
    
    
    @Override
    public void startDocument() throws SAXException 
    {
        // re-initialize
    	clientSettingInfoList = new ArrayList<ClientSettingInfo>();
    } 
    
    @Override
    public void endDocument() throws SAXException 
    {

    } 
    
    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException 
    {
    	if (localName.equalsIgnoreCase("clientSetting"))
    	{
    		// add new object to list
    		clientSettingInfoList.add(new ClientSettingInfo(atts.getValue("", "name"), atts.getValue("", "value"), "0"));
    	}
    } 
    
    @Override
    public void characters(char ch[], int start, int length) 
    {
    	// do nothing, values are only in attributes
    } 
    
    @Override
    public void endElement(String namespaceURI, String localName, String qName)  throws SAXException 
    {
    	// do nothing, values are only in attributes
    }
}

