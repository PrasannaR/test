package com.cognizant.trumobi.persona;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.TruBoxSDK.SharedPreferences;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseActivity;
import com.cognizant.trumobi.log.PersonaLog;

public class PersonaUserCertificateWarningActivity extends TruMobiBaseActivity {
	Button okButton;
	TextView certificateAlertTextView;
	boolean isFirstTimeLaunch;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.pr_certificate_warning_screen);
		certificateAlertTextView = (TextView) findViewById(R.id.certificateAlertTextView);
		okButton = (Button) findViewById(R.id.okButton);
		okButton.setVisibility(View.VISIBLE);
		int warningMessage = getIntent().getIntExtra("warningMessageCode", 0);

		PersonaLog.e("Warning Message", "" + warningMessage);
		isFirstTimeLaunch = new SharedPreferences(this).getBoolean(
				"isFirstTimeLaunch", true);
		userWarningMessage(warningMessage);

		

		okButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {

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
		default:
			certificateAlertTextView.setText(getResources().getString(
					R.string.error_certificate_download));
		}

	}

}
