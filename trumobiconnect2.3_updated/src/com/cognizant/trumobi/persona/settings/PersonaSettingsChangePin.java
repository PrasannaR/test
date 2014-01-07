package com.cognizant.trumobi.persona.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.TruBoxSDK.TruBoxDatabase;
import com.TruBoxSDK.TruboxException;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobBaseEditText;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.em.provider.EmEmailContent;
import com.cognizant.trumobi.externaladapter.ExternalAdapterRegistrationClass;
import com.cognizant.trumobi.log.PersonaLog;
import com.cognizant.trumobi.persona.PersonaLocalAuthentication;
import com.cognizant.trumobi.persona.PersonaSecurityProfileUpdate;
import com.cognizant.trumobi.persona.constants.PersonaConstants;
import com.cognizant.trumobi.persona.utils.Customkeyboard;

/**
 * 
 * @author rudhra
 * 
 */
public class PersonaSettingsChangePin extends Fragment {

	String mURL = "";

	private static String TAG = PersonaSettingsChangePin.class.getName();

	private TruMobBaseEditText mCurrentPwdEdit;
	private TruMobBaseEditText mNewPwdEdit;
	private TruMobBaseEditText mConfirmNewPwdEdit;

	private Button mDoneButton, mDoneButton2, mDoneButton3;

	private TextView mTitleText, mRulesLabel;

	private String sCurrentPwd;
	private String sNewPwd;
	private String sConfirmNewPwd;
	private ExternalAdapterRegistrationClass mExtAdapReg;
	private int mLocalAuthtype, mPwdLength;
	private String mTitle, mCurrentCredentialsField, mEnterNewField,
			mConfirmNewField, mRulesText;
	private String mValuesNotMatching, inCorrectCurrentCredentials;
	InputMethodManager imm;
	Customkeyboard customKeyBoardCurrentPin, customKeyBoardNewPin,
			customKeyBoardConfirmPin;
	// From Pin Expiry Dialog
	String fromPage;
	Bundle extras;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PersonaLog.d(TAG, "===== In onCreate()=============");

