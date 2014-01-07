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

/**
	 * KEYCODE 			AUTHOR				PURPOSE
	 * Broadcast		290661				BroadcastReceiver added for listening ROVA events
	 */

package com.cognizant.trumobi.em;

import java.io.File;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;

import com.TruBoxSDK.SharedPreferences;
import com.bugsense.trace.BugSenseHandler;
import com.cognizant.trumobi.PersonaExceptionHandler;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.calendar.view.CalendarAdapter;
import com.cognizant.trumobi.calendar.view.CalendarFragmentType;
import com.cognizant.trumobi.commonabstractclass.TruMobiTimerClass;
import com.cognizant.trumobi.commonbroadcastreceivers.ExternalBroadcastReceiver;
import com.cognizant.trumobi.dialer.DialerParentActivity;
import com.cognizant.trumobi.dialer.DialerParentActivity.PhoneCallListener;
import com.cognizant.trumobi.em.activity.EmAccountShortcutPicker;
import com.cognizant.trumobi.em.activity.EmDebug;
import com.cognizant.trumobi.em.activity.EmMessageCompose;
import com.cognizant.trumobi.em.activity.EmMessageView;
import com.cognizant.trumobi.em.provider.EmEmailContent;
import com.cognizant.trumobi.em.service.EmMailService;
import com.tf.thinkdroid.common.app.IdleHandler;
import com.tf.thinkdroid.common.app.TFApplication;

public class Email extends TFApplication {
	public static final String LOG_TAG = "Email";

	/**
	 * If this is enabled there will be additional logging information sent to
	 * Log.d, including protocol dumps.
	 * 
	 * This should only be used for logs that are useful for debbuging user
	 * problems, not for internal/development logs.
	 * 
	 * This can be enabled by typing "debug" in the AccountFolderList activity.
	 * Changing the value to 'true' here will likely have no effect at all!
	 * 
	 * TODO: rename this to sUserDebug, and rename LOGD below to DEBUG.
	 */
	public static boolean DEBUG = true;

	/**
	 * If this is enabled than logging that normally hides sensitive information
	 * like passwords will show that information.
	 */
	public static boolean DEBUG_SENSITIVE = false;

	/**
	 * Set this to 'true' to enable as much Email logging as possible. Do not
	 * check-in with it set to 'true'!
	 */
	public static final boolean LOGD = true;

	/**
	 * The MIME type(s) of attachments we're willing to send via attachments.
	 * 
	 * Any attachments may be added via Intents with Intent.ACTION_SEND or
	 * ACTION_SEND_MULTIPLE.
	 */
	public static final String[] ACCEPTABLE_ATTACHMENT_SEND_INTENT_TYPES = new String[] { "*/*", };

	/**
	 * The MIME type(s) of attachments we're willing to send from the internal
	 * UI.
	 * 
	 * NOTE: At the moment it is not possible to open a chooser with a list of
	 * filter types, so the chooser is only opened with the first item in the
	 * list.
	 */
	public static final String[] ACCEPTABLE_ATTACHMENT_SEND_UI_TYPES = new String[] {
			"image/*", "video/*", };

	/**
	 * The MIME type(s) of attachments we're willing to view.
	 */
	public static final String[] ACCEPTABLE_ATTACHMENT_VIEW_TYPES = new String[] { "*/*", };

	/**
	 * The MIME type(s) of attachments we're not willing to view.
	 */
	public static final String[] UNACCEPTABLE_ATTACHMENT_VIEW_TYPES = new String[] {};

	/**
	 * The MIME type(s) of attachments we're willing to download to SD.
	 */
	public static final String[] ACCEPTABLE_ATTACHMENT_DOWNLOAD_TYPES = new String[] { "image/*", };

	/**
	 * The MIME type(s) of attachments we're not willing to download to SD.
	 */
	public static final String[] UNACCEPTABLE_ATTACHMENT_DOWNLOAD_TYPES = new String[] {};

	/**
	 * Specifies how many messages will be shown in a folder by default. This
	 * number is set on each new folder and can be incremented with
	 * "Load more messages..." by the VISIBLE_LIMIT_INCREMENT
	 */
	public static final int VISIBLE_LIMIT_DEFAULT = 25;

	/**
	 * Number of additional messages to load when a user selects
	 * "Load more messages..."
	 */
	public static final int VISIBLE_LIMIT_INCREMENT = 25;

