package com.cognizant.trumobi.messenger.model;

public class SmsSearchListModel {

	String mName;
	String mPhone;
	String mBody;
	String mMultiContact;

	public SmsSearchListModel(String name, String phone, String body, String multiContact) {
		mName = name;
		mPhone = phone;
		mBody = body;
		mMultiContact = multiContact;
	}

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		this.mName = name;
	}

	public String getPhone() {
		return mPhone;
	}

	public void setPhone(String phone) {
		this.mPhone = phone;
	}

	public String getBody() {
		return mBody;
	}

	public void setBody(String body) {
		this.mBody = body;
	}
	
	public String getMultiContact() {
		return mMultiContact;
	}

	public void setMultiContact(String multi) {
		this.mMultiContact = multi;
	}

}
