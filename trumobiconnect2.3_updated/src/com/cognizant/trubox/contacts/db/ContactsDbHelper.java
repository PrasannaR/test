package com.cognizant.trubox.contacts.db;

import com.cognizant.trubox.contacts.db.ContactsConsts.CalllogDetail;

public class ContactsDbHelper /* extends SQLiteOpenHelper */{

	public static final String CREATE_CONTACT_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ ContactsConsts.CONTACTS_TABLE_NAME
			+ " ("
			+ ContactsConsts.CONTACT_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ ContactsConsts.CONTACT_SERVER_ID
			+ " VARCHAR(255),"
			+ ContactsConsts.CONTACT_TITLE
			+ " VARCHAR(255),"
			+ ContactsConsts.CONTACT_FIRST_NAME
			+ " VARCHAR(255),"
			+ ContactsConsts.CONTACT_MIDDLE_NAME
			+ " VARCHAR(255),"
			+ ContactsConsts.CONTACT_LAST_NAME
			+ " VARCHAR(255),"
			+ ContactsConsts.CONTACT_SUFFIX
			+ " VARCHAR(255),"
			+ ContactsConsts.CONTACT_EMAIL
			+ " VARCHAR(255),"
			+ ContactsConsts.CONTACT_WEB_ADRESS
			+ " VARCHAR(255),"
			+ ContactsConsts.CONTACT_IM_NAME
			+ " VARCHAR(255),"
			+ ContactsConsts.CONTACT_PHONE
			+ " VARCHAR(255),"
			+ ContactsConsts.CONTACT_BUSS_ADRESS
			+ " INTEGER,"
			+ ContactsConsts.CONTACT_HOME_ADRESS
			+ " INTEGER,"
			+ ContactsConsts.CONTACT_OTHER_ADRESS
			+ " INTEGER,"
			+ ContactsConsts.CONTACT_OFFICE
			+ " VARCHAR(255),"
			+ ContactsConsts.CONTACT_DEPARTMENT
			+ " VARCHAR(255),"
			+ ContactsConsts.CONTACT_COMPANY
			+ " VARCHAR(255),"
			+ ContactsConsts.CONTACT_MANAGER
			+ " VARCHAR(255),"
			+ ContactsConsts.CONTACT_ASSISTANT
			+ " VARCHAR(255),"
			+ ContactsConsts.CONTACT_NOTES
			+ " VARCHAR(255),"
			+ ContactsConsts.CONTACT_PHOTO
			+ " BLOB,"
			+ ContactsConsts.CONTACT_JOB_TITLE
			+ " VARCHAR(255),"
			+ ContactsConsts.CONTACT_ANNIVERSARY
			+ " VARCHAR(255),"
			+ ContactsConsts.CONTACT_SPOUSE
			+ " VARCHAR(255),"
			+ ContactsConsts.CONTACT_NICK_NAME
			+ " VARCHAR(255),"
			+ ContactsConsts.CONTACT_YOMIFIRSTNAME
			+ " VARCHAR(255),"
			+ ContactsConsts.CONTACT_YOMILASTNAME
			+ " VARCHAR(255),"
			+ ContactsConsts.CONTACT_YOMICOMPANYNAME
			+ " VARCHAR(255),"
			+ ContactsConsts.CONTACT_BIRTH_DAY
			+ " VARCHAR(255),"
			+ ContactsConsts.CONTACT_PHONETIC_FAMILY_NAME
			+ " VARCHAR(255),"
			+ ContactsConsts.CONTACT_PHONETIC_MIDDLE_NAME
			+ " VARCHAR(255),"
			+ ContactsConsts.CONTACT_PHONETIC_GIVEN_NAME
			+ " VARCHAR(255),"
			+ ContactsConsts.CONTACT_CLIENT_ID
			+ " VARCHAR(255),"
			+ ContactsConsts.CONTACT_INTERNETCALL
			+ " VARCHAR(255),"
			+ ContactsConsts.CONTACT_RINGTONE_PATH
			+ " VARCHAR(255),"
			+ ContactsConsts.CONTACT_IS_FAVORITE + " INTEGER " + ");";

