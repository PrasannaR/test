/**
 *  FileName : EmEmailProvider
 * 
 *  Desc : 
 * 
 * 
 *     Author                     Date                                         Desc
 *     371990			      07-Oct-2013                         Database name change implementation
 */

package com.cognizant.trumobi.em.provider;

import java.io.File;
import java.util.ArrayList;

import android.accounts.AccountManager;
import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

import com.TruBoxSDK.TruBoxDatabase;
import com.cognizant.trubox.contacts.db.ContactsConsts;
import com.cognizant.trubox.contacts.db.ContactsDbHelper;
import com.cognizant.trumobi.calendar.util.CalendarConstants;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.em.provider.EmEmailContent.Account;
import com.cognizant.trumobi.em.provider.EmEmailContent.AccountColumns;
import com.cognizant.trumobi.em.provider.EmEmailContent.Attachment;
import com.cognizant.trumobi.em.provider.EmEmailContent.AttachmentColumns;
import com.cognizant.trumobi.em.provider.EmEmailContent.Body;
import com.cognizant.trumobi.em.provider.EmEmailContent.BodyColumns;
import com.cognizant.trumobi.em.provider.EmEmailContent.HostAuth;
import com.cognizant.trumobi.em.provider.EmEmailContent.HostAuthColumns;
import com.cognizant.trumobi.em.provider.EmEmailContent.Mailbox;
import com.cognizant.trumobi.em.provider.EmEmailContent.MailboxColumns;
import com.cognizant.trumobi.em.provider.EmEmailContent.Message;
import com.cognizant.trumobi.em.provider.EmEmailContent.MessageColumns;
import com.cognizant.trumobi.em.provider.EmEmailContent.SecureBrowser;
import com.cognizant.trumobi.em.provider.EmEmailContent.SecureBrowserColumns;
import com.cognizant.trumobi.em.provider.EmEmailContent.SyncColumns;
import com.cognizant.trumobi.log.EmailLog;

/**
 * 
 * KEYCODE						AUTHOR		PURPOSE
 * PEN_TEST_SQL_INJECT_CHG		367712		Changes for PEN Test
 *
 */

public class EmEmailProvider extends ContentProvider {

	private static final String TAG = "EmailProvider";

	/*
	 * protected static final String DATABASE_NAME = "EmailProvider.db";
	 * protected static final String BODY_DATABASE_NAME =
	 * "EmailProviderBody.db";
	 */

	protected static final String DATABASE_NAME = TruBoxDatabase.getHashValue(
			"EmailProvider", Email.getAppContext());
	protected static final String BODY_DATABASE_NAME = TruBoxDatabase
			.getHashValue("EmailProviderBody", Email.getAppContext());

	public static final Uri INTEGRITY_CHECK_URI = Uri.parse("content://"
			+ EmEmailContent.AUTHORITY + "/integrityCheck");

	// Definitions for our queries looking for orphaned messages
	private static final String[] ORPHANS_PROJECTION = new String[] {
			MessageColumns.ID, MessageColumns.MAILBOX_KEY };
	private static final int ORPHANS_ID = 0;
	private static final int ORPHANS_MAILBOX_KEY = 1;

	private static final String WHERE_ID = EmEmailContent.RECORD_ID + "=?";

	// Any changes to the database format *must* include update-in-place code.
	// Original version: 3
	// Version 4: Database wipe required; changing AccountManager interface
	// w/Exchange
	// Version 5: Database wipe required; changing AccountManager interface
	// w/Exchange
	// Version 6: Adding Message.mServerTimeStamp column
	// Version 7: Replace the mailbox_delete trigger with a version that removes
	// orphaned messages
	// from the Message_Deletes and Message_Updates tables
	// Version 8: Add security flags column to accounts table
	// Version 9: Add security sync key and signature to accounts table
	// Version 10: Add meeting info to message table
	// Version 11: Add content and flags to attachment table
	// Version 12: Add content_bytes to attachment table. content is deprecated.
	public static final int DATABASE_VERSION = 12;

	// Any changes to the database format *must* include update-in-place code.
	// Original version: 2
	// Version 3: Add "sourceKey" column
	// Version 4: Database wipe required; changing AccountManager interface
	// w/Exchange
	// Version 5: Database wipe required; changing AccountManager interface
	// w/Exchange
	// Version 6: Adding Body.mIntroText column
	public static final int BODY_DATABASE_VERSION = 6;

	public static final String EMAIL_AUTHORITY = "com.cognizant.trumobi.em.provider";

	private static final int ACCOUNT_BASE = 0;
	private static final int ACCOUNT = ACCOUNT_BASE;
	private static final int ACCOUNT_MAILBOXES = ACCOUNT_BASE + 1;
	private static final int ACCOUNT_ID = ACCOUNT_BASE + 2;
	private static final int ACCOUNT_ID_ADD_TO_FIELD = ACCOUNT_BASE + 3;

	private static final int MAILBOX_BASE = 0x1000;
	private static final int MAILBOX = MAILBOX_BASE;
	private static final int MAILBOX_MESSAGES = MAILBOX_BASE + 1;
	private static final int MAILBOX_ID = MAILBOX_BASE + 2;
	private static final int MAILBOX_ID_ADD_TO_FIELD = MAILBOX_BASE + 3;

	private static final int MESSAGE_BASE = 0x2000;
	private static final int MESSAGE = MESSAGE_BASE;
	private static final int MESSAGE_ID = MESSAGE_BASE + 1;
	private static final int SYNCED_MESSAGE_ID = MESSAGE_BASE + 2;

	private static final int ATTACHMENT_BASE = 0x3000;
	private static final int ATTACHMENT = ATTACHMENT_BASE;
	private static final int ATTACHMENT_CONTENT = ATTACHMENT_BASE + 1;
	private static final int ATTACHMENT_ID = ATTACHMENT_BASE + 2;
	private static final int ATTACHMENTS_MESSAGE_ID = ATTACHMENT_BASE + 3;

	private static final int HOSTAUTH_BASE = 0x4000;
	private static final int HOSTAUTH = HOSTAUTH_BASE;
	private static final int HOSTAUTH_ID = HOSTAUTH_BASE + 1;

	private static final int UPDATED_MESSAGE_BASE = 0x5000;
	private static final int UPDATED_MESSAGE = UPDATED_MESSAGE_BASE;
	private static final int UPDATED_MESSAGE_ID = UPDATED_MESSAGE_BASE + 1;

	private static final int DELETED_MESSAGE_BASE = 0x6000;
	private static final int DELETED_MESSAGE = DELETED_MESSAGE_BASE;
	private static final int DELETED_MESSAGE_ID = DELETED_MESSAGE_BASE + 1;
	private static final int DELETED_MESSAGE_MAILBOX = DELETED_MESSAGE_BASE + 2;

	// MUST ALWAYS EQUAL THE LAST OF THE PREVIOUS BASE CONSTANTS
	private static final int LAST_EMAIL_PROVIDER_DB_BASE = DELETED_MESSAGE_BASE;

	public static final int CONTACT_GET_ALL_BASE = 0x7000;
	public static final int CONTACT_GET_ALL = CONTACT_GET_ALL_BASE;
	public static final int CONTACT_GET_ONE = CONTACT_GET_ALL + 1;

	public static final int CONTACT_ADRESS_TABLE_BASE = 0x8000;
	public static final int CONTACT_ADRESS_TABLE = CONTACT_ADRESS_TABLE_BASE;

	public static final int CONTACT_SYNCSTATE_TABLE_BASE = 0x9000;
	public static final int CONTACT_SYNCSTATE_TABLE = CONTACT_SYNCSTATE_TABLE_BASE;

	public static final int CONTACT_ADD_CACHE_ALL_BASE = 0xA000;
	public static final int CONTACT_ADD_CACHE_ALL = CONTACT_ADD_CACHE_ALL_BASE;
	public static final int CONTACT_ADD_CACHE_ONE = CONTACT_ADD_CACHE_ALL + 1;

	public static final int CONTACT_DELETE_CACHE_ALL_BASE = 0xB000;
	public static final int CONTACT_DELETE_CACHE_ALL = CONTACT_DELETE_CACHE_ALL_BASE;
	public static final int CONTACT_DELETE_CACHE_ONE = CONTACT_DELETE_CACHE_ALL + 1;

	public static final int CONTACT_UPDATE_CACHE_ALL_BASE = 0xC000;
	public static final int CONTACT_UPDATE_CACHE_ALL = CONTACT_UPDATE_CACHE_ALL_BASE;
	public static final int CONTACT_UPDATE_CACHE_ONE = CONTACT_UPDATE_CACHE_ALL + 1;

	/*************************************** for calendar **************************************************/

	public static final int CALENDAR_EVENTS_TABLE_BASE = 0xD000;
	public static final int CALENDAR_EVENTS_TABLE = CALENDAR_EVENTS_TABLE_BASE;

	public static final int CALENDAR_REMINDERS_TABLE_BASE = 0xE000;
	public static final int CALENDAR_REMINDERS_TABLE = CALENDAR_REMINDERS_TABLE_BASE;

	public static final int CALENDAR_ATTENDEES_TABLE_BASE = 0xF000;
	public static final int CALENDAR_ATTENDEES_TABLE = CALENDAR_ATTENDEES_TABLE_BASE;

	public static final int CALENDAR_SEARCH_HISTORY_TABLE_BASE = 0x10000;
	public static final int CALENDAR_SEARCH_HISTORY_TABLE = CALENDAR_SEARCH_HISTORY_TABLE_BASE;

	public static final int CALENDAR_EXCEPTIONS_TABLE_BASE = 0x11000;
	public static final int CALENDAR_EXCEPTIONS_TABLE = CALENDAR_EXCEPTIONS_TABLE_BASE;

