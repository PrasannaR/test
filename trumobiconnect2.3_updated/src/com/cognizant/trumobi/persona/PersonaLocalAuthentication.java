package com.cognizant.trumobi.persona;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.TruBoxSDK.SharedPreferences;
import com.TruBoxSDK.TruBoxDatabase;
import com.TruBoxSDK.TruboxException;
import com.cognizant.trumobi.PersonaPimSettingsDbHelper;
import com.cognizant.trumobi.PersonaEmail_Content_Provider;
import com.cognizant.trumobi.PersonaLauncher;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobBaseEditText;
import com.cognizant.trumobi.commonabstractclass.TruMobiTimerClass;
import com.cognizant.trumobi.em.EmExchangeUtils;
import com.cognizant.trumobi.em.activity.EmMessageList;
import com.cognizant.trumobi.em.activity.setup.EmAccountSecurity;
import com.cognizant.trumobi.em.activity.setup.EmAccountSetupExchange;
import com.cognizant.trumobi.em.provider.EmEmailContent;
import com.cognizant.trumobi.em.service.EmEmailBroadcastProcessorService;
import com.cognizant.trumobi.em.service.EmMailService;

import com.cognizant.trumobi.externaladapter.ExternalAdapterRegistrationClass;
import com.cognizant.trumobi.externaladapter.ExternalEmailSettingsInfo;
import com.cognizant.trumobi.externaladapter.ExternalPIMSettingsInfo;
import com.cognizant.trumobi.log.PersonaLog;
import com.cognizant.trumobi.persona.constants.PersonaCertfifcateAuthConstants;
import com.cognizant.trumobi.persona.constants.PersonaConstants;
import com.cognizant.trumobi.persona.net.PersonaAllAppsListDetails;
import com.cognizant.trumobi.persona.net.PersonaCertificateDownloadResponse;
import com.cognizant.trumobi.persona.net.PersonaGetJsonInterface;
import com.cognizant.trumobi.persona.net.PersonaJSONClient;
import com.cognizant.trumobi.persona.net.PersonaLoginFailureResponse;
import com.cognizant.trumobi.persona.net.PersonaLoginResponse;
import com.cognizant.trumobi.persona.net.PersonaOneTimeLoginResponse;
import com.cognizant.trumobi.persona.settings.PersonaSettingsPinExpiry;
import com.cognizant.trumobi.persona.utils.Customkeyboard;
import com.cognizant.trumobi.persona.utils.PersonaCommonfunctions;
import com.cognizant.trumobi.securebrowser.SB_EncrptionDBTables;
import com.google.gson.Gson;

/**
 * 
 * KEYCODE AUTHOR MARK MARK CustomKeypad 290778 AlphanumericKeypad 290778
 * 2/12/13 Certificate 290778 28/11/13 Certificate/App 290778 12/10/13 Upgrade
 * 
 * 
 * KEYCODE AUTHOR PURPOSE PIN_SCREEN_ISSUE 367712 PIN Screen Issue on Email
 * Notification
 * 
 */
public class PersonaLocalAuthentication implements PersonaGetJsonInterface {

	TruMobBaseEditText mEnterPinEditText;
	TruMobBaseEditText mConfirmPinEditText;
	Button mSubmitButton;
	static Context mContext;
	static String mPin;
	String mConfirmPin;
	ArrayList<String> fileName;
	ActivityManager mActivityManager;
	Customkeyboard customKeyBoard, customKeyBoard1;
	TextView mSetYourPinTextView;
	TextView mForgotPinTextView;
	String mPackageName;

	InputMethodManager imm;
	ProgressDialog dialog;
	boolean calledAfterForGotPin, callDeviceProvisioning;
	PersonaCommonfunctions mCommonfunctions;
	LinkedList<PersonaAllAppsListDetails> listDetails = null;

	PersonaLocalAuthentication personaLocalAuthentication;
	PersonaJSONClient mDownloadcertificate, mSendAcknlowdge;

	LinearLayout subHeaderLayout;

	TextView downloadTextView, prSubCaptionTextView;
	TextView appNameTextView, minimumPINHintTextView, mconfirmPinTextView,
			mEnterPinTextView;
	String mUsername, mPassword, mDomain, mServer;
	String mIncorrectCredentials, mCredentialsNotmatching, mCredentialsSaved;
	String mValidationFailed;

	static int mPimAuthType;
	static int mExpiryInDays, mLocalAuthtype, mPwdLength;
	int mNumOfTrialsAllowed;
	boolean bPwdPin;
	long mPwdSetDate;
	boolean isLengthSatisfied, mIsAlphaCharsExist, mIsNumeralExist,
			mIsNonAlphaCharsExist, mIsNonNumericCharsExist;

	Intent intent;
	File mDB;
	TruBoxDatabase mPersonaDB;
	ExternalAdapterRegistrationClass mExtAdapReg;
	PersonaMainActivity mMainActivity;
	PersonaLoginActivity mLoginActivity;
	Typeface normalTf;
	private static final int FROM_PERSONAMAINACTIVITY = 1;
	private static final int FROM_PERSONALOGINACTIVITY = 2;
	int mCallingActivity;
	String at;
	private int inCorrectPinAttemptsAllowed, numOfAttemptsMade;
	static boolean emailNotification = false;
	// boolean calendarNotification = false;
	private boolean isPinSaved;
	ProgressDialog confirmingPinProgress;
	private static Bundle mBndle;

	private static String TAG = PersonaLocalAuthentication.class.getName();

	Button mDoneButton, mDoneButton2;
	Button mDoneButtonAlpha, mDoneButtonAlpha2;
	boolean isEmailAccountConfigured;
	boolean bManualEmailConfig;
	boolean bUserEmailEditable;
	int attemptsLeft;
	boolean flag = false;
	final int CERTIFICATE_DOWNLOAD = 1010, ROUTINE_LOGIN_CHECK = 1011,
			SEND_ACK = 1100;
	int mPwdLocktimeInMins, mChangeHistory;
	int certificateDowloadFailureErrorcode = 2001,
			serverUnreachabelErrorCode = 2005;
	PersonaModelLocalAuthentication personaLocalAuthMdl; // PERSONA_CODE_CLEANUP
	private int SAVE_PIN = 1;

	// PIM_TIMER_CHG - Added Bundle data to support data share from Activity to
	// normal class
	public void bindMainActivityContext(Context context,
			PersonaMainActivity activity, Context baseContext,
			boolean newEmailNotify, Bundle data) {
		mContext = context;
		personaLocalAuthentication = this;
		mMainActivity = activity;
		mCallingActivity = FROM_PERSONAMAINACTIVITY;
		emailNotification = newEmailNotify;
		PersonaLog.d("PersonaMainActivity", "---------- bindmainactivity -----"
				+ emailNotification);
		TruMobiTimerClass.userInteractedStopTimer();
		TruBoxDatabase.initialiseTruBoxDatabase(baseContext);
		mBndle = data; // PIM_TIMER_CHG
		personaLocalAuthMdl = new PersonaModelLocalAuthentication(context);
	}

	public void bindLoginActivityContext(Context context,
			PersonaLoginActivity activity, Context baseContext) {
		mContext = context;
		mLoginActivity = activity;
		personaLocalAuthentication = this;
		mCallingActivity = FROM_PERSONALOGINACTIVITY;
		TruBoxDatabase.initialiseTruBoxDatabase(baseContext);
		personaLocalAuthMdl = new PersonaModelLocalAuthentication(context);
	}

	public void createView(int authType, int numberOfAttempts, int expiryInDays) {

		switch (mCallingActivity) {
		case FROM_PERSONAMAINACTIVITY:
			isPinSaved = true;
			mLocalAuthtype = authType;
			inCorrectPinAttemptsAllowed = numberOfAttempts;
			mExpiryInDays = expiryInDays;
			createViewForAuthentication();

			PersonaLog.d(TAG, "------ createview --------- mLocalAuthtype "
					+ mLocalAuthtype + "-------inCorrectPinAttemptsAllowed  "
					+ inCorrectPinAttemptsAllowed
					+ "----------- mExpiryInDays " + mExpiryInDays);
			break;
		case FROM_PERSONALOGINACTIVITY:
			isPinSaved = false;
			createViewForSave();
			break;
		}

	}

	// CustomKeypad
	// AlphanumericKeypad
	public void createViewForSave() {

		PersonaLog.e("PersonaLocalAuthentication", "createView");
		mLoginActivity.setContentView(R.layout.pr_set_your_pin);
		personaLocalAuthentication = this;
		mPackageName = mContext.getApplicationInfo().packageName;
		mSetYourPinTextView = (TextView) mLoginActivity
				.findViewById(R.id.setYourPinTextView);

		mEnterPinEditText = (TruMobBaseEditText) mLoginActivity
				.findViewById(R.id.enterPinEditText);
		mConfirmPinEditText = (TruMobBaseEditText) mLoginActivity
				.findViewById(R.id.confirmPinEditText);
		customKeyBoard = (Customkeyboard) mLoginActivity
				.findViewById(R.id.key_edit);
		customKeyBoard1 = (Customkeyboard) mLoginActivity
				.findViewById(R.id.key_edit1);
		// mConfirmPinEditText.setOnEditorActionListener(confirmPinListener);
		// mSubmitButton = (Button)
		// mLoginActivity.findViewById(R.id.submitButton);
		// mSubmitButton.setOnClickListener(submitOnclickListener);
		mDoneButton = (Button) customKeyBoard.findViewById(R.id.button_right);
		mDoneButton.setOnClickListener(submitOnclickListener);

		mDoneButton2 = (Button) customKeyBoard1.findViewById(R.id.button_right);
		mDoneButton2.setOnClickListener(submitOnclickListener);

		mDoneButtonAlpha = (Button) customKeyBoard.findViewById(R.id.button47);
		mDoneButtonAlpha.setOnClickListener(submitOnclickListener);

		mDoneButtonAlpha2 = (Button) customKeyBoard1
				.findViewById(R.id.button47);
		mDoneButtonAlpha2.setOnClickListener(submitOnclickListener);

		mActivityManager = (ActivityManager) mLoginActivity
				.getSystemService(Context.ACTIVITY_SERVICE);
		mCommonfunctions = new PersonaCommonfunctions(mContext);
		subHeaderLayout = (LinearLayout) mLoginActivity
				.findViewById(R.id.prSubHeader);
		prSubCaptionTextView = (TextView) mLoginActivity
				.findViewById(R.id.prSubCaption);
		minimumPINHintTextView = (TextView) mLoginActivity
				.findViewById(R.id.minimumLengthLabel);
		mEnterPinTextView = (TextView) mLoginActivity
				.findViewById(R.id.enter_credentials_label);
		mconfirmPinTextView = (TextView) mLoginActivity
				.findViewById(R.id.confirm_credentials_label);
		// mContext.registerReceiver(LoginCompleteReceiver, new
		// IntentFilter("LoginActionComplete"));

		normalTf = Typeface.createFromAsset(mLoginActivity.getAssets(),
				"fonts/Roboto-Regular.ttf");
		postCreateView();
		getSettingsToSaveLocalCredentials();

	}

