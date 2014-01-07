package com.cognizant.trumobi.persona.net;


import com.google.gson.annotations.SerializedName;

public class PersonaLoginResponse {

	@SerializedName("display_message")
	public String display_message;

	@SerializedName("appstore_information")
	public PersonaLoginDetails appstore_information;

	@SerializedName("message_status")
	public String message_status;

	@SerializedName("appstore_request")
	public PersonaLoginAppStoreRequest appstore_request;
}
