package com.cognizant.trumobi.persona.net;

import com.google.gson.annotations.SerializedName;

public class PersonaUser_Certificate_Details {

	@SerializedName("ca_request_id")
	public String ca_request_id;

	@SerializedName("certificate_pwd")
	public String certificate_pwd;

	@SerializedName("ldap_user_id")
	public String ldap_user_id;

	@SerializedName("user_cert")
	public String user_cert;
	
	@SerializedName("user_cert_path")
	public String user_cert_path;

	@SerializedName("user_email")
	public String user_email;
	
	@SerializedName("certificate_request_id")
	public int certificate_request_id;

}
