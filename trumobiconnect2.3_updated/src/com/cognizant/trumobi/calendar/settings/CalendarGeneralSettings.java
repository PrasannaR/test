package com.cognizant.trumobi.calendar.settings;

import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.RingtonePreference;
import android.widget.Toast;

import com.TruBoxSDK.SharedPreferences;
import com.TruBoxSDK.SharedPreferences.Editor;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.calendar.provider.CalendarDatabaseHelper;
import com.cognizant.trumobi.calendar.util.CalendarCommonFunction;
import com.cognizant.trumobi.calendar.util.CalendarConstants;
import com.cognizant.trumobi.calendar.util.CalendarUtility;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseSherlockPreferenceActivity;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.log.CalendarLog;

public class CalendarGeneralSettings extends TruMobiBaseSherlockPreferenceActivity {

	private ListPreference mSplashList, mHome_time_zoneList,mDefault_reminder_time,mDaystoSyncInterval;
	private CheckBoxPreference mDisplay_personal_events, //mHide_declined_event,
			mShow_week_number, mVibrate, mPop_up_notification, mUse_home_time,
			mParent_notification;
	private Preference mClear_history_preference;
	private RingtonePreference mSound;
	//private android.content.SharedPreferences mSecurePrefs;
	SharedPreferences mSecurePrefs;
	private Editor mSecureEditor;
	private android.content.SharedPreferences.Editor mInsecureEditor ;
	 private android.content.SharedPreferences mInsecurePrefs;
	// private boolean use_home_time_flag;
	@SuppressWarnings("deprecation")
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setIcon(R.drawable.pr_calendar_icon);
		getSupportActionBar().setTitle(CalendarConstants.SETTINGS_PREF_NAME);

		addPreferencesFromResource(R.xml.calendar_preferences);
		
