package com.cognizant.trumobi.persona.settings;


import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseFragmentActivity;
import com.cognizant.trumobi.log.PersonaLog;
import com.cognizant.trumobi.persona.settings.PersonaSettingsListFragment.onSettingsMenuItemListener;


public class PersonaSettings extends TruMobiBaseFragmentActivity implements onSettingsMenuItemListener {

	boolean menuItem = false;
	
	private static String TAG=PersonaSettings.class.getName();
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		super.onCreate(savedInstanceState);
		
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
			    detailFragment = new PersonaSettingsChangePin();
			    detailFragment.setArguments(getIntent().getExtras());
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
				 Fragment detailFragment;
				
				 if(URL.contains("PIN")) {
					 
				 	detailFragment = getSupportFragmentManager().findFragmentById(R.id.test_fragment2);
				    detailFragment = new PersonaSettingsChangePin();
				 }
				 else {
					 detailFragment = getSupportFragmentManager().findFragmentById(R.id.test_fragment2);
					 detailFragment = new PersonaSettingsAutoLock();
				 }
				FragmentTransaction ft = getSupportFragmentManager()
						.beginTransaction();
				ft.replace(R.id.test_fragment2, detailFragment, "Detail_Fragment1");
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				ft.commit();
		 }
		else{
			PersonaLog.d(TAG,"===== In ELSE part =====");
				  if(URL.contains("PIN")) {
						  PersonaSettingsChangePin contentFragment = new PersonaSettingsChangePin();	
					//	  contentFragment.setURLContent(URL);
				   FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				   ft.replace(R.id.test_fragment1, contentFragment, "Detail_Fragment2");
				   ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				   ft.addToBackStack(null);
				   ft.commit();
				  }
				  else 
				  {
					  PersonaSettingsAutoLock contentFragment = new PersonaSettingsAutoLock();	
					  contentFragment = new PersonaSettingsAutoLock();
					  FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
					   ft.replace(R.id.test_fragment1, contentFragment, "Detail_Fragment2");
					   ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
					   ft.addToBackStack(null);
					   ft.commit();
				//	  contentFragment.setURLContent(URL);
				  }
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
