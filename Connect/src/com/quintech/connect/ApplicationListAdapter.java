package com.quintech.connect;

import java.util.List;

import com.quintech.common.ILog.Type;
import com.quintech.common.ApplicationAssignedInfo;


import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ApplicationListAdapter extends BaseAdapter 
{
	private static String TAG = "ApplicationListAdapter";
    private List<ApplicationAssignedInfo> resultList;
 
    
    public ApplicationListAdapter(List<ApplicationAssignedInfo> resultList) 
    {
        this.resultList = resultList;
    }
 
    
    public int getCount() 
    {
    	int count = 0;
    	
    	try
    	{
	    	if (resultList != null)
	    		count = resultList.size();
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "getCount", e);
    	}
    	
    	return count;
    }
 
    
    public ApplicationAssignedInfo getItem(int position) 
    {
    	ApplicationAssignedInfo app = null;
    	
    	try
    	{
	    	if (resultList != null)
	    		app = resultList.get(position);
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "getItem", e);
    	}
    	
    	return app;
    }
 
    
    public long getItemId(int position) 
    {
    	long id = 0;
    	
    	try
    	{
    		if (resultList != null)
    			id = resultList.get(position).hashCode();
    	}
    	catch (Exception e)	
    	{
    		Constants.getLog().add(TAG, Type.Error, "getItemId", e);
    	}
    	
    	return id;
    }
 
    
    public View getView(int position, View convertView, ViewGroup parent) 
    {
    	LinearLayout itemLayout = null;
    	
    	try
    	{
    		ApplicationAssignedInfo app = getItem(position);
    		
	        itemLayout = (LinearLayout) LayoutInflater.from(Constants.applicationContext).inflate(R.layout.list_item_application, parent, false);
	        
	        TextView textViewStatus = (TextView)itemLayout.findViewById(R.id.TextView_status);
	        TextView textViewName = (TextView)itemLayout.findViewById(R.id.TextView_name);
	        TextView textViewIdentifier = (TextView)itemLayout.findViewById(R.id.TextView_identifier);
	        TextView textViewVersion = (TextView)itemLayout.findViewById(R.id.TextView_version);
	        TextView textViewInstall = (TextView)itemLayout.findViewById(R.id.TextView_install);
	        
	        // un-installed and mandatory
	        if (!app.isInstalled && app.mandatory)
	        {
	        	textViewStatus.setText("REQUIRED");
	        	textViewStatus.setBackgroundColor(Color.rgb(174, 0, 0));
	        }
	        
	        // un-installed and non-mandatory
	        else if (!app.isInstalled && !app.mandatory)
	        {
	        	textViewStatus.setText("AVAILABLE");
	        	textViewStatus.setBackgroundColor(Color.rgb(100, 100, 100));
	        }
	        
	        else if (app.isInstalled)
	        {
	        	textViewStatus.setText("INSTALLED");
	        	textViewStatus.setBackgroundColor(Color.rgb(180, 180, 180));
	        	textViewInstall.setVisibility(View.GONE);
	        }
	        
	        
	        // set values
	        textViewName.setText(app.packageDisplayName);
	    	textViewIdentifier.setText(app.packageName);
	    	textViewVersion.setText("Version " + app.version);
	    	textViewInstall.setText(Constants.applicationContext.getResources().getStringArray(R.array.tap_to_install)[Constants.getApplicationBuildType().getValue()]);
	    	
	    	
	    	if (app.version == null || app.version.equals(""))
	    		textViewVersion.setVisibility(View.GONE);
	    	
	    	
	    	// TODO
	    	// set app icon if available?
	    	
    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "getView", e);
    	}
 
        return itemLayout;
    }
}