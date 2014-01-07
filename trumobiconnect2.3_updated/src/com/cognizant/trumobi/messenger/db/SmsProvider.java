package com.cognizant.trumobi.messenger.db;

//import com.TruBoxSDK.TruBoxDatabase;
//import com.TruBoxSDK.TruBoxDatabase;

import java.io.File;

import com.TruBoxSDK.TruBoxDatabase;
import com.cognizant.trumobi.em.Email;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class SmsProvider extends ContentProvider {
	public static TruBoxDatabase messengerDatabase;
	private static final String DATABASE_NAME = TruBoxDatabase.getHashValue(
			"cognizantmessenger.db", Email.getAppContext());
	Context context;

	public SmsProvider() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		Context context = getContext();
		if (messengerDatabase == null)
			messengerDatabase = getDataBase(context);
		Log.e("Messenger Provider", "delete :" + uri);
		String table = matchUri(uri);
		if (messengerDatabase != null) {
			messengerDatabase.execSQL(" PRAGMA foreign_keys = ON ");
			messengerDatabase.delete(table, selection, selectionArgs);
		} else
			Log.e("Exception", "TruBoxDatabase not initialised");
		context.getContentResolver().notifyChange(uri, null);
		return 0;

	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		Context context = getContext();
		if (messengerDatabase == null)
			messengerDatabase = getDataBase(context);
		Log.e("Messenger Provider", "insert ");
		String table = matchUri(uri);
		Log.e("Messenger Provider", "insert " + table);
		long id = 0;
		if (messengerDatabase != null) {
			id = messengerDatabase.insert(table, null, values);
		} else
			Log.e("Exception", "TruBoxDatabase not initialised");

		Log.e("Messenger Provider", "insert " + id);
		context.getContentResolver().notifyChange(uri, null);
		return ContentUris.withAppendedId(uri, id);
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		Context context = getContext();
		if (messengerDatabase == null)
			messengerDatabase = getDataBase(context);
		String table = matchUri(uri);
		Log.e("Messenger Provider", "query");
		Cursor cursor = null;
		if (messengerDatabase != null) {
			cursor = messengerDatabase.query(table, projection, selection,
					selectionArgs, null, null, sortOrder);
		} else
			Log.e("Exception", "TruBoxDatabase not initialised");
	//	context.getContentResolver().notifyChange(uri, null);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		Context context = getContext();
		if (messengerDatabase == null)
			messengerDatabase = getDataBase(context);
		Log.e("Messenger Provider", "Update");
		String table = matchUri(uri);
		if (messengerDatabase != null) {
			messengerDatabase.update(table, values, selection, selectionArgs);
		} else
			Log.e("Exception", "TruBoxDatabase not initialised");
		context.getContentResolver().notifyChange(uri, null);
		return 0;
	}

	@Override
	public void shutdown() {
		if (messengerDatabase != null) {
			messengerDatabase.close();
			messengerDatabase = null;
		}

	}

	private static final UriMatcher sURIMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(SmsIndividualTable.AUTHORITY,
				SmsIndividualTable.TABLE_INDIVIDUAL, 0);
		sURIMatcher.addURI(SmsHistoryTable.AUTHORITY,
				SmsHistoryTable.TABLE_HISTORY, 1);
		sURIMatcher.addURI(SmsHistoryTable.AUTHORITY,
				SmsHistoryBackup.TABLE_HISTORY_BACKUP, 2);

	}

	private static String matchUri(Uri uri) {

		switch (sURIMatcher.match(uri)) {
		case 0:
			return SmsIndividualTable.TABLE_INDIVIDUAL;
		case 1:
			return SmsHistoryTable.TABLE_HISTORY;
		case 2:
			return SmsHistoryBackup.TABLE_HISTORY_BACKUP;

		}
		return "";

	}

	public TruBoxDatabase getDataBase(Context mContext) {
		// TODO Auto-generated method stub
		if (isSuccessLogin(context) == true) {
			TruBoxDatabase.initialiseTruBoxDatabase(mContext);

			File databaseFile = mContext.getDatabasePath(DATABASE_NAME);
			if (!databaseFile.exists()) {
				databaseFile.mkdirs();
				databaseFile.delete();
				messengerDatabase = TruBoxDatabase.openOrCreateDatabase(
						databaseFile, null);
				messengerDatabase.execSQL(SmsDbHelper.CREATE_TABLE_INDIVIDUAL);
				messengerDatabase.execSQL(SmsDbHelper.CREATE_TABLE_HISTORY);
				messengerDatabase.execSQL(SmsDbHelper.CREATE_TABLE_HISTORY_BACKUP);

			} else
				messengerDatabase = TruBoxDatabase.openOrCreateDatabase(
						databaseFile, null);
			return messengerDatabase;
		} else
			return null;
	}

	public void checkDataBase() {
		if (messengerDatabase != null) {
			messengerDatabase.close();
			messengerDatabase = null;
		}
	}

	private static boolean isSuccessLogin(Context context) {

		return TruBoxDatabase.isPasswordGenerated();
	}

}
