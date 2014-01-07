package com.cognizant.trumobi.persona;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.calendar.provider.CalendarDatabaseHelper;
import com.cognizant.trumobi.calendar.util.CalendarConstants;
import com.cognizant.trumobi.log.CalendarLog;
import com.cognizant.trumobi.log.PersonaLog;

public class PersonaSyncReceiver extends BroadcastReceiver {

	
	Context mContext;
	String date,dayOfTheWeek;
	PendingIntent mEventElapsedPendingIntent;
	AlarmManager mEventElapsedAlarmManager;
	private String TAG = PersonaSyncReceiver.class.getSimpleName();
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
		mContext = context;
		
		PersonaLog.e("PersonaSyncReceiver","PersonaSyncReceiver received for expected action" + intent.getAction());
		if (intent.getAction().equals("com.cognizant.trumobi.calendar.sync_completed")) {
			 ArrayList<ContentValues> eventsList = getValuesFromDB();
			 if (eventsList != null && !eventsList.isEmpty()) {
					String endTime = eventsList.get(0).getAsString("end_time");
					startAlarmServiceForEventEndTime(context,endTime);
				}
	            updateWidgetPictureAndButtonListener(mContext,eventsList);
	        }
		else if(intent.getAction().equals("com.cognizant.trumobi.calendar.event_elapsed")) {
			 ArrayList<ContentValues> eventsList = getValuesFromDB();
			 SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String nowAsString = sd.format(System.currentTimeMillis());
			  	try {
				Date d = sd.parse(nowAsString);
				PersonaLog.d("PersonaMainActivity","Getting events after date"+ d);
			  	}
			  	catch (Exception e) {
			  		
			  	}
			 if (eventsList != null && !eventsList.isEmpty()) {
					String endTime = eventsList.get(0).getAsString("end_time");
					startAlarmServiceForEventEndTime(context,endTime);
				}
	            updateWidgetPictureAndButtonListener(mContext,eventsList);
		}
	
		
		
	}

	public ArrayList<ContentValues> getValuesFromDB() {
		
		
		PersonaLog.e("PersonaSyncReceiver","PersonaSyncReceiver  "+ "received");
		Calendar calendar = Calendar.getInstance();		
		Date now = new Date(calendar.getTimeInMillis());	
		date = CalendarDatabaseHelper.getPersonaDateFromLong(now.getTime());	
		PersonaLog.e("PersonaSyncReceiver","PersonaSyncReceiver  "+ date);	
		SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
		Date d = new Date();
		dayOfTheWeek = sdf.format(d);
		ArrayList<ContentValues> contentValuesList= new ArrayList<ContentValues>();
		Calendar cal=Calendar.getInstance();
		CalendarLog.d(CalendarConstants.tTag,"orig time "+cal.getTimeInMillis());
		cal.setTimeZone(TimeZone.getTimeZone("GMT"));
		CalendarLog.d(CalendarConstants.tTag,"gmt time "+cal.getTimeInMillis());
		contentValuesList = CalendarDatabaseHelper.getPersonaDayValues(cal.getTimeInMillis());	
		PersonaLog.e("PersonaSyncReceiver","contentValues  "+ contentValuesList.toString() + contentValuesList.size());
	
		return contentValuesList;
		
		
	}

	public void updateWidgetPictureAndButtonListener(Context context,ArrayList<ContentValues> eventsList) {
		
		
		int totalEventsForToday;
		
	totalEventsForToday = eventsList.size();
	PersonaLog.e("PersonaSyncReceiver","totalEventsForToday" + totalEventsForToday);
	switch(totalEventsForToday) {
	case 0:
		updateNoEvents(context);
		break;
	case 1:
		updateSingleEvent(eventsList,context);
		break;
	default:
		updateTwoOrMoreEvents(eventsList,totalEventsForToday,context);
		break;
	}
		
	
		
		
		
	}

	private void updateTwoOrMoreEvents(ArrayList<ContentValues> eventsList,int totalEvents,Context cntxt) {
		RemoteViews remoteViews = new RemoteViews(cntxt.getPackageName(),
                R.layout.pr_calendar_widget_listitem);
		
		remoteViews.setOnClickPendingIntent(R.id.pr_calendar_widget_listitem_layout, PersonaCalendarWidgetProvider.buildViewPendingIntent(cntxt));
		
		String event,event2;
		String eventLocation, event2Location;
		String eventStartTime, eventEndTime;
		String event2StartTime, event2EndTime;
		int remainingEvents = totalEvents - 2;
		event = eventsList.get(0).getAsString("event");
		eventLocation = eventsList.get(0).getAsString("location");
		eventStartTime = eventsList.get(0).getAsString("start_time");
		eventEndTime = eventsList.get(0).getAsString("end_time");
		
		event2 = eventsList.get(1).getAsString("event");
		event2Location = eventsList.get(1).getAsString("location");
		event2StartTime = eventsList.get(1).getAsString("start_time");
		event2EndTime = eventsList.get(1).getAsString("end_time");
		
		remoteViews.setTextViewText(R.id.pr_calendar_widget_listitem_day,dayOfTheWeek);
		remoteViews.setTextViewText(R.id.pr_calendar_widget_listitem_date,date);
		remoteViews.setTextViewText(R.id.pr_calendar_widget_listitem_header_second_row_total_events, totalEvents+"");
		
		  remoteViews.setTextViewText(R.id.pr_calendar_widget_listitem_eventtitle, event);
	      remoteViews.setTextViewText(R.id.pr_calendar_widget_listitem_eventvenue, eventLocation);
	       remoteViews.setTextViewText(R.id.pr_calendar_widget_listitem_time, eventStartTime + "-" +eventEndTime);
	       
	        remoteViews.setTextViewText(R.id.pr_calendar_widget_listitem_eventtitle2, event2);
	        remoteViews.setTextViewText(R.id.pr_calendar_widget_listitem_eventvenue2, event2Location);
	        remoteViews.setTextViewText(R.id.pr_calendar_widget_listitem_time2,  event2StartTime + "-" +event2EndTime);
	 
	        remoteViews.setViewVisibility(R.id.pr_calendar_widget_listitem_icon, View.VISIBLE);
	        remoteViews.setViewVisibility(R.id.pr_calendar_widget_listitem_icon2, View.VISIBLE);
	 
	       
	        if(remainingEvents==1) {
	        	 remoteViews.setViewVisibility(R.id.pr_calendar_widget_listitem_moreevents,View.INVISIBLE);
	        remoteViews.setTextViewText(R.id.pr_calendar_widget_listitem_moreevents, ""+remainingEvents+ " more event.." );
	        }
	        else if(remainingEvents>=2){
	        	 remoteViews.setViewVisibility(R.id.pr_calendar_widget_listitem_moreevents,View.INVISIBLE);
	        	  remoteViews.setTextViewText(R.id.pr_calendar_widget_listitem_moreevents, ""+remainingEvents+ " more events.." );
	        	 }
        
        PersonaCalendarWidgetProvider.pushWidgetUpdate(cntxt.getApplicationContext(),
                remoteViews);
		
	}

	private void updateSingleEvent(ArrayList<ContentValues> eventValues,Context cntxt) {
		
		String event;
		String eventLocation;
		String eventStartTime, eventEndTime;
		event = eventValues.get(0).getAsString("event");
		eventLocation = eventValues.get(0).getAsString("location");
		eventStartTime = eventValues.get(0).getAsString("start_time");
		eventEndTime = eventValues.get(0).getAsString("end_time");
		
		RemoteViews remoteViews = new RemoteViews(cntxt.getPackageName(),
                R.layout.pr_calendar_widget_listitem);
 
		remoteViews.setOnClickPendingIntent(R.id.pr_calendar_widget_listitem_layout, PersonaCalendarWidgetProvider.buildViewPendingIntent(cntxt));
        // updating view
		
		remoteViews.setTextViewText(R.id.pr_calendar_widget_listitem_day,dayOfTheWeek);
		remoteViews.setTextViewText(R.id.pr_calendar_widget_listitem_date,date);
		remoteViews.setTextViewText(R.id.pr_calendar_widget_listitem_header_second_row_total_events, "1");
		
        remoteViews.setTextViewText(R.id.pr_calendar_widget_listitem_eventtitle, event);
        remoteViews.setTextViewText(R.id.pr_calendar_widget_listitem_eventvenue, eventLocation);
        remoteViews.setTextViewText(R.id.pr_calendar_widget_listitem_time, eventStartTime + "-" +eventEndTime);
       
        remoteViews.setTextViewText(R.id.pr_calendar_widget_listitem_eventtitle2, "");
        remoteViews.setTextViewText(R.id.pr_calendar_widget_listitem_eventvenue2, "");
        remoteViews.setTextViewText(R.id.pr_calendar_widget_listitem_time2, "");
 
        remoteViews.setViewVisibility(R.id.pr_calendar_widget_listitem_icon, View.VISIBLE);
        remoteViews.setViewVisibility(R.id.pr_calendar_widget_listitem_icon2, View.INVISIBLE);
        
        remoteViews.setViewVisibility(R.id.pr_calendar_widget_listitem_moreevents,View.INVISIBLE);
        
        PersonaCalendarWidgetProvider.pushWidgetUpdate(cntxt.getApplicationContext(),
                remoteViews);
	}

	private void updateNoEvents(Context cntxt) {
		RemoteViews remoteViews = new RemoteViews(cntxt.getPackageName(),
                R.layout.pr_calendar_widget_listitem);
 
		remoteViews.setOnClickPendingIntent(R.id.pr_calendar_widget_listitem_layout, PersonaCalendarWidgetProvider.buildViewPendingIntent(cntxt));
		remoteViews.setTextViewText(R.id.pr_calendar_widget_listitem_day,dayOfTheWeek);
		remoteViews.setTextViewText(R.id.pr_calendar_widget_listitem_date,date);
		remoteViews.setTextViewText(R.id.pr_calendar_widget_listitem_header_second_row_total_events, "");
		
        // updating view
        remoteViews.setTextViewText(R.id.pr_calendar_widget_listitem_eventtitle2, "No upcoming events.");
        remoteViews.setTextViewText(R.id.pr_calendar_widget_listitem_eventtitle, "");
        remoteViews.setTextViewText(R.id.pr_calendar_widget_listitem_eventvenue, "");
        remoteViews.setTextViewText(R.id.pr_calendar_widget_listitem_eventvenue2, "");
        remoteViews.setTextViewText(R.id.pr_calendar_widget_listitem_time, "");
        remoteViews.setTextViewText(R.id.pr_calendar_widget_listitem_time2, "");
        remoteViews.setViewVisibility(R.id.pr_calendar_widget_listitem_icon, View.INVISIBLE);
        remoteViews.setViewVisibility(R.id.pr_calendar_widget_listitem_icon2, View.INVISIBLE);
        remoteViews.setViewVisibility(R.id.pr_calendar_widget_listitem_moreevents,View.INVISIBLE);
        
        PersonaCalendarWidgetProvider.pushWidgetUpdate(cntxt.getApplicationContext(),
                remoteViews);
		
	}	
	
	
	public void startAlarmServiceForEventEndTime(Context mContext,String mEndTime) {
	      
	      mEventElapsedPendingIntent = PendingIntent.getBroadcast( mContext, 0, new Intent("com.cognizant.trumobi.calendar.event_elapsed"),	0 );
	      mEventElapsedAlarmManager = (AlarmManager)(mContext.getSystemService( Context.ALARM_SERVICE ));
	   
	      	PersonaLog.e(TAG, "endTime: "+mEndTime); 
			SimpleDateFormat sdf= new SimpleDateFormat("HH:mm");
			Date mAlarmTimeDate = null;
			long mAlarmEndTimeLong = 0;
			try {
					Date mDateWithEndTime = sdf.parse(mEndTime);
					PersonaLog.d(TAG, ": "+mDateWithEndTime);
					long mEndTimeLong = mDateWithEndTime.getTime();
					Calendar mCalendar = Calendar.getInstance();
					mCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
					mCalendar.set(mCalendar.get(Calendar.YEAR),mCalendar.get(Calendar.MONTH),mCalendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
					Date mTodayDateMidnight = mCalendar.getTime();
				
					long addedTime = mTodayDateMidnight.getTime() + mEndTimeLong + (1000 * 60); //1000ms added so that event will be there in widget till the last minute of end time
					mAlarmTimeDate = new Date(addedTime);
					PersonaLog.d(TAG,"Calendar date"+ mTodayDateMidnight + "AlarmDate" + mAlarmTimeDate);
					mAlarmEndTimeLong = mAlarmTimeDate.getTime();
					if(mAlarmEndTimeLong >= System.currentTimeMillis()) {
						 mEventElapsedAlarmManager.set( AlarmManager.RTC, mAlarmEndTimeLong, mEventElapsedPendingIntent );
					      PersonaLog.d("PersonaSyncReceiver","Alarm is set");
					}
					else {
						//This case comes in some error scenarios when events that is elapsed comes in content values
						PersonaLog.d("PersonaSyncReceiver", "Alarm is not set-- endtime is lesser than currenttime");
					}
				/*	String nowAsString = sd.format(mAlarmTimeDate);
				  	SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Date mDate = sd.parse(nowAsString);
					PersonaLog.d("PersonaMainActivity","Date is"+ mDate);*/
			}
			catch(Exception e) {
				e.printStackTrace();
			}
	     
	  
	}
	

	
	
}
