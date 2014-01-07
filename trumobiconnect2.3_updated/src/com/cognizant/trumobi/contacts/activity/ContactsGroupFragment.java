package com.cognizant.trumobi.contacts.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.log.ContactsLog;

public class ContactsGroupFragment extends SherlockFragment {

	public static final String TAG = "GroupContactsFragment";

	@Override
	public SherlockFragmentActivity getSherlockActivity() {
		return super.getSherlockActivity();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Get the view from fragmenttab1.xml
		View view = inflater.inflate(R.layout.contacts_groupcontacts, container, false);
		
		view.setFilterTouchesWhenObscured(true);
		
		return view;
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onPrepareOptionsMenu(menu);

		MenuItem edit = menu.findItem(R.id.menu_edit);
		if (edit != null) {
			edit.setVisible(false);
		}

		MenuItem del = menu.findItem(R.id.menu_delete);
		if (del != null) {
			del.setVisible(false);
		}

		MenuItem tone = menu.findItem(R.id.menu_SetRingtone);
		if (tone != null) {
			tone.setVisible(false);
		}

		MenuItem share = menu.findItem(R.id.menu_share);
		if (share != null) {
			share.setVisible(false);
		}

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		setUserVisibleHint(true);
	}

}