	/**
	 * The maximum size of an attachment we're willing to download (either View
	 * or Save) Attachments that are base64 encoded (most) will be about 1.375x
	 * their actual size so we should probably factor that in. A 5MB attachment
	 * will generally be around 6.8MB downloaded but only 5MB saved.
	 */
	public static final int MAX_ATTACHMENT_DOWNLOAD_SIZE = (5 * 1024 * 1024);

	/**
	 * The maximum size of an attachment we're willing to upload (measured as
	 * stored on disk). Attachments that are base64 encoded (most) will be about
	 * 1.375x their actual size so we should probably factor that in. A 5MB
	 * attachment will generally be around 6.8MB uploaded.
	 */
	public static int MAX_ATTACHMENT_UPLOAD_SIZE = (5 * 1024 * 1024);

	private static HashMap<Long, Long> sMailboxSyncTimes = new HashMap<Long, Long>();
	private static final long UPDATE_INTERVAL = 5 * DateUtils.MINUTE_IN_MILLIS;

	/**
	 * This is used to force stacked UI to return to the "welcome" screen any
	 * time we change the accounts list (e.g. deleting accounts in the Account
	 * Manager preferences.)
	 */
	private static boolean sAccountsChangedNotification = false;

	public static final String EXCHANGE_ACCOUNT_MANAGER_TYPE = "com.cognizant.trumobi.exchange";

	/**************** FOR CALENDAR ********************/
	private static String presentDay;
	private static boolean todayEventFlag, searchFlag, editFlag;
	private static boolean viewIsAnimated, pageScrollLeft;
	private static CalendarFragmentType mCurrentFragment;
	//public static int currentdatevalue = 0;
	public static int todayDate, todayMonth, todayYear;
	private static String searchQueryString, newZone = "";
	public static int selectedDay;
	public static int selectedMonth;
	public static int selectedYear;
	private static boolean isDevicePreICS;
	private static Uri CALENDAR_URI;
	private static View calenderView;
	private static CalendarAdapter calenderAdapter;
	private static SharedPreferences mSecurePrefs;
	private static ArrayList<String> eventCount;
	private static String notificationType;

	/*************** FOR DIALER ***********************/
	TelephonyManager telMgr = null;
	PhoneCallListener callListner = null;

	public static String getNotificationType() {
		return notificationType;
	}

	public static void setNotificationType(String notificationType) {
		Email.notificationType = notificationType;
	}

	public static ArrayList<String> getEventCount() {
		return eventCount;
	}

	public static void setEventCount(ArrayList<String> eventCount) {
		Email.eventCount = eventCount;
	}
	
	@Override
    public IdleHandler createIdleHandler() {
        return new TestIdleHandler();
    }
    
	/*   protected class TestIdleHandler implements IdleHandler {

        @Override
        public int getIdleTimeout() {
            return 60*1000;   // ms
        }

        @Override
        public void onTimeout(Context context) {
            //Toast.makeText(context, "Over time", Toast.LENGTH_SHORT).show();
        }
        
    }*/
	
	   protected class TestIdleHandler implements IdleHandler {

	        @Override
	        public int getIdleTimeout() {
	        	//Log.i("", "getIdleTimeout ====== "+(int) (new SharedPreferences(getAppContext()).getLong("selected_autolocktime_l", 60)*1000));
	        	return (int) (new SharedPreferences(getAppContext()).getLong("selected_autolocktime_l", 60)*1000);
	        	
	            //return 60000;   // ms
	        }

	        @Override
	        public void onTimeout(Context context) {
	            //Toast.makeText(context, "Over time", Toast.LENGTH_SHORT).show();

	        	//TruMobiTimerClass.launchMainActivity(context);
	        	Intent intentKill = new Intent("com.thinkfree.KILL_ACTIVITY");
	        	context.sendBroadcast(intentKill);
	        	TruMobiTimerClass.launchMainActivity(context);
	        	//Log.i("", "getIdleTimeout onTimeout ====== "+(new SharedPreferences(CognizantEmail.appContext).getBoolean("showPinOnResume", true)));
	        	//new SharedPreferences(CognizantEmail.appContext).edit().putBoolean("showPinOnResume", true).commit();
	        	//Log.i("", "getIdleTimeout onTimeout after====== "+(new SharedPreferences(CognizantEmail.appContext).getBoolean("showPinOnResume", true)));
	        }

