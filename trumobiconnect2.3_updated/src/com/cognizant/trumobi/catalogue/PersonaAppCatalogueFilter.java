package com.cognizant.trumobi.catalogue;

import android.content.SharedPreferences;

public class PersonaAppCatalogueFilter {

	private PersonaAppCatalogueFilters.Catalogue mCatalogue;

	public PersonaAppCatalogueFilter() {
		this(PersonaAppGroupAdapter.APP_GROUP_ALL);
	}

	public PersonaAppCatalogueFilter(int index) {
		setCurrentGroupIndex(index);
	}

	public boolean checkAppInGroup(String className) {
		boolean result = true;
		if (mCatalogue != null) {
			final SharedPreferences prefs = mCatalogue.getPreferences();
			if (prefs != null)
				result = prefs.getBoolean(className, false);
		}
		return result;
	}

	public boolean isUserGroup() {
		return mCatalogue != null;
	}

	public int getCurrentFilterIndex() {
		if (mCatalogue != null)
			return mCatalogue.getIndex();
		else
			return PersonaAppGroupAdapter.APP_GROUP_ALL;
	}

	public synchronized void setCurrentGroupIndex(int index) {
		if (index != getCurrentFilterIndex()) {
			mCatalogue = PersonaAppCatalogueFilters.getInstance().getCatalogue(index);
		}
	}


}
