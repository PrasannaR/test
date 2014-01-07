package com.cognizant.trubox.contacts.db;

import android.content.ContentResolver;
import android.net.Uri;

public class ContactsConsts {

	public static final String DATABASE_NAME = "cognizantcontacts.db";
	public static final int DATABASE_VERSION = 1;

	public static final String CONTACTS_TABLE_NAME = "contacts";
	public static final String GROUPS_TABLE_NAME = "groups";
	public static final String RECENTS_TABLE_NAME = "recents";
	public static final String CONTACTS_ADRESS_TABLE_NAME = "adress";
	public static final String CONTACTS_SYNCSTATE_TABLE_NAME = "contacts_syncstate";

	// Local cache entry tables for tracking the changes to the db before it is
	// synced to the server.....
	public static final String CONTACTS_LOCAL_CACHE_ADD_TABLE_NAME = "contacts_local_cache_add";
	public static final String CONTACTS_LOCAL_CACHE_UPDATE_TABLE_NAME = "contacts_local_cache_update";
	public static final String CONTACTS_LOCAL_CACHE_DELETE_TABLE_NAME = "contacts_local_cache_delete";

	public static final String AUTHORITY = "com.cognizant.trumobi.em.provider";

	public static final Uri CONTENT_URI_CONTACTS = Uri.parse("content://"
			+ AUTHORITY + "/" + CONTACTS_TABLE_NAME);
	public static final Uri CONTENT_URI_ADRESS = Uri.parse("content://"
			+ AUTHORITY + "/" + CONTACTS_ADRESS_TABLE_NAME);
	public static final Uri CONTENT_URI_GROUPS = Uri.parse("content://"
			+ AUTHORITY + "/" + GROUPS_TABLE_NAME);
	public static final Uri CONTENT_URI_CONTACTS_LOCAL_CACHE_ADD = Uri
			.parse("content://" + AUTHORITY + "/"
					+ CONTACTS_LOCAL_CACHE_ADD_TABLE_NAME);
	public static final Uri CONTENT_URI_CONTACTS_LOCAL_CACHE_UPDATE = Uri
			.parse("content://" + AUTHORITY + "/"
					+ CONTACTS_LOCAL_CACHE_UPDATE_TABLE_NAME);
	public static final Uri CONTENT_URI_CONTACTS_LOCAL_CACHE_DELETE = Uri
			.parse("content://" + AUTHORITY + "/"
					+ CONTACTS_LOCAL_CACHE_DELETE_TABLE_NAME);

	public static final Uri CONTENT_URI_CONTACTS_SYNC_STATE = Uri
			.parse("content://" + AUTHORITY + "/"
					+ CONTACTS_SYNCSTATE_TABLE_NAME);

	public static final String CONTACT_ID = "_id";
	public static final String CONTACT_TITLE = "contact_tile";
	public static final String CONTACT_FIRST_NAME = "first_name";
	public static final String CONTACT_MIDDLE_NAME = "mid_name";
	public static final String CONTACT_LAST_NAME = "last_name";
	public static final String CONTACT_SUFFIX = "contact_suffix";
	public static final String CONTACT_BIRTH_DAY = "birth_day";
	public static final String CONTACT_DEPARTMENT = "department";
	public static final String CONTACT_IM_NAME = "im";
	public static final String CONTACT_NOTES = "notes";
	public static final String CONTACT_EMAIL = "email";
	public static final String CONTACT_SERVER_ID = "server_id";
	public static final String CONTACT_CLIENT_ID = "client_id";
	public static final String CONTACT_PHOTO = "contact_photo";
	public static final String CONTACT_IS_FAVORITE = "is_favorite";
	public static final String CONTACT_WEB_ADRESS = "web_adress";
	public static final String CONTACT_PHONE = "contact_phone";
	public static final String CONTACT_BUSS_ADRESS = "buss_adress";
	public static final String CONTACT_HOME_ADRESS = "home_adress";
	public static final String CONTACT_OTHER_ADRESS = "other_adress";
	public static final String CONTACT_OFFICE = "contact_office";
	public static final String CONTACT_COMPANY = "contact_company";
	public static final String CONTACT_MANAGER = "contact_manager";
	public static final String CONTACT_ASSISTANT = "contact_assistant";

