package com.cognizant.trumobi.calendar.event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.TruBoxSDK.SharedPreferences;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.calendar.provider.CalendarDatabaseHelper;
import com.cognizant.trumobi.calendar.util.CalendarCommonFunction;
import com.cognizant.trumobi.calendar.util.CalendarConstants;
import com.cognizant.trumobi.calendar.view.CalendarAlertDialogActivity;
import com.cognizant.trumobi.calendar.view.CalendarMainActivity;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.log.CalendarLog;
import com.cognizant.trumobi.persona.PersonaMainActivity;


public class CalendarMyAlarmService extends Service

{
	private NotificationManager mManager;
	private static final int NOTIFICATION = 123;
	private SharedPreferences settings;
	private String mTitle="";
	private String mNotificationType = "";
	NotificationManager notificationmanager;
	PendingIntent pendingIntent;
	ArrayList<String> eventID ;
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		settings = new SharedPreferences(Email.getAppContext());
		notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		eventID = new ArrayList<String>();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		try {
			// Set custom UI to Notification
			
			notificationmanager.cancel(102);
			mNotificationType = intent.getStringExtra(CalendarConstants.FLAG_NOTIFICATION_TYPE);
			ArrayList<String> eventCountList = totalEventCount();//intent.getStringArrayListExtra(CalendarConstants.MSG);
			ArrayList<String> eventList = new ArrayList<String>();
			
			if(eventCountList.size()>0)
			{
				for(int i =0;i<eventCountList.size();i++)
				{
					boolean flag = CalendarDatabaseHelper.compareCurrentTimeToSnooze(eventCountList.get(i));
					if(flag)
					{
						eventList.add(eventCountList.get(i));
					}
				}
				
				mTitle = "Event" + "(" + eventList.size() + ")";
				if((mNotificationType.equals("Notification & Pop-Up") && (eventList.size()>0)))
				{
					launchNotification(intent);
					launchAlert(intent);
					
				}
				else if(eventList.size()>0)
				{
					launchNotification(intent);
					
				}
				else
				{
					CalendarDatabaseHelper.launchNotification(true, System.currentTimeMillis()+300000);
				}
				
			}
			else
			{
				CalendarDatabaseHelper.launchNotification(true, System.currentTimeMillis()+300000);
			}

		} catch (Exception exception) {
			CalendarLog.e(CalendarConstants.Tag,
					"CalendarMyAlarmService:onStart" + exception.toString());
		}
		
	
	}

	private ArrayList<String> totalEventCount() {
		ArrayList<String> reminderEventList = new ArrayList<String>();
		ArrayList<String> arr_DismissedEvent = CalendarCommonFunction.fetchSavedEventFromPreference();
		String strDate = Email.getPresentDay();
		Email.setPresentDate(strDate);
		Date date;
		try {
			date = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
					.parse(Email.getPresentDay());
			Calendar cal = Email.getNewCalendar();
			cal.setTime(date);
			SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy",
					Locale.ENGLISH);

			ArrayList<ContentValues> dayEventsList = CalendarDatabaseHelper
					.getDayListValues(sdf.format(cal.getTime()));
			
			
			for (int i = 0; i < dayEventsList.size(); i++) {
				if (dayEventsList.get(i).getAsString("calendarType")
						.equals("CorporateCalendar")
						&& !dayEventsList.get(i).getAsString("event")
								.equals("")) {
					String[] event_count = dayEventsList.get(i)
							.getAsString("start_time").split(";");
					String[] event_ID = dayEventsList.get(i).getAsString("_id").split(";");
					for (int j = 0; j < event_count.length; j++) {

						if (!event_count[j].equalsIgnoreCase("")) {
							if(!arr_DismissedEvent.contains(event_ID[j]))
							{
								reminderEventList.add(event_count[j]);
							}
							eventID.add(event_ID[j]);
						
						}
					}
					
				}
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		return reminderEventList;
	}

	private void launchAlert(Intent intent) {
		 ActivityManager activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
		    List<RunningTaskInfo> services = activityManager
		            .getRunningTasks(Integer.MAX_VALUE);
	    if(!services.get(0).topActivity.getPackageName().toString().equalsIgnoreCase(CalendarConstants.APP_PCK_NAME))
	    {
			Intent launchAlert = new Intent(this,CalendarAlertDialogActivity.class);
			launchAlert.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			launchAlert.putExtra(CalendarConstants.MSG,mTitle);
			startActivity(launchAlert);
	    }
	}

	private void launchNotification(Intent intent) {
	
		// set Intent for notificaition click
		/*Intent launchMainActivity = new Intent(this,
				CalendarMainActivity.class);
		pendingIntent = PendingIntent.getActivity(this, 0,
				launchMainActivity, PendingIntent.FLAG_UPDATE_CURRENT);
		*/
	
			Intent launchMainActivity = new Intent(this,
					CalendarMainActivity.class);
			
			pendingIntent = PendingIntent.getActivity(this, 0,
					launchMainActivity, PendingIntent.FLAG_UPDATE_CURRENT);
			launchMainActivity.putExtra("fromNotification", "true");

		// set Intent for snooze button click
		Intent launchReceiver = new Intent(this, CalendarMyReceiver.class);
		launchReceiver.setAction("notification_snooze");
		PendingIntent snoozePendingIntent = PendingIntent.getBroadcast(this, 0,
				launchReceiver, PendingIntent.FLAG_CANCEL_CURRENT);
		//remoteViews.setOnClickPendingIntent(R.id.btn_snooze, snoozePendingIntent);
		
		// set Intent for dismiss button click
		Intent launchReceiver1 = new Intent(this, CalendarMyReceiver.class);
		launchReceiver1.setAction("notification_cancelled");
		launchReceiver1.putStringArrayListExtra("EventID",eventID);
		PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(this, 0,
				launchReceiver1, PendingIntent.FLAG_CANCEL_CURRENT);
		
		// Support lower verion
		
		int icon = R.drawable.pr_calendar_icon;        
        long when = System.currentTimeMillis();       
        Notification notification = new Notification(icon, "Calendar Notification", when);         
        NotificationManager mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);  
    	RemoteViews contentView = new RemoteViews(getPackageName(),
				R.layout.cal_reminder_notification);
    	contentView.setOnClickPendingIntent(R.id.btn_snooze, snoozePendingIntent);
    	
    	contentView.setOnClickPendingIntent(R.id.btn_dismiss,
				dismissPendingIntent);
    	// Set Title
    
    	contentView.setTextViewText(R.id.text_notification_content,mTitle);//Email.getEventCount()
    	
/*    	String ringtone = settings.getString(getString(R.string.key_ringtone_preferences), RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).toString());
    	
    	Ringtone ringtonevolume = RingtoneManager.getRingtone(this, Uri.parse(ringtone));
    	CalendarLog.e("ringtone","custome service"+ringtonevolume);
		Uri alarmSound = Uri.parse(ringtonevolume.toString());*/
		
    /*	Uri alarmSound = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		*/
    	
    	String rintone = settings.getString(CalendarConstants.RINGTONE_URI,RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).toString());
		
		Uri alarmSound = Uri.parse(rintone);

		if(alarmSound!=null)
		{
			notification.sound = alarmSound;
		}
	/*	else
		{
			alarmSound = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_ALARM);
			notification.sound = alarmSound;
		}*/
    	notification.contentView = contentView;    
    	notification.contentIntent = pendingIntent;   
    	notification.flags |= Notification.FLAG_AUTO_CANCEL; 
    	mNotificationManager.notify(102, notification);
	
	}
	
	@Override
	public void onTaskRemoved(Intent rootIntent) {
		super.onTaskRemoved(rootIntent);
		notificationmanager.cancel(102);
	}

	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

}