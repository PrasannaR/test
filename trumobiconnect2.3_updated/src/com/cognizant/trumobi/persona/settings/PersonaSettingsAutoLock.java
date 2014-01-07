package com.cognizant.trumobi.persona.settings;

import java.util.ArrayList;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.TruBoxSDK.SharedPreferences;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobiTimerClass;
import com.cognizant.trumobi.externaladapter.ExternalAdapterRegistrationClass;
import com.cognizant.trumobi.log.PersonaLog;

import com.cognizant.trumobi.persona.utils.PersonaCommonfunctions;
/**
 * 
 * @author rudhra stopped timer when Enter Pin screen in foreground
 * resolved delayed launch of Lock screen
 *
 */
public class PersonaSettingsAutoLock extends DialogFragment {
	
	ListView mListView;
	AlertDialog.Builder builder;
	private ArrayAdapter<String> mAdapter;
	
	private static String TAG=PersonaSettingsAutoLock.class.getName();
	
	private ArrayList<String> timerList;
	
	private ExternalAdapterRegistrationClass mExtAdapReg;
	
	private ArrayAdapter<ExternalLockTime> mAutoLockAdapter;
	 TextView autoLockTimeLabel;
	private enum ExternalLockTime 
	{
		       Never("Never"),
		       Fifteen("15 sec"),
		       Thirty("30 sec"),
		       Minute("1 minute"),
		       TwoMinutes("2 minutes"),
		       FiveMinutes("5 minutes"),
		       TenMinutes("10 minutes"),
		       QuarterHour("15 minutes"),
		       HalfHour("30 minutes"),
		       Hour("1 hour");
		
		       private String mValue;
		
		       private ExternalLockTime(String value) {
		           mValue = value;
		       }
		
		       public String getValue() {
		           return mValue;
		       }
		
		       @Override
				public String toString() {
					// TODO Auto-generated method stub
					return mValue;
				}
		              
		       public int getLockTimeIndex(String value){
		    	   int ret = 0;
		    	   
		    	   
		    	   
		    	   return ret;
		       }
		       
		   //290778 commented due to build failure    
		 /*      public static ExternalLockTime valueOf(Class<ExternalLockTime> enumType,String lockTimeStr){
		    	   if(lockTimeStr.contains("15"))
		    		   return ExternalLockTime.Fifteen;
		    	   else if(lockTimeStr.contains("30"))
		    		   return ExternalLockTime.Thirty;
		    	   else if(lockTimeStr.contains("60"))
		    		   return ExternalLockTime.Minute;
		    	   else if(lockTimeStr.contains("120"))
		    		   return ExternalLockTime.TwoMinutes;
		    	   else if(lockTimeStr.contains("300"))
		    		   return ExternalLockTime.FiveMinutes;
		    	   else if(lockTimeStr.contains("600"))
		    		   return ExternalLockTime.TenMinutes;
		    	   else if(lockTimeStr.contains("900"))
		    		   return ExternalLockTime.Fifteen;
		    	   else if(lockTimeStr.contains("1800"))
		    		   return ExternalLockTime.HalfHour;
		    	   else if(lockTimeStr.contains("3600"))
		    		   return ExternalLockTime.Hour;
		    	   else
		    		   return ExternalLockTime.Never;
		       }
		       */
	}
	
	
	
	
	 @Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container,
	   Bundle savedInstanceState) {
	
		 PersonaLog.d(TAG, "===== In onCreateView ====");
	  
	//  	View v1 = inflater.inflate(R.layout.pr_autolock_list_container, container, false);
		 View v1 = inflater.inflate(R.layout.pr_autolock_list_container, container, false);
	
	//    mListView = (ListView) v1.findViewById(android.R.id.list);
	   
		// mListView = (ListView) v1.findViewById(R.id.listofURLs);
	    PersonaLog.d(TAG, "===== View Returned ====");
	    return v1;
	    
	 }

	 
	 public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        PersonaLog.d(TAG, "===== In onCreate ====");
	        
	        mExtAdapReg = ExternalAdapterRegistrationClass.getInstance(getActivity().getApplicationContext());
	       
	    }
	 

	    
