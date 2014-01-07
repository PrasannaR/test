package com.cognizant.trumobi.calendar.modal;

import android.net.Uri;

import com.cognizant.trumobi.calendar.util.CalendarConstants;

public class Attendee {
	public static final String AUTHORITY = CalendarConstants.AUTHORITY;
	public static final String EVENT_ID = "event_id"; // The ID of the event.
	public static final String ATTENDEE_NAME = "attendeeName"; // The name of
																// the attendee.
	public static final String ATTENDEE_EMAIL = "attendeeEmail"; // The email
																	// address
																	// of the
																	// attendee.
	public static final String ATTENDEE_RELATIONSHIP = "attendeeRelationship";
	// The relationship of the attendee to the event. One of:
	public static final int RELATIONSHIP_ATTENDEE = 1;
	public static final int RELATIONSHIP_NONE = 0;
	public static final int RELATIONSHIP_ORGANIZER = 2;
	public static final int RELATIONSHIP_PERFORMER = 3;

	public static final int RELATIONSHIP_SPEAKER = 4;

	public static final String ATTENDEE_TYPE = "attendeeType";
	// The type of attendee. One of:

	public static final int TYPE_REQUIRED = 1;
	public static final int TYPE_OPTIONAL = 2;
	public static final int TYPE_NONE = 0;

	public static final String ATTENDEE_STATUS = "attendeeStatus";
	// The attendance status of the attendee. One of:

	public static final int ATTENDEE_STATUS_ACCEPTED = 1;
	public static final int ATTENDEE_STATUS_DECLINED = 2;
	public static final int ATTENDEE_STATUS_INVITED = 3;
	public static final int ATTENDEE_STATUS_NONE = 0;
	public static final int ATTENDEE_STATUS_TENTATIVE = 4;
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/attendees");

	public String event_id;
	public String attendeeName;
	public String attendeeEmail;
	public int attendeeRelationship;
	public int attendeeType;
	public int attendeeStatus;

	public Attendee() {
		super();
		this.event_id = "";
		this.attendeeName = "";
		this.attendeeEmail = "";
		this.attendeeRelationship = 0;
		this.attendeeType = 0;
		this.attendeeStatus = 0;
	}

	public Attendee(String event_id, String attendeeName, String attendeeEmail,
			int attendeeRelationship, int attendeeType, int attendeeStatus) {
		super();
		this.event_id = event_id;
		this.attendeeName = attendeeName;
		this.attendeeEmail = attendeeEmail;
		this.attendeeRelationship = attendeeRelationship;
		this.attendeeType = attendeeType;
		this.attendeeStatus = attendeeStatus;
	}

	public String getEvent_id() {
		return event_id;
	}

	public void setEvent_id(String event_id) {
		this.event_id = event_id;
	}

	public String getAttendeeName() {
		return attendeeName;
	}

	public void setAttendeeName(String attendeeName) {
		this.attendeeName = attendeeName;
	}

	public String getAttendeeEmail() {
		return attendeeEmail;
	}

	public void setAttendeeEmail(String attendeeEmail) {
		this.attendeeEmail = attendeeEmail;
	}

	public int getAttendeeRelationship() {
		return attendeeRelationship;
	}

	public void setAttendeeRelationship(int attendeeRelationship) {
		this.attendeeRelationship = attendeeRelationship;
	}

	public int getAttendeeType() {
		return attendeeType;
	}

	public void setAttendeeType(int attendeeType) {
		this.attendeeType = attendeeType;
	}

	public int getAttendeeStatus() {
		return attendeeStatus;
	}

	public void setAttendeeStatus(int attendeeStatus) {
		this.attendeeStatus = attendeeStatus;
	}

}
