package com.cognizant.trumobi.dialer.db;


public class DialerCorporateDbHelper {

	public static final String CREATE_TABLE_DIALER_CORPORATE = "CREATE TABLE "
			+ DialerCorporateTable.TABLE_NAME + " (" + DialerCorporateTable.ID
			+ " INTEGER," 
			+ DialerCorporateTable.ASSOICIATE_NAME + " TEXT,"
			+ DialerCorporateTable.PHONE_NUMBER + " TEXT,"
			+ DialerCorporateTable.CALL_TYPE + " TEXT,"
			+ DialerCorporateTable.NUMBER_TYPE + " TEXT,"
			+ DialerCorporateTable.DATE + " TEXT,"
			+ DialerCorporateTable.CALL_DURATION + " TEXT,"
			+ DialerCorporateTable.IS_CORPORATE + " TEXT," + "PRIMARY KEY ("
			+ DialerCorporateTable.ID + "));";

}
