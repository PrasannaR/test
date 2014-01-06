package com.cognizant.trumobi.container.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sqlcipher.database.SQLiteQueryBuilder;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.TruBoxSDK.TruBoxDatabase;
import com.cognizant.trumobi.container.Pojo.ChildMailbox;
import com.cognizant.trumobi.em.provider.EmEmailContent;
import com.cognizant.trumobi.em.provider.EmEmailContent.Attachment;
import com.cognizant.trumobi.em.provider.EmEmailContent.AttachmentColumns;
import com.cognizant.trumobi.em.provider.EmEmailContent.Mailbox;
import com.cognizant.trumobi.em.provider.EmEmailContent.MailboxColumns;
import com.cognizant.trumobi.em.provider.EmEmailContent.Message;
import com.cognizant.trumobi.em.provider.EmEmailContent.MessageColumns;
import com.cognizant.trumobi.em.provider.EmEmailContent.SecureBrowser;
import com.cognizant.trumobi.em.provider.EmEmailContent.SecureBrowserColumns;
import com.cognizant.trumobi.em.provider.EmEmailProvider;


public class UpdateDB {

	public static void bookmarkAttachments(Context ctx,
			HashMap<String, Boolean> bookmarkedItemIDs) {

		try {
			String where = AttachmentColumns.LOCATION + " =? ";
			
			Log.d("NEW", "---------------> bookmarkAttachments " + bookmarkedItemIDs.size());
			
			for (Map.Entry<String, Boolean> e : bookmarkedItemIDs.entrySet()) {

				String key = e.getKey();
				Log.d("NEW","-----> "+key);
				ContentValues values = new ContentValues();
				values.put(AttachmentColumns.CONTAINER_BOOKMARK, (boolean) e.getValue());

				Log.d("NEW",
						":"
								+ ctx.getContentResolver().update(
										Attachment.CONTENT_URI, values,
										where, new String[] { key })
								+ "   " + (boolean) e.getValue()+"   "+e.getValue());
			}

			
				UtilList.bookmarkedAttachmentItemIDValuesMail.clear();
				UtilList.bookmarkedAttachmentItemIDMail.clear();
			

		} catch (Exception e) {

			Log.d("NEW","Exception Bookm,ark "+e.toString());
		}
	}
	
	private static Cursor getInnerJoinTable(String where, String[] args,String sortOrder){
		
		SQLiteQueryBuilder _QB = new SQLiteQueryBuilder();
		_QB.setTables(Message.TABLE_NAME + " INNER JOIN "
				+ Attachment.TABLE_NAME + " ON " + "Message."
				+ MessageColumns.ID + " = " + "Attachment."
				+ AttachmentColumns.MESSAGE_KEY);

		TruBoxDatabase _DB = EmEmailProvider.getDB();
		Cursor c = null;	

		try {
			
			c = _QB.query(_DB, Message.CONTAINER_CONTENT_PROJECTION, where,
					args, null, null, sortOrder);
			
			return c;
		}catch(Exception e){
			Log.d("NEW","Exception getInnerJoinTable "+e.toString());
		}
		
		return null;
	}

