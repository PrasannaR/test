package com.cognizant.trumobi.contacts.activity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;

import org.apache.commons.io.output.ByteArrayOutputStream;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.Toast;

import com.TruBoxSDK.SharedPreferences;
import com.actionbarsherlock.internal.widget.IcsAdapterView;
import com.actionbarsherlock.internal.widget.IcsAdapterView.OnItemSelectedListener;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.cognizant.trubox.contacts.db.ContactsConsts;
import com.cognizant.trubox.contacts.db.ContactsModel;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseSherlockActivity;
import com.cognizant.trumobi.contacts.ContactsParentActivity;
import com.cognizant.trumobi.contacts.utils.ContactsUtilities;
import com.cognizant.trumobi.em.EmEmController;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.em.provider.EmEmailContent;
import com.cognizant.trumobi.em.provider.EmEmailContent.Mailbox;
import com.cognizant.trumobi.log.ContactsLog;

public class ContactsAddContact extends TruMobiBaseSherlockActivity implements
		OnItemSelectedListener {

	Spinner AddPhotospinner;

	// PopupMenu popup;
	private PopupWindow pwindo;
	PopupWindow pwindow;
	Button btn;
	ArrayList<String> newArray;
	ArrayAdapter<String> arrayAdapter;
	FrameLayout contact_image_frame;

	ArrayList<String> newArray_image;
	ArrayAdapter<String> arrayAdapter_image;

	ImageView AddImage;

	public static int i;

	public static int phonetici;

	ImageButton imageButton;

	EditText namepfx;

	EditText AddPhone;
	ImageButton AddPhoneremove;
	Button AddNewPhone;
	Button AddNewEmail;
	Button AddNewIM;
	Button AddNewAddress;

	LinearLayout AddPhonelinear2;
	LinearLayout AddPhonelinear1;
	EditText AddPhone2;
	ImageButton AddPhoneremove2;

	LinearLayout AddPhonelinear3;
	EditText AddPhone3;
	ImageButton AddPhoneremove3;
	Spinner phoneSpinner3;
	
	LinearLayout AddPhonelinear4;
	EditText AddPhone4;
	ImageButton AddPhoneremove4;
	Spinner phoneSpinner4;
	
	LinearLayout AddPhonelinear5;
	EditText AddPhone5;
	ImageButton AddPhoneremove5;
	Spinner phoneSpinner5;
	
	LinearLayout AddPhonelinear6;
	EditText AddPhone6;
	ImageButton AddPhoneremove6;
	Spinner phoneSpinner6;
	
	LinearLayout AddPhonelinear7;
	EditText AddPhone7;
	ImageButton AddPhoneremove7;
	Spinner phoneSpinner7;
	
	LinearLayout AddPhonelinear8;
	EditText AddPhone8;
	ImageButton AddPhoneremove8;
	Spinner phoneSpinner8;
	
	LinearLayout AddPhonelinear9;
	EditText AddPhone9;
	ImageButton AddPhoneremove9;
	Spinner phoneSpinner9;
	
	LinearLayout AddPhonelinear10;
	EditText AddPhone10;
	ImageButton AddPhoneremove10;
	Spinner phoneSpinner10;
	
	LinearLayout AddPhonelinear11;
	EditText AddPhone11;
	ImageButton AddPhoneremove11;
	Spinner phoneSpinner11;
	
	LinearLayout AddPhonelinear12;
	EditText AddPhone12;
	ImageButton AddPhoneremove12;
	Spinner phoneSpinner12;
	
	LinearLayout AddPhonelinear13;
	EditText AddPhone13;
	ImageButton AddPhoneremove13;
	Spinner phoneSpinner13;
	
	LinearLayout AddPhonelinear14;
	EditText AddPhone14;
	ImageButton AddPhoneremove14;
	Spinner phoneSpinner14;
	
	LinearLayout AddPhonelinear15;
	EditText AddPhone15;
	ImageButton AddPhoneremove15;
	Spinner phoneSpinner15;
	
	LinearLayout AddIMlinear;
	EditText AddIM;
	ImageButton AddIMremove;
	Spinner IMSpinner;
	
	LinearLayout AddIMlinear2;
	EditText AddIM2;
	ImageButton AddIMremove2;
	Spinner IMSpinner2;
	
	LinearLayout AddIMlinear3;
	EditText AddIM3;
	ImageButton AddIMremove3;
	Spinner IMSpinner3;
	
	LinearLayout AddIMlinear4;
	EditText AddIM4;
	ImageButton AddIMremove4;
	Spinner IMSpinner4;
	
	LinearLayout AddIMlinear5;
	EditText AddIM5;
	ImageButton AddIMremove5;
	Spinner IMSpinner5;
	
	LinearLayout AddIMlinear6;
	EditText AddIM6;
	ImageButton AddIMremove6;
	Spinner IMSpinner6;
	
	
	EditText middleName;
	EditText lastName;

	EditText nameSuffix;

	EditText AddOrganztn;
	EditText AddTitle;
	EditText AddName;

	// EditText AddPhoneticName;

	ImageButton PhoneticNameImageButton;

	EditText AddPhoneticName;
	EditText AddNotes;
	EditText AddNICKNAME;
	EditText AddWEBSITE;
	EditText AddINTERNETCALL;

	EditText AddPhoneticMiddleName;
	EditText AddPhoneticLastName;
	
	
	LinearLayout AddEmaillinear;
	EditText AddEmail;
	ImageButton AddEmailremove;
	Spinner emailSpinner;
	
	LinearLayout AddEmaillinear2;
	EditText AddEmail2;
	ImageButton AddEmailremove2;
	Spinner emailSpinner2;
	
	LinearLayout AddEmaillinear3;
	EditText AddEmail3;
	ImageButton AddEmailremove3;
	Spinner emailSpinner3;
	
	LinearLayout AddEmaillinear4;
	EditText AddEmail4;
	ImageButton AddEmailremove4;
	Spinner emailSpinner4;
	
	LinearLayout AddEmaillinear5;
	EditText AddEmail5;
	ImageButton AddEmailremove5;
	Spinner emailSpinner5;
	
	LinearLayout AddEmaillinear6;
	EditText AddEmail6;
	ImageButton AddEmailremove6;
	Spinner emailSpinner6;
	
	
	
	LinearLayout IMlinear;

	LinearLayout NOTESlinear;
	LinearLayout NICKNAMElinear;

	LinearLayout WEBSITElinear;

	LinearLayout INTERNETCALLlinear;
	Bitmap thumbnail;
	boolean nameSelection = false;
	Spinner phoneSpinner1;
	Spinner phoneSpinner2;

	LinearLayout AddAddresslinear;
	EditText AddStreet;
	EditText AddCity;
	EditText AddState;
	EditText AddPostalcode;
	EditText AddCountry;
	ImageButton AddAddressremove;
	Spinner addressSpinner;

	LinearLayout AddAddresslinear2;
	EditText AddStreet2;
	EditText AddCity2;
	EditText AddState2;
	EditText AddPostalcode2;
	EditText AddCountry2;
	ImageButton AddAddressremove2;
	Spinner addressSpinner2;

	LinearLayout AddAddresslinear3;
	EditText AddStreet3;
	EditText AddCity3;
	EditText AddState3;
	EditText AddPostalcode3;
	EditText AddCountry3;
	ImageButton AddAddressremove3;
	Spinner addressSpinner3;
	
	
	//Newly Added
	private EditText mAddCity;
	private EditText mAddCountry;
	private EditText mAddPostalCode;
	private EditText mAddStreet;
	private EditText mAddState;
		
	
	//Spinner addressSpinner;
	// Spinner imSpinner;
	//EmailRefreshManager mRefreshManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts_addcontact);
		this.setTitle("Done");
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setIcon(
				R.drawable.contacts_ic_menu_done_holo_dark);
		contact_image_frame = (FrameLayout) findViewById(R.id.contact_image_frame);
		AddName = (EditText) findViewById(R.id.AddName);
		AddImage = (ImageView) findViewById(R.id.AddImage);
		btn = (Button) findViewById(R.id.addContactAddAnotherField);
		
		mAddCity = (EditText) findViewById(R.id.AddCity);
		mAddCountry = (EditText) findViewById(R.id.AddCountry);
		mAddPostalCode = (EditText) findViewById(R.id.AddPostalcode);
		mAddStreet = (EditText) findViewById(R.id.AddStreet);
		mAddState = (EditText) findViewById(R.id.AddState);
		
		AddImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// /*
				// * Toast.makeText(AddContact.this,
				// * "The favorite list would appear on clicking this icon",
				// * Toast.LENGTH_LONG).show();
				// */
				//

				LayoutInflater inflater = (LayoutInflater) ContactsAddContact.this
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				View layout = inflater.inflate(R.layout.contacts_screen_popup,
						(ViewGroup) findViewById(R.id.popup_element));

				String[] new_array_image = getResources().getStringArray(
						R.array.add_contact_image_array);

				arrayAdapter_image = new ArrayAdapter<String>(
						ContactsAddContact.this,
						android.R.layout.simple_list_item_1, new_array_image);

				ListView lv = (ListView) layout.findViewById(R.id.listview);
				lv.setAdapter(arrayAdapter_image);

				pwindow = new PopupWindow(layout, 6 * AddImage.getWidth(),
						4 * AddImage.getHeight(), true);

				if (ContactsUtilities.isTablet(ContactsAddContact.this)) {

					pwindow = new PopupWindow(layout, 3 * AddImage.getWidth(),
							1 * AddImage.getHeight(), true);
				}
				pwindow.setBackgroundDrawable(new BitmapDrawable());

				/*
				 * pwindow.showAtLocation(layout, Gravity.NO_GRAVITY,
				 * AddImage.getRight() + AddImage.getWidth(),
				 * AddImage.getBottom() + AddImage.getHeight() / 2 +
				 * pwindow.getHeight() / 2);
				 */

				/*
				 * pwindow.showAtLocation(layout, Gravity.NO_GRAVITY,
				 * AddName.getBottom(), AddName.getBottom());
				 */

				// pwindow.showAsDropDown(AddImage, 50, -30);
				pwindow.showAsDropDown(AddImage, 0, 0);

				/*
				 * pwindow.showAtLocation(layout, Gravity.NO_GRAVITY,
				 * AddImage.getLeft() + AddImage.getWidth(), AddImage.getTop() +
				 * AddImage.getHeight());
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
					public void onItemClick(AdapterView<?> parent, View v,
							int position, long id) {
						// TODO Auto-generated method stub
						// super.onListItemClick(l, v, position, id);
						String name = (String) parent
								.getItemAtPosition(position);
						//System.out.println(name);
						if (name.equals("Take photo")) {

							Intent cameraIntent = new Intent(
									android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

							// request code

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
		});

		String[] new_array = getResources().getStringArray(
				R.array.add_contact_button_arrays);

		newArray = new ArrayList<String>();
		for (int i = 0; i < new_array.length; i++) {

			newArray.add(new_array[i]);

		}
		arrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, newArray);

		/** Instantiating PopupMenu class */
		// popup = new PopupMenu(getBaseContext(), btn);

		// /** Adding menu items to the popumenu */
		// popup.getMenuInflater().inflate(R.menu.addcontactbuttonpopup,
		// popup.getMenu());

		AddPhone = (EditText) findViewById(R.id.AddPhone);
		AddPhoneremove = (ImageButton) findViewById(R.id.AddPhoneremove);
		AddNewPhone = (Button) findViewById(R.id.AddNewPhone);

		AddPhonelinear2 = (LinearLayout) findViewById(R.id.AddPhonelinear2);
		AddPhonelinear1 = (LinearLayout) findViewById(R.id.AddPhonelinear1);

		AddPhonelinear3 = (LinearLayout) findViewById(R.id.AddPhonelinear3);
		phoneSpinner3 = (Spinner) findViewById(R.id.AddPhonespinner3);
		AddPhonelinear4 = (LinearLayout) findViewById(R.id.AddPhonelinear4);
		phoneSpinner4 = (Spinner) findViewById(R.id.AddPhonespinner4);
		AddPhonelinear5 = (LinearLayout) findViewById(R.id.AddPhonelinear5);
		phoneSpinner5 = (Spinner) findViewById(R.id.AddPhonespinner5);
		AddPhonelinear6 = (LinearLayout) findViewById(R.id.AddPhonelinear6);
		phoneSpinner6 = (Spinner) findViewById(R.id.AddPhonespinner6);
		AddPhonelinear7 = (LinearLayout) findViewById(R.id.AddPhonelinear7);
		phoneSpinner7 = (Spinner) findViewById(R.id.AddPhonespinner7);
		AddPhonelinear8 = (LinearLayout) findViewById(R.id.AddPhonelinear8);
		phoneSpinner8 = (Spinner) findViewById(R.id.AddPhonespinner8);
		AddPhonelinear9 = (LinearLayout) findViewById(R.id.AddPhonelinear9);
		phoneSpinner9 = (Spinner) findViewById(R.id.AddPhonespinner9);
		AddPhonelinear10 = (LinearLayout) findViewById(R.id.AddPhonelinear10);
		phoneSpinner10 = (Spinner) findViewById(R.id.AddPhonespinner10);
		AddPhonelinear11 = (LinearLayout) findViewById(R.id.AddPhonelinear11);
		phoneSpinner11 = (Spinner) findViewById(R.id.AddPhonespinner11);
		AddPhonelinear12 = (LinearLayout) findViewById(R.id.AddPhonelinear12);
		phoneSpinner12 = (Spinner) findViewById(R.id.AddPhonespinner12);
		AddPhonelinear13 = (LinearLayout) findViewById(R.id.AddPhonelinear13);
		phoneSpinner13 = (Spinner) findViewById(R.id.AddPhonespinner13);
		AddPhonelinear14 = (LinearLayout) findViewById(R.id.AddPhonelinear14);
		phoneSpinner14 = (Spinner) findViewById(R.id.AddPhonespinner14);
		AddPhonelinear15 = (LinearLayout) findViewById(R.id.AddPhonelinear15);
		phoneSpinner15 = (Spinner) findViewById(R.id.AddPhonespinner15);
		
		phoneSpinner1 = (Spinner) findViewById(R.id.AddPhonespinner);
		phoneSpinner2 = (Spinner) findViewById(R.id.AddPhonespinner2);
		
		AddEmail = (EditText) findViewById(R.id.AddEmail);
		AddEmailremove = (ImageButton) findViewById(R.id.AddEmailremove);
		AddNewEmail = (Button) findViewById(R.id.AddNewEmail);
		
		AddEmaillinear = (LinearLayout) findViewById(R.id.AddEmaillinear);
		emailSpinner = (Spinner) findViewById(R.id.AddEmailspinner);
		AddEmaillinear2 = (LinearLayout) findViewById(R.id.AddEmaillinear2);
		emailSpinner2 = (Spinner) findViewById(R.id.AddEmailspinner2);
		AddEmaillinear3 = (LinearLayout) findViewById(R.id.AddEmaillinear3);
		emailSpinner3 = (Spinner) findViewById(R.id.AddEmailspinner3);
		AddEmaillinear4 = (LinearLayout) findViewById(R.id.AddEmaillinear4);
		emailSpinner4 = (Spinner) findViewById(R.id.AddEmailspinner4);
		AddEmaillinear5 = (LinearLayout) findViewById(R.id.AddEmaillinear5);
		emailSpinner5 = (Spinner) findViewById(R.id.AddEmailspinner5);
		AddEmaillinear6 = (LinearLayout) findViewById(R.id.AddEmaillinear6);
		emailSpinner6 = (Spinner) findViewById(R.id.AddEmailspinner6);
		
		AddIM = (EditText) findViewById(R.id.AddIM);
		AddIMremove = (ImageButton) findViewById(R.id.AddIMremove);
		AddNewIM = (Button) findViewById(R.id.AddNewIM);
		
		AddIMlinear = (LinearLayout) findViewById(R.id.AddIMlinear);
		IMSpinner = (Spinner) findViewById(R.id.AddIMspinner);
		AddIMlinear2 = (LinearLayout) findViewById(R.id.AddIMlinear2);
		IMSpinner2 = (Spinner) findViewById(R.id.AddIMspinner2);
		AddIMlinear3 = (LinearLayout) findViewById(R.id.AddIMlinear3);
		IMSpinner3 = (Spinner) findViewById(R.id.AddIMspinner3);
		AddIMlinear4 = (LinearLayout) findViewById(R.id.AddIMlinear4);
		IMSpinner4 = (Spinner) findViewById(R.id.AddIMspinner4);
		AddIMlinear5 = (LinearLayout) findViewById(R.id.AddIMlinear5);
		IMSpinner5 = (Spinner) findViewById(R.id.AddIMspinner5);
		AddIMlinear6 = (LinearLayout) findViewById(R.id.AddIMlinear6);
		IMSpinner6 = (Spinner) findViewById(R.id.AddIMspinner6);

		AddNewAddress = (Button) findViewById(R.id.AddNewAddress);
		
		AddAddresslinear = (LinearLayout) findViewById(R.id.AddAddresslinear);
		AddStreet = (EditText) findViewById(R.id.AddStreet);
		AddCity = (EditText) findViewById(R.id.AddCity);
		AddState = (EditText) findViewById(R.id.AddState);
		AddPostalcode = (EditText) findViewById(R.id.AddPostalcode);
		AddCountry = (EditText) findViewById(R.id.AddCountry);
		AddAddressremove = (ImageButton) findViewById(R.id.AddAddressremove);
		addressSpinner = (Spinner) findViewById(R.id.AddAddressspinner);
			
		AddAddresslinear2 = (LinearLayout) findViewById(R.id.AddAddresslinear2);
		AddStreet2 = (EditText) findViewById(R.id.AddStreet2);
		AddCity2 = (EditText) findViewById(R.id.AddCity2);
		AddState2 = (EditText) findViewById(R.id.AddState2);
		AddPostalcode2 = (EditText) findViewById(R.id.AddPostalcode2);
		AddCountry2 = (EditText) findViewById(R.id.AddCountry2);
		AddAddressremove2 = (ImageButton) findViewById(R.id.AddAddressremove2);
		addressSpinner2 = (Spinner) findViewById(R.id.AddAddressspinner2);
		
		AddAddresslinear3 = (LinearLayout) findViewById(R.id.AddAddresslinear3);
		AddStreet3 = (EditText) findViewById(R.id.AddStreet3);
		AddCity3 = (EditText) findViewById(R.id.AddCity3);
		AddState3 = (EditText) findViewById(R.id.AddState3);
		AddPostalcode3 = (EditText) findViewById(R.id.AddPostalcode3);
		AddCountry3 = (EditText) findViewById(R.id.AddCountry3);
		AddAddressremove3 = (ImageButton) findViewById(R.id.AddAddressremove3);
		addressSpinner3 = (Spinner) findViewById(R.id.AddAddressspinner3);
		
		AddStreet.addTextChangedListener(address1ChangeListener);
		AddCity.addTextChangedListener(address1ChangeListener);
		AddState.addTextChangedListener(address1ChangeListener);
		AddPostalcode.addTextChangedListener(address1ChangeListener);
		AddCountry.addTextChangedListener(address1ChangeListener);
		
		AddStreet2.addTextChangedListener(address2ChangeListener);
		AddCity2.addTextChangedListener(address2ChangeListener);
		AddState2.addTextChangedListener(address2ChangeListener);
		AddPostalcode2.addTextChangedListener(address2ChangeListener);
		AddCountry2.addTextChangedListener(address2ChangeListener);
		
		AddStreet3.addTextChangedListener(address3ChangeListener);
		AddCity3.addTextChangedListener(address3ChangeListener);
		AddState3.addTextChangedListener(address3ChangeListener);
		AddPostalcode3.addTextChangedListener(address3ChangeListener);
		AddCountry3.addTextChangedListener(address3ChangeListener);
		
		
		
		
		
		
		/// venkat changes////
		Intent intent=this.getIntent();
		String newNumber=intent.getStringExtra("new_num");
		AddPhone.setText(newNumber);
		/// venkat changes////

		AddPhone.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				if (AddPhone.getText().toString().equals("")) {
					AddPhoneremove.setVisibility(View.GONE);
					AddNewPhone.setVisibility(View.GONE);
				} else {
					AddPhoneremove.setVisibility(View.VISIBLE);
					if (AddPhonelinear2.getVisibility() == View.GONE) {
						AddNewPhone.setVisibility(View.VISIBLE);
						checkPhoneVisibility();
					}
				}
			}
		});

		AddPhoneremove2 = (ImageButton) findViewById(R.id.AddPhoneremove2);
		AddPhone2 = (EditText) findViewById(R.id.AddPhone2);
		AddPhone2.addTextChangedListener(new TextWatcher() {

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				if (AddPhone2.getText().toString().equals("")) {
					AddPhoneremove2.setVisibility(View.GONE);
					AddNewPhone.setVisibility(View.GONE);
				} else {

					AddPhoneremove2.setVisibility(View.VISIBLE);
					//if (AddPhonelinear1.getVisibility() == View.GONE) {
						AddNewPhone.setVisibility(View.VISIBLE);
						checkPhoneVisibility();
					//}
				}
			}
		});

		AddPhoneremove3 = (ImageButton) findViewById(R.id.AddPhoneremove3);
		AddPhone3 = (EditText) findViewById(R.id.AddPhone3);
		AddPhone3.addTextChangedListener(new TextWatcher() {

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				if (AddPhone3.getText().toString().equals("")) {
					AddPhoneremove3.setVisibility(View.GONE);
					AddNewPhone.setVisibility(View.GONE);
				} else {

					AddPhoneremove3.setVisibility(View.VISIBLE);
					//if (AddPhonelinear2.getVisibility() == View.GONE) {
						AddNewPhone.setVisibility(View.VISIBLE);
						checkPhoneVisibility();
					//}
				}
			}
		});
		
		AddPhoneremove4 = (ImageButton) findViewById(R.id.AddPhoneremove4);
		AddPhone4 = (EditText) findViewById(R.id.AddPhone4);
		AddPhone4.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
			public void onTextChanged(CharSequence s, int start, int before,int count) {}
			@Override
			public void afterTextChanged(Editable arg0) {
				if (AddPhone4.getText().toString().equals("")) {
					AddPhoneremove4.setVisibility(View.GONE);
					AddNewPhone.setVisibility(View.GONE);
				} else {
					AddPhoneremove4.setVisibility(View.VISIBLE);
					//if (AddPhonelinear3.getVisibility() == View.GONE) {
						AddNewPhone.setVisibility(View.VISIBLE);
						checkPhoneVisibility();
					//}
				}
			}
		});
		
		AddPhoneremove5 = (ImageButton) findViewById(R.id.AddPhoneremove5);
		AddPhone5 = (EditText) findViewById(R.id.AddPhone5);
		AddPhone5.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
			public void onTextChanged(CharSequence s, int start, int before,int count) {}
			@Override
			public void afterTextChanged(Editable arg0) {
				if (AddPhone5.getText().toString().equals("")) {
					AddPhoneremove5.setVisibility(View.GONE);
					AddNewPhone.setVisibility(View.GONE);
				} else {
					AddPhoneremove5.setVisibility(View.VISIBLE);
						AddNewPhone.setVisibility(View.VISIBLE);
						checkPhoneVisibility();
				}
			}
		});
		
		AddPhoneremove6 = (ImageButton) findViewById(R.id.AddPhoneremove6);
		AddPhone6 = (EditText) findViewById(R.id.AddPhone6);
		AddPhone6.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
			public void onTextChanged(CharSequence s, int start, int before,int count) {}
			@Override
			public void afterTextChanged(Editable arg0) {
				if (AddPhone6.getText().toString().equals("")) {
					AddPhoneremove6.setVisibility(View.GONE);
					AddNewPhone.setVisibility(View.GONE);
				} else {
					AddPhoneremove6.setVisibility(View.VISIBLE);
						AddNewPhone.setVisibility(View.VISIBLE);
						checkPhoneVisibility();
				}
			}
		});
		
		AddPhoneremove7 = (ImageButton) findViewById(R.id.AddPhoneremove7);
		AddPhone7 = (EditText) findViewById(R.id.AddPhone7);
		AddPhone7.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
			public void onTextChanged(CharSequence s, int start, int before,int count) {}
			@Override
			public void afterTextChanged(Editable arg0) {
				if (AddPhone7.getText().toString().equals("")) {
					AddPhoneremove7.setVisibility(View.GONE);
					AddNewPhone.setVisibility(View.GONE);
				} else {
					AddPhoneremove7.setVisibility(View.VISIBLE);
						AddNewPhone.setVisibility(View.VISIBLE);
						checkPhoneVisibility();
				}
			}
		});
		
		AddPhoneremove8 = (ImageButton) findViewById(R.id.AddPhoneremove8);
		AddPhone8 = (EditText) findViewById(R.id.AddPhone8);
		AddPhone8.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
			public void onTextChanged(CharSequence s, int start, int before,int count) {}
			@Override
			public void afterTextChanged(Editable arg0) {
				if (AddPhone8.getText().toString().equals("")) {
					AddPhoneremove8.setVisibility(View.GONE);
					AddNewPhone.setVisibility(View.GONE);
				} else {
					AddPhoneremove8.setVisibility(View.VISIBLE);
						AddNewPhone.setVisibility(View.VISIBLE);
						checkPhoneVisibility();
				}
			}
		});
		
		AddPhoneremove9 = (ImageButton) findViewById(R.id.AddPhoneremove9);
		AddPhone9 = (EditText) findViewById(R.id.AddPhone9);
		AddPhone9.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
			public void onTextChanged(CharSequence s, int start, int before,int count) {}
			@Override
			public void afterTextChanged(Editable arg0) {
				if (AddPhone9.getText().toString().equals("")) {
					AddPhoneremove9.setVisibility(View.GONE);
					AddNewPhone.setVisibility(View.GONE);
				} else {
					AddPhoneremove9.setVisibility(View.VISIBLE);
						AddNewPhone.setVisibility(View.VISIBLE);
						checkPhoneVisibility();
				}
			}
		});
		
		AddPhoneremove10 = (ImageButton) findViewById(R.id.AddPhoneremove10);
		AddPhone10 = (EditText) findViewById(R.id.AddPhone10);
		AddPhone10.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
			public void onTextChanged(CharSequence s, int start, int before,int count) {}
			@Override
			public void afterTextChanged(Editable arg0) {
				if (AddPhone10.getText().toString().equals("")) {
					AddPhoneremove10.setVisibility(View.GONE);
					AddNewPhone.setVisibility(View.GONE);
				} else {
					AddPhoneremove10.setVisibility(View.VISIBLE);
						AddNewPhone.setVisibility(View.VISIBLE);
						checkPhoneVisibility();
				}
			}
		});
		
		AddPhoneremove11 = (ImageButton) findViewById(R.id.AddPhoneremove11);
		AddPhone11 = (EditText) findViewById(R.id.AddPhone11);
		AddPhone11.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
			public void onTextChanged(CharSequence s, int start, int before,int count) {}
			@Override
			public void afterTextChanged(Editable arg0) {
				if (AddPhone11.getText().toString().equals("")) {
					AddPhoneremove11.setVisibility(View.GONE);
					AddNewPhone.setVisibility(View.GONE);
				} else {
					AddPhoneremove11.setVisibility(View.VISIBLE);
						AddNewPhone.setVisibility(View.VISIBLE);
						checkPhoneVisibility();
				}
			}
		});
		
		AddPhoneremove12 = (ImageButton) findViewById(R.id.AddPhoneremove12);
		AddPhone12 = (EditText) findViewById(R.id.AddPhone12);
		AddPhone12.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
			public void onTextChanged(CharSequence s, int start, int before,int count) {}
			@Override
			public void afterTextChanged(Editable arg0) {
				if (AddPhone12.getText().toString().equals("")) {
					AddPhoneremove12.setVisibility(View.GONE);
					AddNewPhone.setVisibility(View.GONE);
				} else {
					AddPhoneremove12.setVisibility(View.VISIBLE);
						AddNewPhone.setVisibility(View.VISIBLE);
						checkPhoneVisibility();
				}
			}
		});
		AddPhoneremove13 = (ImageButton) findViewById(R.id.AddPhoneremove13);
		AddPhone13 = (EditText) findViewById(R.id.AddPhone13);
		AddPhone13.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
			public void onTextChanged(CharSequence s, int start, int before,int count) {}
			@Override
			public void afterTextChanged(Editable arg0) {
				if (AddPhone13.getText().toString().equals("")) {
					AddPhoneremove13.setVisibility(View.GONE);
					AddNewPhone.setVisibility(View.GONE);
				} else {
					AddPhoneremove13.setVisibility(View.VISIBLE);
						AddNewPhone.setVisibility(View.VISIBLE);
						checkPhoneVisibility();
				}
			}
		});
		
		AddPhoneremove14 = (ImageButton) findViewById(R.id.AddPhoneremove14);
		AddPhone14 = (EditText) findViewById(R.id.AddPhone14);
		AddPhone14.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
			public void onTextChanged(CharSequence s, int start, int before,int count) {}
			@Override
			public void afterTextChanged(Editable arg0) {
				if (AddPhone14.getText().toString().equals("")) {
					AddPhoneremove14.setVisibility(View.GONE);
					AddNewPhone.setVisibility(View.GONE);
				} else {
					AddPhoneremove14.setVisibility(View.VISIBLE);
						AddNewPhone.setVisibility(View.VISIBLE);
						checkPhoneVisibility();
				}
			}
		});
		
		AddPhoneremove15 = (ImageButton) findViewById(R.id.AddPhoneremove15);
		AddPhone15 = (EditText) findViewById(R.id.AddPhone15);
		AddPhone15.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
			public void onTextChanged(CharSequence s, int start, int before,int count) {}
			@Override
			public void afterTextChanged(Editable arg0) {
				if (AddPhone15.getText().toString().equals("")) {
					AddPhoneremove15.setVisibility(View.GONE);
					AddNewPhone.setVisibility(View.GONE);
				} else {
					AddPhoneremove15.setVisibility(View.VISIBLE);
					//if (AddPhonelinear14.getVisibility() == View.GONE) {
						AddNewPhone.setVisibility(View.VISIBLE);
						checkPhoneVisibility();
					//}
				}
			}
		});
		
		
		AddEmail.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				if (AddEmail.getText().toString().equals("")) {
					AddEmailremove.setVisibility(View.GONE);
					AddNewEmail.setVisibility(View.GONE);
				} else {
					AddEmailremove.setVisibility(View.VISIBLE);
					if (AddEmaillinear2.getVisibility() == View.GONE) {
						AddNewEmail.setVisibility(View.VISIBLE);
						checkEmailVisibility();
					}
				}
			}
		});

		AddEmailremove2 = (ImageButton) findViewById(R.id.AddEmailremove2);
		AddEmail2 = (EditText) findViewById(R.id.AddEmail2);
		AddEmail2.addTextChangedListener(new TextWatcher() {

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				if (AddEmail2.getText().toString().equals("")) {
					AddEmailremove2.setVisibility(View.GONE);
					AddNewEmail.setVisibility(View.GONE);
				} else {

					AddEmailremove2.setVisibility(View.VISIBLE);
					//if (AddEmaillinear1.getVisibility() == View.GONE) {
						AddNewEmail.setVisibility(View.VISIBLE);
						checkEmailVisibility();
					//}
				}
			}
		});

		AddEmailremove3 = (ImageButton) findViewById(R.id.AddEmailremove3);
		AddEmail3 = (EditText) findViewById(R.id.AddEmail3);
		AddEmail3.addTextChangedListener(new TextWatcher() {

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				if (AddEmail3.getText().toString().equals("")) {
					AddEmailremove3.setVisibility(View.GONE);
					AddNewEmail.setVisibility(View.GONE);
				} else {

					AddEmailremove3.setVisibility(View.VISIBLE);
					//if (AddEmaillinear2.getVisibility() == View.GONE) {
						AddNewEmail.setVisibility(View.VISIBLE);
						checkEmailVisibility();
					//}
				}
			}
		});
		
		AddEmailremove4 = (ImageButton) findViewById(R.id.AddEmailremove4);
		AddEmail4 = (EditText) findViewById(R.id.AddEmail4);
		AddEmail4.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
			public void onTextChanged(CharSequence s, int start, int before,int count) {}
			@Override
			public void afterTextChanged(Editable arg0) {
				if (AddEmail4.getText().toString().equals("")) {
					AddEmailremove4.setVisibility(View.GONE);
					AddNewEmail.setVisibility(View.GONE);
				} else {
					AddEmailremove4.setVisibility(View.VISIBLE);
					//if (AddEmaillinear3.getVisibility() == View.GONE) {
						AddNewEmail.setVisibility(View.VISIBLE);
						checkEmailVisibility();
					//}
				}
			}
		});
		
		AddEmailremove5 = (ImageButton) findViewById(R.id.AddEmailremove5);
		AddEmail5 = (EditText) findViewById(R.id.AddEmail5);
		AddEmail5.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
			public void onTextChanged(CharSequence s, int start, int before,int count) {}
			@Override
			public void afterTextChanged(Editable arg0) {
				if (AddEmail5.getText().toString().equals("")) {
					AddEmailremove5.setVisibility(View.GONE);
					AddNewEmail.setVisibility(View.GONE);
				} else {
					AddEmailremove5.setVisibility(View.VISIBLE);
						AddNewEmail.setVisibility(View.VISIBLE);
						checkEmailVisibility();
				}
			}
		});
		
		AddEmailremove6 = (ImageButton) findViewById(R.id.AddEmailremove6);
		AddEmail6 = (EditText) findViewById(R.id.AddEmail6);
		AddEmail6.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
			public void onTextChanged(CharSequence s, int start, int before,int count) {}
			@Override
			public void afterTextChanged(Editable arg0) {
				if (AddEmail6.getText().toString().equals("")) {
					AddEmailremove6.setVisibility(View.GONE);
					AddNewEmail.setVisibility(View.GONE);
				} else {
					AddEmailremove6.setVisibility(View.VISIBLE);
					if (AddEmaillinear5.getVisibility() == View.GONE) {
					AddNewEmail.setVisibility(View.VISIBLE);
					checkEmailVisibility();
					}
				}
			}
		});
		
		//IM field Show/Hide
		AddIM.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				if (AddIM.getText().toString().equals("")) {
					AddIMremove.setVisibility(View.GONE);
					AddNewIM.setVisibility(View.GONE);
				} else {
					AddIMremove.setVisibility(View.VISIBLE);
					if (AddIMlinear2.getVisibility() == View.GONE) {
						AddNewIM.setVisibility(View.VISIBLE);
						checkImVisibility();
					}
				}
			}
		});

		AddIMremove2 = (ImageButton) findViewById(R.id.AddIMremove2);
		AddIM2 = (EditText) findViewById(R.id.AddIM2);
		AddIM2.addTextChangedListener(new TextWatcher() {

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				if (AddIM2.getText().toString().equals("")) {
					AddIMremove2.setVisibility(View.GONE);
					AddNewIM.setVisibility(View.GONE);
				} else {

					AddIMremove2.setVisibility(View.VISIBLE);
					//if (AddIMlinear1.getVisibility() == View.GONE) {
						AddNewIM.setVisibility(View.VISIBLE);
						checkImVisibility();
					//}
				}
			}
		});

		AddIMremove3 = (ImageButton) findViewById(R.id.AddIMremove3);
		AddIM3 = (EditText) findViewById(R.id.AddIM3);
		AddIM3.addTextChangedListener(new TextWatcher() {

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				if (AddIM3.getText().toString().equals("")) {
					AddIMremove3.setVisibility(View.GONE);
					AddNewIM.setVisibility(View.GONE);
				} else {

					AddIMremove3.setVisibility(View.VISIBLE);
					//if (AddIMlinear2.getVisibility() == View.GONE) {
						AddNewIM.setVisibility(View.VISIBLE);
						checkImVisibility();
					//}
				}
			}
		});
		
		AddIMremove4 = (ImageButton) findViewById(R.id.AddIMremove4);
		AddIM4 = (EditText) findViewById(R.id.AddIM4);
		AddIM4.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
			public void onTextChanged(CharSequence s, int start, int before,int count) {}
			@Override
			public void afterTextChanged(Editable arg0) {
				if (AddIM4.getText().toString().equals("")) {
					AddIMremove4.setVisibility(View.GONE);
					AddNewIM.setVisibility(View.GONE);
				} else {
					AddIMremove4.setVisibility(View.VISIBLE);
					//if (AddIMlinear3.getVisibility() == View.GONE) {
						AddNewIM.setVisibility(View.VISIBLE);
						checkImVisibility();
					//}
				}
			}
		});
		
		AddIMremove5 = (ImageButton) findViewById(R.id.AddIMremove5);
		AddIM5 = (EditText) findViewById(R.id.AddIM5);
		AddIM5.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
			public void onTextChanged(CharSequence s, int start, int before,int count) {}
			@Override
			public void afterTextChanged(Editable arg0) {
				if (AddIM5.getText().toString().equals("")) {
					AddIMremove5.setVisibility(View.GONE);
					AddNewIM.setVisibility(View.GONE);
				} else {
					AddIMremove5.setVisibility(View.VISIBLE);
						AddNewIM.setVisibility(View.VISIBLE);
						checkImVisibility();
				}
			}
		});
		
		AddIMremove6 = (ImageButton) findViewById(R.id.AddIMremove6);
		AddIM6 = (EditText) findViewById(R.id.AddIM6);
		AddIM6.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
			public void onTextChanged(CharSequence s, int start, int before,int count) {}
			@Override
			public void afterTextChanged(Editable arg0) {
				if (AddIM6.getText().toString().equals("")) {
					AddIMremove6.setVisibility(View.GONE);
					AddNewIM.setVisibility(View.GONE);
				} else {
					AddIMremove6.setVisibility(View.VISIBLE);
					if (AddIMlinear5.getVisibility() == View.GONE) {
					AddNewIM.setVisibility(View.VISIBLE);
					checkImVisibility();
					}
				}
			}
		});
		
		
		
		

		checkPhoneVisibility();
		checkEmailVisibility();
		checkImVisibility();
		
		
		
		
		
		
		
		
		
		
		
		
		

		/*
		 * if ((AddPhone.getVisibility() == View.VISIBLE) &&
		 * (AddPhone.getVisibility() == View.VISIBLE)) {
		 * AddNewPhone.setVisibility(View.GONE); } else {
		 * 
		 * AddNewPhone.setVisibility(View.VISIBLE);
		 * 
		 * 
		 * }
		 */

		/*
		 * Spinner cRaceSpinner = (Spinner) findViewById(R.id.AddPhonespinner);
		 * cRaceSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
		 * public void onItemSelected(AdapterView<?> arg0, View arg1,arg2, long
		 * arg3) { String strChosenRace = (String) arg0.getItemAtPosition(arg2);
		 * } public void onNothingSelected(AdapterView<?> arg0) {} });
		 */

		IMlinear = (LinearLayout) findViewById(R.id.IMlinear);

		NOTESlinear = (LinearLayout) findViewById(R.id.NOTESlinear);

		NICKNAMElinear = (LinearLayout) findViewById(R.id.NICKNAMElinear);
		WEBSITElinear = (LinearLayout) findViewById(R.id.WEBSITElinear);

		INTERNETCALLlinear = (LinearLayout) findViewById(R.id.INTERNETCALLlinear);

		AddOrganztn = (EditText) findViewById(R.id.AddOrganztn);
		AddTitle = (EditText) findViewById(R.id.AddTitle);
		AddOrganztn.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					AddOrganztn.setHint("Company");

					AddTitle.setVisibility(View.VISIBLE);
				} else {
					// Toast.makeText(getApplicationContext(), "lost the focus",
					// Toast.LENGTH_LONG).show();
				}
			}
		});

		phonetici = 0;

		i = 0;

		namepfx = (EditText) findViewById(R.id.AddNamePrefix);
		middleName = (EditText) findViewById(R.id.AddMiddleName);
		lastName = (EditText) findViewById(R.id.AddLastName);
		nameSuffix = (EditText) findViewById(R.id.AddNameSuffix);

		PhoneticNameImageButton = (ImageButton) findViewById(R.id.AddPhoneticnameimageButton1);

		AddPhoneticName = (EditText) findViewById(R.id.AddPhoneticName);
		AddNotes = (EditText) findViewById(R.id.AddNotes);
		AddNICKNAME = (EditText) findViewById(R.id.AddNICKNAME);
		AddWEBSITE = (EditText) findViewById(R.id.AddWEBSITE);
		AddINTERNETCALL = (EditText) findViewById(R.id.AddINTERNETCALL);

		AddPhoneticMiddleName = (EditText) findViewById(R.id.AddPhoneticMiddleName);
		AddPhoneticLastName = (EditText) findViewById(R.id.AddPhoneticLastName);

		addListenerOnButton();

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		OnClickListener listener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!newArray.isEmpty())
					initiatePopupWindow();

			}
		};

		btn.setOnClickListener(listener);

		
		Bundle bundle = getIntent().getExtras();

		ContactsLog.d("loadContactfromModel", "bundle" + bundle);
		if (bundle != null) {

			Serializable object = bundle.getSerializable("obj");
			if (object != null) {
				ContactsModel mContact = (ContactsModel) object;
				ContactsLog.d("loadContactfromModel", "mContact" + mContact);
				loadContactfromModel(mContact);
			}
		}
	}
	
	
	
	
	
	

	
	void checkPhoneVisibility()
	{

		if (AddPhonelinear1.getVisibility() == View.VISIBLE && AddPhonelinear2.getVisibility() == View.VISIBLE &&
			AddPhonelinear3.getVisibility() == View.VISIBLE && AddPhonelinear4.getVisibility() == View.VISIBLE &&
			AddPhonelinear5.getVisibility() == View.VISIBLE && AddPhonelinear6.getVisibility() == View.VISIBLE &&
			AddPhonelinear7.getVisibility() == View.VISIBLE && AddPhonelinear8.getVisibility() == View.VISIBLE &&
			AddPhonelinear9.getVisibility() == View.VISIBLE && AddPhonelinear10.getVisibility() == View.VISIBLE &&
			AddPhonelinear11.getVisibility() == View.VISIBLE && AddPhonelinear12.getVisibility() == View.VISIBLE &&
			AddPhonelinear13.getVisibility() == View.VISIBLE && AddPhonelinear14.getVisibility() == View.VISIBLE &&
			AddPhonelinear15.getVisibility() == View.VISIBLE)
		{
			AddNewPhone.setVisibility(View.GONE);
		}
				
	}
	
	
	
	
	void checkEmailVisibility()
	{
	
	if (AddEmaillinear.getVisibility() == View.VISIBLE && AddEmaillinear2.getVisibility() == View.VISIBLE &&
			AddEmaillinear3.getVisibility() == View.VISIBLE && AddEmaillinear4.getVisibility() == View.VISIBLE &&
			AddEmaillinear5.getVisibility() == View.VISIBLE && AddEmaillinear6.getVisibility() == View.VISIBLE)
		{
			AddNewEmail.setVisibility(View.GONE);
		}
	
	}
	
	
	void checkImVisibility()
	{
	
	if (AddIMlinear.getVisibility() == View.VISIBLE && AddIMlinear2.getVisibility() == View.VISIBLE &&
			AddIMlinear3.getVisibility() == View.VISIBLE && AddIMlinear4.getVisibility() == View.VISIBLE &&
			AddIMlinear5.getVisibility() == View.VISIBLE && AddIMlinear6.getVisibility() == View.VISIBLE)
		{
			AddNewIM.setVisibility(View.GONE);
		}
	
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	void loadContactfromModel(ContactsModel contact) {
		
		String firstName = contact.getcontacts_first_name();
		String lastnName = contact.getcontacts_last_name();
		String email = contact.getcontacts_email1_address();

		ContactsLog.d("loadContactfromModel", "Firstname " + firstName + " "
				+ lastnName + " " + email);
		AddName.setText(firstName);
		lastName.setText(lastnName);
		AddEmail.setText(email);
		AddPhone.setText(contact.getcontacts_mobile_telephone_number());
		AddOrganztn.setText(contact.getcontacts_yomi_company_name());
		AddTitle.setText(contact.getcontacts_title());
		mAddCity.setText(contact.getcontacts_home_address_city());
		mAddCountry.setText(contact.getcontacts_home_address_country());
		mAddPostalCode.setText(contact.getcontacts_home_address_postal_code());
		mAddState.setText(contact.getcontacts_home_address_state());
		mAddStreet.setText(contact.getcontacts_home_address_street());
		AddPhone.setText(contact.getcontacts_mobile_telephone_number());

	}

TextWatcher address1ChangeListener = new TextWatcher() {
		
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}

		@Override
		public void afterTextChanged(Editable arg0) {
			// TODO Auto-generated method stub
			
			if (AddStreet.getText().toString().equals("") && AddCity.getText().toString().equals("") 
					&& AddState.getText().toString().equals("") && AddPostalcode.getText().toString().equals("") 
					&&  AddCountry.getText().toString().equals("")) {
				AddAddressremove.setVisibility(View.GONE);
				AddNewAddress.setVisibility(View.GONE);
			} else {
				AddAddressremove.setVisibility(View.VISIBLE);
				if(AddAddresslinear.getVisibility() == View.VISIBLE &&
						AddAddresslinear2.getVisibility() == View.VISIBLE &&
						AddAddresslinear3.getVisibility() == View.VISIBLE){
					AddNewAddress.setVisibility(View.GONE);
				}else if (AddAddresslinear2.getVisibility() == View.GONE ) {
					AddNewAddress.setVisibility(View.VISIBLE);
				}
			}
		}
	};
	
	TextWatcher address2ChangeListener = new TextWatcher() {
		
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}

		@Override
		public void afterTextChanged(Editable arg0) {
			// TODO Auto-generated method stub
			
			if (AddStreet2.getText().toString().equals("") && AddCity2.getText().toString().equals("") 
					&& AddState2.getText().toString().equals("") && AddPostalcode2.getText().toString().equals("") 
					&&  AddCountry2.getText().toString().equals("")) {
				AddAddressremove2.setVisibility(View.GONE);
				AddNewAddress.setVisibility(View.GONE);
			} else {
				AddAddressremove2.setVisibility(View.VISIBLE);
				if(AddAddresslinear.getVisibility() == View.VISIBLE &&
						AddAddresslinear2.getVisibility() == View.VISIBLE &&
						AddAddresslinear3.getVisibility() == View.VISIBLE){
					AddNewAddress.setVisibility(View.GONE);
				}else{
					AddNewAddress.setVisibility(View.VISIBLE);
				}
			}
		}
	};
	
	TextWatcher address3ChangeListener = new TextWatcher() {
		
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}

		@Override
		public void afterTextChanged(Editable arg0) {
			// TODO Auto-generated method stub
			if (AddStreet3.getText().toString().equals("") && AddCity3.getText().toString().equals("") 
					&& AddState3.getText().toString().equals("") && AddPostalcode3.getText().toString().equals("") 
					&&  AddCountry3.getText().toString().equals("")) {
				AddAddressremove3.setVisibility(View.GONE);
				AddNewAddress.setVisibility(View.GONE);
			} else {
				AddAddressremove3.setVisibility(View.VISIBLE);
				if (AddAddresslinear2.getVisibility() == View.GONE) {
					AddNewAddress.setVisibility(View.VISIBLE);
				}
			}
		}
	};
	protected void initiatePopupWindow() {
		// TODO Auto-generated method stub
		try {
			// We need to get the instance of the LayoutInflater
			LayoutInflater inflater = (LayoutInflater) ContactsAddContact.this
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			View layout = inflater.inflate(R.layout.contacts_screen_popup,
					(ViewGroup) findViewById(R.id.popup_element));

			ListView lv = (ListView) layout.findViewById(R.id.listview);

			lv.setAdapter(arrayAdapter);
			int height = btn.getHeight() * newArray.size();
			int width = btn.getWidth();
			pwindo = new PopupWindow(layout, width, height, true);
			pwindo.setBackgroundDrawable(new BitmapDrawable());
			/*
			 * int x = (int) btn.getLeft(); int y = (int) btn.getTop();
			 */

			/*
			 * pwindo.showAtLocation(layout, Gravity.NO_GRAVITY, x, (y - (height
			 * + btn.getHeight()) / 2));
			 */

			pwindo.showAsDropDown(btn, 0, 0);

			pwindo.setOutsideTouchable(true);

			lv.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View v,
						int position, long id) {
					// TODO Auto-generated method stub
					// super.onListItemClick(l, v, position, id);
					String name = (String) parent.getItemAtPosition(position);
					//System.out.println(name);
					/* arrayAdapter.remove(name); */

					if (name.equals("Phonetic name")) {

						PhoneticNameImageButton.setVisibility(View.VISIBLE);

						AddPhoneticName.setVisibility(View.VISIBLE);

						// AddPhone2.requestFocus();

						AddPhoneticName.requestFocus();

					} else if (name.equals("IM")) {

						IMlinear.setVisibility(View.VISIBLE);

						AddIM.requestFocus();

					}

					else if (name.equals("Notes")) {

						NOTESlinear.setVisibility(View.VISIBLE);

						AddNotes.requestFocus();

						/*
						 * EditText AddNotes; EditText AddNICKNAME; EditText
						 * AddWEBSITE; EditText AddINTERNETCALL;
						 */

					}

					else if (name.equals("Nickname")) {

						NICKNAMElinear.setVisibility(View.VISIBLE);

						AddNICKNAME.requestFocus();

					}

					else if (name.equals("Website")) {

						WEBSITElinear.setVisibility(View.VISIBLE);
						AddWEBSITE.requestFocus();

					}

					else if (name.equals("Internet call")) {

						INTERNETCALLlinear.setVisibility(View.VISIBLE);
						AddINTERNETCALL.requestFocus();

					}
					pwindo.dismiss();
					newArray.remove(position);
					arrayAdapter.notifyDataSetChanged();
					if (newArray.isEmpty()) {
						btn.setVisibility(View.GONE);
					}

				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.contacts_editcontacttitlemenu,
				(com.actionbarsherlock.view.Menu) menu);
		return true;
	}

	public void addListenerOnButton() {

		imageButton = (ImageButton) findViewById(R.id.editnameimageButton1);

		imageButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				// Toast.makeText(AddContact.this, "ImageButton is clicked!",
				// Toast.LENGTH_SHORT).show();

				if (i == 0) {

					imageButton
							.setImageResource(R.drawable.contacts_ic_menu_expander_maximized_holo_light);

					AddName.setHint("Name Prefix");
					nameSelection = true;
					namepfx.setVisibility(View.VISIBLE);
					namepfx.requestFocus();

					middleName.setVisibility(View.VISIBLE);
					lastName.setVisibility(View.VISIBLE);

					nameSuffix.setVisibility(View.VISIBLE);

					i = 1;

				} else if (i == 1) {

					imageButton
							.setImageResource(R.drawable.contacts_ic_menu_expander_minimized_holo_light);

					AddName.setHint("Name");
					AddName.requestFocus();
					nameSelection = false;
					namepfx.setVisibility(View.GONE);

					middleName.setVisibility(View.GONE);
					lastName.setVisibility(View.GONE);

					nameSuffix.setVisibility(View.GONE);

					i = 0;
				}

			}

		});

		PhoneticNameImageButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				// Toast.makeText(AddContact.this,
				// "Add Phoneticname imageButton1 is clicked!",
				// Toast.LENGTH_SHORT).show();

				if (phonetici == 0) {

					/*
					 * namepfx.setVisibility(View.VISIBLE);
					 * 
					 * middleName.setVisibility(View.VISIBLE);
					 * lastName.setVisibility(View.VISIBLE);
					 * 
					 * nameSuffix.setVisibility(View.VISIBLE);
					 */

					PhoneticNameImageButton
							.setImageResource(R.drawable.contacts_ic_menu_expander_maximized_holo_light);

					AddPhoneticName.setHint("Phonetic family name");

					AddPhoneticMiddleName.setVisibility(View.VISIBLE);
					AddPhoneticLastName.setVisibility(View.VISIBLE);

					phonetici = 1;

				} else if (phonetici == 1) {

					/*
					 * namepfx.setVisibility(View.GONE);
					 * 
					 * middleName.setVisibility(View.GONE);
					 * lastName.setVisibility(View.GONE);
					 * 
					 * nameSuffix.setVisibility(View.GONE);
					 */

					PhoneticNameImageButton
							.setImageResource(R.drawable.contacts_ic_menu_expander_minimized_holo_light);

					AddPhoneticName.setHint("Phonetic name");

					AddPhoneticMiddleName.setVisibility(View.GONE);
					AddPhoneticLastName.setVisibility(View.GONE);

					phonetici = 0;
				}

			}

		});

		AddNewPhone.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				// Toast.makeText(AddContact.this, "AddNewPhone is clicked!",
				// Toast.LENGTH_SHORT).show();
				if (AddPhonelinear14.getVisibility() == View.VISIBLE && AddPhonelinear15.getVisibility() == View.GONE){
					AddPhonelinear15.setVisibility(View.VISIBLE);
					AddPhone15.setText("");
					AddPhone15.requestFocus();
				}
				if (AddPhonelinear13.getVisibility() == View.VISIBLE && AddPhonelinear14.getVisibility() == View.GONE){
					AddPhonelinear14.setVisibility(View.VISIBLE);
					AddPhone14.setText("");
					AddPhone14.requestFocus();
				}
				if (AddPhonelinear12.getVisibility() == View.VISIBLE && AddPhonelinear13.getVisibility() == View.GONE){
					AddPhonelinear13.setVisibility(View.VISIBLE);
					AddPhone13.setText("");
					AddPhone13.requestFocus();
				}
				if (AddPhonelinear11.getVisibility() == View.VISIBLE && AddPhonelinear12.getVisibility() == View.GONE){
					AddPhonelinear12.setVisibility(View.VISIBLE);
					AddPhone12.setText("");
					AddPhone12.requestFocus();
				}
				if (AddPhonelinear10.getVisibility() == View.VISIBLE && AddPhonelinear11.getVisibility() == View.GONE){
					AddPhonelinear11.setVisibility(View.VISIBLE);
					AddPhone11.setText("");
					AddPhone11.requestFocus();
				}
				if (AddPhonelinear9.getVisibility() == View.VISIBLE && AddPhonelinear10.getVisibility() == View.GONE){
					AddPhonelinear10.setVisibility(View.VISIBLE);
					AddPhone10.setText("");
					AddPhone10.requestFocus();
				}
				if (AddPhonelinear8.getVisibility() == View.VISIBLE && AddPhonelinear9.getVisibility() == View.GONE){
					AddPhonelinear9.setVisibility(View.VISIBLE);
					AddPhone9.setText("");
					AddPhone9.requestFocus();
				}
				if (AddPhonelinear7.getVisibility() == View.VISIBLE && AddPhonelinear8.getVisibility() == View.GONE){
					AddPhonelinear8.setVisibility(View.VISIBLE);
					AddPhone8.setText("");
					AddPhone8.requestFocus();
				}
				if (AddPhonelinear6.getVisibility() == View.VISIBLE && AddPhonelinear7.getVisibility() == View.GONE){
					AddPhonelinear7.setVisibility(View.VISIBLE);
					AddPhone7.setText("");
					AddPhone7.requestFocus();
				}
				if (AddPhonelinear5.getVisibility() == View.VISIBLE && AddPhonelinear6.getVisibility() == View.GONE){
					AddPhonelinear6.setVisibility(View.VISIBLE);
					AddPhone6.setText("");
					AddPhone6.requestFocus();
				}
				if (AddPhonelinear4.getVisibility() == View.VISIBLE && AddPhonelinear5.getVisibility() == View.GONE){
					AddPhonelinear5.setVisibility(View.VISIBLE);
					AddPhone5.setText("");
					AddPhone5.requestFocus();
				}
				if (AddPhonelinear3.getVisibility() == View.VISIBLE && AddPhonelinear4.getVisibility() == View.GONE){
					AddPhonelinear4.setVisibility(View.VISIBLE);
					AddPhone4.setText("");
					AddPhone4.requestFocus();
				}
				if (AddPhonelinear2.getVisibility() == View.VISIBLE && AddPhonelinear3.getVisibility() == View.GONE)

				{
					// Either gone or invisible

					AddPhonelinear3.setVisibility(View.VISIBLE);

					AddPhone3.setText("");

					AddPhone3.requestFocus();

				}
				
				if (AddPhonelinear2.getVisibility() == View.GONE  && AddPhonelinear3.getVisibility() == View.GONE 
						&& AddPhonelinear1.getVisibility() == View.VISIBLE )

				{
					// Either gone or invisible

					AddPhonelinear2.setVisibility(View.VISIBLE);

					AddPhone2.setText("");

					AddPhone2.requestFocus();

				}

				if (AddPhonelinear1.getVisibility() == View.GONE)

				{
					// Either gone or invisible

					AddPhonelinear1.setVisibility(View.VISIBLE);

					AddPhone.setText("");
					AddPhone.requestFocus();

				}

				/*
				 * else if (AddPhonelinear2.getVisibility() == View.VISIBLE) {
				 * 
				 * 
				 * AddPhonelinear2.setVisibility(View.GONE);
				 * 
				 * // Its visible }
				 */

			}

		});

		AddPhoneremove.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				// Toast.makeText(AddContact.this, "AddPhoneremove is clicked!",
				// Toast.LENGTH_SHORT).show();

				if (AddPhonelinear2.getVisibility() == View.VISIBLE)

				{
					// Either gone or invisible

					// AddPhonelinear2.setVisibility(View.VISIBLE);

					AddPhonelinear1.setVisibility(View.GONE);

				} else if (AddPhonelinear2.getVisibility() == View.GONE) {

					// AddPhonelinear2.setVisibility(View.GONE);

					AddPhone.setText("");

					// Its visible
				}

			}

		});

		AddPhoneremove2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				// Toast.makeText(AddContact.this,
				// "AddPhoneremove2 is clicked!", Toast.LENGTH_SHORT).show();

				if (AddPhonelinear1.getVisibility() == View.VISIBLE)

				{
					// Either gone or invisible

					// AddPhonelinear2.setVisibility(View.VISIBLE);

					AddPhonelinear2.setVisibility(View.GONE);

				} else if (AddPhonelinear1.getVisibility() == View.GONE) {

					// AddPhonelinear2.setVisibility(View.GONE);

					AddPhone2.setText("");
					// Its visible
				}
			}
		});
		
		AddPhoneremove3.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				// Toast.makeText(AddContact.this,
				// "AddPhoneremove2 is clicked!", Toast.LENGTH_SHORT).show();

				AddPhonelinear3.setVisibility(View.GONE);
				AddPhone3.setText("");
				/*if (AddPhonelinear2.getVisibility() == View.VISIBLE)

				{
					// Either gone or invisible

					// AddPhonelinear2.setVisibility(View.VISIBLE);

					AddPhonelinear3.setVisibility(View.GONE);

				} else if (AddPhonelinear2.getVisibility() == View.GONE) {

					// AddPhonelinear2.setVisibility(View.GONE);

					AddPhone3.setText("");
					// Its visible
				}*/
			}
		});
		
		AddPhoneremove4.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
					AddPhonelinear4.setVisibility(View.GONE);
					AddPhone4.setText("");
				}
		});
		AddPhoneremove5.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
					AddPhonelinear5.setVisibility(View.GONE);
					AddPhone5.setText("");
				}
		});AddPhoneremove6.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
					AddPhonelinear6.setVisibility(View.GONE);
					AddPhone6.setText("");
				}
		});AddPhoneremove7.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
					AddPhonelinear7.setVisibility(View.GONE);
					AddPhone7.setText("");
				}
		});AddPhoneremove8.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
					AddPhonelinear8.setVisibility(View.GONE);
					AddPhone8.setText("");
				}
		});AddPhoneremove9.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
					AddPhonelinear9.setVisibility(View.GONE);
					AddPhone9.setText("");
				}
		});AddPhoneremove10.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
					AddPhonelinear10.setVisibility(View.GONE);
					AddPhone10.setText("");
				}
		});AddPhoneremove11.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
					AddPhonelinear11.setVisibility(View.GONE);
					AddPhone11.setText("");
				}
		});AddPhoneremove12.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
					AddPhonelinear12.setVisibility(View.GONE);
					AddPhone12.setText("");
				}
		});AddPhoneremove13.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
					AddPhonelinear13.setVisibility(View.GONE);
					AddPhone13.setText("");
				}
		});AddPhoneremove14.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
					AddPhonelinear14.setVisibility(View.GONE);
					AddPhone14.setText("");
				}
		});AddPhoneremove15.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
					AddPhonelinear15.setVisibility(View.GONE);
					AddPhone14.setText("");
				}
		});
		


		AddNewEmail.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				if (AddEmaillinear5.getVisibility() == View.VISIBLE && AddEmaillinear6.getVisibility() == View.GONE){
					AddEmaillinear6.setVisibility(View.VISIBLE);
					AddEmail6.setText("");
					AddEmail6.requestFocus();
				}
				if (AddEmaillinear4.getVisibility() == View.VISIBLE && AddEmaillinear5.getVisibility() == View.GONE){
					AddEmaillinear5.setVisibility(View.VISIBLE);
					AddEmail5.setText("");
					AddEmail5.requestFocus();
				}
				if (AddEmaillinear3.getVisibility() == View.VISIBLE && AddEmaillinear4.getVisibility() == View.GONE){
					AddEmaillinear4.setVisibility(View.VISIBLE);
					AddEmail4.setText("");
					AddEmail4.requestFocus();
				}
				if (AddEmaillinear2.getVisibility() == View.VISIBLE && AddEmaillinear3.getVisibility() == View.GONE){
					AddEmaillinear3.setVisibility(View.VISIBLE);
					AddEmail3.setText("");
					AddEmail3.requestFocus();
				}
				if (AddEmaillinear2.getVisibility() == View.GONE  && AddEmaillinear3.getVisibility() == View.GONE 
						&& AddEmaillinear.getVisibility() == View.VISIBLE ){
					AddEmaillinear2.setVisibility(View.VISIBLE);
					AddEmail2.setText("");
					AddEmail2.requestFocus();
				}

				if (AddEmaillinear.getVisibility() == View.GONE){
					AddEmaillinear.setVisibility(View.VISIBLE);
					AddEmail.setText("");
					AddPhone.requestFocus();
				}
			}
		});
		
		AddEmailremove.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				/*	if (AddEmaillinear2.getVisibility() == View.VISIBLE){
				AddEmaillinear.setVisibility(View.GONE);
			} else if (AddEmaillinear2.getVisibility() == View.GONE) {
				AddEmail.setText("");
			}*/
			
				
			if (AddEmaillinear2.getVisibility() == View.VISIBLE){
				AddEmaillinear.setVisibility(View.GONE);
				AddEmail.setText("");
			} else if (AddEmaillinear2.getVisibility() == View.GONE) {
				AddEmail.setText("");
			}
			
			}

		});

		AddEmailremove2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				/*	if (AddEmaillinear.getVisibility() == View.VISIBLE){
				AddEmaillinear2.setVisibility(View.GONE);
			} else if (AddEmaillinear.getVisibility() == View.GONE) {
				AddEmail2.setText("");
			}*/
			
			
	
			
			
			if (AddEmaillinear.getVisibility() == View.VISIBLE){
				AddEmaillinear2.setVisibility(View.GONE);
				AddEmail2.setText("");
			} else if (AddEmaillinear.getVisibility() == View.GONE) {
				AddEmail2.setText("");
			}
			
				
			}
		});
		
		AddEmailremove3.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				AddEmaillinear3.setVisibility(View.GONE);
				AddEmail3.setText("");
			}
		});
		
		AddEmailremove4.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
					AddEmaillinear4.setVisibility(View.GONE);
					AddEmail4.setText("");
				}
		});
		AddEmailremove5.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
					AddEmaillinear5.setVisibility(View.GONE);
					AddEmail5.setText("");
				}
		});AddEmailremove6.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
					AddEmaillinear6.setVisibility(View.GONE);
					AddEmail6.setText("");
				}
		});

		
		//IM AddNew and Remove fields
		AddNewIM.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				if (AddIMlinear5.getVisibility() == View.VISIBLE && AddIMlinear6.getVisibility() == View.GONE){
					AddIMlinear6.setVisibility(View.VISIBLE);
					AddIM6.setText("");
					AddIM6.requestFocus();
				}
				if (AddIMlinear4.getVisibility() == View.VISIBLE && AddIMlinear5.getVisibility() == View.GONE){
					AddIMlinear5.setVisibility(View.VISIBLE);
					AddIM5.setText("");
					AddIM5.requestFocus();
				}
				if (AddIMlinear3.getVisibility() == View.VISIBLE && AddIMlinear4.getVisibility() == View.GONE){
					AddIMlinear4.setVisibility(View.VISIBLE);
					AddIM4.setText("");
					AddIM4.requestFocus();
				}
				if (AddIMlinear2.getVisibility() == View.VISIBLE && AddIMlinear3.getVisibility() == View.GONE){
					AddIMlinear3.setVisibility(View.VISIBLE);
					AddIM3.setText("");
					AddIM3.requestFocus();
				}
				if (AddIMlinear2.getVisibility() == View.GONE  && AddIMlinear3.getVisibility() == View.GONE 
						&& AddIMlinear.getVisibility() == View.VISIBLE ){
					AddIMlinear2.setVisibility(View.VISIBLE);
					AddIM2.setText("");
					AddIM2.requestFocus();
				}

				if (AddIMlinear.getVisibility() == View.GONE){
					AddIMlinear.setVisibility(View.VISIBLE);
					AddIM.setText("");
					AddPhone.requestFocus();
				}
			}
		});
		
		AddIMremove.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (AddIMlinear2.getVisibility() == View.VISIBLE){
					AddIMlinear.setVisibility(View.GONE);
					AddIM.setText("");
				} else if (AddIMlinear2.getVisibility() == View.GONE) {
					AddIM.setText("");
				}
			}

		});

		AddIMremove2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (AddIMlinear.getVisibility() == View.VISIBLE){
					AddIMlinear2.setVisibility(View.GONE);
					AddIM2.setText("");
				} else if (AddIMlinear.getVisibility() == View.GONE) {
					AddIM2.setText("");
				}
			}
		});
		
		AddIMremove3.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				AddIMlinear3.setVisibility(View.GONE);
				AddIM3.setText("");
			}
		});
		
		AddIMremove4.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
					AddIMlinear4.setVisibility(View.GONE);
					AddIM4.setText("");
				}
		});
		AddIMremove5.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
					AddIMlinear5.setVisibility(View.GONE);
					AddIM5.setText("");
				}
		});AddIMremove6.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
					AddIMlinear6.setVisibility(View.GONE);
					AddIM6.setText("");
				}
		});
		
		// Address AddNew and Remove fields
		AddNewAddress.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				

				if (AddAddresslinear2.getVisibility() == View.GONE
						&& AddAddresslinear.getVisibility() == View.VISIBLE
						&& AddAddresslinear3.getVisibility() == View.VISIBLE) {
					AddAddresslinear2.setVisibility(View.VISIBLE);
					AddStreet2.setText("");
					AddStreet2.requestFocus();
					AddCity2.setText("");
					AddState2.setText("");
					AddPostalcode2.setText("");
					AddCountry2.setText("");
				}
				
				
				if (AddAddresslinear2.getVisibility() == View.VISIBLE
						&& AddAddresslinear3.getVisibility() == View.GONE) {
					AddAddresslinear3.setVisibility(View.VISIBLE);
					AddStreet3.setText("");
					AddStreet3.requestFocus();
					AddCity3.setText("");
					AddState3.setText("");
					AddPostalcode3.setText("");
					AddCountry3.setText("");
				}
				if (AddAddresslinear2.getVisibility() == View.GONE
						&& AddAddresslinear3.getVisibility() == View.GONE
						&& AddAddresslinear.getVisibility() == View.VISIBLE) {
					AddAddresslinear2.setVisibility(View.VISIBLE);
					AddStreet2.setText("");
					AddStreet2.requestFocus();
					AddCity2.setText("");
					AddState2.setText("");
					AddPostalcode2.setText("");
					AddCountry2.setText("");
					
				}

				if (AddAddresslinear.getVisibility() == View.GONE) {
					AddAddresslinear.setVisibility(View.VISIBLE);
					AddStreet.setText("");
					AddStreet.requestFocus();
					AddCity.setText("");
					AddState.setText("");
					AddPostalcode.setText("");
					AddCountry.setText("");
				}
			}
		});

		AddAddressremove.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {

				if (AddAddresslinear2.getVisibility() == View.VISIBLE) {
					AddAddresslinear.setVisibility(View.GONE);
					AddStreet.setText("");
					AddCity.setText("");
					AddState.setText("");
					AddPostalcode.setText("");
					AddCountry.setText("");
				} else if (AddAddresslinear2.getVisibility() == View.GONE) {
					AddStreet.setText("");
					AddCity.setText("");
					AddState.setText("");
					AddPostalcode.setText("");
					AddCountry.setText("");
				}
			}

		});

		AddAddressremove2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (AddAddresslinear.getVisibility() == View.VISIBLE) {
					AddAddresslinear2.setVisibility(View.GONE);
					AddStreet2.setText("");
					AddCity2.setText("");
					AddState2.setText("");
					AddPostalcode2.setText("");
					AddCountry2.setText("");
				} else if (AddAddresslinear.getVisibility() == View.GONE) {
					//AddAddresslinear2.setVisibility(View.GONE);
					AddStreet2.setText("");
					AddCity2.setText("");
					AddState2.setText("");
					AddPostalcode2.setText("");
					AddCountry2.setText("");
				}else if(AddAddresslinear.getVisibility() == View.GONE &&
						AddAddresslinear3.getVisibility() == View.GONE){
					AddAddresslinear2.setVisibility(View.VISIBLE);
					AddStreet2.setText("");
					AddCity2.setText("");
					AddState2.setText("");
					AddPostalcode2.setText("");
					AddCountry2.setText("");
				}

			}
		});

		AddAddressremove3.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				AddAddresslinear3.setVisibility(View.GONE);
				AddStreet3.setText("");
				AddCity3.setText("");
				AddState3.setText("");
				AddPostalcode3.setText("");
				AddCountry3.setText("");
				AddAddresslinear.setVisibility(View.VISIBLE);
			}
		});
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:

			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
			
			String nameVal;
			try{
				if (nameSelection) {
					nameVal = namepfx.getText().toString();
				} else {
					nameVal = AddName.getText().toString();
				}
	
				String checklastname = lastName.getText().toString();
	
				if ((nameVal != null && !nameVal.isEmpty())
						|| (checklastname != null && !checklastname.isEmpty()))
	
				{
	
					ContentValues cv = prepareContentValues();
	
					Uri insertDataUri = getContentResolver().insert(
							ContactsConsts.CONTENT_URI_CONTACTS, cv);
	
					/*System.out.println("******CLIENT_ID*****"
							+ cv.getAsString(ContactsConsts.CONTACT_CLIENT_ID));*/
	
					ContentValues editDBValue = new ContentValues();
					Cursor cursor = getContentResolver().query(insertDataUri, null,
							null, null, null);
					cursor.moveToFirst();
	
					cursor.close();
	
					String clientId = cv
							.getAsString(ContactsConsts.CONTACT_CLIENT_ID);
	
					editDBValue.put(ContactsConsts.CONTACT_CLIENT_ID, clientId);
					getContentResolver().insert(
							ContactsConsts.CONTENT_URI_CONTACTS_LOCAL_CACHE_ADD,
							editDBValue);
	
					syncWithServer(clientId);
					
					SharedPreferences.Editor prefEditor = new SharedPreferences(Email.getAppContext()).edit();  
					prefEditor.putBoolean("isPrefUpdated", true);  
					prefEditor.commit();
					finish();
				} else {
					Toast.makeText(this,
							getResources().getString(R.string.contact_name_alert),
							Toast.LENGTH_SHORT).show();
				}
			}catch(Exception exe){
			}
			break;
		default:
			break;
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		try {

			ImageView image = (ImageView) findViewById(R.id.AddImage);

			if (requestCode == 1337 && resultCode == RESULT_OK && data != null) {
				// data.getExtras()
				thumbnail = (Bitmap) data.getExtras().get("data");
				// ImageView image =(ImageView) findViewById(R.id.AddImage);

				if (thumbnail != null) {
					byte[] imageData = ContactsUtilities.getBytes(thumbnail);
					// 1048576
					ContactsLog.i("ContactsAddContact", "imageData **"
							+ imageData.length);
					//&& imageData.length <= 25600
					if (imageData != null ) {

						image.setImageBitmap(thumbnail);
					} 
					/*else {
						thumbnail = null;
						Toast.makeText(
								ContactsAddContact.this,
								"image is too large please choose a smaller one",
								Toast.LENGTH_SHORT).show();
					}*/
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
					// 1048576 //49152
					ContactsLog.i("ContactsAddContact", "imageData **"
							+ imageData.length);
					
					// && imageData.length <= 25600
					if (imageData != null) {

						image.setImageBitmap(thumbnail);
					} 
					
					/*else {
						thumbnail = null;
						Toast.makeText(
								ContactsAddContact.this,
								"image is too large please choose a smaller one",
								Toast.LENGTH_SHORT).show();
					}*/
				}

			}
		} catch (Exception e) {
			ContactsLog.e("Exception", "Exception **" + e.toString());
		}
		super.onActivityResult(requestCode, resultCode, data);
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

	ContentValues prepareContentValues() {
		ContentValues cv = new ContentValues();
		if (((EditText) findViewById(R.id.AddName)).getText() != null
				|| ((EditText) findViewById(R.id.AddNamePrefix)).getText() != null) {

			if (nameSelection) {
				// Toast.makeText(getBaseContext(), "" +
				// namepfx.getText(),Toast.LENGTH_SHORT).show();
				cv.put(ContactsConsts.CONTACT_FIRST_NAME,
						((EditText) findViewById(R.id.AddNamePrefix)).getText()
								.toString());
				// cv.put(ContactsConsts.CONTACT_NAME_PREFIX,
				// ((EditText) findViewById(R.id.AddName)).getText()
				// .toString());

			} else {
				// Toast.makeText(getBaseContext(), "" + AddName.getText(),
				// Toast.LENGTH_SHORT).show();
				cv.put(ContactsConsts.CONTACT_FIRST_NAME,
						((EditText) findViewById(R.id.AddName)).getText()
								.toString());
			}
		}

		if (((EditText) findViewById(R.id.AddName)).getText() != null) {
			cv.put(ContactsConsts.CONTACT_TITLE,
					((EditText) findViewById(R.id.AddName)).getText()
							.toString());
		}
		Log.i("prefix", ((EditText) findViewById(R.id.AddName)).getText()
				.toString());

		if (((EditText) findViewById(R.id.AddMiddleName)).getText() != null) {
			cv.put(ContactsConsts.CONTACT_MIDDLE_NAME,
					((EditText) findViewById(R.id.AddMiddleName)).getText()
							.toString());
		}

		if (((EditText) findViewById(R.id.AddLastName)).getText() != null) {
			cv.put(ContactsConsts.CONTACT_LAST_NAME,
					((EditText) findViewById(R.id.AddLastName)).getText()
							.toString());
		}

		if (((EditText) findViewById(R.id.AddNameSuffix)).getText() != null) {
			cv.put(ContactsConsts.CONTACT_SUFFIX,
					((EditText) findViewById(R.id.AddNameSuffix)).getText()
							.toString());
		}

		String phoneNumbers = "";

		String mobileNo = ((EditText) findViewById(R.id.AddPhone)).getText()
				.toString();
		/*System.out.println("mobiles: " + mobileNo + " ======  "
				+ phoneSpinner1.getSelectedItem());*/
		if (mobileNo != null && !mobileNo.isEmpty()) {
			phoneNumbers += phoneSpinner1.getSelectedItem() + "=" + mobileNo
					+ ":";

		}

		// if ((phoneSpinner1.getSelectedItem()) != null) {
		// cv.put(ContactsConsts.CONTACT_PH_NO_MOBILE_TYPE1, ""
		// + phoneSpinner1.getSelectedItem());
		// }

		String business = ((EditText) findViewById(R.id.AddPhone2)).getText()
				.toString();
		/*System.out.println("business: " + business + " ======  "
				+ phoneSpinner2.getSelectedItem());*/
		if (business != null && !business.isEmpty()) {
			phoneNumbers += phoneSpinner2.getSelectedItem() + "=" + business + ":";
		}
		
		String home_telephone = ((EditText) findViewById(R.id.AddPhone3)).getText().toString();
		//System.out.println("home_telephone: " + home_telephone + " ======  " + phoneSpinner3.getSelectedItem());
		if (home_telephone != null && !home_telephone.isEmpty()) {
			phoneNumbers += phoneSpinner3.getSelectedItem() + "=" + home_telephone + ":";
		}
		String home2_telephone_number = ((EditText) findViewById(R.id.AddPhone4)).getText().toString();
		//System.out.println("home_telephone: " + home2_telephone_number + " ======  " + phoneSpinner4.getSelectedItem());
		if (home2_telephone_number != null && !home2_telephone_number.isEmpty()) {
			phoneNumbers += phoneSpinner4.getSelectedItem() + "=" + home2_telephone_number + ":";
		}
		String home_fax_number = ((EditText) findViewById(R.id.AddPhone5)).getText().toString();
		//System.out.println("home_fax_number: " + home_fax_number + " ======  " + phoneSpinner5.getSelectedItem());
		if (home_fax_number != null && !home_fax_number.isEmpty()) {
			phoneNumbers += phoneSpinner5.getSelectedItem() + "=" + home_fax_number + ":";
		}
		String business2_telephone_number = ((EditText) findViewById(R.id.AddPhone6)).getText().toString();
		//System.out.println("business2_telephone_number: " + business2_telephone_number + " ======  " + phoneSpinner6.getSelectedItem());
		if (business2_telephone_number != null && !business2_telephone_number.isEmpty()) {
			phoneNumbers += phoneSpinner6.getSelectedItem() + "=" + business2_telephone_number + ":";
		}
		String business_fax_number = ((EditText) findViewById(R.id.AddPhone7)).getText().toString();
		//System.out.println("business_fax_number: " + business_fax_number + " ======  " + phoneSpinner7.getSelectedItem());
		if (business_fax_number != null && !business_fax_number.isEmpty()) {
			phoneNumbers += phoneSpinner7.getSelectedItem() + "=" + business_fax_number + ":";
		}
		
		String assistant_telephone_number = ((EditText) findViewById(R.id.AddPhone8)).getText().toString();
		//System.out.println("assistant_telephone_number: " + assistant_telephone_number + " ======  " + phoneSpinner8.getSelectedItem());
		if (assistant_telephone_number != null && !assistant_telephone_number.isEmpty()) {
			phoneNumbers += phoneSpinner8.getSelectedItem() + "=" + assistant_telephone_number + ":";
		}
		
		String car_telephone_number = ((EditText) findViewById(R.id.AddPhone9)).getText().toString();
		//System.out.println("car_telephone_number: " + car_telephone_number + " ======  " + phoneSpinner9.getSelectedItem());
		if (car_telephone_number != null && !car_telephone_number.isEmpty()) {
			phoneNumbers += phoneSpinner9.getSelectedItem() + "=" + car_telephone_number + ":";
		}
		
		String pager_number = ((EditText) findViewById(R.id.AddPhone10)).getText().toString();
		//System.out.println("pager_number: " + pager_number + " ======  " + phoneSpinner10.getSelectedItem());
		if (pager_number != null && !pager_number.isEmpty()) {
			phoneNumbers += phoneSpinner10.getSelectedItem() + "=" + pager_number + ":";
		}
		
		String radio_telephone_number = ((EditText) findViewById(R.id.AddPhone11)).getText().toString();
		//System.out.println("radio_telephone_number: " + radio_telephone_number + " ======  " + phoneSpinner11.getSelectedItem());
		if (radio_telephone_number != null && !radio_telephone_number.isEmpty()) {
			phoneNumbers += phoneSpinner10.getSelectedItem() + "=" + radio_telephone_number + ":";
		}
		
		String company_main_phone = ((EditText) findViewById(R.id.AddPhone12)).getText().toString();
		//System.out.println("company_main_phone: " + company_main_phone + " ======  " + phoneSpinner12.getSelectedItem());
		if (company_main_phone != null && !company_main_phone.isEmpty()) {
			phoneNumbers += phoneSpinner12.getSelectedItem() + "=" + company_main_phone + ":";
		}
		String custom_number1 = ((EditText) findViewById(R.id.AddPhone13)).getText().toString();
		//System.out.println("custom_number1: " + custom_number1 + " ======  " + phoneSpinner13.getSelectedItem());
		if (custom_number1 != null && !custom_number1.isEmpty()) {
			phoneNumbers += phoneSpinner13.getSelectedItem() + "=" + custom_number1 + ":";
		}
		
		String custom_number2 = ((EditText) findViewById(R.id.AddPhone14)).getText().toString();
		//System.out.println("custom_number2: " + custom_number2 + " ======  " + phoneSpinner14.getSelectedItem());
		if (custom_number2 != null && !custom_number2.isEmpty()) {
			phoneNumbers += phoneSpinner14.getSelectedItem() + "=" + custom_number2 + ":";
		}
		String custom_number3 = ((EditText) findViewById(R.id.AddPhone15)).getText().toString();
		//System.out.println("custom_number3: " + custom_number3 + " ======  " + phoneSpinner15.getSelectedItem());
		if (custom_number3 != null && !custom_number3.isEmpty()) {
			phoneNumbers += phoneSpinner15.getSelectedItem() + "=" + custom_number3 + ":";
		}
		

		// if ((phoneSpinner2.getSelectedItem()) != null) {
		// cv.put(ContactsConsts.CONTACT_PH_NO_MOBILE_TYPE2, ""
		// + phoneSpinner2.getSelectedItem());
		// }

		if (phoneNumbers != null) {

			cv.put(ContactsConsts.CONTACT_PHONE, phoneNumbers);
		}

		
		String emailAddress = "";

		String Email_Adress1 = ((EditText) findViewById(R.id.AddEmail)).getText().toString();
		//System.out.println("Email_Adress1: " + Email_Adress1 + " ======  " + emailSpinner.getSelectedItem());
		if (Email_Adress1 != null && !Email_Adress1.isEmpty()) {
			emailAddress += emailSpinner.getSelectedItem() + "=" + Email_Adress1 + ":";
		}
		String Email_Adress2 = ((EditText) findViewById(R.id.AddEmail2)).getText().toString();
		//System.out.println("Email_Adress2: " + Email_Adress2 + " ======  " + emailSpinner2.getSelectedItem());
		if (Email_Adress2 != null && !Email_Adress2.isEmpty()) {
			emailAddress += emailSpinner2.getSelectedItem() + "=" + Email_Adress2 + ":";
		}
		String Email_Adress3 = ((EditText) findViewById(R.id.AddEmail3)).getText().toString();
		//System.out.println("Email_Adress3: " + Email_Adress3 + " ======  " + emailSpinner3.getSelectedItem());
		if (Email_Adress3 != null && !Email_Adress3.isEmpty()) {
			emailAddress += emailSpinner3.getSelectedItem() + "=" + Email_Adress3 + ":";
		}
		String Email_custom1 = ((EditText) findViewById(R.id.AddEmail4)).getText().toString();
		//System.out.println("Email_custom1: " + Email_custom1 + " ======  " + emailSpinner4.getSelectedItem());
		if (Email_custom1 != null && !Email_custom1.isEmpty()) {
			emailAddress += emailSpinner4.getSelectedItem() + "=" + Email_custom1 + ":";
		}
		String Email_custom2 = ((EditText) findViewById(R.id.AddEmail5)).getText().toString();
		//System.out.println("Email_custom2: " + Email_custom2 + " ======  " + emailSpinner5.getSelectedItem());
		if (Email_custom2 != null && !Email_custom2.isEmpty()) {
			emailAddress += emailSpinner5.getSelectedItem() + "=" + Email_custom2 + ":";
		}
		String Email_custom3 = ((EditText) findViewById(R.id.AddEmail6)).getText().toString();
		//System.out.println("Email_custom3: " + Email_custom3 + " ======  " + emailSpinner6.getSelectedItem());
		if (Email_custom3 != null && !Email_custom3.isEmpty()) {
			emailAddress += emailSpinner6.getSelectedItem() + "=" + Email_custom3 + ":";
		}
		
		if (emailAddress != null) {
			cv.put(ContactsConsts.CONTACT_EMAIL, emailAddress);
		}
		
		
		/*if (((EditText) findViewById(R.id.AddEmail)).getText() != null && !((EditText) findViewById(R.id.AddEmail)).getText().toString().isEmpty()) {

			String email = "Email_Adress" + "=" + ((EditText) findViewById(R.id.AddEmail)).getText().toString() + ":";
			cv.put(ContactsConsts.CONTACT_EMAIL, email);

		}*/

		// if ((emailSpinner.getSelectedItem()) != null) {
		// cv.put(ContactsConsts.CONTACT_EMAIL_TYPE,
		// "" + emailSpinner.getSelectedItem());
		// }
		//
		// if ((imSpinner.getSelectedItem()) != null) {
		// cv.put(ContactsConsts.CONTACT_IM_TYPE,
		// "" + imSpinner.getSelectedItem());
		// }

		/*
		 * if (((EditText) findViewById(R.id.AddIM)).getText().toString() !=
		 * null) { System.out.println("Add Contact: " + ((EditText)
		 * findViewById(R.id.AddIM)).getText().toString());
		 * cv.put(ContactsConsts.CONTACT_IM_NAME, ((EditText)
		 * findViewById(R.id.AddIM)).getText().toString()); }
		 */

		
		
		
		
		
		
		String imAddress = "";

		String IM_Adress1 = ((EditText) findViewById(R.id.AddIM)).getText().toString();
		//System.out.println("IM_Adress1: " + IM_Adress1 + " ======  " + IMSpinner.getSelectedItem());
		if (IM_Adress1 != null && !IM_Adress1.isEmpty()) {
			imAddress += IMSpinner.getSelectedItem() + "=" + IM_Adress1 + ":";
		}
		String IM_Adress2 = ((EditText) findViewById(R.id.AddIM2)).getText().toString();
		//System.out.println("IM_Adress2: " + IM_Adress2 + " ======  " + IMSpinner2.getSelectedItem());
		if (IM_Adress2 != null && !IM_Adress2.isEmpty()) {
			imAddress += IMSpinner2.getSelectedItem() + "=" + IM_Adress2 + ":";
		}
		String IM_Adress3 = ((EditText) findViewById(R.id.AddIM3)).getText().toString();
		//System.out.println("IM_Adress3: " + IM_Adress3 + " ======  " + IMSpinner3.getSelectedItem());
		if (IM_Adress3 != null && !IM_Adress3.isEmpty()) {
			imAddress += IMSpinner3.getSelectedItem() + "=" + IM_Adress3 + ":";
		}
		String IM_custom1 = ((EditText) findViewById(R.id.AddIM4)).getText().toString();
		//System.out.println("IM_custom1: " + IM_custom1 + " ======  " + IMSpinner4.getSelectedItem());
		if (IM_custom1 != null && !IM_custom1.isEmpty()) {
			imAddress += IMSpinner4.getSelectedItem() + "=" + IM_custom1 + ":";
		}
		String IM_custom2 = ((EditText) findViewById(R.id.AddIM5)).getText().toString();
		//System.out.println("IM_custom2: " + IM_custom2 + " ======  " + IMSpinner5.getSelectedItem());
		if (IM_custom2 != null && !IM_custom2.isEmpty()) {
			imAddress += IMSpinner5.getSelectedItem() + "=" + IM_custom2 + ":";
		}
		String IM_custom3 = ((EditText) findViewById(R.id.AddIM6)).getText().toString();
		//System.out.println("IM_custom3: " + IM_custom3 + " ======  " + IMSpinner6.getSelectedItem());
		if (IM_custom3 != null && !IM_custom3.isEmpty()) {
			imAddress += IMSpinner6.getSelectedItem() + "=" + IM_custom3 + ":";
		}
		
		if (imAddress != null) {
			cv.put(ContactsConsts.CONTACT_IM_NAME, imAddress);
		}
		
		
		
		/*
		if (((EditText) findViewById(R.id.AddIM)).getText() != null && !((EditText) findViewById(R.id.AddIM)).getText().toString().isEmpty()) {

			// String Addrs = "im" + "=" + ((EditText)
			// findViewById(R.id.AddIM)).getText().toString() + ":" +
			// imSpinner.getSelectedItem();
			cv.put(ContactsConsts.CONTACT_IM_NAME, ((EditText) findViewById(R.id.AddIM)).getText().toString());

		}*/

		
		String bussSpinnerVal= null , homeSpinnerVal = null, otherSpinnerVal= null;
		
		if(AddAddresslinear.getVisibility() == View.VISIBLE){
			bussSpinnerVal = addressSpinner.getSelectedItem().toString();
		}
		if(AddAddresslinear2.getVisibility() == View.VISIBLE){
			 homeSpinnerVal = addressSpinner2.getSelectedItem().toString();
		}
		
		if(AddAddresslinear3.getVisibility() == View.VISIBLE){
			otherSpinnerVal = addressSpinner3.getSelectedItem().toString();
		}
		
		ContentValues adress1 = new ContentValues();
		ContentValues address2 = new ContentValues();
		ContentValues address3 = new ContentValues();
		
			String AddStreet_val = AddStreet.getText().toString();
			if (AddStreet_val != null && !AddStreet_val.isEmpty()) {
				adress1.put(ContactsConsts.STREET, AddStreet_val);
			}
			String AddCity_Val = AddCity.getText().toString();
			if (AddCity_Val != null && !AddCity_Val.isEmpty()) {
				adress1.put(ContactsConsts.CITY, AddCity_Val);
			}
			String AddState_val = AddState.getText().toString();
			if (AddState_val != null && !AddState_val.isEmpty()) {
				adress1.put(ContactsConsts.STATE, AddState_val);
			}
			String AddPostalcode_val = AddPostalcode.getText().toString();
			if (AddPostalcode_val != null && !AddPostalcode_val.isEmpty()) {
				adress1.put(ContactsConsts.ZIP, AddPostalcode_val);
			}
			String AddCountry_val = AddCountry.getText().toString();
			if (AddCountry_val != null && !AddCountry_val.isEmpty()) {
				adress1.put(ContactsConsts.COUNTRY, AddCountry_val);
			}

			String AddStreet2_val = AddStreet2.getText().toString();
			if (AddStreet2_val != null && !AddStreet2_val.isEmpty()) {
				address2.put(ContactsConsts.STREET, AddStreet2_val);
			}
			String AddCity2_Val = AddCity2.getText().toString();
			if (AddCity2_Val != null && !AddCity2_Val.isEmpty()) {
				address2.put(ContactsConsts.CITY, AddCity2_Val);
			}
			String AddState2_val = AddState2.getText().toString();
			if (AddState2_val != null && !AddState2_val.isEmpty()) {
				address2.put(ContactsConsts.STATE, AddState2_val);
			}
			String AddPostalcode2_val = AddPostalcode2.getText().toString();
			if (AddPostalcode2_val != null && !AddPostalcode2_val.isEmpty()) {
				address2.put(ContactsConsts.ZIP, AddPostalcode2_val);
			}
			String AddCountry2_val = AddCountry2.getText().toString();
			if (AddCountry2_val != null && !AddCountry2_val.isEmpty()) {
				address2.put(ContactsConsts.COUNTRY, AddCountry2_val);
			}

			String AddStreet3_val = AddStreet3.getText().toString();
			if (AddStreet3_val != null && !AddStreet3_val.isEmpty()) {
				address3.put(ContactsConsts.STREET, AddStreet3_val);
			}
			String AddCity3_Val = AddCity3.getText().toString();
			if (AddCity3_Val != null && !AddCity3_Val.isEmpty()) {
				address3.put(ContactsConsts.CITY, AddCity3_Val);
			}
			String AddState3_val = AddState3.getText().toString();
			if (AddState3_val != null && !AddState3_val.isEmpty()) {
				address3.put(ContactsConsts.STATE, AddState3_val);
			}
			String AddPostalcode3_val = AddPostalcode3.getText().toString();
			if (AddPostalcode3_val != null && !AddPostalcode3_val.isEmpty()) {
				address3.put(ContactsConsts.ZIP, AddPostalcode_val);
			}
			String AddCountry3_val = AddCountry3.getText().toString();
			if (AddCountry3_val != null && !AddCountry3_val.isEmpty()) {
				address3.put(ContactsConsts.COUNTRY, AddCountry3_val);
			}
			String clientId = "" + System.currentTimeMillis();

			if(bussSpinnerVal !=null ){
				if(bussSpinnerVal.equalsIgnoreCase("BUSINESS")){
					cv.put(ContactsConsts.CONTACT_BUSS_ADRESS, 1);
					addaddressValue(adress1 ,"W" , clientId);
					
				}else if(bussSpinnerVal.equalsIgnoreCase("HOME")){
					cv.put(ContactsConsts.CONTACT_HOME_ADRESS, 1);
					addaddressValue(adress1 ,"H" , clientId);
					
					
				}else if(bussSpinnerVal.equalsIgnoreCase("OTHER")){
					cv.put(ContactsConsts.CONTACT_OTHER_ADRESS, 1);
					addaddressValue(adress1 ,"O", clientId);
			
				}
			}
			
			if(homeSpinnerVal != null){
				if(homeSpinnerVal.equalsIgnoreCase("BUSINESS")){
					cv.put(ContactsConsts.CONTACT_BUSS_ADRESS, 1);
					addaddressValue(address2 ,"W", clientId);
					
				}else if(homeSpinnerVal.equalsIgnoreCase("HOME")){
					cv.put(ContactsConsts.CONTACT_HOME_ADRESS, 1);
					addaddressValue(address2 ,"H", clientId);
					
				}else if(homeSpinnerVal.equalsIgnoreCase("OTHER")){
					cv.put(ContactsConsts.CONTACT_OTHER_ADRESS, 1);
					addaddressValue(address2 ,"O", clientId);
					
				}
				
			}
			
			if(otherSpinnerVal != null){
				if(otherSpinnerVal.equalsIgnoreCase("BUSINESS")){
					cv.put(ContactsConsts.CONTACT_BUSS_ADRESS, 1);
					addaddressValue(address3 ,"W", clientId);
					
				}else if(otherSpinnerVal.equalsIgnoreCase("HOME")){
					cv.put(ContactsConsts.CONTACT_HOME_ADRESS, 1);
					addaddressValue(address3 ,"H", clientId);
					
					
				}else if(otherSpinnerVal.equalsIgnoreCase("OTHER")){
					cv.put(ContactsConsts.CONTACT_OTHER_ADRESS, 1);
					addaddressValue(address3 ,"O", clientId);
				
				}
			}
		
		
		/*	if(adress1.size() == 0){
				cv.put(ContactsConsts.CONTACT_BUSS_ADRESS, 0);
				System.out.println("CONTACT_BUSS_ADRESS :  "+ "0");
			}
			if( address2.size() == 0){
				cv.put(ContactsConsts.CONTACT_HOME_ADRESS, 0);
				System.out.println("CONTACT_HOME_ADRESS :  "+ "0");
			}
			if(address3.size()== 0){
				cv.put(ContactsConsts.CONTACT_OTHER_ADRESS, 0);
				System.out.println("CONTACT_OTHER_ADRESS :  "+ "0");
			}*/




		if (((EditText) findViewById(R.id.AddNotes)).getText() != null) {
			/*System.out.println("Add Contact: "
					+ ((EditText) findViewById(R.id.AddNotes)).getText()
							.toString());*/
			cv.put(ContactsConsts.CONTACT_NOTES,
					((EditText) findViewById(R.id.AddNotes)).getText()
							.toString());
		}

		if (((EditText) findViewById(R.id.AddNICKNAME)).getText() != null) {
			/*System.out.println("Add Contact: "
					+ ((EditText) findViewById(R.id.AddNICKNAME)).getText()
							.toString());*/
			cv.put(ContactsConsts.CONTACT_NICK_NAME,
					((EditText) findViewById(R.id.AddNICKNAME)).getText()
							.toString());
		}

		if (((EditText) findViewById(R.id.AddWEBSITE)).getText() != null) {
			/*System.out.println("Add Contact: "
					+ ((EditText) findViewById(R.id.AddWEBSITE)).getText()
							.toString());*/
			cv.put(ContactsConsts.CONTACT_WEB_ADRESS,
					((EditText) findViewById(R.id.AddWEBSITE)).getText()
							.toString());
		}

		/*
		 * if (((EditText) findViewById(R.id.AddINTERNETCALL)).getText() !=
		 * null) {
		 * 
		 * cv.put(ContactsConsts.CONTACT_INTERNETCALL, ((EditText)
		 * findViewById(R.id.AddINTERNETCALL)).getText().toString()); }
		 */
		
		
		if (((EditText) findViewById(R.id.AddINTERNETCALL)).getText() != null) {

			cv.put(ContactsConsts.CONTACT_INTERNETCALL,
					((EditText) findViewById(R.id.AddINTERNETCALL)).getText()
							.toString());
		}
		

		if (((EditText) findViewById(R.id.AddOrganztn)).getText() != null) {
			//System.out.println(" AddOrganztn  " + AddOrganztn.getText());
			cv.put(ContactsConsts.CONTACT_COMPANY,
					((EditText) findViewById(R.id.AddOrganztn)).getText()
							.toString());
		}
		if (((EditText) findViewById(R.id.AddDepartment)).getText() != null) {
			/*System.out.println(" CONTACT_DEPARTMENT  "
					+ ((EditText) findViewById(R.id.AddDepartment)).getText());*/
			cv.put(ContactsConsts.CONTACT_DEPARTMENT,
					((EditText) findViewById(R.id.AddDepartment)).getText()
							.toString());
		}

		if (((EditText) findViewById(R.id.AddTitle)).getText() != null) {
			/*System.out.println(" AddTitle  "
					+ ((EditText) findViewById(R.id.AddTitle)).getText());*/
			cv.put(ContactsConsts.CONTACT_JOB_TITLE,
					((EditText) findViewById(R.id.AddTitle)).getText()
							.toString());
		}

		if (((EditText) findViewById(R.id.AddPhoneticName)).getText() != null) {
			cv.put(ContactsConsts.CONTACT_PHONETIC_FAMILY_NAME,
					((EditText) findViewById(R.id.AddPhoneticName)).getText()
							.toString());
		}

		if (((EditText) findViewById(R.id.AddPhoneticMiddleName)).getText() != null) {
			cv.put(ContactsConsts.CONTACT_PHONETIC_MIDDLE_NAME,
					((EditText) findViewById(R.id.AddPhoneticMiddleName))
							.getText().toString());
		}

		if (((EditText) findViewById(R.id.AddPhoneticLastName)).getText() != null) {
			cv.put(ContactsConsts.CONTACT_PHONETIC_GIVEN_NAME,
					((EditText) findViewById(R.id.AddPhoneticLastName))
							.getText().toString());
		}

		if (thumbnail != null) {

			cv.put(ContactsConsts.CONTACT_PHOTO,
					ContactsUtilities.getBytes(thumbnail));
		}

		cv.put(ContactsConsts.CONTACT_CLIENT_ID, clientId);
		return cv;
	}

	public void addaddressValue(ContentValues addressVal, String str, String clientId){
		//String clientId = "" + System.currentTimeMillis();
		
		String unique_id = str + "||" + clientId;
	
		addressVal.put(ContactsConsts.ADRESS_CLIENT_ID, unique_id);
		Uri uri =  getContentResolver().insert(ContactsConsts.CONTENT_URI_ADRESS,addressVal);
	}

	void syncWithServer(String clientId) {
		try {
			Cursor c = getContentResolver().query(
					EmEmailContent.Mailbox.CONTENT_URI,
					null,
					EmEmailContent.Account.DISPLAY_NAME + "=?",
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

			}
			if (c != null) {
				c.close();
			}
		} catch (Exception e) {
			Log.e(ContactsParentActivity.class.getName(), "menu_manual_sync "
					+ e.toString());

		} // String nameVal;
		// if (nameSelection) {
		// nameVal = namepfx.getText().toString();
		// } else {
		// nameVal = AddName.getText().toString();
		// }
		//
		// ContactsModel updateModel = new ContactsModel();
		// updateModel.setClientid(clientId);
		// updateModel.setcontacts_first_name(nameVal);
		// updateModel
		// .setcontacts_last_name(((EditText) findViewById(R.id.AddMiddleName))
		// .getText().toString());
		// updateModel
		// .setcontacts_last_name(((EditText) findViewById(R.id.AddLastName))
		// .getText().toString());
		// updateModel
		// .setcontacts_email1_address(((EditText) findViewById(R.id.AddEmail))
		// .getText().toString());
		// updateModel
		// .setcontacts_business_telephone_number(((EditText)
		// findViewById(R.id.AddPhone2))
		// .getText().toString());
		// updateModel
		// .setcontacts_mobile_telephone_number(((EditText)
		// findViewById(R.id.AddPhone))
		// .getText().toString());
		// updateModel
		// .setcontacts_business_address_city(((EditText)
		// findViewById(R.id.AddAddress))
		// .getText().toString());
		// updateModel
		// .setcontacts_company_name(((EditText) findViewById(R.id.AddOrganztn))
		// .getText().toString());
		//
		// updateModel
		// .setcontacts_department(((EditText) findViewById(R.id.AddDepartment))
		// .getText().toString());
		//
		// updateModel
		// .setcontacts_job_title(((EditText) findViewById(R.id.AddTitle))
		// .getText().toString());
		//
		// ArrayList<ContactsModel> changeList = new ArrayList<ContactsModel>();
		// changeList.add(updateModel);

		// Contacts contact = (Contacts) getApplication();
		// // contact.ContactRequest(changeList, Contacts.SYNC_CONTACTS_ADD);
		// contact.getActiveSyncManager().setCurrentRequest(
		// Contacts.SYNC_CONTACTS_ADD);
		// contact.getActiveSyncManager().setMlocalChanges(changeList);
		//
		//
		// EasSynctask addContactTask = new EasSynctask(this,
		// Contacts.STATUS_CONTACTS_SYNC);
		// addContactTask.execute(contact.getActiveSyncManager());

	}

	@Override
	public void onBackPressed() {
		if (pwindo != null && pwindo.isShowing() ){
			pwindo.dismiss();
		}else if (pwindow != null && pwindow.isShowing()){
			pwindow.dismiss();
		}else{
			finish();
		}
			super.onBackPressed();
	}

	@Override
	public void onItemSelected(IcsAdapterView<?> parent, View view,
			int position, long id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onNothingSelected(IcsAdapterView<?> parent) {
		// TODO Auto-generated method stub

	}

	public void taskComplete(int Task, boolean taskStatus, int statusCode,
			int requestStatus, String errorString) {
		// TODO Auto-generated method stub

	}

	public Handler getDialogHandler() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		//System.out.println("MenuItem Selected");
		switch (item.getItemId()) {
		case R.id.editTitleDiscard:

			InputMethodManager immConnect = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						
			 if (immConnect.isAcceptingText()) {
			        //writeToLog("Software Keyboard was shown");				 
				 immConnect.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);				 
			    } else {
			       // writeToLog("Software Keyboard was not shown");			    	
			    }
			
			
			
			finish();
		}

		return super.onMenuItemSelected(featureId, item);
	}
}
