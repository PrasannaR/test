package com.cognizant.trumobi.calendar.view;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.calendar.modal.Event;
import com.cognizant.trumobi.calendar.modal.Recurrence;
import com.cognizant.trumobi.calendar.util.CalendarCommonFunction;
import com.cognizant.trumobi.calendar.view.CalendarMonthViewFragment.OnMonthListener;
import com.cognizant.trumobi.em.Email;

public abstract class CalendarMonthAdapter extends BaseAdapter {
	private GregorianCalendar mCalendar;
	private Calendar mCalendarToday;
	private Context mContext;
	private DisplayMetrics mDisplayMetrics;
	private ArrayList<ArrayList<ContentValues>> eventData;
	private List<String> mItems;
	private int mMonth;
	private int mYear;
	private int mDaysShown = 0;
	private int mDaysLastMonth = 0;
	private int mDaysNextMonth = 0;
	private static int mDaysPreviousMonth = 60;
	private int mTitleHeight, mDayHeight;
	private int mSelectedDay;
	private int mSelectedMonth;
	private int mSelectedYear;
	private OnMonthListener onMonthListener;

	private final int[] mDaysInMonth = { 31, 28, 31, 30, 31, 30, 31, 31, 30,
			31, 30, 31 };
	private int mNewDay;
	private int mNewMonth;
	private int mNewYear;
	// NewlyAdded
	private int mStartDate;
	private int mEndDate;

	public CalendarMonthAdapter(Context c, int month, int year,
			DisplayMetrics metrics, ArrayList<ArrayList<ContentValues>> event,
			OnMonthListener onMonthListener) {
		mContext = c;
		mMonth = month;
		mYear = year;
		mCalendar = new GregorianCalendar(mYear, mMonth, 1);
		mCalendarToday = Calendar.getInstance();
		mDisplayMetrics = metrics;
		eventData = event;
		this.onMonthListener = onMonthListener;
		this.mSelectedDay = Email.selectedDay;
		this.mSelectedMonth = Email.selectedMonth;
		this.mSelectedYear = Email.selectedYear;
		mItems = new ArrayList<String>();

		populatePreviousMonth(calculateprevmonth(month, -2), true);
		populatePreviousMonth(calculateprevmonth(month, -1), false);
		populateMonth();
		populateNextMonth(calculateprevmonth(month, 1), false);
		populateNextMonth(calculateprevmonth(month, 2), true);
	}

	/**
	 * @param date
	 *            - null if day title (0 - dd / 1 - mm / 2 - yy)
	 * @param position
	 *            - position in item list
	 * @param item
	 *            - view for date
	 */
	protected abstract void onDate(int[] date, int position, View item);

	private int calculateprevmonth(int month, int difference) {
		int calmonth = 0;
		calmonth = month + difference;
		if (calmonth == 12)
			calmonth = 0;
		else if (calmonth == 13)
			calmonth = 1;
		else if (calmonth == -1)
			calmonth = 11;
		else if (calmonth == -2)
			calmonth = 10;
		return calmonth;
	}

	private void populateMonth() {
		setPreviousdates(mDaysLastMonth);
		int daysInMonth = daysInMonth(mMonth);
		for (int i = 1; i <= daysInMonth; i++) {
			mItems.add(String.valueOf(i));
			mDaysShown++;
		}
		mDaysNextMonth = 1;
		mTitleHeight = 30;
		// int rows = (mDaysShown / 7);
		int rows = 6;
		mDayHeight = (mDisplayMetrics.heightPixels - mTitleHeight - (rows * 1) - getBarHeight())
				/ (rows);
	}

	private void populatePreviousMonth(int previousMonth, boolean isfirst) {
		Calendar prevCalendar = (Calendar) mCalendar.clone();
		prevCalendar.set(Calendar.MONTH, previousMonth);

		if (isfirst) {
			int firstDay = getDay(prevCalendar.get(Calendar.DAY_OF_WEEK));
			int prevDay;

			if (previousMonth == 0)
				prevDay = daysInMonth(11) - firstDay + 1;
			else
				prevDay = daysInMonth(previousMonth - 1) - firstDay + 1;

			for (int i = 0; i < firstDay; i++) {
				mItems.add(String.valueOf(prevDay + i));
				mDaysLastMonth++;
				mDaysShown++;
			}
		}
		int daysInMonth = daysInMonth(previousMonth);
		for (int i = 1; i <= daysInMonth; i++) {
			mItems.add(String.valueOf(i));
			mDaysLastMonth++;
			mDaysShown++;
		}
	}

