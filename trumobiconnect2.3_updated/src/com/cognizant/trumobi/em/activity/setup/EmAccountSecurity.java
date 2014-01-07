

package com.cognizant.trumobi.em.activity.setup;

import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseActivity;
import com.cognizant.trumobi.em.EmSecurityPolicy;
import com.cognizant.trumobi.em.activity.EmMessageList;
import com.cognizant.trumobi.em.activity.EmWelcome;
import com.cognizant.trumobi.em.provider.EmEmailContent.Account;
import com.cognizant.trumobi.em.provider.EmEmailContent.HostAuth;
import com.cognizant.trumobi.log.EmailLog;
import com.cognizant.trumobi.persona.PersonaMainActivity;


/**
 * Psuedo-activity (no UI) to bootstrap the user up to a higher desired security level.  This
 * bootstrap requires the following steps.
 *
 * 1.  Confirm the account of interest has any security policies defined - exit early if not
 * 2.  If not actively administrating the device, ask Device Policy Manager to start that
 * 3.  When we are actively administrating, check current policies and see if they're sufficient
 * 4.  If not, set policies
 * 5.  If necessary, request for user to update device password
 */
public class EmAccountSecurity extends TruMobiBaseActivity {

    private static final String EXTRA_ACCOUNT_ID = "com.cognizant.trumobi.em.activity.setup.ACCOUNT_ID";

    private static final int REQUEST_ENABLE = 1;

	private static final String TAG = EmAccountSecurity.class.getSimpleName();
    //public static boolean policyRequired = false;
    /**
     * Used for generating intent for this activity (which is intended to be launched
     * from a notification.)
     *
     * @param context Calling context for building the intent
     * @param accountId The account of interest
     * @return an Intent which can be used to view that account
     */
    public static Intent actionUpdateSecurityIntent(Context context, long accountId) {
    	//policyRequired = true;
        Intent intent = new Intent(context, EmAccountSecurity.class);
        intent.putExtra(EXTRA_ACCOUNT_ID, accountId);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        //setContentView(R.layout.account_setup_check_settings);
        //getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.customtitlebar);
        
        Intent i = getIntent();
        long accountId = i.getLongExtra(EXTRA_ACCOUNT_ID, -1);
        EmSecurityPolicy security = EmSecurityPolicy.getInstance(this);
        security.clearNotification(accountId);
        
        if(accountId == -1) {
        	final long frmPersona = i.getLongExtra("accountId", -1);
      
	        if(frmPersona != -1) {
	        	EmailLog.d("EmailAccountSecurity", "Account Id from Persona");
	        	accountId = frmPersona;
	        }
        }
        if (accountId != -1 && !EmSecurityPolicy.skip_Admin_Policy) {	//367712, Admin Disable
            // TODO: spin up a thread to do this in the background, because of DB ops
            Account account = Account.restoreAccountWithId(this, accountId);
            if (account != null) {
                if (account.mSecurityFlags != 0) {
                    // This account wants to control security
                    if (!security.isActiveAdmin()) {
                    	
                    	EmailLog.d(TAG, "App status: "+TruMobiBaseActivity.isAppWentToBg + " : "+TruMobiBaseActivity.isBackPressed);

                        if(TruMobiBaseActivity.isAppWentToBg && !EmMessageList.getCurrentTopActivity(context) ) //add this block
                        {      //Persona PIN
                               Intent intent = new Intent(this,PersonaMainActivity.class);
                               intent.putExtra("email_securitynotify", true);
                               intent.putExtra("accountId", accountId);
                         //      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                   		//	   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                               this.startActivity(intent);
                               TruMobiBaseActivity.isAppWentToBg = false;
                               finish();
                               return;
                               
                        }
                        // retrieve name of server for the format string
                        HostAuth hostAuth =
                                HostAuth.restoreHostAuthWithId(this, account.mHostAuthKeyRecv);
                        if (hostAuth != null) {
                            // try to become active - must happen here in activity, to get result
                            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                                    security.getAdminComponent());
                            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                this.getString(R.string.account_security_policy_explanation_fmt,
                                        hostAuth.mAddress));
                            startActivityForResult(intent, REQUEST_ENABLE);
                            // keep this activity on stack to process result
                            return;
                        }
                    } else {
                        // already active - try to set actual policies, finish, and return
                        setActivePolicies();
                    }
                }
                
            }
        }
        finish();
    }

    /**
     * Handle the eventual result of the user allowing us to become an active device admin
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE:
                if (resultCode == Activity.RESULT_OK) {
                    // now active - try to set actual policies
                	//policyRequired = false;	//NaGa, for showing loading for eas
                	PersonaMainActivity.securityNotification = false;
                    setActivePolicies();
                    Intent i = new Intent(this, EmWelcome.class); // NaGa
                    i.putExtra("fromadminSecurity", true);
            		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            		this.startActivity(i);
                } else {
                    // failed - repost notification, and exit
                	long tempaccnt = getIntent().getLongExtra(EXTRA_ACCOUNT_ID, -1);
                	if (tempaccnt == -1)
                		tempaccnt = getIntent().getLongExtra("accountId", -1);
                    final long accountId = tempaccnt;//getIntent().getLongExtra(EXTRA_ACCOUNT_ID, -1);
                   
                    if (accountId != -1) {
                        new Thread() {
                            @Override
                            public void run() {
                                EmSecurityPolicy.getInstance(EmAccountSecurity.this)
                                        .policiesRequired(accountId);
                            }
                        }.start();
                    }
                }
        }
        finish();
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Now that we are connected as an active device admin, try to set the device to the
     * correct security level, and ask for a password if necessary.
     */
    private void setActivePolicies() {
        EmSecurityPolicy sp = EmSecurityPolicy.getInstance(this);
        // check current security level - if sufficient, we're done!
        if (sp.isActive(null)) {
            sp.clearAccountHoldFlags();
            return;
        }
        // set current security level
        sp.setActivePolicies();
        // check current security level - if sufficient, we're done!
        if (sp.isActive(null)) {
            sp.clearAccountHoldFlags();
            return;
        }
        // if not sufficient, launch the activity to have the user set a new password.
        Intent intent = new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD);
        startActivity(intent);
    }
    
    public static boolean getCurrentTopActivity(final Context context) {

        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> RunningTask = mActivityManager.getRunningTasks(1);
        ActivityManager.RunningTaskInfo ar = RunningTask.get(0);
        if(ar.topActivity.getClassName().toString()
        		.equals("com.cognizant.trumobi.em.activity.setup.EmAccountSecurity"))
        	return true;
        
        return false;
    }

}
