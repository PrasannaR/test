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

import com.TruBoxSDK.SharedPreferences;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.calendar.modal.Event;
import com.cognizant.trumobi.calendar.provider.CalendarDBHelperClassPreICS;
import com.cognizant.trumobi.calendar.provider.CalendarDatabaseHelper;
import com.cognizant.trumobi.calendar.util.CalendarConstants;
import com.cognizant.trumobi.calendar.util.CalendarTextRect;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.log.CalendarLog;

public class CalendarCustomWeekView extends View implements OnTouchListener {

	public int getTopYPosition = 0, viewWidth = 0, viewHeight = 0;
	public static int LONG_PRESS_TIME = 500;

	public Paint mPaint, mTextPaint, mRoundRectP, mDefHour, mLineHour,
			mTextevent, mTextNewevent;
	public int parentWidth = 0, X_OFFSET = 5, Y_OFFSET = 5,
			HOUR_BLOCK_HEIGHT = 60, font_max_width = 0,
			hours_font_max_width = 0, font_height = 0, screenwidth = 0,
			HoursTitle = 0, dayCount = 0;
	private Context mContext;
	private int[] YCordinates;
	private int[] XCordinates;
	private ArrayList<ArrayList<ContentValues>> weekEventValues;
	private ArrayList<CalendarEventRect> eventValues;
	private RectF neweventrect = null;
	private Calendar todayday;
	private String presentDay;
	boolean isLongpresed = false;
	private SharedPreferences mSecurePrefs;

	public CalendarCustomWeekView(Context context,
			ArrayList<ArrayList<ContentValues>> weekEventValues,

			String presentDay) {
		super(context);
		this.mContext = context;
		this.weekEventValues = weekEventValues;
		this.presentDay = presentDay;
		mSecurePrefs = new SharedPreferences(Email.getAppContext());
		// PreferenceManager.getDefaultSharedPreferences(context);//

		this.setOnCreateContextMenuListener(vC);
		HOUR_BLOCK_HEIGHT = getResources().getInteger(R.integer.list_height);
		YCordinates = new int[25];
		XCordinates = new int[8];
		eventValues = new ArrayList<CalendarEventRect>();
		init();
		invalidate();
	}

	public CalendarCustomWeekView(Context context) {
		super(context);
		this.mContext = context;
		init();
	}

