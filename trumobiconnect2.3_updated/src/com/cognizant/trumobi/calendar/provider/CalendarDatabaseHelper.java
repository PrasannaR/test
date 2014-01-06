package com.cognizant.trumobi.calendar.provider;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;

import com.TruBoxSDK.SharedPreferences;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.calendar.event.CalendarMyReceiver;
import com.cognizant.trumobi.calendar.modal.Attendee;
import com.cognizant.trumobi.calendar.modal.Event;
import com.cognizant.trumobi.calendar.modal.Exceptions;
import com.cognizant.trumobi.calendar.modal.Recurrence;
import com.cognizant.trumobi.calendar.modal.Reminder;
import com.cognizant.trumobi.calendar.modal.SyncState;
import com.cognizant.trumobi.calendar.util.CalendarCommonFunction;
import com.cognizant.trumobi.calendar.util.CalendarConstants;
import com.cognizant.trumobi.calendar.view.CalendarWeekPagerAdapter;
import com.cognizant.trumobi.em.EmAccount;
import com.cognizant.trumobi.em.EmEmController;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.em.activity.CalendarEmMessageCompose;
import com.cognizant.trumobi.em.provider.EmEmailContent;
import com.cognizant.trumobi.em.provider.EmEmailContent.Account;
import com.cognizant.trumobi.em.provider.EmEmailContent.Mailbox;
import com.cognizant.trumobi.em.provider.EmEmailContent.Message;
import com.cognizant.trumobi.exchange.EmEasOutboxService;
import com.cognizant.trumobi.exchange.utility.EmCalendarUtilities;
import com.cognizant.trumobi.log.CalendarLog;

//import com.cognizant.trumobi.calendar.modal.Recurrence;

public class CalendarDatabaseHelper {

	private static long MILLISECS_PER_DAY = 24 * 60 * 60 * 1000;

	public static boolean displayPersonalEvent() {
		return Email
				.getmSecurePrefs()
				.getBoolean(
						Email.getAppContext()
								.getString(
										R.string.key_display_personal_events_checkbox_preference),
						false);
	}

	public static void deleteAttendeesForEvent(String string) {
		String where = Attendee.EVENT_ID + " = ?";
		String whereArgs[] ={string};
		Email.getAppContext().getContentResolver()
				.delete(Attendee.CONTENT_URI, where, whereArgs);
	}

	public static ContentValues getAttendeesContentValues(Attendee attendee) {
		ContentValues values = new ContentValues();
		values.put(Attendee.EVENT_ID, attendee.event_id);
		values.put(Attendee.ATTENDEE_NAME, attendee.attendeeName);
		values.put(Attendee.ATTENDEE_EMAIL, attendee.attendeeEmail);
		values.put(Attendee.ATTENDEE_RELATIONSHIP,
				attendee.attendeeRelationship);
		values.put(Attendee.ATTENDEE_TYPE, attendee.attendeeType);
		values.put(Attendee.ATTENDEE_STATUS, attendee.attendeeStatus);

		return values;
	}

	public static ArrayList<ContentValues> getReminderValues(int event_id) {
		Cursor cursor = getCursorForReminder(event_id);
		ArrayList<ContentValues> reminderValues = new ArrayList<ContentValues>();
		if (cursor.moveToFirst())
			while (!cursor.isAfterLast()) {
				ContentValues reminder = new ContentValues();
				reminder.put(Reminder.EVENT_ID,
						cursor.getInt(cursor.getColumnIndex(Reminder.EVENT_ID)));
				reminder.put(Reminder.MINUTES,
						cursor.getInt(cursor.getColumnIndex(Reminder.MINUTES)));
				reminder.put(Reminder.METHOD,
						cursor.getInt(cursor.getColumnIndex(Reminder.METHOD)));
				reminderValues.add(reminder);
				cursor.moveToNext();
			}
		cursor.close();
		return reminderValues;
	}

	private static Cursor getCursorForReminder(int event_id) {
		String projection[] = { Reminder.EVENT_ID, Reminder.MINUTES,
				Reminder.METHOD };
		String whereClause = Reminder.EVENT_ID + " = ?";
		String whereArgs[] = {String.valueOf(event_id)};

		String orderBy = Event.EVENT_ID;
		Cursor cursor = Email
				.getAppContext()
				.getContentResolver()
				.query(Reminder.CONTENT_URI, projection, whereClause, whereArgs,
						orderBy);
		if (cursor.moveToFirst())

			cursor.moveToFirst();
		return cursor;
	}

	public static ArrayList<Attendee> getAttendeeValues(int event_id) {
		Cursor cursor = getCursorForAttendee(event_id);
		ArrayList<Attendee> attendeeValues = new ArrayList<Attendee>();
		if (cursor.moveToFirst())
			while (!cursor.isAfterLast()) {
				Attendee attendee = new Attendee();
				attendee.event_id = cursor.getString(cursor
						.getColumnIndex(Attendee.EVENT_ID));
				attendee.attendeeEmail = cursor.getString(cursor
						.getColumnIndex(Attendee.ATTENDEE_EMAIL));
				attendee.attendeeStatus = cursor.getInt(cursor
						.getColumnIndex(Attendee.ATTENDEE_STATUS));
				attendeeValues.add(attendee);
				cursor.moveToNext();
			}
		cursor.close();
		return attendeeValues;
	}

	public static ArrayList<Attendee> getAttendeeValuesNoResponse(int event_id) {
		Cursor cursor = getCursorForAttendee(event_id);
		ArrayList<Attendee> attendeeValues = new ArrayList<Attendee>();
		if (cursor.moveToFirst())
			while (!cursor.isAfterLast()) {
				Attendee attendee = new Attendee();
				attendee.attendeeEmail = cursor.getString(cursor
						.getColumnIndex(Attendee.ATTENDEE_EMAIL));
				attendee.attendeeStatus = cursor.getInt(cursor
						.getColumnIndex(Attendee.ATTENDEE_STATUS));

				if (attendee.attendeeStatus == Attendee.ATTENDEE_STATUS_NONE) {
					attendeeValues.add(attendee);
					CalendarLog.d(CalendarConstants.Tag,
							"No response attendee " + attendee.attendeeEmail);
				}
				cursor.moveToNext();

			}
		cursor.close();
		return attendeeValues;
	}

	public static ArrayList<Attendee> getAttendeeValuesAccepted(int event_id) {
		Cursor cursor = getCursorForAttendee(event_id);
		ArrayList<Attendee> attendeeValues = new ArrayList<Attendee>();
		if (cursor.moveToFirst())
			while (!cursor.isAfterLast()) {
				Attendee attendee = new Attendee();
				attendee.attendeeEmail = cursor.getString(cursor
						.getColumnIndex(Attendee.ATTENDEE_EMAIL));
				attendee.attendeeStatus = cursor.getInt(cursor
						.getColumnIndex(Attendee.ATTENDEE_STATUS));

				if (attendee.attendeeStatus == Attendee.ATTENDEE_STATUS_ACCEPTED) {
					attendeeValues.add(attendee);
					CalendarLog.d(CalendarConstants.Tag, "accepted attendee "
							+ attendee.attendeeEmail);
				}
				cursor.moveToNext();

			}
		cursor.close();
		return attendeeValues;
	}

	public static ArrayList<Attendee> getAttendeeValuesDecline(int event_id) {
		Cursor cursor = getCursorForAttendee(event_id);
		ArrayList<Attendee> attendeeValues = new ArrayList<Attendee>();
		if (cursor.moveToFirst())
			while (!cursor.isAfterLast()) {
				Attendee attendee = new Attendee();
				attendee.attendeeEmail = cursor.getString(cursor
						.getColumnIndex(Attendee.ATTENDEE_EMAIL));
				attendee.attendeeStatus = cursor.getInt(cursor
						.getColumnIndex(Attendee.ATTENDEE_STATUS));

				if (attendee.attendeeStatus == Attendee.ATTENDEE_STATUS_DECLINED) {
					attendeeValues.add(attendee);
					CalendarLog.d(CalendarConstants.Tag, "declined attendee "
							+ attendee.attendeeEmail);
				}
				cursor.moveToNext();

			}
		cursor.close();
		return attendeeValues;
	}

	public static ArrayList<Attendee> getAttendeeValuesTentative(int event_id) {
		Cursor cursor = getCursorForAttendee(event_id);
		ArrayList<Attendee> attendeeValues = new ArrayList<Attendee>();
		if (cursor.moveToFirst())
			while (!cursor.isAfterLast()) {
				Attendee attendee = new Attendee();
				attendee.attendeeEmail = cursor.getString(cursor
						.getColumnIndex(Attendee.ATTENDEE_EMAIL));
				attendee.attendeeStatus = cursor.getInt(cursor
						.getColumnIndex(Attendee.ATTENDEE_STATUS));
				if (attendee.attendeeStatus == Attendee.ATTENDEE_STATUS_TENTATIVE)

					attendeeValues.add(attendee);
				cursor.moveToNext();
			}
		cursor.close();
		return attendeeValues;
	}

	private static Cursor getCursorForAttendee(int event_id) {
		String projection[] = { Attendee.EVENT_ID, Attendee.ATTENDEE_EMAIL,
				Attendee.ATTENDEE_STATUS };
		String whereClause = Attendee.EVENT_ID + " = ?";
		String whereArgs[] ={String.valueOf(event_id)};
		String orderBy = Attendee.EVENT_ID;
		Cursor cursor = Email
				.getAppContext()
				.getContentResolver()
				.query(Attendee.CONTENT_URI, projection, whereClause, whereArgs,
						orderBy);
		// if (cursor.moveToFirst())
		//
		// cursor.moveToFirst();
		return cursor;
	}

	public static int isInvite(Event event) {
		CalendarLog.d(CalendarConstants.Tag, "is invite " + event.organizer
				+ " " + getEmail_ID());
		if (!event.organizer.equalsIgnoreCase(getEmail_ID()))
			return 1;
		return 0;
	}

	public static void insertAttendeeToDatabase(Attendee attendee) {

		Email.getAppContext()
				.getContentResolver()
				.insert(Attendee.CONTENT_URI,
						getAttendeesContentValues(attendee));
	}

	public static void insertAttendeeList(Event event, String id) {
		CalendarLog.d(CalendarConstants.Tag, "insert attendee for event "
				+ event.title);
		if (event.attendees != null && !event.attendees.isEmpty()) {
			CalendarLog.d(CalendarConstants.Tag, " attendees not null ");

			for (Attendee attendee : event.attendees) {
				attendee.event_id = id;
				CalendarLog.d(CalendarConstants.Tag, " attendees "
						+ attendee.attendeeEmail + " "
						+ attendee.attendeeStatus);
				insertAttendeeToDatabase(attendee);
			}
			if (event.status != Event.STATUS_SYNCED)
				sendMailForAttendees(event,((id.contains("ex_")?true:false)));

		}
	}

	public static Event convertExceptionToEvent(Exceptions exception) {
		Event event = new Event();
		event._id = exception._id;
		event.event_id = exception.event_id;
		event.allDay = exception.allDay;
		event.title = exception.title;
		event.startDate = exception.startDate;
		event.endDate = exception.endDate;
		event.eventTimezone = exception.eventTimezone;
		event.dtstart = exception.dtstart;
		event.dtend = exception.dtend;
		event.description = exception.description;
		event.eventLocation = exception.eventLocation;
		event.organizer = exception.organizer;
		event.availability = exception.availability;
		event.accessibility = exception.accessibility;
		event.responseType = exception.responseType;
		event.calendarUID = exception.calendarUID;
		event.rrule = exception.rrule;
		event.attendees = exception.attendees;
		event.reminders = exception.reminders;
		event.recurrence = exception.recurrence;
		event.status = exception.status;
		return event;
	}

//	public static void insertAttendeeList(Exceptions exception, long id) {
//		CalendarLog.d(CalendarConstants.Tag, "insert attendee for event "
//				+ exception.title);
//		if (exception.attendees != null && !exception.attendees.isEmpty()) {
//			CalendarLog.d(CalendarConstants.Tag, " attendees not null ");
//
//			for (Attendee attendee : exception.attendees) {
//				attendee.event_id = "" + id;
//				CalendarLog.d(CalendarConstants.Tag, " attendees "
//						+ attendee.attendeeEmail + " "
//						+ attendee.attendeeStatus);
//				insertAttendeeToDatabase(attendee);
//			}
//			if (exception.status != Event.STATUS_SYNCED)
//				sendMailForAttendees(convertExceptionToEvent(exception));
//		}
//	}

	public static ContentValues getRemindersContentValues(Reminder reminder) {
		ContentValues values = new ContentValues();
		values.put(Reminder.EVENT_ID, reminder.event_id); // The ID of the
		// event.
		values.put(Reminder.MINUTES, reminder.minutes); // The minutes prior to
		// the event that the
		// reminder should fire.
		values.put(Reminder.METHOD, reminder.method);
		return values;
	}

	public static void changeStatusToModified(int _id) {
		ContentValues values = new ContentValues();
		String where = "_id= ? and " + Event.STATUS + " != ?";
		String whereArg[] = {String.valueOf(_id),String.valueOf(Event.STATUS_ADDED)};
		values.put(Event.STATUS, Event.STATUS_MODIFIED);

		Email.getAppContext().getContentResolver()
				.update(Event.CONTENT_URI, values, where, whereArg);

	}

	public static void insertReminderToDatabase(Reminder reminder,
			boolean isEnableNotification) {
		Email.getAppContext()
				.getContentResolver()
				.insert(Reminder.CONTENT_URI,
						getRemindersContentValues(reminder));
		CalendarLog.e("Insert Reminder TODB", "insertReminderToDatabase"+isEnableNotification);
		launchNotification(isEnableNotification,
				System.currentTimeMillis() + 60000);

	}

	public static void launchNotification(boolean isEnableNotification,
			long interval) {

		if (isEnableNotification) {

			if (Email.getmSecurePrefs() == null) {
				SharedPreferences mSecurePrefs = new SharedPreferences(
						Email.getAppContext());
				Email.setmSecurePrefs(mSecurePrefs);
			}
			boolean notificationflag = Email.getmSecurePrefs().getBoolean(
					Email.getAppContext().getString(
							R.string.key_parent_notifications), true);
			boolean popupflag = Email.getmSecurePrefs().getBoolean(
					Email.getAppContext().getString(
							R.string.key_child_popup_notification), false);
		
			if ((notificationflag) && (popupflag)) {
				addReminderNotification("Notification & Pop-Up", interval);
			} else if (notificationflag) {
				addReminderNotification("Notification", interval);
			} else {
				// addReminderNotification("",System.currentTimeMillis()+60000);
			}

		}
	}

	public static void addReminderNotification(String notificationType,
			long interval) {

		try {

			/*
			 * String strDate = Email.getPresentDay();
			 * Email.setPresentDate(strDate); Date date = new
			 * SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
			 * .parse(Email.getPresentDay());
			 * 
			 * Calendar cal = Email.getNewCalendar(); cal.setTime(date);
			 * SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy",
			 * Locale.ENGLISH);
			 * 
			 * ArrayList<ContentValues> dayEventsList = CalendarDatabaseHelper
			 * .getDayListValues(sdf.format(cal.getTime())); ArrayList<String>
			 * reminderEventList = new ArrayList<String>();
			 * 
			 * for (int i = 0; i < dayEventsList.size(); i++) { if
			 * (dayEventsList.get(i).getAsString("calendarType")
			 * .equals("CorporateCalendar") &&
			 * !dayEventsList.get(i).getAsString("event") .equals("")) {
			 * String[] event_count = dayEventsList.get(i)
			 * .getAsString("start_time").split(";");
			 * 
			 * for (int j = 0; j < event_count.length; j++) {
			 * 
			 * if (!event_count[j].equalsIgnoreCase("")) {
			 * reminderEventList.add(event_count[j]);
			 * 
			 * } } } }
			 * 
			 * if (reminderEventList.size() > 0) {
			 */

			// String count = "Event" + "(" + reminderEventList.size() +
			// ")";

			// CalendarLog.e("count", "reminderEventList.size()"+count);
			Intent myIntent = new Intent(Email.getAppContext(),
					CalendarMyReceiver.class);
			/*
			 * myIntent.putStringArrayListExtra(CalendarConstants.MSG,
			 * reminderEventList);
			 */
			myIntent.putExtra(CalendarConstants.FLAG_NOTIFICATION_TYPE,
					notificationType);
			AlarmManager alarmManager = (AlarmManager) Email.getAppContext()
					.getSystemService(Service.ALARM_SERVICE);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(
					Email.getAppContext(), 0, myIntent, 0);
			alarmManager.set(AlarmManager.RTC_WAKEUP, interval, pendingIntent);
			/* } */

		} catch (Exception exception) {
			exception.printStackTrace();
		}

	}

	public static boolean compareCurrentTimeToSnooze(String string) {
		
		String now = new SimpleDateFormat("hh:mm aa")
				.format(new java.util.Date().getTime());
		SimpleDateFormat inFormat = new SimpleDateFormat("hh:mm aa");
		SimpleDateFormat outFormat = new SimpleDateFormat("HH:mm");
		String time24;
		boolean eventFlag = false;
		try {
			time24 = outFormat.format(inFormat.parse(now));
			String[] timeConversion = time24.split(":");
			long currentTime_Hours = Long.parseLong(timeConversion[0]);
			long currentTime_Minutes = Long.parseLong(timeConversion[1]);
		
			String[] eventTimeConversion = string.split(":");
			long eventTime_Hours = Long.parseLong(eventTimeConversion[0]);
			long eventTime_Minutes = Long.parseLong(eventTimeConversion[1]);
		
			long currentTimeMilliseconds = (currentTime_Hours * 60 * 60 * 1000)
					+ (currentTime_Minutes * 60 * 1000);
			long eventTimeMilliseconds = (eventTime_Hours * 60 * 60 * 1000)
					+ (eventTime_Minutes * 60 * 1000);
		
			long difference = eventTimeMilliseconds - currentTimeMilliseconds;
			long differenceInMinutes = (difference / 1000 * 60 * 60);
		
			CalendarLog.e("Reminder LIST", "difference > 0"
					+ currentTimeMilliseconds + "::::" + eventTimeMilliseconds);
		
			if ((eventTime_Hours >= currentTime_Hours)) {
				if (difference > 0) {
		
					eventFlag = true;
		
				} else {
					eventFlag = false;
				}
			} else {
				eventFlag = false;
			}
		
		} catch (ParseException e) {
			e.printStackTrace();
			eventFlag = false;
		}
		
		return eventFlag;
	}

	public static void insertReminderList(Event event, long id) {
		if (event.reminders != null) {
			for (Reminder reminder : event.reminders) {
				reminder.event_id = id;
				insertReminderToDatabase(reminder, true);
			}
		}
	}

	public static ContentValues getEventContentValues(Event event) {
		ContentValues values = new ContentValues();
		try {
			if (event._id != 0) {
				values.put("_id", event._id);
			}

			if (event.event_id == 0) {

				Cursor cursor = Email
						.getAppContext()
						.getContentResolver()
						.query(Event.CONTENT_URI,
								new String[] { "MAX(_id) AS _id" }, null, null,
								null);
				if (cursor != null) {
					if (cursor.moveToFirst()) {
						int index = cursor.getInt(cursor.getColumnIndex("_id"));

						values.put(Event.EVENT_ID, index + 1);
					} else
						values.put(Event.EVENT_ID, 1);
					cursor.close();
				}

			} else
				values.put(Event.EVENT_ID, event.event_id);

			values.put(Event.CALENDAR_UID, event.calendarUID);
			values.put(Event.STATUS, event.status);

			if (event.responseType != -1)
				values.put(Event.RESPONSE_TYPE, event.responseType);

			if (event.organizer != null && !event.organizer.isEmpty())
				values.put(Event.ORGANIZER, event.organizer);

			if (event.title != null && !event.title.isEmpty())
				values.put(Event.TITLE, event.title);

			if (event.eventLocation != null && !event.eventLocation.isEmpty())
				values.put(Event.EVENT_LOCATION, event.eventLocation);

			if (event.description != null && !event.description.isEmpty())
				values.put(Event.DESCRIPTION, event.description);

			if (event.startDate != null && !event.startDate.isEmpty())
				values.put(Event.START_DATE, event.startDate);

			if (event.endDate != null && !event.endDate.isEmpty())
				values.put(Event.END_DATE, event.endDate);

			if (event.dtstart != -1)
				values.put(Event.DTSTART, event.dtstart);

			if (event.dtend != -1)
				values.put(Event.DTEND, event.dtend);

			CalendarLog.d(CalendarConstants.Tag,
					"Event time zone when inserting " + event.eventTimezone);
			if (event.eventTimezone != null && !event.eventTimezone.isEmpty())
				values.put(Event.EVENT_TIMEZONE, event.eventTimezone);

			if (event.eventEndTimezone != null
					&& !event.eventEndTimezone.isEmpty())
				values.put(Event.EVENT_END_TIMEZONE, event.eventEndTimezone);

			if (event.allDay != -1)
				values.put(Event.ALL_DAY, event.allDay);

			if (event.rdate != null && !event.rdate.isEmpty())
				values.put(Event.RDATE, event.rdate);

			if (event.exrule != null && !event.exrule.isEmpty())
				values.put(Event.EXRULE, event.exrule);

			if (event.exdate != null && !event.exdate.isEmpty())
				values.put(Event.EXDATE, event.exdate);

			if (event.availability != -1)
				values.put(Event.AVAILABILITY, event.availability);

			if (event.accessibility != -1)
				values.put(Event.ACCESSIBILITY, event.accessibility);

			if (event.hasException != -1)
				values.put(Event.HAS_EXCEPTIONS, event.hasException);

			Recurrence recurrence = event.recurrence;
			if (recurrence != null) {
				values.put(Recurrence.TYPE, recurrence.type);
				values.put(Recurrence.OCCURENCES, recurrence.occurences);
				values.put(Recurrence.INTERVAL, recurrence.interval);
				if (recurrence.dow == -1) {
					recurrence.dow = Integer.parseInt(EmCalendarUtilities
							.generateEasDayOfWeek(recurrence.dowString));
				}
				values.put(Recurrence.DOW, recurrence.dow);
				values.put(Recurrence.DOW_STRING, recurrence.dowString);
				values.put(Recurrence.DOM, recurrence.dom);
				values.put(Recurrence.WOM, recurrence.wom);
				values.put(Recurrence.MOY, recurrence.moy);
				values.put(Recurrence.UNTIL, recurrence.until);
			}
			if ((event.rrule == null || event.rrule.isEmpty())
					&& recurrence != null && recurrence.type != -1) {
				String until = null;
				if (recurrence.until != -1)
					until = CalendarDatabaseHelper
							.getDateForSyncing(recurrence.until);
				event.rrule = EmCalendarUtilities.rruleFromRecurrence(
						recurrence.type, recurrence.occurences,
						recurrence.interval, recurrence.dow, recurrence.dom,
						recurrence.wom, recurrence.moy, until);
			}
			if (event.rrule != null && !event.rrule.isEmpty())
				values.put(Event.RRULE, event.rrule);
		} catch (NullPointerException ex) {
			ex.printStackTrace();

		}
		return values;
	}

	public static Event getEventFromContentValues(ContentValues values) {
		Event event = new Event();
		event._id = values.getAsLong("_id");
		event.event_id = values.getAsInteger(Event.EVENT_ID);
		event.calendarUID = values.getAsString(Event.CALENDAR_UID);
		event.status = values.getAsInteger(Event.STATUS);
		event.responseType = values.getAsInteger(Event.RESPONSE_TYPE);
		event.organizer = values.getAsString(Event.ORGANIZER);
		event.title = values.getAsString(Event.TITLE);
		event.eventLocation = values.getAsString(Event.EVENT_LOCATION);
		event.description = values.getAsString(Event.DESCRIPTION);
		event.startDate = values.getAsString(Event.START_DATE);
		event.endDate = values.getAsString(Event.END_DATE);
		event.dtstart = values.getAsLong(Event.DTSTART);
		event.dtend = values.getAsLong(Event.DTEND);
		event.eventTimezone = values.getAsString(Event.EVENT_TIMEZONE);
		event.eventEndTimezone = values.getAsString(Event.EVENT_END_TIMEZONE);
		event.allDay = values.getAsInteger(Event.ALL_DAY);
		event.rrule = values.getAsString(Event.RRULE);
		event.rdate = values.getAsString(Event.RDATE);
		event.exrule = values.getAsString(Event.EXRULE);
		event.exdate = values.getAsString(Event.EXDATE);
		event.availability = values.getAsInteger(Event.AVAILABILITY);
		event.accessibility = values.getAsInteger(Event.ACCESSIBILITY);

		return event;
	}

	public static long getIDForEventID(long event_id) {
		int id = 0;
		String where = Event.EVENT_ID + " = ?";
		String whereArgs[] ={String.valueOf(event_id)};
		String[] projection = { "_id" };
		Cursor cursor = Email.getAppContext().getContentResolver()
				.query(Event.CONTENT_URI, projection, where, whereArgs, null);
		if (cursor.moveToFirst())
			id = cursor.getInt(cursor.getColumnIndex("_id"));
		cursor.close();
		return id;
	}

	private static void insertExceptionToDatabase(Exceptions exception) {
		long id = 0;
		String where = "_id = ? and "
				+ Exceptions.START_DATE + " =  ?";
		String whereArgs[] ={String.valueOf(exception._id),String.valueOf(exception.startDate)};
		
		if (Email
				.getAppContext()
				.getContentResolver()
				.update(Exceptions.CONTENT_URI,
						getExceptionContentValues(exception), where, whereArgs) == 0) {
			Uri uri = Email
					.getAppContext()
					.getContentResolver()
					.insert(Exceptions.CONTENT_URI,
							getExceptionContentValues(exception));
			id = ContentUris.parseId(uri);
		} else {
			id = exception._id;
		}
		deleteAttendeesForEvent("ex_" + id);
		insertAttendeeList(convertExceptionToEvent(exception), "ex_" + id);

	}

