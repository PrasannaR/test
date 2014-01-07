package com.cognizant.trumobi.calendar.provider;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Color;
import android.provider.CalendarContract.Events;
import android.util.Log;
import com.cognizant.trumobi.calendar.modal.Event;
import com.cognizant.trumobi.calendar.modal.Recurrence;
import com.cognizant.trumobi.calendar.util.CalendarCommonFunction;
import com.cognizant.trumobi.calendar.util.CalendarConstants;
import com.cognizant.trumobi.em.Email;

public class CalendarDBHelperClassPreICS {

	private static Cursor getPersonalAllDayEventsByDate(String date) {

		String projection[] = { "calendar_id", Event.TITLE, Event.DTSTART,
				Event.DTEND, Event.ALL_DAY, "_id" };
		String selection = "((dtstart >= "
				+ CalendarCommonFunction
						.convertDateToMillisec(CalendarDatabaseHelper
								.getDateString(date, 0, 0))[0]
				+ ") AND (dtstart <= "
				+ CalendarCommonFunction
						.convertDateToMillisec(CalendarDatabaseHelper
								.getDateString(date, 1, 0))[0]
				+ ") AND (allDay =" + 1 + ") OR ((dtstart" + "-"
				+ "dtend) > 86400000))";

		String orderBy = Event.DTSTART;
		Cursor cursor = Email
				.getAppContext()
				.getContentResolver()
				.query(Email.getCALENDAR_URI(), projection, selection, null,
						orderBy);
		if (cursor != null)
			if (cursor.moveToFirst())
				cursor.moveToFirst();
		return cursor;
	}

	public static ArrayList<ContentValues> getPersonalAllDayListValues(
			String date) {
		Cursor cursor = getPersonalAllDayEventsByDate(date);
		ArrayList<ContentValues> dayValues = new ArrayList<ContentValues>();
		if (cursor != null) {
			if (cursor.moveToFirst()) {

				cursor.moveToFirst();

				while (!cursor.isAfterLast()) {
					String eventTitle = "";
					String eventEndDate = "";
					String eventStartDate = "";
					int eventID = -1;
					int allDay = 0;

					ContentValues dayValue = new ContentValues();

					eventID = cursor.getInt(cursor
							.getColumnIndex(CalendarConstants._ID));

					eventTitle = cursor.getString(cursor
							.getColumnIndex(Event.TITLE));
					eventEndDate = CalendarCommonFunction
							.convertMilliSecondsToDate(cursor.getLong(cursor
									.getColumnIndex(Event.DTEND)));
					eventStartDate = CalendarCommonFunction
							.convertMilliSecondsToDate(cursor.getLong(cursor
									.getColumnIndex(Event.DTSTART)));
					allDay = cursor
							.getInt(cursor.getColumnIndex(Event.ALL_DAY));

					dayValue.put(CalendarConstants.EVENT, eventTitle);
					dayValue.put(CalendarConstants.START_DATE, eventStartDate);
					dayValue.put(CalendarConstants.END_DATE, eventEndDate);
					dayValue.put(CalendarConstants._ID, eventID);
					dayValue.put(CalendarConstants.ALL_DAY, allDay);
					dayValue.put(CalendarConstants.CALENDAR_TYPE_KEY,
							CalendarConstants.CALENDAR_TYPE_PERSONAL);

					dayValues.add(dayValue);
					cursor.moveToNext();
				}

			}
			cursor.close();
		}

		return dayValues;
	}

	private static Cursor getEvent(long event_id) {

		String projection[] = { "calendar_id", Event.TITLE, Event.DTSTART,
				Event.DTEND, Event.ALL_DAY, Event.DESCRIPTION,
				Event.EVENT_LOCATION, Event.ORGANIZER, Event.RRULE, "_id" };
		String whereClause = "_id = " + event_id;

		String orderBy = Event.DTSTART;
		Cursor cursor = Email
				.getAppContext()
				.getContentResolver()
				.query(Email.getCALENDAR_URI(), projection, whereClause, null,
						orderBy);
		if (cursor.moveToFirst())
			cursor.moveToFirst();

		return cursor;
	}

