/*
 * Copyright (C) 2008-2009 Marc Blank
 * Licensed to The Android Open Source Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cognizant.trumobi.exchange.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.content.ContentValues;
import android.content.Entity;
import android.database.Cursor;
import android.net.Uri;
import android.util.Base64;

import com.cognizant.trubox.contacts.db.ContactsConsts;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.em.provider.EmEmailContent.Mailbox;
import com.cognizant.trumobi.exchange.EmEas;
import com.cognizant.trumobi.exchange.EmEasSyncService;
import com.cognizant.trumobi.log.ContactsLog;

/**
 * Sync adapter for EAS Contacts
 * 
 */
public class EmContactsSyncAdapter extends EmAbstractSyncAdapter {

	private static final String TAG = "EasContactsSyncAdapter";
	private static final String SERVER_ID_SELECTION = ContactsConsts.CONTACT_SERVER_ID
			+ "=?";

	private static final Object sSyncKeyLock = new Object();

	public EmContactsSyncAdapter(Mailbox mailbox, EmEasSyncService service) {
		super(mailbox, service);
	}

	@Override
	public boolean isSyncable() {
		// return ContentResolver.getSyncAutomatically(mAccountManagerAccount,
		// ContactsConsts.AUTHORITY);

		return true;
	}

	@Override
	public boolean parse(InputStream is) throws IOException {
		EasContactsSyncParser p = new EasContactsSyncParser(is, this);
		return p.parse();
	}

	/**
	 * We get our SyncKey from ContactsProvider. If there's not one, we set it
	 * to "0" (the reset state) and save that away.
	 */
	@Override
	public String getSyncKey() throws IOException {
		synchronized (sSyncKeyLock) {

			String sync_key = null;
			System.out
					.println("conatacts gettingSyncKey................................");
			synchronized (sSyncKeyLock) {

				String account_name = mAccount.mEmailAddress;

				try {
					Cursor cursor = com.cognizant.trumobi.em.Email
							.getAppContext()
							.getContentResolver()
							.query(ContactsConsts.CONTENT_URI_CONTACTS_SYNC_STATE,
									null, ContactsConsts.ACCOUNT_NAME + "=?",
									new String[] { account_name }, null);

					if (cursor != null) {
						if (cursor.getCount() > 0) {
							cursor.moveToFirst();
							sync_key = cursor.getString(cursor
									.getColumnIndex(ContactsConsts.SYNC_KEY));
						}
						cursor.close();
					}

					if (sync_key == null || sync_key.length() == 0) {
						// Initialize the SyncKey
						setSyncKey("0", false);
						return "0";
					} else {
						ContactsLog.d(" ContactsSyncAdpter", "Got synckey ** "
								+ sync_key);
						return sync_key;
					}
				} catch (IOException x) {
					/*
					 * //System.out.println("get sync key IOException ::: " +
					 * x.toString());
					 */
				} catch (Exception e) {
					// throw new
					// IOException("Can't get SyncKey from ContactsProvider");

					/*
					 * System.out.println("get sync key  Error ::: " +
					 * e.toString());
					 */
				}
			}

			if (sync_key == null) {
				return "0";
			}

			return sync_key;

		}
	}

	/**
	 * We only need to set this when we're forced to make the SyncKey "0" (a
	 * reset). In all other cases, the SyncKey is set within ContactOperations
	 */
	@Override
	public void setSyncKey(String syncKey, boolean inCommands)
			throws IOException {

		// this.mSyncKey = syncKey;

		synchronized (sSyncKeyLock) {
			if ("0".equals(syncKey) || !inCommands) {
				/*
				 * System.out.println("SetSyncKey: is 0 adding account into DB "
				 * + syncKey);
				 */
				// ContentProviderClient client = mService.mContentResolver
				// .acquireContentProviderClient(ContactsContract.AUTHORITY_URI);
				try {
					ContentValues sync_values = new ContentValues();
					sync_values.put(ContactsConsts.ACCOUNT_NAME,
							mAccount.mEmailAddress);
					sync_values.put(ContactsConsts.ACCOUNT_ID,
							EmEas.EXCHANGE_ACCOUNT_MANAGER_TYPE);
					sync_values.put(ContactsConsts.SYNC_KEY, syncKey);

					com.cognizant.trumobi.em.Email

							.getAppContext()
							.getContentResolver()
							.insert(ContactsConsts.CONTENT_URI_CONTACTS_SYNC_STATE,
									sync_values);

				} catch (Exception e) {

					// System.out.println("Error in setsynckey" + e.toString());
					throw new IOException(
							"Can't set SyncKey in ContactsProvider");
				}
			}
			mMailbox.mSyncKey = syncKey;
		}
	}

	public static final class Address {
		String city;
		String country;
		String code;
		String street;
		String state;

		boolean hasData() {
			return city != null || country != null || code != null
					|| state != null || street != null;
		}
	}

	class EasContactsSyncParser extends EmAbstractSyncParser {

		String[] mBindArgument = new String[1];
		String mMailboxIdAsString;
		Uri mAccountUri;

		public EasContactsSyncParser(InputStream in,
				EmContactsSyncAdapter adapter) throws IOException {
			super(in, adapter);
			// mAccountUri =
			// uriWithAccountAndIsSyncAdapter(RawContacts.CONTENT_URI);
			mAccountUri = ContactsConsts.CONTENT_URI_CONTACTS;
		}

		@Override
		public void wipe() {
			mContentResolver.delete(mAccountUri, null, null);
		}

		// public void addData(String serverId, Entity entity, int
		// calling_function)
		// throws IOException {
		//
		// ContactsLog.d(EmContactsSyncAdapter.class.getCanonicalName(),
		// "AddData**** " + serverId);
		//
		// String prefix = null;
		// String firstName = null;
		// String lastName = "";
		// String middleName = null;
		// String suffix = null;
		// String companyName = null;
		// String phone_details = "";
		// String im = null;
		// String assistant = null;
		// String manager = null;
		// String webpage = null;
		// String note_base_body = null;
		// String im_call = null;
		// byte[] picture = null;
		//
		// int work_adress = 0;
		// int home_adress = 0;
		// int other_adress = 0;
		//
		// String note_contact_body = null;
		// String anniversary = null;
		// String birthday = null;
		// String spouse = null;
		// String nickname = null;
		// String yomiFirstName = null;
		// String yomiLastName = null;
		// String yomiCompanyName = null;
		// String job_title = null;
		// String department = null;
		// String officeLocation = null;
		// Address home = new Address();
		// Address work = new Address();
		// Address other = new Address();
		// String email = "";
		//
		// try {
		// while (nextTag(EmTags.SYNC_APPLICATION_DATA) != END) {
		// switch (tag) {
		// case EmTags.CONTACTS_FIRST_NAME:
		// firstName = getValue();
		// break;
		// case EmTags.CONTACTS_LAST_NAME:
		// lastName = getValue();
		// break;
		// case EmTags.CONTACTS_MIDDLE_NAME:
		// middleName = getValue();
		// break;
		// case EmTags.CONTACTS_SUFFIX:
		// suffix = getValue();
		// break;
		// case EmTags.CONTACTS_COMPANY_NAME:
		// companyName = getValue();
		// break;
		// case EmTags.CONTACTS_JOB_TITLE:
		// job_title = getValue();
		// break;
		// case EmTags.CONTACTS_EMAIL1_ADDRESS:
		// String cONTACTS_EMAIL1_ADDRESS = getValue();
		// ContactsLog.d("CONTACTS_EMAIL1_ADDRESS",
		// "cONTACTS_EMAIL1_ADDRESS"
		// + cONTACTS_EMAIL1_ADDRESS);
		// if (cONTACTS_EMAIL1_ADDRESS != null) {
		// email = email + "Email_Adress" + "="
		// + cONTACTS_EMAIL1_ADDRESS + ":";
		//
		// }
		// break;
		// case EmTags.CONTACTS_EMAIL2_ADDRESS:
		// String cONTACTS_EMAIL2_ADDRESS = getValue();
		// if (cONTACTS_EMAIL2_ADDRESS != null) {
		// email = email + "Email_Adress2" + "="
		// + cONTACTS_EMAIL2_ADDRESS + ":";
		//
		// }
		// break;
		// case EmTags.CONTACTS_EMAIL3_ADDRESS:
		// String cONTACTS_EMAIL3_ADDRESS = getValue();
		// if (cONTACTS_EMAIL3_ADDRESS != null) {
		// email = email + "Email_Adress3" + "="
		// + cONTACTS_EMAIL3_ADDRESS + ":";
		//
		// }
		// break;
		// case EmTags.CONTACTS_BUSINESS2_TELEPHONE_NUMBER:
		// String cONTACTS_BUSINESS2_TELEPHONE_NUMBER = getValue();
		// if (cONTACTS_BUSINESS2_TELEPHONE_NUMBER != null) {
		// phone_details = phone_details + "WORK2" + "="
		// + cONTACTS_BUSINESS2_TELEPHONE_NUMBER + ":";
		//
		// }
		// break;
		//
		// case EmTags.CONTACTS_BUSINESS_TELEPHONE_NUMBER:
		// String cONTACTS_BUSINESS_TELEPHONE_NUMBER = getValue();
		// if (cONTACTS_BUSINESS_TELEPHONE_NUMBER != null) {
		// phone_details = phone_details + "WORK" + "="
		// + cONTACTS_BUSINESS_TELEPHONE_NUMBER + ":";
		//
		// }
		//
		// break;
		//
		// case EmTags.CONTACTS_BUSINESS_FAX_NUMBER:
		// String cONTACTS_BUSINESS_FAX_NUMBER = getValue();
		//
		// if (cONTACTS_BUSINESS_FAX_NUMBER != null) {
		// phone_details = phone_details + "FAX_WORK" + "="
		// + cONTACTS_BUSINESS_FAX_NUMBER + ":";
		//
		// }
		//
		// break;
		// case EmTags.CONTACTS2_COMPANY_MAIN_PHONE:
		//
		// String cONTACTS2_COMPANY_MAIN_PHONE = getValue();
		// if (cONTACTS2_COMPANY_MAIN_PHONE != null) {
		// phone_details = phone_details + "COMPANY_MAIN"
		// + "=" + cONTACTS2_COMPANY_MAIN_PHONE + ":";
		//
		// }
		// break;
		// case EmTags.CONTACTS_HOME_FAX_NUMBER:
		//
		// String cONTACTS_HOME_FAX_NUMBER = getValue();
		// if (cONTACTS_HOME_FAX_NUMBER != null) {
		// phone_details = phone_details + "TYPE_FAX_HOME"
		// + "=" + cONTACTS_HOME_FAX_NUMBER + ":";
		//
		// }
		//
		// break;
		// case EmTags.CONTACTS_HOME_TELEPHONE_NUMBER:
		// String cONTACTS_HOME_TELEPHONE_NUMBER = getValue();
		// if (cONTACTS_HOME_TELEPHONE_NUMBER != null) {
		// phone_details = phone_details + "HOME" + "="
		// + cONTACTS_HOME_TELEPHONE_NUMBER + ":";
		//
		// }
		//
		// break;
		// case EmTags.CONTACTS_HOME2_TELEPHONE_NUMBER:
		//
		// String cONTACTS_HOME2_TELEPHONE_NUMBER = getValue();
		// if (cONTACTS_HOME2_TELEPHONE_NUMBER != null) {
		// phone_details = phone_details + "HOME2" + "="
		// + cONTACTS_HOME2_TELEPHONE_NUMBER + ":";
		//
		// }
		//
		// break;
		// case EmTags.CONTACTS_MOBILE_TELEPHONE_NUMBER:
		// String cONTACTS_MOBILE_TELEPHONE_NUMBER = getValue();
		// if (cONTACTS_MOBILE_TELEPHONE_NUMBER != null) {
		// phone_details = phone_details + "MOBILE" + "="
		// + cONTACTS_MOBILE_TELEPHONE_NUMBER + ":";
		//
		// }
		//
		// break;
		// case EmTags.CONTACTS_CAR_TELEPHONE_NUMBER:
		// String cONTACTS_CAR_TELEPHONE_NUMBER = getValue();
		// if (cONTACTS_CAR_TELEPHONE_NUMBER != null) {
		// phone_details = phone_details + "CAR" + "="
		// + cONTACTS_CAR_TELEPHONE_NUMBER + ":";
		//
		// }
		//
		// break;
		// case EmTags.CONTACTS_RADIO_TELEPHONE_NUMBER:
		// String cONTACTS_RADIO_TELEPHONE_NUMBER = getValue();
		//
		// im_call = cONTACTS_RADIO_TELEPHONE_NUMBER;
		//
		// break;
		// case EmTags.CONTACTS_PAGER_NUMBER:
		// String cONTACTS_PAGER_NUMBER = getValue();
		// if (cONTACTS_PAGER_NUMBER != null) {
		// phone_details = phone_details + "PAGER" + "="
		// + cONTACTS_PAGER_NUMBER + ":";
		//
		// }
		//
		// break;
		// case EmTags.CONTACTS_ASSISTANT_TELEPHONE_NUMBER:
		// String cONTACTS_ASSISTANT_TELEPHONE_NUMBER = getValue();
		// if (cONTACTS_ASSISTANT_TELEPHONE_NUMBER != null) {
		// phone_details = phone_details + "ASSISTANT" + "="
		// + cONTACTS_ASSISTANT_TELEPHONE_NUMBER + ":";
		//
		// }
		// break;
		// case EmTags.CONTACTS2_IM_ADDRESS:
		// im = getValue();
		// if (calling_function == 1) {
		// im = "=" + im + ":";
		// } else {
		// }
		// ContactsLog.d(TAG, "IM :: " + im);
		//
		// break;
		// case EmTags.CONTACTS_BUSINESS_ADDRESS_CITY:
		// work.city = getValue();
		// break;
		// case EmTags.CONTACTS_BUSINESS_ADDRESS_COUNTRY:
		// work.country = getValue();
		// break;
		// case EmTags.CONTACTS_BUSINESS_ADDRESS_POSTAL_CODE:
		// work.code = getValue();
		// break;
		// case EmTags.CONTACTS_BUSINESS_ADDRESS_STATE:
		// work.state = getValue();
		// break;
		// case EmTags.CONTACTS_BUSINESS_ADDRESS_STREET:
		// work.street = getValue();
		// break;
		// case EmTags.CONTACTS_HOME_ADDRESS_CITY:
		// home.city = getValue();
		// break;
		// case EmTags.CONTACTS_HOME_ADDRESS_COUNTRY:
		// home.country = getValue();
		// break;
		// case EmTags.CONTACTS_HOME_ADDRESS_POSTAL_CODE:
		// home.code = getValue();
		// break;
		// case EmTags.CONTACTS_HOME_ADDRESS_STATE:
		// home.state = getValue();
		// break;
		// case EmTags.CONTACTS_HOME_ADDRESS_STREET:
		// home.street = getValue();
		// break;
		// case EmTags.CONTACTS_OTHER_ADDRESS_CITY:
		// other.city = getValue();
		// break;
		// case EmTags.CONTACTS_OTHER_ADDRESS_COUNTRY:
		// other.country = getValue();
		// break;
		// case EmTags.CONTACTS_OTHER_ADDRESS_POSTAL_CODE:
		// other.code = getValue();
		// break;
		// case EmTags.CONTACTS_OTHER_ADDRESS_STATE:
		// other.state = getValue();
		// break;
		// case EmTags.CONTACTS_OTHER_ADDRESS_STREET:
		// other.street = getValue();
		// break;
		//
		// case EmTags.CONTACTS_YOMI_COMPANY_NAME:
		// yomiCompanyName = getValue();
		// break;
		//
		// case EmTags.CONTACTS_YOMI_FIRST_NAME:
		// yomiFirstName = getValue();
		// break;
		//
		// case EmTags.CONTACTS_YOMI_LAST_NAME:
		// yomiLastName = getValue();
		// break;
		//
		// case EmTags.CONTACTS2_NICKNAME:
		// nickname = getValue();
		// break;
		//
		// case EmTags.CONTACTS_ASSISTANT_NAME:
		//
		// assistant = getValue();
		// break;
		//
		// case EmTags.CONTACTS2_MANAGER_NAME:
		// manager = getValue();
		//
		// break;
		//
		// case EmTags.CONTACTS_SPOUSE:
		// spouse = getValue();
		// break;
		//
		// case EmTags.CONTACTS_DEPARTMENT:
		// department = getValue();
		// break;
		//
		// case EmTags.CONTACTS_TITLE:
		// prefix = getValue();
		// break;
		//
		// // // EAS Business
		// case EmTags.CONTACTS_OFFICE_LOCATION:
		// officeLocation = getValue();
		// break;
		//
		// // EAS Personal
		// case EmTags.CONTACTS_ANNIVERSARY:
		// anniversary = getValue();
		//
		// break;
		// case EmTags.CONTACTS_BIRTHDAY:
		// birthday = getValue();
		// break;
		//
		// case EmTags.CONTACTS_WEBPAGE:
		// webpage = getValue();
		//
		// break;
		//
		// case EmTags.CONTACTS_PICTURE:
		// String pic = getValue();
		// /* System.out.println("===== PICTURE  :: ============"
		// + pic);*/
		//
		// try {
		// picture = Base64.decode(pic, Base64.NO_WRAP);
		// } catch (Exception e) {
		// ContactsLog.d("CONTACTS_PICTURE", "Unable to parse"
		// + e.toString());
		// }
		//
		// break;
		//
		// case EmTags.BASE_BODY:
		// try {
		// note_base_body = bodyParser();
		// ContactsLog.d("CONTACTS_BODY", "note_base_body **"
		// + note_base_body);
		// } catch (Exception e) {
		//
		// ContactsLog.e("BASE_BODY", "note_base_body  err**"
		// + e.toString());
		// }
		// break;
		//
		// case EmTags.CONTACTS_BODY:
		// try {
		// note_contact_body = getValue();
		// ContactsLog.d("CONTACTS_BODY",
		// "note_contact_body **" + note_contact_body);
		// } catch (Exception e) {
		//
		// ContactsLog.e("CONTACTS_BODY",
		// "note_contact_body  err**" + e.toString());
		// }
		// break;
		//
		// default:
		// skipTag();
		// }
		// }
		// } catch (Exception e) {
		// ContactsLog.e(TAG, "Parsing error: " + e.toString());
		// }
		//
		// // We must have first name, last name, or company name
		// String name = null;
		// if (firstName != null || lastName != null) {
		// if (firstName == null) {
		// name = lastName;
		// } else if (lastName == null) {
		// name = firstName;
		// } else {
		// name = firstName + ' ' + lastName;
		// }
		// } else if (companyName != null) {
		// name = companyName;
		// }
		// ContactsLog.d(TAG, "CONTACT name" + name);
		//
		// if (work.hasData()) {
		// work_adress = 1;
		// String unique_id = "W" + "||" + serverId;
		// if (calling_function == 1) {
		// addPostal(unique_id, work.street, work.city, work.state,
		// work.country, work.code);
		// } else {
		// Cursor cursor = Email
		// .getAppContext()
		// .getContentResolver()
		// .query(ContactsConsts.CONTENT_URI_ADRESS,
		// null,
		// ContactsConsts.ADRESS_UNIQUE_ID + "="
		// + "\"" + unique_id + "\"", null,
		// null);
		// ContactsLog.d(TAG, "update addr count" + cursor.getCount());
		//
		// ContactsLog.d(TAG, "update addr unique_id" + unique_id);
		// if (cursor != null && cursor.getCount() > 0) {
		// // there is a value in db so update
		// cursor.moveToFirst();
		// updatepostal(unique_id, work.street, work.city,
		// work.state, work.country, work.code);
		// } else {
		// // no value in the db so insert a new row
		// addPostal(unique_id, work.street, work.city,
		// work.state, work.country, work.code);
		// }
		// cursor.close();
		// }
		//
		// }
		// if (home.hasData()) {
		// home_adress = 1;
		//
		// String unique_id = "H" + "||" + serverId;
		// if (calling_function == 1) {
		// addPostal(unique_id, home.street, home.city, home.state,
		// home.country, home.code);
		// } else {
		// Cursor cursor = Email
		// .getAppContext()
		// .getContentResolver()
		// .query(ContactsConsts.CONTENT_URI_ADRESS,
		// null,
		// ContactsConsts.ADRESS_UNIQUE_ID + "="
		// + "\"" + unique_id + "\"", null,
		// null);
		//
		// if (cursor != null && cursor.getCount() > 0) {
		// // there is a value in db so update
		// cursor.moveToFirst();
		// updatepostal(unique_id, home.street, home.city,
		// home.state, home.country, home.code);
		// } else {
		// // no value in the db so insert a new row
		// addPostal(unique_id, home.street, home.city,
		// home.state, home.country, home.code);
		// }
		// cursor.close();
		// }
		//
		// }
		// if (other.hasData()) {
		// other_adress = 1;
		//
		// String unique_id = "O" + "||" + serverId;
		// if (calling_function == 1) {
		// addPostal(unique_id, other.street, other.city, other.state,
		// other.country, other.code);
		// } else {
		// Cursor cursor = Email
		// .getAppContext()
		// .getContentResolver()
		// .query(ContactsConsts.CONTENT_URI_ADRESS,
		// null,
		// ContactsConsts.ADRESS_UNIQUE_ID + "="
		// + "\"" + unique_id + "\"", null,
		// null);
		//
		// if (cursor != null && cursor.getCount() > 0) {
		// // there is a value in db so update
		// cursor.moveToFirst();
		// updatepostal(unique_id, other.street, other.city,
		// other.state, other.country, other.code);
		// } else {
		// // no value in the db so insert a new row
		// addPostal(unique_id, other.street, other.city,
		// other.state, other.country, other.code);
		// }
		// cursor.close();
		// }
		//
		// }
		//
		// try {
		// if (calling_function == 1) {
		// addName(serverId, prefix, firstName, lastName, middleName,
		// suffix, email, webpage, im, phone_details,
		// work_adress, home_adress, other_adress,
		// officeLocation, department, companyName, manager,
		// assistant, note_base_body, 0, job_title,
		// anniversary, birthday, spouse, nickname,
		// yomiFirstName, yomiLastName, yomiCompanyName,
		// im_call, picture);
		//
		// } else if (calling_function == 0) {
		// //System.out.println("Updating contact list table");
		// updateName(serverId, prefix, firstName, lastName,
		// middleName, suffix, email, webpage, im,
		// phone_details, work_adress, home_adress,
		// other_adress, officeLocation, department,
		// companyName, manager, assistant, note_base_body,
		// job_title, anniversary, birthday, spouse, nickname,
		// yomiFirstName, yomiLastName, yomiCompanyName,
		// im_call, picture);
		// }
		// } catch (Exception e) {
		// ContactsLog.e(TAG, "Addname error: " + e.toString());
		// }
		//
		// }

