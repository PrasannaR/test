package com.cognizant.trumobi.persona;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.cognizant.trumobi.R;

public class PersonaUserCertificateWarningActivity extends Activity{
	Button okButton;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pr_certificate_warning_screen);
		okButton=(Button)findViewById(R.id.okButton);
		okButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View view) {
				
				finish();
			}
		});
	}
}
