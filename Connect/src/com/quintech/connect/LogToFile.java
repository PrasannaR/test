package com.quintech.connect;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.quintech.common.ILog.Type;


public class LogToFile
{
	private static String TAG = "LogToFile";
    private static PrintWriter printWriter = null;
    private static File outFile = null;
    
    
    public LogToFile()
    {
    	initialize();
    }
    
    
    public static File getOutFile()
    {
    	return outFile;
    }
    

    public File getLogDirectory()
    {
    	File fileLogDirectory = null;
    	
    	try
    	{
    		fileLogDirectory = new File(Constants.applicationContext.getExternalFilesDir(null) + "/" + Constants.getLibrary().getLogDirectoryName());
    		
    		// create directory if it does not exist
        	if (!fileLogDirectory.exists())
        		fileLogDirectory.mkdir();
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "getLogDirectory", e);
    	}
    	
    	
    	return fileLogDirectory;
    }
    
    
    private void initialize()
    {
        try 
        {
        	// get log directory
        	File logDirectory = getLogDirectory();
        	
            if (logDirectory.canWrite())
            {
    			// create file
    			outFile = new File(logDirectory + "/", Library.getDateTimeGMT(Library.DATE_FORMAT) + ".log");
    			
    			if (!outFile.exists())
    			{
    				outFile.createNewFile();
    			}
    			
    			
    			if (outFile.canWrite())
    			{
	                FileWriter fileWriter = new FileWriter(outFile, true);
	                printWriter = new PrintWriter(fileWriter);
	                printWriter.write("\n\nAPPLICATION LOG " + Library.getDateTimeGMT(Library.DATE_TIME_FORMAT) + "\n\n");
	                printWriter.flush();
    			}
            }
            else
            	android.util.Log.w(TAG, "Unable to write to log directory path");
        } 
        catch (IOException e) 
        {
        	android.util.Log.e(TAG, "initialize: " + e.toString());
			e.printStackTrace();
        }
    }
   
    
    public void write(String tag, String message, Exception error)
    {
    	try
    	{
    		// write log message if initialized and has write permission
	        if (outFile != null && printWriter != null && outFile.canWrite())
	        {
	        	printWriter.write(Library.getDateTimeGMT(Library.TIME_FORMAT) + "\t");
    			
    			if (tag != null && !tag.equals(""))
    				printWriter.write(tag + "\t\t");
		        
    			if (message != null && !message.equals(""))
    				printWriter.write(message + "\n");
		        
    			if (error != null)
    			{
    				printWriter.write(error.getMessage().toString() + "\n");
    				
    				for (int i = 0; i < error.getStackTrace().length; i++)
    					printWriter.write(error.getStackTrace()[i].toString() + "\n");
    				
    				printWriter.write("\n\n");
    			}

		        printWriter.flush();
	        }
    	}
    	catch (Exception e)
    	{
    		android.util.Log.e(TAG, "write: " + e.toString());
			e.printStackTrace();
    	}
    }
    
    
    public void write(String message)
    {
    	try
    	{
    		// write log message if initialized and has write permission
	        if (outFile != null && printWriter != null && outFile.canWrite())
	        {
    			if (message != null && !message.equals(""))
    				printWriter.write(message + "\n");
		        
		        printWriter.flush();
	        }
    	}
    	catch (Exception e)
    	{
    		android.util.Log.e(TAG, "write: " + e.toString());
			e.printStackTrace();
    	}
    }
   
    
    public static void shutdown()
    {
    	try
    	{
	        if (printWriter != null)
	            printWriter.close();
    	}
    	catch (Exception e)
    	{
    		android.util.Log.e(TAG, "shutdown: " + e.toString());
			e.printStackTrace();
    	}
    }
}