	private void getSettingsToSaveLocalCredentials() {

		if (PersonaMainActivity.isRovaPoliciesOn) {
			ExternalPIMSettingsInfo pimSettingsInfo;
			pimSettingsInfo = mExtAdapReg.getExternalPIMSettingsInfo();
			mLocalAuthtype = pimSettingsInfo.Passwordtype.getValue();
			mNumOfTrialsAllowed = pimSettingsInfo.nMaxPasswordTries;
			bPwdPin = pimSettingsInfo.bPasswordPIN;
			mExpiryInDays = pimSettingsInfo.PasswordExpires.getValue();
			mPwdLength = pimSettingsInfo.nPasswordLength;
			PersonaLog.d(TAG,
					"------ values while login first time mLocalAuthtype ---------"
							+ mLocalAuthtype + "-------" + bPwdPin
							+ "-----------" + mExpiryInDays + "mPwdLength"
							+ mPwdLength);
			validateValuesFromSDK();
		} else {
			mLocalAuthtype = PersonaConstants.AUTH_TYPE_NUMERIC;
			mNumOfTrialsAllowed = PersonaConstants.NUMBER_OF_TRIALS_ALLOWED;
			bPwdPin = true;
			mExpiryInDays = PersonaConstants.NUMBER_OF_DAYS_TO_EXPIRE;
			mPwdLength = PersonaConstants.LOCAL_CREDENTIALS_LENGTH;
		}
		mCommonfunctions.saveCredentialsDetailsForExpiryScreen(mLocalAuthtype,
				mPwdLength);
		at = mCommonfunctions.setAuthenticationtype(mLocalAuthtype);
		updateUiToSaveLocalCredentials();
		PersonaLog.d(TAG,
				"------ values after hardcoded mLocalAuthtype ---------"
						+ mLocalAuthtype + "-------" + bPwdPin + "-----------"
						+ mExpiryInDays);

	}

	private void updateUiToSaveLocalCredentials() {

		mEnterPinEditText.setTypeface(normalTf, Typeface.NORMAL);
		mConfirmPinEditText.setTypeface(normalTf, Typeface.NORMAL);
		customKeyBoard.setVisibility(View.VISIBLE);
		customKeyBoard1.setVisibility(View.GONE);
		customKeyBoard.setEdit(mEnterPinEditText);
		switch (mLocalAuthtype) {
		case PersonaConstants.AUTH_TYPE_NUMERIC:
			mSetYourPinTextView.setText(mContext
					.getString(R.string.set_your_pin_label));
			minimumPINHintTextView.setText(mContext.getString(
					R.string.pr_set_pin_hint_label, mPwdLength));
			prSubCaptionTextView.setText(mContext
					.getString(R.string.rememberPin));
			mconfirmPinTextView.setText(mContext
					.getString(R.string.confirm_persona_pin));
			getStringsForPIN();
			setUiForPIN();
			break;
		case PersonaConstants.AUTH_TYPE_ALPHABETS:
			minimumPINHintTextView.setText(mContext.getString(
					R.string.pr_set_password_hint_label, mPwdLength));
			updateUiToSavePassword();
			break;
		case PersonaConstants.AUTH_TYPE_ALPHANUMERIC:
			minimumPINHintTextView.setText(mContext.getString(
					R.string.pr_set_password_alphanumeric_hint_label,
					mPwdLength));
			updateUiToSavePassword();
			break;
		case PersonaConstants.AUTH_TYPE_PASSWORD_ALLOW_NUMERALS:
			minimumPINHintTextView.setText(mContext.getString(
					R.string.pr_set_password_allow_numerals_hint_label,
					mPwdLength));
			updateUiToSavePassword();
			break;
		}

	}

	private void updateUiToSavePassword() {
		mSetYourPinTextView.setText(mContext
				.getString(R.string.set_your_password_label));
		prSubCaptionTextView.setText(mContext
				.getString(R.string.rememberPassword));
		mconfirmPinTextView.setText(mContext
				.getString(R.string.confirm_persona_password));
		getStringsForPassword();
		setUiForPassword();
	}

	private void getStringsForPIN() {
		mIncorrectCredentials = mContext.getString(R.string.incorrect_pin);
		mCredentialsNotmatching = mContext.getString(R.string.pin_not_matching);
		mCredentialsSaved = mContext.getString(R.string.prPINSavedMessage);
		mValidationFailed = mContext.getString(R.string.prValidationFailed);
	}

	private void getStringsForPassword() {
		mIncorrectCredentials = mContext.getString(R.string.incorrect_password);
		mCredentialsNotmatching = mContext
				.getString(R.string.password_not_matching);
		mCredentialsSaved = mContext.getString(R.string.prPasswordSavedMessage);
		mValidationFailed = mContext.getString(R.string.prValidationFailed);
	}

	// CustomKeypad
	// AlphanumericKeypad
	public void createViewForAuthentication() {

		PersonaLog.e("PersonaLocalAuthentication", "createView");
		mMainActivity.setContentView(R.layout.pr_set_your_pin);

		mPackageName = mContext.getApplicationInfo().packageName;
		mSetYourPinTextView = (TextView) mMainActivity
				.findViewById(R.id.setYourPinTextView);
		customKeyBoard = (Customkeyboard) mMainActivity
				.findViewById(R.id.key_edit);
		customKeyBoard1 = (Customkeyboard) mMainActivity
				.findViewById(R.id.key_edit1);
		mEnterPinEditText = (TruMobBaseEditText) mMainActivity
				.findViewById(R.id.enterPinEditText);
		mConfirmPinEditText = (TruMobBaseEditText) mMainActivity
				.findViewById(R.id.confirmPinEditText);
		mEnterPinTextView = (TextView) mMainActivity
				.findViewById(R.id.enter_credentials_label);
		mconfirmPinTextView = (TextView) mMainActivity
				.findViewById(R.id.confirm_credentials_label);

		// mEnterPinEditText.setOnEditorActionListener(enterPinListener);
		// mConfirmPinEditText.setOnEditorActionListener(confirmPinListener);

		// mEnterPinEditText.setOnEditorActionListener(enterPinListener);
		// mConfirmPinEditText.setOnEditorActionListener(confirmPinListener);

		mEnterPinEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
		// mSubmitButton = (Button)
		// mMainActivity.findViewById(R.id.submitButton);
		// mSubmitButton.setOnClickListener(submitOnclickListener);

		mActivityManager = (ActivityManager) mMainActivity
				.getSystemService(Context.ACTIVITY_SERVICE);
		mCommonfunctions = new PersonaCommonfunctions(mContext);
		subHeaderLayout = (LinearLayout) mMainActivity
				.findViewById(R.id.prSubHeader);
		prSubCaptionTextView = (TextView) mMainActivity
				.findViewById(R.id.prSubCaption);
		minimumPINHintTextView = (TextView) mMainActivity
				.findViewById(R.id.minimumLengthLabel);
		mDoneButton = (Button) customKeyBoard.findViewById(R.id.button_right);
		mDoneButton.setOnClickListener(submitOnclickListener);
		mDoneButtonAlpha = (Button) customKeyBoard.findViewById(R.id.button47);
		mDoneButtonAlpha.setOnClickListener(submitOnclickListener);

		/*
		 * LocalBroadcastManager.getInstance(mContext.getApplicationContext()).
		 * registerReceiver(LoginCompleteReceiver, new
		 * IntentFilter("LoginActionComplete"));
		 */
		mContext.registerReceiver(LoginCompleteReceiver, new IntentFilter(
				"LoginActionComplete"));

		PersonaLog.e("PersonaLocalAuthentication", "createView ends");
		normalTf = Typeface.createFromAsset(mMainActivity.getAssets(),
				"fonts/Roboto-Regular.ttf");
		PersonaLog
				.e("PersonaLocalAuthentication", "assets initialisation ends");
		postCreateView();
		updateUiForAuthentication();

	}

	@SuppressWarnings("deprecation")
	private void updateUiForAuthentication() {
		mDoneButton.setBackgroundDrawable(mContext.getResources().getDrawable(
				R.drawable.pr_key_done_selector));
		mDoneButtonAlpha.setBackgroundDrawable(mContext.getResources()
				.getDrawable(R.drawable.pr_key_done_selector));
		mconfirmPinTextView.setVisibility(View.GONE);
		mConfirmPinEditText.setVisibility(View.GONE);
		mSetYourPinTextView.setVisibility(View.GONE);
		prSubCaptionTextView.setVisibility(View.GONE);
		subHeaderLayout.setVisibility(View.GONE);
		minimumPINHintTextView.setVisibility(View.GONE);
		mEnterPinEditText.setTypeface(normalTf, Typeface.NORMAL);
		customKeyBoard.setVisibility(View.VISIBLE);
		customKeyBoard1.setVisibility(View.GONE);
		customKeyBoard.setEdit(mEnterPinEditText);
		switch (mLocalAuthtype) {
		case PersonaConstants.AUTH_TYPE_NUMERIC:
			getStringsForPIN();
			setUiForPIN();
			break;
		case PersonaConstants.AUTH_TYPE_ALPHABETS:

		case PersonaConstants.AUTH_TYPE_ALPHANUMERIC:

		case PersonaConstants.AUTH_TYPE_PASSWORD_ALLOW_NUMERALS:
			getStringsForPassword();
			setUiForPassword();
			break;
		}
	}

	private void setUiForPIN() {
		mEnterPinTextView.setText(mContext
				.getString(R.string.enter_persona_pin));
		customKeyBoard.setQwertyKeyBoard(false);
		customKeyBoard1.setQwertyKeyBoard(false);
	}

	private void setUiForPassword() {
		PersonaLog.d(TAG, "---------- setUiForPassword -------");
		mEnterPinTextView.setText(mContext
				.getString(R.string.enter_persona_password));
		customKeyBoard.setQwertyKeyBoard(true);
		customKeyBoard1.setQwertyKeyBoard(true);
	}

	private void postCreateView() {

		// mSubmitButton.setTypeface(normalTf, Typeface.NORMAL);
		if (PersonaMainActivity.isRovaPoliciesOn) {

			mExtAdapReg = ExternalAdapterRegistrationClass
					.getInstance(mContext);
		}

		mEnterPinEditText.setOnTouchListener(new View.OnTouchListener() {

			// CustomKeypad
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mEnterPinEditText.requestFocus();
				customKeyBoard.setVisibility(View.VISIBLE);
				customKeyBoard.setEdit(mEnterPinEditText);
				customKeyBoard1.setVisibility(View.GONE);

				return true;
			}
		});