			@Override
			public boolean isStrict() {
				// TODO Auto-generated method stub
				return true;
			}
	        
	    }
	    
	    public void viewUri(android.app.Activity arg0, Uri url, int arg2) {
	    	
	    	Log.i("", "viewUri ====== "+url.toString()+"   "+arg2);
	    	String link = url.toString();
	    	if (link != null && link.toLowerCase().startsWith("mailto:")) {
	    		 EmMessageCompose.actionCompose(arg0, link,1);
	    		 return;
	        }
	    	
	         if (!url.toString().startsWith("http://")
	                      && !url.toString().startsWith("https://")) {
	        	 link = "http://" + url.toString();
	         }
	         
	         Intent openWebAppIntent = new Intent("com.cognizant.trumobi.securebrowser");
	         openWebAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	         if (openWebAppIntent != null) {
	                openWebAppIntent.putExtra("toBrowser",link);
	                try {
	             	   startActivity(openWebAppIntent);
	             	   
	                }catch (ActivityNotFoundException ex) {
	                }
	         } 
	    	
	    };
		
	

	/**********************************************/

	// The color chip resources and the RGB color values in the array below must
	// be kept in sync
	private static final int[] ACCOUNT_COLOR_CHIP_RES_IDS = new int[] {
			R.drawable.em_appointment_indicator_leftside_1,
			R.drawable.em_appointment_indicator_leftside_2,
			R.drawable.em_appointment_indicator_leftside_3,
			R.drawable.em_appointment_indicator_leftside_4,
			R.drawable.em_appointment_indicator_leftside_5,
			R.drawable.em_appointment_indicator_leftside_6,
			R.drawable.em_appointment_indicator_leftside_7,
			R.drawable.em_appointment_indicator_leftside_8,
			R.drawable.em_appointment_indicator_leftside_9, };

	private static final int[] ACCOUNT_COLOR_CHIP_RGBS = new int[] { 0x71aea7,
			0x621919, 0x18462f, 0xbf8e52, 0x001f79, 0xa8afc2, 0x6b64c4,
			0x738359, 0x9d50a4, };
	private static Context appContext;

	public static Context getAppContext() {
		return appContext;
	}

	private static File sTempDirectory;

	/* package for testing */static int getColorIndexFromAccountId(
			long accountId) {
		// Account id is 1-based, so - 1.
		// Use abs so that it won't possibly return negative.
		return Math.abs((int) (accountId - 1)
				% ACCOUNT_COLOR_CHIP_RES_IDS.length);
	}

	public static int getAccountColorResourceId(long accountId) {
		return ACCOUNT_COLOR_CHIP_RES_IDS[getColorIndexFromAccountId(accountId)];
	}

	public static int getAccountColor(long accountId) {
		return ACCOUNT_COLOR_CHIP_RGBS[getColorIndexFromAccountId(accountId)];
	}

	public static void setTempDirectory(Context context) {
		sTempDirectory = context.getCacheDir();
	}

	public static File getTempDirectory() {
		if (sTempDirectory == null) {
			throw new RuntimeException(
					"TempDirectory not set.  "
							+ "If in a unit test, call Email.setTempDirectory(context) in setUp().");
		}
		return sTempDirectory;
	}

	/**
	 * Called throughout the application when the number of accounts has
	 * changed. This method enables or disables the Compose activity, the boot
	 * receiver and the service based on whether any accounts are configured.
	 * Returns true if there are any accounts configured.
	 */
	public static boolean setServicesEnabled(Context context) {
		Cursor c = null;
		try {
			c = context.getContentResolver().query(
					EmEmailContent.Account.CONTENT_URI,
					EmEmailContent.Account.ID_PROJECTION, null, null, null);
			if (c == null)
				return false;
			boolean enable = c != null && c.getCount() > 0;
			setServicesEnabled(context, enable);
			return enable;
		} finally {
			if (c != null) {
				c.close();
			}
		}
	}

