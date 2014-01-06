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
import com.cognizant.trumobi.log.PersonaLog;
import com.cognizant.trumobi.persona.constants.PersonaConstants;
import com.cognizant.trumobi.persona.settings.PersonaSettings;
import com.cognizant.trumobi.persona.settings.PersonaSettingsPinExpiry;

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
		}

	}

	
private void updateViewForIncorrectLoginAttempts(int action) {
		
		switch (action) {
		
		case PersonaConstants.WIPE_APP:
			mTitleImage.setBackgroundResource(R.drawable.pr_wipe_app);
			mTitle.setText(R.string.prDataWipedOut);
			mMessage.setText(R.string.prDataWipedIncorrectPIN);
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
		mMessage.setText(R.string.prDataWipedApplicationHacked);
		mRightButton.setText(R.string.prRegisterNow);
		mLeftButton.setText(R.string.prLater);
		initializeClickListenersForWipe(PersonaConstants.WIPE_APP);
	
		break;
	}
}


private void updateViewForShowPinExpiry(int action) {
	shared = getActivity().getSharedPreferences(PersonaConstants.PERSONAPREFERENCESFILE, 0);
	
	int daysLeft = shared.getInt("daysLeft", 3);
	String pwdtype = shared.getString("authtype", "PIN");
	
		switch (action) {
		case PersonaConstants.DO_NOTHING:
			break;
		case PersonaConstants.SHOW_PIN_EXPIRY:
			mTitleImage.setBackgroundResource(R.drawable.pr_warn_user);
			mTitle.setText("Warning!");
			mMessage.setText(("Your " + pwdtype + " will expire in "
					+ daysLeft + " days. Do you want to change it now?"));
			mLeftButton.setText("Set PIN now");
			mRightButton.setText("Set PIN later");
			initializeClickListenersForPINChange(PersonaConstants.SHOW_PIN_EXPIRY);
			break;
		}
	}

