package com.cognizant.trumobi.calendar.view;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.view.Window;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.calendar.event.CalendarMyReceiver;
import com.cognizant.trumobi.calendar.util.CalendarConstants;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseActivity;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseSherlockActivity;
import com.cognizant.trumobi.em.Email;

public class CalendarAlertDialogActivity extends TruMobiBaseActivity {
	
	private String mEventcount = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature((int) Window.FEATURE_NO_TITLE);
		setContentView(R.layout.cal_reminder_pop_up);
		initialize();
	}

	private void initialize() {
		
		LinearLayout reminder_pop_up_parent_layout = (LinearLayout)findViewById(R.id.reminder_pop_up_parent_layout);
		reminder_pop_up_parent_layout.setOnTouchListener(parentLayoutlistener);
		
		TextView text_pop_up_content = (TextView)findViewById(R.id.text_pop_up_content);
		text_pop_up_content.setText("Event(10)");
		
		Button btn_pop_up_snooze = (Button)findViewById(R.id.btn_pop_up_snooze);
		btn_pop_up_snooze.setOnClickListener(snoozelistener);
		
		Button btn_pop_up_dismiss = (Button)findViewById(R.id.btn_pop_up_dismiss);
		btn_pop_up_dismiss.setOnClickListener(dismisslistener);
		
		if(getIntent()!=null)
		{
			text_pop_up_content.setText(getIntent().getStringExtra(CalendarConstants.MSG));
		}
		
	}
	
	private OnTouchListener parentLayoutlistener = new OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			Intent myIntent = new Intent(CalendarAlertDialogActivity.this,
					CalendarMainActivity.class);
			startActivity(myIntent);
			finish();
			return true;
		}
	};
	
	private OnClickListener snoozelistener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
		
			Intent myIntent = new Intent(Email.getAppContext(),
					CalendarMyReceiver.class);
			myIntent.setAction("alert");
			//myIntent.putExtra(CalendarConstants.MSG, mEventcount);
			//myIntent.putExtra(CalendarConstants.FLAG_NOTIFICATION_TYPE, "Notification & Pop-Up");
			AlarmManager alarmManager = (AlarmManager) Email
					.getAppContext().getSystemService(Service.ALARM_SERVICE);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(Email.getAppContext(), 0,
					myIntent,0);
			alarmManager.set(AlarmManager.RTC_WAKEUP,0,
					pendingIntent);
			finish();
		}
	};
	
	private OnClickListener dismisslistener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			finish();
		}
	};
}
