package com.cognizant.trumobi.persona.net;

import com.google.gson.annotations.SerializedName;

public class PersonaAppinfoResponse {
	

		@SerializedName("app_id")
		public int app_id;
	
		@SerializedName("app_size")
		public String app_size;
		
		@SerializedName("binary_source_path")
		public String binary_source_path;

		@SerializedName("version_upgrade")
		public int version_upgrade;
		
		@SerializedName("message_status")
		public String message_status;
		
		@SerializedName("app_version")
		public String app_version;
}


