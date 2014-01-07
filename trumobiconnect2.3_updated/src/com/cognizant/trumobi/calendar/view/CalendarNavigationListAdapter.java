package com.cognizant.trumobi.calendar.view;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.calendar.util.CalendarCommonFunction;

public class CalendarNavigationListAdapter extends BaseAdapter {

	private String[] mTitles;
	private Context mContext;
	private LayoutInflater mInflator;
	private String mDay, mDayName, mTodayYear, mTodayMonth, mWeekDays, mView;

	public CalendarNavigationListAdapter(Context context, String[] titles,
			String date, String dayName, String todayYear, String month,
			String weekDays, FragmentManager fragmentmanager, String view) {
		mContext = context;
		mInflator = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		mTitles = titles;
		mDay = date;
		mDayName = dayName;
		mTodayYear = todayYear;
		mTodayMonth = month;
		mWeekDays = weekDays;
		mView = view;
	//	this.fragmentmanager = fragmentmanager;

	}

	@Override
	public int getCount() {
		return mTitles.length;
	}

	@Override
	public Object getItem(int position) {
		return mTitles[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = mInflator.inflate(R.layout.cal_custommenu_top, parent, false);
		TextView txt_menutop_day = (TextView) v.findViewById(R.id.txt_item_day);
		TextView txt_menutop_year = (TextView) v
				.findViewById(R.id.txt_item_year);

		if (CalendarCommonFunction.isTablet(mContext)) {
			txt_menutop_day.setText(mView);
			txt_menutop_year.setVisibility(View.GONE);
		} else {
			txt_menutop_year.setVisibility(View.VISIBLE);
			txt_menutop_day.setText(mDayName.toUpperCase());
			if (mDayName.equals("")) {
				txt_menutop_day.setVisibility(View.GONE);
				txt_menutop_year.setTextSize(16);
			}
			txt_menutop_year.setText(mDay);
		}

		// Log.e("size","heigh"+v.getHeight()+"width"+v.getWidth());
		// Log.e("txt_menutop_day","heigh"+txt_menutop_day.getHeight()+"width"+txt_menutop_day.getWidth());
		// Log.e("txt_menutop_year","heigh"+txt_menutop_year.getHeight()+"width"+txt_menutop_year.getWidth());
		return v;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		// LayoutInflater inflater =
		// (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (convertView == null) {
			convertView = mInflator.inflate(R.layout.cal_custommenu, null);
			holder = new ViewHolder();
			holder.txt_label1 = (TextView) convertView
					.findViewById(R.id.txt_dropdown_label1);
			holder.txt_label2 = (TextView) convertView
					.findViewById(R.id.txt_dropdown_label2);

			if (CalendarCommonFunction.isTablet(mContext)) {
				holder.txt_label2.setVisibility(View.INVISIBLE);
			} else {
				holder.txt_label2.setVisibility(View.VISIBLE);
			}
			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.txt_label1.setText(mTitles[position]);
		// holder.txt_label2.setText("               ");
		if (position == 0 || position == 3) {
			holder.txt_label2.setText(mTodayYear);
		} else if (position == 2) {
			holder.txt_label2.setText(mTodayMonth);
		} else if (position == 1) {
			holder.txt_label2.setText(mWeekDays);
		} else {
			holder.txt_label2.setText("           ");
		}

		return convertView;
	}

	class ViewHolder {
		TextView txt_label1;
		TextView txt_label2;
	}

}
