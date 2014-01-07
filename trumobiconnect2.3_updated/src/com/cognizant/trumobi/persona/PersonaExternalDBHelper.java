/**
 * 
 */
package com.cognizant.trumobi.persona;

import net.sqlcipher.database.SQLiteDatabase.CursorFactory;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.TruBoxSDK.TruBoxDatabase;
import com.TruBoxSDK.TruBoxOpenHelper;

/**
 * @author 290661
 *
 */
public class PersonaExternalDBHelper extends TruBoxOpenHelper {

		
	private static final String ROUTINE_CHECK_TABLE = "table1";
	public static final String COL1 = "COL_S";
	public static final String COL2 = "COL_SA";
	public static final String COL3 = "COL_AT";
	public static final String COL4 = "COL_NT";
	public static final String COL5 = "COL_PS";
	public static final String COL6 = "COL_PE";
	public static final String COL0 = "COL_ID";
	public static final String  ROUTINE_CHECK_TABLE_SCHEMA= "create table if not exists "
			+ ROUTINE_CHECK_TABLE + " (" + COL0 + " integer primary key autoincrement, "
			+ COL1 + " varchar," + COL2 + " varchar,"
			+ COL3 + " varchar," + COL4 + " integer," + COL5 + " varchar," + COL6 + " integer" + ");";
	

	
	public PersonaExternalDBHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, null, version);
	}

	

	@Override
	public void onCreate(TruBoxDatabase db) {
	
			db.execSQL(ROUTINE_CHECK_TABLE_SCHEMA);
		
	}

	
	@Override
	public void onUpgrade(TruBoxDatabase arg0, int arg1, int arg2) {
		

	}

	
	
	




	


}
