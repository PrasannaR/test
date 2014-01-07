package com.cognizant.trumobi.persona.settings;


import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.TruBoxSDK.TruBoxDatabase;
import com.TruBoxSDK.TruboxException;
import com.cognizant.trumobi.PersonaLauncher;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobBaseEditText;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.em.provider.EmEmailContent;
import com.cognizant.trumobi.log.PersonaLog;
import com.cognizant.trumobi.persona.PersonaDatabaseProvider;
import com.cognizant.trumobi.persona.PersonaExternalDBHelper;
import com.cognizant.trumobi.persona.PersonaLocalAuthentication;
import com.cognizant.trumobi.persona.PersonaModelLocalAuthentication;
import com.cognizant.trumobi.persona.PersonaSecurityProfileUpdate;
import com.cognizant.trumobi.persona.constants.PersonaConstants;
import com.cognizant.trumobi.persona.utils.Customkeyboard;
import com.cognizant.trumobi.persona.utils.PersonaCommonfunctions;


public class PersonaSettingsChangePin extends Fragment {

	String mURL = "";

	private static String TAG = PersonaSettingsChangePin.class.getName();

	private TruMobBaseEditText mCurrentPwdEdit;
	private TruMobBaseEditText mNewPwdEdit;
	private TruMobBaseEditText mConfirmNewPwdEdit;
	  ImageView mTitleBarImageView;
	private Button mDoneButton, mDoneButton2, mDoneButton3,mDoneButtonAlpha,mDoneButtonAlpha2,mDoneButtonAlpha3;

	private TextView mTitleText, mRulesLabel;

	private String sCurrentPwd;
	private String sNewPwd;
	private String sConfirmNewPwd;
	private String mTitle, 
			 mRulesText,mCredentialsChangedSuccessfully,mResetFailed;
	private String mValuesNotMatching, inCorrectCurrentCredentials;
	InputMethodManager imm;
	Customkeyboard customKeyBoardCurrentPin, customKeyBoardNewPin,
			customKeyBoardConfirmPin;
	// From Pin Expiry Dialog
	String fromPage;
	int mAuthType, mPwdLength;
	Bundle extras;
	LinearLayout mSubHeader;
	TextView mSubHeaderTextView , mSubCaptionTextView ,mEnterPinTextView ,mConfirmPinTextView;
	TextView mCurrentPinTextView;
	Handler mHandler;
	PersonaCommonfunctions mCommonfunctions;
	private final int PIN_EXPIRED = 1;
	private final int SETTING_CHANGED = 2;
	private final int CORRECT_CREDENTIALS = 1;
	private final int INCORRECT_CORRECT_CREDENTIALS = 2;
	private final int NOT_SATISFIES_QUALITY= 3;
	private final int SHOULD_NOT_BE_IN_HISTORY = 4;
	private final int APPLICATION_HACKED = 5;
	private final int RESET_COMPLETE = 6;
	
	
	
	int mCalledFrom;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PersonaLog.d(TAG, "===== In onCreate()============="+getActivity());
		extras = getArguments();
		mCommonfunctions = new PersonaCommonfunctions(getActivity());
		if (extras != null) {
			fromPage = extras.getString("From");
			mAuthType = extras.getInt("authtype");
			mPwdLength = extras.getInt("credentialsLength");
			 if(extras.getInt("updatedPwdType") != 0)
				mAuthType = extras.getInt("updatedPwdType"); 
			 if(extras.getInt("updatedPwdLength") != 0)
				 mPwdLength = extras.getInt("updatedPwdLength");
			 PersonaLog.d(TAG, "-------- mauthtype is -------" + mAuthType);
		}
		else
		{
			PersonaLog.d(TAG, "-------- else mauthtype is -------" );

		}
		
