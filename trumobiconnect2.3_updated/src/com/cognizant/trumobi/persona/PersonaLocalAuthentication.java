package com.cognizant.trumobi.persona;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

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
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.InputType;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.TruBoxSDK.SharedPreferences;
import com.TruBoxSDK.TruBoxDatabase;
import com.TruBoxSDK.TruboxException;
import com.cognizant.trumobi.PersonaDbEncryptHelper;
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
import com.cognizant.trumobi.log.PersonaLog;
import com.cognizant.trumobi.persona.constants.PersonaCertfifcateAuthConstants;
import com.cognizant.trumobi.persona.constants.PersonaConstants;
import com.cognizant.trumobi.persona.net.PersonaAllAppsListDetails;
import com.cognizant.trumobi.persona.net.PersonaCertificateDownloadResponse;
import com.cognizant.trumobi.persona.net.PersonaGetJsonInterface;
import com.cognizant.trumobi.persona.net.PersonaJSONClient;
import com.cognizant.trumobi.persona.net.PersonaLoginFailureResponse;
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

/**
 * 
 * KEYCODE AUTHOR PURPOSE PIN_SCREEN_ISSUE 367712 PIN Screen Issue on Email
 * Notification
 * 
 */

public class PersonaLocalAuthentication implements PersonaGetJsonInterface {

	TruMobBaseEditText mEnterPinEditText;
	TruMobBaseEditText mConfirmPinEditText;
	Button mSubmitButton;
	Context mContext;
	boolean isShowAuthenticatePinUi, mBackKeyDisabled;
	String mPin, mConfirmPin;
	ArrayList<String> fileName;
	ActivityManager mActivityManager;

	TextView mSetYourPinTextView;
	TextView mForgotPinTextView;
	String mPackageName;

	Customkeyboard customKeyBoard, customKeyBoard1;

	ProgressDialog dialog;
	boolean calledAfterForGotPin, callDeviceProvisioning;
	PersonaCommonfunctions mCommonfunctions;
	LinkedList<PersonaAllAppsListDetails> listDetails = null;

	PersonaLocalAuthentication personaLocalAuthentication;
	PersonaJSONClient mDownloadcertificate, mSendAcknlowdge;

	LinearLayout subHeaderLayout;
	ProgressDialog confirmingPinProgress;
	TextView downloadTextView, prSubCaptionTextView;
	TextView appNameTextView, minimumPINHintTextView, mconfirmPinTextView,
			mEnterPinTextView;

	String mUsername, mPassword, mDomain, mServer;
	String mIncorrectCredentials, mCredentialsNotmatching;
	int mLocalAuthtype, mPwdLength, mExpiryInDays, mPimAuthType;
	int mNumOfTrialsAllowed, mDaysLeft, mDaysSincePwdIsSet;
	long mPwdSetDate, todayMidNightTime;
	boolean isLengthSatisfied, mIsAlphaCharsExist, mIsNumeralExist,
			mIsNonAlphaCharsExist, mIsNonNumericCharsExist;

	String mPwdTypeInString;
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
	boolean emailNotification = false;
	Bundle mBndle;
	private static String TAG = PersonaLocalAuthentication.class.getName();
	private boolean isPinSaved;
	InputMethodManager imm;
	int attemptsLeft;
	boolean isEmailAccountConfigured;
	boolean bManualEmailConfig;
	boolean bUserEmailEditable;

	// PIM_TIMER_CHG - Added Bundle data to support data share from Activity to
	// normal class

	Button mDoneButton, mDoneButton2;
	Button mDoneButtonAlpha, mDoneButtonAlpha2;
	// PIM_TIMER_CHG - Added Bundle data to support data share from Activity to
	// normal class
	final int CERTIFICATE_DOWNLOAD = 1010, ROUTINE_LOGIN_CHECK = 1011,
			SEND_ACK = 1100;
	private TruBoxDatabase mPersonaDataBase;
	int certificateDowloadFailureErrorcode = 2001,
			serverUnreachabelErrorCode = 2005;

	public void bindMainActivityContext(Context context,
			PersonaMainActivity activity, Context baseContext,
			boolean newEmailNotify, boolean calendarNotification, Bundle data) {
		mContext = context;
		mMainActivity = activity;
		mCallingActivity = FROM_PERSONAMAINACTIVITY;
		emailNotification = newEmailNotify;
		TruBoxDatabase.initialiseTruBoxDatabase(baseContext);
		TruMobiTimerClass.userInteractedStopTimer();
		mBndle = data;

	}

	public void bindLoginActivityContext(Context context,
			PersonaLoginActivity activity, Context baseContext) {
		mContext = context;
		mLoginActivity = activity;
		mCallingActivity = FROM_PERSONALOGINACTIVITY;
		TruBoxDatabase.initialiseTruBoxDatabase(baseContext);

	}

	public void createView(int numberOfAttempts) {

		switch (mCallingActivity) {
		case FROM_PERSONAMAINACTIVITY:
			isPinSaved = true;
			createViewForAuthentication();
			inCorrectPinAttemptsAllowed = numberOfAttempts;
			break;
		case FROM_PERSONALOGINACTIVITY:
			isPinSaved = false;
			createViewForSave();
			break;
		}
	}

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

