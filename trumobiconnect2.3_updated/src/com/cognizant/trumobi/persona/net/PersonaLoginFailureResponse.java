package com.cognizant.trumobi.persona.net;

import com.google.gson.annotations.SerializedName;

public class PersonaLoginFailureResponse {
	@SerializedName("appstore_error_code")
	public String errorCode;
	@SerializedName("appstore_error_description")
	public String errorDescription;
	@SerializedName("display_message")
	public String display_message;
	
}
