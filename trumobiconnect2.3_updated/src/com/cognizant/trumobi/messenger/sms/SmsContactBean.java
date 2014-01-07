package com.cognizant.trumobi.messenger.sms;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class SmsContactBean implements Parcelable {
	public String name;
	public String phoneNo;
	public String type;
	public boolean selected;
	// private Bitmap picture;
	public byte[] byteImage;
	public boolean isCorporate;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public void toggleChecked() {
		selected = !selected;
	}

	/*
	 * public void setPicture(Bitmap picture) { // NEW METHOD this.picture =
	 * picture; }
	 * 
	 * public Bitmap getPicture() { // NEW METHOD return picture; }
	 */

	public byte[] getByteImage() {
		return byteImage;
	}

	public void setByteImage(byte[] pic) {
		this.byteImage = pic;
	}
	
	public boolean isCorporate(){
		return isCorporate;
	}
	
	public void setCorporate(boolean corporateFlag){
		this.isCorporate = corporateFlag;
	}

	public SmsContactBean(Parcel in) {
		readFromParcel(in);
	}

	public SmsContactBean() {
		// TODO Auto-generated constructor stub
	}

	private void readFromParcel(Parcel in) {
		// TODO Auto-generated method stub
		this.phoneNo = in.readString();
		this.name = in.readString();
		// byteImage = in.readByteArray(getByteImage());
		byte[] ba = in.createByteArray();
		in.unmarshall(ba, 0, ba.length);
		this.byteImage = ba;
		this.byteImage = new byte[in.readInt()];
		in.readByteArray(this.byteImage);
		//isCorporate  = (in.readInt() == 0) ? false : true;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeString(phoneNo);
		dest.writeString(name);
		if(byteImage != null){
			dest.writeInt(byteImage.length);
			dest.writeByteArray(byteImage);
		}
		//dest.writeInt(isCorporate ? 1 : 0);
	}

	@SuppressWarnings("rawtypes")
	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public SmsContactBean createFromParcel(Parcel parcel) {
			return new SmsContactBean(parcel);
		}

		public SmsContactBean[] newArray(int size) {
			return new SmsContactBean[size];
		}
	};

}
