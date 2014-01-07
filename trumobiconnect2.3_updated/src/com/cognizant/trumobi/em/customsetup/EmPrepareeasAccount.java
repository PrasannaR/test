package com.cognizant.trumobi.em.customsetup;

import java.net.URI;
import java.net.URISyntaxException;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseActivity;
import com.cognizant.trumobi.em.activity.setup.EmAccountSettingsUtils;
import com.cognizant.trumobi.em.activity.setup.EmAccountSettingsUtils.Provider;
import com.cognizant.trumobi.em.provider.EmEmailContent;
import com.cognizant.trumobi.em.provider.EmEmailContent.Account;

public class EmPrepareeasAccount extends TruMobiBaseActivity{

	private static String email = null;
	private static String domain = null;
	private EmEmailContent.Account mAccount;
    private Provider mProvider;
    private boolean mEasFlowMode;
    private String mDuplicateAccountName;
    private final static int DEFAULT_ACCOUNT_CHECK_INTERVAL = 15;
    private final static String EXTRA_ACCOUNT = "com.cognizant.trumobi.em.AccountSetupBasics.account";
    public final static String EXTRA_EAS_FLOW = "com.cognizant.trumobi.em.extra.eas_flow";
    private final static String STATE_KEY_PROVIDER =
            "com.cognizant.trumobi.em.AccountSetupBasics.provider";
	
    private Button mCancelButton;
    public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
    	super.onCreate(savedInstanceState);
    	requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.em_account_setup_check_settings);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.em_customtitlebar);
    	
    	 mCancelButton = (Button) findViewById(R.id.cancel);
         mCancelButton.setVisibility(View.INVISIBLE);
    	
    	/*Intent intent = getIntent();
    	
    	mEasFlowMode = intent.getBooleanExtra(EXTRA_EAS_FLOW, false);
    	if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_ACCOUNT)) {
            mAccount = (EmailContent.Account)savedInstanceState.getParcelable(EXTRA_ACCOUNT);
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_KEY_PROVIDER)) {
            mProvider = (Provider)savedInstanceState.getSerializable(STATE_KEY_PROVIDER);
        }
        setupAccount();*/
    	EmAccountSetupBasics.actionNewAccount(this);
    	finish();
	}
    
	public void setupAccount()
	{
		email = "geetha@truboxmdmdev.com";
		domain = "truboxmdmdev.com";
		mProvider = EmAccountSettingsUtils.findProviderForDomain(this, domain);
		onManualSetup(false);
	}
	private void onManualSetup(boolean allowAutoDiscover) {
       
        String[] emailParts = email.split("@");
        String user = emailParts[0].trim();
        String domain = emailParts[1].trim();

      
        mAccount = new EmEmailContent.Account();
        mAccount.setSenderName(getOwnerName());
        mAccount.setEmailAddress(email);
        try {
            URI uri = new URI("placeholder", user + ":" + "", domain, -1, null, null, null);
            mAccount.setStoreUri(this, uri.toString());
            mAccount.setSenderUri(this, uri.toString());
        } catch (URISyntaxException use) {
            // If we can't set up the URL, don't continue - account setup pages will fail too
            Toast.makeText(this, R.string.account_setup_username_password_toast, Toast.LENGTH_LONG)
                    .show();
            mAccount = null;
            return;
        }

        mAccount.setSyncInterval(DEFAULT_ACCOUNT_CHECK_INTERVAL);
        mEasFlowMode = true;
        //AccountSetupAccountType.actionSelectAccountType(this, mAccount, true,
                //mEasFlowMode, allowAutoDiscover);
        onExchange(true);
    }
	
	
	private void onExchange(boolean easFlowMode) {
        try {
            URI uri = new URI(mAccount.getStoreUri(this));
            uri = new URI("eas+ssl+", uri.getUserInfo(), uri.getHost(), uri.getPort(),
                    null, null, null);
            mAccount.setStoreUri(this, uri.toString());
            mAccount.setSenderUri(this, uri.toString());
        } catch (URISyntaxException use) {
            /*
             * This should not happen.
             */
            throw new Error(use);
        }
        // TODO: Confirm correct delete policy for exchange
        mAccount.setDeletePolicy(Account.DELETE_POLICY_ON_DELETE);
        mAccount.setSyncInterval(Account.CHECK_INTERVAL_PUSH);
        mAccount.setSyncLookback(1);
        //AccountSetupCheckSettings.actionValidateSettings(this, mAccount, true, false);
    }
	
	private String getOwnerName() {
        String name = null;
        if (name == null || name.length() == 0) {
            long defaultId = Account.getDefaultAccountId(this);
            if (defaultId != -1) {
                Account account = Account.restoreAccountWithId(this, defaultId);
                if (account != null) {
                    name = account.getSenderName();
                }
            }
        }
        return name;
    }
}