	public static Event getEventDetails(long event_id) {
		Event event = new Event();
		Cursor cursor = getEvent(event_id);
		if (cursor.moveToFirst()) {
			event._id = cursor.getLong(cursor
					.getColumnIndex(CalendarConstants._ID));
			event.event_id = Integer.parseInt(cursor.getString(cursor
					.getColumnIndex(CalendarConstants._ID)));
			event.allDay = cursor.getInt(cursor.getColumnIndex(Event.ALL_DAY));
			event.title = cursor.getString(cursor.getColumnIndex(Event.TITLE));
			event.startDate = CalendarCommonFunction
					.convertMilliSecondsToDate(cursor.getLong(cursor
							.getColumnIndex(Event.DTSTART)));
			event.endDate = CalendarCommonFunction
					.convertMilliSecondsToDate(cursor.getLong(cursor
							.getColumnIndex(Event.DTEND)));
			event.dtstart = cursor
					.getLong(cursor.getColumnIndex(Event.DTSTART));
			event.dtend = cursor.getLong(cursor.getColumnIndex(Event.DTEND));
			event.description = cursor.getString(cursor
					.getColumnIndex(Event.DESCRIPTION));
			event.eventLocation = cursor.getString(cursor
					.getColumnIndex(Event.EVENT_LOCATION));
			event.organizer = cursor.getString(cursor
					.getColumnIndex(Event.ORGANIZER));
			event.rrule = cursor.getString(cursor.getColumnIndex(Event.RRULE));

		}
		cursor.close();
		return event;

	}

	public static ContentValues getPersonalEventContentValues(Event event) {
		ContentValues values = new ContentValues();
		// values.put("_id",event.event_id);
		if (event._id != 0) {
			values.put("_id", event._id);
		}

		if (event.event_id == 0) {
			Cursor cursor = Email
					.getAppContext()
					.getContentResolver()
					.query(Email.getCALENDAR_URI(),
							new String[] { "MAX(_id) AS _id" }, null, null,
							null);
			if (cursor.moveToFirst()) {
				int index = cursor.getInt(cursor.getColumnIndex("_id"));
				values.put(Event.EVENT_ID, index + 1);
			} else
				values.put(Event.EVENT_ID, 1);
			cursor.close();

		} else
			values.put(Event.EVENT_ID, event.event_id);
		// values.put(Event.STATUS, event.status);
		values.put(Event.ORGANIZER, event.organizer);
		values.put(Event.TITLE, event.title);
		values.put(Event.EVENT_LOCATION, event.eventLocation);
		values.put(Event.DESCRIPTION, event.description);
		values.put(Event.START_DATE, event.startDate);
		values.put(Event.END_DATE, event.endDate);
		values.put(Event.DTSTART, event.dtstart);
		values.put(Event.DTEND, event.dtend);
		values.put(Event.EVENT_TIMEZONE, event.eventTimezone);
		values.put(Event.EVENT_END_TIMEZONE, event.eventEndTimezone);
		values.put(Event.ALL_DAY, event.allDay);
		values.put(Event.RRULE, event.rrule);
		values.put(Event.RDATE, event.rdate);
		values.put(Event.EXRULE, event.exrule);
		values.put(Event.EXDATE, event.exdate);
		// values.put(Event.EVENT_COLOR,event.bg_color);
		values.put(Event.AVAILABILITY, event.availability);


		return values;
	}

	public static ArrayList<ArrayList<ContentValues>> getSearchPersonalEventList(
			String searchQuery) {

		ArrayList<ArrayList<ContentValues>> listByDate = new ArrayList<ArrayList<ContentValues>>();
		String fromDate;
		Cursor cursor = getPesonalEventSearchResult(searchQuery);
		if (cursor != null) {
			cursor.moveToFirst();

			while (!cursor.isAfterLast()) {
				ArrayList<ContentValues> dateList = new ArrayList<ContentValues>();
				String temp = CalendarCommonFunction
						.convertMilliSecondsToDate(cursor.getLong(cursor
								.getColumnIndex(Event.DTSTART)));
				for (fromDate = temp; !cursor.isAfterLast()
						&& (fromDate.equalsIgnoreCase(temp));) {
					ContentValues dateValue = new ContentValues();

					dateValue.put(CalendarConstants._ID, cursor.getInt(cursor
							.getColumnIndex(CalendarConstants._ID)));
					dateValue.put(Event.TITLE, cursor.getString(cursor
							.getColumnIndex(Event.TITLE)));
					// dateValue.put(Event.EVENT_ID,
					// cursor.getString(cursor.getColumnIndex(Event.EVENT_ID)));
					dateValue.put(Event.DTSTART, cursor.getLong(cursor
							.getColumnIndex(Event.DTSTART)));
					dateValue.put(Event.START_DATE, temp);
					dateValue.put(Event.DTEND,
							cursor.getLong(cursor.getColumnIndex(Event.DTEND)));
					dateValue.put(CalendarConstants.ALL_DAY, cursor
							.getLong(cursor.getColumnIndex(Event.ALL_DAY)));
					dateValue.put(CalendarConstants.CALENDAR_TYPE_KEY,
							CalendarConstants.CALENDAR_TYPE_PERSONAL);
					// dateValue.put("bg_color",cursor.getLong(cursor.getColumnIndex(Event.EVENT_COLOR)));
					dateList.add(dateValue);
					cursor.moveToNext();
					if (!cursor.isAfterLast()) {
						temp = fromDate;
						fromDate = CalendarCommonFunction
								.convertMilliSecondsToDate(cursor
										.getLong(cursor
												.getColumnIndex(Event.DTSTART)));

					}
				}
				// §Collections.sort(dateList);
				listByDate.add(dateList);
			}
			cursor.close();
		}
		return listByDate;
	}

