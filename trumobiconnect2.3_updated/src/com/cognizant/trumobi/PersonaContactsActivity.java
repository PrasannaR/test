package com.cognizant.trumobi;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.cognizant.trumobi.R;

public class PersonaContactsActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pr_persona_home_screen);
		TextView textView=(TextView)findViewById(R.id.textView1);
		textView.setText("Contacts");
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
}
