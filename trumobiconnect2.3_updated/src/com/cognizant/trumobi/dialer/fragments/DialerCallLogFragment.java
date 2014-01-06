package com.cognizant.trumobi.dialer.fragments;

import java.util.ArrayList;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.TruBoxSDK.SharedPreferences;
import com.actionbarsherlock.app.SherlockListFragment;
import com.cognizant.trubox.contacts.db.ContactsConsts;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.dialer.DialerCallLogHistoryDetail;
import com.cognizant.trumobi.dialer.adapter.DialerCallogAdapter;
import com.cognizant.trumobi.dialer.dbController.DialerCallLogList;
import com.cognizant.trumobi.dialer.utils.DialerUtilities;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.log.DialerLog;

public class DialerCallLogFragment extends SherlockListFragment {

	private TextView textEmptyCallLog, textCallType;
	private ListView lstCalLog;
	private LinearLayout callTypeLayout;
	private ArrayList<DialerCallLogList> mCallLogArrayList;
	private DialerCallogAdapter mcalAdap;
	private CallLogAsyncTask callLogAsyncTask;
	private String TAG = "DialerCallLogFragment";
	private ArrayList<DialerCallLogList> newCallLogList;
	private int mCurrentSelectedItemIndex = 0;
	private final CharSequence[] items = { "Show all", "Show Corporate",
			"Show Personal" };

	private static String contactType = "Show all";

	public static String getContactType() {
		return contactType;
	}

