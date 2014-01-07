package com.cognizant.trumobi.calendar.modal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import android.net.Uri;

import com.cognizant.trumobi.calendar.provider.CalendarDatabaseHelper;
import com.cognizant.trumobi.calendar.util.CalendarConstants;
import com.cognizant.trumobi.log.CalendarLog;

public class Exceptions {

	public static final String AUTHORITY = CalendarConstants.AUTHORITY;
	public static final String EVENT_ID = "event_id"; // The _ID of the calendar
														// the event belongs to.
	
	public static final String EXCEPTION_ID = "exception_id";
	
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
	
	public static final String RRULE = "rrule"; // The recurrence rule for the
	// event format. For example,
	// "FREQ=WEEKLY;COUNT=10;WKST=SU".
	// You can find more examples
	// here.
	
	public static final String EXCEPTION_DTSTART = "exceptionDtStart";
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
	
	public static final String IS_EXCEPTION_DELETED = "isExceptionDeleted";
	public static final String ALL_DAY = "allDay"; // A value of 1 indicates
													// this event occupies the
													// entire day, as defined by
													// the local time zone. A
													// value of 0 indicates it
													// is a regular event that
													// may start and end at any
													// time during a day.
	
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

	public static final String DELETED = "deleted";
	public static final String STATUS = "status";
	public static final int STATUS_SYNCED = 0;
	public static final int STATUS_ADDED = 1;
	public static final int STATUS_MODIFIED = 2;
	public static final int STATUS_DELETED = 3;

	public static final String ACCESSIBILITY = "accessibility";
	
	public static final String RESPONSE_TYPE="responseType";
	public static final int RESPONSE_TYPE_NONE=5;
	public static final int RESPONSE_TYPE_ACCEPTED=1;
	public static final int RESPONSE_TYPE_TENTATIVE=2;
	public static final int RESPONSE_TYPE_REJECTED=3;
	
	public static final String ISINVITE="isInvite";
	
	public static final String MSG_ID="msgID";

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/exceptions");// The content:// style URL for interacting with
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
	public long exceptionDtStart;
	public String eventTimezone;
	public String eventEndTimezone;

	public int status;
	public int responseType;

	public int allDay;
	public int isExceptionDeleted;
	public String rrule;
	public String rdate;
	public String exrule;
	public String exdate;
	public String bg_color;
	public int availability;
	public int accessibility;
	
	public int isInvite;
	public String msgID;
	

	public ArrayList<Reminder> reminders;
	public ArrayList<Attendee> attendees;
	public Recurrence recurrence;

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

	public Exceptions() {
		super();
		this._id = 0;
		this.event_id = 0; // The _ID of the calendar the event belongs to.
		this.organizer = "";
		this.title = "";
		this.eventLocation = "";
		this.description = "";
		this.exceptionDtStart=0;
		this.dtstart = 0;
		this.dtend = 0;
		this.eventTimezone = "";
		this.eventEndTimezone = "";
		this.status = 0;
		this.responseType=RESPONSE_TYPE_NONE;
		this.isExceptionDeleted=0;
		this.allDay = 0;
		this.rrule = "";
		this.rdate = "";
		this.exdate = "";
		this.exrule = "";
		this.bg_color = "";		
		this.reminders = null;
		this.attendees=null;
		this.availability = 0;
		this.accessibility = 1;
		this.isInvite=-1;
		this.msgID="";


	}

