package com.cognizant.trumobi.persona.settings;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseFragmentActivity;
import com.cognizant.trumobi.log.PersonaLog;

public class PersonaSettingsPinExpiry extends TruMobiBaseFragmentActivity {
	
	//Fragment Activity to show the PIN Change screen on Set PIN action in PIN Expiry Dialog.
	
		private static String TAG = PersonaSettingsPinExpiry.class.getName();
		
		
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			
			super.onCreate(savedInstanceState);
			
			PersonaLog.d(TAG,"==== In onCreate =====");
			PersonaLog.d(TAG,"==== In onCreate, Saved Instance : " +savedInstanceState);		
			
			setContentView(R.layout.pr_show_pin_expiry);		
			
			if(savedInstanceState == null) {
				PersonaLog.d(TAG,"==== savedInstanceState null  ====");
				
				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				PersonaSettingsChangePin changePin = new PersonaSettingsChangePin();
				changePin.setArguments(getIntent().getExtras());
				ft.add(R.id.test_fragment1, changePin, "List_Fragment");
				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
				ft.commit();				
			}			
		}
		public PersonaSettingsPinExpiry() {
		// TODO Auto-generated constructor stub
	}
}
