package com.cognizant.trumobi.calendar.view;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.calendar.provider.CalendarDBHelperClassPreICS;
import com.cognizant.trumobi.calendar.provider.CalendarDatabaseHelper;
import com.cognizant.trumobi.calendar.util.CalendarCommonFunction;
import com.cognizant.trumobi.calendar.util.CalendarConstants;
import com.cognizant.trumobi.calendar.view.CalendarCustomScrollView.CalendarCustomScrollViewListener;
import com.cognizant.trumobi.calendar.view.CalendarDayViewFragment.OnViewRefreshListener;
import com.cognizant.trumobi.calendar.view.CalendarView.OnCalendarViewSelectedListener;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.log.CalendarLog;
import com.google.common.collect.Maps;

public class CalendarDayViewPagerAdapter extends PagerAdapter {

	private int[] mPageIDsArray;
	private int mCount;
	private Context mContext;
	private int mCurrentPosition = 0, mPreviousPosition = 0, mResult;
	private ArrayList<ContentValues> mContentValues = new ArrayList<ContentValues>();
	private int mCurrentposition = 0;
	private boolean mIsnextchanged = false;
	private ViewPager mPager;
	private int mFont_max_width;
	private Paint mTextp;
	//private FragmentActivity mFragmentActivity;
	OnCalendarViewSelectedListener mOnCalendarViewSelectedListener;
	Map<Integer, Object> views = Maps.newHashMap();
	private String mStartTime;
	DisplayMetrics metrics;
	

