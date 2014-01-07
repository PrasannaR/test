package com.cognizant.trumobi.persona.settings;

import com.TruBoxSDK.SharedPreferences;
import com.cognizant.trumobi.PersonaLauncher;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.log.PersonaLog;
import com.cognizant.trumobi.persona.PersonaBugSenseHandler;
import com.cognizant.trumobi.persona.PersonaSecurityProfileUpdate;
import com.cognizant.trumobi.persona.constants.PersonaConstants;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

public class PersonaSettingsBugSense extends android.support.v4.app.Fragment{

	private static String TAG = PersonaSettingsBugSense.class.getName();
	
	private CheckBox mBugsenseView; 
	ImageView mHomeIcon;
	public PersonaSettingsBugSense(){
		
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		PersonaLog.d(TAG,"=== In onActivityCreated ====");
		super.onActivityCreated(savedInstanceState);
		mBugsenseView = (CheckBox) getActivity().findViewById(R.id.pr_settings_screen_menu_bugsense);
		
		 
		 mBugsenseView.setOnTouchListener(new View.OnTouchListener() {
		@Override
		public boolean onTouch(View arg0, MotionEvent arg1) {
			
			 if(mBugsenseView.isChecked()) {
				 	// Disable bugsense since it is already enabled
					PersonaBugSenseHandler bugSenseHandler = new PersonaBugSenseHandler();
					bugSenseHandler.closeSession(Email.getAppContext());
					new com.TruBoxSDK.SharedPreferences(Email.getAppContext()).edit().putBoolean(PersonaConstants.IS_BUGSENSE_ON, false).commit();
					PersonaLog.d(TAG, "---------- bug sense diasbled --------");
			 }
			 else{
				 	Intent intent = new Intent(getActivity(),
							PersonaSecurityProfileUpdate.class);
					intent.putExtra("securityevent",
							PersonaConstants.BUGSENSE_ON_ALERT);
					intent.putExtra("securityaction", PersonaConstants.BUGSENSE_ON_ALERT);
					startActivity(intent);
					PersonaLog.d(TAG, "---------- bug sense going to be enabled --------");
				}
				
			return false;
		}
		 });
		
		
		
		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		PersonaLog.d(TAG,"=== In onCreate ====");
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		PersonaLog.d(TAG,"=== In onCreateView ====");
		View view =  inflater.inflate(R.layout.pr_settings_bugsense, container, false);
		mHomeIcon = (ImageView) view.findViewById(R.id.pr_bugsense_title_bar_icon);
		 mHomeIcon.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					 goTolauncherHome();	
				}
			});
		 if(getActivity().getResources().getInteger(R.integer.deviceType) == 1) {
				TextView forceUpdateTitle =(TextView) view.findViewById(R.id.pr_settings_bugsense_title);
				forceUpdateTitle.setVisibility(View.INVISIBLE);
		}
		return view;
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		PersonaLog.d(TAG,"=== In onResume ====");
		super.onResume();
		boolean bugSenseStatus = new SharedPreferences(Email.getAppContext()).getBoolean(PersonaConstants.IS_BUGSENSE_ON, false);
		PersonaLog.d(TAG," bugSenseStatus"+bugSenseStatus);
		mBugsenseView.setChecked(bugSenseStatus);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		PersonaLog.d(TAG,"=== In onViewCreated ====");
		super.onViewCreated(view, savedInstanceState);
	}

	
	private void goTolauncherHome(){
		
		Intent intent =new Intent(getActivity(),PersonaLauncher.class);
		startActivity(intent);
		getActivity().finish();
	}
	
}
