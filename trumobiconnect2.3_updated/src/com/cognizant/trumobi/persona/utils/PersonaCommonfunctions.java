package com.cognizant.trumobi.persona.utils;

/**
 * Common function in MIM module 
 * 
 */

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings.Secure;
import android.util.Base64;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.TruBoxSDK.SharedPreferences;
import com.TruBoxSDK.TruboxException;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.log.PersonaLog;
import com.cognizant.trumobi.persona.constants.PersonaConstants;

public class PersonaCommonfunctions {

	private Context mContext;
	public JSONObject jsonobj;
	boolean isDownloadingUserLevelProfile;
	public static boolean isLandingActivityExist = false;
	protected static HttpResponse response;
	public static final String DISPLAY_MESSAGE_ACTION = "com.cognizant.trumobi.persona.DISPLAY_MESSAGE";
	public static final String EXTRA_MESSAGE = "message";

	protected PersonaRegistrationTask registerAuthenticateTask;
	int appID;
	String devID;
	public String serverDateTime;
	public static final Charset UTF_8 = Charset.forName("UTF-8");
	private static String TAG = PersonaCommonfunctions.class.getName();

	public PersonaCommonfunctions(Context context) {
		mContext = context;

	}

	/**
	 * Used to post request through HTTPClient and get response from server
	 * 
	 * @param url
	 *            - server link to post
	 * @param sessionID
	 *            - sessionID for application
	 * @return response value
	 */

	public String postRequest(String url, String sessionID) {
		String policy = null;
		HttpPost postRequest = null;
		PersonaLog.d("post request", "url - >" + url);

		try {
			HttpClient httpclient = new DefaultHttpClient();
			postRequest = new HttpPost(url);

			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
			HttpConnectionParams.setSoTimeout(httpParameters, 10000);
			postRequest.setParams(httpParameters);

			response = httpclient.execute(postRequest);

			PersonaLog.e("LOG_CAT", "Downloading policies..");

			HttpEntity httpEntity = response.getEntity();

			PersonaLog.d("postRequest", "" + response.getStatusLine().getStatusCode());
			if (response.getStatusLine().getStatusCode() != 200
					&& response.getStatusLine().getStatusCode() != 500) {

				throw new RuntimeException("Failed : HTTP error code : "
						+ response.getStatusLine().getStatusCode());
			}

			InputStream inputStream = httpEntity.getContent();

			BufferedReader br = new BufferedReader(new InputStreamReader(
					inputStream, "iso-8859-1"), 8);

			StringBuilder sb = new StringBuilder();
			String value = "";

			while ((value = br.readLine()) != null) {
				sb.append(value);
			}
			policy = sb.toString();
			inputStream.close();
			httpEntity = null;
			PersonaLog.d("postRequest", "policy " + policy);

		} catch (IOException ioException) {
			PersonaLog.i("TAG  :", "" + ioException);
			PersonaLog.i("TAG  :", "Policy in Catch " + policy);
			ioException.printStackTrace();

		}

		return policy;
	}

	/**
	 * 
	 * @param message
	 * @param title
	 * @param isFinishActivity
	 * @param mContext
	 */
	public void showAlertDialog(String message,String title,final boolean isFinishActivity,final Context mContext) {
		
		final Dialog alertDialog = new Dialog(mContext);
		alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		alertDialog.setCancelable(false);
		alertDialog.setContentView(R.layout.pr_show_diagloue);
		Button okButton = (Button)alertDialog.findViewById(R.id.button_okay_dia);
		TextView errorMessage = (TextView)alertDialog.findViewById(R.id.textView1);
		TextView msg_title = (TextView)alertDialog.findViewById(R.id.txt_title);
		errorMessage.setText(message);
		msg_title.setText(title);
		okButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				alertDialog.dismiss();
				if(isFinishActivity)
					((Activity)mContext).finish();
				
			}
		});

		alertDialog.show();

	}
	/**
	 * 
	 * @return
	 */
	public String readMandatoryAppsList() {
		StringBuilder buffer = null;
		try {
			FileInputStream fis;
			fis = mContext.openFileInput(PersonaConstants.mandatoryAppsList);
			int byteValue;
			buffer = new StringBuilder();
			while ((byteValue = fis.read()) != -1) {
				buffer.append((char) byteValue);
			}
			fis.close();
			fis = null;
		} catch (Exception e) {

			e.printStackTrace();
		}
		return buffer.toString();

	}
