package com.cognizant.trumobi.contacts.activity;

import java.util.ArrayList;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.cognizant.trubox.contacts.db.ContactsConsts;
import com.cognizant.trubox.contacts.db.ContactsModel;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.contacts.adapter.ContactsFavoriteListAdapter;
import com.cognizant.trumobi.contacts.utils.ContactsUtilities;
import com.cognizant.trumobi.em.Email;

public class ContactsFavoritesFragment extends SherlockFragment {

	public static final String TAG = "FavoritesFragment";
	ContactsFavoriteListAdapter favListAdapter;
	GridView favGrid;

	TextView nofavTxt;
	LinearLayout gridHolder;
	ArrayList<ContactsModel> corporateContacts;
	ArrayList<ContactsModel> mergedContacts;
	ContactsFavAsyncTask contactsFavAsyncTask;

	@Override
	public SherlockFragmentActivity getSherlockActivity() {
		return super.getSherlockActivity();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setHasOptionsMenu(true);
		loadFavContacts();
	}


	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub;
		super.onPrepareOptionsMenu(menu);

		MenuItem settings = menu.findItem(R.id.gsettings);
		if (settings != null) {
			settings.setVisible(true);
		}

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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.contacts_favorite_grid_view, container, false);
		nofavTxt = (TextView)view.findViewById(R.id.nofavorites_txt);
		gridHolder = (LinearLayout) view.findViewById(R.id.gridview_holder);
		favGrid = (GridView) view.findViewById(R.id.fav_grid_view);
		
		loadFavContacts();
		
		view.setFilterTouchesWhenObscured(true);
		
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setRetainInstance(true);
		if(corporateContacts != null){
			if (favListAdapter != null) {
				gridHolder.setVisibility(View.VISIBLE);
				favGrid.setAdapter(favListAdapter);
				favListAdapter.notifyDataSetChanged();
				favGrid.setOnItemClickListener(gridItemClickListener);
			}
		}else{
			favGrid.setAdapter(null);
			nofavTxt.setVisibility(View.VISIBLE);
		}
	}

	
	
	private void loadFavContacts() {
	
		if (contactsFavAsyncTask != null) {
			contactsFavAsyncTask.cancel(true);
		}
		contactsFavAsyncTask = new ContactsFavAsyncTask();
		contactsFavAsyncTask.execute();
	}
	
	private class ContactsFavAsyncTask extends AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... params) {
        	
        	Cursor favCursor = Email.getAppContext().getContentResolver().query(ContactsConsts.CONTENT_URI_CONTACTS, null, 
        			"is_favorite = ?",new String[] { "1" }, null);
        	corporateContacts = ContactsUtilities.PopulateArraylistFromCursor(favCursor, Email.getAppContext());
        	if (favCursor != null) {
				favCursor.close();
			}
              return null;
        }// End of doInBackground method

        @Override
        protected void onPostExecute(Void params) {
            super.onPostExecute(params);
            
            Message msg = new Message();
		    msg.obj = corporateContacts; 
		    //ContactsLog.i("NEW", "-====onPostExecute " + corporateContacts.size());
		    handler.sendMessage(msg);

	    }//End of onPostExecute method
	
		
	}


	private Handler handler = new Handler() {
		@Override
	    public void handleMessage(Message msg) {

			if (corporateContacts != null) {
    			favListAdapter = new ContactsFavoriteListAdapter(Email.getAppContext(), corporateContacts);
    			if(favListAdapter != null){
    				nofavTxt.setVisibility(View.GONE);
        			gridHolder.setVisibility(View.VISIBLE);
        			favGrid.setAdapter(favListAdapter);
        			favListAdapter.notifyDataSetChanged();
        			favGrid.setOnItemClickListener(gridItemClickListener);
    			}
    		}else{
    			favGrid.setAdapter(null);
    			nofavTxt.setVisibility(View.VISIBLE);
    		}
    		
    	/*	if(favCursor != null){
    			favCursor.close();
    		}*/
	    }
	};
	

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		setUserVisibleHint(true);
	}


	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		/*if(contactsFavAsyncTask != null){
			contactsFavAsyncTask.cancel(true);
		}*/
		super.onPause();
	}

	@Override
	public void onResume() {
		loadFavContacts();
		super.onResume();
	};

	
	OnItemClickListener gridItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View child,
				int position, long id) {
			Object obj = favGrid.getItemAtPosition(position);
			ContactsModel contact = (ContactsModel) obj;

			if (ContactsUtilities.isTablet(Email.getAppContext())) {
				Intent myIntent = new Intent(getActivity(),ContactsDialogActivity.class);
				Bundle myBundle = new Bundle();
				myBundle.putSerializable("contact", contact);
				myIntent.putExtra("bundle", myBundle);
				getActivity().startActivity(myIntent);
				
				//new ContactsDialog(getActivity(), contact);
			} else {
				Intent contactDetailIntent = new Intent(Email.getAppContext(),
						ContactsDetailActivity.class);
				ContactsUtilities.SELECTED_ID = null;
				contactDetailIntent.putExtra("obj", contact);
				startActivity(contactDetailIntent);
			}

		}
	};
	
	@Override
	public void onDetach() {

		super.onDetach();
	}
}
