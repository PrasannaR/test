package com.cognizant.trumobi.dialer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CallLog;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.TruBoxSDK.SharedPreferences;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseSherlockFragmentBaseActivity;
import com.cognizant.trumobi.contacts.activity.ContactsAddContact;
import com.cognizant.trumobi.dialer.adapter.DialerCallHistoryAdapter;
import com.cognizant.trumobi.dialer.dbController.DialerCallHistoryList;
import com.cognizant.trumobi.dialer.dbController.DialerCallLogList;
import com.cognizant.trumobi.dialer.utils.DialerUtilities;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.log.DialerLog;
import com.cognizant.trumobi.messenger.sms.SmsMain;

public class DialerCallLogHistoryDetail extends TruMobiBaseSherlockFragmentBaseActivity {

	ListView callLoglist;
	TextView contact_name, contact_phone_no_value, contactNumberType;
	ImageView mContactPhoto, sendSms;
	
	ArrayList<DialerCallLogList> mCallList = new ArrayList<DialerCallLogList>();
	ArrayList<String> callDurationList=new ArrayList<String>();
	DialerCallLogList historyModel = null;
	DialerCallLogList updatedResult=null;
	DialerCallHistoryList historyList = null;
	DialerCallHistoryAdapter mAdapter;

	String contactName = "";
	String contactNum = "";
	String contactNumType = "";
	String callType = "";
	byte[] contactPhoto=null;
	String callId="";
	String callDate="";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialer_calllog_history);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setIcon(R.drawable.pr_dialer_icon);
		getSupportActionBar().setTitle("");
		callLoglist = (ListView) findViewById(R.id.call_log_list);
		contact_name = (TextView) findViewById(R.id.contact_name);
		contact_phone_no_value = (TextView) findViewById(R.id.contact_phone_no_value);
		mContactPhoto = (ImageView) findViewById(R.id.contact_detail_image);
		sendSms = (ImageView) findViewById(R.id.textmessage);
		contactNumberType = (TextView) findViewById(R.id.phoneType);
		//historyList = new DialerCallHistoryList();
		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();
		if (bundle != null) {
			Serializable object = bundle.getSerializable("objCallHistory");
			historyModel = (DialerCallLogList) object;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.dialer_menu_callhistory, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.menu_removecalllog:
			//String where = CallLog.Calls.NUMBER + " = ? ";
			String epochDate="";
			
			SharedPreferences.Editor prefEditor = new SharedPreferences(
					Email.getAppContext()).edit();
			prefEditor.putBoolean("isCallLogUpdated", true);
			prefEditor.commit();
			
			//String where=CallLog.Calls._ID + " = ? " + " AND " + CallLog.Calls.NUMBER + " = ? ";/*Deletes Record one by one that Matches*/
			//String where=CallLog.Calls._ID + " = ? " + " OR " + CallLog.Calls.NUMBER + " = ? "; /*Deletes Entire Record Matches*/
			//String where=CallLog.Calls._ID + " = ? ";
			String where=CallLog.Calls.DATE + " = ?"+" AND " + CallLog.Calls.NUMBER + " = ? ";
			for (int i=0;i<callDurationList.size();i++) {
				try {
					epochDate=callDurationList.get(i);
					int deletedRecord = Email
							.getAppContext()
							.getContentResolver()
							.delete(CallLog.Calls.CONTENT_URI, where,
									new String[] { /*callId,contactNum*/ epochDate,contactNum});
					DialerLog.e("deletedRecord  ", deletedRecord + "");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			finish();
			break;
		case R.id.menu_editnumber:
			Intent intent = new Intent(DialerCallLogHistoryDetail.this,
					DialerParentActivity.class);
			intent.putExtra("number_edit", contactNum);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
			break;
		}
		return true;
	}
	
	private void populateHistoryDetail(DialerCallLogList mHistoryList) {
		
		contactName = mHistoryList.getASSOICIATE_NAME();
		contactNum = mHistoryList.getNUMBER();
		contactNumType = mHistoryList.getNUMBER_TYPE();
		callId=mHistoryList.getFOREIGN_KEY_STRING();
		if (contactName != null ) {
			if (contactName.equalsIgnoreCase("")) {
				contact_name.setText("Add to contacts");
			} else {
				contact_name.setText(contactName);
			}
		} else {
			contact_name.setText("Add to contacts");
		}
		
		ArrayList<DialerCallHistoryList> mListItems = new ArrayList<DialerCallHistoryList>();
		contact_name.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				SharedPreferences.Editor prefEditor = new SharedPreferences(
						Email.getAppContext()).edit();
				prefEditor.putBoolean("isCallLogUpdated", true);
				prefEditor.commit();
				
				if (contactName.equalsIgnoreCase("")) {
					
					Intent intent3 = new Intent(DialerCallLogHistoryDetail.this,
							ContactsAddContact.class);
					if (contactNum != null
							&& !contactNum.isEmpty()) {
						intent3.putExtra("new_num", contactNum);
					}
					startActivity(intent3);
				}
			}
		});
		if (mHistoryList.isNativeContact()) {
			contactPhoto = DialerUtilities.getImageNew(contactNum, this);//historyModel.getImg_src();
			if (contactPhoto != null) {
				mContactPhoto
						.setImageBitmap(DialerUtilities.getImage(contactPhoto));
			} else {
				mContactPhoto
						.setImageResource(R.drawable.contacts_ic_contact_picture_holo_light);
			}
		} else {
			contactPhoto = mHistoryList.getImg_src();
			if (contactPhoto != null) {
				mContactPhoto
						.setImageBitmap(DialerUtilities.getImage(contactPhoto));
			} else {
				mContactPhoto
						.setImageResource(R.drawable.contacts_ic_contact_picture_holo_light);
			}
		}
		
		
		contactNumberType.setText(contactNumType);

		mCallList.add(mHistoryList);
		String callTimes = mHistoryList.getCALL_NO_TIMES_STRING();
		ArrayList<String> callTypeList = mHistoryList.getCALL_TYPE_STRING();
		callDurationList=mHistoryList.getCALL_DURATION();
		Date duration;

		int callCount = Integer.parseInt(callTimes);
		//historyList.setCallCount(callCount);
		for (int i = 0; i < callCount; i++) {
			historyList = new DialerCallHistoryList();
			duration = new Date(Long.parseLong(callDurationList.get(i)));
			String date=DialerUtilities
					.frmSysCal(("HH:mm','EEEE ',' MMM dd','yyyy"),duration);
			DialerLog.e("New", date);
			historyList.setCallDate(date);
			historyList.setCallTime(DialerUtilities.diffInTime(duration,
					DialerUtilities.frmString(DialerUtilities.frmSysCal())));
			DialerLog.e("Call Type List", "call TypeList  ->>  "+callTypeList.get(i));
			historyList.setCallType(callTypeList.get(i));
			mListItems.add(historyList);

		}

		
		contact_phone_no_value.setText("Call " + contactNum);

		contact_phone_no_value.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				makeCall();
			}
		});

		sendSms.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				sendSms();
			}
		});

		mAdapter = new DialerCallHistoryAdapter(Email.getAppContext(),
				mListItems);
		callLoglist.setAdapter(mAdapter);
	
	}

	private void sendSms() {
		
		if (DialerUtilities.checkSim()) {
			// The phone has SIM card

			try {
				Intent sms_intent = new Intent(this, SmsMain.class);
				sms_intent.putExtra("con_phone", contactNum);
				if (contactName != null && !contactName.equalsIgnoreCase("")) { 
					sms_intent.putExtra("con_name", contactName);
				}
				else {
					sms_intent.putExtra("con_name", contactNum);
				}
					
				if (contactPhoto != null) {
					sms_intent.putExtra("con_image", contactPhoto);
				}
				startActivity(sms_intent);

			} catch (ActivityNotFoundException activityException) {
				Log.e("sendsms activity not found", "Call failed");
			}

		} else {
			// No SIM card on the phone

			Toast.makeText(this, "Insert SIM to send message",
					Toast.LENGTH_LONG).show();
		}

	}
	
	private void makeCall() {
		if (DialerUtilities.checkSim()) {
			/*DialerParentActivity.phoneCallIntent(contactNum,
					getBaseContext());*/
			DialerUtilities.phoneCallIntent(contactNum, getBaseContext());
		} else {
			Toast.makeText(this, "Insert SIM to make call",
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {

	}

	@Override
	protected void onResume() {
		if (historyModel != null) {
			if (historyModel.isNativeContact()) {
				String nativeName=historyModel.getASSOICIATE_NAME();
				if (nativeName != null) {
					if (nativeName.isEmpty()) {
						updatedResult=DialerUtilities.populateCorporateCallLogList(historyModel);
					} else {
						updatedResult= historyModel;
					}
				}
			} else {
				updatedResult=DialerUtilities.populateCorporateCallLogList(historyModel);
			}
			
			if (updatedResult != null) {
				populateHistoryDetail(updatedResult);
			}
					
			
		}
		super.onResume();

	}
}
