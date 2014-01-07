package com.cognizant.trumobi.em.activity.setup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.TruBoxSDK.SharedPreferences;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobBaseEditText;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseActivity;
import com.cognizant.trumobi.em.EmEmController;
import com.cognizant.trumobi.log.EmailLog;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.format.DateFormat;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

public class EmOofSettingsActivity extends TruMobiBaseActivity implements
		OnCheckedChangeListener, OnClickListener {

	private ProgressDialog getDialog;
	private GetSettings mGetSettingsTask;
	private SetSettings mSetSettingsTask;
	private Context mContext;
	ToggleButton settings_on_off;
	Button startDate, endDate;
	FrameLayout btnDone, btnDiscard;
	TruMobBaseEditText replyMessage;
	private Calendar oofCal, oofStart, oofend;
	private long mAccountId;
	private EmEmController mController;
	private final int DATE_PICKER_START = 10, DATE_PICKER_END = 20;
	private static boolean mEnabled = false;
	private static int sYear, sMonth, sDay, sDate, eYear, eMonth, eDay, eDate;
	private String[] allMonths = { "Jan", "Feb", "Mar", "Apr", "May", "Jun",
			"July", "Aug", "Sep", "Oct", "Nov", "Dec" };
	private static String oofReplyDefault = "OOO", oofReply = "OOO";
	//public static String EMAIL_OOF_PREFS_FILE = "emailoofsettings";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.em_oofsettings);

		mContext = this;

		mGetSettingsTask = new GetSettings();
		// mSetSettingsTask = new SetSettings();

		getDialog = new ProgressDialog(this, R.style.NewDialog);
		getDialog.setMessage("updating");
		getDialog.setProgress(-1);
		getDialog.setCancelable(false);
		getDialog.setCanceledOnTouchOutside(false);
		getDialog.show();

		mGetSettingsTask.execute();

		mAccountId = getIntent().getLongExtra("ACCOUNT_ID", -1);

		oofReplyDefault = "I'm out of office ";

		startDate = (Button) findViewById(R.id.oof_start_date);
		endDate = (Button) findViewById(R.id.oof_end_date);
		replyMessage = (TruMobBaseEditText) findViewById(R.id.oof_message);
		btnDone = (FrameLayout) findViewById(R.id.oof_settings_done);
		btnDiscard = (FrameLayout) findViewById(R.id.oof_settings_discard);

		btnDone.setOnClickListener(this);
		btnDiscard.setOnClickListener(this);
		startDate.setOnClickListener(this);
		endDate.setOnClickListener(this);

		mEnabled = false;

		EmailLog.e("oofsettingssuccessoofset", "oofsettingssuccess " + mEnabled);
		settings_on_off = (ToggleButton) findViewById(R.id.settings_on_off);
		settings_on_off.setOnCheckedChangeListener(this);
		if (!settings_on_off.isChecked() && !mEnabled)
			enableFields(false);
		else if (!mEnabled)
			setdefaults();
		else if (mEnabled) {
			enableFields(true);
			setValues();
		}

	}

	private boolean isOofEnabled() {
		// SharedPreferences oofPreferences = getSharedPreferences(
		// EMAIL_OOF_PREFS_FILE, MODE_APPEND);
		SharedPreferences oofPreferences = new SharedPreferences(mContext);
		return oofPreferences.getBoolean("oofsettingssuccess", false);
	}

	private void setValues() {
		// TODO Auto-generated method stub
		try {
			settings_on_off.setChecked(true);
			oofStart = Calendar.getInstance();
			oofend = Calendar.getInstance();
			// SharedPreferences oofPreferences = getSharedPreferences(
			// EMAIL_OOF_PREFS_FILE, MODE_APPEND);
			SharedPreferences oofPreferences = new SharedPreferences(mContext);
			mEnabled = oofPreferences.getBoolean("oofsettingssuccess", false);

			SimpleDateFormat dFormat = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ss.sss'Z'");
			dFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

			oofStart.setTime(dFormat.parse(oofPreferences.getString(
					"oofstartdate", "")));
			oofend.setTime(dFormat.parse(oofPreferences.getString("oofenddate",
					"")));
			oofReply = oofPreferences.getString("oofreplymessage", "");
			changeDates(DATE_PICKER_START, oofStart.get(Calendar.YEAR),
					oofStart.get(Calendar.MONTH), oofStart.get(Calendar.DATE),
					false);
			changeDates(DATE_PICKER_END, oofend.get(Calendar.YEAR),
					oofend.get(Calendar.MONTH), oofend.get(Calendar.DATE),
					false);
			EmailLog.e("ccccccccccccccc", "jjjjjjjjj: " + oofStart.getTime());
			EmailLog.e("ddddddddddddddd", "jjjjjjjjj: " + oofend.getTime());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setdefaults() {
		// TODO Auto-generated method stub
		oofCal = Calendar.getInstance();
		sYear = oofCal.get(Calendar.YEAR);
		sMonth = oofCal.get(Calendar.MONTH);
		sDay = oofCal.get(Calendar.DAY_OF_WEEK);
		sDate = oofCal.get(Calendar.DATE);

		String mDayName = DateFormat.format("EE",
				new Date(sYear, sMonth, sDay - 1)).toString();

		String cDate = new StringBuilder(mDayName + ", " + allMonths[sMonth]
				+ " " + sDate + ", " + sYear).toString();
		startDate.setText(cDate);

		// oofCal.add(Calendar.DATE, 1);
		eYear = sYear;// oofCal.get(Calendar.YEAR);
		eMonth = sMonth;// oofCal.get(Calendar.MONTH);
		eDay = sDay;// oofCal.get(Calendar.DAY_OF_WEEK);
		eDate = sDate;// oofCal.get(Calendar.DATE);

		mDayName = DateFormat.format("EE", new Date(eYear, eMonth, eDay - 1))
				.toString();

		cDate = new StringBuilder(mDayName + ", " + allMonths[eMonth] + " "
				+ eDate + ", " + eYear).toString();
		endDate.setText(cDate);
		String rMessage = ((startDate.getText().toString()).equals((endDate
				.getText().toString()))) ? ("on " + startDate.getText()
				.toString()) : ("from " + startDate.getText().toString()
				+ " to " + endDate.getText().toString());

		replyMessage
				.setText(mEnabled ? oofReply : (oofReplyDefault + rMessage));

	}

	private void changeDates(int dialogId, int year, int month, int day,
			boolean isdefault) {
		// TODO Auto-generated method stub
		String mDayName, cDate;
		// EmailLog.e("Dialogggggggggggggggg", "day : " + day + " month : " +
		// month
		// + " year : " + year);
		if (dialogId == DATE_PICKER_START) {
			sYear = year;
			sMonth = month;
			sDate = day;

			mDayName = DateFormat.format("EE",
					new Date(sYear, sMonth, sDate - 1)).toString();

			cDate = new StringBuilder(mDayName + ", " + allMonths[sMonth] + " "
					+ sDate + ", " + sYear).toString();
			startDate.setText(cDate);
			// Calendar endTemp = Calendar.getInstance();
			// endTemp.set(sYear, sMonth, sDate);
			// endTemp.add(Calendar.DATE, 1);
			changeDates(DATE_PICKER_END, sYear, sMonth, sDate, true);
		} else if (dialogId == DATE_PICKER_END) {
			eYear = year;
			eMonth = month;
			eDate = day;

			mDayName = DateFormat.format("EE",
					new Date(eYear, eMonth, eDate - 1)).toString();

			cDate = new StringBuilder(mDayName + ", " + allMonths[eMonth] + " "
					+ eDate + ", " + eYear).toString();
			endDate.setText(cDate);
		}
		
		setStartEndDates();
		String rMessage = ((startDate.getText().toString()).equals((endDate
				.getText().toString()))) ? ("on " + startDate.getText()
				.toString()) : ("from " + startDate.getText().toString()
				+ " to " + endDate.getText().toString());

		replyMessage.setText((mEnabled && !isdefault) ? oofReply
				: (oofReplyDefault + rMessage));
	}
	
	private void setStartEndDates()
	{
		oofStart = Calendar.getInstance();
		oofStart.set(sYear, sMonth, sDate, 00, 00, 00);
		oofStart.set(Calendar.MILLISECOND, 000);
		oofend = Calendar.getInstance();
		oofend.set(eYear, eMonth, eDate, 23, 30, 00);
		oofend.set(Calendar.MILLISECOND, 000);
		isValidEndDate();
	}
	
	private boolean isValidEndDate()
	{
		if((oofStart.getTime()).after(oofend.getTime()))
		{
			Toast.makeText(
					mContext,
					"Start Time should be less than the End Time",
					Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}
	
	private void enableFields(boolean enabled) {
		// TODO Auto-generated method stub
		
		if(!enabled)
		{
			startDate.setText("");
			endDate.setText("");
			replyMessage.setText("");
		}
		
		if (startDate != null)
			startDate.setEnabled(enabled);
		if (endDate != null)
			endDate.setEnabled(enabled);
		if (replyMessage != null)
			replyMessage.setEnabled(enabled);
	}

	private DatePickerDialog.OnDateSetListener mStartCallback = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int month, int day) {
			// TODO Auto-generated method stub
			EmailLog.e("Startttttttttttt", "year : " + year + " month : "
					+ month + " day : " + day);
			changeDates(DATE_PICKER_START, year, month, day, true);
		}

	};
	private DatePickerDialog.OnDateSetListener mEndCallback = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int month, int day) {
			// TODO Auto-generated method stub
			EmailLog.e("Endddddddddddddd", "year : " + year + " month : "
					+ month + " day : " + day);
			changeDates(DATE_PICKER_END, year, month, day, true);
		}

	};

	@Override
	@Deprecated
	protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		// return super.onCreateDialog(id);
		//int year = (id == DATE_PICKER_START) ? sYear : eYear;
		int month = (id == DATE_PICKER_START) ? sMonth : eMonth;
		int day = (id == DATE_PICKER_START) ? sDate : eDate;
		DatePickerDialog datePicker = new DatePickerDialog(this,
				(id == DATE_PICKER_START) ? mStartCallback : mEndCallback,
						sYear, sMonth, day);
