package com.cognizant.trumobi.persona;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.log.PersonaLog;
import com.cognizant.trumobi.persona.constants.PersonaCertfifcateAuthConstants;
import com.cognizant.trumobi.persona.constants.PersonaConstants;
import com.cognizant.trumobi.persona.settings.PersonaSettings;
import com.cognizant.trumobi.persona.settings.PersonaSettingsPinExpiry;
import com.cognizant.trumobi.persona.utils.PersonaCommonfunctions;

public class PersonaSecurityUpdateDialogFragment extends DialogFragment {

	int mEvent, mAction;
	View mDialogView;
	TextView mMessage, mTitle;
	Button mRightButton, mLeftButton;
	ImageView mTitleImage;
	private SharedPreferences shared;
	Typeface mRobotoRegular;
	static final String TAG = PersonaSecurityUpdateDialogFragment.class.getName();
	private DialogCancelListener mListener;

	public PersonaSecurityUpdateDialogFragment() {

	}

	public PersonaSecurityUpdateDialogFragment(int event, int action) {
		this.mEvent = event;
		this.mAction = action;
	}

	public static interface DialogCancelListener {
		public void onDialogCancelled();
		public void onShowNextScreen(int showNextScreen);
		public void onRequestCertificate();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setCancelable(false);
		mRobotoRegular = Typeface.createFromAsset(getActivity().getAssets(),
				"fonts/Roboto-Regular.ttf");
		// setStyle(DialogFragment.STYLE_NO_TITLE,
		// android.R.style.Theme_NoTitleBar_Fullscreen);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		mDialogView = inflater.inflate(
				R.layout.pr_security_profile_dialog_fragment, container);
	
		mTitle = (TextView) mDialogView
				.findViewById(R.id.personaDialogFragmentTitle);
		mTitle.setTypeface(mRobotoRegular, Typeface.BOLD);
		mTitleImage = (ImageView) mDialogView
				.findViewById(R.id.personaDialogFragmentTitleImage);
		
		mMessage = (TextView) mDialogView
				.findViewById(R.id.personaDialogFragmentMessage);
		mMessage.setTypeface(mRobotoRegular, Typeface.NORMAL);
		mRightButton = (Button) mDialogView
				.findViewById(R.id.personaDialogFragmentRightButtonText);
		mRightButton.setTypeface(mRobotoRegular, Typeface.BOLD);
		mLeftButton = (Button) mDialogView
				.findViewById(R.id.personaDialogFragmentLeftButtonText);
		mLeftButton.setTypeface(mRobotoRegular, Typeface.BOLD);

		updateView();
		return mDialogView;
		// return super.onCreateView(inflater, container, savedInstanceState);

	}

	private void updateView() {

		switch (mEvent) {

		case PersonaConstants.SIMCARD_REMOVAL_EVENT:
			updateViewForSimRemovalEvent(mAction);
			break;
		case PersonaConstants.IME_NOT_WHITELISTED:
			updateViewForIMEBlackListed(mAction);
			break;
		case PersonaConstants.DEVICE_ROOTED_EVENT:
			updateViewForDeviceRooted(mAction);
			break;
		case PersonaConstants.INCORRECT_LOGIN_ATTEMPTS:
			updateViewForIncorrectLoginAttempts(mAction);
			break;
		case PersonaConstants.PIN_RESET_SUCCESS:
			updateViewForPinResetSuccess(mAction);
			break;
		case PersonaConstants.SHOW_PIN_EXPIRY:
			updateViewForShowPinExpiry(mAction);
			break;
		case PersonaConstants.APPLICATION_HACKED:
			updateViewForApplicationHacked(mAction);
			break;

			
		case PersonaCertfifcateAuthConstants.CERT_FAILURE_EVENT:
			updateViewForCertificateFailureEvent(mAction);
			break;

		case PersonaConstants.BUGSENSE_ON_ALERT:
			PersonaLog.d(TAG,"***** BUGSENSE_ON_ALERT ****");
			updateViewForBugSenseOn();

			break;
		}
		

	}
	
