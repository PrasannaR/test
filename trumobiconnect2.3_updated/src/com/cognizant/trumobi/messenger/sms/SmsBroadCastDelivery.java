package com.cognizant.trumobi.messenger.sms;

import java.util.List;

import com.cognizant.trumobi.messenger.db.SmsHistoryTable;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class SmsBroadCastDelivery extends BroadcastReceiver {
	public static final String MESSAGE_STATUS_RECEIVED_ACTION = "com.android.MESSAGE_STATUS_RECEIVED";
/**
 * This method will call when a particular message successfully delivered to sender
 */
	@Override
	public void onReceive(Context context, Intent intent) {
		if (MESSAGE_STATUS_RECEIVED_ACTION.equals(intent.getAction())) {
			int statusCode = getResultCode();
			String where = SmsHistoryTable.HISTROY_ID + " =? ";
			String statusString = getStatusString(statusCode);
			Uri messageUri = intent.getData();

			List<String> a = messageUri.getPathSegments();
			String strId = a.get((a.size() - 1));
			Log.e(">>><<<<", "URI ID" + strId);
			ContentResolver cr = context.getContentResolver();
			Cursor cursor = cr.query(SmsHistoryTable.CONTENT_URI_HISTORY, null,
					where, new String[] { strId }, null);
			int cursorSize = cursor.getCount();
			Log.e(">>><<<<", "Query Id Count" + cursorSize);
			if (cursorSize == 1) {
				ContentValues updatedValue = new ContentValues();
				updatedValue.put("delivery_status", statusString);
				cr.update(SmsHistoryTable.CONTENT_URI_HISTORY, updatedValue,
						where, new String[] { strId });

			}
			cursor.close();
		}
		Toast.makeText(context, "Delivered", Toast.LENGTH_SHORT).show();
	}
/**
 * Used to get message delivery status string
 * @param mResult : MESSAGE_STATUS_RECEIVED_ACTION intent status code
 * @return
 */
	private String getStatusString(int mResult) {
		// TODO Auto-generated method stub
		String statusValue = null;
		switch (mResult) {
		case 1:
			statusValue = "SMS delivered";
			break;
		case 2:
			statusValue = "SMS not delivered";
			break;
		}
		return statusValue;
	}
}