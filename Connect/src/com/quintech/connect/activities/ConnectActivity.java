package com.quintech.connect.activities;
	 
import com.quintech.common.AbstractData.Flags;
import com.quintech.common.ILog.Type;
import com.quintech.common.SmartConnectAllowedConnectionTypes.ConnectionType;
import com.quintech.connect.*;
import com.quintech.connect.SmartConnect.RankedScanResultItem;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


public class ConnectActivity extends Activity 
{
	private static String TAG = "ConnectActivity";
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		try
		{
			super.onCreate(savedInstanceState);

	        setContentView(R.layout.connect);
	        

	         
	        // create listener
	        final Button buttonEnableWiFi = (Button) findViewById(R.id.ButtonEnableWiFi);
	        buttonEnableWiFi.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v) 
				{
					try
					{
						if (!buttonEnableWiFi.isEnabled())
							return;
						
						// enable WiFi
						Library.enableWiFi(buttonEnableWiFi.getContext());
						
						// disable button
						buttonEnableWiFi.setEnabled(false);
						buttonEnableWiFi.setText(getResources().getStringArray(R.array.enabling_wifi)[Constants.getApplicationBuildType().getValue()]);
					}
					catch (Exception e)
		           	{
		       			Constants.getLog().add(TAG, Type.Error, "EventHandler", e);
		           	}
				}
			});
			
			
	        // create listener
	        final Button buttonConnect = (Button) findViewById(R.id.ButtonConnect);
			buttonConnect.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v) 
				{
					try
					{
						if (!buttonConnect.isEnabled())
							return;
						
						// attempt connection
						Intent intent =	new Intent(buttonConnect.getContext(), ApplicationService.class);
						intent.setAction(ApplicationService.Action.SMART_CONNECT);
						buttonConnect.getContext().startService(intent);
						
						// disable button
						buttonConnect.setEnabled(false);
						buttonConnect.setText(getResources().getStringArray(R.array.connecting)[Constants.getApplicationBuildType().getValue()]);
					}
					catch (Exception e)
		           	{
		       			Constants.getLog().add(TAG, Type.Error, "EventHandler", e);
		           	}
				}
			});
			
			
			// create listener
			final Button buttonDisconnect = (Button) findViewById(R.id.ButtonDisconnect);
			buttonDisconnect.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v) 
				{
					try
					{
						if (!buttonDisconnect.isEnabled())
							return;
						
						// disable smart connect if it is enabled
						if (Constants.getData().getFlag(Flags.FLG_AutoConnect))
							Constants.getData().setFlag(Flags.FLG_AutoConnect, false, false);
						
						// disconnect
						Intent intent =	new Intent(buttonDisconnect.getContext(), ApplicationService.class);
						intent.setAction(ApplicationService.Action.DISCONNECT);	
						buttonDisconnect.getContext().startService(intent);
						
						// disable button
						buttonDisconnect.setEnabled(false);
						buttonDisconnect.setText(getResources().getStringArray(R.array.disconnecting)[Constants.getApplicationBuildType().getValue()]);
					}
					catch (Exception e)
		           	{
		       			Constants.getLog().add(TAG, Type.Error, "EventHandler", e);
		           	}
				}
			});
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "onCreate", e);
		}
	}
	
	
	@Override
	protected void onStart()
	{
		super.onStart();
		
		try
		{
			// call Library function to perform probe test and refresh
			Constants.getLibrary().refreshUI();
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "onStart", e);
    	}
	}
	
	
	@Override
    protected void onNewIntent(Intent intent) 
    {
    	try
    	{
	    	super.onNewIntent(intent);
	    	

    		// call Library function to perform probe test and refresh
			Constants.getLibrary().refreshUI();

    	}
    	catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "onNewIntent", e);
    	}
    }
	
	
	public void refreshUI(boolean isAuthenticated)
	{
		try
		{
			final Button buttonConnect = (Button) findViewById(R.id.ButtonConnect);
	        final Button buttonDisconnect = (Button) findViewById(R.id.ButtonDisconnect);
	        final Button buttonEnableWiFi = (Button) findViewById(R.id.ButtonEnableWiFi);
	        final TextView textViewMessage = (TextView) findViewById(R.id.TextViewMessage);
	        final TextView textViewSSID = (TextView) findViewById(R.id.TextViewSSID);
	        
	        // set default states
	        buttonEnableWiFi.setVisibility(View.GONE);
	        buttonEnableWiFi.setEnabled(false);
	        
			buttonDisconnect.setVisibility(View.GONE);
			textViewMessage.setVisibility(View.INVISIBLE);
			textViewSSID.setVisibility(View.INVISIBLE);
			
			buttonConnect.setVisibility(View.GONE);
			buttonConnect.setEnabled(false);
			buttonConnect.setText(getResources().getStringArray(R.array.connect)[Constants.getApplicationBuildType().getValue()]);
			
			
			// check if wifi is enabled
	        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	        
	        if (wifi.getWifiState() !=  WifiManager.WIFI_STATE_ENABLED)
	        {
	        	// show message
	        	textViewMessage.setText(getResources().getStringArray(R.array.wifi_not_enabled)[Constants.getApplicationBuildType().getValue()]);
				textViewMessage.setVisibility(View.VISIBLE);
				
				// show and enable wifi button
				buttonEnableWiFi.setText(getResources().getStringArray(R.array.enable_wifi)[Constants.getApplicationBuildType().getValue()]);
				buttonEnableWiFi.setVisibility(View.VISIBLE);
		        buttonEnableWiFi.setEnabled(true);
		        
	        	return;
	        }
	        
	        else
	        {
	        	// set Connect button to show as default if smart connect is disabled
	        	if (!Constants.getData().getFlag(Flags.FLG_AutoConnect))
	        		buttonConnect.setVisibility(View.VISIBLE);
	        }
	        
	        
	        
	        // get current connection info
	        ConnectivityManager connManager = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	        

	        // check for Wi-Fi connecting, Smart Connect in progress, or WISPr authenticating states
			if ((networkInfo != null && !networkInfo.isConnected() && networkInfo.isConnectedOrConnecting()) ||
					Constants.getSmartConnect().getIsAttemptInProgress() ||
					Constants.isWISPrInProgress)
			{
				// set button properties if smart connect is disabled
				if (!Constants.getData().getFlag(Flags.FLG_AutoConnect))
				{
					buttonConnect.setText(getResources().getStringArray(R.array.connecting)[Constants.getApplicationBuildType().getValue()]);
					buttonConnect.setVisibility(View.VISIBLE);
					buttonConnect.setEnabled(false);
				}
				
				// set connecting message
				textViewMessage.setText(Constants.applicationContext.getResources().getStringArray(R.array.connecting)[Constants.getApplicationBuildType().getValue()]);
				textViewMessage.setVisibility(View.VISIBLE);
				
				return;
			}
			
	        // check if we're associated to a hotspot in the directory
	        boolean isAssociated = false;

			if (networkInfo != null && 
					networkInfo.isConnected() &&
					Constants.getData().isSSIDInDirectory(wifi.getConnectionInfo().getSSID(), ConnectionType.Unknown) != null)
			{
				isAssociated = true;
			}
		
			  
	        
	        // if connected
			if (isAssociated && isAuthenticated)
			{
				// enable disconnect button if smart connect is disabled
				if (!Constants.getData().getFlag(Flags.FLG_AutoConnect))
				{
					buttonDisconnect.setText(getResources().getStringArray(R.array.disconnect)[Constants.getApplicationBuildType().getValue()]);
					buttonDisconnect.setVisibility(View.VISIBLE);
					buttonDisconnect.setEnabled(true);
				}
				else
				{
					// show text to disable Smart Connect (and disconnect)
					buttonDisconnect.setText(getResources().getStringArray(R.array.disable_smart_connect)[Constants.getApplicationBuildType().getValue()]);
					buttonDisconnect.setVisibility(View.VISIBLE);
					buttonDisconnect.setEnabled(true);
				}
				
				// hide connect button
				buttonConnect.setVisibility(View.GONE);
				
				
				WifiManager wifiMan = (WifiManager)getSystemService(Context.WIFI_SERVICE);
				WifiInfo wifiInfo = wifiMan.getConnectionInfo();
				String ssid = wifiInfo.getSSID();
				
				// set network name
				textViewSSID.setText(ssid);
				textViewSSID.setVisibility(View.VISIBLE);
				
				// set textview to Connected
				textViewMessage.setText(Constants.applicationContext.getResources().getStringArray(R.array.connected_network)[Constants.getApplicationBuildType().getValue()]);
				textViewMessage.setVisibility(View.VISIBLE);
			}
	        
			else
			{
				// check if there is an available SSID in the scan list
				RankedScanResultItem item = Constants.getSmartConnect().getConnectRankedScanResultItem();
				
				// enable connect button
				if (item != null && item.scanResult != null)
				{
					if (!Constants.getData().getFlag(Flags.FLG_AutoConnect))
					{
						buttonConnect.setText(getResources().getStringArray(R.array.connect)[Constants.getApplicationBuildType().getValue()]);
						buttonConnect.setVisibility(View.VISIBLE);
						buttonConnect.setEnabled(true);
					}

					
					// set network name
					textViewSSID.setText(item.scanResult.SSID);
					textViewSSID.setVisibility(View.VISIBLE);
					
					// set available network message
					textViewMessage.setText(Constants.applicationContext.getResources().getStringArray(R.array.available_network)[Constants.getApplicationBuildType().getValue()]);
					textViewMessage.setVisibility(View.VISIBLE);
				}
				
				// if no networks are found, and there are attempted Smart Connect items
				else if (Constants.getSmartConnect().getAttemptedItemsCount() > 0)
				{
					// enable Connect button to allow user to manually re-attempt Smart Connect, set button text to "Retry"
					buttonConnect.setText(getResources().getStringArray(R.array.retry)[Constants.getApplicationBuildType().getValue()]);
					buttonConnect.setVisibility(View.VISIBLE);
					buttonConnect.setEnabled(true);
					
					// show not networks available message
					textViewMessage.setText(getResources().getStringArray(R.array.not_in_range)[Constants.getApplicationBuildType().getValue()]);
					textViewMessage.setVisibility(View.VISIBLE);
				}
				
				else
				{
					// show not networks available message
					textViewMessage.setText(getResources().getStringArray(R.array.not_in_range)[Constants.getApplicationBuildType().getValue()]);
					textViewMessage.setVisibility(View.VISIBLE);
				}
			}
		}
		catch (Exception e)
		{
			Constants.getLog().add(TAG, Type.Error, "refreshUI", e);
		}
	}
}
	 
