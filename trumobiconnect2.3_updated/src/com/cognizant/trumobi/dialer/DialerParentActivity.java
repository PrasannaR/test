package com.cognizant.trumobi.dialer;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.TruBoxSDK.SharedPreferences;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.cognizant.trubox.contacts.db.ContactsConsts;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseSherlockFragmentBaseActivity;
import com.cognizant.trumobi.contacts.activity.ContactsAddContact;
import com.cognizant.trumobi.dialer.adapter.DialerPagerAdapter;
import com.cognizant.trumobi.dialer.db.DialerCorporateTable;
import com.cognizant.trumobi.dialer.fragments.DialerCallLogFragment;
import com.cognizant.trumobi.dialer.utils.DialerUtilities;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.log.DialerLog;

public class DialerParentActivity extends
		TruMobiBaseSherlockFragmentBaseActivity {

	private ViewPager mPager;
	public static String dialNum = "";
	private int tabVal;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts_activity_main);
		Intent intent = getIntent();
		dialNum = intent.getStringExtra("number_edit");

		final ActionBar mActionBar = getSupportActionBar();
		mActionBar.setDisplayShowTitleEnabled(false);
		mActionBar.setDisplayShowHomeEnabled(false);
		// mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

		// Activate Fragment Manager
		FragmentManager fm = getSupportFragmentManager();
		ViewPager.SimpleOnPageChangeListener ViewPagerListener = new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				super.onPageSelected(position);
				// Find the ViewPager Position

				mActionBar.setSelectedNavigationItem(position);
				supportInvalidateOptionsMenu();

			}
		};

		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setOnPageChangeListener(ViewPagerListener);

		DialerPagerAdapter viewpageradapter = new DialerPagerAdapter(fm);
		mPager.setAdapter(viewpageradapter);
		mPager.setOffscreenPageLimit(3);

		ActionBar.TabListener tabListener = new ActionBar.TabListener() {
			@Override
			public void onTabSelected(Tab tab, FragmentTransaction ft) {
				tabVal = tab.getPosition();
				mPager.setCurrentItem(tabVal);
			}

			@Override
			public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			}

			@Override
			public void onTabReselected(Tab tab, FragmentTransaction ft) {
			}
		};
		Tab tab;

		// Create first Tab
		tab = mActionBar.newTab().setIcon(R.drawable.ic_action_call)
				.setTabListener(tabListener);
		mActionBar.addTab(tab);

		// Create second Tab
		tab = mActionBar.newTab()
				.setIcon(R.drawable.contacts_ic_ab_history_holo_dark)
				.setTabListener(tabListener);
		mActionBar.addTab(tab);

		// Create third Tab
		tab = mActionBar.newTab()
				.setIcon(R.drawable.contacts_ic_contacts_holo_dark)
				.setTabListener(tabListener);
		mActionBar.addTab(tab);

		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		if (savedInstanceState != null) {
			if (savedInstanceState.getInt("tab") != -1) {

				mPager.setCurrentItem(savedInstanceState.getInt("tab"));
			}
		}

		updateNativeCall();
		setSharedPrefrenceDialedNumber("DialedNumber", "");

	}

	private void updateNativeCall() {
		ContentValues values = new ContentValues();
		values.put(Calls.NEW, 0);
		StringBuilder where = new StringBuilder();
		where.append(Calls.NEW);
		where.append(" = 1 AND ");
		where.append(Calls.TYPE);
		where.append(" = ?");
		int res = getContentResolver().update(Calls.CONTENT_URI, values,
				where.toString(),
				new String[] { Integer.toString(Calls.MISSED_TYPE) });
		DialerLog.d("MissedCall Count", "DB Update result   ->  " + res);

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {

		/*
		 * outState.putInt("tab", getSupportActionBar()
		 * .getSelectedNavigationIndex());
		 */
		outState.putInt("tab", mPager.getCurrentItem());
		outState.putInt("pagerState", mPager.getId());
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		// to save the state of search widget
		if (savedInstanceState.getInt("tab") != -1) {
			getSupportActionBar().setSelectedNavigationItem(
					savedInstanceState.getInt("tab"));
		} else {

			getSupportActionBar().setNavigationMode(
					ActionBar.NAVIGATION_MODE_STANDARD);

		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.contacts_phone_options, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		int pageItem = mPager.getCurrentItem();
		if (pageItem == 0) {
			menu.findItem(R.id.menu_extractcallLog).setVisible(false);
			menu.findItem(R.id.menu_callLog).setVisible(false);
			menu.findItem(R.id.menu_settings).setVisible(false);
			menu.findItem(R.id.menu_showincomming).setVisible(false);
			menu.findItem(R.id.menu_showmissed).setVisible(false);
			menu.findItem(R.id.menu_showallCalls).setVisible(false);
			menu.findItem(R.id.menu_showoutgoing).setVisible(false);
			menu.findItem(R.id.menu_show_connect).setVisible(true);
			menu.findItem(R.id.menu_filter_calllog).setVisible(false);

		} else if (pageItem == 1) {
			menu.findItem(R.id.menu_dial).setVisible(false);
			menu.findItem(R.id.menu_new_contact).setVisible(false);
			menu.findItem(R.id.menu_extractcallLog).setVisible(false);
			menu.findItem(R.id.menu_callLog).setVisible(true);
			menu.findItem(R.id.menu_settings).setVisible(false);
			menu.findItem(R.id.menu_search).setVisible(false);
			menu.findItem(R.id.menu_showincomming).setVisible(true);
			menu.findItem(R.id.menu_showmissed).setVisible(true);
			menu.findItem(R.id.menu_showallCalls).setVisible(true);
			menu.findItem(R.id.menu_showoutgoing).setVisible(true);
			menu.findItem(R.id.menu_show_connect).setVisible(true);
			menu.findItem(R.id.menu_filter_calllog).setVisible(true);

		} else if (pageItem == 2) {
			menu.findItem(R.id.menu_dial).setVisible(false);
			menu.findItem(R.id.menu_new_contact).setVisible(true);
			menu.findItem(R.id.menu_extractcallLog).setVisible(false);
			menu.findItem(R.id.menu_callLog).setVisible(false);
			menu.findItem(R.id.menu_settings).setVisible(false);
			menu.findItem(R.id.menu_showincomming).setVisible(false);
			menu.findItem(R.id.menu_showmissed).setVisible(false);
			menu.findItem(R.id.menu_showallCalls).setVisible(false);
			menu.findItem(R.id.menu_showoutgoing).setVisible(false);
			menu.findItem(R.id.menu_show_connect).setVisible(true);
			menu.findItem(R.id.menu_filter_calllog).setVisible(false);

		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		setSharedPrefrenceDialedNumber("DialedNumber", dialNum);

		String dialedNumber = getSharedPrefrenceDialedNumber("DialedNumber");
		DialerLog.e("DialerParentActivity",
				"dialed Number in Option item ->   " + dialedNumber);

		switch (item.getItemId()) {

		case R.id.menu_show_connect:
			finish();
			break;

		case R.id.menu_dial:

			if (dialedNumber != null && !dialedNumber.isEmpty()) {
				if (DialerUtilities.checkSim()) {

					SharedPreferences.Editor prefEditor = new SharedPreferences(
							Email.getAppContext()).edit();
					prefEditor.putBoolean("isCallLogUpdated", true);
					prefEditor.commit();

					DialerUtilities.phoneCallIntent(dialedNumber, this);
				} else {
					Toast.makeText(this, "Insert SIM to make call",
							Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(this, "Can't dial without any number",
						Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.menu_new_contact:
			Intent intent3 = new Intent(DialerParentActivity.this,
					ContactsAddContact.class);
			if (dialedNumber != null && !dialedNumber.isEmpty()) {
				dialedNumber = dialedNumber.replaceAll("[^+a-zA-Z0-9]", "");
				intent3.putExtra("new_num", dialedNumber);

			}
			startActivity(intent3);
			break;

		case R.id.menu_callLog:

			int size = getCallLogSize();

			if (size > 0) {
				Fragment showIncommingFragment = (Fragment) mPager.getAdapter()
						.instantiateItem(mPager, mPager.getCurrentItem());
				((DialerCallLogFragment) showIncommingFragment)
						.showClearCallLog();
			}
			break;

		case R.id.menu_showincomming:
			ContactsConsts.callType = "1";
			Fragment showIncommingFragment = (Fragment) mPager.getAdapter()
					.instantiateItem(mPager, mPager.getCurrentItem());
			((DialerCallLogFragment) showIncommingFragment).uIRendering();
			break;

		case R.id.menu_showmissed:
			ContactsConsts.callType = "3";
			Fragment showMissedFragment = (Fragment) mPager.getAdapter()
					.instantiateItem(mPager, mPager.getCurrentItem());

			((DialerCallLogFragment) showMissedFragment).uIRendering();
			break;

		case R.id.menu_showoutgoing:
			ContactsConsts.callType = "2";
			Fragment showOutgoingFragment = (Fragment) mPager.getAdapter()
					.instantiateItem(mPager, mPager.getCurrentItem());

			((DialerCallLogFragment) showOutgoingFragment).uIRendering();
			break;

		case R.id.menu_showallCalls:
			ContactsConsts.callType = "";
			Fragment showAllCallsFragment = (Fragment) mPager.getAdapter()
					.instantiateItem(mPager, mPager.getCurrentItem());

			((DialerCallLogFragment) showAllCallsFragment).uIRendering();
			break;

		case R.id.menu_filter_calllog:
			Fragment showContactTypeFragment = (Fragment) mPager.getAdapter()
					.instantiateItem(mPager, mPager.getCurrentItem());

			((DialerCallLogFragment) showContactTypeFragment)
					.showCustomAlertDialog();
			break;

		case R.id.menu_search:
			Intent dialIntent = new Intent(DialerParentActivity.this,
					DialerSearchActivity.class);
			startActivity(dialIntent);
			break;

		case R.id.menu_extractcallLog:
			Intent showIntent = new Intent(DialerParentActivity.this,
					DialerShowDBActivity.class);
			startActivity(showIntent);
		default:
			break;
		}

		return true;
	}

	private int getCallLogSize() {
		Cursor cursor = getContentResolver().query(CallLog.Calls.CONTENT_URI,
				null, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);

		int size = cursor.getCount();
		if (cursor != null)
			cursor.close();

		return size;
	}

	// monitor phone call activities
	public class PhoneCallListener extends PhoneStateListener {

		private boolean isPhoneCalling = false;
		private boolean isPhoneRinging = false;
		public boolean frmParent = false;
		protected Context ctx;
		private int lastState = TelephonyManager.CALL_STATE_IDLE;

		public PhoneCallListener(Context ctx) {

			this.ctx = ctx;
		}

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {

			if (lastState == state) {
				return;
			}

			SharedPreferences.Editor prefEditor = new SharedPreferences(
					Email.getAppContext()).edit();
			prefEditor.putBoolean("isCallLogUpdated", true);
			prefEditor.commit();

			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE:

				if (isPhoneRinging) {

					isPhoneRinging = false;

				} else {

					if (isPhoneCalling) {

						try {

							// Restart App
							Intent i = getBaseContext().getPackageManager()
									.getLaunchIntentForPackage(
											getBaseContext().getPackageName());
							// i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
							startActivity(i);
						} catch (Exception e) {

						}

						isPhoneCalling = false;
					}
				}
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:

				isPhoneCalling = true;
				break;
			case TelephonyManager.CALL_STATE_RINGING:

				if (lastState == TelephonyManager.CALL_STATE_OFFHOOK) {

				}

				// checkDB(incomingNumber);
				isPhoneRinging = true;
				break;
			}
			lastState = state;
		}
	}

	public void checkDB(String incomingNumber) {
		if (incomingNumber != null && incomingNumber.length() > 10) {
			incomingNumber = incomingNumber
					.substring(incomingNumber.length() - 10);
		}
		// Original query
		Cursor selectedValueCursor = null;

		String where = ContactsConsts.CONTACT_PHONE + " LIKE ?";
		String[] args = new String[] { "%" + incomingNumber + "%" };

		try {
			selectedValueCursor = Email
					.getAppContext()
					.getContentResolver()
					.query(ContactsConsts.CONTENT_URI_CONTACTS, null, where,
							args, null);
			if (selectedValueCursor != null) {
				DialerLog.e("DialerParentActivity", "cursor count -> "
						+ selectedValueCursor.getCount() + "");
				selectedValueCursor.moveToFirst();
				String IncomingNumberRingtonePath = selectedValueCursor
						.getString(selectedValueCursor
								.getColumnIndex(ContactsConsts.CONTACT_RINGTONE_PATH));
				DialerLog.e("DialerParentActivity",
						"Incomming Ringtone Path ->  "
								+ IncomingNumberRingtonePath);

				customringtone(IncomingNumberRingtonePath);
			}
		} catch (Exception e) {
			DialerLog.e("DialerParentActivity", "Exception in Check DB ---> "
					+ e.getMessage());
			e.printStackTrace();
		} finally {
			if (selectedValueCursor != null) {
				DialerLog.e("DialerParentActivity", "finally called");
				selectedValueCursor.close();
			}
		}
	}

	public void customringtone(String IncomingNumberRingtonePath) {
		try {

			Uri ConvertedToUri = Uri.parse(IncomingNumberRingtonePath);
			MediaPlayer mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setDataSource(Email.getAppContext(), ConvertedToUri);
			final AudioManager audioManager = (AudioManager) Email
					.getAppContext().getSystemService(Context.AUDIO_SERVICE);
			if (audioManager.getStreamVolume(AudioManager.STREAM_RING) != 0) {
				mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
				mMediaPlayer.setLooping(true);
				mMediaPlayer.prepare();
				mMediaPlayer.start();
			}

		} catch (Exception e) {
			DialerLog.e("DialerParentActivity", "Exception in Custom Ringtone "
					+ e.getMessage());
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

	private String getSharedPrefrenceDialedNumber(String key) {
		return new SharedPreferences(Email.getAppContext()).getString(key, "");
	}

	private void setSharedPrefrenceDialedNumber(String key, String value) {
		SharedPreferences.Editor edit = new SharedPreferences(
				Email.getAppContext()).edit();
		edit.putString("DialedNumber", value);
		edit.commit();
	}

	/***
	 * 
	 * @author 361572 New DB Implementation
	 * 
	 */

	public static void insertCallLogDB() {
		String[] projection = new String[] { CallLog.Calls.NUMBER,
				CallLog.Calls.TYPE, CallLog.Calls.CACHED_NAME,
				CallLog.Calls.DATE, CallLog.Calls.CACHED_NUMBER_TYPE,
				CallLog.Calls.DURATION, CallLog.Calls._ID };

		Cursor cursor = Email
				.getAppContext()
				.getContentResolver()
				.query(CallLog.Calls.CONTENT_URI, projection, null, null,
						CallLog.Calls.DEFAULT_SORT_ORDER);

		if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {

			try {

				do {

					DialerCorporateTable corporateTable = new DialerCorporateTable();

					String lastCallnumber = cursor.getString(0);
					corporateTable.setPhoneNumber(lastCallnumber);

					String type = cursor.getString(1);
					corporateTable.setCallType(type);

					String name = cursor.getString(2);
					corporateTable.setAssociateName(name);

					String date = cursor.getString(3);
					corporateTable.setDate(date);

					String numberType = cursor.getString(4);
					corporateTable.setNumberType(numberType);

					String duration = cursor.getString(5);
					corporateTable.setCallDuration(duration);

					String _id = cursor.getString(6);
					corporateTable.setId(_id);

					addCorporateCallRecord(corporateTable);

				} while (cursor.moveToNext());
			} catch (Exception e) {

			}
			if (cursor != null)
				cursor.close();
		}
	}

	public static void addCorporateCallRecord(DialerCorporateTable corporateData)
			throws android.database.sqlite.SQLiteConstraintException {

		String where = DialerCorporateTable.ID + " =? ";

		Cursor cursor = Email
				.getAppContext()
				.getContentResolver()
				.query(DialerCorporateTable.CONTENT_URI_DIALER, null, where,
						new String[] { corporateData.getId() },
						CallLog.Calls.DEFAULT_SORT_ORDER);
		if (cursor != null && cursor.getCount() <= 0) {

			DialerCorporateTable corporateTable = getCorporateDetails(corporateData);

			ContentValues values = new ContentValues();
			values.put(DialerCorporateTable.ID, corporateTable.getId());
			values.put(DialerCorporateTable.ASSOICIATE_NAME,
					corporateTable.getAssociateName());
			values.put(DialerCorporateTable.PHONE_NUMBER,
					corporateTable.getPhoneNumber());
			values.put(DialerCorporateTable.CALL_TYPE,
					corporateTable.getCallType());
			values.put(DialerCorporateTable.NUMBER_TYPE,
					corporateTable.getNumberType());
			values.put(DialerCorporateTable.DATE, corporateTable.getDate());
			values.put(DialerCorporateTable.CALL_DURATION,
					corporateTable.getCallDuration());
			values.put(DialerCorporateTable.IS_CORPORATE,
					corporateTable.isCorporate());
			@SuppressWarnings("unused")
			Uri uri = Email.getAppContext().getContentResolver()
					.insert(DialerCorporateTable.CONTENT_URI_DIALER, values);

		}
		if (cursor != null)
			cursor.close();

	}

	public static DialerCorporateTable getCorporateDetails(
			DialerCorporateTable corporateTable) {
		boolean found = false;
		String lastCallNumber = corporateTable.getPhoneNumber();
		if (lastCallNumber != null && lastCallNumber.length() > 10) {
			lastCallNumber = lastCallNumber
					.substring(lastCallNumber.length() - 10);
		}

		Cursor cursor = null;

		String where = ContactsConsts.CONTACT_PHONE + " LIKE ?";
		String[] args = new String[] { "%" + lastCallNumber + "%" };

		cursor = Email
				.getAppContext()
				.getContentResolver()
				.query(ContactsConsts.CONTENT_URI_CONTACTS, null, where, args,
						null);

		if (cursor != null && cursor.moveToFirst()) {

			try {

				ArrayList<HashMap<String, String>> phoneVal;

				do {
					phoneVal = new ArrayList<HashMap<String, String>>();
					String phone_details = cursor.getString(cursor
							.getColumnIndex(ContactsConsts.CONTACT_PHONE));

					String[] splitParentStr = phone_details.split(":");

					for (int i = 0; i < splitParentStr.length; i++) {
						if (splitParentStr[i].contains("=")) {
							String[] phone_array = splitParentStr[i].split("=");
							HashMap<String, String> row = new HashMap<String, String>();
							row.put("phonetype" + i, phone_array[0]);
							row.put("phoneNumber" + i, phone_array[1]);
							phoneVal.add(row);
						}
					}
					String phoneNumber = null;
					String numberType = null;
					for (int i = 0; i < phoneVal.size(); i++) {

						phoneNumber = phoneVal.get(i).get("phoneNumber" + i);
						numberType = phoneVal.get(i).get("phonetype" + i);

						if (phoneNumber != null && phoneNumber.length() > 10) {
							phoneNumber = phoneNumber.substring(phoneNumber
									.length() - 10);
						}

						if (phoneNumber.equalsIgnoreCase(lastCallNumber)) {

							DialerLog.e("CorporateContact", "phone_details  "
									+ numberType);
							corporateTable.setNumberType(numberType);
							String name = cursor
									.getString(cursor
											.getColumnIndex(ContactsConsts.CONTACT_FIRST_NAME));

							corporateTable.setAssociateName(name);
							corporateTable.setCorporate("1");
							found = true;

							break;
						}

					}
					if (!found) {

						corporateTable.setAssociateName("");
						corporateTable.setCorporate("0");
					}

				} while (cursor.moveToNext());

			} catch (Exception e) {

				corporateTable.setAssociateName("");
				corporateTable.setCorporate("0");

			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}

		} else {
			corporateTable.setCorporate("0");
			if (cursor != null) {
				cursor.close();
			}
		}

		return corporateTable;
	}

}