	public static void setServicesEnabled(Context context, boolean enabled) {
		PackageManager pm = context.getPackageManager();
		if (!enabled
				&& pm.getComponentEnabledSetting(new ComponentName(context,
						EmMailService.class)) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
			/*
			 * If no accounts now exist but the service is still enabled we're
			 * about to disable it so we'll reschedule to kill off any existing
			 * alarms.
			 */
			EmMailService.actionReschedule(context);
		}
		pm.setComponentEnabledSetting(new ComponentName(context,
				EmMessageCompose.class),
				enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
						: PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
				PackageManager.DONT_KILL_APP);
		pm.setComponentEnabledSetting(new ComponentName(context,
				EmAccountShortcutPicker.class),
				enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
						: PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
				PackageManager.DONT_KILL_APP);
		pm.setComponentEnabledSetting(new ComponentName(context,
				EmMailService.class),
				enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
						: PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
				PackageManager.DONT_KILL_APP);
		if (enabled
				&& pm.getComponentEnabledSetting(new ComponentName(context,
						EmMailService.class)) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
			/*
			 * And now if accounts do exist then we've just enabled the service
			 * and we want to schedule alarms for the new accounts.
			 */
			EmMailService.actionReschedule(context);
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		BugSenseHandler.initAndStartSession(this.getApplicationContext(),
				"377266e1");
		Thread.setDefaultUncaughtExceptionHandler(new PersonaExceptionHandler(
				this, Thread.getDefaultUncaughtExceptionHandler()));
		EmPreferences prefs = EmPreferences.getPreferences(this);
		DEBUG = prefs.getEnableDebugLogging();
		DEBUG_SENSITIVE = prefs.getEnableSensitiveLogging();
		setTempDirectory(this);
		appContext = getApplicationContext();
		// Reset all accounts to default visible window
		EmEmController.getInstance(this).resetVisibleLimits();

		// Enable logging in the EAS service, so it starts up as early as
		// possible.
		EmDebug.updateLoggingFlags(this);

		// Listen Phone Call Changes
		DialerParentActivity listenState = new DialerParentActivity();
		callListner = listenState.new PhoneCallListener(getAppContext());
		telMgr = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);
		telMgr.listen(callListner, PhoneCallListener.LISTEN_CALL_STATE);
		//Broadcast
				registerBroadcastReceiver();

	}
	
	private void registerBroadcastReceiver() {
		ExternalBroadcastReceiver extBroadcastReceiver = new ExternalBroadcastReceiver();
		LocalBroadcastManager.getInstance(appContext).registerReceiver(extBroadcastReceiver,
			      new IntentFilter("com.cognizant.trubox.event"));
		
	}

	/**
	 * Internal, utility method for logging. The calls to log() must be guarded
	 * with "if (Email.LOGD)" for performance reasons.
	 */
	public static void log(String message) {
		Log.d(LOG_TAG, message);
	}

	/**
	 * Update the time when the mailbox is refreshed
	 * 
	 * @param mailboxId
	 *            mailbox which need to be updated
	 */
	public static void updateMailboxRefreshTime(long mailboxId) {
		synchronized (sMailboxSyncTimes) {
			sMailboxSyncTimes.put(mailboxId, System.currentTimeMillis());
		}
	}

	/**
	 * Check if the mailbox is need to be refreshed
	 * 
	 * @param mailboxId
	 *            mailbox checked the need of refreshing
	 * @return the need of refreshing
	 */
	public static boolean mailboxRequiresRefresh(long mailboxId) {
		synchronized (sMailboxSyncTimes) {
			return !sMailboxSyncTimes.containsKey(mailboxId)
					|| (System.currentTimeMillis()
							- sMailboxSyncTimes.get(mailboxId) > UPDATE_INTERVAL);
		}
	}

	/**
	 * Called by the accounts reconciler to notify that accounts have changed,
	 * or by "Welcome" to clear the flag.
	 * 
	 * @param setFlag
	 *            true to set the notification flag, false to clear it
	 */
	public static synchronized void setNotifyUiAccountsChanged(boolean setFlag) {
		sAccountsChangedNotification = setFlag;
	}

	/**
	 * Called from activity onResume() functions to check for an
	 * accounts-changed condition, at which point they should finish() and jump
	 * to the Welcome activity.
	 */
	public static synchronized boolean getNotifyUiAccountsChanged() {
		return sAccountsChangedNotification;
	}

	/**************** FOR CALENDAR ******************/

	public static SharedPreferences getmSecurePrefs() {
		if(mSecurePrefs ==  null){
			mSecurePrefs = new SharedPreferences(appContext);
		}

		return mSecurePrefs;
	}

	public static void setmSecurePrefs(SharedPreferences mSecurePrefs) {

		Email.mSecurePrefs = mSecurePrefs;
	}