/*	    @Override
	public void setSelection(int position) {
	
	    	
		super.setSelection(position);
		PersonaLog.d(TAG,"===== In setSelection ====");
		PersonaLog.d(TAG,"==== value of position:"+position);
		mListView.setSelection(position);
		mListView.setItemChecked(position, true);
	}
*/

		@Override
	    public void onActivityCreated(Bundle savedInstanceState) 
	    {
	        super.onActivityCreated(savedInstanceState);
	        PersonaLog.d(TAG, "===== In onActivityCreated ====");
	        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			/*
			 * imm.hideSoftInputFromWindow(getCurrentFocus() .getWindowToken(),
			 * 0);
			 */
			if (imm.isActive()) {
				imm.hideSoftInputFromWindow(getView().getApplicationWindowToken(), 0);
			}
	        setDefaultAutoLockTimerValue();
	       TextView autoLockLabel=(TextView)getView().findViewById(R.id.prautoLockLabel);
	        autoLockLabel.setText("Autolock the application");
	        autoLockTimeLabel=(TextView)getView().findViewById(R.id.prautoLocktimeLabel);
	   
	      String lastSavedAutoLockTime=new SharedPreferences(getActivity()).getString("selected_autolocktime","1 minute");
	        autoLockTimeLabel.setText(lastSavedAutoLockTime);
	        ImageView navigation=(ImageView)getView().findViewById(R.id.navigationArrow);
	       navigation.setOnClickListener(new View.OnClickListener() {
	    	  
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					//Intent intent=new Intent(getActivity(),PersonaAutoLockPreferenceActivity.class);
					//startActivity(intent);
					
					   builder = new AlertDialog.Builder(getActivity());
					   builder.setNegativeButton(R.string.pr_cancel_action, new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							dialog.cancel();
						}
						   
					   });
				      
					    int previouslySetTime=new SharedPreferences(getActivity()).getInt("selected_position",3);
				       builder.setSingleChoiceItems(R.array.personaAutoLock, previouslySetTime,new DialogInterface.OnClickListener() {
				                  public void onClick(DialogInterface dialog, int position) {
				                  // The 'position' argument contains the index position
				                  // of the selected item
				                	  
				                	  saveSelectedLockTime(mAutoLockAdapter.getItem(position));
				          		
				          			dialog.cancel();
				                  }
				                  
				       }); 
				       AlertDialog dialog = builder.create();
				      
				       dialog.show();
					
					
				}
			});
	        
	       mAutoLockAdapter = new ArrayAdapter<ExternalLockTime>(getActivity(),R.layout.pr_settings_autolock_list_layout,ExternalLockTime.values());
		
	    
	     //  mListView.setAdapter(mAutoLockAdapter);
	 //      mListView.setSelection(ListView.CHOICE_MODE_SINGLE);
	      //  mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
	        
	        
	       //
	      
	  
	  

	    }
	    
	 
		
	  
	    
	    
	


	/*	@Override
		public void onListItemClick(ListView l, View v, int position, long id) {
			// TODO Auto-generated method stub
			super.onListItemClick(l, v, position, id);
			
			PersonaLog.d(TAG,"==== position:"+position);
			PersonaLog.d(TAG,"===== selected item :"+mAutoLockAdapter.getItem(position));
		//	saveSelectedLockTime(mAutoLockAdapter.getItem(position));
			//new SharedPreferences(getActivity()).edit().putInt("selected_position",position).commit();
		
		}
	  */  
	    private void saveSelectedLockTime(PersonaSettingsAutoLock.ExternalLockTime eLockTime){
	    	PersonaLog.d(TAG,"===== saveSelectedLockTime ===="+eLockTime.mValue);
	    	PersonaLog.d(TAG,"===== saveSelectedLockTime ===="+eLockTime.toString());
	    	PersonaLog.d(TAG,"===== saveSelectedLockTime ===="+eLockTime.getValue());
	    	PersonaLog.d(TAG, "====== selected Position=="+eLockTime.ordinal());
	    	
	    	PersonaCommonfunctions prcmnFn = new PersonaCommonfunctions(getActivity());
	    	
	    	//int autoLockTimeInteger = prcmnFn.convertAutoLockTimerStringToInt(eLockTime.toString());
	    	long autoLockTimeInteger = prcmnFn.convertAutoLockTimerStringToInt(eLockTime.toString());
	    	PersonaLog.d(TAG,"===== Value of autoLockTimeInteger"+autoLockTimeInteger);
	    	autoLockTimeLabel.setText(eLockTime.toString());
	    	new SharedPreferences(getActivity()).edit().putString("selected_autolocktime", eLockTime.toString()).commit();
	    	//new SharedPreferences(getActivity()).edit().putInt("selected_autolocktime_c", autoLockTimeInteger).commit();
	    	new SharedPreferences(getActivity()).edit().putLong("selected_autolocktime_l", autoLockTimeInteger).commit();
	    	new SharedPreferences(getActivity()).edit().putInt("selected_position", eLockTime.ordinal()).commit();
	    	TruMobiTimerClass.userInteractedStopTimer();
	    	TruMobiTimerClass.startTimerTrigger(getActivity());
	    }


		@Override
		public void onResume() {
			// TODO Auto-generated method stub
			super.onResume();
			PersonaLog.d(TAG, "===== onResume =====");
			
			
		
			int prevSelPos = new SharedPreferences(getActivity()).getInt("selected_position",-1);
			PersonaLog.d(TAG,"====== prev selected pos"+prevSelPos);
			if(prevSelPos == -1)
			{
				setDefaultAutoLockTimerValue();
			}
			else{
				
			//	mListView.setItemChecked(prevSelPos, true);
			//	mListView.setSelection(prevSelPos);
			}
		
		}


		@Override
		public void setInitialSavedState(SavedState state) {
			// TODO Auto-generated method stub
			super.setInitialSavedState(state);
			
		}
	    
		private void setDefaultAutoLockTimerValue(){
			PersonaLog.d(TAG,"===== In setDefaultAutoLockTimerValue ====");
			Integer _autoLockTimer = mExtAdapReg.getExternalPIMSettingsInfo().PasswordFailLockout.getValue();
			String _lockTimeStr = _autoLockTimer.toString();
			PersonaLog.d(TAG," Value of _autoLockTimer:"+_autoLockTimer);
			PersonaLog.d(TAG," Value of _lockTimeStr:"+_lockTimeStr);
			
			// 290778 COMMENTED OUT FOR build failure -CRASH 
	//		PersonaLog.d(TAG,"=== ExternalLockTime Ordinal ==="+ExternalLockTime.valueOf(ExternalLockTime.class,_lockTimeStr).ordinal());
	//		int enumPosition= ExternalLockTime.valueOf(ExternalLockTime.class,_lockTimeStr).ordinal();
			// COMMENTED OUT FOR CRASH 
			
			int enumPosition = 0;
			PersonaLog.d(TAG,"==== Value of enumPosition:"+enumPosition);
			
			//mListView.setItemChecked(enumPosition, true);
			//mListView.setSelection(enumPosition);
			PersonaLog.d(TAG,"===== out setDefaultAutoLockTimerValue ====");
		}


		public PersonaSettingsAutoLock() {
		// TODO Auto-generated constructor stub
	}
		
	    
}
