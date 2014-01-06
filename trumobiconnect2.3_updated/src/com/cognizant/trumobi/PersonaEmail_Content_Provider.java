package com.cognizant.trumobi;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;


import com.TruBoxSDK.TruBoxDatabase;
import com.cognizant.trumobi.log.PersonaLog;


public class PersonaEmail_Content_Provider extends ContentProvider {
	private static final int CORPORATE_EMAIL = 1;
	private static final int PERSONAL_EMAIL = 2;

	//trumobiedits
	
	//private PersonaEmail_Database mEmail_db;

	private static final String AUTHORITY = "com.cognizant.trumobi.persona.launcher.emailcontentprovider";
	public static final int TUTORIALS = 100;
	public static final int TUTORIAL_ID = 110;
	private static final String EMAIL_BASE_PATH = "email";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + EMAIL_BASE_PATH);
	
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/mt-email";
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/mt-email";

	private static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, EMAIL_BASE_PATH, CORPORATE_EMAIL);
	}

	//trumobiedits
	
	private TruBoxDatabase mPersonaEncryptDb;
	
	// Return the MIME type corresponding to a content URI
	@Override
	public String getType(Uri uri) {

		switch (uriMatcher.match(uri)) {
		case CORPORATE_EMAIL:
			return "vnd.android.cursor.dir/vnd.com.cognizant.trumobi.persona.launcher.emailcontentprovider.email";
		case PERSONAL_EMAIL:
			return "vnd.android.cursor.dir/vnd.com.cognizant.trumobi.persona.launcher.emailcontentprovider.email";
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		
		//trumobiedits
		
		/*SQLiteDatabase sqlDB = mEmail_db.getWritableDatabase();

		long id = 0;
		switch (uriMatcher.match(uri)) {
		case CORPORATE_EMAIL:
			id = sqlDB.insert(PersonaEmail_Database.TABLE_EMAIL, null, values);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse(EMAIL_BASE_PATH + "/" + id);*/
		
		
		long id = 0;
		switch (uriMatcher.match(uri)) {
		case CORPORATE_EMAIL:
			
			id = mPersonaEncryptDb.insert(PersonaDbEncryptHelper.TABLE_EMAIL, null, values);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse(EMAIL_BASE_PATH + "/" + id);
		
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		//trumobiedits
		//mEmail_db = new PersonaEmail_Database(getContext());
		//mPersonaEncryptDb = getEncryptedDatabaseInstance();
		
		
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		
		
		//trumobiedits starts here

		/*SQLiteDatabase db = mEmail_db.getReadableDatabase();
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setTables(PersonaEmail_Database.TABLE_EMAIL);
		PersonaLog.d("PersonaEmail_Content_Provider", "" + uriMatcher.match(uri));
		switch (uriMatcher.match(uri)) {
		case CORPORATE_EMAIL:
			PersonaLog.d("PersonaEmail_Content_Provider", "Inside Corprate");

			
			break;
		case PERSONAL_EMAIL:
			// no filter
			PersonaLog.d("PersonaEmail_Content_Provider", "Inside Personal");
			break;
		default:
			throw new IllegalArgumentException("Unknown URI");
		}
		Cursor cursor = queryBuilder.query(db, projection, selection,
				selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);*/
		
		//trumobiedits end here
		PersonaLog.d("Email_Content_Provider", "" + uriMatcher.match(uri));
		switch (uriMatcher.match(uri)) {
		case CORPORATE_EMAIL:
			PersonaLog.d("Email_Content_Provider", "Inside Corprate");

			/*
			 * queryBuilder.appendWhere(Email_Database.ID + "=" + " 1");
			 */
			break;
		case PERSONAL_EMAIL:
			// no filter
			PersonaLog.d("Email_Content_Provider", "Inside Personal");
			break;
		default:
			throw new IllegalArgumentException("Unknown URI");
		}
		
		mPersonaEncryptDb = getEncryptedDatabaseInstance();
		Cursor cursor = mPersonaEncryptDb.query(PersonaDbEncryptHelper.TABLE_EMAIL, projection, selection,
				selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
	
		
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selections,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		
		//trumobiedits
		
		/*SQLiteDatabase db = mEmail_db.getWritableDatabase();
		int rowsUpdated = 0;
		switch (uriMatcher.match(uri)) {
		case CORPORATE_EMAIL:
			PersonaLog.d("PersonaEmail_Content_Provider", "Inside Corprate");
			break;
		case PERSONAL_EMAIL:
			PersonaLog.d("PersonaEmail_Content_Provider", "Inside Personal");
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		rowsUpdated = db.update(PersonaEmail_Database.TABLE_EMAIL, values, selections,
				selectionArgs);

		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;*/
		
		
		int rowsUpdated = 0;
		switch (uriMatcher.match(uri)) {
		case CORPORATE_EMAIL:
			PersonaLog.d("Email_Content_Provider", "Inside Corprate");
			break;
		case PERSONAL_EMAIL:
			PersonaLog.d("Email_Content_Provider", "Inside Personal");
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		
		
		rowsUpdated = mPersonaEncryptDb.update(PersonaDbEncryptHelper.TABLE_EMAIL, values, selections,
				selectionArgs);

		getContext().getContentResolver().notifyChange(uri, null);
		return rowsUpdated;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		
		//trumobiedits 
		
		/*SQLiteDatabase db = mEmail_db.getWritableDatabase();
		switch (uriMatcher.match(uri)) {
		case CORPORATE_EMAIL:
			// do nothing
			break;
		case PERSONAL_EMAIL:
			// do nothing
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		int deleteCount = db.delete(PersonaEmail_Database.TABLE_EMAIL, selection,
				selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return deleteCount;*/
		

		switch (uriMatcher.match(uri)) {
		case CORPORATE_EMAIL:
			// do nothing
			break;
		case PERSONAL_EMAIL:
			// do nothing
			break;
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
		
		int deleteCount = mPersonaEncryptDb.delete(PersonaDbEncryptHelper.TABLE_EMAIL, selection,
				selectionArgs);

		
		getContext().getContentResolver().notifyChange(uri, null);
		return deleteCount;
		
		
	}

	/*
	 * private static final UriMatcher sURIMatcher = new UriMatcher(
	 * UriMatcher.NO_MATCH); static { sURIMatcher.addURI(AUTHORITY,
	 * EMAIL_BASE_PATH, TUTORIALS); sURIMatcher.addURI(AUTHORITY,
	 * EMAIL_BASE_PATH + "/#", TUTORIAL_ID); }
	 */
	
	
	/* 
	 * Method to retrieve encrypted database instance
	 * 
	 */
	private TruBoxDatabase getEncryptedDatabaseInstance(){
		
		// Added new Encryption for Persona db
				PersonaDbEncryptHelper personDbHelper = new PersonaDbEncryptHelper(getContext());
				
				TruBoxDatabase localTruBoxdb = personDbHelper.getWritableDatabase();
			
				return localTruBoxdb;
	}

}
