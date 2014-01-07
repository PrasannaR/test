package com.cognizant.trumobi.persona;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.log.PersonaLog;
import com.cognizant.trumobi.persona.constants.PersonaConstants;
import com.cognizant.trumobi.persona.net.PersonaAllAppsDetails;
import com.cognizant.trumobi.persona.net.PersonaAllAppsListDetails;
import com.cognizant.trumobi.persona.net.PersonaAllAppsSuccessResponse;
import com.cognizant.trumobi.persona.net.PersonaGetJsonInterface;
import com.cognizant.trumobi.persona.utils.PersonaCommonfunctions;
import com.google.gson.Gson;

public class PersonaMandatoryAppsScreen extends Activity implements PersonaGetJsonInterface {

	GridView gridView;
	LinkedList<PersonaAllAppsListDetails> allAppsLists;
	PersonaAppListAdapter adapter;
	ArrayList<String> PackageNameList;
	ArrayList<String> fileName;
	PersonaCommonfunctions mCommonfunctions;
	HashMap<String, Boolean> bundleIdInfo;
	String EASLauncherName;
	Context mContext;
	ProgressDialog dialog;
	PersonaDownloadApk personaDownloadApk;
	SharedPreferences sharedPreferences;
	PersonaMandatoryAppsScreen mMandatoryAppsScreen;
	LinkedList<PersonaAllAppsListDetails> listDetails = null;
	Button logoutButton;
	boolean isCertificatedDownloaded;
	boolean isNotFirstTimeLaunch;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pr_persona_home_screen);
		mContext = this;
		mCommonfunctions = new PersonaCommonfunctions(mContext);
		mMandatoryAppsScreen = this;
		PackageNameList = getIntent().getStringArrayListExtra(
				"mandatoryAppPkgName");
		PersonaLog.e("PersonaMandatoryAppsScreen","PersonaApplicationConstants.appsDetails static variable is "+PersonaConstants.appsDetails);
		allAppsLists = PersonaConstants.appsDetails;
		sharedPreferences = getSharedPreferences(
				PersonaConstants.PERSONAPREFERENCESFILE, Context.MODE_PRIVATE);
		isCertificatedDownloaded = sharedPreferences.getBoolean(
				"certificate_downloaded", false);
		isNotFirstTimeLaunch = sharedPreferences.getBoolean(
				"isNotFirstTimeLaunch", false);

		if (!isCertificatedDownloaded && !isNotFirstTimeLaunch) {
			Editor editor = sharedPreferences.edit();
			editor.putBoolean("isNotFirstTimeLaunch", true);
			editor.commit();
			Intent UserCertificateIntent = new Intent(PersonaMandatoryAppsScreen.this,
					PersonaUserCertificateWarningActivity.class);
			startActivity(UserCertificateIntent);

		} 
			
			bundleIdInfo = new HashMap<String, Boolean>();

			PersonaLog.i("MandatoryAppsscreen", "oncreate");
			PersonaLog.i("MandatoryAppsscreen", "PackageNameList size"
					+ PackageNameList.size());
			// String[] mandatoryApps= PackageNameList.get(0);

			checkAppsInstalled(PackageNameList);

			logoutButton = (Button) findViewById(R.id.logoutButton);

			logoutButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View view) {
					// TODO Auto-generated method stub
					finish();
				}
			});

			updategridView();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (dialog != null && dialog.isShowing()) {
			dialog.cancel();
		}
	}

	private void installApplication(String apkUrl) {
		// TODO Auto-generated method stub
		if(!checkIfAppDownloadedAlready(apkUrl)) {
			if (mCommonfunctions.isConnectedToNetwork()){
				dialog = new ProgressDialog(mContext, R.style.NewDialog);
				dialog.setMessage("Downloading application...");
				dialog.setCancelable(false);
				dialog.show();
				callDownloadAsyncTask(apkUrl);
			}
			else {
				Toast.makeText(PersonaMandatoryAppsScreen.this,
						"Please connect to Internet to download application",
						Toast.LENGTH_LONG).show();
			}
		}
		else {
			callDownloadAsyncTask(apkUrl);
		}
		
	}
	
	

	private void callDownloadAsyncTask(String apkUrl) {
		personaDownloadApk = new PersonaDownloadApk(apkUrl, this, -1, -1, null,
				mMandatoryAppsScreen);
		personaDownloadApk.execute();
		
	}

	private boolean checkIfAppDownloadedAlready(String apkUrl) {
		String fileName = apkUrl.substring(apkUrl.lastIndexOf('/') + 1,
				apkUrl.length());
		String destinationFilePath = PersonaConstants.DOWNLOAD_PATH
				+ fileName;
		File apk = new File(destinationFilePath);
		if (apk.exists()) {
			return true;
		}
		return false;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		
/*		sharedPreferences = getSharedPreferences(
				PersonaConstants.PERSONAPREFERENCESFILE, Context.MODE_PRIVATE);
		isNotFirstTimeLaunch = sharedPreferences.getBoolean(
				"isNotFirstTimeLaunch", false);
		if( isNotFirstTimeLaunch)*/
		updateUi();

	}

	private void updateUi() {

		String responseMessage = mCommonfunctions.readMandatoryAppsList();
		ArrayList<String> mandatoryAppPkgName = new ArrayList<String>();
		Gson gson = new Gson();
		int listSize;
		PersonaAllAppsSuccessResponse personaAllAppsSuccessResponse = gson.fromJson(
				responseMessage, PersonaAllAppsSuccessResponse.class);
		PersonaAllAppsDetails responseAppsDetails = personaAllAppsSuccessResponse.appstore_information;
		PersonaConstants.appsDetails = responseAppsDetails.appstore_app_items;
		listSize = PersonaConstants.appsDetails.size();
		for (int i = 0; i < listSize; i++) {
			mandatoryAppPkgName
					.add(PersonaConstants.appsDetails.get(i).bundle_identifier);

		}

		allAppsLists = PersonaConstants.appsDetails;
		checkAppsInstalled(mandatoryAppPkgName);

		gridView = (GridView) findViewById(R.id.gridView1);
		adapter = new PersonaAppListAdapter(PersonaMandatoryAppsScreen.this, allAppsLists);

		gridView.setAdapter(adapter);
		adapter.notifyDataSetChanged();

	}

	private void checkAppsInstalled(ArrayList<String> mandatoryapps) {

		for (int i = 0; i < mandatoryapps.size(); i++) {
			PackageManager pm = getPackageManager();
		//	boolean app_installed = false;
			try {
				pm.getPackageInfo(mandatoryapps.get(i),
						PackageManager.GET_ACTIVITIES);
			//	app_installed = true;
				if(allAppsLists!= null)
				allAppsLists.get(i).setInstalled(true);
			} catch (PackageManager.NameNotFoundException e) {
		//		app_installed = false;
			}
			/*
			 * if (!app_installed) {
			 * 
			 * String appUrl = PersonaAPIConstants.STAGING + "staticvip/"+ urls[i];
			 * //downloadapk and install PersonaDownloadApk personaDownloadApk = new
			 * PersonaDownloadApk(appUrl,PersonaMandatoryAppsScreen.this);
			 * personaDownloadApk.execute();
			 * 
			 * } else { PersonaLog.i("persona", mandatoryapps.get(i) +
			 * "already installed"); }
			 */
		}
		PersonaLog.i("Mandatory", "list cleared");

		PackageNameList.clear();
		PersonaLog.i("MandatoryAppsscreen", "PackageNameList size after clear"
				+ PackageNameList.size());

	}

	private String checkAppPackage(String packageName) {
		// TODO Auto-generated method stub

		if (packageName == null) {
			PersonaLog.e("PersonaMandatoryAppsScreen", "packagename is null");
			return "packagename is null";
		}
		try {
			final PackageManager pm = getPackageManager();

			Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
			mainIntent.addCategory(Intent.CATEGORY_DEFAULT);

			List<ResolveInfo> appList = pm.queryIntentActivities(mainIntent, 0);
			Collections
					.sort(appList, new ResolveInfo.DisplayNameComparator(pm));

			for (ResolveInfo temp : appList) {

				PersonaLog.d("my logs", "package and activity name = "
						+ temp.activityInfo.packageName + "    "
						+ temp.activityInfo.name);
				if (temp.activityInfo.packageName.equals(packageName)) {
					EASLauncherName = temp.activityInfo.name;
					return EASLauncherName;

				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "empty";
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		PersonaLog.i("MandatoryApps screen","configuration changed");		
		setContentView(R.layout.pr_persona_home_screen);
		updategridView();
		if(dialog != null && dialog.isShowing()) {
			PersonaLog.i("MandatoryApps screen","dialog showing");	
			dialog.show();
		}
	    
	}

	private void updategridView() {
		gridView = (GridView) findViewById(R.id.gridView1);
		adapter = new PersonaAppListAdapter(PersonaMandatoryAppsScreen.this,
				PersonaConstants.appsDetails);

		gridView.setAdapter(adapter);
		adapter.notifyDataSetChanged();

		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {

				// view.setBackgroundColor(getResources().getColor(R.color.COLOR_LIGHT_GRAY));
				view.setBackgroundResource(R.drawable.pr_list_selected_holo);
				PersonaLog.d("onItemClick", "position [" + position + "]");
				
				if(allAppsLists!=null) {
				String packageName = allAppsLists.get(position).bundle_identifier;
				if (allAppsLists.get(position).getAppInstalled()) {
					String LauncherActivityName = checkAppPackage(packageName);

					if (LauncherActivityName.equals("empty")) {
						Toast.makeText(PersonaMandatoryAppsScreen.this,
								getResources().getString(R.string.error_opening_app),
								Toast.LENGTH_LONG).show();

					} else {
						Intent intent = new Intent();
						intent.setClassName(packageName,
								LauncherActivityName);
						startActivity(intent);
					}
				} else {
					String apkUrl = PersonaConstants.STAGING + "staticvip/"
							+ allAppsLists.get(position).binary_source_path;
					installApplication(apkUrl);
				}
				}

			}

		});

		gridView.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View view,
					int position, long id) {
				view.setBackgroundResource(R.drawable.pr_list_selected_holo);
				PersonaLog.d("onItemClick", "position [" + position + "]");
				String packageName = allAppsLists.get(position).bundle_identifier;
				if (allAppsLists.get(position).getAppInstalled()) {
					String LauncherActivityName = checkAppPackage(packageName);

					if (LauncherActivityName.equals("empty")) {
						Toast.makeText(PersonaMandatoryAppsScreen.this,
								getResources().getString(R.string.error_opening_app),
								Toast.LENGTH_LONG).show();
						

					} else {
						Intent intent = new Intent();
						intent.setClassName(packageName,
								LauncherActivityName);
						startActivity(intent);
					}
				} else {
					String apkUrl = PersonaConstants.STAGING + "staticvip/"
							+ allAppsLists.get(position).binary_source_path;
					installApplication(apkUrl);
				}// TODO Auto-generated method stub

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}

		});
		
	}

	@Override
	public void onRemoteCallComplete(Message json, int code) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRemoteCallComplete(String json, int code) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRemoteCallComplete(String[] fileName, int code) {
		// TODO Auto-generated method stub
		PersonaLog.i("PersonaMandatoryAppsScreen", "onRemoteCallComplete");
		for (int i = 0; i < fileName.length; i++) {
			String apkName = PersonaConstants.DOWNLOAD_PATH + fileName[i];
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(new File(apkName)),
					"application/vnd.android.package-archive");
			mContext.startActivity(intent);
		}

	}

	@Override
	public void onRemoteCallComplete(ArrayList<String> json, int code) {
		// TODO Auto-generated method stub

	}
	
	

}