	public Exceptions(
			int event_id, // The _ID of the calendar the event belongs to.
			String organizer, String title, String eventLocation,
			String description, String startDate, String endDate,long exceptionDtStart, long dtstart,
			long dtend, String eventTimezone, String eventEndTimezone,
			int status,int responseType, int allDay,int isExceptionDeleted, String rrule, String rdate, String exrule,
			String exdate, String eventColor, ArrayList<Attendee> attendees,
			ArrayList<Reminder> reminders, int availability, int accessibiltiy,int isInvite,
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
		this.exceptionDtStart=exceptionDtStart;
		this.dtstart = dtstart;
		this.dtend = dtend;
		this.eventTimezone = eventTimezone;
		this.eventEndTimezone = eventEndTimezone;
		this.status = status;
		this.responseType=responseType;
		this.allDay = allDay;
		this.isExceptionDeleted=isExceptionDeleted;
		this.rrule = rrule;
		this.rdate = rdate;
		this.exdate = exdate;
		this.exrule = exrule;
		this.bg_color = eventColor;	
		this.attendees=attendees;
		this.reminders = reminders;
		this.availability = availability;
		this.accessibility = accessibiltiy;
		this.isInvite=isInvite;

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

	public int isInvite() {
		return isInvite;
	}

	public void setInvite(int isInvite) {
		this.isInvite = isInvite;
	}
	public String getCalendarUID() {
		return calendarUID;
	}

	public void setCalendarUID(String calendarUID) {
		this.calendarUID = calendarUID;
	}

	public long getExcpetionDtStart() {
		return exceptionDtStart;
	}

	public void setExcpetionDtStart(long excpetionDtStart) {
		this.exceptionDtStart = excpetionDtStart;
	}

	public int getIsExceptionDeleted() {
		return isExceptionDeleted;
	}

	public void setIsExceptionDeleted(int isExceptionDeleted) {
		this.isExceptionDeleted = isExceptionDeleted;
	}

	public String getMsgID() {
		return msgID;
	}

	public void setMsgID(String msgID) {
		this.msgID = msgID;
	}

	public Recurrence getRecurrence() {
		return recurrence;
	}

	public void setRecurrence(Recurrence recurrence) {
		this.recurrence = recurrence;
	}
	public boolean isEqual(Exceptions exception)
	{
		
		if (this._id != exception._id) {
			return false;
		}

		if (this.calendarUID != null && !this.calendarUID.isEmpty()) {
			if (!this.calendarUID.equalsIgnoreCase(exception.calendarUID)) {
				return false;
			}
		} else {
			if (exception.calendarUID != null && !exception.calendarUID.isEmpty())
				return false;
		}

		if (this.event_id != exception.event_id) {
			return false;
		} // The _ID of the calendar the event belongs to.

		if (this.organizer != null && !this.organizer.isEmpty()) {
			if (!this.organizer.equalsIgnoreCase(exception.organizer)) {
				return false;
			}
		} else {
			if (exception.organizer != null && !exception.organizer.isEmpty())
				return false;
		}

		if (this.title != null && !this.title.isEmpty()) {
			if (!this.title.equalsIgnoreCase(exception.title)) {
				return false;
			}
		} else {
			if (exception.title != null && !exception.title.isEmpty())
				return false;
		}

		if (this.eventLocation != null && !this.eventLocation.isEmpty()) {
			if (!this.eventLocation.equalsIgnoreCase(exception.eventLocation)) {
				return false;
			}
		} else {
			if (exception.eventLocation != null && !exception.eventLocation.isEmpty())
				return false;
		}

		if (this.description != null && !this.description.isEmpty()) {
			if (!this.description.equalsIgnoreCase(exception.description)) {
				return false;
			}
		} else {
			if (exception.description != null && !exception.description.isEmpty())
				return false;
		}

		if (this.startDate != null && !this.startDate.isEmpty()) {
			if (!this.startDate.equalsIgnoreCase(exception.startDate)) {
				return false;
			}
		} else {
			if (exception.startDate != null && !exception.startDate.isEmpty())
				return false;
		}

		if (this.endDate != null && !this.endDate.isEmpty()) {
			if (!this.endDate.equalsIgnoreCase(exception.endDate)) {
				return false;
			}
		} else {
			if (exception.endDate != null && !exception.endDate.isEmpty())
				return false;
		}

		if (this.dtstart != exception.dtstart) {
			return false;
		}

		if (this.dtend != exception.dtend) {
			return false;
		}

		if (this.eventTimezone != null && !this.eventTimezone.isEmpty()) {
			if (!this.eventTimezone.equalsIgnoreCase(exception.eventTimezone)) {
				if (!CalendarDatabaseHelper.getTimeZoneOffsetFromString(this.eventTimezone)
						.equalsIgnoreCase(
								CalendarDatabaseHelper.getTimeZoneOffsetFromString(exception.eventTimezone))) {
					return false;
				}

			}
		} else {
			if (exception.eventTimezone != null && !exception.eventTimezone.isEmpty())
				return false;
		}

		if (this.responseType != exception.responseType) {
			return false;
		}

		if (this.allDay != exception.allDay) {
			return false;
		}

		if (this.rrule != null) {
			if (!this.rrule.equalsIgnoreCase(exception.rrule)) {
				return false;
			}
		} else if (exception.rrule != null) {
			if (!exception.rrule.equalsIgnoreCase(this.rrule)) {
				return false;
			}
		}

		if (this.availability != exception.availability) {
			return false;
		}
		if (this.accessibility != exception.accessibility) {
			return false;
		}
		
		if (exception.attendees != null && !exception.attendees.isEmpty()) {
			int index = 0;
			Iterator<Attendee> attendeeIterator = exception.attendees.iterator();
			while (attendeeIterator.hasNext()) {
				Attendee attendee = attendeeIterator.next();
				CalendarLog.d(CalendarConstants.Tag, "attendee1 : "
						+ CalendarDatabaseHelper.getAttendeesContentValues(attendee).toString());
				if (attendee.attendeeEmail.equalsIgnoreCase(CalendarDatabaseHelper.getEmail_ID())) {
					attendeeIterator.remove();
					CalendarLog.d("isEqualEvent", "2");
					break;
				}
			}
			attendeeIterator = exception.attendees.iterator();
			while (attendeeIterator.hasNext()) {
				Attendee attendee = attendeeIterator.next();
				while (attendeeIterator.hasNext()) {
					Attendee attendee1 = attendeeIterator.next();
					CalendarLog.d("isEqualEvent", "step att 2 "
							+ attendee1.attendeeEmail);
					if (attendee.attendeeEmail
							.equalsIgnoreCase(attendee1.attendeeEmail)) {
						if (index != exception.attendees.indexOf(attendee1)) {
							CalendarLog.d("isEqualEvent", "4 " + index + " "
									+ exception.attendees.indexOf(attendee1));
							attendeeIterator.remove();
							CalendarLog.d("isEqualEvent", "size "
									+ exception.attendees.size());

						}
					}
				}
				index++;
			}
			CalendarLog.d("isEqualEvent", "step att 2 ");
			if (this.attendees == null && !this.attendees.isEmpty()) {
				CalendarLog.d("isEqualEvent", "2 ");
				return false;

			} else {
				index = 0;
				for (Attendee attendee : exception.attendees) {
					boolean isExists = false;
					for (Attendee attendee2 : this.attendees) {
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

				if (this.attendees.size() != exception.attendees.size()) {
					CalendarLog.d("isEqualEvent",
							"7 " + this.attendees.size() + " "
									+ exception.attendees.size());
					return false;

				}
			}

		} else {
			if (this.attendees != null && !this.attendees.isEmpty()) {
				CalendarLog.d("isEqualEvent", "8");
				return false;

			} else {

				CalendarLog.d("isEqualEvent", "9");
			}
		}
		
		CalendarLog.d("isEqualEvent", "step rem ");
		if (this.reminders != null && !this.reminders.isEmpty()) {
			if (exception.reminders != null && !exception.reminders.isEmpty()) {
				if (this.reminders.size() == exception.reminders.size()) {
					ArrayList<Integer> reminderList1 = CalendarDatabaseHelper.getReminderMinutesFromReminders(this.reminders);
					ArrayList<Integer> reminderList2 = CalendarDatabaseHelper.getReminderMinutesFromReminders(exception.reminders);
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
			if (exception.reminders != null && !exception.reminders.isEmpty()) {
				CalendarLog.d("isEqualEvent", "17 ");
				return false;

			} else {
				CalendarLog.d("isEqualEvent", "18 ");
				return true;

			}
		}
		return true;
	}

}