	public static ContentValues getExceptionContentValues(Exceptions exception) {

		ContentValues values = new ContentValues();

		values.put("_id", exception._id);
		values.put(Exceptions.EVENT_ID, exception.event_id);
		values.put(Exceptions.CALENDAR_UID, exception.calendarUID);
		values.put(Exceptions.STATUS, exception.status);
		values.put(Exceptions.IS_EXCEPTION_DELETED,
				exception.isExceptionDeleted);
		values.put(Exceptions.RESPONSE_TYPE, exception.responseType);
		values.put(Exceptions.ORGANIZER, exception.organizer);
		values.put(Exceptions.TITLE, exception.title);
		values.put(Exceptions.EVENT_LOCATION, exception.eventLocation);
		values.put(Exceptions.DESCRIPTION, exception.description);
		values.put(Exceptions.START_DATE, exception.startDate);
		values.put(Exceptions.END_DATE, exception.endDate);
		values.put(Exceptions.DTSTART, exception.dtstart);
		values.put(Exceptions.EXCEPTION_DTSTART, exception.exceptionDtStart);
		values.put(Exceptions.DTEND, exception.dtend);
		CalendarLog.d(CalendarConstants.Tag, "Event time zone when inserting "
				+ exception.eventTimezone);
		values.put(Exceptions.EVENT_TIMEZONE, exception.eventTimezone);
		values.put(Exceptions.EVENT_END_TIMEZONE, exception.eventEndTimezone);
		values.put(Exceptions.ALL_DAY, exception.allDay);
		values.put(Exceptions.EXRULE, exception.exrule);
		values.put(Exceptions.EXDATE, exception.exdate);
		values.put(Exceptions.AVAILABILITY, exception.availability);
		values.put(Exceptions.ACCESSIBILITY, exception.accessibility);
		values.put(Exceptions.ISINVITE, exception.isInvite);
		Recurrence recurrence = exception.recurrence;
		if (recurrence != null) {
			values.put(Recurrence.TYPE, recurrence.type);
			values.put(Recurrence.OCCURENCES, recurrence.occurences);
			values.put(Recurrence.INTERVAL, recurrence.interval);
			if (recurrence.dow == -1) {
				recurrence.dow = Integer.parseInt(EmCalendarUtilities
						.generateEasDayOfWeek(recurrence.dowString));
			}
			values.put(Recurrence.DOW, recurrence.dow);
			values.put(Recurrence.DOW_STRING, recurrence.dowString);
			values.put(Recurrence.DOM, recurrence.dom);
			values.put(Recurrence.WOM, recurrence.wom);
			values.put(Recurrence.MOY, recurrence.moy);
			values.put(Recurrence.UNTIL, recurrence.until);
		}
		if (exception.rrule.isEmpty() && recurrence != null
				&& recurrence.type != -1) {
			String until = null;
			if (recurrence.until != -1)
				until = CalendarDatabaseHelper
						.getDateForSyncing(recurrence.until);
			exception.rrule = EmCalendarUtilities.rruleFromRecurrence(
					recurrence.type, recurrence.occurences,
					recurrence.interval, recurrence.dow, recurrence.dom,
					recurrence.wom, recurrence.moy, until);
		}
		values.put(Exceptions.RRULE, exception.rrule);

		CalendarLog.d(CalendarConstants.Tag,
				"Exception values which are inserted :" + values.toString());
		return values;

	}

	public static void insertExceptionList(Event event, long id) {
		if (event.exceptions != null) {
			for (Exceptions exception : event.exceptions) {
				exception.event_id = event.event_id;
				exception._id = id;
				exception.status = event.status;
				insertExceptionToDatabase(exception);
			}
		}
	}

	public static void deleteException(int _id, String startDate) {
		ContentValues values = new ContentValues();
		String where = "_id= ? and " + Exceptions.START_DATE + " = ?";
		String whereArgs[] = {String.valueOf(_id),startDate};
		values.put(Exceptions.IS_EXCEPTION_DELETED, "1");

		Email.getAppContext().getContentResolver()
				.update(Exceptions.CONTENT_URI, values, where, whereArgs);
	}

	public static ArrayList<Exceptions> getExceptionsForEvent(int event_id) {
		ArrayList<Exceptions> exceptions = new ArrayList<Exceptions>();
		Cursor cursor = getCursorForException(event_id);
		if (cursor.moveToFirst())
			while (!cursor.isAfterLast()) {
				Exceptions exception = new Exceptions();

				exception._id = cursor.getLong(cursor.getColumnIndex("_id"));
				exception.event_id = Integer.parseInt(cursor.getString(cursor
						.getColumnIndex(Exceptions.EVENT_ID)));
				exception.allDay = cursor.getInt(cursor
						.getColumnIndex(Exceptions.ALL_DAY));
				exception.title = cursor.getString(cursor
						.getColumnIndex(Exceptions.TITLE));
				exception.startDate = cursor.getString(cursor
						.getColumnIndex(Exceptions.START_DATE));
				exception.endDate = cursor.getString(cursor
						.getColumnIndex(Exceptions.END_DATE));
				exception.eventTimezone = cursor.getString(cursor
						.getColumnIndex(Exceptions.EVENT_TIMEZONE));
				exception.dtstart = cursor.getLong(cursor
						.getColumnIndex(Exceptions.DTSTART));
				exception.dtend = cursor.getLong(cursor
						.getColumnIndex(Exceptions.DTEND));
				exception.description = cursor.getString(cursor
						.getColumnIndex(Exceptions.DESCRIPTION));
				exception.eventLocation = cursor.getString(cursor
						.getColumnIndex(Exceptions.EVENT_LOCATION));
				exception.organizer = cursor.getString(cursor
						.getColumnIndex(Exceptions.ORGANIZER));
				exception.availability = cursor.getInt(cursor
						.getColumnIndex(Exceptions.AVAILABILITY));
				exception.accessibility = cursor.getInt(cursor
						.getColumnIndex(Exceptions.ACCESSIBILITY));
				exception.responseType = cursor.getInt(cursor
						.getColumnIndex(Exceptions.RESPONSE_TYPE));
				exception.calendarUID = cursor.getString(cursor
						.getColumnIndex(Exceptions.CALENDAR_UID));
				exception.isInvite = cursor.getInt(cursor
						.getColumnIndex(Exceptions.ISINVITE));
				exception.exceptionDtStart = cursor.getLong(cursor
						.getColumnIndex(Exceptions.EXCEPTION_DTSTART));
				exception.isExceptionDeleted = cursor.getInt(cursor
						.getColumnIndex(Exceptions.IS_EXCEPTION_DELETED));

				exceptions.add(exception);
				cursor.moveToNext();
			}
		cursor.close();

		return exceptions;
	}

	private static Cursor getCursorForException(int event_id) {
		String projection[] = { "_id", Exceptions.EVENT_ID,
				Exceptions.CALENDAR_UID, Exceptions.TITLE,
				Exceptions.START_DATE, Exceptions.END_DATE, Exceptions.DTSTART,
				Exceptions.DTEND, Exceptions.EVENT_TIMEZONE,
				Exceptions.ALL_DAY, Exceptions.DESCRIPTION,
				Exceptions.RESPONSE_TYPE, Exceptions.EVENT_LOCATION,
				Exceptions.ORGANIZER, Exceptions.AVAILABILITY,
				Exceptions.ACCESSIBILITY, Exceptions.ISINVITE,
				Exceptions.RRULE, Exceptions.EXCEPTION_DTSTART,
				Exceptions.IS_EXCEPTION_DELETED };
		String whereClause = "_id = ?";
		String whereArgs[] = {String.valueOf(event_id)};
		String orderBy = "_id";
		Cursor cursor = Email
				.getAppContext()
				.getContentResolver()
				.query(Exceptions.CONTENT_URI, projection, whereClause, whereArgs,
						orderBy);

		return cursor;
	}

	public static long insertEventToDatabase(Event event) {
		String where;
		String[] selectionArgs;
		if (event._id == 0L) {
			where = Event.EVENT_ID + "=?";
			selectionArgs = new String[] { "" + event.event_id };

		} else {
			where = "_id=?";
			selectionArgs = new String[] { "" + event._id };

		}
		if (event.calendarUID == null)
			event.calendarUID = UUID.randomUUID().toString();
		Uri uri;
		long id;

		if (Email
				.getAppContext()
				.getContentResolver()
				.update(Event.CONTENT_URI, getEventContentValues(event), where,
						selectionArgs) == 0) {

			uri = Email.getAppContext().getContentResolver()
					.insert(Event.CONTENT_URI, getEventContentValues(event));
			id = ContentUris.parseId(uri);
		} else {
			CalendarLog
					.d(CalendarConstants.Tag, "Event updated " + event.title);
			id = getIDForEventID(event.event_id);
		}
		CalendarDatabaseHelper.deleteAttendeesForEvent("" + id);
		deleteRemindersForEvent((int) id);
		insertAttendeeList(event, "" + id);
		insertReminderList(event, id);
		insertExceptionList(event, id);
		return id;

	}

	public static void insertListToDatabase(ArrayList<Event> eventList) {
		for (Event event : eventList) {
			insertEventToDatabase(event);
		}
	}

	public static void syncAllEvents() {
		ArrayList<Event> eventLists = new ArrayList<Event>();

		if (!eventLists.isEmpty())
			insertListToDatabase(eventLists);
		else
			CalendarLog.e(CalendarConstants.Tag, "Error"
					+ " not inserted to array list");

		ArrayList<String> deletedServerIds = new ArrayList<String>();

		if (!deletedServerIds.isEmpty())
			deleteServerIdsFromDatabase(deletedServerIds);
		else
			CalendarLog.e(CalendarConstants.Tag, "Error"
					+ " not inserted to delete array list");

	}

	private static void deleteServerIdsFromDatabase(
			ArrayList<String> deletedServerIds) {
		for (String id : deletedServerIds) {
			String where = Event.EVENT_ID + " = ? and " + Event.STATUS
					+ " = ?" ;
			
			String whereArgs[] = {id,String.valueOf(Event.STATUS_SYNCED)};
			Email.getAppContext().getContentResolver()
					.delete(Event.CONTENT_URI, where, whereArgs);
		}

	}

	public static String getDateString(String date, int dayDifference,
			int monthDifference) {
		Calendar cal = Email.getNewCalendar();
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.US);
		try {
			cal.setTime(sdf.parse(date));
		} catch (ParseException e) {
			e.printStackTrace();
		}// all done
		SimpleDateFormat newSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + monthDifference);
		cal.set(Calendar.DATE, cal.get(Calendar.DATE) + dayDifference);
		newSdf.format(cal.getTime());

		String stringDate = newSdf.format(cal.getTime());

		return stringDate;

	}

	/****************** CREATE EVENT ********************/
	public static long getLongFromTime(String date,String tz) {

		SimpleDateFormat sdf = new SimpleDateFormat("E,dd MMM yyyy HH:mm",
				Locale.US);
		sdf.setTimeZone(getTimeZoneFromString(tz));
		long time = 0;
		try {
			time = sdf.parse(date).getTime();
			CalendarLog.d(CalendarConstants.Tag, "aish getLongFromTime "+sdf.parse(date).toString());
		} catch (ParseException e) {

			e.printStackTrace();

		}
		
		
		return time;
	}

	public static long getLongFromDate(String date) {

		SimpleDateFormat sdf = new SimpleDateFormat("E,dd MMM yyyy", Locale.US);
		long time = 0;
		try {
			time = sdf.parse(date).getTime();
		} catch (ParseException e) {

			e.printStackTrace();

		}
		return time;
	}

