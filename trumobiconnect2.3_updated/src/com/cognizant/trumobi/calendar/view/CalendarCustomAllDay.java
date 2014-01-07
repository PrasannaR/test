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
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.TruBoxSDK.SharedPreferences;
import com.cognizant.trumobi.R;

import com.cognizant.trumobi.calendar.provider.CalendarDBHelperClassPreICS;
import com.cognizant.trumobi.calendar.provider.CalendarDatabaseHelper;
import com.cognizant.trumobi.calendar.util.CalendarCommonFunction;
import com.cognizant.trumobi.calendar.util.CalendarConstants;
import com.cognizant.trumobi.calendar.util.CalendarTextRect;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.log.CalendarLog;

public class CalendarCustomAllDay extends View implements OnTouchListener {

	private Paint mPaint, mTextPaint, mRoundRectP;
	private Paint mDefHour, mLineHour, mTextevent, mTextNewevent;
	private int mParentWidth = 0, X_OFFSET = 5, Y_OFFSET = 5,
			HOUR_BLOCK_HEIGHT = 0;
	private int mFont_max_width = 0, mFont_height = 0, mScreenwidth = 0;
	public int mViewWidth = 0, mViewHeight = 0;
	private int[] YCordinates;
	private int[] XCordinates;
	private Context mContext;
	private ArrayList<ArrayList<ContentValues>> mWeekEventValues;
	private ArrayList<CalendarEventRect> mEventValues;
	public static int LONG_PRESS_TIME = 500;
	private Calendar mTodayday;
	private SharedPreferences mPrefs;

	public CalendarCustomAllDay(Context context,
			ArrayList<ArrayList<ContentValues>> weekEventValues,
			String presentDay, int totalHeight) {
		super(context);		
		this.mContext = context;
		this.mWeekEventValues = weekEventValues;		
		mPrefs = new SharedPreferences(Email.getAppContext());//PreferenceManager.getDefaultSharedPreferences(context);		
		HOUR_BLOCK_HEIGHT = totalHeight;
		YCordinates = new int[25];
		XCordinates = new int[8];
		mEventValues = new ArrayList<CalendarEventRect>();
		init();
		invalidate();
	}

	public CalendarCustomAllDay(Context context) {
		super(context);

	}

	public CalendarCustomAllDay(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public CalendarCustomAllDay(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);

	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mParentWidth = MeasureSpec.getSize(widthMeasureSpec);
		setMeasuredDimension(mParentWidth, HOUR_BLOCK_HEIGHT);
		mScreenwidth = mParentWidth;
		calculateRectValues();
		invalidate();
	}

	private void init() {
		DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
		mPaint = new Paint();
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(Color.BLACK);
		mPaint.setAntiAlias(true);
		mPaint.setTextSize(12);
		mPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
		mPaint.setStrokeWidth(0.1f);
		mPaint.setPathEffect(new DashPathEffect(new float[] { 1, 2 }, 0));

		mTextPaint = new Paint();
		mTextPaint.setStyle(Paint.Style.FILL);
		mTextPaint.setColor(mContext.getResources().getColor(R.color.agenda_day_item_text_color));
		mTextPaint.setAntiAlias(true);
		mTextPaint.setTextSize(11*metrics.density);

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
		float weekfontsize = 12*metrics.density;
		mTextevent.setTextSize(12*metrics.density);

		mTextNewevent = new Paint();
		mTextNewevent.setStyle(Paint.Style.FILL);
		mTextNewevent.setColor(Color.WHITE);
		mTextNewevent.setAntiAlias(true);
		mTextNewevent.setTextSize(weekfontsize + 10);
		
		mScreenwidth = metrics.widthPixels;
		this.setOnTouchListener(this);
		mEventValues = new ArrayList<CalendarEventRect>();
		calculateRectValues();
		invalidate();
	}

