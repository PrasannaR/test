package com.cognizant.trumobi.calendar.view;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cognizant.trumobi.R;

public class CalendarAdapter extends BaseAdapter {

	public GregorianCalendar pmonth; // calendar instance for previous month
	/**
	 * calendar instance for previous month for getting complete view
	 */
	public GregorianCalendar pmonthmaxset;
	private GregorianCalendar mSelectedDate;
	private Context mContext;
	private java.util.Calendar month;
	private int mFirstDay;
	private int mMaxWeeknumber;
	private int mMaxP;
	private int mCalMaxP;
	private int mMonthlength;
	private String mItemvalue, mCurrentDateString;
	private DateFormat mDateFormat;
	private ArrayList<String> mCalendarItems;
	public static List<String> sdayString;
	private View mPreviousView;

	public CalendarAdapter(Context c, GregorianCalendar monthCalendar) {
		CalendarAdapter.sdayString = new ArrayList<String>();
		Locale.setDefault(Locale.US);
		month = monthCalendar;
		mSelectedDate = (GregorianCalendar) monthCalendar.clone();
		mContext = c;
		month.set(GregorianCalendar.DAY_OF_MONTH, 1);
		this.mCalendarItems = new ArrayList<String>();
		mDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
		mCurrentDateString = mDateFormat.format(mSelectedDate.getTime());
		refreshDays();
	}

	public void setItems(ArrayList<String> items) {
		for (int i = 0; i != items.size(); i++) {
			if (items.get(i).length() == 1) {
				items.set(i, "0" + items.get(i));
			}
		}
		this.mCalendarItems = items;
	}

	public int getCount() {
		return sdayString.size();
	}

	public Object getItem(int position) {
		return sdayString.get(position);
	}

	public long getItemId(int position) {
		return 0;
	}

	// create a new view for each item referenced by the Adapter
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		TextView dayView;
		if (convertView == null) { // if it's not recycled, initialize some
									// attributes
			LayoutInflater vi = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.cal_item, null);

		}
		dayView = (TextView) v.findViewById(R.id.date);
		dayView.setHeight(50);
		// separates daystring into parts.
		String[] separatedTime = sdayString.get(position).split("-");
		// taking last part of date. ie; 2 from 2012-12-02
		String gridvalue = separatedTime[2].replaceFirst("^0*", "");
		// checking whether the day is in current month or not.
		if ((Integer.parseInt(gridvalue) > 1) && (position < mFirstDay)) {
			// setting offdays to white color.
			dayView.setTextColor(Color.WHITE);
			dayView.setClickable(false);
			dayView.setFocusable(false);
		} else if ((Integer.parseInt(gridvalue) < 7) && (position > 28)) {
			dayView.setTextColor(Color.WHITE);
			dayView.setClickable(false);
			dayView.setFocusable(false);
		} else {
			// setting curent month's days in blue color.
			dayView.setTextColor(Color.BLACK);
		}

		if (sdayString.get(position).equals(mCurrentDateString)) {
			setSelected(v);
			mPreviousView = v;
		} else {
			// v.setBackgroundColor(Color.WHITE);
			v.setBackgroundResource(R.drawable.cal_list_item_background);
		}
		dayView.setText(gridvalue);

		// create date string for comparison
		String date = sdayString.get(position);

		if (date.length() == 1) {
			date = "0" + date;
		}
		String monthStr = "" + (month.get(GregorianCalendar.MONTH) + 1);
		if (monthStr.length() == 1) {
			monthStr = "0" + monthStr;
		}

		// show icon if date is not empty and it exists in the items array
		ImageView iw = (ImageView) v.findViewById(R.id.date_icon);
		if (date.length() > 0 && mCalendarItems != null && mCalendarItems.contains(date)) {
			iw.setVisibility(View.VISIBLE);
		} else {
			iw.setVisibility(View.INVISIBLE);
		}
		return v;
	}

	public View setSelected(View view) {
		if (mPreviousView != null) {
			mPreviousView
					.setBackgroundResource(R.drawable.cal_list_item_background);
		}
		mPreviousView = view;
		view.setBackgroundResource(R.drawable.cal_cel_selectl);
		return view;
	}

	public void refreshDays() {
		// clear items
		mCalendarItems.clear();
		sdayString.clear();
		Locale.setDefault(Locale.US);
		pmonth = (GregorianCalendar) month.clone();
		// month start day. ie; sun, mon, etc
		mFirstDay = month.get(GregorianCalendar.DAY_OF_WEEK);
		// finding number of weeks in current month.
		mMaxWeeknumber = month.getActualMaximum(GregorianCalendar.WEEK_OF_MONTH);
		// allocating maximum row number for the gridview.
		mMonthlength = mMaxWeeknumber * 7;
		mMaxP = getMaxP(); // previous month maximum day 31,30....
		mCalMaxP = mMaxP - (mFirstDay - 1);// calendar offday starting 24,25 ...
		/**
		 * Calendar instance for getting a complete gridview including the three
		 * month's (previous,current,next) dates.
		 */
		pmonthmaxset = (GregorianCalendar) pmonth.clone();
		/**
		 * setting the start date as previous month's required date.
		 */
		pmonthmaxset.set(GregorianCalendar.DAY_OF_MONTH, mCalMaxP + 1);

		/**
		 * filling calendar gridview.
		 */
		for (int n = 0; n < mMonthlength; n++) {

			mItemvalue = mDateFormat.format(pmonthmaxset.getTime());
			pmonthmaxset.add(GregorianCalendar.DATE, 1);
			sdayString.add(mItemvalue);

		}
	}

	private int getMaxP() {
		int maxP;
		if (month.get(GregorianCalendar.MONTH) == month
				.getActualMinimum(GregorianCalendar.MONTH)) {
			pmonth.set((month.get(GregorianCalendar.YEAR) - 1),
					month.getActualMaximum(GregorianCalendar.MONTH), 1);
		} else {
			pmonth.set(GregorianCalendar.MONTH,
					month.get(GregorianCalendar.MONTH) - 1);
		}
		maxP = pmonth.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);

		return maxP;
	}

}