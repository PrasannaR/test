package com.cognizant.trumobi.calendar.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.view.MenuItem;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.calendar.provider.CalendarDatabaseHelper;
import com.cognizant.trumobi.calendar.util.CalendarConstants;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseSherlockFragmentBaseActivity;

public class CalendarSettings extends TruMobiBaseSherlockFragmentBaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cal_generalsettings);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setIcon(R.drawable.pr_calendar_icon);
		getSupportActionBar().setTitle(CalendarConstants.CALENDAR_SETTINGS);

		String[] items = getResources().getStringArray(R.array.generalsettings);
		if (CalendarDatabaseHelper.getEmail_ID() != null)
			items[1] = CalendarDatabaseHelper.getEmail_ID();
		ArrayAdapter<String> adaptersettings = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1, items);

		ListView lstSettings = (ListView) findViewById(R.id.mainmenulist);
		lstSettings.setAdapter(adaptersettings);

		lstSettings.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == 0) {
					Intent inte = new Intent(CalendarSettings.this,
							CalendarGeneralSettings.class);
					startActivity(inte);
				}
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case android.R.id.home:
			finish();
			overridePendingTransition(R.anim.cal_slide_in_right,
					R.anim.cal_slide_in_left);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {

		super.onBackPressed();
		overridePendingTransition(R.anim.cal_slide_in_right,
				R.anim.cal_slide_in_left);
	}
}
