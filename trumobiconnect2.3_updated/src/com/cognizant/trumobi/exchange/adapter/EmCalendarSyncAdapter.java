package com.cognizant.trumobi.exchange.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TimeZone;

import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Entity;
import android.content.Entity.NamedContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.util.Log;

import com.cognizant.trumobi.calendar.modal.Attendee;
import com.cognizant.trumobi.calendar.modal.Event;
import com.cognizant.trumobi.calendar.modal.Exceptions;
import com.cognizant.trumobi.calendar.modal.Recurrence;
import com.cognizant.trumobi.calendar.modal.Reminder;
import com.cognizant.trumobi.calendar.provider.CalendarDatabaseHelper;
import com.cognizant.trumobi.calendar.util.CalendarConstants;
import com.cognizant.trumobi.common.provider.Calendar;
import com.cognizant.trumobi.common.provider.Calendar.Attendees;
import com.cognizant.trumobi.common.provider.Calendar.Calendars;
import com.cognizant.trumobi.common.provider.Calendar.Events;
import com.cognizant.trumobi.common.provider.Calendar.ExtendedProperties;
import com.cognizant.trumobi.common.provider.Calendar.Reminders;
import com.cognizant.trumobi.em.EmUtility;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.em.provider.EmEmailContent;
import com.cognizant.trumobi.em.provider.EmEmailContent.Mailbox;
import com.cognizant.trumobi.em.provider.EmEmailContent.Message;
import com.cognizant.trumobi.exchange.EmEas;
import com.cognizant.trumobi.exchange.EmEasOutboxService;
import com.cognizant.trumobi.exchange.EmEasSyncService;
import com.cognizant.trumobi.exchange.EmSyncManager;
import com.cognizant.trumobi.exchange.utility.EmCalendarUtilities;
import com.cognizant.trumobi.exchange.utility.EmDuration;
import com.cognizant.trumobi.log.CalendarLog;

/**
 * Sync adapter class for EAS calendars
 * 
 */
public class EmCalendarSyncAdapter extends EmAbstractSyncAdapter {

	private static final String TAG = "EasCalendarSyncAdapter";
	// Since exceptions will have the same _SYNC_ID as the original event we
	// have to check that
	// there's no original event when finding an item by _SYNC_ID
	private static final String SERVER_ID_AND_CALENDAR_ID = Events._SYNC_ID
			+ "=? AND " + Events.ORIGINAL_EVENT + " ISNULL AND "
			+ Events.CALENDAR_ID + "=?";
	private static final String DIRTY_OR_MARKED_TOP_LEVEL_IN_CALENDAR = "("
			+ Events._SYNC_DIRTY + "=1 OR " + Events._SYNC_MARK + "= 1) AND "
			+ Events.ORIGINAL_EVENT + " ISNULL AND " + Events.CALENDAR_ID
			+ "=?";
	private static final String DIRTY_EXCEPTION_IN_CALENDAR = Events._SYNC_DIRTY
			+ "=1 AND "
			+ Events.ORIGINAL_EVENT
			+ " NOTNULL AND "
			+ Events.CALENDAR_ID + "=?";
	private static final String CLIENT_ID_SELECTION = Events._SYNC_DATA + "=?";
	private static final String ORIGINAL_EVENT_AND_CALENDAR = Events.ORIGINAL_EVENT
			+ "=? AND " + Events.CALENDAR_ID + "=?";
	private static final String ATTENDEES_EXCEPT_ORGANIZER = Attendees.EVENT_ID
			+ "=? AND " + Attendees.ATTENDEE_RELATIONSHIP + "!="
			+ Attendees.RELATIONSHIP_ORGANIZER;
	private static final String[] ID_PROJECTION = new String[] { Events._ID };
	private static final String[] ORIGINAL_EVENT_PROJECTION = new String[] {
			Events.ORIGINAL_EVENT, Events._ID };
	private static final String EVENT_ID_AND_NAME = ExtendedProperties.EVENT_ID
			+ "=? AND " + ExtendedProperties.NAME + "=?";

	// Note that we use LIKE below for its case insensitivity
	private static final String EVENT_AND_EMAIL = Attendees.EVENT_ID
			+ "=? AND " + Attendees.ATTENDEE_EMAIL + " LIKE ?";
	private static final int ATTENDEE_STATUS_COLUMN_STATUS = 0;
	private static final String[] ATTENDEE_STATUS_PROJECTION = new String[] { Attendees.ATTENDEE_STATUS };

	public static final String CALENDAR_SELECTION = Calendars._SYNC_ACCOUNT
			+ "=? AND " + Calendars._SYNC_ACCOUNT_TYPE + "=?";
	private static final int CALENDAR_SELECTION_ID = 0;

	private static final String[] EXTENDED_PROPERTY_PROJECTION = new String[] { ExtendedProperties._ID };
	private static final int EXTENDED_PROPERTY_ID = 0;

	private static final String CATEGORY_TOKENIZER_DELIMITER = "\\";
	private static final String ATTENDEE_TOKENIZER_DELIMITER = CATEGORY_TOKENIZER_DELIMITER;

	private static final String EXTENDED_PROPERTY_USER_ATTENDEE_STATUS = "userAttendeeStatus";
	private static final String EXTENDED_PROPERTY_ATTENDEES = "attendees";
	private static final String EXTENDED_PROPERTY_DTSTAMP = "dtstamp";
	private static final String EXTENDED_PROPERTY_MEETING_STATUS = "meeting_status";
	private static final String EXTENDED_PROPERTY_CATEGORIES = "categories";
	// Used to indicate that we removed the attendee list because it was too
	// large
	private static final String EXTENDED_PROPERTY_ATTENDEES_REDACTED = "attendeesRedacted";
	// Used to indicate that upsyncs aren't allowed (we catch this in
	// sendLocalChanges)
	private static final String EXTENDED_PROPERTY_UPSYNC_PROHIBITED = "upsyncProhibited";

	private static final ContentProviderOperation PLACEHOLDER_OPERATION = ContentProviderOperation
			.newInsert(Uri.EMPTY).build();

	private static final Uri EVENTS_URI = asSyncAdapter(Events.CONTENT_URI);
	private static final Uri ATTENDEES_URI = asSyncAdapter(Attendees.CONTENT_URI);
	private static final Uri EXTENDED_PROPERTIES_URI = asSyncAdapter(ExtendedProperties.CONTENT_URI);
	private static final Uri REMINDERS_URI = asSyncAdapter(Reminders.CONTENT_URI);

	private static final Object sSyncKeyLock = new Object();

	private static final TimeZone UTC_TIMEZONE = TimeZone.getTimeZone("UTC");
	private final TimeZone mLocalTimeZone = TimeZone.getDefault();

	// Change this to use the constant in Calendar, when that constant is
	// defined
	private static final String EVENT_TIMEZONE2_COLUMN = "eventTimezone2";

	// Maximum number of allowed attendees; above this number, we mark the Event
	// with the
	// attendeesRedacted extended property and don't allow the event to be
	// upsynced to the server
	private static final int MAX_SYNCED_ATTENDEES = 50;
	// We set the organizer to this when the user is the organizer and we've
	// redacted the
	// attendee list. By making the meeting organizer OTHER than the user, we
	// cause the UI to
	// prevent edits to this event (except local changes like reminder).
	private static final String BOGUS_ORGANIZER_EMAIL = "upload_disallowed@uploadisdisallowed.aaa";
	// Maximum number of CPO's before we start redacting attendees in exceptions
	// The number 500 has been determined empirically; 1500 CPOs appears to be
	// the limit before
	// binder failures occur, but we need room at any point for additional
	// events/exceptions so
	// we set our limit at 1/3 of the apparent maximum for extra safety
	// TODO Find a better solution to this workaround
	private static final int MAX_OPS_BEFORE_EXCEPTION_ATTENDEE_REDACTION = 500;

	private long mCalendarId = -1;
	private String mCalendarIdString;
	private String[] mCalendarIdArgument;
	private String mEmailAddress;

	private ArrayList<Long> mDeletedIdList = new ArrayList<Long>();
	private ArrayList<Long> mUploadedIdList = new ArrayList<Long>();
	private ArrayList<Long> mSendCancelIdList = new ArrayList<Long>();
	private ArrayList<Message> mOutgoingMailList = new ArrayList<Message>();

	public EmCalendarSyncAdapter(Mailbox mailbox, EmEasSyncService service) {
		super(mailbox, service);
		// mEmailAddress = mAccount.mEmailAddress;
		// Cursor c = mService.mContentResolver.query(Calendars.CONTENT_URI,
		// new String[] {Calendars._ID}, CALENDAR_SELECTION,
		// new String[] {mEmailAddress, Email.EXCHANGE_ACCOUNT_MANAGER_TYPE},
		// null);
		// if (c == null) return;
		// try {
		// if (c.moveToFirst()) {
		// mCalendarId = c.getLong(CALENDAR_SELECTION_ID);
		// } else {
		// mCalendarId = EmCalendarUtilities.createCalendar(mService, mAccount,
		// mMailbox);
		// }
		// mCalendarIdString = Long.toString(mCalendarId);
		// mCalendarIdArgument = new String[] {mCalendarIdString};
		// } finally {
		// c.close();
		// }
	}

	@Override
	public String getCollectionName() {
		return "Calendar";
	}

	@Override
	public void cleanup() {
	}

	@Override
	public boolean isSyncable() {
		return ContentResolver.getSyncAutomatically(mAccountManagerAccount,
				CalendarConstants.AUTHORITY);
	}

	@Override
	public boolean parse(InputStream is) throws IOException {
		EasCalendarSyncParser p = new EasCalendarSyncParser(is, this);
		return p.parse();
	}

	static Uri asSyncAdapter(Uri uri) {
		return uri.buildUpon()
				.appendQueryParameter(Calendar.CALLER_IS_SYNCADAPTER, "true")
				.build();
	}