//	public static String getDateFormatForDatabase(String date) {
//		long time = getLongFromTime(date);
//		return getDateFromLong(time);
//
//	}
	
	public static long getLongForPresentDate(String date) {

		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.US);
		long time = 0;
		try {
			time = sdf.parse(date).getTime();
		} catch (ParseException e) {

			e.printStackTrace();

		}
		return time;
	}

	public static String getDateFormatForAgenda(String date) {
		String stringDate = "";

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		try {
			Date datenew = sdf.parse(date);
			sdf.applyPattern("dd MMMM yyyy,");
			stringDate = sdf.format(datenew);

		} catch (ParseException e) {
			e.printStackTrace();
		}// all done

		return stringDate;
	}

	public static String getDateFormatForPresentDay(String date) {
		String stringDate = "";

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		try {
			Date datenew = sdf.parse(date);
			sdf.applyPattern("dd MMM yyyy");
			stringDate = sdf.format(datenew);

		} catch (ParseException e) {
			e.printStackTrace();
		}// all done

		return stringDate;
	}

	public static String getDateForAgenda(String date) {
		String stringDate = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		try {
			Date datenew = sdf.parse(date);
			sdf.applyPattern("EEEE dd MMMM,");
			stringDate = sdf.format(datenew);

		} catch (ParseException e) {
			e.printStackTrace();
		}// all done

		return stringDate;
	}

	/*************************** DAY VIEW ********************************************************************/
	private static Cursor getAllEventsByDate(String date) {

		try {
			String[] days = date.split(" ");
			// Log.e("check time","check s"+days[0] + " -" +date);

			Calendar cal = Email.getNewCalendar();
			cal.setFirstDayOfWeek(Calendar.SUNDAY);
			SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy",
					Locale.US);
			try {
				cal.setTime(sdf.parse(date));
			} catch (ParseException e) {
				e.printStackTrace();
			}// all done
			cal.set(Calendar.DAY_OF_MONTH, Integer.valueOf(days[0]));
			cal.set(Calendar.MONTH, cal.get(Calendar.MONTH));
			cal.set(Calendar.YEAR, Integer.valueOf(days[2]));
			int dayoftheweek = cal.get(Calendar.DAY_OF_WEEK);

			String today = days[0];
			String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
			if (Integer.valueOf(today) < 10 && today.length() == 1) {
				today = "0" + today;
			}
			if (Integer.valueOf(month) < 10 && month.length() == 1) {
				month = "0" + month;
			}
			int weekday = Integer.parseInt(today) / 7;
			weekday++;
			dayoftheweek = dayoftheweek - 1;
			String projection[] = { "_id", Event.TITLE, Event.START_DATE,
					Event.EVENT_LOCATION, Event.END_DATE, Event.DTSTART,
					Event.DTEND, Event.EVENT_TIMEZONE, Event.ALL_DAY,
					Event.RRULE, Recurrence.TYPE, Recurrence.OCCURENCES,
					Recurrence.INTERVAL, Recurrence.DOW, Recurrence.DOW_STRING,
					Recurrence.DOM, Recurrence.WOM, Recurrence.MOY,
					Recurrence.UNTIL, Event.HAS_EXCEPTIONS };

			String eventDate = getDateString(date, 0, 0);
			long[] timeInterval = CalendarCommonFunction
					.convertDateToMillisec(eventDate);
			String whereDailyRecurrence = "(" + Recurrence.TYPE + " = ? and "
					+ Event.START_DATE + " <= ? and (((julianday('" + eventDate
					+ "')- julianday(" + Event.START_DATE + ")) % "
					+ Recurrence.INTERVAL + " = CAST( ? as INTEGER) or " + Recurrence.INTERVAL
					+ "= ?) and ((julianday('" + eventDate + "')- julianday("
					+ Event.START_DATE + ")) < (" + Recurrence.INTERVAL + " * "
					+ Recurrence.OCCURENCES + ") or " + Recurrence.OCCURENCES
					+ " = ?) and (" + Recurrence.UNTIL + " >= ? or " + Recurrence.UNTIL
					+ " = ?)))";
			
			

			String whereWeeklyRecurrence = "(" + Recurrence.TYPE + " = ? and "
					+ Event.START_DATE + " <= ? and (((julianday('" + eventDate
					+ "')- julianday(" + Event.START_DATE + ")) % "
					+ Recurrence.INTERVAL + " = ? or " + Recurrence.INTERVAL
					+ "= ?) and ((julianday('" + eventDate + "')- julianday("
					+ Event.START_DATE + ")) < (" + Recurrence.INTERVAL + " * "
					+ Recurrence.OCCURENCES + ") or " + Recurrence.OCCURENCES
					+ " = ?) and (" + Recurrence.UNTIL + " >= ? or " + Recurrence.UNTIL
					+ " = ?)) and " + Recurrence.DOW_STRING + " LIKE ?)";

			
			
			
			String whereMonthlyRecurrence = "((" + Recurrence.TYPE
					+ " = ? and " + Event.START_DATE + " <= ?  and "
					+ Recurrence.DOM + " = ?  and ("
					+ Recurrence.UNTIL + " >= ? or " + Recurrence.UNTIL + " = -1)) or ("
					+ Recurrence.TYPE + " = ? and " + Event.START_DATE
					+ " <= ?  and (" + Recurrence.UNTIL + " >= ? or " + Recurrence.UNTIL
					+ " = ?) and " + Recurrence.WOM + " = ? and " + Recurrence.DOW_STRING + " LIKE ? ))";			
			
			

			String whereYearlyRecurrence = "((" + Recurrence.TYPE + " = ? and "
					+ Event.START_DATE + " <= ? and " + Recurrence.DOM + " = ? and " + 
					Recurrence.MOY + " = ?  and (" + Recurrence.UNTIL + " >= ? or " + Recurrence.UNTIL
					+ " = ?)) or " + "(" + Recurrence.TYPE + " = ? and "
					+ Event.START_DATE + " <= ?  and (" + Recurrence.UNTIL
					+ " >= ? or "+ Recurrence.UNTIL + " = ?) and " + Recurrence.WOM + " = ? and " + 
					Recurrence.DOW_STRING + " LIKE ?))";
			
			

			String whereClause = "((" + Event.START_DATE + " = ? and "
					+ Recurrence.TYPE + " < ? and " + Event.DTSTART
					+ " >= ?) or (" + Event.END_DATE + "= ? and " + Event.DTEND
					+ " <= ? and " + Recurrence.TYPE
					+ " < ? )" + " or " + whereDailyRecurrence + " or "
					+ whereWeeklyRecurrence + " or " + whereMonthlyRecurrence
					+ "  or " + whereYearlyRecurrence + ") and "
					+ Event.ALL_DAY + " = ? and " + Event.STATUS + " != ?";
			
			String[] whereArgs = new String[] { getDateString(date, 0, 0),"0",
					Long.toString(timeInterval[0]), getDateString(date, 0, 0),String.valueOf(timeInterval[1]),"0",
					"0",getDateString(date, 0, 0),"0","-1","-1",String.valueOf(cal.getTimeInMillis()),"-1" ,//Daily
					
					"1",getDateString(date, 0, 0),"0","-1","-1",String.valueOf(cal.getTimeInMillis()),"-1",
					"%"+ CalendarWeekPagerAdapter.getWeekDayOfDate(cal, 2)+"%",//Weekly
					
					"2",getDateString(date, 0, 0),String.valueOf(today), String.valueOf(cal.getTimeInMillis()),
					"3",getDateString(date, 0, 0),String.valueOf(cal.getTimeInMillis()),"-1",String.valueOf(weekday),
					"%"+ CalendarWeekPagerAdapter.getWeekDayOfDate(cal, 2)+"%",//Monthly
					
					"5",getDateString(date, 0, 0), String.valueOf(today),String.valueOf(month) ,
					String.valueOf(cal.getTimeInMillis()),"-1",
					"6",getDateString(date, 0, 0),
					String.valueOf(cal.getTimeInMillis()) ,"-1",String.valueOf(weekday),
					"%"+ CalendarWeekPagerAdapter.getWeekDayOfDate(cal, 2)+"%",//Yearly
					"0",String.valueOf(Event.STATUS_DELETED)};

			// CalendarLog.d(CalendarConstants.Tag, "timeInterval[1] "
			// + timeInterval[1]);
			
			String orderBy = Event.DTSTART;

			// CalendarLog.e("Reminder LIST", "RECEIVER" + "projection"
			// + projection + "whereClause" + whereClause + "whereArgs"
			// + whereArgs + "orderBy" + orderBy);

			Cursor cursor = Email
					.getAppContext()
					.getContentResolver()
					.query(Event.CONTENT_URI, projection, whereClause,
							whereArgs, orderBy);
			if (cursor != null) {
				// CalendarLog.e("Reminder LIST", "RECEIVER  cursor not null");
				if (cursor.moveToFirst())
					CalendarLog.d(
							CalendarConstants.Tag,
							"The number of rows returned " + ""
									+ cursor.getCount());
				cursor.moveToFirst();
				return cursor;
			} else {
				// CalendarLog.e("Reminder LIST", "RECEIVER  cursor  null");
				return null;
			}

		} catch (NumberFormatException e) {
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public static ArrayList<ContentValues> getDayListValues() {
		return getDayListValues(Email.getPresentDay());

	}

	private static Cursor getAllDayEventsByDate(String date) {

		try {
			String projection[] = { "_id", Event.TITLE, Event.START_DATE,
					Event.END_DATE, Event.DTSTART, Event.DTEND, Event.ALL_DAY,
					Recurrence.TYPE, Recurrence.OCCURENCES,
					Recurrence.INTERVAL, Recurrence.DOW, Recurrence.DOW_STRING,
					Recurrence.DOM, Recurrence.WOM, Recurrence.MOY,
					Recurrence.UNTIL };
			String whereClause = "(" + Event.START_DATE + " = ? ) and (("
					+ Event.ALL_DAY + " = ?) or ((" + Event.DTEND + " - "
					+ Event.DTSTART + ") >= ? )" + ") and "
					+ Event.STATUS + " != ?" ;
			String[] whereArgs = new String[] { getDateString(date, 0, 0),"1","86400000",String.valueOf(Event.STATUS_DELETED) };
			String orderBy = Event.START_DATE;
			Cursor cursor = Email
					.getAppContext()
					.getContentResolver()
					.query(Event.CONTENT_URI, projection, whereClause,
							whereArgs, orderBy);
			return cursor;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// private static Cursor getAllDayEventsByDate(String date)
	// {
	//
	//
	// String whereClause = Event.START_DATE+" =? and (("+
	// Event.ALL_DAY+" = 1) or (("+Event.DTEND+" - "+Event.DTSTART+") > 86400000))";
	// String[] whereArgs = new String[] {
	// getDateString(date,0,0),
	// };
	// String orderBy = Event.START_DATE;
	// Cursor
	// cursor=Email.getAppContext().getContentResolver().query(Event.CONTENT_URI,projection,
	// whereClause, whereArgs, orderBy);
	// if(cursor.moveToFirst())
	// //CalendarLog.d(CalendarConstants.Tag,"The number of rows returned ",""+cursor.getCount());
	// cursor.moveToFirst();
	// return cursor;
	// }
	public static ArrayList<ContentValues> getAllDayListValues(String date) {
		ArrayList<ContentValues> mergedAllDayEvent = new ArrayList<ContentValues>();
		ArrayList<ContentValues> personalAllDayEvent;
		personalAllDayEvent = CalendarDBHelperClassPreICS
				.getPersonalAllDayListValues(date);

		Cursor cursor = getAllDayEventsByDate(date);
		ArrayList<ContentValues> dayValues = new ArrayList<ContentValues>();
		try
		{
		if (cursor != null) {
			dayValues = new ArrayList<ContentValues>(cursor.getCount());
			if (cursor.moveToFirst()) {

				// CalendarLog.d(CalendarConstants.Tag,"number of rows in cursor ",""+cursor.getCount());
				cursor.moveToFirst();

				while (!cursor.isAfterLast()) {
					String eventTitle = "";
					String eventEndDate = "";
					String eventStartDate = "";
					int eventID = -1;
					int allDay = 0;

					ContentValues dayValue = new ContentValues();

					eventID = cursor.getInt(cursor.getColumnIndex("_id"));

					eventTitle = cursor.getString(cursor
							.getColumnIndex(Event.TITLE));
					eventEndDate = cursor.getString(cursor
							.getColumnIndex(Event.END_DATE));
					eventStartDate = cursor.getString(cursor
							.getColumnIndex(Event.START_DATE));
					allDay = cursor
							.getInt(cursor.getColumnIndex(Event.ALL_DAY));
					// CalendarLog.d(CalendarConstants.Tag,"title",eventTitle);

					dayValue.put("event", eventTitle);
					dayValue.put("start_date", eventStartDate);
					dayValue.put("end_date", eventEndDate);
					dayValue.put("_id", eventID);
					dayValue.put("all_day", allDay);
					dayValue.put("calendarType", "CorporateCalendar");
					// //CalendarLog.d(CalendarConstants.Tag,"_id After",""+dayValue.get("_id"));
					// CalendarLog.d(CalendarConstants.Tag,""+dayValue.get("event"),""+allDay);
					dayValue.put(Recurrence.TYPE, cursor.getInt(cursor
							.getColumnIndex(Recurrence.TYPE)));
					dayValue.put(Recurrence.OCCURENCES, cursor.getInt(cursor
							.getColumnIndex(Recurrence.OCCURENCES)));
					dayValue.put(Recurrence.INTERVAL, cursor.getInt(cursor
							.getColumnIndex(Recurrence.INTERVAL)));
					dayValue.put(Recurrence.DOW, cursor.getInt(cursor
							.getColumnIndex(Recurrence.DOW)));
					dayValue.put(Recurrence.DOW_STRING, cursor.getString(cursor
							.getColumnIndex(Recurrence.DOW_STRING)));
					dayValue.put(Recurrence.DOM, cursor.getInt(cursor
							.getColumnIndex(Recurrence.DOM)));
					dayValue.put(Recurrence.WOM, cursor.getInt(cursor
							.getColumnIndex(Recurrence.WOM)));
					dayValue.put(Recurrence.MOY, cursor.getInt(cursor
							.getColumnIndex(Recurrence.MOY)));
					dayValue.put(Recurrence.UNTIL, cursor.getLong(cursor
							.getColumnIndex(Recurrence.UNTIL)));
					dayValues.add(dayValue);
					cursor.moveToNext();
				}

			}
			cursor.close();
		}
		mergedAllDayEvent.addAll(dayValues);

		if (displayPersonalEvent()) {
			mergedAllDayEvent.addAll(personalAllDayEvent);
		}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return mergedAllDayEvent;
	}

	private static Cursor getAllDayEventsByEndDate(String date) {

		long endtime = getLongForPresentDate(date);
		String projection[] = { "_id", Event.TITLE, Event.START_DATE,
				Event.END_DATE, Event.DTSTART, Event.DTEND, Event.ALL_DAY };
		String whereClause = Event.START_DATE + " < ? and " + Event.DTEND
				+ " > ? and ((" + Event.ALL_DAY + " = ?) or ((" + Event.DTEND
				+ " - " + Event.DTSTART + ") > ?)" + ") and "
				+ Event.STATUS + " != " + Event.STATUS_DELETED + "";
		String[] whereArgs = new String[] { getDateString(date, 0, 0),
				String.valueOf(endtime),"1","86400000" };
		String orderBy = Event.START_DATE;
		Cursor cursor = Email
				.getAppContext()
				.getContentResolver()
				.query(Event.CONTENT_URI, projection, whereClause, whereArgs,
						orderBy);

		return cursor;
	}

	public static ArrayList<ContentValues> getAllDayListValuesForEndDate(
			String date) {
		Cursor cursor = getAllDayEventsByEndDate(date);
		ArrayList<ContentValues> dayValues = new ArrayList<ContentValues>();
		try
		{
		if (cursor != null && cursor.moveToFirst()) {

			// CalendarLog.d(CalendarConstants.Tag,"number of rows in cursor ",""+cursor.getCount());
			cursor.moveToFirst();

			while (!cursor.isAfterLast()) {
				String eventTitle = "";
				String eventEndDate = "";
				String eventStartDate = "";
				int eventID = -1;
				int allDay = 0;

				ContentValues dayValue = new ContentValues();

				eventID = cursor.getInt(cursor.getColumnIndex("_id"));

				eventTitle = cursor.getString(cursor
						.getColumnIndex(Event.TITLE));
				eventEndDate = cursor.getString(cursor
						.getColumnIndex(Event.END_DATE));
				eventStartDate = cursor.getString(cursor
						.getColumnIndex(Event.START_DATE));
				allDay = cursor.getInt(cursor.getColumnIndex(Event.ALL_DAY));
				// CalendarLog.d(CalendarConstants.Tag,"title",eventTitle);

				dayValue.put("event", eventTitle);
				dayValue.put("start_date", eventStartDate);
				dayValue.put("end_date", eventEndDate);
				dayValue.put("_id", eventID);
				dayValue.put("all_day", allDay);
				dayValue.put("calendarType", "CorporateCalendar");
				// //CalendarLog.d(CalendarConstants.Tag,"_id After",""+dayValue.get("_id"));
				// CalendarLog.d(CalendarConstants.Tag,""+dayValue.get("event"),""+allDay);
				dayValues.add(dayValue);
				cursor.moveToNext();
			}

		}
		if (cursor != null)
			cursor.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		return dayValues;
	}

	/*
	 * To get the event details for given date
	 */

	public static ArrayList<ContentValues> getDayListValues(String date) {
	ArrayList<ContentValues> mergedValues = new ArrayList<ContentValues>();
	try {
		ArrayList<ContentValues> personalCalendarDayList;
		Cursor cursor = getAllEventsByDate(date);
		ArrayList<ContentValues> dayValues = new ArrayList<ContentValues>(
				CalendarConstants.HOURS.length);
		personalCalendarDayList = CalendarDBHelperClassPreICS
				.getPersonalDayListValues(date);

		int index = 0;
		CalendarLog.d(CalendarConstants.Tag, "Date " + date);
		if (cursor != null){
			cursor.moveToFirst();
		while (index < CalendarConstants.HOURS.length) {
			ContentValues dayValue = new ContentValues();
			String eventTitle = "";
			dayValue.put("_id", "");
			dayValue.put("hours", CalendarConstants.HOURS[index]);
			dayValue.put("event", eventTitle);
			dayValue.put("bg_color", Email.getAppContext()
					.getResources().getColor(R.color.event_bgcolor));
			dayValue.put("text_color", Color.BLACK);
			dayValue.put("start_time", "-1");
			dayValue.put("end_time", "-1");
			dayValue.put("all_day", "0");
			dayValue.put(Event.EVENT_LOCATION, "");
			dayValue.put("calendarType", "CorporateCalendar");
			dayValues.add(dayValue);
			index++;

		}
		String eventDate = getDateString(date, 0, 0);
		long[] dayTime = CalendarCommonFunction.convertDateToMillisec(eventDate);
		while (!cursor.isAfterLast()) {
			int startTimeHours;
			int endTimeHours;
			// int durationHours;
			String eventTitle = "";
			String eventLocation = "";
			String eventStartTime = "";
			String eventEndTime = "";
			String hasException = "-1";
			int eventID = -1;
			Exceptions exception = new Exceptions();
			int allDay = 0;
			long startTimeLong = 0;
			long endTimeLong = 0;
			long durationLong;
			int reccurentType = -1;
			boolean isException = false;
			boolean isDeleted = false;
			Date startingDate = null, endingDate = null;
			hasException = cursor.getString(cursor
					.getColumnIndex(Event.HAS_EXCEPTIONS));
			int checkTime = 0;
			int i = 0;

			eventID = cursor.getInt(cursor.getColumnIndex("_id"));
			reccurentType = cursor.getInt(cursor
					.getColumnIndex(Recurrence.TYPE));

			CalendarLog.d(CalendarConstants.Tag, "HAS_EXCEPTIONS:"
					+ hasException);
			CalendarLog.d(
					CalendarConstants.Tag,
					"TITLE:"
							+ cursor.getString(cursor
									.getColumnIndex(Event.TITLE)));
			CalendarLog.d(CalendarConstants.Tag, "date:" + date);
			// ArrayList<Exceptions> exceptionListT =
			// getExceptionsForEvent(24);
			// CalendarLog.d(CalendarConstants.Tag,
			// "sizeTT:" + exceptionListT.size());
			if (hasException.equalsIgnoreCase("1")) {
				ArrayList<Exceptions> exceptionList = getExceptionsForEvent(eventID);
				CalendarLog.d(CalendarConstants.Tag, "size:"
						+ exceptionList.size());
				for (Exceptions excepti : exceptionList) {
					if (excepti.isExceptionDeleted == 1) {
						CalendarLog.d(CalendarConstants.Tag,
								"isException if:" + isException);
						// cursor.moveToNext();
						if (excepti.startDate
								.equalsIgnoreCase(getDateString(date, 0, 0))) {
							isDeleted = true;
							isException = true;
						}

					} else {
						CalendarLog.d(CalendarConstants.Tag,
								"isException else:" + isException);
						
						CalendarLog.d(CalendarConstants.Tag,
								"excepti.isExceptionDeleted  else:" + excepti.isExceptionDeleted );
						
						CalendarLog.d(CalendarConstants.Tag,
								"excepti  else:" + excepti );
						CalendarLog.d(
								CalendarConstants.Tag,
								"isException else date:"
										+ getDateString(date, 0, 0));
						// getExceptionContentValues(excepti);
						if (excepti.startDate
								.equalsIgnoreCase(getDateString(date, 0, 0))) {
							if(excepti.endDate !=null){
							isException = true;
							exception = excepti;
							try {
								startingDate = new SimpleDateFormat(
										"yyyy-MM-dd", Locale.US)
										.parse(excepti.startDate);
								endingDate = new SimpleDateFormat(
										"yyyy-MM-dd", Locale.US)
										.parse(excepti.endDate);
							} catch (ParseException e) {
								e.printStackTrace();
							}

							startTimeLong = excepti.dtstart;
							endTimeLong = excepti.dtend;
							startTimeHours = new Date(startTimeLong)
									.getHours();
							endTimeHours = new Date(endTimeLong).getHours();
							if (startingDate.equals(endingDate) || (startTimeLong >= dayTime[0] && endTimeLong <= (dayTime[0]+86400000))) {
								checkTime = startTimeHours;
							} else {
								durationLong = endTimeLong - startTimeLong;
								if (durationLong >= 86400000) {
									checkTime = -1;
								} else {
									if (startingDate.equals(new Date(date))) {
										checkTime = startTimeHours;
										endTimeLong = -1;
									} else {
										checkTime = endTimeHours;
										startTimeLong = -1;
									}
								}

							}
							}else{
								isDeleted=true;
							}

						}
					}
				}
			}
			CalendarLog.d(CalendarConstants.Tag, "isException out:"
					+ isException);
			if (!isDeleted) {
				if (!isException) {
					try {
						startingDate = new SimpleDateFormat("yyyy-MM-dd",
								Locale.US).parse(cursor.getString(cursor
								.getColumnIndex(Event.START_DATE)));
						endingDate = new SimpleDateFormat("yyyy-MM-dd",
								Locale.US).parse(cursor.getString(cursor
								.getColumnIndex(Event.END_DATE)));
					} catch (ParseException e) {
						e.printStackTrace();
					}

					startTimeLong = cursor.getLong(cursor
							.getColumnIndex(Event.DTSTART));
					endTimeLong = cursor.getLong(cursor
							.getColumnIndex(Event.DTEND));
					startTimeHours = new Date(startTimeLong).getHours();
					// Log.d("startTime ",""+startTimeHours);
					endTimeHours = new Date(endTimeLong).getHours();
					// Log.d("endTime ",""+endTimeHours);
					// durationHours = endTimeHours - startTimeHours;

					CalendarLog.d(CalendarConstants.Tag, "startingDate out:"
							+ startingDate);
					CalendarLog.d(CalendarConstants.Tag, "startTimeLong out:"
							+ startTimeLong);
					CalendarLog.d(CalendarConstants.Tag, "endTimeLong out:"
							+ endTimeLong);
					CalendarLog.d(CalendarConstants.Tag, "checkTime out:"
							+ checkTime);
					CalendarLog.d(CalendarConstants.Tag, "checkTime dayTime[0]:"
							+ dayTime[0]);
					CalendarLog.d(CalendarConstants.Tag, "checkTime dayTime[1]:"
							+ dayTime[1]);
					// To draw cont events for two days
					if (startingDate.equals(endingDate) || (startTimeLong >= dayTime[0] && endTimeLong <= (dayTime[0]+86400000))) {
						checkTime = startTimeHours;
					} else {
						durationLong = endTimeLong - startTimeLong;
						if (durationLong >= 86400000) {
							checkTime = -1;
						} else {
							if (startingDate.equals(new Date(date))
									|| reccurentType >= 0) {
								checkTime = startTimeHours;
								endTimeLong = -1;
							} else {
								checkTime = endTimeHours;
								startTimeLong = -1;
							}
						}

					}
				}
				CalendarLog.d(CalendarConstants.Tag, "startingDate out:"
						+ startingDate);
				CalendarLog.d(CalendarConstants.Tag, "endingDate out:"
						+ endingDate);
				CalendarLog.d(CalendarConstants.Tag, "startTimeLong out:"
						+ startTimeLong);
				CalendarLog.d(CalendarConstants.Tag, "endTimeLong out:"
						+ endTimeLong);
				CalendarLog.d(CalendarConstants.Tag, "checkTime out:"
						+ checkTime);
				// for(ContentValues dayValue:dayValues)
				for (i = 0; i < dayValues.size(); i++) {
					ContentValues dayValue = dayValues.get(i);

					if (checkTime != -1
							&& checkTime >= (Long
									.parseLong((String) dayValue
											.get("hours")))
							&& checkTime < (Long
									.parseLong((String) dayValue
											.get("hours")) + 1)) {
						// CalendarLog.d(CalendarConstants.Tag,"check time",""+checkTime);
						eventID = cursor.getInt(cursor
								.getColumnIndex("_id"));
						hasException = cursor.getString(cursor
								.getColumnIndex(Event.HAS_EXCEPTIONS));
						String timeZone = cursor.getString(cursor
								.getColumnIndex(Event.EVENT_TIMEZONE));
						if (isException) {

							eventTitle = exception.title;
							eventLocation = exception.eventLocation;

							if (eventLocation == null)
								eventLocation = "";
							if (startTimeLong == -1)
								eventStartTime = "00:00";
							else {
								CalendarLog.d(CalendarConstants.Tag,
										" daylist values startTime");
								eventStartTime = getTimeFromLong(
										startTimeLong, getDefaultTimeZone());
								CalendarLog.d(CalendarConstants.Tag,
										" getTimefromLong starttime"
												+ eventStartTime);
							}
							// CalendarLog.d(CalendarConstants.Tag," start time obtained from db is "+eventStartTime);
							if (endTimeLong == -1)
								eventEndTime = "23:59";
							else {
								CalendarLog.d(CalendarConstants.Tag,
										" daylist values endTime");
								eventEndTime = getTimeFromLong(endTimeLong,
										getDefaultTimeZone());
								CalendarLog.d(CalendarConstants.Tag,
										" getTimefromLong endTime"
												+ eventEndTime);
							}
							 CalendarLog.d(CalendarConstants.Tag," end time obtained from db is "+eventEndTime);
							 long newstartTimeLong = cursor.getLong(cursor
										.getColumnIndex(Event.DTSTART));
								long newendTimeLong = cursor.getLong(cursor
										.getColumnIndex(Event.DTEND));
								CalendarLog.d(CalendarConstants.Tag,
										"getTimefromLong newendTimeLong"
												+ newendTimeLong);
								CalendarLog.d(CalendarConstants.Tag,
										" getTimefromLong dayTime[1]"
												+ dayTime[1]);
								if(newstartTimeLong>=dayTime[0] || newendTimeLong>=dayTime[0])
								{
							if (eventEndTime.equalsIgnoreCase("00:00") && !(eventStartTime.equalsIgnoreCase("00:00")))
								eventEndTime = "23:59";

							allDay = exception.allDay;
							 CalendarLog.d(CalendarConstants.Tag," end time obtained from db is "+eventEndTime);
							if (dayValue.getAsString("start_time")
									.equalsIgnoreCase("-1")) {
								dayValue.put("start_time", "");
								// //CalendarLog.d(CalendarConstants.Tag,"ST if",""+dayValue.get("start_time"));
							}
							CalendarLog.d(CalendarConstants.Tag,
									"eventTitle" + "" + eventTitle);
							CalendarLog.d(CalendarConstants.Tag,
									"eventLocation" + "" + eventLocation);

							if (dayValue.getAsString("end_time")
									.equalsIgnoreCase("-1"))
								dayValue.put("end_time", "");
							dayValue.put("event", dayValue.get("event")
									+ ";" + eventTitle);
							dayValue.put("_id", dayValue.get("_id") + ";"
									+ eventID);
							dayValue.put("start_time",
									dayValue.get("start_time") + ";"
											+ eventStartTime);
							dayValue.put("end_time",
									dayValue.get("end_time") + ";"
											+ eventEndTime);
							dayValue.put("all_day", allDay);
							dayValue.put(Event.EVENT_LOCATION,
									dayValue.get(Event.EVENT_LOCATION)
											+ ";" + eventLocation);

							dayValue.put("bg_color", Email
									.getAppContext().getResources()
									.getColor(R.color.event_bgcolor));
							Recurrence recrr = exception.recurrence;
							dayValue.put(Recurrence.TYPE,
									exception.responseType);
							if (recrr != null) {
								dayValue.put(Recurrence.OCCURENCES,
										recrr.occurences);
								dayValue.put(Recurrence.INTERVAL,
										recrr.interval);
								dayValue.put(Recurrence.DOW, recrr.dow);
								dayValue.put(Recurrence.DOW_STRING,
										recrr.dowString);
								dayValue.put(Recurrence.DOM, recrr.dom);
								dayValue.put(Recurrence.WOM, recrr.wom);
								dayValue.put(Recurrence.MOY, recrr.moy);
								dayValue.put(Recurrence.UNTIL, recrr.until);
							} else {
								dayValue.put(Recurrence.OCCURENCES, "-1");
								dayValue.put(Recurrence.INTERVAL, "-1");
								dayValue.put(Recurrence.DOW, "-1");
								dayValue.put(Recurrence.DOW_STRING, "");
								dayValue.put(Recurrence.DOM, "-1");
								dayValue.put(Recurrence.WOM, "-1");
								dayValue.put(Recurrence.MOY, "-1");
								dayValue.put(Recurrence.UNTIL, "-1");
							}
							dayValue.put(
									CalendarConstants.RECURRENCE_CURRENTDATE,
									date);
							CalendarLog.d(CalendarConstants.Tag,
									"RECURRENCE_CURRENTDATE" + date);
							CalendarLog
									.d(CalendarConstants.Tag,
											"Recurrence.UNTIL"
													+ cursor.getLong(cursor
															.getColumnIndex(Recurrence.UNTIL)));
							CalendarLog.d(CalendarConstants.Tag, "event"
									+ "" + dayValue.get("event"));
							CalendarLog.d(
									CalendarConstants.Tag,
									"start_time" + ""
											+ dayValue.get("start_time"));
							CalendarLog.d(CalendarConstants.Tag, "end_time"
									+ "" + dayValue.get("end_time"));
								}

						} else {

							eventTitle = cursor.getString(cursor
									.getColumnIndex(Event.TITLE));
							eventLocation = cursor.getString(cursor
									.getColumnIndex(Event.EVENT_LOCATION));

							if (eventLocation == null)
								eventLocation = "";
							if (startTimeLong == -1)
								eventStartTime = "00:00";
							else {
								CalendarLog.d(CalendarConstants.Tag,
										" daylist values startTime");
								eventStartTime = getTimeFromLong(
										startTimeLong, getDefaultTimeZone());
								CalendarLog.d(CalendarConstants.Tag,
										" getTimefromLong starttime"
												+ eventStartTime);
							}
							if (endTimeLong == -1)
								eventEndTime = "23:59";
							else {
								CalendarLog.d(CalendarConstants.Tag,
										" daylist values endtime");
								eventEndTime = getTimeFromLong(endTimeLong,
										getDefaultTimeZone());
								CalendarLog.d(CalendarConstants.Tag,
										" getTimefromLong endtime"
												+ eventEndTime);
							}
							long newstartTimeLong = cursor.getLong(cursor
									.getColumnIndex(Event.DTSTART));
							long newendTimeLong = cursor.getLong(cursor
									.getColumnIndex(Event.DTEND));
							CalendarLog.d(CalendarConstants.Tag,
									"getTimefromLong newendTimeLong"
											+ newendTimeLong);
							CalendarLog.d(CalendarConstants.Tag,
									" getTimefromLong dayTime[1]"
											+ dayTime[1]);
							int recurrenceType = cursor.getInt(cursor
									.getColumnIndex(Recurrence.TYPE));
							if(((newstartTimeLong>=dayTime[0] || newendTimeLong>=dayTime[0]) && 
									(newstartTimeLong<dayTime[1]) && recurrenceType<0) || recurrenceType>=0){
							if (eventEndTime.equalsIgnoreCase("00:00"))
								eventEndTime = "23:59";

							// CalendarLog.d(CalendarConstants.Tag,
							// "end_time---" + ""
							// + eventEndTime);

							allDay = cursor.getInt(cursor
									.getColumnIndex(Event.ALL_DAY));

							if (dayValue.getAsString("start_time")
									.equalsIgnoreCase("-1")) {
								dayValue.put("start_time", "");
								// //CalendarLog.d(CalendarConstants.Tag,"ST if",""+dayValue.get("start_time"));
							}
							CalendarLog.d(CalendarConstants.Tag,
									"eventTitle no exce " + "" + eventTitle);
							CalendarLog.d(CalendarConstants.Tag,
									"eventLocation no excep " + "" + eventLocation);

							if (dayValue.getAsString("end_time")
									.equalsIgnoreCase("-1"))
								dayValue.put("end_time", "");
							dayValue.put("event", dayValue.get("event")
									+ ";" + eventTitle);
							dayValue.put("_id", dayValue.get("_id") + ";"
									+ eventID);
							dayValue.put("start_time",
									dayValue.get("start_time") + ";"
											+ eventStartTime);
							dayValue.put("end_time",
									dayValue.get("end_time") + ";"
											+ eventEndTime);
							dayValue.put("all_day", allDay);
							dayValue.put(Event.EVENT_LOCATION,
									dayValue.get(Event.EVENT_LOCATION)
											+ ";" + eventLocation);

							dayValue.put("bg_color", Email
									.getAppContext().getResources()
									.getColor(R.color.event_bgcolor));

							dayValue.put(
									Recurrence.TYPE,
									cursor.getInt(cursor
											.getColumnIndex(Recurrence.TYPE)));
							dayValue.put(
									Recurrence.OCCURENCES,
									cursor.getInt(cursor
											.getColumnIndex(Recurrence.OCCURENCES)));
							dayValue.put(
									Recurrence.INTERVAL,
									cursor.getInt(cursor
											.getColumnIndex(Recurrence.INTERVAL)));
							dayValue.put(
									Recurrence.DOW,
									cursor.getInt(cursor
											.getColumnIndex(Recurrence.DOW)));
							dayValue.put(
									Recurrence.DOW_STRING,
									cursor.getString(cursor
											.getColumnIndex(Recurrence.DOW_STRING)));
							dayValue.put(
									Recurrence.DOM,
									cursor.getInt(cursor
											.getColumnIndex(Recurrence.DOM)));
							dayValue.put(
									Recurrence.WOM,
									cursor.getInt(cursor
											.getColumnIndex(Recurrence.WOM)));
							dayValue.put(
									Recurrence.MOY,
									cursor.getInt(cursor
											.getColumnIndex(Recurrence.MOY)));
							dayValue.put(
									Recurrence.UNTIL,
									cursor.getLong(cursor
											.getColumnIndex(Recurrence.UNTIL)));
							dayValue.put(
									CalendarConstants.RECURRENCE_CURRENTDATE,
									date);
							CalendarLog
									.d(CalendarConstants.Tag,
											"Recurrence.UNTIL"
													+ cursor.getLong(cursor
															.getColumnIndex(Recurrence.UNTIL)));
							CalendarLog.d(CalendarConstants.Tag, "event"
									+ "" + dayValue.get("event"));
							CalendarLog.d(
									CalendarConstants.Tag,
									"start_time" + ""
											+ dayValue.get("start_time"));
							CalendarLog.d(CalendarConstants.Tag, "end_time"
									+ "" + dayValue.get("end_time"));
							}
						}
					}
				}
			}
			cursor.moveToNext();
		}
		cursor.close();
		}
		for (int j = 0; j < dayValues.size(); j++) {
			ContentValues cv = dayValues.get(j);
			ContentValues personalEvent = personalCalendarDayList.get(j);
			String cvStartTime = cv.getAsString("start_time");
			String personalStartTime = personalEvent
					.getAsString("start_time");

			if ((cvStartTime.equalsIgnoreCase("-1"))
					&& (displayPersonalEvent())) {
				// if (cvStartTime.equalsIgnoreCase("-1")){
				cv = personalEvent;
			} else {

				if ((!personalStartTime.equalsIgnoreCase("-1"))
						&& (displayPersonalEvent())) {
					// if (!personalStartTime.equalsIgnoreCase("-1")){
					cv.put("event",
							cv.get("event") + ""
									+ personalEvent.get("event"));
					cv.put("_id",
							cv.get("_id") + "" + personalEvent.get("_id"));
					cv.put("start_time", cv.get("start_time") + ""
							+ personalEvent.get("start_time"));
					cv.put("end_time", cv.get("end_time") + ""
							+ personalEvent.get("end_time"));
					cv.put("calendarType", cv.get("calendarType") + ""
							+ personalEvent.get("calendarType"));

				}
			}

			mergedValues.add(cv);
		}
		// //CalendarLog.d(CalendarConstants.Tag,"vaues for date is ",date+dayValues);
	} catch (Exception execption) {
		execption.printStackTrace();

	}
	return mergedValues;
	}


	/*
	 * To get the event details for given date for agenda view
	 */
	public static ArrayList<ContentValues> getDayListValuesAgenda(String date) {

		ArrayList<ContentValues> personalCalendarDayList;
		Cursor cursor = getAllEventsByDate(date);
		ArrayList<ContentValues> mergedValues = new ArrayList<ContentValues>();
		ArrayList<ContentValues> dayValues = new ArrayList<ContentValues>();
		personalCalendarDayList = CalendarDBHelperClassPreICS
				.getPersonalDayListValues(date);

		// int index = 0;
		CalendarLog.d(CalendarConstants.Tag, "Date " + date);
		if (cursor != null)
			cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			ContentValues dayValue = new ContentValues();
			dayValue.put(Event.TITLE,
					cursor.getString(cursor.getColumnIndex(Event.TITLE)));
			dayValue.put("_id", cursor.getInt(cursor.getColumnIndex("_id")));
			dayValue.put(Event.START_DATE, getDateString(date, 0, 0));
			dayValue.put(Event.END_DATE,
					cursor.getString(cursor.getColumnIndex(Event.END_DATE)));
			dayValue.put(Event.DTSTART,
					cursor.getLong(cursor.getColumnIndex(Event.DTSTART)));
			dayValue.put(Event.DTEND,
					cursor.getLong(cursor.getColumnIndex(Event.DTEND)));
			dayValue.put("all_day",
					cursor.getInt(cursor.getColumnIndex(Event.ALL_DAY)));
			dayValue.put(Event.EVENT_LOCATION, cursor.getString(cursor
					.getColumnIndex(Event.EVENT_LOCATION)));
			dayValue.put("bg_color", Email.getAppContext()
					.getResources().getColor(R.color.event_bgcolor));

			dayValue.put(Recurrence.TYPE,
					cursor.getInt(cursor.getColumnIndex(Recurrence.TYPE)));
			dayValue.put(Recurrence.OCCURENCES,
					cursor.getInt(cursor.getColumnIndex(Recurrence.OCCURENCES)));
			dayValue.put(Recurrence.INTERVAL,
					cursor.getInt(cursor.getColumnIndex(Recurrence.INTERVAL)));
			dayValue.put(Recurrence.DOW,
					cursor.getInt(cursor.getColumnIndex(Recurrence.DOW)));
			dayValue.put(Recurrence.DOW_STRING, cursor.getString(cursor
					.getColumnIndex(Recurrence.DOW_STRING)));
			dayValue.put(Recurrence.DOM,
					cursor.getInt(cursor.getColumnIndex(Recurrence.DOM)));
			dayValue.put(Recurrence.WOM,
					cursor.getInt(cursor.getColumnIndex(Recurrence.WOM)));
			dayValue.put(Recurrence.MOY,
					cursor.getInt(cursor.getColumnIndex(Recurrence.MOY)));
			dayValue.put(Recurrence.UNTIL,
					cursor.getLong(cursor.getColumnIndex(Recurrence.UNTIL)));
			dayValue.put(CalendarConstants.CALENDAR_TYPE_KEY,
					CalendarConstants.CALENDAR_TYPE_CORPORATE);
			dayValues.add(dayValue);
			// CalendarLog.d(CalendarConstants.Tag, "eventTitle "
			// + eventTitle);
			// CalendarLog.d(
			// CalendarConstants.Tag,
			// "TYPE "
			// + cursor.getInt(cursor
			// .getColumnIndex(Recurrence.TYPE)));
			// CalendarLog
			// .d(CalendarConstants.Tag,
			// "INTERVAL "
			// + cursor.getInt(cursor
			// .getColumnIndex(Recurrence.INTERVAL)));
			// CalendarLog.d(
			// CalendarConstants.Tag,
			// "UNTIL "
			// + cursor.getLong(cursor
			// .getColumnIndex(Recurrence.UNTIL)));
			// CalendarLog
			// .d(CalendarConstants.Tag,
			// "OCCURENCES "
			// + cursor.getInt(cursor
			// .getColumnIndex(Recurrence.OCCURENCES)));
			// CalendarLog
			// .d(CalendarConstants.Tag,
			// "DOW_STRING "
			// + cursor.getString(cursor
			// .getColumnIndex(Recurrence.DOW_STRING)));
			// dayValue.put("text_color", Color.WHITE);
			// } else {
			// for (int j = durationHours; j > 0
			// && k < dayValues.size(); j--) {
			// ContentValues dayValue1 = dayValues.get(k);
			// dayValue1.put("bg_color",
			// Email.getAppContext().getResources().getColor(R.color.event_bgcolor));
			// dayValue1.put("text_color", Color.WHITE);
			//
			// k++;
			// }
			// }

			// }

			cursor.moveToNext();
		}
		cursor.close();
		try {
			for (int j = 0; j < dayValues.size(); j++) {
				ContentValues cv = dayValues.get(j);
				ContentValues personalEvent = personalCalendarDayList.get(j);
				String cvStartTime = cv.getAsString(Event.DTSTART);
				String personalStartTime = personalEvent
						.getAsString("start_time");

				if ((cvStartTime.equalsIgnoreCase("-1"))
						&& (displayPersonalEvent())) {
					// if (cvStartTime.equalsIgnoreCase("-1")){
					cv = personalEvent;
				} else {

					if ((!personalStartTime.equalsIgnoreCase("-1"))
							&& (displayPersonalEvent())) {
						// if (!personalStartTime.equalsIgnoreCase("-1")){
						cv.put(Event.TITLE, cv.get("event") + ""
								+ personalEvent.get("event"));
						cv.put("_id",
								cv.get("_id") + "" + personalEvent.get("_id"));
						cv.put(Event.DTSTART, cv.get(Event.DTSTART) + ""
								+ personalEvent.get("start_time"));
						cv.put(Event.DTEND, cv.get("Event.DTEND") + ""
								+ personalEvent.get("end_time"));
						cv.put("calendarType", cv.get("calendarType") + ""
								+ personalEvent.get("calendarType"));

					}
				}

				mergedValues.add(cv);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		CalendarLog.d(CalendarConstants.Tag, "mergedValues.size() "
				+ mergedValues.size());
		return mergedValues;

	}

	/************************ AGENDA VIEW ********************************************************************/

	public static String getDateFromLong(long longDate) {
		Calendar cal = Email.getNewCalendar();
		cal.setTime(new Date(longDate));
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.US);

		String stringDate = getDateString(sdf.format(cal.getTime()), 0, 0);
		// CalendarLog.d(CalendarConstants.Tag,"date",stringDate);
		return stringDate;

	}
	
	public static String getDateFromLong(long longDate,String tz) {
		Calendar cal = Email.getNewCalendar();
		cal.setTimeInMillis(longDate);
		cal.setTimeZone(getTimeZoneFromString(tz));
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.US);
		sdf.setTimeZone(getTimeZoneFromString(tz));
//		CalendarLog.d(CalendarConstants.Tag,"aish getDateFromLong"+cal.get(Calendar.DATE)+" tz"+getTimeZoneFromString(tz).getID());
		String stringDate = getDateString(sdf.format(cal.getTime()), 0, 0);
		 
		return stringDate;

	}

	public static String getTimeFromLong(long longDate,String tz) {
		
		Calendar cal=Email.getNewCalendar();
		cal.setTimeInMillis(longDate);
		cal.setTimeZone(getTimeZoneFromString(tz));
		CalendarLog.d(CalendarConstants.Tag,"aish teh timezoe obt "+getTimeZoneFromString(tz).getID());
		String min = "";
		String hrs = "";	
		if (String.valueOf(cal.get(Calendar.MINUTE)).length() == 1)
			min = "0" + String.valueOf(cal.get(Calendar.MINUTE));
		else
		min = String.valueOf(cal.get(Calendar.MINUTE));
		if (String.valueOf(cal.get(Calendar.HOUR_OF_DAY)).length() == 1)
		hrs = "0" + String.valueOf(cal.get(Calendar.HOUR_OF_DAY));
		else
		hrs = String.valueOf(cal.get(Calendar.HOUR_OF_DAY));	
		
		return hrs + ":" + min;

	}

	public static String getDayFromLong(Long long1) {
		Calendar cal = Email.getNewCalendar();
		cal.setTime(new Date(long1));
		// CalendarLog.d(CalendarConstants.Tag,"date obtained is",cal.getTime().toString()+" "+cal.get(Calendar.DAY_OF_WEEK));
		switch (cal.get(Calendar.DAY_OF_WEEK)) {
		case Calendar.SUNDAY:
			return "SUNDAY";
		case Calendar.MONDAY:
			return "MONDAY";
		case Calendar.TUESDAY:
			return "TUESDAY";
		case Calendar.WEDNESDAY:
			return "WEDNESDAY";
		case Calendar.THURSDAY:
			return "THURSDAY";
		case Calendar.FRIDAY:
			return "FRIDAY";
		case Calendar.SATURDAY:
			return "SATURDAY";
		}
		return "";
	}

	/**
	 * To get all yearly recurrence events
	 * 
	 * @param curTime
	 *            - Calendar that holds the Date to check
	 * @param weekEvents
	 *            - All yearly recurrence of request type = 5 and 6 without
	 *            other conditions
	 * @param dayValues
	 *            - Empty Default values for the particular day
	 * @return
	 */
	private static ArrayList<ContentValues> getYearlyEvents(Calendar curTime,
			ArrayList<ContentValues> yearEvents,
			ArrayList<ContentValues> dayValues) {
		try {
			
//			CalendarLog.d(CalendarConstants.Tag,"get yrly events "+yearEvents.size());
			DecimalFormat mFormat = new DecimalFormat("00");
			for (ContentValues contentValues : yearEvents) {
//				CalendarLog.d(CalendarConstants.Tag, "yrly event title is "+contentValues.getAsString(Event.TITLE));
				String eventDate = contentValues.getAsString(Event.START_DATE);
				int rectype = contentValues.getAsInteger(Recurrence.TYPE);
				int dom = contentValues.getAsInteger(Recurrence.DOM);
				int moy = contentValues.getAsInteger(Recurrence.MOY);

				// int interval = contentValues.getAsInteger(Recurrence.INTERVAL);
				// int occurences =
				// contentValues.getAsInteger(Recurrence.OCCURENCES);
				long untill = contentValues.getAsLong(Recurrence.UNTIL);
				String eventDowString = contentValues
						.getAsString(Recurrence.DOW_STRING);
				// Log.e("getMontlyEvents", ""+eventDate);
				String[] eventDays = eventDate.split("-");
				Calendar currentCal = Email.getNewCalendar();
				currentCal.setFirstDayOfWeek(Calendar.SUNDAY);
				currentCal
						.set(Calendar.DAY_OF_MONTH, Integer.valueOf(eventDays[2]));
				currentCal.set(Calendar.MONTH, Integer.valueOf(eventDays[1]) - 1);
				currentCal.set(Calendar.YEAR, Integer.valueOf(eventDays[0]));
				boolean isAfter = true;
				if (curTime.getTime().before(currentCal.getTime())) {
					isAfter = false;
				} else
					isAfter = true;
				int weekday = currentCal.get(Calendar.DAY_OF_MONTH) / 7;
				weekday++;
				// int daydifference = ((int) ((curTime.getTimeInMillis()-
				// currentCal.getTimeInMillis())/MILLISECS_PER_DAY)) ;
	
				try {
					if ((rectype == 5 && isAfter
							&& dom == curTime.get(Calendar.DAY_OF_MONTH)
							&& moy == (curTime.get(Calendar.MONTH)+1) && (untill == -1 || untill >= curTime
							.getTimeInMillis()))
							) {
//						CalendarLog.d(CalendarConstants.Tag,"content rectype"+rectype);
//						CalendarLog.d(CalendarConstants.Tag,"content isAfter "+isAfter);
//						CalendarLog.d(CalendarConstants.Tag,"content DAY_OF_MONTH "+curTime.get(Calendar.DAY_OF_MONTH));
//						CalendarLog.d(CalendarConstants.Tag,"content Calendar.MONTH "+(curTime.get(Calendar.MONTH)+1));
//						CalendarLog.d(CalendarConstants.Tag,"content getTimeInMillis "+curTime.getTimeInMillis());
//						CalendarLog.d(CalendarConstants.Tag,"content fvalue for yr "+contentValues.toString());

						boolean isException = false;
						boolean isExceptionDelete = false;
						Exceptions exception = new Exceptions();
						String currDate = curTime.get(Calendar.YEAR)
								+ "-"
								+ mFormat.format(curTime.get(Calendar.MONTH) + 1)
								+ "-"
								+ mFormat
										.format(curTime.get(Calendar.DAY_OF_MONTH));
						String eventID = contentValues.getAsString("_id");
						if (contentValues.getAsString(Event.HAS_EXCEPTIONS)
								.equalsIgnoreCase("1")) {
							ArrayList<Exceptions> exceptionList = getExceptionsForEvent(Integer
									.parseInt(eventID));
							CalendarLog.d(CalendarConstants.Tag, "size:"
									+ exceptionList.size());
							for (Exceptions excepti : exceptionList) {
								if (excepti.isExceptionDeleted == 1) {
									CalendarLog.d(CalendarConstants.Tag,
											"isException if:" + isException);

									isExceptionDelete = true;
								} else {
									CalendarLog.d(CalendarConstants.Tag,
											"isException else:" + isException);
									CalendarLog.d(CalendarConstants.Tag,
											"isException else date:" + currDate);
									// getExceptionContentValues(excepti);
									if (excepti.startDate != null)
										if (excepti.startDate
												.equalsIgnoreCase(currDate)) {
											isException = true;
											exception = excepti;
										}
								}
							}
						}

						if (isExceptionDelete) {

						} else {
							ContentValues dayValue = new ContentValues();
							if (isException) {

								dayValue.put("_id", eventID);
								dayValue.put(Event.TITLE, exception.title);
								dayValue.put("bg_color", Color.WHITE);
								dayValue.put("text_color", Color.BLACK);
								dayValue.put(Event.DTSTART, exception.dtstart);
								dayValue.put(Event.START_DATE, currDate);
								dayValue.put("HeaderDate", currDate);
								dayValue.put(Event.END_DATE, exception.endDate);
								dayValue.put(Event.EVENT_TIMEZONE, exception.eventTimezone);
								dayValue.put(Event.DTEND, exception.dtend);
								dayValue.put("all_day", exception.allDay);
								dayValue.put(CalendarConstants.CALENDAR_TYPE_KEY,
										CalendarConstants.CALENDAR_TYPE_CORPORATE);

								Recurrence recrr = exception.recurrence;
								dayValue.put(Recurrence.TYPE,
										exception.responseType);
								if (recrr != null) {
									dayValue.put(Recurrence.OCCURENCES,
											recrr.occurences);
									dayValue.put(Recurrence.INTERVAL,
											recrr.interval);
									dayValue.put(Recurrence.DOW, recrr.dow);
									dayValue.put(Recurrence.DOW_STRING,
											recrr.dowString);
									dayValue.put(Recurrence.DOM, recrr.dom);
									dayValue.put(Recurrence.WOM, recrr.wom);
									dayValue.put(Recurrence.MOY, recrr.moy);
									dayValue.put(Recurrence.UNTIL, recrr.until);
								} else {
									dayValue.put(Recurrence.OCCURENCES, "-1");
									dayValue.put(Recurrence.INTERVAL, "-1");
									dayValue.put(Recurrence.DOW, "-1");
									dayValue.put(Recurrence.DOW_STRING, "");
									dayValue.put(Recurrence.DOM, "-1");
									dayValue.put(Recurrence.WOM, "-1");
									dayValue.put(Recurrence.MOY, "-1");
									dayValue.put(Recurrence.UNTIL, "-1");
								}
								dayValue.put(
										CalendarConstants.RECURRENCE_CURRENTDATE,
										currDate);
								// CalendarLog.d(CalendarConstants.Tag,"RECURRENCE_CURRENTDATE"+
								// date);
								// CalendarLog.d(CalendarConstants.Tag,"event"+""+
								// dayValue.get("event"));
								// CalendarLog.d(CalendarConstants.Tag,"start_time"+""+
								// dayValue.get("start_time"));
								// CalendarLog.d(CalendarConstants.Tag,"end_time"+""+
								// dayValue.get("end_time"));

							}else {

								dayValue.put("_id",
										contentValues.getAsString("_id"));
								dayValue.put(Event.TITLE,
										contentValues.getAsString(Event.TITLE));
								dayValue.put("bg_color", Color.WHITE);
								dayValue.put("text_color", Color.BLACK);
								dayValue.put(Event.DTSTART,
										contentValues.getAsString(Event.DTSTART));
								dayValue.put(
										Event.START_DATE,
										curTime.get(Calendar.YEAR)
												+ "-"
												+ mFormat.format(curTime
														.get(Calendar.MONTH) + 1)
												+ "-"
												+ mFormat.format(curTime
														.get(Calendar.DAY_OF_MONTH)));
								dayValue.put("HeaderDate", currDate);
								// Log.e("Date :","Agenta "+curTime.get(Calendar.YEAR)
								// + "-"
								// + mFormat.format(curTime.get(Calendar.MONTH)) +
								// "-"
								// +
								// mFormat.format(curTime.get(Calendar.DAY_OF_MONTH)));
								dayValue.put(Event.END_DATE,
										contentValues.getAsString(Event.END_DATE));
								dayValue.put(Event.EVENT_TIMEZONE, contentValues.getAsString(Event.EVENT_TIMEZONE));
								dayValue.put(Event.DTEND,
										contentValues.getAsString(Event.DTEND));
								dayValue.put("all_day",
										contentValues.getAsString(Event.ALL_DAY));
								dayValue.put(CalendarConstants.CALENDAR_TYPE_KEY,
										CalendarConstants.CALENDAR_TYPE_CORPORATE);

								dayValue.put(Recurrence.TYPE,
										contentValues.getAsInteger(Recurrence.TYPE));
								dayValue.put(Recurrence.OCCURENCES, contentValues
										.getAsInteger(Recurrence.OCCURENCES));
								dayValue.put(Recurrence.INTERVAL, contentValues
										.getAsInteger(Recurrence.INTERVAL));
								dayValue.put(Recurrence.DOW,
										contentValues.getAsInteger(Recurrence.DOW));
								dayValue.put(Recurrence.DOW_STRING, contentValues
										.getAsString(Recurrence.DOW_STRING));
								dayValue.put(Recurrence.DOM,
										contentValues.getAsInteger(Recurrence.DOM));
								dayValue.put(Recurrence.WOM,
										contentValues.getAsInteger(Recurrence.WOM));
								dayValue.put(Recurrence.MOY,
										contentValues.getAsInteger(Recurrence.MOY));
								dayValue.put(Recurrence.UNTIL,
										contentValues.getAsLong(Recurrence.UNTIL));

							}
							dayValues.add(dayValue);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return dayValues;
	}

	/**
	 * To get all monthly recurrence events
	 * 
	 * @param curTime
	 *            - Calendar that holds the Date to check
	 * @param weekEvents
	 *            - All monthly recurrence of request type =2 and 3 without
	 *            other conditions
	 * @param dayValues
	 *            - Empty Default values for the particular day
	 * @return
	 */
	private static ArrayList<ContentValues> getMontlyEvents(Calendar curTime,
			ArrayList<ContentValues> monthEvents,
			ArrayList<ContentValues> dayValues) {
		try {
			DecimalFormat mFormat = new DecimalFormat("00");
			for (ContentValues contentValues : monthEvents) {
				String eventDate = contentValues.getAsString(Event.START_DATE);
				int rectype = contentValues.getAsInteger(Recurrence.TYPE);
				// int interval = contentValues.getAsInteger(Recurrence.INTERVAL);
				// int occurences =
				// contentValues.getAsInteger(Recurrence.OCCURENCES);
				int dom = contentValues.getAsInteger(Recurrence.DOM);
				int wom = contentValues.getAsInteger(Recurrence.WOM);
				long untill = contentValues.getAsLong(Recurrence.UNTIL);
				String eventDowString = contentValues
						.getAsString(Recurrence.DOW_STRING);
				// Log.e("getMontlyEvents", ""+eventDate);
				String[] eventDays = eventDate.split("-");
				Calendar currentCal = Email.getNewCalendar();
				currentCal.setFirstDayOfWeek(Calendar.SUNDAY);
				currentCal
						.set(Calendar.DAY_OF_MONTH, Integer.valueOf(eventDays[2]));
				currentCal.set(Calendar.MONTH, Integer.valueOf(eventDays[1]) - 1);
				currentCal.set(Calendar.YEAR, Integer.valueOf(eventDays[0]));
				int weekday = currentCal.get(Calendar.DAY_OF_MONTH) / 7;
				weekday++;
				boolean isAfter = true;
				if (curTime.getTime().before(currentCal.getTime())) {
					isAfter = false;
				} else
					isAfter = true;
				// CalendarLog.d(CalendarConstants.Tag,"Recc-Month-rectype:"+rectype);
				// CalendarLog.d(CalendarConstants.Tag,"Recc-Month-dom:"+dom);
				// CalendarLog.d(CalendarConstants.Tag,"Recc-Month-Event.TITLE:"+contentValues.getAsString(Event.TITLE));
				// int daydifference = ((int) ((curTime.getTimeInMillis()-
				// currentCal.getTimeInMillis())/MILLISECS_PER_DAY)) ;
				try {
					if ((rectype == 2 && isAfter
							&& dom == curTime.get(Calendar.DAY_OF_MONTH) && (untill == -1 || untill >= curTime
							.getTimeInMillis()))
							|| (rectype == 3
									&& isAfter
									&& (untill == -1 || untill >= curTime
											.getTimeInMillis()) && (wom == weekday) && (eventDowString
										.contains(CalendarWeekPagerAdapter
												.getWeekDayOfDate(curTime, 2))))) {
						CalendarLog.d(CalendarConstants.Tag,"content fvalue for month"+contentValues.toString());
						boolean isException = false;
						boolean isExceptionDelete = false;
						Exceptions exception = new Exceptions();
						String currDate = curTime.get(Calendar.YEAR)
								+ "-"
								+ mFormat.format(curTime.get(Calendar.MONTH) + 1)
								+ "-"
								+ mFormat
										.format(curTime.get(Calendar.DAY_OF_MONTH));
						String eventID = contentValues.getAsString("_id");
						if (contentValues.getAsString(Event.HAS_EXCEPTIONS)
								.equalsIgnoreCase("1")) {
							ArrayList<Exceptions> exceptionList = getExceptionsForEvent(Integer
									.parseInt(eventID));
							CalendarLog.d(CalendarConstants.Tag, "size:"
									+ exceptionList.size());
							for (Exceptions excepti : exceptionList) {
								if (excepti.isExceptionDeleted == 1) {
									CalendarLog.d(CalendarConstants.Tag,
											"isException if:" + isException);

									isExceptionDelete = true;
								} else {
									CalendarLog.d(CalendarConstants.Tag,
											"isException else:" + isException);
									CalendarLog.d(CalendarConstants.Tag,
											"isException else date:" + currDate);
									// getExceptionContentValues(excepti);
									if (excepti.startDate != null)
										if (excepti.startDate
												.equalsIgnoreCase(currDate)) {
											isException = true;
											exception = excepti;
										}
								}
							}
						}

						if (isExceptionDelete) {

						} else {
							ContentValues dayValue = new ContentValues();
							if (isException) {

								dayValue.put("_id", eventID);
								dayValue.put(Event.TITLE, exception.title);
								dayValue.put("bg_color", Color.WHITE);
								dayValue.put("text_color", Color.BLACK);
								dayValue.put(Event.DTSTART, exception.dtstart);
								dayValue.put(Event.START_DATE, currDate);
								dayValue.put("HeaderDate", currDate);
								dayValue.put(Event.END_DATE, exception.endDate);
								dayValue.put(Event.EVENT_TIMEZONE, exception.eventTimezone);
								dayValue.put(Event.DTEND, exception.dtend);
								dayValue.put("all_day", exception.allDay);
								dayValue.put(CalendarConstants.CALENDAR_TYPE_KEY,
										CalendarConstants.CALENDAR_TYPE_CORPORATE);

								Recurrence recrr = exception.recurrence;
								dayValue.put(Recurrence.TYPE,
										exception.responseType);
								if (recrr != null) {
									dayValue.put(Recurrence.OCCURENCES,
											recrr.occurences);
									dayValue.put(Recurrence.INTERVAL,
											recrr.interval);
									dayValue.put(Recurrence.DOW, recrr.dow);
									dayValue.put(Recurrence.DOW_STRING,
											recrr.dowString);
									dayValue.put(Recurrence.DOM, recrr.dom);
									dayValue.put(Recurrence.WOM, recrr.wom);
									dayValue.put(Recurrence.MOY, recrr.moy);
									dayValue.put(Recurrence.UNTIL, recrr.until);
								} else {
									dayValue.put(Recurrence.OCCURENCES, "-1");
									dayValue.put(Recurrence.INTERVAL, "-1");
									dayValue.put(Recurrence.DOW, "-1");
									dayValue.put(Recurrence.DOW_STRING, "");
									dayValue.put(Recurrence.DOM, "-1");
									dayValue.put(Recurrence.WOM, "-1");
									dayValue.put(Recurrence.MOY, "-1");
									dayValue.put(Recurrence.UNTIL, "-1");
								}
								dayValue.put(
										CalendarConstants.RECURRENCE_CURRENTDATE,
										currDate);
								// CalendarLog.d(CalendarConstants.Tag,"RECURRENCE_CURRENTDATE"+
								// date);
								// CalendarLog.d(CalendarConstants.Tag,"event"+""+
								// dayValue.get("event"));
								// CalendarLog.d(CalendarConstants.Tag,"start_time"+""+
								// dayValue.get("start_time"));
								// CalendarLog.d(CalendarConstants.Tag,"end_time"+""+
								// dayValue.get("end_time"));

							}else {

								dayValue.put("_id",
										contentValues.getAsString("_id"));
								dayValue.put(Event.TITLE,
										contentValues.getAsString(Event.TITLE));
								dayValue.put("bg_color", Color.WHITE);
								dayValue.put("text_color", Color.BLACK);
								dayValue.put(Event.DTSTART,
										contentValues.getAsString(Event.DTSTART));
								dayValue.put(
										Event.START_DATE,
										curTime.get(Calendar.YEAR)
												+ "-"
												+ mFormat.format(curTime
														.get(Calendar.MONTH) + 1)
												+ "-"
												+ mFormat.format(curTime
														.get(Calendar.DAY_OF_MONTH)));
								dayValue.put("HeaderDate", currDate);
								// Log.e("Date :","Agenta "+curTime.get(Calendar.YEAR)
								// + "-"
								// + mFormat.format(curTime.get(Calendar.MONTH)) +
								// "-"
								// +
								// mFormat.format(curTime.get(Calendar.DAY_OF_MONTH)));
								dayValue.put(Event.END_DATE,
										contentValues.getAsString(Event.END_DATE));
								dayValue.put(Event.DTEND,
										contentValues.getAsLong(Event.DTEND));
								dayValue.put(Event.EVENT_TIMEZONE,
										contentValues.getAsString(Event.EVENT_TIMEZONE));
								dayValue.put("all_day",
										contentValues.getAsString(Event.ALL_DAY));
								dayValue.put(CalendarConstants.CALENDAR_TYPE_KEY,
										CalendarConstants.CALENDAR_TYPE_CORPORATE);

								dayValue.put(Recurrence.TYPE,
										contentValues.getAsInteger(Recurrence.TYPE));
								dayValue.put(Recurrence.OCCURENCES, contentValues
										.getAsInteger(Recurrence.OCCURENCES));
								dayValue.put(Recurrence.INTERVAL, contentValues
										.getAsInteger(Recurrence.INTERVAL));
								dayValue.put(Recurrence.DOW,
										contentValues.getAsInteger(Recurrence.DOW));
								dayValue.put(Recurrence.DOW_STRING, contentValues
										.getAsString(Recurrence.DOW_STRING));
								dayValue.put(Recurrence.DOM,
										contentValues.getAsInteger(Recurrence.DOM));
								dayValue.put(Recurrence.WOM,
										contentValues.getAsInteger(Recurrence.WOM));
								dayValue.put(Recurrence.MOY,
										contentValues.getAsInteger(Recurrence.MOY));
								dayValue.put(Recurrence.UNTIL,
										contentValues.getAsLong(Recurrence.UNTIL));

							}
							dayValues.add(dayValue);
						}
					}
				} catch (Exception e) {

					e.printStackTrace();
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dayValues;
	}

	/**
	 * To get all weekly recurrence events
	 * 
	 * @param curTime
	 *            - Calendar that holds the Date to check
	 * @param weekEvents
	 *            - All weekday of request type =1 without other conditions
	 * @param dayValues
	 *            - Empty Default values for the particular day
	 * @return
	 */
	private static ArrayList<ContentValues> getWeeklyEvents(Calendar curTime,
			ArrayList<ContentValues> weekEvents,
			ArrayList<ContentValues> dayValues) {

		try {
			DecimalFormat mFormat = new DecimalFormat("00");
			curTime.setFirstDayOfWeek(Calendar.SUNDAY);
			int eventdayofweek = curTime.get(Calendar.DAY_OF_WEEK);

			eventdayofweek = eventdayofweek - 1;

			for (ContentValues contentValues : weekEvents) {
				String eventDate = contentValues.getAsString(Event.START_DATE);
				int interval = contentValues.getAsInteger(Recurrence.INTERVAL);
				int occurences = contentValues.getAsInteger(Recurrence.OCCURENCES);
				long untill = contentValues.getAsLong(Recurrence.UNTIL);
				String eventDowString = contentValues
						.getAsString(Recurrence.DOW_STRING);

				// Log.e("getMontlyEvents", ""+eventDate);
				String[] eventDays = eventDate.split("-");
				Calendar currentCal = Email.getNewCalendar();
				currentCal.setFirstDayOfWeek(Calendar.SUNDAY);
				currentCal
						.set(Calendar.DAY_OF_MONTH, Integer.valueOf(eventDays[2]));
				currentCal.set(Calendar.MONTH, Integer.valueOf(eventDays[1]) - 1);
				currentCal.set(Calendar.YEAR, Integer.valueOf(eventDays[0]));
				int currentdayofweek = currentCal.get(Calendar.DAY_OF_WEEK);
				boolean isAfter = true;
				if (curTime.getTime().before(currentCal.getTime())) {
					isAfter = false;
				} else
					isAfter = true;
				currentdayofweek = currentdayofweek - 1;

				int daydifference = ((int) ((curTime.getTimeInMillis() - currentCal
						.getTimeInMillis()) / MILLISECS_PER_DAY));
				try {
					if (isAfter
							&& ((daydifference % interval) == 0 || interval == -1 || interval == 1)
							&& (occurences == -1 || daydifference < (interval * occurences))
							&& (untill == -1 || untill >= curTime.getTimeInMillis())
							&& (eventDowString.contains(CalendarWeekPagerAdapter
									.getWeekDayOfDate(curTime, 2)))) {
						CalendarLog.d(CalendarConstants.Tag,"content getWeekDayOfDate "+CalendarWeekPagerAdapter
								.getWeekDayOfDate(curTime, 2));
						CalendarLog.d(CalendarConstants.Tag,"content eventDowString "+eventDowString);
						CalendarLog.d(CalendarConstants.Tag,"content curTime "+curTime.getTime());
						CalendarLog.d(CalendarConstants.Tag,"content fvalue for week"+contentValues.toString());
						boolean isException = false;
						boolean isExceptionDelete = false;
						Exceptions exception = new Exceptions();
						String currDate = curTime.get(Calendar.YEAR)
								+ "-"
								+ mFormat.format(curTime.get(Calendar.MONTH) + 1)
								+ "-"
								+ mFormat
										.format(curTime.get(Calendar.DAY_OF_MONTH));
						String eventID = contentValues.getAsString("_id");
						if (contentValues.getAsString(Event.HAS_EXCEPTIONS)
								.equalsIgnoreCase("1")) {
							ArrayList<Exceptions> exceptionList = getExceptionsForEvent(Integer
									.parseInt(eventID));
							CalendarLog.d(CalendarConstants.Tag, "size:"
									+ exceptionList.size());
							for (Exceptions excepti : exceptionList) {
								if (excepti.isExceptionDeleted == 1) {
									CalendarLog.d(CalendarConstants.Tag,
											"isException if:" + isException);

									isExceptionDelete = true;
								} else {
									CalendarLog.d(CalendarConstants.Tag,
											"isException else:" + isException);
									CalendarLog.d(CalendarConstants.Tag,
											"isException else date:" + currDate);
									// getExceptionContentValues(excepti);
									if (excepti.startDate != null)
										if (excepti.startDate
												.equalsIgnoreCase(currDate)) {
											isException = true;
											exception = excepti;
										}
								}
							}
						}

						if (isExceptionDelete) {

						} else {
							ContentValues dayValue = new ContentValues();
							if (isException) {

								dayValue.put("_id", eventID);
								dayValue.put(Event.TITLE, exception.title);
								dayValue.put("bg_color", Color.WHITE);
								dayValue.put("text_color", Color.BLACK);
								dayValue.put(Event.DTSTART, exception.dtstart);
								dayValue.put(Event.START_DATE, currDate);
								dayValue.put("HeaderDate", currDate);
								dayValue.put(Event.END_DATE, exception.endDate);
								dayValue.put(Event.EVENT_TIMEZONE, exception.eventTimezone);
								dayValue.put(Event.DTEND, exception.dtend);
								dayValue.put("all_day", exception.allDay);
								dayValue.put(CalendarConstants.CALENDAR_TYPE_KEY,
										CalendarConstants.CALENDAR_TYPE_CORPORATE);

								Recurrence recrr = exception.recurrence;
								dayValue.put(Recurrence.TYPE,
										exception.responseType);
								if (recrr != null) {
									dayValue.put(Recurrence.OCCURENCES,
											recrr.occurences);
									dayValue.put(Recurrence.INTERVAL,
											recrr.interval);
									dayValue.put(Recurrence.DOW, recrr.dow);
									dayValue.put(Recurrence.DOW_STRING,
											recrr.dowString);
									dayValue.put(Recurrence.DOM, recrr.dom);
									dayValue.put(Recurrence.WOM, recrr.wom);
									dayValue.put(Recurrence.MOY, recrr.moy);
									dayValue.put(Recurrence.UNTIL, recrr.until);
								} else {
									dayValue.put(Recurrence.OCCURENCES, "-1");
									dayValue.put(Recurrence.INTERVAL, "-1");
									dayValue.put(Recurrence.DOW, "-1");
									dayValue.put(Recurrence.DOW_STRING, "");
									dayValue.put(Recurrence.DOM, "-1");
									dayValue.put(Recurrence.WOM, "-1");
									dayValue.put(Recurrence.MOY, "-1");
									dayValue.put(Recurrence.UNTIL, "-1");
								}
								dayValue.put(
										CalendarConstants.RECURRENCE_CURRENTDATE,
										currDate);
								// CalendarLog.d(CalendarConstants.Tag,"RECURRENCE_CURRENTDATE"+
								// date);
								// CalendarLog.d(CalendarConstants.Tag,"event"+""+
								// dayValue.get("event"));
								// CalendarLog.d(CalendarConstants.Tag,"start_time"+""+
								// dayValue.get("start_time"));
								// CalendarLog.d(CalendarConstants.Tag,"end_time"+""+
								// dayValue.get("end_time"));

							} else {

								dayValue.put("_id",
										contentValues.getAsString("_id"));
								dayValue.put(Event.TITLE,
										contentValues.getAsString(Event.TITLE));
								dayValue.put("bg_color", Color.WHITE);
								dayValue.put("text_color", Color.BLACK);
								dayValue.put(Event.DTSTART,
										contentValues.getAsString(Event.DTSTART));
								dayValue.put(
										Event.START_DATE,
										curTime.get(Calendar.YEAR)
												+ "-"
												+ mFormat.format(curTime
														.get(Calendar.MONTH) + 1)
												+ "-"
												+ mFormat.format(curTime
														.get(Calendar.DAY_OF_MONTH)));
								dayValue.put("HeaderDate", currDate);
								// Log.e("Date :","Agenta "+curTime.get(Calendar.YEAR)
								// + "-"
								// + mFormat.format(curTime.get(Calendar.MONTH)) +
								// "-"
								// +
								// mFormat.format(curTime.get(Calendar.DAY_OF_MONTH)));
								dayValue.put(Event.END_DATE,
										contentValues.getAsString(Event.END_DATE));
								dayValue.put(Event.DTEND,
										contentValues.getAsString(Event.DTEND));
								dayValue.put(Event.EVENT_TIMEZONE, contentValues.getAsString(Event.EVENT_TIMEZONE));
								dayValue.put("all_day",
										contentValues.getAsString(Event.ALL_DAY));
								dayValue.put(CalendarConstants.CALENDAR_TYPE_KEY,
										CalendarConstants.CALENDAR_TYPE_CORPORATE);

								dayValue.put(Recurrence.TYPE,
										contentValues.getAsInteger(Recurrence.TYPE));
								dayValue.put(Recurrence.OCCURENCES, contentValues
										.getAsInteger(Recurrence.OCCURENCES));
								dayValue.put(Recurrence.INTERVAL, contentValues
										.getAsInteger(Recurrence.INTERVAL));
								dayValue.put(Recurrence.DOW,
										contentValues.getAsInteger(Recurrence.DOW));
								dayValue.put(Recurrence.DOW_STRING, contentValues
										.getAsString(Recurrence.DOW_STRING));
								dayValue.put(Recurrence.DOM,
										contentValues.getAsInteger(Recurrence.DOM));
								dayValue.put(Recurrence.WOM,
										contentValues.getAsInteger(Recurrence.WOM));
								dayValue.put(Recurrence.MOY,
										contentValues.getAsInteger(Recurrence.MOY));
								dayValue.put(Recurrence.UNTIL,
										contentValues.getAsLong(Recurrence.UNTIL));

							}
							dayValues.add(dayValue);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dayValues;
	}

	private static ContentValues DefaultContantValues(Cursor cursor) {
		ContentValues dateValue = new ContentValues();
		
		try {
			dateValue.put("_id", cursor.getInt(cursor.getColumnIndex("_id")));
			// CalendarLog.i(CalendarConstants.Tag, "Event.TITLE "
			// + cursor.getString(cursor.getColumnIndex(Event.TITLE)));
			dateValue.put(Event.TITLE,
					cursor.getString(cursor.getColumnIndex(Event.TITLE)));
			// dateValue.put(Event.EVENT_ID,
			// cursor.getString(cursor.getColumnIndex(Event.EVENT_ID)));
			dateValue.put(Event.DTSTART,
					cursor.getLong(cursor.getColumnIndex(Event.DTSTART)));
			dateValue.put(Event.EVENT_TIMEZONE, cursor.getString(cursor.getColumnIndex(Event.EVENT_TIMEZONE)));
			dateValue.put(Event.START_DATE,
					cursor.getString(cursor.getColumnIndex(Event.START_DATE)));
			dateValue.put(Event.END_DATE,
					cursor.getString(cursor.getColumnIndex(Event.END_DATE)));
			dateValue.put(Event.EVENT_LOCATION,
					cursor.getString(cursor.getColumnIndex(Event.EVENT_LOCATION)));
			dateValue.put(Event.DTEND,
					cursor.getLong(cursor.getColumnIndex(Event.DTEND)));
			dateValue.put("all_day",
					cursor.getLong(cursor.getColumnIndex(Event.ALL_DAY)));
			dateValue.put(CalendarConstants.CALENDAR_TYPE_KEY,
					CalendarConstants.CALENDAR_TYPE_CORPORATE);
			dateValue.put(Recurrence.TYPE,
					cursor.getInt(cursor.getColumnIndex(Recurrence.TYPE)));
			dateValue.put(Recurrence.OCCURENCES,
					cursor.getInt(cursor.getColumnIndex(Recurrence.OCCURENCES)));
			dateValue.put(Recurrence.INTERVAL,
					cursor.getInt(cursor.getColumnIndex(Recurrence.INTERVAL)));
			dateValue.put(Recurrence.DOW,
					cursor.getInt(cursor.getColumnIndex(Recurrence.DOW)));
			dateValue.put(Recurrence.DOW_STRING,
					cursor.getString(cursor.getColumnIndex(Recurrence.DOW_STRING)));
			dateValue.put(Recurrence.DOM,
					cursor.getInt(cursor.getColumnIndex(Recurrence.DOM)));
			dateValue.put(Recurrence.WOM,
					cursor.getInt(cursor.getColumnIndex(Recurrence.WOM)));
			dateValue.put(Recurrence.MOY,
					cursor.getInt(cursor.getColumnIndex(Recurrence.MOY)));
			dateValue.put(Recurrence.UNTIL,
					cursor.getLong(cursor.getColumnIndex(Recurrence.UNTIL)));
			dateValue.put(Event.HAS_EXCEPTIONS,
					cursor.getLong(cursor.getColumnIndex(Event.HAS_EXCEPTIONS)));
			// CalendarLog.i(CalendarConstants.Tag, "Recurrence.UNTIL "
			// + cursor.getLong(cursor.getColumnIndex(Recurrence.UNTIL)));
		} catch (Exception e) {
			e.printStackTrace();
		}
//		CalendarLog.d(CalendarConstants.Tag, "default content values "+dateValue.toString());
		return dateValue;
	}

	public static ArrayList<ArrayList<ContentValues>> getEventListAgenda(
			String fromDate, String toDate) {

		// return getEventListAgendaByDate(fromDate, toDate);
		CalendarLog
				.d(CalendarConstants.Tag, "Date :" + fromDate + " " + toDate);

		ArrayList<ArrayList<ContentValues>> personalAgendaList;
		ArrayList<ArrayList<ContentValues>> mergedAgendaList = new ArrayList<ArrayList<ContentValues>>();
		ArrayList<ArrayList<ContentValues>> listByDate = new ArrayList<ArrayList<ContentValues>>();
		personalAgendaList = CalendarDBHelperClassPreICS
				.getPersonalAgendaEventList(fromDate, toDate);
		ArrayList<ArrayList<ContentValues>> agendaDayValues = new ArrayList<ArrayList<ContentValues>>();
		Cursor cursor = getAllEventsByMonthRange(fromDate, toDate);
		try
		{
		if (cursor != null) {

			cursor.moveToFirst();
			boolean isDaily = false;
			/* To check Daily Events */
			ArrayList<ContentValues> dailyEvents = new ArrayList<ContentValues>();
			ArrayList<ContentValues> weekEvents = new ArrayList<ContentValues>();
			ArrayList<ContentValues> monthEvents = new ArrayList<ContentValues>();
			ArrayList<ContentValues> yearEvents = new ArrayList<ContentValues>();
			while (!cursor.isAfterLast()) {
				int rectype = cursor.getInt(cursor
						.getColumnIndex(Recurrence.TYPE));
				 CalendarLog.d(CalendarConstants.Tag, "Agenda -rectype "
				 + rectype);
				if (rectype == 0) {
					isDaily = true;
					dailyEvents.add(DefaultContantValues(cursor));
				} else if (rectype == 1) {
					weekEvents.add(DefaultContantValues(cursor));
				} else if (rectype == 2 || rectype == 3) {
					monthEvents.add(DefaultContantValues(cursor));
				} else if (rectype == 5 || rectype == 6) {
					yearEvents.add(DefaultContantValues(cursor));
				}
				cursor.moveToNext();
			}
			CalendarLog.i(CalendarConstants.Tag, "Agenda -dailyEvents.size() "
					+ dailyEvents.size());
			CalendarLog.i(CalendarConstants.Tag, "Agenda -weekEvents.size() "
					+ weekEvents.size());
			/* For creating empty list for all days */
			
			Calendar curTime = Email.getNewCalendar();
			Calendar endTime = Email.getNewCalendar();
			DateFormat formatter = new SimpleDateFormat("dd MMM yyyy",
					Locale.US);

			try {
				curTime.setTime(formatter.parse(fromDate));
				endTime.setTime(formatter.parse(toDate));
			} catch (ParseException e) {

				e.printStackTrace();
			}
			endTime.add(Calendar.DAY_OF_YEAR, -1); // Date

			while (curTime.getTimeInMillis() <= endTime.getTimeInMillis()) {
				// dates.add(new Date(curTime));
				ContentValues dayValue = new ContentValues();
				ArrayList<ContentValues> dayValues = new ArrayList<ContentValues>();
				DecimalFormat mFormat = new DecimalFormat("00");
				if (isDaily) {
					// CalendarLog.i(CalendarConstants.Tag,
					// "Agenda - Daily "+curTime);

					int totalEvents = 0;
					for (ContentValues contentValues : dailyEvents) {
						String eventStartDate = contentValues
								.getAsString(Event.START_DATE);
						int interval = contentValues
								.getAsInteger(Recurrence.INTERVAL);
						int occurences = contentValues
								.getAsInteger(Recurrence.OCCURENCES);
						long untill = contentValues.getAsLong(Recurrence.UNTIL);
						// CalendarLog.i(CalendarConstants.Tag,
						// "Agenda - Event.TITLE "+contentValues
						// .getAsString(Event.TITLE));
						// CalendarLog.i(CalendarConstants.Tag,
						// "Agenda - interval "+interval);
						// CalendarLog.i(CalendarConstants.Tag,
						// "Agenda - occurences "+occurences);
						// CalendarLog.i(CalendarConstants.Tag,
						// "Agenda - untill "+untill);
						String[] strDayArray = eventStartDate.split("-");
						// String strYear = strDayArray[0];
						String eveMonth = strDayArray[1];
						String eveDay = strDayArray[2];
						boolean isAfter = true;
						Calendar eventCal = Email.getNewCalendar();
						eventCal.setFirstDayOfWeek(Calendar.SUNDAY);
						eventCal.set(Calendar.DAY_OF_MONTH,
								Integer.valueOf(eveDay));
						eventCal.set(Calendar.MONTH,
								Integer.valueOf(eveMonth) - 1);
						eventCal.set(Calendar.YEAR,
								Integer.valueOf(strDayArray[0]));
						if (curTime.getTime().before(eventCal.getTime())) {
							isAfter = false;
						} else
							isAfter = true;

						int daydifference = ((int) ((curTime.getTimeInMillis() - eventCal
								.getTimeInMillis()) / MILLISECS_PER_DAY));
						String eventDowString = contentValues
								.getAsString(Recurrence.DOW_STRING);
						// CalendarLog.i(CalendarConstants.Tag,
						// "Agenda - getTimeInMillis() "+curTime.getTimeInMillis());
						// CalendarLog.i(CalendarConstants.Tag,
						// "Agenda - isAfter "+isAfter);
						//
						// CalendarLog.i(CalendarConstants.Tag,
						// "Agenda - daydifference 3 "+(occurences == -1 ||
						// daydifference < (interval * occurences)));
						// CalendarLog.i(CalendarConstants.Tag,
						// "Agenda - daydifference 2 "+((daydifference %
						// interval)
						// == 0 || interval == -1 || interval == 1) );
						//
						// CalendarLog.i(CalendarConstants.Tag,
						// "Agenda - daydifference "+daydifference);
						// CalendarLog.i(CalendarConstants.Tag,
						// "Agenda - daydifference untill"+(untill == -1 ||
						// untill
						// >= curTime.getTimeInMillis()));
						if (isAfter
								&& ((daydifference % interval) == 0
										|| interval == -1 || interval == 1)
								&& (occurences == -1 || daydifference < (interval * occurences))
								&& (untill == -1 || untill >= curTime
										.getTimeInMillis()) ) {
							CalendarLog.i(
									CalendarConstants.Tag,
									"Agenda - Daily After : "
											+ contentValues
													.getAsString(Event.TITLE));
							boolean isException = false;
							boolean isExceptionDelete = false;
							Exceptions exception = new Exceptions();
							String currDate = curTime.get(Calendar.YEAR)
									+ "-"
									+ mFormat.format(curTime
											.get(Calendar.MONTH) + 1)
									+ "-"
									+ mFormat.format(curTime
											.get(Calendar.DAY_OF_MONTH));
							String eventID = contentValues.getAsString("_id");
							if (contentValues.getAsString(Event.HAS_EXCEPTIONS)
									.equalsIgnoreCase("1")) {
								ArrayList<Exceptions> exceptionList = getExceptionsForEvent(Integer
										.parseInt(eventID));
								CalendarLog.d(CalendarConstants.Tag, "size:"
										+ exceptionList.size());
								for (Exceptions excepti : exceptionList) {
									if (excepti.isExceptionDeleted == 1) {
										CalendarLog
												.d(CalendarConstants.Tag,
														"isException if:"
																+ isException);

										isExceptionDelete = true;
									} else {
										CalendarLog.d(CalendarConstants.Tag,
												"isException else:"
														+ isException);
										CalendarLog.d(CalendarConstants.Tag,
												"isException else date:"
														+ currDate);
										// getExceptionContentValues(excepti);
										if (excepti.startDate
												.equalsIgnoreCase(currDate)) {
											isException = true;
											exception = excepti;
										}
									}
								}
							}
							if (isExceptionDelete) {

							} else {
								if (isException) {

									dayValue.put("_id", eventID);
									dayValue.put(Event.TITLE, exception.title);
									dayValue.put("bg_color", Color.WHITE);
									dayValue.put("text_color", Color.BLACK);
									dayValue.put(Event.DTSTART,
											exception.dtstart);
									dayValue.put(Event.START_DATE, currDate);
									dayValue.put("HeaderDate", currDate);
									dayValue.put(Event.END_DATE,
											exception.endDate);
									dayValue.put(Event.DTEND, exception.dtend);
									dayValue.put("all_day", exception.allDay);
									dayValue.put(
											CalendarConstants.CALENDAR_TYPE_KEY,
											CalendarConstants.CALENDAR_TYPE_CORPORATE);

									Recurrence recrr = exception.recurrence;
									dayValue.put(Recurrence.TYPE,
											exception.responseType);
									if (recrr != null) {
										dayValue.put(Recurrence.OCCURENCES,
												recrr.occurences);
										dayValue.put(Recurrence.INTERVAL,
												recrr.interval);
										dayValue.put(Recurrence.DOW, recrr.dow);
										dayValue.put(Recurrence.DOW_STRING,
												recrr.dowString);
										dayValue.put(Recurrence.DOM, recrr.dom);
										dayValue.put(Recurrence.WOM, recrr.wom);
										dayValue.put(Recurrence.MOY, recrr.moy);
										dayValue.put(Recurrence.UNTIL,
												recrr.until);
									} else {
										dayValue.put(Recurrence.OCCURENCES,
												"-1");
										dayValue.put(Recurrence.INTERVAL, "-1");
										dayValue.put(Recurrence.DOW, "-1");
										dayValue.put(Recurrence.DOW_STRING, "");
										dayValue.put(Recurrence.DOM, "-1");
										dayValue.put(Recurrence.WOM, "-1");
										dayValue.put(Recurrence.MOY, "-1");
										dayValue.put(Recurrence.UNTIL, "-1");
									}
									dayValue.put(
											CalendarConstants.RECURRENCE_CURRENTDATE,
											currDate);
									// CalendarLog.d(CalendarConstants.Tag,"RECURRENCE_CURRENTDATE"+
									// date);
									// CalendarLog.d(CalendarConstants.Tag,"event"+""+
									// dayValue.get("event"));
									// CalendarLog.d(CalendarConstants.Tag,"start_time"+""+
									// dayValue.get("start_time"));
									// CalendarLog.d(CalendarConstants.Tag,"end_time"+""+
									// dayValue.get("end_time"));

								} else {
									
									dayValue.put("_id", eventID);
									dayValue.put(Event.TITLE, contentValues
											.getAsString(Event.TITLE));
									dayValue.put("bg_color", Color.WHITE);
									dayValue.put("text_color", Color.BLACK);
									dayValue.put(Event.DTSTART, contentValues
											.getAsString(Event.DTSTART));
									dayValue.put(Event.EVENT_TIMEZONE, contentValues.getAsString(Event.EVENT_TIMEZONE));
									dayValue.put(Event.START_DATE, currDate);
									dayValue.put("HeaderDate", currDate);
									dayValue.put(Event.END_DATE, contentValues
											.getAsString(Event.END_DATE));
									dayValue.put(Event.DTEND, contentValues
											.getAsString(Event.DTEND));
									dayValue.put("all_day", contentValues
											.getAsString(Event.ALL_DAY));
									dayValue.put(
											CalendarConstants.CALENDAR_TYPE_KEY,
											CalendarConstants.CALENDAR_TYPE_CORPORATE);

									dayValue.put(Recurrence.TYPE, contentValues
											.getAsInteger(Recurrence.TYPE));
									dayValue.put(
											Recurrence.OCCURENCES,
											contentValues
													.getAsInteger(Recurrence.OCCURENCES));
									dayValue.put(
											Recurrence.INTERVAL,
											contentValues
													.getAsInteger(Recurrence.INTERVAL));
									dayValue.put(Recurrence.DOW, contentValues
											.getAsInteger(Recurrence.DOW));
									dayValue.put(
											Recurrence.DOW_STRING,
											contentValues
													.getAsString(Recurrence.DOW_STRING));
									dayValue.put(Recurrence.DOM, contentValues
											.getAsInteger(Recurrence.DOM));
									dayValue.put(Recurrence.WOM, contentValues
											.getAsInteger(Recurrence.WOM));
									dayValue.put(Recurrence.MOY, contentValues
											.getAsInteger(Recurrence.MOY));
									dayValue.put(
											Recurrence.UNTIL,
											contentValues
													.getAsLong(Recurrence.UNTIL));
									CalendarLog.d(CalendarConstants.Tag,"content fvalue for daily"+dayValue.toString());
									dayValues.add(dayValue);
								}
								totalEvents++;
							}
						}
					}
					if (totalEvents == 0) {
						CalendarLog.i(CalendarConstants.Tag,
								"Agenda - Daily 0 events ");
						dayValue.put("_id", "");
						dayValue.put(Event.TITLE, "");
						dayValue.put("bg_color", Color.WHITE);
						dayValue.put("text_color", Color.BLACK);
						dayValue.put(Event.DTSTART, "-1");
						dayValue.put(
								Event.START_DATE,
								curTime.get(Calendar.YEAR)
										+ "-"
										+ mFormat.format(curTime
												.get(Calendar.MONTH) + 1)
										+ "-"
										+ mFormat.format(curTime
												.get(Calendar.DAY_OF_MONTH)));
						dayValue.put(Event.DTEND, "-1");
						dayValue.put("all_day", "0");
						dayValue.put(CalendarConstants.CALENDAR_TYPE_KEY,
								CalendarConstants.CALENDAR_TYPE_CORPORATE);

						dayValues.add(dayValue);
					}
					dayValues = getWeeklyEvents(curTime, weekEvents, dayValues);
					dayValues = getMontlyEvents(curTime, monthEvents, dayValues);
					dayValues = getYearlyEvents(curTime, yearEvents, dayValues);
					agendaDayValues.add(dayValues);

				} else {
					CalendarLog.i(CalendarConstants.Tag,
							"Agenda - Daily 0 events else");
					dayValue.put("_id", "");
					dayValue.put(Event.TITLE, "");
					dayValue.put("bg_color", Color.WHITE);
					dayValue.put("text_color", Color.BLACK);
					dayValue.put(Event.DTSTART, "-1");
					dayValue.put(
							Event.START_DATE,
							curTime.get(Calendar.YEAR)
									+ "-"
									+ mFormat.format(curTime
											.get(Calendar.MONTH) + 1)
									+ "-"
									+ mFormat.format(curTime
											.get(Calendar.DAY_OF_MONTH)));
					dayValue.put(Event.DTEND, "-1");
					dayValue.put("all_day", "0");
					dayValue.put(CalendarConstants.CALENDAR_TYPE_KEY,
							CalendarConstants.CALENDAR_TYPE_CORPORATE);
					dayValues.add(dayValue);
					dayValues = getWeeklyEvents(curTime, weekEvents, dayValues);
					dayValues = getMontlyEvents(curTime, monthEvents, dayValues);
					dayValues = getYearlyEvents(curTime, yearEvents, dayValues);
					agendaDayValues.add(dayValues);
				}

				curTime.add(Calendar.DAY_OF_YEAR, 1);
			}
			CalendarLog.i(CalendarConstants.Tag,
					"Agenda -agendaDayValues.size() " + agendaDayValues.size());
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				ArrayList<ContentValues> dateList = new ArrayList<ContentValues>();

				String temp = cursor.getString(cursor
						.getColumnIndex(Event.START_DATE));
				// CalendarLog.d(CalendarConstants.Tag,"start date for agenda",temp);
				for (fromDate = temp; !cursor.isAfterLast()
						&& (fromDate.equalsIgnoreCase(temp));) {
					ContentValues dateValue = new ContentValues();

					dateValue.put("_id",
							cursor.getInt(cursor.getColumnIndex("_id")));
					dateValue.put(Event.TITLE, cursor.getString(cursor
							.getColumnIndex(Event.TITLE)));
					// dateValue.put(Event.EVENT_ID,
					// cursor.getString(cursor.getColumnIndex(Event.EVENT_ID)));
					dateValue.put(Event.DTSTART, cursor.getLong(cursor
							.getColumnIndex(Event.DTSTART)));
					dateValue.put(Event.START_DATE, temp);
					dateValue.put(Event.END_DATE, cursor.getString(cursor
							.getColumnIndex(Event.END_DATE)));
					dateValue.put(Event.EVENT_LOCATION, cursor.getString(cursor
							.getColumnIndex(Event.EVENT_LOCATION)));
					dateValue.put(Event.EVENT_TIMEZONE, cursor.getString(cursor
							.getColumnIndex(Event.EVENT_TIMEZONE)));					
					dateValue.put(Event.DTEND,
							cursor.getLong(cursor.getColumnIndex(Event.DTEND)));
					dateValue.put("all_day", cursor.getLong(cursor
							.getColumnIndex(Event.ALL_DAY)));
					dateValue.put(CalendarConstants.CALENDAR_TYPE_KEY,
							CalendarConstants.CALENDAR_TYPE_CORPORATE);
					dateValue.put(Recurrence.TYPE, cursor.getInt(cursor
							.getColumnIndex(Recurrence.TYPE)));
					dateValue.put(Recurrence.OCCURENCES, cursor.getInt(cursor
							.getColumnIndex(Recurrence.OCCURENCES)));
					dateValue.put(Recurrence.INTERVAL, cursor.getInt(cursor
							.getColumnIndex(Recurrence.INTERVAL)));
					dateValue.put(Recurrence.DOW, cursor.getInt(cursor
							.getColumnIndex(Recurrence.DOW)));
					dateValue.put(Recurrence.DOW_STRING, cursor
							.getString(cursor
									.getColumnIndex(Recurrence.DOW_STRING)));
					dateValue.put(Recurrence.DOM, cursor.getInt(cursor
							.getColumnIndex(Recurrence.DOM)));
					dateValue.put(Recurrence.WOM, cursor.getInt(cursor
							.getColumnIndex(Recurrence.WOM)));
					dateValue.put(Recurrence.MOY, cursor.getInt(cursor
							.getColumnIndex(Recurrence.MOY)));
					dateValue.put(Recurrence.UNTIL, cursor.getLong(cursor
							.getColumnIndex(Recurrence.UNTIL)));

					// CalendarLog.i(CalendarConstants.Tag, "temp " + temp);
					// if (rr.equalsIgnoreCase("0"))
					dateList.add(dateValue);
					cursor.moveToNext();
					if (!cursor.isAfterLast()) {
						temp = fromDate;
						fromDate = cursor.getString(cursor
								.getColumnIndex(Event.START_DATE));

					}
				}
				// §Collections.sort(dateList);
				listByDate.add(dateList);
				// cursor.moveToNext();
			}
			CalendarLog.i(CalendarConstants.Tag, "Agenda -listByDate.size() "
					+ listByDate.size());
			cursor.close();
		}
		mergedAgendaList.addAll(listByDate);
		// mergedAgendaList.addAll(personalAgendaList);
		CalendarLog.i(CalendarConstants.Tag, "Agenda -mergedAgendaList.size() "
				+ mergedAgendaList.size());
		if (displayPersonalEvent()) {
			mergedAgendaList.addAll(personalAgendaList);
		}
		CalendarLog.i(CalendarConstants.Tag, "Agenda -mergedAgendaList.size() "
				+ mergedAgendaList.size());
		// CalendarLog.e(CalendarConstants.Tag,
		// "Agenda -agendaDayValues.size() "
		// + agendaDayValues.size());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		ArrayList<ArrayList<ContentValues>> agendaDay = mergerlist(
				agendaDayValues, mergedAgendaList);
		CalendarLog.i(CalendarConstants.Tag, "Agenda -agendaDay.size() "
				+ agendaDay.size());
		return agendaDay;
	}

	/**
	 * For checking the Recurrence events and merge the values with database
	 * 
	 * @param agendaDayValues
	 * @param listByDate
	 * @return
	 */

	private static ArrayList<ArrayList<ContentValues>> mergerlist(
			ArrayList<ArrayList<ContentValues>> agendaDayValues,
			ArrayList<ArrayList<ContentValues>> listByDate) {
		ArrayList<ArrayList<ContentValues>> removedEmpty = new ArrayList<ArrayList<ContentValues>>();
		try {
			for (int i = 0; i < agendaDayValues.size(); i++) {
				
				for (int j = 0; j < listByDate.size(); j++) {					
					if (listByDate.get(j).size() > 0
							&& agendaDayValues.get(i).size() > 0) {	
						
						String agendaStartTime = agendaDayValues
								.get(i)
								.get(0)
								.getAsString(Event.START_DATE);
//						CalendarLog.d(CalendarConstants.Tag, "mergelist-agendaStartTime"+agendaStartTime);
						long[] agendaLong = CalendarCommonFunction.convertDateToMillisec(agendaStartTime);
//						CalendarLog.d(CalendarConstants.Tag, "mergelist-agendaLong[0]"+agendaLong[0]);
//						CalendarLog.d(CalendarConstants.Tag, "mergelist-agendaLong[1]"+agendaLong[1]);
						for (int k = 0; k < listByDate.get(j).size(); k++) {
						
						long listStartTime = listByDate.get(j).get(k)
								.getAsLong(Event.DTSTART);
						long listEndTime = listByDate.get(j).get(k)
								.getAsLong(Event.DTEND);
						
						
//						CalendarLog.d(CalendarConstants.Tag, "mergelist-listStartTime"+listStartTime);
//						CalendarLog.d(CalendarConstants.Tag, "mergelist-listEndTime"+listEndTime);
						
						if (agendaDayValues
								.get(i)
								.get(0)
								.getAsString(Event.START_DATE)
								.equalsIgnoreCase(
										listByDate.get(j).get(k)
												.getAsString(Event.START_DATE))) {
							ContentValues tempEntery = new ContentValues(listByDate.get(j).get(k));
							tempEntery.put("HeaderDate", agendaStartTime);
							CalendarLog.d(CalendarConstants.Tag, "mergelist if -Event.TITLE "+i +"k:"+k+ " - " +j+ " - "+listByDate.get(j).get(0)
									.getAsString(Event.TITLE)+ " $ " +tempEntery);
							agendaDayValues.get(i).add(tempEntery);
						} else if (agendaDayValues
								.get(i)
								.get(0)
								.getAsString(Event.START_DATE)
								.equalsIgnoreCase(
										listByDate.get(j).get(k)
												.getAsString(Event.END_DATE)) && listByDate.get(j).get(k)
												.getAsString(Event.END_DATE).equalsIgnoreCase("0")) {
							ContentValues tempEntery = new ContentValues(listByDate.get(j).get(k));
//							CalendarLog.d(CalendarConstants.Tag, "mergelist -Event.TITLE"+i +"k:"+k+ " - " +j+ " - "+ listByDate.get(j).get(0)
//									.getAsString(Event.TITLE) + " $ " +tempEntery);
							tempEntery.put("HeaderDate",agendaStartTime);
							CalendarLog.d(CalendarConstants.Tag, "mergelist 1 -Event.TITLE"+i +"k:"+k+ " - " +j+ " - "+ listByDate.get(j).get(k)
									.getAsString(Event.TITLE) + " $ " +tempEntery);
							agendaDayValues.get(i).add(tempEntery);
						}
						else if((listStartTime<agendaLong[0] && listStartTime<agendaLong[1]) 
								&& (listEndTime>agendaLong[1] && listEndTime>agendaLong[0])){
//							listByDate.get(j).get(0).put("HeaderDate", agendaDayValues
//									.get(i)
//									.get(0)
//									.getAsString(Event.START_DATE));
//							agendaDayValues.get(i).addAll(listByDate.get(j));
							ContentValues tempEntery = new ContentValues(listByDate.get(j).get(k));
							tempEntery.put("HeaderDate", agendaStartTime);
							CalendarLog.d(CalendarConstants.Tag, "mergelist else if 2 -Event.TITLE "+i +"k:"+k+ " - " +j+ " - "+listByDate.get(j).get(0)
									.getAsString(Event.TITLE)+ " $ " +tempEntery);
							agendaDayValues.get(i).add(tempEntery);
						}
					}
					}
				}
			}

			for (ArrayList<ContentValues> arrContent : agendaDayValues) {
				ArrayList<ContentValues> removeArrCont = new ArrayList<ContentValues>();
				for (ContentValues contentValues : arrContent) {
					if (contentValues.getAsString("_id") != null)
						if (!contentValues.getAsString("_id").equalsIgnoreCase(
								"")) {
							// arrContent.remove(contentValues);
							removeArrCont.add(contentValues);
						}
				}
				if (removeArrCont.size() > 0)
					removedEmpty.add(removeArrCont);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return removedEmpty;
	}

	private static Cursor getAllEventsByMonthRange(String fromDate,
			String toDate) {

		try {
			String projection[] = { "_id", Event.START_DATE, Event.END_DATE,
					Event.EVENT_LOCATION, Event.DTSTART, Event.DTEND,
					Event.EVENT_TIMEZONE, Event.TITLE, Event.ALL_DAY,
					Event.RRULE, Recurrence.TYPE, Recurrence.OCCURENCES,
					Recurrence.INTERVAL, Recurrence.DOW, Recurrence.DOW_STRING,
					Recurrence.DOM, Recurrence.WOM, Recurrence.MOY,
					Recurrence.UNTIL, Event.HAS_EXCEPTIONS };
			// CalendarLog.d(CalendarConstants.Tag,"from month ",getDateString(fromDate,0,0));
			// CalendarLog.d(CalendarConstants.Tag,"to month ",getDateString(toDate,0,0));
			String whereClause = "((" + Event.START_DATE
					+ " between ? and ? ) or " + Recurrence.TYPE
					+ " >= ? ) and " + Event.STATUS + " != ? order by startDate";
			
//			String whereClause = "((" + Event.START_DATE
//					+ " between ? and ? ) and " + Recurrence.TYPE
//					+ " < 0 ) and " + Event.STATUS + " != "
//					+ Event.STATUS_DELETED + " order by startDate";
			String[] whereArgs = new String[] { getDateString(fromDate, 0, 0),
					getDateString(toDate, 0, 0),"0",String.valueOf(Event.STATUS_DELETED) };

			Cursor cursor = Email
					.getAppContext()
					.getContentResolver()
					.query(Event.CONTENT_URI, projection, whereClause,
							whereArgs, null);
			return cursor;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	private static Cursor getAllEventsByMonth(String date, int difference) {
		try {
			String[] days = date.split(" ");

			Calendar cal = Email.getNewCalendar();
			cal.setFirstDayOfWeek(Calendar.SUNDAY);
			SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy",
					Locale.US);
			try {
				cal.setTime(sdf.parse(date));
			} catch (ParseException e) {
				e.printStackTrace();
			}// all done
			cal.set(Calendar.DAY_OF_MONTH, Integer.valueOf(days[0]));
			cal.set(Calendar.MONTH, cal.get(Calendar.MONTH));
			cal.set(Calendar.YEAR, Integer.valueOf(days[2]));
			// int dayoftheweek = cal.get(Calendar.DAY_OF_WEEK);

			String month = String.valueOf(cal.get(Calendar.MONTH) + 1);

			if (Integer.valueOf(month) < 10 && month.length() == 1) {
				month = "0" + month;
			}
			String projection[] = { "_id", Event.TITLE, Event.START_DATE,
					Event.END_DATE, Event.DTSTART, Event.DTEND, Event.ALL_DAY,
					Event.RRULE, Recurrence.TYPE, Recurrence.OCCURENCES,
					Recurrence.INTERVAL, Recurrence.DOW, Recurrence.DOW_STRING,
					Recurrence.DOM, Recurrence.WOM, Recurrence.MOY,
					Recurrence.UNTIL };
			// CalendarLog.d(CalendarConstants.Tag,"from month ",getDateString(date,0,0));
			// CalendarLog.d(CalendarConstants.Tag,"to month "+difference,getDateString(date,0,difference));
			String whereClause = "(" + Event.START_DATE
					+ " between ? and ? )  and " + Event.STATUS + " != ?";
			String[] whereArgs = new String[] { getDateString(date, 0, 0),
					getDateString(date, 0, difference),String.valueOf(Event.STATUS_DELETED) };
			String orderBy = Event.START_DATE;

			Cursor cursor = Email
					.getAppContext()
					.getContentResolver()
					.query(Event.CONTENT_URI, projection, whereClause,
							whereArgs, orderBy);
			return cursor;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return null;
		}

	}

	// To get month events pass 1 as difference for the following method

	public static ArrayList<ArrayList<ContentValues>> getEventListAgenda(
			String date, int difference) {

		ArrayList<ArrayList<ContentValues>> listByDate = new ArrayList<ArrayList<ContentValues>>();
		Cursor cursor = getAllEventsByMonth(date, difference);
		// CalendarLog.d(CalendarConstants.Tag,"number of rows in cursor ",""+cursor.getCount());
		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			ArrayList<ContentValues> dateList = new ArrayList<ContentValues>();
			String temp = cursor.getString(cursor
					.getColumnIndex(Event.START_DATE));
			for (date = temp; !cursor.isAfterLast()
					&& (date.equalsIgnoreCase(cursor.getString(cursor
							.getColumnIndex(Event.START_DATE))));) {
				ContentValues dateValue = new ContentValues();
				dateValue.put("_id",
						cursor.getInt(cursor.getColumnIndex("_id")));
				dateValue.put(Event.TITLE,
						cursor.getString(cursor.getColumnIndex(Event.TITLE)));
				dateValue.put("all_day",
						cursor.getLong(cursor.getColumnIndex(Event.ALL_DAY)));
				dateValue.put("calendarType", "CorporateCalendar");
				dateList.add(dateValue);
				cursor.moveToNext();
				if (!cursor.isAfterLast())
					date = cursor.getString(cursor
							.getColumnIndex(Event.START_DATE));
			}
			listByDate.add(dateList);
			cursor.moveToNext();
		}
		cursor.close();
		return listByDate;
	}

	public static ArrayList<ArrayList<ContentValues>> getMonthEventList(
			String date) {
		CalendarLog.d(CalendarConstants.Tag, "From date " + date);
		CalendarLog.d(CalendarConstants.Tag,
				"To date " + getDateString(date, 0, 1));
		// String fromdate = getDateFormatForPresentDay(date);
		String todate = getDateFormatForPresentDay(getDateString(date, 0, 1));
		return getEventListAgenda(date, todate);
	}

	/*********************** GET EVENT DETAILS WITH EVENT_ID ************************************/

	public static Cursor getEvent(long event_id) {
		String projection[] = { "_id", Event.EVENT_ID, Event.CALENDAR_UID,
				Event.TITLE, Event.START_DATE, Event.END_DATE, Event.DTSTART,
				Event.DTEND, Event.EVENT_TIMEZONE, Event.ALL_DAY,
				Event.DESCRIPTION, Event.RESPONSE_TYPE, Event.EVENT_LOCATION,
				Event.ORGANIZER, Event.AVAILABILITY, Event.ACCESSIBILITY,
				Event.RRULE, Recurrence.TYPE, Recurrence.OCCURENCES,
				Recurrence.INTERVAL, Recurrence.DOW, Recurrence.DOW_STRING,
				Recurrence.DOM, Recurrence.WOM, Recurrence.MOY,
				Recurrence.UNTIL };
		String whereClause = "_id = ? and " + Event.STATUS
				+ " != ?" ;
String whereArg[] = {String.valueOf(event_id),String.valueOf(Event.STATUS_DELETED)};
		String orderBy = Event.START_DATE;
		Cursor cursor = Email
				.getAppContext()
				.getContentResolver()
				.query(Event.CONTENT_URI, projection, whereClause, whereArg,
						orderBy);
		return cursor;
	}

	public static String getDateForSyncing(long milliseconds) {

		Calendar calendar = Email.getNewCalendar();
		calendar.setTimeInMillis(milliseconds);
		calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
		String month = "" + (calendar.get(Calendar.MONTH) + 1);
		if (month.length() == 1)
			month = "0" + month;
		String date = "" + (calendar.get(Calendar.DATE));
		if (date.length() == 1)
			date = "0" + date;
		String hr = "" + (calendar.get(Calendar.HOUR_OF_DAY));
		if (hr.length() == 1)
			hr = "0" + hr;
		String min = "" + (calendar.get(Calendar.MINUTE));
		if (min.length() == 1)
			min = "0" + min;
		String sec = "" + (calendar.get(Calendar.SECOND));
		if (sec.length() == 1)
			sec = "0" + sec;
		String startString = calendar.get(Calendar.YEAR) + month + date + "T"
				+ hr + min + sec + "Z";
		CalendarLog.d(CalendarConstants.Tag, "get dat for syncing "
				+ startString);
		return startString;
	}

	public static Event getEventDetails(long event_id) {
		Event event = new Event();
		Cursor cursor = getEvent(event_id);
		if (cursor != null && cursor.moveToFirst()) {
			event._id = cursor.getLong(cursor.getColumnIndex("_id"));
			event.event_id = Integer.parseInt(cursor.getString(cursor
					.getColumnIndex(Event.EVENT_ID)));
			event.allDay = cursor.getInt(cursor.getColumnIndex(Event.ALL_DAY));
			event.title = cursor.getString(cursor.getColumnIndex(Event.TITLE));
			event.startDate = cursor.getString(cursor
					.getColumnIndex(Event.START_DATE));
			event.endDate = cursor.getString(cursor
					.getColumnIndex(Event.END_DATE));
			event.eventTimezone = cursor.getString(cursor
					.getColumnIndex(Event.EVENT_TIMEZONE));
			event.dtstart = cursor
					.getLong(cursor.getColumnIndex(Event.DTSTART));
			event.dtend = cursor.getLong(cursor.getColumnIndex(Event.DTEND));
			event.description = cursor.getString(cursor
					.getColumnIndex(Event.DESCRIPTION));
			event.eventLocation = cursor.getString(cursor
					.getColumnIndex(Event.EVENT_LOCATION));
			event.organizer = cursor.getString(cursor
					.getColumnIndex(Event.ORGANIZER));
			event.availability = cursor.getInt(cursor
					.getColumnIndex(Event.AVAILABILITY));
			event.accessibility = cursor.getInt(cursor
					.getColumnIndex(Event.ACCESSIBILITY));
			event.responseType = cursor.getInt(cursor
					.getColumnIndex(Event.RESPONSE_TYPE));
			event.calendarUID = cursor.getString(cursor
					.getColumnIndex(Event.CALENDAR_UID));

			event.rrule = cursor.getString(cursor.getColumnIndex(Event.RRULE));
			Recurrence recurrence = new Recurrence();
			recurrence.type = cursor.getInt(cursor
					.getColumnIndex(Recurrence.TYPE));
			recurrence.occurences = cursor.getInt(cursor
					.getColumnIndex(Recurrence.OCCURENCES));
			recurrence.interval = cursor.getInt(cursor
					.getColumnIndex(Recurrence.INTERVAL));
			recurrence.dow = cursor.getInt(cursor
					.getColumnIndex(Recurrence.DOW));
			recurrence.dowString = cursor.getString(cursor
					.getColumnIndex(Recurrence.DOW_STRING));
			recurrence.dom = cursor.getInt(cursor
					.getColumnIndex(Recurrence.DOM));
			recurrence.wom = cursor.getInt(cursor
					.getColumnIndex(Recurrence.WOM));
			recurrence.moy = cursor.getInt(cursor
					.getColumnIndex(Recurrence.MOY));
			recurrence.until = cursor.getLong(cursor
					.getColumnIndex(Recurrence.UNTIL));
			event.recurrence = recurrence;

		}
		if (cursor != null)
			cursor.close();
		return event;

	}

	/************************** DELETE EVENT WITH ID ********************/

	public static int deleteEventsFromDatabase() {
		String where = Event.STATUS + "= ?";
		String whereArg[] = {String.valueOf(Event.STATUS_DELETED)};
		int numberOfRowsDeleted = Email.getAppContext()
				.getContentResolver().delete(Event.CONTENT_URI, where, whereArg);
		// CalendarUtility.logCalendar("number of rows del",numberOfRowsDeleted+"");
		return numberOfRowsDeleted;
	}

	public static long deleteEventDetails(Event event) {
		// CalendarLog.d(CalendarConstants.Tag,"delete event ",""+id);
		event.status = Event.STATUS_DELETED;
		return insertEventToDatabase(event);

	}

	/************************** UPDATE EVENT WITH ID ********************/

	private static int updateEvent(Event event, long id) {
		String where = "_id = ?";
		String whereArg[] = {String.valueOf(id)};
		ContentValues values = getEventContentValues(event);
		int numberOfRowsUpdated = Email.getAppContext()
				.getContentResolver()
				.update(Event.CONTENT_URI, values, where, whereArg);
		return numberOfRowsUpdated;
	}

	public static int updateEventDetails(Event event, long id) {

		int numberOfRows = updateEvent(event, id);
		return numberOfRows;

	}

	/********************** SEARCH HISTORY ******************************/

	public static Cursor getSearchResult(String searchQuery) {
		String projection[] = { "_id", Event.TITLE, Event.EVENT_LOCATION,
				Event.DTSTART, Event.DTEND, Event.ALL_DAY, Event.START_DATE };
		String selection = Event.TITLE + " LIKE ?" + " OR "
				+ Event.EVENT_LOCATION + " LIKE ? order by " + Event.START_DATE;
		String[] selectionArgs = new String[] { "%" + searchQuery + "%" };

		// String orderBy = Event.START_DATE;
		Cursor cursor = Email
				.getAppContext()
				.getContentResolver()
				.query(Event.CONTENT_URI, projection, selection, selectionArgs,
						null);

		return cursor;
	}

	public static ArrayList<ArrayList<ContentValues>> getSearchEventListAgenda(
			String searchQuery) {
		ArrayList<ArrayList<ContentValues>> mergeSearchResult = new ArrayList<ArrayList<ContentValues>>();
		ArrayList<ArrayList<ContentValues>> listByDate = new ArrayList<ArrayList<ContentValues>>();
		ArrayList<ArrayList<ContentValues>> personalCalendarSearch;
		try {
			personalCalendarSearch = CalendarDBHelperClassPreICS
					.getSearchPersonalEventList(searchQuery);

			String fromDate;
			Cursor cursor = getSearchResult(searchQuery);
			cursor.moveToFirst();

			while (!cursor.isAfterLast()) {
				ArrayList<ContentValues> dateList = new ArrayList<ContentValues>();
				String temp = cursor.getString(cursor
						.getColumnIndex(Event.START_DATE));
				for (fromDate = temp; !cursor.isAfterLast()
						&& (fromDate.equalsIgnoreCase(temp));) {
					ContentValues dateValue = new ContentValues();

					dateValue.put("_id",
							cursor.getInt(cursor.getColumnIndex("_id")));
					dateValue.put(Event.TITLE,
							cursor.getString(cursor.getColumnIndex(Event.TITLE)));
					// dateValue.put(Event.EVENT_ID,
					// cursor.getString(cursor.getColumnIndex(Event.EVENT_ID)));
					dateValue.put(Event.DTSTART,
							cursor.getLong(cursor.getColumnIndex(Event.DTSTART)));
					dateValue.put(Event.START_DATE, temp);
					dateValue.put("HeaderDate", temp);
					dateValue.put(Event.DTEND,
							cursor.getLong(cursor.getColumnIndex(Event.DTEND)));
					dateValue.put("all_day",
							cursor.getLong(cursor.getColumnIndex(Event.ALL_DAY)));
					dateValue.put("calendarType", "CorporateCalendar");
					dateList.add(dateValue);
					cursor.moveToNext();
					if (!cursor.isAfterLast()) {
						temp = fromDate;
						fromDate = cursor.getString(cursor
								.getColumnIndex(Event.START_DATE));

					}
				}
				// §Collections.sort(dateList);
				listByDate.add(dateList);
			}
			cursor.close();
			mergeSearchResult.addAll(listByDate);
			// mergeSearchResult.addAll(personalCalendarSearch);
			if (displayPersonalEvent()) {
				mergeSearchResult.addAll(personalCalendarSearch);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mergeSearchResult;
	}

	public static void insertSearchQuery(String searchQuery) {
		ContentValues values = new ContentValues();
		values.put(CalendarConstants.SEARCH_STRING, searchQuery);
		Email.getAppContext().getContentResolver()
				.insert(CalendarConstants.SEARCH_CONTENT_URI, values);
	}

	public static ArrayList<String> getSearchHistory() {
		ArrayList<String> eventSearchHistoryList = new ArrayList<String>();

		Cursor cursor = Email
				.getAppContext()
				.getContentResolver()
				.query(CalendarConstants.SEARCH_CONTENT_URI, null, null, null,
						null);
		while (cursor.moveToNext()) {
			eventSearchHistoryList.add(cursor.getString(cursor
					.getColumnIndex(CalendarConstants.SEARCH_STRING)));
		}
		cursor.close();

		return eventSearchHistoryList;

	}

	public static void deleteSearchHistory() {
		Email.getAppContext().getContentResolver()
				.delete(CalendarConstants.SEARCH_CONTENT_URI, null, null);
	}

	public static void deleteRemindersForEvent(int event_id) {
		String where = Reminder.EVENT_ID + " = ?";
		String whereArg[] = {String.valueOf(event_id)};
		Email.getAppContext().getContentResolver()
				.delete(Reminder.CONTENT_URI, where, whereArg);
	}

	public static void changeStatusToSynced(String clientId, String newEventId,
			int status) {
		ContentValues values = new ContentValues();
		String where = Event.EVENT_ID + "= ? and (" + Event.STATUS + " = ? or "
				+ Event.STATUS + " = ?)";
		
		String selectionArgs[] = { clientId + "", Event.STATUS_ADDED + "",
				Event.STATUS_MODIFIED + "" };
		values.put(Event.EVENT_ID, newEventId);
		values.put(Event.STATUS, status);

		int numberOfRowsUpdated = Email.getAppContext()
				.getContentResolver()
				.update(Event.CONTENT_URI, values, where, selectionArgs);
		CalendarLog.d(CalendarConstants.Tag,
				"the number of rows updated to sync status is "
						+ numberOfRowsUpdated);

	}

	/*************** dual Sync added ***************************************/

	private static Cursor getAddedEventsFromDatabase() {
		String projection[] = { "_id", Event.EVENT_ID, Event.CALENDAR_UID,
				Event.TITLE, Event.START_DATE, Event.END_DATE, Event.DTSTART,
				Event.DTEND, Event.EVENT_TIMEZONE, Event.ALL_DAY,
				Event.DESCRIPTION, Event.EVENT_LOCATION, Event.ORGANIZER,
				Event.RRULE, Event.AVAILABILITY, Event.ACCESSIBILITY };
		String whereClause = Event.STATUS + " = ?";
		String whereArg[] = {String.valueOf(Event.STATUS_ADDED)};
		Cursor cursor = Email.getAppContext().getContentResolver()
				.query(Event.CONTENT_URI, projection, whereClause, whereArg, null);
		return cursor;

	}

	public static ArrayList<Event> getAllAddedEvents() {
		ArrayList<Event> changes = new ArrayList<Event>();
		Cursor cursor;
		cursor = getAddedEventsFromDatabase();

		if (cursor.moveToFirst()) {
			while (!cursor.isAfterLast()) {
				Event event = new Event();
				event._id = cursor.getLong(cursor.getColumnIndex("_id"));
				event.event_id = Integer.parseInt(cursor.getString(cursor
						.getColumnIndex(Event.EVENT_ID)));
				event.calendarUID = cursor.getString(cursor
						.getColumnIndex(Event.CALENDAR_UID));

				event.allDay = cursor.getInt(cursor
						.getColumnIndex(Event.ALL_DAY));
				event.title = cursor.getString(cursor
						.getColumnIndex(Event.TITLE));
				event.startDate = cursor.getString(cursor
						.getColumnIndex(Event.START_DATE));
				event.endDate = cursor.getString(cursor
						.getColumnIndex(Event.END_DATE));
				event.eventTimezone = cursor.getString(cursor
						.getColumnIndex(Event.EVENT_TIMEZONE));
				event.dtstart = cursor.getLong(cursor
						.getColumnIndex(Event.DTSTART));
				event.dtend = cursor
						.getLong(cursor.getColumnIndex(Event.DTEND));
				event.description = cursor.getString(cursor
						.getColumnIndex(Event.DESCRIPTION));
				event.eventLocation = cursor.getString(cursor
						.getColumnIndex(Event.EVENT_LOCATION));
				event.organizer = cursor.getString(cursor
						.getColumnIndex(Event.ORGANIZER));
				event.rrule = cursor.getString(cursor
						.getColumnIndex(Event.RRULE));
				event.availability = cursor.getInt(cursor
						.getColumnIndex(Event.AVAILABILITY));
				event.accessibility = cursor.getInt(cursor
						.getColumnIndex(Event.ACCESSIBILITY));
				changes.add(event);
				cursor.moveToNext();
			}
		}
		cursor.close();

		return changes;
	}

	private static Cursor getAllInvitesFromDatabase() {

		try {
			String projection[] = { "_id", Event.EVENT_ID, Event.CALENDAR_UID,
					Event.TITLE, Event.START_DATE, Event.END_DATE,
					Event.DTSTART, Event.DTEND, Event.ALL_DAY,
					Event.DESCRIPTION, Event.RESPONSE_TYPE,
					Event.EVENT_LOCATION, Event.ORGANIZER, Event.AVAILABILITY,
					Event.ACCESSIBILITY, Event.RRULE };
			String whereClause = Event.ORGANIZER + " != ? and " +
					Event.RESPONSE_TYPE + " != ? and " + Event.STATUS + " = ?";
			
			String whereArg[] = {getEmail_ID(),String.valueOf(Event.RESPONSE_TYPE_NONE),String.valueOf(Event.STATUS_MODIFIED)};

			Cursor cursor = Email
					.getAppContext()
					.getContentResolver()
					.query(Event.CONTENT_URI, projection, whereClause, whereArg,
							null);
			return cursor;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public static ArrayList<Event> getAllInvitedEvents() {
		ArrayList<Event> changes = new ArrayList<Event>();
		try {
			Cursor cursor;
			cursor = getAllInvitesFromDatabase();
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					while (!cursor.isAfterLast()) {
						Event event = new Event();
						event._id = cursor
								.getLong(cursor.getColumnIndex("_id"));
						event.event_id = Integer.parseInt(cursor
								.getString(cursor
										.getColumnIndex(Event.EVENT_ID)));
						event.allDay = cursor.getInt(cursor
								.getColumnIndex(Event.ALL_DAY));
						event.title = cursor.getString(cursor
								.getColumnIndex(Event.TITLE));
						event.startDate = cursor.getString(cursor
								.getColumnIndex(Event.START_DATE));
						event.endDate = cursor.getString(cursor
								.getColumnIndex(Event.END_DATE));
						event.dtstart = cursor.getLong(cursor
								.getColumnIndex(Event.DTSTART));
						event.dtend = cursor.getLong(cursor
								.getColumnIndex(Event.DTEND));
						event.description = cursor.getString(cursor
								.getColumnIndex(Event.DESCRIPTION));
						event.eventLocation = cursor.getString(cursor
								.getColumnIndex(Event.EVENT_LOCATION));
						event.organizer = cursor.getString(cursor
								.getColumnIndex(Event.ORGANIZER));
						event.availability = cursor.getInt(cursor
								.getColumnIndex(Event.AVAILABILITY));
						event.accessibility = cursor.getInt(cursor
								.getColumnIndex(Event.ACCESSIBILITY));
						event.responseType = cursor.getInt(cursor
								.getColumnIndex(Event.RESPONSE_TYPE));
						event.calendarUID = cursor.getString(cursor
								.getColumnIndex(Event.CALENDAR_UID));

						event.rrule = cursor.getString(cursor
								.getColumnIndex(Event.RRULE));
						event.attendees = getAttendeeValues((int) event._id);
						changes.add(event);
						cursor.moveToNext();
					}
				}
				cursor.close();
			}
			return changes;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return changes;
		}
	}

	private static Cursor getModifiedEventsFromDatabase() {
		String projection[] = { "_id", Event.EVENT_ID, Event.CALENDAR_UID,
				Event.TITLE, Event.START_DATE, Event.END_DATE, Event.DTSTART,
				Event.EVENT_TIMEZONE, Event.DTEND, Event.ALL_DAY,
				Event.DESCRIPTION, Event.EVENT_LOCATION, Event.ORGANIZER,
				Event.RRULE, Event.ACCESSIBILITY, Event.AVAILABILITY };
		String whereClause = Event.STATUS + " = ?";
		String whereArg[] = {String.valueOf(Event.STATUS_MODIFIED)};
		Cursor cursor = Email.getAppContext().getContentResolver()
				.query(Event.CONTENT_URI, projection, whereClause, whereArg, null);
		return cursor;

	}

	public static ArrayList<Event> getAllModifiedEvents() {
		ArrayList<Event> changes = new ArrayList<Event>();
		Cursor cursor;
		cursor = getModifiedEventsFromDatabase();

		if (cursor.moveToFirst()) {
			while (!cursor.isAfterLast()) {
				Event event = new Event();
				event._id = cursor.getLong(cursor.getColumnIndex("_id"));
				event.event_id = Integer.parseInt(cursor.getString(cursor
						.getColumnIndex(Event.EVENT_ID)));
				CalendarLog.d(CalendarConstants.Tag, "updated evebnts "
						+ event.event_id);
				event.calendarUID = cursor.getString(cursor
						.getColumnIndex(Event.CALENDAR_UID));

				event.allDay = cursor.getInt(cursor
						.getColumnIndex(Event.ALL_DAY));
				event.title = cursor.getString(cursor
						.getColumnIndex(Event.TITLE));
				event.startDate = cursor.getString(cursor
						.getColumnIndex(Event.START_DATE));
				event.endDate = cursor.getString(cursor
						.getColumnIndex(Event.END_DATE));
				event.dtstart = cursor.getLong(cursor
						.getColumnIndex(Event.DTSTART));
				event.eventTimezone = cursor.getString(cursor
						.getColumnIndex(Event.EVENT_TIMEZONE));
				event.dtend = cursor
						.getLong(cursor.getColumnIndex(Event.DTEND));
				event.description = cursor.getString(cursor
						.getColumnIndex(Event.DESCRIPTION));
				event.eventLocation = cursor.getString(cursor
						.getColumnIndex(Event.EVENT_LOCATION));
				event.organizer = cursor.getString(cursor
						.getColumnIndex(Event.ORGANIZER));
				event.rrule = cursor.getString(cursor
						.getColumnIndex(Event.RRULE));
				event.availability = cursor.getInt(cursor
						.getColumnIndex(Event.AVAILABILITY));
				event.accessibility = cursor.getInt(cursor
						.getColumnIndex(Event.ACCESSIBILITY));
				changes.add(event);
				cursor.moveToNext();
			}
		}
		cursor.close();

		return changes;
	}

	private static Cursor getDeletedEventsFromDatabase() {
		String projection[] = { "_id", Event.EVENT_ID };
		String whereClause = Event.STATUS + " = ?";
		String whereArg[] = {String.valueOf(Event.STATUS_DELETED)};
		Cursor cursor = Email.getAppContext().getContentResolver()
				.query(Event.CONTENT_URI, projection, whereClause, whereArg, null);
		return cursor;

	}

	public static ArrayList<String> getAllDeletedEvents() {
		ArrayList<String> changes = new ArrayList<String>();
		Cursor cursor;
		cursor = getDeletedEventsFromDatabase();
		if (cursor.moveToFirst()) {
			while (!cursor.isAfterLast()) {

				String event_id = cursor.getString(cursor
						.getColumnIndex(Event.EVENT_ID));
				CalendarLog.d(CalendarConstants.Tag, "del evebnts " + event_id);
				changes.add(event_id);
				cursor.moveToNext();
			}
		}
		cursor.close();

		return changes;
	}

	// @Override
	// public void taskComplete(int Task, boolean taskStatus, int statusCode,
	// int requestStatus, String errorString) {
	// /
	//
	// }

	public static byte[] getSyncKeyFromDatabase(String emailID,
			android.accounts.Account mAccountManagerAccount) {

		String[] DATA_PROJECTION = new String[] { SyncState.DATA, SyncState._ID };
		String SELECT_BY_ACCOUNT = SyncState.ACCOUNT_NAME + "=? AND "
				+ SyncState.ACCOUNT_TYPE + "=? order by " + SyncState._ID
				+ " DESC";

		Cursor cursor = Email
				.getAppContext()
				.getContentResolver()
				.query(SyncState.SYNCSTATE_CONTENT_URI,
						DATA_PROJECTION,
						SELECT_BY_ACCOUNT,
						new String[] { mAccountManagerAccount.name,
								mAccountManagerAccount.type }, null);

		try {
			if (cursor.moveToFirst()) {
				CalendarLog
						.d(CalendarConstants.Tag,
								"id is "
										+ cursor.getInt(cursor
												.getColumnIndexOrThrow(SyncState._ID))
										+ " "
										+ new String(
												cursor.getBlob(cursor
														.getColumnIndexOrThrow(SyncState.DATA))));
				return cursor.getBlob(cursor
						.getColumnIndexOrThrow(SyncState.DATA));
			}
		} finally {
			cursor.close();
		}
		return null;
	}

	public static void setSyncKeyToDatabase(android.accounts.Account account,
			byte[] data) {
		ContentValues values = new ContentValues();
		values.put(SyncState.DATA, data);
		values.put(SyncState.ACCOUNT_NAME, account.name);
		values.put(SyncState.ACCOUNT_TYPE, account.type);

		CalendarLog.d(CalendarConstants.Tag, "values inserted for sync"
				+ values.toString());

		Uri insertURi = Email.getAppContext().getContentResolver()
				.insert(SyncState.SYNCSTATE_CONTENT_URI, values);
		CalendarLog.d(CalendarConstants.Tag,
				"id inserted for sync" + insertURi.toString());

	}

	public static void updateSyncKeyInDatabase(
			android.accounts.Account account, byte[] data) {
		ContentValues values = new ContentValues();
		values.put(SyncState.DATA, data);
		values.put(SyncState.ACCOUNT_NAME, account.name);
		values.put(SyncState.ACCOUNT_TYPE, account.type);

		String where = SyncState.ACCOUNT_NAME + " = ? and "
				+ SyncState.ACCOUNT_TYPE + " = ?";
		String selectionArgs[] = new String[] { account.name, account.type };

		int numberOfRowsupdated = Email
				.getAppContext()
				.getContentResolver()
				.update(SyncState.SYNCSTATE_CONTENT_URI, values, where,
						selectionArgs);
		CalendarLog.d(CalendarConstants.Tag, "number of rows updated  for sync"
				+ numberOfRowsupdated);

	}

	/********************* Persona Helper ****************************/

	public static String getPersonaDateFromLong(long longDate) {
		Calendar cal = Email.getNewCalendar();
		cal.setTime(new Date(longDate));
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.US);

		return sdf.format(cal.getTime());

	}

	public static int getTimeZoneOffsetIntFromTimeZone(String timezoneString) {
		if(timezoneString==null)
		{
			Calendar cal=Calendar.getInstance();
			return Calendar.getInstance().getTimeZone().getOffset(cal.getTimeInMillis());
		}
		TimeZone timeZone = TimeZone.getTimeZone(timezoneString);
		Calendar calendar = GregorianCalendar.getInstance(timeZone);
		int offsetInMillis = timeZone.getOffset(calendar.getTimeInMillis());		
		return offsetInMillis;
	}

	private static Cursor getPersonaEventsByDate(long startTime) {
		try {
//			String[] days = date.split(" ");
			// Log.e("check time","check s"+days[0] + " -" +date);

			Calendar cal = Email.getNewCalendar();
			cal.setTimeInMillis(startTime);
			
			CalendarLog.d(CalendarConstants.tTag,"start time is "+cal.getTime().toString());
			
//			cal.setFirstDayOfWeek(Calendar.SUNDAY);
//			SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy",
//					Locale.US);
//			try {
//				cal.setTime(sdf.parse(date));
//			} catch (ParseException e) {
//				e.printStackTrace();
//			}// all done
//			cal.set(Calendar.DAY_OF_MONTH, Integer.valueOf(days[0]));
//			cal.set(Calendar.MONTH, cal.get(Calendar.MONTH));
//			cal.set(Calendar.YEAR, Integer.valueOf(days[2]));
			int dayoftheweek = cal.get(Calendar.DAY_OF_WEEK);

			String today =  String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
			String month = String.valueOf(cal.get(Calendar.MONTH) + 1);
			if (Integer.valueOf(today) < 10 && today.length() == 1) {
				today = "0" + today;
			}
			if (Integer.valueOf(month) < 10 && month.length() == 1) {
				month = "0" + month;       
			}
			int weekday = Integer.parseInt(today) / 7;
			weekday++;
			dayoftheweek = dayoftheweek - 1;
			String projection[] = { "_id", Event.TITLE, Event.DTSTART,
					Event.DTEND, Event.EVENT_TIMEZONE, Event.ALL_DAY,
					Event.EVENT_LOCATION,"strftime('%H', ("+Event.DTSTART+"+"+getTimeZoneOffsetIntFromTimeZone(getDefaultTimeZone())+")/ 1000, 'unixepoch')","(strftime('%M', ("+Event.DTSTART+"+"+getTimeZoneOffsetIntFromTimeZone(getDefaultTimeZone())+")/ 1000, 'unixepoch'))/60" };

			String eventDate = getDateString(getPersonaDateFromLong(startTime), 0, 0);
			long[] timeInterval = CalendarCommonFunction
					.convertDateToMillisec(eventDate);
			Calendar cal1=Calendar.getInstance();
			cal1.setTimeInMillis(timeInterval[1]);
			CalendarLog.d(CalendarConstants.tTag,"start time is "+cal1.getTime().toString());

			String whereClause="("+Event.DTSTART+" >= ? and "+
			Event.DTSTART+" < ? ) or ("+Event.DTEND +" > ? and "+Event.DTEND+" <= ?) and " + Event.STATUS+ " != ? order by strftime('%H', ("+Event.DTSTART+"+"+
			getTimeZoneOffsetIntFromTimeZone(getDefaultTimeZone())+")/ 1000,'unixepoch')+(strftime('%M', ("+
			Event.DTSTART+"+"+getTimeZoneOffsetIntFromTimeZone(getDefaultTimeZone())+")/ 1000,	'unixepoch')/60) ";
			
			String whereArg[] = {String.valueOf(startTime),String.valueOf(timeInterval[1]),
					String.valueOf(startTime),String.valueOf(timeInterval[1]),String.valueOf(Event.STATUS_DELETED)};			
			CalendarLog.d(CalendarConstants.tTag,
					" persona values whereClause"+whereClause);
			// CalendarLog.e("Reminder LIST", "RECEIVER" + "projection"
			// + projection + "whereClause" + whereClause + "whereArgs"
			// + whereArgs + "orderBy" + orderBy);

			Cursor cursor = Email
					.getAppContext()
					.getContentResolver()
					.query(Event.CONTENT_URI, projection, whereClause,
							whereArg, null);
			if (cursor != null) {
				// CalendarLog.e("Reminder LIST", "RECEIVER  cursor not null");
				if (cursor.moveToFirst())
					CalendarLog.d(
							CalendarConstants.tTag,
							"The number of rows returned " + ""
									+ cursor.getCount());
				cursor.moveToFirst();
				return cursor;
			} else {
				// CalendarLog.e("Reminder LIST", "RECEIVER  cursor  null");
				return null;
			}

		} catch (NumberFormatException e) {
			e.printStackTrace();
			return null;
		}}
public static ArrayList<ContentValues> getPersonaDayValues(long start_Time) {
		
//		CalendarLog.d(CalendarConstants.tTag,"get persona day values "+getDateFromLong(start_Time)+" "+getTimeFromLong(start_Time, getDefaultTimeZone()));
		
		Cursor cursor = getPersonaEventsByDate(start_Time);
		ArrayList<ContentValues> dayValues = new ArrayList<ContentValues>();
		if (cursor != null) {
			if (cursor.moveToFirst()) {

				// CalendarLog.d(CalendarConstants.Tag,"number of rows in cursor ",""+cursor.getCount());
				cursor.moveToFirst();

				while (!cursor.isAfterLast()) {
					String eventTitle = "";
					String eventLocation;
					String startTime;
					String endTime;
					String timeZone;
					int eventID = -1;
					int allDay = 0;

					ContentValues dayValue = new ContentValues();

					eventID = cursor.getInt(cursor.getColumnIndex("_id"));

					eventTitle = cursor.getString(cursor
							.getColumnIndex(Event.TITLE));
					eventLocation = cursor.getString(cursor
							.getColumnIndex(Event.EVENT_LOCATION));

					
					
					allDay = cursor
							.getInt(cursor.getColumnIndex(Event.ALL_DAY));
					timeZone = cursor.getString(cursor
							.getColumnIndex(Event.EVENT_TIMEZONE));
					CalendarLog.d(CalendarConstants.tTag,"time zone for persona "+getDefaultTimeZone());
					
					long startTimeLong=cursor.getLong(cursor
							.getColumnIndex(Event.DTSTART));
					startTime = getTimeFromLong(startTimeLong,
							getDefaultTimeZone());
					
//					if(startTimeLong<start_Time || )
//					{
//						cursor.moveToNext();
//						break;
//					}
				
					endTime = getTimeFromLong(
							cursor.getLong(cursor.getColumnIndex(Event.DTEND)),
							getDefaultTimeZone());
				
					dayValue.put("event", eventTitle);
					dayValue.put("start_time", startTime);
					dayValue.put("end_time", endTime);
					dayValue.put("location", eventLocation);
					dayValue.put("all_day", allDay);

					dayValues.add(dayValue);
					
					CalendarLog.d(CalendarConstants.tTag,
							" persona val "+eventTitle);
					
					
					CalendarLog.d(CalendarConstants.tTag,
							" persona values  "+cursor.getString(cursor.getColumnIndex("strftime('%H', ("+Event.DTSTART+"+"+getTimeZoneOffsetIntFromTimeZone(getDefaultTimeZone())+")/ 1000, 'unixepoch')"))+" - "+eventTitle + " ---" + cursor.getString(cursor.getColumnIndex("(strftime('%M', ("+Event.DTSTART+"+"+getTimeZoneOffsetIntFromTimeZone(getDefaultTimeZone())+")/ 1000, 'unixepoch'))/60")) + "-------- minutes");
					cursor.moveToNext();
				}

			}
		}
		if (cursor != null)
			cursor.close();

		return dayValues;
	}

	

	public static String getMessageIDsForEvent(int event_id) {
		String projection[] = { "_id", Event.MSG_ID };
		String whereClause = "_id = ?";
		String whereArg[] = {String.valueOf(event_id)};
		String msgID = "";
		Cursor cursor = Email.getAppContext().getContentResolver()
				.query(Event.CONTENT_URI, projection, whereClause, whereArg, null);
		if (cursor.moveToFirst())
			// CalendarLog.d(CalendarConstants.Tag,"The number of rows returned ",""+cursor.getCount());
			msgID = cursor.getString(cursor.getColumnIndex(Event.MSG_ID));
		CalendarLog.d(CalendarConstants.Tag, "msg IDss obtined are " + msgID);
		cursor.close();
		if (msgID != null)
			return msgID;
		else
			return "";

	}

	public static ArrayList<Event> getMessageIDs() {
		CalendarLog.d(CalendarConstants.Tag, "get msg ids called ");
		String projection[] = { Event.EVENT_ID, Event.MSG_ID };
		String whereClause = Event.MSG_ID + " != ''";
		ArrayList<Event> forwardedEvents = new ArrayList<Event>();
		Cursor cursor = Email.getAppContext().getContentResolver()
				.query(Event.CONTENT_URI, projection, whereClause, null, null);

		if (cursor.moveToFirst()) {

			CalendarLog.d(
					CalendarConstants.Tag,
					"number of rows in cursor getMessageIds"
							+ cursor.getCount());
			cursor.moveToFirst();

			while (!cursor.isAfterLast()) {
				Event event = new Event();
				event.event_id = cursor.getInt(cursor
						.getColumnIndex(Event.EVENT_ID));
				event.msgID = cursor.getString(cursor
						.getColumnIndex(Event.MSG_ID));
				CalendarLog.d(CalendarConstants.Tag, "msg IDss obtined are "
						+ event.msgID);
				forwardedEvents.add(event);
				cursor.moveToNext();
			}

		}
		cursor.close();
		return forwardedEvents;
	}

	public static void makeMsgIDEmpty(int event_id, int msgID) {
		CalendarLog.d(CalendarConstants.Tag, "makeMessageEmpty " + event_id
				+ " " + msgID);
		String where = "_id = ?";
		String whereArg[] = {String.valueOf(event_id)};
		ContentValues msgValues = new ContentValues();
		String msgIDs = "";

		ArrayList<String> msgList = new ArrayList<String>();
		Collections.addAll(msgList, getMessageIDsForEvent(event_id).split(";"));
		CalendarLog.d(CalendarConstants.Tag, "getMSG "
				+ getMessageIDsForEvent(event_id));
		msgList.remove("" + msgID);
		if (!msgIDs.isEmpty()) {
			for (String msg : msgList) {
				msgIDs += msgIDs + msg + ";";
			}
			CalendarLog.d(CalendarConstants.Tag, "getMSG changed 1" + msgIDs);
			msgIDs = msgIDs.substring(0, msgIDs.length() - 1);
		} else {
			msgIDs = "";
		}
		CalendarLog.d(CalendarConstants.Tag, "getMSG changed 2" + msgIDs);
		msgValues.put(Event.MSG_ID, msgIDs);

		Email.getAppContext().getContentResolver()
				.update(Event.CONTENT_URI, msgValues, where, whereArg);
	}

	public static void insertMessageID(int event_id, int msg_id) {

		String where = "_id = ?";
		String whereArg[] = {String.valueOf(event_id)};
		ContentValues msgValues = new ContentValues();

		String msgIDs = getMessageIDsForEvent(event_id);

		if (msgIDs.isEmpty()) {

			msgValues.put(Event.MSG_ID, "" + msg_id);
		} else {

			msgValues.put(Event.MSG_ID, msgIDs + ";" + msg_id);
		}
		if (Email.getAppContext().getContentResolver()
				.update(Event.CONTENT_URI, msgValues, where, whereArg) == 0) {

			Email.getAppContext().getContentResolver()
					.insert(Event.CONTENT_URI, msgValues);

		}
	}

	public static void forwardEvent(Event event) {
		Context context = Email.getAppContext();
		Intent intent = new Intent(context, CalendarEmMessageCompose.class);
		intent.putExtra("event_id", event._id);
		intent.putExtra("account_id", getAccount().mId);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);

	}

	public static ArrayList<ContentValues> getListOfAccountOwner() {
		ArrayList<ContentValues> mergedAccountOwnerList = new ArrayList<ContentValues>();
		ArrayList<ContentValues> list_owner = new ArrayList<ContentValues>();
		ArrayList<ContentValues> personalList = null;
		personalList = CalendarDBHelperClassPreICS.getListOfAccountOwner();

		ContentValues ownerContentValue = new ContentValues();
		ownerContentValue.put(CalendarConstants.PERSONAL_OWNER_ACC_NAME,
				CalendarDatabaseHelper.getEmail_ID());
		ownerContentValue
				.put(CalendarConstants.PERSONAL_OWNER_ACC_COLOR,
						Email.getAppContext().getResources()
								.getColor(R.color.event_bgcolor));
		list_owner.add(ownerContentValue);

		mergedAccountOwnerList.addAll(list_owner);
		if ((personalList.size() > 0) && (displayPersonalEvent())) {
			mergedAccountOwnerList.addAll(personalList);
		}

		HashSet hs = new HashSet();
		hs.addAll(mergedAccountOwnerList);
		mergedAccountOwnerList.clear();
		mergedAccountOwnerList.addAll(hs);
		// Log.e("Account owner list ",
		// "--->"+mergedAccountOwnerList.toString()+isDisplayPersonalEvent());

		return mergedAccountOwnerList;

	}

	/*********** Response type **********************/

	public static void changeResponseType(int _id, int status) {
		ContentValues values = new ContentValues();
		String where = "_id= ?";
		String whereArg[] = {String.valueOf(_id)};
		values.put(Event.RESPONSE_TYPE, status);

		Email.getAppContext().getContentResolver()
				.update(Event.CONTENT_URI, values, where, whereArg);
		changeStatusToModified(_id);
	}

	public static int getResponseTypeId(int responseType) {
		switch (responseType) {
		case Event.RESPONSE_TYPE_ACCEPTED:
			return R.id.response_yes;
		case Event.RESPONSE_TYPE_TENTATIVE:
			return R.id.response_maybe;
		case Event.RESPONSE_TYPE_REJECTED:
			return R.id.response_no;
		default:
			return -1;
		}
	}

	public static int getResponseType(int responseType) {
		switch (responseType) {
		case R.id.response_yes:
			return Event.RESPONSE_TYPE_ACCEPTED;
		case R.id.response_maybe:
			return Event.RESPONSE_TYPE_TENTATIVE;
		case R.id.response_no:
			return Event.RESPONSE_TYPE_REJECTED;
		default:
			return Event.RESPONSE_TYPE_NONE;
		}
	}

	/************* RECURSION ************/

	public static Recurrence getRecursionForEvent(int event_id) {
		Recurrence recurrence = new Recurrence();
		String projection[] = { Recurrence.TYPE, Recurrence.OCCURENCES,
				Recurrence.INTERVAL, Recurrence.DOW, Recurrence.DOW_STRING,
				Recurrence.DOM, Recurrence.WOM, Recurrence.MOY,
				Recurrence.UNTIL };
		String whereClause = "_id = ? and " + Event.STATUS
				+ " != ?" ;
		String whereArg[] = {String.valueOf(event_id),String.valueOf(Event.STATUS_DELETED)};
		String orderBy = Event.START_DATE;
		Cursor cursor = Email
				.getAppContext()
				.getContentResolver()
				.query(Event.CONTENT_URI, projection, whereClause, whereArg,
						orderBy);
		if (cursor.moveToFirst())
		// CalendarLog.d(CalendarConstants.Tag,"The number of rows returned ",""+cursor.getCount());

		{
			recurrence.type = cursor.getInt(cursor
					.getColumnIndex(Recurrence.TYPE));
			recurrence.occurences = cursor.getInt(cursor
					.getColumnIndex(Recurrence.OCCURENCES));
			recurrence.interval = cursor.getInt(cursor
					.getColumnIndex(Recurrence.INTERVAL));
			recurrence.dow = cursor.getInt(cursor
					.getColumnIndex(Recurrence.DOW));
			recurrence.dowString = cursor.getString(cursor
					.getColumnIndex(Recurrence.DOW_STRING));
			recurrence.dom = cursor.getInt(cursor
					.getColumnIndex(Recurrence.DOM));
			recurrence.wom = cursor.getInt(cursor
					.getColumnIndex(Recurrence.WOM));
			recurrence.moy = cursor.getInt(cursor
					.getColumnIndex(Recurrence.MOY));
			recurrence.until = cursor.getLong(cursor
					.getColumnIndex(Recurrence.UNTIL));

			CalendarLog.d(CalendarConstants.Tag, "-Recurrence.type"
					+ recurrence.type);
			CalendarLog.d(CalendarConstants.Tag, "recurrence.dom"
					+ recurrence.dom);
			CalendarLog.d(CalendarConstants.Tag, "recurrence.dow"
					+ recurrence.dow);
			CalendarLog.d(CalendarConstants.Tag, "recurrence.dowString"
					+ recurrence.dowString);
			CalendarLog.d(CalendarConstants.Tag, "recurrence.interval"
					+ recurrence.interval);
			CalendarLog.d(CalendarConstants.Tag, "recurrence.moy"
					+ recurrence.moy);
			CalendarLog.d(CalendarConstants.Tag, "recurrence.occurences"
					+ recurrence.occurences);
			CalendarLog.d(CalendarConstants.Tag, "recurrence.until"
					+ recurrence.until);
			CalendarLog.d(CalendarConstants.Tag, "-recurrence.wom"
					+ recurrence.wom);

		}
		cursor.close();
		return recurrence;
	}

	public static String getFolderIdForCalendar() {
		String folderId = "";
		try {
			Cursor c = Email
					.getAppContext()
					.getContentResolver()
					.query(EmEmailContent.Mailbox.CONTENT_URI, null,
							EmEmailContent.Mailbox.DISPLAY_NAME + "=?",
							new String[] { "Calendar" }, null);

			if (c != null && c.getCount() > 0) {
				c.moveToFirst();

				folderId = c.getString(c.getColumnIndex("serverId"));

			}
			if (c != null) {
				c.close();
			}
		} catch (Exception e) {
			CalendarLog.e(CalendarConstants.Tag,
					"Excepiton while getting the folder id " + e.toString());

		}
		return folderId;

	}

	public static String getEmail_ID() {
		String emailID = "";
		/*ExternalEmailSettingsInfo extnEmailSettInfo;
		ExternalAdapterRegistrationClass mExtAdapReg;*/
		try {
			Account mEmailAccount = getAccount();
			emailID=mEmailAccount.mEmailAddress;
			/*mExtAdapReg = ExternalAdapterRegistrationClass.getInstance(Email.getAppContext());
			extnEmailSettInfo = mExtAdapReg.getExternalEmailSettingsInfo();
			emailID = extnEmailSettInfo.EmailAddress;*/
			/*Cursor c = Email
					.getAppContext()
					.getContentResolver()
					.query(EmEmailContent.Account.CONTENT_URI,
							new String[] { EmEmailContent.Account.EMAIL_ADDRESS },
							null, null, null);

			if (c != null && c.getCount() > 0) {
				c.moveToFirst();

				emailID = c.getString(c
						.getColumnIndex(EmEmailContent.Account.EMAIL_ADDRESS));
			}
			if (c != null) {
				c.close();
			}*/
		} catch (Exception e) {
			CalendarLog.d(
					CalendarConstants.Tag,
					"Excepiton while getting the account email id "
							+ e.toString());

		}
		return emailID;

	}

	public static Account getAccount() {
		String emailID = "";
		long id = 0;
		try {
			Cursor c = Email
					.getAppContext()
					.getContentResolver()
					.query(Account.CONTENT_URI,
							new String[] { Account.EMAIL_ADDRESS, Account.ID },
							null, null, null);

			if (c != null && c.getCount() > 0) {
				c.moveToFirst();

				emailID = c.getString(c.getColumnIndex(Account.EMAIL_ADDRESS));
				id = c.getLong(c.getColumnIndex(Account.ID));
			}
			if (c != null) {
				c.close();
			}
		} catch (Exception e) {
			CalendarLog.d(
					CalendarConstants.Tag,
					"Excepiton while getting the account email id "
							+ e.toString());

		}
		Account account = new Account();
		account.mEmailAddress = emailID;
		account.mId = id;
		return account;

	}

	public static void manualSync() {
		try {
			Cursor c = Email
					.getAppContext()
					.getContentResolver()
					.query(EmEmailContent.Mailbox.CONTENT_URI, null,
							EmEmailContent.Mailbox.DISPLAY_NAME + "=?",
							new String[] { "Calendar" }, null);

			if (c != null && c.getCount() > 0) {
				c.moveToFirst();

				String Accname = c.getString(c
						.getColumnIndex(EmEmailContent.Mailbox.ACCOUNT_KEY));

				String folderId = c.getString(c
						.getColumnIndex(Mailbox.RECORD_ID));

				EmEmController controller = EmEmController.getInstance(Email
						.getAppContext());

				controller.updateMailbox(Long.parseLong(Accname),
						Long.parseLong(folderId), null);

			}
			if (c != null) {
				c.close();
			}
		} catch (Exception e) {
			CalendarLog.e(CalendarConstants.Tag,
					"Exception while manual syncing");

		}
	}


	public static void sendMailForAttendees(Event event,boolean isException) {

		Account account = CalendarDatabaseHelper.getAccount();
		CalendarLog.d(CalendarConstants.Tag, "acc email addr "
				+ account.mEmailAddress + account.mId);

		EmEmailContent.Message msg = null;
		try {
			msg = EmCalendarUtilities.createMessageForEvent(event, account,
					Message.FLAG_OUTGOING_MEETING_INVITE,isException);
			// msg=EmailCalendarUtilities.createMessageForEventId(mContext,
			// eventId, Message.FLAG_OUTGOING_MEETING_INVITE, null, mAccount);
		} catch (Exception e) {
			// Nothing to do here; the Event may no longer exist
			CalendarLog.d(CalendarConstants.Tag, "Excpeiotn while sending mail"
					+ e.toString());
		}

		if (msg != null) {
			EmEasOutboxService.sendMessage(Email.getAppContext(),
					account.mId, msg);

		}

	}
	
	public static TimeZone getTimeZoneFromString(String eventTimezoneString)
	{
		TimeZone eventTimeZone;
		int index=eventTimezoneString.indexOf(" ") - 1;
		if(index>1)
			eventTimeZone = TimeZone.getTimeZone(eventTimezoneString
				.substring(1,index));
		else
			eventTimeZone=TimeZone.getTimeZone(eventTimezoneString);
		
		if(eventTimeZone.getID().equalsIgnoreCase("GMT"))
			eventTimeZone=TimeZone.getDefault();
		
		return eventTimeZone;
	}
	
	public static String getTimeZoneOffsetFromString(String eventTimezoneString)
	{

		int index=eventTimezoneString.indexOf(" ") - 1;
		if(index>1)
		{
			return "("+eventTimezoneString
				.substring(1,index)+")";
		}
		else
		{
		TimeZone timeZone=TimeZone.getTimeZone(eventTimezoneString);
		
		Calendar calendar = GregorianCalendar.getInstance(timeZone);
		int offsetInMillis = timeZone.getOffset(calendar.getTimeInMillis());

		String offset = String.format("%02d:%02d",
				Math.abs(offsetInMillis / 3600000),
				Math.abs((offsetInMillis / 60000) % 60));
		offset = (offsetInMillis >= 0 ? "+" : "-") + offset;

		String homeZone = "(" + "GMT" + offset + ")";
		return homeZone;	
		}
			

	}
	
	public static int getTimeZoneOffsetIndex(String timeZoneOffset)
	{
		String[] timeZones = Email.getAppContext().getResources().getStringArray(
				R.array.preference_home_time_zone_label);
		int index = 0,selectedVlaue = 0;
		for (String timeZone : timeZones) {

			if (timeZoneOffset
					.contains(
							CalendarDatabaseHelper
									.getTimeZoneOffsetFromString(timeZone))) {
				selectedVlaue = index;
				break;
			}
			index++;
		}
		return selectedVlaue;
	}
	
	public static String getDefaultTimeZone()
	{
//	CalendarLog.e("CAlendar","getDefsultime"+Email.getNewZone());
	int index=getTimeZoneOffsetIndex("("+Email.getNewZone()+")");
	String[] timeZones = Email.getAppContext().getResources().getStringArray(
			R.array.preference_home_time_zone_label);
//	CalendarLog.d(CalendarConstants.Tag,"aish index "+index+" "+timeZones[index]);
		return timeZones[index];
	}
	
	public static ArrayList<Reminder> convertReminderContentValuesToReminder(
			ArrayList<ContentValues> reminders) {
		ArrayList<Reminder> reminderList = new ArrayList<Reminder>();
		for (ContentValues reminderValue : reminders) {
			Reminder reminder = new Reminder();
			reminder.event_id = reminderValue.getAsInteger(Reminder.EVENT_ID);
			reminder.minutes = reminderValue.getAsInteger(Reminder.MINUTES);
			reminder.method = reminderValue.getAsInteger(Reminder.METHOD);
			reminderList.add(reminder);
		}
		return reminderList;
	}

	public static ArrayList<Integer> getReminderMinutesFromReminders(
			ArrayList<Reminder> reminders) {
		ArrayList<Integer> minutes = new ArrayList<Integer>(reminders.size());
		for (Reminder reminder : reminders) {
			minutes.add(reminder.minutes);
		}
		return minutes;

	}

	public static boolean isEqualEvent(Event event1, Event event2) {
		boolean isEventSame = true;

		CalendarLog.d(CalendarConstants.Tag, "compare two conten values "
				+ getEventContentValues(event1) + "\n"
				+ getEventContentValues(event2));

		if (event1._id != event2._id) {
			return false;
		}

		if (event1.calendarUID != null && !event1.calendarUID.isEmpty()) {
			if (!event1.calendarUID.equalsIgnoreCase(event2.calendarUID)) {
				return false;
			}
		} else {
			if (event2.calendarUID != null && !event2.calendarUID.isEmpty())
				return false;
		}

		if (event1.event_id != event2.event_id) {
			return false;
		} // The _ID of the calendar the event belongs to.

		if (event1.organizer != null && !event1.organizer.isEmpty()) {
			if (!event1.organizer.equalsIgnoreCase(event2.organizer)) {
				return false;
			}
		} else {
			if (event2.organizer != null && !event2.organizer.isEmpty())
				return false;
		}

		if (event1.title != null && !event1.title.isEmpty()) {
			if (!event1.title.equalsIgnoreCase(event2.title)) {
				return false;
			}
		} else {
			if (event2.title != null && !event2.title.isEmpty())
				return false;
		}

		if (event1.eventLocation != null && !event1.eventLocation.isEmpty()) {
			if (!event1.eventLocation.equalsIgnoreCase(event2.eventLocation)) {
				return false;
			}
		} else {
			if (event2.eventLocation != null && !event2.eventLocation.isEmpty())
				return false;
		}

		if (event1.description != null && !event1.description.isEmpty()) {
			if (!event1.description.equalsIgnoreCase(event2.description)) {
				return false;
			}
		} else {
			if (event2.description != null && !event2.description.isEmpty())
				return false;
		}

		if (event1.startDate != null && !event1.startDate.isEmpty()) {
			if (!event1.startDate.equalsIgnoreCase(event2.startDate)) {
				return false;
			}
		} else {
			if (event2.startDate != null && !event2.startDate.isEmpty())
				return false;
		}

		if (event1.endDate != null && !event1.endDate.isEmpty()) {
			if (!event1.endDate.equalsIgnoreCase(event2.endDate)) {
				return false;
			}
		} else {
			if (event2.endDate != null && !event2.endDate.isEmpty())
				return false;
		}

		if (event1.dtstart != event2.dtstart) {
			return false;
		}

		if (event1.dtend != event2.dtend) {
			return false;
		}

		if (event1.eventTimezone != null && !event1.eventTimezone.isEmpty()) {
			if (!event1.eventTimezone.equalsIgnoreCase(event2.eventTimezone)) {
				if (!getTimeZoneOffsetFromString(event1.eventTimezone)
						.equalsIgnoreCase(
								getTimeZoneOffsetFromString(event2.eventTimezone))) {
					return false;
				}

			}
		} else {
			if (event2.eventTimezone != null && !event2.eventTimezone.isEmpty())
				return false;
		}

		if (event1.responseType != event2.responseType) {
			return false;
		}

		if (event1.allDay != event2.allDay) {
			return false;
		}

		if (event1.rrule != null) {
			if (!event1.rrule.equalsIgnoreCase(event2.rrule)) {
				return false;
			}
		} else if (event2.rrule != null) {
			if (!event2.rrule.equalsIgnoreCase(event1.rrule)) {
				return false;
			}
		}

		if (event1.availability != event2.availability) {
			return false;
		}
		if (event1.accessibility != event2.accessibility) {
			return false;
		}
		CalendarLog.d("isEqualEvent", "1 " + isEventSame);

		CalendarLog.d("isEqualEvent", "step att ");

		if (event2.attendees != null && !event2.attendees.isEmpty()) {
			int index = 0;
			Iterator<Attendee> attendeeIterator = event2.attendees.iterator();
			while (attendeeIterator.hasNext()) {
				Attendee attendee = attendeeIterator.next();
				CalendarLog.d(CalendarConstants.Tag, "attendee1 : "
						+ getAttendeesContentValues(attendee).toString());
				if (attendee.attendeeEmail.equalsIgnoreCase(getEmail_ID())) {
					attendeeIterator.remove();
					CalendarLog.d("isEqualEvent", "2");
					break;
				}
			}
			attendeeIterator = event2.attendees.iterator();
			while (attendeeIterator.hasNext()) {
				Attendee attendee = attendeeIterator.next();
				while (attendeeIterator.hasNext()) {
					Attendee attendee1 = attendeeIterator.next();
					CalendarLog.d("isEqualEvent", "step att 2 "
							+ attendee1.attendeeEmail);
					if (attendee.attendeeEmail
							.equalsIgnoreCase(attendee1.attendeeEmail)) {
						if (index != event2.attendees.indexOf(attendee1)) {
							CalendarLog.d("isEqualEvent", "4 " + index + " "
									+ event2.attendees.indexOf(attendee1));
							attendeeIterator.remove();
							CalendarLog.d("isEqualEvent", "size "
									+ event2.attendees.size());

						}
					}
				}
				index++;
			}
			CalendarLog.d("isEqualEvent", "step att 2 ");
			if (event1.attendees == null && !event1.attendees.isEmpty()) {
				CalendarLog.d("isEqualEvent", "2 ");
				return false;

			} else {
				index = 0;
				for (Attendee attendee : event2.attendees) {
					boolean isExists = false;
					for (Attendee attendee2 : event1.attendees) {
						if (attendee.attendeeEmail
								.equalsIgnoreCase(attendee2.attendeeEmail)) {
							isExists = true;
							CalendarLog.d("isEqualEvent", "5 ");
							break;
						}
					}
					if (!isExists) {
						CalendarLog.d("isEqualEvent", "6 ");
						return false;

					}

					index++;

				}

				if (event1.attendees.size() != event2.attendees.size()) {
					CalendarLog.d("isEqualEvent",
							"7 " + event1.attendees.size() + " "
									+ event2.attendees.size());
					return false;

				}
			}

		} else {
			if (event1.attendees != null && !event1.attendees.isEmpty()) {
				CalendarLog.d("isEqualEvent", "8");
				return false;

			} else {
				isEventSame = true;
				CalendarLog.d("isEqualEvent", "9");
			}
		}

		CalendarLog.d("isEqualEvent", "step rec ");

		if (event1.recurrence != null
				&& !event1.recurrence.isEqual(new Recurrence())) {
			if (event2.recurrence != null) {
				if (!event1.recurrence.isEqual(event2.recurrence)) {
					CalendarLog.d("isEqualEvent", "10 ");
					return false;

				}
			} else {
				CalendarLog.d("isEqualEvent", "11 ");
				return false;

			}
		} else {
			if (event2.recurrence != null
					&& !event2.recurrence.isEqual(new Recurrence())) {
				CalendarLog.d("isEqualEvent", "12 ");
				return false;

			} else {
				CalendarLog.d("isEqualEvent", "13 ");
				isEventSame = true;

			}
		}
		CalendarLog.d("isEqualEvent", "step rem ");
		if (event1.reminders != null && !event1.reminders.isEmpty()) {
			if (event2.reminders != null && !event2.reminders.isEmpty()) {
				if (event1.reminders.size() == event2.reminders.size()) {
					ArrayList<Integer> reminderList1 = getReminderMinutesFromReminders(event1.reminders);
					ArrayList<Integer> reminderList2 = getReminderMinutesFromReminders(event2.reminders);
					for (int minute : reminderList1) {
						if (Collections.frequency(reminderList1, minute) != Collections
								.frequency(reminderList2, minute)) {
							CalendarLog.d("isEqualEvent", "14 ");
							return false;

						}
					}
				} else {
					CalendarLog.d("isEqualEvent", "15 ");
					return false;

				}
			} else {
				CalendarLog.d("isEqualEvent", "16 ");
				return false;

			}
		} else {
			if (event2.reminders != null && !event2.reminders.isEmpty()) {
				CalendarLog.d("isEqualEvent", "17 ");
				return false;

			} else {
				CalendarLog.d("isEqualEvent", "18 ");
				isEventSame = true;
			}
		}

		CalendarLog.d("isEqualEvent", "exception equal");
		if (event1.exceptions != null && !event1.exceptions.isEmpty()) {
			int index = 0;
			if (event2.exceptions != null && !event2.exceptions.isEmpty()) {
				while (index < event1.exceptions.size()) {
					if (!event1.exceptions.get(index).isEqual(
							event2.exceptions.get(index))) {
						CalendarLog
								.d("isEqual",
										"exceptions not equal "
												+ getExceptionContentValues(event1.exceptions
														.get(index))
												+ " "
												+ getExceptionContentValues(event2.exceptions
														.get(index)));
						return false;
					}
				}
			} else {
				return false;
			}
		} else {
			if (event2.exceptions != null && !event2.exceptions.isEmpty()) {
				return false;
			} else
				return true;
		}

		return isEventSame;
	}
}
