package com.cognizant.trumobi.persona.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedList;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.cognizant.trumobi.log.PersonaLog;
import com.cognizant.trumobi.persona.constants.PersonaConstants;
import com.cognizant.trumobi.persona.net.PersonaAllAppsListDetails;
import com.cognizant.trumobi.persona.net.PersonaGetJsonInterface;

public class PersonaRegistrationTask extends AsyncTask<String, Integer, Integer> {


	private String username;
	private String password;
	private String registrationResponse, displayMessage;
	//private String mErrorCode = "000";
	private int mErrorCode = 0;
	private String errorDesc = "Error";
	private String mDeviceId, mSerialId, mImei, mMacAddress, mUDID, mMake,
			mModel;
	private String mAppstoreSessionId;
	private int registrationStatusCode;

	public static LinkedList<PersonaAllAppsListDetails> listDetails = null;
	private boolean IsPoliciesDownloaded;
	HttpResponse response;
	private Context mContext;
	JSONObject mJsonObj;
	PersonaGetJsonInterface personaGetJsonInterface;
	SharedPreferences sharedPreferences;
	PersonaCommonfunctions mCommonfunctions;
	int remoteCallCode;

	public PersonaRegistrationTask(Context context, PersonaGetJsonInterface jsonInterface,
			Handler callback,int code) {
		this.mContext = context;
		mCommonfunctions = new PersonaCommonfunctions(mContext);
		this.personaGetJsonInterface = jsonInterface;
		this.remoteCallCode=code;
	}

