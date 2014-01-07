/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cognizant.trumobi;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Xml;

import com.TruBoxSDK.TruBoxDatabase;
import com.cognizant.trumobi.PersonaLauncherSettings.Favorites;
import com.cognizant.trumobi.calendar.util.CalendarConstants;
import com.cognizant.trumobi.log.CalendarLog;
import com.cognizant.trumobi.log.PersonaLog;
import com.cognizant.trumobi.persona.launcherutils.PersonaXmlUtils;
public class PersonaLauncherProvider extends ContentProvider {
	private static final String LOG_TAG = "PersonaLauncherProvider";
	private static final boolean LOGD = true;

	private static final String DATABASE_NAME = "launcher.db";

	private static final int DATABASE_VERSION = 5;

	static final String AUTHORITY = "com.cognizant.trumobi.persona.launcher.settings";

	static final String EXTRA_BIND_SOURCES = "com.cognizant.trumobi.persona.launcher.settings.bindsources";
	static final String EXTRA_BIND_TARGETS = "com.cognizant.trumobi.persona.launcher.settings.bindtargets";

	static final String TABLE_FAVORITES = "favorites";
	static final String TABLE_GESTURES = "gestures";
	static final String PARAMETER_NOTIFY = "notify";


	/**
	 * {@link Uri} triggered at any registered {@link android.database.ContentObserver} when
	 * {@link AppWidgetHost#deleteHost()} is called during database creation.
	 * Use this to recall {@link AppWidgetHost#startListening()} if needed.
	 */
	static final Uri CONTENT_APPWIDGET_RESET_URI =
			Uri.parse("content://" + AUTHORITY + "/appWidgetReset");

	//trumobiedits


	//   private SQLiteOpenHelper mOpenHelper;
	private PersonaLauncherDbEncryptHelper mPersonaLauncherHelper;

	private TruBoxDatabase mDatabase;

	synchronized TruBoxDatabase getDatabase(Context context) {
		// Always return the cached database, if we've got one
	//	if(TruBoxDatabase.isPasswordGenerated()) {
			if (mDatabase != null) {
				return mDatabase;
			}
	
			mPersonaLauncherHelper = new PersonaLauncherDbEncryptHelper(getContext(), DATABASE_NAME);
			mDatabase = mPersonaLauncherHelper.getWritableDatabase();
	//	}
		return mDatabase;
	}


	@Override
	public boolean onCreate() {

		//trumobiedits
		//   mOpenHelper = new DatabaseHelper(getContext());
	
		//    	 mPersonaLauncherHelper = new PersonaLauncherDbEncryptHelper(getContext(), DATABASE_NAME);
		//mDatabase=getDatabase(getContext());
		return true;

	}

