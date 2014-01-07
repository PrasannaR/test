package com.cognizant.trumobi.persona;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.TruBoxSDK.SharedPreferences;
import com.TruBoxSDK.TruBoxDatabase;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.externaladapter.ExternalAdapterListener;
import com.cognizant.trumobi.externaladapter.ExternalAdapterRegistrationClass;
import com.cognizant.trumobi.externaladapter.ExternalEmailSettingsInfo;
import com.cognizant.trumobi.log.PersonaLog;
import com.cognizant.trumobi.persona.PersonaSecurityUpdateDialogFragment.DialogCancelListener;
import com.cognizant.trumobi.persona.constants.PersonaCertfifcateAuthConstants;
import com.cognizant.trumobi.persona.constants.PersonaConstants;
import com.cognizant.trumobi.persona.net.PersonaGetJsonInterface;
import com.cognizant.trumobi.persona.utils.Customkeyboard;
import com.cognizant.trumobi.persona.utils.PersonaCommonfunctions;
import com.cognizant.trumobi.persona.utils.PersonaRegistrationTask;
import com.quintech.rovacommon.rovashared;

public class PersonaLoginActivity extends FragmentActivity implements
		PersonaGetJsonInterface ,DialogCancelListener{

	PersonaCommonfunctions mCommonfunctions;
	Context mContext;
	Button loginButton, cancelButton;
	String mDeviceId, mSerialId, mImei, mMacAddress;
	String mUDID, mModel, mMake;
	String mUserName, mPassword;
	Handler regCallBack;
	ProgressDialog dialog;
	String mDomain, mServer;
	SharedPreferences sharedPreferences;
	EditText mUsernameEditText, mPasswordEditText;
	PersonaLoginActivity personaLoginActivity;
	ProgressDialog updatingPolicyProgress;
	PersonaRegistrationTask regTask;
	String message, title;
	rovashared rova;
	File mDB;
	TruBoxDatabase mPersonaDB;
	boolean callForGetPinService, isFinishActivity;
	int code;
	PersonaLoginActivity mActivity;
	HashMap<String, String> myDeviceDetails;
	int authenticationCode = 1001, forgotPincode = 1002, loginCode = 1003;

	LinearLayout mRelativeLayoutLogin;
	TextView mHeaderTextView, msubHeaderTextView;
	int mPwdLength, mLocalauthtype;
	ExternalAdapterRegistrationClass mExtAdapReg;
	static String TAG = PersonaLoginActivity.class.getName();

	Customkeyboard customKeyBoard, customKeyBoard1;
	Button mDoneButton, mDoneButton2;
	int certificateDowloadFailureErrorcode=2001;
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		mContext = this;
		mActivity = this;
		String s = new SharedPreferences(mContext).getString(
				"trumobi_username", "defString");
		PersonaLog.d(TAG, "After deleting data: username:" + s);
		personaLoginActivity = this;
		mCommonfunctions = new PersonaCommonfunctions(mContext);

		setContentView(R.layout.pr_login_phone);
		mUsernameEditText = (EditText) findViewById(R.id.UsernameEditText);
		mPasswordEditText = (EditText) findViewById(R.id.PasswordEditText);

		//loginButton = (Button) findViewById(R.id.LoginButton);
		
		customKeyBoard = (Customkeyboard)findViewById(R.id.key_edit);
		customKeyBoard.setQwertyKeyBoard(true);
		customKeyBoard.setEdit(mUsernameEditText);
		customKeyBoard1 = (Customkeyboard)findViewById(R.id.key_edit1);
		customKeyBoard1.setQwertyKeyBoard(true);
		customKeyBoard.setVisibility(View.VISIBLE);
		customKeyBoard1.setVisibility(View.GONE);
		mRelativeLayoutLogin = (LinearLayout) findViewById(R.id.relativeLayoutLogin);
		mHeaderTextView = (TextView) findViewById(R.id.prCaption);
		msubHeaderTextView = (TextView) findViewById(R.id.prSubCaption);
		mDoneButton = (Button) customKeyBoard.findViewById(R.id.button47);
		//mDoneButton.setOnClickListener(submitOnclickListener);

		mDoneButton2 = (Button) customKeyBoard1.findViewById(R.id.button47);
	
	//	mDoneButton2.setOnClickListener(submitOnclickListener);

	

		mRelativeLayoutLogin = (LinearLayout) findViewById(R.id.relativeLayoutLogin);
		mHeaderTextView = (TextView) findViewById(R.id.prCaption);
		msubHeaderTextView = (TextView) findViewById(R.id.prSubCaption);


		Typeface normalTf = Typeface.createFromAsset(getAssets(),
				"fonts/Roboto-Regular.ttf");
		mUsernameEditText.setTypeface(normalTf, Typeface.NORMAL);
		mPasswordEditText.setTypeface(normalTf, Typeface.NORMAL);
	//	loginButton.setTypeface(normalTf, Typeface.NORMAL);
		msubHeaderTextView.setTypeface(normalTf, Typeface.NORMAL);

		Typeface boldTf = Typeface.createFromAsset(getAssets(),
				"fonts/Roboto-Bold.ttf");
		mHeaderTextView.setTypeface(boldTf, Typeface.BOLD);

		IntentFilter intentFilter =new IntentFilter("com.cognizant.trumobi.showtoast");
		registerReceiver(showToastReceiver,intentFilter);
		mDoneButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

				if (imm.isActive()) {
					imm.hideSoftInputFromWindow(v.getApplicationWindowToken(),
							0);
				}
				mUserName = mUsernameEditText.getText().toString();
				mPassword = mPasswordEditText.getText().toString();
				if (mCommonfunctions.isConnectedToNetwork())
					
					
					if (customKeyBoard.getVisibility() == 0) {
						mPasswordEditText.requestFocus();
					} else {
						validateTextFields();
					}

				else {

					Toast.makeText(PersonaLoginActivity.this,
							"Please connect to Internet", Toast.LENGTH_LONG)
							.show();
					//finish();
				}

			}

		});
		
		mDoneButton2.setOnClickListener(new View.OnClickListener() {


			@Override
			public void onClick(View v) {

				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

				if (imm.isActive()) {
					imm.hideSoftInputFromWindow(v.getApplicationWindowToken(),
							0);
				}
				mUserName = mUsernameEditText.getText().toString();
				mPassword = mPasswordEditText.getText().toString();
				if (mCommonfunctions.isConnectedToNetwork())
					validateTextFields();

				else {

					Toast.makeText(PersonaLoginActivity.this,
							"Please connect to Internet", Toast.LENGTH_LONG)
							.show();
					// finish();
				}

			}

		});

		mPasswordEditText
				.setOnEditorActionListener(new OnEditorActionListener() {

					@Override
					public boolean onEditorAction(TextView textView,
							int actionId, KeyEvent event) {

						if (actionId == EditorInfo.IME_ACTION_DONE
								|| event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

							mUserName = mUsernameEditText.getText().toString();
							mPassword = mPasswordEditText.getText().toString();
							if (mCommonfunctions.isConnectedToNetwork())
								validateTextFields();

							else {
								message = getString(R.string.noconnection);
								title = getString(R.string.error_in_connection);
								isFinishActivity = false;
								mCommonfunctions.showAlertDialog(message,
										title, isFinishActivity, mContext);
							}

						}
						return false;
					}
				});

		
		mUsernameEditText.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				mUsernameEditText.requestFocus();
				customKeyBoard.setVisibility(View.VISIBLE);
				customKeyBoard.setEdit(mUsernameEditText);
				customKeyBoard1.setVisibility(View.GONE);

				return true;
			}
		});

		mPasswordEditText.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub

				mPasswordEditText.requestFocus();
				customKeyBoard1.setVisibility(View.VISIBLE);
				customKeyBoard1.setEdit(mPasswordEditText);
				customKeyBoard.setVisibility(View.GONE);

				return true;
			}
		});
		mUsernameEditText
				.setOnFocusChangeListener(new View.OnFocusChangeListener() {

					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						// TODO Auto-generated method stub
						
						InputMethodManager imm = (InputMethodManager)getSystemService(
							      Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(mUsernameEditText.getWindowToken(), 0);
						if (hasFocus) {

							customKeyBoard.setVisibility(View.VISIBLE);
							customKeyBoard1.setVisibility(View.GONE);
							customKeyBoard.setEdit(mUsernameEditText);
							
							mUsernameEditText
									.setTextColor(getResources().getColor(
											R.color.PR_TEXTBOX_NORMAL_COLOR));


						} else {
							mUsernameEditText
									.setTextColor(getResources().getColor(
											R.color.PR_TEXTBOX_FOCUSED_COLOR));
						}
					}
				});
		mPasswordEditText
				.setOnFocusChangeListener(new View.OnFocusChangeListener() {

					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						// TODO Auto-generated method stub
					
						InputMethodManager imm = (InputMethodManager)getSystemService(
							      Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(mPasswordEditText.getWindowToken(), 0);
						if (hasFocus) {

							mDoneButton2
							.setBackgroundDrawable(mContext
									.getResources()
									.getDrawable(
											R.drawable.pr_key_done_selector));
							customKeyBoard.setVisibility(View.GONE);
							customKeyBoard1.setVisibility(View.VISIBLE);
							customKeyBoard1.setEdit(mPasswordEditText);

						} else {

							mPasswordEditText
									.setTextColor(getResources().getColor(
											R.color.PR_TEXTBOX_FOCUSED_COLOR));
						}
					}
				});

		registerReceiver(UpdateUIReceiver, new IntentFilter(
				"UpdateLoginScreenUI"));

		/*
		 * final rovashared libtest = new rovashared(getBaseContext());
		 * PIMAuthType test = libtest.GetPIMAuthenticationTypeInformation();
		 * RegistrationInfo testinfo = libtest.GetRegistrationInformation();
		 * PersonaLog
		 * .d("PersonaLoginActivity","Is Registered"+libtest.isRegistered());
		 * reg = new Registrationlistner() {
		 * 
		 * @Override public boolean onRegistered(boolean bRegistered) {
		 * PersonaLog.v("ROVA"," ROVA Reg EVENT FIRED");
		 * 
		 * PersonaLog.v("ROVA"," ROVA Reg EVENT FIRED next" +
		 * libtest.isRegistered()); getEmailSettings(); // flag to be sent back
		 * return true; }
		 * 
		 * @Override public void OnFailed(String error, int errorCode) {
		 * PersonaLog.v("ROVA","ROVA Failed EVENT FIRED");
		 * 
		 * // TODO Auto-generated method stub } };
		 * libtest.StartRegistration(null, reg);
		 * 
		 * 
		 * myListener = new Registrationlistner() {
		 * 
		 * @Override public boolean onRegistered(boolean arg0) { // TODO
		 * Auto-generated method stub
		 * PersonaLog.d(PersonaLoginActivity.class.getSimpleName
		 * (),"Registered callback returned success"); getEmailSettings();
		 * return true; }
		 * 
		 * 
		 * 
		 * @Override public void OnFailed(String arg0, int arg1) { // TODO
		 * Auto-generated method stub
		 * PersonaLog.d(PersonaLoginActivity.class.getSimpleName
		 * (),"Registered callback returned failure"); } };
		 */

	}

	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		 getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
			
			
			
	}
	protected void onDestroy() {
		super.onDestroy();

		if (updatingPolicyProgress != null
				&& updatingPolicyProgress.isShowing()) {

			updatingPolicyProgress.cancel();
			updatingPolicyProgress = null;

		}
		if (showToastReceiver != null) {
			unregisterReceiver(showToastReceiver);
		}
		try {
			unregisterReceiver(UpdateUIReceiver);
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	/**
	 * ExternalAdapterListener to listen the callback from ExternalAdapterClass
	 * 
	 */

	ExternalAdapterListener extAdapListener = new ExternalAdapterListener() {

		/*
		 * (non-Javadoc)
		 * 
		 * @see com.cognizant.trumobi.externaladapter.ExternalAdapterListener#
		 * onExternalAdapterRegistered(boolean)
		 */
		@Override
		public boolean onExternalAdapterRegistered(boolean bRegistered) {
			// TODO Auto-generated method stub
			PersonaLog.d(TAG, "=== In onExternalAdapterRegistered ====="
					+ bRegistered);

			/*
			 * Once onExternalAdapterRegistered() callback is called, Check for
			 * the method isAllRegisteredInformationAvailable() - in
			 * ExternalAdapterRegistrationClass This will check ,whether all the
			 * external provided methods are available or not. if the return
			 * value is TRUE , proceed with displaying PIN / Password screen by
			 * reading the exterPIMSettingsInfo() method. If the return value is
			 * FALSE , call initiateFetchAllInformation() This method would call
			 * external API and external SDK would trigger broad cast intent
			 * SETTINGS_FETCH_INFO_COMPLETE event ,check for
			 * isAllRegisteredInformationAvailable() method there ,based on the
			 * return value ,call PIN /PASSWORD entry screen from there.
			 */
			// ROVA_SDK_CHG
	
				// isRegistered = false; //ROVA_SDK_CHG_CHECK
				boolean isAllInfoAvail = mExtAdapReg
						.isAllRegisteredInformationAvailable();
				if (isAllInfoAvail) {
					saveUserNamePwd();
					PersonaLog.d(PersonaLoginActivity.class.getSimpleName(),
							"Registered truue");
					Intent trumobiReceiverIntent = new Intent(
							"UpdateLoginScreenUI");
					mContext.sendBroadcast(trumobiReceiverIntent);
				}

				else {
					PersonaLog.d(PersonaLoginActivity.class.getSimpleName(),
							"Registered false");
					mExtAdapReg.initiateExternalToFetchAllSettingsInfo();
				}
			
			return true;
		}

		@Override
		public void onExternalAdapterFailed(String error, int errorCode) {
			// TODO Auto-generated method stub

			// Error message from sdk is "Invalid username or password" even
			// when Connection is timed out. So Generic message used
			PersonaLog.d(PersonaLoginActivity.class.getSimpleName(),
					"Registered callback returned failure");
			Intent updateUI = new Intent("UpdateLoginScreenUI");
			updateUI.putExtra("safe_method_failed", true);
			updateUI.putExtra("error_code", 1001);
			mContext.sendBroadcast(updateUI);
			// saveUserNamePwd();
		}

	};

	/*
	 * Registrationlistner myListener = new Registrationlistner() {
	 * 
	 * @Override public boolean onRegistered(boolean arg0) { // TODO
	 * Auto-generated method stub
	 * PersonaLog.d(PersonaMainActivity.class.getSimpleName
	 * (),"Registered callback returned success"); if (updatingPolicyProgress!=
	 * null && updatingPolicyProgress.isShowing()) {
	 * 
	 * updatingPolicyProgress.cancel(); updatingPolicyProgress = null;
	 * 
	 * }
	 * 
	 * if(!isPinSaved()) { saveUserDetails(); savePIMSettings();
	 * 
	 * Intent localAuthentication = new Intent( PersonaLoginActivity.this,
	 * PersonaLocalAuthentication.class);
	 * 
	 * localAuthentication.putExtra("username",mUserName); //
	 * localAuthentication.putExtra("mail_id",mUserName+"@truboxmdmdev.com" );
	 * localAuthentication.putExtra("mail_id",mUserName);
	 * localAuthentication.putExtra("password",mPassword);
	 * localAuthentication.putExtra("mLocalAuthType",mLocalauthtype);
	 * localAuthentication.putExtra("mPwdLength",mPwdLength);
	 * localAuthentication.putExtra("domain","truboxmdmdev.com");
	 * localAuthentication.putExtra("server","trubox.cognizant.com");
	 * 
	 * 
	 * startActivity(localAuthentication); finish(); } else{ getEmailSettings();
	 * Intent EmailSetupIntent = new Intent( PersonaLoginActivity.this,
	 * EmailWelcomeActivity.class);
	 * EmailSetupIntent.putExtra("username",mUserName); //
	 * EmailSetupIntent.putExtra("mail_id",mUserName+"@truboxmdmdev.com" );
	 * EmailSetupIntent.putExtra("mail_id",mUserName);
	 * EmailSetupIntent.putExtra("password",mPassword);
	 * EmailSetupIntent.putExtra("callDeviceProvisioning",true);
	 * EmailSetupIntent.putExtra("domain","truboxmdmdev.com");
	 * EmailSetupIntent.putExtra("server","trubox.cognizant.com");
	 * EmailSetupIntent.putExtra("domain",mDomain);
	 * EmailSetupIntent.putExtra("server",mServer);
	 * startActivity(EmailSetupIntent); finish(); }
	 * 
	 * return true; }
	 * 
	 * 
	 * @Override public void OnFailed(String arg0, int arg1) { // TODO
	 * Auto-generated method stub
	 * PersonaLog.d(PersonaLoginActivity.class.getSimpleName
	 * (),"Registered callback returned failure"); if (updatingPolicyProgress!=
	 * null && updatingPolicyProgress.isShowing()) {
	 * 
	 * updatingPolicyProgress.cancel(); updatingPolicyProgress = null;
	 * 
	 * } } };
	 */

	private boolean isPinSaved() {

		String personaSavedPinValue = "", encPersonaPinKey = "";
		/*
		 * try { encPersonaPinKey = new String
		 * (Base64.encode(nBoxContentEncryption
		 * .encrypt("persona_pin"),Base64.DEFAULT)); personaSavedPinValue =
		 * sharedPreferences.getString(encPersonaPinKey,
		 * PersonaConstants.emptyString);
		 * 
		 * //decPersonaPin = (String)
		 * nBoxContentEncryption.decrypt(Base64.decode
		 * (personaSavedPinValue,Base64.DEFAULT), DataType.STRING); } catch
		 * (TruboxException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); }
		 */

		// personaSavedPinValue = new
		// SharedPreferences(this).getString("persona_pin",PersonaConstants.emptyString);
		// if(personaSavedPinValue.equals(PersonaConstants.emptyString)) {
		boolean isPinSaved = new SharedPreferences(this).getBoolean(
				"isPersonaKeySaved", false);
		if (isPinSaved) {
			return true;
		}
		return false;

	}

	private void validateTextFields() {

		/*
		 * PersonaLocalAuthentication mLocalAuth = new
		 * PersonaLocalAuthentication();
		 * mLocalAuth.bindLoginActivityContext(mContext,
		 * mActivity,getBaseContext()); mLocalAuth.createView(0);
		 */

		if (mUserName.equals("") || mPassword.equals(""))
			Toast.makeText(mContext, "Fields cannot be empty",
					Toast.LENGTH_SHORT).show();
		else {
			/*
			 * if (!isDeviceRegisteredWithGCM()) { message =
			 * getString(R.string.deviceNotRegisteredWithGCM); title =
			 * getString(R.string.alert); isFinishActivity = true;
			 * mCommonfunctions.showAlertDialog(message, title,
			 * isFinishActivity, mContext); } else { getDeviceDetails();
			 */
			// showProgressDialog();

			// 290778 modified for forget pin service check for the username
			if (updatingPolicyProgress == null)
				updatingPolicyProgress = new ProgressDialog(mContext,
						R.style.PersonaNewDialog);

			updatingPolicyProgress.setCancelable(false);

			if (callForGetPinService) {
				updatingPolicyProgress.setMessage("Authenticating user...");
				updatingPolicyProgress.show();
				regTask = new PersonaRegistrationTask(mContext,
						personaLoginActivity, regCallBack, forgotPincode);
				regTask.execute(mUserName, mPassword, mDeviceId, mSerialId,
						mImei, mMacAddress, mUDID, mMake, mModel,
						PersonaConstants.FORGET_PIN_SERVICE);
			} else {

				if (PersonaMainActivity.isRovaPoliciesOn) {
					updatingPolicyProgress.setMessage("Authenticating...");
					updatingPolicyProgress.show();
					// ROVA_SDK_CHG

					mExtAdapReg = ExternalAdapterRegistrationClass
							.getInstance(mContext);
					getDeviceDetails();

					boolean bExtAdapRegistered = mExtAdapReg.isExternalAdapterRegistered();
					PersonaLog.d(TAG," ExternalAdapter - ROVA registered value"+bExtAdapRegistered);
					mExtAdapReg.StartRegistrationExternalAdapter(myDeviceDetails, extAdapListener);
			
				/*	if(!bExtAdapRegistered) {
						
						
				
					}
					else {
						PersonaLog.d(TAG," ExternalAdapter - ROVA registered value"+bExtAdapRegistered);
					}*/
				}
				else {
					
					if(PersonaMainActivity.isCertificateBasedAuth){
						PersonaLog.d(TAG," isCognizantCertificateBasedAuth");
						updatingPolicyProgress
						.setMessage("Authenticating...");
						updatingPolicyProgress.show();
						PersonaRegistrationTask regTask = new PersonaRegistrationTask(
								mContext, personaLoginActivity, regCallBack, authenticationCode);
						regTask.execute(mUserName, mPassword,
								PersonaCertfifcateAuthConstants.AUTHENTICATION_SERVICE_PERSONA);
						
					}
					else{
						PersonaLog.d(TAG,"Form based");
					updatingPolicyProgress
					.setMessage("Authenticating...");

					updatingPolicyProgress.show();
					PersonaRegistrationTask regTask = new PersonaRegistrationTask(
							mContext, personaLoginActivity, regCallBack,
							loginCode);
					regTask.execute(mUserName, mPassword,
							PersonaConstants.ROVA_TRUMOBI_LOGIN_SERVICE);

					}

				//	mExtAdapReg = ExternalAdapterRegistrationClass.getInstance(mContext);

				}
			}

		}

	}

	BroadcastReceiver showToastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub

			Toast.makeText(
					mContext,
					getResources().getString(
							R.string.prDeviceRegistrationMessage),
					Toast.LENGTH_LONG).show();

		};

	};


	@Override
	public void onRemoteCallComplete(Message msg, int code) {
		// TODO Auto-generated method stub

		Bundle bundle = msg.getData();
		boolean result = bundle.getBoolean(PersonaConstants.result);

		if (result) {

			saveUserNamePwd();
			getDeviceDetails();


			postOnregisteredTasks();
				

		} else {

			if (updatingPolicyProgress != null
					&& updatingPolicyProgress.isShowing()) {

				updatingPolicyProgress.cancel();
				updatingPolicyProgress = null;

			}
			int mErrorcode = bundle.getInt(PersonaConstants.appstoreErrorCode);
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
			Toast.makeText(mContext,
					"Server Unavailable, Please try later",
					Toast.LENGTH_SHORT).show();
			break;
			//application not installed
		case 1127:
			saveUserNamePwd();
			getDeviceDetails();

			postOnregisteredTasks();
			/*Toast.makeText(mContext,
					"Server Unavailable, Please try later",
					Toast.LENGTH_SHORT).show();*/
			break;
			//Username/deviceid matches for the next authentication ("Request submitted successfully")
		case 1148:
			saveUserNamePwd();
			getDeviceDetails();

			postOnregisteredTasks();
			break;
			
			
		case 1149:
			
			showCertificateWarningScreen(certificateDowloadFailureErrorcode);
			break;
			//added for certificate not present error
		case 1166:
		
			showCertificateWarningScreen(certificateDowloadFailureErrorcode);
			break;
			
			//added for CRS rejection
		case 1244:
			
			new SharedPreferences(mContext).edit().putBoolean("crs_rejected", true).commit();
			showCertificateWarningScreen(certificateDowloadFailureErrorcode);
			break;
			
		default:
			Toast.makeText(mContext,
					"Server Unavailable, Please try later",
					Toast.LENGTH_SHORT).show();
			break;
		}

		}
	}

	private void showCertificateWarningScreen(int code) {
		Intent intent = new Intent(this,
				PersonaUserCertificateWarningActivity.class);
		
		PersonaLog.e(TAG,""+ code);
		intent.putExtra("warningMessageCode", code);
		mContext.startActivity(intent);
		finish();
	}

	
	private void postOnregisteredTasks() {
		try {
			if (updatingPolicyProgress != null
					&& updatingPolicyProgress.isShowing()) {

				updatingPolicyProgress.cancel();
				updatingPolicyProgress = null;

			}
			unregisterReceiver(UpdateUIReceiver);
			PersonaLocalAuthentication mLocalAuth = new PersonaLocalAuthentication();
			mLocalAuth.bindLoginActivityContext(mContext, mActivity,
					getBaseContext());
			mLocalAuth.createView(0);
		} catch (Exception e) {

		}

	}

	private void saveUserNamePwd() {

		if (PersonaMainActivity.isRovaPoliciesOn) {
			ExternalEmailSettingsInfo extnEmailSettInfo;
			extnEmailSettInfo = mExtAdapReg.getExternalEmailSettingsInfo();
			String usrnme, password, emailAddress;
			usrnme = extnEmailSettInfo.EmpId;
			password = extnEmailSettInfo.Password;
			emailAddress = extnEmailSettInfo.EmailAddress;
			if(usrnme.equals(emailAddress)) {
				//in some error scenarios,  username will be same as emailaddress. Sync will not happen with exchange email address
			usrnme = mUserName;
			}
			if (password != null && password.equals(""))
				password = mPassword;

			new SharedPreferences(mContext).edit()
					.putString("trumobi_username", usrnme).commit();
			new SharedPreferences(mContext).edit()
					.putString("trumobi_password", password).commit();
			
		} else {
			new SharedPreferences(mContext).edit()
					.putString("trumobi_username", mUserName).commit();
			new SharedPreferences(mContext).edit()
					.putString("trumobi_password", mPassword).commit();
			
		}
		PersonaLog.d(
				TAG,
				"username saved in rova off: "
						+ new SharedPreferences(mContext).getString(
								"trumobi_username", ""));
		PersonaLog.d(
				TAG,
				"password saved n rova off : "
						+ new SharedPreferences(mContext).getString(
								"trumobi_password", ""));

	}

	private void getDeviceDetails() {
		// TODO Auto-generated method stub
		PersonaLog.d(TAG, "=========== In getDeviceDetails =====");
		myDeviceDetails = new HashMap<String, String>();
		myDeviceDetails.put("Username", mUserName);
		myDeviceDetails.put("Password", mPassword);
		PersonaLog.d(TAG, "============ username :" + mUserName);
		PersonaLog.d(TAG, "============ Password :" + mPassword);
	}

	@Override
	public void onRemoteCallComplete(String json, int code) {
		// TODO Auto-generated method stub

	}



	/*
	 * @Override protected void onPause() { super.onPause(); if
	 * (updatingPolicyProgress != null && updatingPolicyProgress.isShowing()) {
	 * 
	 * updatingPolicyProgress.cancel(); updatingPolicyProgress = null;
	 * 
	 * }
	 * 
	 * };
	 */

	BroadcastReceiver UpdateUIReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (updatingPolicyProgress != null
					&& updatingPolicyProgress.isShowing()) {

				updatingPolicyProgress.cancel();
				updatingPolicyProgress = null;

			}
			boolean isSafeMethodFailed = intent.getBooleanExtra(
					"safe_method_failed", false);

			int errorCode = intent.getIntExtra("error_code", -1);
			if (isSafeMethodFailed) {
				switch (errorCode) {
				case 1001:
					PersonaLog.d("PersonaLoginActivity",
							"in updateui onreceive case 1001");
					Toast.makeText(mContext,
							"Authentication failed. Please try again",
							Toast.LENGTH_SHORT).show();
					break;
				default:

					Toast.makeText(mContext, "Unable to get Settings.",
							Toast.LENGTH_SHORT).show();
					finish();
					break;
				}

			}

			else {
				try {
					unregisterReceiver(UpdateUIReceiver);
				} catch (Exception e) {

				}
				PersonaLog
						.d("PersonaLoginActivity",
								"calling local pin screen after initiateExternalToFetchAllSettingsInfo success");
				PersonaLocalAuthentication mLocalAuth = new PersonaLocalAuthentication();
				mLocalAuth.bindLoginActivityContext(context, mActivity,
						getBaseContext());
				mLocalAuth.createView(0);

			}
		}

	};

	@Override
	public void onDialogCancelled() {
		// TODO Auto-generated method stub
		finish();
	}


	@Override
	public void onShowNextScreen(int showNextScreen) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onRequestCertificate() {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this,PersonaInstallCertificateActivity.class);
		startActivity(intent);
		finish();
	}

}
