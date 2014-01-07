package com.cognizant.trumobi.dialer.fragments;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.TruBoxSDK.SharedPreferences;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.cognizant.trubox.contacts.db.ContactsConsts;
import com.cognizant.trubox.contacts.db.ContactsModel;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.contacts.activity.ContactsDetailActivity;
import com.cognizant.trumobi.contacts.utils.ContactsDialog;
import com.cognizant.trumobi.contacts.utils.ContactsUtilities;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.em.provider.EmEmailContent;
import com.cognizant.trumobi.em.provider.EmEmailContent.MailboxColumns;
import com.cognizant.trumobi.exchange.EmEasSyncService;
import com.cognizant.trumobi.exchange.provider.EmGalResult;
import com.cognizant.trumobi.exchange.provider.EmGalResult.GalData;
import com.cognizant.trumobi.log.ContactsLog;

@SuppressLint("ValidFragment")
public class DialerSearchFragment extends SherlockFragment {

	public static final String TAG = "DialerSearchFragment";

	ListView contactSearchList;
	private int mCurrentSelectedItemIndex = -1;

	ArrayList<ContactsModel> serachresults;
	ArrayList<ContactsModel> newSerachresults;
	ProgressDialog searchDialog;
	EditText search;
	FragmentManager fragmentManager;
	String receivedExtra = "null";
	search searchtask;
	SearchAdaptor searchListAdaptor;
	ArrayList<GalData> searchItems = new ArrayList<EmGalResult.GalData>();

