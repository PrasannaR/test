package com.cognizant.trumobi.contacts.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.cognizant.trumobi.contacts.activity.ContactsAllContactsFragment;
import com.cognizant.trumobi.contacts.activity.ContactsFavoritesFragment;
import com.cognizant.trumobi.contacts.activity.ContactsGroupFragment;

public class ContactPageAdapter extends FragmentPagerAdapter {

	// Declare the number of ViewPager pages
	final int PAGE_COUNT = 3;
		ContactsGroupFragment grpCtcFragment = null;
	ContactsAllContactsFragment allCtcFragment = null;
	ContactsFavoritesFragment favoritesFragment = null;

	public ContactPageAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int arg0) {
		
		switch (arg0) {
		
		case 0:
			if (grpCtcFragment == null) {
				grpCtcFragment = new ContactsGroupFragment();
			}
			return grpCtcFragment;

		case 1:
			if (allCtcFragment == null) {
				allCtcFragment = new ContactsAllContactsFragment();
			}
			return allCtcFragment;

		case 2:
			if (favoritesFragment == null) {
				favoritesFragment = new ContactsFavoritesFragment();
			}
			return favoritesFragment;
		}
		return null;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return PAGE_COUNT;
	}

}