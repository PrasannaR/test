package com.cognizant.trumobi.messenger.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.messenger.model.SmsDetailsModel;
import com.google.common.base.Strings;

public class SmsCustomAdapter extends BaseAdapter {
	Context mContext;

	ArrayList<SmsDetailsModel> msgDetails;
	private ViewHolder mViewHolder;

	public SmsCustomAdapter(Context context, ArrayList<SmsDetailsModel> msgDetails) {
		super();
		mContext = context;
		this.msgDetails = msgDetails;
	}

	@Override
	public int getCount() {

		if (msgDetails != null)
			return msgDetails.size();
		else
			return 0;
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

		if (newView == null) {
			mViewHolder = new ViewHolder();
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			newView = inflater.inflate(R.layout.sms_message_details, parent,
					false);
			mViewHolder.parentLayout = (RelativeLayout) newView.findViewById(R.id.parentRelative);
			mViewHolder.mMsgDate = (TextView) newView.findViewById(R.id.date);
			mViewHolder.mMsgBody = (TextView) newView
					.findViewById(R.id.message);
			mViewHolder.mMsgAddress = (TextView) newView
					.findViewById(R.id.contact_details);
			mViewHolder.conImage = (ImageView) newView
					.findViewById(R.id.contact_icon);
			mViewHolder.mUnreadCount = (TextView) newView
					.findViewById(R.id.unread_sms);
			newView.setTag(mViewHolder);
		} else {
			mViewHolder = (ViewHolder) newView.getTag();
		}

		if (msgDetails.get(position).getmContactAddress() != null
				&& msgDetails.get(position).getmContactAddress().length() != 0) {
			if(msgDetails.get(position).getUnreadCount() != 0){
				mViewHolder.parentLayout.setBackgroundColor(Color.WHITE);
				mViewHolder.mMsgDate.setTypeface(null, Typeface.BOLD);
				mViewHolder.mMsgBody.setTypeface(null, Typeface.BOLD);
				mViewHolder.mMsgAddress.setTypeface(null, Typeface.BOLD);
				mViewHolder.mUnreadCount.setTypeface(null, Typeface.BOLD);
				mViewHolder.mUnreadCount.setText(String.valueOf(msgDetails.get(position).getUnreadCount()));
			}
			mViewHolder.mMsgAddress
					.setText((msgDetails.get(position).getName()).toString());
			String strStatus = msgDetails.get(position).getmMsgTime()
					.toString();
			if ("Failed".equals(strStatus))
				mViewHolder.mMsgDate.setText(Html
						.fromHtml("<font color=\"red\">" + "Failed "
								+ "</font>"));
			else if ("Draft".equals(strStatus))
				mViewHolder.mMsgDate
						.setText(Html.fromHtml("<font color=\"red\">"
								+ "Draft " + "</font>"));
			else
				mViewHolder.mMsgDate.setText((msgDetails.get(position)
						.getmMsgTime()).toString());
			if(!Strings.isNullOrEmpty(msgDetails.get(position).getMsgBody()))
					mViewHolder.mMsgBody
					.setText((msgDetails.get(position).getMsgBody()).toString());
			byte[] b = msgDetails.get(position).getImageByte();
			if (b != null) {
				Bitmap image = BitmapFactory.decodeByteArray(b, 0, b.length);
				mViewHolder.conImage.setImageBitmap(image);
			}
		}

		return newView;
	}

	static class ViewHolder {
		TextView mMsgDate;
		TextView mMsgBody;
		TextView mMsgAddress;
		ImageView conImage;
		RelativeLayout parentLayout;
		TextView mUnreadCount;
		
	}

}
