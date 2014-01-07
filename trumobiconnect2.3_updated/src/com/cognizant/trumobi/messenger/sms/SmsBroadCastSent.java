package com.cognizant.trumobi.messenger.sms;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Message;
import android.os.Messenger;
import android.sax.StartElementListener;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.cognizant.trumobi.messenger.db.SmsHistoryBackup;
import com.cognizant.trumobi.messenger.db.SmsHistoryTable;
import com.cognizant.trumobi.messenger.db.SmsIndividualTable;
import com.cognizant.trumobi.messenger.model.SmsSearchListModel;

public class SmsBroadCastSent extends BroadcastReceiver {
	static String MESSAGE_SENT_ACTION = "com.android.MESSAGE_STATUS_SENT";
	static String sentPhoneNumber;
/**
 * This method will call when "MESSAGE_SENT_ACTION" intent gets the data
 */
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (MESSAGE_SENT_ACTION.equals(intent.getAction())) {
			int mResult = 0;
			switch (getResultCode()) {
			case Activity.RESULT_OK:
				mResult = 1;
				break;
			case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
				mResult = 2;
				break;
			case SmsManager.RESULT_ERROR_NO_SERVICE:
				mResult = 3;
				break;
			case SmsManager.RESULT_ERROR_NULL_PDU:
				mResult = 4;
				break;
			case SmsManager.RESULT_ERROR_RADIO_OFF:
				mResult = 5;
				break;

			}
			String statusString = getStatusString(mResult);
			Uri messageUri = intent.getData();
			List<String> a = messageUri.getPathSegments();
			String strId = a.get((a.size() - 1));
			String name = null;
			String num = null;
			String lBody = null;
			String mSR = null;
			long time = 0;
			byte[] picture = null;
			String strlock = null;
			String[] projection = { SmsHistoryTable.FIRST_NAME,
					SmsHistoryTable.PHONE_NUMBER, SmsHistoryTable.MESSAGE,
					SmsHistoryTable.SEND_RECEIVE, SmsHistoryTable.HISTROY_ID,
					SmsHistoryTable.TIME, SmsHistoryTable.IMAGE,
					SmsHistoryTable.SENT_STATUS, SmsHistoryTable.LOCKED };
			String where = SmsHistoryTable.HISTROY_ID + " =? ";
			ContentResolver cr = context.getContentResolver();
			Cursor cursor = cr.query(SmsHistoryTable.CONTENT_URI_HISTORY,
					projection, where, new String[] { strId }, null);
			int cursorSize = cursor.getCount();
			if (cursor.moveToFirst()) {
				do {
					sentPhoneNumber = cursor.getString(cursor
							.getColumnIndex(SmsHistoryTable.PHONE_NUMBER));
					Log.e(">>><<<<", "PPPPPHHHHHHOOOONNNNNEEEEEE"
							+ sentPhoneNumber);
					name = cursor.getString(cursor
							.getColumnIndex(SmsHistoryTable.FIRST_NAME));
					num = cursor.getString(cursor
							.getColumnIndex(SmsHistoryTable.PHONE_NUMBER));
					lBody = cursor.getString(cursor
							.getColumnIndex(SmsHistoryTable.MESSAGE));
					mSR = cursor.getString(cursor
							.getColumnIndex(SmsHistoryTable.SEND_RECEIVE));
					time = cursor.getLong(cursor
							.getColumnIndex(SmsHistoryTable.TIME));
					picture = cursor.getBlob(cursor
							.getColumnIndex(SmsHistoryTable.IMAGE));
					strlock = cursor.getString(cursor
							.getColumnIndex(SmsHistoryTable.LOCKED));
				} while (cursor.moveToNext());
			}
			Log.e(">>><<<<", "Query Id Count" + cursorSize);
			if (cursorSize == 1) {

				SmsHistoryBackup backup = new SmsHistoryBackup();
				backup.setFirstName(name);
				backup.setPhoneNumber(num);
				backup.setMessage(lBody);
				if (mSR.equals("recv"))
					backup.setSend(false);
				else
					backup.setSend(true);
				SimpleDateFormat df = new SimpleDateFormat("dd MMM yyyy-hh:mm");
				String curTime = df.format(time);
				String[] splitDate = curTime.split("-");
				String mDate = splitDate[0];
				String mTime = splitDate[1];
				backup.setDate(mDate);
				backup.setTime(mTime);
				backup.setContactImageByte(picture);
				if (strlock.equals("1"))
					backup.setLocked(true);
				else
					backup.setLocked(false);
				backup.setSentStatus(statusString);
				backup.setCorporateFlag(false);
				backup.setMsgCount(calculateMsgCount(lBody));

				ContentValues updatedValue = new ContentValues();
				updatedValue.put("sent_status", statusString);
				Log.e(">>><<<<", "Seeeeeeeeeeend Stttttttaus" + statusString);
				ContentValues backupTableValue = new ContentValues();
				backupTableValue = getBackupListValue(backup);

				cr.update(SmsHistoryTable.CONTENT_URI_HISTORY, updatedValue,
						where, new String[] { strId });
				cr.update(SmsIndividualTable.CONTENT_URI_INDIVIDUAL,
						updatedValue, SmsIndividualTable.PHONE_NUMBER + " =? ",
						new String[] { sentPhoneNumber });
				cr.insert(SmsHistoryBackup.CONTENT_URI_HISTORY_BACKUP,
						backupTableValue);
				new SmsContactPickAsyncTask(context).execute(num);

			}
			cursor.close();
		}
		LocalBroadcastManager.getInstance(context).sendBroadcast(
				new Intent("LoaclReciev"));
	}

	private ContentValues getBackupListValue(SmsHistoryBackup backup) {
		// TODO Auto-generated method stub
		ContentValues backupValue = new ContentValues();
		backupValue.put("firstname", backup.getFirstName());
		backupValue.put("phonenumber", backup.getPhoneNumber());
		backupValue.put("message", backup.getMessage());
		backupValue.put("date", backup.getDate());
		backupValue.put("time", backup.getTime());
		backupValue.put("is_send", backup.isSend());
		backupValue.put("personimage", backup.getContacctImageByte());
		backupValue.put("sent_status", backup.getSentStatus());
		backupValue.put("lock", backup.getLocked());
		backupValue.put("corporate_contact", backup.getCoporateFlag());
		backupValue.put("msg_count", backup.getMsgCount());

		return backupValue;

	}

	public int calculateMsgCount(String message) {
		// TODO Auto-generated method stub
		int totalSize = message.length();
		int count = 0;
		if (totalSize <= SmsHistoryBackup.MSG_LENGTH)
			count = 1;
		else {
			int msgInt = totalSize / SmsHistoryBackup.MSG_LENGTH;
			int mod = totalSize % SmsHistoryBackup.MSG_LENGTH;
			if (mod == 0)
				count = msgInt;
			else
				count = msgInt + 1;
		}
		return count;
	}

	private String getStatusString(int mResult) {
		// TODO Auto-generated method stub
		String statusValue = null;
		switch (mResult) {
		case 1:
			statusValue = "SMS sent";
			break;
		case 2:
			statusValue = "Generic failure";
			break;
		case 3:
			statusValue = "No service";
			break;
		case 4:
			statusValue = "Null PDU";
			break;
		case 5:
			statusValue = "Radio off";
			break;
		}
		return statusValue;
	}

}
