/**
 * 
 */
package com.cognizant.trumobi.persona;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import com.TruBoxSDK.TruBoxDatabase;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.log.PersonaLog;



/**
 * @author 290661
 *
 */
public class PersonaDatabaseProvider extends ContentProvider {

	private static final String AUTHORITY = "com.cognizant.trumobi.persona.routinecheck";
	private static final String BASE_PATH = "db";
	public  static final String PERSONA_EXTERNAL_DB_NAME = "testdb";
	private static final String ROUTINE_CHECK_TABLE = "table1";
	private static final int PERSONA_EXTERNAL_DB_VERSION = 1;  
	private static final String TAG = PersonaDatabaseProvider.class.getSimpleName();
	public static final int CODE = 5000;
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
		/*switch (uriMatcher.match(uri)) {
		case CODE:
			PersonaLog.i("db provider","gettype in case code");
		return "vnd.android.cursor.dir/vnd.com.cognizant.trumobi.persona.routinecheck.db";
		}*/
		return null;
		
	}

	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
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
		if(db != null && db.isOpen()) 
			return db;
		TruBoxDatabase.initialiseTruBoxDatabase(Email.getAppContext());
		PersonaExternalDBHelper personaExtDb = new PersonaExternalDBHelper(Email.getAppContext(),
				TruBoxDatabase.getHashValue(PERSONA_EXTERNAL_DB_NAME, context),null,PERSONA_EXTERNAL_DB_VERSION);
		db = personaExtDb.getTruboxStaticWritableDatabase();
		return db;
	}




	@Override
	public Cursor query(Uri uri, String[] projection, String where, String[] whereArgs,
			String sortorder) {
		db = getMyDatabase();
		Cursor c;
		c = db.query(ROUTINE_CHECK_TABLE, projection, where, whereArgs, null, null, sortorder);
		return c;
	}

	
	@Override
	public int update(Uri uri, ContentValues values, String where, String[] args) {
		db = getMyDatabase();
		db.update(ROUTINE_CHECK_TABLE, values, where, args);
		getContext().getContentResolver().notifyChange(uri, null);
		return 0;
	}

}
