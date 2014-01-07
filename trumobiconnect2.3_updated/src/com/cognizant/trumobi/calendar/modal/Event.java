package com.cognizant.trumobi.calendar.modal;

import java.util.ArrayList;

import android.net.Uri;

import com.cognizant.trumobi.calendar.util.CalendarConstants;

public class Event {
	public static final String SYNC_DATA4 = "sync_data4";
	public static final String AUTHORITY = CalendarConstants.AUTHORITY;
	public static final String EVENT_ID = "event_id"; // The _ID of the calendar
														// the event belongs to.
	public static final String CALENDAR_UID="calendar_uid";
	public static final String ORGANIZER = "organizer"; // Email of the
														// organizer (owner) of
														// the event.
	public static final String TITLE = "title"; // The title of the event.
	public static final String EVENT_LOCATION = "eventLocation"; // Where the
																	// event
																	// takes
																	// place.
	public static final String DESCRIPTION = "description"; // The description
															// of the event.
	public static final String START_DATE = "startDate";
	public static final String END_DATE = "endDate";
	public static final String DTSTART = "dtstart"; // The time the event starts
													// in UTC milliseconds since
													// the epoch.
	public static final String DTEND = "dtend"; // The time the event ends in
												// UTC milliseconds since the
												// epoch.
	public static final String EVENT_TIMEZONE = "eventTimezone"; // The time
																	// zone for
																	// the
																	// event.
	public static final String EVENT_END_TIMEZONE = "eventEndTimezone"; // The
																		// time
																		// zone
																		// for
																		// the
																		// end
																		// time
																		// of
																		// the
																		// event.

	public static final String OWNER_ACCOUNT = "ownerAccount";
	public static final String CALENDAR_COLOR = "color";
	public static final String ALL_DAY = "allDay"; // A value of 1 indicates
													// this event occupies the
													// entire day, as defined by
													// the local time zone. A
													// value of 0 indicates it
													// is a regular event that
													// may start and end at any
													// time during a day.
	public static final String RRULE = "rrule"; // The recurrence rule for the
												// event format. For example,
												// "FREQ=WEEKLY;COUNT=10;WKST=SU".
												// You can find more examples
												// here.
	public static final String RDATE = "rdate"; // The recurrence dates for the
												// event. You typically use
												// RDATE in conjunction with
												// RRULE to define an aggregate
												// set of repeating occurrences.
												// For more discussion, see the
												// RFC5545 spec.
	public static final String EXDATE = "exdate";// The recurrence exception
													// dates for the event.
	public static final String EXRULE = "exrule";// The recurrence exception
													// rule for the event.
	public static final String AVAILABILITY = "availability"; // If this event
																// counts as
																// busy time or
																// is free time
																// that can be
																// scheduled
																// over.
	public static final int AVAILABILITY_BUSY = 0;
	public static final int AVAILABILITY_FREE = 1;
	public static final int AVAILABILITY_TENTATIVE = 2;
	public static final int AVAILABILITY_AWAY = 3;
	
	public static final String EVENT_COLOR = "eventColor";

	public static final String DELETED = "deleted";
	public static final String STATUS = "status";
	public static final int STATUS_SYNCED = 0;
	public static final int STATUS_ADDED = 1;
	public static final int STATUS_MODIFIED = 2;
	public static final int STATUS_DELETED = 3;

	public static final String ACCESSIBILITY = "accessibility";
	
	public static final int  ACCESS_DEFAULT=0;
	public static final int ACCESS_PUBLIC=3;
	public static final int ACCESS_CONFIDENTIAL=1;
	public static final int ACCESS_PRIVATE = 2;

	
	public static final String RESPONSE_TYPE="responseType";
	public static final int RESPONSE_TYPE_NONE=5;
	public static final int RESPONSE_TYPE_ACCEPTED=1;
	public static final int RESPONSE_TYPE_TENTATIVE=2;
	public static final int RESPONSE_TYPE_REJECTED=3;
	

	public static final String HAS_EXCEPTIONS="hasExceptions";
	
	public static final String MSG_ID="msgID";

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/events");// The content:// style URL for interacting with
							// events.

	public long _id;
	public String calendarUID;
	
	
	public int event_id; // The _ID of the calendar the event belongs to.
	
	public String organizer;

	public String title;
	public String eventLocation;
	public String description;
	public String startDate;
	public String endDate;

	public long dtstart;
	public long dtend;
	public String eventTimezone;
	public String eventEndTimezone;

	public int status;
	public int responseType;

	public int allDay;
	public String rrule;
	public String rdate;
	public String exrule;
	public String exdate;
	public String bg_color;
	public int availability;
	public int accessibility;
	

	public int hasException;
	public String msgID;
	

	public ArrayList<Attendee> attendees;
	public ArrayList<Reminder> reminders;
	public Recurrence recurrence;
	public ArrayList<Exceptions>exceptions; 

