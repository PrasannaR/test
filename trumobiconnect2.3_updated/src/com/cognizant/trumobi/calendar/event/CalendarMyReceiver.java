package com.cognizant.trumobi.calendar.event;

import java.util.ArrayList;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.TruBoxSDK.SharedPreferences;
import com.cognizant.trumobi.calendar.provider.CalendarDatabaseHelper;
import com.cognizant.trumobi.calendar.util.CalendarCommonFunction;
import com.cognizant.trumobi.calendar.util.CalendarConstants;
import com.cognizant.trumobi.em.Email;


public class CalendarMyReceiver extends BroadcastReceiver
{
	 
	@Override
	 public void onReceive(Context context, Intent intent)
	{
		
		if(intent.getAction()!=null)
		{
			if(intent.getAction().equals("notification_cancelled"))
	        {
				NotificationManager notificationmanager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
				notificationmanager.cancelAll();
				SharedPreferences mSecurePrefs = new SharedPreferences(
						Email.getAppContext());
				ArrayList<String> dismissEvent = intent.getStringArrayListExtra("EventID");
				mSecurePrefs.edit().putInt("eventlistsize",dismissEvent.size()).commit();
			
				CalendarCommonFunction.saveDismissedEventInPreference(dismissEvent);
				//CalendarDatabaseHelper.launchNotification(true, System.currentTimeMillis()+300000);
	        }
			else if(intent.getAction().equals("notification_snooze"))
			{
				NotificationManager notificationmanager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
				notificationmanager.cancelAll();
				// Set Alarm
				CalendarDatabaseHelper.launchNotification(true, System.currentTimeMillis()+300000);
				//CalendarDatabaseHelper.addReminderNotification("Notification",System.currentTimeMillis()+60000);
				/*
				AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
				Intent launchService = new Intent(context, CalendarMyAlarmService.class);
				PendingIntent snoozePendingIntent = PendingIntent.getService(context, 0,
						launchService, PendingIntent.FLAG_CANCEL_CURRENT);
				am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+60000,
						snoozePendingIntent);//+300000
				*/
			}
			else if(intent.getAction().equals("alert"))
			{
				// Do nothing
				CalendarDatabaseHelper.launchNotification(true, System.currentTimeMillis()+300000);
			}
			else
			{
				
			}
		}
		else 
		{
			//ArrayList<String> msg = intent.getStringArrayListExtra(CalendarConstants.MSG);
			String type = intent.getStringExtra(CalendarConstants.FLAG_NOTIFICATION_TYPE);
			Intent service1 = new Intent(context, CalendarMyAlarmService.class);
			//service1.putStringArrayListExtra(CalendarConstants.MSG, msg);
			service1.putExtra(CalendarConstants.FLAG_NOTIFICATION_TYPE, type);
			context.startService(service1);
		}
	/*	else
		{
			CalendarDatabaseHelper.launchNotification(true, System.currentTimeMillis()+60000);
		}*/
		
	
	   
	 }
	
}
