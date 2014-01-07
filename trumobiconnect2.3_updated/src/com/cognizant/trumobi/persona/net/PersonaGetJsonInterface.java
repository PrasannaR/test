package com.cognizant.trumobi.persona.net;

import java.util.ArrayList;

import android.os.Message;

public interface PersonaGetJsonInterface {

		public void onRemoteCallComplete(Message json,int code);
		public void onRemoteCallComplete(String json, int code);
		public void onRemoteCallComplete(ArrayList<String> json, int code);
		public void onRemoteCallComplete(String[] json, int code);
}
