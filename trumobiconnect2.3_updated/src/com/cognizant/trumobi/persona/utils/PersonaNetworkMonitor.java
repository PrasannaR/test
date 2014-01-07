package com.cognizant.trumobi.persona.utils;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

public class PersonaNetworkMonitor {

	private static PersonaNetworkMonitor personaNetworkMonitor;
	boolean firstlaunch = true;
	Context context;
	/**
	 * Creates a network monitor instance
	 */
	private PersonaNetworkMonitor() {
		
		try {
			isAcivated(context);
		} catch (Exception e) {
			// PersonaLog.e(getClass().getName(), PersonaApplicationConstants.EXCEPTION, e);
		}
	}

	/**
	 * Returns a personaNetworkMonitor instance
	 * 
	 * @return PersonaNetworkMonitor object
	 */
	public static PersonaNetworkMonitor getNetworkMonitor() {
		if (personaNetworkMonitor == null) {
			personaNetworkMonitor = new PersonaNetworkMonitor();
		}
		return personaNetworkMonitor;
	}

	public boolean isAcivated(Context context) {
		// Creating Connection manager.
		//System.out.println("==========context---"+context);
		try {
	
			return(isWifiAvilable(context)||is3GAvilable(context));
		    }

		catch (Exception ex) {
			 //PersonaLog.e(PersonaApplicationConstants.EXCEPTION,context.getResources().getString(R.string.network_not_found));
			//ex.printStackTrace();
			return false;
		}
		
	}
	@SuppressWarnings("unused")
	private boolean defaultCheck(){
		return true;
	}
	public static String getNetworkProvider(Context context) {
		String networkProvider = null;
		try {
			TelephonyManager telephonyManager;
			telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
			networkProvider = telephonyManager.getNetworkOperatorName();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return networkProvider;
	}
	

	
	private boolean isWifiAvilable(Context context) {
		final ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo wifi = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifi.isAvailable() && wifi.isConnected()) {
			return true;
		}
		return false;
	}
	

	private boolean is3GAvilable(Context context) {
		final ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (mobile.isAvailable() && mobile.isConnected()) {
			return true;
		}
		return false;
	}
	
}
