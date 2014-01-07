package com.cognizant.trumobi.persona;

import java.net.URI;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.TruBoxSDK.SharedPreferences;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseActivity;
import com.cognizant.trumobi.log.PersonaLog;
import com.cognizant.trumobi.persona.constants.PersonaCertfifcateAuthConstants;

public class PersonaUserCertificateWarningActivity extends TruMobiBaseActivity {
	Button okButton;
	TextView certificateAlertTextView;
	boolean isFirstTimeLaunch;
	int warningMessage ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.pr_certificate_warning_screen);
		certificateAlertTextView = (TextView) findViewById(R.id.certificateAlertTextView);
		okButton = (Button) findViewById(R.id.okButton);
		okButton.setVisibility(View.VISIBLE);
		 warningMessage = getIntent().getIntExtra("warningMessageCode", 0);

		PersonaLog.e("Warning Message", "" + warningMessage);
		isFirstTimeLaunch = new SharedPreferences(this).getBoolean(
				"isFirstTimeLaunch", true);
		userWarningMessage(warningMessage);

		okButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				if(warningMessage==PersonaCertfifcateAuthConstants.TRUHUB_NOT_INSTALLED)
				{
					Intent launchBrowerIntent=new Intent(Intent.ACTION_VIEW,Uri.parse(PersonaCertfifcateAuthConstants.STAGING+"LaunchPad"));
					
					startActivity(launchBrowerIntent);
					finish();
				}
					
				finish();
			}
		});
	}

	private void userWarningMessage(int messageCode) {
		switch (messageCode) {

		case 2001:
			// need to be updated once the message is finalized

			if (isFirstTimeLaunch) {
				new SharedPreferences(this).edit()
						.putBoolean("isFirstTimeLaunch", false).commit();
				certificateAlertTextView.setText(getResources().getString(
						R.string.certificate_warning_message));
			} else
				certificateAlertTextView.setText(getResources().getString(
						R.string.certificate_warning_message_later));
			break;
		case 2005:
			certificateAlertTextView.setText(getResources().getString(
					R.string.try_again));
			break;
		case PersonaCertfifcateAuthConstants.TRUHUB_NOT_INSTALLED:
			certificateAlertTextView.setText(getString(R.string.pr_TruHub_Not_Installed,PersonaCertfifcateAuthConstants.STAGING+"LaunchPad"));
		break;
		default:
			certificateAlertTextView.setText(getResources().getString(
					R.string.error_certificate_download));
		}

	}

}
