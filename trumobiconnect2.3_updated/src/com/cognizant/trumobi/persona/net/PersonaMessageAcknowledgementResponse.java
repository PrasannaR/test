package com.cognizant.trumobi.persona.net;

import com.google.gson.annotations.SerializedName;

public class PersonaMessageAcknowledgementResponse {

	@SerializedName("appstore_information")
	public PersonaDevice_appstore_information appstore_information;

	@SerializedName("appstore_request")
	public PersonaMessageAcknowledgementRequest appstore_request;

	@SerializedName("display_message")
	public String display_message;

	@SerializedName("message_status")
	public String message_status;
}
