package com.cognizant.trumobi.messenger.model;

import java.text.SimpleDateFormat;

import com.google.common.base.Strings;

import android.util.Log;

public class SmsDetailsModel {

	String mMsgTime;
	String mContactAddress;
	String mName;
	String mMsgBody;
	String mId;
	byte[] byteImage;
	String mSentStatus;
	String mdraft;
	int mUnreadCount;

	public SmsDetailsModel(long time, String lContactAddress, String name,
			String lBody, String id, byte[] img, String sent, String draft,
			int unreadCount) {
		String currentTime;
		SimpleDateFormat df = new SimpleDateFormat("dd MMM-hh:mm");
		String dbTime = df.format(time);
		String[] splitDate = dbTime.split("-");
		String mDate = splitDate[0];
		String mTime = splitDate[1];
		long curTime = System.currentTimeMillis();
		SimpleDateFormat curdf = new SimpleDateFormat("dd MMM-hh:mm");
		String strTime = df.format(curTime);
		String[] strSplitDate = strTime.split("-");
		String strmDate = strSplitDate[0];
		String strmTime = strSplitDate[1];
		if (mDate.equals(strmDate)) {
			currentTime = mTime;
		} else {
			currentTime = mDate;
		}
		mContactAddress = lContactAddress;
		mName = name;
		mMsgBody = lBody;
		mId = id;
		byteImage = img;
		String strStatus = sent;
		if ((Strings.isNullOrEmpty(sent) || "SMS sent"
				.equalsIgnoreCase(strStatus))
				&& ("0".equalsIgnoreCase(draft) || Strings.isNullOrEmpty(draft)))
			mMsgTime = currentTime;
		else if ("1".equalsIgnoreCase(draft))
			mMsgTime = "Draft";
		else
			mMsgTime = "Failed";
		mUnreadCount = unreadCount;
	}

	public String getmMsgTime() {
		return mMsgTime;
	}

	public void setmMsgTime(String mMsgTime) {
		this.mMsgTime = mMsgTime;
	}

	public String getmContactAddress() {
		return mContactAddress;
	}

	public void setmContactAddress(String mContactAddress) {
		this.mContactAddress = mContactAddress;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public String getMsgBody() {
		return mMsgBody;

	}

	public void setMsgBody(String lBdy) {
		mMsgBody = lBdy;

	}

	public String getMsgId() {
		return mId;

	}

	public void setMsgId(String id) {
		mId = id;

	}

	public byte[] getImageByte() {
		return byteImage;
	}

	public void setImageByte(byte[] img) {
		byteImage = img;
	}

	public int getUnreadCount() {
		return mUnreadCount;
	}

	public void setUnreadCount(int count) {
		this.mUnreadCount = count;
	}

}