	/**
	 * Generate the uri for the data row associated with this NamedContentValues
	 * object
	 * 
	 * @param ncv
	 *            the NamedContentValues object
	 * @return a uri that can be used to refer to this row
	 */
	public Uri dataUriFromNamedContentValues(NamedContentValues ncv) {
		long id = ncv.values.getAsLong(RawContacts._ID);
		Uri dataUri = ContentUris.withAppendedId(ncv.uri, id);
		return dataUri;
	}

	/**
	 * We get our SyncKey from CalendarProvider. If there's not one, we set it
	 * to "0" (the reset state) and save that away.
	 */
	@Override
	public String getSyncKey() throws IOException {
		synchronized (sSyncKeyLock) {
			ContentProviderClient client = mService.mContentResolver
					.acquireContentProviderClient(Calendar.CONTENT_URI);
			try {
				// byte[] data = SyncStateContract.Helpers.get(client,
				// asSyncAdapter(Calendar.SyncState.CONTENT_URI),
				// mAccountManagerAccount);
				byte[] data = CalendarDatabaseHelper.getSyncKeyFromDatabase(
						mEmailAddress, mAccountManagerAccount);
				if (data == null || data.length == 0) {
					// Initialize the SyncKey
					setSyncKey("0", false);
					return "0";
				} else {
					String syncKey = new String(data);
					userLog("SyncKey retrieved as ", syncKey,
							" from CalendarProvider");
					return syncKey;
				}
			} catch (Exception e) {
				throw new IOException(
						"Can't get SyncKey from CalendarProvider "
								+ e.toString());
			}
		}
	}

	/**
	 * We only need to set this when we're forced to make the SyncKey "0" (a
	 * reset). In all other cases, the SyncKey is set within Calendar
	 */
	@Override
	public void setSyncKey(String syncKey, boolean inCommands)
			throws IOException {
		synchronized (sSyncKeyLock) {
			if ("0".equals(syncKey) || !inCommands) {
				ContentProviderClient client = mService.mContentResolver
						.acquireContentProviderClient(Calendar.CONTENT_URI);
				try {
					// SyncStateContract.Helpers.set(client,
					// asSyncAdapter(Calendar.SyncState.CONTENT_URI),
					// mAccountManagerAccount,
					// syncKey.getBytes());
					CalendarDatabaseHelper.setSyncKeyToDatabase(
							mAccountManagerAccount, syncKey.getBytes());
					userLog("SyncKey set to ", syncKey, " in CalendarProvider");
				} catch (Exception e) {
					throw new IOException(
							"Can't set SyncKey in CalendarProvider "
									+ e.toString());
				}
			}
			mMailbox.mSyncKey = syncKey;
		}
	}

	public class EasCalendarSyncParser extends EmAbstractSyncParser {

		String[] mBindArgument = new String[1];
		Uri mAccountUri;
		CalendarOperations mOps = new CalendarOperations();

		public EasCalendarSyncParser(InputStream in,
				EmCalendarSyncAdapter adapter) throws IOException {
			super(in, adapter);
			setLoggingTag("CalendarParser");
			mAccountUri = Events.CONTENT_URI;
		}

		@Override
		public void wipe() {
			// Delete the calendar associated with this account
			// CalendarProvider2 does NOT handle selection arguments in
			// deletions
			mContentResolver
					.delete(Calendars.CONTENT_URI,
							Calendars._SYNC_ACCOUNT
									+ "="
									+ DatabaseUtils
											.sqlEscapeString(mEmailAddress)
									+ " AND "
									+ Calendars._SYNC_ACCOUNT_TYPE
									+ "="
									+ DatabaseUtils
											.sqlEscapeString(Email.EXCHANGE_ACCOUNT_MANAGER_TYPE),
							null);
			// Invalidate our calendar observers
			EmSyncManager.unregisterCalendarObservers();
		}

		private void addOrganizerToAttendees(CalendarOperations ops,
				long eventId, String organizerName, String organizerEmail) {
			// Handle the organizer (who IS an attendee on device, but NOT in
			// EAS)
			if (organizerName != null || organizerEmail != null) {
				ContentValues attendeeCv = new ContentValues();
				if (organizerName != null) {
					attendeeCv.put(Attendees.ATTENDEE_NAME, organizerName);
				}
				if (organizerEmail != null) {
					attendeeCv.put(Attendees.ATTENDEE_EMAIL, organizerEmail);
				}
				attendeeCv.put(Attendees.ATTENDEE_RELATIONSHIP,
						Attendees.RELATIONSHIP_ORGANIZER);
				attendeeCv
						.put(Attendees.ATTENDEE_TYPE, Attendees.TYPE_REQUIRED);
				attendeeCv.put(Attendees.ATTENDEE_STATUS,
						Attendees.ATTENDEE_STATUS_ACCEPTED);
				if (eventId < 0) {
					ops.newAttendee(attendeeCv);
				} else {
					ops.updatedAttendee(attendeeCv, eventId);
				}
			}
		}

		/**
		 * Set DTSTART, DTEND, DURATION and EVENT_TIMEZONE as appropriate for
		 * the given Event The follow rules are enforced by CalendarProvider2:
		 * Events that aren't exceptions MUST have either 1) a DTEND or 2) a
		 * DURATION Recurring events (i.e. events with RRULE) must have a
		 * DURATION All-day recurring events MUST have a DURATION that is in the
		 * form P<n>D Other events MAY have a DURATION in any valid form (we use
		 * P<n>M) All-day events MUST have hour, minute, and second = 0; in
		 * addition, they must have the EVENT_TIMEZONE set to UTC Also,
		 * exceptions to all-day events need to have an ORIGINAL_INSTANCE_TIME
		 * that has hour, minute, and second = 0 and be set in UTC
		 * 
		 * @param cv
		 *            the ContentValues for the Event
		 * @param startTime
		 *            the start time for the Event
		 * @param endTime
		 *            the end time for the Event
		 * @param allDayEvent
		 *            whether this is an all day event (1) or not (0)
		 */
		/* package */void setTimeRelatedValues(ContentValues cv,
				long startTime, long endTime, int allDayEvent) {
			// If there's no startTime, the event will be found to be invalid,
			// so return
			if (startTime < 0)
				return;
			// EAS events can arrive without an end time, but CalendarProvider
			// requires them
			// so we'll default to 30 minutes; this will be superceded if this
			// is an all-day event
			if (endTime < 0)
				endTime = startTime + (30 * MINUTES);

			// If this is an all-day event, set hour, minute, and second to
			// zero, and use UTC
			if (allDayEvent != 0) {
				startTime = EmCalendarUtilities.getUtcAllDayCalendarTime(
						startTime, mLocalTimeZone);
				endTime = EmCalendarUtilities.getUtcAllDayCalendarTime(endTime,
						mLocalTimeZone);
				String originalTimeZone = cv.getAsString(Events.EVENT_TIMEZONE);
				cv.put(EVENT_TIMEZONE2_COLUMN, originalTimeZone);
				cv.put(Events.EVENT_TIMEZONE, UTC_TIMEZONE.getID());
			}

			// If this is an exception, and the original was an all-day event,
			// make sure the
			// original instance time has hour, minute, and second set to zero,
			// and is in UTC
			if (cv.containsKey(Events.ORIGINAL_INSTANCE_TIME)
					&& cv.containsKey(Events.ORIGINAL_ALL_DAY)) {
				Integer ade = cv.getAsInteger(Events.ORIGINAL_ALL_DAY);
				if (ade != null && ade != 0) {
					long exceptionTime = cv
							.getAsLong(Events.ORIGINAL_INSTANCE_TIME);
					GregorianCalendar cal = new GregorianCalendar(UTC_TIMEZONE);
					cal.setTimeInMillis(exceptionTime);
					cal.set(GregorianCalendar.HOUR_OF_DAY, 0);
					cal.set(GregorianCalendar.MINUTE, 0);
					cal.set(GregorianCalendar.SECOND, 0);
					cv.put(Events.ORIGINAL_INSTANCE_TIME, cal.getTimeInMillis());
				}
			}

			// Always set DTSTART
			cv.put(Events.DTSTART, startTime);
			// For recurring events, set DURATION. Use P<n>D format for all day
			// events
			if (cv.containsKey(Events.RRULE)) {
				if (allDayEvent != 0) {
					cv.put(Events.DURATION, "P"
							+ ((endTime - startTime) / DAYS) + "D");
				} else {
					cv.put(Events.DURATION, "P"
							+ ((endTime - startTime) / MINUTES) + "M");
				}
				// For other events, set DTEND and LAST_DATE
			} else {
				cv.put(Events.DTEND, endTime);
				cv.put(Events.LAST_DATE, endTime);
			}
		}

