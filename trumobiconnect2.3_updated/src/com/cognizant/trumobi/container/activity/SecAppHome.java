package com.cognizant.trumobi.container.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseActivity;
import com.cognizant.trumobi.container.Utils.OutlookPreference;
import com.cognizant.trumobi.container.Utils.UtilList;

public class SecAppHome extends TruMobiBaseActivity{

	String responseMessage, userId;
	public static SecAppHome app = null;
	LinearLayout prg;
	AlertDialog alertDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		

		super.onCreate(savedInstanceState);
		setContentView(R.layout.con_activity_main);
		prg = (LinearLayout) findViewById(R.id.login_status);
		prg.setVisibility(View.INVISIBLE);
		app = this;
		String syncTimeGMT = UtilList.manipulateTimeGMT(-7);

		Log.i("Tag ", "Time -7 " + syncTimeGMT);

		OutlookPreference.getInstance(this).setValue(
				getResources().getString(R.string.container_synctime_mail), syncTimeGMT);
		OutlookPreference.getInstance(this).setValue(
				getResources().getString(R.string.container_synctime_cal), syncTimeGMT);
		
		
		try{
		
		
				Intent launchActivity = new Intent(this,
						AttachmentListActivity.class);
				launchActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(launchActivity);
				finish();
		
		}catch(Exception e){
			
			//showAlertDialogForResult("Certificate not generated!",this);
			
			
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		try{
			
		if(alertDialog != null){
			alertDialog.dismiss();
			//stopService(new Intent(this, EasSyncService.class));
		}
		}catch(Exception e){
			
		}
		
	}

	public void showAlertDialogForResult(String msg,final Context ctx) {

		try {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					this);
			alertDialogBuilder.setTitle("Server Response");
			alertDialogBuilder
					.setMessage(msg)
					.setCancelable(false)
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									//stopService(new Intent(ctx, EasSyncService.class));
									finish();
								}
							});

			// create alert dialog
			alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		} catch (Exception e) {

		}
	}

}
