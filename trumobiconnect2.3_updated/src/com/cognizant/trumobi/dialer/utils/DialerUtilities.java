package com.cognizant.trumobi.dialer.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.TelephonyManager;

import com.cognizant.trubox.contacts.db.ContactsConsts;
import com.cognizant.trumobi.dialer.dbController.DialerCallLogList;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.log.DialerLog;

@SuppressLint("SimpleDateFormat")
public class DialerUtilities {
	private static Resources appResources;
	public static String SELECTED_ID = null;
	public static String editNum = null;
	static ArrayList<DialerCallLogList> mMsgItemList = null;

	public static byte[] getBytes(Bitmap bitmap) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.PNG, 0, stream);
		return stream.toByteArray();
	}

	public static Bitmap getImage(byte[] image) {

		/*
		 * BitmapFactory.Options o=new BitmapFactory.Options();
		 * o.inJustDecodeBounds=true;
		 * 
		 * //The new size we want to scale to final int REQUIRED_SIZE=70;
		 * 
		 * //Find the correct scale value. It should be the power of 2. int
		 * scale=1; while(o.outWidth/scale/2>=REQUIRED_SIZE &&
		 * o.outHeight/scale/2>=REQUIRED_SIZE) scale*=2;
		 * 
		 * o.inSampleSize=scale;
		 */

		return BitmapFactory.decodeByteArray(image, 0, image.length);
	}

	public static boolean isTablet(Context context) {
		appResources = context.getResources();
		boolean xlarge = ((appResources.getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
		boolean large = ((appResources.getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
		return (xlarge || large);
	}

	public static String diffInTime(Date Start, Date Stop) {

		long difference = Stop.getTime() - Start.getTime();
		int days = (int) (difference / (1000 * 60 * 60 * 24));

		if (days <= 0) {
			int hours = (int) ((difference - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60));

			if (hours <= 0) {
				int min = (int) (difference - (1000 * 60 * 60 * 24 * days) - (1000 * 60 * 60 * hours))
						/ (1000 * 60);
				return min + " mins ago";
			} else {
				return hours + " hours ago";
			}
		} else {
			return days + " days ago";
		}
	}

	public static Date frmString(String DateString) {

		DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date startDate = null;
		try {
			startDate = df.parse(DateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		// System.out.println(startDate.getHours());
		return startDate;
	}

	public static String frmSysCal() {

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		// get current date time with Date()
		Date date = new Date();
		String strDate = dateFormat.format(date);
		// System.out.println(strDate);

		return strDate;
	}

	public static String frmSysCal(String format, Date date) {

		DateFormat dateFormat = new SimpleDateFormat(
				"HH:mm','EEEE ',' MMM dd','yyyy");
		// get current date time with Date()
		// Date date = new Date();
		String strDate = dateFormat.format(date);
		// System.out.println(strDate);

		return strDate;
	}

	// EEEE '-' MMM dd','yyyy
	public static String frmEpochDate(Date date) {

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String strDate = dateFormat.format(date);
		DialerLog.i("New", "frmEpochDate " + strDate);

		return strDate;
	}

	public static boolean checkSim() {
		TelephonyManager tm = (TelephonyManager) Email.getAppContext()
				.getSystemService(Context.TELEPHONY_SERVICE);
		if (tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT) {
			return true;
		} else {
			return false;
		}
	}

	public static byte[] getImageNew(String phoneNumber, Context ctx) {
		byte[] data = null;

		String[] projection = new String[] { PhoneLookup._ID };
		Uri contactUri = Uri.withAppendedPath(
				ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
				Uri.encode(phoneNumber));
		Cursor c = ctx.getContentResolver().query(contactUri, projection, null,
				null, null);

		if (c != null && c.moveToFirst()) {
			long contactId = c.getLong(c.getColumnIndex(PhoneLookup._ID));
			InputStream inputStream = Contacts
					.openContactPhotoInputStream(ctx.getContentResolver(),
							ContentUris.withAppendedId(Contacts.CONTENT_URI,
									contactId));
			if (inputStream != null) {
				try {
					byte[] buffer = new byte[1024];
					int bytesRead;
					ByteArrayOutputStream output = new ByteArrayOutputStream();
					while ((bytesRead = inputStream.read(buffer)) != -1) {
						output.write(buffer, 0, bytesRead);
					}

					data = output.toByteArray();
					output.flush();

				} catch (IOException e) {
				}
			}
		}
		return data;

	}

	public static void phoneCallIntent(String numberToDial, Context context) {

		try {
			Intent callIntent = new Intent(Intent.ACTION_CALL);

			if (numberToDial.indexOf("#") > -1) {
				String encodedHash = Uri.encode("#");
				numberToDial = numberToDial.replace("#", encodedHash);
			}

			callIntent.setData(Uri.parse("tel:" + numberToDial));
			callIntent.addCategory("android.intent.category.DEFAULT");
			callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			final List<ResolveInfo> pkgAppsList = context.getPackageManager()
					.queryIntentActivities(callIntent, 0);

			for (ResolveInfo mtemp : pkgAppsList) {
				if (mtemp.activityInfo.applicationInfo.packageName
						.equalsIgnoreCase("com.android.phone")) {
					String packageName = mtemp.activityInfo.applicationInfo.packageName;
					String className = mtemp.activityInfo.name;
					callIntent.setClassName(packageName, className);
				}

			}

			context.startActivity(callIntent);

		} catch (ActivityNotFoundException activityException) {
			DialerLog.e("phonecall activity not found", "Call failed ");
		}

	}

	public static DialerCallLogList populateNativeCallLogList(
			DialerCallLogList mCallLogList) {
		DialerLog.e("Native id", mCallLogList.getFOREIGN_KEY_STRING());
		String[] projection = new String[] { CallLog.Calls.CACHED_NAME };
		String where = CallLog.Calls._ID + " = ? ";
		String[] args = new String[] { mCallLogList.getFOREIGN_KEY_STRING() };
		String name = "";
		DialerLog.e("Native Name", name);

		Cursor c = null;

		try {
			c = Email
					.getAppContext()
					.getContentResolver()
					.query(CallLog.Calls.CONTENT_URI, projection, where, args,
							null);

			if (c != null) {
				c.moveToFirst();
				name = c.getString(0);
				DialerLog.e("Native Name", name);
				mCallLogList.setASSOICIATE_NAME(name);
				mCallLogList.setNativeContact(true);
				mCallLogList.setNativeProfilePic(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
			DialerLog.e("Not a Native", "Empty Name");
			populateCorporateCallLogList(mCallLogList);
		} finally {
			if (c != null) {
				c.close();
			}

		}
		return mCallLogList;

	}

	public static DialerCallLogList populateCorporateCallLogList(
			DialerCallLogList mCallLogList) {
		String[] projection = new String[] { CallLog.Calls.NUMBER };
		String where = CallLog.Calls._ID + " = ? ";
		String[] args = new String[] { mCallLogList.getFOREIGN_KEY_STRING() };
		String lastCallNumber = "";

		Cursor c = null;
		try {
			c = Email
					.getAppContext()
					.getContentResolver()
					.query(CallLog.Calls.CONTENT_URI, projection, where, args,
							null);
			if (c != null) {
				c.moveToFirst();
				lastCallNumber = c.getString(0);
				DialerLog.e("call Number  ", lastCallNumber);
				mCallLogList.setNUMBER(lastCallNumber);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return getDetails(mCallLogList, Email.getAppContext(), lastCallNumber);

	}

	public static DialerCallLogList getDetails(DialerCallLogList mMsgItem,
			Context mContext, String lastCallNumber) {

		boolean found = false;
		if (lastCallNumber != null && lastCallNumber.length() > 10) {
			lastCallNumber = lastCallNumber
					.substring(lastCallNumber.length() - 10);
		}

		Cursor img_c = null;
		try {

			String where = ContactsConsts.CONTACT_PHONE + " LIKE ?";
			String[] args = new String[] { "%" + lastCallNumber + "%" };

			img_c = mContext.getContentResolver().query(
					ContactsConsts.CONTENT_URI_CONTACTS, null, where, args,
					null);
			/*img_c = mContext.getContentResolver().query(
					ContactsConsts.CONTENT_URI_CONTACTS,
					null,
					ContactsConsts.CONTACT_PHONE + " LIKE '%" + lastCallNumber
							+ "%'", null, null);*/
			if (img_c != null && img_c.moveToFirst()) {
				ArrayList<HashMap<String, String>> phoneVal;

				do {
					phoneVal = new ArrayList<HashMap<String, String>>();
					String phone_details = img_c.getString(img_c
							.getColumnIndex(ContactsConsts.CONTACT_PHONE));

					DialerLog.e("CorporateContact", "phone_details  "
							+ phone_details);

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

						phoneNumber = phoneVal.get(i).get("phoneNumber" + i);
						numberType = phoneVal.get(i).get("phonetype" + i);

						if (phoneNumber != null && phoneNumber.length() > 10) {
							phoneNumber = phoneNumber.substring(phoneNumber
									.length() - 10);
						}

						DialerLog.e("CorporateContact", "phone_no  "
								+ phoneNumber + "  " + lastCallNumber);

						if (phoneNumber.equalsIgnoreCase(lastCallNumber)) {

							DialerLog.e("CorporateContact", "phone_details  "
									+ numberType);
							mMsgItem.setNUMBER_TYPE(numberType);
							String name = img_c
									.getString(img_c
											.getColumnIndex(ContactsConsts.CONTACT_FIRST_NAME));
							byte[] imageBytes = img_c
									.getBlob(img_c
											.getColumnIndex(ContactsConsts.CONTACT_PHOTO));
							if (imageBytes != null)
								mMsgItem.setImg_src(imageBytes);
							DialerLog.e("setASSOICIATE_NAME", name);
							mMsgItem.setASSOICIATE_NAME(name);
							mMsgItem.setNativeContact(false);
							found = true;

							break;
						}

					}
					if (!found) {

						mMsgItem.setNativeContact(true);
						DialerLog.e("setASSOICIATE_NAME", "Empty");
						mMsgItem.setASSOICIATE_NAME("");
						mMsgItem.setImg_src(null);
						mMsgItem.setNativeProfilePic(false);

					}
					// if (phoneNumber.equalsIgnoreCase(lastCallNumber)) {
					/*
					 * String name = img_c .getString(img_c
					 * .getColumnIndex(ContactsConsts.CONTACT_FIRST_NAME));
					 * byte[] imageBytes = img_c .getBlob(img_c
					 * .getColumnIndex(ContactsConsts.CONTACT_PHOTO)); if
					 * (imageBytes != null) mMsgItem.setImg_src(imageBytes);
					 * mMsgItem.setASSOICIATE_NAME(name);
					 */
					// } else {
					/*
					 * mMsgItem.setNativeContact(true);
					 * mMsgItem.setASSOICIATE_NAME("");
					 * mMsgItem.setImg_src(null);
					 * mMsgItem.setNativeProfilePic(false);
					 */
					// }

				} while (img_c.moveToNext());

			} else {
				mMsgItem.setNativeContact(true);
				mMsgItem.setASSOICIATE_NAME("");
				mMsgItem.setImg_src(null);
				mMsgItem.setNativeProfilePic(false);
			}

		} catch (Exception e) {
			mMsgItem.setNativeContact(true);
			mMsgItem.setASSOICIATE_NAME("");
			mMsgItem.setImg_src(null);
			mMsgItem.setNativeProfilePic(false);
		} finally {
			if (img_c != null) {
				img_c.close();
			}
		}
		return mMsgItem;
	}
}
