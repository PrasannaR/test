package com.cognizant.trumobi.calendar.event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.text.util.Rfc822Tokenizer;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.TruBoxSDK.SharedPreferences;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.calendar.modal.Attendee;
import com.cognizant.trumobi.calendar.modal.Event;
import com.cognizant.trumobi.calendar.modal.Exceptions;
import com.cognizant.trumobi.calendar.modal.Recurrence;
import com.cognizant.trumobi.calendar.modal.Reminder;
import com.cognizant.trumobi.calendar.provider.CalendarDatabaseHelper;
import com.cognizant.trumobi.calendar.util.CalendarCommonFunction;
import com.cognizant.trumobi.calendar.util.CalendarConstants;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseSherlockActivity;
import com.cognizant.trumobi.em.EmEmailAddressAdapter;
import com.cognizant.trumobi.em.EmEmailAddressValidator;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.em.mail.EmAddress;
import com.cognizant.trumobi.em.provider.EmEmailContent.Account;
import com.cognizant.trumobi.exchange.provider.EmGalEmailAddressAdapter;
import com.cognizant.trumobi.log.CalendarLog;

public class CalendarAddEvent extends TruMobiBaseSherlockActivity {

	private int mFromYear, mFromMonth, mFromDay;
	private int mRecurrenceYear, mRecurrenceMonth, mRecurrenceDay;
	private int mFromHours = 0, mFromMins = 0;
	private int mToYear, mToMonth, mToDay;
	private int mToHours = 0, mToMins = 0, mDiffHours = 0, mDiffmMins = 0,
			mresponseType = Event.RESPONSE_TYPE_NONE;
	private int mSetSelectionMinutes;
	private Button mFromDate = null;
	private Button mFromTime = null;
	private Button mToDate = null;
	private Button mToTime = null;
	private Button mRepetition = null;

	private ArrayList<ContentValues> mReminderValues;
	private ArrayList<ContentValues> mSaveReminderValueList = new ArrayList<ContentValues>();
	private ContentValues mContentValueReminder;
	private LinearLayout mReminderLayout, mParentLayout, mReminderParentLayout;
	private ImageButton mBtn_cancel;
	private int i = 0;
	private TextView mTxt_edit_event_add_reminder;
	private SharedPreferences mSecurePrefs = null;

	private Spinner mSpinner_minutes, mSpinner_notification, timeZoneList,
			mShow_me_as_spinner, mPrivacy_spinner;

	private com.cognizant.trumobi.commonabstractclass.TruMobBaseEditText mTitle,
			mLocation, mDescription = null;
	private MultiAutoCompleteTextView mGuest = null;
	private CheckBox mAllDay = null;
	int mDatePicker = 0;
	private boolean mIsFromDate = true;
	private boolean isFromTime = true,mNewCal=false;
	private static final int DATE_DIALOG_ID = 10;
	static final int DATE_RECURRENCE_DIALOG_ID = 40;
	private static final int TIME_DIALOG_ID = 20;
	private static final int TIME_TO_DIALOG_ID = 30;
	private boolean flagNewEvent = true;
	private String selectedTimeZone;

	// CalendarScheduleClient scheduleClient = null;
	private Event mEvent;
	Recurrence mRecurrence;
	public String eventTitle, eventLocation, eventDescription, eventGuest,

	eventStartDate, eventEndDate, eventFromTime, eventToTime;

	int eventAllDay;
	private String[] mDayOfMonths = { "Jan", "Feb", "Mar", "Apr", "May", "Jun",
			"July", "Aug", "Sep", "Oct", "Nov", "Dec" };
	private ContentValues mEventContentValues;
	private Bundle mGetuserID;
	/** Get the current date */
	private Calendar mCal = Calendar.getInstance();
	private Calendar mEndCal = Calendar.getInstance();

	private EmEmailAddressAdapter mAddressAdapterTo;
	FrameLayout done;
	private Account mAccount;
	private boolean is24Hrs;
	private boolean isAM;
	private String am_pm;

	/*** Recurrence ***/
	ToggleButton mRecurrenceSwitch = null;
	Spinner mRecurrenceType = null, mRecurrenceRepeat = null;
	com.cognizant.trumobi.commonabstractclass.TruMobBaseEditText mRecurrenceNoOfDays = null,
			mRecurrenceRepeatDays = null;
	LinearLayout mRecurrenceDayRow = null, mRecurrenceWeekRow = null,
			mRecurrenctMonthRow = null, mRecurrencerepeatRow = null;
	Button mRecurrenceDone = null, mRecurrenceRepeatTime = null;
	RadioButton mRecurrenceSame = null, mRecurrenceEvery = null;
	ToggleButton mRecurrenceSun = null, mRecurrenceMon = null,
			mRecurrenceTue = null, mRecurrenceWed = null,
			mRecurrenceThur = null, mRecurrenceFri = null,
			mRecurrenceSat = null;
	TextView mRecurrenceDailyEvery = null, mRecurrenceDailyWeekDays = null,
			mRecurrenceRepeatDaysLable = null;
	Dialog mDialog = null;
	private int mRecurEditItem = -1;
	private String defaultReminderminuts, current_am_pm = "am";
	ArrayList<Attendee> mAttendee;

