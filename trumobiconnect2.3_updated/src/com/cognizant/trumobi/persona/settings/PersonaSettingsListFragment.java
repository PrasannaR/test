package com.cognizant.trumobi.persona.settings;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
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
import android.widget.TextView;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.log.PersonaLog;

public class PersonaSettingsListFragment extends Fragment {
	PersonaSettingsListAdapter personaSettingsListAdapter;
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
	  PersonaLog.v("ListFragment", "onActivityCreated().");
	  PersonaLog.v("ListsavedInstanceState", savedInstanceState == null ? "true" : "false");
	   
	  //Generate list View from ArrayList
	  displayListView();
	   
	 }
	
	public interface onSettingsMenuItemListener {
        public void onSettingsMenuItemSelected(String URL);
    }
	 private void displayListView() {
		 
		  //Array list of countries
		  List<String> settingsMenu = new ArrayList<String>();
		  settingsMenu.add("Change PIN");
		  settingsMenu.add("Auto Lock");
		 // settingsMenu.add("http://maps.google.com");
		   
		  //create an ArrayAdaptar from the String Array
		  personaSettingsListAdapter = new PersonaSettingsListAdapter(getActivity(), settingsMenu);
		  ListView listView = (ListView) getView().findViewById(R.id.listofURLs);
	
		  // Assign adapter to ListView
		  listView.setAdapter(personaSettingsListAdapter);
		/*  ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
					R.layout.pr_settings_menu_row,settingsMenu);
					ListView listView = (ListView) getView().findViewById(R.id.listofURLs);

					 listView.setAdapter(dataAdapter);
		  
		   */
		  //enables filtering for the contents of the given ListView
		  listView.setTextFilterEnabled(true);
		final  int deviceType=getResources().getInteger(R.integer.deviceType);
	  listView.setOnItemClickListener(new OnItemClickListener() {
		   public void onItemClick(AdapterView<?> parent, 
				   View view,
		     int position, long id) {
		    // Send the URL to the host activity
			  
			  // ((TextView)view).setBackground(getResources().getDrawable(R.drawable.pr_list_background));
			//   view.setBackground(getResources().getDrawable(R.drawable.pr_list_selected_bg));
			   if(deviceType==1)
			   {
			   if(position!=0){
				   selectedItem=1;
				   personaSettingsListAdapter.notifyDataSetChanged();
			   }
			   else{
				   selectedItem=0;
			   personaSettingsListAdapter.notifyDataSetChanged();
			   }
			   }
			   mListener.onSettingsMenuItemSelected(((TextView) view).getText().toString());
		   }
		  });
		  
		  listView.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long arg3) {
				// TODO Auto-generated method stub
				view.setBackgroundDrawable(getResources().getDrawable(R.drawable.pr_list_selected_bg));
				   mListener.onSettingsMenuItemSelected(((TextView) view).getText().toString());
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				parent.setBackgroundDrawable(getResources().getDrawable(R.drawable.pr_panel_bg));
			}
		});
		 }
	 
	 @Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container,
	   Bundle savedInstanceState) {
	  PersonaLog.v("ListFragment", "onCreateView()");
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
