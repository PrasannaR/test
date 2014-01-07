package com.cognizant.trumobi.persona;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.provider.Settings.Secure;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.TruBoxSDK.SharedPreferences;
import com.TruBoxSDK.TruBoxContentEncryption;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.log.PersonaLog;
import com.cognizant.trumobi.persona.constants.PersonaCertfifcateAuthConstants;
import com.cognizant.trumobi.persona.constants.PersonaConstants;
import com.cognizant.trumobi.persona.net.PersonaCertificateDownloadResponse;
import com.cognizant.trumobi.persona.net.PersonaGetJsonInterface;
import com.cognizant.trumobi.persona.net.PersonaJSONClient;
import com.cognizant.trumobi.persona.net.PersonaLoginFailureResponse;
import com.cognizant.trumobi.persona.net.PersonaLoginResponse;
import com.cognizant.trumobi.persona.net.PersonaMessageAcknowledgementResponse;
import com.cognizant.trumobi.persona.utils.PersonaCommonfunctions;
import com.cognizant.trumobi.persona.utils.PersonaMDMHashTable;
import com.google.gson.Gson;

public class PersonaInstallCertificateActivity extends FragmentActivity
		implements PersonaGetJsonInterface {

	private static final String TAG = PersonaInstallCertificateActivity.class
			.getSimpleName();
	String password;
	private final int REQUEST_CERTIFICATE_DOWNLOAD = 1215;
	private final int SEND_ACK = 1217;
	Context mContext;
	boolean isFinishActivity;
	StringBuilder sb = new StringBuilder();
	PersonaInstallCertificateActivity personaInstallCertificateActivity;
	int certificateNo;
	Dialog alertDialog1;
	String message,title;
	SharedPreferences emailPref, fileNames;
	TextView show_password_layout, downloadTextView, appNameTextView;
	ImageView image_title, image_title1;
	String email_id;
	PersonaCommonfunctions mCommonfunctions;
	String EASLauncherName;
	SharedPreferences sharedPreferences;
	PersonaJSONClient mDownloadcertificate, mSendAcknlowdge;
	TruBoxContentEncryption nBoxContentEncryption;
	int certificateDowloadFailureErrorcode = 2001,
			serverUnreachabelErrorCode = 2005;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.pr_certificate_warning_screen);
		 mContext = this;
		 mCommonfunctions = new PersonaCommonfunctions(mContext);
		 ProgressBar progressBar =(ProgressBar)findViewById(R.id.progressImageIcon);
		 progressBar.setVisibility(View.INVISIBLE);
		 downloadTextView=(TextView)findViewById(R.id.textView1);
     	appNameTextView=(TextView)findViewById(R.id.textView2);     	
     	downloadTextView.setVisibility(View.INVISIBLE);
     	appNameTextView.setVisibility(View.INVISIBLE);
     	
		personaInstallCertificateActivity = this;
		/*
		 * boolean isCertificatedDownloaded = sharedPreferences.getBoolean(
		 * "certificate_downloaded", false);
		 */
		
		
		requestForCertificate();
		
	
			}

	private void requestForCertificate() {
		
		HashMap map = new HashMap();

		JSONObject holder = null;

		try {
			holder = new JSONObject();

			String sDeviceId = Secure.getString(getContentResolver(),
					Secure.ANDROID_ID);
			holder.put("platform_name", "Android");
			holder.put("os_version", mCommonfunctions.getOSVersion());
			holder.put("model_number", mCommonfunctions.getDeviceModel());
			holder.put("device_type", mCommonfunctions.getDeviceType(mContext));
			holder.put("device_id", sDeviceId);
			holder.put("is_device_managed", true);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		PersonaLog.d("PersonaInstallCertificateActivity",
				"PersonaAPIConstants.REQUEST_CERTIFICATE_DOWNLOAD : holder :" + holder);
		mDownloadcertificate = new PersonaJSONClient(mContext,
				personaInstallCertificateActivity, holder, getResources()
						.getString(R.string.pr_resubmit_request_message),
				PersonaCertfifcateAuthConstants.EMAIL_RE_REQUEST_SERVICE,
				false, REQUEST_CERTIFICATE_DOWNLOAD, false);

		mDownloadcertificate.execute();

	}

	@Override
	public void onRemoteCallComplete(String json, int code) {
		// TODO Auto-generated method stub
		
		PersonaLog.d("PersonaInstallCertificateActivity",
				"codeE :"
						+ code);
		
		switch(code){
		case REQUEST_CERTIFICATE_DOWNLOAD:
			try {
				PersonaLog.d("PersonaInstallCertificateActivity",
						"PersonaAPIConstants.REQUEST_CERTIFICATE : json :"
								+ json);
				Gson gson = new Gson();
			PersonaLoginResponse response = gson.fromJson(
						json, PersonaLoginResponse.class);
			
			PersonaLog.d("PersonaInstallCertificateActivity",
					"PersonaAPIConstants.REQUEST_CERTIFICATE : "+ response.message_status);
				//String response = json;
				if (response != null
						&& response.message_status.contains("Success.")) {

					showCertificateWarningScreen(certificateDowloadFailureErrorcode);
					
				} else if (response.message_status.contains("Failure")) {
					
					PersonaLoginFailureResponse loginFailureResponse = gson
							.fromJson(json, PersonaLoginFailureResponse.class);
					
					parseFailureResponse(loginFailureResponse);



					} else {
					// response is null /server is unreachable due to
					// connectivity
					showCertificateWarningScreen(serverUnreachabelErrorCode);

					}

			} catch (Exception e) {
				e.printStackTrace();
				// Exception in downloading the certificate
				showCertificateWarningScreen(0);
				// finish();
				/*
				 * Toast.makeText( mContext, mContext.getResources().getString(
				 * R.string.error_in_connection), Toast.LENGTH_LONG).show();
				 */
				// showCertificateWarningScreen();

			}
			break;

		case SEND_ACK:
			try {
				PersonaLog.d("InstallActivity",
						"PersonaAPIConstants.REGISTER_DEVICE : json :" + json);
				Gson gson = new Gson();

				if (json != null) {
					String response = json;
					if (response.contains(PersonaConstants.MSG_SUCCESS)) {

					/*	SharedPreferences email_push_prefs = getSharedPreferences(
								"Email_push_prefs", Context.MODE_PRIVATE);
						Editor editor_email = email_push_prefs.edit();
						editor_email.putString("MDMAction", null);
						editor_email.putInt("CertificateNo", 0);
						editor_email.putString("UID", null);
						editor_email.commit();*/

						PersonaMessageAcknowledgementResponse personaMessageAcknowledgementResponse = gson
								.fromJson(
										json,
										PersonaMessageAcknowledgementResponse.class);

						PersonaMDMHashTable.email_initiate
								.remove(personaMessageAcknowledgementResponse.appstore_request.command_uid);

						String successResponse = personaMessageAcknowledgementResponse.display_message;
						PersonaLog
								.d("InstallActivity",
								"**** SUcess response new "
										+ personaMessageAcknowledgementResponse.appstore_request.command_uid);
						Intent aintent = new Intent(this,
								PersonaMainActivity.class);
						startActivity(aintent);

						finish();
					} else {

						Toast.makeText(
								PersonaInstallCertificateActivity.this,
								getResources().getString(
										R.string.error_sending_ack),
								Toast.LENGTH_SHORT).show();
						PersonaLog.d("InstallActivity", "REsponse failure");
						finish();

					}
				}

			} catch (Exception e) {
				e.printStackTrace();
				finish();
			}
			break;
		}


	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// Destroytasks();
	}

	public void sendMDMAcknowledgement(String mComments, int pushCommandStatus) {
/*
		JSONObject holder = null;
		String sDeviceId = Secure.getString(getContentResolver(),
				Secure.ANDROID_ID);
		SharedPreferences newsharedPrefs = getSharedPreferences(
				PersonaConstants.PERSONAPREFERENCESFILE, MODE_PRIVATE);
		String reg_id = newsharedPrefs.getString(
				PersonaConstants.REGISTRATION_ID, "");

		try {
			holder = new JSONObject();

			SharedPreferences email_push_prefs = getSharedPreferences(
					"Email_push_prefs", Context.MODE_PRIVATE);
			String mUid = email_push_prefs.getString("UID", null);
			holder.put(PersonaConstants.COMMAND_UID, mUid);
			holder.put(PersonaConstants.COMMENTS, mComments);
			holder.put(PersonaConstants.DEVICE_ID, sDeviceId);
			holder.put(PersonaConstants.PUSH_COMMAND_STATUS, pushCommandStatus);
			holder.put(PersonaConstants.REGISTRATION_ID, reg_id);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		PersonaLog.d("InstallActivity",
				"PersonaAPIConstants.ACKNOWLEDGEMENT_PUSH_NOTIFICATION : holder :"
						+ holder);
		mSendAcknlowdge = new PersonaJSONClient(
				this,
				personaInstallCertificateActivity,
				holder,
				"Loading...",
				PersonaCertfifcateAuthConstants.ACKNOWLEDGEMENT_CERTIFICATE_DOWNLOAD,
				false, SEND_ACK, true);

		mSendAcknlowdge.execute();*/

	}

	@Override
	public void onRemoteCallComplete(Message json, int code) {
		// TODO Auto-generated method stub

	}

	private void showCertificateWarningScreen(int code) {
		Intent intent = new Intent(this,
				PersonaUserCertificateWarningActivity.class);

		PersonaLog.e(TAG, "" + code);
		intent.putExtra("warningMessageCode", code);
		mContext.startActivity(intent);
		finish();
	}

	private void parseFailureResponse(
			PersonaLoginFailureResponse personaLoginFailureResponse) {

		int errorCode = Integer.parseInt(personaLoginFailureResponse.errorCode);

		switch (errorCode) {

		case 1242:
			showMessage(PersonaCertfifcateAuthConstants.CERT_FAILURE_EVENT,
					PersonaCertfifcateAuthConstants.RE_REQUEST_CERT);
			break;

		
		case 1244:
			
			new SharedPreferences(mContext).edit().putBoolean("crs_rejected", true);
			showCertificateWarningScreen(certificateDowloadFailureErrorcode);
			break;
			
		case 1166:
			showCertificateWarningScreen(certificateDowloadFailureErrorcode);
			break;
		default:
			// showMessage();
			showCertificateWarningScreen(certificateDowloadFailureErrorcode);

	}
	}

	private void showMessage(int event, int action) {
		FragmentManager fm = getSupportFragmentManager();

		PersonaLog.d(TAG, "#################### Event called is : " + event);
		PersonaLog.d(TAG, "#################### Action called is : " + action);
		PersonaSecurityUpdateDialogFragment dialog = new PersonaSecurityUpdateDialogFragment(
				event, action);
		dialog.setRetainInstance(true);
		dialog.show(fm, "fragment_name");

}

}