		public void addEvent(CalendarOperations ops, String serverId,
				boolean update) throws IOException {
			CalendarLog.d(CalendarConstants.Tag, "add event called");
			ContentValues cv = new ContentValues();
			// cv.put(Events.CALENDAR_ID, mCalendarId);
			// cv.put(Events._SYNC_ACCOUNT, mEmailAddress);
			// cv.put(Events._SYNC_ACCOUNT_TYPE,
			// Email.EXCHANGE_ACCOUNT_MANAGER_TYPE);
			// cv.put(Events._SYNC_ID, serverId);
			// cv.put(Events.HAS_ATTENDEE_DATA, 1);
			// cv.put(Events._SYNC_DATA, "0");

			int allDayEvent = 0;
			String organizerName = null;
			String organizerEmail = null;
			int eventOffset = -1;
			int deleteOffset = -1;
			int busyStatus = EmCalendarUtilities.BUSY_STATUS_TENTATIVE;

			boolean firstTag = true;
			long eventId = -1;
			long startTime = -1;
			long endTime = -1;
			TimeZone timeZone = null;

			// Keep track of the attendees; exceptions will need them
			ArrayList<Attendee> attendeeValues = new ArrayList<Attendee>();
			int reminderMins = -1;
			String dtStamp = null;
			boolean organizerAdded = false;
			Event event = new Event();
			event.event_id = Integer.parseInt(serverId.split(":")[1]);
			event.status = Event.STATUS_SYNCED;
			ArrayList<Reminder> reminderList = new ArrayList<Reminder>();
			event.recurrence = new Recurrence();
			event.exceptions=new ArrayList<Exceptions>();

			while (nextTag(EmTags.SYNC_APPLICATION_DATA) != END) {
//				if (update && firstTag) {
//					// Find the event that's being updated
//					Cursor c = getServerIdCursor(serverId);
//					long id = -1;
//					try {
//						if (c != null && c.moveToFirst()) {
//							id = c.getLong(0);
//						}
//					} finally {
//						if (c != null)
//							c.close();
//					}
//					if (id > 0) {
//						// DTSTAMP can come first, and we simply need to track
//						// it
//						if (tag == EmTags.CALENDAR_DTSTAMP) {
//							dtStamp = getValue();
//							continue;
//						} else if (tag == EmTags.CALENDAR_ATTENDEES) {
//							// This is an attendees-only update; just
//							// delete/re-add attendees
//							mBindArgument[0] = Long.toString(id);
//							ops.add(ContentProviderOperation
//									.newDelete(ATTENDEES_URI)
//									.withSelection(ATTENDEES_EXCEPT_ORGANIZER,
//											mBindArgument).build());
//							eventId = id;
//						} else {
//							// Otherwise, delete the original event and recreate
//							// it
//							userLog("Changing (delete/add) event ", serverId);
//							deleteOffset = ops.newDelete(id, serverId);
//							// Add a placeholder event so that associated tables
//							// can reference
//							// this as a back reference. We add the event at the
//							// end of the method
//							eventOffset = ops.newEvent(PLACEHOLDER_OPERATION);
//						}
//					} else {
//						// The changed item isn't found. We'll treat this as a
//						// new item
//						eventOffset = ops.newEvent(PLACEHOLDER_OPERATION);
//						userLog(TAG, "Changed item not found; treating as new.");
//					}
//				} else if (firstTag) {
					// Add a placeholder event so that associated tables can
					// reference
					// this as a back reference. We add the event at the end of
					// the method
					eventOffset = ops.newEvent(PLACEHOLDER_OPERATION);
//				}
				firstTag = false;
				switch (tag) {
				case EmTags.CALENDAR_ALL_DAY_EVENT:
					allDayEvent = getValueInt();
					if (allDayEvent != 0 && timeZone != null) {
						// If the event doesn't start at midnight local time, we
						// won't consider
						// this an all-day event in the local time zone (this is
						// what OWA does)
						GregorianCalendar cal = new GregorianCalendar(
								mLocalTimeZone);
						cal.setTimeInMillis(startTime);
						userLog("All-day event arrived in: " + timeZone.getID());
						if (cal.get(GregorianCalendar.HOUR_OF_DAY) != 0
								|| cal.get(GregorianCalendar.MINUTE) != 0) {
							allDayEvent = 0;
							userLog("Not an all-day event locally: "
									+ mLocalTimeZone.getID());
						}
					}
					event.allDay = allDayEvent;
					break;
				case EmTags.CALENDAR_ATTENDEES:
					// If eventId >= 0, this is an update; otherwise, a new
					// Event
					attendeeValues = attendeesParser(event.event_id);
					break;
				case EmTags.BASE_BODY:
					// cv.put(Events.DESCRIPTION, bodyParser());
					event.description = bodyParser();
					break;
				case EmTags.CALENDAR_BODY:
					// cv.put(Events.DESCRIPTION, getValue());
					event.description = getValue();
					break;
				case EmTags.CALENDAR_TIME_ZONE:
					timeZone = EmCalendarUtilities
							.tziStringToTimeZone(getValue());
					if (timeZone == null) {
						timeZone = mLocalTimeZone;
					}
					// cv.put(Events.EVENT_TIMEZONE, timeZone.getID());
//					event.eventTimezone = timeZone.getID();
                    //event.eventTimezone = CalendarDatabaseHelper.getTimeZoneOffsetFromString(timeZone);
					break;
				case EmTags.CALENDAR_START_TIME:
					startTime = EmUtility.parseDateTimeToMillis(getValue());
					event.dtstart = startTime;
					event.startDate = CalendarDatabaseHelper
							.getDateFromLong(startTime);
					break;
				case EmTags.CALENDAR_END_TIME:
					endTime = EmUtility.parseDateTimeToMillis(getValue());
					event.dtend = endTime;
					event.endDate = CalendarDatabaseHelper
							.getDateFromLong(endTime);
					break;
				case EmTags.CALENDAR_EXCEPTIONS:
			
					exceptionsParser(event);

					break;
				case EmTags.CALENDAR_LOCATION:
					// cv.put(Events.EVENT_LOCATION, getValue());
					event.eventLocation = getValue();
					break;
				case EmTags.CALENDAR_RECURRENCE:
					StringBuilder sb = new StringBuilder();
					event.recurrence = recurrenceParser(sb, event.event_id);
					String rrule = sb.toString();

					if (rrule != null) {
						// cv.put(Events.RRULE, rrule);
						event.rrule = rrule;
					}
					break;
				case EmTags.CALENDAR_ORGANIZER_EMAIL:
					organizerEmail = getValue();
					// cv.put(Events.ORGANIZER, organizerEmail);
					event.organizer = organizerEmail;
					break;
				case EmTags.CALENDAR_SUBJECT:
					// cv.put(Events.TITLE, getValue());
					event.title = getValue();
					break;
				case EmTags.CALENDAR_SENSITIVITY:
					int accessibility = encodeVisibility(getValueInt());
					CalendarLog.d(CalendarConstants.Tag, "event visibility "
							+ accessibility);
					// cv.put(Events.ACCESS_LEVEL,
					// encodeVisibility(getValueInt()));
					event.accessibility = accessibility;
					break;
				case EmTags.CALENDAR_ORGANIZER_NAME:
					organizerName = getValue();
					break;
				case EmTags.CALENDAR_REMINDER_MINS_BEFORE:
					reminderMins = getValueInt();
					// ops.newReminder(reminderMins);
					Reminder reminder = new Reminder(event.event_id,
							reminderMins, Reminder.METHOD_ALARM);
					reminderList.add(reminder);
					// cv.put(Events.HAS_ALARM, 1);
					break;
				// The following are fields we should save (for changes), though
				// they don't
				// relate to data used by CalendarProvider at this point
				case EmTags.CALENDAR_UID:
					event.calendarUID = getValue();
					break;
				case EmTags.CALENDAR_DTSTAMP:
					dtStamp = getValue();
					break;
				case EmTags.CALENDAR_MEETING_STATUS:
					ops.newExtendedProperty(EXTENDED_PROPERTY_MEETING_STATUS,
							getValue());
					break;
				case EmTags.CALENDAR_BUSY_STATUS:
					// We'll set the user's status in the Attendees table below
					// Don't set selfAttendeeStatus or CalendarProvider will
					// create a duplicate
					// attendee!
					int av = getValueInt();
					event.availability = EmCalendarUtilities
							.availabilityFromBusyStatus(av);
					CalendarLog.d(CalendarConstants.Tag, "event availablity "
							+ av + event.availability);
					break;
				case EmTags.CALENDAR_RESPONSE_TYPE:
					// EAS 14+ uses this for the user's response status; we'll
					// use this instead
					// of busy status, if it appears
					int responseType = getValueInt();
					switch (responseType) {
					case 3:
						event.responseType = Event.RESPONSE_TYPE_ACCEPTED;
						break;
					case 2:
						event.responseType = Event.RESPONSE_TYPE_TENTATIVE;
						break;
					case 4:
						event.responseType = Event.RESPONSE_TYPE_REJECTED;
						break;
					default:
						event.responseType = Event.RESPONSE_TYPE_NONE;

					}

					CalendarLog.d(CalendarConstants.Tag,
							"response type from server is "
									+ event.responseType);
					break;

				case EmTags.CALENDAR_CATEGORIES:
					String categories = categoriesParser(ops);
					if (categories.length() > 0) {
						ops.newExtendedProperty(EXTENDED_PROPERTY_CATEGORIES,
								categories);
					}
					break;
				default:
					skipTag();
				}
			}
			event.attendees = attendeeValues;
			event.reminders = reminderList;


			if(event.exceptions!=null)event.hasException=1;
			else
				event.hasException=0;
			
				

			CalendarLog.d(CalendarConstants.Tag, "inserting event from server "
					+ event.title);
			if(CalendarDatabaseHelper.insertEventToDatabase(event)==-1)
				CalendarLog.e(CalendarConstants.Tag,"Not inserted event "+CalendarDatabaseHelper.getEventContentValues(event));

		}

		private void logEventColumns(ContentValues cv, String reason) {
			if (EmEas.USER_LOG) {
				StringBuilder sb = new StringBuilder("Event invalid, " + reason
						+ ", skipping: Columns = ");
				for (Entry<String, Object> entry : cv.valueSet()) {
					sb.append(entry.getKey());
					sb.append('/');
				}
				userLog(TAG, sb.toString());
			}
		}

		/* package */boolean isValidEventValues(ContentValues cv) {
			boolean isException = cv.containsKey(Events.ORIGINAL_INSTANCE_TIME);
			// All events require DTSTART
			if (!cv.containsKey(Events.DTSTART)) {
				logEventColumns(cv, "DTSTART missing");
				return false;
				// If we're a top-level event, we must have _SYNC_DATA (uid)
			} else if (!isException && !cv.containsKey(Events._SYNC_DATA)) {
				logEventColumns(cv, "_SYNC_DATA missing");
				return false;
				// We must also have DTEND or DURATION if we're not an exception
			} else if (!isException && !cv.containsKey(Events.DTEND)
					&& !cv.containsKey(Events.DURATION)) {
				logEventColumns(cv, "DTEND/DURATION missing");
				return false;
				// Exceptions require DTEND
			} else if (isException && !cv.containsKey(Events.DTEND)) {
				logEventColumns(cv, "Exception missing DTEND");
				return false;
				// If this is a recurrence, we need a DURATION (in days if an
				// all-day event)
			} else if (cv.containsKey(Events.RRULE)) {
				String duration = cv.getAsString(Events.DURATION);
				if (duration == null)
					return false;
				if (cv.containsKey(Events.ALL_DAY)) {
					Integer ade = cv.getAsInteger(Events.ALL_DAY);
					if (ade != null && ade != 0 && !duration.endsWith("D")) {
						return false;
					}
				}
			}
			return true;
		}

