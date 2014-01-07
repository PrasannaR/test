package com.cognizant.trumobi.persona;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Base64;

import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;



import com.TruBoxSDK.SharedPreferences;
import com.TruBoxSDK.TruboxException;
import com.TruBoxSDK.TruBoxContentEncryption.DataType;
import com.cognizant.trumobi.R;

import com.cognizant.trumobi.log.PersonaLog;
import com.cognizant.trumobi.persona.constants.PersonaCertfifcateAuthConstants;
import com.cognizant.trumobi.persona.constants.PersonaConstants;

import com.cognizant.trumobi.persona.net.PersonaGetJsonInterface;
import com.cognizant.trumobi.persona.net.PersonaLoginResponse;
import com.cognizant.trumobi.persona.utils.PersonaCommonfunctions;
import com.cognizant.trumobi.persona.utils.PersonaRegistrationTask;
import com.google.gson.Gson;

public class PersonaRegistrationActivity extends Activity implements PersonaGetJsonInterface {

	PersonaCommonfunctions mCommonfunctions;
	Context mContext;
	//Button loginButton, cancelButton;
	String mDeviceId, mSerialId, mImei, mMacAddress;
	String mUDID, mModel, mMake;
	String mUserName, mPassword;
	Handler regCallBack;
	ProgressDialog dialog;
	
	EditText mUsernameEditText, mPasswordEditText;
	PersonaRegistrationActivity personaRegistrationActivity;
	ProgressDialog updatingPolicyProgress;
	PersonaRegistrationTask regTask;
	String message,title;
	boolean callForGetPinService,isFinishActivity;
	int code;
	int authenticationCode = 1001, forgotPincode = 1002;
	
	RelativeLayout mRelativeLayoutLogin;
	TextView mHeaderTextView, msubHeaderTextView;
	LinearLayout prSubHeaderLinearLayout;

	//290778 UI related changes made
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		mContext = this;
		personaRegistrationActivity = this;
		mCommonfunctions = new PersonaCommonfunctions(mContext);
		
		setContentView(R.layout.pr_login_phone);
		mUsernameEditText = (EditText) findViewById(R.id.UsernameEditText);
		mPasswordEditText = (EditText) findViewById(R.id.PasswordEditText);
	//	loginButton = (Button) findViewById(R.id.LoginButton);
	
		mRelativeLayoutLogin=(RelativeLayout)findViewById(R.id.relativeLayoutLogin);
		mHeaderTextView=(TextView)findViewById(R.id.prCaption);
		msubHeaderTextView=(TextView)findViewById(R.id.prSubCaption);
		prSubHeaderLinearLayout=(LinearLayout)findViewById(R.id.prSubHeader);
		
		Typeface normalTf = Typeface.createFromAsset(getAssets(),"fonts/Roboto-Regular.ttf");
		mUsernameEditText.setTypeface(normalTf,Typeface.NORMAL);
		mPasswordEditText.setTypeface(normalTf,Typeface.NORMAL);
		//loginButton.setTypeface(normalTf,Typeface.NORMAL);
		msubHeaderTextView.setTypeface(normalTf,Typeface.NORMAL);
		
		
		Typeface boldTf = Typeface.createFromAsset(getAssets(),"fonts/Roboto-Bold.ttf");
		mHeaderTextView.setTypeface(boldTf,Typeface.BOLD);
		
		
		
		callForGetPinService = getIntent().getBooleanExtra(
				"CalledFromLocalAuthentication", false);

		if (callForGetPinService) {
	//		loginButton.setText(R.string.submitButton);
			prSubHeaderLinearLayout.setVisibility(View.GONE);
			
		} else {

		}

		
		
		//loginButton = (Button) findViewById(R.id.LoginButton);
		/*loginButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(getCurrentFocus()
						.getWindowToken(), 0);
				if(imm.isActive())
				{
					imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
				}
				mUserName = mUsernameEditText.getText().toString();
				mPassword = mPasswordEditText.getText().toString();
				if(mCommonfunctions.isConnectedToNetwork())
				validateTextFields();

				else {
					
					Toast.makeText(PersonaRegistrationActivity.this, "Please connect to Internet",
							Toast.LENGTH_LONG).show();
					finish();
				}
				

			}

		

		});
		*/
		
