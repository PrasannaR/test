package com.cognizant.trumobi.contacts.activity;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.cognizant.trubox.contacts.db.ContactsModel;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseSherlockFragmentBaseActivity;
import com.cognizant.trumobi.contacts.utils.ContactsUtilities;

public class ContactsSearchActivity extends TruMobiBaseSherlockFragmentBaseActivity {

	ListView contactSearchList;
	// Contacts mycontacts;
	ArrayList<ContactsModel> serachresults;
	ProgressDialog searchDialog;
	// EditText search;
	FragmentManager fragmentManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts_search_contact_view);
		searchDialog = new ProgressDialog(this);
		searchDialog.setCancelable(true);
		searchDialog.setCanceledOnTouchOutside(false);
		searchDialog.setMessage("Searching contact...");
		fragmentManager = getSupportFragmentManager();
		contactSearchList = (ListView) findViewById(R.id.search_view_contacts_list);

		contactSearchList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> a, View v, int position,
					long id) {
				Object obj = contactSearchList.getItemAtPosition(position);
				ContactsModel contact = (ContactsModel) obj;

				Bundle data = new Bundle();

				if (ContactsUtilities.isTablet(getApplicationContext())) {

					ContactsDetailFragment ctcDetailFragment = new ContactsDetailFragment();
					data.putSerializable("obj", contact);
					ctcDetailFragment.setArguments(data);

					FragmentTransaction transaction = fragmentManager
							.beginTransaction();
					getSupportFragmentManager().popBackStack();
					transaction.replace(R.id.displayDetail, ctcDetailFragment,
							ContactsDetailFragment.TAG);
					// transaction.addToBackStack(null);
					transaction.commit();

				} else {

					Intent contactDetailIntent = new Intent(
							getApplicationContext(),
							ContactsDetailActivity.class);
					contactDetailIntent.putExtra("obj", contact);
					startActivity(contactDetailIntent);
				}

				/*
				 * Intent contactDetailIntent = new
				 * Intent(ContactsSearchActivity
				 * .this,ContactsDetailActivity.class);
				 * contactDetailIntent.putExtra("obj", contact);
				 * startActivity(contactDetailIntent);
				 */

			}
		});

		// mycontacts = (Contacts) getApplication();
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		// actionBar.setIcon(R.drawable.ic_ab_search_holo_dark);

		LayoutInflater inflator = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflator.inflate(R.layout.contacts_search_layout, null);
		actionBar.setCustomView(v);
		/*
		 * search = (EditText) v.findViewById(R.id.search);
		 * search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
		 * search.requestFocus();
		 * search.setOnEditorActionListener(searchListener);
		 */
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

	}

	class SearchAdaptor extends BaseAdapter {

		ArrayList<ContactsModel> SearchList;
		Context mContext;
		private LayoutInflater mInflater;

		public SearchAdaptor(Context c, ArrayList<ContactsModel> list) {
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
		public ContactsModel getItem(int position) {
			// TODO Auto-generated method stub
			return SearchList.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(
						R.layout.contacts_search_contact_list, null);
				holder = new ViewHolder();
				holder.PrimaryName = (TextView) convertView
						.findViewById(R.id.contact_primary_name);
				holder.SecondaryName = (TextView) convertView
						.findViewById(R.id.contact_sec_name);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.PrimaryName.setText(serachresults.get(position)
					.getcontacts_first_name());
			holder.SecondaryName.setText(serachresults.get(position)
					.getcontacts_last_name());

			return convertView;
		}

	}

	static class ViewHolder {
		TextView PrimaryName;
		TextView SecondaryName;

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		default:
			break;
		}
		return true;
	}

	TextView.OnEditorActionListener searchListener = new TextView.OnEditorActionListener() {

		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_SEARCH) {

				Log.d("SearchActivity", "Search clicked");
				Message m = new Message();
				m.arg1 = 1;
				// getDialogHandler().sendMessage(m);
				String text = v.getText().toString();
				Log.d("SearchActivity", "Search clicked ***" + text);
				// EasSynctask task = new
				// EasSynctask(ContactsSearchActivity.this,
				// Contacts.STATUS_SEARCH_GAL, text);
				//
				// task.execute(mycontacts.getActiveSyncManager());

			}
			return true;

		}
	};

	Handler DialogHandler = new Handler() {

		public void handleMessage(android.os.Message msg) {

			if (msg.arg1 == 1) {
				searchDialog.show();
			} else {
				searchDialog.cancel();
			}
		};
	};

	public void setDialogHandler(Handler dialogHandler) {
		DialogHandler = dialogHandler;
	}

	public void taskComplete(int Task, boolean taskStatus, int statusCode,
			int requestStatus, String errorString) {
		Message m = new Message();
		m.arg1 = 2;
		// getDialogHandler().sendMessage(m);
		if (taskStatus) {

			// serachresults = mycontacts.getActiveSyncManager().getResults();

			Log.d("ContactsSearchActivity",
					"serachresults" + serachresults.size());
			if (serachresults.size() != 0) {
				contactSearchList.setAdapter(new SearchAdaptor(this,
						serachresults));
			} else {
				AlertDialog alertDialog = new AlertDialog.Builder(
						ContactsSearchActivity.this).create();
				// alertDialog.setTitle("Unable to load certificate");
				alertDialog.setMessage("No Result Found");
				// alertDialog.setIcon(R.drawable.tick);
				alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								finish();
							}
						});
				alertDialog.show();
			}

		}

	};

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		finish();
	}
}
