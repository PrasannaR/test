package com.cognizant.trumobi.persona.net;

import java.util.LinkedList;

import com.google.gson.annotations.SerializedName;
public class PersonaAllAppsDetails {

	@SerializedName("appstore_app_items")
	public LinkedList<PersonaAllAppsListDetails> appstore_app_items;


}