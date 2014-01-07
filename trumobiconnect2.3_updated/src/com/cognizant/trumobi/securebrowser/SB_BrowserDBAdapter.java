package com.cognizant.trumobi.securebrowser;

import java.io.File;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.GetChars;
import android.util.Log;

import com.TruBoxSDK.TruBoxDatabase;
import com.cognizant.trumobi.securebrowser.SB_DatabaseHelper;
import com.cognizant.trumobi.securebrowser.SB_Log;

public  class SB_BrowserDBAdapter extends ContentProvider{
	public static final String AUTHORITY = "com.cognizant.trumobi.securebrowser.provider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	protected static final String DATABASE_NAME = "SecureBrowserLocal";
	public static final String MASTER_TABLE = "sb_master";
	public static final String URLLIST_TABLE = "sb_urllist";
	public static final String CONTENTBLOCK_TABLE = "sb_contentkeywordlist";
	public static final String SAFESEARCH_TABLE = "sb_safesearchlist";
	public static final String NETWORK_TABLE = "sb_networklist";
	public static final String REGIONFILTER_TABLE = "sb_regionfilterlist";
	public static final String MASTER_COL1="functionalites";
	public static final String MASTER_COL2="flag";
	public static final String URL_COL1="url";
	public static final String URL_COL2="ip_flag";
	public static final String URL_COL3="ip_networkid";
	public static final String URL_COL4="ip_subnetmask";
	public static final String CONTENTBLOCK_COL="keywords";
	public static final String NETWORK_COL="ip";
	public static final String REGIONFILTER_COL1="latitude";
	public static final String REGIONFILTER_COL2="longitude";
	private static final String[] TABLE_NAMES = {MASTER_TABLE,URLLIST_TABLE,CONTENTBLOCK_TABLE,SAFESEARCH_TABLE,NETWORK_TABLE,REGIONFILTER_TABLE};
	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	private static final int DATABASE_VERSION = 1;
	public static final int BROWSER_MASTER_TABLE = 0001;
	public static final int BROWSER_URLLIST_TABLE = 0002;
	public static final int BROWSER_CONTENTBLOCK_TABLE = 0003;
	public static final int BROWSER_SAFESEARCH_TABLE = 0004;
	public static final int BROWSER_NETWORK_TABLE = 0005;
	public static final int BROWSER_REGIONFILTER_TABLE =0006;
	private Context context = null;
	private SB_DatabaseHelper DBHelper;
	private  TruBoxDatabase mDatabase;
	public static final String  CREATE_MASTER_TABLE="CREATE TABLE " + MASTER_TABLE + " ("
			 + MASTER_COL1+" TEXT,"
			+ MASTER_COL2+" TEXT" + ");";
	public static final String CREATE_URLLIST_TABLE="CREATE TABLE " + URLLIST_TABLE + " ("
			 + URL_COL1+" TEXT," +  URL_COL2+" TEXT," + URL_COL3+" INTEGER," + URL_COL4+" INTEGER"+ ");";

	static {
		// CognizantEmail URI matching table
		UriMatcher matcher = sURIMatcher;
		matcher.addURI(AUTHORITY, MASTER_TABLE, BROWSER_MASTER_TABLE);
		matcher.addURI(AUTHORITY, URLLIST_TABLE, BROWSER_URLLIST_TABLE);
	}
	private static int findMatch(Uri uri, String methodName) {
		int match = sURIMatcher.match(uri);
		if (match < 0) {
			throw new IllegalArgumentException("Unknown uri: " + uri);
		} else if (SB_Log.DEBUG_LOG) {
		}
		return match;
	}
	

		@Override
		public int delete(Uri uri, String selection, String[] selectionArgs) {
			final int match = findMatch(uri, "delete");
			Context context = getContext();
			// Pick the correct database for this operation
			// If we're in a transaction already (which would happen during
			// applyBatch), then the
			// body database is already attached to the email database and any
			// attempt to use the
			// body database directly will result in a SQLiteException (the database
			// is locked)
			TruBoxDatabase db = getDatabase(context);
			if(db == null)
				return -1;
			int table = match;
		
			String id = "0";
			boolean messageDeletion = false;
			ContentResolver resolver = context.getContentResolver();

			/*String tableName = TABLE_NAMES[table];*/
			int result = -1;

			try {
				switch (match) {
				
				
				case BROWSER_MASTER_TABLE:
					result = db.delete(MASTER_TABLE, selection, selectionArgs);
					// Log.d(TAG, "table name of delete: " + tableName);
					break;
				case BROWSER_URLLIST_TABLE:
					result = db.delete(URLLIST_TABLE, selection, selectionArgs);
					// Log.d(TAG, "table name of delete: " + tableName);
					break;
				
					
				
				default:
					throw new IllegalArgumentException("Unknown URI " + uri);
				}
				if (messageDeletion) {
					
					db.setTransactionSuccessful();
				}
			} catch (SQLiteException e) {
				checkDatabases();
				throw e;
			} finally {
				if (messageDeletion) {
					db.endTransaction();
				}
			}

			
			return result;
		}

