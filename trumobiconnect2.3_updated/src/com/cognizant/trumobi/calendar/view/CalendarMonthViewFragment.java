package com.cognizant.trumobi.calendar.view;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.ContentValues;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.calendar.provider.CalendarDatabaseHelper;
import com.cognizant.trumobi.calendar.util.CalendarCommonFunction;
import com.cognizant.trumobi.em.Email;


public class CalendarMonthViewFragment extends Fragment {

	private ArrayList<ArrayList<ContentValues>> mCurrentEvent;
	private ArrayList<ArrayList<ContentValues>> mPreviousEvent;
	private ArrayList<ArrayList<ContentValues>> mFurtureEvent;
	private GridView mMonthView;

	private Calendar mCalendar;
	private int mToday[] = new int[3];

	private int count = 0;
	private OnMonthListener onMonthListener;

	private TextView mTxt_monthViewHeader;
	private View v;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		v = inflater.inflate(R.layout.cal_month_view, container, false);
		if (CalendarCommonFunction.isTablet(getActivity()
				.getApplicationContext())) {
			mTxt_monthViewHeader = (TextView) v
					.findViewById(R.id.txt_monthview_header);
			mTxt_monthViewHeader.setText(CalendarCommonFunction
					.getNameOFMonth(Email.todayMonth)
					+ " "
					+ Email.todayYear);
		}
//		mMonthView = (GridView) v.findViewById(R.id.gridview1);
//
//		String string = Email.getPresentDay();
//		Date currentDate = null;
//		try {
//			currentDate = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
//					.parse(string);
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
//		mCalendar = Email.getNewCalendar();
//		mCalendar.setTime(currentDate);
//		mToday[0] = mCalendar.get(Calendar.DAY_OF_MONTH);
//		mToday[1] = mCalendar.get(Calendar.MONTH); // zero based
//		mToday[2] = mCalendar.get(Calendar.YEAR);
//
//		mMonthView.setOnItemClickListener(new OnItemClickListener() {
//
//			@Override
//			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
//					long arg3) {
//
//			}
//		});

		// get display metrics
//		final DisplayMetrics metrics = new DisplayMetrics();
//		getActivity().getWindowManager().getDefaultDisplay()
//				.getMetrics(metrics);
//
//		final String date = "1" + " " + getMonth(mToday[1]) + " " + ""
//				+ mToday[2];// ""+mToday[0]+" "+getMonth(mToday[1])+" "+""+mToday[2];
//		mCurrentEvent = CalendarDatabaseHelper.getMonthEventList(date);
//		mMonthView.setAdapter(new CalendarMonthAdapter(getActivity(),
//				mToday[1], mToday[2], metrics, mCurrentEvent, onMonthListener) {
//
//			@Override
//			protected void onDate(int[] date, int position, View item) {
//
//			}
//		});

//		new Thread() {
//			public void run() {
//				mCurrentEvent = CalendarDatabaseHelper.getMonthEventList(date);
//				mMonthView.setAdapter(new CalendarMonthAdapter(getActivity(),
//						mToday[1], mToday[2], metrics, mCurrentEvent,
//						onMonthListener) {
//
//					@Override
//					protected void onDate(int[] date, int position, View item) {
//
//					}
//				});
//				handler.sendEmptyMessage(0);
//
//			}
//
//		}.start();
		mMonthView.setSmoothScrollbarEnabled(true);
		mMonthView.setVerticalScrollBarEnabled(false);
		count = 0;
		mMonthView.setSelected(true);
		mMonthView.setSelection(CalendarMonthAdapter.getPreviousdates());