	public static final int CALENDAR_SYNCSTATE_TABLE_BASE = 0x12000;
	public static final int CALENDAR_SYNCSTATE_TABLE = CALENDAR_SYNCSTATE_TABLE_BASE;

	// DO NOT CHANGE BODY_BASE!!
	private static final int BODY_BASE = CALENDAR_SYNCSTATE_TABLE_BASE + 0x1000;
	private static final int BODY = BODY_BASE;
	private static final int BODY_ID = BODY_BASE + 1;
	private static final int BODY_MESSAGE_ID = BODY_BASE + 2;
	private static final int BODY_HTML = BODY_BASE + 3;
	private static final int BODY_TEXT = BODY_BASE + 4;

	// Securebrowser Changes
	private static final int SECURE_BASE = BODY_BASE + 0x1000;
	private static final int SECURE_BROWSER = SECURE_BASE;

	private static final int BASE_SHIFT = 12; // 12 bits to the base type: 0,
	// 0x1000, 0x2000, etc.

	private static final String[] TABLE_NAMES = {
			EmEmailContent.Account.TABLE_NAME,
			EmEmailContent.Mailbox.TABLE_NAME,
			EmEmailContent.Message.TABLE_NAME,
			EmEmailContent.Attachment.TABLE_NAME,
			EmEmailContent.HostAuth.TABLE_NAME,
			EmEmailContent.Message.UPDATED_TABLE_NAME,
			EmEmailContent.Message.DELETED_TABLE_NAME,
			ContactsConsts.CONTACTS_TABLE_NAME,
			ContactsConsts.CONTACTS_ADRESS_TABLE_NAME,
			ContactsConsts.CONTACTS_SYNCSTATE_TABLE_NAME,
			ContactsConsts.CONTACTS_LOCAL_CACHE_ADD_TABLE_NAME,
			ContactsConsts.CONTACTS_LOCAL_CACHE_DELETE_TABLE_NAME,
			ContactsConsts.CONTACTS_LOCAL_CACHE_UPDATE_TABLE_NAME,
			CalendarConstants.TABLE_EVENTS, CalendarConstants.TABLE_REMINDERS,
			CalendarConstants.TABLE_ATTENDEES,
			CalendarConstants.TABLE_SEARCH_HISTORY,
			CalendarConstants.TABLE_EXCEPTIONS,
			CalendarConstants.TABLE_SYNC_STATE, EmEmailContent.Body.TABLE_NAME,
			SecureBrowser.TABLE_NAME /* Securebrowser */};

	private static final UriMatcher sURIMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	/**
	 * Let's only generate these SQL strings once, as they are used frequently
	 * Note that this isn't relevant for table creation strings, since they are
	 * used only once
	 */
	private static final String UPDATED_MESSAGE_INSERT = "insert or ignore into "
			+ Message.UPDATED_TABLE_NAME
			+ " select * from "
//			+ Message.TABLE_NAME + " where " + EmEmailContent.RECORD_ID + "=";
			+ Message.TABLE_NAME + " where " + EmEmailContent.RECORD_ID + "=?";	//PEN_TEST_SQL_INJECT_CHG

	private static final String UPDATED_MESSAGE_DELETE = "delete from "
			+ Message.UPDATED_TABLE_NAME + " where " + EmEmailContent.RECORD_ID
//			+ "=";
			+ "=?";	//PEN_TEST_SQL_INJECT_CHG

	private static final String DELETED_MESSAGE_INSERT = "insert or replace into "
			+ Message.DELETED_TABLE_NAME
			+ " select * from "
//			+ Message.TABLE_NAME + " where " + EmEmailContent.RECORD_ID + "=";
			+ Message.TABLE_NAME + " where " + EmEmailContent.RECORD_ID + "=?";	//PEN_TEST_SQL_INJECT_CHG

	private static final String DELETE_ORPHAN_BODIES = "delete from "
			+ Body.TABLE_NAME + " where " + BodyColumns.MESSAGE_KEY + " in "
			+ "(select " + BodyColumns.MESSAGE_KEY + " from " + Body.TABLE_NAME
			+ " except select " + EmEmailContent.RECORD_ID + " from "
			+ Message.TABLE_NAME + ')';

	private static final String DELETE_BODY = "delete from " + Body.TABLE_NAME
//			+ " where " + BodyColumns.MESSAGE_KEY + "=";
			+ " where " + BodyColumns.MESSAGE_KEY + "=?";	//PEN_TEST_SQL_INJECT_CHG

	private static final String ID_EQUALS = EmEmailContent.RECORD_ID + "=?";

	private static final String TRIGGER_MAILBOX_DELETE = "create trigger mailbox_delete before delete on "
			+ Mailbox.TABLE_NAME
			+ " begin"
			+ " delete from "
			+ Message.TABLE_NAME
			+ "  where "
			+ MessageColumns.MAILBOX_KEY
			+ "=old."
			+ EmEmailContent.RECORD_ID
			+ "; delete from "
			+ Message.UPDATED_TABLE_NAME
			+ "  where "
			+ MessageColumns.MAILBOX_KEY
			+ "=old."
			+ EmEmailContent.RECORD_ID
			+ "; delete from "
			+ Message.DELETED_TABLE_NAME
			+ "  where "
			+ MessageColumns.MAILBOX_KEY
			+ "=old."
			+ EmEmailContent.RECORD_ID
			+ "; end";
	
	
	static {
		// Email URI matching table
		UriMatcher matcher = sURIMatcher;

		// All accounts
		matcher.addURI(EMAIL_AUTHORITY, "account", ACCOUNT);
		// A specific account
		// insert into this URI causes a mailbox to be added to the account
		matcher.addURI(EMAIL_AUTHORITY, "account/#", ACCOUNT_ID);
		// The mailboxes in a specific account
		matcher.addURI(EMAIL_AUTHORITY, "account/#/mailbox", ACCOUNT_MAILBOXES);

		// All mailboxes
		matcher.addURI(EMAIL_AUTHORITY, "mailbox", MAILBOX);
		// A specific mailbox
		// insert into this URI causes a message to be added to the mailbox
		// ** NOTE For now, the accountKey must be set manually in the values!
		matcher.addURI(EMAIL_AUTHORITY, "mailbox/#", MAILBOX_ID);
		// The messages in a specific mailbox
		matcher.addURI(EMAIL_AUTHORITY, "mailbox/#/message", MAILBOX_MESSAGES);

		// All messages
		matcher.addURI(EMAIL_AUTHORITY, "message", MESSAGE);
		// A specific message
		// insert into this URI causes an attachment to be added to the message
		matcher.addURI(EMAIL_AUTHORITY, "message/#", MESSAGE_ID);

		// A specific attachment
		matcher.addURI(EMAIL_AUTHORITY, "attachment", ATTACHMENT);
		// A specific attachment (the header information)
		matcher.addURI(EMAIL_AUTHORITY, "attachment/#", ATTACHMENT_ID);
		// The content for a specific attachment
		// NOT IMPLEMENTED
		matcher.addURI(EMAIL_AUTHORITY, "attachment/content/*",
				ATTACHMENT_CONTENT);
		// The attachments of a specific message (query only) (insert & delete
		// TBD)
		matcher.addURI(EMAIL_AUTHORITY, "attachment/message/#",
				ATTACHMENTS_MESSAGE_ID);

		// All mail bodies
		matcher.addURI(EMAIL_AUTHORITY, "body", BODY);
		// A specific mail body
		matcher.addURI(EMAIL_AUTHORITY, "body/#", BODY_ID);
		// The body for a specific message
		matcher.addURI(EMAIL_AUTHORITY, "body/message/#", BODY_MESSAGE_ID);
		// The HTML part of a specific mail body
		matcher.addURI(EMAIL_AUTHORITY, "body/#/html", BODY_HTML);
		// The plain text part of a specific mail body
		matcher.addURI(EMAIL_AUTHORITY, "body/#/text", BODY_TEXT);

		// All hostauth records
		matcher.addURI(EMAIL_AUTHORITY, "hostauth", HOSTAUTH);
		// A specific hostauth
		matcher.addURI(EMAIL_AUTHORITY, "hostauth/#", HOSTAUTH_ID);

		// Atomically a constant value to a particular field of a
		// mailbox/account
		matcher.addURI(EMAIL_AUTHORITY, "mailboxIdAddToField/#",
				MAILBOX_ID_ADD_TO_FIELD);
		matcher.addURI(EMAIL_AUTHORITY, "accountIdAddToField/#",
				ACCOUNT_ID_ADD_TO_FIELD);

		// ******************** For Contacts

		matcher.addURI(ContactsConsts.AUTHORITY,
				ContactsConsts.CONTACTS_TABLE_NAME, CONTACT_GET_ALL);
		matcher.addURI(ContactsConsts.AUTHORITY,
				ContactsConsts.CONTACTS_TABLE_NAME + "/#", CONTACT_GET_ONE);
		matcher.addURI(ContactsConsts.AUTHORITY,
				ContactsConsts.CONTACTS_ADRESS_TABLE_NAME, CONTACT_ADRESS_TABLE);
		matcher.addURI(ContactsConsts.AUTHORITY,
				ContactsConsts.CONTACTS_SYNCSTATE_TABLE_NAME,
				CONTACT_SYNCSTATE_TABLE);
		matcher.addURI(ContactsConsts.AUTHORITY,
				ContactsConsts.CONTACTS_LOCAL_CACHE_ADD_TABLE_NAME,
				CONTACT_ADD_CACHE_ALL);
		matcher.addURI(ContactsConsts.AUTHORITY,
				ContactsConsts.CONTACTS_LOCAL_CACHE_UPDATE_TABLE_NAME,
				CONTACT_UPDATE_CACHE_ALL);
		matcher.addURI(ContactsConsts.AUTHORITY,
				ContactsConsts.CONTACTS_LOCAL_CACHE_DELETE_TABLE_NAME,
				CONTACT_DELETE_CACHE_ALL);

		// ******************** For Contacts
		/************************************************** for calendar *************************************************/
		matcher.addURI(CalendarConstants.AUTHORITY,
				CalendarConstants.TABLE_EVENTS, CALENDAR_EVENTS_TABLE);
		matcher.addURI(CalendarConstants.AUTHORITY,
				CalendarConstants.TABLE_REMINDERS, CALENDAR_REMINDERS_TABLE);
		matcher.addURI(CalendarConstants.AUTHORITY,
				CalendarConstants.TABLE_ATTENDEES, CALENDAR_ATTENDEES_TABLE);
		matcher.addURI(CalendarConstants.AUTHORITY,
				CalendarConstants.TABLE_SEARCH_HISTORY,
				CALENDAR_SEARCH_HISTORY_TABLE);
		matcher.addURI(CalendarConstants.AUTHORITY,
				CalendarConstants.TABLE_EXCEPTIONS, CALENDAR_EXCEPTIONS_TABLE);
		matcher.addURI(CalendarConstants.AUTHORITY,
				CalendarConstants.TABLE_SYNC_STATE, CALENDAR_SYNCSTATE_TABLE);

		/****************************************************************************************************************/

		// Securebrowser
		matcher.addURI(EMAIL_AUTHORITY, SecureBrowser.TABLE_NAME,
				SECURE_BROWSER);

		/**
		 * THIS URI HAS SPECIAL SEMANTICS ITS USE IS INTENDED FOR THE UI
		 * APPLICATION TO MARK CHANGES THAT NEED TO BE SYNCED BACK TO A SERVER
		 * VIA A SYNC ADAPTER
		 */
		matcher.addURI(EMAIL_AUTHORITY, "syncedMessage/#", SYNCED_MESSAGE_ID);

		/**
		 * THE URIs BELOW THIS POINT ARE INTENDED TO BE USED BY SYNC ADAPTERS
		 * ONLY THEY REFER TO DATA CREATED AND MAINTAINED BY CALLS TO THE
		 * SYNCED_MESSAGE_ID URI BY THE UI APPLICATION
		 */
		// All deleted messages
		matcher.addURI(EMAIL_AUTHORITY, "deletedMessage", DELETED_MESSAGE);
		// A specific deleted message
		matcher.addURI(EMAIL_AUTHORITY, "deletedMessage/#", DELETED_MESSAGE_ID);
		// All deleted messages from a specific mailbox
		// NOT IMPLEMENTED; do we need this as a convenience?
		matcher.addURI(EMAIL_AUTHORITY, "deletedMessage/mailbox/#",
				DELETED_MESSAGE_MAILBOX);

		// All updated messages
		matcher.addURI(EMAIL_AUTHORITY, "updatedMessage", UPDATED_MESSAGE);
		// A specific updated message
		matcher.addURI(EMAIL_AUTHORITY, "updatedMessage/#", UPDATED_MESSAGE_ID);
	}