	@Override
	public SherlockFragmentActivity getSherlockActivity() {
		return super.getSherlockActivity();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(
				true);
		Bundle args = this.getArguments();
		receivedExtra = args.getString("search_query");
		fragmentManager = getSherlockActivity().getSupportFragmentManager();
		searchItems.clear();

		if (!ContactsUtilities.isTablet(Email.getAppContext())) {
			setRetainInstance(true);
		}
		searchContacts();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		outState.putInt("searchListIndex", mCurrentSelectedItemIndex);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);
	}

	private void searchContacts() {
		ContactsLog.d("SearchActivity", "Search clicked ***" + receivedExtra);
		if (searchtask != null) {
			searchtask.cancel(true);
		}
		searchtask = new search();
		searchtask.execute();
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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View view = inflater.inflate(R.layout.dialer_list, container, false);
		contactSearchList = (ListView) view.findViewById(android.R.id.list);
		contactSearchList.setOnItemClickListener(searchClickListener);
		return view;
	}

	OnItemClickListener searchClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> a, View v, int position, long id) {
			populatesearchResult(position, true);

		}
	};

	class search extends AsyncTask<Void, Void, ArrayList<GalData>> {
		// ArrayList<GalData> newsearchResults;

		@Override
		// protected EmGalResult doInBackground(Void... params) {
		protected ArrayList<GalData> doInBackground(Void... params) {

			SharedPreferences.Editor edit = new SharedPreferences(
					Email.getAppContext()).edit();
			edit.putInt("searchListIndex", 0);
			edit.commit();

			ArrayList<GalData> newsearchResults = new ArrayList<EmGalResult.GalData>();

			// Local Search
			// newsearchResults = new ArrayList<EmGalResult.GalData>();

			try {
				/*System.out.println("query: " + "\"" + "'%" + receivedExtra
						+ "%'" + "\"");*/
				Cursor local_cursor = Email
						.getAppContext()
						.getContentResolver()
						.query(ContactsConsts.CONTENT_URI_CONTACTS,
								null,
								ContactsConsts.CONTACT_FIRST_NAME + " like "
										+ "'" + receivedExtra + "%'" + " or "
										+ ContactsConsts.CONTACT_LAST_NAME
										+ " like " + "'" + receivedExtra + "%'",
								null, null);
				/*System.out.println("LOCAL SEARCH VALUE: "
						+ local_cursor.getCount());*/
				if (local_cursor != null && local_cursor.getCount() > 0) {
					local_cursor.moveToFirst();
					for (int j = 0; j < local_cursor.getCount(); j++) {
						/*System.out.println("LOCAL SEARCH VALUE: "
								+ local_cursor.getCount());*/
						GalData contact = new GalData();

						String first_name = local_cursor
								.getString(local_cursor
										.getColumnIndex(ContactsConsts.CONTACT_FIRST_NAME));
						if (first_name != null && !first_name.isEmpty())
							contact.put(GalData.FIRST_NAME, first_name);

						String last_name = local_cursor
								.getString(local_cursor
										.getColumnIndex(ContactsConsts.CONTACT_LAST_NAME));
						if (last_name != null && !last_name.isEmpty())
							contact.put(GalData.LAST_NAME, last_name);

						String display_name = first_name + " " + last_name;
						if (display_name.length() > 1) {
							contact.put(GalData.DISPLAY_NAME, display_name);
						}

						String title = local_cursor.getString(local_cursor
								.getColumnIndex(ContactsConsts.CONTACT_TITLE));
						if (title != null && !title.isEmpty())
							contact.put(GalData.TITLE, title);

						String company = local_cursor
								.getString(local_cursor
										.getColumnIndex(ContactsConsts.CONTACT_COMPANY));
						if (company != null && !company.isEmpty())
							contact.put(GalData.COMPANY, company);

						String office = local_cursor.getString(local_cursor
								.getColumnIndex(ContactsConsts.CONTACT_OFFICE));
						if (office != null && !office.isEmpty())
							contact.put(GalData.COMPANY, office);

						ContentValues emailNumbers = new ContentValues();
						String email_details = local_cursor
								.getString(local_cursor
										.getColumnIndex(ContactsConsts.CONTACT_EMAIL));
						//System.out.println("Email details: " + email_details);
						if (email_details != null && !email_details.isEmpty()) {
							String[] email_array = null;
							email_array = email_details.split(":");
							String[] email_type_number = null;
							/*System.out.println("email_array.length: "
									+ email_array.length);*/

							for (int i = 0; i < email_array.length; i++) {
								/*System.out.println("email_array.length: "
										+ email_array[i]);*/
								if (email_array[i] != null
										&& !email_array[i].isEmpty()) {
									email_type_number = email_array[i]
											.split("=");
									emailNumbers.put(email_type_number[0],
											email_type_number[1]);

								}
							}
							if (emailNumbers.size() > 0) {
								if (emailNumbers.containsKey("Email_Adress")) {
									String Email_Adress = emailNumbers.get(
											"Email_Adress").toString();
									if (Email_Adress != null
											&& !Email_Adress.isEmpty()) {
										contact.put(GalData.EMAIL_ADDRESS,
												Email_Adress);
										contact.emailAddress = Email_Adress;
									}
								}

							}
						}

						ContentValues phoneNumbers = new ContentValues();
						String phone_details = local_cursor
								.getString(local_cursor
										.getColumnIndex(ContactsConsts.CONTACT_PHONE));
						//System.out.println("Email details: " + phone_details);
						if (phone_details != null && !phone_details.isEmpty()) {
							String[] phone_array = null;
							phone_array = phone_details.split(":");
							String[] phone_type_number = null;
							/*System.out.println("phone_array.length: "
									+ phone_array.length);*/

							for (int i = 0; i < phone_array.length; i++) {
								/*System.out.println("email_array.length: "
										+ phone_array[i]);*/
								if (phone_array[i] != null
										&& !phone_array[i].isEmpty()) {
									phone_type_number = phone_array[i]
											.split("=");
									phoneNumbers.put(phone_type_number[0],
											phone_type_number[1]);

								}
							}

							if (phoneNumbers.size() > 0) {
								if (phoneNumbers.containsKey("MOBILE")) {
									String MOBILE = phoneNumbers.get("MOBILE")
											.toString();
									if (MOBILE != null && !MOBILE.isEmpty()) {
										contact.put(GalData.MOBILE_PHONE,
												MOBILE);
									}
								}

								if (phoneNumbers.containsKey("WORK")) {
									String WORK = phoneNumbers.get("WORK")
											.toString();
									if (WORK != null && !WORK.isEmpty()) {
										contact.put(GalData.WORK_PHONE, WORK);
									}
								}

								if (phoneNumbers.containsKey("HOME")) {
									String HOME = phoneNumbers.get("HOME")
											.toString();
									if (HOME != null && !HOME.isEmpty()) {
										contact.put(GalData.HOME_PHONE, HOME);
									}
								}

							}
						}

						// case EmailTags.GAL_ALIAS:
						// galData.put(GalData.ALIAS, getValue());
						// break;
						//

						newsearchResults.add(contact);
						local_cursor.moveToNext();
					}

				}
				local_cursor.close();
			} catch (Exception e) {
				Log.e("ContactsSearchFragment", "local_searh: " + e.toString());
			}

			/*
			 * try{ // LDAP Search
			 * 
			 * Cursor c = Email.getAppContext().getContentResolver()
			 * .query(EmEmailContent.Mailbox.CONTENT_URI, null,
			 * EmEmailContent.Account.DISPLAY_NAME + "=?", new String[] {
			 * "Contacts" }, null); ContactsLog.d("SearchActivity",
			 * "Search fragment clicked ***" + c.getColumnCount() +
			 * " row count**" + c.getCount()); if (c != null && c.getCount() >
			 * 0) { c.moveToFirst();
			 * 
			 * String Accname = c.getString(c
			 * .getColumnIndex(EmEmailContent.Mailbox.ACCOUNT_KEY)); EmGalResult
			 * galResult= null; try{ galResult =
			 * EmEasSyncService.searchGal(Email.getAppContext(),
			 * Long.parseLong(Accname), receivedExtra);
			 * }catch(NoClassDefFoundError excep){
			 * 
			 * }
			 * 
			 * 
			 * return galResult; } if (c != null) { c.close(); }
			 * }catch(Exception e){
			 * 
			 * } return null;
			 */

			if (receivedExtra.length() <= 2) {
				return newsearchResults;
			}

			ContactsLog.d(
					"SearchActivity",
					"Ldap search initiated quesy string count "
							+ receivedExtra.length());

			Cursor c = null;
			try {
				// LDAP Search

				c = Email
						.getAppContext()
						.getContentResolver()
						.query(EmEmailContent.Mailbox.CONTENT_URI, null,
								EmEmailContent.Account.DISPLAY_NAME + "=?",
								new String[] { "Contacts" }, null);
				ContactsLog.d("SearchActivity", "Search fragment clicked ***"
						+ c.getColumnCount() + " row count**" + c.getCount());
				if (c != null && c.getCount() > 0) {
					c.moveToFirst();

					String Accname = c.getString(c
							.getColumnIndex(MailboxColumns.ACCOUNT_KEY));
					try {
						EmGalResult galResult = EmEasSyncService.searchGal(
								Email.getAppContext(), Long.parseLong(Accname),
								receivedExtra);

						if (galResult != null) {
							newsearchResults.addAll(galResult.galData);
						}

					} catch (NoClassDefFoundError excep) {

					}
					// return galResult;
				}

			} catch (Exception e) {

			} finally {
				if (c != null) {
					c.close();
				}
			}
			return newsearchResults;

		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			Message m = new Message();
			m.arg1 = 1;

			DialogHandler.sendMessage(m);
		}

		@Override
		protected void onPostExecute(ArrayList<GalData> newsearchResults) {
			// TODO Auto-generated method stub
			super.onPostExecute(newsearchResults);

			if (searchDialog != null) {
				try {
					searchDialog.dismiss();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

			Message m = new Message();
			m.arg1 = 2;

			DialogHandler.sendMessage(m);

			/*
			 * if (galResult != null) {
			 * newsearchResults.addAll(galResult.galData); }
			 */

			if (newsearchResults != null && newsearchResults.size() > 0) {
				searchItems = newsearchResults;
				searchListAdaptor = new SearchAdaptor(Email.getAppContext(),
						newsearchResults);
				contactSearchList.setAdapter(searchListAdaptor);
				populatesearchResult(0, false);

			} else {

				Message noinfo = new Message();
				noinfo.arg1 = 3;

				DialogHandler.sendMessage(noinfo);

			}

		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		setRetainInstance(true);
		if (searchListAdaptor != null) {
			if (searchItems.size() > 0) {
				contactSearchList.setAdapter(searchListAdaptor);
				searchListAdaptor.notifyDataSetChanged();
				final int selected_index = new SharedPreferences(
						Email.getAppContext()).getInt("searchListIndex", 0);
				populatesearchResult(selected_index, false);
			}
		}
	}

	public void populatesearchResult(int position, boolean itemclick) {
		GalData contact = searchItems.get(position);
		mCurrentSelectedItemIndex = position;
		SharedPreferences.Editor edit = new SharedPreferences(
				Email.getAppContext()).edit();
		edit.putInt("searchListIndex", mCurrentSelectedItemIndex);
		edit.commit();

		Bundle data = new Bundle();

		data.putSerializable("searchObj", contact);

		if (itemclick) {
			Intent contactDetailIntent = new Intent(Email.getAppContext(),
					ContactsDetailActivity.class);
			ContactsUtilities.SELECTED_ID = null;

			contactDetailIntent.putExtras(data);
			startActivity(contactDetailIntent);
		}

	}

	class SearchAdaptor extends BaseAdapter {

		ArrayList<GalData> SearchList;
		Context mContext;
		private LayoutInflater mInflater;

		public SearchAdaptor(Context c, ArrayList<GalData> list) {
			// TODO Auto-generated constructor stub
			this.mContext = c;
			this.SearchList = list;
			mInflater = LayoutInflater.from(c);

		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return SearchList.size();
		}

		@Override
		public GalData getItem(int position) {
			// TODO Auto-generated method stub
			return SearchList.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder;

			String firstName = SearchList.get(position).get(GalData.FIRST_NAME);
			String lastName = SearchList.get(position).get(GalData.LAST_NAME);
			String displayName = SearchList.get(position).get(
					GalData.DISPLAY_NAME);

			ContactsLog.d("SearchView", "Firstname" + firstName);
			ContactsLog.d("SearchView", "LastName" + lastName);
			ContactsLog.d("SearchView", "displayName" + displayName);

			if ((firstName == null && lastName == null) && displayName != null) {

				firstName = displayName;

			}

			if (convertView == null) {
				convertView = mInflater.inflate(
						R.layout.contacts_search_contact_list, null);
				holder = new ViewHolder();
				holder.PrimaryName = (TextView) convertView
						.findViewById(R.id.contact_primary_name);
				holder.SecondaryName = (TextView) convertView
						.findViewById(R.id.contact_sec_name);

				holder.contactImage = (ImageView) convertView
						.findViewById(R.id.contact_image);

				convertView.setTag(holder);

				if (firstName != null) {
					holder.PrimaryName.setText(firstName);
				}
				if (lastName != null) {
					holder.SecondaryName.setText(lastName);
				}

				holder.contactImage.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {

						GalData contact = searchItems.get(position);
						new ContactsDialog(getSherlockActivity(),
								(GalData) contact);

					}
				});

				return convertView;

			} else {
				holder = (ViewHolder) convertView.getTag();

				if (firstName != null) {

					TextView first = (TextView) holder.PrimaryName;
					first.setText(firstName);
				}
				if (lastName != null) {
					TextView sec = (TextView) holder.SecondaryName;
					sec.setText(lastName);
				}

				holder.contactImage.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View view) {

						GalData contact = searchItems.get(position);
						new ContactsDialog(getSherlockActivity(),
								(GalData) contact);

					}
				});

				return convertView;

			}
			/*
			 * String firstName =
			 * SearchList.get(position).get(GalData.FIRST_NAME); String lastName
			 * = SearchList.get(position).get(GalData.LAST_NAME);
			 * ContactsLog.d("SearchView", "Firstname" +
			 * SearchList.get(position).get(GalData.FIRST_NAME));
			 * ContactsLog.d("SearchView", "LastName" +
			 * SearchList.get(position).get(GalData.LAST_NAME)); if (firstName
			 * != null) { holder.PrimaryName.setText(firstName); } if (lastName
			 * != null) { holder.SecondaryName.setText(lastName); }
			 * 
			 * 
			 * contactImage = (ImageView)
			 * convertView.findViewById(R.id.contact_image);
			 * contactImage.setOnClickListener(new OnClickListener() {
			 * 
			 * @Override public void onClick(View view) {
			 * 
			 * GalData contact = searchItems.get(position); new
			 * ContactsDialog(getSherlockActivity(), (GalData) contact);
			 * 
			 * } });
			 * 
			 * return convertView;
			 */
		}
	}

	static class ViewHolder {
		TextView PrimaryName;
		TextView SecondaryName;
		ImageView contactImage;

	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		if (searchDialog != null) 
		{
			try{
			searchDialog.dismiss();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		SharedPreferences.Editor edit = new SharedPreferences(Email.getAppContext()).edit();
		edit.putInt("searchListIndex", 0);
		edit.commit();
		super.onDestroy();
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	Handler DialogHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {

			if (msg.arg1 == 1) {
				showDialog();

			} else if (msg.arg1 == 2) {
					
					if (searchDialog != null) 
					{
						try{
						searchDialog.dismiss();
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
						
			} else {
				new MyDialog().show(getActivity().getSupportFragmentManager(), "dialog");

			}
		};
	};

	public class MyDialog extends DialogFragment {

		@SuppressLint("ValidFragment")
		public MyDialog() {
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setRetainInstance(true);
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return new AlertDialog.Builder(getActivity())
				.setMessage("No Result Found")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							// finish();
							FragmentManager fragmentManager = getSherlockActivity()
									.getSupportFragmentManager();
							FragmentTransaction fragmentTransaction = fragmentManager
									.beginTransaction();

							/*fragmentTransaction.remove(fragmentManager
									.findFragmentById(android.R.id.content));*/
							fragmentTransaction.commit();
						}
					}).create();
		}

		/**
		 * workaround for issue #17423
		 */
		@Override
		public void onDestroyView() {
			if (getDialog() != null && getRetainInstance())
				getDialog().setOnDismissListener(null);

			super.onDestroyView();
		}
	}
}