	public static Cursor getPesonalEventSearchResult(String searchQuery) {
		String projection[] = { "calendar_id", Event.TITLE,
				Event.EVENT_LOCATION, Event.DTSTART, Event.DTEND,
				Event.ALL_DAY, "_id" };
		String selection = Event.TITLE + " LIKE ? OR " + Event.EVENT_LOCATION
				+ " LIKE ?";
		String[] selectionArgs = new String[] { "%" + searchQuery + "%" };

		String orderBy = Event.DTSTART;
		Cursor cursor = Email
				.getAppContext()
				.getContentResolver()
				.query(Email.getCALENDAR_URI(), projection, selection,
						selectionArgs, orderBy);

		return cursor;
	}

	public static ArrayList<ArrayList<ContentValues>> getPersonalAgendaEventList(
			String fromDate, String toDate) {

		ArrayList<ArrayList<ContentValues>> listByDate = new ArrayList<ArrayList<ContentValues>>();
		Cursor cursor = getAllPersonalAgendaEventsByMonthRange(fromDate, toDate);

		if (cursor != null) {
			cursor.moveToFirst();

			while (!cursor.isAfterLast()) {
				ArrayList<ContentValues> dateList = new ArrayList<ContentValues>();
				String temp = CalendarCommonFunction
						.convertMilliSecondsToDate(cursor.getLong(cursor
								.getColumnIndex(Event.DTSTART)));

				for (fromDate = temp; !cursor.isAfterLast()
						&& (fromDate.equalsIgnoreCase(temp));) {
					ContentValues dateValue = new ContentValues();
					dateValue.put(CalendarConstants._ID, cursor.getInt(cursor
							.getColumnIndex(CalendarConstants._ID)));
					dateValue.put(Event.TITLE, cursor.getString(cursor
							.getColumnIndex(Event.TITLE)));
					// dateValue.put(Event.EVENT_ID,
					// cursor.getString(cursor.getColumnIndex(Event.EVENT_ID)));
					dateValue.put(Event.DTSTART, cursor.getLong(cursor
							.getColumnIndex(Event.DTSTART)));
					dateValue.put(Event.START_DATE, temp);
					dateValue.put(Event.DTEND,
							cursor.getLong(cursor.getColumnIndex(Event.DTEND)));
					dateValue.put(Event.EVENT_TIMEZONE,
							cursor.getLong(cursor.getColumnIndex(Events.EVENT_TIMEZONE)));
					dateValue.put(CalendarConstants.ALL_DAY, cursor
							.getLong(cursor.getColumnIndex(Event.ALL_DAY)));
					dateValue.put(CalendarConstants.CALENDAR_TYPE_KEY,
							CalendarConstants.CALENDAR_TYPE_PERSONAL);
					dateValue.put(Recurrence.TYPE, "-1");
					dateValue.put(Recurrence.OCCURENCES, "-1");
					dateValue.put(Recurrence.INTERVAL, "-1");
					dateValue.put(Recurrence.DOW, "-1");
					dateValue.put(Recurrence.DOW_STRING, "");
					dateValue.put(Recurrence.DOM, "-1");
					dateValue.put(Recurrence.WOM, "-1");
					dateValue.put(Recurrence.MOY, "-1");
					dateValue.put(Recurrence.UNTIL, "-1");
					// dateValue.put("bg_color",cursor.getString(cursor.getColumnIndex(Event.EVENT_COLOR)));
					dateList.add(dateValue);
					cursor.moveToNext();
					if (!cursor.isAfterLast()) {
						temp = fromDate;
						fromDate = CalendarCommonFunction
								.convertMilliSecondsToDate(cursor
										.getLong(cursor
												.getColumnIndex(Event.DTSTART)));

					}

				}
				listByDate.add(dateList);

			}
			cursor.close();
		}

		return listByDate;
	}

