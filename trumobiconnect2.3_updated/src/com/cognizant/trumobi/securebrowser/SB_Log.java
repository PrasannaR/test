package com.cognizant.trumobi.securebrowser;

import android.util.Log;

public class SB_Log {
	public static boolean DEBUG_LOG=true;
public static void sbI(String tag,String msg){
	if(DEBUG_LOG){
		Log.i(tag,msg);
	}
}
public static void sbD(String tag,String msg){
	if(DEBUG_LOG){
		Log.d(tag, msg);
		}
}
public static void sbE(String tag,String msg){
	if(DEBUG_LOG){
	Log.e(tag,msg);
	}
}
public static void sbV(String tag,String msg){
	if(DEBUG_LOG){
	Log.v(tag,msg);
	}
}
public static void sbW(String tag,String msg){
	if(DEBUG_LOG){
	Log.w(tag,msg);
	}
}
}
