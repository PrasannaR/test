package com.cognizant.trumobi.dialer.db;

import java.io.File;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.TruBoxSDK.TruBoxDatabase;

public class DialerCorporateProvider extends ContentProvider {

	public static TruBoxDatabase dialerCorporateDatabase;

	public DialerCorporateProvider() {

	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		Context context = getContext();
		if (dialerCorporateDatabase == null)
			dialerCorporateDatabase = getDataBase(context);
		Log.e("DialerCorporate Provider", "delete :" + uri);
		String table = matchUri(uri);
		if (dialerCorporateDatabase != null) {
			try {
				dialerCorporateDatabase.execSQL(" PRAGMA foreign_keys = ON ");
				dialerCorporateDatabase.delete(table, selection, selectionArgs);
			} catch (Exception e) {
			}
		} else
			Log.e("DialerCorporate Exception", "TruBoxDatabase not initialised");
		return 0;

	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Context context = getContext();
		if (dialerCorporateDatabase == null)
			dialerCorporateDatabase = getDataBase(context);
		Log.e("DialerCorporate Provider", "insert ");
		String table = matchUri(uri);
		Log.e("DialerCorporate Provider", "insert " + table);
		long id = 0;
		if (dialerCorporateDatabase != null) {
			try {
				id = dialerCorporateDatabase.insert(table, null, values);
			} catch (Exception e) {
			}
		} else
			Log.e("DialerCorporate Exception", "TruBoxDatabase not initialised");

		Log.e("DialerCorporate Provider", "insert " + id);

		return ContentUris.withAppendedId(uri, id);
	}

	@Override
	public boolean onCreate() {

		Context context = getContext();
		dialerCorporateDatabase = getDataBase(context);
		return (dialerCorporateDatabase == null) ? false : true;

	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		Context context = getContext();
		if (dialerCorporateDatabase == null)
			dialerCorporateDatabase = getDataBase(context);
		String table = matchUri(uri);		
		Cursor cursor = null;
		if (dialerCorporateDatabase != null) {
			try {
				cursor = dialerCorporateDatabase.query(table, projection,
						selection, selectionArgs, null, null, sortOrder);
			} catch (Exception e) {
			}
		} else
			Log.e("DialerCorporate Exception", "TruBoxDatabase not initialised");
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {

		Context context = getContext();
		if (dialerCorporateDatabase == null)
			dialerCorporateDatabase = getDataBase(context);
		Log.e("DialerCorporate Provider", "Update");
		String table = matchUri(uri);
		if (dialerCorporateDatabase != null) {
			try {
				dialerCorporateDatabase.update(table, values, selection,
						selectionArgs);
			} catch (Exception e) {
			}
		} else
			Log.e("DialerCorporate Exception", "TruBoxDatabase not initialised");
		return 0;
	}

	@Override
	public void shutdown() {
		if (dialerCorporateDatabase != null) {
			dialerCorporateDatabase.close();
			dialerCorporateDatabase = null;
		}

	}

	private static final UriMatcher sURIMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(DialerCorporateTable.AUTHORITY,
				DialerCorporateTable.TABLE_NAME, 0);

	}

	private static String matchUri(Uri uri) {

		switch (sURIMatcher.match(uri)) {
		case 0:
			return DialerCorporateTable.TABLE_NAME;

		}
		return "";

	}

	public TruBoxDatabase getDataBase(Context mContext) {

		if (isSuccessLogin(mContext) == true) {
			TruBoxDatabase.initialiseTruBoxDatabase(mContext);

			File databaseFile = mContext
					.getDatabasePath(DialerCorporateTable.DATABASE_NAME);
			if (!databaseFile.exists()) {
				databaseFile.mkdirs();
				databaseFile.delete();

				dialerCorporateDatabase = TruBoxDatabase.openOrCreateDatabase(
						databaseFile, null);
				dialerCorporateDatabase
						.execSQL(DialerCorporateDbHelper.CREATE_TABLE_DIALER_CORPORATE);

			} else
				dialerCorporateDatabase = TruBoxDatabase.openOrCreateDatabase(
						databaseFile, null);
			return dialerCorporateDatabase;
		} else
			return null;
	}

	public void checkDataBase() {
		if (dialerCorporateDatabase != null) {
			dialerCorporateDatabase.close();
			dialerCorporateDatabase = null;
		}
	}

	private static boolean isSuccessLogin(Context context) {

		return TruBoxDatabase.isPasswordGenerated();
	}

}
