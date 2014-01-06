package com.cognizant.trumobi.dialer.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.TruBoxSDK.SharedPreferences;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.cognizant.trubox.contacts.db.ContactsConsts;
import com.cognizant.trubox.contacts.db.ContactsModel;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.contacts.activity.ContactsDetailActivity;
import com.cognizant.trumobi.contacts.adapter.ContactsListAdapter;
import com.cognizant.trumobi.contacts.utils.ContactsUtilities;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.log.DialerLog;

@SuppressLint("HandlerLeak")
public class DialerContactFragment extends SherlockListFragment {

	private ListView mContactList;
	private TextView mContactsCountView, mContactsNotAvailable;
	private LinearLayout contactCounthHolder;
	private int mCurrentSelectedItemIndex = -1;
	private ContactsListAdapter listAdapter;
	private Cursor cursor;
	private ArrayList<ContactsModel> mergedContacts;
	private ProgressDialog pDialog;
	private ContactsAsyncTask contactsAsyncTask;
	private boolean nativeContactsPrefsVal;
	private String sortOrder;
	public static final String TAG = "DialerContactsFragment";

	@Override
	public SherlockFragmentActivity getSherlockActivity() {
		return super.getSherlockActivity();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		IntentFilter filter = new IntentFilter(
				"com.cognizant.trumobi.contact.sync_completed");
		Email.getAppContext().registerReceiver(receiveCompleteSync, filter);

		if (!ContactsUtilities.isTablet(Email.getAppContext())) {
			setRetainInstance(true);
		}

		loadContacts();
	}

	BroadcastReceiver receiveCompleteSync = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			DialerLog.d("receiveCompleteSync", "on update...");
			if (listAdapter != null) {

				loadContacts();
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Get the view from fragmenttab2.xml
		// View view = inflater.inflate(R.layout.contact_list, container,
		// false);
		View view = inflater.inflate(R.layout.dial_phone_contact, container,
				false);

		contactCounthHolder = (LinearLayout) view
				.findViewById(R.id.user_profile_holder);
		mContactsNotAvailable = (TextView) view
				.findViewById(R.id.noContacts_txt);
		mContactsCountView = (TextView) view.findViewById(R.id.contacts_count);
		mContactList = (ListView) view.findViewById(android.R.id.list);
		mContactList.setCacheColorHint(Color.WHITE);
		mContactList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		view.setFilterTouchesWhenObscured(true);
		return view;
	}

	private void loadContacts() {
		nativeContactsPrefsVal = new SharedPreferences(Email.getAppContext())
				.getBoolean("prefNativeContacts", false);

		sortOrder = new SharedPreferences(Email.getAppContext()).getString(
				"prefSortOrder", ContactsConsts.CONTACT_FIRST_NAME);

		if (contactsAsyncTask != null) {
			contactsAsyncTask.cancel(true);
		}
		contactsAsyncTask = new ContactsAsyncTask();
		contactsAsyncTask.execute();
	}