		public void addData(String serverId, Entity entity, int calling_function)
				throws IOException {

			ContactsLog.d(EmAbstractSyncParser.class.getCanonicalName(),
					"AddData**** " + serverId);
			
			//Method to handle the Custom email type append with email type from server
			ContentValues newemailVal = emailData(serverId);
			
			//Method to handle the Custom Phone type append with Phone type from server
			ContentValues newphoneVal = phoneData(serverId);
			

			String prefix = null;
			String firstName = null;
			String lastName = "";
			String middleName = null;
			String suffix = null;
			String companyName = null;
			String phone_details = "";
			String im = null;
			String assistant = null;
			String manager = null;
			String webpage = null;
			String note_base_body = null;
			String im_call = null;
			byte[] picture = null;

			int work_adress = 0;
			int home_adress = 0;
			int other_adress = 0;

			String note_contact_body = null;
			String anniversary = null;
			String birthday = null;
			String spouse = null;
			String nickname = null;
			String yomiFirstName = null;
			String yomiLastName = null;
			String yomiCompanyName = null;
			String job_title = null;
			String department = null;
			String officeLocation = null;
			Address home = new Address();
			Address work = new Address();
			Address other = new Address();
			String email = "";
			// EasBusiness business = new EasBusiness();
			// EasPersonal personal = new EasPersonal();
			// ArrayList<String> children = new ArrayList<String>();
			// ArrayList<UntypedRow> emails = new ArrayList<UntypedRow>();
			// ArrayList<UntypedRow> ims = new ArrayList<UntypedRow>();
			// ArrayList<UntypedRow> homePhones = new ArrayList<UntypedRow>();
			// ArrayList<UntypedRow> workPhones = new ArrayList<UntypedRow>();
			if (entity == null) {
				// ops.newContact(serverId);

			}
			try {
				while (nextTag(EmTags.SYNC_APPLICATION_DATA) != END) {
					switch (tag) {
					case EmTags.CONTACTS_FIRST_NAME:
						firstName = getValue();
						break;
					case EmTags.CONTACTS_LAST_NAME:
						lastName = getValue();
						break;
					case EmTags.CONTACTS_MIDDLE_NAME:
						middleName = getValue();
						break;
					case EmTags.CONTACTS_SUFFIX:
						suffix = getValue();
						break;
					case EmTags.CONTACTS_COMPANY_NAME:
						companyName = getValue();
						break;
					case EmTags.CONTACTS_JOB_TITLE:
						job_title = getValue();
						break;
					case EmTags.CONTACTS_EMAIL1_ADDRESS:
						String cONTACTS_EMAIL1_ADDRESS = getValue();
						ContactsLog.d("CONTACTS_EMAIL1_ADDRESS",
								"cONTACTS_EMAIL1_ADDRESS"
										+ cONTACTS_EMAIL1_ADDRESS);
						if (cONTACTS_EMAIL1_ADDRESS != null) {
							newemailVal.put("HOME", cONTACTS_EMAIL1_ADDRESS);
							/*email = email + "Email_Adress" + "="
									+ cONTACTS_EMAIL1_ADDRESS + ":";*/
							// //System.out.println("email email1" + email);
						}
						break;
					case EmTags.CONTACTS_EMAIL2_ADDRESS:
						String cONTACTS_EMAIL2_ADDRESS = getValue();
						if (cONTACTS_EMAIL2_ADDRESS != null) {
							newemailVal.put("WORK", cONTACTS_EMAIL2_ADDRESS);
							
							/*email = email + "Email_Adress2" + "="
									+ cONTACTS_EMAIL2_ADDRESS + ":";*/
							// //System.out.println("email email2" + email);
						}
						break;
					case EmTags.CONTACTS_EMAIL3_ADDRESS:
						String cONTACTS_EMAIL3_ADDRESS = getValue();
						if (cONTACTS_EMAIL3_ADDRESS != null) {
							
							newemailVal.put("OTHER", cONTACTS_EMAIL3_ADDRESS);
							/*email = email + "Email_Adress3" + "="
									+ cONTACTS_EMAIL3_ADDRESS + ":";*/
							// //System.out.println("email email3" + email);
						}
						break;
					case EmTags.CONTACTS_BUSINESS2_TELEPHONE_NUMBER:
						String cONTACTS_BUSINESS2_TELEPHONE_NUMBER = getValue();
						if (cONTACTS_BUSINESS2_TELEPHONE_NUMBER != null) {
							newphoneVal.put("WORK2", cONTACTS_BUSINESS2_TELEPHONE_NUMBER);
							/*phone_details = phone_details + "WORK2" + "="
									+ cONTACTS_BUSINESS2_TELEPHONE_NUMBER + ":";*/
							// //System.out.println("phone_details" +
							// phone_details);
						}
						break;

					case EmTags.CONTACTS_BUSINESS_TELEPHONE_NUMBER:
						String cONTACTS_BUSINESS_TELEPHONE_NUMBER = getValue();
						if (cONTACTS_BUSINESS_TELEPHONE_NUMBER != null) {
							
							newphoneVal.put("WORK", cONTACTS_BUSINESS_TELEPHONE_NUMBER);
							/*phone_details = phone_details + "WORK" + "="
									+ cONTACTS_BUSINESS_TELEPHONE_NUMBER + ":";*/
							// //System.out.println("phone_details" +
							// phone_details);
						}
						// workPhones.add(new PhoneRow(getValue(),
						// Phone.TYPE_WORK));
						break;
					// case EmTags.CONTACTS2_MMS:
					// ops.addPhone(entity, Phone.TYPE_MMS, getValue());
					// break;
					case EmTags.CONTACTS_BUSINESS_FAX_NUMBER:
						String cONTACTS_BUSINESS_FAX_NUMBER = getValue();
						// ops.addPhone(entity, Phone.TYPE_FAX_WORK,
						// getValue());
						if (cONTACTS_BUSINESS_FAX_NUMBER != null) {
							newphoneVal.put("WORK FAX", cONTACTS_BUSINESS_FAX_NUMBER);
							
							/*phone_details = phone_details + "WORK FAX" + "="
									+ cONTACTS_BUSINESS_FAX_NUMBER + ":";*/
							// //System.out.println("phone_details" +
							// phone_details);
						}

						break;
					case EmTags.CONTACTS2_COMPANY_MAIN_PHONE:
					/*	// ops.addPhone(entity, Phone.TYPE_COMPANY_MAIN,
						// getValue());
						String cONTACTS2_COMPANY_MAIN_PHONE = getValue();
						if (cONTACTS2_COMPANY_MAIN_PHONE != null) {
							phone_details = phone_details + "COMPANY_MAIN"
									+ "=" + cONTACTS2_COMPANY_MAIN_PHONE + ":";
							// //System.out.println("phone_details" +
							// phone_details);
						}
						break;*/
						
						
						String cONTACTS2_COMPANY_MAIN_PHONE = getValue();
						if (cONTACTS2_COMPANY_MAIN_PHONE != null) {
							newphoneVal.put("MAIN", cONTACTS2_COMPANY_MAIN_PHONE);
							/*phone_details = phone_details + "MAIN"
									+ "=" + cONTACTS2_COMPANY_MAIN_PHONE + ":";*/
						
						}
						break;
						
						
					case EmTags.CONTACTS_HOME_FAX_NUMBER:
						/*// ops.addPhone(entity, Phone.TYPE_FAX_HOME,
						// getValue());
						String cONTACTS_HOME_FAX_NUMBER = getValue();
						if (cONTACTS_HOME_FAX_NUMBER != null) {
							phone_details = phone_details + "TYPE_FAX_HOME"
									+ "=" + cONTACTS_HOME_FAX_NUMBER + ":";
							// //System.out.println("phone_details" +
							// phone_details);
						}
						break;*/
						
						
						
						
						
						
						String cONTACTS_HOME_FAX_NUMBER = getValue();
						if (cONTACTS_HOME_FAX_NUMBER != null) {
							newphoneVal.put("HOME FAX", cONTACTS_HOME_FAX_NUMBER);
							/*phone_details = phone_details + "HOME FAX"
									+ "=" + cONTACTS_HOME_FAX_NUMBER + ":";*/
						
						}

						break;
					case EmTags.CONTACTS_HOME_TELEPHONE_NUMBER:
						String cONTACTS_HOME_TELEPHONE_NUMBER = getValue();
						if (cONTACTS_HOME_TELEPHONE_NUMBER != null) {
							
							newphoneVal.put("HOME", cONTACTS_HOME_TELEPHONE_NUMBER);
							
							/*phone_details = phone_details + "HOME" + "="
									+ cONTACTS_HOME_TELEPHONE_NUMBER + ":";*/
							// //System.out.println("phone_details" +
							// phone_details);
						}

						break;
					case EmTags.CONTACTS_HOME2_TELEPHONE_NUMBER:
						// homePhones.add(new PhoneRow(getValue(),
						// Phone.TYPE_HOME));
						String cONTACTS_HOME2_TELEPHONE_NUMBER = getValue();
						if (cONTACTS_HOME2_TELEPHONE_NUMBER != null) {
							newphoneVal.put("HOME2", cONTACTS_HOME2_TELEPHONE_NUMBER);
							
							/*phone_details = phone_details + "HOME2" + "="
									+ cONTACTS_HOME2_TELEPHONE_NUMBER + ":";*/
							// //System.out.println("phone_details" +
							// phone_details);
						}

						break;
					case EmTags.CONTACTS_MOBILE_TELEPHONE_NUMBER:
						String cONTACTS_MOBILE_TELEPHONE_NUMBER = getValue();
						if (cONTACTS_MOBILE_TELEPHONE_NUMBER != null) {
							
							newphoneVal.put("MOBILE", cONTACTS_MOBILE_TELEPHONE_NUMBER);
							/*phone_details = phone_details + "MOBILE" + "="
									+ cONTACTS_MOBILE_TELEPHONE_NUMBER + ":";*/
							// //System.out.println("phone_details" +
							// phone_details);
						}

						// ops.addPhone(entity, Phone.TYPE_MOBILE, getValue());
						break;
					case EmTags.CONTACTS_CAR_TELEPHONE_NUMBER:
						String cONTACTS_CAR_TELEPHONE_NUMBER = getValue();
						if (cONTACTS_CAR_TELEPHONE_NUMBER != null) {
							newphoneVal.put("CAR", cONTACTS_CAR_TELEPHONE_NUMBER);
							
							/*phone_details = phone_details + "CAR" + "="
									+ cONTACTS_CAR_TELEPHONE_NUMBER + ":";*/
							// //System.out.println("phone_details" +
							// phone_details);
						}
						// ops.addPhone(entity, Phone.TYPE_CAR, getValue());
						break;
					case EmTags.CONTACTS_RADIO_TELEPHONE_NUMBER:
						String cONTACTS_RADIO_TELEPHONE_NUMBER = getValue();
						// if (cONTACTS_RADIO_TELEPHONE_NUMBER != null) {
						// phone_details = phone_details + "RADIO" + "=" +
						// cONTACTS_RADIO_TELEPHONE_NUMBER + ":";
						// ////System.out.println("phone_details" +
						// phone_details);
						// }
						im_call = cONTACTS_RADIO_TELEPHONE_NUMBER;

						// ops.addPhone(entity, Phone.TYPE_RADIO, getValue());
						break;
					case EmTags.CONTACTS_PAGER_NUMBER:
						String cONTACTS_PAGER_NUMBER = getValue();
						if (cONTACTS_PAGER_NUMBER != null) {
							newphoneVal.put("PAGER", cONTACTS_PAGER_NUMBER);
							/*phone_details = phone_details + "PAGER" + "="
									+ cONTACTS_PAGER_NUMBER + ":";*/
							// //System.out.println("phone_details" +
							// phone_details);
						}
						// ops.addPhone(entity, Phone.TYPE_PAGER, getValue());
						break;
					case EmTags.CONTACTS_ASSISTANT_TELEPHONE_NUMBER:
						String cONTACTS_ASSISTANT_TELEPHONE_NUMBER = getValue();
						if (cONTACTS_ASSISTANT_TELEPHONE_NUMBER != null) {
							newphoneVal.put("ASSISTANT", cONTACTS_ASSISTANT_TELEPHONE_NUMBER);
							
							/*phone_details = phone_details + "ASSISTANT" + "="
									+ cONTACTS_ASSISTANT_TELEPHONE_NUMBER + ":";*/
							// //System.out.println("phone_details" +
							// phone_details);
						}
						break;
					case EmTags.CONTACTS2_IM_ADDRESS:
						im = getValue();
						if (calling_function == 1) {
							im = "=" + im + ":";
						} else {
						}
						ContactsLog.d(
								EmAbstractSyncParser.class.getCanonicalName(),
								"IM :: " + im);

						break;
					case EmTags.CONTACTS_BUSINESS_ADDRESS_CITY:
						work.city = getValue();
						break;
					case EmTags.CONTACTS_BUSINESS_ADDRESS_COUNTRY:
						work.country = getValue();
						break;
					case EmTags.CONTACTS_BUSINESS_ADDRESS_POSTAL_CODE:
						work.code = getValue();
						break;
					case EmTags.CONTACTS_BUSINESS_ADDRESS_STATE:
						work.state = getValue();
						break;
					case EmTags.CONTACTS_BUSINESS_ADDRESS_STREET:
						work.street = getValue();
						break;
					case EmTags.CONTACTS_HOME_ADDRESS_CITY:
						home.city = getValue();
						break;
					case EmTags.CONTACTS_HOME_ADDRESS_COUNTRY:
						home.country = getValue();
						break;
					case EmTags.CONTACTS_HOME_ADDRESS_POSTAL_CODE:
						home.code = getValue();
						break;
					case EmTags.CONTACTS_HOME_ADDRESS_STATE:
						home.state = getValue();
						break;
					case EmTags.CONTACTS_HOME_ADDRESS_STREET:
						home.street = getValue();
						break;
					case EmTags.CONTACTS_OTHER_ADDRESS_CITY:
						other.city = getValue();
						break;
					case EmTags.CONTACTS_OTHER_ADDRESS_COUNTRY:
						other.country = getValue();
						break;
					case EmTags.CONTACTS_OTHER_ADDRESS_POSTAL_CODE:
						other.code = getValue();
						break;
					case EmTags.CONTACTS_OTHER_ADDRESS_STATE:
						other.state = getValue();
						break;
					case EmTags.CONTACTS_OTHER_ADDRESS_STREET:
						other.street = getValue();
						break;

					// case EmTags.CONTACTS_CHILDREN:
					// childrenParser(children);
					// break;

					case EmTags.CONTACTS_YOMI_COMPANY_NAME:
						yomiCompanyName = getValue();
						break;

					case EmTags.CONTACTS_YOMI_FIRST_NAME:
						yomiFirstName = getValue();
						break;

					case EmTags.CONTACTS_YOMI_LAST_NAME:
						yomiLastName = getValue();
						break;

					case EmTags.CONTACTS2_NICKNAME:
						nickname = getValue();
						break;

					case EmTags.CONTACTS_ASSISTANT_NAME:
						// ops.addRelation(entity, Relation.TYPE_ASSISTANT,
						// getValue());
						assistant = getValue();
						break;

					case EmTags.CONTACTS2_MANAGER_NAME:
						manager = getValue();
						// ops.addRelation(entity, Relation.TYPE_MANAGER,
						// getValue());
						break;

					case EmTags.CONTACTS_SPOUSE:
						spouse = getValue();
						break;

					case EmTags.CONTACTS_DEPARTMENT:
						department = getValue();
						break;

					case EmTags.CONTACTS_TITLE:
						prefix = getValue();
						break;

					// // EAS Business
					case EmTags.CONTACTS_OFFICE_LOCATION:
						officeLocation = getValue();
						break;

					// case EmTags.CONTACTS2_CUSTOMER_ID:
					// business.customerId = getValue();
					// break;
					// case EmTags.CONTACTS2_GOVERNMENT_ID:
					// business.governmentId = getValue();
					// break;
					// case EmTags.CONTACTS2_ACCOUNT_NAME:
					// business.accountName = getValue();
					// break;

					// EAS Personal
					case EmTags.CONTACTS_ANNIVERSARY:
						anniversary = getValue();

						break;
					case EmTags.CONTACTS_BIRTHDAY:
						birthday = getValue();
						break;

					case EmTags.CONTACTS_WEBPAGE:
						webpage = getValue();
						// ops.addWebpage(entity, getValue());
						break;

					case EmTags.CONTACTS_PICTURE:
						String pic = getValue();
						/*
						 * System.out.println("===== PICTURE  :: ============" +
						 * pic);
						 */
						// Log.i("Email Sync adapter PHOTO downsync", "pic:" +
						// pic);
						// Cursor cursor = Email
						// .getAppContext()
						// .getContentResolver()
						// .query(ContactsConsts.CONTENT_URI_CONTACTS,
						// null, SERVER_ID_SELECTION,
						// new String[] { serverId }, null);
						try {
							Cursor cursor = Email
									.getAppContext()
									.getContentResolver()
									.query(ContactsConsts.CONTENT_URI_CONTACTS,
											null,
											ContactsConsts.CONTACT_SERVER_ID
													+ "=" + "\"" + serverId
													+ "\"", null, null);
							// // Read Contact Image
							if (cursor != null && cursor.getCount() > 0) {
								cursor.moveToFirst();
								/*
								 * Log.i("Email Sync adapter PHOTO downsync",
								 * "cursor.getCount():" + cursor.getCount());
								 */
								int index = cursor
										.getColumnIndex(ContactsConsts.CONTACT_PHOTO);

								if (index > 0) {

									byte[] img = cursor.getBlob(index);
									cursor.close(); //367712, exception on 5 times wrong PIN
									// Log.i("Email Sync adapter PHOTO downsync",
									// "img.toString():" + img.toString());

									if (img != null && img.length > 0) {

										/*
										 * Log.i("Email Sync adapter PHOTO downsync"
										 * , "img.length:" + img.length);
										 */

									} else {
										try {
											picture = Base64.decode(pic,
													Base64.NO_WRAP);

											/*
											 * Log.i(
											 * "Email Sync adapter PHOTO downsync"
											 * , "picture.toString() 1:" +
											 * picture .toString());
											 */
										} catch (Exception e) {
											/*
											 * ContactsLog.d(
											 * "CONTACTS_PICTURE",
											 * "Unable to parse" +
											 * e.toString());
											 */
										}
									}
								} else {
									try {
										picture = Base64.decode(pic,
												Base64.NO_WRAP);

										/*
										 * Log.i("Email Sync adapter PHOTO downsync"
										 * , "picture.toString() 1:" +
										 * picture.toString());
										 */
									} catch (Exception e) {
										/*
										 * ContactsLog.d( "CONTACTS_PICTURE",
										 * "Unable to parse" + e.toString());
										 */
									}
								}
							} else {
								try {
									picture = Base64
											.decode(pic, Base64.NO_WRAP);
									/*
									 * Log.i("Email Sync adapter PHOTO downsync",
									 * "picture.toString() 2:" +
									 * picture.toString());
									 */
								} catch (Exception e) {
									/*
									 * ContactsLog.d("CONTACTS_PICTURE",
									 * "Unable to parse" + e.toString());
									 */
								}
							}
						} catch (Exception e) {

							// ContactsLog.d("CONTACTS_PICTURE",
							// "Unable to add"+ e.getMessage());
						}
						// ops.addPhoto(entity, getValue());
						break;
					case EmTags.BASE_BODY:
						try {
							note_base_body = bodyParser();
							ContactsLog.d("CONTACTS_BODY", "note_base_body **"
									+ note_base_body);
						} catch (Exception e) {

							ContactsLog.e("BASE_BODY", "note_base_body  err**"
									+ e.toString());
						}
						break;

					case EmTags.CONTACTS_BODY:
						try {
							note_contact_body = getValue();
							ContactsLog.d("CONTACTS_BODY",
									"note_contact_body **" + note_contact_body);
						} catch (Exception e) {

							ContactsLog.e("CONTACTS_BODY",
									"note_contact_body  err**" + e.toString());
						}
						break;

					// case EmTags.CONTACTS_CATEGORIES:
					// mGroupsUsed = true;
					// categoriesParser(ops, entity);
					// break;

					default:
						skipTag();
					}
				}
			} catch (Exception e) {
				ContactsLog.e(EmAbstractSyncAdapter.class.getCanonicalName(),
						"Parsing error: " + e.toString());
			}

			//Method to the Concatenate EmailType as single string
			email = updatedEmailVal(newemailVal);
			
			//Method to the Concatenate EmailType as single string
			phone_details = updatedEmailVal(newphoneVal);
			
			
			// We must have first name, last name, or company name
			String name = null;
			if (firstName != null || lastName != null) {
				if (firstName == null) {
					name = lastName;
				} else if (lastName == null) {
					name = firstName;
				} else {
					name = firstName + ' ' + lastName;
				}
			} else if (companyName != null) {
				name = companyName;
			}
			ContactsLog.d(EmAbstractSyncParser.class.getCanonicalName(),
					"CONTACT name" + name);

			if (work.hasData()) {
				work_adress = 1;
				String unique_id = "W" + "||" + serverId;
				if (calling_function == 1) {
					addPostal(unique_id, work.street, work.city, work.state,
							work.country, work.code);
				} else {
					Cursor cursor = Email
							.getAppContext()
							.getContentResolver()
							.query(ContactsConsts.CONTENT_URI_ADRESS,
									null,
									ContactsConsts.ADRESS_UNIQUE_ID + "="
											+ "\"" + unique_id + "\"", null,
									null);

					if (cursor != null && cursor.getCount() > 0) {
						// there is a value in db so update
						cursor.moveToFirst();
						updatepostal(unique_id, work.street, work.city,
								work.state, work.country, work.code);
					} else {
						// no value in the db so insert a new row
						addPostal(unique_id, work.street, work.city,
								work.state, work.country, work.code);
					}
					cursor.close();
				}

			}
			if (home.hasData()) {
				home_adress = 1;

				String unique_id = "H" + "||" + serverId;
				if (calling_function == 1) {
					addPostal(unique_id, home.street, home.city, home.state,
							home.country, home.code);
				} else {
					Cursor cursor = Email
							.getAppContext()
							.getContentResolver()
							.query(ContactsConsts.CONTENT_URI_ADRESS,
									null,
									ContactsConsts.ADRESS_UNIQUE_ID + "="
											+ "\"" + unique_id + "\"", null,
									null);

					if (cursor != null && cursor.getCount() > 0) {
						// there is a value in db so update
						cursor.moveToFirst();
						updatepostal(unique_id, home.street, home.city,
								home.state, home.country, home.code);
					} else {
						// no value in the db so insert a new row
						addPostal(unique_id, home.street, home.city,
								home.state, home.country, home.code);
					}
					cursor.close();
				}
				// ops.addPostal(entity, StructuredPostal.TYPE_HOME,
				// home.street,
				// home.city, home.state, home.country, home.code);
			}
			if (other.hasData()) {
				other_adress = 1;

				String unique_id = "O" + "||" + serverId;
				if (calling_function == 1) {
					addPostal(unique_id, other.street, other.city, other.state,
							other.country, other.code);
				} else {
					Cursor cursor = Email
							.getAppContext()
							.getContentResolver()
							.query(ContactsConsts.CONTENT_URI_ADRESS,
									null,
									ContactsConsts.ADRESS_UNIQUE_ID + "="
											+ "\"" + unique_id + "\"", null,
									null);

					if (cursor != null && cursor.getCount() > 0) {
						// there is a value in db so update
						cursor.moveToFirst();
						updatepostal(unique_id, other.street, other.city,
								other.state, other.country, other.code);
					} else {
						// no value in the db so insert a new row
						addPostal(unique_id, other.street, other.city,
								other.state, other.country, other.code);
					}
					cursor.close();
				}
				// ops.addPostal(entity, StructuredPostal.TYPE_OTHER,
				// other.street, other.city, other.state, other.country,
				// other.code);
			}

			// if (companyName != null) {
			// ops.addOrganization(entity, Organization.TYPE_WORK,
			// companyName, title, department, yomiCompanyName,
			// officeLocation);
			// }

			try {
				if (calling_function == 1) {
					addName(serverId, prefix, firstName, lastName, middleName,
							suffix, email, webpage, im, phone_details,
							work_adress, home_adress, other_adress,
							officeLocation, department, companyName, manager,
							assistant, note_base_body, 0, job_title,
							anniversary, birthday, spouse, nickname,
							yomiFirstName, yomiLastName, yomiCompanyName,
							im_call, picture);
				} else if (calling_function == 0) {
					// System.out.println("Updating contact list table");
					updateName(serverId, prefix, firstName, lastName,
							middleName, suffix, email, webpage, im,
							phone_details, work_adress, home_adress,
							other_adress, officeLocation, department,
							companyName, manager, assistant, note_base_body,
							job_title, anniversary, birthday, spouse, nickname,
							yomiFirstName, yomiLastName, yomiCompanyName,
							im_call, picture);
				}
			} catch (Exception e) {
				ContactsLog.e(EmAbstractSyncAdapter.class.getCanonicalName(),
						"Addname error: " + e.toString());
			}

		}

