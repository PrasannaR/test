package com.cognizant.trumobi.dialer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.TruBoxSDK.SharedPreferences;
import com.actionbarsherlock.app.ActionBar;
import com.cognizant.trubox.contacts.db.ContactsConsts;
import com.cognizant.trubox.contacts.db.ContactsModel;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobBaseEditText;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseSherlockActivity;
import com.cognizant.trumobi.contacts.activity.ContactsDetailActivity;
import com.cognizant.trumobi.contacts.adapter.ContactsListAdapter;
import com.cognizant.trumobi.contacts.utils.ContactsUtilities;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.em.provider.EmEmailContent;
import com.cognizant.trumobi.exchange.EmEasSyncService;
import com.cognizant.trumobi.exchange.provider.EmGalResult;

public class DialerSearchActivity extends TruMobiBaseSherlockActivity {

	private ListView contactList;
	private TruMobBaseEditText searchText;
	private ContactsAsyncTask contactsAsyncTask;
	private Context mContext;
	private ArrayList<ContactsModel> mergedContacts;
	private ArrayList<ContactsModel> SearchContacts;
	private String sortOrder;
	private boolean nativeContactsPrefsVal;
	private ContactsListAdapter listAdapter;
	private ContactsListAdapter mSearchListAdaptor;
	private int mCurrentSelectedItemIndex = -1;
	private String mCurrentAccountName;
	private TextView mContactsCountView;
	private TextView mContactsNotAvailable;
	private ImageView dialerIcon;
	private LinearLayout contactCounthHolder;
	private GalSearchTask mSearchAsynctask = null;
	private boolean isNewSearch = false;
	private String searchTextString = "";

	public static final String TAG = DialerSearchActivity.class.getName();

	public String getSearchTextString() {
		return searchTextString;
	}

	public void setSearchTextString(String searchTextString) {
		this.searchTextString = searchTextString;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.dialer_search_list);
		mContext = this;

		final ActionBar mActionBar = getSupportActionBar();
		mActionBar.hide();
		mActionBar.setDisplayShowHomeEnabled(false);

		searchText = (TruMobBaseEditText) findViewById(R.id.search_text);
		contactList = (ListView) findViewById(R.id.contact_list_view);

		mContactsCountView = (TextView) findViewById(R.id.contacts_count);
		mContactsNotAvailable = (TextView) findViewById(R.id.noContacts_txt);
		contactCounthHolder = (LinearLayout) findViewById(R.id.user_profile_holder);
		dialerIcon = (ImageView) findViewById(R.id.dialer_icon);
		searchText.setSingleLine();

