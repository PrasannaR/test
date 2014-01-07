package com.cognizant.trumobi.contatcs.dbController;

public class DialerCalLogs {
	
	
	private String mNumber;
	private String mName;
	private Byte[] ImageCal;
	private Boolean bmissCal;
	private Boolean bdailCal;
	private Boolean brecievCal;
	private String mDate;
	
	
	
	public String getmNumber() {
		return mNumber;
	}
	public void setmNumber(String mNumber) {
		this.mNumber = mNumber;
	}
	public String getmName() {
		return mName;
	}
	public void setmName(String mName) {
		this.mName = mName;
	}
	public Byte[] getImageCal() {
		return ImageCal;
	}
	public void setImageCal(Byte[] imageCal) {
		ImageCal = imageCal;
	}
	public Boolean getBmissCal() {
		return bmissCal;
	}
	public void setBmissCal(Boolean bmissCal) {
		this.bmissCal = bmissCal;
	}
	public Boolean getBdailCal() {
		return bdailCal;
	}
	public void setBdailCal(Boolean bdailCal) {
		this.bdailCal = bdailCal;
	}
	public Boolean getBrecievCal() {
		return brecievCal;
	}
	public void setBrecievCal(Boolean brecievCal) {
		this.brecievCal = brecievCal;
	}
	public String getmDate() {
		return mDate;
	}
	public void setmDate(String mDate) {
		this.mDate = mDate;
	}
}
