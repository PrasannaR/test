package com.cognizant.trumobi.contacts.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.TruBoxSDK.SharedPreferences;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnActionExpandListener;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnCloseListener;
import com.cognizant.trubox.contacts.db.ContactsConsts;
import com.cognizant.trubox.contacts.db.ContactsModel;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.contacts.adapter.ContactsListAdapter;
import com.cognizant.trumobi.contacts.utils.ContactsUtilities;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.em.provider.EmEmailContent;
import com.cognizant.trumobi.exchange.EmEasSyncService;
import com.cognizant.trumobi.exchange.provider.EmGalResult;
import com.cognizant.trumobi.log.ContactsLog;

public class ContactsAllContactsFragment extends SherlockListFragment {
	private GalSearchTask mSearchAsynctask = null;
	private String mCurrentAccountName = null;
	private ContactsListAdapter mSearchListAdaptor = null;
	private Menu mContextMenu;
	private ProgressDialog searchDialog;
	private TextView mContactsNotAvailable;
	private ListView mContactList;
	private TextView mContactsCountView;
	private LinearLayout contactCounthHolder;
	private int mCurrentSelectedItemIndex = 0;
	private SearchView mSearchView;
	private ContactsListAdapter listAdapter;
	private Cursor cursor;
	private ArrayList<ContactsModel> mergedContacts;
	private ArrayList<ContactsModel> SearchContacts;
	private ProgressDialog pDialog;
	private ContactsAsyncTask contactsAsyncTask;
	boolean nativeContactsPrefsVal;
	private String sortOrder;
	private FragmentManager fragmentManager;
	private boolean isNewSearch = false;

	private String mSearchTerm;

	public static final String TAG = ContactsAllContactsFragment.class
			.getName();

	private ContactsFragmentChangeListner mActivityCallBack;

	public interface ContactsFragmentChangeListner {

		public void onUpdate(ContactsModel position);
		
		public void onSearchInitiated(boolean isonSearchView);


	}

	@Override
	public SherlockFragmentActivity getSherlockActivity() {
		return super.getSherlockActivity();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ContactsLog.d(TAG, "onCreate ");
		setHasOptionsMenu(true);
		fragmentManager = getSherlockActivity().getSupportFragmentManager();
		IntentFilter filter = new IntentFilter(
				"com.cognizant.trumobi.contact.sync_completed");
		Email.getAppContext().registerReceiver(receiveCompleteSync, filter);
		if (savedInstanceState != null) {

			mSearchTerm = savedInstanceState.getString(SearchManager.QUERY);

		}
		if (!ContactsUtilities.isTablet(Email.getAppContext())) {
			setRetainInstance(true);
		} else {
			Bundle data = new Bundle();
			ContactsDetailFragment ctcDetailFragment = new ContactsDetailFragment();
			data.putSerializable("obj", null);
			ctcDetailFragment.setArguments(data);

			try {

				FragmentTransaction transaction = getSherlockActivity()
						.getSupportFragmentManager().beginTransaction();
				transaction.replace(R.id.displayDetail, ctcDetailFragment,
						ContactsDetailFragment.TAG);
				transaction.commit();
			} catch (IllegalStateException e1) {
				e1.printStackTrace();
			}
		}
		if (mCurrentAccountName == null) {
			mCurrentAccountName = GetAccountDetails();
		}
		loadContacts();
	}

