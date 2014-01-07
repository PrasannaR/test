package com.cognizant.trumobi.calendar.view;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.calendar.provider.CalendarDatabaseHelper;
import com.cognizant.trumobi.em.Email;


public class CalendarView extends Fragment {

	public GregorianCalendar month, itemmonth;// calendar instances.

	public GregorianCalendar week, itemweek;
	public GregorianCalendar year, itemyear;

	public CalendarAdapter adapter;// adapter instance
	public Handler handler;// for grabbing some event values for showing the dot
							// marker.
	public ArrayList<String> items; // container to store calendar items which
									// needs showing the event marker

	private View mCalendarView;
	private int mPreviousPosition;
	private GridView mGridview;
	private ListView mListViewAccountOwner;
	private OnCalendarViewSelectedListener mObjCalendarViewSelectedListener;
	private boolean mSelectedViewflag = false;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		try {
			mObjCalendarViewSelectedListener = (OnCalendarViewSelectedListener) activity;

		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnURLSelectedListener");
		}
	}

	// Container Activity must implement this interface
	public interface OnCalendarViewSelectedListener {
		public void onDayViewScrolled(Calendar cal);

		public void updateViewOnCalendarSelection();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		super.onCreate(savedInstanceState);
		mSelectedViewflag = false;
		mCalendarView = inflater.inflate(R.layout.cal_calendar, null);
		Locale.setDefault(Locale.US);

		// rLayout = (LinearLayout) calendarView.findViewById(R.id.text);
		month = (GregorianCalendar) GregorianCalendar.getInstance();
		itemmonth = (GregorianCalendar) month.clone();

		items = new ArrayList<String>();
		adapter = new CalendarAdapter(getActivity(), month);

		mGridview = (GridView) mCalendarView.findViewById(R.id.gridview);
		mGridview.setAdapter(adapter);
		Email.setCalenderAdapter(adapter);

		mListViewAccountOwner = (ListView) mCalendarView
				.findViewById(R.id.list_account_owner);
		ArrayList<ContentValues> list_of_owner_account = CalendarDatabaseHelper
				.getListOfAccountOwner();
		CalendarCustomAccountOwnerListAdapter listAdapter = new CalendarCustomAccountOwnerListAdapter(
				getActivity(), list_of_owner_account);
		mListViewAccountOwner.setAdapter(listAdapter);
		//
		handler = new Handler();
		handler.post(calendarUpdater);

		TextView title = (TextView) mCalendarView.findViewById(R.id.title);
		title.setText(android.text.format.DateFormat.format("MMMM yyyy", month));

		RelativeLayout previous = (RelativeLayout) mCalendarView
				.findViewById(R.id.previous);

		previous.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setPreviousMonthOnArrayClick();
				refreshCalendarOnArrayClick();
			}
		});

		RelativeLayout next = (RelativeLayout) mCalendarView
				.findViewById(R.id.next);
		next.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setNextMonthOnArrayClick();
				refreshCalendarOnArrayClick();

			}
		});

		mGridview.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {

				((CalendarAdapter) parent.getAdapter()).setSelected(v);

				String selectedGridDate = CalendarAdapter.sdayString
						.get(position);
				String[] separatedTime = selectedGridDate.split("-");
				String gridvalueString = separatedTime[2].replaceFirst("^0*",
						"");// taking last part of date. ie; 2 from 2012-12-02.
				int gridvalue = Integer.parseInt(gridvalueString);
				// navigate to next or previous month on clicking offdays.
				if ((gridvalue > 10) && (position < 8)) {
					// setPreviousMonth();
					// refreshCalendar();
				} else if ((gridvalue < 7) && (position > 28)) {
					// setNextMonth();
					// refreshCalendar();
				}
				String newDate[] = selectedGridDate.split("-");
				((CalendarAdapter) parent.getAdapter()).setSelected(v);
				String newDateString = newDate[2]
						+ " "
						+ Email.getNameOFMonth(Integer
								.parseInt(newDate[1])) + " " + newDate[0];
				Email.setPresentDate(newDateString);
				Email.todayDate = Integer.parseInt(newDate[2]);
				//Email.currentdatevalue = Integer.parseInt(newDate[2]);
				Email.todayMonth = Integer.parseInt(newDate[1]);
				Email.todayYear = Integer.parseInt(newDate[0]);
				mObjCalendarViewSelectedListener
						.updateViewOnCalendarSelection();
			}
		});

		Email.setCalenderView(mCalendarView);

		return mCalendarView;
	}

	public void setSelectionGridView(Context c, Calendar cal) {
		mSelectedViewflag = true;
		GregorianCalendar cale = (GregorianCalendar) cal.clone();
		if (Email.getCalenderView() != null) {
			mGridview = (GridView) Email.getCalenderView()
					.findViewById(R.id.gridview);
			adapter = new CalendarAdapter(c, cale);
			itemmonth = cale;
			mGridview.setAdapter(adapter);

			String selectedGridDate = Email.getPresentDay();
			try {

				String[] separatedTime = selectedGridDate.split(" ");
				String gridvalueString = separatedTime[2].replaceFirst("^0*",
						"");// taking last part of date. ie; 2 from 2012-12-02.
				int gridvalue = Integer.parseInt(gridvalueString);
				// navigate to next or previous month on clicking offdays.
				if ((gridvalue > 10) && ((mPreviousPosition) < 8)) {

					setPreviousMonth(cale);
					refreshCalendar(cale, adapter);
				} else if ((gridvalue < 7) && ((mPreviousPosition) > 28)) {
					setNextMonth(cale);
					refreshCalendar(cale, adapter);
				}

			} catch (ArrayIndexOutOfBoundsException exception) {
				exception.printStackTrace();
			}

		}

	}

	protected void setNextMonth(Calendar cal) {
		month = (GregorianCalendar) cal.clone();
		if (month.get(Calendar.MONTH) == month.getActualMaximum(Calendar.MONTH)) {
			month.set((month.get(Calendar.YEAR) + 1),
					month.getActualMinimum(Calendar.MONTH), 1);
		} else {
			month.set(Calendar.MONTH, month.get(Calendar.MONTH) + 1);
		}
	}

	protected void setPreviousMonth(Calendar cal) {
		month = (GregorianCalendar) cal.clone();
		if (month.get(Calendar.MONTH) == month.getActualMinimum(Calendar.MONTH)) {
			month.set((month.get(Calendar.YEAR) - 1),
					month.getActualMaximum(Calendar.MONTH), 1);
		} else {
			month.set(Calendar.MONTH, month.get(Calendar.MONTH) - 1);
		}

	}

	public void refreshCalendar(GregorianCalendar monthCal,
			CalendarAdapter adapter) {
		TextView title = (TextView) Email.getCalenderView()
				.findViewById(R.id.title);
		adapter.refreshDays();
		adapter.notifyDataSetChanged();
		handler = new Handler();
		handler.post(calendarUpdater); // generate some calendar items
		title.setText(android.text.format.DateFormat.format("MMMM yyyy",
				monthCal));
	}

	public Runnable calendarUpdater = new Runnable() {
		@Override
		public void run() {
			if (items != null) {
				items.clear();
				// Print dates of the current week
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				String itemvalue;
				for (int i = 0; i < 7; i++) {
					itemvalue = df.format(itemmonth.getTime());
					itemmonth.add(Calendar.DATE, 1);
					items.add("2012-09-12");
					items.add("2012-10-07");
					items.add("2012-10-15");
					items.add("2012-10-20");
					items.add("2012-11-30");
					items.add("2012-11-28");
				}

				adapter.setItems(items);
				adapter.notifyDataSetChanged();
			}
		}

	};

	public Runnable calendarUpdater1 = new Runnable() {
		@Override
		public void run() {
			if (items != null) {

				items.clear();
				// Print dates of the current week
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				String itemvalue;
				for (int i = 0; i < 7; i++) {
					itemvalue = df.format(itemmonth.getTime());
					itemmonth.add(GregorianCalendar.DATE, 1);
					items.add("2012-09-12");
					items.add("2012-10-07");
					items.add("2012-10-15");
					items.add("2012-10-20");
					items.add("2012-11-30");
					items.add("2012-11-28");
				}

				adapter.setItems(items);
				adapter.notifyDataSetChanged();
			}
		}

	};

	public void refreshCalendarOnArrayClick() {
		TextView title = (TextView) mCalendarView.findViewById(R.id.title);
		itemmonth = (GregorianCalendar) month.clone();
		adapter.refreshDays();
		adapter.notifyDataSetChanged();

		handler.post(calendarUpdater1); // generate some calendar items

		title.setText(android.text.format.DateFormat.format("MMMM yyyy", month));
		mGridview.setAdapter(adapter);
	}

	protected void setNextMonthOnArrayClick() {
		if (month.get(GregorianCalendar.MONTH) == month
				.getActualMaximum(GregorianCalendar.MONTH)) {
			month.set((month.get(GregorianCalendar.YEAR) + 1),
					month.getActualMinimum(GregorianCalendar.MONTH), 1);
		} else {
			month.set(GregorianCalendar.MONTH,
					month.get(GregorianCalendar.MONTH) + 1);
		}

	}

	protected void setPreviousMonthOnArrayClick() {
		if (month.get(GregorianCalendar.MONTH) == month
				.getActualMinimum(GregorianCalendar.MONTH)) {
			month.set((month.get(GregorianCalendar.YEAR) - 1),
					month.getActualMaximum(GregorianCalendar.MONTH), 1);
		} else {
			month.set(GregorianCalendar.MONTH,
					month.get(GregorianCalendar.MONTH) - 1);
		}

	}
}
