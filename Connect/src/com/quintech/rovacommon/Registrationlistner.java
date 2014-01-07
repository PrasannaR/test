package com.quintech.rovacommon;

import android.os.Message;


public interface Registrationlistner {
	boolean onRegistered(Message msg);
	//boolean onRegistered(boolean bRegistered);
	
	void OnFailed(String error, int errorCode);
}
