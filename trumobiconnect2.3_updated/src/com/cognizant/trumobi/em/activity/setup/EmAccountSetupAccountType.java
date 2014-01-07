

package com.cognizant.trumobi.em.activity.setup;

import java.net.URI;
import java.net.URISyntaxException;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseActivity;
import com.cognizant.trumobi.em.mail.EmStore;
import com.cognizant.trumobi.em.provider.EmEmailContent;
import com.cognizant.trumobi.em.provider.EmEmailContent.Account;

/**
 * Prompts the user to select an account type. The account type, along with the
 * passed in email address, password and makeDefault are then passed on to the
 * AccountSetupIncoming activity.
 */
public class EmAccountSetupAccountType extends TruMobiBaseActivity implements OnClickListener {

    private static final String EXTRA_ACCOUNT = "account";
    private static final String EXTRA_MAKE_DEFAULT = "makeDefault";
    private static final String EXTRA_EAS_FLOW = "easFlow";
    private static final String EXTRA_ALLOW_AUTODISCOVER = "allowAutoDiscover";

    private Account mAccount;
    private boolean mMakeDefault;
    private boolean mAllowAutoDiscover;

    public static void actionSelectAccountType(Activity fromActivity, Account account,
            boolean makeDefault, boolean easFlowMode, boolean allowAutoDiscover) {
        Intent i = new Intent(fromActivity, EmAccountSetupAccountType.class);
        i.putExtra(EXTRA_ACCOUNT, account);
        i.putExtra(EXTRA_MAKE_DEFAULT, makeDefault);
        i.putExtra(EXTRA_EAS_FLOW, easFlowMode);
        i.putExtra(EXTRA_ALLOW_AUTODISCOVER, allowAutoDiscover);
        fromActivity.startActivity(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mAccount = (Account) intent.getParcelableExtra(EXTRA_ACCOUNT);
        mMakeDefault = intent.getBooleanExtra(EXTRA_MAKE_DEFAULT, false);
        boolean easFlowMode = intent.getBooleanExtra(EXTRA_EAS_FLOW, false);
        mAllowAutoDiscover = intent.getBooleanExtra(EXTRA_ALLOW_AUTODISCOVER, true);

        // If we're in account setup flow mode, for EAS, skip this screen and "click" EAS
        //if (easFlowMode) 
        {
            onExchange(true);
            return;
        }

        // Otherwise proceed into this screen
        /*requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);	//NaGa, adding title Bar
        setContentView(R.layout.em_account_setup_account_type);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.em_customtitlebar);
        ((Button)findViewById(R.id.pop)).setOnClickListener(this);
        ((Button)findViewById(R.id.imap)).setOnClickListener(this);
        final Button exchangeButton = ((Button)findViewById(R.id.exchange));
        exchangeButton.setOnClickListener(this);*/

        /*if (isExchangeAvailable()) {	//NaGa, removing exchange button
            exchangeButton.setVisibility(View.VISIBLE);
            if (VendorPolicyLoader.getInstance(this).useAlternateExchangeStrings()) {
                exchangeButton.setText(
                        R.string.account_setup_account_type_exchange_action_alternate);
            }
        }*/
        // TODO: Dynamic creation of buttons, instead of just hiding things we don't need
    }

    private void onPop() {
        try {
            URI uri = new URI(mAccount.getStoreUri(this));
            uri = new URI("pop3", uri.getUserInfo(), uri.getHost(), uri.getPort(), null, null, null);
            mAccount.setStoreUri(this, uri.toString());
        } catch (URISyntaxException use) {
            /*
             * This should not happen.
             */
            throw new Error(use);
        }
        EmAccountSetupIncoming.actionIncomingSettings(this, mAccount, mMakeDefault);
        finish();
    }

    /**
     * The user has selected an IMAP account type.  Try to put together a URI using the entered
     * email address.  Also set the mail delete policy here, because there is no UI (for IMAP).
     */
    private void onImap() {
        try {
            URI uri = new URI(mAccount.getStoreUri(this));
            uri = new URI("imap", uri.getUserInfo(), uri.getHost(), uri.getPort(), null, null, null);
            mAccount.setStoreUri(this, uri.toString());
        } catch (URISyntaxException use) {
            /*
             * This should not happen.
             */
            throw new Error(use);
        }
        // Delete policy must be set explicitly, because IMAP does not provide a UI selection
        // for it. This logic needs to be followed in the auto setup flow as well.
        mAccount.setDeletePolicy(Account.DELETE_POLICY_ON_DELETE);
        EmAccountSetupIncoming.actionIncomingSettings(this, mAccount, mMakeDefault);
        finish();
    }

    /**
     * The user has selected an exchange account type.  Try to put together a URI using the entered
     * email address.  Also set the mail delete policy here, because there is no UI (for exchange),
     * and switch the default sync interval to "push".
     */
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
        EmAccountSetupExchange.actionIncomingSettings(this, mAccount, mMakeDefault, easFlowMode,
                mAllowAutoDiscover);
        finish();
    }

    /**
     * Determine if we can show the "exchange" option
     *
     * TODO: This should be dynamic and data-driven for all account types, not just hardcoded
     * like this.
     */
    private boolean isExchangeAvailable() {
        //EXCHANGE-REMOVE-SECTION-START
        try {
            URI uri = new URI(mAccount.getStoreUri(this));
            uri = new URI("eas", uri.getUserInfo(), uri.getHost(), uri.getPort(), null, null, null);
            EmStore.StoreInfo storeInfo = EmStore.StoreInfo.getStoreInfo(uri.toString(), this);
            return (storeInfo != null && checkAccountInstanceLimit(storeInfo));
        } catch (URISyntaxException e) {
        }
        //EXCHANGE-REMOVE-SECTION-END
        return false;
    }

    /**
     * If the optional store specifies a limit on the number of accounts, make sure that we
     * don't violate that limit.
     * @return true if OK to create another account, false if not OK (limit reached)
     */
    /* package */ boolean checkAccountInstanceLimit(EmStore.StoreInfo storeInfo) {
        // return immediately if account defines no limit
        if (storeInfo.mAccountInstanceLimit < 0) {
            return true;
        }

        // count existing accounts
        int currentAccountsCount = 0;
        Cursor c = null;
        try {
            c = this.getContentResolver().query(
                    Account.CONTENT_URI,
                    Account.CONTENT_PROJECTION,
                    null, null, null);
            while (c.moveToNext()) {
                Account account = EmEmailContent.getContent(c, Account.class);
                String storeUri = account.getStoreUri(this);
                if (storeUri != null && storeUri.startsWith(storeInfo.mScheme)) {
                    currentAccountsCount++;
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        // return true if we can accept another account
        return (currentAccountsCount < storeInfo.mAccountInstanceLimit);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pop:
                onPop();
                break;
            case R.id.imap:
                onImap();
                break;
            case R.id.exchange:
                onExchange(false);
                break;
        }
    }
}
