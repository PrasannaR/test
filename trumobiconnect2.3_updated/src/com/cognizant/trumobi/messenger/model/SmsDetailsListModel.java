package com.cognizant.trumobi.messenger.model;

import java.text.SimpleDateFormat;

import android.util.Log;

public class SmsDetailsListModel {

	String mMsgTime;
	String mMsgDate;
	String mContactAddress;
	String mMsgBody;
	String mId;
	boolean mSendRec;
	byte[] picture;
	String sentStatus;
	boolean mLock;
	String multiContact;

	public boolean ismSendRec() {
		return mSendRec;
	}

	public void setmSendRec(boolean mSendRec) {
		this.mSendRec = mSendRec;
	}

	public boolean ismLocked() {
		return mLock;
	}

	public void setmLocked(boolean locked) {
		this.mLock = locked;
	}

	public SmsDetailsListModel(long lMsgDate, String lContactAddress,
			String lBody, String mSR, String id, byte[] pic, String sent,
			String lock, String multiContact) {

		SimpleDateFormat df = new SimpleDateFormat("dd MMM-hh:mm");
		String curTime = df.format(lMsgDate);
		String[] splitDate = curTime.split("-");
		String mDate = splitDate[0];
		String mTime = splitDate[1];
		String status = sent;
		if ("Sending....".equalsIgnoreCase(status)) {
			mMsgTime = sent;
			mMsgDate = "";
		}

		else if ("SMS sent".equalsIgnoreCase(status)) {
			mMsgTime = mTime;
			mMsgDate = mDate;
		} else if ("recv".equalsIgnoreCase(mSR)) {
			mMsgTime = mTime;
			mMsgDate = mDate;
		} else {
			mMsgTime = "Failed";
			mMsgDate = mDate;
		}

		mContactAddress = lContactAddress;
		mMsgBody = lBody;
		mId = id;
		picture = pic;
		this.multiContact = multiContact;

		if ("recv".equalsIgnoreCase(mSR))
			mSendRec = false;
		else
			mSendRec = true;
		if ("0".equalsIgnoreCase(lock)) {
			mLock = false;
		} else {
			mLock = true;
		}
	}

	public String getmMsgTime() {
		return mMsgTime;
	}

	public void setmMsgTime(String mMsgTime) {
		this.mMsgTime = mMsgTime;
	}

	public String getmMsgDate() {
		return mMsgDate;
	}

	public void setmMsgDate(String mMsgDate) {
		this.mMsgDate = mMsgDate;
	}

	public String getmContactAddress() {
		return mContactAddress;
	}

	public void setmContactAddress(String mContactAddress) {
		this.mContactAddress = mContactAddress;
	}

	public String getMsgBody() {
		return mMsgBody;

	}

	public void setMsgBody(String lBdy) {
		this.mMsgBody = lBdy;

	}

	public String getMsgId() {
		return mId;
	}

	public void setMsgId(String id) {
		this.mId = id;
	}

	public byte[] getPicture() {
		return picture;
	}

	public void setPicture(byte[] pic) {
		this.picture = pic;
	}

	/*
	 * public String getSentStatus() { return sentStatus;
	 * 
	 * } public void setSentStatus(String status) { this.sentStatus = status;
	 * 
	 * }
	 */

	public String getMultiContact() {
		return multiContact;
	}

	public void setMultiContact(String multi) {
		this.multiContact = multi;
	}

}
