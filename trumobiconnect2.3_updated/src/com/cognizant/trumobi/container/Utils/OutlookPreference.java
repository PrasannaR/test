package com.cognizant.trumobi.container.Utils;

import com.TruBoxSDK.SharedPreferences;
import com.TruBoxSDK.TruBoxContentEncryption;
import com.TruBoxSDK.TruboxException;

import android.content.Context;
import android.util.Base64;

public class OutlookPreference {

	private static OutlookPreference mOutlookPreference = null;
	private SharedPreferences mSharedPreferences;
	private SharedPreferences.Editor mEditor;
	//TruBoxContentEncryption nBoxContentEncryption;

	public static OutlookPreference getInstance(Context context) {
		if (mOutlookPreference == null) {
			mOutlookPreference = new OutlookPreference(context);
		}
		return mOutlookPreference;
	}

	protected OutlookPreference(Context context) {
		/*this.mSharedPreferences = context.getSharedPreferences(
				Constants.PREF_APP_SHARED_PREFS, Context.MODE_PRIVATE);
		this.mEditor = mSharedPreferences.edit();*/

		/*if (nBoxContentEncryption == null)
			nBoxContentEncryption = new TruBoxContentEncryption(context);*/
		if (mSharedPreferences == null){
			mSharedPreferences = new SharedPreferences(context);
		this.mEditor = mSharedPreferences.edit();
		}
	}

	public String getValue(String key) {
		String result = (mSharedPreferences.getString(key, " "));
		return result;
	}

	public String getValue(String key, String defaultValue) {
		String result = (mSharedPreferences.getString(key, defaultValue));
		return result;
	}

	public void setValue(String key, String value) {
		mEditor.putString(key, (value));
		mEditor.commit();
	}

	public void setValue(String key, String value, String defaultValue) {
		if (!(value.equalsIgnoreCase("")))
			mEditor.putString(key, (value));
		else
			mEditor.putString(key, (defaultValue));

		mEditor.commit();
	}

	public void setBoolean(String key, boolean value) {
		mEditor.putBoolean(key, value);
		mEditor.commit();
	}

	public boolean getBoolean(String key) {
		boolean result = mSharedPreferences.getBoolean(key, false);
		return result;
	}

	/*private String encrypt(String value) {

		try {
			return new String(Base64.encode(
					nBoxContentEncryption.encrypt(value), Base64.DEFAULT));// if pbm(String slength differs if we use default) occurs use Base64.NO_WRAP
		} catch (TruboxException e) {
			e.printStackTrace();
		}catch (Exception e) {

		
		}
		return null;

	}

	private String decrypt(String value) {

		try {
			return (String) nBoxContentEncryption.decrypt(Base64.decode(value, Base64.DEFAULT),com.TruBoxSDK.TruBoxContentEncryption.DataType.STRING);
		} catch (TruboxException e) {
			e.printStackTrace();
		}catch (Exception e) {
			
			return value;
			
		}
		return null;

	}*/
}
