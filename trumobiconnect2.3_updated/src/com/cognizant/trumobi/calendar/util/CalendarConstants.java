package com.cognizant.trumobi.calendar.util;

import android.net.Uri;

import com.cognizant.trumobi.calendar.modal.Attendee;
import com.cognizant.trumobi.calendar.modal.Event;
import com.cognizant.trumobi.calendar.modal.Exceptions;
import com.cognizant.trumobi.calendar.modal.Recurrence;
import com.cognizant.trumobi.calendar.modal.Reminder;
import com.cognizant.trumobi.calendar.modal.SyncState;

public class CalendarConstants {
	public static final String LOADING_MESSAGE = "Loading...";

	public static final String[] HOURS = { "00", "01", "02", "03", "04", "05",
			"06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16",
			"17", "18", "19", "20", "21", "22", "23" };
	public static final int numberOfDaysInWeek = 7;
	public static final String[] WEEK = { "Monday", "Tuesday", "Wednesday",
			"Thursday", "Friday", "Saturday", "Sunday" };
	public static String[] CALENDAR = { "Day", "Week", "Month", "Agenda" };
	public static final String CERT_DETAILS = "Certificatedetails";
	public static final String CERT_DOMAIN = "cert_domain";
	public static final String CERT_EXCHANGE = "cert_exchange";
	public static final String CERT_CRT_PWD = "cert_pwd";
	public static final String CERT_EMAIL = "cert_email";

	public static final String PREFS_NAME = "Eas_Pref";
	public static final String POLICY_KEY = "POLICYKEY";
	public static final String DEVICE_ID = "DEVICEID";

	public static final String TABLE_EVENTS = "events";
	public static final String TABLE_EXCEPTIONS = "exceptions";
	public static final String TABLE_REMINDERS = "reminders";
	public static final String TABLE_SEARCH_HISTORY = "search_history";
	public static final String TABLE_ATTENDEES = "attendees";
	public static final String TABLE_SYNC_STATE = "calendar_syncstate";

	public static final String SEARCH_STRING = "search_string";
	public static final String AUTHORITY = "com.cognizant.trumobi.em.provider";
	public static final Uri SEARCH_CONTENT_URI = Uri.parse("content://"
			+ AUTHORITY + "/" + TABLE_SEARCH_HISTORY);

	public static final String CREATE_TABLE_EVENTS = "create table "
			+ TABLE_EVENTS + " ( _id integer primary key autoincrement, "
			+ Event.EVENT_ID + " integer not null, " + Event.CALENDAR_UID
			+ " text, " + Event.STATUS + " integer not null, "
			+ Event.RESPONSE_TYPE + " integer, " + Event.ORGANIZER + " text, "
			+ Event.TITLE + " text, " + Event.EVENT_LOCATION + " text,  "
			+ Event.DESCRIPTION + " text, " + Event.START_DATE + " text, "
			+ Event.DTSTART + " real, " + Event.END_DATE + " text, "
			+ Event.DTEND + " real, " + Event.EVENT_TIMEZONE + " text, "
			+ Event.EVENT_END_TIMEZONE + " text, " + Event.ALL_DAY
			+ " integer, " + Event.RRULE + " text, " + Event.RDATE + " text, "
			+ Event.EXRULE + " text, " + Event.EXDATE + " text ,"
			+ Event.AVAILABILITY + " integer, " + Event.ACCESSIBILITY
			+ " integer, " 
			+ Event.HAS_EXCEPTIONS + " integer, " + Event.MSG_ID + " text, "
			+ Recurrence.TYPE + " integer, " + Recurrence.OCCURENCES
			+ " integer, " + Recurrence.INTERVAL + " integer, "
			+ Recurrence.DOW + " integer, " + Recurrence.DOW_STRING + " text, "
			+ Recurrence.DOM + " integer, " + Recurrence.WOM + " integer, "
			+ Recurrence.MOY + " integer, " + Recurrence.UNTIL
			+ " integer, unique(_id," + Event.EVENT_ID + "," + Event.STATUS
			+ "));";

