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

import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Process;

import com.cognizant.trumobi.PersonaIconShader.CompiledIconShader;
import com.cognizant.trumobi.catalogue.PersonaAppCatalogueFilters;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.externaladapter.ExternalAdapterRegistrationClass;
import com.cognizant.trumobi.log.PersonaLog;
import com.cognizant.trumobi.persona.PersonaMainActivity;

/**
 * Maintains in-memory state of the PersonaLauncher. It is expected that there
 * should be only one PersonaLauncherModel object held in a static. Also provide
 * APIs for updating the database state for the PersonaLauncher.
 */

/**
 * 
 * KEYCODE 			AUTHOR 			PURPOSE
 * PIMSettings 		290661
 * DesktopLoaderCrash 290778       Multi Crash issue fixed
 * //PIM App Mask	 290778		  //PIM App masking implemented in All apps view
 */
public class PersonaLauncherModel {
	static final boolean DEBUG_LOADERS = true;
	static final String LOG_TAG = "HomeLoaders";

	private static final int UI_NOTIFICATION_RATE = 4;
	private static final int DEFAULT_APPLICATIONS_NUMBER = 42;
	private static final int INITIAL_ICON_CACHE_CAPACITY = 50;

	private static final Collator sCollator = Collator.getInstance();

	private boolean mApplicationsLoaded;
	private boolean mDesktopItemsLoaded;

	private ArrayList<PersonaItemInfo> mDesktopItems;
	private ArrayList<PersonaLauncherAppWidgetInfo> mDesktopAppWidgets;
	private HashMap<Long, PersonaFolderInfo> mFolders;

	public static ArrayList<PersonaApplicationInfo> mApplications;

	public static PersonaApplicationsAdapter mApplicationsAdapter;
	private ApplicationsLoader mApplicationsLoader;
	private DesktopItemsLoader mDesktopItemsLoader;
	private Thread mApplicationsLoaderThread;
	private Thread mDesktopLoaderThread;
	private int mDesktopColumns;
	private int mDesktopRows;
	private ExternalAdapterRegistrationClass mExtAdapReg;
	finishLauncherActivityInterface finishLauncherActivityInterface;
	private final HashMap<ComponentName, PersonaApplicationInfo> mAppInfoCache = new HashMap<ComponentName, PersonaApplicationInfo>(
			INITIAL_ICON_CACHE_CAPACITY);

	private static String compiledIconShaderName;
	private static CompiledIconShader compiledIconShader;

	synchronized void abortLoaders() {
		if (DEBUG_LOADERS)
			PersonaLog.d(LOG_TAG, "aborting loaders");

		if (mApplicationsLoader != null && mApplicationsLoader.isRunning()) {
			if (DEBUG_LOADERS)
				PersonaLog.d(LOG_TAG, "  --> aborting applications loader");
			mApplicationsLoader.stop();
		}

		if (mDesktopItemsLoader != null && mDesktopItemsLoader.isRunning()) {
			if (DEBUG_LOADERS)
				PersonaLog.d(LOG_TAG, "  --> aborting workspace loader");
			mDesktopItemsLoader.stop();
			mDesktopItemsLoaded = false;
		}
	}

	/**
	 * Drop our cache of components to their lables & icons. We do this from
	 * PersonaLauncher when applications are added/removed. It's a bit overkill,
	 * but it's a rare operation anyway.
	 */
	synchronized void dropApplicationCache() {
		mAppInfoCache.clear();
	}

	/**
	 * Loads the list of installed applications in mApplications.
	 * 
	 * @return true if the applications loader must be started (see
	 *         startApplicationsLoader()), false otherwise.
	 */
	synchronized boolean loadApplications(boolean isLaunching,
			PersonaLauncher personaLauncher, boolean localeChanged) {

		if (DEBUG_LOADERS)
			PersonaLog.d(LOG_TAG, "load applications");

		if (isLaunching && mApplicationsLoaded && !localeChanged) {
			mApplicationsAdapter = new PersonaApplicationsAdapter(
					personaLauncher, mApplications, PersonaAppCatalogueFilters
							.getInstance().getDrawerFilter());
			if (DEBUG_LOADERS)
				PersonaLog.d(LOG_TAG, "  --> applications loaded, return");
			return false;
		}

		stopAndWaitForApplicationsLoader();

		if (localeChanged) {
			dropApplicationCache();
		}

		if (mApplicationsAdapter == null || isLaunching || localeChanged) {
			mApplications = new ArrayList<PersonaApplicationInfo>(
					DEFAULT_APPLICATIONS_NUMBER);
			mApplicationsAdapter = new PersonaApplicationsAdapter(
					personaLauncher, mApplications, PersonaAppCatalogueFilters
							.getInstance().getDrawerFilter());
		}

		mApplicationsLoaded = false;

		if (!isLaunching) {
			startApplicationsLoaderLocked(personaLauncher, false);
			return false;
		}

		return true;
	}

	private synchronized void stopAndWaitForApplicationsLoader() {
		if (mApplicationsLoader != null && mApplicationsLoader.isRunning()) {
			if (DEBUG_LOADERS) {
				PersonaLog.d(LOG_TAG, "  --> wait for applications loader ("
						+ mApplicationsLoader.mId + ")");
			}

			mApplicationsLoader.stop();
			// Wait for the currently running thread to finish, this can take a
			// little
			// time but it should be well below the timeout limit
			try {
				mApplicationsLoaderThread.join();
			} catch (InterruptedException e) {
				PersonaLog.e(LOG_TAG,
						"mApplicationsLoaderThread didn't exit in time");
			}
		}
	}

	private synchronized void startApplicationsLoader(
			PersonaLauncher personaLauncher, boolean isLaunching) {
		if (DEBUG_LOADERS)
			PersonaLog
					.d(LOG_TAG, "  --> starting applications loader unlocked");
		startApplicationsLoaderLocked(personaLauncher, isLaunching);
	}

	private void startApplicationsLoaderLocked(PersonaLauncher personaLauncher,
			boolean isLaunching) {
		if (DEBUG_LOADERS)
			PersonaLog.d(LOG_TAG, "  --> starting applications loader");

		stopAndWaitForApplicationsLoader();

		mApplicationsLoader = new ApplicationsLoader(personaLauncher,
				isLaunching);
		mApplicationsLoaderThread = new Thread(mApplicationsLoader,
				"Applications Loader");
		mApplicationsLoaderThread.start();
	}

	synchronized void addPackage(PersonaLauncher personaLauncher,
			String packageName) {
		if (mApplicationsLoader != null && mApplicationsLoader.isRunning()) {
			startApplicationsLoaderLocked(personaLauncher, false);
			return;
		}

		if (packageName != null && packageName.length() > 0
				&& mApplicationsAdapter != null) {
			final PackageManager packageManager = personaLauncher
					.getPackageManager();
			final List<ResolveInfo> matches = findActivitiesForPackage(
					packageManager, packageName);

			if (matches.size() > 0) {
				final PersonaApplicationsAdapter adapter = mApplicationsAdapter;
				final HashMap<ComponentName, PersonaApplicationInfo> cache = mAppInfoCache;

				for (ResolveInfo info : matches) {
					adapter.setNotifyOnChange(false);
					
					
					adapter.add(makeAndCacheApplicationInfo(packageManager,
							cache, info, personaLauncher));
				}
				
				PersonaLog.e(PersonaLauncher.class.getSimpleName(),"adapter.getCount(): "+adapter.getCount());
				adapter.updateDataSet();

				// adapter.sort(new ApplicationInfoComparator());
				// adapter.notifyDataSetChanged();
			}
		}
	}

	@SuppressWarnings("static-access")
	synchronized void removePackage(PersonaLauncher personaLauncher,
			String packageName) {
		// for add/remove Package, we need use applications adapter's "full"
		// list.

		if (mApplicationsLoader != null && mApplicationsLoader.isRunning()) {
			dropApplicationCache(); // TODO: this could be optimized
			startApplicationsLoaderLocked(personaLauncher, false);
			return;
		}

		if (packageName != null && packageName.length() > 0
				&& mApplicationsAdapter != null) {
			final PersonaApplicationsAdapter adapter = mApplicationsAdapter;

			final List<PersonaApplicationInfo> toRemove = new ArrayList<PersonaApplicationInfo>();
			final ArrayList<PersonaApplicationInfo> allItems = adapter.allItems;
			final int count = allItems.size();

			for (int i = 0; i < count; i++) {
				final PersonaApplicationInfo personaApplicationInfo = allItems
						.get(i);
				final Intent intent = personaApplicationInfo.intent;
				final ComponentName component = intent.getComponent();
				if (packageName.equals(component.getPackageName())) {
					toRemove.add(personaApplicationInfo);
				}
			}

			final HashMap<ComponentName, PersonaApplicationInfo> cache = mAppInfoCache;
			for (PersonaApplicationInfo info : toRemove) {
				adapter.setNotifyOnChange(false);
				adapter.remove(info);
				cache.remove(info.intent.getComponent());
			}

			if (toRemove.size() > 0) {
				adapter.updateDataSet();
				// adapter.sort(new ApplicationInfoComparator());
				// adapter.notifyDataSetChanged();
			}
		}
	}