		init();
		getValue();

	}

	/**
	 * Used to initialise all values
	 */
	@SuppressWarnings("deprecation")
	private void init() {
		mSecurePrefs = new SharedPreferences(Email.getAppContext());
		mSecureEditor = mSecurePrefs.edit();
		mInsecurePrefs = PreferenceManager.getDefaultSharedPreferences(this);
		mInsecureEditor = mInsecurePrefs.edit();
		/*mSecurePrefs = getSharedPreferences(
				CalendarConstants.SETTINGS_PREF_NAME, MODE_PRIVATE);
		mSecureEditor = mSecurePrefs.edit();*/
		mSplashList = (ListPreference) findPreference(getString(R.string.key_list_preference));
		
		mHome_time_zoneList = (ListPreference) findPreference(getString(R.string.key_home_time_zone_preference));
		mHome_time_zoneList.setSummary(mSecurePrefs.getString(getString(R.string.key_home_time_zone_preference), ""));
		
		mDisplay_personal_events = (CheckBoxPreference) findPreference(getString(R.string.key_display_personal_events_checkbox_preference));
		mDisplay_personal_events.setChecked(mSecurePrefs.getBoolean(getString(R.string.key_display_personal_events_checkbox_preference), false));
		
//		mHide_declined_event = (CheckBoxPreference) findPreference(getString(R.string.key_hide_event_checkbox_preference));
//		mHide_declined_event.setChecked(mSecurePrefs.getBoolean(getString(R.string.key_hide_event_checkbox_preference), false));
		
		mUse_home_time = (CheckBoxPreference) findPreference(getString(R.string.key_parent_calender));
		mUse_home_time.setChecked(mSecurePrefs.getBoolean(getString(R.string.key_parent_calender), false));
		
		mShow_week_number = (CheckBoxPreference) findPreference(getString(R.string.key_show_week_checkbox_preference));
		mShow_week_number.setChecked(mSecurePrefs.getBoolean(getString(R.string.key_show_week_checkbox_preference),false));
	
		mClear_history_preference = (Preference) findPreference(getString(R.string.key_clear_search));
		
		mParent_notification = (CheckBoxPreference) findPreference(getString(R.string.key_parent_notifications));
		mParent_notification.setChecked(mSecurePrefs.getBoolean(getString(R.string.key_parent_notifications), true));
		
		
				
		mVibrate = (CheckBoxPreference) findPreference(getString(R.string.key_child_vibrate_notification));
		
		mPop_up_notification = (CheckBoxPreference) findPreference(getString(R.string.key_child_popup_notification));
		mPop_up_notification.setChecked(mSecurePrefs.getBoolean(getString(R.string.key_child_popup_notification), false));
		
		mDefault_reminder_time = (ListPreference) findPreference(getString(R.string.key_default_reminderlist_preference));
		//mDefault_reminder_time.setSummary(mSecurePrefs.getString(getString(R.string.key_default_reminderlist_preference), ""));
		
		mDaystoSyncInterval=(ListPreference)findPreference(getString(R.string.cal_sync_key_list));
		mDaystoSyncInterval.setSummary(CalendarUtility.getIntervalString(mSecurePrefs.getString(getString(R.string.cal_sync_key_list), "Two Weeks")));
		
		mSound = (RingtonePreference) findPreference(getString(R.string.key_ringtone_preferences));
		
		String ringtoneUri = mSecurePrefs.getString(getString(R.string.key_ringtone_preferences), "");
		if(ringtoneUri.equals(""))
		{
			Uri uri = Uri.parse(RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).toString());
			Ringtone ringtone = RingtoneManager.getRingtone(this, uri);
			ringtoneUri = ringtone.getTitle(this);
		}
		//Ringtone ringtone = RingtoneManager.getRingtone(this,Uri.parse(ringtoneUri));
		//String name = ringtone.getTitle(this);
		mSound.setSummary(ringtoneUri);

	}

	/**
	 * Used to get value from each preference
	 */
	private void getValue() {
		
		// get the value of week start day
		mSplashList.setSummary(mSplashList.getEntry());
		mSplashList
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						String nv = (String) newValue;

						if (preference.getKey().equals(
								getString(R.string.key_list_preference))) {
							mSplashList = (ListPreference) preference;
							mSplashList.setSummary(mSplashList.getEntries()[mSplashList
									.findIndexOfValue(nv)]);
						}
						return true;
					}

				});

		// Check Whether use home time check box is enabled
		//checkUseHomeZoneIsEnabled();

		mHome_time_zoneList
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						String selectedZone = (String) newValue;
						if (preference
								.getKey()
								.equals(getString(R.string.key_home_time_zone_preference))) {
							mHome_time_zoneList = (ListPreference) preference;
							if (mHome_time_zoneList.getEntries()[mHome_time_zoneList
									.findIndexOfValue(selectedZone)]
									.equals(CalendarConstants.STRING_DEFAULT)) {
								mHome_time_zoneList.setSummary(CalendarCommonFunction
										.getDeviceCurrentTimezoneOffset());
							} else {
								mHome_time_zoneList.setSummary(mHome_time_zoneList
										.getEntries()[mHome_time_zoneList
										.findIndexOfValue(selectedZone)]);

							}
							String finalStrZone = mHome_time_zoneList
									.getSummary().toString();
							/*mInsecureEditor.putString(
									getString(R.string.key_home_time_zone_preference),
									finalStrZone);
							mInsecureEditor.commit();*/
							
							mSecureEditor.putString(
									getString(R.string.key_home_time_zone_preference),
									finalStrZone);
							mInsecureEditor.remove(getString(R.string.key_home_time_zone_preference));
							mInsecureEditor.commit();
							mSecureEditor.commit();
							
							int endIndex = mHome_time_zoneList.getSummary()
									.toString().indexOf(")");
							mSecureEditor.putString(
									CalendarConstants.PREF_NEW_CALENDAR_ZONE,
									finalStrZone.substring(1, endIndex));
							mSecureEditor.commit();
							Email.setNewZone(finalStrZone.substring(1,
									endIndex));
						}
						return true;
					}
				});
		
		// get value for clear history

		mClear_history_preference
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {

						CalendarDatabaseHelper.deleteSearchHistory();
						Toast.makeText(getApplicationContext(), CalendarConstants.SEARCH_HISTORY_CLEARED, Toast.LENGTH_SHORT).show();
						return true;
					}
				});

		
		mDefault_reminder_time.setSummary(mDefault_reminder_time.getEntry());
		// Default Reminder Click listener
		mDefault_reminder_time
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						if (preference
								.getKey()
								.equals(getString(R.string.key_default_reminderlist_preference))) {
							mDefault_reminder_time = (ListPreference) preference;
							mDefault_reminder_time.setSummary(mDefault_reminder_time
									.getEntries()[mDefault_reminder_time
									.findIndexOfValue((String) newValue)]);
							
							CalendarLog.d(CalendarConstants.Tag, "Default Minutes sett"+ newValue);
							String finalStrZone = mDefault_reminder_time
									.getSummary().toString();	
							/*final String[] reminderminutes = getResources().getStringArray(
									R.array.reminder_minutes_values);
							finalStrZone= reminderminutes[mDefault_reminder_time
									.findIndexOfValue((String) newValue)];*/
							/*mInsecureEditor.putString(getString(R.string.key_default_reminderlist_preference),finalStrZone);
							mInsecureEditor.commit();*/
							
							mSecureEditor.putString(
									getString(R.string.key_default_reminderlist_preference),
									finalStrZone);
							mInsecureEditor.remove(getString(R.string.key_default_reminderlist_preference));
							mInsecureEditor.commit();
							mSecureEditor.commit();
						}
						return true;
					}
				});
		
		mDaystoSyncInterval.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				String selectedZone = (String) newValue;
				if (preference
						.getKey()
						.equals(getString(R.string.cal_sync_key_list))) {
					mDaystoSyncInterval = (ListPreference) preference;			
					mDaystoSyncInterval.setSummary(mDaystoSyncInterval
								.getEntries()[mDaystoSyncInterval
								.findIndexOfValue(selectedZone)]);

//					String finalSynInterval = mDaystoSyncInterval.getSummary().toString();
					/*mInsecureEditor.putString(getString(R.string.cal_sync_key_list), finalSynInterval);
					mInsecureEditor.commit();*/
					
					mSecureEditor.putString(getString(R.string.cal_sync_key_list), selectedZone);
					mInsecureEditor.remove(getString(R.string.cal_sync_key_list));
					mInsecureEditor.commit();
					mSecureEditor.commit();
					CalendarDatabaseHelper.manualSync();
					
				}
				return true;
			}
		});
		
		mSound.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				Uri ringtoneUri = Uri.parse(newValue.toString());
				Ringtone ringtone = RingtoneManager.getRingtone(
						getBaseContext(), ringtoneUri);
				String name = ringtone.getTitle(getBaseContext());
				mSound.setSummary(name);

				/*mInsecureEditor.putString(
						getString(R.string.key_ringtone_preferences), name);
				mInsecureEditor.commit();*/
				mSecureEditor.putString(
						getString(R.string.key_ringtone_preferences), name);
				mInsecureEditor
						.remove(getString(R.string.key_ringtone_preferences));
				mInsecureEditor.commit();
				mSecureEditor.commit();
				mSecureEditor.putString(CalendarConstants.RINGTONE_URI, ringtoneUri.toString());
				mSecureEditor.commit();