	@Override
	protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
		super.onSizeChanged(xNew, yNew, xOld, yOld);
		mViewWidth = xNew;
		mViewHeight = yNew;
	}

	private void calculateRectValues() {
		String finalVal = " 12AM; ";
		Rect result = new Rect();
		mTextPaint.getTextBounds(finalVal, 0, finalVal.length(), result);
		mFont_max_width = result.width();
		mFont_height = result.height();
		mEventValues = new ArrayList<CalendarEventRect>();
		for (int week = 0; week < 7; week++) {
			CalendarLog.d(CalendarConstants.Tag, "week"+ week);
			for (int i = 0; i < mWeekEventValues.get(week).size(); i++) {
				String eventtitle = mWeekEventValues.get(week).get(i)
						.get(CalendarConstants.EVENT).toString();
				String start_date = mWeekEventValues.get(week).get(i)
						.get(CalendarConstants.START_DATE).toString();
				String end_date = mWeekEventValues.get(week).get(i)
						.get(CalendarConstants.END_DATE).toString();
				int diff = DateDifference(start_date, end_date);
				CalendarLog.d(CalendarConstants.Tag, "diff"
						+ diff);
				int nextevent = 0;
				/*To check next day events*/
				if (week < 6) {
					for(int diffcount =0;diffcount<diff;diffcount++){
						if(week+diffcount+1 < 7)
					if (mWeekEventValues.get(week+diffcount+1).size() > 0) {
						Calendar todayEnd = CalendarCommonFunction
								.convertDBDateToCalendar(end_date);
						String next_start_date = mWeekEventValues.get(week+diffcount+1)
								.get(0).get(CalendarConstants.START_DATE).toString();
						Calendar nextStart = CalendarCommonFunction
								.convertDBDateToCalendar(next_start_date);
						CalendarLog.d(CalendarConstants.Tag, "end_date "
								+ end_date);
						CalendarLog.d(CalendarConstants.Tag, "next_start_date "
								+ next_start_date);
						CalendarLog.d(CalendarConstants.Tag, "end_datet "
								+ todayEnd.getTime().getTime());
						CalendarLog.d(CalendarConstants.Tag,
								"next_start_datet "
										+ nextStart.getTime().getTime());
						if (todayEnd.getTime().getTime() >= nextStart.getTime()
								.getTime())
							nextevent = mWeekEventValues.get(week+diffcount+1).size();
					}
					}
				}
				int prevevent = 0;
				/*To check previous day Events*/
				if (week > 0) {
					for(int checkprevious = week-1;checkprevious>0;checkprevious--)
					if (mWeekEventValues.get(checkprevious).size() > 0) {
						Calendar todayStart = CalendarCommonFunction
								.convertDBDateToCalendar(start_date);
						String prev_end_date = mWeekEventValues.get(checkprevious)
								.get(0).get(CalendarConstants.END_DATE).toString();
						Calendar prevStart = CalendarCommonFunction
								.convertDBDateToCalendar(prev_end_date);
						if (todayStart.getTime().getTime() <= prevStart
								.getTime().getTime())
							prevevent = mWeekEventValues.get(checkprevious).size();
					}
				}
				int templeft = 0;
				int startpos = mFont_max_width - 10;
				int twidth = (mParentWidth - startpos) / 7;
				templeft = startpos + (twidth * week);
				int right = templeft + (twidth) - 1;
				int left = templeft + 2;
				int len = mContext.getResources().getInteger(
						R.integer.list_height);
				int alldayheight = len / 3;
				int totalevents = mWeekEventValues.get(week).size();
				int top = (HOUR_BLOCK_HEIGHT / (totalevents + nextevent + prevevent))
						* ((i) + prevevent);
				int bottom = (HOUR_BLOCK_HEIGHT / (totalevents + nextevent + prevevent))
						* (i + 1 + prevevent);

				top = (i + prevevent) * alldayheight;
				bottom = top + alldayheight;

				String calendarType = mWeekEventValues.get(week).get(i)
						.get(CalendarConstants.CALENDAR_TYPE_KEY).toString();
				CalendarLog
						.d(CalendarConstants.Tag, "eventtitle " + eventtitle);
				CalendarLog.d(CalendarConstants.Tag, "i " + i);
				CalendarLog.d(CalendarConstants.Tag, "totalevents "
						+ totalevents);
				CalendarLog.d(CalendarConstants.Tag, "nextevent " + nextevent);
				CalendarLog.d(CalendarConstants.Tag, "prevevent " + prevevent);
				CalendarLog.d(CalendarConstants.Tag, "bottom" + bottom);
				CalendarLog.d(CalendarConstants.Tag, "HOUR_BLOCK_HEIGHT"
						+ HOUR_BLOCK_HEIGHT);
				CalendarLog.d(CalendarConstants.Tag, "top" + top);
				CalendarLog.d(CalendarConstants.Tag, "left" + left);
				CalendarLog.d(CalendarConstants.Tag, "right" + right);
				int eventlength = 0;
				if (!mWeekEventValues.get(week).get(i).containsKey("startday")){
					eventlength = DateDifference(start_date, end_date);
					if(eventlength>0)
					eventlength--;
				}
				if(eventlength == 0)
					eventlength = 1;
				CalendarLog.d(CalendarConstants.Tag, "twidth" + twidth);
				CalendarLog.d(CalendarConstants.Tag, "eventlength" + eventlength);
				CalendarLog.d(CalendarConstants.Tag, "templeft" + templeft);
				right = templeft + (twidth * eventlength)-1;
				// Log.e("CAD","eventlength"+eventlength);

				if ((bottom - top) < alldayheight)
					bottom = top + alldayheight;
				if(HOUR_BLOCK_HEIGHT < bottom){
					HOUR_BLOCK_HEIGHT = bottom;
				}
				CalendarLog.d(CalendarConstants.Tag, "bottom" + bottom);
				CalendarLog.d(CalendarConstants.Tag, "left" + left);
				CalendarLog.d(CalendarConstants.Tag, "right" + right);
				RectF temp = new RectF(left, top, right, bottom - 2);
				ContentValues tContentvalues = new ContentValues();
				tContentvalues.put("eventtitle", eventtitle);
				tContentvalues.put("eventID", mWeekEventValues.get(week).get(i)
						.get("_id").toString());
				tContentvalues.put(CalendarConstants.CALENDAR_TYPE_KEY, calendarType);
				mEventValues.add(new CalendarEventRect(temp, tContentvalues));
			}
		}
	}

	private int DateDifference(String Start_date, String End_date) {
		String[] ST = Start_date.split("-");
		String[] ET = End_date.split("-");
		int diff = Integer.valueOf(ET[2]) - Integer.valueOf(ST[2]);
		return diff;
	}

	public void draw(Canvas canvas) {
		super.draw(canvas);

		mTodayday = Email.getNewCalendar();

		// int currday = todayday.get(Calendar.DAY_OF_WEEK);
		Calendar firstCal = (Calendar) mTodayday.clone();

		String WeekStartsOn = mPrefs.getString(
				mContext.getString(R.string.key_list_preference), "");
		if (WeekStartsOn.equalsIgnoreCase("2")) {
			firstCal.setFirstDayOfWeek(Calendar.SATURDAY);
			mTodayday.setFirstDayOfWeek(Calendar.SATURDAY);
		} else if (WeekStartsOn.equalsIgnoreCase("3")) {
			firstCal.setFirstDayOfWeek(Calendar.SUNDAY);
			mTodayday.setFirstDayOfWeek(Calendar.SUNDAY);
		} else if (WeekStartsOn.equalsIgnoreCase("4")) {
			firstCal.setFirstDayOfWeek(Calendar.MONDAY);
			mTodayday.setFirstDayOfWeek(Calendar.MONDAY);
		}

		int firstDayOfWeek = mTodayday.getFirstDayOfWeek();
		int dayOfWeek = mTodayday.get(Calendar.DAY_OF_WEEK);
		int currday = mTodayday.get(Calendar.DAY_OF_WEEK)
				- mTodayday.getFirstDayOfWeek();
		if (firstDayOfWeek == 7)
			currday = dayOfWeek;
		// firstCal.setFirstDayOfWeek(Calendar.SUNDAY);
		firstCal.add(Calendar.DAY_OF_WEEK, firstCal.getFirstDayOfWeek()
				- firstCal.get(Calendar.DAY_OF_WEEK));

		Calendar lastCal = (Calendar) firstCal.clone();
		lastCal.add(Calendar.DAY_OF_YEAR, 6);
		Calendar currentday = Email.getCurrentDay();
		if ((mTodayday.get(Calendar.MONTH) == currentday.get(Calendar.MONTH))
				&& (mTodayday.get(Calendar.YEAR) == currentday
						.get(Calendar.YEAR))) {
			if (firstCal.get(Calendar.DATE) <= currentday.get(Calendar.DATE)
					&& currentday.get(Calendar.DATE) <= lastCal
							.get(Calendar.DATE)) {
				mTodayday.set(Calendar.DATE, currentday.get(Calendar.DATE));
			}
		}

		
		int startpos = mFont_max_width - 10;
		int twidth = (mParentWidth - startpos) / 7;
		int bottompos = (1 * HOUR_BLOCK_HEIGHT + mFont_height / 2) - 6;
		if ((mTodayday.get(Calendar.MONTH) > currentday.get(Calendar.MONTH))
				&& (mTodayday.get(Calendar.YEAR) >= currentday
						.get(Calendar.YEAR))) {
	
			canvas.drawColor(mContext.getResources().getColor(R.color.layoutbg));

		} else if ((mTodayday.get(Calendar.DATE) == currentday
				.get(Calendar.DATE))
				&& (mTodayday.get(Calendar.MONTH) == currentday
						.get(Calendar.MONTH))
				&& (mTodayday.get(Calendar.YEAR) == currentday
						.get(Calendar.YEAR))) {
			
			canvas.drawColor(mContext.getResources().getColor(R.color.white));
			Paint paint = new Paint();
			paint.setColor(mContext.getResources().getColor(R.color.layoutbg));
			canvas.drawRect(startpos, 0, startpos + (twidth * (currday + 1)),
					bottompos, paint);

		} else if ((mTodayday.get(Calendar.MONTH) >= currentday
				.get(Calendar.MONTH))
				&& (mTodayday.get(Calendar.YEAR) >= currentday
						.get(Calendar.YEAR))) {
			
			if (mTodayday.get(Calendar.DATE) > currentday.get(Calendar.DATE))
				canvas.drawColor(mContext.getResources().getColor(R.color.layoutbg));
			else
				canvas.drawColor(mContext.getResources().getColor(R.color.white));
		} else {
			
			canvas.drawColor(mContext.getResources().getColor(R.color.white));
		}

		// int startposi = font_max_width - 10;

		canvas.drawLine(startpos, 0, startpos, bottompos, mLineHour);
		XCordinates[0] = mFont_max_width - 10;
		for (int i = 1; i < 7; i++) {
			
			int weeksplit = twidth * (i);
			int bottom = (HOUR_BLOCK_HEIGHT + mFont_height / 2) - 6;
			XCordinates[i] = weeksplit + startpos;
			canvas.drawLine(weeksplit + startpos, 0, weeksplit + startpos,
					bottom, mLineHour);
		}
		for (int i = 0; i < 1; i++) {
			float top = 0;
			float bottom = 0;
			if (i > 0)
				top = ((i - 1) * HOUR_BLOCK_HEIGHT + mFont_height / 2) - 5;
			bottom = (HOUR_BLOCK_HEIGHT + mFont_height / 2) - 6;
			canvas.drawRect(0, top, mFont_max_width - 10, bottom, mDefHour);
		}
		for (int i = 0; i < 1; i++) {

			// Y Coordinates of hours
			int lineYPosition = (i * HOUR_BLOCK_HEIGHT + mFont_height / 2) - 5;

			YCordinates[i] = lineYPosition;

			canvas.drawLine(0, lineYPosition, mParentWidth - 4, lineYPosition,
					mLineHour);

			// canvas.drawText(preString, X_OFFSET + 3, (i * HOUR_BLOCK_HEIGHT)
			// + font_height + 10, textp);

		}

		for (CalendarEventRect tEvent : mEventValues) {
			RectF drawRect = tEvent.getRect();
			canvas.drawRoundRect(drawRect, 1, 1, mRoundRectP);

			CalendarTextRect textRect = new CalendarTextRect(mTextevent);
			textRect.prepare(tEvent.getContentValues()
					.getAsString(CalendarConstants.EVENT_TITLE), (int) drawRect.width()
					- X_OFFSET, (int) drawRect.height() - Y_OFFSET);
			textRect.draw(canvas, (int) drawRect.left + 1, (int) drawRect.top);
		}

	}

	public boolean onTouch(View v, MotionEvent event) {

		int x = (int) event.getX();
		int y = (int) event.getY();
		
		for (CalendarEventRect tEvent : mEventValues) {
			RectF drawRect = tEvent.getRect();
			
			if (drawRect.contains(x, y)) {
				
				ContentValues values = tEvent.getContentValues();
				if (values.containsKey(CalendarConstants.EVENTS_ID)) {
					String event_id = tEvent.getContentValues().getAsString(
							CalendarConstants.EVENTS_ID);

					String calendarType = tEvent.getContentValues()
							.get(CalendarConstants.CALENDAR_TYPE_KEY).toString();

					
					ContentValues contentValues = null;
					if (calendarType.equals(CalendarConstants.CALENDAR_TYPE_CORPORATE)) {
						contentValues = CalendarDatabaseHelper
								.getEventContentValues(CalendarDatabaseHelper
										.getEventDetails(Integer
												.parseInt(event_id)));
						contentValues.put(CalendarConstants.CALENDAR_TYPE_KEY, CalendarConstants.CALENDAR_TYPE_CORPORATE);
					} else if (calendarType.equals(CalendarConstants.CALENDAR_TYPE_PERSONAL)) {
						if (Email.isDevicePreICS()) {
							contentValues = CalendarDBHelperClassPreICS
									.getPersonalEventContentValues(CalendarDBHelperClassPreICS
											.getEventDetails(Integer
													.parseInt(event_id)));
							contentValues.put(CalendarConstants.CALENDAR_TYPE_KEY,
									CalendarConstants.CALENDAR_TYPE_PERSONAL);

						} else {
							contentValues = CalendarDBHelperClassPreICS
									.getPersonalEventContentValues(CalendarDBHelperClassPreICS
											.getEventDetails(Integer
													.parseInt(event_id)));
							contentValues.put(CalendarConstants.CALENDAR_TYPE_KEY,
									CalendarConstants.CALENDAR_TYPE_PERSONAL);

						}
					} else {

					}
					Bundle contentValue = new Bundle();
					contentValue.putParcelable(CalendarConstants.EVENT_CONTENT_VALUES,
							contentValues);
					if(!contentValues.getAsString(CalendarConstants.CALENDAR_TYPE_KEY).equals(CalendarConstants.CALENDAR_TYPE_PERSONAL))
					{
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
		return super.onTouchEvent(event);
	}

}
