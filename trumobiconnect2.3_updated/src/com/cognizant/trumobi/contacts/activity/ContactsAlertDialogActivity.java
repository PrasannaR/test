package com.cognizant.trumobi.contacts.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.cognizant.trumobi.commonabstractclass.TruMobiBaseActivity;

public class ContactsAlertDialogActivity extends TruMobiBaseActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();

		if (intent != null) {

			String title = intent.getStringExtra("title");
			String msg = intent.getStringExtra("msg");

			showDialog(title, msg);
		}
	}

	void showDialog(String title, String msg) {

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// set title
		alertDialogBuilder.setTitle(title);

		// set dialog message
		alertDialogBuilder.setMessage(msg).setCancelable(true)
				.setNeutralButton("Ok", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();

					}
				});
		alertDialogBuilder
				.setOnCancelListener(new DialogInterface.OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
						// TODO Auto-generated method stub
						finish();
					}
				});
		// .setPositiveButton("Yes",
		// new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog, int id) {
		// // if this button is clicked, close
		// // current activity
		// AlertDialogActivity.this.finish();
		// }
		// })
		// .setNegativeButton("No", new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog, int id) {
		// // if this button is clicked, just close
		// // the dialog box and do nothing
		// dialog.cancel();
		// }
		// });

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();

	}
}
