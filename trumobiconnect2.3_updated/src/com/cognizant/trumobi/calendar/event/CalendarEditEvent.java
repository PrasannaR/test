package com.cognizant.trumobi.calendar.event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

import com.TruBoxSDK.SharedPreferences;
import com.cognizant.trubox.contacts.db.ContactsModel;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.calendar.modal.Attendee;
import com.cognizant.trumobi.calendar.modal.Event;
import com.cognizant.trumobi.calendar.modal.Exceptions;
import com.cognizant.trumobi.calendar.modal.Recurrence;
import com.cognizant.trumobi.calendar.modal.Reminder;
import com.cognizant.trumobi.calendar.provider.CalendarDatabaseHelper;
import com.cognizant.trumobi.calendar.util.CalendarCommonFunction;
import com.cognizant.trumobi.calendar.util.CalendarConstants;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseSherlockFragmentBaseActivity;
import com.cognizant.trumobi.contacts.activity.ContactsAddContact;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.log.CalendarLog;

public class CalendarEditEvent extends TruMobiBaseSherlockFragmentBaseActivity {

	private View mView;
	private TextView mTxt_edit_event_name, mTxt_edit_event_time,
			mTxt_edit_event_rrule, mTxt_edit_event_location,
			mTxt_edit_event_description, mTxt_edit_event_mail,
			mTxt_edit_event_add_reminder, mTxt_forward;
	private Spinner mSpinner_minutes, mSpinner_notification;
	private ImageButton mBtn_cancel;
	private LinearLayout mBtn_back;
	private TextView mTxt_edit, mTxt_delete;
	private LinearLayout mReminderLayout, mParentLayout, mReminderParentLayout;
	private int i = 0;
	LinearLayout mResponseRow;
	RadioGroup mResponseType;
	OnCheckedChangeListener mResponseStatusChangedListener;
	private ContentValues mEventValues;
	private ArrayList<ContentValues> mReminderValues = new ArrayList<ContentValues>();
	private ArrayList<ContentValues> mSaveReminderValueList = new ArrayList<ContentValues>();
	private ContentValues mContentValueReminder;
	private int mSetSelectionMinutes;
	private static String sCureventID = "";
	private static String sCureventType = "";
	private int j = 0;
	private Bundle mGetuserID;
	private SharedPreferences mSecurePrefs;
	private boolean isEdited = false;
	private int isFirst = 0;
	// private boolean isAllChoice = true;
	private int mRecurDeletedItem = -1;
//	private URL url;
	SpannableStringBuilder ssb;

	// newlyAdded
	private int dialogState;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// CalendarLog.d(CalendarConstants.Tag, "EditEventonCreate");

		if (!isTablet(this)) {
			setTheme(R.style.Calendar_AppTheme);
			// getActionBar().hide();
		}
		super.onCreate(savedInstanceState);

		setContentView(R.layout.cal_edit_events);
		mSecurePrefs = new SharedPreferences(Email.getAppContext());
		if (getSupportActionBar() != null) {
			getSupportActionBar().hide();
		}
		/*
		 * mSecurePrefs = //new SharedPreferences(Email.getAppContext());
		 * PreferenceManager.getDefaultSharedPreferences(this);
		 */
		// getActionBar().hide();
		mGetuserID = getIntent().getExtras();
		mParentLayout = (LinearLayout) findViewById(R.id.edit_reminder_multiple_row);
		setBundleValues();
		if (mParentLayout.getChildCount() > 0)
			mParentLayout.removeAllViews();
		init();
		if (savedInstanceState != null) {
			mEventValues = savedInstanceState
					.getParcelable(CalendarConstants.EVENTVALUES);
		}

		isEdited = false;
		CalendarLog.d(CalendarConstants.Tag, "add_reminder oncreate :"
				+ isEdited);
		// NewlyAdded
		if (savedInstanceState != null) {
			// Restore value of members from saved state
			dialogState = savedInstanceState.getInt("dialogState");
			if (dialogState == 1) {
				showDialog();
			} else if (dialogState == 2) {
				showRecurrenceDialog(mEventValues.getAsString(Event.TITLE),
						true);
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			String recurrenceDate = "";
			if (mEventValues != null) {

				if (mEventValues
						.containsKey(CalendarConstants.RECURRENCE_CURRENTDATE)) {
					recurrenceDate = mEventValues
							.getAsString(CalendarConstants.RECURRENCE_CURRENTDATE);
				}
				mEventValues = CalendarCommonFunction.getUpdatedEvents(
						mEventValues.getAsInteger("_id").toString(),
						mEventValues.get(CalendarConstants.CALENDAR_TYPE_KEY)
								.toString());
			}
			if (!recurrenceDate.equalsIgnoreCase("")) {
				mEventValues.put(CalendarConstants.RECURRENCE_CURRENTDATE,
						recurrenceDate);
				// CalendarLog.d(CalendarConstants.Tag,"RECURRENCE_CURRENTDATE"+
				// mEventValues.getAsString(CalendarConstants.RECURRENCE_CURRENTDATE));
			}
			if (mParentLayout.getChildCount() > 0)
				mParentLayout.removeAllViews();
			init();
			CalendarLog.d(CalendarConstants.Tag, "add_reminder onresume :"
					+ isEdited);
		} catch (Exception e) {
			e.printStackTrace();
			finish();
		}
	}

