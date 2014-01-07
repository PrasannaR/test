package com.cognizant.trumobi.persona;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;



public class PersonaRetriveCalendarEvents extends AsyncTask<Integer, Integer, Integer> {

	Context ctx;
	ArrayList<ContentValues> eventsList;
	PersonaSyncReceiver personaProvider;
	@Override
	protected Integer doInBackground(Integer... params) {
		personaProvider = new PersonaSyncReceiver();
		 eventsList = personaProvider.getValuesFromDB();
		 if (eventsList != null && !eventsList.isEmpty()) {
			String endTime = eventsList.get(0).getAsString("end_time");
			personaProvider.startAlarmServiceForEventEndTime(ctx,endTime);
			
		 }
		return 0;
	}
	@Override
	protected void onPostExecute(Integer result) {
		
		super.onPostExecute(result);
		personaProvider.updateWidgetPictureAndButtonListener(ctx,eventsList);
		
	}
	
	public PersonaRetriveCalendarEvents(Context ctx) {
		this.ctx = ctx;
	}
}