	private long syncEvent(Event newEvent) {
		long eventid = CalendarDatabaseHelper.insertEventToDatabase(newEvent);
		CalendarDatabaseHelper.manualSync();
		return eventid;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cal_newevent);
		mSecurePrefs = new SharedPreferences(Email.getAppContext());
		defaultReminderminuts = mSecurePrefs.getString(
				getString(R.string.key_default_reminderlist_preference), "");
		mGetuserID = getIntent().getExtras();
		mRecurrence = new Recurrence();
		checkTime();
		initialize();
		initDialog();
		setBundleValues();
		TextView account_label = (TextView) findViewById(R.id.account_label);
		account_label.setText(CalendarDatabaseHelper.getEmail_ID());
		Button account_button = (Button) findViewById(R.id.account_button);
		account_button.setText(CalendarDatabaseHelper.getEmail_ID());
		getSupportActionBar().hide();
		// To Cancel Add Event
		mParentLayout = (LinearLayout) findViewById(R.id.reminder_multiple_row);
		mAllDay = (CheckBox) findViewById(R.id.is_all_day);
		addReminderView();
		FrameLayout cancel = (FrameLayout) findViewById(R.id.actionbar_discard);
		cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Email.setEditFlag(false);
				CalendarAddEvent.this.finish();
				// onBackPressed();

			}
		});

		mRepetition = (Button) findViewById(R.id.repetition_button);
		mRepetition.setOnClickListener(btn_recurrence_listener);
		// mRepetition = (Spinner) findViewById(R.id.repetition_button);
		// mRepetition.setAdapter(populateItems(getResources().getStringArray(
		// R.array.repetition_methods_labels)));

		done = (FrameLayout) findViewById(R.id.actionbar_done);
		done.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				checkAndSaveEvent(v, false);

			}
		});
		// To Show from date dialog
		mFromDate = (Button) findViewById(R.id.from_date);
		mFromDate.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mDatePicker = 0;
				showDialog(DATE_DIALOG_ID);

			}
		});

		mFromTime = (Button) findViewById(R.id.from_time);
		mFromTime.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				isFromTime = true;
				showDialog(TIME_DIALOG_ID);

			}
		});

		mToDate = (Button) findViewById(R.id.to_date);
		mToDate.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mDatePicker = 1;
				showDialog(DATE_DIALOG_ID);

			}
		});

		mToTime = (Button) findViewById(R.id.to_time);
		mToTime.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				isFromTime = false;
				showDialog(TIME_TO_DIALOG_ID);

			}
		});

		mAllDay.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked) {
					mFromTime.setVisibility(View.GONE);
					mToTime.setVisibility(View.GONE);
				} else {
					mFromTime.setVisibility(View.VISIBLE);
					mToTime.setVisibility(View.VISIBLE);
				}

			}
		});
		/*
		 * PreferenceManager . getDefaultSharedPreferences ( CalendarAddEvent
		 * .this);
		 */
		if (mGetuserID != null) {
			setCurrentCalendar();

		} else {
			mFromYear = mCal.get(Calendar.YEAR);
			mFromMonth = mCal.get(Calendar.MONTH);
			mFromDay = mCal.get(Calendar.DAY_OF_MONTH);

			mFromHours = mCal.get(Calendar.HOUR_OF_DAY);
			mFromMins = mCal.get(Calendar.MINUTE);

			mToYear = mCal.get(Calendar.YEAR);
			mToMonth = mCal.get(Calendar.MONTH);
			mToDay = mCal.get(Calendar.DAY_OF_MONTH);

			mToHours = mCal.get(Calendar.HOUR_OF_DAY);
			mToHours++;
			mToMins = mCal.get(Calendar.MINUTE);

			mDiffHours = mToHours - mFromHours;
			mDiffmMins = mToMins - mFromMins;

			mRecurrenceYear = mCal.get(Calendar.YEAR);
			mRecurrenceMonth = mCal.get(Calendar.MONTH);
			mRecurrenceDay = mCal.get(Calendar.DAY_OF_MONTH);

			String defaultminuts = mSecurePrefs
					.getString(
							getString(R.string.key_default_reminderlist_preference),
							"");
			if (defaultminuts.equalsIgnoreCase("")) {
				defaultminuts = "0";
			}

			mReminderParentLayout.addView(addChildEventReminderView(
					defaultminuts, "4"));
			if (!defaultReminderminuts.equals("None")) {
				mReminderParentLayout.setVisibility(View.VISIBLE);
			}
		}

		/** Display the current date in the TextView */
		updateDisplay();
		updateToDate();
		updateTime();
		updateToTime();
		updateRecurrenceDate();
	}

	protected void checkAndSaveEvent(View v, boolean backButtonClicked) {
		try {
			mNewCal = false;
			String saveMessage = CalendarConstants.EVENT_ADDED_SUCCESSFULLY;
			if (!backButtonClicked) {
				v.setEnabled(false);
			}

			Email.setEditFlag(false);
			// Calendar c = Calendar.getInstance();
			// c.set(mYear, mMonth, mDay);
			// c.set(Calendar.HOUR_OF_DAY, mFromHours);
			// if (!mAllDay.isChecked())
			// c.set(Calendar.MINUTE, mFromMins);
			LinearLayout reme = (LinearLayout) mParentLayout.getChildAt(0);

			setEventScreen();
			if (validateEvent()) {
				Toast.makeText(getApplicationContext(),
						CalendarConstants.EVENT_VALIDATION, Toast.LENGTH_SHORT)
						.show();
				if (!backButtonClicked) {
					v.setEnabled(true);
				}
				return;
			}
			if (mAllDay.isChecked()) {
				eventFromTime = "00:00";
				eventToTime = "24:00";
			}
			long startTime = CalendarDatabaseHelper.getLongFromTime(
					eventStartDate + " " + eventFromTime, timeZoneList
							.getSelectedItem().toString());
			long endTime = CalendarDatabaseHelper.getLongFromTime(eventEndDate
					+ " " + eventToTime, timeZoneList.getSelectedItem()
					.toString());
			ArrayList<Attendee> attendees = new ArrayList<Attendee>();
			EmAddress[] emailAddress = getAddresses(mGuest);
			boolean isAttendee = false;
			for (EmAddress email : emailAddress) {
				isAttendee = true;
				Attendee attendee = new Attendee();

				CalendarLog.d(CalendarConstants.Tag,
						"Email id added in attendees " + email.getAddress());
				attendee.setAttendeeEmail(email.getAddress());
				attendees.add(attendee);
			}

			CalendarLog.d(CalendarConstants.Tag,
					"number of attendees in event " + attendees.size());

			CalendarLog.d(CalendarConstants.Tag, "show_me_as_spinner "
					+ mShow_me_as_spinner.getSelectedItemId());

			String[] avalibityShow = getResources().getStringArray(
					R.array.show_me_as_suggestion_value);
			int selectedshow = Integer
					.parseInt(avalibityShow[(int) mShow_me_as_spinner
							.getSelectedItemId()]);
			CalendarLog.d(CalendarConstants.Tag, "Edit set add" + selectedshow);

			Event newevent = new Event(0, CalendarDatabaseHelper.getEmail_ID(),
					eventTitle, eventLocation, eventDescription,
					CalendarDatabaseHelper.getDateFromLong(startTime),
					CalendarDatabaseHelper.getDateFromLong(endTime), startTime,
					endTime, timeZoneList.getSelectedItem().toString(),
					timeZoneList.getSelectedItem().toString(),
					Event.STATUS_ADDED, Event.RESPONSE_TYPE_NONE, eventAllDay,
					null, null, null, null, null, attendees, null,
					selectedshow, (int) mPrivacy_spinner.getSelectedItemId(),
					false, false, false);

			if (isAttendee)
				saveMessage = CalendarConstants.EVENT_INVITE_CREATED;

			CalendarLog.d(CalendarConstants.Tag, "");
			if (mEventContentValues != null) {
				saveMessage = CalendarConstants.EVENT_SAVED_SUCCESSFULLY;
				if (isAttendee)
					saveMessage = CalendarConstants.EVENT_INVITE_UPDATE;
				newevent._id = mEventContentValues.getAsInteger("_id");
				newevent.event_id = mEventContentValues
						.getAsInteger(Event.EVENT_ID);
				newevent.setStatus(Event.STATUS_MODIFIED);
				newevent.setResponseType(mresponseType);
				newevent.calendarUID = mEventContentValues
						.getAsString(Event.CALENDAR_UID);
				if(mRecurrence==null)
					newevent.rrule="";
			}

			newevent.recurrence = mRecurrence;			
			if (mEventContentValues != null) {
				if (mEventContentValues
						.containsKey(CalendarConstants.RECURRENCE_CURRENTDATE)) {
					if (mRecurEditItem == 2) {
						/** For Editing the Future recurrence event **/
						String recurrCurrendate = mEventContentValues
								.getAsString(CalendarConstants.RECURRENCE_CURRENTDATE);
//						String[] days = recurrCurrendate.split(" ");
						// Log.e("check time","check s"+days[0] + " -" +date);

						Calendar cal = Calendar.getInstance();
						cal.setFirstDayOfWeek(Calendar.SUNDAY);
						SimpleDateFormat sdf = new SimpleDateFormat(
								"dd MMM yyyy", Locale.US);
						try {
							cal.setTime(sdf.parse(recurrCurrendate));
						} catch (ParseException e) {
							e.printStackTrace();
						}// all done
							// cal.set(Calendar.DAY_OF_MONTH,
							// Integer.valueOf(days[0]));
						// cal.set(Calendar.MONTH, cal.get(Calendar.MONTH));
						// cal.set(Calendar.YEAR, Integer.valueOf(days[2]));
						cal.add(Calendar.DAY_OF_MONTH, -1);
						// newevent.dte

						Event exceptionEvent = CalendarDatabaseHelper
								.getEventDetails(mEventContentValues
										.getAsLong("_id"));
						if(!newevent.startDate.equalsIgnoreCase(exceptionEvent.startDate)){
							/***  If user choose for first/starting occurence of event ***/
//							CalendarLog.d(CalendarConstants.Tag, "This not equal "+exceptionEvent.startDate+" - "+recurrCurrendate +" - "+ recurrentEditDate + " - " + newevent.startDate);
						newevent._id = 0;
						newevent.event_id = 0;
						newevent.setStatus(Event.STATUS_ADDED);
						newevent.setResponseType(Event.RESPONSE_TYPE_NONE);
						newevent.rrule = "";
						newevent.calendarUID = null;

						
						Recurrence recurrence = exceptionEvent.recurrence;
						recurrence.until = cal.getTimeInMillis();
						 recurrence.occurences = -1;
						exceptionEvent.recurrence = recurrence;
						exceptionEvent.rrule = "";
						exceptionEvent.setStatus(Event.STATUS_MODIFIED);
						
							syncEvent(exceptionEvent);
						}
					} else if (mRecurEditItem == 0) {
						CalendarLog.d(CalendarConstants.Tag,
								"Recurrence Only this Event");
						/****** For Editing Recurrence Only this Event ****/
						String recurrCurrendate = mEventContentValues
								.getAsString(CalendarConstants.RECURRENCE_CURRENTDATE);
						String[] days = recurrCurrendate.split(" ");
						// Log.e("check time","check s"+days[0] + " -" +date);

						Calendar cal = Calendar.getInstance();
						cal.setFirstDayOfWeek(Calendar.SUNDAY);
						SimpleDateFormat sdf = new SimpleDateFormat(
								"dd MMM yyyy", Locale.US);
						try {
							cal.setTime(sdf.parse(recurrCurrendate));
						} catch (ParseException e) {
							e.printStackTrace();
						}// all done
						cal.set(Calendar.DAY_OF_MONTH, Integer.valueOf(days[0]));
						cal.set(Calendar.MONTH, cal.get(Calendar.MONTH));
						cal.set(Calendar.YEAR, Integer.valueOf(days[2]));
						Calendar calStart = Email.getNewCalendar();
						calStart.setFirstDayOfWeek(Calendar.SUNDAY);
						calStart.setTimeInMillis(newevent.dtstart);
						calStart.set(Calendar.DAY_OF_MONTH,
								Integer.valueOf(days[0]));
						calStart.set(Calendar.MONTH, cal.get(Calendar.MONTH));
						calStart.set(Calendar.YEAR, Integer.valueOf(days[2]));
						newevent.hasException = 1;

						Exceptions exceptions = new Exceptions();
						exceptions._id = newevent._id;
						exceptions.accessibility = newevent.accessibility;
						exceptions.allDay = exceptions.allDay;
						exceptions.attendees = newevent.getAttendees();
						exceptions.bg_color = newevent.bg_color;
						exceptions.calendarUID = newevent.calendarUID;
						exceptions.description = newevent.description;
						exceptions.dtend = newevent.dtend;
						exceptions.dtstart = calStart.getTimeInMillis();
						exceptions.endDate = newevent.endDate;
						exceptions.event_id = newevent.event_id;
						exceptions.eventEndTimezone = newevent.eventEndTimezone;
						exceptions.eventLocation = newevent.eventLocation;
						exceptions.eventTimezone = newevent.eventTimezone;
						exceptions.isExceptionDeleted = 0;
						exceptions.exdate = exceptions.exdate;
						exceptions.exrule = newevent.exrule;
						exceptions.title = newevent.title;
						exceptions.organizer = newevent.organizer;
						exceptions.rdate = newevent.rdate;
						exceptions.startDate = CalendarCommonFunction
								.convertMilliSecondsToDate(cal
										.getTimeInMillis());
						ArrayList<Exceptions> exceptionsArray = new ArrayList<Exceptions>();
						exceptionsArray.add(exceptions);
						newevent = CalendarDatabaseHelper
								.getEventDetails(newevent._id);
						newevent.exceptions = exceptionsArray;
						newevent.setStatus(Event.STATUS_MODIFIED);
						newevent.hasException = 1;
					}
				}
			}
			ArrayList<Reminder> reminders = new ArrayList<Reminder>();
//				newevent._id = (int) eventid;
//				CalendarDatabaseHelper.deleteRemindersForEvent((int) eventid);
				// cancelEventsNotification(newevent);
				for (i = 0; i < reme.getChildCount(); i++) {
					LinearLayout childReme = (LinearLayout) reme.getChildAt(i);
					Spinner spMinutes = (Spinner) childReme.getChildAt(0);
					Spinner spEmail = (Spinner) childReme.getChildAt(1);
					int reminder = (int) spMinutes.getSelectedItemId();
					int notification = (int) spEmail.getSelectedItemId();
					final String[] reminderminutes = getResources()
							.getStringArray(R.array.reminder_minutes_values);
					reminder = Integer.parseInt(reminderminutes[reminder]);

					int notificationValue;
					if (notification == 0) {
						notificationValue = 4;
						// c.add(Calendar.MINUTE,
						// -Integer.parseInt(reminderminutes[reminder]));

						// addEventsNotification(c, newevent, post);
					} else {
						notificationValue = 2;
					}
//					CalendarDatabaseHelper
//							.changeStatusToModified((int) eventid);
					reminders.add(new Reminder(-1, reminder, notificationValue));
//					CalendarDatabaseHelper.insertReminderToDatabase(
//							new Reminder((int) eventid, reminder,
//									notificationValue), true);// Integer.parseInt(reminderminutes[reminder])

				}
				newevent.reminders = reminders;
				boolean shouldBeInserted = false;
				if (mEventContentValues != null) {

					Event oldEvent = CalendarDatabaseHelper
							.getEventFromContentValues(mEventContentValues);
					oldEvent.attendees = mAttendee;
					oldEvent.reminders = CalendarDatabaseHelper
							.convertReminderContentValuesToReminder(mReminderValues);
					oldEvent.recurrence = CalendarDatabaseHelper
							.getRecursionForEvent((int) oldEvent._id);
					oldEvent.exceptions=CalendarDatabaseHelper.getExceptionsForEvent((int)oldEvent._id);
					if (CalendarDatabaseHelper.isEqualEvent(oldEvent, newevent)) {
						// Toast.makeText(getApplicationContext(),
						// "Event not changed", Toast.LENGTH_SHORT).show();
						CalendarLog.d(CalendarConstants.Tag,
								"Event not inserted : no change");
					} else {
						shouldBeInserted = true;
					}

				} else
					shouldBeInserted = true;
				if (shouldBeInserted) {
					long eventid = syncEvent(newevent);
					if (eventid > 0) {
						// for (Reminder reminder : reminders) {
						// CalendarDatabaseHelper.insertReminderToDatabase(reminder,
						// true);// Integer.parseInt(reminderminutes[reminder])
						// }
				Toast.makeText(getApplicationContext(), saveMessage,
						Toast.LENGTH_SHORT).show();
//				Email.setEditFlag(false);
//				CalendarAddEvent.this.finish();// onBackPressed();
			} else {

				if (!backButtonClicked) {
					v.setEnabled(true);
				}
				Toast.makeText(getApplicationContext(),
						CalendarConstants.EVENT_NOT_ADDED, Toast.LENGTH_SHORT)
						.show();
				Email.setEditFlag(false);
				CalendarAddEvent.this.finish();// onBackPressed();
			}
				}
				Email.setEditFlag(false);
				CalendarAddEvent.this.finish();
		} catch (Exception e) {
			if (!backButtonClicked) {
				v.setEnabled(true);
			}
			e.printStackTrace();
		}
	}

	private void checkTime() {
		String userSettings = DateFormat.getTimeFormat(Email.getAppContext())
				.format(Email.getNewCalendar().getTime());
		is24Hrs = true;
		if (userSettings.indexOf("PM") != -1) {
			is24Hrs = false;
			am_pm = "pm";
			current_am_pm = "pm";
		} else if (userSettings.indexOf("AM") != -1) {
			is24Hrs = false;
			am_pm = "am";
			current_am_pm = "am";
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Email.setEditFlag(false);
		checkAndSaveEvent(done, true);

	}

	private static EmAddress[] getAddresses(TextView view) {
		EmAddress[] addresses = EmAddress.parse(view.getText().toString()
				.trim());
		return addresses;
	}

	/**
	 * Used to set calendar
	 */
	private void setCurrentCalendar() {

		if (Email.isEditFlag()) {
			try {

				mAttendee = CalendarDatabaseHelper
						.getAttendeeValues(mEventContentValues
								.getAsInteger("_id"));
				addAddresses(mGuest, mAttendee);
				mRecurrence = CalendarDatabaseHelper
						.getRecursionForEvent(mEventContentValues
								.getAsInteger("_id"));
				mDescription.setText(mEventContentValues
						.getAsString(Event.DESCRIPTION));

				String[] reminderminutes = getResources().getStringArray(
						R.array.show_me_as_suggestion_value);
				int selectedshow = mEventContentValues
						.getAsInteger(Event.AVAILABILITY);

				for (int i = 0; i < reminderminutes.length; i++) {
					if (reminderminutes[i].equalsIgnoreCase(String
							.valueOf(selectedshow))) {
						selectedshow = i;
						break;
					}
				}
				CalendarLog.d(CalendarConstants.Tag, "Edit set" + selectedshow);
				mShow_me_as_spinner.setSelection(selectedshow);
				mPrivacy_spinner.setSelection(mEventContentValues
						.getAsInteger(Event.ACCESSIBILITY));
				String startingDate = CalendarDatabaseHelper.getDateFromLong(
						mEventContentValues.getAsLong(Event.DTSTART),
						mEventContentValues.getAsString(Event.EVENT_TIMEZONE));

				String[] splitStr = CalendarDatabaseHelper
						.getDateFormatForAgenda(startingDate).split("\\s+");
				String startDate = splitStr[0] + " " + splitStr[1] + " "
						+ splitStr[2];
				String[] splitTime = CalendarDatabaseHelper.getTimeFromLong(
						mEventContentValues.getAsLong(Event.DTSTART),
						mEventContentValues.getAsString(Event.EVENT_TIMEZONE))
						.split(":");

				mCal = Email.getCurrentDay(startDate);
				mCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitTime[0]));
				mCal.set(Calendar.MINUTE, Integer.parseInt(splitTime[1]));

				mFromYear = mCal.get(Calendar.YEAR);
				mFromMonth = mCal.get(Calendar.MONTH);
				mFromDay = mCal.get(Calendar.DAY_OF_MONTH);

				mFromHours = mCal.get(Calendar.HOUR_OF_DAY);
				mFromMins = mCal.get(Calendar.MINUTE);

				long mRecurrenceUntill = mEventContentValues
						.getAsLong(Recurrence.UNTIL);
				if (mRecurrenceUntill > -1) {
					Calendar recurrenceCal = Email.getNewCalendar();
					recurrenceCal.setTimeInMillis(mRecurrenceUntill);
					mRecurrenceYear = recurrenceCal.get(Calendar.YEAR);
					mRecurrenceMonth = recurrenceCal.get(Calendar.MONTH);
					mRecurrenceDay = recurrenceCal.get(Calendar.DAY_OF_MONTH);
				} else {
					mRecurrenceYear = mCal.get(Calendar.YEAR);
					mRecurrenceMonth = mCal.get(Calendar.MONTH);
					mRecurrenceDay = mCal.get(Calendar.DAY_OF_MONTH);
				}
				updateRecurrenceDate();
				initRecurrence();
				String recurreanceText = getRecurrenceValue();
				mRepetition.setText(recurreanceText);
				String endingDate = CalendarDatabaseHelper.getDateFromLong(
						mEventContentValues.getAsLong(Event.DTEND),
						mEventContentValues.getAsString(Event.EVENT_TIMEZONE));

				String[] splitEndDate = CalendarDatabaseHelper
						.getDateFormatForAgenda(endingDate).split("\\s+");
				String endDate = splitEndDate[0] + " " + splitEndDate[1] + " "
						+ splitEndDate[2];
				String[] splitEndTime = CalendarDatabaseHelper.getTimeFromLong(
						mEventContentValues.getAsLong(Event.DTEND),
						mEventContentValues.getAsString(Event.EVENT_TIMEZONE))
						.split(":");
				mEndCal = Email.getCurrentDay(endDate);
				mEndCal.set(Calendar.HOUR_OF_DAY,
						Integer.parseInt(splitEndTime[0]));
				mEndCal.set(Calendar.MINUTE, Integer.parseInt(splitEndTime[1]));

				mToYear = mEndCal.get(Calendar.YEAR);
				mToMonth = mEndCal.get(Calendar.MONTH);
				mToDay = mEndCal.get(Calendar.DAY_OF_MONTH);

				mToHours = mEndCal.get(Calendar.HOUR_OF_DAY);
				mToMins = mEndCal.get(Calendar.MINUTE);

				mDiffHours = mToHours - mFromHours;
				mDiffmMins = mToMins - mFromMins;

				mEvent = new Event();
				mEvent.setAttendees(mAttendee);
				if (!mEventContentValues.getAsString(Event.ORGANIZER)
						.equalsIgnoreCase(CalendarDatabaseHelper.getEmail_ID())) {
					LinearLayout responserow = (LinearLayout) findViewById(R.id.response_row);
					responserow.setVisibility(View.VISIBLE);
					int response = mEventContentValues
							.getAsInteger(Event.RESPONSE_TYPE);
					if (response == Event.RESPONSE_TYPE_ACCEPTED) {
						RadioButton responsetype = (RadioButton) findViewById(R.id.response_yes);
						responsetype.setChecked(true);
					} else if (response == Event.RESPONSE_TYPE_REJECTED) {
						RadioButton responsetype = (RadioButton) findViewById(R.id.response_no);
						responsetype.setChecked(true);
					} else if (response == Event.RESPONSE_TYPE_TENTATIVE) {
						RadioButton responsetype = (RadioButton) findViewById(R.id.response_maybe);
						responsetype.setChecked(true);
					}
				}
				if (mEventContentValues.getAsString(Event.ALL_DAY)
						.equalsIgnoreCase("1")) {
					mAllDay.setChecked(true);
					mEndCal.add(Calendar.MINUTE, -1);
				}
				mToYear = mEndCal.get(Calendar.YEAR);
				mToMonth = mEndCal.get(Calendar.MONTH);
				mToDay = mEndCal.get(Calendar.DAY_OF_MONTH);
				mToHours = mEndCal.get(Calendar.HOUR_OF_DAY);
				mToMins = mEndCal.get(Calendar.MINUTE);

				mDiffHours = mToHours - mFromHours;
				mDiffmMins = mToMins - mFromMins;

				if (!mEventContentValues.getAsString(Recurrence.TYPE)
						.equalsIgnoreCase("-1")) {
					showRecurrenceDialog("Details");
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			mCal = Email.getCurrentDay();
			int Hours = getIntent().getExtras().getInt("Hours", 0);
			int daysadd = getIntent().getExtras().getInt("daysneedtoadd", -30);
			int daycou = daysadd - mCal.get(Calendar.DAY_OF_WEEK);
			daycou++;
			int day = getIntent().getExtras().getInt("NewDay", 0);
			int month = getIntent().getExtras().getInt("NewMonth", 0);
			int year = getIntent().getExtras().getInt("NewYear", 0);
			if (day != 0)
				mCal.set(Calendar.DATE, day);
			if (month != 0)
				mCal.set(Calendar.MONTH, month);
			if (year != 0)
				mCal.set(Calendar.YEAR, year);
			if (daysadd != -30)
				mCal.add(Calendar.DAY_OF_YEAR, daycou);
			if (Hours != 0)
				mCal.set(Calendar.HOUR_OF_DAY, Hours);
			mCal.set(Calendar.MINUTE, 0);
			mFromYear = mCal.get(Calendar.YEAR);
			mFromMonth = mCal.get(Calendar.MONTH);
			mFromDay = mCal.get(Calendar.DAY_OF_MONTH);
			mFromHours = mCal.get(Calendar.HOUR_OF_DAY);
			mFromMins = mCal.get(Calendar.MINUTE);

			mToYear = mCal.get(Calendar.YEAR);
			mToMonth = mCal.get(Calendar.MONTH);
			mToDay = mCal.get(Calendar.DAY_OF_MONTH);
			mToHours = mCal.get(Calendar.HOUR_OF_DAY);
			mToHours++;
			mToMins = mCal.get(Calendar.MINUTE);

			mDiffHours = mToHours - mFromHours;
			mDiffmMins = mToMins - mFromMins;

			mRecurrenceYear = mCal.get(Calendar.YEAR);
			mRecurrenceMonth = mCal.get(Calendar.MONTH);
			mRecurrenceDay = mCal.get(Calendar.DAY_OF_MONTH);

			String defaultminuts = mSecurePrefs
					.getString(
							getString(R.string.key_default_reminderlist_preference),
							"");
			if (defaultminuts.equalsIgnoreCase("")) {
				defaultminuts = "0";
			}
			mReminderParentLayout.addView(addChildEventReminderView(
					defaultminuts, "4"));
			if (!defaultReminderminuts.equals("None")) {
				mReminderParentLayout.setVisibility(View.VISIBLE);
			}
		}
	}

	private void addAddresses(MultiAutoCompleteTextView view,
			ArrayList<Attendee> attendee) {

		for (Attendee address : attendee) {
			addAddress(view, address.getAttendeeEmail().toString());
		}
	}

	private static void addAddress(MultiAutoCompleteTextView view,
			String address) {
		view.append(address + ", ");
	}

	/**
	 * Used to set value on edit event click
	 */
	private void setBundleValues() {
		if (Email.isEditFlag() && (mGetuserID != null)) {
			mEventContentValues = mGetuserID
					.getParcelable("EventContentValues");
			if (mEventContentValues == null)
				return;
			mTitle.setText(mEventContentValues.getAsString(Event.TITLE));
			mLocation.setText(mEventContentValues
					.getAsString(Event.EVENT_LOCATION));
			mReminderValues = CalendarDatabaseHelper
					.getReminderValues(mEventContentValues.getAsInteger("_id"));

			String timeZone = mEventContentValues
					.getAsString(Event.EVENT_TIMEZONE);
			CalendarLog.d(CalendarConstants.Tag,
					"timezone obtained for event is " + timeZone);
			// timeZoneList.setSelection(Arrays.asList(
			// this.getResources().getStringArray(
			// R.array.preference_home_time_zone_label)).indexOf(
			// timeZone));
			timeZoneList.setSelection(CalendarDatabaseHelper
					.getTimeZoneOffsetIndex(CalendarDatabaseHelper
							.getTimeZoneOffsetFromString(timeZone)));

		}
	}

	private void initialize() {
		mTitle = (com.cognizant.trumobi.commonabstractclass.TruMobBaseEditText) findViewById(R.id.event);
		mLocation = (com.cognizant.trumobi.commonabstractclass.TruMobBaseEditText) findViewById(R.id.location);
		mGuest = (MultiAutoCompleteTextView) findViewById(R.id.guests);
		mDescription = (com.cognizant.trumobi.commonabstractclass.TruMobBaseEditText) findViewById(R.id.description);
		mAllDay = (CheckBox) findViewById(R.id.is_all_day);
		mShow_me_as_spinner = (Spinner) findViewById(R.id.show_me_as_spinner);
		mPrivacy_spinner = (Spinner) findViewById(R.id.privacy_spinner);
		TextWatcher watcher = new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start,
					int before, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			public void afterTextChanged(android.text.Editable s) {
			}
		};
		InputFilter recipientFilter = new InputFilter() {

			public CharSequence filter(CharSequence source, int start, int end,
					Spanned dest, int dstart, int dend) {

				// quick check - did they enter a single space?
				if (end - start != 1 || source.charAt(start) != ' ') {
					return null;
				}

				// determine if the characters before the new space fit the
				// pattern
				// follow backwards and see if we find a comma, dot, or @
				int scanBack = dstart;
				boolean dotFound = false;
				while (scanBack > 0) {
					char c = dest.charAt(--scanBack);
					switch (c) {
					case '.':
						dotFound = true; // one or more dots are req'd
						break;
					case ',':
						return null;
					case '@':
						if (!dotFound) {
							return null;
						}

						// we have found a comma-insert case. now just do it
						// in the least expensive way we can.
						if (source instanceof Spanned) {
							SpannableStringBuilder sb = new SpannableStringBuilder(
									",");
							sb.append(source);
							return sb;
						} else {
							return ", ";
						}
					default:
						// just keep going
					}
				}

				// no termination cases were found, so don't edit the input
				return null;
			}
		};
		InputFilter[] recipientFilters = new InputFilter[] { recipientFilter };
		mGuest.addTextChangedListener(watcher);
		// NOTE: assumes no other filters are set
		mGuest.setFilters(recipientFilters);

		EmEmailAddressValidator addressValidator = new EmEmailAddressValidator();

		setupAddressAdapters();
		mGuest.setAdapter(mAddressAdapterTo);
		mGuest.setTokenizer(new Rfc822Tokenizer());
		mGuest.setValidator(addressValidator);
		initListener();
		CalendarLog.d(CalendarConstants.Tag, "Account set");
		long accountId = 1;
		Account account = null;
		if (accountId == -1) {
			CalendarLog.d(CalendarConstants.Tag, "Account -1");
			accountId = Account.getDefaultAccountId(this);
		}
		if (accountId != -1) {
			// Make sure it exists...
			CalendarLog.d(CalendarConstants.Tag, "Account not -1");
			account = Account.restoreAccountWithId(this, accountId);
			// Deleted account is no account...
		}
		if (accountId == -1 || account == null) {
			CalendarLog.d(CalendarConstants.Tag, "null");
		} else {
			mAccount = account;
			mAddressAdapterTo.setAccount(account);

		}

		timeZoneList = (Spinner) findViewById(R.id.timezone_button);
		timeZoneList.setAdapter(populateItems(getResources().getStringArray(
				R.array.preference_home_time_zone_label)));
		// int selectedVlaue = Arrays.asList(
		// this.getResources().getStringArray(
		// R.array.preference_home_time_zone_label)).indexOf(
		// mSecurePrefs
		// .getString(CalendarConstants.PREF_TIME_ZONE_KEY,
		// CalendarCommonFunction
		// .getDeviceCurrentTimezoneOffset()));
		// timeZoneList.setSelection(selectedVlaue);
		int selectedVlaue = Arrays
				.asList(this.getResources().getStringArray(
						R.array.preference_home_time_zone_label))
				.indexOf(
						mSecurePrefs
								.getString(
										getString(R.string.key_home_time_zone_preference),
										CalendarCommonFunction
												.getDeviceCurrentTimezoneOffset()));
		CalendarLog.d(CalendarConstants.Tag, "cal opffset of default "
				+ CalendarCommonFunction.getDeviceCurrentTimezoneOffset());
		if (selectedVlaue == -1) {
			selectedVlaue = CalendarDatabaseHelper
					.getTimeZoneOffsetIndex(CalendarCommonFunction
							.getDeviceCurrentTimezoneOffset());
		}
		CalendarLog.d(CalendarConstants.Tag, "select timezone is "
				+ selectedVlaue + " ");
		timeZoneList.setSelection(selectedVlaue);
		timeZoneList.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {

				selectedTimeZone = getResources().getStringArray(
						R.array.preference_home_time_zone_label)[position];

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}

		});
	}

	/**
	 * Set up address auto-completion adapters.
	 */
	private void setupAddressAdapters() {
		/* EXCHANGE-REMOVE-SECTION-START */
		if (true) {
			mAddressAdapterTo = new EmGalEmailAddressAdapter(this);
		} 
//		else {
//			/* EXCHANGE-REMOVE-SECTION-END */
//			mAddressAdapterTo = new EmEmailAddressAdapter(this);
//			/* EXCHANGE-REMOVE-SECTION-START */
//		}
		/* EXCHANGE-REMOVE-SECTION-END */
	}

	private void initListener() {
		// show me as spinner
		mShow_me_as_spinner.setAdapter(new ArrayAdapter<String>(this,
				R.layout.cal_spinner_add_event, getResources().getStringArray(
						R.array.show_me_as_suggestion)));
		mPrivacy_spinner.setAdapter(new ArrayAdapter<String>(this,
				R.layout.cal_spinner_add_event, getResources().getStringArray(
						R.array.privacy_suggestion)));

		mGuest.addTextChangedListener(mWatcher);
	}

	/**
	 * Watches the to, cc, bcc, subject, and message body fields.
	 */
	private final TextWatcher mWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int before,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// mSubjectView.setError(null);
			// mMessageContentView.setError(null);
			// setMessageChanged(true);
		}

		@Override
		public void afterTextChanged(android.text.Editable s) {
		}
	};

	private boolean validateEvent() {
		if (eventTitle.trim().equalsIgnoreCase("")
				&& eventLocation.trim().equalsIgnoreCase("")
				&& eventDescription.trim().equalsIgnoreCase("")) {
			return true;

		}
		return false;
	}

	/** Get edit text Ids for event screen **/

	private void setEventScreen() {
		eventTitle = mTitle.getText().toString();
		eventLocation = mLocation.getText().toString();
		eventStartDate = mFromDate.getText().toString();
		eventEndDate = mToDate.getText().toString();

		eventDescription = mDescription.getText().toString();
//		eventFromTime = mFromTime.getText().toString();
//		eventToTime = mToTime.getText().toString();
//CalendarLog.d(CalendarConstants.Tag, eventFromTime);
		 eventFromTime = mFromHours+":"+mFromMins;
		 eventToTime = mToHours+":"+mToMins;

		if (mAllDay.isChecked()) {
			eventAllDay = 1;
		} else {
			eventAllDay = 0;
		}
		RadioButton responseyes = (RadioButton) findViewById(R.id.response_yes);
		RadioButton responseno = (RadioButton) findViewById(R.id.response_no);
		RadioButton responsemaybe = (RadioButton) findViewById(R.id.response_maybe);
		if (responseyes.isChecked()) {
			mresponseType = Event.RESPONSE_TYPE_ACCEPTED;
		} else if (responseno.isChecked()) {
			mresponseType = Event.RESPONSE_TYPE_REJECTED;
		} else if (responsemaybe.isChecked()) {
			mresponseType = Event.RESPONSE_TYPE_TENTATIVE;

		}
	}

	/**
	 * Add child view to custom parent layout
	 * 
	 * @param id
	 * @return
	 */
	private View addChildEventReminderView(String minute, String method) {

		mContentValueReminder = new ContentValues();
		mReminderLayout = new LinearLayout(this);
		LinearLayout.LayoutParams mReminderLayoutParam = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		mReminderLayoutParam.setMargins(16, 0, 16, 0);
		mReminderLayout.setLayoutParams(mReminderLayoutParam);
		mReminderLayout.setOrientation(LinearLayout.HORIZONTAL);

		mReminderLayout.setGravity(Gravity.CENTER_VERTICAL);

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mSpinner_minutes = (Spinner) inflater.inflate(
				R.layout.cal_styled_spinner, null);
		mSpinner_minutes.setLayoutParams(new LinearLayout.LayoutParams(0,
				LayoutParams.WRAP_CONTENT, 2));
		mSpinner_minutes.setPadding(0, 0, 4, 0);
		mSpinner_minutes.setOverScrollMode(2);

		ArrayAdapter<String> spinner_minutesArrayAdapter = new ArrayAdapter<String>(
				this, R.layout.cal_spinner_item, R.id.list, getResources()
						.getStringArray(R.array.reminder_minutes_labels));

		mSpinner_minutes.setAdapter(spinner_minutesArrayAdapter);

		// if(minute.length()>2)
		// {reminder_minutes_labels
		mSetSelectionMinutes = Arrays.asList(
				this.getResources().getStringArray(
						R.array.reminder_minutes_values)).indexOf(minute);
		mSpinner_minutes.setSelection(mSetSelectionMinutes);
		// }
		// else
		// {
		// mSpinner_minutes.setSelection(Integer.parseInt(minute));
		// }

		mSpinner_minutes
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> adapter,
							View view, int position, long id) {
						mContentValueReminder
								.put(Reminder.MINUTES,
										getResources()
												.getStringArray(
														R.array.reminder_minutes_values)[position]);

					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {

					}

				});

		mSpinner_notification = (Spinner) inflater.inflate(
				R.layout.cal_styled_spinner, null);

		mSpinner_notification.setLayoutParams(new LinearLayout.LayoutParams(0,
				LayoutParams.WRAP_CONTENT, 2));
		mSpinner_notification.setPadding(0, 0, 4, 0);
		ArrayAdapter<String> spinner_notificationArrayAdapter = new ArrayAdapter<String>(
				this, R.layout.cal_spinner_item, R.id.list, getResources()
						.getStringArray(R.array.reminder_methods_labels));
		mSpinner_notification.setAdapter(spinner_notificationArrayAdapter);
		if (method.equals("4")) {
			mSpinner_notification.setSelection(Arrays.asList(
					this.getResources().getStringArray(
							R.array.reminder_methods_labels)).indexOf(
					CalendarConstants.NOTIFICATION));
		} else if (method.equals("2")) {
			mSpinner_notification.setSelection(Arrays.asList(
					this.getResources().getStringArray(
							R.array.reminder_methods_labels)).indexOf(
					CalendarConstants.EMAIL));
		}
		mSpinner_notification
				.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> adapter,
							View view, int position, long id) {

						if (mSpinner_notification.getItemAtPosition(position)
								.toString()
								.equals(CalendarConstants.NOTIFICATION)) {
							mContentValueReminder.put(Reminder.METHOD, 4);
						} else {
							mContentValueReminder.put(Reminder.METHOD, 2);
						}

					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {

					}
				});

		mBtn_cancel = new ImageButton(this);
		LinearLayout.LayoutParams btn_cancelLayoutParam = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		mBtn_cancel.setLayoutParams(btn_cancelLayoutParam);
		mBtn_cancel.setBackgroundResource(R.drawable.cal_cancel);
		mBtn_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mReminderParentLayout.removeView((View) v.getParent());
				mContentValueReminder.remove(Reminder.MINUTES);
				mContentValueReminder.remove(Reminder.METHOD);

			}
		});

		mReminderLayout.addView(mSpinner_minutes);
		mReminderLayout.addView(mSpinner_notification);
		mReminderLayout.addView(mBtn_cancel);

		// Save Content Values to List
		mSaveReminderValueList.add(mContentValueReminder);
		return mReminderLayout;
	}

	/**
	 * Add Reminder view
	 */
	private void addReminderView() {

		mReminderParentLayout = new LinearLayout(CalendarAddEvent.this);
		LinearLayout.LayoutParams reminderParentLayoutParam = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		reminderParentLayoutParam.setMargins(10, 10, 5, 0);
		mReminderParentLayout.setLayoutParams(reminderParentLayoutParam);
		mReminderParentLayout.setOrientation(LinearLayout.VERTICAL);

		mTxt_edit_event_add_reminder = new TextView(CalendarAddEvent.this);
		LinearLayout.LayoutParams txt_edit_event_add_reminderLayoutParam = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		int left = Email.convertToDp(20);
		txt_edit_event_add_reminderLayoutParam.setMargins(left, 10, 0, 0);
		mTxt_edit_event_add_reminder
				.setLayoutParams(txt_edit_event_add_reminderLayoutParam);
		mTxt_edit_event_add_reminder.setText(CalendarConstants.ADD_REMINDER);
		mTxt_edit_event_add_reminder.setTextColor(Color.BLACK);
		mTxt_edit_event_add_reminder.setTextSize(18);

		if (Email.isEditFlag()) {
			if (mReminderValues != null) {

				for (i = 0; i < mReminderValues.size(); i++) {
					ContentValues setReminder = mReminderValues.get(i);
					mReminderParentLayout.addView(addChildEventReminderView(
							setReminder.getAsString(CalendarConstants.MINUTES),
							setReminder.getAsString(CalendarConstants.METHOD)));
				}
			}
		}
		mTxt_edit_event_add_reminder.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String defaultminuts = mSecurePrefs
						.getString(
								getString(R.string.key_default_reminderlist_preference),
								"");
				CalendarLog.d(CalendarConstants.Tag, "Default Minutes add"
						+ defaultminuts);
				if (defaultminuts.equalsIgnoreCase("")) {
					defaultminuts = "0";
				}
				if (mReminderParentLayout.getVisibility() != View.VISIBLE) {

					mReminderParentLayout.setVisibility(View.VISIBLE);
					mReminderParentLayout.removeAllViews();

				}
				mReminderParentLayout.addView(addChildEventReminderView(
						defaultminuts, "4"));
			}
		});

		if (!defaultReminderminuts.equals("None")) {
			mReminderParentLayout.setVisibility(View.VISIBLE);

		} else {
			mReminderParentLayout.setVisibility(View.GONE);
		}

		mParentLayout.addView(mReminderParentLayout);
		mParentLayout.addView(mTxt_edit_event_add_reminder);

	}

	/** Create a new dialog for date picker */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:
			return new DatePickerDialog(this, pDateSetListener, mFromYear, mFromMonth,
					mFromDay);
		case DATE_RECURRENCE_DIALOG_ID:
			return new DatePickerDialog(this, pDateSetListener,
					mRecurrenceYear, mRecurrenceMonth, mRecurrenceDay);
		case TIME_DIALOG_ID:
			return new TimePickerDialog(this, timeDate, mFromHours, mFromMins,
					is24Hrs);
		case TIME_TO_DIALOG_ID:

			return new TimePickerDialog(this, timeDate, mToHours, mToMins,
					is24Hrs);
		}
		return null;
	}

	/** Callback received when the user "picks" a date in the dialog */
	private DatePickerDialog.OnDateSetListener pDateSetListener = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			if (mDatePicker == 0) {
				mFromYear = year;
				mFromMonth = monthOfYear;
				mFromDay = dayOfMonth;
				mToYear = year;
				mToMonth = monthOfYear;
				mToDay = dayOfMonth;
				updateDisplay();
				updateToDate();
			} else if (mDatePicker == 1) {

				int compare = CalendarCommonFunction.compareTo(
						CalendarCommonFunction.getZeroTimeDate(dayOfMonth,
								monthOfYear, year), CalendarCommonFunction
								.getZeroTimeDate(mFromDay, mFromMonth, mFromYear));
//				CalendarLog.d(CalendarConstants.Tag, "campare : " + compare);
				if (compare > 0) {
					mToYear = year;
					mToMonth = monthOfYear;
					mToDay = dayOfMonth;
				} else {
					mToYear = mFromYear;
					mToMonth = mFromMonth;
					mToDay = mFromDay;
				}
				updateToDate();
				eventStartDate = mFromDate.getText().toString();
				eventEndDate = mToDate.getText().toString();
				eventFromTime = mFromHours+":"+mFromMins;
				eventToTime = mToHours+":"+mToMins;
				long startTime = CalendarDatabaseHelper.getLongFromTime(
						eventStartDate + " " + eventFromTime, timeZoneList
								.getSelectedItem().toString());
				long endTime = CalendarDatabaseHelper.getLongFromTime(eventEndDate
						+ " " + eventToTime, timeZoneList.getSelectedItem()
						.toString());
				if(endTime<startTime){
					mToHours=mFromHours;
					mToMins=mFromMins;
					updateToTime();
				}
			} else if (mDatePicker == 2) {
				int compare = CalendarCommonFunction.compareTo(
						CalendarCommonFunction.getZeroTimeDate(dayOfMonth,
								monthOfYear, year), CalendarCommonFunction
								.getZeroTimeDate(mFromDay, mFromMonth, mFromYear));
				CalendarLog.d(CalendarConstants.Tag, "campare : " + compare);
				if (compare > 0) {
					mRecurrenceYear = year;
					mRecurrenceMonth = monthOfYear;
					mRecurrenceDay = dayOfMonth;
				} else {
					mToYear = mFromYear;
					mToMonth = mFromMonth;
					mToDay = mFromDay;
				}

				updateRecurrenceDate();
			}
			// displayToast();
		}
	};

	private TimePickerDialog.OnTimeSetListener timeDate = new TimePickerDialog.OnTimeSetListener() {

		@Override
		public void onTimeSet(TimePicker view, int hours, int minutes) {
			mNewCal = false;
			if (isFromTime) {
				mFromHours = hours;
				mFromMins = minutes;
				mToHours = hours + mDiffHours;
				mToMins = minutes + mDiffmMins;
				updateTime();
				updateToTime();
			} else {
				mToHours = hours;
				mToMins = minutes;
				mDiffHours = mToHours - mFromHours;
				mDiffmMins = mToMins - mFromMins;

				updateToTime();
			}

		}
	};

	private void updateTime() {
		// FromTime.setText(CalendarDatabaseHelper.getTimeFromLong(eventContentValues.getAsLong(com.sample.calendar.modal.Calendar.Event.DTSTART)));
		int hr = mFromHours;
		int min = mFromMins;
		if (!is24Hrs) {
			if (hr >= 12) {
				am_pm = "pm";
			} else {
				am_pm = "am";
			}
			Calendar timepickerCalendar = Calendar.getInstance();
			timepickerCalendar.set(Calendar.HOUR, hr);
			timepickerCalendar.set(Calendar.MINUTE, min);

			SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
			String time = sdf.format(timepickerCalendar.getTime());
			String[] fromTime = time.split(":");
			hr = Integer.parseInt(fromTime[0]);
			min = Integer.parseInt(fromTime[1]);
			mFromTime.setText(hr + ":" + TimeConversation(min) + "" + am_pm);
		} else {
			mFromTime.setText(TimeConversation(hr) + ":"
					+ TimeConversation(min));
		}

		// mFromTime.setText(mFromHours + ":" + TimeConversation(mFromMins));
	}

	private void updateToTime() {
		int toHr = mToHours;
		int toMin = mToMins;
		eventStartDate = mFromDate.getText().toString();
		eventEndDate = mToDate.getText().toString();
		eventFromTime = mFromHours+":"+mFromMins;
		eventToTime = mToHours+":"+mToMins;
		long startTime = CalendarDatabaseHelper.getLongFromTime(
				eventStartDate + " " + eventFromTime, timeZoneList
						.getSelectedItem().toString());
		long endTime = CalendarDatabaseHelper.getLongFromTime(eventEndDate
				+ " " + eventToTime, timeZoneList.getSelectedItem()
				.toString());
		if (!is24Hrs) {
			if (toHr >= 12) {
				am_pm = "pm";
			} else {
				am_pm = "am";
			}
			Calendar timepickerCalendar = Calendar.getInstance();
			timepickerCalendar.set(Calendar.HOUR, toHr);
			timepickerCalendar.set(Calendar.MINUTE, toMin);
			SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
			String time = sdf.format(timepickerCalendar.getTime());
			String[] fromTime = time.split(":");
			toHr = Integer.parseInt(fromTime[0]);
			toMin = Integer.parseInt(fromTime[1]);
			mToTime.setText(toHr + ":" + TimeConversation(toMin) + "" + am_pm);
			
			if(endTime<startTime){
				updateDate(1);
			}
//			if((mEventContentValues==null ) && (!flagNewEvent) )
//			{
//				updateDate(1);
//			}
//			else if((!flagNewEvent) && (endTime<startTime))
//			{
//				updateDate(1);
//			}
//			else
//			{
//				updateDate(0);
//			}
			
		}
		else {
				
				Calendar newCal = Calendar.getInstance();
				if((mEventContentValues==null) && (!flagNewEvent)){
					
					newCal.set(Calendar.DATE, mToDay + 1);
					newCal.set(Calendar.MONTH, mToMonth);
					newCal.set(Calendar.YEAR, mToYear);
					flagNewEvent = false;
					
				}
				else{
					
					newCal.set(Calendar.DATE, mToDay + 0);
					newCal.set(Calendar.MONTH, mToMonth);
					newCal.set(Calendar.YEAR, mToYear);
					
					flagNewEvent = false;
				}
				
				int currentmToDay = newCal.get(Calendar.DATE);
				int currentmToMonth = newCal.get(Calendar.MONTH);
				int currentmToYear = newCal.get(Calendar.YEAR);
				mToYear = currentmToYear;
				mToMonth = currentmToMonth;
				mToDay = currentmToDay;
				
				if(endTime<startTime)
				{
					String dayOfWeek = DateFormat
							.format("EE",
									new Date(currentmToYear, currentmToMonth,
											currentmToDay - 1)).toString();
					mToDate.setText(new StringBuilder()
							// Month is 0 based so add 1
							.append(dayOfWeek).append(",").append(currentmToDay)
							.append(" ").append(mDayOfMonths[currentmToMonth])
							.append(" ").append(currentmToYear).append(" "));
				}
				mToTime.setText(TimeConversation(toHr) + ":"
						+ TimeConversation(toMin));
		}
	
		
	}

	private void updateDate(int days) {
		if ((current_am_pm.equals("pm") && am_pm.equals("am")) && (!mNewCal)) {
			Calendar newCal = Calendar.getInstance();
			newCal.set(Calendar.DATE, mToDay + days);
			newCal.set(Calendar.MONTH, mToMonth);
			newCal.set(Calendar.YEAR, mToYear);
			int currentmToDay = newCal.get(Calendar.DATE);
			int currentmToMonth = newCal.get(Calendar.MONTH);
			int currentmToYear = newCal.get(Calendar.YEAR);
			
			mToYear = currentmToYear;
			mToMonth = currentmToMonth;
			mToDay = currentmToDay;
			
			/*if((newCal.get(Calendar.HOUR)>mToHours))*/
			//{
				String dayOfWeek = DateFormat
						.format("EE",
								new Date(currentmToYear, currentmToMonth,
										currentmToDay - 1)).toString();
				mToDate.setText(new StringBuilder()
						// Month is 0 based so add 1
						.append(dayOfWeek).append(",").append(currentmToDay)
						.append(" ").append(mDayOfMonths[currentmToMonth])
						.append(" ").append(currentmToYear).append(" "));
				mNewCal = true;
				flagNewEvent = false;
			//}
			
		}
		else if((current_am_pm.equals("pm") && am_pm.equals("pm")) && (!mNewCal)) {
			Calendar newCal = Calendar.getInstance();
			newCal.set(Calendar.DATE, mToDay + days);
			newCal.set(Calendar.MONTH, mToMonth);
			newCal.set(Calendar.YEAR, mToYear);
			int currentmToDay = newCal.get(Calendar.DATE);
			int currentmToMonth = newCal.get(Calendar.MONTH);
			int currentmToYear = newCal.get(Calendar.YEAR);
			
			mToYear = currentmToYear;
			mToMonth = currentmToMonth;
			mToDay = currentmToDay;
			
			/*if((newCal.get(Calendar.HOUR)>mToHours))*/
			//{
				String dayOfWeek = DateFormat
						.format("EE",
								new Date(currentmToYear, currentmToMonth,
										currentmToDay - 1)).toString();
				mToDate.setText(new StringBuilder()
						// Month is 0 based so add 1
						.append(dayOfWeek).append(",").append(currentmToDay)
						.append(" ").append(mDayOfMonths[currentmToMonth])
						.append(" ").append(currentmToYear).append(" "));
				mNewCal = true;
				flagNewEvent = false;
			//}
			
		}
		else if((current_am_pm.equals("am") && am_pm.equals("am")) && (!mNewCal))
		{
			Calendar newCal = Calendar.getInstance();
			newCal.set(Calendar.DATE, mToDay + days);
			newCal.set(Calendar.MONTH, mToMonth);
			newCal.set(Calendar.YEAR, mToYear);
			int currentmToDay = newCal.get(Calendar.DATE);
			int currentmToMonth = newCal.get(Calendar.MONTH);
			int currentmToYear = newCal.get(Calendar.YEAR);
			
			mToYear = currentmToYear;
			mToMonth = currentmToMonth;
			mToDay = currentmToDay;
			
//			if((newCal.get(Calendar.HOUR) > mToHours))
			{
				String dayOfWeek = DateFormat
						.format("EE",
								new Date(currentmToYear, currentmToMonth,
										currentmToDay - 1)).toString();
				mToDate.setText(new StringBuilder()
						// Month is 0 based so add 1
						.append(dayOfWeek).append(",").append(currentmToDay)
						.append(" ").append(mDayOfMonths[currentmToMonth])
						.append(" ").append(currentmToYear).append(" "));
				mNewCal = true;
				flagNewEvent = false;
			}
			
		}
		else{
				String dayOfWeek = DateFormat.format("EE",
						new Date(mToYear, mToMonth, mToDay - 1)).toString();
				mToDate.setText(new StringBuilder()
						// Month is 0 based so add 1
				.append(dayOfWeek).append(",").append(mToDay).append(" ")
				.append(mDayOfMonths[mToMonth]).append(" ").append(mToYear)
				.append(" "));
				flagNewEvent = false;
		}
		
		
	}

	private String TimeConversation(int minutes) {
		String result = "";
		if (minutes < 10)
			result = "0" + minutes;
		else
			result = String.valueOf(minutes);
		return result;
	}

	private void updateDisplay() {
		// String dayOfWeek =
		// CalendarDatabaseHelper.getDateForAgenda(eventContentValues.getAsString(com.sample.calendar.modal.Calendar.Event.START_DATE));
		// FromDate.setText(dayOfWeek);
		String dayOfWeek = DateFormat.format("EE",
				new Date(mFromYear, mFromMonth, mFromDay - 1)).toString();
		mFromDate.setText(new StringBuilder()
				// Month is 0 based so add 1
				.append(dayOfWeek).append(",").append(mFromDay).append(" ")
				.append(mDayOfMonths[mFromMonth]).append(" ").append(mFromYear)
				.append(" "));
	}

	@SuppressWarnings("deprecation")
	private void updateToDate() {
		// String dayOfWeek =
		// CalendarDatabaseHelper.getDateForAgenda(eventContentValues.getAsString(com.sample.calendar.modal.Calendar.Event.END_DATE));
		// ToDate.setText(dayOfWeek);
		String dayOfWeek = DateFormat.format("EE",
				new Date(mToYear, mToMonth, mToDay - 1)).toString();
		mToDate.setText(new StringBuilder()
				// Month is 0 based so add 1
				.append(dayOfWeek).append(",").append(mToDay).append(" ")
				.append(mDayOfMonths[mToMonth]).append(" ").append(mToYear)
				.append(" "));
	}

	@SuppressWarnings("deprecation")
	private void updateRecurrenceDate() {

		String dayOfWeek = DateFormat
				.format("EE",
						new Date(mRecurrenceYear, mRecurrenceMonth,
								mRecurrenceDay - 1)).toString();
		mRecurrenceRepeatTime.setText(new StringBuilder()
				// Month is 0 based so add 1
				.append(dayOfWeek).append(",").append(mRecurrenceDay)
				.append(" ").append(mDayOfMonths[mRecurrenceMonth]).append(" ")
				.append(mRecurrenceYear).append(" "));
	}

	private ArrayAdapter<String> populateItems(String[] objects) {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.cal_spinner_add_event, objects);
		return adapter;
	}

	private OnClickListener btn_recurrence_listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			mRecurrenceEvery.setText(checkmonth());
			initRecurrence();
			mDialog.show();

		}
	};

	private void initDialog() {
		mDialog = new Dialog(CalendarAddEvent.this);
		mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mDialog.setContentView(R.layout.cal_recurrence_view);
		mRecurrenceSwitch = (ToggleButton) mDialog
				.findViewById(R.id.recurrence_switch);

		mRecurrenceType = (Spinner) mDialog.findViewById(R.id.recurrence_type);
		mRecurrenceType.setAdapter(new ArrayAdapter<String>(
				CalendarAddEvent.this, R.layout.cal_spinner_add_event,
				getResources()
						.getStringArray(R.array.repetition_methods_labels)));
		mRecurrenceRepeat = (Spinner) mDialog
				.findViewById(R.id.recurrence_repeat);
		mRecurrenceRepeat
				.setAdapter(new ArrayAdapter<String>(CalendarAddEvent.this,
						R.layout.cal_spinner_add_event, getResources()
								.getStringArray(
										R.array.recurrence_repeat_labels)));

		mRecurrenceNoOfDays = (com.cognizant.trumobi.commonabstractclass.TruMobBaseEditText) mDialog
				.findViewById(R.id.recurrence_daily_days);
		mRecurrenceNoOfDays.setText("1");
		mRecurrenceRepeatDays = (com.cognizant.trumobi.commonabstractclass.TruMobBaseEditText) mDialog
				.findViewById(R.id.recurrence_repeat_days);
		mRecurrenceRepeatDays.setText("1");

		mRecurrenceDayRow = (LinearLayout) mDialog
				.findViewById(R.id.recurrence_day_row);
		mRecurrenceWeekRow = (LinearLayout) mDialog
				.findViewById(R.id.recurrence_weekly_row);
		mRecurrenctMonthRow = (LinearLayout) mDialog
				.findViewById(R.id.recurrence_monthly_row);
		mRecurrencerepeatRow = (LinearLayout) mDialog
				.findViewById(R.id.recurrence_repeat_row);

		mRecurrenceDone = (Button) mDialog.findViewById(R.id.recurrence_done);
		mRecurrenceRepeatTime = (Button) mDialog
				.findViewById(R.id.recurrence_repeat_time);
		mRecurrenceRepeatTime.setOnClickListener(new View.OnClickListener() {

			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				mDatePicker = 2;
				showDialog(DATE_RECURRENCE_DIALOG_ID);

			}
		});

		mRecurrenceSun = (ToggleButton) mDialog
				.findViewById(R.id.recurrence_sun);
		mRecurrenceMon = (ToggleButton) mDialog
				.findViewById(R.id.recurrence_mon);
		mRecurrenceTue = (ToggleButton) mDialog
				.findViewById(R.id.recurrence_tue);
		mRecurrenceWed = (ToggleButton) mDialog
				.findViewById(R.id.recurrence_wed);
		mRecurrenceThur = (ToggleButton) mDialog
				.findViewById(R.id.recurrence_thur);
		mRecurrenceFri = (ToggleButton) mDialog
				.findViewById(R.id.recurrence_fri);
		mRecurrenceSat = (ToggleButton) mDialog
				.findViewById(R.id.recurrence_sat);

		mRecurrenceEvery = (RadioButton) mDialog
				.findViewById(R.id.recurrence_monthly_every);
		mRecurrenceSame = (RadioButton) mDialog
				.findViewById(R.id.recurrence_monthly_same);

		mRecurrenceDailyEvery = (TextView) mDialog
				.findViewById(R.id.daily_every_label);
		mRecurrenceDailyWeekDays = (TextView) mDialog
				.findViewById(R.id.daily_every_weekdays_label);
		mRecurrenceRepeatDaysLable = (TextView) mDialog
				.findViewById(R.id.recurrence_repeat_days_label);

		enableRecurrenceControl(false);

		mRecurrenceSwitch
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						enableRecurrenceControl(isChecked);
					}
				});

		mRecurrenceType.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == 0) {
					mRecurrenctMonthRow.setVisibility(View.GONE);
					mRecurrenceWeekRow.setVisibility(View.GONE);
					mRecurrenceDailyWeekDays.setText(getString(R.string.day));
				} else if (position == 1) {
					mRecurrenctMonthRow.setVisibility(View.GONE);
					mRecurrenceWeekRow.setVisibility(View.VISIBLE);
					mRecurrenceDailyWeekDays.setText(getString(R.string.week));

				} else if (position == 2) {
					mRecurrenctMonthRow.setVisibility(View.VISIBLE);
					mRecurrenceWeekRow.setVisibility(View.GONE);
					mRecurrenceDailyWeekDays.setText(getString(R.string.month));
				} else if (position == 3) {
					mRecurrenctMonthRow.setVisibility(View.GONE);
					mRecurrenceWeekRow.setVisibility(View.GONE);
					mRecurrenceDailyWeekDays.setText(getString(R.string.year));
				}

			}

			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		mRecurrenceRepeat
				.setOnItemSelectedListener(new OnItemSelectedListener() {
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						if (position == 0) {
							mRecurrenceRepeatTime.setVisibility(View.GONE);
							mRecurrenceRepeatDays.setVisibility(View.GONE);
							mRecurrenceRepeatDaysLable.setVisibility(View.GONE);
						} else if (position == 1) {
							mRecurrenceRepeatTime.setVisibility(View.VISIBLE);
							mRecurrenceRepeatDays.setVisibility(View.GONE);
							mRecurrenceRepeatDaysLable.setVisibility(View.GONE);

						} else if (position == 2) {
							mRecurrenceRepeatTime.setVisibility(View.GONE);
							mRecurrenceRepeatDays.setVisibility(View.VISIBLE);
							mRecurrenceRepeatDaysLable
									.setVisibility(View.VISIBLE);

						}

					}

					public void onNothingSelected(AdapterView<?> parent) {

					}
				});

		mRecurrenceDone.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String recurreanceText = getRecurrenceValue();
				if (recurreanceText.equalsIgnoreCase("-1")) {
					Toast.makeText(CalendarAddEvent.this,
							getString(R.string.weekly_error_msg),
							Toast.LENGTH_LONG).show();
				} else {
					mRepetition.setText(recurreanceText);
					mDialog.dismiss();
				}
			}
		});

	}

	private String getRecurrenceValue() {
		String result = getString(R.string.no_repetition);
		if (mRecurrenceSwitch.isChecked()) {
			String numberofdays = mRecurrenceNoOfDays.getText().toString()
					.trim();
			String numberofdaysRepeat = mRecurrenceRepeatDays.getText()
					.toString().trim();
			String daystring = "";
//			String daystringRepeat = "";
			if (numberofdays.equalsIgnoreCase("")) {
				numberofdays = "1";
			}
			if (numberofdaysRepeat.equalsIgnoreCase("")) {
				numberofdaysRepeat = "1";
			}
			CalendarLog.d(CalendarConstants.Tag,
					"Selected " + mRecurrenceType.getSelectedItemId());
			int noofdays = Integer.parseInt(numberofdays);
			if (noofdays > 1) {
				daystring = "";
			}

			int noofdaysRepeat = Integer.parseInt(numberofdaysRepeat);
			if (noofdaysRepeat > 1) {
//				daystringRepeat = "s";
			}
			if (mRecurrenceType.getSelectedItemId() == 0) {
				result = "Every " + numberofdays + " Day" + daystring + ";";
				mRecurrence.type = 0;
				mRecurrence.interval = noofdays;
			} else if (mRecurrenceType.getSelectedItemId() == 1) {
				mRecurrence.type = 1;
				String Weekdays = "";
				String dowString = "";
//				int dow = 0;
				if (mRecurrenceSun.isChecked()) {
					Weekdays += ",Sun";
					dowString += ",SU";
//					dow += Recurrence.DOW_SUNDAY;
				}
				if (mRecurrenceMon.isChecked()) {
					Weekdays += ",Mon";
					dowString += ",MO";
//					dow += Recurrence.DOW_MONDAY;
				}
				if (mRecurrenceTue.isChecked()) {
					Weekdays += ",Tue";
					dowString += ",TU";
//					dow += Recurrence.DOW_TUESDAY;
				}
				if (mRecurrenceWed.isChecked()) {
					Weekdays += ",Wed";
					dowString += ",WE";
//					dow += Recurrence.DOW_WEDNESDAY;
				}
				if (mRecurrenceThur.isChecked()) {
//					dow += Recurrence.DOW_THURSDAY;
					Weekdays += ",Thur";
					dowString += ",TH";
				}
				if (mRecurrenceFri.isChecked()) {
//					dow += Recurrence.DOW_FRIDAY;
					dowString += ",FR";
					Weekdays += ",Fri";
				}
				if (mRecurrenceSat.isChecked()) {
//					dow += Recurrence.DOW_SATURDAY;
					Weekdays += ",Sat";
					dowString += ",SA";
				}
				// mRecurrence.dow = dow;
				mRecurrence.dow = -1;

				result = "Every " + numberofdays + " Week" + daystring + ";";
				if (Weekdays.equalsIgnoreCase(""))
					return "-1";
				else {
					Weekdays = Weekdays.substring(1);
					result += "On " + Weekdays + ";";
				}
				if (dowString.equalsIgnoreCase("")) {
				} else {
					dowString = dowString.substring(1);
					mRecurrence.dowString = dowString;
				}
				mRecurrence.interval = noofdays;
			} else if (mRecurrenceType.getSelectedItemId() == 2) {

				mRecurrence.interval = noofdays;
				if (mRecurrenceSame.isChecked()) {
					mRecurrence.type = 2;
					mRecurrence.dom = mFromDay;
					result = "Every " + numberofdays + " Month" + daystring
							+ ";";
				} else if (mRecurrenceEvery.isChecked()) {
					mRecurrence.type = 3;
					String dayOfWeek = GetDayoftheWeek(mFromYear, mFromMonth, mFromDay);
					dayOfWeek = dayOfWeek.toUpperCase().substring(0,
							dayOfWeek.length() - 1);
					int weekday = mFromDay / 7;
					mRecurrence.dow = -1;
					mRecurrence.dowString = dayOfWeek;
					mRecurrence.wom = weekday + 1;
					mRecurrence.moy = mFromMonth;
					CalendarLog.d(CalendarConstants.Tag, "dayOfWeek"
							+ dayOfWeek);
					result = "Every " + numberofdays + " Month" + daystring
							+ "(" + mRecurrenceEvery.getText().toString()
							+ ");";
				}
			} else if (mRecurrenceType.getSelectedItemId() == 3) {
				result = "Every " + numberofdays + " Year" + daystring + ";";
				mRecurrence.interval = noofdays;
				mRecurrence.type = 5;
				mRecurrence.dom = mFromDay;
				mRecurrence.moy = mFromMonth + 1;
			}

			if (mRecurrenceRepeat.getSelectedItemId() == 1) {
				result = result + "Until "
						+ mRecurrenceRepeatTime.getText().toString() + ";";
				mRecurrence.until = CalendarDatabaseHelper.getLongFromTime(
						mRecurrenceRepeatTime.getText().toString() + " "
								+ mFromHours + ":" + mFromMins, timeZoneList
								.getSelectedItem().toString());
				CalendarLog.d(CalendarConstants.Tag, "Untill-get for "
						+ mRecurrenceRepeatTime.getText().toString() + " "
						+ mFromHours + ":" + mFromMins + " is "
						+ mRecurrence.until);
			} else if (mRecurrenceRepeat.getSelectedItemId() == 2) {
				result = result + "Every " + numberofdaysRepeat
						+ " occurrences;";
				mRecurrence.occurences = noofdaysRepeat;
			}
		} else {
			mRecurrence.type = -1;
			mRecurrence.interval = -1;
			mRecurrence.until = -1;
			mRecurrence.occurences = -1;
			mRecurrence.dom = -1;
			mRecurrence.dow = 0;
			mRecurrence.wom = -1;
			mRecurrence.moy = -1;
			// mRecurrence=null;
		}
		return result;
	}

	private void initRecurrence() {
		if (mRecurrence.type == -1) {
			mRecurrenceSwitch.setChecked(false);
		} else {
			mRecurrenceSwitch.setChecked(true);
			if (mRecurrence.type == 0) {
				mRecurrenceType.setSelection(0);
				setRecurrenceRepeat();
			}
			if (mRecurrence.type == 1) {
				if (mRecurrence.dow == 62)
					mRecurrenceType.setSelection(0);
				else {
					mRecurrenceType.setSelection(1);
				}
				setRecurrenceRepeat();
				setRecurrenceRepeatWeek();
			}
			if (mRecurrence.type == 2 || mRecurrence.type == 3) {
				mRecurrenceType.setSelection(2);
				if (mRecurrence.type == 2) {
					mRecurrenceSame.setChecked(true);
				} else if (mRecurrence.type == 3) {
					mRecurrenceEvery.setChecked(true);
				}
				setRecurrenceRepeat();
			}
			if (mRecurrence.type == 5 || (mRecurrence.type == 6)) {
				mRecurrenceType.setSelection(3);
				setRecurrenceRepeat();
			}

		}
		String recurreanceText = getRecurrenceValue();
		mRepetition.setText(recurreanceText);
	}

	private void setRecurrenceRepeatWeek() {
		String dowStrings = mRecurrence.dowString;
		String[] dowString = dowStrings.split(",");
		for (String dow : dowString) {
			if (dow.equalsIgnoreCase("SU"))
				mRecurrenceSun.setChecked(true);
			if (dow.equalsIgnoreCase("MO"))
				mRecurrenceMon.setChecked(true);
			if (dow.equalsIgnoreCase("TU"))
				mRecurrenceTue.setChecked(true);
			if (dow.equalsIgnoreCase("WE"))
				mRecurrenceWed.setChecked(true);
			if (dow.equalsIgnoreCase("TH"))
				mRecurrenceThur.setChecked(true);
			if (dow.equalsIgnoreCase("FR"))
				mRecurrenceFri.setChecked(true);
			if (dow.equalsIgnoreCase("SA"))
				mRecurrenceSat.setChecked(true);
		}
	}

	private String checkmonth() {
		String dayOfWeek = GetDayoftheWeek(mFromYear, mFromMonth, mFromDay);

		int weekday = mFromDay / 7;
		return "on every " + getWeekCount(weekday) + " " + dayOfWeek;
	}

	@SuppressWarnings("deprecation")
	private String GetDayoftheWeek(int weekyear, int weekmonth, int weekday) {
		String dayOfWeek = DateFormat.format("EE",
				new Date(weekyear, weekmonth, weekday - 1)).toString();
		return dayOfWeek;
	}

	private String getWeekCount(int day) {
		switch (day) {
		case 0:
			return "First";
		case 1:
			return "Second";
		case 2:
			return "Third";
		case 3:
			return "Fourth";
		case 4:
			return "Fifth";
		case 5:
			return "Sixth";
		default:
			return "First";
		}

	}

	private void setRecurrenceRepeat() {
		if ((mRecurrence.occurences == -1) && mRecurrence.until == -1) {
			mRecurrenceRepeat.setSelection(0);
			if (mRecurrence.interval != -1)
				mRecurrenceNoOfDays.setText(String
						.valueOf(mRecurrence.interval));
		} else if ((mRecurrence.occurences == -1) && mRecurrence.until != -1) {
			mRecurrenceRepeat.setSelection(1);
			String date = CalendarCommonFunction
					.convertMilliSecondsToDate(mRecurrence.until);
			Calendar repeatdate = CalendarCommonFunction
					.convertDBDateToCalendar(date);

			mRecurrenceYear = repeatdate.get(Calendar.YEAR);
			mRecurrenceMonth = repeatdate.get(Calendar.MONTH);
			mRecurrenceDay = repeatdate.get(Calendar.DATE);
			updateRecurrenceDate();

			// mRecurrenceRepeatTime.setText(resid)
		} else if ((mRecurrence.occurences != -1) && mRecurrence.until == -1) {
			mRecurrenceRepeat.setSelection(2);
			mRecurrenceRepeatDays.setText(String
					.valueOf(mRecurrence.occurences));
		}
	}

	private void enableRecurrenceControl(boolean status) {
		mRecurrenceType.setEnabled(status);
		mRecurrenceRepeat.setEnabled(status);
		mRecurrenceRepeatTime.setEnabled(status);
		mRecurrenceNoOfDays.setEnabled(status);
		mRecurrenceEvery.setEnabled(status);
		mRecurrenceSame.setEnabled(status);
		mRecurrenceSun.setEnabled(status);
		mRecurrenceMon.setEnabled(status);
		mRecurrenceTue.setEnabled(status);
		mRecurrenceWed.setEnabled(status);
		mRecurrenceThur.setEnabled(status);
		mRecurrenceFri.setEnabled(status);
		mRecurrenceSat.setEnabled(status);
		mRecurrenceRepeatDays.setEnabled(status);

		mRecurrenceDailyEvery.setEnabled(status);
		mRecurrenceDailyWeekDays.setEnabled(status);
		mRecurrenceRepeatDaysLable.setEnabled(status);
	}

	protected void showRecurrenceDialog(String title) {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				CalendarAddEvent.this);
		builder.setTitle(title + "?");
		final String[] rest = new String[3];
		// For Edit
		rest[0] = "Change only this event";
		rest[1] = "Change all events in the series.";
		rest[2] = "Change this and all future events";
		final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
				CalendarAddEvent.this,
				android.R.layout.select_dialog_singlechoice);
		arrayAdapter.add("Change only this event");
		arrayAdapter.add("Change all events in the series.");
		arrayAdapter.add("Change this and all future events");
		if (mEventContentValues
				.containsKey(CalendarConstants.RECURRENCE_CURRENTDATE)) {
			// CalendarLog
			// .d(CalendarConstants.Tag,
			// "RECURRENCE_CURRENTDATE"
			// + mEventContentValues
			// .getAsString(CalendarConstants.RECURRENCE_CURRENTDATE));
		}
		builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				if (arg1 == 2 || arg1 == 0) {
					String recurrCurrendate = mEventContentValues
							.getAsString(CalendarConstants.RECURRENCE_CURRENTDATE);
					// CalendarLog.d(
					// CalendarConstants.Tag,
					// "RECURRENCE_CURRENTDATE"
					// + mEventContentValues
					// .getAsString(CalendarConstants.RECURRENCE_CURRENTDATE));
					String[] days = recurrCurrendate.split(" ");
					// Log.e("check time","check s"+days[0] + " -" +date);

					Calendar cal = Calendar.getInstance();
					cal.setFirstDayOfWeek(Calendar.SUNDAY);
					SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy",
							Locale.US);
					try {
						cal.setTime(sdf.parse(recurrCurrendate));
					} catch (ParseException e) {
						e.printStackTrace();
					}// all done
					cal.set(Calendar.DAY_OF_MONTH, Integer.valueOf(days[0]));
					cal.set(Calendar.MONTH, cal.get(Calendar.MONTH));
					cal.set(Calendar.YEAR, Integer.valueOf(days[2]));

					mFromDay = cal.get(Calendar.DAY_OF_MONTH);
					mFromMonth = cal.get(Calendar.MONTH);
					mFromYear = cal.get(Calendar.YEAR);
					updateDisplay();

					mToDay = cal.get(Calendar.DAY_OF_MONTH);
					mToMonth = cal.get(Calendar.MONTH);
					mToYear = cal.get(Calendar.YEAR);
					updateToDate();
//					if(arg1 ==0){
//						mRecurrenceSwitch.setChecked(false);
//						String recurreanceText = getRecurrenceValue();
//						mRepetition.setText(getString(R.string.no_repetition));
//						mRecurrence=null;
////						mEventContentValues.
//						mRepetition.setOnClickListener(new OnClickListener() {
//
//							@Override
//							public void onClick(View v) {
//////								String recurreanceText = getRecurrenceValue();
//////								if (recurreanceText.equalsIgnoreCase("-1")) {
//////									Toast.makeText(CalendarAddEvent.this,
//////											getString(R.string.weekly_error_msg),
//////											Toast.LENGTH_LONG);
//////								} else {
//////									mRepetition.setText(recurreanceText);
//////									mDialog.dismiss();
//////								}
//							}
//						});
////
//					}
					
				}
				mRecurEditItem = arg1;
				dialog.cancel();
			}
		});

		final AlertDialog alert = builder.create();
		alert.setOnKeyListener(new Dialog.OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface arg0, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					CalendarAddEvent.this.finish();
					alert.dismiss();
				}
				return true;
			}
		});
		alert.show();
	}

}
