package com.cognizant.trumobi.calendar.modal;

public class Recurrence {

	public static final String EVENT_ID = Event.EVENT_ID;
	public static final String TYPE = "type";
	public static final String OCCURENCES = "occurences";
	public static final String INTERVAL = "interval";
	public static final String DOW = "dow";
	public static final String DOW_STRING = "dow_string";
	public static final String DOM = "dom";
	public static final String WOM = "wom";
	public static final String MOY = "moy";
	public static final String UNTIL = "until";
	public static int DOW_SUNDAY=1;
	public static int DOW_MONDAY=2;
	public static int DOW_TUESDAY=4;
	public static int DOW_WEDNESDAY=8;
	public static int DOW_THURSDAY=16;
	public static int DOW_FRIDAY=32;
	public static int DOW_SATURDAY=64;

	public int event_id;
	public int type;
	public int occurences;
	public int interval;
	public int dow;
	public String dowString;
	public int dom;
	public int wom;
	public int moy;
	public long until;
	
	
	
	public Recurrence() {
		super();
		this.event_id = -1;
		this.type = -1;
		this.occurences = -1;
		this.interval = -1;
		this.dow = 0;
		this.dowString="";
		this.dom = -1;
		this.wom = -1;
		this.moy = -1;
		this.until = -1;
	}


	public Recurrence(int event_id, int type, int occurences, int interval,
			int dow,String dowString, int dom, int wom, int moy, long until) {
		super();
		this.event_id = event_id;
		this.type = type;
		this.occurences = occurences;
		this.interval = interval;
		this.dow = dow;
		this.dowString=dowString;
		this.dom = dom;
		this.wom = wom;
		this.moy = moy;
		this.until = until;
	}
	
	
	public int getEvent_id() {
		return event_id;
	}
	public void setEvent_id(int event_id) {
		this.event_id = event_id;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getOccurences() {
		return occurences;
	}
	public void setOccurences(int occurences) {
		this.occurences = occurences;
	}
	public int getInterval() {
		return interval;
	}
	public void setInterval(int interval) {
		this.interval = interval;
	}
	public String getDowString() {
		return dowString;
	}
	public int getDow() {
		return dow;
	}


	public void setDow(int dow) {
		this.dow = dow;
	}


	public void setDowString(String dowString) {
		this.dowString = dowString;
	}
	public int getDom() {
		return dom;
	}
	public void setDom(int dom) {
		this.dom = dom;
	}
	public int getWom() {
		return wom;
	}
	public void setWom(int wom) {
		this.wom = wom;
	}
	public int getMoy() {
		return moy;
	}
	public void setMoy(int moy) {
		this.moy = moy;
	}
	public long getUntil() {
		return until;
	}
	public void setUntil(long until) {
		this.until = until;
	}
	public boolean isEqual(Recurrence recurrence)
	{

		if(type != recurrence.type){  return false;     }
		if(occurences != recurrence.occurences){  return false;     }
		if(interval != recurrence.interval){  return false;     }
		if(dow != recurrence.dow){  return false;     }
		if(!dowString.equalsIgnoreCase(recurrence.dowString)){  return false;     }
		if(dom != recurrence.dom){  return false;     }
		if(wom != recurrence.wom){  return false;     }
		if(moy != recurrence.moy){  return false;     }
		if(until != recurrence.until){  return false;     }
		return true;
	}


}