	private static Cursor getAllPersonalAgendaEventsByMonthRange(
			String fromDate, String toDate) {

		String projection[] = { "calendar_id", Event.DTSTART, Event.DTEND,
				Event.TITLE, Event.ALL_DAY, "_id", Event.RRULE,Event.EVENT_TIMEZONE };
		String whereClause = Event.DTSTART
				+ " BETWEEN "
				+ CalendarCommonFunction
						.convertDateToMillisec(CalendarDatabaseHelper
								.getDateString(fromDate, 0, 0))[0]
				+ " AND "
				+ CalendarCommonFunction
						.convertDateToMillisec(CalendarDatabaseHelper
								.getDateString(toDate, 0, 0))[0];
		String orderBy = Event.DTSTART;
		Cursor cursor = Email
				.getAppContext()
				.getContentResolver()
				.query(Email.getCALENDAR_URI(), projection, whereClause, null,
						orderBy);
		if (cursor != null)
			if (cursor.moveToFirst())

				cursor.moveToFirst();
		return cursor;

	}

	public static ArrayList<ArrayList<ContentValues>> getPersonalMonthEventList(
			String date) {

		ArrayList<ArrayList<ContentValues>> listByMonth = new ArrayList<ArrayList<ContentValues>>();
		Cursor cursor = getAllEventsByMonth(date, 1);

		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			ArrayList<ContentValues> dateList = new ArrayList<ContentValues>();
			String temp = CalendarCommonFunction
					.convertMilliSecondsToDate(cursor.getLong(cursor
							.getColumnIndex(Event.DTSTART)));
			for (date = temp; !cursor.isAfterLast()
					&& (date.equalsIgnoreCase(temp));) {
				ContentValues dateValue = new ContentValues();
				dateValue.put(CalendarConstants._ID, cursor.getInt(cursor
						.getColumnIndex(CalendarConstants._ID)));
				dateValue.put(Event.TITLE,
						cursor.getString(cursor.getColumnIndex(Event.TITLE)));
				dateValue.put(Event.DTSTART,
						cursor.getLong(cursor.getColumnIndex(Event.DTSTART)));
				dateValue.put(Event.START_DATE, temp);
				dateValue.put(Event.DTEND,
						cursor.getLong(cursor.getColumnIndex(Event.DTEND)));
				dateValue.put(Event.RRULE,
						cursor.getString(cursor.getColumnIndex(Event.RRULE)));
				dateValue.put(CalendarConstants.ALL_DAY,
						cursor.getLong(cursor.getColumnIndex(Event.ALL_DAY)));
				dateValue.put(CalendarConstants.CALENDAR_TYPE_KEY,
						CalendarConstants.CALENDAR_TYPE_PERSONAL);
				// dateValue.put("bg_color",cursor.getString(cursor.getColumnIndex(Event.EVENT_COLOR)));
				dateList.add(dateValue);
				cursor.moveToNext();
				if (!cursor.isAfterLast()) {
					temp = date;
					date = cursor.getString(cursor
							.getColumnIndex(Event.DTSTART));

				}
			}
			listByMonth.add(dateList);

		}
		cursor.close();

