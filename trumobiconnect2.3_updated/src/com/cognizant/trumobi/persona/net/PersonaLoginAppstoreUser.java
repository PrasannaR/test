package com.cognizant.trumobi.persona.net;

import java.util.Map;

import com.google.gson.annotations.SerializedName;

public class PersonaLoginAppstoreUser {

	@SerializedName("appstore_user_id")
	public String appstore_user_id;
	
	@SerializedName("email_address")
	public String email_address;
	
	@SerializedName("first_name")
	public String first_name;

	@SerializedName("last_modified_date")
	public String last_modified_date;
	
	@SerializedName("last_name")
	public String last_name;
	@SerializedName("language_locale")
	public Map<String,String> language_locale;
	
	
}