		// private void addName(String serverId, String prefix, String
		// firstName,
		// String lastName, String middleName, String suffix,
		// String email, String webpage, String im, String phone_details,
		// int work_adress, int home_adress, int other_adress,
		// String officeLocation, String department, String companyName,
		// String manager, String assistant, String note_base_body,
		// int is_contact_fav, String title, String anniversary,
		// String birthday, String spouse, String nickname,
		// String yomiFirstName, String yomiLastName,
		// String yomiCompanyName, String im_call, byte[] picture) {
		//
		// ContentValues cv = new ContentValues();
		//
		// cv.put(ContactsConsts.CONTACT_SERVER_ID, serverId);
		// cv.put(ContactsConsts.CONTACT_TITLE, prefix);
		// cv.put(ContactsConsts.CONTACT_FIRST_NAME, firstName);
		// cv.put(ContactsConsts.CONTACT_LAST_NAME, lastName);
		// cv.put(ContactsConsts.CONTACT_MIDDLE_NAME, middleName);
		// cv.put(ContactsConsts.CONTACT_SUFFIX, suffix);
		// cv.put(ContactsConsts.CONTACT_EMAIL, email);
		// cv.put(ContactsConsts.CONTACT_WEB_ADRESS, webpage);
		// cv.put(ContactsConsts.CONTACT_IM_NAME, im);
		// cv.put(ContactsConsts.CONTACT_PHONE, phone_details);
		// cv.put(ContactsConsts.CONTACT_BUSS_ADRESS, work_adress);
		// cv.put(ContactsConsts.CONTACT_HOME_ADRESS, home_adress);
		// cv.put(ContactsConsts.CONTACT_OTHER_ADRESS, other_adress);
		// cv.put(ContactsConsts.CONTACT_OFFICE, officeLocation);
		// cv.put(ContactsConsts.CONTACT_DEPARTMENT, department);
		// cv.put(ContactsConsts.CONTACT_COMPANY, companyName);
		// cv.put(ContactsConsts.CONTACT_MANAGER, manager);
		// cv.put(ContactsConsts.CONTACT_ASSISTANT, assistant);
		// cv.put(ContactsConsts.CONTACT_NOTES, note_base_body);
		// cv.put(ContactsConsts.CONTACT_IS_FAVORITE, is_contact_fav);
		// cv.put(ContactsConsts.CONTACT_JOB_TITLE, title);
		// cv.put(ContactsConsts.CONTACT_ANNIVERSARY, anniversary);
		// cv.put(ContactsConsts.CONTACT_BIRTH_DAY, birthday);
		// cv.put(ContactsConsts.CONTACT_SPOUSE, spouse);
		// cv.put(ContactsConsts.CONTACT_NICK_NAME, nickname);
		// cv.put(ContactsConsts.CONTACT_YOMIFIRSTNAME, yomiFirstName);
		// cv.put(ContactsConsts.CONTACT_YOMILASTNAME, yomiLastName);
		// cv.put(ContactsConsts.CONTACT_YOMICOMPANYNAME, yomiCompanyName);
		// cv.put(ContactsConsts.CONTACT_INTERNETCALL, im_call);
		// if (picture != null) {
		// cv.put(ContactsConsts.CONTACT_PHOTO, picture);
		// }
		//
		// com.cognizant.trumobi.em.Email.getAppContext().getContentResolver()
		// .insert(ContactsConsts.CONTENT_URI_CONTACTS, cv);
		//
		// }
		//
		// private void updateName(String serverId, String prefix,
		// String firstName, String lastName, String middleName,
		// String suffix, String email, String webpage, String im,
		// String phone_details, int work_adress, int home_adress,
		// int other_adress, String officeLocation, String department,
		// String companyName, String manager, String assistant,
		// String note_base_body, String title, String anniversary,
		// String birthday, String spouse, String nickname,
		// String yomiFirstName, String yomiLastName,
		// String yomiCompanyName, String im_call, byte[] picture) {
		//
		// ContactsLog.d("UTILS", "update name**" + im);
		// ContentValues cv = new ContentValues();
		//
		// cv.put(ContactsConsts.CONTACT_SERVER_ID, serverId);
		// cv.put(ContactsConsts.CONTACT_TITLE, prefix);
		// cv.put(ContactsConsts.CONTACT_FIRST_NAME, firstName);
		// cv.put(ContactsConsts.CONTACT_LAST_NAME, lastName);
		// cv.put(ContactsConsts.CONTACT_MIDDLE_NAME, middleName);
		// cv.put(ContactsConsts.CONTACT_SUFFIX, suffix);
		// cv.put(ContactsConsts.CONTACT_EMAIL, email);
		// cv.put(ContactsConsts.CONTACT_WEB_ADRESS, webpage);
		//
		// cv.put(ContactsConsts.CONTACT_PHONE, phone_details);
		// cv.put(ContactsConsts.CONTACT_BUSS_ADRESS, work_adress);
		// cv.put(ContactsConsts.CONTACT_HOME_ADRESS, home_adress);
		// cv.put(ContactsConsts.CONTACT_OTHER_ADRESS, other_adress);
		// cv.put(ContactsConsts.CONTACT_OFFICE, officeLocation);
		// cv.put(ContactsConsts.CONTACT_DEPARTMENT, department);
		// cv.put(ContactsConsts.CONTACT_COMPANY, companyName);
		// cv.put(ContactsConsts.CONTACT_MANAGER, manager);
		// cv.put(ContactsConsts.CONTACT_ASSISTANT, assistant);
		// cv.put(ContactsConsts.CONTACT_NOTES, note_base_body);
		// cv.put(ContactsConsts.CONTACT_JOB_TITLE, title);
		// cv.put(ContactsConsts.CONTACT_ANNIVERSARY, anniversary);
		// cv.put(ContactsConsts.CONTACT_BIRTH_DAY, birthday);
		// cv.put(ContactsConsts.CONTACT_SPOUSE, spouse);
		// cv.put(ContactsConsts.CONTACT_NICK_NAME, nickname);
		// cv.put(ContactsConsts.CONTACT_YOMIFIRSTNAME, yomiFirstName);
		// cv.put(ContactsConsts.CONTACT_YOMILASTNAME, yomiLastName);
		// cv.put(ContactsConsts.CONTACT_YOMICOMPANYNAME, yomiCompanyName);
		// cv.put(ContactsConsts.CONTACT_INTERNETCALL, im_call);
		// if (picture != null) {
		// cv.put(ContactsConsts.CONTACT_PHOTO, picture);
		// }
		// // //System.out.println("Update table cv values: " + cv.toString());
		//
		// String im_details = null;
		// // Cursor cursor = Email
		// // .getAppContext()
		// // .getContentResolver()
		// // .query(ContactsConsts.CONTENT_URI_CONTACTS, null,
		// // "_id = ?", new String[] { serverId }, null);
		//
		// Cursor cursor = com.cognizant.trumobi.em.Email
		// .getAppContext()
		// .getContentResolver()
		// .query(ContactsConsts.CONTENT_URI_CONTACTS, null,
		// SERVER_ID_SELECTION, new String[] { serverId },
		// null);
		// ContactsLog.d("UTILS", " serverId**" + serverId);
		//
		// ContactsLog.d("UTILS",
		// " cursor column count**" + cursor.getColumnCount());
		// ContactsLog.d("UTILS", " cursor  count**" + cursor.getCount());
		//
		// if (cursor != null && cursor.moveToFirst()) {
		//
		// ContactsLog.d("UTILS",
		// " cursor column isFirst**" + cursor.isFirst());
		// ContactsLog
		// .d("UTILS",
		// " cursor column isFirst**"
		// + cursor.getString(cursor
		// .getColumnIndex(ContactsConsts.CONTACT_IM_NAME)));
		//
		// while (cursor.isAfterLast() == false) {
		// im_details = cursor.getString(cursor
		// .getColumnIndex(ContactsConsts.CONTACT_IM_NAME));
		//
		// ContactsLog.d("UTILS", "Imdetails" + im_details);
		// ;
		// if (im_details != null) {
		//
		// String[] splitParentStr = im_details.split(":");
		//
		// ContactsLog.d("UTILS", "splitParentStr.length :: "
		// + splitParentStr.length);
		// String updatedIM = "";
		// for (int i = 0; i < splitParentStr.length; i++) {
		// if (splitParentStr[i].contains("=")) {
		// String[] imArray = splitParentStr[i].split("=");
		//
		// String type = imArray[0];
		// if (type == null || type.isEmpty()) {
		// type = "AIM";
		// }
		//
		// String val = imArray[1];
		//
		// if (i == 0) {
		// val = im;
		// }
		// updatedIM += type + "=" + val + ":";
		//
		// }
		// }
		// ContactsLog.d("UTILS", "updatedIM :: " + updatedIM);
		// cv.put(ContactsConsts.CONTACT_IM_NAME, updatedIM);
		// }
		// cursor.moveToNext();
		// }
		//
		// }
		// cursor.close();
		// cursor = null;
		//
		// int count = com.cognizant.trumobi.em.Email
		// .getAppContext()
		// .getContentResolver()
		// .update(ContactsConsts.CONTENT_URI_CONTACTS,
		// cv,
		// ContactsConsts.CONTACT_SERVER_ID + " = " + "\""
		// + serverId + "\"", null);
		//
		// ContactsLog.d("UTILS", "update count" + count);
		//
		// }
		//
		// private void addPostal(String unique_id, String street, String city,
		// String state, String country, String code) {
		// ContentValues postal_values = new ContentValues();
		// postal_values.put(ContactsConsts.ADRESS_UNIQUE_ID, unique_id);
		// postal_values.put(ContactsConsts.STREET, street);
		// postal_values.put(ContactsConsts.CITY, city);
		// postal_values.put(ContactsConsts.STATE, state);
		// postal_values.put(ContactsConsts.COUNTRY, country);
		// postal_values.put(ContactsConsts.ZIP, code);
		//
		// com.cognizant.trumobi.em.Email.getAppContext().getContentResolver()
		// .insert(ContactsConsts.CONTENT_URI_ADRESS, postal_values);
		//
		// }
		//
		// private void updatepostal(String unique_id, String street, String
		// city,
		// String state, String country, String code) {
		// try {
		// ContentValues postal_values = new ContentValues();
		// postal_values.put(ContactsConsts.ADRESS_UNIQUE_ID, unique_id);
		// postal_values.put(ContactsConsts.STREET, street);
		// postal_values.put(ContactsConsts.CITY, city);
		// postal_values.put(ContactsConsts.STATE, state);
		// postal_values.put(ContactsConsts.COUNTRY, country);
		// postal_values.put(ContactsConsts.ZIP, code);
		//
		// com.cognizant.trumobi.em.Email
		// .getAppContext()
		// .getContentResolver()
		// .update(ContactsConsts.CONTENT_URI_ADRESS,
		// postal_values,
		// ContactsConsts.ADRESS_UNIQUE_ID + " = " + "\""
		// + unique_id + "\"", null);
		// } catch (Exception e) {
		//
		// ContactsLog.e(TAG, "Addupdate err" + e.toString());
		// }
		//
		// }

