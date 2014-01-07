package com.cognizant.trumobi.persona;

import android.content.Context;

import com.TruBoxSDK.SharedPreferences;
import com.bugsense.trace.BugSenseHandler;
import com.cognizant.trumobi.em.EmAccount;
import com.cognizant.trumobi.persona.constants.PersonaConstants;
import com.cognizant.trumobi.persona.utils.PersonaCommonfunctions;

public class PersonaBugSenseHandler {

	private String mUsername;
	public PersonaBugSenseHandler() {
	
	}

	public void initSession(Context appContext) {
		
		mUsername = getUserName(appContext);
		if(PersonaMainActivity.isRovaPoliciesOn) {
			BugSenseHandler.initAndStartSession(appContext, PersonaConstants.ROVA_BUGSENSE_CODE);
			BugSenseHandler.setUserIdentifier(getEmailAddress());
			BugSenseHandler.addCrashExtraData("build.type", PersonaConstants.ROVA_BUILD_TYPE);
            BugSenseHandler.addCrashExtraData("build.user.unique.name", mUsername );
		}
		else {
			BugSenseHandler.initAndStartSession(appContext, PersonaConstants.BYOD_BUGSENSE_CODE);
			BugSenseHandler.setUserIdentifier(getEmailAddress());
			BugSenseHandler.addCrashExtraData("build.type", PersonaConstants.BYOD_BUILD_TYPE);
            BugSenseHandler.addCrashExtraData("build.user.unique.name", mUsername );

		}
		
	}

	private String getUserName(Context ctx) {
		return new SharedPreferences(ctx).getString("trumobi_username", "");
	}

	private String getEmailAddress() {
	 String emailAddress = "";
	 EmAccount account = PersonaCommonfunctions.getAccount();
	 if(account != null) {
	 //emailAddress = account.getEmailAddress();
	 }
	 return emailAddress;
	}

	public void closeSession(Context appContext) {
		BugSenseHandler.closeSession(appContext);
	}
	
	
	

}
