package com.cognizant.trumobi.commonbroadcastreceivers;


import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.cognizant.trumobi.em.provider.EmEmailContent;
import com.cognizant.trumobi.externaladapter.ExternalAdapterRegistrationClass;
import com.cognizant.trumobi.log.PersonaLog;
import com.cognizant.trumobi.persona.PersonaLocalAuthentication;
import com.cognizant.trumobi.persona.PersonaSecurityProfileUpdate;
import com.cognizant.trumobi.persona.PersonaSecurityUpdateDialogFragment;
import com.cognizant.trumobi.persona.constants.PersonaConstants;

public class ExternalPolicyIntentService extends IntentService {
	private static String TAG=ExternalPolicyIntentService.class.getName();
	public ExternalPolicyIntentService() {
		super("PolicyIntentSrevice");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		int _id = intent.getIntExtra("Id", 0);
		switch(_id){
		case ExternalPolicyUtil.EVENT_EMAIL_SETTINGS_UPDATE_ID:

			break;
		case ExternalPolicyUtil.EVENT_LOGIN_REGISTER_COMP_ID:

			break;
		case ExternalPolicyUtil.EVENT_PIM_CONFIG_UPDATE_ID:
			/*Intent st = new Intent(PloicyMainActivity.getPolicyAppcontext(), PolicyPopUp.class);
			st.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			st.putExtra("msg", "EVENT_PIM_CONFIG_UPDATE_ID");
			PloicyMainActivity.getPolicyAppcontext().startActivity(st);*/
			break;
		case ExternalPolicyUtil.EVENT_SEC_PROFILE_CHANGE_ID:
			localBroadCast();
			processSecurityProfileChange();
			break;
		case ExternalPolicyUtil.EVENT_SIM_CARD_REMOVAL_NOTI_ID:

			break;
		case ExternalPolicyUtil.EVENT_VPN_UPDATE_ID:

			break;
		case ExternalPolicyUtil.EVENT_SETTING_INFO_COMP_ID:

			break;
		}
	}

	 private void  processSecurityProfileChange() {
			// TODO Auto-generated method stub
			PersonaLog.d(TAG,"=====processSecurityProfileChange====");
			ExternalAdapterRegistrationClass extnAdap = ExternalAdapterRegistrationClass.getInstance(this);  //----  To get the instance of ExternalAdapterRegistrationClass.
			boolean bDeviceBlock = extnAdap.getExternalSecurityProfInfo().bDeviceBlock;
			bDeviceBlock = true;
			boolean bDeviceUnregister= extnAdap.getExternalSecurityProfInfo().bDeviceUnregister;
			boolean bDeviceWipe= extnAdap.getExternalSecurityProfInfo().bDeviceWipe;
			
			int iSecProcInfo = extnAdap.getExternalPIMSettingsInfo().SecurityRoot.getValue();
			PersonaLog.d(TAG," Value of isSecProcInfo:======	"+iSecProcInfo);
			int mEvent = PersonaConstants.DEVICE_ROOTED_EVENT;
			int mAction = PersonaConstants.WARN_USER;
			
//			if(bDeviceWipe){
//			Intent intent=new Intent("com.cognizant.trumobi.action.APP_WIPE");
//			long accountId=EmailAccount.getDefaultAccountId(getApplicationContext());
//			EmailAccount account = EmailAccount.restoreAccountWithId(this, accountId);
//			intent.putExtra("Email_id", account.toString());
//			getApplicationContext().sendBroadcast(intent);
//			}
			//290778- To check for deviceBlock and push the screen if the app is in foreground
			if(bDeviceBlock){
//				ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
//			    List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
//				final String packageName = getApplicationContext().getPackageName();
//				  for (RunningAppProcessInfo appProcess : appProcesses) {
//				      if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
				
						//	sendDeviceWipeBroadCastIntent();
				    		Intent intent=new Intent(getApplicationContext(),PersonaSecurityProfileUpdate.class);
				    		intent.putExtra("securityevent", mEvent);
				    		intent.putExtra("securityaction", mAction );
				    		long accountId = EmEmailContent.Account.getDefaultAccountId(this);
				    		EmEmailContent.Account account = EmEmailContent.Account.restoreAccountWithId(this, accountId);
							if (account != null) {
								intent.putExtra("Email_id", account.toString());
							}
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							getApplicationContext().startActivity(intent);
				      //}
				      }
			
				
		
			PersonaLog.d(TAG,"=====after Send BroadCast====");
		}

	  
	  private void localBroadCast(){
		  
		  Intent brdIntent = new Intent("KILL_ACT");
		  LocalBroadcastManager localMgr = LocalBroadcastManager.getInstance(this);
		  localMgr.sendBroadcast(brdIntent);
		  
	  }






}
