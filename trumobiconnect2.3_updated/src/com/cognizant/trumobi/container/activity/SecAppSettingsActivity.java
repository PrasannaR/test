package com.cognizant.trumobi.container.activity;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseActivity;
import com.cognizant.trumobi.container.Utils.CustomListAdapterSetting;
import com.cognizant.trumobi.container.Utils.OutlookPreference;
import com.cognizant.trumobi.container.Utils.SettingItem;
import com.cognizant.trumobi.container.Utils.UtilList;

/**
 * Activity which displays a list with grouped mail items to the user, with
 * provision to select the grouping parameter
 * 
 * @author Saran Kumar N M. <SaranKumar.NachimuthuManian@cognizant.com>
 * 
 */

public class SecAppSettingsActivity extends TruMobiBaseActivity {

	public static int pos1 = 0;

	public ListView lv1;
	public TextView mViewItem;
	PendingIntent sender;
	ImageView settingsBack;
	TextView settingheader;

	Context ctx;

	public static ArrayList<SettingItem> image_details;
	CustomListAdapterSetting test;
	ArrayList<SettingItem> results = new ArrayList<SettingItem>();

	protected OnClickListener listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			finish();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.con_settings_list_activity);
		image_details = getListData();

		settingheader = (TextView) findViewById(R.id.txt_settings);
		settingheader.setTypeface(UtilList.getTextTypeFaceBold(this));

		ctx = this;
		lv1 = (ListView) findViewById(R.id.custom_list);
		settingsBack = (ImageView) findViewById(R.id.settings_back);
		settingsBack.setOnClickListener(listener);

		//settingheader.setOnClickListener(listener);

		test = new CustomListAdapterSetting(this, image_details);
		lv1.setAdapter(test);
		registerForContextMenu(lv1);
		lv1.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> a, View v, int position,
					long id) {
				pos1 = position;
				if ((position == 0)) {
					RelativeLayout mView = (RelativeLayout) v;
					mViewItem = (TextView) mView.getChildAt(1);
				} /*else if (position == 1) {

					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
							SecAppSettingsActivity.this);

					alertDialogBuilder.setTitle(
							R.string.container_setting_logout_dialog)
							.setMessage("Are you sure you want to logout?");

					alertDialogBuilder

							.setCancelable(false)
							.setPositiveButton("Yes",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {

											Intent dataDeleted = new Intent();
											dataDeleted.putExtra("Broadcast",
													"Kill");
											dataDeleted
													.setAction("Restricted_App");
											sendBroadcast(dataDeleted);
											finish();

										}
									})
							.setNegativeButton("No",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {

											dialog.cancel();
										}
									});

					// create alert dialog
					AlertDialog alertDialog = alertDialogBuilder.create();

					// show it
					alertDialog.show();

				}*/
				openContextMenu(v);

			}

		});

	}

	private ArrayList<SettingItem> getListData() {

		SettingItem newsData = new SettingItem();
		newsData.setReporterName("Default View");

		/* SharedPreferences preferences = getSharedPreferences("pref", 0); */
		newsData.setDate(OutlookPreference.getInstance(this).getValue(
				getResources().getString(R.string.container_default_view),
				"Recently received"));
		results.add(newsData);

		/*newsData = new SettingItem();
		newsData.setReporterName("Logout");
		newsData.setDate("");
		results.add(newsData);*/

		return results;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		String[] menuItems;
		if (v.getId() == R.id.custom_list) {

			switch (pos1) {

			case 0:
				menuItems = getResources().getStringArray(
						R.array.container_menu0);
				for (int i = 0; i < menuItems.length; i++) {
					menu.add(Menu.NONE, i, i, menuItems[i]);
				}
				break;

			}

		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		String str = (String) item.getTitle();
		Log.i("", ":------> " + item.getItemId() + "    " + str);

		switch (pos1) {

		case 0:
			OutlookPreference.getInstance(this).setValue(
					getResources().getString(R.string.container_default_view),
					str);
			Intent dataDeleted = new Intent();
			dataDeleted.putExtra("Broadcast", "DefaultView");
			dataDeleted.setAction("Restricted_App");
			ctx.sendBroadcast(dataDeleted);

			break;

		}

		mViewItem.setText(str);
		return true;

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}
}
