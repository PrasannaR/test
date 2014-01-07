/**
 * 
 */
package com.cognizant.trumobi.persona;

import java.io.File;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;

import android.net.Uri;

import com.TruBoxSDK.TruBoxDatabase;
import com.cognizant.trumobi.log.PersonaLog;



/**
 * @author 290661
 *
 */
public class PersonaDatabaseProvider extends ContentProvider {

	private static final String AUTHORITY = "com.cognizant.trumobi.persona.routinecheck";
	private static final String BASE_PATH = "db";
	public static final String DB_NAME = "testdb";
	private static final String ROUTINE_CHECK_TABLE = "table1";
	public static final String COL1 = "COL_S";
	public static final String COL2 = "COL_SA";
	public static final String COL3 = "COL_AT";
	public static final String COL4 = "COL_NT";
	public static final String COL5 = "COL_PS";
	public static final String COL0 = "COL_ID";

	public static final int CODE = 5000;
	public static final String  ROUTINE_CHECK_TABLE_SCHEMA= "create table if not exists "
			+ ROUTINE_CHECK_TABLE + " (" + COL0 + " integer primary key autoincrement, "
			+ COL1 + " varchar," + COL2 + " varchar,"
			+ COL3 + " varchar," + COL4 + " integer," + COL5 + " varchar" +");";
	private Context context;
	private TruBoxDatabase db;
	public static final Uri ROUTINE_CHECK_CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/" + BASE_PATH);
	public PersonaDatabaseProvider() {
		// TODO Auto-generated constructor stub
	}

	private static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, BASE_PATH, CODE);
	}

	
	@Override
	public int delete(Uri uri, String arg1, String[] arg2) {
		getContext().getContentResolver().notifyChange(uri, null);
		return 0;
	}

	
	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		switch (uriMatcher.match(uri)) {
		case CODE:
			PersonaLog.i("db provider","gettype in case code");
		return "vnd.android.cursor.dir/vnd.com.cognizant.trumobi.persona.routinecheck.db";
		}
		return null;
		
	}

	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		db = getMyDatabase();
		PersonaLog.d("PersonaDatabaseProvider","insert"); 
		
		db.insert(ROUTINE_CHECK_TABLE,null,values);
		getContext().getContentResolver().notifyChange(uri, null);
		
		
		return uri;
	}

	
	@Override
	public boolean onCreate() {
		PersonaLog.i("persona db provider", "oncreate");
		context = getContext();
		return true;
	}

	
	private TruBoxDatabase getMyDatabase() {
		//SQLiteDatabase.loadLibs(context);
		 TruBoxDatabase.isPIMSettings(context);
		 String DB_NAME_HASHED = TruBoxDatabase.getHashValue(DB_NAME, context);
		 File databaseFile = context.getDatabasePath(DB_NAME_HASHED);
	//	 File databaseFile = context.getDatabasePath(DB_NAME);
		 PersonaLog.i("Db provider", databaseFile.toString());
		 if (!databaseFile.getParentFile().exists()) {
			
			 databaseFile.getParentFile().mkdirs();
			}
		 if(db!= null && db.isOpen())
			{
				return db;
			}
		else {
				//db = SQLiteDatabase.openOrCreateDatabase(databaseFile, "test", null);	
				 db = TruBoxDatabase.openOrCreateDatabase(databaseFile,null);
				if(!isTableExists(db,ROUTINE_CHECK_TABLE)) {
					createThisTable(db,ROUTINE_CHECK_TABLE_SCHEMA);
				}
		}
		return db;
	}


	private boolean isTableExists(TruBoxDatabase db, String routineCheckTable) {
		Cursor c = null;
	    boolean tableExists = false;

	    try
	    {
	        c = db.query(routineCheckTable, null,
	            null, null, null, null, null);
	        if(c != null)
	            tableExists = true;
	        else
	        	tableExists = false;
	    }
	    catch (Exception e) {
	       
	    }
	    if(c!=null)
	    c.close();
	    return tableExists;
		
	}


	private void createThisTable(TruBoxDatabase db,String schema) {
		
		db.execSQL(schema);
			
	}


	@Override
	public Cursor query(Uri arg0, String[] arg1, String arg2, String[] arg3,
			String arg4) {
		 db = getMyDatabase();
		Cursor c;
		 c = db.query(ROUTINE_CHECK_TABLE, null, null, null,null,null,null);
		return c;
	}

	
	@Override
	public int update(Uri uri, ContentValues values, String where, String[] arg3) {
		 db = getMyDatabase();
		db.update(ROUTINE_CHECK_TABLE, values, where, null);
		getContext().getContentResolver().notifyChange(uri, null);
		return 0;
	}

}