	/*
	 * Internal helper method for index creation. Example:
	 * "create index message_" + MessageColumns.FLAG_READ + " on " +
	 * Message.TABLE_NAME + " (" + MessageColumns.FLAG_READ + ");"
	 */
	/* package */
	static String createIndex(String tableName, String columnName) {
		return "create index " + tableName.toLowerCase() + '_' + columnName
				+ " on " + tableName + " (" + columnName + ");";
	}

	static void createMessageTable(TruBoxDatabase db) {
		String messageColumns = MessageColumns.DISPLAY_NAME + " text, "
				+ MessageColumns.TIMESTAMP + " integer, "
				+ MessageColumns.SUBJECT + " text, " + MessageColumns.FLAG_READ
				+ " integer, " + MessageColumns.FLAG_LOADED + " integer, "
				+ MessageColumns.FLAG_FAVORITE + " integer, "
				+ MessageColumns.FLAG_ATTACHMENT + " integer, "
				+ MessageColumns.FLAGS + " integer, "
				+ MessageColumns.CLIENT_ID + " integer, "
				+ MessageColumns.MESSAGE_ID + " text, "
				+ MessageColumns.MAILBOX_KEY + " integer, "
				+ MessageColumns.ACCOUNT_KEY + " integer, "
				+ MessageColumns.FROM_LIST + " text, " + MessageColumns.TO_LIST
				+ " text, " + MessageColumns.CC_LIST + " text, "
				+ MessageColumns.BCC_LIST + " text, "
				+ MessageColumns.REPLY_TO_LIST + " text, "
				+ MessageColumns.MEETING_INFO + " text" + ");";

		// This String and the following String MUST have the same columns,
		// except for the type
		// of those columns!
		String createString = " (" + EmEmailContent.RECORD_ID
				+ " integer primary key autoincrement, "
				+ SyncColumns.SERVER_ID + " text, "
				+ SyncColumns.SERVER_TIMESTAMP + " integer, " + messageColumns;

		// For the updated and deleted tables, the id is assigned, but we do
		// want to keep track
		// of the ORDER of updates using an autoincrement primary key. We use
		// the DATA column
		// at this point; it has no other function
		String altCreateString = " (" + EmEmailContent.RECORD_ID
				+ " integer unique, " + SyncColumns.SERVER_ID + " text, "
				+ SyncColumns.SERVER_TIMESTAMP + " integer, " + messageColumns;

		// The three tables have the same schema
		db.execSQL("create table " + Message.TABLE_NAME + createString);
		db.execSQL("create table " + Message.UPDATED_TABLE_NAME
				+ altCreateString);
		db.execSQL("create table " + Message.DELETED_TABLE_NAME
				+ altCreateString);

		String indexColumns[] = { MessageColumns.TIMESTAMP,
				MessageColumns.FLAG_READ, MessageColumns.FLAG_LOADED,
				MessageColumns.MAILBOX_KEY, SyncColumns.SERVER_ID };

		for (String columnName : indexColumns) {
			db.execSQL(createIndex(Message.TABLE_NAME, columnName));
		}

		// Deleting a Message deletes all associated Attachments
		// Deleting the associated Body cannot be done in a trigger, because the
		// Body is stored
		// in a separate database, and trigger cannot operate on attached
		// databases.
		db.execSQL("create trigger message_delete before delete on "
				+ Message.TABLE_NAME + " begin delete from "
				+ Attachment.TABLE_NAME + "  where "
				+ AttachmentColumns.MESSAGE_KEY + "=old."
				+ EmEmailContent.RECORD_ID + "; end");

		// Add triggers to keep unread count accurate per mailbox

		// Insert a message; if flagRead is zero, add to the unread count of the
		// message's mailbox
		db.execSQL("create trigger unread_message_insert before insert on "
				+ Message.TABLE_NAME + " when NEW." + MessageColumns.FLAG_READ
				+ "=0" + " begin update " + Mailbox.TABLE_NAME + " set "
				+ MailboxColumns.UNREAD_COUNT + '='
				+ MailboxColumns.UNREAD_COUNT + "+1" + "  where "
				+ EmEmailContent.RECORD_ID + "=NEW."
				+ MessageColumns.MAILBOX_KEY + "; end");

		// Delete a message; if flagRead is zero, decrement the unread count of
		// the msg's mailbox
		db.execSQL("create trigger unread_message_delete before delete on "
				+ Message.TABLE_NAME + " when OLD." + MessageColumns.FLAG_READ
				+ "=0" + " begin update " + Mailbox.TABLE_NAME + " set "
				+ MailboxColumns.UNREAD_COUNT + '='
				+ MailboxColumns.UNREAD_COUNT + "-1" + "  where "
				+ EmEmailContent.RECORD_ID + "=OLD."
				+ MessageColumns.MAILBOX_KEY + "; end");

		// Change a message's mailbox
		db.execSQL("create trigger unread_message_move before update of "
				+ MessageColumns.MAILBOX_KEY + " on " + Message.TABLE_NAME
				+ " when OLD." + MessageColumns.FLAG_READ + "=0"
				+ " begin update " + Mailbox.TABLE_NAME + " set "
				+ MailboxColumns.UNREAD_COUNT + '='
				+ MailboxColumns.UNREAD_COUNT + "-1" + "  where "
				+ EmEmailContent.RECORD_ID + "=OLD."
				+ MessageColumns.MAILBOX_KEY + "; update " + Mailbox.TABLE_NAME
				+ " set " + MailboxColumns.UNREAD_COUNT + '='
				+ MailboxColumns.UNREAD_COUNT + "+1" + " where "
				+ EmEmailContent.RECORD_ID + "=NEW."
				+ MessageColumns.MAILBOX_KEY + "; end");

		// Change a message's read state
		db.execSQL("create trigger unread_message_read before update of "
				+ MessageColumns.FLAG_READ + " on " + Message.TABLE_NAME
				+ " when OLD." + MessageColumns.FLAG_READ + "!=NEW."
				+ MessageColumns.FLAG_READ + " begin update "
				+ Mailbox.TABLE_NAME + " set " + MailboxColumns.UNREAD_COUNT
				+ '=' + MailboxColumns.UNREAD_COUNT + "+ case OLD."
				+ MessageColumns.FLAG_READ + " when 0 then -1 else 1 end"
				+ "  where " + EmEmailContent.RECORD_ID + "=OLD."
				+ MessageColumns.MAILBOX_KEY + "; end");
	}

	static void resetMessageTable(TruBoxDatabase db, int oldVersion,
			int newVersion) {
		try {
			db.execSQL("drop table " + Message.TABLE_NAME);
			db.execSQL("drop table " + Message.UPDATED_TABLE_NAME);
			db.execSQL("drop table " + Message.DELETED_TABLE_NAME);
		} catch (SQLException e) {
		}
		createMessageTable(db);
	}

