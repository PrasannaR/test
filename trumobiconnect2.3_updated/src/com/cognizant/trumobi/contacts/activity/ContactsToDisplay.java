package com.cognizant.trumobi.contacts.activity;



import java.util.Map.Entry;

import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceManager;
import android.view.KeyEvent;

import com.TruBoxSDK.SharedPreferences;
import com.actionbarsherlock.view.MenuItem;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseSherlockPreferenceActivity;

public class ContactsToDisplay extends TruMobiBaseSherlockPreferenceActivity {

	private CheckBoxPreference mCheckBox;
    private android.content.SharedPreferences mInsecurePrefs;
    private android.content.SharedPreferences mSecurePrefs;
	
    
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.contacts_display_prefsettings);
		
		mInsecurePrefs = PreferenceManager.getDefaultSharedPreferences(this);
	    mSecurePrefs = new SharedPreferences(this);
	    mCheckBox = (CheckBoxPreference) findPreference(getString(R.string.prefNative_Contacts));
		this.setTitle("Contacts to display");
	    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	      
	}

	
	@Override
    public void onStart() {
        super.onStart();
        // Decrypt relevant key/value pairs, if they exist
        for (Entry<String, ?> entry : mSecurePrefs.getAll().entrySet()) {
            final String key = entry.getKey();
            if (key == null) {
                continue;
            } else if (key.equals(getString(R.string.prefNative_Contacts))) {
                mCheckBox.setChecked(mSecurePrefs.getBoolean(key, false));
            }
        }
    }
	 
	 
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			
			final Editor insecureEditor = mInsecurePrefs.edit();
			final Editor secureEditor = mSecurePrefs.edit();
		   
			String key = getString(R.string.prefNative_Contacts);
	        if (mInsecurePrefs.contains(key)) {
	            secureEditor.putBoolean(key, mInsecurePrefs.getBoolean(key, false));
	            insecureEditor.remove(key);
	        }
	        secureEditor.putInt("currentListIndex", 0);
	        secureEditor.putString("currentListRowID", null);
	        secureEditor.putBoolean("isPrefUpdated", true);
	        insecureEditor.commit();
	        secureEditor.commit();
	
			finish();
		default:
			break;
		}
		return false;
	}
	
	
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	

			final Editor insecureEditor = mInsecurePrefs.edit();
			final Editor secureEditor = mSecurePrefs.edit();
		   
			String key = getString(R.string.prefNative_Contacts);
	        if (mInsecurePrefs.contains(key)) {
	            secureEditor.putBoolean(key, mInsecurePrefs.getBoolean(key, false));
	            insecureEditor.remove(key);
	        }
	        secureEditor.putInt("currentListIndex", 0);
	        secureEditor.putString("currentListRowID", null);
	        secureEditor.putBoolean("isPrefUpdated", true);
	        insecureEditor.commit();
	        secureEditor.commit();
	
			finish();
        }
        return true;
}
	
	
}