	public static void setPresentDate(String presentDayString) {
		presentDay = presentDayString;
	}

	public static String getPresentDay() {
		return presentDay;
	}

	public static void unregisterCalendarReceiver(BroadcastReceiver receiver) {
		try {
			appContext.unregisterReceiver(receiver);
		} catch (Exception e) {
			Log.d("Exception while unregistering " + receiver.toString(),
					e.toString());
		}
	}

	@SuppressWarnings("unused")
	public static void getCurrentDDMMYY() {

		// getting local time, date, day of week and other details in local
		// timezone
		Calendar localCalendar = getNewCalendar();
		Date currentTime = localCalendar.getTime();
		int currentDay = localCalendar.get(Calendar.DATE);
		int currentMonth = localCalendar.get(Calendar.MONTH) + 1;
		int currentYear = localCalendar.get(Calendar.YEAR);

		int currentDayOfWeek = localCalendar.get(Calendar.DAY_OF_WEEK);
		int currentDayOfMonth = localCalendar.get(Calendar.DAY_OF_MONTH);
		int CurrentDayOfYear = localCalendar.get(Calendar.DAY_OF_YEAR);
		//currentdatevalue = currentDay;
		todayDate = currentDay;
		todayMonth = currentMonth;
		todayYear = currentYear;

	}

	public static String getNameOFMonth(int month) {
		// int month = 3;
		// Calendar cal = Calendar.getInstance();
		DateFormatSymbols dateFormatSymbol = new DateFormatSymbols();
		String monthName = dateFormatSymbol.getShortMonths()[month - 1];
		return monthName;
	}

	public static int getTotalNumberOfDay(int month, int date, int year) {

		Calendar calendar = Calendar.getInstance();
		calendar.set(year, month, 1);
		int days = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		return days;
	}

	public static int convertToDp(int input) {
		// Get the screen's density scale
		final float scale = appContext.getResources().getDisplayMetrics().density;
		// Convert the dps to pixels, based on density scale
		return (int) (input * scale + 0.5f);
	}

	public static int convertToPixels(int input) {
		// Get the screen's density scale
		final float scale = appContext.getResources().getDisplayMetrics().density;
		// Convert the dps to pixels, based on density scale
		return (int) (input * scale + 0.5f);
	}

