package com.cognizant.trumobi.calendar.view;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.calendar.util.CalendarCommonFunction;
import com.cognizant.trumobi.calendar.view.CalendarView.OnCalendarViewSelectedListener;
import com.cognizant.trumobi.em.Email;


public class CalendarWeekViewFragment extends Fragment {
	private ViewPager mViewPager;
	private OnViewWeekRefreshListener mOnViewRefreshListener;
	private OnCalendarViewSelectedListener mOnCalendarViewSelectedListener;

	Calendar currentDate;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if (container == null) {
			return null;
		}
		if (currentDate == null) {
			String string = Email.getPresentDay();
			Date date = null;
			try {
				date = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
						.parse(string);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			currentDate = Calendar.getInstance();
			currentDate.setTime(date);
		}
		View view = inflater.inflate(
				R.layout.cal_activity_view_pager_horizontal_swipe, container,
				false);
		init(view);
		return view;
	}

	private void init(View view) {

		mViewPager = (ViewPager) view.findViewById(R.id.myfivepanelpager);

		CalendarWeekPagerAdapter adapter = new CalendarWeekPagerAdapter(
				getActivity(), mViewPager, new int[] { R.layout.cal_weekview,
						R.layout.cal_weekview, R.layout.cal_weekview },
				mOnViewRefreshListener, mOnCalendarViewSelectedListener,
				currentDate);

		mViewPager.setAdapter(adapter);
		mViewPager.setOffscreenPageLimit(3);
		mViewPager.setCurrentItem(3);
	
		if (Email.isTodayEventFlag()) {
			if (Email.isPageScrollLeft()) {
				mViewPager.setAnimation(CalendarCommonFunction
						.inFromLeftAnimation());
			} else {
				mViewPager.setAnimation(CalendarCommonFunction
						.inFromRightAnimation());
			}
			Email.setTodayEventFlag(false);
		
		}

	}

	public interface OnViewWeekSelectedListener {

	}

	public interface OnViewWeekRefreshListener {
		public void UpdateActionBar(int day, int month, int Year);

	}

	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mOnViewRefreshListener = (OnViewWeekRefreshListener) activity;
			mOnCalendarViewSelectedListener = (OnCalendarViewSelectedListener) activity;

		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnURLSelectedListener");
		}

	}

}
