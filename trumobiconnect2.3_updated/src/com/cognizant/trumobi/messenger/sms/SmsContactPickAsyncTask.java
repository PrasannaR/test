package com.cognizant.trumobi.messenger.sms;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;

import com.cognizant.trubox.contacts.db.ContactsConsts;
import com.cognizant.trumobi.messenger.db.SmsHistoryBackup;

public class SmsContactPickAsyncTask extends
		AsyncTask<String, String, ArrayList<SmsContactBean>> {
	Context context;

	public SmsContactPickAsyncTask(Context context) {
		// TODO Auto-generated constructor stub
		this.context = context;
	}

	@Override
	protected ArrayList<SmsContactBean> doInBackground(String... arg0) {
		// TODO Auto-generated method stub
		ArrayList<SmsContactBean> contacts = new ArrayList<SmsContactBean>();
		try {
			Cursor phones = context.getContentResolver().query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
					ContactsContract.CommonDataKinds.Phone.NUMBER + "=? ",
					new String[] { arg0[0].toString() }, null);
			Cursor coporateContact = context.getContentResolver().query(
					ContactsConsts.CONTENT_URI_CONTACTS, null,
					ContactsConsts.CONTACT_PHONE + " LIKE ?",
					new String[] { "'%" + arg0[0].toString() + "%'" }, null);
			if (coporateContact.getCount() >= 1) {
				while (coporateContact.moveToNext()) {
					ArrayList<HashMap<String, String>> phoneVal = new ArrayList<HashMap<String, String>>();
					String name = coporateContact.getString(coporateContact
							.getColumnIndex(ContactsConsts.CONTACT_FIRST_NAME));
					String phone_details = coporateContact
							.getString(coporateContact
									.getColumnIndex(ContactsConsts.CONTACT_PHONE));
					String[] splitParentStr = phone_details.split(":");
					for (int i = 0; i < splitParentStr.length; i++) {
						if (splitParentStr[i].contains("=")) {
							String[] phone_array = splitParentStr[i].split("=");
							HashMap<String, String> row = new HashMap<String, String>();
							row.put("phonetype" + i, phone_array[0]);
							row.put("phoneNumber" + i, phone_array[1]);
							phoneVal.add(row);
						}
					}
					String phoneNumber = null;
					String numberType = null;
					for (int i = 0; i < phoneVal.size(); i++) {
						if (i == 0) {
							phoneNumber = phoneVal.get(i)
									.get("phoneNumber" + i);
							numberType = phoneVal.get(i).get("phonetype" + i);
						}

					}
					SmsContactBean corporateContact = new SmsContactBean();
					corporateContact.setName(name);
					corporateContact.setPhoneNo(phoneNumber);
					corporateContact.setCorporate(true);
					contacts.add(corporateContact);
				}
			} else if (phones.getCount() >= 1) {
				while (phones.moveToNext()) {
					String name = phones
							.getString(phones
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

					String phoneNumber = phones
							.getString(phones
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					String numberType = phones
							.getString(phones
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
					SmsContactBean nativeContact = new SmsContactBean();
					nativeContact.setName(name);
					nativeContact.setPhoneNo(phoneNumber);
					nativeContact.setType(numberType);
					nativeContact.setCorporate(false);
					contacts.add(nativeContact);
				}
			} else {
				SmsContactBean unKnownContact = new SmsContactBean();
				unKnownContact.setName(arg0[0].toString());
				unKnownContact.setPhoneNo(arg0[0].toString());
				unKnownContact.setType(null);
				unKnownContact.setCorporate(false);
				contacts.add(unKnownContact);
			}
			coporateContact.close();
			phones.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return contacts;
	}

	@Override
	protected void onPostExecute(ArrayList<SmsContactBean> contactDetails) {
		// TODO Auto-generated method stub
		ContentResolver cr = context.getContentResolver();
		if (contactDetails != null) {
			for (SmsContactBean contact : contactDetails) {
				ContentValues corporateFlag = new ContentValues();
				corporateFlag.put("corporate_contact", contactDetails.get(0)
						.isCorporate());
				Cursor backupCursor = cr.query(
						SmsHistoryBackup.CONTENT_URI_HISTORY_BACKUP, null,
						SmsHistoryBackup.PHONE_NUMBER + "=?",
						new String[] { contactDetails.get(0).getPhoneNo()
								.toString() }, null);
				int cursorSize = backupCursor.getCount();
				if (cursorSize >= 1) {
					cr.update(SmsHistoryBackup.CONTENT_URI_HISTORY_BACKUP,
							corporateFlag,
							SmsHistoryBackup.PHONE_NUMBER + "=?",
							new String[] { contactDetails.get(0).getPhoneNo()
									.toString() });
				}
				backupCursor.close();
			}

		}

	}
}
