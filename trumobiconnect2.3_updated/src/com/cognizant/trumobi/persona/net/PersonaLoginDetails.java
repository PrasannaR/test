package com.cognizant.trumobi.persona.net;

import com.google.gson.annotations.SerializedName;

public class PersonaLoginDetails {

	@SerializedName("appstore_authentication_type")
	public int appstore_authentication_type;

	@SerializedName("appstore_session_id")
	public String appstore_session_id;

	@SerializedName("appstore_user")
	public PersonaLoginAppstoreUser appstore_user;

	@SerializedName("is_appstore_vip_user")
	public boolean is_appstore_vip_user;

	@SerializedName("is_device_enrolled")
	public boolean is_device_enrolled;

	@SerializedName("is_owner")
	public boolean is_owner;

	@SerializedName("is_pending_push_messages")
	public boolean is_pending_push_messages;

	@SerializedName("has_device_token")
	public boolean has_device_token;

	@SerializedName("serial_number")
	public boolean serial_number;

	@SerializedName("device_make")
	public boolean device_make;

	@SerializedName("device_imei")
	public boolean device_imei;

	@SerializedName("ip_address")
	public boolean ip_address;

}