private void updateViewForPinResetSuccess(int action) {
	
	switch (action) {
	
	case PersonaConstants.WARN_USER:
		mTitleImage.setBackgroundResource(R.drawable.pr_warn_user);
		mTitle.setText("");
		mMessage.setText("PIN reset success!   ");
		mRightButton.setText("OK");
		mLeftButton.setVisibility(View.GONE);
		android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
	            LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 2f);
		mRightButton.setLayoutParams(params);
		mRightButton.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				mListener.onDialogCancelled();
				return true;
			}
		});
		break;
	case PersonaConstants.SHOW_LAUNCHER_SCREEN :
		mTitleImage.setBackgroundResource(R.drawable.pr_warn_user);
		mTitle.setText("");
		mMessage.setText("PIN reset success!   ");
		mRightButton.setText("OK");
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
	private void updateViewForDeviceRooted(int action) {

		switch (action) {
		case PersonaConstants.DO_NOTHING:
			break;
		case PersonaConstants.WARN_USER:
			mTitleImage.setBackgroundResource(R.drawable.pr_warn_user);
			mTitle.setText("Warning!");
			mMessage.setText("SafeSpace found your device is rooted\nPlease be careful\nYour device and secure data are more vulnerable to attack by unknown sources");
			mRightButton.setText("OK");
			mLeftButton.setVisibility(View.GONE);
			android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 2f);
			mRightButton.setLayoutParams(params);
			mRightButton.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View arg0, MotionEvent arg1) {
					mListener.onDialogCancelled();
					return true;
				}
			});
			break;
		case PersonaConstants.BLOCK_DEVICE:
			mTitleImage.setBackgroundResource(R.drawable.pr_block_device);
			mTitle.setText("Access Denied!");
			mMessage.setText("You are denied access SafeSpace.\nTo request access, contact admin team.");
			mRightButton.setText("OK");
			mLeftButton.setText("Contact");
			initializeClickListenersForBlock(PersonaConstants.BLOCK_DEVICE);

			break;
		case PersonaConstants.WIPE_APP:
			mTitleImage.setBackgroundResource(R.drawable.pr_wipe_app);
			mTitle.setText("Data Wiped Out!");
			mMessage.setText("All your enterprise data has been wiped out.");
			mRightButton.setText("Register");
			mLeftButton.setText("Cancel");
			initializeClickListenersForWipe(PersonaConstants.WIPE_APP);

			break;
		}
	}

	private void updateViewForIMEBlackListed(int action) {
		switch (action) {
		case PersonaConstants.DO_NOTHING:
			break;
		case PersonaConstants.WARN_USER:
			mTitleImage.setBackgroundResource(R.drawable.pr_warn_user);
			mTitle.setText("Warning!");
			mMessage.setText("Your IME is not found in Whitelist..!!");
			mRightButton.setText("OK");
			mLeftButton.setVisibility(View.GONE);
			android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 2f);
			mRightButton.setLayoutParams(params);
			mRightButton.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View arg0, MotionEvent arg1) {
					mListener.onDialogCancelled();
					return true;
				}
			});
			break;
		case PersonaConstants.BLOCK_DEVICE:
			mTitleImage.setBackgroundResource(R.drawable.pr_block_device);
			mTitle.setText("Access Denied!");
			mMessage.setText("Your IME is not found in Whitelist..!!\nYou are denied access SafeSpace.\nTo request access, contact admin team.");
			mRightButton.setText("OK");
			mLeftButton.setText("Contact");
			initializeClickListenersForBlock(PersonaConstants.BLOCK_DEVICE);

			break;
		case PersonaConstants.WIPE_APP:
			mTitleImage.setBackgroundResource(R.drawable.pr_wipe_app);
			mTitle.setText("Data Wiped Out!");
			mMessage.setText("All your enterprise data has been wiped out.");
			mRightButton.setText("Register");
			mLeftButton.setText("Cancel");
			initializeClickListenersForWipe(PersonaConstants.WIPE_APP);

			break;
		}
	}

	private void updateViewForSimRemovalEvent(int action) {

		switch (action) {
		case PersonaConstants.DO_NOTHING:
			break;
		case PersonaConstants.WARN_USER:
			mTitleImage.setBackgroundResource(R.drawable.pr_warn_user);
			mTitle.setText("Warning!");
			mMessage.setText("SafeSpace found your Device ID and\nSIM number do not match");
			mRightButton.setText("OK");
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
		case PersonaConstants. BLOCK_DEVICE:
			mTitleImage.setBackgroundResource(R.drawable.pr_block_device);
			mTitle.setText("Access Denied!");
			mMessage.setText("SafeSpace found your Device ID and\nSIM number do not match..!!\nYou are denied access SafeSpace.\nTo request access, contact admin team.");
			mRightButton.setText("OK");
			mLeftButton.setText("Contact");
			initializeClickListenersForWipe( PersonaConstants.BLOCK_DEVICE);
			break;
		case PersonaConstants. WIPE_APP:
			mTitleImage.setBackgroundResource(R.drawable.pr_wipe_app);
			mTitle.setText("Data Wiped Out!");
			mMessage.setText("All your enterprise data has been wiped out.");
			mRightButton.setText("Register");
			mLeftButton.setText("Cancel");
			initializeClickListenersForWipe( PersonaConstants.WIPE_APP);
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
	
	private void initializeClickListenersForPINChange(int screen) {
		
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
					PersonaLog.d(TAG, "************************ Device Type is " +deviceType);
					if(deviceType == PersonaConstants.STR_PHONE)
					{
						Intent pinExpiryIntent = new Intent(v.getContext(), PersonaSettingsPinExpiry.class);
						pinExpiryIntent.putExtra("From_Page", "PINExpiryPage");
						//SharedPreferences.Editor edit = shared.edit();
						//edit.putBoolean("showExpiry", false);
						//edit.commit();
						PersonaLog.d(TAG, "*************** Calling Persona Settings Pjn Expiry Intent *************" );
						startActivity(pinExpiryIntent);
					}
					else if (deviceType == PersonaConstants.STR_TABLET)
					{
						Intent pinExpiryIntent = new Intent(v.getContext(), PersonaSettings.class);
						pinExpiryIntent.putExtra("From_Page", "PINExpiryPage");
						//SharedPreferences.Editor edit = shared.edit();
						//edit.putBoolean("showExpiry", false);
						//edit.commit();
						startActivity(pinExpiryIntent);
					}
				}
			});		
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
}
