package com.cognizant.trumobi.externaladapter;

import android.os.Message;

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
