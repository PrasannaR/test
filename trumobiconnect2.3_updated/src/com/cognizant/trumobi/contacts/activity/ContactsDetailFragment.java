package com.cognizant.trumobi.contacts.activity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.output.ByteArrayOutputStream;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.TruBoxSDK.SharedPreferences;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.cognizant.trubox.contacts.db.ContactsConsts;
import com.cognizant.trubox.contacts.db.ContactsModel;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.contacts.utils.ContactsUtilities;
import com.cognizant.trumobi.dialer.DialerParentActivity;
import com.cognizant.trumobi.dialer.utils.DialerUtilities;
import com.cognizant.trumobi.em.EmEmController;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.em.activity.EmMessageCompose;
import com.cognizant.trumobi.em.provider.EmEmailContent;
import com.cognizant.trumobi.em.provider.EmEmailContent.Mailbox;
import com.cognizant.trumobi.exchange.provider.EmGalResult.GalData;
import com.cognizant.trumobi.log.ContactsLog;
import com.cognizant.trumobi.messenger.sms.SmsMain;

public class ContactsDetailFragment extends SherlockFragment implements OnClickListener {

	
	
	

	
	private MenuItem menuEdit;
	private MenuItem menuDelete;
	private MenuItem menuSetRingtone;
	private MenuItem menuShare;
	
	
	
	
	static boolean isAirplaneEnabled;
	public static final String TAG = "ContactDetailFragment";

	private static final int RESULT_OK = 0;
	
	String ContactName = "";

	String phonemobile = "";
	String primemail = "";
	String StrEmailType = "";
	
	
	
	String SecuredBrowserUrl = "";
	byte[] imageBytes = null;

	ImageView AddImage;
	ArrayAdapter<String> arrayAdapter_image;
	PopupWindow pwindow;
	Bitmap thumbnail;
	ContentValues phoneNumbers = new ContentValues();
	ContentValues emailNumbers = new ContentValues();

	boolean isFavorite = false;
	int list_position;
	LinearLayout l_contactprimaryTxt;
	LinearLayout l_contactsecondaryTxt;
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
	TextView contactprimaryTxt;
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

	TextView contactsecondaryTxt;
	TextView contactnickTxt;
	TextView contactphonenoTxt;

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
	
	TextView emailtype;
	TextView emailtype2;
	TextView emailtype3;
	TextView emailtype4;
	TextView emailtype5;
	TextView emailtype6;

	TextView contactaddrsTxt1;
	TextView addresstype1;
	TextView contactaddrsTxt2;
	TextView addresstype2;
	TextView contactaddrsTxt3;
	TextView addresstype3;
	String addrsMap = "";
	String addrsMap2 = "";
	String addrsMap3 = "";

	TextView imtype;
	TextView imtype2;
	TextView imtype3;
	TextView imtype4;
	TextView imtype5;
	TextView imtype6;

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
	TextView contact_name;
	TextView contact_designation;
	TextView contact_company;
	TextView contact_comma;
	TextView contactNicknameTxt;
	TextView contactInternetCallTxt;
	ImageView image_view;
	ImageView fav_icon_view;
	ImageView imgView;
	String selectionArgument = "_id = ?";
	String receivedExtra = null;
	Cursor mCursor;
	ContactsModel cModel = null;
	GalData cGalData = null;
	
	FrameLayout frameLayout;

	@Override
	public SherlockFragmentActivity getSherlockActivity() {
		return super.getSherlockActivity();
	}

	// @Override
	// public void onConfigurationChanged(Configuration newConfig) {
	// // TODO Auto-generated method stub
	// super.onConfigurationChanged(newConfig);
	// frameLayout.removeAllViews();
	//
	// LayoutInflater inflater = (LayoutInflater) getActivity()
	// .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	// View view = inflater.inflate(R.layout.contact_detail_view, null);
	// // populateViewForOrientation(view);
	// frameLayout.addView(view);
	// loadmodel();
	// }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setHasOptionsMenu(true);
		Bundle args = this.getArguments();

		if (args != null) {
			Serializable searchobject = args.getSerializable("searchObj");
			if (searchobject != null) {
				cGalData = (GalData) searchobject;
			}
			Serializable object = args.getSerializable("obj");
			if (object != null) {
				cModel = (ContactsModel) object;
				receivedExtra = String.valueOf(cModel.getContact_id());
			}
		}
		
		
		if (!ContactsUtilities.isTablet(getSherlockActivity())) {
			getSherlockActivity().getSupportActionBar()
					.setDisplayHomeAsUpEnabled(true);
		}
		System.out
				.println("Row id clicked in the framgent::::" + receivedExtra);

	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onPrepareOptionsMenu(menu);
		
		menuEdit = menu.findItem(R.id.menu_edit);
		menuDelete = menu.findItem(R.id.menu_delete);
		menuSetRingtone = menu.findItem(R.id.menu_SetRingtone);
		menuShare = menu.findItem(R.id.menu_share);


		if (cModel == null) {

			menu.findItem(R.id.menu_edit).setVisible(false);
			menu.findItem(R.id.menu_delete).setVisible(false);

			menu.findItem(R.id.menu_SetRingtone).setVisible(false);

			menu.findItem(R.id.menu_share).setVisible(false);

		} else if (cModel != null
				&& (cModel.isContactfromSearch() || cModel.isNativeContact())) {
			
			menu.findItem(R.id.menu_edit).setVisible(false);
			menu.findItem(R.id.menu_delete).setVisible(false);

			menu.findItem(R.id.menu_SetRingtone).setVisible(false);

			menu.findItem(R.id.menu_share).setVisible(false);
		}
		
		
		/*else if(cModel != null && (!cModel.isContactfromSearch() || !cModel.isNativeContact())){
				menu.findItem(R.id.menu_edit).setVisible(true);
		}*/

	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		frameLayout = new FrameLayout(getActivity());

		frameLayout.addView(inflateView());

		return frameLayout;

	}

	View inflateView() {
		// Get the view from fragmenttab2.xml
		LayoutInflater inflater = (LayoutInflater) getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View view = inflater.inflate(R.layout.contact_detail_view, null);

		fav_icon_view = (ImageView) view.findViewById(R.id.set_fav_icon);
		// contact_name = (TextView) view.findViewById(R.id.contact_name);
		contact_name = (TextView) view.findViewById(R.id.contact_name);
		image_view = (ImageView) view.findViewById(R.id.contact_detail_image);
		image_view.setVisibility(View.GONE);
		image_view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
	if (cGalData == null && !cModel.isNativeContact()
						&& ContactsUtilities.SELECTED_ID != null
						&& !cModel.isContactfromSearch()) {
					LayoutInflater inflater = (LayoutInflater) getSherlockActivity()
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View layout = inflater.inflate(R.layout.contacts_screen_popup, (ViewGroup) v.findViewById(R.id.popup_element));

				String[] new_array_image = getResources().getStringArray(R.array.add_contact_image_array);

				arrayAdapter_image = new ArrayAdapter<String>(getSherlockActivity(), android.R.layout.simple_list_item_1, new_array_image);

				ListView lv = (ListView) layout.findViewById(R.id.listview);
				lv.setAdapter(arrayAdapter_image);

			
					
					
					
					
					pwindow = new PopupWindow(layout, image_view.getWidth(),
							5 * contact_name.getHeight(), true);
					
					
				// pwindow = new PopupWindow(layout, 20, 20, true);
				pwindow.setBackgroundDrawable(new BitmapDrawable());
				// pwindow.showAsDropDown(AddImage, AddImage.getBottom(),
				// AddImage.getLeft());

				//pwindow.showAsDropDown(AddImage, 0, 0);
				pwindow.showAsDropDown(image_view,  0, -50);

















				pwindow.setOutsideTouchable(true);
				// WORKING BUT MENU APPEARS AT BOTTOM
				/*
				 * pwindow.showAtLocation(layout, Gravity.NO_GRAVITY,
				 * AddImage.getRight() + AddImage.getWidth(),
				 * AddImage.getBottom() + AddImage.getHeight() / 2 +
				 * pwindow.getHeight() / 2);
				 */
				/*
				 * pwindow.showAtLocation(layout,
				 * Gravity.NO_GRAVITY,contact_image_frame.getRight() ,
				 * AddImage.getBottom() + AddImage.getHeight() / 2 +
				 * pwindow.getHeight() / 2);
				 */

				pwindow.setOutsideTouchable(true);

				lv.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
						// TODO Auto-generated method stub
						// super.onListItemClick(l, v, position, id);
						String name = (String) parent.getItemAtPosition(position);
						//System.out.println(name);
						if (name.equals("Take photo")) {

							Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

							// request code

							startActivityForResult(cameraIntent, 1337);

						} else if (name.equals("Choose photo from Gallery")) {

							Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
							startActivityForResult(i, 1);

						}

						pwindow.dismiss();

					}
				});
				}

			}
		});
















































		l_contactprimaryTxt = (LinearLayout) view.findViewById(R.id.l_prim_name);
		l_contactsecondaryTxt = (LinearLayout) view.findViewById(R.id.l_sec_name);
		l_contactphonenoTxt = (LinearLayout) view.findViewById(R.id.l_contact_phone_no);
		l_contactphoneno2Txt = (LinearLayout) view.findViewById(R.id.layoutTab2);
		l_contactphoneno3Txt = (LinearLayout) view.findViewById(R.id.layoutTab3);
		l_contactphoneno4Txt = (LinearLayout) view.findViewById(R.id.layoutTab4);
		l_contactphoneno5Txt = (LinearLayout) view.findViewById(R.id.layoutTab5);
		l_contactphoneno6Txt = (LinearLayout) view.findViewById(R.id.layoutTab6);
		l_contactphoneno7Txt = (LinearLayout) view.findViewById(R.id.layoutTab7);
		l_contactphoneno8Txt = (LinearLayout) view.findViewById(R.id.layoutTab8);
		l_contactphoneno9Txt = (LinearLayout) view.findViewById(R.id.layoutTab9);
		l_contactphoneno10Txt = (LinearLayout) view.findViewById(R.id.layoutTab10);
		l_contactphoneno11Txt = (LinearLayout) view.findViewById(R.id.layoutTab11);
		l_contactphoneno12Txt = (LinearLayout) view.findViewById(R.id.layoutTab12);
		l_contactphoneno13Txt = (LinearLayout) view.findViewById(R.id.layoutTab13);
		l_contactphoneno14Txt = (LinearLayout) view.findViewById(R.id.layoutTab14);
		l_contactphoneno15Txt = (LinearLayout) view.findViewById(R.id.layoutTab15);
		
		l_contactemail = (LinearLayout) view.findViewById(R.id.l_contact_email);
		l_contactemailTxt = (LinearLayout) view.findViewById(R.id.layoutemailTab1);
		l_contactemail2Txt = (LinearLayout) view.findViewById(R.id.layoutemailTab2);
		l_contactemail3Txt = (LinearLayout) view.findViewById(R.id.layoutemailTab3);
		l_contactemail4Txt = (LinearLayout) view.findViewById(R.id.layoutemailTab4);
		l_contactemail5Txt = (LinearLayout) view.findViewById(R.id.layoutemailTab5);
		l_contactemail6Txt = (LinearLayout) view.findViewById(R.id.layoutemailTab6);
		
		l_contactim = (LinearLayout) view.findViewById(R.id.l_im_name);
		l_contactimTxt = (LinearLayout) view.findViewById(R.id.layoutimTab1);
		l_contactim2Txt = (LinearLayout) view.findViewById(R.id.layoutimTab2);
		l_contactim3Txt = (LinearLayout) view.findViewById(R.id.layoutimTab3);
		l_contactim4Txt = (LinearLayout) view.findViewById(R.id.layoutimTab4);
		l_contactim5Txt = (LinearLayout) view.findViewById(R.id.layoutimTab5);
		l_contactim6Txt = (LinearLayout) view.findViewById(R.id.layoutimTab6);
		
		
