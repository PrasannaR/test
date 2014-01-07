package com.cognizant.trumobi.messenger.sms;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
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
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.cognizant.trubox.contacts.db.ContactsConsts;
import com.cognizant.trumobi.PersonaLauncher;
import com.cognizant.trumobi.R;
import com.TruBoxSDK.*;

import com.cognizant.trumobi.messenger.adapter.SmsContactPickerAdapter;
import com.cognizant.trumobi.messenger.adapter.SmsCustomListAdapter;
import com.cognizant.trumobi.messenger.adapter.SmsCustomMenuAdapter;
import com.cognizant.trumobi.messenger.db.SmsHistoryTable;
import com.cognizant.trumobi.messenger.db.SmsIndividualTable;
import com.cognizant.trumobi.messenger.model.SmsDetailsListModel;
import com.cognizant.trumobi.messenger.sms.SmsChipsMultiAutoCompleteTextview.SelectItemContact;
import com.google.common.base.Strings;
import com.TruBoxSDK.TruBoxEncryptionPreference;
import com.cognizant.trumobi.commonabstractclass.TruMobBaseEditText;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseActivity;
import com.cognizant.trumobi.container.Utils.OutlookPreference;
import com.cognizant.trumobi.em.Email;

public class SmsMain extends TruMobiBaseActivity implements SelectItemContact {
	ImageView btnSendSMS;
	ImageView addContact;
	ImageView callContact;
	ImageView corporateIcon;
	ImageView messengerIcon;
	// EditText txtPhoneNo;
	TruMobBaseEditText txtMessage;
	TextView sendHeader;
	LinearLayout lnr;
	String mNum;
	String mBody;
	String mName;
	long mTime;
	ArrayList<SmsDetailsListModel> mArraySms;
	ArrayList<SmsContactBean> selectedContacts;
	ArrayList<SmsContactBean> draftContacts;
	ArrayList<SmsContactBean> saveContactInstace;
	ArrayList<SmsContactBean> savedContactInstace;
	ArrayList<String> smsPhoneNumber;
	ArrayList<String> menuItem;
	List<SmsContactBean> questions;
	String mNewMessageNumber;
	String mNewMessageName;
	String mNumberSelect;
	String mNameSelect;
	String mMsgBody;
	String mConPhone;
	String mConName;
	String orientationConPhone;
	String orientationPhoneNumber;
	String orientationPhoneName;
	byte[] orientationImage;
	boolean orientationFlag;
	byte[] mConImage;
	PendingIntent sentPI;
	PendingIntent deliveryPI;
	LinearLayout mToAddressBar;
	LinearLayout mThreadLayout;
	SmsCustomListAdapter mListAdapter;
	ListView lst;
	boolean bNewCompose = false;
	boolean fromContact = false;
	boolean menuFlag = false;
	boolean lock = false;
	boolean draftBlock = false;
	static Context context;
	private Dialog dialog;
	String phoneNo;
	String msgBoday;
	byte[] contactImageByte;
	SmsIndividualTable values;
	Uri uriHistroyInsert;
	// @SuppressLint("HandlerLeak")
	Uri uriIndividualInsert;
	String broadString;
	ImageView mMenu;
	ListView mListMenu;
	int menuCount = 0;
	SmsChipsMultiAutoCompleteTextview txtPhoneNo;
	private static final int MENU_INSERT_SMILEY = 26;
	private static final int DELETE_THREAD = 27;
	private static final int SETTINGS = 28;
	private AlertDialog mSmileyDialog;
	private android.content.SharedPreferences mPrefSecure;
	static HashMap<String, SmsContactBean> mHashMap;
	static HashMap<String, SmsContactBean> map;
	SmsContactPickerAdapter contactPickerAdapter;
	SmsContactPickerAdapter savedContactPickerAdapter;
	ProgressDialog waitSpinner;
	SmsContactBean selectedContactPos;
	SmsContactBean orientationPhoneDetails;
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// dialog.dismiss();

			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					UIupdate();
				}
			});

		}

	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPrefSecure = new com.TruBoxSDK.SharedPreferences(this);
		context = this;
		setContentView(R.layout.sms_send_message);

		btnSendSMS = (ImageView) findViewById(R.id.send_message);
		txtPhoneNo = (SmsChipsMultiAutoCompleteTextview) findViewById(R.id.toNumber);
		txtMessage = (TruMobBaseEditText) findViewById(R.id.sendMessage);
		mToAddressBar = (LinearLayout) findViewById(R.id.toLayout);
		mThreadLayout = (LinearLayout) findViewById(R.id.parent_layout);
		callContact = (ImageView) findViewById(R.id.icon_call_icon);
		corporateIcon = (ImageView) findViewById(R.id.corporate_send_icon);
		messengerIcon = (ImageView) findViewById(R.id.icon);
		sendHeader = (TextView) findViewById(R.id.con_name);
		lst = (ListView) findViewById(R.id.listView);
		mNumberSelect = getIntent().getExtras().getString("number");
		mNameSelect = getIntent().getExtras().getString("name");
		mMsgBody = getIntent().getExtras().getString("msg");
		broadString = getIntent().getExtras().getString("Message");
		mConPhone = getIntent().getExtras().getString("con_phone");
		mConName = getIntent().getExtras().getString("con_name");
		mConImage = getIntent().getExtras().getByteArray("con_image");
		mArraySms = new ArrayList<SmsDetailsListModel>();
		smsPhoneNumber = new ArrayList<String>();
		if (savedInstanceState != null) {
			mNewMessageNumber = savedInstanceState
					.getString("NewMessageNumber");
			mNewMessageName = savedInstanceState.getString("NewMessageName");
			bNewCompose = savedInstanceState.getBoolean("NewComposeFlag");
			orientationConPhone = savedInstanceState.getString("ContactPhone");
		}

		if (Strings.isNullOrEmpty(mNumberSelect)) {
			if (!Strings.isNullOrEmpty(mMsgBody)) {
				mToAddressBar.setVisibility(View.VISIBLE);
				callContact.setVisibility(View.GONE);
				sendHeader.setText("New Message");
				txtMessage.setText(mMsgBody);
				bNewCompose = true;
			} else if (!Strings.isNullOrEmpty(mConPhone)) {
				mToAddressBar.setVisibility(View.VISIBLE);
				callContact.setVisibility(View.GONE);
				sendHeader.setText("New Message");
				if (mConName.contains(",")) {
					mConName = mConName.replace(",", " ");
					String contactChip = mConName.concat(",");
					txtPhoneNo.setText(contactChip);
				} else {
					String contactChip = mConName.concat(",");
					txtPhoneNo.setText(contactChip);
				}
				int position = txtPhoneNo.length();
				Editable etext = txtPhoneNo.getText();
				Selection.setSelection(etext, position);
				bNewCompose = true;
			} else {
				mToAddressBar.setVisibility(View.VISIBLE);
				callContact.setVisibility(View.GONE);
				sendHeader.setText("New Message");
				bNewCompose = true;
			}
		} else {
			bNewCompose = false;
			mToAddressBar.setVisibility(View.GONE);
			callContact.setVisibility(View.VISIBLE);
			sendHeader.setText(mNameSelect);

			if (mNumberSelect.contains("^")) {
				String[] splitedString = mNumberSelect.split("\\^");
				for (int i = 0; i < splitedString.length; i++) {
					smsPhoneNumber.add(splitedString[i]);
				}
			} else
				smsPhoneNumber.add(mNumberSelect);

			if (!Strings.isNullOrEmpty(mMsgBody))
				txtMessage.setText(mMsgBody);
		}

		values = new SmsIndividualTable();
		btnSendSMS.setOnClickListener(new SendOnClickListener());

		callContact.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (!Strings.isNullOrEmpty(mConPhone))
					callToMessageNumber(mConPhone);
				else if (!Strings.isNullOrEmpty(mNewMessageNumber))
					callToMessageNumber(mNewMessageNumber);
				else
					callToMessageNumber(mNumberSelect);
			}
		});

		corporateIcon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(context, PersonaLauncher.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		});
		messengerIcon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onDiscardMessage();
			}
		});
		menuItemSetting();
		mMenu = (ImageView) findViewById(R.id.icon_overflow);
		mMenu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				menuItemSetting();
				if (View.GONE == mListMenu.getVisibility()) {
					mListMenu.setVisibility(View.VISIBLE);
				} else {
					mListMenu.setVisibility(View.GONE);
				}
			}
		});

		lst.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mListMenu.setVisibility(View.GONE);
					break;
				}
				return false;
			}
		});

		mThreadLayout.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				Log.e("Layout Touch", "On Touch");
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mListMenu.setVisibility(View.GONE);
					break;
				}
				return false;
			}
		});

		txtPhoneNo.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				Log.e("Layout Touch", "On Touch");
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mListMenu.setVisibility(View.GONE);
					break;
				}
				return false;
			}
		});

		txtPhoneNo.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					txtMessage.requestFocus();
				}
				return false;
			}
		});

		txtMessage.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mListMenu.setVisibility(View.GONE);
					break;
				}
				return false;
			}
		});

		selectedContacts = new ArrayList<SmsContactBean>();
		mHashMap = new HashMap<String, SmsContactBean>();
		if (savedInstanceState != null) {
			// mNewMessageNumber =
			// savedInstanceState.getString("NewMessageNumber");
			/*
			 * selectedContactPos = savedInstanceState
			 * .getParcelable("SelectedContactPostion");
			 */
			mHashMap = (HashMap<String, SmsContactBean>) savedInstanceState
					.getSerializable("HashMapValue");
			/*
			 * if (mHashMap != null) mHashMap = (HashMap<String,
			 * SmsContactBean>) savedInstanceState
			 * .getSerializable("HashMapValue");
			 */
			// mHashMap.put(selectedContactPos.name, selectedContactPos);
			orientationPhoneName = savedInstanceState
					.getString("OrientationName");
			orientationPhoneNumber = savedInstanceState
					.getString("OrientationPhone");
			orientationImage = savedInstanceState
					.getByteArray("OrientationImage");
			orientationFlag = savedInstanceState.getBoolean("OrientationFlag");
			selectedContacts = savedInstanceState
					.getParcelableArrayList("OrientationPhoneDetails");
		}

		map = new HashMap<String, SmsContactBean>();
		if (bNewCompose && savedInstanceState == null)
			new LoadContacts().execute();
		else if (bNewCompose && savedInstanceState != null) {
			saveContactInstace = new ArrayList<SmsContactBean>();
			saveContactInstace = savedInstanceState
					.getParcelableArrayList("ContactAdapter");
			contactPickerAdapter = new SmsContactPickerAdapter(context,
					android.R.layout.simple_list_item_1, saveContactInstace);
			txtPhoneNo.setAdapter(contactPickerAdapter);
			txtPhoneNo
					.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
		}

		if (!Strings.isNullOrEmpty(mNumberSelect))
			UIupdate();
		if (!Strings.isNullOrEmpty(mNewMessageNumber))
			UIupdate();
		if (!Strings.isNullOrEmpty(orientationConPhone))
			UIupdate();
		mListMenu.setVisibility(View.GONE);
		registerForContextMenu(lst);
	}

	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("ComposeMessage", "ValueSaved");
		outState.putParcelableArrayList("ContactAdapter", saveContactInstace);
		outState.putString("NewMessageNumber", mNewMessageNumber);
		outState.putString("NewMessageName", mNewMessageName);
		outState.putBoolean("NewComposeFlag", bNewCompose);
		outState.putString("ContactPhone", orientationConPhone);
		outState.putString("OrientationName", orientationPhoneName);
		outState.putString("OrientationPhone", orientationPhoneNumber);
		outState.putByteArray("OrientationImage", orientationImage);
		outState.putBoolean("OrientationFlag", orientationFlag);
		outState.putParcelableArrayList("OrientationPhoneDetails",
				selectedContacts);
		// outState.putParcelable("SelectedContactPostion", selectedContactPos);
		outState.putSerializable("HashMapValue", mHashMap);
		super.onSaveInstanceState(outState);
	}

	private void menuItemSetting() {
		// TODO Auto-generated method stub
		mListMenu = (ListView) findViewById(R.id.list_comp_menu);
		mListMenu.setAdapter(null);
		menuItem = new ArrayList<String>();
		menuItem.add("Insert Smiley");
		if (mToAddressBar.getVisibility() == View.VISIBLE)
			menuFlag = true;
		else
			menuFlag = false;
		if (menuFlag)
			menuItem.add("Discard");
		else
			menuItem.add("Delete thread");
		menuItem.add("Settings");

		SmsCustomMenuAdapter adp = new SmsCustomMenuAdapter(this, menuItem, 1);
		mListMenu.setDivider(null);
		mListMenu.setDividerHeight(0);

		adp.notifyDataSetChanged();
		mListMenu.setAdapter(adp);

		mListMenu.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				if (menuItem.get(arg2).toString() == "Insert Smiley") {
					showSmileyDialog();
				} else if (menuItem.get(arg2).toString() == "Discard") {
					onDiscardMessage();
				} else if (menuItem.get(arg2).toString() == "Delete thread") {
					deleteThreadedMessage();
				} else if (menuItem.get(arg2).toString() == "Settings") {
					callSettingsActivity();
				}
				mListMenu.setVisibility(View.GONE);
			}
		});
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
	public void onBackPressed() {
		// do something on back.
		onDiscardMessage();
		return;
	}

	private void onDiscardMessage() {
		// TODO Auto-generated method stub
		mListMenu.setVisibility(View.GONE);
		long drafttime = System.currentTimeMillis();
		String draftString = txtMessage.getText().toString();
		String draftWhere = SmsHistoryTable.PHONE_NUMBER + " =? " + "AND "
				+ SmsHistoryTable.MULTI_CONTACT + " IS NULL ";
		String draftWhereMultiContact = SmsHistoryTable.MULTI_CONTACT + " =? ";
		if (Strings.isNullOrEmpty(mNumberSelect)) {
			String number = txtPhoneNo.getText().toString();
			if (Strings.isNullOrEmpty(number)) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						context);
				// set title
				alertDialogBuilder.setTitle("Discard");
				// set dialog message
				alertDialogBuilder
						.setMessage(
								"Your message will be discarded because it has no valid recipients")
						.setCancelable(false)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// if this button is clicked, close
										// current activity
										onDraftCallListActivity();
									}
								})
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// if this button is clicked, just close
										// the dialog box and do nothing
										dialog.cancel();
									}
								});

				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();

				// show it
				alertDialog.show();
			} else if (bNewCompose == true && Strings.isNullOrEmpty(mConPhone)
					&& mHashMap.isEmpty()) {
				nonSendDraftMessage();
			} else if (bNewCompose == true && Strings.isNullOrEmpty(mConPhone)
					&& !mHashMap.isEmpty()) {
				nonSendDraftMessage();
			}

			else if (!Strings.isNullOrEmpty(mConPhone)) {
				// nonSendDraftMessage();
				if (Strings.isNullOrEmpty(draftString)) {
					onDraftCallListActivity();
				} else {
					final ContentResolver fromContact = getContentResolver();
					ContentValues fromContactValues = new ContentValues();
					Cursor cursor = fromContact.query(
							SmsIndividualTable.CONTENT_URI_INDIVIDUAL, null,
							SmsIndividualTable.PHONE_NUMBER + "=?",
							new String[] { mConPhone }, null);
					if (cursor.getCount() == 1) {
						fromContactValues.put("message", draftString);
						fromContactValues.put("sent_status", "draft");
						fromContactValues.put("draft_msg", true);
						fromContactValues.put("time", drafttime);
						fromContact.update(
								SmsIndividualTable.CONTENT_URI_INDIVIDUAL,
								fromContactValues,
								SmsIndividualTable.PHONE_NUMBER + "=?",
								new String[] { mConPhone });
						onDraftCallListActivity();
						Toast.makeText(getApplicationContext(),
								"Message Saved as draft", Toast.LENGTH_SHORT)
								.show();
					} else {
						fromContactValues.put("phonenumber", mConPhone);
						fromContactValues.put("firstname", mConName);
						fromContactValues.put("message", draftString);
						fromContactValues.put("sent_status", "draft");
						fromContactValues.put("draft_msg", true);
						fromContactValues.put("time", drafttime);
						fromContact.insert(
								SmsIndividualTable.CONTENT_URI_INDIVIDUAL,
								fromContactValues);
						onDraftCallListActivity();
						Toast.makeText(getApplicationContext(),
								"Message Saved as draft", Toast.LENGTH_SHORT)
								.show();
					}
					cursor.close();
				}

			} else {
				final ContentResolver unKnownNumber = getContentResolver();
				ContentValues unknownValues = new ContentValues();
				Cursor cursor = unKnownNumber.query(
						SmsIndividualTable.CONTENT_URI_INDIVIDUAL, null,
						SmsIndividualTable.PHONE_NUMBER + "=?",
						new String[] { orientationPhoneNumber }, null);
				if (!Strings.isNullOrEmpty(draftString)) {
					if (cursor.getCount() == 1) {
						unknownValues.put("message", draftString);
						unknownValues.put("sent_status", "draft");
						unknownValues.put("draft_msg", true);
						unknownValues.put("time", drafttime);
						unKnownNumber.update(
								SmsIndividualTable.CONTENT_URI_INDIVIDUAL,
								unknownValues, SmsIndividualTable.PHONE_NUMBER
										+ "=?",
								new String[] { orientationPhoneNumber });
						onDraftCallListActivity();
						Toast.makeText(getApplicationContext(),
								"Message Saved as draft", Toast.LENGTH_SHORT)
								.show();
					} else {
						unknownValues
								.put("phonenumber", orientationPhoneNumber);
						unknownValues.put("firstname", orientationPhoneName);
						unknownValues.put("message", draftString);
						unknownValues.put("sent_status", "draft");
						unknownValues.put("draft_msg", true);
						unknownValues.put("time", drafttime);
						unKnownNumber.insert(
								SmsIndividualTable.CONTENT_URI_INDIVIDUAL,
								unknownValues);
						onDraftCallListActivity();
						Toast.makeText(getApplicationContext(),
								"Message Saved as draft", Toast.LENGTH_SHORT)
								.show();
					}

				} else {
					this.finish();
				}
				cursor.close();
			}

		} else {
			if (!Strings.isNullOrEmpty(draftString)) {
				final ContentResolver draftResolver = getContentResolver();
				ContentValues draftValues = new ContentValues();
				draftValues.put("message", draftString);
				draftValues.put("draft_msg", true);
				draftValues.put("time", drafttime);
				draftResolver.update(SmsIndividualTable.CONTENT_URI_INDIVIDUAL,
						draftValues, SmsHistoryTable.PHONE_NUMBER + "=?",
						new String[] { mNumberSelect });
				Toast.makeText(getApplicationContext(),
						"Message Saved as draft", Toast.LENGTH_SHORT).show();
			} else {
				final ContentResolver cr = getContentResolver();
				final String[] projection = { SmsHistoryTable.PHONE_NUMBER,
						SmsHistoryTable.MESSAGE, SmsHistoryTable.TIME,
						SmsHistoryTable.LOCKED, SmsHistoryTable.MULTI_CONTACT };
				onDraftInboxUpdate(mNumberSelect, cr, projection, draftWhere,
						draftWhereMultiContact);
			}
			onDraftCallListActivity();

		}

		if (!Strings.isNullOrEmpty(mConPhone)) {
			// Intent contactActivity = new Intent()
		}
	}

	private void nonSendDraftMessage() {
		// TODO Auto-generated method stub
		draftBlock = true;
		final SmsIndividualTable home = new SmsIndividualTable();
		final SmsHistoryTable detail = new SmsHistoryTable();
		ContentResolver cr = getContentResolver();

		Bitmap defaultPhoto = BitmapFactory.decodeResource(getResources(),
				R.drawable.sms_ic_contact_picture);
		byte[] defaultId = bitmaptoByteArray(defaultPhoto);
		String msgBody = txtMessage.getText().toString();
		if (Strings.isNullOrEmpty(msgBody))
			this.finish();
		ArrayList<String> messageAray = new ArrayList<String>();
		messageAray.add(msgBody);
		txtMessage.setText(formatMessage(msgBody).toString());
		home.setMessage(txtMessage.getText().toString());
		mArraySms = new ArrayList<SmsDetailsListModel>();
		if (bNewCompose == true && Strings.isNullOrEmpty(mConPhone)) {
			String fullstr1 = txtPhoneNo.getText().toString();
			String fullsrt = fullstr1.replace(", ", ",");
			String chips[] = fullsrt.toString().trim().split(",");
			draftContacts = new ArrayList<SmsContactBean>();
			for (String c : chips) {

				SmsContactBean selectedObject = mHashMap.get(c);
				if (selectedObject != null) {
					draftContacts.add(selectedObject);
				} else if (PhoneNumberUtils.isGlobalPhoneNumber(c)) {
					SmsContactBean unknownContact = new SmsContactBean();
					new PickContacts().execute(c);
					unknownContact.setName(c);
					unknownContact.setPhoneNo(c);
					unknownContact.setByteImage(null);
					draftContacts.add(unknownContact);
				} else
					this.finish();

			}

			StringBuilder mContactApp = new StringBuilder();
			StringBuilder mContactName = new StringBuilder();
			int mMulSize = draftContacts.size();

			for (int i = 0; i < mMulSize; i++) {
				if ((i > 0) && (i < mMulSize)) {
					mContactApp.append("^");
					mContactName.append(",");
				}

				mContactApp.append(draftContacts.get(i).getPhoneNo());
				mContactName.append(draftContacts.get(i).getName());
				mNumberSelect = mContactApp.toString();
				mNameSelect = mContactName.toString();
				if (mMulSize == 1)
					home.setImageByte(draftContacts.get(i).getByteImage());
				else
					home.setImageByte(defaultId);
			}
			home.setFirstName(mNameSelect);
			home.setPhoneNumber(mNumberSelect);
			home.setDraftMessage(true);
			home.setSentStatus("draft");

		} else if (!Strings.isNullOrEmpty(mConPhone)) {
			SmsContactBean fromOtherApp = new SmsContactBean();
			fromOtherApp.setName(mConName);
			fromOtherApp.setPhoneNo(mConPhone);
			fromOtherApp.setByteImage(mConImage);
			draftContacts.add(fromOtherApp);
			home.setFirstName(mConName);
			home.setPhoneNumber(mConPhone);
			home.setImageByte(mConImage);
			home.setDraftMessage(true);
			home.setSentStatus("draft");
		}
		if (home.getPhoneNumber().length() > 0
				&& home.getMessage().length() > 0) {
			long tim = System.currentTimeMillis();
			byte[] picture = null;
			String name = null;
			home.setTime(tim);
			Cursor draftcursor = cr.query(
					SmsIndividualTable.CONTENT_URI_INDIVIDUAL, null,
					SmsIndividualTable.PHONE_NUMBER + "=?",
					new String[] { home.getPhoneNumber() }, null);
			ContentValues draftHomePageValue = new ContentValues();
			draftHomePageValue.put("phonenumber", home.getPhoneNumber());
			draftHomePageValue.put("firstname", home.getFirstName());
			draftHomePageValue.put("message", home.getMessage());
			draftHomePageValue.put("date", home.getDate());
			draftHomePageValue.put("time", home.getTime());
			draftHomePageValue.put("personimage", home.getImageByte());
			draftHomePageValue.put("sent_status", home.getSentStatus());
			draftHomePageValue.put("draft_msg", home.getDraftMessage());
			if (draftcursor.getCount() >= 1) {
				cr.update(SmsIndividualTable.CONTENT_URI_INDIVIDUAL,
						draftHomePageValue, SmsIndividualTable.PHONE_NUMBER
								+ "=?", new String[] { home.getPhoneNumber() });
			} else {
				uriIndividualInsert = cr.insert(
						SmsIndividualTable.CONTENT_URI_INDIVIDUAL,
						draftHomePageValue);
			}
			draftcursor.close();
			UIupdate();
		}
	}

	private void onDraftCallListActivity() {
		// TODO Auto-generated method stub
		this.finish();
	}

	private void onDraftInboxUpdate(String phoneNumber, ContentResolver cr,
			String[] projection, String draftWhere,
			String draftWhereMultiContact) {
		// TODO Auto-generated method stub
		String number = null;
		String msgBody = null;
		long mTime = 0;
		String multiContact = null;
		Cursor inboxUpdate = null;
		// String draftWhere = SmsHistoryTable.PHONE_NUMBER + " =? " + "AND " +
		// SmsHistoryTable.MULTI_CONTACT + " IS NULL ";
		// String draftWhereMultiContact = SmsHistoryTable.MULTI_CONTACT
		// +" =? ";
		if (phoneNumber.contains("^")) {
			inboxUpdate = cr.query(SmsHistoryTable.CONTENT_URI_HISTORY,
					projection, draftWhereMultiContact,
					new String[] { phoneNumber }, null);
		} else {
			inboxUpdate = cr.query(SmsHistoryTable.CONTENT_URI_HISTORY,
					projection, draftWhere, new String[] { phoneNumber }, null);
		}

		if (inboxUpdate != null) {
			if (inboxUpdate.moveToLast()) {

				number = inboxUpdate.getString(inboxUpdate
						.getColumnIndex(SmsHistoryTable.PHONE_NUMBER));
				msgBody = inboxUpdate.getString(inboxUpdate
						.getColumnIndex(SmsHistoryTable.MESSAGE));
				mTime = inboxUpdate.getLong(inboxUpdate
						.getColumnIndex(SmsHistoryTable.TIME));
				multiContact = inboxUpdate.getString(inboxUpdate
						.getColumnIndex(SmsHistoryTable.MULTI_CONTACT));
			}
			ContentValues updateInbox = new ContentValues();
			updateInbox.put("message", msgBody);
			updateInbox.put("time", mTime);
			updateInbox.put("draft_msg", false);
			if (!Strings.isNullOrEmpty(multiContact)) {
				cr.update(SmsIndividualTable.CONTENT_URI_INDIVIDUAL,
						updateInbox, SmsIndividualTable.PHONE_NUMBER + "=?",
						new String[] { multiContact });
			} else {
				cr.update(SmsIndividualTable.CONTENT_URI_INDIVIDUAL,
						updateInbox, SmsIndividualTable.PHONE_NUMBER + "=?",
						new String[] { number });
			}
			inboxUpdate.close();
		}
	}

	public void deleteThreadedMessage() {
		// TODO Auto-generated method stub
		final boolean multiContactCheck;
		if (!Strings.isNullOrEmpty(mNumberSelect)) {
			if (mNumberSelect.contains("^")) {
				multiContactCheck = true;
				commonDeleteFromMenu(mNumberSelect, multiContactCheck);
			} else {
				multiContactCheck = false;
				commonDeleteFromMenu(mNumberSelect, multiContactCheck);
			}
		} else if (!Strings.isNullOrEmpty(mNewMessageNumber)) {
			if (mNewMessageNumber.contains("^")) {
				multiContactCheck = true;
				commonDeleteFromMenu(mNewMessageNumber, multiContactCheck);
			} else {
				multiContactCheck = false;
				commonDeleteFromMenu(mNewMessageNumber, multiContactCheck);
			}
		} else if (!Strings.isNullOrEmpty(orientationPhoneNumber)) {
			if (orientationPhoneNumber.contains("^")) {
				multiContactCheck = true;
				commonDeleteFromMenu(orientationPhoneNumber, multiContactCheck);
			} else {
				multiContactCheck = false;
				commonDeleteFromMenu(orientationPhoneNumber, multiContactCheck);
			}
		}
	}

	private void commonDeleteFromMenu(final String phoneNumber,
			final boolean multiContactCheck) {
		// TODO Auto-generated method stub
		final ContentResolver cr = getContentResolver();
		final String[] projection = { SmsHistoryTable.PHONE_NUMBER,
				SmsHistoryTable.MESSAGE, SmsHistoryTable.TIME,
				SmsHistoryTable.LOCKED };
		String deleteWhere = SmsHistoryTable.PHONE_NUMBER + " =? " + "AND "
				+ SmsHistoryTable.MULTI_CONTACT + " IS NULL " + "AND "
				+ SmsHistoryTable.LOCKED + "=?";
		String deleteWhereMultiContact = SmsHistoryTable.MULTI_CONTACT + " =? "
				+ "AND " + SmsHistoryTable.LOCKED + "=?";
		Cursor deleteHistory = null;
		if (multiContactCheck) {
			deleteHistory = cr.query(SmsHistoryTable.CONTENT_URI_HISTORY,
					projection, deleteWhereMultiContact, new String[] {
							phoneNumber, "1" }, null);
		} else {
			deleteHistory = cr.query(SmsHistoryTable.CONTENT_URI_HISTORY,
					projection, deleteWhere, new String[] { phoneNumber, "1" },
					null);
		}
		/*
		 * Cursor deleteHistory = cr.query(SmsHistoryTable.CONTENT_URI_HISTORY,
		 * projection, SmsHistoryTable.PHONE_NUMBER + "='" + mNumberSelect + "'"
		 * + " AND " + SmsHistoryTable.LOCKED + "='" + "1" + "'", null, null);
		 */
		int count = deleteHistory.getCount();
		if (count == 1) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					context);
			// set title
			alertDialogBuilder.setTitle("Delete");
			// set dialog message
			alertDialogBuilder
					.setMessage("Delete this protected message?")
					.setCancelable(false)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// if this button is clicked, close
									// current activity
									if (multiContactCheck)
										cr.delete(
												SmsHistoryTable.CONTENT_URI_HISTORY,
												SmsHistoryTable.MULTI_CONTACT
														+ "=?",
												new String[] { phoneNumber });
									else
										cr.delete(
												SmsHistoryTable.CONTENT_URI_HISTORY,
												SmsHistoryTable.PHONE_NUMBER
														+ "=?",
												new String[] { phoneNumber });
									cr.delete(
											SmsIndividualTable.CONTENT_URI_INDIVIDUAL,
											SmsIndividualTable.PHONE_NUMBER
													+ "=?",
											new String[] { phoneNumber });
									UIupdate();
									callMsgListActivity();
								}
							})
					.setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									// if this button is clicked, just close
									// the dialog box and do nothing
									dialog.cancel();
								}
							});

			// create alert dialog
			AlertDialog alertDialog = alertDialogBuilder.create();

			// show it
			alertDialog.show();
		} else {
			if (multiContactCheck)
				cr.delete(SmsHistoryTable.CONTENT_URI_HISTORY,
						SmsHistoryTable.MULTI_CONTACT + "=?",
						new String[] { phoneNumber });
			else
				cr.delete(SmsHistoryTable.CONTENT_URI_HISTORY,
						SmsHistoryTable.PHONE_NUMBER + "=?",
						new String[] { phoneNumber });
			cr.delete(SmsIndividualTable.CONTENT_URI_INDIVIDUAL,
					SmsIndividualTable.PHONE_NUMBER + "=?",
					new String[] { phoneNumber });
			callMsgListActivity();

		}
		deleteHistory.close();
		UIupdate();
	}

	private void callMsgListActivity() {
		// TODO Auto-generated method stub
		Intent smsListDisplay = new Intent(context, SmsListdisplay.class);
		smsListDisplay.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		smsListDisplay.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(smsListDisplay);
	}

	private void callSettingsActivity() {
		// TODO Auto-generated method stub
		Intent setting = new Intent(context,
				SmsSettingsPreferenceActivity.class);
		startActivity(setting);
	}

	private void showSmileyDialog() {
		if (mSmileyDialog == null) {
			int[] icons = SmileyParser.DEFAULT_SMILEY_RES_IDS;
			String[] names = getResources().getStringArray(
					SmileyParser.DEFAULT_SMILEY_NAMES);
			final String[] texts = getResources().getStringArray(
					SmileyParser.DEFAULT_SMILEY_TEXTS);

			final int N = names.length;

			List<Map<String, ?>> entries = new ArrayList<Map<String, ?>>();
			for (int i = 0; i < N; i++) {
				// We might have different ASCII for the same icon, skip it if
				// the icon is already added.
				boolean added = false;
				for (int j = 0; j < i; j++) {
					if (icons[i] == icons[j]) {
						added = true;
						break;
					}
				}
				if (!added) {
					HashMap<String, Object> entry = new HashMap<String, Object>();

					entry.put("icon", icons[i]);
					entry.put("name", names[i]);
					entry.put("text", texts[i]);

					entries.add(entry);
				}
			}

			final SimpleAdapter a = new SimpleAdapter(this, entries,
					R.layout.sms_smiley_menu, new String[] { "icon", "name",
							"text" }, new int[] { R.id.smiley_icon,
							R.id.smiley_name, R.id.smiley_text });
			SimpleAdapter.ViewBinder viewBinder = new SimpleAdapter.ViewBinder() {
				@Override
				public boolean setViewValue(View view, Object data,
						String textRepresentation) {
					if (view instanceof ImageView) {
						Drawable img = getResources().getDrawable(
								(Integer) data);
						((ImageView) view).setImageDrawable(img);
						return true;
					}
					return false;
				}
			};
			a.setViewBinder(viewBinder);

			AlertDialog.Builder b = new AlertDialog.Builder(this);

			b.setTitle(getString(R.string.menu_insert_smiley));

			b.setCancelable(true);
			b.setAdapter(a, new DialogInterface.OnClickListener() {
				@Override
				@SuppressWarnings("unchecked")
				public final void onClick(DialogInterface dialog, int which) {
					HashMap<String, Object> item = (HashMap<String, Object>) a
							.getItem(which);

					String smiley = (String) item.get("text");

					txtMessage.append(smiley);
					txtMessage.requestFocus();
					dialog.dismiss();
				}
			});

			mSmileyDialog = b.create();
		}

		mSmileyDialog.show();

	}

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
	}

	BroadcastReceiver mlocalBroad = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (!bNewCompose)
				UIupdate();
		}
	};

	public void UIupdate() {
		// TODO Auto-generated method stub
		mArraySms.clear();
		query_list(mArraySms);
		lst.invalidateViews();
		mListAdapter = new SmsCustomListAdapter(this, mArraySms);
		mListAdapter.notifyDataSetChanged();
		lst.setAdapter(mListAdapter);
		bNewCompose = false;
		mToAddressBar.setVisibility(View.GONE);
		if (!Strings.isNullOrEmpty(mNewMessageName))
			sendHeader.setText(mNewMessageName);
		else if (!Strings.isNullOrEmpty(mConName))
			sendHeader.setText(mNewMessageName);
		if (!Strings.isNullOrEmpty(mNewMessageNumber)) {
			if (mNewMessageNumber.contains("^"))
				callContact.setVisibility(View.GONE);
			else
				callContact.setVisibility(View.VISIBLE);
		} else {
			if (mNumberSelect.contains("^"))
				callContact.setVisibility(View.GONE);
			else
				callContact.setVisibility(View.VISIBLE);
		}

	}

	private void sendSMS(String phoneNumber, ArrayList<String> message, Uri m) {
		Uri mUri;
		mUri = m;
		Messenger messenger = new Messenger(handler);
		ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>(
				1);
		ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>(1);
		boolean notificationsEnabled = mPrefSecure.getBoolean(
				SmsSettingsPreferenceActivity.SMS_DELIVERY_REPORT_MODE, false);
		if (notificationsEnabled) {
			deliveryIntents.add(PendingIntent.getBroadcast(this, 0, new Intent(
					SmsBroadCastDelivery.MESSAGE_STATUS_RECEIVED_ACTION, mUri,
					this, SmsBroadCastDelivery.class), 0));
		} else {
			deliveryIntents.add(null);
		}
		sentIntents.add(PendingIntent.getBroadcast(this, 1, new Intent(
				SmsBroadCastSent.MESSAGE_SENT_ACTION, mUri, this,
				SmsBroadCastSent.class), 0));
		SmsManager sms = SmsManager.getDefault();
		mNum = phoneNumber;
		try {
			sms.sendMultipartTextMessage(phoneNumber, null, message,
					sentIntents, deliveryIntents);
		} catch (Exception e) {
			e.printStackTrace();
		}
		UIupdate();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (waitSpinner != null && waitSpinner.isShowing()) {
			waitSpinner.cancel();
		}

	}

	private void query_list(ArrayList<SmsDetailsListModel> mArraySms) {
		// TODO Auto-generated method stub
		String where = SmsHistoryTable.PHONE_NUMBER + " =? " + "AND "
				+ SmsHistoryTable.MULTI_CONTACT + " IS NULL ";
		String whereMultiContact = SmsHistoryTable.MULTI_CONTACT + " =? ";
		String[] projection = { SmsHistoryTable.PHONE_NUMBER,
				SmsHistoryTable.MESSAGE, SmsHistoryTable.SEND_RECEIVE,
				SmsHistoryTable.HISTROY_ID, SmsHistoryTable.TIME,
				SmsHistoryTable.IMAGE, SmsHistoryTable.SENT_STATUS,
				SmsHistoryTable.LOCKED, SmsHistoryTable.MULTI_CONTACT };
		Cursor indicur = null;

		if (!Strings.isNullOrEmpty(mNumberSelect)) {
			if (mNumberSelect.contains("^")) {
				indicur = getContentResolver()
						.query(SmsHistoryTable.CONTENT_URI_HISTORY, projection,
								whereMultiContact,
								new String[] { mNumberSelect }, null);
			} else {
				indicur = getContentResolver().query(
						SmsHistoryTable.CONTENT_URI_HISTORY, projection, where,
						new String[] { mNumberSelect }, null);
			}
		} else if (!Strings.isNullOrEmpty(mNewMessageNumber)) {
			if (mNewMessageNumber.contains("^")) {
				indicur = getContentResolver().query(
						SmsHistoryTable.CONTENT_URI_HISTORY, projection,
						whereMultiContact, new String[] { mNewMessageNumber },
						null);
			} else {
				indicur = getContentResolver().query(
						SmsHistoryTable.CONTENT_URI_HISTORY, projection, where,
						new String[] { mNewMessageNumber }, null);
			}
		} else {
			indicur = getContentResolver().query(
					SmsHistoryTable.CONTENT_URI_HISTORY, projection, where,
					new String[] { mConPhone }, null);
		}

		if (indicur != null) {
			Log.e(">>><<<<",
					"Cursor Count For Threaded Message" + indicur.getCount());
			if (indicur.moveToFirst()) {
				do {

					String id = indicur.getString(indicur
							.getColumnIndex(SmsHistoryTable.HISTROY_ID));
					String num = indicur.getString(indicur
							.getColumnIndex(SmsHistoryTable.PHONE_NUMBER));
					String lBody = indicur.getString(indicur
							.getColumnIndex(SmsHistoryTable.MESSAGE));
					String mSR = indicur.getString(indicur
							.getColumnIndex(SmsHistoryTable.SEND_RECEIVE));
					long time = indicur.getLong(indicur
							.getColumnIndex(SmsHistoryTable.TIME));
					byte[] picture = indicur.getBlob(indicur
							.getColumnIndex(SmsHistoryTable.IMAGE));
					byte[] pic = loadContactPhoto(picture);
					String sentStatus = indicur.getString(indicur
							.getColumnIndex(SmsHistoryTable.SENT_STATUS));
					String strlock = indicur.getString(indicur
							.getColumnIndex(SmsHistoryTable.LOCKED));
					String multiCotact = indicur.getString(indicur
							.getColumnIndex(SmsHistoryTable.MULTI_CONTACT));
					mArraySms.add(new SmsDetailsListModel(time, num, lBody,
							mSR, id, pic, sentStatus, strlock, multiCotact));
				} while (indicur.moveToNext());
			}
			indicur.close();
		}

	}

	private byte[] bitmaptoByteArray(Bitmap bmp) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
		byte[] byteArray = stream.toByteArray();
		return byteArray;
	}

	public byte[] loadContactPhoto(byte[] imageId) {
		// TODO Auto-generated method stub
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
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		SmsDetailsListModel list = mArraySms.get(info.position);

		Boolean strLock = list.ismLocked();
		if (!strLock) {
			// lock = true;
			inflater.inflate(R.menu.sms_thread_menu, menu);
		} else {
			// lock = false;
			inflater.inflate(R.menu.sms_thread_menu1, menu);
		}
		menu.setHeaderTitle("Message options");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		SmsDetailsListModel list = mArraySms.get(info.position);
		switch (item.getItemId()) {
		case R.id.copy_text:
			int sdk = android.os.Build.VERSION.SDK_INT;
			if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
				android.text.ClipboardManager ClipMan = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				ClipMan.setText(list.getMsgBody());
			} else {
				android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				clipboard.setText(list.getMsgBody());
			}
			OutlookPreference.getInstance(context).setValue("PIMPOLICIES",
					list.getMsgBody());
			Toast.makeText(this, getResources().getString(R.string.copy_clip),
					Toast.LENGTH_SHORT).show();
			return true;
		case R.id.forward:
			onActivityCallItself(list);
			return true;
		case R.id.lock:
			lockMessage(list);
			return true;
		case R.id.unlock:
			unlockMessage(list);
			return true;
		case R.id.view:
			onViewDetails(list);
			return true;
		case R.id.delete: {
			onHistroyDelete(list);
			return true;
		}
		default:
			return super.onContextItemSelected(item);
		}
	}

	private void onViewDetails(SmsDetailsListModel list) {
		// TODO Auto-generated method stub
		String name = list.getmContactAddress();
		String time = list.getmMsgTime();
		String date = list.getmMsgDate();
		boolean sentFlag = list.ismSendRec();
		AlertDialog.Builder altDialog = new AlertDialog.Builder(this);
		altDialog.setTitle("Message details");
		if (sentFlag) {
			altDialog.setMessage("Type : Text message" + "\n" + "To : " + name
					+ "\n" + "Sent : " + time + ", " + date);
		} else {
			altDialog.setMessage("Type : Text message" + "\n" + "From : "
					+ name + "\n" + "Received : " + time + ", " + date);
		}

		altDialog.setNeutralButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
			}
		});
		altDialog.show();

	}

	public void unlockMessage(SmsDetailsListModel list) {
		// TODO Auto-generated method stub
		ContentResolver cr = getContentResolver();
		ContentValues unLockMsg = new ContentValues();
		unLockMsg.put("lock", false);
		Cursor cur = cr.query(SmsHistoryTable.CONTENT_URI_HISTORY, null,
				SmsHistoryTable.HISTROY_ID + "=?",
				new String[] { list.getMsgId() }, null);
		if (cur.getCount() == 1) {
			cr.update(SmsHistoryTable.CONTENT_URI_HISTORY, unLockMsg,
					SmsHistoryTable.HISTROY_ID + "=?",
					new String[] { list.getMsgId() });
		}
		cur.close();
		UIupdate();
	}

	public void lockMessage(SmsDetailsListModel list) {
		// TODO Auto-generated method stub
		ContentResolver cr = getContentResolver();
		ContentValues lockMsg = new ContentValues();
		lockMsg.put("lock", true);

		cr.update(SmsHistoryTable.CONTENT_URI_HISTORY, lockMsg,
				SmsHistoryTable.HISTROY_ID + "=?",
				new String[] { list.getMsgId() });
		UIupdate();
	}

	public ContentValues lockContentValue(boolean msglock) {
		ContentValues lockMsg = new ContentValues();
		lockMsg.put("lock", msglock);
		return lockMsg;

	}

	public void onActivityCallItself(SmsDetailsListModel list) {
		// TODO Auto-generated method stub
		String msgBody = list.getMsgBody();
		Intent fwdMessage = new Intent(SmsMain.this, SmsMain.class);
		fwdMessage.putExtra("msg", msgBody);
		startActivity(fwdMessage);

	}

	public void onHistroyDelete(final SmsDetailsListModel home) {
		String locked = null;
		final ContentResolver cr = getContentResolver();
		final String[] projection = { SmsHistoryTable.PHONE_NUMBER,
				SmsHistoryTable.MESSAGE, SmsHistoryTable.TIME,
				SmsHistoryTable.LOCKED, SmsHistoryTable.SENT_STATUS };
		Cursor lockCheck = cr.query(SmsHistoryTable.CONTENT_URI_HISTORY,
				projection, SmsHistoryTable.HISTROY_ID + "=?",
				new String[] { home.getMsgId() }, null);
		if (lockCheck.moveToFirst()) {
			locked = lockCheck.getString(lockCheck
					.getColumnIndex(SmsHistoryTable.LOCKED));
		}
		lockCheck.close();
		if ("1".equalsIgnoreCase(locked)) {
			if (!isFinishing()) {
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
						this);
				// set title
				alertDialogBuilder.setTitle("Delete");
				// set dialog message
				alertDialogBuilder
						.setMessage("Delete this protected message?")
						.setCancelable(false)
						.setPositiveButton("Yes",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// if this button is clicked, close
										// current activity
										onInboxDelete(home, cr, projection);
										cr.delete(
												SmsHistoryTable.CONTENT_URI_HISTORY,
												SmsHistoryTable.HISTROY_ID
														+ "=?",
												new String[] { home.getMsgId() });
										onInboxUpdate(home, cr, projection);
										// SMS.this.finish();
										UIupdate();
									}
								})
						.setNegativeButton("No",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {
										// if this button is clicked, just close
										// the dialog box and do nothing
										dialog.cancel();
									}
								});

				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();

				// show it
				alertDialog.show();
			}

		} else {
			onInboxDelete(home, cr, projection);
			cr.delete(SmsHistoryTable.CONTENT_URI_HISTORY,
					SmsHistoryTable.HISTROY_ID + "=?",
					new String[] { home.getMsgId() });
			onInboxUpdate(home, cr, projection);
		}
		UIupdate();
	}

	private void onInboxUpdate(SmsDetailsListModel home, ContentResolver cr,
			String[] projection) {
		// TODO Auto-generated method stub
		String number = null;
		String multiContact = null;
		String msgBody = null;
		long mTime = 0;
		String msgStatus = null;
		boolean multiContactFlag = false;
		Cursor inboxUpdate = null;
		if (!Strings.isNullOrEmpty(home.getMultiContact())
				&& home.getMultiContact().contains("^")) {
			multiContactFlag = true;
			inboxUpdate = cr.query(SmsHistoryTable.CONTENT_URI_HISTORY, null,
					SmsHistoryTable.MULTI_CONTACT + "=?",
					new String[] { home.getMultiContact() }, null);
		} else {
			multiContactFlag = false;
			inboxUpdate = cr.query(SmsHistoryTable.CONTENT_URI_HISTORY, null,
					SmsHistoryTable.PHONE_NUMBER + "=?" + "AND "
							+ SmsHistoryTable.MULTI_CONTACT + " IS NULL ",
					new String[] { home.getmContactAddress() }, null);
		}
		if(inboxUpdate.getCount() != 0){
			if (inboxUpdate.moveToLast()) {
	
				number = inboxUpdate.getString(inboxUpdate
						.getColumnIndex(SmsHistoryTable.PHONE_NUMBER));
				multiContact = inboxUpdate.getString(inboxUpdate
						.getColumnIndex(SmsHistoryTable.MULTI_CONTACT));
				msgBody = inboxUpdate.getString(inboxUpdate
						.getColumnIndex(SmsHistoryTable.MESSAGE));
				mTime = inboxUpdate.getLong(inboxUpdate
						.getColumnIndex(SmsHistoryTable.TIME));
				msgStatus = inboxUpdate.getString(inboxUpdate
						.getColumnIndex(SmsHistoryTable.SENT_STATUS));
			}
			inboxUpdate.close();
			ContentValues updateInbox = new ContentValues();
			updateInbox.put("message", msgBody);
			updateInbox.put("time", mTime);
			updateInbox.put("sent_status", msgStatus);
			if (multiContactFlag) {
				cr.update(SmsIndividualTable.CONTENT_URI_INDIVIDUAL, updateInbox,
						SmsIndividualTable.PHONE_NUMBER + "=?",
						new String[] { multiContact });
			}else{
				cr.update(SmsIndividualTable.CONTENT_URI_INDIVIDUAL, updateInbox,
						SmsIndividualTable.PHONE_NUMBER + "=?",
						new String[] { number });
			}
		}
	}

	private void onInboxDelete(SmsDetailsListModel home, ContentResolver cr,
			String[] projection) {
		// TODO Auto-generated method stub
		Cursor cur = null;
		boolean multiContactFlag = false;
		if (!Strings.isNullOrEmpty(home.getMultiContact())
				&& home.getMultiContact().contains("^")) {
			multiContactFlag = true;
			cur = cr.query(SmsHistoryTable.CONTENT_URI_HISTORY, null,
					SmsHistoryTable.MULTI_CONTACT + "=?",
					new String[] { home.getMultiContact() }, null);
		} else {
			multiContactFlag = false;
			cur = cr.query(SmsHistoryTable.CONTENT_URI_HISTORY, null,
					SmsHistoryTable.PHONE_NUMBER + "=?" + "AND "
							+ SmsHistoryTable.MULTI_CONTACT + " IS NULL ",
					new String[] { home.getmContactAddress() }, null);
		}
		int cursorCount = cur.getCount();
		if (cursorCount == 1) {
			if (multiContactFlag) {
				cr.delete(SmsIndividualTable.CONTENT_URI_INDIVIDUAL,
						SmsIndividualTable.PHONE_NUMBER + "=?",
						new String[] { home.getMultiContact() });
			} else {
				cr.delete(SmsIndividualTable.CONTENT_URI_INDIVIDUAL,
						SmsIndividualTable.PHONE_NUMBER + "=?",
						new String[] { home.getmContactAddress() });
			}
			Intent inbox = new Intent(context, SmsListdisplay.class);
			startActivity(inbox);
		}
		cur.close();

	}

	private class SendOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			mListMenu.setVisibility(View.GONE);
			final SmsIndividualTable home = new SmsIndividualTable();
			final SmsHistoryTable detail = new SmsHistoryTable();
			ContentResolver cr = getContentResolver();

			Bitmap defaultPhoto = BitmapFactory.decodeResource(getResources(),
					R.drawable.sms_ic_contact_picture);
			byte[] defaultId = bitmaptoByteArray(defaultPhoto);
			String msgBody = txtMessage.getText().toString();
			SmsManager sms = SmsManager.getDefault();
			ArrayList<String> messageAray = sms.divideMessage(msgBody);
			txtMessage.setText(formatMessage(msgBody).toString());
			home.setMessage(txtMessage.getText().toString());
			mArraySms = new ArrayList<SmsDetailsListModel>();

			if (bNewCompose == true && Strings.isNullOrEmpty(mConPhone)) {
				groupMessageCollection(defaultId, mConPhone, home);
			} else if (orientationFlag) {
				home.setFirstName(orientationPhoneName);
				home.setPhoneNumber(orientationPhoneNumber);
				home.setImageByte(orientationImage);
			} else if (!Strings.isNullOrEmpty(mConPhone) && mHashMap.isEmpty()) {
				// selectedContacts.add(objectFromOtherApp(home));
				groupMessageCollection(defaultId, mConPhone, home);
				/*
				 * home.setFirstName(mConName); home.setPhoneNumber(mConPhone);
				 * home.setImageByte(mConImage); mNewMessageName = mConName;
				 */
				orientationConPhone = mConPhone;
			} else if (!Strings.isNullOrEmpty(mConPhone) && !mHashMap.isEmpty()) {
				groupMessageCollection(defaultId, mConPhone, home);
			} else {
				home.setPhoneNumber(mNumberSelect);
				home.setFirstName(mNameSelect);
				mNewMessageName = mNameSelect;
			}
			validatingMessageContent(home, detail, messageAray);
		}

		@SuppressWarnings("null")
		private void validatingMessageContent(SmsIndividualTable home,
				SmsHistoryTable detail, ArrayList<String> messageAray) {
			Uri uriTable = null;
			String[] uriString = new String[50];
			if (!Strings.isNullOrEmpty(home.getPhoneNumber())
					&& !Strings.isNullOrEmpty(home.getMessage())) {
				if (home.getPhoneNumber().contains("^")) {
					txtMessage.setText("");
					String[] phone = home.getPhoneNumber().split("\\^");
					String[] name = home.getFirstName().split(",");
					for (int i = 0; i < phone.length; i++) {
						detail.setFirstName(name[i]);
						detail.setPhoneNumber(phone[i]);
						detail.setMultiContact(home.getPhoneNumber());
						uriTable = storeMessageDetailsInDB(home, detail);
						smsCountDelete();
						UIupdate();
						uriString[i] = uriTable.toString();
					}
					callSMSForMultiContact(messageAray, uriString);
				} else {
					detail.setFirstName(home.getFirstName());
					detail.setPhoneNumber(home.getPhoneNumber());
					detail.setMultiContact(null);
					uriTable = storeMessageDetailsInDB(home, detail);
					smsCountDelete();
					UIupdate();
					txtMessage.setText("");
					// callSMS(messageAray);
					uriString[0] = uriTable.toString();
					callSMSForMultiContact(messageAray, uriString);
				}
			} else if (Strings.isNullOrEmpty(home.getPhoneNumber())
					&& Strings.isNullOrEmpty(home.getMessage())) {
				Toast.makeText(getBaseContext(),
						"Please Enter both Phone number and Message.",
						Toast.LENGTH_SHORT).show();
			} else if (Strings.isNullOrEmpty(home.getMessage())) {
				Toast.makeText(getBaseContext(), "Please Enter Message",
						Toast.LENGTH_SHORT).show();
			}

			else
				Toast.makeText(getBaseContext(), "Please Enter Phone number",
						Toast.LENGTH_SHORT).show();
		}

		private Uri storeMessageDetailsInDB(SmsIndividualTable home,
				SmsHistoryTable detail) {
			// TODO Auto-generated method stub
			Uri uriHistoryTable = null;
			ContentResolver cr = getContentResolver();
			// final SmsHistoryTable detail = new SmsHistoryTable();
			Bitmap defaultPhoto = BitmapFactory.decodeResource(getResources(),
					R.drawable.sms_ic_contact_picture);
			byte[] defaultId = bitmaptoByteArray(defaultPhoto);
			long tim = System.currentTimeMillis();
			byte[] picture = null;
			String name = null;
			home.setTime(tim);
			Cursor cursor = cr.query(SmsIndividualTable.CONTENT_URI_INDIVIDUAL,
					null, SmsIndividualTable.PHONE_NUMBER + "=?",
					new String[] { home.getPhoneNumber() }, null);
			int cursorSize = cursor.getCount();
			if (cursorSize == 1) {
				if (cursor.moveToFirst() && !fromContact) {
					// fromContact = false;
					picture = cursor.getBlob(cursor
							.getColumnIndex(SmsHistoryTable.IMAGE));
					name = cursor.getString(cursor
							.getColumnIndex(SmsIndividualTable.FIRST_NAME));
					if (picture != null) {
						if (picture == defaultId && name != home.getFirstName()) {
							home.setImageByte(home.getImageByte());
							home.setFirstName(home.getFirstName());
						} else {
							home.setImageByte(picture);
							home.setFirstName(name);
						}
					}
				}
				ContentValues updatedValue = new ContentValues();
				home.setUnreadCount(0);
				updatedValue = getHomeListValue(home);
				cr.update(SmsIndividualTable.CONTENT_URI_INDIVIDUAL,
						updatedValue, SmsIndividualTable.PHONE_NUMBER + "=?",
						new String[] { home.getPhoneNumber() });

				// detail.setFirstName(home.getFirstName());
				// detail.setPhoneNumber(home.getPhoneNumber());
				detail.setMessage(home.getMessage());
				detail.setSendReceive(false);
				detail.setTime(home.getTime());
				detail.setContactImageByte(home.getImageByte());
				detail.setSentStatus("Sending....");
				detail.setLocked(false);

				ContentValues individual = new ContentValues();
				individual = getDetailListValue(detail);
				Cursor detailHistory = cr.query(
						SmsHistoryTable.CONTENT_URI_HISTORY, null,
						SmsHistoryTable.PHONE_NUMBER + "=?",
						new String[] { home.getPhoneNumber() }, null);
				if (detailHistory != null) {
					ContentValues existingValue = new ContentValues();
					existingValue.put("firstname", detail.getFirstName());
					existingValue.put("personimage",
							detail.getContacctImageByte());
					cr.update(SmsHistoryTable.CONTENT_URI_HISTORY,
							existingValue, SmsHistoryTable.PHONE_NUMBER + "=?",
							new String[] { home.getPhoneNumber() });
				}
				detailHistory.close();
				/*
				 * uriHistroyInsert = cr.insert(
				 * SmsHistoryTable.CONTENT_URI_HISTORY, individual);
				 * uriHistoryTable = cr.insert(
				 * SmsHistoryTable.CONTENT_URI_HISTORY, detailPageValue);
				 */
				uriHistoryTable = cr.insert(
						SmsHistoryTable.CONTENT_URI_HISTORY, individual);
				ContentValues image = new ContentValues();
				image.put("personimage", home.getImageByte());
				cr.update(SmsHistoryTable.CONTENT_URI_HISTORY, image,
						SmsHistoryTable.PHONE_NUMBER + "=?",
						new String[] { home.getPhoneNumber() });
			} else {
				ContentValues homePageValue = new ContentValues();
				home.setUnreadCount(0);
				homePageValue = getHomeListValue(home);
				uriIndividualInsert = cr.insert(
						SmsIndividualTable.CONTENT_URI_INDIVIDUAL,
						homePageValue);
				Log.e(">>><<<<", "Return URI" + uriHistroyInsert);
				// detail.setFirstName(home.getFirstName());
				// detail.setPhoneNumber(home.getPhoneNumber());
				detail.setMessage(home.getMessage());
				detail.setSendReceive(false);
				detail.setTime(home.getTime());
				detail.setContactImageByte(home.getImageByte());
				detail.setLocked(false);
				detail.setSentStatus("Sending....");

				ContentValues detailPageValue = new ContentValues();
				detailPageValue = getDetailListValue(detail);
				/*
				 * uriHistroyInsert = cr.insert(
				 * SmsHistoryTable.CONTENT_URI_HISTORY, detailPageValue);
				 */
				uriHistoryTable = cr.insert(
						SmsHistoryTable.CONTENT_URI_HISTORY, detailPageValue);
				// Log.e(">>><<<<", "Return URI" + uriHistroyInsert);
				Log.e(">>><<<<", "Return URI" + uriHistoryTable);
			}
			cursor.close();
			return uriHistoryTable;
		}

		private void smsCountDelete() {
			boolean deleteflag = mPrefSecure.getBoolean("pref_key_auto_delete",
					false);
			String limit = mPrefSecure.getString("pref_key_sms_delete_limit",
					null);
			// String strLimit = String.valueOf(limit);
			if (deleteflag) {
				if (!Strings.isNullOrEmpty(mNumberSelect)
						&& !Strings.isNullOrEmpty(limit))
					new DeleteThreadMessage(context).execute(mNumberSelect,
							limit);
				else if (!Strings.isNullOrEmpty(mConPhone)
						&& !Strings.isNullOrEmpty(limit))
					new DeleteThreadMessage(context).execute(mConPhone, limit);
				else if (Strings.isNullOrEmpty(mNewMessageNumber)
						&& !Strings.isNullOrEmpty(limit))
					new DeleteThreadMessage(context).execute(mNewMessageNumber,
							limit);
			}
		}

		private void callSMSForMultiContact(ArrayList<String> messageAray,
				String[] uriString) {
			if (!selectedContacts.isEmpty()) {
				fromContact = false;
				
				for(int i = 0; i < selectedContacts.size(); i++){
					sendSMS(selectedContacts.get(i).getPhoneNo(), messageAray,
							Uri.parse(uriString[i]));
				}
			} else {
				for (int i = 0; i < smsPhoneNumber.size(); i++) {
					sendSMS(smsPhoneNumber.get(i), messageAray,
							Uri.parse(uriString[i]));
				}
			}
		}
	}

	public ContentValues getHomeListValue(SmsIndividualTable home) {
		// TODO Auto-generated method stub
		ContentValues cv = new ContentValues();
		cv.put("phonenumber", home.getPhoneNumber());
		cv.put("firstname", home.getFirstName());
		cv.put("message", home.getMessage());
		cv.put("date", home.getDate());
		cv.put("time", home.getTime());
		cv.put("personimage", home.getImageByte());
		cv.put("unread_count", home.getUnreadCount());
		return cv;
	}

	public ContentValues getDetailListValue(SmsHistoryTable detail) {
		// TODO Auto-generated method stub
		ContentValues newValue = new ContentValues();
		newValue.put("firstname", detail.getFirstName());
		newValue.put("phonenumber", detail.getPhoneNumber());
		newValue.put("message", detail.getMessage());
		newValue.put("time", detail.getTime());
		newValue.put("send_receive", "send");
		newValue.put("personimage", detail.getContacctImageByte());
		newValue.put("sent_status", detail.getSentStatus());
		newValue.put("lock", detail.getLocked());
		newValue.put("multi_contact", detail.getMultiContact());
		return newValue;
	}

	public static CharSequence formatMessage(String body) {
		SmileyParser.init(context);
		SpannableStringBuilder buf = new SpannableStringBuilder();
		SmileyParser parser = SmileyParser.getInstance();
		buf.append(parser.addSmileySpans(body));
		return buf;
	}

	public int calculateMsgCount(String message) {
		// TODO Auto-generated method stub
		int totalSize = message.length();
		int count = totalSize / 60;
		return count;
	}

	public SmsContactBean objectFromOtherApp(SmsIndividualTable home) {
		// TODO Auto-generated method stub
		SmsContactBean fromOtherApp = new SmsContactBean();
		fromOtherApp.setName(mConName);
		fromOtherApp.setPhoneNo(mConPhone);
		fromOtherApp.setByteImage(mConImage);
		return fromOtherApp;
	}

	public void groupMessageCollection(byte[] defaultId, String mConPhone,
			SmsIndividualTable home) {
		// TODO Auto-generated method stub
		// final SmsIndividualTable home = new SmsIndividualTable();
		int contactNumber = 0;
		String fullstr1 = txtPhoneNo.getText().toString();
		String fullsrt = fullstr1.replace(", ", ",");
		String chips[] = fullsrt.toString().trim().split(",");
		for (String c : chips) {
			SmsContactBean selectedObject = mHashMap.get(c);
			if (selectedObject != null && Strings.isNullOrEmpty(mConPhone)) {
				selectedContacts.add(selectedObject);
			} else if (selectedObject != null
					&& !Strings.isNullOrEmpty(mConPhone)) {
				selectedContacts.add(selectedObject);
				// selectedContacts.add(objectFromOtherApp(home));
			} else if (selectedObject == null
					&& !Strings.isNullOrEmpty(mConPhone) && contactNumber == 0) {
				contactNumber++;
				selectedContacts.add(objectFromOtherApp(home));
			}
			if (PhoneNumberUtils.isGlobalPhoneNumber(c.trim())) {
				SmsContactBean unknownContact = new SmsContactBean();
				new PickContacts().execute(c, home.getMessage());
				unknownContact.setName(c);
				unknownContact.setPhoneNo(c);
				unknownContact.setByteImage(null);
				selectedContacts.add(unknownContact);
			}

		}

		StringBuilder mContactApp = new StringBuilder();
		StringBuilder mContactName = new StringBuilder();
		int mMulSize = selectedContacts.size();

		for (int i = 0; i < mMulSize; i++) {
			if ((i > 0) && (i < mMulSize)) {
				mContactApp.append("^");
				mContactName.append(",");
			}

			mContactApp.append(selectedContacts.get(i).getPhoneNo());
			mContactName.append(selectedContacts.get(i).getName().trim());
			/*
			 * mNumberSelect = mContactApp.toString(); mNameSelect =
			 * mContactName.toString();
			 */
			mNewMessageNumber = mContactApp.toString();
			mNewMessageName = mContactName.toString();
			if (mMulSize == 1) {
				home.setImageByte(selectedContacts.get(i).getByteImage());
				orientationImage = home.getImageByte();
			} else {
				home.setImageByte(defaultId);
				orientationImage = home.getImageByte();
			}
		}
		home.setFirstName(mNewMessageName);
		home.setPhoneNumber(mNewMessageNumber);
		orientationPhoneName = home.getFirstName();
		orientationPhoneNumber = home.getPhoneNumber();
		orientationFlag = bNewCompose;
	}

	@Override
	public void selectContactPos(int pos) {
		// TODO Auto-generated method stub
		// ContactBean selectedObject = contactPickerAdapter.getItem(pos);
		selectedContactPos = contactPickerAdapter.getItem(pos);
		mHashMap.put(selectedContactPos.name, selectedContactPos);
	}

	protected void callToMessageNumber(String mNumberSelect) {
		// TODO Auto-generated method stub
		Intent callIntent = new Intent(Intent.ACTION_CALL);
		callIntent.setData(Uri.parse("tel:" + mNumberSelect));
		callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		final List<ResolveInfo> pkgAppsList = context.getPackageManager()
				.queryIntentActivities(callIntent, 0);
		for (ResolveInfo mtemp : pkgAppsList) {
			if (mtemp.activityInfo.applicationInfo.packageName
					.equalsIgnoreCase("com.android.phone")) {
				String packageName = mtemp.activityInfo.applicationInfo.packageName;
				String className = mtemp.activityInfo.name;
				callIntent.setClassName(packageName, className);
			}
		}
		context.startActivity(callIntent);

	}

	private class PickContacts extends
			AsyncTask<String, String, ArrayList<SmsContactBean>> {
		String txtBodyMessage;
		Cursor phones = null;

		@Override
		protected ArrayList<SmsContactBean> doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			if (!draftBlock)
				txtBodyMessage = arg0[1].toString();
			ArrayList<SmsContactBean> contacts = new ArrayList<SmsContactBean>();
			try {
				boolean nativeContactsPrefsVal = new SharedPreferences(
					 Email.getAppContext()).getBoolean( "prefNativeContacts",
					 false);
				if(nativeContactsPrefsVal) {
				phones = context.getContentResolver().query(
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						null,
						ContactsContract.CommonDataKinds.Phone.NUMBER
									+ "=?",
							new String[] { arg0[0].toString() }, null);
				}
				Cursor coporateContact = context.getContentResolver().query(
						ContactsConsts.CONTENT_URI_CONTACTS, null,
						ContactsConsts.CONTACT_PHONE + " LIKE ?",
						new String[] { "'%" + arg0[0].toString() + "%'" }, null);
				if (coporateContact.getCount() >= 1) {
					while (coporateContact.moveToNext()) {
						ArrayList<HashMap<String, String>> phoneVal = new ArrayList<HashMap<String, String>>();
						String name = coporateContact
								.getString(coporateContact
										.getColumnIndex(ContactsConsts.CONTACT_FIRST_NAME));
						String phone_details = coporateContact
								.getString(coporateContact
										.getColumnIndex(ContactsConsts.CONTACT_PHONE));
						String[] splitParentStr = phone_details.split(":");
						for (int i = 0; i < splitParentStr.length; i++) {
							if (splitParentStr[i].contains("=")) {
								String[] phone_array = splitParentStr[i]
										.split("=");
								HashMap<String, String> row = new HashMap<String, String>();
								row.put("phonetype" + i, phone_array[0]);
								row.put("phoneNumber" + i, phone_array[1]);
								phoneVal.add(row);
							}
						}
						String phoneNumber = null;
						String numberType = null;
						for (int i = 0; i < phoneVal.size(); i++) {
							if (i == 0) {
								phoneNumber = phoneVal.get(i).get(
										"phoneNumber" + i);
								numberType = phoneVal.get(i).get(
										"phonetype" + i);
							}

						}

						byte[] imageBytes = coporateContact
								.getBlob(coporateContact
										.getColumnIndex(ContactsConsts.CONTACT_PHOTO));
						if (imageBytes == null) {
							Bitmap defaultPhoto = BitmapFactory.decodeResource(
									context.getResources(),
									R.drawable.sms_ic_contact_picture);
							imageBytes = bitmaptoByteArray(defaultPhoto);
						}
						SmsContactBean corporateContact = new SmsContactBean();
						corporateContact.setName(name);
						corporateContact.setPhoneNo(phoneNumber);
						corporateContact.setType(numberType);
						corporateContact.setByteImage(imageBytes);
						contacts.add(corporateContact);
					}
				} else if (phones.getCount() >= 1) {
					while (phones.moveToNext()) {

						String name = phones
								.getString(phones
										.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

						String phoneNumber = phones
								.getString(phones
										.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						String numberType = phones
								.getString(phones
										.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
						long imageId = phones
								.getLong(phones
										.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));

						Bitmap phPhoto = loadContactPhotoUpdate(context,
								context.getContentResolver(), imageId);
						byte[] byteImage = bitmaptoByteArray(phPhoto);
						SmsContactBean nativeContact = new SmsContactBean();
						nativeContact.setName(name);
						nativeContact.setPhoneNo(phoneNumber);
						nativeContact.setType(numberType);
						nativeContact.setByteImage(byteImage);
						contacts.add(nativeContact);
					}
				}
				coporateContact.close();
				phones.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return contacts;
		}

		@Override
		protected void onPostExecute(ArrayList<SmsContactBean> contactDetails) {
			// TODO Auto-generated method stub
			ContentResolver cr = getContentResolver();
			Bitmap defaultPhoto = BitmapFactory.decodeResource(getResources(),
					R.drawable.sms_ic_contact_picture);
			byte[] defaultId = bitmaptoByteArray(defaultPhoto);
			if (contactDetails != null) {
				// ContactBean home = new ContactBean();
				for (SmsContactBean contact : contactDetails) {
					SmsIndividualTable home = new SmsIndividualTable();
					SmsHistoryTable detail = new SmsHistoryTable();
					home.setFirstName(contactDetails.get(0).getName()
							.toString());
					home.setPhoneNumber(contactDetails.get(0).getPhoneNo()
							.toString());
					home.setImageByte(contactDetails.get(0).getByteImage());

					long tim = System.currentTimeMillis();
					home.setTime(tim);
					home.setMessage(txtBodyMessage);

					byte[] picture = null;
					String name = null;
					Cursor cursor = cr.query(
							SmsIndividualTable.CONTENT_URI_INDIVIDUAL, null,
							SmsIndividualTable.PHONE_NUMBER + "=?",
							new String[] { home.getPhoneNumber() }, null);
					int cursorSize = cursor.getCount();
					if (cursorSize == 1) {
						if (cursor.moveToFirst()) {
							picture = cursor.getBlob(cursor
									.getColumnIndex(SmsHistoryTable.IMAGE));
							name = cursor
									.getString(cursor
											.getColumnIndex(SmsIndividualTable.FIRST_NAME));
							if (picture != null) {
								if (picture == defaultId
										&& picture != home.getImageByte())
									home.setImageByte(home.getImageByte());
								else {
									home.setImageByte(picture);
									home.setFirstName(name);
								}
							}
						}
						ContentValues updatedValue = new ContentValues();
						updatedValue = getHomeListValue(home);
						cr.update(SmsIndividualTable.CONTENT_URI_INDIVIDUAL,
								updatedValue, SmsIndividualTable.PHONE_NUMBER
										+ "=?",
								new String[] { home.getPhoneNumber() });

						detail.setFirstName(home.getFirstName());
						detail.setPhoneNumber(home.getPhoneNumber());
						detail.setMessage(home.getMessage());
						detail.setSendReceive(false);
						detail.setTime(home.getTime());
						detail.setContactImageByte(home.getImageByte());
						detail.setSentStatus("Sending....");
						detail.setLocked(false);

						ContentValues individual = new ContentValues();

						ContentValues image = new ContentValues();
						image.put("personimage", home.getImageByte());
						cr.update(SmsHistoryTable.CONTENT_URI_HISTORY, image,
								SmsHistoryTable.PHONE_NUMBER + "=?",
								new String[] { home.getPhoneNumber() });

					} /*
					 * else { ContentValues homePageValue = new ContentValues();
					 * homePageValue = getHomeListValue(home);
					 * uriIndividualInsert = cr.insert(
					 * SmsIndividualTable.CONTENT_URI_INDIVIDUAL,
					 * homePageValue); Log.e(">>><<<<", "Return URI From Async"
					 * + uriHistroyInsert);
					 * detail.setFirstName(home.getFirstName());
					 * detail.setPhoneNumber(home.getPhoneNumber());
					 * detail.setMessage(home.getMessage());
					 * detail.setSendReceive(false);
					 * detail.setTime(home.getTime());
					 * detail.setContactImageByte(home.getImageByte());
					 * detail.setLocked(false);
					 * detail.setSentStatus("Sending....");
					 * 
					 * ContentValues detailPageValue = new ContentValues();
					 * detailPageValue = getDetailListValue(detail);
					 * uriHistroyInsert = cr.insert(
					 * SmsHistoryTable.CONTENT_URI_HISTORY, detailPageValue);
					 * Log.e(">>><<<<", "Return URI From Async" +
					 * uriHistroyInsert); }
					 */
					cursor.close();
					UIupdate();
				}

			}

		}

		public Bitmap loadContactPhotoUpdate(Context ctx,
				ContentResolver contentResolver, long imageId) {
			// TODO Auto-generated method stub
			Uri photoUri = null;
			photoUri = ContentUris.withAppendedId(
					ContactsContract.Contacts.CONTENT_URI, imageId);
			Bitmap defaultPhoto = BitmapFactory.decodeResource(
					ctx.getResources(), R.drawable.sms_ic_contact_picture);
			if (photoUri != null) {
				InputStream input = ContactsContract.Contacts
						.openContactPhotoInputStream(contentResolver, photoUri);
				if (input != null) {
					return BitmapFactory.decodeStream(input);
				}
			} else {

				// return defaultPhoto;
			}

			return defaultPhoto;
		}
	}

	private class LoadContacts extends
			AsyncTask<Void, SmsContactPickerAdapter, SmsContactPickerAdapter> {
		Context context;

		@Override
		protected SmsContactPickerAdapter doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			saveContactInstace = new ArrayList<SmsContactBean>();
			saveContactInstace = SmsUtil.getContacts(Email.getAppContext(),
					false);
			contactPickerAdapter = new SmsContactPickerAdapter(
					Email.getAppContext(), android.R.layout.simple_list_item_1,
					saveContactInstace);
			return contactPickerAdapter;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			waitSpinner = new ProgressDialog(SmsMain.this);
			waitSpinner.setCancelable(true);
			waitSpinner.setMessage("Loading ...Please wait");
			waitSpinner.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			waitSpinner.setProgress(0);
			waitSpinner.show();
		}

		@Override
		protected void onPostExecute(SmsContactPickerAdapter contactValue) {
			super.onPostExecute(contactValue);
			txtPhoneNo.setAdapter(contactValue);
			txtPhoneNo
					.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
			waitSpinner.dismiss();
		}

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

}