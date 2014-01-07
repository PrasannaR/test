package com.cognizant.trumobi.persona.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.cognizant.trumobi.R;
//trumobiEdits
/*import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;*/

public class PersonaJSONClient extends AsyncTask<String, Void, String> {
	ProgressDialog progressDialog;
	PersonaGetJsonInterface  getJSONListener;
	Context mCurContext;
	static JSONObject mJsonObject;
	// File cacheDir;
	boolean mIsMultipart = false;
	static String mUrl;
	static boolean mIsLogin = false;
	static HashMap mLoginValues;
	static int mCode;
	static List<NameValuePair> mNameValuePairs;
	static boolean multipartFlag = false;

public PersonaJSONClient(Context context, PersonaGetJsonInterface listener,
		JSONObject jsonObject, String mProgressDialogMessage, String url,
		boolean isLogin, int code,

		boolean isWithoutSession) {

	this.getJSONListener = listener;
	mJsonObject = jsonObject;
	mCurContext = context;
	mUrl = url;
	mCode = code;

}
@Override
protected String doInBackground(String... urls) {
	return connect();
}

//trumobiEdits

	/*public static MultipartEntity setMultipart() {
	MultipartEntity mEntity = new MultipartEntity(
			HttpMultipartMode.BROWSER_COMPATIBLE);

	if (mNameValuePairs != null) {
		for (int index = 0; index < mNameValuePairs.size(); index++) {
			if ((mNameValuePairs.get(index).getName()
					.equalsIgnoreCase(PersonaApplicationConstants.APPSTORE_USER_USERIMAGEBYTE))
					|| (mNameValuePairs.get(index).getName()
							.equalsIgnoreCase(PersonaApplicationConstants.APPSTORE_ICON_BINARY))) {
				// If the key equals to "image", we use FileBody to transfer
				// the data
				//
				File jsonFile = new File(PersonaApplicationConstants.cacheDir, mNameValuePairs
						.get(index).getValue());

				mEntity.addPart(mNameValuePairs.get(index).getName()
						.toString(), new FileBody(jsonFile,
						"multipart/form-data"));
			} else {
				// Normal string data
				try {

					mEntity.addPart(mNameValuePairs.get(index).getName()
							.toString(),
							new StringBody(mNameValuePairs.get(index)
									.getValue().toString(),
									"application/x-www-form-urlencoded",
									Charset.forName("UTF-8")));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

		// httpPostRequest.setEntity(mEntity);

	}

	return mEntity;
}
*/
	



public String connect() {
	// check already data exists are or not??

	InputStream instream = null;
	String result = null;

	HttpParams my_httpParams = new BasicHttpParams();
	HttpClient httpclient;

	HttpConnectionParams.setConnectionTimeout(my_httpParams, 10000);
	HttpConnectionParams.setSoTimeout(my_httpParams, 10000);
	httpclient = new DefaultHttpClient(my_httpParams);

	trustEveryone();

	// Prepare a request object

	HttpPost httpPostRequest = new HttpPost(mUrl);

	// Execute the request
	HttpResponse response;
	try {
		if (!multipartFlag) {
			httpPostRequest.setHeader("Content-Type",
					"application/json; charset=utf-8");

		
			StringEntity se = null;
			if (mJsonObject != null) {
				se = new StringEntity(mJsonObject.toString());
				httpPostRequest.setEntity(se);
			}
		} else {
			
			multipartFlag = false;
			// httpPostRequest.setHeader("Content-Type",
			// "multipart/form-data");

			//trumobiEdits
			
			/*MultipartEntity mEntity = setMultipart();
			httpPostRequest.setEntity(mEntity);
*/
		}

		response = httpclient.execute(httpPostRequest);
		//System.out.println("Status Code" + response.getStatusLine());

		HttpEntity entity = response.getEntity();

		if (entity != null) {

			// A Simple JSON Response Read
			instream = entity.getContent();
			result = convertStreamToString(instream);

			// Closing the input stream will trigger connection release
			instream.close();
			// PersonaLog.i("result", "result: " + result);
			return result;

		}

	} catch (ClientProtocolException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();

		// TODO Auto-generated catch block

		// 11-16 07:02:36.706: W/System.err(1515):
		// java.net.SocketTimeoutException
	} catch (ConnectTimeoutException timeout) {
		timeout.printStackTrace();

	} catch (IOException e) {
		e.printStackTrace();
	} catch (Exception e) {
		// TODO Auto-generated catch block
		// add exception value

		e.printStackTrace();
	}

	return result;
}

@Override
protected void onPreExecute() {
	// TODO Auto-generated method stub
	super.onPreExecute();
	progressDialog=new ProgressDialog(mCurContext,R.style.NewDialog);
	progressDialog.setMessage("Downloading certificate...");
	progressDialog.setCancelable(false);
	progressDialog.setProgressStyle(R.style.CustomDialog);
	try {
		if (progressDialog != null && !progressDialog.isShowing()) {
			
			progressDialog.show();
		}
	} catch (Exception e) {
		
	}
}

@Override
protected void onPostExecute(String json) {
	try {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
	} catch (Exception e) {
	}
	progressDialog = null;
	getJSONListener.onRemoteCallComplete(json, mCode);

}

public static String convertStreamToString(InputStream is)
		throws IOException {
	/*
	 * To convert the InputStream to String we use the Reader.read(char[]
	 * buffer) method. We iterate until the Reader return -1 which means
	 * there's no more data to read. We use the StringWriter class to
	 * produce the string.
	 */
	if (is != null) {
		Writer writer = new StringWriter();

		char[] buffer = new char[1024 * 4];
		try {
			Reader reader = new BufferedReader(new InputStreamReader(is,
					"UTF-8"));
			int n;
			while ((n = reader.read(buffer)) != -1) {
				writer.write(buffer, 0, n);
			}
		} finally {
			is.close();
		}
		return writer.toString();
	} else {
		return "";
	}
}

	// https connection code
public static void trustEveryone() {
	try {
		HttpsURLConnection
				.setDefaultHostnameVerifier(new HostnameVerifier() {
					public boolean verify(String hostname,
							SSLSession session) {
						return true;
					}
				});
		SSLContext context = SSLContext.getInstance("TLS");
		context.init(null, new X509TrustManager[] { new X509TrustManager() {
			public void checkClientTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
			}

			public void checkServerTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
			}

			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}
		} }, new SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(context
				.getSocketFactory());
	} catch (Exception e) {
		e.printStackTrace();
	}
}

}