		return listByMonth;
	}

	private static Cursor getAllEventsByMonth(String date, int difference) {

		String projection[] = { "calendar_id", Event.TITLE, Event.DTSTART,
				Event.DTEND, Event.ALL_DAY, "_id", Event.RRULE };
		String whereClause = Event.DTSTART
				+ " BETWEEN "
				+ CalendarCommonFunction
						.convertDateToMillisec(CalendarDatabaseHelper
								.getDateString(date, 0, 0))[0]
				+ " AND "
				+ CalendarCommonFunction
						.convertDateToMillisec(CalendarDatabaseHelper
								.getDateString(date, 0, difference))[0];
		String orderBy = Event.DTSTART;
		Cursor cursor = Email
				.getAppContext()
				.getContentResolver()
				.query(Email.getCALENDAR_URI(), projection, whereClause, null,
						orderBy);

		if (cursor.moveToFirst())
			cursor.moveToFirst();
		return cursor;

	}

	public static ArrayList<ContentValues> getPersonalDayListValues(String date) {

		Cursor cursor = getAllPersonalEventsByDate(date);
		ArrayList<ContentValues> dayValues = new ArrayList<ContentValues>(
				CalendarConstants.HOURS.length);

		int index = 0;
		if (cursor != null) {
			cursor.moveToFirst();
			while (index < CalendarConstants.HOURS.length) {
				ContentValues dayValue = new ContentValues();
				String eventTitle = "";
				dayValue.put(CalendarConstants._ID, "");
				dayValue.put(CalendarConstants.HOURS_KEY1,
						CalendarConstants.HOURS[index]);
				dayValue.put(CalendarConstants.EVENT, eventTitle);
				dayValue.put(CalendarConstants.BG_COLOR, Color.WHITE);
				dayValue.put(CalendarConstants.TEXT_COLOR, Color.BLACK);
				dayValue.put(CalendarConstants.START_TIME, "-1");
				dayValue.put(CalendarConstants.END_TIME, "-1");
				dayValue.put(CalendarConstants.ALL_DAY, "0");
				dayValue.put(CalendarConstants.CALENDAR_TYPE_KEY,
						CalendarConstants.CALENDAR_TYPE_PERSONAL);
				dayValues.add(dayValue);

				index++;

			}

			while (!cursor.isAfterLast()) {
				int startTimeHours;
				int endTimeHours;
				int durationHours;
				String eventTitle = "";
				String eventStartTime = "";
				String eventEndTime = "";
				String timeZone="";
				int eventID = -1;
				int allDay = 0;
				long startTimeLong;
				long endTimeLong;
				long durationLong;
				String eventColor;
				Date startingDate = null, endingDate = null;

				int checkTime = 0;

				try {
					startingDate = new SimpleDateFormat("yyyy-MM-dd")
							.parse(CalendarDatabaseHelper.getDateString(date,
									0, 0));
					endingDate = new SimpleDateFormat("yyyy-MM-dd")
							.parse(CalendarDatabaseHelper.getDateString(date,
									0, 0));
				} catch (ParseException e) {
					e.printStackTrace();
				}

				startTimeLong = cursor.getLong(cursor
						.getColumnIndex(Event.DTSTART));
				endTimeLong = cursor
						.getLong(cursor.getColumnIndex(Event.DTEND));
				startTimeHours = new Date(startTimeLong).getHours();
				endTimeHours = new Date(endTimeLong).getHours();
				durationHours = endTimeHours - startTimeHours;
			
				int i = 0;

				if (startingDate.equals(endingDate)) {
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
				// for(ContentValues dayValue:dayValues)
				for (i = 0; i < dayValues.size(); i++) {

					ContentValues dayValue = dayValues.get(i);
					if (checkTime != -1
							&& checkTime >= (Long.parseLong((String) dayValue
									.get(CalendarConstants.HOURS_KEY1)))
							&& checkTime < (Long.parseLong((String) dayValue
									.get(CalendarConstants.HOURS_KEY1)) + 1)) {

						eventID = cursor.getInt(cursor
								.getColumnIndex(CalendarConstants._ID));
						eventTitle = cursor.getString(cursor
								.getColumnIndex(Event.TITLE));
						timeZone=cursor.getString(cursor
								.getColumnIndex(Events.EVENT_TIMEZONE));

						// eventColor =
						// cursor.getString(cursor.getColumnIndex(Event.EVENT_COLOR));
						if (startTimeLong == -1)
							eventStartTime = "00:00";
						else
							eventStartTime = CalendarDatabaseHelper
									.getTimeFromLong(startTimeLong, CalendarDatabaseHelper.getDefaultTimeZone());

						if (endTimeLong == -1)
							eventEndTime = "23:59";
						else
							eventEndTime = CalendarDatabaseHelper
									.getTimeFromLong(endTimeLong, CalendarDatabaseHelper.getDefaultTimeZone());

						allDay = cursor.getInt(cursor
								.getColumnIndex(Event.ALL_DAY));

						if (dayValue.getAsString(CalendarConstants.START_TIME)
								.equalsIgnoreCase("-1")) {
							dayValue.put(CalendarConstants.START_TIME, "");
						}
						if (dayValue.getAsString(CalendarConstants.END_TIME)
								.equalsIgnoreCase("-1"))
							dayValue.put(CalendarConstants.END_TIME, "");

						dayValue.put(CalendarConstants.EVENT,
								dayValue.get(CalendarConstants.EVENT) + ";"
										+ eventTitle);

						dayValue.put(CalendarConstants._ID,
								dayValue.get(CalendarConstants._ID) + ";"
										+ eventID);
						dayValue.put(CalendarConstants.START_TIME,
								dayValue.get(CalendarConstants.START_TIME)
										+ ";" + eventStartTime);
						dayValue.put(CalendarConstants.END_TIME,
								dayValue.get(CalendarConstants.END_TIME) + ";"
										+ eventEndTime);
						dayValue.put(CalendarConstants.ALL_DAY, allDay);
						dayValue.put(CalendarConstants.CALENDAR_TYPE_KEY,
								CalendarConstants.CALENDAR_TYPE_PERSONAL);
						// dayValue.put("bg_color",eventColor);

						int k = i;
						if (durationHours == 0) {
							dayValue.put(CalendarConstants.BG_COLOR, Color.RED);
							dayValue.put(CalendarConstants.TEXT_COLOR,
									Color.WHITE);
						} else {
							for (int j = durationHours; j > 0
									&& k < dayValues.size(); j--) {
								ContentValues dayValue1 = dayValues.get(k);
								dayValue1.put(CalendarConstants.BG_COLOR,
										Color.RED);
								dayValue1.put(CalendarConstants.TEXT_COLOR,
										Color.WHITE);

								k++;
							}
						}

					}
				}

				cursor.moveToNext();
			}
			cursor.close();
		}
		return dayValues;

	}

	public static Cursor getAllPersonalEventsByDate(String date) {
		// ArrayList<ArrayList<ContentValues>> totalEventsList = new
		// ArrayList<ArrayList<ContentValues>>();
		long[] dateToMilliSeconds = CalendarCommonFunction
				.convertDateToMillisec(CalendarDatabaseHelper.getDateString(
						date, 0, 0));
		String selection = "(((dtstart >= " + dateToMilliSeconds[0]
				+ ") AND (dtend <= " + dateToMilliSeconds[1]
				+ ")) AND (allDay =" + 0 + "))";
		String projection[] = { "calendar_id", Event.TITLE, Event.DTSTART,
				Event.DTEND, Event.ALL_DAY, "_id",Event.EVENT_TIMEZONE };
		String orderBy = Event.DTSTART;
		Cursor cursor = Email
				.getAppContext()
				.getContentResolver()
				.query(Email.getCALENDAR_URI(), projection, selection, null,
						orderBy);
		if (cursor != null)
			if (cursor.moveToFirst())
				cursor.moveToFirst();

		return cursor;
	}

	public static ArrayList<ContentValues> getListOfAccountOwner() {
		ArrayList<ContentValues> list_owner = new ArrayList<ContentValues>();
		try {
			Cursor cursor = getAllCalenderownerAccount();
			while (cursor.moveToNext()) {
				ContentValues ownerContentValue = new ContentValues();
				ownerContentValue.put(
						CalendarConstants.PERSONAL_OWNER_ACC_NAME, cursor
								.getString(cursor
										.getColumnIndex(Event.OWNER_ACCOUNT)));
				ownerContentValue.put(
						CalendarConstants.PERSONAL_OWNER_ACC_COLOR, cursor
								.getLong(cursor
										.getColumnIndex(Event.CALENDAR_COLOR)));
				list_owner.add(ownerContentValue);
			}
			HashSet hs = new HashSet();
			hs.addAll(list_owner);
			list_owner.clear();
			list_owner.addAll(hs);
		} catch (Exception Ex) {
			Ex.printStackTrace();
		}
		return list_owner;
	}

	private static Cursor getAllCalenderownerAccount() {
		String projection[] = { Events.OWNER_ACCOUNT, Event.CALENDAR_COLOR };

		Cursor cursor = Email.getAppContext().getContentResolver()
				.query(Email.getCALENDAR_URI(), projection, null, null, null);
		if (cursor.moveToFirst())
			cursor.moveToFirst();
		return cursor;

	}

}
