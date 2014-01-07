package com.cognizant.trumobi.calendar.view;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class CalendarDayViewListAdapter extends BaseAdapter {
	
	//int Height;
	private Context mContext;
	private String mPresentday;
	private ArrayList<ContentValues> mDayEventValues;

	public CalendarDayViewListAdapter(Context context,
			ArrayList<ContentValues> dayEventValues, String presentday) {
		super();
	
		//Height = 90;
		this.mContext = context;
		this.mDayEventValues = dayEventValues;
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
	public View getView(int index, View convertView, ViewGroup parent) {
		convertView = new CalendarCustomDayView(mContext, mDayEventValues,
				mPresentday);
		convertView.invalidate();
		return convertView;
	}

}