	public static final String CONTACT_JOB_TITLE = "job_title";
	public static final String CONTACT_ANNIVERSARY = "anniversary";
	public static final String CONTACT_SPOUSE = "spouse";
	public static final String CONTACT_YOMIFIRSTNAME = "yomifirstname";
	public static final String CONTACT_YOMILASTNAME = "yomilastname";
	public static final String CONTACT_YOMICOMPANYNAME = "yomicompanyname";

	public static final String CONTACT_PRIM_EMAIL = "prim_email";
	public static final String CONTACT_SEC_EMAIL = "sec_email";

	public static final String CONTACT_BUSINESS_ORGN = "busi_orgn";
	public static final String CONTACT_BUSINESS_DESIGNATION = "busi_designation";
	public static final String CONTACT_BUSINESS_LOCATION = "busi_addr";

	public static final String CONTACT_BUSINESS_CITY = "busi_city";
	public static final String CONTACT_BUSINESS_STATE = "busi_state";
	public static final String CONTACT_BUSINESS_PINCODE = "busi_zip";

	public static final String CONTACT_NAME_PREFIX = "name_prefix";
	public static final String CONTACT_NAME_SUFFIX = "name_suffix";
	public static final String CONTACT_PHONETIC_FAMILY_NAME = "contact_phonetic_family_name";
	public static final String CONTACT_PHONETIC_MIDDLE_NAME = "contact_phonetic_middle_name";
	public static final String CONTACT_PHONETIC_GIVEN_NAME = "contact_phonetic_given_name";
	public static final String CONTACT_PH_NO_MOBILE_TYPE1 = "phone_number_mobile_type1";
	public static final String CONTACT_PH_NO_MOBILE_TYPE2 = "phone_number_mobile_type2";
	public static final String CONTACT_EMAIL_TYPE = "contact_email_type";
	public static final String CONTACT_IM_TYPE = "contact_im_type";
	public static final String CONTACT_BUSINESS_LOCATION_TYPE = "contact_business_location_type";

	public static final String CONTACT_NICK_NAME = "contact_nick_name";
	public static final String CONTACT_WEBSITE = "contact_website";
	public static final String CONTACT_INTERNETCALL = "contact_internetcall";

	public static final String CONTACT_PH_NO_MOBILE = "phone_number_mobile";
	public static final String CONTACT_PH_NO_RES = "phone_number_res";
	public static final String CONTACT_PH_NO_BUSINESS = "phone_number_business";

	public static final String CONTACT_DB_ACTION_DELETE = "contact_delete";
	public static final String CONTACT_DB_ACTION_ADD = "contact_add";
	public static final String CONTACT_DB_ACTION_UPDATE = "contact_update";

	public static final String CONTACT_RINGTONE_PATH = "contact_ringtone_path";

	public static final String SYNC_ID = "_id";
	public static final String ACCOUNT_NAME = "account_name";
	public static final String ACCOUNT_ID = "accound_id";
	public static final String SYNC_KEY = "sync_key";
	public static final String CONTACTS_SYNC_STATE = "sync_state";

	public static final String GROUP_ID = "_id";
	public static final String GROUP_NAME = "group_name";

	public static final String RECENT_ID = "_id";
	public static final String RECENT_NAME = "recent_name";

	public static final String ADRESS_ID = "_id";
	public static final String ADRESS_UNIQUE_ID = "unique_id";
	public static final String ADRESS_CLIENT_ID = "unique_client_id";
	public static final String STREET = "street";
	public static final String CITY = "city";
	public static final String STATE = "state";
	public static final String ZIP = "zip";
	public static final String COUNTRY = "country";