	static void createAccountTable(TruBoxDatabase db) {
		String s = " (" + EmEmailContent.RECORD_ID
				+ " integer primary key autoincrement, "
				+ AccountColumns.DISPLAY_NAME + " text, "
				+ AccountColumns.EMAIL_ADDRESS + " text, "
				+ AccountColumns.SYNC_KEY + " text, "
				+ AccountColumns.SYNC_LOOKBACK + " integer, "
				+ AccountColumns.SYNC_INTERVAL + " text, "
				+ AccountColumns.HOST_AUTH_KEY_RECV + " integer, "
				+ AccountColumns.HOST_AUTH_KEY_SEND + " integer, "
				+ AccountColumns.FLAGS + " integer, "
				+ AccountColumns.IS_DEFAULT + " integer, "
				+ AccountColumns.COMPATIBILITY_UUID + " text, "
				+ AccountColumns.SENDER_NAME + " text, "
				+ AccountColumns.RINGTONE_URI + " text, "
				+ AccountColumns.PROTOCOL_VERSION + " text, "
				+ AccountColumns.NEW_MESSAGE_COUNT + " integer, "
				+ AccountColumns.SECURITY_FLAGS + " integer, "
				+ AccountColumns.SECURITY_SYNC_KEY + " text, "
				+ AccountColumns.SIGNATURE + " text " + ");";
		db.execSQL("create table " + Account.TABLE_NAME + s);
		// Deleting an account deletes associated Mailboxes and HostAuth's
		db.execSQL("create trigger account_delete before delete on "
				+ Account.TABLE_NAME + " begin delete from "
				+ Mailbox.TABLE_NAME + " where " + MailboxColumns.ACCOUNT_KEY
				+ "=old." + EmEmailContent.RECORD_ID + "; delete from "
				+ HostAuth.TABLE_NAME + " where " + EmEmailContent.RECORD_ID
				+ "=old." + AccountColumns.HOST_AUTH_KEY_RECV
				+ "; delete from " + HostAuth.TABLE_NAME + " where "
				+ EmEmailContent.RECORD_ID + "=old."
				+ AccountColumns.HOST_AUTH_KEY_SEND + "; end");
		
	}

	static void resetAccountTable(TruBoxDatabase db, int oldVersion,
			int newVersion) {
		try {
			db.execSQL("drop table " + Account.TABLE_NAME);
		} catch (SQLException e) {
		}
		createAccountTable(db);
	}

	static void createHostAuthTable(TruBoxDatabase db) {
		String s = " (" + EmEmailContent.RECORD_ID
				+ " integer primary key autoincrement, "
				+ HostAuthColumns.PROTOCOL + " text, "
				+ HostAuthColumns.ADDRESS + " text, " + HostAuthColumns.PORT
				+ " integer, " + HostAuthColumns.FLAGS + " integer, "
				+ HostAuthColumns.LOGIN + " text, " + HostAuthColumns.PASSWORD
				+ " text, " + HostAuthColumns.DOMAIN + " text, "
				+ HostAuthColumns.ACCOUNT_KEY + " integer" + ");";
		db.execSQL("create table " + HostAuth.TABLE_NAME + s);
	}

	static void resetHostAuthTable(TruBoxDatabase db, int oldVersion,
			int newVersion) {
		try {
			db.execSQL("drop table " + HostAuth.TABLE_NAME);
		} catch (SQLException e) {
		}
		createHostAuthTable(db);
	}

	static void createMailboxTable(TruBoxDatabase db) {
		String s = " (" + EmEmailContent.RECORD_ID
				+ " integer primary key autoincrement, "
				+ MailboxColumns.DISPLAY_NAME + " text, "
				+ MailboxColumns.SERVER_ID + " text, "
				+ MailboxColumns.PARENT_SERVER_ID + " text, "
				+ MailboxColumns.ACCOUNT_KEY + " integer, "
				+ MailboxColumns.TYPE + " integer, " + MailboxColumns.DELIMITER
				+ " integer, " + MailboxColumns.SYNC_KEY + " text, "
				+ MailboxColumns.SYNC_LOOKBACK + " integer, "
				+ MailboxColumns.SYNC_INTERVAL + " integer, "
				+ MailboxColumns.SYNC_TIME + " integer, "
				+ MailboxColumns.UNREAD_COUNT + " integer, "
				+ MailboxColumns.FLAG_VISIBLE + " integer, "
				+ MailboxColumns.FLAGS + " integer, "
				+ MailboxColumns.VISIBLE_LIMIT + " integer, "
				+ MailboxColumns.SYNC_STATUS + " text" + ");";
		db.execSQL("create table " + Mailbox.TABLE_NAME + s);
		db.execSQL("create index mailbox_" + MailboxColumns.SERVER_ID + " on "
				+ Mailbox.TABLE_NAME + " (" + MailboxColumns.SERVER_ID + ")");
		db.execSQL("create index mailbox_" + MailboxColumns.ACCOUNT_KEY
				+ " on " + Mailbox.TABLE_NAME + " ("
				+ MailboxColumns.ACCOUNT_KEY + ")");
		// Deleting a Mailbox deletes associated Messages in all three tables
		db.execSQL(TRIGGER_MAILBOX_DELETE);
	}

	static void resetMailboxTable(TruBoxDatabase db, int oldVersion,
			int newVersion) {
		try {
			db.execSQL("drop table " + Mailbox.TABLE_NAME);
		} catch (SQLException e) {
		}
		createMailboxTable(db);
	}

	// 3-9-2013
	static void createSecureBrowserTable(TruBoxDatabase db) {
		String s = " (" + EmEmailContent.RECORD_ID
				+ " integer primary key autoincrement, "
				+ SecureBrowserColumns.FILENAME + " text, "
				+ SecureBrowserColumns.MIME_TYPE + " text, "
				+ SecureBrowserColumns.SIZE + " text, "
				+ SecureBrowserColumns.CONTENT_URI + " text, "
				+ SecureBrowserColumns.DATE_TIME + " text, "
				+ SecureBrowserColumns.URL + " text, "
				+ SecureBrowserColumns.CONTAINER_BOOKMARK
				+ " boolean not null default false, "
				+ SecureBrowserColumns.CONTAINER_DELETE
				+ " boolean not null default '0' " + ");";
		db.execSQL("create table " + SecureBrowser.TABLE_NAME + s);
		db.execSQL(createIndex(SecureBrowser.TABLE_NAME,
				SecureBrowserColumns.ID));
	}

	// 3-9-2013

	static void createAttachmentTable(TruBoxDatabase db) {
		String s = " (" + EmEmailContent.RECORD_ID
				+ " integer primary key autoincrement, "
				+ AttachmentColumns.FILENAME + " text, "
				+ AttachmentColumns.MIME_TYPE + " text, "
				+ AttachmentColumns.SIZE + " integer, "
				+ AttachmentColumns.CONTENT_ID + " text, "
				+ AttachmentColumns.CONTENT_URI + " text, "
				+ AttachmentColumns.MESSAGE_KEY + " integer, "
				+ AttachmentColumns.LOCATION + " text, "
				+ AttachmentColumns.ENCODING + " text, "
				+ AttachmentColumns.CONTENT + " text, "
				+ AttachmentColumns.FLAGS
				+ " integer, "
				+ AttachmentColumns.CONTAINER_BOOKMARK
				+ " boolean not null default false, "// CONTAINER CHANGES
				+ AttachmentColumns.CONTAINER_DELETE
				+ " boolean not null default '0', "// CONTAINER CHANGES ENDS
				+ AttachmentColumns.CONTENT_BYTES + " blob" + ");";
		db.execSQL("create table " + Attachment.TABLE_NAME + s);
		db.execSQL(createIndex(Attachment.TABLE_NAME,
				AttachmentColumns.MESSAGE_KEY));
	}

	static void resetAttachmentTable(TruBoxDatabase db, int oldVersion,
			int newVersion) {
		try {
			db.execSQL("drop table " + Attachment.TABLE_NAME);
		} catch (SQLException e) {
		}
		createAttachmentTable(db);
	}

	static void createBodyTable(TruBoxDatabase db) {
		String s = " (" + EmEmailContent.RECORD_ID
				+ " integer primary key autoincrement, "
				+ BodyColumns.MESSAGE_KEY + " integer, "
				+ BodyColumns.HTML_CONTENT + " text, "
				+ BodyColumns.TEXT_CONTENT + " text, " + BodyColumns.HTML_REPLY
				+ " text, " + BodyColumns.TEXT_REPLY + " text, "
				+ BodyColumns.SOURCE_MESSAGE_KEY + " text, "
				+ BodyColumns.INTRO_TEXT + " text" + ");";
		db.execSQL("create table " + Body.TABLE_NAME + s);
		db.execSQL(createIndex(Body.TABLE_NAME, BodyColumns.MESSAGE_KEY));
	}

	static void upgradeBodyTable(TruBoxDatabase db, int oldVersion,
			int newVersion) {
		if (oldVersion < 5) {
			try {
				db.execSQL("drop table " + Body.TABLE_NAME);
				createBodyTable(db);
			} catch (SQLException e) {
			}
		} else if (oldVersion == 5) {
			try {
				db.execSQL("alter table " + Body.TABLE_NAME + " add "
						+ BodyColumns.INTRO_TEXT + " text");
			} catch (SQLException e) {
				// Shouldn't be needed unless we're debugging and interrupt the
				// process
				Log.w(TAG,
						"Exception upgrading EmailProviderBody.db from v5 to v6",
						e);
			}
			oldVersion = 6;
		}
	}

	// private SQLiteDatabase mDatabase;
	// private SQLiteDatabase mBodyDatabase;

	// CONTAINER CHANGES
	private static TruBoxDatabase mstaticDatabase;

	public static TruBoxDatabase getDB() {
		return mstaticDatabase;
	}

	// CONTAINER CHANGES ENDS

	private TruBoxDatabase mDatabase;
	private TruBoxDatabase mBodyDatabase;