		public Recurrence recurrenceParser(StringBuilder sb, int event_id)
				throws IOException {

			// Turn this information into an RRULE
			int type = -1;
			int occurrences = -1;
			int interval = -1;
			int dow = -1;
			int dom = -1;
			int wom = -1;
			int moy = -1;
			String until = null;

			while (nextTag(EmTags.CALENDAR_RECURRENCE) != END) {
				switch (tag) {
				case EmTags.CALENDAR_RECURRENCE_TYPE:
					type = getValueInt();
					break;
				case EmTags.CALENDAR_RECURRENCE_INTERVAL:
					interval = getValueInt();
					break;
				case EmTags.CALENDAR_RECURRENCE_OCCURRENCES:
					occurrences = getValueInt();
					break;
				case EmTags.CALENDAR_RECURRENCE_DAYOFWEEK:
					dow = getValueInt();
					break;
				case EmTags.CALENDAR_RECURRENCE_DAYOFMONTH:
					dom = getValueInt();
					break;
				case EmTags.CALENDAR_RECURRENCE_WEEKOFMONTH:
					wom = getValueInt();
					break;
				case EmTags.CALENDAR_RECURRENCE_MONTHOFYEAR:
					moy = getValueInt();
					break;
				case EmTags.CALENDAR_RECURRENCE_UNTIL:
					until = getValue();
					break;
				default:
					skipTag();
				}
			}

			Recurrence recurrence = new Recurrence(event_id, type, occurrences,
					interval, dow, "", dom, wom, moy, -1);
			recurrence.dowString = EmCalendarUtilities.getDaysFromDow(dow, wom);
			if (until != null)
				recurrence.until = EmUtility.parseDateTimeToMillis(until);
			else
				recurrence.until = -1;
			sb.append(EmCalendarUtilities.rruleFromRecurrence(type,
					occurrences, interval, dow, dom, wom, moy, until));
			return recurrence;

		}

		private void exceptionParser(Event event) throws IOException {
			Exceptions exception = new Exceptions();
			exception.calendarUID = event.calendarUID;

			// It appears that these values have to be copied from the parent
			// if
			// they are to appear
			// Note that they can be overridden below
			exception.organizer = event.organizer;
			exception.title = event.title;
			exception.description = event.description;
			exception.allDay = event.allDay;

			exception.eventLocation = event.eventLocation;
			exception.accessibility = event.accessibility;
			exception.eventTimezone = event.eventTimezone;

			// Exceptions should always have this set to zero, since EAS has
			// no
			// concept of
			// separate attendee lists for exceptions; if we fail to do this,
			// then the UI will
			// allow the user to change attendee data, and this change would
			// never get reflected
			// on the server.

			int allDayEvent = 0;

			// This column is the key that links the exception to the
			// serverId
			exception.event_id = event.event_id;

			String exceptionStartTime = "_noStartTime";
			while (nextTag(EmTags.SYNC_APPLICATION_DATA) != END) {
				switch (tag) {
			
				case EmTags.CALENDAR_EXCEPTION_START_TIME:
					exceptionStartTime = getValue();
					exception.exceptionDtStart = EmUtility
							.parseDateTimeToMillis(exceptionStartTime);
					exception.startDate = CalendarDatabaseHelper
							.getDateFromLong(exception.exceptionDtStart);

					break;
				case EmTags.CALENDAR_EXCEPTION_IS_DELETED:
					exception.isExceptionDeleted = getValueInt();
					break;
				case EmTags.CALENDAR_ATTENDEES:
					exception.attendees = attendeesParser(event.event_id);
					break;
				case EmTags.CALENDAR_ALL_DAY_EVENT:
					allDayEvent = getValueInt();
					exception.allDay = allDayEvent;
				case EmTags.BASE_BODY:
					exception.description = bodyParser();
					break;
				case EmTags.CALENDAR_BODY:
					exception.description = getValue();
					break;
				case EmTags.CALENDAR_START_TIME:
					exception.dtstart = EmUtility
							.parseDateTimeToMillis(getValue());
					exception.startDate = CalendarDatabaseHelper
							.getDateFromLong(exception.dtstart);
					break;
				case EmTags.CALENDAR_END_TIME:
					exception.dtend = EmUtility
							.parseDateTimeToMillis(getValue());
					exception.endDate = CalendarDatabaseHelper
							.getDateFromLong(exception.dtend);
					break;
				case EmTags.CALENDAR_LOCATION:
					exception.eventLocation = getValue();
					break;
				case EmTags.CALENDAR_RECURRENCE:
					StringBuilder sb = new StringBuilder();
					exception.recurrence = recurrenceParser(sb,
							exception.event_id);
					String rrule = sb.toString();
					if (rrule != null) {
						exception.rrule = rrule;
					}
					break;
				case EmTags.CALENDAR_SUBJECT:
					exception.title = getValue();
					break;
				case EmTags.CALENDAR_SENSITIVITY:
					exception.accessibility = encodeVisibility(getValueInt());
					break;
				case EmTags.CALENDAR_BUSY_STATUS:
					int av = getValueInt();
					exception.availability = EmCalendarUtilities
							.availabilityFromBusyStatus(av);
					// Don't set selfAttendeeStatus or CalendarProvider will
					// create a duplicate
					// attendee!
					break;
				// TODO How to handle these items that are linked to event id!
				// case EmTags.CALENDAR_DTSTAMP:
				// ops.newExtendedProperty("dtstamp", getValue());
				// break;
				// case EmTags.CALENDAR_REMINDER_MINS_BEFORE:
				// ops.newReminder(getValueInt());
				// break;
				default:
					skipTag();
				}
			}
			event.exceptions.add(exception);
		}

		private int encodeVisibility(int easVisibility) {
			int visibility = 0;
			switch (easVisibility) {
			case 0:
				visibility = 0;
				break;
			case 1:
				visibility = 0;
				break;
			case 2:
				visibility = 1;
				break;
			case 3:
				visibility = 1;
				break;
			}
			return visibility;
		}

		private void exceptionsParser(Event event) throws IOException {
			
			while (nextTag(EmTags.CALENDAR_EXCEPTIONS) != END) {
				switch (tag) {
				case EmTags.CALENDAR_EXCEPTION:
					exceptionParser(event);

					break;
				default:
					skipTag();
				}
			}
		}

		private String categoriesParser(CalendarOperations ops)
				throws IOException {
			StringBuilder categories = new StringBuilder();
			while (nextTag(EmTags.CALENDAR_CATEGORIES) != END) {
				switch (tag) {
				case EmTags.CALENDAR_CATEGORY:
					// TODO Handle categories (there's no similar concept for
					// gdata AFAIK)
					// We need to save them and spit them back when we update
					// the event
					categories.append(getValue());
					categories.append(CATEGORY_TOKENIZER_DELIMITER);
					break;
				default:
					skipTag();
				}
			}
			return categories.toString();
		}

		private ArrayList<Attendee> attendeesParser(long eventId)
				throws IOException {
			int attendeeCount = 0;
			ArrayList<Attendee> attendeeValues = new ArrayList<Attendee>();
			while (nextTag(EmTags.CALENDAR_ATTENDEES) != END) {
				switch (tag) {
				case EmTags.CALENDAR_ATTENDEE:
					Attendee cv = attendeeParser();
					// If we're going to redact these attendees anyway, let's
					// avoid
					// unnecessary
					// memory pressure, and not keep them around
					// We still need to parse them all, however
					attendeeCount++;
					// Allow one more than MAX_ATTENDEES, so that the check for
					// "too many" will
					// succeed in addEvent
					if (attendeeCount <= (MAX_SYNCED_ATTENDEES + 1)) {
						attendeeValues.add(cv);
					}
					break;
				default:
					skipTag();
				}
			}
			return attendeeValues;
		}

		private Attendee attendeeParser() throws IOException {
			Attendee attendee = new Attendee();		
			while (nextTag(EmTags.CALENDAR_ATTENDEE) != END) {
				switch (tag) {
				case EmTags.CALENDAR_ATTENDEE_EMAIL:
					attendee.attendeeEmail = getValue();
					break;
				case EmTags.CALENDAR_ATTENDEE_NAME:
					attendee.attendeeName = getValue();
					break;
				case EmTags.CALENDAR_ATTENDEE_STATUS:
					int status = getValueInt();
					attendee.attendeeStatus = (status == 2) ? Attendee.ATTENDEE_STATUS_TENTATIVE
							: (status == 3) ? Attendee.ATTENDEE_STATUS_ACCEPTED
									: (status == 4) ? Attendee.ATTENDEE_STATUS_DECLINED
											: (status == 5) ? Attendee.ATTENDEE_STATUS_INVITED
													: Attendee.ATTENDEE_STATUS_NONE;
					break;
				case EmTags.CALENDAR_ATTENDEE_TYPE:
					int type = Attendee.TYPE_NONE;
					// EAS types: 1 = req'd, 2 = opt, 3 = resource
					switch (getValueInt()) {
					case 1:
						type = Attendee.TYPE_REQUIRED;
						break;
					case 2:
						type = Attendee.TYPE_OPTIONAL;
						break;
					}
					attendee.attendeeType = type;
					break;
				default:
					skipTag();
				}
			}
			attendee.attendeeRelationship = Attendee.RELATIONSHIP_ATTENDEE;
			return attendee;
		}