	public static class RetrievalType {
		public static final int CONTACT_GET_ALL = 0;
		public static final int CONTACT_GET_ONE = 1;
		public static final int GROUP_GET_ALL = 2;
		public static final int GROUP_GET_ONE = 3;
		public static final int CONTACT_ADD_CACHE_ALL = 4;
		public static final int CONTACT_ADD_CACHE_ONE = 5;
		public static final int CONTACT_DELETE_CACHE_ALL = 6;
		public static final int CONTACT_DELETE_CACHE_ONE = 7;
		public static final int CONTACT_UPDATE_CACHE_ALL = 8;
		public static final int CONTACT_UPDATE_CACHE_ONE = 9;
		public static final int CONTACT_ADRESS_TABLE = 10;

	}

	public static final String CONTACTS_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/vnd.com.cognizant.trubox.contactssample.Contacts";
	public static final String SINGLE_CONTACT_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/vnd.com.cognizant.trubox.contactssample.Contacts";

	public static final String ADRESS_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/vnd.com.cognizant.trubox.contactssample.Contacts";

	public static final String SYNCSTATE_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/vnd.com.cognizant.trubox.contactssample.Contacts";

	// Sudarshan changes

	public static String dialedNumber = "";
	public static String callType = "";

	public static final String CALLLOG_TABLE_NAME = "calllog";
	public static final Uri CONTENT_URI_CALLLOG = Uri.parse("content://"
			+ AUTHORITY + "/" + CALLLOG_TABLE_NAME);

	public interface CalllogDetail {

		public static final String ID = "_idCalllog";
		// user name in the message list
		public static final String ASSOICIATE_NAME = "associateName";
		public static final String NUMBER_TYPE = "numberType";
		public static final String DATE = "date";
		public static final String CALL_DURATION = "callDuration";
		public static final String CALL_TYPE_INT = "callTypeInt";
		public static final String NUMBER = "number";
		public static final String CALL_TYPE_STRING = "callTypeString";
		public static final String CALL_NO_TIMES_STRING = "numberOfTimesString";
		public static final String FOREIGN_KEY_STRING = "_id";
		// 0->Incoming---1->Outgoing----2->Missedcall
		public static final String CALL_STATE_INT = "callStateInt";

	}

	/*
	 * public static final String[] CONTENT_PROJECTION_MESSAGE_ID_KEY = new
	 * String[] {
	 * CalllogDetail.ID,CalllogDetail.ASSOICIATE_NAME,CalllogDetail.NUMBER_TYPE
	 * ,CalllogDetail.DATE };
	 */

	public static final String[] CONTENT_PROJECTION_MESSAGE_ID_KEY = new String[] {
			CONTACT_ID, CONTACT_PHOTO };
	public static final int CONTENT_PROJECTION_REC_ID = 0;
	public static final int CONTENT_PROJECTION_PHOTO = 1;

	public static final String[] CONTENT_PROJECTION_NATIVE_CALL_KEY = new String[] {
			CONTACT_ID, CONTACT_PHOTO, ContactsConsts.CONTACT_FIRST_NAME,ContactsConsts.CONTACT_PHONE };
	public static final int CONTENT_PROJECTION_NATIVE_CALL_REC_ID = 0;
	public static final int CONTENT_PROJECTION_NATIVE_CALL_PHOTO = 1;
	public static final int CONTENT_PROJECTION_NATIVE_CALL_NAME = 2;
	public static final int CONTENT_PROJECTION_NATIVE_CALL_PHONE = 3;

	public static final int REC_ID = 0;
	public static final int ASSOICIATE_NAME = 1;
	public static final int NUMBER_TYPE = 2;
	public static final int DATE = 3;
	public static final int CALL_DURATION = 4;
	public static final int CALL_TYPE_INT = 5;
	public static final int NUMBER = 6;
	public static final int CALL_TYPE_STRING = 7;
	public static final int CALL_NO_TIMES_STRING = 8;
	public static final int FOREIGN_KEY_STRING = 9;
	public static final int CALL_STATE_INT = 10;

	public static final String[] CONTENT_PROJECTION_FOR_CALL_LOG = new String[] {
			CONTACT_ID, CONTACT_FIRST_NAME };
	public static final int CONTENT_PROJECTION_CALLOG_REC_ID = 0;
	public static final int CONTENT_PROJECTION_CALLOG_FIRST_NAME = 1;

	// Sudarshan changes ends

}
