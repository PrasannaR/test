package com.cognizant.trumobi.persona.utils;

/**
 * Common function in MIM module 
 * 
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.TruBoxSDK.SharedPreferences;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.em.EmAccount;
import com.cognizant.trumobi.log.EmailLog;
import com.cognizant.trumobi.log.PersonaLog;
import com.cognizant.trumobi.persona.constants.PersonaConstants;
import com.cognizant.trumobi.persona.utils.PersonaCommonfunctions.ExecShell.SHELL_CMD;

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

			PersonaLog.d("postRequest", ""
					+ response.getStatusLine().getStatusCode());
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

	public String getAuthtypeInString(int authtype) {
		switch (authtype) {
		case PersonaConstants.AUTH_TYPE_NUMERIC:
			return "PIN";
		default:
			return "password";
		}

	}

	/**
	 * 
	 * @param message
	 * @param title
	 * @param isFinishActivity
	 * @param mContext
	 */
	public void showAlertDialog(String message, String title,
			final boolean isFinishActivity, final Context mContext) {

		final Dialog alertDialog = new Dialog(mContext);
		alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		alertDialog.setCancelable(false);
		alertDialog.setContentView(R.layout.pr_show_diagloue);
		Button okButton = (Button) alertDialog
				.findViewById(R.id.button_okay_dia);
		TextView errorMessage = (TextView) alertDialog
				.findViewById(R.id.textView1);
		TextView msg_title = (TextView) alertDialog
				.findViewById(R.id.txt_title);
		errorMessage.setText(message);
		msg_title.setText(title);
		okButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				alertDialog.dismiss();
				if (isFinishActivity)
					((Activity) mContext).finish();

			}
		});

		alertDialog.show();

	}

	public static void showAlert(Activity act, final String title,
			final String msg, final String buttontext,
			final boolean finishScreen, final String web_url) {
		final Activity myact = act;

		try {
			act.runOnUiThread(new Runnable() {
				public void run() {
					final AlertDialog alertDialog = new AlertDialog.Builder(
							myact).create();
					alertDialog.setTitle(title);
					alertDialog.setMessage(msg);
					alertDialog.setCancelable(false);
					alertDialog.setButton(buttontext,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									if (finishScreen)
										// myact.finish();
										alertDialog.dismiss();
									if (web_url.contains("http")) {
										Intent browserIntent = new Intent(
												Intent.ACTION_VIEW, Uri
														.parse(web_url));
										myact.startActivity(browserIntent);

										myact.finish();
									} else {
										Toast.makeText(myact, "Invalid URL",
												Toast.LENGTH_LONG).show();
										alertDialog.dismiss();
									}

									return;
								}
							});
					alertDialog.show();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}

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
		try {
			FileOutputStream fOut;
			fOut = mContext.openFileOutput(PersonaConstants.mandatoryAppsList,
					Context.MODE_PRIVATE);
			OutputStreamWriter osw = new OutputStreamWriter(fOut);
			osw.write(registrationResponse);
			osw.flush();
			osw.close();
			fOut.close();
		} catch (Exception e) {
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
			// String preferenceFileName=new
			// String(nBoxContentEncryption.encrypt(PersonaConstants.PERSONAPREFERENCESFILE));

			// String encRegistrationKey=new
			// String(Base64.encode(nBoxContentEncryption.encrypt(PersonaConstants.REGISTRATION_ID),Base64.DEFAULT));
			new SharedPreferences(context)
					.edit()
					.putString(PersonaConstants.REGISTRATION_ID, registrationId)
					.commit();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * ConvertStringToInt This method converts autolock timer from String to
	 * integer 383038
	 */

	public long convertAutoLockTimerStringToInt(String sInputStr) {

		long value = 0;
		long SixtySec = 60;

		PersonaLog.d(TAG, "======= In convertAutoLockTimerStringToInt === ");
		PersonaLog.d(TAG, "=== value of sInputStr:" + sInputStr);
		if (sInputStr.contains("minute")) {
			PersonaLog.d(TAG, "=== minutes ====");
			PersonaLog.d(
					TAG,
					" Before Intege"
							+ sInputStr.substring(0,
									(sInputStr.indexOf("minute") - 1)));
			// value = SixtySec *
			// (Integer.parseInt(sInputStr.substring(0,(sInputStr.indexOf("minutes")-1))));
			value = SixtySec
					* (Long.parseLong(sInputStr.substring(0,
							(sInputStr.indexOf("minute") - 1))));

			PersonaLog.d(TAG,
					"======= In convertAutoLockTimerStringToInt  value=== :"
							+ value);

		} else if (sInputStr.contains("hour")) {
			PersonaLog.d(TAG, "=== hour ====");
			PersonaLog.d(
					TAG,
					" Before Intege"
							+ sInputStr.substring(0,
									(sInputStr.indexOf("hour") - 1)));
			// value = SixtySec * SixtySec *
			// (Integer.parseInt(sInputStr.substring(0,sInputStr.indexOf("hour")-1)));
			value = SixtySec
					* SixtySec
					* (Long.parseLong(sInputStr.substring(0,
							sInputStr.indexOf("hour") - 1)));
			PersonaLog.d(TAG,
					"======= In convertAutoLockTimerStringToInt  value=== :"
							+ value);

		}

		else if (sInputStr.contains("sec")) {
			PersonaLog.d(TAG, "=== sec ====");
			PersonaLog.d(
					TAG,
					" Before Intege"
							+ sInputStr.substring(0,
									(sInputStr.indexOf("sec") - 1)));
			// value = SixtySec * SixtySec *
			// (Integer.parseInt(sInputStr.substring(0,sInputStr.indexOf("hour")-1)));
			value = Long.parseLong(sInputStr.substring(0,
					sInputStr.indexOf("sec") - 1));
			PersonaLog.d(TAG,
					"======= In convertAutoLockTimerStringToInt  value=== :"
							+ value);

		} else {
			PersonaLog.d(TAG, "=== ZERO ====");
			value = 0;

		}

		PersonaLog.d(TAG,
				"======= In convertAutoLockTimerStringToInt  value=== :"
						+ value);
		return value;
	}

	public static boolean checkIsCertificatePoliciesOn(Context context,
			String key) {

		PackageManager pm = context.getPackageManager();
		// params - package name ,activity name
		boolean isCertificateEnabled = false;

		try {

			ApplicationInfo ai = pm.getApplicationInfo(
					context.getPackageName(), PackageManager.GET_META_DATA);
			Bundle bundle = ai.metaData;
			// String aValue=aBundle.getString("aKey");
			// ActivityInfo ai = pm.getActivityInfo(cname,
			// PackageManager.GET_META_DATA);
			// Bundle bundle = ai.metaData;

			if (key != null) {

				if (key.equals(PersonaConstants.CERTIFICATE_ENABLED)) {
					Object object = bundle
							.get(PersonaConstants.CERTIFICATE_ENABLED);// COGNIZANT_POLICIES_ENABLED
					android.util.Log.e("TAG", "activity Info --->" + object);
					if (object.equals(true)) {
						isCertificateEnabled = true;
						return isCertificateEnabled;
					} else {
						isCertificateEnabled = false;
						return isCertificateEnabled;
					}

				}
			}

			return false;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return isCertificateEnabled;
		}

	}

	public String getPersonaKey() {

		long time = System.currentTimeMillis();

		return "persona" + time;

	}

	public String getAppVersion() {
		String appVersion = "";
		try {
			appVersion = mContext.getPackageManager().getPackageInfo(
					mContext.getPackageName(), 0).versionName;
		} catch (Exception e) {

		}
		return appVersion;
	}

	public String getOSVersion() {
		return android.os.Build.VERSION.RELEASE;
	}

	public String getDeviceMake() {

		return Build.MANUFACTURER;

	}

	public String getDeviceModel() {

		return Build.MODEL;

	}

	public String getMacAddress() {
		String mMac;
		WifiManager manager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = manager.getConnectionInfo();
		mMac = info.getMacAddress();
		return mMac;
	}

	public String getIMEI() {

		String mImei;
		TelephonyManager telephonyManager = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		mImei = telephonyManager.getDeviceId();
		return mImei;

	}

	/**
	 * Used to check Device Type
	 * 
	 * @param context
	 * @return deviceType
	 */
	public String getDeviceType(Context context) {
		boolean xlarge = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
		boolean large = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);

		int deviceType = context.getResources()
				.getInteger(R.integer.deviceType);

		// if (xlarge || large)

		// 290778 modified for resolving tablets/phones issue
		if (deviceType == 1)
			return "Tablets";
		return "Phones";

	}

	public String getSerialNumber() {
		return android.os.Build.SERIAL;
	}

	public Bundle getSettingsDataInBundle(Context context) {
		int authtype = new SharedPreferences(context).getInt("authtype",
				PersonaConstants.AUTH_TYPE_NUMERIC);
		int credentialsLength = new SharedPreferences(context).getInt(
				"credentialsLength", PersonaConstants.LOCAL_CREDENTIALS_LENGTH);
		Bundle bundle = new Bundle();
		bundle.putInt("authtype", authtype);
		bundle.putInt("credentialsLength", credentialsLength);
		PersonaLog.d("PersonaCommonFunctions",
				"---------- getSettingsDataInBundle-----" + bundle.toString());
		PersonaLog.d("PersonaCommonFunctions",
				"---------- getSettingsDataInBundle-----" + authtype + "  "
						+ credentialsLength);
		return bundle;
	}

	public boolean isDeviceRooted() {
		if (checkRootMethod1()) {
			return true;
		}
		if (checkRootMethod2()) {
			return true;
		}
		if (checkRootMethod3()) {
			return true;
		}
		return false;
	}

	public boolean checkRootMethod1() {
		String buildTags = android.os.Build.TAGS;

		if (buildTags != null && buildTags.contains("test-keys")) {
			return true;
		}
		return false;
	}

	public boolean checkRootMethod2() {
		try {
			File file = new File("/system/app/Superuser.apk");
			if (file.exists()) {
				return true;
			}
		} catch (Exception e) {
		}

		return false;
	}

	public boolean checkRootMethod3() {
		if (new ExecShell().executeCommand(SHELL_CMD.check_su_binary) != null) {
			return true;
		} else {
			return false;
		}
	}

	public static class ExecShell {

		private final String LOG_TAG = ExecShell.class.getName();

		public static enum SHELL_CMD {
			check_su_binary(new String[] { "/system/xbin/which", "su" }), ;

			String[] command;

			SHELL_CMD(String[] command) {
				this.command = command;
			}
		}

		public ArrayList<String> executeCommand(SHELL_CMD shellCmd) {
			String line = null;
			ArrayList<String> fullResponse = new ArrayList<String>();
			Process localProcess = null;

			try {
				localProcess = Runtime.getRuntime().exec(shellCmd.command);
			} catch (Exception e) {
				return null;
				// e.printStackTrace();
			}

			// BufferedWriter out = new BufferedWriter(new
			// OutputStreamWriter(localProcess.getOutputStream()));
			BufferedReader in = new BufferedReader(new InputStreamReader(
					localProcess.getInputStream()));

			try {
				while ((line = in.readLine()) != null) {
					PersonaLog.d(LOG_TAG, "--> Line received: " + line);
					fullResponse.add(line);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			PersonaLog.d(LOG_TAG, "--> Full response was: " + fullResponse);

			return fullResponse;
		}

	}

	public String getBundleId() {
		return mContext.getApplicationInfo().packageName;
	}

	public int getAuthenticationType(String mAT) {

		if (mAT.equals(PersonaConstants.AUTHENTICATION_TYPE_NUMERIC_STRING))
			return PersonaConstants.AUTH_TYPE_NUMERIC;
		else if (mAT
				.equals(PersonaConstants.AUTHENTICATION_TYPE_ALPHABETS_STRING))
			return PersonaConstants.AUTH_TYPE_ALPHABETS;
		else if (mAT
				.equals(PersonaConstants.AUTHENTICATION_TYPE_ALPHANUMERIC_STRING))
			return PersonaConstants.AUTH_TYPE_ALPHANUMERIC;
		else if (mAT
				.equals(PersonaConstants.AUTHENTICATION_TYPE_PASSWORD_ALLOW_NUMERALS_STRING))
			return PersonaConstants.AUTH_TYPE_PASSWORD_ALLOW_NUMERALS;
		else
			return PersonaConstants.AUTH_TYPE_NUMERIC;

	}

	public boolean isPinSatisfiesRequirements(String mPin, int pwdType,
			int mPwdLength) {
		boolean result = false;
		Pattern mPattern, mPattern2;
		Matcher mMatcher, mMatcher2;
		int pinLength;

		boolean mIsNonNumericCharsExist;
		boolean isLengthSatisfied;
		boolean mIsNonAlphaCharsExist;
		boolean mIsAlphaCharsExist;
		boolean mIsNumeralExist;
		switch (pwdType) {

		case PersonaConstants.AUTH_TYPE_NUMERIC:
			mPattern = Pattern.compile("[^0-9]");
			mMatcher = mPattern.matcher(mPin);
			mIsNonNumericCharsExist = mMatcher.find();
			pinLength = mPin.length();
			isLengthSatisfied = pinLength == mPwdLength;
			result = !mIsNonNumericCharsExist && isLengthSatisfied;
			break;

		case PersonaConstants.AUTH_TYPE_ALPHABETS:
			mPattern = Pattern.compile("[^a-zA-Z]");
			mMatcher = mPattern.matcher(mPin);
			mIsNonAlphaCharsExist = mMatcher.find();
			pinLength = mPin.length();
			isLengthSatisfied = pinLength >= mPwdLength;
			result = !mIsNonAlphaCharsExist && isLengthSatisfied;
			break;

		case PersonaConstants.AUTH_TYPE_ALPHANUMERIC:
			mPattern = Pattern.compile("[a-zA-Z]");
			mPattern2 = Pattern.compile("[0-9]");
			mMatcher = mPattern.matcher(mPin);
			mMatcher2 = mPattern2.matcher(mPin);
			mIsAlphaCharsExist = mMatcher.find();
			mIsNumeralExist = mMatcher2.find();
			pinLength = mPin.length();
			isLengthSatisfied = pinLength >= mPwdLength;
			result = mIsAlphaCharsExist && mIsNumeralExist && isLengthSatisfied;
			break;

		case PersonaConstants.AUTH_TYPE_PASSWORD_ALLOW_NUMERALS:
			/*
			 * mPattern = Pattern.compile("[a-zA-Z]"); mPattern2 =
			 * Pattern.compile("[0-9]"); mMatcher = mPattern.matcher(mPin);
			 * mMatcher2 = mPattern2.matcher(mPin); mIsAlphaCharsExist =
			 * mMatcher.find(); mIsNumeralExist = mMatcher2.find();
			 */
			pinLength = mPin.length();
			isLengthSatisfied = pinLength >= mPwdLength;
			result = isLengthSatisfied;
			break;

		}

		return result;
	}

	public String setAuthenticationtype(int authType) {
		String authTypeInString = null;
		switch (authType) {
		case PersonaConstants.AUTH_TYPE_NUMERIC:
			authTypeInString = PersonaConstants.AUTHENTICATION_TYPE_NUMERIC_STRING;
			break;
		case PersonaConstants.AUTH_TYPE_ALPHABETS:
			authTypeInString = PersonaConstants.AUTHENTICATION_TYPE_ALPHABETS_STRING;
			break;
		case PersonaConstants.AUTH_TYPE_ALPHANUMERIC:
			authTypeInString = PersonaConstants.AUTHENTICATION_TYPE_ALPHANUMERIC_STRING;
			break;
		case PersonaConstants.AUTH_TYPE_PASSWORD_ALLOW_NUMERALS:
			authTypeInString = PersonaConstants.AUTHENTICATION_TYPE_PASSWORD_ALLOW_NUMERALS_STRING;
			break;
		}

		return authTypeInString;

	}

	public static EmAccount getAccount() {
		
		return null;

	}

	public void saveCredentialsDetailsForExpiryScreen(int mLocalAuthtype,
			int mPwdLength) {
		new SharedPreferences(mContext).edit()
				.putInt("authtype", mLocalAuthtype).commit();
		new SharedPreferences(mContext).edit()
				.putInt("credentialsLength", mPwdLength).commit();

	}

	public boolean getTruHubDetails() {

		Uri customEmailURI = null;
		ContentProviderClient cpEmailClient = null;
		Cursor cursor = null;

		if(checkIfTruHubAppInstalled()){
		// 290778 - modified the ContentProvider location
		customEmailURI = Uri
				.parse("content://com.cognizant.appstore.nativeandroid.screens.emailcontentprovider/email");
		cpEmailClient = mContext.getContentResolver()
				.acquireContentProviderClient(customEmailURI);
		if (cpEmailClient != null) {

			String ID = "_id";
			String COL_EMAIL_ID = "email_id";
			String COL_DOMAIN = "domain";
			String COL_SERVER = "server";
			String COL_PWD = "password";
			String COL_CERTIFICATE = "file";
			String DEVICE_ADMIN = "deviceAdmin";
			String[] list_pro = { ID, COL_EMAIL_ID, COL_CERTIFICATE,
					COL_DOMAIN, COL_SERVER, COL_PWD ,DEVICE_ADMIN};

			try {
				cursor = cpEmailClient.query(customEmailURI, list_pro, null,
						null, null);
				if (cursor != null & cursor.moveToFirst()) {
					do {
						int isDeviceAdminEnabledforTruHub = 0;
						isDeviceAdminEnabledforTruHub = cursor.getInt(cursor
								.getColumnIndex(DEVICE_ADMIN));
						
						PersonaLog
								.e("===============truhub admin check============",
										"isDeviceAdminEnabledforTruHub: "
												+ isDeviceAdminEnabledforTruHub);
						
						if(isDeviceAdminEnabledforTruHub==1)
					return true;
						

					} while (cursor.moveToNext());
				} else {

					cursor.close();
					return false;
				}

			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				return false;
			} catch (Exception e) {
				EmailLog.e("EXception in ",
						"getTruHubDetails " + e);
				e.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		} else {

			return false;
		}
		}
		return false;
	}
	
	private boolean checkIfTruHubAppInstalled() {
		// TODO Auto-generated method stub
		final PackageManager pm = mContext.getPackageManager();
		boolean isTruHubInstalled=false;
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		List<ResolveInfo> appList = pm.queryIntentActivities(mainIntent, 0);
		Collections.sort(appList, new ResolveInfo.DisplayNameComparator(pm));

		for (ResolveInfo temp : appList) {

			PersonaLog.d("PersonCommon Functions", temp.activityInfo.packageName);
				if (temp.activityInfo.packageName.equals("com.cognizant.appstore.nativeandroid.screens")
						|| temp.activityInfo.packageName.equals("com.cognizant.appshubtablet")) {

					
					isTruHubInstalled= true;
					break;
				}
				else{
					isTruHubInstalled=false;
					
				}
		

			}

		

		return isTruHubInstalled;
	}

}
