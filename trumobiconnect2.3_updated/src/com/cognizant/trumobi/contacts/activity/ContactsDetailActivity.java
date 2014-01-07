package com.cognizant.trumobi.contacts.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.TruBoxSDK.SharedPreferences;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.cognizant.trubox.contacts.db.ContactsConsts;
import com.cognizant.trubox.contacts.db.ContactsModel;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseSherlockFragmentBaseActivity;
import com.cognizant.trumobi.contacts.utils.ContactsUtilities;
import com.cognizant.trumobi.contacts.utils.ContactsVcardShare;
import com.cognizant.trumobi.dialer.utils.DialerUtilities;
import com.cognizant.trumobi.em.EmEmController;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.em.activity.EmMessageCompose;
import com.cognizant.trumobi.em.provider.EmEmailContent;
import com.cognizant.trumobi.em.provider.EmEmailContent.Mailbox;
import com.cognizant.trumobi.exchange.provider.EmGalResult.GalData;
import com.cognizant.trumobi.log.ContactsLog;
import com.cognizant.trumobi.messenger.sms.SmsMain;

public class ContactsDetailActivity extends TruMobiBaseSherlockFragmentBaseActivity implements OnClickListener {

	
	
	static boolean isAirplaneEnabled;
	
	String SecuredBrowserUrl = "";
	String SelectedRingTone ="";
	byte[] imageBytes = null;
	
	Intent Mringtone;
	public Uri CustomRingToneUri;
	RingtoneManager mRingtoneManager;
	
	File vcfFile;
	String surname = "";
	String frstname = "";
	String org = "";
	
	String Sharetitle="";
	String SharePhone1="";
	String SharePhone2="";
	String ShareEmailId="";
	
	String phone1 = "";
	String phone2 = "";
	String phone3 = "";
	String phone4 = "";
	String phone5 = "";
	String phone6 = "";
	String phone7 = "";
	String phone8 = "";
	String phone9 = "";
	String phone10 = "";
	String phone11 = "";
	String phone12 = "";
	String phone13 = "";
	String phone14 = "";
	String phone15 = "";
	
	String emailid = "";

	String StrEmailType = "";

	String phonetypeget1 = "";
	String phonetypeget2 = "";
	String phonetypeget3 = "";
	String phonetypeget4 = "";
	String phonetypeget5 = "";
	String phonetypeget6 = "";
	String phonetypeget7 = "";
	String phonetypeget8 = "";
	String phonetypeget9 = "";
	String phonetypeget10 = "";
	String phonetypeget11 = "";
	String phonetypeget12 = "";
	String phonetypeget13 = "";
	String phonetypeget14 = "";
	String phonetypeget15 = "";
	
	String addrs = "";

	String phonemobile = "";
	String ContactName="";
	String primemail = "";

	ImageView AddImage;
	ArrayAdapter<String> arrayAdapter_image;
	PopupWindow pwindow;

	Bitmap thumbnail;

	String selectionArgument = "_id = ?";
	String receivedExtra = "null";
	ContactsModel cModel = null;
	ProgressDialog searchDialog;
	GalData cGalData = null;
	public Integer favstatus;
	boolean isFavorite = false;
	ContentValues phoneNumbers = new ContentValues();
	ContentValues emailNumbers = new ContentValues();

	LinearLayout l_contactprimaryTxt;
	LinearLayout l_contactsecondaryTxt;

	LinearLayout l_contact_phone_no;

	LinearLayout l_contactphonenoTxt;
	LinearLayout l_contactphoneno2Txt;
	LinearLayout l_contactphoneno3Txt;
	LinearLayout l_contactphoneno4Txt;
	LinearLayout l_contactphoneno5Txt;
	LinearLayout l_contactphoneno6Txt;
	LinearLayout l_contactphoneno7Txt;
	LinearLayout l_contactphoneno8Txt;
	LinearLayout l_contactphoneno9Txt;
	LinearLayout l_contactphoneno10Txt;
	LinearLayout l_contactphoneno11Txt;
	LinearLayout l_contactphoneno12Txt;
	LinearLayout l_contactphoneno13Txt;
	LinearLayout l_contactphoneno14Txt;
	LinearLayout l_contactphoneno15Txt;
	
	
	LinearLayout l_contactemail;
	LinearLayout l_contactemailTxt;
	LinearLayout l_contactemail2Txt;
	LinearLayout l_contactemail3Txt;
	LinearLayout l_contactemail4Txt;
	LinearLayout l_contactemail5Txt;
	LinearLayout l_contactemail6Txt;
	
	LinearLayout l_contactim;
	LinearLayout l_contactimTxt;
	LinearLayout l_contactim2Txt;
	LinearLayout l_contactim3Txt;
	LinearLayout l_contactim4Txt;
	LinearLayout l_contactim5Txt;
	LinearLayout l_contactim6Txt;

	LinearLayout l_contactaddr;
	LinearLayout l_contactaddrTxt;
	LinearLayout l_contactaddr2Txt;
	LinearLayout l_contactaddr3Txt;

	LinearLayout l_contactimnameTxt;
	// LinearLayout l_contactaddrsTxt;
	LinearLayout l_contactnotesTxt;
	LinearLayout l_contactWebsiteTxt;
	LinearLayout l_contactInternetCallTxt;
	LinearLayout l_nickname;
	LinearLayout l_desig_comp;

	TextView contactphoneticTxt;
	TextView contactnickTxt;
	TextView contactphonenoTxt;
	TextView contactphoneno2Txt;
	TextView contactphoneno3Txt;
	TextView contactphoneno4Txt;
	TextView contactphoneno5Txt;
	TextView contactphoneno6Txt;
	TextView contactphoneno7Txt;
	TextView contactphoneno8Txt;
	TextView contactphoneno9Txt;	
	TextView contactphoneno10Txt;
	TextView contactphoneno11Txt;
	TextView contactphoneno12Txt;
	TextView contactphoneno13Txt;
	TextView contactphoneno14Txt;
	TextView contactphoneno15Txt;
	

	TextView emailtype;
	TextView emailtype2;
	TextView emailtype3;
	TextView emailtype4;
	TextView emailtype5;
	TextView emailtype6;
	
	TextView imtype;
	TextView imtype2;
	TextView imtype3;
	TextView imtype4;
	TextView imtype5;
	TextView imtype6;

	// TextView imtype;

	TextView contactaddrsTxt1;
	TextView addresstype1;
	TextView contactaddrsTxt2;
	TextView addresstype2;
	TextView contactaddrsTxt3;
	TextView addresstype3;
	String addrsMap = "";
	String addrsMap2 = "";
	String addrsMap3 = "";

	TextView phonetype;
	TextView phonetype2;
	TextView phonetype3;
	TextView phonetype4;
	TextView phonetype5;
	TextView phonetype6;
	TextView phonetype7;
	TextView phonetype8;
	TextView phonetype9;
	TextView phonetype10;
	TextView phonetype11;
	TextView phonetype12;
	TextView phonetype13;
	TextView phonetype14;
	TextView phonetype15;
	
	ImageView textmessage;
	ImageView textmessage2;
	ImageView textmessage3;
	ImageView textmessage4;
	ImageView textmessage5;
	ImageView textmessage6;
	ImageView textmessage7;
	ImageView textmessage8;
	ImageView textmessage9;
	ImageView textmessage10;
	ImageView textmessage11;
	ImageView textmessage12;
	ImageView textmessage13;
	ImageView textmessage14;
	ImageView textmessage15;
	

	TextView contactemailTxt;
	TextView contactemail2Txt;
	TextView contactemail3Txt;
	TextView contactemail4Txt;
	TextView contactemail5Txt;
	TextView contactemail6Txt;
	
	
	
	TextView contactimnameTxt;
	TextView contactimname2Txt;
	TextView contactimname3Txt;
	TextView contactimname4Txt;
	TextView contactimname5Txt;
	TextView contactimname6Txt;
	