		// mEnterPinEditText.setOnEditorActionListener(enterPinListener);
		// mConfirmPinEditText.setOnEditorActionListener(confirmPinListener);
		customKeyBoard = (Customkeyboard) mLoginActivity
				.findViewById(R.id.key_edit);
		customKeyBoard1 = (Customkeyboard) mLoginActivity
				.findViewById(R.id.key_edit1);
		// mSubmitButton = (Button)
		// mLoginActivity.findViewById(R.id.submitButton);
		// centerView=(View)findViewById(R.id.centerInParentView);
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
		PersonaLog.e("mDoneButton2.getSelectionEnd()",
				"" + mDoneButton2.getSelectionEnd());
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

	}

	public void createViewForAuthentication() {

		personaLocalAuthentication = this;
		mMainActivity.setContentView(R.layout.pr_set_your_pin);
		customKeyBoard = (Customkeyboard) mMainActivity
				.findViewById(R.id.key_edit);
		mPackageName = mContext.getApplicationInfo().packageName;
		mSetYourPinTextView = (TextView) mMainActivity
				.findViewById(R.id.setYourPinTextView);

		mEnterPinEditText = (TruMobBaseEditText) mMainActivity
				.findViewById(R.id.enterPinEditText);
		mConfirmPinEditText = (TruMobBaseEditText) mMainActivity
				.findViewById(R.id.confirmPinEditText);
		mconfirmPinTextView = (TextView) mMainActivity
				.findViewById(R.id.confirm_credentials_label);
		// mEnterPinEditText.setOnEditorActionListener(enterPinListener);
		mEnterPinEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
		// mConfirmPinEditText.setOnEditorActionListener(confirmPinListener);
		customKeyBoard = (Customkeyboard) mMainActivity
				.findViewById(R.id.key_edit);
		customKeyBoard1 = (Customkeyboard) mMainActivity
				.findViewById(R.id.key_edit1);
		// mSubmitButton = (Button)
		// mMainActivity.findViewById(R.id.submitButton);
		// centerView=(View)findViewById(R.id.centerInParentView);
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
		mEnterPinTextView = (TextView) mMainActivity
				.findViewById(R.id.enter_credentials_label);
		mconfirmPinTextView = (TextView) mMainActivity
				.findViewById(R.id.confirm_credentials_label);
		mDoneButton = (Button) mMainActivity.findViewById(R.id.button_right);

		mDoneButton.setOnClickListener(submitOnclickListener);
		mDoneButtonAlpha = (Button) customKeyBoard.findViewById(R.id.button47);
		mDoneButtonAlpha.setOnClickListener(submitOnclickListener);
		/*
		 * LocalBroadcastManager.getInstance(mContext).registerReceiver(
		 * LoginCompleteReceiver, new IntentFilter("LoginActionComplete"));
		 */
		mContext.registerReceiver(LoginCompleteReceiver, new IntentFilter(
				"LoginActionComplete"));
		PersonaLog.e("PersonaLocalAuthentication", "createView ends");
		normalTf = Typeface.createFromAsset(mMainActivity.getAssets(),
				"fonts/Roboto-Regular.ttf");
		PersonaLog
				.e("PersonaLocalAuthentication", "assets initialisation ends");
		postCreateView();

	}

	private void postCreateView() {
		mEnterPinEditText.setTypeface(normalTf, Typeface.NORMAL);
		mConfirmPinEditText.setTypeface(normalTf, Typeface.NORMAL);
		// mSubmitButton.setTypeface(normalTf, Typeface.NORMAL);
		if (PersonaMainActivity.isRovaPoliciesOn) {
			mExtAdapReg = ExternalAdapterRegistrationClass
					.getInstance(mContext);
		}
		isShowAuthenticatePinUi = isPinSaved;

		updateUI(isShowAuthenticatePinUi);

		mEnterPinEditText.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				mEnterPinEditText.requestFocus();
				customKeyBoard.setVisibility(View.VISIBLE);
				customKeyBoard1.setVisibility(View.GONE);
				customKeyBoard.setEdit(mEnterPinEditText);
				return true;
			}
		});

		mConfirmPinEditText.setOnTouchListener(new View.OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				mConfirmPinEditText.requestFocus();
				customKeyBoard.setVisibility(View.GONE);
				customKeyBoard1.setVisibility(View.VISIBLE);
				customKeyBoard1.setEdit(mConfirmPinEditText);
				return true;
			}
		});

		mEnterPinEditText
				.setOnFocusChangeListener(new View.OnFocusChangeListener() {

					@Override
					public void onFocusChange(View v, boolean hasFocus) {

						if (!isShowAuthenticatePinUi)
							((InputMethodManager) mLoginActivity
									.getSystemService(Context.INPUT_METHOD_SERVICE))
									.hideSoftInputFromWindow(
											mEnterPinEditText.getWindowToken(),
											0);
						else
							((InputMethodManager) mMainActivity
									.getSystemService(Context.INPUT_METHOD_SERVICE))
									.hideSoftInputFromWindow(
											mEnterPinEditText.getWindowToken(),
											0);
						if (hasFocus) {
							customKeyBoard.setVisibility(View.VISIBLE);
							customKeyBoard1.setVisibility(View.GONE);
							customKeyBoard.setEdit(mEnterPinEditText);
							mEnterPinEditText.setTextColor(mContext
									.getResources().getColor(
											R.color.PR_TEXTBOX_NORMAL_COLOR));

						} else {
							mEnterPinEditText.setTextColor(mContext
									.getResources().getColor(
											R.color.PR_TEXTBOX_FOCUSED_COLOR));
						}
					}
				});
		mConfirmPinEditText
				.setOnFocusChangeListener(new View.OnFocusChangeListener() {

					@Override
					public void onFocusChange(View v, boolean hasFocus) {

						((InputMethodManager) mLoginActivity
								.getSystemService(Context.INPUT_METHOD_SERVICE))
								.hideSoftInputFromWindow(
										mConfirmPinEditText.getWindowToken(), 0);
						if (hasFocus) {
							mDoneButton2.setBackgroundDrawable(mContext
									.getResources().getDrawable(
											R.drawable.pr_key_done_selector));
							mDoneButtonAlpha2.setBackgroundDrawable(mContext
									.getResources().getDrawable(
											R.drawable.pr_key_done_selector));
							customKeyBoard.setVisibility(View.GONE);
							customKeyBoard1.setVisibility(View.VISIBLE);
							customKeyBoard1.setEdit(mConfirmPinEditText);
							mConfirmPinEditText.setTextColor(mContext
									.getResources().getColor(
											R.color.PR_TEXTBOX_NORMAL_COLOR));
						} else {
							mConfirmPinEditText.setTextColor(mContext
									.getResources().getColor(
											R.color.PR_TEXTBOX_FOCUSED_COLOR));
						}
					}
				});

		PersonaLog.e("PersonaLocalAuthentication", "post createview ends");
	}

	private void getEmailSettings() {

		String emailAddress;
		if (PersonaMainActivity.isRovaPoliciesOn) {

			ExternalEmailSettingsInfo extnEmailSettInfo;
			extnEmailSettInfo = mExtAdapReg.getExternalEmailSettingsInfo();

			mUsername = extnEmailSettInfo.EmpId;
			emailAddress = extnEmailSettInfo.EmailAddress;
			mPassword = extnEmailSettInfo.Password;
			mDomain = extnEmailSettInfo.Domain;
			mServer = extnEmailSettInfo.Server;
			bUserEmailEditable = extnEmailSettInfo.bAllowEditEmailSettings;
			bManualEmailConfig = extnEmailSettInfo.bSetupAutomatic;

			if (mUsername.equals(emailAddress)) {
				// in some error scenarios, username will be same as
				// emailaddress. Sync will not happen with exchange email
				// address
				mUsername = new SharedPreferences(mContext).getString(
						"trumobi_username", "");
			}
			// Since password field from ROVA SDK is null,we get it from
			// preferences. Username from sdk is exchange email id. So getting
			// from local preferences
			mPassword = new SharedPreferences(mContext).getString(
					"trumobi_password", "");

			// Manual setup turned on
			bManualEmailConfig = true;

		} else {
			mUsername = new SharedPreferences(mContext).getString(
					"trumobi_username", "");
			emailAddress = mUsername;
			mPassword = new SharedPreferences(mContext).getString(
					"trumobi_password", "");
			mDomain = PersonaConstants.mExchangeDomain;
			mServer = PersonaConstants.mExchangeServer;
			bManualEmailConfig = true;
		}

		PersonaLog.d(TAG, "mUsername: " + mUsername + "mPassword: " + mPassword
				+ "mEmailId: " + emailAddress + "exchange email id "
				+ emailAddress);
		PersonaLog.d(TAG, "mDomain:   " + mDomain + "mServer: " + mServer);
	}

	// CustomKeypad
	private void updateUI(boolean isShowAuthenticatePinUi) {

		boolean bPwdPin;

		if (PersonaMainActivity.isRovaPoliciesOn) {
			mLocalAuthtype = mExtAdapReg.getExternalPIMSettingsInfo().Passwordtype
					.getValue();
			mNumOfTrialsAllowed = mExtAdapReg.getExternalPIMSettingsInfo().nMaxPasswordTries;
			bPwdPin = mExtAdapReg.getExternalPIMSettingsInfo().bPasswordPIN;
			mExpiryInDays = mExtAdapReg.getExternalPIMSettingsInfo().PasswordExpires
					.getValue();
			if (mExpiryInDays <= 0)
				mExpiryInDays = PersonaConstants.NUMBER_OF_DAYS_TO_EXPIRE;
			mPwdLength = mExtAdapReg.getExternalPIMSettingsInfo().nPasswordLength;
			mPimAuthType = mExtAdapReg.getExternalEmailSettingsInfo().Authtype
					.getValue();

			if (mPimAuthType == PersonaConstants.mAuthTypeNone
					|| mPimAuthType == PersonaConstants.mAuthTypeCertificate) {
				mPimAuthType = PersonaConstants.mAuthTypeBasic;
			}

		} else {
			mLocalAuthtype = PersonaConstants.AUTH_TYPE_NUMERIC;
			mNumOfTrialsAllowed = PersonaConstants.NUMBER_OF_TRIALS_ALLOWED;
			bPwdPin = true;
			mExpiryInDays = PersonaConstants.NUMBER_OF_DAYS_TO_EXPIRE;
			mPwdLength = PersonaConstants.LOCAL_CREDENTIALS_LENGTH;
			if (PersonaMainActivity.isCertificateBasedAuth)
				mPimAuthType = PersonaConstants.mAuthTypeCertificate;
			else
				mPimAuthType = PersonaConstants.mAuthTypeBasic;
		}
		customKeyBoard.setVisibility(View.VISIBLE);
		customKeyBoard1.setVisibility(View.GONE);
		customKeyBoard.setEdit(mEnterPinEditText);
		if (mNumOfTrialsAllowed <= 0)
			mNumOfTrialsAllowed = 5;

		if (bPwdPin == PersonaConstants.PASSPHRASE_AUTH) {
			switch (mLocalAuthtype) {

			case PersonaConstants.AUTH_TYPE_NUMERIC:
				updateUIforPIN(PersonaConstants.AUTH_TYPE_NUMERIC);

				break;
			case PersonaConstants.AUTH_TYPE_ALPHABETS:

				setAuthenticationtype(PersonaConstants.AUTH_TYPE_ALPHABETS);
				updateUIForPwd();

				break;

			case PersonaConstants.AUTH_TYPE_ALPHANUMERIC:
				setAuthenticationtype(PersonaConstants.AUTH_TYPE_ALPHANUMERIC);
				updateUIForPwd();

				break;

			}
		} else {
			updateUIforPIN(PersonaConstants.PIN_BASED_AUTH);
		}

		if (isShowAuthenticatePinUi) {

			// mMainActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			PersonaLog
					.d(TAG,
							"########### Inside isShowAuthenticatePinUi of PersonaLocalAuthentication");
			mDoneButton.setBackgroundDrawable(mContext.getResources()
					.getDrawable(R.drawable.pr_key_done_selector));
			mDoneButtonAlpha.setBackgroundDrawable(mContext.getResources()
					.getDrawable(R.drawable.pr_key_done_selector));
			imm = (InputMethodManager) mContext
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			mMainActivity.getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

			// mSubmitButton.setVisibility(View.GONE);
			mconfirmPinTextView.setVisibility(View.GONE);
			mConfirmPinEditText.setVisibility(View.GONE);
			mSetYourPinTextView.setVisibility(View.GONE);

			prSubCaptionTextView.setVisibility(View.GONE);
			subHeaderLayout.setVisibility(View.GONE);
			minimumPINHintTextView.setVisibility(View.GONE);

		} else {

			switch (mLocalAuthtype) {

			case PersonaConstants.AUTH_TYPE_NUMERIC:
				/*
				 * imm = (InputMethodManager)mContext.getSystemService(Context.
				 * INPUT_METHOD_SERVICE); imm.getInputMethodList();
				 * imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
				 */
				mSetYourPinTextView.setText(mContext
						.getString(R.string.set_your_pin_label));
				minimumPINHintTextView.setText(mContext.getString(
						R.string.pr_set_pin_hint_label, mPwdLength));
				prSubCaptionTextView.setText(mContext
						.getString(R.string.rememberPin));
				mEnterPinTextView.setText(mContext
						.getString(R.string.enter_persona_pin));
				mconfirmPinTextView.setText(mContext
						.getString(R.string.confirm_persona_pin));
				break;
			case PersonaConstants.AUTH_TYPE_ALPHABETS:
				mSetYourPinTextView.setText(mContext
						.getString(R.string.set_your_password_label));
				minimumPINHintTextView.setText(mContext.getString(
						R.string.pr_set_password_hint_label, mPwdLength));
				prSubCaptionTextView.setText(mContext
						.getString(R.string.rememberPassword));
				mEnterPinTextView.setText(mContext
						.getString(R.string.enter_persona_password));
				mconfirmPinTextView.setText(mContext
						.getString(R.string.confirm_persona_password));
				break;
			case PersonaConstants.AUTH_TYPE_ALPHANUMERIC:
				mSetYourPinTextView.setText(mContext
						.getString(R.string.set_your_password_label));
				minimumPINHintTextView.setText(mContext.getString(
						R.string.pr_set_password_alphanumeric_hint_label,
						mPwdLength));
				prSubCaptionTextView.setText(mContext
						.getString(R.string.rememberPassword));
				mEnterPinTextView.setText(mContext
						.getString(R.string.enter_persona_password));
				mconfirmPinTextView.setText(mContext
						.getString(R.string.confirm_persona_password));
				break;

			}

		}

	}

	private void updateUIforPIN(int authType) {
		setAuthenticationtype(authType);

		/*
		 * mEnterPinEditText.setInputType(InputType.TYPE_CLASS_PHONE |
		 * InputType.TYPE_NUMBER_VARIATION_PASSWORD);
		 * mConfirmPinEditText.setInputType(InputType.TYPE_CLASS_PHONE |
		 * InputType.TYPE_TEXT_VARIATION_PASSWORD);
		 */
		// mEnterPinEditText.setHint(R.string.enter_persona_pin);
		// mConfirmPinEditText.setHint(R.string.confirm_persona_pin);

		mIncorrectCredentials = mContext.getString(R.string.incorrect_pin);
		mCredentialsNotmatching = mContext.getString(R.string.pin_not_matching);

		mPwdTypeInString = "PIN";
		customKeyBoard.setQwertyKeyBoard(false);
		customKeyBoard1.setQwertyKeyBoard(false);
	}

	private void setAuthenticationtype(int authType) {

		switch (authType) {
		case PersonaConstants.PIN_BASED_AUTH:
			at = "N1";
			break;
		case PersonaConstants.AUTH_TYPE_NUMERIC:
			at = "A3";
			break;
		case PersonaConstants.AUTH_TYPE_ALPHABETS:
			at = "A1";
			break;
		case PersonaConstants.AUTH_TYPE_ALPHANUMERIC:
			at = "A2";
			break;
		}

	}

	private void checkLoginToday() {

		if (mCommonfunctions.isConnectedToNetwork()) {
			final int millisInDay = 24 * 60 * 60 * 1000;
			long lastEmaasLoginTime = new SharedPreferences(mContext).getLong(
					"LastEmaasLogin", 0);
			// long currentTime = System.currentTimeMillis();

			long difference = getTodayMidnightTime() - lastEmaasLoginTime;
			PersonaLog.d(TAG, "********** lastlogin *********"
					+ lastEmaasLoginTime);
			PersonaLog.d(TAG, "********** today midnight time *********"
					+ todayMidNightTime);
			if (Long.signum(difference) == -1)
				difference = -difference;
			PersonaLog.d(TAG, "********** difference " + difference);
			if (difference >= millisInDay) {
				PersonaLog.d(TAG,
						"********** exceeded since last emaas login *********");

				if (PersonaMainActivity.isRovaPoliciesOn)
					mExtAdapReg.startLoginExternalAdapter();
				else {
					confirmingPinProgress.dismiss();
					if (PersonaMainActivity.isCertificateBasedAuth) {
						routineLogincheck();
					} else {
						checkPinAlreadyExpired(mContext);
						// showAppsList();
					}

				}
			}

			else {
				PersonaLog
						.d(TAG,
								"********** didnt exceeded since last emaas login skipping emaaslogin *********");
				confirmingPinProgress.dismiss();
				checkPinAlreadyExpired(mContext);
			}
		} else {
			PersonaLog
					.d(TAG,
							"********** not connected to network  skipping emaaslogin *********");

			confirmingPinProgress.dismiss();
			checkPinAlreadyExpired(mContext);
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

	private void checkPinAlreadyExpired(Context ctx) {
		if (isPinExpired(ctx)) {
			// int authenticationType = new
			// SharedPreferences(ctx).getInt("authtype",
			// PersonaConstants.AUTH_TYPE_NUMERIC);
			// int credentialsLength = new
			// SharedPreferences(ctx).getInt("credentialsLength",
			// PersonaConstants.LOCAL_CREDENTIALS_LENGTH);
			Intent pinExpiryIntent = new Intent(ctx,
					PersonaSettingsPinExpiry.class);
			pinExpiryIntent.putExtra("From", "PINAlreadyExpired");
			pinExpiryIntent.putExtra("credentials", mPin);
			// pinExpiryIntent.putExtra("authtype", authenticationType);
			// pinExpiryIntent.putExtra("credentialsLength", credentialsLength);
			ctx.startActivity(pinExpiryIntent);
			new com.TruBoxSDK.SharedPreferences(ctx).edit()
					.putBoolean("showExpiry", false).commit();
			finishCurrentActivity();
		} else {
			showAppsList();
		}

	}

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

	public Handler showLauncherCallback = new Handler() {

		public void handleMessage(android.os.Message msg) {
			PersonaLog.d(TAG, "------------ handlemessage called --------");
			switch (msg.what) {
			case 1:
				PersonaLog.d(TAG,
						"------------ handlemessage called case 1 --------");
				getEmailSettings();
				if (PersonaMainActivity.isRovaPoliciesOn) {
					mPimAuthType = mExtAdapReg.getExternalEmailSettingsInfo().Authtype
							.getValue();

					if (mPimAuthType == PersonaConstants.mAuthTypeNone
							|| mPimAuthType == PersonaConstants.mAuthTypeCertificate) {

						mPimAuthType = PersonaConstants.mAuthTypeBasic;
					}
				} else {
					mPimAuthType = PersonaConstants.mAuthTypeBasic;
				}
				showAppsList();
				break;
			case 0:
				Toast.makeText(mContext, "Reset failed", Toast.LENGTH_SHORT)
						.show();
				break;
			}

		};
	};

	private void updateUIForPwd() {
		mEnterPinEditText.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_VARIATION_PASSWORD);
		mConfirmPinEditText.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_VARIATION_PASSWORD);

		imm = (InputMethodManager) mContext
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.getInputMethodList();
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
		mIncorrectCredentials = mContext.getString(R.string.incorrect_password);
		mCredentialsNotmatching = mContext
				.getString(R.string.password_not_matching);

		mPwdTypeInString = "password";
		customKeyBoard.setQwertyKeyBoard(true);
		customKeyBoard1.setQwertyKeyBoard(true);
	}

	private void saveUserDetails() {
		TruBoxDatabase.initialiseTruBoxDatabase(mContext);
		String DB_NAME_HASHED = TruBoxDatabase.getHashValue(
				PersonaDbEncryptHelper.DB_NAME, mContext);
		mDB = mContext.getDatabasePath(DB_NAME_HASHED);

		// mDB = mContext.getDatabasePath(PersonaDbEncryptHelper.DB_NAME);
		mPersonaDB = TruBoxDatabase.openOrCreateDatabase(mDB, null);
		String tabName = PersonaDbEncryptHelper.TABLE_USERDETAILS;
		mPersonaDB.execSQL(PersonaDbEncryptHelper.CREATE_TABLE_USERDETAILS);
		String query = "SELECT * FROM " + tabName + " WHERE "
				+ PersonaDbEncryptHelper.USER_NAME + "= '" + mUsername + "'";
		Cursor c = mPersonaDB.rawQuery(query, null);

		if (c.getCount() == 0) {
			String[] args = { mUsername, mPassword };
			mPersonaDB.execSQL("INSERT OR REPLACE INTO " + tabName + " ("
					+ PersonaDbEncryptHelper.USER_NAME + ","
					+ PersonaDbEncryptHelper.PASSWORD + ")" + " VALUES (?, ?)",
					args);
			PersonaLog.d("PersonaLoginActivity", "inserting user details");
		}

		else {
			String[] args = { mPassword };
			mPersonaDB.execSQL("UPDATE " + tabName + " SET "
					+ PersonaDbEncryptHelper.PASSWORD + "= ? WHERE "
					+ PersonaDbEncryptHelper.USER_NAME + "= '" + mUsername
					+ "'", args);
			PersonaLog.d("PersonaLoginActivity", "updating user details");
		}
		c.close();
		c = mPersonaDB.rawQuery("SELECT * FROM " + tabName, null);
		if (c.moveToFirst()) {
			while (!c.isAfterLast()) {
				PersonaLog.d(
						"PersonaLoginActivity",
						"After inserting values in USERDETAILS table  "
								+ c.getString(0) + "  " + c.getString(1));
				c.moveToNext();
			}
		} else {
			PersonaLog.d("PersonaLoginActivity",
					"moveToFirst failed in userdetails table");
		}
		c.close();

	}

	public class SavePIMSettings extends AsyncTask<String, Integer, Integer> {
		ProgressDialog confirmingPinProgress;

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
			PersonaLog.d(TAG, "Exiting On PreExecute");
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			if (result != null) {
				if (confirmingPinProgress != null
						&& confirmingPinProgress.isShowing()
						&& !mLoginActivity.isFinishing())
					confirmingPinProgress.dismiss();
				PersonaLog.d(TAG, "Dismissing Progress Bar");

				Toast.makeText(
						mContext,
						mContext.getResources().getString(
								R.string.prPINSavedMessage), Toast.LENGTH_LONG)
						.show();

				new SharedPreferences(mContext).edit()
						.putBoolean("isPersonaKeySaved", true).commit();
				showAppsList();
			}
		}

		@Override
		protected Integer doInBackground(String... arg0) {

			PersonaLog.d("PersonaLoginActivity", "in savePIMSettings");

			try {
				getEmailSettings();
				saveUserDetails();
				int mPwdLocktimeInMins, mChangeHistory;
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
						+ "mChangeHistory" + mChangeHistory);

				String tabName = PersonaDbEncryptHelper.TABLE_PWDSETTINGS;
				mPersonaDB
						.execSQL(PersonaDbEncryptHelper.CREATE_TABLE_PWDSETTINGS);
				String query = "SELECT * FROM " + tabName + " WHERE "
						+ PersonaDbEncryptHelper.USERNAME_PWDSETTINGS + "= '"
						+ mUsername + "'";
				Cursor c = mPersonaDB.rawQuery(query, null);

				if (c.getCount() == 0) {
					String[] args = { mUsername,
							String.valueOf(mLocalAuthtype),
							String.valueOf(mExpiryInDays),
							String.valueOf(mPwdLocktimeInMins),
							String.valueOf(mPwdLength),
							String.valueOf(mNumOfTrialsAllowed),
							String.valueOf(mChangeHistory), mPin };
					mPersonaDB.execSQL("INSERT OR REPLACE INTO " + tabName
							+ " ("
							+ PersonaDbEncryptHelper.USERNAME_PWDSETTINGS + ","
							+ PersonaDbEncryptHelper.PWD_TYPE + ","
							+ PersonaDbEncryptHelper.PWD_EXPIRY_DURATION + ","
							+ PersonaDbEncryptHelper.PWD_LOCK_TIME + ","
							+ PersonaDbEncryptHelper.PWD_LENGTH + ","
							+ PersonaDbEncryptHelper.PWD_NUM_TRIAL + ","
							+ PersonaDbEncryptHelper.PWD_CHANGE_HISTORY + ","
							+ PersonaDbEncryptHelper.LAST_PASSWORD + ")"
							+ " VALUES (?,?,?,?,?,?,?,?)", args);
					PersonaLog.d("PersonaLoginActivity",
							"inserting pim setting details");
				}

				else {
					String[] args = { String.valueOf(mLocalAuthtype),
							String.valueOf(mExpiryInDays),
							String.valueOf(mPwdLocktimeInMins),
							String.valueOf(mPwdLength),
							String.valueOf(mNumOfTrialsAllowed),
							String.valueOf(mChangeHistory) };

					mPersonaDB.execSQL("UPDATE " + tabName + " SET "
							+ PersonaDbEncryptHelper.PWD_TYPE + "= ?,"
							+ PersonaDbEncryptHelper.PWD_EXPIRY_DURATION
							+ "= ?, " + PersonaDbEncryptHelper.PWD_LOCK_TIME
							+ "= ?," + PersonaDbEncryptHelper.PWD_LENGTH
							+ "= ?," + PersonaDbEncryptHelper.PWD_NUM_TRIAL
							+ "= ?,"
							+ PersonaDbEncryptHelper.PWD_CHANGE_HISTORY
							+ "= ?," + PersonaDbEncryptHelper.LAST_PASSWORD
							+ "= ?" +

							"WHERE "
							+ PersonaDbEncryptHelper.USERNAME_PWDSETTINGS
							+ "= '" + mUsername + "'", args);
					PersonaLog.d("PersonaLoginActivity",
							"updating pim setting details");
				}
				c.close();

				c = mPersonaDB.rawQuery("SELECT * FROM " + tabName, null);
				if (c.moveToFirst()) {
					while (!c.isAfterLast()) {
						PersonaLog.d(
								"PersonaLoginActivity",
								"After inserting values in PWDSETTINGS table  "
										+ c.getString(0) + "  "
										+ c.getString(1) + "  "
										+ c.getString(2) + "  "
										+ c.getString(3) + "  "
										+ c.getString(4) + "  "
										+ c.getString(5) + "  "
										+ c.getString(6) + "  "
										+ c.getString(7));
						c.moveToNext();
					}
				}

				else {
					PersonaLog.d("PersonaLoginActivity", "moveToFirst failed");
				}
				c.close();
				mPersonaDB.close();
				saveInRoutineCheckDB();
				isEmailAccountConfigured = new SharedPreferences(mContext)
						.getBoolean("isEmailAccountConfigured", false);
				TruBoxDatabase.setPimSettings(false);
			} catch (Exception e) {
				PersonaLog.d("PersonaLoginActivity", "in catch");
				e.printStackTrace();
			}
			PersonaLog.d("PersonaLoginActivity", "in savePIMSettings end");

			return 0;
		}

		private void saveInRoutineCheckDB() {

			String[] projection = { PersonaDatabaseProvider.COL0 };
			String where = PersonaDatabaseProvider.COL0 + " = 1";

			ContentValues values = new ContentValues();
			values.put(PersonaDatabaseProvider.COL1, "");
			values.put(PersonaDatabaseProvider.COL2, "");
			values.put(PersonaDatabaseProvider.COL3, at);
			values.put(PersonaDatabaseProvider.COL4, mNumOfTrialsAllowed);
			values.put(PersonaDatabaseProvider.COL5, "Y1");

			Cursor cursor = mContext.getContentResolver().query(
					PersonaDatabaseProvider.ROUTINE_CHECK_CONTENT_URI,
					projection, where, null, null);

			if (cursor.getCount() == 0) {
				try {
					PersonaLog.d("PersonaMainActivity", "Calling insert");
					mContext.getContentResolver().insert(
							PersonaDatabaseProvider.ROUTINE_CHECK_CONTENT_URI,
							values);
					Cursor c = mContext.getContentResolver().query(
							PersonaDatabaseProvider.ROUTINE_CHECK_CONTENT_URI,
							null, null, null, null);
					if (c.getCount() > 0) {
						c.moveToFirst();
						PersonaLog.i("Data in check table", c.getString(1));
						PersonaLog.i("Data in check table", c.getString(2));
						PersonaLog.i("Data in check table", c.getString(3));
						PersonaLog.i("Data in check table", c.getInt(4) + "");
						PersonaLog
								.i("Data in check table", c.getString(5) + "");
						c.close();
					} else {
						PersonaLog.d("PersonaLocalAuth", "row not inserted");
					}
				} catch (Exception e) {
					PersonaLog.d("PersonaLocalAuth",
							"row not inserted inside catch");
					e.printStackTrace();
				}
			} else {

				mContext.getContentResolver().update(
						PersonaDatabaseProvider.ROUTINE_CHECK_CONTENT_URI,
						values, where, null);
				Cursor c = mContext.getContentResolver().query(
						PersonaDatabaseProvider.ROUTINE_CHECK_CONTENT_URI,
						null, null, null, null);
				PersonaLog.i("Data in check table", c.getString(1));
				PersonaLog.i("Data in check table", c.getString(2));
				PersonaLog.i("Data in check table", c.getString(3));
				PersonaLog.i("Data in check table", c.getInt(4) + "");
				c.close();
			}
			cursor.close();
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
			if (imm.isActive()) {
				imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
			}

			if (isShowAuthenticatePinUi) {
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
				} else
					validateTextFields();
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
	 * if (isShowAuthenticatePinUi) { mPin =
	 * mEnterPinEditText.getText().toString(); if (mPin.equals("")) {
	 * Toast.makeText(mContext, "Please enter credentials",
	 * Toast.LENGTH_SHORT).show(); } else { authenticatePinAsyncTask
	 * authenticatePINTask = new authenticatePinAsyncTask();
	 * authenticatePINTask.execute(); }
	 * 
	 * } else { validateTextFields(); } } return false; } };
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
	 * if (isShowAuthenticatePinUi) { mPin =
	 * mEnterPinEditText.getText().toString(); if (mPin.equals("")) {
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
		else if (!mPin.equals(mConfirmPin))
			Toast.makeText(mContext, mCredentialsNotmatching,
					Toast.LENGTH_SHORT).show();
		else {

			savePin(mPin);

		}

	}

	public class authenticatePinAsyncTask extends
			AsyncTask<String, Integer, Integer> {

		@Override
		protected Integer doInBackground(String... arg0) {
			int result = authenticatePin();
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
					/*
					 * Toast.makeText(mContext, mIncorrectCredentials + ". "+
					 * attemptsLeft + " attempt(s) left",
					 * Toast.LENGTH_SHORT).show();
					 */
					Toast.makeText(mContext, mIncorrectCredentials,
							Toast.LENGTH_SHORT).show();
					mEnterPinEditText.setText("");
				}

			}

			else if (result == 0) {

				if (PersonaMainActivity.isRovaPoliciesOn) {
					checkLoginToday();
				} else {
					dismissDialog();
					showAppsList();
				}

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

		int returnValue;

		try {
			if (TruBoxDatabase.validatePassword(mPin)) {

				updatePreferences();
				if (shouldShowExpiryAlert()) {

					new SharedPreferences(mContext).edit()
							.putBoolean("showExpiry", true).commit();
					new SharedPreferences(mContext).edit()
							.putInt("daysLeft", mDaysLeft).commit();
					new SharedPreferences(mContext).edit()
							.putString("authtype", mPwdTypeInString).commit();

				}

				isEmailAccountConfigured = new SharedPreferences(mContext)
						.getBoolean("isEmailAccountConfigured", false);
				if (!isEmailAccountConfigured)
					getEmailSettings();
				returnValue = 0;
			} else {
				/*
				 * try { mContext.unregisterReceiver(LoginCompleteReceiver); }
				 * catch(Exception e) {
				 * 
				 * }
				 */
				numOfAttemptsMade++;
				attemptsLeft = inCorrectPinAttemptsAllowed - numOfAttemptsMade;
				returnValue = -2;
			}
		} catch (TruboxException e) {
			returnValue = -3;
		}

		return returnValue;

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

	private void updatePreferences() {
		new SharedPreferences(mContext).edit()
				.putBoolean("showPinOnResume", false).commit();
	}

	private void showAppsList() {

		try {
			// LocalBroadcastManager.getInstance(mContext).unregisterReceiver(LoginCompleteReceiver);
			mContext.unregisterReceiver(LoginCompleteReceiver);
		} catch (Exception e) {

		}

	/*	if (mPimAuthType == PersonaConstants.mAuthTypeNone
				|| mPimAuthType == PersonaConstants.mAuthTypeCertificate) {
			mPimAuthType = PersonaConstants.mAuthTypeBasic;
		}*/

		// PIN_SCREEN_ISSUE -->
		if (!emailNotification)
			emailNotification = new SharedPreferences(mContext).getBoolean(
					"enotificationonpin", false);
		// PIN_SCREEN_ISSUE, <--

		switch (mPimAuthType) {
		case PersonaConstants.mAuthTypeNone:
			PersonaLog.d(TAG, "-- mAuthTypeNone - from TIMER CLASS");
			break;

		case PersonaConstants.mAuthTypeBasic:
			PersonaLog.d(TAG, "-- mAuthTypeBasic - from TIMER CLASS");
			// PIM_TIMER_CHG
			if (mBndle != null && !emailNotification) {
				if (mBndle.getBoolean("fromTimer")) {
					PersonaLog.d(TAG, "-- mAuthTypeBasic - from TIMER CLASS");
					finishCurrentActivity();
				}
			}// PIM_TIMER_CHG
			else {
				if (!isEmailAccountConfigured) {

					// 290778 modified for both auto and manual setup of email
					// account configuration
					new SharedPreferences(mContext).edit()
							.putInt("AuthType", 1).commit();
					/*
					 * new SharedPreferences(mContext).edit()
					 * .putBoolean("useManualSetup", false).commit();
					 */
					new SharedPreferences(mContext)
							.edit()
							.putBoolean("useManualSetup",
									this.bManualEmailConfig).commit();
					new SharedPreferences(mContext)
							.edit()
							.putBoolean("usereditable", this.bUserEmailEditable)
							.commit();
					Intent EmailSetupIntent = new Intent(mContext,
							EmAccountSetupExchange.class);
					EmailSetupIntent.putExtra("username", mUsername);

					EmailSetupIntent.putExtra("mail_id", mUsername);

					EmailSetupIntent.putExtra("password", mPassword);
					EmailSetupIntent.putExtra("callDeviceProvisioning", true);
					EmailSetupIntent.putExtra("domain", mDomain);
					EmailSetupIntent.putExtra("server", mServer);

					EmailSetupIntent.putExtra("callDeviceProvisioning", true);
					mContext.startActivity(EmailSetupIntent);

					finishCurrentActivity();
				} else {

					// PersonaLog.d("PersonaLocalAuthentication",
					// "========= calendarNotification :"+calendarNotification);
					if (emailNotification) {
						Intent emailWelcomeActivity = new Intent(mContext,
								EmMessageList.class);
						// PIN_SCREEN_ISSUE, -->
						new SharedPreferences(mContext).edit()
								.putBoolean("enotificationonpin", false)
								.commit(); // PIN_SCREEN_ISSUE
						new SharedPreferences(mContext).edit()
								.putBoolean("notifyEmailFrmPersona", true)
								.commit();
						// PIN_SCREEN_ISSUE, <--
						mContext.startActivity(emailWelcomeActivity);
						finishCurrentActivity();
					} else if (PersonaMainActivity.securityNotification) {
						long accountId = new SharedPreferences(mContext)
								.getLong("notificatioId", -1);
						if (accountId != -1) {
							PersonaLog.d(TAG,
									"========== Calling Account Security for id: "
											+ accountId);
							// Intent emailActivity = new Intent(mContext,
							// EmailAccountSecurity.class);
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
					/*
					 * if(calendarNotification) { Intent
					 * launchCalendarmainActivity = new Intent(mContext,
					 * CalendarMainActivity.class);
					 * mContext.startActivity(launchCalendarmainActivity);
					 * finishCurrentActivity(); }
					 */
					else {
						cancelNotification(9999);
						// SYNC_FROM_PERSONA - 12/11/2013
						// Start Email Exchange service from Persona module to
						// start the sync on opening the app after entering pin.
						EmExchangeUtils.startExchangeService(mContext);
						long accountId = EmEmailContent.Account
								.getDefaultAccountId(mContext);
						EmMailService.actionNotifyNewMessages(mContext,
								accountId);
						EmEmailBroadcastProcessorService
								.cancelServiceNotification(mContext);

						Intent PersonaLauncherIntent = new Intent(mContext,
								PersonaLauncher.class);
						mContext.startActivity(PersonaLauncherIntent);
						finishCurrentActivity();
					}
				}
			}
			break;

		case PersonaConstants.mAuthTypeCertificate:
			// PIM_TIMER_CHG
			PersonaLog.d(TAG, "-- mAuthTypeCertificate - from TIMER CLASS");
			if (mBndle != null) {
				if (mBndle.getBoolean("fromTimer")) {
					PersonaLog.d(TAG, "-- mAuthTypeBasic - from TIMER CLASS");
					finishCurrentActivity();
				}
			}// PIM_TIMER_CHG
			else {

				// 290778 modified for certificate based email account
				// configuration
				new SharedPreferences(mContext).edit().putInt("AuthType", 2)
						.commit();
				PersonaLog.d("", "======== mAuthTypeCertificate: " + ""
						+ PersonaConstants.mAuthTypeCertificate);

				// Certificate
				// 290778 modified for certificate based with rova OFF
				if (PersonaMainActivity.isCertificateBasedAuth) {

					boolean isCertificatedDownloaded = new SharedPreferences(
							mContext).getBoolean("certificate_downloaded",
							false);

					if (!checkForCertificateInDb()) {
						download_certificate();
						// 290778 modified to check for the one day once login
						// service call
						// routineLogincheck();

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
				} else {
					if (!isEmailAccountConfigured) {
						Intent EmailSetupIntent = new Intent(
								mContext,
								com.cognizant.trumobi.em.customsetup.EmAccountSetupExchange.class);
						mContext.startActivity(EmailSetupIntent);
						finishCurrentActivity();
					} else {
						PersonaLog.d("PersonaLocalAuthentication",
								"========= emailNotification :"
										+ emailNotification);

						postPINValidation();
					}
				}
			}
			break;

		}

		/*
		 * if (!isEmailAccountConfigured) {
		 * 
		 * Intent EmailSetupIntent = new Intent( mContext,
		 * EmailWelcomeActivity.class); EmailSetupIntent.putExtra("username",
		 * mUsername); //
		 * EmailSetupIntent.putExtra("mail_id",mUsername+"@truboxmdmdev.com" //
		 * ); EmailSetupIntent.putExtra("mail_id", mUsername); PersonaLog.d("",
		 * "======== passs: " + mPassword);
		 * EmailSetupIntent.putExtra("password", mPassword);
		 * EmailSetupIntent.putExtra("callDeviceProvisioning", true);
		 * EmailSetupIntent.putExtra("domain", mDomain);
		 * EmailSetupIntent.putExtra("server", mServer);
		 * EmailSetupIntent.putExtra("callDeviceProvisioning", true);
		 * mContext.startActivity(EmailSetupIntent); finishCurrentActivity(); }
		 * else { Intent PersonaLauncherIntent = new Intent( <<<<<<< HEAD
		 * PersonaLocalAuthentication.this, PersonaLauncher.class);
		 * startActivity(PersonaLauncherIntent); finish(); }
		 */

	}

	// }
	/*
	 * else { Toast.makeText(PersonaLocalAuthentication.this,
	 * getResources().getString(R.string.no_enterprise_apps),
	 * Toast.LENGTH_LONG).show(); finish(); }
	 */

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
				PersonaEmail_Content_Provider.CONTENT_URI, list_pro, null,
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

	public void cancelNotification(int notificationId) {

		if (Context.NOTIFICATION_SERVICE != null) {
			String ns = Context.NOTIFICATION_SERVICE;
			NotificationManager nMgr = (NotificationManager) mContext
					.getSystemService(ns);
			nMgr.cancel(notificationId);
		}
	}

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

	private boolean shouldShowExpiryAlert() {

		long currentDate = System.currentTimeMillis();
		// mPwdSetDate = getSettingsFromDB();
		mDaysSincePwdIsSet = (int) ((currentDate - mPwdSetDate) / (1000 * 60 * 60 * 24));
		mDaysLeft = mExpiryInDays - mDaysSincePwdIsSet;
		if (mDaysLeft <= 3) {
			PersonaLog.d("pesonaLocalAuth", "daysleft" + mDaysLeft);
			return true;
		}
		return false;
	}

	/*
	 * private long getSettingsFromDB() {
	 * 
	 * 
	 * //File mDB =
	 * mMainActivity.getDatabasePath(PersonaDbEncryptHelper.DB_NAME); File mDB =
	 * mContext.getDatabasePath(PersonaDbEncryptHelper.DB_NAME); TruBoxDatabase
	 * mPersonaDB = TruBoxDatabase.openOrCreateDatabase(mDB, null); String
	 * tabName = PersonaDbEncryptHelper.TABLE_PWDSETTINGS; String query =
	 * "SELECT * FROM "+tabName+ " WHERE "+
	 * PersonaDbEncryptHelper.USERNAME_PWDSETTINGS+"= '"+mUsername+"'"; Cursor c
	 * = mPersonaDB.rawQuery(query, null); c =
	 * mPersonaDB.rawQuery("SELECT * FROM "+tabName, null); if(c.moveToFirst())
	 * { while(!c.isAfterLast()) { //mLocalAuthType = c.getInt(2);
	 * //mExpiryInDays = c.getInt(3); //mNumOfTrialsAllowed = c.getInt(6);
	 * mPwdSetDate = c.getLong(11); PersonaLog.d(TAG,
	 * "Reading values from PWDSETTINGS table and pwd set date is  "
	 * +mPwdSetDate ); c.moveToNext(); } }
	 * 
	 * c.close(); mPersonaDB.close(); return mPwdSetDate;
	 * 
	 * }
	 */

	private void savePin(String personaPin) {

		try {

			if (isPinSatisfiesRequirements(mPin, mLocalAuthtype, mPwdLength)) {
				if (TruBoxDatabase.generatePassword(mPin)) {
					postGenerationOfDBKey();
				}
			} else {
				switch (mLocalAuthtype) {

				case PersonaConstants.AUTH_TYPE_NUMERIC:
					Toast.makeText(mContext,
							mContext.getString(R.string.pr_pin_meet_criteria),
							Toast.LENGTH_LONG).show();
					break;
				case PersonaConstants.AUTH_TYPE_ALPHABETS:
					Toast.makeText(
							mContext,
							mContext.getString(R.string.pr_password_meet_criteria),
							Toast.LENGTH_LONG).show();
					break;
				case PersonaConstants.AUTH_TYPE_ALPHANUMERIC:
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
		initiateSaving.execute();

	}

	public boolean isNotOlderPassword(String password, Context context) {
		// TruBoxDatabase.setPimSettings(false);
		String mLastPassword = null, mOlderPassword = null, mOldestPassword = null;

		String DB_NAME_HASHED = TruBoxDatabase.getHashValue(
				PersonaDbEncryptHelper.DB_NAME, context);
		mDB = context.getDatabasePath(DB_NAME_HASHED);
		mUsername = new SharedPreferences(context).getString(
				"trumobi_username", "");
		// File mDB = context.getDatabasePath(PersonaDbEncryptHelper.DB_NAME);
		PersonaLog.d(TAG, "------- mDB is ------- " + mDB.toString());

		mPersonaDataBase = TruBoxDatabase.openOrCreateDatabase(mDB, null);
		if (mPersonaDataBase == null) {
			PersonaLog.d(TAG, "------- mPersonaDatabase is null------- ");
		} else {
			PersonaLog.d(TAG, "------- mPersonaDatabase is not null------- "
					+ mPersonaDataBase);
		}
		String tabName = PersonaDbEncryptHelper.TABLE_PWDSETTINGS;
		String query = "SELECT * FROM " + tabName + " WHERE "
				+ PersonaDbEncryptHelper.USERNAME_PWDSETTINGS + "= '"
				+ mUsername + "'";
		Cursor c = mPersonaDataBase.rawQuery(query, null);
		try {
			c = mPersonaDataBase.rawQuery("SELECT * FROM " + tabName, null);
			if (c.moveToFirst()) {
				mLastPassword = c.getString(8);
				mOlderPassword = c.getString(9);
				mOldestPassword = c.getString(10);
				PersonaLog.d("PersonaMainActivity",
						"Reading values from PWDSETTINGS table  "
								+ " mLastPassword " + mLastPassword
								+ " mOlderPassword " + mOlderPassword
								+ " mOldestPassword " + mOldestPassword);
			}

			c.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (mPersonaDataBase != null && mPersonaDataBase.isOpen()) {
				mPersonaDataBase.close();
			}
		}

		if (password.equals(mLastPassword) || password.equals(mOlderPassword)
				|| password.equals(mOldestPassword)) {
			return false;
		}
		saveCredential(password, mLastPassword, mOlderPassword, context);
		return true;
	}

	private void saveCredential(String password, String mLastPassword,
			String mOlderPassword, Context context) {

		String[] args = { password, mLastPassword, mOlderPassword,
				String.valueOf(System.currentTimeMillis()) };
		String DB_NAME_HASHED = TruBoxDatabase.getHashValue(
				PersonaDbEncryptHelper.DB_NAME, context);
		mDB = context.getDatabasePath(DB_NAME_HASHED);
		try {
			// File mDB =
			// context.getDatabasePath(PersonaDbEncryptHelper.DB_NAME);
			// File mDB =
			// context.getDatabasePath(PersonaDbEncryptHelper.DB_NAME);
			mPersonaDataBase = TruBoxDatabase.openOrCreateDatabase(mDB, null);
			mPersonaDataBase.execSQL("UPDATE "
					+ PersonaDbEncryptHelper.TABLE_PWDSETTINGS + " SET "
					+ PersonaDbEncryptHelper.LAST_PASSWORD + "= ?,"
					+ PersonaDbEncryptHelper.OLDER_PASSWORD + "= ?, "
					+ PersonaDbEncryptHelper.OLDEST_PASSWORD + "= ?,"
					+ PersonaDbEncryptHelper.CREATED_DATE + "= ?" +

					"WHERE " + PersonaDbEncryptHelper.USERNAME_PWDSETTINGS
					+ "= '" + mUsername + "'", args);
			PersonaLog
					.d("PersonaLoginActivity", "updating pim setting details");
		} catch (Exception e) {

		} finally {
			if (mPersonaDataBase != null && mPersonaDataBase.isOpen()) {
				mPersonaDataBase.close();
			}
		}

	}

	public boolean isPinSatisfiesRequirements(String mPin, int pwdType,
			int mPwdLength) {
		boolean result = false;
		Pattern mPattern, mPattern2;
		Matcher mMatcher, mMatcher2;
		int pinLength;

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
			boolean mIsNonAlphaCharsExist = mMatcher.find();
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

		}

		return result;
	}

	/*
	 * public void sendMDMAcknowledgement(String mComments, int
	 * pushCommandStatus) {
	 * 
	 * JSONObject holder = null; String sDeviceId =
	 * Secure.getString(getContentResolver(), Secure.ANDROID_ID);
	 * 
	 * String reg_id = new SharedPreferences(this).getString(
	 * PersonaConstants.REGISTRATION_ID, "");
	 * 
	 * try { holder = new JSONObject();
	 * 
	 * String mUid = new SharedPreferences(this).getString("UID", null);
	 * holder.put(PersonaConstants.COMMAND_UID, mUid);
	 * holder.put(PersonaConstants.COMMENTS, mComments);
	 * holder.put(PersonaConstants.DEVICE_ID, sDeviceId);
	 * holder.put(PersonaConstants.PUSH_COMMAND_STATUS, pushCommandStatus);
	 * holder.put(PersonaConstants.REGISTRATION_ID, reg_id); } catch
	 * (JSONException e) { e.printStackTrace(); }
	 * 
	 * PersonaLog.d("InstallActivity",
	 * "PersonaAPIConstants.ACKNOWLEDGEMENT_PUSH_NOTIFICATION : holder :" +
	 * holder); mSendAcknlowdge = new PersonaJSONClient(this,
	 * personaLocalAuthentication, holder, getResources().getString(
	 * R.string.loading), PersonaConstants.ACKNOWLEDGEMENT_PUSH_NOTIFICATION,
	 * false, SEND_ACK, true);
	 * 
	 * mSendAcknlowdge.execute();
	 * 
	 * }
	 */
	@Override
	public void onRemoteCallComplete(String json, int code) {

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
						&& response.message_status.contains("Success.")) {

					// String preferenceFileName=new
					// String(nBoxContentEncryption.encrypt(PersonaConstants.PERSONAPREFERENCESFILE));

					String password = response.appstore_information.user_certificate_details.certificate_pwd;
					String exchange_server = response.appstore_information.exchange_server;
					String email_domain = response.appstore_information.email_domain;
					String email_id = response.appstore_information.user_certificate_details.user_email;
					String userCertificate = response.appstore_information.user_certificate_details.user_cert;
					int certificateID = response.appstore_information.user_certificate_details.certificate_request_id;

					if (userCertificate != null) {
						byte[] convertme = Base64
								.decode(response.appstore_information.user_certificate_details.user_cert,
										Base64.DEFAULT);
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
						String COL_Email_id = "email_id";
						String COL_Domain = "domain";
						String COL_Server = "server";
						String COL_Password = "password";
						String COL_File = "file";
						String[] list_pro = { ID, COL_Email_id, COL_File,
								COL_Domain, COL_Server, COL_Password };

						Cursor cursor = mContext.getContentResolver().query(
								PersonaEmail_Content_Provider.CONTENT_URI,
								list_pro, null, null, null);
						try {
							PersonaLog.d(TAG, " " + cursor.getCount());

							if (cursor.getCount() == 0) {

								mContext.getContentResolver()
										.insert(PersonaEmail_Content_Provider.CONTENT_URI,
												values);
							} else {
								PersonaLog.d(TAG, " " + cursor.getCount()
										+ " :update: ");

								mContext.getContentResolver()
										.update(PersonaEmail_Content_Provider.CONTENT_URI,
												values, ID + "= 1", null);
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
						Intent EmailSetupIntent = new Intent(
								mContext,
								com.cognizant.trumobi.em.customsetup.EmAccountSetupExchange.class);
						mContext.startActivity(EmailSetupIntent);
						finishCurrentActivity();
					}

					// 290778 added this check if the certificate service
					// request is true but the certificate information is not
					// avalibale
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

				}

				else {

					/*
					 * Toast.makeText( mContext,
					 * mContext.getResources().getString(
					 * R.string.error_certificate_download),
					 * Toast.LENGTH_LONG).show();
					 */
					showCertificateWarningScreen(serverUnreachabelErrorCode);

				}

			} catch (Exception e) {
				e.printStackTrace();
				// finish();
				/*
				 * Toast.makeText( mContext, mContext.getResources().getString(
				 * R.string.error_in_connection), Toast.LENGTH_LONG).show();
				 */
				showCertificateWarningScreen(0);

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
						&& response.message_status.contains("Success.")) {

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
								PersonaConstants.INCORRECT_LOGIN_ATTEMPTS);
						intent.putExtra("securityaction",
								PersonaConstants.WIPE_APP);
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

							mCommonfunctions
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
						checkPinAlreadyExpired(mContext);
					}

				} else {

					/*
					 * Toast.makeText( mContext, "Invalid User Session",
					 * Toast.LENGTH_LONG).show();
					 */
					checkPinAlreadyExpired(mContext);
				}
			} catch (Exception e) {
				// assuming no network available;
				e.printStackTrace();

				PersonaLog.e(TAG, e.toString());
				checkPinAlreadyExpired(mContext);

			}
			break;

		}

	}

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

	private void showCertificateWarningScreen(int code) {
		Intent intent = new Intent(mContext,
				PersonaUserCertificateWarningActivity.class);

		PersonaLog.e(TAG, "" + code);
		intent.putExtra("warningMessageCode", code);
		mContext.startActivity(intent);
		finishCurrentActivity();
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

	@Override
	public void onRemoteCallComplete(Message json, int code) {

	}

	public BroadcastReceiver LoginCompleteReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			// Dismiss any progress dialogs here if you show it for any
			// asynccalls in startEmassLogin
			if (confirmingPinProgress != null
					&& confirmingPinProgress.isShowing())
				confirmingPinProgress.dismiss();
			// UI based on Login results commented due to SSLPeerUnverified
			// exception
			/*
			 * boolean isEmaasLoginSuccess = intent.getBooleanExtra("Result",
			 * false); if(isEmaasLoginSuccess) {
			 * 
			 * } else { Toast.makeText(context, "Emaas Login failed. Try Again",
			 * Toast.LENGTH_SHORT).show(); PersonaLog.d(TAG,
			 * "------ emaaslogin failed ---------"); }
			 */

			new SharedPreferences(mContext).edit()
					.putLong("LastEmaasLogin", todayMidNightTime).commit();
			PersonaLog.d(TAG, "------ emaaslogin success ---------");
			showAppsList();

		}
	};

}
