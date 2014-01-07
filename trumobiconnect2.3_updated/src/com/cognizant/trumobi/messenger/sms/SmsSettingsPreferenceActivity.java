package com.cognizant.trumobi.messenger.sms;

//import com.concentriclivers.mms.com.android.mms.ui.MessagingPreferenceActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.TruBoxSDK.SharedPreferences;
import com.cognizant.trumobi.PersonaLauncher;
import com.cognizant.trumobi.R;
import com.google.common.base.Strings;

public class SmsSettingsPreferenceActivity extends PreferenceActivity implements
		OnPreferenceChangeListener {

	public static final String SMS_DELIVERY_REPORT_MODE = "pref_key_sms_delivery_reports";
	public static final String NOTIFICATION_ENABLED = "pref_key_enable_notifications";
	public static final String NOTIFICATION_VIBRATE = "pref_key_vibrate";
	public static final String NOTIFICATION_VIBRATE_WHEN = "pref_key_vibrateWhen";
	public static final String NOTIFICATION_RINGTONE = "pref_key_ringtone";
	public static final String AUTO_DELETE = "pref_key_auto_delete";
	private Recycler mSmsRecycler;
	// private Preference mSmsLimitPref;
	private EditTextPreference mSmsLimitEdit;
	private CheckBoxPreference mSmsDeliveryReportPref;
	private CheckBoxPreference mSmsAutoDelete;
	private ListPreference mVibrateWhenPref;
	private CheckBoxPreference mEnableNotificationsPref;
	private CharSequence[] mVibrateEntries;
	private CharSequence[] mVibrateValues;
	private RingtonePreference mRingtone;
	private android.content.SharedPreferences mInsecurePrefs;
	private android.content.SharedPreferences mSecurePrefs;
	// SharedPreferences sp;
	public Context mContext;
	ImageView corporateIcon;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_main);
		setContentView(R.layout.sms_settings_header);

		addPreferencesFromResource(R.xml.sms_preferences);
		mInsecurePrefs = PreferenceManager.getDefaultSharedPreferences(this);
		mSecurePrefs = new SharedPreferences(this);
		mContext = this;
		loadPreferences();
		ImageView img = (ImageView) findViewById(R.id.icon);
		corporateIcon = (ImageView) findViewById(R.id.corporate_setting_icon);
		img.setImageDrawable(getResources().getDrawable(
				R.drawable.sms_messenger_app_icon));
		img.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();

			}
		});

		corporateIcon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(mContext, PersonaLauncher.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		});
		TextView title = (TextView) findViewById(R.id.app_name);
		title.setText("Settings");

		mSmsLimitEdit
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						// TODO Auto-generated method stub
						final Editor insecureEditor = mInsecurePrefs.edit();
						final Editor secureEditor = mSecurePrefs.edit();
						// mSmsLimitEdit.setSummary(newValue.toString());
						if (newValue.toString() != null) {
							insecureEditor
									.putString(
											getString(R.string.pref_key_sms_delete_limit),
											newValue.toString());
							insecureEditor.commit();
						}
						String insecure = mInsecurePrefs.getString(
								getString(R.string.pref_key_sms_delete_limit),
								null);
						System.out
								.println("Insecure Limit Value : " + insecure);
						secureEditor
								.putString(
										getString(R.string.pref_key_sms_delete_limit),
										mInsecurePrefs
												.getString(
														getString(R.string.pref_key_sms_delete_limit),
														null));
						insecureEditor
								.remove(getString(R.string.pref_key_sms_delete_limit));

						insecureEditor.commit();
						secureEditor.commit();
						if (newValue.toString().length() != 0)
							mSmsLimitEdit.setSummary(getString(
									R.string.pref_summary_delete_limit,
									newValue.toString()));
						else
							mSmsLimitEdit.setSummary(null);
						return true;
					}

				});

		mSmsAutoDelete
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						// TODO Auto-generated method stub
						final Editor insecureEditor = mInsecurePrefs.edit();
						final Editor secureEditor = mSecurePrefs.edit();
						secureEditor
								.putBoolean(
										getString(R.string.pref_key_auto_delete),
										mInsecurePrefs
												.getBoolean(
														getString(R.string.pref_key_auto_delete),
														false));
						insecureEditor
								.remove(getString(R.string.pref_key_auto_delete));
						insecureEditor.commit();
						secureEditor.commit();
						return true;
					}

				});

		mSmsDeliveryReportPref
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						// TODO Auto-generated method stub
						final Editor insecureEditor = mInsecurePrefs.edit();
						final Editor secureEditor = mSecurePrefs.edit();
						secureEditor
								.putBoolean(
										getString(R.string.pref_key_sms_delivery_reports),
										mInsecurePrefs
												.getBoolean(
														getString(R.string.pref_key_sms_delivery_reports),
														false));
						insecureEditor
								.remove(getString(R.string.pref_key_sms_delivery_reports));
						insecureEditor.commit();
						secureEditor.commit();
						return true;
					}

				});

	}

	@Override
	protected void onResume() {
		super.onResume();
		String limitString = mSecurePrefs.getString(
				getString(R.string.pref_key_sms_delete_limit), null);
		if (!Strings.isNullOrEmpty(limitString)) {
			mSmsLimitEdit.setText(limitString);
		}
	}

	private void loadPreferences() {
		// mSmsLimitPref = findPreference("pref_key_sms_delete_limit");
		mSmsLimitEdit = (EditTextPreference) findPreference("pref_key_sms_delete_limit");
		mSmsAutoDelete = (CheckBoxPreference) findPreference("pref_key_auto_delete");
		mSmsDeliveryReportPref = (CheckBoxPreference) findPreference("pref_key_sms_delivery_reports");

		mSmsRecycler = Recycler.getSmsRecycler();
		// Fix up the recycler's summary with the correct values
		// setSmsDisplayLimit();
	}

	private void setSmsDisplayLimit() {
		String limitString = mSecurePrefs.getString(
				getString(R.string.pref_key_sms_delete_limit), null);
		System.out.println("On Load Pref Value :" + limitString);
		if (!Strings.isNullOrEmpty(limitString)) {
			System.out.println("On IF Pref Value :" + limitString);
			mSmsLimitEdit.setSummary(getString(
					R.string.pref_summary_delete_limit, limitString));
		}

	}

	@Override
	public void onStart() {
		super.onStart();
		mSmsDeliveryReportPref.setChecked(mSecurePrefs.getBoolean(
				getString(R.string.pref_key_sms_delivery_reports), false));
		mSmsAutoDelete.setChecked(mSecurePrefs.getBoolean(
				getString(R.string.pref_key_auto_delete), false));
		mSmsLimitEdit.setSummary(getString(R.string.pref_summary_delete_limit,
				(mSecurePrefs.getString(
						getString(R.string.pref_key_sms_delete_limit), null))));
		// setSmsDisplayLimit();
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {

		/*
		 * if (preference == mSmsLimitPref) { new NumberPickerDialog(this,
		 * mSmsLimitListener, mSmsRecycler.getMessageLimit(this),
		 * mSmsRecycler.getMessageMinLimit(), mSmsRecycler.getMessageMaxLimit(),
		 * R.string.pref_title_sms_delete).show(); }
		 */
		return super.onPreferenceTreeClick(preferenceScreen, preference);

	}

	/*
	 * NumberPickerDialog.OnNumberSetListener mSmsLimitListener = new
	 * NumberPickerDialog.OnNumberSetListener() { public void onNumberSet(int
	 * limit) { mSmsRecycler.setMessageLimit(SettingsPreferenceActivity.this,
	 * limit); setSmsDisplayLimit(); } };
	 */

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		boolean result = false;
		if (preference == mVibrateWhenPref) {
			adjustVibrateSummary((String) newValue);
			result = true;
		}
		return result;
	}

	private void adjustVibrateSummary(String value) {
		int len = mVibrateValues.length;
		for (int i = 0; i < len; i++) {
			if (mVibrateValues[i].equals(value)) {
				mVibrateWhenPref.setSummary(mVibrateEntries[i]);
				return;
			}
		}
		mVibrateWhenPref.setSummary(null);
	}

	@Override
	public void onStop() {
		super.onStop();

		// Replace unencrypted key/value pairs with encrypted ones
		final Editor insecureEditor = mInsecurePrefs.edit();
		final Editor secureEditor = mSecurePrefs.edit();

		secureEditor.putBoolean(
				getString(R.string.pref_key_sms_delivery_reports),
				mInsecurePrefs.getBoolean(
						getString(R.string.pref_key_sms_delivery_reports),
						false));
		insecureEditor
				.remove(getString(R.string.pref_key_sms_delivery_reports));

		secureEditor.putBoolean(getString(R.string.pref_key_auto_delete),
				mInsecurePrefs.getBoolean(
						getString(R.string.pref_key_auto_delete), false));
		insecureEditor.remove(getString(R.string.pref_key_auto_delete));

		String limit = mSecurePrefs.getString(
				getString(R.string.pref_key_sms_delete_limit), null);
		System.out.println("Edit Limit Value :" + limit);
		/*
		 * secureEditor.putString(getString(R.string.pref_key_sms_delete_limit),
		 * mInsecurePrefs
		 * .getString(getString(R.string.pref_key_sms_delete_limit), null));
		 * insecureEditor.remove(getString(R.string.pref_key_sms_delete_limit));
		 */

		insecureEditor.commit();
		secureEditor.commit();
	}
}
