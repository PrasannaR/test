package com.cognizant.trumobi.securebrowser;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class SB_UninstallBroadCastReceiver extends BroadcastReceiver {
	Context cxt;
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		this.cxt = context;		
		android.util.Log.e("getdata",""+intent.getData());
		// uninstall the secure browser app once the persona is uninstalled
		if(intent.getData()!=null){		
		android.util.Log.d("UninstallBroadCastReceiver","App uninstalled");
		if(intent.getData().equals("com.cognizant.trumobi.persona")) {			
			Log.d("persona secure browser", "uninstalled");
			//delete app cache data
			deleteLocalData();			
			//delete the downloaded data 
			 String PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()+"/SecureBrowser";
			 File file = new File(PATH);	
			 deleteDir(file);
			//any other action item(if required) on deletion of persona		
		}
		}
	}
//delte the cache data
public void deleteLocalData() {
		// TODO Auto-generated method stub	
		String cache = cxt.getCacheDir().getPath();
	    File appDir = new File(cache);
		if (appDir.exists()) {
			String[] children = appDir.list();
			for (String s : children) {
				if (!s.equals("lib")) {
					deleteDir(new File(appDir, s));

					//Log.i(LOG_TAG, "/data/data/APP_PACKAGE/" + s + " DELETED");
				}
			}

		}
		
	}	
	// delete the downloaded data
	public boolean deleteDir(File dir) {
		//Toast.makeText(cxt, "deleted the directory",Toast.LENGTH_LONG).show();
		Log.d("Secure browser", "deleted directory");
		
		if (dir != null && dir.isDirectory()) {
			String[] children = dir.list();
			int mChildrenLength = children.length;
			for (int i = 0; i < mChildrenLength; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();		

	}
}