	public static final String CREATE_TABLE_EXCEPTIONS = "create table "
			+ TABLE_EXCEPTIONS + " ( _id integer , " + Exceptions.EVENT_ID
			+ " integer not null, " + Exceptions.EXCEPTION_ID
			+ " integer primary key autoincrement, " + Exceptions.CALENDAR_UID + " text, "
			+ Exceptions.STATUS + " integer not null, "
			+ Exceptions.RESPONSE_TYPE + " integer, " + Exceptions.ORGANIZER
			+ " text, " + Exceptions.TITLE + " text, "
			+ Exceptions.EVENT_LOCATION + " text,  " + Exceptions.DESCRIPTION
			+ " text, " + Exceptions.START_DATE + " text, "
			+ Exceptions.DTSTART + " real, " + Exceptions.EXCEPTION_DTSTART
			+ " real, " + Exceptions.END_DATE + " text, " + Exceptions.DTEND
			+ " real, " + Exceptions.EVENT_TIMEZONE + " text, "
			+ Exceptions.EVENT_END_TIMEZONE + " text, "
			+ Exceptions.IS_EXCEPTION_DELETED + " integer, "
			+ Exceptions.ALL_DAY + " integer, " + Exceptions.RRULE + " text, "
			+ Exceptions.EXRULE + " text, " + Exceptions.EXDATE + " text ,"
			+ Exceptions.AVAILABILITY + " integer, " + Exceptions.ACCESSIBILITY
			+ " integer, " + Exceptions.ISINVITE + " integer, "
			+ Exceptions.MSG_ID + " text, " + Recurrence.TYPE + " integer, "
			+ Recurrence.OCCURENCES + " integer, " + Recurrence.INTERVAL
			+ " integer, " + Recurrence.DOW + " integer, "
			+ Recurrence.DOW_STRING + " text, " + Recurrence.DOM + " integer, "
			+ Recurrence.WOM + " integer, " + Recurrence.MOY + " integer, "
			+ Recurrence.UNTIL + " integer);";

	public static final String CREATE_TABLE_ATTENDEES = "create table "
			+ TABLE_ATTENDEES + " ( " + Attendee.EVENT_ID + " text , "
			+ Attendee.ATTENDEE_NAME + " text, " + Attendee.ATTENDEE_EMAIL
			+ " text, " + Attendee.ATTENDEE_RELATIONSHIP + " integer,  "
			+ Attendee.ATTENDEE_TYPE + " integer, " + Attendee.ATTENDEE_STATUS
			+ " integer );";
	public static final String CREATE_TABLE_REMINDERS = "create table "
			+ TABLE_REMINDERS + " ( " + Reminder.EVENT_ID + " integer , "
			+ Reminder.MINUTES + " integer, " + Reminder.METHOD + " integer );";

	public static final String CREATE_TABLE_SEARCH_HISTORY = "create table "
			+ TABLE_SEARCH_HISTORY + " ( " + SEARCH_STRING + " text );";

	public static final String CREATE_TABLE_SYNC_STATE = "create table "
			+ TABLE_SYNC_STATE + " ( _id integer primary key autoincrement, "
			+ SyncState.ACCOUNT_NAME + " text , " + SyncState.ACCOUNT_TYPE
			+ " text, " + SyncState.DATA + " text );";

	/********** Preference Key ***********/
	public static final String SETTINGS_PREF_NAME = "General Settings";
	public static final String PREF_TIME_ZONE_KEY = "zone_entry_key";
	public static final String PREF_DEFAULT_REMINDER_KEY = "default_remindar_key";
	public static final String PREF_NEW_CALENDAR_ZONE = "new_calendar_zone";
	public static final String PREF_USE_HOME_TIME_ZONE_RESULT = "use_home_time_zone_result";
	public static final String DISPLAY_PERSONAL_EVENTS = "display_personal_events";
	public static final String ABOUT_CALENDAR = "About Calendar";
	public static final String QUICK_RESPONSE = "Quick Responses";
	public static final String CALENDAR_SETTINGS = "CalendarSettings";
	public static final String LOWER_VERSION_PROVIDER_URI = "content://com.android.calendar/events";
	public static final String DEFAULT_PROVIDER = "content://calendar/events";
	public static final String NOTIFICATION_REMINDER = "notification_reminder";
	public static final String POP_UP_REMINDER = "pop_up_reminder";
	public static final String SEARCH_HISTORY_CLEARED = "Search history cleared.";
	public static final String RINGTONE_URI = "rintoneURI";

	/********** Strings **************/
	public static final String STRING_DEFAULT = "Default";
	public static final String Tag = "Calendar_Log";
	public static final String INTENT_ACTION_SYNC_COMPLETED = "com.cognizant.trumobi.calendar.sync_completed";
	public static final String CALENDAR_DAY_VIEW_FRAGMENT ="CalendarDayViewFragment";
	public static final String CALENDAR_WEEK_VIEW_FRAGMENT ="CalendarWeekViewFragment";
	public static final String CALENDAR_MONTH_VIEW_FRAGMENT ="CalendarMonthViewFragment";
	public static final String CALENDAR_AGENDA_VIEW_FRAGMENT ="AgendaViewFragment";
	public static final String CALENDAR_VIEW_FRAGMENT = "CalendarView_Fragment";
	public static final String CURRENT_FRAGMENT_TYPE = "FragmentType";
	public static final String FLAG_HIDE_CONTROLS = "FlagHideControls";
	public static final String FLAG_NOTIFICATION_TYPE = "Flag_Notification_type";
	public static final String CALENDAR_NAVIGATION_POSITION="NavigationPreviousPosition";
	
