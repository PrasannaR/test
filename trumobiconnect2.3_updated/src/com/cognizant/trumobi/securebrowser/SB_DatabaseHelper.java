package com.cognizant.trumobi.securebrowser;

import java.io.File;

import android.content.Context;

import com.TruBoxSDK.TruBoxDatabase;
import com.cognizant.trumobi.securebrowser.SB_BrowserDBAdapter;

public final class SB_DatabaseHelper {
	public static class DatabaseHelper {
		TruBoxDatabase dbEncryption;
		Context mContext;
		public static boolean initTruboxEncryption=false;
		  public DatabaseHelper(Context context, String name) {
			// super(context, name, null, DATABASE_VERSION);

			mContext = context;
			/*if (initTruboxEncryption == false) {
				synchronized(mContext)
	    		{
					TruBoxDatabase.initialiseTruBoxDatabase(mContext);
	    		}
				initTruboxEncryption = true;
			}
			TruBoxDatabase.setPimSettings(false);*/
			try {
				File dbFile = mContext.getDatabasePath(name);
				///File dbFile = new File(
						//Environment.getExternalStorageDirectory() + "/" + name);
				SB_Log.sbE("EEE",dbFile.getParentFile().getAbsolutePath()+"");
				if (!dbFile.getParentFile().exists()) {
					dbFile.getParentFile().mkdirs();
				}
				if(dbEncryption == null){
					dbEncryption = TruBoxDatabase.openOrCreateDatabase(dbFile, null);
				}

				if (dbEncryption != null) {
					if (!isTableExists(dbEncryption, SB_BrowserDBAdapter.MASTER_TABLE)) {
						onCreate(dbEncryption);
					}
				}

			} catch (Exception e) {
				SB_Log.sbE("Failed to open", "Failed to open file: " + e);
			}

		}
        void createTablesForSecureBrowsers(TruBoxDatabase db)
        {
        	db.execSQL(SB_BrowserDBAdapter.CREATE_MASTER_TABLE);
			db.execSQL(SB_BrowserDBAdapter.CREATE_URLLIST_TABLE);
			
		SB_Log.sbE("CREATED", "CREATED all tables");
			
        }
		public void onCreate(TruBoxDatabase db) {
			SB_Log.sbE("NEW", "Creating Browser database");
			// Create all tables here; each class has its own method
			createTablesForSecureBrowsers(db);
	}

		

		public void onOpen(TruBoxDatabase db) {
		}

		public TruBoxDatabase getWritableDatabase() {
			// TODO Auto-generated method stub
			return dbEncryption;
			// return TruBoxDatabase.openDatabase(mDBName, null,
			// TruBoxDatabase.NO_LOCALIZED_COLLATORS);
		}
	}
	private static boolean isTableExists(TruBoxDatabase mDatabase,
			String table_Name) {
		boolean tableExists = false;
		/* get cursor on it */
		try {
			mDatabase.query(table_Name, null, null, null, null, null, null);
			tableExists = true;
		} catch (Exception e) {
			/* fail */
			SB_Log.sbE("Table exists", table_Name + " doesn't exist :(((");
		}

		return tableExists;
	}
}
