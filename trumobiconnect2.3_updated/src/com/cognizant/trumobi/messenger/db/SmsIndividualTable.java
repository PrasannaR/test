package com.cognizant.trumobi.messenger.db;

import android.graphics.Bitmap;
import android.net.Uri;

public class SmsIndividualTable {

	public static final String TABLE_INDIVIDUAL = "Individual";
	// Individual Table Columns names
	public static final String INDIVIDUAL_ID = "_id";
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
	public static final String DRAFT_MSG = "draft_msg";
	public static final String UNREAD_COUNT = "unread_count";

	public static final String AUTHORITY = "com.cognizant.trumobi.messenger.db";
	public static final Uri CONTENT_URI_INDIVIDUAL = Uri.parse("content://"
			+ AUTHORITY + "/" + TABLE_INDIVIDUAL);

	public String firstName;
	public String lastName;// SUBJECT;
	public String message;// BODY;
	public String phoneNumber;// PERSON;
	public String date;
	public long time;
	public boolean sendReceive;
	// public Bitmap picture;
	public byte[] image;
	public String delivery_status;
	public String sent_status;
	public boolean draft_msg;
	public int unread_count;

	public SmsIndividualTable() {
		// TODO Auto-generated constructor stub
		this.firstName = "";
		this.lastName = "";
		this.message = "";
		this.phoneNumber = "";
		this.date = "";
		this.time = 0;
		this.sendReceive = false;
		// this.picture = null;
		this.image = null;
		this.delivery_status = "";
		this.sent_status = "";
		this.draft_msg = false;
		this.unread_count = 0;
	}

	public SmsIndividualTable(String firstName, String lastName,
			String message, String phoneNumber, String date, long time,
			boolean sendReceive, byte[] img, String delivery_status,
			String sent_status, boolean draft_msg, int unread_count) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.message = message;
		this.phoneNumber = phoneNumber;
		this.date = date;
		this.time = time;
		this.sendReceive = sendReceive;
		// this.picture = pic;
		this.image = img;
		this.delivery_status = delivery_status;
		this.sent_status = sent_status;
		this.draft_msg = draft_msg;
		this.unread_count = unread_count;

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
	 * public Bitmap getPicture(){ return picture; }
	 * 
	 * public void setPicture(Bitmap pic){ this.picture = pic; }
	 */

	public byte[] getImageByte() {
		return image;
	}

	public void setImageByte(byte[] img) {
		this.image = img;
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

	public boolean getDraftMessage() {
		return draft_msg;
	}

	public void setDraftMessage(boolean draftMsg) {
		this.draft_msg = draftMsg;
	}
	
	public int getUnreadCount(){
		return unread_count;
	}
	
	public void setUnreadCount(int count){
		this.unread_count = count;
	}

}
