package com.cognizant.trumobi;

import java.io.File;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.TruBoxSDK.TruBoxDatabase;
import com.cognizant.trumobi.log.PersonaLog;
//ENCRYPT_DB CHG
public final class PersonaDbEncryptHelper {
	
	private static String TAG = "PersonaDbEncryptHelper";
	
	private static final int DB_VERSION = 1;
//	private static final String DB_NAME = "Email_data";
	public static final String DB_NAME = "PersonaDb";
	public static final String TABLE_EMAIL = "email";
	public static final String ID = "_id";
	public static final String COL_Email_id = "email_id";
	public static final String COL_Domain = "domain";
	public static final String COL_Server = "server";
	public static final String COL_Password = "password";
	public static final String COL_File = "file";
	public static final String COL_URL = "url";
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
	public static final String LAST_PASSWORD = "last_password";
	public static final String OLDER_PASSWORD = "older_password";
	public static final String OLDEST_PASSWORD = "oldest_password";
	public static final String CREATED_DATE = "created_date";
	
	
	public static final String TABLE_USERDETAILS = "userdetails";
	public static final String USER_NAME = "user_name";
	public static final String PASSWORD = "password";
	
	public static final String CREATE_TABLE_USERDETAILS = "create table if not exists "
			+ TABLE_USERDETAILS + " (" + USER_NAME + " varchar primary key," 
			+ PASSWORD + " varchar not null);";
			
	
	public static final String CREATE_TABLE_PWDSETTINGS = "create table if not exists "
			+ TABLE_PWDSETTINGS + " (" + USER_ID + " integer primary key autoincrement," 
			+ USERNAME_PWDSETTINGS + " varchar not null,"
			+ PWD_TYPE + " integer not null,"			
			+ PWD_EXPIRY_DURATION + " integer not null," 
			+ PWD_LOCK_TIME + " integer not null,"
			+ PWD_LENGTH + " integer not null," 
			+ PWD_NUM_TRIAL + " integer not null,"
			+ PWD_CHANGE_HISTORY+ " integer not null,"
			+ LAST_PASSWORD + " varchar,"
			+ OLDER_PASSWORD + " varchar,"
			+ OLDEST_PASSWORD + " varchar,"
			+ CREATED_DATE + " long,"
			+"FOREIGN KEY(" +USERNAME_PWDSETTINGS+") REFERENCES "+TABLE_USERDETAILS+"("+USER_NAME+"));";
	

	private static final String DB_SCHEMA = CREATE_TABLE_EMAIL;
	
	private Context mContext;
	
	private TruBoxDatabase mPersonaEncryptDb;
	
	
	
	public PersonaDbEncryptHelper(Context context){
	
		mContext = context;
		
		// Initialize Trubox database
		TruBoxDatabase.initialiseTruBoxDatabase(mContext);	
		
		try{
				// Create a persona db file with encryption on DB_NAME
			String DB_NAME_HASHED = TruBoxDatabase.getHashValue(DB_NAME, context) ;
			File _personaDbfile = mContext.getDatabasePath(DB_NAME_HASHED);
			
		//	File _personaDbfile = mContext.getDatabasePath(DB_NAME);
				
				if (!_personaDbfile.getParentFile().exists()) {
					_personaDbfile.getParentFile().mkdirs();
				}
				
				mPersonaEncryptDb = TruBoxDatabase.openOrCreateDatabase(_personaDbfile, null);
				
				if(mPersonaEncryptDb != null) {
					if(!isTableExists(mPersonaEncryptDb, TABLE_EMAIL)) {
						onCreate(mPersonaEncryptDb);
						PersonaLog.d("TABLE_EMAIL", "created");
					}
					
					if(!isTableExists(mPersonaEncryptDb, TABLE_USERDETAILS)) {
						
						mPersonaEncryptDb.execSQL(CREATE_TABLE_USERDETAILS);
						PersonaLog.d("TABLE_USERDETAILS", "created");
					}
					
					if(!isTableExists(mPersonaEncryptDb, TABLE_PWDSETTINGS)) {
						mPersonaEncryptDb.execSQL(CREATE_TABLE_PWDSETTINGS);
						PersonaLog.d("TABLE_PWDSETTINGS", "created");
					}
					
					
					
					
				}	
				
				
				/*PersonaLog.d("PersonaLocalAuthentication", "Table in DB  "+"check");
				Cursor c = mPersonaEncryptDb.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
				if(c.moveToFirst()) {
					while(!c.isAfterLast()){
					PersonaLog.d("PersonaLocalAuthentication", "Table in DB  "+c.getString(0));
					c.moveToNext();
					}
				}
				c.close();*/
			}catch(Exception E){
				
				PersonaLog.d(TAG,"===== problem creating encrypted file ");
			}
	}
	
	
	public void onCreate(TruBoxDatabase db) {
	
		PersonaLog.d(TAG,"In onCreate");
		db.execSQL(DB_SCHEMA);
	}

	public void onUpgrade(TruBoxDatabase db, int oldVersion, int newVersion) {
	
	}
	
	public void onOpen(TruBoxDatabase db) {
		  
    }

	public TruBoxDatabase getWritableDatabase() {
			// TODO Auto-generated method stub
			return  mPersonaEncryptDb;
			
	}
	
	
	
	
	private boolean isTableExists(TruBoxDatabase mEncryptDb,String sTableName)
	{
		Cursor c = null;
	    boolean tableExists = false;

	    try
	    {
	        c = mEncryptDb.query(sTableName, null,
	            null, null, null, null, null);
	        if(c != null)
	            tableExists = true;
	        else
	        	tableExists = false;
	    }
	    catch (Exception e) {
	        /* fail */
	        PersonaLog.d(TAG, sTableName+" doesn't exist :(((");
	    }

	    return tableExists;
	   
	}

}
