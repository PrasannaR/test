package com.cognizant.trumobi.messenger.sms;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import com.TruBoxSDK.SharedPreferences;
import com.cognizant.trubox.contacts.db.ContactsConsts;
import com.cognizant.trubox.contacts.db.ContactsModel;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.contacts.utils.ContactsUtilities;
import com.cognizant.trumobi.em.Email;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;

public class SmsUtil {

	public static HashMap<String, String> selectedContact = new HashMap<String, String>();
	static ArrayList<ContactsModel> corporateContacts;
	static ArrayList<ContactsModel> nativeContacts;
	static ArrayList<ContactsModel> mergedContacts;
	static SmsContactBean objContact = new SmsContactBean();

	public static ArrayList<SmsContactBean> getContacts(Context context,
			boolean addAllConatct) {
		ArrayList<SmsContactBean> contacts = new ArrayList<SmsContactBean>();
		try {
			boolean nativeContactsPrefsVal = new SharedPreferences(
					 Email.getAppContext()).getBoolean( "prefNativeContacts",
					 false);
			if(nativeContactsPrefsVal){
			Cursor phones = context.getContentResolver().query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
					null, null, null);
			if (phones != null) {
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

					String strNumberType = getMessageType(Integer
							.parseInt(numberType));
					long imageId = phones
							.getLong(phones
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));

					Bitmap phPhoto = loadContactPhoto(context,
							context.getContentResolver(), imageId);
					byte[] byteImage = bitmaptoByteArray(phPhoto);
					SmsContactBean nativeContact = new SmsContactBean();
					nativeContact.setName(name);
					nativeContact.setPhoneNo(phoneNumber);
					nativeContact.setType(strNumberType);
					nativeContact.setByteImage(byteImage);
					contacts.add(nativeContact);
				}
			}
			phones.close();
			}
			Cursor cursor = context.getContentResolver()
					.query(ContactsConsts.CONTENT_URI_CONTACTS, null, null,
							null, null);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					ArrayList<HashMap<String, String>> phoneVal = new ArrayList<HashMap<String, String>>();
					String firstName = cursor.getString(cursor
							.getColumnIndex(ContactsConsts.CONTACT_FIRST_NAME));
					String middleName = cursor
							.getString(cursor
									.getColumnIndex(ContactsConsts.CONTACT_MIDDLE_NAME));
					String lastName = cursor.getString(cursor
							.getColumnIndex(ContactsConsts.CONTACT_LAST_NAME));
					String phone_details = cursor.getString(cursor
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
						// if (i == 0) {
						phoneNumber = phoneVal.get(i).get("phoneNumber" + i);
						numberType = phoneVal.get(i).get("phonetype" + i);
						byte[] imageBytes = cursor.getBlob(cursor
								.getColumnIndex(ContactsConsts.CONTACT_PHOTO));
						if (imageBytes == null) {
							Bitmap defaultPhoto = BitmapFactory.decodeResource(
									context.getResources(),
									R.drawable.sms_ic_contact_picture);
							imageBytes = bitmaptoByteArray(defaultPhoto);
						}
						String name = null;
						if (lastName != null && middleName != null)
							name = firstName + " " + middleName + " "
									+ lastName;
						else if (lastName != null && middleName == null)
							name = firstName + " " + lastName;
						else if (lastName == null && middleName != null)
							name = firstName + " " + middleName;
						else
							name = firstName;
						SmsContactBean corporateContact = new SmsContactBean();
						if(name.contains(",")){
							name = name.replace(","," ");
							corporateContact.setName(name.trim());
						}else
							corporateContact.setName(name.trim());
						corporateContact.setPhoneNo(phoneNumber);
						corporateContact.setType(numberType);
						corporateContact.setByteImage(imageBytes);
						contacts.add(corporateContact);
						// }

					}

					// String strNumberType =
					// getMessageType(Integer.parseInt(numberType));

				}
			}
			cursor.close();

			/*
			 * mergedContacts = new ArrayList<ContactsModel>();
			 * 
			 * boolean nativeContactsPrefsVal = new SharedPreferences(
			 * CognizantEmail.getAppContext()).getBoolean( "prefNativeContacts",
			 * false);
			 * 
			 * String sortOrder = new SharedPreferences(
			 * CognizantEmail.getAppContext()).getString("prefSortOrder",
			 * ContactsConsts.CONTACT_FIRST_NAME);
			 * 
			 * Cursor cursor = CognizantEmail .getAppContext()
			 * .getContentResolver() .query(ContactsConsts.CONTENT_URI_CONTACTS,
			 * null, null, null, sortOrder + " COLLATE NOCASE"); if
			 * (nativeContactsPrefsVal) { nativeContacts = ContactsUtilities
			 * .PopulateLocalContactsFromCursor(CognizantEmail
			 * .getAppContext().getContentResolver()); if (nativeContacts !=
			 * null) { for (int i=0; i<nativeContacts.size();i++){ ContactBean
			 * nativeContact = new ContactBean(); String name = null; String
			 * firstName = nativeContacts.get(i).getcontacts_first_name();
			 * String middleName =
			 * nativeContacts.get(i).getcontacts_middle_name(); String lastName
			 * = nativeContacts.get(i).getcontacts_last_name(); if(lastName !=
			 * null && middleName != null) name =
			 * firstName+" "+middleName+" "+lastName; else if(lastName != null
			 * && middleName == null) name = firstName+" "+lastName; else
			 * if(lastName == null && middleName != null) name =
			 * firstName+" "+middleName; else name = firstName;
			 * nativeContact.setName(name);
			 * nativeContact.setPhoneNo(nativeContacts
			 * .get(i).getcontacts_mobile_telephone_number());
			 * //nativeContact.setType(numberType); byte[] imageBytes = null;
			 * if(nativeContacts.get(i).getContacts_image() == null){ Bitmap
			 * defaultPhoto =
			 * BitmapFactory.decodeResource(context.getResources(),
			 * R.drawable.sms_ic_contact_picture); imageBytes =
			 * bitmaptoByteArray(defaultPhoto);
			 * nativeContact.setByteImage(imageBytes); } else
			 * nativeContact.setByteImage
			 * (nativeContacts.get(i).getContacts_image());
			 * contacts.add(nativeContact); }
			 * //mergedContacts.addAll(nativeContacts); } }
			 * 
			 * if(cursor != null){ while (cursor.moveToNext()) {
			 * corporateContacts =
			 * ContactsUtilities.PopulateArraylistFromCursor( cursor,
			 * CognizantEmail.getAppContext()); if (corporateContacts != null) {
			 * for (int i=0; i<corporateContacts.size();i++){ ContactBean
			 * corporateContact = new ContactBean(); String name = null; String
			 * firstName = corporateContacts.get(i).getcontacts_first_name();
			 * String middleName =
			 * corporateContacts.get(i).getcontacts_middle_name(); String
			 * lastName = corporateContacts.get(i).getcontacts_last_name();
			 * if(lastName != null && middleName != null) name =
			 * firstName+" "+middleName+" "+lastName; else if(lastName != null
			 * && middleName == null) name = firstName+" "+lastName; else
			 * if(lastName == null && middleName != null) name =
			 * firstName+" "+middleName; else name = firstName;
			 * corporateContact.setName(name);
			 * corporateContact.setPhoneNo(corporateContacts
			 * .get(i).getcontacts_mobile_telephone_number());
			 * //nativeContact.setType(numberType);
			 * corporateContact.setByteImage
			 * (corporateContacts.get(i).getContacts_image());
			 * contacts.add(corporateContact); }
			 * //mergedContacts.addAll(corporateContacts); } } } cursor.close();
			 */

		} catch (Exception e) {
			e.printStackTrace();
		}

		Log.i("contactLength", String.valueOf(contacts.size()));
		return contacts;
	}

	private static byte[] bitmaptoByteArray(Bitmap bmp) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
		byte[] byteArray = stream.toByteArray();
		return byteArray;
	}

	public static Bitmap loadContactPhoto(Context ctx,
			ContentResolver contentResolver, long imageId) {
		// TODO Auto-generated method stub
		Uri photoUri = null;
		photoUri = ContentUris.withAppendedId(
				ContactsContract.Contacts.CONTENT_URI, imageId);
		Bitmap defaultPhoto = BitmapFactory.decodeResource(ctx.getResources(),
				R.drawable.sms_ic_contact_picture);
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

	private static String getMessageType(int type) {
		switch (type) {
		case 1:
			return "HOME";
		case 2:
			return "MOBILE";
		case 3:
			return "WORK";
		case 4:
			return "WORK FAX";
		case 5:
			return "HOME FAX";
		case 6:
			return "PAGER";
		case 7:
			return "OTHER";
		case 11:
			return "ISDN";
		case 12:
			return "MAIN";
		case 13:
			return "OTHER FAX";
		case 17:
			return "MOBILE WORK";
		default:
			return "OTHER";
		}
	}

	public static Object extractBitmapFromTextView(View view) {

		int spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		view.measure(spec, spec);
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		Bitmap b = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
				Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(b);
		c.translate(-view.getScrollX(), -view.getScrollY());
		view.draw(c);
		view.setDrawingCacheEnabled(true);
		Bitmap cacheBmp = view.getDrawingCache();
		Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
		view.destroyDrawingCache();
		return new BitmapDrawable(viewBmp);

	}

}
