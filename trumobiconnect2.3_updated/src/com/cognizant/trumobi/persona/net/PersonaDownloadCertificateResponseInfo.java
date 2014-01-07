package com.cognizant.trumobi.persona.net;

import com.google.gson.annotations.SerializedName;

public class PersonaDownloadCertificateResponseInfo {

	@SerializedName("aasl")
	public String aasl;

	@SerializedName("email_domain")
	public String email_domain;

	@SerializedName("exchange_server")
	public String exchange_server;

	@SerializedName("use_ssl")
	public String use_ssl;

	@SerializedName("user_certificate_details")
	public PersonaUser_Certificate_Details user_certificate_details;

}
