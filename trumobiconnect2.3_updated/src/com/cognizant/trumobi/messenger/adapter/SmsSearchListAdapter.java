package com.cognizant.trumobi.messenger.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.messenger.model.SmsSearchListModel;

public class SmsSearchListAdapter extends BaseAdapter {
	Context mContext;
	ArrayList<SmsSearchListModel> searchList;
	private ViewHolder mViewHolder;

	public SmsSearchListAdapter(Context context,
			ArrayList<SmsSearchListModel> searchList) {
		super();
		mContext = context;
		this.searchList = searchList;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if (searchList != null)
			return searchList.size();
		else
			return 0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if (convertView == null) {
			mViewHolder = new ViewHolder();
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.sms_search_list_item,
					parent, false);
			mViewHolder.mName = (TextView) convertView
					.findViewById(R.id.con_name);
			// mViewHolder.mPhone = (TextView)
			// convertView.findViewById(R.id.con_name);
			mViewHolder.mBody = (TextView) convertView
					.findViewById(R.id.msg_body);
			convertView.setTag(mViewHolder);
		} else {
			mViewHolder = (ViewHolder) convertView.getTag();
		}

		if (searchList.get(position).getBody().length() != 0)// searchList.get(position).getName()!=null
																// &&
		{
			mViewHolder.mName.setText((searchList.get(position).getName())
					.toString());
			// mViewHolder.mPhone.setText((searchList.get(position).getPhone()).toString());
			mViewHolder.mBody.setText((searchList.get(position).getBody())
					.toString());
		}

		return convertView;
	}

	static class ViewHolder {
		TextView mName;
		// TextView mPhone;
		TextView mBody;
	}

}
