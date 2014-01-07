package com.cognizant.trumobi.commonbroadcastreceivers;


import com.cognizant.trumobi.log.PersonaLog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ExternalBroadcastReceiver extends BroadcastReceiver{

	private static String TAG=ExternalBroadcastReceiver.class.getName();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		/*if(intent.getAction().equalsIgnoreCase("android.provider.Telephony.SMS_RECEIVED")){

			LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("KILL_ACT"));

		}*/
		if(intent.getAction().equalsIgnoreCase("com.cognizant.trubox.event")){
			PersonaLog.d(TAG, "=========== OnReceive =====");
			PersonaLog.d(TAG, "=========== intent action ====="+intent.getAction());
			processEventListeners(context,intent);

		}

	}
	
	private void processEventListeners(Context mContext,Intent intent) {
		// TODO Auto-generated method stub

		String evnt = intent.getStringExtra("Event");
		PersonaLog.d(TAG, "=========== OnReceive for event=====" + evnt);
		int _id = ExternalPolicyUtil.getEvent_ID(evnt);
	//	int _id = 7;
		switch(_id){
		case ExternalPolicyUtil.EVENT_EMAIL_SETTINGS_UPDATE_ID:
			ExternalPolicyUtil.emailSettingsUpdateComplete_EventHandler(mContext ,intent);
			break;
		case ExternalPolicyUtil.EVENT_LOGIN_REGISTER_COMP_ID:
			ExternalPolicyUtil.loginOrRegistrationCompletion_EventHandler(mContext ,intent);
			break;
		case ExternalPolicyUtil.EVENT_PIM_CONFIG_UPDATE_ID:
			ExternalPolicyUtil.pimConfigurationUpdate_EventHandler(mContext ,intent);
			break;
		case ExternalPolicyUtil.EVENT_SEC_PROFILE_CHANGE_ID:
			ExternalPolicyUtil.securityProfileChange_EventHandler(mContext ,intent);
			break;
		case ExternalPolicyUtil.EVENT_SIM_CARD_REMOVAL_NOTI_ID:
			ExternalPolicyUtil.simCardRemovalNotification_EventHandler(mContext ,intent);
			break;
		case ExternalPolicyUtil.EVENT_VPN_UPDATE_ID:
			ExternalPolicyUtil.vpnSettingsUpdateComplete_EventHandler(mContext ,intent);
			break;
		case ExternalPolicyUtil.EVENT_SETTING_INFO_COMP_ID:
			ExternalPolicyUtil.settingInformationCompleten_EventHandler(mContext ,intent);
			break;
		}
	}

}
