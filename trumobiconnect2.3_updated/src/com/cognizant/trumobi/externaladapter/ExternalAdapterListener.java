package com.cognizant.trumobi.externaladapter;

/**
 *  FileName : ExternalAdapterListener
 * 
 *  Desc : 
 * 
 * 
 *  KeyCode 				Author				Date						Desc
 * 
 */

public interface ExternalAdapterListener {
	
	boolean onExternalAdapterRegistered(boolean bRegistered);
	void onExternalAdapterFailed(String error,int errorCode);

}