		@Override
		public String getType(Uri arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Uri insert(Uri uri, ContentValues values) {

			int match = findMatch(uri, "insert");
			long longId;
			Context context = getContext();
			ContentResolver resolver = context.getContentResolver();

			// See the comment at delete(), above
			TruBoxDatabase db = getDatabase(context);
			if(db == null)
				return null;
			int table = match;
			/*String tableName = TABLE_NAMES[table];*/
			Uri resultUri = null;
			try {
				switch (match) {
				// NOTE: It is NOT legal for production code to insert directly into
				// UPDATED_MESSAGE
				// or DELETED_MESSAGE; see the comment below for details
				case BROWSER_MASTER_TABLE:
					longId = db.insert(MASTER_TABLE, null, values);
					resultUri = ContentUris.withAppendedId(uri, longId);
					SB_Log.sbE("Inserted", resultUri+"");
					
					break;
				case BROWSER_URLLIST_TABLE:
					longId = db.insert(URLLIST_TABLE, null, values);
					resultUri = ContentUris.withAppendedId(uri, longId);
					SB_Log.sbE("Inserted", resultUri+"");
					
					break;
				}
			}catch(SQLiteException e) {
				checkDatabases();
				throw e;
			}
			return resultUri;
		}
		public void checkDatabases() {
			// Uncache the databases
			if (mDatabase != null) {
				mDatabase = null;
			}
			
			SB_Log.sbE("EmailProvider", "=========== checkDatabases - DATABASE_NAME: "+TruBoxDatabase.getHashValue(DATABASE_NAME,getContext()));
			File databaseFile = getContext().getDatabasePath(TruBoxDatabase.getHashValue(DATABASE_NAME,getContext()));

			// TODO Make sure attachments are deleted
			if (databaseFile.exists() ) {
				//databaseFile.delete();
				getContext().deleteDatabase(TruBoxDatabase.getHashValue(DATABASE_NAME,getContext()));
			} 
		}
		@Override
		public boolean onCreate() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Cursor query(Uri uri, String[] projection , String selection ,
				String[] selectionArgs , String sortOrder) {
			long time = 0L;
			
			
			Cursor c = null;
			int match;
			try {
				match = findMatch(uri, "query");
			} catch (IllegalArgumentException e) {
				String uriString = uri.toString();
				// If we were passed an illegal uri, see if it ends in /-1
				// if so, and if substituting 0 for -1 results in a valid uri,
				// return an empty cursor
				if (uriString != null && uriString.endsWith("/-1")) {
					uri = Uri.parse(uriString.substring(0, uriString.length() - 2) + "0");
					match = findMatch(uri, "query");
					switch (match) {
					
					case BROWSER_MASTER_TABLE:
						return new MatrixCursor(projection, 0);
					case BROWSER_URLLIST_TABLE:
						return new MatrixCursor(projection, 0);
					}
				}
				throw e;
			}
			
			Context context = getContext();
			// See the comment at delete(), above
			TruBoxDatabase db = getDatabase(context);
			
			if(db == null)
				return null;
			
			try {
				switch (match) {
				case BROWSER_MASTER_TABLE:
					Log.i("SecureBrowser", " case secure browser query "+MASTER_TABLE);
					c = db.query(MASTER_TABLE, projection, selection, selectionArgs, null, null, sortOrder, null);
					if (c.moveToFirst()) {
				 		 String d=c.getString(c.getColumnIndex(SB_BrowserDBAdapter.MASTER_COL2));
				 		 SB_Log.sbE("Cursor values", d);
				 		}
					break;
				case BROWSER_URLLIST_TABLE:
					Log.i("SecureBrowser", " case secure browser query "+URLLIST_TABLE);
					c = db.query(URLLIST_TABLE, projection, selection, selectionArgs, null, null, sortOrder, null);
					if (c.moveToFirst()) {
				 		 String d=c.getString(c.getColumnIndex(SB_BrowserDBAdapter.URL_COL1));
				 		String d1=c.getString(c.getColumnIndex(SB_BrowserDBAdapter.URL_COL2));
				 		String d2=c.getString(c.getColumnIndex(SB_BrowserDBAdapter.URL_COL3));
				 		String d3=c.getString(c.getColumnIndex(SB_BrowserDBAdapter.URL_COL4));
				 		 SB_Log.sbD("blacklist", d+" "+d1+" "+d2+" "+d3);
				 		 if(d2!=null){
				 			 SB_Log.sbE("Binary for "+d2,Long.toBinaryString(Long.parseLong(d2))+"");
				 		 }if(d3!=null){
				 			 SB_Log.sbE("Binary for "+d3,Long.toBinaryString(Long.parseLong(d3))+"");
				 		 }
				 		}
					break;
				default:
					throw new IllegalArgumentException("Unknown URI " + uri);
				}
			} catch (SQLiteException e) {
				checkDatabases();
				throw e;
			} catch (RuntimeException e) {
				checkDatabases();
				e.printStackTrace();
				throw e;
			} finally {
				
			}
			return c;
		}

		@Override
		public int update(Uri uri, ContentValues values, String selection,
				String[] selectionArgs) {
			
			int match = findMatch(uri, "update");
			Context context = getContext();
			ContentResolver resolver = context.getContentResolver();
			// See the comment at delete(), above
			TruBoxDatabase db = getDatabase(context);
			if(db == null)
				return -1;
			int result;

			try {
				switch (match) {
						
				case BROWSER_MASTER_TABLE:
					Log.i("SecureBrowser", " case secure browser update "+MASTER_TABLE);
					result = db.update(MASTER_TABLE, values, selection, selectionArgs);
					break;
				case BROWSER_URLLIST_TABLE:
					Log.i("SecureBrowser", " case secure browser update "+URLLIST_TABLE);
					result = db.update(URLLIST_TABLE, values, selection, selectionArgs);
					break;

				default:
					throw new IllegalArgumentException("Unknown URI " + uri);
				}
			} catch (SQLiteException e) {
				checkDatabases();
				throw e;
			}

			
			return result;
		}
		private static TruBoxDatabase mstaticDatabase;

		public static TruBoxDatabase getDB() {
			return mstaticDatabase;
		}

		synchronized TruBoxDatabase getDatabase(Context context) {
			
			
			
				// Always return the cached database, if we've got one
				if (mDatabase != null ) {
					mstaticDatabase = mDatabase;// CONTAINER CHANGES ENDS
					
					//Changes for wipe data. After wiping data, mDatabase will not be null so checking whether db exists	
					//290661 changes
					File dbFile=new File(mDatabase.getPath());
					if(dbFile.exists())			
						return mDatabase;
					else	//367712, DataBaseObject not closed Exception
					{
						if(mDatabase != null)	mDatabase.close();
						
//						mDatabase = null;
//						mBodyDatabase = null;
					}
					//290661 changes end
				}
				//checkDatabases();
				SB_DatabaseHelper.DatabaseHelper helper = new SB_DatabaseHelper.DatabaseHelper(context, TruBoxDatabase.getHashValue(DATABASE_NAME,getContext()));
				mDatabase = helper.getWritableDatabase();
				return mDatabase;
			}
		@Override
		public int bulkInsert(Uri uri, ContentValues[] values) {

			int match = findMatch(uri, "insert");
			Context context = getContext();
			ContentResolver resolver = context.getContentResolver();

			TruBoxDatabase db = getDatabase(context);
			int table = match;

			String id = "0";
			long rowId;
			int rowsAdded = 0;
			int numInserted = 0;

			SB_Log.sbE("SecureBrowser", "table : " + table);
			SB_Log.sbE("SecureBrowser", "match : " + match);
			SB_Log.sbE("SecureBrowser", "SWITCH  : " +TABLE_NAMES[table]+"  "+BROWSER_MASTER_TABLE);
			try {
				switch (match) {
				
				case BROWSER_MASTER_TABLE:
					
					Log.i("SecureBrowser", " case secure browser");
					db.beginTransaction();
					try {

						for (ContentValues cv : values) {

								rowId = db.insert(TABLE_NAMES[table], null, cv);
								Log.i("Email Client Bulk ", ": " + rowId);
								if (rowId > 0)
									rowsAdded++;
						}
						Log.e("Email Client Bulk ", ": " + rowsAdded);
						db.setTransactionSuccessful();
						numInserted = values.length;
					} finally {
						db.endTransaction();
					}
					break;
				
				default:
					throw new IllegalArgumentException("Unknown URL " + uri);
				}
			} catch (SQLiteException e) {
				throw e;
			}

			
			return numInserted;

		}
}
