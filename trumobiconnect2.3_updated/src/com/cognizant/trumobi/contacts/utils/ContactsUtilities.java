package com.cognizant.trumobi.contacts.utils;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;

import com.cognizant.trubox.contacts.db.ContactsConsts;
import com.cognizant.trubox.contacts.db.ContactsModel;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.exchange.provider.EmGalResult.GalData;
import com.cognizant.trumobi.log.ContactsLog;

public class ContactsUtilities {
	private static Resources appResources;
	public static String SELECTED_ID = null;
	static ContentValues emailNumbers = new ContentValues();
	static String TAG = ContactsUtilities.class.getName();

	public static byte[] getBytes(Bitmap bitmap) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.PNG, 0, stream);
		return stream.toByteArray();
	}

	public static Bitmap getImage(byte[] image) {

		return BitmapFactory.decodeByteArray(image, 0, image.length);
	}

	public static int sizeOf(Bitmap data) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
			return data.getRowBytes() * data.getHeight();
		} else {
			return data.getByteCount();
		}
	}

	public static boolean isTablet(Context context) {
		appResources = context.getResources();
		boolean xlarge = ((appResources.getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
		boolean large = ((appResources.getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
		return (xlarge || large);
	}

	// Load Corporate Contacts On the First Time
	public static ArrayList<ContactsModel> PopulateArraylistFromGalResult(
			ArrayList<GalData> galResult) {

		if (galResult != null) {
			ContactsLog.d(TAG,
					"PopulateArraylistFromGalResult*** " + galResult.size());
		}
		ArrayList<ContactsModel> searchResults = new ArrayList<ContactsModel>();
		for (GalData cGalData : galResult) {
			ContactsModel contact = new ContactsModel();

			String firstName = cGalData.get(GalData.FIRST_NAME);

			String lastName = cGalData.get(GalData.LAST_NAME);

			String displayName = cGalData.get(GalData.ALIAS);

			// ContactsLog.d(TAG, "FIRST_NAME  :: " + firstName);
			// ContactsLog.d(TAG, "LAST_NAME  :: " + lastName);
			// ContactsLog.d(TAG, "DISPLAY_NAME  :: " + displayName);
			//
			// ContactsLog.d(TAG, "COMPANY :: " +
			// cGalData.get(GalData.COMPANY));
			// ContactsLog.d(TAG,
			// "WORK_PHONE :: " + cGalData.get(GalData.WORK_PHONE));
			//
			// ContactsLog.d(TAG,
			// "MOBILE_PHONE :: " + cGalData.get(GalData.MOBILE_PHONE));
			//
			// ContactsLog.d(TAG,
			// "EMAIL_ADDRESS :: " + cGalData.get(GalData.EMAIL_ADDRESS));
			//
			// ContactsLog.d(TAG, "TITLE  :: " + cGalData.get(GalData.TITLE));
			if (firstName == null && lastName == null && displayName == null) {
				continue;
			}

			if (firstName == null && displayName != null) {
				firstName = displayName;
			}

			if (lastName == null) {
				lastName = "";
			}

			contact.setcontacts_first_name(firstName);
			contact.setcontacts_last_name(lastName);
			contact.setcontacts_job_title(cGalData.get(GalData.TITLE));

			contact.setcontacts_company_name(cGalData.get(GalData.COMPANY));

			contact.setcontacts_business_telephone_number(cGalData
					.get(GalData.WORK_PHONE));
			contact.setcontacts_mobile_telephone_number(cGalData
					.get(GalData.MOBILE_PHONE));
			contact.setContacts_home_telephone_number_type(cGalData
					.get(GalData.HOME_PHONE));
			contact.setcontacts_email1_address(cGalData
					.get(GalData.EMAIL_ADDRESS));
			contact.setContacts_business_location(cGalData.get(GalData.OFFICE));
			contact.setcontacts_picture(cGalData.get(GalData.PHOTO));
			contact.setSearchContact(true);
			searchResults.add(contact);
		}
		return searchResults;
	}

	// Load Corporate Contacts On the First Time
	public static ArrayList<ContactsModel> PopulateArraylistFromCursor(
			Cursor cursor, Context mContext) {

		ArrayList<ContactsModel> corporateContacts = null;
		try {
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				corporateContacts = new ArrayList<ContactsModel>();

				while (cursor.isAfterLast() == false) {
					ContactsModel contactsModel = new ContactsModel();
					// Read Contact ID
					if (cursor.getInt(cursor
							.getColumnIndex(ContactsConsts.CONTACT_ID)) != 0) {
						contactsModel.setContact_id(cursor.getInt(cursor
								.getColumnIndex(ContactsConsts.CONTACT_ID)));
					}
					// Read Contact FirstName
					if (cursor.getString(cursor
							.getColumnIndex(ContactsConsts.CONTACT_FIRST_NAME)) != null) {
						contactsModel
								.setcontacts_first_name(cursor.getString(cursor
										.getColumnIndex(ContactsConsts.CONTACT_FIRST_NAME)));
					}
					// Read Contact Middle name
					if (cursor
							.getString(cursor
									.getColumnIndex(ContactsConsts.CONTACT_MIDDLE_NAME)) != null) {
						contactsModel
								.setcontacts_middle_name(cursor.getString(cursor
										.getColumnIndex(ContactsConsts.CONTACT_MIDDLE_NAME)));
					}
					// Read Contact Last name
					if (cursor.getString(cursor
							.getColumnIndex(ContactsConsts.CONTACT_LAST_NAME)) != null) {
						contactsModel
								.setcontacts_last_name(cursor.getString(cursor
										.getColumnIndex(ContactsConsts.CONTACT_LAST_NAME)));
					}
					// Read Contact Image
					if (cursor.getBlob(cursor
							.getColumnIndex(ContactsConsts.CONTACT_PHOTO)) != null) {
						contactsModel.setContacts_image(cursor.getBlob(cursor
								.getColumnIndex(ContactsConsts.CONTACT_PHOTO)));
					}
					corporateContacts.add(contactsModel);
					cursor.moveToNext();
				}
			}
		} catch (Exception ee) {
		}
		return corporateContacts;

	}

	// Load Native Contacts On the First Time
	public static ArrayList<ContactsModel> PopulateLocalContactsFromCursor(
			ContentResolver contentResolver) {

		ArrayList<ContactsModel> corporateContacts = new ArrayList<ContactsModel>();

		Uri uri = ContactsContract.Contacts.CONTENT_URI;
		String[] projection = new String[] { ContactsContract.Contacts._ID,
				ContactsContract.Contacts.DISPLAY_NAME };
		String selection = null;
		String[] selectionArgs = null;
		String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + "  ASC";
		Cursor cursor = contentResolver.query(uri, projection, selection,
				selectionArgs, sortOrder);

		if (cursor != null && cursor.getCount() > 0) {

			while (cursor.moveToNext()) {
				ContactsModel contactsModel = new ContactsModel();

				contactsModel.setContact_id(cursor.getInt(cursor
						.getColumnIndex(ContactsContract.Data._ID)));
				contactsModel.setcontacts_first_name(cursor.getString(cursor
						.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)));

				// contactsModel.setContacts_image(cursor.getBlob(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO)));

				// Set Contact as Native:
				contactsModel.setNativeContact(true);
				corporateContacts.add(contactsModel);
			}
		}
		return corporateContacts;
	}

	// Load Corporate Contacts Details for Single Contact based on the ID
	public static ArrayList<ContactsModel> getContactsDetailsFromCursor(
			ContentResolver contentResolver, String receivedExtra) {

		Cursor cursor = contentResolver.query(
				ContactsConsts.CONTENT_URI_CONTACTS, null, "_id = ?",
				new String[] { receivedExtra }, null);

		ArrayList<ContactsModel> corporateContacts = null;

		if (cursor != null && cursor.moveToFirst()) {

			corporateContacts = new ArrayList<ContactsModel>();

			while (cursor.isAfterLast() == false) {
				ContactsModel contactsModel = new ContactsModel();
				// Read Contact ID
				if (cursor.getInt(cursor
						.getColumnIndex(ContactsConsts.CONTACT_ID)) != 0) {
					contactsModel.setContact_id(cursor.getInt(cursor
							.getColumnIndex(ContactsConsts.CONTACT_ID)));
				}
				// Read Contact FirstName
				if (cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_FIRST_NAME)) != null) {
					contactsModel
							.setcontacts_first_name(cursor.getString(cursor
									.getColumnIndex(ContactsConsts.CONTACT_FIRST_NAME)));
				}
				// Read Contact Middle name
				if (cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_MIDDLE_NAME)) != null) {
					contactsModel
							.setcontacts_middle_name(cursor.getString(cursor
									.getColumnIndex(ContactsConsts.CONTACT_MIDDLE_NAME)));
				}
				// Read Contact Last name
				if (cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_LAST_NAME)) != null) {
					contactsModel.setcontacts_last_name(cursor.getString(cursor
							.getColumnIndex(ContactsConsts.CONTACT_LAST_NAME)));
				}
				// Read Contact Image
				if (cursor.getBlob(cursor
						.getColumnIndex(ContactsConsts.CONTACT_PHOTO)) != null) {
					contactsModel.setContacts_image(cursor.getBlob(cursor
							.getColumnIndex(ContactsConsts.CONTACT_PHOTO)));
				}
				
				
				
				
				

				if (cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_PHONETIC_FAMILY_NAME)) != null) {
					contactsModel.setContact_phonetic_family_name(cursor.getString(cursor
							.getColumnIndex(ContactsConsts.CONTACT_PHONETIC_FAMILY_NAME)));
				}
				
				if (cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_PHONETIC_GIVEN_NAME)) != null) {
					contactsModel.setContact_phonetic_given_name(cursor.getString(cursor
							.getColumnIndex(ContactsConsts.CONTACT_PHONETIC_GIVEN_NAME)));
				}
				
				if (cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_PHONETIC_MIDDLE_NAME)) != null) {
					contactsModel.setContact_phonetic_middle_name(cursor.getString(cursor
							.getColumnIndex(ContactsConsts.CONTACT_PHONETIC_MIDDLE_NAME)));
				}
				
				if (cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_TITLE)) != null) {
					contactsModel.setContacts_name_prefix(cursor.getString(cursor
							.getColumnIndex(ContactsConsts.CONTACT_TITLE)));
				}
				
				if (cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_SUFFIX)) != null) {
					contactsModel.setContacts_name_suffix(cursor.getString(cursor
							.getColumnIndex(ContactsConsts.CONTACT_SUFFIX)));
				}
				
				
				
				
				
				
				
				
				
				
				
				
				
				try {
					// Read Contact Phone number
					if (cursor.getString(cursor
							.getColumnIndex(ContactsConsts.CONTACT_PHONE)) != null) {
						String phone_details = cursor.getString(cursor
								.getColumnIndex(ContactsConsts.CONTACT_PHONE));

						ArrayList<HashMap<String, String>> phoneVal = new ArrayList<HashMap<String, String>>();
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

						for (int i = 0; i < phoneVal.size(); i++) {
							if ((phoneVal.size() > 0 && phoneVal.get(i) != null && i == 0)) {

								contactsModel
										.setcontacts_mobile_telephone_number(phoneVal
												.get(i).get("phoneNumber" + i));
								contactsModel
										.setContact_phone_number_mobile_type1(phoneVal
												.get(i).get("phonetype" + i));
							}
							if ((phoneVal.size() > 0 && phoneVal.get(i) != null && i == 1)) {
								contactsModel
										.setcontacts_business_telephone_number(phoneVal
												.get(i).get("phoneNumber" + i));
								contactsModel
										.setContact_phone_number_mobile_type2(phoneVal
												.get(i).get("phonetype" + i));
							}
							if ((phoneVal.size() > 0 && phoneVal.get(i) != null && i == 2)) {
								contactsModel
										.setcontacts_assistant_telephone_number(phoneVal
												.get(i).get("phoneNumber" + i));
								contactsModel
										.setContacts_assistant_telephone_number_type(phoneVal
												.get(i).get("phonetype" + i));
							}
							if ((phoneVal.size() > 0 && phoneVal.get(i) != null && i == 3)) {
								contactsModel
										.setcontacts_business2_telephone_number(phoneVal
												.get(i).get("phoneNumber" + i));
								contactsModel
										.setContacts_business2_telephone_number_type(phoneVal
												.get(i).get("phonetype" + i));
							}
							if ((phoneVal.size() > 0 && phoneVal.get(i) != null && i == 4)) {
								contactsModel
										.setcontacts_business_fax_number(phoneVal
												.get(i).get("phoneNumber" + i));
								contactsModel
										.setContacts_business_fax_number_type(phoneVal
												.get(i).get("phonetype" + i));
							}
							if ((phoneVal.size() > 0 && phoneVal.get(i) != null && i == 5)) {
								contactsModel
										.setcontacts_car_telephone_number(phoneVal
												.get(i).get("phoneNumber" + i));
								contactsModel
										.setContacts_car_telephone_numbe_type(phoneVal
												.get(i).get("phonetype" + i));
							}

							if ((phoneVal.size() > 0 && phoneVal.get(i) != null && i == 6)) {
								contactsModel
										.setcontacts_home2_telephone_number(phoneVal
												.get(i).get("phoneNumber" + i));
								contactsModel
										.setContacts_home2_telephone_number_type(phoneVal
												.get(i).get("phonetype" + i));
							}
							if ((phoneVal.size() > 0 && phoneVal.get(i) != null && i == 7)) {
								contactsModel
										.setcontacts_home_fax_number(phoneVal
												.get(i).get("phoneNumber" + i));
								contactsModel
										.setContacts_home_fax_number_type(phoneVal
												.get(i).get("phonetype" + i));
							}
							if ((phoneVal.size() > 0 && phoneVal.get(i) != null && i == 8)) {
								contactsModel
										.setcontacts_home_telephone_number(phoneVal
												.get(i).get("phoneNumber" + i));
								contactsModel
										.setContacts_home_telephone_number_type(phoneVal
												.get(i).get("phonetype" + i));
							}
							if ((phoneVal.size() > 0 && phoneVal.get(i) != null && i == 9)) {
								contactsModel.setcontacts_pager_number(phoneVal
										.get(i).get("phoneNumber" + i));
								contactsModel
										.setContacts_pager_number_type(phoneVal
												.get(i).get("phonetype" + i));
							}
							if ((phoneVal.size() > 0 && phoneVal.get(i) != null && i == 10)) {
								contactsModel
										.setcontacts_radio_telephone_number(phoneVal
												.get(i).get("phoneNumber" + i));
								contactsModel
										.setContacts_radio_telephone_number_type(phoneVal
												.get(i).get("phonetype" + i));
							}
							if ((phoneVal.size() > 0 && phoneVal.get(i) != null && i == 11)) {
								contactsModel
										.setContacts2_company_main_phone(phoneVal
												.get(i).get("phoneNumber" + i));
								contactsModel
										.setContacts2_company_main_phone_type(phoneVal
												.get(i).get("phonetype" + i));
							}
							if ((phoneVal.size() > 0 && phoneVal.get(i) != null && i == 12)) {
								contactsModel.setContact_custom_phone1(phoneVal
										.get(i).get("phoneNumber" + i));
								contactsModel
										.setContact_custom_phone1_type(phoneVal
												.get(i).get("phonetype" + i));
							}
							if ((phoneVal.size() > 0 && phoneVal.get(i) != null && i == 13)) {
								contactsModel.setContact_custom_phone2(phoneVal
										.get(i).get("phoneNumber" + i));
								contactsModel
										.setContact_custom_phone2_type(phoneVal
												.get(i).get("phonetype" + i));
							}
							if ((phoneVal.size() > 0 && phoneVal.get(i) != null && i == 14)) {
								contactsModel.setContact_custom_phone3(phoneVal
										.get(i).get("phoneNumber" + i));
								contactsModel
										.setContact_custom_phone3_type(phoneVal
												.get(i).get("phonetype" + i));
							}

						}
					}
				} catch (Exception ex) {
					ContactsLog.e("ContactsUtilities",
							" Pnone: " + ex.toString());
				}
				try {
					// Read Contact Email
					if (cursor.getString(cursor
							.getColumnIndex(ContactsConsts.CONTACT_EMAIL)) != null) {
						String email_details = cursor.getString(cursor
								.getColumnIndex(ContactsConsts.CONTACT_EMAIL));

						ArrayList<HashMap<String, String>> emailVal = new ArrayList<HashMap<String, String>>();
						String[] splitParentStr = email_details.split(":");
						for (int i = 0; i < splitParentStr.length; i++) {
							if (splitParentStr[i].contains("=")) {
								String[] emailArray = splitParentStr[i]
										.split("=");
								HashMap<String, String> row = new HashMap<String, String>();
								row.put("emailtype" + i, emailArray[0]);
								row.put("email" + i, emailArray[1]);
								emailVal.add(row);
							}
						}

						for (int i = 0; i < emailVal.size(); i++) {
							if ((emailVal.size() > 0 && emailVal.get(i) != null && i == 0)) {

								contactsModel
										.setcontacts_email1_address(emailVal
												.get(i).get("email" + i));
								contactsModel.setContact_email1_type(emailVal
										.get(i).get("emailtype" + i));
							}
							if ((emailVal.size() > 0 && emailVal.get(i) != null && i == 1)) {
								contactsModel
										.setcontacts_email2_address(emailVal
												.get(i).get("email" + i));
								contactsModel.setContact_email2_type(emailVal
										.get(i).get("emailtype" + i));
							}
							if ((emailVal.size() > 0 && emailVal.get(i) != null && i == 2)) {
								contactsModel
										.setcontacts_email3_address(emailVal
												.get(i).get("email" + i));
								contactsModel.setContact_email3_type(emailVal
										.get(i).get("emailtype" + i));
							}
							if ((emailVal.size() > 0 && emailVal.get(i) != null && i == 3)) {
								contactsModel
										.setContacts_custom_email1_address(emailVal
												.get(i).get("email" + i));
								contactsModel
										.setContact_custom_email1_type(emailVal
												.get(i).get("emailtype" + i));
							}
							if ((emailVal.size() > 0 && emailVal.get(i) != null && i == 4)) {
								contactsModel
										.setContacts_custom_email2_address(emailVal
												.get(i).get("email" + i));
								contactsModel
										.setContact_custom_email2_type(emailVal
												.get(i).get("emailtype" + i));
							}
							if ((emailVal.size() > 0 && emailVal.get(i) != null && i == 5)) {

								contactsModel
										.setContacts_custom_email3_address(emailVal
												.get(i).get("email" + i));
								contactsModel
										.setContact_custom_email3_type(emailVal
												.get(i).get("emailtype" + i));
							}
						}
					}
				} catch (Exception ee) {
					ContactsLog.e("ContactsUtilities",
							" email: " + ee.toString());
				}

				Cursor bus_adrs = null;
				try {
					if (cursor
							.getInt(cursor
									.getColumnIndex(ContactsConsts.CONTACT_BUSS_ADRESS)) == 1) {

						String serverId = cursor
								.getString(cursor
										.getColumnIndex(ContactsConsts.CONTACT_SERVER_ID));
						String clientId = cursor
								.getString(cursor
										.getColumnIndex(ContactsConsts.CONTACT_CLIENT_ID));
						String unique_id = null;
						// Cursor bus_adrs;
						if (serverId != null && !serverId.isEmpty()) {

							unique_id = "W" + "||" + serverId;
							bus_adrs = Email
									.getAppContext()
									.getContentResolver()
									.query(ContactsConsts.CONTENT_URI_ADRESS,
											null,
											ContactsConsts.ADRESS_UNIQUE_ID + "=?",
											new String[] { unique_id }, null);
						} else {
							unique_id = "W" + "||" + clientId;
							bus_adrs = Email
									.getAppContext()

									.getContentResolver()
									.query(ContactsConsts.CONTENT_URI_ADRESS,
											null,
											ContactsConsts.ADRESS_CLIENT_ID + "=?",
											new String[] { unique_id }, null);
						}

						if (bus_adrs != null && bus_adrs.moveToFirst()) {

							String buss_adress = "";

							String street = bus_adrs.getString(bus_adrs
									.getColumnIndex(ContactsConsts.STREET));
							if (street != null && !street.isEmpty()) {
								buss_adress = buss_adress + "Street" + ": "
										+ street + "\n";

								contactsModel
										.setcontacts_business_address_street(street);

							}
							String city = bus_adrs.getString(bus_adrs
									.getColumnIndex(ContactsConsts.CITY));
							if (city != null && !city.isEmpty()) {
								buss_adress = buss_adress + "City" + ": "
										+ city + "\n";
								contactsModel
										.setcontacts_business_address_city(city);
							}
							String state = bus_adrs.getString(bus_adrs
									.getColumnIndex(ContactsConsts.STATE));
							if (state != null && !state.isEmpty()) {
								buss_adress = buss_adress + "State" + ": "
										+ state + "\n";
								contactsModel
										.setcontacts_business_address_state(state);
							}

							String zip = bus_adrs.getString(bus_adrs
									.getColumnIndex(ContactsConsts.ZIP));
							if (zip != null && !zip.isEmpty()) {
								buss_adress = buss_adress + "Zip" + ": " + zip
										+ "\n";
								contactsModel
										.setcontacts_business_address_postal_code(zip);
							}
							String country = bus_adrs.getString(bus_adrs
									.getColumnIndex(ContactsConsts.COUNTRY));
							if (country != null && !country.isEmpty()) {
								buss_adress = buss_adress + "Country" + ": "
										+ country + "\n";
								contactsModel
										.setcontacts_business_address_country(country);
							}

							if (buss_adress != null && !buss_adress.isEmpty()) {
								contactsModel
										.setContacts_business_location(buss_adress);
								contactsModel
										.setContact_business_location_type("BUSINESS");
							}
							ContactsLog.d("Utils", "buss_adress : "
									+ buss_adress);
							bus_adrs.close();
						}

					} else {
						contactsModel.setContacts_business_location(null);
					}
				} catch (Exception e) {
					ContactsLog.e("ContactsUtilities",
							"Address : " + e.toString());
					if (bus_adrs != null) {
						bus_adrs.close();
					}
				}

				Cursor home_adrs = null;
				try {
					if (cursor
							.getInt(cursor
									.getColumnIndex(ContactsConsts.CONTACT_HOME_ADRESS)) == 1) {
						String serverId = cursor
								.getString(cursor
										.getColumnIndex(ContactsConsts.CONTACT_SERVER_ID));
						String clientId = cursor
								.getString(cursor
										.getColumnIndex(ContactsConsts.CONTACT_CLIENT_ID));
						String unique_id = null;
						if (serverId != null && !serverId.isEmpty()) {
							// Read Contact HOME Address
							unique_id = "H" + "||" + serverId;
							home_adrs = Email
									.getAppContext()
									.getContentResolver()
									.query(ContactsConsts.CONTENT_URI_ADRESS,
											null,
											ContactsConsts.ADRESS_UNIQUE_ID + "=?",
											new String[] { unique_id }, null);

						} else {
							// Read Contact HOME Address
							unique_id = "H" + "||" + clientId;
							home_adrs = Email
									.getAppContext()
									.getContentResolver()
									.query(ContactsConsts.CONTENT_URI_ADRESS,
											null,
											ContactsConsts.ADRESS_CLIENT_ID + "=?",
											new String[] { unique_id }, null);
						}

						if (home_adrs != null && home_adrs.getCount() > 0) {
							home_adrs.moveToFirst();
							String home_adress = "";
							String streetHome = home_adrs.getString(home_adrs
									.getColumnIndex(ContactsConsts.STREET));
							if (streetHome != null && !streetHome.isEmpty()) {
								home_adress = home_adress + "Street" + ": "
										+ streetHome + "\n";
								contactsModel
										.setcontacts_home_address_street(streetHome);
							}
							String cityhome = home_adrs.getString(home_adrs
									.getColumnIndex(ContactsConsts.CITY));
							if (cityhome != null && !cityhome.isEmpty()) {
								home_adress = home_adress + "City" + ": "
										+ cityhome + "\n";
								contactsModel
										.setcontacts_home_address_city(cityhome);
							}
							String statehome = home_adrs.getString(home_adrs
									.getColumnIndex(ContactsConsts.STATE));
							if (statehome != null && !statehome.isEmpty()) {
								home_adress = home_adress + "State" + ": "
										+ statehome + "\n";
								contactsModel
										.setcontacts_home_address_state(statehome);
							}

							String ziphome = home_adrs.getString(home_adrs
									.getColumnIndex(ContactsConsts.ZIP));
							if (ziphome != null && !ziphome.isEmpty()) {
								home_adress = home_adress + "Zip" + ": "
										+ ziphome + "\n";
								contactsModel
										.setcontacts_home_address_postal_code(ziphome);
							}
							String countryhome = home_adrs.getString(home_adrs
									.getColumnIndex(ContactsConsts.COUNTRY));
							if (countryhome != null && !countryhome.isEmpty()) {
								home_adress = home_adress + "Country" + ": "
										+ countryhome + "\n";
								contactsModel
										.setcontacts_home_address_country(countryhome);
							}

							if (home_adress != null && !home_adress.isEmpty()) {
								contactsModel
										.setContacts_home_location(home_adress);
								contactsModel
										.setContact_home_location_type("HOME");
							}
							ContactsLog.d("Utils", "home_adress : "
									+ home_adress);
							home_adrs.close();
						} else {
							home_adrs.close();
							ContactsLog.e("ContactsUtilities",
									"HOME Address : NULL ");
						}
					} else {

						contactsModel.setContacts_home_location(null);
					}
				} catch (Exception e) {
					ContactsLog.e("ContactsUtilities",
							" HOME Address : " + e.toString());
					if (home_adrs != null) {
						home_adrs.close();
					}
				}

				Cursor other_adrs = null;
				try {
					if (cursor
							.getInt(cursor
									.getColumnIndex(ContactsConsts.CONTACT_OTHER_ADRESS)) == 1) {
						String serverId = cursor
								.getString(cursor
										.getColumnIndex(ContactsConsts.CONTACT_SERVER_ID));
						String clientId = cursor
								.getString(cursor
										.getColumnIndex(ContactsConsts.CONTACT_CLIENT_ID));
						String unique_id = null;
						if (serverId != null && !serverId.isEmpty()) {
							// Read Contact OTHER Address
							unique_id = "O" + "||" + serverId;
							other_adrs = Email
									.getAppContext()
									.getContentResolver()
									.query(ContactsConsts.CONTENT_URI_ADRESS,
											null,
											ContactsConsts.ADRESS_UNIQUE_ID + "=?",
											new String[] { unique_id }, null);
						} else {
							// Read Contact OTHER Address
							unique_id = "O" + "||" + clientId;
							other_adrs = Email
									.getAppContext()
									.getContentResolver()
									.query(ContactsConsts.CONTENT_URI_ADRESS,
											null,
											ContactsConsts.ADRESS_CLIENT_ID + "=?",
											new String[] { unique_id }, null);
						}

						if (other_adrs != null && other_adrs.moveToFirst()) {
							String other_adress = "";
							String streetother = other_adrs
									.getString(other_adrs
											.getColumnIndex(ContactsConsts.STREET));
							if (streetother != null && !streetother.isEmpty()) {
								other_adress = other_adress + "Street" + ": "
										+ streetother + "\n";
								contactsModel
										.setcontacts_other_address_street(streetother);

							}
							String cityother = other_adrs.getString(other_adrs
									.getColumnIndex(ContactsConsts.CITY));
							if (cityother != null && !cityother.isEmpty()) {
								other_adress = other_adress + "City" + ": "
										+ cityother + "\n";
								contactsModel
										.setcontacts_other_address_city(cityother);
							}
							String stateother = other_adrs.getString(other_adrs
									.getColumnIndex(ContactsConsts.STATE));
							if (stateother != null && !stateother.isEmpty()) {
								other_adress = other_adress + "State" + ": "
										+ stateother + "\n";
								contactsModel
										.setcontacts_other_address_state(stateother);
							}

							String zipother = other_adrs.getString(other_adrs
									.getColumnIndex(ContactsConsts.ZIP));
							if (zipother != null && !zipother.isEmpty()) {
								other_adress = other_adress + "Zip" + ": "
										+ zipother + "\n";
								contactsModel
										.setcontacts_other_address_postal_code(zipother);
							}
							String countryother = other_adrs
									.getString(other_adrs
											.getColumnIndex(ContactsConsts.COUNTRY));
							if (countryother != null && !countryother.isEmpty()) {
								other_adress = other_adress + "Country" + ": "
										+ countryother + "\n";
								contactsModel
										.setcontacts_other_address_country(countryother);
							}

							if (other_adress != null && !other_adress.isEmpty()) {
								contactsModel
										.setContacts_other_location(other_adress);
								contactsModel
										.setContact_other_location_type("OTHER");
							}
							ContactsLog.d("Utils", "other_adress : "
									+ other_adress);
							other_adrs.close();
						}
					} else {

						contactsModel.setContacts_other_location(null);
					}
				} catch (Exception e) {
					ContactsLog.e("ContactsUtilities",
							"Address : " + e.toString());
					if (other_adrs != null) {
						other_adrs.close();
					}
				}

				// Read Contact Company
				if (cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_COMPANY)) != null) {
					contactsModel
							.setcontacts_company_name(cursor.getString(cursor
									.getColumnIndex(ContactsConsts.CONTACT_COMPANY)));
				}
				// Read Contact Designation
				if (cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_JOB_TITLE)) != null) {
					contactsModel.setcontacts_title(cursor.getString(cursor
							.getColumnIndex(ContactsConsts.CONTACT_JOB_TITLE)));
				}
				if (cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_DEPARTMENT)) != null) {
					contactsModel
							.setcontacts_department(cursor.getString(cursor
									.getColumnIndex(ContactsConsts.CONTACT_DEPARTMENT)));
				}

				// Read Contact Nick name
				if (cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_NICK_NAME)) != null) {
					contactsModel.setContact_nick_name(cursor.getString(cursor
							.getColumnIndex(ContactsConsts.CONTACT_NICK_NAME)));
				}
				// Read Contact Web Address
				if (cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_WEB_ADRESS)) != null) {
					contactsModel
							.setContact_website(cursor.getString(cursor
									.getColumnIndex(ContactsConsts.CONTACT_WEB_ADRESS)));
				}
				// Read Contact Internet Call
				if (cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_INTERNETCALL)) != null) {
					contactsModel
							.setContact_internetcall(cursor.getString(cursor
									.getColumnIndex(ContactsConsts.CONTACT_INTERNETCALL)));
				}
				// Read Contact IM name
				try {
					if (cursor.getString(cursor
							.getColumnIndex(ContactsConsts.CONTACT_IM_NAME)) != null) {
						/*
						 * contactsModel.setContact_im_address(cursor.getString(
						 * cursor
						 * .getColumnIndex(ContactsConsts.CONTACT_IM_NAME)));
						 */

						String im_details = cursor
								.getString(cursor
										.getColumnIndex(ContactsConsts.CONTACT_IM_NAME));
						ContactsLog.d("UTILS", "Imdetails" + im_details);
						ArrayList<HashMap<String, String>> imVal = new ArrayList<HashMap<String, String>>();
						String[] splitParentStr = im_details.split(":");
						for (int i = 0; i < splitParentStr.length; i++) {
							if (splitParentStr[i].contains("=")) {
								String[] imArray = splitParentStr[i].split("=");
								HashMap<String, String> row = new HashMap<String, String>();
								String val = imArray[0];
								if (val == null || val.isEmpty()) {
									val = "AIM";
								}
								row.put("imtype" + i, val);
								row.put("im" + i, imArray[1]);
								imVal.add(row);
							}
						}

						for (int i = 0; i < imVal.size(); i++) {
							if ((imVal.size() > 0 && imVal.get(i) != null && i == 0)) {

								contactsModel.setContact_im_address(imVal
										.get(i).get("im" + i));
								contactsModel.setContact_im_address_type(imVal
										.get(i).get("imtype" + i));
							}
							if ((imVal.size() > 0 && imVal.get(i) != null && i == 1)) {
								contactsModel.setContact_im_address1(imVal.get(
										i).get("im" + i));
								contactsModel.setContact_im_address1_type(imVal
										.get(i).get("imtype" + i));
							}
							if ((imVal.size() > 0 && imVal.get(i) != null && i == 2)) {
								contactsModel.setContact_im_address2(imVal.get(
										i).get("im" + i));
								contactsModel.setContact_im_address2_type(imVal
										.get(i).get("imtype" + i));
							}
							if ((imVal.size() > 0 && imVal.get(i) != null && i == 3)) {
								contactsModel
										.setContact_custom_im_address(imVal
												.get(i).get("im" + i));
								contactsModel
										.setContact_custom_im_address_type(imVal
												.get(i).get("imtype" + i));
							}
							if ((imVal.size() > 0 && imVal.get(i) != null && i == 4)) {
								contactsModel
										.setContact_custom_im1_address(imVal
												.get(i).get("im" + i));
								contactsModel
										.setContact_custom_im1_address_type(imVal
												.get(i).get("imtype" + i));
							}
							if ((imVal.size() > 0 && imVal.get(i) != null && i == 5)) {
								contactsModel
										.setContact_custom_im2_address(imVal
												.get(i).get("im" + i));
								contactsModel
										.setContact_custom_im2_address_type(imVal
												.get(i).get("imtype" + i));
							}
						}
					}
				} catch (Exception e) {
					ContactsLog.e("ContactsUtilities",
							"IM Address : " + e.toString());
				}
				// Read Contact Notes

				// System.out.println("CONTACT_NOTES  :"+cursor.getString(cursor.getColumnIndex(ContactsConsts.CONTACT_NOTES)));

				if (cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_NOTES)) != null) {
					contactsModel.setContact_notes(cursor.getString(cursor
							.getColumnIndex(ContactsConsts.CONTACT_NOTES)));
				}

				if (cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_IS_FAVORITE)) != null) {
					contactsModel
							.setContact_isFavorite(Integer.valueOf(cursor.getString(cursor
									.getColumnIndex(ContactsConsts.CONTACT_IS_FAVORITE))));
				}

				if (cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_RINGTONE_PATH)) != null) {
					contactsModel
							.setContacts_ringtone_uri(cursor.getString(cursor
									.getColumnIndex(ContactsConsts.CONTACT_RINGTONE_PATH)));
				}

				corporateContacts.add(contactsModel);
				cursor.moveToNext();
			}
		}
		if (cursor != null) {
			cursor.close();
		}

		return corporateContacts;

	}

	// Load Corporate Contacts Details for Single Contact based on the ID
	public static ArrayList<ContactsModel> getNativeContactsDetailsFromCursor(
			ContentResolver contentResolver, String receivedExtra) {

		ArrayList<ContactsModel> nativeContacts = new ArrayList<ContactsModel>();
		ContactsModel contactsModel = new ContactsModel();

		try {
			// Phone Cursor for Ph number and type
			// Log.i("Contact utilities native contact","phone"+receivedExtra);

			Cursor pCur = contentResolver.query(
					ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
					ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
					new String[] { receivedExtra }, null);
			List<String> phoneVal = new ArrayList<String>();
			List<String> phoneTypeVal = new ArrayList<String>();
			while (pCur.moveToNext()) {
				phoneVal.add(pCur.getString(pCur
						.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
				int type = pCur
						.getInt(pCur
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
				switch (type) {
				case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
					phoneTypeVal.add("Mobile");
					break;
				case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
					phoneTypeVal.add("Home");
					break;
				case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
					phoneTypeVal.add("Work");
					break;
				case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK:
					phoneTypeVal.add("Work Fax");
					break;
				case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME:
					phoneTypeVal.add("Home Fax");
					break;
				case ContactsContract.CommonDataKinds.Phone.TYPE_PAGER:
					phoneTypeVal.add("Pager");
					break;
				case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER:
					phoneTypeVal.add("Other");
					break;
				case ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM:
					phoneTypeVal.add("Custom");
					break;
				case ContactsContract.CommonDataKinds.Phone.TYPE_CALLBACK:
					phoneTypeVal.add("Callback");
					break;
				default:
					break;
				}
			}
			pCur.close();

			/*
			 * if (phoneVal.size() > 0 && phoneVal.get(0) != null) {
			 * contactsModel.setcontacts_mobile_telephone_number(phoneVal
			 * .get(0)); } if (phoneVal.size() > 1 && phoneVal.get(1) != null) {
			 * contactsModel.setcontacts_business_telephone_number(phoneVal
			 * .get(1)); } if (phoneTypeVal.size() > 0 && phoneTypeVal.get(0) !=
			 * null) {
			 * contactsModel.setContact_phone_number_mobile_type1(phoneTypeVal
			 * .get(0)); } if (phoneTypeVal.size() > 1 && phoneTypeVal.get(1) !=
			 * null) {
			 * contactsModel.setContact_phone_number_mobile_type2(phoneTypeVal
			 * .get(1)); }
			 */

			for (int i = 0; i < phoneVal.size(); i++) {
				if ((phoneVal.size() > 0 && phoneVal.get(i) != null && i == 0)) {

					contactsModel.setcontacts_mobile_telephone_number(phoneVal
							.get(0));
					contactsModel
							.setContact_phone_number_mobile_type1(phoneTypeVal
									.get(0));
				}
				if ((phoneVal.size() > 0 && phoneVal.get(i) != null && i == 1)) {
					contactsModel
							.setcontacts_business_telephone_number(phoneVal
									.get(1));
					contactsModel
							.setContact_phone_number_mobile_type2(phoneTypeVal
									.get(1));
				}
				if ((phoneVal.size() > 0 && phoneVal.get(i) != null && i == 2)) {
					contactsModel
							.setcontacts_assistant_telephone_number(phoneVal
									.get(2));
					contactsModel
							.setContacts_assistant_telephone_number_type(phoneTypeVal
									.get(2));
				}
				if ((phoneVal.size() > 0 && phoneVal.get(i) != null && i == 3)) {
					contactsModel
							.setcontacts_business2_telephone_number(phoneVal
									.get(3));
					contactsModel
							.setContacts_business2_telephone_number_type(phoneTypeVal
									.get(3));
				}
				if ((phoneVal.size() > 0 && phoneVal.get(i) != null && i == 4)) {
					contactsModel.setcontacts_business_fax_number(phoneVal
							.get(4));
					contactsModel
							.setContacts_business_fax_number_type(phoneTypeVal
									.get(4));
				}
				if ((phoneVal.size() > 0 && phoneVal.get(i) != null && i == 5)) {
					contactsModel.setcontacts_car_telephone_number(phoneVal
							.get(5));
					contactsModel
							.setContacts_car_telephone_numbe_type(phoneTypeVal
									.get(5));
				}

				if ((phoneVal.size() > 0 && phoneVal.get(i) != null && i == 6)) {
					contactsModel.setcontacts_home2_telephone_number(phoneVal
							.get(6));
					contactsModel
							.setContacts_home2_telephone_number_type(phoneTypeVal
									.get(6));
				}
				if ((phoneVal.size() > 0 && phoneVal.get(i) != null && i == 7)) {
					contactsModel.setcontacts_home_fax_number(phoneVal.get(7));
					contactsModel.setContacts_home_fax_number_type(phoneTypeVal
							.get(7));
				}
				if ((phoneVal.size() > 0 && phoneVal.get(i) != null && i == 8)) {
					contactsModel.setcontacts_home_telephone_number(phoneVal
							.get(8));
					contactsModel
							.setContacts_home_telephone_number_type(phoneTypeVal
									.get(8));
				}
				if ((phoneVal.size() > 0 && phoneVal.get(i) != null && i == 9)) {
					contactsModel.setcontacts_pager_number(phoneVal.get(9));
					contactsModel.setContacts_pager_number_type(phoneTypeVal
							.get(9));
				}
				if ((phoneVal.size() > 0 && phoneVal.get(i) != null && i == 10)) {
					contactsModel.setcontacts_radio_telephone_number(phoneVal
							.get(10));
					contactsModel
							.setContacts_radio_telephone_number_type(phoneTypeVal
									.get(10));
				}
				if ((phoneVal.size() > 0 && phoneVal.get(i) != null && i == 11)) {
					contactsModel.setContacts2_company_main_phone(phoneVal
							.get(11));
					contactsModel
							.setContacts2_company_main_phone_type(phoneTypeVal
									.get(11));
				}
				if ((phoneVal.size() > 0 && phoneVal.get(i) != null && i == 12)) {
					contactsModel.setContact_custom_phone1(phoneVal.get(12));
					contactsModel.setContact_custom_phone1_type(phoneTypeVal
							.get(12));
				}
				if ((phoneVal.size() > 0 && phoneVal.get(i) != null && i == 13)) {
					contactsModel.setContact_custom_phone2(phoneVal.get(13));
					contactsModel.setContact_custom_phone2_type(phoneTypeVal
							.get(13));
				}
				if ((phoneVal.size() > 0 && phoneVal.get(i) != null && i == 14)) {
					contactsModel.setContact_custom_phone3(phoneVal.get(14));
					contactsModel.setContact_custom_phone3_type(phoneTypeVal
							.get(14));
				}

			}

		} catch (Exception ex) {
			ContactsLog.e("ContactsUtilities",
					" Native Phone: " + ex.toString());
		}

		try {
			// Email Cursor
			// Log.i("Contact utilities native contact","Email"+receivedExtra);

			Cursor emailCur = contentResolver.query(
					ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
					ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
					new String[] { receivedExtra }, null);
			List<String> emailVal = new ArrayList<String>();
			List<String> emailTypeVal = new ArrayList<String>();
			while (emailCur.moveToNext()) {
				emailVal.add(emailCur.getString(emailCur
						.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)));
				int type = emailCur
						.getInt(emailCur
								.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
				switch (type) {
				case ContactsContract.CommonDataKinds.Email.TYPE_HOME:
					emailTypeVal.add("Home");
					break;
				case ContactsContract.CommonDataKinds.Email.TYPE_WORK:
					emailTypeVal.add("Work");
					break;
				case ContactsContract.CommonDataKinds.Email.TYPE_OTHER:
					emailTypeVal.add("Other");
					break;
				case ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM:
					emailTypeVal.add("Custom");
					break;
				default:
					emailTypeVal.add("Internet");
					break;
				}
			}
			emailCur.close();

			/*
			 * if (emailVal.size() > 0 && emailVal.get(0) != null) {
			 * contactsModel.setcontacts_email1_address(emailVal.get(0)); } if
			 * (emailTypeVal.size() > 0 && emailTypeVal.get(0) != null) {
			 * contactsModel.setContact_email1_type(emailTypeVal.get(0)); }
			 */

			for (int i = 0; i < emailVal.size(); i++) {
				if ((emailVal.size() > 0 && emailVal.get(i) != null && i == 0)) {

					contactsModel.setcontacts_email1_address(emailVal.get(0));
					contactsModel.setContact_email1_type(emailTypeVal.get(0));
				}
				if ((emailVal.size() > 0 && emailVal.get(i) != null && i == 1)) {
					contactsModel.setcontacts_email2_address(emailVal.get(1));
					contactsModel.setContact_email2_type(emailTypeVal.get(1));
				}
				if ((emailVal.size() > 0 && emailVal.get(i) != null && i == 2)) {
					contactsModel.setcontacts_email3_address(emailVal.get(2));
					contactsModel.setContact_email3_type(emailTypeVal.get(2));
				}
				if ((emailVal.size() > 0 && emailVal.get(i) != null && i == 3)) {
					contactsModel.setContacts_custom_email1_address(emailVal
							.get(3));
					contactsModel.setContact_custom_email1_type(emailTypeVal
							.get(3));
				}
				if ((emailVal.size() > 0 && emailVal.get(i) != null && i == 4)) {
					contactsModel.setContacts_custom_email2_address(emailVal
							.get(4));
					contactsModel.setContact_custom_email2_type(emailTypeVal
							.get(4));
				}
				if ((emailVal.size() > 0 && emailVal.get(i) != null && i == 5)) {
					/*
					 * System.out.println(" INSIDE)    ::: ####### " +
					 * emailVal.get(5));
					 */
					contactsModel.setContacts_custom_email3_address(emailVal
							.get(5));
					contactsModel.setContact_custom_email3_type(emailTypeVal
							.get(5));
				}
			}

		} catch (Exception ex) {
			ContactsLog
					.e("ContactsUtilities", "Native email: " + ex.toString());
		}

		// List Contact Name
		try {

			// Log.i("Contact utilities native contact","Contact name"+receivedExtra);

			Cursor nameCur = contentResolver
					.query(ContactsContract.Data.CONTENT_URI,
							null,
							ContactsContract.Data.MIMETYPE + " = ? AND "
									+ ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID + " = ?",
									new String[] {
											ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
											receivedExtra },
									ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME);
			while (nameCur.moveToNext()) {

				if (nameCur
						.getString(nameCur
								.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME)) != null) {
					contactsModel
							.setcontacts_first_name(nameCur.getString(nameCur
									.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME)));
				}

				if (nameCur
						.getString(nameCur
								.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME)) != null) {
					contactsModel
							.setcontacts_middle_name(nameCur.getString(nameCur
									.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME)));
				}

				if (nameCur
						.getString(nameCur
								.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME)) != null) {
					contactsModel
							.setcontacts_last_name(nameCur.getString(nameCur
									.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME)));
				}

				if (nameCur
						.getString(nameCur
								.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.PREFIX)) != null) {
					contactsModel
							.setContacts_name_prefix(nameCur.getString(nameCur
									.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.PREFIX)));
				}

				if (nameCur
						.getString(nameCur
								.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.SUFFIX)) != null) {
					contactsModel
							.setContacts_name_suffix(nameCur.getString(nameCur
									.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.SUFFIX)));
				}

				if (nameCur
						.getString(nameCur
								.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.PHONETIC_FAMILY_NAME)) != null) {
					contactsModel
							.setContact_phonetic_family_name(nameCur.getString(nameCur
									.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.PHONETIC_FAMILY_NAME)));
				}

				if (nameCur
						.getString(nameCur
								.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.PHONETIC_GIVEN_NAME)) != null) {
					contactsModel
							.setContact_phonetic_given_name(nameCur.getString(nameCur
									.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.PHONETIC_GIVEN_NAME)));
				}
				if (nameCur
						.getString(nameCur
								.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.PHONETIC_MIDDLE_NAME)) != null) {
					contactsModel
							.setContact_phonetic_middle_name(nameCur.getString(nameCur
									.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.PHONETIC_MIDDLE_NAME)));
				}

				if (nameCur
						.getString(nameCur
								.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.STARRED)) != null) {
					contactsModel
							.setContact_isFavorite(Integer.valueOf(nameCur.getString(nameCur
									.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.STARRED))));
				}

			}
			nameCur.close();
		} catch (Exception ex) {
			ContactsLog.e("ContactsUtilities", "Native Name: " + ex.toString());
		}

		// Contact Organization Cursor
		String orgWhere = ContactsContract.Data.CONTACT_ID + " = ? AND "
				+ ContactsContract.Data.MIMETYPE + " = ?";
		String[] orgWhereParams = new String[] { receivedExtra,
				ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE };
		Cursor orgCur = null;
		try {
			// Log.i("Contact utilities native contact","org"+receivedExtra);

			orgCur = contentResolver.query(ContactsContract.Data.CONTENT_URI,
					null, orgWhere, orgWhereParams, null);
			if (orgCur != null && orgCur.getCount() > 0) {
				orgCur.moveToFirst();
				// while (orgCur.moveToNext()) {
				if (orgCur
						.getString(orgCur
								.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY)) != null) {
					contactsModel
							.setcontacts_company_name(orgCur.getString(orgCur
									.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY)));
				}

				if (orgCur
						.getString(orgCur
								.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE)) != null) {
					contactsModel
							.setcontacts_title(orgCur.getString(orgCur
									.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE)));
				}

				if (orgCur
						.getString(orgCur
								.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DEPARTMENT)) != null) {
					contactsModel
							.setcontacts_department(orgCur.getString(orgCur
									.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DEPARTMENT)));
				}
				// }
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (orgCur != null) {
				orgCur.close();
			}

		}

		// Contact Address Cursor
		String addrwhere = ContactsContract.Data.CONTACT_ID + " = ? AND "
				+ ContactsContract.Data.MIMETYPE + " = ?";
		String[] whereaddrParameters = new String[] {
				receivedExtra,
				ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE };
		Cursor addrCur = null;
		try {


			addrCur = contentResolver.query(ContactsContract.Data.CONTENT_URI,
					null, addrwhere, whereaddrParameters, null);
			if (addrCur != null && addrCur.getCount() > 0) {
				addrCur.moveToFirst();
								
					while (addrCur.isAfterLast() == false) {
						/*ContactsLog.d("ADDRESSS    :::   ===",""+addrCur.getString(addrCur
									.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.DATA)));*/
						
						if (addrCur.getInt(addrCur
										.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE)) != 0) {

							int type = addrCur.getInt(addrCur
											.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE));

							switch (type) {
							case ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME:
								contactsModel.setContact_home_location_type("HOME");
								
								if (addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.DATA)) != null) {
									contactsModel.setContacts_home_location(addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.DATA)));
								}
								if (addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET)) != null) {
									contactsModel.setcontacts_home_address_street(addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY)));
								}
								
								if (addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY)) != null) {
									contactsModel.setcontacts_home_address_city(addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY)));
								}

								if (addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION)) != null) {
									contactsModel.setcontacts_home_address_state(addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION)));
								}

								if (addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE)) != null) {
									contactsModel.setcontacts_home_address_postal_code(addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE)));
								}
								
								if (addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY)) != null) {
									contactsModel.setcontacts_home_address_country(addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE)));
								}
								
									
								break;
							case ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK:
								contactsModel.setContact_business_location_type("WORK");
								
								if (addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.DATA)) != null) {
									contactsModel.setContacts_business_location(addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.DATA)));
								}
								if (addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET)) != null) {
									contactsModel.setcontacts_business_address_street(addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY)));
								}
								
								if (addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY)) != null) {
									contactsModel.setcontacts_business_address_city(addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY)));
								}

								if (addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION)) != null) {
									contactsModel.setcontacts_business_address_state(addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION)));
								}

								if (addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE)) != null) {
									contactsModel.setcontacts_business_address_postal_code(addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE)));
								}
								
								if (addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY)) != null) {
									contactsModel.setcontacts_business_address_country(addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE)));
								}
								
								
								break;
							case ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER:
								contactsModel.setContact_other_location_type("OTHER");
								
								if (addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.DATA)) != null) {
									contactsModel.setContacts_other_location(addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.DATA)));
								}
								if (addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET)) != null) {
									contactsModel.setcontacts_other_address_street(addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY)));
								}
								
								if (addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY)) != null) {
									contactsModel.setcontacts_other_address_city(addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY)));
								}

								if (addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION)) != null) {
									contactsModel.setcontacts_other_address_state(addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION)));
								}

								if (addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE)) != null) {
									contactsModel.setcontacts_other_address_postal_code(addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE)));
								}
								
								if (addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY)) != null) {
									contactsModel.setcontacts_other_address_country(addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE)));
								}
								
								break;
							default:
								break;
							}
						}
						addrCur.moveToNext();
					}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (addrCur != null) {
				addrCur.close();
			}

		}

		/*
		 * // Contact Photo Cursor String[] photocols = {
		 * ContactsContract.CommonDataKinds.Photo.PHOTO }; String photofilter =
		 * ContactsContract.Data.CONTACT_ID + " = ? " + " and " +
		 * ContactsContract.Data.MIMETYPE + " = '" +
		 * ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'";
		 * String[] photoparams = { String.valueOf(receivedExtra) }; Cursor
		 * photoCur = null;
		 * 
		 * try { photoCur = contentResolver.query(
		 * ContactsContract.Data.CONTENT_URI, photocols, photofilter,
		 * photoparams, null); if (photoCur != null && photoCur.getCount() > 0)
		 * { photoCur.moveToFirst(); while (photoCur.moveToNext()) { if
		 * (photoCur .getBlob(photoCur
		 * .getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO)) !=
		 * null) { contactsModel .setContacts_image(photoCur.getBlob(photoCur
		 * .getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO))); } }
		 * }
		 * 
		 * } catch (Exception e) { e.printStackTrace(); } finally { if (photoCur
		 * != null) { photoCur.close(); } }
		 */

		// Contact Photo Cursor
		String[] photocols = { ContactsContract.CommonDataKinds.Photo.PHOTO };
		String photofilter = ContactsContract.Data.CONTACT_ID + " = ? "
				+ " AND " + ContactsContract.Data.MIMETYPE + " = ?";
		String[] photoparams = { String.valueOf(receivedExtra) ,  ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE};
		
		Cursor photoCur = contentResolver.query(
				ContactsContract.Data.CONTENT_URI, photocols, photofilter,
				photoparams, null);

		if (photoCur != null && photoCur.getCount() > 0) {
			photoCur.moveToFirst();
			// while (photoCur.moveToNext()) {
			if (photoCur
					.getBlob(photoCur
							.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO)) != null) {
				contactsModel
						.setContacts_image(photoCur.getBlob(photoCur
								.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO)));
			}
			// }
		}

		if (photoCur != null) {
			photoCur.close();
		}

		// Contact Website Cursor
		String[] cols = { ContactsContract.CommonDataKinds.Website.URL };
		String filter = ContactsContract.Data.CONTACT_ID + " = ? " + " AND "
				+ ContactsContract.Data.MIMETYPE + " = ?";
		String[] params = { String.valueOf(receivedExtra), ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE};
		Cursor webCur = null;

		try {

			// Log.i("Contact utilities native contact","web"+receivedExtra);

			webCur = contentResolver.query(ContactsContract.Data.CONTENT_URI,
					cols, filter, params, null);
			if (webCur != null && webCur.getCount() > 0) {
				webCur.moveToFirst();
				// while (webCur.moveToNext()) {
				if (webCur
						.getString(webCur
								.getColumnIndex(ContactsContract.CommonDataKinds.Website.URL)) != null) {
					contactsModel
							.setContact_website(webCur.getString(webCur
									.getColumnIndex(ContactsContract.CommonDataKinds.Website.URL)));
				}
				// }
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (webCur != null) {
				webCur.close();
			}
		}

		// Contact Notes Cursor
		String where = ContactsContract.Data.CONTACT_ID + " = ? AND "
				+ ContactsContract.Data.MIMETYPE + " = ?";
		String[] whereParameters = new String[] { receivedExtra,
				ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE };
		Cursor notesCur = null;

		try {

			// Log.i("Contact utilities native contact","notes"+receivedExtra);

			notesCur = contentResolver.query(ContactsContract.Data.CONTENT_URI,
					null, where, whereParameters, null);
			if (notesCur != null && notesCur.getCount() > 0) {
				notesCur.moveToFirst();
				// while (notesCur.moveToNext()) {
				if (notesCur
						.getString(notesCur
								.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE)) != null) {
					contactsModel
							.setContact_notes(notesCur.getString(notesCur
									.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE)));
				}
				// }
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (notesCur != null) {
				notesCur.close();
			}
		}

		try {
			// IMMMMMMM Cursor

			// Log.i("Contact utilities native contact","IM"+receivedExtra);

			String imwhere = ContactsContract.Data.CONTACT_ID + " = ? AND "
					+ ContactsContract.Data.MIMETYPE + " = ?";
			String[] whereImParameters = new String[] { receivedExtra,
					ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE };

			Cursor imCur = null;
			imCur = contentResolver.query(ContactsContract.Data.CONTENT_URI,
					null, imwhere, whereImParameters, null);

			List<String> ImVal = new ArrayList<String>();
			List<String> ImTypeVal = new ArrayList<String>();
			while (imCur.moveToNext()) {
				ImVal.add(imCur.getString(imCur
						.getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA)));
				int type = imCur
						.getInt(imCur
								.getColumnIndex(ContactsContract.CommonDataKinds.Im.PROTOCOL));
				switch (type) {

				case ContactsContract.CommonDataKinds.Im.PROTOCOL_AIM:
					ImTypeVal.add("Aim");
					break;
				case ContactsContract.CommonDataKinds.Im.PROTOCOL_NETMEETING:
					ImTypeVal.add("Net Meeting");
					break;
				case ContactsContract.CommonDataKinds.Im.PROTOCOL_YAHOO:
					ImTypeVal.add("Yahoo");
					break;
				case ContactsContract.CommonDataKinds.Im.PROTOCOL_SKYPE:
					ImTypeVal.add("Skype");
					break;
				case ContactsContract.CommonDataKinds.Im.PROTOCOL_QQ:
					ImTypeVal.add("QQ");
					break;
				case ContactsContract.CommonDataKinds.Im.PROTOCOL_GOOGLE_TALK:
					ImTypeVal.add("Google Talk");
					break;
				case ContactsContract.CommonDataKinds.Im.PROTOCOL_ICQ:
					ImTypeVal.add("ICQ");
					break;
				case ContactsContract.CommonDataKinds.Im.PROTOCOL_JABBER:
					ImTypeVal.add("Jabber");
					break;
				case ContactsContract.CommonDataKinds.Im.PROTOCOL_CUSTOM:
					ImTypeVal.add("Custom");
					break;
				default:
					break;

				}
			}
			if (imCur != null) {
				imCur.close();
			}

			for (int i = 0; i < ImVal.size(); i++) {
				if ((ImVal.size() > 0 && ImVal.get(i) != null && i == 0)) {

					contactsModel.setContact_im_address(ImVal.get(0));
					contactsModel.setContact_im_address_type(ImTypeVal.get(0));
				}
				if ((ImVal.size() > 0 && ImVal.get(i) != null && i == 1)) {
					contactsModel.setContact_im_address1(ImVal.get(1));
					contactsModel.setContact_im_address1_type(ImTypeVal.get(1));
				}
				if ((ImVal.size() > 0 && ImVal.get(i) != null && i == 2)) {
					contactsModel.setContact_im_address2(ImVal.get(2));
					contactsModel.setContact_im_address2_type(ImTypeVal.get(2));
				}
				if ((ImVal.size() > 0 && ImVal.get(i) != null && i == 3)) {
					contactsModel.setContact_custom_im_address(ImVal.get(3));
					contactsModel.setContact_custom_im_address_type(ImTypeVal
							.get(3));
				}
				if ((ImVal.size() > 0 && ImVal.get(i) != null && i == 4)) {
					contactsModel.setContact_custom_im1_address(ImVal.get(4));
					contactsModel.setContact_custom_im1_address_type(ImTypeVal
							.get(4));
				}
				if ((ImVal.size() > 0 && ImVal.get(i) != null && i == 5)) {
					contactsModel.setContact_custom_im2_address(ImVal.get(5));
					contactsModel.setContact_custom_im2_address_type(ImTypeVal
							.get(5));
				}
			}

		} catch (Exception ex) {
			ContactsLog.e("ContactsUtilities", "Native IM: " + ex.toString());
		} finally {

		}

		/*
		 * // Contact Im Cursor String imwhere =
		 * ContactsContract.Data.CONTACT_ID + " = ? AND " +
		 * ContactsContract.Data.MIMETYPE + " = ?"; String[] whereImParameters =
		 * new String[] { receivedExtra,
		 * ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE }; Cursor imCur
		 * = null;
		 * 
		 * try { imCur =
		 * contentResolver.query(ContactsContract.Data.CONTENT_URI, null,
		 * imwhere, whereImParameters, null); if (imCur != null &&
		 * imCur.getCount() > 0) { imCur.moveToFirst(); while
		 * (imCur.moveToNext()) {
		 * 
		 * if (imCur.getString(imCur
		 * .getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA)) != null) {
		 * contactsModel .setContact_im_address(imCur.getString(imCur
		 * .getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA))); }
		 * 
		 * if (imCur .getInt(imCur
		 * .getColumnIndex(ContactsContract.CommonDataKinds.Im.PROTOCOL)) != 0)
		 * {
		 * 
		 * int type = imCur .getInt(imCur
		 * .getColumnIndex(ContactsContract.CommonDataKinds.Im.PROTOCOL));
		 * switch (type) { case
		 * ContactsContract.CommonDataKinds.Im.PROTOCOL_AIM:
		 * contactsModel.setContact_im_address_type("Aim"); break; case
		 * ContactsContract.CommonDataKinds.Im.PROTOCOL_NETMEETING:
		 * contactsModel.setContact_im_address_type("Net Meeting"); break; case
		 * ContactsContract.CommonDataKinds.Im.PROTOCOL_YAHOO:
		 * contactsModel.setContact_im_address_type("Yahoo"); break; case
		 * ContactsContract.CommonDataKinds.Im.PROTOCOL_SKYPE:
		 * contactsModel.setContact_im_address_type("Skype"); break; case
		 * ContactsContract.CommonDataKinds.Im.PROTOCOL_QQ:
		 * contactsModel.setContact_im_address_type("QQ"); break; case
		 * ContactsContract.CommonDataKinds.Im.PROTOCOL_GOOGLE_TALK:
		 * contactsModel.setContact_im_address_type("Google Talk"); break; case
		 * ContactsContract.CommonDataKinds.Im.PROTOCOL_ICQ:
		 * contactsModel.setContact_im_address_type("ICQ"); break; case
		 * ContactsContract.CommonDataKinds.Im.PROTOCOL_JABBER:
		 * contactsModel.setContact_im_address_type("Jabber"); break; case
		 * ContactsContract.CommonDataKinds.Im.PROTOCOL_CUSTOM:
		 * contactsModel.setContact_im_address_type("Custom"); break; default:
		 * break; }
		 * 
		 * }
		 * 
		 * } }
		 * 
		 * } catch (Exception e) { e.printStackTrace(); } finally { if (imCur !=
		 * null) { imCur.close(); } }
		 */
		// Contact Nick name Cursor
		String nickwhere = ContactsContract.Data.CONTACT_ID + " = ? AND "
				+ ContactsContract.Data.MIMETYPE + " = ?";
		String[] wherenickParameters = new String[] { receivedExtra,
				ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE };
		Cursor nickCur = null;

		try {

			// Log.i("Contact utilities native contact","Nickname"+receivedExtra);

			nickCur = contentResolver.query(ContactsContract.Data.CONTENT_URI,
					null, nickwhere, wherenickParameters, null);
			if (nickCur != null && nickCur.getCount() > 0) {

				// Log.i("Contact utilities native contact","Nickname count"+nickCur.getCount());
				nickCur.moveToFirst();
				// while (nickCur.moveToNext()) {
				if (nickCur
						.getString(nickCur
								.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.DATA)) != null) {

					/*
					 * Log.i("Contact utilities native contact","Nickname db value"
					 * +nickCur.getString(nickCur
					 * .getColumnIndex(ContactsContract
					 * .CommonDataKinds.Nickname.DATA)).toString());
					 */
					contactsModel
							.setContact_nick_name(nickCur.getString(nickCur
									.getColumnIndex(ContactsContract.CommonDataKinds.Nickname.DATA)));
				}
				// }
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (nickCur != null) {
				nickCur.close();
			}
		}

		// Contact Internetcall Cursor
		String sipaddrwhere = ContactsContract.Data.CONTACT_ID + " = ? AND "
				+ ContactsContract.Data.MIMETYPE + " = ?";
		String[] sipaddrParameters = new String[] { receivedExtra,
				ContactsContract.CommonDataKinds.SipAddress.CONTENT_ITEM_TYPE };
		Cursor sipaddrCur = null;

		try {
			sipaddrCur = contentResolver.query(
					ContactsContract.Data.CONTENT_URI, null, sipaddrwhere,
					sipaddrParameters, null);
			if (sipaddrCur != null && sipaddrCur.getCount() > 0) {
				sipaddrCur.moveToFirst();
				// while (sipaddrCur.moveToNext()) {
				if (sipaddrCur
						.getString(sipaddrCur
								.getColumnIndex(ContactsContract.CommonDataKinds.SipAddress.SIP_ADDRESS)) != null) {
					contactsModel
							.setContact_internetcall(sipaddrCur.getString(sipaddrCur
									.getColumnIndex(ContactsContract.CommonDataKinds.SipAddress.SIP_ADDRESS)));
				}
				// }
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (sipaddrCur != null) {
				sipaddrCur.close();
			}
		}
		
		// Set Contact as Native:
		contactsModel.setNativeContact(true);
		
		nativeContacts.add(contactsModel);
		return nativeContacts;

	}
}
