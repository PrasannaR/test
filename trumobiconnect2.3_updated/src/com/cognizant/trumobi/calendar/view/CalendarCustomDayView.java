package com.cognizant.trumobi.calendar.view;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.calendar.event.CalendarAddEvent;
import com.cognizant.trumobi.calendar.modal.Event;
import com.cognizant.trumobi.calendar.provider.CalendarDBHelperClassPreICS;
import com.cognizant.trumobi.calendar.provider.CalendarDatabaseHelper;
import com.cognizant.trumobi.calendar.util.CalendarCommonFunction;
import com.cognizant.trumobi.calendar.util.CalendarConstants;
import com.cognizant.trumobi.calendar.util.CalendarTextRect;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.log.CalendarLog;

public class CalendarCustomDayView extends View implements OnTouchListener {

	public int mGetTopYPosition = 0, mViewWidth = 0, mViewHeight = 0;
	public static int LONG_PRESS_TIME = 500;
	public Paint p, textp, roundRectP, defHour, lineHour, textevent;

	private Context mContext;
	private int mParentWidth = 0, X_OFFSET = 5, Y_OFFSET = 5,
			HOUR_BLOCK_HEIGHT = 60;
	private int mFont_max_width = 0, mHours_font_max_width = 0,
			mFont_height = 0, mNo_of_events = 1, mScreenwidth = 0,
			mHoursTitle = 0;

	private int[] mYCordinates;
	private ArrayList<ContentValues> mDayEventValues;
	private ArrayList<CalendarEventRect> mCalendarRectEventValues;
	private RectF mNeweventrect = null;
	private boolean isLongpresed = false;
	public boolean isRedaw = true;
	private Canvas mCanvas = null;

	// private int[] mLocation = { 0, 0 };

	public CalendarCustomDayView(Context context,
			ArrayList<ContentValues> dayEventValues, String presentday) {
		super(context);
		this.mContext = context;
		this.setOnCreateContextMenuListener(vC);
		this.mDayEventValues = dayEventValues;
		init();
	}

	public CalendarCustomDayView(Context context) {
		super(context);
		this.mContext = context;
		init();
	}