	// public static final String CREATE_CONTACT_TABLE =
	// "CREATE TABLE IF NOT EXISTS "
	// + ContactsConsts.CONTACTS_TABLE_NAME
	// + " ("
	// + ContactsConsts.CONTACT_ID
	// + " INTEGER PRIMARY KEY AUTOINCREMENT,"
	// + ContactsConsts.CONTACT_FIRST_NAME
	// + " VARCHAR(255),"
	// + ContactsConsts.CONTACT_SERVER_ID
	// + " VARCHAR(255),"
	// + ContactsConsts.CONTACT_CLIENT_ID
	// + " VARCHAR(255),"
	// + ContactsConsts.CONTACT_PHOTO
	// + " BLOB,"
	// + ContactsConsts.CONTACT_IS_FAVORITE
	// + " INTEGER,"
	// + ContactsConsts.CONTACT_MIDDLE_NAME
	// + " VARCHAR(255),"
	// + ContactsConsts.CONTACT_LAST_NAME
	// + " VARCHAR(255),"
	// + ContactsConsts.CONTACT_BIRTH_DAY
	// + " VARCHAR(255),"
	// // + ContactsConsts.CONTACT_PH_NO_MOBILE
	// // + " VARCHAR(255),"
	// + ContactsConsts.CONTACT_IM_NAME
	// + " VARCHAR(255),"
	// + ContactsConsts.CONTACT_NOTES
	// + " TEXT,"
	// // + ContactsConsts.CONTACT_PH_NO_RES
	// // + " VARCHAR(255),"
	// + ContactsConsts.CONTACT_DEPARTMENT
	// + " VARCHAR(255)"
	// // + ContactsConsts.CONTACT_BUSINESS_LOCATION
	// // + " VARCHAR(255),"
	// // + ContactsConsts.CONTACT_BUSINESS_DESIGNATION
	// // + " VARCHAR(255),"
	// // + ContactsConsts.CONTACT_BUSINESS_ORGN
	// // + " VARCHAR(255),"
	// // + ContactsConsts.CONTACT_BUSINESS_CITY
	// // + " VARCHAR(255),"
	// // + ContactsConsts.CONTACT_BUSINESS_STATE
	// // + " VARCHAR(255),"
	// // + ContactsConsts.CONTACT_BUSINESS_PINCODE
	// // + " VARCHAR(255),"
	// // + ContactsConsts.CONTACT_PH_NO_BUSINESS
	// // + " VARCHAR(255),"
	// // + ContactsConsts.CONTACT_PRIM_EMAIL
	// // + " VARCHAR(255),"
	// // + ContactsConsts.CONTACT_SEC_EMAIL + " VARCHAR(255)"
	// + ");";

	public static final String CREATE_ADRESS_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ ContactsConsts.CONTACTS_ADRESS_TABLE_NAME
			+ " ("
			+ ContactsConsts.ADRESS_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ ContactsConsts.ADRESS_UNIQUE_ID
			+ " VARCHAR(255),"
			+ ContactsConsts.ADRESS_CLIENT_ID
			+ " VARCHAR(255),"
			+ ContactsConsts.STREET
			+ " VARCHAR(255),"
			+ ContactsConsts.CITY
			+ " VARCHAR(255),"
			+ ContactsConsts.STATE
			+ " VARCHAR(255),"
			+ ContactsConsts.ZIP
			+ " VARCHAR(255),"
			+ ContactsConsts.COUNTRY
			+ " VARCHAR(255)" + ");";

	public static String CREATE_SYNCSTATE_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ ContactsConsts.CONTACTS_SYNCSTATE_TABLE_NAME + " ("
			+ ContactsConsts.SYNC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ ContactsConsts.ACCOUNT_NAME + " VARCHAR(255),"
			+ ContactsConsts.ACCOUNT_ID + " VARCHAR(255),"
			+ ContactsConsts.SYNC_KEY + " VARCHAR(255)" + ");";

	public static final String CREATE_GROUPS_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ ContactsConsts.GROUPS_TABLE_NAME
			+ " ("
			+ ContactsConsts.GROUP_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ ContactsConsts.GROUP_NAME
			+ " VARCHAR(255)" + ");";

	public static final String CREATE_RECENTS_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ ContactsConsts.RECENTS_TABLE_NAME
			+ " ("
			+ ContactsConsts.RECENT_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ ContactsConsts.RECENT_NAME + " VARCHAR(255)" + ");";

