package com.cognizant.trumobi.messenger.sms;

import com.cognizant.trumobi.messenger.db.SmsHistoryTable;
import com.cognizant.trumobi.messenger.db.SmsIndividualTable;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class SmsDeliveryStatus extends IntentService {

	public SmsDeliveryStatus() {
		super("SmsDeliveryStatus");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		Log.e("SmsDeliveryStatus", "start");

		Log.e("SmsDeliveryStatus", "end");

		Bundle extras = intent.getExtras();
		String mbody = extras.getString("body");
		String mNumber = extras.getString("number");
		long mTime = extras.getLong("time");
		int mResult = extras.getInt("deliveryStatus");
		String deliveryStatus = getStatusCode(mResult);

		Log.e("DELIVEY MESSAGE", mNumber + "<>" + mbody + "<>" + mResult + "<>"
				+ deliveryStatus);

		ContentResolver cr = getContentResolver();
		final SmsIndividualTable home = new SmsIndividualTable();
		final SmsHistoryTable detail = new SmsHistoryTable();

		home.setDeliveryStatus(deliveryStatus);
		long tim = System.currentTimeMillis();
		home.setTime(tim);
		Cursor cursor = cr.query(SmsHistoryTable.CONTENT_URI_HISTORY, null,
				SmsHistoryTable.PHONE_NUMBER + "='" + mNumber + "'" + " AND "
						+ SmsHistoryTable.MESSAGE + "='" + mbody + "'"
						+ " AND " + SmsHistoryTable.TIME + "='" + mTime + "'",
				null, null);
		int cursorSize = cursor.getCount();
		Log.e(">>><<<<", "Query Count" + cursorSize);
		if (cursorSize == 1) {
			ContentValues updatedValue = new ContentValues();
			updatedValue = getHomeListValue(home);
			cr.update(SmsIndividualTable.CONTENT_URI_INDIVIDUAL, updatedValue,
					SmsIndividualTable.PHONE_NUMBER + "='" + mNumber + "'"
							+ " AND " + SmsIndividualTable.MESSAGE + "='"
							+ mbody + "'" + " AND " + SmsIndividualTable.TIME
							+ "='" + mTime + "'", null);

			detail.setTime(home.getTime());
			detail.setDeliveryStatus(home.getDeliveryStatus());

			ContentValues individual = new ContentValues();
			individual = getDetailListValue(detail);
			cr.update(SmsHistoryTable.CONTENT_URI_HISTORY, individual,
					SmsHistoryTable.PHONE_NUMBER + "='" + mNumber + "'"
							+ " AND " + SmsHistoryTable.MESSAGE + "='" + mbody
							+ "'" + " AND " + SmsHistoryTable.TIME + "='"
							+ mTime + "'", null);
		}
		cursor.close();
		Messenger messenger = (Messenger) extras.get("MESSENGER");
		Message msg = Message.obtain();
		try {
			messenger.send(msg);
		} catch (android.os.RemoteException e1) {
			Log.w(getClass().getName(), "Exception sending message", e1);
		}

	}

	private String getStatusCode(int mResult) {
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

	public ContentValues getHomeListValue(SmsIndividualTable home) {
		// TODO Auto-generated method stub
		ContentValues cv = new ContentValues();
		cv.put("time", home.getTime());
		cv.put("delivery_status", home.getDeliveryStatus());
		return cv;
	}

	public ContentValues getDetailListValue(SmsHistoryTable detail) {
		// TODO Auto-generated method stub
		ContentValues newValue = new ContentValues();
		newValue.put("time", detail.getTime());
		newValue.put("delivery_status", detail.getDeliveryStatus());
		return newValue;
	}
}