	private void setBundleValues() {
		if (mGetuserID != null) {
			mEventValues = mGetuserID
					.getParcelable(CalendarConstants.EVENT_CONTENT_VALUES);
			if (mEventValues == null)
				return;

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
		mReminderLayout = new LinearLayout(CalendarEditEvent.this);
		LinearLayout.LayoutParams mReminderLayoutParam = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		mReminderLayoutParam.setMargins(16, 0, 16, 0);
		mReminderLayout.setLayoutParams(mReminderLayoutParam);
		mReminderLayout.setOrientation(LinearLayout.HORIZONTAL);
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mSpinner_minutes = (Spinner) inflater.inflate(
				R.layout.cal_styled_spinner, null);
		// mSpinner_minutes.setOverScrollMode(2);
		mSpinner_minutes.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 2));
		mSpinner_minutes.setPadding(10, 0, 4, 0);
		// mSpinner_minutes.setGravity(Gravity.CENTER_VERTICAL);

		ArrayAdapter<String> spinner_minutesArrayAdapter = new ArrayAdapter<String>(
				this, R.layout.cal_spinner_item, R.id.list, this.getResources()
						.getStringArray(R.array.reminder_minutes_labels));

		mSpinner_minutes.setAdapter(spinner_minutesArrayAdapter);
		// if(minute.length()>2)
		// {
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
						CalendarLog.d(CalendarConstants.Tag, "minutes");
						if (isFirst > mReminderValues.size()) {
							isEdited = true;
						} else {
							isFirst++;
						}