//				Toast.makeText(getApplicationContext(), name, 5).show();
				// MediaPlayer player =
				// MediaPlayer.create(MainPreferenceActivity.this,
				// ringtoneUri);
				// player.start();
				return true;
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case android.R.id.home:
			savePreference();
			// CalendarCommonFunction.inFromLeftAnimation();
			finish();
			overridePendingTransition(R.anim.cal_slide_in_right,
					R.anim.cal_slide_in_left);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void savePreference(){
		mSecureEditor.putBoolean(getString(R.string.key_parent_calender), mInsecurePrefs.getBoolean(getString(R.string.key_parent_calender), false));
		mInsecureEditor.remove(getString(R.string.key_parent_calender));
		mInsecureEditor.commit();
		mSecureEditor.commit();
		
		/*if (mUse_home_time.isChecked()) {
			mSecureEditor.putBoolean(
					CalendarConstants.PREF_USE_HOME_TIME_ZONE_RESULT, true);
		}

		else {
			mSecureEditor.putBoolean(
					CalendarConstants.PREF_USE_HOME_TIME_ZONE_RESULT, false);
		}
		mSecureEditor.commit();*/
		
		checkHideDeclinedEvent();
		checkDisplaypersonalEvent();
		// CalendarDatabaseHelper.isDisplayPersonalEvent();
	//	checkUseHomeZoneIsEnabled();
		checkWeeknumberIsEnabled();
		checkNotificationEnabled();
		checkPopUpEnabled();
	}

	private void checkHideDeclinedEvent() {

		mSecureEditor.putBoolean(getString(R.string.key_hide_event_checkbox_preference), mInsecurePrefs.getBoolean(getString(R.string.key_hide_event_checkbox_preference), false));
		mInsecureEditor.remove(getString(R.string.key_hide_event_checkbox_preference));
		mInsecureEditor.commit();
		mSecureEditor.commit();
	}

	private void checkPopUpEnabled() {
		
		mSecureEditor.putBoolean(getString(R.string.key_child_popup_notification), mInsecurePrefs.getBoolean(getString(R.string.key_child_popup_notification), false));
		mInsecureEditor.remove(getString(R.string.key_child_popup_notification));
		mInsecureEditor.commit();
		mSecureEditor.commit();
		
		/*if(mPop_up_notification.isChecked())
		{
			mSecureEditor.putBoolean(
					CalendarConstants.POP_UP_REMINDER, true);
		}
		else
		{
			mSecureEditor.putBoolean(
					CalendarConstants.POP_UP_REMINDER, false);
		}
		
		mSecureEditor.commit();*/
	}

	private void checkNotificationEnabled() {

		mSecureEditor.putBoolean(getString(R.string.key_parent_notifications), mInsecurePrefs.getBoolean(getString(R.string.key_parent_notifications), false));
		mInsecureEditor.remove(getString(R.string.key_parent_notifications));
		mInsecureEditor.commit();
		mSecureEditor.commit();
		
		/*if(mParent_notification.isChecked())
		{
			mSecureEditor.putBoolean(
					CalendarConstants.NOTIFICATION_REMINDER, true);
		}
		else
		{
			mSecureEditor.putBoolean(
					CalendarConstants.NOTIFICATION_REMINDER, false);
		}
		mSecureEditor.commit();*/
	}

	private void checkWeeknumberIsEnabled() {		
		
		mSecureEditor.putBoolean(getString(R.string.key_show_week_checkbox_preference),mInsecurePrefs.getBoolean(getString(R.string.key_show_week_checkbox_preference), false));
		mInsecureEditor.remove(getString(R.string.key_show_week_checkbox_preference));
		mInsecureEditor.commit();
		mSecureEditor.commit();
		/*if(mShow_week_number.isChecked())
		{
			mSecureEditor.putBoolean(getString(R.string.key_show_week_checkbox_preference), true);
		}
		else
		{
			mSecureEditor.putBoolean(getString(R.string.key_show_week_checkbox_preference), false);
		}
		mSecureEditor.commit();*/
	}

	private void checkDisplaypersonalEvent() {
		
		mSecureEditor.putBoolean(getString(R.string.key_display_personal_events_checkbox_preference),mInsecurePrefs.getBoolean(getString(R.string.key_display_personal_events_checkbox_preference), false));
		mInsecureEditor.remove(getString(R.string.key_display_personal_events_checkbox_preference));
		mInsecureEditor.commit();
		mSecureEditor.commit();
		/*if (mDisplay_personal_events.isChecked()) {
			mSecureEditor.putBoolean(CalendarConstants.DISPLAY_PERSONAL_EVENTS, true);
		}

		else {
			mSecureEditor.putBoolean(CalendarConstants.DISPLAY_PERSONAL_EVENTS, false);
		}

		mSecureEditor.commit();*/

	}

