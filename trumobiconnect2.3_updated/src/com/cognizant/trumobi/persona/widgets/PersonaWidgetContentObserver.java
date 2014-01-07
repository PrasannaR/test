package com.cognizant.trumobi.persona.widgets;

import android.database.ContentObserver;
import android.os.Handler;

import com.cognizant.trumobi.log.PersonaLog;

/**
 * 
 * @author Koxx
 * 
 */
public class PersonaWidgetContentObserver extends ContentObserver {

	private static final String TAG = "PersonaWidgetContentObserver";

	private static final boolean LOGD = true;

	PersonaWidgetDataChangeListener personaWidgetDataChangeListener;

	public PersonaWidgetContentObserver(Handler handler, PersonaWidgetDataChangeListener dataChangeListener_p) {
		super(handler);
		personaWidgetDataChangeListener = dataChangeListener_p;
	}

	public void onChange(boolean selfChange) {

		if (personaWidgetDataChangeListener != null) {
			if (LOGD)
				PersonaLog.d(TAG, "onChange");
			personaWidgetDataChangeListener.onChange();
		} else {
			if (LOGD)
				PersonaLog.d(TAG, "onChange -> no listerner");
		}

	}

}