		mConfirmPinEditText.setOnTouchListener(new View.OnTouchListener() {

			// CustomKeypad
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mConfirmPinEditText.requestFocus();
				customKeyBoard1.setVisibility(View.VISIBLE);
				customKeyBoard1.setEdit(mConfirmPinEditText);
				customKeyBoard.setVisibility(View.GONE);

				return true;
			}
		});

		mEnterPinEditText
				.setOnFocusChangeListener(new View.OnFocusChangeListener() {

					// CustomKeypad
					@Override
					public void onFocusChange(View v, boolean hasFocus) {

						if (v != null) {

							// hideSoftKeyboard(mLoginActivity);
							((InputMethodManager) mContext
									.getSystemService(Context.INPUT_METHOD_SERVICE))
									.hideSoftInputFromWindow(
											mEnterPinEditText.getWindowToken(),
											0);
							if (isPinSaved) {

								/*
								 * ((InputMethodManager) mMainActivity
								 * .getSystemService
								 * (Activity.INPUT_METHOD_SERVICE))
								 * .hideSoftInputFromWindow( mEnterPinEditText
								 * .getWindowToken(), 0);
								 */
								// mDoneButton.setText(mContext.getResources().getString(R.string.done_action));

							}

						}

						if (hasFocus) {
							customKeyBoard.setVisibility(View.VISIBLE);
							customKeyBoard1.setVisibility(View.GONE);
							customKeyBoard.setEdit(mEnterPinEditText);
							// mDoneButton.setText(mContext.getResources().getString(R.string.next_action));
							mEnterPinEditText.setTextColor(mContext
									.getResources().getColor(
											R.color.PR_TEXTBOX_NORMAL_COLOR));

						} else {

							// customKeyBoard.setEdit(mConfirmPinEditText);
							PersonaLog.d("Enter Pin has no focus", ""
									+ hasFocus);
							// mDoneButton.setText(mContext.getResources().getString(R.string.next_action));
							mEnterPinEditText.setTextColor(mContext
									.getResources().getColor(
											R.color.PR_TEXTBOX_FOCUSED_COLOR));
						}
					}
				});

		mConfirmPinEditText
				.setOnFocusChangeListener(new View.OnFocusChangeListener() {

					// CustomKeypad
					@SuppressWarnings("deprecation")
					@Override
					public void onFocusChange(View v, boolean hasFocus) {

						if (v != null) {

							// mDoneButton.setText(mContext.getResources().getString(R.string.done_action));
							((InputMethodManager) mContext
									.getSystemService(Context.INPUT_METHOD_SERVICE))
									.hideSoftInputFromWindow(
											mConfirmPinEditText
													.getWindowToken(), 0);

							if (hasFocus) {

								customKeyBoard.setVisibility(View.GONE);
								customKeyBoard1.setVisibility(View.VISIBLE);
								customKeyBoard1.setEdit(mConfirmPinEditText);
								PersonaLog.d("Confirm Pin has  focus", ""
										+ hasFocus);

								mConfirmPinEditText
										.setTextColor(mContext
												.getResources()
												.getColor(
														R.color.PR_TEXTBOX_NORMAL_COLOR));

								mDoneButton2
										.setBackgroundDrawable(mContext
												.getResources()
												.getDrawable(
														R.drawable.pr_key_done_selector));

								mDoneButtonAlpha2
										.setBackgroundDrawable(mContext
												.getResources()
												.getDrawable(
														R.drawable.pr_key_done_selector));
							} else {
								PersonaLog.d("Confirm Pin has  focus", ""
										+ hasFocus);
								mConfirmPinEditText
										.setTextColor(mContext
												.getResources()
												.getColor(
														R.color.PR_TEXTBOX_FOCUSED_COLOR));
							}
						}
					}
				});

	}

	private void getEmailSettings() {

		String emailAddress;
		if (PersonaMainActivity.isRovaPoliciesOn) {

			ExternalEmailSettingsInfo extnEmailSettInfo;
			mExtAdapReg = ExternalAdapterRegistrationClass
					.getInstance(mContext);
			extnEmailSettInfo = mExtAdapReg.getExternalEmailSettingsInfo();
			emailAddress = extnEmailSettInfo.EmailAddress;
			mUsername = extnEmailSettInfo.EmpId;
			mPassword = extnEmailSettInfo.Password;
			mDomain = extnEmailSettInfo.Domain;
			mServer = extnEmailSettInfo.Server;
			bUserEmailEditable = extnEmailSettInfo.bAllowEditEmailSettings;
			bManualEmailConfig = extnEmailSettInfo.bSetupAutomatic;

			// username from sdk is error handled and saved in preferences
			// already. Using it.
			// To fix the issue.. when we get username and emailaddress as same,
			// we'll save username
			// entered by user. Clashes come while reading values from db
			mUsername = new SharedPreferences(mContext).getString(
					"trumobi_username", "");

			// Since password field from ROVA SDK is null,we get it from
			// preferences. Username from sdk is exchange email id. So getting
			// from local preferences
			mPassword = new SharedPreferences(mContext).getString(
					"trumobi_password", "");

			// Manual setup turned on
			bManualEmailConfig = true;
			mPimAuthType = mExtAdapReg.getExternalEmailSettingsInfo().Authtype
					.getValue();
			if (mPimAuthType == PersonaConstants.mAuthTypeNone
					|| mPimAuthType == PersonaConstants.mAuthTypeCertificate) {
				mPimAuthType = PersonaConstants.mAuthTypeBasic;
			}

		} else {
			mUsername = new SharedPreferences(mContext).getString(
					"trumobi_username", "");
			emailAddress = mUsername;
			mPassword = new SharedPreferences(mContext).getString(
					"trumobi_password", "");
			mDomain = PersonaConstants.mExchangeDomain;
			mServer = PersonaConstants.mExchangeServer;
			bManualEmailConfig = true;
			if (PersonaMainActivity.isCertificateBasedAuth)
				mPimAuthType = PersonaConstants.mAuthTypeCertificate;
			else
				mPimAuthType = PersonaConstants.mAuthTypeBasic;
		}

		PersonaLog.d(TAG, "mUsername: " + mUsername + "mPassword: " + mPassword
				+ "mEmailId: " + emailAddress + "exchange email id "
				+ emailAddress);
		PersonaLog.d(TAG, "mDomain:   " + mDomain + "mServer: " + mServer);
	}

	private void validateValuesFromSDK() {

		if (mNumOfTrialsAllowed <= 0)
			mNumOfTrialsAllowed = 5;
		if (!(mLocalAuthtype == PersonaConstants.AUTH_TYPE_NUMERIC)
				&& bPwdPin == PersonaConstants.PIN_AUTH) {
			mLocalAuthtype = PersonaConstants.AUTH_TYPE_PASSWORD_ALLOW_NUMERALS;
		}

		// Hardcoded values since values from sdk is not updated
		/*
		 * mLocalAuthtype = PersonaConstants.AUTH_TYPE_NUMERIC;
		 * mNumOfTrialsAllowed = 5; mExpiryInDays = 10; bPwdPin = true;
		 * mPwdLength = 4;
		 */
	}

	private void checkLoginToday() {

		/*
		 * if(PersonaMainActivity.isCertificateBasedAuth){
		 * 
		 * boolean isCertAcKSent =new
		 * SharedPreferences(mContext).getBoolean("ack_sent", false);
		 * 
		 * if(!isCertAcKSent) sendCertificateAcknowledgement(); }
		 */

		if (mCommonfunctions.isConnectedToNetwork()) {
			final int millisInDay = 24 * 60 * 60 * 1000;
			long lastEmaasLoginTime = new SharedPreferences(mContext).getLong(
					"LastEmaasLogin", 0);
			// long currentTime = System.currentTimeMillis();

			long difference = getTodayMidnightTime() - lastEmaasLoginTime;
			PersonaLog.d(TAG, "********** lastlogin *********"
					+ lastEmaasLoginTime);

			if (Long.signum(difference) == -1)
				difference = -difference;
			PersonaLog.d(TAG, "********** difference " + difference);

			if (difference >= millisInDay
					|| PersonaMainActivity.pullRovaSettingsOn) {
				PersonaLog
						.d(TAG,
								"********** exceeded since last emaas login or pullrovasettings enabled *********");

				if (PersonaMainActivity.isRovaPoliciesOn)
					mExtAdapReg.startLoginExternalAdapter();
				else {

					if (PersonaMainActivity.isCertificateBasedAuth) {
						routineLogincheck();
					} else {
						confirmingPinProgress.dismiss();
						checkUpdateInPimSettings(mContext, 0);
						// showAppsList();
					}

				}
			}

			else {
				PersonaLog
						.d(TAG,
								"********** didnt exceeded since last emaas login skipping emaaslogin *********");
				confirmingPinProgress.dismiss();
				checkUpdateInPimSettings(mContext, 0);
			}
		} else {
			PersonaLog
					.d(TAG,
							"********** not connected to network  skipping emaaslogin *********");

			confirmingPinProgress.dismiss();
			checkUpdateInPimSettings(mContext, 0);
		}

	}

	private void checkUpdateInPimSettings(Context ctx, int calledfrom) {

		if (calledfrom == SAVE_PIN) {
			if (confirmingPinProgress != null
					&& confirmingPinProgress.isShowing())

				confirmingPinProgress.dismiss();
			showAppsList();
		} else {
			PersonaLog.d(TAG, "------ checkUpdateInPimSettings --------- ");
			PersonaLog.d(TAG, "-------- mLocalAuthtype -------"
					+ mLocalAuthtype);
			PersonaLog.d(TAG, "-------- mNumOfTrialsAllowed -------"
					+ mNumOfTrialsAllowed);
			PersonaLog.d(TAG, "-------- mExpiryInDays -------" + mExpiryInDays);
			PersonaLog.d(TAG, "-------- mPwdLength -------" + mPwdLength);
			PersonaLog.d(TAG, "-------- mPwdLocktimeInMins -------"
					+ mPwdLocktimeInMins);
			PersonaLog.d(TAG, "-------- mChangeHistory -------"
					+ mChangeHistory);
			Bundle bundle = new Bundle();
			bundle.putInt("mLocalAuthtype", mLocalAuthtype);
			bundle.putInt("mNumOfTrialsAllowed", mNumOfTrialsAllowed);
			bundle.putInt("mExpiryInDays", mExpiryInDays);
			bundle.putInt("mPwdLength", mPwdLength);
			bundle.putInt("mPwdLocktimeInMins", mPwdLocktimeInMins);
			bundle.putInt("mChangeHistory", mChangeHistory);
			CheckForUpdateInPimSettings checkUpdate = new CheckForUpdateInPimSettings();
			checkUpdate.execute(bundle);
		}

	}

	public Handler showLauncherCallback = new Handler() {

		public void handleMessage(android.os.Message msg) {
			PersonaLog.d(TAG, "------------ handlemessage called --------");
			switch (msg.what) {
			case 1:
				PersonaLog.d(TAG,
						"------------ handlemessage called case 1 --------");
				getEmailSettings();
				showAppsList();
				break;

			}

		};
	};

	private boolean isPinExpired(Context ctx) {
		int days = new SharedPreferences(ctx).getInt("daysLeft",
				PersonaConstants.NUMBER_OF_DAYS_TO_EXPIRE);
		if (days <= 0) {
			return true;
		}
		return false;
	}

	private long getTodayMidnightTime() {

		Calendar mCalendar = Calendar.getInstance();
		mCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
		mCalendar.set(mCalendar.get(Calendar.YEAR),
				mCalendar.get(Calendar.MONTH),
				mCalendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
		Date mTodayDateMidnight = mCalendar.getTime();
		return mTodayDateMidnight.getTime();
	}

	public void callLoginServiceOnce() {

		Calendar c = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		String currentDate = dateFormat.format(c.getTime());

		String lastupdatedDateInPreference = new SharedPreferences(mContext)
				.getString("current_Date", "empty");

		if (lastupdatedDateInPreference.equals("empty")) {

			new SharedPreferences(mContext).edit().putString("current_Date",
					currentDate);

		} else {

			SimpleDateFormat curFormater = new SimpleDateFormat("dd/MM/yyyy");

			try {
				Date dateObj = curFormater.parse(lastupdatedDateInPreference);
				if (!c.getTime().equals(dateObj))
					PersonaLog.d(TAG, "**********dateObj*********" + dateObj);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		PersonaLog.d(TAG, "**********currentDate*********" + currentDate);

	}

	/*
	 * private void saveUserDetails() {
	 * 
	 * 
	 * TruBoxDatabase.initialiseTruBoxDatabase(mContext); String DB_NAME_HASHED
	 * = TruBoxDatabase.getHashValue( PersonaDbEncryptHelper.DB_NAME, mContext);
	 * mDB = mContext.getDatabasePath(DB_NAME_HASHED);
	 * PersonaLog.d("personalocalauthentication",
	 * "----------- databasepath in saveuserdetails -------" + mDB); mPersonaDB
	 * = TruBoxDatabase.openOrCreateDatabase(mDB, null); String tabName =
	 * PersonaDbEncryptHelper.TABLE_USERDETAILS; try {
	 * mPersonaDB.execSQL(PersonaDbEncryptHelper.CREATE_TABLE_USERDETAILS);
	 * String query = "SELECT * FROM " + tabName + " WHERE " +
	 * PersonaDbEncryptHelper.USER_NAME + "= '" + mUsername + "'"; Cursor c =
	 * mPersonaDB.rawQuery(query, null);
	 * 
	 * if (c.getCount() == 0) { String[] args = { mUsername};
	 * mPersonaDB.execSQL("INSERT OR REPLACE INTO " + tabName + " (" +
	 * PersonaDbEncryptHelper.USER_NAME + ")" + " VALUES (?)", args);
	 * PersonaLog.d("PersonaLoginActivity", "inserting user details"); }
	 * 
	 * else { String[] args = { mPassword }; mPersonaDB.execSQL("UPDATE " +
	 * tabName + " SET " + PersonaDbEncryptHelper.PASSWORD + "= ? WHERE " +
	 * PersonaDbEncryptHelper.USER_NAME + "= '" + mUsername + "'", args);
	 * PersonaLog.d("PersonaLoginActivity", "updating user details"); }
	 * c.close(); c = mPersonaDB.rawQuery("SELECT * FROM " + tabName, null); if
	 * (c.moveToFirst()) { while (!c.isAfterLast()) { PersonaLog.d(
	 * "PersonaLoginActivity", "After inserting values in USERDETAILS table  " +
	 * c.getString(0)); c.moveToNext(); } } else {
	 * PersonaLog.d("PersonaLoginActivity",
	 * "moveToFirst failed in userdetails table"); }
	 * 
	 * c.close(); } catch (Exception e) {
	 * 
	 * }
	 * 
	 * }
	 */

	public class SavePIMSettings extends AsyncTask<String, Integer, Integer> {

		ProgressDialog confirmingPinProgress;

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			PersonaLog.d(TAG, "Completed Saving PIM Settings");
			if (result != null) {

				PersonaLog.d(TAG, "Dismissing Progress Bar");

				Toast.makeText(mContext, mCredentialsSaved, Toast.LENGTH_LONG)
						.show();

				new SharedPreferences(mContext).edit()
						.putBoolean("isPersonaKeySaved", true).commit();
				if (confirmingPinProgress != null
						&& confirmingPinProgress.isShowing()
						&& !mLoginActivity.isFinishing())
					confirmingPinProgress.dismiss();

				checkUpdateInPimSettings(mContext, 1);
			}

		}

		@Override
		protected void onPreExecute() {

			super.onPreExecute();

			if (confirmingPinProgress == null)
				confirmingPinProgress = new ProgressDialog(mContext,
						R.style.PersonaNewDialog);
			confirmingPinProgress.setCancelable(false);
			confirmingPinProgress.setMessage(mContext.getResources().getString(
					R.string.prSaveCredentials));
			if (!mLoginActivity.isFinishing())
				confirmingPinProgress.show();
		}

		@Override
		protected Integer doInBackground(String... arg0) {

			try {
				getEmailSettings();
				// PERSONA_CODE_CLEANUP
				Bundle usrBndle = new Bundle();
				usrBndle.putString("username", mUsername);
				personaLocalAuthMdl.saveUserDetails(usrBndle);

				if (PersonaMainActivity.isRovaPoliciesOn) {
					mPwdLocktimeInMins = mExtAdapReg
							.getExternalPIMSettingsInfo().PasswordFailLockout
							.getValue();
					mChangeHistory = mExtAdapReg.getExternalPIMSettingsInfo().nPasswordHistory;
				} else {
					mPwdLocktimeInMins = PersonaConstants.LOG_OUT_PERIOD;
					mChangeHistory = 0;
				}

				PersonaLog.d("PersonaLoginActivity", "mLocalAuthenticationType"
						+ mLocalAuthtype + "mPwdExpiryDurationInDays"
						+ mExpiryInDays + "mPwdLocktimeInMins"
						+ mPwdLocktimeInMins + "mPwdLength" + mPwdLength
						+ "mMaxNoPwdTrials" + mNumOfTrialsAllowed
						+ "mChangeHistory" + mChangeHistory + "musername"
						+ mUsername);

				Bundle pimSettingsBndle = new Bundle();
				pimSettingsBndle.putString("username", mUsername);
				pimSettingsBndle.putInt("localauthtype", mLocalAuthtype);
				pimSettingsBndle.putInt("expiryindays", mExpiryInDays);
				pimSettingsBndle
						.putInt("pwdlocktimeinmins", mPwdLocktimeInMins);
				pimSettingsBndle.putInt("pwdlength", mPwdLength);
				pimSettingsBndle.putInt("numoftrialsallowed",
						mNumOfTrialsAllowed);
				pimSettingsBndle.putInt("changehistory", mChangeHistory);

				personaLocalAuthMdl.savePIMsettings(pimSettingsBndle);

				if (mPersonaDB != null && mPersonaDB.isOpen()) {
					mPersonaDB.close();
					mPersonaDB = null;
				}

				Bundle routineCheckbndle = new Bundle();
				routineCheckbndle.putInt("numoftrialsallowed",
						mNumOfTrialsAllowed);
				routineCheckbndle.putString("authorizationtype", at);
				routineCheckbndle.putInt("expiryInDays", mExpiryInDays);
				personaLocalAuthMdl.saveInRoutineCheckDB(routineCheckbndle);

				isEmailAccountConfigured = new SharedPreferences(mContext)
						.getBoolean("isEmailAccountConfigured", false);
				
			} catch (Exception e) {

				e.printStackTrace();
			}

			return 0;
		}

	}

	public class CheckForUpdateInPimSettings extends
			AsyncTask<Bundle, Integer, Bundle> {

		ProgressDialog checkForUpdate;

		@Override
		protected Bundle doInBackground(Bundle... bundle) {
			PersonaLog
					.d(TAG,
							"-------- CheckForUpdateInPimSettings doInBackground ---------- ");
			Bundle returnBundle = new Bundle();

			if (bundle[0].getInt("mExpiryInDays") > 0)
				shouldShowExpiryAlert(mContext,
						bundle[0].getInt("mExpiryInDays"));

			if (PersonaMainActivity.isRovaPoliciesOn) {
				bundle[0].putString("username", new SharedPreferences(mContext)
						.getString("trumobi_username", ""));
				returnBundle = personaLocalAuthMdl.updatePIMSettings(bundle[0]);
			} else {
				returnBundle.putBoolean("passwordResetRequired", false);
			}
			PersonaLog.d(TAG, "------- returnbundle --------- " + returnBundle);
			return returnBundle;
		}

		@Override
		protected void onPostExecute(Bundle bundle) {
			super.onPostExecute(bundle);
			if (confirmingPinProgress != null
					&& confirmingPinProgress.isShowing())

				confirmingPinProgress.dismiss();
			boolean issettingsUpdated;
			int updatedPwdType = 0, updatedPwdLength = 0;
			issettingsUpdated = bundle.getBoolean("passwordResetRequired");
			updatedPwdType = bundle.getInt("updatedPwdType", 0);
			updatedPwdLength = bundle.getInt("updatedPwdLength", 0);
			if (issettingsUpdated) {
				PersonaLog
						.d(TAG,
								"-------- CheckForUpdateInPimSettings onpostexecute ---------- ");
				Intent settingsChangedIntent = new Intent(mContext,
						PersonaSettingsPinExpiry.class);
				settingsChangedIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				settingsChangedIntent.putExtra("From", "PimSettingsUpdated");
				settingsChangedIntent.putExtra("credentials", mPin);
				settingsChangedIntent
						.putExtra("updatedPwdType", updatedPwdType);
				settingsChangedIntent.putExtra("updatedPwdLength",
						updatedPwdLength);
				mContext.startActivity(settingsChangedIntent);
				try {
					mContext.unregisterReceiver(LoginCompleteReceiver);
				} catch (Exception e) {

				}
				finishCurrentActivity();
			} else {
				checkPinExpired(mContext);
			}
		}

		@Override
		protected void onPreExecute() {
			/*
			 * checkForUpdate = new ProgressDialog(mContext,
			 * R.style.PersonaNewDialog); checkForUpdate.setCancelable(false);
			 * checkForUpdate
			 * .setMessage(mContext.getResources().getString(R.string
			 * .prValCredentials)); checkForUpdate.show(); super.onPreExecute();
			 */
		}

	}

	private void checkPinExpired(Context mContext) {

		if (isPinExpired(mContext)) {

			Intent pinExpiryIntent = new Intent(mContext,
					PersonaSettingsPinExpiry.class);
			pinExpiryIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			pinExpiryIntent.putExtra("From", "PINAlreadyExpired");
			pinExpiryIntent.putExtra("credentials", mPin);
			mContext.startActivity(pinExpiryIntent);
			new com.TruBoxSDK.SharedPreferences(mContext).edit()
					.putBoolean("showExpiry", false).commit();
			try {
				mContext.unregisterReceiver(LoginCompleteReceiver);
			} catch (Exception e) {

			}
			finishCurrentActivity();
		} else {
			getEmailSettings();
			showAppsList();
		}

	}

	View.OnClickListener submitOnclickListener = new View.OnClickListener() {

		@Override
		public void onClick(View view) {

			InputMethodManager imm = (InputMethodManager) mContext
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			/*
			 * imm.hideSoftInputFromWindow(getCurrentFocus() .getWindowToken(),
			 * 0);
			 */

			// if(mDoneButton.getText().equals(mContext.getResources().getString(R.string.next_action)))
			// mConfirmPinEditText.requestFocus();

			if (imm.isActive()) {
				imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
			}

			if (isPinSaved) {
				mPin = mEnterPinEditText.getText().toString();
				if (mPin.equals("")) {
					Toast.makeText(mContext, "Please enter credentials",
							Toast.LENGTH_SHORT).show();
				} else {
					authenticatePinAsyncTask authenticatePINTask = new authenticatePinAsyncTask();
					authenticatePINTask.execute();
				}

			} else {

				if (customKeyBoard.getVisibility() == 0) {
					mConfirmPinEditText.requestFocus();
				} else {
					validateTextFields();
				}
			}
		}

	};

	/*
	 * TextView.OnEditorActionListener enterPinListener = new
	 * TextView.OnEditorActionListener() {
	 * 
	 * @Override public boolean onEditorAction(TextView textView, int actionId,
	 * KeyEvent event) {
	 * 
	 * if (actionId == EditorInfo.IME_ACTION_DONE) {
	 * 
	 * if (isShowAuthenticatePinUi) {
	 * 
	 * mPin = mEnterPinEditText.getText().toString(); if (mPin.equals("")) {
	 * Toast.makeText(mContext, "Please enter credentials",
	 * Toast.LENGTH_SHORT).show(); } else { authenticatePinAsyncTask
	 * authenticatePINTask = new authenticatePinAsyncTask();
	 * authenticatePINTask.execute(); } } else { validateTextFields(); } }
	 * return false; } };
	 * 
	 * TextView.OnEditorActionListener confirmPinListener = new
	 * TextView.OnEditorActionListener() {
	 * 
	 * @Override public boolean onEditorAction(TextView textView, int actionId,
	 * KeyEvent event) {
	 * 
	 * if (actionId == EditorInfo.IME_ACTION_DONE || event.getKeyCode() ==
	 * KeyEvent.KEYCODE_ENTER) { // mExtAdapReg = //
	 * ExternalAdapterRegistrationClass.getInstance(mContext);
	 * 
	 * if (isShowAuthenticatePinUi) {
	 * 
	 * mPin = mEnterPinEditText.getText().toString(); if (mPin.equals("")) {
	 * Toast.makeText(mContext, "Please enter credentials",
	 * Toast.LENGTH_SHORT).show(); } else { authenticatePinAsyncTask
	 * authenticatePINTask = new authenticatePinAsyncTask();
	 * authenticatePINTask.execute(); } } else { validateTextFields(); } }
	 * return false; } };
	 */
	private void validateTextFields() {

		mPin = mEnterPinEditText.getText().toString();

		mConfirmPin = mConfirmPinEditText.getText().toString();

		PersonaLog.e("mPin", mPin);
		PersonaLog.e("mConfirmPin", mConfirmPin);

		if (mPin.equals("") || mConfirmPin.equals(""))
			Toast.makeText(mContext, "Fields cannot be empty",
					Toast.LENGTH_SHORT).show();
		else if (!mPin.equals(mConfirmPin)) {
			customKeyBoard.clearEditBox();
			customKeyBoard1.clearEditBox();
			Toast.makeText(mContext, mCredentialsNotmatching,
					Toast.LENGTH_SHORT).show();
		} else {

			savePin(mPin);
		}
	}

	public class authenticatePinAsyncTask extends
			AsyncTask<String, Integer, Integer> {

		@Override
		protected Integer doInBackground(String... arg0) {
			int result = authenticatePin();
			PersonaLog.d(TAG,
					"---------- authenticate pin completed --------- ");
			return result;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			if (result == -2) {
				dismissDialog();
				if (attemptsLeft <= 0) {
					Intent intent = new Intent(mContext,
							PersonaSecurityProfileUpdate.class);
					intent.putExtra("securityevent",
							PersonaConstants.INCORRECT_LOGIN_ATTEMPTS);
					intent.putExtra("securityaction", PersonaConstants.WIPE_APP);
					intent.putExtra("wipedAlready", false);
					/*
					 * long accountId =
					 * EmailAccount.getDefaultAccountId(mContext); EmailAccount
					 * account = EmailAccount.restoreAccountWithId( mContext,
					 * accountId);
					 */

					// Crash fix on account id query
					long accountId = EmEmailContent.Account
							.getDefaultAccountId(mContext);
					EmEmailContent.Account account = EmEmailContent.Account
							.restoreAccountWithId(mContext, accountId);
					if (account != null) {
						intent.putExtra("Email_id", account.toString());
					}
					mContext.startActivity(intent);
					finishCurrentActivity();
				} else {
					customKeyBoard.clearEditBox();

					Toast.makeText(mContext, mIncorrectCredentials,
							Toast.LENGTH_SHORT).show();
				}

			}  else if (result == 0) {

				if (PersonaMainActivity.isCertificateBasedAuth) {
					boolean isAckSent = new SharedPreferences(mContext)
							.getBoolean("ack_sent", false);
					boolean isCertificateDownloaded = new SharedPreferences(
							mContext).getBoolean("certificate_downloaded",
							false);

					if (!isAckSent && isCertificateDownloaded)
						sendCertificateAcknowledgement();
				}
				checkLoginToday();

			}

			else if (result == -3) {
				dismissDialog();
				Intent intent = new Intent(mContext,
						PersonaSecurityProfileUpdate.class);
				intent.putExtra("securityevent",
						PersonaConstants.APPLICATION_HACKED);
				intent.putExtra("securityaction", PersonaConstants.WIPE_APP);
				intent.putExtra("wipedAlready", false);
				long accountId = EmEmailContent.Account
						.getDefaultAccountId(mContext);
				EmEmailContent.Account account = EmEmailContent.Account
						.restoreAccountWithId(mContext, accountId);
				if (account != null) {
					intent.putExtra("Email_id", account.toString());
				}
				mContext.startActivity(intent);
				finishCurrentActivity();
			}
			
			else {
				dismissDialog();
				customKeyBoard.clearEditBox();
				Toast.makeText(mContext, mValidationFailed,
						Toast.LENGTH_SHORT).show();
			}

		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (confirmingPinProgress == null)
				confirmingPinProgress = new ProgressDialog(mContext,
						R.style.PersonaNewDialog);
			confirmingPinProgress.setCancelable(false);
			confirmingPinProgress.setMessage(mContext.getResources().getString(
					R.string.prValCredentials));
			if (!mMainActivity.isFinishing())
				confirmingPinProgress.show();
		}

	}

	private void dismissDialog() {
		if (confirmingPinProgress != null && confirmingPinProgress.isShowing()
				&& !mMainActivity.isFinishing()) {
			confirmingPinProgress.dismiss();

		}
	}

	private int authenticatePin() {
		int returnValue = -4;
		try {
			if (TruBoxDatabase.validatePassword(mPin)) {
				updatePreferences();
				isEmailAccountConfigured = new SharedPreferences(mContext)
						.getBoolean("isEmailAccountConfigured", false);
				if (!isEmailAccountConfigured)
					getEmailSettings();
				returnValue = 0;
			} else {
				numOfAttemptsMade++;
				attemptsLeft = inCorrectPinAttemptsAllowed - numOfAttemptsMade;
				returnValue = -2;

			}
		} catch (TruboxException e) {
			returnValue = -3;

		}
		catch(Exception e) {
			returnValue = -4;
		}
		return returnValue;

	}

	private void updatePreferences() {
		new SharedPreferences(mContext).edit()
				.putBoolean("showPinOnResume", false).commit();
	}

	/**
	 * Certificate
	 */
	private void download_certificate() {

		if (mCommonfunctions.isConnectedToNetwork()) {
			HashMap map = new HashMap();

			JSONObject holder = null;

			try {
				holder = new JSONObject();
				String sDeviceId = Secure.getString(
						mContext.getContentResolver(), Secure.ANDROID_ID);
				holder.put("device_uid", sDeviceId);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			PersonaLog.d(TAG,
					"PersonaAPIConstants.DOWNLOAD_CERTIFICATE : holder :"
							+ holder);

			mDownloadcertificate = new PersonaJSONClient(
					mContext,
					personaLocalAuthentication,
					holder,
					mContext.getResources().getString(
							R.string.download_email_certificate),
					PersonaCertfifcateAuthConstants.DOWNLOAD_CERTIFICATE_PERSONA,
					false, CERTIFICATE_DOWNLOAD, false);
			mDownloadcertificate.execute();
		} else {

			showCertificateWarningScreen(serverUnreachabelErrorCode);
		}

	}

	public void showAppsList() {

		try {
			mContext.unregisterReceiver(LoginCompleteReceiver);
		} catch (Exception e) {
			// e.printStackTrace();
		}
		boolean isExchangeAccountConfigured = new SharedPreferences(mContext)
				.getBoolean("isEmailAccountConfigured", false);
		// To cancel all SafeSpace crash notifications after login done
		cancelNotification(9999);

		PersonaLog.d(TAG, "---------- mPimAuthType -----" + mPimAuthType);

		// PIN_SCREEN_ISSUE -->
		if (!emailNotification)
			emailNotification = new SharedPreferences(mContext).getBoolean(
					"enotificationonpin", false);
		if (mBndle != null && !emailNotification) {
			if (mBndle.getBoolean("fromTimer")) {
				PersonaLog.d(TAG, "-- mAuthTypeBasic - from TIMER CLASS");
				finishCurrentActivity();
				return;
			}
		}

		switch (mPimAuthType) {
		case PersonaConstants.mAuthTypeNone:
			break;

		case PersonaConstants.mAuthTypeBasic:

			if (!isExchangeAccountConfigured) {

				PersonaLog.d(TAG, "Entering Email Account Configuration");

				new SharedPreferences(mContext).edit().putInt("AuthType", 1)
						.commit();

				new SharedPreferences(mContext).edit()
						.putBoolean("useManualSetup", this.bManualEmailConfig)
						.commit();
				new SharedPreferences(mContext).edit()
						.putBoolean("usereditable", this.bUserEmailEditable)
						.commit();
				Intent EmailSetupIntent = new Intent(mContext,
						EmAccountSetupExchange.class);
				EmailSetupIntent.putExtra("username", mUsername);
				EmailSetupIntent.putExtra("password", mPassword);
				EmailSetupIntent.putExtra("callDeviceProvisioning", true);
				EmailSetupIntent.putExtra("domain", mDomain);
				EmailSetupIntent.putExtra("server", mServer);
				EmailSetupIntent.putExtra("callDeviceProvisioning", true);
				mContext.startActivity(EmailSetupIntent);
				PersonaLog.d(TAG, "Completed Email Configuration");
				finishCurrentActivity();
			} else {
				if (emailNotification) {

					PersonaLog.d(TAG, "========== email notification =======");
					new SharedPreferences(mContext).edit()
					// 367712
							.putBoolean("emailnotification", false).commit();
					Intent emailWelcomeActivity = new Intent(mContext,
							EmMessageList.class);
					new SharedPreferences(mContext).edit()
							.putBoolean("enotificationonpin", false).commit();
					new SharedPreferences(mContext).edit()
							.putBoolean("notifyEmailFrmPersona", true).commit();
					mContext.startActivity(emailWelcomeActivity);
					finishCurrentActivity();
				} else if (PersonaMainActivity.securityNotification) {
					long accountId = new SharedPreferences(mContext).getLong(
							"notificatioId", -1);
					if (accountId != -1) {
						PersonaLog.d(TAG,
								"========== Calling Account Security for id: "
										+ accountId);

						Intent emailActivity = new Intent(mContext,
								EmAccountSecurity.class);

						emailActivity.putExtra("accountId", accountId);
						PersonaLog.d(TAG,
								"========== In Email to configured security notification : accountId: "
										+ accountId);
						mContext.startActivity(emailActivity);
						PersonaMainActivity.securityNotification = false;
						finishCurrentActivity();
					}

				}

				else {
					cancelNotification(PersonaConstants.appCrashNotificationId);
					EmExchangeUtils.startExchangeService(mContext);
					long accountId = EmEmailContent.Account
							.getDefaultAccountId(mContext);
					EmMailService.actionNotifyNewMessages(mContext, accountId);
					EmEmailBroadcastProcessorService
							.cancelServiceNotification(mContext);

					Intent PersonaLauncherIntent = new Intent(mContext,
							PersonaLauncher.class);

					mContext.startActivity(PersonaLauncherIntent);
					finishCurrentActivity();

				}
			}

			break;

		case PersonaConstants.mAuthTypeCertificate:

			new SharedPreferences(mContext).edit().putInt("AuthType", 2)
					.commit();
			PersonaLog.d("", "======== mAuthTypeCertificate: " + ""
					+ PersonaConstants.mAuthTypeCertificate);

			if (PersonaMainActivity.isCertificateBasedAuth) {

				if (!checkForCertificateInDb()) {
					download_certificate();

				} else {
					if (!isEmailAccountConfigured) {

						Intent EmailSetupIntent = new Intent(
								mContext,
								com.cognizant.trumobi.em.customsetup.EmAccountSetupExchange.class);
						EmailSetupIntent.putExtra("username", mUsername);

						EmailSetupIntent.putExtra("password", mPassword);
						mContext.startActivity(EmailSetupIntent);
						finishCurrentActivity();

					} else {
						postPINValidation();
					}
				}
			}

			else {
				if (!isEmailAccountConfigured) {

					Intent EmailSetupIntent = new Intent(
							mContext,
							com.cognizant.trumobi.em.customsetup.EmAccountSetupExchange.class);
					mContext.startActivity(EmailSetupIntent);
					finishCurrentActivity();

				} else {
					postPINValidation();
				}
			}

			break;

		}
	}

	public boolean checkForCertificateInDb() {

		boolean isCursorCountNotNull = false;

		String ID = "_id";
		String COL_Email_id = "email_id";
		String COL_Domain = "domain";
		String COL_Server = "server";
		String COL_Password = "password";
		String COL_File = "file";
		String[] list_pro = { ID, COL_Email_id, COL_File, COL_Domain,
				COL_Server, COL_Password };

		Cursor cursor = mContext.getContentResolver().query(
				PersonaPimSettingsProvider.TABLE_EMAIL_URI, list_pro, null,
				null, null);
		try {
			if (cursor.getCount() == 0) {
				PersonaLog.e(TAG, "Cursor count is null");
				isCursorCountNotNull = false;
			} else {
				PersonaLog.e(TAG, "Cursor count is not null");
				isCursorCountNotNull = true;
			}
		} catch (Exception e) {
			PersonaLog.e(TAG, e.toString());
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return isCursorCountNotNull;
	}

	public void postPINValidation() {

		if (emailNotification) {// geetha - added to handle email
			// notification
			Intent emailWelcomeActivity = new Intent(
					mContext,
					com.cognizant.trumobi.em.customsetup.EmAccountSetupExchange.class);
			mContext.startActivity(emailWelcomeActivity);
			finishCurrentActivity();
		} else {

			EmExchangeUtils.startExchangeService(mContext);
			long accountId = EmEmailContent.Account
					.getDefaultAccountId(mContext);
			EmMailService.actionNotifyNewMessages(mContext, accountId);
			EmEmailBroadcastProcessorService
					.cancelServiceNotification(mContext);

			Intent PersonaLauncherIntent = new Intent(mContext,
					PersonaLauncher.class);
			mContext.startActivity(PersonaLauncherIntent);
			finishCurrentActivity();
		}
	}

	/**
	 * Certificate/App Upgrade
	 */
	private void routineLogincheck() {

		JSONObject holder = null;

		try {
			holder = new JSONObject();

			holder.put("device_uid", mCommonfunctions.getDeviceID());
			// int app_id = checkForAppUpgrade();
			// holder.put("app_id", app_id);
			holder.put("device_type", mCommonfunctions.getDeviceType(mContext));
			holder.put("app_version", mCommonfunctions.getAppVersion());
			holder.put("bundle_identifier", mCommonfunctions.getBundleId());
		} catch (JSONException e) {
			e.printStackTrace();
		}

		PersonaLog.d(TAG, "PersonaAPIConstants.routineLogincheck : holder :"
				+ holder);

		mDownloadcertificate = new PersonaJSONClient(mContext,
				personaLocalAuthentication, holder, mContext.getResources()
						.getString(R.string.prValCredentials),
				PersonaCertfifcateAuthConstants.ROUTINE_LOGIN_CHECK, false,
				ROUTINE_LOGIN_CHECK, false);
		mDownloadcertificate.execute();

	}

	private int checkForAppUpgrade() {

		String appVersionFromPref = new SharedPreferences(mContext).getString(
				"new_app_version", "");
		int latestAppid = new SharedPreferences(mContext).getInt("new_app_id",
				0);
		String appRecentVersion = mCommonfunctions.getAppVersion();

		if (appRecentVersion.equals(appVersionFromPref)) {

			new SharedPreferences(mContext).edit()
					.putInt("app_id", latestAppid).commit();
			return latestAppid;
		} else {
			int appId = new SharedPreferences(mContext).getInt("app_id", 0);
			return appId;
		}
	}

	public void cancelNotification(int notificationId) {

		if (Context.NOTIFICATION_SERVICE != null) {
			String ns = Context.NOTIFICATION_SERVICE;
			NotificationManager nMgr = (NotificationManager) mContext
					.getSystemService(ns);
			nMgr.cancel(notificationId);
		}
	}

	// }
	/*
	 * else { Toast.makeText(PersonaLocalAuthentication.this,
	 * getResources().getString(R.string.no_enterprise_apps),
	 * Toast.LENGTH_LONG).show(); finish(); }
	 */

	private void finishCurrentActivity() {
		switch (mCallingActivity) {
		case FROM_PERSONAMAINACTIVITY:
			mMainActivity.finish();
			break;
		case FROM_PERSONALOGINACTIVITY:
			mLoginActivity.finish();
			break;
		}

	}

	private void shouldShowExpiryAlert(Context context, int expiryDays) {

		File mDB;
		String username;
		long pwdSetDate = 0L;
		int daysSincePwdSet, mDaysLeft;
		username = getUserNameFromPreferences(context);
		mDB = context.getDatabasePath(TruBoxDatabase.getHashValue(
				PersonaPimSettingsDbHelper.DB_NAME, context));
		PersonaLog.d("personalocalauthentication",
				"----------- databasepath -------" + mDB);
		TruBoxDatabase.initialiseTruBoxDatabase(context);
		TruBoxDatabase mPersonaDB = TruBoxDatabase.openOrCreateDatabase(mDB,
				null);

		String tabName = PersonaPimSettingsDbHelper.TABLE_PWDSETTINGS;
		String sQuery = "SELECT * FROM " + tabName + " WHERE "
				+ PersonaPimSettingsDbHelper.USERNAME_PWDSETTINGS + "=?";

		String whereArgs[] = { username };
		Cursor c = mPersonaDB.rawQuery(sQuery, whereArgs);
		PersonaLog.d(TAG, "**** NEW QUERY out ***");

		if (c.moveToFirst()) {
			while (!c.isAfterLast()) {
				pwdSetDate = c
						.getLong(c
								.getColumnIndex(PersonaPimSettingsDbHelper.CREATED_DATE));
				PersonaLog.d(TAG,
						"Reading values from PWDSETTINGS table and pwd set date is  "
								+ pwdSetDate);
				c.moveToNext();
			}
		}
		c.close();
		mPersonaDB.close();
		PersonaLog.d("PersonaLocalAuthentication",
				"------- daysleft ------ getTodayMidnightTime()"
						+ getTodayMidnightTime());
		PersonaLog.d("PersonaLocalAuthentication",
				"------- daysleft ------ pwdSetDate" + pwdSetDate);
		PersonaLog
				.d("PersonaLocalAuthentication",
						"------- daysleft ------"
								+ ((getTodayMidnightTime() - pwdSetDate) / (float) (1000 * 60 * 60 * 24)));

		daysSincePwdSet = Math.round((getTodayMidnightTime() - pwdSetDate)
				/ (float) (1000 * 60 * 60 * 24));
		PersonaLog.d("PersonaLocalAuthentication",
				"------- daysSincePwdSet ------ " + daysSincePwdSet
						+ "------ expiryDays -----" + expiryDays);
		mDaysLeft = expiryDays - daysSincePwdSet;
		PersonaLog.d("personaLocalAuth", "daysleft" + mDaysLeft);
		new SharedPreferences(context).edit().putInt("daysLeft", mDaysLeft)
				.commit();

		if (mDaysLeft <= 3) {
			new SharedPreferences(context).edit()
					.putBoolean("showExpiry", true).commit();
		}
	}

	private String getUserNameFromPreferences(Context context) {

		String username = new SharedPreferences(context).getString(
				"trumobi_username", "");
		return username;

	}

	private void savePin(String personaPin) {

		try {

			if (mCommonfunctions.isPinSatisfiesRequirements(mPin,
					mLocalAuthtype, mPwdLength)) {

				if (TruBoxDatabase.generatePassword(mPin)) {
					postGenerationOfDBKey();
				}

			} else {
				switch (mLocalAuthtype) {

				case PersonaConstants.AUTH_TYPE_NUMERIC:
					customKeyBoard.clearEditBox();
					customKeyBoard1.clearEditBox();
					Toast.makeText(mContext,
							mContext.getString(R.string.pr_pin_meet_criteria),
							Toast.LENGTH_LONG).show();
					break;
				case PersonaConstants.AUTH_TYPE_ALPHABETS:

				case PersonaConstants.AUTH_TYPE_ALPHANUMERIC:
				case PersonaConstants.AUTH_TYPE_PASSWORD_ALLOW_NUMERALS:
					customKeyBoard.clearEditBox();
					customKeyBoard1.clearEditBox();
					Toast.makeText(
							mContext,
							mContext.getString(R.string.pr_password_meet_criteria),
							Toast.LENGTH_LONG).show();
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void postGenerationOfDBKey() {
		// Initializing Secure Browser DB - @ 31/10/2013
		SB_EncrptionDBTables sbEncryptTable = new SB_EncrptionDBTables(mContext);
		sbEncryptTable.getFromPersona();
		SavePIMSettings initiateSaving = new SavePIMSettings();
		initiateSaving.execute(mUsername);

	}

	/*
	 * public boolean isNotOlderPassword(String password, Context context) {
	 * TruBoxDatabase.setPimSettings(false); String mLastPassword = null,
	 * mOlderPassword = null; String DB_NAME_HASHED =
	 * TruBoxDatabase.getHashValue( PersonaDbEncryptHelper.DB_NAME, context);
	 * mDB = context.getDatabasePath(DB_NAME_HASHED); mUsername = new
	 * SharedPreferences(context).getString( "trumobi_username", ""); // File
	 * mDB = context.getDatabasePath(PersonaDbEncryptHelper.DB_NAME); try {
	 * mPersonaDB = TruBoxDatabase.openOrCreateDatabase(mDB, null); String
	 * tabName = PersonaDbEncryptHelper.TABLE_PWDSETTINGS; String query =
	 * "SELECT * FROM " + tabName + " WHERE " +
	 * PersonaDbEncryptHelper.USERNAME_PWDSETTINGS + "= '" + mUsername + "'";
	 * Cursor c = mPersonaDB.rawQuery(query, null);
	 * 
	 * c = mPersonaDB.rawQuery("SELECT * FROM " + tabName, null);
	 * 
	 * if (c.moveToFirst()) { mLastPassword = c.getString(8); mOlderPassword =
	 * c.getString(9); // mOldestPassword = c.getString(10);
	 * PersonaLog.d("PersonaMainActivity",
	 * "Reading values from PWDSETTINGS table  " + " mLastPassword " +
	 * mLastPassword + " mOlderPassword " + mOlderPassword); // +
	 * " mOldestPassword " + mOldestPassword); }
	 * 
	 * c.close(); } catch (Exception e) { e.printStackTrace(); } finally { if
	 * (mPersonaDB != null && mPersonaDB.isOpen()) { mPersonaDB.close(); } }
	 * 
	 * if (password.equals(mLastPassword) || password.equals(mOlderPassword)) {
	 * return false; } saveCredential(mLastPassword, mOlderPassword, context);
	 * return true; }
	 */
	/*
	 * private void saveCredential( String mLastPassword, String mOlderPassword,
	 * Context context) {
	 * 
	 * String[] args = {mLastPassword, mOlderPassword,
	 * String.valueOf(getTodayMidnightTime()) }; String DB_NAME_HASHED =
	 * TruBoxDatabase.getHashValue( PersonaDbEncryptHelper.DB_NAME, mContext);
	 * mDB = context.getDatabasePath(DB_NAME_HASHED);
	 * 
	 * // File mDB = context.getDatabasePath(PersonaDbEncryptHelper.DB_NAME);
	 * try { mPersonaDB = TruBoxDatabase.openOrCreateDatabase(mDB, null);
	 * mPersonaDB.execSQL("UPDATE " + PersonaDbEncryptHelper.TABLE_PWDSETTINGS +
	 * " SET " // + PersonaDbEncryptHelper.LAST_PASSWORD + "= ?," +
	 * PersonaDbEncryptHelper.OLDER_PASSWORD + "= ?, " +
	 * PersonaDbEncryptHelper.OLDEST_PASSWORD + "= ?," +
	 * PersonaDbEncryptHelper.CREATED_DATE + "= ?" +
	 * 
	 * "WHERE " + PersonaDbEncryptHelper.USERNAME_PWDSETTINGS + "= '" +
	 * mUsername + "'", args); PersonaLog.d("PersonaLoginActivity",
	 * "updating pim setting details : " + "older password:" + mLastPassword +
	 * "oldest: " + mOlderPassword); } catch (Exception e) {
	 * e.printStackTrace(); } finally { if (mPersonaDB != null &&
	 * mPersonaDB.isOpen()) { mPersonaDB.close(); } }
	 * 
	 * }
	 */

	public void sendCertificateAcknowledgement() {

		JSONObject holder = null;

		try {
			holder = new JSONObject();
			int certificateReqId = new SharedPreferences(mContext).getInt(
					PersonaConstants.CERT_REQUEST_ID, 0);

			holder.put(PersonaConstants.CERT_REQUEST_ID, certificateReqId);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		PersonaLog.d(TAG,
				"PersonaAPIConstants.ACKNOWLEDGEMENT_CERTIFICATE_DOWNLOAD : holder :"
						+ holder);
		mSendAcknlowdge = new PersonaJSONClient(
				mContext,
				personaLocalAuthentication,
				holder,
				mContext.getResources().getString(R.string.loading),
				PersonaCertfifcateAuthConstants.ACKNOWLEDGEMENT_CERTIFICATE_DOWNLOAD,
				false, SEND_ACK, true);

		mSendAcknlowdge.execute();

	}

	/**
	 * Certificate
	 */
	@Override
	public void onRemoteCallComplete(String json, int code) {
		PersonaLog.d(TAG, "response : json :" + json);

		switch (code) {

		case CERTIFICATE_DOWNLOAD:

			try {
				PersonaLog.d(TAG,
						"PersonaAPIConstants.DOWNLOAD_CERTIFICATE : json :"
								+ json);
				Gson gson = new Gson();
				PersonaCertificateDownloadResponse response = gson.fromJson(
						json, PersonaCertificateDownloadResponse.class);

				if (response != null
						&& response.message_status.contains("Success")) {

					// String preferenceFileName=new
					// String(nBoxContentEncryption.encrypt(PersonaConstants.PERSONAPREFERENCESFILE));

					String password = response.appstore_information.user_certificate_details.certificate_pwd;
					String exchange_server = response.appstore_information.exchange_server;
					String email_domain = response.appstore_information.email_domain;
					String email_id = response.appstore_information.user_certificate_details.user_email;
					String userCertificate = response.appstore_information.user_certificate_details.user_cert;
					int certificateID = response.appstore_information.user_certificate_details.certificate_request_id;
					String filename = response.appstore_information.user_certificate_details.user_email
							+ "_cert" + ".pfx";
					if (userCertificate != null) {

						byte[] convertme = Base64
								.decode(response.appstore_information.user_certificate_details.user_cert,
										Base64.DEFAULT);

						// writePfxToSdCard(filename,convertme);
						new SharedPreferences(mContext).edit()
								.putBoolean("certificate_downloaded", true)
								.commit();
						new SharedPreferences(mContext)
								.edit()
								.putInt(PersonaConstants.CERT_REQUEST_ID,
										certificateID).commit();
						ContentValues values = new ContentValues();
						values.put("email_id", email_id);
						values.put("domain", email_domain);
						values.put("server", exchange_server);
						values.put("password", password);
						values.put("file", convertme);

						String ID = "_id";

						String[] list_pro = { ID };

						Cursor cursor = mContext.getContentResolver().query(
								PersonaPimSettingsProvider.TABLE_EMAIL_URI,
								list_pro, null, null, null);
						try {

							PersonaLog.d(TAG, " " + cursor.getCount());

							if (cursor.getCount() == 0) {

								mContext.getContentResolver()
										.insert(PersonaPimSettingsProvider.TABLE_EMAIL_URI,
												values);
							} else {
								PersonaLog.d(TAG, " " + cursor.getCount()
										+ " :update: ");
								String where = PersonaPimSettingsDbHelper.ID
										+ " = ? ";
								mContext.getContentResolver()
										.update(PersonaPimSettingsProvider.TABLE_EMAIL_URI,

										values, where, new String[] { "1" });

							}
						} finally {
							if (cursor != null)
								cursor.close();

						}

						boolean isAcksent = new SharedPreferences(mContext)
								.getBoolean("ack_sent", false);
						if (!isAcksent) {
							sendCertificateAcknowledgement();

						}

						Toast.makeText(
								mContext,
								mContext.getResources()
										.getString(
												R.string.certificate_downloaded_successfully),
								Toast.LENGTH_LONG).show();
						Intent EmailSetupIntent = new Intent(mContext,
								com.cognizant.trumobi.em.customsetup.EmAccountSetupExchange.class);
						mContext.startActivity(EmailSetupIntent);
						finishCurrentActivity();
					}

					// 290778 added this check if the certificate service
					// request is true but the certificate information is not
					// available
					else {
						showCertificateWarningScreen(certificateDowloadFailureErrorcode);
					}

				}

				// Failure response in downling the certificate
				else if (response.message_status.contains("Failure")) {

					PersonaLoginFailureResponse loginFailureResponse = gson
							.fromJson(json, PersonaLoginFailureResponse.class);

					parseFailureResponse(loginFailureResponse);

					// Failure response
					/*
					 * Toast.makeText( mContext,
					 * mContext.getResources().getString(
					 * R.string.error_certificate_download),
					 * Toast.LENGTH_LONG).show();
					 */
					// showCertificateWarningScreen(certificateDowloadFailureErrorcode);

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
		case ROUTINE_LOGIN_CHECK:

			try {
				PersonaLog.d(TAG,
						"PersonaAPIConstants.ROUTINE_LOGIN_CHECK : json :"
								+ json);
				Gson gson = new Gson();
				PersonaOneTimeLoginResponse response = gson.fromJson(json,
						PersonaOneTimeLoginResponse.class);
				if (response != null
						&& response.message_status.contains("Success")) {
					confirmingPinProgress.dismiss();
					new SharedPreferences(mContext).edit()
							.putLong("LastEmaasLogin", getTodayMidnightTime())
							.commit();
					PersonaLog.d(TAG, "------ emaaslogin success ---------");

					if (response.appstore_information.require_selective_wipe == true) {

						PersonaLog.d(TAG,
								"------ require_selective_wipe  ---------"
										+ "true");
						Intent intent = new Intent(mContext,
								PersonaSecurityProfileUpdate.class);
						intent.putExtra("securityevent",
								PersonaConstants.APPLICATION_HACKED);
						intent.putExtra("securityaction",
								PersonaConstants.WIPE_APP);
						intent.putExtra("wipedAlready", false);
						/*
						 * long accountId = EmailAccount
						 * .getDefaultAccountId(mContext); EmailAccount account
						 * = EmailAccount .restoreAccountWithId(mContext,
						 * accountId);
						 */
						long accountId = EmEmailContent.Account
								.getDefaultAccountId(mContext);
						EmEmailContent.Account account = EmEmailContent.Account
								.restoreAccountWithId(mContext, accountId);
						if (account != null) {
							intent.putExtra("Email_id", account.toString());
						}
						mContext.startActivity(intent);
						finishCurrentActivity();
					}

					else if (response.appstore_information.app_upgrade_info.version_upgrade == 1
							|| response.appstore_information.app_upgrade_info.version_upgrade == 2)

					{
						int appId = response.appstore_information.app_upgrade_info.app_id;
						String newAppVersion = response.appstore_information.app_upgrade_info.app_version;
						new SharedPreferences(mContext).edit()
								.putInt(" new_app_id", appId).commit();
						new SharedPreferences(mContext).edit()
								.putString("new_app_version", newAppVersion)
								.commit();
						String appUrl = response.appstore_information.app_upgrade_info.binary_source_path;

						if (appUrl != null) {
							/*
							 * Intent intent =new
							 * Intent(Intent.ACTION_VIEW,Uri.parse(appUrl));
							 * mContext.startActivity(intent);
							 * finishCurrentActivity();
							 */

							PersonaCommonfunctions
									.showAlert(
											mMainActivity,
											mContext.getResources().getString(
													R.string.app_name),
											mContext.getResources()
													.getString(
															R.string.download_app_message),
											mContext.getResources().getString(
													R.string.install_app),
											true, appUrl);

						}

					}

					else {

						checkUpdateInPimSettings(mContext, 0);

					}

				} else {
					confirmingPinProgress.dismiss();
					/*
					 * Toast.makeText( mContext, "Invalid User Session",
					 * Toast.LENGTH_LONG).show();
					 */
					checkUpdateInPimSettings(mContext, 0);
				}
			} catch (Exception e) {
				// assuming no network available;
				confirmingPinProgress.dismiss();
				e.printStackTrace();

				PersonaLog.e(TAG, e.toString());
				checkUpdateInPimSettings(mContext, 0);

			}
			break;

		/*
		 * case SEND_ACK:
		 * 
		 * try { PersonaLog.d(TAG,
		 * "PersonaAPIConstants.ROUTINE_LOGIN_CHECK : json :" + json); Gson gson
		 * = new Gson(); PersonaLoginResponse response = gson.fromJson(json,
		 * PersonaLoginResponse.class); parseAckResponse(response);
		 * 
		 * } catch (Exception e) { e.printStackTrace(); } break;
		 */
		}
	}

	private void parseAckResponse(PersonaLoginResponse response) {

		if (response != null && response.message_status.contains("Success")) {

			new SharedPreferences(mContext).edit().putBoolean("ack_sent", true)
					.commit();
		} else {

			new SharedPreferences(mContext).edit()
					.putBoolean("ack_sent", false).commit();
		}

	}

	private void showMessage(int event, int action) {
		FragmentManager fm;
		if (mMainActivity != null)
			fm = ((FragmentActivity) mMainActivity).getSupportFragmentManager();
		else
			fm = ((FragmentActivity) mLoginActivity)
					.getSupportFragmentManager();

		PersonaLog.d(TAG, "#################### Event called is : " + event);
		PersonaLog.d(TAG, "#################### Action called is : " + action);
		PersonaSecurityUpdateDialogFragment dialog = new PersonaSecurityUpdateDialogFragment(
				event, action);
		dialog.setRetainInstance(true);
		dialog.show(fm, "fragment_name");

	}

	private void parseFailureResponse(
			PersonaLoginFailureResponse personaLoginFailureResponse) {

		int errorCode = Integer.parseInt(personaLoginFailureResponse.errorCode);

		switch (errorCode) {

		case 1242:
			showMessage(PersonaCertfifcateAuthConstants.CERT_FAILURE_EVENT,
					PersonaCertfifcateAuthConstants.RE_REQUEST_CERT);
			break;

		case 1243:

			showMessage(PersonaCertfifcateAuthConstants.CERT_FAILURE_EVENT,
					PersonaCertfifcateAuthConstants.WARN_USER);
			break;
		default:
			// showMessage();
			showCertificateWarningScreen(certificateDowloadFailureErrorcode);

		}

	}

	private void showCertificateWarningScreen(int code) {
		Intent intent = new Intent(mContext,
				PersonaUserCertificateWarningActivity.class);

		PersonaLog.e(TAG, "" + code);
		intent.putExtra("warningMessageCode", code);
		mContext.startActivity(intent);
		finishCurrentActivity();
	}

	@Override
	public void onRemoteCallComplete(Message json, int code) {

	}

	public BroadcastReceiver LoginCompleteReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			// UI based on Login results commented due to SSLPeerUnverified
			// exception
			// called after login completion event(emaaslogin) and settings info
			// completed event(initiatefetchinfo)
			boolean settingsInfoFetchComplete = intent.getBooleanExtra(
					"settingsInfoFetchComplete", false);
			if (!settingsInfoFetchComplete) {
				PersonaLog
						.d(TAG, "------ settingsInfoComplete false ---------");
				boolean isEmaasLoginSuccess = intent.getBooleanExtra("Result",
						false);
				boolean isExchangePasswordValidationSuccess = intent
						.getBooleanExtra("isExchangePasswordCorrect", true);
				isEmaasLoginSuccess = true;
				if (isEmaasLoginSuccess) {
					new SharedPreferences(mContext).edit()
							.putLong("LastEmaasLogin", getTodayMidnightTime())
							.commit();
					PersonaLog.d(TAG, "------ emaaslogin success ---------");
					mExtAdapReg.initiateExternalToFetchAllSettingsInfo();

				} else {
					if (!isExchangePasswordValidationSuccess) {
						if (confirmingPinProgress != null
								&& confirmingPinProgress.isShowing())

							confirmingPinProgress.dismiss();
						Intent launchLoginScreenIntent = new Intent(context,
								PersonaLoginActivity.class);
						launchLoginScreenIntent.putExtra("passwordChanged",
								true);
						context.startActivity(launchLoginScreenIntent);
						finishCurrentActivity();
					} else {
						if (confirmingPinProgress != null
								&& confirmingPinProgress.isShowing())

							confirmingPinProgress.dismiss();
						Toast.makeText(context,
								"Emaas Login failed. Try Again",
								Toast.LENGTH_SHORT).show();
						PersonaLog.d(TAG, "------ emaaslogin failed ---------");
					}
				}

			} else {
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
						finishCurrentActivity();
						break;
					}

				} else {

					PersonaLog.d(TAG,
							"------ safe method returns true ---------");
					ExternalPIMSettingsInfo pimSettingsInfo;
					pimSettingsInfo = mExtAdapReg.getExternalPIMSettingsInfo();
					mLocalAuthtype = pimSettingsInfo.Passwordtype.getValue();
					mNumOfTrialsAllowed = pimSettingsInfo.nMaxPasswordTries;
					mExpiryInDays = pimSettingsInfo.PasswordExpires.getValue();
					mPwdLength = pimSettingsInfo.nPasswordLength;
					mPwdLocktimeInMins = pimSettingsInfo.PasswordFailLockout
							.getValue();
					mChangeHistory = pimSettingsInfo.nPasswordHistory;
					bPwdPin = pimSettingsInfo.bPasswordPIN;
					validateValuesFromSDK();
					PersonaLog.d(TAG,
							"------after safe method returns true --------- ");
					PersonaLog.d(TAG, "-------- mLocalAuthtype -------"
							+ mLocalAuthtype);
					PersonaLog.d(TAG, "-------- mNumOfTrialsAllowed -------"
							+ mNumOfTrialsAllowed);
					PersonaLog.d(TAG, "-------- mExpiryInDays -------"
							+ mExpiryInDays);
					PersonaLog.d(TAG, "-------- mPwdLength -------"
							+ mPwdLength);
					PersonaLog.d(TAG, "-------- mPwdLocktimeInMins -------"
							+ mPwdLocktimeInMins);
					PersonaLog.d(TAG, "-------- mChangeHistory -------"
							+ mChangeHistory);
					checkUpdateInPimSettings(mContext, 0);

				}

			}

		}
	};

	public interface DoneButtonListener {

		public void onDoneButtonClicked();

	}

	public static void hideSoftKeyboard(Activity activity) {
		InputMethodManager inputMethodManager = (InputMethodManager) activity
				.getSystemService(Activity.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus()
				.getWindowToken(), 0);
	}

	private void writePfxToSdCard(String filename, byte[] certInfo) {
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			File downloadFilePath = new File(
					PersonaCertfifcateAuthConstants.DOWNLOAD_PATH);
			if (!downloadFilePath.exists())
				downloadFilePath.mkdirs();
			File file = new File(
					Environment
							.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
					filename);

			// this will be used to write the downloaded
			// data
			// into the file we created
			FileOutputStream fos;
			try {
				fos = new FileOutputStream(file);
				fos.write(certInfo, 0, certInfo.length);
				fos.flush();
				fos.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
}
