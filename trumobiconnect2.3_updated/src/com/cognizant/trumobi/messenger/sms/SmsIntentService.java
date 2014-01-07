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

public class SmsIntentService extends IntentService {

	public SmsIntentService() {
		super("SmsIntentService");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		Log.e("SmsIntentService", "start");

		Log.e("SmsIntentService", "end");

		Bundle extras = intent.getExtras();
		String mbody = extras.getString("body");
		String mNumber = extras.getString("number");
		long mTime = extras.getLong("time");
		int mResult = extras.getInt("result");
		String sentStatus = getStatusCode(mResult);

		Log.e("SMSMESSAGING", mNumber + "<>" + mbody + "<>" + mResult);
		Log.e(">>><<<<", "IIIIIIIIIIIIIIIIIIIIIII" + sentStatus);

		ContentResolver cr = getContentResolver();
		final SmsIndividualTable home = new SmsIndividualTable();
		final SmsHistoryTable detail = new SmsHistoryTable();

		home.setSentStatus(sentStatus);
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
							+ mbody + "'" + " AND "
							+ SmsIndividualTable.MESSAGE + "='" + mbody + "'",
					null);

			detail.setTime(home.getTime());
			detail.setSentStatus(home.getSentStatus());

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

	public ContentValues getHomeListValue(SmsIndividualTable home) {
		// TODO Auto-generated method stub
		ContentValues cv = new ContentValues();
		cv.put("time", home.getTime());
		cv.put("sent_status", home.getSentStatus());
		return cv;
	}

	public ContentValues getDetailListValue(SmsHistoryTable detail) {
		// TODO Auto-generated method stub
		ContentValues newValue = new ContentValues();
		newValue.put("time", detail.getTime());
		newValue.put("sent_status", detail.getSentStatus());
		return newValue;
	}

}