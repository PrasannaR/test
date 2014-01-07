package com.cognizant.trumobi.dialer.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.TelephonyManager;
import android.widget.TextView;

@SuppressLint("NewApi")
public final class DialerPhoneNumberFormatter {
    private DialerPhoneNumberFormatter() {}

    /**
     * Load {@link TextWatcherLoadAsyncTask} in a worker thread and set it to a {@link TextView}.
     */
    private static class TextWatcherLoadAsyncTask extends
            AsyncTask<Void, Void, PhoneNumberFormattingTextWatcher> {
        private final String mCountryCode;
        private final TextView mTextView;

        public TextWatcherLoadAsyncTask(String countryCode, TextView textView) {
            mCountryCode = countryCode;
            mTextView = textView;
        }

       @Override
    protected PhoneNumberFormattingTextWatcher doInBackground(
    		Void... params) {
    	// TODO Auto-generated method stub
    	return new PhoneNumberFormattingTextWatcher();
    }

        @Override
        protected void onPostExecute(PhoneNumberFormattingTextWatcher watcher) {
            if (watcher == null || isCancelled()) {
                return; // May happen if we cancel the task.
            }
            // Setting a text changed listener is safe even after the view is detached.
            mTextView.addTextChangedListener(watcher);

            // Note changes the user made before onPostExecute() will not be formatted, but
            // once they type the next letter we format the entire text, so it's not a big deal.
            // (And loading PhoneNumberFormattingTextWatcher is usually fast enough.)
            // We could use watcher.afterTextChanged(mTextView.getEditableText()) to force format
            // the existing content here, but that could cause unwanted results.
            // (e.g. the contact editor thinks the user changed the content, and would save
            // when closed even when the user didn't make other changes.)
        }
    }

    /**
     * Delay-set {@link PhoneNumberFormattingTextWatcher} to a {@link TextView}.
     */
    public static final void setPhoneNumberFormattingTextWatcher(Context context,
            TextView textView) {
        new TextWatcherLoadAsyncTask(getCurrentCountryIso(context), textView)
                .execute();
    }
    
    public static final String getCurrentCountryIso(Context context) {
    	 TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
         String countryCode = tm.getNetworkCountryIso();

        return countryCode;
    }
}