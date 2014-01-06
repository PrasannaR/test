package com.quintech.connect;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

import com.quintech.common.AbstractData.Settings;
import com.quintech.common.ILog.Type;

public class LocationUtility 
{
	private static String TAG = "LocationUtility";
	
	private static boolean inProgress = false;
	private static Location resultLocation = null;
	private static LocationManager locationManager = null;
	private static LocationListener locationListener = null;
	private static Timer timerStopListener = null;
	
	private static int LOCATION_REQUIREMENT_MIN_LISTENER_DURATION_SECONDS = 40;
	public static int LOCATION_REQUIREMENT_NEWER_THAN_MINUTES = 15;
	
	
	
	public static float getMinimumLocationAccuracyMeters()
	{
		// set default value
		float meters = 80.0f;
		
		try
		{
			meters = Float.parseFloat(Constants.getData().getSetting(Settings.SET_MinimumLocationAccuracyMeters));
		}
		catch (Exception e) 
	    {
			Constants.getLog().add(TAG, Type.Error, "getMinimumLocationAccuracyMeters", e);
	    }
		
		return meters;
	}
	
	
	public static String getRequiredProvider()
	{
		// default value
		String requiredProvider = LocationManager.GPS_PROVIDER;
		
		try
		{
			Criteria criteria = new Criteria();
	    	criteria.setAccuracy(Criteria.ACCURACY_FINE); 
	    	
			LocationManager locationManager = (LocationManager)Constants.applicationContext.getSystemService(Context.LOCATION_SERVICE);
			requiredProvider = locationManager.getBestProvider(criteria, true);
		}
		catch (Exception e) 
	    {
			Constants.getLog().add(TAG, Type.Error, "getRequiredProvider", e);
	    }
		
		return requiredProvider;
	}
	
	
	public static boolean isInProgress()
	{
		return inProgress;
	}
	
	
	public static Location getResultLocation()
	{
		return resultLocation;
	}
	
	
	public static int getDefaultListenerDurationSeconds()
	{
		return LOCATION_REQUIREMENT_MIN_LISTENER_DURATION_SECONDS;
	}
	
	
	private static LocationManager getLocationManager()
	{
		if (locationManager == null)
			 locationManager = (LocationManager)Constants.applicationContext.getSystemService(Context.LOCATION_SERVICE);
		
		return locationManager;
	}
	
	
	public static boolean isLocationSatisfactory(android.location.Location location)
	{
		boolean result = false;
		
		try
		{
			if (location == null)
				return false;
			
			// check when last known location was retrieved
			long diffMinutes = ((System.currentTimeMillis() - location.getTime()) / 1000) / 60;
			Constants.getLog().add(TAG, Type.Debug, "Location retrieved " + String.valueOf(diffMinutes) + " minutes ago");

	        if (diffMinutes > LocationUtility.LOCATION_REQUIREMENT_NEWER_THAN_MINUTES)
	        {
	        	Constants.getLog().add(TAG, Type.Debug, "Location not being reported, older than " + String.valueOf(LocationUtility.LOCATION_REQUIREMENT_NEWER_THAN_MINUTES) + " minutes");
	        	return false;
	        }
	        
	        
	        // require accuracy within X meters
        	if (location.getAccuracy() == 0.0 || location.getAccuracy() > getMinimumLocationAccuracyMeters())
        	{
        		Constants.getLog().add(TAG, Type.Debug, "Location not being reported, accuracy (" + String.valueOf(location.getAccuracy()) + "m) greater than minimum required (" + String.valueOf(getMinimumLocationAccuracyMeters()) + "m)");
        		return false;
        	}
        	
        	
        	// valid result
        	Constants.getLog().add(TAG, Type.Debug, "Retrieved satisfactory location with accuracy (" + String.valueOf(location.getAccuracy()) + "m)");
        	result = true;
		}
		catch (Exception e) 
	    {
			Constants.getLog().add(TAG, Type.Error, "isLocationSatisfactory", e);
	    }
		
		return result;
	}
	
	
	private static LocationListener getLocationListener()
	{
		if (locationListener == null)
		{
			// create listener to get updated location
        	locationListener = new LocationListener() 
        	{
        	    public void onLocationChanged(Location location) 
        	    {
        	    	try
        	    	{
	        	    	// update newLocation value whenever most recent value is more accurate
	        	    	if (isBetterLocation(location, resultLocation))
	        	    		resultLocation = location;
	        	    	
	        	    	Constants.getLog().add(TAG, Type.Verbose, "Received updated location with accuracy: " + String.valueOf(location.getAccuracy()));
	        	    	
	        	    	// stop listening after getting a satisfactory result
	        	    	if (isLocationSatisfactory(resultLocation))
	        	    	{
	        	    		Constants.getLog().add(TAG, Type.Verbose, "Retrieved satisfactory new location, stopping listener");
	        	    		stopListener();
	        	    	}
        	    	}
        	    	catch (Exception e) 
        		    {
        				Constants.getLog().add(TAG, Type.Error, "onLocationChanged", e);
        		    }
        	    }
        	    
        	    public void onStatusChanged(String provider, int status, Bundle extras) {}
        	    public void onProviderEnabled(String provider) {}
        	    public void onProviderDisabled(String provider) {}
        	};
		}
		
		
		return locationListener;
	}
	
	
	public static void updateLocation()
	{
		// use default value
		updateLocation(getDefaultListenerDurationSeconds());
	}
	
	
	public static void updateLocation(int listenerDurationSeconds)
	{
		try
		{
        	// if there is not already an active call
        	if (!inProgress)
        	{
        		inProgress = true;
        		
        		// reset static variable
        		resultLocation = null;
        		
        		// prepare looper
        		Looper.prepare();

        		
	        	// register the listener for enabled providers, set flag
	        	if (getLocationManager().isProviderEnabled(LocationManager.NETWORK_PROVIDER))
	        		getLocationManager().requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, getLocationListener());
	        	
	        	if (getLocationManager().isProviderEnabled(LocationManager.GPS_PROVIDER))
	        		getLocationManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, getLocationListener());
	        	
	        	Constants.getLog().add(TAG, Type.Debug, "Started listening for new location");
	        	
	        	// reset timer to stop listener after set amount of time
	        	restartTimer(listenerDurationSeconds);
	        	
	        	Looper.loop();
	        	Looper.myLooper().quit();
        	}
        	
