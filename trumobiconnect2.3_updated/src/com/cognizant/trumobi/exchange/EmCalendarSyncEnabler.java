

package com.cognizant.trumobi.exchange;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.calendar.util.CalendarConstants;
import com.cognizant.trumobi.common.provider.Calendar;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.em.service.EmMailService;

/**
 * Utility class to enable Exchange calendar sync for all existing Exchange accounts.
 *
 * <p>Exchange calendar was first supported on Froyo.  It wasn't supported on Eclair, which
 * was the first version that supported Exchange email.
 *
 * <p>This class is used only once when the devices is upgraded to Froyo (or later) from Eclair,
 * to enable calendar sync for all the existing Exchange accounts.
 */
public class EmCalendarSyncEnabler {
    private final Context mContext;

    public EmCalendarSyncEnabler(Context context) {
        this.mContext = context;
    }

    /**
     * Enable calendar sync for all the existing exchange accounts, and post a notification if any.
     */
    public final void enableEasCalendarSync() {
        String emailAddresses = enableEasCalendarSyncInternal();
        if (emailAddresses.length() > 0) {
            // Exchange account(s) found.
            showNotification(emailAddresses.toString());
        }
    }

    /**
     * Enable calendar sync for all the existing exchange accounts
     *
     * @return email addresses of the Exchange accounts joined with spaces as delimiters,
     *     or the empty string if there's no Exchange accounts.
     */
    /* package for testing */ final String enableEasCalendarSyncInternal() {
        StringBuilder emailAddresses = new StringBuilder();

        Account[] exchangeAccounts = AccountManager.get(mContext)
                .getAccountsByType(Email.EXCHANGE_ACCOUNT_MANAGER_TYPE);
        for (Account account : exchangeAccounts) {
            final String emailAddress = account.name;
            Log.i(Email.LOG_TAG, "Enabling Exchange calendar sync for " + emailAddress);

            ContentResolver.setIsSyncable(account, CalendarConstants.AUTHORITY, 1);
            ContentResolver.setSyncAutomatically(account, CalendarConstants.AUTHORITY, true);

            // Accumulate addresses for notification.
            if (emailAddresses.length() > 0) {
                emailAddresses.append(' ');
            }
            emailAddresses.append(emailAddress);
        }
        return emailAddresses.toString();
    }

    /**
     * Show the "Exchange calendar added" notification.
     *
     * @param emailAddresses space delimited list of email addresses of Exchange accounts.  It'll
     *     be shown on the notification.
     */
    /* package for testing */ void showNotification(String emailAddresses) {
        // Launch Calendar app when clicked.
        PendingIntent launchCalendarPendingIntent = PendingIntent.getActivity(mContext, 0,
                createLaunchCalendarIntent(), 0);

        String tickerText = mContext.getString(R.string.notification_exchange_calendar_added);
        Notification n = new Notification(R.drawable.em_stat_notify_calendar,
                tickerText, System.currentTimeMillis());
        n.setLatestEventInfo(mContext, tickerText, emailAddresses, launchCalendarPendingIntent);
        n.flags = Notification.FLAG_AUTO_CANCEL;

        NotificationManager nm =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(EmMailService.NOTIFICATION_ID_EXCHANGE_CALENDAR_ADDED, n);
    }

    /** @return {@link Intent} to launch the Calendar app. */
    private Intent createLaunchCalendarIntent() {
        return new Intent(Intent.ACTION_VIEW, Uri.parse("content://com.android.calendar/time"));
    }
}