	synchronized void updatePackage(PersonaLauncher personaLauncher,
			String packageName) {
		if (mApplicationsLoader != null && mApplicationsLoader.isRunning()) {
			startApplicationsLoaderLocked(personaLauncher, false);
			return;
		}

		if (packageName != null && packageName.length() > 0
				&& mApplicationsAdapter != null) {
			final PackageManager packageManager = personaLauncher
					.getPackageManager();
			final PersonaApplicationsAdapter adapter = mApplicationsAdapter;

			final List<ResolveInfo> matches = findActivitiesForPackage(
					packageManager, packageName);
			final int count = matches.size();

			boolean changed = false;

			for (int i = 0; i < count; i++) {
				final ResolveInfo info = matches.get(i);
				final PersonaApplicationInfo personaApplicationInfo = findIntent(
						adapter, info.activityInfo.applicationInfo.packageName,
						info.activityInfo.name);
				if (personaApplicationInfo != null) {
					updateAndCacheApplicationInfo(packageManager, info,
							personaApplicationInfo, personaLauncher);
					changed = true;
				}
			}

			if (syncLocked(personaLauncher, packageName))
				changed = true;

			if (changed) {
				adapter.updateDataSet();
				// adapter.sort(new ApplicationInfoComparator());
				// adapter.notifyDataSetChanged();
			}
		}
	}

	private void updateAndCacheApplicationInfo(PackageManager packageManager,
			ResolveInfo info, PersonaApplicationInfo personaApplicationInfo,
			Context context) {

		updateApplicationInfoTitleAndIcon(packageManager, info,
				personaApplicationInfo, context);

		ComponentName componentName = new ComponentName(
				info.activityInfo.applicationInfo.packageName,
				info.activityInfo.name);
		mAppInfoCache.put(componentName, personaApplicationInfo);
	}

	synchronized void syncPackage(PersonaLauncher personaLauncher,
			String packageName) {
		if (mApplicationsLoader != null && mApplicationsLoader.isRunning()) {
			startApplicationsLoaderLocked(personaLauncher, false);
			return;
		}

		if (packageName != null && packageName.length() > 0
				&& mApplicationsAdapter != null) {
			if (syncLocked(personaLauncher, packageName)) {
				final PersonaApplicationsAdapter adapter = mApplicationsAdapter;
				adapter.updateDataSet();
				// adapter.sort(new ApplicationInfoComparator());
				// adapter.notifyDataSetChanged();
			}
		}
	}

	private boolean syncLocked(PersonaLauncher personaLauncher,
			String packageName) {
		final PackageManager packageManager = personaLauncher
				.getPackageManager();
		final List<ResolveInfo> matches = findActivitiesForPackage(
				packageManager, packageName);

		if (matches.size() > 0 && mApplicationsAdapter != null) {
			final PersonaApplicationsAdapter adapter = mApplicationsAdapter;

			// Find disabled activities and remove them from the adapter
			boolean removed = removeDisabledActivities(packageName, matches,
					adapter);
			// Find enable activities and add them to the adapter
			// Also updates existing activities with new labels/icons
			boolean added = addEnabledAndUpdateActivities(matches, adapter,
					personaLauncher);

			return added || removed;
		}

		return false;
	}

	private static List<ResolveInfo> findActivitiesForPackage(
			PackageManager packageManager, String packageName) {

		final Intent mainIntent = new Intent("Email", null);
		mainIntent.addCategory("android.intent.category.PIM");

		final List<ResolveInfo> apps = packageManager.queryIntentActivities(
				mainIntent, 0);
		final List<ResolveInfo> matches = new ArrayList<ResolveInfo>();

		if (apps != null) {
			// Find all activities that match the packageName
			int count = apps.size();
			for (int i = 0; i < count; i++) {
				final ResolveInfo info = apps.get(i);
				final ActivityInfo activityInfo = info.activityInfo;
				if (packageName.equals(activityInfo.packageName)) {
					matches.add(info);
				}
			}
		}

		return matches;
	}

	private static List<ResolveInfo> findActivitiesForPackageTest(
			PackageManager packageManager, String packageName) {

		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_DEFAULT);

		final List<ResolveInfo> apps = packageManager.queryIntentActivities(
				mainIntent, 0);
		final List<ResolveInfo> matches = new ArrayList<ResolveInfo>();

		if (apps != null) {
			// Find all activities that match the packageName
			int count = apps.size();
			for (int i = 0; i < count; i++) {
				final ResolveInfo info = apps.get(i);
				final ActivityInfo activityInfo = info.activityInfo;
				if (packageName.equals(activityInfo.packageName)) {
					matches.add(info);
				}
			}
		}