	public static List<ChildMailbox> getListSentDetails(Context ctx) {

		List<ChildMailbox> mMsgItemList = new ArrayList<ChildMailbox>();

		
		Cursor c = null;
		
		String timeStamp = "0";

		try {
			timeStamp = UtilList.frmDateEpoch(UtilList.manipulateTime(-3));
		} catch (Exception e1) {
			e1.printStackTrace();
			timeStamp = "0";
		}

		try {

			/*String where1 = EmEmailContent.Message.TABLE_NAME + "."
					+ MessageColumns.FLAG_ATTACHMENT + " = ?";*/
			String where1;
			
			/*String[] args1 = { "1" };*/
			
			String sortOrder = EmEmailContent.Message.TABLE_NAME + "."
					+ MessageColumns.FROM_LIST + " ASC";
			
			
			String key = getMailboxKey(ctx);

				Log.i("NEW", "____ key " + key);

				if (!(key.equalsIgnoreCase("0"))) {
					where1 = EmEmailContent.Message.TABLE_NAME + "."
							+ MessageColumns.FLAG_ATTACHMENT + " = ? AND "
							+ EmEmailContent.Message.TABLE_NAME + "."
							+ MessageColumns.MAILBOX_KEY + " = ? AND "
							+ Attachment.TABLE_NAME + "."
							+ AttachmentColumns.CONTAINER_DELETE + " = ? AND "
							+ EmEmailContent.Message.TABLE_NAME + "."
							+ MessageColumns.TIMESTAMP + " > ? AND "
							+ Attachment.TABLE_NAME + "."
							+ AttachmentColumns.CONTENT_ID + " IS NULL AND "
						+ AttachmentColumns.MIME_TYPE + " <> ?";

					c = getInnerJoinTable(where1, new String[] { "1", key, "0",
							timeStamp,"application/eml" }, sortOrder);

				} else {

					where1 = EmEmailContent.Message.TABLE_NAME + "."
							+ MessageColumns.FLAG_ATTACHMENT + " = ? AND "
							+ Attachment.TABLE_NAME + "."
							+ AttachmentColumns.CONTAINER_DELETE + " = ? AND "
							+ Attachment.TABLE_NAME + "."
							+ AttachmentColumns.CONTENT_ID + " IS NULL ";

					c = getInnerJoinTable(where1, new String[] { "1", "0" },
							sortOrder);

				}
			
				/*c = getInnerJoinTable(where1,args1,sortOrder);context.getContentResolver().query(
						EmailMessage.INBOX_CONTENT_URI,
						EmailMessage.CONTENT_PROJECTION_LIST_DETAILS, where,
						new String[] { "0" },
						InboxListDetail.FROM_LIST + " ASC");*/
			

			if ((c != null) && (c.getCount() <= 0)) {

				try {
					c.close();
				} catch (Exception e) {

				}
				return null;
			}

			try {
				c.moveToFirst();
				String[] a = c.getColumnNames();
				Log.i("Size ", "S : " + a.length + " " + c.getCount());

				do {
					ChildMailbox mMsgItem = new ChildMailbox();

					mMsgItem.setID(c
							.getString(Message.CONTAINER_LIST_ID_COLUMN));
					
					mMsgItem.setDISPLAY_NAME(c
							.getString(Message.CONTAINER_LIST_DISPLAY_NAME_COLUMN));
					
					mMsgItem.setSubject(c
							.getString(Message.CONTAINER_LIST_SUBJECT_COLUMN));
					
					mMsgItem.setREAD(c
							.getString(Message.CONTAINER_LIST_READ_COLUMN));
					
					mMsgItem.setSERVER_ID(c
							.getString(Message.CONTAINER_LIST_SERVER_ID_COLUMN));
					
					mMsgItem.setMAILBOX_KEY(c
							.getString(Message.CONTAINER_LIST_MAILBOX_KEY_COLUMN));
					
					mMsgItem.setEmailAddress(c
							.getString(Message.CONTAINER_LIST_FROM_LIST_COLUMN));
					
					String date = c
							.getString(Message.CONTAINER_LIST_SERVER_TIMESTAMP_COLUMN);
					Date expiry = new Date(Long.parseLong(date));
					
					mMsgItem.setDateTimeReceived(UtilList.frmEpochDate(expiry));
					
					mMsgItem.setAttachmentName(c
							.getString(Message.CONTAINER_LIST_ATTACHMENT_FILENAME_COLUMN));

					mMsgItem.setContent(c
							.getString(Message.CONTAINER_LIST_ATTACHMENT_CONTENT_URI_COLUMN));

					mMsgItem.setSize(c
							.getString(Message.CONTAINER_LIST_ATTACHMENT_SIZE_COLUMN));
					
					mMsgItem.setAttachmentId(c
							.getString(Message.CONTAINER_LIST_ATTACHMENT_LOCATION_COLUMN));

					mMsgItem.setATTACHMENT_BOOKMARKED(c
							.getInt(Message.CONTAINER_LIST_ATTACHMENT_CONTAINER_BOOKMARK_COLUMN) == 1 ? true
									: false);

					mMsgItem.setACCOUNT_KEY(c
							.getString(Message.CONTAINER_LIST_ACCOUNT_KEY_COLUMN));
					
					mMsgItem.setATTACHMENT_TABLE_ID(c
							.getString(Message.CONTAINER_LIST_ATTACHMENT_TABLE_ID_COLUMN));

					mMsgItemList.add(mMsgItem);
					mMsgItem = null;

				} while (c.moveToNext());
			} catch (Exception e) {

				Log.e("Exception ", "Get List details " + e.toString());
				cursorClose(c);
				return null;

			}

			cursorClose(c);
			return mMsgItemList;
		} catch (Exception e) {

		}
		cursorClose(c);
		return null;
	}
	
