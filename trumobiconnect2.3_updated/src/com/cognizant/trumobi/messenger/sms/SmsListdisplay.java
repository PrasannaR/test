package com.cognizant.trumobi.messenger.sms;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.TruBoxSDK.TruBoxDatabase;
import com.cognizant.trumobi.log.EmailLog;
import com.cognizant.trumobi.messenger.adapter.SmsCustomAdapter;
import com.cognizant.trumobi.messenger.adapter.SmsCustomMenuAdapter;
import com.cognizant.trumobi.messenger.db.SmsHistoryBackup;
import com.cognizant.trumobi.messenger.db.SmsHistoryTable;
import com.cognizant.trumobi.messenger.db.SmsIndividualTable;
import com.cognizant.trumobi.messenger.db.SmsDbHelper;
import com.cognizant.trumobi.PersonaLauncher;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.messenger.model.SmsDetailsModel;
import com.cognizant.trumobi.messenger.model.SmsSearchListModel;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseActivity;
import com.google.common.base.Strings;

public class SmsListdisplay extends TruMobiBaseActivity {

	ArrayList<SmsDetailsModel> mLocallistArray;
	ArrayList<SmsDetailsModel> mQueryListArray;
	ArrayList<SmsDetailsModel> mToastListArray;
	ArrayList<SmsDetailsModel> mToastLocalArray;
	RelativeLayout parentLayout;
	LinearLayout noConversation;
	SmsCustomAdapter mListAdapter;
	SmsCustomMenuAdapter mMenuAadapter;
	Context mContext;
	ListView mListDsp;
	ListView mListMenu;
	ImageView mMenu;
	ImageView corporateIcon;
	ImageView mBackButton;
	String mName;
	String mNumber;
	String mDraft;
	String mMsgBody;
	int menuCount = 0;
	private android.content.SharedPreferences mPrefSecure;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sms_list_activity);
		mContext = this;
		mPrefSecure = new com.TruBoxSDK.SharedPreferences(this);
		parentLayout = (RelativeLayout) findViewById(R.id.parent_layout);
		noConversation = (LinearLayout) findViewById(R.id.sms_no_conversation);
		ImageView lNewMessage = (ImageView) findViewById(R.id.icon_new_messeage);
		mBackButton = (ImageView) findViewById(R.id.home_back_button);
		mBackButton.setVisibility(View.GONE);
		corporateIcon = (ImageView) findViewById(R.id.corporate_icon);
		mListMenu = (ListView) findViewById(R.id.list_menu);

		lNewMessage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startSMS("", "", "", "");

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

		mListDsp = (ListView) findViewById(R.id.list_inbox);
		mListDsp.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				mName = mQueryListArray.get(arg2).getName();
				mNumber = mQueryListArray.get(arg2).getmContactAddress();
				mMsgBody = mQueryListArray.get(arg2).getMsgBody();
				mDraft = mQueryListArray.get(arg2).getmMsgTime();
				boolean deleteflag = mPrefSecure.getBoolean(
						"pref_key_auto_delete", false);
				String limit = mPrefSecure.getString(
						"pref_key_sms_delete_limit", null);
				// String strLimit = String.valueOf(limit);
				if (deleteflag && !Strings.isNullOrEmpty(limit))
					new DeleteThreadMessage(mContext).execute(mNumber, limit);
				startSMS(mName, mNumber, mMsgBody, mDraft);
			}
		});

		mListDsp.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mListMenu.setVisibility(View.GONE);
					menuCount = 0;
					break;
				}
				return false;
			}
		});

		parentLayout.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mListMenu.setVisibility(View.GONE);
					menuCount = 0;
					break;
				}
				return false;
			}
		});
		mMenu = (ImageView) findViewById(R.id.icon_overflow);
		mMenu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				menuItemSetting();
				// TODO Auto-generated method stub
				if (View.GONE == mListMenu.getVisibility()) {
					mListMenu.setVisibility(View.VISIBLE);
				} else {
					mListMenu.setVisibility(View.GONE);
				}
			}
		});
		UIupdate();
		menuItemSetting();
		mListMenu.setVisibility(View.GONE);
		registerForContextMenu(mListDsp);
	}

	private void menuItemSetting() {
		// TODO Auto-generated method stub
		mListMenu.setAdapter(null);
		ArrayList<String> st = new ArrayList<String>();
		if (mQueryListArray.isEmpty()) {
			st.add("Settings");
		} else {
			st.add("Settings");
			st.add("Delete thread");
		}

		mMenuAadapter = new SmsCustomMenuAdapter(this, st, 0);
		mMenuAadapter.notifyDataSetChanged();

		mListMenu.setDivider(null);
		mListMenu.setDividerHeight(0);
		mListMenu.setAdapter(mMenuAadapter);
		mListMenu.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				if (0 == arg2) {
					menuCount = 0;
					Intent setting = new Intent(SmsListdisplay.this,
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

						//

						// customPopup();
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
	public void onBackPressed() {
		// do something on back.
		this.finish();
		return;
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

	BroadcastReceiver mlocalBroad = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			UIupdate();
		}
	};

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

		LocalBroadcastManager.getInstance(this).unregisterReceiver(mlocalBroad);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		LocalBroadcastManager.getInstance(this).registerReceiver(mlocalBroad,
				new IntentFilter("LoaclReciev"));
		UIupdate();
	}

	public int unReadSMSCount(Context ctx) {

		String[] column = { SmsIndividualTable.UNREAD_COUNT };

		Cursor totalUnreadCount = ctx.getContentResolver().query(
				SmsIndividualTable.CONTENT_URI_INDIVIDUAL, column, null, null,
				null);

		int totalCount = 0;
		if (totalUnreadCount != null) {
			if (totalUnreadCount.moveToFirst()) {
				do {
					int unreadCount = totalUnreadCount.getInt(totalUnreadCount
							.getColumnIndex(SmsIndividualTable.UNREAD_COUNT));
					totalCount = totalCount + unreadCount;
				} while (totalUnreadCount.moveToNext());
			}
			totalUnreadCount.close();
		}
		System.out.println("Total Count of Unread Message : " + totalCount);

		return totalCount;

	}

	public void startSMS(String name, String number, String msg, String draft) {
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
		if ("Draft".equalsIgnoreCase(draft)) {
			intsms.putExtra("name", name);
			intsms.putExtra("number", number);
			intsms.putExtra("msg", msg);
		} else {
			intsms.putExtra("name", name);
			intsms.putExtra("number", number);
		}
		startActivity(intsms);
	}

	public void startSearch() {
		mListMenu.setVisibility(View.GONE);
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
		// mListDsp.setSelection(0);
		mListDsp.scrollTo(0, 0);
		mListDsp.setAdapter(mListAdapter);

	}

	public void refreshUI() {
		arrayGetItems();
		mListAdapter = new SmsCustomAdapter(mContext, mQueryListArray);
		mListDsp.setSelection(0);
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
					int unreadmsg = indicur.getInt(indicur
							.getColumnIndex(SmsIndividualTable.UNREAD_COUNT));
					System.out.println("Unread msg Count : " + unreadmsg);
					result.add(new SmsDetailsModel(time, phoneNumber, name,
							message, id, image, sentStatus, draftString,
							unreadmsg));

				} while (indicur.moveToNext());
			}
			indicur.close();
		}
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
			menuItemSetting();
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
		cr.delete(SmsHistoryTable.CONTENT_URI_HISTORY,
				SmsHistoryTable.PHONE_NUMBER + "=?",
				new String[] { inbox.getmContactAddress() });
		UIupdate();
	}

	private class DeleteThreadMessage extends AsyncTask<String, String, Void> {
		Context context;

		public DeleteThreadMessage(Context mContext) {
			// TODO Auto-generated constructor stub
			this.context = mContext;
		}

		@Override
		protected Void doInBackground(String... params) {
			// TODO Auto-generated method stub
			int deleteCount = Integer.parseInt(params[1].toString());
			Cursor threadCursor = context.getContentResolver().query(
					SmsHistoryTable.CONTENT_URI_HISTORY, null,
					SmsHistoryTable.PHONE_NUMBER + "=?",
					new String[] { params[0].toString() }, null);
			if (threadCursor.getCount() > deleteCount) {
				int numberToDelete = threadCursor.getCount() - deleteCount;
				if (threadCursor.moveToFirst()) {
					for (int i = 1; i <= numberToDelete; i++) {
						String id = threadCursor.getString(threadCursor
								.getColumnIndex(SmsHistoryTable.HISTROY_ID));
						context.getContentResolver().delete(
								SmsHistoryTable.CONTENT_URI_HISTORY,
								SmsHistoryTable.HISTROY_ID + "=?",
								new String[] { id });
						threadCursor.moveToNext();
						if (i == numberToDelete)
							threadCursor.moveToLast();
					}
				}
			}
			threadCursor.close();
			return null;
		}

	}

	public void customPopup() {
		// TODO Auto-generated method stub
		final Dialog dialog = new Dialog(context);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.sms_custompopup);

		TextView btnl = (TextView) dialog.findViewById(R.id.leftbtn);
		btnl.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				dialog.cancel();
			}
		});
		TextView btnr = (TextView) dialog.findViewById(R.id.rightbtn);
		btnr.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				ContentResolver cr = getContentResolver();
				cr.delete(SmsHistoryTable.CONTENT_URI_HISTORY, null, null);
				cr.delete(SmsIndividualTable.CONTENT_URI_INDIVIDUAL, null, null);
				UIupdate();
				dialog.cancel();
			}
		});
		dialog.show();

	}

}
