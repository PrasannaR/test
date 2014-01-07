package com.cognizant.trumobi.persona.net;

import com.google.gson.annotations.SerializedName;

public class PersonaAppStoreInfoResponse {

	
	@SerializedName("app_upgrade_info")
	public PersonaAppinfoResponse app_upgrade_info;
	
	
	@SerializedName("require_selective_wipe")
	public boolean require_selective_wipe;
}
