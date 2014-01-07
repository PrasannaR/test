package com.cognizant.trubox.contacts.db;

import java.io.Serializable;

import android.R.string;
import android.text.util.Rfc822Token;
import android.text.util.Rfc822Tokenizer;

public class ContactsModel implements Serializable {

	String contacts_anniversary = "";
	String contacts_assistant_name = "";
	String contacts_assistant_telephone_number = "";
	String contacts_birthday = "";
	String contacts_body = "";
	String contacts_body_size = "";
	String contacts_body_truncated = "";
	String contacts_business2_telephone_number = "";
	String contacts_business_address_city = "";
	String contacts_business_address_country = "";
	String contacts_business_address_postal_code = "";
	String contacts_business_address_state = "";
	String contacts_business_address_street = "";
	String contacts_business_fax_number = "";
	String contacts_business_telephone_number = "";
	String contacts_car_telephone_number = "";
	String contacts_categories = "";
	String contacts_category = "";
	String contacts_children = "";
	String contacts_child = "";
	String contacts_company_name = "";
	String contacts_department = "";
	String contacts_email1_address = "";
	String contacts_email2_address = "";
	String contacts_email3_address = "";
	String contacts_file_as = "";
	String contacts_first_name = "";
	String contacts_home2_telephone_number = "";
	String contacts_home_address_city = "";
	String contacts_home_address_country = "";
	String contacts_home_address_postal_code = "";
	String contacts_home_address_state = "";
	String contacts_home_address_street = "";
	String contacts_home_fax_number = "";
	String contacts_home_telephone_number = "";
	String contacts_job_title = "";
	String contacts_last_name = "";
	String contacts_middle_name = "";
	String contacts_mobile_telephone_number = "";
	String contacts_office_location = "";
	String contacts_other_address_city = "";
	String contacts_other_address_country = "";
	String contacts_other_address_postal_code = "";
	String contacts_other_address_state = "";
	String contacts_other_address_street = "";
	String contacts_pager_number = "";
	String contacts_radio_telephone_number = "";
	String contacts_spouse = "";
	String contacts_suffix = "";
	String contacts_title = "";
	String contacts_webpage = "";
	String contacts_yomi_company_name = "";
	String contacts_yomi_first_name = "";
	String contacts_yomi_last_name = "";
	String contacts_compressed_rtf = "";
	String contacts_picture = "";
	String contacts_business_location = "";
	String contacts_home_location = "";
	String contacts_other_location = "";

	String contacts_name_prefix;
	String contacts_name_suffix;
	String contact_phonetic_family_name;
	String contact_phonetic_middle_name;
	String contact_phonetic_given_name;
	String contact_phone_number_mobile_type1;
	String contact_phone_number_mobile_type2;

	String contacts_assistant_telephone_number_type;
	String contacts_business2_telephone_number_type;
	String contacts_business_fax_number_type;
	String contacts_car_telephone_numbe_type;
	String contacts_home2_telephone_number_type;
	String contacts_home_fax_number_type;
	String contacts_home_telephone_number_type;
	String contacts_pager_number_type;
	String contacts_radio_telephone_number_type;
	String contacts2_company_main_phone;
	String contacts2_company_main_phone_type;
	String contact_custom_phone1;
	String contact_custom_phone1_type;
	String contact_custom_phone2;
	String contact_custom_phone2_type;
	String contact_custom_phone3;
	String contact_custom_phone3_type;
	
	String contact_email1_type;
	String contact_email2_type;
	String contact_email3_type;
	String contacts_custom_email1_address;
	String contacts_custom_email2_address;
	String contacts_custom_email3_address;
	String contact_custom_email1_type;
	String contact_custom_email2_type;
	String contact_custom_email3_type;
	
	
	
	
	String contact_email_type;
	String contact_business_location_type;
	String contact_home_location_type;
	String contact_other_location_type;
	String contact_nick_name;
	String contact_website;
	String contact_internetcall;
	String contact_im_address;
	String contact_im_address1;
	String contact_im_address2;
	String contact_custom_im_address;
	String contact_custom_im1_address;
	String contact_custom_im2_address;
	
