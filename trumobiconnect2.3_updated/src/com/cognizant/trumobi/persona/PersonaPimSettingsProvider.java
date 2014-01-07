package com.cognizant.trumobi.persona;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import com.TruBoxSDK.TruBoxDatabase;
import com.cognizant.trumobi.PersonaPimSettingsDbHelper;

public class PersonaPimSettingsProvider extends ContentProvider {
												
	private static final String AUTHORITY = "com.cognizant.trumobi.persona.pimsettings";
	private static final String TABLE_USERDETAILS_PATH = "users";
	private static final String TABLE_PWDSETTINGS_PATH = "pwdsettings";
	private static final String TABLE_EMAIL_PATH = "email";
	private static final String TABLE_PWD_HISTORY_PATH = "pwdhistory";
	private static final int TABLE_USERDETAILS_MATCHER  = 1;
	private static final int TABLE_PWDSETTINGS_MATCHER = 2;
	private static final int TABLE_EMAIL_MATCHER = 3;
	private static final int TABLE_PWD_HISTORY_MATCHER = 4;
	private Context context;
	private TruBoxDatabase db;
	//private String TAG = PersonaPimSettingsProvider.class.getSimpleName();
	public static final Uri TABLE_USERDETAILS_URI 	= 	Uri.parse("content://" + AUTHORITY
			+ "/" + TABLE_USERDETAILS_PATH);
	public static final Uri TABLE_PWDSETTINGS_URI 	=	Uri.parse("content://" + AUTHORITY
			+ "/" + TABLE_PWDSETTINGS_PATH);
	public static final Uri TABLE_EMAIL_URI 		=	Uri.parse("content://" + AUTHORITY
			+ "/" + TABLE_EMAIL_PATH);
	public static final Uri TABLE_PWD_HISTORY_URI 	= 	Uri.parse("content://" + AUTHORITY
			+ "/" + TABLE_PWD_HISTORY_PATH);

	
	private static final UriMatcher uriMatcher;
	static {
	    uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	    uriMatcher.addURI(AUTHORITY, TABLE_USERDETAILS_PATH, TABLE_USERDETAILS_MATCHER);
	    uriMatcher.addURI(AUTHORITY, TABLE_PWDSETTINGS_PATH, TABLE_PWDSETTINGS_MATCHER);      
	    uriMatcher.addURI(AUTHORITY, TABLE_EMAIL_PATH, TABLE_EMAIL_MATCHER);
	    uriMatcher.addURI(AUTHORITY, TABLE_PWD_HISTORY_PATH, TABLE_PWD_HISTORY_MATCHER);      
	}
	
	public PersonaPimSettingsProvider() {
	}

	@Override
	public int delete(Uri uri, String arg1, String[] arg2) {
		
		return 0;
	}
	
	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Uri insert(Uri uri, ContentValues values) {
		db = getDatabase();
		switch (uriMatcher.match(uri)){
	
		case TABLE_USERDETAILS_MATCHER:
			db.insert(PersonaPimSettingsDbHelper.TABLE_USERDETAILS,null,values);
			break;
		case TABLE_PWDSETTINGS_MATCHER:
			db.insert(PersonaPimSettingsDbHelper.TABLE_PWDSETTINGS,null,values);
			break;
		case TABLE_EMAIL_MATCHER:
			db.insert(PersonaPimSettingsDbHelper.TABLE_EMAIL,null,values);
			break;
		case TABLE_PWD_HISTORY_MATCHER:
			db.insert(PersonaPimSettingsDbHelper.PERSONA_PASSWORD_HISTORY_TABLE,null,values);
			break;
		
		}
		 getContext().getContentResolver().notifyChange(uri, null); 
		return uri;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionargs,
			String sortorder) {
			db = getDatabase();
			Cursor c = null;
			switch (uriMatcher.match(uri)){
			
			case TABLE_USERDETAILS_MATCHER:
				c = db.query(PersonaPimSettingsDbHelper.TABLE_USERDETAILS, projection, selection, selectionargs, null, null, sortorder);
				break;
			case TABLE_PWDSETTINGS_MATCHER:
				c = db.query(PersonaPimSettingsDbHelper.TABLE_PWDSETTINGS, projection, selection, selectionargs, null, null, sortorder);
				break;
			case TABLE_EMAIL_MATCHER:
				c = db.query(PersonaPimSettingsDbHelper.TABLE_EMAIL, projection, selection, selectionargs, null, null, sortorder);
				break;
			case TABLE_PWD_HISTORY_MATCHER:
				c = db.query(PersonaPimSettingsDbHelper.PERSONA_PASSWORD_HISTORY_TABLE, projection, selection, selectionargs, null, null, sortorder);
				break;
			
			}
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionargs) {
		db = getDatabase();
		switch (uriMatcher.match(uri)){
		
		case TABLE_USERDETAILS_MATCHER:
			db.update(PersonaPimSettingsDbHelper.TABLE_USERDETAILS, values, selection, selectionargs);
			break;
		case TABLE_PWDSETTINGS_MATCHER:
			db.update(PersonaPimSettingsDbHelper.TABLE_PWDSETTINGS, values, selection, selectionargs);
			break;
		case TABLE_EMAIL_MATCHER:
			db.update(PersonaPimSettingsDbHelper.TABLE_EMAIL, values, selection, selectionargs);
			break;
		case TABLE_PWD_HISTORY_MATCHER:
			db.update(PersonaPimSettingsDbHelper.PERSONA_PASSWORD_HISTORY_TABLE, values, selection, selectionargs);
			break;
		
		}
		getContext().getContentResolver().notifyChange(uri, null); 
		return 0;
	}

	@Override
	public boolean onCreate() {
		context = getContext();
		return false;
	}

	
	private TruBoxDatabase getDatabase() {
		
		if(db != null && db.isOpen()) 
			return db;
		PersonaPimSettingsDbHelper pimSettingsDbHelper = new PersonaPimSettingsDbHelper(context);
		db = pimSettingsDbHelper.getTruboxDynamicWritableDatabase(context);
		return db;
	}
}
