package com.quintech.connect.activities;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseActivity;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
/**
 * 
 * @author rudhra changed the extending activity for auto lock
 *
 */
public class MainActivity extends TruMobiBaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pr_activity_main);
	}

	//@Override
	/*public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}*/

}