		private String bodyParser() throws IOException {
			String body = null;
			while (nextTag(EmTags.BASE_BODY) != END) {
				switch (tag) {
				case EmTags.BASE_DATA:
					body = getValue();
					break;
				default:
					skipTag();
				}
			}

			// Handle null data without error
			if (body == null)
				return "";
			// Remove \r's from any body text
			return body.replace("\r\n", "\n");
		}

		public void addParser(CalendarOperations ops) throws IOException {
			String serverId = null;
			while (nextTag(EmTags.SYNC_ADD) != END) {
				switch (tag) {
				case EmTags.SYNC_SERVER_ID: // same as
					serverId = getValue();
					break;
				case EmTags.SYNC_APPLICATION_DATA:
					addEvent(ops, serverId, false);
					break;
				default:
					skipTag();
				}
			}
		}

//		private Cursor getServerIdCursor(String serverId) {
//			return mContentResolver.query(mAccountUri, ID_PROJECTION,
//					SERVER_ID_AND_CALENDAR_ID, new String[] { serverId,
//							mCalendarIdString }, null);
//		}

//		private Cursor getClientIdCursor(String clientId) {
//			mBindArgument[0] = clientId;
//			return mContentResolver.query(mAccountUri, ID_PROJECTION,
//					CLIENT_ID_SELECTION, mBindArgument, null);
//		}

		public void deleteParser(CalendarOperations ops) throws IOException {
			while (nextTag(EmTags.SYNC_DELETE) != END) {
				switch (tag) {
				case EmTags.SYNC_SERVER_ID:
					String serverId = getValue();
					serverId = "" + Integer.parseInt(serverId.split(":")[1]);
					// Find the event with the given serverId
					// Cursor c = getServerIdCursor(serverId);
					try {
						// if (c.moveToFirst()) {
						CalendarLog.d(CalendarConstants.Tag, "Deleting "
								+ serverId);
						String where = Event.EVENT_ID + " = " + serverId;

						Email.getAppContext().getContentResolver()
								.delete(Event.CONTENT_URI, where, null);

						// Intent i = new Intent();
						// i.setAction("com.cognizant.trumobi.calendar.sync_completed");
						// Email.getAppContext().sendBroadcast(i);
						// ops.delete(c.getLong(0), serverId);
						// }
					} finally {
						// c.close();
					}
					break;
				default:
					skipTag();
				}
			}
		}

		/**
		 * A change is handled as a delete (including all exceptions) and an add
		 * This isn't as efficient as attempting to traverse the original and
		 * all of its exceptions, but changes happen infrequently and this code
		 * is both simpler and easier to maintain
		 * 
		 * @param ops
		 *            the array of pending ContactProviderOperations.
		 * @throws IOException
		 */
		public void changeParser(CalendarOperations ops) throws IOException {
			String serverId = null;
			while (nextTag(EmTags.SYNC_CHANGE) != END) {
				switch (tag) {
				case EmTags.SYNC_SERVER_ID:
					serverId = getValue();
					break;
				case EmTags.SYNC_APPLICATION_DATA:
					userLog("Changing " + serverId);
					addEvent(ops, serverId, true);
					break;
				default:
					skipTag();
				}
			}
		}

		@Override
		public void commandsParser() throws IOException {
			while (nextTag(EmTags.SYNC_COMMANDS) != END) {
				if (tag == EmTags.SYNC_ADD) {
					addParser(mOps);
					incrementChangeCount();
				} else if (tag == EmTags.SYNC_DELETE) {
					deleteParser(mOps);
					incrementChangeCount();
				} else if (tag == EmTags.SYNC_CHANGE) {
					changeParser(mOps);
					incrementChangeCount();
				} else
					skipTag();
			}
		}

		@Override
		public void commit() throws IOException {
			userLog("Calendar SyncKey saved as: ", mMailbox.mSyncKey);
			// Save the syncKey here, using the Helper provider by Calendar
			// provider
			// mOps.add(SyncStateContract.Helpers.newSetOperation(SyncState.CONTENT_URI,
			// mAccountManagerAccount, mMailbox.mSyncKey.getBytes()));
			CalendarDatabaseHelper.updateSyncKeyInDatabase(
					mAccountManagerAccount, mMailbox.mSyncKey.getBytes());

			// We need to send cancellations now, because the Event won't exist
			// after the commit
			for (long eventId : mSendCancelIdList) {
				EmEmailContent.Message msg;
				try {
					msg = EmCalendarUtilities
							.createMessageForEventId(
									mContext,
									eventId,
									EmEmailContent.Message.FLAG_OUTGOING_MEETING_CANCEL,
									null, mAccount);
				} catch (RemoteException e) {
					// Nothing to do here; the Event may no longer exist
					continue;
				}
				if (msg != null) {
					EmEasOutboxService.sendMessage(mContext, mAccount.mId, msg);
				}
			}

			// Execute these all at once...
			// mOps.execute();

			if (mOps.mResults != null) {
				// Clear dirty and mark flags for updates sent to server
				if (!mUploadedIdList.isEmpty()) {
					ContentValues cv = new ContentValues();
					cv.put(Events._SYNC_DIRTY, 0);
					cv.put(Events._SYNC_MARK, 0);
					for (long eventId : mUploadedIdList) {
						mContentResolver
								.update(ContentUris.withAppendedId(EVENTS_URI,
										eventId), cv, null, null);
					}
				}
				// Delete events marked for deletion
				if (!mDeletedIdList.isEmpty()) {
					for (long eventId : mDeletedIdList) {
						mContentResolver
								.delete(ContentUris.withAppendedId(EVENTS_URI,
										eventId), null, null);
					}
				}
				// Send any queued up email (invitations replies, etc.)
				for (Message msg : mOutgoingMailList) {
					EmEasOutboxService.sendMessage(mContext, mAccount.mId, msg);
				}
			}
		}

		public void addResponsesParser() throws IOException {
			String serverId = null;
			String clientId = null;
			int status = -1;
			ContentValues cv = new ContentValues();
			while (nextTag(EmTags.SYNC_ADD) != END) {
				switch (tag) {
				case EmTags.SYNC_SERVER_ID:
					serverId = getValue();
					break;
				case EmTags.SYNC_CLIENT_ID:
					clientId = getValue();
					break;
				case EmTags.SYNC_STATUS:
					String val = getValue();
					if (val.equalsIgnoreCase("1")) {
						CalendarDatabaseHelper.changeStatusToSynced(clientId,
								"" + Integer.parseInt(serverId.split(":")[1]),
								Event.STATUS_SYNCED);
					}
					if (status != 1) {
						userLog("Attempt to add event failed with status: "
								+ status);
					}
					break;
				default:
					skipTag();
				}
			}

//			if (clientId == null)
//				return;
//			if (serverId == null) {
//				// TODO Reconsider how to handle this
//				serverId = "FAIL:" + status;
//			}

//			Cursor c = getClientIdCursor(clientId);
//			try {
//				if (c.moveToFirst()) {
//					cv.put(Events._SYNC_ID, serverId);
//					cv.put(Events._SYNC_DATA, clientId);
//					long id = c.getLong(0);
//					// Write the serverId into the Event
//					mOps.add(ContentProviderOperation
//							.newUpdate(
//									ContentUris.withAppendedId(EVENTS_URI, id))
//							.withValues(cv).build());
//					userLog("New event " + clientId + " was given serverId: "
//							+ serverId);
//				}
//			} finally {
//				c.close();
//			}
		}

		public void changeResponsesParser() throws IOException {
			String serverId = null;
			String status = null;
			while (nextTag(EmTags.SYNC_CHANGE) != END) {
				switch (tag) {
				case EmTags.SYNC_SERVER_ID:
					serverId = getValue();
					break;
				case EmTags.SYNC_STATUS:
					status = getValue();
					if (status.equalsIgnoreCase("1")) {
						CalendarDatabaseHelper.changeStatusToSynced(
								serverId.split(":")[1], serverId.split(":")[1],
								Event.STATUS_SYNCED);

					}
					break;
				default:
					skipTag();
				}
			}
			if (serverId != null && status != null) {
				userLog("Changed event " + serverId + " failed with status: "
						+ status);
			}
		}

		@Override
		public void responsesParser() throws IOException {
			// Handle server responses here (for Add and Change)
			while (nextTag(EmTags.SYNC_RESPONSES) != END) {
				if (tag == EmTags.SYNC_ADD) {
					addResponsesParser();
				} else if (tag == EmTags.SYNC_CHANGE) {
					changeResponsesParser();
				} else
					skipTag();
			}
		}
	}