		private void addName(String serverId, String prefix, String firstName,
				String lastName, String middleName, String suffix,
				String email, String webpage, String im, String phone_details,
				int work_adress, int home_adress, int other_adress,
				String officeLocation, String department, String companyName,
				String manager, String assistant, String note_base_body,
				int is_contact_fav, String title, String anniversary,
				String birthday, String spouse, String nickname,
				String yomiFirstName, String yomiLastName,
				String yomiCompanyName, String im_call, byte[] picture) {

			ContentValues cv = new ContentValues();

			cv.put(ContactsConsts.CONTACT_SERVER_ID, serverId);
			cv.put(ContactsConsts.CONTACT_TITLE, prefix);
			cv.put(ContactsConsts.CONTACT_FIRST_NAME, firstName);
			cv.put(ContactsConsts.CONTACT_LAST_NAME, lastName);
			cv.put(ContactsConsts.CONTACT_MIDDLE_NAME, middleName);
			cv.put(ContactsConsts.CONTACT_SUFFIX, suffix);
			cv.put(ContactsConsts.CONTACT_EMAIL, email);
			cv.put(ContactsConsts.CONTACT_WEB_ADRESS, webpage);
			cv.put(ContactsConsts.CONTACT_IM_NAME, im);
			cv.put(ContactsConsts.CONTACT_PHONE, phone_details);
			cv.put(ContactsConsts.CONTACT_BUSS_ADRESS, work_adress);
			cv.put(ContactsConsts.CONTACT_HOME_ADRESS, home_adress);
			cv.put(ContactsConsts.CONTACT_OTHER_ADRESS, other_adress);
			cv.put(ContactsConsts.CONTACT_OFFICE, officeLocation);
			cv.put(ContactsConsts.CONTACT_DEPARTMENT, department);
			cv.put(ContactsConsts.CONTACT_COMPANY, companyName);
			cv.put(ContactsConsts.CONTACT_MANAGER, manager);
			cv.put(ContactsConsts.CONTACT_ASSISTANT, assistant);
			cv.put(ContactsConsts.CONTACT_NOTES, note_base_body);
			cv.put(ContactsConsts.CONTACT_IS_FAVORITE, is_contact_fav);
			cv.put(ContactsConsts.CONTACT_JOB_TITLE, title);
			cv.put(ContactsConsts.CONTACT_ANNIVERSARY, anniversary);
			cv.put(ContactsConsts.CONTACT_BIRTH_DAY, birthday);
			cv.put(ContactsConsts.CONTACT_SPOUSE, spouse);
			cv.put(ContactsConsts.CONTACT_NICK_NAME, nickname);
			cv.put(ContactsConsts.CONTACT_YOMIFIRSTNAME, yomiFirstName);
			cv.put(ContactsConsts.CONTACT_YOMILASTNAME, yomiLastName);
			cv.put(ContactsConsts.CONTACT_YOMICOMPANYNAME, yomiCompanyName);
			cv.put(ContactsConsts.CONTACT_INTERNETCALL, im_call);
			if (picture != null) {
				cv.put(ContactsConsts.CONTACT_PHOTO, picture);
			}

			Email.getAppContext().getContentResolver()
					.insert(ContactsConsts.CONTENT_URI_CONTACTS, cv);

		}

		private void updateName(String serverId, String prefix,
				String firstName, String lastName, String middleName,
				String suffix, String email, String webpage, String im,
				String phone_details, int work_adress, int home_adress,
				int other_adress, String officeLocation, String department,
				String companyName, String manager, String assistant,
				String note_base_body, String title, String anniversary,
				String birthday, String spouse, String nickname,
				String yomiFirstName, String yomiLastName,
				String yomiCompanyName, String im_call, byte[] picture) {
			ContactsLog.d("UTILS", "update name**" + im);
			ContentValues cv = new ContentValues();

			cv.put(ContactsConsts.CONTACT_SERVER_ID, serverId);
			cv.put(ContactsConsts.CONTACT_TITLE, prefix);
			cv.put(ContactsConsts.CONTACT_FIRST_NAME, firstName);
			cv.put(ContactsConsts.CONTACT_LAST_NAME, lastName);
			cv.put(ContactsConsts.CONTACT_MIDDLE_NAME, middleName);
			cv.put(ContactsConsts.CONTACT_SUFFIX, suffix);
			cv.put(ContactsConsts.CONTACT_EMAIL, email);
			cv.put(ContactsConsts.CONTACT_WEB_ADRESS, webpage);

			cv.put(ContactsConsts.CONTACT_PHONE, phone_details);
			cv.put(ContactsConsts.CONTACT_BUSS_ADRESS, work_adress);
			cv.put(ContactsConsts.CONTACT_HOME_ADRESS, home_adress);
			cv.put(ContactsConsts.CONTACT_OTHER_ADRESS, other_adress);
			cv.put(ContactsConsts.CONTACT_OFFICE, officeLocation);
			cv.put(ContactsConsts.CONTACT_DEPARTMENT, department);
			cv.put(ContactsConsts.CONTACT_COMPANY, companyName);
			cv.put(ContactsConsts.CONTACT_MANAGER, manager);
			cv.put(ContactsConsts.CONTACT_ASSISTANT, assistant);
			cv.put(ContactsConsts.CONTACT_NOTES, note_base_body);
			cv.put(ContactsConsts.CONTACT_JOB_TITLE, title);
			cv.put(ContactsConsts.CONTACT_ANNIVERSARY, anniversary);
			cv.put(ContactsConsts.CONTACT_BIRTH_DAY, birthday);
			cv.put(ContactsConsts.CONTACT_SPOUSE, spouse);
			cv.put(ContactsConsts.CONTACT_NICK_NAME, nickname);
			cv.put(ContactsConsts.CONTACT_YOMIFIRSTNAME, yomiFirstName);
			cv.put(ContactsConsts.CONTACT_YOMILASTNAME, yomiLastName);
			cv.put(ContactsConsts.CONTACT_YOMICOMPANYNAME, yomiCompanyName);
			cv.put(ContactsConsts.CONTACT_INTERNETCALL, im_call);

			if (picture != null) {
				cv.put(ContactsConsts.CONTACT_PHOTO, picture);
			}
			// //System.out.println("Update table cv values: " + cv.toString());

			String im_details = null;
			// Cursor cursor = Email
			// .getAppContext()
			// .getContentResolver()
			// .query(ContactsConsts.CONTENT_URI_CONTACTS, null,
			// "_id = ?", new String[] { serverId }, null);
			ContactsLog.d("UTILS", "Received im :: " + im);
			//if (im != null) {
				Cursor cursor = Email
						.getAppContext()
						.getContentResolver()
						.query(ContactsConsts.CONTENT_URI_CONTACTS, null,
								SERVER_ID_SELECTION, new String[] { serverId },
								null);

				if (cursor != null && cursor.moveToFirst()) {

					while (cursor.isAfterLast() == false) {
						im_details = cursor
								.getString(cursor
										.getColumnIndex(ContactsConsts.CONTACT_IM_NAME));

						ContactsLog.d("UTILS", "Imdetails" + im_details);
						;
						if (im_details != null && im_details.length() > 0) {

							String[] splitParentStr = im_details.split(":");

							ContactsLog.d("UTILS", "splitParentStr.length :: "
									+ splitParentStr.length);
							String updatedIM = "";
							for (int i = 0; i < splitParentStr.length; i++) {
								if (splitParentStr[i].contains("=")) {
									String[] imArray = splitParentStr[i]
											.split("=");

									String type = imArray[0];
									if (type == null || type.isEmpty()) {
										type = "AIM";
									}

									String val = imArray[1];

									if (i == 0) {
										val = im;
									}
									if(val != null && val.length() > 0){
										updatedIM += type + "=" + val + ":";
										ContactsLog.d("UTILS 1", "IM val Not null :: " + updatedIM);
									}
									else{
										ContactsLog.d("UTILS 2", "IM val null :: " + updatedIM);
										
									}
								}
							}
							ContactsLog.d("UTILS", "updatedIM :: " + updatedIM);
							cv.put(ContactsConsts.CONTACT_IM_NAME, updatedIM);
						} else {
							if(im != null){
								String updatedIM = "AIM" + "=" + im + ":";
								cv.put(ContactsConsts.CONTACT_IM_NAME, updatedIM);

								ContactsLog.d("UTILS", "update else" + updatedIM);
							}else{
								cv.put(ContactsConsts.CONTACT_IM_NAME, im);
							}
						}
						cursor.moveToNext();
					}
				}
				cursor.close();
				cursor = null;
			//} else {

			//	cv.put(ContactsConsts.CONTACT_IM_NAME, im);
			//}

			int count = Email
					.getAppContext()
					.getContentResolver()
					.update(ContactsConsts.CONTENT_URI_CONTACTS,
							cv,
							ContactsConsts.CONTACT_SERVER_ID + " = " + "\""
									+ serverId + "\"", null);

			ContactsLog.d("UTILS", "update count" + count);

		}

		private void addPostal(String unique_id, String street, String city,
				String state, String country, String code) {
			ContentValues postal_values = new ContentValues();
			postal_values.put(ContactsConsts.ADRESS_UNIQUE_ID, unique_id);
			postal_values.put(ContactsConsts.STREET, street);
			postal_values.put(ContactsConsts.CITY, city);
			postal_values.put(ContactsConsts.STATE, state);
			postal_values.put(ContactsConsts.COUNTRY, country);
			postal_values.put(ContactsConsts.ZIP, code);

			Email.getAppContext().getContentResolver()
					.insert(ContactsConsts.CONTENT_URI_ADRESS, postal_values);

			ContactsLog.d("Address fileds", "Address added ");

		}

		private void updatepostal(String unique_id, String street, String city,
				String state, String country, String code) {
			ContentValues postal_values = new ContentValues();
			postal_values.put(ContactsConsts.ADRESS_UNIQUE_ID, unique_id);
			postal_values.put(ContactsConsts.STREET, street);
			postal_values.put(ContactsConsts.CITY, city);
			postal_values.put(ContactsConsts.STATE, state);
			postal_values.put(ContactsConsts.COUNTRY, country);
			postal_values.put(ContactsConsts.ZIP, code);

			int value = Email
					.getAppContext()
					.getContentResolver()
					.update(ContactsConsts.CONTENT_URI_ADRESS,
							postal_values,
							ContactsConsts.ADRESS_UNIQUE_ID + " = " + "\""
									+ unique_id + "\"", null);

			ContactsLog.d("Address fileds", "Update count " + value);

		}

		private String bodyParser() throws IOException {
			String body = null;
			while (nextTag(EmTags.BASE_BODY) != END) {
				switch (tag) {
				case EmTags.BASE_DATA:
					body = getValue();
					break;
				default:
					skipTag();
				}
			}
			return body;
		}

		public void addParser() throws IOException {
			String serverId = null;
			while (nextTag(EmTags.SYNC_ADD) != END) {
				switch (tag) {
				case EmTags.SYNC_SERVER_ID: // same as
					serverId = getValue();
					break;
				case EmTags.SYNC_APPLICATION_DATA:
					addData(serverId, null, 1);
					break;
				default:
					skipTag();
				}
			}
		}

		private Cursor getServerIdCursor(String serverId) {
			mBindArgument[0] = serverId;
			return Email
					.getAppContext()
					.getContentResolver()
					.query(mAccountUri, null, SERVER_ID_SELECTION,
							mBindArgument, null);
		}

		public void deleteParser() throws IOException {

			ContactsLog.d("Deleting ", "deleteParser***************");
			while (nextTag(EmTags.SYNC_DELETE) != END) {
				switch (tag) {
				case EmTags.SYNC_SERVER_ID:
					String serverId = getValue();
					// Find the message in this mailbox with the given serverId
					Cursor c = getServerIdCursor(serverId);

					ContactsLog.d("Deleting ", "Cursor" + c.getCount());
					try {
						if (c.moveToFirst()) {
							ContactsLog.d("Deleting ", " serverId ***"
									+ serverId);
							com.cognizant.trumobi.em.Email
									.getAppContext()
									.getContentResolver()
									.delete(mAccountUri,
											ContactsConsts.CONTACT_SERVER_ID
													+ "=" + "\"" + serverId
													+ "\"", null);
						}
					} catch (Exception e) {
						ContactsLog.d("Deleting ", "Unable to del");
					} finally {
						c.close();
					}
					break;
				default:
					skipTag();
				}
			}
		}

		class ServerChange {
			long id;
			boolean read;

			ServerChange(long _id, boolean _read) {
				id = _id;
				read = _read;
			}
		}

		/**
		 * Changes are handled row by row, and only changed/new rows are acted
		 * upon
		 * 
		 * @param ops
		 *            the array of pending ContactProviderOperations.
		 * @throws IOException
		 */
		public void changeParser() throws IOException {
			String serverId = null;
			Entity entity = null;
			while (nextTag(EmTags.SYNC_CHANGE) != END) {
				switch (tag) {
				case EmTags.SYNC_SERVER_ID:
					serverId = getValue();
					break;
				case EmTags.SYNC_APPLICATION_DATA:
					addData(serverId, entity, 0);
					break;
				default:
					skipTag();
				}
			}
		}

		@Override
		public void commandsParser() throws IOException {
			while (nextTag(EmTags.SYNC_COMMANDS) != END) {
				if (tag == EmTags.SYNC_ADD) {
					addParser();
					incrementChangeCount();
				} else if (tag == EmTags.SYNC_DELETE) {

					ContactsLog.d("Deleting ",
							"EmTags.SYNC_DELETE***************");
					deleteParser();
					incrementChangeCount();
				} else if (tag == EmTags.SYNC_CHANGE) {
					changeParser();
					incrementChangeCount();
				} else
					skipTag();
			}
		}

		@Override
		public void commit() throws IOException {

			ContactsLog.d(EmContactsSyncAdapter.class.getName(), "Synckey*** "
					+ mMailbox.mSyncKey);

			ContentValues sync_values = new ContentValues();
			sync_values
					.put(ContactsConsts.ACCOUNT_NAME, mAccount.mEmailAddress);
			sync_values.put(ContactsConsts.ACCOUNT_ID,
					EmEas.EXCHANGE_ACCOUNT_MANAGER_TYPE);
			sync_values.put(ContactsConsts.SYNC_KEY, mMailbox.mSyncKey);

			com.cognizant.trumobi.em.Email
					.getAppContext()
					.getContentResolver()
					.update(ContactsConsts.CONTENT_URI_CONTACTS_SYNC_STATE,
							sync_values, null, null);

			clearcacheDb(getSyncKey());

		}

		void clearcacheDb(String newSynckey) {

			try {
				Cursor get_added = com.cognizant.trumobi.em.Email
						.getAppContext()
						.getContentResolver()
						.query(ContactsConsts.CONTENT_URI_CONTACTS_LOCAL_CACHE_DELETE,
								null, null, null, null);

				String syncState = null;

				if (get_added != null) {

					for (int i = 0; i < get_added.getCount(); i++) {

						if (i == 0) {
							get_added.moveToFirst();

						}
						syncState = get_added
								.getString(get_added
										.getColumnIndex(ContactsConsts.CONTACTS_SYNC_STATE));

						ContactsLog.d(TAG, "syncState " + syncState);
						if (!newSynckey.equals(syncState)) {
							com.cognizant.trumobi.em.Email
									.getAppContext()
									.getContentResolver()
									.delete(ContactsConsts.CONTENT_URI_CONTACTS_LOCAL_CACHE_DELETE,
											ContactsConsts.CONTACTS_SYNC_STATE
													+ "=" + "\"" + syncState
													+ "\"", null);

						}

					}

				}

				get_added.close();

				get_added = com.cognizant.trumobi.em.Email
						.getAppContext()
						.getContentResolver()
						.query(ContactsConsts.CONTENT_URI_CONTACTS_LOCAL_CACHE_UPDATE,
								null, null, null, null);

				if (get_added != null) {

					for (int i = 0; i < get_added.getCount(); i++) {

						if (i == 0) {
							get_added.moveToFirst();

						}
						syncState = get_added
								.getString(get_added
										.getColumnIndex(ContactsConsts.CONTACTS_SYNC_STATE));

						ContactsLog.d(TAG, "update syncState " + syncState);
						if (!newSynckey.equals(syncState)) {
							com.cognizant.trumobi.em.Email
									.getAppContext()
									.getContentResolver()
									.delete(ContactsConsts.CONTENT_URI_CONTACTS_LOCAL_CACHE_UPDATE,
											ContactsConsts.CONTACTS_SYNC_STATE
													+ "=" + "\"" + syncState
													+ "\"", null);

						}

					}

				}

				get_added.close();

			} catch (Exception e) {

				ContactsLog.d(TAG, "Exception 2312");
			}

		}

		// public void addResponsesParser() throws IOException {
		// String serverId = null;
		// String clientId = null;
		// String status = null;
		//
		// while (nextTag(EmTags.SYNC_ADD) != END) {
		// switch (tag) {
		// case EmTags.SYNC_SERVER_ID:
		// serverId = getValue();
		// break;
		// case EmTags.SYNC_CLIENT_ID:
		// clientId = getValue();
		// break;
		// case EmTags.SYNC_STATUS:
		// status = getValue();
		// break;
		// default:
		// skipTag();
		// }
		// }
		//
		// // This is theoretically impossible, but...
		// if (clientId == null || serverId == null)
		// return;
		// if (status.equals("1")) {
		//
		// ContentValues sync_values = new ContentValues();
		// sync_values.put(ContactsConsts.CONTACT_SERVER_ID, serverId);
		// //
		// //System.out.println("***********************updateing servier id for add********");
		// com.cognizant.trumobi.em.Email
		// .getAppContext()
		// .getContentResolver()
		// .update(ContactsConsts.CONTENT_URI_CONTACTS,
		// sync_values,
		// ContactsConsts.CONTACT_CLIENT_ID + "=" + "\""
		// + clientId + "\"", null);
		// //
		// //System.out.println("***********************updated servier id for add********");
		// com.cognizant.trumobi.em.Email
		// .getAppContext()
		// .getContentResolver()
		// .delete(ContactsConsts.CONTENT_URI_CONTACTS_LOCAL_CACHE_ADD,
		//
		// ContactsConsts.CONTACT_CLIENT_ID + "=" + "\""
		// + clientId + "\"", null);
		// //
		// //System.out.println("***********************deleted from chache client id id for add********");
		// // Updating adress table
		// try {
		// ContentValues sync_values_adress = new ContentValues();
		// String unique_id = "W" + "||" + serverId;
		// sync_values_adress.put(ContactsConsts.ADRESS_UNIQUE_ID,
		// unique_id);
		// String clinet_unique = "W" + "||" + clientId;
		//
		// Cursor c = com.cognizant.trumobi.em.Email
		// .getAppContext()
		// .getContentResolver()
		// .query(ContactsConsts.CONTENT_URI_ADRESS,
		// null,
		// ContactsConsts.ADRESS_CLIENT_ID + "="
		// + "\"" + clinet_unique + "\"",
		// null, null);
		//
		// if (c != null && c.getCount() > 0) {
		// com.cognizant.trumobi.em.Email
		// .getAppContext()
		// .getContentResolver()
		// .update(ContactsConsts.CONTENT_URI_ADRESS,
		// sync_values_adress,
		// ContactsConsts.ADRESS_CLIENT_ID + "="
		// + "\"" + clinet_unique + "\"",
		// null);
		// }
		// c.close();
		//
		// } catch (Exception e) {
		// ContactsLog.e("EMail ContactSync Adapter",
		// "updateting the adress server id: " + e.toString());
		// }
		//
		// }
		// }

