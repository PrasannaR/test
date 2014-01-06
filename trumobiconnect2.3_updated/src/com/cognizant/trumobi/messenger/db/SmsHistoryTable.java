package com.cognizant.trumobi.messenger.db;

import android.graphics.Bitmap;
import android.net.Uri;

public class SmsHistoryTable {

	public static final String TABLE_HISTORY = "History";
	// History Table Columns names
	public static final String HISTROY_ID = "_id";
	public static final String FIRST_NAME = "firstname";
	public static final String LAST_NAME = "lastname";
	public static final String PHONE_NUMBER = "phonenumber";
	public static final String MESSAGE = "message";
	public static final String DATE = "date";
	public static final String TIME = "time";
	public static final String IMAGE = "personimage";
	public static final String SEND_RECEIVE = "send_receive";
	public static final String DELIVERY_STATUS = "delivery_status";
	public static final String SENT_STATUS = "sent_status";
	public static final String LOCKED = "lock";
	public static final String MULTI_CONTACT = "multi_contact";

	public static final String AUTHORITY = "com.cognizant.trumobi.messenger.db";
	public static final Uri CONTENT_URI_HISTORY = Uri.parse("content://"
			+ AUTHORITY + "/" + TABLE_HISTORY);

	public String firstName;
	public String lastName;// SUBJECT;
	public String message;// BODY;
	public String phoneNumber;// PERSON;
	public String date;
	public long time;
	public boolean sendReceive;
	// public Bitmap conPicture;
	public byte[] contactImage;
	public String delivery_status;
	public String sent_status;
	public boolean lock;
	public String multi_contact;

	public SmsHistoryTable() {
		// TODO Auto-generated constructor stub
		this.firstName = "";
		this.lastName = "";
		this.message = "";
		this.phoneNumber = "";
		this.date = "";
		this.time = 0;
		this.sendReceive = false;
		// this.conPicture = null;
		this.contactImage = null;
		this.delivery_status = "";
		this.sent_status = "";
		this.lock = false;
		this.multi_contact = "";
	}

	public SmsHistoryTable(String firstName, String lastName, String message,
			String phoneNumber, String date, long time, boolean sendReceive,
			byte[] conPic, String delivery_status, String sent_status,
			boolean lock, String multi_contact) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.message = message;
		this.phoneNumber = phoneNumber;
		this.date = date;
		this.time = time;
		this.sendReceive = sendReceive;
		// this.conPicture = conPicBitmap;
		this.contactImage = conPic;
		this.delivery_status = delivery_status;
		this.sent_status = sent_status;
		this.lock = lock;
		this.multi_contact = multi_contact;

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

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public boolean getSendReceive() {
		return sendReceive;
	}

	public void setSendReceive(boolean sendReceive) {
		this.sendReceive = sendReceive;
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
	
	public String getMultiContact() {
		return multi_contact;
	}

	public void setMultiContact(String multiNumber) {
		this.multi_contact = multiNumber;
	}

}