	public static void deleteAttachments(Context ctx,
			ArrayList<String> deletedItemIDs) {

		int affected = 0;
		ContentValues values = new ContentValues();
		values.put(AttachmentColumns.CONTAINER_DELETE, 1);
		try {
			String where = AttachmentColumns.LOCATION + " =? ";

			Log.i("New ", "Ids size " + deletedItemIDs.size());

			for (int dc = 0; dc < deletedItemIDs.size(); dc++) {

				try {

					affected = ctx.getContentResolver().update(
							Attachment.CONTENT_URI, values, where,
							new String[] { deletedItemIDs.get(dc) });

					Log.i("NEW", "DElEted ----> " + affected);
				} catch (Exception e) {

					Log.i("New ", "Ids size " + e.toString());
				}

			}
		} catch (Exception e) {

			Log.i("New ", "Ids size " + e.toString());
		}

	}


	public static List<ChildMailbox> getBookmarkedListDetails(Context context) {

		List<ChildMailbox> mMsgItemList = new ArrayList<ChildMailbox>();

		
		Cursor c = null;

		try {

			String where1 = Attachment.TABLE_NAME + "."
					+ AttachmentColumns.CONTAINER_BOOKMARK + " = ? AND "
					+ Attachment.TABLE_NAME + "."
					+ AttachmentColumns.CONTAINER_DELETE + " = ?";
			String[] args1 = { "1", "0" };

			String sortOrder = Message.TABLE_NAME + "."
					+ MessageColumns.TIMESTAMP + " DESC";
			
				c = getInnerJoinTable(where1,args1,sortOrder);
			

			if ((c == null)) {
				Log.d("NEW","get bokk list cursor null");
				return null;
			}

			try {
				c.moveToFirst();
				String[] a = c.getColumnNames();
				Log.i("Size ",
						"getBookmarkedListDetails : " + a.length + " " + c.getCount());

				do {
					ChildMailbox mMsgItem = new ChildMailbox();

					mMsgItem.setID(c
							.getString(Message.CONTAINER_LIST_ID_COLUMN));
					
					mMsgItem.setDISPLAY_NAME(c
							.getString(Message.CONTAINER_LIST_DISPLAY_NAME_COLUMN));
					
					mMsgItem.setSubject(c
							.getString(Message.CONTAINER_LIST_SUBJECT_COLUMN));
					
					mMsgItem.setREAD(c
							.getString(Message.CONTAINER_LIST_READ_COLUMN));
					
					mMsgItem.setSERVER_ID(c
							.getString(Message.CONTAINER_LIST_SERVER_ID_COLUMN));
					
					mMsgItem.setMAILBOX_KEY(c
							.getString(Message.CONTAINER_LIST_MAILBOX_KEY_COLUMN));
					
					mMsgItem.setEmailAddress(c
							.getString(Message.CONTAINER_LIST_FROM_LIST_COLUMN));
					
					String date = c
							.getString(Message.CONTAINER_LIST_SERVER_TIMESTAMP_COLUMN);
					Date expiry = new Date(Long.parseLong(date));
					
					mMsgItem.setDateTimeReceived(UtilList.frmEpochDate(expiry));
					
					mMsgItem.setAttachmentName(c
							.getString(Message.CONTAINER_LIST_ATTACHMENT_FILENAME_COLUMN));

					mMsgItem.setContent(c
							.getString(Message.CONTAINER_LIST_ATTACHMENT_CONTENT_URI_COLUMN));

					mMsgItem.setSize(c
							.getString(Message.CONTAINER_LIST_ATTACHMENT_SIZE_COLUMN));
					
					mMsgItem.setAttachmentId(c
							.getString(Message.CONTAINER_LIST_ATTACHMENT_LOCATION_COLUMN));

					mMsgItem.setATTACHMENT_BOOKMARKED(c
							.getInt(Message.CONTAINER_LIST_ATTACHMENT_CONTAINER_BOOKMARK_COLUMN) == 1 ? true
									: false);

					mMsgItem.setACCOUNT_KEY(c
							.getString(Message.CONTAINER_LIST_ACCOUNT_KEY_COLUMN));
					
					mMsgItem.setATTACHMENT_TABLE_ID(c
							.getString(Message.CONTAINER_LIST_ATTACHMENT_TABLE_ID_COLUMN));


					mMsgItemList.add(mMsgItem);
					mMsgItem = null;

				} while (c.moveToNext());
			} catch (Exception e) {

				Log.e("Exception ", "Get List details " + e.toString());
				c.close();
				return null;

			}
			cursorClose(c);
			return mMsgItemList;
		} catch (Exception e) {

		}
		cursorClose(c);
		return null;
	}