	private class CalendarOperations extends
			ArrayList<ContentProviderOperation> {
		private static final long serialVersionUID = 1L;
		public int mCount = 0;
		private ContentProviderResult[] mResults = null;
		private int mEventStart = 0;

		@Override
		public boolean add(ContentProviderOperation op) {
			super.add(op);
			mCount++;
			return true;
		}

		public int newEvent(ContentProviderOperation op) {
			mEventStart = mCount;
			add(op);
			return mEventStart;
		}

		public int newDelete(long id, String serverId) {
			int offset = mCount;
			delete(id, serverId);
			return offset;
		}

		public void newAttendee(ContentValues cv) {
			newAttendee(cv, mEventStart);
		}

		public void newAttendee(ContentValues cv, int eventStart) {
			add(ContentProviderOperation.newInsert(ATTENDEES_URI)
					.withValues(cv)
					.withValueBackReference(Attendees.EVENT_ID, eventStart)
					.build());
		}

		public void updatedAttendee(ContentValues cv, long id) {
			cv.put(Attendees.EVENT_ID, id);
			add(ContentProviderOperation.newInsert(ATTENDEES_URI)
					.withValues(cv).build());
		}

		public void newException(ContentValues cv) {
			add(ContentProviderOperation.newInsert(EVENTS_URI).withValues(cv)
					.build());
		}

		public void newExtendedProperty(String name, String value) {
			add(ContentProviderOperation
					.newInsert(EXTENDED_PROPERTIES_URI)
					.withValue(ExtendedProperties.NAME, name)
					.withValue(ExtendedProperties.VALUE, value)
					.withValueBackReference(ExtendedProperties.EVENT_ID,
							mEventStart).build());
		}

		public void updatedExtendedProperty(String name, String value, long id) {
			// Find an existing ExtendedProperties row for this event and
			// property name
			Cursor c = mService.mContentResolver.query(
					ExtendedProperties.CONTENT_URI,
					EXTENDED_PROPERTY_PROJECTION, EVENT_ID_AND_NAME,
					new String[] { Long.toString(id), name }, null);
			long extendedPropertyId = -1;
			// If there is one, capture its _id
			if (c != null) {
				try {
					if (c.moveToFirst()) {
						extendedPropertyId = c.getLong(EXTENDED_PROPERTY_ID);
					}
				} finally {
					c.close();
				}
			}
			// Either do an update or an insert, depending on whether one
			// already exists
			if (extendedPropertyId >= 0) {
				add(ContentProviderOperation
						.newUpdate(
								ContentUris.withAppendedId(
										EXTENDED_PROPERTIES_URI,
										extendedPropertyId))
						.withValue(ExtendedProperties.VALUE, value).build());
			} else {
				newExtendedProperty(name, value);
			}
		}

		public void newReminder(int mins, int eventStart) {
			add(ContentProviderOperation
					.newInsert(REMINDERS_URI)
					.withValue(Reminders.MINUTES, mins)
					.withValue(Reminders.METHOD, Reminders.METHOD_ALERT)
					.withValueBackReference(ExtendedProperties.EVENT_ID,
							eventStart).build());
		}

		public void newReminder(int mins) {
			newReminder(mins, mEventStart);
		}

		public void delete(long id, String syncId) {
			add(ContentProviderOperation.newDelete(
					ContentUris.withAppendedId(EVENTS_URI, id)).build());
			// Delete the exceptions for this Event (CalendarProvider doesn't do
			// this)
			add(ContentProviderOperation
					.newDelete(EVENTS_URI)
					.withSelection(Events.ORIGINAL_EVENT + "=?",
							new String[] { syncId }).build());
		}

		public void execute() {
			synchronized (mService.getSynchronizer()) {
				if (!mService.isStopped()) {
					try {
						if (!isEmpty()) {
							mService.userLog("Executing ", size(), " CPO's");
							mResults = mContext.getContentResolver()
									.applyBatch(CalendarConstants.AUTHORITY,
											this);
						}
					} catch (RemoteException e) {
						// There is nothing sensible to be done here
						Log.e(TAG,
								"problem inserting event during server update",
								e);
					} catch (OperationApplicationException e) {
						// There is nothing sensible to be done here
						Log.e(TAG,
								"problem inserting event during server update",
								e);
					}
				}
			}
		}
	}

	private String decodeVisibility(int visibility) {
		int easVisibility = 0;
		switch (visibility) {
		case Event.ACCESS_DEFAULT:
			easVisibility = 1;
			break;
		case Event.ACCESS_PUBLIC:
			easVisibility = 1;
			break;
		case Event.ACCESS_PRIVATE:
			easVisibility = 2;
			break;
		case Event.ACCESS_CONFIDENTIAL:
			easVisibility = 2;
			break;
		}
		return Integer.toString(easVisibility);
	}

	private int getInt(ContentValues cv, String column) {
		Integer i = cv.getAsInteger(column);
		if (i == null)
			return 0;
		return i;
	}