	@Override
	public String getType(Uri uri) {
		SqlArguments args = new SqlArguments(uri, null, null);
		if (TextUtils.isEmpty(args.where)) {
			return "vnd.android.cursor.dir/" + args.table;
		} else {
			return "vnd.android.cursor.item/" + args.table;
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		//trumobiedits

		/*  SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(args.table);


        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Cursor result = qb.query(db, projection, args.where, args.args, null, null, sortOrder);
        result.setNotificationUri(getContext().getContentResolver(), uri);

        return result;*/
		SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(args.table);

		PersonaLog.d(LOG_TAG,"============= In query - Encrypt DB helper ==========");
		//TruBoxDatabase db = mPersonaLauncherHelper.getWritableDatabase();
		TruBoxDatabase db =getDatabase(getContext());
		if(db!=null){

		Cursor result = db.query(args.table, projection, args.where, args.args, null, null, sortOrder);
		result.setNotificationUri(getContext().getContentResolver(), uri);

		return result;
		}
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		SqlArguments args = new SqlArguments(uri);
		//trumobiedits

		// SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		TruBoxDatabase db = getDatabase(getContext());
		final long rowId = db.insert(args.table, null, initialValues);
		if (rowId <= 0) return null;

		uri = ContentUris.withAppendedId(uri, rowId);
		sendNotify(uri);

		return uri;






	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {
		SqlArguments args = new SqlArguments(uri);

		//trumobiedits

		//   SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		TruBoxDatabase db = getDatabase(getContext());
		db.beginTransaction();
		try {
			int numValues = values.length;
			for (int i = 0; i < numValues; i++) {
				if (db.insert(args.table, null, values[i]) < 0) return 0;
			}
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}

		sendNotify(uri);
		return values.length;









	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SqlArguments args = new SqlArguments(uri, selection, selectionArgs);

		//trumobiedits

		//  SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		TruBoxDatabase db = getDatabase(getContext());
		int count = db.delete(args.table, args.where, args.args);
		if (count > 0) sendNotify(uri);

		return count;

	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
		//trumobiedits
		// SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		TruBoxDatabase db = getDatabase(getContext());
		int count = db.update(args.table, values, args.where, args.args);
		if (count > 0) sendNotify(uri);

		return count;

	}

	private void sendNotify(Uri uri) {
		String notify = uri.getQueryParameter(PARAMETER_NOTIFY);
		if (notify == null || "true".equals(notify)) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
	}

	private static class DatabaseHelper extends SQLiteOpenHelper {
		private static final String TAG_FAVORITES = "favorites";
		private static final String TAG_FAVORITE = "favorite";
		private static final String TAG_CLOCK = "clock";
		private static final String TAG_SEARCH = "search";
		private static final String TAG_APPWIDGET = "appwidget";
		private static final String TAG_SHORTCUT = "shortcut";        

		private final Context mContext;
		private final AppWidgetHost mAppWidgetHost;

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			mContext = context;
			mAppWidgetHost = new AppWidgetHost(context, PersonaLauncher.APPWIDGET_HOST_ID);
		}

		/**
		 * Send notification that we've deleted the {@link AppWidgetHost},
		 * probably as part of the initial database creation. The receiver may
		 * want to re-call {@link AppWidgetHost#startListening()} to ensure
		 * callbacks are correctly set.
		 */
		private void sendAppWidgetResetNotify() {
			final ContentResolver resolver = mContext.getContentResolver();
			resolver.notifyChange(CONTENT_APPWIDGET_RESET_URI, null);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			if (LOGD) PersonaLog.d(LOG_TAG, "creating new launcher database");

			db.execSQL("CREATE TABLE favorites (" +
					"_id INTEGER PRIMARY KEY," +
					"title TEXT," +
					"intent TEXT," +
					"container INTEGER," +
					"screen INTEGER," +
					"cellX INTEGER," +
					"cellY INTEGER," +
					"spanX INTEGER," +
					"spanY INTEGER," +
					"itemType INTEGER," +
					"appWidgetId INTEGER NOT NULL DEFAULT -1," +
					"isShortcut INTEGER," +
					"iconType INTEGER," +
					"iconPackage TEXT," +
					"iconResource TEXT," +
					"icon BLOB," +
					"uri TEXT," +
					"displayMode INTEGER" +
					");");

			db.execSQL("CREATE TABLE gestures (" +
					"_id INTEGER PRIMARY KEY," +
					"title TEXT," +
					"intent TEXT," +
					"itemType INTEGER," +
					"iconType INTEGER," +
					"iconPackage TEXT," +
					"iconResource TEXT," +
					"icon BLOB" +
					");");

			// Database was just created, so wipe any previous widgets
			if (mAppWidgetHost != null) {
				mAppWidgetHost.deleteHost();
				sendAppWidgetResetNotify();
			}

			if (!convertDatabase(db)) {
				// Populate favorites table with initial favorites
				loadFavorites(db);
			}
		}

		private boolean convertDatabase(SQLiteDatabase db) {
			if (LOGD) PersonaLog.d(LOG_TAG, "converting database from an older format, but not onUpgrade");
			boolean converted = false;

			final Uri uri = Uri.parse("content://" + Settings.AUTHORITY +
					"/old_favorites?notify=true");
			final ContentResolver resolver = mContext.getContentResolver();
			Cursor cursor = null;

			try {
				cursor = resolver.query(uri, null, null, null, null);
			} catch (Exception e) {
				// Ignore
			}

			// We already have a favorites database in the old provider
			if (cursor != null && cursor.getCount() > 0) {
				try {
					converted = copyFromCursor(db, cursor) > 0;
				} finally {
					cursor.close();
				}

				if (converted) {
					resolver.delete(uri, null, null);
				}
			}

			if (converted) {
				// Convert widgets from this import into widgets
				if (LOGD) PersonaLog.d(LOG_TAG, "converted and now triggering widget upgrade");
				convertWidgets(db);
			}

			return converted;
		}

		private int copyFromCursor(SQLiteDatabase db, Cursor c) {
			final int idIndex = c.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites._ID);
			final int intentIndex = c.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.INTENT);
			final int titleIndex = c.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.TITLE);
			final int iconTypeIndex = c.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.ICON_TYPE);
			final int iconIndex = c.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.ICON);
			final int iconPackageIndex = c.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.ICON_PACKAGE);
			final int iconResourceIndex = c.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.ICON_RESOURCE);
			final int containerIndex = c.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.CONTAINER);
			final int itemTypeIndex = c.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.ITEM_TYPE);
			final int screenIndex = c.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.SCREEN);
			final int cellXIndex = c.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.CELLX);
			final int cellYIndex = c.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.CELLY);
			final int uriIndex = c.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.URI);
			final int displayModeIndex = c.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.DISPLAY_MODE);

			ContentValues[] rows = new ContentValues[c.getCount()];
			int i = 0;
			while (c.moveToNext()) {
				ContentValues values = new ContentValues(c.getColumnCount());
				values.put(PersonaLauncherSettings.Favorites._ID, c.getLong(idIndex));
				values.put(PersonaLauncherSettings.Favorites.INTENT, c.getString(intentIndex));
				values.put(PersonaLauncherSettings.Favorites.TITLE, c.getString(titleIndex));
				values.put(PersonaLauncherSettings.Favorites.ICON_TYPE, c.getInt(iconTypeIndex));
				values.put(PersonaLauncherSettings.Favorites.ICON, c.getBlob(iconIndex));
				values.put(PersonaLauncherSettings.Favorites.ICON_PACKAGE, c.getString(iconPackageIndex));
				values.put(PersonaLauncherSettings.Favorites.ICON_RESOURCE, c.getString(iconResourceIndex));
				values.put(PersonaLauncherSettings.Favorites.CONTAINER, c.getInt(containerIndex));
				values.put(PersonaLauncherSettings.Favorites.ITEM_TYPE, c.getInt(itemTypeIndex));
				values.put(PersonaLauncherSettings.Favorites.APPWIDGET_ID, -1);
				values.put(PersonaLauncherSettings.Favorites.SCREEN, c.getInt(screenIndex));
				values.put(PersonaLauncherSettings.Favorites.CELLX, c.getInt(cellXIndex));
				values.put(PersonaLauncherSettings.Favorites.CELLY, c.getInt(cellYIndex));
				values.put(PersonaLauncherSettings.Favorites.URI, c.getString(uriIndex));
				values.put(PersonaLauncherSettings.Favorites.DISPLAY_MODE, c.getInt(displayModeIndex));
				rows[i++] = values;
			}

			db.beginTransaction();
			int total = 0;
			try {
				int numValues = rows.length;
				for (i = 0; i < numValues; i++) {
					if (db.insert(TABLE_FAVORITES, null, rows[i]) < 0) {
						return 0;
					} else {
						total++;
					}
				}
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}

			return total;
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if (LOGD) PersonaLog.d(LOG_TAG, "onUpgrade triggered");

			int version = oldVersion;
			if (version < 3) {
				// upgrade 1,2 -> 3 added appWidgetId column
				db.beginTransaction();
				try {
					// Insert new column for holding appWidgetIds
					db.execSQL("ALTER TABLE favorites " +
							"ADD COLUMN appWidgetId INTEGER NOT NULL DEFAULT -1;");
					db.setTransactionSuccessful();
					version = 3;
				} catch (SQLException ex) {
					// Old version remains, which means we wipe old data
					PersonaLog.e(LOG_TAG, ex.getMessage(), ex);
				} finally {
					db.endTransaction();
				}

				// Convert existing widgets only if table upgrade was successful
				if (version == 3) {
					convertWidgets(db);
				}
			}

			if (version < 4) {
				db.beginTransaction();
				try {
					db.execSQL("CREATE TABLE gestures (" +
							"_id INTEGER PRIMARY KEY," +
							"title TEXT," +
							"intent TEXT," +
							"itemType INTEGER," +
							"iconType INTEGER," +
							"iconPackage TEXT," +
							"iconResource TEXT," +
							"icon BLOB" +
							");");
					db.setTransactionSuccessful();
					version = 4;
				} catch (SQLException ex) {
					// Old version remains, which means we wipe old data
					PersonaLog.e(LOG_TAG, ex.getMessage(), ex);
				} finally {
					db.endTransaction();
				}
			}
			if (version < 5) {
				db.beginTransaction();
				try {
					ComponentName c=new ComponentName(mContext, PersonaCustomShirtcutActivity.class);
					db.execSQL("INSERT INTO 'favorites'" +
							"("+PersonaLauncherSettings.Favorites.TITLE+","+
							PersonaLauncherSettings.Favorites.INTENT+","+
							PersonaLauncherSettings.Favorites.CONTAINER+","+
							PersonaLauncherSettings.Favorites.SCREEN+","+
							PersonaLauncherSettings.Favorites.CELLX+","+
							PersonaLauncherSettings.Favorites.CELLY+","+
							PersonaLauncherSettings.Favorites.SPANX+","+
							PersonaLauncherSettings.Favorites.SPANY+","+
							PersonaLauncherSettings.Favorites.ITEM_TYPE+","+
							PersonaLauncherSettings.Favorites.APPWIDGET_ID+","+
							PersonaLauncherSettings.Favorites.ICON_TYPE+","+
							PersonaLauncherSettings.Favorites.ICON_PACKAGE+","+
							PersonaLauncherSettings.Favorites.ICON_RESOURCE+","+
							PersonaLauncherSettings.Favorites.ICON+","+
							PersonaLauncherSettings.Favorites.URI+","+
							PersonaLauncherSettings.Favorites.DISPLAY_MODE+
							")"+
							"VALUES(" +
							"'Apps'," +
							"'#Intent;action="+PersonaCustomShirtcutActivity.ACTION_LAUNCHERACTION+";component="+c.flattenToString()+";i.DefaultLauncherAction.EXTRA_BINDINGVALUE=4;end'," +
							PersonaLauncherSettings.Favorites.CONTAINER_MAB+"," +
							"-1," +
							"-1," +
							"-1," +
							"1," +
							"1," +
							"1," +
							"-1," +
							"0," +
							"'"+c.getPackageName()+"'," +
							"'"+c.getPackageName()+":drawable/all_apps_button'," +
							"NULL," +
							"NULL," +
							"NULL);");
					db.setTransactionSuccessful();
					version = 5;
				} catch (SQLException ex) {
					// Old version remains, which means we wipe old data
					PersonaLog.e(LOG_TAG, ex.getMessage(), ex);
				} finally {
					db.endTransaction();
				}
			}

			if (version != DATABASE_VERSION) {
				PersonaLog.w(LOG_TAG, "Destroying all old data.");
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_GESTURES);
				onCreate(db);
			}
		}

		/**
		 * Upgrade existing clock and photo frame widgets into their new widget
		 * equivalents. This method allocates appWidgetIds, and then hands off to
		 * LauncherAppWidgetBinder to finish the actual binding.
		 */
		private void convertWidgets(SQLiteDatabase db) {
			final int[] bindSources = new int[] {
					Favorites.ITEM_TYPE_WIDGET_CLOCK,
					Favorites.ITEM_TYPE_WIDGET_PHOTO_FRAME,
			};

			final ArrayList<ComponentName> bindTargets = new ArrayList<ComponentName>();
			bindTargets.add(new ComponentName("com.android.alarmclock",
					"com.android.alarmclock.AnalogAppWidgetProvider"));
			bindTargets.add(new ComponentName("com.android.camera",
					"com.android.camera.PhotoAppWidgetProvider"));

			final String selectWhere = buildOrWhereString(Favorites.ITEM_TYPE, bindSources);

			Cursor c = null;
			boolean allocatedAppWidgets = false;

			db.beginTransaction();
			try {
				// Select and iterate through each matching widget
				c = db.query(TABLE_FAVORITES, new String[] { Favorites._ID },
						selectWhere, null, null, null, null);

				if (LOGD) PersonaLog.d(LOG_TAG, "found upgrade cursor count="+c.getCount());

				final ContentValues values = new ContentValues();
				while (c != null && c.moveToNext()) {
					long favoriteId = c.getLong(0);

					// Allocate and update database with new appWidgetId
					try {
						int appWidgetId = mAppWidgetHost.allocateAppWidgetId();

						if (LOGD) PersonaLog.d(LOG_TAG, "allocated appWidgetId="+appWidgetId+" for favoriteId="+favoriteId);

						values.clear();
						values.put(PersonaLauncherSettings.Favorites.APPWIDGET_ID, appWidgetId);

						// Original widgets might not have valid spans when upgrading
						values.put(PersonaLauncherSettings.Favorites.SPANX, 2);
						values.put(PersonaLauncherSettings.Favorites.SPANY, 2);

						String updateWhere = Favorites._ID + "=" + favoriteId;
						db.update(TABLE_FAVORITES, values, updateWhere, null);

						allocatedAppWidgets = true;
					} catch (RuntimeException ex) {
						PersonaLog.e(LOG_TAG, "Problem allocating appWidgetId", ex);
					}
				}

				db.setTransactionSuccessful();
			} catch (SQLException ex) {
				PersonaLog.w(LOG_TAG, "Problem while allocating appWidgetIds for existing widgets", ex);
			} finally {
				db.endTransaction();
				if (c != null) {
					c.close();
				}
			}

			// If any appWidgetIds allocated, then launch over to binder
			if (allocatedAppWidgets) {
				launchAppWidgetBinder(bindSources, bindTargets);
			}
		}

		/**
		 * Launch the widget binder that walks through the PersonaLauncher database,
		 * binding any matching widgets to the corresponding targets. We can't
		 * bind ourselves because our parent process can't obtain the
		 * BIND_APPWIDGET permission.
		 */
		private void launchAppWidgetBinder(int[] bindSources, ArrayList<ComponentName> bindTargets) {
			final Intent intent = new Intent();
			intent.setComponent(new ComponentName("com.android.settings",
					"com.android.settings.LauncherAppWidgetBinder"));
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			final Bundle extras = new Bundle();
			extras.putIntArray(EXTRA_BIND_SOURCES, bindSources);
			extras.putParcelableArrayList(EXTRA_BIND_TARGETS, bindTargets);
			intent.putExtras(extras);

			mContext.startActivity(intent);
		}

		/**
		 * Loads the default set of favorite packages from an xml file.
		 *
		 * @param db The database to write the values into
		 */
		private int loadFavorites(SQLiteDatabase db) {
			/* Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);*/


			//290778 modified for loading shortcuts
			Intent intent = new Intent("Email");
			intent.addCategory("android.intent.category.PIM");
			ContentValues values = new ContentValues();

			XmlResourceParser parser = null;

			PackageManager packageManager = mContext.getPackageManager();
			int i = 0;
			try {

				if(PersonaLauncher.wsettings==0)
				{
					parser = mContext.getResources().getXml(R.xml.pr_default_workspace);
				}
		


				AttributeSet attrs = Xml.asAttributeSet(parser);
				PersonaXmlUtils.beginDocument(parser, TAG_FAVORITES);

				final int depth = parser.getDepth();

				int type;
				while (((type = parser.next()) != XmlPullParser.END_TAG ||
						parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {

					if (type != XmlPullParser.START_TAG) {
						continue;
					}

					boolean added = false;
					final String name = parser.getName();

					TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.Favorite);

					values.clear();          
					String container = a.getString(R.styleable.Favorite_container);
					if (container == null) {
						values.put(PersonaLauncherSettings.Favorites.CONTAINER, PersonaLauncherSettings.Favorites.CONTAINER_DESKTOP);
					} else {
						values.put(PersonaLauncherSettings.Favorites.CONTAINER, container);
					}

					values.put(PersonaLauncherSettings.Favorites.SCREEN,
							a.getString(R.styleable.Favorite_screen));
					values.put(PersonaLauncherSettings.Favorites.CELLX,
							a.getString(R.styleable.Favorite_x));
					values.put(PersonaLauncherSettings.Favorites.CELLY,
							a.getString(R.styleable.Favorite_y));

					if (TAG_FAVORITE.equals(name)) {
						added = addShortcut(db, values, a, packageManager, intent);
					} else if (TAG_SEARCH.equals(name)) {
						added = addSearchWidget(db, values);
					} else if (TAG_CLOCK.equals(name)) {
						added = addClockWidget(db, values);
					} else if (TAG_APPWIDGET.equals(name)) {
						added = addAppWidget(db, values, a);
					} else if (TAG_SHORTCUT.equals(name)) {
						added = addUriShortcut(db, values, a);
					}

					if (added) i++;

					a.recycle();
				}
			} catch (XmlPullParserException e) {
				PersonaLog.w(LOG_TAG, "Got exception parsing favorites.", e);
			} catch (IOException e) {
				PersonaLog.w(LOG_TAG, "Got exception parsing favorites.", e);
			}

			return i;
		}

		private boolean addShortcut(SQLiteDatabase db, ContentValues values, TypedArray a,
				PackageManager packageManager, Intent intent) {

			ActivityInfo info;
			String packageName = a.getString(R.styleable.Favorite_packageName);
			String className = a.getString(R.styleable.Favorite_className);
			try {
				ComponentName cn = new ComponentName(packageName, className);
				info = packageManager.getActivityInfo(cn, 0);
				intent.setComponent(cn);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				values.put(Favorites.INTENT, intent.toUri(0));
				values.put(Favorites.TITLE, info.loadLabel(packageManager).toString());
				values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_APPLICATION);
				values.put(Favorites.SPANX, 1);
				values.put(Favorites.SPANY, 1);
				db.insert(TABLE_FAVORITES, null, values);
			} catch (PackageManager.NameNotFoundException e) {
				PersonaLog.w(LOG_TAG, "Unable to add favorite: " + packageName +
						"/" + className, e);
				return false;
			}
			return true;
		}

		private boolean addSearchWidget(SQLiteDatabase db, ContentValues values) {
			// Add a search box
			values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_WIDGET_SEARCH);
			values.put(Favorites.SPANX, 4);
			values.put(Favorites.SPANY, 1);
			db.insert(TABLE_FAVORITES, null, values);

			return true;
		}

		private boolean addClockWidget(SQLiteDatabase db, ContentValues values) {
			final int[] bindSources = new int[] {
					Favorites.ITEM_TYPE_WIDGET_CLOCK,
			};

			final ArrayList<ComponentName> bindTargets = new ArrayList<ComponentName>();
			bindTargets.add(new ComponentName("com.android.alarmclock",
					"com.android.alarmclock.AnalogAppWidgetProvider"));

			boolean allocatedAppWidgets = false;

			// Try binding to an analog clock widget
			try {
				int appWidgetId = mAppWidgetHost.allocateAppWidgetId();

				values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_WIDGET_CLOCK);
				values.put(Favorites.SPANX, 2);
				values.put(Favorites.SPANY, 2);
				values.put(Favorites.APPWIDGET_ID, appWidgetId);
				db.insert(TABLE_FAVORITES, null, values);

				allocatedAppWidgets = true;
			} catch (RuntimeException ex) {
				PersonaLog.e(LOG_TAG, "Problem allocating appWidgetId", ex);
			}

			// If any appWidgetIds allocated, then launch over to binder
			if (allocatedAppWidgets) {
				launchAppWidgetBinder(bindSources, bindTargets);
			}

			return allocatedAppWidgets;
		}

		private boolean addAppWidget(SQLiteDatabase db, ContentValues values, TypedArray a) {
			String packageName = a.getString(R.styleable.Favorite_packageName);
			String className = a.getString(R.styleable.Favorite_className);

			if (packageName == null || className == null) {
				return false;
			}

			ComponentName cn = new ComponentName(packageName, className);

			boolean allocatedAppWidgets = false;
			final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);

			try {
				int appWidgetId = mAppWidgetHost.allocateAppWidgetId();

				values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_APPWIDGET);
				values.put(Favorites.SPANX, a.getString(R.styleable.Favorite_spanX));
				values.put(Favorites.SPANY, a.getString(R.styleable.Favorite_spanY));
				values.put(Favorites.APPWIDGET_ID, appWidgetId);
				db.insert(TABLE_FAVORITES, null, values);

				allocatedAppWidgets = true;

				//Commented for Higher SDK version

				//appWidgetManager.bindAppWidgetId(appWidgetId, cn);
			} catch (RuntimeException ex) {
				PersonaLog.e(LOG_TAG, "Problem allocating appWidgetId", ex);
			}

			return allocatedAppWidgets;
		}

		private boolean addUriShortcut(SQLiteDatabase db, ContentValues values,
				TypedArray a) {
			Resources r = mContext.getResources();

			final int iconResId = a.getResourceId(R.styleable.Favorite_pr_icon, 0);
			final int titleResId = a.getResourceId(R.styleable.Favorite_pr_title, 0);

			Intent intent;
			String uri = null;
			try {
				uri = a.getString(R.styleable.Favorite_uri);
				intent = Intent.parseUri(uri, 0);
			} catch (URISyntaxException e) {
				PersonaLog.w(LOG_TAG, "Shortcut has malformed uri: " + uri);
				return false; // Oh well
			}

			if (iconResId == 0 || titleResId == 0) {
				PersonaLog.w(LOG_TAG, "Shortcut is missing title or icon resource ID");
				return false;
			}

			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			values.put(Favorites.INTENT, intent.toUri(0));
			values.put(Favorites.TITLE, r.getString(titleResId));
			values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_SHORTCUT);
			values.put(Favorites.SPANX, 1);
			values.put(Favorites.SPANY, 1);
			values.put(Favorites.ICON_TYPE, Favorites.ICON_TYPE_RESOURCE);
			values.put(Favorites.ICON_PACKAGE, mContext.getPackageName());
			values.put(Favorites.ICON_RESOURCE, r.getResourceName(iconResId));

			db.insert(TABLE_FAVORITES, null, values);

			return true;
		}
	}

	/**
	 * Build a query string that will match any row where the column matches
	 * anything in the values list.
	 */
	static String buildOrWhereString(String column, int[] values) {
		StringBuilder selectWhere = new StringBuilder();
		for (int i = values.length - 1; i >= 0; i--) {
			selectWhere.append(column).append("=").append(values[i]);
			if (i > 0) {
				selectWhere.append(" OR ");
			}
		}
		return selectWhere.toString();
	}

	static class SqlArguments {
		public final String table;
		public final String where;
		public final String[] args;

		SqlArguments(Uri url, String where, String[] args) {
			if (url.getPathSegments().size() == 1) {
				this.table = url.getPathSegments().get(0);
				this.where = where;
				this.args = args;
			} else if (url.getPathSegments().size() != 2) {
				throw new IllegalArgumentException("Invalid URI: " + url);
			} else if (!TextUtils.isEmpty(where)) {
				throw new UnsupportedOperationException("WHERE clause not supported: " + url);
			} else {
				this.table = url.getPathSegments().get(0);
				this.where = "_id=" + ContentUris.parseId(url);                
				this.args = null;
			}
		}

		SqlArguments(Uri url) {
			if (url.getPathSegments().size() == 1) {
				table = url.getPathSegments().get(0);
				where = null;
				args = null;
			} else {
				throw new IllegalArgumentException("Invalid URI: " + url);
			}
		}
	}
}