		public void addResponsesParser() throws IOException {
			String serverId = null;
			String clientId = null;
			String status = null;
			ContentValues cv = new ContentValues();
			while (nextTag(EmTags.SYNC_ADD) != END) {
				switch (tag) {
				case EmTags.SYNC_SERVER_ID:
					serverId = getValue();

					ContactsLog.d(EmContactsSyncAdapter.class.getName(),
							"responsesParser serverId " + serverId);
					break;
				case EmTags.SYNC_CLIENT_ID:
					clientId = getValue();

					ContactsLog.d(EmContactsSyncAdapter.class.getName(),
							"responsesParser clientId " + clientId);
					break;
				case EmTags.SYNC_STATUS:
					status = getValue();

					ContactsLog.d(EmContactsSyncAdapter.class.getName(),
							"responsesParser SYNC_STATUS " + status);
					break;
				default:
					skipTag();
				}
			}

			// This is theoretically impossible, but...
			if (clientId == null || serverId == null)
				return;
			if (status.equals("1")) {
				ContentValues sync_values = new ContentValues();
				sync_values.put(ContactsConsts.CONTACT_SERVER_ID, serverId);
				// //System.out.println("***********************updateing servier id for add********");
				Email.getAppContext()
						.getContentResolver()
						.update(ContactsConsts.CONTENT_URI_CONTACTS,
								sync_values,
								ContactsConsts.CONTACT_CLIENT_ID + "=" + "\""
										+ clientId + "\"", null);
				// //System.out.println("***********************updated servier id for add********");
				Email.getAppContext()
						.getContentResolver()
						.delete(ContactsConsts.CONTENT_URI_CONTACTS_LOCAL_CACHE_ADD,

								ContactsConsts.CONTACT_CLIENT_ID + "=" + "\""
										+ clientId + "\"", null);
				// //System.out.println("***********************deleted from chache client id id for add********");
				// Updating adress table
				try {
					ContentValues sync_values_adress = new ContentValues();
					String unique_id = "W" + "||" + serverId;
					sync_values_adress.put(ContactsConsts.ADRESS_UNIQUE_ID,
							unique_id);
					String clinet_unique = "W" + "||" + clientId;

					Cursor c = Email
							.getAppContext()
							.getContentResolver()
							.query(ContactsConsts.CONTENT_URI_ADRESS,
									null,
									ContactsConsts.ADRESS_CLIENT_ID + "="
											+ "\"" + clinet_unique + "\"",
									null, null);

					if (c != null && c.getCount() > 0) {
						Email.getAppContext()
								.getContentResolver()
								.update(ContactsConsts.CONTENT_URI_ADRESS,
										sync_values_adress,
										ContactsConsts.ADRESS_CLIENT_ID + "="
												+ "\"" + clinet_unique + "\"",
										null);
					}
					c.close();

					// Updating Home adress table
					ContentValues sync_values_home_adress = new ContentValues();
					String unique_id_home = "H" + "||" + serverId;
					sync_values_home_adress.put(
							ContactsConsts.ADRESS_UNIQUE_ID, unique_id_home);
					String clinet_unique_home = "H" + "||" + clientId;

					Cursor cur_home = Email
							.getAppContext()
							.getContentResolver()
							.query(ContactsConsts.CONTENT_URI_ADRESS,
									null,
									ContactsConsts.ADRESS_CLIENT_ID + "="
											+ "\"" + clinet_unique_home + "\"",
									null, null);

					if (cur_home != null && cur_home.getCount() > 0) {
						Email.getAppContext()
								.getContentResolver()
								.update(ContactsConsts.CONTENT_URI_ADRESS,
										sync_values_home_adress,
										ContactsConsts.ADRESS_CLIENT_ID + "="
												+ "\"" + clinet_unique_home
												+ "\"", null);
					}
					cur_home.close();

					// Updating Other adress table
					ContentValues sync_values_other_adress = new ContentValues();
					String unique_id_other = "O" + "||" + serverId;
					sync_values_other_adress.put(
							ContactsConsts.ADRESS_UNIQUE_ID, unique_id_other);
					String clinet_unique_other = "O" + "||" + clientId;

					Cursor cur_other = Email
							.getAppContext()
							.getContentResolver()
							.query(ContactsConsts.CONTENT_URI_ADRESS,
									null,
									ContactsConsts.ADRESS_CLIENT_ID + "="
											+ "\"" + clinet_unique_other + "\"",
									null, null);

					if (cur_other != null && cur_other.getCount() > 0) {
						Email.getAppContext()
								.getContentResolver()
								.update(ContactsConsts.CONTENT_URI_ADRESS,
										sync_values_other_adress,
										ContactsConsts.ADRESS_CLIENT_ID + "="
												+ "\"" + clinet_unique_other
												+ "\"", null);
					}

					cur_other.close();

				} catch (Exception e) {
					ContactsLog.e("EMail ContactSync Adapter",
							"updateting the adress server id: " + e.toString());
				}
			}
		}

		public void changeResponsesParser() throws IOException {
			ContactsLog.d("changeResponsesParser", " calling");

			String serverId = null;
			String status = null;
			while (nextTag(EmTags.SYNC_CHANGE) != END) {
				switch (tag) {
				case EmTags.SYNC_SERVER_ID:
					serverId = getValue();
					ContactsLog.d("changeResponsesParser", " server Id "
							+ serverId);
					break;
				case EmTags.SYNC_STATUS:
					status = getValue();
					break;
				default:
					skipTag();
				}
			}
			if (serverId != null && status != null) {
				userLog("Changed contact " + serverId + " failed with status: "
						+ status);
			}

			if (status.equals("1")) {
				com.cognizant.trumobi.em.Email
						.getAppContext()
						.getContentResolver()
						.delete(ContactsConsts.CONTENT_URI_CONTACTS_LOCAL_CACHE_UPDATE,

								ContactsConsts.CONTACT_SERVER_ID + "=" + "\""
										+ serverId + "\"", null);
			}

		}

		@Override
		public void responsesParser() throws IOException {
			// Handle server responses here (for Add and Change)
			while (nextTag(EmTags.SYNC_RESPONSES) != END) {
				if (tag == EmTags.SYNC_ADD) {
					addResponsesParser();
				} else if (tag == EmTags.SYNC_CHANGE) {
					changeResponsesParser();
				} else
					skipTag();
			}
		}
	}

	private void get_local_contact_updates(EmSerializer s) {

		try {
			Cursor get_added = com.cognizant.trumobi.em.Email
					.getAppContext()
					.getContentResolver()
					.query(ContactsConsts.CONTENT_URI_CONTACTS_LOCAL_CACHE_UPDATE,
							null, null, null, null);

			String Server_id = null;

			if (get_added != null) {

				for (int i = 0; i < get_added.getCount(); i++) {

					if (i == 0) {
						get_added.moveToFirst();

					}
					Server_id = get_added.getString(get_added
							.getColumnIndex(ContactsConsts.CONTACT_SERVER_ID));
					if (Server_id != null) {
						ContactsLog
								.d(TAG, "Contacts update *** : " + Server_id);
						s.start(EmTags.SYNC_CHANGE).data(EmTags.SYNC_SERVER_ID,
								Server_id);
						s.start(EmTags.SYNC_APPLICATION_DATA);

						Cursor cursor = getCursor(EmTags.SYNC_CHANGE, Server_id);
						ContactsLog.d(
								TAG,
								"Contacts update *** : count "
										+ cursor.getCount() + "coloumn count"
										+ cursor.getColumnCount()
										+ "Client Id **" + Server_id);
						pouplateContactDetailsByCursor(cursor, s);
						if (cursor != null) {
							cursor.close();
						}
						s.end();
						s.end();

						String key = getSyncKey();
						ContentValues deleteValues = new ContentValues();
						deleteValues.put(ContactsConsts.CONTACTS_SYNC_STATE,
								key);

						ContactsLog.d(TAG, "sync key : " + key);
						int count = com.cognizant.trumobi.em.Email
								.getAppContext()
								.getContentResolver()
								.update(ContactsConsts.CONTENT_URI_CONTACTS_LOCAL_CACHE_UPDATE,
										deleteValues,
										ContactsConsts.CONTACT_SERVER_ID + "="
												+ "\"" + Server_id + "\"", null);

						ContactsLog.d(TAG, " updated count : " + count);

						// Email
						// .getAppContext()
						// .getContentResolver()
						// .delete(ContactsConsts.CONTENT_URI_CONTACTS_LOCAL_CACHE_UPDATE,
						//
						// ContactsConsts.CONTACT_SERVER_ID + "="
						// + "\"" + Server_id + "\"", null);
					}
					get_added.moveToNext();
				}

			}

			get_added.close();
		} catch (Exception e) {

			ContactsLog.d(TAG, "Exception in update");
		}

	}

	private void get_local_contact_added(EmSerializer s) {
		try {
			Cursor get_added = com.cognizant.trumobi.em.Email
					.getAppContext()
					.getContentResolver()
					.query(ContactsConsts.CONTENT_URI_CONTACTS_LOCAL_CACHE_ADD,
							null, null, null, null);

			String client_id = null;

			if (get_added != null) {

				for (int i = 0; i < get_added.getCount(); i++) {

					if (i == 0) {
						get_added.moveToFirst();

					}
					client_id = get_added.getString(get_added
							.getColumnIndex(ContactsConsts.CONTACT_CLIENT_ID));
					if (client_id != null) {
						ContactsLog.d(TAG,
								"Creating new contact with clientId: "
										+ client_id);
						s.start(EmTags.SYNC_ADD).data(EmTags.SYNC_CLIENT_ID,
								client_id);
						s.start(EmTags.SYNC_APPLICATION_DATA);

						Cursor cursor = getCursor(EmTags.SYNC_ADD, client_id);
						ContactsLog.d(TAG,
								"Creating new contact with clientId: count "
										+ cursor.getCount() + "coloumn count"
										+ cursor.getColumnCount()
										+ "Client Id **" + client_id);

						pouplateContactDetailsByCursor(cursor, s);
						if (cursor != null) {
							cursor.close();
						}
						s.end();
						s.end();
					}
					get_added.moveToNext();
				}

			}

			get_added.close();
		} catch (Exception e) {

			ContactsLog.d(TAG, "Exception in  contact add");
		}

	}

	private void get_local_deleted(EmSerializer s) {

		try {
			Cursor get_added = com.cognizant.trumobi.em.Email
					.getAppContext()
					.getContentResolver()
					.query(ContactsConsts.CONTENT_URI_CONTACTS_LOCAL_CACHE_DELETE,
							null, null, null, null);

			String server_id_deleted = null;

			if (get_added != null) {

				for (int i = 0; i < get_added.getCount(); i++) {

					if (i == 0) {
						get_added.moveToFirst();

					}
					server_id_deleted = get_added.getString(get_added
							.getColumnIndex(ContactsConsts.CONTACT_SERVER_ID));
					if (server_id_deleted != null) {
						ContactsLog.d(TAG, "delete serverid : "
								+ server_id_deleted);
						s.start(EmTags.SYNC_DELETE).data(EmTags.SYNC_SERVER_ID,
								server_id_deleted);

						s.end();

						String key = getSyncKey();
						ContentValues deleteValues = new ContentValues();
						deleteValues.put(ContactsConsts.CONTACTS_SYNC_STATE,
								key);

						ContactsLog.d(TAG, "sync key : " + key);
						int count = com.cognizant.trumobi.em.Email
								.getAppContext()
								.getContentResolver()
								.update(ContactsConsts.CONTENT_URI_CONTACTS_LOCAL_CACHE_DELETE,
										deleteValues,
										ContactsConsts.CONTACT_SERVER_ID + "="
												+ "\"" + server_id_deleted
												+ "\"", null);

						ContactsLog.d(TAG, " updated count : " + count);
					}

					/*
					 * com.cognizant.trumobi.em.Email .getAppContext()
					 * .getContentResolver() .delete(ContactsConsts.
					 * CONTENT_URI_CONTACTS_LOCAL_CACHE_DELETE,
					 * ContactsConsts.CONTACT_SERVER_ID + "=" + "\"" +
					 * server_id_deleted + "\"", null); //
					 * get_added.moveToNext();
					 */}

			}

			get_added.close();

		} catch (Exception e) {

			ContactsLog.d(TAG, "Exception 2312");
		}

	}