	public synchronized TruBoxDatabase getDatabase(Context context) {
		// Always return the cached database, if we've got one

		if ((isSuccessLogin(context) == true)) {

			if (mDatabase != null) {
				mstaticDatabase = mDatabase;// CONTAINER CHANGES ENDS

				// Changes for wipe data. After wiping data, mDatabase will not
				// be null so checking whether db exists
				// 290661 changes
				File dbFile = new File(mDatabase.getPath());
				if (dbFile.exists())
					return mDatabase;
				// 290661 changes end

			}

			// Whenever we create or re-cache the databases, make sure that we
			// haven't lost one
			// to corruption
			checkDatabases();

			DatabaseHelper helper = new DatabaseHelper(context, DATABASE_NAME);
			mDatabase = helper.getWritableDatabase();
			if (mDatabase != null) {
				mDatabase.setLockingEnabled(true);
				BodyDatabaseHelper bodyHelper = new BodyDatabaseHelper(context,
						BODY_DATABASE_NAME);
				mBodyDatabase = bodyHelper.getWritableDatabase();
				if (mBodyDatabase != null) {
					mBodyDatabase.setLockingEnabled(true);
					String bodyFileName = mBodyDatabase.getPath();
					mDatabase.execSQL("attach \"" + bodyFileName
							+ "\" as BodyDatabase");
				}
			}

			// Check for any orphaned Messages in the updated/deleted tables
			deleteOrphans(mDatabase, Message.UPDATED_TABLE_NAME);
			deleteOrphans(mDatabase, Message.DELETED_TABLE_NAME);

			mstaticDatabase = mDatabase;// CONTAINER CHANGES ENDS
		}
		return mDatabase;
	}

	/*
	 * package static SQLiteDatabase getReadableDatabase(Context context) {
	 * DatabaseHelper helper = new EmailProvider().new DatabaseHelper(context,
	 * DATABASE_NAME); return helper.getReadableDatabase(); }
	 */

	/* package */static void deleteOrphans(TruBoxDatabase database,
			String tableName) {
		if (database != null) {
			// We'll look at all of the items in the table; there won't be many
			// typically
			Cursor c = database.query(tableName, ORPHANS_PROJECTION, null,
					null, null, null, null);
			// Usually, there will be nothing in these tables, so make a quick
			// check
			try {
				if (c.getCount() == 0)
					return;
				ArrayList<Long> foundMailboxes = new ArrayList<Long>();
				ArrayList<Long> notFoundMailboxes = new ArrayList<Long>();
				ArrayList<Long> deleteList = new ArrayList<Long>();
				String[] bindArray = new String[1];
				while (c.moveToNext()) {
					// Get the mailbox key and see if we've already found this
					// mailbox
					// If so, we're fine
					long mailboxId = c.getLong(ORPHANS_MAILBOX_KEY);
					// If we already know this mailbox doesn't exist, mark the
					// message for deletion
					if (notFoundMailboxes.contains(mailboxId)) {
						deleteList.add(c.getLong(ORPHANS_ID));
						// If we don't know about this mailbox, we'll try to
						// find it
					} else if (!foundMailboxes.contains(mailboxId)) {
						bindArray[0] = Long.toString(mailboxId);
						Cursor boxCursor = database.query(Mailbox.TABLE_NAME,
								Mailbox.ID_PROJECTION, WHERE_ID, bindArray,
								null, null, null);
						try {
							// If it exists, we'll add it to the "found"
							// mailboxes
							if (boxCursor.moveToFirst()) {
								foundMailboxes.add(mailboxId);
								// Otherwise, we'll add to "not found" and mark
								// the message for deletion
							} else {
								notFoundMailboxes.add(mailboxId);
								deleteList.add(c.getLong(ORPHANS_ID));
							}
						} finally {
							boxCursor.close();
						}
					}
				}
				// Now, delete the orphan messages
				for (long messageId : deleteList) {
					bindArray[0] = Long.toString(messageId);
					database.delete(tableName, WHERE_ID, bindArray);
				}
			} finally {
				c.close();
			}
		}
	}

	private class BodyDatabaseHelper {

		TruBoxDatabase bodyDBEncryption;
		private Context mContext;

		BodyDatabaseHelper(Context context, String name) {
			// super(context, name, null, BODY_DATABASE_VERSION);
			mContext = context;
			TruBoxDatabase.initialiseTruBoxDatabase(mContext);
			try {
				File bodyDBFile = mContext.getDatabasePath(name);
				if (!bodyDBFile.getParentFile().exists()) {
					bodyDBFile.getParentFile().mkdirs();
				}

				bodyDBEncryption = TruBoxDatabase.openOrCreateDatabase(
						bodyDBFile, null);

				if (bodyDBEncryption != null) {
					onCreate(bodyDBEncryption);
				}
			} catch (Exception e) {
				Log.d(TAG, "failed to create database " + e);
			}
		}

		// @Override
		public void onCreate(TruBoxDatabase db) {
			Log.d(TAG, "Creating EmailProviderBody database");
			createBodyTable(db);
		}

		// @Override
		public void onUpgrade(TruBoxDatabase db, int oldVersion, int newVersion) {
			upgradeBodyTable(db, oldVersion, newVersion);
		}

		// @Override
		public void onOpen(TruBoxDatabase db) {
		}

		public TruBoxDatabase getWritableDatabase() {
			// TODO Auto-generated method stub
			return bodyDBEncryption;
			// return SQLiteDatabase.openDatabase(mBodyDBName, null,
			// SQLiteDatabase.NO_LOCALIZED_COLLATORS);
		}
	}

	private class DatabaseHelper {
		TruBoxDatabase dbEncryption;
		Context mContext;
		String mDBName;

		DatabaseHelper(Context context, String name) {
			// super(context, name, null, DATABASE_VERSION);
			mContext = context;
			mDBName = name;
			// SQLiteDatabase.initialiSQLiteDatabasese(mContext);
			try {
				File dbFile = mContext.getDatabasePath(name);
				// File dbFile = new File(
				// Environment.getExternalStorageDirectory() + "/" + name);

				if (!dbFile.getParentFile().exists()) {
					dbFile.getParentFile().mkdirs();
				}

				dbEncryption = TruBoxDatabase
						.openOrCreateDatabase(dbFile, null);

				if (dbEncryption != null) {
					onCreate(dbEncryption);
				}

			} catch (Exception e) {
				Log.d(TAG, "Failed to open file: " + e);
			}
		}

		void createTablesForCalendar(TruBoxDatabase db) {
			db.execSQL(CalendarConstants.CREATE_TABLE_EVENTS);
			db.execSQL(CalendarConstants.CREATE_TABLE_EXCEPTIONS);
			db.execSQL(CalendarConstants.CREATE_TABLE_ATTENDEES);
			db.execSQL(CalendarConstants.CREATE_TABLE_REMINDERS);
			db.execSQL(CalendarConstants.CREATE_TABLE_SEARCH_HISTORY);
			db.execSQL(CalendarConstants.CREATE_TABLE_SYNC_STATE);
		}

		// @Override
		public void onCreate(TruBoxDatabase db) {
			Log.d(TAG, "Creating EmailProvider database");
			// Create all tables here; each class has its own method
			createTablesForCalendar(db);
			createContactTable(db);
			createMessageTable(db);
			createAttachmentTable(db);
			createMailboxTable(db);
			createHostAuthTable(db);
			createAccountTable(db);
			createSecureBrowserTable(db);// 3-9-2013
		}

		void createContactTable(TruBoxDatabase db) {

			Log.d(TAG, "Creating ContactTable database");
			db.execSQL(ContactsDbHelper.CREATE_CONTACT_TABLE);
			db.execSQL(ContactsDbHelper.CREATE_ADRESS_TABLE);
			db.execSQL(ContactsDbHelper.CREATE_SYNCSTATE_TABLE);
			db.execSQL(ContactsDbHelper.CONTACTS_LOCAL_CACHE_TABLE_ADD);
			db.execSQL(ContactsDbHelper.CONTACTS_LOCAL_CACHE_TABLE_UPDATE);
			db.execSQL(ContactsDbHelper.CONTACTS_LOCAL_CACHE_TABLE_DELETE);

		}

