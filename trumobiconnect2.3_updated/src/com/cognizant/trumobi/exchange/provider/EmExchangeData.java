package com.cognizant.trumobi.exchange.provider;

/**
 *  To get the user details from the Connect or Truhub app
 */

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.cognizant.trumobi.log.EmailLog;

import android.content.ContentProviderClient;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.RemoteException;
import android.util.Log;

public class EmExchangeData {

	private byte[] pfxbyteArray;
	private String pfxPass;
	private static String domain;
	private static String server;
	private static String email_ID ;
	private static String pwd;
	private boolean callProvDeviceId;
	private Context context;
	public static EmExchangeData dataObject;
	
	public static EmExchangeData getInstance(Context context)
	{
		if(dataObject == null)
		{
			dataObject = new EmExchangeData(context);
			//dataObject.setCertificateData(context);
			dataObject.getEasDetails();
		}
		return dataObject;
	}

	public EmExchangeData(Context ctx) {
		context = ctx;
	}

	public byte[] getPfxbyteArray() {
		// EmailLog.d("getPfxbyteArray ", ""+pfxbyteArray);
		return pfxbyteArray;
	}

	public void setPfxbyteArray(byte[] pfxbyteArray) {

		this.pfxbyteArray = pfxbyteArray;
	}

	public String getPfxPass() {
		return pfxPass;
	}

	public void setPfxPass(String pfxPass) {
		this.pfxPass = pfxPass;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getServer() {
		EmailLog.d("getServer ", "" + server);
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public static String getEmail_ID() {
		EmailLog.d("getEmail_ID ", "" + email_ID);
		return email_ID;
	}

	public void setEmail_ID(String email_ID) {
		this.email_ID = email_ID;
	}
	
	public String getPassword() {
		EmailLog.d("pwd ", "" + pwd);
		return pwd;
	}
	
	public void setPassword(String password) {
		this.pwd = password;
	}
	
	public boolean getCallProvisionDevice() {
		EmailLog.d("pwd ", "" + pwd);
		return callProvDeviceId;
	}
	
	public void setCallProvisionDevice(boolean callDeviceId) {
		this.callProvDeviceId = callDeviceId;
	}

	private static byte[] getpfxbArray(Context context) {
		byte[] bytes = null;
		try {
			File sdcard = Environment.getExternalStorageDirectory();
			// Get the .pfx file
			File file = new File(sdcard, "geetha.pfx");
			FileInputStream fis = new FileInputStream(file);
			InputStream is = new BufferedInputStream(fis);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] b = new byte[1024];
			int bytesRead;
			while ((bytesRead = is.read(b)) != -1) {
				bos.write(b, 0, bytesRead);
			}
			bytes = bos.toByteArray();
			Log.e("bytesbytesbytes", "bytesbytesbytesbytes : "+bytes);
			is.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bytes;
	}
	public void setCertificateData(Context context)
	{		
    	setEmail_ID("geetha@truboxmdmdev.com");
    	setPassword("pass");
    	setPfxPass("pass");
    	setDomain("truboxmdmdev.com");
    	setServer("trubox.cognizant.com");
    	setPfxbyteArray(getpfxbArray(context));
    	//Log.e("Byteeeeeeeeeee", "Arrayyyyyyyyyyyyy "+getpfxbArray(context));
	}

	public boolean getEasDetails() {

//		Uri customEmailURI = null;
//		ContentProviderClient cpEmailClient = null;
//		Cursor cursor = null;
//		// customEmailURI =
//		// Uri.parse("content://com.cognizant.trumobi.appstore.nativeandroid.screens.emailcontentprovider/email");
//
//		// 290778 - modified the ContentProvider location
//		customEmailURI = Uri
//				.parse("content://com.cognizant.trumobi.persona.launcher.emailcontentprovider/email");
//		cpEmailClient = context.getContentResolver()
//				.acquireContentProviderClient(customEmailURI);
//		if (cpEmailClient != null) {
//
//			String ID = "_id";
//			String COL_EMAIL_ID = "email_id";
//			String COL_DOMAIN = "domain";
//			String COL_SERVER = "server";
//			String COL_PWD = "password";
//			String COL_CERTIFICATE = "file";
//			String[] list_pro = { ID, COL_EMAIL_ID, COL_CERTIFICATE,
//					COL_DOMAIN, COL_SERVER, COL_PWD };
//			
//			try {
//				cursor = cpEmailClient.query(customEmailURI, list_pro, null,
//						null, null);
//				if (cursor != null & cursor.moveToFirst()) {
//					do {
//						setPfxbyteArray(cursor.getBlob(cursor
//								.getColumnIndex(COL_CERTIFICATE)));
//						setPfxPass(cursor.getString(cursor
//								.getColumnIndex(COL_PWD)));
//						setDomain(cursor.getString(cursor
//								.getColumnIndex(COL_DOMAIN)));
//						setServer(cursor.getString(cursor
//								.getColumnIndex(COL_SERVER)));
//						setEmail_ID(cursor.getString(cursor
//								.getColumnIndex(COL_EMAIL_ID)));
//					} while (cursor.moveToNext());
//				} else {
//
//					cursor.close();
//					return false;
//				}
//
//			} catch (RemoteException e) {
//				// TODO Auto-generated catch block
//				// e.printStackTrace();
//				return false;
//			} catch (Exception e) {
//				EmailLog.e("EXception in ",
//						"geteasDetails geteasDetails geteasDetails " + e);
//				e.printStackTrace();
//			} finally {
//				if (cursor != null) {
//					cursor.close();
//				}
//			}
//		} else {
//
//			return false;
//		}

		return true;
	}
}