		return matches;
	}
	//PIM App Mask
	private boolean addEnabledAndUpdateActivities(List<ResolveInfo> matches,
			PersonaApplicationsAdapter adapter, PersonaLauncher personaLauncher) {

		final List<PersonaApplicationInfo> toAdd = new ArrayList<PersonaApplicationInfo>();
		final int count = matches.size();

		boolean changed = false;

		for (int i = 0; i < count; i++) {
			final ResolveInfo info = matches.get(i);
			final PersonaApplicationInfo personaApplicationInfo = findIntent(
					adapter, info.activityInfo.applicationInfo.packageName,
					info.activityInfo.name);
			if (personaApplicationInfo == null) {
				
				/*boolean showConnect = false ;
				
				if(PersonaMainActivity.isRovaPoliciesOn)
				{
					//mExtAdapReg = ExternalAdapterRegistrationClass.getInstance(CognizantEmail.getAppContext());
					// showConnect = mExtAdapReg.getExternalPIMSettingsInfo().bShowEmail;
					 showConnect = true;			 
				}*/
			
				if(!PersonaMainActivity.isCertificateBasedAuth || !(info.activityInfo.name.equals("com.quintech.connect.activities.Connect_MainActivity")))
				
					{
				toAdd.add(makeAndCacheApplicationInfo(
						personaLauncher.getPackageManager(), mAppInfoCache,
						info, personaLauncher));
				changed = true;
				
					}
			} else {
				updateAndCacheApplicationInfo(
						personaLauncher.getPackageManager(), info,
						personaApplicationInfo, personaLauncher);
				changed = true;
			}
		}

		for (PersonaApplicationInfo info : toAdd) {
			adapter.setNotifyOnChange(false);
			adapter.add(info);
		}

		return changed;
	}

	private boolean removeDisabledActivities(String packageName,
			List<ResolveInfo> matches, PersonaApplicationsAdapter adapter) {

		
		PersonaLog.e(LOG_TAG, "=================removeDisabledActivities================");
		final List<PersonaApplicationInfo> toRemove = new ArrayList<PersonaApplicationInfo>();
		final int count = adapter.getCount();

		boolean changed = false;

		for (int i = 0; i < count; i++) {
			final PersonaApplicationInfo personaApplicationInfo = adapter
					.getItem(i);
			final Intent intent = personaApplicationInfo.intent;
			final ComponentName component = intent.getComponent();
			if (packageName.equals(component.getPackageName())) {
				if (!findIntent(matches, component)) {
					toRemove.add(personaApplicationInfo);
					changed = true;
				}
			}
		}

		final HashMap<ComponentName, PersonaApplicationInfo> cache = mAppInfoCache;
		for (PersonaApplicationInfo info : toRemove) {
			adapter.setNotifyOnChange(false);
			adapter.remove(info);
			cache.remove(info.intent.getComponent());
		}

		return changed;
	}

	private static PersonaApplicationInfo findIntent(
			PersonaApplicationsAdapter adapter, String packageName, String name) {

		final int count = adapter.getCount();
		for (int i = 0; i < count; i++) {
			final PersonaApplicationInfo personaApplicationInfo = adapter
					.getItem(i);
			final Intent intent = personaApplicationInfo.intent;
			final ComponentName component = intent.getComponent();
			if (packageName.equals(component.getPackageName())
					&& name.equals(component.getClassName())) {
				return personaApplicationInfo;
			}
		}

		return null;
	}

	private static boolean findIntent(List<ResolveInfo> apps,
			ComponentName component) {
		final String className = component.getClassName();
		for (ResolveInfo info : apps) {
			final ActivityInfo activityInfo = info.activityInfo;
			if (activityInfo.name.equals(className)) {
				return true;
			}
		}
		return false;
	}

	Drawable getApplicationInfoIcon(PackageManager manager,
			PersonaApplicationInfo info, Context context) {
		final ResolveInfo resolveInfo = manager.resolveActivity(info.intent, 0);
		if (resolveInfo == null || info.customIcon) {
			return null;
		}
		ComponentName componentName = new ComponentName(
				resolveInfo.activityInfo.applicationInfo.packageName,
				resolveInfo.activityInfo.name);
		PersonaApplicationInfo application = mAppInfoCache.get(componentName);
		if (application == null) {
			// return resolveInfo.activityInfo.loadIcon(manager);
			return getIcon(manager, context, resolveInfo.activityInfo);
		}

		return application.icon;
	}

	private static PersonaApplicationInfo makeAndCacheApplicationInfo(
			PackageManager manager,
			HashMap<ComponentName, PersonaApplicationInfo> appInfoCache,
			ResolveInfo info, Context context) {

		ComponentName componentName = new ComponentName(
				info.activityInfo.applicationInfo.packageName,
				info.activityInfo.name);
		PersonaApplicationInfo application = appInfoCache.get(componentName);

		if (application == null) {
			application = new PersonaApplicationInfo();
			application.container = PersonaItemInfo.NO_ID;

			updateApplicationInfoTitleAndIcon(manager, info, application,
					context);

			/*
			 * application.setActivity(componentName,
			 * Intent.FLAG_ACTIVITY_NEW_TASK |
			 * Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			 */

			application.setActivityForPersonaApps(componentName, 0);

			appInfoCache.put(componentName, application);
		}

		return application;
	}

	private static void updateApplicationInfoTitleAndIcon(
			PackageManager manager, ResolveInfo info,
			PersonaApplicationInfo application, Context context) {

		application.title = info.loadLabel(manager);
		if (application.title == null) {
			application.title = info.activityInfo.name;

		}

		if (info.activityInfo.name
				.contains("com.cognizant.trumobi.em.activity.EmWelcome")) {

			application.title = "Email";
			application.icon = context.getResources().getDrawable(
					R.drawable.pr_email_icon);
		}

		else if (info.activityInfo.name
				.contains("com.cognizant.trumobi.contacts.ContactsParentActivity")) { // 383038
																						// contacts
																						// intent

			application.title = "Contacts";
			application.icon = context.getResources().getDrawable(
					R.drawable.pr_contacts_icon);
		} else if (info.activityInfo.name
				.contains("com.cognizant.trumobi.calendar.view.CalendarMainActivity")) {

			application.title = "Calendar";
			application.icon = context.getResources().getDrawable(
					R.drawable.pr_calendar_icon);
		} else if (info.activityInfo.name
				.contains("com.cognizant.trumobi.container.activity.SecAppHome")) {

			application.title = "File Viewer";

			application.icon = context.getResources().getDrawable(
					R.drawable.pr_file_viewer_icon);

		} else if (info.activityInfo.name
				.contains("com.cognizant.trumobi.securebrowser.SB_MainActivity")) {

			application.title = "Browser";
			application.icon = context.getResources().getDrawable(
					R.drawable.pr_browser_icon);
		} else if ( info.activityInfo.name
				.contains("com.quintech.connect.activities") ) {

			application.title = "Connect";
			application.icon = context.getResources().getDrawable(
					R.drawable.pr_connect_icon);
		} else if (info.activityInfo.name
				.contains("com.cognizant.trumobi.dialer.DialerParentActivity")) {

			application.title = "Dialer";
			application.icon = context.getResources().getDrawable(
					R.drawable.pr_dialer_icon);
		} else if (info.activityInfo.name
				.contains("com.cognizant.trumobi.messenger.sms.SmsListdisplay")) {

			application.title = "Messenger";
			application.icon = context.getResources().getDrawable(
					R.drawable.pr_messenger_app_icon);
		}

		else if (info.activityInfo.name
				.contains("com.cognizant.trumobi.persona.settings.PersonaSettings")) { // PIM_REQ

			application.title = "Settings";
			application.icon = context.getResources().getDrawable(
					R.drawable.pr_settings_app_icon);
		}/* else {

			application.title = "";

			application.icon = context.getResources().getDrawable(
					R.drawable.pr_app_icon);

		}*/

		application.filtered = false;
	}

	private static final AtomicInteger sAppsLoaderCount = new AtomicInteger(1);
	private static final AtomicInteger sWorkspaceLoaderCount = new AtomicInteger(
			1);

	private class ApplicationsLoader implements Runnable {
		private final WeakReference<PersonaLauncher> mLauncher;

		private volatile boolean mStopped;
		private volatile boolean mRunning;
		private final boolean mIsLaunching;
		private final int mId;

		ApplicationsLoader(PersonaLauncher personaLauncher, boolean isLaunching) {
			mIsLaunching = isLaunching;
			mLauncher = new WeakReference<PersonaLauncher>(personaLauncher);
			mRunning = true;
			mId = sAppsLoaderCount.getAndIncrement();
		}

		void stop() {
			mStopped = true;
		}

		boolean isRunning() {
			return mRunning;
		}

		public void run() {
			if (DEBUG_LOADERS)
				PersonaLog.d(LOG_TAG, "  ----> running applications loader ("
						+ mId + ")");

			// Elevate priority when Home launches for the first time to avoid
			// starving at boot time. Staring at a blank home is not cool.
			android.os.Process
					.setThreadPriority(mIsLaunching ? Process.THREAD_PRIORITY_DEFAULT
							: Process.THREAD_PRIORITY_BACKGROUND);

			final Intent mainIntent = new Intent("Email", null);
			mainIntent.addCategory("android.intent.category.PIM");

			final PersonaLauncher personaLauncher = mLauncher.get();
			final PackageManager manager = personaLauncher.getPackageManager();
			final List<ResolveInfo> apps = manager.queryIntentActivities(
					mainIntent, 0);

			if (apps != null && !mStopped) {
				final int count = apps.size();
				// Can be set to null on the UI thread by the unbind() method
				// Do not access without checking for null first
				final PersonaApplicationsAdapter applicationList = mApplicationsAdapter;

				ChangeNotifier action = new ChangeNotifier(applicationList,
						true);
				final HashMap<ComponentName, PersonaApplicationInfo> appInfoCache = mAppInfoCache;
				
				boolean showConnect = false ;

				if(PersonaMainActivity.isRovaPoliciesOn)
				{
					// PIMSettings

					mExtAdapReg = ExternalAdapterRegistrationClass
							.getInstance(Email.getAppContext());
					// showConnect = mExtAdapReg.getExternalPIMSettingsInfo().bShowEmail;
					 showConnect = true;			 
				}
			
				for (int i = 0; i < count && !mStopped; i++) {
					ResolveInfo info = null;
					PersonaApplicationInfo application = null;
					if (showConnect
							|| !apps.get(i).activityInfo.name
									.equals("com.quintech.connect.activities.Connect_MainActivity")) {
						info = apps.get(i);

						application = makeAndCacheApplicationInfo(manager,
								appInfoCache, info, personaLauncher);

						if (action.add(application, info) && !mStopped) {
							personaLauncher.runOnUiThread(action);
							action = new ChangeNotifier(applicationList, false);
						}
					}
				}

				personaLauncher.runOnUiThread(action);
			}

			/*
			 * If we've made it this far and mStopped isn't set, we've
			 * successfully loaded applications. Otherwise, applications aren't
			 * loaded.
			 */
			mApplicationsLoaded = !mStopped;

			if (mStopped) {
				if (DEBUG_LOADERS)
					PersonaLog
							.d(LOG_TAG, "  ----> applications loader stopped ("
									+ mId + ")");
			}
			mRunning = false;
		}
	}

	private static class ChangeNotifier implements Runnable {
		private final PersonaApplicationsAdapter mApplicationList;
		private final ArrayList<PersonaApplicationInfo> mBuffer;

		private boolean mFirst = true;

		ChangeNotifier(PersonaApplicationsAdapter applicationList, boolean first) {
			mApplicationList = applicationList;
			mFirst = first;
			mBuffer = new ArrayList<PersonaApplicationInfo>(
					UI_NOTIFICATION_RATE);
		}

		public void run() {
			final PersonaApplicationsAdapter applicationList = mApplicationList;
			// Can be set to null on the UI thread by the unbind() method
			if (applicationList == null)
				return;

			if (mFirst) {
				applicationList.setNotifyOnChange(false);
				applicationList.clear();
				if (DEBUG_LOADERS)
					PersonaLog.d(LOG_TAG, "  ----> cleared application list");
				mFirst = false;
			}

			final ArrayList<PersonaApplicationInfo> buffer = mBuffer;
			final int count = buffer.size();

			for (int i = 0; i < count; i++) {
				applicationList.setNotifyOnChange(false);
				applicationList.add(buffer.get(i));
			}

			buffer.clear();

			applicationList.updateDataSet();
			// applicationList.sort(new ApplicationInfoComparator());
			// applicationList.notifyDataSetChanged();
		}

		boolean add(PersonaApplicationInfo application, ResolveInfo info) {
			final ArrayList<PersonaApplicationInfo> buffer = mBuffer;
			// PIMSettings
			if (info != null) {
				if (info.activityInfo.packageName.contains("com.cognizant"))
					buffer.add(application);
			}
			return buffer.size() >= UI_NOTIFICATION_RATE;
		}
	}

	static class ApplicationInfoComparator implements
			Comparator<PersonaApplicationInfo> {
		public final int compare(PersonaApplicationInfo a,
				PersonaApplicationInfo b) {
			return sCollator.compare(a.title.toString(), b.title.toString());
		}
	}
	//DesktopLoaderCrash
	public boolean isDesktopLoaded() {
		try {
			if (mDesktopItems.size() == 0)
				return false;
		} catch (Exception e) {
		}
		return mDesktopItems != null && mDesktopAppWidgets != null
				&& mDesktopItemsLoaded;
	}

	/**
	 * Loads all of the items on the desktop, in folders, or in the dock. These
	 * can be apps, shortcuts or widgets
	 */
	void loadUserItems(boolean isLaunching, PersonaLauncher personaLauncher,
			boolean localeChanged, boolean loadApplications) {
		if (DEBUG_LOADERS)
			PersonaLog.d(LOG_TAG, "loading user items in "
					+ Thread.currentThread().toString());
		// ADW: load columns/rows settings
		mDesktopRows = PersonaAlmostNexusSettingsHelper
				.getDesktopRows(personaLauncher);
		mDesktopColumns = PersonaAlmostNexusSettingsHelper
				.getDesktopColumns(personaLauncher);
		if (isLaunching && isDesktopLoaded()) {
			if (DEBUG_LOADERS)
				PersonaLog.d(LOG_TAG, "  --> items loaded, return");
			if (loadApplications)
				startApplicationsLoader(personaLauncher, true);
			// if (SystemProperties.get("debug.launcher.ignore-cache", "") ==
			// "") {
			// We have already loaded our data from the DB
			if (DEBUG_LOADERS)
				PersonaLog.d(LOG_TAG,
						"  --> loading from cache: " + mDesktopItems.size()
								+ ", " + mDesktopAppWidgets.size());
			personaLauncher.onDesktopItemsLoaded(mDesktopItems,
					mDesktopAppWidgets);
			return;
			// }
			// else
			// {
			// d(LOG_TAG, "  ----> debug: forcing reload of workspace");
			// }
		}

		if (mDesktopItemsLoader != null && mDesktopItemsLoader.isRunning()) {
			if (DEBUG_LOADERS)
				PersonaLog.d(LOG_TAG, "  --> stopping workspace loader");
			mDesktopItemsLoader.stop();
			// Wait for the currently running thread to finish, this can take a
			// little
			// time but it should be well below the timeout limit
			try {
				mDesktopLoaderThread.join();
			} catch (InterruptedException e) {
				PersonaLog.e(LOG_TAG,
						"mDesktopLoaderThread didn't exit in time");
			}

			// If the thread we are interrupting was tasked to load the list of
			// applications make sure we keep that information in the new loader
			// spawned below
			// note: we don't apply this to localeChanged because the thread can
			// only be stopped *after* the localeChanged handling has occured
			loadApplications = mDesktopItemsLoader.mLoadApplications;
		}

		if (DEBUG_LOADERS)
			PersonaLog.d(LOG_TAG, "  --> starting workspace loader");
		mDesktopItemsLoaded = false;
		mDesktopItemsLoader = new DesktopItemsLoader(personaLauncher,
				localeChanged, loadApplications, isLaunching);
		mDesktopLoaderThread = new Thread(mDesktopItemsLoader,
				"Desktop Items Loader");
		mDesktopLoaderThread.start();
	}

	private static String getLabel(PackageManager manager,
			ActivityInfo activityInfo) {
		String label = activityInfo.loadLabel(manager).toString();
		if (label == null) {
			label = manager.getApplicationLabel(activityInfo.applicationInfo)
					.toString();
			if (label == null) {
				label = activityInfo.name;
			}
		}
		return label;
	}

	static PersonaApplicationInfo loadApplicationInfoById(Context context,
			long Id) {

		final ContentResolver contentResolver = context.getContentResolver();
		final PackageManager manager = context.getPackageManager();
		final Cursor c = contentResolver.query(
				PersonaLauncherSettings.Favorites.CONTENT_URI, null, null,
				null, null);
		if (c == null)
			return null;
		try {
			c.moveToFirst();
			while (!c.isAfterLast()) {
				final int idIndex = c
						.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites._ID);
				final long id = c.getLong(idIndex);
				if (id == Id) {

					final int intentIndex = c
							.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.INTENT);
					final int titleIndex = c
							.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.TITLE);
					final int iconTypeIndex = c
							.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.ICON_TYPE);
					final int iconIndex = c
							.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.ICON);
					final int iconPackageIndex = c
							.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.ICON_PACKAGE);
					final int iconResourceIndex = c
							.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.ICON_RESOURCE);
					final int containerIndex = c
							.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.CONTAINER);
					final int itemTypeIndex = c
							.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.ITEM_TYPE);
					final int screenIndex = c
							.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.SCREEN);
					final int cellXIndex = c
							.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.CELLX);
					final int cellYIndex = c
							.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.CELLY);

					int container;
					Intent intent;

					final int itemType = c.getInt(itemTypeIndex);
					if (itemType == PersonaLauncherSettings.Favorites.ITEM_TYPE_APPLICATION
							|| itemType == PersonaLauncherSettings.Favorites.ITEM_TYPE_SHORTCUT) {
						String intentDescription = c.getString(intentIndex);
						try {
							intent = Intent.parseUri(intentDescription, 0);
						} catch (java.net.URISyntaxException e) {
							return null;
						}

						PersonaApplicationInfo info;
						if (itemType == PersonaLauncherSettings.Favorites.ITEM_TYPE_APPLICATION) {
							info = getApplicationInfo(manager, intent, context);
						} else {
							info = getApplicationInfoShortcut(c, context,
									iconTypeIndex, iconPackageIndex,
									iconResourceIndex, iconIndex);
						}

						if (info == null) {
							info = new PersonaApplicationInfo();
							info.icon = manager.getDefaultActivityIcon();
						}

						if (info != null) {
							info.title = c.getString(titleIndex);
							info.intent = intent;

							info.id = id;
							container = c.getInt(containerIndex);
							info.container = container;
							info.screen = c.getInt(screenIndex);
							info.cellX = c.getInt(cellXIndex);
							info.cellY = c.getInt(cellYIndex);
						}
						return info;
					}
				}
				c.moveToNext();
			}
		} finally {
			c.close();
		}
		return null;
	}

	private class DesktopItemsLoader implements Runnable {
		private volatile boolean mStopped;
		private volatile boolean mFinished;

		private final WeakReference<PersonaLauncher> mLauncher;
		private final boolean mLocaleChanged;
		private final boolean mLoadApplications;
		private final boolean mIsLaunching;
		private final int mId;

		DesktopItemsLoader(PersonaLauncher personaLauncher,
				boolean localeChanged, boolean loadApplications,
				boolean isLaunching) {
			mLoadApplications = loadApplications;
			mIsLaunching = isLaunching;
			mLauncher = new WeakReference<PersonaLauncher>(personaLauncher);
			mLocaleChanged = localeChanged;
			mId = sWorkspaceLoaderCount.getAndIncrement();
			mFinished = false;
		}

		void stop() {
			PersonaLog.d(LOG_TAG, "  ----> workspace loader " + mId
					+ " stopped from " + Thread.currentThread().toString());
			mStopped = true;
		}

		boolean isRunning() {
			return !mFinished;
		}

		public void run() {
			assert (!mFinished); // can only run once
			load_workspace();
			mFinished = true;
		}

		private void load_workspace() {
			if (DEBUG_LOADERS)
				PersonaLog.d(LOG_TAG, "  ----> running workspace loader ("
						+ mId + ")");

			android.os.Process
					.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);

			final PersonaLauncher personaLauncher = mLauncher.get();
			final ContentResolver contentResolver = personaLauncher
					.getContentResolver();
			final PackageManager manager = personaLauncher.getPackageManager();

			if (mLocaleChanged) {
				updateShortcutLabels(contentResolver, manager);
			}

			final ArrayList<PersonaItemInfo> desktopItems = new ArrayList<PersonaItemInfo>();
			final ArrayList<PersonaLauncherAppWidgetInfo> desktopAppWidgets = new ArrayList<PersonaLauncherAppWidgetInfo>();
			final HashMap<Long, PersonaFolderInfo> folders = new HashMap<Long, PersonaFolderInfo>();

			Cursor c = null;
			try {
				c = contentResolver.query(
						PersonaLauncherSettings.Favorites.CONTENT_URI, null,
						null, null, null);

				final int idIndex = c
						.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites._ID);
				final int intentIndex = c
						.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.INTENT);
				final int titleIndex = c
						.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.TITLE);
				final int iconTypeIndex = c
						.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.ICON_TYPE);
				final int iconIndex = c
						.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.ICON);
				final int iconPackageIndex = c
						.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.ICON_PACKAGE);
				final int iconResourceIndex = c
						.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.ICON_RESOURCE);
				final int containerIndex = c
						.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.CONTAINER);
				final int itemTypeIndex = c
						.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.ITEM_TYPE);
				final int appWidgetIdIndex = c
						.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.APPWIDGET_ID);
				final int screenIndex = c
						.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.SCREEN);
				final int cellXIndex = c
						.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.CELLX);
				final int cellYIndex = c
						.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.CELLY);
				final int spanXIndex = c
						.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.SPANX);
				final int spanYIndex = c
						.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.SPANY);
				final int uriIndex = c
						.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.URI);
				final int displayModeIndex = c
						.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.DISPLAY_MODE);

				PersonaApplicationInfo info;
				String intentDescription;
				PersonaWidget widgetInfo;
				PersonaLauncherAppWidgetInfo appWidgetInfo;
				int container;
				long id;
				Intent intent;

				while (!mStopped && c.moveToNext()) {
					try {
						int itemType = c.getInt(itemTypeIndex);
						switch (itemType) {
						case PersonaLauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
						case PersonaLauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
							intentDescription = c.getString(intentIndex);
							try {
								intent = Intent.parseUri(intentDescription, 0);
							} catch (java.net.URISyntaxException e) {
								continue;
							}
							if (itemType == PersonaLauncherSettings.Favorites.ITEM_TYPE_APPLICATION) {
								info = getApplicationInfo(manager, intent,
										personaLauncher);
							} else {
								info = getApplicationInfoShortcut(c,
										personaLauncher, iconTypeIndex,
										iconPackageIndex, iconResourceIndex,
										iconIndex);
							}

							if (info == null) {
								info = new PersonaApplicationInfo();
								info.icon = manager.getDefaultActivityIcon();
							}

							if (info != null) {
								info.title = c.getString(titleIndex);
								info.intent = intent;

								info.id = c.getLong(idIndex);
								container = c.getInt(containerIndex);
								info.container = container;
								info.screen = c.getInt(screenIndex);
								info.cellX = c.getInt(cellXIndex);
								info.cellY = c.getInt(cellYIndex);

								switch (container) {
								case PersonaLauncherSettings.Favorites.CONTAINER_DESKTOP:
								case PersonaLauncherSettings.Favorites.CONTAINER_DOCKBAR:
								case PersonaLauncherSettings.Favorites.CONTAINER_LAB:
								case PersonaLauncherSettings.Favorites.CONTAINER_RAB:
								case PersonaLauncherSettings.Favorites.CONTAINER_LAB2:
								case PersonaLauncherSettings.Favorites.CONTAINER_RAB2:
								case PersonaLauncherSettings.Favorites.CONTAINER_MAB:
									desktopItems.add(info);
									break;
								default:
									// Item is in a user folder
									PersonaUserFolderInfo folderInfo = findOrMakeUserFolder(
											folders, container);
									folderInfo.add(info);
									break;
								}
							}
							break;
						case PersonaLauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER:

							id = c.getLong(idIndex);
							PersonaUserFolderInfo folderInfo = findOrMakeUserFolder(
									folders, id);

							folderInfo.title = c.getString(titleIndex);

							folderInfo.id = id;
							container = c.getInt(containerIndex);
							folderInfo.container = container;
							folderInfo.screen = c.getInt(screenIndex);
							folderInfo.cellX = c.getInt(cellXIndex);
							folderInfo.cellY = c.getInt(cellYIndex);

							switch (container) {
							case PersonaLauncherSettings.Favorites.CONTAINER_DESKTOP:
							case PersonaLauncherSettings.Favorites.CONTAINER_DOCKBAR:
							case PersonaLauncherSettings.Favorites.CONTAINER_LAB:
							case PersonaLauncherSettings.Favorites.CONTAINER_RAB:
							case PersonaLauncherSettings.Favorites.CONTAINER_LAB2:
							case PersonaLauncherSettings.Favorites.CONTAINER_RAB2:
							case PersonaLauncherSettings.Favorites.CONTAINER_MAB:
								desktopItems.add(folderInfo);
								break;
							}
							break;
						case PersonaLauncherSettings.Favorites.ITEM_TYPE_LIVE_FOLDER:

							id = c.getLong(idIndex);
							PersonaLiveFolderInfo personaLiveFolderInfo = findOrMakeLiveFolder(
									folders, id);

							intentDescription = c.getString(intentIndex);
							intent = null;
							if (intentDescription != null) {
								try {
									intent = Intent.parseUri(intentDescription,
											0);
								} catch (java.net.URISyntaxException e) {
									// Ignore, a live folder might not have a
									// base intent
								}
							}

							personaLiveFolderInfo.title = c
									.getString(titleIndex);
							personaLiveFolderInfo.id = id;
							container = c.getInt(containerIndex);
							personaLiveFolderInfo.container = container;
							personaLiveFolderInfo.screen = c
									.getInt(screenIndex);
							personaLiveFolderInfo.cellX = c.getInt(cellXIndex);
							personaLiveFolderInfo.cellY = c.getInt(cellYIndex);
							personaLiveFolderInfo.uri = Uri.parse(c
									.getString(uriIndex));
							personaLiveFolderInfo.baseIntent = intent;
							personaLiveFolderInfo.displayMode = c
									.getInt(displayModeIndex);

							loadLiveFolderIcon(personaLauncher, c,
									iconTypeIndex, iconPackageIndex,
									iconResourceIndex, personaLiveFolderInfo);

							switch (container) {
							case PersonaLauncherSettings.Favorites.CONTAINER_DESKTOP:
							case PersonaLauncherSettings.Favorites.CONTAINER_DOCKBAR:
							case PersonaLauncherSettings.Favorites.CONTAINER_LAB:
							case PersonaLauncherSettings.Favorites.CONTAINER_RAB:
							case PersonaLauncherSettings.Favorites.CONTAINER_LAB2:
							case PersonaLauncherSettings.Favorites.CONTAINER_RAB2:
							case PersonaLauncherSettings.Favorites.CONTAINER_MAB:
								desktopItems.add(personaLiveFolderInfo);
								break;
							}
							break;
						case PersonaLauncherSettings.Favorites.ITEM_TYPE_WIDGET_SEARCH:
							widgetInfo = PersonaWidget.makeSearch();

							container = c.getInt(containerIndex);
							if (container != PersonaLauncherSettings.Favorites.CONTAINER_DESKTOP) {
								PersonaLog
										.e(PersonaLauncher.LOG_TAG,
												"PersonaWidget found where container "
														+ "!= CONTAINER_DESKTOP  ignoring!");
								continue;
							}

							widgetInfo.id = c.getLong(idIndex);
							widgetInfo.screen = c.getInt(screenIndex);
							widgetInfo.container = container;
							widgetInfo.cellX = c.getInt(cellXIndex);
							widgetInfo.cellY = c.getInt(cellYIndex);
							widgetInfo.spanX = c.getInt(spanXIndex);
							widgetInfo.spanY = c.getInt(spanYIndex);

							desktopItems.add(widgetInfo);
							break;
						case PersonaLauncherSettings.Favorites.ITEM_TYPE_APPWIDGET:
							// Read all PersonaLauncher-specific widget details
							int appWidgetId = c.getInt(appWidgetIdIndex);
							appWidgetInfo = new PersonaLauncherAppWidgetInfo(
									appWidgetId);
							appWidgetInfo.id = c.getLong(idIndex);
							appWidgetInfo.screen = c.getInt(screenIndex);
							appWidgetInfo.cellX = c.getInt(cellXIndex);
							appWidgetInfo.cellY = c.getInt(cellYIndex);
							appWidgetInfo.spanX = c.getInt(spanXIndex);
							appWidgetInfo.spanY = c.getInt(spanYIndex);

							container = c.getInt(containerIndex);
							if (container != PersonaLauncherSettings.Favorites.CONTAINER_DESKTOP) {
								PersonaLog
										.e(PersonaLauncher.LOG_TAG,
												"PersonaWidget found where container "
														+ "!= CONTAINER_DESKTOP -- ignoring!");
								continue;
							}
							appWidgetInfo.container = c.getInt(containerIndex);

							desktopAppWidgets.add(appWidgetInfo);
							break;
						}
					} catch (Exception e) {
						PersonaLog.w(PersonaLauncher.LOG_TAG,
								"Desktop items loading interrupted:", e);
					}
				}
			}
			//DesktopLoaderCrash
			catch (Exception e) {
				if (c != null)
					c.close();
				PersonaLog.e("Before calling", "Finish");
				// finishLauncherActivityInterface = personaLauncher;
				// finishLauncherActivityInterface.finishLauncherActivty();
				
				personaLauncher.startActivity(new Intent(personaLauncher,
						PersonaMainActivity.class));
				personaLauncher.finish();
				PersonaLog.e("After calling", "Finish");
			} finally {

				if (c != null)
					c.close();
			}
			if (DEBUG_LOADERS) {
				PersonaLog.d(LOG_TAG, "  ----> workspace loader " + mId
						+ " finished loading data");
				PersonaLog.d(LOG_TAG,
						"  ----> worskpace items=" + desktopItems.size());
				PersonaLog.d(LOG_TAG, "  ----> worskpace widgets="
						+ desktopAppWidgets.size());
			}

			synchronized (PersonaLauncherModel.this) {
				if (!mStopped) {
					if (DEBUG_LOADERS) {
						PersonaLog.d(LOG_TAG,
								"  --> done loading workspace; not stopped");
					}

					// Create a copy of the lists in case the workspace loader
					// is restarted
					// and the list are cleared before the UI can go through
					// them
					final ArrayList<PersonaItemInfo> uiDesktopItems = new ArrayList<PersonaItemInfo>(
							desktopItems);
					final ArrayList<PersonaLauncherAppWidgetInfo> uiDesktopWidgets = new ArrayList<PersonaLauncherAppWidgetInfo>(
							desktopAppWidgets);

					if (!mStopped) {
						PersonaLog.d(LOG_TAG,
								"  ----> items cloned, ready to refresh UI");
						personaLauncher.runOnUiThread(new Runnable() {
							public void run() {
								if (DEBUG_LOADERS)
									PersonaLog.d(LOG_TAG,
											"  ----> onDesktopItemsLoaded()");
								personaLauncher.onDesktopItemsLoaded(
										uiDesktopItems, uiDesktopWidgets);
							}
						});
					}

					if (mLoadApplications) {
						if (DEBUG_LOADERS) {
							PersonaLog
									.d(LOG_TAG,
											"  ----> loading applications from workspace loader");
						}
						startApplicationsLoader(personaLauncher, mIsLaunching);
					}

					mDesktopItems = desktopItems;
					mDesktopAppWidgets = desktopAppWidgets;
					mFolders = folders;
					mDesktopItemsLoaded = true;
				} else {
					if (DEBUG_LOADERS)
						PersonaLog.d(LOG_TAG,
								"  ----> worskpace loader was stopped");
				}
			}
		}

		private void updateShortcutLabels(ContentResolver resolver,
				PackageManager manager) {
			Cursor c = null;

			c = resolver.query(PersonaLauncherSettings.Favorites.CONTENT_URI,
					new String[] { PersonaLauncherSettings.Favorites._ID,
							PersonaLauncherSettings.Favorites.TITLE,
							PersonaLauncherSettings.Favorites.INTENT,
							PersonaLauncherSettings.Favorites.ITEM_TYPE },
					null, null, null);

			final int idIndex = c
					.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites._ID);
			final int intentIndex = c
					.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.INTENT);
			final int itemTypeIndex = c
					.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.ITEM_TYPE);
			final int titleIndex = c
					.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.TITLE);

			// boolean changed = false;

			try {
				while (!mStopped && c.moveToNext()) {
					try {
						if (c.getInt(itemTypeIndex) != PersonaLauncherSettings.Favorites.ITEM_TYPE_APPLICATION) {
							continue;
						}

						final String intentUri = c.getString(intentIndex);
						if (intentUri != null) {
							final Intent shortcut = Intent.parseUri(intentUri,
									0);
							if (Intent.ACTION_MAIN.equals(shortcut.getAction())) {
								final ComponentName name = shortcut
										.getComponent();
								if (name != null) {
									final ActivityInfo activityInfo = manager
											.getActivityInfo(name, 0);
									final String title = c
											.getString(titleIndex);
									String label = getLabel(manager,
											activityInfo);

									if (title == null || !title.equals(label)) {
										final ContentValues values = new ContentValues();
										values.put(
												PersonaLauncherSettings.Favorites.TITLE,
												label);

										resolver.update(
												PersonaLauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION,
												values, "_id=?",
												new String[] { String.valueOf(c
														.getLong(idIndex)) });

										// changed = true;
									}
								}
							}
						}
					} catch (URISyntaxException e) {
						// Ignore
					} catch (PackageManager.NameNotFoundException e) {
						// Ignore
					}
				}
			} finally {
				c.close();
			}

			// if (changed)
			// resolver.notifyChange(Settings.Favorites.CONTENT_URI, null);
		}
	}

	private static void loadLiveFolderIcon(PersonaLauncher personaLauncher,
			Cursor c, int iconTypeIndex, int iconPackageIndex,
			int iconResourceIndex, PersonaLiveFolderInfo personaLiveFolderInfo) {

		int iconType = c.getInt(iconTypeIndex);
		switch (iconType) {
		case PersonaLauncherSettings.Favorites.ICON_TYPE_RESOURCE:
			String packageName = c.getString(iconPackageIndex);
			String resourceName = c.getString(iconResourceIndex);
			PackageManager packageManager = personaLauncher.getPackageManager();
			try {
				Resources resources = packageManager
						.getResourcesForApplication(packageName);
				final int id = resources
						.getIdentifier(resourceName, null, null);
				personaLiveFolderInfo.icon = resources.getDrawable(id);
			} catch (Exception e) {
				personaLiveFolderInfo.icon = personaLauncher.getResources()
						.getDrawable(R.drawable.pr_ic_launcher_folder);
			}
			personaLiveFolderInfo.iconResource = new Intent.ShortcutIconResource();
			personaLiveFolderInfo.iconResource.packageName = packageName;
			personaLiveFolderInfo.iconResource.resourceName = resourceName;
			break;
		default:
			personaLiveFolderInfo.icon = personaLauncher.getResources()
					.getDrawable(R.drawable.pr_ic_launcher_folder);
		}
	}

	/**
	 * Finds the user folder defined by the specified id.
	 * 
	 * @param id
	 *            The id of the folder to look for.
	 * 
	 * @return A PersonaUserFolderInfo if the folder exists or null otherwise.
	 */
	PersonaFolderInfo findFolderById(long id) {
		if (mFolders == null)
			return null;
		return mFolders.get(id);
	}

	void addFolder(PersonaFolderInfo info) {
		if (mFolders == null)
			return;
		mFolders.put(info.id, info);
	}

	/**
	 * Return an existing PersonaUserFolderInfo object if we have encountered
	 * this ID previously, or make a new one.
	 */
	private PersonaUserFolderInfo findOrMakeUserFolder(
			HashMap<Long, PersonaFolderInfo> folders, long id) {
		// See if a placeholder was created for us already
		PersonaFolderInfo personaFolderInfo = folders.get(id);
		if (personaFolderInfo == null
				|| !(personaFolderInfo instanceof PersonaUserFolderInfo)) {
			// No placeholder -- create a new instance
			personaFolderInfo = new PersonaUserFolderInfo();
			folders.put(id, personaFolderInfo);
		}
		return (PersonaUserFolderInfo) personaFolderInfo;
	}

	/**
	 * Return an existing PersonaUserFolderInfo object if we have encountered
	 * this ID previously, or make a new one.
	 */
	private PersonaLiveFolderInfo findOrMakeLiveFolder(
			HashMap<Long, PersonaFolderInfo> folders, long id) {
		// See if a placeholder was created for us already
		PersonaFolderInfo personaFolderInfo = folders.get(id);
		if (personaFolderInfo == null
				|| !(personaFolderInfo instanceof PersonaLiveFolderInfo)) {
			// No placeholder -- create a new instance
			personaFolderInfo = new PersonaLiveFolderInfo();
			folders.put(id, personaFolderInfo);
		}
		return (PersonaLiveFolderInfo) personaFolderInfo;
	}

	/**
	 * Remove the callback for the cached drawables or we leak the previous Home
	 * screen on orientation change.
	 */
	void unbind() {
		// Interrupt the applications loader before setting the adapter to null
		stopAndWaitForApplicationsLoader();
		mApplicationsAdapter = null;
		unbindAppDrawables(mApplications);
		unbindDrawables(mDesktopItems);
		unbindAppWidgetHostViews(mDesktopAppWidgets);
		unbindCachedIconDrawables();
	}

	/**
	 * Remove the callback for the cached drawables or we leak the previous Home
	 * screen on orientation change.
	 */
	private void unbindDrawables(ArrayList<PersonaItemInfo> desktopItems) {
		if (desktopItems != null) {
			final int count = desktopItems.size();
			for (int i = 0; i < count; i++) {
				PersonaItemInfo item = desktopItems.get(i);
				switch (item.itemType) {
				case PersonaLauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
				case PersonaLauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
					((PersonaApplicationInfo) item).icon.setCallback(null);
					break;
				}
			}
		}
	}

	/**
	 * Remove the callback for the cached drawables or we leak the previous Home
	 * screen on orientation change.
	 */
	private void unbindAppDrawables(
			ArrayList<PersonaApplicationInfo> applications) {
		if (applications != null) {
			final int count = applications.size();
			for (int i = 0; i < count; i++) {
				// PIMSettings
				if (applications.get(i) != null
						&& applications.get(i).icon != null)
					applications.get(i).icon.setCallback(null);
			}
		}
	}

	/**
	 * Remove any {@link PersonaLauncherAppWidgetHostView} references in our
	 * widgets.
	 */
	private void unbindAppWidgetHostViews(
			ArrayList<PersonaLauncherAppWidgetInfo> appWidgets) {
		if (appWidgets != null) {
			final int count = appWidgets.size();
			for (int i = 0; i < count; i++) {
				PersonaLauncherAppWidgetInfo launcherInfo = appWidgets.get(i);
				launcherInfo.hostView = null;
			}
		}
	}

	/**
	 * Remove the callback for the cached drawables or we leak the previous Home
	 * screen on orientation change.
	 */
	private void unbindCachedIconDrawables() {
		for (PersonaApplicationInfo appInfo : mAppInfoCache.values()) {
			// PIMSettings
			if (appInfo.icon != null)
				appInfo.icon.setCallback(null);
		}
	}

	/**
	 * Fills in the occupied structure with all of the shortcuts, apps, folders
	 * and widgets in the model.
	 */
	void findAllOccupiedCells(boolean[][] occupied, int countX, int countY,
			int screen) {
		final ArrayList<PersonaItemInfo> desktopItems = mDesktopItems;
		if (desktopItems != null) {
			final int count = desktopItems.size();
			for (int i = 0; i < count; i++) {
				// ADW: Don't load items outer current columns/rows limits
				if ((desktopItems.get(i).cellX + (desktopItems.get(i).spanX - 1)) < mDesktopColumns
						&& (desktopItems.get(i).cellY + (desktopItems.get(i).spanY - 1)) < mDesktopRows)
					addOccupiedCells(occupied, screen, desktopItems.get(i));
			}
		}

		final ArrayList<PersonaLauncherAppWidgetInfo> desktopAppWidgets = mDesktopAppWidgets;
		if (desktopAppWidgets != null) {
			final int count = desktopAppWidgets.size();
			for (int i = 0; i < count; i++) {
				addOccupiedCells(occupied, screen, desktopAppWidgets.get(i));
			}
		}
	}

	/**
	 * Add the footprint of the specified item to the occupied array
	 */
	private void addOccupiedCells(boolean[][] occupied, int screen,
			PersonaItemInfo item) {
		if (item.screen == screen) {
			for (int xx = item.cellX; xx < item.cellX + item.spanX; xx++) {
				for (int yy = item.cellY; yy < item.cellY + item.spanY; yy++) {
					if (xx < mDesktopColumns && yy < mDesktopRows)
						occupied[xx][yy] = true;
				}
			}
		}
	}

	/**
	 * @return The current list of applications
	 */
	PersonaApplicationsAdapter getApplicationsAdapter() {
		return mApplicationsAdapter;
	}

	/**
	 * Add an item to the desktop
	 * 
	 * @param info
	 */
	void addDesktopItem(PersonaItemInfo info) {
		// TODO: write to DB; also check that folder has been added to folders
		// list
		if (mDesktopItems != null)
			mDesktopItems.add(info);
	}

	/**
	 * Remove an item from the desktop
	 * 
	 * @param info
	 */
	void removeDesktopItem(PersonaItemInfo info) {
		// TODO: write to DB; figure out if we should remove folder from folders
		// list
		if (mDesktopItems != null)
			mDesktopItems.remove(info);
	}

	/**
	 * Add a widget to the desktop
	 */
	void addDesktopAppWidget(PersonaLauncherAppWidgetInfo info) {
		if (mDesktopAppWidgets != null)
			mDesktopAppWidgets.add(info);
	}

	/**
	 * Remove a widget from the desktop
	 */
	void removeDesktopAppWidget(PersonaLauncherAppWidgetInfo info) {
		if (mDesktopAppWidgets != null)
			mDesktopAppWidgets.remove(info);
	}

	/**
	 * Make an PersonaApplicationInfo object for an application
	 */
	private static PersonaApplicationInfo getApplicationInfo(
			PackageManager manager, Intent intent, Context context) {
		// ADW: Changed the check to avoid bypassing SDcard apps in froyo
		ComponentName componentName = intent.getComponent();
		if (componentName == null) {
			return null;
		}

		final ResolveInfo resolveInfo = manager.resolveActivity(intent, 0);

		final PersonaApplicationInfo info = new PersonaApplicationInfo();
		if (resolveInfo != null) {
			final ActivityInfo activityInfo = resolveInfo.activityInfo;

			info.icon = getIcon(manager, context, activityInfo);

			if (info.title == null || info.title.length() == 0) {
				info.title = activityInfo.loadLabel(manager);
			}
			if (info.title == null) {
				info.title = "";
			}
		} else {
			// ADW: add default icon for apps on SD
			info.icon = manager.getDefaultActivityIcon();
		}
		info.itemType = PersonaLauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
		return info;
	}

	/**
	 * Make an PersonaApplicationInfo object for a shortcut
	 */
	private static PersonaApplicationInfo getApplicationInfoShortcut(Cursor c,
			Context context, int iconTypeIndex, int iconPackageIndex,
			int iconResourceIndex, int iconIndex) {

		final PersonaApplicationInfo info = new PersonaApplicationInfo();
		info.itemType = PersonaLauncherSettings.Favorites.ITEM_TYPE_SHORTCUT;

		int iconType = c.getInt(iconTypeIndex);
		switch (iconType) {
		case PersonaLauncherSettings.Favorites.ICON_TYPE_RESOURCE:
			String packageName = c.getString(iconPackageIndex);
			String resourceName = c.getString(iconResourceIndex);
			PackageManager packageManager = context.getPackageManager();
			try {
				Resources resources = packageManager
						.getResourcesForApplication(packageName);
				final int id = resources
						.getIdentifier(resourceName, null, null);
				info.icon = PersonaUtilities.createIconThumbnail(
						resources.getDrawable(id), context);
			} catch (Exception e) {
				info.icon = packageManager.getDefaultActivityIcon();
			}
			info.iconResource = new Intent.ShortcutIconResource();
			info.iconResource.packageName = packageName;
			info.iconResource.resourceName = resourceName;
			info.customIcon = false;
			break;
		case PersonaLauncherSettings.Favorites.ICON_TYPE_BITMAP:
			byte[] data = c.getBlob(iconIndex);
			try {
				Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
						data.length);
				info.icon = new PersonaFastBitmapDrawable(
						PersonaUtilities.createBitmapThumbnail(bitmap, context));
			} catch (Exception e) {
				packageManager = context.getPackageManager();
				info.icon = packageManager.getDefaultActivityIcon();
			}
			info.filtered = true;
			info.customIcon = true;
			break;
		default:
			info.icon = context.getPackageManager().getDefaultActivityIcon();
			info.customIcon = false;
			break;
		}
		return info;
	}

	/**
	 * Remove an item from the in-memory representation of a user folder. Does
	 * not change the DB.
	 */
	void removeUserFolderItem(PersonaUserFolderInfo folder, PersonaItemInfo info) {
		// noinspection SuspiciousMethodCalls
		folder.contents.remove(info);
	}

	/**
	 * Removes a PersonaUserFolder from the in-memory list of folders. Does not
	 * change the DB.
	 * 
	 * @param personaUserFolderInfo
	 */
	void removeUserFolder(PersonaUserFolderInfo personaUserFolderInfo) {
		mFolders.remove(personaUserFolderInfo.id);
	}

	/**
	 * Adds an item to the DB if it was not created previously, or move it to a
	 * new <container, screen, cellX, cellY>
	 */
	static void addOrMoveItemInDatabase(Context context, PersonaItemInfo item,
			long container, int screen, int cellX, int cellY) {
		if (item.container == PersonaItemInfo.NO_ID) {
			// From all apps
			addItemToDatabase(context, item, container, screen, cellX, cellY,
					false);
		} else {
			// From somewhere else
			moveItemInDatabase(context, item, container, screen, cellX, cellY);
		}
	}

	/**
	 * Move an item in the DB to a new <container, screen, cellX, cellY>
	 */
	static void moveItemInDatabase(Context context, PersonaItemInfo item,
			long container, int screen, int cellX, int cellY) {
		item.container = container;
		item.screen = screen;
		item.cellX = cellX;
		item.cellY = cellY;

		final ContentValues values = new ContentValues();
		final ContentResolver cr = context.getContentResolver();

		values.put(PersonaLauncherSettings.Favorites.CONTAINER, item.container);
		values.put(PersonaLauncherSettings.Favorites.CELLX, item.cellX);
		values.put(PersonaLauncherSettings.Favorites.CELLY, item.cellY);
		values.put(PersonaLauncherSettings.Favorites.SCREEN, item.screen);

		cr.update(
				PersonaLauncherSettings.Favorites.getContentUri(item.id, false),
				values, null, null);
	}

	/**
	 * Returns true if the shortcuts already exists in the database. we identify
	 * a shortcut by its title and intent.
	 */
	static boolean shortcutExists(Context context, String title, Intent intent) {
		final ContentResolver cr = context.getContentResolver();
		Cursor c = cr.query(PersonaLauncherSettings.Favorites.CONTENT_URI,
				new String[] { "title", "intent" }, "title=? and intent=?",
				new String[] { title, intent.toUri(0) }, null);
		boolean result = false;
		try {
			result = c.moveToFirst();
		} finally {
			c.close();
		}
		return result;
	}

	PersonaFolderInfo getFolderById(Context context, long id) {
		final ContentResolver cr = context.getContentResolver();
		Cursor c = cr
				.query(PersonaLauncherSettings.Favorites.CONTENT_URI,
						null,
						"_id=? and (itemType=? or itemType=?)",
						new String[] {
								String.valueOf(id),
								String.valueOf(PersonaLauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER),
								String.valueOf(PersonaLauncherSettings.Favorites.ITEM_TYPE_LIVE_FOLDER) },
						null);

		try {
			if (c.moveToFirst()) {
				final int itemTypeIndex = c
						.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.ITEM_TYPE);
				final int titleIndex = c
						.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.TITLE);
				final int containerIndex = c
						.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.CONTAINER);
				final int screenIndex = c
						.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.SCREEN);
				final int cellXIndex = c
						.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.CELLX);
				final int cellYIndex = c
						.getColumnIndexOrThrow(PersonaLauncherSettings.Favorites.CELLY);

				PersonaFolderInfo personaFolderInfo = null;
				switch (c.getInt(itemTypeIndex)) {
				case PersonaLauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER:
					personaFolderInfo = findOrMakeUserFolder(mFolders, id);
					break;
				case PersonaLauncherSettings.Favorites.ITEM_TYPE_LIVE_FOLDER:
					personaFolderInfo = findOrMakeLiveFolder(mFolders, id);
					break;
				}

				personaFolderInfo.title = c.getString(titleIndex);
				personaFolderInfo.id = id;
				personaFolderInfo.container = c.getInt(containerIndex);
				personaFolderInfo.screen = c.getInt(screenIndex);
				personaFolderInfo.cellX = c.getInt(cellXIndex);
				personaFolderInfo.cellY = c.getInt(cellYIndex);

				return personaFolderInfo;
			}
		} finally {
			c.close();
		}

		return null;
	}

	/**
	 * Add an item to the database in a specified container. Sets the container,
	 * screen, cellX and cellY fields of the item. Also assigns an ID to the
	 * item.
	 */
	static void addItemToDatabase(Context context, PersonaItemInfo item,
			long container, int screen, int cellX, int cellY, boolean notify) {
		item.container = container;
		item.screen = screen;
		item.cellX = cellX;
		item.cellY = cellY;

		final ContentValues values = new ContentValues();
		final ContentResolver cr = context.getContentResolver();

		item.onAddToDatabase(values);

		Uri result = cr
				.insert(notify ? PersonaLauncherSettings.Favorites.CONTENT_URI
						: PersonaLauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION,
						values);

		if (result != null) {
			item.id = Integer.parseInt(result.getPathSegments().get(1));
		}
	}

	/**
	 * Update an item to the database in a specified container.
	 */
	static void updateItemInDatabase(Context context, PersonaItemInfo item) {
		final ContentValues values = new ContentValues();
		final ContentResolver cr = context.getContentResolver();

		item.onAddToDatabase(values);

		cr.update(
				PersonaLauncherSettings.Favorites.getContentUri(item.id, false),
				values, null, null);
	}

	/**
	 * Removes the specified item from the database
	 * 
	 * @param context
	 * @param item
	 */
	static void deleteItemFromDatabase(Context context, PersonaItemInfo item) {
		final ContentResolver cr = context.getContentResolver();

		cr.delete(
				PersonaLauncherSettings.Favorites.getContentUri(item.id, false),
				null, null);
	}

	/**
	 * Remove the contents of the specified folder from the database
	 */
	static void deleteUserFolderContentsFromDatabase(Context context,
			PersonaUserFolderInfo info) {
		final ContentResolver cr = context.getContentResolver();

		cr.delete(
				PersonaLauncherSettings.Favorites.getContentUri(info.id, false),
				null, null);
		cr.delete(PersonaLauncherSettings.Favorites.CONTENT_URI,
				PersonaLauncherSettings.Favorites.CONTAINER + "=" + info.id,
				null);
	}

	/**
	 * Get an the icon for an activity Accounts for theme and icon shading
	 */
	static Drawable getIcon(PackageManager manager, Context context,
			ActivityInfo activityInfo) {
		String themePackage = PersonaAlmostNexusSettingsHelper
				.getThemePackageName(context, PersonaLauncher.THEME_DEFAULT);
		Drawable icon = null;
		if (themePackage.equals(PersonaLauncher.THEME_DEFAULT)) {
			icon = PersonaUtilities.createIconThumbnail(
					activityInfo.loadIcon(manager), context);
		} else {
			// get from theme
			Resources themeResources = null;
			if (PersonaAlmostNexusSettingsHelper.getThemeIcons(context)) {
				activityInfo.name = activityInfo.name.toLowerCase().replace(
						".", "_");
				try {
					themeResources = manager
							.getResourcesForApplication(themePackage);
				} catch (NameNotFoundException e) {
					// e.printStackTrace();
				}
				if (themeResources != null) {
					int resource_id = themeResources.getIdentifier(
							activityInfo.name, "drawable", themePackage);
					if (resource_id != 0) {
						icon = themeResources.getDrawable(resource_id);
					}

					// use PersonaIconShader
					if (icon == null) {
						if (compiledIconShaderName == null
								|| compiledIconShaderName
										.compareTo(themePackage) != 0) {
							compiledIconShader = null;
							resource_id = themeResources.getIdentifier(
									"shader", "xml", themePackage);
							if (resource_id != 0) {
								XmlResourceParser xpp = themeResources
										.getXml(resource_id);
								compiledIconShader = PersonaIconShader
										.parseXml(xpp);
							}
						}

						if (compiledIconShader != null) {
							icon = PersonaUtilities.createIconThumbnail(
									activityInfo.loadIcon(manager), context);
							try {
								icon = PersonaIconShader.processIcon(icon,
										compiledIconShader);
							} catch (Exception e) {
							}
						}
					}
				}
			}

			if (icon == null) {
				icon = PersonaUtilities.createIconThumbnail(
						activityInfo.loadIcon(manager), context);
			} else {
				icon = PersonaUtilities.createIconThumbnail(icon, context);
			}
		}
		return icon;
	}

	/**
	 * Resize an item in the DB to a new <container, screen, cellX, cellY>
	 */
	static void resizeItemInDatabase(Context context, PersonaItemInfo item,
			long container, int screen, int cellX, int cellY, int spanX,
			int spanY) {
		item.container = container;
		item.screen = screen;
		item.cellX = cellX;
		item.cellY = cellY;
		item.spanX = spanX;
		item.spanY = spanY;

		final ContentValues values = new ContentValues();
		final ContentResolver cr = context.getContentResolver();

		values.put(PersonaLauncherSettings.Favorites.CONTAINER, item.container);
		values.put(PersonaLauncherSettings.Favorites.CELLX, item.cellX);
		values.put(PersonaLauncherSettings.Favorites.CELLY, item.cellY);
		values.put(PersonaLauncherSettings.Favorites.SPANX, item.spanX);
		values.put(PersonaLauncherSettings.Favorites.SPANY, item.spanY);
		values.put(PersonaLauncherSettings.Favorites.SCREEN, item.screen);

		cr.update(
				PersonaLauncherSettings.Favorites.getContentUri(item.id, false),
				values, null, null);
	}

	boolean ocuppiedArea(int screen, int id, Rect rect) {
		final ArrayList<PersonaItemInfo> desktopItems = mDesktopItems;
		int count = desktopItems.size();
		Rect r = new Rect();
		for (int i = 0; i < count; i++) {
			if (desktopItems.get(i).screen == screen) {
				PersonaItemInfo it = desktopItems.get(i);
				r.set(it.cellX, it.cellY, it.cellX + it.spanX, it.cellY
						+ it.spanY);
				if (rect.intersect(r)) {
					return true;
				}
			}
		}
		final ArrayList<PersonaLauncherAppWidgetInfo> desktopWidgets = mDesktopAppWidgets;
		count = desktopWidgets.size();
		for (int i = 0; i < count; i++) {
			if (desktopWidgets.get(i).screen == screen) {
				PersonaLauncherAppWidgetInfo it = desktopWidgets.get(i);
				if (id != it.appWidgetId) {
					r.set(it.cellX, it.cellY, it.cellX + it.spanX, it.cellY
							+ it.spanY);
					if (rect.intersect(r)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	synchronized void updateCounterForPackage(PersonaLauncher personaLauncher,
			String packageName, int counter, int color) {
		if (mApplicationsLoader != null && mApplicationsLoader.isRunning()) {
			startApplicationsLoaderLocked(personaLauncher, false);
			return;
		}
		boolean changed = false;
		if (packageName != null && packageName.length() > 0
				&& mApplicationsAdapter != null) {
			final int count = mApplications.size();
			for (int i = 0; i < count; i++) {
				final PersonaApplicationInfo info = mApplications.get(i);
				final Intent intent = info.intent;
				final ComponentName name = intent.getComponent();
				if (info.itemType == PersonaLauncherSettings.Favorites.ITEM_TYPE_APPLICATION
						&& packageName.equals(name.getPackageName())
						&& info.counter != counter) {
					info.counter = counter;
					info.counterColor = color;
					changed = true;
				}
			}
		}
		if (changed)
			mApplicationsAdapter.notifyDataSetChanged();
	}

	void updateCounterDesktopItem(PersonaItemInfo info, int counter, int color) {
		try {
			if (mDesktopItems.get(mDesktopItems.indexOf(info)) instanceof PersonaApplicationInfo) {
				((PersonaApplicationInfo) mDesktopItems.get(mDesktopItems
						.indexOf(info))).counter = counter;
				((PersonaApplicationInfo) mDesktopItems.get(mDesktopItems
						.indexOf(info))).counterColor = color;
			}
		} catch (Exception e) {
			// Sync issue........
		}
	}

	public interface finishLauncherActivityInterface {

		public void finishLauncherActivty();

	}

}
