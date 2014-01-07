package com.quintech.common;


import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler; 

import com.quintech.common.VenueInfo;

public class VZWDirectoryVenueParser extends DefaultHandler 
{ 
	public List<VenueInfo> directoryVenueList = null;
    private StringBuffer buffer = null;
    private boolean buffering = false; 
    
    public VZWDirectoryVenueParser()
    {
    	// initialize 
    	directoryVenueList = new ArrayList<VenueInfo>();
    }
    
    
    @Override
    public void startDocument() throws SAXException 
    {
        // re-initialize
    	directoryVenueList = new ArrayList<VenueInfo>();
    } 
    
    
    @Override
    public void endDocument() throws SAXException 
    {


    } 
    
    
    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException 
    {
    	if (localName.equalsIgnoreCase("venueInfo"))
    	{
    		// create new object
    		directoryVenueList.add(new VenueInfo());
    	}
    	
        	
    	if (localName.equalsIgnoreCase("venueId") ||
			localName.equalsIgnoreCase("name") || 
			localName.equalsIgnoreCase("streetAddress1") ||
			localName.equalsIgnoreCase("zipCode") ||
			localName.equalsIgnoreCase("VenueType") ||
			localName.equalsIgnoreCase("operatorID") ||
			localName.equalsIgnoreCase("operatorName") ||
			localName.equalsIgnoreCase("country") ||
			localName.equalsIgnoreCase("state") ||
			localName.equalsIgnoreCase("city") ||
			localName.equalsIgnoreCase("averageFeedback") ||
			localName.equalsIgnoreCase("venuePhoneNumber"))
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
    	if (localName.equalsIgnoreCase("venueId"))
    		directoryVenueList.get(directoryVenueList.size() - 1).venueID = buffer.toString();
    	
    	else if (localName.equalsIgnoreCase("name"))
    		directoryVenueList.get(directoryVenueList.size() - 1).name = buffer.toString();
    	
    	else if (localName.equalsIgnoreCase("streetAddress1"))
    		directoryVenueList.get(directoryVenueList.size() - 1).streetAddress1 = buffer.toString();
    	
    	else if (localName.equalsIgnoreCase("zipCode"))
    		directoryVenueList.get(directoryVenueList.size() - 1).zipCode = buffer.toString();
    	
    	else if (localName.equalsIgnoreCase("VenueType"))
    		directoryVenueList.get(directoryVenueList.size() - 1).venueType = buffer.toString();
    	
    	else if (localName.equalsIgnoreCase("operatorID"))
    		directoryVenueList.get(directoryVenueList.size() - 1).operatorID = buffer.toString();
    	
    	else if (localName.equalsIgnoreCase("operatorName"))
    		directoryVenueList.get(directoryVenueList.size() - 1).operatorName = buffer.toString();
    	
    	else if (localName.equalsIgnoreCase("country"))
    		directoryVenueList.get(directoryVenueList.size() - 1).country = buffer.toString();
    	
    	else if (localName.equalsIgnoreCase("state"))
    		directoryVenueList.get(directoryVenueList.size() - 1).state= buffer.toString();
    	
    	else if (localName.equalsIgnoreCase("city"))
    		directoryVenueList.get(directoryVenueList.size() - 1).city = buffer.toString();
    	
    	else if (localName.equalsIgnoreCase("averageFeedback"))
    		directoryVenueList.get(directoryVenueList.size() - 1).averageFeedback = buffer.toString();
    	
    	else if (localName.equalsIgnoreCase("venuePhoneNumber"))
    		directoryVenueList.get(directoryVenueList.size() - 1).venuePhoneNumber = buffer.toString();
    }
}