	public CalendarDayViewPagerAdapter(Context context, final ViewPager pager,
			int[] pageIDs, final OnViewRefreshListener mOnViewRefreshListener,
			OnCalendarViewSelectedListener mOnCalendarViewSelectedListener,
			String startTime) {
		super();
		
		mContext = context;
		mStartTime = startTime;
		//this.mFragmentActivity = fragmentActivity;
		metrics = mContext.getResources().getDisplayMetrics();
		this.mOnCalendarViewSelectedListener = mOnCalendarViewSelectedListener;
		int actualNoOfIDs = pageIDs.length;
		mCount = actualNoOfIDs + 2;
		mPageIDsArray = new int[mCount];
		for (int i = 0; i < actualNoOfIDs; i++) {
			mPageIDsArray[i + 1] = pageIDs[i];
		}
		mPageIDsArray[0] = pageIDs[actualNoOfIDs - 1];
		mPageIDsArray[mCount - 1] = pageIDs[0];

		this.mPager = pager;
		mTextp = new Paint();
		mTextp.setStyle(Paint.Style.FILL);
		mTextp.setColor(mContext.getResources().getColor(R.color.agenda_day_item_text_color));
		mTextp.setAntiAlias(true);
		//DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		//screenwidth = metrics.widthPixels;
		mTextp.setTextSize(12*metrics.density);
		String finalVal = " 12AM; ";
		Rect fontrect = new Rect();
		mTextp.getTextBounds(finalVal, 0, finalVal.length(), fontrect);
		mFont_max_width = fontrect.width();
		pager.setOnPageChangeListener(new OnPageChangeListener() {

			public void onPageSelected(int position) {
				mPreviousPosition = mCurrentPosition;
				mCurrentPosition = position;
				CalendarLog.d(CalendarConstants.Tag, "currentposition : "
						+ mCurrentposition);
				CalendarLog.d(CalendarConstants.Tag, "position : " + position);
				
				mResult = mCurrentPosition - mPreviousPosition;
				mOnViewRefreshListener.onRefreshActionBar(mResult);

				int pageCount = getCount();
				if (position == 0) {
					mIsnextchanged = true;
					pager.setCurrentItem(pageCount - 2, false);
					return;
				} else if (position == pageCount - 1) {
					mIsnextchanged = true;
					pager.setCurrentItem(1, false);
					return;
				}
				if (mIsnextchanged) {
					CalendarLog.d(CalendarConstants.Tag, "OnPage-isnext"
							+ "position :" + position);
					View tempView1 = findViewForPosition(position - 1);
					if (tempView1 != null)
						UpdateView(tempView1, -1);

					View tempView2 = findViewForPosition(position);
					UpdateView(tempView2, 0);

					tempView1 = findViewForPosition(position + 1);
					if (tempView1 != null)
						UpdateView(tempView1, 1);

				} else {
					if (position != 3) {
						View tempView2 = findViewForPosition(position);
						displayListView(tempView2);
					}
				}
				mCurrentposition = position;
			}

			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
			}

			public void onPageScrollStateChanged(int state) {

			}
		});
	}

	public Object instantiateItem(View container, int position) {
		LayoutInflater inflater = (LayoutInflater) container.getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		int pageId = mPageIDsArray[position];

		View view = inflater.inflate(pageId, null);
		// Log.e("instantiate Day View", "----");
		if (position == 3)
			displayListView(view);
		else if (position == 2) {
			UpdateView(view,-1);
			
		} else if (position == 1) {
			UpdateView(view, -2);
		} else if (position == 0) {
			UpdateView(view, -3);
		} else if (position == 4) {
			UpdateView(view, 1);
		}
		views.put(position, view);
		((ViewPager) container).addView(view, 0);
		return view;
	}

	protected View findViewForPosition(int position) {
		Object object = views.get(position);
		if (object != null) {
			for (int i = 0; i < mPager.getChildCount(); i++) {
				View view = mPager.getChildAt(i);
				if (isViewFromObject(view, object)) {
					return view;
				}
			}
		}
		return null;
	}

	public int getCount() {
		return mCount;
	}

	@Override
	public void destroyItem(View container, int position, Object object) {
		((ViewPager) container).removeView((View) object);
	}

	@Override
	public void finishUpdate(View container) {
		
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == ((View) object);
	}

	@Override
	public void restoreState(Parcelable state, ClassLoader loader) {

	}

	@Override
	public Parcelable saveState() {

		return null;
	}

	@Override
	public void startUpdate(View container) {

	}

	public void displayListView(View loadListview) {
		// create an ArrayAdaptar from the String Array
		if (loadListview != null) {
			final com.cognizant.trumobi.calendar.view.CalendarCustomScrollView listView = (com.cognizant.trumobi.calendar.view.CalendarCustomScrollView) loadListview
					.findViewById(R.id.hrs_listview);
			if (CalendarCommonFunction.isTablet(mContext)) {
				TextView txt_dayview_header = (TextView) loadListview
						.findViewById(R.id.txt_dayview_header);
				String dayViewHeadervalue = CalendarCommonFunction
						.getNameOFMonth(Email.todayMonth).toString()
						+ " "
						+ Email.todayDate
						+ ","
						+ Email.todayYear
						+ " "
						+ Email
								.getWeekDayOfDate(Email.todayDate
										+ "/"
										+ Email.todayMonth
										+ "/"
										+ Email.todayYear);
				txt_dayview_header.setText(dayViewHeadervalue);
			}
			Calendar cal = Email.getNewCalendar();
			String presentHours = cal.get(Calendar.HOUR_OF_DAY) + ":"
					+ cal.get(Calendar.MINUTE);
			Calendar presentCal = Email.getCurrentDay();
			AddAlldayEvents(loadListview, Email.getPresentDay());
//			new Thread(new Runnable() {
//				
//				@Override
//				public void run() {
					mContentValues = CalendarDatabaseHelper.getDayListValues();		
//				}
//			}).start();
			
		
			if (listView.getChildCount() > 0)
				listView.removeAllViews();
			final CalendarCustomDayView convertView = new CalendarCustomDayView(
					mContext, mContentValues, Email.getPresentDay());
			convertView.invalidate();
			final int couHours = convertView.convertTimetoSeconds(presentHours);
			listView.addView(convertView);
			final int len =(int)( 60 * metrics.density);
			if (cal.get(Calendar.DATE) == presentCal.get(Calendar.DATE)
					&& cal.get(Calendar.MONTH) == presentCal
							.get(Calendar.MONTH)
					&& cal.get(Calendar.YEAR) == presentCal.get(Calendar.YEAR)){
				listView.post(new Runnable() {
					@Override
					public void run() {
						int hours = 0;
						hours = couHours;
						if (hours > len)
							hours = hours - len;
						listView.scrollTo(0, hours);
					}
				});
			}else if (!mStartTime.equalsIgnoreCase("")) {
				long strTime = Long.parseLong(mStartTime);
				cal.setTimeInMillis(strTime);
				presentHours = cal.get(Calendar.HOUR_OF_DAY) + ":"
						+ cal.get(Calendar.MINUTE);
				final int couHour = convertView
						.convertTimetoSeconds(presentHours);
				listView.post(new Runnable() {
					@Override
					public void run() {
						int hours = 0;
						hours = couHour;
						if (hours > len)
							hours = hours - len;
						listView.scrollTo(0, hours);
					}
				});
			}
			
			else
			{
				
				final Calendar  newCal = Email.getNewCalendar();
				presentHours = newCal.get(Calendar.HOUR_OF_DAY) + ":"
						+ newCal.get(Calendar.MINUTE);
				final int couHour = convertView
						.convertTimetoSeconds(presentHours);
				listView.post(new Runnable() {
					@Override
					public void run() {
						int hours = 0;
						hours = couHour;
						if (hours > len)
							hours = hours - len;
						listView.scrollTo(0, hours);
					}
				});
			}
			listView.setOnScrollViewListener(new CalendarCustomScrollViewListener() {
				public void onScrollChanged(CalendarCustomScrollView v, int l,
						int t, int oldl, int oldt) {
					convertView.mGetTopYPosition = t + len;

				}
			});
		}
	}

	public void UpdateView(View loadListview, int numberofdays) {
		String string = Email.getPresentDay();

		Date date = null;
		try {
			date = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
					.parse(string);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Calendar cal = Email.getNewCalendar();
		cal.setTime(date);
		if (CalendarCommonFunction.isTablet(mContext)) {

			TextView txt_dayview_header = (TextView) loadListview
					.findViewById(R.id.txt_dayview_header);
			String dayViewHeadervalue = CalendarCommonFunction.getNameOFMonth(
					Email.todayMonth).toString()
					+ " "
					+ Email.todayDate
					+ ","
					+ Email.todayYear
					+ " "
					+ Email
							.getWeekDayOfDate(Email.todayDate
									+ "/" + Email.todayMonth + "/"
									+ Email.todayYear);
			txt_dayview_header.setText(dayViewHeadervalue);
			this.mOnCalendarViewSelectedListener.onDayViewScrolled(cal);
		}
		cal.add(Calendar.DAY_OF_YEAR, numberofdays);
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy",
				Locale.ENGLISH);

		AddAlldayEvents(loadListview, sdf.format(cal.getTime()));
		ArrayList<ContentValues> tempValues = new ArrayList<ContentValues>();//CalendarDatabaseHelper
//				.getDayListValues(sdf.format(cal.getTime()));

		tempValues = CalendarDatabaseHelper
				.getDayListValues(sdf.format(cal.getTime()));
		CalendarCustomScrollView listView = (CalendarCustomScrollView) loadListview
				.findViewById(R.id.hrs_listview);
		if (listView.getChildCount() > 0) {
			listView.removeAllViews();
		}
		final int len =  (int)( 60 * metrics.density);
		final CalendarCustomDayView convertView = new CalendarCustomDayView(mContext,
				tempValues, sdf.format(cal.getTime()));
		convertView.invalidate();
		listView.setOnScrollViewListener(new CalendarCustomScrollViewListener() {
			public void onScrollChanged(CalendarCustomScrollView v, int l,
					int t, int oldl, int oldt) {
				convertView.mGetTopYPosition = t + len;

			}
		});
		listView.addView(convertView);
		listView.scrollTo(0, 120);

	}

	private void AddAlldayEvents(View loadListview, String date) {
		ScrollView scrollView = (ScrollView) loadListview
				.findViewById(R.id.allday);
		LinearLayout linLay = (LinearLayout) scrollView.getChildAt(0);
		if (linLay.getChildCount() > 0)
			linLay.removeAllViews();
		int alldayheight = mContext.getResources().getInteger(
				R.integer.event_all_day_height);
		// Hours lines height
		int len = mContext.getResources().getInteger(R.integer.list_height);
		alldayheight = len / 3;
		linLay.setPadding(mFont_max_width - 10, 0, 3, 0);

		ArrayList<ContentValues> tempValues = CalendarDatabaseHelper
				.getAllDayListValues(date);
		ArrayList<ContentValues> tempEndValues = CalendarDatabaseHelper
				.getAllDayListValuesForEndDate(date);
		for (ContentValues tempEnd : tempEndValues) {
			tempValues.add(tempEnd);
		}
		int alldaycount = 0;
		for (int i = 0; i < tempValues.size(); i++) {
			TextView tv = new TextView(mContext);
			LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, alldayheight);

			tv.setBackgroundColor(mContext.getResources().getColor(
					R.color.event_bgcolor));
			final String event_id = tempValues.get(i).getAsString("_id");
			final String calendarType = tempValues.get(i).get(CalendarConstants.CALENDAR_TYPE_KEY)
					.toString();
			tv.setText(tempValues.get(i).getAsString("event"));
			tv.setTextColor(Color.WHITE);
			tv.setPadding(5, 0, 0, 0);
			if (alldaycount > 0) {
				param.setMargins(0, 1, 0, 0);
			} else {

			}
			tv.setLayoutParams(param);

			tv.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					ContentValues contentValues = null;

					CalendarLog.d(CalendarConstants.Tag, "All day"
							+ calendarType);
					if (calendarType.equals(CalendarConstants.CALENDAR_TYPE_CORPORATE)) {
						contentValues = CalendarDatabaseHelper
								.getEventContentValues(CalendarDatabaseHelper
										.getEventDetails(Integer
												.parseInt(event_id)));
						contentValues.put(CalendarConstants.CALENDAR_TYPE_KEY, CalendarConstants.CALENDAR_TYPE_CORPORATE);
					} else if (calendarType.equals(CalendarConstants.CALENDAR_TYPE_PERSONAL)) {
						
							contentValues = CalendarDBHelperClassPreICS
									.getPersonalEventContentValues(CalendarDBHelperClassPreICS
											.getEventDetails(Integer
													.parseInt(event_id)));
							contentValues.put(CalendarConstants.CALENDAR_TYPE_KEY,
									CalendarConstants.CALENDAR_TYPE_PERSONAL);
						
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
						eventIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						mContext.startActivity(eventIntent);
					}
					

				}
			});
			linLay.addView(tv);
			alldaycount++;

		}
		if (alldaycount > 0) {
			scrollView.setVisibility(View.VISIBLE);
			int MaxLength = alldayheight * (alldaycount);

			if (MaxLength > len) {
				MaxLength = len;
				MaxLength = MaxLength - 15;
			}
			CalendarLog.d(CalendarConstants.Tag, "len:" + len);
			CalendarLog.d(CalendarConstants.Tag, "MaxLength:" + MaxLength);
			LinearLayout.LayoutParams allparam = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, MaxLength);
			scrollView.setLayoutParams(allparam);
		} else {
			scrollView.setVisibility(View.GONE);
		}
	}
	
}
