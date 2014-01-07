package com.cognizant.trumobi.dialer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.provider.CallLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.cognizant.trumobi.R;
import com.cognizant.trumobi.commonabstractclass.TruMobiBaseSherlockActivity;
import com.cognizant.trumobi.dialer.db.DialerCorporateTable;
import com.cognizant.trumobi.dialer.utils.DialerUtilities;
import com.cognizant.trumobi.em.Email;


public class DialerShowDBActivity extends TruMobiBaseSherlockActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialer_show_db);
		final ListView listView = (ListView) findViewById(R.id.listView);

		ArrayList<DialerCorporateTable> dialerCorporateTables = getCorporateContentProviderList();
		ArrayList<DialerCorporateTable> dialerCorporateTablesList = new ArrayList<DialerCorporateTable>();

		if (dialerCorporateTables != null && dialerCorporateTables.size() > 0) {
			for (int i = 0; i < dialerCorporateTables.size(); i++) {
				DialerCorporateTable dialer = dialerCorporateTables.get(i);

				if (dialer.getDate() != null) {
					Date expiry = new Date(Long.parseLong(dialer.getDate()));
					dialer.setDate(DialerUtilities.frmEpochDate(expiry));

				}

				if (dialer.getCallType() != null) {
					String call = dialer.getCallType();

					if (call.equalsIgnoreCase("1") || call.equalsIgnoreCase("4")) {
						dialer.setCallType("Incoming");
					} else if (call.equalsIgnoreCase("2")) {
						dialer.setCallType("Outgoing");
					} else if (call.equalsIgnoreCase("3")) {
						dialer.setCallType("Missed");
					}

				}

				if (dialer.getCallDuration() != null) {
					String duration = getDurationString(Integer.parseInt(dialer
							.getCallDuration()));
					dialer.setCallDuration(duration);

				}

				if (dialer.isCorporate() != null) {
					Boolean result = (dialer.isCorporate()
							.equalsIgnoreCase("1")) ? true : false;
					dialer.setCorporate("" + result);
				}

				dialerCorporateTablesList.add(dialer);

			}
		}

		if (dialerCorporateTablesList != null
				&& dialerCorporateTablesList.size() > 0) {
			MyAdapter mAdapter = new MyAdapter(DialerShowDBActivity.this,
					R.layout.dialer_show_db_custom, dialerCorporateTablesList);

			listView.setAdapter(mAdapter);
		}
		if (dialerCorporateTablesList != null
				&& dialerCorporateTablesList.size() > 0) {
			// createCSVfile(dialerCorporateTablesList);
		}

	}

	public int getCorporateContentProviderSize() {
		String[] projection = new String[] { DialerCorporateTable.PHONE_NUMBER,
				DialerCorporateTable.CALL_TYPE,
				DialerCorporateTable.ASSOICIATE_NAME,
				DialerCorporateTable.DATE, DialerCorporateTable.NUMBER_TYPE,
				DialerCorporateTable.CALL_DURATION, DialerCorporateTable.ID };

		Cursor cursor = Email
				.getAppContext()
				.getContentResolver()
				.query(DialerCorporateTable.CONTENT_URI_DIALER, projection,
						null, null, CallLog.Calls.DEFAULT_SORT_ORDER);

		int size = cursor.getCount();
		if (cursor != null)
			cursor.close();

		return size;
	}

	public ArrayList<DialerCorporateTable> getCorporateContentProviderList() {
		ArrayList<DialerCorporateTable> dialerCorporateTablesList = null;
		String[] projection = new String[] { DialerCorporateTable.PHONE_NUMBER,
				DialerCorporateTable.CALL_TYPE,
				DialerCorporateTable.ASSOICIATE_NAME,
				DialerCorporateTable.DATE, DialerCorporateTable.NUMBER_TYPE,
				DialerCorporateTable.CALL_DURATION, DialerCorporateTable.ID,
				DialerCorporateTable.IS_CORPORATE };

		Cursor cursor = Email
				.getAppContext()
				.getContentResolver()
				.query(DialerCorporateTable.CONTENT_URI_DIALER, projection,
						null, null, CallLog.Calls.DEFAULT_SORT_ORDER);

		if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {

			try {

				dialerCorporateTablesList = new ArrayList<DialerCorporateTable>();

				do {

					DialerCorporateTable corporateTable = new DialerCorporateTable();

					String lastCallnumber = cursor.getString(0);
					corporateTable.setPhoneNumber(lastCallnumber);

					String type = cursor.getString(1);
					corporateTable.setCallType(type);

					String name = cursor.getString(2);
					corporateTable.setAssociateName(name);

					String date = cursor.getString(3);
					corporateTable.setDate(date);

					String numberType = cursor.getString(4);
					corporateTable.setNumberType(numberType);

					String duration = cursor.getString(5);
					corporateTable.setCallDuration(duration);

					String _id = cursor.getString(6);
					corporateTable.setId(_id);

					String isCorp = cursor.getString(7);
					corporateTable.setCorporate(isCorp);

					dialerCorporateTablesList.add(corporateTable);

				} while (cursor.moveToNext());
			} catch (Exception e) {

			}
			if (cursor != null)
				cursor.close();
		}

		return dialerCorporateTablesList;
	}

	@SuppressWarnings("unused")
	private void createCSVfile(
			ArrayList<DialerCorporateTable> dialerCorporateTablesList) {

		String comma = ",";
		String newLine = "\n";
		String filePath = Environment.getExternalStorageDirectory()
				+ File.separator + "CTS" + File.separator + "SafeSpace.csv";
		try {
			File file = new File(filePath);
			if (file.exists())
				file.delete();
			file.createNewFile();
			FileOutputStream fo = new FileOutputStream(file);
			OutputStreamWriter osw = new OutputStreamWriter(fo);
			osw.write("_id");
			osw.write(comma);
			osw.write("name");
			osw.write(comma);
			osw.write("phoneNumber");
			osw.write(comma);
			osw.write("callType");
			osw.write(comma);
			osw.write("date");
			osw.write(comma);
			osw.write("duration");
			osw.write(comma);
			osw.write("isCorporate");
			osw.write(newLine);
			for (int i = 0; i < dialerCorporateTablesList.size(); i++) {
				DialerCorporateTable dialerCorporateTable = dialerCorporateTablesList
						.get(i);

				osw.write(dialerCorporateTable.getId());
				osw.write(comma);

				if (dialerCorporateTable.getAssociateName() != null)
					osw.write(dialerCorporateTable.getAssociateName());
				osw.write(comma);

				osw.write(dialerCorporateTable.getPhoneNumber());
				osw.write(comma);

				osw.write(dialerCorporateTable.getCallType());
				osw.write(comma);

				if (dialerCorporateTable.getDate() != null) {
					osw.write(dialerCorporateTable.getDate());
				}
				osw.write(comma);

				if (dialerCorporateTable.getCallDuration() != null) {
					osw.write(dialerCorporateTable.getCallDuration());
				}
				osw.write(comma);

				osw.write(dialerCorporateTable.isCorporate());
				osw.write(newLine);
			}

			osw.flush();
			osw.close();
		} catch (Exception e) {

			e.printStackTrace();

		}

	}

	class MyAdapter extends ArrayAdapter<DialerCorporateTable> {

		private List<DialerCorporateTable> list;

		public MyAdapter(Context context, int resourseID,
				List<DialerCorporateTable> list) {
			super(context, resourseID, list);
			this.list = list;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public DialerCorporateTable getItem(int arg0) {
			return list.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View rowView = convertView;

			LayoutInflater inflater = (LayoutInflater) this.getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			rowView = inflater.inflate(R.layout.dialer_show_db_custom, parent,
					false);

			TextView phoneNumber = (TextView) rowView
					.findViewById(R.id.phoneNumber);
			TextView date = (TextView) rowView.findViewById(R.id.date);
			TextView associateName = (TextView) rowView
					.findViewById(R.id.associateName);
			TextView callType = (TextView) rowView.findViewById(R.id.callType);
			TextView callDuration = (TextView) rowView
					.findViewById(R.id.callDuration);
			TextView isCorporate = (TextView) rowView
					.findViewById(R.id.isCorporate);

			Object obj = list.get(position);
			if (obj instanceof DialerCorporateTable) {
				DialerCorporateTable dialer = (DialerCorporateTable) obj;

				if (dialer.getPhoneNumber() != null) {
					phoneNumber.setText(dialer.getPhoneNumber());
				}

				if (dialer.getDate() != null) {
					date.setText(dialer.getDate());
				}
				if (dialer.getAssociateName() != null) {
					associateName.setText(dialer.getAssociateName());

				}

				if (dialer.getCallType() != null) {
					callType.setText(dialer.getCallType());

				}

				if (dialer.getCallDuration() != null) {

					callDuration.setText(dialer.getCallDuration());

				}

				if (dialer.isCorporate() != null) {

					isCorporate.setText(dialer.isCorporate());
				}

			}
			return rowView;
		}
	}

	private String getDurationString(int seconds) {

		int hours = seconds / 3600;
		int minutes = (seconds % 3600) / 60;
		seconds = seconds % 60;

		return twoDigitString(hours) + " : " + twoDigitString(minutes) + " : "
				+ twoDigitString(seconds);
	}

	private String twoDigitString(int number) {

		if (number == 0) {
			return "00";
		}

		if (number / 10 == 0) {
			return "0" + number;
		}

		return String.valueOf(number);
	}
}
