package com.cognizant.trumobi.persona;



import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cognizant.trumobi.log.PersonaLog;

public class PersonaCalendarWidgetReceiver extends BroadcastReceiver {

	ActivityManager mActivityManager;
	Context mContext;
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		PersonaLog.e("PersonaCalendarWidgetReceiver","onReceive");
		mActivityManager = (ActivityManager)context. getSystemService(Context.ACTIVITY_SERVICE);
		 if (intent.getAction().equals("openConnectApplication")) {
			 mContext = context;
			// Log.e("PersonaCalendarWidgetReceiver","PersonaCalendarWidgetReceiver received for expected action");
	          //  updateWidgetPictureAndButtonListener(context);
			 openConnectApp();
	        }
	}
	
	
	private void openConnectApp() {
		
		if(appIsInForeground()) {
			Intent appLaunchingIntent = new Intent();
			PersonaLog.d("PersonaCalendarWidgetReceiver","openConnectApp");
			appLaunchingIntent.setClassName(mContext, "com.cognizant.trumobi.calendar.view.CalendarMainActivity");
			appLaunchingIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			appLaunchingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(appLaunchingIntent);
		}
		
		else {
			Intent appLaunchingIntent = new Intent();
			PersonaLog.d("PersonaCalendarWidgetReceiver","openConnectApp");
			appLaunchingIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			appLaunchingIntent.setClassName(mContext, "com.cognizant.trumobi.persona.PersonaMainActivity");
			appLaunchingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(appLaunchingIntent);
		}
		
		
	}


	private boolean appIsInForeground() {
		
		String className = mActivityManager.getRunningTasks(1).get(0).topActivity
				.getClassName();
		if (className.equals("com.cognizant.trumobi.PersonaLauncher")) {
					return true;
				}
		
		return false;
	}


	/*private void updateWidgetPictureAndButtonListener(Context context) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.pr_calendar_widget_listitem);
 
        // updating view
        remoteViews.setTextViewText(R.id.pr_calendar_widget_listitem_eventtitle, "TrumobiMeeting");
        remoteViews.setTextViewText(R.id.pr_calendar_widget_listitem_eventvenue, "SDB3");
 
        // re-registering for click listener
        remoteViews.setOnClickPendingIntent(R.id.pr_calendar_widget_listitem_icon,
                PersonaCalendarWidgetProvider.buildButtonPendingIntent(context));
 
        PersonaCalendarWidgetProvider.pushWidgetUpdate(context.getApplicationContext(),
                remoteViews);
    }*/
 

}
