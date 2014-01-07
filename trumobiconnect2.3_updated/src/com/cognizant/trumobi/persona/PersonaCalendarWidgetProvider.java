/**
 * 
 */
package com.cognizant.trumobi.persona;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.calendar.provider.CalendarDatabaseHelper;
import com.cognizant.trumobi.calendar.util.CalendarUtility;
import com.cognizant.trumobi.log.PersonaLog;

/**
 * @author 290661
 * 
 */

public class PersonaCalendarWidgetProvider extends AppWidgetProvider {

	/**
	 * 
	 */
	private String TAG = PersonaCalendarWidgetProvider.class.getSimpleName();

	public PersonaCalendarWidgetProvider() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		// TODO Auto-generated method stub
		super.onDeleted(context, appWidgetIds);
	}

	@Override
	public void onDisabled(Context context) {
		// TODO Auto-generated method stub
		super.onDisabled(context);
	}

	@Override
	public void onEnabled(Context context) {
		// TODO Auto-generated method stub
		super.onEnabled(context);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		// TODO Auto-generated method stub
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		CalendarUtility.setTimeZone();
		PersonaLog.e("PersonaCalendarWidgetProvider", "widget onupdate");
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
				R.layout.pr_calendar_widget_listitem);

		Date mDate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
		String dayOfTheWeek  = sdf.format(mDate);
		mDate = new Date(Calendar.getInstance().getTimeInMillis());	
		String date = CalendarDatabaseHelper.getPersonaDateFromLong(mDate.getTime());	
		remoteViews.setOnClickPendingIntent(
				R.id.pr_calendar_widget_listitem_layout,
				PersonaCalendarWidgetProvider.buildViewPendingIntent(context));
		remoteViews.setOnClickPendingIntent(R.id.pr_calendar_widget_listitem_layout, PersonaCalendarWidgetProvider.buildViewPendingIntent(context));
		remoteViews.setTextViewText(R.id.pr_calendar_widget_listitem_day,dayOfTheWeek);
		remoteViews.setTextViewText(R.id.pr_calendar_widget_listitem_date,date);
		pushWidgetUpdate(context, remoteViews);
		
		// updating view with initial data

		/*PersonaSyncReceiver personaProvider = new PersonaSyncReceiver();
		ArrayList<ContentValues> eventsList = personaProvider.getValuesFromDB();
		if (eventsList != null && !eventsList.isEmpty()) {
			personaProvider.updateWidgetPictureAndButtonListener(context,
					eventsList);
			String endTime = eventsList.get(0).getAsString("end_time");
			personaProvider.startAlarmServiceForEventEndTime(context,endTime);
			
		}*/
		PersonaRetriveCalendarEvents retriveEvents = new PersonaRetriveCalendarEvents(context);
		retriveEvents.execute();
		
		// pushWidgetUpdate(context, remoteViews);
	}

	public static void pushWidgetUpdate(Context context, RemoteViews remoteViews) {
		ComponentName myWidget = new ComponentName(context,
				PersonaCalendarWidgetProvider.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		manager.updateAppWidget(myWidget, remoteViews);
	}

	/*
	 * public static PendingIntent buildButtonPendingIntent(Context context) {
	 * 
	 * 
	 * // initiate widget update request Intent intent = new Intent();
	 * intent.setAction("UpdateAction"); return
	 * PendingIntent.getBroadcast(context, 0, intent,
	 * PendingIntent.FLAG_UPDATE_CURRENT); }
	 */

	public static PendingIntent buildViewPendingIntent(Context context) {

		Intent intent = new Intent();
		intent.setAction("openConnectApplication");
		return PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
	}

}
