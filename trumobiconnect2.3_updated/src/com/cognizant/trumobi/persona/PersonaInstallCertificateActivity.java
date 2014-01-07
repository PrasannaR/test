package com.cognizant.trumobi.persona;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Message;
import android.provider.Settings.Secure;
import android.util.Base64;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.TruBoxSDK.SharedPreferences;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.log.PersonaLog;
import com.cognizant.trumobi.persona.constants.PersonaConstants;
import com.cognizant.trumobi.PersonaEmail_Content_Provider;
import com.cognizant.trumobi.persona.net.PersonaCertificateDownloadResponse;
import com.cognizant.trumobi.persona.net.PersonaGetJsonInterface;
import com.cognizant.trumobi.persona.net.PersonaJSONClient;
import com.cognizant.trumobi.persona.net.PersonaMessageAcknowledgementResponse;
import com.cognizant.trumobi.persona.utils.PersonaCommonfunctions;
import com.cognizant.trumobi.persona.utils.PersonaMDMHashTable;
import com.google.gson.Gson;

public class PersonaInstallCertificateActivity extends Activity implements
		PersonaGetJsonInterface {

	private static final String TAG = PersonaInstallCertificateActivity.class
			.getSimpleName();
	String password;
	private final int CERTIFICATE_DOWNLOAD = 1215;
	private final int SEND_ACK = 1217;
	Context mContext;
	boolean isFinishActivity;
	StringBuilder sb = new StringBuilder();
	PersonaInstallCertificateActivity personaInstallCertificateActivity;
	int certificateNo;
	Dialog alertDialog1;
	String message,title;
	
	TextView show_password_layout, downloadTextView, appNameTextView;
	ImageView image_title, image_title1;
	String email_id;
	PersonaCommonfunctions mCommonfunctions;
	String EASLauncherName;

	PersonaJSONClient mDownloadcertificate, mSendAcknlowdge;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		 setContentView(R.layout.pr_main_activity);
		 mContext = this;
		 mCommonfunctions = new PersonaCommonfunctions(mContext);
		 ProgressBar progressBar =(ProgressBar)findViewById(R.id.progressImageIcon);
		 progressBar.setVisibility(View.INVISIBLE);
		 downloadTextView=(TextView)findViewById(R.id.textView1);
     	appNameTextView=(TextView)findViewById(R.id.textView2);     	
     	downloadTextView.setVisibility(View.INVISIBLE);
     	appNameTextView.setVisibility(View.INVISIBLE);
     	
    
				
		
		personaInstallCertificateActivity = this;
/*		boolean isCertificatedDownloaded = sharedPreferences.getBoolean(
				"certificate_downloaded", false);*/
		
		
		
	
		android.accounts.Account[] accounts = AccountManager.get(mContext)
				.getAccounts();

		String filename = email_id + "_" + certificateNo + ".pfx";
		File f = new File(filename);
		boolean account_present = false;

		for (android.accounts.Account account : accounts) {

			if (account.name.equalsIgnoreCase(email_id)) {
				account_present = true;
			}
		}

		
		
		String encCertificateDownloadKey="",encCertificateDownloadValue="";
		try {
			
			//String preferenceFileName=new String(nBoxContentEncryption.encrypt(PersonaConstants.PERSONAPREFERENCESFILE));
		
			encCertificateDownloadValue=new  SharedPreferences(mContext).getString("certificate_downloaded",
						PersonaConstants.emptyString);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		if (!account_present & encCertificateDownloadValue.equals(PersonaConstants.emptyString)) {
			download_certificate();
		} else {
			message =getString(R.string.certificateAlreadyDownloaded);
			title = getString(R.string.alert);
			isFinishActivity = true;
			mCommonfunctions.showAlertDialog(message, title, isFinishActivity, mContext);
			}

	}

	private void download_certificate() {

		HashMap map = new HashMap();

		JSONObject holder = null;

		try {
			holder = new JSONObject();
			String sDeviceId = Secure.getString(getContentResolver(),
					Secure.ANDROID_ID);
			holder.put("device_uid", sDeviceId);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		PersonaLog.d("PersonaInstallCertificateActivity",
				"PersonaAPIConstants.DOWNLOAD_CERTIFICATE : holder :" + holder);
		mDownloadcertificate = new PersonaJSONClient(mContext,
				personaInstallCertificateActivity, holder, getResources().getString(
						R.string.download_email_certificate),
				PersonaConstants.DOWNLOAD_CERTIFICATE_PERSONA, false,
				CERTIFICATE_DOWNLOAD, false);

		mDownloadcertificate.execute();

	}

	@Override
	public void onRemoteCallComplete(String json, int code) {
		// TODO Auto-generated method stub
		if (code == CERTIFICATE_DOWNLOAD) {
			try {
				PersonaLog.d("PersonaInstallCertificateActivity",
						"PersonaAPIConstants.DOWNLOAD_CERTIFICATE : json :" + json);
				Gson gson = new Gson();
				PersonaCertificateDownloadResponse response = gson.fromJson(json,
						PersonaCertificateDownloadResponse.class);
				if (response != null
						&& response.message_status.contains("Success.")) {

					
					
					
					//String preferenceFileName=new String(nBoxContentEncryption.encrypt(PersonaConstants.PERSONAPREFERENCESFILE));
					new SharedPreferences(mContext).edit().putString("certificate_downloaded","true").commit();

					String filename = response.appstore_information.user_certificate_details.user_email
							+ "_"
							+ response.appstore_request.certificate_request_id
							+ ".pfx";
					password = response.appstore_information.user_certificate_details.certificate_pwd;
					String exchange_server = response.appstore_information.exchange_server;
					String email_domain = response.appstore_information.email_domain;
					String email_id = response.appstore_information.user_certificate_details.user_email;
					String path = response.appstore_information.user_certificate_details.user_cert;
					byte[] convertme = Base64
							.decode(response.appstore_information.user_certificate_details.user_cert,
									Base64.DEFAULT);
					ContentValues values = new ContentValues();
					values.put("email_id", email_id);
					values.put("domain", email_domain);
					values.put("server", exchange_server);
					values.put("password", password);
					values.put("file", convertme);

					String ID = "_id";
					String COL_Email_id = "email_id";
					String COL_Domain = "domain";
					String COL_Server = "server";
					String COL_Password = "password";
					String COL_File = "file";
					String[] list_pro = { ID, COL_Email_id, COL_File,
							COL_Domain, COL_Server, COL_Password };

					Cursor cursor = getContentResolver().query(
							PersonaEmail_Content_Provider.CONTENT_URI, list_pro, null,
							null, null);

					PersonaLog.d("Install Certificate Activity",
							" " + cursor.getCount());

					if (cursor.getCount() == 0) {

						getContentResolver().insert(
								PersonaEmail_Content_Provider.CONTENT_URI, values);
					} else {
						PersonaLog.d("Install Certificate Activity", " " + cursor.getCount()
								+ " :update: ");

						getContentResolver().update(
								PersonaEmail_Content_Provider.CONTENT_URI, values,
								ID + "= 1", null);
					}

					byte[] data = Base64
							.decode(response.appstore_information.user_certificate_details.user_cert,
									Base64.DEFAULT);
					String text = new String(data, "UTF-8");
					Toast.makeText(PersonaInstallCertificateActivity.this,
							getResources().getString(R.string.certificate_downloaded_successfully),
							Toast.LENGTH_LONG).show();
					finish();
				} else {

					Toast.makeText(PersonaInstallCertificateActivity.this,
							getResources().getString(R.string.error_certificate_download), Toast.LENGTH_LONG).show();

					finish();
				}

			} catch (Exception e) {
				e.printStackTrace();
				finish();
				Toast.makeText(PersonaInstallCertificateActivity.this,
						getResources().getString(R.string.error_in_connection), Toast.LENGTH_LONG).show();
			}

		} else if (code == SEND_ACK) {
			try {
				PersonaLog.d("InstallActivity",
						"PersonaAPIConstants.REGISTER_DEVICE : json :" + json);
				Gson gson = new Gson();

				if (json != null) {
					String response = json;
					if (response.contains(PersonaConstants.MSG_SUCCESS)) {

						new SharedPreferences(mContext) .edit().putString("MDMAction", null).commit();
						new SharedPreferences(mContext) .edit().putInt("CertificateNo", 0).commit();
						new SharedPreferences(mContext) .edit().putString("UID",null).commit();

						PersonaMessageAcknowledgementResponse personaMessageAcknowledgementResponse = gson
								.fromJson(json,
										PersonaMessageAcknowledgementResponse.class);

						PersonaMDMHashTable.email_initiate
								.remove(personaMessageAcknowledgementResponse.appstore_request.command_uid);

						String successResponse = personaMessageAcknowledgementResponse.display_message;
						PersonaLog.d("InstallActivity",
								"**** SUcess response new "
										+ personaMessageAcknowledgementResponse.appstore_request.command_uid);
						Intent aintent = new Intent(this, PersonaMainActivity.class);
						startActivity(aintent);

						finish();
					} else {

						Toast.makeText(PersonaInstallCertificateActivity.this,
								getResources().getString(R.string.error_sending_ack), Toast.LENGTH_SHORT).show();
						PersonaLog.d("InstallActivity", "REsponse failure");
						finish();

					}
				}

			} catch (Exception e) {
				e.printStackTrace();
				finish();
			}

		}

	}


	@Override
	protected void onDestroy() {
		super.onDestroy();

		// Destroytasks();
	}

	public void sendMDMAcknowledgement(String mComments, int pushCommandStatus) {

		JSONObject holder = null;
		String sDeviceId = Secure.getString(getContentResolver(),
				Secure.ANDROID_ID);
	
		String reg_id = new SharedPreferences(mContext).getString(PersonaConstants.REGISTRATION_ID, "");

		try {
			holder = new JSONObject();

			String mUid=new SharedPreferences(mContext).getString("UID", null);
			holder.put(PersonaConstants.COMMAND_UID, mUid);
			holder.put(PersonaConstants.COMMENTS, mComments);
			holder.put(PersonaConstants.DEVICE_ID, sDeviceId);
			holder.put(PersonaConstants.PUSH_COMMAND_STATUS,
					pushCommandStatus);
			holder.put(PersonaConstants.REGISTRATION_ID, reg_id);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		PersonaLog.d("InstallActivity",
				"PersonaAPIConstants.ACKNOWLEDGEMENT_PUSH_NOTIFICATION : holder :"
						+ holder);
		mSendAcknlowdge = new PersonaJSONClient(this, personaInstallCertificateActivity,
				holder, "Loading...",
				PersonaConstants.ACKNOWLEDGEMENT_PUSH_NOTIFICATION, false,
				SEND_ACK, true);

		mSendAcknlowdge.execute();

	}

	@Override
	public void onRemoteCallComplete(Message json, int code) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRemoteCallComplete(String[] json, int code) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRemoteCallComplete(ArrayList<String> json, int code) {
		// TODO Auto-generated method stub

	}

}