		if (Email.isTodayEventFlag()) {
			if (Email.isPageScrollLeft()) {
				mMonthView.startAnimation(CalendarCommonFunction
						.inFromTopToBottomAnimation());
				Email.setPageScrollLeft(false);
			} else {
				mMonthView.startAnimation(CalendarCommonFunction
						.inFromBottomToTopAnimation());
			}
			Email.setTodayEventFlag(false);

		}

//		mMonthView.setOnScrollListener(new OnScrollListener() {
//			@Override
//			public void onScroll(AbsListView view, int firstVisibleItem,
//					int visibleItemCount, int totalItemCount) {
//
//				int currentFirstVisPos = view.getFirstVisiblePosition();
//				if (count <= 1) {
//					count++;
//					// isfirst = false;
//					return;
//				}
//
//			}
//
//			@Override
//			public void onScrollStateChanged(AbsListView arg0, int arg1) {
//				int currentFirstVisPos = mMonthView.getFirstVisiblePosition();
//				if (currentFirstVisPos >= 91) {
//					// Log.e("Test","Move down");
//					mCalendar.add(Calendar.MONTH, 1);
//					int nextMonth = mCalendar.get(Calendar.MONTH);
//					int nextYear = mCalendar.get(Calendar.YEAR);
//					String date = "1" + " " + getMonth(nextMonth) + " " + ""
//							+ nextYear;
//					Email.todayDate = mCalendar.get(Calendar.DATE);
//					Email.currentdatevalue = mCalendar
//							.get(Calendar.DATE);
//					Email.todayMonth = nextMonth + 1;
//					Email.todayYear = nextYear;
//
//					String presentDay = Email.currentdatevalue
//							+ " "
//							+ Email
//									.getNameOFMonth(Email.todayMonth)
//							+ " " + Email.todayYear;
//					Email.setPresentDate(presentDay);
//					((CalendarMainActivity) getActivity())
//							.updateMonth(getMonth(nextMonth) + " " + nextYear);
//					mFurtureEvent = CalendarDatabaseHelper
//							.getMonthEventList(date);
//					mMonthView.setAdapter(new CalendarMonthAdapter(
//							getActivity(), nextMonth, nextYear, metrics,
//							mFurtureEvent, onMonthListener) {
//
//						@Override
//						protected void onDate(int[] date, int position,
//								View item) {
//
//						}
//					});
//					count = 0;
//					mMonthView.setSmoothScrollbarEnabled(true);
//					final Animation animationFadeIn = AnimationUtils
//							.loadAnimation(getActivity(), R.anim.cal_fadein);
//					mMonthView.startAnimation(animationFadeIn);
//					mMonthView.setSelection(CalendarMonthAdapter
//							.getPreviousdates());
//
//					Email.setPageScrollLeft(true);
//
//					return;
//				} else if (currentFirstVisPos <= 21) {
//
//					// Log.e("Test","Move UP");
//					mCalendar.add(Calendar.MONTH, -1);
//					int preMonth = mCalendar.get(Calendar.MONTH);
//
//					int preYear = mCalendar.get(Calendar.YEAR);
//					String date = "1" + " " + getMonth(preMonth) + " " + ""
//							+ preYear;
//					Email.todayDate = mCalendar.get(Calendar.DATE);
//					Email.currentdatevalue = mCalendar
//							.get(Calendar.DATE);
//					Email.todayMonth = preMonth + 1;
//					Email.todayYear = preYear;
//					String presentDay = Email.currentdatevalue
//							+ " "
//							+ Email
//									.getNameOFMonth(Email.todayMonth)
//							+ " " + Email.todayYear;
//					Email.setPresentDate(presentDay);
//					((CalendarMainActivity) getActivity())
//							.updateMonth(getMonth(preMonth) + " " + preYear);
//					mPreviousEvent = CalendarDatabaseHelper
//							.getMonthEventList(date);
//					mMonthView.setAdapter(new CalendarMonthAdapter(
//							getActivity(), preMonth, preYear, metrics,
//							mPreviousEvent, onMonthListener) {
//
//						@Override
//						protected void onDate(int[] date, int position,
//								View item) {
//
//						}
//					});
//					count = 0;
//					final Animation animationFadeIn = AnimationUtils
//							.loadAnimation(getActivity(), R.anim.cal_fadein);
//					mMonthView.startAnimation(animationFadeIn);
//					mMonthView.setSelection(CalendarMonthAdapter
//							.getPreviousdates());
//
//					return;
//				}
//
//				if (CalendarCommonFunction.isTablet(getActivity()
//						.getApplicationContext())) {
//					mTxt_monthViewHeader = (TextView) v
//							.findViewById(R.id.txt_monthview_header);
//					mTxt_monthViewHeader.setText(CalendarCommonFunction
//							.getNameOFMonth(Email.todayMonth)
//							+ " "
//							+ Email.todayYear);
//				}
//			}
//
//		});
		return v;
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			super.handleMessage(msg);

		}

	};

	private String getMonth(int day) {
		switch (day) {
		case 0:
			return "Jan";
		case 1:
			return "Feb";
		case 2:
			return "Mar";
		case 3:
			return "Apr";
		case 4:
			return "May";
		case 5:
			return "Jun";
		case 6:
			return "Jul";
		case 7:
			return "Aug";
		case 8:
			return "Sep";
		case 9:
			return "Oct";
		case 10:
			return "Nov";
		case 11:
			return "Dec";
		default:
			return "Jan";
		}

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	public interface OnMonthListener {
		public void callMonth(int day, int month, int year);

	}

	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			onMonthListener = (OnMonthListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnMonthListener");
		}

	}
}