		searchText
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {

					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {

						if (actionId == EditorInfo.IME_ACTION_SEARCH
								|| actionId == EditorInfo.IME_ACTION_DONE) {
							String searchString = searchText.getText()
									.toString();
							setSearchTextString(searchString);
							if (searchString != null
									&& searchString.length() > 0) {
								searchContacts(searchString);
							}
							return true;
						}
						return false;
					}
				});

		searchText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				String newText = s.toString().trim();
				setSearchTextString(newText);
				if (newText != null && newText.length() > 0) {
					searchContacts(newText);
				} else {
					if (mergedContacts != null && mergedContacts.size() > 0) {
						Message msg = new Message();
						msg.obj = mergedContacts;
						ContactHandler.sendMessage(msg);
					}
				}

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		contactList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {

				mCurrentSelectedItemIndex = position;
				ContactsModel contact = (ContactsModel) contactList
						.getAdapter().getItem(position);

				SharedPreferences.Editor edit = new SharedPreferences(Email
						.getAppContext()).edit();
				edit.putInt("currentListIndex", mCurrentSelectedItemIndex);
				edit.putString("currentListRowID",
						String.valueOf(contact.getContact_id()));
				edit.commit();

				/*
				 * if
				 * (ContactsUtilities.isTablet(CognizantEmail.getAppContext()))
				 * {
				 * 
				 * } else { Intent contactDetailIntent = new
				 * Intent(CognizantEmail .getAppContext(),
				 * ContactsDetailActivity.class);
				 * 
				 * contactDetailIntent.putExtra("obj", contact);
				 * startActivity(contactDetailIntent); }
				 */
				populatesearchResult(contact, position, true);

			}
		});

		dialerIcon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});

		/*
		 * ImageButton backButton = (ImageButton)
		 * findViewById(R.id.home_back_button);
		 * 
		 * backButton.setOnClickListener(new View.OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { finish();
		 * 
		 * } });
		 */

		loadContacts();
	}

	/*
	 * @Override public boolean onOptionsItemSelected(MenuItem item) { // TODO
	 * Auto-generated method stub switch (item.getItemId()) { case
	 * android.R.id.home: finish(); break; } return true; }
	 */

	@Override
	public void onConfigurationChanged(Configuration newConfig) {

		super.onConfigurationChanged(newConfig);
		closeKeyBoard();
	}

	public void closeKeyBoard() {
		InputMethodManager imm = (InputMethodManager) mContext
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null && imm.isActive(searchText))
			imm.hideSoftInputFromWindow(searchText.getApplicationWindowToken(),
					0);
	}

	@Override
	protected void onResume() {

		super.onResume();

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

	private void searchContacts(String query) {

		if (mCurrentAccountName == null) {
			mCurrentAccountName = GetAccountDetails();
		}

		if (mCurrentAccountName == null) {
			Toast.makeText(
					mContext,
					"Account informations is are not available. please try again",
					Toast.LENGTH_SHORT).show();
		}

		if (mSearchAsynctask != null) {
			mSearchAsynctask.cancel(true);
		}
		mSearchAsynctask = new GalSearchTask();
		mSearchAsynctask.execute(query);
	}

	String GetAccountDetails() {

		Cursor c = null;
		try {

			c = Email
					.getAppContext()
					.getContentResolver()
					.query(EmEmailContent.Mailbox.CONTENT_URI, null,
							EmEmailContent.Account.DISPLAY_NAME + "=?",
							new String[] { "Contacts" }, null);

			if (c != null && c.getCount() > 0) {
				c.moveToFirst();
				return c.getString(c
						.getColumnIndex(EmEmailContent.Mailbox.ACCOUNT_KEY));
			}

		} catch (Exception e) {

		} finally {
			if (c != null) {
				c.close();
			}
		}
		return null;

	}

	private class ContactsAsyncTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {

		}

		@Override
		protected Void doInBackground(Void... params) {
			mergedContacts = new ArrayList<ContactsModel>();

			Cursor cursor = mContext.getContentResolver().query(
					ContactsConsts.CONTENT_URI_CONTACTS, null, null, null,
					sortOrder + " COLLATE NOCASE");
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
		}

		@Override
		protected void onPostExecute(Void params) {
			super.onPostExecute(params);

			Message msg = new Message();
			msg.obj = mergedContacts;
			ContactHandler.sendMessage(msg);
		}

	}

	private ProgressDialog showDialog() {
		ProgressDialog pDialog = null;
		try {
			if (pDialog == null) {
				pDialog = new ProgressDialog(mContext);
				pDialog.setCancelable(true);
				pDialog.setCanceledOnTouchOutside(false);
			}
			pDialog.setMessage("Searching contact...");
			pDialog.show();
		} catch (Exception exp) {

		}

		return pDialog;

	}

	class GalSearchTask extends
			AsyncTask<String, Void, ArrayList<ContactsModel>> {
		boolean isGalSearch = false;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected ArrayList<ContactsModel> doInBackground(String... params) {

			String receivedExtra = params[0];
			SharedPreferences.Editor edit = new SharedPreferences(
					Email.getAppContext()).edit();
			edit.putInt("searchListIndex", 0);
			edit.commit();

			// Local Search
			ArrayList<ContactsModel> newsearchResults = new ArrayList<ContactsModel>();

			if (receivedExtra == null) {
				return newsearchResults;
			}
			try {

				String where = ContactsConsts.CONTACT_FIRST_NAME
						+ " LIKE (?) or " + ContactsConsts.CONTACT_LAST_NAME
						+ " LIKE (?)";
				String[] args = new String[] { receivedExtra + "%",
						receivedExtra + "%" };

				Cursor local_cursor = Email
						.getAppContext()
						.getContentResolver()
						.query(ContactsConsts.CONTENT_URI_CONTACTS, null,
								where, args, null);

				/*Cursor local_cursor = Email
						.getAppContext()
						.getContentResolver()
						.query(ContactsConsts.CONTENT_URI_CONTACTS,
								null,
								ContactsConsts.CONTACT_FIRST_NAME + " like "
										+ "'" + receivedExtra + "%'" + " or "
										+ ContactsConsts.CONTACT_LAST_NAME
										+ " like " + "'" + receivedExtra + "%'",
								null, null);*/

				if (local_cursor != null && local_cursor.getCount() > 0) {
					newsearchResults = ContactsUtilities
							.PopulateArraylistFromCursor(local_cursor,
									Email.getAppContext());
				}

				local_cursor.close();
			} catch (Exception e) {

			}

			if (receivedExtra.length() > 2) {

				if (isNewSearch) {

					publishProgress();

				}

				try {
					isGalSearch = false;
					long acc = Long.parseLong(mCurrentAccountName);
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

				}
			}
			return newsearchResults;
		}

		@Override
		protected void onProgressUpdate(Void... values) {

			super.onProgressUpdate(values);
			isNewSearch = false;
			Toast.makeText(mContext, "Searching corporate directory..",
					Toast.LENGTH_SHORT).show();
		}

		@Override
		protected void onPostExecute(ArrayList<ContactsModel> newsearchResults) {
			// TODO Auto-generated method stub
			super.onPostExecute(newsearchResults);

			String searchString = getSearchTextString();

			if (searchString != null && searchString.length() > 0) {
				if (newsearchResults != null && newsearchResults.size() > 0) {
					mContactsNotAvailable.setVisibility(View.GONE);
					SearchContacts = newsearchResults;
					mSearchListAdaptor = new ContactsListAdapter(mContext,
							newsearchResults);
					mContactsCountView.setText(SearchContacts.size()
							+ " Contacts");
					contactList.setAdapter(mSearchListAdaptor);

					populatesearchResult(newsearchResults.get(0), 0, false);

				} else {
					mContactsNotAvailable.setVisibility(View.VISIBLE);
					SearchContacts = newsearchResults;
					// mSearchListAdaptor = new
					// ContactsListAdapter(getActivity(),
					// newsearchResults);
					mContactsCountView.setText("0 Contacts");
					contactList.setAdapter(null);

					if (isGalSearch) {
						isGalSearch = false;

						Toast.makeText(mContext, "No contacts available",
								Toast.LENGTH_SHORT).show();
					}

				}
			} else {
				if (mergedContacts != null && mergedContacts.size() > 0) {

					Message msg = new Message();
					msg.obj = mergedContacts;
					ContactHandler.sendMessage(msg);
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
		if (itemclick) {
			Intent contactDetailIntent = new Intent(Email.getAppContext(),
					ContactsDetailActivity.class);
			ContactsUtilities.SELECTED_ID = null;

			contactDetailIntent.putExtras(data);
			startActivity(contactDetailIntent);
		}
	}

	private Handler ContactHandler = new Handler() {
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {

			int time_remaining = new SharedPreferences(Email.getAppContext())
					.getInt("currentListIndex", 0);

			final ArrayList<ContactsModel> newMergedContacts = (ArrayList<ContactsModel>) msg.obj;

			if (time_remaining > newMergedContacts.size()) {
				time_remaining = 0;
			}

			if (newMergedContacts != null && newMergedContacts.size() != 0) {

				listAdapter = new ContactsListAdapter(mContext,
						newMergedContacts);

				SharedPreferences.Editor prefEditor = new SharedPreferences(
						Email.getAppContext()).edit();
				prefEditor.putBoolean("ContactsCheckAvailability", true);
				prefEditor.commit();

				mContactsNotAvailable.setVisibility(View.GONE);

				String sortOrder = new SharedPreferences(Email.getAppContext())
						.getString("prefSortOrder",
								ContactsConsts.CONTACT_FIRST_NAME);

				if (sortOrder.equals("first_name")) {

					Collections.sort(newMergedContacts,
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
					Collections.sort(newMergedContacts,
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
				mContactsCountView.setText(newMergedContacts.size()
						+ " Contacts");

				if (time_remaining != 0) {
					contactList.setAdapter(null);
					contactList.setTag(null);
					listAdapter.setSelectedPosition(time_remaining);
				}

				contactList.setAdapter(listAdapter);
				listAdapter.notifyDataSetChanged();

			} else {
				SharedPreferences.Editor edit = new SharedPreferences(
						Email.getAppContext()).edit();
				edit.putInt("currentListIndex", 0);
				edit.putString("currentListRowID", null);
				edit.commit();

				if (listAdapter != null) {
					contactCounthHolder.setVisibility(View.GONE);
					contactList.setAdapter(null);
					contactList.setTag(null);
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
}
