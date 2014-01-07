package com.cognizant.trumobi.persona.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import android.content.Context;
import android.hardware.Camera;


public class PersonaMDMHashTable {
	
	private static PersonaMDMHashTable sMDMHashTable = new PersonaMDMHashTable();
	String LOG_CAT=PersonaMDMHashTable.class.getSimpleName();
	static PersonaMDMHashTable personaMDMHashTable;
	public static Hashtable<String, String> hashTable= new Hashtable<String, String>();
	public static ArrayList<String> email_initiate= new ArrayList<String>();
	public static ArrayList<String> mDMPush=new ArrayList<String>();
	public static String mHashUid, mHashMessage, mHashEmailId;
	public static int mHashCertificateno ;
	static Context mHashContext; 
	public static String upgrade;
	public static String profileName;
	
	public PersonaMDMHashTable() {


}
	public static PersonaMDMHashTable getInstance(Context context) {
		mHashContext = context;
		return sMDMHashTable;
	}
	
	public static enum MDMFeature {
	   Sms_Block,Sms_Unblock,Email_Block,Email_Unblock,Browser_Block,Browser_Unblock,Camera_Block,Camera_Unblock,;
	}
	
	public static enum DeviceManufracturer {
		HTC,SAMSUNG,LG,SONY
		}
 
	
}