	private void populateNextMonth(int nextMonth, boolean islast) {
		Calendar nextCalendar = (Calendar) mCalendar.clone();
		nextCalendar.set(Calendar.MONTH, nextMonth);

		int daysInMonth = daysInMonth(nextMonth);
		for (int i = 1; i <= daysInMonth; i++) {
			mItems.add(String.valueOf(i));
			mDaysNextMonth++;
			mDaysShown++;
		}
		int extradays = 1;
		if (islast)
			while (mDaysShown % 7 != 0) {
				mItems.add(String.valueOf(extradays));
				mDaysShown++;
				mDaysNextMonth++;
				extradays++;
			}
	}

	private int daysInMonth(int month) {
		int daysInMonth = mDaysInMonth[month];
		if (month == 1 && mCalendar.isLeapYear(mYear))
			daysInMonth++;
		return daysInMonth;
	}

	private int getBarHeight() {
		switch (mDisplayMetrics.densityDpi) {
		case DisplayMetrics.DENSITY_HIGH:
			return 48;
		case DisplayMetrics.DENSITY_MEDIUM:
			return 32;
		case DisplayMetrics.DENSITY_LOW:
			return 24;
		default:
			return 48;
		}
	}

	private int getDay(int day) {
		switch (day) {
		case Calendar.MONDAY:
			return 0;
		case Calendar.TUESDAY:
			return 1;
		case Calendar.WEDNESDAY:
			return 2;
		case Calendar.THURSDAY:
			return 3;
		case Calendar.FRIDAY:
			return 4;
		case Calendar.SATURDAY:
			return 5;
		case Calendar.SUNDAY:
			return 6;
		default:
			return 0;
		}
	}

	private boolean isToday(int day, int month, int year) {
		if (mCalendarToday.get(Calendar.MONTH) == month
				&& mCalendarToday.get(Calendar.YEAR) == year
				&& mCalendarToday.get(Calendar.DAY_OF_MONTH) == day) {
			return true;
		}
		return false;
	}

	private void getWeekDate(int day, int month, int year) {
		// set the date

		Calendar cal = Email.getNewCalendar();
		cal.set(year, month - 1, day);

		// "calculate" the start date of the week
		Calendar first = (Calendar) cal.clone();
		first.add(Calendar.DAY_OF_WEEK,
				first.getFirstDayOfWeek() - first.get(Calendar.DAY_OF_WEEK));

		// and add six days to the end date
		Calendar last = (Calendar) first.clone();
		last.add(Calendar.DAY_OF_YEAR, 6);

		// print the result
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

		String[] splitStartDate = CalendarCommonFunction.getSplitDate(df
				.format(first.getTime()));
		String[] splitEndDate = CalendarCommonFunction.getSplitDate(df
				.format(last.getTime()));

		// NewlyAdded
		mStartDate = Integer.parseInt(splitStartDate[2].trim());
		mEndDate = Integer.parseInt(splitEndDate[2].trim());

	}

	private int[] getDate(int position) {
		int date[] = new int[3];
		if (position <= mDaysLastMonth - 1) {
			// previous month
			date[0] = Integer.parseInt(mItems.get(position));
			if (mMonth == 0) {
				date[1] = 11;
				date[2] = mYear - 1;
			} else {
				date[1] = mMonth - 1;
				date[2] = mYear;
			}
		} else if (position <= mDaysShown - mDaysNextMonth) {
			// current month
			date[0] = position - (mDaysLastMonth - 1);
			date[1] = mMonth;
			date[2] = mYear;
		} else {
			// next month
			date[0] = Integer.parseInt(mItems.get(position));
			if (mMonth == 11) {
				date[1] = 0;
				date[2] = mYear + 1;
			} else {
				date[1] = mMonth + 1;
				date[2] = mYear;
			}
		}
		return date;
	}

