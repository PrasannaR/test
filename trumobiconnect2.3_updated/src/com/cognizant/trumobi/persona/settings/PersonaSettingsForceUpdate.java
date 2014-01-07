package com.cognizant.trumobi.persona.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.TruBoxSDK.SharedPreferences;
import com.cognizant.trumobi.PersonaLauncher;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.externaladapter.ExternalAdapterRegistrationClass;
import com.cognizant.trumobi.persona.PersonaModelLocalAuthentication;

public class PersonaSettingsForceUpdate extends Fragment {

	Button mUpdateSettings;
	ExternalAdapterRegistrationClass mExtReg;
	int mLocalAuthtype,mNumOfTrialsAllowed,mExpiryInDays,mPwdLength;
	ImageView mHomeIcon;
	public PersonaSettingsForceUpdate() {
		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		 super.onCreateView(inflater, container, savedInstanceState);
		 View view = inflater.inflate(R.layout.pr_settings_force_update_settings, container,false);
		 mHomeIcon = (ImageView) view.findViewById(R.id.pr_force_update_title_bar_icon);
		 mHomeIcon.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					 goTolauncherHome();	
				}
			});
		 if(getActivity().getResources().getInteger(R.integer.deviceType) == 1) {
				TextView forceUpdateTitle =(TextView) view.findViewById(R.id.pr_settings_force_update_title);
				forceUpdateTitle.setVisibility(View.INVISIBLE);
		}
		 return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		
		super.onActivityCreated(savedInstanceState);
		getActivity().registerReceiver(SettingsUpdatedReceiver, new IntentFilter(
				"UpdatedSettingsInfoRetrived"));

		mUpdateSettings = (Button)getActivity().findViewById(R.id.pr_settings_force_update_textview);
		mUpdateSettings.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mExtReg = ExternalAdapterRegistrationClass.getInstance(getActivity());
				mExtReg.initiateExternalToFetchAllSettingsInfo();
			}
		});
		
	}
	
	

	public BroadcastReceiver SettingsUpdatedReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			mExtReg = ExternalAdapterRegistrationClass.getInstance(context);
		mLocalAuthtype = mExtReg.getExternalPIMSettingsInfo().Passwordtype
					.getValue();
		mPwdLength = mExtReg.getExternalPIMSettingsInfo().nPasswordLength;
		/*mNumOfTrialsAllowed = mExtReg.getExternalPIMSettingsInfo().nMaxPasswordTries;
		mExpiryInDays = mExtReg.getExternalPIMSettingsInfo().PasswordExpires
					.getValue();*/
		Bundle bundle = new Bundle();
		bundle.putString("username",new SharedPreferences(context).getString(
				"trumobi_username", ""));
		bundle.putInt("localauthtype", mLocalAuthtype);
		bundle.putInt("pwdlength", mPwdLength);
		PersonaModelLocalAuthentication modelAuthentication = new PersonaModelLocalAuthentication(context);
		modelAuthentication.updatePIMSettings(bundle);
		}
	};

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		try {
			getActivity().unregisterReceiver(SettingsUpdatedReceiver);
		}
		catch(Exception e) {
			
		}
	};
	
	
	private void goTolauncherHome(){
		
		Intent intent =new Intent(getActivity(),PersonaLauncher.class);
		startActivity(intent);
		getActivity().finish();
	}
	
}