	public static List<ChildMailbox> getListDetails(Context ctx) {

		List<ChildMailbox> mMsgItemList = new ArrayList<ChildMailbox>();

		/*String where1 = EmEmailContent.Message.TABLE_NAME + "."
				+ MessageColumns.FLAG_ATTACHMENT + " = ?";
		String[] args1 = { "1" };*/
		String where1;

		String sortOrder = EmEmailContent.Message.TABLE_NAME + "."
				+ MessageColumns.TIMESTAMP + " DESC";
		Cursor c = null;

		String key = getMailboxKey(ctx).trim();

		String timeStamp = "0";

		try {
			timeStamp = UtilList.frmDateEpoch(UtilList.manipulateTime(-3));
		} catch (Exception e1) {
			e1.printStackTrace();
			timeStamp = "0";
		}

		try {
			Log.i("NEW", "-------> key " + key);

			if (!(key.equalsIgnoreCase("0"))) {
			
				where1 = EmEmailContent.Message.TABLE_NAME + "."
						+ MessageColumns.FLAG_ATTACHMENT + " = ? AND "
						+ EmEmailContent.Message.TABLE_NAME + "."
						+ MessageColumns.MAILBOX_KEY + " = ? AND "
						+ Attachment.TABLE_NAME + "."
						+ AttachmentColumns.CONTAINER_DELETE + " = ? AND "
						+ EmEmailContent.Message.TABLE_NAME + "."
						+ MessageColumns.TIMESTAMP + " > ? AND "
						+ Attachment.TABLE_NAME + "."
						+ AttachmentColumns.CONTENT_ID + " IS NULL AND "
						+ AttachmentColumns.MIME_TYPE + " <> ?";

				c = getInnerJoinTable(where1, new String[] { "1", key, "0",
						timeStamp,"application/eml" }, sortOrder);
				
			}else{
				
				where1 = EmEmailContent.Message.TABLE_NAME + "."
						+ MessageColumns.FLAG_ATTACHMENT + " = ? AND "
						+ Attachment.TABLE_NAME + "."
						+ AttachmentColumns.CONTAINER_DELETE + " = ? AND "
						+ Attachment.TABLE_NAME + "."
						+ AttachmentColumns.CONTENT_ID + " IS NULL ";

				c = getInnerJoinTable(where1, new String[] { "1", "0" },
						sortOrder);
				
			}
			/*c = getInnerJoinTable(where1,args1,sortOrder);*/

			if ((c == null)) {
				return null;
			}

			try {
				c.moveToFirst();
				String[] a = c.getColumnNames();
				Log.i("Size getListDetails",
						"S : " + a.length + " " + c.getCount());

				do {
					ChildMailbox mMsgItem = new ChildMailbox();

					mMsgItem.setID(c
							.getString(Message.CONTAINER_LIST_ID_COLUMN));
					
					mMsgItem.setDISPLAY_NAME(c
							.getString(Message.CONTAINER_LIST_DISPLAY_NAME_COLUMN));
					
					mMsgItem.setSubject(c
							.getString(Message.CONTAINER_LIST_SUBJECT_COLUMN));
					
					mMsgItem.setREAD(c
							.getString(Message.CONTAINER_LIST_READ_COLUMN));
					
					mMsgItem.setSERVER_ID(c
							.getString(Message.CONTAINER_LIST_SERVER_ID_COLUMN));
					
					mMsgItem.setMAILBOX_KEY(c
							.getString(Message.CONTAINER_LIST_MAILBOX_KEY_COLUMN));
					
					mMsgItem.setEmailAddress(c
							.getString(Message.CONTAINER_LIST_FROM_LIST_COLUMN));
					
					String date = c
							.getString(Message.CONTAINER_LIST_SERVER_TIMESTAMP_COLUMN);
					Date expiry = new Date(Long.parseLong(date));
					
					mMsgItem.setDateTimeReceived(UtilList.frmEpochDate(expiry));
					
					mMsgItem.setAttachmentName(c
							.getString(Message.CONTAINER_LIST_ATTACHMENT_FILENAME_COLUMN));

					Log.d("NEW",
							"Att Uri ------> "
									+ c.getString(Message.CONTAINER_LIST_ATTACHMENT_CONTENT_URI_COLUMN));
					
					mMsgItem.setContent(c
							.getString(Message.CONTAINER_LIST_ATTACHMENT_CONTENT_URI_COLUMN));

					mMsgItem.setSize(c
							.getString(Message.CONTAINER_LIST_ATTACHMENT_SIZE_COLUMN));
					
					mMsgItem.setAttachmentId(c
							.getString(Message.CONTAINER_LIST_ATTACHMENT_LOCATION_COLUMN));

					mMsgItem.setATTACHMENT_BOOKMARKED(c
							.getInt(Message.CONTAINER_LIST_ATTACHMENT_CONTAINER_BOOKMARK_COLUMN) == 1 ? true
									: false);

					mMsgItem.setACCOUNT_KEY(c
							.getString(Message.CONTAINER_LIST_ACCOUNT_KEY_COLUMN));
					
					mMsgItem.setATTACHMENT_TABLE_ID(c
							.getString(Message.CONTAINER_LIST_ATTACHMENT_TABLE_ID_COLUMN));

					mMsgItemList.add(mMsgItem);
					mMsgItem = null;

				} while (c.moveToNext());
			} catch (Exception e) {

				Log.e("Exception ", "Get List details " + e.toString());
				cursorClose(c);
				return null;

			}

			cursorClose(c);
			return mMsgItemList;
		} catch (Exception e) {

		}
		cursorClose(c);
		return null;
	}
	
