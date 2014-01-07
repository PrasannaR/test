package com.cognizant.trumobi.calendar.event;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.cognizant.trumobi.calendar.util.CalendarConstants;

public class AlarmTask implements Runnable {
	// The date selected for the alarm
	private final Calendar mDate;
	// The android system alarm manager
	private final AlarmManager mAlarmManager;
	// Your context to retrieve the alarm manager from
	private final Context mContext;
	// Repeat interval
	private final long repeat;

	public AlarmTask(Context context, Calendar date, long repeat) {
		this.mContext = context;
		this.mAlarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		this.mDate = date;
		this.repeat = repeat;
	}

	@Override
	public void run() {
		// Request to start are service when the alarm date is upon us
		// We don't start an activity as we just want to pop up a notification
		// into the system bar not a full activity
		long repeatTime=0;
		Intent intent = new Intent(mContext, CalendarNotifyService.class);
		intent.putExtra(CalendarNotifyService.INTENT_NOTIFY, true);
		PendingIntent pendingIntent = PendingIntent.getService(mContext, 0,
				intent, 0);

		if (repeat == 0) {
			// Sets an alarm - note this alarm will be lost if the phone is
			// turned off and on again
			mAlarmManager.set(AlarmManager.RTC, mDate.getTimeInMillis(), pendingIntent);
		} else {
			//To repeat the interval
			if(repeat==1){
				intent.putExtra(CalendarConstants.FLAG,true);
				repeatTime=(AlarmManager.INTERVAL_DAY);
			}else if(repeat==2){
				intent.putExtra(CalendarConstants.FLAG,true);
				repeatTime=(AlarmManager.INTERVAL_DAY*7);
			}
			else if(repeat==3){
				intent.putExtra(CalendarConstants.MONTH,mDate.DAY_OF_MONTH);
				repeatTime=(AlarmManager.INTERVAL_DAY);
			}else if(repeat==4){
				intent.putExtra(CalendarConstants.YEAR,mDate.DAY_OF_MONTH);
				intent.putExtra(CalendarConstants.MONTH,mDate.MONTH);
				repeatTime=(AlarmManager.INTERVAL_DAY);
			}
			mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, mDate.getTimeInMillis(),
					repeatTime, pendingIntent);
			
		}
		
	}
}
