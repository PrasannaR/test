package com.cognizant.trumobi.persona.net;

import com.google.gson.annotations.SerializedName;

public class PersonaOneTimeLoginResponse {

	@SerializedName("appstore_information")
	public PersonaAppStoreInfoResponse appstore_information;

	@SerializedName("display_message")
	public String display_message;

	@SerializedName("message_status")
	public String message_status;

	@SerializedName("appstore_request")
	public PersonaLoginAppStoreRequest appstore_request;

}
