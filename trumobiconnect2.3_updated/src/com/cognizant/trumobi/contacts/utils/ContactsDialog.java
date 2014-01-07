package com.cognizant.trumobi.contacts.utils;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.TruBoxSDK.SharedPreferences;
import com.cognizant.trubox.contacts.db.ContactsModel;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobiTimerClass;
import com.cognizant.trumobi.dialer.utils.DialerUtilities;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.em.activity.EmMessageCompose;
import com.cognizant.trumobi.exchange.provider.EmGalResult.GalData;
import com.cognizant.trumobi.log.ContactsLog;
import com.cognizant.trumobi.messenger.sms.SmsMain;

public class ContactsDialog extends Dialog implements
android.view.View.OnClickListener {

	static boolean isAirplaneEnabled;

	String addrsMap = "";
	String addrsMap2 = "";
	String addrsMap3 = "";
	
	
	
	String SecuredBrowserUrl = "";

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

	LinearLayout l_contactemail;
	LinearLayout l_contactemailTxt;
	LinearLayout l_contactemail2Txt;
	LinearLayout l_contactemail3Txt;
	LinearLayout l_contactemail4Txt;
	LinearLayout l_contactemail5Txt;
	LinearLayout l_contactemail6Txt;

	TextView contactemailTxt;
	TextView contactemail2Txt;
	TextView contactemail3Txt;
	TextView contactemail4Txt;
	TextView contactemail5Txt;
	TextView contactemail6Txt;

	TextView emailtype;
	TextView emailtype2;
	TextView emailtype3;
	TextView emailtype4;
	TextView emailtype5;
	TextView emailtype6;

	
	
	LinearLayout l_contactaddr;
	LinearLayout l_contactaddrTxt;
	LinearLayout l_contactaddr2Txt;
	LinearLayout l_contactaddr3Txt;
	
	LinearLayout l_contactaddrsTxt1;
	TextView contactaddrsTxt1;
	TextView addresstype1;
	
	//LinearLayout l_contactaddrsTxt2;
	TextView contactaddrsTxt2;
	TextView addresstype2;
	

	
	//LinearLayout l_contactaddrsTxt3;
	TextView contactaddrsTxt3;
	TextView addresstype3;
	
	TextView contactWebsiteTxt;
	LinearLayout l_contactWebsiteTxt;

	byte[] imageBytes = null;

	LinearLayout phonetab;
	LinearLayout emailtab;

	String lastname = "";
	String firstname = "";
	String fullname = "";
	String phonemobile = "";
	String primemail = "";
	final Dialog dialog;
	protected Resources res;
	// Cursor mCursor;
	Context mContext;
	TextView txtfullname;
	// TextView phonetext;
	// TextView phonetype;
	// ImageView textmessage;
	// TextView Emailid;
	// TextView Emailtype;
	ContactsModel cModel = null;
	GalData cGalData = null;

	ImageView popupcontactimage;

	String cursorPosition;
	String selectionArgument = "_id = ?";

	public Dialog getDialog(){
		return dialog;
	}
	
	public ContactsDialog(Context context, ContactsModel contactObj) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext = context;

		cModel = contactObj;
		
		//dialog = new Dialog(mContext);
		//Code Implemented for handling the PIN screen Lock issue
		dialog = new Dialog(mContext)
		{
			public boolean dispatchTouchEvent(MotionEvent event)  
			{
				//ContactsLog.d(TAG,"=====onTouchEvent ==== ");
				Rect dialogBounds = new Rect();
			    getWindow().getDecorView().getHitRect(dialogBounds);
			    if (dialogBounds.contains((int) event.getX(), (int) event.getY())) {
			    	TruMobiTimerClass.userInteractedTrigger();
			    } 
			    return super.dispatchTouchEvent(event);
			}
		};
				
		
		
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setCanceledOnTouchOutside(true);
		dialog.setContentView(R.layout.contacts_popup);

		ContactsLog.d(ContactsUtilities.TAG, "Contact " + cModel);

		if (cModel != null) {
			cursorPosition = String.valueOf(cModel.getContact_id());
			ArrayList<ContactsModel> result;
			if (cModel.isNativeContact()) {
				result = ContactsUtilities.getNativeContactsDetailsFromCursor(
						Email.getAppContext().getContentResolver(),
						cursorPosition);
			} else if (cModel.isContactfromSearch()) {
				result = new ArrayList<ContactsModel>();
				result.add(cModel);
			} else {
				result = ContactsUtilities.getContactsDetailsFromCursor(
						Email.getAppContext().getContentResolver(),
						cursorPosition);
			}
			if (result != null && result.size() > 0) {
				populateDialog(result);
			}
		}
	}

	public ContactsDialog(Context context, GalData cGalDataObj) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext = context;
		cGalData = cGalDataObj;
		
		//dialog = new Dialog(mContext);
		//Code Implemented for handling the PIN screen Lock issue
		dialog = new Dialog(mContext)
		{
			public boolean dispatchTouchEvent(MotionEvent event)  
			{
				//ContactsLog.d(TAG,"=====onTouchEvent ==== ");
				Rect dialogBounds = new Rect();
			    getWindow().getDecorView().getHitRect(dialogBounds);
			    if (dialogBounds.contains((int) event.getX(), (int) event.getY())) {
			    	TruMobiTimerClass.userInteractedTrigger();
			    } 
			    return super.dispatchTouchEvent(event);
			}
		};
	
		
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setCanceledOnTouchOutside(true);
		dialog.setContentView(R.layout.contacts_popup);
		populateDialogbyGalData();
	}

	public void populateDialogbyGalData() {

		if (cGalData != null) {

			String FirstName = cGalData.get(GalData.FIRST_NAME);
			String lastName = cGalData.get(GalData.LAST_NAME);
			String displayname = cGalData.get(GalData.DISPLAY_NAME);
			String title = cGalData.get(GalData.TITLE);

			if (displayname == null) {
				displayname = FirstName;
			}
			
			
			String viewOrder = new SharedPreferences(mContext).getString(
					"prefViewType", "First_name_first");

			if (viewOrder.equals("First_name_first")) {

			if (lastName != null && !lastName.isEmpty()) {

				if (FirstName != null && !FirstName.isEmpty()) {
					fullname = FirstName + " " + lastName;
				} else {
					fullname = lastName;
				}

			} else {
				if (FirstName != null && !FirstName.isEmpty()) {

					fullname = FirstName;
				} else {
					if (displayname != null && !displayname.isEmpty()) {
						// getSupportActionBar().setTitle(displayname);
						fullname = displayname;
					} else {

						// getSupportActionBar().setTitle("Empty");
						fullname = " ";
					}
				}
			}
			}
			else if(viewOrder.equals("Last_name_first"))
			{				


				if (lastName != null && !lastName.isEmpty()) {

					if (FirstName != null && !FirstName.isEmpty()) {
						fullname = lastName + "," + FirstName;
					} else {
						fullname = lastName;
					}

				} else {
					if (FirstName != null && !FirstName.isEmpty()) {

						fullname = FirstName;
					} else {
						if (displayname != null && !displayname.isEmpty()) {
							// getSupportActionBar().setTitle(displayname);
							fullname = displayname;
						} else {

							// getSupportActionBar().setTitle("Empty");
							fullname = " ";
						}
					}
				}
				
				
			}

			/*
			 * // Toast.makeText(mContext, "Saran", Toast.LENGTH_SHORT).show();
			 * //if(cGalData.get(GalData.FIRST_NAME).toString()!= null &
			 * !cGalData.get(GalData.FIRST_NAME).isEmpty()) //{ firstname =
			 * cGalData.get(GalData.FIRST_NAME); //}
			 * //if(cGalData.get(GalData.LAST_NAME).toString()!= null &
			 * !cGalData.get(GalData.LAST_NAME).isEmpty()) //{ lastname =
			 * cGalData.get(GalData.LAST_NAME); //} //String displayname =
			 * cGalData.get(GalData.DISPLAY_NAME); //String title =
			 * cGalData.get(GalData.TITLE); if(firstname!=null &&
			 * !firstname.isEmpty()) { fullname = firstname; if(lastname!=null
			 * && !lastname.isEmpty()) { fullname = firstname+ " " + lastname; }
			 * } else { fullname = lastname; }
			 */

			// String company = cGalData.get(GalData.COMPANY);
			// phonemobile = cGalData.get(GalData.WORK_PHONE);
			phonemobile = cGalData.get(GalData.MOBILE_PHONE);
			// String homePhone = cGalData.get(GalData.HOME_PHONE);
			primemail = cGalData.get(GalData.EMAIL_ADDRESS);
			// String office = cGalData.get(GalData.OFFICE);
			Log.i("popup fullname", "" + fullname);
			Log.i("popup phonemobile", "" + phonemobile);
			Log.i("popup primemail", "" + primemail);
			res = mContext.getResources();
			TabHost tabHost = (TabHost) dialog.findViewById(R.id.tabhost);
			tabHost.setup();
			TabHost.TabSpec spec;
			
			spec = tabHost
					.newTabSpec("status")
					.setIndicator(getTabIndicator(mContext, R.drawable.contacts_dailer_icon))
									.setContent(R.id.layoutTabPhone1);

			tabHost.addTab(spec);
			spec = tabHost
					.newTabSpec("actionitems")
					.setIndicator(getTabIndicator(mContext, R.drawable.contacts_email_icon))
									.setContent(R.id.layoutTabEmail2);
			tabHost.addTab(spec);
			
			tabHost.setCurrentTab(0);
			phonetab = (LinearLayout) dialog.findViewById(R.id.layoutTabPhone1);
			emailtab = (LinearLayout) dialog.findViewById(R.id.layoutTabEmail2);
			txtfullname = (TextView) dialog.findViewById(R.id.fullname);
			txtfullname.setText(fullname);
			// phonetext = (TextView) dialog.findViewById(R.id.Phonecall);

			contactphonenoTxt = (TextView) dialog
					.findViewById(R.id.contact_phone_no_value);
			contactphonenoTxt.setOnClickListener(this);
			phonetype = (TextView) dialog.findViewById(R.id.Phonetype);
			phonetype.setOnClickListener(this);

			l_contact_phone_no = (LinearLayout) dialog
					.findViewById(R.id.l_contact_phone_no);
			l_contactemail = (LinearLayout) dialog
					.findViewById(R.id.l_contact_email);

			if (phonemobile != null && !phonemobile.isEmpty()) {

				tabHost.setVisibility(View.VISIBLE);
				tabHost.getTabWidget().getChildAt(0)
				.setVisibility(View.VISIBLE);
				// phonetab.setVisibility(View.VISIBLE);
				l_contact_phone_no.setVisibility(View.VISIBLE);
				contactphonenoTxt.setText(phonemobile);
				contactphonenoTxt.setVisibility(View.VISIBLE);
				phonetype.setVisibility(View.VISIBLE);
				// phonetext.setOnClickListener(this);

			} else {
				// tabHost.setVisibility(View.GONE);
				l_contact_phone_no.setVisibility(View.GONE);
				phonetab.setVisibility(View.GONE);
				contactphonenoTxt.setVisibility(View.GONE);
				phonetype.setVisibility(View.GONE);
				tabHost.getTabWidget().getChildAt(0).setVisibility(View.GONE);
			}

			contactemailTxt = (TextView) dialog
					.findViewById(R.id.contact_email_value);
			contactemailTxt.setOnClickListener(this);
			emailtype = (TextView) dialog.findViewById(R.id.emailtype);
			emailtype.setOnClickListener(this);

			// Emailid = (TextView) dialog.findViewById(R.id.Emailid);
			if (primemail != null && !primemail.isEmpty()) {

				tabHost.setVisibility(View.VISIBLE);
				tabHost.getTabWidget().getChildAt(1)
				.setVisibility(View.VISIBLE);
				l_contactemail.setVisibility(View.VISIBLE);
				contactemailTxt.setVisibility(View.VISIBLE);
				contactemailTxt.setText(primemail);
				emailtype.setVisibility(View.VISIBLE);
				// emailtab.setVisibility(View.VISIBLE);
				// Emailid.setText(primemail);
				contactemailTxt.setText(primemail);
				// Emailid.setOnClickListener(this);

			} else {
				// tabHost.setVisibility(View.GONE);
				// emailtab.setVisibility(View.GONE);
				l_contactemail.setVisibility(View.GONE);
				contactemailTxt.setVisibility(View.GONE);
				emailtype.setVisibility(View.GONE);
				tabHost.getTabWidget().getChildAt(1).setVisibility(View.GONE);

			}
			phonetype = (TextView) dialog.findViewById(R.id.Phonetype);
			phonetype.setOnClickListener(this);
			textmessage = (ImageView) dialog.findViewById(R.id.textmessage);
			textmessage.setOnClickListener(this);
			// Emailtype = (TextView) dialog.findViewById(R.id.Emailtype);
			// Emailtype.setOnClickListener(this);

			if ((tabHost.getTabWidget().getChildAt(0).getVisibility() == View.GONE)
					&& (tabHost.getTabWidget().getChildAt(1).getVisibility() == View.GONE)) {
				tabHost.setVisibility(View.GONE);

				/*
				 * if (this.getWindow().getWindowManager().getDefaultDisplay()
				 * .getOrientation() ==
				 * ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) { // portrait mode
				 * dialog.getWindow().setLayout(250, 323); }
				 */

			} else {
				/*
				 * if (this.getWindow().getWindowManager().getDefaultDisplay()
				 * .getOrientation() ==
				 * ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) { // portrait mode
				 * dialog.getWindow().setLayout(600, 323); }
				 */
			}

			if ((tabHost.getTabWidget().getChildAt(0).getVisibility() == View.VISIBLE)) {
				tabHost.setCurrentTab(0);
			}

			else if ((tabHost.getTabWidget().getChildAt(1).getVisibility() == View.VISIBLE)) {
				tabHost.setCurrentTab(1);
			}

			dialog.show();

		}

	}

	public void populateDialog(ArrayList<ContactsModel> result) {

		// String name =
		// mCursor.getString(mCursor.getColumnIndex(ContactsConsts.CONTACT_LAST_NAME));

		// Toast.makeText(mContext, ""+name, Toast.LENGTH_SHORT).show();

		if (result != null && result.size() > 0) {

			/*	if (result.get(0).getcontacts_last_name() != null
					&& !result.get(0).getcontacts_last_name().isEmpty()) {
				lastname = result.get(0).getcontacts_last_name();
			}

			if (result.get(0).getcontacts_first_name() != null
					&& !result.get(0).getcontacts_first_name().isEmpty()) {
				firstname = result.get(0).getcontacts_first_name();
			}
			 */




			String viewOrder = new SharedPreferences(mContext).getString(
					"prefViewType", "First_name_first");

			if (viewOrder.equals("First_name_first")) {

				if (result.get(0).getcontacts_first_name() != null
						&& !result.get(0).getcontacts_first_name().isEmpty()) {
					if (result.get(0).getcontacts_last_name() != null
							&& !result.get(0).getcontacts_last_name().isEmpty()) {
					
							fullname = result.get(0).getcontacts_first_name()+ " "
									+ result.get(0).getcontacts_last_name();

						
					} else {
						 
							fullname = result.get(0).getcontacts_first_name();

						
					}
				} else {
					if (result.get(0).getcontacts_last_name() != null
							&& !result.get(0).getcontacts_last_name().isEmpty()) {

				
							fullname = result.get(0).getcontacts_last_name();
					
					} else {
						
						
							fullname = "";
						
					}
				}



			} else if (viewOrder.equals("Last_name_first")) {

				if (result.get(0).getcontacts_last_name() != null
						&& !result.get(0).getcontacts_last_name().isEmpty()) {
					if (result.get(0).getcontacts_first_name() != null
							&& !result.get(0).getcontacts_first_name().isEmpty()) {
						
							fullname = result.get(0)
									.getcontacts_last_name()
									+ ", "
									+ result.get(0).getcontacts_first_name();

						
					} else {
						
							fullname = result.get(0)
									.getcontacts_last_name() ;
						
					}
				} else {

					if (result.get(0).getcontacts_first_name() != null
							&& !result.get(0).getcontacts_first_name().isEmpty()) {

							fullname = result.get(0)
									.getcontacts_first_name();
					
					} else {
					
							fullname = "";
					
					}

				}

			}





			/*
			 * if (result.get(0).getcontacts_mobile_telephone_number() != null
			 * &&
			 * !result.get(0).getcontacts_mobile_telephone_number().isEmpty()) {
			 * phonemobile =
			 * result.get(0).getcontacts_mobile_telephone_number(); }
			 */

			if (result.get(0).getcontacts_email1_address() != null
					&& !result.get(0).getcontacts_email1_address().isEmpty()) {
				primemail = result.get(0).getcontacts_email1_address();
			}

			//fullname = firstname + " " + lastname;
		}

		popupcontactimage = (ImageView) dialog.findViewById(R.id.thumbImage);
		// byte[] imageBytes = result.get(0).getContacts_image();
		imageBytes = result.get(0).getContacts_image();

		if (imageBytes != null)
			popupcontactimage.setImageBitmap(ContactsUtilities
					.getImage(imageBytes));

		res = mContext.getResources();
		TabHost tabHost = (TabHost) dialog.findViewById(R.id.tabhost);
		tabHost.setup();
		TabHost.TabSpec spec;
		spec = tabHost
				.newTabSpec("status")
				.setIndicator(getTabIndicator(mContext, R.drawable.contacts_dailer_icon))
								.setContent(R.id.layoutTabPhone1);

		tabHost.addTab(spec);
		spec = tabHost
				.newTabSpec("actionitems")
				.setIndicator(getTabIndicator(mContext, R.drawable.contacts_email_icon))
								.setContent(R.id.layoutTabEmail2);
		tabHost.addTab(spec);

		spec = tabHost
				.newTabSpec("status")
				.setIndicator(getTabIndicator(mContext, R.drawable.contacts_map_icon))
								.setContent(R.id.layoutTabAddress3);

		tabHost.addTab(spec);

		spec = tabHost
				.newTabSpec("status")
				.setIndicator(getTabIndicator(mContext, R.drawable.contacts_browser_icon))
								.setContent(R.id.layoutTabWeb3);

		tabHost.addTab(spec);

		tabHost.setMinimumWidth(400);

		tabHost.setCurrentTab(0);

		phonetab = (LinearLayout) dialog.findViewById(R.id.layoutTabPhone1);
		// Emailid = (TextView) dialog.findViewById(R.id.Emailid);

		l_contact_phone_no = (LinearLayout) dialog
				.findViewById(R.id.l_contact_phone_no);

		l_contactphonenoTxt = (LinearLayout) dialog
				.findViewById(R.id.layoutTab1);
		l_contactphoneno2Txt = (LinearLayout) dialog
				.findViewById(R.id.layoutTab2);
		l_contactphoneno3Txt = (LinearLayout) dialog
				.findViewById(R.id.layoutTab3);
		l_contactphoneno4Txt = (LinearLayout) dialog
				.findViewById(R.id.layoutTab4);
		l_contactphoneno5Txt = (LinearLayout) dialog
				.findViewById(R.id.layoutTab5);
		l_contactphoneno6Txt = (LinearLayout) dialog
				.findViewById(R.id.layoutTab6);
		l_contactphoneno7Txt = (LinearLayout) dialog
				.findViewById(R.id.layoutTab7);
		l_contactphoneno8Txt = (LinearLayout) dialog
				.findViewById(R.id.layoutTab8);

		l_contactphoneno9Txt = (LinearLayout) dialog
				.findViewById(R.id.layoutTab9);
		l_contactphoneno10Txt = (LinearLayout) dialog
				.findViewById(R.id.layoutTab10);
		l_contactphoneno11Txt = (LinearLayout) dialog
				.findViewById(R.id.layoutTab11);
		l_contactphoneno12Txt = (LinearLayout) dialog
				.findViewById(R.id.layoutTab12);
		l_contactphoneno13Txt = (LinearLayout) dialog
				.findViewById(R.id.layoutTab13);
		l_contactphoneno14Txt = (LinearLayout) dialog
				.findViewById(R.id.layoutTab14);
		l_contactphoneno15Txt = (LinearLayout) dialog
				.findViewById(R.id.layoutTab15);

		contactphonenoTxt = (TextView) dialog
				.findViewById(R.id.contact_phone_no_value);
		contactphoneno2Txt = (TextView) dialog
				.findViewById(R.id.contact_phone_no_value2);
		contactphoneno3Txt = (TextView) dialog
				.findViewById(R.id.contact_phone_no_value3);
		contactphoneno4Txt = (TextView) dialog
				.findViewById(R.id.contact_phone_no_value4);
		contactphoneno5Txt = (TextView) dialog
				.findViewById(R.id.contact_phone_no_value5);
		contactphoneno6Txt = (TextView) dialog
				.findViewById(R.id.contact_phone_no_value6);
		contactphoneno7Txt = (TextView) dialog
				.findViewById(R.id.contact_phone_no_value7);
		contactphoneno8Txt = (TextView) dialog
				.findViewById(R.id.contact_phone_no_value8);
		contactphoneno9Txt = (TextView) dialog
				.findViewById(R.id.contact_phone_no_value9);
		contactphoneno10Txt = (TextView) dialog
				.findViewById(R.id.contact_phone_no_value10);
		contactphoneno11Txt = (TextView) dialog
				.findViewById(R.id.contact_phone_no_value11);
		contactphoneno12Txt = (TextView) dialog
				.findViewById(R.id.contact_phone_no_value12);
		contactphoneno13Txt = (TextView) dialog
				.findViewById(R.id.contact_phone_no_value13);
		contactphoneno14Txt = (TextView) dialog
				.findViewById(R.id.contact_phone_no_value14);
		contactphoneno15Txt = (TextView) dialog
				.findViewById(R.id.contact_phone_no_value15);

		contactphonenoTxt.setOnClickListener(this);
		phonetype = (TextView) dialog.findViewById(R.id.Phonetype);
		phonetype.setOnClickListener(this);

		contactphoneno2Txt.setOnClickListener(this);
		phonetype2 = (TextView) dialog.findViewById(R.id.Phonetype2);
		phonetype2.setOnClickListener(this);

		contactphoneno3Txt.setOnClickListener(this);
		phonetype3 = (TextView) dialog.findViewById(R.id.Phonetype3);
		phonetype3.setOnClickListener(this);

		contactphoneno4Txt.setOnClickListener(this);
		phonetype4 = (TextView) dialog.findViewById(R.id.Phonetype4);
		phonetype4.setOnClickListener(this);

		contactphoneno5Txt.setOnClickListener(this);
		phonetype5 = (TextView) dialog.findViewById(R.id.Phonetype5);
		phonetype5.setOnClickListener(this);

		contactphoneno6Txt.setOnClickListener(this);
		phonetype6 = (TextView) dialog.findViewById(R.id.Phonetype6);
		phonetype6.setOnClickListener(this);

		contactphoneno7Txt.setOnClickListener(this);
		phonetype7 = (TextView) dialog.findViewById(R.id.Phonetype7);
		phonetype7.setOnClickListener(this);

		contactphoneno8Txt.setOnClickListener(this);
		phonetype8 = (TextView) dialog.findViewById(R.id.Phonetype8);
		phonetype8.setOnClickListener(this);

		contactphoneno9Txt.setOnClickListener(this);
		phonetype9 = (TextView) dialog.findViewById(R.id.Phonetype9);
		phonetype9.setOnClickListener(this);

		contactphoneno10Txt.setOnClickListener(this);
		phonetype10 = (TextView) dialog.findViewById(R.id.Phonetype10);
		phonetype10.setOnClickListener(this);

		contactphoneno11Txt.setOnClickListener(this);
		phonetype11 = (TextView) dialog.findViewById(R.id.Phonetype11);
		phonetype11.setOnClickListener(this);

		contactphoneno12Txt.setOnClickListener(this);
		phonetype12 = (TextView) dialog.findViewById(R.id.Phonetype12);
		phonetype12.setOnClickListener(this);

		contactphoneno13Txt.setOnClickListener(this);
		phonetype13 = (TextView) dialog.findViewById(R.id.Phonetype10);
		phonetype13.setOnClickListener(this);

		contactphoneno14Txt.setOnClickListener(this);
		phonetype14 = (TextView) dialog.findViewById(R.id.Phonetype14);
		phonetype14.setOnClickListener(this);

		contactphoneno15Txt.setOnClickListener(this);
		phonetype15 = (TextView) dialog.findViewById(R.id.Phonetype15);
		phonetype15.setOnClickListener(this);

		textmessage = (ImageView) dialog.findViewById(R.id.textmessage);
		textmessage.setOnClickListener(this);

		textmessage2 = (ImageView) dialog.findViewById(R.id.textmessage2);
		textmessage2.setOnClickListener(this);
		textmessage3 = (ImageView) dialog.findViewById(R.id.textmessage3);
		textmessage3.setOnClickListener(this);

		textmessage4 = (ImageView) dialog.findViewById(R.id.textmessage4);
		textmessage4.setOnClickListener(this);

		textmessage5 = (ImageView) dialog.findViewById(R.id.textmessage5);
		textmessage5.setOnClickListener(this);

		textmessage6 = (ImageView) dialog.findViewById(R.id.textmessage6);
		textmessage6.setOnClickListener(this);

		textmessage7 = (ImageView) dialog.findViewById(R.id.textmessage7);
		textmessage7.setOnClickListener(this);

		textmessage8 = (ImageView) dialog.findViewById(R.id.textmessage8);
		textmessage8.setOnClickListener(this);

		textmessage9 = (ImageView) dialog.findViewById(R.id.textmessage9);
		textmessage9.setOnClickListener(this);

		textmessage10 = (ImageView) dialog.findViewById(R.id.textmessage10);
		textmessage10.setOnClickListener(this);

		textmessage11 = (ImageView) dialog.findViewById(R.id.textmessage11);
		textmessage11.setOnClickListener(this);

		textmessage12 = (ImageView) dialog.findViewById(R.id.textmessage12);
		textmessage12.setOnClickListener(this);
		textmessage13 = (ImageView) dialog.findViewById(R.id.textmessage13);
		textmessage13.setOnClickListener(this);

		textmessage14 = (ImageView) dialog.findViewById(R.id.textmessage14);
		textmessage14.setOnClickListener(this);
		textmessage15 = (ImageView) dialog.findViewById(R.id.textmessage15);
		textmessage15.setOnClickListener(this);

		l_contactemail = (LinearLayout) dialog
				.findViewById(R.id.l_contact_email);
		l_contactemailTxt = (LinearLayout) dialog
				.findViewById(R.id.layoutemailTab1);
		l_contactemail2Txt = (LinearLayout) dialog
				.findViewById(R.id.layoutemailTab2);
		l_contactemail3Txt = (LinearLayout) dialog
				.findViewById(R.id.layoutemailTab3);
		l_contactemail4Txt = (LinearLayout) dialog
				.findViewById(R.id.layoutemailTab4);
		l_contactemail5Txt = (LinearLayout) dialog
				.findViewById(R.id.layoutemailTab5);
		l_contactemail6Txt = (LinearLayout) dialog
				.findViewById(R.id.layoutemailTab6);

		contactemailTxt = (TextView) dialog
				.findViewById(R.id.contact_email_value);
		contactemailTxt.setOnClickListener(this);
		emailtype = (TextView) dialog.findViewById(R.id.emailtype);
		emailtype.setOnClickListener(this);

		contactemail2Txt = (TextView) dialog
				.findViewById(R.id.contact_email_value2);
		contactemail2Txt.setOnClickListener(this);
		emailtype2 = (TextView) dialog.findViewById(R.id.emailtype2);
		emailtype2.setOnClickListener(this);

		contactemail3Txt = (TextView) dialog
				.findViewById(R.id.contact_email_value3);
		contactemail3Txt.setOnClickListener(this);
		emailtype3 = (TextView) dialog.findViewById(R.id.emailtype3);
		emailtype3.setOnClickListener(this);

		contactemail4Txt = (TextView) dialog
				.findViewById(R.id.contact_email_value4);
		contactemail4Txt.setOnClickListener(this);
		emailtype4 = (TextView) dialog.findViewById(R.id.emailtype4);
		emailtype4.setOnClickListener(this);

		contactemail5Txt = (TextView) dialog
				.findViewById(R.id.contact_email_value5);
		contactemail5Txt.setOnClickListener(this);
		emailtype5 = (TextView) dialog.findViewById(R.id.emailtype5);
		emailtype5.setOnClickListener(this);

		contactemail6Txt = (TextView) dialog
				.findViewById(R.id.contact_email_value6);
		contactemail6Txt.setOnClickListener(this);
		emailtype6 = (TextView) dialog.findViewById(R.id.emailtype6);
		emailtype6.setOnClickListener(this);

		l_contactaddrsTxt1 = (LinearLayout) dialog.findViewById(R.id.l_contact_address);
		contactaddrsTxt1 = (TextView) dialog.findViewById(R.id.contact_address_value1);
		contactaddrsTxt1.setOnClickListener(this);
		addresstype1 = (TextView) dialog.findViewById(R.id.addresstype1);
		addresstype1.setOnClickListener(this);
		
		
		contactaddrsTxt2 = (TextView) dialog.findViewById(R.id.contact_address_value2);
		contactaddrsTxt2.setOnClickListener(this);
		addresstype2 = (TextView) dialog.findViewById(R.id.addresstype2);
		addresstype2.setOnClickListener(this);
		
		contactaddrsTxt3 = (TextView) dialog.findViewById(R.id.contact_address_value3);
		contactaddrsTxt3.setOnClickListener(this);
		addresstype3 = (TextView) dialog.findViewById(R.id.addresstype3);
		addresstype3.setOnClickListener(this);
		
		
		l_contactaddr = (LinearLayout) dialog.findViewById(R.id.l_contact_address);
		l_contactaddrTxt = (LinearLayout) dialog.findViewById(R.id.layoutaddrTab1);
		l_contactaddr2Txt = (LinearLayout) dialog.findViewById(R.id.layoutaddrTab2);
		l_contactaddr3Txt = (LinearLayout) dialog.findViewById(R.id.layoutaddrTab3);

		l_contactWebsiteTxt = (LinearLayout) dialog
				.findViewById(R.id.l_website);
		contactWebsiteTxt = (TextView) dialog.findViewById(R.id.website_value);
		contactWebsiteTxt.setOnClickListener(this);

		emailtab = (LinearLayout) dialog.findViewById(R.id.layoutTabEmail2);
		// Emailtype = (TextView) dialog.findViewById(R.id.Emailtype);
		// Emailtype.setOnClickListener(this);

		txtfullname = (TextView) dialog.findViewById(R.id.fullname);
		txtfullname.setText(fullname);
		// phonetext = (TextView) dialog.findViewById(R.id.Phonecall);

		/*
		 * if (phonemobile != null & !phonemobile.isEmpty()) {
		 * 
		 * tabHost.setVisibility(View.VISIBLE);
		 * tabHost.getTabWidget().getChildAt(0).setVisibility(View.VISIBLE); //
		 * phonetab.setVisibility(View.VISIBLE); phonetext.setText(phonemobile);
		 * phonetext.setOnClickListener(this);
		 * 
		 * } else { // tabHost.setVisibility(View.GONE);
		 * phonetab.setVisibility(View.GONE);
		 * tabHost.getTabWidget().getChildAt(0).setVisibility(View.GONE);
		 * 
		 * }
		 */

		if (result.get(0).getcontacts_mobile_telephone_number() != null
				&& !result.get(0).getcontacts_mobile_telephone_number()
				.isEmpty()) {

			tabHost.setVisibility(View.VISIBLE);
			tabHost.getTabWidget().getChildAt(0).setVisibility(View.VISIBLE);

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

			tabHost.setVisibility(View.VISIBLE);
			tabHost.getTabWidget().getChildAt(0).setVisibility(View.VISIBLE);

			contactphoneno2Txt.setText(result.get(0)
					.getcontacts_business_telephone_number());

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

			tabHost.setVisibility(View.VISIBLE);
			tabHost.getTabWidget().getChildAt(0).setVisibility(View.VISIBLE);

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

			tabHost.setVisibility(View.VISIBLE);
			tabHost.getTabWidget().getChildAt(0).setVisibility(View.VISIBLE);

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

			tabHost.setVisibility(View.VISIBLE);
			tabHost.getTabWidget().getChildAt(0).setVisibility(View.VISIBLE);

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

			tabHost.setVisibility(View.VISIBLE);
			tabHost.getTabWidget().getChildAt(0).setVisibility(View.VISIBLE);

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

			tabHost.setVisibility(View.VISIBLE);
			tabHost.getTabWidget().getChildAt(0).setVisibility(View.VISIBLE);

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

			tabHost.setVisibility(View.VISIBLE);
			tabHost.getTabWidget().getChildAt(0).setVisibility(View.VISIBLE);

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

			tabHost.setVisibility(View.VISIBLE);
			tabHost.getTabWidget().getChildAt(0).setVisibility(View.VISIBLE);

			contactphoneno9Txt.setText(result.get(0)
					.getcontacts_home_telephone_number());

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

			tabHost.setVisibility(View.VISIBLE);
			tabHost.getTabWidget().getChildAt(0).setVisibility(View.VISIBLE);

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

			tabHost.setVisibility(View.VISIBLE);
			tabHost.getTabWidget().getChildAt(0).setVisibility(View.VISIBLE);

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

			tabHost.setVisibility(View.VISIBLE);
			tabHost.getTabWidget().getChildAt(0).setVisibility(View.VISIBLE);

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

			tabHost.setVisibility(View.VISIBLE);
			tabHost.getTabWidget().getChildAt(0).setVisibility(View.VISIBLE);

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

			tabHost.setVisibility(View.VISIBLE);
			tabHost.getTabWidget().getChildAt(0).setVisibility(View.VISIBLE);

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

			tabHost.setVisibility(View.VISIBLE);
			tabHost.getTabWidget().getChildAt(0).setVisibility(View.VISIBLE);

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
			phonetab.setVisibility(View.GONE);
			tabHost.getTabWidget().getChildAt(0).setVisibility(View.GONE);
			Log.e("Contacts Detail page", "Phone number available check 2");
		}

		if (result.get(0).getcontacts_email1_address() != null
				&& !result.get(0).getcontacts_email1_address().isEmpty()) {
			contactemailTxt.setText(result.get(0).getcontacts_email1_address());
			emailtype.setText(result.get(0).getContact_email1_type());
			primemail = result.get(0).getcontacts_email1_address();
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
			tabHost.setVisibility(View.VISIBLE);
			tabHost.getTabWidget().getChildAt(1).setVisibility(View.VISIBLE);
		} else {
			l_contactemail.setVisibility(View.GONE);
			tabHost.getTabWidget().getChildAt(1).setVisibility(View.GONE);
		}
		
		//-----------------------
		
		
		//Populate Business Location
				if (result.get(0).getContacts_business_location() != null
						&& !result.get(0).getContacts_business_location().isEmpty()) {
					try {
						String val[] = (result.get(0).getContacts_business_location()).split("\n");
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
					contactaddrsTxt1.setText(result.get(0).getContacts_business_location());
					addresstype1.setText(result.get(0).getContact_business_location_type());
					l_contactaddr.setVisibility(View.VISIBLE);
					l_contactaddrTxt.setVisibility(View.VISIBLE);
					
					tabHost.setVisibility(View.VISIBLE);
					tabHost.getTabWidget().getChildAt(2).setVisibility(View.VISIBLE);
				}

				else {
					contactaddrsTxt1.setText("");
					addresstype1.setText("");
					l_contactaddrTxt.setVisibility(View.GONE);
				}
				
				//Populate Home Location
					if (result.get(0).getContacts_home_location() != null
							&& !result.get(0).getContacts_home_location().isEmpty()) {
						try {
							String val[] = (result.get(0).getContacts_home_location()).split("\n");
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
						
						tabHost.setVisibility(View.VISIBLE);
						tabHost.getTabWidget().getChildAt(2).setVisibility(View.VISIBLE);
					}
			
					else {
						contactaddrsTxt2.setText("");
						addresstype2.setText("");
						l_contactaddr2Txt.setVisibility(View.GONE);
					}
					
					
					//Populate Home Location
					if (result.get(0).getContacts_other_location() != null
							&& !result.get(0).getContacts_other_location().isEmpty()) {
						try {
							String val[] = (result.get(0).getContacts_other_location()).split("\n");
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
						contactaddrsTxt3.setText(result.get(0).getContacts_other_location());
						addresstype3.setText(result.get(0).getContact_other_location_type());
						l_contactaddr.setVisibility(View.VISIBLE);
						l_contactaddr3Txt.setVisibility(View.VISIBLE);
						
						tabHost.setVisibility(View.VISIBLE);
						tabHost.getTabWidget().getChildAt(2).setVisibility(View.VISIBLE);
					}
			
					else {
						contactaddrsTxt3.setText("");
						addresstype3.setText("");
						l_contactaddr3Txt.setVisibility(View.GONE);
					}
					
					if (l_contactaddrTxt.getVisibility() == View.GONE &&
							l_contactaddr2Txt.getVisibility() == View.GONE &&
							l_contactaddr3Txt.getVisibility() == View.GONE) {
						
						//l_contactaddrsTxt.setVisibility(View.GONE);					
						
						l_contactaddr.setVisibility(View.GONE);					
						tabHost.getTabWidget().getChildAt(2).setVisibility(View.GONE);
					}
		
		
				
		
		//-----------------------------------------------------

	/*	if (result.get(0).getContacts_business_location() != null
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
			contactaddrsTxt.setText(result.get(0).getContacts_business_location());
			addresstype.setText(result.get(0).getContact_business_location_type());
			l_contactaddrsTxt.setVisibility(View.VISIBLE);
			tabHost.setVisibility(View.VISIBLE);
			tabHost.getTabWidget().getChildAt(2).setVisibility(View.VISIBLE);
		}

		else {
			contactaddrsTxt.setText("");
			addresstype.setText("");
			l_contactaddrsTxt.setVisibility(View.GONE);
			tabHost.getTabWidget().getChildAt(2).setVisibility(View.GONE);
		}*/

		if (result.get(0).getContact_website() != null
				&& !result.get(0).getContact_website().isEmpty()) {
			contactWebsiteTxt.setText(result.get(0).getContact_website());
			l_contactWebsiteTxt.setVisibility(View.VISIBLE);
			tabHost.getTabWidget().getChildAt(3).setVisibility(View.VISIBLE);
		} else {
			contactWebsiteTxt.setText("");
			l_contactWebsiteTxt.setVisibility(View.GONE);
			tabHost.getTabWidget().getChildAt(3).setVisibility(View.GONE);
		}

		if ((tabHost.getTabWidget().getChildAt(0).getVisibility() == View.GONE)
				&& (tabHost.getTabWidget().getChildAt(1).getVisibility() == View.GONE)
				&& (tabHost.getTabWidget().getChildAt(2).getVisibility() == View.GONE)
				&& (tabHost.getTabWidget().getChildAt(3).getVisibility() == View.GONE)) {
			tabHost.setVisibility(View.GONE);

			/*
			 * if (this.getWindow().getWindowManager().getDefaultDisplay()
			 * .getOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
			 * // portrait mode dialog.getWindow().setLayout(250, 323); }
			 */

		} else {
			/*
			 * if (this.getWindow().getWindowManager().getDefaultDisplay()
			 * .getOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
			 * // portrait mode dialog.getWindow().setLayout(600, 323); }
			 */
		}

		if ((tabHost.getTabWidget().getChildAt(0).getVisibility() == View.VISIBLE)) {
			tabHost.setCurrentTab(0);
		}

		else if ((tabHost.getTabWidget().getChildAt(1).getVisibility() == View.VISIBLE)) {
			tabHost.setCurrentTab(1);
		} else if ((tabHost.getTabWidget().getChildAt(2).getVisibility() == View.VISIBLE)) {
			tabHost.setCurrentTab(2);
		} else if ((tabHost.getTabWidget().getChildAt(3).getVisibility() == View.VISIBLE)) {
			tabHost.setCurrentTab(3);
		}

		/*
		 * if (myImageView.getVisibility() == View.VISIBLE) { // Its visible }
		 * else { // Either gone or invisible }
		 */

		/*
		 * if (primemail != null & !primemail.isEmpty()) {
		 * 
		 * tabHost.setVisibility(View.VISIBLE);
		 * tabHost.getTabWidget().getChildAt(1).setVisibility(View.VISIBLE);
		 * 
		 * // emailtab.setVisibility(View.VISIBLE); Emailid.setText(primemail);
		 * Emailid.setOnClickListener(this);
		 * 
		 * } else {
		 * 
		 * // tabHost.setVisibility(View.GONE); //
		 * emailtab.setVisibility(View.GONE);
		 * tabHost.getTabWidget().getChildAt(1).setVisibility(View.GONE);
		 * 
		 * }
		 */

		/*
		 * phonetype = (TextView) dialog.findViewById(R.id.Phonetype);
		 * phonetype.setOnClickListener(this); textmessage = (ImageView)
		 * dialog.findViewById(R.id.textmessage);
		 * textmessage.setOnClickListener(this);
		 */

		/*
		 * Emailtype = (TextView) dialog.findViewById(R.id.Emailtype);
		 * Emailtype.setOnClickListener(this);
		 */

		dialog.show();

	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub

		switch (view.getId()) {

		/*
		 * case R.id.Phonecall: phonecall(); break; case R.id.Phonetype:
		 * phonecall(); break; case R.id.textmessage:
		 * 
		 * phonemobile =phonetext.getText().toString(); fullname =
		 * txtfullname.getText().toString(); sendsms(); break;
		 */

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
			fullname = txtfullname.getText().toString();
			sendsms();
			break;
		case R.id.textmessage2:
			phonemobile = contactphoneno2Txt.getText().toString();
			fullname = txtfullname.getText().toString();
			sendsms();
			break;
		case R.id.textmessage3:
			phonemobile = contactphoneno3Txt.getText().toString();
			fullname = txtfullname.getText().toString();
			sendsms();
			break;
		case R.id.textmessage4:
			phonemobile = contactphoneno4Txt.getText().toString();
			fullname = txtfullname.getText().toString();
			sendsms();
			break;
		case R.id.textmessage5:
			phonemobile = contactphoneno5Txt.getText().toString();
			fullname = txtfullname.getText().toString();
			sendsms();

			break;
		case R.id.textmessage6:
			phonemobile = contactphoneno6Txt.getText().toString();
			fullname = txtfullname.getText().toString();
			sendsms();
			break;
		case R.id.textmessage7:
			phonemobile = contactphoneno7Txt.getText().toString();
			fullname = txtfullname.getText().toString();
			sendsms();
			break;
		case R.id.textmessage8:
			phonemobile = contactphoneno8Txt.getText().toString();
			fullname = txtfullname.getText().toString();
			sendsms();
			break;
		case R.id.textmessage9:
			phonemobile = contactphoneno9Txt.getText().toString();
			fullname = txtfullname.getText().toString();
			sendsms();
			break;
		case R.id.textmessage10:
			phonemobile = contactphoneno10Txt.getText().toString();
			fullname = txtfullname.getText().toString();
			sendsms();
			break;
		case R.id.textmessage11:
			phonemobile = contactphoneno11Txt.getText().toString();
			fullname = txtfullname.getText().toString();
			sendsms();
			break;
		case R.id.textmessage12:
			phonemobile = contactphoneno12Txt.getText().toString();
			fullname = txtfullname.getText().toString();
			sendsms();
			break;
		case R.id.textmessage13:
			phonemobile = contactphoneno13Txt.getText().toString();
			fullname = txtfullname.getText().toString();
			sendsms();
			break;
		case R.id.textmessage14:
			phonemobile = contactphoneno14Txt.getText().toString();
			fullname = txtfullname.getText().toString();
			sendsms();
			break;
		case R.id.textmessage15:
			phonemobile = contactphoneno15Txt.getText().toString();
			fullname = txtfullname.getText().toString();
			sendsms();
			break;

		case R.id.contact_email_value:

			primemail = contactemailTxt.getText().toString();

			if (isEmailValid(primemail))
				sendmail();
			else
				Toast.makeText(mContext, "Email-ID is not valid",
						Toast.LENGTH_LONG).show();
			break;
		case R.id.contact_email_value2:
			primemail = contactemail2Txt.getText().toString();
			if (isEmailValid(primemail))
				sendmail();
			else
				Toast.makeText(mContext, "Email-ID is not valid",
						Toast.LENGTH_LONG).show();
			break;
		case R.id.contact_email_value3:
			primemail = contactemail3Txt.getText().toString();
			if (isEmailValid(primemail))
				sendmail();
			else
				Toast.makeText(mContext, "Email-ID is not valid",
						Toast.LENGTH_LONG).show();
			break;
		case R.id.contact_email_value4:
			primemail = contactemail4Txt.getText().toString();
			if (isEmailValid(primemail))
				sendmail();
			else
				Toast.makeText(mContext, "Email-ID is not valid",
						Toast.LENGTH_LONG).show();
			break;
		case R.id.contact_email_value5:
			primemail = contactemail5Txt.getText().toString();
			if (isEmailValid(primemail))
				sendmail();
			else
				Toast.makeText(mContext, "Email-ID is not valid",
						Toast.LENGTH_LONG).show();
			break;
		case R.id.contact_email_value6:
			primemail = contactemail6Txt.getText().toString();
			if (isEmailValid(primemail))
				sendmail();
			else
				Toast.makeText(mContext, "Email-ID is not valid",
						Toast.LENGTH_LONG).show();
			break;

		case R.id.contact_address_value1:

			try {

				Log.e("Addressmap after formed", addrsMap);

				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(String
						.format("geo:0,0?q=%s", URLEncoder.encode(addrsMap))));

				mContext.startActivity(i);
			} catch (ActivityNotFoundException activityNotFound) {

				Toast.makeText(mContext,
						"Please install Google-Maps App, to view the map",
						Toast.LENGTH_LONG).show();

			}

			break;

		case R.id.addresstype1:
			try {

				Log.e("Addressmap after formed", addrsMap);

				Intent i2 = new Intent(Intent.ACTION_VIEW, Uri.parse(String
						.format("geo:0,0?q=%s", URLEncoder.encode(addrsMap))));

				mContext.startActivity(i2);
			} catch (ActivityNotFoundException activityNotFound) {

				Toast.makeText(mContext,
						"Please install Google-Maps App, to view the map",
						Toast.LENGTH_LONG).show();

			}
			break;
			
			
		case R.id.contact_address_value2:

			try {

				Log.e("Addressmap after formed", addrsMap2);

				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(String
						.format("geo:0,0?q=%s", URLEncoder.encode(addrsMap2))));

				mContext.startActivity(i);
			} catch (ActivityNotFoundException activityNotFound) {

				Toast.makeText(mContext,
						"Please install Google-Maps App, to view the map",
						Toast.LENGTH_LONG).show();

			}

			break;

		case R.id.addresstype2:
			try {

				Log.e("Addressmap after formed", addrsMap2);

				Intent i2 = new Intent(Intent.ACTION_VIEW, Uri.parse(String
						.format("geo:0,0?q=%s", URLEncoder.encode(addrsMap2))));

				mContext.startActivity(i2);
			} catch (ActivityNotFoundException activityNotFound) {

				Toast.makeText(mContext,
						"Please install Google-Maps App, to view the map",
						Toast.LENGTH_LONG).show();

			}
			break;
			
		case R.id.contact_address_value3:

			try {

				Log.e("Addressmap after formed", addrsMap3);

				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(String
						.format("geo:0,0?q=%s", URLEncoder.encode(addrsMap3))));

				mContext.startActivity(i);
			} catch (ActivityNotFoundException activityNotFound) {

				Toast.makeText(mContext,
						"Please install Google-Maps App, to view the map",
						Toast.LENGTH_LONG).show();

			}

			break;

		case R.id.addresstype3:
			try {

				Log.e("Addressmap after formed", addrsMap3);

				Intent i2 = new Intent(Intent.ACTION_VIEW, Uri.parse(String
						.format("geo:0,0?q=%s", URLEncoder.encode(addrsMap3))));

				mContext.startActivity(i2);
			} catch (ActivityNotFoundException activityNotFound) {

				Toast.makeText(mContext,
						"Please install Google-Maps App, to view the map",
						Toast.LENGTH_LONG).show();

			}
			break;

		case R.id.website_value:

			SecuredBrowserUrl = contactWebsiteTxt.getText().toString();
			CallSecuredBrowser();

		default:
			break;
		}

	}

	private void phonecall() {



		TelephonyManager tm = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);

		if(tm.getPhoneType()==TelephonyManager.PHONE_TYPE_NONE)
		{
			if (tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT) {
				 //coming here if Tablet WITHOUT SIM SLOT 
				Toast.makeText(mContext, "Not registered on network",
						Toast.LENGTH_SHORT).show();
			}else{
				 //coming here if Tablet WITH SIM SLOT 
				/*Toast.makeText(mContext, "TAB",
						Toast.LENGTH_SHORT).show();*/
				if (tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT) {

					isAirplaneEnabled = Settings.System.getInt(mContext
							.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1;
					if (isAirplaneEnabled) {
						Toast.makeText(mContext,
								"Flight mode on. Turn Flight mode off to make calls",
								Toast.LENGTH_SHORT).show();
					} else {
						DialerUtilities.phoneCallIntent(phonemobile,
								Email.getAppContext());
					}

				} else {

					Toast.makeText(mContext, "Not registered on network",
							Toast.LENGTH_SHORT).show();
				}
			}
		 
		}
		else{
			/*Toast.makeText(mContext, "phone",
					Toast.LENGTH_SHORT).show();*/
			//coming here if phone
			if (tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT) {

				isAirplaneEnabled = Settings.System.getInt(mContext
						.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1;
				if (isAirplaneEnabled) {
					Toast.makeText(mContext,
							"Flight mode on. Turn Flight mode off to make calls",
							Toast.LENGTH_SHORT).show();
				} else {
					DialerUtilities.phoneCallIntent(phonemobile,
							Email.getAppContext());
				}

			} else {

				Toast.makeText(mContext, "Not registered on network",
						Toast.LENGTH_SHORT).show();
			}
		}
		
		

		
	}

	private void sendsms() {

		/*
		TelephonyManager tm = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);

		if (tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT) {

			isAirplaneEnabled = Settings.System.getInt(
					mContext.getContentResolver(),
					Settings.System.AIRPLANE_MODE_ON, 0) == 1;
			if (isAirplaneEnabled) {
				Toast.makeText(mContext,
						"Flight mode on. Turn Flight mode off to make calls",
						Toast.LENGTH_SHORT).show();
			} else {
				// Toast.makeText(this, "phone has SIM card",
				// Toast.LENGTH_SHORT).show();

				try {

					Intent callSMS = new Intent(mContext, SmsMain.class);
					callSMS.putExtra("con_name", fullname);
					callSMS.putExtra("con_phone", phonemobile);

					if (imageBytes != null) {
						callSMS.putExtra("con_image", imageBytes);
					} else {
						// callSMS.putExtra("con_image","null");
						callSMS.putExtra("con_image", imageBytes);
						// Need to pass null, once messenger team conforms, what
						// format they need null
					}
					mContext.startActivity(callSMS);

				} catch (ActivityNotFoundException activityException) {
					Log.e("sendsms activity not found", "Call failed");
				}

			}

		} else {

			Toast.makeText(mContext, "Not registered on network",
					Toast.LENGTH_SHORT).show();
		}*/
		
		
		

		
		TelephonyManager tm = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);

		if(tm.getPhoneType()==TelephonyManager.PHONE_TYPE_NONE)
		{
			if (tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT) {
				 //coming here if Tablet WITHOUT SIM SLOT 
				Toast.makeText(mContext, "Not registered on network",
						Toast.LENGTH_SHORT).show();
			}else{
				 //coming here if Tablet WITH SIM SLOT 
				/*Toast.makeText(mContext, "TAB",
						Toast.LENGTH_SHORT).show();*/
				if (tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT) {

					isAirplaneEnabled = Settings.System.getInt(mContext
							.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1;
					if (isAirplaneEnabled) {
						Toast.makeText(mContext,
								"Flight mode on. Turn Flight mode off to make calls",
								Toast.LENGTH_SHORT).show();
					} else {
						try {
							Intent callSMS = new Intent(mContext,
									SmsMain.class);
							callSMS.putExtra("con_name", fullname);
							callSMS.putExtra("con_phone", phonemobile);

							if (imageBytes != null) {
								callSMS.putExtra("con_image", imageBytes);
							} else {
								// callSMS.putExtra("con_image","null");
								callSMS.putExtra("con_image", imageBytes);
								// Need to pass null, once messenger team conforms, what
								// format they need null

							}
							mContext.startActivity(callSMS);

						} catch (ActivityNotFoundException activityException) {
							Log.e("sendsms activity not found", "Call failed");
						}
					}

				} else {

					Toast.makeText(mContext, "Not registered on network",
							Toast.LENGTH_SHORT).show();
				}
			}
		 
		}
		else{
			/*Toast.makeText(mContext, "phone",
					Toast.LENGTH_SHORT).show();*/
			//coming here if phone
			if (tm.getSimState() != TelephonyManager.SIM_STATE_ABSENT) {

				isAirplaneEnabled = Settings.System.getInt(mContext
						.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) == 1;
				if (isAirplaneEnabled) {
					Toast.makeText(mContext,
							"Flight mode on. Turn Flight mode off to make calls",
							Toast.LENGTH_SHORT).show();
				} else {
					try {
						Intent callSMS = new Intent(mContext,
								SmsMain.class);
						callSMS.putExtra("con_name", fullname);
						callSMS.putExtra("con_phone", phonemobile);

						if (imageBytes != null) {
							callSMS.putExtra("con_image", imageBytes);
						} else {
							// callSMS.putExtra("con_image","null");
							callSMS.putExtra("con_image", imageBytes);
							// Need to pass null, once messenger team conforms, what
							// format they need null

						}
						mContext.startActivity(callSMS);

					} catch (ActivityNotFoundException activityException) {
						Log.e("sendsms activity not found", "Call failed");
					}
				}

			} else {

				Toast.makeText(mContext, "Not registered on network",
						Toast.LENGTH_SHORT).show();
			}
		}


	}

	public static boolean isEmailValid(String email) {
		
		
		return true;

	/*	boolean isValid = false;

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
				mContext.startActivity(openWebAppIntent);
				// result = true;
			} catch (ActivityNotFoundException ex) {
				// No applications can handle it. Ignore.
			}
		}

	}

	private void sendmail() {

		try {
			Intent targetedShareIntent = new Intent(mContext,
					EmMessageCompose.class);
			targetedShareIntent.putExtra("ContactsAppHandshake",
					"ContactsAppHandshake");
			// targetedShareIntent.putExtra(Intent.EXTRA_EMAIL," ");
			targetedShareIntent.putExtra(Intent.EXTRA_SUBJECT, "");
			targetedShareIntent.putExtra(Intent.EXTRA_TEXT, "");
			targetedShareIntent.setData(Uri.parse("mailto:" + primemail));

			targetedShareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(targetedShareIntent);

		} catch (ActivityNotFoundException activityException) {
			Log.e("Secured apps not available to send Email", "Call failed");
			Toast.makeText(mContext,
					"Secured apps not available to send Email",
					Toast.LENGTH_LONG).show();
		}

	}
	
	 private View getTabIndicator(Context context, int drawable) {
	        View view = LayoutInflater.from(context).inflate(R.layout.contacts_tab_layout, null);
	        TextView tv = (TextView) view.findViewById(R.id.textView);
	        tv.setBackgroundResource(drawable);
	        return view;
	    }

	/*@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
	    Rect dialogBounds = new Rect();
	    getWindow().getDecorView().getHitRect(dialogBounds);

	    if (dialogBounds.contains((int) ev.getX(), (int) ev.getY())) {
	        Log.d("================= test ===========", "inside");
	    } else {
	        Log.d("================= test ===========", "outside");
	    }
	    return super.dispatchTouchEvent(ev);
	}*/
}