		mPasswordEditText.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {
			
				
				if (actionId == EditorInfo.IME_ACTION_DONE
						|| event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
		
					mUserName = mUsernameEditText.getText().toString();
					mPassword = mPasswordEditText.getText().toString();
					if(mCommonfunctions.isConnectedToNetwork())
					validateTextFields();

					else {
						message = getString(R.string.noconnection);
						title = getString(R.string.error_in_connection);
						isFinishActivity = false;
						mCommonfunctions.showAlertDialog(message, title, isFinishActivity, mContext);
					}
					
				
				}
				return false;
			}
		});
		
		mUsernameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if(hasFocus)
				{
					mUsernameEditText.setTextColor(getResources().getColor(R.color.PR_TEXTBOX_FOCUSED_COLOR));
				
			}
				else{
					mUsernameEditText.setTextColor(getResources().getColor(R.color.PR_TEXTBOX_NORMAL_COLOR));
				}
			}
		});
		mPasswordEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if(hasFocus)
				{
					mPasswordEditText.setTextColor(getResources().getColor(R.color.PR_TEXTBOX_FOCUSED_COLOR));
			}
				else{
					mPasswordEditText.setTextColor(getResources().getColor(R.color.PR_TEXTBOX_NORMAL_COLOR));
				}
			}
		});
			
	}
	private void validateTextFields() {

		if (mUserName.equals("") || mPassword.equals(""))
			Toast.makeText(mContext, "Fields cannot be empty",
					Toast.LENGTH_SHORT).show();
		else {
			if (!isDeviceRegisteredWithGCM()) {
				message = getString(R.string.deviceNotRegisteredWithGCM);
				title = getString(R.string.alert);
				isFinishActivity = true;
				mCommonfunctions.showAlertDialog(message, title, isFinishActivity, mContext);
			}
			 else {
				getDeviceDetails();
				// showProgressDialog();
				updatingPolicyProgress = new ProgressDialog(mContext,R.style.PersonaNewDialog);
				updatingPolicyProgress.setCancelable(false);
			
				if (callForGetPinService) {
					updatingPolicyProgress.setMessage("Authenticating user...");
					updatingPolicyProgress.show();
					 regTask = new PersonaRegistrationTask(
							mContext, personaRegistrationActivity,
							regCallBack, forgotPincode);
					regTask.execute(mUserName, mPassword, mDeviceId,
							mSerialId, mImei, mMacAddress, mUDID,
							mMake, mModel,
							PersonaConstants.FORGET_PIN_SERVICE);
				} else {
					
					updatingPolicyProgress.setMessage("Registering Device...");
					updatingPolicyProgress.show();
					PersonaRegistrationTask regTask = new PersonaRegistrationTask(
							mContext, personaRegistrationActivity,
							regCallBack, authenticationCode);
					regTask.execute(mUserName, mPassword, mDeviceId,
							mSerialId, mImei, mMacAddress, mUDID,
							mMake, mModel,
							PersonaCertfifcateAuthConstants.AUTHENTICATION_SERVICE_PERSONA);
				}


			}
		}

	}
	private void getDeviceDetails() {

		mDeviceId = mCommonfunctions.getDeviceID();
		mSerialId = getSerialNumber();
		mImei = getIMEI();
		mMacAddress = getMacAddress();
		mMake = Build.MANUFACTURER;
		mModel = Build.MODEL;

	}

	private boolean isDeviceRegisteredWithGCM() {
		mUDID = getRegistrationId();
		if (mUDID.equals(PersonaConstants.GCMError)) {
			return false;
		}
		return true;

	}

	private String getRegistrationId() {
		String registrationId="";
		String preferenceFileName="";
		try {
			 //preferenceFileName=new String(nBoxContentEncryption.encrypt(PersonaConstants.PERSONAPREFERENCESFILE));
			
			 registrationId =new  SharedPreferences(mContext).getString(PersonaConstants.REGISTRATION_ID,
						PersonaConstants.emptyString);
			 
			
			 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return registrationId;
	}

	private String getMacAddress() {
		String mMac;
		WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = manager.getConnectionInfo();
		mMac = info.getMacAddress();
		return mMac;
	}

	private String getIMEI() {

		String mImei;
		TelephonyManager telephonyManager = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		mImei = telephonyManager.getDeviceId();
		return mImei;

	}

	private String getSerialNumber() {
		return android.os.Build.SERIAL;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onRemoteCallComplete(Message msg, int code) {

		// TODO Auto-generated method stub
		PersonaLog.i("onRemotecallcomplete", "registration service");

		if (updatingPolicyProgress!= null && updatingPolicyProgress.isShowing()) {
			
			updatingPolicyProgress.cancel();
			updatingPolicyProgress = null;
			
		}
		if (code == authenticationCode)

		{
			Bundle bundle = msg.getData();
			boolean result = bundle.getBoolean(PersonaConstants.result);
			String responseMessage = bundle
					.getString(PersonaConstants.responseMessage);
			int mResponseStatusCode = bundle
					.getInt(PersonaConstants.responseStatusCode);
			int mErrorcode;

			if (result) {

				
				if(!isPinSaved()) {
			Intent localAuthentication = new Intent(
						PersonaRegistrationActivity.this, PersonaLocalAuthentication.class);
				localAuthentication.putExtra("callDeviceProvisioning",true);
				localAuthentication.putExtra("username",mUserName);
				localAuthentication.putExtra("mail_id",mUserName+"@truboxmdmdev.com" );
				PersonaLog.d("", "======== passs: "+mPassword);
				localAuthentication.putExtra("password",mPassword);
				startActivity(localAuthentication);
				finish();
				}
				else{
					// call email setup page
				}
				
				
			/*	Intent EmailSetupIntent = new Intent(
					PersonaRegistrationActivity.this, EmailWelcomeActivity.class);
					EmailSetupIntent.putExtra("username",mUserName);
					EmailSetupIntent.putExtra("mail_id",mUserName+"@truboxmdmdev.com" );
					PersonaLog.d("", "======== passs: "+mPassword);
					EmailSetupIntent.putExtra("password",mPassword);
					EmailSetupIntent.putExtra("callDeviceProvisioning",true);
					startActivity(EmailSetupIntent);
					finish();*/

			} else if (mResponseStatusCode == PersonaConstants.RESPONSE500) {

				mErrorcode = bundle.getInt(PersonaConstants.appstoreErrorCode);
				switch (mErrorcode) {
				case 1007:
					Toast.makeText(mContext, "Invalid login credentials",
							Toast.LENGTH_SHORT).show();
					break;
				case 9999:
					Toast.makeText(mContext, "Error in connectivity",
							Toast.LENGTH_SHORT).show();
					break;
				case 1106:
					Toast.makeText(mContext, "Server Unavailable, Please try later",
							Toast.LENGTH_SHORT).show();
					break;
				default:
					Toast.makeText(mContext, "Server Unavailable, Please try later",
							Toast.LENGTH_SHORT).show();
					break;
				}

			}
			

		}
		if (code == forgotPincode) {

			Bundle bundle = msg.getData();
			boolean result = bundle.getBoolean(PersonaConstants.result);
			String responseMessage = bundle
					.getString(PersonaConstants.responseMessage);
			int mResponseStatusCode = bundle
					.getInt(PersonaConstants.responseStatusCode);
			int mErrorcode;

			if (result) {

				Gson gson = new Gson();
				PersonaLoginResponse response = gson.fromJson(responseMessage,
						PersonaLoginResponse.class);

				if (!response.appstore_information.is_owner) {

					showAlertDialog();
				} else {
					 new SharedPreferences(mContext).edit(). putString("persona_pin","").commit();

					Intent localAuthentication = new Intent(
							PersonaRegistrationActivity.this,
							PersonaLocalAuthentication.class);
					localAuthentication
							.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					localAuthentication.putExtra("CalledAfterForgotPin", true);
					startActivity(localAuthentication);
					finish();
				}
			} else if (mResponseStatusCode == PersonaConstants.RESPONSE500) {

				mErrorcode = bundle.getInt(PersonaConstants.appstoreErrorCode);
				switch (mErrorcode) {
				case 1007:
					Toast.makeText(mContext, "Invalid login credentials",
							Toast.LENGTH_SHORT).show();
					break;
				case 9999:
					Toast.makeText(mContext, "Error in connectivity",
							Toast.LENGTH_SHORT).show();
					break;
				case 1106:
					Toast.makeText(mContext, "Server Unavailable, Please try later",
							Toast.LENGTH_SHORT).show();
					break;
				}

			}
		}

	}
	
	private boolean isPinSaved() {
		
		 String personaSavedPinValue = "",encPersonaPinKey="";
		try {
			//encPersonaPinKey = new String (Base64.encode(nBoxContentEncryption.encrypt("persona_pin"),Base64.DEFAULT));
			 personaSavedPinValue = new SharedPreferences(mContext).getString("persona_pin",
					PersonaConstants.emptyString);
			 //decPersonaPin = (String) nBoxContentEncryption.decrypt(Base64.decode(personaSavedPinValue,Base64.DEFAULT), DataType.STRING);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(personaSavedPinValue.equals(PersonaConstants.emptyString)) {
		return false;
		}
		return true;
		
	}

	private void showAlertDialog() {
		final AlertDialog alertDialog = new AlertDialog.Builder(this).create();

		String message = "You are not an authorised user for this device";

		alertDialog.setMessage(message);
		alertDialog.setTitle("Alert !!!");
		alertDialog.setIcon(R.drawable.pr_app_icon);
		alertDialog.setCancelable(false);
		alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "OK",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {

						alertDialog.dismiss();

					}
				});

		alertDialog.show();

	}

	@Override
	public void onRemoteCallComplete(String json, int code) {
		// TODO Auto-generated method stub

	}
	
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(regTask!=null)
			regTask.cancel(true);
	}


	
	

}