        	else
        		Constants.getLog().add(TAG, Type.Debug, "Location update already in progress");
		}
		catch (Exception e) 
	    {
			Constants.getLog().add(TAG, Type.Error, "updateLocation", e);
	    }
	}
	
	
	private static void stopListener()
	{
		try
		{
			// stop timer
			timerStopListener.cancel();
			
			// stop location listeners
			getLocationManager().removeUpdates(getLocationListener());
			
			Constants.getLog().add(TAG, Type.Debug, "Stopped listening for new location");
		}
		catch (Exception e) 
	    {
			Constants.getLog().add(TAG, Type.Error, "stopListener", e);
	    }
		finally
		{
			// reset flag
			inProgress = false;
		}
	}
	
	
	private static void restartTimer(int listenerDurationSeconds)
	{
		// reset timer to continuously update the session end time
		try
		{
			// cancel previous timer
			if (timerStopListener != null)
				timerStopListener.cancel();
			
				
			// create a new timer
			timerStopListener = new Timer();
			
			// create schedule
			timerStopListener.scheduleAtFixedRate(new TimerTask() 
												{
													@Override
													public void run() 
													{
														stopListener();
													}
												}, listenerDurationSeconds * 1000, listenerDurationSeconds * 1000);
			
			Constants.getLog().add(TAG, Type.Debug, "Restarted stop listener timer");
		}
		catch (Exception e)
    	{
			Constants.getLog().add(TAG, Type.Error, "restartTimer", e);
    	}
	}
	
	
	private static boolean isBetterLocation(Location location, Location currentBestLocation) 
	{
		try
		{
			/** Determines whether one Location reading is better than the current Location fix
			  * @param location  The new Location that you want to evaluate
			  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
			  */
			
		    if (currentBestLocation == null) 
		    {
		        // A new location is always better than no location
		        return true;
		    }
	
		    // Check whether the new location fix is more or less accurate
		    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		    boolean isLessAccurate = accuracyDelta > 0;
		    boolean isMoreAccurate = accuracyDelta < 0;
		    boolean isSignificantlyLessAccurate = accuracyDelta > 200;
	
		    // Check if the old and new location are from the same provider
		    boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());
	
		    // Determine location quality using a combination of timeliness and accuracy
		    if (isMoreAccurate) {
		        return true;
		    } else if (!isLessAccurate) {
		        return true;
		    } else if (!isSignificantlyLessAccurate && isFromSameProvider) {
		        return true;
		    }
		    
		    return false;
		}
		catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "isBetterLocation", e);
    	}
		
		return false;
	}
	
	
	private static boolean isSameProvider(String provider1, String provider2) 
	{
		try
		{
			/** Checks whether two providers are the same */
		    if (provider1 == null) {
		      return provider2 == null;
		    }
		    return provider1.equals(provider2);
		}
		catch (Exception e)
    	{
    		Constants.getLog().add(TAG, Type.Error, "isSameProvider", e);
    	}
		 
		return false;
	}
	
}
