package com.cognizant.trumobi.container.Pojo;

import java.util.ArrayList;
import java.util.HashMap;

public class AttachmentSubItems {

	/*private  String ITEM_ID = "";*/
	private  ArrayList<String> ATTACHMENT_ID;
	
	private HashMap<String, String> ATTACHMENT_NAME;
	private HashMap<String, String> ATTACHMENT_SIZE;
	
	public AttachmentSubItems() {
		ATTACHMENT_ID = new ArrayList<String>();
		ATTACHMENT_NAME = new HashMap<String, String>();
		ATTACHMENT_SIZE = new HashMap<String, String>();
	}
	
	
	
	
	
	public HashMap<String, String> getATTACHMENT_NAME() {
		return ATTACHMENT_NAME;
	}
	
	public HashMap<String, String> getATTACHMENT_SIZE() {
		return ATTACHMENT_SIZE;
	}




	public void setATTACHMENT_NAME(String key, String value) {
		ATTACHMENT_NAME.put(key, value);
	}

	public String getATTACHMENT_NAME_Value(String key) {
		return ATTACHMENT_NAME.get(key);
	}
	
	public void setATTACHMENT_SIZE(String key, String value) {
		ATTACHMENT_SIZE.put(key, value);
	}

	public String getATTACHMENT_SIZE_Value(String key) {
		return ATTACHMENT_SIZE.get(key);
	}

	
	
	

	/*public String getITEM_ID() {
		return ITEM_ID;
	}
	public void setITEM_ID(String iTEM_ID) {
		ITEM_ID = iTEM_ID;
	}*/
	
	public String getATTACHMENT_ID_Value(int index) {
		return ATTACHMENT_ID.get(index);
	}
	
	
	public ArrayList<String> getATTACHMENT_ID() {
		return ATTACHMENT_ID;
	}
	public void setATTACHMENT_ID(String aTTACHMENT_ID) {
		ATTACHMENT_ID.add(aTTACHMENT_ID);
	}
	
	
}
