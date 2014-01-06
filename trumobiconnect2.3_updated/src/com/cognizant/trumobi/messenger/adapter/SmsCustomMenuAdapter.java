package com.cognizant.trumobi.messenger.adapter;

import java.util.ArrayList;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.messenger.model.SmsDetailsModel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SmsCustomMenuAdapter extends BaseAdapter {
	Context mContext;
	int menuItemType;

	ArrayList<String> msgDetails;

	public SmsCustomMenuAdapter(Context context, ArrayList<String> msgDetails,
			int menu) {
		super();
		this.menuItemType = menu;
		mContext = context;
		this.msgDetails = msgDetails;
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
	public View getView(int position, View newView, ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		newView = inflater.inflate(R.layout.sms_menuitems, parent, false);

		TextView txtvw = (TextView) newView.findViewById(R.id.menu);
		txtvw.setText(msgDetails.get(position));
		View linevw = (View) newView.findViewById(R.id.linediv);

		if (menuItemType == 0) {
			if ((position == 2) || (position == 0)) {
				linevw.setVisibility(View.INVISIBLE);
			} else {
				linevw.setVisibility(View.VISIBLE);
			}
		} else if (menuItemType == 1) {
			if ((position == 1) || (position == 2)) {
				linevw.setVisibility(View.VISIBLE);
			} else {
				linevw.setVisibility(View.INVISIBLE);
			}
		} else if (menuItemType == 2) {
			if ((position == 2) || (position == 0)) {
				linevw.setVisibility(View.INVISIBLE);
			} else {
				linevw.setVisibility(View.VISIBLE);
			}
		}
		return newView;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return msgDetails.size();
	}

}
