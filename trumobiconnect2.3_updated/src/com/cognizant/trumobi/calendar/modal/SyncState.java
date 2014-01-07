package com.cognizant.trumobi.calendar.modal;

import android.net.Uri;

import com.cognizant.trumobi.calendar.util.CalendarConstants;

public class SyncState {
	public static final String ACCOUNT_NAME = "account_name";
	public static final String DATA = "data";
	public static final String ACCOUNT_TYPE = "account_type";
	public static final Uri SYNCSTATE_CONTENT_URI = Uri.parse("content://"
			+ CalendarConstants.AUTHORITY + "/"
			+ CalendarConstants.TABLE_SYNC_STATE);
	public static final String _ID = "_id";
}