	private void sendEvent(Entity entity, String clientId, EmSerializer s)
			throws IOException {
		// Serialize for EAS here
		// Set uid with the client id we created
		// 1) Serialize the top-level event
		// 2) Serialize attendees and reminders from subvalues
		// 3) Look for exceptions and serialize with the top-level event
		ContentValues entityValues = entity.getEntityValues();
		final boolean isException = (clientId == null);
		boolean hasAttendees = false;
		final boolean isChange = entityValues.containsKey(Events._SYNC_ID);
		final Double version = mService.mProtocolVersionDouble;
		final boolean allDay = EmCalendarUtilities.getIntegerValueAsBoolean(
				entityValues, Events.ALL_DAY);

		// NOTE: Exchange 2003 (EAS 2.5) seems to require the
		// "exception deleted" and "exception
		// start time" data before other data in exceptions. Failure to do so
		// results in a
		// status 6 error during sync
		if (isException) {
			// Send exception deleted flag if necessary
			Integer deleted = entityValues
					.getAsInteger(Calendar.EventsColumns.DELETED);
			boolean isDeleted = deleted != null && deleted == 1;
			Integer eventStatus = entityValues.getAsInteger(Events.STATUS);
			boolean isCanceled = eventStatus != null
					&& eventStatus.equals(Events.STATUS_CANCELED);
			if (isDeleted || isCanceled) {
				s.data(EmTags.CALENDAR_EXCEPTION_IS_DELETED, "1");
				// If we're deleted, the UI will continue to show this exception
				// until we mark
				// it canceled, so we'll do that here...
				if (isDeleted && !isCanceled) {
					final long eventId = entityValues.getAsLong(Events._ID);
					ContentValues cv = new ContentValues();
					cv.put(Events.STATUS, Events.STATUS_CANCELED);
					mService.mContentResolver.update(
							ContentUris.withAppendedId(EVENTS_URI, eventId),
							cv, null, null);
				}
			} else {
				s.data(EmTags.CALENDAR_EXCEPTION_IS_DELETED, "0");
			}

			// TODO Add reminders to exceptions (allow them to be specified!)
			Long originalTime = entityValues
					.getAsLong(Events.ORIGINAL_INSTANCE_TIME);
			if (originalTime != null) {
				final boolean originalAllDay = EmCalendarUtilities
						.getIntegerValueAsBoolean(entityValues,
								Events.ORIGINAL_ALL_DAY);
				if (originalAllDay) {
					// For all day events, we need our local all-day time
					originalTime = EmCalendarUtilities
							.getLocalAllDayCalendarTime(originalTime,
									mLocalTimeZone);
				}
				s.data(EmTags.CALENDAR_EXCEPTION_START_TIME,
						EmCalendarUtilities.millisToEasDateTime(originalTime));
			} else {
				// Illegal; what should we do?
			}
		}

		// Get the event's time zone
		String timeZoneName = entityValues
				.getAsString(allDay ? EVENT_TIMEZONE2_COLUMN
						: Events.EVENT_TIMEZONE);
		if (timeZoneName == null) {
			timeZoneName = mLocalTimeZone.getID();
		}
		TimeZone eventTimeZone = TimeZone.getTimeZone(timeZoneName);

		if (!isException) {
			// A time zone is required in all EAS events; we'll use the default
			// if none is set
			// Exchange 2003 seems to require this first... :-)
			String timeZone = EmCalendarUtilities
					.timeZoneToTziString(eventTimeZone);
			s.data(EmTags.CALENDAR_TIME_ZONE, timeZone);
		}

		s.data(EmTags.CALENDAR_ALL_DAY_EVENT, allDay ? "1" : "0");

		// DTSTART is always supplied
		long startTime = entityValues.getAsLong(Events.DTSTART);
		// Determine endTime; it's either provided as DTEND or we calculate
		// using DURATION
		// If no DURATION is provided, we default to one hour
		long endTime;
		if (entityValues.containsKey(Events.DTEND)) {
			endTime = entityValues.getAsLong(Events.DTEND);
		} else {
			long durationMillis = HOURS;
			if (entityValues.containsKey(Events.DURATION)) {
				EmDuration duration = new EmDuration();
				try {
					duration.parse(entityValues.getAsString(Events.DURATION));
					durationMillis = duration.getMillis();
				} catch (ParseException e) {
					// Can't do much about this; use the default (1 hour)
				}
			}
			endTime = startTime + durationMillis;
		}
		if (allDay) {
			TimeZone tz = mLocalTimeZone;
			startTime = EmCalendarUtilities.getLocalAllDayCalendarTime(
					startTime, tz);
			endTime = EmCalendarUtilities.getLocalAllDayCalendarTime(endTime,
					tz);
		}
		s.data(EmTags.CALENDAR_START_TIME,
				EmCalendarUtilities.millisToEasDateTime(startTime));
		s.data(EmTags.CALENDAR_END_TIME,
				EmCalendarUtilities.millisToEasDateTime(endTime));

		s.data(EmTags.CALENDAR_DTSTAMP, EmCalendarUtilities
				.millisToEasDateTime(System.currentTimeMillis()));

		String loc = entityValues.getAsString(Events.EVENT_LOCATION);
		if (!TextUtils.isEmpty(loc)) {
			if (version < EmEas.SUPPORTED_PROTOCOL_EX2007_DOUBLE) {
				// EAS 2.5 doesn't like bare line feeds
				loc = EmUtility.replaceBareLfWithCrlf(loc);
			}
			s.data(EmTags.CALENDAR_LOCATION, loc);
		}
		s.writeStringValue(entityValues, Events.TITLE, EmTags.CALENDAR_SUBJECT);

		String desc = entityValues.getAsString(Events.DESCRIPTION);
		if (desc != null && desc.length() > 0) {
			if (version >= EmEas.SUPPORTED_PROTOCOL_EX2007_DOUBLE) {
				s.start(EmTags.BASE_BODY);
				s.data(EmTags.BASE_TYPE, "1");
				s.data(EmTags.BASE_DATA, desc);
				s.end();
			} else {
				// EAS 2.5 doesn't like bare line feeds
				s.data(EmTags.CALENDAR_BODY,
						EmUtility.replaceBareLfWithCrlf(desc));
			}
		}

		if (!isException) {
			// For Exchange 2003, only upsync if the event is new
			if ((version >= EmEas.SUPPORTED_PROTOCOL_EX2007_DOUBLE)
					|| !isChange) {
				s.writeStringValue(entityValues, Events.ORGANIZER,
						EmTags.CALENDAR_ORGANIZER_EMAIL);
			}

			String rrule = entityValues.getAsString(Events.RRULE);
			if (rrule != null) {
				EmCalendarUtilities.recurrenceFromRrule(rrule, startTime, s);
			}

			// Handle associated data EXCEPT for attendees, which have to be
			// grouped
			ArrayList<NamedContentValues> subValues = entity.getSubValues();
			// The earliest of the reminders for this Event; we can only send
			// one reminder...
			int earliestReminder = -1;
			for (NamedContentValues ncv : subValues) {
				Uri ncvUri = ncv.uri;
				ContentValues ncvValues = ncv.values;
				if (ncvUri.equals(ExtendedProperties.CONTENT_URI)) {
					String propertyName = ncvValues
							.getAsString(ExtendedProperties.NAME);
					String propertyValue = ncvValues
							.getAsString(ExtendedProperties.VALUE);
					if (TextUtils.isEmpty(propertyValue)) {
						continue;
					}
					if (propertyName.equals(EXTENDED_PROPERTY_CATEGORIES)) {
						// Send all the categories back to the server
						// We've saved them as a String of delimited tokens
						StringTokenizer st = new StringTokenizer(propertyValue,
								CATEGORY_TOKENIZER_DELIMITER);
						if (st.countTokens() > 0) {
							s.start(EmTags.CALENDAR_CATEGORIES);
							while (st.hasMoreTokens()) {
								String category = st.nextToken();
								s.data(EmTags.CALENDAR_CATEGORY, category);
							}
							s.end();
						}
					}
				} else if (ncvUri.equals(Reminders.CONTENT_URI)) {
					Integer mins = ncvValues.getAsInteger(Reminders.MINUTES);
					if (mins != null) {
						// -1 means "default", which for Exchange, is 30
						if (mins < 0) {
							mins = 30;
						}
						// Save this away if it's the earliest reminder
						// (greatest minutes)
						if (mins > earliestReminder) {
							earliestReminder = mins;
						}
					}
				}
			}

			// If we have a reminder, send it to the server
			if (earliestReminder >= 0) {
				s.data(EmTags.CALENDAR_REMINDER_MINS_BEFORE,
						Integer.toString(earliestReminder));
			}

			// We've got to send a UID, unless this is an exception. If the
			// event is new, we've
			// generated one; if not, we should have gotten one from extended
			// properties.
			if (clientId != null) {
				s.data(EmTags.CALENDAR_UID, clientId);
			}

			// Handle attendee data here; keep track of organizer and stream it
			// afterward
			String organizerName = null;
			String organizerEmail = null;
			for (NamedContentValues ncv : subValues) {
				Uri ncvUri = ncv.uri;
				ContentValues ncvValues = ncv.values;
				if (ncvUri.equals(Attendees.CONTENT_URI)) {
					Integer relationship = ncvValues
							.getAsInteger(Attendees.ATTENDEE_RELATIONSHIP);
					// If there's no relationship, we can't create this for EAS
					// Similarly, we need an attendee email for each invitee
					if (relationship != null
							&& ncvValues.containsKey(Attendees.ATTENDEE_EMAIL)) {
						// Organizer isn't among attendees in EAS
						if (relationship == Attendees.RELATIONSHIP_ORGANIZER) {
							organizerName = ncvValues
									.getAsString(Attendees.ATTENDEE_NAME);
							organizerEmail = ncvValues
									.getAsString(Attendees.ATTENDEE_EMAIL);
							continue;
						}
						if (!hasAttendees) {
							s.start(EmTags.CALENDAR_ATTENDEES);
							hasAttendees = true;
						}
						s.start(EmTags.CALENDAR_ATTENDEE);
						String attendeeEmail = ncvValues
								.getAsString(Attendees.ATTENDEE_EMAIL);
						String attendeeName = ncvValues
								.getAsString(Attendees.ATTENDEE_NAME);
						if (attendeeName == null) {
							attendeeName = attendeeEmail;
						}
						s.data(EmTags.CALENDAR_ATTENDEE_NAME, attendeeName);
						s.data(EmTags.CALENDAR_ATTENDEE_EMAIL, attendeeEmail);
						if (version >= EmEas.SUPPORTED_PROTOCOL_EX2007_DOUBLE) {
							s.data(EmTags.CALENDAR_ATTENDEE_TYPE, "1"); // Required
						}
						s.end(); // Attendee
					}
				}
			}
			if (hasAttendees) {
				s.end(); // Attendees
			}

			// Get busy status from Attendees table
			long eventId = entityValues.getAsLong(Events._ID);
			int busyStatus = EmCalendarUtilities.BUSY_STATUS_TENTATIVE;
			Cursor c = mService.mContentResolver.query(ATTENDEES_URI,
					ATTENDEE_STATUS_PROJECTION, EVENT_AND_EMAIL, new String[] {
							Long.toString(eventId), mEmailAddress }, null);
			if (c != null) {
				try {
					if (c.moveToFirst()) {
						busyStatus = EmCalendarUtilities
								.busyStatusFromAttendeeStatus(c
										.getInt(ATTENDEE_STATUS_COLUMN_STATUS));
					}
				} finally {
					c.close();
				}
			}
			s.data(EmTags.CALENDAR_BUSY_STATUS, Integer.toString(busyStatus));

			// Meeting status, 0 = appointment, 1 = meeting, 3 = attendee
			if (mEmailAddress.equalsIgnoreCase(organizerEmail)) {
				s.data(EmTags.CALENDAR_MEETING_STATUS, hasAttendees ? "1" : "0");
			} else {
				s.data(EmTags.CALENDAR_MEETING_STATUS, "3");
			}

			// For Exchange 2003, only upsync if the event is new
			if (((version >= EmEas.SUPPORTED_PROTOCOL_EX2007_DOUBLE) || !isChange)
					&& organizerName != null) {
				s.data(EmTags.CALENDAR_ORGANIZER_NAME, organizerName);
			}

			// NOTE: Sensitivity must NOT be sent to the server for exceptions
			// in Exchange 2003
			// The result will be a status 6 failure during sync
			Integer visibility = entityValues.getAsInteger(Events.VISIBILITY);
			if (visibility != null) {
				s.data(EmTags.CALENDAR_SENSITIVITY,
						decodeVisibility(visibility));
			} else {
				// Default to private if not set
				s.data(EmTags.CALENDAR_SENSITIVITY, "1");
			}
		}
	}

	/**
	 * Convenience method for sending an email to the organizer declining the
	 * meeting
	 * 
	 * @param entity
	 * @param clientId
	 */
	private void sendDeclinedEmail(Entity entity, String clientId) {
		Message msg = EmCalendarUtilities.createMessageForEntity(mContext,
				entity, Message.FLAG_OUTGOING_MEETING_DECLINE, clientId,
				mAccount);
		if (msg != null) {
			userLog("Queueing declined response to " + msg.mTo);
			mOutgoingMailList.add(msg);
		}
	}

