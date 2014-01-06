package com.cognizant.trumobi.calendar.view;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.apache.cordova.api.LOG;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
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

import com.TruBoxSDK.SharedPreferences;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.calendar.provider.CalendarDatabaseHelper;
import com.cognizant.trumobi.calendar.util.CalendarCommonFunction;
import com.cognizant.trumobi.calendar.util.CalendarConstants;
import com.cognizant.trumobi.calendar.view.CalendarCustomScrollView.CalendarCustomScrollViewListener;
import com.cognizant.trumobi.calendar.view.CalendarView.OnCalendarViewSelectedListener;
import com.cognizant.trumobi.calendar.view.CalendarWeekViewFragment.OnViewWeekRefreshListener;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.log.CalendarLog;
import com.google.common.collect.Maps;

public class CalendarWeekPagerAdapter extends PagerAdapter {
	private int[] mPageIDsArray;
	private int mCount = 0;
	private Context mContext;

	private ViewPager mPager;
	private int mCurrentposition = 0;
	private final Calendar mCurrentCalDate;
	private boolean mIsnextchanged = false;
	private Paint mTextp;
	private int mScreenwidth;
	private Map<Integer, Object> mViews = Maps.newHashMap();
	private OnCalendarViewSelectedListener mOnCalendarViewSelectedListener;
	private SharedPreferences mSecurePrefs;
	private int pos;
	private FragmentActivity mFragmentActivity;
	