	private View.OnCreateContextMenuListener vC = new View.OnCreateContextMenuListener() {

		@Override
		public void onCreateContextMenu(ContextMenu arg0, View arg1,
				ContextMenuInfo arg2) {
			Calendar cal = Email.getCurrentDay();
			cal.set(Calendar.DATE, mNewDay);
			cal.set(Calendar.MONTH, mNewMonth);
			cal.set(Calendar.YEAR, mNewYear);
			String weekday = Email.getWeekDayOfDate(cal.get(Calendar.DATE)
					+ "/" + (cal.get(Calendar.MONTH)) + "/"
					+ cal.get(Calendar.YEAR));

			arg0.setHeaderTitle(weekday + "," + mNewDay + " "
					+ Email.getNameOFMonth(mNewMonth));
			arg0.add(0, 0, 0, "New Event").setOnMenuItemClickListener(
					mMenuItemClickListener);

		}

		private OnMenuItemClickListener mMenuItemClickListener = new OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {

				switch (item.getItemId()) {
				case 0:
					// Log.i("info", "Call");
					Intent eventIntent = new Intent(
							mContext,
							com.cognizant.trumobi.calendar.event.CalendarAddEvent.class);
					eventIntent.putExtra("NewDay", mNewDay);
					eventIntent.putExtra("NewMonth", mNewMonth - 1);
					eventIntent.putExtra("NewYear", mNewYear);
					mContext.startActivity(eventIntent);
					return true;

				}

				return false;
			}
		};
	};

	int dayPosition;

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final int dayPosition = position;
		final int[] day = getDate(dayPosition);
		final int strDay = day[0];
		final int strMonth = day[1];
		final String disMonth = CalendarCommonFunction.getMonth(strMonth);
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View gridView = null;

		gridView = inflater.inflate(R.layout.cal_monthview_item, null);

		final RelativeLayout layoutViews = (RelativeLayout) gridView
				.findViewById(R.id.monthitem_view);
		LinearLayout dotLinear = (LinearLayout) gridView
				.findViewById(R.id.monthitem_events);

		final TextView views = (TextView) gridView
				.findViewById(R.id.month_item_date);

		LinearLayout.LayoutParams lParam;
		lParam = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);

		TextView firstView = (TextView) dotLinear.getChildAt(0);
		int mEventTop = firstView.getHeight();

		layoutViews.setOnCreateContextMenuListener(vC);
		int mEventSize = (mDayHeight - mEventTop - 5) / 6;
		mEventSize = mEventSize - 2;
		int mEventCountsShowing = 0;
		for (int i = 0; i < eventData.size(); i++) {

			for (int j = 0; j < eventData.get(i).size(); j++) {
				int rtype = eventData.get(i).get(j)
						.getAsInteger(Recurrence.TYPE);
				String eventStartDate = eventData.get(i).get(j)
						.getAsString(Event.START_DATE);
				String eventEndDate = eventData.get(i).get(j)
						.getAsString(Event.END_DATE);
				// CalendarLog.d(CalendarConstants.TAG, "eventStartDate "
				// + eventStartDate + " - " + eventEndDate);
				String rrule = eventData.get(i).get(j).getAsString(Event.RRULE);
				long startTime = eventData.get(i).get(j)
						.getAsLong(Event.DTSTART);
				long endTime = eventData.get(i).get(j).getAsLong(Event.DTEND);
				String eventTitle = eventData.get(i).get(j)
						.getAsString(Event.TITLE);
				if (rrule == null)
					rrule = "0";

				String[] strDayArray = eventStartDate.split("-");
				String eveMonth = strDayArray[1];
				String eveDay = strDayArray[2];
				int eveEndMonth = Integer.parseInt(eveMonth);
				int eveEndDay = Integer.parseInt(eveDay);
				// int eveEndYear = Integer.parseInt(strDayArray[0]);

				// boolean isAfter = true;
				Calendar eventCal = Calendar.getInstance();
				eventCal.setFirstDayOfWeek(Calendar.SUNDAY);
				eventCal.set(Calendar.DAY_OF_MONTH, Integer.valueOf(eveDay));
				eventCal.set(Calendar.MONTH, Integer.valueOf(eveMonth) - 1);
				eventCal.set(Calendar.YEAR, Integer.valueOf(strDayArray[0]));
				int eventdayofweek = eventCal.get(Calendar.DAY_OF_WEEK);

				eventdayofweek = eventdayofweek - 1;

				Calendar currentCal = Calendar.getInstance();
				currentCal.setFirstDayOfWeek(Calendar.SUNDAY);
				currentCal.set(Calendar.DAY_OF_MONTH, Integer.valueOf(strDay));
				currentCal.set(Calendar.MONTH, Integer.valueOf(strMonth));
				currentCal.set(Calendar.YEAR, Integer.valueOf(day[2]));
				int currentdayofweek = currentCal.get(Calendar.DAY_OF_WEEK);

				Calendar eventEndCal = CalendarCommonFunction
						.convertDBDateToCalendar(eventEndDate);

				if (eventEndDate != null) {
					// String[] strEndDayArray = eventEndDate.split("-");
					if ((endTime - startTime) > 86400000) {
						eventEndCal.add(Calendar.DAY_OF_YEAR, -1);
					}
					eveEndMonth = eventEndCal.get(Calendar.MONTH) + 1;
					eveEndDay = eventEndCal.get(Calendar.DAY_OF_MONTH);

					// eveEndYear = eventEndCal.get(Calendar.YEAR);
				}
				// CalendarLog.d(CalendarConstants.TAG, "allday "
				// + allday);
				// CalendarLog.d(CalendarConstants.TAG, "eventTitle "
				// + eventTitle);
				// CalendarLog.d(CalendarConstants.TAG, "eveMonth " + eveMonth
				// + " - " + eveDay);
				// CalendarLog.d(CalendarConstants.TAG, "eveEndMonth "
				// + eveEndMonth + " - " + eveEndDay);
				// CalendarLog.d(CalendarConstants.TAG, "strMonth " + strMonth
				// + " - " + strDay);
				// if (currentCal.getTime().before(eventCal.getTime())) {
				// isAfter = false;
				// } else
				// isAfter = true;

				currentdayofweek = currentdayofweek - 1;

				if (((Integer.parseInt(eveDay) <= strDay && Integer
						.parseInt(eveMonth) <= strMonth + 1) && (eveEndDay >= strDay && eveEndMonth >= strMonth + 1))
						|| (rtype > -1 && (Integer.parseInt(eveDay) == strDay && Integer
								.parseInt(eveMonth) == strMonth + 1))) {
					// int size = Email.convertToDp(12);
					View mEventShow = inflater.inflate(
							R.layout.cal_month_events, null);
					if (mEventCountsShowing < 4) {
						TextView textView = (TextView) mEventShow
								.findViewById(R.id.month_event_color);
						textView.setHeight(mEventSize);
						textView.setWidth(mEventSize);
						textView.setBackgroundResource(R.drawable.cal_month_event_color);

						TextView mEventTitle = (TextView) mEventShow
								.findViewById(R.id.month_event_title);
						mEventTitle.setHeight(mEventSize);
						mEventTitle.setText(eventTitle);
						dotLinear.addView(mEventShow, lParam);
					} else if (mEventCountsShowing == 4) {
						TextView textView = (TextView) mEventShow
								.findViewById(R.id.month_event_color);
						textView.setHeight(mEventSize);
						textView.setWidth(mEventSize);
						textView.setVisibility(View.INVISIBLE);
						TextView mEventTitle = (TextView) mEventShow
								.findViewById(R.id.month_event_title);
						mEventTitle.setHeight(mEventSize);
						mEventTitle.setTextSize(12);
						mEventTitle.setText("+ 1");
						dotLinear.addView(mEventShow, lParam);
					} else {
						LinearLayout mLay = (LinearLayout) dotLinear
								.getChildAt(5);

						if (mLay.getChildCount() > 0) {
							TextView mEventTitle = (TextView) mLay
									.getChildAt(1);
							mEventTitle.setTextSize(12);
							mEventTitle.setHeight(mEventSize);
							int mReminingCount = mEventCountsShowing - 3;
							mEventTitle.setText("+ "
									+ String.valueOf(mReminingCount));
						}
					}
					mEventCountsShowing++;

					// CalendarLog.e("MonthAdapter", "size" + size);
					// CalendarLog.e("MonthAdapter",
					// "dotLinear" + dotLinear.getHeight());
				}
			}
		}
		// }
		// CalendarLog.e("MonthAdapter", "dotLinear 2");

		views.setGravity(Gravity.RIGHT);
		views.setText(mItems.get(position));
		views.setTextColor(mContext.getResources().getColor(
				R.color.month_default_font_color));
		views.setSingleLine(true);
		layoutViews.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				String date = views.getText().toString();

				String presentDay = date + " " + disMonth + " " + day[2];
				Email.setPresentDate(presentDay);
				onMonthListener.callMonth(day[0], day[1] + 1, day[2]);

			}
		});

		layoutViews.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View arg0) {
				mNewDay = day[0];
				mNewMonth = day[1] + 1;
				mNewYear = day[2];

				layoutViews.showContextMenu();
				return true;
			}
		});
		int[] date = getDate(position);
		if (date != null) {
			views.setHeight(mDayHeight - 5);
			if (date[1] != mMonth) {
				// previous or next month
				views.setBackgroundColor(mContext.getResources().getColor(
						R.color.layoutbg));
				layoutViews.setBackgroundColor(mContext.getResources()
						.getColor(R.color.layoutbg));
			} else {
				// current month
				getWeekDate(mSelectedDay, mSelectedMonth, mSelectedYear);
				views.setBackgroundColor(Color.rgb(244, 244, 244));
				layoutViews.setBackgroundColor(Color.rgb(244, 244, 244));
				if (date[0] >= mStartDate && date[0] <= mEndDate) {

				}
				if (isToday(date[0], date[1], date[2])) {
					views.setTypeface(null, Typeface.BOLD);
					views.setTextColor(mContext.getResources().getColor(
							R.color.month_current_fontcolor));
					views.setBackgroundColor(Color.WHITE);
					layoutViews.setBackgroundColor(Color.WHITE);
				}
			}
		}

		onDate(date, position, views);
		return gridView;
	}

	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public static int getPreviousdates() {
		return mDaysPreviousMonth;
	}

	public static void setPreviousdates(int mdays) {
		mDaysPreviousMonth = mdays;
	}

}