	private static String getMailboxKey(Context ctx) {

		Cursor c = null;
		int mailId = 0;
		try {

			String where = MailboxColumns.DISPLAY_NAME + " = ?";
			String[] args = { "Inbox" };

			c = ctx.getContentResolver().query(Mailbox.CONTENT_URI,
					Mailbox.CONTAINER_MAILBOX_PROJECTION,
					where, args, null);

			if ((c != null) && (c.getCount() <= 0)) {

				cursorClose(c);
				return Integer.toString(0);
			}

			try {
				c.moveToFirst();
				String[] a = c.getColumnNames();
				Log.i("getSingleAttachmentDetails ", "S : " + a.length + " "
						+ c.getCount());

				do {

					mailId = c
							.getInt(Mailbox.CONTAINER_MAILBOX_COLUMN_ID);
					break;

				} while (c.moveToNext());
			} catch (Exception e) {

				Log.e("Exception ", "Get List details " + e.toString());
				cursorClose(c);
				return Integer.toString(0);

			}

			cursorClose(c);
			return Integer.toString(mailId);
		} catch (Exception e) {

		}
		cursorClose(c);
		return Integer.toString(0);

	}

	public static Long[] getMailAccKey(Context ctx) {

		Cursor c = null;
		Long mailId = 0L;
		Long acckey = 0L;
		try {

			String where = MailboxColumns.DISPLAY_NAME + " = ?";
			String[] args = { "Inbox" };
			// Long[] a;
			c = ctx.getContentResolver().query(Mailbox.CONTENT_URI,
					Mailbox.CONTAINER_MAILACC_PROJECTION,
					where, args, null);

			if ((c != null) && (c.getCount() <= 0)) {

				cursorClose(c);
				return new Long[] { 0L, 0L };
			}

			try {
				c.moveToFirst();

				do {

					mailId = c
							.getLong(Mailbox.CONTAINER_MAILACC_COLUMN_ID);
					acckey = c
							.getLong(Mailbox.CONTAINER_MAILACC_COLUMN_ACC_ID);

					break;

				} while (c.moveToNext());
			} catch (Exception e) {

				Log.e("Exception ", "Get List details " + e.toString());
				cursorClose(c);
				return new Long[] { 0L, 0L };

			}

			cursorClose(c);

			return new Long[] { mailId, acckey };
		} catch (Exception e) {

		}
		cursorClose(c);
		return new Long[] { 0L, 0L };

	}

	
	private static void cursorClose(Cursor c) {

		try {

			c.close();

		} catch (Exception e) {

		}

	}
	
