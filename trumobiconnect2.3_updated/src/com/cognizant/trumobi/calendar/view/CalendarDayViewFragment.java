package com.cognizant.trumobi.calendar.view;

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


public class CalendarDayViewFragment extends Fragment {
	private ViewPager mViewPager;
	private OnViewRefreshListener mOnViewRefreshListener;
	private OnCalendarViewSelectedListener mOnCalendarViewSelectedListener;

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

		View view = inflater.inflate(
				R.layout.cal_activity_view_pager_horizontal_swipe, container,
				false);
		init(view);
		return view;
	}

	
	private void init(View view) {
		mViewPager = (ViewPager) view.findViewById(R.id.myfivepanelpager);
		String StartTime=((CalendarMainActivity) getActivity()).GetStartTime();
		CalendarDayViewPagerAdapter adapter = new CalendarDayViewPagerAdapter(
				getActivity(), mViewPager, new int[] { R.layout.cal_dayview,
						R.layout.cal_dayview, R.layout.cal_dayview },
				mOnViewRefreshListener, mOnCalendarViewSelectedListener,StartTime);
		mViewPager.setAdapter(adapter);
		mViewPager.setOffscreenPageLimit(3);
		mViewPager.setCurrentItem(3);
		if (Email.isTodayEventFlag()) {
			if (Email.isPageScrollLeft()) {
				mViewPager.setAnimation(CalendarCommonFunction
						.inFromRightAnimation());
			} else {
				mViewPager.setAnimation(CalendarCommonFunction
						.inFromLeftAnimation());
			}
		}

	}

	// Container Activity must implement this interface
	public interface OnViewSelectedListener {

	}

	public interface OnViewRefreshListener {
		public void onRefreshActionBar(int position);

	}

	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mOnViewRefreshListener = (OnViewRefreshListener) activity;
			mOnCalendarViewSelectedListener = (OnCalendarViewSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnURLSelectedListener");
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

}
