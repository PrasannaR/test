package com.cognizant.trumobi.persona.net;

import com.google.gson.annotations.SerializedName;

public class PersonaLoginAppStoreRequest {

	@SerializedName("device_id")
	public String device_id;

	@SerializedName("device_type")
	public String device_type;

	@SerializedName("model_number")
	public String model_number;

	@SerializedName("os_version")
	public String os_version;

	@SerializedName("platform_name")
	public String platform_name;

}