	private class ContactsAsyncTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			if (pDialog == null) {
				pDialog = new ProgressDialog(getSherlockActivity());
				pDialog.setMessage(" Loading.. Please Wait");
				pDialog.setCancelable(false);
				// pDialog.show();
			} else if (pDialog != null) {
				// pDialog.show();
			}

		}

		@Override
		protected Void doInBackground(Void... params) {
			mergedContacts = new ArrayList<ContactsModel>();
			try {
				cursor = Email
						.getAppContext()
						.getContentResolver()
						.query(ContactsConsts.CONTENT_URI_CONTACTS, null, null,
								null, sortOrder + " COLLATE NOCASE");
				if (cursor != null) {
					if (nativeContactsPrefsVal) {
						ArrayList<ContactsModel> nativeContacts = ContactsUtilities
								.PopulateLocalContactsFromCursor(Email
										.getAppContext().getContentResolver());
						if (nativeContacts != null) {
							mergedContacts.addAll(nativeContacts);
						}
					}
					ArrayList<ContactsModel> corporateContacts = ContactsUtilities
							.PopulateArraylistFromCursor(cursor,
									Email.getAppContext());
					if (corporateContacts != null) {
						mergedContacts.addAll(corporateContacts);
					}

				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void params) {
			super.onPostExecute(params);
			Message msg = new Message();
			msg.obj = mergedContacts;
			handler.sendMessage(msg);

		}
	}

	private Handler handler = new Handler() {
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {

			int time_remaining = new SharedPreferences(Email.getAppContext())
					.getInt("currentListIndex", 0);

			mergedContacts = (ArrayList<ContactsModel>) msg.obj;

			if (time_remaining > mergedContacts.size()) {
				time_remaining = 0;
			}
			if (mergedContacts.size() > 0) {

				SharedPreferences.Editor prefEditor = new SharedPreferences(
						Email.getAppContext()).edit();
				prefEditor.putBoolean("ContactsCheckAvailability", true);
				prefEditor.commit();

				mContactsNotAvailable.setVisibility(View.GONE);
				if (pDialog != null) {

					try {
						pDialog.dismiss();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
				}

				listAdapter = new ContactsListAdapter(getActivity(),
						mergedContacts);

				String sortOrder = new SharedPreferences(Email.getAppContext())
						.getString("prefSortOrder",
								ContactsConsts.CONTACT_FIRST_NAME);

				if (sortOrder.equals("first_name")) {

					Collections.sort(mergedContacts,
							new Comparator<ContactsModel>() {
								public int compare(ContactsModel obj1,
										ContactsModel obj2) {
									String s1 = obj1.getcontacts_first_name()
											+ obj1.getcontacts_last_name();
									String s2 = obj2.getcontacts_first_name()
											+ obj2.getcontacts_last_name();
									return s1.compareToIgnoreCase(s2);
								}
							});

				} else if (sortOrder.equals("last_name")) {
					Collections.sort(mergedContacts,
							new Comparator<ContactsModel>() {
								public int compare(ContactsModel obj1,
										ContactsModel obj2) {
									String s1 = obj1.getcontacts_last_name()
											+ obj1.getcontacts_first_name();
									String s2 = obj2.getcontacts_last_name()
											+ obj2.getcontacts_first_name();
									return s1.compareToIgnoreCase(s2);
								}
							});
				}

				contactCounthHolder.setVisibility(View.VISIBLE);
				mContactsCountView.setText(mergedContacts.size() + " Contacts");
				if (time_remaining != 0) {
					mContactList.setAdapter(null);
					mContactList.setTag(null);
					listAdapter.setSelectedPosition(time_remaining);
				}
				mContactList.setAdapter(listAdapter);
				mContactList.setTag(ContactsModel.class);
				listAdapter.notifyDataSetChanged();

			} else {
				if (pDialog != null) {

					try {
						pDialog.dismiss();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
				}
				SharedPreferences.Editor edit = new SharedPreferences(
						Email.getAppContext()).edit();
				edit.putInt("currentListIndex", 0);
				edit.putString("currentListRowID", null);
				edit.commit();
				if (listAdapter != null) {
					contactCounthHolder.setVisibility(View.GONE);
					mContactList.setAdapter(null);
					mContactList.setTag(null);
					listAdapter.notifyDataSetChanged();

				}
				listAdapter = null;
				SharedPreferences.Editor prefEditor = new SharedPreferences(
						Email.getAppContext()).edit();
				prefEditor.putBoolean("ContactsCheckAvailability", false);
				prefEditor.commit();

				mContactsNotAvailable.setVisibility(View.VISIBLE);

			}
		}
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setRetainInstance(true);
		if (listAdapter != null) {
			if (mergedContacts.size() > 0) {
				contactCounthHolder.setVisibility(View.VISIBLE);
				mContactsCountView.setText(mergedContacts.size() + " Contacts");
				mContactList.setAdapter(listAdapter);
				mContactList.setTag(ContactsModel.class);
				listAdapter.notifyDataSetChanged();
			}
		} else {
			mContactsNotAvailable.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("currentListIndex", mCurrentSelectedItemIndex);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		super.onConfigurationChanged(newConfig);

	}

	@Override
	public void onListItemClick(ListView listview, View view, int position,
			long id) {
		mCurrentSelectedItemIndex = position;
		Object obj = mContactList.getItemAtPosition(position);
		ContactsModel contact = (ContactsModel) obj;
		listAdapter.setSelectedPosition(mCurrentSelectedItemIndex);

		SharedPreferences.Editor edit = new SharedPreferences(
				Email.getAppContext()).edit();
		edit.putInt("currentListIndex", mCurrentSelectedItemIndex);
		edit.putString("currentListRowID",
				String.valueOf(contact.getContact_id()));
		edit.commit();

		Intent contactDetailIntent = new Intent(Email.getAppContext(),
				ContactsDetailActivity.class);
		contactDetailIntent.putExtra("obj", contact);
		startActivity(contactDetailIntent);

	}

	@Override
	public void onResume() {
		boolean isListRefresh = new SharedPreferences(Email.getAppContext())
				.getBoolean("isPrefUpdated", false);

		if (isListRefresh) {
			SharedPreferences.Editor prefEditor = new SharedPreferences(
					Email.getAppContext()).edit();
			prefEditor.putBoolean("isPrefUpdated", false);
			prefEditor.commit();
			loadContacts();
		}
		super.onResume();
	}

	@Override
	public void onPause() {

		super.onPause();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {

		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onDestroyView() {

		try {
			if (receiveCompleteSync != null)
				Email.getAppContext().unregisterReceiver(receiveCompleteSync);
		} catch (Exception e) {

		}

		/*
		 * SharedPreferences.Editor edit = new SharedPreferences(
		 * Email.getAppContext()).edit(); edit.putInt("currentListIndex", 0);
		 * edit.putString("currentListRowID", null); edit.commit();
		 */
		super.onDestroyView();
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

}