	public static void setContactType(String contactType) {
		DialerCallLogFragment.contactType = contactType;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.dial_call_log, container, false);
		textEmptyCallLog = (TextView) v.findViewById(R.id.empty_call_log);
		textCallType = (TextView) v.findViewById(R.id.call_title);
		lstCalLog = (ListView) v.findViewById(android.R.id.list);
		callTypeLayout = (LinearLayout) v.findViewById(R.id.call_type_header);
		lstCalLog.setCacheColorHint(Color.WHITE);
		lstCalLog.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		// uIRendering();
		v.setFilterTouchesWhenObscured(true);
		return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int selection = getSharedPrefrenceSelectOption("SelectedCallType");
		setContactType(items[selection].toString());
		uIRendering();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		setRetainInstance(true);
		if (mcalAdap != null) {

			if (newCallLogList != null && newCallLogList.size() > 0) {
				textEmptyCallLog.setVisibility(View.GONE);
				lstCalLog.setVisibility(View.VISIBLE);
				callTypeLayout.setVisibility(View.VISIBLE);
				textCallType.setText(getHeaderTextData());
				lstCalLog.setAdapter(mcalAdap);
				mcalAdap.notifyDataSetChanged();
			} else {
				// If list got cleared after data update
				System.out.println("List is empty...");
				textEmptyCallLog.setVisibility(View.VISIBLE);
				lstCalLog.setVisibility(View.GONE);
				callTypeLayout.setVisibility(View.GONE);
				textEmptyCallLog.setText(getHeaderTextNoData());
			}

		} else {
			// If adapter got cleared after data update
			System.out.println("Adapter is empty....");
			textEmptyCallLog.setVisibility(View.VISIBLE);
			lstCalLog.setVisibility(View.GONE);
			callTypeLayout.setVisibility(View.GONE);
			textEmptyCallLog.setText(getHeaderTextNoData());
		}
	}

	public void uIRendering() {

		if (callLogAsyncTask != null) {
			callLogAsyncTask.cancel(true);
		}

		callLogAsyncTask = new CallLogAsyncTask();
		callLogAsyncTask.execute();

	}

	public void showCustomAlertDialog() {

		FragmentManager manager = getSherlockActivity()
				.getSupportFragmentManager();
		splitInfoDialog splitDialog = new splitInfoDialog();
		Bundle b = new Bundle();
		b.putInt("SelectedCallType",
				getSharedPrefrenceSelectOption("SelectedCallType"));
		splitDialog.setArguments(b);
		splitDialog.show(manager, TAG);

	}

	private void setSharedPrefrenceSelectOption(String key, int selection) {
		SharedPreferences.Editor edit = new SharedPreferences(
				Email.getAppContext()).edit();
		edit.putInt(key, selection);
		edit.commit();

	}

	private int getSharedPrefrenceSelectOption(String key) {
		return new SharedPreferences(Email.getAppContext()).getInt(key, 0);
	}

	private void getCallLogListBasedOnContactType(String item,
			ArrayList<DialerCallLogList> mCallLogArray) {
		ArrayList<DialerCallLogList> callLogList = null;
		setContactType(item);

		if (item.equalsIgnoreCase("Show all")) {
			if (mCallLogArray != null && mCallLogArray.size() > 0) {

				callLogList = mCallLogArray;
			}

		} else if (item.equalsIgnoreCase("Show Corporate")) {

			if (mCallLogArray != null && mCallLogArray.size() > 0) {
				ArrayList<DialerCallLogList> callLogCorporate = new ArrayList<DialerCallLogList>();
				for (int i = 0; i < mCallLogArray.size(); i++) {
					DialerCallLogList callLogItem = mCallLogArray.get(i);
					if (!callLogItem.isNativeContact())
						callLogCorporate.add(callLogItem);
				}

				callLogList = callLogCorporate;
			}

		} else if (item.equalsIgnoreCase("Show Personal")) {

			if (mCallLogArray != null && mCallLogArray.size() > 0) {
				ArrayList<DialerCallLogList> callLogPersonal = new ArrayList<DialerCallLogList>();
				for (int i = 0; i < mCallLogArray.size(); i++) {
					DialerCallLogList callLogItem = mCallLogArray.get(i);
					if (callLogItem.isNativeContact())
						callLogPersonal.add(callLogItem);
				}

				callLogList = callLogPersonal;
			}

		}

		Message msg = new Message();
		msg.obj = callLogList;
		handler.sendMessage(msg);
	}

	public void showClearCallLog() {

		FragmentManager manager = getSherlockActivity()
				.getSupportFragmentManager();
		ClearCallLogDialog dialog = new ClearCallLogDialog();
		dialog.show(manager, "dialog");

	}

	public class ClearCallLogDialog extends DialogFragment {

		public ClearCallLogDialog() {
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			setRetainInstance(true);
			super.onCreate(savedInstanceState);
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {

			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					getSherlockActivity());

			// set dialog message
			alertDialogBuilder
					.setMessage("Do you want to clear Call Log?")
					.setCancelable(false)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {

									clearCallLog();
									dialog.cancel();
								}
							}).setNegativeButton("Cancel", null);

			return alertDialogBuilder.create();
		}

		@Override
		public void onDestroyView() {
			if (getDialog() != null && getRetainInstance()) {
				getDialog().setOnDismissListener(null);
			}
			super.onDestroyView();
		}
	}

	private void clearCallLog() {
		getSherlockActivity().getContentResolver().delete(
				CallLog.Calls.CONTENT_URI, null, null);
		ContactsConsts.callType = "";
		uIRendering();
	}

	@Override
	public void onResume() {

		boolean isListRefresh = new SharedPreferences(Email.getAppContext())
				.getBoolean("isCallLogUpdated", false);

		if (isListRefresh) {

			SharedPreferences.Editor prefEditor = new SharedPreferences(
					Email.getAppContext()).edit();
			prefEditor.putBoolean("isCallLogUpdated", false);
			prefEditor.commit();
			uIRendering();
		}
		super.onResume();
	}

	@Override
	public void onPause() {

		if (callLogAsyncTask != null) {
			callLogAsyncTask.cancel(true);
		}
		super.onPause();
	}

	public ArrayList<DialerCallLogList> frmDeviceLog() {

		String[] projection = new String[] { CallLog.Calls.NUMBER,
				CallLog.Calls.TYPE, CallLog.Calls.CACHED_NAME,
				CallLog.Calls.DATE, CallLog.Calls.CACHED_NUMBER_TYPE,
				CallLog.Calls._ID };
		String where = null;
		String[] args = null;

		if (ContactsConsts.callType.equalsIgnoreCase("")) {
			where = null;
			args = null;
		} else {
			where = CallLog.Calls.TYPE + " =? ";
			args = new String[] { ContactsConsts.callType };
		}

		String lastNo = "";
		ArrayList<String> arr_Type = null;
		ArrayList<String> arr_Date = null;
		int count = 0;
		ArrayList<DialerCallLogList> mMsgItemList = new ArrayList<DialerCallLogList>();

		Cursor c;
		/*
		 * c = getSherlockActivity().getContentResolver().query(
		 * CallDialerLog.Calls.CONTENT_URI, projection, null, null,
		 * CallDialerLog.Calls.DEFAULT_SORT_ORDER);
		 */
		c = getSherlockActivity().getContentResolver().query(
				CallLog.Calls.CONTENT_URI, projection, where, args,
				CallLog.Calls.DEFAULT_SORT_ORDER);

		if (c != null && c.getCount() > 0) {

			try {

				c.moveToFirst();
				do {

					DialerCallLogList mMsgItem = new DialerCallLogList();
					// ArrayList<String> arr_Type = new ArrayList<String>();

					String lastCallnumber = c.getString(0);
					String type = c.getString(1);
					String name = c.getString(2);
					String date = c.getString(3);
					String numberType = c.getString(4);
					String _id = c.getString(5);

					Date expiry = new Date(Long.parseLong(date));

					DialerLog.e(
							"Call Log List",
							"No. " + lastCallnumber + " Type: " + type
									+ " Name: " + name + " date: " + date
									+ "   "
									+ DialerUtilities.frmEpochDate(expiry)
									+ " Number Type: " + numberType + "ID : "
									+ _id);
					mMsgItem.setDATE(DialerUtilities.frmEpochDate(expiry));
					if (numberType != null
							&& !(numberType.equalsIgnoreCase(null))) {
						mMsgItem.setNUMBER_TYPE(getNumberType(Integer
								.valueOf(numberType)));
					}
					if (_id != null && !(_id.equalsIgnoreCase(null))) {
						mMsgItem.setFOREIGN_KEY_STRING(_id);
					}
					if (!(lastCallnumber.equalsIgnoreCase(lastNo))) {

						ArrayList<String> arr_date = new ArrayList<String>();
						arr_Date = null;
						arr_Date = arr_date;
						arr_Date.add(date);
						mMsgItem.setCALL_DURATION(arr_Date);

						mMsgItem.setNUMBER(lastCallnumber);

						lastNo = lastCallnumber;

						ArrayList<String> arr_Type1 = new ArrayList<String>();
						arr_Type = null;
						arr_Type = arr_Type1;
						arr_Type.add(type);

						mMsgItem.setCALL_TYPE_STRING(arr_Type);

						try {
							if (name != null && !(name.equalsIgnoreCase(null))
									&& !name.isEmpty()) {
								mMsgItem.setASSOICIATE_NAME(name);
								mMsgItem.setNativeContact(true);
								mMsgItem.setNativeProfilePic(true);
								// DialerUtilities.populateNativeCallLogList(mMsgItem);

							} else {
								// DialerUtilities.populateCorporateCallLogList(mMsgItem);
								DialerUtilities.getDetails(mMsgItem,
										Email.getAppContext(), lastNo);
							}

						} catch (Exception e) {

						}

						count = 1;
						mMsgItem.setCALL_NO_TIMES_STRING("1");

						mMsgItemList.add(mMsgItem);

					} else {
						count = count + 1;

						DialerLog.e(
								"Call Count ",
								"frmDeviceLog catch "
										+ count
										+ "  "
										+ mMsgItemList.get(
												mMsgItemList.size() - 1)
												.getNUMBER() + "Listsize--> "
										+ mMsgItemList.size()
										+ " arr_typesize " + arr_Type.size()
										+ "   " + type);

						mMsgItemList.get(mMsgItemList.size() - 1)
								.setCALL_NO_TIMES_STRING(
										Integer.toString(count));

						arr_Type.add(type);
						arr_Date.add(date);

						mMsgItemList.get(mMsgItemList.size() - 1)
								.setCALL_TYPE_STRING(arr_Type);
						mMsgItemList.get(mMsgItemList.size() - 1)
								.setCALL_DURATION(arr_Date);
						// mMsgItemList.get(mMsgItemList.size()-(mMsgItemList.size()-arr_Type.size())).setCALL_TYPE_STRING(arr_Type);
						// mMsgItemList.get(count).setCALL_TYPE_STRING(arr_Type);

					}
					// mMsgItemList.get(mMsgItemList.size() -
					// 1).setCALL_TYPE_STRING(arr_Type);
					mMsgItem = null;

				} while (c.moveToNext());
			} catch (Exception e) {

			}
			c.close();

		}

		lastNo = "";
		return mMsgItemList;
	}

	public class splitInfoDialog extends DialogFragment {

		public splitInfoDialog() {

		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			setRetainInstance(true);
			super.onCreate(savedInstanceState);
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {

			Bundle b = getArguments();
			int selection = b.getInt("SelectedCallType");
			return new AlertDialog.Builder(getSherlockActivity())
					.setTitle("")
					.setSingleChoiceItems(items, selection,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int item) {

									dialog.cancel();
									setSharedPrefrenceSelectOption(
											"SelectedCallType", item);
									getCallLogListBasedOnContactType(
											items[item].toString(),
											mCallLogArrayList);
								}
							}).create();
		}

		@Override
		public void onDestroyView() {
			// TODO Auto-generated method stub
			if (getDialog() != null && getRetainInstance()) {
				getDialog().setOnDismissListener(null);
			}
			super.onDestroyView();
		}
	}

	private String getNumberType(int key) {
		String type = "";
		switch (key) {
		case 1:
			type = "HOME";
			break;
		case 2:
			type = "MOBILE";
			break;
		case 3:
			type = "WORK";
			break;
		case 4:
			type = "FAX WORK";
			break;
		case 5:
			type = "FAX HOME ";
			break;
		case 6:
			type = "PAGER";
			break;
		case 7:
			type = "OTHER";
			break;
		case 8:
			type = "CALLBACK";
			break;
		case 9:
			type = "CAR";
			break;
		case 10:
			type = "COMPANY MAIN";
			break;
		case 11:
			type = "ISDN";
			break;
		case 12:
			type = "MAIN";
			break;
		case 13:
			type = "OTHER FAX";
			break;
		case 14:
			type = "RADIO";
			break;
		case 15:
			type = "TELEX";
			break;
		case 16:
			type = "TTY TDD";
			break;
		case 17:
			type = "WORK MOBILE";
			break;
		case 18:
			type = "WORK PAGER";
			break;
		case 19:
			type = "ASSISTANT";
			break;

		default:
			break;
		}

		return type;
	}

	private class CallLogAsyncTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected Void doInBackground(Void... params) {

			System.out.println("aysnc task do in bg called");

			mCallLogArrayList = new ArrayList<DialerCallLogList>();

			ArrayList<DialerCallLogList> mCallLog = frmDeviceLog();

			if (mCallLog != null && !mCallLog.isEmpty()) {
				mCallLogArrayList.addAll(mCallLog);

			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			getCallLogListBasedOnContactType(getContactType(),
					mCallLogArrayList);

		}

	}

	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {

			newCallLogList = (ArrayList<DialerCallLogList>) msg.obj;

			int selectedItem = new SharedPreferences(Email.getAppContext())
					.getInt("currentListIndex", 0);

			if (newCallLogList != null && newCallLogList.size() > 0) {

				if (selectedItem > newCallLogList.size()) {
					selectedItem = 0;
				}

				mcalAdap = new DialerCallogAdapter(Email.getAppContext(),
						newCallLogList);

				textEmptyCallLog.setVisibility(View.GONE);
				lstCalLog.setVisibility(View.VISIBLE);

				callTypeLayout.setVisibility(View.VISIBLE);
				textCallType.setText(getHeaderTextData());
				if (selectedItem != 0) {
					lstCalLog.setAdapter(null);
					mcalAdap.setSelectedPosition(selectedItem);
				}
				lstCalLog.setAdapter(mcalAdap);
				mcalAdap.notifyDataSetChanged();

			} else {

				System.out.println("call list empty in handler");
				textEmptyCallLog.setVisibility(View.VISIBLE);
				lstCalLog.setVisibility(View.GONE);
				callTypeLayout.setVisibility(View.GONE);
				textEmptyCallLog.setText(getHeaderTextNoData());

				SharedPreferences.Editor edit = new SharedPreferences(
						Email.getAppContext()).edit();
				edit.putInt("currentListIndex", 0);
				edit.commit();

			}

		}
	};

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		// super.onListItemClick(l, v, position, id);
		// Object callHistoryObj=lstCalLog.getItemAtPosition(position);

		mCurrentSelectedItemIndex = position;

		Object callHistoryObj = newCallLogList.get(position);

		DialerCallLogList historyModel = (DialerCallLogList) callHistoryObj;

		mcalAdap.setSelectedPosition(mCurrentSelectedItemIndex);

		SharedPreferences.Editor edit = new SharedPreferences(
				Email.getAppContext()).edit();
		edit.putInt("currentListIndex", mCurrentSelectedItemIndex);
		edit.commit();

		Intent callHistoryDetailIntent = new Intent(Email.getAppContext(),
				DialerCallLogHistoryDetail.class);
		callHistoryDetailIntent.putExtra("objCallHistory", historyModel);
		startActivity(callHistoryDetailIntent);
	}

	@Override
	public void onDestroy() {

		super.onDestroy();
	}

	private String getHeaderTextData() {

		String contactType = "";
		if (getContactType().equalsIgnoreCase("Show all")) {
			contactType = "Corporate & Personal";
		} else if (getContactType().equalsIgnoreCase("Show Corporate")) {
			contactType = "Corporate";
		} else if (getContactType().equalsIgnoreCase("Show Personal")) {
			contactType = "Personal";
		}

		String header = "";

		if (ContactsConsts.callType.equalsIgnoreCase("")) {
			header = contactType + " - Call Log";
		} else if (ContactsConsts.callType.equalsIgnoreCase("1")) {
			header = contactType + " - Incoming Call Log";
		} else if (ContactsConsts.callType.equalsIgnoreCase("2")) {
			header = contactType + " - Outgoing Call Log";
		} else if (ContactsConsts.callType.equalsIgnoreCase("3")) {
			header = contactType + " - Missed Call Log";
		}

		return header;
	}

	private String getHeaderTextNoData() {

		String contactType = "";
		if (getContactType().equalsIgnoreCase("Show all")) {
			contactType = "Corporate & Personal";
		} else if (getContactType().equalsIgnoreCase("Show Corporate")) {
			contactType = "Corporate";
		} else if (getContactType().equalsIgnoreCase("Show Personal")) {
			contactType = "Personal";
		}

		String header = "";

		if (ContactsConsts.callType.equalsIgnoreCase("")) {
			header = contactType + " - Call log is empty";
		} else if (ContactsConsts.callType.equalsIgnoreCase("1")) {
			header = contactType + " - No Incoming Call Log";
		} else if (ContactsConsts.callType.equalsIgnoreCase("2")) {
			header = contactType + " - No Outgoing Call Log";
		} else if (ContactsConsts.callType.equalsIgnoreCase("3")) {
			header = contactType + " - No Missed Call Log";
		}

		System.out.println("Header data for empty list  ->  " + header);

		return header;
	}
}