	private void populateCalendarDetailsByevent(Event event, EmSerializer s,boolean isException) {
		// TODO Auto-generated method stub
		final Double version = mService.mProtocolVersionDouble;
		CalendarLog.d(CalendarConstants.Tag, "pop cal details for event "
				+ CalendarDatabaseHelper.getEventContentValues(event));
		try {
			if (event.title != null) {
				s.data(EmTags.CALENDAR_SUBJECT, event.getTitle());
				
			}

			if (event.organizer != null) {
				s.data(EmTags.CALENDAR_ORGANIZER_EMAIL, event.organizer);
				
			}

			if (event.calendarUID != null && !event.calendarUID.isEmpty())
				s.data(EmTags.CALENDAR_UID, event.calendarUID);
			if(!isException)
			{
			TimeZone eventTimeZone;
			
			CalendarLog.d(CalendarConstants.Tag, "clanedar time zone "+event.eventTimezone);
			if (event.eventTimezone != null && !event.eventTimezone.isEmpty()) {
				
					eventTimeZone=CalendarDatabaseHelper.getTimeZoneFromString(event.eventTimezone);
				CalendarLog.d(CalendarConstants.Tag,"event time zoe is "+eventTimeZone.getID());
			} else
				eventTimeZone = mLocalTimeZone;
			
			CalendarLog.d(CalendarConstants.Tag,"event time zoe is "+eventTimeZone.getID());

			// A time zone is required in all EAS events; we'll use the default
			// if none is set
			// Exchange 2003 seems to require this first...
			String timeZone = EmCalendarUtilities
					.timeZoneToTziStringImpl(eventTimeZone);
			
			CalendarLog.d(CalendarConstants.Tag, "timezone obt from string "+timeZone+" is "+EmCalendarUtilities
			.tziStringToTimeZone(timeZone).getID());

			if (timeZone != null)
				s.data(EmTags.CALENDAR_TIME_ZONE, timeZone);
			}
			s.data(EmTags.CALENDAR_ALL_DAY_EVENT, "" + event.allDay);

			s.data(EmTags.CALENDAR_START_TIME,
					CalendarDatabaseHelper.getDateForSyncing(event.dtstart));
			Log.e("serialixze",
					"date for syncing "
							+ CalendarDatabaseHelper
									.getDateForSyncing(event.dtstart));
			s.data(EmTags.CALENDAR_END_TIME,
					CalendarDatabaseHelper.getDateForSyncing(event.dtend));
			Log.e("serialixze",
					"date for syncing "
							+ CalendarDatabaseHelper
									.getDateForSyncing(event.dtend));

			if (event.eventLocation != null)
				s.data(EmTags.CALENDAR_LOCATION, event.eventLocation);
			if (event.description != null) {
				if (version >= EmEas.SUPPORTED_PROTOCOL_EX2007_DOUBLE) {
					CalendarLog.d(CalendarConstants.Tag, "version matches");
					s.start(EmTags.BASE_BODY);
					s.data(EmTags.BASE_TYPE, "1");
					s.data(EmTags.BASE_DATA, event.description);
					s.end();
				} else {
					// EAS 2.5 doesn't like bare line feeds
					CalendarLog.d(CalendarConstants.Tag, "version not matches");
					s.data(EmTags.CALENDAR_BODY, event.description);
				}
			}
			event.attendees = CalendarDatabaseHelper
					.getAttendeeValues((int) event._id);
			if (!event.attendees.isEmpty()) {
				s.start(EmTags.CALENDAR_ATTENDEES);
				for (Attendee attendee : event.attendees) {
					// for(int i=0;i<2;i++)
					// {
					s.start(EmTags.CALENDAR_ATTENDEE);
					if (attendee.attendeeName != null)
						s.data(EmTags.CALENDAR_ATTENDEE_NAME,
								attendee.attendeeName);
					else
						s.data(EmTags.CALENDAR_ATTENDEE_NAME, "");
					CalendarLog.d(CalendarConstants.Tag, "serialize "
							+ attendee.attendeeEmail);
					if (attendee.attendeeEmail != null)
						s.data(EmTags.CALENDAR_ATTENDEE_EMAIL,
								attendee.attendeeEmail);
					// s.data(EmTags.CALENDAR_ATTENDEE_NAME, "aishwarya");
					// s.data(EmTags.CALENDAR_ATTENDEE_EMAIL,
					// "abc@truboxmdmdev.com");
					if (version >= EmEas.SUPPORTED_PROTOCOL_EX2007_DOUBLE) {
						s.data(EmTags.CALENDAR_ATTENDEE_TYPE, "1"); // Required
					}
					s.end(); // Attendee
				}
				s.end(); // Attendees
			}

			ArrayList<ContentValues> reminderValues = CalendarDatabaseHelper
					.getReminderValues((int) event._id);
			if (!reminderValues.isEmpty()) {
				CalendarLog.d(
						CalendarConstants.Tag,
						"reminder values not empty "
								+ reminderValues.get(0).getAsInteger(
										Reminder.MINUTES));
				s.data(EmTags.CALENDAR_REMINDER_MINS_BEFORE,
						Integer.toString(reminderValues.get(0).getAsInteger(
								Reminder.MINUTES)));
				// s.data(EmTags.CALENDAR_REMINDER_MINS_BEFORE,
				// Integer.toString(120));

			} else {
				CalendarLog.d(CalendarConstants.Tag, "reminder values empty ");
			}
			int busyStatus = EmCalendarUtilities
					.busyStatusFromAvailability(event.availability);
			s.data(EmTags.CALENDAR_BUSY_STATUS, Integer.toString(busyStatus));

			if (event.accessibility != 0) {
				s.data(EmTags.CALENDAR_SENSITIVITY, "2");
			} else {
				// Default to private if not set
				s.data(EmTags.CALENDAR_SENSITIVITY, "1");
			}
			if ((!event.organizer.equalsIgnoreCase(CalendarDatabaseHelper.getEmail_ID())) && !event.attendees.isEmpty()) {
				s.data(EmTags.CALENDAR_MEETING_STATUS, "1");
			}

			if (event.rrule != null && !event.rrule.isEmpty()) {
				EmCalendarUtilities.recurrenceFromRrule(event.rrule,
						event.dtstart, s);
			}

		} catch (Exception e) {

			CalendarLog.e(CalendarConstants.Tag,
					"Excpetion while populating data " + e.toString());
		}
	}

	@Override
	public boolean sendLocalChanges(EmSerializer s) throws IOException {

		if (getSyncKey().equals("0")) {
			return false;
		}

		Log.d(TAG, "sendLocalChanges");
		// First, let's find Contacts that have changed.

		ArrayList<Event> addedEvents = CalendarDatabaseHelper
				.getAllAddedEvents();
		ArrayList<Event> changedEvents = CalendarDatabaseHelper
				.getAllModifiedEvents();
		ArrayList<String> deletedEvents = CalendarDatabaseHelper
				.getAllDeletedEvents();
		String folder_id = CalendarDatabaseHelper.getFolderIdForCalendar();
		// String client_id = "123456789";

		try {
			boolean first = true;

			if (first) {
				s.start(EmTags.SYNC_COMMANDS);
				CalendarLog.d(CalendarConstants.Tag,
						"Sending Calendar changes to the server");
				first = false;
			}

			if (!addedEvents.isEmpty()) {
				for (Event event : addedEvents) {
					String client_id = "" + event.event_id;
					CalendarLog.d(CalendarConstants.Tag,
							"Creating new contact with clientId: " + client_id);
					s.start(EmTags.SYNC_ADD).data(EmTags.SYNC_CLIENT_ID,
							client_id);
					s.start(EmTags.SYNC_APPLICATION_DATA);
					populateCalendarDetailsByevent(event, s,false);		
					if (event.exceptions != null && !event.exceptions.isEmpty()) {
						s.start(EmTags.CALENDAR_EXCEPTIONS);
						for (Exceptions exception : event.exceptions) {
							exception.calendarUID=null;
							exception.organizer=null;
							exception.eventTimezone=null;
							CalendarLog.d(CalendarConstants.Tag,"exceptions modified "+CalendarDatabaseHelper.getExceptionContentValues(exception).toString());
							s.start(EmTags.CALENDAR_EXCEPTION);
							if (exception.isExceptionDeleted == 1) {
								s.data(EmTags.CALENDAR_EXCEPTION_IS_DELETED,
										"1");		
								if(exception.exceptionDtStart!=0)
								{
									s.data(EmTags.CALENDAR_EXCEPTION_START_TIME,
					                        EmCalendarUtilities.millisToEasDateTime(exception.exceptionDtStart));
								}
								else
								s.data(EmTags.CALENDAR_EXCEPTION_START_TIME,
				                        EmCalendarUtilities.millisToEasDateTime(exception.dtstart));
							} else {
							
								populateCalendarDetailsByevent(
										CalendarDatabaseHelper
												.convertExceptionToEvent(exception),
										s,true);		
								if(exception.exceptionDtStart>0)
								{
								s.data(EmTags.CALENDAR_EXCEPTION_START_TIME,
				                        EmCalendarUtilities.millisToEasDateTime(exception.exceptionDtStart));
								}
								else
								{
									s.data(EmTags.CALENDAR_EXCEPTION_START_TIME,
					                        EmCalendarUtilities.millisToEasDateTime(exception.dtstart));
								}
							}
							s.end();
						}
						s.end();
					}
					s.end();
					s.end();
				}
			}
			if (!deletedEvents.isEmpty()) {
				for (String server_id_deleted : deletedEvents) {
					CalendarLog.d(CalendarConstants.Tag,
							"Deleting event with serverId: " + folder_id + ":"
									+ server_id_deleted);

					s.start(EmTags.SYNC_DELETE);
					s.data(EmTags.SYNC_SERVER_ID, folder_id + ":"
							+ server_id_deleted);
					s.end();
				}
			}

			if (!changedEvents.isEmpty()) {
				for (Event event : changedEvents) {
					String server_id_updates = "" + event.event_id;
					CalendarLog.d(CalendarConstants.Tag,
							"Upsync change to contact with serverId: "
									+ folder_id + ":" + server_id_updates);
					s.start(EmTags.SYNC_CHANGE).data(
							EmTags.SYNC_SERVER_ID,
							folder_id + ":" + server_id_updates);
					s.start(EmTags.SYNC_APPLICATION_DATA);
					populateCalendarDetailsByevent(event, s,false);
					CalendarLog.d(CalendarConstants.Tag,"events modified "+CalendarDatabaseHelper.getEventContentValues(event));
					CalendarLog.d(CalendarConstants.Tag, "size of exceptions "+event.exceptions.size());
					if (event.exceptions != null && !event.exceptions.isEmpty()) {
						s.start(EmTags.CALENDAR_EXCEPTIONS);
						for (Exceptions exception : event.exceptions) {
							exception.calendarUID=null;
							exception.organizer=null;
							exception.eventTimezone=null;
							CalendarLog.d(CalendarConstants.Tag,"exceptions modified "+CalendarDatabaseHelper.getExceptionContentValues(exception).toString());
							s.start(EmTags.CALENDAR_EXCEPTION);
							if (exception.isExceptionDeleted == 1) {
								s.data(EmTags.CALENDAR_EXCEPTION_IS_DELETED,
										"1");		
								if(exception.exceptionDtStart!=0)
								{
									s.data(EmTags.CALENDAR_EXCEPTION_START_TIME,
					                        EmCalendarUtilities.millisToEasDateTime(exception.exceptionDtStart));
								}
								else
								s.data(EmTags.CALENDAR_EXCEPTION_START_TIME,
				                        EmCalendarUtilities.millisToEasDateTime(exception.dtstart));
							} else {
							
								populateCalendarDetailsByevent(
										CalendarDatabaseHelper
												.convertExceptionToEvent(exception),
										s,true);		
								if(exception.exceptionDtStart>0)
								{
								s.data(EmTags.CALENDAR_EXCEPTION_START_TIME,
				                        EmCalendarUtilities.millisToEasDateTime(exception.exceptionDtStart));
								}
								else
								{
									s.data(EmTags.CALENDAR_EXCEPTION_START_TIME,
					                        EmCalendarUtilities.millisToEasDateTime(exception.dtstart));
								}
							}
							s.end();
						}
						s.end();
					}
					s.end();
					s.end();
				}

			}

			// Change
			// mUpdatedIdList.add(entityValues.getAsLong(RawContacts._ID));
			// }
			if (!first) {
				s.end(); // Commands
			}
		} catch (Exception e) {

			CalendarLog.e(CalendarConstants.Tag,
					"Exception " + e.toString());
		}

		return false;
	}
}
