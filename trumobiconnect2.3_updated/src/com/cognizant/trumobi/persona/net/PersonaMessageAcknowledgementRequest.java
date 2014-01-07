package com.cognizant.trumobi.persona.net;

import com.google.gson.annotations.SerializedName;

public class PersonaMessageAcknowledgementRequest {

	@SerializedName("command_uid")
	public String command_uid;

	@SerializedName("comments")
	public String comments;

	@SerializedName("device_id")
	public String device_id;

	@SerializedName("push_command_status")
	public String push_command_status;

	@SerializedName("registration_id")
	public String registration_id;

}
