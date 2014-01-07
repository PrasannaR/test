

package com.cognizant.trumobi.em.service;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.em.EmExchangeUtils;
import com.cognizant.trumobi.em.EmPreferences;
import com.cognizant.trumobi.em.EmVendorPolicyLoader;
import com.cognizant.trumobi.em.provider.EmEmailContent.Account;
import com.cognizant.trumobi.log.EmailLog;
import com.cognizant.trumobi.persona.PersonaMainActivity;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.Uri;
import android.provider.Settings;
import android.util.Config;
import android.util.Log;

/**
 * The service that really handles broadcast intents on a worker thread.
 *
 * We make it a service, because:
 * <ul>
 *   <li>So that it's less likely for the process to get killed.
 *   <li>Even if it does, the Intent that have started it will be re-delivered by the system,
 *   and we can start the process again.  (Using {@link #setIntentRedelivery}).
 * </ul>
 */
public class EmEmailBroadcastProcessorService extends IntentService {
    private static final String TAG = null;
	private static final int NOTIFICATION_ID_SYNCSERVICE_NEEDED = 10;
	private NotificationManager mNotificationManager;
	

	public EmEmailBroadcastProcessorService() {
        // Class name will be the thread name.
        super(EmEmailBroadcastProcessorService.class.getName());

        // Intent should be redelivered if the process gets killed before completing the job.
        setIntentRedelivery(true);
    }

    /**
     * Entry point for {@link EmEmailBroadcastReceiver}.
     */
    public static void processBroadcastIntent(Context context, Intent broadcastIntent) {
        Intent i = new Intent(context, EmEmailBroadcastProcessorService.class);
        i.putExtra(Intent.EXTRA_INTENT, broadcastIntent);
        context.startService(i);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // This method is called on a worker thread.

        final Intent original = intent.getParcelableExtra(Intent.EXTRA_INTENT);
        final String action = original.getAction();
        mNotificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            onBootCompleted();

        // TODO: Do a better job when we get ACTION_DEVICE_STORAGE_LOW.
        //       The code below came from very old code....
        } else if (Intent.ACTION_DEVICE_STORAGE_LOW.equals(action)) {
            // Stop IMAP/POP3 poll.
            EmMailService.actionCancel(this);
        } else if (Intent.ACTION_DEVICE_STORAGE_OK.equals(action)) {
            enableComponentsIfNecessary();
        }
    }

    private void enableComponentsIfNecessary() {
        if (Email.setServicesEnabled(this)) {
            // At least one account exists.
            // TODO probably we should check if it's a POP/IMAP account.
            EmMailService.actionReschedule(this);
        }
    }

    /**
     * Handles {@link Intent#ACTION_BOOT_COMPLETED}.  Called on a worker thread.
     */
    private void onBootCompleted() {
        if (Config.LOGD) {
            Log.d(Email.LOG_TAG, "BOOT_COMPLETED");
        }
        performOneTimeInitialization();

        enableComponentsIfNecessary();
        
        EmailLog.d(TAG, "Show service notification .............................");
        Intent intent = new Intent(this, PersonaMainActivity.class);
        intent.putExtra("service_notification", "true");
        String title = this.getString(R.string.service_notification_content_title);
        this.showStartServiceNotification(this, null, title,
        		"Launch the SafeSpace to start its sync service", intent);

        
        EmailLog.d(TAG, "onBootCompleted " +
    			"starts the service for Exchange");

        // Starts the service for Exchange, if supported.
        EmExchangeUtils.startExchangeService(this);
    }

    private void performOneTimeInitialization() {
        final EmPreferences pref = EmPreferences.getPreferences(this);
        int progress = pref.getOneTimeInitializationProgress();
        final int initialProgress = progress;

        if (progress < 1) {
            Log.i(Email.LOG_TAG, "Onetime initialization: 1");
            progress = 1;
            if (EmVendorPolicyLoader.getInstance(this).useAlternateExchangeStrings()) {
                setComponentEnabled(EmEasAuthenticatorServiceAlternate.class, true);
                setComponentEnabled(EmEasAuthenticatorService.class, false);
            }

            EmExchangeUtils.enableEasCalendarSync(this);
        }

        // Add your initialization steps here.
        // Use "progress" to skip the initializations that's already done before.
        // Using this preference also makes it safe when a user skips an upgrade.  (i.e. upgrading
        // version N to version N+2)

        if (progress != initialProgress) {
            pref.setOneTimeInitializationProgress(progress);
            Log.i(Email.LOG_TAG, "Onetime initialization: completed.");
        }
    }

    private void setComponentEnabled(Class<?> clazz, boolean enabled) {
        final ComponentName c = new ComponentName(this, clazz.getName());
        getPackageManager().setComponentEnabledSetting(c,
                enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
    
    private  Notification createStartServiceNotificationBuilder(
            Context mContext, String ticker, CharSequence title, String contentText, Intent intent, Bitmap largeIcon,
            Integer number, boolean enableAudio, boolean ongoing) {

     // Pending Intent
	     PendingIntent pending = null;
	     if (intent != null) {
	            pending = PendingIntent.getActivity(mContext, 0, intent,
	                         PendingIntent.FLAG_UPDATE_CURRENT | Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR);
	
	     }

	     // NOTE: the ticker is not shown for notifications in the Holo UX
	     /*final Notification.Builder builder = new Notification.Builder(mContext)
	                  .setContentTitle(title).setContentText(contentText)
	                  .setContentIntent(pending).setLargeIcon(largeIcon)
	                  .setNumber(number == null ? 0 : number)
	                  .setSmallIcon(R.drawable.pr_app_icon).setWhen(System.currentTimeMillis())
	                  .setTicker(ticker).setOngoing(ongoing);*/
	     
     
	     Notification notification = new Notification(
	             	R.drawable.pr_app_icon,
	             	title,
	             System.currentTimeMillis());
     
	     notification.setLatestEventInfo(this,
	    		 title,
	    		 contentText,
	             pending);

	 	notification.flags = Notification.FLAG_ONGOING_EVENT + Notification.FLAG_NO_CLEAR;
            
	    String ringtoneString = Settings.System.DEFAULT_NOTIFICATION_URI.getPath(); 
	     
	    notification.sound = Uri.parse(ringtoneString);
	    
	    return notification;
	
	}
	public void showStartServiceNotification(Context context, String ticker,
	            String title, String contentText, Intent intent) {
	
		NotificationManager lNotificationManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = createStartServiceNotificationBuilder(context, ticker, title, contentText, intent, null, null, true,
	                  true);
		
	
		lNotificationManager.notify(NOTIFICATION_ID_SYNCSERVICE_NEEDED, notification);
	}
	
	
	public static void cancelServiceNotification(Context context) {
		NotificationManager lNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		lNotificationManager.cancel(NOTIFICATION_ID_SYNCSERVICE_NEEDED);
		
	}
}
