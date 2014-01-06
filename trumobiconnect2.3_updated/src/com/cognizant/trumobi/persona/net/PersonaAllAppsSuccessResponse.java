package com.cognizant.trumobi.persona.net;

import com.google.gson.annotations.SerializedName;

public class PersonaAllAppsSuccessResponse {
	@SerializedName("appstore_information")
	public PersonaAllAppsDetails appstore_information;
	@SerializedName("display_message")
	public String display_message;
	@SerializedName("message_status")
	public String message_status;
}
