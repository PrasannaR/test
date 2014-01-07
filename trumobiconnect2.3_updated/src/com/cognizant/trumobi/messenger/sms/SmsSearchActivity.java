package com.cognizant.trumobi.messenger.sms;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cognizant.trumobi.messenger.adapter.SmsCustomAdapter;
import com.cognizant.trumobi.messenger.adapter.SmsCustomMenuAdapter;
import com.cognizant.trumobi.messenger.adapter.SmsSearchListAdapter;
import com.cognizant.trumobi.messenger.db.SmsHistoryTable;
import com.cognizant.trumobi.messenger.db.SmsIndividualTable;
import com.cognizant.trumobi.PersonaLauncher;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.messenger.model.SmsDetailsModel;
import com.cognizant.trumobi.messenger.model.SmsSearchListModel;
import com.cognizant.trumobi.commonabstractclass.TruMobBaseEditText;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseActivity;
import com.google.common.base.Strings;

public class SmsSearchActivity extends TruMobiBaseActivity {

	ListView searchList;
	RelativeLayout searchParent;
	ArrayList<SmsSearchListModel> searchArrayList;
	ArrayList<SmsSearchListModel> searchArrayList1;
	ArrayList<SmsDetailsModel> mToastListArray;
	ArrayList<SmsDetailsModel> mToastLocalArray;
	SmsSearchListAdapter searchListAdapter;
	Context context;
	TruMobBaseEditText searchText;
	ImageView searchImage;
	// ImageView corporateIcon;
	String searchString;
	String contactName;
	String contactPhone;
	ImageView messengerIcon;

	ArrayList<SmsDetailsModel> mLocallistArray;
	ArrayList<SmsDetailsModel> mQueryListArray;
	SmsCustomAdapter mListAdapter;
	Context mContext;
	ListView mListDsp;
	ListView mListMenu;
	ImageView mMenu;
	ImageView corporateIcon;
	LinearLayout noConversation;
	String mName;
	String mNumber;
	int menuCount = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// context = this;
		mContext = this;
		setContentView(R.layout.sms_search_list);