		PersonaLog.d(TAG, "=========== From Page Value is : " + fromPage);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		PersonaLog.d(TAG, "===== In onActivityCreated()=============");

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {

		super.onSaveInstanceState(outState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Log.v("DetailFragment", "onCreateView()");
		PersonaLog.d(TAG, "============ In OnCreateView()=============");
		
		View view = inflater.inflate(R.layout.pr_change_pin, container, false);
		initWidgets(view);
		mCurrentPwdEdit.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub

				if(v!=null)
				{	
				((InputMethodManager) getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE))
						.hideSoftInputFromWindow(
								mCurrentPwdEdit.getWindowToken(),
								0);
			
				if (hasFocus) {
					customKeyBoardCurrentPin.setEdit(mCurrentPwdEdit);
					customKeyBoardCurrentPin.setVisibility(View.VISIBLE);
					customKeyBoardNewPin.setVisibility(View.GONE);
					customKeyBoardConfirmPin.setVisibility(View.GONE);
					mCurrentPwdEdit.setTextColor(getActivity().getResources()
							.getColor(R.color.PR_TEXTBOX_NORMAL_COLOR));
				} else {
					mCurrentPwdEdit.setTextColor(getActivity().getResources()
							.getColor(R.color.PR_TEXTBOX_FOCUSED_COLOR));
				}
				}
			}
		});
		mNewPwdEdit.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				((InputMethodManager) getActivity()
						.getSystemService(Context.INPUT_METHOD_SERVICE))
						.hideSoftInputFromWindow(
								mNewPwdEdit.getWindowToken(),
								0);
				if (hasFocus) {
					customKeyBoardNewPin.setEdit(mNewPwdEdit);
					customKeyBoardNewPin.setVisibility(View.VISIBLE);
					customKeyBoardCurrentPin.setVisibility(View.GONE);

					customKeyBoardConfirmPin.setVisibility(View.GONE);
					mNewPwdEdit.setTextColor(getActivity().getResources()
							.getColor(R.color.PR_TEXTBOX_NORMAL_COLOR));
				} else {
					mNewPwdEdit.setTextColor(getActivity().getResources()
							.getColor(R.color.PR_TEXTBOX_FOCUSED_COLOR));
				}
			}
		});
		mConfirmNewPwdEdit
				.setOnFocusChangeListener(new OnFocusChangeListener() {

					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						// TODO Auto-generated method stub
						
						((InputMethodManager) getActivity()
								.getSystemService(Context.INPUT_METHOD_SERVICE))
								.hideSoftInputFromWindow(
										mConfirmNewPwdEdit.getWindowToken(),
										0);
						if (hasFocus) {
							
							mDoneButton3.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.pr_key_done_selector));	
							mDoneButtonAlpha3.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.pr_key_done_selector));	
							customKeyBoardConfirmPin
									.setEdit(mConfirmNewPwdEdit);
							customKeyBoardConfirmPin
									.setVisibility(View.VISIBLE);
							customKeyBoardNewPin.setVisibility(View.GONE);
							customKeyBoardCurrentPin.setVisibility(View.GONE);
							mConfirmNewPwdEdit.setTextColor(getActivity()
									.getResources().getColor(
											R.color.PR_TEXTBOX_NORMAL_COLOR));
						} else {
							mConfirmNewPwdEdit.setTextColor(getActivity()
									.getResources().getColor(
											R.color.PR_TEXTBOX_FOCUSED_COLOR));
						}
					}
				});
		mCurrentPwdEdit.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				mCurrentPwdEdit.requestFocus();
				customKeyBoardCurrentPin.setEdit(mCurrentPwdEdit);
				customKeyBoardCurrentPin.setVisibility(View.VISIBLE);
				customKeyBoardNewPin.setVisibility(View.GONE);
				customKeyBoardConfirmPin.setVisibility(View.GONE);
				return true;
			}
		});
		mNewPwdEdit.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				mNewPwdEdit.requestFocus();
				customKeyBoardNewPin.setEdit(mNewPwdEdit);
				customKeyBoardNewPin.setVisibility(View.VISIBLE);
				customKeyBoardCurrentPin.setVisibility(View.GONE);

				customKeyBoardConfirmPin.setVisibility(View.GONE);
				return true;
			}
		});
		mConfirmNewPwdEdit.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				mConfirmNewPwdEdit.requestFocus();
				customKeyBoardConfirmPin.setEdit(mConfirmNewPwdEdit);
				customKeyBoardConfirmPin.setVisibility(View.VISIBLE);
				customKeyBoardNewPin.setVisibility(View.GONE);
				customKeyBoardCurrentPin.setVisibility(View.GONE);

				return true;
			}
		});
		
		mTitleBarImageView.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				goTolauncherHome();
			}
		});
		return view;
	}

	private void initWidgets(View view) {

			mSubHeader = (LinearLayout)view.findViewById(R.id.pr_change_pin_screen_subheader);
		  mSubHeaderTextView = (TextView)view.findViewById(R.id.pr_change_pin_screen_subheader_textview);
		  mSubCaptionTextView = (TextView)view.findViewById(R.id.pr_change_pin_screen_subheader_subcaption);
		  mEnterPinTextView = (TextView)view.findViewById(R.id.pr_change_pin_screen_enter_pin_textview);
		  mConfirmPinTextView = (TextView)view.findViewById(R.id.pr_change_pin_screen_confirm_pin_textview);
		  mCurrentPinTextView = (TextView)view.findViewById(R.id.pr_change_pin_screen_current_pin_textview);
		  mCurrentPwdEdit = (TruMobBaseEditText) view.findViewById(R.id.pr_change_pin_screen_current_pin);
		  RelativeLayout mTitleBar = (RelativeLayout) view.findViewById(R.id.pr_change_pin_screen_titlebar);
		  mTitleText = (TextView) view.findViewById(R.id.pr_change_pin_screen_title);
		  mRulesLabel = (TextView) view.findViewById(R.id.pr_change_pin_screen_minimumLengthLabel);
		   mTitleBarImageView = (ImageView)view.findViewById(R.id.pr_change_pin_title_bar_icon);
		  if(fromPage != null) {
			  
			 if(fromPage.equals("PINAlreadyExpired"))
				 mCalledFrom = PIN_EXPIRED; 
			 if(fromPage.equals("PimSettingsUpdated"))
				 mCalledFrom = SETTING_CHANGED; 
			mSubHeader.setVisibility(View.VISIBLE);
			mCurrentPwdEdit.setVisibility(View.GONE);
			mCurrentPinTextView.setVisibility(View.GONE);
			mTitleBar.setVisibility(View.VISIBLE);
			mTitleText.setVisibility(View.VISIBLE);
			mTitleBarImageView.setVisibility(View.INVISIBLE);
			mTitleText.setText(mTitle);
			mTitleText.setGravity(Gravity.LEFT);
			updateView(mCalledFrom);
		}
		  
		
		/*mExtAdapReg = ExternalAdapterRegistrationClass
				.getInstance(getActivity());

		mLocalAuthtype = mExtAdapReg.getExternalPIMSettingsInfo().Passwordtype
				.getValue();
		mPwdLength = mExtAdapReg.getExternalPIMSettingsInfo().nPasswordLength;*/

		
		// mCurrentPwdEdit.setOnEditorActionListener(currentPinListener);

		mNewPwdEdit = (TruMobBaseEditText) view.findViewById(R.id.pr_change_pin_screen_enterPinEditText);
		// mNewPwdEdit.setOnEditorActionListener(enterNewPinListener);

		mConfirmNewPwdEdit = (TruMobBaseEditText) view.findViewById(R.id.pr_change_pin_screen_confirmPinEditText);
		mConfirmNewPwdEdit.setOnEditorActionListener(confirmPinListener);
	
		customKeyBoardCurrentPin = (Customkeyboard) view.findViewById(R.id.key_edit);
		customKeyBoardNewPin = (Customkeyboard) view.findViewById(R.id.key_edit1);
		customKeyBoardConfirmPin = (Customkeyboard) view.findViewById(R.id.key_edit2);
		getUiStrings(mAuthType, mPwdLength);
		setUistrings();

		mDoneButton = (Button) customKeyBoardCurrentPin	.findViewById(R.id.button_right);
		mDoneButton.setOnClickListener(doneOnclickListener);
		mDoneButton2 = (Button) customKeyBoardNewPin.findViewById(R.id.button_right);
		mDoneButton2.setOnClickListener(doneOnclickListener);
		mDoneButton3 = (Button) customKeyBoardConfirmPin.findViewById(R.id.button_right);
		mDoneButton3.setOnClickListener(doneOnclickListener);
		
		mDoneButtonAlpha = (Button) customKeyBoardCurrentPin	.findViewById(R.id.button47);
		
		mDoneButtonAlpha.setOnClickListener(doneOnclickListener);
		mDoneButtonAlpha2 = (Button) customKeyBoardNewPin.findViewById(R.id.button47);
		mDoneButtonAlpha2.setOnClickListener(doneOnclickListener);
		mDoneButtonAlpha3 = (Button) customKeyBoardConfirmPin.findViewById(R.id.button47);
		mDoneButtonAlpha3.setOnClickListener(doneOnclickListener);
		
		InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE); 
		if (imm.isActive()) {
			imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
		}
		/*new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				PersonaLog.d(TAG, "----- in run -------");*/
				//PersonaLocalAuthentication personaLocalAuth = new PersonaLocalAuthentication();
			//	personaLocalAuth.showAppsList();
			/*	
			}
		}, 2000);*/
	}

	private void updateView(int mCalledFrom) {
		switch(mCalledFrom) {
		case PIN_EXPIRED:
			updateViewForPINExpired();
			break;
		case SETTING_CHANGED:
			updateViewForSettingsChanged();
			break;
		}
		
	}
	private void updateViewForPINExpired() {
	
		 switch(mAuthType) {
		 	case PersonaConstants.AUTH_TYPE_NUMERIC:
		 		mSubHeaderTextView.setText(getString(R.string.pin_expired));
		 		mSubCaptionTextView.setText(getString(R.string.pin_expired_subcaption));
		 		break;
		 	default:
		 		mSubHeaderTextView.setText(getString(R.string.password_expired));
	 			mSubCaptionTextView.setText(getString(R.string.password_expired_subcaption));
	 			break;
		 }
		
	}
	
	private void updateViewForSettingsChanged() {
		 switch(mAuthType) {
		 	case PersonaConstants.AUTH_TYPE_NUMERIC:
		 		mSubHeaderTextView.setText(getString(R.string.settings_changed));
		 		mSubCaptionTextView.setText(getString(R.string.pin_expired_subcaption));
		 		break;
		 	default:
		 		mSubHeaderTextView.setText(getString(R.string.settings_changed));
	 			mSubCaptionTextView.setText(getString(R.string.password_expired_subcaption));
	 			break;
		 }
		
	}


	private void setUistrings() {
		
		if( getActivity().getResources().getInteger(R.integer.deviceType) == 0)
			mTitleText.setText(mTitle);
		mRulesLabel.setText(mRulesText);
		// mCurrentPwdEdit.setHint(mCurrentCredentialsField);
		// mNewPwdEdit.setHint(mEnterNewField);
		// mConfirmNewPwdEdit.setHint(mConfirmNewField);
	}

	private void getUiStrings(int mLocalAuthtype, int mPwdLength) {

		
		switch (mLocalAuthtype) {

		case PersonaConstants.AUTH_TYPE_NUMERIC:
			mTitle = getString(R.string.change_pin_label);
		//	mCurrentCredentialsField = getString(R.string.current_pin_label);
		//	mEnterNewField = getString(R.string.enter_new_pin_label);
		//	mConfirmNewField = getString(R.string.confirm_new_pin_label);
			mRulesText = getString(R.string.pr_set_pin_hint_label, mPwdLength);
			mValuesNotMatching = getString(R.string.pin_not_matching);
			inCorrectCurrentCredentials = getString(R.string.incorrect_pin);
			mCredentialsChangedSuccessfully = getString(R.string.prPinResetMessage);
			mResetFailed = getString(R.string.pr_pin_reset_failed);
			/*mNewPwdEdit.setInputType(InputType.TYPE_CLASS_NUMBER
					| InputType.TYPE_NUMBER_VARIATION_PASSWORD);
			mConfirmNewPwdEdit.setInputType(InputType.TYPE_CLASS_NUMBER
					| InputType.TYPE_NUMBER_VARIATION_PASSWORD);
			mCurrentPwdEdit.setInputType(InputType.TYPE_CLASS_NUMBER
					| InputType.TYPE_NUMBER_VARIATION_PASSWORD);*/
			customKeyBoardCurrentPin.setEdit(mCurrentPwdEdit);
			customKeyBoardCurrentPin.setVisibility(View.VISIBLE);
			customKeyBoardCurrentPin.setQwertyKeyBoard(false);
			customKeyBoardConfirmPin.setQwertyKeyBoard(false);
			customKeyBoardNewPin.setQwertyKeyBoard(false);
			mEnterPinTextView.setText(getString(R.string.enter_persona_pin));
			mCurrentPinTextView.setText(getString(R.string.current_pin_label));
			mConfirmPinTextView.setText(getString(R.string.confirm_persona_pin));
			
			/*
			 * imm = (InputMethodManager)getActivity().getSystemService(Context.
			 * INPUT_METHOD_SERVICE); imm.getInputMethodList(); //Removed force
			 * opening as alphanumeric keypad is opened for a instant and then
			 * showing numeric keypad.
			 * imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
			 */
			break;
		case PersonaConstants.AUTH_TYPE_ALPHABETS:
			getUistringsForPassword();
			mRulesText = getString(R.string.pr_set_password_hint_label, mPwdLength);
			
			
		/*	mNewPwdEdit.setInputType(InputType.TYPE_CLASS_TEXT
					| InputType.TYPE_TEXT_VARIATION_PASSWORD);
			mConfirmNewPwdEdit.setInputType(InputType.TYPE_CLASS_TEXT
					| InputType.TYPE_TEXT_VARIATION_PASSWORD);
			mCurrentPwdEdit.setInputType(InputType.TYPE_CLASS_TEXT
					| InputType.TYPE_TEXT_VARIATION_PASSWORD);*/
			/*
			 * imm = (InputMethodManager)getActivity().getSystemService(Context.
			 * INPUT_METHOD_SERVICE); imm.getInputMethodList();
			 * imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
			 * 
			 * 
			 */
			break;
		case PersonaConstants.AUTH_TYPE_ALPHANUMERIC:
			getUistringsForPassword();
			mRulesText = getString(R.string.pr_set_password_alphanumeric_hint_label, mPwdLength);
			
		/*	mNewPwdEdit.setInputType(InputType.TYPE_CLASS_TEXT
					| InputType.TYPE_TEXT_VARIATION_PASSWORD);
			mConfirmNewPwdEdit.setInputType(InputType.TYPE_CLASS_TEXT
					| InputType.TYPE_TEXT_VARIATION_PASSWORD);
			mCurrentPwdEdit.setInputType(InputType.TYPE_CLASS_TEXT
					| InputType.TYPE_TEXT_VARIATION_PASSWORD);*/
			/*
			 * imm = (InputMethodManager)getActivity().getSystemService(Context.
			 * INPUT_METHOD_SERVICE); imm.getInputMethodList();
			 * imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
			 */
			break;
		case PersonaConstants.AUTH_TYPE_PASSWORD_ALLOW_NUMERALS:
			getUistringsForPassword();
			mRulesText = getString(R.string.pr_set_password_allow_numerals_hint_label,mPwdLength);
			break;

		}
	}

	private void getUistringsForPassword() {
		mTitle = getString(R.string.change_password_label);
	//	mCurrentCredentialsField = getString(R.string.current_password_label);
	//	mEnterNewField = getString(R.string.enter_new_password_label);
	//	mConfirmNewField = getString(R.string.confirm_new_password_label);
		mValuesNotMatching = getString(R.string.password_not_matching);
		inCorrectCurrentCredentials = getString(R.string.incorrect_password);
		mCredentialsChangedSuccessfully = getString(R.string.prPasswordResetMessage);
		customKeyBoardCurrentPin.setQwertyKeyBoard(true);
		customKeyBoardConfirmPin.setQwertyKeyBoard(true);
		customKeyBoardNewPin.setQwertyKeyBoard(true);
		mEnterPinTextView.setText(getString(R.string.enter_persona_password));
		mCurrentPinTextView.setText(getString(R.string.current_password_label));
		mConfirmPinTextView.setText(getString(R.string.confirm_persona_password));
		mResetFailed = getString(R.string.pr_password_reset_failed);
	}
	View.OnClickListener doneOnclickListener = new View.OnClickListener() {

		@Override
		public void onClick(View view) {

			InputMethodManager imm = (InputMethodManager) getActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);

			if (imm.isActive()) {
				imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
			}
			
			if(customKeyBoardCurrentPin.getVisibility()==0)
			mNewPwdEdit.requestFocus();
			else if(customKeyBoardNewPin.getVisibility()==0)
				mConfirmNewPwdEdit.requestFocus();
			else {
				if(fromPage!=null && fromPage.equals("PINAlreadyExpired") ||
						fromPage!=null && fromPage.equals("PimSettingsUpdated")	)
					postPinExpiredOrSettingsUpdatedActions();
				else 
					validateTextFields();
			}

		}

		

	};

	TextView.OnEditorActionListener confirmPinListener = new TextView.OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView textView, int actionId,
				KeyEvent event) {

			if (actionId == EditorInfo.IME_ACTION_DONE
					|| event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
				validateTextFields();
			}
			return false;
		}
	};

	private void postPinExpiredOrSettingsUpdatedActions() {
		try {
		getTextFromFields();
		sCurrentPwd = getArguments().getString("credentials");
		PersonaLog.e(TAG, "--------- sCurrentPwd -------" + sCurrentPwd);
		if (sConfirmNewPwd.equals("")|| sNewPwd.equals("")) {
			Toast.makeText(getActivity(), getString(R.string.fields_cannot_be_empty),
					Toast.LENGTH_SHORT).show();
		}
		else if (!sNewPwd.equals(sConfirmNewPwd)){
			clearTextFieldContents();
			Toast.makeText(getActivity(), mValuesNotMatching,
					Toast.LENGTH_SHORT).show();
		}
		else if (sCurrentPwd.equals(sNewPwd)) {
			clearTextFieldContents();
			Toast.makeText(getActivity(),
					getString(R.string.last_three_pin_password_warning),
					Toast.LENGTH_LONG).show();
		} 
		
		else {
			ResetPinOrPassword reset = new ResetPinOrPassword(false);
			reset.execute(sCurrentPwd, sNewPwd);
		}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void getTextFromFields() {
		
		sCurrentPwd = mCurrentPwdEdit.getText().toString();
		sConfirmNewPwd = mConfirmNewPwdEdit.getText().toString();
		sNewPwd = mNewPwdEdit.getText().toString();
		
	}

	private void validateTextFields() {

		getTextFromFields();
		
		if (sCurrentPwd.equals("") || sConfirmNewPwd.equals("")
				|| sNewPwd.equals("")){
			Toast.makeText(getActivity(), getString(R.string.fields_cannot_be_empty),
					Toast.LENGTH_SHORT).show();
		}
		else {
			
				if (!sNewPwd.equals(sConfirmNewPwd)){
					clearTextFieldContents();
					Toast.makeText(getActivity(), mValuesNotMatching,
							Toast.LENGTH_SHORT).show();
				}
				
				else if (sCurrentPwd.equals(sNewPwd)) {
					clearTextFieldContents();
					Toast.makeText(getActivity(),
							getString(R.string.last_three_pin_password_warning),
							Toast.LENGTH_LONG).show();
				} 
				
				else {
					ResetPinOrPassword reset = new ResetPinOrPassword(true);
					reset.execute(sCurrentPwd, sNewPwd);
				}
			 
		}
	}
		
	

	public class ResetPinOrPassword extends AsyncTask<String, Integer, Integer> {

		boolean resetStatus;
		boolean callingFromChangePinScreen;
		int result = -1;
		ProgressDialog resettingCredentials;
		
		@Override
		protected Integer doInBackground(String... params) {
			String currentPwd = params[0];
			String newPwd = params[1];
			if(!callingFromChangePinScreen) {
				result = CORRECT_CREDENTIALS;
			}
			else {
			result = validatePassword(currentPwd);
			}
			if(result == CORRECT_CREDENTIALS) {
				boolean isSatisfiesQuality = mCommonfunctions
						.isPinSatisfiesRequirements(newPwd, mAuthType, mPwdLength);
					if (isSatisfiesQuality) {
						PersonaModelLocalAuthentication modelLocalAuthentication = new PersonaModelLocalAuthentication(getActivity());
						Bundle bundle = new Bundle();
						bundle.putString("pwd", newPwd);
						bundle.putString("presentPwdToBeReplaced",currentPwd);
						boolean isSatisfiesNotOlderPassword = modelLocalAuthentication
								.isNotOlderPassword(bundle);
						if (isSatisfiesNotOlderPassword) {
							
							resetStatus = TruBoxDatabase.resetPassword(currentPwd, newPwd);
							PersonaLog.d(TAG,"-------------- password reset status ------------" + resetStatus);
							if(resetStatus && !callingFromChangePinScreen) {
								updateExternalDB();
								updatePimSettingsDB(mAuthType,mPwdLength);
							}
							updatePasswordHistoryTable(currentPwd);
							return RESET_COMPLETE;
							
						}
						else {
							return SHOULD_NOT_BE_IN_HISTORY;
						}
					}
					else {
						return NOT_SATISFIES_QUALITY;
					}
			}
			else {
				return result;
			}
			
		}
		
		

		@Override
		protected void onPostExecute(Integer result) {
			
			super.onPostExecute(result);
			
			if(resettingCredentials != null)
				resettingCredentials.dismiss();
			
			clearTextFieldContents();
			
			switch(result) {
			case INCORRECT_CORRECT_CREDENTIALS:
				Toast.makeText(getActivity(), inCorrectCurrentCredentials,Toast.LENGTH_SHORT).show();
				break;
			case APPLICATION_HACKED:
				postDetectionOfApplicationHack();
				break;
			case NOT_SATISFIES_QUALITY:
				showToastForInSufficientPasswordQuality();
				break;
			case SHOULD_NOT_BE_IN_HISTORY:
				Toast.makeText(getActivity(),
						getString(R.string.last_three_pin_password_warning),
						Toast.LENGTH_LONG).show();
				break;
			case RESET_COMPLETE:
				PersonaLog.d(TAG," ---------- in case  RESET_COMPLETE " + resetStatus);
				showNextScreen(resetStatus);
				break;
			}
		}
		
		public ResetPinOrPassword(boolean callingFromChangePinScreen) {
			this.callingFromChangePinScreen = callingFromChangePinScreen;
		}
		
		
		@Override
		protected void onPreExecute() {
			
			super.onPreExecute();
			PersonaLog.d(TAG, "-------- ResetPinOrPassword onPreExecute ---------- ");
			resettingCredentials = new ProgressDialog(getActivity(),
					R.style.PersonaNewDialog);
			resettingCredentials.setCancelable(false);
			resettingCredentials.setMessage(getActivity().getResources().getString(R.string.resetting_credentials));
			resettingCredentials.show();
		}
	}
	
	private void showToastForInSufficientPasswordQuality() {
		switch (mAuthType) {

		case PersonaConstants.AUTH_TYPE_NUMERIC:
			Toast.makeText(getActivity(),
					getString(R.string.pr_pin_meet_criteria),
					Toast.LENGTH_LONG).show();
			break;
		case PersonaConstants.AUTH_TYPE_ALPHABETS:
			
		case PersonaConstants.AUTH_TYPE_ALPHANUMERIC:
			
		case PersonaConstants.AUTH_TYPE_PASSWORD_ALLOW_NUMERALS:
			Toast.makeText(getActivity(),
					getString(R.string.pr_password_meet_criteria),
					Toast.LENGTH_LONG).show();
			break;
		}
	}
	
	public void updatePimSettingsDB(int mAuthType, int mPwdLength) {
		PersonaModelLocalAuthentication modelAuthentication = new PersonaModelLocalAuthentication(getActivity());
		Bundle bundle = new Bundle();
		bundle.putInt("localauthtype", mAuthType);
		bundle.putInt("pwdlength", mPwdLength);
		modelAuthentication.updateCurrentPimSettings(bundle);
	}

	private void updatePasswordHistoryTable(String oldPassword) {
		PersonaModelLocalAuthentication modelAuthentication = new PersonaModelLocalAuthentication(getActivity());
		modelAuthentication.updatePasswordHistoryTable(oldPassword);
	}
	
	private void updateExternalDB() {
		String authType = mCommonfunctions.setAuthenticationtype(mAuthType);
		String[] projection = { PersonaExternalDBHelper.COL0 };
		String where = PersonaExternalDBHelper.COL0 + " = ?";
		String[] selectionArgs = {"1"};
		ContentValues values = new ContentValues();
		values.put(PersonaExternalDBHelper.COL3, authType);
		Cursor cursor = getActivity().getContentResolver().query(
				PersonaDatabaseProvider.ROUTINE_CHECK_CONTENT_URI,
				projection, where, selectionArgs, null);
		if (cursor.getCount() != 0) {
			
			getActivity().getContentResolver().update(PersonaDatabaseProvider.ROUTINE_CHECK_CONTENT_URI,
					values, where, selectionArgs);
		} 
		if(cursor!=null)
		cursor.close();
	}

	private int validatePassword(String currentPwd) {
		int result = -1;
		try {
		if (TruBoxDatabase.validatePassword(currentPwd)) {
			result = CORRECT_CREDENTIALS;
		}
		else {
			result = INCORRECT_CORRECT_CREDENTIALS;	
		}
		}
		catch (TruboxException e) {
			clearTextFieldContents();
			result = APPLICATION_HACKED;
		}catch (Exception e) {
			clearTextFieldContents();
			result = APPLICATION_HACKED;
			e.printStackTrace();
		}
		return result;
		
	}
	
	
	private void postDetectionOfApplicationHack() {
		Intent intent = new Intent(getActivity(),
				PersonaSecurityProfileUpdate.class);
		intent.putExtra("securityevent",
				PersonaConstants.APPLICATION_HACKED);
		intent.putExtra("securityaction", PersonaConstants.WIPE_APP);
		intent.putExtra("wipedAlready", false);
		long accountId = EmEmailContent.Account
				.getDefaultAccountId(Email.getAppContext());
		EmEmailContent.Account account = EmEmailContent.Account
				.restoreAccountWithId(Email.getAppContext(), accountId);
		if (account != null) {
			intent.putExtra("Email_id", account.toString());
		}
		startActivity(intent);
	}
	

	private void clearTextFieldContents(){
		try {
		customKeyBoardConfirmPin.clearEditBox();
		customKeyBoardNewPin.clearEditBox();
		
		customKeyBoardCurrentPin.clearEditBox();
		}
		catch(Exception e) {
			e.printStackTrace();
			//in case current pin doesnot exist in pinexpired and settings changed cases
		}
	}
	private void showNextScreen(boolean result) {
		try {
		int resetSuccess;
		if(result) {
			PersonaLog.e(TAG, "------- showNextScreen result ---------- " + result);
			resetSuccess = 1 ;
			Toast.makeText(getActivity(),mCredentialsChangedSuccessfully, 
					Toast.LENGTH_LONG).show();
			
		}
		else {
			resetSuccess = 0;
			Toast.makeText(getActivity(), mResetFailed, Toast.LENGTH_SHORT)
					.show();
		}
		if (fromPage != null && fromPage.equalsIgnoreCase("PINAlreadyExpired")) {
			PersonaLog.e(TAG, "------- mHandler while sendingmessage---------- " + mHandler);
			getActivity().finish();
			mHandler.sendEmptyMessage(resetSuccess);
		
		}
		else if(fromPage != null && fromPage.equalsIgnoreCase("PimSettingsUpdated")) {
			PersonaLog.e(TAG, "------- PimSettingsUpdated mHandler while sendingmessage---------- " + mHandler);
			getActivity().finish();
			mHandler.sendEmptyMessage(resetSuccess);
		}
		}
		catch(Exception e) {
		e.printStackTrace();	
		}
	}
	
	 public PersonaSettingsChangePin(Handler callback) {
		mHandler = callback;
		PersonaLog.e(TAG, "------- mHandler ---------- " + mHandler);
	}
	
	public PersonaSettingsChangePin() 
	{
	}
	
	 
	private void goTolauncherHome(){
		Intent intent =new Intent(getActivity(),PersonaLauncher.class);
		startActivity(intent);
	}
}
