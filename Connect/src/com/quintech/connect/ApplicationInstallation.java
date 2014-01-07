package com.quintech.connect;



import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;
import android.content.Intent;
import android.database.Cursor;

import com.quintech.common.ApplicationAssignedInfo;
import com.quintech.common.DownloadQueueInfo;
import com.quintech.common.ILog.Type;



@SuppressLint("NewApi")
public class ApplicationInstallation 
{
	private static String TAG = "ApplicationInstallation";
	private static DownloadManager downloadManager = null;
	
	
	private static DownloadManager getDownloadManager()
	{
		try
		{
			if (downloadManager == null)
			{
				// create download manager
				downloadManager = (DownloadManager) Constants.applicationContext.getSystemService(Context.DOWNLOAD_SERVICE);
				
				// create receiver
				BroadcastReceiver receiver = new BroadcastReceiver() 
				{
	            	@Override
		            public void onReceive(Context context, Intent intent) 
		            {
	            		handleDownloadComplete(intent);
			        };
				};
			     
				// register receiver
				Constants.applicationContext.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "getDownloadManager", e);
		}
		
		return downloadManager;
	}
	

	private static void handleDownloadComplete(Intent intent)
	{
		try
    	{
			if (Build.VERSION.SDK_INT < 9)
			{
				Constants.getLog().add(TAG, Type.Debug, "handleDownloadComplete exiting, API SDK 9 required but not available");
				return;
			}
				
            String action = intent.getAction();
            
            if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) 
            {
            	long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                int downloadStatus = getDownloadStatus(downloadId);
                
                
                switch (downloadStatus)
        		{
                	case DownloadManager.STATUS_SUCCESSFUL:
                		
                		String uriString = getDownloadLocalUri(downloadId);
                		Constants.getLog().add(TAG, Type.Debug, "Successfully downloaded: " + uriString);
  
                		DownloadQueueInfo downloadQueueInfo = Constants.getData().getDownloadQueueInfo(downloadId);

                		installApp(uriString, downloadQueueInfo.downloadID, downloadQueueInfo.silentInstall);

                		break;
                	
                	case DownloadManager.STATUS_FAILED:
                		
                		// remove failed download
                		removeDownload(downloadId);
                		
                		break;
                		
                	case DownloadManager.STATUS_PAUSED:
                	case DownloadManager.STATUS_PENDING:
                	case DownloadManager.STATUS_RUNNING:
                		break;
        		}
            }
    	}
    	catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "handleDownloadComplete", e);
		}
	}
	
	
	private static int getDownloadStatus(long downloadId)
	{
		int status = -1;
		
		try
    	{
			if (Build.VERSION.SDK_INT < 9)
			{
				Constants.getLog().add(TAG, Type.Debug, "handleDownloadComplete exiting, API SDK 9 required but not available");
				return status;
			}
			
            Query query = new Query();
            query.setFilterById(downloadId);
            Cursor cursor = getDownloadManager().query(query);
                
            if (cursor.moveToFirst())
            {
            	status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            }
            
            
            if (cursor != null)
            	cursor.close();
    	}
    	catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "getDownloadStatus", e);
		}
    	
    	return status;
	}
	
	
	private static String getDownloadLocalUri(long downloadId)
	{
		String localUri = "";
		
		try
    	{
			if (Build.VERSION.SDK_INT < 9)
			{
				Constants.getLog().add(TAG, Type.Debug, "handleDownloadComplete exiting, API SDK 9 required but not available");
				return localUri;
			}
			
			
            Query query = new Query();
            query.setFilterById(downloadId);
            Cursor cursor = getDownloadManager().query(query);
                
            if (cursor.moveToFirst())
            {
            	localUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
            }
            
            
            if (cursor != null)
            	cursor.close();
    	}
    	catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "getDownloadLocalUri", e);
		}
    	
    	return localUri;
	}
	

	public static void removeDownload(long downloadId)
	{
		try
		{
			// remove references if file cannot be found or read
			Constants.getData().deleteDownloadQueueInfo(downloadId);
			
			if (Build.VERSION.SDK_INT < 9)
			{
				Constants.getLog().add(TAG, Type.Debug, "removeDownload exiting, API SDK 9 required but not available");
				return;
			}
			
			
			getDownloadManager().remove(downloadId);
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "removeDownload", e);
		}
	}
	
	
	@SuppressWarnings("deprecation")
	public static void install(ApplicationAssignedInfo app, boolean silent)
	{
		try
		{	
			// clear notifications
			Notifications.remove(Notifications.Type.NOTIFY_APPLICATIONS_REQUIRED);
			
			
			// attempt to enable app with manufacturer API
			if (Constants.getDeviceManagementManufacturer() != null)
				Constants.getDeviceManagementManufacturer().enableApp(app);
			
			
			
			// Google Play apps
			if (app.filePath.toLowerCase().contains("play.google.com"))
			{
				// attempt to install with manufacturer api
				boolean success = false;
				
				if (Constants.getDeviceManagementManufacturer() != null)
					success = Constants.getDeviceManagementManufacturer().installAppGooglePlay(app.filePath);
				
				if (success)
				{
					Constants.getLog().add(TAG, Type.Debug, "Installed Google Play app with device manufacturer API: " + app.filePath);
					return;
				}
				
				
				if (!silent)
				{
					// launch market intent for application install
					Constants.getLog().add(TAG, Type.Info, "Launching Marketplace to install " + app.packageDisplayName);
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + app.packageName));
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					Constants.applicationContext.startActivity(intent);
				}
				
				return;
			}
			
		
			
			// download and install APK
			if (Build.VERSION.SDK_INT >= 9)
			{
				// check if file has already been downloaded
				boolean isAppDownloadingOrInstalling = false;
				DownloadQueueInfo downloadQueueInfo = Constants.getData().getDownloadQueueInfo(app.portalSysID);
				
				int downloadStatus = getDownloadStatus(downloadQueueInfo.downloadID);
				Constants.getLog().add(TAG, Type.Info, "Download status " + String.valueOf(downloadStatus) + " for DownloadID " + String.valueOf(downloadQueueInfo.downloadID));
			
                
                switch (downloadStatus)
        		{
                	case DownloadManager.STATUS_SUCCESSFUL:
                		
                		// check if file exists
            			String localFileUriString = getDownloadLocalUri(downloadQueueInfo.downloadID);
  
                		if (localFileUriString != null && localFileUriString.length() > 0)
                		{
                			// package has already been downloaded, attempt installation
	    					Constants.getLog().add(TAG, Type.Debug, "Detected apk has already been downloaded, installing app: " + app.packageName);
	    					installApp(localFileUriString, downloadQueueInfo.downloadID, silent);
	    					
	    					// set flag
	    					isAppDownloadingOrInstalling = true;
                		}
                		
                		// remove download if file cannot be found
                		else
                			removeDownload(downloadQueueInfo.downloadID);
                		
                		break;
                	
                	case DownloadManager.STATUS_FAILED:
                		break;
                		
                	case DownloadManager.STATUS_PAUSED:
                	case DownloadManager.STATUS_PENDING:
                	case DownloadManager.STATUS_RUNNING:
                		
                		// set flag if download is in progress
                		isAppDownloadingOrInstalling = true;
                		break;
        		}
                

                
				// package is already downloading or installing
				if (isAppDownloadingOrInstalling)
				{
					Constants.getLog().add(TAG, Type.Debug, "App is in the process of downloading or installing: " + app.packageName);
				}
				
				
				// package needs to be downloaded
				else
				{
					Constants.getLog().add(TAG, Type.Debug, "Detected apk has not been downloaded, initiating download: " + app.filePath);
					
					// DownloadManager cannot download from SSL connections (LAME!)
					app.filePath = app.filePath.replace("https://", "http://");
					
					Request request = new Request(Uri.parse(app.filePath));
					request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, app.getTempLocalFilename());
					request.setAllowedOverRoaming(false);
					request.setTitle(app.packageDisplayName);
					request.setDescription("via " + Constants.getLibrary().getClientPackageDisplayName());
					
					
					if (Build.VERSION.SDK_INT >= 11)
					{
						// Disabled - apparently there is a bug in Android which causes a security exception when if this is set
						//request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
					}
					else
						request.setShowRunningNotification(true);
					
					
					// add to queue
					long downloadId = getDownloadManager().enqueue(request);
					
					Constants.getLog().add(TAG, Type.Debug, "Added file URL to download queue for application installation: " + app.filePath);
					
					// save download id and silent install flag
					Constants.getData().insertDownloadQueueInfo(new DownloadQueueInfo(app.portalSysID, downloadId, silent));
					
					
				    // add TOAST message
					if (!silent)
					{
					    if (app.packageDisplayName != null && !app.packageDisplayName.equals(""))
					    	Toast.makeText(	Constants.applicationContext, 
					    					Constants.applicationContext.getResources().getStringArray(R.array.added_app_to_queue)[Constants.getApplicationBuildType().getValue()].replace("%APP_NAME%", app.packageDisplayName), 
					    					Toast.LENGTH_LONG).show();
					    else
					    	Toast.makeText(	Constants.applicationContext, 
					    					Constants.applicationContext.getResources().getStringArray(R.array.added_app_to_queue)[Constants.getApplicationBuildType().getValue()].replace("%APP_NAME%", "application"), 
					    					Toast.LENGTH_LONG).show();
					}
				}
			}
			
			// download file for manual installation
			else if (!silent)
			{
				Constants.getLog().add(TAG, Type.Debug, "Detected SDK version : " + String.valueOf(Build.VERSION.SDK_INT) + " which is not compatible with the Download Manager.");
				Constants.getLog().add(TAG, Type.Debug, "Downloading file for manual installation: " + app.filePath);
				
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(app.filePath));
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				Constants.applicationContext.startActivity(intent);
				
				// add TOAST message
			    if (app.packageDisplayName != null && !app.packageDisplayName.equals(""))
			    	Toast.makeText(	Constants.applicationContext, 
			    					Constants.applicationContext.getResources().getStringArray(R.array.downloading_app)[Constants.getApplicationBuildType().getValue()].replace("%APP_NAME%", app.packageDisplayName), 
			    					Toast.LENGTH_LONG).show();
			    else
			    	Toast.makeText(	Constants.applicationContext, 
			    					Constants.applicationContext.getResources().getStringArray(R.array.downloading_app)[Constants.getApplicationBuildType().getValue()].replace("%APP_NAME%", "application"), 
			    					Toast.LENGTH_LONG).show();
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "install", e);
		}
	}
	
	
	private static void installApp(String localApkFilePath, long downloadId, boolean silent)
	{
		try
		{
			// attempt to install downloaded APK with manufacturer API
			boolean success = false;
			
			if (Constants.getDeviceManagementManufacturer() != null)
				success = Constants.getDeviceManagementManufacturer().installAppLocalApk(localApkFilePath);
			
			if (success)
			{
				Constants.getLog().add(TAG, Type.Debug, "Installed app with device manufacturer API: " + localApkFilePath);
				
				// remove download after success
        		removeDownload(downloadId);
        		
				return;
			}
    		
			
    		// open APK to prompt user for installation using Android SDK
			if (!silent)
			{
				Constants.getLog().add(TAG, Type.Debug, "Launching intent for user to install app: " + localApkFilePath);
				
				Intent launchIntent = new Intent(Intent.ACTION_VIEW);
				launchIntent.setDataAndType(Uri.parse(localApkFilePath), "application/vnd.android.package-archive");
				launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				Constants.applicationContext.startActivity(launchIntent);
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "installApp", e);
		}
	}
	

	public static void uninstall(String packageIdentifier)
	{
		try
		{
			// attempt to uninstall with manufacturer API
			boolean success = false;
			
			if (Constants.getDeviceManagementManufacturer() != null)
				success = Constants.getDeviceManagementManufacturer().uninstallApp(packageIdentifier);
			
			if (success)
			{
				Constants.getLog().add(TAG, Type.Debug, "Uninstalled app with device manufacturer API: " + packageIdentifier);
				return;
			}
			
			
			// uninstall using Android SDK
			
			Constants.getLog().add(TAG, Type.Info, "Prompting to uninstall " + packageIdentifier);
			Uri packageUri = Uri.parse("package:" + packageIdentifier);
			Intent intent = new Intent(Intent.ACTION_DELETE, packageUri);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			Constants.applicationContext.startActivity(intent);
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "uninstall", e);
		}
	}
}
