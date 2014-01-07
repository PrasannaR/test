package com.cognizant.trumobi.messenger.sms;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.InputMismatchException;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.TruBoxSDK.TruBoxDatabase;
import com.cognizant.trubox.contacts.db.ContactsConsts;
import com.cognizant.trumobi.messenger.db.SmsHistoryBackup;
import com.cognizant.trumobi.messenger.db.SmsHistoryTable;
import com.cognizant.trumobi.messenger.db.SmsIndividualTable;
import com.cognizant.trumobi.R;

public class SmsReceiver extends BroadcastReceiver {
	Uri uriIndividualInsert;
	Uri uriHistroyInsert;
	StringBuilder smsBody = new StringBuilder();
	String strBdy = "";

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equalsIgnoreCase(
				"android.provider.Telephony.SMS_RECEIVED")
				&& TruBoxDatabase.isPasswordGenerated()) {
			// ---get the SMS message passed in---
			Bundle bundle = intent.getExtras();
			SmsMessage[] msgs = null;
			// String strBdy = "";
			String strNum = "";
			String orginalNumber = null;
			// StringBuilder smsBody = new StringBuilder();
			if (bundle != null) {
				// ---retrieve the SMS message received---
				StringBuilder smsSummary = new StringBuilder(
						"SMS received from: ");
				Object[] pdus = (Object[]) bundle.get("pdus");
				msgs = new SmsMessage[pdus.length];
				for (int i = 0; i < msgs.length; i++) {
					msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
					strNum = msgs[i].getOriginatingAddress();
					strBdy = msgs[i].getMessageBody().toString();
					smsBody = smsBody.append(msgs[i].getMessageBody()
							.toString());
					smsSummary.append(strNum);
					smsSummary.append("; ").append(strBdy).append("\n");
					Log.d("SMS_RECEIVER", "Str is   " + smsSummary);
				}
				try {
					if (strNum.indexOf("+91") > -1) {
						orginalNumber = strNum.substring(3, strNum.length());
					} else {
						orginalNumber = strNum;
					}
				} catch (NumberFormatException nfe) {
					throw new InputMismatchException(nfe.getMessage());
				}

				/*
				 * NotificationManager notifManager = (NotificationManager)
				 * context .getSystemService(Context.NOTIFICATION_SERVICE);
				 * 
				 * Notification notif = new Notification(
				 * R.drawable.sms_messenger_icon, smsSummary,
				 * System.currentTimeMillis()); notif.defaults |=
				 * Notification.DEFAULT_SOUND; notif.defaults |=
				 * Notification.DEFAULT_VIBRATE; notif.defaults |=
				 * Notification.DEFAULT_LIGHTS;
				 * 
				 * // The notification will be canceled when clicked by the
				 * user...
				 * 
				 * notif.flags |= Notification.FLAG_AUTO_CANCEL;
				 * 
				 * // ...but we still need to provide and intent; an empty one
				 * will // suffice. Alter for your own app's requirement.
				 * 
				 * Intent notificationIntent = new Intent(context, SMS.class);
				 * notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				 * notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				 * notificationIntent.putExtra("number", orginalNumber);
				 * notificationIntent.putExtra("body", strBdy); PendingIntent pi
				 * = PendingIntent.getActivity(context, 0, notificationIntent,
				 * 0); notif.setLatestEventInfo(context, strNum, strBdy, pi);
				 * 
				 * 
				 * notifManager.notify(0, notif); Log.d("SMSMESSAGING",
				 * orginalNumber +"<>"+strBdy +"<>"+"recieved");
				 */

				byte[] picture = null;
				String name;
				int newMsg_count = 0;
				ContentResolver cr = context.getContentResolver();
				final SmsIndividualTable home = new SmsIndividualTable();
				final SmsHistoryTable detail = new SmsHistoryTable();
				final SmsHistoryBackup backup = new SmsHistoryBackup();
				// new PickContacts(context).execute(orginalNumber);
				home.setMessage(strBdy);
				home.setPhoneNumber(orginalNumber);
				home.setFirstName(strNum);

				long tim = System.currentTimeMillis();
				home.setTime(tim);

				String[] projection = { SmsIndividualTable.IMAGE,
						SmsIndividualTable.FIRST_NAME,
						SmsIndividualTable.UNREAD_COUNT };
				Cursor cursor = cr.query(
						SmsIndividualTable.CONTENT_URI_INDIVIDUAL, projection,
						SmsIndividualTable.PHONE_NUMBER + "=?",
						new String[] { home.getPhoneNumber() }, null);
				int cursorSize = cursor.getCount();
				if (cursor != null) {
					if (cursorSize == 1) {
						if (cursor.moveToFirst()) {
							picture = cursor.getBlob(cursor
									.getColumnIndex(SmsIndividualTable.IMAGE));
							name = cursor
									.getString(cursor
											.getColumnIndex(SmsIndividualTable.FIRST_NAME));
							newMsg_count = cursor
									.getInt(cursor
											.getColumnIndex(SmsIndividualTable.UNREAD_COUNT));
							if (newMsg_count == 0)
								home.setUnreadCount(1);
							else
								home.setUnreadCount(newMsg_count + 1);
							home.setFirstName(name);
							home.setImageByte(picture);
						}
						ContentValues updatedValue = new ContentValues();
						updatedValue = getHomeListValue(home);
						cr.update(SmsIndividualTable.CONTENT_URI_INDIVIDUAL,
								updatedValue, SmsIndividualTable.PHONE_NUMBER
										+ "=?",
								new String[] { home.getPhoneNumber() });

						detail.setFirstName(home.getFirstName());
						detail.setPhoneNumber(home.getPhoneNumber());
						detail.setMessage(home.getMessage());
						detail.setTime(home.getTime());
						detail.setContactImageByte(home.getImageByte());
						detail.setLocked(false);

						ContentValues backupTable = new ContentValues();
						backupTable = getBackupListValue(setBackupObject(home));

						ContentValues individual = new ContentValues();
						individual = getDetailListValue(detail);
						cr.insert(SmsHistoryTable.CONTENT_URI_HISTORY,
								individual);
						cr.insert(SmsHistoryBackup.CONTENT_URI_HISTORY_BACKUP,
								backupTable);
						new SmsContactPickAsyncTask(context)
								.execute(orginalNumber);
					} else {
						ContentValues homePageValue = new ContentValues();
						home.setImageByte(picture);
						home.setUnreadCount(1);
						homePageValue = getHomeListValue(home);
						cr.insert(SmsIndividualTable.CONTENT_URI_INDIVIDUAL,
								homePageValue);

						detail.setPhoneNumber(home.getPhoneNumber());
						detail.setFirstName(home.getFirstName());
						detail.setMessage(strBdy);
						detail.setTime(home.getTime());
						detail.setContactImageByte(home.getImageByte());
						detail.setLocked(false);

						ContentValues backupTable = new ContentValues();
						backupTable = getBackupListValue(setBackupObject(home));

						ContentValues detailPageValue = new ContentValues();
						detailPageValue = getDetailListValue(detail);
						cr.insert(SmsHistoryTable.CONTENT_URI_HISTORY,
								detailPageValue);
						cr.insert(SmsHistoryBackup.CONTENT_URI_HISTORY_BACKUP,
								backupTable);
						new SmsContactPickAsyncTask(context)
								.execute(orginalNumber);
					}
					cursor.close();
				}
			}
			LocalBroadcastManager.getInstance(context).sendBroadcast(
					new Intent("LoaclReciev"));
		}
	}

	private ContentValues getBackupListValue(SmsHistoryBackup backupObject) {
		// TODO Auto-generated method stub
		ContentValues backupValue = new ContentValues();
		backupValue.put("firstname", backupObject.getFirstName());
		backupValue.put("phonenumber", backupObject.getPhoneNumber());
		backupValue.put("message", backupObject.getMessage());
		backupValue.put("date", backupObject.getDate());
		backupValue.put("time", backupObject.getTime());
		backupValue.put("personimage", backupObject.getContacctImageByte());
		backupValue.put("is_send", backupObject.isSend());
		backupValue.put("lock", backupObject.getLocked());
		backupValue.put("corporate_contact", backupObject.getCoporateFlag());
		backupValue.put("msg_count", backupObject.getMsgCount());
		return backupValue;
	}

	private SmsHistoryBackup setBackupObject(SmsIndividualTable home) {
		// TODO Auto-generated method stub
		final SmsHistoryBackup backup = new SmsHistoryBackup();
		backup.setFirstName(home.getFirstName());
		backup.setPhoneNumber(home.getPhoneNumber());
		backup.setMessage(home.getMessage());
		SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy-hh:mm");
		String curTime = df.format(home.getTime());
		String[] splitDate = curTime.split("-");
		String mDate = splitDate[0];
		String mTime = splitDate[1];
		backup.setDate(mDate);
		backup.setTime(mTime);
		backup.setContactImageByte(home.getImageByte());
		backup.setLocked(false);
		backup.setSend(false);
		backup.setCorporateFlag(false);
		backup.setMsgCount(calculateMsgCount(home.getMessage()));
		return backup;
	}

	public ContentValues getHomeListValue(SmsIndividualTable home) {
		// TODO Auto-generated method stub
		ContentValues cv = new ContentValues();
		cv.put("phonenumber", home.getPhoneNumber());
		cv.put("firstname", home.getFirstName());
		cv.put("message", strBdy);
		cv.put("date", home.getDate());
		cv.put("time", home.getTime());
		cv.put("personimage", home.getImageByte());
		cv.put("unread_count", home.getUnreadCount());
		return cv;
	}

	public ContentValues getDetailListValue(SmsHistoryTable detail) {
		// TODO Auto-generated method stub
		ContentValues newValue = new ContentValues();
		newValue.put("firstname", detail.getFirstName());
		newValue.put("phonenumber", detail.getPhoneNumber());
		newValue.put("message", detail.getMessage());
		newValue.put("time", detail.getTime());
		newValue.put("personimage", detail.getContacctImageByte());
		newValue.put("send_receive", "recv");
		newValue.put("lock", detail.getLocked());
		return newValue;
	}

	public int calculateMsgCount(String message) {
		// TODO Auto-generated method stub
		int totalSize = message.length();
		int count = 0;
		if (totalSize <= SmsHistoryBackup.MSG_LENGTH)
			count = 1;
		else {
			int msgInt = totalSize / SmsHistoryBackup.MSG_LENGTH;
			int mod = totalSize % SmsHistoryBackup.MSG_LENGTH;
			if (mod == 0)
				count = msgInt;
			else
				count = msgInt + 1;
		}
		return count;
	}

	private class PickContacts extends
			AsyncTask<String, String, ArrayList<SmsContactBean>> {
		Context context;

		public PickContacts(Context context) {
			// TODO Auto-generated constructor stub
			this.context = context;
		}

		@Override
		protected ArrayList<SmsContactBean> doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			ArrayList<SmsContactBean> contacts = new ArrayList<SmsContactBean>();
			try {
				Cursor phones = context.getContentResolver().query(
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						null,
						ContactsContract.CommonDataKinds.Phone.NUMBER + "=?",
						new String[] { arg0[0].toString() }, null);
				Cursor coporateContact = context.getContentResolver().query(
						ContactsConsts.CONTENT_URI_CONTACTS, null,
						ContactsConsts.CONTACT_PHONE + " LIKE ?",
						new String[] { "%'"+ arg0[0].toString()+ "%'" }, null);
				if (coporateContact.getCount() >= 1) {
					while (coporateContact.moveToNext()) {
						ArrayList<HashMap<String, String>> phoneVal = new ArrayList<HashMap<String, String>>();
						String name = coporateContact
								.getString(coporateContact
										.getColumnIndex(ContactsConsts.CONTACT_FIRST_NAME));
						String phone_details = coporateContact
								.getString(coporateContact
										.getColumnIndex(ContactsConsts.CONTACT_PHONE));
						String[] splitParentStr = phone_details.split(":");
						for (int i = 0; i < splitParentStr.length; i++) {
							if (splitParentStr[i].contains("=")) {
								String[] phone_array = splitParentStr[i]
										.split("=");
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
								phoneNumber = phoneVal.get(i).get(
										"phoneNumber" + i);
								numberType = phoneVal.get(i).get(
										"phonetype" + i);
							}

						}

						byte[] imageBytes = coporateContact
								.getBlob(coporateContact
										.getColumnIndex(ContactsConsts.CONTACT_PHOTO));
						if (imageBytes == null) {
							Bitmap defaultPhoto = BitmapFactory.decodeResource(
									context.getResources(),
									R.drawable.sms_ic_contact_picture);
							imageBytes = bitmaptoByteArray(defaultPhoto);
						}
						SmsContactBean corporateContact = new SmsContactBean();
						corporateContact.setName(name);
						corporateContact.setPhoneNo(phoneNumber);
						corporateContact.setType(numberType);
						corporateContact.setByteImage(imageBytes);
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
						long imageId = phones
								.getLong(phones
										.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));

						Bitmap phPhoto = loadContactPhotoUpdate(context,
								context.getContentResolver(), imageId);
						byte[] byteImage = bitmaptoByteArray(phPhoto);
						SmsContactBean nativeContact = new SmsContactBean();
						nativeContact.setName(name);
						nativeContact.setPhoneNo(phoneNumber);
						nativeContact.setType(numberType);
						nativeContact.setByteImage(byteImage);
						nativeContact.setCorporate(false);
						contacts.add(nativeContact);
					}
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
			Bitmap defaultPhoto = BitmapFactory.decodeResource(
					context.getResources(), R.drawable.sms_ic_contact_picture);
			byte[] defaultId = bitmaptoByteArray(defaultPhoto);
			if (contactDetails != null) {
				// ContactBean home = new ContactBean();
				for (SmsContactBean contact : contactDetails) {
					SmsIndividualTable home = new SmsIndividualTable();
					SmsHistoryTable detail = new SmsHistoryTable();
					home.setFirstName(contactDetails.get(0).getName()
							.toString());
					home.setPhoneNumber(contactDetails.get(0).getPhoneNo()
							.toString());
					home.setImageByte(contactDetails.get(0).getByteImage());

					long tim = System.currentTimeMillis();
					home.setTime(tim);

					byte[] picture = null;
					String name = null;
					Cursor cursor = cr.query(
							SmsIndividualTable.CONTENT_URI_INDIVIDUAL, null,
							SmsIndividualTable.PHONE_NUMBER + "=?",
							new String[] { home.getPhoneNumber() }, null);
					int cursorSize = cursor.getCount();
					if (cursorSize == 1) {
						if (cursor.moveToFirst()) {
							picture = cursor.getBlob(cursor
									.getColumnIndex(SmsHistoryTable.IMAGE));
							name = cursor
									.getString(cursor
											.getColumnIndex(SmsIndividualTable.FIRST_NAME));
							if (picture != null) {
								if (picture == defaultId
										&& picture != home.getImageByte())
									home.setImageByte(home.getImageByte());
								else {
									home.setImageByte(picture);
									home.setFirstName(name);
								}
							}
						}
						ContentValues updatedValue = new ContentValues();
						updatedValue = getHomeListValue(home);
						cr.update(SmsIndividualTable.CONTENT_URI_INDIVIDUAL,
								updatedValue, SmsIndividualTable.PHONE_NUMBER
										+ "=?",
								new String[] { home.getPhoneNumber() });

						detail.setFirstName(home.getFirstName());
						detail.setPhoneNumber(home.getPhoneNumber());
						detail.setMessage(home.getMessage());
						detail.setSendReceive(false);
						detail.setTime(home.getTime());
						detail.setContactImageByte(home.getImageByte());
						// detail.setSentStatus("Sending....");
						detail.setLocked(false);

						ContentValues individual = new ContentValues();
						/*
						 * individual = getDetailListValue(detail);
						 * uriHistroyInsert = cr.insert(
						 * HistoryTable.CONTENT_URI_HISTORY, individual);
						 */
						ContentValues image = new ContentValues();
						image.put("personimage", home.getImageByte());
						cr.update(SmsHistoryTable.CONTENT_URI_HISTORY, image,
								SmsHistoryTable.PHONE_NUMBER + "=?",
								new String[] { home.getPhoneNumber() });
						/*
						 * System.out.println("Histroy Insert URI From Async :"
						 * + uriHistroyInsert.toString());
						 */
					} else {
						// fromContact = false;
						ContentValues homePageValue = new ContentValues();
						homePageValue = getHomeListValue(home);
						uriIndividualInsert = cr.insert(
								SmsIndividualTable.CONTENT_URI_INDIVIDUAL,
								homePageValue);
						Log.e(">>><<<<", "Return URI From Async"
								+ uriHistroyInsert);
						detail.setFirstName(home.getFirstName());
						detail.setPhoneNumber(home.getPhoneNumber());
						detail.setMessage(home.getMessage());
						detail.setSendReceive(false);
						detail.setTime(home.getTime());
						detail.setContactImageByte(home.getImageByte());
						detail.setLocked(false);
						detail.setSentStatus("Sending....");

						ContentValues detailPageValue = new ContentValues();
						detailPageValue = getDetailListValue(detail);
						uriHistroyInsert = cr.insert(
								SmsHistoryTable.CONTENT_URI_HISTORY,
								detailPageValue);
						Log.e(">>><<<<", "Return URI From Async"
								+ uriHistroyInsert);
					}
					cursor.close();
					LocalBroadcastManager.getInstance(context).sendBroadcast(
							new Intent("LoaclReciev"));
				}

			}

		}

		public byte[] bitmaptoByteArray(Bitmap bmp) {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
			byte[] byteArray = stream.toByteArray();
			return byteArray;
		}

		public Bitmap loadContactPhotoUpdate(Context ctx,
				ContentResolver contentResolver, long imageId) {
			// TODO Auto-generated method stub
			Uri photoUri = null;
			photoUri = ContentUris.withAppendedId(
					ContactsContract.Contacts.CONTENT_URI, imageId);
			Bitmap defaultPhoto = BitmapFactory.decodeResource(
					ctx.getResources(), R.drawable.sms_ic_contact_picture);
			if (photoUri != null) {
				InputStream input = ContactsContract.Contacts
						.openContactPhotoInputStream(contentResolver, photoUri);
				if (input != null) {
					return BitmapFactory.decodeStream(input);
				}
			} else {

				// return defaultPhoto;
			}

			return defaultPhoto;
		}
	}

}