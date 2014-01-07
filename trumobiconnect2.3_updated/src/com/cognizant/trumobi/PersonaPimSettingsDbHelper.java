package com.cognizant.trumobi;

import android.content.Context;

import com.TruBoxSDK.TruBoxDatabase;
import com.TruBoxSDK.TruBoxOpenHelper;
import com.cognizant.trumobi.log.PersonaLog;

public final class PersonaPimSettingsDbHelper extends TruBoxOpenHelper {
	

	private static final int DB_VERSION = 1;
	public static final String DB_NAME = "PersonaDb";
	public static final String TABLE_EMAIL = "email";
	public static final String ID = "_id";
	public static final String COL_Email_id = "email_id";
	public static final String COL_Domain = "domain";
	public static final String COL_Server = "server";
	public static final String COL_Password = "password";
	public static final String COL_File = "file";
	public static final String COL_URL = "url";
	private static final String TAG = PersonaPimSettingsDbHelper.class.getSimpleName();
	private static final String CREATE_TABLE_EMAIL = "create table "
			+ TABLE_EMAIL + " (" + ID + " integer primary key autoincrement,"
			+ COL_Email_id + " text not null," + COL_File + " BLOB not null,"
			+ COL_Domain + " text not null," + COL_Server + " text not null,"
			+ COL_Password + " text not null);";
	
	public static final String TABLE_PWDSETTINGS = "pwdsettings";
	public static final String USERNAME_PWDSETTINGS = "username_pwdsettings";
	public static final String USER_ID = "user_id";
	public static final String PWD_TYPE = "pwd_type";
	public static final String PWD_EXPIRY_DURATION= "pwd_expiry_duration";
	public static final String PWD_LOCK_TIME = "pwd_lock_time";
	public static final String PWD_LENGTH = "pwd_length";
	public static final String PWD_NUM_TRIAL = "pwd_num_trial";
	public static final String PWD_CHANGE_HISTORY = "pwd_change_history";
	public static final String CREATED_DATE = "created_date";
	public static final String UPDATED_AUTH_TYPE = "updated_auth_type";
	public static final String UPDATED_AUTH_LENGTH = "updated_auth_length";
	
	
	public static final String TABLE_USERDETAILS = "userdetails";
	public static final String USER_NAME = "user_name";
	
	public static final String CREATE_TABLE_USERDETAILS = "create table if not exists "
			+ TABLE_USERDETAILS + " (" + USER_NAME + " varchar primary key" + ");";
			
	
	public static final String CREATE_TABLE_PWDSETTINGS = "create table if not exists "
			+ TABLE_PWDSETTINGS + " (" + USER_ID + " integer primary key autoincrement," 
			+ USERNAME_PWDSETTINGS + " varchar not null,"
			+ PWD_TYPE + " integer not null,"			
			+ PWD_EXPIRY_DURATION + " integer not null," 
			+ PWD_LOCK_TIME + " integer not null,"
			+ PWD_LENGTH + " integer not null," 
			+ PWD_NUM_TRIAL + " integer not null,"
			+ PWD_CHANGE_HISTORY+ " integer not null,"
			+ CREATED_DATE + " long,"
			+ UPDATED_AUTH_TYPE + " integer,"	
			+ UPDATED_AUTH_LENGTH + " integer," 
			+"FOREIGN KEY(" +USERNAME_PWDSETTINGS+") REFERENCES "+TABLE_USERDETAILS+"("+USER_NAME+"));";
	

	public static final String PERSONA_PASSWORD_HISTORY_TABLE = "pwd_history";
	private static final String ROW_ID = "user_id";
	public static final String USERNAME_PWD_HISTORY = "username";
	public static final String PASSWORD = "password";
	public static final String PASSWORD_CREATED_DATE = "pwd_created_date";
	private static final String CREATE_PASSWORD_HISTORY_TABLE = "create table if not exists "
			+ PERSONA_PASSWORD_HISTORY_TABLE + " (" + ROW_ID + " integer primary key autoincrement," 
			+ USERNAME_PWD_HISTORY + " varchar not null,"
			+ PASSWORD + " varchar not null,"		
			+ PASSWORD_CREATED_DATE + " long not null,"
			+"FOREIGN KEY(" +USERNAME_PWD_HISTORY+") REFERENCES "+TABLE_USERDETAILS+"("+USER_NAME+"));";
	

	
	
	
	public PersonaPimSettingsDbHelper(Context context){
	
		super(context, TruBoxDatabase.getHashValue(DB_NAME,context), null, DB_VERSION);
	}
	
	@Override
	public void onCreate(TruBoxDatabase db) {
		db.execSQL(CREATE_TABLE_USERDETAILS);
		db.execSQL(CREATE_TABLE_PWDSETTINGS);
		db.execSQL(CREATE_TABLE_EMAIL);
		db.execSQL(CREATE_PASSWORD_HISTORY_TABLE);
	}
	
	

	public void onUpgrade(TruBoxDatabase db, int oldVersion, int newVersion) {
	
	}
	
	

}