/*	public void checkUseHomeZoneIsEnabled() {
		if (!mSecurePrefs.getBoolean(
				CalendarConstants.PREF_USE_HOME_TIME_ZONE_RESULT, false)) {
			mHome_time_zoneList.setSummary(mSecurePrefs.getString(
					getString(R.string.key_home_time_zone_preference),
					CalendarCommonFunction.getDeviceCurrentTimezoneOffset()));
			String finalStrZone = CalendarCommonFunction
					.getDeviceCurrentTimezoneOffset();
			int endIndex = finalStrZone.indexOf(")");
			CognizantEmail.setNewZone(finalStrZone.substring(1, endIndex));
		} else {
			mHome_time_zoneList.setSummary(mSecurePrefs.getString(
					getString(R.string.key_home_time_zone_preference),
					CalendarCommonFunction.getDeviceCurrentTimezoneOffset()));
			String finalStrZone = mHome_time_zoneList.getSummary().toString();
			int endIndex = finalStrZone.indexOf(")");
			CognizantEmail.setNewZone(finalStrZone.substring(1, endIndex));
		}
	}*/

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		savePreference();
		overridePendingTransition(R.anim.cal_slide_in_right,
				R.anim.cal_slide_in_left);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		savePreference();
		super.onConfigurationChanged(newConfig);
	}
}