l_contactaddr = (LinearLayout) view
				.findViewById(R.id.l_contact_address);
		l_contactaddrTxt = (LinearLayout) view
				.findViewById(R.id.layoutaddrTab1);
		l_contactaddr2Txt = (LinearLayout) view
				.findViewById(R.id.layoutaddrTab2);
		l_contactaddr3Txt = (LinearLayout) view
				.findViewById(R.id.layoutaddrTab3);


		l_contactimnameTxt = (LinearLayout) view.findViewById(R.id.l_im_name);
		//l_contactaddrsTxt = (LinearLayout) view.findViewById(R.id.l_contact_address);
		l_contactnotesTxt = (LinearLayout) view.findViewById(R.id.l_contact_notes);
		l_contactWebsiteTxt = (LinearLayout) view.findViewById(R.id.l_website);
		l_contactInternetCallTxt = (LinearLayout) view.findViewById(R.id.l_internetcall);
		l_nickname = (LinearLayout) view.findViewById(R.id.l_nickname);
		l_desig_comp = (LinearLayout) view.findViewById(R.id.l_desig_comp);

		contactphoneticTxt = (TextView) view.findViewById(R.id.contact_prim_name_value);
		contactnickTxt = (TextView) view.findViewById(R.id.contact_sec_name_value);
		contactprimaryTxt = (TextView) view.findViewById(R.id.contact_prim_name_value);
		contactsecondaryTxt = (TextView) view.findViewById(R.id.contact_sec_name_value);
		contactphonenoTxt = (TextView) view.findViewById(R.id.contact_phone_no_value);
		contactphoneno2Txt = (TextView) view.findViewById(R.id.contact_phone_no_value2);
		contactphoneno3Txt = (TextView) view.findViewById(R.id.contact_phone_no_value3);
		contactphoneno4Txt = (TextView) view.findViewById(R.id.contact_phone_no_value4);
		contactphoneno5Txt = (TextView) view.findViewById(R.id.contact_phone_no_value5);
		contactphoneno6Txt = (TextView) view.findViewById(R.id.contact_phone_no_value6);
		contactphoneno7Txt = (TextView) view.findViewById(R.id.contact_phone_no_value7);
		contactphoneno8Txt = (TextView) view.findViewById(R.id.contact_phone_no_value8);
		contactphoneno9Txt = (TextView) view.findViewById(R.id.contact_phone_no_value9);
		contactphoneno10Txt = (TextView) view.findViewById(R.id.contact_phone_no_value10);
		contactphoneno11Txt = (TextView) view.findViewById(R.id.contact_phone_no_value11);
		contactphoneno12Txt = (TextView) view.findViewById(R.id.contact_phone_no_value12);
		contactphoneno13Txt = (TextView) view.findViewById(R.id.contact_phone_no_value13);
		contactphoneno14Txt = (TextView) view.findViewById(R.id.contact_phone_no_value14);
		contactphoneno15Txt = (TextView) view.findViewById(R.id.contact_phone_no_value15);
		












		contactphonenoTxt.setOnClickListener(this);
		phonetype = (TextView) view.findViewById(R.id.Phonetype);
		phonetype.setOnClickListener(this);

		contactphoneno2Txt.setOnClickListener(this);
		phonetype2 = (TextView) view.findViewById(R.id.Phonetype2);
		phonetype2.setOnClickListener(this);
		contactphoneno3Txt.setOnClickListener(this);
		phonetype3 = (TextView)  view.findViewById(R.id.Phonetype3);
		phonetype3.setOnClickListener(this);

		contactphoneno4Txt.setOnClickListener(this);
		phonetype4 = (TextView)  view.findViewById(R.id.Phonetype4);
		phonetype4.setOnClickListener(this);

		contactphoneno5Txt.setOnClickListener(this);
		phonetype5 = (TextView)  view.findViewById(R.id.Phonetype5);
		phonetype5.setOnClickListener(this);

		contactphoneno6Txt.setOnClickListener(this);
		phonetype6 = (TextView)  view.findViewById(R.id.Phonetype6);
		phonetype6.setOnClickListener(this);

		contactphoneno7Txt.setOnClickListener(this);
		phonetype7 = (TextView)  view.findViewById(R.id.Phonetype7);
		phonetype7.setOnClickListener(this);

		contactphoneno8Txt.setOnClickListener(this);
		phonetype8 = (TextView)  view.findViewById(R.id.Phonetype8);
		phonetype8.setOnClickListener(this);

		contactphoneno9Txt.setOnClickListener(this);
		phonetype9 = (TextView)  view.findViewById(R.id.Phonetype9);
		phonetype9.setOnClickListener(this);

		contactphoneno10Txt.setOnClickListener(this);
		phonetype10 = (TextView)  view.findViewById(R.id.Phonetype10);
		phonetype10.setOnClickListener(this);

		contactphoneno11Txt.setOnClickListener(this);
		phonetype11 = (TextView)  view.findViewById(R.id.Phonetype11);
		phonetype11.setOnClickListener(this);

		contactphoneno12Txt.setOnClickListener(this);
		phonetype12 = (TextView)  view.findViewById(R.id.Phonetype12);
		phonetype12.setOnClickListener(this);

		contactphoneno13Txt.setOnClickListener(this);
		phonetype13 = (TextView)  view.findViewById(R.id.Phonetype13);
		phonetype13.setOnClickListener(this);

		contactphoneno14Txt.setOnClickListener(this);
		phonetype14 = (TextView)  view.findViewById(R.id.Phonetype14);
		phonetype14.setOnClickListener(this);

		contactphoneno15Txt.setOnClickListener(this);
		phonetype15 = (TextView)  view.findViewById(R.id.Phonetype15);
		phonetype15.setOnClickListener(this);
		
		textmessage = (ImageView) view.findViewById(R.id.textmessage);
		textmessage.setOnClickListener(this);

		textmessage2 = (ImageView) view.findViewById(R.id.textmessage2);
		textmessage2.setOnClickListener(this);
		textmessage3 = (ImageView) view.findViewById(R.id.textmessage3);
		textmessage3.setOnClickListener(this);

		textmessage4 = (ImageView) view.findViewById(R.id.textmessage4);
		textmessage4.setOnClickListener(this);

		textmessage5 = (ImageView) view.findViewById(R.id.textmessage5);
		textmessage5.setOnClickListener(this);

		textmessage6 = (ImageView) view.findViewById(R.id.textmessage6);
		textmessage6.setOnClickListener(this);

		textmessage7 = (ImageView) view.findViewById(R.id.textmessage7);
		textmessage7.setOnClickListener(this);

		textmessage8 = (ImageView) view.findViewById(R.id.textmessage8);
		textmessage8.setOnClickListener(this);

		textmessage9 = (ImageView) view.findViewById(R.id.textmessage9);
		textmessage9.setOnClickListener(this);

		textmessage10 = (ImageView) view.findViewById(R.id.textmessage10);
		textmessage10.setOnClickListener(this);

		textmessage11 = (ImageView) view.findViewById(R.id.textmessage11);
		textmessage11.setOnClickListener(this);

		textmessage12 = (ImageView) view.findViewById(R.id.textmessage12);
		textmessage12.setOnClickListener(this);
		textmessage13 = (ImageView) view.findViewById(R.id.textmessage13);
		textmessage13.setOnClickListener(this);

		textmessage14 = (ImageView) view.findViewById(R.id.textmessage14);
		textmessage14.setOnClickListener(this);
		textmessage15 = (ImageView) view.findViewById(R.id.textmessage15);
		textmessage15.setOnClickListener(this);
		
		contactemailTxt = (TextView) view.findViewById(R.id.contact_email_value);
		contactemailTxt.setOnClickListener(this);
		emailtype = (TextView) view.findViewById(R.id.emailtype);
		contactemail2Txt = (TextView) view.findViewById(R.id.contact_email_value2);
		contactemail2Txt.setOnClickListener(this);
		emailtype2 = (TextView) view.findViewById(R.id.emailtype2);
		
		contactemail3Txt = (TextView) view.findViewById(R.id.contact_email_value3);
		contactemail3Txt.setOnClickListener(this);
		emailtype3 = (TextView) view.findViewById(R.id.emailtype3);

		contactemail4Txt = (TextView) view.findViewById(R.id.contact_email_value4);
		contactemail4Txt.setOnClickListener(this);
		emailtype4 = (TextView) view.findViewById(R.id.emailtype4);
		contactemail5Txt = (TextView) view.findViewById(R.id.contact_email_value5);
		contactemail5Txt.setOnClickListener(this);
		emailtype5 = (TextView) view.findViewById(R.id.emailtype5);
		contactemail6Txt = (TextView) view.findViewById(R.id.contact_email_value6);
		contactemail6Txt.setOnClickListener(this);
		emailtype6 = (TextView) view.findViewById(R.id.emailtype6);
		


		contactimnameTxt = (TextView) view.findViewById(R.id.contact_im_name_value1);
		imtype = (TextView) view.findViewById(R.id.imtype1);
		contactimname2Txt = (TextView) view.findViewById(R.id.contact_im_name_value2);
		imtype2 = (TextView) view.findViewById(R.id.imtype2);
		contactimname3Txt = (TextView) view.findViewById(R.id.contact_im_name_value3);
		imtype3 = (TextView) view.findViewById(R.id.imtype3);
		contactimname4Txt = (TextView) view.findViewById(R.id.contact_im_name_value4);
		imtype4 = (TextView) view.findViewById(R.id.imtype4);
		contactimname5Txt = (TextView) view.findViewById(R.id.contact_im_name_value5);
		imtype5 = (TextView) view.findViewById(R.id.imtype5);
		contactimname6Txt = (TextView) view.findViewById(R.id.contact_im_name_value6);
		imtype6 = (TextView) view.findViewById(R.id.imtype6);
		
			contactaddrsTxt1 = (TextView) view
				.findViewById(R.id.contact_address_value1);
		contactaddrsTxt1.setOnClickListener(this);
		addresstype1 = (TextView) view.findViewById(R.id.addresstype1);
		addresstype1.setOnClickListener(this);

		contactaddrsTxt2 = (TextView) view
				.findViewById(R.id.contact_address_value2);
		contactaddrsTxt2.setOnClickListener(this);
		addresstype2 = (TextView) view.findViewById(R.id.addresstype2);
		addresstype2.setOnClickListener(this);

		contactaddrsTxt3 = (TextView) view
				.findViewById(R.id.contact_address_value3);
		contactaddrsTxt3.setOnClickListener(this);
		addresstype3 = (TextView) view.findViewById(R.id.addresstype3);
		addresstype3.setOnClickListener(this);








		contactnotesTxt = (TextView) view.findViewById(R.id.contact_notes_value);
		contactWebsiteTxt = (TextView) view.findViewById(R.id.website_value);
		contactWebsiteTxt.setOnClickListener(this);
		
		contactNicknameTxt = (TextView) view.findViewById(R.id.nickname_value);
		contactInternetCallTxt = (TextView) view.findViewById(R.id.internetcall_value);

		//contact_name = (TextView) view.findViewById(R.id.contact_name);
		contact_designation = (TextView) view.findViewById(R.id.contact_designation);
		contact_company = (TextView) view.findViewById(R.id.contact_company);
		contact_comma = (TextView) view.findViewById(R.id.contact_comma);
		/* fav_icon_view = (ImageView)view.findViewById(R.id.set_fav_icon); */
		// fav_icon_view.setImageResource(R.drawable.ic_menu_star_holo_dark);
		fav_icon_view.setVisibility(View.GONE);
		fav_icon_view.setImageResource(isFavorite ? R.drawable.contacts_ic_menu_star_holo_light : R.drawable.contacts_ic_menu_star_holo_dark);
		fav_icon_view.setOnClickListener(favClickListener);

		return view;

	}


	public void updateModel(ContactsModel model) {
		this.cModel = model;
		ContactsLog.i(TAG, "updateModel **" + model);

		receivedExtra = null;

		View view = inflateView();
		// populateViewForOrientation(view);
		frameLayout.removeAllViews();
		// image_view = (ImageView)
		// view.findViewById(R.id.contact_detail_image);
		//
		// image_view.setVisibility(View.GONE);
		//
		// fav_icon_view = (ImageView) view.findViewById(R.id.set_fav_icon);
		// fav_icon_view.setVisibility(View.GONE);
		//
		frameLayout.addView(view);

		if (model != null) {

			ContactsLog.i(TAG,
					"updateModel name**" + model.getcontacts_first_name());

			receivedExtra = String.valueOf(cModel.getContact_id());
			 loadmodel();

			///pouplateContactDetailsByModel(model);
		}


	}
	OnClickListener favClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (cModel != null && cModel.isContactfromSearch()) {

				Intent addContact = new Intent(getActivity(),
						ContactsAddContact.class);
				Bundle data = new Bundle();
				data.putSerializable("obj", cModel);

				addContact.putExtras(data);
				startActivity(addContact);

			} else {
				if (!isFavorite) {
					fav_icon_view
							.setImageResource(R.drawable.contacts_ic_menu_star_holo_light);
					isFavorite = true;
				} else {
					fav_icon_view
							.setImageResource(R.drawable.contacts_ic_menu_star_holo_dark);
					isFavorite = false;
				}
				System.out.println("isFAv**** " + isFavorite);
				if (receivedExtra != null) {
					getSherlockActivity().getContentResolver().update(
							ContactsConsts.CONTENT_URI_CONTACTS,
							prepareFavoriteUpdateValues(isFavorite),
							ContactsConsts.CONTACT_ID + "=?",
							new String[] { receivedExtra });
				}
			}
		}
	};


	@Override
	public void onResume() {

		super.onResume();

		loadmodel();

	}

	void loadmodel() {
		if (cModel != null) {
			ArrayList<ContactsModel> result = null;
			if (cModel.isNativeContact()) {
				result = ContactsUtilities.getNativeContactsDetailsFromCursor(
						Email.getAppContext().getContentResolver(),
						String.valueOf(cModel.getContact_id()));

				SharedPreferences.Editor prefEditor = new SharedPreferences(
						Email.getAppContext()).edit();
				prefEditor.putString("receivedExtra",
						"" + String.valueOf(cModel.getContact_id()));
				prefEditor.commit();
				Log.e("Contactdetailfragment receivedExtra Shared pref",
						""
								+ new SharedPreferences(Email
										.getAppContext()).getString(
										"receivedExtra", "" + receivedExtra));
				if (result != null && result.size() > 0) {
					pouplateContactDetailsByModel(result.get(0));
				}
			} else if (cModel.isContactfromSearch()) {

				pouplateContactDetailsByModel(cModel);

			} else {
				
				menuEdit.setVisible(true);
				menuDelete.setVisible(true);
				menuSetRingtone.setVisible(true);
				menuShare.setVisible(true);

				result = ContactsUtilities.getContactsDetailsFromCursor(
						Email.getAppContext().getContentResolver(),
						receivedExtra);

				SharedPreferences.Editor prefEditor = new SharedPreferences(
						Email.getAppContext()).edit();
				prefEditor.putString("receivedExtra", "" + receivedExtra);
				prefEditor.commit();
				Log.e("Contactdetailfragment receivedExtra Shared pref",
						""
								+ new SharedPreferences(Email
										.getAppContext()).getString(
										"receivedExtra", "" + receivedExtra));

				if (result != null && result.size() > 0) {
					pouplateContactDetailsByModel(result.get(0));
				}

			}

		} else if (cGalData != null) {
			
			menuEdit.setVisible(false);
			menuDelete.setVisible(false);
			menuSetRingtone.setVisible(false);
			menuShare.setVisible(false);
			
			pouplateContactDetailsByGalModel();
		}}

	private ContentValues prepareFavoriteUpdateValues(boolean value) {
		int favValue = value ? 1 : 0;
		ContentValues updateValues = new ContentValues();
		updateValues.put(ContactsConsts.CONTACT_IS_FAVORITE, favValue);
		return updateValues;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		//setRetainInstance(true);

	}

	private ContentValues contactImageUpdateValues(byte[] thumbnail) {
		ContentValues updateValues = new ContentValues();
		updateValues.put(ContactsConsts.CONTACT_PHOTO, thumbnail);
		return updateValues;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		try {
			ImageView AddImages = (ImageView) getSherlockActivity()
					.findViewById(R.id.contact_detail_image);

			if (requestCode == 1337) {
				thumbnail = (Bitmap) data.getExtras().get("data");

				if (thumbnail != null) {
					byte[] imageData = ContactsUtilities.getBytes(thumbnail);
					// && imageData.length <= 25600
					if (imageData != null) {
						getSherlockActivity().getContentResolver().update(
								ContactsConsts.CONTENT_URI_CONTACTS,
								contactImageUpdateValues(imageData),
								ContactsConsts.CONTACT_ID + "=?",
								new String[] { receivedExtra });
						if (imageData.length <= 48 * 1024) {
							updateCacheTable();
						}

						AddImages.setImageBitmap(thumbnail);
					} else {
						thumbnail = null;
						Toast.makeText(
								getActivity(),
								"image is too large please choose a smaller one",
								Toast.LENGTH_SHORT).show();
					}
				}

				/*
				 * if (thumbnail != null) {
				 * 
				 * 
				 * 
				 * byte[] imageData = ContactsUtilities.getBytes(thumbnail); //
				 * 1048576 if (imageData != null && imageData.length <= 25600) {
				 * getSherlockActivity().getContentResolver().update(
				 * ContactsConsts.CONTENT_URI_CONTACTS,
				 * contactImageUpdateValues(imageData), "_id = " + "\"" +
				 * receivedExtra + "\"", null); updateCacheTable();
				 * 
				 * AddImages.setImageBitmap(thumbnail); } else { thumbnail =
				 * null; Toast.makeText( getActivity(),
				 * "image is too large please choose a smaller one",
				 * Toast.LENGTH_SHORT).show(); } }
				 */
				// if (thumbnail != null) {
				//
				// getSherlockActivity().getContentResolver().update(
				// ContactsConsts.CONTENT_URI_CONTACTS,
				// contactImageUpdateValues(thumbnail),
				// "_id = "
				// + "\""
				// + new SharedPreferences(CognizantEmail
				// .getAppContext()).getString(
				// "receivedExtra", "" + receivedExtra)
				// + "\"", null);
				// updateCacheTable();
				// AddImages.setImageBitmap(thumbnail);
				//
				// }

			}

			else if (requestCode == 1 && data != null) {

				Uri selectedImage = data.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };
				Cursor cursor = getSherlockActivity().getContentResolver()
						.query(selectedImage, filePathColumn, null, null, null);
				cursor.moveToFirst();
				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String picturePath = cursor.getString(columnIndex);
				cursor.close();

				thumbnail = decodeFile(picturePath);
				// if (thumbnail != null) {
				//
				// getSherlockActivity().getContentResolver().update(
				// ContactsConsts.CONTENT_URI_CONTACTS,
				// contactImageUpdateValues(thumbnail),
				// "_id = "
				// + "\""
				// + new SharedPreferences(CognizantEmail
				// .getAppContext()).getString(
				// "receivedExtra", "" + receivedExtra)
				// + "\"", null);
				// updateCacheTable();
				//
				// AddImages.setImageBitmap(thumbnail);
				//
				// }

				/*
				 * if (thumbnail != null) { byte[] imageData =
				 * ContactsUtilities.getBytes(thumbnail); // 1048576 if
				 * (imageData != null && imageData.length <= 25600) {
				 * 
				 * getSherlockActivity().getContentResolver().update(
				 * ContactsConsts.CONTENT_URI_CONTACTS,
				 * contactImageUpdateValues(imageData), "_id = " + "\"" +
				 * receivedExtra + "\"", null); updateCacheTable();
				 * 
				 * AddImages.setImageBitmap(thumbnail); } else { thumbnail =
				 * null; Toast.makeText( getActivity(),
				 * "image is too large please choose a smaller one",
				 * Toast.LENGTH_SHORT).show(); } }
				 */

				if (thumbnail != null) {
					byte[] imageData = ContactsUtilities.getBytes(thumbnail);
					// 1048576
					if (imageData != null) {
						getSherlockActivity().getContentResolver().update(
								ContactsConsts.CONTENT_URI_CONTACTS,
								contactImageUpdateValues(imageData),
								ContactsConsts.CONTACT_ID + "=?",
								new String[] { receivedExtra });
						if (imageData.length <= 48 * 1024) {
							updateCacheTable();
						}

						AddImages.setImageBitmap(thumbnail);
					} else {
						thumbnail = null;
						Toast.makeText(
								getActivity(),
								"image is too large please choose a smaller one",
								Toast.LENGTH_SHORT).show();
					}
				}

			}
		} catch (Exception e) {
			ContactsLog.e("Exception", "Exception **" + e.toString());
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void updateCacheTable() {
		ContentValues updateIDValue = new ContentValues();
		if (receivedExtra != null) {
			Cursor c = Email
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
						getSherlockActivity()
								.getContentResolver()
								.insert(ContactsConsts.CONTENT_URI_CONTACTS_LOCAL_CACHE_UPDATE,
										updateIDValue);
					}
				}
			}

		}

		try {
			Cursor c = getSherlockActivity().getContentResolver().query(EmEmailContent.Mailbox.CONTENT_URI,
					null, EmEmailContent.Account.DISPLAY_NAME + "=?",
					new String[] { "Contacts" }, null);

			if (c != null && c.getCount() > 0) {
				c.moveToFirst();

				String Accname = c.getString(c
						.getColumnIndex(EmEmailContent.Mailbox.ACCOUNT_KEY));

				String folderId = c.getString(c
						.getColumnIndex(Mailbox.RECORD_ID));

				EmEmController controller = EmEmController.getInstance(getSherlockActivity().getApplication());
				controller.updateMailbox(Long.parseLong(Accname), Long.parseLong(folderId), null);

			}
			if (c != null) {
				c.close();
			}
		} catch (Exception e) {
			Log.e("Contacts Detail activity update cache table", "menu_manual_sync " + e.toString());

		}
	
	}
	
	private ContentValues contactImageUpdateValues(Bitmap thumbnail) {
		ContentValues updateValues = new ContentValues();
		updateValues.put(ContactsConsts.CONTACT_PHOTO,
				ContactsUtilities.getBytes(thumbnail));
		return updateValues;
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

	Cursor getCursor() {
		return getSherlockActivity().getContentResolver().query(
				ContactsConsts.CONTENT_URI_CONTACTS, null, selectionArgument,
				new String[] { receivedExtra }, null);
	}

	public void pouplateContactDetailsByCursor(Cursor cursor) {

		String viewOrder = new SharedPreferences(getSherlockActivity()
				.getApplicationContext()).getString("prefViewType",
				"First_name_first");
		String sortOrder = new SharedPreferences(getSherlockActivity()
				.getApplicationContext()).getString("prefSortOrder",
				ContactsConsts.CONTACT_FIRST_NAME);
		imgView.setVisibility(View.VISIBLE);
		while (cursor != null && cursor.moveToNext()) {

			if (viewOrder.equals("First_name_first")) {

				if (cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_FIRST_NAME)) != null
						&& !cursor
								.getString(
										cursor.getColumnIndex(ContactsConsts.CONTACT_FIRST_NAME))
								.isEmpty()) {

					contact_name
							.setText(cursor.getString(cursor
									.getColumnIndex(ContactsConsts.CONTACT_FIRST_NAME))
									+ " "
									+ cursor.getString(cursor
											.getColumnIndex(ContactsConsts.CONTACT_LAST_NAME)));

				} else {
					contact_name.setText(cursor.getString(cursor
							.getColumnIndex(ContactsConsts.CONTACT_LAST_NAME)));
				}

			} else if (viewOrder.equals("Last_name_first")) {

				if (cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_LAST_NAME)) != null
						&& !cursor
								.getString(
										cursor.getColumnIndex(ContactsConsts.CONTACT_LAST_NAME))
								.isEmpty()) {

					if (cursor.getString(cursor
							.getColumnIndex(ContactsConsts.CONTACT_FIRST_NAME)) != null
							&& !cursor
									.getString(
											cursor.getColumnIndex(ContactsConsts.CONTACT_FIRST_NAME))
									.isEmpty()) {

						contact_name
								.setText(cursor.getString(cursor
										.getColumnIndex(ContactsConsts.CONTACT_LAST_NAME))
										+ ", "
										+ cursor.getString(cursor
												.getColumnIndex(ContactsConsts.CONTACT_FIRST_NAME)));
					} else {
						contact_name
								.setText(cursor.getString(cursor
										.getColumnIndex(ContactsConsts.CONTACT_LAST_NAME)));
					}

				} else {

					contact_name
							.setText(cursor.getString(cursor
									.getColumnIndex(ContactsConsts.CONTACT_FIRST_NAME)));

				}

			}

			// Populating the UI...

			if (cursor.getString(cursor
					.getColumnIndex(ContactsConsts.CONTACT_FIRST_NAME)) != null
					&& !cursor
							.getString(
									cursor.getColumnIndex(ContactsConsts.CONTACT_FIRST_NAME))
							.isEmpty()) {
				contactphoneticTxt.setText(cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_FIRST_NAME)));
				// l_contactprimaryTxt.setVisibility(View.VISIBLE);
			}

			if (cursor.getString(cursor
					.getColumnIndex(ContactsConsts.CONTACT_LAST_NAME)) != null
					&& !cursor
							.getString(
									cursor.getColumnIndex(ContactsConsts.CONTACT_LAST_NAME))
							.isEmpty()) {
				contactnickTxt.setText(cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_LAST_NAME)));
				// l_contactsecondaryTxt.setVisibility(View.VISIBLE);
			}

			if (cursor.getString(cursor
					.getColumnIndex(ContactsConsts.CONTACT_JOB_TITLE)) != null
					&& !cursor
							.getString(
									cursor.getColumnIndex(ContactsConsts.CONTACT_JOB_TITLE))
							.isEmpty()) {
				contact_designation.setText(cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_JOB_TITLE)));
				// contact_designation.setVisibility(View.VISIBLE);
				// contact_comma.setVisibility(View.VISIBLE);
				// l_desig_comp.setVisibility(View.VISIBLE);
			}

			if (cursor.getString(cursor
					.getColumnIndex(ContactsConsts.CONTACT_COMPANY)) != null
					&& !cursor
							.getString(
									cursor.getColumnIndex(ContactsConsts.CONTACT_COMPANY))
							.isEmpty()) {
				System.out
						.println(""
								+ cursor.getString(cursor
										.getColumnIndex(ContactsConsts.CONTACT_COMPANY)));
				contact_company.setText(cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_COMPANY)));
				contact_company.setVisibility(View.VISIBLE);
				l_desig_comp.setVisibility(View.VISIBLE);

			}

			try {
				String phone_details = cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_PHONE));
				ArrayList<HashMap<String, String>> phoneVal = new ArrayList<HashMap<String, String>>();
				String[] splitParentStr = phone_details.split(":");

				for (int i = 0; i < splitParentStr.length; i++) {
					if (splitParentStr[i].contains("=")) {

						String[] phone_array = splitParentStr[i].split("=");
						HashMap<String, String> row = new HashMap<String, String>();
						row.put("phonetype" + i, phone_array[0]);
						row.put("phoneNumber" + i, phone_array[1]);
						phoneVal.add(row);
					}
				}

				for (int i = 0; i < phoneVal.size(); i++) {
					if (i == 0) {
						contactphonenoTxt.setText(phoneVal.get(i).get(
								"phoneNumber" + i));
						phonetype.setText(phoneVal.get(i).get("phonetype" + i));
						phonemobile = phoneVal.get(i).get("phoneNumber" + i);
						// l_contact_phone_no.setVisibility(View.VISIBLE);
						l_contactphonenoTxt.setVisibility(View.VISIBLE);
					}

					if (i == 1) {
						contactphoneno2Txt.setText(phoneVal.get(i).get(
								"phoneNumber" + i));
						phonetype2
								.setText(phoneVal.get(i).get("phonetype" + i));
						phonemobile = phoneVal.get(i).get("phoneNumber" + i);
						// l_contact_phone_no.setVisibility(View.VISIBLE);
						l_contactphoneno2Txt.setVisibility(View.VISIBLE);

					}
				}

			} catch (Exception e) {
				Log.e("ContactsDetailActivity", "Phone: " + e.toString());
			}

			try {
				String email_details = cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_EMAIL));
				System.out.println("Phone details: " + email_details);
				if (email_details != null && !email_details.isEmpty()) {
					String[] email_array = null;
					email_array = email_details.split(":");
					String[] email_type_number = null;
					System.out.println("phone_array.length: "
							+ email_array.length);
					// if (phone_array.length > 0) {

					for (int i = 0; i < email_array.length; i++) {
						System.out.println("email_array.length: "
								+ email_array[i]);
						if (email_array[i] != null && !email_array[i].isEmpty()) {
							email_type_number = email_array[i].split("=");
							// System.out.println("email_type_number.length: "
							// + email_type_number.length + " "
							// + email_type_number[0] + email_type_number[1]);
							emailNumbers.put(email_type_number[0],
									email_type_number[1]);

						}
					}

				}

				if (emailNumbers.size() > 0) {

					contactemailTxt.setText(emailNumbers.get("Email_Adress")
							.toString());

					emailtype.setText("Custom");
					primemail = emailNumbers.get("Email_Adress").toString();

					l_contactemailTxt.setVisibility(View.VISIBLE);
				}
			} catch (Exception e) {
				Log.e("ContactsDetailActivity", " email: " + e.toString());
			}

			try {
				String im_details = cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_IM_NAME));

				ContactsLog.d("ConatacsDetailfragment", "im_details"
						+ im_details);
				// ArrayList<HashMap<String, String>> imVal = new
				// ArrayList<HashMap<String, String>>();
				// String[] splitParentStr = im_details.split(":");
				//
				// for (int i = 0; i < splitParentStr.length; i++) {
				// if (splitParentStr[i].contains("=")) {
				//
				// String[] im_array = splitParentStr[i].split("=");
				// if (im_array.length >= 2) {
				// HashMap<String, String> row = new HashMap<String, String>();
				// row.put("imtype" + i, im_array[0]);
				// row.put("imNumber" + i, im_array[1]);
				// imVal.add(row);
				// }
				// }
				// }
				//
				// for (int i = 0; i < imVal.size(); i++) {
				// if (i == 0) {
				if (im_details != null && !im_details.isEmpty()) {
					contactimnameTxt.setText(im_details);
					imtype.setText("Custom");
					// primemail = imVal.get(i).get("imNumber"+i);
					l_contactimnameTxt.setVisibility(View.VISIBLE);
				}

			}

			catch (Exception e) {

				Log.e("ContactsDetailActivity", "IM: " + e.toString());
			}

			try {
				if (cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_NOTES)) != null
						&& !cursor
								.getString(
										cursor.getColumnIndex(ContactsConsts.CONTACT_NOTES))
								.isEmpty()) {
					contactnotesTxt.setText(cursor.getString(cursor
							.getColumnIndex(ContactsConsts.CONTACT_NOTES)));
					l_contactnotesTxt.setVisibility(View.VISIBLE);
				}
			} catch (Exception e) {
				Log.e("ContactsDetailActivity", "NOTES:  " + e.toString());
			}

			if (cursor.getString(cursor
					.getColumnIndex(ContactsConsts.CONTACT_WEB_ADRESS)) != null
					&& !cursor
							.getString(
									cursor.getColumnIndex(ContactsConsts.CONTACT_WEB_ADRESS))
							.isEmpty()) {
				contactWebsiteTxt.setText(cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_WEB_ADRESS)));
				l_contactWebsiteTxt.setVisibility(View.VISIBLE);
			}

			if (cursor.getString(cursor
					.getColumnIndex(ContactsConsts.CONTACT_NICK_NAME)) != null
					&& !cursor
							.getString(
									cursor.getColumnIndex(ContactsConsts.CONTACT_NICK_NAME))
							.isEmpty()) {
				contactNicknameTxt.setText(cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_NICK_NAME)));
				l_nickname.setVisibility(View.VISIBLE);
			}

			if (cursor.getString(cursor
					.getColumnIndex(ContactsConsts.CONTACT_INTERNETCALL)) != null
					&& !cursor
							.getString(
									cursor.getColumnIndex(ContactsConsts.CONTACT_INTERNETCALL))
							.isEmpty()) {
				contactInternetCallTxt.setText(cursor.getString(cursor
						.getColumnIndex(ContactsConsts.CONTACT_INTERNETCALL)));
				l_contactInternetCallTxt.setVisibility(View.VISIBLE);
			}

			byte[] imageBytes = cursor.getBlob(cursor
					.getColumnIndex(ContactsConsts.CONTACT_PHOTO));
			if (imageBytes != null)
				image_view.setImageBitmap(ContactsUtilities
						.getImage(imageBytes));
			if (cursor.getInt(cursor
					.getColumnIndex(ContactsConsts.CONTACT_IS_FAVORITE)) == 0) {
				fav_icon_view
						.setImageResource(R.drawable.contacts_ic_menu_star_holo_dark);
				isFavorite = false;
			} else
				fav_icon_view
						.setImageResource(R.drawable.contacts_ic_menu_star_holo_light);
			isFavorite = true;
		}

	}

	private void pouplateContactDetailsByModel(ContactsModel contact) {
/*
		String viewOrder = new SharedPreferences(getActivity()).getString(
				"prefViewType", "First_name_first");

		String firstName = null;
		String lastName = null;
		image_view.setVisibility(View.VISIBLE);
		if (contact.isContactfromSearch()) {
			contactphoneticTxt.setText("test");

		}
		if (viewOrder.equals("First_name_first")) {
			firstName = contact.getcontacts_first_name();
			lastName = contact.getcontacts_last_name();
		} else {
			lastName = contact.getcontacts_first_name();
			firstName = contact.getcontacts_last_name();
		}
		String middleName = contact.getcontacts_middle_name();
		String displayName = "";

		ContactsLog.d(TAG, "firstname** " + firstName);
		ContactsLog.d(TAG, "lastName** " + lastName);
		ContactsLog.d(TAG,
				"middleName** " + middleName + contactphoneticTxt.isShown());

		if (firstName != null && !firstName.isEmpty()) {

			displayName += firstName + " ";
			//contactphoneticTxt.setText(firstName);
			contact_name.setText(firstName);
		} else {
			firstName = "";
		}

		if (middleName != null && !middleName.isEmpty()) {

			displayName += middleName + " ";
		} else {
			middleName = "";
		}

		if (lastName != null && !lastName.isEmpty()) {

			displayName += lastName + " ";
		} else {
			lastName = "";
		}

		if (displayName != null && !displayName.isEmpty()) {
			contact_name.setText(displayName);

		} else {

			contact_name.setText("Empty");
		}

		if (firstName != null && !firstName.isEmpty()) {
			contactphoneticTxt.setText(contact.getcontacts_first_name());
			l_contactprimaryTxt.setVisibility(View.VISIBLE);
			contact_name.setText(contact.getcontacts_first_name());
		}

		if (lastName != null && !lastName.isEmpty()) {
			contactnickTxt.setText(contact.getcontacts_last_name());

		}*/
		
		
		

        String firstName = "";
        String lastName = "";
        String middleName ="";
        firstName = contact.getcontacts_first_name();
        lastName = contact.getcontacts_last_name();
        middleName = contact.getcontacts_middle_name();
        image_view.setVisibility(View.VISIBLE);

        if (contact.isContactfromSearch()) 
               contact_name.setText("test");

        String viewOrder = new SharedPreferences(getActivity()).getString("prefViewType", "First_name_first");
        if (viewOrder.equals("First_name_first")) 
        {
               if (firstName != null&& !firstName.isEmpty()) 
               {
                     if (lastName != null && !lastName.isEmpty()) 
                     {
                            if (middleName != null && !middleName.isEmpty())
                                   contact_name.setText(firstName+ " "+ middleName+ " "+ lastName);
                            else 
                                   contact_name.setText(firstName+ " "+ lastName);
                     } 
                     else 
                     {
                            if (middleName != null     && !middleName.isEmpty())
                                   contact_name.setText(firstName+ " "+ middleName);                           
                            else 
                                   contact_name.setText(firstName);
                     }
 
               } 
               else {
                     if (lastName != null && !lastName.isEmpty()) 
                     {
                            if (middleName != null     && !middleName.isEmpty())
                                   contact_name.setText(middleName+ " "+ lastName);                     
                            else 
                                   contact_name.setText(lastName);                 
                     } 
                     else 
                     {
                            if (middleName != null && !middleName.isEmpty())
                                   contact_name.setText(middleName);
                            else 
                                   contact_name.setText("Empty");
                     }
 
               }
        } 
        else if (viewOrder.equals("Last_name_first")) 
        {
               if (lastName != null && !lastName.isEmpty()) 
               {
                     if (firstName != null && !firstName.isEmpty()) 
                     {
                            if (middleName != null && !middleName.isEmpty()) 
                                   contact_name.setText(lastName+ ", "+ middleName+ " "+ firstName);
                            else 
                                   contact_name.setText(lastName+ ", "+ firstName);
                     }                          
                     else 
                     {
                            if (middleName != null     && !middleName.isEmpty()) 
                                   contact_name.setText(lastName+ ", "+ middleName);
                            else 
                                   contact_name.setText(lastName);
                     }
               } 
               else 
               {
                     if (firstName != null && !firstName.isEmpty()) 
                     {
                            if (middleName != null && !middleName.isEmpty())
                                   contact_name.setText(firstName + " "+ middleName);
                            else 
                                   contact_name.setText(firstName);
                     } 
                     else 
                     {
                            if (middleName != null && !middleName.isEmpty())
                                   contact_name.setText(middleName);
                            else 
                                   contact_name.setText("Empty");
                     }
               }
        }

		
		

		if (contact.getcontacts_title() != null
				&& !contact.getcontacts_title().isEmpty()) {
			contact_designation.setText(contact.getcontacts_title());
			contact_designation.setVisibility(View.VISIBLE);
			contact_comma.setVisibility(View.GONE);
			l_desig_comp.setVisibility(View.VISIBLE);
		}

		String company_details = "";
		if (contact.getcontacts_department() != null
				&& !contact.getcontacts_department().isEmpty()) {
			company_details = company_details
					+ contact.getcontacts_department();
		}
		if (contact.getcontacts_company_name() != null
				&& !contact.getcontacts_company_name().isEmpty()) {
			company_details = company_details + " "
					+ contact.getcontacts_company_name();
			/*
			 * System.out.println(""+ result.get(0).getcontacts_company_name());
			 * contact_company
			 * .setText(result.get(0).getcontacts_department()+" "+
			 * result.get(0).getcontacts_company_name());
			 * contact_company.setVisibility(View.VISIBLE);
			 * l_desig_comp.setVisibility(View.VISIBLE);
			 */
		}

		if (company_details != null && !company_details.isEmpty()) {
			contact_company.setText(company_details);
			contact_company.setVisibility(View.VISIBLE);
			l_desig_comp.setVisibility(View.VISIBLE);
		}

		// Phone type 1 data population
		if (contact.getcontacts_mobile_telephone_number() != null
				&& !contact.getcontacts_mobile_telephone_number().isEmpty()) {

			contactphonenoTxt.setText(contact
					.getcontacts_mobile_telephone_number());
			phonetype.setText(contact.getContact_phone_number_mobile_type1());
			phonemobile = contact.getcontacts_mobile_telephone_number();
			l_contactphonenoTxt.setVisibility(View.VISIBLE);
		} else {
			contactphonenoTxt.setText("");
			l_contactphonenoTxt.setVisibility(View.GONE);
		} /* Phone type 2 data population */
		if (contact.getcontacts_business_telephone_number() != null
				&& !contact.getcontacts_business_telephone_number().isEmpty()) {
			contactphoneno2Txt.setText(contact
					.getcontacts_business_telephone_number());

			phonetype2.setText(contact.getContact_phone_number_mobile_type2());
			phonemobile = contact.getcontacts_business_telephone_number();
			l_contactphoneno2Txt.setVisibility(View.VISIBLE);
		} else {
			contactphoneno2Txt.setText("");
			l_contactphoneno2Txt.setVisibility(View.GONE);
		}
		if (contact.getcontacts_assistant_telephone_number() != null
				&& !contact.getcontacts_assistant_telephone_number().isEmpty()) {
			contactphoneno3Txt.setText(contact
					.getcontacts_assistant_telephone_number());

			phonetype3.setText(contact
					.getContacts_assistant_telephone_number_type());
			phonemobile = contact.getcontacts_assistant_telephone_number();
			l_contactphoneno3Txt.setVisibility(View.VISIBLE);
		} else {
			contactphoneno3Txt.setText("");
			l_contactphoneno3Txt.setVisibility(View.GONE);
		}
		if (contact.getcontacts_business2_telephone_number() != null
				&& !contact.getcontacts_business2_telephone_number().isEmpty()) {
			contactphoneno4Txt.setText(contact
					.getcontacts_business2_telephone_number());

			phonetype4.setText(contact
					.getContacts_business2_telephone_number_type());
			phonemobile = contact.getcontacts_business2_telephone_number();
			l_contactphoneno4Txt.setVisibility(View.VISIBLE);
		} else {
			contactphoneno4Txt.setText("");
			l_contactphoneno4Txt.setVisibility(View.GONE);
		}
		if (contact.getcontacts_business_fax_number() != null
				&& !contact.getcontacts_business_fax_number().isEmpty()) {
			contactphoneno5Txt.setText(contact
					.getcontacts_business_fax_number());

			phonetype5.setText(contact.getContacts_business_fax_number_type());
			phonemobile = contact.getcontacts_business_fax_number();
			l_contactphoneno5Txt.setVisibility(View.VISIBLE);
		} else {
			contactphoneno5Txt.setText("");
			l_contactphoneno5Txt.setVisibility(View.GONE);
		}
		if (contact.getcontacts_car_telephone_number() != null
				&& !contact.getcontacts_car_telephone_number().isEmpty()) {
			contactphoneno6Txt.setText(contact
					.getcontacts_car_telephone_number());

			phonetype6.setText(contact.getContacts_car_telephone_numbe_type());
			phonemobile = contact.getcontacts_car_telephone_number();
			l_contactphoneno6Txt.setVisibility(View.VISIBLE);
		} else {
			contactphoneno6Txt.setText("");
			l_contactphoneno6Txt.setVisibility(View.GONE);
		}
		if (contact.getcontacts_home2_telephone_number() != null
				&& !contact.getcontacts_home2_telephone_number().isEmpty()) {
			contactphoneno7Txt.setText(contact
					.getcontacts_home2_telephone_number());

			phonetype7.setText(contact
					.getContacts_home2_telephone_number_type());
			phonemobile = contact.getcontacts_home2_telephone_number();
			l_contactphoneno7Txt.setVisibility(View.VISIBLE);
		} else {
			contactphoneno7Txt.setText("");
			l_contactphoneno7Txt.setVisibility(View.GONE);
		}
		if (contact.getcontacts_home_fax_number() != null
				&& !contact.getcontacts_home_fax_number().isEmpty()) {
			contactphoneno8Txt.setText(contact.getcontacts_home_fax_number());

			phonetype8.setText(contact.getContacts_home_fax_number_type());
			phonemobile = contact.getcontacts_home_fax_number();
			l_contactphoneno8Txt.setVisibility(View.VISIBLE);
		} else {
			contactphoneno8Txt.setText("");
			l_contactphoneno8Txt.setVisibility(View.GONE);
		}
		if (contact.getcontacts_home_telephone_number() != null
				&& !contact.getcontacts_home_telephone_number().isEmpty()) {
			contactphoneno9Txt.setText(contact
					.getcontacts_home_telephone_number());

			phonetype9
					.setText(contact.getContacts_home_telephone_number_type());
			phonemobile = contact.getcontacts_home_telephone_number();
			l_contactphoneno9Txt.setVisibility(View.VISIBLE);
		} else {
			contactphoneno9Txt.setText("");
			l_contactphoneno9Txt.setVisibility(View.GONE);
		}
		if (contact.getcontacts_pager_number() != null
				&& !contact.getcontacts_pager_number().isEmpty()) {
			contactphoneno10Txt.setText(contact.getcontacts_pager_number());

			phonetype10.setText(contact.getContacts_pager_number_type());
			phonemobile = contact.getcontacts_pager_number();
			l_contactphoneno10Txt.setVisibility(View.VISIBLE);
		} else {
			contactphoneno10Txt.setText("");
			l_contactphoneno10Txt.setVisibility(View.GONE);
		}
		if (contact.getcontacts_radio_telephone_number() != null
				&& !contact.getcontacts_radio_telephone_number().isEmpty()) {
			contactphoneno11Txt.setText(contact
					.getcontacts_radio_telephone_number());

			phonetype11.setText(contact
					.getContacts_radio_telephone_number_type());
			phonemobile = contact.getcontacts_radio_telephone_number();
			l_contactphoneno11Txt.setVisibility(View.VISIBLE);
		} else {
			contactphoneno11Txt.setText("");
			l_contactphoneno11Txt.setVisibility(View.GONE);
		}
		if (contact.getContacts2_company_main_phone() != null
				&& !contact.getContacts2_company_main_phone().isEmpty()) {
			contactphoneno12Txt.setText(contact
					.getContacts2_company_main_phone());

			phonetype12.setText(contact.getContacts2_company_main_phone_type());
			phonemobile = contact.getContacts2_company_main_phone();
			l_contactphoneno12Txt.setVisibility(View.VISIBLE);
		} else {
			contactphoneno12Txt.setText("");
			l_contactphoneno12Txt.setVisibility(View.GONE);
		}
		if (contact.getContact_custom_phone1() != null
				&& !contact.getContact_custom_phone1().isEmpty()) {
			contactphoneno13Txt.setText(contact.getContact_custom_phone1());

			phonetype13.setText(contact.getContact_custom_phone1_type());
			phonemobile = contact.getContact_custom_phone1();
			l_contactphoneno13Txt.setVisibility(View.VISIBLE);
		} else {
			contactphoneno13Txt.setText("");
			l_contactphoneno13Txt.setVisibility(View.GONE);
		}
		if (contact.getContact_custom_phone2() != null
				&& !contact.getContact_custom_phone2().isEmpty()) {
			contactphoneno14Txt.setText(contact.getContact_custom_phone2());

			phonetype14.setText(contact.getContact_custom_phone2_type());
			phonemobile = contact.getContact_custom_phone2();
			l_contactphoneno14Txt.setVisibility(View.VISIBLE);
		} else {
			contactphoneno14Txt.setText("");
			l_contactphoneno14Txt.setVisibility(View.GONE);
		}
		if (contact.getContact_custom_phone3() != null
				&& !contact.getContact_custom_phone3().isEmpty()) {
			contactphoneno15Txt.setText(contact.getContact_custom_phone3());

			phonetype15.setText(contact.getContact_custom_phone3_type());
			phonemobile = contact.getContact_custom_phone3();
			l_contactphoneno15Txt.setVisibility(View.VISIBLE);
		} else {
			contactphoneno15Txt.setText("");
			l_contactphoneno15Txt.setVisibility(View.GONE);
		}

		if (contact.getcontacts_email1_address() != null
				&& !contact.getcontacts_email1_address().isEmpty()) {
			contactemailTxt.setText(contact.getcontacts_email1_address());
			emailtype.setText(contact.getContact_email1_type());
			primemail = contact.getcontacts_email1_address();
			l_contactemail.setVisibility(View.VISIBLE);
			l_contactemailTxt.setVisibility(View.VISIBLE);
		} else {
			contactemailTxt.setText("");
			emailtype.setText("");
			primemail = "";
			l_contactemailTxt.setVisibility(View.GONE);
		}

		if (contact.getcontacts_email2_address() != null
				&& !contact.getcontacts_email2_address().isEmpty()) {
			contactemail2Txt.setText(contact.getcontacts_email2_address());
			emailtype2.setText(contact.getContact_email2_type());
			primemail = contact.getcontacts_email2_address();
			l_contactemail.setVisibility(View.VISIBLE);
			l_contactemail2Txt.setVisibility(View.VISIBLE);
		} else {
			contactemail2Txt.setText("");
			emailtype2.setText("");
			primemail = "";
			l_contactemail2Txt.setVisibility(View.GONE);
		}
		if (contact.getcontacts_email3_address() != null
				&& !contact.getcontacts_email3_address().isEmpty()) {
			contactemail3Txt.setText(contact.getcontacts_email3_address());
			emailtype3.setText(contact.getContact_email3_type());
			primemail = contact.getcontacts_email3_address();
			l_contactemail.setVisibility(View.VISIBLE);
			l_contactemail3Txt.setVisibility(View.VISIBLE);
		} else {
			contactemail3Txt.setText("");
			emailtype3.setText("");
			primemail = "";
			l_contactemail3Txt.setVisibility(View.GONE);
		}
		if (contact.getContacts_custom_email1_address() != null
				&& !contact.getContacts_custom_email1_address().isEmpty()) {
			contactemail4Txt.setText(contact
					.getContacts_custom_email1_address());
			emailtype4.setText(contact.getContact_custom_email1_type());
			primemail = contact.getContacts_custom_email1_address();
			l_contactemail.setVisibility(View.VISIBLE);
			l_contactemail4Txt.setVisibility(View.VISIBLE);
		} else {
			contactemail4Txt.setText("");
			emailtype4.setText("");
			primemail = "";
			l_contactemail4Txt.setVisibility(View.GONE);
		}
		if (contact.getContacts_custom_email2_address() != null
				&& !contact.getContacts_custom_email2_address().isEmpty()) {
			contactemail5Txt.setText(contact
					.getContacts_custom_email2_address());
			emailtype5.setText(contact.getContact_custom_email2_type());
			primemail = contact.getContacts_custom_email2_address();
			l_contactemail.setVisibility(View.VISIBLE);
			l_contactemail5Txt.setVisibility(View.VISIBLE);
		} else {
			contactemail5Txt.setText("");
			emailtype5.setText("");
			primemail = "";
			l_contactemail5Txt.setVisibility(View.GONE);
		}
		if (contact.getContacts_custom_email3_address() != null
				&& !contact.getContacts_custom_email3_address().isEmpty()) {
			contactemail6Txt.setText(contact
					.getContacts_custom_email3_address());
			emailtype6.setText(contact.getContact_custom_email3_type());
			primemail = contact.getContacts_custom_email3_address();
			l_contactemail.setVisibility(View.VISIBLE);
			l_contactemail6Txt.setVisibility(View.VISIBLE);
		} else {
			contactemail6Txt.setText("");
			emailtype6.setText("");
			primemail = "";
			l_contactemail6Txt.setVisibility(View.GONE);
		}

		if (contact.getcontacts_email1_address() == null
				&& contact.getcontacts_email1_address().isEmpty()
				&& contact.getcontacts_email2_address() == null
				&& contact.getcontacts_email2_address().isEmpty()
				&& contact.getcontacts_email3_address() == null
				&& contact.getcontacts_email3_address().isEmpty()
				&& contact.getContacts_custom_email1_address() == null
				&& contact.getContacts_custom_email1_address().isEmpty()
				&& contact.getContacts_custom_email2_address() == null
				&& contact.getContacts_custom_email2_address().isEmpty()
				&& contact.getContacts_custom_email3_address() == null
				&& contact.getContacts_custom_email3_address().isEmpty()) {
			l_contactemail.setVisibility(View.GONE);
		}

		// Populate Business Location
		if (contact.getContacts_business_location() != null
				&& !contact.getContacts_business_location().isEmpty()) {
			try {

				if (contact   .getcontacts_business_address_street() != null
				&& !contact.getcontacts_business_address_street().isEmpty()) {
					addrsMap = contact.getcontacts_business_address_street();
				}
				
				if (contact.getcontacts_business_address_city() != null
						&& !contact.getcontacts_business_address_city().isEmpty() && addrsMap.isEmpty()) {
					addrsMap = contact.getcontacts_business_address_city();
				}
				
				if (contact.getcontacts_business_address_state() != null
						&& !contact.getcontacts_business_address_state().isEmpty() && addrsMap.isEmpty()) {
					addrsMap = contact.getcontacts_business_address_state();
				}
				
				if (contact.getcontacts_business_address_postal_code() != null
						&& !contact.getcontacts_business_address_postal_code().isEmpty() && addrsMap.isEmpty()) {
					addrsMap = contact.getcontacts_business_address_postal_code();
				}
				
				if (contact.getcontacts_business_address_country() != null
						&& !contact.getcontacts_business_address_country().isEmpty() && addrsMap.isEmpty()) {
					addrsMap = contact.getcontacts_business_address_country();
				}
				
				
				/*
				String val[] = (contact.getContacts_business_location())
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

			*/} catch (Exception e) {

			}
			contactaddrsTxt1.setText(contact.getContacts_business_location());
			addresstype1.setText(contact.getContact_business_location_type());
			l_contactaddr.setVisibility(View.VISIBLE);
			l_contactaddrTxt.setVisibility(View.VISIBLE);
		}

		else {
			contactaddrsTxt1.setText("");
			addresstype1.setText("");
			l_contactaddrTxt.setVisibility(View.GONE);
		}

		// Populate Home Location
		if (contact.getContacts_home_location() != null
				&& !contact.getContacts_home_location().isEmpty()) {
			try {
				String val[] = (contact.getContacts_home_location())
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

			System.out.println("%%%%%%%%%"
					+ contact.getContacts_home_location());
			contactaddrsTxt2.setText(contact.getContacts_home_location());
			addresstype2.setText(contact.getContact_home_location_type());
			l_contactaddr.setVisibility(View.VISIBLE);
			l_contactaddr2Txt.setVisibility(View.VISIBLE);
		}

		else {
			System.out.println("%%%%%%%%%" + "null");
			contactaddrsTxt2.setText("");
			addresstype2.setText("");
			l_contactaddr2Txt.setVisibility(View.GONE);
		}

		// Populate Home Location
		if (contact.getContacts_other_location() != null
				&& !contact.getContacts_other_location().isEmpty()) {
			try {
				String val[] = (contact.getContacts_other_location())
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
			System.out.println("%%%%%%%%%" + "home  "
					+ contact.getContacts_other_location());
			contactaddrsTxt3.setText(contact.getContacts_other_location());
			addresstype3.setText(contact.getContact_other_location_type());
			l_contactaddr.setVisibility(View.VISIBLE);
			l_contactaddr3Txt.setVisibility(View.VISIBLE);
		}

		else {
			System.out.println("%%%%%%%%%" + "null");
			contactaddrsTxt3.setText("");
			addresstype3.setText("");
			l_contactaddr3Txt.setVisibility(View.GONE);
		}

		if (contact.getContacts_business_location() == null
				&& contact.getContacts_home_location() == null
				&& contact.getContacts_other_location() == null) {
			l_contactaddr.setVisibility(View.GONE);
		}

		String im = contact.getContact_im_address();
		if (im != null && !im.isEmpty()) {
			contactimnameTxt.setText(im);
			imtype.setText(contact.getContact_im_address_type());
			l_contactim.setVisibility(View.VISIBLE);
			l_contactimTxt.setVisibility(View.VISIBLE);
		} else {
			contactimnameTxt.setText("");
			imtype.setText("");
			l_contactimTxt.setVisibility(View.GONE);
		}

		if (contact.getContact_im_address1() != null
				&& !contact.getContact_im_address1().isEmpty()) {
			contactimname2Txt.setText(contact.getContact_im_address1());
			imtype2.setText(contact.getContact_im_address1_type());
			l_contactim.setVisibility(View.VISIBLE);
			l_contactim2Txt.setVisibility(View.VISIBLE);
		} else {
			contactimname2Txt.setText("");
			imtype2.setText("");
			l_contactim2Txt.setVisibility(View.GONE);
		}

		if (contact.getContact_im_address2() != null
				&& !contact.getContact_im_address2().isEmpty()) {
			contactimname3Txt.setText(contact.getContact_im_address2());
			imtype3.setText(contact.getContact_im_address2_type());
			l_contactim.setVisibility(View.VISIBLE);
			l_contactim3Txt.setVisibility(View.VISIBLE);
		} else {
			contactimname3Txt.setText("");
			imtype3.setText("");
			l_contactim3Txt.setVisibility(View.GONE);
		}
		if (contact.getContact_custom_im_address() != null
				&& !contact.getContact_custom_im_address().isEmpty()) {
			contactimname4Txt.setText(contact.getContact_custom_im_address());
			imtype4.setText(contact.getContact_custom_im_address_type());
			l_contactim.setVisibility(View.VISIBLE);
			l_contactim4Txt.setVisibility(View.VISIBLE);
		} else {
			contactimname4Txt.setText("");
			imtype4.setText("");
			l_contactim4Txt.setVisibility(View.GONE);
		}

		if (contact.getContact_custom_im1_address() != null
				&& !contact.getContact_custom_im1_address().isEmpty()) {
			contactimname5Txt.setText(contact.getContact_custom_im1_address());
			imtype5.setText(contact.getContact_custom_im1_address_type());
			l_contactim.setVisibility(View.VISIBLE);
			l_contactim5Txt.setVisibility(View.VISIBLE);
		} else {
			contactimname5Txt.setText("");
			imtype5.setText("");
			l_contactim5Txt.setVisibility(View.GONE);
		}

		if (contact.getContact_custom_im2_address() != null
				&& !contact.getContact_custom_im2_address().isEmpty()) {
			contactimname6Txt.setText(contact.getContact_custom_im2_address());
			imtype6.setText(contact.getContact_custom_im2_address_type());
			l_contactim.setVisibility(View.VISIBLE);
			l_contactim6Txt.setVisibility(View.VISIBLE);
		} else {
			contactimname6Txt.setText("");
			imtype6.setText("");
			l_contactim6Txt.setVisibility(View.GONE);
		}

		if (contact.getContact_im_address() == null
				&& contact.getContact_im_address1() == null
				&& contact.getContact_im_address2() == null
				&& contact.getContact_custom_im_address() == null
				&& contact.getContact_custom_im1_address() == null
				&& contact.getContact_custom_im2_address() == null) {
			l_contactim.setVisibility(View.GONE);
		}

		if (contact.getContact_notes() != null
				&& !contact.getContact_notes().isEmpty()) {
			contactnotesTxt.setText(contact.getContact_notes());
			l_contactnotesTxt.setVisibility(View.VISIBLE);
		}

		if (contact.getContact_website() != null
				&& !contact.getContact_website().isEmpty()) {
			contactWebsiteTxt.setText(contact.getContact_website());
			l_contactWebsiteTxt.setVisibility(View.VISIBLE);
		}

		if (contact.getContact_nick_name() != null
				&& !contact.getContact_nick_name().isEmpty()) {
			contactNicknameTxt.setText(contact.getContact_nick_name());
			l_nickname.setVisibility(View.VISIBLE);
		}

		if (contact.getContact_internetcall() != null
				&& !contact.getContact_internetcall().isEmpty()) {
			contactInternetCallTxt.setText(contact.getContact_internetcall());
			l_contactInternetCallTxt.setVisibility(View.VISIBLE);
		}

		if (cModel.isNativeContact()) {
			System.out.println("=============NATIVE ===========" + "NATIVE");

			SharedPreferences.Editor prefEditor = new SharedPreferences(
					Email.getAppContext()).edit();
			prefEditor.putBoolean("ContactsNative", true);
			prefEditor.commit();
			Log.e("ContactsDetailFragment ContactsNative:",
					"ContactsNative: "
							+ new SharedPreferences(Email
									.getAppContext()).getBoolean(
									"ContactsNative", false));

			ContactsUtilities.SELECTED_ID = null;

		} else if (cModel.isContactfromSearch()) {
			SharedPreferences.Editor prefEditor = new SharedPreferences(
					Email.getAppContext()).edit();
			prefEditor.putBoolean("ContactsNative", false);
			prefEditor.commit();
			ContactsUtilities.SELECTED_ID = null;
		} else {

			SharedPreferences.Editor prefEditor = new SharedPreferences(
					Email.getAppContext()).edit();
			prefEditor.putBoolean("ContactsNative", false);
			prefEditor.commit();
			Log.e("ContactsDetailFragment ContactsNative:",
					"ContactsNative: "
							+ new SharedPreferences(Email
									.getAppContext()).getBoolean(
									"ContactsNative", false));

			System.out.println("=============CORPORATE ==========="
					+ "CORPORATE");
			ContactsUtilities.SELECTED_ID = String.valueOf(contact
					.getContact_id());
			fav_icon_view.setVisibility(View.VISIBLE);
		}

		// byte[] imageBytes = result.get(0).getContacts_image();

		imageBytes = contact.getContacts_image();

		if (imageBytes != null) {
			image_view.setImageBitmap(ContactsUtilities.getImage(imageBytes));
		}

		ContactsLog.d(
				TAG,
				"result.get(0).isContactfromSearch()"
						+ contact.isContactfromSearch());

		if (contact.isContactfromSearch()) {
			fav_icon_view.setVisibility(View.VISIBLE);
			fav_icon_view
					.setImageResource(R.drawable.contacts_ic_add_contact_holo_light);

		} else if (contact.isNativeContact()) {
			
			fav_icon_view.setVisibility(View.GONE);

		} else {
			fav_icon_view.setVisibility(View.VISIBLE);
			if (contact.getContact_isFavorite() == 0) {
				fav_icon_view
						.setImageResource(R.drawable.contacts_ic_menu_star_holo_dark);
				isFavorite = false;
			} else {
				fav_icon_view
						.setImageResource(R.drawable.contacts_ic_menu_star_holo_light);
				isFavorite = true;
			}
		}

	}

	@SuppressWarnings("static-access")
	private void pouplateContactDetailsByGalModel() {

		image_view.setVisibility(View.VISIBLE);

		String FirstName = cGalData.get(GalData.FIRST_NAME);
		String lastName = cGalData.get(GalData.LAST_NAME);
		String displayname = cGalData.get(GalData.DISPLAY_NAME);
		String title = cGalData.get(GalData.TITLE);

		if (displayname == null) {
			displayname = FirstName;
		}
		getSherlockActivity().getSupportActionBar().setTitle(displayname);

		if (lastName != null && !lastName.isEmpty()) {

			if (FirstName != null && !FirstName.isEmpty()) {
				getSherlockActivity().getSupportActionBar().setSubtitle(
						FirstName + " " + lastName);
				contact_name.setText(FirstName + " " + lastName);
			} else {
				contact_name.setText(lastName);
			}

		} else {
			if (FirstName != null && !FirstName.isEmpty()) {
				getSherlockActivity().getSupportActionBar().setSubtitle(
						FirstName);
				contact_name.setText(FirstName);
			} else {
				if (displayname != null && !displayname.isEmpty()) {
					// getSupportActionBar().setTitle(displayname);
					contact_name.setText(displayname);
				} else {

					// getSupportActionBar().setTitle("Empty");
					contact_name.setText("Empty");
				}
			}
		}

		getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(
				true);

		if (title != null && !title.isEmpty()) {
			// contact_designation.setText(title);

			contact_designation.setVisibility(View.VISIBLE);
			// contact_comma.setVisibility(View.VISIBLE);
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

			System.out.println("" + cGalData.COMPANY);
			contact_company.setText(company);
			contact_company.setVisibility(View.VISIBLE);

			// l_contact_company.setVisibility(View.VISIBLE);
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
		// if (office != null && !office.isEmpty()) {
		//
		// contact_company.setText(office);
		// contact_company.setVisibility(View.VISIBLE);
		// // l_desig_comp.setVisibility(View.VISIBLE);
		// }
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		if (mCursor != null) {
			mCursor.close();
		}
		super.onDestroyView();
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub

		switch (arg0.getId()) {

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
			sendsms();
			phonemobile = contactphoneno4Txt.getText().toString();
			ContactName = contact_name.getText().toString();
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
		// case R.id.Emailid:
		case R.id.contact_email_value:

			primemail = contactemailTxt.getText().toString();

			if (isEmailValid(primemail))
				sendmail();
			else
				Toast.makeText(getSherlockActivity(), "Email-ID is not valid",
						Toast.LENGTH_LONG).show();
			break;
		case R.id.contact_email_value2:
			primemail = contactemail2Txt.getText().toString();
			if (isEmailValid(primemail))
				sendmail();
			else
				Toast.makeText(getSherlockActivity(), "Email-ID is not valid",
						Toast.LENGTH_LONG).show();
			break;
		case R.id.contact_email_value3:
			primemail = contactemail3Txt.getText().toString();
			if (isEmailValid(primemail))
				sendmail();
			else
				Toast.makeText(getSherlockActivity(), "Email-ID is not valid",
						Toast.LENGTH_LONG).show();
			break;
		case R.id.contact_email_value4:
			primemail = contactemail4Txt.getText().toString();
			if (isEmailValid(primemail))
				sendmail();
			else
				Toast.makeText(getSherlockActivity(), "Email-ID is not valid",
						Toast.LENGTH_LONG).show();
			break;
		case R.id.contact_email_value5:
			primemail = contactemail5Txt.getText().toString();
			if (isEmailValid(primemail))
				sendmail();
			else
				Toast.makeText(getSherlockActivity(), "Email-ID is not valid",
						Toast.LENGTH_LONG).show();
			break;
		case R.id.contact_email_value6:
			primemail = contactemail6Txt.getText().toString();
			if (isEmailValid(primemail))
				sendmail();
			else
				Toast.makeText(getSherlockActivity(), "Email-ID is not valid",
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

			SecuredBrowserUrl = contactWebsiteTxt.getText().toString();

			CallSecuredBrowser();

		default:
			break;
		}

	}

	private void showMap(String address) {
		try {

			Log.e("Addressmap after formed", address);

			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format(
					"geo:0,0?q=%s", URLEncoder.encode(address))));
			startActivity(i);
		} catch (ActivityNotFoundException activityNotFound) {

			Toast.makeText(getActivity(),
					"Please install Google-Maps App, to view the map",
					Toast.LENGTH_LONG).show();

		}
	}

	public void CallSecuredBrowser() {

		/*if (!SecuredBrowserUrl.startsWith("_http://")
				&& !SecuredBrowserUrl.startsWith("_https://")) {
			SecuredBrowserUrl = "http://" + SecuredBrowserUrl;
		}*/

		Intent openWebAppIntent = new Intent(
				"com.cognizant.trumobi.securebrowser");
		if (openWebAppIntent != null) {
			openWebAppIntent.putExtra("toBrowser", SecuredBrowserUrl);
			//openWebAppIntent.putExtra("name", SecuredBrowserUrl);
			// openWebAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			/*openWebAppIntent
					.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);*/
			try {
				// activity.startActivity(openWebAppIntent);
				this.startActivity(openWebAppIntent);
				// result = true;
			} catch (ActivityNotFoundException ex) {
				// No applications can handle it. Ignore.
			}
		}
	}

	private void phonecall() {
		
		

		TelephonyManager tm = (TelephonyManager) getSherlockActivity()
				.getSystemService(Context.TELEPHONY_SERVICE);

		if(tm.getPhoneType()==TelephonyManager.PHONE_TYPE_NONE)
		{
			if (tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT) {
				 //coming here if Tablet WITHOUT SIM SLOT 
				Toast.makeText(getSherlockActivity(), "Not registered on network",
						Toast.LENGTH_SHORT).show();
			}else{
				 //coming here if Tablet WITH SIM SLOT 
				/*Toast.makeText(getSherlockActivity(), "TAB",
						Toast.LENGTH_SHORT).show();*/
				if (tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT) {

					isAirplaneEnabled = Settings.System.getInt(getSherlockActivity()
							.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1;
					if (isAirplaneEnabled) {
						Toast.makeText(getSherlockActivity(),
								"Flight mode on. Turn Flight mode off to make calls",
								Toast.LENGTH_SHORT).show();
					} else {
						
						SharedPreferences.Editor prefEditor = new SharedPreferences(
								Email.getAppContext()).edit();
						prefEditor.putBoolean("isCallLogUpdated", true);
						prefEditor.commit();
						
						DialerUtilities.phoneCallIntent(phonemobile,
								Email.getAppContext());
					}

				} else {

					Toast.makeText(getSherlockActivity(), "Not registered on network",
							Toast.LENGTH_SHORT).show();
				}
			}
		 
		}
		else{
			/*Toast.makeText(getSherlockActivity(), "phone",
					Toast.LENGTH_SHORT).show();*/
			//coming here if phone
			if (tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT) {

				isAirplaneEnabled = Settings.System.getInt(getSherlockActivity()
						.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1;
				if (isAirplaneEnabled) {
					Toast.makeText(getSherlockActivity(),
							"Flight mode on. Turn Flight mode off to make calls",
							Toast.LENGTH_SHORT).show();
				} else {
					
					SharedPreferences.Editor prefEditor = new SharedPreferences(
							Email.getAppContext()).edit();
					prefEditor.putBoolean("isCallLogUpdated", true);
					prefEditor.commit();
					
					DialerUtilities.phoneCallIntent(phonemobile,
							Email.getAppContext());
				}

			} else {

				Toast.makeText(getSherlockActivity(), "Not registered on network",
						Toast.LENGTH_SHORT).show();
			}
		}
		
		
	


	}

	private void sendsms() {
		
/*
		TelephonyManager tm = (TelephonyManager) getSherlockActivity()
				.getSystemService(Context.TELEPHONY_SERVICE);

		if (tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT) {

			isAirplaneEnabled = Settings.System.getInt(getSherlockActivity()
					.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1;
			if (isAirplaneEnabled) {
				Toast.makeText(getSherlockActivity(),
						"Flight mode on. Turn Flight mode off to make calls",
						Toast.LENGTH_SHORT).show();
			} else {

				// The phone has SIM card

				try {

					Intent callSMS = new Intent(getSherlockActivity(),
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

			Toast.makeText(getSherlockActivity(), "Not registered on network",
					Toast.LENGTH_SHORT).show();
		}*/
		
		
		
		
		
		
		
		

		TelephonyManager tm = (TelephonyManager) getSherlockActivity()
				.getSystemService(Context.TELEPHONY_SERVICE);

		if(tm.getPhoneType()==TelephonyManager.PHONE_TYPE_NONE)
		{
			if (tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT) {
				 //coming here if Tablet WITHOUT SIM SLOT 
				Toast.makeText(getSherlockActivity(), "Not registered on network",
						Toast.LENGTH_SHORT).show();
			}else{
				 //coming here if Tablet WITH SIM SLOT 
				/*Toast.makeText(getSherlockActivity(), "TAB",
						Toast.LENGTH_SHORT).show();*/
				if (tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT) {

					isAirplaneEnabled = Settings.System.getInt(getSherlockActivity()
							.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1;
					if (isAirplaneEnabled) {
						Toast.makeText(getSherlockActivity(),
								"Flight mode on. Turn Flight mode off to make calls",
								Toast.LENGTH_SHORT).show();
					} else {
						try {
							Intent callSMS = new Intent(getSherlockActivity(),
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

					Toast.makeText(getSherlockActivity(), "Not registered on network",
							Toast.LENGTH_SHORT).show();
				}
			}
		 
		}
		else{
			/*Toast.makeText(getSherlockActivity(), "phone",
					Toast.LENGTH_SHORT).show();*/
			//coming here if phone
			if (tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT) {

				isAirplaneEnabled = Settings.System.getInt(getSherlockActivity()
						.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1;
				if (isAirplaneEnabled) {
					Toast.makeText(getSherlockActivity(),
							"Flight mode on. Turn Flight mode off to make calls",
							Toast.LENGTH_SHORT).show();
				} else {
					try {
						Intent callSMS = new Intent(getSherlockActivity(),
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

				Toast.makeText(getSherlockActivity(), "Not registered on network",
						Toast.LENGTH_SHORT).show();
			}
		}
		
		
		

	}

	public static boolean isEmailValid(String email) {

		return true;

		/*
		 * boolean isValid = false;
		 * 
		 * String TempMail = email; String TempMailbackup = email;
		 * 
		 * String[] TempMailbackup1; String[] TempMailbackup2; String
		 * ServerMail1; String ServerMail2;
		 * 
		 * if (TempMailbackup.contains("\"") && TempMailbackup.contains(">")) {
		 * if (TempMailbackup.startsWith("\"") && TempMailbackup.endsWith(">"))
		 * { Log.e("TempMailbackup", "TempMailbackup" + TempMailbackup); if
		 * (TempMailbackup.contains("\" <")) { TempMailbackup1 =
		 * TempMailbackup.split("\" <"); Log.e("TempMailbackup1", "" +
		 * TempMailbackup1[0]); Log.e("TempMailbackup1", "" +
		 * TempMailbackup1[1]);
		 * 
		 * ServerMail1 = TempMailbackup1[0].substring(1); ServerMail2 =
		 * TempMailbackup1[1].substring(0, TempMailbackup1[1].length() - 1);
		 * Log.e("ServerMail1", "" + ServerMail1); Log.e("ServerMail2", "" +
		 * ServerMail2); if (ServerMail1.equals(ServerMail2)) { String
		 * expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
		 * 
		 * Pattern pattern = Pattern.compile(expression,
		 * Pattern.CASE_INSENSITIVE); Matcher matcher =
		 * pattern.matcher(ServerMail1); if (matcher.matches()) { isValid =
		 * true; } } else { String expression =
		 * "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"; CharSequence inputStr =
		 * email;
		 * 
		 * Pattern pattern = Pattern.compile(expression,
		 * Pattern.CASE_INSENSITIVE); Matcher matcher =
		 * pattern.matcher(inputStr); if (matcher.matches()) { isValid = true; }
		 * }
		 * 
		 * } else { String expression =
		 * "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"; CharSequence inputStr =
		 * email;
		 * 
		 * Pattern pattern = Pattern.compile(expression,
		 * Pattern.CASE_INSENSITIVE); Matcher matcher =
		 * pattern.matcher(inputStr); if (matcher.matches()) { isValid = true; }
		 * }
		 * 
		 * } else { String expression =
		 * "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"; CharSequence inputStr =
		 * email;
		 * 
		 * Pattern pattern = Pattern.compile(expression,
		 * Pattern.CASE_INSENSITIVE); Matcher matcher =
		 * pattern.matcher(inputStr); if (matcher.matches()) { isValid = true; }
		 * }
		 * 
		 * } else { String expression =
		 * "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"; CharSequence inputStr =
		 * email;
		 * 
		 * Pattern pattern = Pattern.compile(expression,
		 * Pattern.CASE_INSENSITIVE); Matcher matcher =
		 * pattern.matcher(inputStr); if (matcher.matches()) { isValid = true; }
		 * }
		 * 
		 * return isValid;
		 * 
		 * // WORKING FINE except for "xyz@m.com"<xyz@m.com>
		 * 
		 * boolean isValid = false;
		 * 
		 * String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
		 * CharSequence inputStr = email;
		 * 
		 * Pattern pattern = Pattern.compile(expression,
		 * Pattern.CASE_INSENSITIVE); Matcher matcher =
		 * pattern.matcher(inputStr); if (matcher.matches()) { isValid = true; }
		 * return isValid;
		 */
	}

	private void sendmail() {

		try {
			Intent targetedShareIntent = new Intent(getSherlockActivity(),
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
			Toast.makeText(getSherlockActivity(),
					"Secured apps not available to send Email",
					Toast.LENGTH_LONG).show();
		}

		/*
		 * try {
		 * 
		 * List<Intent> targetedShareIntents = new ArrayList<Intent>(); Intent
		 * shareIntent = new Intent(android.content.Intent.ACTION_SEND);
		 * shareIntent.setType("text/plain");
		 * 
		 * List<ResolveInfo> resInfo = getSherlockActivity()
		 * .getPackageManager().queryIntentActivities(shareIntent, 0); if
		 * (!resInfo.isEmpty()) { for (ResolveInfo resolveInfo : resInfo) {
		 * String packageName = resolveInfo.activityInfo.packageName; Intent
		 * targetedShareIntent = new Intent(
		 * android.content.Intent.ACTION_SENDTO);
		 * targetedShareIntent.setType("text/plain");
		 * 
		 * targetedShareIntent.putExtra(Intent.EXTRA_SUBJECT, "");
		 * targetedShareIntent.putExtra(Intent.EXTRA_TEXT, "");
		 * targetedShareIntent.setData(Uri .parse("mailto:" + primemail));
		 * targetedShareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		 * 
		 * if (packageName.equals("com.cognizant.trumobi")) {
		 * 
		 * targetedShareIntent.setPackage(packageName);
		 * targetedShareIntents.add(targetedShareIntent);
		 * 
		 * // Add messaging app package name //
		 * targetedShareIntent.setPackage(packageName); //
		 * targetedShareIntents.add(targetedShareIntent);
		 * 
		 * } else { // Add messaging app package name //
		 * targetedShareIntent.setPackage(packageName); //
		 * targetedShareIntents.add(targetedShareIntent); } }
		 * 
		 * if (targetedShareIntents.size() > 0) { Intent chooserIntent =
		 * Intent.createChooser( targetedShareIntents.remove(0),
		 * "Secured Email");
		 * chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
		 * targetedShareIntents.toArray(new Parcelable[] {}));
		 * 
		 * startActivity(chooserIntent); } else {
		 * Toast.makeText(getSherlockActivity(),
		 * "Secured apps not available to send Email",
		 * Toast.LENGTH_LONG).show();
		 * 
		 * } }
		 * 
		 * } catch (ActivityNotFoundException activityException) {
		 * Log.e("Secured apps not available to send Email", "Call failed"); }
		 */

		/*
		 * try { Intent intent = new Intent(Intent.ACTION_SENDTO);
		 * intent.setType("text/plain"); intent.putExtra(Intent.EXTRA_SUBJECT,
		 * "Subject of email"); intent.putExtra(Intent.EXTRA_TEXT,
		 * "Body of email"); intent.setData(Uri.parse("mailto:" + primemail));
		 * intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		 * startActivity(intent);
		 * 
		 * } catch (ActivityNotFoundException activityException) {
		 * Log.e("sendsms activity not found", "Call failed"); }
		 */
	}

	/*
	 * @Override public boolean onOptionsItemSelected(MenuItem item) {
	 * 
	 * Fragment currentFragment =
	 * this;//mAdapter.getItem(mPager.getCurrentItem()); if (currentFragment !=
	 * null && currentFragment instanceof SherlockFragment) {
	 * ((SherlockFragment)currentFragment).onOptionsItemSelected(item); } //
	 * return super.onOptionsItemSelected(item);
	 * 
	 * 
	 * if (item.getItemId() == android.R.id.home) { finish();
	 * 
	 * } return super.onOptionsItemSelected(item); };
	 * 
	 * }
	 * 
	 * 
	 * 
	 * FragmentManager fragmentManager = getSupportFragmentManager();
	 * FragmentTransaction fragmentTransaction =
	 * fragmentManager.beginTransaction();
	 * 
	 * switch (item.getItemId()) {
	 * 
	 * case android.R.id.home: // Toast.makeText(this, "Home selected",
	 * Toast.LENGTH_SHORT).show(); getSherlockActivity().finish(); break;
	 * 
	 * case R.id.indContEdit: Toast.makeText(getSherlockActivity(),
	 * "Edit selected", Toast.LENGTH_SHORT).show();
	 * 
	 * // Intent intent = new //
	 * Intent(ContactsDetailActivity.this,EditContact.class); //
	 * startActivity(intent);
	 * 
	 * break; case R.id.indContShare: Toast.makeText(getSherlockActivity(),
	 * "Share selected", Toast.LENGTH_SHORT).show(); break; case
	 * R.id.indContDelete: Toast.makeText(getSherlockActivity(),
	 * "Delete selected", Toast.LENGTH_SHORT).show();
	 * 
	 * 
	 * Intent intent = new Intent(ParentActivity.this, contactstodisplay.class);
	 * startActivity(intent);
	 * 
	 * 
	 * new AlertDialog.Builder(getSherlockActivity()).setMessage(
	 * "This contact will be deleted.") .setPositiveButton("OK", new
	 * DialogInterface.OnClickListener() {
	 * 
	 * @Override public void onClick(DialogInterface dialog, int which) {
	 * getSherlockActivity
	 * ().getContentResolver().delete(ContactsConsts.CONTENT_URI_CONTACTS,
	 * "_id = ?", new String[] { receivedExtra }); updateLocalCache(); }
	 * }).setNegativeButton("Cancel", null).create().show(); break;
	 * 
	 * case R.id.indContPlaceonHomescreen: Toast.makeText(getSherlockActivity(),
	 * "Place on Home screen selected", Toast.LENGTH_SHORT).show(); break;
	 * 
	 * default: break; }
	 * 
	 * return true;
	 * 
	 * }
	 * 
	 * @Override public void onCreateOptionsMenu(Menu menu, MenuInflater
	 * inflater) { menu.clear(); inflater =
	 * getSherlockActivity().getSupportMenuInflater();
	 * inflater.inflate(R.menu.individualcontactsettings, menu);
	 * super.onCreateOptionsMenu(menu, inflater); }
	 * 
	 * private void updateLocalCache() { ContentValues deleteValues = new
	 * ContentValues(); String deletedServerID = getSherlockActivity()
	 * .getContentResolver() .query(ContactsConsts.CONTENT_URI_CONTACTS, new
	 * String[] { ContactsConsts.CONTACT_SERVER_ID }, "_id = ?", new String[] {
	 * receivedExtra }, null).getString(0);
	 * System.out.println("*****DELETED SERVER_ID*****"+deletedServerID);
	 * deleteValues.put(ContactsConsts.CONTACT_SERVER_ID, deletedServerID);
	 * getSherlockActivity().getContentResolver().insert(ContactsConsts.
	 * CONTENT_URI_CONTACTS_LOCAL_CACHE_DELETE, deleteValues); }
	 */
}
