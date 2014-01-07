package com.quintech.connect.activities;


import java.util.ArrayList;
import java.util.List;

import com.quintech.common.AbstractLibrary;
import com.quintech.common.ApplicationAssignedInfo;
import com.quintech.common.ApplicationInstalledInfo;
import com.quintech.common.ILog.Type;
import com.quintech.connect.*;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

public class ApplicationsActivity extends ListActivity 
{
	private static String TAG = "ApplicationsActivity";
	
	
	public void onCreate(Bundle savedInstanceState) 
	{
		 try
		 {
			 super.onCreate(savedInstanceState);

			 setContentView(R.layout.applications);

			 // set ListView properties
			 ListView lv = getListView();
			 lv.setTextFilterEnabled(false);
			 
			 lv.setOnItemClickListener(new OnItemClickListener() 
			 {			
				 @Override
				 public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
				 {
					 handleItemClick (parent, view, position, id);	
				 }
			 });
			
			 
			 // set text
			 TextView textViewNoAssignedApps = (TextView) findViewById(R.id.textViewNoAssignedApps);
			 textViewNoAssignedApps.setText(getResources().getStringArray(R.array.no_assigned_apps)[Constants.getApplicationBuildType().getValue()]);
			 
			 // load list of assigned applications
			 refreshUI();
		 }
		 
		 catch (Exception e)
		 {
			 Constants.getLog().add(TAG, Type.Error, "onCreate", e);
		 }
    }
	
	
	@Override
    protected void onNewIntent(Intent intent) 
    {
    	try
    	{
	    	super.onNewIntent(intent);
	    	
	    	Constants.getLog().add(TAG, Type.Verbose, "Received onNewIntent");
	    	
	    	// refresh UI
	    	refreshUI();
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "onNewIntent", e);
    	}
    }
	
	
	@Override 
	public void onWindowFocusChanged(boolean hasFocus)
	{
		try
    	{
	    	super.onWindowFocusChanged(hasFocus);
	    	
	    	Constants.getLog().add(TAG, Type.Verbose, "Received onWindowFocusChanged");
	    	
	    	// refresh UI
	    	refreshUI();
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "onWindowFocusChanged", e);
    	}
	}
	
	
	private void loadApplicationlist()
	{
		try
		{
			// get assigned application list
			List<ApplicationAssignedInfo> listAssignedApps = Constants.getData().getAssignedApplications();
			
			// set flag if application is currently installed, include system packages
			List<ApplicationInstalledInfo> listInstalledApps =	Constants.getLibrary().getInstalledApplications(true);
			
			for (ApplicationAssignedInfo assignedApp : listAssignedApps)
			{
				boolean isInstalled = false;
				
				for (ApplicationInstalledInfo installedApp : listInstalledApps)
				{
					if (!assignedApp.packageName.equalsIgnoreCase(installedApp.packageName))
						continue;
					
					// check that installed version is greater or equal to the assigned version
					isInstalled = AbstractLibrary.isInstalledSoftwareVersionCurrent(installedApp.version, assignedApp.version);	
					
					break;
				}
				
				
				// set flag
				assignedApp.isInstalled = isInstalled;
			}
			
			
			// sort assigned list in order of Mandatory (not installed), Non-Mandatory (not installed), Installed
			List<ApplicationAssignedInfo> listAssignedAppsSorted = new ArrayList<ApplicationAssignedInfo>();
			
			// add un-installed mandatory items
			for (ApplicationAssignedInfo assignedApp : listAssignedApps)
				if (!assignedApp.isInstalled && assignedApp.mandatory)
					listAssignedAppsSorted.add(assignedApp);
			
			// add un-installed non-mandatory items
			for (ApplicationAssignedInfo assignedApp : listAssignedApps)
				if (!assignedApp.isInstalled && !assignedApp.mandatory)
					listAssignedAppsSorted.add(assignedApp);
			
			// add installed items
			for (ApplicationAssignedInfo assignedApp : listAssignedApps)
				if (assignedApp.isInstalled)
					listAssignedAppsSorted.add(assignedApp);
			

			// load list of assigned applications
			setListAdapter(new ApplicationListAdapter(listAssignedAppsSorted));
		}
		catch (Exception e)
		{
			 Constants.getLog().add(TAG, Type.Error, "loadApplicationlist", e);
		}
	}
	
	
	public void handleItemClick(AdapterView<?> parent, View view, int position, long id) 
	{
		try
		{
			// get selected item
			final ApplicationAssignedInfo app = ((ApplicationListAdapter)parent.getAdapter()).getItem(position);
			
			// if app is already installed
			if (app.isInstalled)
			{
				// prompt user to launch or uninstall
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage(app.packageDisplayName)
				       .setCancelable(true)
				       .setPositiveButton(getResources().getStringArray(R.array.launch)[Constants.getApplicationBuildType().getValue()], new DialogInterface.OnClickListener() 
				       {
				           public void onClick(DialogInterface dialog, int id) 
				           {
				        	   	try
				        	   	{
					        	   	Constants.getLog().add(TAG, Type.Info, getResources().getStringArray(R.array.launching)[Constants.getApplicationBuildType().getValue()] + " " + app.packageDisplayName);
									Intent intent = getPackageManager().getLaunchIntentForPackage(app.packageName);
									startActivity(intent);
				        	   	}
				        	   	catch (Exception e)
					           	{
					       			Constants.getLog().add(TAG, Type.Error, "EventHandler", e);
					           	}
				           }
				       })
				       .setNegativeButton(getResources().getStringArray(R.array.uninstall)[Constants.getApplicationBuildType().getValue()], new DialogInterface.OnClickListener() 
				       {
				           public void onClick(DialogInterface dialog, int id) 
				           {
				        	   	try
				        	   	{
				        		   ApplicationInstallation.uninstall(app.packageName);
				        	   	}
				        	   	catch (Exception e)
					           	{
					       			Constants.getLog().add(TAG, Type.Error, "EventHandler", e);
					           	}
				           }
				       });
				AlertDialog alert = builder.create();
				alert.show();
			}
			
			// otherwise install selected app
			else
			{
				ApplicationInstallation.install(app, false);
			}
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "handleItemClick", e);
    	}
	}

	
	public void refreshUI()
	{
		try
		{
			// reload list
			loadApplicationlist();
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "refreshUI", e);
    	}
	}
}
