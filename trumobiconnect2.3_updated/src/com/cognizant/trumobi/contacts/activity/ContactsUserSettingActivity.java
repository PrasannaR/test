package com.cognizant.trumobi.contacts.activity;

import java.util.Map.Entry;

import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.view.KeyEvent;

import com.TruBoxSDK.SharedPreferences;
import com.actionbarsherlock.view.MenuItem;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseSherlockPreferenceActivity;

public class ContactsUserSettingActivity extends TruMobiBaseSherlockPreferenceActivity {

	 private ListPreference mList_sort;
	 private ListPreference mList_view;
	 private android.content.SharedPreferences mInsecurePrefs;
	 private android.content.SharedPreferences mSecurePrefs;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.contacts_prefsettings);
		mInsecurePrefs = PreferenceManager.getDefaultSharedPreferences(this);
	    mSecurePrefs = new SharedPreferences(this);
	    mList_sort = (ListPreference) findPreference(getString(R.string.prefSort_Order));
		mList_view = (ListPreference) findPreference(getString(R.string.prefView_Type));
		
		this.setTitle("Settings");
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
            } else if (key.equals(getString(R.string.prefSort_Order))) {
            	final String value =mSecurePrefs.getString(key, null);
                mList_sort.setValue(value);
            }else if (key.equals(getString(R.string.prefView_Type))) {
            	 final String value = mSecurePrefs.getString(key, null);
                mList_view.setValue(value);
            }
        }
    }
	 
	
	
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {

		case android.R.id.home:
			final Editor insecureEditor = mInsecurePrefs.edit();
			final Editor secureEditor = mSecurePrefs.edit();

			String key = getString(R.string.prefSort_Order);
	        if (mInsecurePrefs.contains(key)) {
	            secureEditor.putString(key, mInsecurePrefs.getString(key, null));
	            insecureEditor.remove(key);
	        }
	        key = getString(R.string.prefView_Type);
	        if (mInsecurePrefs.contains(key)) {
	            secureEditor.putString(key, mInsecurePrefs.getString(key, null));
	            insecureEditor.remove(key);
	        }
	        secureEditor.putBoolean("isPrefUpdated", true);
	        insecureEditor.commit();
	        secureEditor.commit();

	        finish();
			break;
		default:
			break;
		}
		return true;
	}
	
	
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {    
        	
        	final Editor insecureEditor = mInsecurePrefs.edit();
			final Editor secureEditor = mSecurePrefs.edit();

			String key = getString(R.string.prefSort_Order);
	        if (mInsecurePrefs.contains(key)) {
	            secureEditor.putString(key, mInsecurePrefs.getString(key, null));
	            insecureEditor.remove(key);
	        }
	        key = getString(R.string.prefView_Type);
	        if (mInsecurePrefs.contains(key)) {
	            secureEditor.putString(key, mInsecurePrefs.getString(key, null));
	            insecureEditor.remove(key);
	        }
	        secureEditor.putBoolean("isPrefUpdated", true);
	        insecureEditor.commit();
	        secureEditor.commit();
			finish();
        }
        return true;
}
	
}