		extras = getActivity().getIntent().getExtras();
		if (extras != null) {
			fromPage = extras.getString("From_Page");
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

				if (v != null) {
					((InputMethodManager) getActivity().getSystemService(
							Context.INPUT_METHOD_SERVICE))
							.hideSoftInputFromWindow(
									mCurrentPwdEdit.getWindowToken(), 0);

					if (hasFocus) {
						customKeyBoardCurrentPin.setEdit(mCurrentPwdEdit);
						customKeyBoardCurrentPin.setVisibility(View.VISIBLE);
						customKeyBoardNewPin.setVisibility(View.GONE);
						customKeyBoardConfirmPin.setVisibility(View.GONE);
						mCurrentPwdEdit.setTextColor(getActivity()
								.getResources().getColor(
										R.color.PR_TEXTBOX_NORMAL_COLOR));
					} else {
						mCurrentPwdEdit.setTextColor(getActivity()
								.getResources().getColor(
										R.color.PR_TEXTBOX_FOCUSED_COLOR));
					}
				}
			}
		});
		mNewPwdEdit.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				((InputMethodManager) getActivity().getSystemService(
						Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
						mNewPwdEdit.getWindowToken(), 0);
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

						((InputMethodManager) getActivity().getSystemService(
								Context.INPUT_METHOD_SERVICE))
								.hideSoftInputFromWindow(
										mConfirmNewPwdEdit.getWindowToken(), 0);
						if (hasFocus) {
							mDoneButton3.setBackgroundDrawable(getActivity()
									.getResources().getDrawable(
											R.drawable.pr_key_done_selector));
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
		return view;
	}

	private void initWidgets(View view) {

		mExtAdapReg = ExternalAdapterRegistrationClass
				.getInstance(getActivity());

		mLocalAuthtype = mExtAdapReg.getExternalPIMSettingsInfo().Passwordtype
				.getValue();
		mPwdLength = mExtAdapReg.getExternalPIMSettingsInfo().nPasswordLength;

		mCurrentPwdEdit = (TruMobBaseEditText) view
				.findViewById(R.id.pr_change_pin_screen_current_pin);
		// mCurrentPwdEdit.setOnEditorActionListener(currentPinListener);

		mNewPwdEdit = (TruMobBaseEditText) view
				.findViewById(R.id.pr_change_pin_screen_enterPinEditText);
		// mNewPwdEdit.setOnEditorActionListener(enterNewPinListener);

		mConfirmNewPwdEdit = (TruMobBaseEditText) view
				.findViewById(R.id.pr_change_pin_screen_confirmPinEditText);
		mConfirmNewPwdEdit.setOnEditorActionListener(confirmPinListener);

		customKeyBoardCurrentPin = (Customkeyboard) view
				.findViewById(R.id.key_edit);
		customKeyBoardNewPin = (Customkeyboard) view
				.findViewById(R.id.key_edit1);
		customKeyBoardConfirmPin = (Customkeyboard) view
				.findViewById(R.id.key_edit2);
		mTitleText = (TextView) view
				.findViewById(R.id.pr_change_pin_screen_title);
		mRulesLabel = (TextView) view
				.findViewById(R.id.pr_change_pin_screen_minimumLengthLabel);
		getUiStrings(mLocalAuthtype, mPwdLength);
		setUistrings();

		mDoneButton = (Button) customKeyBoardCurrentPin
				.findViewById(R.id.button_right);
		mDoneButton.setOnClickListener(doneOnclickListener);
		mDoneButton2 = (Button) customKeyBoardNewPin
				.findViewById(R.id.button_right);
		mDoneButton2.setOnClickListener(doneOnclickListener);
		mDoneButton3 = (Button) customKeyBoardConfirmPin
				.findViewById(R.id.button_right);
		mDoneButton3.setOnClickListener(doneOnclickListener);
		InputMethodManager imm = (InputMethodManager) getActivity()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm.isActive()) {
			imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
		}
	}

	private void setUistrings() {

		// mTitleText.setHint(mTitle);
		mRulesLabel.setHint(mRulesText);
		// mCurrentPwdEdit.setHint(mCurrentCredentialsField);
		// mNewPwdEdit.setHint(mEnterNewField);
		// mConfirmNewPwdEdit.setHint(mConfirmNewField);
	}

	private void getUiStrings(int mLocalAuthtype, int mPwdLength) {

		switch (mLocalAuthtype) {

		case 1:
			mTitle = getString(R.string.change_pin_label);
			mCurrentCredentialsField = getString(R.string.current_pin_label);
			mEnterNewField = getString(R.string.enter_new_pin_label);
			mConfirmNewField = getString(R.string.confirm_new_pin_label);
			mRulesText = "PIN must be a " + mPwdLength + "-digit number";
			mValuesNotMatching = getString(R.string.pin_not_matching);
			inCorrectCurrentCredentials = getString(R.string.incorrect_pin);
			/*
			 * mNewPwdEdit.setInputType(InputType.TYPE_CLASS_NUMBER |
			 * InputType.TYPE_NUMBER_VARIATION_PASSWORD);
			 * mConfirmNewPwdEdit.setInputType(InputType.TYPE_CLASS_NUMBER |
			 * InputType.TYPE_NUMBER_VARIATION_PASSWORD);
			 * mCurrentPwdEdit.setInputType(InputType.TYPE_CLASS_NUMBER |
			 * InputType.TYPE_NUMBER_VARIATION_PASSWORD);
			 */
			customKeyBoardCurrentPin.setEdit(mCurrentPwdEdit);
			customKeyBoardCurrentPin.setVisibility(View.VISIBLE);
			customKeyBoardCurrentPin.setQwertyKeyBoard(false);
			customKeyBoardConfirmPin.setQwertyKeyBoard(false);
			customKeyBoardNewPin.setQwertyKeyBoard(false);

			/*
			 * imm = (InputMethodManager)getActivity().getSystemService(Context.
			 * INPUT_METHOD_SERVICE); imm.getInputMethodList(); //Removed force
			 * opening as alphanumeric keypad is opened for a instant and then
			 * showing numeric keypad.
			 * imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
			 */
			break;
		case 2:
			mTitle = getString(R.string.change_password_label);
			mCurrentCredentialsField = getString(R.string.current_password_label);
			mEnterNewField = getString(R.string.enter_new_password_label);
			mConfirmNewField = getString(R.string.confirm_new_password_label);
			mRulesText = "Password should contain Alphabets only";
			mValuesNotMatching = getString(R.string.password_not_matching);
			inCorrectCurrentCredentials = getString(R.string.incorrect_password);
			/*
			 * mNewPwdEdit.setInputType(InputType.TYPE_CLASS_TEXT |
			 * InputType.TYPE_TEXT_VARIATION_PASSWORD);
			 * mConfirmNewPwdEdit.setInputType(InputType.TYPE_CLASS_TEXT |
			 * InputType.TYPE_TEXT_VARIATION_PASSWORD);
			 * mCurrentPwdEdit.setInputType(InputType.TYPE_CLASS_TEXT |
			 * InputType.TYPE_TEXT_VARIATION_PASSWORD);
			 */
			/*
			 * imm = (InputMethodManager)getActivity().getSystemService(Context.
			 * INPUT_METHOD_SERVICE); imm.getInputMethodList();
			 * imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
			 */
			break;
		case 3:
			mTitle = getString(R.string.change_password_label);
			mCurrentCredentialsField = getString(R.string.current_password_label);
			mEnterNewField = getString(R.string.enter_new_password_label);
			mConfirmNewField = getString(R.string.confirm_new_password_label);
			inCorrectCurrentCredentials = getString(R.string.incorrect_password);
			mRulesText = "Password should contain atleast an Alphabet and a Numeral";
			mValuesNotMatching = getString(R.string.password_not_matching);
			/*
			 * mNewPwdEdit.setInputType(InputType.TYPE_CLASS_TEXT |
			 * InputType.TYPE_TEXT_VARIATION_PASSWORD);
			 * mConfirmNewPwdEdit.setInputType(InputType.TYPE_CLASS_TEXT |
			 * InputType.TYPE_TEXT_VARIATION_PASSWORD);
			 * mCurrentPwdEdit.setInputType(InputType.TYPE_CLASS_TEXT |
			 * InputType.TYPE_TEXT_VARIATION_PASSWORD);
			 */
			/*
			 * imm = (InputMethodManager)getActivity().getSystemService(Context.
			 * INPUT_METHOD_SERVICE); imm.getInputMethodList();
			 * imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
			 */
			break;

		}
	}

	View.OnClickListener doneOnclickListener = new View.OnClickListener() {

		@Override
		public void onClick(View view) {

			InputMethodManager imm = (InputMethodManager) getActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);

			if (imm.isActive()) {
				imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
			}

			if (customKeyBoardCurrentPin.getVisibility() == 0)
				mNewPwdEdit.requestFocus();
			else if (customKeyBoardNewPin.getVisibility() == 0)
				mConfirmNewPwdEdit.requestFocus();
			else
				validateTextFields();

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

	private void validateTextFields() {

		sCurrentPwd = mCurrentPwdEdit.getText().toString();

		sConfirmNewPwd = mConfirmNewPwdEdit.getText().toString();

		sNewPwd = mNewPwdEdit.getText().toString();

		if (sCurrentPwd.equals("") || sConfirmNewPwd.equals("")
				|| sNewPwd.equals(""))
			Toast.makeText(getActivity(), "Fields cannot be empty",
					Toast.LENGTH_SHORT).show();
		else {
			try {
				if (!TruBoxDatabase.validatePassword(sCurrentPwd)) {
					clearTextFieldContents();

					Toast.makeText(getActivity(), inCorrectCurrentCredentials,
							Toast.LENGTH_SHORT).show();
				}
				/**
				 * 290778 modified for Current/New PIN reset with same value
				 */
				else if (sCurrentPwd.equals(sNewPwd)) {
					clearTextFieldContents();

					Toast.makeText(getActivity(),
							"Should NOT be one of last three PIN/Password",
							Toast.LENGTH_LONG).show();
				} else if (!sNewPwd.equals(sConfirmNewPwd)) {
					Toast.makeText(getActivity(), mValuesNotMatching,
							Toast.LENGTH_SHORT).show();
					clearTextFieldContents();
				}

				else {

					resetCredentials(sCurrentPwd, sNewPwd);
				}

			} catch (TruboxException e) {
			clearTextFieldContents();
//			Toast.makeText(getActivity(), "Policy Violation..!!",Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(getActivity(),
					PersonaSecurityProfileUpdate.class);
			intent.putExtra("securityevent",
					PersonaConstants.APPLICATION_HACKED);
			intent.putExtra("securityaction", PersonaConstants.WIPE_APP);
			intent.putExtra("wipedAlready", false);
			long accountId = EmEmailContent.Account.getDefaultAccountId(getActivity());
			EmEmailContent.Account account = EmEmailContent.Account.restoreAccountWithId(
					Email.getAppContext(), accountId);
			if (account != null) {
				intent.putExtra("Email_id", account.toString());
			}
			startActivity(intent);
			}
		}

	}

	private void resetCredentials(String currentPwd, String newPwd) {
		PersonaLocalAuthentication localAuthentication = new PersonaLocalAuthentication();
		PersonaLog.d("Personalog", "new pwd" + newPwd);
		boolean isSatisfiesQuality = localAuthentication
				.isPinSatisfiesRequirements(newPwd, mLocalAuthtype, mPwdLength);
		boolean isSatisfiesNotOlderPassword = localAuthentication
				.isNotOlderPassword(newPwd, getActivity());
		if (isSatisfiesNotOlderPassword) {
			if (isSatisfiesQuality) {
				PersonaLog.d("Settings", "Reset credentials");
				TruBoxDatabase.resetPassword(currentPwd, newPwd);
				/*
				 * mCurrentPwdEdit.setText(""); mConfirmNewPwdEdit.setText("");
				 * mNewPwdEdit.setText("");
				 */
				clearTextFieldContents();
				// showMessage();
				Toast.makeText(
						getActivity(),
						getActivity().getResources().getString(
								R.string.prPinResetMessage), Toast.LENGTH_LONG)
						.show();
				PersonaLog.d(TAG, "----------pin changed toast shown-------");
			} else {

				switch (mLocalAuthtype) {

				case PersonaConstants.AUTH_TYPE_NUMERIC:
					Toast.makeText(getActivity(),
							"PIN must be a " + mPwdLength + "-digit number",
							Toast.LENGTH_LONG).show();
					clearTextFieldContents();
					break;
				case PersonaConstants.AUTH_TYPE_ALPHABETS:
					Toast.makeText(getActivity(),
							"Password should contain Alphabets only",
							Toast.LENGTH_LONG).show();
					clearTextFieldContents();
					break;
				case PersonaConstants.AUTH_TYPE_ALPHANUMERIC:
					Toast.makeText(
							getActivity(),
							"Password should contain atleast an Alphabet and a Numeral",
							Toast.LENGTH_LONG).show();
					clearTextFieldContents();
					break;
				}
			}
		} else {
			Toast.makeText(getActivity(),
					"Should NOT be one of last three PIN/Password",
					Toast.LENGTH_LONG).show();
			PersonaLog
					.d(TAG,
							"----------Should NOT be one of last three PIN/Password toast shown-------");
			clearTextFieldContents();
		}
	}

	private void clearTextFieldContents() {
		customKeyBoardConfirmPin.clearEditBox();
		customKeyBoardNewPin.clearEditBox();
		customKeyBoardCurrentPin.clearEditBox();
	}

	private void showMessage() {
		Intent intent = new Intent(getActivity(),
				PersonaSecurityProfileUpdate.class);
		if (fromPage == null) {
			intent.putExtra("securityevent", PersonaConstants.PIN_RESET_SUCCESS);
			intent.putExtra("securityaction", PersonaConstants.WARN_USER);
		} else if (fromPage != null
				&& fromPage.equalsIgnoreCase("PINExpiryPage")) {
			PersonaLog.d(TAG,
					"=========== Inside Pin Expiry section and from page value is : "
							+ fromPage);
			intent.putExtra("securityevent", PersonaConstants.PIN_RESET_SUCCESS);
			intent.putExtra("securityaction",
					PersonaConstants.SHOW_LAUNCHER_SCREEN);
		}
		getActivity().startActivity(intent);
	}
	
	public PersonaSettingsChangePin() 
	{
	}
}
