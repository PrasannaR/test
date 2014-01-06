package com.cognizant.trumobi.messenger.db;

public class SmsDbHelper {

	public static final String CREATE_TABLE_INDIVIDUAL = "CREATE TABLE "
			+ SmsIndividualTable.TABLE_INDIVIDUAL + " ("
			+ SmsIndividualTable.INDIVIDUAL_ID + " INTEGER,"
			+ SmsIndividualTable.PHONE_NUMBER + " TEXT,"
			+ SmsIndividualTable.FIRST_NAME + " TEXT,"
			+ SmsIndividualTable.LAST_NAME + " TEXT,"
			+ SmsIndividualTable.MESSAGE + " TEXT," + SmsIndividualTable.DATE
			+ " TEXT," + SmsIndividualTable.TIME + " INTEGER,"
			+ SmsIndividualTable.SEND_RECEIVE + " TEXT,"
			+ SmsIndividualTable.IMAGE + " BLOB,"
			+ SmsIndividualTable.DELIVERY_STATUS + " TEXT,"
			+ SmsIndividualTable.SENT_STATUS + " TEXT,"
			+ SmsIndividualTable.UNREAD_COUNT + " INTEGER,"
			+ SmsIndividualTable.DRAFT_MSG + " TEXT," + "PRIMARY KEY ("
			+ SmsIndividualTable.INDIVIDUAL_ID + ","
			+ SmsIndividualTable.PHONE_NUMBER + "));";

	public static final String CREATE_TABLE_HISTORY = "CREATE TABLE "
			+ SmsHistoryTable.TABLE_HISTORY + " (" + SmsHistoryTable.HISTROY_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ SmsHistoryTable.PHONE_NUMBER + " TEXT,"
			+ SmsHistoryTable.MULTI_CONTACT + " TEXT,"
			+ SmsHistoryTable.FIRST_NAME + " TEXT," + SmsHistoryTable.LAST_NAME
			+ " TEXT," + SmsHistoryTable.MESSAGE + " TEXT,"
			+ SmsHistoryTable.DATE + " TEXT," + SmsHistoryTable.TIME + " TEXT,"
			+ SmsHistoryTable.SEND_RECEIVE + " TEXT,"
			+ SmsHistoryTable.DELIVERY_STATUS + " TEXT,"
			+ SmsHistoryTable.SENT_STATUS + " TEXT," + SmsHistoryTable.IMAGE
			+ " BLOB," + SmsHistoryTable.LOCKED + " TEXT);";
	// + "FOREIGN KEY(" +HistoryTable.PHONE_NUMBER +") REFERENCES"+" "+
	// IndividualTable.TABLE_INDIVIDUAL +"("
	// +IndividualTable.PHONE_NUMBER+") ON DELETE CASCADE)";
	
	public static final String CREATE_TABLE_HISTORY_BACKUP = "CREATE TABLE "
			+ SmsHistoryBackup.TABLE_HISTORY_BACKUP + " (" + SmsHistoryBackup.HISTROY_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ SmsHistoryBackup.PHONE_NUMBER + " TEXT,"
			+ SmsHistoryBackup.FIRST_NAME + " TEXT," + SmsHistoryBackup.LAST_NAME
			+ " TEXT," + SmsHistoryBackup.MESSAGE + " TEXT,"
			+ SmsHistoryBackup.DATE + " TEXT," + SmsHistoryBackup.TIME + " TEXT,"
			+ SmsHistoryBackup.IS_SEND + " TEXT,"
			+ SmsHistoryBackup.DELIVERY_STATUS + " TEXT,"
			+ SmsHistoryBackup.SENT_STATUS + " TEXT," + SmsHistoryBackup.IMAGE
			+ " BLOB," + SmsHistoryBackup.LOCKED + " TEXT,"
			+ SmsHistoryBackup.MSGCOUNT + " TEXT," + SmsHistoryBackup.CORPORATE_CONTACT + " TEXT);";

}