/**
 * 
 * @param registrationResponse
 */
	public void writeResponsetoFile(String registrationResponse) {
		try{
		FileOutputStream fOut;
		fOut = mContext.openFileOutput(PersonaConstants.mandatoryAppsList, Context.MODE_PRIVATE);
		OutputStreamWriter osw = new OutputStreamWriter(fOut);
		osw.write(registrationResponse);
		osw.flush();
		osw.close();
		fOut.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}



	/**
	 * Used to check network connection
	 * 
	 * @return boolean
	 */
	public boolean isConnectedToNetwork() {

		ConnectivityManager mConnMgr;
		NetworkInfo mMobile, mWifi;
		boolean mIsConnectedToMobileNetwork = false, mIsConnectedToWifi = false;
		mConnMgr = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		mWifi = mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		// mMobile = mConnMgr.getActiveNetworkInfo();
		mMobile = mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (mMobile == null) {
			mIsConnectedToMobileNetwork = false;
		} else {
			mIsConnectedToMobileNetwork = mMobile.isConnected();
		}

		if (mWifi == null) {
			mIsConnectedToWifi = false;
		} else {
			mIsConnectedToWifi = mWifi.isConnected();
		}

		return mIsConnectedToMobileNetwork || mIsConnectedToWifi;
	}

	/**
	 * used to put json values to JSONobject
	 */
	public JSONObject putJSONValue(List<NameValuePair> jsonObjList) {
		JSONObject jsonobj = new JSONObject();
		try {
			for (NameValuePair obj : jsonObjList) {

				jsonobj.put(obj.getName(), obj.getValue());
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonobj;

		// File I/O
		// Toast message

	}

	/**
	 * Used to get Device ID
	 * 
	 * @return
	 */
	public String getDeviceID() {
		String deviceId = Secure.getString(this.mContext.getContentResolver(),
				Secure.ANDROID_ID);

		return deviceId;
		
	}


/**
 * 
 * @param sha1
 * @return
 */
	static int getSmallHashFromSha1(byte[] sha1) {
		final int offset = sha1[19] & 0xf; // SHA1 is 20 bytes.
		return ((sha1[offset] & 0x7f) << 24)
				| ((sha1[offset + 1] & 0xff) << 16)
				| ((sha1[offset + 2] & 0xff) << 8)
				| ((sha1[offset + 3] & 0xff));
	}

/**
 * 
 * @param context
 * @param message
 */
	public static void displayMessage(Context context, String message) {
		Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
		intent.putExtra(EXTRA_MESSAGE, message);
		context.sendBroadcast(intent);
	}
/**
 * 
 * @param context
 * @param registrationId
 */
	public static void saveRegistrationId(Context context, String registrationId) {
		
		
	
		
		try {
			//String preferenceFileName=new String(nBoxContentEncryption.encrypt(PersonaConstants.PERSONAPREFERENCESFILE));	
		
			new SharedPreferences(context).edit().putString(PersonaConstants.REGISTRATION_ID,registrationId).commit();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	

	}
	
	/**
	 *  ConvertStringToInt 
	 *  This method converts autolock timer from String to integer
	 *  383038 
	 */
	

	public long  convertAutoLockTimerStringToInt(String sInputStr){
		
		long value=0;
		long SixtySec = 60;
		
		PersonaLog.d(TAG,"======= In convertAutoLockTimerStringToInt === ");
		PersonaLog.d(TAG,"=== value of sInputStr:"+sInputStr);
		if(sInputStr.contains("minute")){
			PersonaLog.d(TAG,"=== minutes ====");
			PersonaLog.d(TAG," Before Intege"+sInputStr.substring(0,(sInputStr.indexOf("minute")-1)));
			//value = SixtySec * (Integer.parseInt(sInputStr.substring(0,(sInputStr.indexOf("minutes")-1))));
			value = SixtySec * (Long.parseLong(sInputStr.substring(0,(sInputStr.indexOf("minute")-1))));
			
			PersonaLog.d(TAG,"======= In convertAutoLockTimerStringToInt  value=== :"+value);

			
		}
		else if(sInputStr.contains("hour")){
			PersonaLog.d(TAG,"=== hour ====");
			PersonaLog.d(TAG," Before Intege"+sInputStr.substring(0,(sInputStr.indexOf("hour")-1)));
			//value = SixtySec * SixtySec * (Integer.parseInt(sInputStr.substring(0,sInputStr.indexOf("hour")-1)));
			value = SixtySec * SixtySec * (Long.parseLong(sInputStr.substring(0,sInputStr.indexOf("hour")-1)));
			PersonaLog.d(TAG,"======= In convertAutoLockTimerStringToInt  value=== :"+value);

		}
		
		else if(sInputStr.contains("sec")){
			PersonaLog.d(TAG,"=== sec ====");
			PersonaLog.d(TAG," Before Intege"+sInputStr.substring(0,(sInputStr.indexOf("sec")-1)));
			//value = SixtySec * SixtySec * (Integer.parseInt(sInputStr.substring(0,sInputStr.indexOf("hour")-1)));
			value =  Long.parseLong(sInputStr.substring(0,sInputStr.indexOf("sec")-1));
			PersonaLog.d(TAG,"======= In convertAutoLockTimerStringToInt  value=== :"+value);

		}
		else{
			PersonaLog.d(TAG,"=== ZERO ====");
			value = 0;

		}
		
		PersonaLog.d(TAG,"======= In convertAutoLockTimerStringToInt  value=== :"+value);
		return value;
	}

}