	@Override
	protected Integer doInBackground(String... params) {
		// TODO Auto-generated method stub

		IsPoliciesDownloaded = false;
		String url; 
		switch(remoteCallCode) {
		case 1003: {
			username = params[0];
			password = params[1];
			url=params[2];
			registrationResponse = postLoginRequest(url);
			PersonaLog.d("Registration/Login Task","registrationResponse: "+registrationResponse);
			break;
		}
		default: {
			username = params[0];
			password = params[1];
			mDeviceId = params[2];
			mSerialId = params[3];
			mImei = params[4];
			mMacAddress = params[5];
			mUDID = params[6];
			mMake = params[7];
			mModel = params[8];
			url=params[9];
			registrationResponse = postRegistrationRequest(url);
			PersonaLog.d("Registration/Login Task","registrationResponse: "+registrationResponse);
			break;
			
		}
			
		}
		
		return registrationStatusCode;
		
		
	}

	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		super.onPreExecute();

	}

	@Override
	protected void onPostExecute(Integer result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		JSONObject jsonPolicy;
		try {
		jsonPolicy = new JSONObject(registrationResponse);
		
		if (result == PersonaConstants.RESPONSEOK) {
			IsPoliciesDownloaded = true;				
		}
		else {
			IsPoliciesDownloaded = false;
			mErrorCode = jsonPolicy.getInt(PersonaConstants.appstoreErrorCode);
		}
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			IsPoliciesDownloaded = false;
			mErrorCode = 9999;
			errorDesc = "Server not reachable";
			Toast.makeText(mContext, "Error in connectivity",
					Toast.LENGTH_SHORT).show();
		}
		
		switch(remoteCallCode) {
		case 1001: {
			
			mCommonfunctions.writeResponsetoFile(registrationResponse);
			break;

		}
		}

			Bundle bundle = new Bundle();
			bundle.putBoolean(PersonaConstants.result, IsPoliciesDownloaded);
			bundle.putString(PersonaConstants.responseMessage, registrationResponse);
			bundle.putString(PersonaConstants.displayMessage, displayMessage);
			bundle.putInt(PersonaConstants.appstoreErrorCode, mErrorCode);
			bundle.putString(PersonaConstants.ERROR_DESC, errorDesc);
			bundle.putInt(PersonaConstants.responseStatusCode, result);
			Message msg = new Message();
			msg.setData(bundle);
			personaGetJsonInterface.onRemoteCallComplete(msg, remoteCallCode);
			PersonaLog.i("called", "onremotecallcomplete");
		
		}
		
		

	public String postRegistrationRequest(String url) {
		String registrationPolicy = null;
		try {
			HttpClient httpClient = getNewHttpClient();
			HttpPost httpPost = new HttpPost(url);
			httpPost.setHeader(PersonaConstants.postContentType,
					PersonaConstants.postContentTypeValue);
			httpPost.setHeader(PersonaConstants.postHeader,
					PersonaConstants.postContentTypeValue);
			httpPost.setHeader(PersonaConstants.appstore_user_id, username);
			httpPost.setHeader(PersonaConstants.appstore_password, password);
			mJsonObj = appendRegistrationJSON();
			PersonaLog.i("PersonaRegistrationTask", mJsonObj.toString());
			httpPost.setEntity(new StringEntity(mJsonObj.toString(), "UTF-8"));
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 20000);
			HttpConnectionParams.setSoTimeout(httpParams, 20000);
			httpPost.setParams(httpParams);
			response = httpClient.execute(httpPost);

			HttpEntity httpEntity = response.getEntity();
			InputStream inputStream = httpEntity.getContent();
			registrationStatusCode = response.getStatusLine().getStatusCode();
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(inputStream, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				sb.append(line);
			}
			registrationPolicy = sb.toString();
			inputStream.close();
			PersonaLog.i("Registration Policy", registrationPolicy);
		} 

		catch (SocketTimeoutException e) {

			e.printStackTrace();

		} catch (ConnectTimeoutException e) {

			e.printStackTrace();
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return registrationPolicy;
	}

	public HttpClient getNewHttpClient() {
	    try {
	        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
	        trustStore.load(null, null);

	        SSLSocketFactory sf = new PersonaSSLSocketFactory(trustStore);
	        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

	        HttpParams params = new BasicHttpParams();
	        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
	        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

	        SchemeRegistry registry = new SchemeRegistry();
	        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	        registry.register(new Scheme("https", sf, 443));

	        ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

	        return new DefaultHttpClient(ccm, params);
	    } catch (Exception e) {
	        return new DefaultHttpClient();
	    }
	}
	
	public class PersonaSSLSocketFactory extends SSLSocketFactory {
	    SSLContext sslContext = SSLContext.getInstance("TLS");

	    public PersonaSSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
	        super(truststore);

	        TrustManager tm = new X509TrustManager() {
	            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	            }

	            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	            }

	            public X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }
	        };

	        sslContext.init(null, new TrustManager[] { tm }, null);
	    }

	    @Override
	    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
	        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
	    }

	    @Override
	    public Socket createSocket() throws IOException {
	        return sslContext.getSocketFactory().createSocket();
	    }
	}
	
	private JSONObject appendRegistrationJSON() {
		mJsonObj = new JSONObject();
		try {
			mJsonObj.put("platform_name", "Android");
			mJsonObj.put("os_version", getOSVersion());
			mJsonObj.put("model_number", mModel);
			mJsonObj.put("device_type", getDeviceType(mContext));
			mJsonObj.put("device_id", mDeviceId);
			mJsonObj.put("device_imei", mImei);
			mJsonObj.put("device_make", mMake);
			mJsonObj.put("serial_number", mSerialId);
			mJsonObj.put("ip_address", mMacAddress);
			mJsonObj.put("device_token", mUDID);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return mJsonObj;

	}

	
	public String postLoginRequest(String url) {
		String loginResponse = null;
		try {
			HttpClient httpClient = getNewHttpClient();
			HttpPost httpPost = new HttpPost(url);
			httpPost.setHeader(PersonaConstants.postContentType,
					PersonaConstants.postContentTypeValue);
			httpPost.setHeader(PersonaConstants.postHeader,
					PersonaConstants.postContentTypeValue);
			httpPost.setHeader(PersonaConstants.appstore_user_id, username);
			httpPost.setHeader(PersonaConstants.appstore_password, password);
			httpPost.setHeader("tenant_id", "1");
			httpPost.setHeader("source", "0");
			
	
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 20000);
			HttpConnectionParams.setSoTimeout(httpParams, 20000);
			httpPost.setParams(httpParams);
			
			response = httpClient.execute(httpPost);
			HttpEntity httpEntity = response.getEntity();
			InputStream inputStream = httpEntity.getContent();
			registrationStatusCode = response.getStatusLine().getStatusCode();
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(inputStream, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				sb.append(line);
			}
			registrationResponse = sb.toString();
			inputStream.close();
			
		} 

		catch (SocketTimeoutException e) {

			e.printStackTrace();

		} catch (ConnectTimeoutException e) {

			e.printStackTrace();
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return registrationResponse;
	}
	
	
	


	/**
	 * Used to check Device Type
	 * 
	 * @param context
	 * @return deviceType
	 */
	private String getDeviceType(Context context) {
		boolean xlarge = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
		boolean large = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
		if (xlarge || large)
			return "Tablets";
		return "Phones";

	}

	/**
	 * Used to get OS version
	 * 
	 * @return
	 */
	private String getOSVersion() {
		return android.os.Build.VERSION.RELEASE;
	}

}