	String contact_im_address_type;
	String contact_im_address1_type;
	String contact_im_address2_type;
	String contact_custom_im_address_type;
	String contact_custom_im1_address_type;
	String contact_custom_im2_address_type;
	String contacts_ringtone_uri;
	
	int contact_id;
	byte[] contacts_image;
	int contact_isFavorite;
	boolean isNativeContact;
	boolean isSearchContact;
	String contact_notes;

	String serverId = "";
	String clientid = "";

	public String getClientid() {
		return clientid;
	}

	public void setClientid(String clientid) {
		this.clientid = clientid;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public int getContact_id() {
		return contact_id;
	}

	public void setContact_id(int contact_id) {
		this.contact_id = contact_id;
	}

	class EmailRow {
		String email;
		String displayName;

		public EmailRow(String _email) {
			Rfc822Token[] tokens = Rfc822Tokenizer.tokenize(_email);
			// Can't happen, but belt & suspenders
			if (tokens.length == 0) {
				email = "";
				displayName = "";
			} else {
				Rfc822Token token = tokens[0];
				email = token.getAddress();
				displayName = token.getName();
			}
		}

	}

	class ImRow {
		String im;

		public ImRow(String _im) {
			im = _im;
		}

	}

	public String getcontacts_anniversary() {
		return contacts_anniversary;
	}

	public void setcontacts_anniversary(String contacts_anniversary) {
		this.contacts_anniversary = contacts_anniversary;
	}

	public String getcontacts_assistant_name() {
		return contacts_assistant_name;
	}

	public void setcontacts_assistant_name(String contacts_assistant_name) {
		this.contacts_assistant_name = contacts_assistant_name;
	}

	public String getcontacts_assistant_telephone_number() {
		return contacts_assistant_telephone_number;
	}

	public void setcontacts_assistant_telephone_number(
			String contacts_assistant_telephone_number) {
		this.contacts_assistant_telephone_number = contacts_assistant_telephone_number;
	}

	public String getcontacts_birthday() {
		return contacts_birthday;
	}

	public void setcontacts_birthday(String contacts_birthday) {
		this.contacts_birthday = contacts_birthday;
	}

	public String getcontacts_body() {
		return contacts_body;
	}

	public void setcontacts_body(String contacts_body) {
		this.contacts_body = contacts_body;
	}

	public String getcontacts_body_size() {
		return contacts_body_size;
	}

	public void setcontacts_body_size(String contacts_body_size) {
		this.contacts_body_size = contacts_body_size;
	}

	public String getcontacts_body_truncated() {
		return contacts_body_truncated;
	}

	public void setcontacts_body_truncated(String contacts_body_truncated) {
		this.contacts_body_truncated = contacts_body_truncated;
	}

	public String getcontacts_business2_telephone_number() {
		return contacts_business2_telephone_number;
	}

	public void setcontacts_business2_telephone_number(
			String contacts_business2_telephone_number) {
		this.contacts_business2_telephone_number = contacts_business2_telephone_number;
	}

	public String getcontacts_business_address_city() {
		return contacts_business_address_city;
	}

	public void setcontacts_business_address_city(
			String contacts_business_address_city) {
		this.contacts_business_address_city = contacts_business_address_city;
	}

	public String getcontacts_business_address_country() {
		return contacts_business_address_country;
	}

	public void setcontacts_business_address_country(
			String contacts_business_address_country) {
		this.contacts_business_address_country = contacts_business_address_country;
	}

	public String getcontacts_business_address_postal_code() {
		return contacts_business_address_postal_code;
	}

	public void setcontacts_business_address_postal_code(
			String contacts_business_address_postal_code) {
		this.contacts_business_address_postal_code = contacts_business_address_postal_code;
	}

	public String getcontacts_business_address_state() {
		return contacts_business_address_state;
	}

	public void setcontacts_business_address_state(
			String contacts_business_address_state) {
		this.contacts_business_address_state = contacts_business_address_state;
	}

	public String getcontacts_business_address_street() {
		return contacts_business_address_street;
	}

	public void setcontacts_business_address_street(
			String contacts_business_address_street) {
		this.contacts_business_address_street = contacts_business_address_street;
	}

	public String getcontacts_business_fax_number() {
		return contacts_business_fax_number;
	}

	public void setcontacts_business_fax_number(
			String contacts_business_fax_number) {
		this.contacts_business_fax_number = contacts_business_fax_number;
	}

	public String getcontacts_business_telephone_number() {
		return contacts_business_telephone_number;
	}

	public void setcontacts_business_telephone_number(
			String contacts_business_telephone_number) {
		this.contacts_business_telephone_number = contacts_business_telephone_number;
	}

	public String getcontacts_car_telephone_number() {
		return contacts_car_telephone_number;
	}

	public void setcontacts_car_telephone_number(
			String contacts_car_telephone_number) {
		this.contacts_car_telephone_number = contacts_car_telephone_number;
	}

	public String getcontacts_categories() {
		return contacts_categories;
	}

	public void setcontacts_categories(String contacts_categories) {
		this.contacts_categories = contacts_categories;
	}

	public String getcontacts_category() {
		return contacts_category;
	}

	public void setcontacts_category(String contacts_category) {
		this.contacts_category = contacts_category;
	}

	public String getcontacts_children() {
		return contacts_children;
	}

	public void setcontacts_children(String contacts_children) {
		this.contacts_children = contacts_children;
	}

	public String getcontacts_child() {
		return contacts_child;
	}

	public void setcontacts_child(String contacts_child) {
		this.contacts_child = contacts_child;
	}

	public String getcontacts_company_name() {
		return contacts_company_name;
	}

	public void setcontacts_company_name(String contacts_company_name) {
		this.contacts_company_name = contacts_company_name;
	}

	public String getcontacts_department() {
		return contacts_department;
	}

	public void setcontacts_department(String contacts_department) {
		this.contacts_department = contacts_department;
	}

	public String getcontacts_email1_address() {
		return contacts_email1_address;
	}

	public void setcontacts_email1_address(String contacts_email1_address) {
		this.contacts_email1_address = contacts_email1_address;
	}

	public String getcontacts_email2_address() {
		return contacts_email2_address;
	}

	public void setcontacts_email2_address(String contacts_email2_address) {
		this.contacts_email2_address = contacts_email2_address;
	}

	public String getcontacts_email3_address() {
		return contacts_email3_address;
	}

	public void setcontacts_email3_address(String contacts_email3_address) {
		this.contacts_email3_address = contacts_email3_address;
	}

	public String getcontacts_file_as() {
		return contacts_file_as;
	}

	public void setcontacts_file_as(String contacts_file_as) {
		this.contacts_file_as = contacts_file_as;
	}

	public String getcontacts_first_name() {
		return contacts_first_name;
	}

	public void setcontacts_first_name(String contacts_first_name) {
		this.contacts_first_name = contacts_first_name;
	}

	public String getcontacts_home2_telephone_number() {
		return contacts_home2_telephone_number;
	}

	public void setcontacts_home2_telephone_number(
			String contacts_home2_telephone_number) {
		this.contacts_home2_telephone_number = contacts_home2_telephone_number;
	}

	public String getcontacts_home_address_city() {
		return contacts_home_address_city;
	}

	public void setcontacts_home_address_city(String contacts_home_address_city) {
		this.contacts_home_address_city = contacts_home_address_city;
	}

	public String getcontacts_home_address_country() {
		return contacts_home_address_country;
	}

	public void setcontacts_home_address_country(
			String contacts_home_address_country) {
		this.contacts_home_address_country = contacts_home_address_country;
	}

	public String getcontacts_home_address_postal_code() {
		return contacts_home_address_postal_code;
	}

	public void setcontacts_home_address_postal_code(
			String contacts_home_address_postal_code) {
		this.contacts_home_address_postal_code = contacts_home_address_postal_code;
	}

	public String getcontacts_home_address_state() {
		return contacts_home_address_state;
	}

	public void setcontacts_home_address_state(
			String contacts_home_address_state) {
		this.contacts_home_address_state = contacts_home_address_state;
	}

	public String getcontacts_home_address_street() {
		return contacts_home_address_street;
	}

	public void setcontacts_home_address_street(
			String contacts_home_address_street) {
		this.contacts_home_address_street = contacts_home_address_street;
	}

	public String getcontacts_home_fax_number() {
		return contacts_home_fax_number;
	}

	public void setcontacts_home_fax_number(String contacts_home_fax_number) {
		this.contacts_home_fax_number = contacts_home_fax_number;
	}

	public String getcontacts_home_telephone_number() {
		return contacts_home_telephone_number;
	}

	public void setcontacts_home_telephone_number(
			String contacts_home_telephone_number) {
		this.contacts_home_telephone_number = contacts_home_telephone_number;
	}

	public String getcontacts_job_title() {
		return contacts_job_title;
	}

	public void setcontacts_job_title(String contacts_job_title) {
		this.contacts_job_title = contacts_job_title;
	}

	public String getcontacts_last_name() {
		return contacts_last_name;
	}

	public void setcontacts_last_name(String contacts_last_name) {
		this.contacts_last_name = contacts_last_name;
	}

	public String getcontacts_middle_name() {
		return contacts_middle_name;
	}

	public void setcontacts_middle_name(String contacts_middle_name) {
		this.contacts_middle_name = contacts_middle_name;
	}

	public String getcontacts_mobile_telephone_number() {
		return contacts_mobile_telephone_number;
	}

	public void setcontacts_mobile_telephone_number(
			String contacts_mobile_telephone_number) {
		this.contacts_mobile_telephone_number = contacts_mobile_telephone_number;
	}

	public String getcontacts_office_location() {
		return contacts_office_location;
	}

	public void setcontacts_office_location(String contacts_office_location) {
		this.contacts_office_location = contacts_office_location;
	}

	public String getcontacts_other_address_city() {
		return contacts_other_address_city;
	}

	public void setcontacts_other_address_city(
			String contacts_other_address_city) {
		this.contacts_other_address_city = contacts_other_address_city;
	}

	public String getcontacts_other_address_country() {
		return contacts_other_address_country;
	}

	public void setcontacts_other_address_country(
			String contacts_other_address_country) {
		this.contacts_other_address_country = contacts_other_address_country;
	}

	public String getcontacts_other_address_postal_code() {
		return contacts_other_address_postal_code;
	}

	public void setcontacts_other_address_postal_code(
			String contacts_other_address_postal_code) {
		this.contacts_other_address_postal_code = contacts_other_address_postal_code;
	}

	public String getcontacts_other_address_state() {
		return contacts_other_address_state;
	}

	public void setcontacts_other_address_state(
			String contacts_other_address_state) {
		this.contacts_other_address_state = contacts_other_address_state;
	}

	public String getcontacts_other_address_street() {
		return contacts_other_address_street;
	}

	public void setcontacts_other_address_street(
			String contacts_other_address_street) {
		this.contacts_other_address_street = contacts_other_address_street;
	}

	public String getcontacts_pager_number() {
		return contacts_pager_number;
	}

	public void setcontacts_pager_number(String contacts_pager_number) {
		this.contacts_pager_number = contacts_pager_number;
	}

	public String getcontacts_radio_telephone_number() {
		return contacts_radio_telephone_number;
	}

	public void setcontacts_radio_telephone_number(
			String contacts_radio_telephone_number) {
		this.contacts_radio_telephone_number = contacts_radio_telephone_number;
	}

	public String getcontacts_spouse() {
		return contacts_spouse;
	}

	public void setcontacts_spouse(String contacts_spouse) {
		this.contacts_spouse = contacts_spouse;
	}

	public String getcontacts_suffix() {
		return contacts_suffix;
	}

	public void setcontacts_suffix(String contacts_suffix) {
		this.contacts_suffix = contacts_suffix;
	}

	public String getcontacts_title() {
		return contacts_title;
	}

	public void setcontacts_title(String contacts_title) {
		this.contacts_title = contacts_title;
	}

	public String getcontacts_webpage() {
		return contacts_webpage;
	}

	public void setcontacts_webpage(String contacts_webpage) {
		this.contacts_webpage = contacts_webpage;
	}

	public String getcontacts_yomi_company_name() {
		return contacts_yomi_company_name;
	}

	public void setcontacts_yomi_company_name(String contacts_yomi_company_name) {
		this.contacts_yomi_company_name = contacts_yomi_company_name;
	}

	public String getcontacts_yomi_first_name() {
		return contacts_yomi_first_name;
	}

	public void setcontacts_yomi_first_name(String contacts_yomi_first_name) {
		this.contacts_yomi_first_name = contacts_yomi_first_name;
	}

	public String getcontacts_yomi_last_name() {
		return contacts_yomi_last_name;
	}

	public void setcontacts_yomi_last_name(String contacts_yomi_last_name) {
		this.contacts_yomi_last_name = contacts_yomi_last_name;
	}

	public String getcontacts_compressed_rtf() {
		return contacts_compressed_rtf;
	}

	public void setcontacts_compressed_rtf(String contacts_compressed_rtf) {
		this.contacts_compressed_rtf = contacts_compressed_rtf;
	}

	public String getcontacts_picture() {
		return contacts_picture;
	}

	public void setcontacts_picture(String contacts_picture) {
		this.contacts_picture = contacts_picture;
	}

	public String getContacts_name_prefix() {
		return contacts_name_prefix;
	}

	public void setContacts_name_prefix(String contacts_name_prefix) {
		this.contacts_name_prefix = contacts_name_prefix;
	}

	public String getContacts_name_suffix() {
		return contacts_name_suffix;
	}

	public void setContacts_name_suffix(String contacts_name_suffix) {
		this.contacts_name_suffix = contacts_name_suffix;
	}

	public String getContact_phonetic_family_name() {
		return contact_phonetic_family_name;
	}

	public void setContact_phonetic_family_name(
			String contact_phonetic_family_name) {
		this.contact_phonetic_family_name = contact_phonetic_family_name;
	}

	public String getContact_phonetic_middle_name() {
		return contact_phonetic_middle_name;
	}

	public void setContact_phonetic_middle_name(
			String contact_phonetic_middle_name) {
		this.contact_phonetic_middle_name = contact_phonetic_middle_name;
	}

	public String getContact_phonetic_given_name() {
		return contact_phonetic_given_name;
	}

	public void setContact_phonetic_given_name(
			String contact_phonetic_given_name) {
		this.contact_phonetic_given_name = contact_phonetic_given_name;
	}

	public String getContact_phone_number_mobile_type1() {
		return contact_phone_number_mobile_type1;
	}

	public void setContact_phone_number_mobile_type1(
			String contact_phone_number_mobile_type1) {
		this.contact_phone_number_mobile_type1 = contact_phone_number_mobile_type1;
	}

	public String getContact_phone_number_mobile_type2() {
		return contact_phone_number_mobile_type2;
	}

	public void setContact_phone_number_mobile_type2(
			String contact_phone_number_mobile_type2) {
		this.contact_phone_number_mobile_type2 = contact_phone_number_mobile_type2;
	}

	public String getContact_email_type() {
		return contact_email_type;
	}

	public void setContact_email_type(String contact_email_type) {
		this.contact_email_type = contact_email_type;
	}

	public String getContact_business_location_type() {
		return contact_business_location_type;
	}

	public void setContact_business_location_type(
			String contact_business_location_type) {
		this.contact_business_location_type = contact_business_location_type;
	}

	public String getContact_nick_name() {
		return contact_nick_name;
	}

	public void setContact_nick_name(String contact_nick_name) {
		this.contact_nick_name = contact_nick_name;
	}

	public String getContact_website() {
		return contact_website;
	}

	public void setContact_website(String contact_website) {
		this.contact_website = contact_website;
	}

	public String getContact_internetcall() {
		return contact_internetcall;
	}

	public void setContact_internetcall(String contact_internetcall) {
		this.contact_internetcall = contact_internetcall;
	}

		
	public byte[] getContacts_image() {
		return contacts_image;
	}

	public void setContacts_image(byte[] contacts_image) {
		this.contacts_image = contacts_image;
	}

	public int getContact_isFavorite() {
		return contact_isFavorite;
	}

	public void setContact_isFavorite(int contact_isFavorite) {
		this.contact_isFavorite = contact_isFavorite;
	}

	public boolean isNativeContact() {
		return isNativeContact;
	}

	public void setNativeContact(boolean isNativeContact) {
		this.isNativeContact = isNativeContact;
	}

	public boolean isContactfromSearch() {
		return isSearchContact;
	}

	public void setSearchContact(boolean searchContact) {
		this.isSearchContact = searchContact;
	}

	public String getContact_notes() {
		return contact_notes;
	}

	public void setContact_notes(String contact_notes) {
		this.contact_notes = contact_notes;
	}

	public String getContacts_assistant_telephone_number_type() {
		return contacts_assistant_telephone_number_type;
	}

	public void setContacts_assistant_telephone_number_type(
			String contacts_assistant_telephone_number_type) {
		this.contacts_assistant_telephone_number_type = contacts_assistant_telephone_number_type;
	}

	public String getContacts_business2_telephone_number_type() {
		return contacts_business2_telephone_number_type;
	}

	public void setContacts_business2_telephone_number_type(
			String contacts_business2_telephone_number_type) {
		this.contacts_business2_telephone_number_type = contacts_business2_telephone_number_type;
	}

	public String getContacts_business_fax_number_type() {
		return contacts_business_fax_number_type;
	}

	public void setContacts_business_fax_number_type(
			String contacts_business_fax_number_type) {
		this.contacts_business_fax_number_type = contacts_business_fax_number_type;
	}

	public String getContacts_car_telephone_numbe_type() {
		return contacts_car_telephone_numbe_type;
	}

	public void setContacts_car_telephone_numbe_type(
			String contacts_car_telephone_numbe_type) {
		this.contacts_car_telephone_numbe_type = contacts_car_telephone_numbe_type;
	}

	public String getContacts_home2_telephone_number_type() {
		return contacts_home2_telephone_number_type;
	}

	public void setContacts_home2_telephone_number_type(
			String contacts_home2_telephone_number_type) {
		this.contacts_home2_telephone_number_type = contacts_home2_telephone_number_type;
	}

	public String getContacts_home_fax_number_type() {
		return contacts_home_fax_number_type;
	}

	public void setContacts_home_fax_number_type(
			String contacts_home_fax_number_type) {
		this.contacts_home_fax_number_type = contacts_home_fax_number_type;
	}

	public String getContacts_home_telephone_number_type() {
		return contacts_home_telephone_number_type;
	}

	public void setContacts_home_telephone_number_type(
			String contacts_home_telephone_number_type) {
		this.contacts_home_telephone_number_type = contacts_home_telephone_number_type;
	}

	public String getContacts_radio_telephone_number_type() {
		return contacts_radio_telephone_number_type;
	}

	public void setContacts_radio_telephone_number_type(
			String contacts_radio_telephone_number_type) {
		this.contacts_radio_telephone_number_type = contacts_radio_telephone_number_type;
	}

	public String getContacts2_company_main_phone_type() {
		return contacts2_company_main_phone_type;
	}

	public void setContacts2_company_main_phone_type(
			String contacts2_company_main_phone_type) {
		this.contacts2_company_main_phone_type = contacts2_company_main_phone_type;
	}

	public String getContact_custom_phone1() {
		return contact_custom_phone1;
	}

	public void setContact_custom_phone1(String contact_custom_phone1) {
		this.contact_custom_phone1 = contact_custom_phone1;
	}

	public String getContact_custom_phone1_type() {
		return contact_custom_phone1_type;
	}

	public void setContact_custom_phone1_type(String contact_custom_phone1_type) {
		this.contact_custom_phone1_type = contact_custom_phone1_type;
	}

	public String getContact_custom_phone2() {
		return contact_custom_phone2;
	}

	public void setContact_custom_phone2(String contact_custom_phone2) {
		this.contact_custom_phone2 = contact_custom_phone2;
	}

	public String getContact_custom_phone2_type() {
		return contact_custom_phone2_type;
	}

	public void setContact_custom_phone2_type(String contact_custom_phone2_type) {
		this.contact_custom_phone2_type = contact_custom_phone2_type;
	}

	public String getContact_custom_phone3() {
		return contact_custom_phone3;
	}

	public void setContact_custom_phone3(String contact_custom_phone3) {
		this.contact_custom_phone3 = contact_custom_phone3;
	}

	public String getContact_custom_phone3_type() {
		return contact_custom_phone3_type;
	}

	public void setContact_custom_phone3_type(String contact_custom_phone3_type) {
		this.contact_custom_phone3_type = contact_custom_phone3_type;
	}

	public String getContacts_pager_number_type() {
		return contacts_pager_number_type;
	}

	public void setContacts_pager_number_type(String contacts_pager_number_type) {
		this.contacts_pager_number_type = contacts_pager_number_type;
	}

	public String getContacts2_company_main_phone() {
		return contacts2_company_main_phone;
	}

	public void setContacts2_company_main_phone(
			String contacts2_company_main_phone) {
		this.contacts2_company_main_phone = contacts2_company_main_phone;
	}

	public String getContact_email1_type() {
		return contact_email1_type;
	}

	public void setContact_email1_type(String contact_email1_type) {
		this.contact_email1_type = contact_email1_type;
	}

	public String getContact_email2_type() {
		return contact_email2_type;
	}

	public void setContact_email2_type(String contact_email2_type) {
		this.contact_email2_type = contact_email2_type;
	}

	public String getContact_email3_type() {
		return contact_email3_type;
	}

	public void setContact_email3_type(String contact_email3_type) {
		this.contact_email3_type = contact_email3_type;
	}
	public String getContacts_custom_email1_address() {
		return contacts_custom_email1_address;
	}

	public void setContacts_custom_email1_address(
			String contacts_custom_email1_address) {
		this.contacts_custom_email1_address = contacts_custom_email1_address;
	}

	public String getContacts_custom_email2_address() {
		return contacts_custom_email2_address;
	}

	public void setContacts_custom_email2_address(
			String contacts_custom_email2_address) {
		this.contacts_custom_email2_address = contacts_custom_email2_address;
	}

	public String getContacts_custom_email3_address() {
		return contacts_custom_email3_address;
	}

	public void setContacts_custom_email3_address(
			String contacts_custom_email3_address) {
		this.contacts_custom_email3_address = contacts_custom_email3_address;
	}
	public String getContact_custom_email1_type() {
		return contact_custom_email1_type;
	}

	public void setContact_custom_email1_type(String contact_custom_email1_type) {
		this.contact_custom_email1_type = contact_custom_email1_type;
	}

	public String getContact_custom_email2_type() {
		return contact_custom_email2_type;
	}

	public void setContact_custom_email2_type(String contact_custom_email2_type) {
		this.contact_custom_email2_type = contact_custom_email2_type;
	}

	public String getContact_custom_email3_type() {
		return contact_custom_email3_type;
	}

	public void setContact_custom_email3_type(String contact_custom_email3_type) {
		this.contact_custom_email3_type = contact_custom_email3_type;
	}

	public String getContact_im_address() {
		return contact_im_address;
	}

	public void setContact_im_address(String contact_im_address) {
		this.contact_im_address = contact_im_address;
	}

	public String getContact_im_address1() {
		return contact_im_address1;
	}

	public void setContact_im_address1(String contact_im_address1) {
		this.contact_im_address1 = contact_im_address1;
	}

	public String getContact_im_address2() {
		return contact_im_address2;
	}

	public void setContact_im_address2(String contact_im_address2) {
		this.contact_im_address2 = contact_im_address2;
	}

	public String getContact_custom_im_address() {
		return contact_custom_im_address;
	}

	public void setContact_custom_im_address(String contact_custom_im_address) {
		this.contact_custom_im_address = contact_custom_im_address;
	}

	public String getContact_custom_im1_address() {
		return contact_custom_im1_address;
	}

	public void setContact_custom_im1_address(String contact_custom_im1_address) {
		this.contact_custom_im1_address = contact_custom_im1_address;
	}

	public String getContact_custom_im2_address() {
		return contact_custom_im2_address;
	}

	public void setContact_custom_im2_address(String contact_custom_im2_address) {
		this.contact_custom_im2_address = contact_custom_im2_address;
	}

	public String getContact_im_address_type() {
		return contact_im_address_type;
	}

	public void setContact_im_address_type(String contact_im_address_type) {
		this.contact_im_address_type = contact_im_address_type;
	}

	public String getContact_im_address1_type() {
		return contact_im_address1_type;
	}

	public void setContact_im_address1_type(String contact_im_address1_type) {
		this.contact_im_address1_type = contact_im_address1_type;
	}

	public String getContact_im_address2_type() {
		return contact_im_address2_type;
	}

	public void setContact_im_address2_type(String contact_im_address2_type) {
		this.contact_im_address2_type = contact_im_address2_type;
	}

	public String getContact_custom_im_address_type() {
		return contact_custom_im_address_type;
	}

	public void setContact_custom_im_address_type(
			String contact_custom_im_address_type) {
		this.contact_custom_im_address_type = contact_custom_im_address_type;
	}

	public String getContact_custom_im1_address_type() {
		return contact_custom_im1_address_type;
	}

	public void setContact_custom_im1_address_type(
			String contact_custom_im1_address_type) {
		this.contact_custom_im1_address_type = contact_custom_im1_address_type;
	}

	public String getContact_custom_im2_address_type() {
		return contact_custom_im2_address_type;
	}

	public void setContact_custom_im2_address_type(
			String contact_custom_im2_address_type) {
		this.contact_custom_im2_address_type = contact_custom_im2_address_type;
	}
	

	public String getContacts_ringtone_uri() {
		return contacts_ringtone_uri;
	}

	public void setContacts_ringtone_uri(String contacts_ringtone_uri) {
		this.contacts_ringtone_uri = contacts_ringtone_uri;
	}
	

	public String getContacts_business_location() {
		return contacts_business_location;
	}

	public void setContacts_business_location(String contacts_business_location) {
		this.contacts_business_location = contacts_business_location;
	}

	public String getContacts_home_location() {
		return contacts_home_location;
	}

	public void setContacts_home_location(String contacts_home_location) {
		this.contacts_home_location = contacts_home_location;
	}

	public String getContacts_other_location() {
		return contacts_other_location;
	}

	public void setContacts_other_location(String contacts_other_location) {
		this.contacts_other_location = contacts_other_location;
	}

	public String getContact_home_location_type() {
		return contact_home_location_type;
	}

	public void setContact_home_location_type(String contact_home_location_type) {
		this.contact_home_location_type = contact_home_location_type;
	}

	public String getContact_other_location_type() {
		return contact_other_location_type;
	}

	public void setContact_other_location_type(String contact_other_location_type) {
		this.contact_other_location_type = contact_other_location_type;
	}
}