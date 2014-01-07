package com.cognizant.trumobi.calendar.modal;

import android.net.Uri;

import com.cognizant.trumobi.calendar.util.CalendarConstants;

public class Reminder {
	public static final String AUTHORITY = CalendarConstants.AUTHORITY;
	public static final String EVENT_ID = "event_id"; // The ID of the event.
	public static final String MINUTES = "minutes"; // The minutes prior to the
													// event that the reminder
													// should fire.
	public static final String METHOD = "method";
	// The alarm method, as set on the server. One of:

	public static final int METHOD_ALERT = 1;
	public static final int METHOD_DEFAULT = 0;
	public static final int METHOD_EMAIL = 2;
	public static final int METHOD_SMS = 3;

	public static final int METHOD_ALARM = 4;
	public static final int MINUTES_DEFAULT = -1;
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/reminders");
	public long event_id; // The ID of the event.
	public int minutes; // The minutes prior to the event that the reminder
						// should fire.
	public int method;

	public Reminder(int event_id, int minutes, int method) {
		super();
		this.event_id = event_id;
		this.minutes = minutes;
		this.method = method;
	}
	
	public Reminder() {
		// Auto-generated constructor stub
		this.event_id = -1;
		this.minutes = -1;
		this.method = -1;
	}

	public long getEvent_id() {
		return event_id;
	}

	public void setEvent_id(long event_id) {
		this.event_id = event_id;
	}

	public int getMinutes() {
		return minutes;
	}

	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}

	public int getMethod() {
		return method;
	}

	public void setMethod(int method) {
		this.method = method;
	}

}