	BroadcastReceiver receiveCompleteSync = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			ContactsLog.d("receiveCompleteSync", "on update...");
			loadContacts();
		}
	};

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ContactsLog.d(TAG, "onCreateView ");
		// Get the view from Allcontacts.xml
		View view = inflater.inflate(R.layout.contact_list, container, false);

		mContactsNotAvailable = (TextView) view
				.findViewById(R.id.noContacts_txt);

		contactCounthHolder = (LinearLayout) view
				.findViewById(R.id.user_profile_holder);
		mContactsCountView = (TextView) view.findViewById(R.id.contacts_count);
		mContactList = (ListView) view.findViewById(android.R.id.list);
		mContactList.setCacheColorHint(Color.WHITE);
		mContactList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		view.setFilterTouchesWhenObscured(true);

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setRetainInstance(true);
		ContactsLog.d(TAG, "onActivityCreated ");
		if (listAdapter != null) {
			if (mergedContacts.size() > 0) {
				contactCounthHolder.setVisibility(View.VISIBLE);
				if (mergedContacts.size() == 1) {
					mContactsCountView.setText(mergedContacts.size()
							+ " Contact");
				} else {
					mContactsCountView.setText(mergedContacts.size()
							+ " Contacts");
				}

				mContactList.setAdapter(listAdapter);
				mContactList.setTag(ContactsModel.class);
				listAdapter.notifyDataSetChanged();

				// On Orientaion Maintain the Selected Index value
				if (ContactsUtilities.isTablet(Email.getAppContext())) {
					ContactsModel contact = (ContactsModel) mContactList
							.getItemAtPosition(new SharedPreferences(Email
									.getAppContext()).getInt(
									"currentListIndex", 0));
					Bundle data = new Bundle();
					ContactsDetailFragment ctcDetailFragment = new ContactsDetailFragment();
					data.putSerializable("obj", contact);
					ctcDetailFragment.setArguments(data);

					try {
						FragmentTransaction transaction = getSherlockActivity()
								.getSupportFragmentManager().beginTransaction();
						transaction.replace(R.id.displayDetail,
								ctcDetailFragment, ContactsDetailFragment.TAG);
						transaction.commit();
					} catch (IllegalStateException e1) {
						e1.printStackTrace();
					}
				}
			}

		} else {
			//mContactsNotAvailable.setVisibility(View.VISIBLE);

			if (ContactsUtilities.isTablet(Email.getAppContext())) {
				Bundle data = new Bundle();
				ContactsDetailFragment ctcDetailFragment = new ContactsDetailFragment();
				data.putSerializable("obj", null);
				ctcDetailFragment.setArguments(data);
				try {

					FragmentTransaction transaction = getSherlockActivity()
							.getSupportFragmentManager().beginTransaction();
					transaction.replace(R.id.displayDetail, ctcDetailFragment,
							ContactsDetailFragment.TAG);
					transaction.commit();
				} catch (IllegalStateException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);

		mActivityCallBack = (ContactsFragmentChangeListner) activity;

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt("currentListIndex", mCurrentSelectedItemIndex);

		if (!TextUtils.isEmpty(mSearchTerm)) {
			// Saves the current search string
			outState.putString(SearchManager.QUERY, mSearchTerm);
			ContactsLog.d(TAG, "onSaveInstanceState " + mSearchTerm);
		}

	}

	private void loadContacts() {

		ContactsLog.d(TAG, "loadContacts ");
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
				pDialog.show();
			} else if (pDialog != null) {
				pDialog.show();
			}

		}// End of onPreExecute method

		@Override
		protected Void doInBackground(Void... params) {
			mergedContacts = new ArrayList<ContactsModel>();

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
				cursor.close();
			}
			cursor = null;

			return null;
		}// End of doInBackground method

		@Override
		protected void onPostExecute(Void params) {
			super.onPostExecute(params);

			Message msg = new Message();
			msg.obj = mergedContacts;
			handler.sendMessage(msg);

		}// End of onPostExecute method

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

				ContactsLog.i("NEW", "-==== " + mergedContacts.size());
				listAdapter = new ContactsListAdapter(getActivity(),
						mergedContacts);

				String sortOrder = new SharedPreferences(Email.getAppContext())
						.getString("prefSortOrder",
								ContactsConsts.CONTACT_FIRST_NAME);
				// System.out.println("get cusor: " + sortOrder.toString());
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
				if (mergedContacts.size() == 1) {
					mContactsCountView.setText(mergedContacts.size()
							+ " Contact");
				} else {
					mContactsCountView.setText(mergedContacts.size()
							+ " Contacts");
				}
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
					time_remaining = 0;

				}
				listAdapter = null;
				SharedPreferences.Editor prefEditor = new SharedPreferences(
						Email.getAppContext()).edit();
				prefEditor.putBoolean("ContactsCheckAvailability", false);
				prefEditor.commit();

				mContactsNotAvailable.setVisibility(View.VISIBLE);
			}
			final String cursorId = new SharedPreferences(Email.getAppContext())
					.getString("currentListRowID", null);

			ContactsLog.d(TAG, "cursorId" + cursorId);
			if (ContactsUtilities.isTablet(Email.getAppContext())) {
				Object obj = null;
				if (cursorId != null) {

					if (time_remaining > mContactList.getCount()) {
						time_remaining = 0;
					} else {
						obj = mContactList.getItemAtPosition(time_remaining);
					}
				} else {
					obj = mContactList.getItemAtPosition(0);
				}

				ContactsModel contact = (ContactsModel) obj;

				if (ContactsUtilities.isTablet(Email.getAppContext())) {
					if (listAdapter != null) {
						if (time_remaining > listAdapter.getCount()) {
							time_remaining = 0;
						}

						listAdapter.setSelectedPosition(time_remaining);
						mContactList.setSelection(time_remaining);
					}
					mActivityCallBack.onUpdate(contact);

				}
			}

		}
	};

	@Override
	public void onListItemClick(ListView listview, View view, int position,
			long id) {
		mCurrentSelectedItemIndex = position;

		ContactsModel contact = (ContactsModel) listview.getAdapter().getItem(
				position);

		SharedPreferences.Editor edit = new SharedPreferences(
				Email.getAppContext()).edit();
		edit.putInt("currentListIndex", mCurrentSelectedItemIndex);
		edit.putString("currentListRowID",
				String.valueOf(contact.getContact_id()));
		edit.commit();

		Bundle data = new Bundle();

		if (ContactsUtilities.isTablet(Email.getAppContext())) {
			if (listAdapter != null) {
				if (mCurrentSelectedItemIndex > listAdapter.getCount()) {
					mCurrentSelectedItemIndex = 0;
				}
				listAdapter.setSelectedPosition(mCurrentSelectedItemIndex);
				mContactList.setSelection(mCurrentSelectedItemIndex);
			}
			mActivityCallBack.onUpdate(contact);

		} else {
			Intent contactDetailIntent = new Intent(Email.getAppContext(),
					ContactsDetailActivity.class);

			contactDetailIntent.putExtra("obj", contact);
			startActivity(contactDetailIntent);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		if (SearchContacts != null && SearchContacts.size() > 0) {
			ContactsLog.d(TAG, "onresume " + SearchContacts.size());
			if (SearchContacts.size() == 1) {
				mContactsCountView.setText(SearchContacts.size() + " Contact");
			} else {
				mContactsCountView.setText(SearchContacts.size() + " Contacts");
			}

			mSearchListAdaptor = new ContactsListAdapter(getActivity(),
					SearchContacts);
			mContactList.setAdapter(mSearchListAdaptor);

			populatesearchResult(SearchContacts.get(0), 0, false);
			mContactList.setAdapter(mSearchListAdaptor);

			mSearchListAdaptor.notifyDataSetChanged();

			return;
		} else {
			ContactsLog.d(TAG, "onresume 0 # " + mSearchListAdaptor);
		}
		ContactsLog.d(TAG, "onresume loading..");
		boolean isListRefresh = new SharedPreferences(Email.getAppContext())
				.getBoolean("isPrefUpdated", false);

		if (isListRefresh) {
			SharedPreferences.Editor prefEditor = new SharedPreferences(
					Email.getAppContext()).edit();
			prefEditor.putBoolean("isPrefUpdated", false);
			prefEditor.commit();
			loadContacts();
		}
	}

	@Override
	public void onPause() {

		super.onPause();
		// TODO Auto-generated method stub
		try {
			if (pDialog != null && pDialog.isShowing()) {
				pDialog.dismiss();
			}
			pDialog = null;
		} catch (Exception e) {
			ContactsLog.e(TAG, "onPause " + e.toString());
		}

	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub

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
	public void onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onPrepareOptionsMenu(menu);
	}

	class SearchexpandListner implements OnActionExpandListener {

		@Override
		public boolean onMenuItemActionExpand(MenuItem item) {
			
			mSearchView.setOnQueryTextListener(queryTextListener);
			
			isNewSearch = true;

			getSherlockActivity().getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

			// mSearchTerm = null;
			ContactsLog.d(TAG, "onMenuItemActionExpand***");
			getSherlockActivity().getSupportActionBar().setNavigationMode(
					ActionBar.NAVIGATION_MODE_STANDARD);

			mContextMenu.findItem(R.id.menu_add_contact).setVisible(false);
			mContextMenu.findItem(R.id.menu_search).setVisible(false);
			mContextMenu.findItem(R.id.contactstodisplay).setVisible(false);
			mContextMenu.findItem(R.id.gsettings).setVisible(false);
			mContextMenu.findItem(R.id.menu_show_connect).setVisible(true);
			mContextMenu.findItem(R.id.menu_manual_sync).setVisible(false);
			if (ContactsUtilities.isTablet(getActivity())) {

				/*mContextMenu.findItem(R.id.gsettings).setVisible(true);

				mContextMenu.findItem(R.id.menu_edit).setVisible(false);
				mContextMenu.findItem(R.id.menu_delete).setVisible(false);
				mContextMenu.findItem(R.id.menu_SetRingtone).setVisible(false);
				mContextMenu.findItem(R.id.menu_show_connect).setVisible(true);
				mContextMenu.findItem(R.id.menu_share).setVisible(false);

				mContextMenu.findItem(R.id.menu_add_contact).setVisible(false);*/
				
				
				mContextMenu.findItem(R.id.gsettings).setVisible(true);

				//mContextMenu.findItem(R.id.menu_edit).setVisible(false);
				//mContextMenu.findItem(R.id.menu_delete).setVisible(false);
				//mContextMenu.findItem(R.id.menu_SetRingtone).setVisible(false);
				mContextMenu.findItem(R.id.menu_show_connect).setVisible(true);
				//mContextMenu.findItem(R.id.menu_share).setVisible(false);

				mContextMenu.findItem(R.id.menu_add_contact).setVisible(false);
				
				

			} else {
				
				
				mContextMenu.findItem(R.id.menu_invisible).setVisible(true);
				mContextMenu.findItem(R.id.menu_invisible2).setVisible(true);
				mContextMenu.findItem(R.id.menu_invisible3).setVisible(false);
				
				mContextMenu.findItem(R.id.menu_invisible).setEnabled(false);
				mContextMenu.findItem(R.id.menu_invisible2).setEnabled(false);
				mContextMenu.findItem(R.id.menu_invisible3).setEnabled(false);
				
				
				mActivityCallBack.onSearchInitiated(true);
				mContextMenu.findItem(R.id.menu_add_group_contact).setVisible(
						false);
			}

			return true;
		}

		@Override
		public boolean onMenuItemActionCollapse(MenuItem item) {
			mSearchTerm = null;
			isNewSearch = false;

			getSherlockActivity().getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

			ContactsLog.d(TAG, "onMenuItemActionCollapse***");
			mSearchListAdaptor = null;
			SearchContacts = null;
			mSearchView.setIconified(true);

			if (mergedContacts != null && mergedContacts.size() > 0) {
				ContactsLog.d(TAG, "onMenuItemActionCollapse***"
						+ mergedContacts.size());
				// if (listAdapter == null) {
				listAdapter = new ContactsListAdapter(getActivity(),
						mergedContacts);
				// }

				mContactList.setAdapter(listAdapter);
				mActivityCallBack.onUpdate(listAdapter.getItem(0));
				listAdapter.notifyDataSetChanged();
				mCurrentSelectedItemIndex = 0;
				mContactsNotAvailable.setVisibility(View.GONE);
				if (mergedContacts.size() == 1) {
					mContactsCountView.setText(mergedContacts.size()
							+ " Contact");
				} else {
					mContactsCountView.setText(mergedContacts.size()
							+ " Contacts");
				}

			} else {
				ContactsLog.d(TAG, "onMenuItemActionCollapse***"
						+ mergedContacts);
				listAdapter = null;
				mActivityCallBack.onUpdate(null);
				mContactList.setAdapter(null);
				mContactsNotAvailable.setVisibility(View.VISIBLE);

			}

			mContextMenu.findItem(R.id.menu_add_contact).setVisible(true);
			mContextMenu.findItem(R.id.menu_manual_sync).setVisible(true);
			mContextMenu.findItem(R.id.menu_search).setVisible(true);
			mContextMenu.findItem(R.id.contactstodisplay).setVisible(true);
			mContextMenu.findItem(R.id.gsettings).setVisible(true);
			mContextMenu.findItem(R.id.menu_show_connect).setVisible(true);

			if (ContactsUtilities.isTablet(getActivity())) {
				mContextMenu.findItem(R.id.menu_edit).setVisible(true);
				mContextMenu.findItem(R.id.menu_delete).setVisible(true);
				mContextMenu.findItem(R.id.menu_SetRingtone).setVisible(true);
				mContextMenu.findItem(R.id.menu_show_connect).setVisible(true);
				mContextMenu.findItem(R.id.menu_share).setVisible(true);

				mContextMenu.findItem(R.id.menu_add_contact).setVisible(true);

				getSherlockActivity().getSupportActionBar().setNavigationMode(
						ActionBar.NAVIGATION_MODE_LIST);

			} else {
				
				
				mContextMenu.findItem(R.id.menu_invisible).setVisible(false);
				mContextMenu.findItem(R.id.menu_invisible2).setVisible(false);
				mContextMenu.findItem(R.id.menu_invisible3).setVisible(false);
				
				mContextMenu.findItem(R.id.menu_invisible).setEnabled(false);
				mContextMenu.findItem(R.id.menu_invisible2).setEnabled(false);
				mContextMenu.findItem(R.id.menu_invisible3).setEnabled(false);
				
				
				mActivityCallBack.onSearchInitiated(false);
				getSherlockActivity().getSupportActionBar().setNavigationMode(
						ActionBar.NAVIGATION_MODE_TABS);
			}
			
			
			//Refresh Listview only when the contact is added
			boolean isListRefresh = new SharedPreferences(Email.getAppContext())
					.getBoolean("isPrefUpdated", false);
			if (isListRefresh) {
				SharedPreferences.Editor prefEditor = new SharedPreferences(
						Email.getAppContext()).edit();
				prefEditor.putBoolean("isPrefUpdated", false);
				prefEditor.commit();
				loadContacts();
			}

			return true;
		}
	};

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu, inflater);
		ContactsLog.d(TAG, "onCreateOptionsMenu ");
		this.mContextMenu = menu;
		SearchManager searchManager = (SearchManager) getActivity()
				.getSystemService(Context.SEARCH_SERVICE);

		MenuItem searchitem = menu.findItem(R.id.menu_search);
		searchitem.setOnActionExpandListener(new SearchexpandListner());
		mSearchView = (SearchView) menu.findItem(R.id.menu_search)
				.getActionView();

		mSearchView.setQueryHint(getResources().getString(
				R.string.search_contacts));
		mSearchView.setSearchableInfo(searchManager
				.getSearchableInfo(getActivity().getComponentName()));
		mSearchView.setIconifiedByDefault(true);

		mSearchView.setSubmitButtonEnabled(false);
		

		mSearchView.setOnCloseListener(new OnCloseListener() {

			@Override
			public boolean onClose() {

				ContactsLog.d(TAG, "onCreateOptionsMenu searchview closed...");
				return false;
			}
		});

		if (mSearchTerm != null) {
			final String savedSearchTerm = mSearchTerm;
			searchitem.expandActionView();

			// mSearchView.setQuery(savedSearchTerm, false);

		}

	}

	SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
		public boolean onQueryTextChange(String newText) {
			ContactsLog.d("onOptionsItemSelected", "queryTextListener");

			String newFilter = !TextUtils.isEmpty(newText) ? newText : null;
			ContactsLog.d("onOptionsItemSelected",
					"queryTextListener newFilters" + newFilter
							+ " mSearchTerm " + mSearchTerm);
			
			if (!ContactsUtilities.isTablet(getActivity())) {
				// Don't do anything if the filter is empty
				if (mSearchTerm == null && newFilter == null) {
					return true;
				}
				if (mSearchTerm != null && mSearchTerm.equals(newFilter)) {
					return true;
				}
				// Updates current filter to new filter
				mSearchTerm = newFilter;
				ContactsLog.d("onOptionsItemSelected",
						"queryTextListener mSearchTerm" + mSearchTerm);
				searchContacts(mSearchTerm);
			}else{
				searchContacts(newFilter);
			}
			
			

			return true;
		}

		public boolean onQueryTextSubmit(String query) {

			return true;
		}
	};

	private void searchContacts(String query) {

		if (mCurrentAccountName == null) {
			mCurrentAccountName = GetAccountDetails();
		}

		if (mCurrentAccountName == null) {
			Toast.makeText(getActivity(),
					"Account informations are not available. please try again",
					Toast.LENGTH_SHORT).show();
		}

		if (mSearchAsynctask != null) {
			mSearchAsynctask.cancel(true);
		}
		mSearchAsynctask = new GalSearchTask();
		mSearchAsynctask.execute(query);
	}

	class GalSearchTask extends
			AsyncTask<String, Void, ArrayList<ContactsModel>> {

		boolean isGalSearch = false;
		String receivedExtra = null;

		@Override
		protected ArrayList<ContactsModel> doInBackground(String... params) {

			receivedExtra = params[0];
			SharedPreferences.Editor edit = new SharedPreferences(
					Email.getAppContext()).edit();
			edit.putInt("searchListIndex", 0);
			edit.commit();
			ContactsLog.d("onOptionsItemSelected", "receivedExtra "
					+ receivedExtra);
			// Local Search
			ArrayList<ContactsModel> newsearchResults = new ArrayList<ContactsModel>();

			if (receivedExtra == null) {
				return newsearchResults;
			}
			try {
				String where = ContactsConsts.CONTACT_FIRST_NAME
						+ " LIKE ? OR " + ContactsConsts.CONTACT_LAST_NAME
						+ " LIKE ?";
				String[] whereParameters = new String[] {
						"%" + receivedExtra + "%", "%" + receivedExtra + "%" };

				Cursor local_cursor = Email.getAppContext()
						.getContentResolver()
						.query(ContactsConsts.CONTENT_URI_CONTACTS, null,
								where, whereParameters, null);
				
				
				ContactsLog.d(TAG, "Local count # " + local_cursor.getCount());
				if (local_cursor != null && local_cursor.getCount() > 0) {
					newsearchResults = ContactsUtilities
							.PopulateArraylistFromCursor(local_cursor,
									Email.getAppContext());
				}

				local_cursor.close();
			} catch (Exception e) {
				Log.e("ContactsSearchFragment", "local_searh: " + e.toString());
			}
			ContactsLog.d(TAG, "Local newsearchResults # " + newsearchResults);
			if (receivedExtra.length() > 2) {

				ContactsLog.d("SearchActivity",
						"Ldap search initiated quesy string count "

						+ receivedExtra.length());

				if (isNewSearch) {

					publishProgress();

				}

				try {
					isGalSearch = false;
					long acc = Long.parseLong(mCurrentAccountName);
					/*
					 * EmailGalResult galResult = EasSyncService.searchGal(
					 * Email.getAppContext(), acc, receivedExtra, 25);
					 */

					EmGalResult galResult = EmEasSyncService.searchGal(
							Email.getAppContext(), acc, receivedExtra);

					isGalSearch = true;
					if (galResult != null) {

						ArrayList<ContactsModel> temp = ContactsUtilities
								.PopulateArraylistFromGalResult(galResult.galData);

						if (temp != null && temp.size() > 0) {

							if (newsearchResults == null) {
								newsearchResults = temp;
							} else {
								newsearchResults.addAll(temp);
							}
						}
					}
				} catch (Exception error) {
					ContactsLog
							.d("SearchActivity", "error " + error.toString());
				}
			}
			return newsearchResults;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			isNewSearch = false;
			Toast.makeText(getSherlockActivity(),
					"Searching corporate directory..", Toast.LENGTH_SHORT)
					.show();

		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			Message m = new Message();
			m.arg1 = 1;

			// DialogHandler.sendMessage(m);
		}

		@Override
		protected void onPostExecute(ArrayList<ContactsModel> newsearchResults) {
			// TODO Auto-generated method stub
			super.onPostExecute(newsearchResults);

			if (newsearchResults != null && newsearchResults.size() > 0) {
				mContactsNotAvailable.setVisibility(View.GONE);
				SearchContacts = newsearchResults;
				mSearchListAdaptor = new ContactsListAdapter(getActivity(),
						newsearchResults);

				if (SearchContacts.size() == 1) {
					mContactsCountView.setText(SearchContacts.size()
							+ " Contact");
				} else {
					mContactsCountView.setText(SearchContacts.size()
							+ " Contacts");
				}

				mContactList.setAdapter(mSearchListAdaptor);

				populatesearchResult(newsearchResults.get(0), 0, false);

			} else {

				SearchContacts = newsearchResults;
				mSearchListAdaptor = null;
				if (receivedExtra == null && mergedContacts != null
						&& mergedContacts.size() > 0) {
					ContactsLog.d(TAG, "onMenuItemActionCollapse***"
							+ mergedContacts.size());
					// if (listAdapter == null) {
					listAdapter = new ContactsListAdapter(getActivity(),
							mergedContacts);
					// }

					mContactList.setAdapter(listAdapter);
					mActivityCallBack.onUpdate(listAdapter.getItem(0));
					listAdapter.notifyDataSetChanged();
					mCurrentSelectedItemIndex = 0;
					mContactsNotAvailable.setVisibility(View.GONE);
					if (mergedContacts.size() == 1) {
						mContactsCountView.setText(mergedContacts.size()
								+ " Contact");
					} else {
						mContactsCountView.setText(mergedContacts.size()
								+ " Contacts");
					}

				} else {

					mContactsNotAvailable.setVisibility(View.VISIBLE);

					// mSearchListAdaptor = new
					// ContactsListAdapter(getActivity(),
					// newsearchResults);
					mContactsCountView.setText("0 Contact");
					mContactList.setAdapter(null);
					ContactsLog.d(TAG, "No search results available");

					mActivityCallBack.onUpdate(null);
					if (isGalSearch) {
						isGalSearch = false;

						Toast.makeText(getActivity(), "No contacts available",
								Toast.LENGTH_SHORT).show();
					}
				}
			}
		}
	}

	public void populatesearchResult(ContactsModel contact, int position,
			boolean itemclick) {

		mCurrentSelectedItemIndex = position;
		SharedPreferences.Editor edit = new SharedPreferences(
				Email.getAppContext()).edit();
		edit.putInt("searchListIndex", mCurrentSelectedItemIndex);
		edit.commit();
		Bundle data = new Bundle();
		data.putSerializable("obj", contact);

		if (ContactsUtilities.isTablet(Email.getAppContext())) {
			mActivityCallBack.onUpdate(contact);

		} else {
			if (itemclick) {
				Intent contactDetailIntent = new Intent(Email.getAppContext(),
						ContactsDetailActivity.class);
				ContactsUtilities.SELECTED_ID = null;

				contactDetailIntent.putExtras(data);
				startActivity(contactDetailIntent);
			}
		}

	}

	void showDialog() {
		try {
			if (searchDialog == null) {
				searchDialog = new ProgressDialog(getActivity());
			}
			searchDialog.setCancelable(true);
			searchDialog.setCanceledOnTouchOutside(false);
			searchDialog.setMessage("Searching contact...");
			searchDialog.show();
		} catch (Exception exp) {
			Log.e("ContactsSearchFragment", "local_searh: " + exp.toString());
			Toast.makeText(getActivity(), "Unable to complete the Search",
					Toast.LENGTH_SHORT).show();
		}

	}

	String GetAccountDetails() {

		Cursor c = null;
		try {

			/*
			 * c = Email .getAppContext() .getContentResolver()
			 * .query(EmailMailbox.CONTENT_URI, null, EmailMailbox.DISPLAY_NAME
			 * + "=?", new String[] { "Contacts" }, null);
			 */

			c = Email
					.getAppContext()
					.getContentResolver()
					.query(EmEmailContent.Mailbox.CONTENT_URI, null,
							EmEmailContent.Account.DISPLAY_NAME + "=?",
							new String[] { "Contacts" }, null);

			if (c != null && c.getCount() > 0) {
				c.moveToFirst();

				// return
				// c.getString(c.getColumnIndex(MailboxColumns.ACCOUNT_KEY));

				return c.getString(c
						.getColumnIndex(EmEmailContent.Mailbox.ACCOUNT_KEY));

			}

		} catch (Exception e) {

			ContactsLog.e(TAG, "Unable to get Account info");

		} finally {
			if (c != null) {
				c.close();
			}
		}
		return null;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub

		ContactsLog.d("onOptionsItemSelected", "Menu item selected..." + item);

		switch (item.getItemId()) {

		case R.id.menu_search:

			// item.setOnActionExpandListener();

		}
		return true;
	}

	@Override
	public void onDetach() {

		super.onDetach();

	}

}