		// @Override
		public void onUpgrade(TruBoxDatabase db, int oldVersion, int newVersion) {
			// For versions prior to 5, delete all data
			// Versions >= 5 require that data be preserved!
			if (oldVersion < 5) {
				android.accounts.Account[] accounts = AccountManager.get(
						mContext).getAccountsByType(
						Email.EXCHANGE_ACCOUNT_MANAGER_TYPE);
				for (android.accounts.Account account : accounts) {
					AccountManager.get(mContext).removeAccount(account, null,
							null);
				}
				resetMessageTable(db, oldVersion, newVersion);
				resetAttachmentTable(db, oldVersion, newVersion);
				resetMailboxTable(db, oldVersion, newVersion);
				resetHostAuthTable(db, oldVersion, newVersion);
				resetAccountTable(db, oldVersion, newVersion);
				return;
			}
			if (oldVersion == 5) {
				// Message Tables: Add SyncColumns.SERVER_TIMESTAMP
				try {
					db.execSQL("alter table " + Message.TABLE_NAME
							+ " add column " + SyncColumns.SERVER_TIMESTAMP
							+ " integer" + ";");
					db.execSQL("alter table " + Message.UPDATED_TABLE_NAME
							+ " add column " + SyncColumns.SERVER_TIMESTAMP
							+ " integer" + ";");
					db.execSQL("alter table " + Message.DELETED_TABLE_NAME
							+ " add column " + SyncColumns.SERVER_TIMESTAMP
							+ " integer" + ";");
				} catch (SQLException e) {
					// Shouldn't be needed unless we're debugging and interrupt
					// the process
					Log.w(TAG,
							"Exception upgrading EmailProvider.db from v5 to v6",
							e);
				}
				oldVersion = 6;
			}
			if (oldVersion == 6) {
				// Use the newer mailbox_delete trigger
				db.execSQL("drop trigger mailbox_delete;");
				db.execSQL(TRIGGER_MAILBOX_DELETE);
				oldVersion = 7;
			}
			if (oldVersion == 7) {
				// add the security (provisioning) column
				try {
					db.execSQL("alter table " + Account.TABLE_NAME
							+ " add column " + AccountColumns.SECURITY_FLAGS
							+ " integer" + ";");
				} catch (SQLException e) {
					// Shouldn't be needed unless we're debugging and interrupt
					// the process
					Log.w(TAG,
							"Exception upgrading EmailProvider.db from 7 to 8 "
									+ e);
				}
				oldVersion = 8;
			}
			if (oldVersion == 8) {
				// accounts: add security sync key & user signature columns
				try {
					db.execSQL("alter table " + Account.TABLE_NAME
							+ " add column " + AccountColumns.SECURITY_SYNC_KEY
							+ " text" + ";");
					db.execSQL("alter table " + Account.TABLE_NAME
							+ " add column " + AccountColumns.SIGNATURE
							+ " text" + ";");
				} catch (SQLException e) {
					// Shouldn't be needed unless we're debugging and interrupt
					// the process
					Log.w(TAG,
							"Exception upgrading EmailProvider.db from 8 to 9 "
									+ e);
				}
				oldVersion = 9;
			}
			if (oldVersion == 9) {
				// Message: add meeting info column into Message tables
				try {
					db.execSQL("alter table " + Message.TABLE_NAME
							+ " add column " + MessageColumns.MEETING_INFO
							+ " text" + ";");
					db.execSQL("alter table " + Message.UPDATED_TABLE_NAME
							+ " add column " + MessageColumns.MEETING_INFO
							+ " text" + ";");
					db.execSQL("alter table " + Message.DELETED_TABLE_NAME
							+ " add column " + MessageColumns.MEETING_INFO
							+ " text" + ";");
				} catch (SQLException e) {
					// Shouldn't be needed unless we're debugging and interrupt
					// the process
					Log.w(TAG,
							"Exception upgrading EmailProvider.db from 9 to 10 "
									+ e);
				}
				oldVersion = 10;
			}
			if (oldVersion == 10) {
				// Attachment: add content and flags columns
				try {
					db.execSQL("alter table " + Attachment.TABLE_NAME
							+ " add column " + AttachmentColumns.CONTENT
							+ " text" + ";");
					db.execSQL("alter table " + Attachment.TABLE_NAME
							+ " add column " + AttachmentColumns.FLAGS
							+ " integer" + ";");
				} catch (SQLException e) {
					// Shouldn't be needed unless we're debugging and interrupt
					// the process
					Log.w(TAG,
							"Exception upgrading EmailProvider.db from 10 to 11 "
									+ e);
				}
				oldVersion = 11;
			}
			if (oldVersion == 11) {
				// Attachment: add content_bytes
				try {
					db.execSQL("alter table " + Attachment.TABLE_NAME
							+ " add column " + AttachmentColumns.CONTENT_BYTES
							+ " blob" + ";");
				} catch (SQLException e) {
					// Shouldn't be needed unless we're debugging and interrupt
					// the process
					Log.w(TAG,
							"Exception upgrading EmailProvider.db from 11 to 12 "
									+ e);
				}
				oldVersion = 12;
			}
		}

		// @Override
		public void onOpen(TruBoxDatabase db) {
		}

