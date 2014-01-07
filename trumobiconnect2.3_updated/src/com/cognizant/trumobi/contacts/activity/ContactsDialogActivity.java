package com.cognizant.trumobi.contacts.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.view.Window;

import com.TruBoxSDK.SharedPreferences;
import com.cognizant.trubox.contacts.db.ContactsModel;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseActivity;
import com.cognizant.trumobi.contacts.utils.ContactsDialog;
import com.cognizant.trumobi.em.Email;

public class ContactsDialogActivity extends TruMobiBaseActivity {

	ContactsDialog contactsDialog;
	ContactsModel contactModel;
	Dialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		if (savedInstanceState == null) {
			Bundle bundle = getIntent().getExtras().getBundle("bundle");
			contactModel = (ContactsModel) bundle.getSerializable("contact");
		} else {

			contactModel = (ContactsModel) savedInstanceState
					.getSerializable("contact");
		}
		contactsDialog = new ContactsDialog(this, contactModel);
		dialog = contactsDialog.getDialog();

		dialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				
				SharedPreferences.Editor prefEditor = new SharedPreferences(
						Email.getAppContext()).edit();
				prefEditor.putBoolean("isPrefUpdated", false);
				prefEditor.commit();
				
				
				
				dialog.dismiss();
				finish();
			}
		});
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("contact", contactModel);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		
		
		
		
		
		
		

		
		SharedPreferences.Editor prefEditor = new SharedPreferences(
				Email.getAppContext()).edit();
		prefEditor.putBoolean("isPrefUpdated", false);
		prefEditor.commit();
		
		
		
		
		
		
		
		
		dialog.dismiss();
	}

}
