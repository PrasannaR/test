package com.cognizant.trumobi.contacts.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseSherlockActivity;

public class ContactsGeneralSettingsDisplay extends TruMobiBaseSherlockActivity {

	// public TextView t;
	public TextView tv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts_gsettingsdisplay);
		this.setTitle("Display options");
		getSherlock().getActionBar().setDisplayHomeAsUpEnabled(false);

		tv = (TextView) findViewById(R.id.Sortlistby);
		tv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Log.i("inside textview click", "inside textview click");

				alt();

				/*
				 * //good working final CharSequence[] PhoneModels =
				 * {"First name","last name"}; AlertDialog.Builder alt_bld = new
				 * AlertDialog.Builder(this);
				 * 
				 * 
				 * 
				 * 
				 * //alt_bld.setIcon(R.drawable.icon);
				 * alt_bld.setTitle("Sort list by");
				 * alt_bld.setSingleChoiceItems(PhoneModels, -1, new
				 * DialogInterface.OnClickListener() { public void
				 * onClick(DialogInterface dialog, int item) {
				 * Toast.makeText(getApplicationContext(),
				 * "Selected radio button = "+PhoneModels[item],
				 * Toast.LENGTH_SHORT).show(); } }); AlertDialog alert =
				 * alt_bld.create(); alert.show();
				 */

				/*
				 * CharSequence[] PhoneModels = {"First name","last name"}; //
				 * AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
				 * 
				 * AlertDialog alt_bld = new
				 * AlertDialog.Builder(gsettingsdisplay.this).create();
				 * 
				 * 
				 * //alt_bld.setIcon(R.drawable.icon);
				 * alt_bld.setTitle("Sort list by"); //
				 * alt_bld.setSingleChoiceItems(PhoneModels, -1, new
				 * DialogInterface.OnClickListener() {
				 * 
				 * 
				 * alt_bld.setItems(PhoneModels, new
				 * DialogInterface.OnClickListener(){
				 * 
				 * public void onClick(DialogInterface dialog, int item) {
				 * Toast.makeText(getApplicationContext(),
				 * "Selected radio button = "+PhoneModels[item],
				 * Toast.LENGTH_SHORT).show(); } }); AlertDialog alert =
				 * alt_bld.create(); alert.show();
				 */

			}
		});

		tv = (TextView) findViewById(R.id.Viewcontactnamesas);
		tv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Log.i("inside textview click", "inside textview click");

				alt2();

			}
		});

	}

	public void onClick(View arg0) {
		// t.setText("My text on click");

	}

	public void alt() {

		// good working
		final CharSequence[] PhoneModels = { "First name", "last name" };
		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);

		// alt_bld.setIcon(R.drawable.icon);
		alt_bld.setTitle("Sort list by");
		alt_bld.setSingleChoiceItems(PhoneModels, -1,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						Toast.makeText(getApplicationContext(),
								"Selected radio button = " + PhoneModels[item],
								Toast.LENGTH_SHORT).show();
					}
				});
		AlertDialog alert = alt_bld.create();
		alert.show();

	}

	public void alt2() {

		// good working
		final CharSequence[] PhoneModels = { "First name first",
				"last name first" };
		AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);

		// alt_bld.setIcon(R.drawable.icon);
		alt_bld.setTitle("View contact names as");
		alt_bld.setSingleChoiceItems(PhoneModels, -1,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						Toast.makeText(getApplicationContext(),
								"Selected radio button = " + PhoneModels[item],
								Toast.LENGTH_SHORT).show();
					}
				});
		AlertDialog alert = alt_bld.create();
		alert.show();

	}

	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) { // Inflate the
	 * menu; this adds items to the action bar if it is present.
	 * getMenuInflater().inflate(R.menu.main, menu); return true; }
	 */

}
