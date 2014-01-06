package com.quintech.common;

import java.security.MessageDigest;

import com.quintech.common.ILog.Type;


public class SimpleSHA1 
{
	private static String TAG = "SimpleSHA1";
	
	
	private static String convertToHex(byte[] data) 
	{
		String sResult = "";
		
		try
		{
	        StringBuffer buffer = new StringBuffer();
	        
	        for (int i = 0; i < data.length; i++) 
	        { 
	            int halfbyte = (data[i] >>> 4) & 0x0F;
	            int two_halfs = 0;
	            
	            do 
	            { 
	                if ((0 <= halfbyte) && (halfbyte <= 9)) 
	                    buffer.append((char) ('0' + halfbyte));
	                else 
	                    buffer.append((char) ('a' + (halfbyte - 10)));
	                
	                halfbyte = data[i] & 0x0F;
	                
	            } 
	            while(two_halfs++ < 1);
	        } 
	        
	        sResult = buffer.toString();
		}
		catch (Exception e)
		{
			AbstractConstants.log.add(TAG, Type.Error, "convertToHex", e);
		}
        
        return sResult;
    } 

    public static String getSHA1(String text)
    { 
    	String sResult = "";
    	
    	try
    	{
	    	MessageDigest md = MessageDigest.getInstance("SHA-1");
	    	byte[] sha1hash = new byte[40];
	    
	    	md.update(text.getBytes("iso-8859-1"), 0, text.length());
	    	sha1hash = md.digest();
	    	
	    	sResult = convertToHex(sha1hash);
    	}
    	catch (Exception e)
	    {
    		AbstractConstants.log.add(TAG, Type.Error, "getSHA1", e);
	    }
    
    	return sResult;
    } 
}
