package com.cognizant.trumobi.persona.settings;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.TruBoxSDK.SharedPreferences;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.log.PersonaLog;
import com.cognizant.trumobi.persona.PersonaMainActivity;
import com.cognizant.trumobi.persona.PersonaSecurityProfileUpdate;
import com.cognizant.trumobi.persona.constants.PersonaConstants;

public class PersonaSettingsListFragment extends Fragment {
	PersonaSettingsListAdapter personaSettingsListAdapter;
	private String TAG = PersonaSettingsListFragment.class.getSimpleName();
	onSettingsMenuItemListener mListener;
	public static int selectedItem=0;
	@Override
	 public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	  PersonaLog.v("ListFragment", "onCreate()");
	 }
	
	@Override
	 public void onActivityCreated(Bundle savedInstanceState) {
	  super.onActivityCreated(savedInstanceState);
	  PersonaLog.d("ListFragment", "onActivityCreated().");
	  PersonaLog.v("ListsavedInstanceState", savedInstanceState == null ? "true" : "false");
	//  initialiseClickListeners();
	  //Generate list View from ArrayList
	  displayListView();
	   
	 }
	
	public interface onSettingsMenuItemListener {
        public void onSettingsMenuItemSelected(String URL);
    }
	 private void displayListView() {
		 
		 PersonaLog.d(TAG,"***** In displayListView *****");
		  //Array list of countries
		  List<String> settingsMenu = new ArrayList<String>();
		  int authtype = new SharedPreferences(getActivity()).getInt("authtype", PersonaConstants.AUTH_TYPE_NUMERIC);
		  switch(authtype) {
		  case PersonaConstants.AUTH_TYPE_NUMERIC:
			  settingsMenu.add("Change PIN");
			  break;
		  default:
			  settingsMenu.add("Change password");
			  break;
		  }
		  settingsMenu.add("Auto Lock");
		  if(PersonaMainActivity.isRovaPoliciesOn)
		  settingsMenu.add("Update Settings");
		  settingsMenu.add("Bug Sense"); //BUG_SENSE - 27Dec2013
		
		   
		  //create an ArrayAdaptar from the String Array
		  personaSettingsListAdapter = new PersonaSettingsListAdapter(getActivity(), settingsMenu);
		  ListView listView = (ListView) getView().findViewById(R.id.listofURLs);
	
		  // Assign adapter to ListView
		  listView.setAdapter(personaSettingsListAdapter);
		
		  //enables filtering for the contents of the given ListView
		  listView.setTextFilterEnabled(true);
		final  int deviceType=getResources().getInteger(R.integer.deviceType);
	  listView.setOnItemClickListener(new OnItemClickListener() {
		   public void onItemClick(AdapterView<?> parent, 
				   View view,
		     int position, long id) {
		    // Send the URL to the host activity
			  
			   PersonaLog.d(TAG,"--------- itemclick position ------" + position);

			   if(deviceType==1)
			   {
				 //BUG_SENSE - 27Dec2013
				  
					   selectedItem=position;
					   PersonaLog.d(TAG,"--------- selectedItem ------" + selectedItem);
				   personaSettingsListAdapter.notifyDataSetChanged();
				 //BUG_SENSE - 27Dec2013
			   }
			   PersonaLog.d(TAG,"----- ((TextView) view).getText().toString()"+((TextView) view).getText().toString());
			   mListener.onSettingsMenuItemSelected(((TextView) view).getText().toString());
		   }
		  });
		  
	  		PersonaLog.d(TAG,"***** out displayListView *****");
		 }
	 
	 @Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container,
	   Bundle savedInstanceState) {
	  PersonaLog.d("ListFragment", "onCreateView()");
	  PersonaLog.v("ListContainer", container == null ? "true" : "false");
	  PersonaLog.v("ListsavedInstanceState", savedInstanceState == null ? "true" : "false");
	  if (container == null) {
	            return null;
	        }
	  View view = inflater.inflate(R.layout.pr_settings_menu_item_listview, container, false);
	  return view;
	 }
	 
	 @Override
	    public void onAttach(Activity activity) {
	        super.onAttach(activity);
	        try {
	            mListener = (onSettingsMenuItemListener) activity;
	        } catch (ClassCastException e) {
	            throw new ClassCastException(activity.toString() + " must implement OnURLSelectedListener");
	        }
	    }
		
		public PersonaSettingsListFragment() {
		// TODO Auto-generated constructor stub
	}
}