	public CalendarCustomWeekView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		init();
	}

	public CalendarCustomWeekView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
		init();
	}

	private void init() {
		DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(Color.BLACK);
		mPaint.setAntiAlias(true);
		mPaint.setTextSize(12);
		mPaint.setTypeface(Typeface
				.create(Typeface.SANS_SERIF, Typeface.NORMAL));
		// p.setColor(Color.LTGRAY);
		mPaint.setStrokeWidth(0.1f);
		mPaint.setPathEffect(new DashPathEffect(new float[] { 1, 2 }, 0));

		mTextPaint = new Paint();
		mTextPaint.setStyle(Paint.Style.FILL);
		mTextPaint.setColor(mContext.getResources().getColor(
				R.color.agenda_day_item_text_color));
		mTextPaint.setAntiAlias(true);
		mTextPaint.setTextSize(11 * metrics.density);

		mRoundRectP = new Paint();
		mRoundRectP.setStyle(Paint.Style.FILL);
		mRoundRectP.setStrokeWidth(5);
		mRoundRectP.setAntiAlias(false);
		mRoundRectP.setColor(getResources().getColor(R.color.event_bgcolor));

		mDefHour = new Paint();
		mDefHour.setStyle(Paint.Style.FILL);
		mDefHour.setAntiAlias(false);
		mDefHour.setColor(mContext.getResources().getColor(R.color.def_hours));
		mDefHour.setStrokeWidth(5);

		mLineHour = new Paint();
		mLineHour.setStyle(Paint.Style.FILL);
		mLineHour.setColor(mContext.getResources().getColor(R.color.line_hrs));

		mTextevent = new Paint();
		mTextevent.setStyle(Paint.Style.FILL);
		mTextevent.setColor(Color.WHITE);
		mTextevent.setAntiAlias(true);
		float weekfontsize = 12 * metrics.density; // getResources().getInteger(R.integer.event_font)
													// - 2;
		mTextevent.setTextSize(12 * metrics.density);

		mTextNewevent = new Paint();
		mTextNewevent.setStyle(Paint.Style.FILL);
		mTextNewevent.setColor(Color.WHITE);
		mTextNewevent.setAntiAlias(true);
		mTextNewevent.setTextSize(weekfontsize + 10);

		HOUR_BLOCK_HEIGHT = getResources().getInteger(R.integer.list_height);
		HOUR_BLOCK_HEIGHT = (int) (60 * metrics.density);
		// mScreenwidth = metrics.widthPixels;
		this.setOnTouchListener(this);
		eventValues = new ArrayList<CalendarEventRect>();
		invalidate();
	}

	@Override
	protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
		super.onSizeChanged(xNew, yNew, xOld, yOld);

		viewWidth = xNew;
		viewHeight = yNew;
	}

	private void calculateRectValues() {
		try {
			// Calculating the Rects to draw in the paint this includes x,y
			// points
			// starting of the rect and width and height of the rectangle.
			// int font_max_width = calculate the width needed to draw the hours
			// from 0 to 24 hours;
			String finalVal = " 12AM; ";
			Rect result = new Rect();
			mTextPaint.getTextBounds(finalVal, 0, finalVal.length(), result);
			font_max_width = result.width();

			String hourVal = "AM";
			Rect hourRect = new Rect();
			mTextPaint.getTextBounds(hourVal, 0, hourVal.length(), hourRect);
			hours_font_max_width = hourRect.width();

			font_height = result.height();
			eventValues = new ArrayList<CalendarEventRect>();
			// drawArr=new ArrayList<RectF>();
			// no_of_events=0;
			for (int week = 0; week < weekEventValues.size(); week++) {
				// Log.e("week","week"+week);

				for (int i = 0; i < weekEventValues.get(week).size(); i++) {
					// for checking no of events
					String currStartTime = weekEventValues.get(week).get(i)
							.getAsString(CalendarConstants.START_TIME);
					if (currStartTime != "-1" && currStartTime != null) {
						int nextevent = 0;
						if (i < weekEventValues.get(week).size() - 1) {

							String currEndTime = weekEventValues.get(week)
									.get(i)
									.getAsString(CalendarConstants.END_TIME);
							currEndTime = currEndTime.substring(1);
							currStartTime = currStartTime.substring(1);

							String[] currEndTimeArr = currEndTime.split(";");
							String[] currStartTimeArr = currStartTime
									.split(";");
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
								String nextStartTime = weekEventValues
										.get(week)
										.get(i + j)
										.getAsString(
												CalendarConstants.START_TIME);
								if (nextStartTime != "-1") {

									nextStartTime = nextStartTime.substring(1);
									String[] nextStartTimeArr = nextStartTime
											.split(";");
									int currHours = Integer
											.parseInt(currEndTime.substring(0,
													currEndTimeArr[0]
															.indexOf(":")));

									int prevHours = Integer
											.parseInt(nextStartTimeArr[0]
													.substring(
															0,
															nextStartTimeArr[0]
																	.indexOf(":")));

									currHours = convertTimetoSeconds(currEndTimeArr[0]);
									prevHours = convertTimetoSeconds(nextStartTimeArr[0]);
									if ((currHours > prevHours)) {
										String nextevents = weekEventValues
												.get(week).get(i + j)
												.get(CalendarConstants._ID)
												.toString();
										if (nextevents.length() > 0) {
											nextevents = nextevents
													.substring(1);
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
							currStartTime = weekEventValues.get(week).get(i)
									.get(CalendarConstants.START_TIME)
									.toString();
							currStartTime = currStartTime.substring(1);
							String[] currStartTimeArr = currStartTime
									.split(";");
							for (int j = 1; j <= i; j++) {
								if (weekEventValues
										.get(week)
										.get(i - j)
										.getAsString(
												CalendarConstants.START_TIME) != "-1") {
									String prevEndTime = weekEventValues
											.get(week)
											.get(i - j)
											.getAsString(
													CalendarConstants.END_TIME);
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

										int prevHours = Integer
												.parseInt(preEndTime.substring(
														0,
														preEndTime.indexOf(":")));

										currHours = convertTimetoSeconds(currStartTimeArr[0]);
										prevHours = convertTimetoSeconds(preEndTime);
										if ((currHours < prevHours)) {
											String nextevents = weekEventValues
													.get(week).get(i - j)
													.get(CalendarConstants._ID)
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
						String events = weekEventValues.get(week).get(i)
								.get(CalendarConstants.EVENT).toString();
						String event_ids = weekEventValues.get(week).get(i)
								.get(CalendarConstants._ID).toString();
						String event_ST = weekEventValues.get(week).get(i)
								.get(CalendarConstants.START_TIME).toString();
						String event_ED = weekEventValues.get(week).get(i)
								.get(CalendarConstants.END_TIME).toString();
						String calendarType = weekEventValues.get(week).get(i)
								.get(CalendarConstants.CALENDAR_TYPE_KEY)
								.toString();
						String eventLocation = weekEventValues.get(week).get(i)
								.getAsString(Event.EVENT_LOCATION);
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
							String[] eventLocationArr = eventLocation
									.split(";");
							int startcount = 1;
							int totalevents = (eventIDArr.length + nextevent + prevevent);
							if (totalevents == 0)
								totalevents = 1;
							int templeft = 0;
							int startpos = font_max_width - 10;
							int twidth = (parentWidth - startpos) / 7;
							int tempsplit = (twidth) / (totalevents);
							templeft = startpos + (twidth * week);
							boolean isOnetime = true;
							if (nextevent > 0 && isOnetime) {

								templeft = templeft + (tempsplit * nextevent);
								startcount = startcount + nextevent;
								isOnetime = false;
							}
							int count = 0;

							for (String eveID : eventIDArr) {
								int left = templeft + 1;
								int right = templeft + (tempsplit) - 1;

								int rectTop = convertTimetoSeconds(eventSTArr[count]);
								int rectBottom = convertTimetoSeconds(eventEDArr[count]);
								if ((rectBottom - rectTop) < (HOUR_BLOCK_HEIGHT / 3)) {
									rectBottom = rectTop
											+ (HOUR_BLOCK_HEIGHT / 3);
								}
								RectF temp = new RectF(left, rectTop, right,
										rectBottom);
								templeft = right + 1;
								startcount++;
								String eventtitle = "";
								if (eventarr.length > count)
									eventtitle = eventarr[count];
								String eventloc = "";
								if (eventLocationArr.length > count)
									eventloc = eventLocationArr[count];

								ContentValues tContentvalues = new ContentValues();

								tContentvalues.put(Event.EVENT_LOCATION,
										eventloc);
								tContentvalues.put(
										CalendarConstants.EVENT_TITLE,
										eventtitle);
								tContentvalues.put(CalendarConstants.EVENTS_ID,
										eveID);
								tContentvalues.put(
										CalendarConstants.EVENT_START_TIME,
										eventSTArr[count]);
								tContentvalues.put(
										CalendarConstants.EVENT_END_TIME,
										eventEDArr[count]);
								tContentvalues.put(
										CalendarConstants.CALENDAR_TYPE_KEY,
										calendarType);
								eventValues.add(new CalendarEventRect(temp,
										tContentvalues));
								count++;
							}
						}
						if (nextevent > 0) {
							prevevent = nextevent;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			CalendarLog.d(CalendarConstants.Tag, "CalendarCustomWeekView "
					+ "calculateRectValues " + e.toString());
		}
	}

	public int convertTimetoSeconds(String _startTime) {
		int total = (Integer.parseInt(_startTime.substring(0,
				_startTime.indexOf(":")))
				* HOUR_BLOCK_HEIGHT + font_height / 2) - 5;
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
		parentWidth = MeasureSpec.getSize(widthMeasureSpec);

		setMeasuredDimension(parentWidth, 24 * HOUR_BLOCK_HEIGHT);
		screenwidth = parentWidth;
		calculateRectValues();
		invalidate();
	}

	public void draw(Canvas canvas) {
		super.draw(canvas);

		todayday = Email.getNewCalendar();

		Calendar firstCal = (Calendar) todayday.clone();
		String WeekStartsOn = mSecurePrefs.getString(
				mContext.getString(R.string.key_list_preference), "");
		if (WeekStartsOn.equalsIgnoreCase("2")) {
			firstCal.setFirstDayOfWeek(Calendar.SATURDAY);
			todayday.setFirstDayOfWeek(Calendar.SATURDAY);
		} else if (WeekStartsOn.equalsIgnoreCase("3")) {
			firstCal.setFirstDayOfWeek(Calendar.SUNDAY);
			todayday.setFirstDayOfWeek(Calendar.SUNDAY);
		} else if (WeekStartsOn.equalsIgnoreCase("4")) {
			firstCal.setFirstDayOfWeek(Calendar.MONDAY);
			todayday.setFirstDayOfWeek(Calendar.MONDAY);
		}

		int firstDayOfWeek = todayday.getFirstDayOfWeek();
		int dayOfWeek = todayday.get(Calendar.DAY_OF_WEEK);
		int currday = dayOfWeek - firstDayOfWeek;
		if (firstDayOfWeek == 7)
			currday = dayOfWeek;

		firstCal.add(Calendar.DAY_OF_WEEK, firstCal.getFirstDayOfWeek()
				- firstCal.get(Calendar.DAY_OF_WEEK));

		Calendar lastCal = (Calendar) firstCal.clone();
		lastCal.add(Calendar.DAY_OF_YEAR, 6);
		Calendar currentday = Email.getCurrentDay(presentDay);
		if ((todayday.get(Calendar.MONTH) == currentday.get(Calendar.MONTH))
				&& (todayday.get(Calendar.YEAR) == currentday
						.get(Calendar.YEAR))) {
			if (firstCal.get(Calendar.DATE) <= currentday.get(Calendar.DATE)
					&& currentday.get(Calendar.DATE) <= lastCal
							.get(Calendar.DATE)) {
				todayday.set(Calendar.DATE, currentday.get(Calendar.DATE));
			}
		}

		String currenthrs = todayday.get(Calendar.HOUR_OF_DAY) + ":"
				+ todayday.get(Calendar.MINUTE);

		int startpos = font_max_width - 10;
		int twidth = (parentWidth - startpos) / 7;
		int bottompos = (24 * HOUR_BLOCK_HEIGHT + font_height / 2) - 6;
		if ((todayday.get(Calendar.MONTH) > currentday.get(Calendar.MONTH))
				&& (todayday.get(Calendar.YEAR) >= currentday
						.get(Calendar.YEAR))) {
			canvas.drawColor(mContext.getResources().getColor(R.color.layoutbg));

		} else if ((todayday.get(Calendar.DATE) == currentday
				.get(Calendar.DATE))
				&& (todayday.get(Calendar.MONTH) == currentday
						.get(Calendar.MONTH))
				&& (todayday.get(Calendar.YEAR) == currentday
						.get(Calendar.YEAR))) {
			canvas.drawColor(mContext.getResources().getColor(R.color.white));
			Paint paint = new Paint();
			paint.setColor(mContext.getResources().getColor(R.color.layoutbg));
			canvas.drawRect(startpos, 0, startpos + (twidth * (currday)),
					bottompos, paint);
			canvas.drawRect(startpos + (twidth * (currday)), 0, startpos
					+ (twidth * (currday + 1)),
					convertTimetoSeconds(currenthrs), paint);

		} else if ((todayday.get(Calendar.MONTH) >= currentday
				.get(Calendar.MONTH))
				&& (todayday.get(Calendar.YEAR) >= currentday
						.get(Calendar.YEAR))) {
			if (todayday.get(Calendar.DATE) > currentday.get(Calendar.DATE))
				canvas.drawColor(mContext.getResources().getColor(
						R.color.layoutbg));
			else
				canvas.drawColor(mContext.getResources()
						.getColor(R.color.white));
		} else {
			canvas.drawColor(mContext.getResources().getColor(R.color.white));
		}

		canvas.drawLine(startpos, 0, startpos, bottompos, mLineHour);
		XCordinates[0] = font_max_width - 10;
		for (int i = 1; i < 7; i++) {

			int weeksplit = twidth * (i);
			int bottom = (24 * HOUR_BLOCK_HEIGHT + font_height / 2) - 6;
			XCordinates[i] = weeksplit + startpos;
			canvas.drawLine(weeksplit + startpos, 0, weeksplit + startpos,
					bottom, mLineHour);
		}
		for (int i = 0; i < 25; i++) {
			float top = 0;
			float bottom = 0;
			if (i > 0)
				top = ((i - 1) * HOUR_BLOCK_HEIGHT + font_height / 2) - 5;
			bottom = (i * HOUR_BLOCK_HEIGHT + font_height / 2) - 6;
			canvas.drawRect(0, top, font_max_width - 10, bottom, mDefHour);
		}
		String userSettings = DateFormat.getTimeFormat(mContext).format(
				todayday.getTime());
		boolean is24Hrs = true;
		if (userSettings.indexOf("PM") != -1)
			is24Hrs = false;
		else if (userSettings.indexOf("AM") != -1)
			is24Hrs = false;
		for (int i = 0; i < 25; i++) {
			// Y Coordinates of hours
			int lineYPosition = (i * HOUR_BLOCK_HEIGHT + font_height / 2) - 5;
			int lineYNextPosition = ((i + 1) * HOUR_BLOCK_HEIGHT + font_height / 2) - 5;
			String preString = "";
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
					spacebefore = "  ";
				else
					spacebefore = "  ";
				if (i == 0 && getTopYPosition <= convertTimetoSeconds("01:00"))
					preString = spacebefore + "12\nAM";
				else if (i == 12
						&& getTopYPosition < convertTimetoSeconds("13:00"))
					preString = spacebefore + "12\nPM";
				else {
					if (lineYPosition < getTopYPosition
							&& getTopYPosition < lineYNextPosition) {

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
			drawMultiline(canvas, preString, font_max_width
					- (hours_font_max_width + X_OFFSET + 10),
					(i * HOUR_BLOCK_HEIGHT) + font_height + 10, mTextPaint);

			YCordinates[i] = lineYPosition;

			canvas.drawLine(0, lineYPosition, parentWidth - 4, lineYPosition,
					mLineHour);

		}

		for (CalendarEventRect tEvent : eventValues) {
			RectF drawRect = tEvent.getRect();
			canvas.drawRoundRect(drawRect, 3, 3, mRoundRectP);

			CalendarTextRect textRect = new CalendarTextRect(mTextevent);
			String textToDisplay = "";
			if ((drawRect.height() + 1) == (HOUR_BLOCK_HEIGHT / 3)) {
				textToDisplay = tEvent.getContentValues().getAsString(
						"eventtitle")
						+ " - "
						+ tEvent.getContentValues().getAsString(
								Event.EVENT_LOCATION);
			} else {
				textToDisplay = tEvent.getContentValues().getAsString(
						"eventtitle")
						+ " \n "
						+ tEvent.getContentValues().getAsString(
								Event.EVENT_LOCATION);
			}
			textRect.prepare(textToDisplay, (int) drawRect.width() - X_OFFSET,
					(int) drawRect.height() - Y_OFFSET);
			textRect.draw(canvas, (int) drawRect.left + 3,
					(int) drawRect.top + 3);
		}
		if ((todayday.get(Calendar.DATE) == currentday.get(Calendar.DATE))
				&& (todayday.get(Calendar.MONTH) == currentday
						.get(Calendar.MONTH))
				&& (todayday.get(Calendar.YEAR) == currentday
						.get(Calendar.YEAR))) {
			Paint paint = new Paint();
			paint.setColor(Color.BLACK);
			paint.setShadowLayer(1, 0, 0, 0xFF555555);
			paint.setStrokeWidth(2);

			canvas.drawLine(startpos + (twidth * (currday)),
					convertTimetoSeconds(currenthrs), startpos
							+ (twidth * (currday + 1)),
					convertTimetoSeconds(currenthrs), paint);
		}
		if (neweventrect != null) {
			canvas.drawRoundRect(neweventrect, 1, 1, mRoundRectP);
			int xPos = (int) (neweventrect.left + (int) (neweventrect.width() / 2));
			int yPos = (int) (neweventrect.top)
					+ (int) ((neweventrect.height() / 2) - ((mTextNewevent
							.descent() + mTextNewevent.ascent()) / 2));
			canvas.drawText("+", xPos - 5, yPos - 2, mTextNewevent);
		}

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
			isLongpresed=true;
			Calendar cal = Email.getCurrentDay();
			int daycou = dayCount - cal.get(Calendar.DAY_OF_WEEK);
			daycou++;
			String weekday = Email.getWeekDayOfDate((cal
					.get(Calendar.DATE) + daycou)
					+ "/"
					+ (cal.get(Calendar.MONTH) + 1)
					+ "/"
					+ cal.get(Calendar.YEAR));

			String HoursContTitle = "";
			if (HoursTitle < 10)
				HoursContTitle = "0" + HoursTitle;
			else
				HoursContTitle = String.valueOf(HoursTitle);
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
							HoursTitle);
					eventIntent.putExtra(CalendarConstants.DAYS_NEED_TO_ADD,
							dayCount);
					mContext.startActivity(eventIntent);
					invalidate();
					return true;

				}

				return false;
			}
		};
	};

//	final Handler _handler = new Handler();
//	Runnable _longPressed = new Runnable() {
//		public void run() {
//			try {
//				Looper.prepare();
//				neweventrect = null;
//				isLongpresed = true;
//				showContextMenu();
//				invalidate();
//
//			} catch (Exception Ex) {
//
//			}
//		}
//	};

	public boolean onTouch(View v, MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();
		setFilterTouchesWhenObscured(true);
		// boolean isEventtouch = false;

//		if (neweventrect != null) {
//			if (neweventrect.contains(x, y)) {
//				Intent eventIntent = new Intent(
//						mContext,
//						com.cognizant.trumobi.calendar.event.CalendarAddEvent.class);
//				int hrs = y / HOUR_BLOCK_HEIGHT;
//
//				eventIntent.putExtra(CalendarConstants.HOURS_KEY, hrs);
//				eventIntent.putExtra(CalendarConstants.DAYS_NEED_TO_ADD,
//						dayCount);
//				mContext.startActivity(eventIntent);
//				invalidate();
//				neweventrect = null;
//				return super.onTouchEvent(event);
//			}
//		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			isLongpresed = false;
			HoursTitle = y / HOUR_BLOCK_HEIGHT;
			for (int j = 0; j < 7; j++) {
				if (XCordinates[j] < x && x < XCordinates[j + 1]) {
					dayCount = j;
					break;
				}
			}
			if (!isLongpresed) {
				if (neweventrect != null) {
					if (neweventrect.contains(x, y)) {
						Intent eventIntent = new Intent(
								mContext,
								com.cognizant.trumobi.calendar.event.CalendarAddEvent.class);
						int hrs = y / HOUR_BLOCK_HEIGHT;

						eventIntent.putExtra(CalendarConstants.HOURS_KEY, hrs);
						eventIntent.putExtra(CalendarConstants.DAYS_NEED_TO_ADD,
								dayCount);
						mContext.startActivity(eventIntent);
//						isReDraw = true;
						invalidate();
						isLongpresed=true;
						neweventrect = null;
						return super.onTouchEvent(event);
					}
				}
			}
			// _handler.postDelayed(_longPressed, LONG_PRESS_TIME);
			// return true;
		case MotionEvent.ACTION_MOVE:
			neweventrect = null;
			// _handler.removeCallbacks(_longPressed);
			break;
		case MotionEvent.ACTION_UP:
			// _handler.removeCallbacks(_longPressed);
			if (!isLongpresed) {
				for (CalendarEventRect tEvent : eventValues) {
					RectF drawRect = tEvent.getRect();
					if (drawRect.contains(x, y)) {
						if (event.getAction() == MotionEvent.ACTION_UP) {
							ContentValues values = tEvent.getContentValues();
							if (values.containsKey(CalendarConstants.EVENTS_ID)) {
								String event_id = tEvent.getContentValues()
										.getAsString(
												CalendarConstants.EVENTS_ID);

								String calendarType = tEvent
										.getContentValues()
										.get(CalendarConstants.CALENDAR_TYPE_KEY)
										.toString();
								ContentValues contentValues = null;
								if (calendarType
										.equals(CalendarConstants.CALENDAR_TYPE_CORPORATE)) {
									contentValues = CalendarDatabaseHelper
											.getEventContentValues(CalendarDatabaseHelper.getEventDetails(Integer
													.parseInt(event_id)));
									contentValues
											.put(CalendarConstants.CALENDAR_TYPE_KEY,
													CalendarConstants.CALENDAR_TYPE_CORPORATE);
								} else if (calendarType
										.equals(CalendarConstants.CALENDAR_TYPE_PERSONAL)) {

									contentValues = CalendarDBHelperClassPreICS
											.getPersonalEventContentValues(CalendarDBHelperClassPreICS.getEventDetails(Integer
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
									mContext.startActivity(eventIntent);
								}
							}
							return super.onTouchEvent(event);
						}
					}
				}

				for (int i = 0; i < 24; i++) {

					if (YCordinates[i] < y && y < YCordinates[i + 1]) {
						for (int j = 0; j < 7; j++) {
							if (XCordinates[j] < x && x < XCordinates[j + 1]) {
								dayCount = j;
								HoursTitle = YCordinates[i] / HOUR_BLOCK_HEIGHT;

								neweventrect = new RectF(XCordinates[j] - 1,
										YCordinates[i] + 1,
										XCordinates[j + 1] - 1,
										YCordinates[i + 1] - 1);
								invalidate();
								return super.onTouchEvent(event);
								//break;
							}
						}
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