	TextView contactnotesTxt;
	TextView contactWebsiteTxt;
	TextView contactNicknameTxt;
	TextView contactInternetCallTxt;
	TextView contact_name;
	TextView contact_designation;
	TextView contact_company;
	TextView contact_comma;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_details);

		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();

		if (bundle != null) {
			Serializable searchobject = bundle.getSerializable("searchObj");
			if (searchobject != null) {
				cGalData = (GalData) searchobject;
			}
			Serializable object = bundle.getSerializable("obj");
			if (object != null) {
				cModel = (ContactsModel) object;
				receivedExtra = String.valueOf(cModel.getContact_id());
			}
		}
		
		Log.d("ContactDetail", " receivedExtra;" + receivedExtra);
		
		contact_name = (TextView) findViewById(R.id.contact_name);
		AddImage = (ImageView) findViewById(R.id.contact_detail_image);
		AddImage.setOnClickListener(immageClickListener);

		l_contactprimaryTxt = (LinearLayout) findViewById(R.id.l_prim_name);
		l_contactsecondaryTxt = (LinearLayout) findViewById(R.id.l_sec_name);

		l_contact_phone_no = (LinearLayout) findViewById(R.id.l_contact_phone_no);

		l_contactphonenoTxt = (LinearLayout) findViewById(R.id.layoutTab1);
		l_contactphoneno2Txt = (LinearLayout) findViewById(R.id.layoutTab2);
		l_contactphoneno3Txt = (LinearLayout) findViewById(R.id.layoutTab3);
		l_contactphoneno4Txt = (LinearLayout) findViewById(R.id.layoutTab4);
		l_contactphoneno5Txt = (LinearLayout) findViewById(R.id.layoutTab5);
		l_contactphoneno6Txt = (LinearLayout) findViewById(R.id.layoutTab6);
		l_contactphoneno7Txt = (LinearLayout) findViewById(R.id.layoutTab7);
		l_contactphoneno8Txt = (LinearLayout) findViewById(R.id.layoutTab8);
		
		l_contactphoneno9Txt = (LinearLayout) findViewById(R.id.layoutTab9);
		l_contactphoneno10Txt = (LinearLayout) findViewById(R.id.layoutTab10);
		l_contactphoneno11Txt = (LinearLayout) findViewById(R.id.layoutTab11);
		l_contactphoneno12Txt = (LinearLayout) findViewById(R.id.layoutTab12);
		l_contactphoneno13Txt = (LinearLayout) findViewById(R.id.layoutTab13);
		l_contactphoneno14Txt = (LinearLayout) findViewById(R.id.layoutTab14);
		l_contactphoneno15Txt = (LinearLayout) findViewById(R.id.layoutTab15);
		
		l_contactemail = (LinearLayout) findViewById(R.id.l_contact_email);
		l_contactemailTxt = (LinearLayout) findViewById(R.id.layoutemailTab1);
		l_contactemail2Txt = (LinearLayout) findViewById(R.id.layoutemailTab2);
		l_contactemail3Txt = (LinearLayout) findViewById(R.id.layoutemailTab3);
		l_contactemail4Txt = (LinearLayout) findViewById(R.id.layoutemailTab4);
		l_contactemail5Txt = (LinearLayout) findViewById(R.id.layoutemailTab5);
		l_contactemail6Txt = (LinearLayout) findViewById(R.id.layoutemailTab6);
		
		
		l_contactim = (LinearLayout) findViewById(R.id.l_im_name);
		l_contactimTxt = (LinearLayout) findViewById(R.id.layoutimTab1);
		l_contactim2Txt = (LinearLayout) findViewById(R.id.layoutimTab2);
		l_contactim3Txt = (LinearLayout) findViewById(R.id.layoutimTab3);
		l_contactim4Txt = (LinearLayout) findViewById(R.id.layoutimTab4);
		l_contactim5Txt = (LinearLayout) findViewById(R.id.layoutimTab5);
		l_contactim6Txt = (LinearLayout) findViewById(R.id.layoutimTab6);

		l_contactaddr = (LinearLayout) findViewById(R.id.l_contact_address);
		l_contactaddrTxt = (LinearLayout) findViewById(R.id.layoutaddrTab1);
		l_contactaddr2Txt = (LinearLayout) findViewById(R.id.layoutaddrTab2);
		l_contactaddr3Txt = (LinearLayout) findViewById(R.id.layoutaddrTab3);

		l_contactnotesTxt = (LinearLayout) findViewById(R.id.l_contact_notes);
		l_contactWebsiteTxt = (LinearLayout) findViewById(R.id.l_website);
		l_contactInternetCallTxt = (LinearLayout) findViewById(R.id.l_internetcall);
		l_nickname = (LinearLayout) findViewById(R.id.l_nickname);
		l_desig_comp = (LinearLayout) findViewById(R.id.l_desig_comp);

		contactphoneticTxt = (TextView) findViewById(R.id.contact_prim_name_value);
		contactnickTxt = (TextView) findViewById(R.id.contact_sec_name_value);
		contactphonenoTxt = (TextView) findViewById(R.id.contact_phone_no_value);
		contactphoneno2Txt = (TextView) findViewById(R.id.contact_phone_no_value2);
		contactphoneno3Txt = (TextView) findViewById(R.id.contact_phone_no_value3);
		contactphoneno4Txt = (TextView) findViewById(R.id.contact_phone_no_value4);
		contactphoneno5Txt = (TextView) findViewById(R.id.contact_phone_no_value5);
		contactphoneno6Txt = (TextView) findViewById(R.id.contact_phone_no_value6);
		contactphoneno7Txt = (TextView) findViewById(R.id.contact_phone_no_value7);
		contactphoneno8Txt = (TextView) findViewById(R.id.contact_phone_no_value8);
		contactphoneno9Txt = (TextView) findViewById(R.id.contact_phone_no_value9);
		contactphoneno10Txt = (TextView) findViewById(R.id.contact_phone_no_value10);
		contactphoneno11Txt = (TextView) findViewById(R.id.contact_phone_no_value11);
		contactphoneno12Txt = (TextView) findViewById(R.id.contact_phone_no_value12);
		contactphoneno13Txt = (TextView) findViewById(R.id.contact_phone_no_value13);
		contactphoneno14Txt = (TextView) findViewById(R.id.contact_phone_no_value14);
		contactphoneno15Txt = (TextView) findViewById(R.id.contact_phone_no_value15);

		contactphonenoTxt.setOnClickListener(this);
		phonetype = (TextView) findViewById(R.id.Phonetype);
		phonetype.setOnClickListener(this);

		contactphoneno2Txt.setOnClickListener(this);
		phonetype2 = (TextView) findViewById(R.id.Phonetype2);
		phonetype2.setOnClickListener(this);

		contactphoneno3Txt.setOnClickListener(this);
		phonetype3 = (TextView) findViewById(R.id.Phonetype3);
		phonetype3.setOnClickListener(this);

		contactphoneno4Txt.setOnClickListener(this);
		phonetype4 = (TextView) findViewById(R.id.Phonetype4);
		phonetype4.setOnClickListener(this);

		contactphoneno5Txt.setOnClickListener(this);
		phonetype5 = (TextView) findViewById(R.id.Phonetype5);
		phonetype5.setOnClickListener(this);

		contactphoneno6Txt.setOnClickListener(this);
		phonetype6 = (TextView) findViewById(R.id.Phonetype6);
		phonetype6.setOnClickListener(this);

		contactphoneno7Txt.setOnClickListener(this);
		phonetype7 = (TextView) findViewById(R.id.Phonetype7);
		phonetype7.setOnClickListener(this);

		contactphoneno8Txt.setOnClickListener(this);
		phonetype8 = (TextView) findViewById(R.id.Phonetype8);
		phonetype8.setOnClickListener(this);

		contactphoneno9Txt.setOnClickListener(this);
		phonetype9 = (TextView) findViewById(R.id.Phonetype9);
		phonetype9.setOnClickListener(this);

		contactphoneno10Txt.setOnClickListener(this);
		phonetype10 = (TextView) findViewById(R.id.Phonetype10);
		phonetype10.setOnClickListener(this);

		contactphoneno11Txt.setOnClickListener(this);
		phonetype11 = (TextView) findViewById(R.id.Phonetype11);
		phonetype11.setOnClickListener(this);

		contactphoneno12Txt.setOnClickListener(this);
		phonetype12 = (TextView) findViewById(R.id.Phonetype12);
		phonetype12.setOnClickListener(this);

		contactphoneno13Txt.setOnClickListener(this);
		phonetype13 = (TextView) findViewById(R.id.Phonetype13);
		phonetype13.setOnClickListener(this);

		contactphoneno14Txt.setOnClickListener(this);
		phonetype14 = (TextView) findViewById(R.id.Phonetype14);
		phonetype14.setOnClickListener(this);

		contactphoneno15Txt.setOnClickListener(this);
		phonetype15 = (TextView) findViewById(R.id.Phonetype15);
		phonetype15.setOnClickListener(this);

		textmessage = (ImageView) findViewById(R.id.textmessage);
		textmessage.setOnClickListener(this);

		textmessage2 = (ImageView) findViewById(R.id.textmessage2);
		textmessage2.setOnClickListener(this);
		textmessage3 = (ImageView) findViewById(R.id.textmessage3);
		textmessage3.setOnClickListener(this);

		textmessage4 = (ImageView) findViewById(R.id.textmessage4);
		textmessage4.setOnClickListener(this);

		textmessage5 = (ImageView) findViewById(R.id.textmessage5);
		textmessage5.setOnClickListener(this);

		textmessage6 = (ImageView) findViewById(R.id.textmessage6);
		textmessage6.setOnClickListener(this);

		textmessage7 = (ImageView) findViewById(R.id.textmessage7);
		textmessage7.setOnClickListener(this);

		textmessage8 = (ImageView) findViewById(R.id.textmessage8);
		textmessage8.setOnClickListener(this);

		textmessage9 = (ImageView) findViewById(R.id.textmessage9);
		textmessage9.setOnClickListener(this);

		textmessage10 = (ImageView) findViewById(R.id.textmessage10);
		textmessage10.setOnClickListener(this);

		textmessage11 = (ImageView) findViewById(R.id.textmessage11);
		textmessage11.setOnClickListener(this);

		textmessage12 = (ImageView) findViewById(R.id.textmessage12);
		textmessage12.setOnClickListener(this);
		textmessage13 = (ImageView) findViewById(R.id.textmessage13);
		textmessage13.setOnClickListener(this);

		textmessage14 = (ImageView) findViewById(R.id.textmessage14);
		textmessage14.setOnClickListener(this);
		textmessage15 = (ImageView) findViewById(R.id.textmessage15);
		textmessage15.setOnClickListener(this);


		contactemailTxt = (TextView) findViewById(R.id.contact_email_value);
		contactemailTxt.setOnClickListener(this);
		emailtype = (TextView) findViewById(R.id.emailtype);

		contactemail2Txt = (TextView) findViewById(R.id.contact_email_value2);
		contactemail2Txt.setOnClickListener(this);
		emailtype2 = (TextView) findViewById(R.id.emailtype2);
		
		contactemail3Txt = (TextView) findViewById(R.id.contact_email_value3);
		contactemail3Txt.setOnClickListener(this);
		emailtype3 = (TextView) findViewById(R.id.emailtype3);

		contactemail4Txt = (TextView) findViewById(R.id.contact_email_value4);
		contactemail4Txt.setOnClickListener(this);
		emailtype4 = (TextView) findViewById(R.id.emailtype4);
		contactemail5Txt = (TextView) findViewById(R.id.contact_email_value5);
		contactemail5Txt.setOnClickListener(this);
		emailtype5 = (TextView) findViewById(R.id.emailtype5);
		contactemail6Txt = (TextView) findViewById(R.id.contact_email_value6);
		contactemail6Txt.setOnClickListener(this);
		emailtype6 = (TextView) findViewById(R.id.emailtype6);
		

		contactimnameTxt = (TextView) findViewById(R.id.contact_im_name_value1);
		imtype = (TextView) findViewById(R.id.imtype1);
		contactimname2Txt = (TextView) findViewById(R.id.contact_im_name_value2);
		imtype2 = (TextView) findViewById(R.id.imtype2);
		contactimname3Txt = (TextView) findViewById(R.id.contact_im_name_value3);
		imtype3 = (TextView) findViewById(R.id.imtype3);
		contactimname4Txt = (TextView) findViewById(R.id.contact_im_name_value4);
		imtype4 = (TextView) findViewById(R.id.imtype4);
		contactimname5Txt = (TextView) findViewById(R.id.contact_im_name_value5);
		imtype5 = (TextView) findViewById(R.id.imtype5);
		contactimname6Txt = (TextView) findViewById(R.id.contact_im_name_value6);
		imtype6 = (TextView) findViewById(R.id.imtype6);

		contactaddrsTxt1 = (TextView) findViewById(R.id.contact_address_value1);
		contactaddrsTxt1.setOnClickListener(this);
		addresstype1 = (TextView) findViewById(R.id.addresstype1);
		addresstype1.setOnClickListener(this);

		contactaddrsTxt2 = (TextView) findViewById(R.id.contact_address_value2);
		contactaddrsTxt2.setOnClickListener(this);
		addresstype2 = (TextView) findViewById(R.id.addresstype2);
		addresstype2.setOnClickListener(this);

		contactaddrsTxt3 = (TextView) findViewById(R.id.contact_address_value3);
		contactaddrsTxt3.setOnClickListener(this);
		addresstype3 = (TextView) findViewById(R.id.addresstype3);
		addresstype3.setOnClickListener(this);

		contactnotesTxt = (TextView) findViewById(R.id.contact_notes_value);
		contactWebsiteTxt = (TextView) findViewById(R.id.website_value);
		contactWebsiteTxt.setOnClickListener(this);
		
		contactNicknameTxt = (TextView) findViewById(R.id.nickname_value);
		contactInternetCallTxt = (TextView) findViewById(R.id.internetcall_value);
		/* contact_name = (TextView) findViewById(R.id.contact_name); */
		contact_designation = (TextView) findViewById(R.id.contact_designation);
		contact_company = (TextView) findViewById(R.id.contact_company);
		contact_comma = (TextView) findViewById(R.id.contact_comma);

		if (cModel != null) {

			if (cModel.getContact_id() > 0) {
				ArrayList<ContactsModel> result;
				if (cModel.isNativeContact()) {
					result = ContactsUtilities
							.getNativeContactsDetailsFromCursor(Email
									.getAppContext().getContentResolver(),
									receivedExtra);
				} else {
					result = ContactsUtilities
							.getContactsDetailsFromCursor(Email
									.getAppContext().getContentResolver(),
									receivedExtra);
				}
			} else {
				pouplateContactDetailsByContactModel();
			}

		} else if (cGalData != null) {
			pouplateContactDetailsByGalModel();
		}

		if (savedInstanceState != null) {
			thumbnail = savedInstanceState.getParcelable("bitmap");
			if (thumbnail != null) {
				AddImage.setImageBitmap(thumbnail);
			}

		}

	}

	OnClickListener immageClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {

			LayoutInflater inflater = (LayoutInflater) ContactsDetailActivity.this
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View layout = inflater.inflate(R.layout.contacts_screen_popup,
					(ViewGroup) findViewById(R.id.popup_element));

			String[] new_array_image = getResources().getStringArray(
					R.array.add_contact_image_array);

			arrayAdapter_image = new ArrayAdapter<String>(
					ContactsDetailActivity.this,
					android.R.layout.simple_list_item_1, new_array_image);

			ListView lv = (ListView) layout.findViewById(R.id.listview);
			lv.setAdapter(arrayAdapter_image);

			if (cGalData == null && !cModel.isNativeContact()
					&& receivedExtra != null && !cModel.isContactfromSearch()) {
				/*
				 * pwindow = new PopupWindow(layout, contact_name.getWidth() /
				 * 2, 4 * contact_name.getHeight(), true);
				 */

				pwindow = new PopupWindow(layout, 4 * AddImage.getWidth(),
						4 * contact_name.getHeight(), true);

				pwindow.setBackgroundDrawable(new BitmapDrawable());

				/*
				 * pwindow.showAtLocation(layout, Gravity.NO_GRAVITY,
				 * (AddImage.getLeft() + AddImage.getWidth()) / 2,
				 * (AddImage.getTop() + AddImage.getHeight()) / 2);
				 */

				// pwindow.showAsDropDown(AddImage, AddImage.getBottom(),
				// AddImage.getLeft());

				pwindow.showAsDropDown(AddImage,  0, -50);

				pwindow.setOutsideTouchable(true);

				lv.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View v,
							int position, long id) {
						// super.onListItemClick(l, v, position, id);
						String name = (String) parent
								.getItemAtPosition(position);
						if (name.equals("Take photo")) {
							Log.i("=========================", "Take photo");
							Intent cameraIntent = new Intent(
									android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

							startActivityForResult(cameraIntent, 1337);

						} else if (name.equals("Choose photo from Gallery")) {

							Intent i = new Intent(
									Intent.ACTION_PICK,
									android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
							startActivityForResult(i, 1);

						}

						pwindow.dismiss();

					}
				});
			}

		}
	};

	private void updateCacheTable() {
		ContentValues updateIDValue = new ContentValues();
		if (receivedExtra != null) {
			Cursor c =Email
					.getAppContext()
					.getContentResolver()
					.query(ContactsConsts.CONTENT_URI_CONTACTS, null,
							"_id = ?", new String[] { receivedExtra }, null);

			if (c != null) {
				if (c.moveToFirst()) {

					if (c.getString(c
							.getColumnIndex(ContactsConsts.CONTACT_SERVER_ID)) != null) {
						String serverid = c
								.getString(c
										.getColumnIndex(ContactsConsts.CONTACT_SERVER_ID));
						ContactsLog.d("serverid", "serverid******" + serverid);
						updateIDValue.put(ContactsConsts.CONTACT_SERVER_ID,
								serverid);
						getContentResolver()
								.insert(ContactsConsts.CONTENT_URI_CONTACTS_LOCAL_CACHE_UPDATE,
										updateIDValue);
					}
				}

				c.close();
			}

		}

		try {
			Cursor c = getContentResolver().query(EmEmailContent.Mailbox.CONTENT_URI,
					null, EmEmailContent.Account.DISPLAY_NAME + "=?",
					new String[] { "Contacts" }, null);

			if (c != null && c.getCount() > 0) {
				c.moveToFirst();

				String Accname = c.getString(c
						.getColumnIndex(EmEmailContent.Mailbox.ACCOUNT_KEY));

				String folderId = c.getString(c
						.getColumnIndex(Mailbox.RECORD_ID));

				EmEmController controller = EmEmController.getInstance(getApplication());
				controller.updateMailbox(Long.parseLong(Accname), Long.parseLong(folderId), null);
				
				SharedPreferences.Editor prefEditor = new SharedPreferences(
						Email.getAppContext()).edit();
				prefEditor.putBoolean("isPrefUpdated", true);
				prefEditor.commit();

			}
			if (c != null) {
				c.close();
			}
		} catch (Exception e) {
			Log.e("Contacts Detail activity update cache table", "menu_manual_sync " + e.toString());

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		try {

			ImageView image = (ImageView) findViewById(R.id.contact_detail_image);
			if (requestCode == 1337 && resultCode == RESULT_OK && data != null) {

				thumbnail = (Bitmap) data.getExtras().get("data");

				if (thumbnail != null) {
					byte[] imageData = ContactsUtilities.getBytes(thumbnail);
					// && imageData.length <= 25600
					if (imageData != null) {
						getContentResolver().update(
								ContactsConsts.CONTENT_URI_CONTACTS,
								contactImageUpdateValues(imageData),
								ContactsConsts.CONTACT_ID + "=?",
								new String[] { receivedExtra });
						if (imageData.length <= 48 * 1024) {
							updateCacheTable();
						}

						image.setImageBitmap(thumbnail);
					} else {
						thumbnail = null;
						Toast.makeText(
								ContactsDetailActivity.this,
								"image is too large please choose a smaller one",
								Toast.LENGTH_SHORT).show();
					}
				}

			} else if (requestCode == 1 && resultCode == RESULT_OK
					&& data != null) {
				Uri selectedImage = data.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };
				Cursor cursor = getContentResolver().query(selectedImage,
						filePathColumn, null, null, null);
				cursor.moveToFirst();
				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String picturePath = cursor.getString(columnIndex);
				cursor.close();

				thumbnail = decodeFile(picturePath);

				if (thumbnail != null) {
					byte[] imageData = ContactsUtilities.getBytes(thumbnail);
					// 1048576
					if (imageData != null) {
						getContentResolver().update(
								ContactsConsts.CONTENT_URI_CONTACTS,
								contactImageUpdateValues(imageData),
								ContactsConsts.CONTACT_ID + "=?",
								new String[] { receivedExtra });
						if (imageData.length <= 48 * 1024) {
							updateCacheTable();
						}

						image.setImageBitmap(thumbnail);
					} else {
						thumbnail = null;
						Toast.makeText(
								ContactsDetailActivity.this,
								"image is too large please choose a smaller one",
								Toast.LENGTH_SHORT).show();
					}
				}

			}

			switch (resultCode) {
			case RESULT_OK:

				CustomRingToneUri = data
						.getParcelableExtra(mRingtoneManager.EXTRA_RINGTONE_PICKED_URI);

				if (CustomRingToneUri != null) {

					getContentResolver().update(
							ContactsConsts.CONTENT_URI_CONTACTS,
							contactRingTone(CustomRingToneUri.toString()),
							ContactsConsts.CONTACT_ID + "=?",
							new String[] { receivedExtra });

					RingtoneManager.setActualDefaultRingtoneUri(this,
							RingtoneManager.TYPE_RINGTONE, CustomRingToneUri);

					Log.i("Sample", "CustomRingToneUri " + CustomRingToneUri);

					break;

				}

			}
		} catch (Exception e) {
			ContactsLog.e("=========================", "ImageLoading failed "
					+ e.toString());
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (thumbnail != null) {
			outState.putParcelable("bitmap", thumbnail);
		}
	}

	private Bitmap decodeFile(String picturePath) {
		try {
			// Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(picturePath), null,
					o);

			// The new size we want to scale to
			final int REQUIRED_SIZE = 150;

			// Find the correct scale value. It should be the power of 2.
			int scale = 1;
			while (o.outWidth / scale / 2 >= REQUIRED_SIZE
					&& o.outHeight / scale / 2 >= REQUIRED_SIZE)
				scale *= 2;

			// Decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			return BitmapFactory.decodeStream(new FileInputStream(picturePath),
					null, o2);
		} catch (FileNotFoundException e) {
		}
		return null;
	}

	public void onClick(View view) {
		// TODO Auto-generated method stub

		switch (view.getId()) {

		case R.id.contact_phone_no_value:
			phonemobile = contactphonenoTxt.getText().toString();
			phonecall();
			break;
		case R.id.contact_phone_no_value2:
			phonemobile = contactphoneno2Txt.getText().toString();
			phonecall();
			break;
		case R.id.contact_phone_no_value3:
			phonemobile = contactphoneno3Txt.getText().toString();
			phonecall();
			break;
		case R.id.contact_phone_no_value4:
			phonemobile = contactphoneno4Txt.getText().toString();
			phonecall();
			break;
		case R.id.contact_phone_no_value5:
			phonemobile = contactphoneno5Txt.getText().toString();
			phonecall();
			break;
		case R.id.contact_phone_no_value6:
			phonemobile = contactphoneno6Txt.getText().toString();
			phonecall();
			break;
		case R.id.contact_phone_no_value7:
			phonemobile = contactphoneno7Txt.getText().toString();
			phonecall();
			break;
		case R.id.contact_phone_no_value8:
			phonemobile = contactphoneno8Txt.getText().toString();
			phonecall();
			break;
		case R.id.contact_phone_no_value9:
			phonemobile = contactphoneno9Txt.getText().toString();
			phonecall();
			break;
		case R.id.contact_phone_no_value10:
			phonemobile = contactphoneno10Txt.getText().toString();
			phonecall();
			break;
		case R.id.contact_phone_no_value11:
			phonemobile = contactphoneno11Txt.getText().toString();
			phonecall();
			break;
		case R.id.contact_phone_no_value12:
			phonemobile = contactphoneno12Txt.getText().toString();
			phonecall();
			break;
		case R.id.contact_phone_no_value13:
			phonemobile = contactphoneno13Txt.getText().toString();
			phonecall();
			break;
		case R.id.contact_phone_no_value14:
			phonemobile = contactphoneno14Txt.getText().toString();
			phonecall();
			break;
		case R.id.contact_phone_no_value15:
			phonemobile = contactphoneno15Txt.getText().toString();
			phonecall();
			break;
		case R.id.Phonetype:
			phonemobile = contactphonenoTxt.getText().toString();
			phonecall();
			break;
		case R.id.Phonetype2:
			phonemobile = contactphoneno2Txt.getText().toString();
			phonecall();
			break;
		case R.id.Phonetype3:
			phonemobile = contactphoneno3Txt.getText().toString();
			phonecall();
			break;
		case R.id.Phonetype4:
			phonemobile = contactphoneno4Txt.getText().toString();
			phonecall();
			break;
		case R.id.Phonetype5:
			phonemobile = contactphoneno5Txt.getText().toString();
			phonecall();
			break;
		case R.id.Phonetype6:
			phonemobile = contactphoneno6Txt.getText().toString();
			phonecall();
			break;
		case R.id.Phonetype7:
			phonemobile = contactphoneno7Txt.getText().toString();
			phonecall();
			break;
		case R.id.Phonetype8:
			phonemobile = contactphoneno8Txt.getText().toString();
			phonecall();
			break;
		case R.id.Phonetype9:
			phonemobile = contactphoneno9Txt.getText().toString();
			phonecall();
			break;
		case R.id.Phonetype10:
			phonemobile = contactphoneno10Txt.getText().toString();
			phonecall();
			break;
		case R.id.Phonetype11:
			phonemobile = contactphoneno11Txt.getText().toString();
			phonecall();
			break;
		case R.id.Phonetype12:
			phonemobile = contactphoneno12Txt.getText().toString();
			phonecall();
			break;
		case R.id.Phonetype13:
			phonemobile = contactphoneno13Txt.getText().toString();
			phonecall();
			break;
		case R.id.Phonetype14:
			phonemobile = contactphoneno14Txt.getText().toString();
			phonecall();
			break;
		case R.id.Phonetype15:
			phonemobile = contactphoneno15Txt.getText().toString();
			phonecall();
			break;
		case R.id.textmessage:
			phonemobile = contactphonenoTxt.getText().toString();			
			ContactName = contact_name.getText().toString();			
			sendsms();
			break;
		case R.id.textmessage2:
			phonemobile = contactphoneno2Txt.getText().toString();
			ContactName = contact_name.getText().toString();
			sendsms();
			break;
		case R.id.textmessage3:
			phonemobile = contactphoneno3Txt.getText().toString();
			ContactName = contact_name.getText().toString();
			sendsms();
			break;
		case R.id.textmessage4:
			phonemobile = contactphoneno4Txt.getText().toString();
			ContactName = contact_name.getText().toString();
			sendsms();
			break;
		case R.id.textmessage5:
			phonemobile = contactphoneno5Txt.getText().toString();
			ContactName = contact_name.getText().toString();
			sendsms();

			break;
		case R.id.textmessage6:
			phonemobile = contactphoneno6Txt.getText().toString();
			ContactName = contact_name.getText().toString();
			sendsms();
			break;
		case R.id.textmessage7:
			phonemobile = contactphoneno7Txt.getText().toString();
			ContactName = contact_name.getText().toString();
			sendsms();
			break;
		case R.id.textmessage8:
			phonemobile = contactphoneno8Txt.getText().toString();
			ContactName = contact_name.getText().toString();
			sendsms();
			break;
		case R.id.textmessage9:
			phonemobile = contactphoneno9Txt.getText().toString();
			ContactName = contact_name.getText().toString();
			sendsms();
			break;
		case R.id.textmessage10:
			phonemobile = contactphoneno10Txt.getText().toString();
			ContactName = contact_name.getText().toString();
			sendsms();
			break;
		case R.id.textmessage11:
			phonemobile = contactphoneno11Txt.getText().toString();
			ContactName = contact_name.getText().toString();
			sendsms();
			break;
		case R.id.textmessage12:
			phonemobile = contactphoneno12Txt.getText().toString();
			ContactName = contact_name.getText().toString();
			sendsms();
			break;
		case R.id.textmessage13:
			phonemobile = contactphoneno13Txt.getText().toString();
			ContactName = contact_name.getText().toString();
			sendsms();
			break;
		case R.id.textmessage14:
			phonemobile = contactphoneno14Txt.getText().toString();
			ContactName = contact_name.getText().toString();
			sendsms();
			break;
		case R.id.textmessage15:
			phonemobile = contactphoneno15Txt.getText().toString();
			ContactName = contact_name.getText().toString();
			sendsms();
			break;
	
		//case R.id.Emailid:
		case R.id.contact_email_value:
			
			primemail = contactemailTxt.getText().toString();
			
			if(isEmailValid(primemail))		
				sendmail();
			else
				Toast.makeText(this, "Email-ID is not valid",Toast.LENGTH_LONG).show();
			break;
		case R.id.contact_email_value2:
			primemail = contactemail2Txt.getText().toString();
			if(isEmailValid(primemail))		
				sendmail();
			else
				Toast.makeText(this, "Email-ID is not valid",Toast.LENGTH_LONG).show();
			break;
		case R.id.contact_email_value3:
			primemail = contactemail3Txt.getText().toString();
			if(isEmailValid(primemail))		
				sendmail();
			else
				Toast.makeText(this, "Email-ID is not valid",Toast.LENGTH_LONG).show();
			break;
		case R.id.contact_email_value4:
			primemail = contactemail4Txt.getText().toString();
			if(isEmailValid(primemail))		
				sendmail();
			else
				Toast.makeText(this, "Email-ID is not valid",Toast.LENGTH_LONG).show();
			break;
		case R.id.contact_email_value5:
			primemail = contactemail5Txt.getText().toString();
			if(isEmailValid(primemail))		
				sendmail();
			else
				Toast.makeText(this, "Email-ID is not valid",Toast.LENGTH_LONG).show();
			break;
		case R.id.contact_email_value6:
			primemail = contactemail6Txt.getText().toString();
			if(isEmailValid(primemail))		
				sendmail();
			else
				Toast.makeText(this, "Email-ID is not valid",
						Toast.LENGTH_LONG).show();
				
			break;

	case R.id.contact_address_value1:
			showMap(addrsMap);
			break;
		case R.id.contact_address_value2:
			showMap(addrsMap2);
			break;
		case R.id.contact_address_value3:
			showMap(addrsMap3);
			break;

		case R.id.addresstype1:
			showMap(addrsMap);
			break;
		case R.id.addresstype2:
			showMap(addrsMap2);
			break;
		case R.id.addresstype3:
			showMap(addrsMap3);
			break;

			
		case R.id.website_value:
			
			
			SecuredBrowserUrl =  contactWebsiteTxt.getText().toString();
			
			CallSecuredBrowser();

		default:
			break;
		}

	}

	public void CallSecuredBrowser()
	{

		
	/*	if (!SecuredBrowserUrl.startsWith("_http://")
	            && !SecuredBrowserUrl.startsWith("_https://")) {
			SecuredBrowserUrl = "http://" + SecuredBrowserUrl;
	}*/

	Intent openWebAppIntent = new Intent("com.cognizant.trumobi.securebrowser");
	if (openWebAppIntent != null) {
	      openWebAppIntent.putExtra("toBrowser",SecuredBrowserUrl);
	      //openWebAppIntent.putExtra("name",SecuredBrowserUrl);
	      //openWebAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	     // openWebAppIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
	      try {
	   	  // activity.startActivity(openWebAppIntent);
	    	  this.startActivity(openWebAppIntent);
	   	   //result = true;
	      }catch (ActivityNotFoundException ex) {
	          // No applications can handle it.  Ignore.
	      }
	}  

	}
	
	private void showMap(String address) {
		try {

			Log.e("Addressmap after formed", address);

			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(
					"geo:0,0?q=%s", URLEncoder.encode(address))));
			startActivity(i);
		} catch (ActivityNotFoundException activityNotFound) {

			Toast.makeText(this,
					"Please install Google-Maps App, to view the map",
					Toast.LENGTH_LONG).show();

		}
	}

	
	private void phonecall() {
		
		
		/*
		TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		
		if (tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT){
			
			isAirplaneEnabled = Settings.System.getInt(this.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1;
			if(isAirplaneEnabled){
				Toast.makeText(this, "Flight mode on. Turn Flight mode off to make calls", Toast.LENGTH_SHORT).show();
			}else{
				//Toast.makeText(this, "phone has SIM card", Toast.LENGTH_SHORT).show();
				//DialerParentActivity.frmCorporate=true;
				DialerUtilities.phoneCallIntent(phonemobile, this);

			}
			
		} else {
			
			Toast.makeText(this, "Not registered on network", Toast.LENGTH_SHORT).show();	
		}
		*/
		
		
		

		TelephonyManager tm = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);

		if(tm.getPhoneType()==TelephonyManager.PHONE_TYPE_NONE)
		{
			if (tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT) {
				 //coming here if Tablet WITHOUT SIM SLOT 
				Toast.makeText(this, "Not registered on network",
						Toast.LENGTH_SHORT).show();
			}else{
				 //coming here if Tablet WITH SIM SLOT 
				/*Toast.makeText(this, "TAB",
						Toast.LENGTH_SHORT).show();*/
				if (tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT) {

					isAirplaneEnabled = Settings.System.getInt(this
							.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1;
					if (isAirplaneEnabled) {
						Toast.makeText(this,
								"Flight mode on. Turn Flight mode off to make calls",
								Toast.LENGTH_SHORT).show();
					} else {
						
						SharedPreferences.Editor prefEditor = new SharedPreferences(
								Email.getAppContext()).edit();
						prefEditor.putBoolean("isCallLogUpdated", true);
						prefEditor.commit();
						
						DialerUtilities.phoneCallIntent(phonemobile,
								this);
					}

				} else {

					Toast.makeText(this, "Not registered on network",
							Toast.LENGTH_SHORT).show();
				}
			}
		 
		}
		else{
			/*Toast.makeText(this, "phone",
					Toast.LENGTH_SHORT).show();*/
			//coming here if phone
			if (tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT) {

				isAirplaneEnabled = Settings.System.getInt(this
						.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1;
				if (isAirplaneEnabled) {
					Toast.makeText(this,
							"Flight mode on. Turn Flight mode off to make calls",
							Toast.LENGTH_SHORT).show();
				} else {
					
					SharedPreferences.Editor prefEditor = new SharedPreferences(
							Email.getAppContext()).edit();
					prefEditor.putBoolean("isCallLogUpdated", true);
					prefEditor.commit();
					
					DialerUtilities.phoneCallIntent(phonemobile,
							this);
				}

			} else {

				Toast.makeText(this, "Not registered on network",
						Toast.LENGTH_SHORT).show();
			}
		}
		
		
	}

	private void sendsms() {
		
/*
		TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		
		if (tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT){
			
			isAirplaneEnabled = Settings.System.getInt(this.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1;
			if(isAirplaneEnabled){
				Toast.makeText(this, "Flight mode on. Turn Flight mode off to make calls", Toast.LENGTH_SHORT).show();
			}else{
				//Toast.makeText(this, "phone has SIM card", Toast.LENGTH_SHORT).show();
				try {

					Intent callSMS = new Intent(this, SmsMain.class);
					callSMS.putExtra("con_name", ContactName);
					callSMS.putExtra("con_phone", phonemobile);

					if (imageBytes != null) {
						callSMS.putExtra("con_image", imageBytes);
					} else {
						// callSMS.putExtra("con_image","null");
						callSMS.putExtra("con_image", imageBytes);
						//Need to pass null, once messenger team conforms, what format they need null
						
					}
					startActivity(callSMS);

				} catch (ActivityNotFoundException activityException) {
					Log.e("sendsms activity not found", "Call failed");
				}


			}
			
		} else {
			
			Toast.makeText(this, "Not registered on network", Toast.LENGTH_SHORT).show();	
		}
	
	
	
	*/
	
	
		

		TelephonyManager tm = (TelephonyManager) this
				.getSystemService(Context.TELEPHONY_SERVICE);

		if(tm.getPhoneType()==TelephonyManager.PHONE_TYPE_NONE)
		{
			if (tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT) {
				
				Toast.makeText(this, "Not registered on network",
						Toast.LENGTH_SHORT).show();
			}else{
				
				if (tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT) {

					isAirplaneEnabled = Settings.System.getInt(this
							.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1;
					if (isAirplaneEnabled) {
						Toast.makeText(this,
								"Flight mode on. Turn Flight mode off to make calls",
								Toast.LENGTH_SHORT).show();
					} else {
						try {
							Intent callSMS = new Intent(this,
									SmsMain.class);
							callSMS.putExtra("con_name", ContactName);
							callSMS.putExtra("con_phone", phonemobile);

							if (imageBytes != null) {
								callSMS.putExtra("con_image", imageBytes);
							} else {
								// callSMS.putExtra("con_image","null");
								callSMS.putExtra("con_image", imageBytes);

							}
							this.startActivity(callSMS);

						} catch (ActivityNotFoundException activityException) {
							Log.e("sendsms activity not found", "Call failed");
						}
					}

				} else {

					Toast.makeText(this, "Not registered on network",
							Toast.LENGTH_SHORT).show();
				}
			}
		 
		}
		else{
			/*Toast.makeText(this, "phone",
					Toast.LENGTH_SHORT).show();*/
			//coming here if phone
			if (tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT) {

				isAirplaneEnabled = Settings.System.getInt(this
						.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1;
				if (isAirplaneEnabled) {
					Toast.makeText(this,
							"Flight mode on. Turn Flight mode off to make calls",
							Toast.LENGTH_SHORT).show();
				} else {
					try {
						Intent callSMS = new Intent(this,
								SmsMain.class);
						callSMS.putExtra("con_name", ContactName);
						callSMS.putExtra("con_phone", phonemobile);

						if (imageBytes != null) {
							callSMS.putExtra("con_image", imageBytes);
						} else {
							// callSMS.putExtra("con_image","null");
							callSMS.putExtra("con_image", imageBytes);
							// Need to pass null, once messenger team conforms, what
							// format they need null

						}
						this.startActivity(callSMS);

					} catch (ActivityNotFoundException activityException) {
						Log.e("sendsms activity not found", "Call failed");
					}
				}

			} else {

				Toast.makeText(this, "Not registered on network",
						Toast.LENGTH_SHORT).show();
			}
		}
		

	}

	
	public static boolean isEmailValid(String email) {
		
		
		return true;

		/*boolean isValid = false;

		String TempMail = email;
		String TempMailbackup = email;

		String[] TempMailbackup1;
		String[] TempMailbackup2;
		String ServerMail1;
		String ServerMail2;

		if (TempMailbackup.contains("\"") && TempMailbackup.contains(">")) {
			if (TempMailbackup.startsWith("\"") && TempMailbackup.endsWith(">")) {
				Log.e("TempMailbackup", "TempMailbackup" + TempMailbackup);
				if (TempMailbackup.contains("\" <")) {
					TempMailbackup1 = TempMailbackup.split("\" <");
					Log.e("TempMailbackup1", "" + TempMailbackup1[0]);
					Log.e("TempMailbackup1", "" + TempMailbackup1[1]);

					ServerMail1 = TempMailbackup1[0].substring(1);
					ServerMail2 = TempMailbackup1[1].substring(0,
							TempMailbackup1[1].length() - 1);
					Log.e("ServerMail1", "" + ServerMail1);
					Log.e("ServerMail2", "" + ServerMail2);
					if (ServerMail1.equals(ServerMail2)) {
						String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";

						Pattern pattern = Pattern.compile(expression,
								Pattern.CASE_INSENSITIVE);
						Matcher matcher = pattern.matcher(ServerMail1);
						if (matcher.matches()) {
							isValid = true;
						}
					} else {
						String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
						CharSequence inputStr = email;

						Pattern pattern = Pattern.compile(expression,
								Pattern.CASE_INSENSITIVE);
						Matcher matcher = pattern.matcher(inputStr);
						if (matcher.matches()) {
							isValid = true;
						}
					}

				} else {
					String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
					CharSequence inputStr = email;

					Pattern pattern = Pattern.compile(expression,
							Pattern.CASE_INSENSITIVE);
					Matcher matcher = pattern.matcher(inputStr);
					if (matcher.matches()) {
						isValid = true;
					}
				}

			} else {
				String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
				CharSequence inputStr = email;

				Pattern pattern = Pattern.compile(expression,
						Pattern.CASE_INSENSITIVE);
				Matcher matcher = pattern.matcher(inputStr);
				if (matcher.matches()) {
					isValid = true;
				}
			}

		} else {
			String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
			CharSequence inputStr = email;

			Pattern pattern = Pattern.compile(expression,
					Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(inputStr);
			if (matcher.matches()) {
				isValid = true;
			}
		}

		return isValid;*/

	}

	private void sendmail() {

		try {
			Intent targetedShareIntent = new Intent(this,
					EmMessageCompose.class);
			targetedShareIntent.putExtra("ContactsAppHandshake",
					"ContactsAppHandshake");
			// targetedShareIntent.putExtra(Intent.EXTRA_EMAIL," ");
			targetedShareIntent.putExtra(Intent.EXTRA_SUBJECT, "");
			targetedShareIntent.putExtra(Intent.EXTRA_TEXT, "");
			targetedShareIntent.setData(Uri.parse("mailto:" + primemail));

			targetedShareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(targetedShareIntent);

		} catch (ActivityNotFoundException activityException) {
			Log.e("Secured apps not available to send Email", "Call failed");
			Toast.makeText(ContactsDetailActivity.this,
					"Secured apps not available to send Email",
					Toast.LENGTH_LONG).show();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();

		if (cModel != null) {

			ArrayList<ContactsModel> result = null;
			if (cModel.isNativeContact()) {

				result = ContactsUtilities.getNativeContactsDetailsFromCursor(
						Email.getAppContext().getContentResolver(),
						String.valueOf(cModel.getContact_id()));

			} else if (cModel.isContactfromSearch()) {
				result = new ArrayList<ContactsModel>();
				result.add(cModel);
			} else {
				result = ContactsUtilities.getContactsDetailsFromCursor(
						Email.getAppContext().getContentResolver(),
						receivedExtra);
			}
			if (result != null && result.size() > 0) {

				pouplateContactDetailsByModel(result);

			}
		} else if (cGalData != null) {
			pouplateContactDetailsByGalModel();

		}

	}

	Cursor getCursor() {
		return getContentResolver().query(ContactsConsts.CONTENT_URI_CONTACTS,
				null, selectionArgument, new String[] { receivedExtra }, null);
	}

	private void pouplateContactDetailsByModel(ArrayList<ContactsModel> result) {

		getSupportActionBar().setTitle(result.get(0).getcontacts_first_name());

		String viewOrder = new SharedPreferences(getApplicationContext())
				.getString("prefViewType", "First_name_first");
		String sortOrder = new SharedPreferences(getApplicationContext())
				.getString("prefSortOrder", ContactsConsts.CONTACT_FIRST_NAME);

		if (viewOrder.equals("First_name_first")) {

			if (result.get(0).getcontacts_first_name() != null
					&& !result.get(0).getcontacts_first_name().isEmpty()) {
				if (result.get(0).getcontacts_last_name() != null
						&& !result.get(0).getcontacts_last_name().isEmpty()) {
					if (result.get(0).getcontacts_middle_name() != null
							&& !result.get(0).getcontacts_middle_name()
									.isEmpty())

					{
						contact_name.setText(result.get(0)
								.getcontacts_first_name()
								+ " "
								+ result.get(0).getcontacts_middle_name()
								+ " "
								+ result.get(0).getcontacts_last_name());
						getSupportActionBar().setTitle(
								result.get(0).getcontacts_first_name());
						getSupportActionBar()
								.setSubtitle(
										result.get(0).getcontacts_middle_name()
												+ " "
												+ result.get(0)
														.getcontacts_last_name());
					} else {
						contact_name.setText(result.get(0)
								.getcontacts_first_name()
								+ " "
								+ result.get(0).getcontacts_last_name());
						getSupportActionBar().setTitle(
								result.get(0).getcontacts_first_name());
						getSupportActionBar().setSubtitle(
								result.get(0).getcontacts_last_name());

					}

				} else {
					if (result.get(0).getcontacts_middle_name() != null
							&& !result.get(0).getcontacts_middle_name()
									.isEmpty())

					{
						contact_name.setText(result.get(0)
								.getcontacts_first_name()
								+ " "
								+ result.get(0).getcontacts_middle_name());
						getSupportActionBar().setTitle(
								result.get(0).getcontacts_first_name());
						getSupportActionBar().setSubtitle(
								result.get(0).getcontacts_middle_name());
					} else {
						contact_name.setText(result.get(0)
								.getcontacts_first_name());
						getSupportActionBar().setTitle(
								result.get(0).getcontacts_first_name());
					}
				}
			} else {
				if (result.get(0).getcontacts_last_name() != null
						&& !result.get(0).getcontacts_last_name().isEmpty()) {

					if (result.get(0).getcontacts_middle_name() != null
							&& !result.get(0).getcontacts_middle_name()
									.isEmpty())

					{
						getSupportActionBar()
								.setTitle(
										result.get(0).getcontacts_middle_name()
												+ " "
												+ result.get(0)
														.getcontacts_last_name());
						contact_name.setText(result.get(0)
								.getcontacts_middle_name()
								+ " "
								+ result.get(0).getcontacts_last_name());
					} else {

						getSupportActionBar().setTitle(
								result.get(0).getcontacts_last_name());
						contact_name.setText(result.get(0)
								.getcontacts_last_name());
					}
				} else {
					if (result.get(0).getcontacts_middle_name() != null
							&& !result.get(0).getcontacts_middle_name()
									.isEmpty())

					{
						getSupportActionBar().setTitle(
								result.get(0).getcontacts_middle_name());
						contact_name.setText(result.get(0)
								.getcontacts_middle_name());
					} else {
						getSupportActionBar().setTitle("Empty");
						contact_name.setText("Empty");
					}
				}
			}

			frstname = result.get(0).getcontacts_first_name();
			surname = result.get(0).getcontacts_last_name();

		} else if (viewOrder.equals("Last_name_first")) {

			if (result.get(0).getcontacts_last_name() != null
					&& !result.get(0).getcontacts_last_name().isEmpty()) {
				if (result.get(0).getcontacts_first_name() != null
						&& !result.get(0).getcontacts_first_name().isEmpty()) {
					if (result.get(0).getcontacts_middle_name() != null
							&& !result.get(0).getcontacts_middle_name()
									.isEmpty()) {
						contact_name.setText(result.get(0)
								.getcontacts_last_name()
								+ ", "
								+ result.get(0).getcontacts_middle_name()
								+ " "
								+ result.get(0).getcontacts_first_name());
						getSupportActionBar().setTitle(
								result.get(0).getcontacts_last_name());
						getSupportActionBar().setSubtitle(
								result.get(0).getcontacts_middle_name()
										+ " "
										+ result.get(0)
												.getcontacts_first_name());
					} else {
						contact_name.setText(result.get(0)
								.getcontacts_last_name()
								+ ", "
								+ result.get(0).getcontacts_first_name());
						getSupportActionBar().setTitle(
								result.get(0).getcontacts_last_name());
						getSupportActionBar().setSubtitle(
								result.get(0).getcontacts_first_name());

					}

				} else {
					// contact_name.setText(result.get(0).getcontacts_last_name());
					if (result.get(0).getcontacts_middle_name() != null
							&& !result.get(0).getcontacts_middle_name()
									.isEmpty()) {
						contact_name.setText(result.get(0)
								.getcontacts_last_name()
								+ ", "
								+ result.get(0).getcontacts_middle_name());
						getSupportActionBar().setTitle(
								result.get(0).getcontacts_last_name()
										+ ", "
										+ result.get(0)
												.getcontacts_middle_name());
					} else {
						contact_name.setText(result.get(0)
								.getcontacts_last_name());
						getSupportActionBar().setTitle(
								result.get(0).getcontacts_last_name());
					}

				}
			} else {

				if (result.get(0).getcontacts_first_name() != null
						&& !result.get(0).getcontacts_first_name().isEmpty()) {

					if (result.get(0).getcontacts_middle_name() != null
							&& !result.get(0).getcontacts_middle_name()
									.isEmpty())

					{
						getSupportActionBar().setTitle(
								result.get(0).getcontacts_first_name()
										+ " "
										+ result.get(0)
												.getcontacts_middle_name());
						contact_name.setText(result.get(0)
								.getcontacts_first_name()
								+ " "
								+ result.get(0).getcontacts_middle_name());

					} else {

						getSupportActionBar().setTitle(
								result.get(0).getcontacts_first_name());
						contact_name.setText(result.get(0)
								.getcontacts_first_name());
					}
				} else {
					if (result.get(0).getcontacts_middle_name() != null
							&& !result.get(0).getcontacts_middle_name()
									.isEmpty())

					{
						getSupportActionBar().setTitle(
								result.get(0).getcontacts_middle_name());
						contact_name.setText(result.get(0)
								.getcontacts_middle_name());
					} else {
						getSupportActionBar().setTitle("Empty");
						contact_name.setText("Empty");
					}
				}

				/*
				 * getSupportActionBar().setTitle(result.get(0).
				 * getcontacts_first_name());
				 * contact_name.setText(result.get(0).getcontacts_first_name());
				 */

			}
			frstname = result.get(0).getcontacts_first_name();
			surname = result.get(0).getcontacts_last_name();

		}

		/*
		 * if (result.get(0).getcontacts_last_name() != null &&
		 * !result.get(0).getcontacts_last_name().isEmpty()) {
		 * getSupportActionBar
		 * ().setSubtitle(result.get(0).getcontacts_last_name());
		 * 
		 * contact_name.setText(result.get(0).getcontacts_first_name() + " " +
		 * result.get(0).getcontacts_last_name()); } else {
		 * contact_name.setText(result.get(0).getcontacts_first_name()); }
		 */

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setIcon(R.drawable.pr_contacts_icon);

		if (result.get(0).getcontacts_first_name() != null
				&& !result.get(0).getcontacts_first_name().isEmpty()) {
			contactphoneticTxt.setText(result.get(0).getcontacts_first_name());
			// l_contactprimaryTxt.setVisibility(View.VISIBLE);
		}

		if (result.get(0).getcontacts_last_name() != null
				&& !result.get(0).getcontacts_last_name().isEmpty()) {
			contactnickTxt.setText(result.get(0).getcontacts_last_name());
			// l_contactsecondaryTxt.setVisibility(View.VISIBLE);
		}

		if (result.get(0).getcontacts_title() != null
				&& !result.get(0).getcontacts_title().isEmpty()) {

			Sharetitle = result.get(0).getcontacts_title();
			contact_designation.setText(result.get(0).getcontacts_title());
			contact_designation.setVisibility(View.VISIBLE);
			contact_comma.setVisibility(View.GONE);
			l_desig_comp.setVisibility(View.VISIBLE);
		} else {
			contact_designation.setText("");
			contact_designation.setVisibility(View.GONE);
			contact_comma.setVisibility(View.GONE);
			l_desig_comp.setVisibility(View.VISIBLE);
		}

		String company_details = "";
		if (result.get(0).getcontacts_department() != null
				&& !result.get(0).getcontacts_department().isEmpty()) {
			company_details = company_details
					+ result.get(0).getcontacts_department();
		}
		if (result.get(0).getcontacts_company_name() != null
				&& !result.get(0).getcontacts_company_name().isEmpty()) {
			company_details = company_details + " "
					+ result.get(0).getcontacts_company_name();
		}

		if (company_details != null && !company_details.isEmpty()) {
			contact_company.setText(company_details);
			org = company_details;
			contact_company.setVisibility(View.VISIBLE);
			l_desig_comp.setVisibility(View.VISIBLE);
		} else {
			contact_company.setText("");
			contact_company.setVisibility(View.GONE);
			l_desig_comp.setVisibility(View.VISIBLE);
		}

		// Phone type 1 data population
		if (result.get(0).getcontacts_mobile_telephone_number() != null
				&& !result.get(0).getcontacts_mobile_telephone_number()
						.isEmpty()) {

			contactphonenoTxt.setText(result.get(0)
					.getcontacts_mobile_telephone_number());
			phonetype.setText(result.get(0)
					.getContact_phone_number_mobile_type1());
			phonemobile = result.get(0).getcontacts_mobile_telephone_number();

			l_contact_phone_no.setVisibility(View.VISIBLE);
			l_contactphonenoTxt.setVisibility(View.VISIBLE);
		} else {
			contactphonenoTxt.setText("");
			l_contactphonenoTxt.setVisibility(View.GONE);
		} /* Phone type 2 data population */
		if (result.get(0).getcontacts_business_telephone_number() != null
				&& !result.get(0).getcontacts_business_telephone_number()
						.isEmpty()) {
			contactphoneno2Txt.setText(result.get(0)
					.getcontacts_business_telephone_number());

			SharePhone1 = result.get(0).getcontacts_business_telephone_number();

			phonetype2.setText(result.get(0)
					.getContact_phone_number_mobile_type2());
			phonemobile = result.get(0).getcontacts_business_telephone_number();
			l_contact_phone_no.setVisibility(View.VISIBLE);
			l_contactphoneno2Txt.setVisibility(View.VISIBLE);
		} else {
			contactphoneno2Txt.setText("");
			l_contactphoneno2Txt.setVisibility(View.GONE);
		}
		if (result.get(0).getcontacts_assistant_telephone_number() != null
				&& !result.get(0).getcontacts_assistant_telephone_number()
						.isEmpty()) {
			contactphoneno3Txt.setText(result.get(0)
					.getcontacts_assistant_telephone_number());

			phonetype3.setText(result.get(0)
					.getContacts_assistant_telephone_number_type());
			phonemobile = result.get(0)
					.getcontacts_assistant_telephone_number();
			l_contact_phone_no.setVisibility(View.VISIBLE);
			l_contactphoneno3Txt.setVisibility(View.VISIBLE);
		} else {
			contactphoneno3Txt.setText("");
			l_contactphoneno3Txt.setVisibility(View.GONE);
		}
		if (result.get(0).getcontacts_business2_telephone_number() != null
				&& !result.get(0).getcontacts_business2_telephone_number()
						.isEmpty()) {
			contactphoneno4Txt.setText(result.get(0)
					.getcontacts_business2_telephone_number());

			phonetype4.setText(result.get(0)
					.getContacts_business2_telephone_number_type());
			phonemobile = result.get(0)
					.getcontacts_business2_telephone_number();
			l_contact_phone_no.setVisibility(View.VISIBLE);
			l_contactphoneno4Txt.setVisibility(View.VISIBLE);
		} else {
			contactphoneno4Txt.setText("");
			l_contactphoneno4Txt.setVisibility(View.GONE);
		}
		if (result.get(0).getcontacts_business_fax_number() != null
				&& !result.get(0).getcontacts_business_fax_number().isEmpty()) {
			contactphoneno5Txt.setText(result.get(0)
					.getcontacts_business_fax_number());

			phonetype5.setText(result.get(0)
					.getContacts_business_fax_number_type());
			phonemobile = result.get(0).getcontacts_business_fax_number();
			l_contact_phone_no.setVisibility(View.VISIBLE);
			l_contactphoneno5Txt.setVisibility(View.VISIBLE);
		} else {
			contactphoneno5Txt.setText("");
			l_contactphoneno5Txt.setVisibility(View.GONE);
		}
		if (result.get(0).getcontacts_car_telephone_number() != null
				&& !result.get(0).getcontacts_car_telephone_number().isEmpty()) {
			contactphoneno6Txt.setText(result.get(0)
					.getcontacts_car_telephone_number());

			phonetype6.setText(result.get(0)
					.getContacts_car_telephone_numbe_type());
			phonemobile = result.get(0).getcontacts_car_telephone_number();
			l_contact_phone_no.setVisibility(View.VISIBLE);
			l_contactphoneno6Txt.setVisibility(View.VISIBLE);
		} else {
			contactphoneno6Txt.setText("");
			l_contactphoneno6Txt.setVisibility(View.GONE);
		}
		if (result.get(0).getcontacts_home2_telephone_number() != null
				&& !result.get(0).getcontacts_home2_telephone_number()
						.isEmpty()) {
			contactphoneno7Txt.setText(result.get(0)
					.getcontacts_home2_telephone_number());

			phonetype7.setText(result.get(0)
					.getContacts_home2_telephone_number_type());
			phonemobile = result.get(0).getcontacts_home2_telephone_number();
			l_contact_phone_no.setVisibility(View.VISIBLE);
			l_contactphoneno7Txt.setVisibility(View.VISIBLE);
		} else {
			contactphoneno7Txt.setText("");
			l_contactphoneno7Txt.setVisibility(View.GONE);
		}
		if (result.get(0).getcontacts_home_fax_number() != null
				&& !result.get(0).getcontacts_home_fax_number().isEmpty()) {
			contactphoneno8Txt.setText(result.get(0)
					.getcontacts_home_fax_number());

			phonetype8
					.setText(result.get(0).getContacts_home_fax_number_type());
			phonemobile = result.get(0).getcontacts_home_fax_number();
			l_contact_phone_no.setVisibility(View.VISIBLE);
			l_contactphoneno8Txt.setVisibility(View.VISIBLE);
		} else {
			contactphoneno8Txt.setText("");
			l_contactphoneno8Txt.setVisibility(View.GONE);
		}
		if (result.get(0).getcontacts_home_telephone_number() != null
				&& !result.get(0).getcontacts_home_telephone_number().isEmpty()) {
			contactphoneno9Txt.setText(result.get(0)
					.getcontacts_home_telephone_number());
			SharePhone2 = result.get(0).getcontacts_home_telephone_number();

			phonetype9.setText(result.get(0)
					.getContacts_home_telephone_number_type());
			phonemobile = result.get(0).getcontacts_home_telephone_number();
			l_contact_phone_no.setVisibility(View.VISIBLE);
			l_contactphoneno9Txt.setVisibility(View.VISIBLE);
		} else {
			contactphoneno9Txt.setText("");
			l_contactphoneno9Txt.setVisibility(View.GONE);
		}
		if (result.get(0).getcontacts_pager_number() != null
				&& !result.get(0).getcontacts_pager_number().isEmpty()) {
			contactphoneno10Txt.setText(result.get(0)
					.getcontacts_pager_number());

			phonetype10.setText(result.get(0).getContacts_pager_number_type());
			phonemobile = result.get(0).getcontacts_pager_number();
			l_contact_phone_no.setVisibility(View.VISIBLE);
			l_contactphoneno10Txt.setVisibility(View.VISIBLE);
		} else {
			contactphoneno10Txt.setText("");
			l_contactphoneno10Txt.setVisibility(View.GONE);
		}
		if (result.get(0).getcontacts_radio_telephone_number() != null
				&& !result.get(0).getcontacts_radio_telephone_number()
						.isEmpty()) {
			contactphoneno11Txt.setText(result.get(0)
					.getcontacts_radio_telephone_number());

			phonetype11.setText(result.get(0)
					.getContacts_radio_telephone_number_type());
			phonemobile = result.get(0).getcontacts_radio_telephone_number();
			l_contact_phone_no.setVisibility(View.VISIBLE);
			l_contactphoneno11Txt.setVisibility(View.VISIBLE);
		} else {
			contactphoneno11Txt.setText("");
			l_contactphoneno11Txt.setVisibility(View.GONE);
		}
		if (result.get(0).getContacts2_company_main_phone() != null
				&& !result.get(0).getContacts2_company_main_phone().isEmpty()) {
			contactphoneno12Txt.setText(result.get(0)
					.getContacts2_company_main_phone());

			phonetype12.setText(result.get(0)
					.getContacts2_company_main_phone_type());
			phonemobile = result.get(0).getContacts2_company_main_phone();
			l_contact_phone_no.setVisibility(View.VISIBLE);
			l_contactphoneno12Txt.setVisibility(View.VISIBLE);
		} else {
			contactphoneno12Txt.setText("");
			l_contactphoneno12Txt.setVisibility(View.GONE);
		}
		if (result.get(0).getContact_custom_phone1() != null
				&& !result.get(0).getContact_custom_phone1().isEmpty()) {
			contactphoneno13Txt.setText(result.get(0)
					.getContact_custom_phone1());

			phonetype13.setText(result.get(0).getContact_custom_phone1_type());
			phonemobile = result.get(0).getContact_custom_phone1();
			l_contact_phone_no.setVisibility(View.VISIBLE);
			l_contactphoneno13Txt.setVisibility(View.VISIBLE);
		} else {
			contactphoneno13Txt.setText("");
			l_contactphoneno13Txt.setVisibility(View.GONE);
		}
		if (result.get(0).getContact_custom_phone2() != null
				&& !result.get(0).getContact_custom_phone2().isEmpty()) {
			contactphoneno14Txt.setText(result.get(0)
					.getContact_custom_phone2());

			phonetype14.setText(result.get(0).getContact_custom_phone2_type());
			phonemobile = result.get(0).getContact_custom_phone2();
			l_contact_phone_no.setVisibility(View.VISIBLE);
			l_contactphoneno14Txt.setVisibility(View.VISIBLE);
		} else {
			contactphoneno14Txt.setText("");
			l_contactphoneno14Txt.setVisibility(View.GONE);
		}
		if (result.get(0).getContact_custom_phone3() != null
				&& !result.get(0).getContact_custom_phone3().isEmpty()) {
			contactphoneno15Txt.setText(result.get(0)
					.getContact_custom_phone3());

			phonetype15.setText(result.get(0).getContact_custom_phone3_type());
			phonemobile = result.get(0).getContact_custom_phone3();
			l_contact_phone_no.setVisibility(View.VISIBLE);
			l_contactphoneno15Txt.setVisibility(View.VISIBLE);
		} else {
			contactphoneno15Txt.setText("");
			l_contactphoneno15Txt.setVisibility(View.GONE);
		}

		if (result.get(0).getcontacts_mobile_telephone_number() != null
				&& !result.get(0).getcontacts_mobile_telephone_number()
						.isEmpty()
				|| result.get(0).getcontacts_business_telephone_number() != null
				&& !result.get(0).getcontacts_business_telephone_number()
						.isEmpty()
				|| result.get(0).getcontacts_assistant_telephone_number() != null
				&& !result.get(0).getcontacts_assistant_telephone_number()
						.isEmpty()

				|| result.get(0).getcontacts_business2_telephone_number() != null
				&& !result.get(0).getcontacts_business2_telephone_number()
						.isEmpty()
				|| result.get(0).getcontacts_business_fax_number() != null
				&& !result.get(0).getcontacts_business_fax_number().isEmpty()
				|| result.get(0).getcontacts_car_telephone_number() != null
				&& !result.get(0).getcontacts_car_telephone_number().isEmpty()

				|| result.get(0).getcontacts_home2_telephone_number() != null
				&& !result.get(0).getcontacts_home2_telephone_number()
						.isEmpty()
				|| result.get(0).getcontacts_home_fax_number() != null
				&& !result.get(0).getcontacts_home_fax_number().isEmpty()
				|| result.get(0).getcontacts_home_telephone_number() != null
				&& !result.get(0).getcontacts_home_telephone_number().isEmpty()

				|| result.get(0).getcontacts_pager_number() != null
				&& !result.get(0).getcontacts_pager_number().isEmpty()
				|| result.get(0).getcontacts_radio_telephone_number() != null
				&& !result.get(0).getcontacts_radio_telephone_number()
						.isEmpty()
				|| result.get(0).getContacts2_company_main_phone() != null
				&& !result.get(0).getContacts2_company_main_phone().isEmpty()

				|| result.get(0).getContact_custom_phone1() != null
				&& !result.get(0).getContact_custom_phone1().isEmpty()
				|| result.get(0).getContact_custom_phone2() != null
				&& !result.get(0).getContact_custom_phone2().isEmpty()
				|| result.get(0).getContact_custom_phone3() != null
				&& !result.get(0).getContact_custom_phone3().isEmpty())

		{

			l_contact_phone_no.setVisibility(View.VISIBLE);
			Log.e("Contacts Detail page", "Phone number available check 1 ");

		} else {
			l_contact_phone_no.setVisibility(View.GONE);
			Log.e("Contacts Detail page", "Phone number available check 2");
		}

		if (result.get(0).getcontacts_email1_address() != null
				&& !result.get(0).getcontacts_email1_address().isEmpty()) {
			contactemailTxt.setText(result.get(0).getcontacts_email1_address());
			emailtype.setText(result.get(0).getContact_email1_type());
			primemail = result.get(0).getcontacts_email1_address();
			ShareEmailId = result.get(0).getcontacts_email1_address();
			l_contactemail.setVisibility(View.VISIBLE);
			l_contactemailTxt.setVisibility(View.VISIBLE);
		} else {
			contactemailTxt.setText("");
			emailtype.setText("");
			primemail = "";
			l_contactemailTxt.setVisibility(View.GONE);
		}

		if (result.get(0).getcontacts_email2_address() != null
				&& !result.get(0).getcontacts_email2_address().isEmpty()) {
			contactemail2Txt
					.setText(result.get(0).getcontacts_email2_address());
			emailtype2.setText(result.get(0).getContact_email2_type());
			primemail = result.get(0).getcontacts_email2_address();
			l_contactemail.setVisibility(View.VISIBLE);
			l_contactemail2Txt.setVisibility(View.VISIBLE);
		} else {
			contactemail2Txt.setText("");
			emailtype2.setText("");
			primemail = "";
			l_contactemail2Txt.setVisibility(View.GONE);
		}
		Log.e("Contacts detail actvity", "above email 3");
		if (result.get(0).getcontacts_email3_address() != null
				&& !result.get(0).getcontacts_email3_address().isEmpty()) {
			Log.e("Contacts detail actvity", "inside email 3");
			contactemail3Txt
					.setText(result.get(0).getcontacts_email3_address());
			emailtype3.setText(result.get(0).getContact_email3_type());
			primemail = result.get(0).getcontacts_email3_address();
			l_contactemail.setVisibility(View.VISIBLE);
			l_contactemail3Txt.setVisibility(View.VISIBLE);
		} else {
			Log.e("Contacts detail actvity", "inside email 3 else part");
			contactemail3Txt.setText("");
			emailtype3.setText("");
			primemail = "";
			l_contactemail3Txt.setVisibility(View.GONE);
		}

		Log.e("Contacts detail actvity", "above email 4");
		if (result.get(0).getContacts_custom_email1_address() != null
				&& !result.get(0).getContacts_custom_email1_address().isEmpty()) {

			Log.e("Contacts detail actvity", "inside email 4");
			contactemail4Txt.setText(result.get(0)
					.getContacts_custom_email1_address());
			emailtype4.setText(result.get(0).getContact_custom_email1_type());
			primemail = result.get(0).getContacts_custom_email1_address();
			l_contactemail.setVisibility(View.VISIBLE);
			l_contactemail4Txt.setVisibility(View.VISIBLE);
		} else {
			Log.e("Contacts detail actvity", "inside email 4 else part");
			contactemail4Txt.setText("");
			emailtype4.setText("");
			primemail = "";
			l_contactemail4Txt.setVisibility(View.GONE);
		}
		if (result.get(0).getContacts_custom_email2_address() != null
				&& !result.get(0).getContacts_custom_email2_address().isEmpty()) {
			contactemail5Txt.setText(result.get(0)
					.getContacts_custom_email2_address());
			emailtype5.setText(result.get(0).getContact_custom_email2_type());
			primemail = result.get(0).getContacts_custom_email2_address();
			l_contactemail.setVisibility(View.VISIBLE);
			l_contactemail5Txt.setVisibility(View.VISIBLE);
		} else {
			contactemail5Txt.setText("");
			emailtype5.setText("");
			primemail = "";
			l_contactemail5Txt.setVisibility(View.GONE);
		}

		Log.e("Contacts detail actvity", "above email 6");
		if (result.get(0).getContacts_custom_email3_address() != null
				&& !result.get(0).getContacts_custom_email3_address().isEmpty()) {
			Log.e("Contacts detail actvity", "inside email 6");
			contactemail6Txt.setText(result.get(0)
					.getContacts_custom_email3_address());
			emailtype6.setText(result.get(0).getContact_custom_email3_type());
			primemail = result.get(0).getContacts_custom_email3_address();
			l_contactemail.setVisibility(View.VISIBLE);
			l_contactemail6Txt.setVisibility(View.VISIBLE);
		} else {
			Log.e("Contacts detail actvity", "inside email 6 else part");
			contactemail6Txt.setText("");
			emailtype6.setText("");
			primemail = "";
			l_contactemail6Txt.setVisibility(View.GONE);
		}

		if (result.get(0).getcontacts_email1_address() != null
				&& !result.get(0).getcontacts_email1_address().isEmpty()
				|| result.get(0).getcontacts_email2_address() != null
				&& !result.get(0).getcontacts_email2_address().isEmpty()
				|| result.get(0).getcontacts_email3_address() != null
				&& !result.get(0).getcontacts_email3_address().isEmpty()
				|| result.get(0).getContacts_custom_email1_address() != null
				&& !result.get(0).getContacts_custom_email1_address().isEmpty()
				|| result.get(0).getContacts_custom_email2_address() != null
				&& !result.get(0).getContacts_custom_email2_address().isEmpty()
				|| result.get(0).getContacts_custom_email3_address() != null
				&& !result.get(0).getContacts_custom_email3_address().isEmpty()) {
			l_contactemail.setVisibility(View.VISIBLE);
		} else {
			l_contactemail.setVisibility(View.GONE);
		}

		// Populate Business Location
		if (result.get(0).getContacts_business_location() != null
				&& !result.get(0).getContacts_business_location().isEmpty()) {
			try {
				String val[] = (result.get(0).getContacts_business_location())
						.split("\n");
				ArrayList<HashMap<String, String>> addrVal = new ArrayList<HashMap<String, String>>();
				for (int i = 0; i < val.length; i++) {
					if (val[i].contains(": ")) {
						String[] emailArray = val[i].split(": ");
						HashMap<String, String> row = new HashMap<String, String>();
						row.put("key" + i, emailArray[0]);
						row.put("value" + i, emailArray[1]);
						addrVal.add(row);
					}
				}

				for (int i = 0; i < addrVal.size(); i++) {

					if ((addrVal.size() > 0 && addrVal.get(i) != null && i == 1)) {
						String city = addrVal.get(i).get("value" + i);
						if (city != null && !city.isEmpty()) {

							if (addrsMap != null && !addrsMap.isEmpty()) {
								addrsMap = addrsMap + "," + city;
							} else {
								addrsMap = city;
							}

							// addrsMap = city;
							// break;
						}
					}

					if ((addrVal.size() > 0 && addrVal.get(i) != null && i == 0)) {
						String street = addrVal.get(i).get("value" + i);
						if (street != null && !street.isEmpty()) {
							addrsMap = street;
							// break;
						}
					}

					if ((addrVal.size() > 0 && addrVal.get(i) != null && i == 2)) {
						String state = addrVal.get(i).get("value" + i);
						if (state != null && !state.isEmpty()) {
							// addrsMap = state;
							// break;

							if (addrsMap != null && !addrsMap.isEmpty()) {
								addrsMap = addrsMap + "," + state;
							} else {
								addrsMap = state;
							}

						}
					}

					if ((addrVal.size() > 0 && addrVal.get(i) != null && i == 3)) {
						String pincode = addrVal.get(i).get("value" + i);
						if (pincode != null && !pincode.isEmpty()) {
							// addrsMap = country;
							// break;

							if (addrsMap != null && !addrsMap.isEmpty()) {
								addrsMap = addrsMap + "," + pincode;
							} else {
								addrsMap = pincode;
							}

						}
					}

					if ((addrVal.size() > 0 && addrVal.get(i) != null && i == 4)) {
						String country = addrVal.get(i).get("value" + i);
						if (country != null && !country.isEmpty()) {
							// addrsMap = country;
							// break;

							if (addrsMap != null && !addrsMap.isEmpty()) {
								addrsMap = addrsMap + "," + country;
							} else {
								addrsMap = country;
							}

						}
					}
				}

			} catch (Exception e) {

			}
			contactaddrsTxt1.setText(result.get(0)
					.getContacts_business_location());
			addresstype1.setText(result.get(0)
					.getContact_business_location_type());
			l_contactaddr.setVisibility(View.VISIBLE);
			l_contactaddrTxt.setVisibility(View.VISIBLE);
		}

		else {
			contactaddrsTxt1.setText("");
			addresstype1.setText("");
			l_contactaddrTxt.setVisibility(View.GONE);
		}

		// Populate Home Location
		if (result.get(0).getContacts_home_location() != null
				&& !result.get(0).getContacts_home_location().isEmpty()) {
			try {
				String val[] = (result.get(0).getContacts_home_location())
						.split("\n");
				ArrayList<HashMap<String, String>> addrVal = new ArrayList<HashMap<String, String>>();
				for (int i = 0; i < val.length; i++) {
					if (val[i].contains(": ")) {
						String[] emailArray = val[i].split(": ");
						HashMap<String, String> row = new HashMap<String, String>();
						row.put("key" + i, emailArray[0]);
						row.put("value" + i, emailArray[1]);
						addrVal.add(row);
					}
				}

				for (int i = 0; i < addrVal.size(); i++) {

					if ((addrVal.size() > 0 && addrVal.get(i) != null && i == 1)) {
						String city = addrVal.get(i).get("value" + i);
						if (city != null && !city.isEmpty()) {
							if (addrsMap2 != null && !addrsMap2.isEmpty()) {
								addrsMap2 = addrsMap2 + "," + city;
							} else {
								addrsMap2 = city;
							}
							// addrsMap = city;
							// break;
						}
					}
					if ((addrVal.size() > 0 && addrVal.get(i) != null && i == 0)) {
						String street = addrVal.get(i).get("value" + i);
						if (street != null && !street.isEmpty()) {
							addrsMap2 = street;
							// break;
						}
					}
					if ((addrVal.size() > 0 && addrVal.get(i) != null && i == 2)) {
						String state = addrVal.get(i).get("value" + i);
						if (state != null && !state.isEmpty()) {
							// addrsMap = state;
							// break;
							if (addrsMap2 != null && !addrsMap2.isEmpty()) {
								addrsMap2 = addrsMap2 + "," + state;
							} else {
								addrsMap2 = state;
							}
						}
					}

					if ((addrVal.size() > 0 && addrVal.get(i) != null && i == 3)) {
						String pincode = addrVal.get(i).get("value" + i);
						if (pincode != null && !pincode.isEmpty()) {
							// addrsMap = country;
							// break;
							if (addrsMap2 != null && !addrsMap2.isEmpty()) {
								addrsMap2 = addrsMap2 + "," + pincode;
							} else {
								addrsMap2 = pincode;
							}

						}
					}

					if ((addrVal.size() > 0 && addrVal.get(i) != null && i == 4)) {
						String country = addrVal.get(i).get("value" + i);
						if (country != null && !country.isEmpty()) {
							// addrsMap = country;
							// break;
							if (addrsMap2 != null && !addrsMap2.isEmpty()) {
								addrsMap2 = addrsMap2 + "," + country;
							} else {
								addrsMap2 = country;
							}

						}
					}
				}

			} catch (Exception e) {

			}

			contactaddrsTxt2.setText(result.get(0).getContacts_home_location());
			addresstype2.setText(result.get(0).getContact_home_location_type());
			l_contactaddr.setVisibility(View.VISIBLE);
			l_contactaddr2Txt.setVisibility(View.VISIBLE);
		}

		else {

			contactaddrsTxt2.setText("");
			addresstype2.setText("");
			l_contactaddr2Txt.setVisibility(View.GONE);
		}

		// Populate Home Location
		if (result.get(0).getContacts_other_location() != null
				&& !result.get(0).getContacts_other_location().isEmpty()) {
			try {
				String val[] = (result.get(0).getContacts_other_location())
						.split("\n");
				ArrayList<HashMap<String, String>> addrVal = new ArrayList<HashMap<String, String>>();
				for (int i = 0; i < val.length; i++) {
					if (val[i].contains(": ")) {
						String[] emailArray = val[i].split(": ");
						HashMap<String, String> row = new HashMap<String, String>();
						row.put("key" + i, emailArray[0]);
						row.put("value" + i, emailArray[1]);
						addrVal.add(row);
					}
				}

				for (int i = 0; i < addrVal.size(); i++) {

					if ((addrVal.size() > 0 && addrVal.get(i) != null && i == 1)) {
						String city = addrVal.get(i).get("value" + i);
						if (city != null && !city.isEmpty()) {
							if (addrsMap3 != null && !addrsMap3.isEmpty()) {
								addrsMap3 = addrsMap3 + "," + city;
							} else {
								addrsMap3 = city;
							}
							// addrsMap = city;
							// break;
						}
					}
					if ((addrVal.size() > 0 && addrVal.get(i) != null && i == 0)) {
						String street = addrVal.get(i).get("value" + i);
						if (street != null && !street.isEmpty()) {
							addrsMap3 = street;
							// break;
						}
					}
					if ((addrVal.size() > 0 && addrVal.get(i) != null && i == 2)) {
						String state = addrVal.get(i).get("value" + i);
						if (state != null && !state.isEmpty()) {
							// addrsMap = state;
							// break;
							if (addrsMap3 != null && !addrsMap3.isEmpty()) {
								addrsMap3 = addrsMap3 + "," + state;
							} else {
								addrsMap3 = state;
							}
						}
					}

					if ((addrVal.size() > 0 && addrVal.get(i) != null && i == 3)) {
						String pincode = addrVal.get(i).get("value" + i);
						if (pincode != null && !pincode.isEmpty()) {
							// addrsMap = country;
							// break;
							if (addrsMap3 != null && !addrsMap3.isEmpty()) {
								addrsMap3 = addrsMap3 + "," + pincode;
							} else {
								addrsMap3 = pincode;
							}

						}
					}

					if ((addrVal.size() > 0 && addrVal.get(i) != null && i == 4)) {
						String country = addrVal.get(i).get("value" + i);
						if (country != null && !country.isEmpty()) {
							// addrsMap = country;
							// break;
							if (addrsMap3 != null && !addrsMap3.isEmpty()) {
								addrsMap3 = addrsMap3 + "," + country;
							} else {
								addrsMap3 = country;
							}

						}
					}
				}

			} catch (Exception e) {

			}

			contactaddrsTxt3
					.setText(result.get(0).getContacts_other_location());
			addresstype3
					.setText(result.get(0).getContact_other_location_type());
			l_contactaddr.setVisibility(View.VISIBLE);
			l_contactaddr3Txt.setVisibility(View.VISIBLE);
		}

		else {

			contactaddrsTxt3.setText("");
			addresstype3.setText("");
			l_contactaddr3Txt.setVisibility(View.GONE);
		}

		if (result.get(0).getContacts_business_location() == null
				&& result.get(0).getContacts_home_location() == null
				&& result.get(0).getContacts_other_location() == null) {
			l_contactaddr.setVisibility(View.GONE);
		}

		if (result.get(0).getContact_im_address() != null
				&& !result.get(0).getContact_im_address().isEmpty()) {
			contactimnameTxt.setText(result.get(0).getContact_im_address());
			imtype.setText(result.get(0).getContact_im_address_type());
			l_contactim.setVisibility(View.VISIBLE);
			l_contactimTxt.setVisibility(View.VISIBLE);
		} else {
			contactimnameTxt.setText("");
			imtype.setText("");
			l_contactimTxt.setVisibility(View.GONE);
		}

		if (result.get(0).getContact_im_address1() != null
				&& !result.get(0).getContact_im_address1().isEmpty()) {
			contactimname2Txt.setText(result.get(0).getContact_im_address1());
			imtype2.setText(result.get(0).getContact_im_address1_type());
			l_contactim.setVisibility(View.VISIBLE);
			l_contactim2Txt.setVisibility(View.VISIBLE);
		} else {
			contactimname2Txt.setText("");
			imtype2.setText("");
			l_contactim2Txt.setVisibility(View.GONE);
		}

		if (result.get(0).getContact_im_address2() != null
				&& !result.get(0).getContact_im_address2().isEmpty()) {
			contactimname3Txt.setText(result.get(0).getContact_im_address2());
			imtype3.setText(result.get(0).getContact_im_address2_type());
			l_contactim.setVisibility(View.VISIBLE);
			l_contactim3Txt.setVisibility(View.VISIBLE);
		} else {
			contactimname3Txt.setText("");
			imtype3.setText("");
			l_contactim3Txt.setVisibility(View.GONE);
		}
		if (result.get(0).getContact_custom_im_address() != null
				&& !result.get(0).getContact_custom_im_address().isEmpty()) {
			contactimname4Txt.setText(result.get(0)
					.getContact_custom_im_address());
			imtype4.setText(result.get(0).getContact_custom_im_address_type());
			l_contactim.setVisibility(View.VISIBLE);
			l_contactim4Txt.setVisibility(View.VISIBLE);
		} else {
			contactimname4Txt.setText("");
			imtype4.setText("");
			l_contactim4Txt.setVisibility(View.GONE);
		}

		if (result.get(0).getContact_custom_im1_address() != null
				&& !result.get(0).getContact_custom_im1_address().isEmpty()) {
			contactimname5Txt.setText(result.get(0)
					.getContact_custom_im1_address());
			imtype5.setText(result.get(0).getContact_custom_im1_address_type());
			l_contactim.setVisibility(View.VISIBLE);
			l_contactim5Txt.setVisibility(View.VISIBLE);
		} else {
			contactimname5Txt.setText("");
			imtype5.setText("");
			l_contactim5Txt.setVisibility(View.GONE);
		}

		if (result.get(0).getContact_custom_im2_address() != null
				&& !result.get(0).getContact_custom_im2_address().isEmpty()) {
			contactimname6Txt.setText(result.get(0)
					.getContact_custom_im2_address());
			imtype6.setText(result.get(0).getContact_custom_im2_address_type());
			l_contactim.setVisibility(View.VISIBLE);
			l_contactim6Txt.setVisibility(View.VISIBLE);
		} else {
			contactimname6Txt.setText("");
			imtype6.setText("");
			l_contactim6Txt.setVisibility(View.GONE);
		}

		if (result.get(0).getContact_im_address() == null
				&& result.get(0).getContact_im_address1() == null
				&& result.get(0).getContact_im_address2() == null
				&& result.get(0).getContact_custom_im_address() == null
				&& result.get(0).getContact_custom_im1_address() == null
				&& result.get(0).getContact_custom_im2_address() == null) {
			l_contactim.setVisibility(View.GONE);
		}

		if (result.get(0).getContact_notes() != null
				&& !result.get(0).getContact_notes().isEmpty()) {
			contactnotesTxt.setText(result.get(0).getContact_notes());
			l_contactnotesTxt.setVisibility(View.VISIBLE);
		} else {
			contactnotesTxt.setText("");
			l_contactnotesTxt.setVisibility(View.GONE);
		}

		if (result.get(0).getContact_website() != null
				&& !result.get(0).getContact_website().isEmpty()) {
			contactWebsiteTxt.setText(result.get(0).getContact_website());
			l_contactWebsiteTxt.setVisibility(View.VISIBLE);
		} else {
			contactWebsiteTxt.setText("");
			l_contactWebsiteTxt.setVisibility(View.GONE);
		}

		if (result.get(0).getContact_nick_name() != null
				&& !result.get(0).getContact_nick_name().isEmpty()) {
			contactNicknameTxt.setText(result.get(0).getContact_nick_name());
			l_nickname.setVisibility(View.VISIBLE);
		} else {
			contactNicknameTxt.setText("");
			l_nickname.setVisibility(View.GONE);
		}

		if (result.get(0).getContact_internetcall() != null
				&& !result.get(0).getContact_internetcall().isEmpty()) {
			contactInternetCallTxt.setText(result.get(0)
					.getContact_internetcall());
			l_contactInternetCallTxt.setVisibility(View.VISIBLE);
		} else {
			contactInternetCallTxt.setText("");
			l_contactInternetCallTxt.setVisibility(View.GONE);
		}

		Log.i("cModel.isNativeContact()   ===============",
				"   " + cModel.isNativeContact());
		if (cModel.isNativeContact()) {
			receivedExtra = null;
		}

		ImageView image_view = (ImageView) findViewById(R.id.contact_detail_image);
		// byte[] imageBytes = result.get(0).getContacts_image();
		imageBytes = result.get(0).getContacts_image();
		if (imageBytes != null) {
			image_view.setImageBitmap(ContactsUtilities.getImage(imageBytes));
		}

		if (result.get(0).getContact_isFavorite() == 0) {
			isFavorite = false;
		} else {
			isFavorite = true;
		}

		if (result.get(0).getContacts_ringtone_uri() != null
				&& !result.get(0).getContacts_ringtone_uri().isEmpty()) {
			SelectedRingTone = result.get(0).getContacts_ringtone_uri();
		}
		
		invalidateOptionsMenu();

	}

	private void pouplateContactDetailsByContactModel() {

		String FirstName = cModel.getcontacts_first_name();
		String lastName = cModel.getcontacts_last_name();
		String displayname = null;
		String title = cModel.getcontacts_title();

		if (displayname == null) {
			displayname = FirstName;
		}

		String company = cModel.getcontacts_company_name();
		String workPhone = cModel.getcontacts_business_telephone_number();
		String mobile = cModel.getcontacts_mobile_telephone_number();
		String homePhone = cModel.getContacts_home_telephone_number_type();
		String emailaddress = cModel.getcontacts_email1_address();
		// String office = cGalData.get(GalData.OFFICE);
		if (lastName != null && !lastName.isEmpty()) {

			if (FirstName != null && !FirstName.isEmpty()) {
				getSupportActionBar().setTitle(FirstName);
				getSupportActionBar().setSubtitle(lastName);
				contact_name.setText(FirstName + " " + lastName);
			} else {
				getSupportActionBar().setSubtitle(lastName);
				contact_name.setText(lastName);
			}

		} else {
			if (FirstName != null && !FirstName.isEmpty()) {
				getSupportActionBar().setTitle(FirstName);
				// getSupportActionBar().setSubtitle(FirstName);
				contact_name.setText(FirstName);
			} else {

				if (displayname != null && !displayname.isEmpty()) {
					getSupportActionBar().setTitle(displayname);
					contact_name.setText(displayname);
				} else {

					getSupportActionBar().setTitle("Empty");
					contact_name.setText("Empty");
				}
			}
		}

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		if (title != null && !title.isEmpty()) {
			// contact_designation.setText(title);

			contact_designation.setVisibility(View.VISIBLE);
			contact_comma.setVisibility(View.GONE);
			l_desig_comp.setVisibility(View.VISIBLE);
		}

		if (company != null && !company.isEmpty()) {

			contact_company.setText(company);
			contact_company.setVisibility(View.VISIBLE);
		}

		if (workPhone != null && !workPhone.isEmpty()) {

			contactphonenoTxt.setText(workPhone);
			contactphonenoTxt.setVisibility(View.VISIBLE);
			l_contactphonenoTxt.setVisibility(View.VISIBLE);
		}
		if (mobile != null && !mobile.isEmpty()) {

			contactphoneno2Txt.setText(mobile);
			contactphoneno2Txt.setVisibility(View.VISIBLE);
			l_contactphoneno2Txt.setVisibility(View.VISIBLE);

		}
		if (emailaddress != null && !emailaddress.isEmpty()) {

			contactemailTxt.setText(emailaddress);
			contactemailTxt.setVisibility(View.VISIBLE);
			l_contactemailTxt.setVisibility(View.VISIBLE);

		}

	}

	@SuppressWarnings("static-access")
	private void pouplateContactDetailsByGalModel() {

		String FirstName = cGalData.get(GalData.FIRST_NAME);
		String lastName = cGalData.get(GalData.LAST_NAME);
		String displayname = cGalData.get(GalData.DISPLAY_NAME);
		String title = cGalData.get(GalData.TITLE);

		if (displayname == null) {
			displayname = FirstName;
		}

		if (lastName != null && !lastName.isEmpty()) {

			if (FirstName != null && !FirstName.isEmpty()) {
				getSupportActionBar().setTitle(FirstName);
				getSupportActionBar().setSubtitle(lastName);
				contact_name.setText(FirstName + " " + lastName);
			} else {
				getSupportActionBar().setSubtitle(lastName);
				contact_name.setText(lastName);
			}

		} else {
			if (FirstName != null && !FirstName.isEmpty()) {
				getSupportActionBar().setTitle(FirstName);
				// getSupportActionBar().setSubtitle(FirstName);
				contact_name.setText(FirstName);
			} else {

				if (displayname != null && !displayname.isEmpty()) {
					getSupportActionBar().setTitle(displayname);
					contact_name.setText(displayname);
				} else {

					getSupportActionBar().setTitle("Empty");
					contact_name.setText("Empty");
				}
			}
		}

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		if (title != null && !title.isEmpty()) {
			// contact_designation.setText(title);

			contact_designation.setVisibility(View.VISIBLE);
			contact_comma.setVisibility(View.GONE);
			l_desig_comp.setVisibility(View.VISIBLE);
		}

		String company = cGalData.get(GalData.COMPANY);
		String workPhone = cGalData.get(GalData.WORK_PHONE);
		String mobile = cGalData.get(GalData.MOBILE_PHONE);
		String homePhone = cGalData.get(GalData.HOME_PHONE);
		String emailaddress = cGalData.get(GalData.EMAIL_ADDRESS);
		String office = cGalData.get(GalData.OFFICE);

		if (company != null && !company.isEmpty()) {

			contact_company.setText(company);
			contact_company.setVisibility(View.VISIBLE);
		}

		if (workPhone != null && !workPhone.isEmpty()) {

			contactphonenoTxt.setText(workPhone);
			contactphonenoTxt.setVisibility(View.VISIBLE);
			l_contactphonenoTxt.setVisibility(View.VISIBLE);
		}
		if (mobile != null && !mobile.isEmpty()) {

			contactphoneno2Txt.setText(mobile);
			contactphoneno2Txt.setVisibility(View.VISIBLE);
			l_contactphoneno2Txt.setVisibility(View.VISIBLE);

		}
		if (emailaddress != null && !emailaddress.isEmpty()) {

			contactemailTxt.setText(emailaddress);
			contactemailTxt.setVisibility(View.VISIBLE);
			l_contactemailTxt.setVisibility(View.VISIBLE);

		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_add_contact:

			Intent addContact = new Intent(ContactsDetailActivity.this,
					ContactsAddContact.class);
			Bundle data = new Bundle();
			data.putSerializable("obj", cModel);

			addContact.putExtras(data);
			startActivity(addContact);
			break;
		case R.id.contact_favorites:
			// set boolean value to true
			if (!isFavorite) {
				item.setIcon(R.drawable.contacts_ic_menu_star_holo_light);
				isFavorite = true;

			} else /*
					 * if (cursor.getInt(cursor
					 * .getColumnIndex(ContactsConsts.CONTACT_IS_FAVORITE)) ==
					 * 1)
					 */{
				item.setIcon(R.drawable.contacts_ic_ab_favourites_holo_dark);
				isFavorite = false;
			}

			if (receivedExtra != null) {
				getContentResolver().update(
						ContactsConsts.CONTENT_URI_CONTACTS,
						prepareFavoriteUpdateValues(isFavorite),
						ContactsConsts.CONTACT_ID + "=?",
						new String[] { receivedExtra });
			}

			break;

		case android.R.id.home:

			// if (receivedExtra != null) {
			//
			// getContentResolver().update(
			// ContactsConsts.CONTENT_URI_CONTACTS,
			// prepareFavoriteUpdateValues(isFavorite),
			// "_id = " + "\"" + receivedExtra + "\"", null);
			// if (thumbnail != null) {
			// getContentResolver().update(
			// ContactsConsts.CONTENT_URI_CONTACTS,
			// contactImageUpdateValues(thumbnail),
			// "_id = " + "\"" + receivedExtra + "\"", null);
			//
			// }
			// }

			finish();
			break;

		case R.id.indContEdit:

			if (receivedExtra != null) {
				Intent intent = new Intent(ContactsDetailActivity.this,
						ContactsEditContact.class);
				intent.putExtra("selected_id", receivedExtra);
				startActivity(intent);
			}
			break;
		// case R.id.indContShare:
		// break;

		case R.id.indSetRingtone:

			Mringtone = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
			// Mringtone.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
			// RingtoneManager.TYPE_NOTIFICATION);
			Mringtone.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
					RingtoneManager.TYPE_ALL);
			Mringtone.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE,
					"Select RingTone");
			Mringtone.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
					(Uri) Uri.parse(SelectedRingTone));
			this.startActivityForResult(Mringtone, 0);
			break;

		case R.id.indContShare:

			

			if (cModel != null) 
			{
			
				if (cModel.getContact_id() > 0) {
				new ContactsVcardShare(ContactsDetailActivity.this,
					String.valueOf(cModel.getContact_id()));
				Log.i("Contact detail activity share menu click", ""+cModel.getContact_id());
				}
			}
			
			
			break;
		case R.id.indContDelete:
			if (receivedExtra != null) {

				new MyDialog().show(getSupportFragmentManager(), "dialog");
			}
			break;
		/*
		 * case R.id.indContPlaceonHomescreen: break;
		 */
		default:
			break;
		}

		return true;

	}

	@SuppressLint("ValidFragment")
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
					.setMessage("This contact will be deleted.")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									updateLocalCacheForDelete();
									finish();
								}
							}).setNegativeButton("Cancel", null).create();
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

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		if (receivedExtra != null && !cModel.isContactfromSearch()
				&& !cModel.isNativeContact()) {
			menu.findItem(R.id.contact_favorites).setIcon(
					isFavorite ? R.drawable.contacts_ic_menu_star_holo_light
							: R.drawable.contacts_ic_ab_favourites_holo_dark);
			return super.onPrepareOptionsMenu(menu);
		} else if (cModel.isContactfromSearch()) {
			return super.onPrepareOptionsMenu(menu);
		} else {

			return false;
		}

	}

	private ContentValues prepareFavoriteUpdateValues(boolean value) {
		int favValue = value ? 1 : 0;
		ContentValues updateValues = new ContentValues();
		updateValues.put(ContactsConsts.CONTACT_IS_FAVORITE, favValue);
		return updateValues;
	}

	private ContentValues contactImageUpdateValues(byte[] thumbnail) {
		ContentValues updateValues = new ContentValues();
		updateValues.put(ContactsConsts.CONTACT_PHOTO, thumbnail);
		return updateValues;
	}

	private ContentValues contactImageUpdateValues(Bitmap thumbnail) {
		ContentValues updateValues = new ContentValues();
		updateValues.put(ContactsConsts.CONTACT_PHOTO,
				ContactsUtilities.getBytes(thumbnail));
		return updateValues;
	}

	private ContentValues contactRingTone(String Ringtonepath) {
		ContentValues updateValues = new ContentValues();
		updateValues.put(ContactsConsts.CONTACT_RINGTONE_PATH, Ringtonepath);
		return updateValues;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.

		if (cGalData == null && !cModel.isNativeContact()
				&& receivedExtra != null && !cModel.isContactfromSearch()) {

			MenuInflater inflater = getSupportMenuInflater();
			inflater.inflate(R.menu.contacts_individualcontactsettings, menu);

			return true;
		} else if (cModel.isContactfromSearch()) {
			MenuInflater inflater = getSupportMenuInflater();
			inflater.inflate(R.menu.contact_searchitem_options, menu);
			return true;
		} else {
			return false;
		}
	}

	private void updateLocalCacheForDelete() {
		ContentValues deleteValues = new ContentValues();
		Cursor deletedValueCursor = getContentResolver().query(
				ContactsConsts.CONTENT_URI_CONTACTS,
				new String[] { ContactsConsts.CONTACT_SERVER_ID }, "_id = ?",
				new String[] { receivedExtra }, null);
		deletedValueCursor.moveToFirst();
		String deletedServerID = deletedValueCursor
				.getString(deletedValueCursor
						.getColumnIndex(ContactsConsts.CONTACT_SERVER_ID));

		if (deletedServerID != null && deletedServerID.length() > 0) {
			deleteValues.put(ContactsConsts.CONTACT_SERVER_ID, deletedServerID);
			getContentResolver().insert(
					ContactsConsts.CONTENT_URI_CONTACTS_LOCAL_CACHE_DELETE,
					deleteValues);

		}
		deletedValueCursor.close();
		try {
			int count = getContentResolver().delete(
					ContactsConsts.CONTENT_URI_CONTACTS,
					ContactsConsts.CONTACT_ID + "=?",
					new String[] { receivedExtra });

		} catch (Exception e) {

		}

		try {
			Cursor c = getContentResolver().query(EmEmailContent.Mailbox.CONTENT_URI,
					null, EmEmailContent.Mailbox.DISPLAY_NAME + "=?",
					new String[] { "Contacts" }, null);

			if (c != null && c.getCount() > 0) {
				c.moveToFirst();

				String Accname = c.getString(c
						.getColumnIndex(EmEmailContent.Mailbox.ACCOUNT_KEY));

				String folderId = c.getString(c
						.getColumnIndex(Mailbox.RECORD_ID));

				EmEmController controller = EmEmController
						.getInstance(getApplication());

				controller.updateMailbox(Long.parseLong(Accname),
						Long.parseLong(folderId), null);

				SharedPreferences.Editor prefEditor = new SharedPreferences(
						Email.getAppContext()).edit();
				prefEditor.putBoolean("isPrefUpdated", true);
				prefEditor.commit();

			}
			if (c != null) {
				c.close();
			}
		} catch (Exception e) {
			Log.e("updateLocalCacheForDelete", "menu_manual_sync " + e.toString());

		}

	}



	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// if (keyCode == KeyEvent.KEYCODE_BACK) {
	//
	// // if (receivedExtra != null) {
	// // getContentResolver().update(
	// // ContactsConsts.CONTENT_URI_CONTACTS,
	// // prepareFavoriteUpdateValues(isFavorite),
	// // "_id = " + "\"" + receivedExtra + "\"", null);
	//
	// // if (thumbnail != null) {
	// // getContentResolver().update(
	// // ContactsConsts.CONTENT_URI_CONTACTS,
	// // contactImageUpdateValues(thumbnail),
	// // "_id = " + "\"" + receivedExtra + "\"", null);
	// //
	// // }
	//
	// // }
	// // finish();
	//
	// // your code
	// }
	//
	// return super.onKeyDown(keyCode, event);
	// }

	@Override
	public void onBackPressed() {

		finish();

		// your code.
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		/*SharedPreferences.Editor prefEditor = new SharedPreferences(
				Email.getAppContext()).edit();
		prefEditor.putBoolean("isPrefUpdated", true);
		prefEditor.commit();*/
		super.onPause();
	}

}