	public static int storeInboxMessages(Context context,
			List<ChildMailbox> data) {

		try {
			int size = data.size();
			Log.i("SecureBrowser", " -===- " + data.size() + " records" + "   "
					+ size + "   ");
			ContentValues values[] = new ContentValues[size];
			for (int index = 0; index < size; ++index) {

				ContentValues this_value = values[index] = new ContentValues();

				ChildMailbox datum = data.get(index);

				this_value.put(SecureBrowserColumns.FILENAME,
						datum.getAttachmentName());
				this_value.put(SecureBrowserColumns.MIME_TYPE,
						datum.getMIME_TYPE());
				this_value.put(SecureBrowserColumns.SIZE, datum.getSize());

				this_value.put(SecureBrowserColumns.CONTENT_URI,
						datum.getContent());
				this_value.put(SecureBrowserColumns.DATE_TIME,
						datum.getDateTimeReceived());
				this_value.put(SecureBrowserColumns.URL,
						datum.getEmailAddress());

			}

			int inserted = context.getContentResolver().bulkInsert(
					SecureBrowser.CONTENT_URI, values);

			Log.d("SecureBrowser", "xxxxxx " + inserted + " inserted");

			return inserted;
		} catch (Exception e) {

			Log.e("SecureBrowser", "ee " + e.toString());

		}
		return -1;
	}

	public static List<ChildMailbox> getListBrowserDetails(Context context,
			int listType) {

		List<ChildMailbox> mMsgItemList = new ArrayList<ChildMailbox>();

		String where;
		// String[] args = { "0" };

		Cursor c = null;
		try {

			switch (listType) {

			case 3:
			case 1:

				c = context.getContentResolver().query(
						SecureBrowser.CONTENT_URI,
						SecureBrowser.CONTENT_PROJECTION_LIST_DETAILS, null,
						null, SecureBrowser.DATE_TIME + " DESC");
				break;
			case 2:
				where = SecureBrowser.CONTAINER_BOOKMARK + " = ? ";
				c = context.getContentResolver()
						.query(SecureBrowser.CONTENT_URI,
								SecureBrowser.CONTENT_PROJECTION_LIST_DETAILS,
								where, new String[] { "1" },
								SecureBrowser.DATE_TIME + " DESC");
				break;

			}

			if ((c == null)) {

				cursorClose(c);
				return null;
			}

			try {
				c.moveToFirst();
				String[] a = c.getColumnNames();
				//Log.i("SecureBrowser", "S : " + a.length + " " + c.getCount());

				do {
					ChildMailbox datum = new ChildMailbox();

					datum.setAttachmentName(c
							.getString(SecureBrowser.LIST_SB_FILENAME));
					datum.setMIME_TYPE(c
							.getString(SecureBrowser.LIST_SB_MIME_TYPE));
					datum.setSize(c.getString(SecureBrowser.LIST_SB_SIZE));
					datum.setContent(c
							.getString(SecureBrowser.LIST_SB_CONTENT_URI));

					String date = c
							.getString(SecureBrowser.LIST_SB_DATE_TIME);
				//	Log.e("SecureBrowser", "date  " + date);
					Date expiry = new Date(Long.parseLong(date));
				//	Log.e("SecureBrowser", "expiry  " + date);
					datum.setDateTimeReceived(UtilList.frmEpochDatenew(expiry));
					//Log.e("SecureBrowser", "setDateTimeReceived  " + date);
					
					datum.setEmailAddress(c
							.getString(SecureBrowser.LIST_SB_URL));
					datum.setAttachmentId(c
							.getString(SecureBrowser.LIST_SB_ID));

					datum.setATTACHMENT_BOOKMARKED(c
							.getInt(SecureBrowser.LIST_SB_CONTAINER_BOOKMARK) == 1 ? true
							: false);

					mMsgItemList.add(datum);

				} while (c.moveToNext());
			} catch (Exception e) {

				Log.e("SecureBrowser", "Get List details " + e.toString());
				cursorClose(c);
				return null;

			}

			// Creating Arraylist without duplicate values
			List<ChildMailbox> listWithoutDuplicates = new ArrayList<ChildMailbox>(
					mMsgItemList);

			Log.i("SecureBrowser ",
					"ITEMID SIZE: " + listWithoutDuplicates.size());
			cursorClose(c);
			return listWithoutDuplicates;
		} catch (Exception e) {

		}
		cursorClose(c);
		return null;
	}