	public void pouplateContactDetailsByCursor(Cursor cursor, EmSerializer s) {

		try {

			while (cursor != null && cursor.moveToNext()) {

				String fileAs = "";
				String firstName = cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_FIRST_NAME));
				String lastName = cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_LAST_NAME));
				String middlename = cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_MIDDLE_NAME));

				if (firstName != null && !firstName.isEmpty()) {
					fileAs = firstName;
					s.data(EmTags.CONTACTS_FIRST_NAME, firstName);
					if (lastName != null && !lastName.isEmpty()) {
						fileAs += " " + lastName;
						s.data(EmTags.CONTACTS_LAST_NAME, lastName);

					}

				} else {
					if (lastName != null && !lastName.isEmpty()) {
						fileAs = lastName;
						s.data(EmTags.CONTACTS_LAST_NAME, lastName);
					} else {
						if (middlename != null && !middlename.isEmpty()) {
							fileAs = middlename;
						}
					}
				}

				if (middlename != null && !middlename.isEmpty()) {

					s.data(EmTags.CONTACTS_MIDDLE_NAME, middlename);

				}

				if (fileAs != null && !fileAs.isEmpty()) {

					s.data(EmTags.CONTACTS_FILE_AS, fileAs);

				}

				String jobtitle = cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_JOB_TITLE));
				if (jobtitle != null && !jobtitle.isEmpty()) {

					s.data(EmTags.CONTACTS_JOB_TITLE, jobtitle);

				}

				String org = cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_COMPANY));

				if (org != null && !org.isEmpty()) {

					s.data(EmTags.CONTACTS_COMPANY_NAME, org);

				}

				// Email DATA Population

				try {
					 ContentValues emailAddress = new ContentValues();
					String email_details = cursor.getString(cursor
							.getColumnIndex(ContactsConsts.CONTACT_EMAIL));
					ContactsLog.d("Email details: ", "***" + email_details);
						if (email_details != null && !email_details.isEmpty()) {
							String[] email_array = null;
							email_array = email_details.split(":");
							String[] email_type_number = null;

							for (int i = 0; i < email_array.length; i++) {
								// //System.out.println("email_array.length: " +
								// phone_array[i]);
								if (email_array[i] != null
										&& !email_array[i].isEmpty()) {
									email_type_number = email_array[i].split("=");
									emailAddress.put(email_type_number[0],
											email_type_number[1]);
								}
							}
						
						if (emailAddress.size() > 0) {
							if (emailAddress.containsKey("HOME")) {
								String HOME = emailAddress.get("HOME")
										.toString();
								if (HOME != null && !HOME.isEmpty()) {
									s.data(EmTags.CONTACTS_EMAIL1_ADDRESS,
											HOME);
								}
							}

							if (emailAddress.containsKey("WORK")) {
								String WORK = emailAddress.get("WORK")
										.toString();
								if (WORK != null && !WORK.isEmpty()) {
									s.data(EmTags.CONTACTS_EMAIL2_ADDRESS,
											WORK);
								}
							}

							if (emailAddress.containsKey("OTHER")) {
								String OTHER = emailAddress.get("OTHER")
										.toString();
								if (OTHER != null && !OTHER.isEmpty()) {
									s.data(EmTags.CONTACTS_EMAIL3_ADDRESS,
											OTHER);
								}
							}
						}
						
					}

				} catch (Exception e) {
					ContactsLog.e("EMAIL CONTACT SYNC ADAPTER",
							" email: " + e.toString());
				}

				// Phone data Population

				try {
					ContentValues phoneNumbers = new ContentValues();
					String phone_details = cursor.getString(cursor
							.getColumnIndex(ContactsConsts.CONTACT_PHONE));
					// //System.out.println("Email details: " + phone_details);
					if (phone_details != null && !phone_details.isEmpty()) {
						String[] phone_array = null;
						phone_array = phone_details.split(":");
						String[] phone_type_number = null;
						// //System.out.println("phone_array.length: " +
						// phone_array.length);

						for (int i = 0; i < phone_array.length; i++) {
							// //System.out.println("email_array.length: " +
							// phone_array[i]);
							if (phone_array[i] != null
									&& !phone_array[i].isEmpty()) {
								phone_type_number = phone_array[i].split("=");
								phoneNumbers.put(phone_type_number[0],
										phone_type_number[1]);

							}
						}

						if (phoneNumbers.size() > 0) {
							if (phoneNumbers.containsKey("MOBILE")) {
								String MOBILE = phoneNumbers.get("MOBILE")
										.toString();
								if (MOBILE != null && !MOBILE.isEmpty()) {
									s.data(EmTags.CONTACTS_MOBILE_TELEPHONE_NUMBER,
											MOBILE);
								}
							}

							if (phoneNumbers.containsKey("WORK2")) {
								String WORK2 = phoneNumbers.get("WORK2")
										.toString();
								if (WORK2 != null && !WORK2.isEmpty()) {
									s.data(EmTags.CONTACTS_BUSINESS2_TELEPHONE_NUMBER,
											WORK2);
								}
							}

							if (phoneNumbers.containsKey("WORK")) {
								String WORK = phoneNumbers.get("WORK")
										.toString();
								if (WORK != null && !WORK.isEmpty()) {
									s.data(EmTags.CONTACTS_BUSINESS_TELEPHONE_NUMBER,
											WORK);
								}
							}

							if (phoneNumbers.containsKey("WORK FAX")) {
								String FAX_WORK = phoneNumbers.get("WORK FAX")
										.toString();
								if (FAX_WORK != null && !FAX_WORK.isEmpty()) {
									s.data(EmTags.CONTACTS_BUSINESS_FAX_NUMBER,
											FAX_WORK);
								}
							}

							if (phoneNumbers.containsKey("MAIN")) {
								String COMPANY_MAIN = phoneNumbers.get("MAIN")
										.toString();
								if (COMPANY_MAIN != null
										&& !COMPANY_MAIN.isEmpty()) {
									s.data(EmTags.CONTACTS2_COMPANY_MAIN_PHONE,
											COMPANY_MAIN);
								}
							}

							if (phoneNumbers.containsKey("HOME FAX")) {
								String TYPE_FAX_HOME = phoneNumbers.get(
										"HOME FAX").toString();
								if (TYPE_FAX_HOME != null
										&& !TYPE_FAX_HOME.isEmpty()) {
									s.data(EmTags.CONTACTS_HOME_FAX_NUMBER,
											TYPE_FAX_HOME);
								}
							}

							if (phoneNumbers.containsKey("HOME")) {
								String HOME = phoneNumbers.get("HOME")
										.toString();
								if (HOME != null && !HOME.isEmpty()) {
									s.data(EmTags.CONTACTS_HOME_TELEPHONE_NUMBER,
											HOME);
								}
							}

							if (phoneNumbers.containsKey("HOME2")) {
								String HOME2 = phoneNumbers.get("HOME2")
										.toString();
								if (HOME2 != null && !HOME2.isEmpty()) {
									s.data(EmTags.CONTACTS_HOME2_TELEPHONE_NUMBER,
											HOME2);
								}
							}

							if (phoneNumbers.containsKey("ASSISTANT")) {
								String ASSISTANT = phoneNumbers
										.get("ASSISTANT").toString();
								if (ASSISTANT != null && !ASSISTANT.isEmpty()) {
									s.data(EmTags.CONTACTS_ASSISTANT_TELEPHONE_NUMBER,
											ASSISTANT);
								}
							}

							if (phoneNumbers.containsKey("PAGER")) {
								String PAGER = phoneNumbers.get("PAGER")
										.toString();
								if (PAGER != null && !PAGER.isEmpty()) {
									s.data(EmTags.CONTACTS_PAGER_NUMBER,
											PAGER);
								}
							}

							if (phoneNumbers.containsKey("CAR")) {
								String CAR = phoneNumbers.get("CAR").toString();
								if (CAR != null && !CAR.isEmpty()) {
									s.data(EmTags.CONTACTS_CAR_TELEPHONE_NUMBER,
											CAR);
								}
							}
						}
					}

				} catch (Exception e) {
					ContactsLog.e("EMAIL CONTACT SYNC ADAPTER",
							" phone: " + e.toString());
				}

				// Adress data population
				try {
					String addressBusiness = cursor
							.getString(cursor
									.getColumnIndex(ContactsConsts.CONTACT_BUSS_ADRESS));
					String adressHome = cursor
							.getString(cursor
									.getColumnIndex(ContactsConsts.CONTACT_HOME_ADRESS));
					String adressOther = cursor
							.getString(cursor
									.getColumnIndex(ContactsConsts.CONTACT_OTHER_ADRESS));
					String serverid = cursor.getString(cursor
							.getColumnIndex(ContactsConsts.CONTACT_SERVER_ID));
					String clientid = cursor.getString(cursor
							.getColumnIndex(ContactsConsts.CONTACT_CLIENT_ID));

					// Pouplating data for Business Adress

					if (addressBusiness != null) {
						if (serverid != null && !serverid.isEmpty()) {
							String unique_id = "W" + "||" + serverid;

							// //System.out.println("*****************################");
							if (addressBusiness.equals("1")) {
								// //System.out.println("*****************");
								Cursor adress = Email
										.getAppContext()
										.getContentResolver()
										.query(ContactsConsts.CONTENT_URI_ADRESS,
												null,
												ContactsConsts.ADRESS_UNIQUE_ID
														+ "=" + "\""
														+ unique_id + "\"",
												null, null);
								if (adress != null) {
									adress.moveToFirst();

									String city = adress
											.getString(adress
													.getColumnIndex(ContactsConsts.CITY));
									String street = adress
											.getString(adress
													.getColumnIndex(ContactsConsts.STREET));
									String zip = adress
											.getString(adress
													.getColumnIndex(ContactsConsts.ZIP));
									String country = adress
											.getString(adress
													.getColumnIndex(ContactsConsts.COUNTRY));
									String state = adress
											.getString(adress
													.getColumnIndex(ContactsConsts.STATE));

									// //System.out.println("*****************"
									// + city + " " + street + " " + state + " "
									// + country + " " + zip);
									if (city != null && !city.isEmpty()) {
										s.data(EmTags.CONTACTS_BUSINESS_ADDRESS_CITY,
												city);
									}
									if (street != null && !street.isEmpty()) {
										s.data(EmTags.CONTACTS_BUSINESS_ADDRESS_STREET,
												street);
									}
									if (zip != null && !zip.isEmpty()) {
										s.data(EmTags.CONTACTS_BUSINESS_ADDRESS_POSTAL_CODE,
												zip);
									}
									if (country != null && !country.isEmpty()) {
										s.data(EmTags.CONTACTS_BUSINESS_ADDRESS_COUNTRY,
												country);
									}
									if (state != null && !state.isEmpty()) {
										s.data(EmTags.CONTACTS_BUSINESS_ADDRESS_STATE,
												state);
									}
								}
								adress.close();
							}
						} else if (clientid != null && !clientid.isEmpty()) {
							String unique_id = "W" + "||" + clientid;
							// //System.out.println("*****************################12345");
							if (addressBusiness.equals("1")) {
								// //System.out.println("*****************");
								Cursor adress = Email
										.getAppContext()
										.getContentResolver()
										.query(ContactsConsts.CONTENT_URI_ADRESS,
												null,
												ContactsConsts.ADRESS_CLIENT_ID
														+ "=" + "\""
														+ unique_id + "\"",
												null, null);
								if (adress != null) {
									adress.moveToFirst();

									String city = adress
											.getString(adress
													.getColumnIndex(ContactsConsts.CITY));
									String street = adress
											.getString(adress
													.getColumnIndex(ContactsConsts.STREET));
									String zip = adress
											.getString(adress
													.getColumnIndex(ContactsConsts.ZIP));
									String country = adress
											.getString(adress
													.getColumnIndex(ContactsConsts.COUNTRY));
									String state = adress
											.getString(adress
													.getColumnIndex(ContactsConsts.STATE));

									// //System.out.println("*****************"
									// + city + " " + street + " " + state + " "
									// + country + " " + zip);

									if (city != null && !city.isEmpty()) {
										s.data(EmTags.CONTACTS_BUSINESS_ADDRESS_CITY,
												city);
									}
									if (street != null && !street.isEmpty()) {
										s.data(EmTags.CONTACTS_BUSINESS_ADDRESS_STREET,
												street);
									}
									if (zip != null && !zip.isEmpty()) {
										s.data(EmTags.CONTACTS_BUSINESS_ADDRESS_POSTAL_CODE,
												zip);
									}
									if (country != null && !country.isEmpty()) {
										s.data(EmTags.CONTACTS_BUSINESS_ADDRESS_COUNTRY,
												country);
									}
									if (state != null && !state.isEmpty()) {
										s.data(EmTags.CONTACTS_BUSINESS_ADDRESS_STATE,
												state);
									}

									adress.close();
								}
							}
						}
					}

					// Poupating Home Adress

					if (adressHome != null) {
						if (serverid != null && !serverid.isEmpty()) {
							String unique_id = "H" + "||" + serverid;

							// //System.out.println("*****************################");
							if (adressHome.equals("1")) {
								// //System.out.println("*****************");
								Cursor adress = Email
										.getAppContext()
										.getContentResolver()
										.query(ContactsConsts.CONTENT_URI_ADRESS,
												null,
												ContactsConsts.ADRESS_UNIQUE_ID
														+ "=" + "\""
														+ unique_id + "\"",
												null, null);
								if (adress != null) {
									adress.moveToFirst();

									String city = adress
											.getString(adress
													.getColumnIndex(ContactsConsts.CITY));
									String street = adress
											.getString(adress
													.getColumnIndex(ContactsConsts.STREET));
									String zip = adress
											.getString(adress
													.getColumnIndex(ContactsConsts.ZIP));
									String country = adress
											.getString(adress
													.getColumnIndex(ContactsConsts.COUNTRY));
									String state = adress
											.getString(adress
													.getColumnIndex(ContactsConsts.STATE));

									// //System.out.println("*****************"
									// + city + " " + street + " " + state + " "
									// + country + " " + zip);
									if (city != null && !city.isEmpty()) {
										s.data(EmTags.CONTACTS_HOME_ADDRESS_CITY,
												city);
									}
									if (street != null && !street.isEmpty()) {
										s.data(EmTags.CONTACTS_HOME_ADDRESS_STREET,
												street);
									}
									if (zip != null && !zip.isEmpty()) {
										s.data(EmTags.CONTACTS_HOME_ADDRESS_POSTAL_CODE,
												zip);
									}
									if (country != null && !country.isEmpty()) {
										s.data(EmTags.CONTACTS_HOME_ADDRESS_COUNTRY,
												country);
									}
									if (state != null && !state.isEmpty()) {
										s.data(EmTags.CONTACTS_HOME_ADDRESS_STATE,
												state);
									}

									adress.close();
								}
							}
						} else if (clientid != null && !clientid.isEmpty()) {
							String unique_id = "H" + "||" + clientid;
							// //System.out.println("*****************################12345");
							if (adressHome.equals("1")) {
								// //System.out.println("*****************");
								Cursor adress = Email
										.getAppContext()
										.getContentResolver()
										.query(ContactsConsts.CONTENT_URI_ADRESS,
												null,
												ContactsConsts.ADRESS_CLIENT_ID
														+ "=" + "\""
														+ unique_id + "\"",
												null, null);
								if (adress != null) {
									adress.moveToFirst();

									String city = adress
											.getString(adress
													.getColumnIndex(ContactsConsts.CITY));
									String street = adress
											.getString(adress
													.getColumnIndex(ContactsConsts.STREET));
									String zip = adress
											.getString(adress
													.getColumnIndex(ContactsConsts.ZIP));
									String country = adress
											.getString(adress
													.getColumnIndex(ContactsConsts.COUNTRY));
									String state = adress
											.getString(adress
													.getColumnIndex(ContactsConsts.STATE));

									// //System.out.println("*****************"
									// + city + " " + street + " " + state + " "
									// + country + " " + zip);

									if (city != null && !city.isEmpty()) {
										s.data(EmTags.CONTACTS_HOME_ADDRESS_CITY,
												city);
									}
									if (street != null && !street.isEmpty()) {
										s.data(EmTags.CONTACTS_HOME_ADDRESS_STREET,
												street);
									}
									if (zip != null && !zip.isEmpty()) {
										s.data(EmTags.CONTACTS_HOME_ADDRESS_POSTAL_CODE,
												zip);
									}
									if (country != null && !country.isEmpty()) {
										s.data(EmTags.CONTACTS_HOME_ADDRESS_COUNTRY,
												country);
									}
									if (state != null && !state.isEmpty()) {
										s.data(EmTags.CONTACTS_HOME_ADDRESS_STATE,
												state);
									}
									adress.close();
								}
							}
						}
					}

					// Poupating Other Adress

					if (adressOther != null) {
						if (serverid != null && !serverid.isEmpty()) {
							String unique_id = "O" + "||" + serverid;

							// //System.out.println("*****************################");
							if (adressOther.equals("1")) {
								// //System.out.println("*****************");
								Cursor adress = Email
										.getAppContext()
										.getContentResolver()
										.query(ContactsConsts.CONTENT_URI_ADRESS,
												null,
												ContactsConsts.ADRESS_UNIQUE_ID
														+ "=" + "\""
														+ unique_id + "\"",
												null, null);
								if (adress != null) {
									adress.moveToFirst();

									String city = adress
											.getString(adress
													.getColumnIndex(ContactsConsts.CITY));
									String street = adress
											.getString(adress
													.getColumnIndex(ContactsConsts.STREET));
									String zip = adress
											.getString(adress
													.getColumnIndex(ContactsConsts.ZIP));
									String country = adress
											.getString(adress
													.getColumnIndex(ContactsConsts.COUNTRY));
									String state = adress
											.getString(adress
													.getColumnIndex(ContactsConsts.STATE));

									// //System.out.println("*****************"
									// + city + " " + street + " " + state + " "
									// + country + " " + zip);
									if (city != null && !city.isEmpty()) {
										s.data(EmTags.CONTACTS_OTHER_ADDRESS_CITY,
												city);
									}
									if (street != null && !street.isEmpty()) {
										s.data(EmTags.CONTACTS_OTHER_ADDRESS_STREET,
												street);
									}
									if (zip != null && !zip.isEmpty()) {
										s.data(EmTags.CONTACTS_OTHER_ADDRESS_POSTAL_CODE,
												zip);
									}
									if (country != null && !country.isEmpty()) {
										s.data(EmTags.CONTACTS_OTHER_ADDRESS_COUNTRY,
												country);
									}
									if (state != null && !state.isEmpty()) {
										s.data(EmTags.CONTACTS_OTHER_ADDRESS_STATE,
												state);
									}

									adress.close();
								}
							}
						} else if (clientid != null && !clientid.isEmpty()) {
							String unique_id = "O" + "||" + clientid;
							// //System.out.println("*****************################12345");
							if (adressOther.equals("1")) {
								// //System.out.println("*****************");
								Cursor adress = Email
										.getAppContext()
										.getContentResolver()
										.query(ContactsConsts.CONTENT_URI_ADRESS,
												null,
												ContactsConsts.ADRESS_CLIENT_ID
														+ "=" + "\""
														+ unique_id + "\"",
												null, null);
								if (adress != null) {
									adress.moveToFirst();

									String city = adress
											.getString(adress
													.getColumnIndex(ContactsConsts.CITY));
									String street = adress
											.getString(adress
													.getColumnIndex(ContactsConsts.STREET));
									String zip = adress
											.getString(adress
													.getColumnIndex(ContactsConsts.ZIP));
									String country = adress
											.getString(adress
													.getColumnIndex(ContactsConsts.COUNTRY));
									String state = adress
											.getString(adress
													.getColumnIndex(ContactsConsts.STATE));

									// //System.out.println("*****************"
									// + city + " " + street + " " + state + " "
									// + country + " " + zip);

									if (city != null && !city.isEmpty()) {
										s.data(EmTags.CONTACTS_OTHER_ADDRESS_CITY,
												city);
									}
									if (street != null && !street.isEmpty()) {
										s.data(EmTags.CONTACTS_OTHER_ADDRESS_STREET,
												street);
									}
									if (zip != null && !zip.isEmpty()) {
										s.data(EmTags.CONTACTS_OTHER_ADDRESS_POSTAL_CODE,
												zip);
									}
									if (country != null && !country.isEmpty()) {
										s.data(EmTags.CONTACTS_OTHER_ADDRESS_COUNTRY,
												country);
									}
									if (state != null && !state.isEmpty()) {
										s.data(EmTags.CONTACTS_OTHER_ADDRESS_STATE,
												state);
									}
									adress.close();
								}
							}
						}
					}

				} catch (Exception e) {
					ContactsLog.d("Exception", "err: " + e.toString());
				}

				String im = cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_IM_NAME));
				ContactsLog.d("Exception", "im value " + im);
				if (im != null && !im.isEmpty()) {

					String[] typeSplit = im.split(":");
					// emailNumbers.put(email_type_number[0],
					// email_type_number[1]);

					ContactsLog.d("Exception", "typeSplit " + typeSplit.length);
					for (int i = 0; i < typeSplit.length; i++) {

						String[] imArray = typeSplit[i].split("=");

						if (imArray.length > 1) {
							String type = imArray[0];
							String imVal = imArray[1];
							if (imVal != null && !imVal.isEmpty()) {

								ContactsLog.d("Exception", "imVal: " + imVal);
								s.data(EmTags.CONTACTS2_IM_ADDRESS, imVal);
								break;
							}
						}

					}

				}

				String website = cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_WEB_ADRESS));

				if (website != null && !website.isEmpty()) {

					s.data(EmTags.CONTACTS_WEBPAGE, website);

				}

				String nickname = cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_NICK_NAME));

				if (nickname != null && !nickname.isEmpty()) {

					s.data(EmTags.CONTACTS2_NICKNAME, nickname);

				}

				String internetCall = cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_INTERNETCALL));
				if (internetCall != null && !internetCall.isEmpty()) {

					s.data(EmTags.CONTACTS_RADIO_TELEPHONE_NUMBER, internetCall);

				}

				String title = cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_TITLE));
				if (title != null && !title.isEmpty()) {
					// //System.out.println("datadt: title: " + title);

					s.data(EmTags.CONTACTS_TITLE, title);
				}

				String contact_suffix = cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_SUFFIX));
				if (contact_suffix != null && !contact_suffix.isEmpty()) {
					// //System.out.println("datadt: contact_suffix: " +
					// contact_suffix);

					s.data(EmTags.CONTACTS_SUFFIX, contact_suffix);

				}

				String contact_office = cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_OFFICE));
				if (contact_office != null && !contact_office.isEmpty()) {
					// //System.out.println("datadt: contact_office: " +
					// contact_office);
					s.data(EmTags.CONTACTS_OFFICE_LOCATION, contact_office);

				}

				String contact_department = cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_DEPARTMENT));
				if (contact_department != null && !contact_department.isEmpty()) {
					// //System.out.println("datadt: contact_department: " +
					// contact_department);
					s.data(EmTags.CONTACTS_DEPARTMENT, contact_department);

				}

				String contact_manger = cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_MANAGER));
				if (contact_manger != null && !contact_manger.isEmpty()) {
					// //System.out.println("datadt: contact_manger: " +
					// contact_manger);
					s.data(EmTags.CONTACTS2_MANAGER_NAME, contact_manger);

				}

				String contact_assistant = cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_ASSISTANT));
				if (contact_assistant != null && !contact_assistant.isEmpty()) {
					// //System.out.println("datadt: contact_assistant: " +
					// contact_assistant);
					s.data(EmTags.CONTACTS_ASSISTANT_NAME, contact_assistant);

				}

				String contact_aniversary = cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_ANNIVERSARY));
				if (contact_aniversary != null && !contact_aniversary.isEmpty()) {
					// //System.out.println("datadt: contact_aniversary: " +
					// contact_aniversary);
					s.data(EmTags.CONTACTS_ANNIVERSARY, contact_aniversary);

				}

				String contact_spouse = cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_SPOUSE));
				if (contact_spouse != null && !contact_spouse.isEmpty()) {
					// //System.out.println("datadt: contact_spouse: " +
					// contact_spouse);
					s.data(EmTags.CONTACTS_SPOUSE, contact_spouse);

				}

				// String nick_name =
				// cursor.getString(cursor.getColumnIndex(ContactsConsts.CONTACT_NICK_NAME));
				// if (nick_name != null && !nick_name.isEmpty()) {
				// ////System.out.println("datadt: nick_name: " + nick_name);
				// s.data(EmTags.CONTACTS2_NICKNAME, "NICKNAME");
				//
				// }

				String notes = cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_NOTES));
				if (notes != null && !notes.isEmpty()) {
					// //System.out.println("notes: " + notes);
					if (mService.mProtocolVersionDouble >= EmEas.SUPPORTED_PROTOCOL_EX2007_DOUBLE) {
						s.start(EmTags.BASE_BODY);
						s.data(EmTags.BASE_TYPE, EmEas.BODY_PREFERENCE_TEXT)
								.data(EmTags.BASE_DATA, notes);
						s.end();
					} else {
						s.data(EmTags.CONTACTS_BODY, notes);
					}
				}

				String yomi_first_name = cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_YOMIFIRSTNAME));
				if (yomi_first_name != null && !yomi_first_name.isEmpty()) {
					// //System.out.println("datadt: yomi_first_name: " +
					// yomi_first_name);
					s.data(EmTags.CONTACTS_YOMI_FIRST_NAME, yomi_first_name);

				}

				String yomi_last_name = cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_YOMILASTNAME));
				if (yomi_last_name != null && !yomi_last_name.isEmpty()) {
					// //System.out.println("datadt: yomi_last_name: " +
					// yomi_last_name);
					s.data(EmTags.CONTACTS_YOMI_LAST_NAME, yomi_last_name);

				}

				String yomi_company_name = cursor
						.getString(cursor
								.getColumnIndex(ContactsConsts.CONTACT_YOMICOMPANYNAME));
				if (yomi_company_name != null && !yomi_company_name.isEmpty()) {
					// //System.out.println("datadt: yomi_company_name: " +
					// yomi_company_name);
					s.data(EmTags.CONTACTS_YOMI_COMPANY_NAME, yomi_company_name);

				}

				String birthday = cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_BIRTH_DAY));
				if (birthday != null && !birthday.isEmpty()) {
					// //System.out.println("datadt: birthday: " + birthday);
					s.data(EmTags.CONTACTS_BIRTHDAY, birthday);

				}

				byte[] imageBytes = cursor.getBlob(cursor
						.getColumnIndex(ContactsConsts.CONTACT_PHOTO));

				ContactsLog.d(TAG, "pouplateContactDetailsByCursor  "
						+ imageBytes);
				if (imageBytes != null && imageBytes.length > 0
						&& imageBytes.length < 48000) {

					ContactsLog.d(TAG, "pouplateContactDetailsByCursor  "
							+ imageBytes.length);

					String pic = Base64.encodeToString(imageBytes,
							Base64.NO_WRAP);
					ContactsLog.d(TAG, "pouplateContactDetailsByCursor  pic "
							+ pic);
					if (pic != null && !pic.isEmpty()) {
						s.data(EmTags.CONTACTS_PICTURE, pic);
					} else {
						s.tag(EmTags.CONTACTS_PICTURE);
					}
				} else {

					ContactsLog.i("Conatacts", "Img not synced due to size... "
							+ imageBytes.length);

					s.tag(EmTags.CONTACTS_PICTURE);
				}

			}
		} catch (Exception e) {

			ContactsLog.d(TAG,
					"pouplateContactDetailsByCursor error " + e.toString());
		}
	}

	// public void pouplateContactDetailsByCursor(Cursor cursor, EmSerializer s)
	// {
	//
	// try {
	//
	// while (cursor != null && cursor.moveToNext()) {
	// String fileAs = "";
	// String firstName = cursor.getString(cursor
	// .getColumnIndex(ContactsConsts.CONTACT_FIRST_NAME));
	//
	// String lastName = cursor.getString(cursor
	// .getColumnIndex(ContactsConsts.CONTACT_LAST_NAME));
	// if (firstName != null && !firstName.isEmpty()) {
	// fileAs = firstName;
	// s.data(EmTags.CONTACTS_FIRST_NAME, firstName);
	// if (lastName != null && !lastName.isEmpty()) {
	// fileAs += " " + lastName;
	// s.data(EmTags.CONTACTS_LAST_NAME, lastName);
	//
	// }
	//
	// } else {
	// if (lastName != null && !lastName.isEmpty()) {
	// fileAs = lastName;
	// s.data(EmTags.CONTACTS_LAST_NAME, lastName);
	// }
	// }
	//
	// String middlename = cursor.getString(cursor
	// .getColumnIndex(ContactsConsts.CONTACT_MIDDLE_NAME));
	// if (middlename != null && !middlename.isEmpty()) {
	//
	// s.data(EmTags.CONTACTS_MIDDLE_NAME, middlename);
	//
	// }
	//
	// if (fileAs != null && !fileAs.isEmpty()) {
	//
	// s.data(EmTags.CONTACTS_FILE_AS, fileAs);
	//
	// }
	//
	// String jobtitle = cursor.getString(cursor
	// .getColumnIndex(ContactsConsts.CONTACT_JOB_TITLE));
	// if (jobtitle != null && !jobtitle.isEmpty()) {
	//
	// s.data(EmTags.CONTACTS_JOB_TITLE, jobtitle);
	//
	// }
	//
	// String org = cursor.getString(cursor
	// .getColumnIndex(ContactsConsts.CONTACT_COMPANY));
	//
	// if (org != null && !org.isEmpty()) {
	//
	// s.data(EmTags.CONTACTS_COMPANY_NAME, org);
	//
	// }
	//
	// // Email DATA Population
	//
	// try {
	//
	// // ContentValues emailNumbers = new ContentValues();
	// String email_details = cursor.getString(cursor
	// .getColumnIndex(ContactsConsts.CONTACT_EMAIL));
	// ContactsLog.d("Email details: ", "***" + email_details);
	// if (email_details != null && !email_details.isEmpty()) {
	// String[] email_array = null;
	// email_array = email_details.split(":");
	// String[] email_type_number = null;
	// /*
	// * System.out.println("email_array.length: " +
	// * email_array.length);
	// */
	// int[] tags = new int[] {
	// EmTags.CONTACTS_EMAIL1_ADDRESS,
	// EmTags.CONTACTS_EMAIL2_ADDRESS,
	// EmTags.CONTACTS_EMAIL3_ADDRESS };
	// for (int i = 0; i < email_array.length; i++) {
	// /*
	// * System.out.println("email_array.length: " +
	// * email_array[i]);
	// */
	//
	// if (email_array[i] != null
	// && !email_array[i].isEmpty()) {
	// if (i > 3) {
	// break;
	// }
	// email_type_number = email_array[i].split("=");
	//
	// ContactsLog.d("Email details: ",
	// "email_type_number**"
	// + email_type_number);
	// // emailNumbers.put(email_type_number[0],
	// // email_type_number[1]);
	// if (email_type_number.length > 1) {
	// // String type = email_type_number[0];
	// String email = email_type_number[1];
	// ContactsLog.d("Email details: ",
	// "email origianl**" + email);
	// if (email.indexOf("<") != -1) {
	// email = email.substring(0,
	// email.indexOf("<"));
	// email = email.replace("\"", "");
	// ContactsLog.d("Email details: ",
	// "email substring**" + email);
	// }
	//
	// if (email != null && !email.isEmpty()) {
	// s.data(tags[i], email);
	// }
	// }
	//
	// }
	// }
	//
	// }
	//
	// } catch (Exception e) {
	// ContactsLog.e("EMAIL CONTACT SYNC ADAPTER",
	// " email: " + e.toString());
	// }
	//
	// // Phone data Population
	//
	// try {
	// ContentValues phoneNumbers = new ContentValues();
	// String phone_details = cursor.getString(cursor
	// .getColumnIndex(ContactsConsts.CONTACT_PHONE));
	// // //System.out.println("Email details: " +
	// // phone_details);
	// if (phone_details != null && !phone_details.isEmpty()) {
	// String[] phone_array = null;
	// phone_array = phone_details.split(":");
	// String[] phone_type_number = null;
	// // //System.out.println("phone_array.length: " +
	// // phone_array.length);
	//
	// for (int i = 0; i < phone_array.length; i++) {
	// // //System.out.println("email_array.length: " +
	// // phone_array[i]);
	// if (phone_array[i] != null
	// && !phone_array[i].isEmpty()) {
	// phone_type_number = phone_array[i].split("=");
	// phoneNumbers.put(phone_type_number[0],
	// phone_type_number[1]);
	//
	// }
	// }
	//
	// if (phoneNumbers.size() > 0) {
	// if (phoneNumbers.containsKey("MOBILE")) {
	// String MOBILE = phoneNumbers.get("MOBILE")
	// .toString();
	// if (MOBILE != null && !MOBILE.isEmpty()) {
	// s.data(EmTags.CONTACTS_MOBILE_TELEPHONE_NUMBER,
	// MOBILE);
	// }
	// }
	//
	// if (phoneNumbers.containsKey("WORK2")) {
	// String WORK2 = phoneNumbers.get("WORK2")
	// .toString();
	// if (WORK2 != null && !WORK2.isEmpty()) {
	// s.data(EmTags.CONTACTS_BUSINESS2_TELEPHONE_NUMBER,
	// WORK2);
	// }
	// }
	//
	// if (phoneNumbers.containsKey("WORK")) {
	// String WORK = phoneNumbers.get("WORK")
	// .toString();
	// if (WORK != null && !WORK.isEmpty()) {
	// s.data(EmTags.CONTACTS_BUSINESS_TELEPHONE_NUMBER,
	// WORK);
	// }
	// }
	//
	// if (phoneNumbers.containsKey("FAX_WORK")) {
	// String FAX_WORK = phoneNumbers.get("FAX_WORK")
	// .toString();
	// if (FAX_WORK != null && !FAX_WORK.isEmpty()) {
	// s.data(EmTags.CONTACTS_BUSINESS_FAX_NUMBER,
	// FAX_WORK);
	// }
	// }
	//
	// if (phoneNumbers.containsKey("COMPANY_MAIN")) {
	// String COMPANY_MAIN = phoneNumbers.get(
	// "COMPANY_MAIN").toString();
	// if (COMPANY_MAIN != null
	// && !COMPANY_MAIN.isEmpty()) {
	// s.data(EmTags.CONTACTS2_COMPANY_MAIN_PHONE,
	// COMPANY_MAIN);
	// }
	// }
	//
	// if (phoneNumbers.containsKey("TYPE_FAX_HOME")) {
	// String TYPE_FAX_HOME = phoneNumbers.get(
	// "TYPE_FAX_HOME").toString();
	// if (TYPE_FAX_HOME != null
	// && !TYPE_FAX_HOME.isEmpty()) {
	// s.data(EmTags.CONTACTS_HOME_FAX_NUMBER,
	// TYPE_FAX_HOME);
	// }
	// }
	//
	// if (phoneNumbers.containsKey("HOME")) {
	// String HOME = phoneNumbers.get("HOME")
	// .toString();
	// if (HOME != null && !HOME.isEmpty()) {
	// s.data(EmTags.CONTACTS_HOME_TELEPHONE_NUMBER,
	// HOME);
	// }
	// }
	//
	// if (phoneNumbers.containsKey("HOME2")) {
	// String HOME2 = phoneNumbers.get("HOME2")
	// .toString();
	// if (HOME2 != null && !HOME2.isEmpty()) {
	// s.data(EmTags.CONTACTS_HOME2_TELEPHONE_NUMBER,
	// HOME2);
	// }
	// }
	//
	// if (phoneNumbers.containsKey("ASSISTANT")) {
	// String ASSISTANT = phoneNumbers
	// .get("ASSISTANT").toString();
	// if (ASSISTANT != null && !ASSISTANT.isEmpty()) {
	// s.data(EmTags.CONTACTS_ASSISTANT_TELEPHONE_NUMBER,
	// ASSISTANT);
	// }
	// }
	//
	// if (phoneNumbers.containsKey("PAGER")) {
	// String PAGER = phoneNumbers.get("PAGER")
	// .toString();
	// if (PAGER != null && !PAGER.isEmpty()) {
	// s.data(EmTags.CONTACTS_PAGER_NUMBER, PAGER);
	// }
	// }
	//
	// if (phoneNumbers.containsKey("CAR")) {
	// String CAR = phoneNumbers.get("CAR").toString();
	// if (CAR != null && !CAR.isEmpty()) {
	// s.data(EmTags.CONTACTS_CAR_TELEPHONE_NUMBER,
	// CAR);
	// }
	// }
	// }
	// }
	//
	// } catch (Exception e) {
	// ContactsLog.e("EMAIL CONTACT SYNC ADAPTER",
	// " phone: " + e.toString());
	// }
	//
	// // Adress data population
	// try {
	// String addressBusiness = cursor
	// .getString(cursor
	// .getColumnIndex(ContactsConsts.CONTACT_BUSS_ADRESS));
	// String adressHome = cursor
	// .getString(cursor
	// .getColumnIndex(ContactsConsts.CONTACT_HOME_ADRESS));
	// String adressOther = cursor
	// .getString(cursor
	// .getColumnIndex(ContactsConsts.CONTACT_OTHER_ADRESS));
	// String serverid = cursor.getString(cursor
	// .getColumnIndex(ContactsConsts.CONTACT_SERVER_ID));
	// String clientid = cursor.getString(cursor
	// .getColumnIndex(ContactsConsts.CONTACT_CLIENT_ID));
	//
	// // Pouplating data for Business Adress
	//
	// if (addressBusiness != null) {
	// if (serverid != null && !serverid.isEmpty()) {
	// String unique_id = "W" + "||" + serverid;
	//
	// // //System.out.println("*****************################");
	// if (addressBusiness.equals("1")) {
	// // //System.out.println("*****************");
	// Cursor adress = com.cognizant.trumobi.em.Email
	// .getAppContext()
	// .getContentResolver()
	// .query(ContactsConsts.CONTENT_URI_ADRESS,
	// null,
	// ContactsConsts.ADRESS_UNIQUE_ID
	// + "=" + "\""
	// + unique_id + "\"",
	// null, null);
	// if (adress != null) {
	// adress.moveToFirst();
	//
	// String city = adress
	// .getString(adress
	// .getColumnIndex(ContactsConsts.CITY));
	// String street = adress
	// .getString(adress
	// .getColumnIndex(ContactsConsts.STREET));
	// String zip = adress
	// .getString(adress
	// .getColumnIndex(ContactsConsts.ZIP));
	// String country = adress
	// .getString(adress
	// .getColumnIndex(ContactsConsts.COUNTRY));
	// String state = adress
	// .getString(adress
	// .getColumnIndex(ContactsConsts.STATE));
	//
	// // //System.out.println("*****************"
	// // + city + " " + street + " " + state +
	// // " "
	// // + country + " " + zip);
	// if (city != null && !city.isEmpty()) {
	// s.data(EmTags.CONTACTS_BUSINESS_ADDRESS_CITY,
	// city);
	// }
	// if (street != null && !street.isEmpty()) {
	// s.data(EmTags.CONTACTS_BUSINESS_ADDRESS_STREET,
	// street);
	// }
	// if (zip != null && !zip.isEmpty()) {
	// s.data(EmTags.CONTACTS_BUSINESS_ADDRESS_POSTAL_CODE,
	// zip);
	// }
	// if (country != null && !country.isEmpty()) {
	// s.data(EmTags.CONTACTS_BUSINESS_ADDRESS_COUNTRY,
	// country);
	// }
	// if (state != null && !state.isEmpty()) {
	// s.data(EmTags.CONTACTS_BUSINESS_ADDRESS_STATE,
	// state);
	// }
	// }
	// adress.close();
	// }
	// } else if (clientid != null && !clientid.isEmpty()) {
	// String unique_id = "W" + "||" + clientid;
	// // //System.out.println("*****************################12345");
	// if (addressBusiness.equals("1")) {
	// // //System.out.println("*****************");
	// Cursor adress = com.cognizant.trumobi.em.Email
	// .getAppContext()
	// .getContentResolver()
	// .query(ContactsConsts.CONTENT_URI_ADRESS,
	// null,
	// ContactsConsts.ADRESS_CLIENT_ID
	// + "=" + "\""
	// + unique_id + "\"",
	// null, null);
	// if (adress != null) {
	// adress.moveToFirst();
	//
	// String city = adress
	// .getString(adress
	// .getColumnIndex(ContactsConsts.CITY));
	// String street = adress
	// .getString(adress
	// .getColumnIndex(ContactsConsts.STREET));
	// String zip = adress
	// .getString(adress
	// .getColumnIndex(ContactsConsts.ZIP));
	// String country = adress
	// .getString(adress
	// .getColumnIndex(ContactsConsts.COUNTRY));
	// String state = adress
	// .getString(adress
	// .getColumnIndex(ContactsConsts.STATE));
	//
	// // //System.out.println("*****************"
	// // + city + " " + street + " " + state +
	// // " "
	// // + country + " " + zip);
	//
	// if (city != null && !city.isEmpty()) {
	// s.data(EmTags.CONTACTS_BUSINESS_ADDRESS_CITY,
	// city);
	// }
	// if (street != null && !street.isEmpty()) {
	// s.data(EmTags.CONTACTS_BUSINESS_ADDRESS_STREET,
	// street);
	// }
	// if (zip != null && !zip.isEmpty()) {
	// s.data(EmTags.CONTACTS_BUSINESS_ADDRESS_POSTAL_CODE,
	// zip);
	// }
	// if (country != null && !country.isEmpty()) {
	// s.data(EmTags.CONTACTS_BUSINESS_ADDRESS_COUNTRY,
	// country);
	// }
	// if (state != null && !state.isEmpty()) {
	// s.data(EmTags.CONTACTS_BUSINESS_ADDRESS_STATE,
	// state);
	// }
	//
	// adress.close();
	// }
	// }
	// }
	// }
	//
	// // Poupating Home Adress
	//
	// if (adressHome != null) {
	// if (serverid != null && !serverid.isEmpty()) {
	// String unique_id = "H" + "||" + serverid;
	//
	// // //System.out.println("*****************################");
	// if (adressHome.equals("1")) {
	// // //System.out.println("*****************");
	// Cursor adress = com.cognizant.trumobi.em.Email
	// .getAppContext()
	// .getContentResolver()
	// .query(ContactsConsts.CONTENT_URI_ADRESS,
	// null,
	// ContactsConsts.ADRESS_UNIQUE_ID
	// + "=" + "\""
	// + unique_id + "\"",
	// null, null);
	// if (adress != null) {
	// adress.moveToFirst();
	//
	// String city = adress
	// .getString(adress
	// .getColumnIndex(ContactsConsts.CITY));
	// String street = adress
	// .getString(adress
	// .getColumnIndex(ContactsConsts.STREET));
	// String zip = adress
	// .getString(adress
	// .getColumnIndex(ContactsConsts.ZIP));
	// String country = adress
	// .getString(adress
	// .getColumnIndex(ContactsConsts.COUNTRY));
	// String state = adress
	// .getString(adress
	// .getColumnIndex(ContactsConsts.STATE));
	//
	// // //System.out.println("*****************"
	// // + city + " " + street + " " + state +
	// // " "
	// // + country + " " + zip);
	// if (city != null && !city.isEmpty()) {
	// s.data(EmTags.CONTACTS_HOME_ADDRESS_CITY,
	// city);
	// }
	// if (street != null && !street.isEmpty()) {
	// s.data(EmTags.CONTACTS_HOME_ADDRESS_STREET,
	// street);
	// }
	// if (zip != null && !zip.isEmpty()) {
	// s.data(EmTags.CONTACTS_HOME_ADDRESS_POSTAL_CODE,
	// zip);
	// }
	// if (country != null && !country.isEmpty()) {
	// s.data(EmTags.CONTACTS_HOME_ADDRESS_COUNTRY,
	// country);
	// }
	// if (state != null && !state.isEmpty()) {
	// s.data(EmTags.CONTACTS_HOME_ADDRESS_STATE,
	// state);
	// }
	//
	// adress.close();
	// }
	// }
	// } else if (clientid != null && !clientid.isEmpty()) {
	// String unique_id = "H" + "||" + clientid;
	// // //System.out.println("*****************################12345");
	// if (adressHome.equals("1")) {
	// // //System.out.println("*****************");
	// Cursor adress = com.cognizant.trumobi.em.Email
	// .getAppContext()
	// .getContentResolver()
	// .query(ContactsConsts.CONTENT_URI_ADRESS,
	// null,
	// ContactsConsts.ADRESS_CLIENT_ID
	// + "=" + "\""
	// + unique_id + "\"",
	// null, null);
	// if (adress != null) {
	// adress.moveToFirst();
	//
	// String city = adress
	// .getString(adress
	// .getColumnIndex(ContactsConsts.CITY));
	// String street = adress
	// .getString(adress
	// .getColumnIndex(ContactsConsts.STREET));
	// String zip = adress
	// .getString(adress
	// .getColumnIndex(ContactsConsts.ZIP));
	// String country = adress
	// .getString(adress
	// .getColumnIndex(ContactsConsts.COUNTRY));
	// String state = adress
	// .getString(adress
	// .getColumnIndex(ContactsConsts.STATE));
	//
	// // //System.out.println("*****************"
	// // + city + " " + street + " " + state +
	// // " "
	// // + country + " " + zip);
	//
	// if (city != null && !city.isEmpty()) {
	// s.data(EmTags.CONTACTS_HOME_ADDRESS_CITY,
	// city);
	// }
	// if (street != null && !street.isEmpty()) {
	// s.data(EmTags.CONTACTS_HOME_ADDRESS_STREET,
	// street);
	// }
	// if (zip != null && !zip.isEmpty()) {
	// s.data(EmTags.CONTACTS_HOME_ADDRESS_POSTAL_CODE,
	// zip);
	// }
	// if (country != null && !country.isEmpty()) {
	// s.data(EmTags.CONTACTS_HOME_ADDRESS_COUNTRY,
	// country);
	// }
	// if (state != null && !state.isEmpty()) {
	// s.data(EmTags.CONTACTS_HOME_ADDRESS_STATE,
	// state);
	// }
	// adress.close();
	// }
	// }
	// }
	// }
	//
	// // Poupating Other Adress
	//
	// if (adressOther != null) {
	// if (serverid != null && !serverid.isEmpty()) {
	// String unique_id = "O" + "||" + serverid;
	//
	// // //System.out.println("*****************################");
	// if (adressOther.equals("1")) {
	// // //System.out.println("*****************");
	// Cursor adress = com.cognizant.trumobi.em.Email
	// .getAppContext()
	// .getContentResolver()
	// .query(ContactsConsts.CONTENT_URI_ADRESS,
	// null,
	// ContactsConsts.ADRESS_UNIQUE_ID
	// + "=" + "\""
	// + unique_id + "\"",
	// null, null);
	// if (adress != null) {
	// adress.moveToFirst();
	//
	// String city = adress
	// .getString(adress
	// .getColumnIndex(ContactsConsts.CITY));
	// String street = adress
	// .getString(adress
	// .getColumnIndex(ContactsConsts.STREET));
	// String zip = adress
	// .getString(adress
	// .getColumnIndex(ContactsConsts.ZIP));
	// String country = adress
	// .getString(adress
	// .getColumnIndex(ContactsConsts.COUNTRY));
	// String state = adress
	// .getString(adress
	// .getColumnIndex(ContactsConsts.STATE));
	//
	// // //System.out.println("*****************"
	// // + city + " " + street + " " + state +
	// // " "
	// // + country + " " + zip);
	// if (city != null && !city.isEmpty()) {
	// s.data(EmTags.CONTACTS_OTHER_ADDRESS_CITY,
	// city);
	// }
	// if (street != null && !street.isEmpty()) {
	// s.data(EmTags.CONTACTS_OTHER_ADDRESS_STREET,
	// street);
	// }
	// if (zip != null && !zip.isEmpty()) {
	// s.data(EmTags.CONTACTS_OTHER_ADDRESS_POSTAL_CODE,
	// zip);
	// }
	// if (country != null && !country.isEmpty()) {
	// s.data(EmTags.CONTACTS_OTHER_ADDRESS_COUNTRY,
	// country);
	// }
	// if (state != null && !state.isEmpty()) {
	// s.data(EmTags.CONTACTS_OTHER_ADDRESS_STATE,
	// state);
	// }
	//
	// adress.close();
	// }
	// }
	// } else if (clientid != null && !clientid.isEmpty()) {
	// String unique_id = "O" + "||" + clientid;
	// // //System.out.println("*****************################12345");
	// if (adressHome.equals("1")) {
	// // //System.out.println("*****************");
	// Cursor adress = com.cognizant.trumobi.em.Email
	// .getAppContext()
	// .getContentResolver()
	// .query(ContactsConsts.CONTENT_URI_ADRESS,
	// null,
	// ContactsConsts.ADRESS_CLIENT_ID
	// + "=" + "\""
	// + unique_id + "\"",
	// null, null);
	// if (adress != null) {
	// adress.moveToFirst();
	//
	// String city = adress
	// .getString(adress
	// .getColumnIndex(ContactsConsts.CITY));
	// String street = adress
	// .getString(adress
	// .getColumnIndex(ContactsConsts.STREET));
	// String zip = adress
	// .getString(adress
	// .getColumnIndex(ContactsConsts.ZIP));
	// String country = adress
	// .getString(adress
	// .getColumnIndex(ContactsConsts.COUNTRY));
	// String state = adress
	// .getString(adress
	// .getColumnIndex(ContactsConsts.STATE));
	//
	// // //System.out.println("*****************"
	// // + city + " " + street + " " + state +
	// // " "
	// // + country + " " + zip);
	//
	// if (city != null && !city.isEmpty()) {
	// s.data(EmTags.CONTACTS_OTHER_ADDRESS_CITY,
	// city);
	// }
	// if (street != null && !street.isEmpty()) {
	// s.data(EmTags.CONTACTS_OTHER_ADDRESS_STREET,
	// street);
	// }
	// if (zip != null && !zip.isEmpty()) {
	// s.data(EmTags.CONTACTS_OTHER_ADDRESS_POSTAL_CODE,
	// zip);
	// }
	// if (country != null && !country.isEmpty()) {
	// s.data(EmTags.CONTACTS_OTHER_ADDRESS_COUNTRY,
	// country);
	// }
	// if (state != null && !state.isEmpty()) {
	// s.data(EmTags.CONTACTS_OTHER_ADDRESS_STATE,
	// state);
	// }
	// adress.close();
	// }
	// }
	// }
	// }
	//
	// } catch (Exception e) {
	// ContactsLog.d("Exception", "err: " + e.toString());
	// }
	//
	// String im = cursor.getString(cursor
	// .getColumnIndex(ContactsConsts.CONTACT_IM_NAME));
	// ContactsLog.d("Exception", "im value " + im);
	// if (im != null && !im.isEmpty()) {
	//
	// String[] typeSplit = im.split(":");
	// // emailNumbers.put(email_type_number[0],
	// // email_type_number[1]);
	//
	// ContactsLog.d("Exception", "typeSplit " + typeSplit.length);
	// for (int i = 0; i < typeSplit.length; i++) {
	//
	// String[] imArray = typeSplit[i].split("=");
	//
	// if (imArray.length > 1) {
	// // String type = imArray[0];
	// String imVal = imArray[1];
	// if (imVal != null && !imVal.isEmpty()) {
	//
	// ContactsLog.d("Exception", "imVal: " + imVal);
	// s.data(EmTags.CONTACTS2_IM_ADDRESS, imVal);
	// break;
	// }
	// }
	//
	// }
	//
	// }
	//
	// String website = cursor.getString(cursor
	// .getColumnIndex(ContactsConsts.CONTACT_WEB_ADRESS));
	//
	// if (website != null && !website.isEmpty()) {
	//
	// s.data(EmTags.CONTACTS_WEBPAGE, website);
	//
	// }
	//
	// String nickname = cursor.getString(cursor
	// .getColumnIndex(ContactsConsts.CONTACT_NICK_NAME));
	//
	// if (nickname != null && !nickname.isEmpty()) {
	//
	// s.data(EmTags.CONTACTS2_NICKNAME, nickname);
	//
	// }
	//
	// String internetCall = cursor.getString(cursor
	// .getColumnIndex(ContactsConsts.CONTACT_INTERNETCALL));
	// if (internetCall != null && !internetCall.isEmpty()) {
	//
	// s.data(EmTags.CONTACTS_RADIO_TELEPHONE_NUMBER, internetCall);
	//
	// }
	//
	// String title = cursor.getString(cursor
	// .getColumnIndex(ContactsConsts.CONTACT_TITLE));
	// if (title != null && !title.isEmpty()) {
	// // //System.out.println("datadt: title: " + title);
	//
	// s.data(EmTags.CONTACTS_TITLE, title);
	// }
	//
	// String contact_suffix = cursor.getString(cursor
	// .getColumnIndex(ContactsConsts.CONTACT_SUFFIX));
	// if (contact_suffix != null && !contact_suffix.isEmpty()) {
	// // //System.out.println("datadt: contact_suffix: " +
	// // contact_suffix);
	//
	// s.data(EmTags.CONTACTS_SUFFIX, contact_suffix);
	//
	// }
	//
	// String contact_office = cursor.getString(cursor
	// .getColumnIndex(ContactsConsts.CONTACT_OFFICE));
	// if (contact_office != null && !contact_office.isEmpty()) {
	// // //System.out.println("datadt: contact_office: " +
	// // contact_office);
	// s.data(EmTags.CONTACTS_OFFICE_LOCATION, contact_office);
	//
	// }
	//
	// String contact_department = cursor.getString(cursor
	// .getColumnIndex(ContactsConsts.CONTACT_DEPARTMENT));
	// if (contact_department != null && !contact_department.isEmpty()) {
	// // //System.out.println("datadt: contact_department: " +
	// // contact_department);
	// s.data(EmTags.CONTACTS_DEPARTMENT, contact_department);
	//
	// }
	//
	// String contact_manger = cursor.getString(cursor
	// .getColumnIndex(ContactsConsts.CONTACT_MANAGER));
	// if (contact_manger != null && !contact_manger.isEmpty()) {
	// // //System.out.println("datadt: contact_manger: " +
	// // contact_manger);
	// s.data(EmTags.CONTACTS2_MANAGER_NAME, contact_manger);
	//
	// }
	//
	// String contact_assistant = cursor.getString(cursor
	// .getColumnIndex(ContactsConsts.CONTACT_ASSISTANT));
	// if (contact_assistant != null && !contact_assistant.isEmpty()) {
	// // //System.out.println("datadt: contact_assistant: " +
	// // contact_assistant);
	// s.data(EmTags.CONTACTS_ASSISTANT_NAME, contact_assistant);
	//
	// }
	//
	// String contact_aniversary = cursor.getString(cursor
	// .getColumnIndex(ContactsConsts.CONTACT_ANNIVERSARY));
	// if (contact_aniversary != null && !contact_aniversary.isEmpty()) {
	// // //System.out.println("datadt: contact_aniversary: " +
	// // contact_aniversary);
	// s.data(EmTags.CONTACTS_ANNIVERSARY, contact_aniversary);
	//
	// }
	//
	// String contact_spouse = cursor.getString(cursor
	// .getColumnIndex(ContactsConsts.CONTACT_SPOUSE));
	// if (contact_spouse != null && !contact_spouse.isEmpty()) {
	// // //System.out.println("datadt: contact_spouse: " +
	// // contact_spouse);
	// s.data(EmTags.CONTACTS_SPOUSE, contact_spouse);
	//
	// }
	//
	// // String nick_name =
	// //
	// cursor.getString(cursor.getColumnIndex(ContactsConsts.CONTACT_NICK_NAME));
	// // if (nick_name != null && !nick_name.isEmpty()) {
	// // ////System.out.println("datadt: nick_name: " +
	// // nick_name);
	// // s.data(EmTags.CONTACTS2_NICKNAME, "NICKNAME");
	// //
	// // }
	//
	// String notes = cursor.getString(cursor
	// .getColumnIndex(ContactsConsts.CONTACT_NOTES));
	// if (notes != null && !notes.isEmpty()) {
	// // //System.out.println("notes: " + notes);
	// if (mService.mProtocolVersionDouble >=
	// EmEas.SUPPORTED_PROTOCOL_EX2007_DOUBLE) {
	// s.start(EmTags.BASE_BODY);
	// s.data(EmTags.BASE_TYPE, EmEas.BODY_PREFERENCE_TEXT)
	// .data(EmTags.BASE_DATA, notes);
	// s.end();
	// } else {
	// s.data(EmTags.CONTACTS_BODY, notes);
	// }
	// }
	//
	// String yomi_first_name = cursor.getString(cursor
	// .getColumnIndex(ContactsConsts.CONTACT_YOMIFIRSTNAME));
	// if (yomi_first_name != null && !yomi_first_name.isEmpty()) {
	// // //System.out.println("datadt: yomi_first_name: " +
	// // yomi_first_name);
	// s.data(EmTags.CONTACTS_YOMI_FIRST_NAME, yomi_first_name);
	//
	// }
	//
	// String yomi_last_name = cursor.getString(cursor
	// .getColumnIndex(ContactsConsts.CONTACT_YOMILASTNAME));
	// if (yomi_last_name != null && !yomi_last_name.isEmpty()) {
	// // //System.out.println("datadt: yomi_last_name: " +
	// // yomi_last_name);
	// s.data(EmTags.CONTACTS_YOMI_LAST_NAME, yomi_last_name);
	//
	// }
	//
	// String yomi_company_name = cursor
	// .getString(cursor
	// .getColumnIndex(ContactsConsts.CONTACT_YOMICOMPANYNAME));
	// if (yomi_company_name != null && !yomi_company_name.isEmpty()) {
	// // //System.out.println("datadt: yomi_company_name: " +
	// // yomi_company_name);
	// s.data(EmTags.CONTACTS_YOMI_COMPANY_NAME, yomi_company_name);
	//
	// }
	//
	// String birthday = cursor.getString(cursor
	// .getColumnIndex(ContactsConsts.CONTACT_BIRTH_DAY));
	// if (birthday != null && !birthday.isEmpty()) {
	// // //System.out.println("datadt: birthday: " +
	// // birthday);
	// s.data(EmTags.CONTACTS_BIRTHDAY, birthday);
	//
	// }
	//
	// // String mobile = cursor.getString(cursor
	// // .getColumnIndex(ContactsConsts.CONTACT_PHONE));
	// //
	// // if (mobile != null && !mobile.isEmpty()) {
	// //
	// // s.data(EmTags.CONTACTS_MOBILE_TELEPHONE_NUMBER,
	// // mobile);
	// //
	// // }
	// //
	// // String business_tel = cursor.getString(cursor
	// // .getColumnIndex(ContactsConsts.CONTACT_PHONE));
	// //
	// // if (business_tel != null && !business_tel.isEmpty()) {
	// //
	// // s.data(EmTags.CONTACTS_BUSINESS_TELEPHONE_NUMBER,
	// // business_tel);
	// //
	// // }
	//
	// // String email_primary = cursor.getString(cursor
	// // .getColumnIndex(ContactsConsts.CONTACT_EMAIL));
	// //
	// // if (email_primary != null && !email_primary.isEmpty()) {
	// //
	// // s.data(EmTags.CONTACTS_EMAIL1_ADDRESS, email_primary);
	// //
	// // }
	//
	// // String addressBusiness = cursor.getString(cursor
	// // .getColumnIndex(ContactsConsts.CONTACT_BUSS_ADRESS));
	// //
	// // if (addressBusiness != null &&
	// // !addressBusiness.isEmpty()) {
	// //
	// // // s.data(EmTags.CONTACTS_bu, addressBusiness);
	// //
	// // }
	// // String im = cursor.getString(cursor
	// // .getColumnIndex(ContactsConsts.CONTACT_IM_NAME));
	// //
	// // if (im != null && !im.isEmpty()) {
	// //
	// // s.data(EmTags.CONTACTS2_IM_ADDRESS, im);
	// //
	// // }
	// //
	// // String notes = cursor.getString(cursor
	// // .getColumnIndex(ContactsConsts.CONTACT_NOTES));
	// //
	// // if (notes != null && !notes.isEmpty()) {
	// //
	// // // s.data(EmTags.CONTACTS_, notes);
	// //
	// // }
	// //
	//
	// /*
	// * String website =
	// * cursor.getString(cursor.getColumnIndex(ContactsConsts
	// * .CONTACT_WEB_ADRESS));
	// *
	// * if (website != null && !website.isEmpty()) {
	// *
	// * s.data(EmTags.CONTACTS_WEBPAGE, website);
	// *
	// * }
	// */
	//
	// // String nickname = cursor.getString(cursor
	// // .getColumnIndex(ContactsConsts.CONTACT_NICK_NAME));
	// //
	// // if (nickname != null && !nickname.isEmpty()) {
	// //
	// // s.data(EmTags.CONTACTS2_NICKNAME, nickname);
	// //
	// // }
	//
	// // String internetCall = cursor.getString(cursor
	// // .getColumnIndex(ContactsConsts.CONTACT_INTERNETCALL));
	// //
	// // if (internetCall != null &&S !internetCall.isEmpty()) {
	// //
	// // s.data(EmTags.CONTACTS_RADIO_TELEPHONE_NUMBER,
	// // internetCall);
	// //
	// // }
	//
	// byte[] imageBytes = cursor.getBlob(cursor
	// .getColumnIndex(ContactsConsts.CONTACT_PHOTO));
	//
	// if (imageBytes != null && imageBytes.length > 0) {
	// String pic = Base64.encodeToString(imageBytes,
	// Base64.NO_WRAP);
	//
	// if (pic != null && !pic.isEmpty()) {
	// s.data(EmTags.CONTACTS_PICTURE, pic);
	// } else {
	// s.tag(EmTags.CONTACTS_PICTURE);
	// }
	// } else {
	// s.tag(EmTags.CONTACTS_PICTURE);
	// }
	//
	// }
	//
	// } catch (Exception e) {
	//
	// ContactsLog.d(TAG,
	// "pouplateContactDetailsByCursor error " + e.toString());
	// }
	// }

	Cursor getCursor(int tag, String contactId) {
		Cursor cursor = null;
		switch (tag) {

		case EmTags.SYNC_ADD:
			cursor = com.cognizant.trumobi.em.Email
					.getAppContext()
					.getContentResolver()
					.query(ContactsConsts.CONTENT_URI_CONTACTS, null,
							ContactsConsts.CONTACT_CLIENT_ID + "=?",
							new String[] { contactId }, null);

			// .query(ContactsConsts.CONTENT_URI_CONTACTS_SYNC_STATE,
			// null, ContactsConsts.ACCOUNT_NAME + "=?",
			// new String[] { account_name }, null);

			break;
		case EmTags.SYNC_CHANGE:

			cursor = com.cognizant.trumobi.em.Email
					.getAppContext()
					.getContentResolver()
					.query(ContactsConsts.CONTENT_URI_CONTACTS, null,
							ContactsConsts.CONTACT_SERVER_ID + "= ?",
							new String[] { contactId }, null);

			break;

		default:
			break;
		}
		return cursor;
	}

	@Override
	public boolean sendLocalChanges(EmSerializer s) {

		ContactsLog.d(TAG, "sendLocalChanges");

		try {
			if (getSyncKey().equals("0")) {
				return false;
			}

			boolean first = true;

			if (first) {
				s.start(EmTags.SYNC_COMMANDS);
				ContactsLog.d(EmContactsSyncAdapter.class.getCanonicalName(),
						"Sending Contacts changes to the server");
				first = false;
			}
			get_local_contact_added(s);
			get_local_contact_updates(s);
			get_local_deleted(s);

			if (!first) {
				s.end(); // Commands
			}
		} catch (Exception e) {

			ContactsLog.e(EmContactsSyncAdapter.class.getCanonicalName(),
					"Exception " + e.toString());
		}

		return false;

	}

	@Override
	public String getCollectionName() {
		// TODO Auto-generated method stub
		return "Contacts";
	}

	@Override
	public void cleanup() {
		// TODO Auto-generated method stub

	}
	

	//Method to handle the Custom email type append with email type from server
	private ContentValues emailData(String serverId){
		String email_details = null;
		ContentValues emailVal = new ContentValues();
		Cursor cursor = Email.getAppContext()
				.getContentResolver()
				.query(ContactsConsts.CONTENT_URI_CONTACTS, null,
						SERVER_ID_SELECTION, new String[] { serverId },
						null);
		if (cursor != null && cursor.moveToFirst()) {
			while (cursor.isAfterLast() == false) {
				email_details = cursor
						.getString(cursor
								.getColumnIndex(ContactsConsts.CONTACT_EMAIL));
				try {
					// //System.out.println("Email details: " + phone_details);
					if (email_details != null && !email_details.isEmpty()) {
						String[] email_array = null;
						email_array = email_details.split(":");
						String[] email_type_val = null;

						for (int i = 0; i < email_array.length; i++) {
							if (email_array[i] != null
									&& !email_array[i].isEmpty()) {
								email_type_val = email_array[i].split("=");
								if(email_type_val[0].contains("CUSTOM")){
									emailVal.put(email_type_val[0],email_type_val[1]);
								}
								

							}
						}
					}

				} catch (Exception e) {
					ContactsLog.e("EMAIL CONTACT SYNC ADAPTER",
							" Email: " + e.toString());
				}
				cursor.moveToNext();
			}
		}
		cursor.close();
		cursor = null;
		return emailVal;
	}
	
	
	//Method to handle the Custom phone type append with Phone type from server
		private ContentValues phoneData(String serverId){
			String phone_details = null;
			ContentValues phoneVal = new ContentValues();
			Cursor cursor = Email.getAppContext()
					.getContentResolver()
					.query(ContactsConsts.CONTENT_URI_CONTACTS, null,
							SERVER_ID_SELECTION, new String[] { serverId },
							null);
			if (cursor != null && cursor.moveToFirst()) {
				while (cursor.isAfterLast() == false) {
					phone_details = cursor
							.getString(cursor
									.getColumnIndex(ContactsConsts.CONTACT_PHONE));
					try {
						// //System.out.println("phone details: " + phone_details);
						if (phone_details != null && !phone_details.isEmpty()) {
							String[] phone_array = null;
							phone_array = phone_details.split(":");
							String[] phone_type_val = null;

							for (int i = 0; i < phone_array.length; i++) {
								if (phone_array[i] != null
										&& !phone_array[i].isEmpty()) {
									phone_type_val = phone_array[i].split("=");
									if(phone_type_val[0].contains("CUSTOM") || phone_type_val[0].contains("OTHER")){
										phoneVal.put(phone_type_val[0],phone_type_val[1]);
									}
								}
							}
						}

					} catch (Exception e) {
						ContactsLog.e("EMAIL CONTACT SYNC ADAPTER",
								" Phone: " + e.toString());
					}
					cursor.moveToNext();
				}
			}
			cursor.close();
			cursor = null;
			return phoneVal;
		}
	
	//Method to the Concatenate EmailType as single string
	private String updatedEmailVal(ContentValues vals){
		
		String newString ="";
		Set<Entry<String, Object>> s=vals.valueSet();
		   Iterator itr = s.iterator();
		   while(itr.hasNext()){
		        Map.Entry me = (Map.Entry)itr.next(); 
		        String key = me.getKey().toString();
		        Object value =  me.getValue();

		        if(value != null){
		        	newString = newString + key + "="+ value.toString()+ ":";
		        }
		        ContactsLog.d("DatabaseSync", "Key :: " +key  +"    :::    values --- : "  + (String)(value == null?null:value.toString()));
		   }
		
		
		return newString;
	}
}