						mContentValueReminder
								.put(Reminder.MINUTES,
										getResources()
												.getStringArray(
														R.array.reminder_minutes_values)[position]);
						// Log.d("Tag",contentValueReminder.toString());
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {

					}

				});

		mSpinner_notification = (Spinner) inflater.inflate(
				R.layout.cal_styled_spinner, null);
		mSpinner_notification.setPadding(10, 0, 4, 0);
		mSpinner_notification.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 2));
		// mSpinner_notification.setGravity(Gravity.CENTER_VERTICAL);
		ArrayAdapter<String> spinner_notificationArrayAdapter = new ArrayAdapter<String>(
				this, R.layout.cal_spinner_item, R.id.list, this.getResources()
						.getStringArray(R.array.reminder_methods_labels));
		mSpinner_notification.setAdapter(spinner_notificationArrayAdapter);
		if (method.equals("4")) {
			mSpinner_notification.setSelection(Arrays.asList(
					this.getResources().getStringArray(
							R.array.reminder_methods_labels)).indexOf(
					"Notification"));
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
						CalendarLog.d(CalendarConstants.Tag, "notification");
						if (isFirst > mReminderValues.size()) {
							isEdited = true;
						} else {
							isFirst++;
						}
						if (mSpinner_notification.getItemAtPosition(position)
								.toString()
								.equals(CalendarConstants.NOTIFICATION)) {
							mContentValueReminder.put(Reminder.METHOD,
									Reminder.METHOD_ALARM);

						} else {
							mContentValueReminder.put(Reminder.METHOD,
									Reminder.METHOD_EMAIL);
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
				CalendarLog.d(CalendarConstants.Tag, "cancel");
				isEdited = true;
				mReminderParentLayout.removeView((View) v.getParent());
				mContentValueReminder.remove(Reminder.MINUTES);
				mContentValueReminder.remove(Reminder.METHOD);

			}
		});

		mReminderLayout.addView(mSpinner_minutes);
		mReminderLayout.addView(mSpinner_notification);
		mReminderLayout.addView(mBtn_cancel);

		// Save Content Values to List
		if ((mContentValueReminder.get(Reminder.MINUTES) != null)
				&& (mContentValueReminder.get(Reminder.METHOD) != null)) {
			mSaveReminderValueList.add(mContentValueReminder);
		}

		return mReminderLayout;
	}

	/**
	 * Add Reminder view
	 */
	private void addReminderView(View view2, int id) {

		try {
			LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mReminderParentLayout = new LinearLayout(CalendarEditEvent.this);
			LinearLayout.LayoutParams reminderParentLayoutParam = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			reminderParentLayoutParam.setMargins(0, 10, 0, 0);
			mReminderParentLayout.setLayoutParams(reminderParentLayoutParam);
			mReminderParentLayout.setOrientation(LinearLayout.VERTICAL);

			mTxt_edit_event_add_reminder = (TextView) mInflater.inflate(
					R.layout.cal_styled_textview, null);
			LinearLayout.LayoutParams txt_edit_event_add_reminderLayoutParam = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			int left = Email.convertToDp(20);
			txt_edit_event_add_reminderLayoutParam.setMargins(left, 10, 0, 0);
			mTxt_edit_event_add_reminder
					.setLayoutParams(txt_edit_event_add_reminderLayoutParam);
			mTxt_edit_event_add_reminder
					.setText(CalendarConstants.ADD_REMINDER);
			mTxt_edit_event_add_reminder.setTextSize(16);

			for (j = 0; j < mReminderValues.size(); j++) {

				ContentValues setReminder = mReminderValues.get(j);
				mReminderParentLayout.addView(addChildEventReminderView(
						setReminder.getAsString(Reminder.MINUTES),
						setReminder.getAsString(Reminder.METHOD)));

				isEdited = false;
				CalendarLog
						.d(CalendarConstants.Tag, "add_reminder+" + isEdited);
			}
			mTxt_edit_event_add_reminder
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							CalendarLog
									.d(CalendarConstants.Tag, "add_reminder");
							isEdited = true;
							// addReminderView(v, 2);
							j++;
							mReminderParentLayout.setTag(j);

							String defaultminuts = mSecurePrefs
									.getString(
											getString(R.string.key_default_reminderlist_preference),
											"");
							if (defaultminuts.equalsIgnoreCase("")) {
								defaultminuts = "0";
							}
							mReminderParentLayout
									.addView(addChildEventReminderView(
											defaultminuts, "4"));

						}
					});

			// reminderParentLayout.addView(addChildEventReminderView(i,"0","Notification"));
			mParentLayout.addView(mReminderParentLayout);
			mParentLayout.addView(mTxt_edit_event_add_reminder);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public boolean isTablet(Context context) {
		boolean xlarge = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == 4);
		boolean large = ((context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE);
		return (xlarge || large);
	}

	private String capitalize(String line) {
		return Character.toUpperCase(line.charAt(0))
				+ line.substring(1).toLowerCase();
	}

	private String reccurrenceRrule(String rrule) {
		String modifiedrrule = "";
		String[] rruleArr = rrule.split(";");
		for (String splitrrule : rruleArr) {
			if (splitrrule.contains("FREQ")) {
				String[] splitrruleArr = splitrrule.split("=");
				modifiedrrule = capitalize(splitrruleArr[1]);
			}
			if (splitrrule.contains("UNTIL")) {
				String[] splitrruleArr = splitrrule.split("=");
				modifiedrrule = modifiedrrule + "; until "
						+ rrecurenceUntill(splitrruleArr[1]);
			}
			if (splitrrule.contains("COUNT")) {
				String[] splitrruleArr = splitrrule.split("=");
				modifiedrrule = modifiedrrule + "; for " + splitrruleArr[1]
						+ " times";
			}
		}
		return modifiedrrule;
	}

	private String rrecurenceUntill(String date) {
		final String pattern = "yyyyMMdd'T'HHmmss'Z'";
		;
		final SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
		SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy", Locale.US);
		// Log.i("Tag", "Date : "+date);
		Date d = null;
		try {
			d = sdf.parse(date);
			date = df.format(d);
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return date;
	}

	/**
	 * Inital viewcontext
	 */
	private void init() {

		try {

			mBtn_back = (LinearLayout) findViewById(R.id.btn_back_edit_event_actionbar_layout);
			mBtn_back.setOnClickListener(btn_back_listener);
			if (!isTablet(this)) {
				mBtn_back.setVisibility(View.VISIBLE);
			}

			if (mBtn_back.getChildCount() > 0) {
				ImageButton backImage = (ImageButton) mBtn_back.getChildAt(0);
				backImage.setOnClickListener(btn_back_listener);

				ImageButton backImageApp = (ImageButton) mBtn_back
						.getChildAt(1);
				backImageApp.setOnClickListener(btn_back_listener);
			}
			mTxt_edit = (TextView) findViewById(R.id.btn_edit_event_actionbar_layout);
			mTxt_edit.setOnClickListener(btn_edit_listener);
			String calendarType = mEventValues.get(
					CalendarConstants.CALENDAR_TYPE_KEY).toString();

			mTxt_delete = (TextView) findViewById(R.id.btn_delete_edit_event_actionbar_layout);
			mTxt_delete.setOnClickListener(btn_delete_listener);

			mTxt_forward = (TextView) findViewById(R.id.btn_forward_event_actionbar_layout);
			mTxt_forward.setOnClickListener(btn_forward_listener);

			ImageView view_event_for = (ImageView) findViewById(R.id.view_event_for);
			if (calendarType
					.equalsIgnoreCase(CalendarConstants.CALENDAR_TYPE_CORPORATE)) {

				mTxt_edit.setVisibility(View.VISIBLE);
				mTxt_delete.setVisibility(View.VISIBLE);
				view_event_for.setVisibility(View.VISIBLE);
			} else {
				mTxt_edit.setVisibility(View.GONE);
				mTxt_delete.setVisibility(View.GONE);
				view_event_for.setVisibility(View.GONE);
			}
			
			if(mEventValues == null || mEventValues.getAsInteger("_id") == null){
				Toast.makeText(this, CalendarConstants.EVENT_PROBLEM,
						Toast.LENGTH_SHORT).show();
				finish();
			}
			// editContentLayout = (RelativeLayout) layout
			// .findViewById(R.id.edit_event_titleLayout);
			// editContentLayout.setBackgroundResource(R.color.event_bgcolor);
			// TextView txt_view_event_color = (TextView) layout
			// .findViewById(R.id.txt_view_event_color);

			// String colorBG = eventValues.getAsString("eventBGColor");

			// txt_view_event_color.setBackgroundColor(0xff000000 +
			// Integer.parseInt(colorBG));
			String eventCalendar = CalendarDatabaseHelper.getEmail_ID();
			if (mEventValues.getAsString(Event.ORGANIZER).equalsIgnoreCase(
					eventCalendar)) {
				mTxt_forward.setVisibility(View.GONE);
			}

			mTxt_edit_event_name = (TextView) findViewById(R.id.txt_edit_event_name);
			mTxt_edit_event_name.setText(mEventValues.getAsString(Event.TITLE));
			mTxt_edit_event_time = (TextView) findViewById(R.id.txt_edit_event_time);
			mTxt_edit_event_rrule = (TextView) findViewById(R.id.txt_edit_event_rrule);
			String rrule = mEventValues.getAsString(Event.RRULE);
			CalendarLog.d(CalendarConstants.Tag, "rrule:" + rrule);
			if (rrule == null) {
				rrule = "";
			}
			if (!rrule.equalsIgnoreCase("")) {
				mTxt_edit_event_rrule.setText(reccurrenceRrule(rrule));
				mTxt_edit_event_rrule.setVisibility(View.VISIBLE);
			}
			String time = "";

			long startTimeLong = mEventValues.getAsLong(Event.DTSTART);
			long endTimeLong = mEventValues.getAsLong(Event.DTEND);
//			String allDay = mEventValues.getAsString(Event.ALL_DAY);
//			if (allDay.equalsIgnoreCase("1")) {
//				endTimeLong=endTimeLong-60000;
//			}
			
			String startDate = CalendarDatabaseHelper.getDateFromLong(startTimeLong, CalendarDatabaseHelper.getDefaultTimeZone());
			String endDate = CalendarDatabaseHelper.getDateFromLong(endTimeLong, CalendarDatabaseHelper.getDefaultTimeZone());
			String endtime = CalendarDatabaseHelper
					.getTimeFromLong(mEventValues.getAsLong(Event.DTEND),CalendarDatabaseHelper.getDefaultTimeZone());
//			CalendarLog.d(CalendarConstants.Tag,"aish init values startTime");
			String starttime = CalendarDatabaseHelper
					.getTimeFromLong(mEventValues.getAsLong(Event.DTSTART),CalendarDatabaseHelper.getDefaultTimeZone());
			if (startDate.equalsIgnoreCase(endDate)) {

				if (endtime.equalsIgnoreCase("00:00")
						&& starttime.equalsIgnoreCase("00:00")) {
					time = CalendarDatabaseHelper.getDateForAgenda(startDate);
					time = time.replace(",", "");

				} else if (endtime.equalsIgnoreCase("00:00")) {
					time = CalendarDatabaseHelper.getDateForAgenda(mEventValues
							.getAsString(Event.START_DATE))
							+ " "
							+ starttime
							+ " -  midnight";

				} else {
					time = CalendarDatabaseHelper.getDateForAgenda(startDate)
							+ " " + starttime + " - " + endtime;
				}
			} else {
				if (endtime.equalsIgnoreCase("00:00")
						&& (endTimeLong - startTimeLong) < 86400000) {
					time = CalendarDatabaseHelper.getDateForAgenda(startDate)
							+ " " + starttime + " -  midnight";

				} else if (endtime.equalsIgnoreCase("00:00")) {
					// String newEndDate =
					// CalendarCommonFunction.convertMilliSecondsToDate(endTimeLong-86400000);
					time = CalendarDatabaseHelper.getDateForAgenda(startDate)
							+ " " + starttime + " - "
							+ CalendarDatabaseHelper.getDateForAgenda(endDate)
							+ " -  midnight";

				} else
					time = CalendarDatabaseHelper.getDateForAgenda(startDate)
							+ " " + starttime + " - "
							+ CalendarDatabaseHelper.getDateForAgenda(endDate)
							+ " " + endtime;
			}

			mTxt_edit_event_time.setText(time+" "+CalendarDatabaseHelper.getTimeZoneOffsetFromString(CalendarDatabaseHelper.getDefaultTimeZone()));

			mTxt_edit_event_location = (TextView) findViewById(R.id.txt_edit_event_location);

			mTxt_edit_event_location.setClickable(true);
			mTxt_edit_event_location.setMovementMethod(LinkMovementMethod
					.getInstance());
			String location = mEventValues.getAsString(Event.EVENT_LOCATION);
			if (location == null)
				location = "";
			if (location.equalsIgnoreCase("")) {
				mTxt_edit_event_location.setVisibility(View.GONE);
			} else {
				mTxt_edit_event_location.setVisibility(View.VISIBLE);
			}
			String text = "<u>" + location + " </u>";

			mTxt_edit_event_location.setText(Html.fromHtml(text));
			mTxt_edit_event_location.setTextColor(Color.BLUE);
			mTxt_edit_event_location.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					CalendarCommonFunction.callSecuredBrowser(
							CalendarEditEvent.this,
							CalendarConstants.googleMapsURL
									+ mTxt_edit_event_location.getText()
											.toString());
				}
			});
			mTxt_edit_event_description = (TextView) findViewById(R.id.txt_edit_event_description);
			String eventDescription = mEventValues
					.getAsString(com.cognizant.trumobi.calendar.modal.Event.DESCRIPTION);
			if (eventDescription != null) {
				String[] parts = eventDescription.split(" ");
				// CalendarLog.d("eventDescription",
				// "parts>>>>>>>>>>>>"+parts.length);
				StringBuilder strEventDescriptionBuiler = new StringBuilder();
				for (String items : parts) {
					// CalendarLog.d("get Href for", ":::"+items);
					// if(items.startsWith("\n")){
					// CalendarLog.d("get Href Neline", ":::"+items.length());
					// }
					if (items.startsWith("http://")
							|| (items.startsWith("https://"))
							|| (items.startsWith("www."))) {
						// CalendarLog.d("get Href if ", ":::"+items);
						if (items.contains("<")) {
							items = items.substring(0, items.indexOf("<"));
							// CalendarLog.d("get Href if 1", ":::"+items);

						}
						strEventDescriptionBuiler.append(items + " ");

					} else if (items.contains("<http://")) {

						int startIndex = items.indexOf("<http://");
						int endIndex = items.indexOf(">");
						String replacement = "";
						// CalendarLog.d("get Href else if 1 ",
						// ":::"+startIndex+"--"+endIndex);
						String toBeReplaced = items.substring(startIndex,
								endIndex + 1);

						items = items.replace(toBeReplaced, replacement);
						// CalendarLog.d("get Href else if 1 ", ":::"+items);
						strEventDescriptionBuiler.append(items + " ");
					} else if (items.contains("<https://")) {
						int startIndex = items.indexOf("<https://");
						int endIndex = items.indexOf(">");
						String replacement = "";
						// CalendarLog.d("get Href else if 1 ",
						// ":::"+startIndex+"--"+endIndex);
						String toBeReplaced = items.substring(startIndex,
								endIndex + 1);

						items = items.replace(toBeReplaced, replacement);
						// CalendarLog.d("get Href else if 2 ", ":::"+items);
						strEventDescriptionBuiler.append(items + " ");
					} else {
						// CalendarLog.d("get Href else ", ":::"+items);
						strEventDescriptionBuiler.append(items + " ");
					}

					// CalendarLog.d("get Href",
					// "strBuiler >>>>:"+strEventDescriptionBuiler.toString());
				}
				// CalendarLog.e("get Href",
				// "strBuiler ::"+strEventDescriptionBuiler.toString());
				mTxt_edit_event_description
						.setMovementMethod(LinkMovementMethod.getInstance());
				mTxt_edit_event_description
						.setText(
								setSpannablePropertyForDescription(strEventDescriptionBuiler
										.toString()), BufferType.SPANNABLE);

			} else {
				mTxt_edit_event_description.setVisibility(View.GONE);
			}
			// mTxt_edit_event_description.setText(eventDescription);
			if (eventDescription == null) {
				mTxt_edit_event_description.setVisibility(View.GONE);
			}
			mTxt_edit_event_mail = (TextView) findViewById(R.id.txt_edit_event_mail);
			mTxt_edit_event_mail.setText("Calendar:" + eventCalendar);
			// txt_edit_event_reminder = (TextView) layout
			// .findViewById(R.id.txt_edit_event_remainder);

			mResponseRow = (LinearLayout) findViewById(R.id.response_row);
			mResponseType = (RadioGroup) mResponseRow
					.findViewById(R.id.response_value);
			CalendarLog.d(
					CalendarConstants.Tag,
					"event values obt for edit event is "
							+ mEventValues.toString());
			if (!mEventValues.getAsString(Event.ORGANIZER).equalsIgnoreCase(
					CalendarDatabaseHelper.getEmail_ID())) {
				mResponseType.check(CalendarDatabaseHelper
						.getResponseTypeId(mEventValues
								.getAsInteger(Event.RESPONSE_TYPE)));
				mTxt_edit.setVisibility(View.GONE);

			} else
				mResponseRow.setVisibility(View.GONE);
			mResponseStatusChangedListener = new OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(RadioGroup group, int checkedId) {

					CalendarDatabaseHelper.changeResponseType(mEventValues
							.getAsInteger("_id"), CalendarDatabaseHelper
							.getResponseType(mResponseType
									.getCheckedRadioButtonId()));
					isEdited = true;
				}
			};
			mResponseType
					.setOnCheckedChangeListener(mResponseStatusChangedListener);

			// LinearLayout noResponseRow = (LinearLayout)
			// findViewById(R.id.response_row);
			// noResponseRow.setVisibility(View.GONE);
			addAttendeesNoResponse();
			addAttendeesMayBe();
			addAttendeesYes();
			addAttendeesNo();
			mReminderValues = CalendarDatabaseHelper
					.getReminderValues(mEventValues.getAsInteger("_id"));

			addReminderView(mView, 1);
			// this.getResources().getStringArray(R.array.reminder_minutes_labels);
			CalendarLog.d(CalendarConstants.Tag, "add_reminder init end :"
					+ isEdited);
			isEdited = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addAttendeesNoResponse() {
		try {
			ArrayList<Attendee> mAttendee = CalendarDatabaseHelper
					.getAttendeeValuesNoResponse(mEventValues.getAsInteger("_id"));
			//

			LinearLayout noResponse = (LinearLayout) findViewById(R.id.guest_noresponse_item);
			if (noResponse.getChildCount() > 0)
				noResponse.removeAllViews();
			if (mAttendee.size() == 0) {
				LinearLayout noResponseRow = (LinearLayout) findViewById(R.id.guest_noresponse_row);
				noResponseRow.setVisibility(View.GONE);
			} else {
				TextView noResponseLabel = (TextView) findViewById(R.id.guest_noresponse_label);
				noResponseLabel.setText(getResources().getString(
						R.string.response_no_response).toUpperCase()
						+ " (" + mAttendee.size() + ")");
			}
			addAttendees(mAttendee, noResponse);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addAttendeesMayBe() {
		try {
			ArrayList<Attendee> mAttendee = CalendarDatabaseHelper
					.getAttendeeValuesTentative(mEventValues
							.getAsInteger("_id"));
			// LayoutInflater inflater = (LayoutInflater)
			// getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			LinearLayout noResponse = (LinearLayout) findViewById(R.id.guest_maybe_item);
			if (noResponse.getChildCount() > 0)
				noResponse.removeAllViews();
			if (mAttendee.size() == 0) {
				LinearLayout noResponseRow = (LinearLayout) findViewById(R.id.guest_maybe_row);
				noResponseRow.setVisibility(View.GONE);
			} else {
				TextView noResponseLabel = (TextView) findViewById(R.id.guest_maybe_label);
				noResponseLabel.setText(getResources().getString(
						R.string.response_maybe).toUpperCase()
						+ " (" + mAttendee.size() + ")");
			}
			addAttendees(mAttendee, noResponse);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addAttendeesNo() {
		try {
			ArrayList<Attendee> mAttendee = CalendarDatabaseHelper
					.getAttendeeValuesDecline(mEventValues.getAsInteger("_id"));
			// LayoutInflater inflater = (LayoutInflater)
			// getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			LinearLayout noResponse = (LinearLayout) findViewById(R.id.guest_no_item);
			if (noResponse.getChildCount() > 0)
				noResponse.removeAllViews();
			if (mAttendee.size() == 0) {
				LinearLayout noResponseRow = (LinearLayout) findViewById(R.id.guest_no_row);
				noResponseRow.setVisibility(View.GONE);
			} else {
				TextView noResponseLabel = (TextView) findViewById(R.id.guest_no_label);
				noResponseLabel.setText(getResources().getString(
						R.string.response_no).toUpperCase()
						+ " (" + mAttendee.size() + ")");
			}
			addAttendees(mAttendee, noResponse);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addAttendeesYes() {
		try {
			ArrayList<Attendee> mAttendee = CalendarDatabaseHelper
					.getAttendeeValuesAccepted(mEventValues.getAsInteger("_id"));
			// LayoutInflater inflater = (LayoutInflater)
			// getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			LinearLayout noResponse = (LinearLayout) findViewById(R.id.guest_yes_item);
			if (noResponse.getChildCount() > 0)
				noResponse.removeAllViews();
			if (mAttendee.size() == 0) {
				LinearLayout noResponseRow = (LinearLayout) findViewById(R.id.guest_yes_row);
				noResponseRow.setVisibility(View.GONE);
			} else {
				TextView noResponseLabel = (TextView) findViewById(R.id.guest_yes_label);
				noResponseLabel.setText(getResources().getString(
						R.string.response_yes).toUpperCase()
						+ " (" + mAttendee.size() + ")");
			}
			addAttendees(mAttendee, noResponse);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addAttendees(ArrayList<Attendee> mAttendee,
			LinearLayout noResponse) {
		// LayoutInflater inflater = (LayoutInflater)
		// getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		try {
			for (final Attendee attendee : mAttendee) {
				LinearLayout attendeeView = new LinearLayout(this);
				LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				param.setMargins(0, 2, 0, 2);
				attendeeView.setLayoutParams(param);
				LinearLayout child = new LinearLayout(this);
				TextView title = new TextView(this);				
				title.setText(attendee.attendeeEmail);
				title.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
	                    Intent addContact = new Intent(getBaseContext(),ContactsAddContact.class);
	                    Bundle data = new Bundle();
	                    ContactsModel contact=new ContactsModel();
//	                    contact.setcontacts_first_name(attendee.attendeeName);
	                    contact.setcontacts_email1_address(attendee.attendeeEmail);
	                    data.putSerializable("obj", contact);
	                    addContact.putExtras(data);
	                    startActivity(addContact);
					}
				});
				child.addView(title);
				attendeeView.addView(child);

				noResponse.addView(attendeeView);
			}				
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private CharSequence setSpannablePropertyForDescription(String str) {

		try {
			if (str != null && !str.equals("")) {
				ssb = new SpannableStringBuilder(str);
				String[] parts = str.split("\\s");

				for (String items : parts) {

					if (items.startsWith("http://")
							|| (items.startsWith("https://"))
							|| (items.startsWith("www."))) {
						int idx1 = str.indexOf(items);
						int idx2 = items.length();
						final String clickString = str.substring(idx1,
								(idx1 + idx2));
						ssb.setSpan(new ClickableSpan() {
							@Override
							public void onClick(View widget) {
								CalendarCommonFunction.callSecuredBrowser(
										CalendarEditEvent.this, clickString);
							}
						}, idx1, (idx1 + idx2), 0);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ssb;
	}

	@Override
	public void onDestroy() {

		super.onDestroy();
	}

	/**
	 * save data in Bundle
	 */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putParcelable(CalendarConstants.EVENTVALUES,
				mEventValues);
		savedInstanceState.putInt("dialogState", dialogState);
		super.onSaveInstanceState(savedInstanceState);
	}

	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		dialogState = savedInstanceState.getInt("dialogState");
	}

	private OnClickListener btn_back_listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			onBackPressed();
		}
	};

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		try {
			saveEvent();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void saveEvent() {
		try {
			ArrayList<Reminder> reminder1 = new ArrayList<Reminder>();
			if (!isEdited){			
			
			for (i = 0; i < mReminderParentLayout.getChildCount(); i++) {
				LinearLayout childReme = (LinearLayout) mReminderParentLayout
						.getChildAt(i);
				Spinner spMinutes = (Spinner) childReme.getChildAt(0);
				Spinner spEmail = (Spinner) childReme.getChildAt(1);
				int reminder = (int) spMinutes.getSelectedItemId();
				final String[] reminderminutes = getResources()
						.getStringArray(R.array.reminder_minutes_values);
				reminder = Integer.parseInt(reminderminutes[reminder]);
				int notification = (int) spEmail.getSelectedItemId();
				int notificationValue;
				if (notification == 0) {
					notificationValue = 4;
				} else {
					notificationValue = 2;
				}
				// CalendarDatabaseHelper.changeStatusToModified(eventValues.getAsInteger("_id"));
				
				reminder1.add(new Reminder(mEventValues
								.getAsInteger(CalendarConstants._ID),
								reminder, notificationValue));// Integer.parseInt(reminderminutes[reminder])
			}
			
			ArrayList<Reminder> reminders =  CalendarDatabaseHelper
					.convertReminderContentValuesToReminder(CalendarDatabaseHelper
					.getReminderValues(mEventValues.getAsInteger("_id")));
			isEdited = !(compareReminder(reminders, reminder1)) ;
				
			
			}
			if (isEdited) {							
					CalendarDatabaseHelper.deleteRemindersForEvent(mEventValues
							.getAsInteger(CalendarConstants._ID));
					
					for (i = 0; i < mReminderParentLayout.getChildCount(); i++) {
						CalendarLog.d(CalendarConstants.Tag,"Reminder  inserted :  change");
						LinearLayout childReme = (LinearLayout) mReminderParentLayout
								.getChildAt(i);
						Spinner spMinutes = (Spinner) childReme.getChildAt(0);
						Spinner spEmail = (Spinner) childReme.getChildAt(1);
						int reminder = (int) spMinutes.getSelectedItemId();
						final String[] reminderminutes = getResources()
								.getStringArray(R.array.reminder_minutes_values);
						reminder = Integer.parseInt(reminderminutes[reminder]);
						int notification = (int) spEmail.getSelectedItemId();
						int notificationValue;
						if (notification == 0) {
							notificationValue = 4;
						} else {
							notificationValue = 2;
						}
						 CalendarDatabaseHelper.changeStatusToModified(mEventValues.getAsInteger("_id"));
						
						CalendarDatabaseHelper.insertReminderToDatabase(
							new Reminder(mEventValues
									.getAsInteger(CalendarConstants._ID),
									reminder, notificationValue), false);
					}
//					for (Reminder remindar : reminder1) {
//						 CalendarDatabaseHelper.changeStatusToModified(mEventValues.getAsInteger("_id"));
//						
////						CalendarDatabaseHelper.insertReminderToDatabase(
////								new Reminder(mEventValues
////										.getAsInteger(CalendarConstants._ID),
////										reminder, notificationValue), false);
//						CalendarDatabaseHelper.insertReminderToDatabase(
//								remindar, false);// Integer.parseInt(reminderminutes[reminder])
//					}
					Toast.makeText(getApplicationContext(),
							CalendarConstants.EVENT_SAVED_SUCCESSFULLY,
							Toast.LENGTH_LONG).show();
					CalendarDatabaseHelper.manualSync();

				}
			
		} catch (NumberFormatException e) {

			e.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	private OnClickListener btn_edit_listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			try {
				// Launch New Event
				// String recurrenceType =
				// mEventValues.getAsString(Recurrence.TYPE);
				// CalendarLog.d(CalendarConstants.Tag, "Recur"+recurrenceType);
				// if(recurrenceType.equalsIgnoreCase("-1"))
				callEditEvents(false);
				// else{
				// showRecurrenceDialog(mEventValues.getAsString(Event.TITLE),
				// true);
				// }
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	};

	private void callEditEvents(boolean isRecurrence) {
		try {
			Email.setEditFlag(true);
			Bundle contentValue = new Bundle();
			setCurrentEventID(mEventValues.getAsString(CalendarConstants._ID));
			setCurrentEventType(mEventValues
					.getAsString(CalendarConstants.CALENDAR_TYPE_KEY));

			contentValue.putParcelable(CalendarConstants.EVENT_CONTENT_VALUES,
					mEventValues);
			if (!mEventValues.getAsString(CalendarConstants.CALENDAR_TYPE_KEY)
					.equals(CalendarConstants.CALENDAR_TYPE_PERSONAL)) {
				Intent eventIntent = new Intent(
						CalendarEditEvent.this,
						com.cognizant.trumobi.calendar.event.CalendarAddEvent.class);
				eventIntent.putExtras(contentValue);
				startActivity(eventIntent);
				finish();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private OnClickListener btn_delete_listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			try {
				String recurrenceType = mEventValues.getAsString(Recurrence.TYPE);
				// CalendarLog.d(CalendarConstants.Tag, "Recur"+recurrenceType);
				if (recurrenceType.equalsIgnoreCase("-1"))
					showDialog();
				else {
					showRecurrenceDialog(mEventValues.getAsString(Event.TITLE),
							true);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	private OnClickListener btn_forward_listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			CalendarDatabaseHelper.forwardEvent(CalendarDatabaseHelper
					.getEventDetails(mEventValues.getAsInteger("_id")));
		}
	};

	void deleteEventSync(int id) {

		try {
			Event event = new Event();

			event = CalendarDatabaseHelper.getEventDetails(id);
			if (CalendarDatabaseHelper.deleteEventDetails(event) != 0)
				Toast.makeText(this, CalendarConstants.EVENT_DELETED,
						Toast.LENGTH_SHORT).show();

			CalendarDatabaseHelper.manualSync();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void deleteFutureEvent(int id) {
		try {
			if (mEventValues.containsKey(CalendarConstants.RECURRENCE_CURRENTDATE)) {
				Event newEvent = new Event();
				newEvent = CalendarDatabaseHelper.getEventDetails(id);

				String recurrCurrendate = mEventValues
						.getAsString(CalendarConstants.RECURRENCE_CURRENTDATE);
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
				cal.set(Calendar.DAY_OF_YEAR, -1);
				Recurrence recurrence = newEvent.recurrence;
				recurrence.until = cal.getTimeInMillis();
				recurrence.occurences = -1;

				newEvent.recurrence = recurrence;
				CalendarDatabaseHelper.insertEventToDatabase(newEvent);
				CalendarDatabaseHelper.manualSync();
				Toast.makeText(this, CalendarConstants.EVENT_DELETED,
						Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void deleteThisEvent(int id) {
		try {
			if (mEventValues.containsKey(CalendarConstants.RECURRENCE_CURRENTDATE)) {
				Event newEvent = new Event();
				newEvent = CalendarDatabaseHelper.getEventDetails(id);

				String recurrCurrendate = mEventValues
						.getAsString(CalendarConstants.RECURRENCE_CURRENTDATE);
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
				// cal.add(Calendar.DAY_OF_YEAR, -1);
				newEvent.hasException = 1;
				Exceptions exceptions = new Exceptions();
				exceptions._id = newEvent._id;
				exceptions.accessibility = newEvent.accessibility;
				exceptions.allDay = exceptions.allDay;
				exceptions.attendees = newEvent.getAttendees();
				exceptions.bg_color = newEvent.bg_color;
				exceptions.calendarUID = newEvent.calendarUID;
				exceptions.description = newEvent.description;
				exceptions.dtend = newEvent.dtend;
				exceptions.dtstart = newEvent.dtstart;
				exceptions.endDate = newEvent.endDate;
				exceptions.event_id = newEvent.event_id;
				exceptions.eventEndTimezone = newEvent.eventEndTimezone;
				exceptions.eventLocation = newEvent.eventLocation;
				exceptions.eventTimezone = newEvent.eventTimezone;
				exceptions.isExceptionDeleted = 1;
				exceptions.exdate = exceptions.exdate;
				exceptions.exrule = newEvent.exrule;

				exceptions.organizer = newEvent.organizer;
				exceptions.rdate = newEvent.rdate;
				exceptions.startDate = newEvent.startDate;
				ArrayList<Exceptions> exceptionsArray = new ArrayList<Exceptions>();
				exceptionsArray.add(exceptions);
				newEvent.exceptions = exceptionsArray;
				 newEvent.setStatus(Event.STATUS_MODIFIED);
				CalendarDatabaseHelper.insertEventToDatabase(newEvent);
				// CalendarDatabaseHelper.insertExceptionList(newEvent, id);//
				// deleteException(id,
				// CalendarCommonFunction.convertMilliSecondsToDate(cal.getTimeInMillis()));
				CalendarDatabaseHelper.manualSync();
				Toast.makeText(this, CalendarConstants.EVENT_DELETED,
						Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void showRecurrenceDialog(String title, final boolean isEdit) {
		try {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					CalendarEditEvent.this);
			builder.setTitle(title + "?");
			// builder.setIcon(android.R.id.)
			final String[] rest = new String[3];
			// if (isEdit) {
			// rest[0] = "Change all events in the series.";
			// rest[1] = "Change this and all future events";
			// } else {
			// For delete
			rest[0] = "Only this event";
			rest[1] = "This and future events";
			rest[2] = "All events";
			// }
			// final AlertDialog alert = builder.create();

			if (mEventValues.containsKey(CalendarConstants.RECURRENCE_CURRENTDATE)) {
				CalendarLog
						.d(CalendarConstants.Tag,
								"RECURRENCE_CURRENTDATE"
										+ mEventValues
												.getAsString(CalendarConstants.RECURRENCE_CURRENTDATE));
			}
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					isEdited = false;
					CalendarLog.d(CalendarConstants.Tag,
							"RECURRENCE_CURRENTDATE mRecurDeletedItem"
									+ mRecurDeletedItem);
					if (mRecurDeletedItem == 2) {
						deleteEventSync(mEventValues.getAsInteger("_id"));
						finish();
//						onBackPressed();
					} else if (mRecurDeletedItem == 1) {
						deleteFutureEvent(mEventValues.getAsInteger("_id"));
						finish();
//						onBackPressed();
					} else if (mRecurDeletedItem == 0) {
						deleteThisEvent(mEventValues.getAsInteger("_id"));
						finish();
//						onBackPressed();
					}
					dialog.cancel();
				}
			});
			builder.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (mEventValues
									.containsKey(CalendarConstants.RECURRENCE_CURRENTDATE)) {
								CalendarLog
										.d(CalendarConstants.Tag,
												"RECURRENCE_CURRENTDATE"
														+ mEventValues
																.getAsString(CalendarConstants.RECURRENCE_CURRENTDATE));
							}
							dialogState = 0;
							dialog.cancel();
							mRecurDeletedItem = -1;

						}
					});

			builder.setSingleChoiceItems(rest, -1,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							try {
								((AlertDialog) dialog).getButton(
										DialogInterface.BUTTON_POSITIVE)
										.setEnabled(true);
								mRecurDeletedItem = item;
							} catch (Exception e) {
							}
						}
					});
			final AlertDialog alert = builder.create();
			dialogState = 2;
			alert.show();

			alert.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void showDialog() {

		try {
			final Dialog dialog = new Dialog(this, R.style.FullHeightDialog);
			dialog.setContentView(R.layout.cal_custom_alert);
			dialog.setCancelable(false);
			dialog.setCanceledOnTouchOutside(false);
			TextView text = (TextView) dialog.findViewById(R.id.dialogText);
			text.setText(R.string.edit_event_dialog);
			Button dialogButtonOk = (Button) dialog
					.findViewById(R.id.dialogButtonOK);
			// if button is clicked, close the custom dialog
			dialogButtonOk.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					deleteEventSync(mEventValues.getAsInteger("_id"));
					finish();
					dialog.dismiss();
				}
			});
			Button dialogButtonCancel = (Button) dialog
					.findViewById(R.id.dialogButtonCancel);
			// if button is clicked, close the custom dialog
			dialogButtonCancel.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialogState = 0;
					dialog.dismiss();
				}
			});
			dialogState = 1;
			dialog.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void deleteEvent() {
		try {
			deleteEventSync(mEventValues.getAsInteger("_id"));
			onBackPressed();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void setCurrentEventID(String currenteventID) {
		sCureventID = currenteventID;
	}

	public static String getCurrentEventID() {
		return sCureventID;
	}

	public static void setCurrentEventType(String curreventType) {
		sCureventType = curreventType;
	}

	public static String getCurrentEventType() {
		return sCureventType;
	}
	
	private boolean compareReminder(ArrayList<Reminder> reminders1,ArrayList<Reminder> reminders2){
		if (reminders1 != null && !reminders1.isEmpty()) {
			if (reminders2 != null && !reminders2.isEmpty()) {
				if (reminders1.size() == reminders2.size()) {
					ArrayList<Integer> reminderList1 = CalendarDatabaseHelper.getReminderMinutesFromReminders(reminders1);
					ArrayList<Integer> reminderList2 = CalendarDatabaseHelper.getReminderMinutesFromReminders(reminders2);
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
			if (reminders2 != null && !reminders2.isEmpty()) {
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