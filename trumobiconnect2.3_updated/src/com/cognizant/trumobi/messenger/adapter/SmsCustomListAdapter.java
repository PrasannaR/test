package com.cognizant.trumobi.messenger.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.messenger.model.SmsDetailsListModel;
import com.cognizant.trumobi.messenger.sms.SmsMain;

public class SmsCustomListAdapter extends BaseAdapter {
	Context cnt;
	ArrayList<SmsDetailsListModel> msgArrayList;
	ViewHolder mListViewHolder;

	public SmsCustomListAdapter(Context mListContext,
			ArrayList<SmsDetailsListModel> lMsglist) {
		cnt = mListContext;
		msgArrayList = lMsglist;
	}

	@Override
	public int getCount() {
		return msgArrayList.size();
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
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			mListViewHolder = new ViewHolder();
			LayoutInflater inflater = (LayoutInflater) cnt
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.sms_message_detailr_view,
					parent, false);

			mListViewHolder.mRelSent = (RelativeLayout) convertView
					.findViewById(R.id.sentmail);
			mListViewHolder.mRelRec = (RelativeLayout) convertView
					.findViewById(R.id.recievemail);
			mListViewHolder.sentMessageText = (TextView) convertView
					.findViewById(R.id.bodytext_sent);
			mListViewHolder.sentMessageTime = (TextView) convertView
					.findViewById(R.id.datetext_sent);
			mListViewHolder.sentMessageDate = (TextView) convertView
					.findViewById(R.id.daytext_sent);
			mListViewHolder.sentContactIcon = (ImageView) convertView
					.findViewById(R.id.image_sent);
			mListViewHolder.sentMsgLocked = (ImageView) convertView
					.findViewById(R.id.sent_lock_msg);
			mListViewHolder.recvMessageText = (TextView) convertView
					.findViewById(R.id.bodytext_receive);
			mListViewHolder.recvMessageTime = (TextView) convertView
					.findViewById(R.id.datetext_receive);
			mListViewHolder.recvMessageDate = (TextView) convertView
					.findViewById(R.id.daytext_receive);
			mListViewHolder.recvContactIcon = (ImageView) convertView
					.findViewById(R.id.image_recevive);
			mListViewHolder.recvMsgLocked = (ImageView) convertView
					.findViewById(R.id.rece_lock_msg);
			convertView.setTag(mListViewHolder);
		} else {
			mListViewHolder = (ViewHolder) convertView.getTag();
		}

		if (msgArrayList.get(position).ismSendRec()) {
			mListViewHolder.mRelSent.setVisibility(View.VISIBLE);
			mListViewHolder.mRelRec.setVisibility(View.GONE);
			CharSequence body = SmsMain.formatMessage(msgArrayList.get(position)
					.getMsgBody().toString());
			mListViewHolder.sentMessageText.setText(body);
			String strStatus = msgArrayList.get(position).getmMsgTime()
					.toString();
			if ("Failed".equalsIgnoreCase(strStatus))
				mListViewHolder.sentMessageTime.setText(Html
						.fromHtml("<font color=\"red\">" + "failed "
								+ "</font>"));
			else
				mListViewHolder.sentMessageTime.setText((msgArrayList
						.get(position).getmMsgTime()).toString());
			// mListViewHolder.sentMessageDate.setText(msgArrayList.get(position).getmMsgDate().toString());
			/*
			 * byte[] b = msgArrayList.get(position).getPicture(); Bitmap image
			 * = BitmapFactory.decodeByteArray(b, 0, b.length);
			 * mListViewHolder.sentContactIcon.setImageBitmap(image);
			 */
			String strDate = msgArrayList.get(position).getmMsgDate()
					.toString();
			if (strDate != null)
				mListViewHolder.sentMessageDate.setText((msgArrayList
						.get(position).getmMsgDate()).toString());
			if (msgArrayList.get(position).ismLocked()) {
				mListViewHolder.sentMsgLocked
						.setBackgroundResource(R.drawable.sms_lock_message);
				mListViewHolder.recvMsgLocked.setVisibility(View.VISIBLE);
			} else
				mListViewHolder.sentMsgLocked.setBackgroundDrawable(null);
		} else {
			mListViewHolder.mRelSent.setVisibility(View.GONE);
			mListViewHolder.mRelRec.setVisibility(View.VISIBLE);
			CharSequence body = SmsMain.formatMessage(msgArrayList.get(position)
					.getMsgBody().toString());
			mListViewHolder.recvMessageText.setText(body);
			mListViewHolder.recvMessageTime.setText(msgArrayList.get(position)
					.getmMsgTime().toString());
			mListViewHolder.recvMessageDate.setText(msgArrayList.get(position)
					.getmMsgDate().toString());
			byte[] b = msgArrayList.get(position).getPicture();
			Bitmap image = BitmapFactory.decodeByteArray(b, 0, b.length);
			mListViewHolder.recvContactIcon.setImageBitmap(image);
			if (msgArrayList.get(position).ismLocked()) {
				mListViewHolder.recvMsgLocked
						.setBackgroundResource(R.drawable.sms_lock_message);
				mListViewHolder.recvMsgLocked.setVisibility(View.VISIBLE);
			} else
				mListViewHolder.recvMsgLocked.setBackgroundDrawable(null);
				//mListViewHolder.recvMsgLocked.setVisibility(View.INVISIBLE);
		}

		return convertView;
	}

	static class ViewHolder {
		RelativeLayout mRelSent;
		RelativeLayout mRelRec;
		TextView sentMessageText;
		TextView sentMessageTime;
		TextView sentMessageDate;
		ImageView sentContactIcon;
		TextView recvMessageText;
		TextView recvMessageTime;
		TextView recvMessageDate;
		ImageView recvContactIcon;
		ImageView sentMsgLocked;
		ImageView recvMsgLocked;

	}

}