		searchText = (TruMobBaseEditText) findViewById(R.id.search_text);
		searchImage = (ImageView) findViewById(R.id.icon_search);
		messengerIcon = (ImageView) findViewById(R.id.message_icon);
		corporateIcon = (ImageView) findViewById(R.id.corporate_icon);
		searchParent = (RelativeLayout) findViewById(R.id.search_relative);
		noConversation = (LinearLayout) findViewById(R.id.sms_no_conversation_search);
		searchList = (ListView) findViewById(R.id.search_list_view);
		searchText.setSingleLine();
		searchList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				contactName = searchArrayList.get(arg2).getName();
				contactPhone = searchArrayList.get(arg2).getPhone();
				if(! Strings.isNullOrEmpty(searchArrayList.get(arg2).getMultiContact())){
					String name = queryInIndividualTable(searchArrayList.get(arg2).getMultiContact());
					startSMSActivity(name, searchArrayList.get(arg2).getMultiContact());
				}
				else
					startSMSActivity(contactName, contactPhone);
			}
		});

		searchText
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {

					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						// TODO Auto-generated method stub

						if (actionId == EditorInfo.IME_ACTION_SEARCH
								|| actionId == EditorInfo.IME_ACTION_DONE) {
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(
									searchText.getWindowToken(),
									InputMethodManager.RESULT_UNCHANGED_SHOWN);
							String searchString = searchText.getText()
									.toString();
							if (!Strings.isNullOrEmpty(searchString))
								searchSMS(searchString);
							else {
								searchList.setVisibility(View.GONE);
								Toast.makeText(getApplicationContext(),
										"Please enter a valid text",
										Toast.LENGTH_SHORT).show();
							}
							return true;
						}
						return false;
					}
				});

		searchText.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					menuCount = 0;
					mListMenu.setVisibility(View.GONE);
					break;
				}
				return false;
			}
		});

		searchParent.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					menuCount = 0;
					mListMenu.setVisibility(View.GONE);
					break;
				}
				return false;
			}
		});

		messengerIcon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mListMenu.setVisibility(View.GONE);
				Intent listActivity = new Intent(mContext, SmsListdisplay.class);
				listActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				listActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(listActivity);
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

		ImageView lNewMessage = (ImageView) findViewById(R.id.icon_new_messeage);
		corporateIcon = (ImageView) findViewById(R.id.corporate_icon);
		lNewMessage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startSMS("", "");

			}
		});
		ImageView search = (ImageView) findViewById(R.id.icon_search);
		search.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startSearch();

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

		mListDsp = (ListView) findViewById(R.id.list_search_inbox);
		mListDsp.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				mName = mQueryListArray.get(arg2).getName();
				mNumber = mQueryListArray.get(arg2).getmContactAddress();

				startSMS(mName, mNumber);
			}
		});
		mListDsp.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					menuCount = 0;
					mListMenu.setVisibility(View.GONE);
					break;
				}
				return false;
			}
		});
		menuItemSetting();
		mMenu = (ImageView) findViewById(R.id.icon_overflow);
		mMenu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				// TODO Auto-generated method stub
				if (View.GONE == mListMenu.getVisibility()) {
					mListMenu.setVisibility(View.VISIBLE);
				} else {
					mListMenu.setVisibility(View.GONE);
				}
			}
		});
		UIupdate();
		mListMenu.setVisibility(View.GONE);
		searchList.setVisibility(View.GONE);
		searchImage.setVisibility(View.GONE);
		registerForContextMenu(mListDsp);
	}

	protected String queryInIndividualTable(String multiContact) {
		// TODO Auto-generated method stub
		String name = null;
		Cursor threadCursor = mContext.getContentResolver().query(
				SmsIndividualTable.CONTENT_URI_INDIVIDUAL, null,
				SmsIndividualTable.PHONE_NUMBER + "=?",
				new String[] { multiContact }, null);
		if(threadCursor != null){
			if (threadCursor.moveToFirst()) {
				name = threadCursor.getString(threadCursor.getColumnIndex(SmsIndividualTable.FIRST_NAME));
			}
		}
		threadCursor.close();
		return name;
	}

	@Override
	public void onBackPressed() {
		// do something on back.
		if (View.GONE == searchList.getVisibility())
			this.finish();
		else
			searchList.setVisibility(View.GONE);
		return;
	}

	protected void searchSMS(String searchText) {
		// TODO Auto-generated method stub
		if (searchText.length() > 0) {
			searchList.setVisibility(View.VISIBLE);
			searchArrayList = querySMS(searchText);
		} else
			searchList.setVisibility(View.GONE);

		if (!searchArrayList.isEmpty()) {
			// searchList.setVisibility(View.VISIBLE);
			searchList.invalidateViews();
			searchListAdapter = new SmsSearchListAdapter(mContext,
					searchArrayList);
			searchListAdapter.notifyDataSetChanged();
			searchList.setAdapter(searchListAdapter);
		} else if (searchArrayList.isEmpty() && searchText.length() == 0)
			searchList.setVisibility(View.GONE);
		else {
			searchList.setVisibility(View.GONE);
			Toast.makeText(getApplicationContext(), "No Data Found",
					Toast.LENGTH_SHORT).show();
		}
	}

	// //Search Activity
	public void startSMSActivity(String name, String phone) {
		Intent intsms = new Intent(this, SmsMain.class);
		intsms.putExtra("name", name);
		intsms.putExtra("number", phone);
		startActivity(intsms);
	}

	private ArrayList<SmsSearchListModel> querySMS(CharSequence s) {
		// TODO Auto-generated method stub
		ArrayList<SmsSearchListModel> list = new ArrayList<SmsSearchListModel>();
		;
		String[] projection = { SmsHistoryTable.FIRST_NAME,
				SmsHistoryTable.PHONE_NUMBER, SmsHistoryTable.MESSAGE, SmsHistoryTable.MULTI_CONTACT };
		if (s.length() >= 1) {
			Cursor search = getContentResolver().query(
					SmsHistoryTable.CONTENT_URI_HISTORY, projection,
					SmsIndividualTable.MESSAGE + " LIKE ?",
					new String[] {"%" + (String) s + "%" }, null);

		if (search != null) {
				if (search.moveToFirst()) {
					do {
						String name = search.getString(search
								.getColumnIndex(SmsHistoryTable.FIRST_NAME));
						String phoneNumber = search.getString(search
								.getColumnIndex(SmsHistoryTable.PHONE_NUMBER));
						String message = search.getString(search
								.getColumnIndex(SmsHistoryTable.MESSAGE));
						String multiContact = search.getString(search
								.getColumnIndex(SmsHistoryTable.MULTI_CONTACT));
						list.add(new SmsSearchListModel(name, phoneNumber,
								message, multiContact));
					} while (search.moveToNext());
				}
			}
			search.close();
			return list;
		} else
			return list;
	}// /Search Activity

	private void menuItemSetting() {
		// TODO Auto-generated method stub
		mListMenu = (ListView) findViewById(R.id.list_search_menu);
		ArrayList<String> st = new ArrayList<String>();
		st.add("Settings");
		st.add("Delete thread");

		SmsCustomMenuAdapter adp = new SmsCustomMenuAdapter(this, st, 0);
		mListMenu.setDivider(null);
		mListMenu.setDividerHeight(0);
		mListMenu.setAdapter(adp);
		mListMenu.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				if (0 == arg2) {
					Intent setting = new Intent(SmsSearchActivity.this,
							SmsSettingsPreferenceActivity.class);
					startActivity(setting);
				} else if (1 == arg2) {
					mToastLocalArray = new ArrayList<SmsDetailsModel>();
					mToastListArray = new ArrayList<SmsDetailsModel>();
					mToastListArray = querydata(mToastLocalArray);
					if (!mToastListArray.isEmpty()) {
						AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
								mContext);
						// set title
						alertDialogBuilder.setTitle("Delete all");
						// set dialog message
						alertDialogBuilder
								.setMessage("Threads will be deleted")
								.setCancelable(false)
								.setPositiveButton("Yes",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												// if this button is clicked,
												// close
												// current activity
												ContentResolver cr = getContentResolver();
												cr.delete(
														SmsHistoryTable.CONTENT_URI_HISTORY,
														null, null);
												cr.delete(
														SmsIndividualTable.CONTENT_URI_INDIVIDUAL,
														null, null);
												UIupdate();
											}
										})
								.setNegativeButton("No",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												// if this button is clicked,
												// just close
												// the dialog box and do nothing
												dialog.cancel();
											}
										});

						// create alert dialog
						AlertDialog alertDialog = alertDialogBuilder.create();

						// show it
						alertDialog.show();
					} else
						Toast.makeText(getApplicationContext(),
								"No threads to delete", Toast.LENGTH_SHORT)
								.show();
				}
				mListMenu.setVisibility(View.GONE);

			}
		});
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		refreshUI();
	}

	public void startSMS(String name, String number) {
		mListMenu.setVisibility(View.GONE);
		String[] projection = { SmsIndividualTable.PHONE_NUMBER,
				SmsIndividualTable.UNREAD_COUNT };

		Cursor resetUnreadCountCursor = getContentResolver().query(
				SmsIndividualTable.CONTENT_URI_INDIVIDUAL, projection,
				SmsIndividualTable.PHONE_NUMBER + "=?",
				new String[] { number }, null);
		if (resetUnreadCountCursor != null) {
			if (resetUnreadCountCursor.moveToFirst()) {
				do {

					int unreadCount = resetUnreadCountCursor
							.getInt(resetUnreadCountCursor
									.getColumnIndex(SmsIndividualTable.UNREAD_COUNT));
					System.out.println("UUUUUUUUUUUUUread Count : "
							+ unreadCount);
					if (unreadCount != 0) {
						ContentValues unreadUpdate = new ContentValues();
						unreadUpdate.put("unread_count", 0);
						getContentResolver().update(
								SmsIndividualTable.CONTENT_URI_INDIVIDUAL,
								unreadUpdate,
								SmsIndividualTable.PHONE_NUMBER + "=?",
								new String[] { number });
					}
				} while (resetUnreadCountCursor.moveToNext());
			}
			resetUnreadCountCursor.close();
		}
		Intent intsms = new Intent(this, SmsMain.class);
		intsms.putExtra("name", name);
		intsms.putExtra("number", number);
		startActivity(intsms);
	}

	public void startSearch() {
		Intent searchIntent = new Intent(this, SmsSearchActivity.class);
		// searchIntent.putExtra("char", num);
		startActivity(searchIntent);
	}

	private void arrayGetItems() {
		// TODO Auto-generated method stub
		mLocallistArray = new ArrayList<SmsDetailsModel>();
		mQueryListArray = new ArrayList<SmsDetailsModel>();
		mQueryListArray = querydata(mLocallistArray);
	}

	public void UIupdate() {
		arrayGetItems();
		mListAdapter = new SmsCustomAdapter(mContext, mQueryListArray);
		mListDsp.setAdapter(mListAdapter);

	}

	public void refreshUI() {
		arrayGetItems();
		mListAdapter = new SmsCustomAdapter(mContext, mQueryListArray);
		mListDsp.setAdapter(mListAdapter);

	}

	private ArrayList<SmsDetailsModel> querydata(ArrayList<SmsDetailsModel> ar) {
		// TODO Auto-generated method stub
		ArrayList<SmsDetailsModel> result = new ArrayList<SmsDetailsModel>();
		String[] projection = { SmsIndividualTable.FIRST_NAME,
				SmsIndividualTable.PHONE_NUMBER, SmsIndividualTable.MESSAGE,
				SmsIndividualTable.TIME, SmsIndividualTable.INDIVIDUAL_ID,
				SmsIndividualTable.IMAGE, SmsIndividualTable.SENT_STATUS,
				SmsIndividualTable.DRAFT_MSG, SmsIndividualTable.UNREAD_COUNT };

		Cursor indicur = getContentResolver().query(
				SmsIndividualTable.CONTENT_URI_INDIVIDUAL, projection, null,
				null, SmsIndividualTable.TIME + " DESC");

		if (indicur != null) {
			if (indicur.moveToFirst()) {
				do {

					String id = indicur.getString(indicur
							.getColumnIndex(SmsIndividualTable.INDIVIDUAL_ID));
					String name = indicur.getString(indicur
							.getColumnIndex(SmsIndividualTable.FIRST_NAME));
					String phoneNumber = indicur.getString(indicur
							.getColumnIndex(SmsIndividualTable.PHONE_NUMBER));
					String message = indicur.getString(indicur
							.getColumnIndex(SmsIndividualTable.MESSAGE));
					long time = indicur.getLong(indicur
							.getColumnIndex(SmsIndividualTable.TIME));
					byte[] image = indicur.getBlob(indicur
							.getColumnIndex(SmsIndividualTable.IMAGE));
					String sentStatus = indicur.getString(indicur
							.getColumnIndex(SmsIndividualTable.SENT_STATUS));
					String draftString = indicur.getString(indicur
							.getColumnIndex(SmsIndividualTable.DRAFT_MSG));
					int unreadCount = indicur.getInt(indicur
							.getColumnIndex(SmsIndividualTable.UNREAD_COUNT));
					result.add(new SmsDetailsModel(time, phoneNumber, name,
							message, id, image, sentStatus, draftString,
							unreadCount));

				} while (indicur.moveToNext());
			}
		}
		indicur.close();
		if (result.isEmpty())
			noConversation.setVisibility(View.VISIBLE);
		else
			noConversation.setVisibility(View.GONE);
		return result;
	}

	private byte[] bitmaptoByteArray(Bitmap bmp) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
		byte[] byteArray = stream.toByteArray();
		return byteArray;
	}

	private byte[] loadContactPhoto(byte[] imageId) {
		// TODO Auto-generated method stub
		// Bitmap defaultPhoto = BitmapFactory.decodeResource(getResources(),
		// R.drawable.ic_contact_picture);
		if (imageId != null) {
			return imageId;
		} else {
			Bitmap defaultPhoto = BitmapFactory.decodeResource(getResources(),
					R.drawable.sms_ic_contact_picture);
			byte[] defaultId = bitmaptoByteArray(defaultPhoto);
			return defaultId;
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			if (menuCount == 0) {
				menuItemSetting();
				mListMenu.setVisibility(View.VISIBLE);
				menuCount++;
			} else {
				mListMenu.setVisibility(View.GONE);
				menuCount = 0;
			}
			return true;
		} else {
			return super.onKeyUp(keyCode, event);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.sms_inbox_delete, menu);
		menu.setHeaderTitle("Message Option");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.inbox_delete: {
			SmsDetailsModel list = mQueryListArray.get(info.position);
			onCascadeDelete(list);
			return true;
		}
		default:
			return super.onContextItemSelected(item);
		}
	}

	public void onCascadeDelete(SmsDetailsModel inbox) {
		ContentResolver cr = getContentResolver();
		cr.delete(SmsIndividualTable.CONTENT_URI_INDIVIDUAL,
				SmsIndividualTable.PHONE_NUMBER + "=?",
				new String[] { inbox.getmContactAddress() });
		cr.delete(SmsHistoryTable.CONTENT_URI_HISTORY, null, null);
		UIupdate();
	}
}
