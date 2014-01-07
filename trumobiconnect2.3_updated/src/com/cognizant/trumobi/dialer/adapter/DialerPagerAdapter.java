package com.cognizant.trumobi.dialer.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.cognizant.trumobi.dialer.fragments.DialerCallLogFragment;
import com.cognizant.trumobi.dialer.fragments.DialerContactFragment;
import com.cognizant.trumobi.dialer.fragments.DialerDialPadFragment;
import com.cognizant.trumobi.log.DialerLog;

public class DialerPagerAdapter extends FragmentPagerAdapter {

	final int PAGE_COUNT = 3;

	/** Constructor of the class */
	public DialerPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	/** This method will be invoked when a page is requested to create */
	@Override
	public Fragment getItem(int arg0) {
		// Bundle data = new Bundle();
		DialerLog.e("Tab Position", "in getItem--->"+arg0);
		switch (arg0) {

		/** tab1 is selected */
		case 0:
			DialerDialPadFragment fragment = new DialerDialPadFragment();
			return fragment;

			/** tab2 is selected */
		case 1:
			DialerCallLogFragment callLogFragment = new DialerCallLogFragment();
			return callLogFragment;

			/** tab3 is selected */
		case 2:
			
			DialerContactFragment contactFragment = new DialerContactFragment();
			return contactFragment;

		/*	*//** tab4 is selected *//*
		case 3:
			DialerFavoriteFragment favoriteFragment = new DialerFavoriteFragment();
			return favoriteFragment;*/
		}

		return null;
	}

	
	

	/*@Override
	public Object instantiateItem(ViewGroup container, int position) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = (LayoutInflater) container.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		int pagerLayout = 0;
		switch (position) {
		case 0:
			pagerLayout=R.layout.dial_dialer;
			break;
		case 1:
			pagerLayout=R.layout.dial_call_log;
			break;
		case 2:
			pagerLayout=R.layout.contact_list;
			break;
		case 3:
			pagerLayout=R.layout.contacts_favorite_grid_view;
			break;
		}
		View v=inflater.inflate(pagerLayout, null);
		((ViewPager) container).addView(v, 0);
		return v;
	}*/
	/** Returns the number of pages */
	@Override
	public int getCount() {
		return PAGE_COUNT;
	}

	

}
