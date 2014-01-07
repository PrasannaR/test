package com.cognizant.trumobi.persona.settings;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseFragmentActivity;
import com.cognizant.trumobi.log.PersonaLog;
import com.cognizant.trumobi.persona.PersonaLocalAuthentication;
import com.cognizant.trumobi.persona.utils.PersonaCommonfunctions;

public class PersonaSettingsPinExpiry extends TruMobiBaseFragmentActivity {
	
	//Fragment Activity to show the PIN Change screen on Set PIN action in PIN Expity Dialog.
	
	private static String TAG = PersonaSettingsPinExpiry.class.getName();
	String fromPage,pin;
	PersonaCommonfunctions mCommonfunctions;
	Handler mHandler = null;
	int mUpdatedPwdType, mUpdatedPwdLength;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		super.onCreate(savedInstanceState);
		
		PersonaLog.d(TAG,"==== In onCreate =====");
		PersonaLog.d(TAG,"==== In onCreate, Saved Instance : " +savedInstanceState);	
		mCommonfunctions = new PersonaCommonfunctions(this);
		Bundle mBundle = new Bundle();
		mBundle = mCommonfunctions.getSettingsDataInBundle(this);
		 if (getIntent().getExtras() != null ) {
			 fromPage =	getIntent().getExtras().getString("From");
			 pin  =	getIntent().getExtras().getString("credentials");
		//	 mCurrentPwdType = getIntent().getExtras().getInt("currentPwdType");
		//	 mCurrentPwdlength = getIntent().getExtras().getInt("currentPwdlength");
			 mUpdatedPwdType = getIntent().getExtras().getInt("updatedPwdType");
			 mUpdatedPwdLength = getIntent().getExtras().getInt("updatedPwdLength");
		}
		if(fromPage != null && fromPage.equals("PINAlreadyExpired")) {
			
			mBundle.putString("From", "PINAlreadyExpired");
			mBundle.putString("credentials", pin);
			PersonaLocalAuthentication localauth = new PersonaLocalAuthentication();
			mHandler = localauth.showLauncherCallback;
		}
		else if (fromPage != null && fromPage.equals("PimSettingsUpdated")) {
			PersonaLog.d(TAG,"==== PimSettingsUpdated else =====");
			mBundle.putString("From", "PimSettingsUpdated");
			mBundle.putString("credentials", pin);
		//	mBundle.putInt("currentPwdType", mCurrentPwdType);
		//	mBundle.putInt("currentPwdlength", mCurrentPwdlength);
			mBundle.putInt("updatedPwdType", mUpdatedPwdType);
			mBundle.putInt("updatedPwdLength", mUpdatedPwdLength);
			PersonaLocalAuthentication localauth = new PersonaLocalAuthentication();
			mHandler = localauth.showLauncherCallback;
		}
		/*else {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		}*/
		setContentView(R.layout.pr_show_pin_expiry);
		// SharedPreference is set to false. else on every oncreate/onresume of personaLauncherdialog will be shown.
		//note: screen rotation itself will call oncreate in personalauncher
		new com.TruBoxSDK.SharedPreferences(this).edit().putBoolean("showExpiry", false).commit();
		if(savedInstanceState == null) {
			PersonaLog.d(TAG,"==== savedInstanceState null  ====");
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			PersonaSettingsChangePin changePin = new PersonaSettingsChangePin(mHandler);
			changePin.setArguments(mBundle);
			ft.add(R.id.test_fragment1, changePin, "List_Fragment");
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			ft.commit();
			
		}
		
	}
	
	public PersonaSettingsPinExpiry() {
		// TODO Auto-generated constructor stub
	}
	
	
}
