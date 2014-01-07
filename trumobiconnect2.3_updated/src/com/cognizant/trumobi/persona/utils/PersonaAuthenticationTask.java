package com.cognizant.trumobi.persona.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;


import com.TruBoxSDK.SharedPreferences;
import com.TruBoxSDK.TruboxException;
import com.cognizant.trumobi.log.PersonaLog;
import com.cognizant.trumobi.persona.constants.PersonaConstants;
import com.cognizant.trumobi.persona.net.PersonaGetJsonInterface;

public class PersonaAuthenticationTask extends AsyncTask<String, Integer, Integer> {

	private Context mContext, mAppContext;
	private Handler mHandler;
	SharedPreferences sharedPreferences;
	PersonaCommonfunctions mCommonfunctions;
	PersonaGetJsonInterface personaGetJsonInterface;
	public JSONObject jsonobj;
	protected String authenticationResponse, responseMessage, errorDesc,
			errorCode;
	
	protected static HttpResponse response;
	ProgressDialog mProgressDialog;
	int registrationStatusCode;
	
	public PersonaAuthenticationTask(Context context, Context appContext,PersonaGetJsonInterface jsonInterface) {
		this.mContext = context;
		this.mAppContext = appContext;
		personaGetJsonInterface = jsonInterface;
		mProgressDialog = new ProgressDialog(context);
		mCommonfunctions = new PersonaCommonfunctions(context);

	}

	@Override
	protected Integer doInBackground(String... params) {
		// TODO Auto-generated method stub

		// mTruBox.isAuthenticationRequired = false;
		String url = params[0];

		authenticationResponse = postAuthenticationRequest(url);

		if (authenticationResponse != null) {
			JSONObject jsonPolicy;
			try {
				jsonPolicy = new JSONObject(authenticationResponse);
				responseMessage = jsonPolicy
						.optString(PersonaConstants.responseMessage);

				if (registrationStatusCode == PersonaConstants.RESPONSEOK) {
					JSONObject appStoreInformation = jsonPolicy
							.optJSONObject(PersonaConstants.appstoreInformation);
					mCommonfunctions.writeResponsetoFile(authenticationResponse);
					
				
				} else if (registrationStatusCode == PersonaConstants.RESPONSE500) {
					PersonaLog.d("Error Response", "" + registrationStatusCode);

					errorDesc = jsonPolicy.optString("display_message");
					errorCode = jsonPolicy.optString("appstore_error_code");

				}

			} catch (Exception e) {
				e.printStackTrace();

				errorDesc = "Server not reachable";
				errorCode = "001";
			}
		}

		return registrationStatusCode;
	}

	public String postAuthenticationRequest(String url) {
		String authenticationPolicy = null;
		InputStream inputStream = null;
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost post = new HttpPost(url);
		post.setHeader(PersonaConstants.postContentType,
				PersonaConstants.postContentTypeValue);
		post.setHeader(PersonaConstants.postHeader, PersonaConstants.postContentTypeValue);
		jsonobj = appendJSON();
		try {
			post.setEntity(new StringEntity(jsonobj.toString(), "UTF-8"));
			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters, 15000);
			HttpConnectionParams.setSoTimeout(httpParameters, 15000);
			post.setParams(httpParameters);
			PersonaLog.e("requestttt",jsonobj.toString());
			response = httpClient.execute(post);
			HttpEntity entity = response.getEntity();
			registrationStatusCode = response.getStatusLine().getStatusCode();

			inputStream = entity.getContent();
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(inputStream, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				sb.append(line);
			}
			authenticationPolicy = sb.toString();
			inputStream.close();
			PersonaLog.d("Registrationcheck service", authenticationPolicy);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return authenticationPolicy;
	}

	private JSONObject appendJSON() {
		jsonobj = new JSONObject();
		try {
			jsonobj.put("device_uid", mCommonfunctions.getDeviceID());
			PersonaLog.d("device_uid", "device_id(): " + mCommonfunctions.getDeviceID());
			jsonobj.put("persona_key", getPersonaKey());

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return jsonobj;

	}

	private String getPersonaKey() {
		String personakey = null;
		String decPersonaKeyValue=null;
		try {
			//String preferenceFileName=new String(nBoxContentEncryption.encrypt(PersonaConstants.PERSONAPREFERENCESFILE));
			
			//PersonaLog.d("Auth","preferenceFileName: "+preferenceFileName);
		
			
			
		//	String encPersonaKey=  new String (Base64.encode(nBoxContentEncryption.encrypt("persona_Key"),Base64.DEFAULT));
			//
			
			
			//PersonaLog.d("Persona Key", "Authentication task encPersonaKey() from preference: " + encPersonaKey);
			/* personakey = sharedPreferences.getString("persona_Key",
					PersonaConstants.emptyString);
				PersonaLog.d("Persona Key", "Authentication task getPersonaKey() from preference: " + personakey);*/
			 
			 decPersonaKeyValue=new SharedPreferences(mContext).getString("persona_Key", PersonaConstants.emptyString);
			 
			 
			 
			
			 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return decPersonaKeyValue;
		
	
		
	
	}

	@Override
	protected void onPostExecute(Integer result) {

		// bundle data to Authentication handler

		Bundle bundle = new Bundle();
		bundle.putInt(PersonaConstants.result, result);
		bundle.putString(PersonaConstants.appstoreErrorCode, errorCode);
		bundle.putString(PersonaConstants.responseMessage, authenticationResponse);
		Message msg = new Message();
		msg.setData(bundle);
		//mHandler.sendMessage(msg);
		personaGetJsonInterface.onRemoteCallComplete(msg, 0);
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();
		/*
		 * mProgressDialog.setCancelable(false);
		 * mProgressDialog.setProgressStyle(R.style.CustomDialog);
		 * mProgressDialog.setMessage("Checking device status");
		 * if(mProgressDialog!=null && !mProgressDialog.isShowing() &&
		 * !((Activity)mContext).isFinishing()) mProgressDialog.show();
		 */
	}

}