private void updateViewForShowPinExpiry(int action) {
	shared = getActivity().getSharedPreferences(PersonaConstants.PERSONAPREFERENCESFILE, 0);
	String authTypeInString;
	int daysLeft =	new com.TruBoxSDK.SharedPreferences(getActivity()).getInt("daysLeft", 3);
	int authtype = new com.TruBoxSDK.SharedPreferences(getActivity()).getInt("authtype", PersonaConstants.AUTH_TYPE_NUMERIC);
	/*PersonaLog.d(TAG, "#################### No of days left : "+daysLeft);
	PersonaLog.d(TAG, "#################### Password Type is : "+authtype);*/
	PersonaCommonfunctions mCommonFunctions = new PersonaCommonfunctions(getActivity());
	authTypeInString = mCommonFunctions.getAuthtypeInString(authtype);
		switch (action) {
		case PersonaConstants.DO_NOTHING:
			break;
		case PersonaConstants.SHOW_PIN_EXPIRY:
			mTitleImage.setBackgroundResource(R.drawable.pr_warn_user);
			mTitle.setText(getString(R.string.prWarning));
			mMessage.setText(getString(R.string.prCredentialsEexpiredWarning,authTypeInString,daysLeft));
			mLeftButton.setText(getString(R.string.prSetNowString,authTypeInString));
			mRightButton.setText(getString(R.string.prSetLaterString,authTypeInString));
			initializeClickListenersForPINChange();
			break;
		}
	}
	
	
	private void updateViewForBugSenseOn(){
		PersonaLog.d(TAG,"**** updateViewForBugSenseOn **** ");
		mTitle.setText(R.string.prBugSense);
		mMessage.setText(R.string.prBugSenseOnAlertMessage);
		mRightButton.setText(R.string.prYesButton);
		mLeftButton.setText(R.string.prNoButton);
		initializeClickListenersForBugSense();
		
	}


	private void updateViewForPinResetSuccess(int action) {
		
		PersonaLog.d(TAG, "===============================Action received in Pin Reset Success is :"+action);
		switch (action) {
		
		case PersonaConstants.WARN_USER:
			mTitleImage.setBackgroundResource(R.drawable.pr_warn_user);
			mTitle.setText("");
			mMessage.setText(R.string.pr_pin_reset_success_message);
			mRightButton.setText(R.string.okButton);
			mLeftButton.setVisibility(View.GONE);
			android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
		            LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 2f);
			mRightButton.setLayoutParams(params);
			mRightButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mListener.onDialogCancelled();
				}
			});
			break;
		case PersonaConstants.SHOW_LAUNCHER_SCREEN :
			mTitleImage.setBackgroundResource(R.drawable.pr_warn_user);
			mTitle.setText("");
			mMessage.setText(R.string.pr_pin_reset_success_message);
			mRightButton.setText(R.string.okButton);
			mLeftButton.setVisibility(View.GONE);
			android.widget.LinearLayout.LayoutParams params_1 = new android.widget.LinearLayout.LayoutParams(
		            LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 2f);
			mRightButton.setLayoutParams(params_1);
			mRightButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mListener.onShowNextScreen(PersonaConstants.SHOW_LAUNCHER_SCREEN);
				}
			});
			break;
		}
	}

	private void updateViewForIncorrectLoginAttempts(int action) {
		
		switch (action) {
		
		case PersonaConstants.WIPE_APP:
			mTitleImage.setBackgroundResource(R.drawable.pr_wipe_app);
			mTitle.setText(R.string.prDataWipedOut);
			mMessage.setText(String.format(getResources().getString(R.string.prDataWipedIncorrectPIN),getString(R.string.pr_app_name)));
			mRightButton.setText(R.string.prRegisterNow);
			mLeftButton.setText(R.string.prLater);
			initializeClickListenersForWipe(PersonaConstants.WIPE_APP);
		
			break;
		}
		
	}
	
	private void updateViewForApplicationHacked(int action) {
		switch(action) {
		case PersonaConstants.WIPE_APP:
			mTitleImage.setBackgroundResource(R.drawable.pr_wipe_app);
			mTitle.setText(R.string.prDataWipedOut);
			mMessage.setText(String.format(getResources().getString(R.string.prDataWipedApplicationHacked),getResources().getString(R.string.pr_app_name)));
			mRightButton.setText(R.string.prRegisterNow);
			mLeftButton.setText(R.string.prLater);
			initializeClickListenersForWipe(PersonaConstants.WIPE_APP);
		
			break;
		}
	}

	private void updateViewForDeviceRooted(int action) {

		switch (action) {
		case PersonaConstants.DO_NOTHING:
			break;
		case PersonaConstants.WARN_USER:
			mTitleImage.setBackgroundResource(R.drawable.pr_warn_user);
			mTitle.setText(R.string.prWarning);
			mMessage.setText(String.format(getResources().getString(R.string.pr_device_rooted_warn_message),getResources().getString(R.string.pr_app_name)));
			mRightButton.setText(R.string.okButton);
			mLeftButton.setVisibility(View.GONE);
			android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
		            LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f);
			mRightButton.setLayoutParams(params);
			mRightButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mListener.onDialogCancelled();
				}
			});
			break;
		case PersonaConstants.BLOCK_DEVICE:
			mTitleImage.setBackgroundResource(R.drawable.pr_block_device);
			mTitle.setText(R.string.pr_access_denied);
			mMessage.setText(String.format(getResources().getString(R.string.pr_device_rooted_block_message),getResources().getString(R.string.pr_app_name)));
			mRightButton.setText(R.string.okButton);
			mLeftButton.setText(R.string.pr_contact_button);
			initializeClickListenersForBlock(PersonaConstants.BLOCK_DEVICE);
			
			break;
		case PersonaConstants.WIPE_APP:
			mTitleImage.setBackgroundResource(R.drawable.pr_wipe_app);
			mTitle.setText(R.string.prDataWipedOut);
			mMessage.setText(String.format(getResources().getString(R.string.prDataWipedApplicationHacked),getResources().getString(R.string.pr_app_name)));
			mRightButton.setText(R.string.registerButton);
			mLeftButton.setText(R.string.cancel_action);
			initializeClickListenersForWipe(PersonaConstants.WIPE_APP);
		
			break;
		}
	}

	
	private void updateViewForIMEBlackListed(int action) {
		switch (action) {
		case PersonaConstants.DO_NOTHING:
			break;
			//case 1243- rejected by admin
		case PersonaConstants.WARN_USER:
			mTitleImage.setBackgroundResource(R.drawable.pr_warn_user);
			mTitle.setText(R.string.prWarning);
			mMessage.setText(R.string.pr_imei_blacklist_warn_message);
			mRightButton.setText(R.string.okButton);
			mLeftButton.setVisibility(View.GONE);
			android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
		            LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f);
			mRightButton.setLayoutParams(params);
			mRightButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mListener.onDialogCancelled();
				}
			});
			break;
			//case 1242- rejected by system
		case PersonaCertfifcateAuthConstants.RE_REQUEST_CERT:
			mTitleImage.setBackgroundResource(R.drawable.pr_block_device);
			mTitle.setText(R.string.pr_access_denied);
			mMessage.setText(String.format(getResources().getString(R.string.pr_imei_blacklist_block_message),getResources().getString(R.string.pr_app_name)));
			mRightButton.setText(R.string.submitButton);
			mLeftButton.setText(R.string.later);
			initializeClickListenersForBlock(PersonaConstants.BLOCK_DEVICE);
			
			break;
		
		}
	}
	
	private void updateViewForCertificateFailureEvent(int action) {
		
		switch (action) {
		case PersonaConstants.DO_NOTHING:
			break;
		case PersonaCertfifcateAuthConstants.WARN_USER:
			mTitleImage.setBackgroundResource(R.drawable.pr_warn_user);
			mTitle.setText(R.string.prWarning);
			mMessage.setText(getResources().getString(R.string.pr_certificate_access_denied));
			mRightButton.setText(R.string.okButton);
			mLeftButton.setVisibility(View.GONE);
			android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
		            LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 2f);
			mRightButton.setLayoutParams(params);
			mRightButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mListener.onDialogCancelled();
				}
			});
			break;
		case PersonaCertfifcateAuthConstants.RE_REQUEST_CERT:
			
			mTitleImage.setBackgroundResource(R.drawable.pr_warn_user);
			mTitle.setText(R.string.prWarning);
			mMessage.setText(String.format(getResources().getString(R.string.pr_resubmit_request_message),getResources().getString(R.string.pr_app_name)));
			mRightButton.setText(R.string.submitButton);
			mLeftButton.setVisibility(View.GONE);
			android.widget.LinearLayout.LayoutParams layoutparams = new android.widget.LinearLayout.LayoutParams(
		            LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 2f);
			mRightButton.setLayoutParams(layoutparams);
			mRightButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mListener.onRequestCertificate();
				}
			});
			break;
	
		}

	}