	public long get_id() {
		return _id;
	}

	public void set_id(long _id) {
		this._id = _id;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public ArrayList<Attendee> getAttendees() {
		return attendees;
	}

	public void setAttendees(ArrayList<Attendee> attendees) {
		this.attendees = attendees;
	}

	public ArrayList<Reminder> getReminders() {
		return reminders;
	}

	public void setReminders(ArrayList<Reminder> reminders) {
		this.reminders = reminders;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public int getAccessibility() {
		return accessibility;
	}

	public void setAccessibility(int accessibility) {
		this.accessibility = accessibility;
	}

	public Event() {
		super();
		this._id = 0;
		this.event_id = 0; // The _ID of the calendar the event belongs to.
		this.organizer = "";
		this.title = "";
		this.eventLocation = "";
		this.description = "";
		this.dtstart = -1;
		this.dtend = -1;
		this.eventTimezone = "";
		this.eventEndTimezone = "";
		this.status = -1;
		this.responseType=-1;
		this.allDay = -1;
		this.rrule = "";
		this.rdate = "";
		this.exdate = "";
		this.exrule = "";
		this.bg_color = "";
		this.attendees = null;
		this.reminders = null;
		this.availability = -1;
		this.accessibility = -1;

		this.hasException=-1;
		this.msgID="";


	}

	public Event(
			int event_id, // The _ID of the calendar the event belongs to.
			String organizer, String title, String eventLocation,
			String description, String startDate, String endDate, long dtstart,
			long dtend, String eventTimezone, String eventEndTimezone,
			int status,int responseType, int allDay, String rrule, String rdate, String exrule,
			String exdate, String eventColor, ArrayList<Attendee> attendees,
			ArrayList<Reminder> reminders, int availability, int accessibiltiy,
			boolean guestsCanModify, boolean guestsCanInviteOthers,
			boolean guestsCanSeeGuests) {
		super();
		this.event_id = event_id; // The _ID of the calendar the event belongs
									// to.
		this.organizer = organizer;
		this.title = title;
		this.eventLocation = eventLocation;
		this.description = description;
		this.startDate = startDate;
		this.endDate = endDate;
		this.dtstart = dtstart;
		this.dtend = dtend;
		this.eventTimezone = eventTimezone;
		this.eventEndTimezone = eventEndTimezone;
		this.status = status;
		this.responseType=responseType;
		this.allDay = allDay;
		this.rrule = rrule;
		this.rdate = rdate;
		this.exdate = exdate;
		this.exrule = exrule;
		this.bg_color = eventColor;
		this.attendees = attendees;
		this.reminders = reminders;
		this.availability = availability;
		this.accessibility = accessibiltiy;
	

	}

	public String getExrule() {
		return exrule;
	}

	public void setExrule(String exrule) {
		this.exrule = exrule;
	}

	public String getExdate() {
		return exdate;
	}

	public void setExdate(String exdate) {
		this.exdate = exdate;
	}

	public int getEvent_id() {
		return event_id;
	}

	public void setEvent_id(int event_id) {
		this.event_id = event_id;
	}

	public String getOrganizer() {
		return organizer;
	}

	public void setOrganizer(String organizer) {
		this.organizer = organizer;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getEventLocation() {
		return eventLocation;
	}

	public void setEventLocation(String eventLocation) {
		this.eventLocation = eventLocation;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getDtstart() {
		return dtstart;
	}

	public void setDtstart(long dtstart) {
		this.dtstart = dtstart;
	}

	public long getDtend() {
		return dtend;
	}

	public void setDtend(long dtend) {
		this.dtend = dtend;
	}

	public String getEventTimezone() {
		return eventTimezone;
	}

	public void setEventTimezone(String eventTimezone) {
		this.eventTimezone = eventTimezone;
	}

	public String getEventEndTimezone() {
		return eventEndTimezone;
	}

	public void setEventEndTimezone(String eventEndTimezone) {
		this.eventEndTimezone = eventEndTimezone;
	}

	public int getAllDay() {
		return allDay;
	}

	public void setAllDay(int allDay) {
		this.allDay = allDay;
	}

	public String getRrule() {
		return rrule;
	}

	public void setRrule(String rrule) {
		this.rrule = rrule;
	}

	public String getRdate() {
		return rdate;
	}

	public void setRdate(String rdate) {
		this.rdate = rdate;
	}

	public int getAvailability() {
		return availability;
	}

	public void setAvailability(int availability) {
		this.availability = availability;
	}

	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getBg_color() {
		return bg_color;
	}

	public void setBg_color(String bg_color) {
		this.bg_color = bg_color;
	}
	
	public int getResponseType() {
		return responseType;
	}

	public void setResponseType(int responseType) {
		this.responseType = responseType;
	}

	
	public String getCalendarUID() {
		return calendarUID;
	}

	public void setCalendarUID(String calendarUID) {
		this.calendarUID = calendarUID;
	}

}
