package com.quintech.common;


import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler; 

public class VZWReturnCodeParser extends DefaultHandler 
{ 
	private StringBuffer buffer = null;
	private String targetNodeName = "";
    private boolean buffering = false; 
    
    public String returnCode = "";
    
    public VZWReturnCodeParser(String targetNodeName)
    {
    	if (targetNodeName != null)
    		this.targetNodeName = targetNodeName;
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
    	// read specified node, or default error node for VZW HDS service
    	if (localName.equalsIgnoreCase(targetNodeName) || localName.equalsIgnoreCase("errorCode"))
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
    	if (localName.equalsIgnoreCase(targetNodeName) || localName.equalsIgnoreCase("errorCode"))
    	{
    		returnCode = buffer.toString();
    	}
    }
}

