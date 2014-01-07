package com.cognizant.trumobi.persona.settings;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseFragmentActivity;
import com.cognizant.trumobi.log.PersonaLog;
import com.cognizant.trumobi.persona.settings.PersonaSettingsListFragment.onSettingsMenuItemListener;
import com.cognizant.trumobi.persona.utils.PersonaCommonfunctions;


public class PersonaSettings extends TruMobiBaseFragmentActivity implements onSettingsMenuItemListener {

	boolean menuItem = false;
	
	private static String TAG=PersonaSettings.class.getName();
	private PersonaCommonfunctions mCommonFunctions;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		super.onCreate(savedInstanceState);
		
		mCommonFunctions = new PersonaCommonfunctions(this);
		PersonaLog.d(TAG,"==== In onCreate =====");
		setContentView(R.layout.pr_settings_fragment);
		
		if(savedInstanceState == null) {
			 PersonaLog.d(TAG,"==== savedInstanceState null  ====");
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			PersonaSettingsListFragment listFragment = new PersonaSettingsListFragment();
			ft.add(R.id.test_fragment1, listFragment, "List_Fragment");
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.commit();
			
		}
		
		  if(findViewById(R.id.test_fragment2) != null){
			  
			  PersonaLog.d(TAG,"==== In test fragment 2 not null ====");
				  menuItem = true;
				  getSupportFragmentManager().popBackStack();
			 
			Fragment detailFragment = getSupportFragmentManager().findFragmentById(R.id.test_fragment2);
			   if(detailFragment == null){
				   FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				   Handler mHandler = null;
			    detailFragment = new PersonaSettingsChangePin(mHandler);
			    detailFragment.setArguments(mCommonFunctions.getSettingsDataInBundle(this));
			    ft.replace(R.id.test_fragment2, detailFragment, "Detail_Fragment1");
			    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			    ft.commit();
			   }
		 }
	}

	@Override
	public void onSettingsMenuItemSelected(String URL) {
		
		PersonaLog.d(TAG,"===== In onSettingsMenuItemSelected =====");
		 if(menuItem){
			 PersonaLog.d(TAG,"===== In if  part =====");
			/*   PersonaSettingsChangePin detailFragment = (PersonaSettingsChangePin)
			   getSupportFragmentManager().findFragmentById(R.id.test_fragment2);
			   detailFragment.updateURLContent(URL);*/
				 Fragment detailFragment = null;
				
				 if(URL.contains("PIN") || URL.toLowerCase().contains("password")) {
					 
				 	detailFragment = getSupportFragmentManager().findFragmentById(R.id.test_fragment2);
				 	 Handler mHandler = null;
				    detailFragment = new PersonaSettingsChangePin(mHandler);
				    Bundle mBundle = mCommonFunctions.getSettingsDataInBundle(this);
				    detailFragment.setArguments(mBundle);
				 }
				 else if(URL.contains("Lock")){
					 detailFragment = getSupportFragmentManager().findFragmentById(R.id.test_fragment2);
					 detailFragment = new PersonaSettingsAutoLock();
					 PersonaLog.d("TAG","============ Else PIN ============");
				 }
				 else if(URL.contains(getString(R.string.prUpdateSettings))) {
					 detailFragment = getSupportFragmentManager().findFragmentById(R.id.test_fragment2);
					 detailFragment = new PersonaSettingsForceUpdate();
				 }
				 else if(URL.contains(("Bug Sense"))){ //BUG_SENSE - 27Dec2013
					 PersonaLog.d(TAG,"----- Bug Sense -----");
					 detailFragment = getSupportFragmentManager().findFragmentById(R.id.test_fragment2);
					 detailFragment = new PersonaSettingsBugSense();
				 } //BUG_SENSE - 27Dec2013
				FragmentTransaction ft = getSupportFragmentManager()
						.beginTransaction();
				ft.replace(R.id.test_fragment2, detailFragment, "Detail_Fragment1");
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				ft.commit();
				
				
		 }
		else{
			PersonaLog.d(TAG,"===== In ELSE part =====");
				  if(URL.contains("PIN") || URL.toLowerCase().contains("password")) {
				   Bundle mBundle = mCommonFunctions.getSettingsDataInBundle(this);
				   Handler mHandler = null;
				   PersonaSettingsChangePin contentFragment = new PersonaSettingsChangePin(mHandler);
				   contentFragment.setArguments(mBundle);
					//	  contentFragment.setURLContent(URL);
				   FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				   ft.replace(R.id.test_fragment1, contentFragment, "Detail_Fragment2");
				   ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				   ft.addToBackStack(null);
				   ft.commit();
				  }
				  else if(URL.contains("Lock")){
					  PersonaSettingsAutoLock contentFragment = new PersonaSettingsAutoLock();	
					  FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
					   ft.replace(R.id.test_fragment1, contentFragment, "Detail_Fragment2");
					   ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
					   ft.addToBackStack(null);
					   ft.commit();
				//	  contentFragment.setURLContent(URL);
				  }
				  else if(URL.contains(getString(R.string.prUpdateSettings))) {
					  PersonaSettingsForceUpdate contentFragment = new PersonaSettingsForceUpdate();
					  FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
					   ft.replace(R.id.test_fragment1, contentFragment, "Detail_Fragment2");
					   ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
					   ft.addToBackStack(null);
					   ft.commit();
				  }
				  else if(URL.contains("Bug Sense")){ //BUG_SENSE - 27Dec2013
					  
					  PersonaSettingsBugSense contentFragment = new PersonaSettingsBugSense();
					  FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
					   ft.replace(R.id.test_fragment1, contentFragment, "Detail_Fragment2");
					   ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
					   ft.addToBackStack(null);
					   ft.commit();
				  } //BUG_SENSE - 27Dec2013
		}
		
	}
	
	

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		PersonaSettingsListFragment.selectedItem=0;
	}
	
	public PersonaSettings() {
		// TODO Auto-generated constructor stub
	}
	
}