	public CalendarCustomDayView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		init();
	}

	public CalendarCustomDayView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
		init();
	}

	private void init() {
		DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
		p = new Paint();
		p.setStyle(Paint.Style.FILL);
		p.setColor(Color.BLACK);
		p.setAntiAlias(true);
		p.setTextSize(12);
		p.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
		p.setStrokeWidth(0.1f);
		p.setPathEffect(new DashPathEffect(new float[] { 1, 2 }, 0));

		textp = new Paint();
		textp.setStyle(Paint.Style.FILL);
		textp.setColor(mContext.getResources().getColor(
				R.color.agenda_day_item_text_color));
		textp.setAntiAlias(true);
		textp.setTextSize(11 * metrics.density);
		roundRectP = new Paint();
		roundRectP.setStyle(Paint.Style.FILL);
		roundRectP.setStrokeWidth(5);
		roundRectP.setAntiAlias(false);
		roundRectP.setColor(getResources().getColor(R.color.event_bgcolor));

		defHour = new Paint();

		defHour.setStyle(Paint.Style.FILL);
		defHour.setAntiAlias(false);
		defHour.setColor(mContext.getResources().getColor(R.color.def_hours));
		defHour.setStrokeWidth(5);

		lineHour = new Paint();
		lineHour.setStyle(Paint.Style.FILL);
		lineHour.setColor(mContext.getResources().getColor(R.color.line_hrs));

		textevent = new Paint();
		textevent.setStyle(Paint.Style.FILL);
		textevent.setColor(Color.WHITE);
		textevent.setAntiAlias(true);
		textevent.setTextSize(12 * metrics.density);
		// HOUR_BLOCK_HEIGHT = getResources().getInteger(R.integer.list_height);
		// // (int)
		// Email.convertPixelsToDp(60,context);

		mScreenwidth = metrics.widthPixels;
		mNo_of_events = mDayEventValues.size();
		HOUR_BLOCK_HEIGHT = (int) (60 * metrics.density);
		mYCordinates = new int[25];
		mCalendarRectEventValues = new ArrayList<CalendarEventRect>();
		CalendarLog.d(CalendarConstants.Tag, "Density" + metrics.densityDpi
				+ "float" + metrics.density + "height" + metrics.heightPixels);
		this.setOnTouchListener(this);
		// this.getLocationOnScreen(mLocation);
		// mGetTopYPosition = mLocation[1];
	}

	@Override
	protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
		super.onSizeChanged(xNew, yNew, xOld, yOld);
		mViewWidth = xNew;
		mViewHeight = yNew;
	}

	private void calculateRectValues() {
		try {

			String finalVal = " 12AM; ";
			Rect result = new Rect();
			textevent.getTextBounds(finalVal, 0, finalVal.length(), result);
			mFont_max_width = result.width();
			int eventrightspace = getResources().getInteger(
					R.integer.event_right);

			String hourVal = "AM";
			Rect hourRect = new Rect();
			textp.getTextBounds(hourVal, 0, hourVal.length(), hourRect);
			mHours_font_max_width = hourRect.width();

			mFont_height = result.height();
			mCalendarRectEventValues = new ArrayList<CalendarEventRect>();
			for (int i = 0; i < mDayEventValues.size(); i++) {
				// for checking no of events
				String currStartTime = mDayEventValues.get(i).getAsString(
						CalendarConstants.START_TIME);
				if (currStartTime != "-1" && currStartTime != null) {
					int nextevent = 0;
					if (i < mDayEventValues.size() - 1) {
						String currEndTime = mDayEventValues.get(i)
								.getAsString(CalendarConstants.END_TIME);
						currEndTime = currEndTime.substring(1);
						currStartTime = currStartTime.substring(1);

						String[] currEndTimeArr = currEndTime.split(";");
						String[] currStartTimeArr = currStartTime.split(";");
						int currSTHours = Integer
								.parseInt(currStartTimeArr[0].substring(0,
										currStartTimeArr[0].indexOf(":")));

						int diff = 1;
						for (String curEndTime : currEndTimeArr) {
							int currETHours = Integer.parseInt(curEndTime
									.substring(0, curEndTime.indexOf(":")));
							int timediff = currETHours - currSTHours;
							if (diff < timediff)
								diff = timediff;
						}

						for (int j = 1; j <= diff; j++) {
							String nextStartTime = mDayEventValues.get(i + j)
									.getAsString(CalendarConstants.START_TIME);
							if (nextStartTime != "-1") {

								nextStartTime = nextStartTime.substring(1);
								String[] nextStartTimeArr = nextStartTime
										.split(";");

								int currHours = Integer
										.parseInt(currEndTimeArr[0].substring(
												0,
												currEndTimeArr[0].indexOf(":")));

								int prevHours = Integer
										.parseInt(nextStartTimeArr[0]
												.substring(0,
														nextStartTimeArr[0]
																.indexOf(":")));

								currHours = convertTimetoSeconds(currEndTimeArr[0]);
								prevHours = convertTimetoSeconds(nextStartTimeArr[0]);

								if ((currHours > prevHours)) {
									String nextevents = mDayEventValues
											.get(i + j).get("_id").toString();
									if (nextevents.length() > 0) {
										nextevents = nextevents.substring(1);
										String[] nexteventarr = nextevents
												.split(";");
										nextevent = nexteventarr.length;

									}
								}
							}
						}

					}
					int prevevent = 0;
					if (i > 0) {
						currStartTime = mDayEventValues.get(i)
								.get(CalendarConstants.START_TIME).toString();
						currStartTime = currStartTime.substring(1);

						String[] currStartTimeArr = currStartTime.split(";");
						for (int j = 1; j <= i; j++) {
							if (mDayEventValues.get(i - j).getAsString(
									CalendarConstants.START_TIME) != "-1") {
								String prevEndTime = mDayEventValues
										.get(i - j)
										.getAsString(CalendarConstants.END_TIME);
								prevEndTime = prevEndTime.substring(1);

								String[] prevEndTimeArr = prevEndTime
										.split(";");
								for (String preEndTime : prevEndTimeArr) {
									int currHours = Integer
											.parseInt(currStartTimeArr[0]
													.substring(
															0,
															currStartTimeArr[0]
																	.indexOf(":")));

									int prevHours = Integer.parseInt(preEndTime
											.substring(0,
													preEndTime.indexOf(":")));

									currHours = convertTimetoSeconds(currStartTimeArr[0]);
									prevHours = convertTimetoSeconds(preEndTime);

									if ((currHours < prevHours)) {
										String nextevents = mDayEventValues
												.get(i - j).get("_id")
												.toString();
										if (nextevents.length() > 0) {
											nextevents = nextevents
													.substring(1);
											String[] nexteventarr = nextevents
													.split(";");
											prevevent = nexteventarr.length;

										}
									}
								}
							}

						}
					}
					String events = mDayEventValues.get(i).getAsString(
							CalendarConstants.EVENT);
					String event_ids = mDayEventValues.get(i)
							.get(CalendarConstants._ID).toString();
					String calendarType = mDayEventValues.get(i)
							.get(CalendarConstants.CALENDAR_TYPE_KEY)
							.toString();
					String event_ST = mDayEventValues.get(i).getAsString(
							CalendarConstants.START_TIME);
					String event_ED = mDayEventValues.get(i).getAsString(
							CalendarConstants.END_TIME);
					String eventLocation = mDayEventValues.get(i).getAsString(
							Event.EVENT_LOCATION);
					if (eventLocation == null)
						eventLocation = ";";
					if (events.length() > 0 && event_ids.length() > 0) {
						events = events.substring(1);
						event_ids = event_ids.substring(1);
						event_ST = event_ST.substring(1);
						event_ED = event_ED.substring(1);
						eventLocation = eventLocation.substring(1);
						String[] eventarr = events.split(";");
						String[] eventIDArr = event_ids.split(";");
						String[] eventSTArr = event_ST.split(";");
						String[] eventEDArr = event_ED.split(";");
						String[] eventLocationArr = eventLocation.split(";");
						int startcount = 1;
						int totalevents = (eventIDArr.length + nextevent + prevevent);
						if (totalevents == 0)
							totalevents = 1;
						int templeft = mFont_max_width - 10;
						int tempsplit = mScreenwidth / (totalevents);
						boolean isOnetime = true;
						if (nextevent > 0 && isOnetime) {

							templeft = tempsplit * nextevent;
							startcount = startcount + nextevent;
							isOnetime = false;
						}
						int count = 0;

						for (String eveid : eventIDArr) {

							int rectTop = convertTimetoSeconds(eventSTArr[count]);
							int rectBottom = convertTimetoSeconds(eventEDArr[count]);
							if ((rectBottom - rectTop) < (HOUR_BLOCK_HEIGHT / 3)) {
								rectBottom = rectTop + (HOUR_BLOCK_HEIGHT / 3);
							}
							RectF temp = new RectF(templeft + eventrightspace,
									rectTop, (tempsplit * startcount)
											- eventrightspace, rectBottom - 1);
							templeft = tempsplit * startcount;
							startcount++;
							String eventtitle = "";
							if (eventarr.length > count)
								eventtitle = eventarr[count];

							String eventloc = "";
							if (eventLocationArr.length > count)
								eventloc = eventLocationArr[count];
							ContentValues tContentvalues = new ContentValues();

							// CalendarLog.d(CalendarConstants.Tag,"The eventLocation "+eventLocation);

							tContentvalues.put(CalendarConstants.EVENT_TITLE,
									eventtitle);
							tContentvalues.put(CalendarConstants.EVENTS_ID,
									eveid);
							tContentvalues.put(
									CalendarConstants.EVENT_START_TIME,
									eventSTArr[count]);
							tContentvalues.put(
									CalendarConstants.EVENT_END_TIME,
									eventEDArr[count]);
							tContentvalues.put(
									CalendarConstants.EVENT_START_DATE,
									mDayEventValues.get(i).getAsString(
											CalendarConstants.START_DATE));
							tContentvalues.put(
									CalendarConstants.EVENT_BG_COLOR,
									mDayEventValues.get(i).getAsString(
											CalendarConstants.BG_COLOR));
							tContentvalues.put(Event.EVENT_LOCATION, eventloc);
							tContentvalues.put(
									CalendarConstants.CALENDAR_TYPE_KEY,
									calendarType);
							if (mDayEventValues.get(i).containsKey(
									CalendarConstants.RECURRENCE_CURRENTDATE)) {
								tContentvalues
										.put(CalendarConstants.RECURRENCE_CURRENTDATE,
												mDayEventValues
														.get(i)
														.getAsString(
																CalendarConstants.RECURRENCE_CURRENTDATE));
								CalendarLog
										.d(CalendarConstants.Tag,
												"RECURRENCE_CURRENTDATE"
														+ mDayEventValues
																.get(i)
																.getAsString(
																		CalendarConstants.RECURRENCE_CURRENTDATE));
							}
							mCalendarRectEventValues.add(new CalendarEventRect(
									temp, tContentvalues));
							count++;
						}
					}
					if (nextevent > 0) {
						prevevent = nextevent;
					}
				}

			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public int convertTimetoSeconds(String _startTime) {
		int total = (Integer.parseInt(_startTime.substring(0,
				_startTime.indexOf(":")))
				* HOUR_BLOCK_HEIGHT + mFont_height / 2) - 5;
		int min = Integer.parseInt(_startTime.substring(
				_startTime.indexOf(":") + 1, _startTime.length()));

		if (min > 0) {
			int height = (HOUR_BLOCK_HEIGHT * min) / 60;
			total += (height);
		}
		return total;
	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mParentWidth = MeasureSpec.getSize(widthMeasureSpec);
		setMeasuredDimension(mParentWidth, 24 * HOUR_BLOCK_HEIGHT);
		mScreenwidth = mParentWidth;
		calculateRectValues();
		isRedaw=true;
		invalidate();
	}

	public void draw(Canvas canvas) {

		
//		if (isRedaw) {
			isRedaw=false;
			Calendar todayday = Email.getNewCalendar();
			Calendar currentday = Email.getCurrentDay();
			int todayDate = todayday.get(Calendar.DATE);
			int todayMonth = todayday.get(Calendar.MONTH);
			int todayYear = todayday.get(Calendar.YEAR);
//			int currentDate = currentday.get(Calendar.DATE);
//			int currentMonth = currentday.get(Calendar.MONTH);
//			int currentYear = currentday.get(Calendar.YEAR);
			int prevEventBackground = mContext.getResources().getColor(
					R.color.layoutbg);

			final String currenthrs = todayday.get(Calendar.HOUR_OF_DAY) + ":"
					+ todayday.get(Calendar.MINUTE);
			int compare = CalendarCommonFunction.compareTo(
					currentday.getTime(), CalendarCommonFunction
							.getZeroTimeDate(todayDate, todayMonth, todayYear));
//			CalendarLog.d(CalendarConstants.Tag, "compare" + compare);
//			CalendarLog.d(CalendarConstants.Tag,
//					"currentday" + currentday.getTime());
//			CalendarLog.d(CalendarConstants.Tag,
//					"todayday" + todayday.getTime());

			// if ((todayMonth > currentMonth) && (todayYear >= currentYear)) {
			if (compare == -1) {
				canvas.drawColor(prevEventBackground);
			} else if (compare == 0) {
				canvas.drawColor(mContext.getResources()
						.getColor(R.color.white));
				Paint paint = new Paint();
				paint.setColor(prevEventBackground);
				canvas.drawRect(mFont_max_width - 10, 0, mParentWidth - 1,
						convertTimetoSeconds(currenthrs) + 3, paint);

			} else if (compare == 1) {
				canvas.drawColor(mContext.getResources()
						.getColor(R.color.white));
			} else
				canvas.drawColor(mContext.getResources()
						.getColor(R.color.white));

			int startpos = mFont_max_width - 10;
			int bottompos = (24 * HOUR_BLOCK_HEIGHT + mFont_height / 2) - 6;
			String userSettings = DateFormat.getTimeFormat(mContext).format(
					todayday.getTime());
			boolean is24Hrs = true;
			if (userSettings.indexOf("PM") != -1)
				is24Hrs = false;
			else if (userSettings.indexOf("AM") != -1)
				is24Hrs = false;

			canvas.drawLine(startpos, 0, startpos, bottompos, lineHour);
			for (int i = 0; i < 25; i++) {
				float top = 0;
				float bottom = 0;
				if (i > 0)
					top = ((i - 1) * HOUR_BLOCK_HEIGHT + mFont_height / 2) - 5;
				bottom = (i * HOUR_BLOCK_HEIGHT + mFont_height / 2) - 6;
				// To draw hour rectangle with while background
				canvas.drawRect(0, top, mFont_max_width - 10, bottom + 1,
						defHour);
			}
			// this.getLocationOnScreen(mLocation);

			for (int i = 0; i < 25; i++) {
				// Y Coordinates of hours
				int lineYPosition = (i * HOUR_BLOCK_HEIGHT + mFont_height / 2) - 5;
				int lineYNextPosition = ((i + 1) * HOUR_BLOCK_HEIGHT + mFont_height / 2) - 5;

				String preString = "";
				// Condition for 12/24 hour format
				if (is24Hrs) {
					if (i < 10) {
						preString = "0" + i;
					} else {
						preString = "" + i;
					}
				} else {

					int hours = i;
					if (hours > 12)
						hours = i - 12;
					String spacebefore = "";
					if (hours > 0 && hours < 10)
						spacebefore = " ";
					else
						spacebefore = " ";
					// CalendarLog.e("Calendar Log",
					// "----------"+mGetTopYPosition);
					if (i == 0
							&& mGetTopYPosition <= convertTimetoSeconds("01:00"))
						preString = spacebefore + "12\nAM";
					else if (i == 12
							&& mGetTopYPosition < convertTimetoSeconds("13:00"))
						preString = spacebefore + "12\nPM";
					else {
						if (lineYPosition < mGetTopYPosition
								&& mGetTopYPosition < lineYNextPosition) {

							if (i > 12) {
								preString = spacebefore + hours + "\nPM";
							} else {
								preString = spacebefore + hours + "\nAM";
							}
						} else if (hours == 0) {
							preString = spacebefore + 12;
						} else {
							preString = spacebefore + hours;
						}
					}
				}
				// To draw Hours Text in left side
				drawMultiline(canvas, preString, mFont_max_width
						- (mHours_font_max_width + X_OFFSET + 10),
						(i * HOUR_BLOCK_HEIGHT) + mFont_height + 10, textp);

				mYCordinates[i] = lineYPosition;

				canvas.drawLine(0, lineYPosition, mParentWidth - 1,
						lineYPosition, lineHour);

			}

			// Draw Events for the day
			for (int j = 0; j < mNo_of_events; j++) {
				for (CalendarEventRect tEvent : mCalendarRectEventValues) {
					RectF drawRect = tEvent.getRect();

					String colorBG = tEvent.getContentValues().getAsString(
							CalendarConstants.EVENT_BG_COLOR);
					if (!colorBG.equalsIgnoreCase("null") || colorBG != null)
						roundRectP.setColor(0xff000000 + Integer
								.parseInt(colorBG));
					canvas.drawRoundRect(drawRect, 1, 1, roundRectP);

					CalendarTextRect textRect = new CalendarTextRect(textevent);
					String textToDisplay = "";
					String eventLocation = tEvent.getContentValues()
							.getAsString(Event.EVENT_LOCATION);
					if ((drawRect.height() + 1) == (HOUR_BLOCK_HEIGHT / 3)) {
						if (eventLocation != "") {
							eventLocation = " - " + eventLocation;
						}
						textToDisplay = tEvent.getContentValues().getAsString(
								"eventtitle")
								+ eventLocation;
					} else {
						textToDisplay = tEvent.getContentValues().getAsString(
								"eventtitle")
								+ " \n " + eventLocation;
					}
					textRect.prepare(textToDisplay, (int) drawRect.width()
							- X_OFFSET, (int) drawRect.height() - Y_OFFSET);
					textRect.draw(canvas, (int) drawRect.left + X_OFFSET,
							(int) drawRect.top);

				}

			}
			// Current Time Line
			if (compare == 0) {
				Paint paint = new Paint();
				paint.setColor(Color.BLACK);
				paint.setShadowLayer(1, 0, 0, 0xFF555555);
				paint.setStrokeWidth(2);
				canvas.drawLine(mFont_max_width - 10,
						convertTimetoSeconds(currenthrs), mParentWidth,
						convertTimetoSeconds(currenthrs), paint);
			}
			// For drawing New Event Rectangle.
			if (mNeweventrect != null) {
				roundRectP.setColor(getResources().getColor(
						R.color.event_bgcolor));
				canvas.drawRoundRect(mNeweventrect, 1, 1, roundRectP);
				canvas.drawText("+ New Event", mNeweventrect.left + X_OFFSET,
						mNeweventrect.top + mFont_height + Y_OFFSET, textevent);
			}
			mCanvas=canvas;
//		}
		super.draw(canvas);
	}

	public void drawMultiline(Canvas canvas, String str, int x, int y,
			Paint paint) {
		for (String line : str.split("\n")) {
			canvas.drawText(line, x, y, paint);
			y += -paint.ascent() + paint.descent();
		}
	}

	private View.OnCreateContextMenuListener vC = new View.OnCreateContextMenuListener() {

		@Override
		public void onCreateContextMenu(ContextMenu arg0, View arg1,
				ContextMenuInfo arg2) {
			isLongpresed = true;
			CalendarLog.d(CalendarConstants.Tag, "CalendarCustomDayView-"
					+ "LongPress");
			Calendar cal = Email.getCurrentDay();
			String weekday = Email.getWeekDayOfDate(cal
					.get(Calendar.DATE)
					+ "/"
					+ (cal.get(Calendar.MONTH) + 1)
					+ "/" + cal.get(Calendar.YEAR));
			String HoursContTitle = "";
			if (mHoursTitle < 10)
				HoursContTitle = "0" + mHoursTitle;
			else
				HoursContTitle = String.valueOf(mHoursTitle);
			arg0.setHeaderTitle(HoursContTitle + ":00," + weekday);
			arg0.add(0, 0, 0, "New Event").setOnMenuItemClickListener(
					mMenuItemClickListener);

		}

		private OnMenuItemClickListener mMenuItemClickListener = new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {

				switch (item.getItemId()) {
				case 0:
					Intent eventIntent = new Intent(
							mContext,
							com.cognizant.trumobi.calendar.event.CalendarAddEvent.class);
					eventIntent.putExtra(CalendarConstants.HOURS_KEY,
							mHoursTitle);
					mContext.startActivity(eventIntent);
					return true;

				}

				return false;
			}
		};
	};

	public boolean onTouch(View v, MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();
		setFilterTouchesWhenObscured(true);
//		if (mNeweventrect != null) {
//			if (mNeweventrect.contains(x, y)) {
//				Intent eventIntent = new Intent(
//						mContext,
//						com.cognizant.trumobi.calendar.event.CalendarAddEvent.class);
//				int hrs = y / HOUR_BLOCK_HEIGHT;
//				eventIntent.putExtra("Hours", hrs);
//				mContext.startActivity(eventIntent);
//				mNeweventrect = null;
//				return super.onTouchEvent(event);
//			}
//		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			isLongpresed = false;
//			CalendarLog.d(CalendarConstants.Tag, "CalendarCustomDayView-"
//					+ "Down");
			mHoursTitle = y / HOUR_BLOCK_HEIGHT;
			if (!isLongpresed) {
				if (mNeweventrect != null) {
					CalendarLog.d(CalendarConstants.Tag, "CalendarCustomDayView-"
							+ "UP not null" + isLongpresed);
					if (mNeweventrect.contains(x, y)) {
						CalendarLog.d(CalendarConstants.Tag,
								"CalendarCustomDayView-" + "New Event");
						Intent eventIntent = new Intent(mContext,
								CalendarAddEvent.class);
						int hrs = y / HOUR_BLOCK_HEIGHT;
						eventIntent.putExtra("Hours", hrs);
						mContext.startActivity(eventIntent);
						mNeweventrect = null;
						isLongpresed=true;
						return super.onTouchEvent(event);
					}
				}
			}
			// _handler.postDelayed(_longPressed, LONG_PRESS_TIME);
		case MotionEvent.ACTION_MOVE:
			mNeweventrect = null;
			// _handler.removeCallbacks(_longPressed);
			break;
		case MotionEvent.ACTION_UP:
//			CalendarLog.d(CalendarConstants.Tag, "CalendarCustomDayView-"
//					+ "UP");
			if (!isLongpresed) {
				for (CalendarEventRect tEvent : mCalendarRectEventValues) {
					RectF drawRect = tEvent.getRect();
					if (drawRect.contains(x, y)) {
						ContentValues values = tEvent.getContentValues();
						if (values.containsKey(CalendarConstants.EVENTS_ID)) {
							String event_id = tEvent.getContentValues()
									.getAsString(CalendarConstants.EVENTS_ID);

							String calendarType = tEvent.getContentValues()
									.get(CalendarConstants.CALENDAR_TYPE_KEY)
									.toString();

							ContentValues contentValues = null;
							if (calendarType
									.equals(CalendarConstants.CALENDAR_TYPE_CORPORATE)) {
								contentValues = CalendarDatabaseHelper
										.getEventContentValues(CalendarDatabaseHelper
												.getEventDetails(Integer
														.parseInt(event_id)));
								contentValues
										.put(CalendarConstants.CALENDAR_TYPE_KEY,
												CalendarConstants.CALENDAR_TYPE_CORPORATE);
								if (tEvent
										.getContentValues()
										.containsKey(
												CalendarConstants.RECURRENCE_CURRENTDATE)) {
									contentValues
											.put(CalendarConstants.RECURRENCE_CURRENTDATE,
													tEvent.getContentValues()
															.getAsString(
																	CalendarConstants.RECURRENCE_CURRENTDATE));
									CalendarLog
											.d(CalendarConstants.Tag,
													"RECURRENCE_CURRENTDATE"
															+ tEvent.getContentValues()
																	.getAsString(
																			CalendarConstants.RECURRENCE_CURRENTDATE));
								}
							} else if (calendarType
									.equals(CalendarConstants.CALENDAR_TYPE_PERSONAL)) {

								contentValues = CalendarDBHelperClassPreICS
										.getPersonalEventContentValues(CalendarDBHelperClassPreICS
												.getEventDetails(Integer
														.parseInt(event_id)));
								contentValues
										.put(CalendarConstants.CALENDAR_TYPE_KEY,
												CalendarConstants.CALENDAR_TYPE_PERSONAL);

							} else {

							}
							Bundle contentValue = new Bundle();
							contentValue.putParcelable(
									CalendarConstants.EVENT_CONTENT_VALUES,
									contentValues);
							if (!contentValues
									.getAsString(
											CalendarConstants.CALENDAR_TYPE_KEY)
									.equals(CalendarConstants.CALENDAR_TYPE_PERSONAL)) {
								Intent eventIntent = new Intent(
										mContext,
										com.cognizant.trumobi.calendar.event.CalendarEditEvent.class);
								eventIntent.putExtras(contentValue);
								eventIntent
										.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								mContext.startActivity(eventIntent);
							}
						}
						// break;
						return super.onTouchEvent(event);

					}
				}

				for (int i = 0; i < 24; i++) {

					if (mYCordinates[i] < y && y < mYCordinates[i + 1]) {
						mHoursTitle = mYCordinates[i] / HOUR_BLOCK_HEIGHT;

						mNeweventrect = new RectF(mFont_max_width - 9,
								mYCordinates[i] + 1, mScreenwidth - 1,
								mYCordinates[i + 1] - 1);
						isRedaw=true;
						invalidate();
						return super.onTouchEvent(event);
						// break;
					}
				}

			}

			break;
		default:

			return super.onTouchEvent(event);
		}

		return super.onTouchEvent(event);
	}

}
