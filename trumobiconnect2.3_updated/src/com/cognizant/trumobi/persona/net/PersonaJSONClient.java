package com.cognizant.trumobi.persona.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
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
import org.json.JSONObject;

//trumobiEdits

/*import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;*/

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.TruBoxSDK.SharedPreferences;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.log.PersonaLog;
import com.cognizant.trumobi.persona.constants.PersonaCertfifcateAuthConstants;
import com.cognizant.trumobi.persona.utils.PersonaRegistrationTask.PersonaSSLSocketFactory;

public class PersonaJSONClient extends AsyncTask<String, Void, String> {
	ProgressDialog progressDialog;
	PersonaGetJsonInterface getJSONListener;
	Context mCurContext;
	static JSONObject mJsonObject;
	// File cacheDir;
	boolean mIsMultipart = false;
	static String mUrl;
	static boolean mIsLogin = false;
	static HashMap mLoginValues;
	static int mCode;
	static List<NameValuePair> mNameValuePairs;
	String mProgressMessage;
	static boolean multipartFlag = false;
	final int CERTIFICATE_DOWNLOAD = 1010, ROUTINE_LOGIN_CHECK = 1011, REQUEST_CERTIFICATE_DOWNLOAD = 1215, SEND_ACK=1100;

	public PersonaJSONClient(Context context, PersonaGetJsonInterface listener,
			JSONObject jsonObject, String ProgressDialogMessage, String url,
			boolean isLogin, int code,

			boolean isWithoutSession) {

		this.getJSONListener = listener;
		mJsonObject = jsonObject;
		mCurContext = context;
		mUrl = url;
		mCode = code;
		mProgressMessage = ProgressDialogMessage;

	}

	@Override
	protected String doInBackground(String... urls) {
		return connect();
	}

	public String connect() {
		// check already data exists are or not??

		InputStream instream = null;
		String result = null;

		HttpClient httpclient;

		httpclient = getNewHttpClient();

		// trustEveryone();

		// Prepare a request object

		HttpPost httpPostRequest = new HttpPost(mUrl);

		// Execute the request
		HttpResponse response;

		try {
			if (!multipartFlag) {
				httpPostRequest.setHeader("Content-Type",
						"application/json; charset=utf-8");
				String user_name = new SharedPreferences(mCurContext)
				.getString("trumobi_username", "");
				String pass_word = new SharedPreferences(mCurContext)
				.getString("trumobi_password", "");
				
				if (mCode == ROUTINE_LOGIN_CHECK  || mCode == REQUEST_CERTIFICATE_DOWNLOAD) {
				
					
					httpPostRequest.setHeader("appstore_user_id", user_name);
					httpPostRequest.setHeader("appstore_password", pass_word);
				}
				else if(mCode ==CERTIFICATE_DOWNLOAD)
				{
				
					httpPostRequest.setHeader("appstore_user_id", user_name);
				}
				StringEntity se = null;
				if (mJsonObject != null) {
					se = new StringEntity(mJsonObject.toString());
					httpPostRequest.setEntity(se);
				}
			} else {

				multipartFlag = false;
		
			}

			response = httpclient.execute(httpPostRequest);
			PersonaLog.d("Status Code" , ""+response.getStatusLine());

			HttpEntity entity = response.getEntity();

			if (entity != null) {

				// A Simple JSON Response Read
				instream = entity.getContent();
				result = convertStreamToString(instream);
				System.out.println("Status result" + result);
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
		
		PersonaLog.e("onPreExecute", "------------onPreExecute before-----------");
		if(mCode==ROUTINE_LOGIN_CHECK || (mCode==SEND_ACK ) ){
		}
		else{
			PersonaLog.e("onPreExecute", "------------onPreExecute  after-----------");
		progressDialog = new ProgressDialog(mCurContext,
				R.style.PersonaNewDialog);
		progressDialog.setMessage(mProgressMessage);
		progressDialog.setCancelable(false);
		progressDialog.setProgressStyle(R.style.CustomDialog);
		try {
			if (progressDialog != null && !progressDialog.isShowing()) {

				progressDialog.show();
			}
		} catch (Exception e) {

		}
		}
	}

	@Override
	protected void onPostExecute(String json) {
		//do not show progress dialog for the below codes
		if(mCode==ROUTINE_LOGIN_CHECK || mCode==SEND_ACK){
		}
		else{
		try {
			if (progressDialog != null && progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
		} catch (Exception e) {
		}
		progressDialog = null;
		}
		
		
		if(mCode==SEND_ACK)
		{
			if(json!=null && json.contains("Success")   || json!=null && json.contains("1235") )
				
				new SharedPreferences(mCurContext).edit().putBoolean("ack_sent", true).commit();
			else
				new SharedPreferences(mCurContext).edit().putBoolean("ack_sent", false).commit();
			
		}
		//Callback is called only for other services
		if(mCode==PersonaCertfifcateAuthConstants.REVOKE_EMAIL || mCode==SEND_ACK){
			
		}
		else
		getJSONListener.onRemoteCallComplete(json, mCode);
		
		PersonaLog.w("-----onPostExecute------",
				"--------onPostExecute----------");

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

	public HttpClient getNewHttpClient() {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore
					.getDefaultType());
			trustStore.load(null, null);

			SSLSocketFactory sf = new PersonaSSLSocketFactory(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
			HttpConnectionParams.setConnectionTimeout(params, 10000);
			HttpConnectionParams.setSoTimeout(params, 10000);
			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));

			ClientConnectionManager ccm = new ThreadSafeClientConnManager(
					params, registry);

			return new DefaultHttpClient(ccm, params);
		} catch (Exception e) {
			return new DefaultHttpClient();
		}
	}

	public class PersonaSSLSocketFactory extends SSLSocketFactory {
		SSLContext sslContext = SSLContext.getInstance("TLS");

		public PersonaSSLSocketFactory(KeyStore truststore)
				throws NoSuchAlgorithmException, KeyManagementException,
				KeyStoreException, UnrecoverableKeyException {
			super(truststore);

			TrustManager tm = new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};

			sslContext.init(null, new TrustManager[] { tm }, null);
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port,
				boolean autoClose) throws IOException, UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host,
					port, autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}
	}
}