		public TruBoxDatabase getWritableDatabase() {
			// TODO Auto-generated method stub
			return dbEncryption;
			// return SQLiteDatabase.openDatabase(mDBName, null,
			// SQLiteDatabase.NO_LOCALIZED_COLLATORS);
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		final int match = sURIMatcher.match(uri);
		Context context = getContext();
		// Pick the correct database for this operation
		// If we're in a transaction already (which would happen during
		// applyBatch), then the
		// body database is already attached to the email database and any
		// attempt to use the
		// body database directly will result in a SQLiteException (the database
		// is locked)
		TruBoxDatabase db = getDatabase(context);
		if (db == null)
			return -1;

		int table = match >> BASE_SHIFT;
		String tableName = TABLE_NAMES[table];
		String id = "0";
		boolean messageDeletion = false;

		if (Email.LOGD) {
			Log.v(TAG, "EmailProvider.delete: uri=" + uri + ", match is "
					+ match);
		}

		int result = -1;

		try {
			switch (match) {
			// These are cases in which one or more Messages might get deleted,
			// either by
			// cascade or explicitly
			case MAILBOX_ID:
			case MAILBOX:
			case ACCOUNT_ID:
			case ACCOUNT:
			case MESSAGE:
			case SYNCED_MESSAGE_ID:
			case MESSAGE_ID:
				// Handle lost Body records here, since this cannot be done in a
				// trigger
				// The process is:
				// 1) Begin a transaction, ensuring that both databases are
				// affected atomically
				// 2) Do the requested deletion, with cascading deletions
				// handled in triggers
				// 3) End the transaction, committing all changes atomically
				//
				// Bodies are auto-deleted here; Attachments are auto-deleted
				// via trigger

				messageDeletion = true;
				db.beginTransaction();
				break;
			}
			switch (match) {

			case BODY_ID:
			case DELETED_MESSAGE_ID:
			case SYNCED_MESSAGE_ID:
			case MESSAGE_ID:
			case UPDATED_MESSAGE_ID:
			case ATTACHMENT_ID:
			case MAILBOX_ID:
			case ACCOUNT_ID:
			case HOSTAUTH_ID:
				id = uri.getPathSegments().get(1);
				if (match == SYNCED_MESSAGE_ID) {
					// For synced messages, first copy the old message to the
					// deleted table and
					// delete it from the updated table (in case it was updated
					// first)
					// Note that this is all within a transaction, for atomicity
//					db.execSQL(DELETED_MESSAGE_INSERT + id);
					db.execSQL(DELETED_MESSAGE_INSERT , new String[] {id});	//PEN_TEST_SQL_INJECT_CHG
//					db.execSQL(UPDATED_MESSAGE_DELETE + id);
					db.execSQL(UPDATED_MESSAGE_DELETE , new String[] {id});	//PEN_TEST_SQL_INJECT_CHG
				}
				result = db.delete(TABLE_NAMES[table],
						whereWithId(id, selection), selectionArgs);
				break;
			case ATTACHMENTS_MESSAGE_ID:
				// All attachments for the given message
				id = uri.getPathSegments().get(2);
				result = db
						.delete(TABLE_NAMES[table],
								whereWith(Attachment.MESSAGE_KEY + "=" + id,
										selection), selectionArgs);
				break;

			case BODY:
			case MESSAGE:
			case DELETED_MESSAGE:
			case UPDATED_MESSAGE:
			case ATTACHMENT:
			case MAILBOX:
			case ACCOUNT:
			case HOSTAUTH:
				result = db
						.delete(TABLE_NAMES[table], selection, selectionArgs);
				break;
			case CONTACT_ADD_CACHE_ALL:
			case CONTACT_DELETE_CACHE_ALL:
			case CONTACT_UPDATE_CACHE_ALL:
			case CONTACT_GET_ALL:
			case CONTACT_GET_ONE:
			case CONTACT_ADRESS_TABLE:
				result = db.delete(TABLE_NAMES[table], selection, selectionArgs);
				try {
					Intent i = new Intent();
					i.setAction("com.cognizant.trumobi.contact.sync_completed");
					Email.getAppContext().sendBroadcast(i);
				} catch (Exception e) {
					Log.e(EmEmailProvider.class.getCanonicalName(),
							e.toString());
				}

				Log.d("EmailProvider", "tableName " + tableName);

				break;
			case CALENDAR_EVENTS_TABLE:
			case CALENDAR_REMINDERS_TABLE:
			case CALENDAR_ATTENDEES_TABLE:
			case CALENDAR_SEARCH_HISTORY_TABLE:
			case CALENDAR_EXCEPTIONS_TABLE:
			case CALENDAR_SYNCSTATE_TABLE:
				result = db.delete(tableName, selection, selectionArgs);
				Log.d(TAG, "table name of delete: " + tableName);
				break;

			case SECURE_BROWSER:
				Log.i("SecureBrowser", " case secure browser update "
						+ tableName);
				result = db.delete(tableName, selection, selectionArgs);
				break;

			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
			}
			if (messageDeletion) {
				if (match == MESSAGE_ID) {
					// Delete the Body record associated with the deleted
					// message
//					db.execSQL(DELETE_BODY + id);
					db.execSQL(DELETE_BODY ,new String[] {id});	//PEN_TEST_SQL_INJECT_CHG
				} else {
					// Delete any orphaned Body records
					db.execSQL(DELETE_ORPHAN_BODIES);
				}
				db.setTransactionSuccessful();
			}
		} catch (SQLiteException e) {
			checkDatabases();
			throw e;
		} finally {
			if (messageDeletion) {
				db.endTransaction();
			}
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return result;
	}

	@Override
	// Use the email- prefix because message, mailbox, and account are so
	// generic (e.g. SMS, IM)
	public String getType(Uri uri) {
		int match = sURIMatcher.match(uri);
		switch (match) {
		case BODY_ID:
			return "vnd.android.cursor.item/email-body";
		case BODY:
			return "vnd.android.cursor.dir/email-message";
		case UPDATED_MESSAGE_ID:
		case MESSAGE_ID:
			return "vnd.android.cursor.item/email-message";
		case MAILBOX_MESSAGES:
		case UPDATED_MESSAGE:
		case MESSAGE:
			return "vnd.android.cursor.dir/email-message";
		case ACCOUNT_MAILBOXES:
		case MAILBOX:
			return "vnd.android.cursor.dir/email-mailbox";
		case MAILBOX_ID:
			return "vnd.android.cursor.item/email-mailbox";
		case ACCOUNT:
			return "vnd.android.cursor.dir/email-account";
		case ACCOUNT_ID:
			return "vnd.android.cursor.item/email-account";
		case ATTACHMENTS_MESSAGE_ID:
		case ATTACHMENT:
			return "vnd.android.cursor.dir/email-attachment";
		case ATTACHMENT_ID:
			return "vnd.android.cursor.item/email-attachment";
		case HOSTAUTH:
			return "vnd.android.cursor.dir/email-hostauth";
		case HOSTAUTH_ID:
			return "vnd.android.cursor.item/email-hostauth";

		case CONTACT_ADD_CACHE_ONE:
		case CONTACT_DELETE_CACHE_ONE:
		case CONTACT_UPDATE_CACHE_ONE:
		case CONTACT_GET_ONE:
			return ContactsConsts.SINGLE_CONTACT_MIME_TYPE;
		case CONTACT_GET_ALL:
		case CONTACT_ADRESS_TABLE:
		case CONTACT_SYNCSTATE_TABLE:
		case CONTACT_ADD_CACHE_ALL:
		case CONTACT_UPDATE_CACHE_ALL:
		case CONTACT_DELETE_CACHE_ALL:
			return ContactsConsts.CONTACTS_MIME_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int match = sURIMatcher.match(uri);
		Context context = getContext();
		// See the comment at delete(), above
		TruBoxDatabase db = getDatabase(context);
		if (db == null)
			return null;

		int table = match >> BASE_SHIFT;
		String tableName = TABLE_NAMES[table];
		long id;

		if (Email.LOGD) {
			Log.v(TAG, "EmailProvider.insert: uri=" + uri + ", match is "
					+ match);
		}

		Uri resultUri = null;

		try {
			switch (match) {
			case UPDATED_MESSAGE:
			case DELETED_MESSAGE:
			case BODY:
			case MESSAGE:
			case ATTACHMENT:
			case MAILBOX:
			case ACCOUNT:
			case HOSTAUTH:
				id = db.insert(TABLE_NAMES[table], "foo", values);
				resultUri = ContentUris.withAppendedId(uri, id);
				// Clients shouldn't normally be adding rows to these tables, as
				// they are
				// maintained by triggers. However, we need to be able to do
				// this for unit
				// testing, so we allow the insert and then throw the same
				// exception that we
				// would if this weren't allowed.
				if (match == UPDATED_MESSAGE || match == DELETED_MESSAGE) {
					throw new IllegalArgumentException("Unknown URL " + uri);
				}
				break;
			case MAILBOX_ID:
				// This implies adding a message to a mailbox
				// Hmm, a problem here is that we can't link the account as
				// well, so it must be
				// already in the values...
				id = Long.parseLong(uri.getPathSegments().get(1));
				values.put(MessageColumns.MAILBOX_KEY, id);
				resultUri = insert(Message.CONTENT_URI, values);
				break;
			case MESSAGE_ID:
				// This implies adding an attachment to a message.
				id = Long.parseLong(uri.getPathSegments().get(1));
				values.put(AttachmentColumns.MESSAGE_KEY, id);
				resultUri = insert(Attachment.CONTENT_URI, values);
				break;
			case ACCOUNT_ID:
				// This implies adding a mailbox to an account.
				id = Long.parseLong(uri.getPathSegments().get(1));
				values.put(MailboxColumns.ACCOUNT_KEY, id);
				resultUri = insert(Mailbox.CONTENT_URI, values);
				break;
			case ATTACHMENTS_MESSAGE_ID:
				id = db.insert(TABLE_NAMES[table], "foo", values);
				resultUri = ContentUris.withAppendedId(Attachment.CONTENT_URI,
						id);
			case CONTACT_ADD_CACHE_ALL:
			case CONTACT_DELETE_CACHE_ALL:
			case CONTACT_UPDATE_CACHE_ALL:
			case CONTACT_GET_ALL:
			case CONTACT_ADRESS_TABLE:
			case CONTACT_SYNCSTATE_TABLE:
				try {
					id = db.insert(TABLE_NAMES[table], null, values);
					if (id > 0)
						resultUri = ContentUris.withAppendedId(uri, id);
				} catch (Exception e) {
					Log.e(EmEmailProvider.class.getCanonicalName(), "longId: "
							+ e.toString());
				}

				try {
					Intent i = new Intent();
					i.setAction("com.cognizant.trumobi.contact.sync_completed");
					Email.getAppContext().sendBroadcast(i);
				} catch (Exception e) {
					Log.e(EmEmailProvider.class.getCanonicalName(),
							e.toString());
				}

				break;
			case CALENDAR_EVENTS_TABLE:
			case CALENDAR_REMINDERS_TABLE:
			case CALENDAR_ATTENDEES_TABLE:
			case CALENDAR_SEARCH_HISTORY_TABLE:
			case CALENDAR_EXCEPTIONS_TABLE:
			case CALENDAR_SYNCSTATE_TABLE:
				id = db.insert(tableName, null, values);
				Log.d(TAG, "table name of insert: " + tableName);
				resultUri = ContentUris.withAppendedId(uri, id);
				break;
			default:
				throw new IllegalArgumentException("Unknown URL " + uri);
			}
		} catch (SQLiteException e) {
			checkDatabases();
			throw e;
		}

		// Notify with the base uri, not the new uri (nobody is watching a new
		// record)
		getContext().getContentResolver().notifyChange(uri, null);
		return resultUri;
	}

	@Override
	public boolean onCreate() {
		checkDatabases();
		return false;
	}

	/**
	 * The idea here is that the two databases (EmailProvider.db and
	 * EmailProviderBody.db must always be in sync (i.e. there are two database
	 * or NO databases). This code will delete any "orphan" database, so that
	 * both will be created together. Note that an "orphan" database will exist
	 * after either of the individual databases is deleted due to data
	 * corruption.
	 */
	public void checkDatabases() {
		// Uncache the databases

		if (mDatabase != null) {
			mDatabase = null;
		}
		if (mBodyDatabase != null) {
			mBodyDatabase = null;
		}
		// Look for orphans, and delete as necessary; these must always be in
		// sync
		File databaseFile = getContext().getDatabasePath(DATABASE_NAME);
		File bodyFile = getContext().getDatabasePath(BODY_DATABASE_NAME);

		// TODO Make sure attachments are deleted
		if (databaseFile.exists() && !bodyFile.exists()) {
			Log.w(TAG, "Deleting orphaned EmailProvider database...");
			databaseFile.delete();
		} else if (bodyFile.exists() && !databaseFile.exists()) {
			Log.w(TAG, "Deleting orphaned EmailProviderBody database...");
			bodyFile.delete();
		}
	}

	// Securebrowser changes
	@Override
	public int bulkInsert(Uri uri, ContentValues[] values) {

		int match = sURIMatcher.match(uri);
		Context context = getContext();
		ContentResolver resolver = context.getContentResolver();

		TruBoxDatabase db = getDatabase(context);
		int table = match >> BASE_SHIFT;

		String id = "0";
		long rowId;
		int rowsAdded = 0;
		int numInserted = 0;

		Log.i("SecureBrowser", "table : " + table);
		Log.i("SecureBrowser", "match : " + match);
		Log.i("SecureBrowser", "SWITCH  : " + TABLE_NAMES[table] + "   "
				+ SECURE_BROWSER);
		try {
			switch (match) {
			case UPDATED_MESSAGE:
			case DELETED_MESSAGE:
			case MESSAGE:
			case MAILBOX:

				break;

			case SECURE_BROWSER:

				Log.i("SecureBrowser", " case secure browser");
				db.beginTransaction();
				try {

					for (ContentValues cv : values) {

						rowId = db.insert(TABLE_NAMES[table], null, cv);
						Log.i("Email Client Bulk ", ": " + rowId);
						if (rowId > 0)
							rowsAdded++;
					}
					Log.e("Email Client Bulk ", ": " + rowsAdded);
					db.setTransactionSuccessful();
					numInserted = values.length;
				} finally {
					db.endTransaction();
				}
				break;

			default:
				throw new IllegalArgumentException("Unknown URL " + uri);
			}
		} catch (SQLiteException e) {
			throw e;
		}

		// Notify all notifier cursors
		getContext().getContentResolver().notifyChange(uri, null);

		// Notify all existing cursors.
		resolver.notifyChange(EmEmailContent.CONTENT_URI, null);
		return numInserted;

	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Cursor c = null;
		Uri notificationUri = EmEmailContent.CONTENT_URI;
		int match = sURIMatcher.match(uri);
		Context context = getContext();
		// See the comment at delete(), above
		TruBoxDatabase db = getDatabase(context);
		if (db == null)
			return null;

		int table = match >> BASE_SHIFT;
		EmailLog.d("EmEmailProvider", "================== query  =-table: "
				+ table);
		String tableName = TABLE_NAMES[table];
		String id;

		if (Email.LOGD) {
			Log.v(TAG, "EmailProvider.query: uri=" + uri + ", match is "
					+ match);
		}

		try {
			switch (match) {
			case BODY:
			case MESSAGE:
			case UPDATED_MESSAGE:
			case DELETED_MESSAGE:
			case ATTACHMENT:
			case MAILBOX:
			case ACCOUNT:
			case HOSTAUTH:
				c = db.query(TABLE_NAMES[table], projection, selection,
						selectionArgs, null, null, sortOrder);
				break;
			case BODY_ID:
			case MESSAGE_ID:
			case DELETED_MESSAGE_ID:
			case UPDATED_MESSAGE_ID:
			case ATTACHMENT_ID:
			case MAILBOX_ID:
			case ACCOUNT_ID:
			case HOSTAUTH_ID:
				id = uri.getPathSegments().get(1);
				c = db.query(TABLE_NAMES[table], projection,
						whereWithId(id, selection), selectionArgs, null, null,
						sortOrder);
				break;
			case ATTACHMENTS_MESSAGE_ID:
				// All attachments for the given message
				id = uri.getPathSegments().get(2);
				c = db.query(
						Attachment.TABLE_NAME,
						projection,
						whereWith(Attachment.MESSAGE_KEY + "=" + id, selection),
						selectionArgs, null, null, sortOrder);
				break;
			case CONTACT_GET_ALL:
			case CONTACT_GET_ONE:
				//System.out.println("Inside switch case query for contact");
				c = db.query(ContactsConsts.CONTACTS_TABLE_NAME, projection,
						selection, selectionArgs, null, null, sortOrder);
				break;
			case CONTACT_ADRESS_TABLE:
				c = db.query(ContactsConsts.CONTACTS_ADRESS_TABLE_NAME,
						projection, selection, selectionArgs, null, null,
						sortOrder);
				break;

			case CONTACT_SYNCSTATE_TABLE:
				c = db.query(ContactsConsts.CONTACTS_SYNCSTATE_TABLE_NAME,
						projection, selection, selectionArgs, null, null,
						sortOrder);
				// //Log.d(TAG, "CONTACT_SYNCSTATE_TABLE: " + c);
				break;
			case CONTACT_ADD_CACHE_ALL:
				c = db.query(
						ContactsConsts.CONTACTS_LOCAL_CACHE_ADD_TABLE_NAME,
						projection, selection, selectionArgs, null, null,
						sortOrder);
				// //Log.d(TAG, "CONTACT_ADD_CACHE_ALL: " + c);
				break;
			case CONTACT_UPDATE_CACHE_ALL:
				c = db.query(
						ContactsConsts.CONTACTS_LOCAL_CACHE_UPDATE_TABLE_NAME,
						projection, selection, selectionArgs, null, null,
						sortOrder);
				// //Log.d(TAG, "CONTACT_UPDATE_CACHE_ALL: " + c);
				break;
			case CONTACT_DELETE_CACHE_ALL:
				c = db.query(
						ContactsConsts.CONTACTS_LOCAL_CACHE_DELETE_TABLE_NAME,
						projection, selection, selectionArgs, null, null,
						sortOrder);
				// //Log.d(TAG, "CONTACT_DELETE_CACHE_ALL: " + c);
				break;
			case CALENDAR_EVENTS_TABLE:
			case CALENDAR_REMINDERS_TABLE:
			case CALENDAR_ATTENDEES_TABLE:
			case CALENDAR_SEARCH_HISTORY_TABLE:
			case CALENDAR_EXCEPTIONS_TABLE:
			case CALENDAR_SYNCSTATE_TABLE:
				c = db.query(tableName, projection, selection, selectionArgs,
						null, null, null);
				Log.d(TAG, "table name of query: " + tableName);
				break;

			case SECURE_BROWSER:
				Log.i("SecureBrowser", " case secure browser query "
						+ tableName);
				c = db.query(tableName, projection, selection, selectionArgs,
						null, null, sortOrder);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
			}
		} catch (SQLiteException e) {
			checkDatabases();
			throw e;
		}

		if ((c != null) && !isTemporary()) {
			c.setNotificationUri(getContext().getContentResolver(),
					notificationUri);
		}
		return c;
	}

	private String whereWithId(String id, String selection) {
		StringBuilder sb = new StringBuilder(256);
		sb.append("_id=");
		sb.append(id);
		if (selection != null) {
			sb.append(" AND (");
			sb.append(selection);
			sb.append(')');
		}
		return sb.toString();
	}

	/**
	 * Combine a locally-generated selection with a user-provided selection
	 * 
	 * This introduces risk that the local selection might insert incorrect
	 * chars into the SQL, so use caution.
	 * 
	 * @param where
	 *            locally-generated selection, must not be null
	 * @param selection
	 *            user-provided selection, may be null
	 * @return a single selection string
	 */
	private String whereWith(String where, String selection) {
		if (selection == null) {
			return where;
		}
		StringBuilder sb = new StringBuilder(where);
		sb.append(" AND (");
		sb.append(selection);
		sb.append(')');

		return sb.toString();
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		int match = sURIMatcher.match(uri);
		Context context = getContext();
		// See the comment at delete(), above
		TruBoxDatabase db = getDatabase(context);
		if (db == null)
			return -1;
		int table = match >> BASE_SHIFT;
		String tableName = TABLE_NAMES[table];
		int result;

		if (Email.LOGD) {
			Log.v(TAG, "EmailProvider.update: uri=" + uri + ", match is "
					+ match);
		}

		// We do NOT allow setting of unreadCount via the provider
		// This column is maintained via triggers
		if (match == MAILBOX_ID || match == MAILBOX) {
			values.remove(MailboxColumns.UNREAD_COUNT);
		}

		// Handle this special case the fastest possible way
		if (uri == INTEGRITY_CHECK_URI) {
			checkDatabases();
			return 0;
		}

		String id;
		try {
			switch (match) {
			case MAILBOX_ID_ADD_TO_FIELD:
			case ACCOUNT_ID_ADD_TO_FIELD:
				db.beginTransaction();
				id = uri.getPathSegments().get(1);
				String field = values
						.getAsString(EmEmailContent.FIELD_COLUMN_NAME);
				Long add = values.getAsLong(EmEmailContent.ADD_COLUMN_NAME);
				if (field == null || add == null) {
					throw new IllegalArgumentException(
							"No field/add specified " + uri);
				}
				Cursor c = db.query(TABLE_NAMES[table], new String[] {
						EmEmailContent.RECORD_ID, field },
						whereWithId(id, selection), selectionArgs, null, null,
						null);
				try {
					result = 0;
					ContentValues cv = new ContentValues();
					String[] bind = new String[1];
					while (c.moveToNext()) {
						bind[0] = c.getString(0);
						long value = c.getLong(1) + add;
						cv.put(field, value);
						result = db.update(TABLE_NAMES[table], cv, ID_EQUALS,
								bind);
					}
				} finally {
					c.close();
				}
				db.setTransactionSuccessful();
				db.endTransaction();
				break;
			case BODY_ID:
			case MESSAGE_ID:
			case SYNCED_MESSAGE_ID:
			case UPDATED_MESSAGE_ID:
			case ATTACHMENT_ID:
			case MAILBOX_ID:
			case ACCOUNT_ID:
			case HOSTAUTH_ID:
				id = uri.getPathSegments().get(1);
				if (match == SYNCED_MESSAGE_ID) {
					// For synced messages, first copy the old message to the
					// updated table
					// Note the insert or ignore semantics, guaranteeing that
					// only the first
					// update will be reflected in the updated message table;
					// therefore this row
					// will always have the "original" data
//					db.execSQL(UPDATED_MESSAGE_INSERT + id);
					db.execSQL(UPDATED_MESSAGE_INSERT , new String[] {id});	//PEN_TEST_SQL_INJECT_CHG
				} else if (match == MESSAGE_ID) {
//					db.execSQL(UPDATED_MESSAGE_DELETE + id);
					db.execSQL(UPDATED_MESSAGE_DELETE , new String[] {id});	//PEN_TEST_SQL_INJECT_CHG
				}
				result = db.update(TABLE_NAMES[table], values,
						whereWithId(id, selection), selectionArgs);
				break;
			case BODY:
			case MESSAGE:
			case UPDATED_MESSAGE:
			case ATTACHMENT:
			case MAILBOX:
			case ACCOUNT:
			case HOSTAUTH:
				result = db.update(TABLE_NAMES[table], values, selection,
						selectionArgs);
				break;
			case CONTACT_SYNCSTATE_TABLE:
				result = db.update(TABLE_NAMES[table], values, null, null);
				break;

			case CONTACT_GET_ALL:

			case CONTACT_ADRESS_TABLE:

			case CONTACT_ADD_CACHE_ALL:
			case CONTACT_DELETE_CACHE_ALL:
			case CONTACT_UPDATE_CACHE_ALL:
				result = db.update(tableName, values, selection, selectionArgs);
				try {
					Intent i = new Intent();
					i.setAction("com.cognizant.trumobi.contact.sync_completed");
					Email.getAppContext().sendBroadcast(i);
				} catch (Exception e) {
					Log.e(EmEmailProvider.class.getCanonicalName(),
							e.toString());
				}
				break;
			case CALENDAR_EVENTS_TABLE:
			case CALENDAR_REMINDERS_TABLE:
			case CALENDAR_ATTENDEES_TABLE:
			case CALENDAR_SEARCH_HISTORY_TABLE:
			case CALENDAR_EXCEPTIONS_TABLE:
			case CALENDAR_SYNCSTATE_TABLE:
				result = db.update(tableName, values, selection, selectionArgs);
				Log.d(TAG, "table name of update: " + tableName);
				break;

			case SECURE_BROWSER:
				Log.i("SecureBrowser", " case secure browser update "
						+ tableName);
				result = db.update(tableName, values, selection, selectionArgs);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
			}
		} catch (SQLiteException e) {
			checkDatabases();
			throw e;
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.content.ContentProvider#applyBatch(android.content.
	 * ContentProviderOperation)
	 */
	@Override
	public ContentProviderResult[] applyBatch(
			ArrayList<ContentProviderOperation> operations)
			throws OperationApplicationException {
		Context context = getContext();
		TruBoxDatabase db = getDatabase(context);
		if (db == null)
			return null;
		db.beginTransaction();
		try {
			ContentProviderResult[] results = super.applyBatch(operations);
			db.setTransactionSuccessful();
			return results;
		} finally {
			db.endTransaction();
		}
	}

	private static boolean isSuccessLogin(Context context) {

		return TruBoxDatabase.isPasswordGenerated();
	}
}