	/******************* Add Event *************/
	public static final String KEY_ADD_EVENT_BUNDLE = "bundleExtra";
	public static final String KEY_ADD_EVENT_CONTENTVALUE = "contentvalues";
	public static final String NOTIFICATION = "Notification";
	public static final String EMAIL = "Email";
	public static final String ADD_REMINDER = "Add Reminder";
	public static final String MINUTES = "minutes";
	public static final String METHOD ="method";
	
	/******************PACKAGE NAME *************/
	public static final String APP_PCK_NAME ="com.cognizant.trumobi";
	/**********************************************/
	public static String EVENT_BEFORE = "Touch to view events before 07 Apr 2013";
	public static String EVENT_AFTER = "Touch to view events before 07 Sep 2013";
	
	/****************** Contentvalue **************/
	public static final String CALENDAR_TYPE_KEY = "calendarType";
	public static final String CALENDAR_TYPE_CORPORATE = "CorporateCalendar";
	public static final String CALENDAR_TYPE_PERSONAL = "Personal_Calendar_V4";
	
	/*************** AgendaFragments*************/
	public static final String VIEW_EVENTS_BEFORE = "Touch to view events before";
	public static final String VIEW_EVENTS_AFTER = "Touch to view events after";
	
	/**********EditEvent*************/
	public static final String EVENTVALUES = "eventvalues";
	public static final String EVENT_CONTENT_VALUES = "EventContentValues";
	public static final String EVENT_DELETED = "Event Deleted";
	public static final String EVENT_PROBLEM = "Sorry this event is deleted.";
	
	/************My Receiver*************/
	public static final String MSG = "msg";
	public static final String EVENT_ID = "eventid";
	public static final String FLAG = "Flag";
	public static final String DAY = "day";
	public static final String MONTH = "month";
	public static final String YEAR ="year";
	
	/************Notification Service *********/
	public static final String NOTIFICATION_TITLE = "Alarm!!";
	public static final String NOTIFICATION_DESC ="Your notification time is upon us.";
	
	
	/*******Toast Message********/
	public static final String EVENT_ADDED_SUCCESSFULLY = "Event created.";
	public static final String EVENT_SAVED_SUCCESSFULLY = "Event Saved.";
	public static final String EVENT_NOT_ADDED ="Problem in Adding Events";
	public static final String EVENT_VALIDATION ="Empty event not created.";
	public static final String EVENT_INVITE_CREATED = "Invitations will be sent.";
	public static final String EVENT_INVITE_UPDATE = "Updates will be sent.";
	
	public static final String _ID = "_id";
	public static final String EVENT ="event";
	public static final String START_DATE = "start_date";
	public static final String END_DATE = "end_date";
	public static final String EVENT_TITLE ="eventtitle";
	public static final String RECURRENCE_CURRENTDATE = "recurrence_currentdate";
	public static final String EVENTS_ID ="eventID";
	public static final String START_TIME = "start_time";
	public static final String END_TIME = "end_time";
	public static final String EVENT_START_TIME = "eventStartTime";
	public static final String EVENT_END_TIME = "eventEndTime";
	public static final String EVENT_START_DATE = "eventStartDate";
	public static final String EVENT_BG_COLOR ="eventBGColor";
	public static final String BG_COLOR ="bg_color";
	public static final String HOURS_KEY = "Hours";
	public static final String DAYS_NEED_TO_ADD = "daysneedtoadd";
	public static final String PERSONAL_OWNER_ACC_NAME = "owner_account_name";
	public static final String PERSONAL_OWNER_ACC_COLOR = "owner_account_color";

	//Specific to 2.3
	public static final String ALL_DAY = "all_day";
	public static final String HOURS_KEY1 = "hours";
	public static final String TEXT_COLOR = "text_color";
	public static final String LOCATION = "location";
	public static final String TITLE = "title";
	
	public static final String SYNC_COMPLETED = "com.cognizant.trumobi.calendar.sync_completed";

	public static final String CALENDAR_DAY_VIEW = "CalendarDayViewFragment";
	public static final String CALENDAR_WEEK_VIEW = "CalendarWeekViewFragment";
	public static final String CALENDAR_AGENDA_VIEW = "AgendaViewFragment";
	public static final String CALENDAR_MONTH_VIEW = "CalendarMonthViewFragment";
	public static final String FLAG_HIDE = "FlagHideControls";
	public static final String FRAGMENT_TYPE = "FragmentType";
	public static final String CURRENT_VIEW = "currentView";
	
	/*************Open Maps***********/
	public static final String googleMapsURL="maps.google.com/maps?q=";
	
	
	public static final String tTag = "testing_exceptions";
	

}
