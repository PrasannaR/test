package com.cognizant.trumobi.dialer.db;

import com.TruBoxSDK.TruBoxDatabase;
import com.cognizant.trumobi.em.Email;
import android.net.Uri;

public class DialerCorporateTable {

	public static final String DATABASE_NAME = TruBoxDatabase.getHashValue(
			"cognizantdialer.db", Email.getAppContext());
	public static final String TABLE_NAME = "Corporate";
	public static final String ID = "_id";
	public static final String ASSOICIATE_NAME = "associatename";
	public static final String PHONE_NUMBER = "phonenumber";
	public static final String CALL_TYPE = "calltype";//incoming, outgoing or missed
	public static final String NUMBER_TYPE = "numbertype";
	public static final String DATE = "date";
	public static final String CALL_DURATION = "callduration";
	public static final String IS_CORPORATE = "iscorporate"; // integers 0 (false) and 1 (true).
	public static final String AUTHORITY = "com.cognizant.trumobi.dialer.corporate.db";
	public static final Uri CONTENT_URI_DIALER = Uri.parse("content://"
			+ AUTHORITY + "/" + TABLE_NAME);

	private String id;
	private String associateName;
	private String phoneNumber;
	private String callType;
	private String numberType;
	private String date;
	private String callDuration;
	private String isCorporate;

	public DialerCorporateTable() {
		this.associateName = "";
		this.phoneNumber = "";
		this.callType = "";
		this.numberType = "";
		this.date = "";
		this.callDuration = "";
		this.isCorporate = "";
	}

	public DialerCorporateTable(String associateName, String phoneNumber,
			String callType, String numberType, String date,
			String callDuration, String isCorporate) {
		this.associateName = associateName;
		this.phoneNumber = phoneNumber;
		this.callType = callType;
		this.numberType = numberType;
		this.date = date;
		this.callDuration = callDuration;
		this.isCorporate = isCorporate;

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAssociateName() {
		return associateName;
	}

	public void setAssociateName(String associateName) {
		this.associateName = associateName;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getCallType() {
		return callType;
	}

	public void setCallType(String callType) {
		this.callType = callType;
	}

	public String getNumberType() {
		return numberType;
	}

	public void setNumberType(String numberType) {
		this.numberType = numberType;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getCallDuration() {
		return callDuration;
	}

	public void setCallDuration(String callDuration) {
		this.callDuration = callDuration;
	}

	public String isCorporate() {
		return isCorporate;
	}

	public void setCorporate(String isCorporate) {
		this.isCorporate = isCorporate;
	}

}
