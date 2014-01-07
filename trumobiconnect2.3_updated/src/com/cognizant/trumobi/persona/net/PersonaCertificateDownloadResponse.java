package com.cognizant.trumobi.persona.net;

import com.google.gson.annotations.SerializedName;

public class PersonaCertificateDownloadResponse {

	@SerializedName("appstore_information")
	public PersonaDownloadCertificateResponseInfo appstore_information;

	@SerializedName("appstore_request")
	public PersonaCertificateDownloadRequest appstore_request;

	@SerializedName("display_message")
	public String display_message;

	@SerializedName("message_status")
	public String message_status;
	
}
