package com.cognizant.trumobi.calendar.event;

import java.util.Calendar;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.calendar.util.CalendarConstants;
import com.cognizant.trumobi.calendar.view.CalendarView;

public class CalendarNotifyService extends Service {

	/**
	 * Class for clients to access
	 */
	public class ServiceBinder extends Binder {
		CalendarNotifyService getService() {
			return CalendarNotifyService.this;
		}
	}

	// Unique id to identify the notification.
	private static final int NOTIFICATION = 123;
	// Name of an intent extra we can use to identify if this service was
	// started to create a notification
	public static final String INTENT_NOTIFY = "com.cognizant.trumobi.calendar.event.INTENT_NOTIFY";
	// The system notification manager
	private NotificationManager mNotificationManager;

	@Override
	public void onCreate() {
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		// If this service was started by out AlarmTask intent then we want to
		// show our notification
		if (intent.getBooleanExtra(INTENT_NOTIFY, false))
			showNotification(intent);

		// We don't care if this service is stopped as we have already delivered
		// our notification
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	// This is the object that receives interactions from clients
	private final IBinder mBinder = new ServiceBinder();

	/**
	 * Creates a notification and shows it in the OS drag-down status bar
	 */
	private void showNotification(Intent intent) {
		// This is the 'title' of the notification
		CharSequence title = CalendarConstants.NOTIFICATION_TITLE;
		// This is the icon to use on the notification
		int icon = R.drawable.pr_calendar_icon;
		// This is the scrolling text of the notification
		CharSequence text = CalendarConstants.NOTIFICATION_DESC;
		// What time to show on the notification
		long time = System.currentTimeMillis();

		Notification notification = new Notification(icon, text, time);

		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, CalendarView.class), 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(this, title, text, contentIntent);
		Calendar cal = Calendar.getInstance();
		// Clear the notification when it is pressed
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		if (intent.getIntExtra(CalendarConstants.YEAR, 0) == cal.get(Calendar.DAY_OF_MONTH)) {
			if (intent.getIntExtra(CalendarConstants.MONTH, 0) == cal.get(Calendar.MONDAY))
				mNotificationManager.notify(NOTIFICATION, notification);
		}
		if (intent.getIntExtra(CalendarConstants.MONTH, 0) == cal.get(Calendar.DAY_OF_MONTH)) {

			mNotificationManager.notify(NOTIFICATION, notification);
		}
		// Send the notification to the system.
		if (intent.getBooleanExtra(CalendarConstants.FLAG, false))
			mNotificationManager.notify(NOTIFICATION, notification);

		// Stop the service when we are finished
		stopSelf();
	}
}
