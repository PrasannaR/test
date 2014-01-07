package com.cognizant.trumobi.messenger.db;

import android.graphics.Bitmap;
import android.net.Uri;

public class SmsHistoryBackup {

	public static final String TABLE_HISTORY_BACKUP = "HistoryBackup";
	// History Table Columns names
	public static final String HISTROY_ID = "_id";
	public static final String FIRST_NAME = "firstname";
	public static final String LAST_NAME = "lastname";
	public static final String PHONE_NUMBER = "phonenumber";
	public static final String MESSAGE = "message";
	public static final String DATE = "date";
	public static final String TIME = "time";
	public static final String IMAGE = "personimage";
	public static final String IS_SEND = "is_send";
	public static final String DELIVERY_STATUS = "delivery_status";
	public static final String SENT_STATUS = "sent_status";
	public static final String LOCKED = "lock";
	public static final String MSGCOUNT = "msg_count";
	public static final String CORPORATE_CONTACT = "corporate_contact";

	public static final String AUTHORITY = "com.cognizant.trumobi.messenger.db";
	public static final Uri CONTENT_URI_HISTORY_BACKUP = Uri.parse("content://"
			+ AUTHORITY + "/" + TABLE_HISTORY_BACKUP);
	public static final int MSG_LENGTH = 60;

	public String firstName;
	public String lastName;// SUBJECT;
	public String message;// BODY;
	public String phoneNumber;// PERSON;
	public String date;
	public String time;
	public boolean isSend;
	// public Bitmap conPicture;
	public byte[] contactImage;
	public String delivery_status;
	public String sent_status;
	public boolean lock;
	public int msg_count;
	public boolean corporate_contact;

	public SmsHistoryBackup() {
		// TODO Auto-generated constructor stub
		this.firstName = "";
		this.lastName = "";
		this.message = "";
		this.phoneNumber = "";
		this.date = "";
		this.time = "";
		this.isSend = false;
		// this.conPicture = null;
		this.contactImage = null;
		this.delivery_status = "";
		this.sent_status = "";
		this.lock = false;
		this.msg_count = 0;
		this.corporate_contact = false;
		
	}

	public SmsHistoryBackup(String firstName, String lastName, String message,
			String phoneNumber, String date, String time, boolean isSend,
			byte[] conPic, String delivery_status, String sent_status,
			boolean lock, int msg_count, boolean corporate_contact) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.message = message;
		this.phoneNumber = phoneNumber;
		this.date = date;
		this.time = time;
		this.isSend = isSend;
		// this.conPicture = conPicBitmap;
		this.contactImage = conPic;
		this.delivery_status = delivery_status;
		this.sent_status = sent_status;
		this.lock = lock;
		this.msg_count = msg_count;
		this.corporate_contact = corporate_contact;

	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String mTime) {
		this.time = mTime;
	}

	public boolean isSend() {
		return isSend;
	}

	public void setSend(boolean isSend) {
		this.isSend = isSend;
	}

	/*
	 * public Bitmap getImage(){ return conPicture; }
	 * 
	 * public void setImage(Bitmap pic){ this.conPicture = pic; }
	 */

	public byte[] getContacctImageByte() {
		return contactImage;
	}

	public void setContactImageByte(byte[] img) {
		this.contactImage = img;
	}

	public String getDeliveryStatus() {
		return delivery_status;
	}

	public void setDeliveryStatus(String status) {
		this.delivery_status = status;
	}

	public String getSentStatus() {
		return sent_status;
	}

	public void setSentStatus(String sent_status) {
		this.sent_status = sent_status;
	}

	public boolean getLocked() {
		return lock;
	}

	public void setLocked(boolean lock) {
		this.lock = lock;
	}
	
	public int getMsgCount(){
		return msg_count;
	}
	
	public void setMsgCount(int msg_count){
		this.msg_count = msg_count;
	}
	
	public boolean getCoporateFlag(){
		return corporate_contact;
	}
	
	public void setCorporateFlag(boolean corporateFlag){
		this.corporate_contact = corporateFlag;
	}

}