	public static void bookmarkAttachmentsSecureBrowser(Context ctx,
			HashMap<String, Boolean> bookmarkedItemIDs) {

		try {
			String where = SecureBrowserColumns.ID + " =? ";

			Log.d("SecureBrowser", "---------------> bookmarkAttachments "
					+ bookmarkedItemIDs.size());

			for (Map.Entry<String, Boolean> e : bookmarkedItemIDs.entrySet()) {

				String key = e.getKey();
				Log.d("SecureBrowser", "-----> " + key);
				ContentValues values = new ContentValues();
				values.put(SecureBrowserColumns.CONTAINER_BOOKMARK,
						(boolean) e.getValue());

				Log.d("SecureBrowser",
						": "
								+ ctx.getContentResolver().update(
										SecureBrowser.CONTENT_URI, values,
										where, new String[] { key }) + "   "
								+ (boolean) e.getValue());
			}

		} catch (Exception e) {

			Log.d("SecureBrowser", "Exception Bookmark " + e.toString());
		}
		UtilList.bookmarkedAttachmentItemIDValuesCal.clear();
		UtilList.bookmarkedAttachmentItemIDCal.clear();
	}

	public static void deleteAttachmentsSecureBrowser(Context ctx,
			ArrayList<String> deletedItemIDs) {

		int affected = 0;
		Cursor c = null;

		/*
		 * ContentValues values = new ContentValues();
		 * values.put(AttachmentColumns.CONTAINER_DELETE, 1);
		 */

		try {
			String where = SecureBrowserColumns.ID + " =? ";

			Log.i("New ", "Ids size " + deletedItemIDs.size());

			
			String id;
			String path;
			File obj = null;

			for (int dc = 0; dc < deletedItemIDs.size(); dc++) {

				try {

					c = ctx.getContentResolver().query(
							SecureBrowser.CONTENT_URI,
							SecureBrowser.CONTENT_PROJECTION_DELETE_DETAILS,
							where, new String[] { deletedItemIDs.get(dc) },
							SecureBrowser.DATE_TIME + " DESC");

					c.moveToFirst();
					String[] a = c.getColumnNames();
					Log.i("SecureBrowser",
							"S : " + a.length + " " + c.getCount());

					do {

						id = c.getString(SecureBrowser.LIST_SB_DELETE_ID);
						path = c.getString(SecureBrowser.LIST_SB_DELETE_FILENAME);

						try {
							obj = new File(path);
							obj.delete();
						} catch (Exception e) {

						}
						affected = ctx.getContentResolver().delete(
								SecureBrowser.CONTENT_URI, where,
								new String[] { id });

					} while (c.moveToNext());
					
					cursorClose(c);

					Log.i("NEW", "DElEted ----> " + affected);
				} catch (Exception e) {

					Log.i("New ", "Ids size " + e.toString());
					cursorClose(c);
				}

			}
		} catch (Exception e) {

			Log.i("New ", "Ids size " + e.toString());
		}
		cursorClose(c);

	}

}
