package com.cognizant.trumobi.calendar.agendaView;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.calendar.provider.CalendarDatabaseHelper;
import com.cognizant.trumobi.calendar.util.CalendarCommonFunction;
import com.cognizant.trumobi.calendar.util.CalendarConstants;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.log.CalendarLog;


public class CalendarAgendaFragment extends Fragment {

	private ArrayList<ArrayList<ContentValues>> mValue = new ArrayList<ArrayList<ContentValues>>();
	private CalendarListSectionedAdapter mSectionedAdapter;
	private CalendarPinnedHeaderListView mListView;
	private String compareToday,comparePresentDay;
	private int todayItem, presentday;
	// private OnAgendaViewSelectedListener mAgendViewListener;
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			// mAgendViewListener = (OnAgendaViewSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnAgendaView Listener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if (container == null) {
			return null;
		}

		View view = inflater
				.inflate(R.layout.cal_agenda_main, container, false);
		init(view);
		return view;
	}

	private void init(View view) {
		mListView = (CalendarPinnedHeaderListView) view
				.findViewById(R.id.pinnedListView);
		LayoutInflater inflator = (LayoutInflater) getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout header1 = (LinearLayout) inflator.inflate(
				R.layout.cal_header, null);
		CalendarCommonFunction.getCurrentDDMMYY();

		((TextView) header1.findViewById(R.id.txt_header))
				.setText(CalendarConstants.VIEW_EVENTS_BEFORE + " "
						+ CalendarCommonFunction.setTextpreviousMonthEvent());
		((TextView) header1.findViewById(R.id.txt_header))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						((TextView) v.findViewById(R.id.txt_header))
								.setText(CalendarConstants.VIEW_EVENTS_BEFORE
										+ " "
										+ CalendarCommonFunction
												.setTextpreviousMonthEvent());
						mSectionedAdapter.mAgendaListValues = CalendarDatabaseHelper
								.getEventListAgenda(
										CalendarCommonFunction.sFromDate,
										CalendarCommonFunction.sToDate);
						mSectionedAdapter.notifyDataSetChanged();
					}
				});

		LinearLayout footer = (LinearLayout) inflator.inflate(
				R.layout.cal_header, null);
		((TextView) footer.findViewById(R.id.txt_header))
				.setText(CalendarConstants.EVENT_AFTER);
		((TextView) footer.findViewById(R.id.txt_header))
				.setText(CalendarConstants.VIEW_EVENTS_AFTER + " "
						+ CalendarCommonFunction.setTextNewEvent());
		// Used To search Event
		if ((Email.isSearchFlag())
				&& (!Email.getSearchQueryString().equals(""))) {
			mValue = CalendarDatabaseHelper
					.getSearchEventListAgenda(Email
							.getSearchQueryString());
			Email.setSearchFlag(false);
		} else {
			mValue = CalendarDatabaseHelper.getEventListAgenda(
					CalendarCommonFunction.sFromDate,
					CalendarCommonFunction.sToDate);
		}

		((TextView) footer.findViewById(R.id.txt_header))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						((TextView) v.findViewById(R.id.txt_header))
								.setText(CalendarConstants.VIEW_EVENTS_AFTER
										+ " "
										+ CalendarCommonFunction
												.setTextNewEvent());
						mSectionedAdapter.mAgendaListValues = CalendarDatabaseHelper
								.getEventListAgenda(
										CalendarCommonFunction.sFromDate,
										CalendarCommonFunction.sToDate);
						mSectionedAdapter.notifyDataSetChanged();
					}
				});
		mListView.addHeaderView(header1);
		mListView.addFooterView(footer);
		mSectionedAdapter = new CalendarListSectionedAdapter(getActivity(),
				mValue);
		mListView.setAdapter(mSectionedAdapter);
		getActivity().runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				getTodayDay();
				getPresentDay();
			}
		});
		
		// End of if
		mSectionedAdapter.notifyDataSetChanged();
	}

	private void getPresentDay() {
		
		// Get Present Day
		
		String[] arr_PresentDay = Email.getPresentDay().split(" ");
		Date date;
		try {
				date = new SimpleDateFormat("MMM", Locale.ENGLISH).parse(arr_PresentDay[1]);
				Calendar cal = Calendar.getInstance();
			    cal.setTime(date);
			    int month = cal.get(Calendar.MONTH);
			    if(arr_PresentDay[0].length()==1)
			    {
			    	arr_PresentDay[0] = "0"+arr_PresentDay[0];
			    }
				comparePresentDay = new DateFormatSymbols().getMonths()[month]+" "+arr_PresentDay[0];
				
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (int i = 0; i < mListView.getCount(); i++) {
			ViewGroup view1 = (ViewGroup) mListView.getAdapter().getView(i,
					null, mListView);
			
			//LinearLayout layout = (LinearLayout)view1;
			//if(view1.getChildCount())
			if (view1.getChildCount() == 3) {
				TextView textDate = (TextView) view1.getChildAt(2);
				CalendarLog.e("Today Event", "Inside if Flag...."+textDate
						.getText()
						.toString());
				if(textDate
						.getText()
						.toString()
						.equals(comparePresentDay))
				{
					presentday = i;
					break;
				}
			}

		}
		
		mListView.setSelection(presentday);
		
		
		
	}

	private void getTodayDay() {
		// Get Today
		DateFormatSymbols dateFormatSymbol = new DateFormatSymbols();
		String monthName = dateFormatSymbol.getMonths()[Email.todayMonth - 1];
	   
		if(Email.todayDate<10)
		{
			String todayDate = "0"+Email.todayDate;
			compareToday = monthName+" "+todayDate;
		}
		else
		{
			compareToday = monthName+" "+Email.todayDate;			
		}
		for (int i = 0; i < mListView.getCount(); i++) {
			ViewGroup view1 = (ViewGroup) mListView.getAdapter().getView(i,
					null, mListView);
			
			//LinearLayout layout = (LinearLayout)view1;
			//if(view1.getChildCount())
			if (view1.getChildCount() == 3) {
				TextView textDate = (TextView) view1.getChildAt(2);
				CalendarLog.e("Today Event", "Inside if Flag...."+textDate
						.getText()
						.toString());
				if (textDate
						.getText()
						.toString()
						.equals(compareToday)) {

						todayItem = i;
					
						//mListView.startAnimation(CalendarCommonFunction.inFromBottomToTopAnimation());
						break;
				}
			}

		}
		
		if (Email.isTodayEventFlag()) {
			
			mListView.setSelection(todayItem);
			Email.setTodayEventFlag(false);
		}
		
	}
	
	@Override
	public void onDestroy() {

		super.onDestroy();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	
}
