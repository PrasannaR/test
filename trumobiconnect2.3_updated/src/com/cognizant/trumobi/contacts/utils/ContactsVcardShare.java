package com.cognizant.trumobi.contacts.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.TruBoxSDK.SharedPreferences;
import com.cognizant.trubox.contacts.db.ContactsConsts;
import com.cognizant.trubox.contacts.db.ContactsModel;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.contacts.activity.ContactsDetailActivity;
import com.cognizant.trumobi.em.Email;
import com.cognizant.trumobi.em.activity.EmMessageCompose;

import com.cognizant.trumobi.log.ContactsLog;




import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcelable;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class ContactsVcardShare {




	File vcfFile;
	Cursor cursor;
	Context mContext;

	ArrayList<ContactsModel> result = null;

	String shareLastName="";
	String shareFirstName="";
	String shareMiddleName="";
	String sharePrefixName="";
	String shareSuffixName="";
	String sharePhoneticGivenName="";
	String sharePhoneticMiddleName="";
	String sharePhoneticFamilyName="";
	String shareNickName="";
	String sharePhone1="";
	String sharePhone2="";
	String sharePhone3="";
	String sharePhone4="";
	String sharePhone5="";
	String sharePhone6="";
	String sharePhone7="";
	String sharePhone8="";
	String sharePhone9="";
	String sharePhone10="";
	String sharePhone11="";
	String sharePhone12="";
	String sharePhone13="";
	String sharePhone14="";
	String sharePhone15="";
	String shareEmail1="";
	String shareEmail2="";
	String shareEmail3="";
	String shareEmail4="";
	String shareEmail5="";
	String shareEmail6="";
	String shareIM1="";
	String shareIM2="";
	String shareIM3="";
	String shareIM4="";
	String shareIM5="";
	String shareIM6="";
	String shareAddress1="";
	String shareAddress2="";
	String shareAddress3="";
	String shareCompanyOrg="";
	String shareTitle="";
	String shareWebsiteUrl="";
	String shareNote="";
	String shareInternetCall="";


	String surname="";
	String frstname="";
	String org="";
	String phone1="";
	String phone2="";
	String emailid="";

	String phonetypeget1="";
	String phonetypeget2="";
	//String addrs="";
	//String addrsMap;
	String unique_id = null;
	String sharetitle;

	String cursorPosition;
	String selectionArgument = "_id = ?";
	ContentValues emailNumbers = new ContentValues();


	public ContactsVcardShare(Context context,String cursorPost) {
		//super(context);
		// TODO Auto-generated constructor stub
		mContext = context;
		cursorPosition= cursorPost;

		if(cursorPost != null && ! cursorPost.isEmpty())
		{
			Log.i("ContactsVcardShare", "String cursorPost"+cursorPost);
			result = ContactsUtilities.getContactsDetailsFromCursor(
					Email.getAppContext().getContentResolver(),
					cursorPost);
		}

		if (result != null && result.size() > 0) {
			Log.i("ContactsVcardShare", "result"+result.size());
			Log.i("ContactsVcardShare", "result value"+result.toString());
			pouplateContactDetailsByModel(result);
		}

		VcardShareUtil();
	}


	private void pouplateContactDetailsByModel(ArrayList<ContactsModel> result) {

		ContactsLog.e("FIRST NAME   :  ", "FIRST NAME "+ result.get(0).getcontacts_first_name());


		if (result.get(0).getcontacts_first_name() != null
				&& !result.get(0).getcontacts_first_name().isEmpty()) {

			shareFirstName = result.get(0).getcontacts_first_name();
			Log.i("ContactsVcardShare","shareFirstName"+shareFirstName);

		}

		if (result.get(0).getcontacts_last_name() != null
				&& !result.get(0).getcontacts_last_name().isEmpty()) {
			shareLastName = result.get(0).getcontacts_last_name();
			Log.i("ContactsVcardShare","shareLastName"+shareLastName);

		}

		if (result.get(0).getcontacts_middle_name() != null
				&& !result.get(0).getcontacts_middle_name()
				.isEmpty())

		{
			shareMiddleName = result.get(0).getcontacts_middle_name();
		}

		if (result.get(0).getContacts_name_prefix() != null
				&& !result.get(0).getContacts_name_prefix()
				.isEmpty())

		{
			sharePrefixName = result.get(0).getContacts_name_prefix();
		}

		if (result.get(0).getContacts_name_suffix() != null
				&& !result.get(0).getContacts_name_suffix() 
				.isEmpty())
		{
			shareSuffixName = result.get(0).getContacts_name_suffix();
		}




		if (result.get(0).getContact_phonetic_family_name() != null
				&& !result.get(0).getContact_phonetic_family_name() 
				.isEmpty())
		{
			sharePhoneticFamilyName = result.get(0).getContact_phonetic_family_name();
		}

		if (result.get(0).getContact_phonetic_given_name() != null
				&& !result.get(0).getContact_phonetic_given_name() 
				.isEmpty())
		{
			sharePhoneticGivenName = result.get(0).getContact_phonetic_given_name();
		}

		if (result.get(0).getContact_phonetic_middle_name() != null
				&& !result.get(0).getContact_phonetic_middle_name() 
				.isEmpty())
		{
			sharePhoneticMiddleName = result.get(0).getContact_phonetic_middle_name();
		}


		if (result.get(0).getContacts_name_prefix() != null
				&& !result.get(0).getContacts_name_prefix() 
				.isEmpty())
		{
			sharePrefixName = result.get(0).getContacts_name_prefix();
		}

		if (result.get(0).getContacts_name_suffix() != null
				&& !result.get(0).getContacts_name_suffix()
				.isEmpty())
		{
			shareSuffixName = result.get(0).getContacts_name_suffix();
		}




		if (result.get(0).getcontacts_title() != null
				&& !result.get(0).getcontacts_title().isEmpty()) {
			shareTitle=  result.get(0).getcontacts_title();

		} else {

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

			shareCompanyOrg = company_details;

		} else {

		}

		// Phone type 1 data population
		if (result.get(0).getcontacts_mobile_telephone_number() != null
				&& !result.get(0).getcontacts_mobile_telephone_number()
				.isEmpty()) {

			sharePhone1 = result.get(0).getcontacts_mobile_telephone_number();


		} else {

		} /* Phone type 2 data population */
		if (result.get(0).getcontacts_business_telephone_number() != null
				&& !result.get(0).getcontacts_business_telephone_number()
				.isEmpty()) {
			sharePhone2 = result.get(0).getcontacts_business_telephone_number();

		} else {

		}
		if (result.get(0).getcontacts_assistant_telephone_number() != null
				&& !result.get(0).getcontacts_assistant_telephone_number()
				.isEmpty()) {

			sharePhone3 = result.get(0).getcontacts_assistant_telephone_number();

		} else {

		}
		if (result.get(0).getcontacts_business2_telephone_number() != null
				&& !result.get(0).getcontacts_business2_telephone_number()
				.isEmpty()) {
			sharePhone4 = result.get(0).getcontacts_business2_telephone_number();

		} else {

		}
		if (result.get(0).getcontacts_business_fax_number() != null
				&& !result.get(0).getcontacts_business_fax_number().isEmpty()) {
			sharePhone5 = result.get(0).getcontacts_business_fax_number();

		} else {

		}
		if (result.get(0).getcontacts_car_telephone_number() != null
				&& !result.get(0).getcontacts_car_telephone_number().isEmpty()) {

			sharePhone6 = result.get(0).getcontacts_car_telephone_number();


		} else {

		}
		if (result.get(0).getcontacts_home2_telephone_number() != null
				&& !result.get(0).getcontacts_home2_telephone_number()
				.isEmpty()) {
			sharePhone7 = result.get(0).getcontacts_home2_telephone_number();

		} else {

		}
		if (result.get(0).getcontacts_home_fax_number() != null
				&& !result.get(0).getcontacts_home_fax_number().isEmpty()) {
			sharePhone8 = result.get(0).getcontacts_home_fax_number();

		} else {

		}
		if (result.get(0).getcontacts_home_telephone_number() != null
				&& !result.get(0).getcontacts_home_telephone_number().isEmpty()) {
			sharePhone9 = result.get(0).getcontacts_home_telephone_number();

		} else {

		}
		if (result.get(0).getcontacts_pager_number() != null
				&& !result.get(0).getcontacts_pager_number().isEmpty()) {
			sharePhone10 = result.get(0).getcontacts_pager_number();

		} else {

		}
		if (result.get(0).getcontacts_radio_telephone_number() != null
				&& !result.get(0).getcontacts_radio_telephone_number()
				.isEmpty()) {
			sharePhone11 = result.get(0).getcontacts_radio_telephone_number();

		} else {

		}
		if (result.get(0).getContacts2_company_main_phone() != null
				&& !result.get(0).getContacts2_company_main_phone().isEmpty()) {
			sharePhone12 = result.get(0).getContacts2_company_main_phone();

		} else {

		}
		if (result.get(0).getContact_custom_phone1() != null
				&& !result.get(0).getContact_custom_phone1().isEmpty()) {
			sharePhone13 = result.get(0).getContact_custom_phone1();

		} else {

		}
		if (result.get(0).getContact_custom_phone2() != null
				&& !result.get(0).getContact_custom_phone2().isEmpty()) {
			sharePhone14 = result.get(0).getContact_custom_phone2();

		} else {

		}
		if (result.get(0).getContact_custom_phone3() != null
				&& !result.get(0).getContact_custom_phone3().isEmpty()) {
			sharePhone15 = result.get(0).getContact_custom_phone3();

		} else {

		}



		if (result.get(0).getcontacts_email1_address() != null
				&& !result.get(0).getcontacts_email1_address().isEmpty()) {
			shareEmail1 = result.get(0).getcontacts_email1_address();

		} else {

		}

		if (result.get(0).getcontacts_email2_address() != null
				&& !result.get(0).getcontacts_email2_address().isEmpty()) {
			shareEmail2 = result.get(0).getcontacts_email2_address();

		} else {

		}
		Log.e("Contacts detail actvity", "above email 3");
		if (result.get(0).getcontacts_email3_address() != null
				&& !result.get(0).getcontacts_email3_address().isEmpty()) {
			shareEmail3 = result.get(0).getcontacts_email3_address();

		} else {

		}

		Log.e("Contacts detail actvity", "above email 4");
		if (result.get(0).getContacts_custom_email1_address() != null
				&& !result.get(0).getContacts_custom_email1_address().isEmpty()) {

			shareEmail4 = result.get(0).getContacts_custom_email1_address();

		} else {

		}
		if (result.get(0).getContacts_custom_email2_address() != null
				&& !result.get(0).getContacts_custom_email2_address().isEmpty()) {
			shareEmail5 = result.get(0).getContacts_custom_email2_address();

		} else {

		}

		Log.e("Contacts detail actvity", "above email 6");
		if (result.get(0).getContacts_custom_email3_address() != null
				&& !result.get(0).getContacts_custom_email3_address().isEmpty()) {
			shareEmail6 = result.get(0).getContacts_custom_email3_address();

		} else {

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

							if (shareAddress1 != null && !shareAddress1.isEmpty()) {
								shareAddress1 = shareAddress1 + "," + city;
							} else {
								shareAddress1 = city;
							}

							// addrsMap = city;
							// break;
						}
					}

					if ((addrVal.size() > 0 && addrVal.get(i) != null && i == 0)) {
						String street = addrVal.get(i).get("value" + i);
						if (street != null && !street.isEmpty()) {
							shareAddress1 = street;
							// break;
						}
					}

					if ((addrVal.size() > 0 && addrVal.get(i) != null && i == 2)) {
						String state = addrVal.get(i).get("value" + i);
						if (state != null && !state.isEmpty()) {
							// addrsMap = state;
							// break;

							if (shareAddress1 != null && !shareAddress1.isEmpty()) {
								shareAddress1 = shareAddress1 + "," + state;
							} else {
								shareAddress1 = state;
							}

						}
					}

					if ((addrVal.size() > 0 && addrVal.get(i) != null && i == 3)) {
						String pincode = addrVal.get(i).get("value" + i);
						if (pincode != null && !pincode.isEmpty()) {
							// addrsMap = country;
							// break;

							if (shareAddress1 != null && !shareAddress1.isEmpty()) {
								shareAddress1 = shareAddress1 + "," + pincode;
							} else {
								shareAddress1 = pincode;
							}

						}
					}

					if ((addrVal.size() > 0 && addrVal.get(i) != null && i == 4)) {
						String country = addrVal.get(i).get("value" + i);
						if (country != null && !country.isEmpty()) {
							// addrsMap = country;
							// break;

							if (shareAddress1 != null && !shareAddress1.isEmpty()) {
								shareAddress1 = shareAddress1 + "," + country;
							} else {
								shareAddress1 = country;
							}

						}
					}
				}

			} catch (Exception e) {

			}

		}

		else {

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
							if (shareAddress2 != null && !shareAddress2.isEmpty()) {
								shareAddress2 = shareAddress2 + "," + city;
							} else {
								shareAddress2 = city;
							}
							// addrsMap = city;
							// break;
						}
					}
					if ((addrVal.size() > 0 && addrVal.get(i) != null && i == 0)) {
						String street = addrVal.get(i).get("value" + i);
						if (street != null && !street.isEmpty()) {
							shareAddress2 = street;
							// break;
						}
					}
					if ((addrVal.size() > 0 && addrVal.get(i) != null && i == 2)) {
						String state = addrVal.get(i).get("value" + i);
						if (state != null && !state.isEmpty()) {
							// addrsMap = state;
							// break;
							if (shareAddress2 != null && !shareAddress2.isEmpty()) {
								shareAddress2 = shareAddress2 + "," + state;
							} else {
								shareAddress2 = state;
							}
						}
					}

					if ((addrVal.size() > 0 && addrVal.get(i) != null && i == 3)) {
						String pincode = addrVal.get(i).get("value" + i);
						if (pincode != null && !pincode.isEmpty()) {
							// addrsMap = country;
							// break;
							if (shareAddress2 != null && !shareAddress2.isEmpty()) {
								shareAddress2 = shareAddress2 + "," + pincode;
							} else {
								shareAddress2 = pincode;
							}

						}
					}

					if ((addrVal.size() > 0 && addrVal.get(i) != null && i == 4)) {
						String country = addrVal.get(i).get("value" + i);
						if (country != null && !country.isEmpty()) {
							// addrsMap = country;
							// break;
							if (shareAddress2 != null && !shareAddress2.isEmpty()) {
								shareAddress2 = shareAddress2 + "," + country;
							} else {
								shareAddress2 = country;
							}

						}
					}
				}

			} catch (Exception e) {

			}


		}

		else {


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
							if (shareAddress3 != null && !shareAddress3.isEmpty()) {
								shareAddress3 = shareAddress3 + "," + city;
							} else {
								shareAddress3 = city;
							}
							// addrsMap = city;
							// break;
						}
					}
					if ((addrVal.size() > 0 && addrVal.get(i) != null && i == 0)) {
						String street = addrVal.get(i).get("value" + i);
						if (street != null && !street.isEmpty()) {
							shareAddress3 = street;
							// break;
						}
					}
					if ((addrVal.size() > 0 && addrVal.get(i) != null && i == 2)) {
						String state = addrVal.get(i).get("value" + i);
						if (state != null && !state.isEmpty()) {
							// addrsMap = state;
							// break;
							if (shareAddress3 != null && !shareAddress3.isEmpty()) {
								shareAddress3  = shareAddress3  + "," + state;
							} else {
								shareAddress3 = state;
							}
						}
					}

					if ((addrVal.size() > 0 && addrVal.get(i) != null && i == 3)) {
						String pincode = addrVal.get(i).get("value" + i);
						if (pincode != null && !pincode.isEmpty()) {
							// addrsMap = country;
							// break;
							if (shareAddress3!= null && !shareAddress3.isEmpty()) {
								shareAddress3 = shareAddress3 + "," + pincode;
							} else {
								shareAddress3 = pincode;
							}

						}
					}

					if ((addrVal.size() > 0 && addrVal.get(i) != null && i == 4)) {
						String country = addrVal.get(i).get("value" + i);
						if (country != null && !country.isEmpty()) {
							// addrsMap = country;
							// break;
							if (shareAddress3 != null && !shareAddress3.isEmpty()) {
								shareAddress3 = shareAddress3 + "," + country;
							} else {
								shareAddress3 = country;
							}

						}
					}
				}

			} catch (Exception e) {

			}

		}

		else {

		}



		if (result.get(0).getContact_im_address() != null
				&& !result.get(0).getContact_im_address().isEmpty()) {

			shareIM1 = result.get(0).getContact_im_address();

		} else {

		}

		if (result.get(0).getContact_im_address1() != null
				&& !result.get(0).getContact_im_address1().isEmpty()) {
			shareIM2 = result.get(0).getContact_im_address1();

		} else {

		}

		if (result.get(0).getContact_im_address2() != null
				&& !result.get(0).getContact_im_address2().isEmpty()) {
			shareIM3 = result.get(0).getContact_im_address2();

		} else {

		}
		if (result.get(0).getContact_custom_im_address() != null
				&& !result.get(0).getContact_custom_im_address().isEmpty()) {
			shareIM4 = result.get(0).getContact_custom_im_address();

		} else {

		}

		if (result.get(0).getContact_custom_im1_address() != null
				&& !result.get(0).getContact_custom_im1_address().isEmpty()) {
			shareIM5 = result.get(0).getContact_custom_im1_address();

		} else {

		}

		if (result.get(0).getContact_custom_im2_address() != null
				&& !result.get(0).getContact_custom_im2_address().isEmpty()) {
			shareIM6 = result.get(0).getContact_custom_im2_address();

		} else {

		}



		if (result.get(0).getContact_notes() != null
				&& !result.get(0).getContact_notes().isEmpty()) {
			shareNote = result.get(0).getContact_notes();

		} else {

		}

		if (result.get(0).getContact_website() != null
				&& !result.get(0).getContact_website().isEmpty()) {

			shareWebsiteUrl = result.get(0).getContact_website();

		} else {

		}

		if (result.get(0).getContact_nick_name() != null
				&& !result.get(0).getContact_nick_name().isEmpty()) {
			shareNickName = result.get(0).getContact_nick_name();

		} else {

		}

		if (result.get(0).getContact_internetcall() != null
				&& !result.get(0).getContact_internetcall().isEmpty()) {
			shareInternetCall= result.get(0).getContact_internetcall();

		} else {

		}








		vcfFile = new File(mContext.getExternalFilesDir(null), shareFirstName
				+ shareLastName + "VCard.vcf");

		FileWriter fw;
		try {
			fw = new FileWriter(vcfFile);

			fw.write("BEGIN:VCARD\r\n");
			fw.write("VERSION:2.1\r\n");
			fw.write("N:"+shareLastName+";"+ shareFirstName +";"+shareMiddleName+";"+sharePrefixName+";"+shareSuffixName+"\r\n");
			fw.write("FN:"+sharePrefixName+" "+ shareFirstName +" "+shareMiddleName+" "+shareLastName+", "+shareSuffixName+"\r\n");
			fw.write("X-PHONETIC-FIRST-NAME:"+sharePhoneticGivenName+"\r\n");
			fw.write("X-PHONETIC-MIDDLE-NAME:"+sharePhoneticMiddleName+"\r\n");
			fw.write("X-PHONETIC-LAST-NAME:"+sharePhoneticFamilyName+"\r\n");
			fw.write("X-ANDROID-CUSTOM:vnd.android.cursor.item/nickname;"+shareNickName+";1;;;;;;;;;;;;;\r\n");
			fw.write("TEL;HOME:"+sharePhone1+"\r\n");
			fw.write("TEL;CELL:"+sharePhone2+"\r\n");
			fw.write("TEL;CELL:"+sharePhone3+"\r\n");
			fw.write("TEL;PREF:"+sharePhone4+"\r\n");
			fw.write("TEL;WORK;FAX:"+sharePhone5+"\r\n");
			fw.write("TEL;HOME;FAX:"+sharePhone6+"\r\n");
			fw.write("TEL;PAGER:"+sharePhone7+"\r\n");
			fw.write("TEL;VOICE:"+sharePhone8+"\r\n");
			fw.write("TEL;X-Cus1:"+sharePhone9+"\r\n");
			fw.write("TEL;X-Cus2:"+sharePhone10+"\r\n");
			fw.write("TEL;X-Cus4:"+sharePhone11+"\r\n");
			fw.write("TEL;X-Cust5:"+sharePhone12+"\r\n");
			fw.write("TEL;X-Cust6:"+sharePhone13+"\r\n");
			fw.write("TEL;X-Cust7:"+sharePhone14+"\r\n");
			fw.write("TEL;X-Cust8:"+sharePhone15+"\r\n");
			fw.write("EMAIL;HOME:"+shareEmail1+"\r\n");
			fw.write("EMAIL;WORK:"+shareEmail2+"\r\n");
			fw.write("EMAIL:"+shareEmail3+"\r\n");
			fw.write("EMAIL;X-Cus1:"+shareEmail4+"\r\n");
			fw.write("EMAIL;X-Cus2:"+shareEmail5+"\r\n");
			fw.write("EMAIL;X-Cus3:"+shareEmail6+"\r\n");
			fw.write("ADR;HOME:;;"+shareAddress1+";;;;\r\n");
			fw.write("ADR;WORK:;;"+shareAddress2+";;;;\r\n");
			fw.write("ADR:;;"+shareAddress3+";;;;\r\n");
			fw.write("ORG:"+shareCompanyOrg+"\r\n");
			fw.write("TITLE:"+shareTitle+"\r\n");
			fw.write("URL:"+shareWebsiteUrl+"\r\n");
			//fw.write("PHOTO;ENCODING=BASE64;JPEG:"+thumbnail.toString()+"\r\n");

			fw.write("NOTE:"+shareNote+"\r\n");
			fw.write("X-AIM:"+shareIM1+"\r\n");
			fw.write("X-MSN:"+shareIM2+"\r\n");
			fw.write("X-YAHOO:"+shareIM3+"\r\n");
			fw.write("X-SKYPE-USERNAME:"+shareIM4+"\r\n");
			fw.write("X-QQ:"+shareIM5+"\r\n");
			fw.write("X-GOOGLE-TALK:"+shareIM6+"\r\n");
			fw.write("X-SIP:"+shareInternetCall+"\r\n");
			fw.write("END:VCARD\r\n");

			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}






		try 
		{                       
			Intent targetedShareIntent = new Intent(mContext,EmMessageCompose.class);
			targetedShareIntent.putExtra("ContactsAppHandshake","ContactsAppHandshake");                                     
			targetedShareIntent.setType("text/plain");
			targetedShareIntent.putExtra(Intent.EXTRA_SUBJECT, "");
			targetedShareIntent.putExtra(Intent.EXTRA_TEXT, "");
			targetedShareIntent.putExtra(Intent.EXTRA_STREAM,
					Uri.fromFile(vcfFile));

			targetedShareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(targetedShareIntent);


		} 
		catch (ActivityNotFoundException activityException) 
		{
			Log.e("Secured apps not available to share vcard", "Call failed");
			Toast.makeText(mContext,"Secured apps not available to share vcard",Toast.LENGTH_LONG).show();
		}










	}


	public void VcardShareUtil()
	{

	}



	public void VcardShareUtil(Cursor mCursor)

	{




	}



}