	// Local cache tables for syncing with the server.... Like a pending
	// list.....

	public static final String CONTACTS_LOCAL_CACHE_TABLE_ADD = "CREATE TABLE IF NOT EXISTS "
			+ ContactsConsts.CONTACTS_LOCAL_CACHE_ADD_TABLE_NAME
			+ " ("
			+ ContactsConsts.CONTACT_CLIENT_ID + ");";

	public static final String CONTACTS_LOCAL_CACHE_TABLE_UPDATE = "CREATE TABLE IF NOT EXISTS "
			+ ContactsConsts.CONTACTS_LOCAL_CACHE_UPDATE_TABLE_NAME
			+ " ("
			+ ContactsConsts.CONTACT_SERVER_ID
			+ " VARCHAR(255),"
			+ ContactsConsts.CONTACTS_SYNC_STATE + " VARCHAR(255)" + ");";

	public static final String CONTACTS_LOCAL_CACHE_TABLE_DELETE = "CREATE TABLE IF NOT EXISTS "
			+ ContactsConsts.CONTACTS_LOCAL_CACHE_DELETE_TABLE_NAME
			+ " ("
			+ ContactsConsts.CONTACT_SERVER_ID
			+ " VARCHAR(255),"
			+ ContactsConsts.CONTACTS_SYNC_STATE + " VARCHAR(255)" + ");";
	//
	// + " VARCHAR(255)"
	// + ContactsConsts.SYNC_STATE + " VARCHAR(255)"

	// public ContactsDbHelper(Context context, String name, CursorFactory
	// factory, int version) {
	// super(context, name, factory, version);
	// }

	// @Override
	// public void onCreate(SQLiteDatabase db) {
	// db.execSQL(CREATE_CONTACT_TABLE);
	// db.execSQL(CREATE_GROUPS_TABLE);
	// db.execSQL(CREATE_RECENTS_TABLE);
	// db.execSQL(CONTACTS_LOCAL_CACHE_TABLE_ADD);
	// db.execSQL(CONTACTS_LOCAL_CACHE_TABLE_UPDATE);
	// db.execSQL(CONTACTS_LOCAL_CACHE_TABLE_DELETE);
	// }

	// @Override
	// public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	// {
	// db.execSQL("DROP TABLE IF EXISTS " + ContactsConsts.CONTACTS_TABLE_NAME);
	// db.execSQL("DROP TABLE IF EXISTS " + ContactsConsts.GROUPS_TABLE_NAME);
	// db.execSQL("DROP TABLE IF EXISTS " + ContactsConsts.RECENTS_TABLE_NAME);
	// db.execSQL("DROP TABLE IF EXISTS " +
	// ContactsConsts.CONTACTS_LOCAL_CACHE_ADD_TABLE_NAME);
	// db.execSQL("DROP TABLE IF EXISTS " +
	// ContactsConsts.CONTACTS_LOCAL_CACHE_UPDATE_TABLE_NAME);
	// db.execSQL("DROP TABLE IF EXISTS " +
	// ContactsConsts.CONTACTS_LOCAL_CACHE_DELETE_TABLE_NAME);
	// onCreate(db);
	// }

	// Sudarshan changes
	public static final String PHONE_CALLLOG_TABLE = "CREATE TABLE IF NOT EXISTS "
			+ ContactsConsts.CALLLOG_TABLE_NAME
			+ " ("
			+ CalllogDetail.ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ CalllogDetail.ASSOICIATE_NAME
			+ " text, "
			+ CalllogDetail.NUMBER_TYPE
			+ " text, "
			+ CalllogDetail.DATE
			+ " text, "
			+ CalllogDetail.CALL_DURATION
			+ " text, "
			+ CalllogDetail.CALL_TYPE_INT
			+ " INTEGER, "
			+ CalllogDetail.NUMBER
			+ " text, "
			+ CalllogDetail.CALL_TYPE_STRING
			+ " text, "
			+ CalllogDetail.CALL_NO_TIMES_STRING
			+ " text, "
			+ CalllogDetail.FOREIGN_KEY_STRING
			+ " text, "
			+ CalllogDetail.CALL_STATE_INT + " INTEGER" + ");";

	// Sudarshan changes ends

}
