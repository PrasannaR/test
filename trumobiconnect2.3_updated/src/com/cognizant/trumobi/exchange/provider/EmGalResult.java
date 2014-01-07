package com.cognizant.trumobi.exchange.provider;

import java.io.Serializable;
import java.util.ArrayList;

import android.os.Parcel;

import com.cognizant.trumobi.em.mail.EmPackedString;

/**
 * A container for GAL results from EAS Each element of the galData array
 * becomes an element of the list used by autocomplete
 */
public class EmGalResult {
	// Total number of matches in this result
	public int total;
	public ArrayList<GalData> galData = new ArrayList<GalData>();

	public EmGalResult() {
	}

	/**
	 * Legacy method for email address autocomplete
	 */
	public void addGalData(long id, String displayName, String emailAddress) {
		galData.add(new GalData(id, displayName, emailAddress));
	}

	public void addGalData(GalData data) {
		galData.add(data);
	}

	public static class GalData implements Serializable {
		// PackedString constants for GalData
		public static final String ID = "_id";
		public static final String DISPLAY_NAME = "displayName";
		public static final String EMAIL_ADDRESS = "emailAddress";
		public static final String WORK_PHONE = "workPhone";
		public static final String HOME_PHONE = "homePhone";
		public static final String MOBILE_PHONE = "mobilePhone";
		public static final String FIRST_NAME = "firstName";
		public static final String LAST_NAME = "lastName";
		public static final String COMPANY = "company";
		public static final String TITLE = "title";
		public static final String OFFICE = "office";
		public static final String ALIAS = "alias";
		public static final String PHOTO = "photo"; 
		// The Builder we use to construct the PackedString
		public EmPackedString.Builder builder = new EmPackedString.Builder();

		// The following three fields are for legacy email autocomplete
		public long _id = 0;
		public String displayName;
		public String emailAddress;

		/**
		 * Legacy constructor for email address autocomplete
		 */
		public GalData(long id, String _displayName, String _emailAddress) {
			put(ID, Long.toString(id));
			_id = id;
			put(DISPLAY_NAME, _displayName);
			displayName = _displayName;
			put(EMAIL_ADDRESS, _emailAddress);
			emailAddress = _emailAddress;
		}

		public GalData() {
		}

		public GalData(Parcel in) {
			// TODO Auto-generated constructor stub
		}

		public String get(String field) {

		//	EmailLog.d("GALDATA", "builder " + builder);
			return builder.get(field);
		}

		public void put(String field, String value) {
			builder.put(field, value);
		}

		public String toPackedString() {
			return builder.toString();
		}

	}
}
