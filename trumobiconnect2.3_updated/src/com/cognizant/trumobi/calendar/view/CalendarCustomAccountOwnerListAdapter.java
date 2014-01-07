package com.cognizant.trumobi.calendar.view;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.calendar.util.CalendarConstants;

public class CalendarCustomAccountOwnerListAdapter extends BaseAdapter {
	private Context mContext;
	private ArrayList<ContentValues> mAccount_owner_list = new ArrayList<ContentValues>();
	private static LayoutInflater sInflater = null;

	public CalendarCustomAccountOwnerListAdapter(Context context,
			ArrayList<ContentValues> account_owner_list) {
		this.mContext = context;
		this.mAccount_owner_list = account_owner_list;
		sInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return mAccount_owner_list.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		View vi = view;
		if (view == null)
			vi = sInflater.inflate(R.layout.cal_account_owner_list, null);
		TextView account_name = (TextView) vi.findViewById(R.id.account_name); // title
		View account_color = (View) vi.findViewById(R.id.account_color_view); // thumb
																				// image
		ContentValues account_content_value = mAccount_owner_list.get(position);
		account_name.setText(account_content_value
				.getAsString(CalendarConstants.PERSONAL_OWNER_ACC_NAME));
		account_color.setBackgroundColor(account_content_value
				.getAsInteger(CalendarConstants.PERSONAL_OWNER_ACC_COLOR));
		return vi;
	}

}
