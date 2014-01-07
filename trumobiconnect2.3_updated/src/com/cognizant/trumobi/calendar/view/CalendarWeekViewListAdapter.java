package com.cognizant.trumobi.calendar.view;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class CalendarWeekViewListAdapter extends BaseAdapter{
	
	private Context mContext;
	private String mPresentday;
	
	private ArrayList<ArrayList<ContentValues>> weekEventValues;

	// private OnAgendaViewSelectedListener mAgendaViewSelectedListener;

	public CalendarWeekViewListAdapter(Context context,
			ArrayList<ArrayList<ContentValues>> list, String presentday) {
		mContext = context;
		this.weekEventValues = list;
		//this.mAgendaViewSelectedListener = mOnAgendaViewSelectedListener;
		this.mPresentday = presentday;
	}

	@Override
	public int getCount() {
		return 1;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int index, View view, ViewGroup parent) {
//		if (view == null) {
			view = new CalendarCustomWeekView(mContext,weekEventValues,mPresentday);
			view.invalidate();
//		}
		return view;
	}
}