	public CalendarWeekPagerAdapter(
			Context  context, final ViewPager pager,
			int[] pageIDs,
			final OnViewWeekRefreshListener mOnViewRefreshListener,
			final OnCalendarViewSelectedListener mOnCalendarViewSelectedListener,
			Calendar currentDate) {
		super();
		mContext = context;
		DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
		mTextp = new Paint();
		mTextp.setStyle(Paint.Style.FILL);
		mTextp.setColor(mContext.getResources().getColor(R.color.agenda_day_item_text_color));
		mTextp.setAntiAlias(true);
		mTextp.setTextSize(12*metrics.density);
	//	this.mFragmentActivity = fragmentActivity;
		this.mPager = pager;
		this.mCurrentCalDate = currentDate;

		this.mOnCalendarViewSelectedListener = mOnCalendarViewSelectedListener;

		//DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		mScreenwidth = metrics.widthPixels;
		int actualNoOfIDs = pageIDs.length;
		mCount = actualNoOfIDs + 2;
		mPageIDsArray = new int[mCount];
		for (int i = 0; i < actualNoOfIDs; i++) {
			mPageIDsArray[i + 1] = pageIDs[i];
		}
		mPageIDsArray[0] = pageIDs[actualNoOfIDs - 1];
		mPageIDsArray[mCount - 1] = pageIDs[0];

		pager.setOnPageChangeListener(new OnPageChangeListener() {

			public void onPageSelected(int position) {

				pos = position;
				if (mCurrentposition == (position - 1)) {
					Email.setPageScrollLeft(true);
					mCurrentCalDate.add(Calendar.DAY_OF_YEAR, 7);
				} else if ((mCurrentposition - 1) == position) {
					Email.setPageScrollLeft(false);
					mCurrentCalDate.add(Calendar.DAY_OF_YEAR, -7);
				}
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

					View tempView1 = findViewForPosition(pos - 1);
					if (tempView1 != null) {
						Calendar prevcal = (Calendar) mCurrentCalDate.clone();
						prevcal.add(Calendar.DAY_OF_YEAR, -7);
						UpdateView(tempView1, prevcal, false);
					}

					View tempView2 = findViewForPosition(pos);
					displayListView(tempView2);

					tempView1 = findViewForPosition(pos + 1);
					if (tempView1 != null) {
						Calendar nextcal = (Calendar) mCurrentCalDate.clone();
						nextcal.add(Calendar.DAY_OF_YEAR, 7);
						UpdateView(tempView1, nextcal, false);
					}
				} else {
					View tempView1 = findViewForPosition(pos - 1);
					if (tempView1 != null) {
						Calendar prevcal = (Calendar) mCurrentCalDate.clone();
						prevcal.add(Calendar.DAY_OF_YEAR, -7);
						UpdateView(tempView1, prevcal, false);
					}
					if (pos != 3) {
						View tempView2 = findViewForPosition(pos);
						displayListView(tempView2);
					}
				}
			
			
				
				mCurrentposition = position;
				mOnViewRefreshListener.UpdateActionBar(
						mCurrentCalDate.get(Calendar.DATE),
						mCurrentCalDate.get(Calendar.MONTH),
						mCurrentCalDate.get(Calendar.YEAR));
				CalendarLog.d(CalendarConstants.Tag, "onPageSelected : ");

			}

			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {

			}

			public void onPageScrollStateChanged(int state) {
				CalendarLog.d(CalendarConstants.Tag,
						"onPageScrollStateChanged : " + state + ":"
								+ mCurrentposition);
				/*if(state==0)
				{

					if (mIsnextchanged) {

						View tempView1 = findViewForPosition(pos - 1);
						if (tempView1 != null) {
							Calendar prevcal = (Calendar) mCurrentCalDate.clone();
							prevcal.add(Calendar.DAY_OF_YEAR, -7);
							UpdateView(tempView1, prevcal, false);
						}

						View tempView2 = findViewForPosition(pos);
						displayListView(tempView2);

						tempView1 = findViewForPosition(pos + 1);
						if (tempView1 != null) {
							Calendar nextcal = (Calendar) mCurrentCalDate.clone();
							nextcal.add(Calendar.DAY_OF_YEAR, 7);
							UpdateView(tempView1, nextcal, false);
						}
					} else {
						View tempView1 = findViewForPosition(pos - 1);
						if (tempView1 != null) {
							Calendar prevcal = (Calendar) mCurrentCalDate.clone();
							prevcal.add(Calendar.DAY_OF_YEAR, -7);
							UpdateView(tempView1, prevcal, false);
						}
						if (pos != 3) {
							View tempView2 = findViewForPosition(pos);
							displayListView(tempView2);
						}
					}
				}*/
			}
		});
	}

	protected View findViewForPosition(int position) {
		Object object = mViews.get(position);
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

	@Override
	public Object instantiateItem(View container, int position) {
		LayoutInflater inflater = (LayoutInflater) container.getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		int pageId = mPageIDsArray[position];

		final View view = inflater.inflate(pageId, null);
		CalendarLog.d(CalendarConstants.Tag, "instantiateItem : " + position);

		if (position == 2) {
			
			Calendar nextcal = (Calendar) mCurrentCalDate.clone();
			nextcal.add(Calendar.DAY_OF_YEAR, -7);
			UpdateView(view, nextcal, false);
			
		} else if (position == 3) {
			displayListView(view);
		} else if (position == 4) {
			
			Calendar nextcal = (Calendar) mCurrentCalDate.clone();
			nextcal.add(Calendar.DAY_OF_YEAR, 7);
			UpdateView(view, nextcal, false);//		UpdateView(view, nextcal, false);
		}
		mViews.put(position, view);
		((ViewPager) container).addView(view, 0);
		return view;
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

		Calendar currentcal = mCurrentCalDate;
		UpdateView(loadListview, currentcal, true);

	}

	public static String getWeekDayOfDate(Calendar input) throws ParseException {

		Date dt1 = new Date();
		dt1.setTime(input.getTimeInMillis());
		String finalDay = "";
		// dt1 = format1.parse(input);
		DateFormat format2 = new SimpleDateFormat("EEE", Locale.US);
		finalDay = format2.format(dt1);
		finalDay = finalDay.toUpperCase(Locale.US);
		return finalDay;
	}

	public static String getWeekDayOfDate(Calendar input,int digits)
			throws ParseException {

		Date dt1 = new Date();
		dt1.setTime(input.getTimeInMillis());
		String finalDay = "";
		DateFormat format2 = new SimpleDateFormat("EEE", Locale.US);
		finalDay = format2.format(dt1);
		finalDay = finalDay.toUpperCase(Locale.US);
		finalDay = finalDay.substring(0, digits);
		return finalDay;
	}

	public void UpdateView(View loadListview, Calendar cal, boolean isTimeLine) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.US);
		int alldayCount = 0;
		int tempalldayCount = 0;
		if (loadListview != null) {
			mSecurePrefs = new SharedPreferences(Email.getAppContext());
			// PreferenceManager.getDefaultSharedPreferences(mContext);//
			String WeekStartsOn = mSecurePrefs.getString(
					mContext.getString(R.string.key_list_preference), "");
			Calendar firstCal = (Calendar) cal.clone();

			if (WeekStartsOn.equalsIgnoreCase("2")) {
				firstCal.setFirstDayOfWeek(Calendar.SATURDAY);
			} else if (WeekStartsOn.equalsIgnoreCase("3")) {
				firstCal.setFirstDayOfWeek(Calendar.SUNDAY);
			} else if (WeekStartsOn.equalsIgnoreCase("4")) {
				firstCal.setFirstDayOfWeek(Calendar.MONDAY);
			}

			int firstDayOfWeek = firstCal.getFirstDayOfWeek();
			int dayOfWeek = firstCal.get(Calendar.DAY_OF_WEEK);
			if (firstDayOfWeek == 7) {
				firstCal.add(Calendar.DAY_OF_WEEK, -dayOfWeek);
			} else
				firstCal.add(Calendar.DAY_OF_WEEK, firstCal.getFirstDayOfWeek()
						- firstCal.get(Calendar.DAY_OF_WEEK));

			Calendar secondCal = (Calendar) firstCal.clone();
			secondCal.add(Calendar.DAY_OF_YEAR, 1);

			Calendar thirdCal = (Calendar) firstCal.clone();
			thirdCal.add(Calendar.DAY_OF_YEAR, 2);

			Calendar fourthCal = (Calendar) firstCal.clone();
			fourthCal.add(Calendar.DAY_OF_YEAR, 3);

			Calendar fifthCal = (Calendar) firstCal.clone();
			fifthCal.add(Calendar.DAY_OF_YEAR, 4);

			Calendar sixthCal = (Calendar) firstCal.clone();
			sixthCal.add(Calendar.DAY_OF_YEAR, 5);

			Calendar lastCal = (Calendar) firstCal.clone();
			lastCal.add(Calendar.DAY_OF_YEAR, 6);

			ArrayList<ArrayList<ContentValues>> list = new ArrayList<ArrayList<ContentValues>>();
			if (isTimeLine) {
				list.add(CalendarDatabaseHelper.getDayListValues(sdf
						.format(firstCal.getTime())));
				list.add(CalendarDatabaseHelper.getDayListValues(sdf
						.format(secondCal.getTime())));
				list.add(CalendarDatabaseHelper.getDayListValues(sdf
						.format(thirdCal.getTime())));
				list.add(CalendarDatabaseHelper.getDayListValues(sdf
						.format(fourthCal.getTime())));
				list.add(CalendarDatabaseHelper.getDayListValues(sdf
						.format(fifthCal.getTime())));
				list.add(CalendarDatabaseHelper.getDayListValues(sdf
						.format(sixthCal.getTime())));
				list.add(CalendarDatabaseHelper.getDayListValues(sdf
						.format(lastCal.getTime())));
				
			
			}
			ArrayList<ArrayList<ContentValues>> allDayList = new ArrayList<ArrayList<ContentValues>>();
			if (isTimeLine) {
				ArrayList<ContentValues> allDayEvents = new ArrayList<ContentValues>();

				allDayEvents = CalendarDatabaseHelper.getAllDayListValues(sdf
						.format(firstCal.getTime()));
				ArrayList<ContentValues> tempEndValues = CalendarDatabaseHelper
						.getAllDayListValuesForEndDate(sdf.format(firstCal
								.getTime()));
				for (ContentValues tempEnd : tempEndValues) {
					tempEnd.put("startday", "1");
					allDayEvents.add(tempEnd);
				}
				tempalldayCount = allDayEvents.size();
				if (alldayCount <= tempalldayCount)
					alldayCount = tempalldayCount;
				allDayList.add(allDayEvents);

				allDayEvents = CalendarDatabaseHelper.getAllDayListValues(sdf
						.format(secondCal.getTime()));
				tempalldayCount = allDayEvents.size();
				if (alldayCount <= tempalldayCount)
					alldayCount = tempalldayCount;
				allDayList.add(allDayEvents);
				allDayEvents = CalendarDatabaseHelper.getAllDayListValues(sdf
						.format(thirdCal.getTime()));
				tempalldayCount = allDayEvents.size();
				if (alldayCount <= tempalldayCount)
					alldayCount = tempalldayCount;
				allDayList.add(allDayEvents);
				allDayEvents = CalendarDatabaseHelper.getAllDayListValues(sdf
						.format(fourthCal.getTime()));
				tempalldayCount = allDayEvents.size();
				if (alldayCount <= tempalldayCount)
					alldayCount = tempalldayCount;
				allDayList.add(allDayEvents);
				allDayEvents = CalendarDatabaseHelper.getAllDayListValues(sdf
						.format(fifthCal.getTime()));
				tempalldayCount = allDayEvents.size();
				if (alldayCount <= tempalldayCount)
					alldayCount = tempalldayCount;
				allDayList.add(allDayEvents);
				allDayEvents = CalendarDatabaseHelper.getAllDayListValues(sdf
						.format(sixthCal.getTime()));
				tempalldayCount = allDayEvents.size();
				if (alldayCount <= tempalldayCount)
					alldayCount = tempalldayCount;
				allDayList.add(allDayEvents);
				allDayEvents = CalendarDatabaseHelper.getAllDayListValues(sdf
						.format(lastCal.getTime()));
				tempalldayCount = allDayEvents.size();
				if (alldayCount <= tempalldayCount)
					alldayCount = tempalldayCount;
				allDayList.add(allDayEvents);
			}

			if (CalendarCommonFunction.isTablet(mContext) && isTimeLine) {
				TextView txt_weekview_header = (TextView) loadListview
						.findViewById(R.id.txt_weekview_header);
				String weekViewHeadervalue = Email.getWeekDays() + " "
						+ "WEEK" + mCurrentCalDate.get(Calendar.WEEK_OF_YEAR);

				txt_weekview_header.setText(weekViewHeadervalue);
				this.mOnCalendarViewSelectedListener
						.onDayViewScrolled(mCurrentCalDate);
			}

			String finalVal = " 12AM; ";
			Rect result = new Rect();
			mTextp.getTextBounds(finalVal, 0, finalVal.length(), result);
			int font_max_width = result.width();
			TextView weeknumber = (TextView) loadListview
					.findViewById(R.id.weeknumber);
			// weeknumber.setWidth(font_max_width+10);
			//Email.convertToDp(font_max_width)
			weeknumber.setLayoutParams(new LinearLayout.LayoutParams(
					(font_max_width-10), LayoutParams.WRAP_CONTENT));
			weeknumber.setText(String.valueOf(firstCal
					.get(Calendar.WEEK_OF_YEAR)));
			boolean showWeekNumber = mSecurePrefs.getBoolean(mContext
					.getString(R.string.key_show_week_checkbox_preference),
					false);
			if (showWeekNumber) {
				weeknumber.setVisibility(View.VISIBLE);
			} else {
				weeknumber.setVisibility(View.INVISIBLE);
			}

			TextView firstday = (TextView) loadListview
					.findViewById(R.id.weekfirstday);
			TextView secondday = (TextView) loadListview
					.findViewById(R.id.weeksecondday);
			TextView thirdday = (TextView) loadListview
					.findViewById(R.id.weekthirdday);
			TextView fourthday = (TextView) loadListview
					.findViewById(R.id.weekfourthday);
			TextView fifthday = (TextView) loadListview
					.findViewById(R.id.weekfifthday);
			TextView sixthday = (TextView) loadListview
					.findViewById(R.id.weeksixthday);
			TextView lastday = (TextView) loadListview
					.findViewById(R.id.weekseventhday);

			try {
				CalendarLog.d(CalendarConstants.Tag, "mScreenwidth"+mScreenwidth);
				int screenwidthCheck = mContext.getResources().getInteger(
						R.integer.week_day);
				if (mScreenwidth > screenwidthCheck) {
					String dayofweek = getWeekDayOfDate(firstCal);
					firstday.setText(dayofweek + " "
							+ firstCal.get(Calendar.DAY_OF_MONTH));

					secondday.setText(getWeekDayOfDate(secondCal) + " "
							+ secondCal.get(Calendar.DAY_OF_MONTH));
					thirdday.setText(getWeekDayOfDate(thirdCal) + " "
							+ thirdCal.get(Calendar.DAY_OF_MONTH));
					fourthday.setText(getWeekDayOfDate(fourthCal) + " "
							+ fourthCal.get(Calendar.DAY_OF_MONTH));
					fifthday.setText(getWeekDayOfDate(fifthCal) + " "
							+ fifthCal.get(Calendar.DAY_OF_MONTH));
					sixthday.setText(getWeekDayOfDate(sixthCal) + " "
							+ sixthCal.get(Calendar.DAY_OF_MONTH));
					lastday.setText(getWeekDayOfDate(lastCal) + " "
							+ lastCal.get(Calendar.DAY_OF_MONTH));
				} else {
					firstday.setText(getWeekDayOfDate(firstCal,1) + " "
							+ firstCal.get(Calendar.DAY_OF_MONTH));
					secondday.setText(getWeekDayOfDate(secondCal,1) + " "
							+ secondCal.get(Calendar.DAY_OF_MONTH));
					thirdday.setText(getWeekDayOfDate(thirdCal,1) + " "
							+ thirdCal.get(Calendar.DAY_OF_MONTH));
					fourthday.setText(getWeekDayOfDate(fourthCal,1) + " "
							+ fourthCal.get(Calendar.DAY_OF_MONTH));
					fifthday.setText(getWeekDayOfDate(fifthCal,1) + " "
							+ fifthCal.get(Calendar.DAY_OF_MONTH));
					sixthday.setText(getWeekDayOfDate(sixthCal,1) + " "
							+ sixthCal.get(Calendar.DAY_OF_MONTH));
					lastday.setText(getWeekDayOfDate(lastCal,1) + " "
							+ lastCal.get(Calendar.DAY_OF_MONTH));
				}
			} catch (Exception ex) {

			}

			ScrollView scrollView = (ScrollView) loadListview
					.findViewById(R.id.alldayweekview);

			if (alldayCount > 0) {
				if (scrollView.getChildCount() > 0)
					scrollView.removeAllViews();
//				int alldayheight = mContext.getResources().getInteger(
//						R.integer.event_all_day_height);

				int len = mContext.getResources().getInteger(
						R.integer.list_height);
				int layHeight = (len / 3) * alldayCount;
				
				if (layHeight > len) {
					layHeight = len;
					layHeight = layHeight - 15;
				}

				CalendarLog.d(CalendarConstants.Tag, "alldayCount"
						+ alldayCount);
				CalendarLog.d(CalendarConstants.Tag, "len"
						+ (len / 3));
				CalendarCustomAllDay customday = new CalendarCustomAllDay(
						mContext, allDayList, sdf.format(cal.getTime()),
						((len / 3) * alldayCount));

				LinearLayout.LayoutParams allparam = new LinearLayout.LayoutParams(
						LayoutParams.MATCH_PARENT, layHeight);
				scrollView.setLayoutParams(allparam);

				scrollView.addView(customday);
				scrollView.setVisibility(View.VISIBLE);
				customday.invalidate();
			} else {
				scrollView.setVisibility(View.GONE);
			}
			final com.cognizant.trumobi.calendar.view.CalendarCustomScrollView listView = (com.cognizant.trumobi.calendar.view.CalendarCustomScrollView) loadListview
					.findViewById(R.id.weekviewlist);
			// Assign adapter to ListView
			if (listView.getChildCount() > 0)
				listView.removeAllViews();
			final CalendarCustomWeekView view = new CalendarCustomWeekView(
					mContext, list, sdf.format(cal.getTime()));
			view.invalidate();
			listView.addView(view);
			if (isTimeLine) {
				Calendar todayday = Email.getNewCalendar();

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

				firstCal.add(Calendar.DAY_OF_WEEK, firstCal.getFirstDayOfWeek()
						- firstCal.get(Calendar.DAY_OF_WEEK));
				Calendar currentday = Email.getCurrentDay(sdf
						.format(cal.getTime()));
				if ((todayday.get(Calendar.MONTH) == currentday
						.get(Calendar.MONTH))
						&& (todayday.get(Calendar.YEAR) == currentday
								.get(Calendar.YEAR))) {
					if (firstCal.get(Calendar.DATE) <= currentday
							.get(Calendar.DATE)
							&& currentday.get(Calendar.DATE) <= lastCal
									.get(Calendar.DATE)) {
						todayday.set(Calendar.DATE,
								currentday.get(Calendar.DATE));
					}
				}

				String presentHours = todayday.get(Calendar.HOUR_OF_DAY) + ":"
						+ todayday.get(Calendar.MINUTE);
				final int couHours = view.convertTimetoSeconds(presentHours);
				final int len = mContext.getResources().getInteger(
						R.integer.list_height);

				Calendar presentCal = Email.getCurrentDay();
				if (todayday.get(Calendar.DATE) == presentCal
						.get(Calendar.DATE)
						&& todayday.get(Calendar.MONTH) == presentCal
								.get(Calendar.MONTH)
						&& todayday.get(Calendar.YEAR) == presentCal
								.get(Calendar.YEAR))
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
				else {
					/*listView.post(new Runnable() {
						@Override
						public void run() {
							listView.scrollTo(0, 0);
						}
					});*/
					final Calendar  newCal = Email.getNewCalendar();
					presentHours = newCal.get(Calendar.HOUR_OF_DAY) + ":"
							+ newCal.get(Calendar.MINUTE);
					final int couHour = view
							.convertTimetoSeconds(presentHours);
					listView.post(new Runnable() {
						@Override
						public void run() {
							int hours = 0;
							hours = couHour;
							if (hours > len)
								hours = hours - len;
							listView.scrollTo(0, hours);
						//	((CalendarMainActivity)mContext).setStartTime(String.valueOf(newCal.getTimeInMillis()));
						}
					});
					
				}

				listView.setOnScrollViewListener(new CalendarCustomScrollViewListener() {
					public void onScrollChanged(CalendarCustomScrollView v,
							int l, int t, int oldl, int oldt) {
						view.getTopYPosition = t + len;
					}
				});
			}
		}
	}

	private class Asyncparams 
	{
		View view;
		Calendar nextcal;
		boolean b;
		public Asyncparams(View view, Calendar nextcal, boolean b) {
			this.view = view;
			this.nextcal = nextcal;
			this.b = b;
		}
	}
	private class Asynclass1 extends AsyncTask<Void, Void, Void>
	{

		@Override
		protected Void doInBackground(Void... params) {
			LOG.e("Do in bG", "Asynclas1");
			return null;
		}

	
		
	}
	
//	private class Asynclass2 extends AsyncTask<Asyncparams, Void, Void>
//	{
//		
//	}
}
