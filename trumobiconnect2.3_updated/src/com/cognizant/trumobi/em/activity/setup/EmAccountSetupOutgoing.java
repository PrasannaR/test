

package com.cognizant.trumobi.em.activity.setup;

import java.net.URI;
import java.net.URISyntaxException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobBaseEditText;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseActivity;
import com.cognizant.trumobi.em.EmAccountBackupRestore;
import com.cognizant.trumobi.em.EmUtility;
import com.cognizant.trumobi.em.provider.EmEmailContent;

public class EmAccountSetupOutgoing extends TruMobiBaseActivity implements OnClickListener,
        OnCheckedChangeListener {
    private static final String EXTRA_ACCOUNT = "account";

    private static final String EXTRA_MAKE_DEFAULT = "makeDefault";

    private static final int SMTP_PORTS[] = {
            587, 465, 465, 587, 587
    };

    private static final String SMTP_SCHEMES[] = {
            "smtp", "smtp+ssl+", "smtp+ssl+trustallcerts", "smtp+tls+", "smtp+tls+trustallcerts"
    };

    private TruMobBaseEditText mUsernameView;
    private TruMobBaseEditText mPasswordView;
    private TruMobBaseEditText mServerView;
    private TruMobBaseEditText mPortView;
    private CheckBox mRequireLoginView;
    private ViewGroup mRequireLoginSettingsView;
    private Spinner mSecurityTypeView;
    private Button mNextButton;
    private EmEmailContent.Account mAccount;
    private boolean mMakeDefault;

    public static void actionOutgoingSettings(Activity fromActivity, EmEmailContent.Account account, 
            boolean makeDefault) {
        Intent i = new Intent(fromActivity, EmAccountSetupOutgoing.class);
        i.putExtra(EXTRA_ACCOUNT, account);
        i.putExtra(EXTRA_MAKE_DEFAULT, makeDefault);
        fromActivity.startActivity(i);
    }

    public static void actionEditOutgoingSettings(Activity fromActivity, EmEmailContent.Account account)
            {
        Intent i = new Intent(fromActivity, EmAccountSetupOutgoing.class);
        i.setAction(Intent.ACTION_EDIT);
        i.putExtra(EXTRA_ACCOUNT, account);
        fromActivity.startActivity(i);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);	//NaGa, adding title Bar
        setContentView(R.layout.em_account_setup_outgoing);
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.em_customtitlebar);

        mUsernameView = (TruMobBaseEditText)findViewById(R.id.account_username);
        mPasswordView = (TruMobBaseEditText)findViewById(R.id.account_password);
        mServerView = (TruMobBaseEditText)findViewById(R.id.account_server);
        mPortView = (TruMobBaseEditText)findViewById(R.id.account_port);
        mRequireLoginView = (CheckBox)findViewById(R.id.account_require_login);
        mRequireLoginSettingsView = (ViewGroup)findViewById(R.id.account_require_login_settings);
        mSecurityTypeView = (Spinner)findViewById(R.id.account_security_type);
        mNextButton = (Button)findViewById(R.id.next);

        mNextButton.setOnClickListener(this);
        mRequireLoginView.setOnCheckedChangeListener(this);

        EmSpinnerOption securityTypes[] = {
            new EmSpinnerOption(0, getString(R.string.account_setup_incoming_security_none_label)),
            new EmSpinnerOption(1, getString(R.string.account_setup_incoming_security_ssl_label)),
            new EmSpinnerOption(2, getString(
                    R.string.account_setup_incoming_security_ssl_trust_certificates_label)),
            new EmSpinnerOption(3, getString(R.string.account_setup_incoming_security_tls_label)),
            new EmSpinnerOption(4, getString(
                    R.string.account_setup_incoming_security_tls_trust_certificates_label)),
        };

        ArrayAdapter<EmSpinnerOption> securityTypesAdapter = new ArrayAdapter<EmSpinnerOption>(this,
                android.R.layout.simple_spinner_item, securityTypes);
        securityTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSecurityTypeView.setAdapter(securityTypesAdapter);

        /*
         * Updates the port when the user changes the security type. This allows
         * us to show a reasonable default which the user can change.
         */
        mSecurityTypeView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView arg0, View arg1, int arg2, long arg3) {
                updatePortFromSecurityType();
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        /*
         * Calls validateFields() which enables or disables the Next button
         * based on the fields' validity.
         */
        TextWatcher validationTextWatcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {
                validateFields();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        };
        mUsernameView.addTextChangedListener(validationTextWatcher);
        mPasswordView.addTextChangedListener(validationTextWatcher);
        mServerView.addTextChangedListener(validationTextWatcher);
        mPortView.addTextChangedListener(validationTextWatcher);

        /*
         * Only allow digits in the port field.
         */
        mPortView.setKeyListener(DigitsKeyListener.getInstance("0123456789"));

        mAccount = (EmEmailContent.Account)getIntent().getParcelableExtra(EXTRA_ACCOUNT);
        mMakeDefault = getIntent().getBooleanExtra(EXTRA_MAKE_DEFAULT, false);

        /*
         * If we're being reloaded we override the original account with the one
         * we saved
         */
        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_ACCOUNT)) {
            mAccount = (EmEmailContent.Account)savedInstanceState.getParcelable(EXTRA_ACCOUNT);
        }

        try {
            // TODO this should be accessed directly via the HostAuth structure
            URI uri = new URI(mAccount.getSenderUri(this));
            String username = null;
            String password = null;
            if (uri.getUserInfo() != null) {
                String[] userInfoParts = uri.getUserInfo().split(":", 2);
                username = userInfoParts[0];
                if (userInfoParts.length > 1) {
                    password = userInfoParts[1];
                }
            }

            if (username != null) {
                mUsernameView.setText(username);
                mRequireLoginView.setChecked(true);
            }

            if (password != null) {
                mPasswordView.setText(password);
            }

            for (int i = 0; i < SMTP_SCHEMES.length; i++) {
                if (SMTP_SCHEMES[i].equals(uri.getScheme())) {
                    EmSpinnerOption.setSpinnerOptionValue(mSecurityTypeView, i);
                }
            }

            if (uri.getHost() != null) {
                mServerView.setText(uri.getHost());
            }

            if (uri.getPort() != -1) {
                mPortView.setText(Integer.toString(uri.getPort()));
            } else {
                updatePortFromSecurityType();
            }
        } catch (URISyntaxException use) {
            /*
             * We should always be able to parse our own settings.
             */
            throw new Error(use);
        }

        validateFields();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_ACCOUNT, mAccount);
    }

    /**
     * Preflight the values in the fields and decide if it makes sense to enable the "next" button
     * NOTE:  Does it make sense to extract & combine with similar code in AccountSetupIncoming? 
     */
    private void validateFields() {
        boolean enabled = 
            EmUtility.requiredFieldValid(mServerView) 
                && EmUtility.isPortFieldValid(mPortView);

        if (enabled && mRequireLoginView.isChecked()) {
            enabled = (EmUtility.requiredFieldValid(mUsernameView)
                    && EmUtility.requiredFieldValid(mPasswordView));
        }

        if (enabled) {
            try {
                URI uri = getUri();
            } catch (URISyntaxException use) {
                enabled = false;
            }
        }
        mNextButton.setEnabled(enabled);
        EmUtility.setCompoundDrawablesAlpha(mNextButton, enabled ? 255 : 128);
    }

    private void updatePortFromSecurityType() {
        int securityType = (Integer)((EmSpinnerOption)mSecurityTypeView.getSelectedItem()).value;
        mPortView.setText(Integer.toString(SMTP_PORTS[securityType]));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (Intent.ACTION_EDIT.equals(getIntent().getAction())) {
                if (mAccount.isSaved()) {
                    mAccount.update(this, mAccount.toContentValues());
                    mAccount.mHostAuthSend.update(this, mAccount.mHostAuthSend.toContentValues());
                } else {
                    mAccount.save(this);
                }
                // Update the backup (side copy) of the accounts
                EmAccountBackupRestore.backupAccounts(this);
                finish();
            } else {
                EmAccountSetupOptions.actionOptions(this, mAccount, mMakeDefault, false);
                finish();
            }
        }
    }
    
    /**
     * Attempt to create a URI from the fields provided.  Throws URISyntaxException if there's 
     * a problem with the user input.
     * @return a URI built from the account setup fields
     */
    /* package */ URI getUri() throws URISyntaxException {
        int securityType = (Integer)((EmSpinnerOption)mSecurityTypeView.getSelectedItem()).value;
        String userInfo = null;
        if (mRequireLoginView.isChecked()) {
            userInfo = mUsernameView.getText().toString().trim() + ":" + mPasswordView.getText();
        }
        URI uri = new URI(
                SMTP_SCHEMES[securityType],
                userInfo,
                mServerView.getText().toString().trim(),
                Integer.parseInt(mPortView.getText().toString().trim()),
                null, null, null);
        
        return uri;
    }

    private void onNext() {       
        try {
            // TODO this should be accessed directly via the HostAuth structure
            URI uri = getUri();
            mAccount.setSenderUri(this, uri.toString());
        } catch (URISyntaxException use) {
            /*
             * It's unrecoverable if we cannot create a URI from components that
             * we validated to be safe.
             */
            throw new Error(use);
        }
        EmAccountSetupCheckSettings.actionValidateSettings(this, mAccount, false, true);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.next:
                onNext();
                break;
        }
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mRequireLoginSettingsView.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        validateFields();
    }
}
