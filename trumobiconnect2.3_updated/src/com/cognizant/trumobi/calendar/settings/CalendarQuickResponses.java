package com.cognizant.trumobi.calendar.settings;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.view.MenuItem;
import com.cognizant.trumobi.R;
import com.cognizant.trumobi.calendar.util.CalendarConstants;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseSherlockFragmentBaseActivity;

public class CalendarQuickResponses extends TruMobiBaseSherlockFragmentBaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.cal_generalsettings);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(CalendarConstants.QUICK_RESPONSE);
		getSupportActionBar().setIcon(R.drawable.pr_calendar_icon);
		String[] items = getResources().getStringArray(
				R.array.preferences_quick_responses);
		ArrayAdapter<String> adaptersettings = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1, items);

		ListView lstSettings = (ListView) findViewById(R.id.mainmenulist);
		lstSettings.setAdapter(adaptersettings);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case android.R.id.home:
			finish();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

}