//		Calendar cal = Calendar.getInstance();
//		cal.set(sYear, month, day);
//		datePicker.getDatePicker().setMinDate(cal.getTimeInMillis());
		return datePicker;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		if (!isChecked)
			enableFields(false);
		else {
			oofReplyDefault = "I'm out of office from ";
			enableFields(true);
			if (!mEnabled)
				setdefaults();
			else
				setValues();
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		switch (v.getId()) {
		case R.id.oof_start_date:
			showDialog(DATE_PICKER_START);
			break;
		case R.id.oof_end_date:
			showDialog(DATE_PICKER_END);
			break;
		case R.id.oof_settings_discard:
			// getoofsettings();
			finish();
			break;
		case R.id.oof_settings_done:
			if(oofStart == null || oofend == null)
				setStartEndDates();
			if (isValidEndDate()) {
				mSetSettingsTask = new SetSettings();
				mSetSettingsTask.execute();
				getDialog.show();
				break;
			}
		}

	}

	private boolean sendoofsettings() {

		mController = EmEmController.getInstance(getApplication());
		try {
			// Log.e("Dateeeeeeeeeee",
			// "sYear : "+sYear+" sMonth : "+sMonth+" sDay : "+sDate);
			if (settings_on_off.isChecked()) {
//				oofStart = Calendar.getInstance();
//				oofStart.set(sYear, sMonth, sDate, 00, 00, 00);
//				oofStart.set(Calendar.MILLISECOND, 000);
//				oofend = Calendar.getInstance();
//				oofend.set(eYear, eMonth, eDate, 23, 30, 00);
//				oofend.set(Calendar.MILLISECOND, 000);

				SimpleDateFormat dFormat = new SimpleDateFormat(
						"yyyy-MM-dd'T'HH:mm:ss.sss'Z'");
				dFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

				return mController.setOofSettings(getApplication(), mAccountId,
						true, replyMessage.getText().toString(),
						dFormat.format(oofStart.getTime()),
						dFormat.format(oofend.getTime()));

			} else {
				mEnabled = false;
				return mController.setOofSettings(this, mAccountId, false,
						null, null, null);
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	private boolean getoofsettings() {

		if (mController == null) {
			mController = EmEmController.getInstance(getApplication());
			try {
				return mController.getOofSettings(mAccountId, this);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}

	private class GetSettings extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub
			return getoofsettings();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			if (result) {
				getDialog.dismiss();

				if (isOofEnabled()) {
					enableFields(true);
					setValues();
				}
				// else
				// setdefaults();
			} else {
				getDialog.dismiss();
				Toast.makeText(mContext, "Connectivity error",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	private class SetSettings extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub
			return sendoofsettings();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			EmailLog.e("SetSettings", "resultresultresult " + result);
			if (result) {
				getDialog.dismiss();
				Toast.makeText(
						mContext,
						isOofEnabled() ? R.string.account_oof_settings_summary_on
								: R.string.account_oof_settings_summary_off,
						Toast.LENGTH_SHORT).show();
				finish();
			} else {
				getDialog.dismiss();
				Toast.makeText(mContext, "Connectivity error",
						Toast.LENGTH_SHORT).show();
			}

		}
	}

}