// certificate error warning
	
private void updateViewForSimRemovalEvent(int action) {
		
		switch (action) {
		case PersonaConstants.DO_NOTHING:
			break;
		case PersonaConstants.WARN_USER:
			mTitleImage.setBackgroundResource(R.drawable.pr_warn_user);
			mTitle.setText(R.string.prWarning);
			mMessage.setText(String.format(getResources().getString(R.string.pr_sim_removal_warn_message),getResources().getString(R.string.pr_app_name)));
			mRightButton.setText(R.string.okButton);
			mLeftButton.setVisibility(View.GONE);
			android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
		            LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 2f);
			mRightButton.setLayoutParams(params);
			mRightButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mListener.onDialogCancelled();
				}
			});
			break;
		case PersonaConstants.BLOCK_DEVICE:
			mTitleImage.setBackgroundResource(R.drawable.pr_block_device);
			mTitle.setText(R.string.prDataWipedOut);
			mMessage.setText(String.format(getString(R.string.pr_sim_removal_block_message),getResources().getString(R.string.pr_app_name)));
			mRightButton.setText(R.string.okButton);
			mLeftButton.setText(R.string.pr_contact_button);
			initializeClickListenersForWipe(PersonaConstants.BLOCK_DEVICE);
			break;
		case PersonaConstants.WIPE_APP:
			mTitleImage.setBackgroundResource(R.drawable.pr_wipe_app);
			mTitle.setText(R.string.prDataWipedOut);
			mMessage.setText(String.format(getString(R.string.prDataWipedApplicationHacked),getResources().getString(R.string.pr_app_name)));
			mRightButton.setText(R.string.registerButton);
			mLeftButton.setText(R.string.pr_cancel_action);
			initializeClickListenersForWipe(PersonaConstants.WIPE_APP);
			break;
		}

	}

	private void initializeClickListenersForWipe(int wipeApp) {

		mRightButton.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				mListener
						.onShowNextScreen(PersonaConstants.WIPE_APP);
				return true;
			}
		});
		mLeftButton.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				mListener.onDialogCancelled();
				return true;
			}
		});

	}

	private void initializeClickListenersForBlock(int screen) {

		final int screenToShow = screen;
		mRightButton.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				mListener.onDialogCancelled();
				return true;
			}
		});
		mLeftButton.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				mListener.onShowNextScreen(screenToShow);
				return true;
			}
		});

	}
	private void initializeClickListenersForPINChange() {
		
	PersonaLog.d(TAG, "############# Initialize Click Listener for PIN Change");
		mRightButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mListener.onDialogCancelled();
			}
		});
		mLeftButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				String deviceType = getDeviceType(getActivity().getApplicationContext());
				if(deviceType == PersonaConstants.STR_PHONE)
				{
					Intent pinExpiryIntent = new Intent(v.getContext(), PersonaSettingsPinExpiry.class);
					pinExpiryIntent.putExtra("From_Page", "PINExpiryPage");
				//	new com.TruBoxSDK.SharedPreferences(getActivity()).edit().putBoolean("showExpiry", false).commit();
					
					startActivity(pinExpiryIntent);
					mListener.onDialogCancelled();
				}
				else if (deviceType == PersonaConstants.STR_TABLET)
				{
					Intent pinExpiryIntent = new Intent(v.getContext(), PersonaSettings.class);
				//	pinExpiryIntent.putExtra("From_Page", "PINExpiryPage");
				//	new com.TruBoxSDK.SharedPreferences(getActivity()).edit().putBoolean("showExpiry", false).commit();
					startActivity(pinExpiryIntent);
					mListener.onDialogCancelled();
				}
			}
		});		
	}
	private void initializeClickListenersForBugSense() {
		PersonaLog.d(TAG,"***** In initializeClickListenersForBugSense *****");
		mRightButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
			
				PersonaBugSenseHandler bugSenseHandler = new PersonaBugSenseHandler();
				bugSenseHandler.initSession(Email.getAppContext());
				//BUG_SENSE - 27Dec2013
				boolean bugsenseEnabled = true;
				updatePreferences(Email.getAppContext(),bugsenseEnabled);
				mListener.onDialogCancelled();
				//BUG_SENSE - 27Dec2013
			}
		});
		mLeftButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//BUG_SENSE - 27Dec2013
				boolean bugsenseEnabled = false;
				updatePreferences(Email.getAppContext(),bugsenseEnabled);
				mListener.onDialogCancelled();
				//BUG_SENSE - 27Dec2013
			}
		});
		PersonaLog.d(TAG,"***** out initializeClickListenersForBugSense *****");
	}
	
	@Override
	public void onCancel(DialogInterface dialog) {
		// TODO Auto-generated method stub
		mListener.onDialogCancelled();
		super.onCancel(dialog);
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		mListener = (DialogCancelListener) activity;
		super.onAttach(activity);
	}
	
	/**
	 * Used to check Device Type
	 * 
	 * @param context
	 * @return deviceType
	 */
	private String getDeviceType(Context context) {
		boolean xlarge = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
		boolean large = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
		if (xlarge || large)
			return PersonaConstants.STR_TABLET;
		return PersonaConstants.STR_PHONE;

	}
	
	//BUG_SENSE - 27Dec2013
		private void updatePreferences(Context appContext,boolean bugSenseStatus) {
			
			if(bugSenseStatus){
				PersonaLog.d(TAG," bugSenseStatus TRUE "+bugSenseStatus);
				new com.TruBoxSDK.SharedPreferences(appContext).edit().putBoolean(PersonaConstants.IS_BUGSENSE_ON, true).commit();
			}
			else{
				new com.TruBoxSDK.SharedPreferences(appContext).edit().putBoolean(PersonaConstants.IS_BUGSENSE_ON, false).commit();
			}
			
		}
		//BUG_SENSE - 27Dec2013
}
