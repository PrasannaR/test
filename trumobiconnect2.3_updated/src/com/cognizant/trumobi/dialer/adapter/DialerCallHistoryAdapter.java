package com.cognizant.trumobi.dialer.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.dialer.dbController.DialerCallHistoryList;
import com.cognizant.trumobi.log.DialerLog;

public class DialerCallHistoryAdapter extends BaseAdapter {
	
	ArrayList<DialerCallHistoryList> mArrayCal;
	private LayoutInflater inflater = null;
	
	
	public DialerCallHistoryAdapter(Context context,ArrayList<DialerCallHistoryList> callHistoryList) {
		this.mArrayCal = callHistoryList;
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mArrayCal.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}
	
	@Override
	public boolean isEnabled(int position) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		// TODO Auto-generated method stub
		if (convertView == null) {
			convertView=inflater.inflate(R.layout.dialer_calllog_history_detail, null);
			viewHolder=new ViewHolder();
			viewHolder.callTypeImage=(ImageView)convertView.findViewById(R.id.call_image);
			viewHolder.callTypeText=(TextView) convertView.findViewById(R.id.call_type);
			viewHolder.callDuration=(TextView) convertView.findViewById(R.id.call_duration);
			viewHolder.callDate=(TextView) convertView.findViewById(R.id.call_time);
			convertView.setTag(viewHolder);
		} 
		viewHolder= (ViewHolder) convertView.getTag();
		String callType=mArrayCal.get(position).getCallType();
		
		DialerLog.e("Call Type List", "call Type in adapter  ->>  "+callType);
		
		int val=Integer.parseInt(callType);
		switch (val) {
		case 2:
			viewHolder.callTypeImage.setImageResource(R.drawable.ic_call_outgoing_holo_dark);
			viewHolder.callTypeText.setText("Outgoing call");
			break;
		case 3:
			viewHolder.callTypeImage.setImageResource(R.drawable.ic_call_missed_holo_dark);
			viewHolder.callTypeText.setText("Missed call");
			break;
		case 1:
			viewHolder.callTypeImage.setImageResource(R.drawable.ic_call_incoming_holo_dark);
			viewHolder.callTypeText.setText("Incomming call");
			break;
		case 4:
			viewHolder.callTypeImage.setImageResource(R.drawable.ic_call_incoming_holo_dark);
			viewHolder.callTypeText.setText("Incomming call");
			break;
		default:
			break;
		}
		DialerLog.e("New", "inAdapter --->  "+mArrayCal.get(position).getCallTime()+" Date ->>> "+mArrayCal.get(position).getCallDate());
		viewHolder.callDuration.setText(mArrayCal.get(position).getCallTime());
		viewHolder.callDate.setText(mArrayCal.get(position).getCallDate());
		return convertView;
	}
	
	static class ViewHolder {
		ImageView callTypeImage;
		TextView callTypeText,callDate,callDuration;
		
	}
	
	
}