	public static String getWeekDayOfDate(String input) {
		SimpleDateFormat format1 = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
		Date dt1;
		String finalDay = "";
		try {
			dt1 = format1.parse(input);
			DateFormat format2 = new SimpleDateFormat("EEEE", Locale.US);
			finalDay = format2.format(dt1);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return finalDay;
	}

	public static String getWeekDays() {
		Calendar cal = Calendar.getInstance();
		String string = Email.getPresentDay();
		// Log.e("getPresentDay", ""+string);
		Date date = null;
		try {
			date = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
					.parse(string);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		cal.setTime(date);
		SimpleDateFormat format = new SimpleDateFormat("MMM dd", Locale.US);
		Calendar firstCal = (Calendar) cal.clone();

		firstCal.add(Calendar.DAY_OF_WEEK, firstCal.getFirstDayOfWeek()
				- firstCal.get(Calendar.DAY_OF_WEEK));
		// SharedPreferences prefs = PreferenceManager
		// .getDefaultSharedPreferences(appContext);
		SharedPreferences prefs = new SharedPreferences(appContext);
		String WeekStartsOn = prefs.getString(
				appContext.getString(R.string.key_list_preference), "");
		if (WeekStartsOn.equalsIgnoreCase("2")) {
			firstCal.setFirstDayOfWeek(Calendar.SATURDAY);
		} else if (WeekStartsOn.equalsIgnoreCase("3")) {
			firstCal.setFirstDayOfWeek(Calendar.SUNDAY);
		} else if (WeekStartsOn.equalsIgnoreCase("4")) {
			firstCal.setFirstDayOfWeek(Calendar.MONDAY);
		}
		int firstDayOfWeek = firstCal.getFirstDayOfWeek();
		int dayOfWeek = firstCal.get(Calendar.DAY_OF_WEEK);
		if (firstDayOfWeek == 7) {
			firstCal.add(Calendar.DAY_OF_WEEK, -dayOfWeek);
		} else
			firstCal.add(Calendar.DAY_OF_WEEK, firstCal.getFirstDayOfWeek()
					- firstCal.get(Calendar.DAY_OF_WEEK));
		String[] days = new String[2];
		days[0] = format.format(firstCal.getTime());
		Calendar lastCal = (Calendar) firstCal.clone();
		lastCal.add(Calendar.DAY_OF_YEAR, 6);
		days[1] = format.format(lastCal.getTime());
		String[] weekDays = Arrays.toString(days).split(",");
		// String weekVal2=weekDays[1].substring(weekDays[1].length()-1);
		String weekVal2 = weekDays[1].replace("]", "");
		String weekVal = weekDays[0].substring(1) + " - " + weekVal2;
		return weekVal;
	}

	public static Calendar getCurrentDay() {

		Calendar calendar = Calendar.getInstance();
		String presentDay = getPresentDay();
		Date date = null;
		try {
			date = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
					.parse(presentDay);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		calendar.setTime(date);
		return calendar;
	}

	public static Calendar getCurrentDay(String presentDay) {

		Calendar calendar = Calendar.getInstance();
		Date date = null;
		try {
			date = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
					.parse(presentDay);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		calendar.setTime(date);
		// System.out.println("Set DATE ------------->"+calendar.getTime());
		return calendar;
	}

	public static boolean isTodayEventFlag() {
		return todayEventFlag;
	}

	public static void setTodayEventFlag(boolean todayEventFlag) {
		Email.todayEventFlag = todayEventFlag;
	}

	public static CalendarFragmentType getmCurrentFragment() {
		return mCurrentFragment;
	}

	public static void setmCurrentFragment(CalendarFragmentType mCurrentFragment) {
		Email.mCurrentFragment = mCurrentFragment;
	}

	// public static boolean isPageScrollRight() {
	// return pageScrollRight;
	// }
	//
	// public static void setPageScrollRight(boolean pageScrollRight) {
	// Email.pageScrollRight = pageScrollRight;
	// }

	public static boolean isPageScrollLeft() {
		return pageScrollLeft;
	}

	public static void setPageScrollLeft(boolean pageScrollLeft) {
		Email.pageScrollLeft = pageScrollLeft;
	}

	public static boolean isViewIsAnimated() {
		return viewIsAnimated;
	}

	public static void setViewIsAnimated(boolean viewIsAnimated) {
		Email.viewIsAnimated = viewIsAnimated;
	}

	public static String getSearchQueryString() {
		return searchQueryString;
	}

	public static void setSearchQueryString(String searchQueryString) {
		Email.searchQueryString = searchQueryString;
	}

	public static boolean isSearchFlag() {
		return searchFlag;
	}

	public static void setSearchFlag(boolean searchFlag) {
		Email.searchFlag = searchFlag;
	}

	public static String getNewZone() {
		return newZone;
	}

	public static void setNewZone(String newZone) {
		Email.newZone = newZone;
	}

	public static boolean isEditFlag() {
		return editFlag;
	}

	public static void setEditFlag(boolean editFlag) {
		Email.editFlag = editFlag;
	}

	public static Calendar getNewCalendar() {
		// System.out.println("Application class NewCalendar ZONE"+Email.getNewZone());
		Calendar newCalendar;
		if ((Email.getNewZone().equals("")) || (Email.getNewZone() == null)) {
			newCalendar = Calendar.getInstance(TimeZone.getDefault());
		} else {
			newCalendar = Calendar.getInstance(TimeZone.getTimeZone(Email
					.getNewZone()));
		}
		// System.out.println("Application class NewCalendar ZONE calendar"+newCalendar);

		return newCalendar;
	}

	public static boolean isDevicePreICS() {
		return isDevicePreICS;
	}

	public static void setDevicePreICS(boolean isDevicePreICS) {
		Email.isDevicePreICS = isDevicePreICS;
	}

	public static Uri getCALENDAR_URI() {
		return CALENDAR_URI;
	}

	public static void setCALENDAR_URI(Uri cALENDAR_URI) {
		CALENDAR_URI = cALENDAR_URI;
	}

	public static View getCalenderView() {
		return calenderView;
	}

	public static void setCalenderView(View calenderView) {
		Email.calenderView = calenderView;
	}

	public static CalendarAdapter getCalenderAdapter() {
		return calenderAdapter;
	}

	public static void setCalenderAdapter(CalendarAdapter calenderAdapter) {
		Email.calenderAdapter = calenderAdapter;
	}
	
	
}
