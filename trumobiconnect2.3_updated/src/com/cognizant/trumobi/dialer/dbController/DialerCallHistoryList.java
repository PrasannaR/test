package com.cognizant.trumobi.dialer.dbController;

import java.io.Serializable;

public class DialerCallHistoryList implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//private byte[] img_src;
	
	private String callType="";
	
	private String callDate="";
	
	private String callTime="";
	
	private int callCount=0;
	
	public int getCallCount() {
		return callCount;
	}
	public void setCallCount(int callCount) {
		this.callCount = callCount;
	}
	public String getCallTime() {
		return callTime;
	}
	public void setCallTime(String callTime) {
		this.callTime = callTime;
	}
	
	
	/*public byte[] getImg_src() {
		return img_src;
	}
	public void setImg_src(byte[] img_src) {
		this.img_src = img_src;
	}*/

	
	public String getCallType() {
		return callType;
	}
	public void setCallType(String callType) {
		this.callType = callType;
	}
	
	public String getCallDate() {
		return callDate;
	}
	public void setCallDate(String callDate) {
		this.callDate = callDate;
	}
}
