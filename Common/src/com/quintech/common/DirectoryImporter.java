package com.quintech.common;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.quintech.common.ILog.Type;


public class DirectoryImporter 
{
	private static String TAG = "DirectoryImporter";
	
	
	public static void importDirectories(List<DirectoryInfo> assignedDirectories)
	{
		if (assignedDirectories == null)
			return;
		
		try
	    {
			for (DirectoryInfo directoryInfo : assignedDirectories)
			{
				File tempFileDirectory = null;
				
				try
				{
					// get the current directory version
					float installedDirectoryVersion = AbstractConstants.data.getHotspotVersion(directoryInfo.guid);
					
					// if directory version is less than 1, it is not yet installed
					if (installedDirectoryVersion < 1.0)
					{
						// create a new directory record - this will not insert duplicates
						AbstractConstants.data.insertDirectory(directoryInfo, true, true);
					}
					
					
					// update directory info
					AbstractConstants.data.updateDirectoryInfo(directoryInfo);
					
					
					
					// skip if installed directory has latest SequenceID
					if (installedDirectoryVersion >= Float.parseFloat(String.valueOf(directoryInfo.sequenceID)))
					{
						AbstractConstants.log.add(TAG, Type.Info, "Directory " + directoryInfo.displayName + " is up to date");
						continue;
					}
					
					// download and import new directory file
					
					// download the ZIP file to a new temporary directory
					java.util.Date date = new java.util.Date();
					String downloadUrl = null;
					tempFileDirectory = new File(AbstractConstants.library.getWriteableDirectory(), "/" + String.valueOf(date.getTime() + "/"));
					String targetFileName = "import.zip";
					File downloadedFile = null;
					
					for (String server : AbstractConstants.getRovaPortalServerList())
					{
						// attempt to download file
						downloadUrl = server + directoryInfo.distinctHotspotDirectoryPath;
						downloadedFile = AbstractLibrary.downloadFile(downloadUrl, tempFileDirectory, targetFileName);
						
						// break if downloaded, otherwise attempt from next available server
						if (downloadedFile != null && downloadedFile.exists())
							break;
					}
					
					
					if (downloadedFile == null || !downloadedFile.exists() || downloadedFile.length() < 1)
					{
						AbstractConstants.log.add(TAG, Type.Warn, "Failed to download directory file for: " + directoryInfo.displayName);
						continue;
					}
					
					
					// extract ZIP
					AbstractConstants.log.add(TAG, Type.Debug, "Extracting directory ZIP file for: " + directoryInfo.displayName);
					
					ZipFile zipFile = new ZipFile(downloadedFile);
					
					// write files
					for (ZipEntry zipEntry : java.util.Collections.list(zipFile.entries()))
					{
						File out_file = new File(tempFileDirectory, zipEntry.getName());
						Files.write(ByteStreams.toByteArray(zipFile.getInputStream(zipEntry)), out_file);
						
						// old method using commons-io
						//FileUtils.copyInputStreamToFile(zipFile.getInputStream(zipEntry), out_file);
						
						AbstractConstants.log.add(TAG, Type.Debug, "Extracted directory ZIP file " + zipEntry.getName());
					}
					
					zipFile.close();

					AbstractConstants.log.add(TAG, Type.Debug, "Extracted directory ZIP file for: " + directoryInfo.displayName);
					
					
					
					// parse directory file
					List<HotspotInfo> hotspotInfoList = parseHotspotData(tempFileDirectory);
					
					if (hotspotInfoList.size() > 0)
					{
						// delete existing hotspots in directory
						AbstractConstants.data.deleteHotspots(directoryInfo.guid);
						
						AbstractConstants.log.add(TAG, Type.Debug, "Deleted existing hotspots for: " + directoryInfo.displayName);
						
						// insert new data
						for (HotspotInfo hotspot : hotspotInfoList)
						{
							// default login required flag to false
							hotspot.loginRequired = false;
							
							// if this is a VZW Wi-Fi directory, set flag to true
							if (directoryInfo.isVerizonDirectory)
								hotspot.loginRequired = true;
							
							AbstractConstants.data.insertHotspot(directoryInfo.guid, hotspot);
						}
						
						AbstractConstants.log.add(TAG, Type.Info, "Imported " + String.valueOf(hotspotInfoList.size()) + " hotspots for directory: " + directoryInfo.displayName);
					}
					else
						AbstractConstants.log.add(TAG, Type.Debug, "No hotspots were imported or changed for directory: " + directoryInfo.displayName);
				}
				catch (Exception e)
			    {
			    	AbstractConstants.log.add(TAG, Type.Warn, "Error processing directory " + directoryInfo.guid, e);
			    }
				finally
				{
					try
					{
						// delete all files in temp directory
						if (tempFileDirectory != null && tempFileDirectory.exists())
						{
							for (File file : tempFileDirectory.listFiles())
								file.delete();
							
							// delete temp directory
							tempFileDirectory.delete();
						}
					}
					catch (Exception e)
					{
						AbstractConstants.log.add(TAG, Type.Error, "Error deleting temporary directory import folder", e);
					}
				}
			}
			
			
			// disable all installed imported directories
			AbstractConstants.data.setImportedDirectoriesDisabled();
			
			
			// enable all imported directories in the assigned list
			for (DirectoryInfo directoryInfo : assignedDirectories)
				AbstractConstants.data.setDirectoryEnabled(directoryInfo);
	    }
	    catch (Exception e)
	    {
	    	AbstractConstants.log.add(TAG, Type.Error, "importDirectories", e);
	    }
	}
	
	
	private static List<HotspotInfo> parseHotspotData(File tempFileDirectory)
	{
		List<HotspotInfo> hotspotInfoList = new ArrayList<HotspotInfo>();
		
		try
		{
			// read schema.ini for parsing data
			File schemaFile = new File(tempFileDirectory, "schema.ini");
			
			if (!schemaFile.exists())
			{
				AbstractConstants.log.add(TAG, Type.Debug, "Directory schema file could not be found.");
				return hotspotInfoList;
			}
			
			
			// parse schema file into a string array
			String schemaContents = AbstractConstants.library.readFileTostring(schemaFile);
			String[] schemaArray = schemaContents.split("\n");
			
			// read directory file name from first line, remove brackets
			String directoryFilename = schemaArray[0].trim().replace("[", "").replace("]", "");
			
			

			// open directory file
			File directoryFile = new File(tempFileDirectory, directoryFilename);
			
			if (!directoryFile.exists())
			{
				AbstractConstants.log.add(TAG, Type.Debug, "Directory data file could not be found: " + directoryFilename);
				return hotspotInfoList;
			}
			
			
			
			// parse directory data
			BufferedReader br = new BufferedReader(new FileReader(directoryFile));
			String headerLine = br.readLine();
	        String dataLine;
			
			
			// parse column positions
			int colSequenceID = getDataColumnPosition(headerLine, "SequenceID");
			int colActive = getDataColumnPosition(headerLine, "Active");
			int colNetworkName_SSID = getDataColumnPosition(headerLine, "NetworkName_SSID");
			int colWEPKey = getDataColumnPosition(headerLine, "WEPKey");
			int colWEPKeyIndex = getDataColumnPosition(headerLine, "WEPKeyIndex");
			
			
	        // parse data
	        while ((dataLine = br.readLine()) != null) 
	        {
	        	// split data line
	            String dataArray[] = dataLine.split("\",\"");
	             
	            // ignore inactive records
	            if (!removePrecedingAndTrailingDoubleQuotes(dataArray[colActive]).equalsIgnoreCase("True"))
	            	continue;
	            
	            
	            HotspotInfo hotspot = new HotspotInfo();
	            hotspot.ssid = removePrecedingAndTrailingDoubleQuotes(dataArray[colNetworkName_SSID].trim());
	            hotspot.displaySSID = hotspot.ssid;
	            hotspot.version = Float.parseFloat(removePrecedingAndTrailingDoubleQuotes(dataArray[colSequenceID].trim()));
	            hotspot.password = removePrecedingAndTrailingDoubleQuotes(dataArray[colWEPKey].trim());
	            hotspot.wepKeyIndex = 0;
	            
	            try
	            {
	            	hotspot.wepKeyIndex = Integer.parseInt(removePrecedingAndTrailingDoubleQuotes(dataArray[colWEPKeyIndex].trim()));
	            }
	            catch (Exception e) { }
	            
	            hotspotInfoList.add(hotspot);
	        }
	        
	        br.close();
		}
		catch (Exception e)
	    {
	    	AbstractConstants.log.add(TAG, Type.Error, "parseHotspotData", e);
	    }
		
		return hotspotInfoList;
	}
	
	
	private static String removePrecedingAndTrailingDoubleQuotes(String value)
	{
		try
		{
			// remove beginning and trailing double quotes if they exist
			
			if (value.startsWith("\""))
				value = value.substring(1, value.length() - 1);
			
			if (value.endsWith("\""))
				value = value.substring(0, value.length() - 2);
		}
		catch (Exception e)
	    {
	    	AbstractConstants.log.add(TAG, Type.Error, "removePrecedingAndTrailingDoubleQuotes", e);
	    }
		
		return value;
	}
	
	
	private static int getDataColumnPosition(String headerLine, String columnName)
	{
		int position = -1;
		
		try
		{
			String[] lineArray = headerLine.split(",");
			
			for (int i = 0; i < lineArray.length; i++)
			{
				if (lineArray[i].replaceAll("\"", "").trim().equalsIgnoreCase(columnName))
				{
					position = i;
					break;
				}
			}
		}
		catch (Exception e)
	    {
	    	AbstractConstants.log.add(TAG, Type.Error, "getDataColumnPosition", e);
	    }
		
		return position;
	}
}
